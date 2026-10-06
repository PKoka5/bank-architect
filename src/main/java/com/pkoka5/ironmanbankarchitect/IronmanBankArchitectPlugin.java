package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.google.common.cache.*;
import com.pkoka5.ironmanbankarchitect.analysis.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import com.pkoka5.ironmanbankarchitect.overlay.*;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.*;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.*;
import net.runelite.client.game.*;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.*;
import org.slf4j.*;


@PluginDescriptor(
	name = "Bank Architect",
	description = "Design your own bank tabs, then sort them by hand with read-only move guidance.",
	tags = {"bank", "banking", "tabs", "layout", "planner", "blueprint",
		"organize", "organise", "organization", "sort", "sorting", "tags", "ironman"}
)
public final class IronmanBankArchitectPlugin extends Plugin
{
	static final String PLUGIN_NAME = "Bank Architect";

	private static final String ASSIGN_MENU_OPTION = "Bank Architect";
	private static final String CLEAR_OVERRIDE_OPTION = "Use automatic classification";
	private static final Color ASSIGN_MENU_COLOR = new Color(242, 169, 59);
	private static final String[] OPTION_KEYS = {"alchPile", "gearLayout", "utilitiesLayout", "toolsLayout",
		"resourcesLayout", "cluesLayout", "keepDoseRows", "fillHerbloreRows", "potionDoses", "runeOrder",
		"teleportOrder", "gatherFrequentlyUsed"};

	private static final Logger log = LoggerFactory.getLogger(IronmanBankArchitectPlugin.class);

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private IronmanBankArchitectConfig config;

	@Inject
	private ConfigManager configManager;
	private PresetSettings settings;

	@Inject
	private ScheduledExecutorService analysisExecutor;

	private NavigationButton navigationButton;
	private IronmanBankArchitectPanel panel;
	private BankGuideController guideController;
	private BankAnalysis bankAnalysis;
	private BankGuideOverlay guideOverlay;
	private BankCategoryOverlay categoryOverlay;
	private final Cache<String, AsyncBufferedImage> itemIcons = CacheBuilder.newBuilder().maximumSize(2048).build();
	private final AtomicBoolean settingsRefreshQueued = new AtomicBoolean();
	/** The items the latest analysis request saw; null until a bank was captured. */
	private Map<Integer, List<Integer>> analyzedBankContents;

	@Override
	protected void startUp()
	{
		settingsRefreshQueued.set(false);
		initializeCustomProfiles();
		guideController = new BankGuideController(AllRoundIronmanPreset.create());
		guideController.setBankOpenedListener(this::onBankOpened);
		guideController.publishCategoryOverrideCount(categoryOverrides().size());
		bankAnalysis = new BankAnalysis(
			command -> clientThread.invoke(command),
			analysisExecutor,
			this::bankAnalysisRequest,
			guideController::publishBankAnalysis,
			CompositeItemCatalog.DEFAULT,
			activePreset());
		// Both overlays want the free canvas beside the bank; the shared claim
		// keeps the guidance panel off the destination legend on a small window.
		BankOverlayReservations reservations = new BankOverlayReservations();
		guideOverlay = new BankGuideOverlay(this, client, guideController, config, reservations, this::activePlan);
		overlayManager.add(guideOverlay);
		categoryOverlay = new BankCategoryOverlay(this, client, guideController, config,
			reservations);
		overlayManager.add(categoryOverlay);

		panel = new IronmanBankArchitectPanel(guideController, this::analyzeBank, this::renderItemIcon,
			this::resetCategoryOverrides, bankLayoutModel());
		panel.configureReleaseNotice(config::lastSeenRelease, config::setLastSeenRelease);
		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(createIcon())
			.panel(panel)
			.priority(5)
			.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		settingsRefreshQueued.set(false);
		if (bankAnalysis != null)
		{
			bankAnalysis.close();
			bankAnalysis = null;
		}

		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationButton = null;
		}

		if (guideOverlay != null)
		{
			overlayManager.remove(guideOverlay);
			guideOverlay = null;
		}

		if (categoryOverlay != null)
		{
			overlayManager.remove(categoryOverlay);
			categoryOverlay = null;
		}

		if (panel != null)
		{
			panel.shutdown();
		}

		// The executor belongs to the client and is shared, so it is never shut
		// down here. Closing bankAnalysis invalidates every queued callback.
		guideController = null;
		panel = null;
		itemIcons.invalidateAll();
		analyzedBankContents = null;
	}

	/**
	 * Offers the blueprint destinations on a bank item while assign mode is on,
	 * so the player can correct an item the bundled classification placed wrong.
	 * This only adds menu options; nothing is clicked or moved for the player.
	 */
	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		BankGuideController controller = guideController;
		if (controller == null || !controller.isCategoryAssignMode())
		{
			return;
		}

		int itemId = bankItemIdFor(event.getMenuEntries());
		if (itemId <= 0)
		{
			return;
		}

		ItemComposition composition = itemManager.getItemComposition(itemId);
		String itemName = composition == null ? "item" : composition.getName();
		MenuEntry parent = client.getMenu().createMenuEntry(1)
			.setOption(ASSIGN_MENU_OPTION)
			.setTarget(ColorUtil.wrapWithColorTag(itemName, ASSIGN_MENU_COLOR))
			.setType(MenuAction.RUNELITE);
		Menu submenu = parent.createSubMenu();

		// Tags rather than categories: the plan places tags, so a correction has
		// to name one or the player could not say which part of a bundle an item
		// belongs to. Built back to front because the menu renders bottom-up, so
		// the list reads in catalogue order on screen.
		Optional<String> current = categoryOverrides().categoryKeyFor(itemId);
		BankPreset menuPreset = activePreset();
		String menuProfile = activeProfileName();
		List<BankTag> tags = BankTags.all();
		for (int index = tags.size() - 1; index >= 0; index--)
		{
			BankTag tag = tags.get(index);
			boolean active = current.isPresent() && current.get().equals(tag.getKey());
			submenu.createMenuEntry(-1)
				.setOption((active ? "* " : "") + tag.getName())
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> { if (activePreset() == menuPreset && activeProfileName().equals(menuProfile))
					applyCategoryOverride(itemId, itemName, tag.getKey()); });
		}
		if (current.isPresent())
		{
			submenu.createMenuEntry(-1)
				.setOption(CLEAR_OVERRIDE_OPTION)
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> { if (activePreset() == menuPreset && activeProfileName().equals(menuProfile))
					applyCategoryOverride(itemId, itemName, null); });
		}
	}

	/**
	 * Canonical item ID of the bank slot the menu was opened on, or -1 when the
	 * menu does not belong to a bank item. Placeholders resolve to the real item
	 * so a correction made on one applies to the item itself.
	 */
	private int bankItemIdFor(MenuEntry[] entries)
	{
		for (MenuEntry entry : entries)
		{
			if (entry.getParam1() != InterfaceID.Bankmain.ITEMS)
			{
				continue;
			}
			// Prefer the widget's own item: it is the bank slot occupant even
			// when the entry itself carries no item ID.
			Widget widget = entry.getWidget();
			int itemId = widget == null ? entry.getItemId() : widget.getItemId();
			int canonical = BankItemIds.canonical(client, itemId);
			if (canonical > 0)
			{
				return canonical;
			}
		}
		return -1;
	}

	private void applyCategoryOverride(int itemId, String itemName, String categoryKey)
	{
		initializeCustomProfiles();
		UserCategoryOverrides overrides = categoryOverrides();
		overrides.put(itemId, categoryKey);
		persistCategoryOverrides(overrides);
		log.debug("Category override for {} ({}) set to {}", itemName, itemId, categoryKey);
		analyzeBank();
	}

	private void resetCategoryOverrides()
	{
		persistCategoryOverrides(new UserCategoryOverrides());
		analyzeBank();
	}

	private UserCategoryOverrides categoryOverrides()
	{
		return builtin() ? new UserCategoryOverrides() : UserCategoryOverrides.parse(preferences().get("categoryOverrides", ""));
	}

	private void persistCategoryOverrides(UserCategoryOverrides overrides)
	{
		if (!ensureCustom()) return;
		preferences().set("categoryOverrides", overrides.serialize());
		BankGuideController controller = guideController;
		if (controller != null)
		{
			controller.publishCategoryOverrideCount(overrides.size());
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		refreshSettings();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (guideController != null && IronmanBankArchitectConfig.GROUP.equals(event.getGroup())
			&& Arrays.asList(OPTION_KEYS).contains(event.getKey())
			&& !Objects.equals(event.getOldValue(), event.getNewValue()))
		{
			if (ensureCustom()) preferences().set(event.getKey(), event.getNewValue() == null ? "" : event.getNewValue());
		}
		if (IronmanBankArchitectConfig.GROUP.equals(event.getGroup())
			&& !Arrays.asList("suggestNextMove", "showCategoryOverlay", "hideSortedHighlights",
				"categoryOverlayOpacity", "lastSeenRelease").contains(event.getKey()))
			refreshSettings();
	}

	private void refreshSettings()
	{
		BankAnalysis analysis = bankAnalysis;
		BankGuideController controller = guideController;
		if (analysis == null || controller == null) return;
		analysis.invalidate();
		if (!settingsRefreshQueued.compareAndSet(false, true)) return;
		clientThread.invokeLater(() -> {
			if (bankAnalysis != analysis || guideController != controller) return;
			settingsRefreshQueued.set(false);
			analyzedBankContents = null;
			initializeCustomProfiles();
			controller.publishCategoryOverrideCount(categoryOverrides().size());
			IronmanBankArchitectPanel currentPanel = panel;
			if (currentPanel != null) SwingUtilities.invokeLater(() -> {
				if (panel == currentPanel) currentPanel.refreshSettings();
			});
			analyzeBank();
		});
	}

	/** The player's assignment of categories to bank destinations. */
	private BankLayoutPlan activePlan()
	{
		return builtin() ? BankLayoutPlan.defaultFor(activePreset())
			: BankLayoutPlan.parse(activePreset(), savedProfiles().activePlan());
	}

	private PresetSettings settings()
	{
		if (settings == null) settings = new PresetSettings(configManager, this::activePreset);
		return settings;
	}

	/** The player's layout choices that no plan can state for them. */
	private BankLayoutOptions activeOptions()
	{
		if (builtin()) return BankLayoutOptions.defaultFor(activePreset());
		return readOptions(preferences())
			.withBlockArrangements(BlockArrangements.parse(preferences().get("blockOrders", "")))
			.withItemOrders(BlueprintOrderProfiles.parse(settings().get("blueprintOrdersByProfile", ""))
				.forProfile(activeProfileName()));
	}

	private boolean builtin() { return savedProfiles().isDefaultActive(); }
	private PresetSettings preferences() { return settings().forProfile(activeProfileName()); }

	private BankLayoutOptions readOptions(PresetSettings source)
	{
		boolean main = activePreset().getType() == BankPresetType.MAIN;
		// A layout choice exists only where packed and sorted genuinely differ:
		// where a category has curated geometry to keep or give up. Everywhere
		// else the two are a nudge apart, so no option is offered and packed
		// stands.
		Map<BankCategorySortMode, TabOrder> tabOrders = new EnumMap<>(BankCategorySortMode.class);
		TabOrder utilities = source.get("utilitiesLayout", main ? TabOrder.PACKED : config.utilitiesLayout());
		tabOrders.put(BankCategorySortMode.MAIN, utilities);
		tabOrders.put(BankCategorySortMode.TELEPORTS, utilities);
		tabOrders.put(BankCategorySortMode.CURRENCY, utilities);
		tabOrders.put(BankCategorySortMode.SUPPLIES,
			source.get("keepDoseRows", main || config.keepDoseRows()) ? TabOrder.PACKED : TabOrder.SEQUENTIAL);
		tabOrders.put(BankCategorySortMode.TOOLS, source.get("toolsLayout", main ? TabOrder.PACKED : config.toolsLayout()));
		tabOrders.put(BankCategorySortMode.RESOURCES, source.get("resourcesLayout", main ? TabOrder.PACKED : config.resourcesLayout()));
		tabOrders.put(BankCategorySortMode.CLUES, source.get("cluesLayout", main ? TabOrder.PACKED : config.cluesLayout()));
		return new BankLayoutOptions(true, !main && source.get("fillHerbloreRows", config.fillHerbloreRows()),
			source.get("alchPile", main || config.alchPile()), tabOrders,
			source.get("gearLayout", main ? GearLayout.GRID_STYLES : config.gearLayout()),
			main ? PotionDoseOrder.BY_FAMILY : source.get("potionDoses", config.potionDoses()),
			source.get("runeOrder", main ? RuneOrder.ELEMENTAL : config.runeOrder()),
			source.get("teleportOrder", main ? TeleportOrder.ALPHABETICAL : config.teleportOrder()),
			!main && source.get("gatherFrequentlyUsed", config.gatherFrequentlyUsed()));
	}

	private Object[] optionValues(BankLayoutOptions options)
	{
		return new Object[] {options.alchPile(), options.gearLayout(), options.orderFor(BankCategorySortMode.MAIN),
			options.orderFor(BankCategorySortMode.TOOLS), options.orderFor(BankCategorySortMode.RESOURCES),
			options.orderFor(BankCategorySortMode.CLUES), options.orderFor(BankCategorySortMode.SUPPLIES) == TabOrder.PACKED,
			options.fillHerbloreRows(), options.potionDoses(), options.runeOrder(), options.teleportOrder(), options.gatherFrequentlyUsed()};
	}

	private void storeOptions(PresetSettings target, BankLayoutOptions options)
	{
		Object[] values = optionValues(options);
		for (int index = 0; index < OPTION_KEYS.length; index++) target.set(OPTION_KEYS[index], values[index]);
	}

	private void copyPreferences(String name, BankLayoutOptions options, String corrections, String blocks)
	{
		PresetSettings target = settings().forProfile(name);
		storeOptions(target, options);
		target.set("categoryOverrides", corrections);
		target.set("blockOrders", blocks);
		target.set("profileReady", true);
	}

	private boolean ensureCustom()
	{
		if (!builtin()) return true;
		BankLayoutProfiles profiles = savedProfiles();
		String name = profiles.freeName("Custom layout");
		profiles = profiles.withProfile(name, activePlan().serialize());
		if (!name.equals(profiles.getActiveName()))
		{
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(panel,
				"Delete a custom preset before creating another one.", "Preset limit", JOptionPane.INFORMATION_MESSAGE));
			return false;
		}
		copyPreferences(name, activeOptions(), "", "");
		storeProfiles(profiles);
		return true;
	}

	/** Preserve the previous working layout, while leaving bundled presets immutable. */
	private void initializeCustomProfiles()
	{
		if (settings().get("customPresetsReady", false)) return;
		BankLayoutProfiles profiles = savedProfiles();
		String storedPlan = settings().get("tabOrder", "");
		BankLayoutPlan working = BankLayoutPlan.parse(activePreset(), storedPlan.isEmpty() ? profiles.activePlan() : storedPlan);
		String corrections = settings().get("categoryOverrides", "");
		String blocks = settings().get("blockOrders", "");
		BankLayoutOptions previous = readOptions(settings());
		BlueprintOrderProfiles orders = BlueprintOrderProfiles.parse(settings().get("blueprintOrdersByProfile", ""));
		BlueprintItemOrders items = orders.forProfile(activeProfileName());
		boolean modified = !working.isDefault(activePreset()) || !corrections.isEmpty() || !blocks.isEmpty()
			|| !items.serialize().isEmpty();
		Object[] previousValues = optionValues(previous);
		Object[] defaults = optionValues(BankLayoutOptions.defaultFor(activePreset()));
		for (int index = 0; index < OPTION_KEYS.length; index++)
			modified |= !settings().get(OPTION_KEYS[index], "").isEmpty() && !previousValues[index].equals(defaults[index]);
		if (modified || !profiles.isDefaultActive())
		{
			String name = profiles.isDefaultActive() ? profiles.freeName("Custom layout") : profiles.getActiveName();
			BankLayoutProfiles changed = profiles.withProfile(name, working.serialize());
			if (!name.equals(changed.getActiveName())) return;
			copyPreferences(name, previous, corrections, blocks);
			orders.put(name, items);
			settings().set("blueprintOrdersByProfile", orders.serialize());
			storeProfiles(changed);
		}
		settings().set("customPresetsReady", true);
	}

	/** The layouts the player has saved or imported, and which one they loaded. */
	private BankLayoutProfiles savedProfiles()
	{
		return BankLayoutProfiles.parse(settings().get("layoutProfiles", ""), settings().get("activeLayoutProfile", ""),
			BankLayoutProfiles.defaultName(activePreset()));
	}

	private String activeProfileName()
	{
		return activePreset().getType() == BankPresetType.MAIN ? savedProfiles().getActiveName()
			: BankLayoutShareCode.sanitize(settings().get("activeLayoutProfile", ""));
	}

	private void storeProfiles(BankLayoutProfiles profiles)
	{
		settings().set("layoutProfiles", profiles.serialize());
		settings().set("activeLayoutProfile", profiles.getActiveName());
	}

	/**
	 * Block orders snapshotted per profile name. The outer grammar is the
	 * profiles' own (names cannot contain the separators), while each value is
	 * a {@link BlockArrangements} string, whose grammar avoids both.
	 */
	private Map<String, String> blockOrderSnapshots()
	{
		Map<String, String> snapshots = new LinkedHashMap<>();
		String serialized = BankLayoutProfiles.repairActiveName(settings().get("blockOrdersByProfile", ""), settings().get("activeLayoutProfile", ""));
		if (serialized == null || serialized.isEmpty())
		{
			return snapshots;
		}
		for (String entry : serialized.split(";"))
		{
			int split = entry.indexOf('~');
			if (split > 0 && split < entry.length() - 1)
			{
				snapshots.put(BankLayoutShareCode.sanitize(entry.substring(0, split)), entry.substring(split + 1));
			}
		}
		return snapshots;
	}

	private void storeBlockOrderSnapshots(Map<String, String> snapshots)
	{
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : snapshots.entrySet())
		{
			if (entry.getValue() == null || entry.getValue().isEmpty())
			{
				continue;
			}
			if (builder.length() > 0)
			{
				builder.append(';');
			}
			builder.append(entry.getKey()).append('~').append(entry.getValue());
		}
		settings().set("blockOrdersByProfile", builder.toString());
	}

	/**
	 * The all-round preset. Its ten categories still own classification and
	 * layout; where their items end up is the plan's business, so the preset
	 * itself no longer changes when the player rearranges the bank.
	 */
	private BankPreset activePreset()
	{
		return "MAIN".equals(config.bankPreset()) ? BankPresets.MAIN : BankPresets.IRONMAN;
	}

	/**
	 * Stores a plan the player arranged in the layout screen and re-plans the
	 * bank from it. Only the placement of the tags changes: classification and
	 * corrections are keyed by category, never by destination or position.
	 */
	private BankLayoutModel bankLayoutModel()
	{
		initializeCustomProfiles();
		return new BankLayoutModel()
		{
			@Override
			public String editingContext()
			{
				return activePreset().getKey() + "|" + activeProfileName() + "|" + activePlan().serialize()
					+ "|" + settings().get("blueprintOrdersByProfile", "");
			}

			@Override
			public void saveItemOrder(int tab, List<Integer> itemIds,
				BankOrganizationPreview expected, String expectedContext,
				Consumer<Boolean> completed)
			{
				saveEdits(Collections.singletonMap(tab, itemIds), Collections.emptyMap(),
					itemIds.isEmpty(), expected, expectedContext, completed);
			}

			@Override
			public void saveBlueprintEdit(Map<Integer, List<Integer>> itemOrders,
				Map<String, BlueprintItemOrders.Destination> destinations,
				BankOrganizationPreview expected, String expectedContext,
				Consumer<Boolean> completed)
			{
				saveEdits(itemOrders, destinations, false, expected, expectedContext, completed);
			}

			private void saveEdits(Map<Integer, List<Integer>> itemOrders,
				Map<String, BlueprintItemOrders.Destination> destinations, boolean reset,
				BankOrganizationPreview expected, String expectedContext,
				Consumer<Boolean> completed)
			{
				Map<Integer, List<Integer>> requested = new HashMap<>();
				itemOrders.forEach((tab, ids) -> requested.put(tab, new ArrayList<>(ids)));
				Map<String, BlueprintItemOrders.Destination> routes = new HashMap<>(destinations);
				updateBlueprint(expected, expectedContext, completed, live -> {
					if (!requested.keySet().stream().allMatch(tab -> tab >= 0 && tab < expected.getCategories().size())
						|| !routes.values().stream().allMatch(route -> activePlan().destinationOf(route.tag) == route.tab)) return false;
					Map<Integer, Integer> before = new HashMap<>();
					Map<Integer, Integer> after = new HashMap<>();
					for (int tab = 0; tab < expected.getCategories().size(); tab++)
					{
						List<Integer> ids = new ArrayList<>();
						expected.getCategories().get(tab).getItems().forEach(item -> ids.add(item.getItemId()));
						ids.forEach(id -> before.merge(id, 1, Integer::sum));
						(reset ? ids : requested.getOrDefault(tab, ids)).forEach(id -> after.merge(id, 1, Integer::sum));
					}
					if (!before.equals(after)) return false;
					BlueprintItemOrders orders = activeOptions().itemOrders().withDestinations(routes);
					for (Map.Entry<Integer, List<Integer>> entry : requested.entrySet())
						orders = reset ? orders.resetTab(entry.getKey()) : orders.withTab(entry.getKey(), entry.getValue());
					return storeItemOrders(activeProfileName(), orders);
				});
			}

			private void updateBlueprint(BankOrganizationPreview expected, String expectedContext,
				Consumer<Boolean> completed, Function<BankSnapshot, Boolean> action)
			{
				clientThread.invoke(() -> {
					boolean success = false;
					if (expected != null && guideController != null && guideController.organizationPreview() == expected
						&& editingContext().equals(expectedContext) && activeOptions().itemOrders().isSupported())
					{
						Optional<BankSnapshot> live = BankSnapshotReader.readOpenBank(client);
						if (live.isPresent() && live.get().contents().equals(analyzedBankContents))
						{
							try { success = action.apply(live.get()); }
							catch (IllegalArgumentException changedBank) { log.debug("Blueprint save refused", changedBank); }
						}
					}
					final boolean result = success;
					SwingUtilities.invokeLater(() -> completed.accept(result));
				});
			}

			private boolean storeItemOrders(String name, BlueprintItemOrders orders)
			{
				if (builtin())
				{
					if (!ensureCustom()) return false;
					name = activeProfileName();
				}
				BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse(settings().get("blueprintOrdersByProfile", ""));
				profiles.put(name, orders);
				settings().set("blueprintOrdersByProfile", profiles.serialize());
				analyzeBank();
				return true;
			}

			@Override
			public void captureCurrentBank(String name, BankOrganizationPreview expected, String expectedContext,
				Consumer<Boolean> completed)
			{
				updateBlueprint(expected, expectedContext, completed, live -> {
					if (name == null || name.trim().isEmpty()) return false;
					Optional<Map<Integer, List<Integer>>> tabs = BankSnapshotReader.readTabOrders(client, live);
					if (!tabs.isPresent()) return false;
					BlueprintItemOrders captured = BlueprintItemOrders.capture(tabs.get(), expected, activeOptions().itemOrders());
					BankLayoutProfiles previous = savedProfiles();
					String free = previous.freeName(name);
					BankLayoutProfiles profiles = previous.withProfile(free, activePlan().serialize());
					if (!free.equals(profiles.getActiveName())) return false;
					copyPreferences(free, activeOptions(), categoryOverrides().serialize(), activeOptions().blockArrangements().serialize());
					storeProfiles(profiles);
					storeItemOrders(free, captured);
					return true;
				});
			}

			@Override
			public BankPreset preset()
			{
				return activePreset();
			}

			@Override
			public void selectPreset(BankPresetType type)
			{
				if (type != BankPresetType.IRONMAN && type != BankPresetType.MAIN
					|| type == activePreset().getType() && builtin()) return;
				if (bankAnalysis != null) bankAnalysis.invalidate();
				config.setBankPreset(type.name());
				initializeCustomProfiles();
				selectProfile(defaultProfileName());
				refreshSettings();
			}

			@Override
			public List<String> presetChoices()
			{
				List<String> choices = new ArrayList<>(Arrays.asList("Ironman", "Main"));
				for (BankPreset base : Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN))
				{
					BankLayoutProfiles profiles = BankLayoutProfiles.parse(settings().getFor(base, "layoutProfiles", ""),
						"", BankLayoutProfiles.defaultName(base));
					for (String name : profiles.names())
						if (!profiles.getDefaultName().equals(name)) choices.add(
							(base.getType() == BankPresetType.MAIN ? "Main: " : "Ironman: ") + name);
				}
				return choices;
			}

			@Override
			public BankLayoutPlan plan()
			{
				return activePlan();
			}

			@Override
			public void save(BankLayoutPlan plan)
			{
				if (!ensureCustom()) return;
				storeProfiles(savedProfiles().withProfile(activeProfileName(), plan.completedFor(activePreset()).serialize()));
				settings().set("tabOrder", plan.completedFor(activePreset()).serialize());
				analyzeBank();
			}

			@Override
			public List<String> profileNames()
			{
				return savedProfiles().names();
			}

			/** A custom retains its identity even when its plan matches a bundled preset. */
			@Override
			public String matchingProfile()
			{
				return savedProfiles().getActiveName();
			}

			@Override
			public void selectProfile(String name)
			{
				// The outgoing layout keeps its arrangements and the incoming
				// one brings its own back, so block orders belong to the
				// layout they were made for rather than bleeding across all.
				BankLayoutProfiles profiles = savedProfiles().withActive(name);
				if (!profiles.isDefaultActive() && !settings().forProfile(profiles.getActiveName()).get("profileReady", false))
					copyPreferences(profiles.getActiveName(), readOptions(settings()), settings().get("categoryOverrides", ""),
						blockOrderSnapshots().getOrDefault(profiles.getActiveName(), ""));
				storeProfiles(profiles);
				settings().set("tabOrder", BankLayoutPlan
					.parse(activePreset(), profiles.activePlan())
					.serialize());
				analyzeBank();
			}

			@Override
			public void saveProfile(String name, BankLayoutPlan plan)
			{
				BlueprintOrderProfiles itemProfiles = BlueprintOrderProfiles.parse(settings().get("blueprintOrdersByProfile", ""));
				BlueprintItemOrders previousItems = activeOptions().itemOrders();
				BankLayoutProfiles previous = savedProfiles();
				BankLayoutProfiles profiles = previous.withProfile(name,
					plan.completedFor(activePreset()).serialize());
				if (profiles == previous) return;
				copyPreferences(profiles.getActiveName(), activeOptions(), categoryOverrides().serialize(),
					activeOptions().blockArrangements().serialize());
				storeProfiles(profiles);
				itemProfiles.put(profiles.getActiveName(), previousItems);
				settings().set("blueprintOrdersByProfile", itemProfiles.serialize());
				save(plan);
			}

			@Override
			public void deleteProfile(String name)
			{
				if (defaultProfileName().equals(name)) return;
				if (name.equals(activeProfileName())) selectProfile(defaultProfileName());
				BlueprintOrderProfiles itemProfiles = BlueprintOrderProfiles.parse(settings().get("blueprintOrdersByProfile", ""));
				itemProfiles.remove(name);
				settings().set("blueprintOrdersByProfile", itemProfiles.serialize());
				storeProfiles(savedProfiles().without(name));
				Map<String, String> snapshots = blockOrderSnapshots();
				if (snapshots.remove(name) != null)
				{
					storeBlockOrderSnapshots(snapshots);
				}
				analyzeBank();
			}

			@Override
			public void saveBlockOrder(String tagKey, List<String> blockKeys)
			{
				if (!ensureCustom()) return;
				preferences().set("blockOrders", activeOptions().blockArrangements()
					.withTag(tagKey, blockKeys).serialize());
				analyzeBank();
			}

			@Override
			public BankLayoutOptions options()
			{
				return activeOptions();
			}

			@Override
			public void saveOptions(BankLayoutOptions options)
			{
				// The inverse of activeOptions(): the sidebar hands back a whole
				// options object, and each field goes home to the setting it came
				// from. The three utility categories share one setting, so MAIN
				// stands for all of them, and supplies asks its question as a
				// checkbox rather than an order.
				if (!ensureCustom()) return;
				storeOptions(preferences(), options);
				analyzeBank();
			}
		};
	}

	@Provides
	IronmanBankArchitectConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanBankArchitectConfig.class);
	}

	/**
	 * Under the auto-guide setting, an opening bank re-analyzes itself and arms
	 * the guide in its quiet form, so guidance is simply there when wanted and
	 * invisible when the bank is already in shape.
	 */
	private void onBankOpened()
	{
		if (!config.autoGuide())
		{
			return;
		}
		BankGuideController controller = guideController;
		if (controller == null)
		{
			return;
		}
		controller.enableGuideAutomatically();
		analyzeBank();
	}

	/**
	 * Under the auto-guide setting, a bank that gains or loses an item is
	 * analyzed again on the spot. The plan only knows the items it was built
	 * from, so a deposit or withdrawal would otherwise stall the guide on
	 * "contents changed" until the sidebar was used. Items merely changing
	 * places leave the contents alone and never trigger this, so a move the
	 * guide is verifying is not reset from under it.
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.BANK || !config.autoGuide())
		{
			return;
		}
		BankGuideController controller = guideController;
		if (controller == null || !controller.isBankOpen())
		{
			return;
		}
		Optional<BankSnapshot> snapshot = BankSnapshotReader.readOpenBank(client);
		if (snapshot.isPresent() && !snapshot.get().contents().equals(analyzedBankContents))
		{
			analyzeBank();
		}
	}

	private void analyzeBank()
	{
		BankAnalysis analysis = bankAnalysis;
		if (analysis != null)
		{
			analysis.analyzeBank();
		}
	}

	/** Captures every fact used by one request while on RuneLite's client thread. */
	private Optional<BankAnalysisRequest> bankAnalysisRequest()
	{
		Optional<BankSnapshot> snapshot = BankSnapshotReader.readOpenBank(client);
		if (!snapshot.isPresent())
		{
			return Optional.empty();
		}

		BankSnapshot bankSnapshot = snapshot.get();
		analyzedBankContents = bankSnapshot.contents();
		return Optional.of(new BankAnalysisRequest(bankSnapshot,
			collectGearStats(bankSnapshot), collectAlchValues(bankSnapshot),
			categoryOverrides().asMap(), activePlan(), activeOptions(), activePreset()));
	}

	private Map<Integer, Integer> collectAlchValues(BankSnapshot snapshot)
	{
		Map<Integer, Integer> valueById = new HashMap<>();
		for (BankItemSnapshot item : snapshot.getItems())
		{
			ItemComposition composition = itemManager.getItemComposition(item.getItemId());
			if (composition != null && composition.isTradeable())
			{
				valueById.put(item.getItemId(), composition.getHaPrice());
			}
		}

		return valueById;
	}

	private Map<Integer, GearStats> collectGearStats(BankSnapshot snapshot)
	{
		Map<Integer, GearStats> statsById = new HashMap<>();
		for (BankItemSnapshot item : snapshot.getItems())
		{
			gearStatsFor(item.getItemId()).ifPresent(stats -> statsById.put(item.getItemId(), stats));
		}

		return statsById;
	}

	private Optional<GearStats> gearStatsFor(int itemId)
	{
		ItemStats stats = itemManager.getItemStats(itemId);
		if (stats == null || !stats.isEquipable() || stats.getEquipment() == null)
		{
			return Optional.empty();
		}

		ItemEquipmentStats equipment = stats.getEquipment();
		GearSlot slot = GearSlot.fromRuneLiteSlot(equipment.getSlot());
		if (slot == null)
		{
			return Optional.empty();
		}

		// Magic damage is a percentage; keep it in tenths so the comparison never
		// depends on float equality.
		int magicDamageTenths = Math.round(equipment.getMdmg() * 10f);
		return Optional.of(new GearStats(slot, equipment.getAstab(), equipment.getAslash(), equipment.getAcrush(),
			equipment.getAmagic(), equipment.getArange(), equipment.getStr(), equipment.getRstr(),
			equipment.getPrayer(), equipment.getDstab(), equipment.getDslash(), equipment.getDcrush(),
			equipment.getDmagic(), equipment.getDrange(), magicDamageTenths, equipment.getAspeed()));
	}

	private void renderItemIcon(BankPreviewItem item, JLabel label)
	{
		if (item.getItemId() <= 0)
		{
			return;
		}

		String cacheKey = item.getItemId() + ":" + item.getQuantity();
		AsyncBufferedImage image = itemIcons.asMap().computeIfAbsent(cacheKey,
			key -> itemManager.getImage(item.getItemId(), item.getQuantity(), item.getQuantity() > 1));
		label.setText("");
		image.addTo(label);
	}

	// 16px version of the Plugin Hub icon.png: blueprint bank grid with a gold coin.
	static BufferedImage createIcon()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(27, 59, 111));
		graphics.fillRoundRect(1, 1, 14, 14, 6, 6);
		graphics.setColor(new Color(94, 135, 184));
		graphics.fillRect(3, 3, 4, 4);
		graphics.fillRect(9, 3, 4, 4);
		graphics.fillRect(3, 9, 4, 4);
		graphics.setColor(new Color(242, 169, 59));
		graphics.fillOval(8, 8, 7, 7);
		graphics.setColor(new Color(184, 122, 27));
		graphics.drawOval(8, 8, 7, 7);
		graphics.dispose();
		return icon;
	}
}

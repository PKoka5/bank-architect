package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest;
import com.pkoka5.ironmanbankarchitect.bank.BankItemIds;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankCategorySortMode;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankTag;
import com.pkoka5.ironmanbankarchitect.organize.BankTags;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import com.pkoka5.ironmanbankarchitect.organize.TabOrder;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import com.pkoka5.ironmanbankarchitect.overlay.BankCategoryOverlay;
import com.pkoka5.ironmanbankarchitect.overlay.BankGuideOverlay;
import com.pkoka5.ironmanbankarchitect.overlay.BankOverlayReservations;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JLabel;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private ScheduledExecutorService analysisExecutor;

	private NavigationButton navigationButton;
	private IronmanBankArchitectPanel panel;
	private BankGuideController guideController;
	private BankAnalysis bankAnalysis;
	private BankGuideOverlay guideOverlay;
	private BankCategoryOverlay categoryOverlay;
	private UserCategoryOverrides categoryOverrides = new UserCategoryOverrides();
	private final Map<String, AsyncBufferedImage> itemIcons = new ConcurrentHashMap<>();

	@Override
	protected void startUp()
	{
		guideController = new BankGuideController(AllRoundIronmanPreset.create());
		guideController.setBankOpenedListener(this::onBankOpened);
		categoryOverrides = UserCategoryOverrides.parse(config.categoryOverrides());
		guideController.publishCategoryOverrideCount(categoryOverrides.size());
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
		guideOverlay = new BankGuideOverlay(this, client, guideController, config, reservations);
		overlayManager.add(guideOverlay);
		categoryOverlay = new BankCategoryOverlay(this, client, guideController, config,
			reservations);
		overlayManager.add(categoryOverlay);

		panel = new IronmanBankArchitectPanel(guideController, this::analyzeBank, this::renderItemIcon,
			this::resetCategoryOverrides, bankLayoutModel());
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
		itemIcons.clear();
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
		Optional<String> current = categoryOverrides.categoryKeyFor(itemId);
		List<BankTag> tags = BankTags.all();
		for (int index = tags.size() - 1; index >= 0; index--)
		{
			BankTag tag = tags.get(index);
			boolean active = current.isPresent() && current.get().equals(tag.getKey());
			submenu.createMenuEntry(-1)
				.setOption((active ? "* " : "") + tag.getName())
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> applyCategoryOverride(itemId, itemName, tag.getKey()));
		}
		if (current.isPresent())
		{
			submenu.createMenuEntry(-1)
				.setOption(CLEAR_OVERRIDE_OPTION)
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> applyCategoryOverride(itemId, itemName, null));
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
			ItemComposition composition = client.getItemDefinition(itemId);
			int canonical = BankItemIds.canonical(itemId,
				composition == null ? -1 : composition.getPlaceholderTemplateId(),
				composition == null ? -1 : composition.getPlaceholderId());
			if (canonical > 0)
			{
				return canonical;
			}
		}
		return -1;
	}

	private void applyCategoryOverride(int itemId, String itemName, String categoryKey)
	{
		categoryOverrides.put(itemId, categoryKey);
		persistCategoryOverrides();
		log.debug("Category override for {} ({}) set to {}", itemName, itemId, categoryKey);
		analyzeBank();
	}

	private void resetCategoryOverrides()
	{
		categoryOverrides.clear();
		persistCategoryOverrides();
		analyzeBank();
	}

	private void persistCategoryOverrides()
	{
		config.setCategoryOverrides(categoryOverrides.serialize());
		BankGuideController controller = guideController;
		if (controller != null)
		{
			controller.publishCategoryOverrideCount(categoryOverrides.size());
		}
	}

	/** The player's assignment of categories to bank destinations. */
	private BankLayoutPlan activePlan()
	{
		return BankLayoutPlan.parse(BankPresets.IRONMAN, config.tabOrder());
	}

	/** The player's layout choices that no plan can state for them. */
	private BankLayoutOptions activeOptions()
	{
		// A layout choice exists only where packed and sorted genuinely differ:
		// where a category has curated geometry to keep or give up. Everywhere
		// else the two are a nudge apart, so no option is offered and packed
		// stands.
		Map<BankCategorySortMode, TabOrder> tabOrders = new EnumMap<>(BankCategorySortMode.class);
		tabOrders.put(BankCategorySortMode.MAIN, config.utilitiesLayout());
		tabOrders.put(BankCategorySortMode.TELEPORTS, config.utilitiesLayout());
		tabOrders.put(BankCategorySortMode.CURRENCY, config.utilitiesLayout());
		tabOrders.put(BankCategorySortMode.SUPPLIES, config.suppliesLayout());
		tabOrders.put(BankCategorySortMode.TOOLS, config.toolsLayout());
		tabOrders.put(BankCategorySortMode.RESOURCES, config.resourcesLayout());
		tabOrders.put(BankCategorySortMode.CLUES, config.cluesLayout());
		return new BankLayoutOptions(config.fillGearRows(), config.fillHerbloreRows(),
			config.alchPile(), config.gearOrder(), tabOrders,
			config.potionDoses(), config.runeOrder(), config.teleportOrder());
	}

	/** The layouts the player has saved or imported, and which one they loaded. */
	private BankLayoutProfiles savedProfiles()
	{
		return BankLayoutProfiles.parse(config.layoutProfiles(), config.activeLayoutProfile());
	}

	private void storeProfiles(BankLayoutProfiles profiles)
	{
		config.setLayoutProfiles(profiles.serialize());
		config.setActiveLayoutProfile(profiles.getActiveName());
	}

	/**
	 * The all-round preset. Its ten categories still own classification and
	 * layout; where their items end up is the plan's business, so the preset
	 * itself no longer changes when the player rearranges the bank.
	 */
	private BankPreset activePreset()
	{
		return BankPresets.IRONMAN;
	}

	/**
	 * Stores a plan the player arranged in the layout screen and re-plans the
	 * bank from it. Only the placement of the tags changes: classification and
	 * corrections are keyed by category, never by destination or position.
	 */
	private BankLayoutModel bankLayoutModel()
	{
		return new BankLayoutModel()
		{
			@Override
			public BankPreset preset()
			{
				return BankPresets.IRONMAN;
			}

			@Override
			public BankLayoutPlan plan()
			{
				return activePlan();
			}

			@Override
			public void save(BankLayoutPlan plan)
			{
				config.setTabOrder(plan.completedFor(BankPresets.IRONMAN).serialize());
				analyzeBank();
			}

			@Override
			public List<String> profileNames()
			{
				return savedProfiles().names();
			}

			/**
			 * Compared by the plan the layout produces, not by the text it was
			 * stored as, so a profile written by an older version still counts as
			 * a match once both sides mean the same arrangement.
			 */
			@Override
			public String matchingProfile()
			{
				BankLayoutProfiles profiles = savedProfiles();
				List<List<String>> current = activePlan().getDestinations();
				for (String name : profiles.names())
				{
					if (BankLayoutPlan.parse(BankPresets.IRONMAN, profiles.planFor(name))
						.getDestinations().equals(current))
					{
						return name;
					}
				}

				return "";
			}

			@Override
			public void selectProfile(String name)
			{
				BankLayoutProfiles profiles = savedProfiles().withActive(name);
				config.setActiveLayoutProfile(profiles.getActiveName());
				config.setTabOrder(BankLayoutPlan
					.parse(BankPresets.IRONMAN, profiles.activePlan())
					.serialize());
				analyzeBank();
			}

			@Override
			public void saveProfile(String name, BankLayoutPlan plan)
			{
				BankLayoutProfiles profiles = savedProfiles().withProfile(name,
					plan.completedFor(BankPresets.IRONMAN).serialize());
				storeProfiles(profiles);
				save(plan);
			}

			@Override
			public void deleteProfile(String name)
			{
				storeProfiles(savedProfiles().without(name));
			}

			@Override
			public BankLayoutOptions options()
			{
				return activeOptions();
			}

			@Override
			public void saveOptions(BankLayoutOptions options)
			{
				config.setFillGearRows(options.fillGearRows());
				config.setFillHerbloreRows(options.fillHerbloreRows());
				config.setAlchPile(options.alchPile());
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
		return Optional.of(new BankAnalysisRequest(bankSnapshot,
			collectGearStats(bankSnapshot), collectAlchValues(bankSnapshot),
			categoryOverrides.asMap(), activePlan(), activeOptions()));
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
		AsyncBufferedImage image = itemIcons.computeIfAbsent(cacheKey,
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

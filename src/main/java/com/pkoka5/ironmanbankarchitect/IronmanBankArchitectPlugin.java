package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.pkoka5.ironmanbankarchitect.bank.BankItemIds;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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
	description = "Creates an Ironman bank blueprint with read-only, manual move guidance.",
	tags = {"bank", "planner", "blueprint", "organization", "ironman"}
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

	private NavigationButton navigationButton;
	private IronmanBankArchitectPanel panel;
	private BankGuideController guideController;
	private BankGuideOverlay guideOverlay;
	private BankCategoryOverlay categoryOverlay;
	private ExecutorService analysisExecutor;
	private UserCategoryOverrides categoryOverrides = new UserCategoryOverrides();
	private final Map<String, AsyncBufferedImage> itemIcons = new ConcurrentHashMap<>();

	@Override
	protected void startUp()
	{
		analysisExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "ironman-bank-architect-analysis");
			thread.setDaemon(true);
			return thread;
		});
		guideController = new BankGuideController(AllRoundIronmanPreset.create());
		categoryOverrides = UserCategoryOverrides.parse(config.categoryOverrides());
		guideController.publishCategoryOverrideCount(categoryOverrides.size());
		// Both overlays want the free canvas beside the bank; the shared claim
		// keeps the guidance panel off the destination legend on a small window.
		BankOverlayReservations reservations = new BankOverlayReservations();
		guideOverlay = new BankGuideOverlay(this, client, guideController, config, reservations);
		overlayManager.add(guideOverlay);
		categoryOverlay = new BankCategoryOverlay(this, client, guideController, config,
			reservations);
		overlayManager.add(categoryOverlay);

		panel = new IronmanBankArchitectPanel(guideController, this::analyzeBank, this::renderItemIcon,
			this::resetCategoryOverrides);
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

		if (analysisExecutor != null)
		{
			analysisExecutor.shutdownNow();
			analysisExecutor = null;
		}

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

		Optional<String> current = categoryOverrides.categoryKeyFor(itemId);
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			boolean active = current.isPresent() && current.get().equals(category.getKey());
			submenu.createMenuEntry(-1)
				.setOption((active ? "* " : "") + category.getName())
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> applyCategoryOverride(itemId, itemName, category.getKey()));
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

	@Provides
	IronmanBankArchitectConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanBankArchitectConfig.class);
	}

	private void analyzeBank()
	{
		BankGuideController controller = guideController;
		ExecutorService executor = analysisExecutor;
		if (controller == null || executor == null || executor.isShutdown())
		{
			return;
		}

		controller.publishAnalysisStarted();
		clientThread.invoke(() -> {
			Optional<BankSnapshot> snapshot = BankSnapshotReader.readOpenBank(client);
			if (!snapshot.isPresent())
			{
				controller.publishBankClosedAnalysis();
				return;
			}

			BankSnapshot bankSnapshot = snapshot.get();
			// Item stats and prices can only be read on the client thread; collect
			// them here so the analysis thread works from plain maps.
			Map<Integer, GearStats> gearStatsById = collectGearStats(bankSnapshot);
			Map<Integer, Integer> alchValuesById = collectAlchValues(bankSnapshot);
			try
			{
				executor.execute(() -> publishBankAnalysis(controller, bankSnapshot, gearStatsById, alchValuesById));
			}
			catch (RejectedExecutionException ignored)
			{
				// Plugin is shutting down; ignore late analysis requests.
			}
		});
	}

	private void publishBankAnalysis(BankGuideController controller, BankSnapshot bankSnapshot,
		Map<Integer, GearStats> gearStatsById, Map<Integer, Integer> alchValuesById)
	{
		if (guideController != controller)
		{
			return;
		}

		try
		{
			controller.publishCatalogSummary(BankCatalogSummarizer.summarize(bankSnapshot,
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN));
			controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(bankSnapshot,
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN,
				itemId -> Optional.ofNullable(gearStatsById.get(itemId)),
				itemId -> alchValuesById.getOrDefault(itemId, 0),
				categoryOverrides));
		}
		catch (RuntimeException ex)
		{
			log.error("Bank analysis failed", ex);
		}
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

		int defenceSum = equipment.getDstab() + equipment.getDslash() + equipment.getDcrush()
			+ equipment.getDmagic() + equipment.getDrange();
		return Optional.of(new GearStats(slot, equipment.getAstab(), equipment.getAslash(), equipment.getAcrush(),
			equipment.getAmagic(), equipment.getArange(), equipment.getStr(), equipment.getRstr(),
			equipment.getPrayer(), defenceSum));
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
	private static BufferedImage createIcon()
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

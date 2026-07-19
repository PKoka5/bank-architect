package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import com.pkoka5.ironmanbankarchitect.overlay.BankGuideOverlay;
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
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;
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
	private ExecutorService analysisExecutor;
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
		guideOverlay = new BankGuideOverlay(this, client, guideController, config);
		overlayManager.add(guideOverlay);

		panel = new IronmanBankArchitectPanel(guideController, this::analyzeBank, this::renderItemIcon);
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
				itemId -> alchValuesById.getOrDefault(itemId, 0)));
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

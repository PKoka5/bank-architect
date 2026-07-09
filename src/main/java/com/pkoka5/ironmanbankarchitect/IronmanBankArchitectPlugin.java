package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.overlay.BankGuideOverlay;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.inject.Inject;
import javax.swing.JLabel;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;

@PluginDescriptor(
	name = "Ironman Bank Architect",
	description = "Standalone Ironman bank blueprint planner with manual organization guidance.",
	tags = {"ironman", "bank", "planner", "organisation", "organization"}
)
public final class IronmanBankArchitectPlugin extends Plugin
{
	static final String PLUGIN_NAME = "Ironman Bank Architect";

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
		guideOverlay = new BankGuideOverlay(this, client, guideController);
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
			try
			{
				executor.execute(() -> publishBankAnalysis(controller, bankSnapshot));
			}
			catch (RejectedExecutionException ignored)
			{
				// Plugin is shutting down; ignore late analysis requests.
			}
		});
	}

	private void publishBankAnalysis(BankGuideController controller, BankSnapshot bankSnapshot)
	{
		if (guideController != controller)
		{
			return;
		}

		controller.publishCatalogSummary(BankCatalogSummarizer.summarize(bankSnapshot,
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN));
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(bankSnapshot,
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN));
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

	private static BufferedImage createIcon()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(38, 83, 64));
		graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
		graphics.setColor(new Color(214, 186, 101));
		graphics.fillRect(4, 4, 8, 2);
		graphics.fillRect(4, 7, 8, 2);
		graphics.fillRect(4, 10, 8, 2);
		graphics.dispose();
		return icon;
	}
}

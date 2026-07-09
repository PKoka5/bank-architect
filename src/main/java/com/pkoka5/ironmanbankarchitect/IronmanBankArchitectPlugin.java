package com.pkoka5.ironmanbankarchitect;

import com.google.inject.Provides;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.overlay.BankGuideOverlay;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

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

	private NavigationButton navigationButton;
	private IronmanBankArchitectPanel panel;
	private BankGuideController guideController;
	private BankGuideOverlay guideOverlay;

	@Override
	protected void startUp()
	{
		guideController = new BankGuideController(AllRoundIronmanPreset.create());
		guideOverlay = new BankGuideOverlay(this, client, guideController);
		overlayManager.add(guideOverlay);

		panel = new IronmanBankArchitectPanel(guideController, this::analyzeBank);
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

		guideController = null;
		panel = null;
	}

	@Provides
	IronmanBankArchitectConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanBankArchitectConfig.class);
	}

	private void analyzeBank()
	{
		clientThread.invoke(() -> {
			Optional<BankSnapshot> snapshot = BankSnapshotReader.readOpenBank(client);
			if (!snapshot.isPresent())
			{
				guideController.publishBankClosedAnalysis();
				return;
			}

			BankSnapshot bankSnapshot = snapshot.get();
			guideController.publishCatalogSummary(BankCatalogSummarizer.summarize(bankSnapshot,
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN));
		});
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

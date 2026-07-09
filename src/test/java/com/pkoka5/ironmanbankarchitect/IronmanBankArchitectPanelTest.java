package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Component;
import java.awt.Container;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.junit.Test;

public class IronmanBankArchitectPanelTest
{
	@Test
	public void analyzeButtonRunsCallback()
	{
		AtomicInteger calls = new AtomicInteger();
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()),
			calls::incrementAndGet
		);

		panel.getAnalyzeButton().doClick();

		assertEquals(1, calls.get());
		panel.shutdown();
	}

	@Test
	public void panelRendersCompactBankScanOverviewFromCatalogSummary()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		BankCatalogSummary summary = BankCatalogSummarizer.summarize(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		)), StaticItemCatalog.INSTANCE);
		controller.publishCatalogSummary(summary);
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		String labelText = panel.getCatalogSummaryLabel().getText();
		assertTrue(labelText.contains("Bank Scan Overview"));
		assertTrue(labelText.contains("Recognized item IDs: 2"));
		assertTrue(labelText.contains("Unrecognized IDs: 1"));
		assertFalse(labelText.contains("Categories:"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("Suggested Bank Blueprint"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("Irit seed"));

		panel.shutdown();
	}

	@Test
	public void panelFramesWholeBankOrganizationAsPrimaryFlow()
	{
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()),
			() -> {});

		String panelText = String.join("\n", collectLabelText(panel));

		assertTrue(panelText.contains("Profile"));
		assertTrue(panelText.contains(AllRoundIronmanPreset.PROFILE_NAME));
		assertTrue(panelText.contains("Read-only whole-bank organization planner"));
		assertTrue(panelText.contains("Main action"));
		assertTrue(panelText.contains("Analyze My Bank"));
		assertTrue(panelText.contains("Whole Bank Scan"));
		assertTrue(panelText.contains("Suggested Bank Blueprint"));
		assertTrue(panelText.contains("Analyze your bank to preview owned-item blueprint."));
		assertTrue(panelText.contains("Bank guide preview is temporary"));
		assertTrue(panelText.contains("Overlay preview is neutral"));
		assertFalse(panelText.contains("Advanced preview block"));
		assertFalse(panelText.contains("Owned:"));
		assertFalse(panelText.contains("Missing:"));

		panel.shutdown();
	}

	@Test
	public void selectedBlockMatchDetailsDoNotRenderInMainOrganizerSidebar()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		controller.selectBlock("irit-super-attack");
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));
		controller.publishCatalogSummary(BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE));
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(snapshot, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		String panelText = String.join("\n", collectLabelText(panel));
		assertTrue(panel.getCatalogSummaryLabel().getText().contains("Bank Scan Overview"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("Irit seed"));
		assertFalse(panelText.contains("Owned:"));
		assertFalse(panelText.contains("Missing:"));

		panel.shutdown();
	}

	@Test
	public void panelSourceHasNoRuneLiteClientApiImports() throws Exception
	{
		String source = new String(Files.readAllBytes(Paths.get("src/main/java/com/pkoka5/ironmanbankarchitect/IronmanBankArchitectPanel.java")), StandardCharsets.UTF_8);

		assertFalse(source.contains("net.runelite.api.Client"));
		assertFalse(source.contains("net.runelite.api.widgets.Widget"));
		assertFalse(source.contains("InventoryID"));
		assertFalse(source.contains("ItemContainer"));
		assertFalse(source.contains("ItemManager"));
		assertFalse(source.contains("net.runelite.api."));
	}

	private static List<String> collectLabelText(Container container)
	{
		List<String> texts = new ArrayList<>();
		for (Component component : container.getComponents())
		{
			if (component instanceof JLabel)
			{
				texts.add(((JLabel) component).getText());
			}
			if (component instanceof AbstractButton)
			{
				texts.add(((AbstractButton) component).getText());
			}
			if (component instanceof Container)
			{
				texts.addAll(collectLabelText((Container) component));
			}
		}

		return texts;
	}
}

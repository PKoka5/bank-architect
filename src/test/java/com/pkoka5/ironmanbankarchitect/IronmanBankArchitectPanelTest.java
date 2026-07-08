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
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
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

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		String labelText = panel.getCatalogSummaryLabel().getText();
		assertTrue(labelText.contains("Bank Scan Overview"));
		assertTrue(labelText.contains("Known catalog IDs: 2"));
		assertTrue(labelText.contains("Unknown IDs: 1"));
		assertTrue(labelText.contains("Farming: 1"));
		assertTrue(labelText.contains("Herblore: 1"));
		assertTrue(labelText.contains("Unknown: 1"));

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

}

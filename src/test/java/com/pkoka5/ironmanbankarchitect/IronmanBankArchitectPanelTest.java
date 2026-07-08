package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

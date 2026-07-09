package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import java.util.Arrays;
import org.junit.Test;

public class BankOrganizationPreviewBuilderTest
{
	@Test
	public void previewKeepsPresetOrderAndOwnedItemSamples()
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 3, 0),
			new BankItemSnapshot(209, 2, 1),
			new BankItemSnapshot(145, 1, 2),
			new BankItemSnapshot(999999, 1, 3)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		assertEquals(BankPresets.IRONMAN, preview.getPreset());
		assertEquals(10, preview.getCategories().size());
		assertEquals("currency-utilities", preview.getCategories().get(0).getCategory().getKey());
		assertEquals("teleports-runes", preview.getCategories().get(1).getCategory().getKey());
		assertEquals("farming-herblore", preview.getCategories().get(4).getCategory().getKey());
		assertEquals(0, preview.getCategories().get(0).getItemCount());
		assertEquals(1, preview.getCategories().get(3).getItemCount());
		assertEquals(2, preview.getCategories().get(4).getItemCount());
		assertEquals(1, preview.getCategories().get(9).getItemCount());
		assertEquals("Super attack (3)", preview.getCategories().get(3).getSampleItems().get(0));
		assertEquals("Irit seed x3", preview.getCategories().get(4).getSampleItems().get(0));
		assertEquals("Grimy irit x2", preview.getCategories().get(4).getSampleItems().get(1));
		assertEquals("Unknown item #999999", preview.getCategories().get(9).getSampleItems().get(0));
	}

	@Test
	public void previewTextRendersReadableTabPlan()
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(145, 1, 1)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		String text = preview.toPreviewText();

		assertTrue(text.contains("Suggested Bank Blueprint"));
		assertTrue(text.contains("Ironman - All-Round Bank"));
		assertTrue(text.contains("1. Currency & Account Utilities: 0"));
		assertTrue(text.contains("Irit seed"));
		assertTrue(text.contains("4. Potions, Food & PvM Supplies: 1"));
		assertTrue(text.contains("Super attack (3)"));
	}
}

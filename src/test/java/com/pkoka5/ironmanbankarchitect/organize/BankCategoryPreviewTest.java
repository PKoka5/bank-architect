package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class BankCategoryPreviewTest
{
	@Test
	public void itemCountAndSamplesIgnorePlannedBlankSlots()
	{
		BankCategoryPreview preview = new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0),
			Arrays.asList(
				BankPreviewItem.blank(),
				new BankPreviewItem(1, "Coins", 100),
				BankPreviewItem.blank(),
				new BankPreviewItem(2, "Rope", 1)
			));

		assertEquals(2, preview.getItemCount());
		assertEquals(4, preview.getItems().size());
		assertEquals(Arrays.asList("Coins x100", "Rope"), preview.getSampleItems());
	}

	@Test
	public void blankPreviewItemHasNoRealItemId()
	{
		assertTrue(BankPreviewItem.blank().isBlank());
		assertEquals(-1, BankPreviewItem.blank().getItemId());
		assertEquals(false, new BankPreviewItem(1, "Coins", 1).isBlank());
	}
}

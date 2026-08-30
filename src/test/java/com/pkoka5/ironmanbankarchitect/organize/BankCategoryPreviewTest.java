package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
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

	@Test
	public void expandsMixedPlaceholderAndOwnedOccurrencesWithoutLosingEither()
	{
		CatalogItem item = new CatalogItem(6687, "Saradomin brew(3)", ItemCategory.POTION,
			"potion", Collections.emptySet(), null);
		BankPreviewItem aggregate = new BankPreviewItem(item, 3, false, Arrays.asList(0, 3));

		BankCategoryPreview preview = new BankCategoryPreview(
			BankPresets.IRONMAN.getCategory("potions-food"), Collections.singletonList(aggregate));

		assertEquals(2, preview.getItems().size());
		assertTrue(preview.getItems().get(0).isPlaceholder());
		assertEquals(0, preview.getItems().get(0).getQuantity());
		assertEquals(false, preview.getItems().get(1).isPlaceholder());
		assertEquals(3, preview.getItems().get(1).getQuantity());
	}
}

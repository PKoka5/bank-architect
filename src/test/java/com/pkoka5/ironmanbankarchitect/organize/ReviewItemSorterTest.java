package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Collections;
import org.junit.Test;

public class ReviewItemSorterTest
{
	@Test
	public void labelsReviewItemsByHumanLane()
	{
		assertEquals("External Storage", ReviewItemSorter.label(item(1, "Costume room cape", ItemCategory.CLEANUP)));
		assertEquals("Clue & STASH", ReviewItemSorter.label(item(2, "Clue scroll (hard)", ItemCategory.CLEANUP)));
		assertEquals("Cosmetics & Collection", ReviewItemSorter.label(item(3, "Prospector jacket", ItemCategory.CLEANUP)));
		assertEquals("Quest Leftovers", ReviewItemSorter.label(item(4, "Instruction manual", ItemCategory.CLEANUP)));
		assertEquals("Holiday & Event", ReviewItemSorter.label(item(5, "Christmas cracker", ItemCategory.CLEANUP)));
		assertEquals("Burnt & Junk", ReviewItemSorter.label(item(6, "Burnt shark", ItemCategory.CLEANUP)));
		assertEquals("Redundant Gear Review", ReviewItemSorter.label(item(7, "Rune platebody", ItemCategory.CLEANUP)));
		assertEquals("Unknown Safe Review", ReviewItemSorter.label(new BankPreviewItem(CatalogItem.unknown(999999), 1)));
	}

	@Test
	public void ranksUnknownAfterRecognizedReviewItems()
	{
		assertEquals(10, ReviewItemSorter.rank(item(1, "Reward casket", ItemCategory.CLEANUP)));
		assertEquals(90, ReviewItemSorter.rank(new BankPreviewItem(CatalogItem.unknown(999999), 1)));
	}

	private static BankPreviewItem item(int itemId, String name, ItemCategory category)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}
}

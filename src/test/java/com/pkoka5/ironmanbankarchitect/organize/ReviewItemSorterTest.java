package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Collections;
import org.junit.Test;

public class ReviewItemSorterTest
{
	@Test
	public void ranksEachRecognizedReviewLane()
	{
		assertEquals(0, ReviewItemSorter.rank(item(1, "Costume room cape", ItemCategory.CLEANUP)));
		assertEquals(10, ReviewItemSorter.rank(item(2, "Clue scroll (hard)", ItemCategory.CLEANUP)));
		assertEquals(20, ReviewItemSorter.rank(item(3, "Prospector jacket", ItemCategory.CLEANUP)));
		assertEquals(30, ReviewItemSorter.rank(item(4, "Instruction manual", ItemCategory.CLEANUP)));
		assertEquals(40, ReviewItemSorter.rank(item(5, "Christmas cracker", ItemCategory.CLEANUP)));
		assertEquals(50, ReviewItemSorter.rank(item(6, "Burnt shark", ItemCategory.CLEANUP)));
		assertEquals(60, ReviewItemSorter.rank(item(7, "Rune platebody", ItemCategory.CLEANUP)));
		assertEquals(90, ReviewItemSorter.rank(new BankPreviewItem(CatalogItem.unknown(999999), 1)));
	}

	@Test
	public void ranksUnknownAfterRecognizedReviewItems()
	{
		assertEquals(10, ReviewItemSorter.rank(item(1, "Reward casket", ItemCategory.CLEANUP)));
		assertEquals(90, ReviewItemSorter.rank(new BankPreviewItem(CatalogItem.unknown(999999), 1)));
	}

	@Test
	public void keyMatchingDoesNotMistakeMonkeyNamesForKeys()
	{
		assertEquals(30, ReviewItemSorter.rank(item(1, "Fetid key", ItemCategory.CLEANUP)));
		assertEquals(70, ReviewItemSorter.rank(item(2, "Monkey nuts", ItemCategory.CLEANUP)));
		assertEquals(30, ReviewItemSorter.rank(item(3, "Karamjan monkey greegree", ItemCategory.CLEANUP)));
	}

	@Test
	public void ambiguousItemsStayGeneralInsteadOfBeingCalledJunk()
	{
		BankPreviewItem item = item(1, "Mysterious reusable object", ItemCategory.CLEANUP);

		assertEquals(70, ReviewItemSorter.rank(item));
	}

	private static BankPreviewItem item(int itemId, String name, ItemCategory category)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}
}

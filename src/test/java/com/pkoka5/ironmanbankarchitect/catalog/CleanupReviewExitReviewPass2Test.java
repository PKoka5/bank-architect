package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class CleanupReviewExitReviewPass2Test
{
	@Test
	public void routesCompleteResolvedFamiliesToMaintainerDestinations()
	{
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			1555, 1556, 1557, 1558, 1559, 1560,
			1561, 1562, 1563, 1564, 1565, 1566,
			1567, 1568, 1569, 1570, 1571, 1572,
			6549, 6550, 6551, 6552, 6553, 6554,
			6555, 6556, 6557, 6558, 6559, 6560,
			7581, 7582, 7583, 7584, 7585);
		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot", 6199);
	}

	@Test
	public void legacyCluesExcludedSiblingsAndUnknownsRemainFailClosed()
	{
		for (int itemId : new int[] {
			0, -1, 2_000_000_000,
			3557, 3583, 7246, 7318, 12551, 19765, 19861, 19897,
			1491, // Quest-specific witch's cat
			1554, // Quest-specific Fluffs' kitten
			18086, // PLACEHOLDER mystery-box declaration
			24965 // Internal kitten dummy
		})
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			assertEquals(item.getDisplayName(), "storage-cleanup",
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	private static void assertFamily(ItemCategory expectedCategory, String expectedTab,
		int... itemIds)
	{
		for (int itemId : itemIds)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId).get();
			assertEquals(item.getDisplayName(), expectedCategory, item.getCategory());
			assertEquals(item.getDisplayName(), expectedTab,
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}
}

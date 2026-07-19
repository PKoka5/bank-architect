package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class RealBankReviewCurationTest
{
	@Test
	public void routesCompleteQuestUtilityFamiliesToSkillingTools()
	{
		assertFamily(ItemCategory.TOOL, "skilling-tools", 4021, 4022); // M'speak amulets
		assertFamily(ItemCategory.TOOL, "skilling-tools", 9083); // Seal of passage
		assertFamily(ItemCategory.TOOL, "skilling-tools", 1506); // Gas mask
	}

	@Test
	public void routesAllFourCompleteSpiceDoseFamiliesToResources()
	{
		assertFamily(ItemCategory.SKILLING, "resources", 7480, 7481, 7482, 7483); // Red
		assertFamily(ItemCategory.SKILLING, "resources", 7484, 7485, 7486, 7487); // Orange
		assertFamily(ItemCategory.SKILLING, "resources", 7488, 7489, 7490, 7491); // Brown
		assertFamily(ItemCategory.SKILLING, "resources", 7492, 7493, 7494, 7495); // Yellow
	}

	@Test
	public void unknownAndExcludedControlsRemainFailClosed()
	{
		for (int itemId : new int[] {
			0, -1, 2_000_000_000,
			16701, // PLACEHOLDER Seal of passage
			17365, 17366, 17367, 17368 // PLACEHOLDER spice records
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

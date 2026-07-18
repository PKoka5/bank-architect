package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class CleanupReviewCurationRound2Test
{
	@Test
	public void routesCompleteBenchmarkRoundTwoFamiliesToFunctionalIronmanTabs()
	{
		assertFamily(ItemCategory.TOOL, "skilling-tools", 33, 36); // Standard candles
		assertFamily(ItemCategory.TOOL, "skilling-tools", 4527, 4529, 4531, 4532, 4534);
		assertFamily(ItemCategory.TOOL, "skilling-tools", 2025); // Cocktail shaker
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 2574); // Sextant
		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot", 989, 23951); // Crystal keys

		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot",
			3450, 3451, 3452, 3453, 3454, 3455, 3456, 3457, 3458, 3459,
			3460, 3461, 3462, 3463, 3464, 3465, 3466, 3467, 3468, 3469,
			25424, 25426, 25428, 25430, 25432); // Shade keys

		assertFamily(ItemCategory.SKILLING, "resources", 4542, 4544);
		assertFamily(ItemCategory.TOOL, "skilling-tools", 4546, 4548, 4550); // Bullseye lantern

		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10400, 10402, 10404, 10406, 10408, 10410, 10412, 10414,
			10416, 10418, 10420, 10422, 10424, 10426, 10428, 10430,
			10432, 10434, 10436, 10438, 12315, 12317, 12339, 12341,
			12343, 12345, 12347, 12349); // Elegant outfits
		assertFamily(ItemCategory.GEAR, "combat-gear", 9666, 9668, 9670); // Prayer harness packs
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 11891, 11892); // God banners
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			13149, 13151, 13153, 13155, 13157, 13159); // God-book page sets

		assertFamily(ItemCategory.SKILLING, "resources",
			21562, 21564, 21566, 21568, 21570, 21572, 21574, 21576, 21578,
			21580, 21582, 21584, 21586, 21588, 21600, 21602, 21604, 21606,
			21608, 21610, 21612, 21614, 21616, 21618, 21620); // Fossilised remains
		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot", 21820); // Revenant ether
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 19939, 23183); // Strange devices
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			22494, 22496, 22498, 22500, 22502); // Sinhaza shrouds

		assertFamily(ItemCategory.GEAR, "combat-gear",
			22552, 22555, 27662, 27665, 27676, 27679, 27785, 27788); // Wilderness sceptres
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			23911, 23913, 23915, 23917, 23919, 23921, 23923, 23925); // Crystal crowns
		assertFamily(ItemCategory.GEAR, "combat-gear",
			28238, 28240, 28242, 28244, 33504, 33508, 33512, 33516,
			33827); // Repairable broken ancient sceptres
		assertFamily(ItemCategory.GEAR, "combat-gear",
			28826, 28828, 28947, 28949, 28951, 28953, 28955, 28957,
			33524, 33526, 33528, 33530); // Dizana's quivers
		assertFamily(ItemCategory.GEAR, "combat-gear", 29084); // Sulphur blades

		assertFamily(ItemCategory.SKILLING, "resources", 29224, 29227, 29230); // Butterfly wings
		assertFamily(ItemCategory.CURRENCY, "currency-utilities", 29482); // Brimhaven voucher
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 20020, 20023, 20026); // Demon masks
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			25712, 25714, 25715, 25716, 25717, 25718, 25719, 25720); // Clan cloaks
	}

	@Test
	public void unknownAndExcludedControlsRemainInCleanup()
	{
		for (int itemId : new int[] {
			0, -1, 2_000_000_000,
			34, // CERT lit candle
			4530, // CERT candle lantern
			14424, // PLACEHOLDER god-book page
			22197, // PLACEHOLDER bird house
			28948 // PLACEHOLDER Dizana's quiver
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

package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class CleanupReviewCurationRound3Test
{
	@Test
	public void routesCompleteBenchmarkRoundThreeFamiliesToFunctionalIronmanTabs()
	{
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10374, 10382, 10390, 12496, 12504, 12512); // God coifs
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10440, 10442, 10444, 12199, 12263, 12275); // God croziers
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10446, 10448, 10450, 12197, 12261, 12273); // God cloaks
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10452, 10454, 10456, 12203, 12259, 12271); // God mitres
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10458, 10460, 10462, 12193, 12253, 12265); // God robe tops
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10464, 10466, 10468, 12195, 12255, 12267); // God robe bottoms
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10470, 10472, 10474, 12201, 12257, 12269); // God stoles

		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			12373, 12375, 12377, 12379); // Metal canes
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			2645, 2647, 2649, 12299, 12301, 12303, 12305, 12307); // Headbands
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			7319, 7321, 7323, 7325, 7327, 12309, 12311, 12313); // Boaters
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			2639, 2641, 2643, 12321, 12323, 12325); // Cavaliers
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			2633, 2635, 2637, 12247); // Berets
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10392, 10394, 10396, 10398, 12430); // Novelty clue clothing

		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			2978, 2979, 2980, 2981, 2982, 2983, 2984, 2985, 2986,
			2987, 2988, 2989, 2990, 2991, 2992, 2993, 2994, 2995); // Chompy hats
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			3759, 3761, 3763, 3765, 3777, 3779, 3781, 3783, 3785,
			3787, 3789); // Fremennik cloaks
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			6798, 6799, 6800, 6801, 6802, 6803, 6804, 6805, 6806,
			6807, 6808); // Champion scrolls

		assertFamily(ItemCategory.SKILLING, "resources",
			7779, 7780, 7781, 7782, 7783, 7784, 7785, 7786, 7787,
			7788, 7789, 7790, 7791, 7792, 7793, 7794, 7795, 7796,
			7797, 7798, 7799); // Temple Trekking tomes
		assertFamily(ItemCategory.GEAR, "combat-gear",
			10035, 10037, 10039, 10041, 10043, 10045, 10047, 10049,
			10051); // Hunter-fur outfits
		assertFamily(ItemCategory.TOOL, "skilling-tools",
			10053, 10055, 10057, 10059, 10061, 10063, 10065, 10067); // Camo outfits
		assertFamily(ItemCategory.GEAR, "combat-gear", 12596, 23249); // Ranger garments
		assertFamily(ItemCategory.SKILLING, "resources", 12640, 12641); // Amylase

		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot", 27681, 27684, 27687);
		assertFamily(ItemCategory.GEAR, "combat-gear", 27690); // Voidwaker

		assertFamily(ItemCategory.SKILLING, "resources", 7636); // Rod dust
		assertFamily(ItemCategory.GEAR, "combat-gear",
			7637, 7638, 7639, 7640, 7641, 7642, 7643, 7644, 7645,
			7646, 7647, 7648); // Rod of Ivandis states
		assertFamily(ItemCategory.TOOL, "skilling-tools", 7649); // Rod mould
	}

	@Test
	public void unknownAndExcludedControlsRemainInCleanup()
	{
		for (int itemId : new int[] {
			0, -1, 2_000_000_000,
			34, // CERT lit candle
			4530, // CERT candle lantern
			17957, // PLACEHOLDER Chompy bird hat
			7621 // Internal blank Rod of Ivandis control record
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

package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class CleanupReviewCurationTest
{
	@Test
	public void routesCompleteSimulatorReviewFamiliesToFunctionalIronmanTabs()
	{
		assertFamily(ItemCategory.GEAR, "combat-gear", 10041, 10043, 10045); // Larupia outfit
		assertFamily(ItemCategory.TOOL, "skilling-tools", 10595, 10596); // Clockwork suits
		assertFamily(ItemCategory.FARMING, "seeds-farming", 5350); // Empty plant pot stage
		assertFamily(ItemCategory.FARMING, "seeds-farming", 6311); // Gout tuber
		assertFamily(ItemCategory.TOOL, "skilling-tools",
			11095, 11097, 11099, 11101, 11103); // Abyssal bracelet charges

		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			1038, 1040, 1042, 1044, 1046, 1048, 11862, 11863, 27828); // Partyhats
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			12363, 12365, 12367, 12369, 12371, 12518, 12520, 12522, 12524,
			23270, 23273); // Dragon masks
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 12249); // Imp mask
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 21211); // 4th birthday hat
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", 27820); // 10th birthday balloons

		assertFamily(ItemCategory.SKILLING, "resources", 1794); // Bronze wire
		assertFamily(ItemCategory.SKILLING, "resources", 1933); // Pot of flour
		assertFamily(ItemCategory.SKILLING, "resources",
			21512, 21515, 21518, 21521, 22192, 22195, 22198, 22201, 22204); // Bird houses
		assertFamily(ItemCategory.GEAR, "combat-gear", 11908, 22290, 33328, 33434); // Tridents

		assertGodBookFamily(3840, 3827, 3828, 3829, 3830); // Saradomin
		assertGodBookFamily(3842, 3831, 3832, 3833, 3834); // Zamorak
		assertGodBookFamily(3844, 3835, 3836, 3837, 3838); // Guthix
		assertGodBookFamily(12608, 12613, 12614, 12615, 12616); // Bandos
		assertGodBookFamily(12610, 12617, 12618, 12619, 12620); // Armadyl
		assertGodBookFamily(12612, 12621, 12622, 12623, 12624); // Ancient

		assertFamily(ItemCategory.GEAR, "combat-gear", 12863); // Dwarf cannon set
		assertFamily(ItemCategory.GEAR, "combat-gear", 19478, 19481, 26712); // Ballista weapons
		assertFamily(ItemCategory.SKILLING, "resources",
			19586, 19589, 19592, 19595, 19598, 19604, 19607); // Ballista assembly
		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot",
			21034, 21047, 21079, 30626, 30627); // Prayer scrolls
		assertFamily(ItemCategory.UNIQUE, "slayer-boss-loot", 21804); // Ancient crystal
	}

	@Test
	public void unknownControlsStillFailClosedIntoCleanup()
	{
		for (int itemId : new int[] {0, -1, 2_000_000_000})
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			assertEquals(ItemCategory.UNKNOWN, item.getCategory());
			assertEquals("storage-cleanup",
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	private static void assertGodBookFamily(int completedBookId, int... pageIds)
	{
		assertFamily(ItemCategory.GEAR, "combat-gear", completedBookId);
		assertFamily(ItemCategory.CLUE, "clues-cosmetics", pageIds);
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

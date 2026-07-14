package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class PresetItemSorterTest
{
	@Test
	public void combatGearSortsWearRowsBeforeWeaponsAndAmmo()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"), Arrays.asList(
			item(5, "Rune arrow", ItemCategory.GEAR),
			item(1, "Dragon scimitar", ItemCategory.GEAR),
			item(2, "Neitiznot helm", ItemCategory.GEAR),
			item(3, "Mystic robe bottom", ItemCategory.GEAR),
			item(4, "Rune boots", ItemCategory.GEAR)
		));

		assertEquals(Arrays.asList("Neitiznot helm", "Rune boots", "Dragon scimitar", "Mystic robe bottom", "Rune arrow"),
			names(sorted));
	}

	@Test
	public void potionsFoodSortsPotionsBeforeFood()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("potions-food"), Arrays.asList(
			item(1, "Shark", ItemCategory.POTION),
			item(2, "Prayer potion(4)", ItemCategory.POTION),
			item(3, "Karambwan", ItemCategory.POTION)
		));

		assertEquals(Arrays.asList("Prayer potion(4)", "Karambwan", "Shark"), names(sorted));
	}

	@Test
	public void ironmanSupplyPresetUsesIdBasedDirectHealingOrder()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("potions-food"), Arrays.asList(
				item(379, "Lobster", ItemCategory.POTION),
				item(3144, "Cooked karambwan", ItemCategory.POTION),
				item(385, "Shark", ItemCategory.POTION)));

		assertEquals(Arrays.asList("Shark", "Cooked karambwan", "Lobster"), names(sorted));
	}

	@Test
	public void ironmanSupplyPresetUsesIdBasedPotionFamiliesAndDoses()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("potions-food"), Arrays.asList(
				item(143, "Prayer potion(1)", ItemCategory.POTION),
				item(121, "Attack potion(3)", ItemCategory.POTION),
				item(2434, "Prayer potion(4)", ItemCategory.POTION),
				item(2428, "Attack potion(4)", ItemCategory.POTION)));

		assertEquals(Arrays.asList("Attack potion(4)", "Attack potion(3)",
			"Prayer potion(4)", "Prayer potion(1)"), names(sorted));
	}

	@Test
	public void pvpSupplyCategoryReusesDoseAwareSupplySorting()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.PVP.getCategory("food-potions"), Arrays.asList(
				item(385, "Shark", ItemCategory.POTION),
				item(143, "Prayer potion(1)", ItemCategory.POTION),
				item(2434, "Prayer potion(4)", ItemCategory.POTION)));

		assertEquals(Arrays.asList("Prayer potion(4)", "Prayer potion(1)", "Shark"), names(sorted));
	}

	@Test
	public void mainReviewCategoryReusesSafeUnknownLastSorting()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.MAIN.getCategory("junk-review"), Arrays.asList(
				new BankPreviewItem(CatalogItem.unknown(999999), 1),
				item(1, "Burnt shark", ItemCategory.CLEANUP)));

		assertEquals(Arrays.asList("Burnt shark", "Unknown item #999999"), names(sorted));
	}

	@Test
	public void farmingHerbloreSortsSeedsHerbsSecondariesUnfinished()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("herblore"), Arrays.asList(
			item(1, "Eye of newt", ItemCategory.HERBLORE),
			item(2, "Irit potion (unf)", ItemCategory.HERBLORE),
			item(3, "Grimy irit", ItemCategory.HERBLORE),
			item(4, "Irit seed", ItemCategory.FARMING)
		));

		// Recipe row order: grimy herb, seed, unfinished potion, secondary.
		assertEquals(Arrays.asList("Grimy irit", "Irit seed", "Irit potion (unf)", "Eye of newt"), names(sorted));
	}

	@Test
	public void resourcesSortByMaterialFlow()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("resources"), Arrays.asList(
			item(1, "Oak plank", ItemCategory.SKILLING),
			item(2, "Iron ore", ItemCategory.SKILLING),
			item(3, "Iron bar", ItemCategory.SKILLING),
			item(4, "Dragon bones", ItemCategory.SKILLING)
		));

		assertEquals(Arrays.asList("Iron ore", "Iron bar", "Oak plank", "Dragon bones"), names(sorted));
	}

	@Test
	public void cleanupSortsUnknownReviewAfterKnownCleanup()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("storage-cleanup"), Arrays.asList(
			new BankPreviewItem(CatalogItem.unknown(999999), 1),
			item(1, "Clue scroll", ItemCategory.CLEANUP),
			item(2, "Quest key", ItemCategory.CLEANUP),
			item(3, "Burnt shark", ItemCategory.CLEANUP),
			item(4, "Graceful top", ItemCategory.CLEANUP)
		));

		assertEquals(Arrays.asList("Clue scroll", "Graceful top", "Quest key", "Burnt shark",
			"Unknown item #999999"), names(sorted));
	}

	@Test
	public void cleanupItemsExposeHumanReviewLaneLabels()
	{
		BankCategory cleanup = BankPresets.IRONMAN.getCategory("storage-cleanup");

		assertEquals("Clue & STASH", PresetItemSorter.subgroupLabel(cleanup,
			item(1, "Reward casket", ItemCategory.CLEANUP)));
		assertEquals("Cosmetics & Collection", PresetItemSorter.subgroupLabel(cleanup,
			item(2, "Graceful hood", ItemCategory.CLEANUP)));
		assertEquals("Quest Leftovers", PresetItemSorter.subgroupLabel(cleanup,
			item(3, "Old notes", ItemCategory.CLEANUP)));
		assertEquals("Burnt & Junk", PresetItemSorter.subgroupLabel(cleanup,
			item(4, "Burnt lobster", ItemCategory.CLEANUP)));
		assertEquals("Unknown Safe Review", PresetItemSorter.subgroupLabel(cleanup,
			new BankPreviewItem(CatalogItem.unknown(999999), 1)));
	}

	@Test
	public void cluesFollowDifficultyOrderInsteadOfAlphabeticalOrder()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("clues-cosmetics"), Arrays.asList(
				item(1, "Clue scroll (elite)", ItemCategory.CLUE),
				item(2, "Clue scroll (easy)", ItemCategory.CLUE),
				item(3, "Clue scroll (hard)", ItemCategory.CLUE),
				item(4, "Clue scroll (medium)", ItemCategory.CLUE),
				item(5, "Clue scroll (beginner)", ItemCategory.CLUE)));

		assertEquals(Arrays.asList("Clue scroll (beginner)", "Clue scroll (easy)",
			"Clue scroll (medium)", "Clue scroll (hard)", "Clue scroll (elite)"), names(sorted));
	}

	@Test
	public void activityRewardsStayTogetherAfterTreasureTrailItems()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("clues-cosmetics"), Arrays.asList(
				item(6183, "Frog token", ItemCategory.CURRENCY),
				item(1, "Clue scroll (hard)", ItemCategory.CLUE),
				item(2, "Reward casket (hard)", ItemCategory.CLUE),
				item(6529, "Tokkul", ItemCategory.CURRENCY),
				item(6306, "Trading sticks", ItemCategory.CURRENCY),
				item(12012, "Golden nugget", ItemCategory.CURRENCY),
				item(25527, "Stardust", ItemCategory.CURRENCY),
				item(21555, "Numulite", ItemCategory.CURRENCY),
				item(3, "Bob shirt", ItemCategory.CLUE)));

		assertEquals(Arrays.asList("Clue scroll (hard)", "Reward casket (hard)",
			"Frog token", "Golden nugget", "Numulite", "Stardust", "Tokkul", "Trading sticks",
			"Bob shirt"), names(sorted));
	}

	@Test
	public void ironmanMainSortsChargedJewelleryDownWithinAlphabeticalFamilies()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("currency-utilities"), Arrays.asList(
				item(3867, "Games necklace(1)", ItemCategory.TELEPORT),
				item(21175, "Burning amulet(1)", ItemCategory.TELEPORT),
				item(2558, "Ring of dueling(5)", ItemCategory.TELEPORT),
				item(3853, "Games necklace(8)", ItemCategory.TELEPORT),
				item(21166, "Burning amulet(5)", ItemCategory.TELEPORT),
				item(2566, "Ring of dueling(1)", ItemCategory.TELEPORT),
				item(21171, "Burning amulet(3)", ItemCategory.TELEPORT),
				item(3859, "Games necklace(5)", ItemCategory.TELEPORT),
				item(2552, "Ring of dueling(8)", ItemCategory.TELEPORT)));

		assertEquals(Arrays.asList("Burning amulet(5)", "Burning amulet(3)", "Burning amulet(1)",
			"Games necklace(8)", "Games necklace(5)", "Games necklace(1)",
			"Ring of dueling(8)", "Ring of dueling(5)", "Ring of dueling(1)"), names(sorted));
	}

	@Test
	public void bossLootKeepsUniqueUpgradesSeparateFromAlchCandidates()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("slayer-boss-loot"), Arrays.asList(
				item(1, "Adamant platebody", ItemCategory.GEAR),
				item(2, "Pegasian crystal", ItemCategory.UNIQUE),
				item(3, "Crystal weapon seed", ItemCategory.UNIQUE),
				item(4, "Enhanced crystal weapon seed", ItemCategory.UNIQUE),
				item(5, "Soaked page", ItemCategory.UNIQUE),
				item(6, "Burnt page", ItemCategory.UNIQUE)));

		assertEquals(Arrays.asList("Crystal weapon seed", "Enhanced crystal weapon seed",
			"Pegasian crystal", "Burnt page", "Soaked page", "Adamant platebody"), names(sorted));
	}

	private static BankPreviewItem item(int itemId, String name, ItemCategory category)
	{
		String subcategory = category == ItemCategory.UNIQUE
			? uniqueSubcategory(name) : category.getDisplayLabel().toLowerCase();
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			subcategory, Collections.emptySet(), null), 1);
	}

	private static String uniqueSubcategory(String name)
	{
		if (name.contains("weapon seed")) return "weapon-upgrade";
		if (name.contains("crystal")) return "equipment-upgrade";
		if (name.contains("page")) return "equipment-charge";
		return "unique";
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}
}

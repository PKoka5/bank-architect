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

		assertEquals(Arrays.asList("Neitiznot helm", "Mystic robe bottom", "Rune boots", "Dragon scimitar", "Rune arrow"),
			nonBlankNames(sorted));
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
	public void farmingHerbloreSortsSeedsHerbsSecondariesUnfinished()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("farming-herblore"), Arrays.asList(
			item(1, "Eye of newt", ItemCategory.HERBLORE),
			item(2, "Irit potion (unf)", ItemCategory.HERBLORE),
			item(3, "Grimy irit", ItemCategory.HERBLORE),
			item(4, "Irit seed", ItemCategory.FARMING)
		));

		assertEquals(Arrays.asList("Irit seed", "Grimy irit", "Eye of newt", "Irit potion (unf)"), names(sorted));
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

	private static BankPreviewItem item(int itemId, String name, ItemCategory category)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}

	private static List<String> nonBlankNames(List<BankPreviewItem> items)
	{
		return items.stream()
			.filter(item -> !item.isBlank())
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}
}

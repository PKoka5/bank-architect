package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class GearItemSorterTest
{
	@Test
	public void sortsStyleColumnsWithinEquipmentRows()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Mystic robe bottom"),
				item(2, "Black d'hide chaps"),
				item(3, "Rune platelegs"),
				item(4, "Mystic robe top"),
				item(5, "Black d'hide body"),
				item(6, "Rune platebody"),
				item(7, "Mystic hat"),
				item(8, "Black d'hide coif"),
				item(9, "Rune full helm")
			));

		assertEquals(Arrays.asList(
			"Rune full helm",
			"Black d'hide coif",
			"Mystic hat",
			"Rune platebody",
			"Black d'hide body",
			"Mystic robe top",
			"Rune platelegs",
			"Black d'hide chaps",
			"Mystic robe bottom"
		), names(sorted));
	}

	@Test
	public void ranksWeaponsAfterWearableGear()
	{
		assertEquals(true, GearItemSorter.rank(item(1, "Rune platebody")) < GearItemSorter.rank(item(2, "Dragon scimitar")));
		assertEquals(true, GearItemSorter.rank(item(3, "Magic shortbow")) < GearItemSorter.rank(item(4, "Mystic staff")));
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			ItemCategory.GEAR.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}
}

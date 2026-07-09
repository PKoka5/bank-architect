package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
	public void laysOutStyleLanesAsColumnsWithBlankPadding()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
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

		assertEquals(24, laidOut.size());

		assertEquals("Rune full helm", laidOut.get(0).getDisplayName());
		assertEquals("Black d'hide coif", laidOut.get(1).getDisplayName());
		assertEquals("Mystic hat", laidOut.get(2).getDisplayName());
		assertBlank(laidOut, 3, 7);

		assertEquals("Rune platebody", laidOut.get(8).getDisplayName());
		assertEquals("Black d'hide body", laidOut.get(9).getDisplayName());
		assertEquals("Mystic robe top", laidOut.get(10).getDisplayName());
		assertBlank(laidOut, 11, 15);

		assertEquals("Rune platelegs", laidOut.get(16).getDisplayName());
		assertEquals("Black d'hide chaps", laidOut.get(17).getDisplayName());
		assertEquals("Mystic robe bottom", laidOut.get(18).getDisplayName());
		assertBlank(laidOut, 19, 23);
	}

	@Test
	public void promotesBestOwnedGearIntoSetupLanes()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Rune platelegs"),
				item(2, "Bandos tassets"),
				item(3, "Rune platebody"),
				item(4, "Bandos chestplate"),
				item(5, "Mystic robe bottom"),
				item(6, "Ahrim's robeskirt"),
				item(7, "Black d'hide chaps"),
				item(8, "Armadyl chainskirt")
			));

		assertEquals(20, laidOut.size());

		assertEquals("Bandos chestplate", laidOut.get(0).getDisplayName());
		assertBlank(laidOut, 1, 7);

		assertEquals("Bandos tassets", laidOut.get(8).getDisplayName());
		assertEquals("Armadyl chainskirt", laidOut.get(9).getDisplayName());
		assertEquals("Ahrim's robeskirt", laidOut.get(10).getDisplayName());
		assertBlank(laidOut, 11, 15);

		assertEquals(Arrays.asList(
			"Rune platebody",
			"Rune platelegs",
			"Black d'hide chaps",
			"Mystic robe bottom"
		), names(laidOut.subList(16, 20)));
	}

	@Test
	public void leavesSidegradesAfterPrimarySetupLanes()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Rune platelegs"),
				item(2, "Bandos tassets"),
				item(3, "Dragon scimitar"),
				item(4, "Abyssal whip"),
				item(5, "Magic shortbow"),
				item(6, "Rune crossbow")
			));

		assertEquals(19, laidOut.size());

		assertEquals("Bandos tassets", laidOut.get(0).getDisplayName());
		assertBlank(laidOut, 1, 7);

		assertEquals("Abyssal whip", laidOut.get(8).getDisplayName());
		assertEquals("Magic shortbow", laidOut.get(9).getDisplayName());
		assertBlank(laidOut, 10, 15);

		assertEquals(Arrays.asList("Rune platelegs", "Dragon scimitar", "Rune crossbow"),
			names(laidOut.subList(16, 19)));
	}

	@Test
	public void placesRangedAmmoInRangedLaneColumn()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Magic shortbow"),
				item(2, "Adamant arrow")
			));

		assertEquals(16, laidOut.size());

		assertTrue(laidOut.get(0).isBlank());
		assertEquals("Magic shortbow", laidOut.get(1).getDisplayName());
		assertBlank(laidOut, 2, 7);

		assertTrue(laidOut.get(8).isBlank());
		assertEquals("Adamant arrow", laidOut.get(9).getDisplayName());
		assertBlank(laidOut, 10, 15);
	}

	@Test
	public void fallsBackToFlatSortWithoutRecognizedSetupGear()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Cannonball"),
				item(2, "Salve amulet")
			));

		assertEquals(Arrays.asList("Salve amulet", "Cannonball"), names(laidOut));
		for (BankPreviewItem item : laidOut)
		{
			assertFalse(item.isBlank());
		}
	}

	@Test
	public void ranksWeaponsAfterWearableGear()
	{
		assertEquals(true, GearItemSorter.rank(item(1, "Rune platebody")) < GearItemSorter.rank(item(2, "Dragon scimitar")));
		assertEquals(true, GearItemSorter.rank(item(3, "Magic shortbow")) < GearItemSorter.rank(item(4, "Mystic staff")));
	}

	@Test
	public void laneRowsMatchPopupGridWidth()
	{
		assertEquals(8, GearItemSorter.LANE_GRID_COLUMNS);
	}

	private static void assertBlank(List<BankPreviewItem> items, int fromIndex, int toIndex)
	{
		for (int i = fromIndex; i <= toIndex; i++)
		{
			assertTrue("expected blank at index " + i + " but was " + items.get(i).getDisplayName(),
				items.get(i).isBlank());
		}
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

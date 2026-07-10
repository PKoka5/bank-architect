package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
	public void laysOutOneDenseRunPerCombatStyle()
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

		assertEquals(Arrays.asList(
			"Rune full helm",
			"Rune platebody",
			"Rune platelegs",
			"Black d'hide coif",
			"Black d'hide body",
			"Black d'hide chaps",
			"Mystic hat",
			"Mystic robe top",
			"Mystic robe bottom"
		), names(laidOut));
	}

	@Test
	public void planNeverContainsBlankSlotsBecauseTheBankCompacts()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Rune full helm"),
				item(2, "Magic shortbow"),
				item(3, "Adamant arrow"),
				item(4, "Mystic robe top")
			));

		assertEquals(4, laidOut.size());
		for (BankPreviewItem item : laidOut)
		{
			assertFalse("plan must be dense, found blank slot", item.isBlank());
		}
	}

	@Test
	public void promotesBestOwnedGearIntoSetupRuns()
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

		assertEquals(Arrays.asList(
			"Bandos chestplate",
			"Bandos tassets",
			"Armadyl chainskirt",
			"Ahrim's robeskirt",
			"Rune platebody",
			"Rune platelegs",
			"Black d'hide chaps",
			"Mystic robe bottom"
		), names(laidOut));
	}

	@Test
	public void leavesSidegradesAfterPrimarySetupRuns()
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

		assertEquals(Arrays.asList(
			"Bandos tassets",
			"Abyssal whip",
			"Magic shortbow",
			"Rune platelegs",
			"Dragon scimitar",
			"Rune crossbow"
		), names(laidOut));
	}

	@Test
	public void ammoJoinsTheRangedRunAfterTheWeapon()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(1, "Adamant arrow"),
				item(2, "Magic shortbow")
			));

		assertEquals(Arrays.asList("Magic shortbow", "Adamant arrow"), names(laidOut));
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

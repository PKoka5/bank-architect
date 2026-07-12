package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
	public void ammoNeverFillsAMissingEquipmentCell()
	{
		List<BankPreviewItem> input = Arrays.asList(
				item(1, "Helm of neitiznot"), item(2, "Archer helm"), item(3, "Farseer helm"),
				item(4, "Proselyte sallet"), item(5, "Gold ring"), item(6, "Emerald ring"),
				item(7, "Sapphire ring"), item(8, "Ring of life"), item(9, "Dragon arrow")
			);
		GearStatsSource stats = itemId -> {
			if (itemId == 1) return Optional.of(new GearStats(GearSlot.HEAD, 1, 0, 0, 0, 0, 0, 0, 0, 1));
			if (itemId == 2) return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 1, 0, 0, 0, 1));
			if (itemId == 3) return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 1, 0, 0, 0, 0, 1));
			if (itemId == 4) return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 0, 0, 1, 1));
			return Optional.empty();
		};
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), input, stats);

		assertTrue("ammo must follow the aligned equipment row", names(laidOut).indexOf("Dragon arrow") >= 8);
	}

	@Test
	public void terminalAmmoBlockFollowsGenericAccessories()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), Arrays.asList(
				item(1, "Adamant arrow"), item(2, "Gold ring"), item(3, "Salve amulet")));

		assertEquals(Arrays.asList("Salve amulet", "Gold ring", "Adamant arrow"), names(laidOut));
	}

	@Test
	public void terminalAmmoBlockGroupsFamiliesBeforeTiers()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), Arrays.asList(
				item(1, "Steel cannonball"), item(2, "Rune arrow"), item(3, "Adamant bolts"),
				item(4, "Dragon arrow"), item(5, "Mithril bolts"), item(6, "Rune cannonball"),
				item(7, "Mith grapple")));

		assertEquals(Arrays.asList("Dragon arrow", "Rune arrow", "Adamant bolts", "Mithril bolts",
			"Rune cannonball", "Steel cannonball", "Mith grapple"), names(laidOut));
	}

	@Test
	public void ammoMaterialNamesNeverOverrideNonAmmoGearScores()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(1, "Best body"), item(2, "Useful body"), item(3, "Rune body"));
		GearStatsSource stats = itemId -> Optional.of(new GearStats(GearSlot.BODY,
			0, 0, 0, 0, 0, 0, 0, 0, itemId == 1 ? 3000 : itemId == 2 ? 2000 : 100));

		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), input, stats);

		assertEquals(Arrays.asList("Best body", "Useful body", "Rune body"), names(laidOut));
	}

	@Test
	public void spareSameSlotItemsCompleteTheRowInTierOrder()
	{
		// Four style helms plus four spare helms: the helm row fills itself
		// with the spares (best first) instead of needing generic filler.
		List<BankPreviewItem> input = Arrays.asList(
			item(1, "Melee helm"), item(2, "Ranged helm"), item(3, "Magic helm"),
			item(4, "Prayer helm"), item(5, "Spare helm strong"), item(6, "Spare helm weak"),
			item(7, "Spare helm medium"), item(8, "Spare helm minor")
		);
		GearStatsSource stats = itemId -> {
			switch (itemId)
			{
				case 1: return Optional.of(new GearStats(GearSlot.HEAD, 5, 0, 0, 0, 0, 0, 0, 0, 90));
				case 2: return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 5, 0, 0, 0, 80));
				case 3: return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 5, 0, 0, 0, 0, 70));
				case 4: return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 0, 0, 5, 60));
				case 5: return Optional.of(new GearStats(GearSlot.HEAD, 4, 0, 0, 0, 0, 0, 0, 0, 50));
				case 6: return Optional.of(new GearStats(GearSlot.HEAD, 1, 0, 0, 0, 0, 0, 0, 0, 10));
				case 7: return Optional.of(new GearStats(GearSlot.HEAD, 2, 0, 0, 0, 0, 0, 0, 0, 30));
				case 8: return Optional.of(new GearStats(GearSlot.HEAD, 1, 0, 0, 0, 0, 0, 0, 0, 20));
				default: return Optional.empty();
			}
		};

		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), input, stats);

		assertEquals(Arrays.asList(
			"Melee helm", "Ranged helm", "Magic helm", "Prayer helm",
			"Spare helm strong", "Spare helm medium", "Spare helm minor", "Spare helm weak"
		), names(laidOut));
	}

	@Test
	public void completeSlotRowsStayAlignedWithoutGenericFiller()
	{
		// Two full slot rows and zero rings/utility filler: both rows must
		// still come out aligned instead of collapsing into dense runs.
		List<BankPreviewItem> input = Arrays.asList(
			item(1, "Body m"), item(2, "Body r"), item(3, "Body g"), item(4, "Body p"),
			item(5, "Body s1"), item(6, "Body s2"), item(7, "Body s3"), item(8, "Body s4"),
			item(11, "Legs m"), item(12, "Legs r"), item(13, "Legs g"), item(14, "Legs p"),
			item(15, "Legs s1"), item(16, "Legs s2"), item(17, "Legs s3"), item(18, "Legs s4")
		);
		GearStatsSource stats = itemId -> {
			GearSlot slot = itemId < 10 ? GearSlot.BODY : GearSlot.LEGS;
			int style = itemId % 10;
			int stab = style == 1 ? 5 : style >= 5 ? 1 : 0;
			int ranged = style == 2 ? 5 : 0;
			int magic = style == 3 ? 5 : 0;
			int prayer = style == 4 ? 5 : 0;
			int defence = 100 - style * 10;
			return Optional.of(new GearStats(slot, stab, 0, 0, magic, ranged, 0, 0, prayer, defence));
		};

		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), input, stats);

		assertEquals(16, laidOut.size());
		assertEquals("Body m", laidOut.get(0).getDisplayName());
		assertEquals("Body r", laidOut.get(1).getDisplayName());
		assertEquals("Body g", laidOut.get(2).getDisplayName());
		assertEquals("Body p", laidOut.get(3).getDisplayName());
		assertEquals("Legs m", laidOut.get(8).getDisplayName());
		assertEquals("Legs r", laidOut.get(9).getDisplayName());
		assertEquals("Legs g", laidOut.get(10).getDisplayName());
		assertEquals("Legs p", laidOut.get(11).getDisplayName());
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

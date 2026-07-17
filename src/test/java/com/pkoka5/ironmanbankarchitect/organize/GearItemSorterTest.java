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
	public void primarySetupWinsWhenACompactBankCannotAlsoPreserveVerticalFamilies()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(950001, "Melee body"), item(950002, "Ranged body"),
			item(950003, "Magic body"), item(950004, "Prayer body"),
			item(544, "Monk's robe top"), item(542, "Monk's robe"),
			item(4720, "Dharok's platebody"), item(4722, "Dharok's platelegs"));
		GearStatsSource stats = itemId -> {
			if (itemId < 950001 || itemId > 950004)
			{
				// Keep the four exact set members out of later setup-row reservations;
				// this fixture isolates the no-alternative-filler fallback itself.
				return Optional.of(new GearStats(GearSlot.RING,
					0, 0, 0, 0, 0, 0, 0, 0, 0));
			}
			int style = itemId - 950001;
			return Optional.of(new GearStats(GearSlot.BODY,
				style == 0 ? 10 : 0, 0, 0, style == 2 ? 10 : 0,
				style == 1 ? 10 : 0, 0, 0, style == 3 ? 10 : 0, 2000));
		};

		List<BankPreviewItem> laidOut = GearItemSorter.layout(input, stats);

		assertEquals(Arrays.asList("Melee body", "Ranged body", "Magic body", "Prayer body"),
			names(laidOut.subList(0, 4)));
		assertEquals(8, laidOut.size());
		assertEquals(input.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()),
			laidOut.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
	}

	@Test
	public void sparseEarlySlotDoesNotBlockLaterCompleteRow()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(1, "Sparse helm"),
			item(11, "Body m"), item(12, "Body r"), item(13, "Body g"), item(14, "Body p"),
			item(15, "Body spare 1"), item(16, "Body spare 2"),
			item(17, "Body spare 3"), item(18, "Body spare 4"));
		GearStatsSource stats = itemId -> {
			if (itemId == 1)
			{
				return Optional.of(new GearStats(GearSlot.HEAD, 5, 0, 0, 0, 0, 0, 0, 0, 50));
			}
			int suffix = itemId % 10;
			int melee = suffix == 1 || suffix >= 5 ? 5 : 0;
			int ranged = suffix == 2 ? 5 : 0;
			int magic = suffix == 3 ? 5 : 0;
			int prayer = suffix == 4 ? 5 : 0;
			return Optional.of(new GearStats(GearSlot.BODY,
				melee, 0, 0, magic, ranged, 0, 0, prayer, 100 - suffix));
		};

		List<BankPreviewItem> laidOut = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("combat-gear"), input, stats);

		assertEquals(9, laidOut.size());
		assertEquals(Arrays.asList("Body m", "Body r", "Body g", "Body p",
			"Body spare 1", "Body spare 2", "Body spare 3", "Body spare 4", "Sparse helm"),
			names(laidOut));
		assertEquals(input.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()),
			laidOut.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
		for (BankPreviewItem item : laidOut)
		{
			assertFalse("plan must not invent a blank/filler cell", item.isBlank());
		}
	}

	@Test
	public void earlyRowsCannotExhaustFillersNeededByLaterStyleRows()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(1, "Melee helm"), item(2, "Ranged helm"),
			item(3, "Magic helm"), item(4, "Prayer helm"),
			item(5, "Ring one"), item(6, "Ring two"),
			item(7, "Ring three"), item(8, "Ring four"),
			item(11, "Melee cape"), item(12, "Ranged cape"), item(13, "Magic cape"),
			item(21, "Melee amulet"), item(22, "Ranged amulet"),
			item(23, "Spare amulet one"), item(24, "Spare amulet two"),
			item(25, "Spare amulet three"), item(26, "Spare amulet four"),
			item(27, "Spare amulet five"));
		GearStatsSource stats = itemId -> {
			GearSlot slot;
			int style;
			if (itemId <= 4)
			{
				slot = GearSlot.HEAD;
				style = itemId;
			}
			else if (itemId >= 11 && itemId <= 13)
			{
				slot = GearSlot.CAPE;
				style = itemId - 10;
			}
			else if (itemId >= 21)
			{
				slot = GearSlot.NECK;
				style = itemId == 22 ? 2 : 1;
			}
			else
			{
				return Optional.empty();
			}
			int melee = style == 1 ? 5 : 0;
			int ranged = style == 2 ? 5 : 0;
			int magic = style == 3 ? 5 : 0;
			int prayer = style == 4 ? 5 : 0;
			int defence = itemId == 21 || itemId == 22 ? 100 : 10;
			return Optional.of(new GearStats(slot, melee, 0, 0, magic, ranged,
				melee, ranged, prayer, defence));
		};

		GearItemSorter.GearLayout plan = GearItemSorter.plan(input, stats);
		List<BankPreviewItem> setup = plan.getSetupRows();

		assertEquals(16, setup.size());
		assertEquals(Arrays.asList("Melee helm", "Ranged helm", "Magic helm", "Prayer helm"),
			names(setup.subList(0, 4)));
		assertEquals(Arrays.asList("Ring four", "Ring one", "Ring three", "Ring two"),
			names(setup.subList(4, 8)));
		assertEquals(Arrays.asList("Melee cape", "Ranged cape", "Magic cape"),
			names(setup.subList(8, 11)));
		assertEquals(input.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()),
			GearItemSorter.layout(input, stats).stream()
				.map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
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

	@Test
	public void curatedTierScoreOutranksNameHeuristicForRealItemIds()
	{
		int torvaScore = GearItemSorter.score(item(26384, "Torva platebody"), GearStatsSource.NONE);
		int bandosScore = GearItemSorter.score(item(11832, "Bandos chestplate"), GearStatsSource.NONE);
		int runeScore = GearItemSorter.score(item(1127, "Rune platebody"), GearStatsSource.NONE);

		assertTrue("End tier must outrank Late tier", torvaScore > bandosScore);
		assertTrue("Late tier must outrank a lower curated tier", bandosScore > runeScore);
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

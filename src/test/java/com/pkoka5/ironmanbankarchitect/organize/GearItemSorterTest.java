package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
				item(50001, "Mystic robe bottom"),
				item(50002, "Black d'hide chaps"),
				item(50003, "Rune platelegs"),
				item(50004, "Mystic robe top"),
				item(50005, "Black d'hide body"),
				item(50006, "Rune platebody"),
				item(50007, "Mystic hat"),
				item(50008, "Black d'hide coif"),
				item(50009, "Rune full helm")
			));

		assertEquals(Arrays.asList(
			"Black d'hide coif",
			"Black d'hide body",
			"Black d'hide chaps",
			"Rune full helm",
			"Rune platebody",
			"Rune platelegs",
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
	public void keepsStyleLoadoutsTogetherBeforeLowerTierAlternatives()
	{
		List<BankPreviewItem> laidOut = PresetItemSorter.sort(BankPresets.IRONMAN.getCategory("combat-gear"),
			Arrays.asList(
				item(50101, "Rune platelegs"),
				item(50102, "Bandos tassets"),
				item(50103, "Rune platebody"),
				item(50104, "Bandos chestplate"),
				item(50105, "Mystic robe bottom"),
				item(50106, "Ahrim's robeskirt"),
				item(50107, "Black d'hide chaps"),
				item(50108, "Armadyl chainskirt")
			));

		assertEquals(Arrays.asList(
			"Armadyl chainskirt",
			"Black d'hide chaps",
			"Bandos chestplate",
			"Bandos tassets",
			"Rune platebody",
			"Rune platelegs",
			"Ahrim's robeskirt",
			"Mystic robe bottom"
		), names(laidOut));
	}

	@Test
	public void keepsMeleeAlternativesTogetherBeforeTheRangedLoadout()
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
			"Dragon scimitar",
			"Rune platelegs",
			"Magic shortbow",
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
	public void keepsBowfaCrystalArmourAndMasoriAssemblerInOneLoadout()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(10551, "Fighter torso"),
			item(23971, "Crystal helm"),
			item(23975, "Crystal body"),
			item(23979, "Crystal legs"),
			item(25865, "Bow of faerdhinen"),
			item(27374, "Masori assembler"),
			item(24271, "Neitiznot faceguard")
		), GearItemSorterTest::combatStats);

		assertEquals(Arrays.asList(
			"Crystal helm", "Crystal body", "Crystal legs", "Bow of faerdhinen", "Masori assembler"
		), names(laidOut.subList(0, 5)));
	}

	@Test
	public void recognizesCrystalBowAndColouredArmourAsTheSameFunctionalLoadout()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(27709, "Crystal body (Hefin)"),
			item(23983, "Crystal bow"),
			item(27713, "Crystal legs (Hefin)"),
			item(27705, "Crystal helm (Hefin)"),
			item(27374, "Masori assembler")
		));

		assertEquals(Arrays.asList(
			"Crystal helm (Hefin)", "Crystal body (Hefin)", "Crystal legs (Hefin)",
			"Crystal bow", "Masori assembler"
		), names(laidOut));
	}

	@Test
	public void derivesCompleteProgressionLoadoutsFromAnyOwnedTiers()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(101, "Early helm"), item(102, "Late body"),
			item(103, "Middle legs"), item(104, "Early weapon"),
			item(105, "Late helm"), item(106, "Middle body"),
			item(107, "Late legs"), item(108, "Middle weapon"),
			item(109, "Middle helm"), item(110, "Early body"),
			item(111, "Early legs"), item(112, "Late weapon")
		);
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(101, meleeStats(GearSlot.HEAD, 10));
		stats.put(102, meleeStats(GearSlot.BODY, 30));
		stats.put(103, meleeStats(GearSlot.LEGS, 20));
		stats.put(104, meleeStats(GearSlot.WEAPON, 10));
		stats.put(105, meleeStats(GearSlot.HEAD, 30));
		stats.put(106, meleeStats(GearSlot.BODY, 20));
		stats.put(107, meleeStats(GearSlot.LEGS, 30));
		stats.put(108, meleeStats(GearSlot.WEAPON, 20));
		stats.put(109, meleeStats(GearSlot.HEAD, 20));
		stats.put(110, meleeStats(GearSlot.BODY, 10));
		stats.put(111, meleeStats(GearSlot.LEGS, 10));
		stats.put(112, meleeStats(GearSlot.WEAPON, 30));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(input,
			itemId -> Optional.ofNullable(stats.get(itemId)));

		assertEquals(Arrays.asList(
			"Late helm", "Late body", "Late legs", "Late weapon",
			"Middle helm", "Middle body", "Middle legs", "Middle weapon",
			"Early helm", "Early body", "Early legs", "Early weapon"
		), names(laidOut));
	}

	@Test
	public void alignsDynamicArmourLoadoutsAsVerticalColumnsWhenRealItemsFillTheGrid()
	{
		String[] tiers = {"Late", "Upper middle", "Lower middle", "Early"};
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.WEAPON,
			GearSlot.CAPE, GearSlot.NECK, GearSlot.HANDS, GearSlot.FEET};
		String[] slotNames = {"helm", "body", "legs", "weapon", "cape", "neck", "hands", "feet"};
		List<BankPreviewItem> input = new ArrayList<>();
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int tier = tiers.length - 1; tier >= 0; tier--)
		{
			int strength = (tiers.length - tier) * 10;
			for (int slot = slots.length - 1; slot >= 0; slot--)
			{
				int itemId = 10000 + tier * 10 + slot;
				input.add(item(itemId, tiers[tier] + " " + slotNames[slot]));
				stats.put(itemId, meleeStats(slots[slot], strength));
			}
		}

		List<BankPreviewItem> laidOut = GearItemSorter.layout(input,
			itemId -> Optional.ofNullable(stats.get(itemId)));

		for (int column = 0; column < tiers.length; column++)
		{
			assertEquals(tiers[column] + " helm", laidOut.get(column).getDisplayName());
			assertEquals(tiers[column] + " body", laidOut.get(8 + column).getDisplayName());
			assertEquals(tiers[column] + " legs", laidOut.get(16 + column).getDisplayName());
			assertEquals(tiers[column] + " weapon", laidOut.get(24 + column).getDisplayName());
		}
	}

	@Test
	public void disablingRowFillingAlsoDisablesVerticalAlignment()
	{
		String[] tiers = {"Late", "Upper middle", "Lower middle", "Early"};
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.WEAPON,
			GearSlot.CAPE, GearSlot.NECK, GearSlot.HANDS, GearSlot.FEET};
		String[] slotNames = {"helm", "body", "legs", "weapon", "cape", "neck", "hands", "feet"};
		List<BankPreviewItem> input = new ArrayList<>();
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int tier = tiers.length - 1; tier >= 0; tier--)
		{
			int strength = (tiers.length - tier) * 10;
			for (int slot = slots.length - 1; slot >= 0; slot--)
			{
				int itemId = 60000 + tier * 10 + slot;
				input.add(item(itemId, tiers[tier] + " " + slotNames[slot]));
				stats.put(itemId, meleeStats(slots[slot], strength));
			}
		}

		List<BankPreviewItem> laidOut = GearItemSorter.layout(input,
			itemId -> Optional.ofNullable(stats.get(itemId)), false);

		assertEquals(Arrays.asList(
			"Late helm", "Late body", "Late legs", "Late weapon",
			"Late cape", "Late neck", "Late hands", "Late feet"
		), names(laidOut.subList(0, 8)));
	}

	@Test
	public void alignsCrystalVerticallyWhenCombatLoadoutsFillTheRows()
	{
		List<BankPreviewItem> crystal = Arrays.asList(
			item(23971, "Crystal helm"), item(23975, "Crystal body"),
			item(23979, "Crystal legs"), item(25865, "Bow of faerdhinen"));
		List<BankPreviewItem> accessories = new ArrayList<>();
		List<BankPreviewItem> allItems = new ArrayList<>(crystal);
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (BankPreviewItem crystalItem : crystal)
		{
			stats.put(crystalItem.getItemId(), combatStats(crystalItem.getItemId()).get());
		}
		int[] accessoryIds = {27374, 11773, 19710, 29591};
		String[] accessoryNames = {
			"Masori assembler", "Berserker ring (i)",
			"Ring of suffering (i)", "Scorching bow"
		};
		GearSlot[] accessorySlots = {
			GearSlot.CAPE, GearSlot.RING, GearSlot.RING, GearSlot.WEAPON
		};
		for (int index = 0; index < accessorySlots.length; index++)
		{
			int itemId = accessoryIds[index];
			BankPreviewItem accessory = item(itemId, accessoryNames[index]);
			accessories.add(accessory);
			allItems.add(accessory);
			stats.put(itemId, rangedStats(accessorySlots[index], 20));
		}

		List<CombatGearGridLayout.Block> blocks = new ArrayList<>();
		CombatGearIndex gear;
		List<List<BankPreviewItem>> genericLoadouts = new ArrayList<>();
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.WEAPON,
			GearSlot.CAPE, GearSlot.NECK, GearSlot.HANDS, GearSlot.FEET};
		for (int loadout = 0; loadout < 3; loadout++)
		{
			List<BankPreviewItem> items = new ArrayList<>();
			for (int slot = 0; slot < slots.length; slot++)
			{
				int itemId = 70100 + loadout * 10 + slot;
				String name = "Ranged loadout " + loadout + " item " + slot;
				if (loadout == 0 && slot >= 4)
				{
					int[] replacedIds = {10499, 2412, 2413, 2414};
					String[] replacedNames = {
						"Ava's accumulator", "Saradomin cape", "Guthix cape", "Zamorak cape"
					};
					itemId = replacedIds[slot - 4];
					name = replacedNames[slot - 4];
				}
				BankPreviewItem item = item(itemId, name);
				items.add(item);
				allItems.add(item);
				stats.put(itemId, rangedStats(slots[slot], 10 - loadout));
			}
			genericLoadouts.add(items);
		}
		gear = new CombatGearIndex(allItems,
			itemId -> Optional.ofNullable(stats.get(itemId)));
		List<BankPreviewItem> crystalLoadout = new ArrayList<>(crystal);
		crystalLoadout.addAll(accessories);
		blocks.add(new CombatGearGridLayout.Block(
			"crystal", GearStyle.RANGED, crystalLoadout, gear));
		for (int index = 0; index < genericLoadouts.size(); index++)
		{
			blocks.add(new CombatGearGridLayout.Block(
				"generic-" + index, GearStyle.RANGED, genericLoadouts.get(index), gear));
		}

		List<BankPreviewItem> laidOut = CombatGearGridLayout.layout(blocks, gear, true);

		assertEquals("Crystal helm", laidOut.get(0).getDisplayName());
		assertEquals(accessoryNames[0], laidOut.get(4).getDisplayName());
		assertEquals(accessoryNames[1], laidOut.get(5).getDisplayName());
		assertEquals(accessoryNames[2], laidOut.get(6).getDisplayName());
		assertEquals(accessoryNames[3], laidOut.get(7).getDisplayName());
		assertEquals("Crystal body", laidOut.get(8).getDisplayName());
		assertEquals("Crystal legs", laidOut.get(16).getDisplayName());
		assertEquals("Bow of faerdhinen", laidOut.get(24).getDisplayName());
	}

	@Test
	public void choosesTheVoidHelmThatMatchesTheStrongestOwnedStyle()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(11663, "Void mage helm"), item(11664, "Void ranger helm"),
			item(11665, "Void melee helm"), item(8839, "Void knight top"),
			item(8840, "Void knight robe"), item(8842, "Void knight gloves"),
			item(20997, "Twisted bow"), item(4151, "Abyssal whip"),
			item(12899, "Trident of the swamp")
		);
		GearStatsSource stats = itemId ->
		{
			if (itemId == 20997)
			{
				return Optional.of(rangedStats(GearSlot.WEAPON, 40));
			}
			if (itemId == 4151)
			{
				return Optional.of(meleeStats(GearSlot.WEAPON, 10));
			}
			if (itemId == 12899)
			{
				return Optional.of(magicStats(GearSlot.WEAPON, 5));
			}
			return Optional.empty();
		};

		List<BankPreviewItem> laidOut = GearItemSorter.layout(input, stats);

		assertEquals(Arrays.asList(
			"Void ranger helm", "Void knight top", "Void knight robe", "Void knight gloves"
		), names(laidOut.subList(0, 4)));
	}

	@Test
	public void keepsEveryOwnedVoidHelmBesideTheSharedVoidCore()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(11663, "Void mage helm"), item(11664, "Void ranger helm"),
			item(11665, "Void melee helm"), item(8839, "Void knight top"),
			item(8840, "Void knight robe"), item(8842, "Void knight gloves"),
			item(1127, "Rune platebody"), item(4151, "Abyssal whip")
		));

		assertContiguous(laidOut, Arrays.asList(
			"Void mage helm", "Void ranger helm", "Void melee helm",
			"Void knight top", "Void knight robe", "Void knight gloves"));
	}

	@Test
	public void familyLayoutIsIndependentOfBankInputOrder()
	{
		List<BankPreviewItem> input = new ArrayList<>(Arrays.asList(
			item(11663, "Void mage helm"), item(11664, "Void ranger helm"),
			item(11665, "Void melee helm"), item(8839, "Void knight top"),
			item(8840, "Void knight robe"), item(8842, "Void knight gloves"),
			item(6109, "Ghostly hood"), item(6107, "Ghostly robe"),
			item(6108, "Ghostly robe"), item(4151, "Abyssal whip")
		));
		List<Integer> forward = GearItemSorter.layout(input).stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
		Collections.reverse(input);
		List<Integer> reversed = GearItemSorter.layout(input).stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());

		assertEquals(forward, reversed);
	}

	@Test
	public void highImpactUtilityGearRanksBeforeRoutineFallbacks()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(4734, "Karil's crossbow"), item(4738, "Karil's leatherskirt"),
			item(7460, "Rune gloves"), item(11200, "Dwarven helmet"),
			item(20714, "Tome of fire"), item(5698, "Dragon dagger(p++)"),
			item(11773, "Berserker ring (i)"), item(12926, "Toxic blowpipe"),
			item(12931, "Serpentine helm"), item(29591, "Scorching bow"),
			item(29594, "Purging staff"), item(19710, "Ring of suffering (i)"),
			item(29589, "Emberlight"), item(29577, "Burning claws"),
			item(12018, "Salve amulet(ei)"), item(30634, "Twinflame staff"),
			item(11865, "Slayer helmet (i)"), item(19641, "Black slayer helmet (i)"),
			item(29818, "Araxyte slayer helmet (i)")
		));

		List<String> routine = Arrays.asList(
			"Karil's crossbow", "Rune gloves", "Dwarven helmet");
		List<String> priority = Arrays.asList(
			"Tome of fire", "Dragon dagger(p++)", "Berserker ring (i)",
			"Toxic blowpipe", "Serpentine helm", "Scorching bow", "Purging staff",
			"Ring of suffering (i)", "Emberlight", "Burning claws", "Salve amulet(ei)",
			"Twinflame staff", "Slayer helmet (i)", "Black slayer helmet (i)",
			"Araxyte slayer helmet (i)");
		int lastPriority = priority.stream().mapToInt(name -> names(laidOut).indexOf(name)).max().getAsInt();
		int firstRoutine = routine.stream().mapToInt(name -> names(laidOut).indexOf(name)).min().getAsInt();
		assertTrue("priority gear must precede routine fallback gear: " + names(laidOut),
			lastPriority < firstRoutine);
		assertTrue("Karil's armour remains more useful than its situational crossbow",
			names(laidOut).indexOf("Karil's leatherskirt")
				< names(laidOut).indexOf("Karil's crossbow"));
	}

	@Test
	public void imbuedGodCapeRanksBeforeEveryOwnedStandardGodCape()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(2412, "Saradomin cape"), item(2413, "Guthix cape"),
			item(2414, "Zamorak cape"), item(21793, "Imbued guthix cape"),
			item(29594, "Purging staff"), item(30634, "Twinflame staff"),
			item(20714, "Tome of fire")
		), itemId -> {
			if (itemId == 2412 || itemId == 2413 || itemId == 2414)
			{
				return Optional.of(magicStats(GearSlot.CAPE, 10));
			}
			if (itemId == 21793)
			{
				return Optional.of(magicStats(GearSlot.CAPE, 15));
			}
			return Optional.empty();
		});

		int firstStandardCape = Math.min(names(laidOut).indexOf("Saradomin cape"),
			Math.min(names(laidOut).indexOf("Guthix cape"), names(laidOut).indexOf("Zamorak cape")));
		assertTrue("the imbued cape should precede every obsolete base cape: " + names(laidOut),
			names(laidOut).indexOf("Imbued guthix cape") < firstStandardCape);
		assertTrue("owned base capes should follow the higher-value magic utilities: " + names(laidOut),
			firstStandardCape > names(laidOut).indexOf("Purging staff")
				&& firstStandardCape > names(laidOut).indexOf("Twinflame staff")
				&& firstStandardCape > names(laidOut).indexOf("Tome of fire"));
	}

	@Test
	public void completeCannonStaysInAssemblyOrder()
	{
		List<BankPreviewItem> input = new ArrayList<>(Arrays.asList(
			item(6, "Cannon base"), item(8, "Cannon stand"),
			item(10, "Cannon barrels"), item(12, "Cannon furnace"),
			item(1635, "Gold ring"), item(1637, "Sapphire ring"),
			item(1639, "Emerald ring"), item(1641, "Ruby ring")
		));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int tier = 0; tier < 3; tier++)
		{
			int strength = 30 - tier * 10;
			GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.WEAPON,
				GearSlot.CAPE, GearSlot.NECK, GearSlot.HANDS, GearSlot.FEET};
			for (int slot = 0; slot < slots.length; slot++)
			{
				int itemId = 40000 + tier * 10 + slot;
				input.add(item(itemId, "Tier " + tier + " slot " + slot));
				stats.put(itemId, meleeStats(slots[slot], strength));
			}
		}
		List<BankPreviewItem> laidOut = GearItemSorter.layout(input,
			itemId -> Optional.ofNullable(stats.get(itemId)));

		assertContiguous(laidOut, Arrays.asList(
			"Cannon base", "Cannon stand", "Cannon barrels", "Cannon furnace"));
	}

	@Test
	public void mixedOrnamentedCannonPartsRemainOneAssemblyFamily()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(6, "Cannon base"), item(26522, "Cannon stand (or)"),
			item(10, "Cannon barrels"), item(26526, "Cannon furnace (or)"),
			item(4151, "Abyssal whip"), item(1127, "Rune platebody")
		));

		assertContiguousIds(laidOut, Arrays.asList(6, 26522, 10, 26526));
	}

	@Test
	public void mechanicLoadoutComplementsNeverBorrowAmmunition()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(25865, "Bow of faerdhinen"), item(23971, "Crystal helm"),
			item(23975, "Crystal body"), item(23979, "Crystal legs"),
			item(892, "Rune arrow"), item(9244, "Dragon bolts (e)"),
			item(11212, "Dragon arrow"), item(10034, "Red chinchompa"),
			item(811, "Rune dart"), item(868, "Rune knife"),
			item(4151, "Abyssal whip")
		), itemId -> {
			if (itemId == 892 || itemId == 9244 || itemId == 11212
				|| itemId == 10034 || itemId == 811 || itemId == 868)
			{
				return Optional.of(rangedStats(GearSlot.WEAPON, 25));
			}
			return combatStats(itemId);
		});

		int lastGear = names(laidOut).indexOf("Abyssal whip");
		assertTrue(names(laidOut).indexOf("Rune arrow") > lastGear);
		assertTrue(names(laidOut).indexOf("Dragon bolts (e)") > lastGear);
		assertTrue(names(laidOut).indexOf("Dragon arrow") > lastGear);
		assertTrue(names(laidOut).indexOf("Red chinchompa") > lastGear);
		assertTrue(names(laidOut).indexOf("Rune dart") > lastGear);
		assertTrue(names(laidOut).indexOf("Rune knife") > lastGear);
	}

	@Test
	public void startsMixedMeleeWithAUsableLoadoutInsteadOfSlotRows()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(24271, "Neitiznot faceguard"),
			item(10551, "Fighter torso"),
			item(29022, "Blood moon chestplate"),
			item(29025, "Blood moon tassets"),
			item(4151, "Abyssal whip"),
			item(6570, "Fire cape"),
			item(6585, "Amulet of fury"),
			item(7462, "Barrows gloves")
		), GearItemSorterTest::combatStats);

		assertEquals(Arrays.asList(
			"Neitiznot faceguard", "Blood moon chestplate", "Blood moon tassets", "Abyssal whip",
			"Fire cape", "Amulet of fury", "Barrows gloves", "Fighter torso"
		), names(laidOut));
	}

	@Test
	public void keepsACompleteMoonSetTogether()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(24271, "Neitiznot faceguard"),
			item(28997, "Dual macuahuitl"),
			item(29028, "Blood moon helm"),
			item(29022, "Blood moon chestplate"),
			item(29025, "Blood moon tassets"),
			item(10551, "Fighter torso")
		), GearItemSorterTest::combatStats);

		assertEquals(Arrays.asList(
			"Blood moon helm", "Blood moon chestplate", "Blood moon tassets", "Dual macuahuitl"
		), names(laidOut.subList(0, 4)));
	}

	@Test
	public void keepsTheBlueMoonSpearWithItsCompleteMagicSet()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(28988, "Blue moon spear"),
			item(29019, "Blue moon helm"),
			item(29013, "Blue moon chestplate"),
			item(29016, "Blue moon tassets"),
			item(4091, "Mystic robe top")
		));

		assertEquals(Arrays.asList(
			"Blue moon helm", "Blue moon chestplate", "Blue moon tassets", "Blue moon spear"
		), names(laidOut.subList(0, 4)));
	}

	@Test
	public void keepsIncompleteAndCompleteBarrowsFamiliesTogether()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(4753, "Verac's helm"), item(4755, "Verac's flail"),
			item(4757, "Verac's brassard"),
			item(4716, "Dharok's helm"), item(4718, "Dharok's greataxe"),
			item(4720, "Dharok's platebody"), item(4722, "Dharok's platelegs"),
			item(1127, "Rune platebody"), item(4151, "Abyssal whip")
		));

		assertContiguousIds(laidOut, Arrays.asList(4753, 4757, 4755));
		assertContiguousIds(laidOut, Arrays.asList(4716, 4720, 4722, 4718));
	}

	@Test
	public void completeKarilLoadoutDoesNotKeepTheStandaloneCrossbowPenalty()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(51001, rangedStats(GearSlot.HEAD, 94));
		stats.put(51002, rangedStats(GearSlot.BODY, 94));
		stats.put(51003, rangedStats(GearSlot.LEGS, 94));
		stats.put(51004, rangedStats(GearSlot.WEAPON, 94));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(4732, "Karil's coif"), item(4736, "Karil's leathertop"),
			item(4738, "Karil's leatherskirt"), item(4734, "Karil's crossbow"),
			item(51001, "Competitor head"), item(51002, "Competitor body"),
			item(51003, "Competitor legs"), item(51004, "Competitor weapon")
		), itemId -> Optional.ofNullable(stats.get(itemId)));

		assertEquals(Arrays.asList(
			"Karil's coif", "Karil's leathertop", "Karil's leatherskirt", "Karil's crossbow"
		), names(laidOut.subList(0, 4)));
	}

	@Test
	public void keepsGhostlyAndEliteBlackFamiliesInEquipmentOrder()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(6106, "Ghostly boots"), item(6107, "Ghostly robe"),
			item(6108, "Ghostly robe"), item(6109, "Ghostly hood"),
			item(6110, "Ghostly gloves"), item(6111, "Ghostly cloak"),
			item(29560, "Elite black full helm"), item(29562, "Elite black platebody"),
			item(29564, "Elite black platelegs"), item(1127, "Rune platebody"),
			item(4151, "Abyssal whip")
		));

		assertContiguousIds(laidOut, Arrays.asList(6109, 6107, 6108, 6111, 6110, 6106));
		assertContiguousIds(laidOut, Arrays.asList(29560, 29562, 29564));
	}

	@Test
	public void keepsEveryOwnedShayzienTierTogetherInEquipmentOrder()
	{
		for (int tier = 1; tier <= 5; tier++)
		{
			int hands = 13357 + (tier - 1) * 5;
			int feet = hands + 1;
			int head = hands + 2;
			int legs = hands + 3;
			int body = hands + 4;
			Map<Integer, GearStats> stats = new LinkedHashMap<>();
			stats.put(hands, meleeStats(GearSlot.HANDS, tier));
			stats.put(feet, meleeStats(GearSlot.FEET, tier));
			stats.put(head, meleeStats(GearSlot.HEAD, tier));
			stats.put(legs, meleeStats(GearSlot.LEGS, tier));
			stats.put(body, meleeStats(GearSlot.BODY, tier));
			stats.put(1163, meleeStats(GearSlot.HEAD, 30));
			List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
				item(hands, "Shayzien gloves (" + tier + ")"),
				item(feet, "Shayzien boots (" + tier + ")"),
				item(head, "Shayzien helm (" + tier + ")"),
				item(legs, "Shayzien greaves (" + tier + ")"),
				item(body, "Shayzien body (" + tier + ")"),
				item(1163, "Rune full helm")
			), itemId -> Optional.ofNullable(stats.get(itemId)));

			assertContiguousIds(laidOut, Arrays.asList(head, body, legs, hands, feet));
		}
	}

	@Test
	public void sendsBrokenEquipmentToTheRepairBlock()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(29052, "Eclipse moon tassets (broken)"),
			item(24271, "Neitiznot faceguard"),
			item(10551, "Fighter torso"),
			item(4151, "Abyssal whip")
		), GearItemSorterTest::combatStats);

		assertEquals("Eclipse moon tassets (broken)",
			laidOut.get(laidOut.size() - 1).getDisplayName());
	}

	@Test
	public void sendsDamagedTorvaToTheRepairBlock()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(26376, "Torva full helm (damaged)"),
			item(1163, "Rune full helm")
		));

		assertEquals("Rune full helm", laidOut.get(0).getDisplayName());
		assertEquals("Torva full helm (damaged)", laidOut.get(1).getDisplayName());
	}

	@Test
	public void placeholdersCannotCompleteMechanicLoadouts()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(25865, "Bow of faerdhinen"),
			placeholder(23971, "Crystal helm"),
			item(23975, "Crystal body"),
			item(23979, "Crystal legs"),
			item(1163, "Rune full helm")
		);
		List<BankPreviewItem> owned = input.stream()
			.filter(item -> !item.isPlaceholder())
			.collect(Collectors.toList());
		CombatLoadoutResolver.Relationships relationships = CombatLoadoutResolver.resolve(
			new CombatGearIndex(owned, GearItemSorterTest::combatStats));
		List<BankPreviewItem> laidOut = GearItemSorter.layout(
			input, GearItemSorterTest::combatStats);

		assertFalse(relationships.loadouts().stream()
			.anyMatch(loadout -> "crystal-bowfa".equals(loadout.key())));
		assertContiguousIds(laidOut, Arrays.asList(23971, 23975, 23979, 25865));
	}

	@Test
	public void placeholdersKeepAnInactiveBarrowsFamilyTogetherWithoutRaisingItsPriority()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(25865, "Bow of faerdhinen"),
			item(23971, "Crystal helm"),
			item(23975, "Crystal body"),
			item(23979, "Crystal legs"),
			item(27374, "Masori assembler"),
			item(4716, "Dharok's helm"),
			placeholder(4718, "Dharok's greataxe"),
			placeholder(4720, "Dharok's platebody"),
			placeholder(4722, "Dharok's platelegs"),
			item(24271, "Neitiznot faceguard"),
			item(10551, "Fighter torso"),
			item(4151, "Abyssal whip"),
			item(6570, "Fire cape")
		), GearItemSorterTest::combatStats, false);

		assertEquals("Crystal helm", laidOut.get(0).getDisplayName());
		assertContiguousIds(laidOut, Arrays.asList(4716, 4720, 4722, 4718));
	}

	@Test
	public void strongerGenericLoadoutRanksBeforeLowerTierExactSet()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(6523, "Toktz-xil-ak"),
			item(21298, "Obsidian helmet"),
			item(21301, "Obsidian platebody"),
			item(21304, "Obsidian platelegs"),
			item(26382, "Torva full helm"),
			item(26384, "Torva platebody"),
			item(26386, "Torva platelegs")
		));

		assertEquals(Arrays.asList("Torva full helm", "Torva platebody", "Torva platelegs"),
			names(laidOut.subList(0, 3)));
	}

	@Test
	public void duplicatePhysicalCopiesDoNotOverflowTheCompletedLoadoutRow()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(200, rangedStats(GearSlot.CAPE, 20));
		stats.put(201, rangedStats(GearSlot.NECK, 20));
		stats.put(202, rangedStats(GearSlot.HANDS, 20));
		stats.put(203, rangedStats(GearSlot.FEET, 20));
		stats.put(301, meleeStats(GearSlot.HEAD, 5));
		stats.put(302, meleeStats(GearSlot.BODY, 5));
		stats.put(303, meleeStats(GearSlot.LEGS, 5));
		stats.put(304, meleeStats(GearSlot.WEAPON, 5));

		List<BankPreviewItem> logical = GearItemSorter.layout(Arrays.asList(
			item(25865, "Bow of faerdhinen"),
			item(23971, "Crystal helm"),
			itemCopies(23975, "Crystal body", 1, 1, 1),
			item(23979, "Crystal legs"),
			item(200, "Ranged cape"), item(201, "Ranged necklace"),
			item(202, "Ranged gloves"), item(203, "Ranged boots"),
			item(301, "Early helm"), item(302, "Early body"),
			item(303, "Early legs"), item(304, "Early weapon")
		), itemId -> {
			Optional<GearStats> known = combatStats(itemId);
			return known.isPresent() ? known : Optional.ofNullable(stats.get(itemId));
		});

		List<BankPreviewItem> physical = new BankCategoryPreview(
			BankPresets.IRONMAN.getCategory("combat-gear"), logical).getItems();
		assertEquals("Ranged gloves", physical.get(8).getDisplayName());
	}

	@Test
	public void excludesZeroDurabilityBarrowsFromAnActivatedSet()
	{
		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(4716, "Dharok's helm"),
			item(4718, "Dharok's greataxe"),
			item(4720, "Dharok's platebody"),
			item(4902, "Dharok's platelegs 0"),
			item(4151, "Abyssal whip")
		));

		assertEquals("Dharok's platelegs 0", laidOut.get(laidOut.size() - 1).getDisplayName());
		assertFalse("zero durability must not complete the four-piece set",
			names(laidOut.subList(0, 4)).equals(Arrays.asList(
				"Dharok's greataxe", "Dharok's helm", "Dharok's platebody", "Dharok's platelegs 0")));
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

	/**
	 * Blood moon was the only Perilous Moons set with a curated tier and the Barrows melee brothers
	 * had none at all, so their untiered siblings scored below plain metal armour and drifted to the
	 * far end of the gear tab, away from the rest of their family.
	 */
	@Test
	public void tieredSetsOutrankPlainMetalArmourAsWholeFamilies()
	{
		List<String> perilousMoons = names(GearItemSorter.dense(Arrays.asList(
			item(1163, "Rune full helm"),
			item(29010, "Eclipse moon helm"),
			item(29019, "Blue moon helm"),
			item(29028, "Blood moon helm")
		), GearStatsSource.NONE));
		assertItemsBefore(perilousMoons, "Rune full helm", Arrays.asList(
			"Blood moon helm", "Blue moon helm", "Eclipse moon helm"));

		List<String> barrows = names(GearItemSorter.dense(Arrays.asList(
			item(1163, "Rune full helm"),
			item(4753, "Verac's helm"),
			item(4745, "Torag's helm"),
			item(4724, "Guthan's helm"),
			item(4716, "Dharok's helm")
		), GearStatsSource.NONE));
		assertItemsBefore(barrows, "Rune full helm", Arrays.asList(
			"Dharok's helm", "Guthan's helm", "Torag's helm", "Verac's helm"));
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			ItemCategory.GEAR.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}

	private static BankPreviewItem itemCopies(int itemId, String name, Integer... quantities)
	{
		int total = Arrays.stream(quantities).mapToInt(Integer::intValue).sum();
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			ItemCategory.GEAR.getDisplayLabel().toLowerCase(), Collections.emptySet(), null),
			total, false, Arrays.asList(quantities));
	}

	private static BankPreviewItem placeholder(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			ItemCategory.GEAR.getDisplayLabel().toLowerCase(), Collections.emptySet(), null),
			0, true, Collections.singletonList(0));
	}

	private static Optional<GearStats> combatStats(int itemId)
	{
		switch (itemId)
		{
			case 23971:
				return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 30, 0, 0, 0, 80));
			case 23975:
			case 10551:
			case 29022:
				return Optional.of(new GearStats(GearSlot.BODY, 0, 0, 0, 0,
					itemId == 23975 ? 30 : 0, itemId == 23975 ? 0 : 4, 0, 0, 80));
			case 23979:
				return Optional.of(new GearStats(GearSlot.LEGS, 0, 0, 0, 0, 30, 0, 0, 0, 80));
			case 29025:
			case 29052:
				return Optional.of(new GearStats(GearSlot.LEGS, 0, 0, 0, 0, 0, 4, 0, 0, 80));
			case 25865:
				return Optional.of(new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 128, 0, 106, 0, 0));
			case 27374:
				return Optional.of(new GearStats(GearSlot.CAPE, 0, 0, 0, 0, 8, 0, 2, 0, 10));
			case 24271:
			case 29028:
				return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 6, 0, 0, 80));
			case 4151:
			case 28997:
				return Optional.of(new GearStats(GearSlot.WEAPON, 82, 82, 82, 0, 0, 82, 0, 0, 0));
			case 6570:
				return Optional.of(new GearStats(GearSlot.CAPE, 1, 1, 1, 0, 0, 4, 0, 2, 30));
			case 6585:
				return Optional.of(new GearStats(GearSlot.NECK, 10, 10, 10, 10, 10, 8, 0, 3, 30));
			case 7462:
				return Optional.of(new GearStats(GearSlot.HANDS, 12, 12, 12, 6, 6, 12, 0, 0, 50));
			default:
				return Optional.empty();
		}
	}

	private static GearStats meleeStats(GearSlot slot, int strength)
	{
		return new GearStats(slot, strength, 0, 0, 0, 0, strength, 0, 0, strength);
	}

	private static GearStats rangedStats(GearSlot slot, int strength)
	{
		return new GearStats(slot, 0, 0, 0, 0, strength, 0, strength, 0, strength);
	}

	private static GearStats magicStats(GearSlot slot, int attack)
	{
		return new GearStats(slot, 0, 0, 0, attack, 0, 0, 0, 0, attack);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}

	private static void assertContiguous(List<BankPreviewItem> items, List<String> expectedNames)
	{
		List<String> actualNames = names(items);
		int first = actualNames.size();
		int last = -1;
		for (String expectedName : expectedNames)
		{
			int index = actualNames.indexOf(expectedName);
			assertTrue("missing " + expectedName + " from " + actualNames, index >= 0);
			first = Math.min(first, index);
			last = Math.max(last, index);
		}
		assertEquals("family was split: " + actualNames, expectedNames.size(), last - first + 1);
	}

	private static void assertItemsBefore(List<String> actualNames, String boundary,
		List<String> expectedNames)
	{
		int boundaryIndex = actualNames.indexOf(boundary);
		assertTrue("missing " + boundary + " from " + actualNames, boundaryIndex >= 0);
		for (String expectedName : expectedNames)
		{
			int index = actualNames.indexOf(expectedName);
			assertTrue("expected " + expectedName + " before " + boundary + " in " + actualNames,
				index >= 0 && index < boundaryIndex);
		}
	}

	private static void assertContiguousIds(List<BankPreviewItem> items, List<Integer> expectedIds)
	{
		List<Integer> actualIds = items.stream()
			.map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
		int first = actualIds.size();
		int last = -1;
		for (int expectedId : expectedIds)
		{
			int index = actualIds.indexOf(expectedId);
			assertTrue("missing " + expectedId + " from " + actualIds, index >= 0);
			first = Math.min(first, index);
			last = Math.max(last, index);
		}
		assertEquals("family was split: " + actualIds, expectedIds.size(), last - first + 1);
		assertEquals("family order was wrong: " + actualIds,
			expectedIds, actualIds.subList(first, last + 1));
	}
}

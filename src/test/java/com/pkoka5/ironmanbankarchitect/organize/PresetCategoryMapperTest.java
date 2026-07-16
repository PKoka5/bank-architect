package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemRegistry;
import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class PresetCategoryMapperTest
{
	@Test
	public void ironmanMapsAllGearIntoSingleCombatGearCategory()
	{
		BankCategory category = PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.GEAR, "Abyssal whip"));

		assertEquals("combat-gear", category.getKey());
	}

	@Test
	public void ironmanKeepsHerbSeedsWithHerbloreAndRoutesOtherFarmingSeparately()
	{
		assertEquals("herblore", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.HERBLORE, "Clean irit")).getKey());
		CatalogItem herbSeed = new CatalogItem(5297, "Irit seed", ItemCategory.FARMING,
			"herb-seed", Collections.emptySet(), null);
		assertEquals("herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, herbSeed).getKey());
		assertEquals("seeds-farming", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.FARMING, "Willow seed")).getKey());
	}

	@Test
	public void ironmanMapsToolsIntoSkillingToolsTab()
	{
		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.TOOL, "Dragon pickaxe")).getKey());
		assertEquals("resources", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.SKILLING, "Iron ore")).getKey());
	}

	@Test
	public void ironmanKeepsValuableUniqueComponentsOutOfCleanup()
	{
		assertEquals("slayer-boss-loot", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.UNIQUE, "Pegasian crystal")).getKey());
	}

	@Test
	public void ironmanRoutesActiveCluesAndCosmeticsToTheClueTab()
	{
		CatalogItem active = new CatalogItem(2722, "Clue scroll (hard)", ItemCategory.CLUE,
			"treasure-trail", Collections.emptySet(), null);
		CatalogItem cosmetic = new CatalogItem(12345, "Clue ornament kit", ItemCategory.CLUE,
			"cosmetic", Collections.emptySet(), null);

		assertEquals("clues-cosmetics",
			PresetCategoryMapper.map(BankPresets.IRONMAN, active).getKey());
		assertEquals("clues-cosmetics",
			PresetCategoryMapper.map(BankPresets.IRONMAN, cosmetic).getKey());
	}

	@Test
	public void ironmanRoutesReviewedGracefulAndRunePouchesToMainByExactId()
	{
		CatalogItem graceful = new CatalogItem(11850, "Graceful hood", ItemCategory.TOOL,
			"skilling-outfit", Collections.emptySet(), null);
		CatalogItem runePouch = new CatalogItem(12791, "Rune pouch", ItemCategory.RUNE,
			"rune-container", Collections.emptySet(), null);
		CatalogItem divineRunePouch = new CatalogItem(27281, "Divine rune pouch", ItemCategory.RUNE,
			"rune-container", Collections.emptySet(), null);
		CatalogItem recolouredGraceful = new CatalogItem(13579, "Graceful hood", ItemCategory.TOOL,
			"skilling-outfit", Collections.emptySet(), null);

		assertEquals("currency-utilities",
			PresetCategoryMapper.map(BankPresets.IRONMAN, graceful).getKey());
		assertEquals("currency-utilities",
			PresetCategoryMapper.map(BankPresets.IRONMAN, runePouch).getKey());
		assertEquals("currency-utilities",
			PresetCategoryMapper.map(BankPresets.IRONMAN, divineRunePouch).getKey());
		assertEquals("skilling-tools",
			PresetCategoryMapper.map(BankPresets.IRONMAN, recolouredGraceful).getKey());
	}

	@Test
	public void ironmanRoutesPartialDosesToHerbloreAndKeepsDoseFourInSupplies()
	{
		CatalogItem partial = new CatalogItem(139, "Prayer potion(3)", ItemCategory.POTION,
			"potion-dose-3", Collections.emptySet(), null);
		CatalogItem full = new CatalogItem(2434, "Prayer potion(4)", ItemCategory.POTION,
			"potion-dose-4", Collections.emptySet(), null);
		CatalogItem unknownPartial = new CatalogItem(999_999, "Unknown potion(3)", ItemCategory.POTION,
			"potion-dose-3", Collections.emptySet(), null);
		CatalogItem barbarianMix = new CatalogItem(11429, "Superattack mix(2)", ItemCategory.POTION,
			"potion-dose-2", Collections.emptySet(), null);

		assertEquals("herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, partial).getKey());
		assertEquals("potions-food", PresetCategoryMapper.map(BankPresets.IRONMAN, full).getKey());
		assertEquals("herblore",
			PresetCategoryMapper.map(BankPresets.IRONMAN, unknownPartial).getKey());
		assertEquals("herblore",
			PresetCategoryMapper.map(BankPresets.IRONMAN, barbarianMix).getKey());
	}

	@Test
	public void ironmanIncludesLegacyPartialDoseItemsInHerblorePrep()
	{
		CatalogItem legacy = new CatalogItem(1, "Super attack (3)", ItemCategory.POTION,
			"dose-3", Collections.emptySet(), null);

		assertEquals("herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, legacy).getKey());
	}

	@Test
	public void pvmSeparatesRunesIntoMagicGear()
	{
		BankCategory category = PresetCategoryMapper.map(BankPresets.PVM,
			item(ItemCategory.RUNE, "Blood rune"));

		assertEquals("magic-gear", category.getKey());
		assertEquals(BankCategorySortMode.GENERIC, category.getSortMode());
	}

	@Test
	public void everyCombatPresetKeepsCanonicalPartialDosesInItsSupplyCategory()
	{
		CatalogItem prayerThree = new CatalogItem(139, "Prayer potion(3)", ItemCategory.POTION,
			"potion-dose-3", Collections.emptySet(), null);

		assertEquals("herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, prayerThree).getKey());
		assertEquals("potions-food", PresetCategoryMapper.map(BankPresets.MAIN, prayerThree).getKey());
		assertEquals("potions-food", PresetCategoryMapper.map(BankPresets.PVM, prayerThree).getKey());
		assertEquals("food-potions", PresetCategoryMapper.map(BankPresets.PVP, prayerThree).getKey());
	}

	@Test
	public void ironmanRoutesRunecraftingFocusToToolsAndRunesToMain()
	{
		CatalogItem talisman = new CatalogItem(1438, "Air talisman", ItemCategory.RUNE,
			"runecrafting-focus", Collections.emptySet(), null);
		CatalogItem rune = new CatalogItem(556, "Air rune", ItemCategory.RUNE,
			"rune", Collections.emptySet(), null);

		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN, talisman).getKey());
		assertEquals("currency-utilities", PresetCategoryMapper.map(BankPresets.IRONMAN, rune).getKey());
	}

	@Test
	public void ironmanReroutesExactResourceRunecraftingAndActivityRewardIds()
	{
		for (int itemId : new int[] {7936, 24704, 32083, 32085})
		{
			assertEquals("resources", PresetCategoryMapper.map(BankPresets.IRONMAN,
				item(itemId, ItemCategory.RUNE, "Exact resource")).getKey());
		}
		for (int itemId : new int[] {5509, 5510, 5511, 5512, 5513, 5514, 5515, 26784, 26786, 5521})
		{
			assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
				item(itemId, ItemCategory.RUNE, "RC tool")).getKey());
		}
		for (int itemId : new int[] {6183, 6529, 6306, 12012, 25527, 21555})
		{
			assertEquals("clues-cosmetics", PresetCategoryMapper.map(BankPresets.IRONMAN,
				item(itemId, ItemCategory.CURRENCY, "Activity reward")).getKey());
		}
	}

	@Test
	public void ironmanMovesSoulBearerToToolsWhileChiselAndSpadeStayMain()
	{
		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(19634, ItemCategory.UNKNOWN, "Soul bearer")).getKey());
		assertEquals("currency-utilities", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(1755, ItemCategory.TOOL, "Chisel")).getKey());
		assertEquals("currency-utilities", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(952, ItemCategory.TOOL, "Spade")).getKey());
		assertEquals("junk-review", PresetCategoryMapper.map(BankPresets.MAIN,
			item(19634, ItemCategory.UNKNOWN, "Soul bearer")).getKey());
		assertEquals("loot-clues-storage", PresetCategoryMapper.map(BankPresets.SKILLER,
			item(19634, ItemCategory.UNKNOWN, "Soul bearer")).getKey());
	}

	@Test
	public void ironmanHonorsReviewedSpreadsheetTabChoicesByExactId()
	{
		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(805, ItemCategory.GEAR, "Rune thrownaxe")).getKey());
		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(25781, ItemCategory.SKILLING, "Ash sanctifier")).getKey());
		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(13392, ItemCategory.TELEPORT, "Xeric's talisman (inert)")).getKey());
		assertEquals("slayer-boss-loot", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(1201, ItemCategory.GEAR, "Rune kiteshield")).getKey());
		assertEquals("storage-cleanup", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(1588, ItemCategory.GEAR, "Grip's keyring")).getKey());

		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(9433, ItemCategory.GEAR, "Bolt pouch")).getKey());
		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(23037, ItemCategory.GEAR, "Boots of stone")).getKey());
		assertEquals("storage-cleanup", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(762, ItemCategory.CLEANUP, "Falador shield")).getKey());
	}

	@Test
	public void vettedCanonicalCorrectionBatchRoutesAllSixtyFiveIdsToTheirIronmanTabs()
	{
		assertRegistryItemsRoute("storage-cleanup",
			1, 14, 16, 74, 75, 286, 287, 288, 295, 686, 762, 9054, 9055, 9056,
			9057, 9058, 9059, 26567);
		assertRegistryItemsRoute("clues-cosmetics",
			626, 628, 630, 632, 634, 636, 638, 640, 642, 644, 646, 648, 650, 652,
			654, 656, 658, 660, 662, 664,
			19958, 19961, 19964, 19967, 19970, 19973, 19976, 19979, 19982, 19985);
		assertRegistryItemsRoute("skilling-tools",
			88, 1005, 20713, 25434, 25436, 25438, 25440, 25597, 27031, 28172, 28176);
		assertRegistryItemsRoute("herblore", 243);
		assertRegistryItemsRoute("combat-gear", 10, 805, 20714, 25574, 30064);
	}

	@Test
	public void ironmanRoutesEmptyElementalTomesToLootAndChargedTomesToGear()
	{
		for (int itemId : new int[] {
			ItemID.TOME_OF_FIRE_UNCHARGED,
			ItemID.TOME_OF_WATER_UNCHARGED,
			ItemID.TOME_OF_EARTH_UNCHARGED
		})
		{
			assertEquals("slayer-boss-loot", PresetCategoryMapper.map(BankPresets.IRONMAN,
				item(itemId, ItemCategory.GEAR, "Empty elemental tome")).getKey());
		}

		for (int itemId : new int[] {
			ItemID.TOME_OF_FIRE,
			ItemID.TOME_OF_WATER,
			ItemID.TOME_OF_EARTH
		})
		{
			assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN,
				item(itemId, ItemCategory.GEAR, "Charged elemental tome")).getKey());
		}

		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.MAIN,
			item(ItemID.TOME_OF_FIRE_UNCHARGED, ItemCategory.GEAR, "Tome of fire (empty)")).getKey());
		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(27358, ItemCategory.GEAR, "BR Tome of fire")).getKey());
	}

	@Test
	public void auditBatchSevenRoutesRestrictedVariantsWithoutMovingFunctionalControls()
	{
		assertRegistryItemsRoute("storage-cleanup",
			4045, 13666, 20390, 20430, 20527, 20586, 23533, 23628, 23650, 23831,
			23858, 24534, 25087, 25102, 25104, 26500, 26549, 27178, 28705, 30361,
			30363, 30453, 30461, 31174, 33239, 33241);
		assertRegistryItemsRoute("slayer-boss-loot", 12783, 20716);
		assertRegistryItemsRoute("clues-cosmetics", 24207, 24209, 24211, 24213, 24215, 24520);

		assertRegistryItemsRoute("currency-utilities",
			10934, 10935, 10936, 10942, 10943, 10944, 12791);
		assertRegistryItemsRoute("potions-food", 30616, 30619);
	}

	@Test
	public void questToolPilotSeparatesRepeatableUtilitiesFromQuestReview()
	{
		assertRegistryItemsRoute("storage-cleanup",
			602, 681, 1804, 1805, 1808, 1809, 1810, 2946, 2949, 2951, 3107,
			3696, 3697, 3719, 4200, 4415, 4444, 5586, 5587, 5601, 5605, 6712,
			6747, 7001, 7410, 7807, 9020, 9103, 26952, 28413, 28414, 28415,
			28964, 28965, 28966, 28967, 30320, 30989, 30990, 4021);
		assertRegistryItemsRoute("clues-cosmetics", 6786, 6787, 7917);
		assertRegistryItemsRoute("skilling-tools",
			4, 88, 552, 775, 776, 1456, 1506, 1580, 1585, 2871, 2872, 2873,
			2874, 2957, 2958, 3157, 3159, 4020, 4023, 4031, 4446, 4567,
			4657, 6465, 6544, 7409, 7534, 7535, 7649, 9064, 9065, 9625, 10491,
			11323, 20722, 22435, 24673, 24674, 24675, 24676, 24678, 24680,
			24942, 28575, 28577, 31398, 31986);
	}

	@Test
	public void questFarmingAuditRoutesOnlyVerifiedRepeatableRewardsOutsideCleanup()
	{
		assertRegistryItemsRoute("storage-cleanup",
			735, 736, 4205, 4206, 4486, 6112, 6453, 6454, 6455, 6456, 6457,
			6458, 6459, 6460, 6464, 6468, 6710, 9932, 23802, 23808, 23810);
		assertRegistryItemsRoute("clues-cosmetics", 7950);
		assertRegistryItemsRoute("skilling-tools", 9626);
	}

	@Test
	public void questPotionsFoodAuditRoutesConfirmedNonConsumablesByFunction()
	{
		assertRegistryItemsRoute("storage-cleanup",
			5591, 29539, 33797, 10841, 28351, 28355, 29542, 29551, 22407,
			739, 756, 7542, 30950, 6766, 33769, 28425, 33772, 29545, 26959,
			30317, 29544, 11154, 29925, 1501, 524, 522, 525, 523, 29540,
			23818, 7942, 7943, 337, 28353, 29548, 23806, 3152, 3154, 21394,
			3155, 3156, 77, 29550, 2395, 22009, 22010, 22011, 22012, 22013,
			22014, 22015, 22016, 22017, 22018, 22019, 22020, 22021, 22022,
			22023, 22024, 22025, 22026, 22027, 22028, 22029, 22030, 22031,
			22032, 28350, 28352, 1486, 2394, 22409, 28382, 21531, 29547,
			29546, 3742, 22589, 28354, 29541, 22096, 28443, 2882, 1552,
			280, 281, 282, 283, 25813, 33820, 4836, 28383, 28388, 29543,
			26904, 7579, 25812, 29898, 29899, 33771, 9656, 9657, 9658,
			3265, 28386, 28387, 33770, 29928);
		assertRegistryItemsRoute("currency-utilities", 3691, 6125, 6126, 6127);
		assertRegistryItemsRoute("resources", 7528, 3422, 3424, 3426, 3428);
		assertRegistryItemsRoute("combat-gear", 732, 7645, 7646, 7647, 7648);
		assertRegistryItemsRoute("potions-food",
			3153, 7488, 7489, 7490, 7491, 7521, 7523, 7524, 7525, 7526,
			7568, 2343, 30985, 7509, 7510, 4417, 4419, 4421, 4423, 6714,
			2149, 22081, 9021, 9022, 9023, 9024, 4610, 7480, 7481, 7482,
			7483, 2379, 3408, 3410, 3412, 3414, 3416, 3417, 3418, 3419,
			11204, 7479, 7492, 7493, 7494, 7495);
	}

	@Test
	public void reviewedSpreadsheetChoicesControlledByTheMapperRemainStableAgainstTheRealRegistry()
	{
		Map<Integer, String> expected = new LinkedHashMap<>();
		for (int itemId : new int[] {10888, 9433, 8850, 10551, 23037}) expected.put(itemId, "combat-gear");
		expected.put(1588, "storage-cleanup");
		expected.put(805, "combat-gear");
		for (int itemId : new int[] {9419, 27027, 27025, 1923, 13392, 13116, 6664, 25781})
		{
			expected.put(itemId, "skilling-tools");
		}
		for (int itemId : new int[] {21171, 21166, 6707, 1755, 27281, 772, 11860, 11852, 11858,
			11856, 21153, 19564, 11105, 11870, 11866, 13109})
		{
			expected.put(itemId, "currency-utilities");
		}
		expected.put(1201, "slayer-boss-loot");

		// Hammer (2347), the 33rd PDF choice, is intentionally handled later by
		// IronmanQuickToolSelector and has its own full-preview regression tests.
		assertEquals("the mapper controls 32 of the 33 reviewed PDF decisions", 32, expected.size());
		for (Map.Entry<Integer, String> choice : expected.entrySet())
		{
			CatalogItem item = ResourceItemRegistry.INSTANCE.findById(choice.getKey()).get();
			assertEquals(item.getDisplayName(), choice.getValue(),
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	@Test
	public void ironmanKeepsReviewedGrabItemsAndAchievementRewardsOnMain()
	{
		for (CatalogItem mainItem : Arrays.asList(
			item(12791, ItemCategory.RUNE, "Rune pouch"),
			item(27281, ItemCategory.RUNE, "Divine rune pouch"),
			item(27509, ItemCategory.RUNE, "Divine rune pouch (l)"),
			item(995, ItemCategory.CURRENCY, "Coins"),
			item(11849, ItemCategory.CURRENCY, "Mark of grace"),
			item(24711, ItemCategory.UNKNOWN, "Hallowed mark"),
			item(25926, ItemCategory.GEAR, "Ghommal's hilt 4"),
			item(22941, ItemCategory.GEAR, "Rada's blessing 4")))
		{
			assertEquals(mainItem.getDisplayName(), "currency-utilities",
				PresetCategoryMapper.map(BankPresets.IRONMAN, mainItem).getKey());
		}
	}

	@Test
	public void exactIronmanReroutesDoNotChangeOtherPresetRouting()
	{
		CatalogItem essence = item(7936, ItemCategory.RUNE, "Pure essence");
		CatalogItem coupon = item(32083, ItemCategory.SKILLING, "Sawmill coupon (wood plank)");

		assertEquals("teleports-runes", PresetCategoryMapper.map(BankPresets.MAIN, essence).getKey());
		assertEquals("magic-gear", PresetCategoryMapper.map(BankPresets.PVM, essence).getKey());
		assertEquals("teleports-runes", PresetCategoryMapper.map(BankPresets.SKILLER, essence).getKey());
		assertEquals("skilling-supplies", PresetCategoryMapper.map(BankPresets.MAIN, coupon).getKey());
	}

	@Test
	public void ironmanMainCombinesRecurringUtilitiesAndAchievementRewardsWithCurrency()
	{
		CatalogItem houseTab = new CatalogItem(8013, "Teleport to house", ItemCategory.TELEPORT,
			"teleport", Collections.emptySet(), null);
		CatalogItem diaryBody = new CatalogItem(13106, "Varrock armour 3", ItemCategory.GEAR,
			"body", Collections.emptySet(), null);
		CatalogItem normalBody = new CatalogItem(11832, "Bandos chestplate", ItemCategory.GEAR,
			"body", Collections.emptySet(), null);

		assertEquals("currency-utilities", PresetCategoryMapper.map(BankPresets.IRONMAN, houseTab).getKey());
		assertEquals("currency-utilities", PresetCategoryMapper.map(BankPresets.IRONMAN, diaryBody).getKey());
		assertEquals("combat-gear", PresetCategoryMapper.map(BankPresets.IRONMAN, normalBody).getKey());
	}

	@Test
	public void skillerSplitWorkflowStaysGenericUntilCrossTabRoutingIsDesigned()
	{
		BankCategory farming = PresetCategoryMapper.map(BankPresets.SKILLER,
			item(ItemCategory.FARMING, "Irit seed"));
		BankCategory herblore = PresetCategoryMapper.map(BankPresets.SKILLER,
			item(ItemCategory.HERBLORE, "Clean irit"));

		assertEquals("farming", farming.getKey());
		assertEquals(BankCategorySortMode.GENERIC, farming.getSortMode());
		assertEquals("herblore-materials", herblore.getKey());
		assertEquals(BankCategorySortMode.GENERIC, herblore.getSortMode());
	}

	@Test
	public void skillerRoutesFoodToCookingAndTruePotionsToHerbloreMaterials()
	{
		CatalogItem curatedFood = new CatalogItem(385, "Shark", ItemCategory.POTION,
			"legacy-supplies", Collections.emptySet(), null);
		CatalogItem classifiedFood = new CatalogItem(999_999, "New cooked fish", ItemCategory.POTION,
			"food", Collections.emptySet(), null);
		CatalogItem potion = new CatalogItem(2434, "Prayer potion(4)", ItemCategory.POTION,
			"potion-dose-4", Collections.emptySet(), null);

		assertEquals("fishing-cooking",
			PresetCategoryMapper.map(BankPresets.SKILLER, curatedFood).getKey());
		assertEquals("fishing-cooking",
			PresetCategoryMapper.map(BankPresets.SKILLER, classifiedFood).getKey());
		assertEquals("herblore-materials",
			PresetCategoryMapper.map(BankPresets.SKILLER, potion).getKey());
	}

	@Test
	public void everyPresetKeepsUniqueAndClueItemsOutOfGenericJunkFallbacks()
	{
		assertEquals("boss-slayer-loot", PresetCategoryMapper.map(BankPresets.MAIN,
			item(ItemCategory.UNIQUE, "Pegasian crystal")).getKey());
		assertEquals("clues-collection-log", PresetCategoryMapper.map(BankPresets.MAIN,
			item(ItemCategory.CLUE, "Clue scroll (hard)")).getKey());
		assertEquals("loot-drops", PresetCategoryMapper.map(BankPresets.PVM,
			item(ItemCategory.UNIQUE, "Burnt page")).getKey());
		assertEquals("loot-keys-review", PresetCategoryMapper.map(BankPresets.PVP,
			item(ItemCategory.CLUE, "Clue scroll (elite)")).getKey());
		assertEquals("loot-clues-storage", PresetCategoryMapper.map(BankPresets.SKILLER,
			item(ItemCategory.UNIQUE, "Crystal weapon seed")).getKey());
	}

	private static CatalogItem item(ItemCategory category, String name)
	{
		return new CatalogItem(1, name, category, "test", Collections.emptySet(), null);
	}

	private static CatalogItem item(int itemId, ItemCategory category, String name)
	{
		return new CatalogItem(itemId, name, category, "test", Collections.emptySet(), null);
	}

	private static void assertRegistryItemsRoute(String expectedCategoryKey, int... itemIds)
	{
		for (int itemId : itemIds)
		{
			CatalogItem item = ResourceItemRegistry.INSTANCE.findById(itemId).get();
			assertEquals(item.getDisplayName(), expectedCategoryKey,
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}
}

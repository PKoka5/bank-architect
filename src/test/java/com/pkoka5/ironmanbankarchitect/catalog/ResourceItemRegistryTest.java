package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class ResourceItemRegistryTest
{
	@Test
	public void explicitGearCategoriesAreNotClobberedByCleanupNameRules()
	{
		// These names contain cleanup-sounding words ("robe top", "helm") but the
		// registry explicitly labels them GEAR, and that label must win.
		assertCategory(4091, "Mystic robe top", ItemCategory.GEAR);
		assertCategory(4712, "Ahrim's robetop", ItemCategory.GEAR);
		assertCategory(11864, "Slayer helmet", ItemCategory.GEAR);
	}

	@Test
	public void rawFishStaysASkillingResourceInsteadOfFood()
	{
		assertCategory(383, "Raw shark", ItemCategory.SKILLING);
		assertCategory(371, "Raw swordfish", ItemCategory.SKILLING);
		assertCategory(373, "Swordfish", ItemCategory.POTION);
	}

	@Test
	public void curatedFoodIdsOverrideUnreliableGeneratedCategories()
	{
		assertCategory(6705, "Potato with cheese", ItemCategory.POTION);
		assertCategory(7058, "Mushroom potato", ItemCategory.POTION);
		assertCategory(7946, "Monkfish", ItemCategory.POTION);
		assertCategory(29143, "Cooked moonlight antelope", ItemCategory.POTION);
		assertCategory(29128, "Cooked wild kebbit", ItemCategory.POTION);
		assertCategory(31556, "Swordtip squid", ItemCategory.POTION);
		assertCategory(31564, "Jumbo squid", ItemCategory.POTION);
		assertCategory(32312, "Giant krill", ItemCategory.POTION);
		assertCategory(32320, "Haddock", ItemCategory.POTION);
		assertCategory(32328, "Yellowfin", ItemCategory.POTION);
		assertCategory(32352, "Marlin", ItemCategory.POTION);
		assertSubcategory(29143, "food");
	}

	@Test
	public void productionComponentsDoNotBecomeCombatGearFromPartialNames()
	{
		assertCategory(43, "Adamant arrowtips", ItemCategory.SKILLING);
		assertCategory(52, "Arrow shaft", ItemCategory.SKILLING);
		assertCategory(1595, "Amulet mould", ItemCategory.TOOL);
		assertCategory(11065, "Bracelet mould", ItemCategory.TOOL);
	}

	@Test
	public void valuableUpgradeComponentsAreNotFarmingOrCleanup()
	{
		assertCategory(4207, "Crystal weapon seed", ItemCategory.UNIQUE);
		assertCategory(25859, "Enhanced crystal weapon seed", ItemCategory.UNIQUE);
		assertCategory(13229, "Pegasian crystal", ItemCategory.UNIQUE);
	}

	@Test
	public void unlabelledHighValueEquipmentIsRecognizedByEquipmentType()
	{
		assertCategory(11832, "Bandos chestplate", ItemCategory.GEAR);
		assertSubcategory(11832, "body");
	}

	@Test
	public void runeGearIsRefinedToGearWhileActualRunesStayRunes()
	{
		assertCategory(1333, "Rune scimitar", ItemCategory.GEAR);
		assertCategory(811, "Rune dart", ItemCategory.GEAR);
		assertCategory(554, "Fire rune", ItemCategory.RUNE);
		assertCategory(556, "Air rune", ItemCategory.RUNE);
		assertCategory(7936, "Pure essence", ItemCategory.RUNE);
	}

	@Test
	public void explicitTeleportCategoryIsPreserved()
	{
		assertCategory(8013, "Teleport to house", ItemCategory.TELEPORT);
	}

	@Test
	public void teleportJewelleryOverridesUnreliableGearAndPotionLabels()
	{
		// The registry generator labelled charged jewellery as GEAR or POTION
		// (charge suffixes like "(4)" were read as potion doses).
		assertCategory(2552, "Ring of dueling(8)", ItemCategory.TELEPORT);
		assertCategory(1712, "Amulet of glory(4)", ItemCategory.TELEPORT);
		assertCategory(3853, "Games necklace(8)", ItemCategory.TELEPORT);
	}

	@Test
	public void itemsWithoutExplicitCategoryStillRefineByName()
	{
		// TSV category UNKNOWN: quest junk should land in CLEANUP for manual review.
		assertCategory(3, "Nulodion's notes", ItemCategory.CLEANUP);
	}

	@Test
	public void runeThrownWeaponsAreGearNotRunes()
	{
		assertCategory(805, "Rune thrownaxe", ItemCategory.GEAR);
		assertCategory(868, "Rune knife", ItemCategory.GEAR);
	}

	@Test
	public void mislabelledWeaponsAreGearNotSkilling()
	{
		assertCategory(13576, "Dragon warhammer", ItemCategory.GEAR);
		assertCategory(4747, "Torag's hammers", ItemCategory.GEAR);
	}

	@Test
	public void skillingToolsAndOutfitsGetTheirOwnToolCategory()
	{
		assertCategory(1359, "Rune axe", ItemCategory.TOOL);
		assertCategory(11920, "Dragon pickaxe", ItemCategory.TOOL);
		assertCategory(2347, "Hammer", ItemCategory.TOOL);
		assertCategory(11854, "Graceful top", ItemCategory.TOOL);
		assertCategory(13259, "Angler top", ItemCategory.TOOL);
		assertCategory(11850, "Graceful hood", ItemCategory.TOOL);
		assertCategory(12013, "Prospector helmet", ItemCategory.TOOL);
		assertCategory(20708, "Pyromancer hood", ItemCategory.TOOL);
		assertCategory(13642, "Farmer's jacket", ItemCategory.TOOL);
		assertCategory(13646, "Farmer's strawhat", ItemCategory.TOOL);
		assertCategory(1411, "Farmer's fork", ItemCategory.CLEANUP);
	}

	@Test
	public void gearNameFragmentsDoNotCaptureToolsAndComponents()
	{
		assertCategory(8790, "Bolt of cloth", ItemCategory.SKILLING);
		assertCategory(9422, "Blurite limbs", ItemCategory.SKILLING);
		assertCategory(29311, "Hunter spear tips", ItemCategory.SKILLING);
		assertCategory(9416, "Mith grapple tip", ItemCategory.SKILLING);
		assertCategory(946, "Knife", ItemCategory.TOOL);
		assertCategory(5340, "Watering can(8)", ItemCategory.TOOL);
		assertCategory(567, "Unpowered orb", ItemCategory.SKILLING);
	}

	@Test
	public void clueScrollsHaveADedicatedCategory()
	{
		assertCategory(23182, "Clue scroll (beginner)", ItemCategory.CLUE);
		assertCategory(7249, "Clue scroll (hard)", ItemCategory.CLUE);
		assertCategory(7991, "Big swordfish", ItemCategory.CLUE);
	}

	@Test
	public void burntFishAndSpecialVialsDoNotFollowBroadFoodOrGlassNames()
	{
		assertCategory(375, "Burnt swordfish", ItemCategory.CLEANUP);
		assertCategory(22405, "Vial of blood", ItemCategory.CLEANUP);
		assertCategory(22446, "Vial of blood", ItemCategory.UNIQUE);
	}

	@Test
	public void unfinishedPotionsAreHerbloreInputs()
	{
		assertCategory(109, "Dwarf weed potion (unf)", ItemCategory.HERBLORE);
		assertCategory(3002, "Toadflax potion (unf)", ItemCategory.HERBLORE);
	}

	@Test
	public void completeHerbFamilyOverridesBadSourceCategories()
	{
		assertCategory(2998, "Toadflax", ItemCategory.HERBLORE);
		assertCategory(3000, "Snapdragon", ItemCategory.HERBLORE);
		assertCategory(2481, "Lantadyme", ItemCategory.HERBLORE);
	}

	@Test
	public void gemsAndGlassInputsAreResources()
	{
		assertCategory(1601, "Diamond", ItemCategory.SKILLING);
		assertCategory(1603, "Ruby", ItemCategory.SKILLING);
		assertCategory(1617, "Uncut diamond", ItemCategory.SKILLING);
		assertCategory(229, "Vial", ItemCategory.SKILLING);
		assertCategory(1783, "Bucket of sand", ItemCategory.SKILLING);
	}

	@Test
	public void runecraftingFociAreNotCleanup()
	{
		assertCategory(1438, "Air talisman", ItemCategory.RUNE);
		assertCategory(5527, "Air tiara", ItemCategory.RUNE);
	}

	@Test
	public void skillingEquipmentDoesNotPolluteCombatGear()
	{
		assertCategory(9805, "Firemaking cape(t)", ItemCategory.TOOL);
		assertCategory(9799, "Fishing cape(t)", ItemCategory.TOOL);
		assertCategory(25539, "Celestial ring (uncharged)", ItemCategory.TOOL);
		assertCategory(775, "Cooking gauntlets", ItemCategory.TOOL);
		assertCategory(776, "Goldsmith gauntlets", ItemCategory.TOOL);
	}

	@Test
	public void treasureTrailContainersJoinClues()
	{
		assertCategory(24361, "Scroll box (beginner)", ItemCategory.CLUE);
		assertCategory(24364, "Scroll box (hard)", ItemCategory.CLUE);
	}

	@Test
	public void reusableTravelAndRuneUtilitiesAvoidCleanup()
	{
		assertCategory(13660, "Chronicle", ItemCategory.TELEPORT);
		assertCategory(29275, "Perfected quetzal whistle", ItemCategory.TELEPORT);
		assertCategory(12791, "Rune pouch", ItemCategory.RUNE);
	}

	@Test
	public void effectBasedGearCanBeClassifiedWithoutVisibleStats()
	{
		assertCategory(25975, "Lightbearer", ItemCategory.GEAR);
		assertSubcategory(25975, "ring");
	}

	@Test
	public void commonToolsSecondariesAndResourcesAvoidCleanup()
	{
		assertCategory(10006, "Bird snare", ItemCategory.TOOL);
		assertCategory(10008, "Box trap", ItemCategory.TOOL);
		assertCategory(5075, "Bird nest", ItemCategory.HERBLORE);
		assertCategory(6693, "Crushed nest", ItemCategory.HERBLORE);
		assertCategory(23962, "Crystal shard", ItemCategory.SKILLING);
		assertCategory(954, "Rope", ItemCategory.SKILLING);
		assertCategory(31964, "Repair kit", ItemCategory.SKILLING);
		assertCategory(24711, "Hallowed mark", ItemCategory.CURRENCY);
	}

	@Test
	public void slayerToolFamilyStaysOutOfCombatAndCleanup()
	{
		assertCategory(4164, "Facemask", ItemCategory.TOOL);
		assertCategory(4166, "Earmuffs", ItemCategory.TOOL);
		assertCategory(4168, "Nose peg", ItemCategory.TOOL);
		assertCategory(6696, "Ice cooler", ItemCategory.TOOL);
	}

	@Test
	public void farmingAndPrayerInputsHaveStableFamilies()
	{
		assertCategory(5312, "Acorn", ItemCategory.FARMING);
		// Corrected by the Batch 2B canonical override: SKILLING/prayer-resource,
		// not FARMING produce. See canonicalSkillingPrayerResourceOverrides.
		assertCategory(5076, "Bird's egg", ItemCategory.SKILLING);
		assertSubcategory(5076, "prayer-resource");
		assertCategory(13496, "Ensouled bloodveld head", ItemCategory.SKILLING);
		assertCategory(13511, "Ensouled dragon head", ItemCategory.SKILLING);
	}

	@Test
	public void miningConstructionAndGatheringMaterialsAreResources()
	{
		assertCategory(22603, "Basalt", ItemCategory.SKILLING);
		assertCategory(21543, "Calcite", ItemCategory.SKILLING);
		assertCategory(21545, "Pyrophosphite", ItemCategory.SKILLING);
		assertCategory(22595, "Efh salt", ItemCategory.SKILLING);
		assertCategory(21562, "Unidentified small fossil", ItemCategory.SKILLING);
		assertCategory(5931, "Jute fibre", ItemCategory.SKILLING);
	}

	@Test
	public void clueCosmeticSuppliesJoinTheClueTab()
	{
		assertCategory(7329, "Red firelighter", ItemCategory.CLUE);
		assertCategory(1763, "Red dye", ItemCategory.CLUE);
	}

	@Test
	public void standardPotionDosesAreDistinguishedFromOtherNumberedItems()
	{
		assertSubcategory(143, "potion-dose-1");
		assertSubcategory(141, "potion-dose-2");
		assertSubcategory(139, "potion-dose-3");
		assertSubcategory(2434, "potion-dose-4");
		assertSubcategory(179, "potion-dose-1");
		assertSubcategory(177, "potion-dose-2");
		assertSubcategory(175, "potion-dose-3");
		assertSubcategory(2446, "potion-dose-4");
		assertSubcategory(6687, "potion-dose-3");
		assertSubcategory(21175, "teleport");
		assertSubcategory(5464, "produce-container");
		assertSubcategory(7484, "cooking-material");
	}

	@Test
	public void herbSeedsAreFarmingInputsRatherThanCleanHerbs()
	{
		assertCategory(5292, "Marrentill seed", ItemCategory.FARMING);
		assertCategory(5296, "Toadflax seed", ItemCategory.FARMING);
		assertCategory(5300, "Snapdragon seed", ItemCategory.FARMING);
		assertSubcategory(5300, "herb-seed");
	}

	@Test
	public void cookingToolsContainersAndSpicyStewSuppliesKeepDistinctRoles()
	{
		assertCategory(1887, "Cake tin", ItemCategory.TOOL);
		assertCategory(2313, "Pie dish", ItemCategory.SKILLING);
		assertCategory(2315, "Pie shell", ItemCategory.SKILLING);
		assertCategory(5464, "Cabbages(3)", ItemCategory.FARMING);
		assertCategory(5446, "Onions(4)", ItemCategory.FARMING);
		assertCategory(7484, "Orange spice (4)", ItemCategory.SKILLING);
		assertCategory(3434, "Sacred oil(2)", ItemCategory.SKILLING);
		assertCategory(1825, "Waterskin(3)", ItemCategory.TOOL);
	}

	@Test
	public void resourceContainersAndRunecraftingPouchesRouteToUtilities()
	{
		assertCategory(12019, "Coal bag", ItemCategory.TOOL);
		assertCategory(28136, "Forestry kit", ItemCategory.TOOL);
		assertCategory(25584, "Open fish barrel", ItemCategory.TOOL);
		assertCategory(24481, "Open gem bag", ItemCategory.TOOL);
		assertCategory(5509, "Small pouch", ItemCategory.RUNE);
		assertCategory(5510, "Medium pouch", ItemCategory.RUNE);
		assertCategory(1734, "Thread", ItemCategory.SKILLING);
		assertCategory(32085, "Sawmill coupon (oak plank)", ItemCategory.CURRENCY);
	}

	@Test
	public void obviousEquipmentDoesNotRemainInResources()
	{
		assertCategory(10551, "Fighter torso", ItemCategory.GEAR);
		assertCategory(10888, "Barrelchest anchor", ItemCategory.GEAR);
		assertCategory(29280, "Mixed hide top", ItemCategory.GEAR);
		assertCategory(7534, "Fishbowl helmet", ItemCategory.TOOL);
	}

	@Test
	public void exportDrivenUtilityAndWorkflowOverridesBeatBadSourceLabels()
	{
		assertCategory(5100, "Limpwurt seed", ItemCategory.FARMING);
		assertCategory(22879, "Snape grass seed", ItemCategory.FARMING);
		assertCategory(5343, "Seed dibber", ItemCategory.TOOL);
		assertCategory(24482, "Open seed box", ItemCategory.TOOL);
		assertCategory(24478, "Open herb sack", ItemCategory.TOOL);
		assertCategory(4544, "Bullseye lantern (unf)", ItemCategory.SKILLING);
		assertCategory(28924, "Sunfire splinters", ItemCategory.UNIQUE);
		assertCategory(1391, "Battlestaff", ItemCategory.SKILLING);
		assertCategory(1635, "Gold ring", ItemCategory.SKILLING);
		assertCategory(7918, "Bonesack", ItemCategory.CLUE);
		assertCategory(4084, "Sled", ItemCategory.CLEANUP);
		assertCategory(31331, "Mayor of catherby", ItemCategory.CLEANUP);
	}

	@Test
	public void teleportFunctionOverridesEquippableRingsAndAmulets()
	{
		// These charge/degrade across states but always function purely as
		// teleport devices; the generated registry read them as plain GEAR.
		assertCategory(11866, "Slayer ring (8)", ItemCategory.TELEPORT);
		assertSubcategory(11866, "teleport");
		assertCategory(6707, "Camulet", ItemCategory.TELEPORT);
		assertSubcategory(6707, "teleport");
		assertCategory(13110, "Wilderness sword 3", ItemCategory.TELEPORT);
		assertSubcategory(13110, "teleport");
		assertCategory(30638, "Giantsoul amulet", ItemCategory.TELEPORT);
		assertSubcategory(30638, "teleport");
	}

	@Test
	public void skillingToolsAndOutfitsWithNoNameKeywordsAreRecognized()
	{
		assertCategory(31043, "Fletching knife", ItemCategory.TOOL);
		assertSubcategory(31043, "skilling-utility");
		assertCategory(13353, "Gricoller's can", ItemCategory.TOOL);
		assertSubcategory(13353, "tool");
		assertCategory(1580, "Ice gloves", ItemCategory.TOOL);
		assertSubcategory(1580, "skilling-utility");
		assertCategory(25553, "Golden prospector legs", ItemCategory.TOOL);
		assertSubcategory(25553, "skilling-outfit");
		assertCategory(26852, "Robe top of the eye", ItemCategory.TOOL);
		assertSubcategory(26852, "skilling-outfit");
	}

	@Test
	public void uniqueWeaponsAndJewelleryMissedByGeneratedKeywordsAreGear()
	{
		// None of these names contain a combat keyword the generated registry
		// recognizes, so they were left in CLEANUP or SKILLING.
		assertCategory(29045, "Blood moon tassets", ItemCategory.GEAR);
		assertSubcategory(29045, "legs");
		assertCategory(29000, "Eclipse atlatl", ItemCategory.GEAR);
		assertSubcategory(29000, "weapon");
		assertCategory(24699, "Blisterwood flail", ItemCategory.GEAR);
		assertSubcategory(24699, "weapon");
		assertCategory(29589, "Emberlight", ItemCategory.GEAR);
		assertSubcategory(29589, "weapon");
		assertCategory(30955, "Arkan blade", ItemCategory.GEAR);
		assertSubcategory(30955, "weapon");
		assertCategory(19547, "Necklace of anguish", ItemCategory.GEAR);
		assertSubcategory(19547, "neck");
		assertCategory(12002, "Occult necklace", ItemCategory.GEAR);
		assertSubcategory(12002, "neck");
		assertCategory(13263, "Abyssal bludgeon", ItemCategory.GEAR);
		assertSubcategory(13263, "weapon");
		assertCategory(24271, "Neitiznot faceguard", ItemCategory.GEAR);
		assertSubcategory(24271, "head");
		assertCategory(4726, "Guthan's warspear", ItemCategory.GEAR);
		assertSubcategory(4726, "weapon");
		assertCategory(4755, "Verac's flail", ItemCategory.GEAR);
		assertSubcategory(4755, "weapon");
		assertCategory(4718, "Dharok's greataxe", ItemCategory.GEAR);
		assertSubcategory(4718, "weapon");
		assertCategory(28810, "Zombie axe", ItemCategory.GEAR);
		assertSubcategory(28810, "weapon");
	}

	@Test
	public void shayzienArmourTiersRemainEquipmentDespiteNumericSuffixes()
	{
		String[] roles = {"hands", "feet", "head", "legs", "body"};
		for (int itemId = 13357; itemId <= 13381; itemId++)
		{
			assertCategoryOnly(itemId, ItemCategory.GEAR);
			assertSubcategory(itemId, roles[(itemId - 13357) % roles.length]);
		}
	}

	@Test
	public void everyBallistaAssemblyStageStaysWithFletchingComponents()
	{
		for (int itemId : new int[]{19586, 19589, 19592, 19595, 19598, 19601, 19604, 19607, 19610})
		{
			assertCategoryOnly(itemId, ItemCategory.SKILLING);
			assertSubcategory(itemId, "ammo-component");
		}
	}

	@Test
	public void barrowsWeaponFamiliesStayGearAcrossEveryChargeState()
	{
		// Bounded family recognition: base name plus the 100/75/50/25/0 charge
		// suffixes only, still the same weapon at every degradation state.
		assertCategory(4718, "Dharok's greataxe", ItemCategory.GEAR);
		assertCategory(4886, "Dharok's greataxe 100", ItemCategory.GEAR);
		assertCategory(4887, "Dharok's greataxe 75", ItemCategory.GEAR);
		assertCategory(4888, "Dharok's greataxe 50", ItemCategory.GEAR);
		assertCategory(4889, "Dharok's greataxe 25", ItemCategory.GEAR);
		assertCategory(4890, "Dharok's greataxe 0", ItemCategory.GEAR);
		assertSubcategory(4886, "weapon");
		assertSubcategory(4887, "weapon");
		assertSubcategory(4888, "weapon");
		assertSubcategory(4889, "weapon");
		assertSubcategory(4890, "weapon");

		assertCategory(4726, "Guthan's warspear", ItemCategory.GEAR);
		assertCategory(4910, "Guthan's warspear 100", ItemCategory.GEAR);
		assertCategory(4911, "Guthan's warspear 75", ItemCategory.GEAR);
		assertCategory(4912, "Guthan's warspear 50", ItemCategory.GEAR);
		assertCategory(4913, "Guthan's warspear 25", ItemCategory.GEAR);
		assertCategory(4914, "Guthan's warspear 0", ItemCategory.GEAR);
		assertSubcategory(4910, "weapon");
		assertSubcategory(4911, "weapon");
		assertSubcategory(4912, "weapon");
		assertSubcategory(4913, "weapon");
		assertSubcategory(4914, "weapon");

		assertCategory(4755, "Verac's flail", ItemCategory.GEAR);
		assertCategory(4982, "Verac's flail 100", ItemCategory.GEAR);
		assertCategory(4983, "Verac's flail 75", ItemCategory.GEAR);
		assertCategory(4984, "Verac's flail 50", ItemCategory.GEAR);
		assertCategory(4985, "Verac's flail 25", ItemCategory.GEAR);
		assertCategory(4986, "Verac's flail 0", ItemCategory.GEAR);
		assertSubcategory(4982, "weapon");
		assertSubcategory(4983, "weapon");
		assertSubcategory(4984, "weapon");
		assertSubcategory(4985, "weapon");
		assertSubcategory(4986, "weapon");
	}

	@Test
	public void raimentsOfTheEyeOutfitIsFullyRecognizedWithoutTouchingTheAmulet()
	{
		assertCategory(26850, "Hat of the eye", ItemCategory.TOOL);
		assertCategory(26852, "Robe top of the eye", ItemCategory.TOOL);
		assertCategory(26854, "Robe bottoms of the eye", ItemCategory.TOOL);
		assertCategory(26856, "Boots of the eye", ItemCategory.TOOL);
		assertSubcategory(26850, "skilling-outfit");
		assertSubcategory(26852, "skilling-outfit");
		assertSubcategory(26854, "skilling-outfit");
		assertSubcategory(26856, "skilling-outfit");

		// One full color variant set, confirming the bounded prefix also
		// covers recolored copies of each garment.
		assertCategory(26858, "Hat of the eye (red)", ItemCategory.TOOL);
		assertCategory(26860, "Robe top of the eye (red)", ItemCategory.TOOL);
		assertCategory(26862, "Robe bottoms of the eye (red)", ItemCategory.TOOL);
		assertSubcategory(26858, "skilling-outfit");
		assertSubcategory(26860, "skilling-outfit");
		assertSubcategory(26862, "skilling-outfit");

		// Canonical green variant set.
		assertCategory(26864, "Hat of the eye (green)", ItemCategory.TOOL);
		assertCategory(26866, "Robe top of the eye (green)", ItemCategory.TOOL);
		assertCategory(26868, "Robe bottoms of the eye (green)", ItemCategory.TOOL);
		assertSubcategory(26864, "skilling-outfit");
		assertSubcategory(26866, "skilling-outfit");
		assertSubcategory(26868, "skilling-outfit");

		// Canonical blue variant set.
		assertCategory(26870, "Hat of the eye (blue)", ItemCategory.TOOL);
		assertCategory(26872, "Robe top of the eye (blue)", ItemCategory.TOOL);
		assertCategory(26874, "Robe bottoms of the eye (blue)", ItemCategory.TOOL);
		assertSubcategory(26870, "skilling-outfit");
		assertSubcategory(26872, "skilling-outfit");
		assertSubcategory(26874, "skilling-outfit");

		// Negative controls: Amulet of the eye shares the "of the eye" phrase but
		// not the "hat"/"robe top"/"robe bottoms"/"boots" prefix, so it must stay
		// GEAR and never be swept into the skilling-outfit rule.
		assertCategory(26914, "Amulet of the eye", ItemCategory.GEAR);
		assertCategory(26990, "Amulet of the eye", ItemCategory.GEAR);
		assertCategory(26992, "Amulet of the eye", ItemCategory.GEAR);
		assertCategory(26994, "Amulet of the eye", ItemCategory.GEAR);
	}

	@Test
	public void prefixLeakageIntoUnrelatedRecordsIsBlocked()
	{
		// Shares a display-name prefix with the real Wilderness sword rewards but is
		// an unrelated cert record; must not be swept into TELEPORT.
		assertCategory(3981, "Wilderness sword", ItemCategory.GEAR);

		// Shares a constant (OCCULT_NECKLACE_ORNAMENT) with the legitimate
		// "Occult necklace (or)" record but is a separate, unproven display name;
		// must not be swept into GEAR by a broad "occult necklace" prefix.
		assertCategory(19721, "Occult Necklace Ornament", ItemCategory.CLEANUP);
	}

	@Test
	public void negativeControlsFromThePriorCategoryBatchStayUnchanged()
	{
		assertCategory(10858, "Shadow sword", ItemCategory.GEAR);
		assertCategory(31906, "Bronze cannonball", ItemCategory.GEAR);
		assertSubcategory(31906, "ammo");
		assertCategory(22586, "Looting bag", ItemCategory.TOOL);
		assertCategory(1777, "Bow string", ItemCategory.SKILLING);
	}

	@Test
	public void canonicalWeaponOverridesCoverDualMacuahuitlAndLeafBladedFamily()
	{
		// The generated registry mislabels all four as HERBLORE/UNKNOWN; the exact
		// item IDs are pinned to GEAR/weapon regardless of that generated label.
		assertCategory(28997, "Dual macuahuitl", ItemCategory.GEAR);
		assertSubcategory(28997, "weapon");
		assertCategory(4158, "Leaf-bladed spear", ItemCategory.GEAR);
		assertSubcategory(4158, "weapon");
		assertCategory(11902, "Leaf-bladed sword", ItemCategory.GEAR);
		assertSubcategory(11902, "weapon");
		assertCategory(20727, "Leaf-bladed battleaxe", ItemCategory.GEAR);
		assertSubcategory(20727, "weapon");
	}

	@Test
	public void canonicalGearOverridesCoverAvaAndMasoriAssemblers()
	{
		assertCategory(10498, "Ava's attractor", ItemCategory.GEAR);
		assertSubcategory(10498, "gear");
		assertCategory(10499, "Ava's accumulator", ItemCategory.GEAR);
		assertSubcategory(10499, "gear");
		assertCategory(22109, "Ava's assembler", ItemCategory.GEAR);
		assertSubcategory(22109, "gear");
		assertCategory(24222, "Ava's assembler (l)", ItemCategory.GEAR);
		assertSubcategory(24222, "gear");
		assertCategory(27374, "Masori assembler", ItemCategory.GEAR);
		assertSubcategory(27374, "gear");
		assertCategory(27376, "Masori assembler (l)", ItemCategory.GEAR);
		assertSubcategory(27376, "gear");
	}

	@Test
	public void canonicalAmmoOverridesCoverBoltRackAndGodBlessings()
	{
		assertCategory(4740, "Bolt rack", ItemCategory.GEAR);
		assertSubcategory(4740, "ammo");
		assertCategory(20220, "Holy blessing", ItemCategory.GEAR);
		assertSubcategory(20220, "ammo");
		assertCategory(20223, "Unholy blessing", ItemCategory.GEAR);
		assertSubcategory(20223, "ammo");
		assertCategory(20226, "Peaceful blessing", ItemCategory.GEAR);
		assertSubcategory(20226, "ammo");
		assertCategory(20229, "Honourable blessing", ItemCategory.GEAR);
		assertSubcategory(20229, "ammo");
		assertCategory(20232, "War blessing", ItemCategory.GEAR);
		assertSubcategory(20232, "ammo");
		assertCategory(20235, "Ancient blessing", ItemCategory.GEAR);
		assertSubcategory(20235, "ammo");
	}

	@Test
	public void canonicalFeetOverridesCoverAvernicTreadsFamily()
	{
		assertCategory(31088, "Avernic treads", ItemCategory.GEAR);
		assertSubcategory(31088, "feet");
		assertCategory(31091, "Avernic treads (pr)", ItemCategory.GEAR);
		assertSubcategory(31091, "feet");
		assertCategory(31092, "Avernic treads (pe)", ItemCategory.GEAR);
		assertSubcategory(31092, "feet");
		assertCategory(31093, "Avernic treads (et)", ItemCategory.GEAR);
		assertSubcategory(31093, "feet");
		assertCategory(31094, "Avernic treads (pr)(pe)", ItemCategory.GEAR);
		assertSubcategory(31094, "feet");
		assertCategory(31095, "Avernic treads (pr)(et)", ItemCategory.GEAR);
		assertSubcategory(31095, "feet");
		assertCategory(31096, "Avernic treads (pe)(et)", ItemCategory.GEAR);
		assertSubcategory(31096, "feet");
		assertCategory(31097, "Avernic treads (max)", ItemCategory.GEAR);
		assertSubcategory(31097, "feet");
	}

	@Test
	public void canonicalWeaponOverrideCoversWarpedSceptre()
	{
		assertCategory(28583, "Warped sceptre (uncharged)", ItemCategory.GEAR);
		assertSubcategory(28583, "weapon");
		assertCategory(28585, "Warped sceptre", ItemCategory.GEAR);
		assertSubcategory(28585, "weapon");
	}

	@Test
	public void canonicalHerbloreSecondaryOverridesCoverDustsAndWine()
	{
		assertCategory(235, "Unicorn horn dust", ItemCategory.HERBLORE);
		assertSubcategory(235, "secondary");
		assertCategory(241, "Dragon scale dust", ItemCategory.HERBLORE);
		assertSubcategory(241, "secondary");
		assertCategory(245, "Wine of zamorak", ItemCategory.HERBLORE);
		assertSubcategory(245, "secondary");
	}

	@Test
	public void vettedQuestAndJunkCorrectionsUseOnlyCanonicalIds()
	{
		assertClassifications(
			new int[] {1, 14, 16, 74, 75, 286, 287, 288, 295, 762, 9054, 9055, 9056,
				9057, 9058, 9059, 26567},
			new String[] {"Toolkit", "Railing", "Magic whistle", "Khazard helmet",
				"Khazard armour", "Orange goblin mail", "Blue goblin mail", "Goblin mail",
				"Glarial's amulet", "Falador shield", "Red goblin mail", "Black goblin mail",
				"Yellow goblin mail", "Green goblin mail", "Purple goblin mail",
				"Pink goblin mail", "White goblin mail"},
			ItemCategory.CLEANUP, "quest-item");
		assertClassification(686, "Rusty sword", ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void canonicalGnomeClothingIsACompleteCosmeticFamily()
	{
		assertClassifications(
			new int[] {626, 628, 630, 632, 634, 636, 638, 640, 642, 644, 646, 648, 650,
				652, 654, 656, 658, 660, 662, 664},
			new String[] {"Pink boots", "Green boots", "Blue boots", "Cream boots",
				"Turquoise boots", "Pink robe top", "Green robe top", "Blue robe top",
				"Cream robe top", "Turquoise robe top", "Pink robe bottoms",
				"Green robe bottoms", "Blue robe bottoms", "Cream robe bottoms",
				"Turquoise robe bottoms", "Pink hat", "Green hat", "Blue hat", "Cream hat",
				"Turquoise hat"},
			ItemCategory.CLUE, "cosmetic");
	}

	@Test
	public void canonicalTuxedoClothingIsACompleteCosmeticFamily()
	{
		assertClassifications(
			new int[] {19958, 19961, 19964, 19967, 19970, 19973, 19976, 19979, 19982, 19985},
			new String[] {"Dark tuxedo jacket", "Dark tuxedo cuffs", "Dark trousers",
				"Dark tuxedo shoes", "Dark bow tie", "Light tuxedo jacket",
				"Light tuxedo cuffs", "Light trousers", "Light tuxedo shoes", "Light bow tie"},
			ItemCategory.CLUE, "cosmetic");
	}

	@Test
	public void canonicalSkillingUtilitiesAndMissingOutfitPiecesStayOutOfGear()
	{
		assertClassification(88, "Boots of lightness", ItemCategory.TOOL, "skilling-utility");
		assertClassification(1005, "White apron", ItemCategory.TOOL, "cooking-tool");
		assertClassifications(
			new int[] {20713, 25434, 25436, 25438, 25440, 25597, 27031, 28172, 28176},
			new String[] {"Pyromancer Gloves", "Zealot's robe top", "Zealot's robe bottom",
				"Zealot's helm", "Zealot's boots", "Spirit Angler Legs", "Smiths gloves (i)",
				"Forestry Lumberjack Legs", "Forestry Lumberjack Boots"},
			ItemCategory.TOOL, "skilling-outfit");
	}

	@Test
	public void canonicalBlueDragonScaleAndCombatUtilitiesUseSpecificSemantics()
	{
		assertClassification(243, "Blue dragon scale", ItemCategory.HERBLORE, "secondary");
		assertClassification(10, "Cannon barrels", ItemCategory.GEAR, "cannon-part");
		assertClassification(805, "Rune thrownaxe", ItemCategory.GEAR, "thrown-weapon");
		assertClassifications(
			new int[] {20714, 25574, 30064},
			new String[] {"Tome of fire", "Tome of water", "Tome of earth"},
			ItemCategory.GEAR, "magic-offhand");
	}

	@Test
	public void canonicalTeleportOverridesCoverSeedPodsAndPendantOfAtes()
	{
		assertCategory(9469, "Grand seed pod", ItemCategory.TELEPORT);
		assertSubcategory(9469, "teleport");
		assertCategory(19564, "Royal seed pod", ItemCategory.TELEPORT);
		assertSubcategory(19564, "teleport");
		assertCategory(29892, "Pendant of ates (inert)", ItemCategory.TELEPORT);
		assertSubcategory(29892, "teleport");
		assertCategory(29893, "Pendant of ates", ItemCategory.TELEPORT);
		assertSubcategory(29893, "teleport");
	}

	@Test
	public void canonicalTeleportContainerOverridesCoverMasterScrollBook()
	{
		assertCategory(21387, "Master scroll book (empty)", ItemCategory.TELEPORT);
		assertSubcategory(21387, "teleport-container");
		assertCategory(21389, "Master scroll book", ItemCategory.TELEPORT);
		assertSubcategory(21389, "teleport-container");
	}

	@Test
	public void canonicalTeleportChargeOverrideCoversFrozenTear()
	{
		assertCategory(29895, "Frozen tear", ItemCategory.TELEPORT);
		assertSubcategory(29895, "teleport-charge");
	}

	@Test
	public void canonicalSkillingResourceOverrideCoversAmethyst()
	{
		assertCategory(21347, "Amethyst", ItemCategory.SKILLING);
		assertSubcategory(21347, "resource");
	}

	@Test
	public void canonicalSkillingAmmoComponentOverridesCoverUnfinishedBoltsOnly()
	{
		assertCategory(9375, "Bronze bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9375, "ammo-component");
		assertCategory(9376, "Blurite bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9376, "ammo-component");
		assertCategory(9377, "Iron bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9377, "ammo-component");
		assertCategory(9378, "Steel bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9378, "ammo-component");
		assertCategory(9379, "Mithril bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9379, "ammo-component");
		assertCategory(9380, "Adamant bolts(unf)", ItemCategory.SKILLING);
		assertSubcategory(9380, "ammo-component");
		assertCategory(9381, "Runite bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9381, "ammo-component");
		assertCategory(9382, "Silver bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(9382, "ammo-component");
		assertCategory(21930, "Dragon bolts (unf)", ItemCategory.SKILLING);
		assertSubcategory(21930, "ammo-component");

		// Finished bolts are not part of this override: only the "(unf)" fletching
		// intermediate becomes an ammo-component. ID 9144 is the finished item that
		// shares a name prefix with ID 9381; it must remain untouched combat gear.
		assertCategory(9144, "Runite bolts", ItemCategory.GEAR);
		assertSubcategory(9144, "ammo");
	}

	@Test
	public void canonicalUnstrungBowsAreFletchingComponentsWithoutTouchingDuplicateRecords()
	{
		assertCategory(48, "Longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(48, "ammo-component");
		assertCategory(50, "Shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(50, "ammo-component");
		assertCategory(54, "Oak shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(54, "ammo-component");
		assertCategory(56, "Oak longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(56, "ammo-component");
		assertCategory(58, "Willow longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(58, "ammo-component");
		assertCategory(60, "Willow shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(60, "ammo-component");
		assertCategory(62, "Maple longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(62, "ammo-component");
		assertCategory(64, "Maple shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(64, "ammo-component");
		assertCategory(66, "Yew longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(66, "ammo-component");
		assertCategory(68, "Yew shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(68, "ammo-component");
		assertCategory(70, "Magic longbow (u)", ItemCategory.SKILLING);
		assertSubcategory(70, "ammo-component");
		assertCategory(72, "Magic shortbow (u)", ItemCategory.SKILLING);
		assertSubcategory(72, "ammo-component");

		assertFalse(CanonicalItemClassificationOverrides.find(49).isPresent());
		assertFalse(CanonicalItemClassificationOverrides.find(51).isPresent());
		assertFalse(CanonicalItemClassificationOverrides.find(18229).isPresent());
		assertFalse(CanonicalItemClassificationOverrides.find(18230).isPresent());
	}

	@Test
	public void canonicalUnfinishedCrossbowsAreFletchingComponents()
	{
		assertAuditFamily(new int[] {9454, 9456, 9457, 9459, 9461, 9463, 9465, 21921},
			ItemCategory.SKILLING, "ammo-component");
	}

	@Test
	public void guildHunterOutfitStaysWithSkillingEquipment()
	{
		assertAuditFamily(new int[] {29263, 29265, 29267, 29269},
			ItemCategory.TOOL, "skilling-equipment");
	}

	@Test
	public void canonicalSkillingPrayerResourceOverridesCoverBirdsEgg()
	{
		assertCategory(5076, "Bird's egg", ItemCategory.SKILLING);
		assertSubcategory(5076, "prayer-resource");
		assertCategory(5077, "Bird's egg", ItemCategory.SKILLING);
		assertSubcategory(5077, "prayer-resource");
		assertCategory(5078, "Bird's egg", ItemCategory.SKILLING);
		assertSubcategory(5078, "prayer-resource");
	}

	@Test
	public void auditedUnfinishedBroadBoltFamiliesRouteToFletchingResources()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Unfinished_broad_bolts?oldid=15185058
		assertAuditFamily(new int[] {11876}, ItemCategory.SKILLING, "ammo-component");
		// Wiki: https://oldschool.runescape.wiki/w/Unfinished_broad_bolt_pack?oldid=15215059
		assertAuditFamily(new int[] {11887}, ItemCategory.SKILLING, "ammo-component");
	}

	@Test
	public void auditedHerbloreItemsWithRepeatableFunctionsStayInFunctionalWorkflows()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Cadava_berries?oldid=15183459
		assertAuditFamily(new int[] {753}, ItemCategory.FARMING, "produce");

		// Wiki: https://oldschool.runescape.wiki/w/Grimy_snake_weed?oldid=15186652
		assertAuditFamily(new int[] {1525}, ItemCategory.HERBLORE, "secondary");
		// Wiki: https://oldschool.runescape.wiki/w/Snake_weed?oldid=15184203
		assertAuditFamily(new int[] {1526}, ItemCategory.HERBLORE, "secondary");
		// Wiki: https://oldschool.runescape.wiki/w/Grimy_rogue's_purse?oldid=15186655
		assertAuditFamily(new int[] {1533}, ItemCategory.HERBLORE, "secondary");
		// Wiki: https://oldschool.runescape.wiki/w/Goutweed?oldid=15183815
		assertAuditFamily(new int[] {3261}, ItemCategory.HERBLORE, "secondary");

		// Wiki: https://oldschool.runescape.wiki/w/Unfired_pot_lid?oldid=15187385
		assertAuditFamily(new int[] {4438}, ItemCategory.SKILLING, "crafting-material");
		// Wiki: https://oldschool.runescape.wiki/w/Herb_tea_mix?oldid=15185742
		assertAuditFamily(new int[] {4464, 4466, 4468, 4470, 4472, 4474, 4476, 4478, 4480, 4482},
			ItemCategory.HERBLORE, "herblore");
		// Wiki: https://oldschool.runescape.wiki/w/Magic_essence_(unf)?oldid=15186269
		assertAuditFamily(new int[] {9019}, ItemCategory.HERBLORE, "herblore");

		// Wiki: https://oldschool.runescape.wiki/w/Goblin_potion?oldid=15195296
		assertClassification(26581, "Goblin potion(4)", ItemCategory.POTION, "potion-dose-4");
		assertClassification(26583, "Goblin potion(3)", ItemCategory.POTION, "potion-dose-3");
		assertClassification(26585, "Goblin potion(2)", ItemCategory.POTION, "potion-dose-2");
		assertClassification(26587, "Goblin potion(1)", ItemCategory.POTION, "potion-dose-1");
	}

	@Test
	public void auditedQuestHerbloreFamiliesRouteToCleanupReview()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Ardrigal_mixture?oldid=15185810
		assertAuditFamily(new int[] {738}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Unicorn_horn_(Underground_Pass)?oldid=15185533
		assertAuditFamily(new int[] {1487}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Grimy_ardrigal?oldid=15186651
		assertAuditFamily(new int[] {1527}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Grimy_sito_foil?oldid=15186653
		assertAuditFamily(new int[] {1529}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Grimy_volencia_moss?oldid=15186654
		assertAuditFamily(new int[] {1531}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Mysterious_herb?oldid=15188842
		assertAuditFamily(new int[] {22402}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Unfinished_blood_potion?oldid=15188844
		assertAuditFamily(new int[] {22406}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Unfinished_potion_(A_Taste_of_Hope)?oldid=15188845
		assertAuditFamily(new int[] {22408}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Elder_cadantine?oldid=15189342
		assertAuditFamily(new int[] {23798}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Elder_cadantine_potion_(unf)?oldid=15189343
		assertAuditFamily(new int[] {23800}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Korbal_herb?oldid=15191527
		assertAuditFamily(new int[] {28384}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Smooth_leaf?oldid=15192435
		assertAuditFamily(new int[] {28978}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Sticky_leaf?oldid=15192437
		assertAuditFamily(new int[] {28979}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Enriched_snapdragon?oldid=15192547
		assertAuditFamily(new int[] {29530}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Enriched_snapdragon_seed?oldid=15192546
		assertAuditFamily(new int[] {29538}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Grimy_note?oldid=15192550
		assertAuditFamily(new int[] {29558}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Putrid_sticky_potion?oldid=15254431
		assertAuditFamily(new int[] {33803, 33804, 33805, 33806}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Foul_chunky_potion?oldid=15254430
		assertAuditFamily(new int[] {33807, 33808, 33809, 33810}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Rancid_slimy_potion?oldid=15263434
		assertAuditFamily(new int[] {33811, 33812, 33813, 33814}, ItemCategory.CLEANUP, "quest-item");
		// Wiki: https://oldschool.runescape.wiki/w/Rank_frothy_potion?oldid=15254428
		assertAuditFamily(new int[] {33815, 33816, 33817, 33818}, ItemCategory.CLEANUP, "quest-item");
	}

	@Test
	public void auditedLastManStandingPotionFamiliesRouteToCleanupReview()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Prayer_potion_(Last_Man_Standing)?oldid=15209257
		assertAuditFamily(new int[] {20393, 20394, 20395, 20396}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Super_energy_(Last_Man_Standing)?oldid=15239453
		assertAuditFamily(new int[] {20548, 20549, 20550, 20551}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Super_combat_potion_(Last_Man_Standing)?oldid=15239452
		assertAuditFamily(new int[] {23543, 23545, 23547, 23549}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Ranging_potion_(Last_Man_Standing)?oldid=15214947
		assertAuditFamily(new int[] {23551, 23553, 23555, 23557}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Sanfew_serum_(Last_Man_Standing)?oldid=15214946
		assertAuditFamily(new int[] {23559, 23561, 23563, 23565}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Super_restore_(Last_Man_Standing)?oldid=15239451
		assertAuditFamily(new int[] {23567, 23569, 23571, 23573}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Saradomin_brew_(Last_Man_Standing)?oldid=15214944
		assertAuditFamily(new int[] {23575, 23577, 23579, 23581}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Stamina_potion_(Last_Man_Standing)?oldid=15239456
		assertAuditFamily(new int[] {23583, 23585, 23587, 23589}, ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void auditedRestrictedMainTabFamiliesRouteToCleanupReview()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Deadman_teleport_tablet?oldid=15188858
		assertAuditFamily(new int[] {13666}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Ancient_magicks_tablet?oldid=15211531
		assertAuditFamily(new int[] {20430}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Survival_token?oldid=15187813
		assertAuditFamily(new int[] {20527}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_glory_(Last_Man_Standing)?oldid=15208961
		assertAuditFamily(new int[] {20586}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Rune_pouch_(Last_Man_Standing)?oldid=15208869
		assertAuditFamily(new int[] {23650}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_teleport_crystal?oldid=15189491
		assertAuditFamily(new int[] {23858}, ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void auditedRingOfWealthScrollKeepsItsRepeatableUpgradeFunction()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Ring_of_wealth_scroll?oldid=15186408
		assertAuditFamily(new int[] {12783}, ItemCategory.UNIQUE, "equipment-upgrade");
	}

	@Test
	public void auditedLeagueMainTabFamiliesRouteToCleanupReview()
	{
		// Sources are revision-pinned beside each exact switch case.
		assertAuditFamily(new int[] {
			25087, 25102, 25104, 26500, 26549, 28705, 30361, 30363, 30453, 30461
		}, ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void auditedRestrictedSupplyAndFarmingFamiliesRouteToCleanupReview()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Explosive_potion?oldid=15187163
		assertAuditFamily(new int[] {4045}, ItemCategory.CLEANUP, "junk");
		// Last Man Standing sources are revision-pinned beside each exact switch case.
		assertAuditFamily(new int[] {20390, 23533, 23628, 27178},
			ItemCategory.CLEANUP, "junk");
		// Corrupted Gauntlet and temporary League sources are likewise pinned beside the cases.
		assertAuditFamily(new int[] {23831, 31174, 33239, 33241},
			ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Mithril_seeds_(Last_Man_Standing)?oldid=15208871
		assertAuditFamily(new int[] {24534}, ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void auditedVictorsCapeFamilyRoutesToCollectionCosmetics()
	{
		/*
		 * Wiki revisions 15189530 through 15189534 and 15189665 confirm that
		 * every tier is an equipable LMS achievement cape with zero combat stats.
		 */
		assertAuditFamily(new int[] {24207, 24209, 24211, 24213, 24215, 24520},
			ItemCategory.CLUE, "cosmetic");
	}

	@Test
	public void auditedQuestToolPilotRoutesSingleUseQuestObjectsToReview()
	{
		/*
		 * Exact revision-pinned Wiki sources are recorded beside each matching
		 * switch family in CanonicalItemClassificationOverrides.
		 */
		assertAuditFamily(new int[] {
			602, 681, 1804, 1805, 1808, 1809, 1810, 2946, 2949, 2951, 3107,
			3696, 3697, 3719, 4200, 4415, 4444, 5586, 5587, 5601, 5605, 6712,
			6747, 7001, 7410, 7807, 9020, 9103, 26952, 28413, 28414, 28415,
			28964, 28965, 28966, 28967, 30320, 30989, 30990
		}, ItemCategory.CLEANUP, "quest-item");

		// Wearable zero-stat quest rewards belong with collection cosmetics.
		assertAuditFamily(new int[] {6786, 6787, 7917}, ItemCategory.CLUE, "cosmetic");
	}

	@Test
	public void auditedQuestToolCorrectionRestoresRepeatableFunctions()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Emerald_lantern?oldid=15185115
		assertAuditFamily(new int[] {9064, 9065, 20722}, ItemCategory.TOOL, "light-source");
		// Wiki: https://oldschool.runescape.wiki/w/Blessed_axe?oldid=15254568
		assertAuditFamily(new int[] {10491}, ItemCategory.TOOL, "tool");
	}

	@Test
	public void auditedFunctionalVariantControlsKeepTheirExistingSemantics()
	{
		// Normal-game Rune pouch remains an explicitly curated Main-tab grab item.
		assertClassification(12791, "Rune pouch", ItemCategory.RUNE, "rune-container");
		// Bounty supply crates are bankable, repeatable Bounty Hunter supplies.
		assertClassification(30616, "Bounty supply crate (manta ray)", ItemCategory.POTION, "food");
		assertClassification(30619, "Bounty supply crate (anglerfish)", ItemCategory.POTION, "food");
		// The empty tome is intentionally routed as reviewed loot rather than functional charged gear.
		assertClassification(20716, "Tome of fire (empty)", ItemCategory.CLEANUP, "cleanup");
	}

	@Test
	public void auditedLastManStandingGearFamiliesRouteToCleanupReview()
	{
		/*
		 * Each exact ID below has its own revision-pinned Wiki source beside the
		 * matching switch case in CanonicalItemClassificationOverrides. The pages
		 * identify these as Last Man Standing-only copies, not bankable versions
		 * of the normal functional equipment.
		 */
		assertAuditFamily(new int[] {
			27201, 27200, 20577, 20576, 20405, 20599, 20598, 23653, 20525, 23640,
			27173,
			27194, 27193, 27184, 20431, 33202, 23611, 20593, 29852, 23646, 23593,
			27169, 23595, 20423, 20424, 23642, 29846, 29848, 29847, 29843, 29845,
			29849, 29844, 27187, 20578, 33166, 33170, 33168, 20408, 25516, 23639,
			25515, 23633, 23649, 33460, 20407, 23597, 23648, 27157, 20406, 33186,
			29851, 29840, 29842, 29841, 27176, 27175, 21205, 23644, 27166, 27167,
			27168, 20557, 23638, 27180, 23591, 23603, 23607, 23605, 23622, 27170,
			27195, 27196, 27198, 27197, 23632, 23626, 27870, 33180, 33190, 33192,
			20581, 23619, 20426, 27159, 27161, 20425, 27158, 27160, 27172, 33178,
			33462, 23654, 27192, 33184, 23601, 27185, 20607, 20422, 27182, 23624,
			20397,
			23599, 23613, 23620, 23637, 23634, 27171, 33194, 33182, 27190, 27189,
			23636, 23635, 23615, 33198, 33196, 25517, 27162, 27181, 27186, 23617
		}, ItemCategory.CLEANUP, "junk");
	}

	@Test
	public void auditedCleanupItemsWithRepeatableWorkflowsReturnToFunctionalTabs()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Unstrung_light_ballista?oldid=15187508
		assertAuditFamily(new int[] {19604}, ItemCategory.SKILLING, "ammo-component");
		// Wiki: https://oldschool.runescape.wiki/w/Unstrung_heavy_ballista?oldid=15187507
		assertAuditFamily(new int[] {19607}, ItemCategory.SKILLING, "ammo-component");

		// Wiki: https://oldschool.runescape.wiki/w/Eclipse_moon_tassets?oldid=15211425
		assertAuditFamily(new int[] {29052}, ItemCategory.GEAR, "legs");
		// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_tassets?oldid=15211422
		assertAuditFamily(new int[] {29061}, ItemCategory.GEAR, "legs");
		// Wiki: https://oldschool.runescape.wiki/w/Blood_moon_tassets?oldid=15211419
		assertAuditFamily(new int[] {29070}, ItemCategory.GEAR, "legs");
	}

	@Test
	public void auditedRestrictedToolFamiliesFollowTheirVerifiedOngoingFunction()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Broken_pickaxe_(bronze)?oldid=15184083
		// Family cross-checks: oldid=15184086 (adamant) and oldid=15189068 (black).
		assertAuditFamily(new int[] {468, 470, 472, 474, 476, 478, 11923, 12594},
			ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Broken_fishing_rod?oldid=15184527
		assertAuditFamily(new int[] {6662}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_axe?oldid=15189345
		assertAuditFamily(new int[] {23821}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_pickaxe?oldid=15189346
		assertAuditFamily(new int[] {23822}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_harpoon?oldid=15189347
		assertAuditFamily(new int[] {23823}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Echo_axe?oldid=15239873
		assertAuditFamily(new int[] {25110}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Echo_pickaxe?oldid=15262483
		assertAuditFamily(new int[] {25112}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Echo_harpoon?oldid=15239875
		assertAuditFamily(new int[] {25114}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Sage's_axe?oldid=15224281
		assertAuditFamily(new int[] {28773}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Morrigan's_throwing_axe_(bh)?oldid=15191344
		assertAuditFamily(new int[] {27912, 27914}, ItemCategory.GEAR, "thrown-weapon");
	}

	@Test
	public void auditedRestrictedResourceFamiliesFollowTheirVerifiedOngoingFunction()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Broken_axe_(bronze)?oldid=15184560
		// Family cross-checks: oldid=15184562 (adamant) and oldid=15182617 (black).
		assertAuditFamily(new int[] {494, 496, 498, 500, 502, 504, 6741},
			ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Logs_(Tutorial_Island)?oldid=15190473
		assertAuditFamily(new int[] {2511, 24650}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Raw_shrimps_(Tutorial_Island)?oldid=15190474
		assertAuditFamily(new int[] {2514, 24652}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Bones_(Tutorial_Island)?oldid=15190476
		// Wiki: https://oldschool.runescape.wiki/w/Bones_(Soul_Wars)?oldid=15209204
		assertAuditFamily(new int[] {2530, 24655, 25199}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Climbing_rope?oldid=15187162
		assertAuditFamily(new int[] {4047}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Barricade?oldid=15187164
		// Wiki: https://oldschool.runescape.wiki/w/Barricade_(Soul_Wars)?oldid=15209192
		assertAuditFamily(new int[] {4053, 25209, 25210}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Queen_help_book?oldid=15186957
		assertAuditFamily(new int[] {10562}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Rope_(Last_Man_Standing)?oldid=15209259
		assertAuditFamily(new int[] {20587}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_dust?oldid=15189349
		assertAuditFamily(new int[] {23830}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_orb?oldid=15189352
		assertAuditFamily(new int[] {23833}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_ore?oldid=15189391
		assertAuditFamily(new int[] {23837}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Corrupted_paddlefish?oldid=15190372
		assertAuditFamily(new int[] {25958}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Burning_claws_(Last_Man_Standing)?oldid=15208943
		assertAuditFamily(new int[] {33200}, ItemCategory.CLEANUP, "junk");
		// Wiki: https://oldschool.runescape.wiki/w/Barbarian_arm?oldid=15194403
		assertAuditFamily(new int[] {33221}, ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Broken_bark_snelm?oldid=15182921
		assertAuditFamily(new int[] {3335}, ItemCategory.GEAR, "head");
		// Wiki: https://oldschool.runescape.wiki/w/Fighter_hat?oldid=15260298
		// Wiki: https://oldschool.runescape.wiki/w/Ranger_hat?oldid=15260300
		// Wiki: https://oldschool.runescape.wiki/w/Healer_hat?oldid=15260302
		// Wiki: https://oldschool.runescape.wiki/w/Runner_hat?oldid=15260301
		assertAuditFamily(new int[] {20507, 20509, 20511, 24531}, ItemCategory.GEAR, "head");
		// Wiki: https://oldschool.runescape.wiki/w/Cannon_barrels_(or)?oldid=15190733
		assertAuditFamily(new int[] {26524}, ItemCategory.GEAR, "cannon-part");
		// Wiki: https://oldschool.runescape.wiki/w/Dragon_battleaxe_(cr)?oldid=15191318
		assertAuditFamily(new int[] {28037}, ItemCategory.GEAR, "weapon");

		// Wiki: https://oldschool.runescape.wiki/w/Broken_antler?oldid=15194017
		assertAuditFamily(new int[] {31086}, ItemCategory.SKILLING, "ammo-component");
	}

	@Test
	public void canonicalHerbloreOverridesCoverWaterAndYewRoots()
	{
		assertCategory(227, "Vial of water", ItemCategory.HERBLORE);
		assertSubcategory(227, "herblore-base");
		assertCategory(6049, "Yew roots", ItemCategory.HERBLORE);
		assertSubcategory(6049, "secondary");
	}

	@Test
	public void canonicalSailingUtilitiesAvoidResourceAndCleanupLeaks()
	{
		assertCategory(31986, "Captain's log", ItemCategory.TOOL);
		assertSubcategory(31986, "sailing-utility");
		assertCategory(31989, "Boat bottle (empty)", ItemCategory.TOOL);
		assertSubcategory(31989, "sailing-utility");
		assertCategory(31733, "Barrel stand", ItemCategory.TOOL);
		assertSubcategory(31733, "sailing-upgrade");
		assertCategory(31745, "Captured wind mote", ItemCategory.TOOL);
		assertSubcategory(31745, "sailing-upgrade");
		assertCategory(31757, "Heart of ithell", ItemCategory.TOOL);
		assertSubcategory(31757, "sailing-upgrade");
	}

	@Test
	public void canonicalSailingItemsKeepTheirSpecificSemantics()
	{
		assertCategory(32399, "Sailors' amulet", ItemCategory.TELEPORT);
		assertSubcategory(32399, "teleport");
		assertCategory(31511, "Elkhorn frag", ItemCategory.FARMING);
		assertSubcategory(31511, "coral-fragment");
		assertCategory(31515, "Umbral frag", ItemCategory.FARMING);
		assertSubcategory(31515, "coral-fragment");
		assertCategory(31732, "Stormy key", ItemCategory.UNIQUE);
		assertSubcategory(31732, "reward-key");
		assertCategory(31744, "Fetid key", ItemCategory.UNIQUE);
		assertSubcategory(31744, "reward-key");
		assertCategory(31756, "Serrated key", ItemCategory.UNIQUE);
		assertSubcategory(31756, "reward-key");
	}

	@Test
	public void canonicalQuestUtilitiesAndKeyHalvesAvoidCleanup()
	{
		assertCategory(10890, "Prayer book", ItemCategory.TOOL);
		assertSubcategory(10890, "quest-utility");
		assertCategory(4024, "Ninja monkey greegree", ItemCategory.TOOL);
		assertSubcategory(4024, "quest-utility");
		assertCategory(4026, "Gorilla greegree", ItemCategory.TOOL);
		assertSubcategory(4026, "quest-utility");
		assertCategory(4030, "Zombie monkey greegree", ItemCategory.TOOL);
		assertSubcategory(4030, "quest-utility");
		assertCategory(4031, "Karamjan monkey greegree", ItemCategory.TOOL);
		assertSubcategory(4031, "quest-utility");
		assertCategory(985, "Tooth half of key", ItemCategory.UNIQUE);
		assertSubcategory(985, "key-material");
		assertCategory(987, "Loop half of key", ItemCategory.UNIQUE);
		assertSubcategory(987, "key-material");
	}

	@Test
	public void functionalQuestItemsDoNotPolluteCombatGear()
	{
		assertCategory(552, "Ghostspeak amulet", ItemCategory.TOOL);
		assertSubcategory(552, "quest-utility");
		assertCategory(4021, "M'speak amulet", ItemCategory.TOOL);
		assertSubcategory(4021, "quest-utility");
		assertCategory(6544, "Catspeak amulet(e)", ItemCategory.TOOL);
		assertSubcategory(6544, "quest-utility");
		assertCategory(6465, "Ring of charos(a)", ItemCategory.TOOL);
		assertSubcategory(6465, "quest-utility");
		assertCategory(4657, "Ring of visibility", ItemCategory.TOOL);
		assertSubcategory(4657, "quest-utility");
		assertCategory(3107, "Spiked boots", ItemCategory.CLEANUP);
		assertSubcategory(3107, "quest-item");
		assertCategory(6786, "Robe of elidinis", ItemCategory.CLUE);
		assertSubcategory(6786, "cosmetic");
		assertCategory(6787, "Robe of elidinis", ItemCategory.CLUE);
		assertSubcategory(6787, "cosmetic");
		assertCategory(4567, "Gold helmet", ItemCategory.TOOL);
		assertSubcategory(4567, "quest-utility");
		assertCategory(7917, "Ram skull helm", ItemCategory.CLUE);
		assertSubcategory(7917, "cosmetic");
	}

	@Test
	public void exactSlayerAndWintertodtUtilitiesDoNotPolluteCombatGear()
	{
		int[] slayerTools = {3337, 4156, 4551, 6720, 7159, 31398};
		for (int itemId : slayerTools)
		{
			assertCategoryOnly(itemId, ItemCategory.TOOL);
			assertSubcategory(itemId, "slayer-tool");
		}
		assertCategory(20712, "Warm gloves", ItemCategory.TOOL);
		assertSubcategory(20712, "skilling-utility");
		assertClassification(23037, "Boots of stone", ItemCategory.GEAR, "feet");
	}

	@Test
	public void exactMovementThievingAndMorytaniaUtilitiesDoNotPolluteCombatGear()
	{
		assertCategory(4600, "Willow blackjack", ItemCategory.TOOL);
		assertSubcategory(4600, "skilling-utility");
		assertCategory(6666, "Flippers", ItemCategory.TOOL);
		assertSubcategory(6666, "skilling-utility");
		assertCategory(10069, "Spotted cape", ItemCategory.TOOL);
		assertSubcategory(10069, "skilling-utility");
		assertCategory(10071, "Spottier cape", ItemCategory.TOOL);
		assertSubcategory(10071, "skilling-utility");
		assertCategory(22435, "Enchanted emerald sickle (b)", ItemCategory.TOOL);
		assertSubcategory(22435, "quest-utility");
	}

	@Test
	public void weakOrCosmeticLookingArmourWithRealDefenceStaysGear()
	{
		assertCategory(9733, "Mind helmet", ItemCategory.GEAR);
		assertSubcategory(9733, "head");
		assertCategory(23787, "Ardougne knight platebody", ItemCategory.GEAR);
		assertSubcategory(23787, "body");
	}

	@Test
	public void dramenStaffRoutesWithTheFairyRingTransportSystem()
	{
		assertCategory(772, "Dramen staff", ItemCategory.TELEPORT);
		assertSubcategory(772, "transport-access");
	}

	@Test
	public void questOriginDoesNotEvictRealCombatEquipmentFromGear()
	{
		assertCategory(1540, "Anti-dragon shield", ItemCategory.GEAR);
		assertSubcategory(1540, "shield");
		assertCategory(9084, "Lunar staff", ItemCategory.GEAR);
		assertSubcategory(9084, "weapon");
		assertCategory(9674, "Proselyte hauberk", ItemCategory.GEAR);
		assertSubcategory(9674, "body");
		assertCategory(10499, "Ava's accumulator", ItemCategory.GEAR);
		assertSubcategory(10499, "gear");
	}

	@Test
	public void canonicalRelicsConstructionMaterialAndShieldAvoidNameCollisions()
	{
		assertCategory(32865, "Dull knife", ItemCategory.UNIQUE);
		assertSubcategory(32865, "salvaging-relic");
		assertCategory(32870, "Smashed mirror", ItemCategory.UNIQUE);
		assertSubcategory(32870, "salvaging-relic");
		assertCategory(22710, "Curator's medallion", ItemCategory.SKILLING);
		assertSubcategory(22710, "construction-material");
	}

	@Test
	public void questFarmingAuditSeparatesQuestRemnantsFromRepeatableRewards()
	{
		// Full revision pages are cited beside the exact overrides. These IDs have
		// no recorded repeatable Farming function after their quest step.
		assertAuditFamily(new int[] {
			735, 736, 4205, 4206, 4486, 6112, 6453, 6454, 6455, 6456, 6457,
			6458, 6459, 6460, 6464, 6468, 6710, 9932, 23802, 23808, 23810
		}, ItemCategory.CLEANUP, "quest-item");

		// https://oldschool.runescape.wiki/w/Bone_seeds?oldid=15184790
		// Obtainable after Swan Song and repeatedly summons a cosmetic skeleton.
		assertClassification(7950, "Bone seeds", ItemCategory.CLUE, "cosmetic");

		// https://oldschool.runescape.wiki/w/Crystal_saw_seed?oldid=15203824
		// Repeatably replaceable precursor to the functional crystal saw.
		assertClassification(9626, "Crystal saw seed", ItemCategory.TOOL, "tool");
	}

	@Test
	public void questPotionsFoodAuditRoutesEveryConfirmedNonConsumableFamily()
	{
		// Every family has a full-page Wiki revision and function/counterevidence
		// comment beside its exact-ID override.
		assertAuditFamily(new int[] {5591}, ItemCategory.CLEANUP, "quest-item"); // ??? mixture
		assertAuditFamily(new int[] {29539}, ItemCategory.CLEANUP, "quest-item"); // agility dolmen
		assertAuditFamily(new int[] {33797}, ItemCategory.CLEANUP, "quest-item"); // amitire stew
		assertAuditFamily(new int[] {10841}, ItemCategory.CLEANUP, "quest-item"); // apricot cream pie
		assertAuditFamily(new int[] {28351}, ItemCategory.CLEANUP, "quest-item"); // arder-musca poison
		assertAuditFamily(new int[] {28355}, ItemCategory.CLEANUP, "quest-item"); // arder-resper poison
		assertAuditFamily(new int[] {29542}, ItemCategory.CLEANUP, "quest-item"); // attack dolmen
		assertAuditFamily(new int[] {29551}, ItemCategory.CLEANUP, "quest-item"); // balance dolmen
		assertAuditFamily(new int[] {22407}, ItemCategory.CLEANUP, "quest-item"); // blood potion
		assertAuditFamily(new int[] {739}, ItemCategory.CLEANUP, "quest-item"); // bravery potion
		assertAuditFamily(new int[] {756}, ItemCategory.CLEANUP, "quest-item"); // cadava potion
		assertAuditFamily(new int[] {7542}, ItemCategory.CLEANUP, "quest-item"); // cake of guidance
		assertAuditFamily(new int[] {30950}, ItemCategory.CLEANUP, "quest-item"); // canvas piece
		assertAuditFamily(new int[] {6766}, ItemCategory.CLEANUP, "quest-item"); // cat antipoison
		assertAuditFamily(new int[] {33769}, ItemCategory.CLEANUP, "quest-item"); // cloudy grey potion
		assertAuditFamily(new int[] {28425}, ItemCategory.CLEANUP, "quest-item"); // code converter
		assertAuditFamily(new int[] {33772}, ItemCategory.CLEANUP, "quest-item"); // cold bluish-white potion
		assertAuditFamily(new int[] {29545}, ItemCategory.CLEANUP, "quest-item"); // combat dolmen
		assertAuditFamily(new int[] {26959}, ItemCategory.CLEANUP, "quest-item"); // crocodile emblem
		assertAuditFamily(new int[] {30317}, ItemCategory.CLEANUP, "quest-item"); // decoder strips
		assertAuditFamily(new int[] {29544}, ItemCategory.CLEANUP, "quest-item"); // defence dolmen
		assertAuditFamily(new int[] {11154}, ItemCategory.CLEANUP, "quest-item"); // dream potion
		assertAuditFamily(new int[] {29925}, ItemCategory.CLEANUP, "quest-item"); // drinking flask
		assertAuditFamily(new int[] {1501}, ItemCategory.CLEANUP, "quest-item"); // dwarf brew
		assertAuditFamily(new int[] {524}, ItemCategory.CLEANUP, "quest-item"); // enchanted bear
		assertAuditFamily(new int[] {522}, ItemCategory.CLEANUP, "quest-item"); // enchanted beef
		assertAuditFamily(new int[] {525}, ItemCategory.CLEANUP, "quest-item"); // enchanted chicken
		assertAuditFamily(new int[] {523}, ItemCategory.CLEANUP, "quest-item"); // enchanted rat
		assertAuditFamily(new int[] {29540}, ItemCategory.CLEANUP, "quest-item"); // energy dolmen
		assertAuditFamily(new int[] {23818}, ItemCategory.CLEANUP, "quest-item"); // explosive potion
		assertAuditFamily(new int[] {7942, 7943}, ItemCategory.CLEANUP, "quest-item"); // fresh monkfish
		assertAuditFamily(new int[] {337}, ItemCategory.CLEANUP, "quest-item"); // giant carp
		assertAuditFamily(new int[] {28353}, ItemCategory.CLEANUP, "quest-item"); // holos-arder poison
		assertAuditFamily(new int[] {29548}, ItemCategory.CLEANUP, "quest-item"); // hunter dolmen
		assertAuditFamily(new int[] {23806}, ItemCategory.CLEANUP, "quest-item"); // inversion potion
		assertAuditFamily(new int[] {3152, 3154}, ItemCategory.CLEANUP, "quest-item"); // useless karambwan paste
		assertAuditFamily(new int[] {21394}, ItemCategory.CLEANUP, "quest-item"); // karambwanji
		assertAuditFamily(new int[] {3155, 3156}, ItemCategory.CLEANUP, "quest-item"); // karambwanji paste
		assertAuditFamily(new int[] {77}, ItemCategory.CLEANUP, "quest-item"); // khali brew
		assertAuditFamily(new int[] {29550}, ItemCategory.CLEANUP, "quest-item"); // magic dolmen
		assertAuditFamily(new int[] {2395}, ItemCategory.CLEANUP, "quest-item"); // magic ogre potion
		assertAuditFamily(new int[] {
			22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016, 22017,
			22018, 22019, 22020, 22021, 22022, 22023, 22024, 22025, 22026,
			22027, 22028, 22029, 22030, 22031, 22032
		}, ItemCategory.CLEANUP, "quest-item"); // Dragon Slayer II map pieces
		assertAuditFamily(new int[] {28350}, ItemCategory.CLEANUP, "quest-item"); // musca-holos poison
		assertAuditFamily(new int[] {28352}, ItemCategory.CLEANUP, "quest-item"); // musca-resper poison
		assertAuditFamily(new int[] {1486}, ItemCategory.CLEANUP, "quest-item"); // piece of railing
		assertAuditFamily(new int[] {2394, 22409}, ItemCategory.CLEANUP, "quest-item"); // generic potion family
		assertAuditFamily(new int[] {28382}, ItemCategory.CLEANUP, "quest-item"); // potion note
		assertAuditFamily(new int[] {21531}, ItemCategory.CLEANUP, "quest-item"); // potion of sealegs
		assertAuditFamily(new int[] {29547}, ItemCategory.CLEANUP, "quest-item"); // prayer dolmen
		assertAuditFamily(new int[] {29546}, ItemCategory.CLEANUP, "quest-item"); // ranged dolmen
		assertAuditFamily(new int[] {3742}, ItemCategory.CLEANUP, "quest-item"); // red herring
		assertAuditFamily(new int[] {22589}, ItemCategory.CLEANUP, "quest-item"); // reduced cadava potion
		assertAuditFamily(new int[] {28354}, ItemCategory.CLEANUP, "quest-item"); // resper-holos poison
		assertAuditFamily(new int[] {29541}, ItemCategory.CLEANUP, "quest-item"); // restoration dolmen
		assertAuditFamily(new int[] {22096}, ItemCategory.CLEANUP, "quest-item"); // revitalisation potion
		assertAuditFamily(new int[] {28443}, ItemCategory.CLEANUP, "quest-item"); // scarred scraps
		assertAuditFamily(new int[] {2882}, ItemCategory.CLEANUP, "quest-item"); // seasoned chompy
		assertAuditFamily(new int[] {1552}, ItemCategory.CLEANUP, "quest-item"); // seasoned sardine
		assertAuditFamily(new int[] {280, 281, 282, 283}, ItemCategory.CLEANUP, "quest-item"); // sheep bones
		assertAuditFamily(new int[] {25813}, ItemCategory.CLEANUP, "quest-item"); // shielding potion
		assertAuditFamily(new int[] {33820}, ItemCategory.CLEANUP, "quest-item"); // smelly kebab
		assertAuditFamily(new int[] {4836, 28383}, ItemCategory.CLEANUP, "quest-item"); // strange potions
		assertAuditFamily(new int[] {28388}, ItemCategory.CLEANUP, "quest-item"); // strangler serum
		assertAuditFamily(new int[] {29543}, ItemCategory.CLEANUP, "quest-item"); // strength dolmen
		assertAuditFamily(new int[] {26904}, ItemCategory.CLEANUP, "quest-item"); // strong cup of tea
		assertAuditFamily(new int[] {7579}, ItemCategory.CLEANUP, "quest-item"); // stuffed snake
		assertAuditFamily(new int[] {25812}, ItemCategory.CLEANUP, "quest-item"); // sulphur potion
		assertAuditFamily(new int[] {29898, 29899}, ItemCategory.CLEANUP, "quest-item"); // test kebabs
		assertAuditFamily(new int[] {33771}, ItemCategory.CLEANUP, "quest-item"); // thick red potion
		assertAuditFamily(new int[] {9656, 9657, 9658}, ItemCategory.CLEANUP, "quest-item"); // experience tome
		assertAuditFamily(new int[] {3265}, ItemCategory.CLEANUP, "quest-item"); // troll potion
		assertAuditFamily(new int[] {28386, 28387}, ItemCategory.CLEANUP, "quest-item"); // unfinished serum
		assertAuditFamily(new int[] {33770}, ItemCategory.CLEANUP, "quest-item"); // black puzzle potion
		assertAuditFamily(new int[] {29928}, ItemCategory.CLEANUP, "quest-item"); // wine labels

		assertAuditFamily(new int[] {3691, 6125, 6126, 6127}, ItemCategory.TELEPORT, "teleport");
		assertAuditFamily(new int[] {7528, 3422, 3424, 3426, 3428}, ItemCategory.SKILLING, "resource");
		assertAuditFamily(new int[] {732}, ItemCategory.GEAR, "thrown-weapon");
		assertAuditFamily(new int[] {7645, 7646, 7647, 7648}, ItemCategory.GEAR, "weapon");

		// The poison-paste variant remains a functional repeatable combat supply.
		assertClassification(3153, "Karambwan paste", ItemCategory.POTION, "food");
	}

	@Test
	public void resourcesQuestAuditPartOneRoutesReviewedFamiliesByOngoingFunction()
	{
		// Full revision-pinned pages were read for every family; source URLs match
		// the comments beside the exact-ID overrides.
		assertAuditFamily(new int[] {2410, 3718, 5604, 5584, 5583},
			ItemCategory.CLEANUP, "quest-item"); // Recruitment Drive materials
		assertAuditFamily(new int[] {27, 26, 338},
			ItemCategory.CLEANUP, "quest-item"); // Fishing Contest
		assertAuditFamily(new int[] {
			6956, 6947, 6958, 6954, 6948, 6950, 6951, 6946, 6953, 6945, 6952, 6957
		}, ItemCategory.CLEANUP, "quest-item"); // Hand in the Sand evidence
		assertAuditFamily(new int[] {1481, 1482, 1483, 1484, 23812},
			ItemCategory.CLEANUP, "quest-item"); // orbs of light
		assertAuditFamily(new int[] {18}, ItemCategory.CLEANUP, "quest-item"); // magic gold feather
		assertAuditFamily(new int[] {1797, 1799, 1801, 1803},
			ItemCategory.CLEANUP, "quest-item"); // Murder Mystery silver
		assertAuditFamily(new int[] {7531}, ItemCategory.CLEANUP, "quest-item"); // burnt fishcake
		assertAuditFamily(new int[] {10885, 10898},
			ItemCategory.CLEANUP, "quest-item"); // quest/dummy keg
		assertAuditFamily(new int[] {10174}, ItemCategory.CLEANUP, "quest-item"); // metal feather
		assertAuditFamily(new int[] {21261}, ItemCategory.CLEANUP, "quest-item"); // mysterious orb
		assertAuditFamily(new int[] {2389, 2390},
			ItemCategory.CLEANUP, "quest-item"); // Watchtower vials
		assertAuditFamily(new int[] {4617}, ItemCategory.CLEANUP, "quest-item"); // display cabinet key
		assertAuditFamily(new int[] {24938, 24939},
			ItemCategory.CLEANUP, "quest-item"); // waxwood
		assertAuditFamily(new int[] {2950}, ItemCategory.CLEANUP, "quest-item"); // Priest in Peril feather
		assertAuditFamily(new int[] {3218, 3219, 3220, 3221, 3215, 6095, 3222, 3223, 6093},
			ItemCategory.CLEANUP, "quest-item"); // Regicide explosives
		assertAuditFamily(new int[] {1842}, ItemCategory.CLEANUP, "quest-item"); // Ana in a barrel
		assertAuditFamily(new int[] {23791}, ItemCategory.CLEANUP, "quest-item"); // knight tabard
		assertAuditFamily(new int[] {11050}, ItemCategory.CLEANUP, "quest-item"); // Dorgesh artefact
		assertAuditFamily(new int[] {10488, 10489},
			ItemCategory.CLEANUP, "quest-item"); // Animal Magnetism materials
		assertAuditFamily(new int[] {4272}, ItemCategory.CLEANUP, "quest-item"); // Ghosts Ahoy bone key
		assertAuditFamily(new int[] {618, 604, 609, 610},
			ItemCategory.CLEANUP, "quest-item"); // Shilo quest remains
		assertAuditFamily(new int[] {21530}, ItemCategory.CLEANUP, "quest-item"); // bone charm
		assertAuditFamily(new int[] {7145}, ItemCategory.CLEANUP, "quest-item"); // Cabin Fever cannon barrel
		assertAuditFamily(new int[] {25145}, ItemCategory.CLEANUP, "quest-item"); // clay head
		assertAuditFamily(new int[] {5056, 5057, 5058, 5059, 5060, 5061},
			ItemCategory.CLEANUP, "quest-item"); // dwarven battleaxe states
		assertAuditFamily(new int[] {6673}, ItemCategory.CLEANUP, "quest-item"); // fishbowl and net
		assertAuditFamily(new int[] {704}, ItemCategory.CLEANUP, "quest-item"); // ground charcoal
		assertAuditFamily(new int[] {1502}, ItemCategory.CLEANUP, "quest-item"); // Iban's ashes
		assertAuditFamily(new int[] {587}, ItemCategory.CLEANUP, "quest-item"); // orb of protection
		assertAuditFamily(new int[] {1853}, ItemCategory.CLEANUP, "quest-item"); // prototype dart tip
		assertAuditFamily(new int[] {7543}, ItemCategory.CLEANUP, "quest-item"); // raw guide cake
		assertAuditFamily(new int[] {3702, 3703, 3704, 3705},
			ItemCategory.CLEANUP, "quest-item"); // Fremennik merchant chain
		assertAuditFamily(new int[] {759}, ItemCategory.CLEANUP, "quest-item"); // weapon store key
		assertAuditFamily(new int[] {
			28390, 33759, 33791, 33743, 33765, 33766, 33790, 33792, 33793, 33794
		}, ItemCategory.CLEANUP, "quest-item"); // Blood Moon quest materials

		int[] museumSpecimens = new int[105];
		for (int index = 0; index < museumSpecimens.length; index++)
		{
			museumSpecimens[index] = 7812 + index;
		}
		assertAuditFamily(museumSpecimens, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {26589, 26590, 26591, 26592, 26593},
			ItemCategory.CLEANUP, "quest-item"); // cave goblin leaders
		assertAuditFamily(new int[] {7967, 7969, 7970, 7971},
			ItemCategory.CLEANUP, "quest-item"); // pulley machine parts
		assertAuditFamily(new int[] {29549}, ItemCategory.CLEANUP, "quest-item"); // fishing dolmen
		assertAuditFamily(new int[] {28130}, ItemCategory.CLEANUP, "quest-item"); // strange icon
		assertAuditFamily(new int[] {28468, 28469, 28470},
			ItemCategory.CLEANUP, "quest-item"); // gooey notes
		assertAuditFamily(new int[] {10179, 600, 19515},
			ItemCategory.CLEANUP, "quest-item"); // POH-bookcase quest books
		assertAuditFamily(new int[] {24682, 28401},
			ItemCategory.CLEANUP, "quest-item"); // completed puzzle notes

		// Repeatable or durable functions win over quest origin.
		assertAuditFamily(new int[] {7529}, ItemCategory.SKILLING, "resource");
		assertAuditFamily(new int[] {7530}, ItemCategory.POTION, "food");
		assertAuditFamily(new int[] {678, 679}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {605}, ItemCategory.TOOL, "quest-utility");
		// Consumed into Kharedst's memoirs, where it adds 20 charges and one destination.
		// Wiki: https://oldschool.runescape.wiki/w/Kharedst's_memoirs?oldid=15240499
		assertAuditFamily(new int[] {21764}, ItemCategory.TELEPORT, "teleport-charge");

	}

	@Test
	public void resourcesQuestAuditPartTwoClosesTheRemainingReviewedFamilies()
	{
		// All linked revision pages were read in full; the URLs and decisive
		// post-quest/function facts are documented beside the exact-ID cases.
		assertAuditFamily(new int[] {
			30963, 783, 1841, 3216, 25794, 28970, 10177, 10175, 10176, 6545,
			7630, 28462, 28463, 23804, 4245, 4246, 33777, 33776, 11031, 22761,
			28458, 4568, 4007, 2384, 1583, 11196, 2391, 29427, 25968, 28806,
			22079, 30962, 31330, 10876, 24260, 6821, 588, 4621, 7577, 4445,
			7121, 7148, 11045, 6467, 9943, 30943, 3269, 29906, 9080, 9081,
			22095, 28394, 28395, 28396, 28397, 28398, 28399, 28400, 29536,
			29537, 28982, 28973, 9086, 33774, 26579, 11035, 27525
		}, ItemCategory.CLEANUP, "quest-item");

		assertAuditFamily(new int[] {9067, 28132, 28977, 7532, 29076, 4498},
			ItemCategory.CLEANUP, "junk");
		assertAuditFamily(new int[] {1581}, ItemCategory.HERBLORE, "secondary");
		assertAuditFamily(new int[] {31833, 25631}, ItemCategory.POTION, "food");
		assertAuditFamily(new int[] {31985, 31807}, ItemCategory.TOOL, "sailing-utility");
		assertAuditFamily(new int[] {6635, 9681, 28133, 28363, 21756},
			ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {10167, 3694},
			ItemCategory.SKILLING, "crafting-material");
		assertAuditFamily(new int[] {4241}, ItemCategory.SKILLING, "cooking-material");
		assertAuditFamily(new int[] {30970, 29874}, ItemCategory.CLUE, "cosmetic");
		assertAuditFamily(new int[] {21798}, ItemCategory.UNIQUE, "equipment-upgrade");
		assertAuditFamily(new int[] {274}, ItemCategory.CLUE, "treasure-trail");
		assertAuditFamily(new int[] {29535}, ItemCategory.TELEPORT, "teleport");
		assertAuditFamily(new int[] {676}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {2861}, ItemCategory.SKILLING, "ammo-component");

		// Normal repeatable resources and supplies were false-positive quest hits:
		// they remain in their existing functional categories.
		for (int itemId : new int[] {
			2862, 10810, 24691, 668, 32902, 9077, 9076, 3130, 3133, 3180,
			3128, 3129, 3131, 3132, 2365, 446, 29216, 7566, 3150, 2148,
			3179, 10812
		})
		{
			assertCategoryOnly(itemId, ItemCategory.SKILLING);
		}
		// All four Evil Dave colours are cooking ingredients for spicy stews.
		assertAuditFamily(new int[] {
			7480, 7481, 7482, 7483, 7484, 7485, 7486, 7487,
			7488, 7489, 7490, 7491, 7492, 7493, 7494, 7495
		}, ItemCategory.SKILLING, "cooking-material");

		// Elemental metal and split logs are confirmed repeatable materials, but
		// exact category overrides cannot change their existing OTHER_RESOURCE zone.
		// A classifier/metadata change is forbidden in this batch, so it remains
		// an explicit Group-B follow-up.
		assertCategoryOnly(2893, ItemCategory.SKILLING);
		assertFalse(CanonicalItemClassificationOverrides.find(2893).isPresent());
		assertCategoryOnly(10812, ItemCategory.SKILLING);
		assertFalse(CanonicalItemClassificationOverrides.find(10812).isPresent());
	}

	@Test
	public void combatGearQuestAuditPartOneRoutesOnlyFullyVerifiedFamilies()
	{
		// Every linked revision page and relevant creation/use chain was read in
		// full. The exact source URLs are documented beside the production cases.
		assertAuditFamily(new int[] {6, 8, 10}, ItemCategory.GEAR, "cannon-part");
		assertAuditFamily(new int[] {2886, 9715}, ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {721, 722, 723, 724, 725, 726},
			ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {720}, ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {2387}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {33103, 33104}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {30981}, ItemCategory.POTION, "food");
		assertAuditFamily(new int[] {4239}, ItemCategory.POTION, "food");
		assertAuditFamily(new int[] {30979}, ItemCategory.SKILLING, "cooking-material");
		assertAuditFamily(new int[] {4237}, ItemCategory.SKILLING, "cooking-material");
		assertAuditFamily(new int[] {2865}, ItemCategory.SKILLING, "ammo-component");
		assertAuditFamily(new int[] {24735}, ItemCategory.UNIQUE, "equipment-upgrade");

		assertAuditFamily(new int[] {6752}, ItemCategory.CLUE, "cosmetic");
		// The boots classify with the rest of the Builder's outfit; a set
		// never splits across tabs.
		assertAuditFamily(new int[] {10865}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {10171}, ItemCategory.CLUE, "cosmetic");
		assertAuditFamily(new int[] {29868, 29870, 29872}, ItemCategory.CLUE, "cosmetic");
		assertAuditFamily(new int[] {773}, ItemCategory.CLUE, "cosmetic");

		assertAuditFamily(new int[] {2888, 2889}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {1497, 1498, 1499}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33762}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {24688}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33738}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11048}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {9717}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11678}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {25799}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33787}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {757}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {6818}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {763, 765}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {4579}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {1813, 1814}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {2380, 2381, 2382, 2383},
			ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {25800}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {3267}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33754, 33755}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {5064}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {26903}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {6718}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {4674}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11052}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11013}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {30965}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {4196}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11058, 33760}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {4247}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {2385}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33740}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {10872}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {1849}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {22083}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11199}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {6985, 6986, 6987, 6988},
			ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {2397}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33749}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11054}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {28807}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33739}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {6772}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33758}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {84}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {28976}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {3109, 3110, 3111, 3112, 3113},
			ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {3700}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33757}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11056}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {623}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {6788, 6789}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {33751, 33752, 33753},
			ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {2386}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {11012}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {2964}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {10500}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {20781, 21059}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {86}, ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {29911}, ItemCategory.CLEANUP, "quest-item");

		assertAuditFamily(new int[] {19559}, ItemCategory.CLEANUP, "junk");
		assertAuditFamily(new int[] {31400}, ItemCategory.CLEANUP, "junk");
		assertAuditFamily(new int[] {29572}, ItemCategory.CLEANUP, "junk");
		assertAuditFamily(new int[] {31298, 31401}, ItemCategory.CLEANUP, "junk");
		assertAuditFamily(new int[] {4704}, ItemCategory.CLEANUP, "junk");

		// Armadyl pendant has a documented repeatable God Wars protection role.
		assertCategoryOnly(87, ItemCategory.GEAR);
		assertFalse(CanonicalItemClassificationOverrides.find(87).isPresent());
	}

	@Test
	public void combatGearQuestAuditPartTwoClosesTheRemainingFamilies()
	{
		// Full revision pages and linked variant/function chains were read. Source
		// URLs are repeated beside the exact production cases.
		assertAuditFamily(new int[] {89}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {4677, 4250, 4202},
			ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {4021, 4022, 9083}, ItemCategory.TOOL, "quest-utility");
		assertAuditFamily(new int[] {1845, 1846}, ItemCategory.TOOL, "skilling-utility");
		assertAuditFamily(new int[] {6066, 6067}, ItemCategory.CLUE, "cosmetic");
		assertAuditFamily(new int[] {4187, 4188, 4183},
			ItemCategory.CLEANUP, "quest-item");
		assertAuditFamily(new int[] {7806, 7808, 7809, 33801},
			ItemCategory.CLEANUP, "junk");

		// Wiki: https://oldschool.runescape.wiki/w/Crystal_bow_(historical)?oldid=15221194
		// Wiki: https://oldschool.runescape.wiki/w/Crystal_bow_(i)?oldid=15187529
		assertAuditFamily(new int[] {
			4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223,
			11748, 11749, 11750, 11751, 11752, 11753, 11754, 11755, 11756, 11757, 11758
		}, ItemCategory.CLEANUP, "junk");

		// The remaining 90 exact IDs all have a sourced, ongoing combat or
		// equipment function. This includes repairable/inactive states, which
		// remain Gear rather than being treated as cleanup merely by suffix.
		for (int itemId : new int[] {
			35, 78, 87, 428, 589, 667, 746, 747, 767, 777, 778, 1187, 1409,
			1410, 1478, 1495, 2405, 2415, 2416, 2417, 2866, 2883, 2890, 2952,
			3105, 4081, 4236, 4502, 5574, 5575, 6068, 6069, 6106, 6107, 6108,
			6109, 6110, 6611, 7668, 9091, 9092, 9093, 9096, 9098, 9099, 9100,
			9101, 9102, 9104, 9642, 9672, 9674, 9676, 9678, 9729, 9733, 10828,
			10838, 10839, 10858, 10887, 10888, 11014, 11061, 11200, 12017,
			23785, 23787, 23789, 23983, 23985, 23991, 23993, 24123, 24127,
			24265, 24266, 24699, 25250, 26763, 28327, 28329, 29560, 29562,
			29564, 29566, 29568, 29570, 30955, 33722
		})
		{
			assertCategoryOnly(itemId, ItemCategory.GEAR);
		}
	}

	@Test
	public void exactExportCleanupRoutesToolsSuppliesAndTeleports()
	{
		int[] smithsOutfit = {27023, 27025, 27027, 27029};
		for (int itemId : smithsOutfit)
		{
			assertCategoryOnly(itemId, ItemCategory.TOOL);
			assertSubcategory(itemId, "skilling-outfit");
		}

		assertClassification(1923, "Bowl", ItemCategory.TOOL, "cooking-tool");
		assertClassification(1588, "Grip's keyring", ItemCategory.GEAR, "gear");
		assertClassification(9433, "Bolt pouch", ItemCategory.GEAR, "gear");
		assertClassification(25580, "Tackle box", ItemCategory.TOOL, "resource-container");
		assertClassification(9419, "Mith grapple", ItemCategory.TOOL, "skilling-utility");
		assertClassification(13116, "Bonecrusher", ItemCategory.TOOL, "skilling-utility");
		assertClassification(6664, "Fishing explosive", ItemCategory.TOOL, "slayer-tool");
		assertClassification(26822, "Abyssal lantern", ItemCategory.TOOL, "runecrafting-utility");
		assertClassification(2309, "Bread", ItemCategory.POTION, "food");
		assertClassification(22081, "Locator orb", ItemCategory.POTION, "pvm-utility");
		assertClassification(4286, "Bucket of slime", ItemCategory.SKILLING, "prayer-resource");
		int[] damagedGodBooks = {3839, 3841, 3843, 12607, 12609, 12611};
		for (int itemId : damagedGodBooks)
		{
			assertClassification(itemId, "Damaged book", ItemCategory.GEAR, "shield");
		}

		assertClassification(21129, "Ring of returning(5)", ItemCategory.TELEPORT, "teleport");
		assertClassification(21132, "Ring of returning(4)", ItemCategory.TELEPORT, "teleport");
		assertClassification(21134, "Ring of returning(3)", ItemCategory.TELEPORT, "teleport");
		assertClassification(21136, "Ring of returning(2)", ItemCategory.TELEPORT, "teleport");
		assertClassification(21138, "Ring of returning(1)", ItemCategory.TELEPORT, "teleport");
	}

	@Test
	public void reviewedBlueprintOutliersUseTheirRepeatableGameplayFunction()
	{
		assertClassification(5016, "Bone spear", ItemCategory.GEAR, "weapon");
		assertClassification(22711, "Collection log", ItemCategory.CLUE, "collection-trophy");
		assertClassification(10107, "Long kebbit spike", ItemCategory.SKILLING, "ammo-component");
		assertAuditFamily(new int[] {11260, 29466}, ItemCategory.TOOL, "resource-container");
		assertClassification(10109, "Kebbit teeth", ItemCategory.HERBLORE, "secondary");
		assertClassification(1735, "Shears", ItemCategory.TOOL, "tool");
		assertAuditFamily(new int[] {20720, 4550}, ItemCategory.TOOL, "light-source");
		assertClassification(10476, "Purple sweets", ItemCategory.POTION, "food");
		assertAuditFamily(new int[] {1955, 1982, 5986}, ItemCategory.FARMING, "produce");
		assertClassification(5291, "Guam seed", ItemCategory.FARMING, "herb-seed");
		assertAuditFamily(new int[] {
			7480, 7481, 7482, 7483, 7484, 7485, 7486, 7487,
			7488, 7489, 7490, 7491, 7492, 7493, 7494, 7495
		}, ItemCategory.SKILLING, "cooking-material");
	}

	@Test
	public void exactExportCleanupRoutesBossLootAndCollectionTrophies()
	{
		assertClassification(19677, "Ancient shard", ItemCategory.UNIQUE, "equipment-charge");

		int[] hydraUpgrades = {22969, 22971, 22973};
		for (int itemId : hydraUpgrades)
		{
			assertCategoryOnly(itemId, ItemCategory.UNIQUE);
			assertSubcategory(itemId, "equipment-upgrade");
		}

		int[] bossKeys = {11942, 19679, 19681, 19683, 19685, 20754, 21724, 26356};
		for (int itemId : bossKeys)
		{
			assertCategoryOnly(itemId, ItemCategory.UNIQUE);
			assertSubcategory(itemId, "boss-access-key");
		}

		int[] collectionTrophies = {6800, 6807, 7975, 7977, 7981, 11258, 12007, 21275, 23064, 23077};
		for (int itemId : collectionTrophies)
		{
			assertCategoryOnly(itemId, ItemCategory.CLUE);
			assertSubcategory(itemId, "collection-trophy");
		}
	}

	@Test
	public void templateNeighborFalsePositivesKeepTheirOwnSemantics()
	{
		// These items sit next to categories touched by canonical override
		// batches (resources, teleport supplies, currency-like rewards) but are
		// not part of any override; their long-standing classification must
		// not shift as a side effect of adding nearby exact-ID mappings.
		assertCategory(954, "Rope", ItemCategory.SKILLING);
		assertSubcategory(954, "resource");
		assertCategory(23962, "Crystal shard", ItemCategory.SKILLING);
		assertSubcategory(23962, "resource");
		assertCategory(22586, "Looting bag", ItemCategory.TOOL);
		assertSubcategory(22586, "utility-container");
		assertCategory(22947, "Rada's blessing 4", ItemCategory.CURRENCY);
		assertSubcategory(22947, "currency");
		assertCategory(995, "Coins", ItemCategory.CURRENCY);
		assertSubcategory(995, "currency");
	}

	@Test
	public void unmappedItemIdsReceiveNoCanonicalOverride()
	{
		// Neither ID belongs to the canonical override batch; the lookup itself
		// (not just the resulting category) must report no override present.
		assertFalse(CanonicalItemClassificationOverrides.find(4718).isPresent());
		assertFalse(CanonicalItemClassificationOverrides.find(946).isPresent());

		// Battle Royale duplicates of already-mapped canonical IDs.
		assertFalse(CanonicalItemClassificationOverrides.find(29850).isPresent()); // BR Dual macuahuitl
		assertFalse(CanonicalItemClassificationOverrides.find(23609).isPresent()); // BR Ava's accumulator
		assertFalse(CanonicalItemClassificationOverrides.find(33172).isPresent()); // BR Avernic treads

		// Certs, placeholders, and lookalikes introduced by this batch's new families.
		assertFalse(CanonicalItemClassificationOverrides.find(23489).isPresent()); // Fake wine of zamorak
		assertFalse(CanonicalItemClassificationOverrides.find(21348).isPresent()); // cert/placeholder Amethyst
		assertFalse(CanonicalItemClassificationOverrides.find(21349).isPresent()); // cert/placeholder Amethyst
		assertFalse(CanonicalItemClassificationOverrides.find(21388).isPresent()); // cert/placeholder Master scroll book
		assertFalse(CanonicalItemClassificationOverrides.find(21390).isPresent()); // cert/placeholder Master scroll book
		assertFalse(CanonicalItemClassificationOverrides.find(19565).isPresent()); // placeholder Royal seed pod
		assertFalse(CanonicalItemClassificationOverrides.find(29894).isPresent()); // placeholder Pendant of ates
		assertFalse(CanonicalItemClassificationOverrides.find(29896).isPresent()); // placeholder Frozen tear
		assertFalse(CanonicalItemClassificationOverrides.find(28584).isPresent()); // placeholder Warped sceptre
		assertFalse(CanonicalItemClassificationOverrides.find(28586).isPresent()); // placeholder Warped sceptre

		// Duplicate/internal aliases and explicit no-op decisions from the vetted correction batch.
		for (int itemId : new int[] {289, 627, 637, 14048, 16420, 17427, 17428,
			19959, 19962, 20715, 20716, 25575, 25576, 27358, 30065, 30066,
			1201, 28027, 13393, 4178})
		{
			assertFalse("no canonical override for negative-control ID " + itemId,
				CanonicalItemClassificationOverrides.find(itemId).isPresent());
		}
	}

	private static void assertClassifications(int[] itemIds, String[] expectedNames,
		ItemCategory expectedCategory, String expectedSubcategory)
	{
		assertEquals("IDs and names must describe the same family", itemIds.length, expectedNames.length);
		for (int index = 0; index < itemIds.length; index++)
		{
			assertClassification(itemIds[index], expectedNames[index], expectedCategory, expectedSubcategory);
		}
	}

	private static void assertCategory(int itemId, String expectedName, ItemCategory expectedCategory)
	{
		Optional<CatalogItem> item = ResourceItemRegistry.INSTANCE.findById(itemId);
		assertTrue("registry should contain item " + itemId, item.isPresent());
		assertEquals(expectedName, item.get().getDisplayName());
		assertEquals("category of " + expectedName, expectedCategory, item.get().getCategory());
	}

	private static void assertSubcategory(int itemId, String expectedSubcategory)
	{
		CatalogItem item = ResourceItemRegistry.INSTANCE.findById(itemId).get();
		assertEquals("subcategory of " + item.getDisplayName(), expectedSubcategory, item.getSubcategory());
	}

	private static void assertAuditFamily(int[] itemIds, ItemCategory expectedCategory,
		String expectedSubcategory)
	{
		for (int itemId : itemIds)
		{
			assertCategoryOnly(itemId, expectedCategory);
			assertSubcategory(itemId, expectedSubcategory);
		}
	}

	private static void assertClassification(int itemId, String expectedName,
		ItemCategory expectedCategory, String expectedSubcategory)
	{
		assertCategory(itemId, expectedName, expectedCategory);
		assertSubcategory(itemId, expectedSubcategory);
	}

	private static void assertCategoryOnly(int itemId, ItemCategory expectedCategory)
	{
		Optional<CatalogItem> item = ResourceItemRegistry.INSTANCE.findById(itemId);
		assertTrue("registry should contain item " + itemId, item.isPresent());
		assertEquals("category of " + item.get().getDisplayName(), expectedCategory,
			item.get().getCategory());
	}
}

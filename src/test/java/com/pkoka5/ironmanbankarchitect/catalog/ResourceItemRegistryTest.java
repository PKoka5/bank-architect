package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
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
		assertCategory(227, "Vial of water", ItemCategory.SKILLING);
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
		assertCategory(5076, "Bird's egg", ItemCategory.FARMING);
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
		assertCategory(6049, "Yew roots", ItemCategory.SKILLING);
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
	public void cookingToolsAndContainersDoNotPollutePvmSupplies()
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
}

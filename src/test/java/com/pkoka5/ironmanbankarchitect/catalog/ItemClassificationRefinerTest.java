package com.pkoka5.ironmanbankarchitect.catalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemClassificationRefinerTest
{
	@Test
	public void routesWeaponComponentsAndChargeItemsAsValuableDrops()
	{
		assertClassification("Fire element staff crown", ItemCategory.GEAR,
			ItemCategory.UNIQUE, "weapon-upgrade");
		assertClassification("Barronite head", ItemCategory.GEAR,
			ItemCategory.UNIQUE, "weapon-upgrade");
		assertClassification("Burnt page", ItemCategory.CLEANUP,
			ItemCategory.UNIQUE, "equipment-charge");
		assertClassification("Searing page", ItemCategory.CLEANUP,
			ItemCategory.UNIQUE, "equipment-charge");
		assertClassification("Zulrah's scales", ItemCategory.SKILLING,
			ItemCategory.UNIQUE, "equipment-charge");
		assertClassification("Vial of blood", "VIAL_BLOOD", ItemCategory.CLEANUP,
			ItemCategory.UNIQUE, "equipment-charge");
		assertClassification("Vial of blood", "MYQ4_BLOOD_VIAL", ItemCategory.CLEANUP,
			ItemCategory.CLEANUP, "cleanup");
		assertClassification("Sunfire splinters", ItemCategory.HERBLORE,
			ItemCategory.UNIQUE, "equipment-charge");
		assertClassification("Salve shard", ItemCategory.CLEANUP,
			ItemCategory.UNIQUE, "equipment-upgrade");
	}

	@Test
	public void rescuesKnownReusableUtilitiesFromReview()
	{
		assertClassification("Binding necklace", ItemCategory.CLEANUP,
			ItemCategory.RUNE, "runecrafting-utility");
		assertClassification("Rock hammer", ItemCategory.SKILLING,
			ItemCategory.TOOL, "slayer-tool");
		assertClassification("Lit bug lantern", ItemCategory.CLEANUP,
			ItemCategory.TOOL, "slayer-tool");
		assertClassification("Large fur pouch", ItemCategory.CLEANUP,
			ItemCategory.TOOL, "resource-container");
		assertClassification("Soul bearer", ItemCategory.CLEANUP,
			ItemCategory.TOOL, "utility-container");
		assertClassification("Diving apparatus", ItemCategory.CLEANUP,
			ItemCategory.TOOL, "quest-utility");
		assertClassification("Holy wrench", ItemCategory.CLEANUP,
			ItemCategory.POTION, "pvm-utility");
		assertClassification("Open seed box", ItemCategory.FARMING,
			ItemCategory.TOOL, "resource-container");
		assertClassification("Open herb sack", ItemCategory.HERBLORE,
			ItemCategory.TOOL, "resource-container");
		assertClassification("Seed dibber", ItemCategory.FARMING,
			ItemCategory.TOOL, "tool");
		assertClassification("Vyre noble top", ItemCategory.GEAR,
			ItemCategory.TOOL, "quest-utility");
	}

	@Test
	public void preservesTeleportFormFromRegistryConstants()
	{
		assertClassification("Teleport to house", "POH_TABLET_TELEPORTTOHOUSE",
			ItemCategory.TELEPORT, ItemCategory.TELEPORT, "teleport-tablet");
		assertClassification("Nardah teleport", "TELEPORTSCROLL_NARDAH",
			ItemCategory.TELEPORT, ItemCategory.TELEPORT, "teleport-scroll");
	}

	@Test
	public void routesProcessingInputsAndFarmingProduceByUse()
	{
		assertClassification("Leaping sturgeon", ItemCategory.CLEANUP,
			ItemCategory.SKILLING, "raw-food");
		assertClassification("Chocolate dust", ItemCategory.SKILLING,
			ItemCategory.HERBLORE, "secondary");
		assertClassification("Swamp tar", ItemCategory.CLEANUP,
			ItemCategory.HERBLORE, "secondary");
		assertClassification("Spirit flakes", ItemCategory.CLEANUP,
			ItemCategory.SKILLING, "resource");
		assertClassification("Dragonfruit", ItemCategory.CLEANUP,
			ItemCategory.FARMING, "produce");
		assertClassification("Chocolate bar", ItemCategory.SKILLING,
			ItemCategory.SKILLING, "cooking-material");
		assertClassification("Ancient codex", ItemCategory.CLEANUP,
			ItemCategory.CLEANUP, "cleanup");
		assertClassification("Limpwurt seed", ItemCategory.HERBLORE,
			ItemCategory.FARMING, "herb-seed");
		assertClassification("Snape grass seed", ItemCategory.HERBLORE,
			ItemCategory.FARMING, "herb-seed");
		assertClassification("Redberries", ItemCategory.HERBLORE,
			ItemCategory.FARMING, "farming");
		assertClassification("White lily", ItemCategory.HERBLORE,
			ItemCategory.FARMING, "farming");
		assertClassification("Bullseye lantern (unf)", ItemCategory.HERBLORE,
			ItemCategory.SKILLING, "crafting-material");
		assertClassification("Battlestaff", ItemCategory.GEAR,
			ItemCategory.SKILLING, "crafting-material");
		assertClassification("Cheese", ItemCategory.CLEANUP,
			ItemCategory.SKILLING, "cooking-material");
		assertClassification("Sulliuscep cap", ItemCategory.CLEANUP,
			ItemCategory.SKILLING, "cooking-material");
		assertClassification("Emerald ring", ItemCategory.GEAR,
			ItemCategory.SKILLING, "crafting-jewellery");
		assertClassification("Bow string", ItemCategory.SKILLING,
			ItemCategory.SKILLING, "textile");
	}

	@Test
	public void burntAndTrophyFishAreNeverTreatedAsEdibleFood()
	{
		assertClassification("Burnt swordfish", ItemCategory.GEAR,
			ItemCategory.CLEANUP, "burnt-food");
		assertClassification("Big swordfish", ItemCategory.GEAR,
			ItemCategory.CLUE, "collection-trophy");
		assertClassification("Stuffed big shark", ItemCategory.CLEANUP,
			ItemCategory.CLUE, "collection-trophy");
	}

	@Test
	public void enrichesGearSlotsWithoutTreatingComponentsAsEquipment()
	{
		assertClassification("Mith grapple", ItemCategory.GEAR,
			ItemCategory.GEAR, "ammo");
		assertClassification("Gold ring", ItemCategory.GEAR,
			ItemCategory.SKILLING, "crafting-jewellery");
	}

	@Test
	public void herbSubstringsInsideOtherWordsDoNotMakeHerbs()
	{
		// "catherby" contains "herb"; the book must never become a herb.
		assertClassification("Mayor of catherby", ItemCategory.CLEANUP,
			ItemCategory.CLEANUP, "quest-item");
	}

	@Test
	public void fishNamesInsideToolNamesDoNotMakeFood()
	{
		assertClassification("Lobster pot", ItemCategory.TOOL,
			ItemCategory.TOOL, "tool");
		assertClassification("Karambwan vessel", ItemCategory.SKILLING,
			ItemCategory.TOOL, "tool");
		assertClassification("Gadderhammer", ItemCategory.TOOL,
			ItemCategory.GEAR, "weapon");
	}

	@Test
	public void barbarianMixesRemainSeparatePartialDosePotions()
	{
		assertClassification("Superattack mix(2)", ItemCategory.POTION,
			ItemCategory.POTION, "potion-dose-2");
		assertClassification("Prayer mix(1)", ItemCategory.POTION,
			ItemCategory.POTION, "potion-dose-1");
	}

	private static void assertClassification(String name, ItemCategory legacyCategory,
		ItemCategory expectedCategory, String expectedSubcategory)
	{
		assertClassification(name, "TEST_ITEM", legacyCategory, expectedCategory, expectedSubcategory);
	}

	private static void assertClassification(String name, String constantName, ItemCategory legacyCategory,
		ItemCategory expectedCategory, String expectedSubcategory)
	{
		ItemClassificationRefiner.Classification classification =
			ItemClassificationRefiner.refine(name, constantName, legacyCategory);

		assertEquals(name, expectedCategory, classification.getCategory());
		assertEquals(name, expectedSubcategory, classification.getSubcategory());
	}
}

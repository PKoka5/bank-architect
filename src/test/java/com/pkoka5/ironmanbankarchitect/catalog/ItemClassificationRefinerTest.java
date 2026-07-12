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
			ItemCategory.TOOL, "skilling-utility");
		assertClassification("Holy wrench", ItemCategory.CLEANUP,
			ItemCategory.POTION, "pvm-utility");
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
		assertClassification("Vyre noble top", ItemCategory.CLEANUP,
			ItemCategory.GEAR, "body");
		assertClassification("Vyre noble shoes", ItemCategory.CLEANUP,
			ItemCategory.GEAR, "feet");
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

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
	}

	private static void assertCategory(int itemId, String expectedName, ItemCategory expectedCategory)
	{
		Optional<CatalogItem> item = ResourceItemRegistry.INSTANCE.findById(itemId);
		assertTrue("registry should contain item " + itemId, item.isPresent());
		assertEquals(expectedName, item.get().getDisplayName());
		assertEquals("category of " + expectedName, expectedCategory, item.get().getCategory());
	}
}

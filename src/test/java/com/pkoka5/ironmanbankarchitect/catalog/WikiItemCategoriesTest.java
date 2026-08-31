package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class WikiItemCategoriesTest
{
	@Test
	public void toolsOutrankTheWorkflowTheToolBelongsTo()
	{
		// A pestle and mortar is filed under Herblore as well as Tools, and it
		// belongs with the tools rather than with the ingredients.
		assertEquals(ItemCategory.TOOL, WikiItemCategories
			.classify(Arrays.asList("Herblore", "Tools")).get().getCategory());
		assertEquals(ItemCategory.FARMING, WikiItemCategories
			.classify(Collections.singletonList("Seeds")).get().getCategory());
	}

	@Test
	public void skillingEquipmentOutranksTheTeleportItHappensToCarry()
	{
		assertEquals(ItemCategory.TOOL, WikiItemCategories
			.classify(Arrays.asList("Teleportation items", "Skilling equipment")).get().getCategory());
	}

	@Test
	public void categoriesOutsideTheSnapshotClassifyNothing()
	{
		assertFalse(WikiItemCategories.classify(
			Collections.singletonList("Quest items")).isPresent());
		assertFalse(WikiItemCategories.classify(Collections.<String>emptyList()).isPresent());
	}

	@Test
	public void agreementLeavesTheRefinerClassificationAlone()
	{
		assertFalse(WikiItemCategories.overrules(ItemCategory.FARMING, ItemCategory.FARMING, true));
	}

	@Test
	public void aTopicalCategoryOnlyPromotesItemsLeftForReview()
	{
		assertTrue(WikiItemCategories.overrules(ItemCategory.CLEANUP, ItemCategory.TELEPORT, false));
		assertTrue(WikiItemCategories.overrules(ItemCategory.UNKNOWN, ItemCategory.TOOL, false));
		// Rope is a tool on the wiki and a resource in the bank; a skillcape
		// teleports and is still worn for its skill.
		assertFalse(WikiItemCategories.overrules(ItemCategory.SKILLING, ItemCategory.TOOL, false));
		assertFalse(WikiItemCategories.overrules(ItemCategory.TOOL, ItemCategory.TELEPORT, false));
	}

	@Test
	public void wikiNeverMovesGearOutOfTheCombatTabForCarryingATeleport()
	{
		assertFalse(WikiItemCategories.overrules(ItemCategory.GEAR, ItemCategory.TELEPORT, true));
		assertTrue(WikiItemCategories.overrules(ItemCategory.GEAR, ItemCategory.TOOL, true));
	}

	@Test
	public void wikiNeverMovesItemsAcrossTheBrewingStageLine()
	{
		assertFalse(WikiItemCategories.overrules(ItemCategory.HERBLORE, ItemCategory.POTION, true));
		assertFalse(WikiItemCategories.overrules(ItemCategory.POTION, ItemCategory.HERBLORE, true));
		assertTrue(WikiItemCategories.overrules(ItemCategory.CLEANUP, ItemCategory.HERBLORE, true));
	}

	@Test
	public void snapshotCarriesTheItemsTheBankAsksAbout()
	{
		// Skull sceptre and Rogue's purse both sat in storage cleanup before the
		// snapshot named them.
		assertEquals(ItemCategory.TELEPORT, WikiItemCategories.INSTANCE
			.overrideFor(9013, ItemCategory.CLEANUP).get().getCategory());
		assertEquals(ItemCategory.HERBLORE, WikiItemCategories.INSTANCE
			.overrideFor(1534, ItemCategory.CLEANUP).get().getCategory());
		assertFalse(WikiItemCategories.INSTANCE.overrideFor(-1, ItemCategory.CLEANUP).isPresent());
		assertTrue(WikiItemCategories.INSTANCE.size() > 2000);
	}
}

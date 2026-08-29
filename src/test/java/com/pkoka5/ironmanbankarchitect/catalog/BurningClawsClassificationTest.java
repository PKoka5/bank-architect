package com.pkoka5.ironmanbankarchitect.catalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Burning claws reached the resources tab because the registry files the
 * weapon and its drop piece under SKILLING. The Last Man Standing copy that
 * shares the display name must keep its junk classification.
 */
public class BurningClawsClassificationTest
{
	@Test
	public void burningClawsAreCombatEquipmentRatherThanSkillingResources()
	{
		ItemClassificationRefiner.Classification weapon =
			CanonicalItemClassificationOverrides.find(29577).get();
		assertEquals(ItemCategory.GEAR, weapon.getCategory());
		assertEquals("weapon", weapon.getSubcategory());

		ItemClassificationRefiner.Classification piece =
			CanonicalItemClassificationOverrides.find(29574).get();
		assertEquals(ItemCategory.GEAR, piece.getCategory());
	}

	@Test
	public void lastManStandingCopyStaysJunk()
	{
		ItemClassificationRefiner.Classification copy =
			CanonicalItemClassificationOverrides.find(33200).get();
		assertEquals(ItemCategory.CLEANUP, copy.getCategory());
	}
}

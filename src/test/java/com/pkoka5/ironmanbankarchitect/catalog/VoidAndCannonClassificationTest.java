package com.pkoka5.ironmanbankarchitect.catalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Two combat families were split across tabs by classification gaps: every
 * Void body piece failed closed into storage cleanup while the rest of the set
 * was gear, and the cannon lists named some parts but not others.
 */
public class VoidAndCannonClassificationTest
{
	private static final int[] VOID_TOPS = {
		8839, 13072, 15737, 20465, 20467, 20468, 24177, 24178, 26463, 26469,
		27000, 27003, 33476, 33478, 33480, 33481, 33482, 33483
	};

	private static final int[] CANNON_PARTS = {
		6, 8, 10, 12, 26520, 26522, 26524, 26526
	};

	@Test
	public void everyVoidBodyPieceIsCombatEquipment()
	{
		for (int itemId : VOID_TOPS)
		{
			ItemClassificationRefiner.Classification classification =
				CanonicalItemClassificationOverrides.find(itemId).get();
			assertEquals("item " + itemId, ItemCategory.GEAR, classification.getCategory());
			assertEquals("item " + itemId, "body", classification.getSubcategory());
		}
	}

	@Test
	public void allFourPartsOfBothCannonsShareOneSubcategory()
	{
		for (int itemId : CANNON_PARTS)
		{
			ItemClassificationRefiner.Classification classification =
				CanonicalItemClassificationOverrides.find(itemId).get();
			assertEquals("item " + itemId, ItemCategory.GEAR, classification.getCategory());
			assertEquals("item " + itemId, "cannon-part", classification.getSubcategory());
		}
	}
}

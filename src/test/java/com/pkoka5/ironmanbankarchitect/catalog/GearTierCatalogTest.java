package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.OptionalInt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GearTierCatalogTest
{
	@Test
	public void loadsCuratedStagesWithoutRuntimeLookups()
	{
		GearTierCatalog catalog = GearTierCatalog.INSTANCE;

		assertEquals(335, catalog.size());
		assertEquals(OptionalInt.of(5), catalog.tierOf(26382)); // Torva full helm, End
		assertEquals(OptionalInt.of(4), catalog.tierOf(11832)); // Bandos chestplate, Late
		assertEquals(OptionalInt.of(3), catalog.tierOf(1275)); // Rune pickaxe, Mid
		assertEquals(OptionalInt.of(1), catalog.tierOf(1725)); // Amulet of strength, Starter
		assertEquals(OptionalInt.of(5), catalog.tierOf(28936)); // Sunfire fanatic cuirass, End
		assertEquals(OptionalInt.of(5), catalog.tierOf(30753)); // Oathplate chest, End
		assertEquals(OptionalInt.of(4), catalog.tierOf(30076)); // Hueycoatl hide body, Late
	}

	@Test
	public void untieredItemReturnsEmpty()
	{
		assertFalse(GearTierCatalog.INSTANCE.tierOf(995).isPresent()); // Coins
	}

	@Test
	public void carriedForwardItemKeepsItsHighestObservedStage()
	{
		// Holy symbol appears at Prayer stage 2 (Early) and stage 3 (Mid) as a carry-forward;
		// the catalog must resolve to the higher stage rather than the first-seen one.
		assertEquals(OptionalInt.of(3), GearTierCatalog.INSTANCE.tierOf(1718));
	}

	@Test
	public void unknownStateIdsUseOnlyReviewedUnambiguousNameFallbacks()
	{
		GearTierCatalog catalog = GearTierCatalog.INSTANCE;

		assertEquals(OptionalInt.of(4), catalog.tierOf(900001, "Ahrim's hood 75"));
		assertEquals(OptionalInt.of(4), catalog.tierOf(900002, "Crystal body (inactive)"));
		assertEquals(OptionalInt.of(4), catalog.tierOf(900003, "Ahrim's hood (or)"));
		assertFalse(catalog.tierOf(900004, "Completely unknown helm (i)").isPresent());
	}

	@Test
	public void everyStageIsWithinTheDocumentedRange()
	{
		for (int itemId = 1; itemId < 30000; itemId++)
		{
			OptionalInt tier = GearTierCatalog.INSTANCE.tierOf(itemId);
			if (tier.isPresent())
			{
				assertTrue("tier out of range for itemId " + itemId, tier.getAsInt() >= 1 && tier.getAsInt() <= 5);
			}
		}
	}
}

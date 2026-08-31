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

		assertEquals(347, catalog.size());
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

	/**
	 * A set whose siblings are tiered but which is itself untiered falls back to the name
	 * heuristics, scores nothing, and drifts to the far end of the gear tab away from its
	 * family. Perilous Moons and Barrows must therefore be tiered as whole groups.
	 */
	@Test
	public void peerSetsOfTheSameSourceAreTieredAsAWholeGroup()
	{
		int[] perilousMoons = {
			29028, 29022, 29025, // Blood moon helm, chestplate, tassets
			29010, 29004, 29007, // Eclipse moon helm, chestplate, tassets
			29019, 29013, 29016, // Blue moon helm, chestplate, tassets
		};
		int[] barrows = {
			4716, 4720, 4722, // Dharok's helm, platebody, platelegs
			4724, 4728, 4730, // Guthan's helm, platebody, chainskirt
			4732, 4736, 4738, // Karil's coif, leathertop, leatherskirt
			4708, 4712, 4714, // Ahrim's hood, robetop, robeskirt
			4745, 4749, 4751, // Torag's helm, platebody, platelegs
			4753, 4757, 4759, // Verac's helm, brassard, plateskirt
		};

		for (int itemId : perilousMoons)
		{
			assertEquals("Perilous Moons " + itemId,
				OptionalInt.of(4), GearTierCatalog.INSTANCE.tierOf(itemId));
		}
		for (int itemId : barrows)
		{
			assertEquals("Barrows " + itemId,
				OptionalInt.of(4), GearTierCatalog.INSTANCE.tierOf(itemId));
		}
	}
}

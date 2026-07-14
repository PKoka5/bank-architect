package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class IronmanAlchCandidateCatalogTest
{
	@Test
	public void canonicalRepresentativesCoverEveryReviewedFamily()
	{
		for (int itemId : new int[] {
			ItemID.RUNE_PLATEBODY, ItemID.DRAGON_DAGGER_P__, ItemID.ADAMANT_KITESHIELD,
			ItemID.BATTLESTAFF, ItemID.MYSTIC_ROBE_TOP_DARK, ItemID.BLACK_DRAGONHIDE_BODY,
			ItemID.GRANITE_LEGS, ItemID.JEWL_DIAMOND_BRACELET
		})
		{
			assertTrue("expected reviewed alchable " + itemId,
				IronmanAlchCandidateCatalog.contains(itemId));
		}
	}

	@Test
	public void notedAndCollisionIdsAreNotInferredFromNames()
	{
		for (int itemId : new int[] {
			1128, 20421, 11094, 12381, 23209, 26533
		})
		{
			assertFalse("non-canonical collision must stay out: " + itemId,
				IronmanAlchCandidateCatalog.contains(itemId));
		}
	}
}

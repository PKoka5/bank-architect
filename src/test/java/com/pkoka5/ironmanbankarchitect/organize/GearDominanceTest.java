package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;

/**
 * Pareto dominance is what lets the alch rule make a claim it can prove. These
 * tests check the implementation against a reference built straight from the
 * numbers each item was constructed with, so a wrong axis order or a flipped
 * sign cannot pass unnoticed.
 */
public class GearDominanceTest
{
	// astab, aslash, acrush, amagic, arange, dstab, dslash, dcrush, dmagic,
	// drange, str, rstr, mdmg tenths, prayer, attack speed
	private static final int SPEED_AXIS = 14;
	private static final int AXES = 15;

	@Test
	public void dominanceMatchesAnIndependentPerAxisReference()
	{
		Random random = new Random(20260818L);
		for (int trial = 0; trial < 20_000; trial++)
		{
			int[] first = randomVector(random);
			int[] second = randomVector(random);

			assertEquals("trial " + trial, referenceDominates(first, second),
				statsOf(first).dominates(statsOf(second)));
		}
	}

	@Test
	public void anItemNeverBeatsItselfAndTiesNeverWin()
	{
		Random random = new Random(4815162342L);
		for (int trial = 0; trial < 2_000; trial++)
		{
			int[] vector = randomVector(random);
			GearStats stats = statsOf(vector);

			assertFalse(stats.dominates(stats));
			assertFalse(stats.dominates(statsOf(vector.clone())));
		}
	}

	@Test
	public void dominanceIsAntisymmetricAndTransitive()
	{
		Random random = new Random(31415926L);
		for (int trial = 0; trial < 5_000; trial++)
		{
			GearStats a = statsOf(randomVector(random));
			GearStats b = statsOf(randomVector(random));
			GearStats c = statsOf(randomVector(random));

			assertFalse("both cannot beat each other", a.dominates(b) && b.dominates(a));
			if (a.dominates(b) && b.dominates(c))
			{
				assertTrue("dominance must carry through", a.dominates(c));
			}
		}
	}

	@Test
	public void aFasterWeaponWithEqualStatsWins()
	{
		GearStats fast = weapon(70, 4);
		GearStats slow = weapon(70, 6);

		assertTrue(fast.dominates(slow));
		assertFalse(slow.dominates(fast));
	}

	@Test
	public void oneBetterAxisIsNotEnoughWhenAnotherIsWorse()
	{
		// Higher attack, but slower: neither item is beaten outright, which is
		// exactly the case a single tier score gets wrong.
		GearStats strongSlow = weapon(90, 6);
		GearStats weakFast = weapon(70, 4);

		assertFalse(strongSlow.dominates(weakFast));
		assertFalse(weakFast.dominates(strongSlow));
	}

	@Test
	public void magicDamageCountsAsItsOwnAxis()
	{
		GearStats withDamage = new GearStats(GearSlot.NECK, 0, 0, 0, 10, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 25, 0);
		GearStats without = new GearStats(GearSlot.NECK, 0, 0, 0, 10, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0);

		assertTrue(withDamage.dominates(without));
		assertFalse(without.dominates(withDamage));
	}

	@Test
	public void differentSlotsAreNeverComparable()
	{
		GearStats body = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			99, 99, 99, 99, 99, 0, 0);
		GearStats legs = new GearStats(GearSlot.LEGS, 0, 0, 0, 0, 0, 0, 0, 0,
			1, 1, 1, 1, 1, 0, 0);

		assertFalse(body.dominates(legs));
		assertFalse(legs.dominates(body));
	}

	@Test
	public void tierOnlyStatsFailClosedRatherThanClaimingAWinner()
	{
		GearStats full = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			20, 20, 20, 20, 20, 0, 0);
		GearStats tierOnly = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, 5);

		assertFalse(full.isComparable() == tierOnly.isComparable());
		assertFalse("cannot judge is not the same as beaten", full.dominates(tierOnly));
		assertFalse(tierOnly.dominates(full));
		assertFalse(tierOnly.dominates(tierOnly));
	}

	@Test
	public void splitDefencesStillProduceTheSameTierScore()
	{
		GearStats split = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			10, 20, 30, 40, 50, 0, 0);
		GearStats summed = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, 150);

		assertEquals(summed.score(), split.score());
		assertEquals(summed.slotRank(), split.slotRank());
		assertEquals(summed.style(), split.style());
	}

	private static GearStats weapon(int attack, int speed)
	{
		return new GearStats(GearSlot.WEAPON, attack, 0, 0, 0, 0, attack, 0, 0,
			0, 0, 0, 0, 0, 0, speed);
	}

	private static int[] randomVector(Random random)
	{
		int[] vector = new int[AXES];
		for (int axis = 0; axis < AXES; axis++)
		{
			// A narrow range makes ties and near-ties common, which is where
			// dominance decisions actually get interesting.
			vector[axis] = random.nextInt(5) - 1;
		}
		vector[SPEED_AXIS] = 4 + random.nextInt(3);
		return vector;
	}

	private static GearStats statsOf(int[] v)
	{
		return new GearStats(GearSlot.WEAPON, v[0], v[1], v[2], v[3], v[4],
			v[10], v[11], v[13], v[5], v[6], v[7], v[8], v[9], v[12], v[SPEED_AXIS]);
	}

	/** Straight from the source numbers, sharing nothing with the implementation. */
	private static boolean referenceDominates(int[] mine, int[] theirs)
	{
		boolean strictlyBetterSomewhere = false;
		for (int axis = 0; axis < AXES; axis++)
		{
			int a = axis == SPEED_AXIS ? -mine[axis] : mine[axis];
			int b = axis == SPEED_AXIS ? -theirs[axis] : theirs[axis];
			if (a < b)
			{
				return false;
			}
			if (a > b)
			{
				strictlyBetterSomewhere = true;
			}
		}
		return strictlyBetterSomewhere;
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import org.junit.Test;
import static org.junit.Assert.*;

public class GearRoleScoreTest
{
	@Test
	public void mixedBonusesKeepEachCombatRolesOwnOffence()
	{
		GearStats stats = new GearStats(GearSlot.BODY,
			-12, 18, 7, -20, 30, -4, 11, 3,
			-5, 9, -6, 8, 4, 9, 5);
		assertEquals(94, stats.score(GearStyle.MELEE));
		assertEquals(186, stats.score(GearStyle.RANGED));
		assertEquals(58, stats.score(GearStyle.MAGIC));
		assertEquals(22, stats.score(GearStyle.PRAYER));
	}

	@Test
	public void negativeAccuracyDoesNotCancelPositivePowerAndNegativePowerDoesNotCancelAccuracy()
	{
		GearStats stats = new GearStats(GearSlot.HEAD,
			-2, -3, -5, 14, -7, 8, 19, -3,
			-4, -1, 2, -6, 3, -8, 4);
		assertEquals(32, stats.score(GearStyle.MELEE));
		assertEquals(76, stats.score(GearStyle.RANGED));
		assertEquals(56, stats.score(GearStyle.MAGIC));
		assertEquals(0, stats.score(GearStyle.PRAYER));
	}

	@Test
	public void prayerRoleCountsDefenceAndPrayerWithoutBorrowingAnyAttackBonus()
	{
		GearStats stats = new GearStats(GearSlot.NECK,
			100, 100, 100, 100, 100, 100, 100, 4,
			1, 2, 3, 4, 5, 100, 4);
		assertEquals(31, stats.score(GearStyle.PRAYER));
	}

	@Test
	public void unknownRoleIsRejected()
	{
		GearStats stats = new GearStats(GearSlot.FEET,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0);
		assertThrows(NullPointerException.class, () -> stats.score(null));
	}
}

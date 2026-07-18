package com.pkoka5.ironmanbankarchitect.simulate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Outcome;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Scenario;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class RandomBankSimulatorTest
{
	private static List<Integer> universe;

	@BeforeClass
	public static void loadUniverse()
	{
		universe = RandomBankSimulator.loadItemUniverse();
	}

	@Test
	public void itemUniverseCoversTheFullRegistryWithoutCacheOnlyRecords()
	{
		assertTrue("universe unexpectedly small: " + universe.size(), universe.size() > 10_000);
	}

	@Test
	public void seededRandomBanksNeverStallOrDeadlockTheGuidanceLoop()
	{
		for (long seed = 1; seed <= 10; seed++)
		{
			for (Scenario scenario : Scenario.values())
			{
				for (int itemCount : new int[]{5, 24, 60})
				{
					SimulationResult result = RandomBankSimulator.simulate(
						seed, scenario, itemCount, universe);
					String context = "seed=" + seed + " scenario=" + scenario
						+ " items=" + itemCount;

					Outcome outcome = result.getOutcome();
					assertTrue(context + " outcome=" + outcome
							+ " error=" + result.getErrorMessage()
							+ " itemIds=" + result.getFailedItemIds(),
						outcome == Outcome.COMPLETED
							|| outcome == Outcome.UNSUPPORTED_PLAN
							|| outcome == Outcome.PLAN_BUILD_ERROR);

					if (outcome == Outcome.COMPLETED)
					{
						assertTrue(context + " final order mismatch",
							result.isFinalOrderVerified());
						if (result.getMinimumSwapsAtSortStart() >= 0)
						{
							assertEquals(context + " sorting used non-minimal swaps",
								result.getMinimumSwapsAtSortStart(), result.getSwapMoves());
						}
					}
				}
			}
		}
	}

	@Test
	public void identicalSeedsReproduceIdenticalSimulationRuns()
	{
		SimulationResult first = RandomBankSimulator.simulate(
			42, Scenario.RANDOM_TABS, 30, universe);
		SimulationResult second = RandomBankSimulator.simulate(
			42, Scenario.RANDOM_TABS, 30, universe);

		assertEquals(first.getOutcome(), second.getOutcome());
		assertEquals(first.getSampledItemIds(), second.getSampledItemIds());
		assertEquals(30, first.getSampledItemIds().size());
		assertEquals(first.getTotalMoves(), second.getTotalMoves());
		assertEquals(first.getSwapMoves(), second.getSwapMoves());
		assertEquals(first.getMoveCounts(), second.getMoveCounts());
	}
}

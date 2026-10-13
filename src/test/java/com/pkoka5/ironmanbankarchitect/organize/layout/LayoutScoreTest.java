package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class LayoutScoreTest
{
	private static final String[] COMPONENT_NAMES = {
		"highMissedRelations",
		"highMissedCompleteness",
		"mediumMissedRelations",
		"mediumMissedCompleteness",
		"lowMissedRelations",
		"lowMissedCompleteness",
		"orientationEvidenceRegret",
		"widthEvidenceRegret",
		"semanticFragmentation",
		"semanticRowBreaks",
		"startColumnDeviation",
		"lockedPrefixSpillover",
		"spilloverCompatibilityCost",
		"spilloverTransitions",
		"nominalFootprintSlack",
		"semanticSpan",
		"unrestrictedSwapLowerBound",
		"totalRankDisplacement"
	};

	@Test
	public void hasExactlyEighteenComponents()
	{
		assertEquals(18, LayoutScore.COMPONENT_COUNT);
		assertEquals(18, COMPONENT_NAMES.length);
		assertEquals(18, LayoutScore.Component.values().length);
		assertEquals(18, LayoutScore.zero().toComponentArray().length);
	}

	@Test
	public void zeroScoreHasAllComponentsZero()
	{
		assertArrayEquals(new int[18], LayoutScore.zero().toComponentArray());
	}

	@Test
	public void namedComponentsMapToPositionsInDocumentOrder()
	{
		int[] values = new int[18];
		for (int index = 0; index < values.length; index++)
		{
			values[index] = index + 1;
		}
		LayoutScore score = score(values);

		assertEquals(1, score.get(LayoutScore.Component.HIGH_MISSED_RELATIONS));
		assertEquals(2, score.get(LayoutScore.Component.HIGH_MISSED_COMPLETENESS));
		assertEquals(3, score.get(LayoutScore.Component.MEDIUM_MISSED_RELATIONS));
		assertEquals(4, score.get(LayoutScore.Component.MEDIUM_MISSED_COMPLETENESS));
		assertEquals(5, score.get(LayoutScore.Component.LOW_MISSED_RELATIONS));
		assertEquals(6, score.get(LayoutScore.Component.LOW_MISSED_COMPLETENESS));
		assertEquals(7, score.get(LayoutScore.Component.ORIENTATION_EVIDENCE_REGRET));
		assertEquals(8, score.get(LayoutScore.Component.WIDTH_EVIDENCE_REGRET));
		assertEquals(9, score.get(LayoutScore.Component.SEMANTIC_FRAGMENTATION));
		assertEquals(10, score.get(LayoutScore.Component.SEMANTIC_ROW_BREAKS));
		assertEquals(11, score.get(LayoutScore.Component.START_COLUMN_DEVIATION));
		assertEquals(12, score.get(LayoutScore.Component.LOCKED_PREFIX_SPILLOVER));
		assertEquals(13, score.get(LayoutScore.Component.SPILLOVER_COMPATIBILITY_COST));
		assertEquals(14, score.get(LayoutScore.Component.SPILLOVER_TRANSITIONS));
		assertEquals(15, score.get(LayoutScore.Component.NOMINAL_FOOTPRINT_SLACK));
		assertEquals(16, score.get(LayoutScore.Component.SEMANTIC_SPAN));
		assertEquals(17, score.get(LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND));
		assertEquals(18, score.get(LayoutScore.Component.TOTAL_RANK_DISPLACEMENT));
		assertArrayEquals(values, score.toComponentArray());
	}

	@Test
	public void everyComponentDominatesAllLaterComponents()
	{
		for (int index = 0; index < LayoutScore.COMPONENT_COUNT; index++)
		{
			int[] smaller = new int[18];
			int[] larger = new int[18];
			smaller[index] = 1;
			larger[index] = 2;
			for (int later = index + 1; later < LayoutScore.COMPONENT_COUNT; later++)
			{
				smaller[later] = 500;
			}

			String component = COMPONENT_NAMES[index];
			assertTrue(component + " must dominate all later components",
				score(smaller).compareTo(score(larger)) < 0);
			assertTrue(component + " comparison must be antisymmetric",
				score(larger).compareTo(score(smaller)) > 0);
		}
	}

	@Test
	public void allComponentsAreMinimized()
	{
		for (int index = 0; index < LayoutScore.COMPONENT_COUNT; index++)
		{
			int[] better = new int[18];
			int[] worse = new int[18];
			worse[index] = 1;

			assertTrue(COMPONENT_NAMES[index] + " must be minimized",
				score(better).compareTo(score(worse)) < 0);
		}
	}

	@Test
	public void movementComponentsStayZeroWithoutProvenDenseOrder()
	{
		LayoutScore score = LayoutScore.builder().movement(unprovenRequest(), 7, 19).build();

		assertEquals(0, score.get(LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND));
		assertEquals(0, score.get(LayoutScore.Component.TOTAL_RANK_DISPLACEMENT));
	}

	@Test
	public void movementWithoutProvenDenseOrderOverridesEarlierProvenValues()
	{
		LayoutScore score = LayoutScore.builder()
			.movement(provenRequest(), 5, 11)
			.movement(unprovenRequest(), 7, 19)
			.build();

		assertEquals(0, score.get(LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND));
		assertEquals(0, score.get(LayoutScore.Component.TOTAL_RANK_DISPLACEMENT));
	}

	@Test
	public void invalidDenseOrderCannotActivateMovement()
	{
		LayoutRequest invalid = new LayoutRequest(Collections.singletonList(
			LayoutEntry.of(new BankPreviewItem(1, "Item 1", 1), 0)), Collections.emptyList(),
			Collections.singletonList(99));

		LayoutScore score = LayoutScore.builder().movement(invalid, 7, 19).build();

		assertEquals(0, score.get(LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND));
		assertEquals(0, score.get(LayoutScore.Component.TOTAL_RANK_DISPLACEMENT));
	}

	@Test
	public void genericSetterCannotBypassProvenMovementOrder()
	{
		for (LayoutScore.Component component : Arrays.asList(
			LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND,
			LayoutScore.Component.TOTAL_RANK_DISPLACEMENT))
		{
			for (int value : new int[]{0, 7})
			{
				LayoutScore.Builder builder = LayoutScore.builder();
				try
				{
					builder.set(component, value);
					fail("generic setter must reject movement component " + component);
				}
				catch (IllegalArgumentException expected)
				{
					assertArrayEquals(new int[18], builder.build().toComponentArray());
				}
			}
		}
	}

	@Test
	public void movementWithProvenDenseOrderKeepsValues()
	{
		LayoutScore score = LayoutScore.builder().movement(provenRequest(), 7, 19).build();

		assertEquals(7, score.get(LayoutScore.Component.UNRESTRICTED_SWAP_LOWER_BOUND));
		assertEquals(19, score.get(LayoutScore.Component.TOTAL_RANK_DISPLACEMENT));
	}

	@Test
	public void rejectsNegativeComponents()
	{
		for (int index = 0; index < 16; index++)
		{
			LayoutScore.Component component = LayoutScore.Component.values()[index];
			try
			{
				LayoutScore.builder().set(component, -1);
				fail("expected IllegalArgumentException for " + component);
			}
			catch (IllegalArgumentException expected)
			{
				// expected
			}
		}

		try
		{
			LayoutScore.builder().movement(provenRequest(), -1, 0);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}

		try
		{
			LayoutScore.builder().movement(provenRequest(), 0, -1);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	@Test
	public void equalScoresAreEqualAndCompareZero()
	{
		int[] values = new int[18];
		Arrays.fill(values, 3);

		assertEquals(score(values), score(values));
		assertEquals(score(values).hashCode(), score(values).hashCode());
		assertEquals(0, score(values).compareTo(score(values)));
	}

	@Test
	public void toComponentArrayIsADefensiveCopy()
	{
		LayoutScore score = LayoutScore.zero();
		int[] components = score.toComponentArray();
		components[0] = 99;

		assertEquals(0, score.get(LayoutScore.Component.HIGH_MISSED_RELATIONS));
	}

	@Test
	public void tieKeyIsOnlyConsultedOnExactlyEqualScores()
	{
		DeterministicTieKey smallerKey = tieKey("alpha.rule", Arrays.asList(1));
		DeterministicTieKey largerKey = tieKey("beta.rule", Arrays.asList(2));

		LayoutScore betterScore = LayoutScore.zero();
		LayoutScore worseScore = score(componentArray(17, 1));

		assertTrue("score must decide before the tie key",
			LayoutScore.compareByScoreThenTieKey(betterScore, largerKey, worseScore, smallerKey) < 0);
		assertTrue("equal scores must fall back to the tie key",
			LayoutScore.compareByScoreThenTieKey(betterScore, smallerKey, betterScore, largerKey) < 0);
		assertEquals("equal scores and equal keys compare zero", 0,
			LayoutScore.compareByScoreThenTieKey(betterScore, smallerKey, betterScore,
				tieKey("alpha.rule", Arrays.asList(1))));
	}

	private static int[] componentArray(int index, int value)
	{
		int[] values = new int[18];
		values[index] = value;
		return values;
	}

	private static DeterministicTieKey tieKey(String ruleKey, java.util.List<Integer> itemIds)
	{
		java.util.List<Integer> blockItems = itemIds.size() >= 2
			? itemIds
			: Arrays.asList(itemIds.get(0), itemIds.get(0) + 1000);
		PlacedBlock block = LayoutTestFixtures.placedBlock(ruleKey, ruleKey + ".atom", 0,
			blockItems.size(), ShapePrimitive.HORIZONTAL_RUN, 0, 0,
			new LayoutCandidate.Row(0, blockItems));
		return new DeterministicTieKey(Arrays.asList(block), blockItems);
	}

	private static LayoutScore score(int[] values)
	{
		return LayoutScore.builder()
			.set(LayoutScore.Component.HIGH_MISSED_RELATIONS, values[0])
			.set(LayoutScore.Component.HIGH_MISSED_COMPLETENESS, values[1])
			.set(LayoutScore.Component.MEDIUM_MISSED_RELATIONS, values[2])
			.set(LayoutScore.Component.MEDIUM_MISSED_COMPLETENESS, values[3])
			.set(LayoutScore.Component.LOW_MISSED_RELATIONS, values[4])
			.set(LayoutScore.Component.LOW_MISSED_COMPLETENESS, values[5])
			.set(LayoutScore.Component.ORIENTATION_EVIDENCE_REGRET, values[6])
			.set(LayoutScore.Component.WIDTH_EVIDENCE_REGRET, values[7])
			.set(LayoutScore.Component.SEMANTIC_FRAGMENTATION, values[8])
			.set(LayoutScore.Component.SEMANTIC_ROW_BREAKS, values[9])
			.set(LayoutScore.Component.START_COLUMN_DEVIATION, values[10])
			.set(LayoutScore.Component.LOCKED_PREFIX_SPILLOVER, values[11])
			.set(LayoutScore.Component.SPILLOVER_COMPATIBILITY_COST, values[12])
			.set(LayoutScore.Component.SPILLOVER_TRANSITIONS, values[13])
			.set(LayoutScore.Component.NOMINAL_FOOTPRINT_SLACK, values[14])
			.set(LayoutScore.Component.SEMANTIC_SPAN, values[15])
			.movement(provenRequest(), values[16], values[17])
			.build();
	}

	private static LayoutRequest provenRequest()
	{
		return new LayoutRequest(Collections.singletonList(
			LayoutEntry.of(new BankPreviewItem(1, "Item 1", 1), 0)), Collections.emptyList(),
			Collections.singletonList(1));
	}

	private static LayoutRequest unprovenRequest()
	{
		return new LayoutRequest(Collections.singletonList(
			LayoutEntry.of(new BankPreviewItem(1, "Item 1", 1), 0)), Collections.emptyList());
	}
}

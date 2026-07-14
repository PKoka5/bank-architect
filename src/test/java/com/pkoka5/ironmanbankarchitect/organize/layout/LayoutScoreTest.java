package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
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
		assertEquals(18, LayoutScore.zero().toComponentArray().length);
	}

	@Test
	public void zeroScoreHasAllComponentsZero()
	{
		assertArrayEquals(new int[18], LayoutScore.zero().toComponentArray());
	}

	@Test
	public void namedGettersMapToComponentPositionsInDocumentOrder()
	{
		int[] values = new int[18];
		for (int index = 0; index < values.length; index++)
		{
			values[index] = index + 1;
		}
		LayoutScore score = score(values);

		assertEquals(1, score.getHighMissedRelations());
		assertEquals(2, score.getHighMissedCompleteness());
		assertEquals(3, score.getMediumMissedRelations());
		assertEquals(4, score.getMediumMissedCompleteness());
		assertEquals(5, score.getLowMissedRelations());
		assertEquals(6, score.getLowMissedCompleteness());
		assertEquals(7, score.getOrientationEvidenceRegret());
		assertEquals(8, score.getWidthEvidenceRegret());
		assertEquals(9, score.getSemanticFragmentation());
		assertEquals(10, score.getSemanticRowBreaks());
		assertEquals(11, score.getStartColumnDeviation());
		assertEquals(12, score.getLockedPrefixSpillover());
		assertEquals(13, score.getSpilloverCompatibilityCost());
		assertEquals(14, score.getSpilloverTransitions());
		assertEquals(15, score.getNominalFootprintSlack());
		assertEquals(16, score.getSemanticSpan());
		assertEquals(17, score.getUnrestrictedSwapLowerBound());
		assertEquals(18, score.getTotalRankDisplacement());
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

		assertEquals(0, score.getUnrestrictedSwapLowerBound());
		assertEquals(0, score.getTotalRankDisplacement());
	}

	@Test
	public void movementWithoutProvenDenseOrderOverridesEarlierProvenValues()
	{
		LayoutScore score = LayoutScore.builder()
			.movement(provenRequest(), 5, 11)
			.movement(unprovenRequest(), 7, 19)
			.build();

		assertEquals(0, score.getUnrestrictedSwapLowerBound());
		assertEquals(0, score.getTotalRankDisplacement());
	}

	@Test
	public void invalidDenseOrderCannotActivateMovement()
	{
		LayoutRequest invalid = new LayoutRequest(Collections.singletonList(
			LayoutEntry.of(new BankPreviewItem(1, "Item 1", 1), 0)), Collections.emptyList(),
			Collections.singletonList(99));

		LayoutScore score = LayoutScore.builder().movement(invalid, 7, 19).build();

		assertEquals(0, score.getUnrestrictedSwapLowerBound());
		assertEquals(0, score.getTotalRankDisplacement());
	}

	@Test
	public void sourceExposesNoDirectMovementComponentSetters() throws Exception
	{
		String source = new String(Files.readAllBytes(Paths.get(
			"src/main/java/com/pkoka5/ironmanbankarchitect/organize/layout/LayoutScore.java")),
			StandardCharsets.UTF_8);

		assertFalse(Pattern.compile("\\bBuilder\\s+unrestrictedSwapLowerBound\\s*\\(").matcher(source).find());
		assertFalse(Pattern.compile("\\bBuilder\\s+totalRankDisplacement\\s*\\(").matcher(source).find());
	}

	@Test
	public void movementWithProvenDenseOrderKeepsValues()
	{
		LayoutScore score = LayoutScore.builder().movement(provenRequest(), 7, 19).build();

		assertEquals(7, score.getUnrestrictedSwapLowerBound());
		assertEquals(19, score.getTotalRankDisplacement());
	}

	@Test
	public void rejectsNegativeComponents()
	{
		try
		{
			LayoutScore.builder().semanticSpan(-1);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
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

		assertEquals(0, score.getHighMissedRelations());
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
			.highMissedRelations(values[0])
			.highMissedCompleteness(values[1])
			.mediumMissedRelations(values[2])
			.mediumMissedCompleteness(values[3])
			.lowMissedRelations(values[4])
			.lowMissedCompleteness(values[5])
			.orientationEvidenceRegret(values[6])
			.widthEvidenceRegret(values[7])
			.semanticFragmentation(values[8])
			.semanticRowBreaks(values[9])
			.startColumnDeviation(values[10])
			.lockedPrefixSpillover(values[11])
			.spilloverCompatibilityCost(values[12])
			.spilloverTransitions(values[13])
			.nominalFootprintSlack(values[14])
			.semanticSpan(values[15])
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

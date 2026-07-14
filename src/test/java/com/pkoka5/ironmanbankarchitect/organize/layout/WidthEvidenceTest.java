package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class WidthEvidenceTest
{
	@Test
	public void gemFactUsesTheExactCompleteSupportVector()
	{
		WidthEvidence evidence = SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED;

		assertEquals(7, evidence.getEligibleTemplateCount());
		assertEquals(Arrays.asList(0, 2, 0, 0, 5, 0, 0, 0),
			evidence.getDistinctTemplateSupportByWidth());
		assertEquals(Arrays.asList(0, 5, 0, 0, 5, 0, 0, 0),
			evidence.getDistinctFamilySupportByWidth());
		assertTrue(evidence.hasPreferredWidth());
		assertEquals(5, evidence.getPreferredWidth());
		assertEquals(714, evidence.getSupportRate(5));
		assertEquals(285, evidence.getSupportRate(2));
		assertEquals(0, evidence.regretForWidth(5));
		assertEquals(429, evidence.regretForWidth(2));
		assertEquals(714, evidence.regretForWidth(1));
		assertEquals(0, evidence.preferenceRankForWidth(5));
		assertEquals(1, evidence.preferenceRankForWidth(2));
		assertEquals(2, evidence.preferenceRankForWidth(1));
	}

	@Test
	public void herbFactUsesFloorDivisionAndDenseEvidenceRanks()
	{
		WidthEvidence evidence = SemanticWidthEvidenceFacts.HERB_WORKFLOW;

		assertEquals(Arrays.asList(0, 1, 6, 1, 0, 0, 0, 0),
			evidence.getDistinctTemplateSupportByWidth());
		assertEquals(Arrays.asList(0, 10, 14, 4, 0, 0, 0, 0),
			evidence.getDistinctFamilySupportByWidth());
		assertEquals(3, evidence.getPreferredWidth());
		assertEquals(857, evidence.getSupportRate(3));
		assertEquals(142, evidence.getSupportRate(2));
		assertEquals(0, evidence.regretForWidth(3));
		assertEquals(715, evidence.regretForWidth(2));
		assertEquals(715, evidence.regretForWidth(4));
		assertEquals(857, evidence.regretForWidth(8));
		assertEquals(0, evidence.preferenceRankForWidth(3));
		assertEquals(1, evidence.preferenceRankForWidth(2));
		assertEquals(1, evidence.preferenceRankForWidth(4));
		assertEquals(2, evidence.preferenceRankForWidth(8));
	}

	@Test
	public void exactThresholdBoundaryActivatesPreference()
	{
		WidthEvidence minimumCounts = evidence(8,
			values(0, 3, 0, 0, 5, 0, 0, 0),
			values(0, 4, 0, 0, 3, 0, 0, 0));

		assertTrue(minimumCounts.hasPreferredWidth());
		assertEquals(5, minimumCounts.getPreferredWidth());
		assertEquals(625, minimumCounts.getSupportRate(5));
		assertEquals(250, minimumCounts.regretForWidth(2));

		WidthEvidence exactSixtyPercent = evidence(10,
			values(0, 4, 0, 0, 6, 0, 0, 0),
			values(0, 4, 0, 0, 3, 0, 0, 0));
		assertTrue(exactSixtyPercent.hasPreferredWidth());
		assertEquals(5, exactSixtyPercent.getPreferredWidth());
		assertEquals(600, exactSixtyPercent.getSupportRate(5));
		assertEquals(200, exactSixtyPercent.regretForWidth(2));
	}

	@Test
	public void everyFailedThresholdMakesAllWidthsNeutral()
	{
		List<WidthEvidence> inactive = Arrays.asList(
			evidence(6, values(0, 2, 0, 0, 4, 0, 0, 0), values(0, 2, 0, 0, 4, 0, 0, 0)),
			evidence(7, values(0, 2, 0, 0, 4, 0, 0, 0), values(0, 2, 0, 0, 4, 0, 0, 0)),
			evidence(7, values(0, 2, 0, 0, 5, 0, 0, 0), values(0, 2, 0, 0, 2, 0, 0, 0)),
			evidence(9, values(0, 2, 0, 0, 5, 0, 0, 0), values(0, 2, 0, 0, 5, 0, 0, 0)),
			evidence(8, values(0, 4, 0, 0, 5, 0, 0, 0), values(0, 4, 0, 0, 5, 0, 0, 0)),
			evidence(8, values(0, 5, 0, 0, 5, 0, 0, 0), values(0, 5, 0, 0, 5, 0, 0, 0)));

		for (WidthEvidence evidence : inactive)
		{
			assertFalse(evidence.hasPreferredWidth());
			for (int width = 1; width <= 8; width++)
			{
				assertEquals(0, evidence.regretForWidth(width));
				assertEquals(0, evidence.preferenceRankForWidth(width));
			}
			assertNoPreferredWidth(evidence);
		}
	}

	@Test
	public void rejectsMalformedOrIncompleteAggregateFacts()
	{
		assertEvidenceFails(() -> evidence(0, values(0, 0, 0, 0, 0, 0, 0, 0),
			values(0, 0, 0, 0, 0, 0, 0, 0)));
		assertEvidenceFails(() -> new WidthEvidence(7, null, values(0, 0, 0, 0, 0, 0, 0, 0)));
		assertEvidenceFails(() -> evidence(7, values(0, 0, 0, 0, 0, 0, 0),
			values(0, 0, 0, 0, 0, 0, 0, 0)));
		assertEvidenceFails(() -> evidence(7, values(0, -1, 0, 0, 5, 0, 0, 0),
			values(0, 1, 0, 0, 5, 0, 0, 0)));
		assertEvidenceFails(() -> evidence(7, values(0, 8, 0, 0, 0, 0, 0, 0),
			values(0, 8, 0, 0, 0, 0, 0, 0)));
		assertEvidenceFails(() -> evidence(7, values(0, 2, 0, 0, 5, 0, 0, 0),
			values(0, -2, 0, 0, 5, 0, 0, 0)));
		assertEvidenceFails(() -> evidence(7, values(0, 0, 0, 0, 5, 0, 0, 0),
			values(0, 2, 0, 0, 5, 0, 0, 0)));
	}

	@Test
	public void widthLookupRejectsValuesOutsideOneThroughEight()
	{
		WidthEvidence evidence = SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED;

		assertEvidenceFails(() -> evidence.getSupportRate(0));
		assertEvidenceFails(() -> evidence.regretForWidth(9));
		assertEvidenceFails(() -> evidence.preferenceRankForWidth(-1));
	}

	@Test
	public void vectorsAreDefensivelyCopiedAndImmutable()
	{
		List<Integer> templates = new ArrayList<>(values(0, 2, 0, 0, 5, 0, 0, 0));
		List<Integer> families = new ArrayList<>(values(0, 5, 0, 0, 5, 0, 0, 0));
		WidthEvidence evidence = new WidthEvidence(7, templates, families);

		templates.set(4, 0);
		families.set(4, 0);
		assertEquals(5, evidence.getDistinctTemplateSupport(5));
		assertEquals(5, evidence.getDistinctFamilySupport(5));
		assertImmutable(evidence.getDistinctTemplateSupportByWidth());
		assertImmutable(evidence.getDistinctFamilySupportByWidth());
	}

	@Test
	public void equalityCoversEveryAggregateFact()
	{
		WidthEvidence copy = evidence(7,
			values(0, 2, 0, 0, 5, 0, 0, 0), values(0, 5, 0, 0, 5, 0, 0, 0));

		assertEquals(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED, copy);
		assertEquals(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED.hashCode(), copy.hashCode());
		assertNotEquals(copy, SemanticWidthEvidenceFacts.HERB_WORKFLOW);
		assertTrue(copy.toString().contains("eligible=7"));
	}

	private static WidthEvidence evidence(int eligibleTemplates, List<Integer> templateSupport,
		List<Integer> familySupport)
	{
		return new WidthEvidence(eligibleTemplates, templateSupport, familySupport);
	}

	private static List<Integer> values(Integer... values)
	{
		return Arrays.asList(values);
	}

	private static void assertNoPreferredWidth(WidthEvidence evidence)
	{
		try
		{
			evidence.getPreferredWidth();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException expected)
		{
			// expected
		}
	}

	private static void assertEvidenceFails(Runnable construction)
	{
		try
		{
			construction.run();
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void assertImmutable(List<?> values)
	{
		try
		{
			((List) values).add(null);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}
}

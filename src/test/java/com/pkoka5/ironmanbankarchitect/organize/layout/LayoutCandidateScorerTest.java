package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class LayoutCandidateScorerTest
{
	@Test
	public void gemStageMatrixGetsExactWidthRegretAndCanonicalFragmentation()
	{
		SemanticRule rule = rule("gem.workflow", ShapePrimitive.STAGE_MATRIX, widths(2, 5),
			SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED,
			atom("gem.a", 10, 11), atom("gem.b", 20, 21), atom("gem.c", 30, 31),
			atom("gem.d", 40, 41), atom("gem.e", 50, 51));
		LayoutCandidateGroup group = onlyGroup(request(
			Arrays.asList(10, 11, 20, 21, 30, 31, 40, 41, 50, 51), rule));

		LayoutScore widthTwo = LayoutCandidateScorer.score(group, candidate(group, 2));
		LayoutScore widthFive = LayoutCandidateScorer.score(group, candidate(group, 5));
		int[] expectedWidthTwo = new int[LayoutScore.COMPONENT_COUNT];
		expectedWidthTwo[7] = 429;
		expectedWidthTwo[8] = 2;

		assertArrayEquals(expectedWidthTwo, widthTwo.toComponentArray());
		assertEquals(LayoutScore.zero(), widthFive);
		assertEquals(1, LayoutCandidateScorer.widthPreferenceRank(group, candidate(group, 2)));
		assertEquals(0, LayoutCandidateScorer.widthPreferenceRank(group, candidate(group, 5)));
	}

	@Test
	public void herbRowGroupUsesEvidenceWithoutInventingFragmentation()
	{
		SemanticRule rule = rule("herb.workflow", ShapePrimitive.ROW_GROUP_MATRIX, widths(2, 3, 4),
			SemanticWidthEvidenceFacts.HERB_WORKFLOW,
			atom("herb.guam", 10, 11), atom("herb.marrentill", 20, 21));
		LayoutCandidateGroup group = onlyGroup(request(Arrays.asList(10, 11, 20, 21), rule));

		assertEquals(715, LayoutCandidateScorer.score(group, candidate(group, 2)).getWidthEvidenceRegret());
		assertEquals(0, LayoutCandidateScorer.score(group, candidate(group, 3)).getWidthEvidenceRegret());
		assertEquals(715, LayoutCandidateScorer.score(group, candidate(group, 4)).getWidthEvidenceRegret());
		assertEquals(0, LayoutCandidateScorer.score(group, candidate(group, 2)).getSemanticFragmentation());
		assertEquals(1, LayoutCandidateScorer.widthPreferenceRank(group, candidate(group, 2)));
		assertEquals(0, LayoutCandidateScorer.widthPreferenceRank(group, candidate(group, 3)));
		assertEquals(1, LayoutCandidateScorer.widthPreferenceRank(group, candidate(group, 4)));
	}

	@Test
	public void absentOrInactiveEvidenceLeavesAllCandidateLocalScoresNeutral()
	{
		SemanticRule withoutEvidence = rule("plain.run", ShapePrimitive.HORIZONTAL_RUN, widths(2, 3), null,
			atom("plain.family", 10, 11));
		LayoutCandidateGroup plainGroup = onlyGroup(request(Arrays.asList(10, 11), withoutEvidence));
		assertEquals(LayoutScore.zero(), LayoutCandidateScorer.score(plainGroup, candidate(plainGroup, 2)));
		assertEquals(0, LayoutCandidateScorer.widthPreferenceRank(plainGroup, candidate(plainGroup, 3)));

		WidthEvidence inactive = new WidthEvidence(6,
			Arrays.asList(0, 2, 3, 2, 1, 0, 0, 0),
			Arrays.asList(0, 2, 6, 5, 5, 0, 0, 0));
		SemanticRule inactiveRule = rule("metal.stage", ShapePrimitive.STAGE_MATRIX, widths(2, 3), inactive,
			atom("metal.iron", 20, 21), atom("metal.steel", 30, 31));
		LayoutCandidateGroup inactiveGroup = onlyGroup(
			request(Arrays.asList(20, 21, 30, 31), inactiveRule));
		assertEquals(LayoutScore.zero(),
			LayoutCandidateScorer.score(inactiveGroup, candidate(inactiveGroup, 2)));
		assertEquals(0,
			LayoutCandidateScorer.widthPreferenceRank(inactiveGroup, candidate(inactiveGroup, 3)));
	}

	@Test
	public void scorerRejectsCandidateFromAnotherGroup()
	{
		SemanticRule firstRule = rule("a.run", ShapePrimitive.HORIZONTAL_RUN, widths(2), null,
			atom("a.family", 10, 11));
		SemanticRule secondRule = rule("b.run", ShapePrimitive.HORIZONTAL_RUN, widths(2), null,
			atom("b.family", 20, 21));
		LayoutCandidateGroup first = onlyGroup(request(Arrays.asList(10, 11), firstRule));
		LayoutCandidateGroup second = onlyGroup(request(Arrays.asList(20, 21), secondRule));

		assertScoreFails(() -> LayoutCandidateScorer.score(first, candidate(second, 2)));
		assertScoreFails(() -> LayoutCandidateScorer.widthPreferenceRank(first, candidate(second, 2)));
	}

	@Test
	public void fallbackFactsPopulateOnlyTheirExactConfidenceTier()
	{
		for (ConfidenceTier tier : ConfidenceTier.values())
		{
			SemanticRule rule = rule("fallback." + tier.ordinal(),
				ShapePrimitive.HORIZONTAL_RUN, widths(3), null, tier,
				atom("fallback.family", 10, 20, 30, 40));
			LayoutCandidateGroup group = onlyGroup(request(Arrays.asList(10, 20, 30), rule));
			int[] expected = new int[LayoutScore.COMPONENT_COUNT];
			int offset = tier == ConfidenceTier.HIGH ? 0 : tier == ConfidenceTier.MEDIUM ? 2 : 4;
			expected[offset] = 2;
			expected[offset + 1] = 750;

			assertArrayEquals(expected, LayoutCandidateScorer.scoreFallback(group).toComponentArray());
		}
	}

	private static LayoutCandidateGroup onlyGroup(LayoutRequest request)
	{
		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(request);
		assertEquals(1, groups.size());
		return groups.get(0);
	}

	private static LayoutCandidate candidate(LayoutCandidateGroup group, int width)
	{
		for (LayoutCandidate candidate : group.getCandidates())
		{
			if (candidate.getWidth() == width)
			{
				return candidate;
			}
		}
		throw new AssertionError("missing width " + width);
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive, Set<Integer> allowedWidths,
		WidthEvidence evidence, SemanticAtom... atoms)
	{
		return rule(ruleKey, primitive, allowedWidths, evidence, ConfidenceTier.HIGH, atoms);
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive, Set<Integer> allowedWidths,
		WidthEvidence evidence, ConfidenceTier confidenceTier, SemanticAtom... atoms)
	{
		SemanticRule.Builder builder = SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(Arrays.asList(atoms))
			.confidenceTier(confidenceTier)
			.shapePrimitive(primitive)
			.allowedWidths(allowedWidths);
		if (evidence != null)
		{
			builder.widthEvidence(evidence);
		}
		return builder.build();
	}

	private static SemanticAtom atom(String atomKey, int... itemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			members.add(new SemanticAtom.Member("stage." + index, itemIds[index]));
		}
		return new SemanticAtom(atomKey, members);
	}

	private static LayoutRequest request(List<Integer> itemIds, SemanticRule rule)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			int itemId = itemIds.get(index);
			entries.add(LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), index));
		}
		return new LayoutRequest(entries, Collections.singletonList(rule));
	}

	private static Set<Integer> widths(int... values)
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int value : values)
		{
			result.add(value);
		}
		return result;
	}

	private static void assertScoreFails(Runnable scoring)
	{
		try
		{
			scoring.run();
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}
}

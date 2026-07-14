package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import org.junit.Test;

public class PlacedBlockTest
{
	@Test
	public void derivesEverySemanticFactFromOneCandidate()
	{
		LayoutCandidateGroup group = group("gem.workflow", "gem.family", 2, 3, 10, 20);
		LayoutCandidate candidate = LayoutTestFixtures.candidateAtWidth(group, 3);
		PlacedBlock block = PlacedBlock.place(group, candidate, 4, 5);

		assertEquals(candidate.getRuleKey(), block.getRuleKey());
		assertEquals(candidate.getAtomKeys(), block.getAtomKeys());
		assertEquals(candidate.getWidth(), block.getWidth());
		assertEquals(candidate.getShapePrimitive(), block.getShapePrimitive());
		assertEquals(candidate.getRows(), block.getRows());
		assertSame(candidate.getRows().get(0), block.getRows().get(0));
		assertEquals(2, block.getWidthPreferenceRank());
		assertEquals(4, block.getStartRow());
		assertEquals(5, block.getStartColumn());
	}

	@Test
	public void equalityAndHashCoverCandidatePreferenceAndOrigin()
	{
		LayoutCandidateGroup group = group("gem.workflow", "gem.family", 0, 2, 10, 20);
		LayoutCandidate candidate = LayoutTestFixtures.candidateAtWidth(group, 2);
		PlacedBlock base = PlacedBlock.place(group, candidate, 1, 2);

		assertEquals(base, PlacedBlock.place(group, candidate, 1, 2));
		assertEquals(base.hashCode(), PlacedBlock.place(group, candidate, 1, 2).hashCode());
	}

	@Test
	public void rejectsInvalidOriginOrCandidateFromAnotherGroup()
	{
		LayoutCandidateGroup group = group("gem.workflow", "gem.family", 0, 3, 10, 20);
		LayoutCandidate candidate = LayoutTestFixtures.candidateAtWidth(group, 3);

		assertBlockFails(group, candidate, -1, 0);
		assertBlockFails(group, candidate, 0, -1);
		assertBlockFails(group, candidate, 0, 8);
		assertBlockFails(group, candidate, 0, 6);

		LayoutCandidateGroup other = group("other.workflow", "other.family", 0, 3, 30, 40);
		assertBlockFails(group, LayoutTestFixtures.candidateAtWidth(other, 3), 0, 0);
	}

	@Test(expected = NullPointerException.class)
	public void rejectsNullGroup()
	{
		PlacedBlock.place(null, null, 0, 0);
	}

	private static LayoutCandidateGroup group(String ruleKey, String atomKey, int preferenceRank,
		int width, Integer... itemIds)
	{
		return LayoutTestFixtures.candidateGroup(ruleKey, atomKey, preferenceRank, width,
			ShapePrimitive.HORIZONTAL_RUN, row(0, itemIds));
	}

	private static LayoutCandidate.Row row(int offset, Integer... ids)
	{
		return new LayoutCandidate.Row(offset, Arrays.asList(ids));
	}

	private static void assertBlockFails(LayoutCandidateGroup group, LayoutCandidate candidate,
		int row, int column)
	{
		try
		{
			PlacedBlock.place(group, candidate, row, column);
			fail("expected construction failure");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}
}

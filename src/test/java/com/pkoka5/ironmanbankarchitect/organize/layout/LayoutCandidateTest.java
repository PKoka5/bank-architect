package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class LayoutCandidateTest
{
	@Test
	public void buildsExplicitRaggedStageMatrixWithoutPhantomCells()
	{
		LayoutCandidate candidate = candidate("gem.workflow", ShapePrimitive.STAGE_MATRIX, 3,
			Arrays.asList("gem.sapphire", "gem.emerald", "gem.ruby", "gem.diamond", "gem.dragonstone"),
			row(0, 10, 20, 30), row(0, 11, 21, 31), row(0, 40, 50), row(0, 41, 51));

		assertEquals(3, candidate.getWidth());
		assertEquals(Arrays.asList(3, 3, 2, 2), rowLengths(candidate));
		assertEquals(Arrays.asList(10, 20, 30, 11, 21, 31, 40, 50, 41, 51),
			candidate.getRowMajorItemIds());
		assertEquals(10, candidate.getRowMajorItemIds().size());
	}

	@Test
	public void rowBoundariesAndOffsetsArePartOfStableIdentity()
	{
		LayoutCandidate twoThenOne = candidate("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
			Collections.singletonList("rune.group"), row(0, 1, 2), row(0, 3));
		LayoutCandidate oneThenTwo = candidate("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
			Collections.singletonList("rune.group"), row(0, 1), row(0, 2, 3));
		LayoutCandidate shifted = candidate("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
			Collections.singletonList("rune.group"), row(1, 1), row(0, 2, 3));

		assertEquals(twoThenOne.getRowMajorItemIds(), oneThenTwo.getRowMajorItemIds());
		assertNotEquals(twoThenOne, oneThenTwo);
		assertNotEquals(oneThenTwo, shifted);
		assertEquals(twoThenOne, candidate("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
			Collections.singletonList("rune.group"), row(0, 1, 2), row(0, 3)));
		assertEquals(twoThenOne.hashCode(), candidate("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
			Collections.singletonList("rune.group"), row(0, 1, 2), row(0, 3)).hashCode());
	}

	@Test
	public void everyIdentityFieldMatters()
	{
		LayoutCandidate base = candidate("potion.doses", ShapePrimitive.HORIZONTAL_RUN, 4,
			Collections.singletonList("potion.prayer"), row(0, 40, 30));

		assertNotEquals(base, candidate("potion.other", ShapePrimitive.HORIZONTAL_RUN, 4,
			Collections.singletonList("potion.prayer"), row(0, 40, 30)));
		assertNotEquals(base, candidate("potion.doses", ShapePrimitive.VERTICAL_RUN, 4,
			Collections.singletonList("potion.prayer"), row(0, 40, 30)));
		assertNotEquals(base, candidate("potion.doses", ShapePrimitive.HORIZONTAL_RUN, 5,
			Collections.singletonList("potion.prayer"), row(0, 40, 30)));
		assertNotEquals(base, candidate("potion.doses", ShapePrimitive.HORIZONTAL_RUN, 4,
			Collections.singletonList("potion.restore"), row(0, 40, 30)));
		assertNotEquals(base, candidate("potion.doses", ShapePrimitive.HORIZONTAL_RUN, 4,
			Collections.singletonList("potion.prayer"), row(0, 30, 40)));
	}

	@Test
	public void rejectsMalformedGeometryAndIdentity()
	{
		assertCandidateFails("Bad Key", 2, Collections.singletonList("atom.one"), rows(row(0, 1)));
		assertCandidateFails("rule.one", 0, Collections.singletonList("atom.one"), rows(row(0, 1)));
		assertCandidateFails("rule.one", 9, Collections.singletonList("atom.one"), rows(row(0, 1)));
		assertCandidateFails("rule.one", 2, null, rows(row(0, 1)));
		assertCandidateFails("rule.one", 2, Collections.emptyList(), rows(row(0, 1)));
		assertCandidateFails("rule.one", 2, Arrays.asList("atom.one", "atom.one"), rows(row(0, 1)));
		assertCandidateFails("rule.one", 2, Collections.singletonList("Bad Key"), rows(row(0, 1)));
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"), null);
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"), Collections.emptyList());
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"), Arrays.asList(row(0, 1), null));
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"), rows(row(1, 1, 2)));
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"), rows(row(0, 1), row(0, 1)));
		assertCandidateFails("rule.one", 2, Collections.singletonList("atom.one"),
			rows(row(Integer.MAX_VALUE, 1)));
	}

	@Test
	public void rowsRejectInvalidItemContent()
	{
		assertRowFails(-1, Arrays.asList(1));
		assertRowFails(0, null);
		assertRowFails(0, Collections.emptyList());
		assertRowFails(0, Arrays.asList(1, null));
		assertRowFails(0, Arrays.asList(1, 0));
		assertRowFails(0, Arrays.asList(1, -4));
		assertRowFails(0, Arrays.asList(1, ItemID.BANK_FILLER));
	}

	@Test
	public void candidateAndRowsDefensivelyCopyCollections()
	{
		List<String> atomKeys = new ArrayList<>(Collections.singletonList("atom.one"));
		List<Integer> itemIds = new ArrayList<>(Arrays.asList(1, 2));
		LayoutCandidate.Row row = new LayoutCandidate.Row(0, itemIds);
		List<LayoutCandidate.Row> rows = new ArrayList<>(Collections.singletonList(row));
		LayoutCandidate candidate = new LayoutCandidate("rule.one", ShapePrimitive.HORIZONTAL_RUN, 2,
			atomKeys, rows);

		atomKeys.add("atom.two");
		itemIds.add(3);
		rows.add(new LayoutCandidate.Row(0, Collections.singletonList(4)));
		assertEquals(Collections.singletonList("atom.one"), candidate.getAtomKeys());
		assertEquals(Arrays.asList(1, 2), candidate.getRows().get(0).getItemIds());
		assertEquals(1, candidate.getRows().size());

		assertImmutable(candidate.getAtomKeys());
		assertImmutable(candidate.getRows());
		assertImmutable(candidate.getRows().get(0).getItemIds());
	}

	private static LayoutCandidate candidate(String ruleKey, ShapePrimitive primitive, int width,
		List<String> atomKeys, LayoutCandidate.Row... rows)
	{
		return new LayoutCandidate(ruleKey, primitive, width, atomKeys, Arrays.asList(rows));
	}

	private static LayoutCandidate.Row row(int offset, Integer... itemIds)
	{
		return new LayoutCandidate.Row(offset, Arrays.asList(itemIds));
	}

	private static List<LayoutCandidate.Row> rows(LayoutCandidate.Row... rows)
	{
		return Arrays.asList(rows);
	}

	private static List<Integer> rowLengths(LayoutCandidate candidate)
	{
		List<Integer> lengths = new ArrayList<>();
		for (LayoutCandidate.Row row : candidate.getRows())
		{
			lengths.add(row.length());
		}
		return lengths;
	}

	private static void assertCandidateFails(String ruleKey, int width, List<String> atomKeys,
		List<LayoutCandidate.Row> rows)
	{
		try
		{
			new LayoutCandidate(ruleKey, ShapePrimitive.HORIZONTAL_RUN, width, atomKeys, rows);
			fail("expected construction failure");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	private static void assertRowFails(int offset, List<Integer> itemIds)
	{
		try
		{
			new LayoutCandidate.Row(offset, itemIds);
			fail("expected construction failure");
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

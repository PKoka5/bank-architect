package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class DeterministicTieKeyTest
{
	@Test
	public void completeStableBlockIdentityDecidesBeforeEveryGeometryField()
	{
		DeterministicTieKey alphaRule = key(Collections.singletonList(
			block("alpha.rule", "atom.z", 0, 2, ShapePrimitive.HORIZONTAL_RUN, 9, 0,
				row(0, 99, 100))), 99, 100);
		DeterministicTieKey betaRule = key(Collections.singletonList(
			block("beta.rule", "atom.a", 0, 2, ShapePrimitive.HORIZONTAL_RUN, 0, 0,
				row(0, 1, 2))), 1, 2);
		assertTrue(alphaRule.compareTo(betaRule) < 0);

		DeterministicTieKey alphaAtom = key(Collections.singletonList(
			block("same.rule", "atom.alpha", 0, 2, ShapePrimitive.HORIZONTAL_RUN, 9, 0,
				row(0, 99, 100))), 99, 100);
		DeterministicTieKey betaAtom = key(Collections.singletonList(
			block("same.rule", "atom.beta", 0, 2, ShapePrimitive.HORIZONTAL_RUN, 0, 0,
				row(0, 1, 2))), 1, 2);
		assertTrue("atom identity must decide before width", alphaAtom.compareTo(betaAtom) < 0);
	}

	@Test
	public void componentVectorsAreComparedInDocumentOrder()
	{
		PlacedBlock preferredWide = block("same.rule", "atom.one", 0, 3,
			ShapePrimitive.HORIZONTAL_RUN, 5, 5, row(0, 10, 11));
		PlacedBlock unpreferredNarrow = block("same.rule", "atom.one", 1, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		assertTrue(key(Collections.singletonList(preferredWide), 10, 11)
			.compareTo(key(Collections.singletonList(unpreferredNarrow), 10, 11)) < 0);

		PlacedBlock narrower = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 5, 4, row(0, 10, 11));
		PlacedBlock wider = block("same.rule", "atom.one", 0, 3,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		assertTrue(key(Collections.singletonList(narrower), 10, 11)
			.compareTo(key(Collections.singletonList(wider), 10, 11)) < 0);

		PlacedBlock horizontal = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 5, 4, row(0, 10, 11));
		PlacedBlock rowGroup = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.ROW_GROUP_MATRIX, 0, 0, row(0, 10, 11));
		assertTrue(key(Collections.singletonList(horizontal), 10, 11)
			.compareTo(key(Collections.singletonList(rowGroup), 10, 11)) < 0);

		PlacedBlock earlierRow = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 4, row(0, 10, 11));
		PlacedBlock laterRow = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 2, 0, row(0, 10, 11));
		assertTrue(key(Collections.singletonList(earlierRow), 10, 11)
			.compareTo(key(Collections.singletonList(laterRow), 10, 11)) < 0);

		PlacedBlock earlierColumn = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 2, row(0, 10, 11));
		PlacedBlock laterColumn = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 3, row(0, 10, 11));
		assertTrue(key(Collections.singletonList(earlierColumn), 10, 11)
			.compareTo(key(Collections.singletonList(laterColumn), 10, 11)) < 0);
	}

	@Test
	public void explicitGeometryDecidesBeforeOpposingFinalItemVector()
	{
		PlacedBlock leftBlock = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 20));
		PlacedBlock rightBlock = block("same.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 21));

		DeterministicTieKey left = new DeterministicTieKey(Collections.singletonList(leftBlock),
			Arrays.asList(99, 10, 20));
		DeterministicTieKey right = new DeterministicTieKey(Collections.singletonList(rightBlock),
			Arrays.asList(1, 10, 21));

		assertTrue("row geometry must decide before final order", left.compareTo(right) < 0);
	}

	@Test
	public void finalTargetVectorIsTheLastTieBreak()
	{
		DeterministicTieKey left = key(Collections.emptyList(), 10, 20, 30);
		DeterministicTieKey right = key(Collections.emptyList(), 10, 20, 31);
		DeterministicTieKey shorter = key(Collections.emptyList(), 10, 20);

		assertTrue(left.compareTo(right) < 0);
		assertTrue(shorter.compareTo(left) < 0);
	}

	@Test
	public void secondBlockWidthStartAndGeometryRemainObservable()
	{
		PlacedBlock first = block("alpha.rule", "atom.first", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		PlacedBlock secondBase = block("beta.rule", "atom.second", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 0, row(0, 20, 21));
		DeterministicTieKey base = key(Arrays.asList(first, secondBase), 10, 11, 20, 21);

		PlacedBlock secondWider = block("beta.rule", "atom.second", 0, 3,
			ShapePrimitive.HORIZONTAL_RUN, 1, 0, row(0, 20, 21));
		assertTrue(base.compareTo(key(Arrays.asList(first, secondWider), 10, 11, 20, 21)) < 0);

		PlacedBlock secondLater = block("beta.rule", "atom.second", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 2, 0, row(0, 20, 21));
		assertTrue(base.compareTo(key(Arrays.asList(first, secondLater), 10, 11, 20, 21)) < 0);

		PlacedBlock secondGeometry = block("beta.rule", "atom.second", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 0, row(0, 20, 22));
		assertTrue(base.compareTo(key(Arrays.asList(first, secondGeometry), 10, 11, 20, 22)) < 0);
	}

	@Test
	public void blockInputOrderIsCanonicalizedByStableIdentity()
	{
		PlacedBlock alpha = block("alpha.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		PlacedBlock beta = block("beta.rule", "atom.two", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 2, row(0, 20, 21));

		DeterministicTieKey forward = key(Arrays.asList(alpha, beta), 10, 11, 20, 21);
		DeterministicTieKey reversed = key(Arrays.asList(beta, alpha), 10, 11, 20, 21);

		assertEquals(Arrays.asList(alpha, beta), forward.getBlocks());
		assertEquals(forward, reversed);
		assertEquals(forward.hashCode(), reversed.hashCode());
		assertEquals(0, forward.compareTo(reversed));
	}

	@Test
	public void equalKeysCompareZeroAndComparatorIsTransitive()
	{
		DeterministicTieKey first = key(Collections.emptyList(), 1);
		DeterministicTieKey second = key(Collections.emptyList(), 2);
		DeterministicTieKey third = key(Collections.emptyList(), 3);

		assertEquals(first, key(Collections.emptyList(), 1));
		assertEquals(first.hashCode(), key(Collections.emptyList(), 1).hashCode());
		assertEquals(0, first.compareTo(key(Collections.emptyList(), 1)));
		assertTrue(first.compareTo(second) < 0);
		assertTrue(second.compareTo(third) < 0);
		assertTrue(first.compareTo(third) < 0);
	}

	@Test
	public void pureFallbackUsesEmptyBlockVector()
	{
		DeterministicTieKey fallback = key(Collections.emptyList(), 10, 20);

		assertTrue(fallback.getBlocks().isEmpty());
		assertEquals(Arrays.asList(10, 20), fallback.getFinalTargetOrderItemIds());
	}

	@Test
	public void rejectsInvalidCompletePlanFacts()
	{
		assertKeyFails(null, Arrays.asList(1));
		assertKeyFails(Arrays.asList((PlacedBlock) null), Arrays.asList(1));
		assertKeyFails(Collections.emptyList(), null);
		assertKeyFails(Collections.emptyList(), Arrays.asList(0));
		assertKeyFails(Collections.emptyList(), Arrays.asList(1, 1));
		assertKeyFails(Collections.emptyList(), Arrays.asList(ItemID.BANK_FILLER));

		PlacedBlock block = block("alpha.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		assertKeyFails(Collections.singletonList(block), Arrays.asList(20));
		assertKeyFails(Arrays.asList(block, block("beta.rule", "atom.two", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 1, row(0, 10, 11))), Arrays.asList(10, 11));
		assertKeyFails(Arrays.asList(block, block("alpha.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 1, 0, row(0, 20, 21))),
			Arrays.asList(10, 11, 20, 21));
	}

	@Test
	public void collectionsAreDetachedAndImmutable()
	{
		PlacedBlock block = block("alpha.rule", "atom.one", 0, 2,
			ShapePrimitive.HORIZONTAL_RUN, 0, 0, row(0, 10, 11));
		List<PlacedBlock> blocks = new ArrayList<>(Collections.singletonList(block));
		List<Integer> finalIds = new ArrayList<>(Arrays.asList(10, 11));
		DeterministicTieKey key = new DeterministicTieKey(blocks, finalIds);
		blocks.clear();
		finalIds.clear();

		assertEquals(1, key.getBlocks().size());
		assertEquals(Arrays.asList(10, 11), key.getFinalTargetOrderItemIds());
		assertImmutable(key.getBlocks());
		assertImmutable(key.getFinalTargetOrderItemIds());
	}

	private static DeterministicTieKey key(List<PlacedBlock> blocks, Integer... finalIds)
	{
		return new DeterministicTieKey(blocks, Arrays.asList(finalIds));
	}

	private static PlacedBlock block(String ruleKey, String atomKey, int preferenceRank, int width,
		ShapePrimitive primitive, int startRow, int startColumn, LayoutCandidate.Row... rows)
	{
		return LayoutTestFixtures.placedBlock(ruleKey, atomKey, preferenceRank, width, primitive,
			startRow, startColumn, rows);
	}

	private static LayoutCandidate.Row row(int offset, Integer... itemIds)
	{
		return new LayoutCandidate.Row(offset, Arrays.asList(itemIds));
	}

	private static void assertKeyFails(List<PlacedBlock> blocks, List<Integer> finalIds)
	{
		try
		{
			new DeterministicTieKey(blocks, finalIds);
			fail("expected construction failure");
		}
		catch (IllegalArgumentException | NullPointerException expected)
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

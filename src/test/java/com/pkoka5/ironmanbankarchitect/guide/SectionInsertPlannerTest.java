package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.SectionInsertPlanner.Step;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SectionInsertPlannerTest
{
	@Test
	public void sortedSectionNeedsNoStep()
	{
		assertNull(SectionInsertPlanner.nextStep(new int[]{1, 2, 3, 4}, 0, items(1, 2, 3, 4)));
		assertEquals(0,
			SectionInsertPlanner.minimumRemainingInserts(new int[]{1, 2, 3, 4}, 0, items(1, 2, 3, 4)));
	}

	@Test
	public void equalChargedCopiesAreInterchangeableInInsertMode()
	{
		int usedEclipseMoonChestplate = 29031;
		int[] actual = {usedEclipseMoonChestplate, 20, usedEclipseMoonChestplate};
		List<BankPreviewItem> target = items(
			usedEclipseMoonChestplate, usedEclipseMoonChestplate, 20);

		assertEquals(1, SectionInsertPlanner.minimumRemainingInserts(actual, 0, target));
		Step step = SectionInsertPlanner.nextStep(actual, 0, target);

		assertNotNull(step);
		assertArrayEquals(new int[]{usedEclipseMoonChestplate, usedEclipseMoonChestplate, 20},
			SectionInsertPlanner.applyInsert(actual, step.getFromSlot(), step.getDropSlot()));
	}

	@Test
	public void singleMisplacedItemIsDroppedInFrontOfItsSuccessor()
	{
		// 3 sits in front of 1 and 2; dropping it on slot 2 shifts 1 and 2 left.
		int[] actual = {3, 1, 2, 4};
		Step step = SectionInsertPlanner.nextStep(actual, 0, items(1, 2, 3, 4));

		assertNotNull(step);
		assertEquals(3, step.getItem().getItemId());
		assertEquals(0, step.getFromSlot());
		assertEquals(2, step.getDropSlot());
		assertEquals(2, step.getAnchorItemId());
		assertArrayEquals(new int[]{1, 2, 3, 4},
			SectionInsertPlanner.applyInsert(actual, step.getFromSlot(), step.getDropSlot()));
	}

	@Test
	public void dragToTheLeftDropsDirectlyOnTheAnchorItem()
	{
		// 1 must end up in front of 2, and dropping it on slot 0 pushes 2 right.
		int[] actual = {2, 3, 1, 4};
		Step step = SectionInsertPlanner.nextStep(actual, 0, items(1, 2, 3, 4));

		assertNotNull(step);
		assertEquals(1, step.getItem().getItemId());
		assertEquals(2, step.getFromSlot());
		assertEquals(0, step.getDropSlot());
		assertEquals(2, step.getAnchorItemId());
		assertArrayEquals(new int[]{1, 2, 3, 4},
			SectionInsertPlanner.applyInsert(actual, step.getFromSlot(), step.getDropSlot()));
	}

	@Test
	public void sectionOffsetIsRespectedAndSlotsAreAbsolute()
	{
		int[] actual = {77, 88, 3, 1, 2};
		Step step = SectionInsertPlanner.nextStep(actual, 2, items(1, 2, 3));

		assertNotNull(step);
		assertEquals(2, step.getFromSlot());
		assertEquals(4, step.getDropSlot());
		assertArrayEquals(new int[]{77, 88, 1, 2, 3},
			SectionInsertPlanner.applyInsert(actual, step.getFromSlot(), step.getDropSlot()));
	}

	@Test
	public void unknownSectionContentsFailClosed()
	{
		assertNull(SectionInsertPlanner.nextStep(new int[]{1, 2, 99}, 0, items(1, 2, 3)));
		assertNull(SectionInsertPlanner.nextStep(new int[]{1, 2}, 0, items(1, 2, 3)));
		assertNull(SectionInsertPlanner.nextStep(new int[]{1, 2, 3}, -1, items(1, 2, 3)));
	}

	@Test
	public void everyPermutationTerminatesInExactlyTheLowerBoundNumberOfDrags()
	{
		List<BankPreviewItem> target = items(1, 2, 3, 4, 5, 6);
		for (List<Integer> permutation : permutations(Arrays.asList(1, 2, 3, 4, 5, 6)))
		{
			int[] actual = toArray(permutation);
			int expected = SectionInsertPlanner.minimumRemainingInserts(actual, 0, target);
			assertEquals(expected, bruteForceMinimumInserts(actual, target));

			int drags = 0;
			Step step;
			while ((step = SectionInsertPlanner.nextStep(actual, 0, target)) != null)
			{
				actual = SectionInsertPlanner.applyInsert(actual, step.getFromSlot(),
					step.getDropSlot());
				drags++;
				assertTrue("guidance must terminate", drags <= 6);
			}

			assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, actual);
			assertEquals(expected, drags);
		}
	}

	@Test
	public void insertsBeatSwapsOnAShuffledSection()
	{
		// Two interleaved runs: swaps must break both cycles, inserts only have
		// to lift the three items outside the longest increasing subsequence.
		int[] actual = {4, 1, 5, 2, 6, 3, 7, 8};
		List<BankPreviewItem> target = items(1, 2, 3, 4, 5, 6, 7, 8);

		assertEquals(3, SectionInsertPlanner.minimumRemainingInserts(actual, 0, target));
		assertEquals(4, TabRouteAdvisor.estimatedRemainingSwaps(actual, target));
	}

	@Test
	public void longestIncreasingSubsequenceIsDeterministicAndAscending()
	{
		int[] indices = SectionInsertPlanner.longestIncreasingSubsequence(
			new int[]{2, 0, 1, 5, 3, 4});

		assertArrayEquals(new int[]{1, 2, 4, 5}, indices);
		assertArrayEquals(indices,
			SectionInsertPlanner.longestIncreasingSubsequence(new int[]{2, 0, 1, 5, 3, 4}));
	}

	@Test
	public void applyInsertMovesInBothDirections()
	{
		assertArrayEquals(new int[]{2, 3, 1},
			SectionInsertPlanner.applyInsert(new int[]{1, 2, 3}, 0, 2));
		assertArrayEquals(new int[]{3, 1, 2},
			SectionInsertPlanner.applyInsert(new int[]{1, 2, 3}, 2, 0));
		assertArrayEquals(new int[]{1, 2, 3},
			SectionInsertPlanner.applyInsert(new int[]{1, 2, 3}, 1, 1));
	}

	/** Independent lower bound: section size minus the longest increasing run kept in place. */
	private static int bruteForceMinimumInserts(int[] actualItemIds, List<BankPreviewItem> target)
	{
		int size = actualItemIds.length;
		int best = 0;
		for (int mask = 0; mask < 1 << size; mask++)
		{
			int previous = -1;
			int kept = 0;
			boolean increasing = true;
			for (int position = 0; position < size && increasing; position++)
			{
				if ((mask & 1 << position) == 0)
				{
					continue;
				}
				int targetOffset = offsetOf(target, actualItemIds[position]);
				increasing = targetOffset > previous;
				previous = targetOffset;
				kept++;
			}
			if (increasing && kept > best)
			{
				best = kept;
			}
		}
		return size - best;
	}

	private static int offsetOf(List<BankPreviewItem> target, int itemId)
	{
		for (int offset = 0; offset < target.size(); offset++)
		{
			if (target.get(offset).getItemId() == itemId)
			{
				return offset;
			}
		}
		throw new IllegalArgumentException("unknown item " + itemId);
	}

	private static List<BankPreviewItem> items(int... itemIds)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}
		return items;
	}

	private static int[] toArray(List<Integer> values)
	{
		int[] result = new int[values.size()];
		for (int index = 0; index < values.size(); index++)
		{
			result[index] = values.get(index);
		}
		return result;
	}

	private static List<List<Integer>> permutations(List<Integer> values)
	{
		List<List<Integer>> results = new ArrayList<>();
		permute(new ArrayList<>(values), 0, results);
		return results;
	}

	private static void permute(List<Integer> values, int index, List<List<Integer>> results)
	{
		if (index == values.size())
		{
			results.add(new ArrayList<>(values));
			return;
		}
		for (int position = index; position < values.size(); position++)
		{
			java.util.Collections.swap(values, index, position);
			permute(values, index + 1, results);
			java.util.Collections.swap(values, index, position);
		}
	}
}

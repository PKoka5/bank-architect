package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class GuideSortingEfficiencyTest
{
	@Test
	public void partialTabDistributionRespectsTheSelectedSortingMode()
	{
		int[] actual = {1, 2, 3, 6, 999, 4, 5};
		BankTabPlan plan = plan(items(1, 2, 3, 4, 5, 6));
		Assessment insert = TabRouteAdvisor.assess(actual, plan, counts(4), 0,
			RearrangeMode.INSERT);
		Assessment swap = TabRouteAdvisor.assess(actual, plan, counts(4), 0,
			RearrangeMode.SWAP);

		assertEquals(Status.READY, insert.getStatus());
		assertEquals(MoveType.DISTRIBUTE_TO_TAB, insert.getMove().get().getType());
		assertEquals(4, insert.getMove().get().getItemId());
		assertEquals(5, insert.getMove().get().getFromSlot());
		assertEquals(5, swap.getMove().get().getItemId());
		assertEquals(1, independentMinimumInserts(new int[]{1, 2, 3, 6, 4, 5},
			new int[]{1, 2, 3, 4, 5, 6}));
		assertEquals(2, independentMinimumInserts(new int[]{1, 2, 3, 6, 5, 4},
			new int[]{1, 2, 3, 4, 5, 6}));
	}

	@Test
	public void everyFixedPrefixGetsAnInsertOptimalAppendTail()
	{
		List<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6);
		int[] targetIds = array(target);
		List<BankPreviewItem> targetItems = items(targetIds);
		BankTabPlan plan = plan(targetItems);
		Set<List<Integer>> checkedPrefixes = new HashSet<>();
		for (List<Integer> permutation : permutations(target))
		{
			for (int length = 1; length < target.size(); length++)
			{
				List<Integer> prefix = new ArrayList<>(permutation.subList(0, length));
				if (!checkedPrefixes.add(prefix)) continue;
				List<Integer> missing = new ArrayList<>(target);
				missing.removeAll(prefix);
				int optimum = target.size();
				for (List<Integer> tail : permutations(missing))
				{
					List<Integer> candidate = new ArrayList<>(prefix);
					candidate.addAll(tail);
					optimum = Math.min(optimum,
						independentMinimumInserts(array(candidate), targetIds));
				}

				List<Integer> bank = new ArrayList<>(prefix);
				bank.add(999);
				List<Integer> reversedMissing = new ArrayList<>(missing);
				Collections.reverse(reversedMissing);
				bank.addAll(reversedMissing);
				int filled = prefix.size();
				while (filled < target.size())
				{
					Assessment assessment = TabRouteAdvisor.assess(array(bank), plan,
						counts(filled), 0, RearrangeMode.INSERT);
					assertEquals("prefix " + prefix, Status.READY, assessment.getStatus());
					Move move = assessment.getMove().get();
					assertEquals(MoveType.DISTRIBUTE_TO_TAB, move.getType());
					assertEquals(1, move.getTargetTab());
					assertTrue(move.getFromSlot() >= filled);
					bank.add(filled, bank.remove(move.getFromSlot()));
					filled++;
				}
				assertEquals(Integer.valueOf(999), bank.get(target.size()));
				assertEquals("prefix " + prefix, optimum,
					independentMinimumInserts(array(bank.subList(0, target.size())), targetIds));
				assertEquals(optimum,
					SectionInsertPlanner.minimumRemainingInserts(array(bank), 0, targetItems));
			}
		}
		assertEquals(1236, checkedPrefixes.size());
	}

	@Test
	public void uniqueOccurrenceOffsetsAndInsertBoundsMatchIndependentSemantics()
	{
		List<Integer> target = Arrays.asList(41, 7, 88, 3, 19, 62);
		int[] targetIds = array(target);
		List<BankPreviewItem> targetItems = items(targetIds);
		for (List<Integer> permutation : permutations(target))
		{
			int[] section = array(permutation);
			int[] actual = new int[section.length + 3];
			actual[0] = 9000;
			actual[1] = 9001;
			System.arraycopy(section, 0, actual, 2, section.length);
			actual[actual.length - 1] = 9002;
			int[] expectedOffsets = new int[section.length];
			for (int index = 0; index < section.length; index++)
				expectedOffsets[index] = target.indexOf(section[index]);
			assertArrayEquals(expectedOffsets,
				ItemOccurrenceMatcher.orderedTargetOffsets(actual, 2, targetItems));
			int minimum = independentMinimumInserts(section, targetIds);
			assertEquals(minimum,
				SectionInsertPlanner.minimumRemainingInserts(actual, 2, targetItems));
			SectionInsertPlanner.Step step = SectionInsertPlanner.nextStep(actual, 2, targetItems);
			if (minimum == 0)
			{
				assertNull(step);
				continue;
			}
			assertNotNull(step);
			assertTrue(step.getFromSlot() >= 2 && step.getFromSlot() < 8);
			assertTrue(step.getDropSlot() >= 2 && step.getDropSlot() < 8);
			int[] after = SectionInsertPlanner.applyInsert(actual,
				step.getFromSlot(), step.getDropSlot());
			assertEquals(9000, after[0]);
			assertEquals(9001, after[1]);
			assertEquals(9002, after[after.length - 1]);
			assertEquals(minimum - 1,
				independentMinimumInserts(Arrays.copyOfRange(after, 2, 8), targetIds));
		}
	}

	@Test
	public void uniqueMatchingStillRejectsInvalidSectionsAndMultiplicityChanges()
	{
		List<BankPreviewItem> target = items(1, 2, 3);
		assertNull(ItemOccurrenceMatcher.orderedTargetOffsets(new int[]{1, 2, 3}, -1, target));
		assertNull(ItemOccurrenceMatcher.orderedTargetOffsets(new int[]{1, 2}, 0, target));
		assertNull(ItemOccurrenceMatcher.orderedTargetOffsets(new int[]{1, 2, 99}, 0, target));
		assertNull(ItemOccurrenceMatcher.orderedTargetOffsets(new int[]{1, 2, 2}, 0, target));
		assertNull(ItemOccurrenceMatcher.orderedTargetOffsets(new int[]{1}, 0, items()));
	}

	@Test
	public void thousandItemRotationKeepsTheLongRunAndMovesOnlyItsShortTail()
	{
		int size = 1000;
		int rotated = 137;
		int[] target = new int[size];
		int[] actual = new int[size];
		int[] expectedOffsets = new int[size];
		for (int index = 0; index < size; index++)
		{
			target[index] = index + 1;
			actual[index] = (index + rotated) % size + 1;
			expectedOffsets[index] = actual[index] - 1;
		}
		List<BankPreviewItem> targetItems = items(target);
		assertArrayEquals(expectedOffsets,
			ItemOccurrenceMatcher.orderedTargetOffsets(actual, 0, targetItems));
		assertEquals(rotated,
			SectionInsertPlanner.minimumRemainingInserts(actual, 0, targetItems));
		SectionInsertPlanner.Step step = SectionInsertPlanner.nextStep(actual, 0, targetItems);
		assertNotNull(step);
		assertEquals(1, step.getItem().getItemId());
		assertEquals(size - rotated, step.getFromSlot());
		assertEquals(0, step.getDropSlot());
		assertEquals(rotated - 1, SectionInsertPlanner.minimumRemainingInserts(
			SectionInsertPlanner.applyInsert(actual, step.getFromSlot(), step.getDropSlot()),
			0, targetItems));
	}

	// Independent common-subsequence bound, without occurrence mapping or LIS.
	private static int independentMinimumInserts(int[] actual, int[] target)
	{
		int[][] lengths = new int[actual.length + 1][target.length + 1];
		for (int current = 1; current <= actual.length; current++)
			for (int desired = 1; desired <= target.length; desired++)
				lengths[current][desired] = actual[current - 1] == target[desired - 1]
					? lengths[current - 1][desired - 1] + 1
					: Math.max(lengths[current - 1][desired], lengths[current][desired - 1]);
		return actual.length - lengths[actual.length][target.length];
	}

	private static BankTabPlan plan(List<BankPreviewItem> target)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++)
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index),
				index == 0 ? items(999) : index == 1 ? target : Collections.emptyList()));
		return BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories));
	}

	private static List<BankPreviewItem> items(int... ids)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		for (int id : ids) result.add(new BankPreviewItem(id, "Item " + id, 1));
		return result;
	}

	private static int[] counts(int first)
	{
		int[] result = new int[TabRouteAdvisor.MAX_TABS];
		result[0] = first;
		return result;
	}

	private static int[] array(List<Integer> values)
	{
		return values.stream().mapToInt(Integer::intValue).toArray();
	}

	private static List<List<Integer>> permutations(List<Integer> values)
	{
		List<List<Integer>> result = new ArrayList<>();
		permute(new ArrayList<>(values), 0, result);
		return result;
	}

	private static void permute(List<Integer> values, int position, List<List<Integer>> result)
	{
		if (position == values.size())
		{
			result.add(new ArrayList<>(values));
			return;
		}
		for (int selected = position; selected < values.size(); selected++)
		{
			Collections.swap(values, position, selected);
			permute(values, position + 1, result);
			Collections.swap(values, position, selected);
		}
	}
}

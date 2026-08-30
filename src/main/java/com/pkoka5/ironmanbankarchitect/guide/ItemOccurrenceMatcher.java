package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Matches interchangeable occurrences of one item ID to physical target slots. */
final class ItemOccurrenceMatcher
{
	private ItemOccurrenceMatcher()
	{
	}

	/**
	 * Returns the target offset assigned to every current offset. Occurrences
	 * already in a matching slot stay fixed, then the remaining equal-ID copies
	 * are paired in target order. Returns {@code null} when the section bounds or
	 * item multiplicities do not match the target.
	 */
	static int[] targetOffsets(int[] actualItemIds, int sectionStart,
		List<BankPreviewItem> targetItems)
	{
		int size = targetItems.size();
		if (sectionStart < 0 || size == 0 || sectionStart + size > actualItemIds.length)
		{
			return null;
		}

		int[] targetOffsets = new int[size];
		boolean[] assignedCurrent = new boolean[size];
		boolean[] assignedTarget = new boolean[size];
		for (int offset = 0; offset < size; offset++)
		{
			if (actualItemIds[sectionStart + offset] == targetItems.get(offset).getItemId())
			{
				targetOffsets[offset] = offset;
				assignedCurrent[offset] = true;
				assignedTarget[offset] = true;
			}
		}

		Map<Integer, ArrayDeque<Integer>> remainingTargetsByItemId = new HashMap<>();
		for (int offset = 0; offset < size; offset++)
		{
			if (!assignedTarget[offset])
			{
				remainingTargetsByItemId.computeIfAbsent(targetItems.get(offset).getItemId(),
					ignored -> new ArrayDeque<>()).addLast(offset);
			}
		}

		for (int offset = 0; offset < size; offset++)
		{
			if (assignedCurrent[offset])
			{
				continue;
			}
			ArrayDeque<Integer> targets = remainingTargetsByItemId.get(
				actualItemIds[sectionStart + offset]);
			if (targets == null || targets.isEmpty())
			{
				return null;
			}
			targetOffsets[offset] = targets.removeFirst();
		}
		for (ArrayDeque<Integer> targets : remainingTargetsByItemId.values())
		{
			if (!targets.isEmpty())
			{
				return null;
			}
		}
		return targetOffsets;
	}

	/**
	 * Matches the first current occurrence to the first target occurrence, and
	 * so on. This preserves equal-copy order and gives insert mode the longest
	 * subsequence that can stay in place.
	 */
	static int[] orderedTargetOffsets(int[] actualItemIds, int sectionStart,
		List<BankPreviewItem> targetItems)
	{
		int size = targetItems.size();
		if (sectionStart < 0 || size == 0 || sectionStart + size > actualItemIds.length)
		{
			return null;
		}

		Map<Integer, ArrayDeque<Integer>> targetsByItemId = new HashMap<>();
		for (int offset = 0; offset < size; offset++)
		{
			targetsByItemId.computeIfAbsent(targetItems.get(offset).getItemId(),
				ignored -> new ArrayDeque<>()).addLast(offset);
		}

		int[] targetOffsets = new int[size];
		for (int offset = 0; offset < size; offset++)
		{
			ArrayDeque<Integer> targets = targetsByItemId.get(actualItemIds[sectionStart + offset]);
			if (targets == null || targets.isEmpty())
			{
				return null;
			}
			targetOffsets[offset] = targets.removeFirst();
		}
		for (ArrayDeque<Integer> targets : targetsByItemId.values())
		{
			if (!targets.isEmpty())
			{
				return null;
			}
		}
		return targetOffsets;
	}

	/** Sorted item IDs whose live occurrence count exceeds the planned count. */
	static List<Integer> excessRepeatedItemIds(Map<Integer, Integer> actualCounts,
		Map<Integer, Integer> plannedCounts)
	{
		TreeSet<Integer> result = new TreeSet<>();
		for (Map.Entry<Integer, Integer> entry : actualCounts.entrySet())
		{
			int planned = plannedCounts.getOrDefault(entry.getKey(), 0);
			if (entry.getValue() > 1 && entry.getValue() > planned)
			{
				result.add(entry.getKey());
			}
		}
		return List.copyOf(result);
	}
}

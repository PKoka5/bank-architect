package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.List;
import java.util.Objects;

/**
 * Pure insert-mode planner for one dense bank section.
 *
 * <p>An insert drag removes the dragged item from its slot and reinserts it at
 * the drop slot index, so every slot between source and drop shifts by one. A
 * planned target slot index is therefore invalid the moment the player drags,
 * and each advised step is anchored to the item that currently occupies the
 * drop slot instead.</p>
 *
 * <p>Step selection keeps a longest increasing subsequence of the target
 * permutation in place and drags every other item exactly once, which is the
 * exact lower bound {@code n - LIS}. Items are served in target order, so the
 * section visibly fills in from the top.</p>
 */
public final class SectionInsertPlanner
{
	private SectionInsertPlanner()
	{
	}

	/**
	 * Exact same-section insert lower bound: every item outside a longest
	 * increasing subsequence of the target permutation must be dragged once.
	 * Returns 0 when the section cannot be interpreted against the plan.
	 */
	static int minimumRemainingInserts(int[] actualItemIds, int sectionStart,
		List<BankPreviewItem> targetItems)
	{
		int[] permutation = permutation(actualItemIds, sectionStart, targetItems);
		return permutation == null
			? 0 : permutation.length - longestIncreasingSubsequence(permutation).length;
	}

	/**
	 * Next drag for this section, or {@code null} when it is already in target
	 * order or cannot be interpreted against the plan.
	 */
	static Step nextStep(int[] actualItemIds, int sectionStart, List<BankPreviewItem> targetItems)
	{
		Objects.requireNonNull(actualItemIds, "actualItemIds");
		Objects.requireNonNull(targetItems, "targetItems");

		int[] permutation = permutation(actualItemIds, sectionStart, targetItems);
		if (permutation == null)
		{
			return null;
		}

		int size = permutation.length;
		boolean[] settled = new boolean[size];
		for (int index : longestIncreasingSubsequence(permutation))
		{
			settled[index] = true;
		}

		int movePosition = -1;
		for (int position = 0; position < size; position++)
		{
			if (!settled[position]
				&& (movePosition < 0 || permutation[position] < permutation[movePosition]))
			{
				movePosition = position;
			}
		}
		if (movePosition < 0)
		{
			return null;
		}

		// Settled items already sit in target order, so the first settled
		// position holding a later target offset is the item this drag must
		// land in front of.
		int anchorPosition = -1;
		for (int position = 0; position < size; position++)
		{
			if (settled[position] && permutation[position] > permutation[movePosition])
			{
				anchorPosition = position;
				break;
			}
		}

		// Dropping on a slot to the right of the source lands the item behind
		// that slot's occupant, because removing the source shifts the section
		// left first. Aim one slot earlier so the item lands in front instead.
		int dropPosition;
		if (anchorPosition < 0)
		{
			dropPosition = size - 1;
		}
		else
		{
			dropPosition = movePosition < anchorPosition ? anchorPosition - 1 : anchorPosition;
		}
		if (dropPosition == movePosition || dropPosition < 0 || dropPosition >= size)
		{
			// A no-op drop would mean the item could have joined the longest
			// increasing subsequence. Fail closed rather than advise nothing.
			return null;
		}

		return new Step(targetItems.get(permutation[movePosition]),
			sectionStart + movePosition, sectionStart + dropPosition,
			actualItemIds[sectionStart + dropPosition]);
	}

	/**
	 * Applies one insert drag: removes {@code fromSlot} and reinserts that item
	 * at {@code dropSlot}. Mirrors how the game rearranges the item container.
	 */
	static int[] applyInsert(int[] itemIds, int fromSlot, int dropSlot)
	{
		Objects.requireNonNull(itemIds, "itemIds");
		if (fromSlot < 0 || fromSlot >= itemIds.length
			|| dropSlot < 0 || dropSlot >= itemIds.length)
		{
			throw new IllegalArgumentException("insert slots must be inside the bank");
		}

		int[] result = new int[itemIds.length];
		int moved = itemIds[fromSlot];
		int source = 0;
		for (int slot = 0; slot < itemIds.length; slot++)
		{
			if (slot == dropSlot)
			{
				result[slot] = moved;
				continue;
			}
			if (source == fromSlot)
			{
				source++;
			}
			result[slot] = itemIds[source++];
		}
		return result;
	}

	private static int[] permutation(int[] actualItemIds, int sectionStart,
		List<BankPreviewItem> targetItems)
	{
		return ItemOccurrenceMatcher.orderedTargetOffsets(actualItemIds, sectionStart, targetItems);
	}

	/**
	 * Indices of one longest strictly increasing subsequence, ascending. The
	 * patience construction is deterministic, which keeps a pinned guidance
	 * step stable while only the bank viewport changes.
	 */
	static int[] longestIncreasingSubsequence(int[] values)
	{
		int size = values.length;
		int[] tailIndexByLength = new int[size + 1];
		int[] predecessor = new int[size];
		int length = 0;
		for (int index = 0; index < size; index++)
		{
			int low = 1;
			int high = length;
			while (low <= high)
			{
				int middle = (low + high) >>> 1;
				if (values[tailIndexByLength[middle]] < values[index])
				{
					low = middle + 1;
				}
				else
				{
					high = middle - 1;
				}
			}
			predecessor[index] = low > 1 ? tailIndexByLength[low - 1] : -1;
			tailIndexByLength[low] = index;
			if (low > length)
			{
				length = low;
			}
		}

		int[] indices = new int[length];
		int index = length > 0 ? tailIndexByLength[length] : -1;
		for (int position = length - 1; position >= 0; position--)
		{
			indices[position] = index;
			index = predecessor[index];
		}
		return indices;
	}

	/** One advised insert drag inside a single section. */
	static final class Step
	{
		private final BankPreviewItem item;
		private final int fromSlot;
		private final int dropSlot;
		private final int anchorItemId;

		private Step(BankPreviewItem item, int fromSlot, int dropSlot, int anchorItemId)
		{
			this.item = Objects.requireNonNull(item, "item");
			this.fromSlot = fromSlot;
			this.dropSlot = dropSlot;
			this.anchorItemId = anchorItemId;
		}

		BankPreviewItem getItem()
		{
			return item;
		}

		int getFromSlot()
		{
			return fromSlot;
		}

		/** Slot the player drops on; its current occupant is the anchor. */
		int getDropSlot()
		{
			return dropSlot;
		}

		int getAnchorItemId()
		{
			return anchorItemId;
		}
	}
}

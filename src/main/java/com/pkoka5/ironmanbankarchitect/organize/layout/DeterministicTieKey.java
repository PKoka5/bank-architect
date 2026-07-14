package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * The deterministic tie comparator of a complete plan. It lives outside the numeric
 * {@link LayoutScore} tuple and is consulted only when two plans have exactly equal scores.
 *
 * <p>A key holds the canonically ordered vector of {@link PlacedBlock} facts plus the complete
 * final target-order item-ID vector. Comparison walks the aligned block vectors one document
 * component at a time — first every block's complete stable identity (rule key plus atom keys),
 * then every width-preference rank, width, primitive ordinal, start row, start column, and explicit
 * row geometry, followed last by the complete item-ID vector. It never reduces a plan to one scalar
 * width/primitive/origin and never compares one block completely before looking at the next. A pure
 * fallback plan uses an empty block vector.</p>
 *
 * <p>Every component is a stable canonical fact of the plan, so the tie outcome never depends on
 * map or insertion iteration order.</p>
 */
public final class DeterministicTieKey implements Comparable<DeterministicTieKey>
{
	private static final Comparator<PlacedBlock> BLOCK_IDENTITY_ORDER = (left, right) ->
	{
		int result = left.getRuleKey().compareTo(right.getRuleKey());
		return result != 0 ? result : compareStringVectors(left.getAtomKeys(), right.getAtomKeys());
	};

	private final List<PlacedBlock> blocks;
	private final List<Integer> finalTargetOrderItemIds;

	public DeterministicTieKey(List<PlacedBlock> blocks, List<Integer> finalTargetOrderItemIds)
	{
		this.blocks = requireBlocks(blocks);
		this.finalTargetOrderItemIds = requireItemIds(finalTargetOrderItemIds);
		validateBlockItems(this.blocks, this.finalTargetOrderItemIds);
	}

	public List<PlacedBlock> getBlocks()
	{
		return blocks;
	}

	public List<Integer> getFinalTargetOrderItemIds()
	{
		return finalTargetOrderItemIds;
	}

	@Override
	public int compareTo(DeterministicTieKey other)
	{
		Objects.requireNonNull(other, "other");

		int result = compareAligned(other, BLOCK_IDENTITY_ORDER::compare);
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other,
			(left, right) -> Integer.compare(left.getWidthPreferenceRank(), right.getWidthPreferenceRank()));
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other, (left, right) -> Integer.compare(left.getWidth(), right.getWidth()));
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other, (left, right) ->
			Integer.compare(left.getShapePrimitive().ordinal(), right.getShapePrimitive().ordinal()));
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other, (left, right) -> Integer.compare(left.getStartRow(), right.getStartRow()));
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other,
			(left, right) -> Integer.compare(left.getStartColumn(), right.getStartColumn()));
		if (result != 0)
		{
			return result;
		}

		result = compareAligned(other, (left, right) -> compareRowVectors(left.getRows(), right.getRows()));
		if (result != 0)
		{
			return result;
		}

		return compareIntegerVectors(finalTargetOrderItemIds, other.finalTargetOrderItemIds);
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof DeterministicTieKey))
		{
			return false;
		}

		DeterministicTieKey key = (DeterministicTieKey) other;
		return blocks.equals(key.blocks) && finalTargetOrderItemIds.equals(key.finalTargetOrderItemIds);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(blocks, finalTargetOrderItemIds);
	}

	@Override
	public String toString()
	{
		return "DeterministicTieKey{blocks=" + blocks + ", finalOrder=" + finalTargetOrderItemIds + "}";
	}

	private interface BlockComponentComparator
	{
		int compare(PlacedBlock left, PlacedBlock right);
	}

	private int compareAligned(DeterministicTieKey other, BlockComponentComparator comparator)
	{
		int shared = Math.min(blocks.size(), other.blocks.size());
		for (int index = 0; index < shared; index++)
		{
			int result = comparator.compare(blocks.get(index), other.blocks.get(index));
			if (result != 0)
			{
				return result;
			}
		}

		return Integer.compare(blocks.size(), other.blocks.size());
	}

	private static int compareRowVectors(List<LayoutCandidate.Row> left, List<LayoutCandidate.Row> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index).getStartOffset(), right.get(index).getStartOffset());
			if (result != 0)
			{
				return result;
			}

			result = compareIntegerVectors(left.get(index).getItemIds(), right.get(index).getItemIds());
			if (result != 0)
			{
				return result;
			}
		}

		return Integer.compare(left.size(), right.size());
	}

	private static List<PlacedBlock> requireBlocks(List<PlacedBlock> blocks)
	{
		Objects.requireNonNull(blocks, "blocks");
		List<PlacedBlock> canonical = new ArrayList<>(blocks.size());
		for (PlacedBlock block : blocks)
		{
			canonical.add(Objects.requireNonNull(block, "blocks must not contain null"));
		}
		canonical.sort(BLOCK_IDENTITY_ORDER);
		for (int index = 1; index < canonical.size(); index++)
		{
			if (BLOCK_IDENTITY_ORDER.compare(canonical.get(index - 1), canonical.get(index)) == 0)
			{
				throw new IllegalArgumentException("blocks must not contain duplicate stable block identity "
					+ canonical.get(index).getRuleKey() + "/" + canonical.get(index).getAtomKeys());
			}
		}

		return Collections.unmodifiableList(canonical);
	}

	private static List<Integer> requireItemIds(List<Integer> itemIds)
	{
		Objects.requireNonNull(itemIds, "finalTargetOrderItemIds");

		Set<Integer> seen = new HashSet<>();
		List<Integer> validated = new ArrayList<>(itemIds.size());
		for (Integer itemId : itemIds)
		{
			if (itemId == null || itemId <= 0)
			{
				throw new IllegalArgumentException("finalTargetOrderItemIds must contain positive item IDs");
			}
			if (itemId == ItemID.BANK_FILLER)
			{
				throw new IllegalArgumentException("finalTargetOrderItemIds must not contain Bank Filler");
			}
			if (!seen.add(itemId))
			{
				throw new IllegalArgumentException("finalTargetOrderItemIds must not contain duplicate item ID "
					+ itemId);
			}
			validated.add(itemId);
		}

		return Collections.unmodifiableList(validated);
	}

	private static void validateBlockItems(List<PlacedBlock> blocks, List<Integer> finalItemIds)
	{
		Set<Integer> finalItems = new HashSet<>(finalItemIds);
		Set<Integer> seenBlockItems = new HashSet<>();
		for (PlacedBlock block : blocks)
		{
			for (LayoutCandidate.Row row : block.getRows())
			{
				for (Integer itemId : row.getItemIds())
				{
					if (!finalItems.contains(itemId))
					{
						throw new IllegalArgumentException("block item " + itemId
							+ " is absent from finalTargetOrderItemIds");
					}
					if (!seenBlockItems.add(itemId))
					{
						throw new IllegalArgumentException("item " + itemId + " appears in more than one block");
					}
				}
			}
		}
	}

	private static int compareStringVectors(List<String> left, List<String> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = left.get(index).compareTo(right.get(index));
			if (result != 0)
			{
				return result;
			}
		}

		return Integer.compare(left.size(), right.size());
	}

	private static int compareIntegerVectors(List<Integer> left, List<Integer> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index), right.get(index));
			if (result != 0)
			{
				return result;
			}
		}

		return Integer.compare(left.size(), right.size());
	}
}

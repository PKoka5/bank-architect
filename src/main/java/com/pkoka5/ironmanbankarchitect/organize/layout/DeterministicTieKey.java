package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.*;
import java.util.List;
import net.runelite.api.gameval.ItemID;

/**
 * Breaks equal LayoutScores using canonical block vectors, component by component: all identities,
 * then width ranks, widths, primitives, origins and row geometry, then the final item-ID vector.
 * Never compares entire blocks sequentially or collapses geometry to scalars. Fallback uses no
 * blocks. Independent of map/insertion order.
 */
public final class DeterministicTieKey implements Comparable<DeterministicTieKey>
{
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

		int result = LayoutOrdering.compareBlockVectors(blocks, other.blocks);
		return result != 0 ? result
			: LayoutOrdering.compareValues(finalTargetOrderItemIds, other.finalTargetOrderItemIds);
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

	private static List<PlacedBlock> requireBlocks(List<PlacedBlock> blocks)
	{
		Objects.requireNonNull(blocks, "blocks");
		List<PlacedBlock> canonical = new ArrayList<>(blocks.size());
		for (PlacedBlock block : blocks)
		{
			canonical.add(Objects.requireNonNull(block, "blocks must not contain null"));
		}
		canonical.sort(LayoutOrdering.IDENTITY);
		for (int index = 1; index < canonical.size(); index++)
		{
			if (LayoutOrdering.IDENTITY.compare(canonical.get(index - 1), canonical.get(index)) == 0)
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

}

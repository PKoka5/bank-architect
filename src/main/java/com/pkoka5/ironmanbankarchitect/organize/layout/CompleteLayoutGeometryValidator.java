package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Independently reconstructs the meaningful semantic cells of a complete layout from its placed
 * block facts. This is the final trust boundary between the bounded search state and the public
 * plan: a block must describe the same targets as both the semantic occupancy state and the final
 * dense item vector.
 *
 * <p>Nominal rectangles may overlap in slack, but every meaningful target and every semantic item
 * may belong to only one block. The complete nominal rectangle of every block must remain inside
 * the dense category, including the ragged final-row tail.</p>
 */
final class CompleteLayoutGeometryValidator
{
	private static final int GRID_COLUMNS = SemanticRule.MAX_WIDTH;

	private CompleteLayoutGeometryValidator()
	{
	}

	static List<LayoutConflict> validate(LayoutRequest request, List<PlacedBlock> blocks,
		int[] semanticItemIdAtTarget, int[] finalItemIds)
	{
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(blocks, "blocks");
		Objects.requireNonNull(semanticItemIdAtTarget, "semanticItemIdAtTarget");
		Objects.requireNonNull(finalItemIds, "finalItemIds");

		List<LayoutConflict> conflicts = new ArrayList<>();
		if (semanticItemIdAtTarget.length != request.size() || finalItemIds.length != request.size())
		{
			conflicts.add(conflict(LayoutConflict.NO_ITEM,
				"geometry vectors must both have request size " + request.size()
					+ ", got semantic=" + semanticItemIdAtTarget.length
					+ " and final=" + finalItemIds.length));
			return immutableCanonical(conflicts);
		}

		List<PlacedBlock> canonicalBlocks = new ArrayList<>(blocks.size());
		for (PlacedBlock block : blocks)
		{
			canonicalBlocks.add(Objects.requireNonNull(block, "blocks must not contain null"));
		}
		canonicalBlocks.sort(CompleteLayoutGeometryValidator::compareBlocks);

		int[] reconstructed = new int[request.size()];
		int physicalStart = request.getGridStartColumn();
		long physicalEndExclusive = (long) physicalStart + request.size();
		Set<Integer> seenBlockItems = new HashSet<>();
		for (PlacedBlock block : canonicalBlocks)
		{
			for (int localRow = 0; localRow < block.getRows().size(); localRow++)
			{
				LayoutCandidate.Row row = block.getRows().get(localRow);
				long physicalRow = (long) block.getStartRow() + localRow;
				long nominalFirst = physicalRow * GRID_COLUMNS + block.getStartColumn();
				long nominalLast = nominalFirst + block.getWidth() - 1L;
				if (nominalFirst < physicalStart || nominalLast >= physicalEndExclusive)
				{
					conflicts.add(conflict(LayoutConflict.NO_ITEM,
						identity(block) + " nominal row " + localRow + " spans physical targets "
							+ nominalFirst + ".." + nominalLast + " outside " + physicalStart
							+ ".." + (physicalEndExclusive - 1)));
				}

				for (int memberIndex = 0; memberIndex < row.length(); memberIndex++)
				{
					int itemId = row.getItemIds().get(memberIndex);
					long physicalTarget = physicalRow * GRID_COLUMNS + block.getStartColumn()
						+ row.getStartOffset() + memberIndex;
					if (!seenBlockItems.add(itemId))
					{
						conflicts.add(conflict(itemId,
							"semantic item appears in more than one placed block: " + identity(block)));
					}
					if (physicalTarget < physicalStart || physicalTarget >= physicalEndExclusive)
					{
						conflicts.add(conflict(itemId,
							identity(block) + " places semantic item outside the physical request window at "
								+ physicalTarget));
						continue;
					}

					int target = Math.toIntExact(physicalTarget - physicalStart);
					if (reconstructed[target] != 0)
					{
						conflicts.add(conflict(itemId,
							"semantic target " + target + " is claimed by items "
								+ reconstructed[target] + " and " + itemId));
					}
					else
					{
						reconstructed[target] = itemId;
					}

					if (finalItemIds[target] != itemId)
					{
						conflicts.add(conflict(itemId,
							identity(block) + " expects item " + itemId + " on target " + target
								+ " but final plan contains " + finalItemIds[target]));
					}
				}
			}
		}

		for (int target = 0; target < reconstructed.length; target++)
		{
			if (reconstructed[target] != semanticItemIdAtTarget[target])
			{
				int itemId = semanticItemIdAtTarget[target] != 0
					? semanticItemIdAtTarget[target] : reconstructed[target];
				conflicts.add(conflict(itemId,
					"semantic state mismatch on target " + target + ": blocks="
						+ reconstructed[target] + ", state=" + semanticItemIdAtTarget[target]));
			}
		}

		return immutableCanonical(conflicts);
	}

	private static LayoutConflict conflict(int itemId, String detail)
	{
		return new LayoutConflict(LayoutConflict.Type.PLAN_SEMANTIC_GEOMETRY_MISMATCH,
			itemId, detail);
	}

	private static List<LayoutConflict> immutableCanonical(List<LayoutConflict> conflicts)
	{
		conflicts.sort(LayoutConflict.CANONICAL_ORDER);
		return Collections.unmodifiableList(conflicts);
	}

	private static String identity(PlacedBlock block)
	{
		return block.getRuleKey() + "/" + block.getAtomKeys();
	}

	private static int compareBlocks(PlacedBlock left, PlacedBlock right)
	{
		int result = left.getRuleKey().compareTo(right.getRuleKey());
		if (result != 0) return result;
		result = compareStrings(left.getAtomKeys(), right.getAtomKeys());
		if (result != 0) return result;
		result = Integer.compare(left.getWidthPreferenceRank(), right.getWidthPreferenceRank());
		if (result != 0) return result;
		result = Integer.compare(left.getWidth(), right.getWidth());
		if (result != 0) return result;
		result = Integer.compare(left.getShapePrimitive().ordinal(), right.getShapePrimitive().ordinal());
		if (result != 0) return result;
		result = Integer.compare(left.getStartRow(), right.getStartRow());
		if (result != 0) return result;
		result = Integer.compare(left.getStartColumn(), right.getStartColumn());
		if (result != 0) return result;
		return compareRows(left.getRows(), right.getRows());
	}

	private static int compareRows(List<LayoutCandidate.Row> left, List<LayoutCandidate.Row> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index).getStartOffset(),
				right.get(index).getStartOffset());
			if (result != 0) return result;
			result = compareIntegers(left.get(index).getItemIds(), right.get(index).getItemIds());
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareStrings(List<String> left, List<String> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = left.get(index).compareTo(right.get(index));
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareIntegers(List<Integer> left, List<Integer> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index), right.get(index));
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}
}

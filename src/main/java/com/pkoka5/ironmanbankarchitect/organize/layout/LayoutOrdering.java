package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.*;
import java.util.List;

/** Shared canonical ordering for completed plans, search states and geometry validation. */
final class LayoutOrdering
{
	static final Comparator<PlacedBlock> IDENTITY = Comparator.comparing(PlacedBlock::getRuleKey)
		.thenComparing(PlacedBlock::getAtomKeys, LayoutOrdering::compareValues);
	static final List<Comparator<PlacedBlock>> COMPONENTS = Collections.unmodifiableList(Arrays.asList(
		IDENTITY,
		Comparator.comparingInt(PlacedBlock::getWidthPreferenceRank),
		Comparator.comparingInt(PlacedBlock::getWidth),
		Comparator.comparingInt(block -> block.getShapePrimitive().ordinal()),
		Comparator.comparingInt(PlacedBlock::getStartRow),
		Comparator.comparingInt(PlacedBlock::getStartColumn),
		Comparator.comparing(PlacedBlock::getRows, LayoutOrdering::compareRows)));
	private static final Comparator<LayoutCandidate.Row> ROW =
		Comparator.comparingInt(LayoutCandidate.Row::getStartOffset)
			.thenComparing(LayoutCandidate.Row::getItemIds, LayoutOrdering::compareValues);

	private LayoutOrdering() { }

	static int compareBlocks(PlacedBlock left, PlacedBlock right)
	{
		for (Comparator<PlacedBlock> component : COMPONENTS)
		{
			int result = component.compare(left, right);
			if (result != 0) return result;
		}
		return 0;
	}

	/** Compare each component across all blocks before proceeding to the next component. */
	static int compareBlockVectors(List<PlacedBlock> left, List<PlacedBlock> right)
	{
		for (Comparator<PlacedBlock> component : COMPONENTS)
		{
			int result = compareLists(left, right, component);
			if (result != 0) return result;
		}
		return 0;
	}

	static <T extends Comparable<? super T>> int compareValues(List<T> left, List<T> right)
	{
		return compareLists(left, right, Comparator.naturalOrder());
	}

	private static int compareRows(List<LayoutCandidate.Row> left, List<LayoutCandidate.Row> right)
	{
		return compareLists(left, right, ROW);
	}

	private static <T> int compareLists(List<T> left, List<T> right, Comparator<? super T> comparator)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = comparator.compare(left.get(index), right.get(index));
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}
}

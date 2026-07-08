package com.pkoka5.ironmanbankarchitect.overlay;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class SlotGridResolver
{
	private SlotGridResolver()
	{
	}

	static List<Rectangle> resolveFirstRow(List<Rectangle> candidateBounds, int maxCells)
	{
		if (candidateBounds == null || candidateBounds.isEmpty() || maxCells <= 0)
		{
			return Collections.emptyList();
		}

		List<Rectangle> sortedByPosition = new ArrayList<>(candidateBounds);
		sortedByPosition.sort(Comparator.comparingInt((Rectangle r) -> r.y).thenComparingInt(r -> r.x));

		Rectangle topLeft = sortedByPosition.get(0);
		int rowTolerance = Math.max(2, topLeft.height / 4);

		List<Rectangle> firstRow = new ArrayList<>();
		for (Rectangle candidate : sortedByPosition)
		{
			if (Math.abs(candidate.y - topLeft.y) <= rowTolerance)
			{
				firstRow.add(candidate);
			}
		}

		firstRow.sort(Comparator.comparingInt(r -> r.x));

		if (firstRow.size() > maxCells)
		{
			firstRow = firstRow.subList(0, maxCells);
		}

		return firstRow;
	}
}

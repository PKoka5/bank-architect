package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Single canonical row-derivation path shared by generation and group validation. A {@code null}
 * result means the requested nominal width is structurally infeasible for the primitive.
 */
final class LayoutCandidateGeometry
{
	private LayoutCandidateGeometry()
	{
	}

	static List<LayoutCandidate.Row> rowsFor(ShapePrimitive primitive,
		List<LayoutCandidateGroup.AtomProjection> projections, int width)
	{
		Objects.requireNonNull(primitive, "primitive");
		if (projections == null || projections.isEmpty())
		{
			throw new IllegalArgumentException("projections must not be empty");
		}
		if (width < SemanticRule.MIN_WIDTH || width > SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("width must be within 1..8");
		}

		switch (primitive)
		{
			case HORIZONTAL_RUN:
				return horizontalRows(projections, width);
			case VERTICAL_RUN:
				return verticalRows(projections, width);
			case STAGE_MATRIX:
				return stageMatrixRows(projections, width);
			case ROW_GROUP_MATRIX:
				return rowGroupRows(projections, width);
			default:
				throw new IllegalStateException("Unhandled shape primitive: " + primitive);
		}
	}

	private static List<LayoutCandidate.Row> horizontalRows(
		List<LayoutCandidateGroup.AtomProjection> projections, int width)
	{
		requireSingleProjection(projections);
		List<Integer> itemIds = projections.get(0).getItemIds();
		return itemIds.size() <= width
			? Collections.singletonList(new LayoutCandidate.Row(0, itemIds))
			: null;
	}

	private static List<LayoutCandidate.Row> verticalRows(
		List<LayoutCandidateGroup.AtomProjection> projections, int width)
	{
		requireSingleProjection(projections);
		if (width != 1)
		{
			return null;
		}

		List<LayoutCandidate.Row> rows = new ArrayList<>();
		for (Integer itemId : projections.get(0).getItemIds())
		{
			rows.add(new LayoutCandidate.Row(0, Collections.singletonList(itemId)));
		}
		return rows;
	}

	private static List<LayoutCandidate.Row> stageMatrixRows(
		List<LayoutCandidateGroup.AtomProjection> projections, int width)
	{
		List<LayoutCandidate.Row> rows = new ArrayList<>();
		int stageCount = projections.get(0).size();
		for (int chunkStart = 0; chunkStart < projections.size(); chunkStart += width)
		{
			int chunkEnd = Math.min(chunkStart + width, projections.size());
			for (int stageIndex = 0; stageIndex < stageCount; stageIndex++)
			{
				List<Integer> stageItemIds = new ArrayList<>(chunkEnd - chunkStart);
				for (int atomIndex = chunkStart; atomIndex < chunkEnd; atomIndex++)
				{
					stageItemIds.add(projections.get(atomIndex).getItemIds().get(stageIndex));
				}
				rows.add(new LayoutCandidate.Row(0, stageItemIds));
			}
		}
		return rows;
	}

	private static List<LayoutCandidate.Row> rowGroupRows(
		List<LayoutCandidateGroup.AtomProjection> projections, int width)
	{
		List<LayoutCandidate.Row> rows = new ArrayList<>(projections.size());
		for (LayoutCandidateGroup.AtomProjection projection : projections)
		{
			if (projection.size() > width)
			{
				return null;
			}
			rows.add(new LayoutCandidate.Row(0, projection.getItemIds()));
		}
		return rows;
	}

	private static void requireSingleProjection(List<LayoutCandidateGroup.AtomProjection> projections)
	{
		if (projections.size() != 1)
		{
			throw new IllegalArgumentException("run geometry requires exactly one projection");
		}
	}
}

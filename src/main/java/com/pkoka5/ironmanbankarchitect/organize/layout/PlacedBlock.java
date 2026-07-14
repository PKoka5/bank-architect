package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.List;
import java.util.Objects;

/**
 * One immutable placed-block fact of a complete plan: the stable block identity (rule key plus
 * ordered atom keys), the width-preference rank, the concrete width, the shape primitive, the
 * physical start row/column, and the explicit candidate-row geometry. All semantic and geometry
 * fields are derived from one {@link LayoutCandidate}; the width-preference rank is derived from
 * that candidate's validated {@link LayoutCandidateGroup}. Callers cannot combine one candidate's
 * identity or geometry with another candidate's evidence.
 *
 * <p>These facts feed the {@link DeterministicTieKey}. Equality covers every field, so two placed
 * blocks are equal exactly when they describe the same block in the same physical position with
 * the same internal geometry.</p>
 */
public final class PlacedBlock
{
	private final LayoutCandidate candidate;
	private final int widthPreferenceRank;
	private final int startRow;
	private final int startColumn;

	private PlacedBlock(LayoutCandidate candidate, int widthPreferenceRank, int startRow, int startColumn)
	{
		this.candidate = Objects.requireNonNull(candidate, "candidate");
		this.widthPreferenceRank = widthPreferenceRank;
		if (startRow < 0)
		{
			throw new IllegalArgumentException("startRow must not be negative");
		}
		this.startRow = startRow;
		if (startColumn < 0 || startColumn >= SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("startColumn must be within 0.." + (SemanticRule.MAX_WIDTH - 1));
		}
		if (startColumn > SemanticRule.MAX_WIDTH - candidate.getWidth())
		{
			throw new IllegalArgumentException("block with start column " + startColumn + " and width "
				+ candidate.getWidth()
				+ " does not fit the eight-column grid");
		}
		this.startColumn = startColumn;
	}

	static PlacedBlock place(LayoutCandidateGroup group, LayoutCandidate candidate,
		int startRow, int startColumn)
	{
		Objects.requireNonNull(group, "group");
		int widthPreferenceRank = LayoutCandidateScorer.widthPreferenceRank(group, candidate);
		return new PlacedBlock(candidate, widthPreferenceRank, startRow, startColumn);
	}

	public String getRuleKey()
	{
		return candidate.getRuleKey();
	}

	public List<String> getAtomKeys()
	{
		return candidate.getAtomKeys();
	}

	public int getWidthPreferenceRank()
	{
		return widthPreferenceRank;
	}

	public int getWidth()
	{
		return candidate.getWidth();
	}

	public ShapePrimitive getShapePrimitive()
	{
		return candidate.getShapePrimitive();
	}

	public int getStartRow()
	{
		return startRow;
	}

	public int getStartColumn()
	{
		return startColumn;
	}

	public List<LayoutCandidate.Row> getRows()
	{
		return candidate.getRows();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof PlacedBlock))
		{
			return false;
		}

		PlacedBlock block = (PlacedBlock) other;
		return widthPreferenceRank == block.widthPreferenceRank
			&& startRow == block.startRow
			&& startColumn == block.startColumn
			&& candidate.equals(block.candidate);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(candidate, widthPreferenceRank, startRow, startColumn);
	}

	@Override
	public String toString()
	{
		return "PlacedBlock{" + getRuleKey() + ", atoms=" + getAtomKeys() + ", prefRank="
			+ widthPreferenceRank + ", width=" + getWidth() + ", " + getShapePrimitive() + ", start=("
			+ startRow + "," + startColumn + "), rows=" + getRows() + "}";
	}
}

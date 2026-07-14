package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The immutable outcome of validating or planning one category layout: either a complete list of
 * placements, or a non-empty list of typed conflicts. There is never a partial plan — a conflicted
 * result carries no placements at all.
 */
public final class LayoutResult
{
	private final List<LayoutPlacement> placements;
	private final List<LayoutConflict> conflicts;

	private LayoutResult(List<LayoutPlacement> placements, List<LayoutConflict> conflicts)
	{
		this.placements = placements;
		this.conflicts = conflicts;
	}

	static LayoutResult success(List<LayoutPlacement> placements)
	{
		Objects.requireNonNull(placements, "placements");
		for (LayoutPlacement placement : placements)
		{
			Objects.requireNonNull(placement, "placements must not contain null");
		}

		return new LayoutResult(Collections.unmodifiableList(new ArrayList<>(placements)),
			Collections.emptyList());
	}

	static LayoutResult conflict(List<LayoutConflict> conflicts)
	{
		Objects.requireNonNull(conflicts, "conflicts");
		if (conflicts.isEmpty())
		{
			throw new IllegalArgumentException("a conflicted result requires at least one conflict");
		}
		for (LayoutConflict conflict : conflicts)
		{
			Objects.requireNonNull(conflict, "conflicts must not contain null");
		}

		return new LayoutResult(Collections.emptyList(),
			Collections.unmodifiableList(new ArrayList<>(conflicts)));
	}

	public boolean isSuccess()
	{
		return conflicts.isEmpty();
	}

	public List<LayoutPlacement> getPlacements()
	{
		return placements;
	}

	public List<LayoutConflict> getConflicts()
	{
		return conflicts;
	}
}

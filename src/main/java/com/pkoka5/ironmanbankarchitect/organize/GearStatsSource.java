package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Optional;

/**
 * Supplies real equipment stats per item ID. The plugin backs this with
 * RuneLite's ItemManager; tests supply fakes. When no stats are available the
 * sorter falls back to name-based heuristics.
 */
@FunctionalInterface
public interface GearStatsSource
{
	GearStatsSource NONE = itemId -> Optional.empty();

	Optional<GearStats> statsFor(int itemId);
}

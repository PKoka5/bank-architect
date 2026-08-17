package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Optional;

/**
 * Supplies a player-chosen destination category key per item ID.
 *
 * <p>Classification is deliberately fail-closed, so an item the bundled data
 * cannot place confidently ends up in the review category. This source is how
 * the player corrects such a decision: the returned key wins over every
 * automatic rule, including the quick-tool and alch-candidate rules.</p>
 *
 * <p>A key that is not part of the active preset is ignored rather than
 * treated as an error, so an override recorded under one preset can never
 * break a plan built with another.</p>
 */
@FunctionalInterface
public interface CategoryOverrideSource
{
	CategoryOverrideSource NONE = itemId -> Optional.empty();

	Optional<String> categoryKeyFor(int itemId);
}

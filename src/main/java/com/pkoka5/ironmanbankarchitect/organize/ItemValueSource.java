package com.pkoka5.ironmanbankarchitect.organize;

/**
 * Supplies the high-alch value per item ID (0 for untradeable or unknown
 * items). The plugin backs this with the game's item data; tests supply fakes.
 */
@FunctionalInterface
public interface ItemValueSource
{
	ItemValueSource NONE = itemId -> 0;

	int highAlchValue(int itemId);
}

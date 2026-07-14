package com.pkoka5.ironmanbankarchitect.organize;

/**
 * Reusable item-ordering behaviour for a preset category.
 *
 * <p>The mode is deliberately independent from the category key. Different
 * account presets can therefore share one proven sorter without relying on
 * matching tab names.</p>
 */
public enum BankCategorySortMode
{
	GENERIC,
	MAIN,
	CURRENCY,
	TELEPORTS,
	GEAR,
	SUPPLIES,
	HERBLORE,
	FARMING,
	TOOLS,
	RESOURCES,
	BOSS_LOOT,
	CLUES,
	REVIEW
}

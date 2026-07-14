package com.pkoka5.ironmanbankarchitect.organize;

/**
 * Primary skill zones inside a Resources-mode category. Every bank entry belongs to exactly one
 * zone; cross-skill use never duplicates an item into a second zone.
 *
 * <p>The declaration order is the deterministic zone order used when concatenating per-zone
 * layouts. It encodes the cohort consensus anchors (Mining/Smithing earliest, followed by stable
 * primary-skill zones) without copying any single community template.</p>
 */
enum ResourceSkillZone
{
	MINING_SMITHING,
	WOODCUTTING,
	CRAFTING,
	CONSTRUCTION,
	FLETCHING,
	FISHING_COOKING,
	HUNTER,
	SAILING,
	PRAYER,
	OTHER_RESOURCE
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

/**
 * Evidence confidence tier of a semantic rule. Higher tiers dominate lower tiers in the
 * lexicographic {@link LayoutScore}: missed high-confidence semantics can never be bought back by
 * savings in a lower tier.
 */
public enum ConfidenceTier
{
	HIGH,
	MEDIUM,
	LOW
}

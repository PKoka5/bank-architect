package com.pkoka5.ironmanbankarchitect.organize;

/**
 * How the combat gear tab is laid out. Gear is the one category with two
 * curated grid shapes, so its layout choice carries three values where every
 * other category chooses between a grid and a list.
 */
public enum GearLayout
{
	/**
	 * The four-style best-in-slot matrix: one row per equipment slot, the
	 * leading columns your strongest melee, ranged, magic and prayer options,
	 * rows completed with same-slot spares so the columns stay straight.
	 */
	GRID_STYLES("Best in slot"),
	/**
	 * Each curated set as one vertical column - helm, body, legs - with the
	 * rest of the kit arranged around it, junk-free.
	 */
	GRID_SETS("Sets together"),
	/**
	 * Each curated set as one left-to-right run in slot order, strongest set
	 * first, loose gear and weapons flowing after like text, junk-free.
	 */
	LIST("List");

	private final String label;

	GearLayout(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

/**
 * How the combat gear tab lays out.
 *
 * <p>{@link #PACKED} is the shipped grid: aligned style columns with slot
 * rows. The other three are linear reads of the same information, for players
 * who prefer a tab that runs item after item: {@link #BY_SLOT} compares
 * across styles (every helmet, then every body), while the style orders read
 * each kit as one block — armour head to feet with {@link #BY_STYLE}, or led
 * by the weapon that names the kit with {@link #BY_STYLE_WEAPON_FIRST}.</p>
 */
public enum GearOrder
{
	PACKED("Style columns"),
	BY_SLOT("By slot"),
	BY_STYLE("By style"),
	BY_STYLE_WEAPON_FIRST("By style, weapon first");

	private final String label;

	GearOrder(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}

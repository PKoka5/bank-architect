package com.pkoka5.ironmanbankarchitect.organize;

/**
 * The layout choices that are a matter of taste rather than of placement.
 *
 * <p>Where a plan can say what the player wants, it does, and no option exists:
 * see {@link BankLayoutStyles}. These cannot be read from a plan, because none
 * of them is a statement about where something goes. They are statements about
 * which compromise the player would rather live with, and only they can answer
 * that.</p>
 *
 * <p>Row filling is asked separately for gear and for Herblore. They are the
 * same mechanism but not the same trade: a player may want mechanic-driven
 * combat sets completed with compatible style gear while accepting that a
 * short recipe row simply stops where it stops.</p>
 */
public final class BankLayoutOptions
{
	public static final BankLayoutOptions DEFAULTS = new BankLayoutOptions(true, true, true);

	private final boolean fillGearRows;
	private final boolean fillHerbloreRows;
	private final boolean alchPile;

	public BankLayoutOptions(boolean fillGearRows, boolean fillHerbloreRows, boolean alchPile)
	{
		this.fillGearRows = fillGearRows;
		this.fillHerbloreRows = fillHerbloreRows;
		this.alchPile = alchPile;
	}

	/**
	 * Whether mechanic-driven gear sets may be completed with compatible equipment.
	 *
	 * <p>On, compatible combat blocks may finish rows and align their helmet,
	 * body, legs, and weapon vertically when real entries can fill the grid. Off,
	 * an exact set remains contiguous but short blocks follow immediately. Neither
	 * mode invents empty cells or promotes unusable equipment.</p>
	 */
	public boolean fillGearRows()
	{
		return fillGearRows;
	}

	/**
	 * Whether a short Herblore recipe row may be padded out to eight columns.
	 *
	 * <p>On, a part-finished recipe borrows from the rest of the tab so the next
	 * recipe still starts at the left edge. Off, a short row is left short and
	 * the recipes simply follow each other.</p>
	 */
	public boolean fillHerbloreRows()
	{
		return fillHerbloreRows;
	}

	/**
	 * Whether gear the player has clearly outgrown is gathered for alching.
	 *
	 * <p>Useful while clearing out, and unwanted by a player who keeps a spare
	 * set on purpose. Off, gear stays gear and nothing is moved on the plugin's
	 * opinion of what is worth keeping.</p>
	 */
	public boolean alchPile()
	{
		return alchPile;
	}
}

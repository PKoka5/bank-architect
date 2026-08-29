package com.pkoka5.ironmanbankarchitect.organize;

/**
 * The layout choices that are a matter of taste rather than of placement.
 *
 * <p>Where a plan can say what the player wants, it does, and no option exists:
 * see {@link BankLayoutStyles}. These two cannot be read from a plan, because
 * neither is a statement about where something goes. They are statements about
 * which compromise the player would rather live with, and only they can answer
 * that.</p>
 */
public final class BankLayoutOptions
{
	public static final BankLayoutOptions DEFAULTS = new BankLayoutOptions(true, true);

	private final boolean fillRows;
	private final boolean alchPile;

	public BankLayoutOptions(boolean fillRows, boolean alchPile)
	{
		this.fillRows = fillRows;
		this.alchPile = alchPile;
	}

	/**
	 * Whether a part-filled row may be completed with unrelated items.
	 *
	 * <p>A bank tab cannot hold an empty slot, so a group can only start on a
	 * fresh row if real items fill the one before it. On, the aligned setup rows
	 * and recipe rows hold their shape at the price of an occasional stranger in
	 * the row. Off, nothing is placed anywhere it does not belong, at the price
	 * of the alignment.</p>
	 */
	public boolean fillRows()
	{
		return fillRows;
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

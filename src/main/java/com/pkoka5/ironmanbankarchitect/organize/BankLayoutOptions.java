package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

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
 * same mechanism but not the same trade: a player may well want the four combat
 * columns straight while accepting that a short recipe row simply stops where
 * it stops. Tab order is asked per category for the same reason: preferring
 * the gear grid says nothing about how the teleports should read.</p>
 */
public final class BankLayoutOptions
{
	public static final BankLayoutOptions DEFAULTS = new BankLayoutOptions(true, true, true);

	private final boolean fillGearRows;
	private final boolean fillHerbloreRows;
	private final boolean alchPile;
	private final GearOrder gearOrder;
	private final Map<BankCategorySortMode, TabOrder> tabOrders;
	private final PotionDoseOrder potionDoses;
	private final RuneOrder runeOrder;
	private final TeleportOrder teleportOrder;

	public BankLayoutOptions(boolean fillGearRows, boolean fillHerbloreRows, boolean alchPile)
	{
		this(fillGearRows, fillHerbloreRows, alchPile, GearOrder.PACKED,
			Collections.emptyMap());
	}

	public BankLayoutOptions(boolean fillGearRows, boolean fillHerbloreRows, boolean alchPile,
		GearOrder gearOrder, Map<BankCategorySortMode, TabOrder> tabOrders)
	{
		this(fillGearRows, fillHerbloreRows, alchPile, gearOrder, tabOrders,
			PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL);
	}

	public BankLayoutOptions(boolean fillGearRows, boolean fillHerbloreRows, boolean alchPile,
		GearOrder gearOrder, Map<BankCategorySortMode, TabOrder> tabOrders,
		PotionDoseOrder potionDoses, RuneOrder runeOrder)
	{
		this(fillGearRows, fillHerbloreRows, alchPile, gearOrder, tabOrders,
			potionDoses, runeOrder, TeleportOrder.ALPHABETICAL);
	}

	public BankLayoutOptions(boolean fillGearRows, boolean fillHerbloreRows, boolean alchPile,
		GearOrder gearOrder, Map<BankCategorySortMode, TabOrder> tabOrders,
		PotionDoseOrder potionDoses, RuneOrder runeOrder, TeleportOrder teleportOrder)
	{
		this.fillGearRows = fillGearRows;
		this.fillHerbloreRows = fillHerbloreRows;
		this.alchPile = alchPile;
		this.gearOrder = Objects.requireNonNull(gearOrder, "gearOrder");
		this.tabOrders = tabOrders.isEmpty()
			? Collections.emptyMap()
			: Collections.unmodifiableMap(new EnumMap<>(tabOrders));
		this.potionDoses = Objects.requireNonNull(potionDoses, "potionDoses");
		this.runeOrder = Objects.requireNonNull(runeOrder, "runeOrder");
		this.teleportOrder = Objects.requireNonNull(teleportOrder, "teleportOrder");
	}

	/**
	 * Whether the aligned gear setup rows may be completed with other equipment.
	 *
	 * <p>A bank tab cannot hold an empty slot, so the four combat-style columns
	 * only stay straight if real items fill the rest of each row. On, the grid
	 * holds its shape at the price of an occasional stranger in a row. Off, the
	 * tab is laid out densely and nothing sits anywhere it does not belong; sets
	 * still hold together as columns, since that is a different rule. Only
	 * consulted while the gear order is the packed grid.</p>
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
	 * the recipes simply follow each other. A sequential Herblore order implies
	 * off, since padding is a row concern.</p>
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

	/** How the combat gear tab lays out; the packed grid unless chosen otherwise. */
	public GearOrder gearOrder()
	{
		return gearOrder;
	}

	/**
	 * How a category lays its tab out; {@link TabOrder#PACKED} unless the
	 * player chose otherwise for that category.
	 */
	public TabOrder orderFor(BankCategorySortMode mode)
	{
		return tabOrders.getOrDefault(mode, TabOrder.PACKED);
	}

	/** Where part doses sit on the supplies tab. */
	public PotionDoseOrder potionDoses()
	{
		return potionDoses;
	}

	/** How runes order among themselves. */
	public RuneOrder runeOrder()
	{
		return runeOrder;
	}

	/** How single teleport items order among themselves. */
	public TeleportOrder teleportOrder()
	{
		return teleportOrder;
	}
}

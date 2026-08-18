package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;

/**
 * Real equipment stats for one item, as read from the game's item data.
 * Style, tier score and lane slot are derived from these numbers instead of
 * from item names.
 */
public final class GearStats
{
	private final GearSlot slot;
	private final int stabAttack;
	private final int slashAttack;
	private final int crushAttack;
	private final int magicAttack;
	private final int rangedAttack;
	private final int meleeStrength;
	private final int rangedStrength;
	private final int prayerBonus;
	private final int defenceSum;
	private final int stabDefence;
	private final int slashDefence;
	private final int crushDefence;
	private final int magicDefence;
	private final int rangedDefence;
	private final int magicDamageTenths;
	private final int attackSpeed;
	private final boolean comparable;

	/**
	 * Tier-only stats: enough to score and lay out an item, but not enough to
	 * prove one item beats another. {@link #dominates(GearStats)} always answers
	 * {@code false} for these.
	 */
	public GearStats(GearSlot slot, int stabAttack, int slashAttack, int crushAttack, int magicAttack,
		int rangedAttack, int meleeStrength, int rangedStrength, int prayerBonus, int defenceSum)
	{
		this(slot, stabAttack, slashAttack, crushAttack, magicAttack, rangedAttack, meleeStrength,
			rangedStrength, prayerBonus, defenceSum, 0, 0, 0, 0, 0, 0, 0, false);
	}

	/**
	 * Full stats, as read from the game's item data. {@code magicDamageTenths}
	 * carries the magic damage percentage in tenths so the comparison stays in
	 * whole numbers, and {@code attackSpeed} is the raw tick count where lower
	 * is better.
	 */
	public GearStats(GearSlot slot, int stabAttack, int slashAttack, int crushAttack, int magicAttack,
		int rangedAttack, int meleeStrength, int rangedStrength, int prayerBonus,
		int stabDefence, int slashDefence, int crushDefence, int magicDefence, int rangedDefence,
		int magicDamageTenths, int attackSpeed)
	{
		this(slot, stabAttack, slashAttack, crushAttack, magicAttack, rangedAttack, meleeStrength,
			rangedStrength, prayerBonus,
			stabDefence + slashDefence + crushDefence + magicDefence + rangedDefence,
			stabDefence, slashDefence, crushDefence, magicDefence, rangedDefence,
			magicDamageTenths, attackSpeed, true);
	}

	private GearStats(GearSlot slot, int stabAttack, int slashAttack, int crushAttack, int magicAttack,
		int rangedAttack, int meleeStrength, int rangedStrength, int prayerBonus, int defenceSum,
		int stabDefence, int slashDefence, int crushDefence, int magicDefence, int rangedDefence,
		int magicDamageTenths, int attackSpeed, boolean comparable)
	{
		this.slot = Objects.requireNonNull(slot, "slot");
		this.stabAttack = stabAttack;
		this.slashAttack = slashAttack;
		this.crushAttack = crushAttack;
		this.magicAttack = magicAttack;
		this.rangedAttack = rangedAttack;
		this.meleeStrength = meleeStrength;
		this.rangedStrength = rangedStrength;
		this.prayerBonus = prayerBonus;
		this.defenceSum = defenceSum;
		this.stabDefence = stabDefence;
		this.slashDefence = slashDefence;
		this.crushDefence = crushDefence;
		this.magicDefence = magicDefence;
		this.rangedDefence = rangedDefence;
		this.magicDamageTenths = magicDamageTenths;
		this.attackSpeed = attackSpeed;
		this.comparable = comparable;
	}

	/** True when the full stat vector is known, so items can be compared. */
	public boolean isComparable()
	{
		return comparable;
	}

	/**
	 * Pareto dominance: this item is beaten outright when {@code other} is at
	 * least equal on every stat and strictly better on at least one.
	 *
	 * <p>Unlike {@link #score()} this cannot produce a false positive. A score
	 * collapses fifteen axes into one number, so it can rank an item lower even
	 * though that item is the better choice on some axis; dominance makes a
	 * claim that holds on every axis at once.</p>
	 *
	 * <p>Fails closed: without the full stat vector on both sides, or across
	 * different equipment slots, the answer is {@code false}. Not being able to
	 * judge is not the same as being beaten.</p>
	 */
	public boolean dominates(GearStats other)
	{
		if (other == null || !comparable || !other.comparable || slot != other.slot)
		{
			return false;
		}

		int[] mine = comparisonVector();
		int[] theirs = other.comparisonVector();
		boolean strictlyBetterSomewhere = false;
		for (int axis = 0; axis < mine.length; axis++)
		{
			if (mine[axis] < theirs[axis])
			{
				return false;
			}
			if (mine[axis] > theirs[axis])
			{
				strictlyBetterSomewhere = true;
			}
		}
		return strictlyBetterSomewhere;
	}

	/** Every axis oriented so that higher is better, including attack speed. */
	private int[] comparisonVector()
	{
		return new int[]{
			stabAttack, slashAttack, crushAttack, magicAttack, rangedAttack,
			stabDefence, slashDefence, crushDefence, magicDefence, rangedDefence,
			meleeStrength, rangedStrength, magicDamageTenths, prayerBonus,
			// A lower tick count attacks faster, so negate it to keep the whole
			// vector pointing the same way.
			-attackSpeed
		};
	}

	public GearSlot getSlot()
	{
		return slot;
	}

	public GearStyle style()
	{
		int melee = Math.max(Math.max(stabAttack, slashAttack), crushAttack) + Math.max(0, meleeStrength);
		int ranged = Math.max(0, rangedAttack) + Math.max(0, rangedStrength);
		int magic = magicAttack;
		if (melee > 0 && melee >= ranged && melee >= magic)
		{
			return GearStyle.MELEE;
		}
		if (ranged > 0 && ranged >= magic)
		{
			return GearStyle.RANGED;
		}
		if (magic > 0)
		{
			return GearStyle.MAGIC;
		}
		if (prayerBonus > 0)
		{
			return GearStyle.PRAYER;
		}

		// Pure defensive pieces (plate armour) carry no positive offensive
		// bonuses at all; treat them as melee tank gear.
		return GearStyle.MELEE;
	}

	/**
	 * Lane slot rank matching GearItemSorter's row order: wearables 0-7,
	 * weapons 8-10 split by style, ammo 11. Rings rank 12 so jewellery groups
	 * with the sidegrades after the setup lanes.
	 */
	public int slotRank()
	{
		switch (slot)
		{
			case HEAD:
				return 0;
			case BODY:
				return 1;
			case LEGS:
				return 2;
			case CAPE:
				return 3;
			case NECK:
				return 4;
			case SHIELD:
				return 5;
			case HANDS:
				return 6;
			case FEET:
				return 7;
			case WEAPON:
				// Prayer has no weapon column; group prayer weapons with melee.
				return style() == GearStyle.PRAYER ? 8 : 8 + style().ordinal();
			case AMMO:
				return 11;
			case RING:
			default:
				return 12;
		}
	}

	/**
	 * Tier score: offensive bonuses dominate (weapons), defence decides
	 * between armour pieces of the same style.
	 */
	public int score()
	{
		int offence = Math.max(0, Math.max(Math.max(stabAttack, slashAttack), crushAttack))
			+ Math.max(0, meleeStrength)
			+ Math.max(0, rangedAttack)
			+ Math.max(0, rangedStrength)
			+ Math.max(0, magicAttack);
		return offence * 4 + Math.max(0, prayerBonus) * 4 + Math.max(0, defenceSum);
	}
}

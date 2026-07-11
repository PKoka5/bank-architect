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

	public GearStats(GearSlot slot, int stabAttack, int slashAttack, int crushAttack, int magicAttack,
		int rangedAttack, int meleeStrength, int rangedStrength, int prayerBonus, int defenceSum)
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

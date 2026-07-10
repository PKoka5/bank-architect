package com.pkoka5.ironmanbankarchitect.organize;

/**
 * Equipment slot of a gear piece. Kept free of RuneLite imports so the
 * organize package stays unit-testable without a client; the numeric values
 * in {@link #fromRuneLiteSlot(int)} follow RuneLite's EquipmentInventorySlot
 * indices.
 */
public enum GearSlot
{
	HEAD,
	CAPE,
	NECK,
	WEAPON,
	BODY,
	SHIELD,
	LEGS,
	HANDS,
	FEET,
	RING,
	AMMO;

	public static GearSlot fromRuneLiteSlot(int equipmentSlotIndex)
	{
		switch (equipmentSlotIndex)
		{
			case 0:
				return HEAD;
			case 1:
				return CAPE;
			case 2:
				return NECK;
			case 3:
				return WEAPON;
			case 4:
				return BODY;
			case 5:
				return SHIELD;
			case 7:
				return LEGS;
			case 9:
				return HANDS;
			case 10:
				return FEET;
			case 12:
				return RING;
			case 13:
				return AMMO;
			default:
				return null;
		}
	}
}

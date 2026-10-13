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

	private static final GearSlot[] RUNELITE_SLOTS = {HEAD,CAPE,NECK,WEAPON,BODY,SHIELD,null,LEGS,null,HANDS,FEET,null,RING,AMMO};

	public static GearSlot fromRuneLiteSlot(int equipmentSlotIndex)
	{
		return equipmentSlotIndex >= 0 && equipmentSlotIndex < RUNELITE_SLOTS.length
			? RUNELITE_SLOTS[equipmentSlotIndex] : null;
	}
}

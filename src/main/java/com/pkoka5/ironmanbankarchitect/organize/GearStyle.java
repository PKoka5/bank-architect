package com.pkoka5.ironmanbankarchitect.organize;

/** Combat style used by equipment metadata and mechanic-driven loadout facts. */
public enum GearStyle
{
	MELEE(0),
	RANGED(1),
	MAGIC(2),
	PRAYER(3),
	OTHER(4);

	private final int sortOrder;

	GearStyle(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	int sortOrder()
	{
		return sortOrder;
	}
}

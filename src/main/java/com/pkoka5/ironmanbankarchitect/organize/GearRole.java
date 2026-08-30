package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Locale;

/** Equipment or assembly role used to order and qualify reviewed gear families. */
enum GearRole
{
	HEAD(0),
	BODY(1),
	LEGS(2),
	WEAPON(3),
	CAPE(4),
	NECK(5),
	HANDS(6),
	FEET(7),
	SHIELD(8),
	RING(9),
	BASE(10),
	STAND(11),
	BARRELS(12),
	FURNACE(13);

	private final int sortOrder;

	GearRole(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	int sortOrder()
	{
		return sortOrder;
	}

	static GearRole fromCatalogValue(String value)
	{
		String normalized = value.toUpperCase(Locale.ROOT);
		if ("AMULET".equals(normalized))
		{
			return NECK;
		}
		return GearRole.valueOf(normalized);
	}
}

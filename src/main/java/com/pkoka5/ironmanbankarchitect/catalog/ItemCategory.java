package com.pkoka5.ironmanbankarchitect.catalog;

public enum ItemCategory
{
	HERBLORE,
	FARMING,
	POTION,
	GEAR,
	RUNE,
	TELEPORT,
	SKILLING,
	CURRENCY,
	CLEANUP,
	UNKNOWN;

	public String getDisplayLabel()
	{
		String lower = name().toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}

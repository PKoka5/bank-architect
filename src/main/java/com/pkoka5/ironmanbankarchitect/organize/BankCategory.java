package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;

public final class BankCategory
{
	private final String key;
	private final String name;
	private final BankCategorySortMode sortMode;

	public BankCategory(String key, String name)
	{
		this(key, name, legacySortMode(key));
	}

	public BankCategory(String key, String name, BankCategorySortMode sortMode)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		this.sortMode = Objects.requireNonNull(sortMode, "sortMode");
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public BankCategorySortMode getSortMode()
	{
		return sortMode;
	}

	private static BankCategorySortMode legacySortMode(String key)
	{
		if ("currency-utilities".equals(key)) return BankCategorySortMode.CURRENCY;
		if ("teleports-runes".equals(key)) return BankCategorySortMode.TELEPORTS;
		if ("combat-gear".equals(key)) return BankCategorySortMode.GEAR;
		if ("potions-food".equals(key)) return BankCategorySortMode.SUPPLIES;
		if ("farming-herblore".equals(key)) return BankCategorySortMode.HERBLORE;
		if ("herblore".equals(key)) return BankCategorySortMode.HERBLORE;
		if ("seeds-farming".equals(key)) return BankCategorySortMode.FARMING;
		if ("skilling-tools".equals(key)) return BankCategorySortMode.TOOLS;
		if ("resources".equals(key)) return BankCategorySortMode.RESOURCES;
		if ("slayer-boss-loot".equals(key)) return BankCategorySortMode.BOSS_LOOT;
		if ("clues-cosmetics".equals(key)) return BankCategorySortMode.CLUES;
		if ("storage-cleanup".equals(key)) return BankCategorySortMode.REVIEW;
		return BankCategorySortMode.GENERIC;
	}

	private static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}

		return value;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof BankCategory))
		{
			return false;
		}

		BankCategory that = (BankCategory) other;
		return key.equals(that.key) && name.equals(that.name) && sortMode == that.sortMode;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(key, name, sortMode);
	}
}

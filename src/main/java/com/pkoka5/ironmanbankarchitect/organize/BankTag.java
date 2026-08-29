package com.pkoka5.ironmanbankarchitect.organize;


/**
 * One independently placeable part of a blueprint category.
 *
 * <p>The ten categories are bundles: "Potions, Food &amp; PvM Supplies" is one
 * destination but three ideas. A tag is the smaller unit a player can actually
 * have an opinion about, so runes can leave the main section without dragging
 * teleports along, and food can sit apart from potions.</p>
 *
 * <p>A tag keeps the key of the category it came from. That category still owns
 * the classification rules and the sorter, so splitting a bundle changes where
 * items go without changing what they are or how they are laid out.</p>
 */
public final class BankTag
{
	private final String key;
	private final String name;
	private final String categoryKey;

	public BankTag(String key, String name, String categoryKey)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		this.categoryKey = requireText(categoryKey, "categoryKey");
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	/** The blueprint category this tag was split out of. */
	public String getCategoryKey()
	{
		return categoryKey;
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
		if (!(other instanceof BankTag))
		{
			return false;
		}

		return key.equals(((BankTag) other).key);
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public String toString()
	{
		return key;
	}
}

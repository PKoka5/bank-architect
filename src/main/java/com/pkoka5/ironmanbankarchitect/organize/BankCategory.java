package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;

public final class BankCategory
{
	private final String key;
	private final String name;

	public BankCategory(String key, String name)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
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
		return key.equals(that.key) && name.equals(that.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(key, name);
	}
}

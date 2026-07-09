package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BankPreset
{
	private final BankPresetType type;
	private final String key;
	private final String name;
	private final List<BankCategory> categories;

	public BankPreset(BankPresetType type, String key, String name, List<BankCategory> categories)
	{
		if (type == null)
		{
			throw new IllegalArgumentException("type must not be null");
		}
		if (categories == null || categories.size() != 10)
		{
			throw new IllegalArgumentException("presets must define exactly 10 categories");
		}

		this.type = type;
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		this.categories = Collections.unmodifiableList(new ArrayList<>(categories));
	}

	public BankPresetType getType()
	{
		return type;
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public List<BankCategory> getCategories()
	{
		return categories;
	}

	public BankCategory getCategory(String key)
	{
		for (BankCategory category : categories)
		{
			if (category.getKey().equals(key))
			{
				return category;
			}
		}

		throw new IllegalArgumentException("Unknown category key: " + key);
	}

	private static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}

		return value;
	}
}

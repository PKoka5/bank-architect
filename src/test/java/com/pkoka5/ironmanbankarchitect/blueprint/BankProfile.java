package com.pkoka5.ironmanbankarchitect.blueprint;

import static com.pkoka5.ironmanbankarchitect.util.TextValidation.requireText;

import java.util.*;
import java.util.List;

public final class BankProfile
{
	private final String key;
	private final String name;
	private final List<BlueprintTab> tabs;

	public BankProfile(String key, String name, List<BlueprintTab> tabs)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		if (tabs == null || tabs.isEmpty())
		{
			throw new IllegalArgumentException("tabs must not be empty");
		}

		this.tabs = Collections.unmodifiableList(new ArrayList<>(tabs));
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public List<BlueprintTab> getTabs()
	{
		return tabs;
	}

}

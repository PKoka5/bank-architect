package com.pkoka5.ironmanbankarchitect.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlueprintTab
{
	private final String key;
	private final String name;
	private final List<BlueprintSection> sections;

	public BlueprintTab(String key, String name, List<BlueprintSection> sections)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		if (sections == null || sections.isEmpty())
		{
			throw new IllegalArgumentException("sections must not be empty");
		}

		this.sections = Collections.unmodifiableList(new ArrayList<>(sections));
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public List<BlueprintSection> getSections()
	{
		return sections;
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

package com.pkoka5.ironmanbankarchitect.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlueprintSection
{
	private final String key;
	private final String name;
	private final List<VisualBlock> blocks;

	public BlueprintSection(String key, String name, List<VisualBlock> blocks)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		if (blocks == null || blocks.isEmpty())
		{
			throw new IllegalArgumentException("blocks must not be empty");
		}

		this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public List<VisualBlock> getBlocks()
	{
		return blocks;
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

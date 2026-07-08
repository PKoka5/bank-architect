package com.pkoka5.ironmanbankarchitect.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VisualBlock
{
	private final String key;
	private final String name;
	private final String typeLabel;
	private final List<BlueprintSlot> slots;

	public VisualBlock(String key, String name, String typeLabel, List<BlueprintSlot> slots)
	{
		this.key = requireText(key, "key");
		this.name = requireText(name, "name");
		this.typeLabel = requireText(typeLabel, "typeLabel");
		if (slots == null || slots.isEmpty())
		{
			throw new IllegalArgumentException("slots must not be empty");
		}

		this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
	}

	public String getKey()
	{
		return key;
	}

	public String getName()
	{
		return name;
	}

	public String getTypeLabel()
	{
		return typeLabel;
	}

	public List<BlueprintSlot> getSlots()
	{
		return slots;
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

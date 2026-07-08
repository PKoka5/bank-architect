package com.pkoka5.ironmanbankarchitect.blueprint;

import java.util.Objects;

public final class BlueprintSlot
{
	private final String key;
	private final String label;
	private final SlotKind kind;

	public BlueprintSlot(String key, String label, SlotKind kind)
	{
		this.key = requireText(key, "key");
		this.label = requireText(label, "label");
		this.kind = Objects.requireNonNull(kind, "kind");
	}

	public static BlueprintSlot gearRole(String key, String label)
	{
		return new BlueprintSlot(key, label, SlotKind.GEAR_ROLE);
	}

	public static BlueprintSlot workflowItem(String key, String label)
	{
		return new BlueprintSlot(key, label, SlotKind.WORKFLOW_ITEM);
	}

	public static BlueprintSlot empty(String key, String label)
	{
		return new BlueprintSlot(key, label, SlotKind.EMPTY);
	}

	public static BlueprintSlot empty(String label)
	{
		return empty("empty." + label.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""), label);
	}

	public String getKey()
	{
		return key;
	}

	public String getLabel()
	{
		return label;
	}

	public SlotKind getKind()
	{
		return kind;
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

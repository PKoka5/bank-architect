package com.pkoka5.ironmanbankarchitect.blueprint;

import static com.pkoka5.ironmanbankarchitect.util.TextValidation.requireText;

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
		return empty("empty." + label.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""), label);
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

}

package com.pkoka5.ironmanbankarchitect.match;

import java.util.Objects;

public final class SlotMatchResult
{
	private final String slotKey;
	private final String slotLabel;
	private final SlotMatchState state;
	private final int itemId;
	private final int quantity;

	public SlotMatchResult(String slotKey, String slotLabel, SlotMatchState state, int itemId, int quantity)
	{
		this.slotKey = requireText(slotKey, "slotKey");
		this.slotLabel = requireText(slotLabel, "slotLabel");
		this.state = Objects.requireNonNull(state, "state");
		this.itemId = itemId;
		this.quantity = quantity;
	}

	public String getSlotKey()
	{
		return slotKey;
	}

	public String getSlotLabel()
	{
		return slotLabel;
	}

	public SlotMatchState getState()
	{
		return state;
	}

	public int getItemId()
	{
		return itemId;
	}

	public int getQuantity()
	{
		return quantity;
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

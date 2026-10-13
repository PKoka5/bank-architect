package com.pkoka5.ironmanbankarchitect.match;

import static com.pkoka5.ironmanbankarchitect.util.TextValidation.requireText;

public final class SlotItemMapping
{
	private final String slotKey;
	private final String displayLabel;
	private final int itemId;

	public SlotItemMapping(String slotKey, String displayLabel, int itemId)
	{
		this.slotKey = requireText(slotKey, "slotKey");
		this.displayLabel = requireText(displayLabel, "displayLabel");
		if (itemId <= 0)
		{
			throw new IllegalArgumentException("itemId must be positive");
		}

		this.itemId = itemId;
	}

	public String getSlotKey()
	{
		return slotKey;
	}

	public String getDisplayLabel()
	{
		return displayLabel;
	}

	public int getItemId()
	{
		return itemId;
	}

}

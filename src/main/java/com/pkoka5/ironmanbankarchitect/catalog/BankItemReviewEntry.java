package com.pkoka5.ironmanbankarchitect.catalog;

public final class BankItemReviewEntry
{
	private final int itemId;
	private final String displayName;
	private final int slotIndex;

	public BankItemReviewEntry(int itemId, String displayName, int slotIndex)
	{
		this.itemId = itemId;
		this.displayName = requireText(displayName, "displayName");
		this.slotIndex = slotIndex;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getSlotIndex()
	{
		return slotIndex;
	}

	public String toCompactText()
	{
		return displayName + " (#" + itemId + ") slot " + slotIndex;
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

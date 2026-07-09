package com.pkoka5.ironmanbankarchitect.organize;

public final class BankPreviewItem
{
	private final int itemId;
	private final String displayName;
	private final int quantity;

	public BankPreviewItem(int itemId, String displayName, int quantity)
	{
		if (quantity <= 0)
		{
			throw new IllegalArgumentException("quantity must be positive");
		}

		this.itemId = itemId;
		this.displayName = requireText(displayName, "displayName");
		this.quantity = quantity;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public String toCompactLabel()
	{
		return quantity > 1 ? displayName + " x" + quantity : displayName;
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

package com.pkoka5.ironmanbankarchitect.bank;

public final class BankItemSnapshot
{
	private final int itemId;
	private final int quantity;
	private final int slotIndex;
	private final boolean placeholder;

	public BankItemSnapshot(int itemId, int quantity, int slotIndex)
	{
		this(itemId, quantity, slotIndex, false);
	}

	public BankItemSnapshot(int itemId, int quantity, int slotIndex, boolean placeholder)
	{
		this.itemId = itemId;
		this.quantity = quantity;
		this.slotIndex = slotIndex;
		this.placeholder = placeholder;
	}

	public int getItemId()
	{
		return itemId;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public int getSlotIndex()
	{
		return slotIndex;
	}

	public boolean isPlaceholder()
	{
		return placeholder;
	}
}

package com.pkoka5.ironmanbankarchitect.bank;

public final class BankItemSnapshot
{
	private final int itemId;
	private final int quantity;
	private final int slotIndex;

	public BankItemSnapshot(int itemId, int quantity, int slotIndex)
	{
		this.itemId = itemId;
		this.quantity = quantity;
		this.slotIndex = slotIndex;
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
}

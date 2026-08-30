package com.pkoka5.ironmanbankarchitect.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BankItemSnapshot
{
	private final int itemId;
	private final int quantity;
	private final int slotIndex;
	private final boolean placeholder;
	private final List<Integer> physicalSlotQuantities;

	public BankItemSnapshot(int itemId, int quantity, int slotIndex)
	{
		this(itemId, quantity, slotIndex, false);
	}

	public BankItemSnapshot(int itemId, int quantity, int slotIndex, boolean placeholder)
	{
		this(itemId, quantity, slotIndex, placeholder, Collections.singletonList(quantity));
	}

	BankItemSnapshot(int itemId, int quantity, int slotIndex, boolean placeholder,
		List<Integer> physicalSlotQuantities)
	{
		this.itemId = itemId;
		this.quantity = quantity;
		this.slotIndex = slotIndex;
		this.placeholder = placeholder;
		this.physicalSlotQuantities = Collections.unmodifiableList(
			new ArrayList<>(physicalSlotQuantities));
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

	/**
	 * Quantities of the physical bank entries represented by this logical item.
	 * A normal stack has one entry. Charge-bearing non-stackable copies can have
	 * several entries with the same item ID. A zero represents a real placeholder.
	 */
	public List<Integer> getPhysicalSlotQuantities()
	{
		return physicalSlotQuantities;
	}
}

package com.pkoka5.ironmanbankarchitect.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.runelite.api.gameval.ItemID;

public final class BankSnapshot
{
	private final List<BankItemSnapshot> items;
	private final Map<Integer, Integer> totalQuantityByItemId;

	public BankSnapshot(List<BankItemSnapshot> rawEntries)
	{
		Objects.requireNonNull(rawEntries, "rawEntries");

		Map<Integer, Integer> quantityByItemId = new LinkedHashMap<>();
		Map<Integer, Integer> firstSlotByItemId = new LinkedHashMap<>();
		Map<Integer, Boolean> placeholderByItemId = new LinkedHashMap<>();
		Map<Integer, List<Integer>> physicalSlotQuantitiesByItemId = new LinkedHashMap<>();
		List<Integer> firstSeenOrder = new ArrayList<>();

		for (BankItemSnapshot raw : rawEntries)
		{
			if (raw == null || raw.getItemId() <= 0 || raw.getItemId() == ItemID.BANK_FILLER
				|| (!raw.isPlaceholder() && raw.getQuantity() <= 0)
				|| (raw.isPlaceholder() && raw.getQuantity() != 0))
			{
				continue;
			}

			int itemId = raw.getItemId();
			if (!quantityByItemId.containsKey(itemId))
			{
				quantityByItemId.put(itemId, 0);
				firstSlotByItemId.put(itemId, raw.getSlotIndex());
				placeholderByItemId.put(itemId, raw.isPlaceholder());
				physicalSlotQuantitiesByItemId.put(itemId, new ArrayList<>());
				firstSeenOrder.add(itemId);
			}
			else if (!raw.isPlaceholder())
			{
				placeholderByItemId.put(itemId, false);
			}

			quantityByItemId.put(itemId, quantityByItemId.get(itemId) + raw.getQuantity());
			physicalSlotQuantitiesByItemId.get(itemId).addAll(raw.getPhysicalSlotQuantities());
		}

		List<BankItemSnapshot> aggregated = new ArrayList<>();
		for (int itemId : firstSeenOrder)
		{
			List<Integer> physicalSlotQuantities = physicalSlotQuantitiesByItemId.get(itemId);
			if (physicalSlotQuantities.isEmpty())
			{
				physicalSlotQuantities = Collections.singletonList(quantityByItemId.get(itemId));
			}
			aggregated.add(new BankItemSnapshot(itemId, quantityByItemId.get(itemId),
				firstSlotByItemId.get(itemId), placeholderByItemId.get(itemId), physicalSlotQuantities));
		}

		this.items = Collections.unmodifiableList(aggregated);

		Map<Integer, Integer> totals = new LinkedHashMap<>();
		for (BankItemSnapshot item : this.items)
		{
			totals.put(item.getItemId(), item.getQuantity());
		}

		this.totalQuantityByItemId = Collections.unmodifiableMap(totals);
	}

	public List<BankItemSnapshot> getItems()
	{
		return items;
	}

	public int getTotalQuantity(int itemId)
	{
		Integer total = totalQuantityByItemId.get(itemId);
		return total == null ? 0 : total;
	}

	public boolean isEmpty()
	{
		return items.isEmpty();
	}
}

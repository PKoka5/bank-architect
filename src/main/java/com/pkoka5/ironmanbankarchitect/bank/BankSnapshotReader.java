package com.pkoka5.ironmanbankarchitect.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;

public final class BankSnapshotReader
{
	private static final int[] TAB_VARBITS = {VarbitID.BANK_TAB_1, VarbitID.BANK_TAB_2,
		VarbitID.BANK_TAB_3, VarbitID.BANK_TAB_4, VarbitID.BANK_TAB_5, VarbitID.BANK_TAB_6,
		VarbitID.BANK_TAB_7, VarbitID.BANK_TAB_8, VarbitID.BANK_TAB_9};
	private BankSnapshotReader()
	{
	}

	public static Optional<BankSnapshot> readOpenBank(Client client)
	{
		Widget bankItems = client.getWidget(InterfaceID.Bankmain.ITEMS);
		boolean bankIsOpen = bankItems != null && !bankItems.isHidden();
		if (!bankIsOpen)
		{
			return Optional.empty();
		}

		ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
		if (bankContainer == null)
		{
			return Optional.empty();
		}

		Item[] items = bankContainer.getItems();
		List<BankItemSnapshot> rawEntries = new ArrayList<>();
		for (int slotIndex = 0; slotIndex < items.length; slotIndex++)
		{
			Item item = items[slotIndex];
			if (item == null)
			{
				continue;
			}

			snapshotItem(client, item, slotIndex).ifPresent(rawEntries::add);
		}

		return Optional.of(new BankSnapshot(rawEntries));
	}

	/** Read on the client thread, immediately after readOpenBank. Numbered tabs precede Main. */
	public static Optional<Map<Integer, List<Integer>>> readTabOrders(Client client, BankSnapshot snapshot)
	{
		ItemContainer container = client.getItemContainer(InventoryID.BANK);
		List<BankItemSnapshot> physical = snapshot.getPhysicalItems();
		if (container == null || physical.isEmpty() || physical.size() > 1410) return Optional.empty();
		Item[] raw = container.getItems();
		if (physical.size() > raw.length) return Optional.empty();
		List<Integer> ids = new ArrayList<>();
		for (int slot = 0; slot < raw.length; slot++)
		{
			if (slot < physical.size())
			{
				Optional<BankItemSnapshot> current = snapshotItem(client, raw[slot], slot);
				BankItemSnapshot stored = physical.get(slot);
				if (stored.getSlotIndex() != slot || !current.isPresent()
					|| stored.getItemId() != current.get().getItemId()
					|| stored.getQuantity() != current.get().getQuantity()
					|| stored.isPlaceholder() != current.get().isPlaceholder()) return Optional.empty();
				ids.add(physical.get(slot).getItemId());
			}
			else if (raw[slot] != null && raw[slot].getId() > 0) return Optional.empty();
		}
		Map<Integer, List<Integer>> tabs = new LinkedHashMap<>();
		int start = 0;
		boolean ended = false;
		for (int tab = 1; tab <= TAB_VARBITS.length; tab++)
		{
			int count = client.getVarbitValue(TAB_VARBITS[tab - 1]);
			if (count < 0 || count > ids.size() - start || ended && count > 0) return Optional.empty();
			ended |= count == 0;
			tabs.put(tab, new ArrayList<>(ids.subList(start, start + count)));
			start += count;
		}
		tabs.put(0, new ArrayList<>(ids.subList(start, ids.size())));
		return Optional.of(tabs);
	}

	private static Optional<BankItemSnapshot> snapshotItem(Client client, Item item, int slot)
	{
		if (item == null) return Optional.empty();
		ItemComposition composition = client.getItemDefinition(item.getId());
		return snapshotItem(item.getId(), item.getQuantity(),
			composition == null ? -1 : composition.getPlaceholderTemplateId(),
			composition == null ? -1 : composition.getPlaceholderId(), slot);
	}

	static boolean isSnapshotItem(int itemId, int quantity)
	{
		return itemId > 0 && quantity > 0 && itemId != ItemID.BANK_FILLER;
	}

	static Optional<BankItemSnapshot> snapshotItem(int itemId, int quantity, int placeholderTemplateId,
		int placeholderItemId, int slotIndex)
	{
		int canonicalItemId = BankItemIds.canonical(itemId, placeholderTemplateId, placeholderItemId);
		if (canonicalItemId < 0)
		{
			return Optional.empty();
		}
		if (placeholderTemplateId != -1)
		{
			return Optional.of(new BankItemSnapshot(canonicalItemId, 0, slotIndex, true));
		}
		if (!isSnapshotItem(itemId, quantity))
		{
			return Optional.empty();
		}
		return Optional.of(new BankItemSnapshot(itemId, quantity, slotIndex));
	}
}

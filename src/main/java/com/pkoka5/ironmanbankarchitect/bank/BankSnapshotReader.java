package com.pkoka5.ironmanbankarchitect.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;

public final class BankSnapshotReader
{
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

			ItemComposition composition = client.getItemDefinition(item.getId());
			int placeholderTemplateId = composition == null ? -1 : composition.getPlaceholderTemplateId();
			int placeholderItemId = composition == null ? -1 : composition.getPlaceholderId();
			snapshotItem(item.getId(), item.getQuantity(), placeholderTemplateId, placeholderItemId, slotIndex)
				.ifPresent(rawEntries::add);
		}

		return Optional.of(new BankSnapshot(rawEntries));
	}

	static boolean isSnapshotItem(int itemId, int quantity)
	{
		return itemId > 0 && quantity > 0 && itemId != ItemID.BANK_FILLER;
	}

	static Optional<BankItemSnapshot> snapshotItem(int itemId, int quantity, int placeholderTemplateId,
		int placeholderItemId, int slotIndex)
	{
		if (itemId <= 0 || itemId == ItemID.BANK_FILLER)
		{
			return Optional.empty();
		}
		if (placeholderTemplateId != -1)
		{
			int canonicalItemId = placeholderItemId > 0 ? placeholderItemId : itemId;
			return Optional.of(new BankItemSnapshot(canonicalItemId, 0, slotIndex, true));
		}
		if (!isSnapshotItem(itemId, quantity))
		{
			return Optional.empty();
		}
		return Optional.of(new BankItemSnapshot(itemId, quantity, slotIndex));
	}
}

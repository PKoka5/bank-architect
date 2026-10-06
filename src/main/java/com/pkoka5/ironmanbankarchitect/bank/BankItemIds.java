package com.pkoka5.ironmanbankarchitect.bank;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.*;


/**
 * Shared placeholder canonicalisation for bank item IDs.
 *
 * <p>A placeholder occupies a bank slot under its own item ID, so anything that
 * keys off item identity - the blueprint, the guide overlay, and player-recorded
 * category overrides - has to agree on resolving it back to the real item.
 * Keeping that single rule here stops the three call sites from drifting.</p>
 */
public final class BankItemIds
{
	private BankItemIds()
	{
	}

	/**
	 * Real item ID behind a bank slot occupant, or -1 for an empty slot or a
	 * bank filler.
	 */
	public static int canonical(int itemId, int placeholderTemplateId, int placeholderItemId)
	{
		if (itemId <= 0 || itemId == ItemID.BANK_FILLER)
		{
			return -1;
		}
		return placeholderTemplateId != -1 && placeholderItemId > 0 ? placeholderItemId : itemId;
	}

	public static int canonical(Client client, int itemId)
	{
		ItemComposition composition = client.getItemDefinition(itemId);
		return canonical(itemId, composition == null ? -1 : composition.getPlaceholderTemplateId(),
			composition == null ? -1 : composition.getPlaceholderId());
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One fixed, recognisable game item per blueprint destination.
 *
 * <p>The sidebar grid could show whatever the player happens to own in a tab,
 * but that makes the panel look different on every account and after every
 * deposit. A fixed item per destination is learnable: the same picture always
 * means the same tab.</p>
 *
 * <p>Item IDs are the canonical records from the bundled item registry, not the
 * cert, placeholder or duplicate variants that share a display name.</p>
 */
public final class CategoryIcons
{
	private static final Map<String, Integer> ITEM_ID_BY_CATEGORY_KEY = build();

	private CategoryIcons()
	{
	}

	/**
	 * Item whose sprite represents this destination, or -1 when the category has
	 * no fixed icon and the caller should fall back to the player's own items.
	 */
	public static int iconItemId(String categoryKey)
	{
		Integer itemId = categoryKey == null ? null : ITEM_ID_BY_CATEGORY_KEY.get(categoryKey);
		return itemId == null ? -1 : itemId;
	}

	private static Map<String, Integer> build()
	{
		Map<String, Integer> icons = new LinkedHashMap<>();
		icons.put("currency-utilities", 563);    // Law rune
		icons.put("combat-gear", 4151);          // Abyssal whip
		icons.put("potions-food", 385);          // Shark
		icons.put("herblore", 207);              // Grimy ranarr weed
		icons.put("seeds-farming", 5295);        // Ranarr seed
		icons.put("skilling-tools", 1275);       // Rune pickaxe
		icons.put("resources", 1511);            // Logs
		icons.put("slayer-boss-loot", 11864);    // Slayer helmet
		icons.put("clues-cosmetics", 20545);     // Reward casket (medium)
		icons.put("storage-cleanup", 1925);      // Bucket
		return Collections.unmodifiableMap(icons);
	}
}

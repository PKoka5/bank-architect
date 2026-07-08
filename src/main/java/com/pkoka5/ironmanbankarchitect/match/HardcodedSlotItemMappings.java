package com.pkoka5.ironmanbankarchitect.match;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class HardcodedSlotItemMappings
{
	private static final Map<String, SlotItemMapping> MAPPINGS_BY_SLOT_KEY = buildMappings();

	private HardcodedSlotItemMappings()
	{
	}

	public static Optional<SlotItemMapping> forSlotKey(String slotKey)
	{
		return Optional.ofNullable(MAPPINGS_BY_SLOT_KEY.get(slotKey));
	}

	private static Map<String, SlotItemMapping> buildMappings()
	{
		Map<String, SlotItemMapping> mappings = new LinkedHashMap<>();
		put(mappings, "herblore.irit.seed", "Irit seed", 5297);
		put(mappings, "herblore.irit.grimy", "Grimy irit", 209);
		put(mappings, "herblore.irit.clean", "Clean irit", 259);
		put(mappings, "herblore.irit.unf", "Irit potion (unf)", 101);
		put(mappings, "herblore.irit.secondary", "Eye of newt", 221);
		// Super attack (4), item ID 2436, is reserved for a future Potions / Consumables / PvM Supplies tab, not this Herblore prep row.
		put(mappings, "herblore.super-attack.3", "Super attack (3)", 145);
		put(mappings, "herblore.super-attack.2", "Super attack (2)", 147);
		put(mappings, "herblore.super-attack.1", "Super attack (1)", 149);

		return Collections.unmodifiableMap(mappings);
	}

	private static void put(Map<String, SlotItemMapping> mappings, String slotKey, String displayLabel, int itemId)
	{
		mappings.put(slotKey, new SlotItemMapping(slotKey, displayLabel, itemId));
	}
}

package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Seed catalog covering only the item IDs already verified for Phase B (the Irit to Super attack
 * Herblore prep row). Later phases add more items; unknown IDs are never treated as an error.
 */
public final class StaticItemCatalog implements ItemCatalog
{
	public static final StaticItemCatalog INSTANCE = new StaticItemCatalog();

	private final Map<Integer, CatalogItem> itemsById;

	private StaticItemCatalog()
	{
		this.itemsById = Collections.unmodifiableMap(buildItems());
	}

	@Override
	public Optional<CatalogItem> findById(int itemId)
	{
		return Optional.ofNullable(itemsById.get(itemId));
	}

	public boolean containsId(int itemId)
	{
		return itemsById.containsKey(itemId);
	}

	public int size()
	{
		return itemsById.size();
	}

	private static Map<Integer, CatalogItem> buildItems()
	{
		Map<Integer, CatalogItem> items = new LinkedHashMap<>();

		put(items, 5297, "Irit seed", ItemCategory.FARMING, "herb-seed",
			tags("herb-seed", "irit", "herblore-source"), "herblore.irit.seed");
		put(items, 209, "Grimy irit", ItemCategory.HERBLORE, "grimy-herb",
			tags("herb", "irit", "grimy"), "herblore.irit.grimy");
		put(items, 259, "Clean irit", ItemCategory.HERBLORE, "clean-herb",
			tags("herb", "irit", "clean"), "herblore.irit.clean");
		put(items, 101, "Irit potion (unf)", ItemCategory.HERBLORE, "unfinished-potion",
			tags("unfinished-potion", "irit"), "herblore.irit.unf");
		put(items, 221, "Eye of newt", ItemCategory.HERBLORE, "secondary",
			tags("secondary", "super-attack"), "herblore.irit.secondary");
		put(items, 145, "Super attack (3)", ItemCategory.POTION, "dose-3",
			tags("super-attack", "dose-3", "partial-dose"), "herblore.super-attack.3");
		put(items, 147, "Super attack (2)", ItemCategory.POTION, "dose-2",
			tags("super-attack", "dose-2", "partial-dose"), "herblore.super-attack.2");
		put(items, 149, "Super attack (1)", ItemCategory.POTION, "dose-1",
			tags("super-attack", "dose-1", "partial-dose"), "herblore.super-attack.1");

		return items;
	}

	private static void put(Map<Integer, CatalogItem> items, int itemId, String displayName, ItemCategory category,
		String subcategory, Set<String> tags, String workflowKey)
	{
		if (items.containsKey(itemId))
		{
			throw new IllegalStateException("Duplicate catalog item ID: " + itemId);
		}

		items.put(itemId, new CatalogItem(itemId, displayName, category, subcategory, tags, workflowKey));
	}

	private static Set<String> tags(String... values)
	{
		return new LinkedHashSet<>(Arrays.asList(values));
	}
}

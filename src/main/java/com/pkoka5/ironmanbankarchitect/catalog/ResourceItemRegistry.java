package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ResourceItemRegistry implements ItemCatalog
{
	static final String RESOURCE_PATH = "/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";

	public static final ResourceItemRegistry INSTANCE = new ResourceItemRegistry();

	private final Map<Integer, CatalogItem> itemsById;

	private ResourceItemRegistry()
	{
		this.itemsById = Collections.unmodifiableMap(loadItems());
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

	private static Map<Integer, CatalogItem> loadItems()
	{
		InputStream stream = ResourceItemRegistry.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing item registry resource: " + RESOURCE_PATH);
		}

		Map<Integer, CatalogItem> items = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().isEmpty())
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length < 2)
				{
					throw new IllegalStateException("Invalid item registry line: " + line);
				}

				String itemIdText = fields[0];
				if (!itemIdText.isEmpty() && itemIdText.charAt(0) == '\uFEFF')
				{
					itemIdText = itemIdText.substring(1);
				}

				int itemId = Integer.parseInt(itemIdText);
				String displayName = fields[1];
				ItemCategory category = parseRegistryCategory(fields.length >= 3 ? fields[2] : "");
				items.putIfAbsent(itemId, new CatalogItem(itemId, displayName, category,
					category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null));
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to load item registry resource", ex);
		}

		return items;
	}

	private static ItemCategory parseRegistryCategory(String value)
	{
		if (value == null || value.trim().isEmpty() || "UNKNOWN".equals(value))
		{
			return ItemCategory.UNCATEGORIZED;
		}

		try
		{
			return ItemCategory.valueOf(value);
		}
		catch (IllegalArgumentException ex)
		{
			return ItemCategory.UNCATEGORIZED;
		}
	}
}

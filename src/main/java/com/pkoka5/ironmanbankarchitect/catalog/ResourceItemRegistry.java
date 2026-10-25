package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.util.BundledTable;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;

/** Exact bundled facts compiled and checked by the development-only RegistryCompiler. */
public final class ResourceItemRegistry implements ItemCatalog
{
	static final String RESOURCE_PATH = "/com/pkoka5/ironmanbankarchitect/catalog/compiled-item-registry.tsv";
	public static final ResourceItemRegistry INSTANCE = new ResourceItemRegistry(
		() -> ResourceItemRegistry.class.getResourceAsStream(RESOURCE_PATH));
	private final RequiredResource<Map<Integer, CatalogItem>> itemsById;

	ResourceItemRegistry(InputStream stream)
	{
		this(() -> stream);
	}

	private ResourceItemRegistry(Supplier<InputStream> source)
	{
		itemsById = new RequiredResource<>("compiled item registry", () -> loadItems(source.get()));
	}

	@Override public void requireAvailable()
	{
		itemsById.get();
		GearTierCatalog.INSTANCE.size();
		WikiItemLists.INSTANCE.requireAvailable();
		com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog.requireAvailable();
		ClassificationNames.group(0);
		ResourceItemSortMetadataCatalog.INSTANCE.size();
	}

	@Override public Optional<CatalogItem> findById(int itemId)
	{
		requireAvailable();
		return Optional.ofNullable(itemsById.get().get(itemId));
	}

	public boolean containsId(int itemId) { return findById(itemId).isPresent(); }
	public int size() { requireAvailable(); return itemsById.get().size(); }

	static Map<Integer, CatalogItem> loadItems(InputStream stream)
	{
		Map<Integer, CatalogItem> result = new LinkedHashMap<>();
		try
		{
			BundledTable.read(stream, RESOURCE_PATH, false, false, (line, fields) -> {
				if (fields.length != 5) throw new IllegalStateException("Invalid compiled item row: " + line);
				int id = Integer.parseInt(fields[0]);
				Set<String> tags = new LinkedHashSet<>();
				if (!fields[4].isEmpty()) for (String tag : fields[4].split(",", -1))
					if (!tag.matches("[a-z][a-z0-9-]*") || !tags.add(tag))
						throw new IllegalStateException("Invalid compiled item tags: " + line);
				CatalogItem item = new CatalogItem(id, fields[1], ItemCategory.valueOf(fields[2]), fields[3], tags, null);
				if (result.putIfAbsent(id, item) != null) throw new IllegalStateException("Duplicate compiled item: " + id);
			});
		}
		catch (IOException ex) { throw new IllegalStateException("Cannot read compiled item registry", ex); }
		if (result.isEmpty()) throw new IllegalStateException("Empty compiled item registry");
		return Collections.unmodifiableMap(result);
	}
}

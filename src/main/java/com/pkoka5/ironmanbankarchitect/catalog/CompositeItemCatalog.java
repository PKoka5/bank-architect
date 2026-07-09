package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompositeItemCatalog implements ItemCatalog
{
	public static final CompositeItemCatalog DEFAULT = new CompositeItemCatalog(
		StaticItemCatalog.INSTANCE,
		ResourceItemRegistry.INSTANCE
	);

	private final List<ItemCatalog> catalogs;

	public CompositeItemCatalog(ItemCatalog... catalogs)
	{
		this.catalogs = Collections.unmodifiableList(Arrays.asList(Objects.requireNonNull(catalogs, "catalogs")));
		if (this.catalogs.isEmpty())
		{
			throw new IllegalArgumentException("catalogs must not be empty");
		}
	}

	@Override
	public Optional<CatalogItem> findById(int itemId)
	{
		for (ItemCatalog catalog : catalogs)
		{
			Optional<CatalogItem> item = catalog.findById(itemId);
			if (item.isPresent())
			{
				return item;
			}
		}

		return Optional.empty();
	}
}

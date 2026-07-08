package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Optional;

public interface ItemCatalog
{
	Optional<CatalogItem> findById(int itemId);

	default CatalogItem describeOrUnknown(int itemId)
	{
		return findById(itemId).orElseGet(() -> CatalogItem.unknown(itemId));
	}
}

package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Optional;

@FunctionalInterface
public interface ItemSortMetadataCatalog
{
	Optional<ItemSortMetadata> findById(int itemId);
}

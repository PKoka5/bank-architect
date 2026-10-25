package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Optional;

@FunctionalInterface
public interface ItemSortMetadataCatalog
{
	Optional<ItemSortMetadata> findById(int itemId);

	/** Required layout facts must agree with the reviewed family and variant. */
	default void requireVariant(int itemId, String family, ItemSortMetadata.VariantKind kind, int value)
	{
		ItemSortMetadata metadata = findById(itemId).orElseThrow(() ->
			new IllegalStateException("Missing semantic metadata for itemId " + itemId));
		if (!family.equals(metadata.getFamilyKey()) || metadata.getVariantKind() != kind
			|| metadata.getVariantValue() != value)
			throw new IllegalStateException("Semantic metadata mismatch for itemId " + itemId
				+ ": expected family=" + family + ", kind=" + kind + ", value=" + value
				+ " but was family=" + metadata.getFamilyKey() + ", kind=" + metadata.getVariantKind()
				+ ", value=" + metadata.getVariantValue());
	}
}

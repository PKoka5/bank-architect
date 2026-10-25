package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.*;
import com.pkoka5.ironmanbankarchitect.util.BundledTable;
import java.util.*;




/**
 * Exact canonical item-ID exceptions loaded from canonical-item-classification-overrides.tsv.
 * Avoids matching cert, placeholder and Battle Royale duplicates by name. Unknown IDs have no
 * override. Table comments and notes preserve per-entry evidence.
 */
final class CanonicalItemClassificationOverrides
{
	private static final String RESOURCE_PATH = "canonical-item-classification-overrides.tsv";
	static final CanonicalItemClassificationOverrides INSTANCE = new CanonicalItemClassificationOverrides(
		CanonicalItemClassificationOverrides.class.getResourceAsStream(RESOURCE_PATH));
	private Map<Integer, ItemClassificationRefiner.Classification> overrides = Collections.emptyMap();
	private CatalogUnavailableException failure;

	CanonicalItemClassificationOverrides(InputStream stream)
	{
		try
		{
			overrides = load(stream);
		}
		catch (IllegalStateException | IllegalArgumentException ex)
		{
			failure = new CatalogUnavailableException(ex);
		}
	}

	void requireAvailable()
	{
		if (failure != null) throw failure;
	}

	static Optional<ItemClassificationRefiner.Classification> find(int itemId)
	{
		return INSTANCE.lookup(itemId);
	}

	Optional<ItemClassificationRefiner.Classification> lookup(int itemId)
	{
		requireAvailable();
		return Optional.ofNullable(overrides.get(itemId));
	}

	private static Map<Integer, ItemClassificationRefiner.Classification> load(InputStream stream)
	{
		Map<Integer, ItemClassificationRefiner.Classification> overrides = new LinkedHashMap<>();
		try
		{
			BundledTable.read(stream, RESOURCE_PATH, false, true, (line, columns) ->
			{
				// A fourth column names the item for a reader of the file; the
				// classification is decided by the first three.
				if (columns.length < 3)
				{
					throw new IllegalStateException("Malformed override row: " + line);
				}
				int itemId = Integer.parseInt(columns[0]);
				ItemCategory category = ItemCategory.valueOf(columns[1]);
				String subcategory = columns[2];
				if (itemId <= 0 || subcategory.isEmpty())
				{
					throw new IllegalStateException("Invalid override row: " + line);
				}
				ItemClassificationRefiner.Classification previous = overrides.put(itemId,
					new ItemClassificationRefiner.Classification(category, subcategory));
				if (previous != null)
				{
					throw new IllegalStateException("Item ID " + itemId + " is overridden twice");
				}
			});
		}
		catch (IOException | IllegalArgumentException e)
		{
			throw new IllegalStateException("Failed to read the override table", e);
		}
		if (overrides.isEmpty()) throw new IllegalStateException("Empty override table");
		return Collections.unmodifiableMap(overrides);
	}
}

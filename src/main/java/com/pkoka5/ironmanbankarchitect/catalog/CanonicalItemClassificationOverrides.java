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

/**
 * Exact item-ID overrides for canonical player-facing classification
 * exceptions (equipment, teleports, Herblore secondaries, and other
 * resources), not exclusively equipment.
 *
 * <p>Used only where a broad display-name rule would also sweep in cert,
 * placeholder, or Battle Royale duplicate records that share a display name
 * or constant family with the real item (for example "Avernic treads (max)"
 * at ID 31097 versus the unrelated Battle Royale duplicate at ID 33172, or
 * the three identically-named "Amethyst" records where only ID 21347 is the
 * real drop). Unknown item IDs receive no override.</p>
 *
 * <p>The table itself lives in {@code canonical-item-classification-overrides.tsv}
 * beside this class. It was 3,635 lines of case labels here until September
 * 2026: a lookup table wearing a switch statement, and by then a seventh of
 * every Java source in the plugin. The reasoning each entry carries - the wiki
 * revision it was read from, the duplicate it disambiguates - moved with it,
 * as comment lines and a trailing note column.</p>
 */
final class CanonicalItemClassificationOverrides
{
	private static final String RESOURCE_PATH = "canonical-item-classification-overrides.tsv";
	private static final String SCHEMA_HEADER = "# schema=1";
	private static final Map<Integer, ItemClassificationRefiner.Classification> OVERRIDES = load();

	private CanonicalItemClassificationOverrides()
	{
	}

	static Optional<ItemClassificationRefiner.Classification> find(int itemId)
	{
		return Optional.ofNullable(OVERRIDES.get(itemId));
	}

	private static Map<Integer, ItemClassificationRefiner.Classification> load()
	{
		InputStream stream = CanonicalItemClassificationOverrides.class
			.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing override table: " + RESOURCE_PATH);
		}

		Map<Integer, ItemClassificationRefiner.Classification> overrides = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (header == null || !header.startsWith(SCHEMA_HEADER))
			{
				throw new IllegalStateException("Unexpected override table schema: " + header);
			}
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				// A fourth column names the item for a reader of the file; the
				// classification is decided by the first three.
				String[] columns = line.split("\t", -1);
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
			}
		}
		catch (IOException | IllegalArgumentException e)
		{
			throw new IllegalStateException("Failed to read the override table", e);
		}
		return Collections.unmodifiableMap(overrides);
	}
}

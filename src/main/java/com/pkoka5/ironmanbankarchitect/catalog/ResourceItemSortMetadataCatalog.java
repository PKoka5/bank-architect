package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ResourceItemSortMetadataCatalog implements ItemSortMetadataCatalog
{
	private static final String SCHEMA_HEADER = "# schema=1";

	static final String METADATA_RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata.tsv";
	static final String SOURCES_RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata-sources.tsv";

	public static final ResourceItemSortMetadataCatalog INSTANCE =
		new ResourceItemSortMetadataCatalog();

	private final Map<Integer, ItemSortMetadata> metadataById;
	private final Set<String> sourceKeys;

	private ResourceItemSortMetadataCatalog()
	{
		this.sourceKeys = Collections.unmodifiableSet(loadSourceKeys(openRequired(SOURCES_RESOURCE_PATH)));
		this.metadataById = Collections.unmodifiableMap(
			loadMetadata(openRequired(METADATA_RESOURCE_PATH), sourceKeys));
	}

	@Override
	public Optional<ItemSortMetadata> findById(int itemId)
	{
		return Optional.ofNullable(metadataById.get(itemId));
	}

	public int size()
	{
		return metadataById.size();
	}

	Collection<ItemSortMetadata> entries()
	{
		return metadataById.values();
	}

	Set<String> sourceKeys()
	{
		return sourceKeys;
	}

	static Map<Integer, ItemSortMetadata> loadMetadata(InputStream stream, Set<String> knownSourceKeys)
	{
		if (stream == null)
		{
			throw new IllegalArgumentException("metadata stream must not be null");
		}
		if (knownSourceKeys == null)
		{
			throw new IllegalArgumentException("knownSourceKeys must not be null");
		}

		Map<Integer, ItemSortMetadata> result = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			int lineNumber = 0;
			boolean schemaSeen = false;
			while ((line = reader.readLine()) != null)
			{
				lineNumber++;
				line = stripBom(line);
				if (!schemaSeen)
				{
					if (line.trim().isEmpty())
					{
						continue;
					}
					if (!SCHEMA_HEADER.equals(line.trim()))
					{
						throw invalid("first non-empty line must be " + SCHEMA_HEADER, lineNumber, null);
					}
					schemaSeen = true;
					continue;
				}
				if (isIgnored(line))
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length != 11)
				{
					throw invalid("expected exactly 11 tab-separated fields", lineNumber, null);
				}

				try
				{
					int itemId = parseInt(fields[0], "item_id");
					String sourceKey = fields[10];
					if (!knownSourceKeys.contains(sourceKey))
					{
						throw new IllegalArgumentException("unknown sourceKey: " + sourceKey);
					}

					ItemSortMetadata metadata = new ItemSortMetadata(itemId, fields[1],
						parseEnum(ItemSortMetadata.VariantKind.class, fields[2], "variant_kind"),
						parseInt(fields[3], "variant_value"),
						parseEnum(ItemSortMetadata.FoodRole.class, fields[4], "food_role"),
						parseEnum(ItemSortMetadata.HealModel.class, fields[5], "heal_model"),
						parseInt(fields[6], "immediate_heal_min"),
						parseInt(fields[7], "immediate_heal_max"),
						parseInt(fields[8], "secondary_heal"),
						parseEnum(ItemSortMetadata.AreaRestriction.class, fields[9], "area_restriction"),
						sourceKey);
					if (result.putIfAbsent(itemId, metadata) != null)
					{
						throw new IllegalArgumentException("duplicate item_id: " + itemId);
					}
				}
				catch (IllegalArgumentException ex)
				{
					throw invalid(ex.getMessage(), lineNumber, ex);
				}
			}
			if (!schemaSeen)
			{
				throw invalid("missing " + SCHEMA_HEADER, lineNumber, null);
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to read item sort metadata", ex);
		}
		return result;
	}

	static Set<String> loadSourceKeys(InputStream stream)
	{
		if (stream == null)
		{
			throw new IllegalArgumentException("source stream must not be null");
		}

		Set<String> result = new LinkedHashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			int lineNumber = 0;
			boolean schemaSeen = false;
			while ((line = reader.readLine()) != null)
			{
				lineNumber++;
				line = stripBom(line);
				if (!schemaSeen)
				{
					if (line.trim().isEmpty())
					{
						continue;
					}
					if (!SCHEMA_HEADER.equals(line.trim()))
					{
						throw invalidSource("first non-empty line must be " + SCHEMA_HEADER,
							lineNumber, null);
					}
					schemaSeen = true;
					continue;
				}
				if (isIgnored(line))
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length != 5)
				{
					throw invalidSource("expected exactly 5 tab-separated fields", lineNumber, null);
				}

				try
				{
					String sourceKey = ItemSortMetadata.requireKey(fields[0], "source_key");
					URI uri = URI.create(requireText(fields[1], "source_url"));
					if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null)
					{
						throw new IllegalArgumentException("source_url must be an absolute HTTPS URL");
					}
					LocalDate.parse(requireText(fields[2], "retrieved_on"));
					String revision = requireText(fields[3], "revision");
					if ("oldschool.runescape.wiki".equalsIgnoreCase(uri.getHost())
						&& !hasQueryParameter(uri, "oldid", revision))
					{
						throw new IllegalArgumentException("Wiki source_url oldid must match revision");
					}
					String license = requireText(fields[4], "license");
					if ("oldschool.runescape.wiki".equalsIgnoreCase(uri.getHost()))
					{
						if (!revision.matches("\\d+"))
						{
							throw new IllegalArgumentException("Wiki revision must be numeric");
						}
						if (!"CC BY-NC-SA 3.0".equals(license))
						{
							throw new IllegalArgumentException("Wiki license must be CC BY-NC-SA 3.0");
						}
					}
					if (!result.add(sourceKey))
					{
						throw new IllegalArgumentException("duplicate source_key: " + sourceKey);
					}
				}
				catch (IllegalArgumentException | DateTimeParseException ex)
				{
					throw invalidSource(ex.getMessage(), lineNumber, ex);
				}
			}
			if (!schemaSeen)
			{
				throw invalidSource("missing " + SCHEMA_HEADER, lineNumber, null);
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to read item sort metadata sources", ex);
		}
		return result;
	}

	private static InputStream openRequired(String resourcePath)
	{
		InputStream stream = ResourceItemSortMetadataCatalog.class.getResourceAsStream(resourcePath);
		if (stream == null)
		{
			throw new IllegalStateException("Missing item sort resource: " + resourcePath);
		}
		return stream;
	}

	private static boolean isIgnored(String line)
	{
		String trimmed = line.trim();
		return trimmed.isEmpty() || trimmed.startsWith("#");
	}

	private static String stripBom(String value)
	{
		return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
	}

	private static boolean hasQueryParameter(URI uri, String key, String expectedValue)
	{
		String query = uri.getRawQuery();
		if (query == null)
		{
			return false;
		}
		for (String parameter : query.split("&"))
		{
			String[] parts = parameter.split("=", 2);
			if (parts.length == 2 && key.equals(parts[0]) && expectedValue.equals(parts[1]))
			{
				return true;
			}
		}
		return false;
	}

	private static int parseInt(String value, String field)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException(field + " must be an integer", ex);
		}
	}

	private static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String field)
	{
		try
		{
			return Enum.valueOf(enumType, value);
		}
		catch (IllegalArgumentException ex)
		{
			throw new IllegalArgumentException("invalid " + field + ": " + value, ex);
		}
	}

	private static String requireText(String value, String field)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(field + " must not be blank");
		}
		return value;
	}

	private static IllegalStateException invalid(String message, int lineNumber, Throwable cause)
	{
		return new IllegalStateException("Invalid item sort metadata at line " + lineNumber + ": " + message,
			cause);
	}

	private static IllegalStateException invalidSource(String message, int lineNumber, Throwable cause)
	{
		return new IllegalStateException("Invalid item sort metadata source at line " + lineNumber + ": "
			+ message, cause);
	}
}

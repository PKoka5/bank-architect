package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Exact-ID gear progression stages (1 = Starter through 5 = End) curated from Wiki-verified
 * research in {@code tools/research/gear-progression-exact-ids.tsv}. An item absent from this
 * catalog has no curated tier and callers must fall back to other scoring evidence; absence is
 * never treated as tier 0.
 */
public final class GearTierCatalog
{
	private static final String SCHEMA_HEADER = "# schema=1";

	static final String RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/gear-tier-catalog.tsv";

	public static final GearTierCatalog INSTANCE = new GearTierCatalog();

	private final Map<Integer, Integer> stageById;
	private final Map<String, Integer> stageByNormalizedName;

	private GearTierCatalog()
	{
		CatalogData data = load(openRequired(RESOURCE_PATH));
		this.stageById = Collections.unmodifiableMap(data.stageById);
		this.stageByNormalizedName = Collections.unmodifiableMap(data.stageByNormalizedName);
	}

	public OptionalInt tierOf(int itemId)
	{
		Integer stage = stageById.get(itemId);
		return stage == null ? OptionalInt.empty() : OptionalInt.of(stage);
	}

	/** Exact ID first; controlled state/aesthetic fallback only when the curated name is unambiguous. */
	public OptionalInt tierOf(int itemId, String displayName)
	{
		OptionalInt exact = tierOf(itemId);
		if (exact.isPresent())
		{
			return exact;
		}
		Integer stage = stageByNormalizedName.get(normalizeTierName(displayName));
		return stage == null ? OptionalInt.empty() : OptionalInt.of(stage);
	}

	public int size()
	{
		return stageById.size();
	}

	private static CatalogData load(InputStream stream)
	{
		Map<Integer, Integer> stages = new LinkedHashMap<>();
		Map<String, Integer> normalizedStages = new LinkedHashMap<>();
		Set<String> ambiguousNames = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (header == null || !header.trim().equals(SCHEMA_HEADER))
			{
				throw new IllegalStateException("Unexpected gear tier catalog schema header: " + header);
			}

			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] columns = line.split("\t", -1);
				if (columns.length < 3)
				{
					throw new IllegalStateException("Malformed gear tier catalog row: " + line);
				}
				int itemId = Integer.parseInt(columns[0]);
				int stage = Integer.parseInt(columns[1]);
				if (itemId <= 0 || stage < 1 || stage > 5)
				{
					throw new IllegalStateException("Gear tier catalog row out of range: " + line);
				}
				if (stages.put(itemId, stage) != null)
				{
					throw new IllegalStateException("Duplicate gear tier catalog itemId: " + itemId);
				}
				String normalizedName = normalizeTierName(columns[2]);
				Integer earlierStage = normalizedStages.putIfAbsent(normalizedName, stage);
				if (earlierStage != null && earlierStage != stage)
				{
					ambiguousNames.add(normalizedName);
				}
			}
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Failed to read gear tier catalog", e);
		}
		for (String ambiguousName : ambiguousNames)
		{
			normalizedStages.remove(ambiguousName);
		}
		return new CatalogData(stages, normalizedStages);
	}

	private static String normalizeTierName(String value)
	{
		if (value == null)
		{
			return "";
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		normalized = normalized.replaceFirst("\\s+(100|75|50|25|0)$", "");
		String earlier;
		do
		{
			earlier = normalized;
			normalized = normalized.replaceFirst(
				"\\s*\\((uncharged|empty|inactive|c|p|p\\+|p\\+\\+|i|or|t|g)\\)$", "");
		}
		while (!normalized.equals(earlier));
		return normalized.trim();
	}

	private static final class CatalogData
	{
		private final Map<Integer, Integer> stageById;
		private final Map<String, Integer> stageByNormalizedName;

		private CatalogData(Map<Integer, Integer> stageById,
			Map<String, Integer> stageByNormalizedName)
		{
			this.stageById = stageById;
			this.stageByNormalizedName = stageByNormalizedName;
		}
	}

	private static InputStream openRequired(String resourcePath)
	{
		InputStream stream = GearTierCatalog.class.getResourceAsStream(resourcePath);
		if (stream == null)
		{
			throw new IllegalStateException("Missing gear tier catalog resource: " + resourcePath);
		}
		return stream;
	}
}

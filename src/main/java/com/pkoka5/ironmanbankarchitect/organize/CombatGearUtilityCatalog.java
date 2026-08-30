package com.pkoka5.ironmanbankarchitect.organize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Reviewed score adjustments for combat value that equipment stats cannot express. */
final class CombatGearUtilityCatalog
{
	private static final String RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/organize/combat-gear-utility.tsv";
	private static final String SCHEMA_HEADER = "# schema=2";
	static final CombatGearUtilityCatalog INSTANCE = new CombatGearUtilityCatalog();

	private volatile Catalog catalog;

	private CombatGearUtilityCatalog()
	{
	}

	int itemScore(int itemId)
	{
		Integer score = catalog().itemScores.get(itemId);
		return score == null ? 0 : score;
	}

	int activeItemScore(int itemId)
	{
		return Math.max(0, itemScore(itemId));
	}

	int loadoutScore(String loadoutKey)
	{
		Integer score = catalog().loadoutScores.get(loadoutKey);
		return score == null ? 0 : score;
	}

	private Catalog catalog()
	{
		Catalog current = catalog;
		if (current != null)
		{
			return current;
		}
		synchronized (this)
		{
			if (catalog == null)
			{
				catalog = loadResource();
			}
			return catalog;
		}
	}

	private static Catalog loadResource()
	{
		InputStream stream = CombatGearUtilityCatalog.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing combat gear utility catalog: " + RESOURCE_PATH);
		}
		return load(stream, CombatGearFacts.loadoutKeys());
	}

	static Catalog load(InputStream stream, Set<String> knownLoadoutKeys)
	{
		if (stream == null)
		{
			throw new IllegalArgumentException("stream must not be null");
		}
		if (knownLoadoutKeys == null)
		{
			throw new IllegalArgumentException("knownLoadoutKeys must not be null");
		}
		Map<Integer, Integer> scores = new LinkedHashMap<>();
		Map<String, Integer> loadoutScores = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (!SCHEMA_HEADER.equals(header))
			{
				throw new IllegalStateException("Unexpected combat gear utility schema: " + header);
			}
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] columns = line.split("\t", -1);
				if (columns.length != 4)
				{
					throw new IllegalStateException("Malformed combat gear utility row: " + line);
				}
				String kind = columns[0];
				int score = Integer.parseInt(columns[1]);
				if (score < -2000 || score > 2000)
				{
					throw new IllegalStateException("Combat gear utility score out of range: " + line);
				}
				if ("loadout".equals(kind))
				{
					for (String key : columns[2].split(","))
					{
						if (!key.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
							|| !knownLoadoutKeys.contains(key)
							|| loadoutScores.put(key, score) != null)
						{
							throw new IllegalStateException("Duplicate or invalid utility loadout key: " + key);
						}
					}
					continue;
				}
				if (!"item".equals(kind))
				{
					throw new IllegalStateException("Unknown combat gear utility kind: " + kind);
				}
				for (String value : columns[2].split(","))
				{
					int itemId = Integer.parseInt(value);
					if (itemId <= 0 || scores.put(itemId, score) != null)
					{
						throw new IllegalStateException("Duplicate or invalid utility item ID: " + itemId);
					}
				}
			}
		}
		catch (IOException | NumberFormatException e)
		{
			throw new IllegalStateException("Failed to read combat gear utility catalog", e);
		}
		return new Catalog(Collections.unmodifiableMap(scores),
			Collections.unmodifiableMap(loadoutScores));
	}

	static final class Catalog
	{
		private final Map<Integer, Integer> itemScores;
		private final Map<String, Integer> loadoutScores;

		private Catalog(Map<Integer, Integer> itemScores, Map<String, Integer> loadoutScores)
		{
			this.itemScores = itemScores;
			this.loadoutScores = loadoutScores;
		}
	}
}

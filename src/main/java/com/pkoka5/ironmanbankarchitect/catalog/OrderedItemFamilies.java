package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ordered item families with configurable overlap between rows. Loading failure is retained until analysis asks for the data. */
public final class OrderedItemFamilies
{
	private Map<String, List<Integer>> families = Collections.emptyMap();
	private CatalogUnavailableException failure;

	public OrderedItemFamilies(InputStream stream, int requiredWidth)
	{
		this(stream, requiredWidth, true);
	}

	/** Some layout tables deliberately reference the same item in different rows. */
	public OrderedItemFamilies(InputStream stream, int requiredWidth, boolean disjoint)
	{
		try
		{
			if (stream == null) throw new IOException("Missing item-family resource");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
			{
				if (!"# schema=1".equals(reader.readLine())) throw new IOException("Invalid item-family schema");
				Map<String, List<Integer>> parsed = new LinkedHashMap<>();
				Set<Integer> seen = new HashSet<>();
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.isEmpty() || line.startsWith("#")) continue;
					String[] fields = line.split("\t", -1);
					if (fields.length != 2 || fields[0].trim().isEmpty()) throw new IOException("Invalid family row: " + line);
					List<Integer> ids = new ArrayList<>();
					if (!disjoint) seen.clear();
					for (String value : fields[1].split(",", -1))
					{
						int id = Integer.parseInt(value);
						if (id <= 0 || !seen.add(id)) throw new IOException("Invalid or repeated family item: " + id);
						ids.add(id);
					}
					if (requiredWidth > 0 && ids.size() != requiredWidth) throw new IOException("Invalid family width: " + fields[0]);
					if (parsed.put(fields[0], Collections.unmodifiableList(ids)) != null) throw new IOException("Duplicate family: " + fields[0]);
				}
				if (parsed.isEmpty()) throw new IOException("Empty item-family table");
				families = Collections.unmodifiableMap(parsed);
			}
		}
		catch (IOException | IllegalArgumentException ex)
		{
			failure = new CatalogUnavailableException(
				"Bank analysis is unavailable because a required item layout table is missing or invalid.", ex);
		}
	}

	public Map<String, List<Integer>> entries()
	{
		if (failure != null) throw failure;
		return families;
	}

	public List<Integer> ids(String key)
	{
		List<Integer> ids = entries().get(key);
		if (ids == null) throw new CatalogUnavailableException(
			"Bank analysis is unavailable because a required item layout row is missing.",
			new IllegalStateException("Missing layout row: " + key));
		return ids;
	}

	/** Named groups retain declaration order and expose the original family keys. */
	public Map<String, List<Integer>> group(String prefix)
	{
		Map<String, List<Integer>> group = new LinkedHashMap<>();
		entries().forEach((key, ids) ->
		{
			if (key.startsWith(prefix + "/")) group.put(key.substring(prefix.length() + 1), ids);
		});
		if (group.isEmpty()) throw new CatalogUnavailableException(
			"Bank analysis is unavailable because a required item layout group is missing.",
			new IllegalStateException("Missing layout group: " + prefix));
		return Collections.unmodifiableMap(group);
	}
}

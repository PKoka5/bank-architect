package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;






/** Exact-ID usage facts; these never choose a physical bank destination. */
final class ItemUsageTags
{
	private static final RequiredResource<Map<Integer, Set<String>>> DATA = new RequiredResource<>("item usage tags",
		() -> load(ItemUsageTags.class.getResourceAsStream("item-usage-tags.tsv")));

	static Set<String> forItem(int itemId)
	{
		return DATA.get().getOrDefault(itemId, Collections.emptySet());
	}

	static Map<Integer, Set<String>> load(InputStream stream)
	{
		if (stream == null) throw new IllegalStateException("Missing item usage tags");
		Map<Integer, Set<String>> result = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			if (!"# schema=1".equals(reader.readLine())) throw new IllegalStateException("Invalid usage tag schema");
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] fields = line.split("\t", -1);
				if (fields.length != 2) throw new IllegalStateException("Invalid usage tag row: " + line);
				int id = Integer.parseInt(fields[0]);
				String[] values = fields[1].split(",", -1);
				Set<String> tags = new LinkedHashSet<>(Arrays.asList(values));
				if (id <= 0 || tags.size() != values.length
					|| tags.stream().anyMatch(tag -> !tag.matches("[a-z][a-z0-9-]*"))
					|| result.putIfAbsent(id, Collections.unmodifiableSet(tags)) != null)
					throw new IllegalStateException("Invalid or duplicate usage tags: " + line);
			}
		}
		catch (IOException | IllegalArgumentException ex)
		{
			throw new IllegalStateException("Cannot read item usage tags", ex);
		}
		if (result.isEmpty()) throw new IllegalStateException("Empty item usage tags");
		return Collections.unmodifiableMap(result);
	}
}

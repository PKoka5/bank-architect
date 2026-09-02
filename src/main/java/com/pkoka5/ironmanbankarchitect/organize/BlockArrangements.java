package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The player's saved block orders, one list of block keys per tag.
 *
 * <p>An absent tag means the curated order stands; the empty arrangement is a
 * statement no one has made. Serialized behind a version token with separators
 * outside the family every other stored format already claims (~ ; | + , =),
 * so a future revision can ride beside plans and profiles without colliding.
 * Entries a parser does not recognize are preserved verbatim on rewrite: a
 * player who runs an older build must not lose a newer build's statements.</p>
 */
public final class BlockArrangements
{
	static final String VERSION = "v1";
	private static final String ENTRY_SEPARATOR = "!";
	private static final String TAG_SEPARATOR = ">";
	private static final String KEY_SEPARATOR = "^";

	public static final BlockArrangements EMPTY =
		new BlockArrangements(Collections.emptyMap(), Collections.emptyList());

	private final Map<String, List<String>> ordersByTag;
	private final List<String> unrecognized;

	private BlockArrangements(Map<String, List<String>> ordersByTag, List<String> unrecognized)
	{
		Map<String, List<String>> orders = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : ordersByTag.entrySet())
		{
			orders.put(entry.getKey(),
				Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
		}
		this.ordersByTag = Collections.unmodifiableMap(orders);
		this.unrecognized = Collections.unmodifiableList(new ArrayList<>(unrecognized));
	}

	public static BlockArrangements parse(String serialized)
	{
		if (serialized == null || serialized.trim().isEmpty())
		{
			return EMPTY;
		}

		String[] entries = serialized.split("\\" + ENTRY_SEPARATOR, -1);
		if (!VERSION.equals(entries[0]))
		{
			// A whole value in a format this build predates: hold it intact.
			return new BlockArrangements(Collections.emptyMap(),
				Collections.singletonList(serialized));
		}

		Map<String, List<String>> orders = new LinkedHashMap<>();
		List<String> unrecognized = new ArrayList<>();
		for (int i = 1; i < entries.length; i++)
		{
			String entry = entries[i];
			int split = entry.indexOf(TAG_SEPARATOR);
			if (split <= 0 || split == entry.length() - 1)
			{
				if (!entry.isEmpty())
				{
					unrecognized.add(entry);
				}
				continue;
			}
			String tagKey = entry.substring(0, split);
			List<String> keys = new ArrayList<>();
			for (String key : entry.substring(split + 1).split("\\" + KEY_SEPARATOR))
			{
				if (!key.isEmpty())
				{
					keys.add(key);
				}
			}
			if (!keys.isEmpty())
			{
				orders.put(tagKey, keys);
			}
		}
		return new BlockArrangements(orders, unrecognized);
	}

	public String serialize()
	{
		if (ordersByTag.isEmpty() && unrecognized.isEmpty())
		{
			return "";
		}
		if (ordersByTag.isEmpty() && unrecognized.size() == 1
			&& !unrecognized.get(0).startsWith(VERSION))
		{
			// The intact newer-format value goes back exactly as it came.
			return unrecognized.get(0);
		}

		StringBuilder builder = new StringBuilder(VERSION);
		for (Map.Entry<String, List<String>> entry : ordersByTag.entrySet())
		{
			builder.append(ENTRY_SEPARATOR).append(entry.getKey()).append(TAG_SEPARATOR)
				.append(String.join(KEY_SEPARATOR, entry.getValue()));
		}
		for (String entry : unrecognized)
		{
			builder.append(ENTRY_SEPARATOR).append(entry);
		}
		return builder.toString();
	}

	/** Saved block orders keyed by tag; tags not present follow curated order. */
	public Map<String, List<String>> orders()
	{
		return ordersByTag;
	}

	public BlockArrangements withTag(String tagKey, List<String> blockKeys)
	{
		Map<String, List<String>> updated = new LinkedHashMap<>(ordersByTag);
		if (blockKeys == null || blockKeys.isEmpty())
		{
			updated.remove(tagKey);
		}
		else
		{
			updated.put(tagKey, new ArrayList<>(blockKeys));
		}
		return new BlockArrangements(updated, unrecognized);
	}

	public BlockArrangements withoutTag(String tagKey)
	{
		return withTag(tagKey, Collections.emptyList());
	}

	public boolean isEmpty()
	{
		return ordersByTag.isEmpty() && unrecognized.isEmpty();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof BlockArrangements
			&& serialize().equals(((BlockArrangements) other).serialize());
	}

	@Override
	public int hashCode()
	{
		return serialize().hashCode();
	}

	@Override
	public String toString()
	{
		return Arrays.asList(serialize()).toString();
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
 * Untouched values round-trip verbatim. Edits use BAblocks2 records separated
 * by !: T records hold a Base64-URL tag, >, and ^-separated Base64-URL keys;
 * U records hold one opaque Base64-URL value. U payloads are never parsed as
 * orders. Legacy v1 values remain readable, and unknown records survive edits.</p>
 */
public final class BlockArrangements
{
	static final String VERSION = "BAblocks2";
	private static final String ENTRY_SEPARATOR = "!";
	private static final String TAG_SEPARATOR = ">";
	private static final String KEY_SEPARATOR = "^";

	public static final BlockArrangements EMPTY =
		new BlockArrangements(Collections.emptyMap(), Collections.emptyList());

	private final Map<String, List<String>> ordersByTag;
	private final List<String> unrecognized;
	private final String original;

	private BlockArrangements(Map<String, List<String>> ordersByTag, List<String> unrecognized)
	{
		this(ordersByTag, unrecognized, null);
	}

	private BlockArrangements(Map<String, List<String>> ordersByTag, List<String> unrecognized,
		String original)
	{
		this.original = original;
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
		if (serialized == null)
		{
			return EMPTY;
		}
		if (serialized.trim().isEmpty())
		{
			return new BlockArrangements(Collections.emptyMap(), Collections.emptyList(), serialized);
		}

		String[] entries = serialized.split("\\" + ENTRY_SEPARATOR, -1);
		boolean encoded = VERSION.equals(entries[0]);
		if (!encoded && !"v1".equals(entries[0]))
		{
			// A whole value in a format this build predates: hold it intact.
			return new BlockArrangements(Collections.emptyMap(),
				Collections.singletonList(serialized), serialized);
		}

		Map<String, List<String>> orders = new LinkedHashMap<>();
		List<String> unrecognized = new ArrayList<>();
		for (int i = 1; i < entries.length; i++)
		{
			String raw = entries[i];
			try
			{
				if (encoded && raw.startsWith("U"))
				{
					unrecognized.add(decode(raw.substring(1)));
					continue;
				}
				if (encoded && !raw.startsWith("T")) throw new IllegalArgumentException();
				String entry = encoded ? raw.substring(1) : raw;
				int split = entry.indexOf(TAG_SEPARATOR);
				if (split <= 0 || split == entry.length() - 1) throw new IllegalArgumentException();
				String tag = entry.substring(0, split);
				List<String> keys = new ArrayList<>();
				for (String key : entry.substring(split + 1).split("\\" + KEY_SEPARATOR, -1))
				{
					if (key.isEmpty()) throw new IllegalArgumentException();
					keys.add(encoded ? decode(key) : key);
				}
				orders.put(encoded ? decode(tag) : tag, keys);
			}
			catch (IllegalArgumentException malformed)
			{
				unrecognized.add(raw);
			}
		}
		return new BlockArrangements(orders, unrecognized, serialized);
	}

	public String serialize()
	{
		if (original != null) return original;
		if (ordersByTag.isEmpty() && unrecognized.isEmpty())
		{
			return "";
		}
		StringBuilder builder = new StringBuilder(VERSION);
		for (Map.Entry<String, List<String>> entry : ordersByTag.entrySet())
		{
			List<String> keys = new ArrayList<>();
			for (String key : entry.getValue()) keys.add(encode(key));
			builder.append(ENTRY_SEPARATOR).append("T").append(encode(entry.getKey()))
				.append(TAG_SEPARATOR).append(String.join(KEY_SEPARATOR, keys));
		}
		for (String entry : unrecognized)
		{
			builder.append(ENTRY_SEPARATOR).append("U").append(encode(entry));
		}
		return builder.toString();
	}

	private static String encode(String value)
	{
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(String value)
	{
		String decoded = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
		// Reject invalid UTF-8 or noncanonical encoding instead of corrupting an opaque payload.
		if (!encode(decoded).equals(value)) throw new IllegalArgumentException();
		return decoded;
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
		return updated.equals(ordersByTag) ? this : new BlockArrangements(updated, unrecognized);
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

package com.pkoka5.ironmanbankarchitect.override;

import com.pkoka5.ironmanbankarchitect.organize.CategoryOverrideSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Player-recorded item-to-category corrections, stored locally as one compact
 * string so they survive a client restart.
 *
 * <p>The format is {@code itemId=categoryKey} pairs separated by commas. Parsing
 * is deliberately forgiving: a malformed or unreadable entry is skipped instead
 * of discarding the whole set, because the value round-trips through plugin
 * configuration that the player can edit or corrupt by hand.</p>
 *
 * <p>Category keys are not validated here. Whether a key belongs to the active
 * preset is decided when the blueprint is built, so an override kept for a
 * preset the player is not currently using stays recorded but inert.</p>
 */
public final class UserCategoryOverrides implements CategoryOverrideSource
{
	private static final char PAIR_SEPARATOR = ',';
	private static final char KEY_SEPARATOR = '=';

	private final Map<Integer, String> categoryKeyByItemId = new LinkedHashMap<>();

	public static UserCategoryOverrides parse(String serialized)
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		if (serialized == null)
		{
			return overrides;
		}

		for (String pair : serialized.split(String.valueOf(PAIR_SEPARATOR)))
		{
			int separator = pair.indexOf(KEY_SEPARATOR);
			if (separator <= 0 || separator == pair.length() - 1)
			{
				continue;
			}
			String categoryKey = pair.substring(separator + 1).trim();
			if (categoryKey.isEmpty())
			{
				continue;
			}
			try
			{
				int itemId = Integer.parseInt(pair.substring(0, separator).trim());
				if (itemId > 0)
				{
					overrides.categoryKeyByItemId.put(itemId, categoryKey);
				}
			}
			catch (NumberFormatException ignored)
			{
				// Skip the damaged entry and keep the rest of the set.
			}
		}
		return overrides;
	}

	@Override
	public synchronized Optional<String> categoryKeyFor(int itemId)
	{
		return Optional.ofNullable(categoryKeyByItemId.get(itemId));
	}

	/**
	 * Records a correction. A blank key clears the override for that item, so
	 * the automatic classification applies again.
	 */
	public synchronized void put(int itemId, String categoryKey)
	{
		if (itemId <= 0)
		{
			throw new IllegalArgumentException("itemId must be positive");
		}
		if (categoryKey == null || categoryKey.trim().isEmpty())
		{
			categoryKeyByItemId.remove(itemId);
			return;
		}
		String trimmed = categoryKey.trim();
		if (trimmed.indexOf(PAIR_SEPARATOR) >= 0 || trimmed.indexOf(KEY_SEPARATOR) >= 0)
		{
			throw new IllegalArgumentException("categoryKey must not contain a separator");
		}
		categoryKeyByItemId.put(itemId, trimmed);
	}

	public synchronized void remove(int itemId)
	{
		categoryKeyByItemId.remove(itemId);
	}

	public synchronized void clear()
	{
		categoryKeyByItemId.clear();
	}

	public synchronized int size()
	{
		return categoryKeyByItemId.size();
	}

	public synchronized boolean isEmpty()
	{
		return categoryKeyByItemId.isEmpty();
	}

	public synchronized Map<Integer, String> asMap()
	{
		return Collections.unmodifiableMap(new LinkedHashMap<>(categoryKeyByItemId));
	}

	/** Round-trips through {@link #parse(String)}. */
	public synchronized String serialize()
	{
		StringBuilder serialized = new StringBuilder();
		for (Map.Entry<Integer, String> entry : categoryKeyByItemId.entrySet())
		{
			if (serialized.length() > 0)
			{
				serialized.append(PAIR_SEPARATOR);
			}
			serialized.append(entry.getKey()).append(KEY_SEPARATOR).append(entry.getValue());
		}
		return serialized.toString();
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof UserCategoryOverrides
			&& asMap().equals(((UserCategoryOverrides) other).asMap());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(asMap());
	}
}

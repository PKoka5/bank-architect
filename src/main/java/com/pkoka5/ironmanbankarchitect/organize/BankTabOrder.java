package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The player's chosen order for the blueprint destinations.
 *
 * <p>The first destination is the bank's main section rather than a tab, so it
 * stays pinned at the front; the nine tabs after it can be arranged freely.
 * Reordering only changes where a destination is placed. Which items land in it
 * is decided by category key, so the classification, the corrections, and the
 * layout inside a tab are all unaffected.</p>
 *
 * <p>Parsing is deliberately forgiving: an unrecognised key is dropped and a
 * missing one is appended in preset order. A stored order written by a version
 * with different destinations therefore still yields a complete, valid one
 * instead of an error the player cannot act on.</p>
 */
public final class BankTabOrder
{
	/** The main section is not a tab, so it cannot be moved out of first place. */
	public static final int FIRST_MOVABLE_INDEX = 1;

	private static final String SEPARATOR = ",";

	private BankTabOrder()
	{
	}

	/** The preset with its destinations in the stored order. */
	public static BankPreset apply(BankPreset preset, String serialized)
	{
		Objects.requireNonNull(preset, "preset");

		List<String> order = orderedKeys(preset, serialized);
		if (order.equals(keysOf(preset.getCategories())))
		{
			return preset;
		}

		List<BankCategory> categories = new ArrayList<>(order.size());
		for (String key : order)
		{
			categories.add(preset.getCategory(key));
		}

		return new BankPreset(preset.getType(), preset.getKey(), preset.getName(), categories);
	}

	/** Every destination key of the preset, in the stored order. */
	public static List<String> orderedKeys(BankPreset preset, String serialized)
	{
		Objects.requireNonNull(preset, "preset");

		List<String> defaults = keysOf(preset.getCategories());
		List<String> ordered = new ArrayList<>(defaults.size());
		ordered.add(defaults.get(0));

		for (String key : split(serialized))
		{
			if (defaults.contains(key) && !ordered.contains(key))
			{
				ordered.add(key);
			}
		}
		for (String key : defaults)
		{
			if (!ordered.contains(key))
			{
				ordered.add(key);
			}
		}

		return Collections.unmodifiableList(ordered);
	}

	/** True when the stored order is the preset's own order. */
	public static boolean isDefault(BankPreset preset, String serialized)
	{
		return orderedKeys(preset, serialized).equals(keysOf(preset.getCategories()));
	}

	/**
	 * The order with the destination at {@code index} shifted by {@code delta},
	 * or the same order when that would move it off the end or into the pinned
	 * main slot.
	 */
	public static List<String> moved(List<String> keys, int index, int delta)
	{
		Objects.requireNonNull(keys, "keys");

		int target = index + delta;
		if (index < FIRST_MOVABLE_INDEX || index >= keys.size()
			|| target < FIRST_MOVABLE_INDEX || target >= keys.size())
		{
			return keys;
		}

		List<String> moved = new ArrayList<>(keys);
		moved.add(target, moved.remove(index));
		return Collections.unmodifiableList(moved);
	}

	public static String serialize(List<String> keys)
	{
		Objects.requireNonNull(keys, "keys");
		return String.join(SEPARATOR, keys);
	}

	private static List<String> keysOf(List<BankCategory> categories)
	{
		List<String> keys = new ArrayList<>(categories.size());
		for (BankCategory category : categories)
		{
			keys.add(category.getKey());
		}
		return keys;
	}

	private static List<String> split(String serialized)
	{
		if (serialized == null || serialized.trim().isEmpty())
		{
			return Collections.emptyList();
		}

		List<String> keys = new ArrayList<>();
		for (String part : serialized.split(SEPARATOR))
		{
			String key = part.trim();
			if (!key.isEmpty())
			{
				keys.add(key);
			}
		}
		return keys;
	}
}

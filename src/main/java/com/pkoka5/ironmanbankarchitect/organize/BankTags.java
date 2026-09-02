package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Splits each blueprint category into the parts a player can place separately.
 *
 * <p>The splits follow the subcategories the classification already records, so
 * a tag is a name for something the catalogue could always distinguish rather
 * than a new judgement about items. Every category ends with a catch-all tag, so
 * an item whose subcategory matches no split still lands somewhere and no part
 * of the bank can go missing when the catalogue gains a subcategory.</p>
 *
 * <p>Where a category is deliberately left whole it simply has one tag. Combat
 * gear is one idea to a player even though it spans every equipment slot, and
 * splitting it by slot would only produce tabs nobody asked for.</p>
 */
public final class BankTags
{
	private static final List<BankTag> TAGS;
	private static final Map<String, BankTag> BY_KEY;
	private static final Map<String, List<Split>> SPLITS_BY_CATEGORY;

	static
	{
		Map<String, List<Split>> splits = new LinkedHashMap<>();
		// Order matters twice over: it is the order the tags are offered in, and
		// the default order they fill their category's destination in.
		splits.put("currency-utilities", Arrays.asList(
			split("frequently-used", "Frequently Used"),
			split("runes", "Runes", "rune", "rune-container"),
			split("teleports", "Teleports",
				"teleport", "teleport-tablet", "teleport-scroll", "teleport-charge",
				"teleport-container", "transport-access"),
			split("currency", "Currency", "currency")));
		splits.put("combat-gear", Arrays.asList(
			split("gear", "Combat Gear"),
			split("ammunition", "Ammunition", "ammo", "thrown-weapon")));
		splits.put("potions-food", Arrays.asList(
			split("potions", "Potions", "potion", "potion-dose-4"),
			split("food", "Food & Drink", "food", "drink")));
		splits.put("herblore", Arrays.asList(
			split("herb-seeds", "Herb Seeds", "herb-seed"),
			split("grimy-herbs", "Grimy Herbs", "grimy-herb"),
			split("clean-herbs", "Clean Herbs", "clean-herb"),
			split("secondaries", "Secondaries", "secondary"),
			split("unfinished-potions", "Unfinished Potions", "unfinished-potion"),
			split("potion-doses", "Part Doses",
				"potion-dose-1", "potion-dose-2", "potion-dose-3", "dose-1", "dose-2", "dose-3"),
			split("herblore-other", "Herblore Other")));
		splits.put("seeds-farming", Arrays.asList(
			split("seeds", "Seeds", "farming"),
			split("produce", "Produce", "produce", "produce-container")));
		splits.put("skilling-tools", Arrays.asList(
			split("tools", "Skilling Tools"),
			split("skilling-outfits", "Skilling Outfits", "skilling-outfit"),
			split("containers", "Containers", "resource-container", "utility-container")));
		splits.put("resources", Arrays.asList(
			split("raw-resources", "Raw & Processed Resources"),
			split("gems", "Gems & Jewellery", "gem", "uncut-gem", "crafting-jewellery"),
			split("ammo-components", "Ammo Components", "ammo-component")));
		splits.put("slayer-boss-loot", Collections.singletonList(
			split("boss-loot", "Slayer & Boss Loot")));
		splits.put("clues-cosmetics", Arrays.asList(
			split("clues", "Clue Scrolls & Caskets"),
			split("cosmetics", "Cosmetics", "cosmetic"),
			split("collection-log", "Collection Log", "collection-trophy", "collection-pet")));
		splits.put("storage-cleanup", Arrays.asList(
			split("cleanup", "Storage & Cleanup"),
			split("quest-items", "Quest Items", "quest-item")));

		List<BankTag> tags = new ArrayList<>();
		Map<String, BankTag> byKey = new LinkedHashMap<>();
		for (Map.Entry<String, List<Split>> entry : splits.entrySet())
		{
			for (Split split : entry.getValue())
			{
				BankTag tag = new BankTag(split.key, split.name, entry.getKey());
				tags.add(tag);
				byKey.put(tag.getKey(), tag);
			}
		}

		TAGS = Collections.unmodifiableList(tags);
		BY_KEY = Collections.unmodifiableMap(byKey);
		SPLITS_BY_CATEGORY = Collections.unmodifiableMap(splits);
	}

	private BankTags()
	{
	}

	/** Every tag, grouped by the category it came from, in offer order. */
	public static List<BankTag> all()
	{
		return TAGS;
	}

	public static BankTag byKey(String tagKey)
	{
		BankTag tag = BY_KEY.get(tagKey);
		if (tag == null)
		{
			throw new IllegalArgumentException("Unknown tag key: " + tagKey);
		}

		return tag;
	}

	public static boolean isKnown(String tagKey)
	{
		return BY_KEY.containsKey(tagKey);
	}

	/** The tags a category splits into, in the order they are offered. */
	public static List<BankTag> forCategory(String categoryKey)
	{
		List<Split> splits = SPLITS_BY_CATEGORY.get(categoryKey);
		if (splits == null)
		{
			return Collections.emptyList();
		}

		List<BankTag> tags = new ArrayList<>(splits.size());
		for (Split split : splits)
		{
			tags.add(BY_KEY.get(split.key));
		}

		return Collections.unmodifiableList(tags);
	}

	/**
	 * Which part of its category an item belongs to. An unmatched subcategory
	 * falls to the category's catch-all rather than being dropped, so a
	 * catalogue that grows a subcategory never silently loses items.
	 */
	public static BankTag tagFor(String categoryKey, String subcategory)
	{
		Objects.requireNonNull(categoryKey, "categoryKey");

		List<Split> splits = SPLITS_BY_CATEGORY.get(categoryKey);
		if (splits == null || splits.isEmpty())
		{
			throw new IllegalArgumentException("Category has no tags: " + categoryKey);
		}

		String normalized = subcategory == null ? "" : subcategory.trim().toLowerCase();
		Split fallback = null;
		for (Split split : splits)
		{
			if (split.subcategories.isEmpty())
			{
				fallback = split;
			}
			else if (split.subcategories.contains(normalized))
			{
				return BY_KEY.get(split.key);
			}
		}

		return BY_KEY.get(fallback == null ? splits.get(0).key : fallback.key);
	}

	private static Split split(String key, String name, String... subcategories)
	{
		return new Split(key, name, subcategories);
	}

	/** A tag plus the subcategories that route to it; empty means catch-all. */
	private static final class Split
	{
		private final String key;
		private final String name;
		private final List<String> subcategories;

		private Split(String key, String name, String[] subcategories)
		{
			this.key = key;
			this.name = name;
			this.subcategories = Collections.unmodifiableList(Arrays.asList(subcategories));
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;
import java.util.List;



/** Splits categories by catalog subcategory into independently placeable tags. Each category ends with a catch-all tag so unmatched items remain in the blueprint. */
public final class BankTags
{
	private static final List<BankTag> TAGS;
	private static final Map<String, BankTag> BY_KEY;
	private static final Map<String, List<Split>> SPLITS_BY_CATEGORY;
	private static final Set<String> OPTIONAL_TAGS = new HashSet<>();

	static
	{
		Map<String, List<Split>> splits = new LinkedHashMap<>();
		for (String[] row : BankDefinitionRows.read("bank-tags.tsv", 4))
		{
			if (row[3].equals("@optional")) OPTIONAL_TAGS.add(row[1]);
			splits.computeIfAbsent(row[0], key -> new ArrayList<>()).add(
				new Split(row[1], row[2], row[3].isEmpty() ? new String[0] : row[3].split(",")));
		}

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

	/** Whether the effective main-tab tag overrides the catalogue-derived role. */
	public static boolean hasDifferentMainTag(BankPreviewItem item)
	{
		return item.getLayoutTagKey() != null
			&& !item.getLayoutTagKey().equals(tagFor("currency-utilities", item.getSubcategory()).getKey());
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

	public static boolean isOptional(String tagKey) { return OPTIONAL_TAGS.contains(tagKey); }

	/** Parent Combat records remain valid as its optional styles are enabled or merged. */
	static boolean sameOriginalTag(String original, String current)
	{
		return Objects.equals(original, current) || "gear".equals(original) && isOptional(current)
			|| "gear".equals(current) && isOptional(original);
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
			if (!isOptional(split.key)) tags.add(BY_KEY.get(split.key));
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

		String normalized = subcategory == null ? "" : subcategory.trim().toLowerCase(java.util.Locale.ROOT);
		Split fallback = null;
		for (Split split : splits)
		{
			if (isOptional(split.key)) continue;
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

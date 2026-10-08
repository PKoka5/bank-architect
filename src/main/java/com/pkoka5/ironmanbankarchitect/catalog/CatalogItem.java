package com.pkoka5.ironmanbankarchitect.catalog;

import static com.pkoka5.ironmanbankarchitect.util.TextValidation.requireText;

import java.util.*;





public final class CatalogItem
{
	private final int itemId;
	private final String displayName;
	private final ItemCategory category;
	private final String subcategory;
	private final Set<String> tags;
	private final String workflowKey;

	public CatalogItem(int itemId, String displayName, ItemCategory category, String subcategory, Set<String> tags, String workflowKey)
	{
		this(itemId, displayName, category, subcategory, tags, workflowKey, true);
	}

	/**
	 * Unknown items may carry any int itemId (including 0, negative, or Integer.MIN_VALUE), since
	 * a bank scan must never crash on an item ID that is not in the catalog. Known catalog items
	 * still require a positive itemId via the public constructor above.
	 */
	private CatalogItem(int itemId, String displayName, ItemCategory category, String subcategory, Set<String> tags,
		String workflowKey, boolean requirePositiveItemId)
	{
		if (requirePositiveItemId && itemId <= 0)
		{
			throw new IllegalArgumentException("itemId must be positive");
		}
		if (!requirePositiveItemId && category != ItemCategory.UNKNOWN)
		{
			throw new IllegalStateException("non-positive itemId is only allowed for category UNKNOWN");
		}

		this.itemId = itemId;
		this.displayName = requireText(displayName, "displayName");
		this.category = Objects.requireNonNull(category, "category");
		this.subcategory = requireText(subcategory, "subcategory");
		this.tags = tags == null || tags.isEmpty()
			? Collections.emptySet()
			: Collections.unmodifiableSet(new LinkedHashSet<>(tags));
		this.workflowKey = workflowKey;
	}

	public static CatalogItem unknown(int itemId)
	{
		return new CatalogItem(itemId, "Unknown item #" + itemId, ItemCategory.UNKNOWN, "unknown", Collections.emptySet(), null, false);
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public ItemCategory getCategory()
	{
		return category;
	}

	public String getSubcategory()
	{
		return subcategory;
	}

	public Set<String> getTags()
	{
		return tags;
	}

	public boolean hasTag(String tag)
	{
		return tags.contains(tag);
	}

	public Optional<String> getWorkflowKey()
	{
		return Optional.ofNullable(workflowKey);
	}

}

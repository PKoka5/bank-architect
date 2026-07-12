package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class BankPreviewItem
{
	private static final BankPreviewItem BLANK = new BankPreviewItem(-1, "Empty slot", 1);

	private final int itemId;
	private final String displayName;
	private final int quantity;
	private final ItemCategory itemCategory;
	private final String subcategory;
	private final Set<String> tags;
	private final boolean placeholder;

	public BankPreviewItem(int itemId, String displayName, int quantity)
	{
		this(itemId, displayName, quantity, ItemCategory.UNKNOWN, "unknown", Collections.emptySet(), false);
	}

	public BankPreviewItem(CatalogItem catalogItem, int quantity)
	{
		this(catalogItem, quantity, false);
	}

	public BankPreviewItem(CatalogItem catalogItem, int quantity, boolean placeholder)
	{
		this(catalogItem.getItemId(), catalogItem.getDisplayName(), quantity, catalogItem.getCategory(),
			catalogItem.getSubcategory(), catalogItem.getTags(), placeholder);
	}

	private BankPreviewItem(int itemId, String displayName, int quantity, ItemCategory itemCategory,
		String subcategory, Set<String> tags, boolean placeholder)
	{
		if (quantity < 0 || (quantity == 0 && !placeholder))
		{
			throw new IllegalArgumentException("quantity must be positive unless the item is a placeholder");
		}

		this.itemId = itemId;
		this.displayName = requireText(displayName, "displayName");
		this.quantity = quantity;
		this.itemCategory = Objects.requireNonNull(itemCategory, "itemCategory");
		this.subcategory = requireText(subcategory, "subcategory");
		this.tags = tags == null || tags.isEmpty()
			? Collections.emptySet()
			: Collections.unmodifiableSet(new LinkedHashSet<>(tags));
		this.placeholder = placeholder;
	}

	public static BankPreviewItem blank()
	{
		return BLANK;
	}

	public boolean isBlank()
	{
		return itemId < 0;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public ItemCategory getItemCategory()
	{
		return itemCategory;
	}

	public String getSubcategory()
	{
		return subcategory;
	}

	public boolean hasTag(String tag)
	{
		return tags.contains(tag);
	}

	public boolean isPlaceholder()
	{
		return placeholder;
	}

	public String toCompactLabel()
	{
		if (placeholder)
		{
			return displayName + " (placeholder)";
		}
		return quantity > 1 ? displayName + " x" + quantity : displayName;
	}

	private static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}

		return value;
	}
}

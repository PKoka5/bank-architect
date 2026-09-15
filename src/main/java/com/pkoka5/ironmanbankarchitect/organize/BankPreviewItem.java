package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
	private final List<Integer> physicalSlotQuantities;
	private final String layoutTagKey;
	private final int blueprintOccurrence;

	public BankPreviewItem(int itemId, String displayName, int quantity)
	{
		this(itemId, displayName, quantity, ItemCategory.UNKNOWN, "unknown", Collections.emptySet(), false,
			Collections.singletonList(quantity));
	}

	public BankPreviewItem(CatalogItem catalogItem, int quantity)
	{
		this(catalogItem, quantity, false);
	}

	public BankPreviewItem(CatalogItem catalogItem, int quantity, boolean placeholder)
	{
		this(catalogItem.getItemId(), catalogItem.getDisplayName(), quantity, catalogItem.getCategory(),
			catalogItem.getSubcategory(), catalogItem.getTags(), placeholder,
			Collections.singletonList(quantity));
	}

	public BankPreviewItem(CatalogItem catalogItem, int quantity, boolean placeholder,
		List<Integer> physicalSlotQuantities)
	{
		this(catalogItem.getItemId(), catalogItem.getDisplayName(), quantity, catalogItem.getCategory(),
			catalogItem.getSubcategory(), catalogItem.getTags(), placeholder, physicalSlotQuantities);
	}

	private BankPreviewItem(int itemId, String displayName, int quantity, ItemCategory itemCategory,
		String subcategory, Set<String> tags, boolean placeholder, List<Integer> physicalSlotQuantities)
	{
		this(itemId, displayName, quantity, itemCategory, subcategory, tags, placeholder,
			physicalSlotQuantities, null);
	}

	private BankPreviewItem(int itemId, String displayName, int quantity, ItemCategory itemCategory,
		String subcategory, Set<String> tags, boolean placeholder, List<Integer> physicalSlotQuantities,
		String layoutTagKey)
	{
		this(itemId, displayName, quantity, itemCategory, subcategory, tags, placeholder,
			physicalSlotQuantities, layoutTagKey, -1);
	}

	private BankPreviewItem(int itemId, String displayName, int quantity, ItemCategory itemCategory,
		String subcategory, Set<String> tags, boolean placeholder, List<Integer> physicalSlotQuantities,
		String layoutTagKey, int blueprintOccurrence)
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
		this.layoutTagKey = layoutTagKey;
		this.blueprintOccurrence = blueprintOccurrence;
		this.physicalSlotQuantities = Collections.unmodifiableList(
			new ArrayList<>(Objects.requireNonNull(physicalSlotQuantities,
				"physicalSlotQuantities")));
		if (this.physicalSlotQuantities.isEmpty())
		{
			throw new IllegalArgumentException("physicalSlotQuantities must not be empty");
		}
		int physicalQuantity = 0;
		for (int slotQuantity : this.physicalSlotQuantities)
		{
			if (slotQuantity < 0)
			{
				throw new IllegalArgumentException("physical slot quantities must not be negative");
			}
			physicalQuantity += slotQuantity;
		}
		if (physicalQuantity != quantity)
		{
			throw new IllegalArgumentException("physical slot quantities must sum to quantity");
		}
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

	/** Effective planner tag, separate from the item's original catalogue metadata. */
	public String getLayoutTagKey()
	{
		return layoutTagKey;
	}

	public BankPreviewItem withLayoutTag(String tagKey)
	{
		if (tagKey != null) BankTags.byKey(tagKey);
		return new BankPreviewItem(itemId, displayName, quantity, itemCategory, subcategory,
			tags, placeholder, physicalSlotQuantities, tagKey, blueprintOccurrence);
	}

	public int getBlueprintOccurrence() { return blueprintOccurrence; }

	public BankPreviewItem withBlueprintOccurrence(int occurrence)
	{
		return new BankPreviewItem(itemId, displayName, quantity, itemCategory, subcategory,
			tags, placeholder, physicalSlotQuantities, layoutTagKey, occurrence);
	}

	int physicalBankSlotCount()
	{
		return physicalSlotQuantities.size();
	}

	/** Expands one logically classified item into its physical bank entries. */
	List<BankPreviewItem> physicalBankSlots()
	{
		if (physicalSlotQuantities.size() == 1)
		{
			return Collections.singletonList(this);
		}
		List<BankPreviewItem> slots = new ArrayList<>(physicalSlotQuantities.size());
		for (int slotQuantity : physicalSlotQuantities)
		{
			slots.add(new BankPreviewItem(itemId, displayName, slotQuantity, itemCategory,
				subcategory, tags, slotQuantity == 0, Collections.singletonList(slotQuantity), layoutTagKey));
		}
		return slots;
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

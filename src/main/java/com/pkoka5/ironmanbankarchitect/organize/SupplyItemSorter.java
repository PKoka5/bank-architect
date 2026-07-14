package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class SupplyItemSorter
{
	private SupplyItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		return sort(items, ResourceItemSortMetadataCatalog.INSTANCE);
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items, ItemSortMetadataCatalog metadataCatalog)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt((BankPreviewItem item) -> roleRank(item, metadataCatalog))
			.thenComparingInt(item -> doseMetadataRank(item, metadataCatalog))
			.thenComparingInt(item -> foodMetadataRank(item, metadataCatalog))
			.thenComparingInt(item -> -immediateHealMax(item, metadataCatalog))
			.thenComparingInt(item -> -immediateHealMin(item, metadataCatalog))
			.thenComparingInt(item -> foodRoleRank(item, metadataCatalog))
			.thenComparing(item -> familyName(item, metadataCatalog))
			.thenComparingInt(item -> areaRestrictionRank(item, metadataCatalog))
			.thenComparingInt(item -> variantRank(item, metadataCatalog))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (doseMetadata(item, metadataCatalog).isPresent()) return 0;
		if (subcategory.equals("potion-dose-4") || subcategory.equals("dose-4")) return 0;
		if (subcategory.contains("pvm-utility")) return 10;
		if (foodMetadata(item, metadataCatalog).isPresent()) return 30;
		if (subcategory.contains("food") || isFoodName(name)) return 30;
		if (subcategory.contains("drink")) return 40;
		if (subcategory.contains("potion") || name.contains(" mix")) return 20;
		return 50;
	}

	private static int doseMetadataRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		if (roleRank(item, metadataCatalog) != 0)
		{
			return 0;
		}
		return doseMetadata(item, metadataCatalog).isPresent() ? 0 : 1;
	}

	private static int foodMetadataRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		if (roleRank(item, metadataCatalog) != 30)
		{
			return 0;
		}
		return foodMetadata(item, metadataCatalog).isPresent() ? 0 : 1;
	}

	private static int foodRoleRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		Optional<ItemSortMetadata> metadata = foodMetadata(item, metadataCatalog);
		if (!metadata.isPresent()) return 100;
		switch (metadata.get().getFoodRole())
		{
			case STANDARD:
				return 0;
			case COMBO:
				return 10;
			case DELAYED:
				return 20;
			case MULTI_BITE:
				return 30;
			case NONE:
			default:
				return 100;
		}
	}

	private static int immediateHealMax(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		return foodMetadata(item, metadataCatalog)
			.map(ItemSortMetadata::getImmediateHealMax)
			.orElse(0);
	}

	private static int immediateHealMin(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		return foodMetadata(item, metadataCatalog)
			.map(ItemSortMetadata::getImmediateHealMin)
			.orElse(0);
	}

	private static String familyName(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		Optional<ItemSortMetadata> metadata = sortMetadata(item, metadataCatalog);
		if (metadata.isPresent()) return metadata.get().getFamilyKey();

		String name = normalized(item.getDisplayName());
		if (name.startsWith("half a ")) name = name.substring("half a ".length());
		if (name.startsWith("1/2 ")) name = name.substring("1/2 ".length());
		return name.replaceFirst("\\s*\\([1-4]\\)$", "");
	}

	private static int areaRestrictionRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		return sortMetadata(item, metadataCatalog)
			.map(metadata -> metadata.getAreaRestriction() == ItemSortMetadata.AreaRestriction.NONE ? 0 : 1)
			.orElse(0);
	}

	private static int variantRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		Optional<ItemSortMetadata> metadata = sortMetadata(item, metadataCatalog);
		if (metadata.isPresent() && metadata.get().getVariantKind() != ItemSortMetadata.VariantKind.NONE)
		{
			return -metadata.get().getVariantValue();
		}

		String name = normalized(item.getDisplayName());
		if (name.startsWith("half a ") || name.startsWith("1/2 ")) return 1;
		int open = name.lastIndexOf('(');
		if (open >= 0 && name.endsWith(")"))
		{
			try
			{
				return 10 - Integer.parseInt(name.substring(open + 1, name.length() - 1));
			}
			catch (NumberFormatException ignored)
			{
				// Non-dose suffix; retain stable name ordering.
			}
		}
		return 0;
	}

	private static Optional<ItemSortMetadata> foodMetadata(BankPreviewItem item,
		ItemSortMetadataCatalog metadataCatalog)
	{
		return sortMetadata(item, metadataCatalog).filter(ItemSortMetadata::isFood);
	}

	private static Optional<ItemSortMetadata> doseMetadata(BankPreviewItem item,
		ItemSortMetadataCatalog metadataCatalog)
	{
		return sortMetadata(item, metadataCatalog)
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.DOSE)
			.filter(metadata -> !metadata.isFood());
	}

	private static Optional<ItemSortMetadata> sortMetadata(BankPreviewItem item,
		ItemSortMetadataCatalog metadataCatalog)
	{
		return metadataCatalog.findById(item.getItemId());
	}

	private static boolean isFoodName(String name)
	{
		return containsAny(name, "pie", "cake", "kebab", "stew", "pizza", "potato",
			"shark", "monkfish", "karambwan", "manta", "anglerfish", "lobster",
			"swordfish", "tuna", "salmon", "trout");
	}

	private static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle)) return true;
		}
		return false;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsAny;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.normalized;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.numericSuffix;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.List;


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
		return sort(items, metadataCatalog, PotionDoseOrder.GRAB_AREA);
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items,
		ItemSortMetadataCatalog metadataCatalog, PotionDoseOrder doseOrder)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt((BankPreviewItem item) -> doseOrder == PotionDoseOrder.BY_FAMILY
				? byFamilyRoleRank(item, metadataCatalog) : roleRank(item, metadataCatalog))
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

	/**
	 * Under the by-family order every dose counts as its potion, so the
	 * family keys further down the comparator run each potion 4 to 1 in one
	 * place instead of trailing the partials as a to-decant pile.
	 */
	private static int byFamilyRoleRank(BankPreviewItem item, ItemSortMetadataCatalog metadataCatalog)
	{
		String subcategory = normalized(item.getSubcategory());
		if (subcategory.startsWith("potion-dose-") || subcategory.startsWith("dose-"))
		{
			return 0;
		}
		return roleRank(item, metadataCatalog);
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
		return 10 - numericSuffix(name, 10);
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
		return containsAny(name, ClassificationNames.group(67));
	}

}

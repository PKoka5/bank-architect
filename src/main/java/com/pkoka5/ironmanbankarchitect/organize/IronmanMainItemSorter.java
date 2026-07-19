package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Stable fallback order for the mixed Ironman quick-access Main tab. */
final class IronmanMainItemSorter
{
	private IronmanMainItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparingInt(IronmanMainItemSorter::rank)
			.thenComparing(IronmanMainItemSorter::familyOrder)
			.thenComparingLong(item -> -(long) charge(item))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static String familyOrder(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = chargedJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getFamilyKey();
		return normalized(item.getDisplayName());
	}

	private static int charge(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = chargedJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getVariantValue();
		return 0;
	}

	private static Optional<ItemSortMetadata> chargedJewelleryMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE)
			.filter(metadata -> metadata.getFamilyKey().startsWith("jewellery."));
	}

	private static int rank(BankPreviewItem item)
	{
		if (item.getItemId() == 995) return 0;
		if (IronmanQuickToolSelector.isTieredTool(item.getItemId())
			|| IronmanMainTabPolicy.isRunePouch(item)) return 10;
		if (IronmanMainTabPolicy.isGraceful(item)) return 20;
		if (item.getItemCategory() == ItemCategory.RUNE) return 30;
		if (item.getItemCategory() == ItemCategory.TELEPORT) return 40;
		if (AchievementDiarySemanticRuleSet.isDiaryReward(item)) return 60;
		String name = normalized(item.getDisplayName());
		if (name.contains("mark of grace") || name.contains("hallowed mark")) return 70;
		if (item.getItemCategory() == ItemCategory.CURRENCY) return 100;
		return 50;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ENGLISH);
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Stable fallback order for the mixed Ironman quick-access Main tab. */
final class IronmanMainItemSorter
{
	private static final Map<Integer, JewelleryFact> LEGACY_CHARGED_JEWELLERY = chargedJewellery();

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
		JewelleryFact fact = LEGACY_CHARGED_JEWELLERY.get(item.getItemId());
		return fact == null ? normalized(item.getDisplayName()) : fact.family;
	}

	private static int charge(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = chargedJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getVariantValue();
		JewelleryFact fact = LEGACY_CHARGED_JEWELLERY.get(item.getItemId());
		return fact == null ? 0 : fact.charge;
	}

	private static Optional<ItemSortMetadata> chargedJewelleryMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE)
			.filter(metadata -> metadata.getFamilyKey().startsWith("jewellery."));
	}

	private static Map<Integer, JewelleryFact> chargedJewellery()
	{
		Map<Integer, JewelleryFact> facts = new HashMap<>();
		put(facts, "combat bracelet", new int[] {11972, 11974, 11118, 11120, 11122, 11124}, 6);
		put(facts, "digsite pendant", new int[] {11194, 11193, 11192, 11191, 11190}, 5);
		put(facts, "ring of wealth", new int[] {11980, 11982, 11984, 11986, 11988}, 5);
		put(facts, "slayer ring", new int[] {11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873}, 8);
		return Collections.unmodifiableMap(facts);
	}

	private static void put(Map<Integer, JewelleryFact> facts, String family, int[] itemIds,
		int highestCharge)
	{
		for (int index = 0; index < itemIds.length; index++)
		{
			facts.put(itemIds[index], new JewelleryFact(family, highestCharge - index));
		}
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

	private static final class JewelleryFact
	{
		private final String family;
		private final int charge;

		private JewelleryFact(String family, int charge)
		{
			this.family = family;
			this.charge = charge;
		}
	}
}

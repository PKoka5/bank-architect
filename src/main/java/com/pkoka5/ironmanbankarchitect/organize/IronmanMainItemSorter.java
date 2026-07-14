package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Stable fallback order for the mixed Ironman quick-access Main tab. */
final class IronmanMainItemSorter
{
	private static final Map<Integer, JewelleryFact> CHARGED_JEWELLERY = chargedJewellery();

	private IronmanMainItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparingInt(IronmanMainItemSorter::rank)
			.thenComparing(IronmanMainItemSorter::familyOrder)
			.thenComparingInt(item -> -charge(item))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static String familyOrder(BankPreviewItem item)
	{
		JewelleryFact fact = CHARGED_JEWELLERY.get(item.getItemId());
		return fact == null ? normalized(item.getDisplayName()) : fact.family;
	}

	private static int charge(BankPreviewItem item)
	{
		JewelleryFact fact = CHARGED_JEWELLERY.get(item.getItemId());
		return fact == null ? 0 : fact.charge;
	}

	private static Map<Integer, JewelleryFact> chargedJewellery()
	{
		Map<Integer, JewelleryFact> facts = new HashMap<>();
		put(facts, "amulet of glory", new int[] {11978, 11976, 1712, 1710, 1708, 1706}, 6);
		put(facts, "burning amulet", new int[] {21166, 21169, 21171, 21173, 21175}, 5);
		put(facts, "combat bracelet", new int[] {11972, 11974, 11118, 11120, 11122, 11124}, 6);
		put(facts, "digsite pendant", new int[] {11194, 11193, 11192, 11191, 11190}, 5);
		put(facts, "games necklace", new int[] {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867}, 8);
		put(facts, "necklace of passage", new int[] {21146, 21149, 21151, 21153, 21155}, 5);
		put(facts, "ring of dueling", new int[] {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566}, 8);
		put(facts, "ring of wealth", new int[] {11980, 11982, 11984, 11986, 11988}, 5);
		put(facts, "skills necklace", new int[] {11968, 11970, 11105, 11107, 11109, 11111}, 6);
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

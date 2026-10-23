package com.pkoka5.ironmanbankarchitect.organize;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.normalized;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog;
import java.util.*;
import java.util.List;

public final class PresetItemSorter
{
	private static final int[] IRONMAN_ACTIVITY_REWARD_IDS = {
		6183, 6529, 6306, 12012, 25527, 21555
	};
	private PresetItemSorter()
	{
	}

	public static List<BankPreviewItem> sort(BankCategory category, List<BankPreviewItem> items)
	{
		return sort(category, items, GearStatsSource.NONE);
	}

	public static List<BankPreviewItem> sort(BankCategory category, List<BankPreviewItem> items,
		GearStatsSource gearStats)
	{
		switch (category.getSortMode())
		{
			case MAIN:
				return IronmanMainItemSorter.sort(items);
			case GEAR:
				return GearItemSorter.layout(items, gearStats);
			case CURRENCY:
				return CurrencyItemSorter.sort(items);
			case SUPPLIES:
				return SupplyItemSorter.sort(items);
			case HERBLORE:
				return HerbloreItemSorter.layout(items);
			case FARMING:
				return FarmingItemSorter.layout(items, 0);
			case TELEPORTS:
				return TeleportItemSorter.sort(items);
			case CLUES:
				return sortClues(items);
			case TOOLS:
				return ToolItemSorter.sort(items);
			case RESOURCES:
				return ResourceItemSorter.sort(items);
			case BOSS_LOOT:
				return sortBossLoot(items);
			case REVIEW:
				return sortReview(items);
			case GENERIC:
			default:
				return sortGeneric(items);
		}
	}

	private static List<BankPreviewItem> sortGeneric(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparing((BankPreviewItem item) -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static List<BankPreviewItem> sortClues(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(PresetItemSorter::clueRank)
			.thenComparingInt(item -> ItemSetCatalog.cosmeticFamilyRankOf(item.getItemId()))
			.thenComparing(PresetItemSorter::clueFamily)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	/**
	 * Keeps a cosmetic family adjacent where plain alphabetical order would
	 * scatter it. Recoloured cosmetics lead with their colour word, so sorting
	 * them by name alone files each partyhat or dye next to whatever else
	 * shares its colour instead of next to the rest of its family. Families
	 * come from the set catalogue by exact ID, so a lookalike name such as the
	 * plain wig disguise never rides along with the real partyhats.
	 */
	private static String clueFamily(BankPreviewItem item)
	{
		return ItemSetCatalog.cosmeticFamilyOf(item.getItemId())
			.map(value -> normalized(value))
			.orElseGet(() -> normalized(item.getDisplayName()));
	}

	private static List<BankPreviewItem> sortBossLoot(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(PresetItemSorter::bossLootRank)
			.thenComparingInt(item -> isDropAmmunition(item) ? GearItemSorter.ammoTierRank(item) : 0)
			.thenComparing(PresetItemSorter::bossLootFamily)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static List<BankPreviewItem> sortReview(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(ReviewItemSorter::rank)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int clueRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String[] order = {"beginner", "easy", "medium", "hard", "elite", "master"};
		for (int i = 0; i < order.length; i++)
		{
			if (name.contains("(" + order[i] + ")"))
			{
				return i;
			}
		}
		if (isIronmanActivityReward(item.getItemId())) return 10;
		return 20;
	}

	private static boolean isIronmanActivityReward(int itemId)
	{
		for (int candidate : IRONMAN_ACTIVITY_REWARD_IDS)
		{
			if (candidate == itemId) return true;
		}
		return false;
	}

	private static int bossLootRank(BankPreviewItem item)
	{
		if ("alch".equals(item.getLayoutTagKey())) return 200;
		if (isDropAmmunition(item)) return 60 + GearItemSorter.ammoFamilyRank(item);
		if (item.getItemCategory() == ItemCategory.UNIQUE)
		{
			String subcategory = normalized(item.getSubcategory());
			if (subcategory.contains("weapon-upgrade")) return 0;
			if (subcategory.contains("equipment-upgrade")) return 10;
			if (subcategory.contains("charge")) return 20;
			return 30;
		}
		if (item.getItemCategory() == ItemCategory.GEAR)
		{
			return 150;
		}
		return 50;
	}

	static boolean isDropAmmunition(ItemCategory category, String subcategory, String name)
	{
		return category == ItemCategory.GEAR && subcategory.matches("ammo|thrown-weapon|weapon")
			&& normalized(name).matches(".*\\b(?:arrows?|bolts?|darts?|cannonballs?)(?:\\s*\\([^)]*\\))?|bolt rack|(?:bronze|iron|steel|black|mithril|adamant|rune) brutal");
	}

	private static boolean isDropAmmunition(BankPreviewItem item)
	{
		return isDropAmmunition(item.getItemCategory(), normalized(item.getSubcategory()), item.getDisplayName());
	}

	private static String bossLootFamily(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		if (name.contains("crystal weapon seed")) return "crystal-weapon-seed";
		if (name.endsWith(" page")) return "charge-page";
		return name;
	}

}

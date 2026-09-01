package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Comparator;
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
				return sortGeneric(category, items);
		}
	}

	private static List<BankPreviewItem> sortGeneric(BankCategory category, List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt((BankPreviewItem item) -> subgroupRank(category.getKey(), item))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static List<BankPreviewItem> sortClues(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(PresetItemSorter::clueRank)
			.thenComparing(PresetItemSorter::clueFamily)
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	/**
	 * Keeps a cosmetic family adjacent where plain alphabetical order would
	 * scatter it. Recoloured cosmetics lead with their colour word, so sorting
	 * them by name alone files each partyhat or dye next to whatever else
	 * shares its colour instead of next to the rest of its family.
	 */
	private static String clueFamily(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		if (name.endsWith(" dye")) return "dye";
		if (name.endsWith(" partyhat")) return "partyhat";
		if (name.endsWith(" halloween mask")) return "halloween mask";
		if (name.endsWith(" boater")) return "boater";
		if (name.endsWith(" headband")) return "headband";
		if (name.endsWith(" beret")) return "beret";
		if (name.endsWith(" firelighter")) return "firelighter";
		return name;
	}

	private static List<BankPreviewItem> sortBossLoot(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(PresetItemSorter::bossLootRank)
			.thenComparing(PresetItemSorter::bossLootFamily)
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static List<BankPreviewItem> sortReview(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(ReviewItemSorter::rank)
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	static int subgroupRank(String categoryKey, BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		String subcategory = normalizedName(item.getSubcategory());

		if ("currency-utilities".equals(categoryKey))
		{
			return rank(name, subcategory,
				group(0, "coins", "tokkul", "numulite", "trading sticks", "stardust", "nugget", "pearl"),
				group(10, "mark of grace", "castle wars ticket", "pieces of eight"),
				group(20, "blessing", "banner", "hilt"),
				group(30, "key", "token"));
		}

		if ("teleports-runes".equals(categoryKey))
		{
			if (item.getItemCategory() == ItemCategory.RUNE || containsAny(name, " rune", "rune ", "essence"))
			{
				return 0;
			}
			return rank(name, subcategory,
				group(10, "tablet", "teletab", "teleport"),
				group(20, "ring of", "games necklace", "amulet of", "skills necklace", "combat bracelet",
					"necklace of passage", "burning amulet"),
				group(30, "talisman", "ectophial", "medallion", "book of the dead", "whistle"));
		}

		if ("combat-gear".equals(categoryKey))
		{
			return GearItemSorter.rank(item);
		}

		if ("potions-food".equals(categoryKey))
		{
			return rank(name, subcategory,
				group(0, "brew", "restore", "prayer potion", "stamina", "super combat",
					"ranging potion", "magic potion", "attack", "strength", "defence", "holy wrench"),
				group(30, "shark", "monkfish", "karambwan", "manta", "anglerfish", "lobster",
					"swordfish", "tuna", "salmon", "trout", "pizza", "pie", "potato", "cake"),
				group(60, "wine", "stew", "curry", "kebab", "fruit", "stout", "mind bomb",
					"lizardkicker"));
		}

		if ("farming-herblore".equals(categoryKey))
		{
			return herbloreSpilloverRank(item);
		}

		if ("resources".equals(categoryKey))
		{
			return rank(name, subcategory,
				group(0, "ore", "coal"),
				group(10, "bar"),
				group(20, "log", "plank"),
				group(30, "hide", "leather", "dragonhide"),
				group(40, "gem", "uncut"),
				group(50, "bone", "bones", "ash"),
				group(60, "vial", "orb", "glass", "sand", "clay"),
				group(70, "feather", "nail", "flax", "bow string"));
		}

		if ("storage-cleanup".equals(categoryKey))
		{
			return ReviewItemSorter.rank(item);
		}

		return 50;
	}

	static int herbloreSpilloverRank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		String subcategory = normalizedName(item.getSubcategory());
		return rank(name, subcategory,
			group(0, "seed", "sapling"),
			group(10, "grimy", "clean", "herb", "leaf"),
			group(20, "secondary", "eye of newt", "snape grass", "limpwurt", "white berries",
				"mort myre", "unicorn horn"),
			group(30, "unf", "unfinished"),
			group(40, "potion"));
	}

	public static String subgroupLabel(BankCategory category, BankPreviewItem item)
	{
		if (category.getSortMode() != BankCategorySortMode.REVIEW)
		{
			return "";
		}

		return ReviewItemSorter.label(item);
	}

	private static int rank(String name, String subcategory, Group... groups)
	{
		for (Group group : groups)
		{
			for (String needle : group.needles)
			{
				if (name.contains(needle))
				{
					return group.rank;
				}
			}
		}

		return 50;
	}

	private static Group group(int rank, String... needles)
	{
		return new Group(rank, needles);
	}

	private static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle))
			{
				return true;
			}
		}

		return false;
	}

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}

	private static int clueRank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
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
		if (item.getItemCategory() == ItemCategory.UNIQUE)
		{
			String subcategory = normalizedName(item.getSubcategory());
			if (subcategory.contains("weapon-upgrade")) return 0;
			if (subcategory.contains("equipment-upgrade")) return 10;
			if (subcategory.contains("charge")) return 20;
			return 30;
		}
		if (item.getItemCategory() == ItemCategory.GEAR)
		{
			return 100;
		}
		return 50;
	}

	private static String bossLootFamily(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		if (name.contains("crystal weapon seed")) return "crystal-weapon-seed";
		if (name.endsWith(" page")) return "charge-page";
		return name;
	}

	private static final class Group
	{
		private final int rank;
		private final String[] needles;

		private Group(int rank, String[] needles)
		{
			this.rank = rank;
			this.needles = needles;
		}
	}
}

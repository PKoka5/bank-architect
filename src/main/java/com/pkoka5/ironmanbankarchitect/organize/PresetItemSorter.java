package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PresetItemSorter
{
	private PresetItemSorter()
	{
	}

	public static List<BankPreviewItem> sort(BankCategory category, List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt((BankPreviewItem item) -> subgroupRank(category.getKey(), item))
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
			return rank(name, subcategory,
				group(0, "helmet", "helm", "coif", "hat", "mask"),
				group(10, "cape", "cloak"),
				group(20, "amulet", "necklace"),
				group(30, "body", "platebody", "robe top"),
				group(40, "legs", "platelegs", "plateskirt", "chaps", "robe bottom"),
				group(50, "gloves", "vambraces"),
				group(60, "boots"),
				group(70, "shield", "defender", "book"),
				group(80, "sword", "scimitar", "mace", "dagger", "spear", "halberd", "whip",
					"bow", "staff", "wand", "crossbow", "maul", "warhammer", "battleaxe"),
				group(90, "arrow", "bolt", "dart", "javelin", "cannonball", "chinchompa"));
		}

		if ("potions-food".equals(categoryKey))
		{
			return rank(name, subcategory,
				group(0, "brew", "restore", "prayer potion", "stamina", "super combat",
					"ranging potion", "magic potion", "attack", "strength", "defence"),
				group(30, "shark", "monkfish", "karambwan", "manta", "anglerfish", "lobster",
					"swordfish", "tuna", "salmon", "trout", "pizza", "pie", "potato", "cake"),
				group(60, "wine", "stew", "curry", "kebab", "fruit"));
		}

		if ("farming-herblore".equals(categoryKey))
		{
			return rank(name, subcategory,
				group(0, "seed", "sapling"),
				group(10, "grimy", "clean", "herb", "leaf"),
				group(20, "secondary", "eye of newt", "snape grass", "limpwurt", "white berries",
					"mort myre", "unicorn horn"),
				group(30, "unf", "unfinished"),
				group(40, "potion"));
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
			if (item.getItemCategory() == ItemCategory.UNKNOWN)
			{
				return 90;
			}

			return rank(name, subcategory,
				group(0, "clue", "casket"),
				group(10, "graceful", "pyromancer", "prospector", "angler", "rogue", "lumberjack"),
				group(20, "quest", "key", "notes", "letter", "book", "scroll"),
				group(30, "costume", "cosmetic", "mask", "hat"),
				group(40, "beer glass", "junk"));
		}

		return 50;
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

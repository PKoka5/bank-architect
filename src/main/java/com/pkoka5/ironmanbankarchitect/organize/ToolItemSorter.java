package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ToolItemSorter
{
	private ToolItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(ToolItemSorter::roleRank)
			.thenComparing(ToolItemSorter::family)
			.thenComparingInt(ToolItemSorter::slotRank)
			.thenComparingInt(item -> -charge(item.getDisplayName()))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item)
	{
		String subcategory = normalized(item.getSubcategory());
		if (subcategory.contains("outfit"))
		{
			return 0;
		}
		if (subcategory.contains("quest-utility"))
		{
			return 25;
		}
		if (subcategory.contains("mould"))
		{
			return 30;
		}
		if (subcategory.contains("slayer"))
		{
			return 40;
		}
		if (subcategory.contains("container"))
		{
			return 50;
		}
		return 20;
	}

	private static String family(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		int role = roleRank(item);
		if (role == 0)
		{
			String[] outfits = {"angler", "carpenter", "farmer's", "graceful", "lumberjack", "prospector",
				"pyromancer", "rogue"};
			for (String outfit : outfits)
			{
				if (name.contains(outfit))
				{
					return outfit;
				}
			}
			return name.contains("cape") ? "skillcape" : name;
		}
		if (role == 20 || role == 50)
		{
			return String.format("%02d", skillRank(name));
		}
		if (role == 25)
		{
			if (containsAny(name, "fishbowl helmet", "diving apparatus")) return "underwater-kit";
			if (name.startsWith("vyre noble ")) return "vyre-noble";
			return name;
		}
		return "";
	}

	private static int skillRank(String name)
	{
		if (containsAny(name, "pickaxe", "mining", "celestial ring", "coal bag", "gem bag",
			"goldsmith gauntlets")) return 0;
		if (containsAny(name, " axe", "machete", "forestry", "log basket", "strung rabbit foot")) return 1;
		if (containsAny(name, "harpoon", "fishing", "lobster pot", "karambwan vessel",
			"fish barrel", "barbarian rod") || name.endsWith(" fishing rod")) return 2;
		if (containsAny(name, "rake", "spade", "seed dibber", "secateurs", "watering can", "trowel")) return 3;
		if (containsAny(name, "hammer", "saw", "plank sack")) return 4;
		if (containsAny(name, "chisel", "glassblowing", "needle")) return 5;
		if (containsAny(name, "snare", "trap", "butterfly net", "noose", "teasing",
			"fur pouch", "meat pouch")) return 6;
		if (containsAny(name, "lockpick", "house keys")) return 7;
		if (containsAny(name, "tinderbox", "warm gloves", "lantern", "torch")) return 8;
		if (containsAny(name, "cooking gauntlets", "cake tin")) return 9;
		return 20;
	}

	private static int slotRank(BankPreviewItem item)
	{
		int role = roleRank(item);
		if (role != 0 && role != 25)
		{
			return 0;
		}
		String name = normalized(item.getDisplayName());
		if (containsAny(name, "hat", "hood", "helmet", "mask")) return 0;
		if (containsAny(name, "top", "garb", "jacket", "shirt")) return 1;
		if (containsAny(name, "legs", "waders", "robe", "trousers")) return 2;
		if (containsAny(name, "gloves", "gauntlets")) return 3;
		if (containsAny(name, "boots")) return 4;
		if (containsAny(name, "cape")) return 5;
		return 6;
	}

	private static int charge(String value)
	{
		String name = normalized(value);
		int open = name.lastIndexOf('(');
		if (open < 0 || !name.endsWith(")")) return -1;
		try
		{
			return Integer.parseInt(name.substring(open + 1, name.length() - 1));
		}
		catch (NumberFormatException ignored)
		{
			return -1;
		}
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

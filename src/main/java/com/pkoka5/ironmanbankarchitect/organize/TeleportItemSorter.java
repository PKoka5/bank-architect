package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class TeleportItemSorter
{
	private TeleportItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(TeleportItemSorter::roleRank)
			.thenComparingInt(TeleportItemSorter::runeRank)
			.thenComparing(TeleportItemSorter::familyName)
			.thenComparingInt(item -> -charge(item.getDisplayName()))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (item.getItemCategory() == ItemCategory.RUNE)
		{
			if (name.contains("essence"))
			{
				return 10;
			}
			if (subcategory.contains("focus") || subcategory.contains("container")
				|| subcategory.contains("utility"))
			{
				return 20;
			}
			return 0;
		}
		if (isJewellery(name))
		{
			return 40;
		}
		if (name.contains("teleport to") || name.endsWith(" teleport")
			|| name.contains("tablet") || name.contains("teletab"))
		{
			return 30;
		}
		if (name.contains("crystal teleport seed"))
		{
			return 35;
		}
		return 50;
	}

	private static int runeRank(BankPreviewItem item)
	{
		if (roleRank(item) != 0)
		{
			return 100;
		}
		String name = normalized(item.getDisplayName());
		String[] order = {"air", "water", "earth", "fire", "mind", "body", "cosmic", "chaos",
			"nature", "law", "death", "blood", "soul", "astral", "wrath"};
		for (int i = 0; i < order.length; i++)
		{
			if (name.equals(order[i] + " rune"))
			{
				return i;
			}
		}
		return 90;
	}

	private static boolean isJewellery(String name)
	{
		return containsAny(name, "ring of dueling", "games necklace", "amulet of glory",
			"skills necklace", "combat bracelet", "burning amulet", "necklace of passage",
			"digsite pendant", "ring of wealth", "slayer ring");
	}

	private static String familyName(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		return isJewellery(name) ? name.replaceFirst("\\s*\\([0-9]+\\)$", "") : "";
	}

	private static int charge(String value)
	{
		String name = normalized(value);
		int open = name.lastIndexOf('(');
		if (open < 0 || !name.endsWith(")"))
		{
			return -1;
		}
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
			if (value.contains(needle))
			{
				return true;
			}
		}
		return false;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

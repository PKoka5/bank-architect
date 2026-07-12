package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SupplyItemSorter
{
	private SupplyItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(SupplyItemSorter::roleRank)
			.thenComparing(SupplyItemSorter::familyName)
			.thenComparingInt(SupplyItemSorter::variantRank)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (subcategory.equals("potion-dose-4") || subcategory.equals("dose-4")) return 0;
		if (subcategory.contains("pvm-utility")) return 10;
		if (subcategory.contains("food") || isFoodName(name)) return 30;
		if (subcategory.contains("drink")) return 40;
		if (subcategory.contains("potion") || name.contains(" mix")) return 20;
		return 50;
	}

	private static String familyName(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		if (name.startsWith("half a ")) name = name.substring("half a ".length());
		return name.replaceFirst("\\s*\\([1-4]\\)$", "");
	}

	private static int variantRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		if (name.startsWith("half a ")) return 1;
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

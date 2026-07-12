package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class CurrencyItemSorter
{
	private CurrencyItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(CurrencyItemSorter::roleRank)
			.thenComparingInt(CurrencyItemSorter::currencyRank)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		return containsAny(name, "hilt", "blessing", "banner") ? 100 : 0;
	}

	private static int currencyRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String[] order = {"coins", "golden nugget", "numulite", "stardust", "tokkul",
			"trading sticks", "mark of grace", "hallowed mark", "coupon", "token", "ticket"};
		for (int i = 0; i < order.length; i++)
		{
			if (name.contains(order[i])) return i;
		}
		return 50;
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

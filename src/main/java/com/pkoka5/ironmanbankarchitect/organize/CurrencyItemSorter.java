package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
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
		if (item.getItemId() == 995)
		{
			return 0;
		}
		if (IronmanMainTabPolicy.isRunePouch(item))
		{
			return 10;
		}
		if (IronmanMainTabPolicy.isGraceful(item))
		{
			return 20;
		}
		if (IronmanMainTabPolicy.isActiveClue(item))
		{
			return 30 + clueTier(item);
		}
		if (AchievementDiarySemanticRuleSet.isDiaryReward(item))
		{
			return 60;
		}
		String name = normalized(item.getDisplayName());
		if (containsAny(name, "mark of grace", "hallowed mark"))
		{
			return 40;
		}
		if (item.getItemCategory() == com.pkoka5.ironmanbankarchitect.catalog.ItemCategory.CURRENCY)
		{
			return 100;
		}
		return containsAny(name, "hilt") ? 70 : 50;
	}

	private static int clueTier(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String[] tiers = {"beginner", "easy", "medium", "hard", "elite", "master"};
		for (int index = 0; index < tiers.length; index++)
		{
			if (name.contains("(" + tiers[index] + ")")) return index;
		}
		return tiers.length;
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

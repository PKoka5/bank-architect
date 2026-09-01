package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Stable fallback order for the mixed Ironman quick-access Main tab. */
final class IronmanMainItemSorter
{
	private IronmanMainItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(IronmanMainItemSorter::compareMainItems);
		return sorted;
	}

	private static int compareMainItems(BankPreviewItem left, BankPreviewItem right)
	{
		int byRank = Integer.compare(rank(left), rank(right));
		if (byRank != 0) return byRank;
		if (left.getItemCategory() == ItemCategory.TELEPORT
			&& right.getItemCategory() == ItemCategory.TELEPORT)
		{
			return TeleportItemSorter.compareTeleportItems(left, right);
		}
		int byName = normalized(left.getDisplayName()).compareTo(normalized(right.getDisplayName()));
		return byName != 0 ? byName : Integer.compare(left.getItemId(), right.getItemId());
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
}

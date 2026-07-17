package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Selects Hammer and exactly one highest-tier owned pickaxe and axe for Ironman Main. */
public final class IronmanQuickToolSelector
{
	private static final int HAMMER = 2347;
	private static final List<List<Integer>> PICKAXE_TIERS = tiers(
		ids(23680), ids(13243, 25063, 30345), ids(20014, 11920, 12797, 23677, 25376, 30351),
		ids(23276, 1275), ids(1271), ids(1273), ids(12297), ids(1269), ids(1267), ids(1265));
	private static final List<List<Integer>> AXE_TIERS = tiers(
		ids(28220, 23673), ids(13241, 25066, 30347), ids(28226, 20011),
		ids(28217, 6739, 25378, 30352), ids(23279, 28214, 1359),
		ids(1357), ids(1355), ids(1361), ids(1353), ids(1349), ids(1351));

	private IronmanQuickToolSelector()
	{
	}

	static Set<Integer> select(BankSnapshot snapshot)
	{
		Set<Integer> owned = new LinkedHashSet<>();
		for (BankItemSnapshot item : snapshot.getItems())
		{
			if (!item.isPlaceholder()) owned.add(item.getItemId());
		}
		Set<Integer> selected = new LinkedHashSet<>();
		if (owned.contains(HAMMER)) selected.add(HAMMER);
		selectHighest(owned, PICKAXE_TIERS, selected);
		selectHighest(owned, AXE_TIERS, selected);
		return Collections.unmodifiableSet(selected);
	}

	static boolean isTieredTool(int itemId)
	{
		return contains(PICKAXE_TIERS, itemId) || contains(AXE_TIERS, itemId) || itemId == HAMMER;
	}

	/** Canonical Main segment: axe, pickaxe, hammer, chisel, then spade. */
	public static int quickAccessRank(int itemId)
	{
		if (contains(AXE_TIERS, itemId)) return 0;
		if (contains(PICKAXE_TIERS, itemId)) return 1;
		if (itemId == HAMMER) return 2;
		if (itemId == 1755) return 3;
		if (itemId == 952) return 4;
		return -1;
	}

	private static void selectHighest(Set<Integer> owned, List<List<Integer>> tiers,
		Set<Integer> selected)
	{
		for (List<Integer> tier : tiers)
		{
			for (Integer itemId : tier)
			{
				if (owned.contains(itemId))
				{
					selected.add(itemId);
					return;
				}
			}
		}
	}

	private static boolean contains(List<List<Integer>> tiers, int itemId)
	{
		for (List<Integer> tier : tiers)
		{
			if (tier.contains(itemId)) return true;
		}
		return false;
	}

	@SafeVarargs
	private static List<List<Integer>> tiers(List<Integer>... tiers)
	{
		return Collections.unmodifiableList(Arrays.asList(tiers));
	}

	private static List<Integer> ids(Integer... ids)
	{
		return Collections.unmodifiableList(Arrays.asList(ids));
	}
}

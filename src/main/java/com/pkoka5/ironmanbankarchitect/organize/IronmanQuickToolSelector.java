package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.*;
import java.util.*;
import java.util.List;


/** Selects one owned tool per quick-access family, preserving bank placeholders. */
public final class IronmanQuickToolSelector
{
	private static final List<List<Integer>> HAMMER_TIERS = tiers(ids(25644, 29775), ids(2347));
	private static final List<List<Integer>> CHISEL_TIERS = tiers(ids(34024), ids(1755));
	private static final List<List<Integer>> PICKAXE_TIERS = tiers(
		ids(23680), ids(13243, 25063, 30345), ids(20014, 11920, 12797, 23677, 25376, 30351, 13244, 25369, 30346),
		ids(23276, 1275), ids(1271), ids(1273), ids(12297), ids(1269), ids(1267), ids(1265));
	private static final List<List<Integer>> AXE_TIERS = tiers(
		ids(28220, 23673), ids(13241, 25066, 30347), ids(28226, 20011),
		ids(28217, 6739, 25378, 30352, 13242, 25371, 30348), ids(23279, 28214, 1359),
		ids(1357), ids(1355), ids(1361), ids(1353), ids(1349), ids(1351));

	private IronmanQuickToolSelector()
	{
	}

	static Set<Integer> select(BankSnapshot snapshot)
	{
		Set<Integer> owned = new LinkedHashSet<>();
		for (BankItemSnapshot item : snapshot.getItems())
		{
			// A bank placeholder preserves the player's tool choice while the
			// item is out of the bank. No inventory/equipment read is needed.
			owned.add(item.getItemId());
		}
		Set<Integer> selected = new LinkedHashSet<>();
		selectHighest(owned, HAMMER_TIERS, selected);
		selectHighest(owned, CHISEL_TIERS, selected);
		selectHighest(owned, PICKAXE_TIERS, selected);
		selectHighest(owned, AXE_TIERS, selected);
		return Collections.unmodifiableSet(selected);
	}

	static boolean isTieredTool(int itemId)
	{
		int rank = quickAccessRank(itemId);
		return rank >= 0 && rank < 4;
	}

	/** Uses functional tiers, never a placeholder or an equivalent ornament as an upgrade. */
	static boolean hasOwnedUpgrade(int itemId, Set<Integer> realOwned)
	{
		for (List<List<Integer>> family : Arrays.asList(PICKAXE_TIERS, AXE_TIERS))
		{
			boolean higherOwned = false;
			for (List<Integer> tier : family)
			{
				if (tier.contains(itemId)) return higherOwned;
				higherOwned |= !Collections.disjoint(realOwned, tier);
			}
		}
		return false;
	}

	/** Canonical Main segment: axe, pickaxe, hammer, chisel, then spade. */
	public static int quickAccessRank(int itemId)
	{
		if (contains(AXE_TIERS, itemId)) return 0;
		if (contains(PICKAXE_TIERS, itemId)) return 1;
		if (contains(HAMMER_TIERS, itemId)) return 2;
		if (contains(CHISEL_TIERS, itemId)) return 3;
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

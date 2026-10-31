package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import java.util.*;
import java.util.List;


/** Selects one owned tool per quick-access family, preserving bank placeholders. */
public final class IronmanQuickToolSelector
{
	private static final RequiredResource<List<List<List<Integer>>>> FAMILIES = new RequiredResource<>("quick tools", () -> {
		OrderedItemFamilies rows = new OrderedItemFamilies(IronmanQuickToolSelector.class.getResourceAsStream("quick-tool-tiers.tsv"),0);
		List<List<List<Integer>>> families = new ArrayList<>();
		for (String family : Arrays.asList("hammer","chisel","pickaxe","axe"))
			families.add(Collections.unmodifiableList(new ArrayList<>(rows.group(family).values())));
		return Collections.unmodifiableList(families);
	});

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
		for (List<List<Integer>> family : FAMILIES.get()) selectHighest(owned,family,selected);
		return Collections.unmodifiableSet(selected);
	}

	static boolean isTieredTool(int itemId)
	{
		int rank = quickAccessRank(itemId);
		return rank >= 0 && rank < 4;
	}

	/** Functional tiers count recorded bank placeholders; equivalent ornaments are not upgrades. */
	static boolean hasOwnedUpgrade(int itemId, Set<Integer> recorded)
	{
		return ownedUpgradeId(itemId, recorded) > 0;
	}

	static int ownedUpgradeId(int itemId, Set<Integer> recorded)
	{
		for (List<List<Integer>> family : FAMILIES.get().subList(2,4))
		{
			int replacement = 0;
			for (List<Integer> tier : family)
			{
				if (tier.contains(itemId)) return replacement;
				if (replacement == 0)
					for (int candidate : tier)
						if (recorded.contains(candidate)) { replacement = candidate; break; }
			}
		}
		return 0;
	}

	/** Canonical Main segment: axe, pickaxe, hammer, chisel, then spade. */
	public static int quickAccessRank(int itemId)
	{
		int[] families = {3,2,0,1};
		for (int rank = 0; rank < families.length; rank++)
			if (contains(FAMILIES.get().get(families[rank]),itemId)) return rank;
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

}

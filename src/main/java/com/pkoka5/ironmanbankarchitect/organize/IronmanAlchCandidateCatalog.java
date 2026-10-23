package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;

/**
 * Reviewed canonical IDs for common Slayer/bossing alch stock.
 *
 * <p>The bundled list preserves RuneLite's player-facing gameval IDs:
 * noted, POH, Battle Royale, League, dummy, ornament and clue variants are not
 * inferred from display names and therefore cannot enter by collision.</p>
 */
final class IronmanAlchCandidateCatalog
{
	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		IronmanAlchCandidateCatalog.class.getResourceAsStream("alch-candidates.tsv"), 0, false);
	private static final RequiredResource<Data> DATA = new RequiredResource<>("alch candidates", Data::new);
	private static final class Data
	{
		final Map<Integer,Integer> tiers = new HashMap<>();
		Data()
		{
			TABLE.group("tiers").forEach((key,ids) -> ids.forEach(id -> tiers.put(id,Integer.parseInt(key))));
		}
		final Set<Integer> candidates = new HashSet<>(TABLE.ids("candidates"));
		final Set<Integer> fixed = new HashSet<>(TABLE.ids("fixed-list"));
		final Collection<List<Integer>> staves = TABLE.group("staves").values();
		final Collection<List<Integer>> colours = TABLE.group("mystic").values();
		final Set<Integer> slashStock = new HashSet<>(TABLE.ids("slash-stock"));
		final Set<Integer> slashUpgrades = new HashSet<>(TABLE.ids("slash-upgrades"));
		final Set<Integer> requiresDominance = new HashSet<>(TABLE.ids("requires-dominance"));
	}

	private IronmanAlchCandidateCatalog()
	{
	}

	/** Exact progression facts; names alone cannot prove a replacement for alching. */
	static int tierOf(int itemId)
	{
		return DATA.get().tiers.getOrDefault(itemId,GearTierCatalog.INSTANCE.tierOf(itemId).orElse(0));
	}

	static boolean contains(int itemId)
	{
		return DATA.get().candidates.contains(itemId) || isElementalStaff(itemId);
	}

	static boolean containsFixed(int itemId) { return DATA.get().fixed.contains(itemId); }

	static boolean isElementalStaff(int itemId)
	{
		for (List<Integer> family : DATA.get().staves)
			for (int member : family) if (member == itemId) return true;
		return false;
	}

	static boolean requiresDominance(int itemId) { return DATA.get().requiresDominance.contains(itemId); }

	/** Exact role evidence; colours never replace each other in both directions. */
	static boolean hasRoleReplacement(int itemId, Set<Integer> owned, Map<Integer, String> choices)
	{
		for (List<Integer> family : DATA.get().staves)
		{
			boolean higherOwned = false;
			for (int member : family)
			{
				if (member == itemId) return higherOwned;
				higherOwned |= owned.contains(member);
			}
		}
		for (List<Integer> family : DATA.get().colours)
		{
			boolean member = false;
			int keeper = -1;
			boolean pinnedKeeper = false;
			for (int candidate : family)
			{
				member |= candidate == itemId;
				String choice = choices.get(candidate);
				boolean pinned = "gear".equals(choice) || "combat-gear".equals(choice);
				if (owned.contains(candidate) && (choice == null || pinned)
					&& (keeper < 0 || pinned && !pinnedKeeper))
				{
					keeper = candidate;
					pinnedKeeper = pinned;
				}
			}
			if (member) return keeper > 0 && keeper != itemId;
		}
		return DATA.get().slashStock.contains(itemId) && !Collections.disjoint(owned, DATA.get().slashUpgrades);
	}
}

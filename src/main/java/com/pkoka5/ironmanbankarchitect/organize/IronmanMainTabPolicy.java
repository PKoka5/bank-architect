package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Reviewed Ironman quick-access semantics for the unnumbered main bank tab.
 * The exact utility IDs recur across the local ten-template research cohort;
 * achievement families are recognized narrowly by their canonical reward prefix.
 */
final class IronmanMainTabPolicy
{
	/**
	 * Every graceful recolour counts, not just the base set.
	 *
	 * <p>The six base IDs were listed here by hand, so a player who swapped
	 * their set for a course recolour watched it leave the tab it had always
	 * sat on. The recolours are already catalogued as {@code tools.graceful-*}
	 * families, so asking the catalogue means a recolour released later works
	 * without anyone remembering to come back here.</p>
	 */
	private static final String GRACEFUL_FAMILY_PREFIX = "tools.graceful-";
	private static final Set<Integer> RUNE_POUCH_IDS = Collections.unmodifiableSet(
		new HashSet<>(Arrays.asList(12791, 27281, 27509)));
	private static final Set<Integer> RECURRING_UTILITY_IDS = Collections.unmodifiableSet(
		new HashSet<>(Arrays.asList(
			8013, 4251, 30638, 32399, 19564, 22400, 13393, 25818, 21389,
			1755, 952, 24711)));

	private static final String[] ACHIEVEMENT_REWARD_PREFIXES = {
		"ardougne cloak", "desert amulet", "explorer's ring", "falador shield",
		"fremennik sea boots", "ghommal's hilt", "kandarin headgear", "karamja gloves",
		"morytania legs", "rada's blessing", "varrock armour", "western banner",
		"wilderness sword"
	};

	private IronmanMainTabPolicy()
	{
	}

	static boolean belongsOnMain(CatalogItem item)
	{
		if (isGraceful(item) || isRunePouch(item)
			|| RECURRING_UTILITY_IDS.contains(item.getItemId()))
		{
			return true;
		}

		String name = normalized(item.getDisplayName());
		for (String prefix : ACHIEVEMENT_REWARD_PREFIXES)
		{
			if (name.equals(prefix) || name.startsWith(prefix + " "))
			{
				return true;
			}
		}
		return false;
	}

	static boolean isGraceful(CatalogItem item)
	{
		return isGraceful(item.getItemId());
	}

	static boolean isGraceful(BankPreviewItem item)
	{
		return isGraceful(item.getItemId());
	}

	static boolean isGraceful(int itemId)
	{
		return ItemSetCatalog.setKeyOf(itemId)
			.filter(key -> key.startsWith(GRACEFUL_FAMILY_PREFIX))
			.isPresent();
	}

	static boolean isRunePouch(CatalogItem item)
	{
		return RUNE_POUCH_IDS.contains(item.getItemId());
	}

	static boolean isRunePouch(BankPreviewItem item)
	{
		return RUNE_POUCH_IDS.contains(item.getItemId());
	}

	static boolean isActiveClue(CatalogItem item)
	{
		return item.getCategory() == com.pkoka5.ironmanbankarchitect.catalog.ItemCategory.CLUE
			&& "treasure-trail".equals(normalized(item.getSubcategory()));
	}

	static boolean isActiveClue(BankPreviewItem item)
	{
		return item.getItemCategory() == com.pkoka5.ironmanbankarchitect.catalog.ItemCategory.CLUE
			&& "treasure-trail".equals(normalized(item.getSubcategory()));
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

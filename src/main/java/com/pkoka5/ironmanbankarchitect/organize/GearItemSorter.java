package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class GearItemSorter
{
	private static final int STYLE_MELEE = 0;
	private static final int STYLE_RANGED = 1;
	private static final int STYLE_MAGIC = 2;
	private static final int STYLE_OTHER = 3;
	private static final int[] SETUP_STYLES = {STYLE_MELEE, STYLE_RANGED, STYLE_MAGIC};
	private static final int[] SETUP_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

	private GearItemSorter()
	{
	}

	/**
	 * Dense layout: the bank always compacts items left-to-right, so the plan
	 * must never contain gaps. Gear is grouped as one contiguous run per combat
	 * style (melee, then ranged, then magic), each run in equipment-slot order
	 * head..weapon/ammo, followed by sidegrades and everything else.
	 */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, GearStatsSource.NONE);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		Map<String, BankPreviewItem> bestSetupItems = bestSetupItems(items, gearStats);

		List<BankPreviewItem> laidOut = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		for (int style : SETUP_STYLES)
		{
			for (int slot : SETUP_SLOTS)
			{
				BankPreviewItem item = bestSetupItems.get(style + ":" + slot);
				if (item != null && usedItemIds.add(item.getItemId()))
				{
					laidOut.add(item);
				}
			}
		}

		laidOut.addAll(remainingSorted(items, usedItemIds, gearStats));
		return laidOut;
	}

	private static Map<String, BankPreviewItem> bestSetupItems(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		Map<String, BankPreviewItem> bestSetupItems = new LinkedHashMap<>();
		for (BankPreviewItem item : items)
		{
			int style = styleRankOf(item, gearStats);
			int slot = slotRankOf(item, gearStats);
			if (style == STYLE_OTHER || slot >= 12)
			{
				continue;
			}

			String key = style + ":" + slot;
			BankPreviewItem current = bestSetupItems.get(key);
			if (current == null || scoreOf(item, gearStats) > scoreOf(current, gearStats))
			{
				bestSetupItems.put(key, item);
			}
		}

		return bestSetupItems;
	}

	private static List<BankPreviewItem> remainingSorted(List<BankPreviewItem> items, Set<Integer> usedItemIds,
		GearStatsSource gearStats)
	{
		List<BankPreviewItem> remaining = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			if (!usedItemIds.contains(item.getItemId()))
			{
				remaining.add(item);
			}
		}

		remaining.sort(Comparator
			.comparingInt((BankPreviewItem item) -> rankOf(item, gearStats))
			.thenComparing((BankPreviewItem item) -> -scoreOf(item, gearStats))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return remaining;
	}

	static int rank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		return slotRank(name) * 100 + styleRank(name);
	}

	private static int rankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		return slotRankOf(item, gearStats) * 100 + styleRankOf(item, gearStats);
	}

	private static int slotRankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			return stats.get().slotRank();
		}

		return slotRank(normalizedName(item.getDisplayName()));
	}

	private static int styleRankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			return stats.get().style().ordinal();
		}

		return styleRank(normalizedName(item.getDisplayName()));
	}

	private static int scoreOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		int score = gearScore(item);
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		return stats.isPresent() ? score + stats.get().score() : score;
	}

	private static int slotRank(String name)
	{
		if (containsAny(name, "helmet", "helm", "coif", "hat", "mask", "hood"))
		{
			return 0;
		}
		if (containsAny(name, "body", "platebody", "robe top", "hauberk", "torso", "chestplate"))
		{
			return 1;
		}
		if (containsAny(name, "legs", "platelegs", "plateskirt", "chaps", "robe bottom", "skirt", "tassets"))
		{
			return 2;
		}
		if (containsAny(name, "cape", "cloak", "ava's", "avas", "accumulator", "assembler"))
		{
			return 3;
		}
		if (containsAny(name, "amulet", "necklace", "symbol", "stole"))
		{
			return 4;
		}
		if (containsAny(name, "shield", "defender", "book", "ward", "offhand"))
		{
			return 5;
		}
		if (containsAny(name, "gloves", "vambraces", "bracelet"))
		{
			return 6;
		}
		if (containsAny(name, "boots"))
		{
			return 7;
		}
		if (containsAny(name, "sword", "scimitar", "mace", "dagger", "spear", "halberd", "whip",
			"maul", "warhammer", "battleaxe", "hasta", "rapier", "salamander", "flail"))
		{
			return 8;
		}
		if (containsAny(name, "bow", "crossbow", "ballista", "blowpipe"))
		{
			return 9;
		}
		if (containsAny(name, "staff", "wand", "trident", "sceptre", "scepter"))
		{
			return 10;
		}
		if (containsAny(name, "arrow", "bolt", "dart", "javelin", "cannonball", "chinchompa", "bolt rack"))
		{
			return 11;
		}

		return 12;
	}

	private static int styleRank(String name)
	{
		if (isRanged(name))
		{
			return STYLE_RANGED;
		}
		if (isMagic(name))
		{
			return STYLE_MAGIC;
		}
		if (isMelee(name))
		{
			return STYLE_MELEE;
		}

		return STYLE_OTHER;
	}

	private static boolean isMelee(String name)
	{
		return containsAny(name, "rune", "dragon", "barrows", "bandos", "torva", "obsidian", "fighter",
			"berserker", "defender", "scimitar", "whip", "mace", "spear", "halberd", "warhammer",
			"battleaxe", "maul", "hasta", "rapier", "platebody", "platelegs", "plateskirt", "helm",
			"neitiznot", "serpentine", "faceguard", "granite", "justiciar", "verac", "dharok", "guthan",
			"torag", "karamja gloves", "barrows gloves");
	}

	private static boolean isRanged(String name)
	{
		return containsAny(name, "bow", "crossbow", "ballista", "blowpipe", "arrow", "bolt", "dart",
			"javelin", "chinchompa", "coif", "chaps", "vambraces", "leather", "d'hide", "dragonhide",
			"karil", "armadyl", "ava's", "avas", "accumulator", "assembler");
	}

	private static boolean isMagic(String name)
	{
		return containsAny(name, "staff", "wand", "trident", "sceptre", "scepter", "mystic", "ahrim",
			"ancestral", "infinity", "wizard", "splitbark", "lunar", "xerician", "ghostly", "robe",
			"occult", "tome");
	}

	private static int gearScore(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		int score = 0;
		score = Math.max(score, scoreIfContains(name, 1000, "torva", "ancestral", "masori", "tumeken", "twisted bow",
			"scythe", "shadow"));
		score = Math.max(score, scoreIfContains(name, 900, "bandos", "armadyl", "ahrim", "karil", "zaryte",
			"crystal", "bowfa", "bow of faerdhinen", "toxic blowpipe", "trident", "occult", "primordial",
			"pegasian", "eternal"));
		score = Math.max(score, scoreIfContains(name, 800, "barrows", "fighter torso", "serpentine",
			"faceguard", "dragonfire", "abyssal", "whip", "tentacle", "dragon defender", "rune defender",
			"blessed d'hide", "god d'hide", "malediction", "odium", "toxic"));
		score = Math.max(score, scoreIfContains(name, 700, "dragon", "black d'hide", "mystic", "infinity",
			"rune crossbow", "magic shortbow", "book of darkness", "tome"));
		score = Math.max(score, scoreIfContains(name, 600, "rune", "red d'hide", "blue d'hide", "green d'hide",
			"splitbark", "xerician"));
		score = Math.max(score, scoreIfContains(name, 500, "adamant", "mithril", "leather", "wizard"));
		return score;
	}

	private static int scoreIfContains(String name, int score, String... needles)
	{
		return containsAny(name, needles) ? score : 0;
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

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

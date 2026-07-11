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
	private static final int STYLE_PRAYER = 3;
	private static final int STYLE_OTHER = 4;
	private static final int[] SETUP_STYLES = {STYLE_MELEE, STYLE_RANGED, STYLE_MAGIC, STYLE_PRAYER};
	private static final int[] SETUP_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

	// Must match the popup grid width; only full rows keep the set columns
	// aligned once the real bank compacts everything.
	static final int GRID_COLUMNS = 8;
	private static final int SET_COLUMNS = SETUP_STYLES.length;
	// One row per equipment slot; cell value = slot for that style column, -1 = filler.
	private static final int[][] SET_ROWS = {
		{0, 0, 0, 0},
		{1, 1, 1, 1},
		{2, 2, 2, 2},
		{3, 3, 3, 3},
		{4, 4, 4, 4},
		{5, 5, 5, 5},
		{6, 6, 6, 6},
		{7, 7, 7, 7},
		{8, 9, 10, -1}
	};

	private GearItemSorter()
	{
	}

	/**
	 * Set-column layout: rows are equipment slots, the first four columns are
	 * the melee/ranged/magic/prayer setups (best owned item first, sidegrades
	 * below it in the same column), and the remaining cells of each row are
	 * filled with grouped leftover items. The bank always compacts, so a row is
	 * only emitted when all {@link #GRID_COLUMNS} cells can be filled; as soon
	 * as filler runs out the layout falls back to dense per-style runs.
	 */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, GearStatsSource.NONE);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		Map<String, List<BankPreviewItem>> setCandidates = new LinkedHashMap<>();
		List<BankPreviewItem> filler = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			int style = styleRankOf(item, gearStats);
			int slot = slotRankOf(item, gearStats);
			if (style == STYLE_OTHER || slot >= 12 || slot == 11)
			{
				filler.add(item);
			}
			else
			{
				setCandidates.computeIfAbsent(style + ":" + slot, key -> new ArrayList<>()).add(item);
			}
		}

		Comparator<BankPreviewItem> byTier = Comparator
			.comparing((BankPreviewItem item) -> -scoreOf(item, gearStats))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId);
		for (List<BankPreviewItem> candidates : setCandidates.values())
		{
			candidates.sort(byTier);
		}
		filler.sort(Comparator
			.comparingInt((BankPreviewItem item) -> rankOf(item, gearStats))
			.thenComparing(byTier));

		List<BankPreviewItem> laidOut = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		Map<String, Integer> takenPerKey = new LinkedHashMap<>();
		int fillerIndex = 0;

		for (int[] setRow : SET_ROWS)
		{
			if (!rowHasSetItem(setRow, setCandidates, takenPerKey))
			{
				continue;
			}

			List<BankPreviewItem> cells = new ArrayList<>(GRID_COLUMNS);
			Map<String, Integer> takenThisRow = new LinkedHashMap<>();
			int rowFillerIndex = fillerIndex;
			for (int column = 0; column < GRID_COLUMNS && cells.size() == column; column++)
			{
				BankPreviewItem cell = null;
				if (column < SET_COLUMNS && setRow[column] >= 0)
				{
					String key = column + ":" + setRow[column];
					List<BankPreviewItem> candidates = setCandidates.get(key);
					int taken = takenPerKey.getOrDefault(key, 0);
					if (candidates != null && taken < candidates.size())
					{
						cell = candidates.get(taken);
						takenThisRow.merge(key, 1, Integer::sum);
					}
				}
				if (cell == null && rowFillerIndex < filler.size())
				{
					cell = filler.get(rowFillerIndex);
					rowFillerIndex++;
				}
				if (cell != null)
				{
					cells.add(cell);
				}
			}

			if (cells.size() < GRID_COLUMNS)
			{
				break;
			}

			for (Map.Entry<String, Integer> taken : takenThisRow.entrySet())
			{
				takenPerKey.merge(taken.getKey(), taken.getValue(), Integer::sum);
			}
			fillerIndex = rowFillerIndex;
			for (BankPreviewItem cell : cells)
			{
				usedItemIds.add(cell.getItemId());
				laidOut.add(cell);
			}
		}

		// Dense fallback: best remaining item per style and slot as contiguous
		// per-style runs, then everything else grouped by slot.
		for (int style : SETUP_STYLES)
		{
			for (int slot : SETUP_SLOTS)
			{
				String key = style + ":" + slot;
				List<BankPreviewItem> candidates = setCandidates.get(key);
				int taken = takenPerKey.getOrDefault(key, 0);
				if (candidates != null && taken < candidates.size())
				{
					BankPreviewItem item = candidates.get(taken);
					if (usedItemIds.add(item.getItemId()))
					{
						laidOut.add(item);
						takenPerKey.put(key, taken + 1);
					}
				}
			}
		}

		laidOut.addAll(remainingSorted(items, usedItemIds, gearStats));
		return laidOut;
	}

	private static boolean rowHasSetItem(int[] setRow, Map<String, List<BankPreviewItem>> setCandidates,
		Map<String, Integer> takenPerKey)
	{
		for (int column = 0; column < SET_COLUMNS; column++)
		{
			if (setRow[column] < 0)
			{
				continue;
			}

			String key = column + ":" + setRow[column];
			List<BankPreviewItem> candidates = setCandidates.get(key);
			if (candidates != null && takenPerKey.getOrDefault(key, 0) < candidates.size())
			{
				return true;
			}
		}

		return false;
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
		if (isPrayer(name))
		{
			return STYLE_PRAYER;
		}

		return STYLE_OTHER;
	}

	private static boolean isPrayer(String name)
	{
		return containsAny(name, "proselyte", "initiate", "monk's", "holy symbol", "holy sandals");
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

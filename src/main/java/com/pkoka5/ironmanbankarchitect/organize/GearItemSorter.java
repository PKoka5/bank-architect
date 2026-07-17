package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
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
	private static final int MIN_STYLE_COLUMNS_PER_ROW = 2;
	// One row per equipment slot; cell value = slot for that style column, -1 = no set cell.
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
	 * below it in the same column). Remaining cells of a row are filled with
	 * more items of the same slot (spare helms complete the helm row, tier
	 * order), then with grouped leftover items. The bank always compacts, so a
	 * row is only emitted when all {@link #GRID_COLUMNS} cells can be filled.
	 * A sparse row falls back to the dense tail without blocking a later slot
	 * row that can still be aligned entirely with real owned items.
	 */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, GearStatsSource.NONE);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return plan(items, gearStats).allItems();
	}

	/**
	 * Separates the physically aligned, complete setup rows from the dense tail.
	 * Callers may apply secondary grouping rules to the tail, but must never
	 * repack the setup rows or the four combat-style columns would be lost.
	 */
	static GearLayout plan(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		Map<String, List<BankPreviewItem>> setCandidates = new LinkedHashMap<>();
		for (BankPreviewItem item : items)
		{
			int style = styleRankOf(item, gearStats);
			int slot = slotRankOf(item, gearStats);
			if (slot == 11)
			{
				// Ammo is a separate dense block after equipment. It must never
				// disguise a missing wearable in the aligned setup rows.
				continue;
			}
			if (style != STYLE_OTHER && slot < 12)
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
		List<BankPreviewItem> laidOut = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		Set<Integer> reservedPrimaryIds = reservePrimaryItems(setCandidates);
		Set<Integer> protectedVerticalSetIds = GearSetSemanticRuleSet.presentFamilyItemIds(items);
		List<BankPreviewItem> fillerOrder = remainingSorted(items, new LinkedHashSet<>(), gearStats);

		for (int[] setRow : SET_ROWS)
		{
			BankPreviewItem[] cells = new BankPreviewItem[GRID_COLUMNS];
			int primaryCount = 0;
			for (int column = 0; column < SET_COLUMNS; column++)
			{
				if (setRow[column] < 0)
				{
					continue;
				}
				List<BankPreviewItem> candidates = setCandidates.get(column + ":" + setRow[column]);
				if (candidates != null && !candidates.isEmpty())
				{
					cells[column] = candidates.get(0);
					primaryCount++;
				}
			}

			if (primaryCount < MIN_STYLE_COLUMNS_PER_ROW)
			{
				continue;
			}

			// These primaries belong to this row. Releasing them before the
			// feasibility check lets a skipped sparse row fall back normally,
			// while all later rows remain protected from filler selection.
			for (int column = 0; column < SET_COLUMNS; column++)
			{
				if (cells[column] != null)
				{
					reservedPrimaryIds.remove(cells[column].getItemId());
					usedItemIds.add(cells[column].getItemId());
				}
			}

			int fillersNeeded = GRID_COLUMNS - primaryCount;
			Set<Integer> rowProtectedVerticalSetIds = protectedVerticalSetIds;
			if (availableFillerCount(fillerOrder, usedItemIds, reservedPrimaryIds,
				rowProtectedVerticalSetIds, gearStats) < fillersNeeded)
			{
				// The dense bank cannot preserve both shapes when no real alternative filler exists.
				// Keep the primary setup row and release family protection only for this row.
				rowProtectedVerticalSetIds = Collections.emptySet();
				if (availableFillerCount(fillerOrder, usedItemIds, reservedPrimaryIds,
					rowProtectedVerticalSetIds, gearStats) < fillersNeeded)
				{
					for (int column = 0; column < SET_COLUMNS; column++)
					{
						if (cells[column] != null)
						{
							usedItemIds.remove(cells[column].getItemId());
							reservedPrimaryIds.add(cells[column].getItemId());
						}
					}
					continue;
				}
			}
			for (int column = 0; column < GRID_COLUMNS; column++)
			{
				if (cells[column] == null && column < SET_COLUMNS && setRow[column] >= 0)
				{
					cells[column] = takeBestFillerForSlot(setRow[column], fillerOrder,
						usedItemIds, reservedPrimaryIds, rowProtectedVerticalSetIds, gearStats);
				}
				if (cells[column] == null)
				{
					cells[column] = takeBestFillerForRow(setRow, fillerOrder,
						usedItemIds, reservedPrimaryIds, rowProtectedVerticalSetIds, gearStats);
				}
				if (cells[column] == null)
				{
					cells[column] = takeFirstFiller(fillerOrder, usedItemIds,
						reservedPrimaryIds, rowProtectedVerticalSetIds, gearStats);
				}
				usedItemIds.add(cells[column].getItemId());
				laidOut.add(cells[column]);
			}
		}

		List<BankPreviewItem> setupRows = new ArrayList<>(laidOut);
		List<BankPreviewItem> tail = new ArrayList<>();

		// Dense fallback: best remaining item per style and slot as contiguous
		// per-style runs, then everything else grouped by slot.
		for (int style : SETUP_STYLES)
		{
			for (int slot : SETUP_SLOTS)
			{
				String key = style + ":" + slot;
				List<BankPreviewItem> candidates = setCandidates.get(key);
				if (candidates != null)
				{
					for (BankPreviewItem item : candidates)
					{
						if (usedItemIds.add(item.getItemId()))
						{
							tail.add(item);
							break;
						}
					}
				}
			}
		}

		tail.addAll(remainingSorted(items, usedItemIds, gearStats));
		return new GearLayout(setupRows, tail);
	}

	static final class GearLayout
	{
		private final List<BankPreviewItem> setupRows;
		private final List<BankPreviewItem> tail;

		private GearLayout(List<BankPreviewItem> setupRows, List<BankPreviewItem> tail)
		{
			this.setupRows = setupRows;
			this.tail = tail;
		}

		List<BankPreviewItem> getSetupRows()
		{
			return setupRows;
		}

		List<BankPreviewItem> getTail()
		{
			return tail;
		}

		private List<BankPreviewItem> allItems()
		{
			List<BankPreviewItem> result = new ArrayList<>(setupRows.size() + tail.size());
			result.addAll(setupRows);
			result.addAll(tail);
			return result;
		}
	}

	private static Set<Integer> reservePrimaryItems(Map<String, List<BankPreviewItem>> setCandidates)
	{
		Set<Integer> reserved = new LinkedHashSet<>();
		for (int[] row : SET_ROWS)
		{
			for (int column = 0; column < SET_COLUMNS; column++)
			{
				if (row[column] < 0)
				{
					continue;
				}
				List<BankPreviewItem> candidates = setCandidates.get(column + ":" + row[column]);
				if (candidates != null && !candidates.isEmpty())
				{
					reserved.add(candidates.get(0).getItemId());
				}
			}
		}
		return reserved;
	}

	private static int availableFillerCount(List<BankPreviewItem> candidates, Set<Integer> usedItemIds,
		Set<Integer> reservedPrimaryIds, Set<Integer> protectedVerticalSetIds,
		GearStatsSource gearStats)
	{
		int available = 0;
		for (BankPreviewItem candidate : candidates)
		{
			if (isAvailableFiller(candidate, usedItemIds, reservedPrimaryIds,
				protectedVerticalSetIds, gearStats))
			{
				available++;
			}
		}
		return available;
	}

	private static BankPreviewItem takeBestFillerForSlot(int slot, List<BankPreviewItem> candidates,
		Set<Integer> usedItemIds, Set<Integer> reservedPrimaryIds,
		Set<Integer> protectedVerticalSetIds, GearStatsSource gearStats)
	{
		for (BankPreviewItem candidate : candidates)
		{
			if (slotRankOf(candidate, gearStats) == slot
				&& isAvailableFiller(candidate, usedItemIds, reservedPrimaryIds,
					protectedVerticalSetIds, gearStats))
			{
				return candidate;
			}
		}
		return null;
	}

	private static BankPreviewItem takeBestFillerForRow(int[] row, List<BankPreviewItem> candidates,
		Set<Integer> usedItemIds, Set<Integer> reservedPrimaryIds,
		Set<Integer> protectedVerticalSetIds, GearStatsSource gearStats)
	{
		for (BankPreviewItem candidate : candidates)
		{
			int slot = slotRankOf(candidate, gearStats);
			if (rowContainsSlot(row, slot)
				&& isAvailableFiller(candidate, usedItemIds, reservedPrimaryIds,
					protectedVerticalSetIds, gearStats))
			{
				return candidate;
			}
		}
		return null;
	}

	private static BankPreviewItem takeFirstFiller(List<BankPreviewItem> candidates,
		Set<Integer> usedItemIds, Set<Integer> reservedPrimaryIds,
		Set<Integer> protectedVerticalSetIds, GearStatsSource gearStats)
	{
		// Prefer unstructured accessories (especially rings) before borrowing
		// from another equipment row. They have no setup row of their own.
		for (BankPreviewItem candidate : candidates)
		{
			int slot = slotRankOf(candidate, gearStats);
			if ((styleRankOf(candidate, gearStats) == STYLE_OTHER || slot >= 12)
				&& isAvailableFiller(candidate, usedItemIds, reservedPrimaryIds,
					protectedVerticalSetIds, gearStats))
			{
				return candidate;
			}
		}

		// When cross-row borrowing is unavoidable, take overflow from the
		// latest slot group (normally spare weapons) so legs, capes, necks and
		// boots still reach their own rows. Candidate order keeps the best item
		// within that chosen slot first.
		int latestSlot = -1;
		BankPreviewItem selected = null;
		for (BankPreviewItem candidate : candidates)
		{
			if (!isAvailableFiller(candidate, usedItemIds, reservedPrimaryIds,
				protectedVerticalSetIds, gearStats))
			{
				continue;
			}
			int slot = slotRankOf(candidate, gearStats);
			if (slot > latestSlot)
			{
				latestSlot = slot;
				selected = candidate;
			}
		}
		return selected;
	}

	private static boolean isAvailableFiller(BankPreviewItem candidate, Set<Integer> usedItemIds,
		Set<Integer> reservedPrimaryIds, Set<Integer> protectedVerticalSetIds,
		GearStatsSource gearStats)
	{
		return slotRankOf(candidate, gearStats) != 11
			&& !usedItemIds.contains(candidate.getItemId())
			&& !reservedPrimaryIds.contains(candidate.getItemId())
			&& !protectedVerticalSetIds.contains(candidate.getItemId());
	}

	private static boolean rowContainsSlot(int[] row, int slot)
	{
		for (int value : row)
		{
			if (value == slot)
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
			.thenComparingInt(item -> ammoFamilyRank(item, gearStats))
			.thenComparingInt(item -> ammoTierRank(item, gearStats))
			.thenComparing((BankPreviewItem item) -> -scoreOf(item, gearStats))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return remaining;
	}

	static int rank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		int slot = slotRank(name);
		return slot == 11 ? 1300 : slot * 100 + styleRank(name);
	}

	private static int rankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		int slot = slotRankOf(item, gearStats);
		return slot == 11 ? 1300 : slot * 100 + styleRankOf(item, gearStats);
	}

	private static int ammoFamilyRank(BankPreviewItem item, GearStatsSource gearStats)
	{
		if (slotRankOf(item, gearStats) != 11) return 0;
		String name = normalizedName(item.getDisplayName());
		if (containsAny(name, "arrow", "brutal")) return 0;
		if (containsAny(name, "bolt", "bolt rack")) return 10;
		if (name.contains("dart")) return 20;
		if (name.contains("javelin")) return 30;
		if (name.contains("cannonball")) return 40;
		if (name.contains("grapple")) return 50;
		return 60;
	}

	private static int ammoTierRank(BankPreviewItem item, GearStatsSource gearStats)
	{
		if (slotRankOf(item, gearStats) != 11) return 0;
		String name = normalizedName(item.getDisplayName());
		String[] tiers = {"diamond", "dragon", "amethyst", "rune", "adamant", "broad",
			"mithril", "bone", "steel", "iron", "bronze"};
		for (int i = 0; i < tiers.length; i++)
		{
			if (name.contains(tiers[i])) return i;
		}
		return 100;
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

	static int score(BankPreviewItem item, GearStatsSource gearStats)
	{
		return scoreOf(item, gearStats);
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
		if (containsAny(name, "arrow", "bolt", "dart", "javelin", "cannonball", "chinchompa",
			"bolt rack", "grapple"))
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

	// Tier 1 (Starter) through tier 5 (End); keeps curated tiers within the pre-existing
	// 0-1000 name-heuristic scale so an untiered item's fallback score stays comparable.
	private static final int GEAR_TIER_SCORE_STEP = 200;

	private static int gearScore(BankPreviewItem item)
	{
		OptionalInt tier = GearTierCatalog.INSTANCE.tierOf(
			item.getItemId(), item.getDisplayName());
		if (tier.isPresent())
		{
			return tier.getAsInt() * GEAR_TIER_SCORE_STEP;
		}

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

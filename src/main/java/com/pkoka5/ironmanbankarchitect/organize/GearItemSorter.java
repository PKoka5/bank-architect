package com.pkoka5.ironmanbankarchitect.organize;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsAny;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.normalized;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.*;
import java.util.List;





final class GearItemSorter
{
	private static final int STYLE_MELEE = 0;
	private static final int STYLE_RANGED = 1;
	private static final int STYLE_MAGIC = 2;
	private static final int STYLE_PRAYER = 3;
	private static final int STYLE_OTHER = 4;
	private static final int[] SETUP_STYLES = {STYLE_MELEE, STYLE_RANGED, STYLE_MAGIC, STYLE_PRAYER};
	private static final int[] SETUP_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	private static final int[] NAME_STYLE_GROUPS = {59, 61, 62, 60};
	private static final int[] NAME_STYLES = {STYLE_PRAYER, STYLE_RANGED, STYLE_MAGIC, STYLE_MELEE};
	private static final List<String> CATALOG_SLOTS = Arrays.asList(
		"head", "body", "legs", "cape", "neck", "shield", "hands", "feet", "weapon", "ammo", "ring");

	// Must match the physical bank and blueprint grid width.
	static final int GRID_COLUMNS = 8;
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
		{8, 9, 10, 8}
	};

	private GearItemSorter()
	{
	}

	/** Logical best-gear order; the preview builder applies full-grid vertical geometry. */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, GearStatsSource.NONE);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return plan(items, gearStats).allItems();
	}

	/** The deterministic loose order used around curated vertical columns. */
	static List<BankPreviewItem> dense(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return remainingSorted(items, new LinkedHashSet<>(), gearStats);
	}

	/**
	 * The list layout: each curated set reads as one run in slot order, helm
	 * to boots, strongest set first; everything outside a set follows in the
	 * dense order, weapons and loose pieces flowing like text.
	 */
	static List<BankPreviewItem> bySet(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		List<List<BankPreviewItem>> presentSets = presentSets(items);
		// Keep secondary families in style blocks, strongest family first within each block.
		presentSets.sort(Comparator.comparing((List<BankPreviewItem> set) ->
			!GearSetSemanticRuleSet.isCannonPart(set.get(0).getItemId()))
			.thenComparingInt(set -> familyStyle(set, gearStats))
			.thenComparingInt(set -> -set.stream().mapToInt(item -> scoreOf(item, gearStats)).max().orElse(0))
			.thenComparingInt(set -> set.get(0).getItemId()));

		List<BankPreviewItem> laidOut = new ArrayList<>(items.size());
		Set<Integer> used = new LinkedHashSet<>();
		for (List<BankPreviewItem> set : presentSets)
		{
			for (BankPreviewItem item : set)
			{
				if (used.add(item.getItemId()))
				{
					laidOut.add(item);
				}
			}
		}
		for (BankPreviewItem item : remainingSorted(items, used, gearStats))
		{
			if (used.add(item.getItemId()))
			{
				laidOut.add(item);
			}
		}
		return laidOut;
	}

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

		// Reviewed navigation roles decide first, then the placement score. A stronger loose item
		// still outranks a set piece. Only where these preferences tie does belonging to a family the player
		// actually owns win the cell, so a full set cannot be decapitated by an
		// equally tiered stranger that merely sorts earlier by name.
		Map<Integer, Integer> ownedFamilySizes = GearSetSemanticRuleSet.ownedFamilySizeByItemId(items);
		Comparator<BankPreviewItem> byPreference = Comparator
			.comparingInt((BankPreviewItem item) -> -GearSetSemanticRuleSet.frontlinePriority(item.getItemId()))
			.thenComparingInt(item -> -scoreOf(item, gearStats))
			.thenComparing(item -> -ownedFamilySizes.getOrDefault(item.getItemId(), 0))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId);
		for (List<BankPreviewItem> candidates : setCandidates.values())
		{
			candidates.sort(byPreference);
		}
		List<BankPreviewItem> rangedWeapons = setCandidates.get(STYLE_RANGED + ":9");
		if (rangedWeapons != null && GearSetSemanticRuleSet.isBowfa(rangedWeapons.get(0).getItemId()))
			for (int slot = 0; slot < 3; slot++)
			{
				List<BankPreviewItem> armour = setCandidates.get(STYLE_RANGED + ":" + slot);
				if (armour != null) armour.sort(Comparator
					.comparing((BankPreviewItem item) -> !GearSetSemanticRuleSet.isCrystalArmour(item.getItemId()))
					.thenComparing(byPreference));
			}
		List<BankPreviewItem> laidOut = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		// Selection is independent of geometry. The full-grid planner supplies
		// real fillers beside these columns and places secondary families vertically.
		for (int style : SETUP_STYLES)
		{
			for (int slot : SETUP_SLOTS)
			{
				String key = style + ":" + slot;
				List<BankPreviewItem> candidates = setCandidates.get(key);
				if (candidates != null && !candidates.isEmpty())
				{
					BankPreviewItem item = candidates.get(0);
					if (usedItemIds.add(item.getItemId()))
					{
						laidOut.add(item);
					}
				}
			}
		}

		return new GearLayout(laidOut, remainingSorted(items, usedItemIds, gearStats), 0);
	}

	static Map<Integer, Integer> setupTargets(GearLayout selection, int capacity, GearStatsSource gearStats)
	{
		Map<String, BankPreviewItem> best = new HashMap<>();
		for (BankPreviewItem item : selection.getSetupRows())
			best.put(styleRankOf(item, gearStats) + ":" + slotRankOf(item, gearStats), item);
		Map<Integer, Integer> targets = new LinkedHashMap<>();
		int start = 0;
		for (int[] row : SET_ROWS)
		{
			boolean present = false;
			for (int style : SETUP_STYLES)
			{
				BankPreviewItem item = best.get(style + ":" + row[style]);
				if (item == null) continue;
				present = true;
				if (start + style < capacity) targets.put(item.getItemId(), start + style);
			}
			if (present) start += GRID_COLUMNS;
		}
		return targets;
	}

	static boolean isAmmo(BankPreviewItem item, GearStatsSource gearStats)
	{
		return slotRankOf(item, gearStats) == 11;
	}

	static final class GearLayout
	{
		private final List<BankPreviewItem> setupRows;
		private final List<BankPreviewItem> tail;
		private final int alignedSize;

		private GearLayout(List<BankPreviewItem> setupRows, List<BankPreviewItem> tail, int alignedSize)
		{
			this.setupRows = setupRows;
			this.tail = tail;
			this.alignedSize = alignedSize;
		}

		int getAlignedSize()
		{
			return alignedSize;
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

	private static int familyStyle(List<BankPreviewItem> family, GearStatsSource gearStats)
	{
		int[] counts = new int[STYLE_OTHER + 1];
		boolean hasArmour = family.stream().anyMatch(item -> slotRankOf(item, gearStats) < 8);
		for (BankPreviewItem item : family)
			if (!hasArmour || slotRankOf(item, gearStats) < 8) counts[styleRankOf(item, gearStats)]++;
		int best = 0;
		for (int style = 1; style < counts.length; style++) if (counts[style] > counts[best]) best = style;
		return best;
	}

	private static List<List<BankPreviewItem>> presentSets(List<BankPreviewItem> items)
	{
		Map<Integer, BankPreviewItem> byId = new HashMap<>();
		for (BankPreviewItem item : items) byId.put(item.getItemId(), item);
		List<List<BankPreviewItem>> result = new ArrayList<>();
		for (List<Integer> family : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			List<BankPreviewItem> present = new ArrayList<>();
			for (int id : family) if (byId.containsKey(id)) present.add(byId.get(id));
			if (present.size() >= 2 || present.size() == 1
				&& GearSetSemanticRuleSet.isCannonPart(present.get(0).getItemId())) result.add(present);
		}
		return result;
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
			.thenComparingInt(item -> slotRankOf(item, gearStats) == 11 ? ammoFamilyRank(item) : 0)
			.thenComparingInt(item -> slotRankOf(item, gearStats) == 11 ? ammoTierRank(item) : 0)
			.thenComparing((BankPreviewItem item) -> -scoreOf(item, gearStats))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return remaining;
	}

	static int rank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		int slot = slotRank(name);
		return slot == 11 ? 1300 : slot * 100 + styleRank(name);
	}

	private static int rankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		int slot = slotRankOf(item, gearStats);
		return slot == 11 ? 1300 : slot * 100 + styleRankOf(item, gearStats);
	}

	static int ammoFamilyRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		if (containsAny(name, "arrow", "brutal")) return 0;
		if (containsAny(name, "bolt", "bolt rack")) return 10;
		if (name.contains("dart")) return 20;
		if (name.contains("javelin")) return 30;
		if (name.contains("cannonball")) return 40;
		if (name.contains("grapple")) return 50;
		return 60;
	}

	static int ammoTierRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName()).replace("runite", "rune");
		String[] tiers = ClassificationNames.group(71);
		for (int i = 0; i < tiers.length; i++)
		{
			if (name.contains(tiers[i])) return i;
		}
		return 100;
	}

	private static int slotRankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		if (!GearSetSemanticRuleSet.weaponRole(item.getItemId()).isEmpty()) return 8 + styleRankOf(item, gearStats);
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			return slotOf(item.getDisplayName(), stats.get());
		}

		String name = normalized(item.getDisplayName());
		int slot = CATALOG_SLOTS.indexOf(item.getSubcategory());
		if (slot < 0) return slotRank(name);
		if (slot == 8)
		{
			int style = styleRank(name);
			return style <= STYLE_MAGIC ? 8 + style : 8;
		}
		return slot < 8 ? slot : slot + 2;
	}

	private static int styleRankOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		if (GearSetSemanticRuleSet.isCrystalArmour(item.getItemId())) return STYLE_RANGED;
		String role = GearSetSemanticRuleSet.weaponRole(item.getItemId());
		if (!role.isEmpty()) return role.startsWith("weapon-ranged") ? STYLE_RANGED
			: role.startsWith("weapon-magic") ? STYLE_MAGIC : STYLE_MELEE;
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			return styleOf(item.getDisplayName(), stats.get()).ordinal();
		}
		return styleRank(normalized(item.getDisplayName()));
	}

	/** Casting role wins over a staff's melee attack bonuses; other gear keeps stat-based style. */
	static GearStyle styleOf(String displayName, GearStats stats)
	{
		String name = normalized(displayName);
		return stats.getSlot() == GearSlot.WEAPON
			&& (containsAny(name, ClassificationNames.group(57)) || name.contains("blue moon spear"))
			? GearStyle.MAGIC : stats.style();
	}

	static int slotOf(String displayName, GearStats stats)
	{
		GearStyle style = styleOf(displayName, stats);
		return stats.getSlot() == GearSlot.WEAPON ? style == GearStyle.PRAYER ? 8 : 8 + style.ordinal() : stats.slotRank();
	}

	private static int scoreOf(BankPreviewItem item, GearStatsSource gearStats)
	{
		int score = gearScore(item);
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		return stats.isPresent() ? score + stats.get().score(styleOf(item.getDisplayName(), stats.get())) : score;
	}

	static int score(BankPreviewItem item, GearStatsSource gearStats)
	{
		return scoreOf(item, gearStats);
	}

	private static int slotRank(String name)
	{
		for (int slot = 0; slot < 12; slot++)
			if (slot == 7 ? containsAny(name, "boots", "sandals", "shoes", "flippers", "manacles", "treads")
				: containsAny(name, ClassificationNames.group(48 + slot - (slot > 7 ? 1 : 0)))) return slot;
		return 12;
	}

	private static int styleRank(String name)
	{
		for (int i = 0; i < NAME_STYLE_GROUPS.length; i++)
			if (containsAny(name, ClassificationNames.group(NAME_STYLE_GROUPS[i]))) return NAME_STYLES[i];
		return STYLE_OTHER;
	}

	// Exact metadata and name fallbacks use the same five progression stages.
	private static final int GEAR_TIER_SCORE_STEP = 200;

	private static int gearScore(BankPreviewItem item)
	{
		OptionalInt tier = GearTierCatalog.INSTANCE.tierOf(
			item.getItemId(), item.getDisplayName());
		if (tier.isPresent())
		{
			return tier.getAsInt() * GEAR_TIER_SCORE_STEP;
		}

		String name = normalized(item.getDisplayName());
		int score = 0;
		for (int stage = 1; stage <= 5; stage++)
			if (containsAny(name, ClassificationNames.group(75 + stage))) score = stage * GEAR_TIER_SCORE_STEP;
		return score;
	}

}

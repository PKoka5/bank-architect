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
	private static final int[] SETUP_STYLES = {STYLE_MELEE, STYLE_RANGED, STYLE_MAGIC};
	private static final int[] SETUP_SLOTS = {0, 1, 2, 3, 7, 4, 8, 9, 10, 5, 6, 12};
	private static final int[] NAME_STYLE_GROUPS = {59, 61, 62, 60};
	private static final int[] NAME_STYLES = {STYLE_PRAYER, STYLE_RANGED, STYLE_MAGIC, STYLE_MELEE};
	private static final List<String> CATALOG_SLOTS = Arrays.asList(
		"head", "body", "legs", "cape", "neck", "shield", "hands", "feet", "weapon", "ammo", "ring");

	// Must match the physical bank and blueprint grid width.
	static final int GRID_COLUMNS = 8;

	private GearItemSorter()
	{
	}

	/** Logical best-gear order; the preview builder applies full-grid vertical geometry. */
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
			.thenComparingInt(set -> familyRoleRank(set, gearStats))
			.thenComparingInt(set -> -set.stream().mapToInt(item -> score(item, gearStats)).max().orElse(0))
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
			if (style <= STYLE_MAGIC && slot <= 12 && !GearSetSemanticRuleSet.roleOf(item.getItemId()).startsWith("support-"))
			{
				for (int candidateStyle : SETUP_STYLES)
					if (style == candidateStyle || (slot >= 3 && slot <= 7 || slot == 12)
						&& gearStats.statsFor(item.getItemId()).map(stats -> stats.supports(GearStyle.values()[candidateStyle])).orElse(false))
						setCandidates.computeIfAbsent(candidateStyle + ":" + slot, key -> new ArrayList<>()).add(item);
			}
		}

		// Reviewed navigation roles decide first, then the placement score. A stronger loose item
		// still outranks a set piece. Only where these preferences tie does belonging to a family the player
		// actually owns win the cell, so a full set cannot be decapitated by an
		// equally tiered stranger that merely sorts earlier by name.
		Map<Integer, Integer> ownedFamilySizes = GearSetSemanticRuleSet.ownedFamilySizeByItemId(items);
		for (Map.Entry<String,List<BankPreviewItem>> entry : setCandidates.entrySet())
		{
			GearStyle desired = GearStyle.values()[Integer.parseInt(entry.getKey().substring(0,1))];
			Comparator<BankPreviewItem> byPreference = Comparator
			.comparingInt((BankPreviewItem item) -> -GearSetSemanticRuleSet.frontlinePriority(item.getItemId()))
			.thenComparingInt(item -> slotRankOf(item, gearStats) == 12
				? -gearStats.statsFor(item.getItemId()).map(stats -> stats.power(desired)).orElse(0) : 0)
			.thenComparingInt(item -> -gearScore(item) - gearStats.statsFor(item.getItemId()).map(stats -> stats.score(desired)).orElse(0))
			.thenComparing(item -> -ownedFamilySizes.getOrDefault(item.getItemId(), 0))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId);
		entry.getValue().sort(byPreference);
		}
		List<BankPreviewItem> rangedWeapons = setCandidates.get(STYLE_RANGED + ":9");
		if (rangedWeapons != null && GearSetSemanticRuleSet.isBowfa(rangedWeapons.get(0).getItemId()))
			for (int slot = 0; slot < 3; slot++)
			{
				List<BankPreviewItem> armour = setCandidates.get(STYLE_RANGED + ":" + slot);
				if (armour != null) armour.sort(Comparator
					.comparing((BankPreviewItem item) -> !GearSetSemanticRuleSet.isCrystalArmour(item.getItemId())));
			}
		List<BankPreviewItem> laidOut = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		Map<Integer,Integer> columns = new HashMap<>();
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
					BankPreviewItem item = candidates.stream().filter(candidate -> !usedItemIds.contains(candidate.getItemId())).findFirst().orElse(null);
					if (item != null && usedItemIds.add(item.getItemId()))
					{
						laidOut.add(item); columns.put(item.getItemId(),style);
					}
				}
			}
		}

		return new GearLayout(laidOut, remainingSorted(items, usedItemIds, gearStats), columns);
	}

	static Map<Integer, Integer> setupTargets(GearLayout selection, int capacity)
	{
		Map<Integer,Integer> targets = new LinkedHashMap<>();
		int[] rows = new int[3];
		for (BankPreviewItem item : selection.getSetupRows())
		{
			Integer column = selection.columns.get(item.getItemId());
			int target = rows[column]++ * GRID_COLUMNS + column;
			if (target >= capacity) return Collections.emptyMap();
			targets.put(item.getItemId(),target);
		}
		return targets;
	}

	static Map<Integer, List<BankPreviewItem>> sections(List<BankPreviewItem> items, GearStatsSource stats)
	{
		Map<Integer, Integer> owner = new HashMap<>();
		for (List<BankPreviewItem> family : presentSets(items))
			for (BankPreviewItem item : family) owner.putIfAbsent(item.getItemId(), familyStyle(family, stats));
		Map<Integer, List<BankPreviewItem>> sections = new TreeMap<>();
		for (BankPreviewItem item : items)
			sections.computeIfAbsent(owner.getOrDefault(item.getItemId(), styleRankOf(item, stats)), key -> new ArrayList<>()).add(item);
		return sections;
	}

	static boolean isAmmo(BankPreviewItem item, GearStatsSource gearStats)
	{
		return slotRankOf(item, gearStats) == 11;
	}

	static final class GearLayout
	{
		private final List<BankPreviewItem> setupRows;
		private final List<BankPreviewItem> tail;
		private final Map<Integer,Integer> columns;

		private GearLayout(List<BankPreviewItem> setupRows, List<BankPreviewItem> tail, Map<Integer,Integer> columns)
		{
			this.setupRows = setupRows;
			this.tail = tail; this.columns = columns;
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

	private static int familyRoleRank(List<BankPreviewItem> family, GearStatsSource stats)
	{
		if (family.stream().anyMatch(item -> slotRankOf(item, stats) < 8)) return 0;
		String role = GearSetSemanticRuleSet.weaponRole(family.get(0).getItemId());
		List<String> roles = Arrays.asList(ClassificationNames.group(87));
		int rank = roles.indexOf(role);
		return rank < 0 ? 20 : 1 + rank;
	}

	private static List<List<BankPreviewItem>> presentSets(List<BankPreviewItem> items)
	{
		Map<Integer, BankPreviewItem> byId = new HashMap<>();
		for (BankPreviewItem item : items) byId.put(item.getItemId(), item);
		Set<Integer> used = new HashSet<>();
		List<List<BankPreviewItem>> result = new ArrayList<>();
		for (List<Integer> family : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			List<BankPreviewItem> present = new ArrayList<>();
			for (int id : family) if (byId.containsKey(id) && !used.contains(id)) present.add(byId.get(id));
			boolean weaponGroup = family.stream().allMatch(id -> !GearSetSemanticRuleSet.weaponRole(id).isEmpty());
			if (present.size() >= 2 || present.size() == 1 && (weaponGroup || GearSetSemanticRuleSet.isCannonPart(present.get(0).getItemId())))
			{
				result.add(present);
				for (BankPreviewItem item : present) used.add(item.getItemId());
			}
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
			.comparingInt((BankPreviewItem item) -> slotRankOf(item, gearStats) == 11 ? 5 : styleRankOf(item, gearStats))
			.thenComparingInt(item -> rankOf(item, gearStats))
			.thenComparingInt(item -> slotRankOf(item, gearStats) == 11 ? ammoFamilyRank(item) : 0)
			.thenComparingInt(item -> slotRankOf(item, gearStats) == 11 ? ammoTierRank(item) : 0)
			.thenComparing((BankPreviewItem item) -> -score(item, gearStats))
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

	static int slotRankOf(BankPreviewItem item, GearStatsSource gearStats)
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
		if (GearSetSemanticRuleSet.isMeleeTankArmour(item.getItemId())) return STYLE_MELEE;
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

	static int score(BankPreviewItem item, GearStatsSource gearStats)
	{
		int score = gearScore(item);
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		return stats.isPresent() ? score + stats.get().score(styleOf(item.getDisplayName(), stats.get())) : score;
	}

	private static int slotRank(String name)
	{
		for (int slot = 0; slot < 12; slot++)
			if (slot == 7 ? containsAny(name, "boots", "sandals", "shoes", "flippers", "manacles", "treads")
				: containsAny(name, ClassificationNames.group(48 + slot - (slot > 7 ? 1 : 0)))) return slot;
		return name.endsWith(" ring") || name.startsWith("ring ") || name.contains(" ring (") ? 12 : 13;
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

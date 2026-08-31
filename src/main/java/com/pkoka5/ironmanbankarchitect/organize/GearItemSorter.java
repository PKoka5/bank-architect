package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Plans combat gear as usable loadouts instead of rows of matching equipment slots. */
final class GearItemSorter
{
	static final int GRID_COLUMNS = CombatGearGridLayout.GRID_COLUMNS;

	private GearItemSorter()
	{
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, GearStatsSource.NONE);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return layout(items, gearStats, true);
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items, GearStatsSource gearStats,
		boolean fillLoadoutRows)
	{
		return plan(items, gearStats, fillLoadoutRows).allItems();
	}

	/** Dense now means loadout-dense: no blanks and no unrelated row filler. */
	static List<BankPreviewItem> dense(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return layout(items, gearStats, false);
	}

	static GearLayout plan(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		return plan(items, gearStats, true);
	}

	private static GearLayout plan(List<BankPreviewItem> items, GearStatsSource gearStats,
		boolean fillLoadoutRows)
	{
		List<BankPreviewItem> ready = new ArrayList<>();
		List<BankPreviewItem> placeholders = new ArrayList<>();
		List<BankPreviewItem> maintenance = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			if (item.isPlaceholder() && !CombatGearRanking.unusable(item))
			{
				placeholders.add(item);
			}
			else
			{
				(CombatGearRanking.unusable(item) ? maintenance : ready).add(item);
			}
		}

		CombatGearIndex ownedGear = new CombatGearIndex(ready, gearStats);
		List<BankPreviewItem> layoutItems = new ArrayList<>(ready);
		layoutItems.addAll(placeholders);
		CombatGearIndex layoutGear = new CombatGearIndex(layoutItems, gearStats);
		CombatLoadoutResolver.Relationships relationships =
			CombatLoadoutResolver.resolve(ownedGear);
		List<CombatLoadoutResolver.Loadout> candidates =
			new ArrayList<>(relationships.loadouts());
		List<CombatLoadoutResolver.Family> families =
			new ArrayList<>(CombatLoadoutResolver.families(layoutGear));
		families.sort(Comparator
			.comparingInt(CombatLoadoutResolver.Family::matchedRoles).reversed()
			.thenComparing(Comparator.comparingInt(
				(CombatLoadoutResolver.Family family) -> familyStrength(family, layoutGear)).reversed())
			.thenComparing(CombatLoadoutResolver.Family::key));
		Set<Integer> familyItemIds = new LinkedHashSet<>();
		for (CombatLoadoutResolver.Family family : families)
		{
			familyItemIds.addAll(family.itemIds());
		}

		Set<Integer> candidateItemIds = new LinkedHashSet<>();
		for (CombatLoadoutResolver.Loadout candidate : candidates)
		{
			candidateItemIds.addAll(candidate.itemIds());
		}
		candidates.sort(Comparator
			.comparingInt((CombatLoadoutResolver.Loadout loadout) ->
				-loadoutValue(loadout, ownedGear, candidateItemIds))
			.thenComparing(CombatLoadoutResolver.Loadout::key));

		List<CombatLoadoutResolver.Loadout> exactLoadouts = new ArrayList<>();
		Set<Integer> reservedRequiredItemIds = new LinkedHashSet<>();
		for (CombatLoadoutResolver.Loadout candidate : candidates)
		{
			if (Collections.disjoint(candidate.requiredItemIds(), reservedRequiredItemIds))
			{
				exactLoadouts.add(candidate);
				reservedRequiredItemIds.addAll(candidate.requiredItemIds());
			}
		}

		Set<Integer> exactItemIds = new LinkedHashSet<>();
		for (CombatLoadoutResolver.Loadout loadout : exactLoadouts)
		{
			exactItemIds.addAll(loadout.itemIds());
		}

		Set<Integer> reservedFamilyItemIds = new LinkedHashSet<>(exactItemIds);
		reservedFamilyItemIds.addAll(familyItemIds);
		Set<Integer> protectedGenericCoreItemIds = primaryGenericCoreItemIds(
			ownedGear, reservedFamilyItemIds);
		Set<Integer> complementExclusions = new LinkedHashSet<>(exactItemIds);
		complementExclusions.addAll(familyItemIds);
		complementExclusions.addAll(protectedGenericCoreItemIds);

		List<CombatGearGridLayout.Block> loadoutBlocks = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		Set<String> consumedFamilyKeys = new LinkedHashSet<>();
		for (CombatLoadoutResolver.Loadout loadout : exactLoadouts)
		{
			List<BankPreviewItem> blockItems = new ArrayList<>();
			int emittedCells = 0;
			for (BankPreviewItem item : loadout.items())
			{
				if (usedItemIds.add(item.getItemId()))
				{
					blockItems.add(item);
					emittedCells += item.physicalBankSlotCount();
				}
			}
			for (CombatLoadoutResolver.Family family : families)
			{
				if (consumedFamilyKeys.contains(family.key())
					|| Collections.disjoint(loadout.requiredItemIds(), family.itemIds()))
				{
					continue;
				}
				for (BankPreviewItem familyItem : family.items())
				{
					if (usedItemIds.add(familyItem.getItemId()))
					{
						blockItems.add(familyItem);
						emittedCells += familyItem.physicalBankSlotCount();
					}
				}
				consumedFamilyKeys.add(family.key());
			}

			int complementCount = fillLoadoutRows ? GRID_COLUMNS - emittedCells : 0;
			List<BankPreviewItem> complements = complementaryGear(ready, loadout.style(),
				complementCount, usedItemIds, complementExclusions, ownedGear);
			for (BankPreviewItem complement : complements)
			{
				usedItemIds.add(complement.getItemId());
				blockItems.add(complement);
			}
			loadoutBlocks.add(new CombatGearGridLayout.Block(loadout.key(), loadout.style(), blockItems, layoutGear,
				loadout.itemIds(), CombatGearUtilityCatalog.INSTANCE.loadoutScore(loadout.key())));
		}

		for (CombatLoadoutResolver.Family family : families)
		{
			if (consumedFamilyKeys.contains(family.key()))
			{
				continue;
			}
			List<BankPreviewItem> familyItems = new ArrayList<>();
			for (BankPreviewItem familyItem : family.items())
			{
				if (usedItemIds.add(familyItem.getItemId()))
				{
					familyItems.add(familyItem);
				}
			}
			if (family.roleCount(familyItems) >= family.minimumRoles())
			{
				loadoutBlocks.add(new CombatGearGridLayout.Block(
					"family-" + family.key(), family.style(), familyItems, layoutGear));
			}
			else
			{
				for (BankPreviewItem familyItem : familyItems)
				{
					usedItemIds.remove(familyItem.getItemId());
				}
			}
		}

		// Independently useful mechanic gear forms coherent style blocks, but the
		// blocks still compete with exact sets and progression loadouts in the
		// shared strength sort below. This is grouping, not an absolute top tier.
		for (GearStyle style : CombatGearRanking.LOADOUT_STYLES)
		{
			List<BankPreviewItem> utilityItems = remainingUtilityItems(
				ownedGear, style, usedItemIds);
			int sequence = 0;
			while (!utilityItems.isEmpty())
			{
				List<BankPreviewItem> row = practicalLoadout(utilityItems, ownedGear);
				for (BankPreviewItem item : row)
				{
					usedItemIds.add(item.getItemId());
					utilityItems.remove(item);
				}
				loadoutBlocks.add(new CombatGearGridLayout.Block(
					"utility-" + style + "-" + sequence++, style, row, layoutGear));
			}
		}

		for (GearStyle style : CombatGearRanking.LOADOUT_STYLES)
		{
			List<BankPreviewItem> styleItems = remainingStyleItems(ownedGear, style, usedItemIds);
			int sequence = 0;
			while (!styleItems.isEmpty())
			{
				List<BankPreviewItem> row = practicalLoadout(styleItems, ownedGear);
				for (BankPreviewItem item : row)
				{
					usedItemIds.add(item.getItemId());
					styleItems.remove(item);
				}
				loadoutBlocks.add(new CombatGearGridLayout.Block(
					"generic-" + style + "-" + sequence++, style, row, layoutGear));
			}
		}

		for (BankPreviewItem item : ready)
		{
			if (!usedItemIds.contains(item.getItemId())
				&& ownedGear.slot(item) != 10
				&& ownedGear.utilityScore(item) < 0)
			{
				usedItemIds.add(item.getItemId());
				loadoutBlocks.add(new CombatGearGridLayout.Block("situational-" + item.getItemId(),
					ownedGear.style(item), Collections.singletonList(item), layoutGear));
			}
		}

		loadoutBlocks.sort(Comparator
			.comparingInt((CombatGearGridLayout.Block block) -> -block.strength())
			.thenComparing(CombatGearGridLayout.Block::style)
			.thenComparing(CombatGearGridLayout.Block::key));
		List<BankPreviewItem> planned = new ArrayList<>();
		planned.addAll(CombatGearGridLayout.layout(loadoutBlocks, layoutGear, fillLoadoutRows));

		List<BankPreviewItem> ammunition = new ArrayList<>();
		for (BankPreviewItem item : ready)
		{
			if (!usedItemIds.contains(item.getItemId()) && ownedGear.slot(item) == 10)
			{
				ammunition.add(item);
			}
		}
		ammunition.sort(Comparator
			.comparingInt((BankPreviewItem item) -> CombatGearRanking.ammunitionFamily(item))
			.thenComparingInt(CombatGearRanking::ammunitionTier)
			.thenComparing((BankPreviewItem item) -> -ownedGear.score(item))
			.thenComparing(item -> CombatGearRanking.normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		planned.addAll(ammunition);
		for (BankPreviewItem placeholder : placeholders)
		{
			if (!usedItemIds.contains(placeholder.getItemId()))
			{
				maintenance.add(placeholder);
			}
		}

		maintenance.sort(Comparator
			.comparingInt((BankPreviewItem item) -> CombatGearRanking.slot(item, gearStats))
			.thenComparing((BankPreviewItem item) -> -CombatGearRanking.score(item, gearStats))
			.thenComparing(item -> CombatGearRanking.normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return new GearLayout(planned, maintenance);
	}

	private static List<BankPreviewItem> complementaryGear(List<BankPreviewItem> items, GearStyle style,
		int cellBudget, Set<Integer> usedItemIds, Set<Integer> excludedItemIds,
		CombatGearIndex gear)
	{
		if (cellBudget <= 0 || style == GearStyle.OTHER)
		{
			return Collections.emptyList();
		}

		List<BankPreviewItem> candidates = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			if (!usedItemIds.contains(item.getItemId())
				&& !excludedItemIds.contains(item.getItemId())
				&& gear.slot(item) != 10
				&& gear.utilityScore(item) >= 0
				&& gear.style(item) == style)
			{
				candidates.add(item);
			}
		}
		candidates.sort(Comparator
			.comparingInt((BankPreviewItem item) -> complementSlot(gear.slot(item)))
			.thenComparing((BankPreviewItem item) -> -gear.score(item))
			.thenComparing(item -> CombatGearRanking.normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		List<BankPreviewItem> selected = new ArrayList<>();
		int remainingCells = cellBudget;
		for (BankPreviewItem candidate : candidates)
		{
			int cells = candidate.physicalBankSlotCount();
			if (cells <= remainingCells)
			{
				selected.add(candidate);
				remainingCells -= cells;
			}
		}
		return selected;
	}

	private static Set<Integer> primaryGenericCoreItemIds(CombatGearIndex gear,
		Set<Integer> exactItemIds)
	{
		Set<Integer> protectedIds = new LinkedHashSet<>();
		for (GearStyle style : CombatGearRanking.LOADOUT_STYLES)
		{
			for (int slot = 0; slot <= 2; slot++)
			{
				BankPreviewItem best = null;
				for (BankPreviewItem item : gear.items())
				{
					if (exactItemIds.contains(item.getItemId())
						|| gear.style(item) != style
						|| gear.slot(item) != slot)
					{
						continue;
					}
					if (best == null || gear.score(item) > gear.score(best))
					{
						best = item;
					}
				}
				if (best != null)
				{
					protectedIds.add(best.getItemId());
				}
			}
		}
		return protectedIds;
	}

	private static List<BankPreviewItem> remainingStyleItems(CombatGearIndex gear, GearStyle style,
		Set<Integer> usedItemIds)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		for (BankPreviewItem item : gear.items())
		{
			if (!usedItemIds.contains(item.getItemId())
				&& gear.slot(item) != 10
				&& gear.utilityScore(item) >= 0
				&& gear.style(item) == style)
			{
				result.add(item);
			}
		}
		return result;
	}

	private static List<BankPreviewItem> remainingUtilityItems(CombatGearIndex gear, GearStyle style,
		Set<Integer> usedItemIds)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		for (BankPreviewItem item : gear.items())
		{
			if (!usedItemIds.contains(item.getItemId())
				&& gear.slot(item) != 10
				&& gear.style(item) == style
				&& gear.utilityScore(item) > 0)
			{
				result.add(item);
			}
		}
		return result;
	}

	private static List<BankPreviewItem> practicalLoadout(List<BankPreviewItem> candidates,
		CombatGearIndex gear)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(candidates);
		sorted.sort(Comparator
			.comparingInt(gear::slot)
			.thenComparing((BankPreviewItem item) -> -gear.score(item))
			.thenComparing(item -> CombatGearRanking.normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));

		List<BankPreviewItem> selected = new ArrayList<>(GRID_COLUMNS);
		Set<Integer> selectedSlots = new LinkedHashSet<>();
		int selectedCells = 0;
		for (BankPreviewItem item : sorted)
		{
			int slot = gear.slot(item);
			int cells = item.physicalBankSlotCount();
			if (!selectedSlots.contains(slot) && selectedCells + cells <= GRID_COLUMNS)
			{
				selectedSlots.add(slot);
				selected.add(item);
				selectedCells += cells;
				if (selectedCells == GRID_COLUMNS)
				{
					return selected;
				}
			}
		}

		List<BankPreviewItem> sidegrades = new ArrayList<>(sorted);
		sidegrades.removeAll(selected);
		sidegrades.sort(Comparator
			.comparingInt((BankPreviewItem item) -> -gear.score(item))
			.thenComparingInt(gear::slot)
			.thenComparing(item -> CombatGearRanking.normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		for (BankPreviewItem item : sidegrades)
		{
			int cells = item.physicalBankSlotCount();
			if (selectedCells + cells <= GRID_COLUMNS)
			{
				selected.add(item);
				selectedCells += cells;
			}
			if (selectedCells == GRID_COLUMNS)
			{
				break;
			}
		}
		if (selected.isEmpty() && !sorted.isEmpty())
		{
			selected.add(sorted.get(0));
		}
		return selected;
	}

	private static int loadoutValue(CombatLoadoutResolver.Loadout loadout,
		CombatGearIndex gear, Set<Integer> candidateItemIds)
	{
		int total = 0;
		int count = 0;
		for (BankPreviewItem item : loadout.items())
		{
			total += gear.activeScore(item);
			count++;
		}

		List<BankPreviewItem> complements = complementaryGear(gear.items(), loadout.style(),
			GRID_COLUMNS - physicalCellCount(loadout.items()), Collections.emptySet(),
			candidateItemIds, gear);
		for (BankPreviewItem complement : complements)
		{
			total += gear.score(complement);
			count++;
		}
		return count == 0 ? 0 : total / count
			+ CombatGearUtilityCatalog.INSTANCE.loadoutScore(loadout.key());
	}

	private static int physicalCellCount(List<BankPreviewItem> items)
	{
		int cells = 0;
		for (BankPreviewItem item : items)
		{
			cells += item.physicalBankSlotCount();
		}
		return cells;
	}

	private static int familyStrength(CombatLoadoutResolver.Family family,
		CombatGearIndex gear)
	{
		int total = 0;
		int peak = 0;
		for (BankPreviewItem item : family.items())
		{
			int score = gear.score(item);
			total += score;
			peak = Math.max(peak, score);
		}
		return peak * 100 + (family.items().isEmpty() ? 0 : total / family.items().size());
	}

	private static int complementSlot(int slot)
	{
		if (slot >= 4 && slot <= 7) return slot - 4;
		if (slot == 9) return 4;
		if (slot == 8) return 5;
		if (slot == 10) return 6;
		return 7 + slot;
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

	static int rank(BankPreviewItem item)
	{
		return CombatGearRanking.legacyRank(item);
	}

	static int score(BankPreviewItem item, GearStatsSource gearStats)
	{
		return CombatGearRanking.score(item, gearStats);
	}

}

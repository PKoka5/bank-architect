package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Places ranked combat blocks densely or as aligned armour columns. */
final class CombatGearGridLayout
{
	static final int GRID_COLUMNS = 8;
	private static final int[] ALIGNED_CORE_SLOTS = {0, 1, 2, 3};

	private CombatGearGridLayout()
	{
	}

	static List<BankPreviewItem> layout(List<Block> blocks, OwnedCombatGearIndex gear,
		boolean alignLoadouts)
	{
		return alignLoadouts ? alignedItems(blocks, gear) : denseItems(blocks);
	}

	private static List<BankPreviewItem> alignedItems(List<Block> blocks,
		OwnedCombatGearIndex gear)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		int start = 0;
		while (start < blocks.size())
		{
			if (!blocks.get(start).isArmourColumn(gear))
			{
				result.addAll(blocks.get(start).items);
				start++;
				continue;
			}

			List<Block> batch = new ArrayList<>();
			int bandCells = 0;
			int armourColumns = 0;
			int physicalCells = 0;
			int end = start;
			while (end < blocks.size())
			{
				Block block = blocks.get(end);
				if (!block.isArmourColumn(gear))
				{
					break;
				}
				int blockBand = block.coreBandWidth(gear);
				if (blockBand > 0 && bandCells + blockBand > GRID_COLUMNS)
				{
					break;
				}
				batch.add(block);
				bandCells += blockBand;
				armourColumns += blockBand > 0 ? 1 : 0;
				physicalCells += physicalCellCount(block.items);
				end++;
				if (armourColumns >= 2 && physicalCells >= GRID_COLUMNS * ALIGNED_CORE_SLOTS.length)
				{
					break;
				}
			}

			List<BankPreviewItem> aligned = armourColumns >= 2
				&& physicalCells >= GRID_COLUMNS * ALIGNED_CORE_SLOTS.length
				? alignedBatch(batch, gear) : null;
			if (aligned == null)
			{
				result.addAll(blocks.get(start).items);
				start++;
			}
			else
			{
				result.addAll(aligned);
				start = end;
			}
		}
		return result;
	}

	private static List<BankPreviewItem> denseItems(List<Block> blocks)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		for (Block block : blocks)
		{
			result.addAll(block.items);
		}
		return result;
	}

	private static List<BankPreviewItem> alignedBatch(List<Block> blocks,
		OwnedCombatGearIndex gear)
	{
		List<BankPreviewItem> allItems = new ArrayList<>();
		Set<Integer> coreItemIds = new LinkedHashSet<>();
		for (Block block : blocks)
		{
			allItems.addAll(block.items);
			if (!block.isArmourColumn(gear))
			{
				continue;
			}
			for (int slot : ALIGNED_CORE_SLOTS)
			{
				BankPreviewItem core = block.coreItem(slot, gear);
				if (core != null)
				{
					coreItemIds.add(core.getItemId());
				}
			}
		}

		List<BankPreviewItem> fillers = new ArrayList<>();
		for (BankPreviewItem item : allItems)
		{
			if (!coreItemIds.contains(item.getItemId()))
			{
				fillers.add(item);
			}
		}

		List<BankPreviewItem> aligned = new ArrayList<>();
		Set<Integer> usedItemIds = new LinkedHashSet<>();
		for (int slot : ALIGNED_CORE_SLOTS)
		{
			int rowCells = 0;
			for (Block block : blocks)
			{
				if (!block.isArmourColumn(gear))
				{
					continue;
				}
				int bandWidth = block.coreBandWidth(gear);
				BankPreviewItem core = block.coreItem(slot, gear);
				int occupied = 0;
				if (core != null)
				{
					aligned.add(core);
					usedItemIds.add(core.getItemId());
					occupied = core.physicalBankSlotCount();
				}

				List<BankPreviewItem> bandFillers = fillerItems(
					fillers, bandWidth - occupied, block.itemIds());
				if (bandFillers == null)
				{
					return null;
				}
				for (BankPreviewItem filler : bandFillers)
				{
					aligned.add(filler);
					fillers.remove(filler);
					usedItemIds.add(filler.getItemId());
				}
				rowCells += bandWidth;
			}

			List<BankPreviewItem> rowFillers = fillerItems(
				fillers, GRID_COLUMNS - rowCells, Collections.emptySet());
			if (rowFillers == null)
			{
				return null;
			}
			for (BankPreviewItem filler : rowFillers)
			{
				aligned.add(filler);
				fillers.remove(filler);
				usedItemIds.add(filler.getItemId());
			}
		}

		for (BankPreviewItem item : allItems)
		{
			if (usedItemIds.add(item.getItemId()))
			{
				aligned.add(item);
			}
		}
		return aligned;
	}

	private static List<BankPreviewItem> fillerItems(List<BankPreviewItem> candidates,
		int requiredCells, Set<Integer> preferredItemIds)
	{
		if (requiredCells == 0)
		{
			return Collections.emptyList();
		}
		if (requiredCells < 0)
		{
			return null;
		}

		List<BankPreviewItem> ordered = new ArrayList<>(candidates);
		ordered.sort(Comparator
			.comparingInt((BankPreviewItem item) -> preferredItemIds.contains(item.getItemId()) ? 0 : 1)
			.thenComparingInt(BankPreviewItem::physicalBankSlotCount)
			.thenComparingInt(BankPreviewItem::getItemId));
		List<List<BankPreviewItem>> byCells = new ArrayList<>(requiredCells + 1);
		for (int cells = 0; cells <= requiredCells; cells++)
		{
			byCells.add(null);
		}
		byCells.set(0, new ArrayList<>());
		for (BankPreviewItem candidate : ordered)
		{
			int width = candidate.physicalBankSlotCount();
			for (int cells = requiredCells; cells >= width; cells--)
			{
				if (byCells.get(cells) == null && byCells.get(cells - width) != null)
				{
					List<BankPreviewItem> combination = new ArrayList<>(byCells.get(cells - width));
					combination.add(candidate);
					byCells.set(cells, combination);
				}
			}
		}
		return byCells.get(requiredCells);
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

	static final class Block
	{
		private final String key;
		private final GearStyle style;
		private final List<BankPreviewItem> items;
		private final int strength;

		Block(String key, GearStyle style, List<BankPreviewItem> items, OwnedCombatGearIndex gear)
		{
			this(key, style, items, gear, Collections.emptySet(), 0);
		}

		Block(String key, GearStyle style, List<BankPreviewItem> items, OwnedCombatGearIndex gear,
			Set<Integer> activeItemIds, int loadoutUtility)
		{
			this.key = key;
			this.style = style;
			this.items = new ArrayList<>(items);
			int total = 0;
			int peakScore = 0;
			for (BankPreviewItem item : items)
			{
				int score = activeItemIds.contains(item.getItemId())
					? gear.activeScore(item) : gear.score(item);
				total += score;
				peakScore = Math.max(peakScore, score);
			}
			int average = items.isEmpty() ? 0 : total / items.size();
			this.strength = (peakScore + loadoutUtility) * 100 + average;
		}

		int strength()
		{
			return strength;
		}

		GearStyle style()
		{
			return style;
		}

		String key()
		{
			return key;
		}

		private boolean isArmourColumn(OwnedCombatGearIndex gear)
		{
			int armourSlots = 0;
			for (int slot = 0; slot <= 2; slot++)
			{
				if (coreItem(slot, gear) != null)
				{
					armourSlots++;
				}
			}
			return armourSlots >= 2;
		}

		private int coreBandWidth(OwnedCombatGearIndex gear)
		{
			int width = 1;
			for (int slot : ALIGNED_CORE_SLOTS)
			{
				BankPreviewItem item = coreItem(slot, gear);
				if (item != null)
				{
					width = Math.max(width, item.physicalBankSlotCount());
				}
			}
			return width;
		}

		private BankPreviewItem coreItem(int slot, OwnedCombatGearIndex gear)
		{
			for (BankPreviewItem item : items)
			{
				if (gear.slot(item) == slot)
				{
					return item;
				}
			}
			return null;
		}

		private Set<Integer> itemIds()
		{
			Set<Integer> result = new LinkedHashSet<>();
			for (BankPreviewItem item : items)
			{
				result.add(item.getItemId());
			}
			return result;
		}
	}
}

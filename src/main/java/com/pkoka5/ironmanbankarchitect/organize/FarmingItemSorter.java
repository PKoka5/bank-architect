package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Packs exact canonical Farming seed families into dense eight-column runs. A family never wraps
 * when real non-family Farming items can finish the current row; no blank or phantom entries are
 * introduced. Herb seeds already claimed by a Herblore recipe are absent from this input.
 */
final class FarmingItemSorter
{
	private static final int GRID_COLUMNS = 8;
	private static final OrderedItemFamilies FARMING_FAMILIES = new OrderedItemFamilies(
		FarmingItemSorter.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/farming-layout-families.tsv"), 0);

	private FarmingItemSorter()
	{
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> sortedItems, int usedColumns)
	{
		Map<Integer, BankPreviewItem> byId = new LinkedHashMap<>();
		for (BankPreviewItem item : sortedItems)
		{
			byId.put(item.getItemId(), item);
		}

		List<List<BankPreviewItem>> runs = new ArrayList<>();
		for (List<Integer> family : FARMING_FAMILIES.entries().values())
		{
			List<BankPreviewItem> present = new ArrayList<>();
			for (Integer itemId : family)
			{
				BankPreviewItem item = byId.remove(itemId);
				if (item != null)
				{
					present.add(item);
				}
			}
			for (int start = 0; start < present.size(); start += GRID_COLUMNS)
			{
				runs.add(new ArrayList<>(present.subList(start,
					Math.min(start + GRID_COLUMNS, present.size()))));
			}
		}

		List<BankPreviewItem> fillers = new ArrayList<>();
		for (BankPreviewItem item : sortedItems)
		{
			if (byId.remove(item.getItemId()) != null)
			{
				fillers.add(item);
			}
		}

		List<BankPreviewItem> result = new ArrayList<>(sortedItems.size());
		int column = Math.floorMod(usedColumns, GRID_COLUMNS);
		for (List<BankPreviewItem> run : runs)
		{
			if (column != 0 && column + run.size() > GRID_COLUMNS)
			{
				while (column != 0 && !fillers.isEmpty())
				{
					result.add(fillers.remove(0));
					column = (column + 1) % GRID_COLUMNS;
				}
			}
			result.addAll(run);
			column = (column + run.size()) % GRID_COLUMNS;
		}
		result.addAll(fillers);
		return result;
	}

	/**
	 * The family runs in their curated order with the leftovers behind them,
	 * and no filler moved forward to keep a run at a row edge.
	 */
	static List<BankPreviewItem> sequential(List<BankPreviewItem> sortedItems)
	{
		Map<Integer, BankPreviewItem> byId = new LinkedHashMap<>();
		for (BankPreviewItem item : sortedItems)
		{
			byId.put(item.getItemId(), item);
		}

		List<BankPreviewItem> result = new ArrayList<>(sortedItems.size());
		for (List<Integer> family : FARMING_FAMILIES.entries().values())
		{
			for (Integer itemId : family)
			{
				BankPreviewItem item = byId.remove(itemId);
				if (item != null)
				{
					result.add(item);
				}
			}
		}
		for (BankPreviewItem item : sortedItems)
		{
			if (byId.remove(item.getItemId()) != null)
			{
				result.add(item);
			}
		}
		return result;
	}

}

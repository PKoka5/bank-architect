package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private static final List<List<Integer>> FARMING_FAMILIES = Collections.unmodifiableList(Arrays.asList(
		ids(5318, 5319, 5324, 5322, 5320, 5323, 5321, 22879),
		ids(5096, 5097, 5098, 5099, 5100, 22887),
		ids(5291, 5292, 5293, 5294, 5295, 5296, 5297, 5298, 5299, 5300, 5301,
			5302, 5303, 5304),
		ids(5305, 5307, 5308, 5306, 5309, 5310, 5311),
		ids(5101, 5102, 5103, 5104, 5105, 5106),
		ids(5280, 22873, 5281, 5282, 13657),
		ids(5312, 5313, 5314, 5315, 5316, 21486, 21488, 22869, 22871,
			31547, 31549, 31551),
		ids(5283, 5284, 5285, 5286, 5287, 5288, 5289, 5290, 22877),
		ids(5317, 22875, 22881, 22883, 22885, 31541, 31543, 31545, 21490),
		ids(6032, 6034, 21483),
		ids(5354, 5356, 6036)));

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
		for (List<Integer> family : FARMING_FAMILIES)
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

	private static List<Integer> ids(Integer... itemIds)
	{
		return Collections.unmodifiableList(Arrays.asList(itemIds));
	}
}

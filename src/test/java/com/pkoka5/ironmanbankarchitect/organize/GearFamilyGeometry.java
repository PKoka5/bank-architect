package com.pkoka5.ironmanbankarchitect.organize;
import java.util.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
final class GearFamilyGeometry
{
	/** Accept an ordered dense run, a vertical column, or an exact column-major rectangle. */
	public static boolean keepsFamiliesTogether(List<BankPreviewItem> items, int physicalStart)
	{
		Map<Integer, Integer> positions = new HashMap<>();
		for (int i = 0; i < items.size(); i++) positions.put(items.get(i).getItemId(), physicalStart + i);
		for (List<Integer> set : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			List<Integer> owned = new ArrayList<>();
			for (int id : set) if (positions.containsKey(id)) owned.add(positions.get(id));
			if (owned.size() < 2) continue;
			int first = owned.get(0), minRow = first / 8, maxRow = minRow;
			int minCol = first % 8, maxCol = minCol;
			boolean run = true;
			for (int i = 0; i < owned.size(); i++)
			{
				int target = owned.get(i);
				run &= target == first + i;
				minRow = Math.min(minRow, target / 8); maxRow = Math.max(maxRow, target / 8);
				minCol = Math.min(minCol, target % 8); maxCol = Math.max(maxCol, target % 8);
			}
			if (run) continue;
			int height = maxRow - minRow + 1;
			if (height * (maxCol - minCol + 1) != owned.size()) return false;
			boolean columnMajor = true, rowMajor = true;
			int width = maxCol - minCol + 1;
			for (int i = 0; i < owned.size(); i++)
			{
				columnMajor &= owned.get(i) == minRow * 8 + minCol + i % height * 8 + i / height;
				rowMajor &= owned.get(i) == minRow * 8 + minCol + i / width * 8 + i % width;
			}
			if (!columnMajor && !rowMajor) return false;
		}
		return true;
	}

}

package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.CategoryPalette;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Item ID to blueprint destination lookup, built once per analysis.
 *
 * <p>Lets an overlay answer "where is this item going?" for a live bank slot
 * without walking the whole preview per frame. Categories keep the order the
 * player arranged them in, which is also the legend order. The colour comes
 * from the destination itself, so reordering tabs does not recolour them.</p>
 */
final class PlannedCategoryIndex
{
	private final List<String> categoryNames;
	private final List<Integer> paletteIndexes;
	private final Map<Integer, Integer> categoryIndexByItemId;

	private PlannedCategoryIndex(List<String> categoryNames, List<Integer> paletteIndexes,
		Map<Integer, Integer> categoryIndexByItemId)
	{
		this.categoryNames = Collections.unmodifiableList(categoryNames);
		this.paletteIndexes = Collections.unmodifiableList(paletteIndexes);
		this.categoryIndexByItemId = categoryIndexByItemId;
	}

	static PlannedCategoryIndex from(BankOrganizationPreview preview)
	{
		Objects.requireNonNull(preview, "preview");

		List<String> names = new ArrayList<>();
		List<Integer> palette = new ArrayList<>();
		Map<Integer, Integer> indexByItemId = new HashMap<>();
		List<BankCategoryPreview> categories = preview.getCategories();
		for (int index = 0; index < categories.size(); index++)
		{
			BankCategoryPreview category = categories.get(index);
			names.add(category.getCategory().getName());
			palette.add(CategoryPalette.paletteIndex(category.getCategory().getKey(), index));
			for (BankPreviewItem item : category.getItems())
			{
				if (item.getItemId() > 0)
				{
					// A placeholder and its real item share an ID here, so the
					// first entry already carries the right destination.
					indexByItemId.putIfAbsent(item.getItemId(), index);
				}
			}
		}

		return new PlannedCategoryIndex(names, palette, indexByItemId);
	}

	/** Preset-order index of the destination, or -1 when the item is not planned. */
	int categoryIndexFor(int itemId)
	{
		Integer index = categoryIndexByItemId.get(itemId);
		return index == null ? -1 : index;
	}

	/** Palette slot of the destination shown at this position. */
	int paletteIndex(int categoryIndex)
	{
		return categoryIndex < 0 || categoryIndex >= paletteIndexes.size()
			? 0 : paletteIndexes.get(categoryIndex);
	}

	String categoryName(int categoryIndex)
	{
		return categoryIndex < 0 || categoryIndex >= categoryNames.size()
			? "" : categoryNames.get(categoryIndex);
	}

	List<String> categoryNames()
	{
		return categoryNames;
	}

	int size()
	{
		return categoryIndexByItemId.size();
	}
}

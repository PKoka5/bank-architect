package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class BankTabPlanTest
{
	@Test
	public void emptyNumberedCategoriesAreCompressedWithoutLosingIdentity()
	{
		BankTabPlan plan = BankTabPlan.fromPreview(preview(
			items(10), items(20), Collections.emptyList(), items(40),
			Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
			Collections.emptyList(), Collections.emptyList(), items(100)));

		assertEquals(3, plan.getNumberedTabs().size());
		assertEquals(1, plan.getNumberedTabs().get(0).getBankTabNumber());
		assertEquals(2, plan.getNumberedTabs().get(0).getBlueprintCategoryNumber());
		assertEquals("Teleports, Runes & Jewellery",
			plan.getNumberedTabs().get(0).getCategoryName());
		assertEquals(2, plan.getNumberedTabs().get(1).getBankTabNumber());
		assertEquals(4, plan.getNumberedTabs().get(1).getBlueprintCategoryNumber());
		assertEquals("Potions, Food & PvM Supplies",
			plan.getNumberedTabs().get(1).getCategoryName());
		assertEquals(3, plan.getNumberedTabs().get(2).getBankTabNumber());
		assertEquals(10, plan.getNumberedTabs().get(2).getBlueprintCategoryNumber());
		assertEquals(Arrays.asList(20, 40, 100, 10), ids(plan.getFlattenedItems()));
		assertEquals("Currency & Account Utilities", plan.getMainCategoryName());
		assertEquals(Arrays.asList(10), ids(plan.getMainItems()));
	}

	@SafeVarargs
	private static BankOrganizationPreview preview(List<BankPreviewItem>... categoryItems)
	{
		if (categoryItems.length != 10)
		{
			throw new IllegalArgumentException("expected ten categories");
		}
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < categoryItems.length; index++)
		{
			categories.add(new BankCategoryPreview(
				BankPresets.IRONMAN.getCategories().get(index), categoryItems[index]));
		}
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}

	private static List<BankPreviewItem> items(int... itemIds)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}
		return items;
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			ids.add(item.getItemId());
		}
		return ids;
	}
}

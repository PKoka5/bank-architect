package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class PlannedCategoryIndexTest
{
	@Test
	public void itemsResolveToTheirPresetOrderedDestination()
	{
		PlannedCategoryIndex index = PlannedCategoryIndex.from(previewWith(
			items(995, 4151), items(11802), Collections.emptyList()));

		assertEquals(0, index.categoryIndexFor(995));
		assertEquals(0, index.categoryIndexFor(4151));
		assertEquals(1, index.categoryIndexFor(11802));
		assertEquals(3, index.size());
	}

	@Test
	public void anItemThatIsNotPlannedReportsNoDestination()
	{
		PlannedCategoryIndex index = PlannedCategoryIndex.from(previewWith(
			items(995), Collections.emptyList(), Collections.emptyList()));

		assertEquals(-1, index.categoryIndexFor(4151));
		assertEquals(-1, index.categoryIndexFor(0));
		assertEquals("", index.categoryName(-1));
	}

	@Test
	public void categoryNamesFollowThePresetOrderSoColoursStayStable()
	{
		PlannedCategoryIndex index = PlannedCategoryIndex.from(previewWith(
			items(995), items(4151), items(11802)));

		List<String> names = index.categoryNames();
		assertEquals(10, names.size());
		for (int position = 0; position < names.size(); position++)
		{
			assertEquals(BankPresets.IRONMAN.getCategories().get(position).getName(),
				names.get(position));
			assertEquals(names.get(position), index.categoryName(position));
		}
	}

	@Test
	public void blankPlannedSlotsAreNotIndexed()
	{
		List<BankPreviewItem> withBlank = new ArrayList<>();
		withBlank.add(BankPreviewItem.blank());
		withBlank.addAll(items(995));

		PlannedCategoryIndex index = PlannedCategoryIndex.from(previewWith(withBlank,
			Collections.emptyList(), Collections.emptyList()));

		assertEquals(1, index.size());
		assertEquals(0, index.categoryIndexFor(995));
	}

	@Test
	public void anItemPlannedTwiceKeepsItsFirstDestination()
	{
		PlannedCategoryIndex index = PlannedCategoryIndex.from(previewWith(
			items(995), items(995), Collections.emptyList()));

		assertEquals(0, index.categoryIndexFor(995));
	}

	@Test
	public void everyDestinationOfTheShippedPresetHasItsOwnColour()
	{
		assertEquals(BankPresets.IRONMAN.getCategories().size(), CategoryPalette.size());
		for (int first = 0; first < CategoryPalette.size(); first++)
		{
			for (int second = first + 1; second < CategoryPalette.size(); second++)
			{
				assertTrue("colours " + first + " and " + second + " must differ",
					!CategoryPalette.colorFor(first).equals(CategoryPalette.colorFor(second)));
			}
		}
	}

	@Test
	public void opacityOnlyChangesTheAlphaChannelAndIsClamped()
	{
		assertEquals(CategoryPalette.colorFor(0).getRGB() & 0xFFFFFF,
			CategoryPalette.colorFor(0, 40).getRGB() & 0xFFFFFF);
		assertEquals(102, CategoryPalette.colorFor(0, 40).getAlpha());
		assertEquals(0, CategoryPalette.colorFor(0, -10).getAlpha());
		assertEquals(255, CategoryPalette.colorFor(0, 500).getAlpha());
	}

	private static BankOrganizationPreview previewWith(List<BankPreviewItem> first,
		List<BankPreviewItem> second, List<BankPreviewItem> third)
	{
		List<BankCategoryPreview> previews = new ArrayList<>();
		for (int index = 0; index < BankPresets.IRONMAN.getCategories().size(); index++)
		{
			List<BankPreviewItem> items = index == 0 ? first
				: index == 1 ? second : index == 2 ? third : Collections.emptyList();
			previews.add(new BankCategoryPreview(
				BankPresets.IRONMAN.getCategories().get(index), items));
		}
		return new BankOrganizationPreview(BankPresets.IRONMAN, previews);
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
}

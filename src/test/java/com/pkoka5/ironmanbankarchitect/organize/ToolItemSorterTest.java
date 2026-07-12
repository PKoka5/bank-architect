package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToolItemSorterTest
{
	@Test
	public void keepsPartialOutfitsContiguousAndSlotOrderedBeforeLooseTools()
	{
		List<BankPreviewItem> sorted = ToolItemSorter.sort(Arrays.asList(
			item(1, "Hammer", "tool"),
			item(2, "Graceful boots", "skilling-outfit"),
			item(3, "Graceful hood", "skilling-outfit"),
			item(4, "Angler waders", "skilling-outfit"),
			item(5, "Angler hat", "skilling-outfit"),
			item(6, "Graceful top", "skilling-outfit"),
			item(7, "Dragon pickaxe", "tool")
		));

		assertEquals(Arrays.asList("Angler hat", "Angler waders", "Graceful hood", "Graceful top",
			"Graceful boots", "Dragon pickaxe", "Hammer"), names(sorted));
	}

	@Test
	public void groupsFishingHunterSlayerAndContainerToolsByUse()
	{
		List<BankPreviewItem> sorted = ToolItemSorter.sort(Arrays.asList(
			item(1, "Butterfly net", "tool"), item(2, "Fishing rod", "tool"),
			item(3, "Barbarian rod", "tool"), item(4, "Rock hammer", "slayer-tool"),
			item(5, "Bag of salt", "slayer-tool"), item(6, "Coal bag", "resource-container"),
			item(7, "Open fish barrel", "resource-container"), item(8, "House keys", "tool"),
			item(9, "Lockpick", "tool")
		));

		assertEquals(Arrays.asList("Barbarian rod", "Fishing rod", "Butterfly net", "House keys",
			"Lockpick", "Bag of salt", "Rock hammer", "Coal bag", "Open fish barrel"), names(sorted));
	}

	@Test
	public void keepsFarmersOutfitInOneSlotOrderedFamily()
	{
		List<BankPreviewItem> sorted = ToolItemSorter.sort(Arrays.asList(
			item(1, "Farmer's boots", "skilling-outfit"),
			item(2, "Farmer's boro trousers", "skilling-outfit"),
			item(3, "Farmer's shirt", "skilling-outfit"),
			item(4, "Farmer's strawhat", "skilling-outfit")
		));

		assertEquals(Arrays.asList("Farmer's strawhat", "Farmer's shirt", "Farmer's boro trousers",
			"Farmer's boots"), names(sorted));
	}

	private static BankPreviewItem item(int id, String name, String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.TOOL, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

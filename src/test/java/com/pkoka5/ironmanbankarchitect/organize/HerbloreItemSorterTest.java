package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class HerbloreItemSorterTest
{
	@Test
	public void laysOutRecipeChainAsOneRow()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Eye of newt"),
			item(2, "Irit potion (unf)"),
			item(3, "Irit leaf"),
			item(4, "Grimy irit"),
			item(5, "Irit seed")
		));

		assertEquals(Arrays.asList(
			"Grimy irit",
			"Irit leaf",
			"Irit seed",
			"Irit potion (unf)",
			"Eye of newt"
		), names(laidOut));
	}

	@Test
	public void padsRecipeRowsToGridWidthSoNextChainStaysAligned()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy ranarr weed"),
			item(2, "Ranarr seed"),
			item(3, "Snape grass"),
			item(4, "Grimy torstol"),
			item(5, "Torstol seed"),
			item(10, "Compost"),
			item(11, "Herb sack"),
			item(12, "Plant cure"),
			item(13, "Bottomless bucket"),
			item(14, "Gardening trowel"),
			item(15, "Empty sack"),
			item(16, "Magic secateurs"),
			item(17, "Amylase crystal"),
			item(18, "Super combat potion(4)"),
			item(19, "Torstol potion (unf)")
		));

		// Ranarr row: 3 chain cells + 5 filler = 8, so the torstol row starts
		// exactly at index 8.
		assertEquals(8, indexOf(laidOut, "Grimy torstol"));
		assertEquals("Grimy ranarr weed", laidOut.get(0).getDisplayName());
		assertEquals("Ranarr seed", laidOut.get(1).getDisplayName());
		assertEquals("Snape grass", laidOut.get(2).getDisplayName());
		assertEquals("Grimy torstol", laidOut.get(8).getDisplayName());
		assertEquals("Torstol seed", laidOut.get(9).getDisplayName());
		assertEquals("Torstol potion (unf)", laidOut.get(10).getDisplayName());
		assertEquals(15, laidOut.size());
	}

	@Test
	public void sharedSecondaryGoesToTheChainThatIsActuallyOwned()
	{
		// Eye of newt is a guam AND irit secondary; with no guam items owned it
		// must join the irit row instead of disappearing into a dead guam row.
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Eye of newt"),
			item(2, "Grimy irit"),
			item(3, "Irit seed")
		));

		assertEquals(Arrays.asList("Grimy irit", "Irit seed", "Eye of newt"), names(laidOut));
	}

	@Test
	public void itemsWithoutAChainKeepTheSubgroupOrder()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Nasturtium seed"),
			item(2, "Compost")
		));

		assertEquals(2, laidOut.size());
	}

	private static int indexOf(List<BankPreviewItem> items, String name)
	{
		for (int i = 0; i < items.size(); i++)
		{
			if (name.equals(items.get(i).getDisplayName()))
			{
				return i;
			}
		}

		return -1;
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.HERBLORE,
			ItemCategory.HERBLORE.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}
}

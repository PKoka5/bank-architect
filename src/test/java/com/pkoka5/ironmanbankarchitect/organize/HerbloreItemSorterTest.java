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

		// Missing semantic cells are filled in-place, and the next recipe still
		// starts on a bank row boundary.
		assertEquals(8, indexOf(laidOut, "Grimy torstol"));
		assertEquals("Grimy ranarr weed", laidOut.get(0).getDisplayName());
		assertEquals("Ranarr seed", laidOut.get(2).getDisplayName());
		assertEquals("Snape grass", laidOut.get(4).getDisplayName());
		assertEquals("Grimy torstol", laidOut.get(8).getDisplayName());
		// Only four fillers remain, so this final incomplete row deliberately
		// falls back to a dense chain instead of inventing an empty slot.
		assertEquals("Torstol seed", laidOut.get(9).getDisplayName());
		assertEquals("Torstol potion (unf)", laidOut.get(10).getDisplayName());
		assertEquals(15, laidOut.size());
	}

	@Test
	public void placesPotionDosesInStableRecipeColumns()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy irit"),
			item(2, "Clean irit"),
			item(3, "Irit seed"),
			item(4, "Irit potion (unf)"),
			item(5, "Eye of newt"),
			item(6, "Super attack (3)"),
			item(7, "Super attack (2)"),
			item(8, "Super attack (1)")));

		assertEquals(Arrays.asList(
			"Grimy irit", "Clean irit", "Irit seed", "Irit potion (unf)",
			"Eye of newt", "Super attack (3)", "Super attack (2)", "Super attack (1)"
		), names(laidOut));
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
	public void sharedSecondaryPrefersHighestTierOwnedRecipe()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Guam leaf"),
			item(2, "Guam seed"),
			item(3, "Grimy irit"),
			item(4, "Irit seed"),
			item(5, "Eye of newt"),
			item(6, "Grimy tarromin"),
			item(7, "Tarromin seed"),
			item(8, "Grimy kwuarm"),
			item(9, "Kwuarm seed"),
			item(10, "Limpwurt root"),
			item(11, "Compost"),
			item(12, "Plant cure"),
			item(13, "Seed dibber"),
			item(14, "Rake"),
			item(15, "Spade"),
			item(16, "Trowel"),
			item(17, "Secateurs"),
			item(18, "Watering can"),
			item(19, "Empty sack"),
			item(20, "Basket"),
			item(21, "Compost potion"),
			item(22, "Gardening boots"),
			item(23, "Gardening gloves"),
			item(24, "Gardening hat"),
			item(25, "Gardening top"),
			item(26, "Gardening legs"),
			item(27, "Gardening cape"),
			item(28, "Gardening amulet"),
			item(29, "Gardening ring"),
			item(30, "Gardening bucket"),
			item(31, "Gardening basket"),
			item(32, "Gardening pouch")
		));

		assertEquals(4, indexOf(laidOut, "Eye of newt") % 8);
		assertEquals(4, indexOf(laidOut, "Limpwurt root") % 8);
		assertEquals(true, indexOf(laidOut, "Eye of newt") > indexOf(laidOut, "Grimy irit"));
		assertEquals(true, indexOf(laidOut, "Limpwurt root") > indexOf(laidOut, "Grimy kwuarm"));
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

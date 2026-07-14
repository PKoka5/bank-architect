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
	public void canonicalIdsRecognizeHerbStagesWithoutDependingOnDisplayNames()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(249, "Deliberately renamed clean herb"),
			item(199, "Deliberately renamed grimy herb"),
			item(91, "Deliberately renamed unfinished potion")
		));

		assertEquals(Arrays.asList(
			"Deliberately renamed grimy herb",
			"Deliberately renamed clean herb",
			"Deliberately renamed unfinished potion"
		), names(laidOut));
	}

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
	public void completeRowsStayAlignedAndPartialRecipesRemainDense()
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

		assertEquals(3, indexOf(laidOut, "Grimy torstol"));
		assertEquals("Grimy ranarr weed", laidOut.get(0).getDisplayName());
		assertEquals("Ranarr seed", laidOut.get(1).getDisplayName());
		assertEquals("Snape grass", laidOut.get(2).getDisplayName());
		assertEquals("Torstol seed", laidOut.get(4).getDisplayName());
		assertEquals("Torstol potion (unf)", laidOut.get(5).getDisplayName());
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
	public void orphanPotionFamilyStillUsesDescendingDoseOrder()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(2456, "Antifire potion(2)"),
			item(2454, "Antifire potion(3)"),
			item(5954, "Antidote++(3)"),
			item(900001, "Bird nest")));

		assertEquals(Arrays.asList("Antifire potion(3)", "Antifire potion(2)",
			"Antidote++(3)", "Bird nest"), names(laidOut));
	}

	@Test
	public void canonicalDoseIdsCompleteRecipeColumnsWithoutDependingOnPotionNames()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(209, "Renamed grimy input"), item(259, "Renamed clean input"),
			item(900001, "Irit seed"), item(101, "Renamed unfinished input"),
			item(900002, "Eye of newt"), item(145, "Renamed dose three"),
			item(147, "Renamed dose two"), item(149, "Renamed dose one")));

		assertEquals(Arrays.asList("Renamed grimy input", "Renamed clean input", "Irit seed",
			"Renamed unfinished input", "Eye of newt", "Renamed dose three",
			"Renamed dose two", "Renamed dose one"), names(laidOut));
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

		assertEquals(indexOf(laidOut, "Irit seed") + 1, indexOf(laidOut, "Eye of newt"));
		assertEquals(indexOf(laidOut, "Kwuarm seed") + 1, indexOf(laidOut, "Limpwurt root"));
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

	@Test
	public void seedNamesNeverMasqueradeAsPotionSecondaries()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy tarromin"), item(2, "Tarromin seed"), item(3, "Limpwurt seed"),
			item(4, "Z filler one"), item(5, "Z filler two"), item(6, "Z filler three"),
			item(7, "Z filler four"), item(8, "Z filler five"), item(9, "Limpwurt root")
		));

		assertEquals(2, indexOf(laidOut, "Limpwurt root"));
		assertEquals(true, indexOf(laidOut, "Limpwurt seed") > indexOf(laidOut, "Limpwurt root"));
	}

	@Test
	public void usesHighestStandardHarralanderAndLantadymeRecipes()
	{
		List<BankPreviewItem> harralander = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy harralander"), item(2, "Harralander potion (unf)"),
			item(3, "Chocolate dust"), item(4, "Energy potion(3)")));
		List<BankPreviewItem> lantadyme = HerbloreItemSorter.layout(Arrays.asList(
			item(5, "Grimy lantadyme"), item(6, "Lantadyme potion (unf)"),
			item(7, "Potato cactus"), item(8, "Magic potion(3)")));

		assertEquals(Arrays.asList("Grimy harralander", "Harralander potion (unf)",
			"Chocolate dust", "Energy potion(3)"), names(harralander));
		assertEquals(Arrays.asList("Grimy lantadyme", "Lantadyme potion (unf)",
			"Potato cactus", "Magic potion(3)"), names(lantadyme));
	}

	@Test
	public void productFamiliesDoNotMatchTheirSuperVariantsBySubstring()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy harralander"), item(2, "Harralander potion (unf)"),
			item(3, "Super energy(3)"), item(4, "Energy potion(3)"),
			item(5, "A filler"), item(6, "B filler"), item(7, "C filler"), item(8, "D filler")
		));

		assertEquals(2, indexOf(laidOut, "Energy potion(3)"));
		assertEquals(true, indexOf(laidOut, "Super energy(3)") != 2);
	}

	@Test
	public void herbStagesUseExactNamesInsteadOfEmbeddedLetterSequences()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy guam leaf"), item(2, "Guam potion (unf)"), item(3, "Eye of newt"),
			item(4, "Spirit seed"), item(5, "A filler"), item(6, "B filler"),
			item(7, "C filler"), item(8, "D filler")
		));

		assertEquals(indexOf(laidOut, "Grimy guam leaf") + 2, indexOf(laidOut, "Eye of newt"));
	}

	@Test
	public void farmingItemsNeverFillMissingRecipeCells()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy guam leaf"), item(2, "Guam seed"),
			categorizedItem(3, "Magic potion(3)", ItemCategory.POTION),
			categorizedItem(11, "Apple tree seed", ItemCategory.FARMING),
			categorizedItem(12, "Banana tree seed", ItemCategory.FARMING),
			categorizedItem(13, "Calquat tree seed", ItemCategory.FARMING),
			categorizedItem(14, "Maple seed", ItemCategory.FARMING),
			categorizedItem(15, "Palm tree seed", ItemCategory.FARMING),
			categorizedItem(16, "Willow seed", ItemCategory.FARMING)));

		assertEquals(Arrays.asList("Grimy guam leaf", "Guam seed", "Magic potion(3)"),
			names(laidOut.subList(0, 3)));
		for (int index = 3; index < laidOut.size(); index++)
		{
			assertEquals(ItemCategory.FARMING, laidOut.get(index).getItemCategory());
		}
	}

	@Test
	public void sparseEarlyRecipeDoesNotBreakALaterCompleteRow()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Grimy guam leaf"), item(2, "Guam potion (unf)"),
			item(3, "Grimy irit"), item(4, "Clean irit"), item(5, "Irit seed"),
			item(6, "Irit potion (unf)"), item(7, "Eye of newt"),
			item(8, "Super attack (3)"), item(9, "Super attack (2)"),
			item(10, "Super attack (1)")
		));

		assertEquals(0, indexOf(laidOut, "Grimy irit"));
		assertEquals(7, indexOf(laidOut, "Super attack (1)"));
		assertEquals(8, indexOf(laidOut, "Grimy guam leaf"));
	}

	@Test
	public void partialRecipeRunsNeverWrapWhenRealSpilloverCanCompleteTheRow()
	{
		List<BankPreviewItem> laidOut = HerbloreItemSorter.layout(Arrays.asList(
			item(1, "Guam leaf"), item(2, "Guam seed"),
			item(3, "Marrentill"), item(4, "Marrentill seed"),
			item(5, "Grimy tarromin"), item(6, "Tarromin"), item(7, "Tarromin seed"),
			item(8, "Tarromin potion (unf)"),
			item(9, "Grimy harralander"), item(10, "Harralander seed"),
			item(11, "Harralander potion (unf)"),
			item(12, "Grimy lantadyme"), item(13, "Lantadyme"),
			item(14, "Lantadyme seed"), item(15, "Lantadyme potion (unf)"),
			item(16, "Vial of water"), item(17, "Bird nest")));

		assertEquals(0, indexOf(laidOut, "Guam leaf"));
		assertEquals(2, indexOf(laidOut, "Marrentill"));
		assertEquals(4, indexOf(laidOut, "Grimy tarromin"));
		int harralander = indexOf(laidOut, "Grimy harralander");
		int lantadyme = indexOf(laidOut, "Grimy lantadyme");
		assertEquals(harralander / 8, indexOf(laidOut, "Harralander potion (unf)") / 8);
		assertEquals(lantadyme / 8, indexOf(laidOut, "Lantadyme potion (unf)") / 8);
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

	private static BankPreviewItem categorizedItem(int itemId, String name, ItemCategory category)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream()
			.map(BankPreviewItem::getDisplayName)
			.collect(Collectors.toList());
	}
}

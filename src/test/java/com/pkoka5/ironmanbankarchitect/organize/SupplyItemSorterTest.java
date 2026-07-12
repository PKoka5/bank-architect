package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SupplyItemSorterTest
{
	@Test
	public void keepsAllFullDosePotionsAheadOfUtilitiesFoodAndDrinks()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(1, "Shark", "food"), item(2, "Super energy(4)", "potion-dose-4"),
			item(3, "Prayer potion(4)", "potion-dose-4"), item(4, "Holy wrench", "pvm-utility"),
			item(5, "Sanfew serum(4)", "potion-dose-4"), item(6, "Bandit's brew", "drink"),
			item(7, "Superattack mix(2)", "potion")));

		assertEquals(Arrays.asList("Prayer potion(4)", "Sanfew serum(4)", "Super energy(4)",
			"Holy wrench", "Superattack mix(2)", "Shark", "Bandit's brew"), names(sorted));
	}

	@Test
	public void keepsFullAndHalfPiesInOneFamily()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(1, "Half a botanical pie", "food"), item(2, "Cake", "food"),
			item(3, "Botanical pie", "food")));

		assertEquals(Arrays.asList("Botanical pie", "Half a botanical pie", "Cake"), names(sorted));
	}

	private static BankPreviewItem item(int id, String name, String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.POTION, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

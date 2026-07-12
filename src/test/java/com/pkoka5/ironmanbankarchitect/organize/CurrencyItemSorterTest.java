package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurrencyItemSorterTest
{
	@Test
	public void exchangeCurrenciesStayAheadOfReusableAccountUtilities()
	{
		List<BankPreviewItem> sorted = CurrencyItemSorter.sort(Arrays.asList(
			item(1, "Ghommal's hilt 1"), item(2, "Coins"), item(3, "Hallowed mark"),
			item(4, "Rada's blessing 2"), item(5, "Sawmill coupon (oak plank)"),
			item(6, "Frog token"), item(7, "Western banner 2")));

		assertEquals(Arrays.asList("Coins", "Hallowed mark", "Sawmill coupon (oak plank)",
			"Frog token", "Ghommal's hilt 1", "Rada's blessing 2", "Western banner 2"), names(sorted));
	}

	private static BankPreviewItem item(int id, String name)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.CURRENCY, "currency",
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

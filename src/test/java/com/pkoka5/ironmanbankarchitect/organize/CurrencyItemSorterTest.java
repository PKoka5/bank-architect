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
	public void mainGrabItemsStayAheadOfDiaryRewardsAndNicheCurrencies()
	{
		List<BankPreviewItem> sorted = CurrencyItemSorter.sort(Arrays.asList(
			item(995, "Coins"), item(3, "Hallowed mark"), item(4, "Rada's blessing 2"),
			item(5, "Sawmill coupon (oak plank)"), item(6, "Frog token"),
			item(7, "Western banner 2"),
			item(12791, "Rune pouch", ItemCategory.RUNE, "rune-container"),
			item(11850, "Graceful hood", ItemCategory.TOOL, "skilling-outfit"),
			item(2722, "Clue scroll (hard)", ItemCategory.CLUE, "treasure-trail"),
			item(4251, "Ectophial", ItemCategory.TELEPORT, "teleport")));

		assertEquals(Arrays.asList("Coins", "Rune pouch", "Graceful hood",
			"Clue scroll (hard)", "Hallowed mark", "Ectophial", "Rada's blessing 2",
			"Western banner 2", "Sawmill coupon (oak plank)", "Frog token"), names(sorted));
	}

	private static BankPreviewItem item(int id, String name)
	{
		return item(id, name, ItemCategory.CURRENCY, "currency");
	}

	private static BankPreviewItem item(int id, String name, ItemCategory category, String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, category, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

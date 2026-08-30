package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IronmanMainItemSorterTest
{
	@Test
	public void mainDelegatesItsTeleportBandToTheTeleportWorkflow()
	{
		List<BankPreviewItem> sorted = IronmanMainItemSorter.sort(Arrays.asList(
			item(995, "Coins", ItemCategory.CURRENCY, "currency"),
			item(554, "Fire rune", ItemCategory.RUNE, "rune"),
			item(12402, "Nardah teleport", ItemCategory.TELEPORT, "teleport"),
			item(8013, "Teleport to house", ItemCategory.TELEPORT, "teleport"),
			item(21389, "Master scroll book", ItemCategory.TELEPORT, "teleport-container"),
			item(2564, "Ring of dueling(2)", ItemCategory.TELEPORT, "teleport"),
			item(2552, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport"),
			item(4251, "Ectophial", ItemCategory.TELEPORT, "teleport")
		));

		assertEquals(Arrays.asList(
			"Coins", "Fire rune", "Ectophial", "Ring of dueling(8)",
			"Ring of dueling(2)", "Master scroll book", "Teleport to house",
			"Nardah teleport"), names(sorted));
	}

	@Test
	public void realBankTeleportFamiliesFollowTheSameWorkflow()
	{
		int[] itemIds = {
			772, 2552, 2564, 3853, 3867, 4251, 6103, 6707, 8013,
			11192, 11194, 13391, 13660, 21155, 21166, 21175, 21389, 22400,
			24709, 25818, 29275, 30638, 32399
		};
		List<BankPreviewItem> items = new ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(ResourceItemRegistry.INSTANCE.findById(itemId).get(), 1));
		}

		List<String> sorted = names(IronmanMainItemSorter.sort(items));
		assertBefore(sorted, "Ectophial", "Ring of dueling(8)");
		assertBefore(sorted, "Ring of dueling(2)", "Master scroll book");
		assertBefore(sorted, "Ring of dueling(2)", "Teleport to house");
		assertBefore(sorted, "Teleport to house", "Lizardman fang");
		assertBefore(sorted, "Lizardman fang", "Dramen staff");
		assertEquals(1, sorted.indexOf("Ring of dueling(2)")
			- sorted.indexOf("Ring of dueling(8)"));
		assertEquals(1, sorted.indexOf("Games necklace(1)")
			- sorted.indexOf("Games necklace(8)"));
		assertEquals(1, sorted.indexOf("Burning amulet(1)")
			- sorted.indexOf("Burning amulet(5)"));
	}

	private static void assertBefore(List<String> sorted, String first, String second)
	{
		assertTrue(sorted.toString(), sorted.indexOf(first) < sorted.indexOf(second));
	}

	private static BankPreviewItem item(int id, String name, ItemCategory category,
		String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, category, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

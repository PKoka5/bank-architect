package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TeleportItemSorterTest
{
	@Test
	public void groupsRolesAndOrdersJewelleryByHighestChargeFirst()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(1, "Ring of dueling(2)", ItemCategory.TELEPORT, "teleport"),
			item(2, "Pure essence", ItemCategory.RUNE, "rune"),
			item(3, "Nature rune", ItemCategory.RUNE, "rune"),
			item(4, "Air rune", ItemCategory.RUNE, "rune"),
			item(5, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport"),
			item(6, "Rune pouch", ItemCategory.RUNE, "rune-container"),
			item(7, "Teleport to house", ItemCategory.TELEPORT, "teleport"),
			item(8, "Ectophial", ItemCategory.TELEPORT, "teleport")
		));

		assertEquals(Arrays.asList("Air rune", "Nature rune", "Pure essence", "Rune pouch",
			"Teleport to house", "Ring of dueling(8)", "Ring of dueling(2)", "Ectophial"),
			names(sorted));
	}

	@Test
	public void keepsRunecraftingUtilityWithPouchesInsteadOfElementalRunes()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(1, "Binding necklace", ItemCategory.RUNE, "runecrafting-utility"),
			item(2, "Water rune", ItemCategory.RUNE, "rune"),
			item(3, "Pure essence", ItemCategory.RUNE, "rune"),
			item(4, "Small pouch", ItemCategory.RUNE, "rune-container")
		));

		assertEquals(Arrays.asList("Water rune", "Pure essence", "Binding necklace", "Small pouch"),
			names(sorted));
	}

	@Test
	public void standardTeleportTabletsAndScrollsStayInConsumableBlock()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(1, "Ectophial", ItemCategory.TELEPORT, "teleport"),
			item(2, "Varrock teleport", ItemCategory.TELEPORT, "teleport"),
			item(3, "Nardah teleport", ItemCategory.TELEPORT, "teleport"),
			item(4, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport")
		));

		assertEquals(Arrays.asList("Nardah teleport", "Varrock teleport", "Ring of dueling(8)",
			"Ectophial"), names(sorted));
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

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
			item(7, "Teleport to house", ItemCategory.TELEPORT, "teleport-tablet"),
			item(8, "Ectophial", ItemCategory.TELEPORT, "teleport")
		));

		assertEquals(Arrays.asList("Air rune", "Nature rune", "Pure essence", "Rune pouch",
			"Ectophial", "Ring of dueling(8)", "Ring of dueling(2)", "Teleport to house"),
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

		assertEquals(Arrays.asList("Water rune", "Pure essence", "Small pouch", "Binding necklace"),
			names(sorted));
	}

	@Test
	public void ordersRunecraftingFocusesAndEssencePouchesSemantically()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(1, "Water talisman", ItemCategory.RUNE, "runecrafting-focus"),
			item(2, "Air talisman", ItemCategory.RUNE, "runecrafting-focus"),
			item(3, "Medium pouch", ItemCategory.RUNE, "rune-container"),
			item(4, "Small pouch", ItemCategory.RUNE, "rune-container"),
			item(5, "Rune pouch", ItemCategory.RUNE, "rune-container"),
			item(6, "Binding necklace", ItemCategory.RUNE, "runecrafting-utility")));

		assertEquals(Arrays.asList("Air talisman", "Water talisman", "Small pouch", "Medium pouch",
			"Rune pouch", "Binding necklace"), names(sorted));
	}

	@Test
	public void standardTeleportTabletsAndScrollsStayInConsumableBlock()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(1, "Ectophial", ItemCategory.TELEPORT, "teleport"),
			item(8007, "Varrock teleport", ItemCategory.TELEPORT, "teleport-tablet"),
			item(12402, "Nardah teleport", ItemCategory.TELEPORT, "teleport-scroll"),
			item(4, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport")
		));

		assertEquals(Arrays.asList("Ectophial", "Ring of dueling(8)", "Varrock teleport",
			"Nardah teleport"), names(sorted));
	}

	@Test
	public void teleportFormsFollowQuickAccessWorkflowAndKeepChargeFamiliesTogether()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(772, "Dramen staff", ItemCategory.TELEPORT, "transport-access"),
			item(12402, "Nardah teleport", ItemCategory.TELEPORT, "teleport-scroll"),
			item(8013, "Teleport to house", ItemCategory.TELEPORT, "teleport-tablet"),
			item(21389, "Master scroll book", ItemCategory.TELEPORT, "teleport-container"),
			item(2564, "Ring of dueling(2)", ItemCategory.TELEPORT, "teleport"),
			item(2552, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport"),
			item(4251, "Ectophial", ItemCategory.TELEPORT, "teleport"),
			item(13393, "Xeric's talisman", ItemCategory.TELEPORT, "teleport"),
			item(13391, "Lizardman fang", ItemCategory.TELEPORT, "teleport-charge")
		));

		assertEquals(Arrays.asList(
			"Ectophial", "Xeric's talisman",
			"Ring of dueling(8)", "Ring of dueling(2)",
			"Master scroll book", "Teleport to house", "Nardah teleport",
			"Lizardman fang", "Dramen staff"), names(sorted));
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

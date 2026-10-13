package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void groupsActualTabletColoursAndKeepsEachColourAlphabetical()
	{
		List<BankPreviewItem> input = Arrays.asList(
			tablet(19629, "Barrows teleport"),
			tablet(12775, "Annakarl teleport"),
			tablet(8007, "Varrock teleport"),
			tablet(24949, "Moonclan teleport"),
			tablet(21863, "Wise old man's teleport tablet"),
			tablet(24336, "Target teleport"),
			tablet(11743, "Pollnivneach teleport"),
			tablet(24961, "Catherby teleport"),
			tablet(19613, "Arceuus library teleport"),
			tablet(24251, "Wilderness crabs teleport"),
			tablet(21541, "Volcanic mine teleport"),
			tablet(31443, "Teleport to boat"),
			tablet(31441, "Summon boat"),
			tablet(8015, "Bones to peaches"),
			tablet(8011, "Ardougne teleport"));

		assertEquals(Arrays.asList(
			"Ardougne teleport", "Bones to peaches", "Pollnivneach teleport", "Teleport to boat",
			"Varrock teleport", "Catherby teleport", "Moonclan teleport", "Annakarl teleport",
			"Summon boat", "Target teleport", "Arceuus library teleport", "Barrows teleport",
			"Wilderness crabs teleport", "Volcanic mine teleport", "Wise old man's teleport tablet"),
			names(TeleportItemSorter.sort(input)));
	}

	@Test
	public void placeholderTabletsUseTheirCanonicalColourAndUnknownNamesDoNotSpoofIt()
	{
		BankPreviewItem placeholder = new BankPreviewItem(new CatalogItem(24949, "Moonclan teleport",
			ItemCategory.TELEPORT, "teleport-tablet", Collections.emptySet(), null), 0, true);
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			tablet(60000, "Ardougne teleport"),
			tablet(19633, "Arceuus-looking teleport"),
			tablet(8006, "A brand new teleport"),
			tablet(19629, "Barrows teleport"),
			placeholder,
			tablet(8007, "Varrock teleport")));

		assertEquals(Arrays.asList(8007, 24949, 19629, 8006, 19633, 60000), ids(sorted));
		assertTrue(sorted.get(1).isPlaceholder());
		assertEquals(0, sorted.get(1).getQuantity());
	}

	@Test
	public void tabletColoursDoNotChangeOtherTeleportRolesOrTheirChargeOrdering()
	{
		List<BankPreviewItem> sorted = TeleportItemSorter.sort(Arrays.asList(
			item(12775, "A teleport charge", ItemCategory.TELEPORT, "teleport-charge"),
			tablet(8007, "Varrock teleport"),
			item(8007, "Z teleport charge", ItemCategory.TELEPORT, "teleport-charge"),
			item(2564, "Ring of dueling(2)", ItemCategory.TELEPORT, "teleport"),
			item(563, "Law rune", ItemCategory.RUNE, "rune"),
			item(12402, "Nardah teleport", ItemCategory.TELEPORT, "teleport-scroll"),
			item(2552, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport"),
			item(556, "Air rune", ItemCategory.RUNE, "rune"),
			item(4251, "Ectophial", ItemCategory.TELEPORT, "teleport"),
			tablet(12775, "Annakarl teleport")));

		assertEquals(Arrays.asList("Air rune", "Law rune", "Ectophial", "Ring of dueling(8)",
			"Ring of dueling(2)", "Varrock teleport", "Annakarl teleport", "Nardah teleport",
			"A teleport charge", "Z teleport charge"), names(sorted));
	}

	@Test
	public void actualMainBlueprintKeepsTabletColoursTogetherAlongsideRunesAndJewellery()
	{
		int[] input = {19629, 12775, 8007, 24949, 19613, 8011, 24961, 24336, 24251,
			556, 563, 8013, 2552, 4251};
		List<BankItemSnapshot> bank = new ArrayList<>();
		for (int index = 0; index < input.length; index++)
		{
			bank.add(new BankItemSnapshot(input[index], 1, index));
		}
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(bank),
			CompositeItemCatalog.DEFAULT, BankPresets.MAIN, GearStatsSource.NONE,
			ItemValueSource.NONE, CategoryOverrideSource.NONE,
			BankLayoutPlan.defaultFor(BankPresets.MAIN), BankLayoutOptions.DEFAULTS);
		List<Integer> colouredTablets = preview.getCategories().get(1).getItems().stream()
			.filter(item -> !item.isBlank() && item.getSubcategory().equals("teleport-tablet"))
			.filter(item -> item.getItemId() != 8013)
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());

		assertEquals(Arrays.asList(8011, 8007, 24961, 24949, 12775, 24336, 19613, 19629, 24251),
			colouredTablets);
		List<Integer> expected = Arrays.stream(input).boxed().sorted().collect(Collectors.toList());
		List<Integer> actual = preview.getPlannedItems().stream().map(BankPreviewItem::getItemId)
			.sorted().collect(Collectors.toList());
		assertEquals("Colour sorting must retain every bank item exactly once", expected, actual);
	}

	@Test
	public void mixedColourComparatorIsStableAcrossShufflesAndTransitive()
	{
		List<BankPreviewItem> bank = Arrays.asList(
			tablet(8007, "Varrock teleport"), tablet(8011, "Ardougne teleport"),
			tablet(24949, "Moonclan teleport"), tablet(24961, "Catherby teleport"),
			tablet(12775, "Annakarl teleport"), tablet(24336, "Target teleport"),
			tablet(19629, "Barrows teleport"), tablet(24251, "Wilderness crabs teleport"),
			tablet(21541, "Volcanic mine teleport"), tablet(21863, "Wise old man's teleport tablet"),
			tablet(60000, "Ardougne teleport"),
			item(556, "Air rune", ItemCategory.RUNE, "rune"),
			item(2552, "Ring of dueling(8)", ItemCategory.TELEPORT, "teleport"),
			item(2564, "Ring of dueling(2)", ItemCategory.TELEPORT, "teleport"),
			item(12402, "Nardah teleport", ItemCategory.TELEPORT, "teleport-scroll"));
		List<Integer> expected = ids(TeleportItemSorter.sort(bank));
		for (int seed = 0; seed < 32; seed++)
		{
			List<BankPreviewItem> shuffled = new ArrayList<>(bank);
			Collections.shuffle(shuffled, new Random(seed));
			assertEquals("Input order must not affect the blueprint", expected,
				ids(TeleportItemSorter.sort(shuffled)));
		}
		for (BankPreviewItem first : bank)
		{
			for (BankPreviewItem second : bank)
			{
				int compared = TeleportItemSorter.compareTeleportItems(first, second);
				assertEquals(Integer.signum(compared),
					-Integer.signum(TeleportItemSorter.compareTeleportItems(second, first)));
				for (BankPreviewItem third : bank)
				{
					if (compared <= 0 && TeleportItemSorter.compareTeleportItems(second, third) <= 0)
					{
						assertTrue("Colour ordering must have a consistent comparator contract",
							TeleportItemSorter.compareTeleportItems(first, third) <= 0);
					}
				}
			}
		}
	}

	private static BankPreviewItem tablet(int id, String name)
	{
		return item(id, name, ItemCategory.TELEPORT, "teleport-tablet");
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
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

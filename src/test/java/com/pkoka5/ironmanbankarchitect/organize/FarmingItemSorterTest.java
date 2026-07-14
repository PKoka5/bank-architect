package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FarmingItemSorterTest
{
	@Test
	public void ownedSeedFamiliesStayCanonicalAndNeverWrapWhenRealFillersExist()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(900001, "Compost"), item(900002, "Coconut"), item(900003, "Plant pot"),
			item(900004, "Papaya fruit"), item(900005, "Supercompost"), item(900006, "Banana"),
			item(900007, "Watermelon"), item(900008, "Redberries"), item(900009, "White lily"),
			item(22879, "Snape grass seed"), item(5096, "Marigold seed"),
			item(5097, "Rosemary seed"), item(5099, "Woad seed"), item(5100, "Limpwurt seed"),
			item(5305, "Barley seed"), item(5307, "Hammerstone seed"),
			item(5308, "Asgarnian seed"), item(5306, "Jute seed"),
			item(5309, "Yanillian seed"), item(5310, "Krandorian seed"),
			item(5311, "Wildblood seed"), item(5312, "Acorn"), item(5313, "Willow seed"),
			item(5314, "Maple seed"), item(21486, "Teak seed"), item(21488, "Mahogany seed"),
			item(22869, "Celastrus seed"), item(31547, "Camphor seed"),
			item(31549, "Ironwood seed"), item(31551, "Rosewood seed"));

		List<BankPreviewItem> result = FarmingItemSorter.layout(input, 1);

		assertRun(result, 1, Arrays.asList(22879, 5096, 5097, 5099, 5100));
		assertRun(result, 1, Arrays.asList(5305, 5307, 5308, 5306, 5309, 5310, 5311));
		assertRun(result, 1, Arrays.asList(5312, 5313, 5314, 21486, 21488, 22869, 31547, 31549));
		assertPermutation(input, result);
	}

	@Test
	public void partialFamiliesStayDenseWithoutInventingMissingSeeds()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(5283, "Apple tree seed"), item(5289, "Palm tree seed"),
			item(22883, "Iasor seed"), item(900001, "Compost"));

		List<BankPreviewItem> result = FarmingItemSorter.layout(input, 0);

		assertEquals(4, result.size());
		assertPermutation(input, result);
		assertEquals(Arrays.asList(5283, 5289, 22883, 900001), ids(result));
	}

	@Test
	public void activeHerbSeedRemainsOwnedByItsPotionMakingChain()
	{
		List<BankPreviewItem> result = HerbloreItemSorter.layout(Arrays.asList(
			herblore(209, "Grimy irit"), herblore(259, "Irit leaf"),
			farming(5297, "Irit seed"), herblore(101, "Irit potion (unf)"),
			farming(5283, "Apple tree seed"), farming(5284, "Banana tree seed")));

		assertEquals(Arrays.asList(209, 259, 5297, 101), ids(result.subList(0, 4)));
		assertEquals(Arrays.asList(5283, 5284), ids(result.subList(4, 6)));
	}

	@Test
	public void compostAndPlantMaterialsRemainSeparateFromProduce()
	{
		List<BankPreviewItem> input = Arrays.asList(
			item(5974, "Coconut"), item(6034, "Supercompost"),
			item(5354, "Filled plant pot"), item(6032, "Compost"),
			item(21483, "Ultracompost"), item(6036, "Plant cure"),
			item(5356, "Plant pot"), item(5504, "Strawberry"));

		List<BankPreviewItem> result = FarmingItemSorter.layout(input, 0);

		assertRun(result, 0, Arrays.asList(6032, 6034, 21483));
		assertRun(result, 0, Arrays.asList(5354, 5356, 6036));
		assertPermutation(input, result);
	}

	private static void assertRun(List<BankPreviewItem> items, int usedColumns,
		List<Integer> expected)
	{
		List<Integer> actual = ids(items);
		int start = actual.indexOf(expected.get(0));
		assertTrue(start >= 0);
		assertTrue((start + usedColumns) % 8 + expected.size() <= 8);
		assertEquals(expected, actual.subList(start, start + expected.size()));
	}

	private static void assertPermutation(List<BankPreviewItem> input, List<BankPreviewItem> output)
	{
		Set<Integer> expected = new HashSet<>(ids(input));
		Set<Integer> actual = new HashSet<>(ids(output));
		assertEquals(input.size(), output.size());
		assertEquals(expected, actual);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			ids.add(item.getItemId());
		}
		return ids;
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return farming(itemId, name);
	}

	private static BankPreviewItem farming(int itemId, String name)
	{
		return categorized(itemId, name, ItemCategory.FARMING);
	}

	private static BankPreviewItem herblore(int itemId, String name)
	{
		return categorized(itemId, name, ItemCategory.HERBLORE);
	}

	private static BankPreviewItem categorized(int itemId, String name, ItemCategory category)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, category,
			category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}
}

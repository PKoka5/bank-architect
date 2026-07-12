package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourceItemSorterTest
{
	@Test
	public void followsMaterialAndProcessingFlows()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Ruby", "gem"), item(2, "Oak plank", "skilling"),
			item(3, "Iron bar", "skilling"), item(4, "Uncut ruby", "uncut-gem"),
			item(5, "Iron ore", "skilling"), item(6, "Oak logs", "skilling")
		));

		assertEquals(Arrays.asList("Iron ore", "Iron bar", "Oak logs", "Oak plank", "Uncut ruby", "Ruby"),
			names(sorted));
	}

	@Test
	public void misleadingFragmentsDoNotBecomeOreBarOrLogBlocks()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Raw barb-tailed kebbit", "skilling"), item(2, "Anima-infused bark", "resource"),
			item(3, "Smashed mirror", "skilling"), item(4, "Copper ore", "skilling"),
			item(5, "Iron bar", "skilling"), item(6, "Soda ash", "skilling"),
			item(7, "Vile ashes", "skilling"), item(8, "Chocolate bar", "cooking-material")
		));

		assertEquals(Arrays.asList("Copper ore", "Iron bar", "Anima-infused bark", "Soda ash",
			"Vile ashes", "Raw barb-tailed kebbit", "Chocolate bar", "Smashed mirror"), names(sorted));
	}

	@Test
	public void pairsLogsAndPlanksBySpeciesWithoutMetalNameLeakage()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Teak plank", "skilling"), item(2, "Oak plank", "skilling"),
			item(3, "Ironwood plank", "skilling"), item(4, "Teak logs", "skilling"),
			item(5, "Oak logs", "skilling")
		));

		assertEquals(Arrays.asList("Oak logs", "Oak plank", "Teak logs", "Teak plank",
			"Ironwood plank"), names(sorted));
	}

	@Test
	public void keepsGlassTextileAndNailWorkflowsContiguous()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Molten glass", "glass-material"), item(2, "Seaweed", "skilling"),
			item(3, "Vial", "glass-material"), item(4, "Soda ash", "skilling"),
			item(5, "Bucket of sand", "glass-material"), item(6, "Bow string", "textile"),
			item(7, "Flax", "skilling"), item(8, "Thread", "textile"),
			item(9, "Rune nails", "skilling"), item(10, "Bronze nails", "skilling"),
			item(11, "Steel nails", "skilling")
		));

		assertEquals(Arrays.asList("Seaweed", "Soda ash", "Bucket of sand", "Molten glass", "Vial",
			"Flax", "Bow string", "Thread", "Bronze nails", "Steel nails", "Rune nails"), names(sorted));
	}

	@Test
	public void gemNamedBoltTipsStayInAmmoComponentWorkflow()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Uncut ruby", "uncut-gem"), item(2, "Ruby", "gem"),
			item(3, "Ruby bolt tips", "ammo-component"), item(4, "Arrow shaft", "ammo-component")
		));

		assertEquals(Arrays.asList("Uncut ruby", "Ruby", "Arrow shaft", "Ruby bolt tips"), names(sorted));
	}

	private static BankPreviewItem item(int id, String name, String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.SKILLING, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

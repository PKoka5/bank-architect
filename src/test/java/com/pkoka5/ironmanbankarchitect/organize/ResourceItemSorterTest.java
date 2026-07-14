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
	public void canonicalWorkflowMetadataKeepsRenamedFamiliesInMicroSortOrder()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1605, "Renamed cut emerald", "unknown"),
			item(2355, "Renamed processed two", "unknown"),
			item(440, "Renamed raw one", "unknown"),
			item(1607, "Renamed cut sapphire", "unknown"),
			item(1621, "Renamed uncut emerald", "unknown"),
			item(442, "Renamed raw two", "unknown"),
			item(2351, "Renamed processed one", "unknown"),
			item(1623, "Renamed uncut sapphire", "unknown")
		));

		assertEquals(Arrays.asList(440, 442, 2351, 2355, 1623, 1621, 1607, 1605), ids(sorted));
	}

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

		// Zone order: Mining/Smithing, Woodcutting, Crafting (glass), Fishing/Cooking, Prayer, other.
		assertEquals(Arrays.asList("Copper ore", "Iron bar", "Anima-infused bark", "Soda ash",
			"Raw barb-tailed kebbit", "Chocolate bar", "Vile ashes", "Smashed mirror"), names(sorted));
	}

	@Test
	public void keepsLogsBeforeTheSeparatePlankRunWithoutMetalNameLeakage()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Teak plank", "skilling"), item(2, "Oak plank", "skilling"),
			item(3, "Ironwood plank", "skilling"), item(4, "Teak logs", "skilling"),
			item(5, "Oak logs", "skilling")
		));

		assertEquals(Arrays.asList("Oak logs", "Teak logs", "Oak plank", "Teak plank",
			"Ironwood plank"), names(sorted));
	}

	@Test
	public void sailingMetalsFollowTheClassicOreAndBarRuns()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Nickel ore", "skilling"), item(2, "Cupronickel bar", "skilling"),
			item(3, "Iron bar", "skilling"), item(4, "Gold ore", "skilling"),
			item(5, "Gold bar", "skilling"), item(6, "Iron ore", "skilling")));

		assertEquals(Arrays.asList("Iron ore", "Gold ore", "Iron bar", "Gold bar",
			"Nickel ore", "Cupronickel bar"), names(sorted));
	}

	@Test
	public void supplementalMiningMaterialsFollowClassicAndSailingMetalsInFallback()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(22603, "Basalt", "resource"), item(21543, "Calcite", "resource"),
			item(13573, "Dynamite", "resource"), item(21545, "Pyrophosphite", "resource"),
			item(13421, "Saltpetre", "resource"), item(21622, "Volcanic ash", "skilling"),
			item(2351, "Iron bar", "skilling"), item(2353, "Steel bar", "skilling"),
			item(2355, "Silver bar", "skilling"), item(2357, "Gold bar", "skilling"),
			item(2359, "Mithril bar", "skilling"), item(2361, "Adamantite bar", "skilling"),
			item(31719, "Nickel ore", "skilling"), item(32892, "Cupronickel bar", "skilling"),
			item(436, "Copper ore", "skilling"), item(440, "Iron ore", "skilling"),
			item(453, "Coal", "skilling"), item(442, "Silver ore", "skilling"),
			item(444, "Gold ore", "skilling"), item(447, "Mithril ore", "skilling"),
			item(449, "Adamantite ore", "skilling"), item(451, "Runite ore", "skilling")));

		assertEquals(Arrays.asList(
			"Copper ore", "Iron ore", "Coal", "Silver ore", "Gold ore", "Mithril ore",
			"Adamantite ore", "Runite ore",
			"Iron bar", "Steel bar", "Silver bar", "Gold bar", "Mithril bar", "Adamantite bar",
			"Nickel ore", "Cupronickel bar",
			"Basalt", "Calcite", "Dynamite", "Pyrophosphite", "Saltpetre", "Volcanic ash"),
			names(sorted));
	}

	@Test
	public void standardLogsAndPlanksPrecedeSpecialWood()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Camphor logs", "skilling"), item(2, "Oak plank", "skilling"),
			item(3, "Anima-infused bark", "resource"), item(4, "Teak logs", "skilling"),
			item(5, "Ironwood plank", "skilling"), item(6, "Oak logs", "skilling")));

		assertEquals(Arrays.asList("Oak logs", "Teak logs", "Oak plank", "Ironwood plank",
			"Camphor logs", "Anima-infused bark"), names(sorted));
	}

	@Test
	public void sawmillCouponsFollowThePlankRunInsideWoodcutting()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(32085, "Sawmill coupon (oak plank)", "currency"),
			item(1511, "Logs", "resource"),
			item(960, "Plank", "resource"),
			item(32083, "Sawmill coupon (wood plank)", "currency"),
			item(1521, "Oak logs", "resource"),
			item(8778, "Oak plank", "resource")));

		assertEquals(Arrays.asList("Logs", "Oak logs", "Plank", "Oak plank",
			"Sawmill coupon (oak plank)", "Sawmill coupon (wood plank)"), names(sorted));
	}

	@Test
	public void jewelleryNeverInterruptsRawAndCutGemStages()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Gold ring", "crafting-jewellery"), item(2, "Sapphire ring", "crafting-jewellery"),
			item(3, "Sapphire", "gem"), item(4, "Uncut sapphire", "uncut-gem"),
			item(5, "Emerald", "gem"), item(6, "Uncut emerald", "uncut-gem")));

		assertEquals(Arrays.asList("Uncut sapphire", "Uncut emerald", "Sapphire", "Emerald",
			"Sapphire ring", "Gold ring"), names(sorted));
	}

	@Test
	public void prayerResourcesFollowTrainingWorkflowOrder()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Bird's egg", "prayer-resource"), item(2, "Ensouled giant head", "prayer-resource"),
			item(3, "Loar remains", "skilling"), item(4, "Dragon bones", "skilling"),
			item(5, "Blessed bone shards", "skilling"), item(6, "Vile ashes", "skilling")));

		assertEquals(Arrays.asList("Blessed bone shards", "Dragon bones", "Ensouled giant head",
			"Loar remains", "Bird's egg", "Vile ashes"), names(sorted));
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

		// Glass and textile workflows stay contiguous inside Crafting; nails are Construction;
		// the bow string is an exact Fletching component and leaves the generic textile block.
		assertEquals(Arrays.asList("Seaweed", "Soda ash", "Bucket of sand", "Molten glass", "Vial",
			"Flax", "Thread", "Bronze nails", "Steel nails", "Rune nails", "Bow string"), names(sorted));
	}

	@Test
	public void mixedInputFollowsSkillZonesAndKeepsInternalWorkflows()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Bow string", "textile"), item(2, "Iron bar", "skilling"),
			item(3, "Dragon bones", "skilling"), item(4, "Oak plank", "skilling"),
			item(5, "Arrow shaft", "ammo-component"), item(6, "Uncut sapphire", "uncut-gem"),
			item(7, "Smashed mirror", "skilling"), item(8, "Iron ore", "skilling"),
			item(9, "Steel nails", "skilling"), item(10, "Raw shark", "raw-food"),
			item(11, "Oak logs", "skilling"), item(12, "Sapphire", "gem")
		));

		assertEquals(Arrays.asList(
			"Iron ore", "Iron bar",
			"Oak logs", "Oak plank",
			"Uncut sapphire", "Sapphire",
			"Steel nails",
			"Arrow shaft", "Bow string",
			"Raw shark",
			"Dragon bones",
			"Smashed mirror"), names(sorted));
	}

	@Test
	public void specialistZonesPrecedePrayerAndStableOtherResourceGroups()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(101, "Urt salt", "resource"), item(102, "Unidentified small fossil", "resource"),
			item(103, "Dragon bones", "skilling"), item(104, "Repair kit", "resource"),
			item(105, "Jerboa tail", "hunter-resource"), item(106, "Spirit flakes", "resource"),
			item(107, "Volcanic ash", "skilling"), item(108, "Basalt", "resource"),
			item(109, "Pheasant tail feathers", "resource"), item(110, "Fish offcuts", "resource"),
			item(111, "Teak repair kit", "resource"), item(112, "Efh salt", "resource"),
			item(113, "Unidentified medium fossil", "resource"), item(114, "Rope", "resource"),
			item(115, "Crystal shard", "resource")));

		assertEquals(Arrays.asList(
			"Basalt", "Volcanic ash",
			"Fish offcuts", "Spirit flakes",
			"Jerboa tail", "Pheasant tail feathers",
			"Repair kit", "Teak repair kit",
			"Dragon bones",
			"Efh salt", "Urt salt",
			"Unidentified medium fossil", "Unidentified small fossil",
			"Crystal shard", "Rope"), names(sorted));
	}

	@Test
	public void gemNamedBoltTipsStayInAmmoComponentWorkflow()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Uncut ruby", "uncut-gem"), item(2, "Ruby", "gem"),
			item(3, "Ruby bolt tips", "ammo-component"), item(4, "Arrow shaft", "ammo-component"),
			item(5, "Feather", "ammo-component"), item(6, "Headless arrow", "ammo-component")
		));

		assertEquals(Arrays.asList("Uncut ruby", "Ruby", "Arrow shaft", "Feather",
			"Headless arrow", "Ruby bolt tips"), names(sorted));
	}

	@Test
	public void keepsCraftingJewelleryAndBarbarianFishFamiliesTogether()
	{
		List<BankPreviewItem> sorted = ResourceItemSorter.sort(Arrays.asList(
			item(1, "Sapphire ring", "crafting-jewellery"), item(2, "Sapphire", "gem"),
			item(3, "Uncut sapphire", "uncut-gem"), item(4, "Leaping sturgeon", "raw-food"),
			item(5, "Leaping trout", "raw-food"), item(6, "Leaping salmon", "raw-food"),
			item(7, "Raw shark", "raw-food")
		));

		assertEquals(Arrays.asList("Uncut sapphire", "Sapphire", "Sapphire ring", "Raw shark",
			"Leaping trout", "Leaping salmon", "Leaping sturgeon"), names(sorted));
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

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

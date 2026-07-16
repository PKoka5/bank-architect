package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemRegistry;
import java.util.Collections;
import org.junit.Test;

public class ResourceSkillZoneClassifierTest
{
	@Test
	public void curatedMetadataFamiliesDecideFirstEvenWithMisleadingNamesAndSubcategories()
	{
		assertEquals(ResourceSkillZone.MINING_SMITHING,
			classify(440, "Renamed textile thing", "textile"));
		assertEquals(ResourceSkillZone.MINING_SMITHING,
			classify(2351, "Renamed processed one", "unknown"));
		assertEquals(ResourceSkillZone.WOODCUTTING,
			classify(1511, "Renamed base wood", "unknown"));
		assertEquals(ResourceSkillZone.WOODCUTTING,
			classify(960, "Renamed board", "construction-material"));
		assertEquals(ResourceSkillZone.CRAFTING,
			classify(1623, "Renamed rough stone", "unknown"));
		assertEquals(ResourceSkillZone.CRAFTING,
			classify(1607, "Renamed cut stone", "unknown"));
		assertEquals(ResourceSkillZone.FLETCHING,
			classify(39, "Renamed pointed thing", "unknown"));
		assertEquals(ResourceSkillZone.FLETCHING,
			classify(48, "Renamed curved thing", "unknown"));
	}

	@Test
	public void ammoComponentsAndExactStringsAreFletching()
	{
		assertEquals(ResourceSkillZone.FLETCHING, classify(1, "Arrow shaft", "ammo-component"));
		assertEquals(ResourceSkillZone.FLETCHING, classify(2, "Ruby bolt tips", "ammo-component"));
		assertEquals(ResourceSkillZone.FLETCHING, classify(3, "Feather", "ammo-component"));
		assertEquals(ResourceSkillZone.FLETCHING, classify(4, "Bow string", "textile"));
		assertEquals(ResourceSkillZone.FLETCHING, classify(5, "Crossbow string", "textile"));
	}

	@Test
	public void auditedBrokenAntlerUsesItsFletchingProductWorkflow()
	{
		// Wiki: https://oldschool.runescape.wiki/w/Broken_antler?oldid=15194017
		CatalogItem item = ResourceItemRegistry.INSTANCE.findById(31086).get();
		assertEquals("ammo-component", item.getSubcategory());
		assertEquals(ResourceSkillZone.FLETCHING,
			ResourceSkillZoneClassifier.classify(new BankPreviewItem(item, 1)));
	}

	@Test
	public void constructionMaterialsAreConstruction()
	{
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(1, "Limestone brick", "construction-material"));
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(2, "Steel nails", "skilling"));
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(3, "Soft clay", "skilling"));
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(4, "Limestone", "skilling"));
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(5, "Curved bone", "skilling"));
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(6, "Long bone", "skilling"));
	}

	@Test
	public void craftingSubcategoriesAndNamesAreCrafting()
	{
		assertEquals(ResourceSkillZone.CRAFTING, classify(1, "Uncut opal", "uncut-gem"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(2, "Opal", "gem"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(3, "Sapphire ring", "crafting-jewellery"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(4, "Molten glass", "glass-material"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(5, "Amethyst chunk", "crafting-material"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(6, "Ball of wool", "textile"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(7, "Green dragonhide", "skilling"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(8, "Hard leather", "skilling"));
		assertEquals(ResourceSkillZone.CRAFTING, classify(9, "Flax", "skilling"));
	}

	@Test
	public void metalAndWoodNameFactsKeepTheirPrimaryZones()
	{
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(1, "Adamantite ore", "skilling"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(2, "Coal", "skilling"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(3, "Steel bar", "skilling"));
		assertEquals(ResourceSkillZone.WOODCUTTING, classify(4, "Yew logs", "skilling"));
		assertEquals(ResourceSkillZone.WOODCUTTING, classify(5, "Mahogany plank", "skilling"));
		assertEquals(ResourceSkillZone.WOODCUTTING, classify(6, "Anima-infused bark", "resource"));
	}

	@Test
	public void prayerAndFishingCookingZones()
	{
		assertEquals(ResourceSkillZone.PRAYER, classify(1, "Dragon bones", "skilling"));
		assertEquals(ResourceSkillZone.PRAYER, classify(2, "Fiendish ashes", "skilling"));
		assertEquals(ResourceSkillZone.PRAYER, classify(3, "Ashes", "skilling"));
		assertEquals(ResourceSkillZone.PRAYER, classify(4, "Ensouled bear head", "prayer-resource"));
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(5, "Raw shark", "raw-food"));
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(6, "Leaping trout", "skilling"));
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(7, "Chocolate bar", "cooking-material"));
	}

	@Test
	public void misleadingNamesDoNotLeakAcrossZones()
	{
		// Glass workflow before the ash suffix: soda ash is Crafting, not Prayer.
		assertEquals(ResourceSkillZone.CRAFTING, classify(1, "Soda ash", "skilling"));
		// Excluded from the bar suffix: chocolate bar is a cooking material, not a metal.
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(2, "Chocolate bar", "cooking-material"));
		// Bow string wins Fletching before the generic textile subcategory rule.
		assertEquals(ResourceSkillZone.FLETCHING, classify(3, "Bow string", "textile"));
		// Raw hunter meat is Fishing/Cooking, not a bar or bone family.
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(4, "Raw barb-tailed kebbit", "skilling"));
		// Word boundaries: bonemeal-like fragments never become Prayer bones.
		assertEquals(ResourceSkillZone.OTHER_RESOURCE, classify(5, "Trombone sheet", "skilling"));
		assertEquals(ResourceSkillZone.OTHER_RESOURCE, classify(6, "Smashed mirror", "skilling"));
		assertEquals(ResourceSkillZone.OTHER_RESOURCE, classify(7, "Granite (5kg)", "resource"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(8, "Volcanic ash", "skilling"));
	}

	@Test
	public void specialistResourcesUseExactPrimarySkillZones()
	{
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(101, "Basalt", "resource"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(102, "Calcite", "resource"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(103, "Dynamite", "resource"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(104, "Pyrophosphite", "resource"));
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(105, "Saltpetre", "resource"));

		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(106, "Fish offcuts", "resource"));
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(107, "Fishing bait", "resource"));
		assertEquals(ResourceSkillZone.FISHING_COOKING, classify(108, "Spirit flakes", "resource"));

		assertEquals(ResourceSkillZone.HUNTER, classify(109, "Jerboa tail", "hunter-resource"));
		assertEquals(ResourceSkillZone.HUNTER, classify(110, "Kebbit claws", "hunter-resource"));
		assertEquals(ResourceSkillZone.HUNTER, classify(111, "Pheasant tail feathers", "resource"));
		assertEquals(ResourceSkillZone.HUNTER, classify(112, "Sunlight antelope antler", "resource"));

		assertEquals(ResourceSkillZone.SAILING, classify(113, "Large mahogany hull parts", "resource"));
		assertEquals(ResourceSkillZone.SAILING, classify(114, "Repair kit", "resource"));
		assertEquals(ResourceSkillZone.SAILING, classify(115, "Teak repair kit", "resource"));

		assertEquals(ResourceSkillZone.OTHER_RESOURCE, classify(116, "Rope", "resource"));
		assertEquals(ResourceSkillZone.OTHER_RESOURCE, classify(117, "Urt salt", "resource"));
	}

	@Test
	public void secondarySkillUseNeverMovesAnItemOutOfItsPrimaryZone()
	{
		// Fletching and Construction consume logs and planks; they stay Woodcutting.
		assertEquals(ResourceSkillZone.WOODCUTTING, classify(1, "Maple logs", "skilling"));
		assertEquals(ResourceSkillZone.WOODCUTTING, classify(2, "Teak plank", "skilling"));
		// Crafting consumes bars; they stay Mining/Smithing.
		assertEquals(ResourceSkillZone.MINING_SMITHING, classify(3, "Gold bar", "skilling"));
		// Fletching consumes gems as bolt tips only after cutting; gems stay Crafting.
		assertEquals(ResourceSkillZone.CRAFTING, classify(4, "Uncut ruby", "uncut-gem"));
		// Nails are driven by Construction even though Smithing produces them.
		assertEquals(ResourceSkillZone.CONSTRUCTION, classify(5, "Rune nails", "skilling"));
	}

	@Test
	public void exactRunecraftingResourcesUseReviewedPrimaryZones()
	{
		assertEquals(ResourceSkillZone.OTHER_RESOURCE,
			classify(7936, "Pure essence", "rune"));
		assertEquals(ResourceSkillZone.OTHER_RESOURCE,
			classify(24704, "Daeyalt essence", "rune"));
		assertEquals(ResourceSkillZone.WOODCUTTING,
			classify(32083, "Sawmill coupon (wood plank)", "currency"));
		assertEquals(ResourceSkillZone.WOODCUTTING,
			classify(32085, "Sawmill coupon (oak plank)", "currency"));
	}

	@Test
	public void questAuditResourcesRetainTheirReviewedProcessingZones()
	{
		// Full Wiki-page review confirms these remain repeatable Elemental Workshop
		// smithing materials rather than one-time quest leftovers.
		for (int itemId : new int[] {2892, 9727, 9728})
		{
			CatalogItem item = ResourceItemRegistry.INSTANCE.findById(itemId).get();
			assertEquals(ItemCategory.SKILLING, item.getCategory());
			assertEquals(ResourceSkillZone.MINING_SMITHING,
				ResourceSkillZoneClassifier.classify(new BankPreviewItem(item, 1)));
		}

		// Elemental metal (2893) is also a repeatable Smithing material, but the
		// existing classifier currently places it in OTHER_RESOURCE. The batch
		// forbids changing that classifier, so this is retained as a Group-B
		// skillzone follow-up instead of forcing a misleading classification.
		CatalogItem elementalMetal = ResourceItemRegistry.INSTANCE.findById(2893).get();
		assertEquals(ItemCategory.SKILLING, elementalMetal.getCategory());
		assertEquals(ResourceSkillZone.OTHER_RESOURCE,
			ResourceSkillZoneClassifier.classify(new BankPreviewItem(elementalMetal, 1)));

		CatalogItem rawFishcake = ResourceItemRegistry.INSTANCE.findById(7529).get();
		assertEquals(ResourceSkillZone.FISHING_COOKING,
			ResourceSkillZoneClassifier.classify(new BankPreviewItem(rawFishcake, 1)));
	}

	@Test
	public void completedQuestAuditKeepsRepeatableResourcesInTheirPrimaryZones()
	{
		assertRegistryZone(ResourceSkillZone.WOODCUTTING,
			2862, 10810, 24691, 32902);
		assertRegistryZone(ResourceSkillZone.MINING_SMITHING,
			668, 9076, 9077, 2365, 446);
		assertRegistryZone(ResourceSkillZone.PRAYER,
			3130, 3133, 3128, 3129, 3131, 3132, 3179, 3180);
		assertRegistryZone(ResourceSkillZone.FISHING_COOKING,
			29216, 7566, 3150, 2148, 4241);
		assertRegistryZone(ResourceSkillZone.CRAFTING, 10167, 3694);
		assertRegistryZone(ResourceSkillZone.FLETCHING, 2861);

		// "Split log" is a repeatable Woodcutting output, but the current,
		// deliberately untouched classifier recognises plural "... logs" only.
		assertRegistryZone(ResourceSkillZone.OTHER_RESOURCE, 10812);
	}

	private static void assertRegistryZone(ResourceSkillZone expected, int... itemIds)
	{
		for (int itemId : itemIds)
		{
			CatalogItem item = ResourceItemRegistry.INSTANCE.findById(itemId).get();
			assertEquals("zone for " + item.getDisplayName(), expected,
				ResourceSkillZoneClassifier.classify(new BankPreviewItem(item, 1)));
		}
	}

	private static ResourceSkillZone classify(int itemId, String name, String subcategory)
	{
		return ResourceSkillZoneClassifier.classify(new BankPreviewItem(
			new CatalogItem(itemId, name, ItemCategory.SKILLING, subcategory,
				Collections.emptySet(), null), 1));
	}
}

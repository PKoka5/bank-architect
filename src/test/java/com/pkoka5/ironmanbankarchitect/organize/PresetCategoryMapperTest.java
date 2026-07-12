package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Collections;
import org.junit.Test;

public class PresetCategoryMapperTest
{
	@Test
	public void ironmanMapsAllGearIntoSingleCombatGearCategory()
	{
		BankCategory category = PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.GEAR, "Abyssal whip"));

		assertEquals("combat-gear", category.getKey());
	}

	@Test
	public void ironmanMapsHerbloreAndFarmingTogether()
	{
		assertEquals("farming-herblore", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.HERBLORE, "Clean irit")).getKey());
		assertEquals("farming-herblore", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.FARMING, "Irit seed")).getKey());
	}

	@Test
	public void ironmanMapsToolsIntoSkillingToolsTab()
	{
		assertEquals("skilling-tools", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.TOOL, "Dragon pickaxe")).getKey());
		assertEquals("resources", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.SKILLING, "Iron ore")).getKey());
	}

	@Test
	public void ironmanKeepsValuableUniqueComponentsOutOfCleanup()
	{
		assertEquals("slayer-boss-loot", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.UNIQUE, "Pegasian crystal")).getKey());
	}

	@Test
	public void ironmanRoutesCluesToTheClueTab()
	{
		assertEquals("clues-cosmetics", PresetCategoryMapper.map(BankPresets.IRONMAN,
			item(ItemCategory.CLUE, "Clue scroll (hard)")).getKey());
	}

	@Test
	public void ironmanRoutesPartialStandardPotionsToHerbloreButKeepsFullPotionsInSupplies()
	{
		CatalogItem partial = new CatalogItem(1, "Prayer potion(3)", ItemCategory.POTION,
			"potion-dose-3", Collections.emptySet(), null);
		CatalogItem full = new CatalogItem(2, "Prayer potion(4)", ItemCategory.POTION,
			"potion-dose-4", Collections.emptySet(), null);

		assertEquals("farming-herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, partial).getKey());
		assertEquals("potions-food", PresetCategoryMapper.map(BankPresets.IRONMAN, full).getKey());
	}

	@Test
	public void ironmanSupportsLegacyDoseMetadataFromTheCuratedCatalog()
	{
		CatalogItem legacy = new CatalogItem(1, "Super attack (3)", ItemCategory.POTION,
			"dose-3", Collections.emptySet(), null);

		assertEquals("farming-herblore", PresetCategoryMapper.map(BankPresets.IRONMAN, legacy).getKey());
	}

	@Test
	public void pvmSeparatesRunesIntoMagicGear()
	{
		BankCategory category = PresetCategoryMapper.map(BankPresets.PVM,
			item(ItemCategory.RUNE, "Blood rune"));

		assertEquals("magic-gear", category.getKey());
	}

	@Test
	public void everyPresetKeepsUniqueAndClueItemsOutOfGenericJunkFallbacks()
	{
		assertEquals("boss-slayer-loot", PresetCategoryMapper.map(BankPresets.MAIN,
			item(ItemCategory.UNIQUE, "Pegasian crystal")).getKey());
		assertEquals("clues-collection-log", PresetCategoryMapper.map(BankPresets.MAIN,
			item(ItemCategory.CLUE, "Clue scroll (hard)")).getKey());
		assertEquals("loot-drops", PresetCategoryMapper.map(BankPresets.PVM,
			item(ItemCategory.UNIQUE, "Burnt page")).getKey());
		assertEquals("loot-keys-review", PresetCategoryMapper.map(BankPresets.PVP,
			item(ItemCategory.CLUE, "Clue scroll (elite)")).getKey());
		assertEquals("loot-clues-storage", PresetCategoryMapper.map(BankPresets.SKILLER,
			item(ItemCategory.UNIQUE, "Crystal weapon seed")).getKey());
	}

	private static CatalogItem item(ItemCategory category, String name)
	{
		return new CatalogItem(1, name, category, "test", Collections.emptySet(), null);
	}
}

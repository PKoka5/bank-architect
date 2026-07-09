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
	public void pvmSeparatesRunesIntoMagicGear()
	{
		BankCategory category = PresetCategoryMapper.map(BankPresets.PVM,
			item(ItemCategory.RUNE, "Blood rune"));

		assertEquals("magic-gear", category.getKey());
	}

	private static CatalogItem item(ItemCategory category, String name)
	{
		return new CatalogItem(1, name, category, "test", Collections.emptySet(), null);
	}
}

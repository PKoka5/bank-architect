package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class BankPresetsTest
{
	@Test
	public void eachPresetHasExactlyTenUniqueCategories()
	{
		for (BankPresetType type : BankPresetType.values())
		{
			BankPreset preset = BankPresets.forType(type);
			assertEquals(10, preset.getCategories().size());

			Set<String> keys = new HashSet<>();
			for (BankCategory category : preset.getCategories())
			{
				keys.add(category.getKey());
			}
			assertEquals(10, keys.size());
		}
	}

	@Test
	public void ironmanKeepsAllCombatGearInOneCategory()
	{
		BankPreset preset = BankPresets.IRONMAN;

		assertEquals("Combat Gear", preset.getCategory("combat-gear").getName());
		assertEquals(10, preset.getCategories().size());
	}

	@Test
	public void activeIronmanCategoryOrderRemainsStable()
	{
		List<String> keys = new ArrayList<>();
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			keys.add(category.getKey());
		}

		assertEquals(Arrays.asList("currency-utilities", "combat-gear", "potions-food",
			"herblore", "seeds-farming", "skilling-tools", "resources",
			"slayer-boss-loot", "clues-cosmetics", "storage-cleanup"), keys);
	}

	@Test
	public void accountPresetsReuseSemanticSortModesInsteadOfCategoryKeyNames()
	{
		assertEquals(BankCategorySortMode.SUPPLIES,
			BankPresets.PVP.getCategory("food-potions").getSortMode());
		assertEquals(BankCategorySortMode.GEAR,
			BankPresets.PVP.getCategory("replacement-sets").getSortMode());
		assertEquals(BankCategorySortMode.CLUES,
			BankPresets.MAIN.getCategory("clues-collection-log").getSortMode());
		assertEquals(BankCategorySortMode.TOOLS,
			BankPresets.SKILLER.getCategory("tools-outfits-pets").getSortMode());
		assertEquals(BankCategorySortMode.REVIEW,
			BankPresets.PVM.getCategory("low-use-review").getSortMode());
	}

	@Test
	public void mixedFutureCategoriesStayGenericUntilTheirRoutingIsComplete()
	{
		assertEquals(BankCategorySortMode.GENERIC,
			BankPresets.PVM.getCategory("magic-gear").getSortMode());
		assertEquals(BankCategorySortMode.GENERIC,
			BankPresets.PVP.getCategory("magic-pk-gear").getSortMode());
		assertEquals(BankCategorySortMode.GENERIC,
			BankPresets.SKILLER.getCategory("farming").getSortMode());
		assertEquals(BankCategorySortMode.GENERIC,
			BankPresets.SKILLER.getCategory("herblore-materials").getSortMode());
	}

	@Test
	public void legacyCategoryConstructorKeepsExistingSpecializedBehaviour()
	{
		assertEquals(BankCategorySortMode.GEAR,
			new BankCategory("combat-gear", "Combat Gear").getSortMode());
		assertEquals(BankCategorySortMode.SUPPLIES,
			new BankCategory("potions-food", "Supplies").getSortMode());
		assertEquals(BankCategorySortMode.GENERIC,
			new BankCategory("custom", "Custom").getSortMode());
	}
}

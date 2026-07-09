package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
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
}

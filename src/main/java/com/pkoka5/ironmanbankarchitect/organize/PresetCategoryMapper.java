package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;

public final class PresetCategoryMapper
{
	private PresetCategoryMapper()
	{
	}

	public static BankCategory map(BankPreset preset, CatalogItem item)
	{
		switch (preset.getType())
		{
			case IRONMAN:
				return mapIronman(preset, item);
			case MAIN:
				return mapMain(preset, item);
			case PVM:
				return mapPvm(preset, item);
			case PVP:
				return mapPvp(preset, item);
			case SKILLER:
				return mapSkiller(preset, item);
			default:
				throw new IllegalArgumentException("Unsupported preset type: " + preset.getType());
		}
	}

	private static BankCategory mapIronman(BankPreset preset, CatalogItem item)
	{
		ItemCategory category = item.getCategory();
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("currency-utilities");
		}
		if (category == ItemCategory.RUNE || category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("teleports-runes");
		}
		if (category == ItemCategory.GEAR)
		{
			return preset.getCategory("combat-gear");
		}
		if (category == ItemCategory.POTION)
		{
			if (isPartialPotionDose(item.getSubcategory()))
			{
				return preset.getCategory("farming-herblore");
			}
			return preset.getCategory("potions-food");
		}
		if (category == ItemCategory.FARMING || category == ItemCategory.HERBLORE)
		{
			return preset.getCategory("farming-herblore");
		}
		if (category == ItemCategory.TOOL)
		{
			return preset.getCategory("skilling-tools");
		}
		if (category == ItemCategory.SKILLING)
		{
			return preset.getCategory("resources");
		}
		if (category == ItemCategory.UNIQUE)
		{
			return preset.getCategory("slayer-boss-loot");
		}
		if (category == ItemCategory.CLUE)
		{
			return preset.getCategory("clues-cosmetics");
		}
		if (category == ItemCategory.CLEANUP || category == ItemCategory.UNKNOWN || category == ItemCategory.UNCATEGORIZED)
		{
			return preset.getCategory("storage-cleanup");
		}

		return preset.getCategory("storage-cleanup");
	}

	private static boolean isPartialPotionDose(String subcategory)
	{
		return "potion-dose-1".equals(subcategory) || "potion-dose-2".equals(subcategory)
			|| "potion-dose-3".equals(subcategory) || "dose-1".equals(subcategory)
			|| "dose-2".equals(subcategory) || "dose-3".equals(subcategory);
	}

	private static BankCategory mapMain(BankPreset preset, CatalogItem item)
	{
		ItemCategory category = item.getCategory();
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("currency-tradeables");
		}
		if (category == ItemCategory.RUNE || category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("teleports-runes");
		}
		if (category == ItemCategory.GEAR)
		{
			return preset.getCategory("combat-gear");
		}
		if (category == ItemCategory.POTION)
		{
			return preset.getCategory("potions-food");
		}
		if (category == ItemCategory.FARMING || category == ItemCategory.HERBLORE)
		{
			return preset.getCategory("farming-herblore");
		}
		if (category == ItemCategory.UNIQUE)
		{
			return preset.getCategory("boss-slayer-loot");
		}
		if (category == ItemCategory.CLUE)
		{
			return preset.getCategory("clues-collection-log");
		}
		if (category == ItemCategory.SKILLING || category == ItemCategory.TOOL)
		{
			return preset.getCategory("skilling-supplies");
		}
		if (category == ItemCategory.CLEANUP || category == ItemCategory.UNKNOWN || category == ItemCategory.UNCATEGORIZED)
		{
			return preset.getCategory("junk-review");
		}

		return preset.getCategory("junk-review");
	}

	private static BankCategory mapPvm(BankPreset preset, CatalogItem item)
	{
		ItemCategory category = item.getCategory();
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("currency-utilities");
		}
		if (category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("teleports-escapes");
		}
		if (category == ItemCategory.RUNE)
		{
			return preset.getCategory("magic-gear");
		}
		if (category == ItemCategory.POTION)
		{
			return preset.getCategory("potions-food");
		}
		if (category == ItemCategory.GEAR)
		{
			return preset.getCategory("melee-gear");
		}
		if (category == ItemCategory.UNIQUE || category == ItemCategory.CLUE)
		{
			return preset.getCategory("loot-drops");
		}
		if (category == ItemCategory.CLEANUP || category == ItemCategory.UNKNOWN || category == ItemCategory.UNCATEGORIZED)
		{
			return preset.getCategory("low-use-review");
		}

		return preset.getCategory("slayer-boss-tools");
	}

	private static BankCategory mapPvp(BankPreset preset, CatalogItem item)
	{
		ItemCategory category = item.getCategory();
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("coins-risk");
		}
		if (category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("teleports-escapes");
		}
		if (category == ItemCategory.RUNE)
		{
			return preset.getCategory("magic-pk-gear");
		}
		if (category == ItemCategory.POTION)
		{
			return preset.getCategory("food-potions");
		}
		if (category == ItemCategory.GEAR)
		{
			return preset.getCategory("melee-pk-gear");
		}
		if (category == ItemCategory.UNIQUE || category == ItemCategory.CLUE)
		{
			return preset.getCategory("loot-keys-review");
		}
		if (category == ItemCategory.CLEANUP || category == ItemCategory.UNKNOWN || category == ItemCategory.UNCATEGORIZED)
		{
			return preset.getCategory("loot-keys-review");
		}

		return preset.getCategory("wildy-tools");
	}

	private static BankCategory mapSkiller(BankPreset preset, CatalogItem item)
	{
		ItemCategory category = item.getCategory();
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("currency-utilities");
		}
		if (category == ItemCategory.RUNE || category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("teleports-runes");
		}
		if (category == ItemCategory.FARMING)
		{
			return preset.getCategory("farming");
		}
		if (category == ItemCategory.HERBLORE || category == ItemCategory.POTION)
		{
			return preset.getCategory("herblore-materials");
		}
		if (category == ItemCategory.SKILLING || category == ItemCategory.TOOL)
		{
			return preset.getCategory("tools-outfits-pets");
		}
		if (category == ItemCategory.UNIQUE || category == ItemCategory.CLUE)
		{
			return preset.getCategory("loot-clues-storage");
		}
		if (category == ItemCategory.CLEANUP || category == ItemCategory.GEAR || category == ItemCategory.UNKNOWN || category == ItemCategory.UNCATEGORIZED)
		{
			return preset.getCategory("loot-clues-storage");
		}

		return preset.getCategory("loot-clues-storage");
	}
}

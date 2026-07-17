package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

public final class PresetCategoryMapper
{
	private static final Set<Integer> IRONMAN_RESOURCE_IDS = ids(7936, 24704, 32083, 32085);
	private static final Set<Integer> IRONMAN_RUNECRAFTING_TOOL_IDS = ids(
		5509, 5510, 5511, 5512, 5513, 5514, 5515, 26784, 26786, 5521);
	private static final Set<Integer> IRONMAN_UTILITY_CONTAINER_IDS = ids(19634);
	private static final Set<Integer> IRONMAN_ACTIVITY_REWARD_IDS = ids(
		6183, 6529, 6306, 12012, 25527, 21555);
	private static final Set<Integer> IRONMAN_REVIEWED_TOOL_IDS = ids(13392, 25781);
	private static final Set<Integer> IRONMAN_REVIEWED_LOOT_IDS = ids(
		1201,
		ItemID.TOME_OF_FIRE_UNCHARGED,
		ItemID.TOME_OF_WATER_UNCHARGED,
		ItemID.TOME_OF_EARTH_UNCHARGED);
	private static final Set<Integer> IRONMAN_REVIEWED_CLEANUP_IDS = ids(762, 1588);

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
		if (IRONMAN_RESOURCE_IDS.contains(item.getItemId()))
		{
			return preset.getCategory("resources");
		}
		if (IRONMAN_RUNECRAFTING_TOOL_IDS.contains(item.getItemId())
			|| IRONMAN_UTILITY_CONTAINER_IDS.contains(item.getItemId())
			|| IRONMAN_REVIEWED_TOOL_IDS.contains(item.getItemId()))
		{
			return preset.getCategory("skilling-tools");
		}
		if (IRONMAN_REVIEWED_LOOT_IDS.contains(item.getItemId()))
		{
			return preset.getCategory("slayer-boss-loot");
		}
		if (IRONMAN_REVIEWED_CLEANUP_IDS.contains(item.getItemId()))
		{
			return preset.getCategory("storage-cleanup");
		}
		if (IRONMAN_ACTIVITY_REWARD_IDS.contains(item.getItemId()))
		{
			return preset.getCategory("clues-cosmetics");
		}
		if (IronmanMainTabPolicy.belongsOnMain(item))
		{
			return preset.getCategory("currency-utilities");
		}
		String setDomain = ItemSetCatalog.domainOf(item.getItemId()).orElse("");
		if ("gear".equals(setDomain))
		{
			return preset.getCategory("combat-gear");
		}
		if ("tools".equals(setDomain))
		{
			return preset.getCategory("skilling-tools");
		}
		if ("cosmetics".equals(setDomain))
		{
			return preset.getCategory("clues-cosmetics");
		}
		if (isRunecraftingFocus(item))
		{
			return preset.getCategory("skilling-tools");
		}
		if (isPartialPotionDose(item))
		{
			return preset.getCategory("herblore");
		}
		if (category == ItemCategory.CURRENCY)
		{
			return preset.getCategory("currency-utilities");
		}
		if (category == ItemCategory.RUNE || category == ItemCategory.TELEPORT)
		{
			return preset.getCategory("currency-utilities");
		}
		if (category == ItemCategory.GEAR)
		{
			return preset.getCategory("combat-gear");
		}
		if (category == ItemCategory.POTION)
		{
			return preset.getCategory("potions-food");
		}
		if (category == ItemCategory.HERBLORE
			|| category == ItemCategory.FARMING && "herb-seed".equals(item.getSubcategory()))
		{
			return preset.getCategory("herblore");
		}
		if (category == ItemCategory.FARMING)
		{
			return preset.getCategory("seeds-farming");
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

	private static boolean isRunecraftingFocus(CatalogItem item)
	{
		return "runecrafting-focus".equals(item.getSubcategory());
	}

	private static Set<Integer> ids(Integer... itemIds)
	{
		return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(itemIds)));
	}

	private static boolean isPartialPotionDose(CatalogItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind()
				== com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata.VariantKind.DOSE)
			.map(metadata -> metadata.getVariantValue() >= 1 && metadata.getVariantValue() <= 3)
			.orElseGet(() -> item.getCategory() == ItemCategory.POTION
				&& (item.getSubcategory().matches("(?:potion-)?dose-[123]")));
	}

	private static boolean isKnownFood(CatalogItem item)
	{
		if ("food".equals(item.getSubcategory()))
		{
			return true;
		}
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.map(metadata -> metadata.isFood())
			.orElse(false);
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
		if (category == ItemCategory.POTION && isKnownFood(item))
		{
			return preset.getCategory("fishing-cooking");
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

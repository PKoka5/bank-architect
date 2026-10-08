package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog;
import java.util.*;
import net.runelite.api.gameval.ItemID;

public final class PresetCategoryMapper
{
	private static final Set<Integer> IRONMAN_RESOURCE_IDS = ids(7936, 24704, 32083, 32085);
	private static final Set<Integer> IRONMAN_RUNECRAFTING_TOOL_IDS = ids(
		5509, 5510, 5511, 5512, 5513, 5514, 5515, 26784, 26786, 5521);
	private static final Set<Integer> IRONMAN_UTILITY_CONTAINER_IDS = ids(19634);
	private static final Set<Integer> IRONMAN_REVIEWED_TOOL_IDS = ids(13392, 25781);
	private static final Set<Integer> IRONMAN_REVIEWED_LOOT_IDS = ids(
		ItemID.TOME_OF_FIRE_UNCHARGED,
		ItemID.TOME_OF_WATER_UNCHARGED,
		ItemID.TOME_OF_EARTH_UNCHARGED);
	private static final Set<Integer> IRONMAN_REVIEWED_CLEANUP_IDS = ids(762, 1588);

	private PresetCategoryMapper()
	{
	}

	public static BankCategory map(BankPreset preset, CatalogItem item)
	{
		return map(preset, item, true);
	}

	/**
	 * Maps with the player's choice about the Frequently Used gathering: when
	 * off, nothing is hoisted to the quick-access main tab and every item
	 * files with its own category.
	 */
	public static BankCategory map(BankPreset preset, CatalogItem item, boolean gatherFrequentlyUsed)
	{
		return preset.getCategory(categoryKey(preset.getType(), item, gatherFrequentlyUsed));
	}

	private static String categoryKey(BankPresetType type, CatalogItem item, boolean gather)
	{
		switch (type)
		{
			case IRONMAN: return mapIronman(item, gather);
			case MAIN: return mapMain(item);
			case PVM:
			case PVP: return mapCombat(item.getCategory(), type == BankPresetType.PVP);
			case SKILLER: return mapSkiller(item);
			default: throw new IllegalArgumentException("Unsupported preset type: " + type);
		}
	}

	private static String mapIronman(CatalogItem item,
		boolean gatherFrequentlyUsed)
	{
		ItemCategory category = item.getCategory();
		if (IRONMAN_RESOURCE_IDS.contains(item.getItemId()))
		{
			return "resources";
		}
		if (IRONMAN_RUNECRAFTING_TOOL_IDS.contains(item.getItemId())
			|| IRONMAN_UTILITY_CONTAINER_IDS.contains(item.getItemId())
			|| IRONMAN_REVIEWED_TOOL_IDS.contains(item.getItemId()))
		{
			return "skilling-tools";
		}
		if (IRONMAN_REVIEWED_LOOT_IDS.contains(item.getItemId()))
		{
			return "slayer-boss-loot";
		}
		if (IRONMAN_REVIEWED_CLEANUP_IDS.contains(item.getItemId()))
		{
			return "storage-cleanup";
		}
		// Frog token redeems cosmetic clothing; spendable activity currencies use Currency.
		if (item.getItemId() == 6183)
		{
			return "clues-cosmetics";
		}
		if (gatherFrequentlyUsed && IronmanMainTabPolicy.belongsOnMain(item))
		{
			return "currency-utilities";
		}
		String setDomain = ItemSetCatalog.domainOf(item.getItemId()).orElse("");
		if ("gear".equals(setDomain))
		{
			return "combat-gear";
		}
		if ("tools".equals(setDomain))
		{
			return "skilling-tools";
		}
		if ("cosmetics".equals(setDomain))
		{
			return "clues-cosmetics";
		}
		if (isRunecraftingFocus(item))
		{
			return "skilling-tools";
		}
		if (isPartialPotionDose(item))
		{
			return "herblore";
		}
		switch (category)
		{
			case CURRENCY:
			case RUNE:
			case TELEPORT: return "currency-utilities";
			case GEAR: return "combat-gear";
			case POTION: return "potions-food";
			case HERBLORE: return "herblore";
			case FARMING:
				return "herb-seed".equals(item.getSubcategory()) ? "herblore" : "seeds-farming";
			case TOOL: return "skilling-tools";
			case SKILLING: return "resources";
			case UNIQUE: return "slayer-boss-loot";
			case CLUE: return "clues-cosmetics";
			default: return "storage-cleanup";
		}
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

	private static String mapMain(CatalogItem item)
	{
		if (item.getItemId() == 27641 || item.getItemId() == 22324 || item.getItemId() == 25734
			|| IRONMAN_REVIEWED_LOOT_IDS.contains(item.getItemId())) return "combat-gear";
		if (item.getItemId() == 9084) return "currency-utilities";
		if (isPartialPotionDose(item)) return "potions-food";
		return mapIronman(item, false);
	}

	private static String mapCombat(ItemCategory category, boolean pvp)
	{
		switch (category)
		{
			case CURRENCY: return pvp ? "coins-risk" : "currency-utilities";
			case TELEPORT: return "teleports-escapes";
			case RUNE: return pvp ? "magic-pk-gear" : "magic-gear";
			case POTION: return pvp ? "food-potions" : "potions-food";
			case GEAR: return pvp ? "melee-pk-gear" : "melee-gear";
			case UNIQUE:
			case CLUE: return pvp ? "loot-keys-review" : "loot-drops";
			case CLEANUP:
			case UNKNOWN:
			case UNCATEGORIZED: return pvp ? "loot-keys-review" : "low-use-review";
			default: return pvp ? "wildy-tools" : "slayer-boss-tools";
		}
	}

	private static String mapSkiller(CatalogItem item)
	{
		switch (item.getCategory())
		{
			case CURRENCY: return "currency-utilities";
			case RUNE:
			case TELEPORT: return "teleports-runes";
			case FARMING: return "farming";
			case POTION: return isKnownFood(item) ? "fishing-cooking" : "herblore-materials";
			case HERBLORE: return "herblore-materials";
			case SKILLING:
			case TOOL: return "tools-outfits-pets";
			default: return "loot-clues-storage";
		}
	}
}

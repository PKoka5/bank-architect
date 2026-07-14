package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class BankPresets
{
	public static final BankPreset IRONMAN = preset(BankPresetType.IRONMAN, "ironman.all-round", "Ironman - All-Round Bank",
		category("currency-utilities", "Frequently Used, Runes & Teleports", BankCategorySortMode.MAIN),
		category("combat-gear", "Combat Gear", BankCategorySortMode.GEAR),
		category("potions-food", "Potions, Food & PvM Supplies", BankCategorySortMode.SUPPLIES),
		category("herblore", "Herblore & Potion Making", BankCategorySortMode.HERBLORE),
		category("seeds-farming", "Seeds & Farming", BankCategorySortMode.FARMING),
		category("skilling-tools", "Skilling Tools", BankCategorySortMode.TOOLS),
		category("resources", "Raw & Processed Resources", BankCategorySortMode.RESOURCES),
		category("slayer-boss-loot", "Slayer, Boss Loot & Unique Drops", BankCategorySortMode.BOSS_LOOT),
		category("clues-cosmetics", "Clues, Cosmetics & Collection Log", BankCategorySortMode.CLUES),
		category("storage-cleanup", "Storage & Cleanup Review", BankCategorySortMode.REVIEW));

	public static final BankPreset MAIN = preset(BankPresetType.MAIN, "main.general", "Main - General Bank",
		category("currency-tradeables", "Currency & Tradeables", BankCategorySortMode.CURRENCY),
		category("teleports-runes", "Teleports & Runes", BankCategorySortMode.TELEPORTS),
		category("combat-gear", "Combat Gear", BankCategorySortMode.GEAR),
		category("potions-food", "Potions & Food", BankCategorySortMode.SUPPLIES),
		category("skilling-supplies", "Skilling Supplies", BankCategorySortMode.GENERIC),
		category("farming-herblore", "Farming & Herblore", BankCategorySortMode.HERBLORE),
		category("boss-slayer-loot", "Bossing & Slayer Loot", BankCategorySortMode.BOSS_LOOT),
		category("clues-collection-log", "Clues & Collection Log", BankCategorySortMode.CLUES),
		category("cosmetics-outfits", "Cosmetics & Outfits", BankCategorySortMode.GENERIC),
		category("junk-review", "Junk, Sell & Storage Review", BankCategorySortMode.REVIEW));

	public static final BankPreset PVM = preset(BankPresetType.PVM, "pvm.general", "PvM Bank",
		category("currency-utilities", "Core Currency & Utilities", BankCategorySortMode.CURRENCY),
		category("teleports-escapes", "Teleports & Escape Items", BankCategorySortMode.TELEPORTS),
		category("potions-food", "Potions, Food & Restores", BankCategorySortMode.SUPPLIES),
		category("melee-gear", "Melee Gear", BankCategorySortMode.GEAR),
		category("ranged-gear", "Ranged Gear & Ammo", BankCategorySortMode.GEAR),
		category("magic-gear", "Magic Gear & Runes", BankCategorySortMode.GENERIC),
		category("spec-switches", "Spec Weapons & Switches", BankCategorySortMode.GEAR),
		category("slayer-boss-tools", "Slayer & Boss Tools", BankCategorySortMode.GENERIC),
		category("loot-drops", "Loot, Drops & Splits", BankCategorySortMode.BOSS_LOOT),
		category("low-use-review", "Low-Use Gear & Review", BankCategorySortMode.REVIEW));

	public static final BankPreset PVP = preset(BankPresetType.PVP, "pvp.general", "PvP Bank",
		category("coins-risk", "Coins, Risk & Utility", BankCategorySortMode.CURRENCY),
		category("teleports-escapes", "Teleports, Escapes & Return Sets", BankCategorySortMode.TELEPORTS),
		category("food-potions", "Food, Potions & Combo Eats", BankCategorySortMode.SUPPLIES),
		category("melee-pk-gear", "Melee PK Gear", BankCategorySortMode.GEAR),
		category("ranged-pk-gear", "Ranged PK Gear & Ammo", BankCategorySortMode.GEAR),
		category("magic-pk-gear", "Magic PK Gear & Runes", BankCategorySortMode.GENERIC),
		category("spec-ko", "Spec Weapons & KO Items", BankCategorySortMode.GEAR),
		category("wildy-tools", "Wilderness Tools & Supplies", BankCategorySortMode.GENERIC),
		category("replacement-sets", "Replacement Sets", BankCategorySortMode.GEAR),
		category("loot-keys-review", "Loot, Keys & Review", BankCategorySortMode.REVIEW));

	public static final BankPreset SKILLER = preset(BankPresetType.SKILLER, "skiller.general", "Skiller Bank",
		category("currency-utilities", "Currency & Utilities", BankCategorySortMode.CURRENCY),
		category("teleports-runes", "Teleports & Runes", BankCategorySortMode.TELEPORTS),
		category("farming", "Farming", BankCategorySortMode.GENERIC),
		category("herblore-materials", "Herblore Materials", BankCategorySortMode.GENERIC),
		category("fishing-cooking", "Fishing & Cooking", BankCategorySortMode.RESOURCES),
		category("woodcutting-fletching", "Woodcutting & Fletching", BankCategorySortMode.RESOURCES),
		category("mining-smithing", "Mining & Smithing", BankCategorySortMode.RESOURCES),
		category("crafting-rc-construction", "Crafting, RC & Construction", BankCategorySortMode.RESOURCES),
		category("tools-outfits-pets", "Tools, Outfits & Pets", BankCategorySortMode.TOOLS),
		category("loot-clues-storage", "Loot, Clues & Storage Review", BankCategorySortMode.REVIEW));

	private static final Map<BankPresetType, BankPreset> BY_TYPE = buildByType();

	private BankPresets()
	{
	}

	public static BankPreset forType(BankPresetType type)
	{
		BankPreset preset = BY_TYPE.get(type);
		if (preset == null)
		{
			throw new IllegalArgumentException("Unknown preset type: " + type);
		}

		return preset;
	}

	private static Map<BankPresetType, BankPreset> buildByType()
	{
		Map<BankPresetType, BankPreset> presets = new EnumMap<>(BankPresetType.class);
		for (BankPreset preset : Arrays.asList(IRONMAN, MAIN, PVM, PVP, SKILLER))
		{
			presets.put(preset.getType(), preset);
		}

		return Collections.unmodifiableMap(presets);
	}

	private static BankPreset preset(BankPresetType type, String key, String name, BankCategory... categories)
	{
		return new BankPreset(type, key, name, Arrays.asList(categories));
	}

	private static BankCategory category(String key, String name, BankCategorySortMode sortMode)
	{
		return new BankCategory(key, name, sortMode);
	}
}

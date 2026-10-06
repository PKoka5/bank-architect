package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;




public final class BankPresets
{
	public static final BankPreset IRONMAN = allRound(false);
	public static final BankPreset MAIN = allRound(true);

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

	private static BankPreset allRound(boolean main)
	{
		return preset(main ? BankPresetType.MAIN : BankPresetType.IRONMAN,
			main ? "main.general" : "ironman.all-round", main ? "Main - All-Round Bank" : "Ironman - All-Round Bank",
			category("currency-utilities", main ? "Currency, Runes & Teleports" : "Frequently Used, Runes & Teleports", BankCategorySortMode.MAIN),
			category("combat-gear", "Combat Gear", BankCategorySortMode.GEAR),
			category("potions-food", main ? "Potions & Food" : "Potions, Food & PvM Supplies", BankCategorySortMode.SUPPLIES),
			category("herblore", main ? "Herblore" : "Herblore & Potion Making", BankCategorySortMode.HERBLORE),
			category("seeds-farming", "Seeds & Farming", BankCategorySortMode.FARMING),
			category("skilling-tools", "Skilling Tools", BankCategorySortMode.TOOLS),
			category("resources", "Raw & Processed Resources", BankCategorySortMode.RESOURCES),
			category("slayer-boss-loot", main ? "Bossing & Slayer Loot" : "Slayer, Boss Loot & Unique Drops", BankCategorySortMode.BOSS_LOOT),
			category("clues-cosmetics", "Clues, Cosmetics & Collection Log", BankCategorySortMode.CLUES),
			category("storage-cleanup", "Storage & Cleanup Review", BankCategorySortMode.REVIEW));
	}

	private static BankCategory category(String key, String name, BankCategorySortMode sortMode)
	{
		return new BankCategory(key, name, sortMode);
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class BankPresets
{
	public static final BankPreset IRONMAN = preset(BankPresetType.IRONMAN, "ironman.all-round", "Ironman - All-Round Bank",
		category("currency-utilities", "Currency & Account Utilities"),
		category("teleports-runes", "Teleports, Runes & Jewellery"),
		category("combat-gear", "Combat Gear"),
		category("potions-food", "Potions, Food & PvM Supplies"),
		category("farming-herblore", "Farming & Herblore"),
		category("skilling-tools", "Skilling Tools"),
		category("resources", "Raw & Processed Resources"),
		category("slayer-boss-loot", "Slayer, Boss Loot & Unique Drops"),
		category("clues-cosmetics", "Clues, Cosmetics & Collection Log"),
		category("storage-cleanup", "Storage & Cleanup Review"));

	public static final BankPreset MAIN = preset(BankPresetType.MAIN, "main.general", "Main - General Bank",
		category("currency-tradeables", "Currency & Tradeables"),
		category("teleports-runes", "Teleports & Runes"),
		category("combat-gear", "Combat Gear"),
		category("potions-food", "Potions & Food"),
		category("skilling-supplies", "Skilling Supplies"),
		category("farming-herblore", "Farming & Herblore"),
		category("boss-slayer-loot", "Bossing & Slayer Loot"),
		category("clues-collection-log", "Clues & Collection Log"),
		category("cosmetics-outfits", "Cosmetics & Outfits"),
		category("junk-review", "Junk, Sell & Storage Review"));

	public static final BankPreset PVM = preset(BankPresetType.PVM, "pvm.general", "PvM Bank",
		category("currency-utilities", "Core Currency & Utilities"),
		category("teleports-escapes", "Teleports & Escape Items"),
		category("potions-food", "Potions, Food & Restores"),
		category("melee-gear", "Melee Gear"),
		category("ranged-gear", "Ranged Gear & Ammo"),
		category("magic-gear", "Magic Gear & Runes"),
		category("spec-switches", "Spec Weapons & Switches"),
		category("slayer-boss-tools", "Slayer & Boss Tools"),
		category("loot-drops", "Loot, Drops & Splits"),
		category("low-use-review", "Low-Use Gear & Review"));

	public static final BankPreset PVP = preset(BankPresetType.PVP, "pvp.general", "PvP Bank",
		category("coins-risk", "Coins, Risk & Utility"),
		category("teleports-escapes", "Teleports, Escapes & Return Sets"),
		category("food-potions", "Food, Potions & Combo Eats"),
		category("melee-pk-gear", "Melee PK Gear"),
		category("ranged-pk-gear", "Ranged PK Gear & Ammo"),
		category("magic-pk-gear", "Magic PK Gear & Runes"),
		category("spec-ko", "Spec Weapons & KO Items"),
		category("wildy-tools", "Wilderness Tools & Supplies"),
		category("replacement-sets", "Replacement Sets"),
		category("loot-keys-review", "Loot, Keys & Review"));

	public static final BankPreset SKILLER = preset(BankPresetType.SKILLER, "skiller.general", "Skiller Bank",
		category("currency-utilities", "Currency & Utilities"),
		category("teleports-runes", "Teleports & Runes"),
		category("farming", "Farming"),
		category("herblore-materials", "Herblore Materials"),
		category("fishing-cooking", "Fishing & Cooking"),
		category("woodcutting-fletching", "Woodcutting & Fletching"),
		category("mining-smithing", "Mining & Smithing"),
		category("crafting-rc-construction", "Crafting, RC & Construction"),
		category("tools-outfits-pets", "Tools, Outfits & Pets"),
		category("loot-clues-storage", "Loot, Clues & Storage Review"));

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

	private static BankCategory category(String key, String name)
	{
		return new BankCategory(key, name);
	}
}

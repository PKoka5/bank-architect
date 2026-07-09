package com.pkoka5.ironmanbankarchitect.organize;

final class GearItemSorter
{
	private GearItemSorter()
	{
	}

	static int rank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		return slotRank(name) * 100 + styleRank(name);
	}

	private static int slotRank(String name)
	{
		if (containsAny(name, "helmet", "helm", "coif", "hat", "mask", "hood"))
		{
			return 0;
		}
		if (containsAny(name, "body", "platebody", "robe top", "hauberk", "torso", "chestplate"))
		{
			return 1;
		}
		if (containsAny(name, "legs", "platelegs", "plateskirt", "chaps", "robe bottom", "skirt", "tassets"))
		{
			return 2;
		}
		if (containsAny(name, "cape", "cloak", "ava's", "avas", "accumulator", "assembler"))
		{
			return 3;
		}
		if (containsAny(name, "amulet", "necklace", "symbol", "stole"))
		{
			return 4;
		}
		if (containsAny(name, "shield", "defender", "book", "ward", "offhand"))
		{
			return 5;
		}
		if (containsAny(name, "gloves", "vambraces", "bracelet"))
		{
			return 6;
		}
		if (containsAny(name, "boots"))
		{
			return 7;
		}
		if (containsAny(name, "sword", "scimitar", "mace", "dagger", "spear", "halberd", "whip",
			"maul", "warhammer", "battleaxe", "hasta", "rapier", "salamander", "flail"))
		{
			return 8;
		}
		if (containsAny(name, "bow", "crossbow", "ballista", "blowpipe"))
		{
			return 9;
		}
		if (containsAny(name, "staff", "wand", "trident", "sceptre", "scepter"))
		{
			return 10;
		}
		if (containsAny(name, "arrow", "bolt", "dart", "javelin", "cannonball", "chinchompa", "bolt rack"))
		{
			return 11;
		}

		return 12;
	}

	private static int styleRank(String name)
	{
		if (isMelee(name))
		{
			return 0;
		}
		if (isRanged(name))
		{
			return 1;
		}
		if (isMagic(name))
		{
			return 2;
		}

		return 3;
	}

	private static boolean isMelee(String name)
	{
		return containsAny(name, "rune", "dragon", "barrows", "bandos", "torva", "obsidian", "fighter",
			"berserker", "defender", "scimitar", "whip", "mace", "spear", "halberd", "warhammer",
			"battleaxe", "maul", "hasta", "rapier", "platebody", "platelegs", "plateskirt", "helm");
	}

	private static boolean isRanged(String name)
	{
		return containsAny(name, "bow", "crossbow", "ballista", "blowpipe", "arrow", "bolt", "dart",
			"javelin", "chinchompa", "coif", "chaps", "vambraces", "leather", "d'hide", "dragonhide",
			"karil", "armadyl", "ava's", "avas", "accumulator", "assembler");
	}

	private static boolean isMagic(String name)
	{
		return containsAny(name, "staff", "wand", "trident", "sceptre", "scepter", "mystic", "ahrim",
			"ancestral", "infinity", "wizard", "splitbark", "lunar", "xerician", "ghostly", "robe",
			"occult", "tome");
	}

	private static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle))
			{
				return true;
			}
		}

		return false;
	}

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

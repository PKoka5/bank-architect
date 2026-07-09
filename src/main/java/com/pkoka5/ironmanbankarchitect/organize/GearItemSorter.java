package com.pkoka5.ironmanbankarchitect.organize;

public final class GearItemSorter
{
	private GearItemSorter()
	{
	}

	static int rank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		return slotRank(item) * 10000 + styleRank(item) * 1000 + tierRank(item);
	}

	public static int slotRank(BankPreviewItem item)
	{
		return slotRank(normalizedName(item.getDisplayName()));
	}

	public static int styleRank(BankPreviewItem item)
	{
		return styleRank(normalizedName(item.getDisplayName()));
	}

	public static int tierRank(BankPreviewItem item)
	{
		return tierRank(normalizedName(item.getDisplayName()));
	}

	public static String slotLabel(int slotRank)
	{
		if (slotRank == 0)
		{
			return "Head";
		}
		if (slotRank == 1)
		{
			return "Body";
		}
		if (slotRank == 2)
		{
			return "Legs";
		}
		if (slotRank == 3)
		{
			return "Cape";
		}
		if (slotRank == 4)
		{
			return "Neck";
		}
		if (slotRank == 5)
		{
			return "Offhand";
		}
		if (slotRank == 6)
		{
			return "Hands";
		}
		if (slotRank == 7)
		{
			return "Feet";
		}
		if (slotRank == 8)
		{
			return "Weapon";
		}
		if (slotRank == 9)
		{
			return "Ranged weapon";
		}
		if (slotRank == 10)
		{
			return "Magic weapon";
		}
		if (slotRank == 11)
		{
			return "Ammo";
		}

		return "Other";
	}

	public static String styleLabel(int styleRank)
	{
		if (styleRank == 0)
		{
			return "Melee";
		}
		if (styleRank == 1)
		{
			return "Ranged";
		}
		if (styleRank == 2)
		{
			return "Magic";
		}

		return "Other";
	}

	public static boolean isSetupSlot(BankPreviewItem item)
	{
		int slotRank = slotRank(item);
		return slotRank >= 0 && slotRank <= 11;
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

	private static int tierRank(String name)
	{
		if (containsAny(name, "torva", "masori", "ancestral", "shadow", "tbow", "twisted bow",
			"scythe", "sanguinesti", "tumeken", "zaryte", "voidwaker"))
		{
			return 0;
		}
		if (containsAny(name, "bandos", "armadyl", "ahrim", "karil", "verac", "dharok", "guthan",
			"torag", "serpentine", "faceguard", "ferocious", "primordial", "pegasian", "eternal",
			"occult", "zenyte", "anguish", "tormented", "avernic", "toxic blowpipe", "trident"))
		{
			return 10;
		}
		if (containsAny(name, "barrows", "crystal", "blessed", "dragon", "obsidian", "fighter torso",
			"abyssal", "whip", "blowpipe", "mystic", "infinity", "malediction", "odium"))
		{
			return 20;
		}
		if (containsAny(name, "rune", "black d'hide", "red d'hide", "blue d'hide", "green d'hide",
			"splitbark", "iban", "warped", "magic shortbow"))
		{
			return 40;
		}
		if (containsAny(name, "adamant", "mithril", "steel", "iron", "bronze"))
		{
			return 70;
		}

		return 50;
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

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import java.util.Optional;
import java.util.OptionalInt;

/** Combat style, slot, lifecycle, and strength facts used by loadout planning. */
final class CombatGearRanking
{
	static final GearStyle[] LOADOUT_STYLES = {
		GearStyle.MELEE, GearStyle.RANGED, GearStyle.MAGIC,
		GearStyle.PRAYER, GearStyle.OTHER
	};

	private static final int GEAR_TIER_SCORE_STEP = 200;

	private CombatGearRanking()
	{
	}

	static int legacyRank(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		int slot = legacySlot(name);
		return slot == 11 ? 1300 : slot * 100 + legacyStyle(name).sortOrder();
	}

	static int score(BankPreviewItem item, GearStatsSource gearStats)
	{
		return score(item, gearStats, false);
	}

	static int score(BankPreviewItem item, GearStatsSource gearStats, boolean activeLoadoutMember)
	{
		int utility = activeLoadoutMember
			? CombatGearUtilityCatalog.INSTANCE.activeItemScore(item.getItemId())
			: CombatGearUtilityCatalog.INSTANCE.itemScore(item.getItemId());
		int score = progressionScore(item) + utility;
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		return stats.isPresent() ? score + stats.get().score() : score;
	}

	static GearStyle style(BankPreviewItem item, GearStatsSource gearStats)
	{
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			if (stats.get().getSlot() == GearSlot.RING)
			{
				return GearStyle.OTHER;
			}
			return stats.get().style();
		}
		return legacyStyle(normalizedName(item.getDisplayName()));
	}

	static int slot(BankPreviewItem item, GearStatsSource gearStats)
	{
		if (isAmmunitionName(normalizedName(item.getDisplayName())))
		{
			return 10;
		}
		Optional<GearStats> stats = gearStats.statsFor(item.getItemId());
		if (stats.isPresent())
		{
			switch (stats.get().getSlot())
			{
				case HEAD: return 0;
				case BODY: return 1;
				case LEGS: return 2;
				case WEAPON: return 3;
				case CAPE: return 4;
				case NECK: return 5;
				case HANDS: return 6;
				case FEET: return 7;
				case SHIELD: return 8;
				case RING: return 9;
				case AMMO: return 10;
				default: return 11;
			}
		}

		int legacySlot = legacySlot(normalizedName(item.getDisplayName()));
		if (legacySlot <= 2) return legacySlot;
		if (legacySlot >= 8 && legacySlot <= 10) return 3;
		if (legacySlot >= 3 && legacySlot <= 7) return legacySlot + 1;
		if (legacySlot == 11) return 10;
		return 11;
	}

	static int ammunitionFamily(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		if (containsAny(name, "arrow", "brutal")) return 0;
		if (containsAny(name, "bolt", "bolt rack")) return 10;
		if (name.contains("dart")) return 20;
		if (name.contains("javelin")) return 30;
		if (containsAny(name, "knife", "thrownaxe", "throwing axe")) return 40;
		if (name.contains("chinchompa")) return 50;
		if (name.contains("cannonball")) return 60;
		if (name.contains("grapple")) return 70;
		return 80;
	}

	static int ammunitionTier(BankPreviewItem item)
	{
		String name = normalizedName(item.getDisplayName());
		String[] tiers = {"diamond", "dragon", "amethyst", "rune", "adamant", "broad",
			"mithril", "bone", "steel", "iron", "bronze"};
		for (int index = 0; index < tiers.length; index++)
		{
			if (name.contains(tiers[index])) return index;
		}
		return 100;
	}

	static boolean unusable(BankPreviewItem item)
	{
		if (CombatGearFacts.unusableItemIds().contains(item.getItemId()))
		{
			return true;
		}
		String name = normalizedName(item.getDisplayName());
		return containsWord(name, "broken") || containsWord(name, "damaged")
			|| containsWord(name, "inactive");
	}

	static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}

	private static int legacySlot(String name)
	{
		if (containsAny(name, "helmet", "helm", "coif", "hat", "mask", "hood")) return 0;
		if (containsAny(name, "body", "platebody", "robe top", "hauberk", "torso", "chestplate")) return 1;
		if (containsAny(name, "legs", "platelegs", "plateskirt", "chaps", "robe bottom", "skirt",
			"tassets", "greaves")) return 2;
		if (containsAny(name, "cape", "cloak", "ava's", "avas", "accumulator", "assembler")) return 3;
		if (containsAny(name, "amulet", "necklace", "symbol", "stole")) return 4;
		if (containsAny(name, "shield", "defender", "book", "ward", "offhand")) return 5;
		if (containsAny(name, "gloves", "vambraces", "bracelet")) return 6;
		if (name.contains("boots")) return 7;
		if (containsAny(name, "sword", "scimitar", "mace", "macuahuitl", "dagger", "spear",
			"halberd", "whip", "maul", "warhammer", "battleaxe", "hasta", "rapier", "salamander", "flail")) return 8;
		if (containsAny(name, "bow", "crossbow", "ballista", "blowpipe", "atlatl")) return 9;
		if (containsAny(name, "staff", "wand", "trident", "sceptre", "scepter")) return 10;
		if (isAmmunitionName(name)) return 11;
		return 12;
	}

	private static boolean isAmmunitionName(String name)
	{
		return containsAnyWord(name, "arrow", "arrows", "bolt", "bolts", "dart", "darts",
			"javelin", "javelins", "cannonball", "cannonballs", "chinchompa",
			"chinchompas", "grapple", "knife", "knives", "thrownaxe", "thrownaxes")
			|| name.contains("bolt rack") || name.contains("throwing axe");
	}

	private static GearStyle legacyStyle(String name)
	{
		if (isRanged(name)) return GearStyle.RANGED;
		if (isMagic(name)) return GearStyle.MAGIC;
		if (isMelee(name)) return GearStyle.MELEE;
		if (isPrayer(name)) return GearStyle.PRAYER;
		return GearStyle.OTHER;
	}

	private static boolean containsWord(String value, String word)
	{
		int from = 0;
		while (from < value.length())
		{
			int index = value.indexOf(word, from);
			if (index < 0)
			{
				return false;
			}
			int end = index + word.length();
			boolean startsAtBoundary = index == 0 || !Character.isLetterOrDigit(value.charAt(index - 1));
			boolean endsAtBoundary = end == value.length() || !Character.isLetterOrDigit(value.charAt(end));
			if (startsAtBoundary && endsAtBoundary)
			{
				return true;
			}
			from = index + 1;
		}
		return false;
	}

	private static boolean isPrayer(String name)
	{
		return containsAny(name, "proselyte", "initiate", "monk's", "holy symbol", "holy sandals");
	}

	private static boolean isMelee(String name)
	{
		return containsAny(name, "blood moon", "rune", "dragon", "barrows", "bandos", "torva",
			"obsidian", "inquisitor", "fighter", "berserker", "defender", "scimitar", "whip", "macuahuitl",
			"mace", "spear", "halberd", "warhammer", "battleaxe", "maul", "hasta", "rapier",
			"platebody", "platelegs", "plateskirt", "helm", "neitiznot", "serpentine", "faceguard",
			"granite", "justiciar", "verac", "dharok", "guthan", "torag", "barrows gloves");
	}

	private static boolean isRanged(String name)
	{
		return containsAny(name, "crystal", "eclipse moon", "atlatl", "bow", "crossbow", "ballista",
			"blowpipe", "arrow", "bolt", "dart", "javelin", "chinchompa", "coif", "chaps",
			"vambraces", "leather", "d'hide", "dragonhide", "karil", "armadyl", "masori",
			"ava's", "avas", "accumulator", "assembler");
	}

	private static boolean isMagic(String name)
	{
		return containsAny(name, "blue moon", "staff", "wand", "trident", "sceptre", "scepter",
			"mystic", "ahrim", "ancestral", "virtus", "infinity", "wizard", "splitbark", "swampbark", "bloodbark",
			"lunar", "xerician", "ghostly", "robe", "occult", "tome");
	}

	private static int progressionScore(BankPreviewItem item)
	{
		OptionalInt tier = GearTierCatalog.INSTANCE.tierOf(item.getItemId(), item.getDisplayName());
		if (tier.isPresent()) return tier.getAsInt() * GEAR_TIER_SCORE_STEP;

		String name = normalizedName(item.getDisplayName());
		int score = 0;
		score = Math.max(score, scoreIfContains(name, 1000, "torva", "ancestral", "masori", "tumeken", "twisted bow", "scythe", "shadow"));
		score = Math.max(score, scoreIfContains(name, 900, "bandos", "armadyl", "ahrim", "karil", "zaryte", "crystal", "bowfa", "bow of faerdhinen", "toxic blowpipe", "trident", "occult", "primordial", "pegasian", "eternal"));
		score = Math.max(score, scoreIfContains(name, 850, "blood moon", "blue moon", "eclipse moon"));
		score = Math.max(score, scoreIfContains(name, 800, "barrows", "fighter torso", "serpentine", "faceguard", "dragonfire", "abyssal", "whip", "tentacle", "dragon defender", "rune defender", "blessed d'hide", "god d'hide", "malediction", "odium", "toxic"));
		score = Math.max(score, scoreIfContains(name, 700, "dragon", "black d'hide", "mystic", "infinity", "rune crossbow", "magic shortbow", "book of darkness", "tome"));
		score = Math.max(score, scoreIfContains(name, 600, "rune", "red d'hide", "blue d'hide", "green d'hide", "splitbark", "xerician"));
		score = Math.max(score, scoreIfContains(name, 500, "adamant", "mithril", "leather", "wizard"));
		return score;
	}

	private static int scoreIfContains(String name, int score, String... needles)
	{
		return containsAny(name, needles) ? score : 0;
	}

	private static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle)) return true;
		}
		return false;
	}

	private static boolean containsAnyWord(String value, String... words)
	{
		for (String word : words)
		{
			int fromIndex = 0;
			while (fromIndex < value.length())
			{
				int index = value.indexOf(word, fromIndex);
				if (index < 0) break;
				int end = index + word.length();
				boolean startsWord = index == 0 || !Character.isLetterOrDigit(value.charAt(index - 1));
				boolean endsWord = end == value.length()
					|| !Character.isLetterOrDigit(value.charAt(end));
				if (startsWord && endsWord) return true;
				fromIndex = index + 1;
			}
		}
		return false;
	}
}

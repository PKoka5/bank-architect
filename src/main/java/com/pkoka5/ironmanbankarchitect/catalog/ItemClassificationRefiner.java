package com.pkoka5.ironmanbankarchitect.catalog;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsAny;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsWord;

final class ItemClassificationRefiner
{
	private ItemClassificationRefiner()
	{
	}

	static Classification refine(String displayName, String constantName, ItemCategory legacyCategory)
	{
		String name = displayName.toLowerCase(java.util.Locale.ROOT);
		String constant = constantName.toLowerCase(java.util.Locale.ROOT);
		String searchable = (displayName + " " + constantName.replace('_', ' ')).toLowerCase(java.util.Locale.ROOT);

		if (containsAny(name, ClassificationNames.group(0)))
		{
			return new Classification(ItemCategory.CLUE, "treasure-trail");
		}
		if ("mayor of catherby".equals(name))
		{
			// A book; without this rule the "herb" inside "catherby" makes it a herb.
			return new Classification(ItemCategory.CLEANUP, "quest-item");
		}
		if (constant.startsWith("cert_arravshield"))
		{
			return new Classification(ItemCategory.CLEANUP, "quest-item");
		}
		if (name.contains("ornament kit"))
		{
			return new Classification(ItemCategory.CLUE, "cosmetic");
		}
		if (isFishTrophy(name))
		{
			return new Classification(ItemCategory.CLUE, "collection-trophy");
		}
		if ("bonesack".equals(name))
		{
			return new Classification(ItemCategory.CLUE, "cosmetic");
		}
		if ("phoenix".equals(name) || "hell cat".equals(name))
		{
			return new Classification(ItemCategory.CLUE, "collection-pet");
		}
		if ("sled".equals(name))
		{
			return new Classification(ItemCategory.CLEANUP, "quest-item");
		}
		if (name.startsWith("burnt ") && containsFishSpecies(name))
		{
			return new Classification(ItemCategory.CLEANUP, "burnt-food");
		}
		if ("burnt page".equals(name) || "searing page".equals(name) || "soaked page".equals(name)
			|| "desiccated page".equals(name)
			|| "zulrah's scales".equals(name) || "sunfire splinters".equals(name))
		{
			return new Classification(ItemCategory.UNIQUE, "equipment-charge");
		}
		if (name.endsWith(" element staff crown") || "barronite head".equals(name)
			|| "barronite handle".equals(name) || "barronite guard".equals(name))
		{
			return new Classification(ItemCategory.UNIQUE, "weapon-upgrade");
		}
		if ("salve shard".equals(name) || name.contains("crystal armour seed")
			|| name.contains("crystal tool seed"))
		{
			return new Classification(ItemCategory.UNIQUE, "equipment-upgrade");
		}
		if ("vial of blood".equals(name) && "vial_blood".equals(constant))
		{
			return new Classification(ItemCategory.UNIQUE, "equipment-charge");
		}
		if ("binding necklace".equals(name))
		{
			return new Classification(ItemCategory.RUNE, "runecrafting-utility");
		}
		if ("lizardman fang".equals(name))
		{
			return new Classification(ItemCategory.TELEPORT, "teleport-charge");
		}
		if (containsAny(name, ClassificationNames.group(1)))
		{
			return new Classification(ItemCategory.TOOL, "slayer-tool");
		}
		if (containsAny(name, ClassificationNames.group(2)))
		{
			return new Classification(ItemCategory.TOOL, "hunter-tool");
		}
		if (containsAny(name, ClassificationNames.group(3)))
		{
			return new Classification(ItemCategory.TOOL, "resource-container");
		}
		if (containsAny(name, "looting bag", "soul bearer"))
		{
			return new Classification(ItemCategory.TOOL, "utility-container");
		}
		if (containsAny(name, "strung rabbit foot", "brown apron"))
		{
			return new Classification(ItemCategory.TOOL, "skilling-utility");
		}
		if (containsAny(name, ClassificationNames.group(4))
			|| name.startsWith("vyre noble "))
		{
			return new Classification(ItemCategory.TOOL, "quest-utility");
		}
		if ("basket".equals(name) || "bucket".equals(name) || "bucket of water".equals(name)
			|| "jug".equals(name) || "jug of water".equals(name) || "pot".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "utility-container");
		}
		if ("seed dibber".equals(name) || "emerald lantern".equals(name) || "unlit torch".equals(name))
		{
			return new Classification(ItemCategory.TOOL,
				"seed dibber".equals(name) ? "tool" : "light-source");
		}
		if ("teasing stick".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if ("holy wrench".equals(name))
		{
			return new Classification(ItemCategory.POTION, "pvm-utility");
		}
		if ("dwarven rock cake".equals(name))
		{
			return new Classification(ItemCategory.POTION, "pvm-utility");
		}
		if ("botanical pie".equals(name) || "half a botanical pie".equals(name)
			|| "cake".equals(name) || "kebab".equals(name) || "stew".equals(name)
			|| (!name.startsWith("uncooked") && !name.startsWith("burnt")
				&& (name.endsWith(" pie")
					|| (name.endsWith(" cake") && !name.endsWith("rock cake")))))
		{
			// Cooked pies and cakes eat like food; without this they fall to the
			// category-label subcategory and sort among the potions. Uncooked
			// and burnt bakes are not food and keep their own piles.
			return new Classification(ItemCategory.POTION, "food");
		}
		if (containsAny(name, ClassificationNames.group(5))
			|| "jug of wine".equals(name) || "bandit's brew".equals(name))
		{
			return new Classification(ItemCategory.POTION, "drink");
		}
		if (isFarmingProduce(name))
		{
			return new Classification(ItemCategory.FARMING, "produce");
		}
		if ("caviar".equals(name) || "roe".equals(name) || "chocolate dust".equals(name)
			|| "swamp tar".equals(name))
		{
			return new Classification(ItemCategory.HERBLORE, "secondary");
		}
		if ("spirit flakes".equals(name) || "jerboa tail".equals(name) || "kebbit claws".equals(name))
		{
			return new Classification(ItemCategory.SKILLING,
				"spirit flakes".equals(name) ? "resource" : "hunter-resource");
		}
		if ("swamp paste".equals(name))
		{
			return new Classification(ItemCategory.SKILLING, "construction-material");
		}
		if (isCookingMaterial(name))
		{
			return new Classification(ItemCategory.SKILLING, "cooking-material");
		}
		if (isUnenchantedJewellery(name))
		{
			return new Classification(ItemCategory.SKILLING, "crafting-jewellery");
		}
		if ("chronicle".equals(name) || name.contains("quetzal whistle"))
		{
			return new Classification(ItemCategory.TELEPORT, "teleport");
		}
		if ("rune pouch".equals(name))
		{
			return new Classification(ItemCategory.RUNE, "rune-container");
		}
		if (containsAny(name, ClassificationNames.group(6)))
		{
			return new Classification(ItemCategory.RUNE, "rune-container");
		}
		if (name.contains("sawmill coupon"))
		{
			return new Classification(ItemCategory.CURRENCY, "currency");
		}
		if ("thread".equals(name))
		{
			return new Classification(ItemCategory.SKILLING, "textile");
		}
		if (containsAny(name, ClassificationNames.group(7))
			|| (name.contains("coal bag") && constant.startsWith("coal_bag"))
			|| (name.contains("gem bag") && constant.startsWith("gem_bag")))
		{
			return new Classification(ItemCategory.TOOL, "resource-container");
		}
		if (containsAny(name, "gardening trowel", "house keys"))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if (containsAny(name, ClassificationNames.group(8)))
		{
			return new Classification(ItemCategory.GEAR,
				name.contains("anchor") ? "weapon" : "body");
		}
		if (isBarrowsWeaponFamily(name)
			|| equalsAny(name, ClassificationNames.group(9)))
		{
			// Confirmed unique weapons the generated registry either missed (no combat
			// keyword in the name) or mislabelled as SKILLING/CLEANUP. Barrows weapons
			// are matched at every 100/75/50/25/0 charge state, still the same weapon.
			return new Classification(ItemCategory.GEAR, "weapon");
		}
		if (equalsAny(name, ClassificationNames.group(10)))
		{
			// Exact names only: "Occult Necklace Ornament" shares a constant with
			// "Occult necklace (or)" but is a separate, unproven record.
			return new Classification(ItemCategory.GEAR, "neck");
		}
		if ("neitiznot faceguard".equals(name))
		{
			return new Classification(ItemCategory.GEAR, "head");
		}
		if ("fishbowl helmet".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "quest-utility");
		}
		if ("lightbearer".equals(name))
		{
			return new Classification(ItemCategory.GEAR, "ring");
		}
		if (name.contains("hallowed mark"))
		{
			return new Classification(ItemCategory.CURRENCY, "currency");
		}
		if (name.startsWith("slayer ring")
			|| (name.startsWith("wilderness sword") && constant.startsWith("wilderness_sword_"))
			|| "camulet".equals(name) || name.startsWith("giantsoul amulet"))
		{
			// Same-function teleport devices across all charge states; the generated
			// registry read these as plain equippable GEAR. The constant guard keeps
			// out ID 3981 ("Wilderness sword", CERT_REINITIALISATION_15_INACTIVE), an
			// unrelated record that happens to share the display name prefix.
			return new Classification(ItemCategory.TELEPORT, "teleport");
		}
		if (legacyCategory == ItemCategory.TELEPORT)
		{
			if (constant.startsWith("teleportscroll_") || constant.endsWith("_teleport_scroll")
				|| "ardougnescroll".equals(constant))
			{
				return new Classification(ItemCategory.TELEPORT, "teleport-scroll");
			}
			if (constant.startsWith("poh_tablet_") || constant.startsWith("nzone_teletab_")
				|| constant.startsWith("teletab_") || constant.startsWith("tablet_")
				|| constant.startsWith("lunar_tablet_") || constant.startsWith("fossil_tablet_"))
			{
				return new Classification(ItemCategory.TELEPORT, "teleport-tablet");
			}
			return new Classification(ItemCategory.TELEPORT, "teleport");
		}
		if (isSkillCapeOrSkillingGear(name) || isRaimentsOfTheEye(name))
		{
			return new Classification(ItemCategory.TOOL, "skilling-outfit");
		}
		if (containsAny(name, ClassificationNames.group(11)))
		{
			return new Classification(ItemCategory.TOOL, "skilling-utility");
		}
		if ("fletching knife".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "skilling-utility");
		}
		if ("gricoller's can".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if (containsAny(name, ClassificationNames.group(12)))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if (containsAny(name, ClassificationNames.group(13)))
		{
			return new Classification(ItemCategory.TOOL, "slayer-tool");
		}
		if (name.contains("crystal weapon seed"))
		{
			return new Classification(ItemCategory.UNIQUE, "weapon-upgrade");
		}
		if (containsAny(name, ClassificationNames.group(14)))
		{
			return new Classification(ItemCategory.UNIQUE, "equipment-upgrade");
		}
		if (name.contains("potion") && containsAny(name, "(unf)", "unfinished"))
		{
			return new Classification(ItemCategory.HERBLORE, "unfinished-potion");
		}
		if (name.endsWith(" seed"))
		{
			String crop = name.substring(0, name.length() - " seed".length());
			return new Classification(ItemCategory.FARMING,
				isHerbloreCrop(crop) ? "herb-seed" : "farming");
		}
		int potionDose = potionDose(name);
		if (potionDose > 0 && name.contains(" mix("))
		{
			// Barbarian mixes only exist as (2)/(1); they are herblore products,
			// not ready-to-use supplies, so give them a partial dose subcategory.
			return new Classification(ItemCategory.POTION, "potion-dose-" + Math.min(potionDose, 3));
		}
		if (legacyCategory == ItemCategory.POTION && potionDose > 0 && isStandardPotionFamily(name))
		{
			return new Classification(ItemCategory.POTION, "potion-dose-" + potionDose);
		}
		if (isKnownHerb(name))
		{
			return new Classification(ItemCategory.HERBLORE,
				name.startsWith("grimy ") ? "grimy-herb" : "clean-herb");
		}
		if (containsAny(name, ClassificationNames.group(15)))
		{
			return new Classification(ItemCategory.HERBLORE, "secondary");
		}
		if ("acorn".equals(name) || name.startsWith("bird's egg") || "redberries".equals(name)
			|| "white lily".equals(name))
		{
			return new Classification(ItemCategory.FARMING, "farming");
		}
		if (isGem(name))
		{
			return new Classification(ItemCategory.SKILLING, name.startsWith("uncut ") ? "uncut-gem" : "gem");
		}
		if ("vial".equals(name) || "vial of water".equals(name)
			|| containsAny(name, ClassificationNames.group(16)))
		{
			return new Classification(ItemCategory.SKILLING, "glass-material");
		}
		if (name.contains("fabric roll") || "jute fibre".equals(name)
			|| containsAny(name, "bow string", "crossbow string"))
		{
			return new Classification(ItemCategory.SKILLING, "textile");
		}
		if (name.endsWith(" fur") || containsAny(name, ClassificationNames.group(17)))
		{
			return new Classification(ItemCategory.SKILLING, "resource");
		}
		if (name.startsWith("ensouled ") && name.endsWith(" head"))
		{
			return new Classification(ItemCategory.SKILLING, "prayer-resource");
		}
		if (containsAny(name, ClassificationNames.group(18)))
		{
			return new Classification(ItemCategory.SKILLING, "resource");
		}
		if (name.endsWith(" roots") || name.endsWith(" fibre") || name.endsWith(" antler"))
		{
			return new Classification(ItemCategory.SKILLING, "resource");
		}
		if (name.endsWith(" firelighter") || name.endsWith(" dye"))
		{
			return new Classification(ItemCategory.CLUE, "cosmetic");
		}
		if (name.endsWith(" talisman") || name.endsWith(" tiara"))
		{
			return new Classification(ItemCategory.RUNE, "runecrafting-focus");
		}
		if (containsAny(name, "lobster pot", "karambwan vessel")
			|| "gadderhammer".equals(name))
		{
			// Checked before the fish rule: "lobster pot" and "karambwan
			// vessel" contain fish names but are tools, and gadderhammer is a
			// weapon despite the "hammer" in its name.
			return "gadderhammer".equals(name)
				? new Classification(ItemCategory.GEAR, "weapon")
				: new Classification(ItemCategory.TOOL, "tool");
		}
		if (name.startsWith("raw ") && isFish(name))
		{
			return new Classification(ItemCategory.SKILLING, "raw-food");
		}
		if (name.startsWith("leaping ") && isFish(name))
		{
			return new Classification(ItemCategory.SKILLING, "raw-food");
		}
		if (isFish(name))
		{
			return new Classification(ItemCategory.POTION, "food");
		}
		if (legacyCategory == ItemCategory.TOOL)
		{
			return new Classification(ItemCategory.TOOL, toolSubcategory(name));
		}
		if ("cake tin".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "cooking-tool");
		}
		if (name.startsWith("waterskin("))
		{
			return new Classification(ItemCategory.TOOL, "utility-container");
		}
		if (containsAny(name, ClassificationNames.group(19)))
		{
			return new Classification(ItemCategory.SKILLING, "cooking-material");
		}
		if ("bullseye lantern (unf)".equals(name) || "battlestaff".equals(name))
		{
			return new Classification(ItemCategory.SKILLING, "crafting-material");
		}
		if (name.startsWith("cabbages(") || name.startsWith("onions("))
		{
			return new Classification(ItemCategory.FARMING, "produce-container");
		}
		if (containsAny(name, ClassificationNames.group(20))
			|| "knife".equals(name) || "dull knife".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if ("feather".equals(name))
		{
			return new Classification(ItemCategory.SKILLING, "ammo-component");
		}
		if (containsAny(name, ClassificationNames.group(21))
			|| searchable.contains("crossbow limbs") || searchable.contains("crossbow stock")
			|| (name.startsWith("broad bolt") && name.contains("unf")))
		{
			return new Classification(ItemCategory.SKILLING, "ammo-component");
		}
		if (name.endsWith(" mould") || searchable.contains(" mould "))
		{
			return new Classification(ItemCategory.TOOL, "crafting-mould");
		}
		if (containsAny(name, ClassificationNames.group(22)))
		{
			return new Classification(ItemCategory.SKILLING, "textile");
		}
		if (containsAny(name, "fishbowl", "unpowered orb"))
		{
			return new Classification(ItemCategory.SKILLING, "crafting-material");
		}
		if (containsAny(name, ClassificationNames.group(23)))
		{
			return new Classification(ItemCategory.GEAR, "body");
		}
		if (containsAny(name, ClassificationNames.group(24))
			|| (name.contains("tassets") && !name.contains("broken")))
		{
			return new Classification(ItemCategory.GEAR, "legs");
		}
		if (containsAny(name, ClassificationNames.group(25)))
		{
			return new Classification(ItemCategory.GEAR, "head");
		}
		if (legacyCategory == ItemCategory.GEAR)
		{
			return new Classification(legacyCategory, gearSubcategory(name));
		}

		return new Classification(legacyCategory, legacyCategory.getDisplayLabel().toLowerCase(java.util.Locale.ROOT));
	}

	private static String gearSubcategory(String name)
	{
		if (containsAny(name, ClassificationNames.group(26)))
		{
			return "feet";
		}
		if (containsAny(name, ClassificationNames.group(27)))
		{
			return "hands";
		}
		if (containsAny(name, ClassificationNames.group(28)))
		{
			return "neck";
		}
		if (containsAny(name, " ring", "ring of"))
		{
			return "ring";
		}
		if (containsAny(name, ClassificationNames.group(29)))
		{
			return "ammo";
		}
		return "gear";
	}

	private static String toolSubcategory(String name)
	{
		if (containsAny(name, ClassificationNames.group(30)))
		{
			return "skilling-outfit";
		}
		if (name.contains("mould"))
		{
			return "crafting-mould";
		}
		return "tool";
	}

	private static boolean isFish(String name)
	{
		return !name.startsWith("burnt ") && !isFishTrophy(name) && containsFishSpecies(name);
	}

	private static boolean isFishTrophy(String name)
	{
		return (name.startsWith("big ") || name.startsWith("stuffed ") || name.startsWith("mounted "))
			&& containsFishSpecies(name);
	}

	private static boolean containsFishSpecies(String name)
	{
		String[] species = ClassificationNames.group(68);
		if (containsAny(name, ClassificationNames.group(31)))
		{
			return true;
		}
		for (String fish : species)
		{
			if (containsWord(name, fish))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isFarmingProduce(String name)
	{
		return "apple".equals(name) || "banana".equals(name) || "orange".equals(name)
			|| "curry leaf".equals(name) || "pineapple".equals(name) || "papaya fruit".equals(name)
			|| "coconut".equals(name) || "dragonfruit".equals(name) || "strawberry".equals(name)
			|| "watermelon".equals(name);
	}

	private static boolean isCookingMaterial(String name)
	{
		return "barley malt".equals(name) || "cheese".equals(name) || "egg".equals(name)
			|| "gnome spice".equals(name) || "spice".equals(name) || "mushroom".equals(name)
			|| "sulliuscep cap".equals(name);
	}

	private static boolean isUnenchantedJewellery(String name)
	{
		String base = name.endsWith(" (u)") ? name.substring(0, name.length() - 4) : name;
		String[] materials = ClassificationNames.group(69);
		String[] types = {"ring", "necklace", "bracelet", "amulet"};
		for (String material : materials)
		{
			for (String type : types)
			{
				if (base.equals(material + " " + type))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isHerbloreCrop(String name)
	{
		return isKnownHerb(name) || "limpwurt".equals(name) || "snape grass".equals(name);
	}

	private static boolean isKnownHerb(String name)
	{
		String herb = name.startsWith("grimy ") ? name.substring("grimy ".length()) : name;
		return containsAny(herb, ClassificationNames.group(32));
	}

	private static boolean isGem(String name)
	{
		String gem = name.startsWith("uncut ") ? name.substring("uncut ".length()) : name;
		return "opal".equals(gem) || "jade".equals(gem) || "red topaz".equals(gem)
			|| "sapphire".equals(gem) || "emerald".equals(gem) || "ruby".equals(gem)
			|| "diamond".equals(gem) || "dragonstone".equals(gem) || "onyx".equals(gem)
			|| "zenyte".equals(gem);
	}

	private static boolean isBarrowsWeaponFamily(String name)
	{
		return isBarrowsWeaponVariant(name, "dharok's greataxe")
			|| isBarrowsWeaponVariant(name, "guthan's warspear")
			|| isBarrowsWeaponVariant(name, "verac's flail");
	}

	private static boolean isBarrowsWeaponVariant(String name, String baseName)
	{
		if (name.equals(baseName))
		{
			return true;
		}
		return equalsAny(name, baseName + " 100", baseName + " 75", baseName + " 50",
			baseName + " 25", baseName + " 0");
	}

	private static boolean isRaimentsOfTheEye(String name)
	{
		return name.startsWith("hat of the eye") || name.startsWith("robe top of the eye")
			|| name.startsWith("robe bottoms of the eye") || name.startsWith("boots of the eye");
	}

	private static boolean isSkillCapeOrSkillingGear(String name)
	{
		if (name.contains("cape") && containsAny(name, ClassificationNames.group(33)))
		{
			return true;
		}
		return equalsAny(name, ClassificationNames.group(34))
			|| name.startsWith("graceful hood") || name.startsWith("graceful top")
			|| name.startsWith("graceful legs") || name.startsWith("graceful gloves")
			|| name.startsWith("graceful boots") || name.startsWith("graceful cape");
	}

	private static boolean equalsAny(String value, String... candidates)
	{
		for (String candidate : candidates)
		{
			if (value.equals(candidate)) return true;
		}
		return false;
	}

	private static int potionDose(String name)
	{
		int length = name.length();
		if (length >= 3 && name.charAt(length - 3) == '(' && name.charAt(length - 1) == ')')
		{
			char dose = name.charAt(length - 2);
			if (dose >= '1' && dose <= '4')
			{
				return dose - '0';
			}
		}
		return -1;
	}

	private static boolean isStandardPotionFamily(String name)
	{
		if (containsAny(name, ClassificationNames.group(35)))
		{
			return false;
		}
		return containsAny(name, ClassificationNames.group(36));
	}

	static final class Classification
	{
		private final ItemCategory category;
		private final String subcategory;

		Classification(ItemCategory category, String subcategory)
		{
			this.category = category;
			this.subcategory = subcategory;
		}

		ItemCategory getCategory()
		{
			return category;
		}

		String getSubcategory()
		{
			return subcategory;
		}
	}
}

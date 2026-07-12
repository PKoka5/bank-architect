package com.pkoka5.ironmanbankarchitect.catalog;

final class ItemClassificationRefiner
{
	private ItemClassificationRefiner()
	{
	}

	static Classification refine(String displayName, String constantName, ItemCategory legacyCategory)
	{
		String name = displayName.toLowerCase();
		String constant = constantName.toLowerCase();
		String searchable = (displayName + " " + constantName.replace('_', ' ')).toLowerCase();

		if (containsAny(name, "clue scroll", "reward casket", "scroll box", "clue bottle",
			"clue geode", "clue nest"))
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
		if (containsAny(name, "rock hammer", "enchanted gem", "bug lantern"))
		{
			return new Classification(ItemCategory.TOOL, "slayer-tool");
		}
		if (containsAny(name, "large fur pouch", "large meat pouch", "seed box", "herb sack",
			"bottomless compost bucket"))
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
		if (containsAny(name, "diving apparatus", "druid pouch", "crystal chime", "gas mask")
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
			|| "cake".equals(name) || "kebab".equals(name) || "stew".equals(name))
		{
			return new Classification(ItemCategory.POTION, "food");
		}
		if (containsAny(name, "dwarven stout", "wizard's mind bomb", "lizardkicker")
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
		if (containsAny(name, "small pouch", "medium pouch", "large pouch", "giant pouch", "colossal pouch"))
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
		if (containsAny(name, "forestry kit", "fish barrel", "log basket", "plank sack")
			|| (name.contains("coal bag") && constant.startsWith("coal_bag"))
			|| (name.contains("gem bag") && constant.startsWith("gem_bag")))
		{
			return new Classification(ItemCategory.TOOL, "resource-container");
		}
		if (containsAny(name, "gardening trowel", "house keys"))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if (containsAny(name, "fighter torso", "mixed hide top", "barrelchest anchor"))
		{
			return new Classification(ItemCategory.GEAR,
				name.contains("anchor") ? "weapon" : "body");
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
		if (legacyCategory == ItemCategory.TELEPORT)
		{
			return new Classification(ItemCategory.TELEPORT, "teleport");
		}
		if (isSkillCapeOrSkillingGear(name))
		{
			return new Classification(ItemCategory.TOOL, "skilling-outfit");
		}
		if (containsAny(name, "celestial ring", "mining helmet", "cooking gauntlets",
			"goldsmith gauntlets", "noose wand", "steel key ring"))
		{
			return new Classification(ItemCategory.TOOL, "skilling-utility");
		}
		if (containsAny(name, "bird snare", "rabbit snare", "box trap", "lockpick", "ogre bellows"))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if (containsAny(name, "facemask", "earmuffs", "nose peg", "reinforced goggles",
			"ice cooler", "bag of salt"))
		{
			return new Classification(ItemCategory.TOOL, "slayer-tool");
		}
		if (name.contains("crystal weapon seed"))
		{
			return new Classification(ItemCategory.UNIQUE, "weapon-upgrade");
		}
		if (containsAny(name, "pegasian crystal", "primordial crystal", "eternal crystal"))
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
		if (containsAny(name, "bird nest", "crushed nest", "cactus spine", "potato cactus",
			"snake weed"))
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
			|| containsAny(name, "bucket of sand", "lantern lens", "molten glass"))
		{
			return new Classification(ItemCategory.SKILLING, "glass-material");
		}
		if (name.contains("fabric roll") || "jute fibre".equals(name)
			|| containsAny(name, "bow string", "crossbow string"))
		{
			return new Classification(ItemCategory.SKILLING, "textile");
		}
		if (name.endsWith(" fur") || containsAny(name,
			"crystal shard", "rope", "repair kit", "hull parts", "dynamite", "saltpetre"))
		{
			return new Classification(ItemCategory.SKILLING, "resource");
		}
		if (name.startsWith("ensouled ") && name.endsWith(" head"))
		{
			return new Classification(ItemCategory.SKILLING, "prayer-resource");
		}
		if (containsAny(name, "basalt", "calcite", "pyrophosphite", "efh salt", "te salt", "urt salt",
			"unidentified small fossil", "unidentified medium fossil", "unidentified large fossil",
			"unidentified rare fossil"))
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
		if (containsAny(name, "pie dish", "pie shell", "orange spice", "sacred oil", "chocolate bar"))
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
		if (containsAny(name, "lobster pot", "karambwan vessel", "watering can")
			|| "knife".equals(name) || "dull knife".equals(name))
		{
			return new Classification(ItemCategory.TOOL, "tool");
		}
		if ("feather".equals(name))
		{
			return new Classification(ItemCategory.SKILLING, "ammo-component");
		}
		if (containsAny(name, "arrowtips", "arrowheads", "arrow shaft", "headless arrow",
			"bolt tips", "dart tip", "dart shaft", "javelin tips", "javelin shaft",
			"spear tips", "grapple tip", "crossbow limbs", "blurite limbs")
			|| searchable.contains("crossbow limbs") || searchable.contains("crossbow stock")
			|| (name.startsWith("broad bolt") && name.contains("unf")))
		{
			return new Classification(ItemCategory.SKILLING, "ammo-component");
		}
		if (name.endsWith(" mould") || searchable.contains(" mould "))
		{
			return new Classification(ItemCategory.TOOL, "crafting-mould");
		}
		if (containsAny(name, "bolt of canvas", "bolt of cloth", "bolt of linen"))
		{
			return new Classification(ItemCategory.SKILLING, "textile");
		}
		if (containsAny(name, "fishbowl", "unpowered orb"))
		{
			return new Classification(ItemCategory.SKILLING, "crafting-material");
		}
		if (containsAny(name, "chestplate", "platebody", "chainbody", "robe top", "hauberk"))
		{
			return new Classification(ItemCategory.GEAR, "body");
		}
		if (containsAny(name, "platelegs", "plateskirt", "robe bottom", "chaps", "cuisse"))
		{
			return new Classification(ItemCategory.GEAR, "legs");
		}
		if (containsAny(name, " helmet", "helm", "coif", "hood"))
		{
			return new Classification(ItemCategory.GEAR, "head");
		}
		if (legacyCategory == ItemCategory.GEAR)
		{
			return new Classification(legacyCategory, gearSubcategory(name));
		}

		return new Classification(legacyCategory, legacyCategory.getDisplayLabel().toLowerCase());
	}

	private static String gearSubcategory(String name)
	{
		if (containsAny(name, " boots", "shoes", "treads"))
		{
			return "feet";
		}
		if (containsAny(name, " gloves", "gauntlets", "vambraces"))
		{
			return "hands";
		}
		if (containsAny(name, " amulet", "necklace", "pendant"))
		{
			return "neck";
		}
		if (containsAny(name, " ring", "ring of"))
		{
			return "ring";
		}
		if (containsAny(name, " arrow", " bolts", " dart", "cannonball", "grapple"))
		{
			return "ammo";
		}
		return "gear";
	}

	private static String toolSubcategory(String name)
	{
		if (containsAny(name, "graceful", "angler", "prospector", "pyromancer", "lumberjack",
			"carpenter", "farmer's strawhat", "farmer's jacket", "farmer's shirt",
			"farmer's boro trousers", "farmer's boots", "rogue "))
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
		String[] species = {"swordfish", "shark", "monkfish", "karambwan", "karambwanji", "eel",
			"anglerfish", "harpoonfish", "lobster", "tuna", "salmon", "trout", "sturgeon",
			"mackerel", "sardine", "herring", "anchovies", "shrimp", "cod", "pike", "bass"};
		if (containsAny(name, "manta ray", "sea turtle", "dark crab"))
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
		String[] materials = {"gold", "opal", "jade", "red topaz", "sapphire",
			"emerald", "ruby", "diamond", "dragon", "dragonstone", "onyx", "zenyte"};
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
		return containsAny(herb, "guam leaf", "marrentill", "tarromin", "harralander", "ranarr weed", "ranarr",
			"toadflax", "irit leaf", "clean irit", "avantoe", "kwuarm", "snapdragon",
			"cadantine", "lantadyme", "dwarf weed", "torstol");
	}

	private static boolean isGem(String name)
	{
		String gem = name.startsWith("uncut ") ? name.substring("uncut ".length()) : name;
		return "opal".equals(gem) || "jade".equals(gem) || "red topaz".equals(gem)
			|| "sapphire".equals(gem) || "emerald".equals(gem) || "ruby".equals(gem)
			|| "diamond".equals(gem) || "dragonstone".equals(gem) || "onyx".equals(gem)
			|| "zenyte".equals(gem);
	}

	private static boolean isSkillCapeOrSkillingGear(String name)
	{
		if (name.contains("cape") && containsAny(name, "agility", "cooking", "construction", "crafting",
			"farming", "firemaking", "fishing", "fletching", "herblore", "hunter", "mining",
			"runecraft", "runecrafting", "slayer", "smithing", "thieving", "woodcutting"))
		{
			return true;
		}
		return equalsAny(name,
			"angler hat", "angler top", "angler waders", "angler boots",
			"spirit angler headband", "spirit angler top", "spirit angler waders", "spirit angler boots",
			"prospector helmet", "prospector jacket", "prospector legs", "prospector boots",
			"pyromancer hood", "pyromancer garb", "pyromancer robe", "pyromancer boots",
			"lumberjack hat", "lumberjack top", "lumberjack legs", "lumberjack boots",
			"carpenter's helmet", "carpenter's shirt", "carpenter's trousers", "carpenter's boots",
			"farmer's strawhat", "farmer's jacket", "farmer's shirt", "farmer's boro trousers",
			"farmer's boots", "rogue mask", "rogue top", "rogue trousers", "rogue gloves", "rogue boots")
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
		if (containsAny(name, " mix", "sacred oil", "orange spice"))
		{
			return false;
		}
		return containsAny(name, "potion", "brew", "restore", "antidote", "antifire",
			"super attack", "super strength", "super defence", "super energy", "sanfew serum",
			"relicym's balm");
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

	private static boolean containsWord(String value, String word)
	{
		int fromIndex = 0;
		while (fromIndex < value.length())
		{
			int index = value.indexOf(word, fromIndex);
			if (index < 0)
			{
				return false;
			}
			int end = index + word.length();
			boolean startBoundary = index == 0 || !Character.isLetterOrDigit(value.charAt(index - 1));
			boolean endBoundary = end == value.length() || !Character.isLetterOrDigit(value.charAt(end));
			if (startBoundary && endBoundary)
			{
				return true;
			}
			fromIndex = index + 1;
		}
		return false;
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

package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ResourceItemRegistry implements ItemCatalog
{
	static final String RESOURCE_PATH = "/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";

	public static final ResourceItemRegistry INSTANCE = new ResourceItemRegistry();

	private final Map<Integer, CatalogItem> itemsById;

	private ResourceItemRegistry()
	{
		this.itemsById = Collections.unmodifiableMap(loadItems());
	}

	@Override
	public Optional<CatalogItem> findById(int itemId)
	{
		return Optional.ofNullable(itemsById.get(itemId));
	}

	public boolean containsId(int itemId)
	{
		return itemsById.containsKey(itemId);
	}

	public int size()
	{
		return itemsById.size();
	}

	private static Map<Integer, CatalogItem> loadItems()
	{
		InputStream stream = ResourceItemRegistry.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing item registry resource: " + RESOURCE_PATH);
		}

		Map<Integer, CatalogItem> items = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().isEmpty())
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length < 2)
				{
					throw new IllegalStateException("Invalid item registry line: " + line);
				}

				String itemIdText = fields[0];
				if (!itemIdText.isEmpty() && itemIdText.charAt(0) == '\uFEFF')
				{
					itemIdText = itemIdText.substring(1);
				}

				int itemId = Integer.parseInt(itemIdText);
				String displayName = fields[1];
				ItemCategory category = parseRegistryCategory(fields.length >= 3 ? fields[2] : "");
				String constantName = fields.length >= 4 ? fields[3] : "";
				category = refineCategory(displayName, constantName, category);
				items.putIfAbsent(itemId, new CatalogItem(itemId, displayName, category,
					category.getDisplayLabel().toLowerCase(), Collections.emptySet(), null));
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to load item registry resource", ex);
		}

		return items;
	}

	private static ItemCategory parseRegistryCategory(String value)
	{
		if (value == null || value.trim().isEmpty() || "UNKNOWN".equals(value))
		{
			return ItemCategory.CLEANUP;
		}

		try
		{
			return ItemCategory.valueOf(value);
		}
		catch (IllegalArgumentException ex)
		{
			return ItemCategory.CLEANUP;
		}
	}

	private static ItemCategory refineCategory(String displayName, String constantName, ItemCategory category)
	{
		String name = (displayName + " " + constantName.replace('_', ' ')).toLowerCase();
		if (containsAny(name, "clue scroll", "reward casket", "ornament kit", "graceful", "pyromancer",
			"prospector", "angler", "rogue", "lumberjack", "farmer's", "carpenter", "cosmetic",
			"costume", "mask", "hat", "robe top", "robe bottom", "platebody ornament", "platelegs ornament",
			"beer glass", "nulodion's notes", "instruction manual", "holy table napkin", "grail bell",
			"holy grail", "cog", "rat poison", "red vine worm", "insect repellent", "candle",
			"khazard cell", "lever", "shiny key", "child's blanket", "quest", "notes", "letter",
			"package", "message", "certificate", "sample", "specimen", "plaque", "pebble", "urn",
			"skull", "portrait", "belt buckle", "old boot", "buttons", "powder", "liquid",
			"chemical", "compound", "vase", "bullroarer", "hollow reed", "casket", "key",
			"gown", "scorpion cage", "bird feed", "sheep feed", "cattleprod", "oil can",
			"rubber tube", "pressure gauge", "distillator", "touch paper", "karamja rum",
			"plague", "pigeon", "newcomer map", "torch", "observatory lens", "scroll",
			"locating crystal", "beads of the dead", "wampum belt", "old tooth",
			"ammonium nitrate", "nitroglycerin", "arcenia root", "tattered", "scrumpled",
			"scribbled", "scrawled", "macro triffidfruit", "report", "shaman's tome",
			"binding book", "heart crystal", "chunk of crystal", "hunk of crystal",
			"lump of crystal", "holy force", "totem", "gnomeball", "crest part"))
		{
			return ItemCategory.CLEANUP;
		}
		if (containsAny(name, "ring of dueling", "games necklace", "amulet of glory", "skills necklace",
			"combat bracelet", "burning amulet", "necklace of passage", "digsite pendant", "ring of wealth",
			"bracelet of ethereum", "teleport", "tablet", "teletab", "jewellery", "ectophial",
			"xeric's talisman", "xeric talisman", "drakan's medallion", "drakans medallion",
			"book of the dead", "magic whistle"))
		{
			return ItemCategory.TELEPORT;
		}
		if (containsAny(name, "shark", "monkfish", "karambwan", "manta ray", "anglerfish", "lobster",
			"swordfish", "tuna", "salmon", "trout", "saradomin brew", "restore", "stamina potion",
			"prayer potion", "super combat", "ranging potion", "magic potion", "cooked", "pizza",
			"potato", "cake", "pie", "wine", "summer pie", "karambwanji", "shrimp", "anchovies",
			"sardine", "herring", "mackerel", "cod", "pike", "bass", "mantaray", "sea turtle",
			"seaturtle", "giant carp", "strange fruit", "chilli potato", "egg potato",
			"tuna potato", "stew", "curry", "kebab", "strength4", "attack4", "defence4",
			"ranging4", "magic4", "poison chalice", "enchanted beef", "enchanted rat",
			"enchanted bear", "enchanted chicken", "cup of tea", "holy water"))
		{
			return ItemCategory.POTION;
		}
		if (containsAny(name, "pickaxe", " axe", "harpoon", "lobster pot", "small fishing net",
			"big fishing net", "chisel", "hammer", "saw", "rake", "seed dibber", "secateurs",
			"watering can", "tinderbox", "knife", "pestle and mortar", "glassblowing pipe",
			"spade", "needle", "thread", "mould", "hammerstone", "fishing rod", "fly fishing rod",
			"small pouch", "medium pouch", "large pouch", "giant pouch", "colossal pouch"))
		{
			return ItemCategory.SKILLING;
		}
		if (containsAny(name, "arrow", "bolt", "dart", "knife", "javelin", "cannonball", "chinchompa",
			"bolt rack", "toktz", "tzhaar", "helmet", "helm", "coif", "body", "chaps", "vambraces",
			"boots", "gloves", "shield", "defender", "sword", "scimitar", "mace", "dagger", "spear",
			"halberd", "whip", "bow", "staff", "wand", "crossbow", "maul", "warhammer", "battleaxe",
			"excalibur", "pendant of lucien", "armadyl pendant", "cannon base", "cannon stand",
			"cannon barrels", "cannon furnace", "twpart", "mcannon", "railing", "gauntlets"))
		{
			return ItemCategory.GEAR;
		}
		if (containsAny(name, "logs", "log", "ore", "bar", "plank", "hide", "leather", "gem",
			"uncut", "essence", "fish", "raw ", "scale", "dust", "ash", "bone", "bones",
			"limestone", "clay", "sand", "molten glass", "flax", "bow string", "bowstring",
			"feather", "nail", "nails", "coconut", "seaweed", "bucket of", "vial", "orb",
			"battlestaff", "dragonhide", "coal", "charcoal", "rock pick", "trowel",
			"panning tray", "oyster"))
		{
			return ItemCategory.SKILLING;
		}
		if (containsAny(name, "seed", "sapling", "compost", "ultracompost", "plant cure", "watering can"))
		{
			return ItemCategory.FARMING;
		}
		if (containsAny(name, "grimy", "clean", "herb", "secondary", "unf", "potion unfinished",
			"eye of newt", "snape grass", "red spiders", "white berries", "limpwurt", "mort myre fungus",
			"unidentified guam", "unidentified marentill", "unidentified marrentill",
			"unidentified tarromin", "unidentified harralander", "unidentified ranarr",
			"unidentified irit", "unidentified avantoe", "unidentified kwuarm",
			"unidentified cadantine", "unidentified dwarf weed", "unidentified torstol",
			"guam", "marentill", "marrentill", "tarromin", "harralander", "ranarr weed",
			"irit leaf", "avantoe", "kwuarm", "cadantine", "dwarf weed", "torstol",
			"unicorn horn", "jangerberries", "weapon poison", "snakeweed mixture",
			"ardrigal mixture", "cadava berries", "cadavaberries"))
		{
			return ItemCategory.HERBLORE;
		}
		if (containsAny(name, "coins", "tokkul", "numulite", "mark of grace", "nugget", "stardust",
			"trading sticks", "ecto-token", "castle wars ticket", "pieces of eight", "pearl",
			"western banner", "rada's blessing", "rada blessing", "ghommal's hilt", "ghommals hilt"))
		{
			return ItemCategory.CURRENCY;
		}

		return category;
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
}

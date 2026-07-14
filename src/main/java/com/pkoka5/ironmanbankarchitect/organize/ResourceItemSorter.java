package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps resource workflows dense and deterministic. The primary
 * {@link ResourceSkillZone} is the first sort key; the existing
 * workflow/family/stage ordering is preserved inside each zone. Name checks
 * deliberately use complete suffixes/tokens so items such as bark, barb-tailed
 * kebbit and smashed mirrors cannot leak into bar or ash blocks.
 */
final class ResourceItemSorter
{
	private static final int METAL = 0;
	private static final int WOOD = 10;
	private static final int HIDE = 20;
	private static final int GEM = 30;
	private static final int PRAYER = 40;
	private static final int GLASS = 50;
	private static final int ASH = 60;
	private static final int AMMO_COMPONENT = 70;
	private static final int RAW_FOOD = 80;
	private static final int COOKING = 90;
	private static final int TEXTILE = 100;
	private static final int CONSTRUCTION = 110;
	private static final int TROLL_SALT = 120;
	private static final int FOSSIL = 121;
	private static final int VALUABLE_RESOURCE = 122;
	private static final int OTHER = 130;
	private static final Map<String, Integer> WORKFLOW_FAMILY_RANKS = workflowFamilyRanks();

	private ResourceItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt((BankPreviewItem item) -> ResourceSkillZoneClassifier.classify(item).ordinal())
			.thenComparingInt(ResourceItemSorter::roleRank)
			.thenComparingInt(ResourceItemSorter::processRank)
			.thenComparingInt(ResourceItemSorter::familyRank)
			.thenComparingInt(ResourceItemSorter::stageRank)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int processRank(BankPreviewItem item)
	{
		if (isSawmillCoupon(item)) return 2;
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (isSailingMetal(name)) return 2;
		if (isMiningResource(name)) return 3;

		Optional<ItemSortMetadata> metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(value -> value.getVariantKind() == ItemSortMetadata.VariantKind.WORKFLOW_STAGE);
		if (metadata.isPresent())
		{
			String family = metadata.get().getFamilyKey();
			if (family.startsWith("metal.")) return family.equals("metal.bars") ? 1 : 0;
			if (family.startsWith("wood."))
			{
				if (family.equals("wood.planks")) return 1;
				return isStandardLog(name) || family.equals("wood.logs.normal") ? 0 : 2;
			}
			if (family.startsWith("gem.")) return metadata.get().getVariantValue();
		}

		if (isMetal(name)) return name.endsWith(" bar") ? 1 : 0;
		if (isWood(name))
		{
			if (name.equals("plank") || name.endsWith(" plank")) return 1;
			return isStandardLog(name) ? 0 : 2;
		}
		if (subcategory.contains("jewellery")) return 2;
		if (isGem(name)) return name.startsWith("uncut ") ? 0 : 1;
		return 0;
	}

	private static int roleRank(BankPreviewItem item)
	{
		if (isSawmillCoupon(item)) return WOOD;
		Optional<ItemSortMetadata> metadata = resourceWorkflowMetadata(item);
		if (metadata.isPresent())
		{
			return metadata.get().getFamilyKey().startsWith("metal.") ? METAL : GEM;
		}

		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (isMetal(name)) return METAL;
		if (isMiningResource(name)) return METAL;
		if (isWood(name)) return WOOD;
		if (containsAny(name, "hide", "leather") || name.endsWith(" fur")) return HIDE;
		if (subcategory.contains("ammo-component")) return AMMO_COMPONENT;
		if (subcategory.contains("gem") || subcategory.contains("jewellery") || isGem(name)) return GEM;
		if (subcategory.contains("prayer") || containsWord(name, "bone")
			|| containsWord(name, "bones") || containsWord(name, "remains")) return PRAYER;
		if (isGlassMaterial(name, subcategory)) return GLASS;
		if (name.endsWith(" ash") || name.endsWith(" ashes")) return ASH;
		if (subcategory.contains("raw-food") || name.startsWith("raw ")
			|| name.startsWith("leaping ")) return RAW_FOOD;
		if (isFishingResource(name)) return RAW_FOOD;
		if (subcategory.contains("cooking")) return COOKING;
		if (isTextile(name, subcategory)) return TEXTILE;
		if (subcategory.contains("construction") || isConstructionMaterial(name)) return CONSTRUCTION;
		if (isTrollSalt(name)) return TROLL_SALT;
		if (isFossil(name)) return FOSSIL;
		if (subcategory.contains("resource")) return VALUABLE_RESOURCE;
		return OTHER;
	}

	private static int familyRank(BankPreviewItem item)
	{
		if (isSawmillCoupon(item)) return 2000;
		Optional<ItemSortMetadata> metadata = resourceWorkflowMetadata(item);
		if (metadata.isPresent())
		{
			return WORKFLOW_FAMILY_RANKS.get(metadata.get().getFamilyKey());
		}

		String name = normalized(item.getDisplayName());
		switch (roleRank(item))
		{
			case METAL:
				return metalRank(name);
			case WOOD:
				return woodRank(name);
			case HIDE:
				return hideRank(name);
			case GEM:
				return gemRank(name);
			case PRAYER:
				return prayerRank(name);
			case RAW_FOOD:
				return foodRank(name);
			case TEXTILE:
				return textileRank(name);
			case CONSTRUCTION:
				return constructionRank(name);
			default:
				return 100;
		}
	}

	private static int stageRank(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = resourceWorkflowMetadata(item);
		if (metadata.isPresent())
		{
			return metadata.get().getVariantValue();
		}

		String name = normalized(item.getDisplayName());
		switch (roleRank(item))
		{
			case METAL:
				if (name.endsWith(" ore")) return name.startsWith("tin ") ? 1 : 0;
				if (name.equals("coal")) return 1;
				if (name.startsWith("steel ")) return 3;
				return 2;
			case WOOD:
				return name.equals("plank") || name.endsWith(" plank") ? 1 : 0;
			case HIDE:
				return name.contains("leather") ? 1 : 0;
			case GEM:
				return name.startsWith("uncut ") ? 0 : 1;
			case GLASS:
				if (containsAny(name, "seaweed")) return 0;
				if (name.equals("soda ash")) return 1;
				if (name.contains("sand")) return 2;
				if (name.contains("molten glass")) return 3;
				return 4;
			case AMMO_COMPONENT:
				if (name.contains("shaft")) return 0;
				if (name.equals("feather")) return 1;
				if (name.contains("headless")) return 2;
				if (containsAny(name, "tip", "head", "limbs")) return 3;
				return 4;
			case TEXTILE:
				if (name.equals("flax")) return 0;
				if (name.contains("string")) return 1;
				if (name.equals("thread")) return 2;
				return 3;
			default:
				return 0;
		}
	}

	private static Optional<ItemSortMetadata> resourceWorkflowMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.WORKFLOW_STAGE)
			.filter(metadata -> WORKFLOW_FAMILY_RANKS.containsKey(metadata.getFamilyKey()));
	}

	private static boolean isSawmillCoupon(BankPreviewItem item)
	{
		return item.getItemId() == 32083 || item.getItemId() == 32085;
	}

	private static Map<String, Integer> workflowFamilyRanks()
	{
		Map<String, Integer> ranks = new LinkedHashMap<>();
		ranks.put("metal.ores-base", 0);
		ranks.put("metal.ores-tier", 10);
		ranks.put("metal.bars", 20);
		ranks.put("gem.sapphire", 40);
		ranks.put("gem.emerald", 50);
		ranks.put("gem.ruby", 60);
		ranks.put("gem.diamond", 70);
		ranks.put("gem.dragonstone", 80);
		return Collections.unmodifiableMap(ranks);
	}

	static boolean isMetal(String name)
	{
		return name.equals("coal") || name.endsWith(" ore")
			|| (name.endsWith(" bar") && !name.equals("chocolate bar"));
	}

	static boolean isWood(String name)
	{
		return name.equals("logs") || name.endsWith(" logs") || name.equals("plank")
			|| name.endsWith(" plank") || name.endsWith(" bark");
	}

	private static boolean isStandardLog(String name)
	{
		return name.equals("logs") || name.equals("oak logs") || name.equals("teak logs")
			|| name.equals("maple logs") || name.equals("mahogany logs")
			|| name.equals("yew logs") || name.equals("magic logs")
			|| name.equals("redwood logs");
	}

	private static boolean isSailingMetal(String name)
	{
		return name.contains("nickel ore") || name.contains("cupronickel bar");
	}

	static boolean isGlassMaterial(String name, String subcategory)
	{
		return subcategory.contains("glass") || name.equals("seaweed") || name.equals("giant seaweed")
			|| name.equals("soda ash") || name.contains("molten glass") || name.contains("bucket of sand")
			|| name.contains("lantern lens") || name.equals("vial") || name.equals("vial of water")
			|| name.contains("unpowered orb");
	}

	static boolean isTextile(String name, String subcategory)
	{
		return subcategory.contains("textile") || name.equals("flax") || name.equals("thread")
			|| name.equals("ball of wool") || name.contains("bolt of cloth")
			|| name.contains("bolt of canvas") || name.contains("bolt of linen");
	}

	static boolean isConstructionMaterial(String name)
	{
		return name.endsWith(" nails") || name.equals("clay") || name.equals("soft clay")
			|| name.contains("limestone") || name.equals("curved bone") || name.equals("long bone");
	}

	static boolean isMiningResource(String name)
	{
		return name.equals("volcanic ash") || name.equals("basalt") || name.equals("calcite")
			|| name.equals("pyrophosphite") || name.equals("dynamite")
			|| name.equals("saltpetre");
	}

	static boolean isFishingResource(String name)
	{
		return name.equals("spirit flakes") || name.equals("fish offcuts")
			|| name.equals("fishing bait");
	}

	static boolean isHunterResource(String name, String subcategory)
	{
		return subcategory.contains("hunter-resource") || name.equals("sunlight antelope antler")
			|| name.equals("pheasant tail feathers");
	}

	static boolean isSailingResource(String name)
	{
		return name.equals("large mahogany hull parts") || name.equals("repair kit")
			|| name.equals("teak repair kit");
	}

	private static boolean isTrollSalt(String name)
	{
		return name.equals("efh salt") || name.equals("te salt") || name.equals("urt salt");
	}

	private static boolean isFossil(String name)
	{
		return name.equals("unidentified small fossil")
			|| name.equals("unidentified medium fossil");
	}

	private static int prayerRank(String name)
	{
		if (name.contains("bone shard")) return 0;
		if (containsWord(name, "bone") || containsWord(name, "bones")) return 10;
		if (name.startsWith("ensouled ")) return 20;
		if (containsWord(name, "remains")) return 30;
		if (name.endsWith(" ash") || name.endsWith(" ashes")) return 40;
		if (name.contains("bird's egg")) return 50;
		return 45;
	}

	private static int metalRank(String name)
	{
		if (containsAny(name, "copper", "tin", "bronze")) return 0;
		if (containsAny(name, "blurite")) return 5;
		if (containsAny(name, "iron", "coal", "steel")) return 10;
		if (name.contains("silver")) return 20;
		if (name.contains("gold")) return 30;
		if (name.contains("mithril")) return 40;
		if (name.contains("adamant")) return 50;
		if (containsAny(name, "runite", "rune ")) return 60;
		if (containsAny(name, "cupronickel", "nickel")) return 70;
		return 100;
	}

	private static int woodRank(String name)
	{
		if (name.equals("logs") || name.equals("plank")) return 0;
		String[] species = {"oak", "willow", "teak", "maple", "mahogany", "arctic pine",
			"yew", "magic", "redwood", "blisterwood", "camphor", "ironwood", "celastrus",
			"anima-infused"};
		return orderedMatch(name, species);
	}

	private static int hideRank(String name)
	{
		String[] types = {"cow", "green", "blue", "red", "black", "bear", "kebbit", "fox",
			"larupia", "graahk", "kyatt", "dagannoth"};
		return orderedMatch(name, types);
	}

	private static int gemRank(String name)
	{
		String[] gems = {"opal", "jade", "topaz", "sapphire", "emerald", "ruby", "diamond",
			"dragonstone", "onyx", "zenyte"};
		return orderedMatch(name, gems);
	}

	private static int foodRank(String name)
	{
		if (name.contains("leaping trout")) return 200;
		if (name.contains("leaping salmon")) return 210;
		if (name.contains("leaping sturgeon")) return 220;
		String[] food = {"shrimp", "sardine", "herring", "anchovies", "mackerel", "trout",
			"salmon", "tuna", "lobster", "swordfish", "monkfish", "karambwan", "shark",
			"sea turtle", "manta ray", "anglerfish", "eel", "dark crab"};
		return orderedMatch(name, food);
	}

	private static int textileRank(String name)
	{
		if (containsAny(name, "flax", "bow string")) return 0;
		if (name.contains("wool")) return 10;
		if (name.equals("thread")) return 20;
		if (name.contains("canvas")) return 30;
		if (name.contains("cloth")) return 40;
		if (name.contains("linen")) return 50;
		return 100;
	}

	private static int constructionRank(String name)
	{
		if (name.endsWith(" nails")) return metalRank(name);
		if (name.contains("clay")) return 80;
		if (name.contains("limestone")) return 90;
		return 100;
	}

	private static int orderedMatch(String name, String[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (name.contains(values[i])) return (i + 1) * 10;
		}
		return 1000;
	}

	static boolean isGem(String name)
	{
		return containsAny(name, "opal", "jade", "topaz", "sapphire", "emerald", "ruby",
			"diamond", "dragonstone", "onyx", "zenyte");
	}

	static boolean containsWord(String value, String word)
	{
		int fromIndex = 0;
		while (fromIndex < value.length())
		{
			int index = value.indexOf(word, fromIndex);
			if (index < 0) return false;
			int end = index + word.length();
			boolean startBoundary = index == 0 || !Character.isLetterOrDigit(value.charAt(index - 1));
			boolean endBoundary = end == value.length() || !Character.isLetterOrDigit(value.charAt(end));
			if (startBoundary && endBoundary) return true;
			fromIndex = index + 1;
		}
		return false;
	}

	static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle)) return true;
		}
		return false;
	}

	static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

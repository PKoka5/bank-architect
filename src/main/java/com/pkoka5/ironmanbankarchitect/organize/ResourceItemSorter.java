package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Keeps resource workflows dense and deterministic. Name checks deliberately
 * use complete suffixes/tokens so items such as bark, barb-tailed kebbit and
 * smashed mirrors cannot leak into bar or ash blocks.
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
	private static final int VALUABLE_RESOURCE = 120;
	private static final int OTHER = 130;

	private ResourceItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator
			.comparingInt(ResourceItemSorter::roleRank)
			.thenComparingInt(ResourceItemSorter::familyRank)
			.thenComparingInt(ResourceItemSorter::stageRank)
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int roleRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (isMetal(name)) return METAL;
		if (isWood(name)) return WOOD;
		if (containsAny(name, "hide", "leather") || name.endsWith(" fur")) return HIDE;
		if (subcategory.contains("ammo-component")) return AMMO_COMPONENT;
		if (subcategory.contains("gem") || isGem(name)) return GEM;
		if (subcategory.contains("prayer") || containsWord(name, "bone")
			|| containsWord(name, "bones") || containsWord(name, "remains")) return PRAYER;
		if (isGlassMaterial(name, subcategory)) return GLASS;
		if (name.endsWith(" ash") || name.endsWith(" ashes")) return ASH;
		if (subcategory.contains("raw-food") || name.startsWith("raw ")
			|| name.startsWith("leaping ")) return RAW_FOOD;
		if (subcategory.contains("cooking")) return COOKING;
		if (isTextile(name, subcategory)) return TEXTILE;
		if (isConstructionMaterial(name)) return CONSTRUCTION;
		if (subcategory.contains("resource")) return VALUABLE_RESOURCE;
		return OTHER;
	}

	private static int familyRank(BankPreviewItem item)
	{
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
				if (name.contains("headless")) return 1;
				if (containsAny(name, "tip", "head", "limbs")) return 2;
				if (name.contains("string")) return 3;
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

	private static boolean isMetal(String name)
	{
		return name.equals("coal") || name.endsWith(" ore")
			|| (name.endsWith(" bar") && !name.equals("chocolate bar"));
	}

	private static boolean isWood(String name)
	{
		return name.equals("logs") || name.endsWith(" logs") || name.equals("plank")
			|| name.endsWith(" plank") || name.endsWith(" bark");
	}

	private static boolean isGlassMaterial(String name, String subcategory)
	{
		return subcategory.contains("glass") || name.equals("seaweed") || name.equals("giant seaweed")
			|| name.equals("soda ash") || name.contains("molten glass") || name.contains("bucket of sand")
			|| name.contains("lantern lens") || name.equals("vial") || name.equals("vial of water")
			|| name.contains("unpowered orb");
	}

	private static boolean isTextile(String name, String subcategory)
	{
		return subcategory.contains("textile") || name.equals("flax") || name.equals("thread")
			|| name.equals("ball of wool") || name.contains("bolt of cloth")
			|| name.contains("bolt of canvas") || name.contains("bolt of linen");
	}

	private static boolean isConstructionMaterial(String name)
	{
		return name.endsWith(" nails") || name.equals("clay") || name.equals("soft clay")
			|| name.contains("limestone");
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
		String[] food = {"shrimp", "sardine", "herring", "anchovies", "mackerel", "trout",
			"salmon", "tuna", "lobster", "swordfish", "monkfish", "karambwan", "shark",
			"sea turtle", "manta ray", "anglerfish", "leaping trout", "leaping salmon",
			"leaping sturgeon"};
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

	private static boolean isGem(String name)
	{
		return containsAny(name, "opal", "jade", "topaz", "sapphire", "emerald", "ruby",
			"diamond", "dragonstone", "onyx", "zenyte");
	}

	private static boolean containsWord(String value, String word)
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

	private static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle)) return true;
		}
		return false;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class TeleportItemSorter
{
	private static final Comparator<BankPreviewItem> TELEPORT_ORDER = Comparator
		.comparingInt(TeleportItemSorter::roleRank)
		.thenComparingInt(TeleportItemSorter::runeRank)
		.thenComparingInt(TeleportItemSorter::pouchRank)
		.thenComparing(TeleportItemSorter::familyName)
		.thenComparingInt(item -> -charge(item))
		.thenComparing(item -> normalized(item.getDisplayName()))
		.thenComparingInt(BankPreviewItem::getItemId);

	private TeleportItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(TELEPORT_ORDER);
		return sorted;
	}

	static int compareTeleportItems(BankPreviewItem left, BankPreviewItem right)
	{
		return TELEPORT_ORDER.compare(left, right);
	}

	private static int roleRank(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		String subcategory = normalized(item.getSubcategory());
		if (item.getItemCategory() == ItemCategory.RUNE)
		{
			if (name.contains("essence"))
			{
				return 10;
			}
			if (subcategory.contains("focus")) return 20;
			if (isEssencePouch(name)) return 21;
			if (name.equals("rune pouch")) return 22;
			if (subcategory.contains("container")) return 22;
			if (subcategory.contains("utility")) return 23;
			return 0;
		}
		if (subcategory.contains("transport-access"))
		{
			return 90;
		}
		if (subcategory.contains("teleport-charge") || name.contains("crystal teleport seed"))
		{
			return 80;
		}
		if (isReusableDevice(name))
		{
			return 30;
		}
		if (isJewellery(item)) return 40;
		if (subcategory.contains("teleport-container")) return 50;
		if (isTablet(item)) return 60;
		if (isScroll(item)) return 70;
		return 85;
	}

	private static int runeRank(BankPreviewItem item)
	{
		int role = roleRank(item);
		if (role != 0 && role != 20)
		{
			return 100;
		}
		String name = normalized(item.getDisplayName());
		String[] order = {"air", "water", "earth", "fire", "mind", "body", "cosmic", "chaos",
			"nature", "law", "death", "blood", "soul", "astral", "wrath"};
		for (int i = 0; i < order.length; i++)
		{
			if ((role == 0 && name.equals(order[i] + " rune"))
				|| (role == 20 && name.startsWith(order[i] + " ")))
			{
				return i;
			}
		}
		return 90;
	}

	private static int pouchRank(BankPreviewItem item)
	{
		if (roleRank(item) != 21) return 100;
		String name = normalized(item.getDisplayName());
		String[] order = {"small", "medium", "large", "giant", "colossal"};
		for (int i = 0; i < order.length; i++)
		{
			if (name.startsWith(order[i] + " pouch")) return i;
		}
		return 90;
	}

	private static boolean isEssencePouch(String name)
	{
		return containsAny(name, "small pouch", "medium pouch", "large pouch", "giant pouch",
			"colossal pouch");
	}

	private static boolean isJewellery(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = teleportJewelleryMetadata(item);
		if (metadata.isPresent()) return true;
		String name = normalized(item.getDisplayName());
		return containsAny(name, "ring of dueling", "games necklace", "amulet of glory",
			"skills necklace", "combat bracelet", "burning amulet", "necklace of passage",
			"digsite pendant", "ring of wealth", "slayer ring", "ring of returning",
			"ring of the elements", "ring of shadows", "camulet", "desert amulet",
			"giantsoul amulet", "sailors' amulet");
	}

	private static String familyName(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = teleportJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getFamilyKey();
		String name = normalized(item.getDisplayName());
		return name.replaceFirst("\\s*\\([0-9]+\\)$", "");
	}

	private static int charge(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = teleportJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getVariantValue();
		String name = normalized(item.getDisplayName());
		int open = name.lastIndexOf('(');
		if (open < 0 || !name.endsWith(")"))
		{
			return -1;
		}
		try
		{
			return Integer.parseInt(name.substring(open + 1, name.length() - 1));
		}
		catch (NumberFormatException ignored)
		{
			return -1;
		}
	}

	private static Optional<ItemSortMetadata> teleportJewelleryMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE)
			.filter(metadata -> metadata.getFamilyKey().startsWith("jewellery."));
	}

	private static boolean isReusableDevice(String name)
	{
		return containsAny(name, "ectophial", "xeric's talisman", "drakan's medallion",
			"royal seed pod", "enchanted lyre", "pharaoh's sceptre", "chronicle",
			"kharedst's memoirs", "book of the dead", "teleport crystal",
			"quetzal whistle", "pendant of ates", "skull sceptre");
	}

	private static boolean isTablet(BankPreviewItem item)
	{
		String subcategory = normalized(item.getSubcategory());
		String name = normalized(item.getDisplayName());
		return subcategory.contains("teleport-tablet") || name.contains("tablet")
			|| name.contains("teletab");
	}

	private static boolean isScroll(BankPreviewItem item)
	{
		String subcategory = normalized(item.getSubcategory());
		String name = normalized(item.getDisplayName());
		return subcategory.contains("teleport-scroll") || name.contains("teleport scroll");
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

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}

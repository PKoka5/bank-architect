package com.pkoka5.ironmanbankarchitect.organize;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsAny;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.normalized;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.numericSuffix;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.List;


final class TeleportItemSorter
{
	private static final OrderedItemFamilies TABLET_COLOURS = new OrderedItemFamilies(
		TeleportItemSorter.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/teleport-tablet-colours.tsv"), 0);
	private static final Comparator<BankPreviewItem> TELEPORT_ORDER = Comparator
		.comparingInt(TeleportItemSorter::roleRank)
		.thenComparingInt(TeleportItemSorter::runeRank)
		.thenComparingInt(TeleportItemSorter::pouchRank)
		.thenComparingInt(TeleportItemSorter::tabletColourRank)
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

	private static int tabletColourRank(BankPreviewItem item)
	{
		if (roleRank(item) != 60) return 0;
		int rank = 0;
		for (List<Integer> ids : TABLET_COLOURS.entries().values())
		{
			if (ids.contains(item.getItemId())) return rank;
			rank++;
		}
		return rank;
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
		if (subcategory.contains("teleport-reusable") || isReusableDevice(name))
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
		String[] order = ClassificationNames.group(81);
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
		String[] order = ClassificationNames.group(82);
		for (int i = 0; i < order.length; i++)
		{
			if (name.startsWith(order[i] + " pouch")) return i;
		}
		return 90;
	}

	private static boolean isEssencePouch(String name)
	{
		return containsAny(name, ClassificationNames.group(83));
	}

	private static boolean isJewellery(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = teleportJewelleryMetadata(item);
		if (metadata.isPresent()) return true;
		String name = normalized(item.getDisplayName());
		return containsAny(name, ClassificationNames.group(84));
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
		return numericSuffix(name, -1);
	}

	static Optional<ItemSortMetadata> teleportJewelleryMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE)
			.filter(metadata -> metadata.getFamilyKey().startsWith("jewellery."));
	}

	private static boolean isReusableDevice(String name)
	{
		return containsAny(name, ClassificationNames.group(85));
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

}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Stable fallback order for the mixed Ironman quick-access Main tab. */
final class IronmanMainItemSorter
{
	private IronmanMainItemSorter()
	{
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items)
	{
		return sort(items, RuneOrder.ALPHABETICAL);
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items, RuneOrder runeOrder)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparingInt(IronmanMainItemSorter::rank)
			.thenComparingInt(item -> runeOrder == RuneOrder.ELEMENTAL
				? elementalRank(item) : 0)
			.thenComparing(IronmanMainItemSorter::familyOrder)
			.thenComparingLong(item -> -(long) charge(item))
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	/**
	 * Single items run by name; charged jewellery families follow as a group,
	 * each family under its base name with charges descending. Positioning a
	 * family by its internal {@code jewellery.} key put the whole group in the
	 * middle of the teleport tablets (j falls between Falador and Lumberyard),
	 * and positioning it by name does the same (so does g) — families after
	 * singles is the only collation that keeps both runs whole.
	 */
	private static String familyOrder(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = chargedJewelleryMetadata(item);
		if (metadata.isPresent())
		{
			// The base name decides where the family sits; the key only breaks
			// ties between families sharing one base name (standard vs imbued
			// rings of wealth), so those stay separate runs instead of merging.
			return "\uFFFF" + baseName(item) + "\u0000" + metadata.get().getFamilyKey();
		}
		return normalized(item.getDisplayName());
	}

	/** The display name without its charge suffix: "Amulet of glory(6)" -> "amulet of glory". */
	private static String baseName(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		int open = name.lastIndexOf('(');
		return open > 0 && name.endsWith(")") ? name.substring(0, open).trim() : name;
	}

	private static int charge(BankPreviewItem item)
	{
		Optional<ItemSortMetadata> metadata = chargedJewelleryMetadata(item);
		if (metadata.isPresent()) return metadata.get().getVariantValue();
		return 0;
	}

	private static Optional<ItemSortMetadata> chargedJewelleryMetadata(BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE)
			.filter(metadata -> metadata.getFamilyKey().startsWith("jewellery."));
	}

	/**
	 * The canonical rune sequence players know from spellbooks and shops.
	 * Anything unrecognized - essence, unusual runes - follows alphabetically.
	 */
	private static int elementalRank(BankPreviewItem item)
	{
		if (item.getItemCategory() != ItemCategory.RUNE)
		{
			return 0;
		}
		String name = normalized(item.getDisplayName());
		String[] order = {"air", "water", "earth", "fire", "mind", "body", "cosmic", "chaos",
			"nature", "law", "death", "blood", "soul", "astral", "wrath"};
		for (int index = 0; index < order.length; index++)
		{
			if (name.equals(order[index] + " rune"))
			{
				return index;
			}
		}
		return order.length + 1;
	}

	private static int rank(BankPreviewItem item)
	{
		if (item.getItemId() == 995) return 0;
		if (IronmanQuickToolSelector.isTieredTool(item.getItemId())
			|| IronmanMainTabPolicy.isRunePouch(item)) return 10;
		if (IronmanMainTabPolicy.isGraceful(item)) return 20;
		if (item.getItemCategory() == ItemCategory.RUNE) return 30;
		if (item.getItemCategory() == ItemCategory.TELEPORT) return 40;
		if (AchievementDiarySemanticRuleSet.isDiaryReward(item)) return 60;
		String name = normalized(item.getDisplayName());
		if (name.contains("mark of grace") || name.contains("hallowed mark")) return 70;
		if (item.getItemCategory() == ItemCategory.CURRENCY) return 100;
		return 50;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ENGLISH);
	}
}

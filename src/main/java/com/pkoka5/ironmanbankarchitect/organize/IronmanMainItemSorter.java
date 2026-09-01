package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import java.util.ArrayList;
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
		return sort(items, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items, RuneOrder runeOrder)
	{
		return sort(items, runeOrder, TeleportOrder.ALPHABETICAL);
	}

	static List<BankPreviewItem> sort(List<BankPreviewItem> items, RuneOrder runeOrder,
		TeleportOrder teleportOrder)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort((left, right) -> compareMainItems(left, right, runeOrder, teleportOrder));
		return sorted;
	}

	private static int compareMainItems(BankPreviewItem left, BankPreviewItem right,
		RuneOrder runeOrder, TeleportOrder teleportOrder)
	{
		int byRank = Integer.compare(rank(left), rank(right));
		if (byRank != 0) return byRank;
		if (runeOrder == RuneOrder.ELEMENTAL)
		{
			int byElemental = Integer.compare(elementalRank(left), elementalRank(right));
			if (byElemental != 0) return byElemental;
		}
		if (left.getItemCategory() == ItemCategory.TELEPORT
			&& right.getItemCategory() == ItemCategory.TELEPORT)
		{
			if (teleportOrder == TeleportOrder.SPELLBOOK_FIRST)
			{
				// The curated order keeps its own residual - other teleports
				// alphabetically with jewellery families after - so it falls
				// through to the family collation below instead of the
				// by-use delegation.
				int bySpellbook = Integer.compare(spellbookRank(left), spellbookRank(right));
				if (bySpellbook != 0) return bySpellbook;
			}
			else
			{
				return TeleportItemSorter.compareTeleportItems(left, right);
			}
		}
		int byFamily = familyOrder(left).compareTo(familyOrder(right));
		if (byFamily != 0) return byFamily;
		int byCharge = -Integer.compare(charge(left), charge(right));
		if (byCharge != 0) return byCharge;
		int byName = normalized(left.getDisplayName()).compareTo(normalized(right.getDisplayName()));
		return byName != 0 ? byName : Integer.compare(left.getItemId(), right.getItemId());
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

	/**
	 * The standard spellbook's city teleports in casting order lead; other
	 * single teleports follow alphabetically; charged jewellery families come
	 * after the singles as always.
	 */
	private static int spellbookRank(BankPreviewItem item)
	{
		if (item.getItemCategory() != ItemCategory.TELEPORT)
		{
			return 0;
		}
		if (chargedJewelleryMetadata(item).isPresent())
		{
			return 90;
		}
		String name = normalized(item.getDisplayName());
		String[] order = {"varrock teleport", "lumbridge teleport", "falador teleport",
			"teleport to house", "camelot teleport", "ardougne teleport", "watchtower teleport",
			"trollheim teleport", "ape atoll teleport", "kourend castle teleport",
			"civitas illa fortis teleport"};
		for (int index = 0; index < order.length; index++)
		{
			if (name.equals(order[index]))
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

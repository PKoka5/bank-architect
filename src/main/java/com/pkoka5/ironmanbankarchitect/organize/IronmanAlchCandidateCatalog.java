package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * Reviewed canonical IDs for common Slayer/bossing alch stock.
 *
 * <p>The list deliberately uses RuneLite's player-facing gameval constants:
 * noted, POH, Battle Royale, League, dummy, ornament and clue variants are not
 * inferred from display names and therefore cannot enter by collision.</p>
 */
final class IronmanAlchCandidateCatalog
{
	private static final Set<Integer> ITEM_IDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		// Rune weapons and armour.
		ItemID.RUNE_PLATEBODY, ItemID.RUNE_PLATELEGS, ItemID.RUNE_PLATESKIRT,
		ItemID.RUNE_CHAINBODY, ItemID.RUNE_KITESHIELD, ItemID.RUNE_SQ_SHIELD,
		ItemID.RUNE_FULL_HELM, ItemID.RUNE_MED_HELM, ItemID.RUNE_2H_SWORD,
		ItemID.RUNE_SCIMITAR, ItemID.RUNE_BATTLEAXE, ItemID.RUNE_LONGSWORD,
		ItemID.RUNE_WARHAMMER, ItemID.RUNE_MACE, ItemID.RUNE_DAGGER,
		ItemID.RUNE_HALBERD, ItemID.RUNE_SWORD, ItemID.RUNE_SPEAR,
		ItemID.RUNE_PICKAXE, ItemID.RUNE_AXE,

		// Dragon duplicates, including the canonical poisoned dagger variants.
		ItemID.DRAGON_DAGGER, ItemID.DRAGON_DAGGER_P, ItemID.DRAGON_DAGGER_P_,
		ItemID.DRAGON_DAGGER_P__, ItemID.DRAGON_MED_HELM, ItemID.DRAGON_MACE,
		ItemID.DRAGON_SCIMITAR, ItemID.DRAGON_LONGSWORD, ItemID.DRAGON_BATTLEAXE,
		ItemID.DRAGON_HALBERD, ItemID.DRAGON_SPEAR, ItemID.DRAGON_SQ_SHIELD,
		ItemID.DRAGON_PLATELEGS, ItemID.DRAGON_PLATESKIRT, ItemID.DRAGON_HARPOON,

		// Adamant weapons and armour.
		ItemID.ADAMANT_PLATEBODY, ItemID.ADAMANT_PLATELEGS, ItemID.ADAMANT_PLATESKIRT,
		ItemID.ADAMANT_KITESHIELD, ItemID.ADAMANT_SQ_SHIELD, ItemID.ADAMANT_FULL_HELM,
		ItemID.ADAMANT_2H_SWORD, ItemID.ADAMANT_BATTLEAXE, ItemID.ADAMANT_SCIMITAR,

		// Battlestaves and the three standard Mystic colourways.
		ItemID.BATTLESTAFF, ItemID.AIR_BATTLESTAFF, ItemID.WATER_BATTLESTAFF,
		ItemID.EARTH_BATTLESTAFF, ItemID.FIRE_BATTLESTAFF,
		ItemID.MYSTIC_ROBE_TOP, ItemID.MYSTIC_ROBE_TOP_DARK, ItemID.MYSTIC_ROBE_TOP_LIGHT,
		ItemID.MYSTIC_ROBE_BOTTOM, ItemID.MYSTIC_ROBE_BOTTOM_DARK, ItemID.MYSTIC_ROBE_BOTTOM_LIGHT,
		ItemID.MYSTIC_HAT, ItemID.MYSTIC_HAT_DARK, ItemID.MYSTIC_HAT_LIGHT,
		ItemID.MYSTIC_BOOTS, ItemID.MYSTIC_BOOTS_DARK, ItemID.MYSTIC_BOOTS_LIGHT,
		ItemID.MYSTIC_GLOVES, ItemID.MYSTIC_GLOVES_DARK, ItemID.MYSTIC_GLOVES_LIGHT,
		ItemID.MYSTIC_AIR_STAFF, ItemID.MYSTIC_WATER_STAFF,
		ItemID.MYSTIC_EARTH_STAFF, ItemID.MYSTIC_FIRE_STAFF,

		// Dragonhide, granite and crafted jewellery.
		ItemID.BLACK_DRAGONHIDE_BODY, ItemID.BLACK_DRAGONHIDE_CHAPS,
		ItemID.RED_DRAGONHIDE_BODY, ItemID.RED_DRAGONHIDE_CHAPS,
		ItemID.BLUE_DRAGONHIDE_BODY, ItemID.BLUE_DRAGONHIDE_CHAPS,
		ItemID.DRAGONHIDE_BODY, ItemID.GRANITE_SHIELD, ItemID.GRANITE_LEGS,
		ItemID.JEWL_DIAMOND_BRACELET, ItemID.DRAGONSTONE_RING,
		ItemID.JEWL_DRAGONSTONE_BRACELET, ItemID.JEWL_GOLD_BRACELET,
		ItemID.TOPAZ_BRACELET)));

	private IronmanAlchCandidateCatalog()
	{
	}

	static boolean contains(int itemId)
	{
		return ITEM_IDS.contains(itemId);
	}
}

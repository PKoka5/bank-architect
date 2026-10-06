package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;
import static net.runelite.api.gameval.ItemID.*;

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
		RUNE_PLATEBODY, RUNE_PLATELEGS, RUNE_PLATESKIRT,
		RUNE_CHAINBODY, RUNE_KITESHIELD, RUNE_SQ_SHIELD,
		RUNE_FULL_HELM, RUNE_MED_HELM, RUNE_2H_SWORD,
		RUNE_SCIMITAR, RUNE_BATTLEAXE, RUNE_LONGSWORD,
		RUNE_WARHAMMER, RUNE_MACE, RUNE_DAGGER,
		RUNE_HALBERD, RUNE_SWORD, RUNE_SPEAR,
		RUNE_PICKAXE, RUNE_AXE,

		// Ordinary med helms and chainbodies; clue/ornament variants are separate IDs.
		BRONZE_MED_HELM, IRON_MED_HELM, STEEL_MED_HELM,
		BLACK_MED_HELM, MITHRIL_MED_HELM, ADAMANT_MED_HELM,
		BRONZE_CHAINBODY, IRON_CHAINBODY, STEEL_CHAINBODY,
		BLACK_CHAINBODY, MITHRIL_CHAINBODY, ADAMANT_CHAINBODY,
		DRAGON_CHAINBODY,

		// Dragon duplicates, including the canonical poisoned dagger variants.
		DRAGON_DAGGER, DRAGON_DAGGER_P, DRAGON_DAGGER_P_,
		DRAGON_DAGGER_P__, DRAGON_MED_HELM, DRAGON_MACE,
		DRAGON_SCIMITAR, DRAGON_LONGSWORD, DRAGON_BATTLEAXE,
		DRAGON_HALBERD, DRAGON_SPEAR, DRAGON_SQ_SHIELD,
		DRAGON_PLATELEGS, DRAGON_PLATESKIRT, DRAGON_HARPOON,

		// Adamant weapons and armour.
		ADAMANT_PLATEBODY, ADAMANT_PLATELEGS, ADAMANT_PLATESKIRT,
		ADAMANT_KITESHIELD, ADAMANT_SQ_SHIELD, ADAMANT_FULL_HELM,
		ADAMANT_2H_SWORD, ADAMANT_BATTLEAXE, ADAMANT_SCIMITAR, ADAMANT_AXE,

		// Battlestaves and the three standard Mystic colourways.
		BATTLESTAFF, AIR_BATTLESTAFF, WATER_BATTLESTAFF,
		EARTH_BATTLESTAFF, FIRE_BATTLESTAFF,
		MYSTIC_ROBE_TOP, MYSTIC_ROBE_TOP_DARK, MYSTIC_ROBE_TOP_LIGHT,
		MYSTIC_ROBE_BOTTOM, MYSTIC_ROBE_BOTTOM_DARK, MYSTIC_ROBE_BOTTOM_LIGHT,
		MYSTIC_HAT, MYSTIC_HAT_DARK, MYSTIC_HAT_LIGHT,
		MYSTIC_BOOTS, MYSTIC_BOOTS_DARK, MYSTIC_BOOTS_LIGHT,
		MYSTIC_GLOVES, MYSTIC_GLOVES_DARK, MYSTIC_GLOVES_LIGHT,
		MYSTIC_AIR_STAFF, MYSTIC_WATER_STAFF,
		MYSTIC_EARTH_STAFF, MYSTIC_FIRE_STAFF,

		// Dragonhide, granite and crafted jewellery.
		BLACK_DRAGONHIDE_BODY, BLACK_DRAGONHIDE_CHAPS,
		RED_DRAGONHIDE_BODY, RED_DRAGONHIDE_CHAPS,
		BLUE_DRAGONHIDE_BODY, BLUE_DRAGONHIDE_CHAPS,
		DRAGONHIDE_BODY, GRANITE_SHIELD, GRANITE_LEGS,
		JEWL_DIAMOND_BRACELET, DRAGONSTONE_RING,
		JEWL_DRAGONSTONE_BRACELET, JEWL_GOLD_BRACELET,
		TOPAZ_BRACELET)));

	// Rows are strongest first; rune supply and standard autocast stay the same.
	private static final int[][] ELEMENTAL_STAVES = {
		{1405, 1397, 1381}, {1403, 1395, 1383}, {1407, 1399, 1385}, {1401, 1393, 1387}};
	// Equal bonuses: keep one ordinary colourway per slot with a stable preference.
	private static final int[][] MYSTIC_COLOURS = {
		{4089, 4099, 4109}, {4091, 4101, 4111}, {4093, 4103, 4113},
		{4095, 4105, 4115}, {4097, 4107, 4117}};
	private static final Set<Integer> SLASH_STOCK = new HashSet<>(Arrays.asList(
		1373, 1319, 1303, 1333, 1289, 1213, 1371, 1317, 1331));
	private static final Set<Integer> SLASH_UPGRADES = new HashSet<>(Arrays.asList(
		4587, 4151, 12006, 26482));

	private IronmanAlchCandidateCatalog()
	{
	}

	static boolean contains(int itemId)
	{
		return ITEM_IDS.contains(itemId) || isElementalStaff(itemId);
	}

	static boolean isElementalStaff(int itemId)
	{
		for (int[] family : ELEMENTAL_STAVES)
			for (int member : family) if (member == itemId) return true;
		return false;
	}

	/** Exact role evidence; colours never replace each other in both directions. */
	static boolean hasRoleReplacement(int itemId, Set<Integer> owned, Map<Integer, String> choices)
	{
		for (int[] family : ELEMENTAL_STAVES)
		{
			boolean higherOwned = false;
			for (int member : family)
			{
				if (member == itemId) return higherOwned;
				higherOwned |= owned.contains(member);
			}
		}
		for (int[] family : MYSTIC_COLOURS)
		{
			boolean member = false;
			int keeper = -1;
			boolean pinnedKeeper = false;
			for (int candidate : family)
			{
				member |= candidate == itemId;
				String choice = choices.get(candidate);
				boolean pinned = "gear".equals(choice) || "combat-gear".equals(choice);
				if (owned.contains(candidate) && (choice == null || pinned)
					&& (keeper < 0 || pinned && !pinnedKeeper))
				{
					keeper = candidate;
					pinnedKeeper = pinned;
				}
			}
			if (member) return keeper > 0 && keeper != itemId;
		}
		return SLASH_STOCK.contains(itemId) && !Collections.disjoint(owned, SLASH_UPGRADES);
	}
}

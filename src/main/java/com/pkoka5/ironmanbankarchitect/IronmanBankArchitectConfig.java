package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import net.runelite.client.config.*;




@ConfigGroup(IronmanBankArchitectConfig.GROUP)
public interface IronmanBankArchitectConfig extends Config
{
	String GROUP = "ironmanbankarchitect";

	@ConfigItem(keyName = "bankPreset", name = "", description = "", hidden = true)
	default String bankPreset() { return "IRONMAN"; }

	@ConfigItem(keyName = "bankPreset", name = "", description = "")
	void setBankPreset(String preset);

	@ConfigSection(
		name = "Guidance",
		description = "The overlays and hints shown while analysing and sorting.",
		position = 0
	)
	String guidanceSection = "Guidance";

	@ConfigSection(
		name = "Frequently used",
		description = "The quick-access items gathered from across the whole bank.",
		position = 1
	)
	String frequentlyUsedSection = "Frequently used";

	@ConfigSection(
		name = "Combat gear",
		description = "How the combat gear tab is arranged and curated.",
		position = 2
	)
	String gearSection = "Combat gear";

	@ConfigSection(
		name = "Food & potions",
		description = "How the supplies tab is arranged.",
		position = 3
	)
	String suppliesSection = "Food & potions";

	@ConfigSection(
		name = "Herblore",
		description = "How the Herblore tab's recipe rows are arranged.",
		position = 4
	)
	String herbloreSection = "Herblore";

	@ConfigSection(
		name = "Runes, teleports & currency",
		description = "How the utility tabs are arranged.",
		position = 5
	)
	String utilitiesSection = "Runes, teleports & currency";

	@ConfigSection(
		name = "Tools",
		description = "How the skilling tools tab is arranged.",
		position = 6
	)
	String toolsSection = "Tools";

	@ConfigSection(
		name = "Resources",
		description = "How the resources tab is arranged.",
		position = 7
	)
	String resourcesSection = "Resources";

	@ConfigSection(
		name = "Clues & cosmetics",
		description = "How the clues and cosmetics tab is arranged.",
		position = 8
	)
	String cluesSection = "Clues & cosmetics";

	@ConfigItem(
		keyName = "suggestNextMove",
		section = guidanceSection,
		position = 0,
		name = "Show next manual move",
		description = "Highlight the next manual collapse, tab drag or reorder in the vanilla All items view. Follow the bank's Swap or Insert mode."
	)
	default boolean suggestNextMove()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showCategoryOverlay",
		section = guidanceSection,
		position = 1,
		name = "Colour bank items by destination",
		description = "In Assign categories mode, colour items by their blueprint destination. Works in any bank view; drawing only."
	)
	default boolean showCategoryOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideSortedHighlights",
		section = guidanceSection,
		position = 2,
		name = "Hide the green on sorted items",
		description = "Hide green highlights on correctly placed items; other validation colours remain. Off shows correct slots in green. Newly misplaced items remain visible while the guide stays on."
	)
	default boolean hideSortedHighlights()
	{
		return false;
	}

	@ConfigItem(
		keyName = "autoGuide",
		section = guidanceSection,
		position = 3,
		name = "Guide on bank open",
		description = "Analyze and enable guidance on bank open and deposits or withdrawals. Automatic guidance hides banners on other tabs or filtered views and hides green highlights; sidebar controls restore normal guidance."
	)
	default boolean autoGuide()
	{
		return false;
	}

	@ConfigItem(
		keyName = "gearLayout",
		section = gearSection,
		position = 0,
		name = "Layout",
		description = "Best in slot: strongest melee, ranged, magic and prayer columns, one row per equipment slot, filled to align. Sets together: vertical sets with remaining kit nearby. List: strongest sets first, then loose gear and weapons, without fillers."
	)
	default GearLayout gearLayout()
	{
		return GearLayout.GRID_STYLES;
	}

	@ConfigItem(
		keyName = "alchPile",
		section = gearSection,
		position = 1,
		name = "Gather outclassed gear for alching",
		description = "Gather replaced gear and tools, including spare standard Mystic colours. Main and Ironman use the same rules; preserve staff rune supply. Dragon halberds always count as alch stock."
	)
	default boolean alchPile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "potionDoses",
		section = suppliesSection,
		position = 0,
		name = "Potion doses",
		description = "Grab area: full potions first, part doses behind food for decanting. By family: each potion together, doses 4 to 1."
	)
	default PotionDoseOrder potionDoses()
	{
		return PotionDoseOrder.GRAB_AREA;
	}

	@ConfigItem(
		keyName = "fillHerbloreRows",
		section = herbloreSection,
		position = 0,
		name = "Fill part-empty Herblore rows",
		description = "Fill incomplete recipe rows with other items so the next recipe starts at the left edge. Off keeps short rows followed immediately by the next recipe."
	)
	default boolean fillHerbloreRows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "keepDoseRows",
		section = suppliesSection,
		position = 1,
		name = "Keep dose sets on one row",
		description = "Move dose families behind later items when needed to keep each family on one row. Off preserves exact order and allows families to wrap."
	)
	default boolean keepDoseRows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "utilitiesLayout",
		section = utilitiesSection,
		position = 0,
		name = "Layout",
		description = "Grid: four-wide rune block and achievement diary grid. List: items in reading order without fillers."
	)
	default TabOrder utilitiesLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "toolsLayout",
		section = toolsSection,
		position = 0,
		name = "Layout",
		description = "Grid: empty container columns above their filled forms. List: tools in skill order without fillers."
	)
	default TabOrder toolsLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "resourcesLayout",
		section = resourcesSection,
		position = 0,
		name = "Layout",
		description = "Grid: raw materials above their processed forms. List: items in reading order without fillers."
	)
	default TabOrder resourcesLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "cluesLayout",
		section = cluesSection,
		position = 0,
		name = "Layout",
		description = "Grid: cosmetic outfits in vertical columns. List: items in reading order without fillers."
	)
	default TabOrder cluesLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "runeOrder",
		section = utilitiesSection,
		position = 1,
		name = "Rune order",
		description = "Alphabetical, or the canonical elemental sequence: air, water, earth, fire, then mind, body, cosmic, chaos, nature, law, death, blood, soul, astral, wrath."
	)
	default RuneOrder runeOrder()
	{
		return RuneOrder.ALPHABETICAL;
	}

	@ConfigItem(
		keyName = "teleportOrder",
		section = utilitiesSection,
		position = 2,
		name = "Teleport order",
		description = "Alphabetical, or standard spellbook city teleports in casting order, followed by other teleports alphabetically and then jewellery."
	)
	default TeleportOrder teleportOrder()
	{
		return TeleportOrder.ALPHABETICAL;
	}

	@ConfigItem(
		keyName = "gatherFrequentlyUsed",
		section = frequentlyUsedSection,
		position = 0,
		name = "Gather frequently used items",
		description = "Gather your best axe and pickaxe, hammer, Graceful, rune pouches, diary rewards and staple utilities under Frequently Used. Off keeps their usual categories."
	)
	default boolean gatherFrequentlyUsed()
	{
		return true;
	}

	@ConfigItem(
		keyName = "categoryOverlayOpacity",
		section = guidanceSection,
		position = 4,
		name = "Destination colour opacity",
		description = "Fill strength of the destination colours, 0-100. Borders stay fully visible."
	)
	default int categoryOverlayOpacity()
	{
		return 25;
	}

	/**
	 * Player-recorded item-to-category corrections, stored locally as
	 * {@code itemId=categoryKey} pairs. Hidden because it is edited through the
	 * bank right-click menu and the sidebar, not by hand.
	 */
	@ConfigItem(
		keyName = "categoryOverrides",
		name = "",
		description = "",
		hidden = true
	)
	default String categoryOverrides()
	{
		return "";
	}

	@ConfigItem(
		keyName = "categoryOverrides",
		name = "",
		description = ""
	)
	void setCategoryOverrides(String serialized);

	/**
	 * The player's blueprint tab order, stored locally as comma-separated
	 * category keys. Hidden because it is edited through the sidebar's tab
	 * order dialog, not by hand.
	 */
	@ConfigItem(
		keyName = "tabOrder",
		name = "",
		description = "",
		hidden = true
	)
	default String tabOrder()
	{
		return "";
	}

	@ConfigItem(
		keyName = "tabOrder",
		name = "",
		description = ""
	)
	void setTabOrder(String serialized);

	/**
	 * The player's saved block orders for the active layout, one entry per
	 * tag. Hidden because they are edited through the sidebar's arrange rows,
	 * not by hand.
	 */
	@ConfigItem(
		keyName = "blockOrders",
		name = "",
		description = "",
		hidden = true
	)
	default String blockOrders()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockOrders",
		name = "",
		description = ""
	)
	void setBlockOrders(String serialized);

	/**
	 * Block orders snapshotted per saved layout, so switching profiles brings
	 * each layout's own arrangements back with it.
	 */
	@ConfigItem(
		keyName = "blockOrdersByProfile",
		name = "",
		description = "",
		hidden = true
	)
	default String blockOrdersByProfile()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockOrdersByProfile",
		name = "",
		description = ""
	)
	void setBlockOrdersByProfile(String serialized);

	@ConfigItem(keyName = "blueprintOrdersByProfile", name = "", description = "", hidden = true)
	default String blueprintOrdersByProfile() { return ""; }

	@ConfigItem(keyName = "blueprintOrdersByProfile", name = "", description = "")
	void setBlueprintOrdersByProfile(String serialized);

	/**
	 * The player's saved tab layouts, stored locally as {@code name~plan} pairs.
	 * Hidden because they are created by saving, importing and switching in the
	 * sidebar rather than typed by hand.
	 */
	@ConfigItem(
		keyName = "layoutProfiles",
		name = "",
		description = "",
		hidden = true
	)
	default String layoutProfiles()
	{
		return "";
	}

	@ConfigItem(
		keyName = "layoutProfiles",
		name = "",
		description = ""
	)
	void setLayoutProfiles(String serialized);

	@ConfigItem(
		keyName = "activeLayoutProfile",
		name = "",
		description = "",
		hidden = true
	)
	default String activeLayoutProfile()
	{
		return "";
	}

	@ConfigItem(
		keyName = "activeLayoutProfile",
		name = "",
		description = ""
	)
	void setActiveLayoutProfile(String name);

	@ConfigItem(keyName = "lastSeenRelease", name = "", description = "", hidden = true)
	default String lastSeenRelease() { return ""; }

	@ConfigItem(keyName = "lastSeenRelease", name = "", description = "")
	void setLastSeenRelease(String releaseId);
}

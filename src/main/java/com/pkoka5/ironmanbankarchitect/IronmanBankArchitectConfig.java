package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.GearLayout;
import com.pkoka5.ironmanbankarchitect.organize.PotionDoseOrder;
import com.pkoka5.ironmanbankarchitect.organize.RuneOrder;
import com.pkoka5.ironmanbankarchitect.organize.TeleportOrder;
import com.pkoka5.ironmanbankarchitect.organize.TabOrder;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(IronmanBankArchitectConfig.GROUP)
public interface IronmanBankArchitectConfig extends Config
{
	String GROUP = "ironmanbankarchitect";

	@ConfigSection(
		name = "Guidance",
		description = "The overlays and hints shown while analysing and sorting.",
		position = 0
	)
	String guidanceSection = "Guidance";

	@ConfigSection(
		name = "Combat gear",
		description = "How the combat gear tab is arranged and curated.",
		position = 1
	)
	String gearSection = "Combat gear";

	@ConfigSection(
		name = "Food & potions",
		description = "How the supplies tab is arranged.",
		position = 2
	)
	String suppliesSection = "Food & potions";

	@ConfigSection(
		name = "Herblore",
		description = "How the Herblore tab's recipe rows are arranged.",
		position = 3
	)
	String herbloreSection = "Herblore";

	@ConfigSection(
		name = "Runes, teleports & currency",
		description = "How the utility tabs are arranged.",
		position = 4
	)
	String utilitiesSection = "Runes, teleports & currency";

	@ConfigSection(
		name = "Tools",
		description = "How the skilling tools tab is arranged.",
		position = 5
	)
	String toolsSection = "Tools";

	@ConfigSection(
		name = "Resources",
		description = "How the resources tab is arranged.",
		position = 6
	)
	String resourcesSection = "Resources";

	@ConfigSection(
		name = "Clues & cosmetics",
		description = "How the clues and cosmetics tab is arranged.",
		position = 7
	)
	String cluesSection = "Clues & cosmetics";

	@ConfigItem(
		keyName = "suggestNextMove",
		section = guidanceSection,
		position = 0,
		name = "Show next manual move",
		description = "Highlight the next safe manual collapse, tab drag, or same-section reorder in the vanilla All items bank view. Guidance follows the bank's Swap or Insert mode."
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
		description = "While Assign categories is on, tint every bank item with the colour of the blueprint tab it is planned for. Turn this off to keep the bank uncoloured even in assign mode. Drawing only; works in any bank view."
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
		description = "Once a slot already holds the item the blueprint wants there, leave it uncoloured. A finished bank then looks untouched and only the items that still need attention stay tinted, so you can keep the guide on permanently and a newly banked item stands out on its own. Off, the guide confirms every correct slot in green. Drawing only; misplaced, wrong and unplanned slots keep their colours either way."
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
		description = "Analyze the bank and arm the sorting guide automatically every time the bank opens, so the sidebar is never needed. Armed this way the guide stays quiet: no banners on other tabs or filtered views, and no green on already-sorted slots - only items still out of place are shown. The sidebar buttons keep working and switch the guide back to its usual form."
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
		description = "Best in slot is the four-style matrix: one row per equipment slot, the leading columns your strongest melee, ranged, magic and prayer options, rows completed so the columns stay straight. Sets together stacks each set down a column - helm, body, legs - with the rest of the kit arranged around it, junk-free. List reads each set as one left-to-right run, strongest set first, loose gear and weapons flowing after, junk-free."
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
		description = "Move equipment you own two strictly better versions of, and that is worth alching, to the Slayer & Boss Loot tab. Turn this off to keep every piece of gear in the combat gear tab, for example when you deliberately keep a spare set."
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
		description = "Grab area keeps full potions at the front with part doses trailing behind the food as the to-decant pile. By family runs each potion 4 to 1 in one place."
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
		description = "On, a part-finished recipe row borrows from the rest of the tab so the next recipe still starts at the left edge. Off, a short row is left short and the recipes simply follow each other."
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
		description = "A dose family meeting a row edge slides behind the items after it so it stays on one row. Off, nothing ever moves: the tab reads in exact order and a family may wrap at the row edge."
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
		description = "Grid keeps the curated shapes: the four-wide rune block and the achievement diary grid. List runs every item in reading order, wrapping like text, junk-free."
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
		description = "Grid keeps the curated shape: empty containers as columns above their filled forms. List runs every tool in skill order, wrapping like text, junk-free."
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
		description = "Grid keeps the curated shape: raw materials aligned above their processed forms. List runs every item in reading order, wrapping like text, junk-free."
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
		description = "Grid keeps the curated shape: cosmetic outfits as vertical columns. List runs every item in reading order, wrapping like text, junk-free."
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
		description = "Alphabetical, or the standard spellbook's city teleports first in casting order - Varrock, Lumbridge, Falador, House, Camelot, Ardougne, Watchtower and on - with other teleports following alphabetically and jewellery after."
	)
	default TeleportOrder teleportOrder()
	{
		return TeleportOrder.ALPHABETICAL;
	}

	@ConfigItem(
		keyName = "alchPile",
		name = "",
		description = ""
	)
	void setAlchPile(boolean alchPile);

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
}

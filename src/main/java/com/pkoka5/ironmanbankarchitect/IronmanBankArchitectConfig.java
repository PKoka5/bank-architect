package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.GearOrder;
import com.pkoka5.ironmanbankarchitect.organize.PotionDoseOrder;
import com.pkoka5.ironmanbankarchitect.organize.RuneOrder;
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
		description = "How the Herblore tab is arranged.",
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
		name = "Guide on bank open",
		description = "Analyze the bank and arm the sorting guide automatically every time the bank opens, so the sidebar is never needed. Armed this way the guide stays quiet: no banners on other tabs or filtered views, and no green on already-sorted slots - only items still out of place are shown. The sidebar buttons keep working and switch the guide back to its usual form."
	)
	default boolean autoGuide()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fillGearRows",
		section = gearSection,
		position = 1,
		name = "Fill part-empty gear rows",
		description = "A bank tab cannot hold an empty slot, so the four combat-style columns only stay straight if real items fill the rest of each row. On, the grid holds its shape and an occasional unrelated item sits in a row to complete it. Off, the gear tab is laid out densely and nothing sits where it does not belong; sets still hold together."
	)
	default boolean fillGearRows()
	{
		return true;
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
		keyName = "alchPile",
		section = gearSection,
		position = 2,
		name = "Gather outclassed gear for alching",
		description = "Move equipment you own two strictly better versions of, and that is worth alching, to the Slayer & Boss Loot tab. Turn this off to keep every piece of gear in the combat gear tab, for example when you deliberately keep a spare set."
	)
	default boolean alchPile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gearOrder",
		section = gearSection,
		position = 0,
		name = "Combat gear order",
		description = "How the combat gear tab lays out. Packed grid is the aligned style-column layout. The linear orders run item after item instead: by slot compares across styles (every helmet, then every body), the style orders read each kit as one block, armour head to feet or led by its weapon."
	)
	default GearOrder gearOrder()
	{
		return GearOrder.PACKED;
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
		keyName = "utilitiesLayout",
		section = utilitiesSection,
		position = 0,
		name = "Tab layout",
		description = "Packed keeps the curated geometry: the four-wide rune block and the achievement diary grid. Sorted runs every item in the sorter's order, wrapping like text."
	)
	default TabOrder utilitiesLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "toolsLayout",
		section = toolsSection,
		position = 0,
		name = "Tab layout",
		description = "Packed keeps the curated geometry: empty containers as columns above their filled forms. Sorted runs every tool in skill order, wrapping like text."
	)
	default TabOrder toolsLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "resourcesLayout",
		section = resourcesSection,
		position = 0,
		name = "Tab layout",
		description = "Packed keeps the curated geometry: raw materials aligned above their processed forms. Sorted runs every item in the sorter's order, wrapping like text."
	)
	default TabOrder resourcesLayout()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "cluesLayout",
		section = cluesSection,
		position = 0,
		name = "Tab layout",
		description = "Packed keeps the curated geometry: cosmetic outfits as vertical columns. Sorted runs every item in the sorter's order, wrapping like text."
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
		keyName = "fillGearRows",
		name = "",
		description = ""
	)
	void setFillGearRows(boolean fillGearRows);

	@ConfigItem(
		keyName = "fillHerbloreRows",
		name = "",
		description = ""
	)
	void setFillHerbloreRows(boolean fillHerbloreRows);

	@ConfigItem(
		keyName = "alchPile",
		name = "",
		description = ""
	)
	void setAlchPile(boolean alchPile);

	@ConfigItem(
		keyName = "categoryOverlayOpacity",
		section = guidanceSection,
		position = 3,
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

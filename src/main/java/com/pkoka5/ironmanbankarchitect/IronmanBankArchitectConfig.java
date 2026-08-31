package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.GearOrder;
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
		name = "Layout",
		description = "How every tab lays its items onto its rows.",
		position = 1
	)
	String layoutSection = "Layout";

	@ConfigSection(
		name = "Combat gear",
		description = "How the combat gear tab is arranged and curated.",
		position = 2
	)
	String gearSection = "Combat gear";

	@ConfigSection(
		name = "Herblore",
		description = "How the Herblore tab is arranged.",
		position = 3
	)
	String herbloreSection = "Herblore";

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
		keyName = "tabLayout",
		section = layoutSection,
		position = 0,
		name = "Tab layout",
		description = "Packed keeps the sorters' order and nudges neighbours so a set of charge variants never breaks across a row edge. Sorted is the exact order, wrapping like text, so a set may split at the row edge."
	)
	default TabOrder tabLayout()
	{
		return TabOrder.PACKED;
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

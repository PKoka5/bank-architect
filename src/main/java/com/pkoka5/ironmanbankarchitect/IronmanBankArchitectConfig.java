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
		name = "Tab order",
		description = "How each category lays its items onto its tab. Everything defaults to the packed layout; open this section to choose a linear order per category.",
		position = 1,
		closedByDefault = true
	)
	String tabOrderSection = "Tab order";

	@ConfigSection(
		name = "Classification",
		description = "Choices about which tab an item belongs to, rather than where it sits on it.",
		position = 2
	)
	String classificationSection = "Classification";

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
		section = tabOrderSection,
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
		section = tabOrderSection,
		position = 8,
		name = "Fill part-empty Herblore rows",
		description = "On, a part-finished recipe row borrows from the rest of the tab so the next recipe still starts at the left edge. Off, a short row is left short and the recipes simply follow each other."
	)
	default boolean fillHerbloreRows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "alchPile",
		section = classificationSection,
		position = 0,
		name = "Gather outclassed gear for alching",
		description = "Move equipment you own two strictly better versions of, and that is worth alching, to the Slayer & Boss Loot tab. Turn this off to keep every piece of gear in the combat gear tab, for example when you deliberately keep a spare set."
	)
	default boolean alchPile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gearOrder",
		section = tabOrderSection,
		position = 0,
		name = "Combat gear order",
		description = "How the combat gear tab lays out. Packed grid is the aligned style-column layout. The linear orders run item after item instead: by slot compares across styles (every helmet, then every body), the style orders read each kit as one block, armour head to feet or led by its weapon."
	)
	default GearOrder gearOrder()
	{
		return GearOrder.PACKED;
	}

	@ConfigItem(
		keyName = "utilitiesOrder",
		section = tabOrderSection,
		position = 2,
		name = "Runes, teleports & currency order",
		description = "How tabs holding runes, teleports, ammunition and currency lay out. Packed rows keep charge sets whole and fill every row; a sorted run keeps like items adjacent and simply wraps, so a charge set may break across a row boundary."
	)
	default TabOrder utilitiesOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "suppliesOrder",
		section = tabOrderSection,
		position = 3,
		name = "Food & potions order",
		description = "How the food and potions tab lays out: packed rows, or the sorter's plain run."
	)
	default TabOrder suppliesOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "toolsOrder",
		section = tabOrderSection,
		position = 4,
		name = "Tools order",
		description = "How the skilling tools tab lays out: packed rows, or the sorter's plain run."
	)
	default TabOrder toolsOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "resourcesOrder",
		section = tabOrderSection,
		position = 5,
		name = "Resources order",
		description = "How the resources tab lays out: packed rows, or the sorter's plain run."
	)
	default TabOrder resourcesOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "farmingOrder",
		section = tabOrderSection,
		position = 6,
		name = "Farming order",
		description = "How the farming tab lays out: packed family runs, or the same runs without fillers moved forward to complete rows."
	)
	default TabOrder farmingOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "herbloreOrder",
		section = tabOrderSection,
		position = 7,
		name = "Herblore order",
		description = "How the Herblore tab lays out. A sorted run never pads a short recipe row; packed rows follow the Fill part-empty Herblore rows setting."
	)
	default TabOrder herbloreOrder()
	{
		return TabOrder.PACKED;
	}

	@ConfigItem(
		keyName = "cluesOrder",
		section = tabOrderSection,
		position = 9,
		name = "Clues & cosmetics order",
		description = "How the clues and cosmetics tab lays out: packed rows, or the sorter's plain run."
	)
	default TabOrder cluesOrder()
	{
		return TabOrder.PACKED;
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

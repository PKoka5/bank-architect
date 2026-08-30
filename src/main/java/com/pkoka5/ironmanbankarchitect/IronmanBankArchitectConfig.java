package com.pkoka5.ironmanbankarchitect;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(IronmanBankArchitectConfig.GROUP)
public interface IronmanBankArchitectConfig extends Config
{
	String GROUP = "ironmanbankarchitect";

	@ConfigItem(
		keyName = "suggestNextMove",
		name = "Show next manual move",
		description = "Highlight the next safe manual collapse, tab drag, or same-section reorder in the vanilla All items bank view. Guidance follows the bank's Swap or Insert mode."
	)
	default boolean suggestNextMove()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showCategoryOverlay",
		name = "Colour bank items by destination",
		description = "While Assign categories is on, tint every bank item with the colour of the blueprint tab it is planned for. Turn this off to keep the bank uncoloured even in assign mode. Drawing only; works in any bank view."
	)
	default boolean showCategoryOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fillGearRows",
		name = "Fill part-empty gear rows",
		description = "A bank tab cannot hold an empty slot, so the four combat-style columns only stay straight if real items fill the rest of each row. On, the grid holds its shape and an occasional unrelated item sits in a row to complete it. Off, the gear tab is laid out densely and nothing sits where it does not belong; sets still hold together."
	)
	default boolean fillGearRows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fillHerbloreRows",
		name = "Fill part-empty Herblore rows",
		description = "On, a part-finished recipe row borrows from the rest of the tab so the next recipe still starts at the left edge. Off, a short row is left short and the recipes simply follow each other."
	)
	default boolean fillHerbloreRows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "alchPile",
		name = "Gather outclassed gear for alching",
		description = "Move equipment you own two strictly better versions of, and that is worth alching, to the Slayer & Boss Loot tab. Turn this off to keep every piece of gear in the combat gear tab, for example when you deliberately keep a spare set."
	)
	default boolean alchPile()
	{
		return true;
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

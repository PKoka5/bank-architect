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
}

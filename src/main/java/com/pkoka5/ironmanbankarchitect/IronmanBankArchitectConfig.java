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
		description = "Highlight the next safe manual collapse, tab drag, or same-section swap in the vanilla All items bank view."
	)
	default boolean suggestNextMove()
	{
		return true;
	}
}

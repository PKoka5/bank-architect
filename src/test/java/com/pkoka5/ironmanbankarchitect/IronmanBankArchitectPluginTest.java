package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

public class IronmanBankArchitectPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IronmanBankArchitectPlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void pluginNameIsDefined()
	{
		assertEquals("Ironman Bank Architect", IronmanBankArchitectPlugin.PLUGIN_NAME);
	}
}

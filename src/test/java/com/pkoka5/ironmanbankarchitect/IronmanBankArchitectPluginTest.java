package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

public class IronmanBankArchitectPluginTest
{
	// loadBuiltin is a generic varargs method; the array creation warning at
	// the call site is inherent to RuneLite's plugin test launcher template.
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IronmanBankArchitectPlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void pluginNameIsDefined()
	{
		assertEquals("Bank Architect", IronmanBankArchitectPlugin.PLUGIN_NAME);
	}
}

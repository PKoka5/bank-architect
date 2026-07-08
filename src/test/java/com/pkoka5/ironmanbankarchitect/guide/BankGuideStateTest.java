package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BankGuideStateTest
{
	@Test
	public void withSelectedBlockKeyReturnsNewImmutableInstance()
	{
		BankGuideState original = new BankGuideState("melee-setup", false);
		BankGuideState updated = original.withSelectedBlockKey("irit-super-attack");

		assertEquals("melee-setup", original.getSelectedBlockKey());
		assertEquals("irit-super-attack", updated.getSelectedBlockKey());
		assertFalse(original.isGuideEnabled());
		assertFalse(updated.isGuideEnabled());
	}

	@Test
	public void withGuideEnabledReturnsNewImmutableInstance()
	{
		BankGuideState original = new BankGuideState("melee-setup", false);
		BankGuideState updated = original.withGuideEnabled(true);

		assertFalse(original.isGuideEnabled());
		assertTrue(updated.isGuideEnabled());
		assertEquals("melee-setup", updated.getSelectedBlockKey());
	}

	@Test
	public void equalStatesAreEqualAndHashConsistently()
	{
		BankGuideState first = new BankGuideState("melee-setup", true);
		BankGuideState second = new BankGuideState("melee-setup", true);

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	public void differingStatesAreNotEqual()
	{
		BankGuideState first = new BankGuideState("melee-setup", true);
		BankGuideState second = new BankGuideState("irit-super-attack", true);

		assertNotEquals(first, second);
	}
}

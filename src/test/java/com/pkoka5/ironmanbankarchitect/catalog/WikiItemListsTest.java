package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WikiItemListsTest
{
	@Test
	public void recognizesSpecialAttackWeaponsIncludingVariants()
	{
		assertTrue(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Dragon dagger"));
		assertTrue(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Dragon dagger(p++)"));
		assertTrue(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Magic shortbow (i)"));
		assertTrue(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Abyssal dagger"));
		assertTrue(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Ancient mace"));
		assertFalse(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Rune scimitar"));
		assertFalse(WikiItemLists.INSTANCE.isSpecialAttackWeapon("Mithril platebody"));
	}

	@Test
	public void recognizesQuestItemsByExactNameOnly()
	{
		assertTrue(WikiItemLists.INSTANCE.isQuestItem("Bullroarer"));
		assertTrue(WikiItemLists.INSTANCE.isQuestItem("Tarn's diary"));
		// Quest variants of regular items are excluded; a real whip is gear.
		assertFalse(WikiItemLists.INSTANCE.isQuestItem("Abyssal whip"));
		assertFalse(WikiItemLists.INSTANCE.isQuestItem("Coins"));
	}

	@Test
	public void baseNameStripsTrailingVariantSuffixes()
	{
		assertEquals("dragon dagger", WikiItemLists.baseName("dragon dagger(p++)"));
		assertEquals("magic shortbow", WikiItemLists.baseName("magic shortbow (i)"));
		assertEquals("abyssal whip", WikiItemLists.baseName("abyssal whip"));
	}
}

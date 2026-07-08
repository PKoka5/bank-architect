package com.pkoka5.ironmanbankarchitect.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HardcodedSlotItemMappingsTest
{
	@Test
	public void iritWorkflowUsesExactLiteralIds()
	{
		assertItemId("herblore.irit.seed", 5297);
		assertItemId("herblore.irit.grimy", 209);
		assertItemId("herblore.irit.clean", 259);
		assertItemId("herblore.irit.unf", 101);
		assertItemId("herblore.irit.secondary", 221);
		assertItemId("herblore.super-attack.3", 145);
		assertItemId("herblore.super-attack.2", 147);
		assertItemId("herblore.super-attack.1", 149);
	}

	@Test
	public void superAttackFourIsReservedForFutureCombatSupplies()
	{
		// Super attack (4), item ID 2436, is reserved for a future Potions / Consumables / PvM Supplies tab
		// and is intentionally not part of this Herblore prep row.
		assertFalse(HardcodedSlotItemMappings.forSlotKey("herblore.super-attack.4").isPresent());
	}

	@Test
	public void certAndPlaceholderIdsAreNotMapped()
	{
		assertFalse(HardcodedSlotItemMappings.forSlotKey("herblore.irit.grimy.cert").isPresent());
		assertFalse(HardcodedSlotItemMappings.forSlotKey("herblore.super-attack.placeholder").isPresent());
	}

	private static void assertItemId(String slotKey, int itemId)
	{
		assertTrue(HardcodedSlotItemMappings.forSlotKey(slotKey).isPresent());
		assertEquals(itemId, HardcodedSlotItemMappings.forSlotKey(slotKey).get().getItemId());
	}
}

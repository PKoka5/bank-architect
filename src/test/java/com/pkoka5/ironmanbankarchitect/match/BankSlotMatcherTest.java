package com.pkoka5.ironmanbankarchitect.match;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSlot;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class BankSlotMatcherTest
{
	@Test
	public void iritWorkflowReturnsOwnedWhenExactIdsExist()
	{
		BlockMatchResult result = BankSlotMatcher.match(iritBlock(), new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 2, 1),
			new BankItemSnapshot(259, 1, 2),
			new BankItemSnapshot(101, 1, 3),
			new BankItemSnapshot(221, 8, 4),
			new BankItemSnapshot(145, 1, 5),
			new BankItemSnapshot(147, 1, 6),
			new BankItemSnapshot(149, 1, 7)
		)));

		assertEquals(8, result.count(SlotMatchState.OWNED));
		assertEquals(0, result.count(SlotMatchState.MISSING));
	}

	@Test
	public void iritWorkflowReturnsMissingWhenExactIdsAreAbsent()
	{
		BlockMatchResult result = BankSlotMatcher.match(iritBlock(), new BankSnapshot(Collections.emptyList()));

		assertEquals(0, result.count(SlotMatchState.OWNED));
		assertEquals(8, result.count(SlotMatchState.MISSING));
	}

	@Test
	public void meleeSetupReturnsRoleOnly()
	{
		BlockMatchResult result = BankSlotMatcher.match(meleeBlock(), new BankSnapshot(Collections.emptyList()));

		assertEquals(8, result.count(SlotMatchState.ROLE_ONLY));
	}

	@Test
	public void emptySlotsReturnReservedEmpty()
	{
		VisualBlock block = new VisualBlock("reserved", "Reserved", "Reserved", Arrays.asList(
			BlueprintSlot.empty("future", "Future")
		));

		BlockMatchResult result = BankSlotMatcher.match(block, new BankSnapshot(Collections.emptyList()));

		assertEquals(SlotMatchState.RESERVED_EMPTY, result.getSlotResults().get(0).getState());
		assertEquals(1, result.count(SlotMatchState.RESERVED_EMPTY));
	}

	@Test
	public void resultPreservesSlotOrderAndCounts()
	{
		BlockMatchResult result = BankSlotMatcher.match(iritBlock(), new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 3),
			new BankItemSnapshot(221, 1, 5),
			new BankItemSnapshot(2436, 99, 6)
		)));

		assertEquals("herblore.irit.seed", result.getSlotResults().get(0).getSlotKey());
		assertEquals("herblore.irit.grimy", result.getSlotResults().get(1).getSlotKey());
		assertEquals("herblore.irit.clean", result.getSlotResults().get(2).getSlotKey());
		assertEquals("herblore.irit.unf", result.getSlotResults().get(3).getSlotKey());
		assertEquals("herblore.irit.secondary", result.getSlotResults().get(4).getSlotKey());
		assertEquals(2, result.count(SlotMatchState.OWNED));
		assertEquals(6, result.count(SlotMatchState.MISSING));
		assertEquals("Owned: 2 | Missing: 6 | Role-only: 0 | Reserved: 0", result.toCompactSummary());
	}

	@Test
	public void slotDetailTextShowsCompactNumberedStates()
	{
		BlockMatchResult result = BankSlotMatcher.match(iritBlock(), new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0)
		)));

		assertEquals(
			"1. Irit seed \u2014 owned\n"
				+ "2. Grimy irit \u2014 missing\n"
				+ "3. Clean irit \u2014 missing\n"
				+ "4. Irit potion (unf) \u2014 missing\n"
				+ "5. Eye of newt \u2014 missing\n"
				+ "6. Super attack (3) \u2014 missing\n"
				+ "7. Super attack (2) \u2014 missing\n"
				+ "8. Super attack (1) \u2014 missing",
			result.toSlotDetailText()
		);
	}

	private static VisualBlock meleeBlock()
	{
		return AllRoundIronmanPreset.create().getTabs().get(0).getSections().get(0).getBlocks().get(0);
	}

	private static VisualBlock iritBlock()
	{
		return AllRoundIronmanPreset.create().getTabs().get(1).getSections().get(0).getBlocks().get(0);
	}
}

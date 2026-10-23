package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

/** The gathering toggle and policy are independent preferences and part of analysis identity. */
@RunWith(Parameterized.class)
public class AlchModeOptionsTest
{
	private final BankPreset preset;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public AlchModeOptionsTest(BankPreset preset)
	{
		this.preset = preset;
	}

	@Test public void defaultsAndExistingBooleanConstructorsPreserveFlexiblePolicy()
	{
		assertEquals(AlchMode.FLEXIBLE, BankLayoutOptions.defaultFor(preset).alchMode());
		assertTrue(BankLayoutOptions.defaultFor(preset).alchPile());
		for (boolean enabled : new boolean[] {true, false})
		{
			BankLayoutOptions legacy = new BankLayoutOptions(true, false, enabled);
			assertEquals(AlchMode.FLEXIBLE, legacy.alchMode());
			assertEquals(enabled, legacy.alchPile());
			BankLayoutOptions fixed = legacy.withAlchMode(AlchMode.FIXED);
			assertEquals(AlchMode.FIXED, fixed.alchMode());
			assertEquals("Selecting a policy never enables or disables gathering", enabled, fixed.alchPile());
			assertEquals(AlchMode.FLEXIBLE, legacy.alchMode());
		}
	}

	@Test public void changingModePreservesAllOtherOptionsAndBothKindsOfSavedOrder()
	{
		Map<BankCategorySortMode, TabOrder> tabOrders = new EnumMap<>(BankCategorySortMode.class);
		for (BankCategorySortMode mode : BankCategorySortMode.values()) tabOrders.put(mode, TabOrder.SEQUENTIAL);
		BlockArrangements blocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042", "item:1044"));
		BlueprintItemOrders items = BlueprintItemOrders.parse("v2|2:11832,11834,999999|c:11832#0:2:gear:gear");
		BankLayoutOptions original = new BankLayoutOptions(false, false, false, tabOrders, GearLayout.LIST,
			PotionDoseOrder.BY_FAMILY, RuneOrder.ELEMENTAL, TeleportOrder.SPELLBOOK_FIRST, false)
			.withBlockArrangements(blocks).withItemOrders(items);
		BankLayoutOptions fixed = original.withAlchMode(AlchMode.FIXED);
		assertNotEquals(original, fixed);
		assertEquals(AlchMode.FLEXIBLE, original.alchMode());
		assertEquals(AlchMode.FIXED, fixed.alchMode());
		assertEquals("Switching back restores all preferences, including opaque saved-order state", original,
			fixed.withAlchMode(AlchMode.FLEXIBLE));
		assertEquals(blocks, fixed.blockArrangements());
		assertEquals(items.serialize(), fixed.itemOrders().serialize());
		assertFalse(fixed.alchPile());
	}

	@Test public void attachingOrReplacingSavedOrdersDoesNotResetFixedPolicy()
	{
		BankLayoutOptions fixed = BankLayoutOptions.defaultFor(preset).withAlchMode(AlchMode.FIXED);
		BlockArrangements blocks = BlockArrangements.EMPTY.withTag("cosmetics", Collections.singletonList("item:1042"));
		BlueprintItemOrders items = BlueprintItemOrders.parse("v1|2:11832,11834,999999");
		BankLayoutOptions blockFirst = fixed.withBlockArrangements(blocks).withItemOrders(items);
		BankLayoutOptions itemFirst = fixed.withItemOrders(items).withBlockArrangements(blocks);
		assertEquals(blockFirst, itemFirst);
		assertEquals(blockFirst.hashCode(), itemFirst.hashCode());
		assertEquals(AlchMode.FIXED, blockFirst.alchMode());
		assertEquals(AlchMode.FIXED, itemFirst.alchMode());
		assertEquals(items.serialize(), itemFirst.itemOrders().serialize());
		assertEquals(blocks, blockFirst.blockArrangements());
		assertEquals(fixed, blockFirst.withBlockArrangements(BlockArrangements.EMPTY).withItemOrders(BlueprintItemOrders.EMPTY));
		assertEquals(AlchMode.FLEXIBLE, BankLayoutOptions.defaultFor(preset).alchMode());
	}

	@Test public void policyChangeInvalidatesTheRetainedAnalysisContextInBothDirections()
	{
		BankLayoutOptions flexible = BankLayoutOptions.defaultFor(preset);
		BankLayoutOptions fixed = flexible.withAlchMode(AlchMode.FIXED);
		BankAnalysisRequest before = request(flexible);
		BankAnalysisRequest after = request(fixed);
		assertTrue(request(flexible.withAlchMode(AlchMode.FLEXIBLE)).sameLayoutContext(before, preset));
		assertFalse("Fixed must recompute classifications retained by Flexible", after.sameLayoutContext(before, preset));
		assertFalse("Flexible must recompute classifications retained by Fixed", before.sameLayoutContext(after, preset));
		assertTrue(request(fixed.withBlockArrangements(BlockArrangements.EMPTY)).sameLayoutContext(after, preset));
	}

	@Test public void gatheringOffAndPolicyAreIndependentPartsOfOptionsEquality()
	{
		BankLayoutOptions offFlexible = new BankLayoutOptions(true, true, false);
		BankLayoutOptions offFixed = offFlexible.withAlchMode(AlchMode.FIXED);
		assertFalse(offFlexible.alchPile());
		assertFalse(offFixed.alchPile());
		assertNotEquals("Remember the chosen policy even while gathering is off", offFlexible, offFixed);
		assertEquals(offFlexible, offFixed.withAlchMode(AlchMode.FLEXIBLE));
		assertFalse(request(offFixed).sameLayoutContext(request(offFlexible), preset));
	}

	private BankAnalysisRequest request(BankLayoutOptions options)
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1127, 3, 0), new BankItemSnapshot(11832, 0, 1, true)));
		return new BankAnalysisRequest(snapshot, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
			BankLayoutPlan.defaultFor(preset), options, preset);
	}
}

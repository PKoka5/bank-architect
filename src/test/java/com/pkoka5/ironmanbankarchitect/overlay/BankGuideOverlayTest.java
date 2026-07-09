package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.pkoka5.ironmanbankarchitect.blueprint.SlotKind;
import org.junit.Test;

public class BankGuideOverlayTest
{
	@Test
	public void normalSlotsUseNeutralPreviewColors()
	{
		assertEquals(BankGuideOverlay.fillFor(SlotKind.GEAR_ROLE), BankGuideOverlay.fillFor(SlotKind.WORKFLOW_ITEM));
		assertEquals(BankGuideOverlay.borderFor(SlotKind.GEAR_ROLE), BankGuideOverlay.borderFor(SlotKind.WORKFLOW_ITEM));
	}

	@Test
	public void reservedEmptySlotsKeepDistinctNeutralStyle()
	{
		assertNotEquals(BankGuideOverlay.fillFor(SlotKind.WORKFLOW_ITEM), BankGuideOverlay.fillFor(SlotKind.EMPTY));
		assertNotEquals(BankGuideOverlay.borderFor(SlotKind.WORKFLOW_ITEM), BankGuideOverlay.borderFor(SlotKind.EMPTY));
	}
}

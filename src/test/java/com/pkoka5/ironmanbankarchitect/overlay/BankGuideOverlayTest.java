package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class BankGuideOverlayTest
{
	@Test
	public void slotValidationDetectsCorrectItem()
	{
		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT,
			BankGuideOverlay.stateFor(previewWithItems(10, 20), 0, 10));
	}

	@Test
	public void slotValidationDetectsMisplacedKnownItem()
	{
		assertEquals(BankGuideOverlay.SlotValidationState.MISPLACED,
			BankGuideOverlay.stateFor(previewWithItems(10, 20), 0, 20));
	}

	@Test
	public void slotValidationDetectsWrongOrEmptyItem()
	{
		assertEquals(BankGuideOverlay.SlotValidationState.WRONG,
			BankGuideOverlay.stateFor(previewWithItems(10, 20), 0, 30));
		assertEquals(BankGuideOverlay.SlotValidationState.WRONG,
			BankGuideOverlay.stateFor(previewWithItems(10, 20), 0, -1));
	}

	@Test
	public void slotValidationDetectsUnknownFutureSlot()
	{
		assertEquals(BankGuideOverlay.SlotValidationState.UNKNOWN,
			BankGuideOverlay.stateFor(previewWithItems(10), 3, -1));
	}

	@Test
	public void validationColorsAreDistinct()
	{
		assertNotEquals(BankGuideOverlay.fillFor(BankGuideOverlay.SlotValidationState.CORRECT),
			BankGuideOverlay.fillFor(BankGuideOverlay.SlotValidationState.MISPLACED));
		assertNotEquals(BankGuideOverlay.borderFor(BankGuideOverlay.SlotValidationState.WRONG),
			BankGuideOverlay.borderFor(BankGuideOverlay.SlotValidationState.UNKNOWN));
	}

	private static BankOrganizationPreview previewWithItems(int... itemIds)
	{
		java.util.List<BankPreviewItem> items = new java.util.ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}

		return new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), items),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(2), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(3), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(4), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(5), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(6), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(7), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(8), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(9), Collections.emptyList())
		));
	}
}

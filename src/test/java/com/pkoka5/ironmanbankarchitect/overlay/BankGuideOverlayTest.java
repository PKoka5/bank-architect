package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import net.runelite.api.gameval.ItemID;
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
	public void slotValidationTreatsPlannedBlankSlotAsUnknown()
	{
		BankOrganizationPreview preview = previewWith(Arrays.asList(
			BankPreviewItem.blank(),
			new BankPreviewItem(10, "Item 10", 1),
			new BankPreviewItem(30, "Item 30", 1)
		));

		assertEquals(BankGuideOverlay.SlotValidationState.UNKNOWN,
			BankGuideOverlay.stateFor(preview, 0, -1));
		assertEquals(BankGuideOverlay.SlotValidationState.UNKNOWN,
			BankGuideOverlay.stateFor(preview, 0, 10));
		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT,
			BankGuideOverlay.stateFor(preview, 1, 10));
		assertEquals(BankGuideOverlay.SlotValidationState.MISPLACED,
			BankGuideOverlay.stateFor(preview, 2, 10));
	}

	@Test
	public void validationColorsAreDistinct()
	{
		assertNotEquals(BankGuideOverlay.fillFor(BankGuideOverlay.SlotValidationState.CORRECT),
			BankGuideOverlay.fillFor(BankGuideOverlay.SlotValidationState.MISPLACED));
		assertNotEquals(BankGuideOverlay.borderFor(BankGuideOverlay.SlotValidationState.WRONG),
			BankGuideOverlay.borderFor(BankGuideOverlay.SlotValidationState.UNKNOWN));
	}

	@Test
	public void placeholderWidgetIdIsCanonicalizedWithoutCollapsingOrdinaryVariants()
	{
		assertEquals(6687, BankGuideOverlay.canonicalItemId(50000, 14401, 6687));
		assertEquals(21166, BankGuideOverlay.canonicalItemId(21166, -1, 21175));
		assertEquals(-1, BankGuideOverlay.canonicalItemId(ItemID.BANK_FILLER, -1, -1));
	}

	@Test
	public void offscreenSlotDirectionsUseTheVisibleLogicalRange()
	{
		HashSet<Integer> visible = new HashSet<>(Arrays.asList(100, 101, 102));

		assertEquals("above this view", BankGuideOverlay.directionForSlot(99, visible));
		assertEquals("below this view", BankGuideOverlay.directionForSlot(103, visible));
		assertEquals("outside the visible bank area", BankGuideOverlay.directionForSlot(101, visible));
	}

	@Test
	public void itemViewportIsLimitedByBothTheItemLayerAndOuterContainer()
	{
		assertEquals(new Rectangle(20, 30, 80, 70), BankGuideOverlay.itemViewportBounds(
			new Rectangle(20, 10, 80, 200), new Rectangle(0, 30, 120, 70)));
	}

	@Test
	public void partiallyClippedSlotIsNotActionable()
	{
		Rectangle viewport = new Rectangle(10, 10, 100, 100);

		assertTrue(BankGuideOverlay.isFullyVisible(viewport,
			new Rectangle(20, 20, 30, 30)));
		assertFalse(BankGuideOverlay.isFullyVisible(viewport,
			new Rectangle(20, 95, 30, 30)));
	}

	private static BankOrganizationPreview previewWithItems(int... itemIds)
	{
		java.util.List<BankPreviewItem> items = new java.util.ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}

		return previewWith(items);
	}

	private static BankOrganizationPreview previewWith(java.util.List<BankPreviewItem> items)
	{
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

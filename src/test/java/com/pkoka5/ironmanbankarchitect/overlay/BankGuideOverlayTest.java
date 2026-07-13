package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
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
	public void slotValidationUsesPhysicalTabsThenMainInsteadOfBlueprintDisplayOrder()
	{
		BankOrganizationPreview preview = previewWithSections(
			items(9), items(1), items(3));

		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT,
			BankGuideOverlay.stateFor(preview, 0, 1));
		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT,
			BankGuideOverlay.stateFor(preview, 1, 3));
		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT,
			BankGuideOverlay.stateFor(preview, 2, 9));
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
	public void offscreenDirectionsUseWidgetGeometryInsteadOfContainerOrder()
	{
		Rectangle viewport = new Rectangle(10, 100, 100, 100);

		assertEquals("above this view", BankGuideOverlay.offscreenDirection(
			new Rectangle(20, 60, 30, 30), viewport));
		assertEquals("below this view", BankGuideOverlay.offscreenDirection(
			new Rectangle(20, 190, 30, 30), viewport));
		assertEquals("outside the visible bank area", BankGuideOverlay.offscreenDirection(
			new Rectangle(5, 120, 30, 30), viewport));
		assertEquals("off-screen", BankGuideOverlay.offscreenDirection(null, viewport));
	}

	@Test
	public void itemViewportIsLimitedByBothTheItemLayerAndOuterContainer()
	{
		assertEquals(new Rectangle(20, 30, 80, 70), BankGuideOverlay.itemViewportBounds(
			new Rectangle(20, 10, 80, 200), new Rectangle(0, 30, 120, 70)));
	}

	@Test
	public void nonOverlappingItemAndOuterBoundsFailClosed()
	{
		assertNull(BankGuideOverlay.itemViewportBounds(
			new Rectangle(0, 0, 100, 100), new Rectangle(200, 200, 100, 100)));
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

	@Test
	public void tabActionChildrenMatchTheVanillaDynamicTabTargets()
	{
		assertEquals(10, BankGuideOverlay.tabActionChildIndex(0));
		assertEquals(11, BankGuideOverlay.tabActionChildIndex(1));
		assertEquals(19, BankGuideOverlay.tabActionChildIndex(9));
	}

	@Test
	public void tabTargetActionValidationFailsClosed()
	{
		assertTrue(BankGuideOverlay.hasAction(
			new String[]{"View tab", "Collapse tab"}, "Collapse tab"));
		assertTrue(BankGuideOverlay.hasAction(new String[]{"New tab"}, "New tab"));
		assertTrue(BankGuideOverlay.hasAction(
			new String[]{"View all items", "Remove placeholders"}, "View all items"));
		assertFalse(BankGuideOverlay.hasAction(null, "View tab"));
		assertFalse(BankGuideOverlay.hasAction(new String[]{"View tab"}, "New tab"));
	}

	@Test
	public void onlyRealTabActionsUseTheTabWidgetResolver()
	{
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.COLLAPSE_TAB));
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.DRAG_TO_NEW_TAB));
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.DISTRIBUTE_TO_TAB));
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.TRANSFER_TO_TAB));
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.RETURN_TO_MAIN));
		assertFalse(BankGuideOverlay.isTabTargetMove(MoveType.SWAP_SECTION));
		assertTrue(BankGuideOverlay.isGridSwap(MoveType.SWAP_SECTION));
	}

	@Test
	public void blockedRecoveryHudKeepsTheManualActionOnShortLines()
	{
		String message = BankGuideOverlay.tabBlockedMessage(
			Status.MANUAL_RECOVERY_REQUIRED);

		assertTrue(message.contains("\nUndo that move manually,\n"));
		assertTrue(message.endsWith("Analyze My Bank again."));
	}

	@Test
	public void missingMainRecoveryTargetNamesInfinityInsteadOfNewTab()
	{
		String mainMessage = BankGuideOverlay.missingTabTargetMessage(
			MoveType.RETURN_TO_MAIN);
		String tabMessage = BankGuideOverlay.missingTabTargetMessage(
			MoveType.TRANSFER_TO_TAB);

		assertTrue(mainMessage.contains("infinity (All items)"));
		assertFalse(mainMessage.contains("New tab"));
		assertTrue(tabMessage.contains("New tab"));
	}

	@Test
	public void disabledHighlightsNeverLeakAConcreteFromToInstructionIntoTheHud()
	{
		BankOrganizationPreview preview = previewWithItems(10, 20);
		TabRouteAdvisor.Assessment assessment = TabRouteAdvisor.assess(
			new int[]{20, 10}, BankTabPlan.fromPreview(preview), new int[9]);

		String hud = BankGuideOverlay.tabHudText(assessment, false);
		assertTrue(hud.contains("Highlights disabled"));
		assertFalse(hud.contains("FROM"));
		assertFalse(hud.contains("TO"));
	}

	@Test
	public void readableHudPrefersOutsideBankAndAvoidsTheGuidedItem()
	{
		Rectangle canvas = new Rectangle(0, 0, 1200, 800);
		Rectangle grid = new Rectangle(600, 100, 300, 500);
		Rectangle source = new Rectangle(610, 110, 36, 32);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			canvas, grid, 260, 70, source, null, true);

		assertTrue(bounds.x + bounds.width < grid.x);
		assertFalse(bounds.intersects(source));
	}

	@Test
	public void hudChoosesAFreeGridCornerWhenNoOutsideSpaceExists()
	{
		Rectangle grid = new Rectangle(0, 0, 300, 300);
		Rectangle source = new Rectangle(230, 10, 36, 32);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			grid, grid, 140, 60, source, null, true);

		assertTrue(grid.contains(bounds));
		assertFalse(bounds.intersects(source));
	}

	@Test
	public void vanillaBankSearchStateMatchesTheClientScriptContract()
	{
		assertTrue(BankGuideOverlay.isBankSearching(11, ""));
		assertTrue(BankGuideOverlay.isBankSearching(0, "coins"));
		assertFalse(BankGuideOverlay.isBankSearching(0, ""));
		assertFalse(BankGuideOverlay.isBankSearching(1, "coins"));
		assertFalse(BankGuideOverlay.isBankSearching(0, null));
	}

	private static BankOrganizationPreview previewWithItems(int... itemIds)
	{
		return previewWith(items(itemIds));
	}

	private static java.util.List<BankPreviewItem> items(int... itemIds)
	{
		java.util.List<BankPreviewItem> items = new java.util.ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}
		return items;
	}

	private static BankOrganizationPreview previewWithSections(
		java.util.List<BankPreviewItem> main, java.util.List<BankPreviewItem> firstTab,
		java.util.List<BankPreviewItem> secondTab)
	{
		return new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), main),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), firstTab),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(2), secondTab),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(3), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(4), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(5), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(6), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(7), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(8), Collections.emptyList()),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(9), Collections.emptyList())
		));
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

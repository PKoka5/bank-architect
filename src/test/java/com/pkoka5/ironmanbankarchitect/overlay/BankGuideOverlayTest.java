package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.RearrangeMode;
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
import java.util.List;
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
	public void sortedSlotsKeepTheirGreenByDefault()
	{
		for (BankGuideOverlay.SlotValidationState state : BankGuideOverlay.SlotValidationState.values())
		{
			assertTrue(BankGuideOverlay.drawsValidation(state, false));
		}
	}

	@Test
	public void hidingSortedHighlightsDropsOnlyTheGreen()
	{
		assertFalse(BankGuideOverlay.drawsValidation(
			BankGuideOverlay.SlotValidationState.CORRECT, true));
		assertTrue(BankGuideOverlay.drawsValidation(
			BankGuideOverlay.SlotValidationState.MISPLACED, true));
		assertTrue(BankGuideOverlay.drawsValidation(
			BankGuideOverlay.SlotValidationState.WRONG, true));
		assertTrue(BankGuideOverlay.drawsValidation(
			BankGuideOverlay.SlotValidationState.UNKNOWN, true));
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
		assertEquals(new Rectangle(20, 30, 80, 70), BankOverlayGeometry.itemViewportBounds(
			new Rectangle(20, 10, 80, 200), new Rectangle(0, 30, 120, 70)));
	}

	@Test
	public void nonOverlappingItemAndOuterBoundsFailClosed()
	{
		assertNull(BankOverlayGeometry.itemViewportBounds(
			new Rectangle(0, 0, 100, 100), new Rectangle(200, 200, 100, 100)));
	}

	@Test
	public void partiallyClippedSlotIsNotActionable()
	{
		Rectangle viewport = new Rectangle(10, 10, 100, 100);

		assertTrue(BankOverlayGeometry.isFullyVisible(viewport,
			new Rectangle(20, 20, 30, 30)));
		assertFalse(BankOverlayGeometry.isFullyVisible(viewport,
			new Rectangle(20, 95, 30, 30)));
	}

	@Test
	public void sectionRangeUsesTabCountPrefixSums()
	{
		int[] counts = {3, 2, 4, 0, 0, 0, 0, 0, 0};

		assertArrayEquals(new int[]{0, 3}, BankGuideOverlay.sectionRangeForTab(counts, 1));
		assertArrayEquals(new int[]{3, 5}, BankGuideOverlay.sectionRangeForTab(counts, 2));
		assertArrayEquals(new int[]{5, 9}, BankGuideOverlay.sectionRangeForTab(counts, 3));
		assertNull(BankGuideOverlay.sectionRangeForTab(counts, 4));
		assertNull(BankGuideOverlay.sectionRangeForTab(counts, 0));
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
		assertTrue(BankGuideOverlay.isGridMove(MoveType.SWAP_SECTION));
		assertTrue(BankGuideOverlay.isGridMove(MoveType.INSERT_SECTION));
		assertFalse(BankGuideOverlay.isTabTargetMove(MoveType.INSERT_SECTION));
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
	public void duplicateItemsMessageFallsBackWhenNamesAreEmpty()
	{
		assertEquals("Duplicate item IDs detected.\nAnalyze again after the bank compacts.",
			BankGuideOverlay.duplicateItemsMessage(List.of()));
	}

	@Test
	public void duplicateItemsMessageShowsOneName()
	{
		assertEquals("Duplicate items detected:\nFeather"
				+ "\nRelease duplicate placeholders,\nthen run Analyze My Bank again.",
			BankGuideOverlay.duplicateItemsMessage(List.of("Feather")));
	}

	@Test
	public void duplicateItemsMessageShowsThreeNamesWithoutARemainder()
	{
		assertEquals("Duplicate items detected:\nFeather, Hammer, Lobster"
				+ "\nRelease duplicate placeholders,\nthen run Analyze My Bank again.",
			BankGuideOverlay.duplicateItemsMessage(
				List.of("Feather", "Hammer", "Lobster")));
	}

	@Test
	public void duplicateItemsMessageLimitsFiveNamesAndReportsTheRemainder()
	{
		assertEquals("Duplicate items detected:\nFeather, Hammer, Lobster (+2 more)"
				+ "\nRelease duplicate placeholders,\nthen run Analyze My Bank again.",
			BankGuideOverlay.duplicateItemsMessage(
				List.of("Feather", "Hammer", "Lobster", "Needle", "Pot")));
	}

	@Test
	public void duplicatePlaceholderRecoveryOnlyTargetsMatchingPlaceholders()
	{
		assertTrue(BankGuideOverlay.isDuplicatePlaceholder(
			6687, true, Collections.singleton(6687)));
		assertFalse(BankGuideOverlay.isDuplicatePlaceholder(
			6687, false, Collections.singleton(6687)));
		assertFalse(BankGuideOverlay.isDuplicatePlaceholder(
			4151, true, Collections.singleton(6687)));
	}

	@Test
	public void planOnlyDuplicateDoesNotMarkAUniqueActualPlaceholderForRelease()
	{
		int[] actualItemIds = {4, 7, 9};
		TabRouteAdvisor.Assessment assessment = TabRouteAdvisor.assess(actualItemIds,
			BankTabPlan.fromPreview(previewWithItems(4, 4, 9)), new int[9]);

		assertEquals(Status.DUPLICATE_ITEMS, assessment.getStatus());
		assertEquals(List.of(4), assessment.getDuplicateItemIds());
		assertTrue(BankGuideOverlay.duplicateIds(actualItemIds).isEmpty());
		assertFalse(BankGuideOverlay.isDuplicatePlaceholder(
			4, true, BankGuideOverlay.duplicateIds(actualItemIds)));
	}

	@Test
	public void actualDuplicateIdsAreDetectedIndependentlyOfThePlan()
	{
		assertEquals(Collections.singleton(4),
			BankGuideOverlay.duplicateIds(new int[]{4, 7, 4, -1}));
	}

	@Test
	public void duplicateItemsOnANumberedTabDirectThePlayerToAllItems()
	{
		assertEquals("Duplicate items detected:\nFeather"
				+ "\nOpen All items to highlight\nthe duplicate placeholders.",
			BankGuideOverlay.duplicateItemsOpenAllMessage(List.of("Feather")));
	}

	@Test
	public void duplicatePlaceholderRecoveryExplainsTheManualReleaseAction()
	{
		assertEquals("Duplicate placeholders found:\nFeather"
				+ "\nRight-click each highlighted slot,"
				+ "\nchoose Release, then Analyze again.",
			BankGuideOverlay.duplicatePlaceholderRecoveryMessage(List.of("Feather")));
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

	/**
	 * A filler leaves a hole that the view-sync check and TabRouteAdvisor both reject, so the
	 * player used to sit on SYNCING BANK forever with no idea what to do about it.
	 */
	@Test
	public void bankFillersAreNamedWithTheirCountAndTheFixToApply()
	{
		String message = BankGuideOverlay.bankFillerMessage(12);

		assertTrue(message.contains("12"));
		assertTrue(message.contains("Clear all item fillers"));
		assertFalse(message.contains("SYNCING BANK"));
	}

	@Test
	public void transientBankSyncKeepsAVisibleNeutralHudAndProgress()
	{
		assertTrue(BankGuideOverlay.viewSyncMessage(false).contains("SYNCING BANK"));
		assertTrue(BankGuideOverlay.viewSyncMessage(true).contains("this tab"));
		assertEquals(42, BankGuideOverlay.blockedProgressPercent(
			TabRouteAdvisor.Status.WAITING_FOR_BANK, 42));
		assertEquals(-1, BankGuideOverlay.blockedProgressPercent(
			TabRouteAdvisor.Status.UNSTABLE_BANK, 42));
	}

	@Test
	public void bankTagsTitleIsRecognizedAsAFilteredBankView()
	{
		assertTrue(BankGuideOverlay.isBankTagTabTitle("Tag tab herbs (35,633,407)"));
		assertTrue(BankGuideOverlay.isBankTagTabTitle("  TAG TAB gear  "));
	}

	@Test
	public void ordinaryBankTitlesAreNotTreatedAsBankTagFilters()
	{
		assertFalse(BankGuideOverlay.isBankTagTabTitle(null));
		assertFalse(BankGuideOverlay.isBankTagTabTitle("The Bank of Gielinor"));
		assertFalse(BankGuideOverlay.isBankTagTabTitle("Tab 1"));
	}

	@Test
	public void disabledHighlightsNeverLeakAConcreteFromToInstructionIntoTheHud()
	{
		BankOrganizationPreview preview = previewWithItems(10, 20);
		TabRouteAdvisor.Assessment assessment = TabRouteAdvisor.assess(
			new int[]{20, 10}, BankTabPlan.fromPreview(preview), new int[9]);

		String hud = BankGuideOverlay.tabHudText(assessment, false, RearrangeMode.SWAP);
		assertTrue(hud.contains("Highlights disabled"));
		assertFalse(hud.contains("FROM"));
		assertFalse(hud.contains("TO"));
	}

	@Test
	public void sortingHudShowsTheExactMinimumRemainingSwapCount()
	{
		BankOrganizationPreview preview = previewWithItems(10, 20, 30);
		TabRouteAdvisor.Assessment assessment = TabRouteAdvisor.assess(
			new int[]{20, 30, 10}, BankTabPlan.fromPreview(preview), new int[9]);

		String hud = BankGuideOverlay.tabHudText(assessment, true, RearrangeMode.SWAP);
		assertTrue(hud.contains("MIN SWAPS 2"));
	}

	@Test
	public void insertModeHudNamesTheDropInsteadOfASwap()
	{
		BankOrganizationPreview preview = previewWithItems(10, 20, 30);
		TabRouteAdvisor.Assessment assessment = TabRouteAdvisor.assess(
			new int[]{20, 30, 10}, BankTabPlan.fromPreview(preview), new int[9], 0,
			RearrangeMode.INSERT);

		String hud = BankGuideOverlay.tabHudText(assessment, true, RearrangeMode.INSERT);
		assertTrue(hud.contains("MIN INSERTS 1"));
		assertTrue(hud.contains("MOVE -> DROP"));
		assertFalse(hud.contains("MIN SWAPS"));
	}

	@Test
	public void readableHudPrefersOutsideBankAndAvoidsTheGuidedItem()
	{
		Rectangle canvas = new Rectangle(0, 0, 1200, 800);
		Rectangle grid = new Rectangle(600, 100, 300, 500);
		Rectangle source = new Rectangle(610, 110, 36, 32);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			canvas, grid, 260, 70, true, source);

		assertTrue(bounds.x + bounds.width < grid.x);
		assertFalse(bounds.intersects(source));
	}

	@Test
	public void hudSlidesUnderTheLegendInsteadOfMovingOntoBankSlots()
	{
		// Only the strip right of the bank is wide enough, and the legend has
		// claimed the top of it. The free space below it must win over any
		// position inside the grid.
		Rectangle canvas = new Rectangle(0, 0, 700, 500);
		Rectangle grid = new Rectangle(20, 40, 420, 400);
		Rectangle legend = new Rectangle(446, 40, 230, 60);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			canvas, grid, 200, 70, true, null, null, legend);

		assertFalse(bounds.intersects(legend));
		assertTrue(canvas.contains(bounds));
		assertTrue("must stay clear of the bank grid", bounds.x >= grid.x + grid.width);
		assertTrue("must sit below the legend", bounds.y >= legend.y + legend.height);
	}

	@Test
	public void hudIgnoresAReleasedLegendClaim()
	{
		Rectangle canvas = new Rectangle(0, 0, 1200, 800);
		Rectangle grid = new Rectangle(600, 100, 300, 500);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			canvas, grid, 260, 70, true, null, null, null);

		assertTrue(bounds.x + bounds.width < grid.x);
	}

	@Test
	public void hudChoosesAFreeGridCornerWhenNoOutsideSpaceExists()
	{
		Rectangle grid = new Rectangle(0, 0, 300, 300);
		Rectangle source = new Rectangle(230, 10, 36, 32);

		Rectangle bounds = BankGuideOverlay.statusBounds(
			grid, grid, 140, 60, true, source);

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

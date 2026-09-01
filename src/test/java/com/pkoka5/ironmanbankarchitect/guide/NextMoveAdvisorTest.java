package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.GuideProgress;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.NextMove;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Session;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import org.junit.Test;

public class NextMoveAdvisorTest
{
	@Test
	public void completeOrderProducesNoMove()
	{
		Assessment result = NextMoveAdvisor.assess(new int[]{10, 20, 30}, plan(10, 20, 30));

		assertEquals(Status.COMPLETE, result.getStatus());
		assertFalse(result.getMove().isPresent());
		assertEquals(100, result.getProgress().getPercent());
	}

	@Test
	public void choosesTheShortestUsefulSwap()
	{
		Assessment result = NextMoveAdvisor.assess(
			new int[]{40, 20, 30, 10, 50, 70, 60}, plan(10, 20, 30, 40, 50, 60, 70));

		assertEquals(Status.READY, result.getStatus());
		NextMove move = result.getMove().get();
		assertEquals(60, move.getItemId());
		assertEquals(6, move.getFromSlot());
		assertEquals(5, move.getToSlot());
	}

	@Test
	public void prefersAMoveWhoseSourceAndTargetAreBothVisible()
	{
		Assessment result = NextMoveAdvisor.assess(
			new int[]{40, 20, 30, 10, 60, 50}, plan(10, 20, 30, 40, 50, 60),
			new HashSet<>(Arrays.asList(0, 3)));

		NextMove move = result.getMove().get();
		assertEquals(10, move.getItemId());
		assertEquals(3, move.getFromSlot());
		assertEquals(0, move.getToSlot());
	}

	@Test
	public void fallsBackToAMoveWithOneVisibleEndpointBeforeSearchingElsewhere()
	{
		Assessment result = NextMoveAdvisor.assess(
			new int[]{40, 20, 30, 10, 60, 50}, plan(10, 20, 30, 40, 50, 60),
			new HashSet<>(Arrays.asList(0)));

		NextMove move = result.getMove().get();
		assertEquals(0, move.getToSlot());
		assertEquals(3, move.getFromSlot());
	}

	@Test
	public void sessionPinsTheMoveWhileOnlyTheViewportChanges()
	{
		int[] actual = {30, 10, 20, 40, 50};
		List<BankPreviewItem> planned = plan(10, 20, 30, 40, 50);
		Session session = new Session();

		NextMove first = session.assess(actual, planned,
			new HashSet<>(Arrays.asList(2, 3, 4))).getMove().get();
		NextMove afterScroll = session.assess(actual, planned,
			new HashSet<>(Arrays.asList(0, 1, 2))).getMove().get();

		assertEquals(first.getItemId(), afterScroll.getItemId());
		assertEquals(first.getFromSlot(), afterScroll.getFromSlot());
		assertEquals(first.getToSlot(), afterScroll.getToSlot());
	}

	@Test
	public void sessionSelectsANewMoveAfterTheBankOrderChanges()
	{
		int[] actual = {30, 10, 20};
		List<BankPreviewItem> planned = plan(10, 20, 30);
		Session session = new Session();
		NextMove first = session.assess(actual, planned,
			new HashSet<>(Arrays.asList(0, 1, 2))).getMove().get();

		int displaced = actual[first.getToSlot()];
		actual[first.getToSlot()] = actual[first.getFromSlot()];
		actual[first.getFromSlot()] = displaced;
		Assessment next = session.assess(actual, planned,
			new HashSet<>(Arrays.asList(0, 1, 2)));

		assertEquals(Status.READY, next.getStatus());
		assertTrue(next.getProgress().getCorrectSlots() > 0);
		assertFalse(first.getItemId() == next.getMove().get().getItemId()
			&& first.getToSlot() == next.getMove().get().getToSlot());
	}

	@Test
	public void sessionInvalidatesWhenTheSamePlanListIsChanged()
	{
		int[] actual = {20, 10, 30};
		List<BankPreviewItem> planned = plan(10, 20, 30);
		Session session = new Session();

		assertEquals(Status.READY, session.assess(actual, planned,
			new HashSet<>(Arrays.asList(0, 1, 2))).getStatus());
		planned.set(0, item(20));
		planned.set(1, item(10));

		assertEquals(Status.COMPLETE, session.assess(actual, planned,
			new HashSet<>(Arrays.asList(0, 1, 2))).getStatus());
	}

	@Test
	public void repeatedLiveSwapAdviceTerminatesCyclesMonotonically()
	{
		int[] actual = {30, 10, 20, 40};
		List<BankPreviewItem> planned = plan(10, 20, 30, 40);
		int previousCorrect = 1;

		for (int step = 0; step < 4; step++)
		{
			Assessment result = NextMoveAdvisor.assess(actual, planned);
			if (result.getStatus() == Status.COMPLETE)
			{
				assertTrue(step > 0);
				return;
			}

			assertEquals(Status.READY, result.getStatus());
			NextMove move = result.getMove().get();
			int displaced = actual[move.getToSlot()];
			actual[move.getToSlot()] = actual[move.getFromSlot()];
			actual[move.getFromSlot()] = displaced;
			int correct = NextMoveAdvisor.progress(actual, planned).getCorrectSlots();
			assertTrue(correct > previousCorrect);
			previousCorrect = correct;
		}

		throw new AssertionError("cycle did not converge");
	}

	@Test
	public void missingOrExtraItemsRequireARescanInsteadOfALaterMove()
	{
		assertEquals(Status.RESCAN_REQUIRED,
			NextMoveAdvisor.assess(new int[]{20, 30}, plan(10, 20, 30)).getStatus());
		assertEquals(Status.RESCAN_REQUIRED,
			NextMoveAdvisor.assess(new int[]{10, 20, 30, 40}, plan(10, 20, 30)).getStatus());
	}

	@Test
	public void equalChargedCopiesAreInterchangeableDuringSorting()
	{
		int usedEclipseMoonChestplate = 29031;
		Assessment result = NextMoveAdvisor.assess(
			new int[]{usedEclipseMoonChestplate, 20, usedEclipseMoonChestplate},
			plan(usedEclipseMoonChestplate, usedEclipseMoonChestplate, 20));

		assertEquals(Status.READY, result.getStatus());
		assertTrue(result.getMove().isPresent());
	}

	@Test
	public void extraRepeatedLiveEntryStillRequiresDuplicateRecovery()
	{
		Assessment result = NextMoveAdvisor.assess(new int[]{10, 20, 10}, plan(10, 20));

		assertEquals(Status.DUPLICATE_ITEMS, result.getStatus());
		assertEquals(List.of(10), result.getDuplicateItemIds());
	}

	@Test
	public void realGapInsideDensePlanIsNotPresentedAsAStableTarget()
	{
		Assessment result = NextMoveAdvisor.assess(new int[]{20, -1, 10}, plan(10, 20));

		assertEquals(Status.UNSTABLE_TARGET, result.getStatus());
		assertFalse(result.getMove().isPresent());
	}

	@Test
	public void plannedBlankSlotsAreExplicitlyUnsupported()
	{
		List<BankPreviewItem> planned = Arrays.asList(item(10), BankPreviewItem.blank(), item(20));

		assertEquals(Status.UNSUPPORTED_PLAN,
			NextMoveAdvisor.assess(new int[]{10, 20}, planned).getStatus());
	}

	@Test
	public void categoryProgressUsesItsAbsoluteBankOffset()
	{
		GuideProgress progress = NextMoveAdvisor.progress(
			new int[]{1, 2, 30, 40, 50}, plan(30, 50, 40), 2);

		assertEquals(1, progress.getCorrectSlots());
		assertEquals(3, progress.getPlannedSlots());
		assertEquals(33, progress.getPercent());
	}

	private static List<BankPreviewItem> plan(int... itemIds)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(item(itemId));
		}
		return items;
	}

	private static BankPreviewItem item(int itemId)
	{
		return new BankPreviewItem(itemId, "Item " + itemId, 1);
	}
}

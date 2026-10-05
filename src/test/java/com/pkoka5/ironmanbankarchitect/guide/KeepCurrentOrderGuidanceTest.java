package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class KeepCurrentOrderGuidanceTest
{
	private static final BankLayoutPlan DEFAULT = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);

	@Test public void logicalDestinationKeepsItsIdentityWhenEarlierEmptyTabsAreCompressed()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(3, true));
		assertEquals(4, plan.getNumberedTabs().get(0).getBlueprintCategoryNumber());
		assertEquals(7, plan.getNumberedTabs().get(1).getBlueprintCategoryNumber());
		int[] live = {42, 40, 41, 72, 70, 71, 12, 10, 11};
		assertEquals(Arrays.asList(42, 40, 41, 70, 71, 72, 10, 11, 12),
			ids(plan.effectiveItems(live, counts(3, 3))));
		assertEquals(Arrays.asList(40, 41, 42, 70, 71, 72, 10, 11, 12),
			ids(plan.getFlattenedItems()));
	}

	@Test public void mainAndPreservedTabCountAsSortedWhileAnotherTabStillNeedsWork()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		for (RearrangeMode mode : RearrangeMode.values())
		{
			List<Integer> live = new ArrayList<>(Arrays.asList(42, 40, 41, 72, 70, 71, 12, 10, 11));
			TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
			for (int step = 0; step < 4; step++)
			{
				TabRouteAdvisor.Assessment result = session.assess(array(live), plan, counts(3, 3),
					step + 1, 1, mode);
				if (result.getStatus() == TabRouteAdvisor.Status.COMPLETE)
				{
					assertEquals(Arrays.asList(42, 40, 41, 70, 71, 72, 12, 10, 11), live);
					assertEquals(100, result.getProgress().getPercent());
					assertEquals(0, result.getProgress().getRemainingDragsEstimate());
					assertFalse(result.getMove().isPresent());
					break;
				}
				assertEquals(TabRouteAdvisor.Status.READY, result.getStatus());
				if (step == 0)
				{
					assertEquals(6, result.getProgress().getCompleted());
					assertEquals(9, result.getProgress().getTotal());
					assertEquals(mode == RearrangeMode.INSERT ? 1 : 2,
						result.getProgress().getRemainingDragsEstimate());
				}
				TabRouteAdvisor.Move move = result.getMove().get();
				assertEquals(2, move.getTargetTab());
				assertEquals(7, move.getBlueprintTabNumber());
				assertTrue(move.getFromSlot() >= 3 && move.getFromSlot() < 6);
				assertTrue(move.getToSlot() >= 3 && move.getToSlot() < 6);
				if (mode == RearrangeMode.SWAP) Collections.swap(live, move.getFromSlot(), move.getToSlot());
				else live.add(move.getToSlot(), live.remove(move.getFromSlot()));
				if (step == 3) fail("Normal ordered tab did not finish in " + mode);
			}
		}
	}

	@Test public void preservedTabStillReturnsForeignItemsAndReceivesMissingMembers()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		for (RearrangeMode mode : RearrangeMode.values())
		{
			int[] foreign = {42, 10, 40, 70, 71, 72, 12, 41, 11};
			assertEquals(Arrays.asList(42, 40, 41, 70, 71, 72, 12, 11, 10),
				ids(plan.effectiveItems(foreign, counts(3, 3))));
			TabRouteAdvisor.Assessment outgoing = TabRouteAdvisor.assess(foreign, plan, counts(3, 3), 1, mode);
			assertEquals(TabRouteAdvisor.Status.READY, outgoing.getStatus());
			assertEquals(TabRouteAdvisor.MoveType.RETURN_TO_MAIN, outgoing.getMove().get().getType());
			assertEquals(10, outgoing.getMove().get().getItemId());
			assertEquals(1, outgoing.getMove().get().getSourceTab());
			int[] afterReturn = {42, 40, 70, 71, 72, 12, 41, 11, 10};
			TabRouteAdvisor.Assessment incoming = TabRouteAdvisor.assess(afterReturn, plan, counts(2, 3), 1, mode);
			assertEquals(TabRouteAdvisor.Status.READY, incoming.getStatus());
			assertEquals(TabRouteAdvisor.MoveType.DISTRIBUTE_TO_TAB, incoming.getMove().get().getType());
			assertEquals(41, incoming.getMove().get().getItemId());
			assertEquals(1, incoming.getMove().get().getTargetTab());
			int[] afterAppend = {42, 40, 41, 70, 71, 72, 12, 11, 10};
			assertEquals(TabRouteAdvisor.Status.COMPLETE,
				TabRouteAdvisor.assess(afterAppend, plan, counts(3, 3), 1, mode).getStatus());
		}
	}

	@Test public void completedBankCanCloseReopenAndKeepLaterIntentionalRearrangements()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		for (RearrangeMode mode : RearrangeMode.values())
		{
			TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
			int[] finished = {42, 40, 41, 70, 71, 72, 12, 10, 11};
			assertEquals(TabRouteAdvisor.Status.COMPLETE,
				session.assess(finished, plan, counts(3, 3), 1, 0, mode).getStatus());
			session.reset();
			assertEquals(TabRouteAdvisor.Status.COMPLETE,
				session.assess(finished, plan, counts(3, 3), 2, 0, mode).getStatus());
			int[] rearranged = {41, 42, 40, 70, 71, 72, 10, 11, 12};
			assertEquals(TabRouteAdvisor.Status.WAITING_FOR_BANK,
				session.assess(rearranged, plan, counts(3, 3), 3, 0, mode).getStatus());
			TabRouteAdvisor.Assessment settled = session.assess(rearranged, plan, counts(3, 3), 4, 0, mode);
			assertEquals(TabRouteAdvisor.Status.COMPLETE, settled.getStatus());
			assertEquals(100, settled.getProgress().getPercent());
		}
	}

	@Test public void intentionalKeptTabMoveWhileOtherGuidanceIsPinnedWaitsThenResumes()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		for (RearrangeMode mode : RearrangeMode.values())
		{
			TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
			int[] original = {42, 40, 41, 72, 70, 71, 12, 10, 11};
			assertEquals(TabRouteAdvisor.Status.READY,
				session.assess(original, plan, counts(3, 3), 1, 0, mode).getStatus());
			int[] changed = {41, 42, 40, 72, 70, 71, 12, 10, 11};
			assertEquals(TabRouteAdvisor.Status.WAITING_FOR_BANK,
				session.assess(changed, plan, counts(3, 3), 2, 0, mode).getStatus());
			TabRouteAdvisor.Assessment resumed = session.assess(changed, plan, counts(3, 3), 3, 0, mode);
			assertEquals(TabRouteAdvisor.Status.READY, resumed.getStatus());
			assertEquals(2, resumed.getMove().get().getTargetTab());
			assertEquals(7, resumed.getMove().get().getBlueprintTabNumber());
		}
	}

	@Test public void preserveModeDoesNotMakeMissingItemsOrInvalidTabCountsSafe()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		assertNotEquals(TabRouteAdvisor.Status.COMPLETE, TabRouteAdvisor.assess(
			new int[]{42, 40, 41, 70, 71, 72, 10, 11}, plan, counts(3, 3)).getStatus());
		assertEquals(TabRouteAdvisor.Status.UNSTABLE_BANK, TabRouteAdvisor.assess(
			new int[]{42, 40, 41, 70, 71, 72, 12, 10, 11}, plan, counts(3, 0, 3)).getStatus());
	}

	@Test public void repeatedOwnedAndPlaceholderItemsKeepEveryOriginalOccurrence()
	{
		BankPreviewItem owned = new BankPreviewItem(CatalogItem.unknown(4151), 7).withBlueprintOccurrence(0);
		BankPreviewItem placeholder = new BankPreviewItem(CatalogItem.unknown(4151), 0, true).withBlueprintOccurrence(1);
		BankPreviewItem distinct = new BankPreviewItem(11840, "Dragon boots", 1);
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++) categories.add(new BankCategoryPreview(
			BankPresets.IRONMAN.getCategories().get(index), index == 3 ? Arrays.asList(owned, distinct, placeholder)
				: Collections.emptyList()));
		BankTabPlan plan = BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories),
			DEFAULT.withCurrentOrder(3, true));
		List<BankPreviewItem> projected = plan.effectiveItems(new int[]{4151, 4151, 11840}, counts(3));
		assertEquals(3, projected.size());
		assertSame(owned, projected.get(0));
		assertSame(placeholder, projected.get(1));
		assertSame(distinct, projected.get(2));
		assertEquals(7, projected.get(0).getQuantity());
		assertTrue(projected.get(1).isPlaceholder());
		assertEquals(0, projected.get(1).getQuantity());
	}

	@Test public void laterItemOrderAndPhysicalTabCountsRefreshTheProjection()
	{
		BankTabPlan plan = plan(DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true));
		int[] live = {42, 40, 41, 70, 71, 72, 12, 10, 11};
		int[] counts = counts(3, 3);
		assertEquals(Arrays.asList(42, 40, 41, 70, 71, 72, 12, 10, 11), ids(plan.effectiveItems(live, counts)));
		live[0] = 40;
		live[1] = 42;
		assertEquals(Arrays.asList(40, 42, 41, 70, 71, 72, 12, 10, 11), ids(plan.effectiveItems(live, counts)));
		int[] changedMembership = {40, 42, 70, 71, 72, 12, 41, 10, 11};
		assertEquals(Arrays.asList(40, 42, 41, 70, 71, 72, 12, 10, 11),
			ids(plan.effectiveItems(changedMembership, counts(2, 3))));
		assertEquals(Arrays.asList(40, 42, 41, 70, 71, 72, 12, 10, 11),
			ids(plan.effectiveItems(live, counts)));
	}

	private static BankTabPlan plan(BankLayoutPlan layout)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++)
		{
			int[] ids = index == 0 ? new int[]{10, 11, 12} : index == 3 ? new int[]{40, 41, 42}
				: index == 6 ? new int[]{70, 71, 72} : new int[0];
			List<BankPreviewItem> items = new ArrayList<>();
			for (int id : ids) items.add(new BankPreviewItem(id, "Item " + id, 1));
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index), items));
		}
		return BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories), layout);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		List<Integer> result = new ArrayList<>();
		for (BankPreviewItem item : items) result.add(item.getItemId());
		return result;
	}

	private static int[] array(List<Integer> items) { return items.stream().mapToInt(Integer::intValue).toArray(); }
	private static int[] counts(int... values)
	{
		int[] result = new int[9];
		System.arraycopy(values, 0, result, 0, values.length);
		return result;
	}
}

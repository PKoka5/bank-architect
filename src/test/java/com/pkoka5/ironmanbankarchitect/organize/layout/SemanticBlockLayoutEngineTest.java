package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SemanticBlockLayoutEngineTest
{
	private final SemanticBlockLayoutEngine engine = new SemanticBlockLayoutEngine();

	@Test
	public void fallbackOrderMustBeAnExactUniquePermutation()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		List<List<Integer>> invalid = Arrays.asList(
			null,
			Collections.singletonList(10),
			Arrays.asList(10, 10),
			Arrays.asList(10, 99),
			Arrays.asList(10, (Integer) null));

		for (List<Integer> fallback : invalid)
		{
			LayoutResult result = engine.plan(request, fallback);
			assertFalse(result.isSuccess());
			assertTrue(result.getPlacements().isEmpty());
			assertEquals(1, result.getConflicts().size());
			assertEquals(LayoutConflict.Type.FALLBACK_ORDER_NOT_PERMUTATION,
				result.getConflicts().get(0).getType());
		}
	}

	@Test
	public void existingRequestConflictsRemainTheFirstValidationBoundary()
	{
		LayoutRequest request = request(entry(10, 0), entry(10, 1));
		LayoutResult result = engine.plan(request, Arrays.asList(10, 10));

		assertFalse(result.isSuccess());
		assertEquals(1, result.getConflicts().size());
		assertEquals(LayoutConflict.Type.DUPLICATE_ITEM_ID, result.getConflicts().get(0).getType());
	}

	@Test
	public void emptyAndSingleItemRequestsProduceCanonicalDensePlans()
	{
		LayoutResult empty = engine.plan(request(), Collections.emptyList());
		assertTrue(empty.isSuccess());
		assertTrue(empty.getPlacements().isEmpty());

		BankPreviewItem item = new BankPreviewItem(10, "Canonical item", 7);
		LayoutRequest singleRequest = new LayoutRequest(
			Collections.singletonList(LayoutEntry.of(item, 42)), Collections.emptyList());
		LayoutResult single = engine.plan(singleRequest, Collections.singletonList(10));
		assertTrue(single.isSuccess());
		assertEquals(1, single.getPlacements().size());
		assertEquals(0, single.getPlacements().get(0).getTargetIndex());
		assertSame(item, single.getPlacements().get(0).getItem());
		assertTrue(LayoutPlanValidator.validate(singleRequest, single.getPlacements()).isSuccess());

		try
		{
			single.getPlacements().add(new LayoutPlacement(item, 1));
			fail("expected immutable result placements");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	@Test
	public void explicitFallbackOrderIsIndependentOfEntryInsertionOrder()
	{
		List<LayoutEntry> entries = Arrays.asList(entry(10, 7), entry(20, 2), entry(30, 99));
		List<LayoutEntry> reversed = new ArrayList<>(entries);
		Collections.reverse(reversed);
		List<Integer> fallback = Arrays.asList(30, 10, 20);

		LayoutResult forward = engine.plan(new LayoutRequest(entries, Collections.emptyList()), fallback);
		LayoutResult backward = engine.plan(new LayoutRequest(reversed, Collections.emptyList()), fallback);

		assertEquals(fallback, targetOrder(forward));
		assertEquals(targetOrder(forward), targetOrder(backward));
	}

	@Test
	public void generatedDensePlanIsAcceptedByExistingManualGuidance()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1), entry(30, 2));
		LayoutResult result = engine.plan(request, Arrays.asList(20, 10, 30));
		List<BankPreviewItem> plannedItems = plannedItems(result);

		assertEquals(NextMoveAdvisor.Status.COMPLETE,
			NextMoveAdvisor.assess(new int[]{20, 10, 30}, plannedItems).getStatus());
		assertEquals(NextMoveAdvisor.Status.READY,
			NextMoveAdvisor.assess(new int[]{10, 20, 30}, plannedItems).getStatus());
	}

	private static LayoutRequest request(LayoutEntry... entries)
	{
		return new LayoutRequest(Arrays.asList(entries), Collections.emptyList());
	}

	private static LayoutEntry entry(int itemId, int sourceSlot)
	{
		return LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), sourceSlot);
	}

	private static List<Integer> targetOrder(LayoutResult result)
	{
		assertTrue(result.getConflicts().toString(), result.isSuccess());
		List<Integer> ids = new ArrayList<>();
		for (LayoutPlacement placement : result.getPlacements())
		{
			ids.add(placement.getItem().getItemId());
		}
		return ids;
	}

	private static List<BankPreviewItem> plannedItems(LayoutResult result)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (LayoutPlacement placement : result.getPlacements())
		{
			items.add(placement.getItem());
		}
		return items;
	}
}

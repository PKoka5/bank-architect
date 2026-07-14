package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class BoundedLayoutPackerTest
{
	@Test
	public void nonZeroGridOffsetPlacesWideRunOnARealPhysicalRowUsingOnlyRealFallback()
	{
		SemanticRule run = rule("offset.run", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(7), atom("offset.family", 1, 2, 3, 4, 5, 6, 7));
		List<Integer> fallback = ids(1, 13);
		LayoutRequest request = request(entries(fallback), run).withGridStartColumn(2);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertPositions(outcome, positions(1, 6, 2, 7, 3, 8, 4, 9, 5, 10, 6, 11, 7, 12));
		assertSingleBlock(outcome, ShapePrimitive.HORIZONTAL_RUN, 7, 1, 0);
		assertEquals(Arrays.asList(8, 9, 10, 11, 12, 13, 1, 2, 3, 4, 5, 6, 7),
			targetOrder(outcome));
		assertValid(request, outcome);
	}

	@Test
	public void nonZeroGridOffsetFallsBackWhenNoRealItemsCanReachAValidFootprint()
	{
		SemanticRule run = rule("offset.run", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(7), atom("offset.family", 1, 2, 3, 4, 5, 6, 7));
		List<Integer> fallback = ids(1, 7);
		LayoutRequest request = request(entries(fallback), run).withGridStartColumn(2);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, targetOrder(outcome));
		assertValid(request, outcome);
	}

	@Test
	public void nonZeroGridOffsetKeepsStageMatrixRowsPhysicallyAligned()
	{
		SemanticRule matrix = rule("offset.matrix", ShapePrimitive.STAGE_MATRIX,
			ConfidenceTier.HIGH, widths(5), atom("a", 1, 11), atom("b", 2, 12),
			atom("c", 3, 13), atom("d", 4, 14), atom("e", 5, 15));
		List<Integer> fallback = ids(1, 17);
		LayoutRequest request = request(entries(fallback), matrix).withGridStartColumn(4);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertPositions(outcome, positions(1, 4, 2, 5, 3, 6, 4, 7, 5, 8,
			11, 12, 12, 13, 13, 14, 14, 15, 15, 16));
		assertSingleBlock(outcome, ShapePrimitive.STAGE_MATRIX, 5, 1, 0);
		assertValid(request, outcome);
	}

	@Test
	public void everyNonZeroOffsetKeepsHorizontalMeaningfulCellsOnOnePhysicalRow()
	{
		for (int offset = 1; offset <= 7; offset++)
		{
			SemanticRule run = rule("offset." + offset, ShapePrimitive.HORIZONTAL_RUN,
				ConfidenceTier.HIGH, widths(2), atom("pair." + offset, 1, 2));
			int size = 8 - offset + 2;
			List<Integer> fallback = ids(1, size);
			LayoutRequest request = request(entries(fallback), run).withGridStartColumn(offset);

			BoundedLayoutPacker.Outcome outcome = plan(request, fallback);
			Map<Integer, Integer> positions = placementPositions(outcome);
			int firstPhysical = offset + positions.get(1);
			int secondPhysical = offset + positions.get(2);
			assertEquals("offset " + offset, firstPhysical / 8, secondPhysical / 8);
			assertEquals("offset " + offset, firstPhysical + 1, secondPhysical);
			assertValid(request, outcome);
		}
	}

	@Test
	public void localLockUsesTheShiftedPhysicalOrigin()
	{
		SemanticRule run = rule("offset.lock", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("locked.pair", 1, 2));
		List<Integer> fallback = ids(1, 7);
		LayoutRequest request = request(entriesWithLocks(fallback, positions(1, 5)), run)
			.withGridStartColumn(3);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertPositions(outcome, positions(1, 5, 2, 6));
		assertSingleBlock(outcome, ShapePrimitive.HORIZONTAL_RUN, 2, 1, 0);
		assertValid(request, outcome);
	}

	@Test
	public void allFourPrimitivesUseTheirCanonicalPhysicalGeometry()
	{
		SemanticRule horizontal = rule("horizontal.rule", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(3), atom("horizontal.family", 1, 2, 3));
		LayoutRequest horizontalRequest = request(entries(ids(1, 3)), horizontal);
		BoundedLayoutPacker.Outcome horizontalOutcome = plan(horizontalRequest, ids(1, 3));
		assertPositions(horizontalOutcome, positions(1, 0, 2, 1, 3, 2));
		assertSingleBlock(horizontalOutcome, ShapePrimitive.HORIZONTAL_RUN, 3, 0, 0);

		SemanticRule vertical = rule("vertical.rule", ShapePrimitive.VERTICAL_RUN,
			ConfidenceTier.HIGH, widths(1), atom("vertical.family", 1, 2, 3));
		LayoutRequest verticalRequest = request(entries(ids(1, 17)), vertical);
		BoundedLayoutPacker.Outcome verticalOutcome = plan(verticalRequest, ids(1, 17));
		assertPositions(verticalOutcome, positions(1, 0, 2, 8, 3, 16));
		assertSingleBlock(verticalOutcome, ShapePrimitive.VERTICAL_RUN, 1, 0, 0);

		SemanticRule stage = rule("stage.rule", ShapePrimitive.STAGE_MATRIX,
			ConfidenceTier.HIGH, widths(3), atom("stage.a", 10, 11),
			atom("stage.b", 20, 21), atom("stage.c", 30, 31));
		List<Integer> stageIds = Arrays.asList(10, 11, 20, 21, 30, 31, 100, 101, 102, 103, 104);
		LayoutRequest stageRequest = request(entries(stageIds), stage);
		BoundedLayoutPacker.Outcome stageOutcome = plan(stageRequest, stageIds);
		assertPositions(stageOutcome, positions(10, 0, 20, 1, 30, 2, 11, 8, 21, 9, 31, 10));
		assertSingleBlock(stageOutcome, ShapePrimitive.STAGE_MATRIX, 3, 0, 0);

		SemanticRule rows = rule("rows.rule", ShapePrimitive.ROW_GROUP_MATRIX,
			ConfidenceTier.HIGH, widths(3), atom("row.a", 40, 41, 42), atom("row.b", 50, 51));
		List<Integer> rowIds = Arrays.asList(40, 41, 42, 50, 51, 110, 111, 112, 113, 114, 115);
		LayoutRequest rowRequest = request(entries(rowIds), rows);
		BoundedLayoutPacker.Outcome rowOutcome = plan(rowRequest, rowIds);
		assertPositions(rowOutcome, positions(40, 0, 41, 1, 42, 2, 50, 8, 51, 9));
		assertSingleBlock(rowOutcome, ShapePrimitive.ROW_GROUP_MATRIX, 3, 0, 0);

		assertValid(horizontalRequest, horizontalOutcome);
		assertValid(verticalRequest, verticalOutcome);
		assertValid(stageRequest, stageOutcome);
		assertValid(rowRequest, rowOutcome);
	}

	@Test
	public void threeAndFiveWideBlocksShareOnePhysicalRowSideBySide()
	{
		SemanticRule three = rule("a.three", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), atom("three.family", 1, 2, 3));
		SemanticRule five = rule("b.five", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(5), atom("five.family", 4, 5, 6, 7, 8));
		List<Integer> fallback = ids(1, 8);
		LayoutRequest request = request(entries(fallback), five, three);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertEquals(fallback, targetOrder(outcome));
		assertEquals(2, outcome.getTieKey().getBlocks().size());
		assertEquals(0, outcome.getTieKey().getBlocks().get(0).getStartColumn());
		assertEquals(3, outcome.getTieKey().getBlocks().get(1).getStartColumn());
		assertValid(request, outcome);
	}

	@Test
	public void meaningfulCellsDoNotReserveNominalSlackAndForeignSlackLockRemainsValid()
	{
		SemanticRule wide = rule("a.wide", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(5), atom("wide.family", 1, 2, 3));
		SemanticRule narrow = rule("b.narrow", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("narrow.family", 4, 5));
		List<Integer> fallback = ids(1, 5);
		LayoutRequest request = request(entriesWithLocks(fallback, positions(4, 3)), narrow, wide);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertEquals(2, outcome.getTieKey().getBlocks().size());
		assertPositions(outcome, positions(1, 0, 2, 1, 3, 2, 4, 3, 5, 4));
		assertEquals(2, outcome.getScore().getNominalFootprintSlack());
		assertValid(request, outcome);
	}

	@Test
	public void locksForceOneOriginAndInconsistentCandidateLocksFallBackSafely()
	{
		SemanticRule stage = rule("locked.stage", ShapePrimitive.STAGE_MATRIX, ConfidenceTier.HIGH,
			widths(2), atom("locked.a", 10, 11), atom("locked.b", 20, 21));
		List<Integer> itemIds = new ArrayList<>(Arrays.asList(10, 11, 20, 21));
		itemIds.addAll(ids(100, 114));

		LayoutRequest forcedRequest = request(entriesWithLocks(itemIds, positions(10, 9, 21, 18)), stage);
		BoundedLayoutPacker.Outcome forced = plan(forcedRequest, itemIds);
		assertPositions(forced, positions(10, 9, 20, 10, 11, 17, 21, 18));
		assertSingleBlock(forced, ShapePrimitive.STAGE_MATRIX, 2, 1, 1);
		assertValid(forcedRequest, forced);

		LayoutRequest inconsistentRequest = request(
			entriesWithLocks(itemIds, positions(10, 0, 21, 17)), stage);
		BoundedLayoutPacker.Outcome inconsistent = plan(inconsistentRequest, itemIds);
		assertTrue(inconsistent.getTieKey().getBlocks().isEmpty());
		assertEquals(2, inconsistent.getScore().getHighMissedRelations());
		assertEquals(2000, inconsistent.getScore().getHighMissedCompleteness());
		assertPositions(inconsistent, positions(10, 0, 21, 17));
		assertValid(inconsistentRequest, inconsistent);
	}

	@Test
	public void denseFallbackRespectsLocksWithoutTreatingThemAsImmovableSourceSlots()
	{
		List<Integer> fallback = Arrays.asList(1, 2, 3, 4);
		LayoutRequest request = request(entriesWithLocks(fallback, positions(2, 3)));

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertEquals(Arrays.asList(1, 3, 4, 2), targetOrder(outcome));
		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertValid(request, outcome);
	}

	@Test
	public void nominalRectangleMustFitBeforeTailEvenWhenMeaningfulCellsWouldFit()
	{
		List<Integer> fallback = ids(1, 10);
		SemanticRule pair = rule("tail.pair", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("tail.pair.family", 1, 2));
		LayoutRequest pairRequest = request(entriesWithLocks(fallback, positions(1, 8)), pair);
		BoundedLayoutPacker.Outcome pairOutcome = plan(pairRequest, fallback);
		assertPositions(pairOutcome, positions(1, 8, 2, 9));
		assertEquals(1, pairOutcome.getTieKey().getBlocks().size());

		SemanticRule triple = rule("tail.triple", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), atom("tail.triple.family", 1, 2, 3));
		LayoutRequest tripleRequest = request(entriesWithLocks(fallback, positions(1, 8)), triple);
		BoundedLayoutPacker.Outcome tripleOutcome = plan(tripleRequest, fallback);
		assertTrue(tripleOutcome.getTieKey().getBlocks().isEmpty());
		assertEquals(2, tripleOutcome.getScore().getHighMissedRelations());
		assertValid(tripleRequest, tripleOutcome);
	}

	@Test
	public void candidateLessEligibleGroupStillChargesExactFallbackSemantics()
	{
		List<Integer> fallback = ids(1, 9);
		SemanticRule impossible = rule("impossible.run", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(1, 2, 3, 4, 5, 6, 7, 8),
			atom("impossible.family", 1, 2, 3, 4, 5, 6, 7, 8, 9));
		LayoutRequest request = request(entries(fallback), impossible);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(8, outcome.getScore().getHighMissedRelations());
		assertEquals(1000, outcome.getScore().getHighMissedCompleteness());
		assertEquals(fallback, targetOrder(outcome));
		assertValid(request, outcome);
	}

	@Test
	public void completeScoreMeasuresRealSlackSpilloverTransitionsAndSpan()
	{
		List<Integer> fallback = ids(1, 5);
		SemanticRule rule = rule("scored.run", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(5), atom("scored.family", 1, 2, 3));
		LayoutRequest request = request(entries(fallback), rule);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertEquals(2, outcome.getScore().getNominalFootprintSlack());
		assertEquals(8, outcome.getScore().getSpilloverCompatibilityCost());
		assertEquals(1, outcome.getScore().getSpilloverTransitions());
		assertEquals(3, outcome.getScore().getSemanticSpan());
		assertEquals(0, outcome.getScore().getSemanticRowBreaks());
		assertValid(request, outcome);
	}

	@Test
	public void spilloverCompatibilityIsDirectionalThenFallsBackToTypedSubcategory()
	{
		List<Integer> fallback = Arrays.asList(1, 2, 3);
		List<LayoutEntry> typedEntries = Arrays.asList(typedEntry(1, 0, "ore"),
			typedEntry(2, 1, "ore"), typedEntry(3, 2, "ore"));
		SemanticRule foreignSingleton = rule("b.foreign", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.LOW, widths(2), atom("foreign.singleton", 3));
		SemanticRule typedOnly = rule("a.block", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), Collections.emptySet(), atom("block.family", 1, 2));
		BoundedLayoutPacker.Outcome typed = plan(
			request(typedEntries, typedOnly, foreignSingleton), fallback);
		assertEquals(1, typed.getScore().getSpilloverCompatibilityCost());

		SemanticRule explicit = rule("a.block", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), Collections.singleton("b.foreign"), atom("block.family", 1, 2));
		BoundedLayoutPacker.Outcome compatible = plan(
			request(typedEntries, explicit, foreignSingleton), fallback);
		assertEquals(0, compatible.getScore().getSpilloverCompatibilityCost());
	}

	@Test
	public void movementIsCalculatedOnlyFromAProvenCompleteCurrentOrder()
	{
		List<Integer> fallback = Arrays.asList(2, 1, 4, 3);
		List<LayoutEntry> entries = entries(ids(1, 4));
		LayoutRequest proven = new LayoutRequest(entries, Collections.emptyList(), ids(1, 4));
		BoundedLayoutPacker.Outcome moved = plan(proven, fallback);
		assertEquals(2, moved.getScore().getUnrestrictedSwapLowerBound());
		assertEquals(4, moved.getScore().getTotalRankDisplacement());

		BoundedLayoutPacker.Outcome unproven = plan(
			new LayoutRequest(entries, Collections.emptyList()), fallback);
		assertEquals(0, unproven.getScore().getUnrestrictedSwapLowerBound());
		assertEquals(0, unproven.getScore().getTotalRankDisplacement());

		BoundedLayoutPacker.Outcome threeCycleAndFixedPoint = plan(
			new LayoutRequest(entries, Collections.emptyList(), ids(1, 4)),
			Arrays.asList(2, 3, 1, 4));
		assertEquals(2, threeCycleAndFixedPoint.getScore().getUnrestrictedSwapLowerBound());
		assertEquals(4, threeCycleAndFixedPoint.getScore().getTotalRankDisplacement());
	}

	@Test
	public void reverseConstructionOrderProducesTheSamePlanScoreTieKeyAndStats()
	{
		SemanticRule three = rule("a.three", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), atom("three.family", 1, 2, 3));
		SemanticRule five = rule("b.five", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(5), atom("five.family", 4, 5, 6, 7, 8));
		List<Integer> fallback = Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1);
		List<LayoutEntry> forwardEntries = entries(ids(1, 8));
		List<LayoutEntry> reversedEntries = new ArrayList<>(forwardEntries);
		Collections.reverse(reversedEntries);

		BoundedLayoutPacker.Outcome forward = plan(
			request(forwardEntries, five, three), fallback);
		BoundedLayoutPacker.Outcome reversed = plan(
			request(reversedEntries, three, five), fallback);

		assertEquals(targetOrder(forward), targetOrder(reversed));
		assertEquals(forward.getScore(), reversed.getScore());
		assertEquals(forward.getTieKey(), reversed.getTieKey());
		assertEquals(forward.getStats().getCandidateOriginEvaluations(),
			reversed.getStats().getCandidateOriginEvaluations());
		assertEquals(forward.getStats().getMaximumBeamSize(),
			reversed.getStats().getMaximumBeamSize());
	}

	@Test
	public void planningIsRepeatableAndIdempotentFromItsOwnProvenDenseOrder()
	{
		SemanticRule three = rule("a.three", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), atom("three.family", 1, 2, 3));
		SemanticRule five = rule("b.five", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(5), atom("five.family", 4, 5, 6, 7, 8));
		List<Integer> fallback = Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1);
		List<LayoutEntry> originalEntries = entries(ids(1, 8));
		LayoutRequest request = request(originalEntries, five, three);

		BoundedLayoutPacker.Outcome first = plan(request, fallback);
		BoundedLayoutPacker.Outcome repeated = plan(request, fallback);
		List<Integer> plannedOrder = targetOrder(first);
		LayoutRequest proven = new LayoutRequest(originalEntries, Arrays.asList(five, three),
			plannedOrder);
		BoundedLayoutPacker.Outcome replanned = plan(proven, fallback);

		assertEquals(plannedOrder, targetOrder(repeated));
		assertEquals(first.getScore(), repeated.getScore());
		assertEquals(first.getTieKey(), repeated.getTieKey());
		assertEquals(plannedOrder, targetOrder(replanned));
		assertEquals(first.getTieKey(), replanned.getTieKey());
		assertEquals(0, replanned.getScore().getUnrestrictedSwapLowerBound());
		assertEquals(0, replanned.getScore().getTotalRankDisplacement());
		assertValid(proven, replanned);
	}

	@Test
	public void injectedExpansionCapClosesEveryRemainingGroupWithDeterministicFallback()
	{
		SemanticRule rule = rule("cap.run", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(3), atom("cap.family", 1, 2, 3));
		List<Integer> fallback = ids(1, 8);
		LayoutRequest request = request(entries(fallback), rule);

		BoundedLayoutPacker.Outcome zero = plan(request, fallback,
			new BoundedLayoutPacker.Limits(4, 0));
		assertTrue(zero.getStats().isCapReached());
		assertEquals(0, zero.getStats().getCandidateOriginEvaluations());
		assertTrue(zero.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, targetOrder(zero));

		BoundedLayoutPacker.Outcome one = plan(request, fallback,
			new BoundedLayoutPacker.Limits(4, 1));
		assertTrue(one.getStats().isCapReached());
		assertEquals(1, one.getStats().getCandidateOriginEvaluations());
		assertTrue(one.getStats().getMaximumBeamSize() <= 4);
		assertValid(request, one);
	}

	@Test
	public void capZeroClosesEveryLaterGroupAndChargesEveryConfidenceTier()
	{
		SemanticRule high = rule("high.pair", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("high.atom", 1, 2));
		SemanticRule medium = rule("medium.triple", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.MEDIUM, widths(3), atom("medium.atom", 3, 4, 5));
		SemanticRule low = rule("low.incomplete", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.LOW,
			widths(2), atom("low.atom", 6, 7, 8));
		List<Integer> fallback = ids(1, 7);
		LayoutRequest request = request(entries(fallback), low, medium, high);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback,
			new BoundedLayoutPacker.Limits(4, 0));

		assertTrue(outcome.getStats().isCapReached());
		assertEquals(0, outcome.getStats().getCandidateOriginEvaluations());
		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(1, outcome.getScore().getHighMissedRelations());
		assertEquals(1000, outcome.getScore().getHighMissedCompleteness());
		assertEquals(2, outcome.getScore().getMediumMissedRelations());
		assertEquals(1000, outcome.getScore().getMediumMissedCompleteness());
		assertEquals(1, outcome.getScore().getLowMissedRelations());
		assertEquals(666, outcome.getScore().getLowMissedCompleteness());
		assertEquals(fallback, targetOrder(outcome));
		assertValid(request, outcome);
	}

	@Test
	public void rejectedForeignLockAttemptStillConsumesExactlyOneCapEvaluation()
	{
		SemanticRule pair = rule("pair", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("pair.atom", 1, 2));
		List<Integer> fallback = ids(1, 3);
		LayoutRequest request = request(entriesWithLocks(fallback, positions(3, 0)), pair);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback,
			new BoundedLayoutPacker.Limits(4, 1));

		assertTrue(outcome.getStats().isCapReached());
		assertEquals(1, outcome.getStats().getCandidateOriginEvaluations());
		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertValid(request, outcome);

		SemanticRule tail = rule("tail", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(4), atom("tail.atom", 1, 2));
		BoundedLayoutPacker.Outcome rejectedTail = plan(request(entries(fallback), tail), fallback,
			new BoundedLayoutPacker.Limits(4, 1));
		assertTrue(rejectedTail.getStats().isCapReached());
		assertEquals(1, rejectedTail.getStats().getCandidateOriginEvaluations());
		assertTrue(rejectedTail.getTieKey().getBlocks().isEmpty());
	}

	@Test
	public void capTruncationUsesEveryCanonicalGroupOrderingLayer()
	{
		List<Integer> four = ids(1, 4);

		SemanticRule lockedLow = rule("z.locked", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.LOW, widths(2), atom("locked.atom", 3, 4));
		SemanticRule unlockedHigh = rule("a.high", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("high.atom", 1, 2));
		assertFirstBlockRule(request(entriesWithLocks(four, positions(3, 0)),
			unlockedHigh, lockedLow), four, "z.locked");

		SemanticRule twoLocks = rule("z.two-locks", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.LOW, widths(2), atom("two-locks.atom", 1, 2));
		SemanticRule oneLock = rule("a.one-lock", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("one-lock.atom", 3, 4));
		assertFirstBlockRule(request(entriesWithLocks(four, positions(1, 0, 2, 1, 3, 2)),
			oneLock, twoLocks), four, "z.two-locks");

		SemanticRule highZ = rule("z.high", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("high.z.atom", 1, 2));
		SemanticRule lowA = rule("a.low", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.LOW, widths(2), atom("low.a.atom", 3, 4));
		assertFirstBlockRule(request(entries(four), lowA, highZ), four, "z.high");

		SemanticRule mediumZ = rule("z.medium", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.MEDIUM, widths(2), atom("medium.z.atom", 1, 2));
		SemanticRule lowAgain = rule("a.low-again", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.LOW, widths(2), atom("low.again.atom", 3, 4));
		assertFirstBlockRule(request(entries(four), lowAgain, mediumZ), four, "z.medium");

		SemanticRule completeZ = rule("z.complete", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("complete.atom", 1, 2));
		SemanticRule incompleteA = rule("a.incomplete", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("incomplete.atom", 3, 4, 5));
		assertFirstBlockRule(request(entries(four), incompleteA, completeZ), four, "z.complete");

		SemanticRule ruleA = rule("a.rule", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("rule.a.atom", 1, 2));
		SemanticRule ruleZ = rule("z.rule", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("rule.z.atom", 3, 4));
		assertFirstBlockRule(request(entries(four), ruleZ, ruleA), four, "a.rule");

		SemanticRule atomOrder = rule("same.rule", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("z.atom", 1, 2), atom("a.atom", 3, 4));
		BoundedLayoutPacker.Outcome atomOutcome = plan(request(entries(four), atomOrder), four,
			new BoundedLayoutPacker.Limits(4, 1));
		assertEquals(Collections.singletonList("a.atom"),
			atomOutcome.getTieKey().getBlocks().get(0).getAtomKeys());
		assertValid(request(entries(four), atomOrder), atomOutcome);
	}

	@Test
	public void reverseConstructionOrderRemainsExactWhenCapTruncatesInSecondGroup()
	{
		SemanticRule a = rule("a.rule", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("a.atom", 1, 2));
		SemanticRule b = rule("b.rule", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("b.atom", 3, 4));
		SemanticRule c = rule("c.rule", ShapePrimitive.HORIZONTAL_RUN, ConfidenceTier.HIGH,
			widths(2), atom("c.atom", 5, 6));
		List<Integer> fallback = Arrays.asList(6, 5, 4, 3, 2, 1);
		List<LayoutEntry> forwardEntries = entries(ids(1, 6));
		List<LayoutEntry> reverseEntries = new ArrayList<>(forwardEntries);
		Collections.reverse(reverseEntries);
		BoundedLayoutPacker.Limits limits = new BoundedLayoutPacker.Limits(2, 8);

		BoundedLayoutPacker.Outcome forward = plan(request(forwardEntries, c, b, a), fallback, limits);
		BoundedLayoutPacker.Outcome reverse = plan(request(reverseEntries, a, b, c), fallback, limits);

		assertTrue(forward.getStats().isCapReached());
		assertTrue(reverse.getStats().isCapReached());
		assertEquals(8, forward.getStats().getCandidateOriginEvaluations());
		assertEquals(8, reverse.getStats().getCandidateOriginEvaluations());
		assertTrue(forward.getStats().getMaximumBeamSize() <= 2);
		assertEquals(1, forward.getTieKey().getBlocks().size());
		assertEquals("a.rule", forward.getTieKey().getBlocks().get(0).getRuleKey());
		assertEquals(2, forward.getScore().getHighMissedRelations());
		assertEquals(2000, forward.getScore().getHighMissedCompleteness());
		assertEquals(targetOrder(forward), targetOrder(reverse));
		assertEquals(forward.getScore(), reverse.getScore());
		assertEquals(forward.getTieKey(), reverse.getTieKey());
		assertEquals(forward.getStats().getMaximumBeamSize(), reverse.getStats().getMaximumBeamSize());
	}

	@Test
	public void largestTrackedFixtureStaysWithinBeamAndExpansionBounds()
	{
		List<Integer> fallback = ids(1000, 1335);
		SemanticRule horizontal = rule("large.horizontal", ShapePrimitive.HORIZONTAL_RUN,
			ConfidenceTier.HIGH, widths(2), atom("large.horizontal.atom", 1000, 1001));
		SemanticRule vertical = rule("large.vertical", ShapePrimitive.VERTICAL_RUN,
			ConfidenceTier.HIGH, widths(1), atom("large.vertical.atom", 1002, 1003));
		SemanticRule stage = rule("large.stage", ShapePrimitive.STAGE_MATRIX,
			ConfidenceTier.HIGH, widths(2), atom("large.stage.a", 1004, 1005),
			atom("large.stage.b", 1006, 1007));
		SemanticRule rows = rule("large.rows", ShapePrimitive.ROW_GROUP_MATRIX,
			ConfidenceTier.HIGH, widths(2), atom("large.rows.a", 1008, 1009),
			atom("large.rows.b", 1010, 1011));
		LayoutRequest request = request(entries(fallback), horizontal, vertical, stage, rows);

		BoundedLayoutPacker.Outcome outcome = plan(request, fallback);

		assertEquals(128, BoundedLayoutPacker.PRODUCTION_BEAM_WIDTH);
		assertEquals(1_000_000, BoundedLayoutPacker.PRODUCTION_CANDIDATE_ORIGIN_CAP);
		assertEquals(336, outcome.getResult().getPlacements().size());
		assertEquals(128, outcome.getStats().getMaximumBeamSize());
		assertTrue(outcome.getStats().getCandidateOriginEvaluations()
			<= BoundedLayoutPacker.PRODUCTION_CANDIDATE_ORIGIN_CAP);
		assertTrue(outcome.getStats().getCandidateOriginEvaluations() > 294);
		assertFalse(outcome.getStats().isCapReached());
		assertTrue(outcome.getStats().getMaterializedPlacementStates() > 128);
		assertTrue(outcome.getStats().getMaterializedPlacementStates()
			<= 4 * BoundedLayoutPacker.PRODUCTION_BEAM_WIDTH);
		assertValid(request, outcome);
	}

	private static void assertFirstBlockRule(LayoutRequest request, List<Integer> fallback,
		String expectedRuleKey)
	{
		BoundedLayoutPacker.Outcome outcome = plan(request, fallback,
			new BoundedLayoutPacker.Limits(4, 1));
		assertTrue(outcome.getStats().isCapReached());
		assertEquals(1, outcome.getStats().getCandidateOriginEvaluations());
		assertEquals(1, outcome.getTieKey().getBlocks().size());
		assertEquals(expectedRuleKey, outcome.getTieKey().getBlocks().get(0).getRuleKey());
		assertValid(request, outcome);
	}

	private static BoundedLayoutPacker.Outcome plan(LayoutRequest request, List<Integer> fallback)
	{
		return plan(request, fallback, BoundedLayoutPacker.Limits.production());
	}

	private static BoundedLayoutPacker.Outcome plan(LayoutRequest request, List<Integer> fallback,
		BoundedLayoutPacker.Limits limits)
	{
		return SemanticBlockLayoutEngine.planDetailed(request, fallback, limits);
	}

	private static LayoutRequest request(List<LayoutEntry> entries, SemanticRule... rules)
	{
		return new LayoutRequest(entries, Arrays.asList(rules));
	}

	private static List<LayoutEntry> entries(List<Integer> itemIds)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			int itemId = itemIds.get(index);
			entries.add(LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), index));
		}
		return entries;
	}

	private static List<LayoutEntry> entriesWithLocks(List<Integer> itemIds,
		Map<Integer, Integer> locks)
	{
		List<LayoutEntry> entries = entries(itemIds);
		List<LayoutEntry> result = new ArrayList<>();
		for (LayoutEntry entry : entries)
		{
			Integer target = locks.get(entry.getItem().getItemId());
			result.add(target == null ? entry : entry.withLockedTarget(target));
		}
		return result;
	}

	private static LayoutEntry typedEntry(int itemId, int sourceSlot, String subcategory)
	{
		CatalogItem catalogItem = new CatalogItem(itemId, "Item " + itemId, ItemCategory.SKILLING,
			subcategory, Collections.emptySet(), null);
		return LayoutEntry.of(new BankPreviewItem(catalogItem, 1), sourceSlot);
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive,
		ConfidenceTier confidenceTier, Set<Integer> widths, SemanticAtom... atoms)
	{
		return rule(ruleKey, primitive, confidenceTier, widths, Collections.emptySet(), atoms);
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive,
		ConfidenceTier confidenceTier, Set<Integer> widths, Set<String> compatibleRuleKeys,
		SemanticAtom... atoms)
	{
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(Arrays.asList(atoms))
			.confidenceTier(confidenceTier)
			.shapePrimitive(primitive)
			.allowedWidths(widths)
			.spilloverCompatibleRuleKeys(compatibleRuleKeys)
			.build();
	}

	private static SemanticAtom atom(String atomKey, int... itemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			members.add(new SemanticAtom.Member("member." + index, itemIds[index]));
		}
		return new SemanticAtom(atomKey, members);
	}

	private static Set<Integer> widths(Integer... values)
	{
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	private static List<Integer> ids(int first, int last)
	{
		List<Integer> ids = new ArrayList<>();
		for (int id = first; id <= last; id++)
		{
			ids.add(id);
		}
		return ids;
	}

	private static Map<Integer, Integer> positions(Integer... itemAndTargetPairs)
	{
		Map<Integer, Integer> result = new HashMap<>();
		for (int index = 0; index < itemAndTargetPairs.length; index += 2)
		{
			result.put(itemAndTargetPairs[index], itemAndTargetPairs[index + 1]);
		}
		return result;
	}

	private static void assertPositions(BoundedLayoutPacker.Outcome outcome,
		Map<Integer, Integer> expected)
	{
		Map<Integer, Integer> actual = new HashMap<>();
		for (LayoutPlacement placement : outcome.getResult().getPlacements())
		{
			actual.put(placement.getItem().getItemId(), placement.getTargetIndex());
		}
		for (Map.Entry<Integer, Integer> position : expected.entrySet())
		{
			assertEquals("item " + position.getKey(), position.getValue(), actual.get(position.getKey()));
		}
	}

	private static Map<Integer, Integer> placementPositions(BoundedLayoutPacker.Outcome outcome)
	{
		Map<Integer, Integer> positions = new HashMap<>();
		for (LayoutPlacement placement : outcome.getResult().getPlacements())
		{
			positions.put(placement.getItem().getItemId(), placement.getTargetIndex());
		}
		return positions;
	}

	private static void assertSingleBlock(BoundedLayoutPacker.Outcome outcome,
		ShapePrimitive primitive, int width, int row, int column)
	{
		assertEquals(1, outcome.getTieKey().getBlocks().size());
		PlacedBlock block = outcome.getTieKey().getBlocks().get(0);
		assertEquals(primitive, block.getShapePrimitive());
		assertEquals(width, block.getWidth());
		assertEquals(row, block.getStartRow());
		assertEquals(column, block.getStartColumn());
	}

	private static List<Integer> targetOrder(BoundedLayoutPacker.Outcome outcome)
	{
		assertTrue(outcome.getResult().getConflicts().toString(), outcome.getResult().isSuccess());
		List<Integer> ids = new ArrayList<>();
		for (LayoutPlacement placement : outcome.getResult().getPlacements())
		{
			ids.add(placement.getItem().getItemId());
		}
		return ids;
	}

	private static void assertValid(LayoutRequest request, BoundedLayoutPacker.Outcome outcome)
	{
		assertTrue(outcome.getResult().getConflicts().toString(), outcome.getResult().isSuccess());
		assertTrue(LayoutPlanValidator.validate(request,
			outcome.getResult().getPlacements()).getConflicts().toString(),
			LayoutPlanValidator.validate(request, outcome.getResult().getPlacements()).isSuccess());
		assertEquals(request.size(), outcome.getResult().getPlacements().size());
		for (int target = 0; target < request.size(); target++)
		{
			assertEquals(target, outcome.getResult().getPlacements().get(target).getTargetIndex());
			assertFalse(outcome.getResult().getPlacements().get(target).getItem().isBlank());
		}
	}
}

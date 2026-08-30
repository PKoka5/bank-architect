package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Phase;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Session;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TabRouteAdvisorTest
{
	@Test
	public void mainIsBlueprintTabOneAndFirstPhysicalAnchorUsesFirstPlannedItem()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{9, 2, 3, 1, 4},
			plan(items(9), items(1, 2), items(3, 4)), counts());

		assertMove(result, MoveType.DRAG_TO_NEW_TAB, 1, 3, -1, 1, 2);
		assertEquals(Phase.CREATING, result.getProgress().getPhase());
		assertEquals("Combat Gear",
			result.getMove().get().getCategoryName());
	}

	@Test
	public void distributionSelectsTheItemExpectedAtTheConfirmedAppendSlot()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{1, 3, 9, 2},
			plan(items(9), items(1, 2, 3), Collections.emptyList()), counts(1));

		assertMove(result, MoveType.DISTRIBUTE_TO_TAB, 2, 3, -1, 1, 2);
	}

	@Test
	public void partialTabAppendClosesAnOpenCycleInsteadOfCreatingAnExtraSwap()
	{
		// Target A,B,C,D; existing prefix C,B. C already occupies the next target's
		// item, so appending A closes A<->C. Appending D first would leave a 3-cycle.
		Assessment result = TabRouteAdvisor.assess(new int[]{3, 2, 9, 4, 1},
			plan(items(9), items(1, 2, 3, 4), Collections.emptyList()), counts(2));

		assertMove(result, MoveType.DISTRIBUTE_TO_TAB, 1, 4, -1, 1, 2);
	}

	@Test
	public void sortingAnchorsTheFirstMismatchedSlotAndReportsExactEstimateForUniqueIds()
	{
		// Slot 0 holds item 2, so the advised drag starts at the anchor slot 0
		// and delivers item 2 to its final slot 1.
		Assessment result = TabRouteAdvisor.assess(new int[]{2, 3, 1, 5, 4},
			plan(items(1, 2, 3, 4, 5), Collections.emptyList(), Collections.emptyList()),
			counts());

		assertMove(result, MoveType.SWAP_SECTION, 2, 0, 1, 0, 1);
		assertEquals(3, result.getProgress().getRemainingDragsEstimate());
		assertEquals(3, TabRouteAdvisor.estimatedRemainingSwaps(
			new int[]{2, 3, 1, 5, 4}, items(1, 2, 3, 4, 5)));
	}

	@Test
	public void insertModeAdvisesADropAnchoredOnTheSlotOccupant()
	{
		// Item 1 sits behind 2 and 3; dropping it on slot 0 pushes both right.
		int[] bank = {2, 3, 1, 5, 4};
		Assessment result = TabRouteAdvisor.assess(bank,
			plan(items(1, 2, 3, 4, 5), Collections.emptyList(), Collections.emptyList()),
			counts(), 0, RearrangeMode.INSERT);

		assertMove(result, MoveType.INSERT_SECTION, 1, 2, 0, 0, 1);
		assertEquals(2, result.getMove().get().getAnchorItemId());
		assertEquals(2, result.getProgress().getRemainingDragsEstimate());
		assertEquals(RearrangeMode.INSERT, result.getProgress().getMode());
	}

	@Test
	public void sortingProgressQuotesBothModeBoundsSoGuidanceCanRecommendInsert()
	{
		int[] bank = {2, 3, 1, 5, 4};
		BankTabPlan plan = plan(items(1, 2, 3, 4, 5), Collections.emptyList(),
			Collections.emptyList());

		Assessment swap = TabRouteAdvisor.assess(bank, plan, counts(), 0, RearrangeMode.SWAP);
		assertEquals(3, swap.getProgress().getRemainingDragsEstimate());
		assertEquals(2, swap.getProgress().getOtherModeDragsEstimate());

		Assessment insert = TabRouteAdvisor.assess(bank, plan, counts(), 0, RearrangeMode.INSERT);
		assertEquals(2, insert.getProgress().getRemainingDragsEstimate());
		assertEquals(3, insert.getProgress().getOtherModeDragsEstimate());
	}

	@Test
	public void insertGuidanceOnlyAdvancesAfterTheAdvisedDropIsPerformed()
	{
		BankTabPlan plan = plan(items(1, 2, 3, 4), Collections.emptyList(),
			Collections.emptyList());
		Session session = new Session();
		Assessment first = session.assess(new int[]{3, 1, 2, 4}, plan, counts(), 1, 0,
			RearrangeMode.INSERT);
		assertMove(first, MoveType.INSERT_SECTION, 3, 0, 2, 0, 1);

		// A swap instead of the advised drop is not the transition we asked for.
		assertEquals(Status.WAITING_FOR_BANK, session.assess(new int[]{1, 3, 2, 4}, plan,
			counts(), 2, 0, RearrangeMode.INSERT).getStatus());

		// Performing the advised drop finishes the section.
		assertEquals(Status.COMPLETE, session.assess(new int[]{1, 2, 3, 4}, plan, counts(), 3, 0,
			RearrangeMode.INSERT).getStatus());
	}

	@Test
	public void switchingRearrangeModeDropsThePinnedAdviceInsteadOfMisreadingTheNextDrag()
	{
		BankTabPlan plan = plan(items(1, 2, 3, 4), Collections.emptyList(),
			Collections.emptyList());
		Session session = new Session();
		assertMove(session.assess(new int[]{3, 1, 2, 4}, plan, counts(), 1, 0,
			RearrangeMode.INSERT), MoveType.INSERT_SECTION, 3, 0, 2, 0, 1);

		Assessment afterSwitch = session.assess(new int[]{3, 1, 2, 4}, plan, counts(), 2, 0,
			RearrangeMode.SWAP);
		assertEquals(MoveType.SWAP_SECTION, afterSwitch.getMove().get().getType());
	}

	@Test
	public void insertRouteTerminatesForEveryStartingOrder()
	{
		BankTabPlan plan = plan(items(9, 8), items(1, 2), items(3, 4));
		for (List<Integer> permutation : permutations(Arrays.asList(1, 2, 3, 4, 8, 9)))
		{
			assertTerminates(plan, model(permutation, counts()), RearrangeMode.INSERT);
		}
	}

	@Test
	public void unorderedCleanBucketIsReusedWhenCreatingTheNextTab()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{2, 1, 9, 4, 3},
			plan(items(9), items(1, 2), items(3, 4)), counts(2));

		assertMove(result, MoveType.DRAG_TO_NEW_TAB, 3, 4, -1, 2, 3);
	}

	@Test
	public void singletonWrongBucketIsCollapsedInsteadOfMovedToNewTab()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{2, 9, 1},
			plan(items(9), items(1), items(2)), counts(1));

		assertMove(result, MoveType.COLLAPSE_TAB, -1, -1, -1, 1, 2);
		assertEquals(Phase.REPAIRING, result.getProgress().getPhase());
	}

	@Test
	public void dirtyLaterBucketCollapsesHighestFirst()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{1, 9, 2, 3},
			plan(items(9), items(1, 2), items(3)), counts(1, 1));

		assertMove(result, MoveType.COLLAPSE_TAB, -1, -1, -1, 2, 3);
	}

	@Test
	public void foreignItemIsRecoveredToItsCorrectExistingTabWithoutCollapse()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{1, 3, 4, 9, 2},
			plan(items(9), items(1, 2), items(3, 4)), counts(2, 1));

		assertMove(result, MoveType.TRANSFER_TO_TAB, 3, 1, -1, 2, 3);
		assertEquals(1, result.getMove().get().getSourceTab());
		assertEquals(Phase.RECOVERING, result.getProgress().getPhase());
	}

	@Test
	public void mainItemStrandedInANumberedTabIsReturnedWithoutCollapse()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{1, 9, 3, 2},
			plan(items(9), items(1, 2), items(3)), counts(2, 1));

		assertMove(result, MoveType.RETURN_TO_MAIN, 9, 1, -1, 0, 1);
		assertEquals(1, result.getMove().get().getSourceTab());
	}

	@Test
	public void distributionBuildsAppendPositionsInFinalOrder()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3, 4));
		Assessment first = TabRouteAdvisor.assess(
			new int[]{1, 3, 9, 4, 2}, plan, counts(1, 1));

		assertMove(first, MoveType.DISTRIBUTE_TO_TAB, 2, 4, -1, 1, 2);

		Assessment second = TabRouteAdvisor.assess(
			new int[]{1, 2, 3, 9, 4}, plan, counts(2, 1));
		assertMove(second, MoveType.DISTRIBUTE_TO_TAB, 4, 4, -1, 2, 3);
		assertEquals(Phase.DISTRIBUTING, second.getProgress().getPhase());
	}

	@Test
	public void sortingStartsWithMainThenMovesThroughPhysicalTabs()
	{
		BankTabPlan plan = plan(items(9, 8), items(1, 2), items(3));
		Assessment mainSwap = TabRouteAdvisor.assess(
			new int[]{2, 1, 3, 8, 9}, plan, counts(2, 1));

		assertMove(mainSwap, MoveType.SWAP_SECTION, 8, 3, 4, 0, 1);

		Assessment tabSwap = TabRouteAdvisor.assess(
			new int[]{2, 1, 3, 9, 8}, plan, counts(2, 1));
		assertMove(tabSwap, MoveType.SWAP_SECTION, 2, 0, 1, 1, 2);
		assertEquals(Phase.SORTING, tabSwap.getProgress().getPhase());
	}

	@Test
	public void focusTabServesItsInteriorSwapBeforeMain()
	{
		BankTabPlan plan = plan(items(9, 8), items(1, 2), items(3));
		int[] bank = new int[]{2, 1, 3, 8, 9};

		// Viewing tab 1: its interior swap is served before the main swap.
		Assessment focused = TabRouteAdvisor.assess(bank, plan, counts(2, 1), 1);
		assertMove(focused, MoveType.SWAP_SECTION, 2, 0, 1, 1, 2);

		// Default order still starts with main.
		Assessment unfocused = TabRouteAdvisor.assess(bank, plan, counts(2, 1), 0);
		assertMove(unfocused, MoveType.SWAP_SECTION, 8, 3, 4, 0, 1);

		// A focused tab that is already sorted falls back to the default order.
		Assessment cleanFocus = TabRouteAdvisor.assess(new int[]{1, 2, 3, 8, 9},
			plan, counts(2, 1), 2);
		assertMove(cleanFocus, MoveType.SWAP_SECTION, 8, 3, 4, 0, 1);
	}

	@Test
	public void exactCountsMembershipAndOrderAreComplete()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{1, 2, 3, 9, 8},
			plan(items(9, 8), items(1, 2), items(3)), counts(2, 1));

		assertEquals(Status.COMPLETE, result.getStatus());
		assertFalse(result.getMove().isPresent());
		assertEquals(Phase.COMPLETE, result.getProgress().getPhase());
		assertEquals(100, result.getProgress().getPercent());
	}

	@Test
	public void equalChargedCopiesAreValidInThePhysicalTabPlan()
	{
		int usedEclipseMoonChestplate = 29031;
		Assessment result = TabRouteAdvisor.assess(
			new int[]{usedEclipseMoonChestplate, usedEclipseMoonChestplate, 9},
			plan(items(9), items(usedEclipseMoonChestplate, usedEclipseMoonChestplate),
				Collections.emptyList()),
			counts(2));

		assertEquals(Status.COMPLETE, result.getStatus());
	}

	@Test
	public void equalChargedCopiesCanBeCreatedDistributedAndSorted()
	{
		int usedEclipseMoonChestplate = 29031;
		BankTabPlan plan = plan(items(9),
			items(usedEclipseMoonChestplate, usedEclipseMoonChestplate, 20),
			Collections.emptyList());

		assertTerminates(plan,
			new ModelBank(new int[]{usedEclipseMoonChestplate, 20,
				usedEclipseMoonChestplate, 9}, counts()), RearrangeMode.SWAP);
		assertTerminates(plan,
			new ModelBank(new int[]{usedEclipseMoonChestplate, 20,
				usedEclipseMoonChestplate, 9}, counts()), RearrangeMode.INSERT);
	}

	@Test
	public void sessionAcceptsDistributionWhenEqualCopiesKeepTheSameFlatOrder()
	{
		int usedEclipseMoonChestplate = 29031;
		BankTabPlan plan = plan(items(9),
			items(usedEclipseMoonChestplate, usedEclipseMoonChestplate),
			Collections.emptyList());
		int[] unchangedFlatOrder = {usedEclipseMoonChestplate, usedEclipseMoonChestplate, 9};
		Session session = new Session();

		Assessment distribution = session.assess(unchangedFlatOrder, plan, counts(1), 100);
		assertMove(distribution, MoveType.DISTRIBUTE_TO_TAB, usedEclipseMoonChestplate,
			1, -1, 1, 2);

		Assessment complete = session.assess(unchangedFlatOrder, plan, counts(2), 100);
		assertEquals(Status.COMPLETE, complete.getStatus());
	}

	@Test
	public void allEmptyPhysicalCategoriesLeaveOnlyMainToSort()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{2, 1},
			plan(items(1, 2), Collections.emptyList(), Collections.emptyList()), counts());

		assertMove(result, MoveType.SWAP_SECTION, 2, 0, 1, 0, 1);
	}

	@Test
	public void malformedCountsAndUnstableItemsFailClosed()
	{
		BankTabPlan plan = plan(items(9), items(1), items(2));

		assertEquals(Status.UNSTABLE_BANK,
			TabRouteAdvisor.assess(new int[]{1, 2, 9}, plan, new int[]{1}).getStatus());
		assertEquals(Status.UNSTABLE_BANK,
			TabRouteAdvisor.assess(new int[]{1, 2, 9}, plan, counts(0, 1)).getStatus());
		Assessment duplicates = TabRouteAdvisor.assess(
			new int[]{1, 1, 9}, plan, counts());
		assertEquals(Status.DUPLICATE_ITEMS, duplicates.getStatus());
		assertEquals(List.of(1), duplicates.getDuplicateItemIds());
		assertEquals(Status.UNSTABLE_BANK,
			TabRouteAdvisor.assess(new int[]{1, 2, -1}, plan, counts()).getStatus());
	}

	@Test
	public void duplicateRecoveryReportsOnlyLiveOccurrencesBeyondThePlan()
	{
		Assessment result = TabRouteAdvisor.assess(new int[]{7, 7, 9},
			plan(items(9), items(4, 4), Collections.emptyList()), counts());

		assertEquals(Status.DUPLICATE_ITEMS, result.getStatus());
		assertEquals(List.of(7), result.getDuplicateItemIds());
	}

	@Test
	public void dirtyBankTerminatesWithBottomMainCollapseAndFrontTabDrops()
	{
		BankTabPlan plan = plan(items(9, 8), items(1, 2), items(3, 4));
		ModelBank bank = new ModelBank(new int[]{3, 9, 2, 4, 1, 8}, counts(1));

		assertTerminates(plan, bank);
		assertEquals(Arrays.asList(1, 2, 3, 4, 9, 8), bank.items);
		assertEquals(2, bank.counts[0]);
		assertEquals(2, bank.counts[1]);
	}

	@Test
	public void everySmallPermutationAndDenseTabPartitionTerminatesSafely()
	{
		BankTabPlan plan = plan(items(4), items(1, 2), items(3));
		int statesChecked = 0;
		for (List<Integer> permutation : permutations(Arrays.asList(1, 2, 3, 4)))
		{
			for (int firstCount = 0; firstCount <= 4; firstCount++)
			{
				if (firstCount == 0)
				{
					assertTerminates(plan, model(permutation, counts()));
					statesChecked++;
					continue;
				}
				for (int secondCount = 0; secondCount <= 4 - firstCount; secondCount++)
				{
					if (secondCount == 0)
					{
						assertTerminates(plan, model(permutation, counts(firstCount)));
						statesChecked++;
						continue;
					}
					for (int thirdCount = 0;
						thirdCount <= 4 - firstCount - secondCount; thirdCount++)
					{
						int[] tabCounts = thirdCount == 0
							? counts(firstCount, secondCount)
							: counts(firstCount, secondCount, thirdCount);
						assertTerminates(plan, model(permutation, tabCounts));
						statesChecked++;
					}
				}
			}
		}
		assertEquals(360, statesChecked);
	}

	@Test
	public void everyFourItemMainPermutationUsesTheExactMinimumNumberOfSwaps()
	{
		BankTabPlan plan = plan(items(1, 2, 3, 4),
			Collections.emptyList(), Collections.emptyList());
		for (List<Integer> permutation : permutations(Arrays.asList(1, 2, 3, 4)))
		{
			int[] actual = permutation.stream().mapToInt(Integer::intValue).toArray();
			int minimum = TabRouteAdvisor.estimatedRemainingSwaps(actual, items(1, 2, 3, 4));
			int swaps = 0;
			while (true)
			{
				Assessment assessment = TabRouteAdvisor.assess(actual, plan, counts());
				if (assessment.getStatus() == Status.COMPLETE)
				{
					break;
				}
				assertEquals(MoveType.SWAP_SECTION, assessment.getMove().get().getType());
				Move move = assessment.getMove().get();
				int temporary = actual[move.getFromSlot()];
				actual[move.getFromSlot()] = actual[move.getToSlot()];
				actual[move.getToSlot()] = temporary;
				swaps++;
			}
			assertEquals("non-minimal route for " + permutation, minimum, swaps);
		}
	}

	@Test
	public void sortingKeepsThePickupSlotAnchoredUntilItsCycleCloses()
	{
		// Two interleaved 3-cycles: slots {0,2,4} and {1,3,5}. The pickup slot
		// must stay on one cycle's anchor until that cycle closes instead of
		// alternating between cycles.
		BankTabPlan plan = plan(items(1, 2, 3, 4, 5, 6),
			Collections.emptyList(), Collections.emptyList());
		int[] actual = new int[]{3, 4, 5, 6, 1, 2};

		List<Integer> fromSlots = new ArrayList<>();
		while (true)
		{
			Assessment assessment = TabRouteAdvisor.assess(actual, plan, counts());
			if (assessment.getStatus() == Status.COMPLETE)
			{
				break;
			}
			Move move = assessment.getMove().get();
			assertEquals(MoveType.SWAP_SECTION, move.getType());
			assertEquals(firstMismatchedSlot(actual), move.getFromSlot());
			fromSlots.add(move.getFromSlot());
			int temporary = actual[move.getFromSlot()];
			actual[move.getFromSlot()] = actual[move.getToSlot()];
			actual[move.getToSlot()] = temporary;
		}
		assertEquals(Arrays.asList(0, 0, 1, 1), fromSlots);
	}

	private static int firstMismatchedSlot(int[] actualItemIds)
	{
		for (int slot = 0; slot < actualItemIds.length; slot++)
		{
			if (actualItemIds[slot] != slot + 1)
			{
				return slot;
			}
		}
		throw new AssertionError("bank already sorted");
	}

	@Test
	public void cycleClosingAppendIsOptimalForEveryFiveItemPartialPermutation()
	{
		List<BankPreviewItem> target = items(1, 2, 3, 4, 5);
		List<Integer> targetIds = Arrays.asList(1, 2, 3, 4, 5);
		for (List<Integer> permutation : permutations(targetIds))
		{
			for (int prefixSize = 1; prefixSize < targetIds.size(); prefixSize++)
			{
				List<Integer> prefix = new ArrayList<>(permutation.subList(0, prefixSize));
				List<Integer> remaining = new ArrayList<>(targetIds);
				remaining.removeAll(prefix);

				List<Integer> greedy = new ArrayList<>(prefix);
				while (greedy.size() < targetIds.size())
				{
					int[] partial = greedy.stream().mapToInt(Integer::intValue).toArray();
					int selected = TabRouteAdvisor.cycleClosingAppendItemId(
						partial, 0, greedy.size(), target);
					assertTrue("selected item was not remaining", remaining.remove((Integer) selected));
					greedy.add(selected);
				}
				int greedySwaps = TabRouteAdvisor.estimatedRemainingSwaps(
					greedy.stream().mapToInt(Integer::intValue).toArray(), target);

				int optimum = Integer.MAX_VALUE;
				List<Integer> originalRemaining = new ArrayList<>(targetIds);
				originalRemaining.removeAll(prefix);
				for (List<Integer> suffix : permutations(originalRemaining))
				{
					List<Integer> candidate = new ArrayList<>(prefix);
					candidate.addAll(suffix);
					optimum = Math.min(optimum, TabRouteAdvisor.estimatedRemainingSwaps(
						candidate.stream().mapToInt(Integer::intValue).toArray(), target));
				}
				assertEquals("non-optimal append for prefix " + prefix, optimum, greedySwaps);
			}
		}
	}

	@Test
	public void sessionAcceptsDistributionAtAnArbitraryTargetPosition()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		Assessment distribute = session.assess(
			new int[]{1, 3, 9, 2}, plan, counts(1, 1));

		assertEquals(MoveType.DISTRIBUTE_TO_TAB, distribute.getMove().get().getType());
		Assessment after = session.assess(new int[]{2, 1, 3, 9}, plan, counts(2, 1));

		assertEquals(Status.READY, after.getStatus());
		assertEquals(MoveType.SWAP_SECTION, after.getMove().get().getType());
	}

	@Test
	public void sessionAcceptsADifferentButCorrectManualDistribution()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3, 4));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 4, 2}, plan, counts(1, 1));

		Assessment afterAlternative = session.assess(
			new int[]{1, 4, 3, 9, 2}, plan, counts(1, 2));
		assertEquals(Status.WAITING_FOR_BANK, afterAlternative.getStatus());
		afterAlternative = session.assess(
			new int[]{1, 4, 3, 9, 2}, plan, counts(1, 2));

		assertEquals(Status.READY, afterAlternative.getStatus());
		assertEquals(MoveType.DISTRIBUTE_TO_TAB,
			afterAlternative.getMove().get().getType());
		assertEquals(2, afterAlternative.getMove().get().getItemId());
	}

	@Test
	public void sessionTurnsAnAccidentalWrongTabDropIntoLocalizedRecovery()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3, 4));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 4, 2}, plan, counts(1, 1));

		Assessment recovery = session.assess(
			new int[]{4, 1, 3, 9, 2}, plan, counts(2, 1));
		assertEquals(Status.WAITING_FOR_BANK, recovery.getStatus());
		recovery = session.assess(
			new int[]{4, 1, 3, 9, 2}, plan, counts(2, 1));

		assertEquals(Status.READY, recovery.getStatus());
		assertEquals(MoveType.TRANSFER_TO_TAB, recovery.getMove().get().getType());
		assertEquals(4, recovery.getMove().get().getItemId());
		assertEquals(1, recovery.getMove().get().getSourceTab());
		assertEquals(2, recovery.getMove().get().getTargetTab());
	}

	@Test
	public void sessionAcknowledgesNumberedAndMainRecoveryTransitions()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3, 4));
		Session transferSession = new Session();
		transferSession.assess(new int[]{1, 3, 4, 9, 2}, plan, counts(2, 1));
		Assessment afterTransfer = transferSession.assess(
			new int[]{1, 3, 4, 9, 2}, plan, counts(1, 2));
		assertEquals(Status.READY, afterTransfer.getStatus());
		assertEquals(MoveType.DISTRIBUTE_TO_TAB,
			afterTransfer.getMove().get().getType());

		BankTabPlan mainPlan = plan(items(9), items(1, 2), items(3));
		Session mainSession = new Session();
		mainSession.assess(new int[]{1, 9, 3, 2}, mainPlan, counts(2, 1));
		Assessment afterMainRecovery = mainSession.assess(
			new int[]{1, 3, 9, 2}, mainPlan, counts(1, 1));
		assertEquals(Status.READY, afterMainRecovery.getStatus());
		assertEquals(MoveType.DISTRIBUTE_TO_TAB,
			afterMainRecovery.getMove().get().getType());
	}

	@Test
	public void sessionRecoversDistributionIntoTheWrongTab()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 2}, plan, counts(1, 1));

		Assessment recovery = session.assess(new int[]{1, 3, 2, 9}, plan, counts(1, 2));
		assertEquals(Status.WAITING_FOR_BANK, recovery.getStatus());
		recovery = session.assess(new int[]{1, 3, 2, 9}, plan, counts(1, 2));

		assertEquals(Status.READY, recovery.getStatus());
		assertEquals(MoveType.TRANSFER_TO_TAB, recovery.getMove().get().getType());
		assertEquals(2, recovery.getMove().get().getSourceTab());
		assertEquals(1, recovery.getMove().get().getTargetTab());
	}

	@Test
	public void sessionRecoversWhenAConcurrentSnapshotLaterSettlesExactly()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 2}, plan, counts(1, 1));

		assertEquals(Status.WAITING_FOR_BANK,
			session.assess(new int[]{3, 1, 9, 2}, plan, counts(1, 1)).getStatus());
		Assessment settled = session.assess(new int[]{2, 1, 3, 9}, plan, counts(2, 1));

		assertEquals(Status.READY, settled.getStatus());
		assertEquals(MoveType.SWAP_SECTION, settled.getMove().get().getType());
	}

	@Test
	public void sessionRejectsAnUnexpectedCollapseEvenWhenTheNewPlanWouldCreateTabs()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 2}, plan, counts(1, 1));

		Assessment afterCollapse = session.assess(
			new int[]{1, 3, 9, 2}, plan, counts(1));
		assertEquals(Status.WAITING_FOR_BANK, afterCollapse.getStatus());
		afterCollapse = session.assess(
			new int[]{1, 3, 9, 2}, plan, counts(1));

		assertEquals(Status.MANUAL_RECOVERY_REQUIRED, afterCollapse.getStatus());
	}

	@Test
	public void sessionRejectsCountsFirstHalfSnapshotWithoutShowingRecovery()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		session.assess(new int[]{1, 3, 9, 2}, plan, counts(1, 1));

		Assessment halfSnapshot = session.assess(
			new int[]{1, 3, 9, 2}, plan, counts(2, 1));

		assertEquals(Status.WAITING_FOR_BANK, halfSnapshot.getStatus());
		assertFalse(halfSnapshot.getMove().isPresent());

		Assessment settled = session.assess(
			new int[]{2, 1, 3, 9}, plan, counts(2, 1));
		assertEquals(Status.READY, settled.getStatus());
	}

	@Test
	public void sessionWaitsAPhysicalTickForALastTabCountsFirstSnapshot()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3, 4));
		Session session = new Session();
		session.assess(new int[]{1, 2, 3, 9, 4}, plan, counts(2, 1), 100);

		Assessment firstHalfFrame = session.assess(
			new int[]{1, 2, 3, 9, 4}, plan, counts(2, 2), 101);
		Assessment repeatedSameTick = session.assess(
			new int[]{1, 2, 3, 9, 4}, plan, counts(2, 2), 101);

		assertEquals(Status.WAITING_FOR_BANK, firstHalfFrame.getStatus());
		assertEquals(Status.WAITING_FOR_BANK, repeatedSameTick.getStatus());
		assertFalse(repeatedSameTick.getMove().isPresent());

		Assessment settled = session.assess(
			new int[]{1, 2, 3, 4, 9}, plan, counts(2, 2), 101);
		assertEquals(Status.COMPLETE, settled.getStatus());
	}

	@Test
	public void sessionDoesNotReadPastItemsWhenAnUnstableSnapshotWasPinned()
	{
		BankTabPlan plan = plan(items(9), items(), items());
		Session session = new Session();
		assertEquals(Status.UNSTABLE_BANK,
			session.assess(new int[]{9}, plan, counts(2)).getStatus());

		assertEquals(Status.WAITING_FOR_BANK,
			session.assess(new int[]{9}, plan, counts()).getStatus());
		assertEquals(Status.MANUAL_RECOVERY_REQUIRED,
			session.assess(new int[]{9}, plan, counts()).getStatus());
	}

	@Test
	public void sessionAcceptsCreateCollapseAndSectionSwapTransitions()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));

		Session createSession = new Session();
		createSession.assess(new int[]{9, 2, 1, 3}, plan, counts());
		Assessment afterCreate = createSession.assess(
			new int[]{1, 9, 2, 3}, plan, counts(1));
		assertEquals(MoveType.DRAG_TO_NEW_TAB, afterCreate.getMove().get().getType());

		Session collapseSession = new Session();
		collapseSession.assess(new int[]{1, 9, 2, 3}, plan, counts(1, 1));
		Assessment afterCollapse = collapseSession.assess(
			new int[]{1, 3, 9, 2}, plan, counts(1));
		assertEquals(Status.READY, afterCollapse.getStatus());

		Session swapSession = new Session();
		swapSession.assess(new int[]{2, 1, 3, 9}, plan, counts(2, 1));
		assertEquals(Status.COMPLETE,
			swapSession.assess(new int[]{1, 2, 3, 9}, plan, counts(2, 1)).getStatus());
	}

	@Test
	public void sessionAdvancesAnExpectedSwapWithinTheSameGameTick()
	{
		BankTabPlan plan = plan(items(9), items(1, 2), items(3));
		Session session = new Session();
		Assessment swap = session.assess(
			new int[]{2, 1, 3, 9}, plan, counts(2, 1), 100);
		assertEquals(MoveType.SWAP_SECTION, swap.getMove().get().getType());

		Assessment after = session.assess(
			new int[]{1, 2, 3, 9}, plan, counts(2, 1), 100);

		assertEquals(Status.COMPLETE, after.getStatus());
		assertFalse(after.getMove().isPresent());
	}

	@Test
	public void sessionResetsWhenPlanBoundariesChangeButFlatOrderDoesNot()
	{
		BankTabPlan firstPlan = plan(items(9), items(1, 2), items(3));
		BankTabPlan changedPlan = plan(items(9), items(1), items(2, 3));
		Session session = new Session();

		assertEquals(Status.COMPLETE,
			session.assess(new int[]{1, 2, 3, 9}, firstPlan, counts(2, 1)).getStatus());
		Assessment changed = session.assess(
			new int[]{1, 2, 3, 9}, changedPlan, counts(2, 1));

		assertMove(changed, MoveType.TRANSFER_TO_TAB, 2, 1, -1, 2, 3);
		assertEquals(1, changed.getMove().get().getSourceTab());
	}

	private static void assertTerminates(BankTabPlan plan, ModelBank bank)
	{
		assertTerminates(plan, bank, RearrangeMode.SWAP);
	}

	private static void assertTerminates(BankTabPlan plan, ModelBank bank, RearrangeMode mode)
	{
		for (int action = 0; action < 30; action++)
		{
			Assessment result = TabRouteAdvisor.assess(bank.itemIds(), plan, bank.tabCounts(),
				0, mode);
			if (result.getStatus() == Status.COMPLETE)
			{
				return;
			}
			assertEquals(Status.READY, result.getStatus());
			bank.apply(result.getMove().get());
		}
		throw new AssertionError("safe bucket route did not terminate: " + bank.items
			+ " counts=" + Arrays.toString(bank.counts));
	}

	private static void assertMove(Assessment assessment, MoveType type, int itemId,
		int fromSlot, int toSlot, int targetTab, int blueprintTab)
	{
		assertEquals(Status.READY, assessment.getStatus());
		Move move = assessment.getMove().get();
		assertEquals(type, move.getType());
		assertEquals(itemId, move.getItemId());
		assertEquals(fromSlot, move.getFromSlot());
		assertEquals(toSlot, move.getToSlot());
		assertEquals(targetTab, move.getTargetTab());
		assertEquals(blueprintTab, move.getBlueprintTabNumber());
	}

	private static BankTabPlan plan(List<BankPreviewItem> main,
		List<BankPreviewItem> firstPhysical, List<BankPreviewItem> secondPhysical)
	{
		@SuppressWarnings("unchecked")
		List<BankPreviewItem>[] categories = new List[10];
		for (int index = 0; index < categories.length; index++)
		{
			categories[index] = Collections.emptyList();
		}
		categories[0] = main;
		categories[1] = firstPhysical;
		categories[2] = secondPhysical;
		List<BankCategoryPreview> previews = new ArrayList<>();
		for (int index = 0; index < categories.length; index++)
		{
			previews.add(new BankCategoryPreview(
				BankPresets.IRONMAN.getCategories().get(index), categories[index]));
		}
		return BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, previews));
	}

	private static List<BankPreviewItem> items(int... itemIds)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int itemId : itemIds)
		{
			items.add(new BankPreviewItem(itemId, "Item " + itemId, 1));
		}
		return items;
	}

	private static int[] counts(int... leadingCounts)
	{
		int[] counts = new int[TabRouteAdvisor.MAX_TABS];
		System.arraycopy(leadingCounts, 0, counts, 0, leadingCounts.length);
		return counts;
	}

	private static ModelBank model(List<Integer> itemIds, int[] counts)
	{
		int[] items = new int[itemIds.size()];
		for (int index = 0; index < itemIds.size(); index++)
		{
			items[index] = itemIds.get(index);
		}
		return new ModelBank(items, counts);
	}

	private static List<List<Integer>> permutations(List<Integer> values)
	{
		List<List<Integer>> results = new ArrayList<>();
		permute(new ArrayList<>(values), 0, results);
		return results;
	}

	private static void permute(List<Integer> values, int index, List<List<Integer>> results)
	{
		if (index == values.size())
		{
			results.add(new ArrayList<>(values));
			return;
		}
		for (int swapIndex = index; swapIndex < values.size(); swapIndex++)
		{
			Collections.swap(values, index, swapIndex);
			permute(values, index + 1, results);
			Collections.swap(values, index, swapIndex);
		}
	}

	private static final class ModelBank
	{
		private final List<Integer> items = new ArrayList<>();
		private final int[] counts;

		private ModelBank(int[] itemIds, int[] counts)
		{
			for (int itemId : itemIds)
			{
				items.add(itemId);
			}
			this.counts = Arrays.copyOf(counts, counts.length);
		}

		private int[] itemIds()
		{
			int[] itemIds = new int[items.size()];
			for (int index = 0; index < items.size(); index++)
			{
				itemIds[index] = items.get(index);
			}
			return itemIds;
		}

		private int[] tabCounts()
		{
			return Arrays.copyOf(counts, counts.length);
		}

		private void apply(Move move)
		{
			switch (move.getType())
			{
				case COLLAPSE_TAB:
					collapse(move.getTargetTab());
					break;
				case DRAG_TO_NEW_TAB:
					assertTrue(move.getFromSlot() >= numberedSize());
					create(move.getFromSlot(), move.getTargetTab());
					break;
				case DISTRIBUTE_TO_TAB:
					assertTrue(move.getFromSlot() >= numberedSize());
					distributeAtFront(move.getFromSlot(), move.getTargetTab());
					break;
				case TRANSFER_TO_TAB:
					assertTrue(counts[move.getSourceTab() - 1] > 1);
					transferAtFront(move.getFromSlot(), move.getSourceTab(), move.getTargetTab());
					break;
				case RETURN_TO_MAIN:
					assertTrue(counts[move.getSourceTab() - 1] > 1);
					returnToMain(move.getFromSlot(), move.getSourceTab());
					break;
				case SWAP_SECTION:
					assertEquals(sectionForSlot(move.getFromSlot()),
						sectionForSlot(move.getToSlot()));
					Collections.swap(items, move.getFromSlot(), move.getToSlot());
					break;
				case INSERT_SECTION:
					assertEquals(sectionForSlot(move.getFromSlot()),
						sectionForSlot(move.getToSlot()));
					items.add(move.getToSlot(), items.remove(move.getFromSlot()));
					break;
				default:
					throw new AssertionError("unsupported move " + move.getType());
			}
		}

		private void collapse(int tabNumber)
		{
			int tabIndex = tabNumber - 1;
			int start = sectionStart(tabIndex);
			int count = counts[tabIndex];
			List<Integer> collapsed = new ArrayList<>(items.subList(start, start + count));
			items.subList(start, start + count).clear();
			items.addAll(collapsed);
			counts[tabIndex] = 0;
		}

		private void create(int sourceSlot, int tabNumber)
		{
			int itemId = items.remove(sourceSlot);
			int insertion = numberedSize();
			items.add(insertion, itemId);
			counts[tabNumber - 1] = 1;
		}

		private void distributeAtFront(int sourceSlot, int tabNumber)
		{
			int itemId = items.remove(sourceSlot);
			int tabIndex = tabNumber - 1;
			items.add(sectionStart(tabIndex), itemId);
			counts[tabIndex]++;
		}

		private void transferAtFront(int sourceSlot, int sourceTab, int targetTab)
		{
			int itemId = items.remove(sourceSlot);
			counts[sourceTab - 1]--;
			items.add(sectionStart(targetTab - 1), itemId);
			counts[targetTab - 1]++;
		}

		private void returnToMain(int sourceSlot, int sourceTab)
		{
			int itemId = items.remove(sourceSlot);
			counts[sourceTab - 1]--;
			items.add(itemId);
		}

		private int numberedSize()
		{
			int size = 0;
			for (int count : counts)
			{
				size += count;
			}
			return size;
		}

		private int sectionStart(int tabIndex)
		{
			int start = 0;
			for (int index = 0; index < tabIndex; index++)
			{
				start += counts[index];
			}
			return start;
		}

		private int sectionForSlot(int slot)
		{
			int start = 0;
			for (int index = 0; index < counts.length && counts[index] > 0; index++)
			{
				start += counts[index];
				if (slot < start)
				{
					return index + 1;
				}
			}
			return 0;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan.TargetTab;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure tab-aware route planner. It first builds clean category buckets from
 * main in top-to-bottom order, then sorts main and every numbered range using
 * same-section swaps only.
 */
public final class TabRouteAdvisor
{
	public static final int MAX_TABS = 9;

	private TabRouteAdvisor()
	{
	}

	public static Assessment assess(int[] actualItemIds, BankTabPlan plan, int[] tabCounts)
	{
		return assess(actualItemIds, plan, tabCounts, 0);
	}

	/**
	 * @param focusTabNumber 1-based bank tab whose interior sorting should be
	 * served first (the tab the player is currently viewing); 0 for the
	 * default order. Earlier phases (recovery, collapse, create, distribute)
	 * are unaffected because those require the All items view anyway.
	 */
	public static Assessment assess(int[] actualItemIds, BankTabPlan plan, int[] tabCounts,
		int focusTabNumber)
	{
		Objects.requireNonNull(actualItemIds, "actualItemIds");
		Objects.requireNonNull(plan, "plan");
		Objects.requireNonNull(tabCounts, "tabCounts");

		if (tabCounts.length != MAX_TABS || plan.getNumberedTabs().size() > MAX_TABS)
		{
			return Assessment.blocked(Status.UNSTABLE_BANK, Progress.none(), List.of());
		}

		Validation validation = validateItems(actualItemIds, plan.getFlattenedItems());
		if (validation.status != null)
		{
			return Assessment.blocked(validation.status, Progress.none(),
				validation.duplicateItemIds);
		}

		int currentTabs = leadingTabCount(tabCounts);
		if (currentTabs < 0)
		{
			return Assessment.blocked(Status.UNSTABLE_BANK, Progress.none(), List.of());
		}
		int mainStart = sumLeadingCounts(tabCounts, currentTabs);
		if (mainStart > actualItemIds.length)
		{
			return Assessment.blocked(Status.UNSTABLE_BANK, Progress.none(), List.of());
		}

		List<TargetTab> targets = plan.getNumberedTabs();
		Map<Integer, BankPreviewItem> itemById = itemsById(plan.getFlattenedItems());
		Map<Integer, Integer> targetTabByItemId = targetTabsByItemId(targets);
		Move recovery = firstRecoveryMove(actualItemIds, plan, tabCounts, currentTabs,
			itemById, targetTabByItemId);
		if (recovery != null)
		{
			return Assessment.ready(recovery,
				Progress.recovering(foreignItemCount(actualItemIds, targets,
					tabCounts, currentTabs)));
		}
		if (!isCleanBucketSkeleton(actualItemIds, plan, tabCounts, currentTabs))
		{
			if (currentTabs == 0)
			{
				return Assessment.blocked(Status.UNSTABLE_BANK, Progress.none(), List.of());
			}
			TargetTab target = currentTabs <= targets.size() ? targets.get(currentTabs - 1) : null;
			return Assessment.ready(Move.collapseTab(currentTabs, target),
				Progress.repairing(currentTabs));
		}

		if (currentTabs < targets.size())
		{
			TargetTab target = targets.get(currentTabs);
			int sourceSlot = firstMainSourceForItem(actualItemIds, mainStart,
				target.getItems().get(0).getItemId());
			if (sourceSlot < 0)
			{
				sourceSlot = firstMainSourceForTab(actualItemIds, mainStart,
					targetTabByItemId, target.getBankTabNumber());
			}
			if (sourceSlot < 0)
			{
				return Assessment.blocked(Status.UNSTABLE_BANK,
					Progress.creating(currentTabs, targets.size()), List.of());
			}
			return Assessment.ready(Move.dragToNewTab(itemById.get(actualItemIds[sourceSlot]),
				sourceSlot, target), Progress.creating(currentTabs, targets.size()));
		}

		int numberedItemTotal = numberedItemCount(targets);
		Move appendFriendly = nextAppendFriendlyDistribution(actualItemIds, mainStart,
			targets, tabCounts, itemById);
		if (appendFriendly != null)
		{
			return Assessment.ready(appendFriendly,
				Progress.distributing(mainStart, numberedItemTotal));
		}
		for (int slot = mainStart; slot < actualItemIds.length; slot++)
		{
			Integer targetTab = targetTabByItemId.get(actualItemIds[slot]);
			if (targetTab != null)
			{
				TargetTab target = targets.get(targetTab - 1);
				return Assessment.ready(Move.distributeToTab(itemById.get(actualItemIds[slot]),
					slot, target), Progress.distributing(mainStart, numberedItemTotal));
			}
		}

		if (!hasExactSectionMembership(actualItemIds, plan, tabCounts, mainStart))
		{
			return Assessment.blocked(Status.UNSTABLE_BANK,
				Progress.distributing(mainStart, numberedItemTotal), List.of());
		}

		int correctPositions = correctPositionCount(actualItemIds, plan.getFlattenedItems());
		Progress sorting = Progress.sorting(correctPositions, actualItemIds.length,
			minimumRemainingSwaps(actualItemIds, plan.getFlattenedItems()));
		if (focusTabNumber >= 1 && focusTabNumber <= targets.size())
		{
			int focusStart = 0;
			for (int tabIndex = 0; tabIndex < focusTabNumber - 1; tabIndex++)
			{
				focusStart += targets.get(tabIndex).getItems().size();
			}
			TargetTab focus = targets.get(focusTabNumber - 1);
			Move focusSwap = nextSectionSwap(actualItemIds, focusStart, focus.getItems(),
				focus.getBankTabNumber(), focus.getBlueprintCategoryNumber(),
				focus.getCategoryName());
			if (focusSwap != null)
			{
				return Assessment.ready(focusSwap, sorting);
			}
		}
		Move mainSwap = nextSectionSwap(actualItemIds, mainStart, plan.getMainItems(),
			0, 1, plan.getMainCategoryName());
		if (mainSwap != null)
		{
			return Assessment.ready(mainSwap, sorting);
		}

		int sectionStart = 0;
		for (TargetTab target : targets)
		{
			Move tabSwap = nextSectionSwap(actualItemIds, sectionStart, target.getItems(),
				target.getBankTabNumber(), target.getBlueprintCategoryNumber(),
				target.getCategoryName());
			if (tabSwap != null)
			{
				return Assessment.ready(tabSwap, sorting);
			}
			sectionStart += target.getItems().size();
		}

		return Assessment.blocked(Status.COMPLETE,
			Progress.complete(actualItemIds.length), List.of());
	}

	static boolean isCleanBucketSkeleton(int[] actualItemIds, BankTabPlan plan,
		int[] tabCounts, int currentTabs)
	{
		List<TargetTab> targets = plan.getNumberedTabs();
		if (currentTabs > targets.size())
		{
			return false;
		}

		int sectionStart = 0;
		for (int tabIndex = 0; tabIndex < currentTabs; tabIndex++)
		{
			int count = tabCounts[tabIndex];
			TargetTab target = targets.get(tabIndex);
			if (count <= 0 || count > target.getItems().size()
				|| sectionStart + count > actualItemIds.length)
			{
				return false;
			}
			Set<Integer> targetIds = itemIds(target.getItems());
			for (int slot = sectionStart; slot < sectionStart + count; slot++)
			{
				if (!targetIds.contains(actualItemIds[slot]))
				{
					return false;
				}
			}
			sectionStart += count;
		}
		return true;
	}

	private static Move firstRecoveryMove(int[] actualItemIds, BankTabPlan plan,
		int[] tabCounts, int currentTabs, Map<Integer, BankPreviewItem> itemById,
		Map<Integer, Integer> targetTabByItemId)
	{
		List<TargetTab> targets = plan.getNumberedTabs();
		int sectionStart = 0;
		for (int sourceIndex = 0; sourceIndex < currentTabs; sourceIndex++)
		{
			int sourceCount = tabCounts[sourceIndex];
			Set<Integer> sourceTargetIds = sourceIndex < targets.size()
				? itemIds(targets.get(sourceIndex).getItems()) : java.util.Collections.emptySet();
			for (int slot = sectionStart; slot < sectionStart + sourceCount; slot++)
			{
				int itemId = actualItemIds[slot];
				if (sourceTargetIds.contains(itemId) || sourceCount <= 1)
				{
					continue;
				}
				Integer targetTab = targetTabByItemId.get(itemId);
				BankPreviewItem item = itemById.get(itemId);
				if (targetTab == null)
				{
					return Move.returnToMain(item, slot, sourceIndex + 1,
						plan.getMainCategoryName());
				}
				if (targetTab <= currentTabs)
				{
					return Move.transferToTab(item, slot, sourceIndex + 1,
						targets.get(targetTab - 1));
				}
			}
			sectionStart += sourceCount;
		}
		return null;
	}

	private static int foreignItemCount(int[] actualItemIds, List<TargetTab> targets,
		int[] tabCounts, int currentTabs)
	{
		int foreign = 0;
		int sectionStart = 0;
		for (int tabIndex = 0; tabIndex < currentTabs; tabIndex++)
		{
			Set<Integer> targetIds = tabIndex < targets.size()
				? itemIds(targets.get(tabIndex).getItems()) : java.util.Collections.emptySet();
			for (int slot = sectionStart; slot < sectionStart + tabCounts[tabIndex]; slot++)
			{
				if (!targetIds.contains(actualItemIds[slot]))
				{
					foreign++;
				}
			}
			sectionStart += tabCounts[tabIndex];
		}
		return foreign;
	}

	private static boolean hasExactSectionMembership(int[] actualItemIds, BankTabPlan plan,
		int[] tabCounts, int mainStart)
	{
		List<TargetTab> targets = plan.getNumberedTabs();
		int sectionStart = 0;
		for (int tabIndex = 0; tabIndex < targets.size(); tabIndex++)
		{
			TargetTab target = targets.get(tabIndex);
			if (tabCounts[tabIndex] != target.getItems().size()
				|| !sectionSet(actualItemIds, sectionStart,
					sectionStart + tabCounts[tabIndex]).equals(itemIds(target.getItems())))
			{
				return false;
			}
			sectionStart += tabCounts[tabIndex];
		}
		return actualItemIds.length - mainStart == plan.getMainItems().size()
			&& sectionSet(actualItemIds, mainStart, actualItemIds.length)
				.equals(itemIds(plan.getMainItems()));
	}

	/**
	 * Anchored cycle walk: the first mismatched slot is the fixed pickup anchor.
	 * Each advised swap drags the anchor's occupant to that item's final slot, so
	 * the displaced item lands back on the anchor and the player keeps grabbing
	 * from one unchanged slot until the cycle closes and the anchor becomes
	 * correct. Every swap still fixes at least one slot, so the total stays at
	 * the exact lower bound {@code n - permutation cycles}, but consecutive
	 * pickups no longer jump between interleaved cycles.
	 */
	private static Move nextSectionSwap(int[] actualItemIds, int sectionStart,
		List<BankPreviewItem> targetItems, int targetTab, int blueprintTabNumber,
		String categoryName)
	{
		Map<Integer, Integer> targetOffsetByItemId = new HashMap<>();
		for (int offset = 0; offset < targetItems.size(); offset++)
		{
			targetOffsetByItemId.put(targetItems.get(offset).getItemId(), offset);
		}

		for (int offset = 0; offset < targetItems.size(); offset++)
		{
			int anchorSlot = sectionStart + offset;
			if (actualItemIds[anchorSlot] == targetItems.get(offset).getItemId())
			{
				continue;
			}
			Integer homeOffset = targetOffsetByItemId.get(actualItemIds[anchorSlot]);
			if (homeOffset == null)
			{
				return null;
			}
			BankPreviewItem occupant = targetItems.get(homeOffset);
			return Move.swapSection(occupant, anchorSlot, sectionStart + homeOffset,
				targetTab, blueprintTabNumber, categoryName);
		}
		return null;
	}

	private static Move nextAppendFriendlyDistribution(int[] actualItemIds, int mainStart,
		List<TargetTab> targets, int[] tabCounts, Map<Integer, BankPreviewItem> itemById)
	{
		int sectionStart = 0;
		for (TargetTab target : targets)
		{
			int count = tabCounts[target.getBankTabNumber() - 1];
			if (count >= target.getItems().size())
			{
				sectionStart += count;
				continue;
			}
			int appendItemId = cycleClosingAppendItemId(actualItemIds, sectionStart,
				count, target.getItems());
			int sourceSlot = firstMainSourceForItem(actualItemIds, mainStart,
				appendItemId);
			if (sourceSlot >= 0)
			{
				return Move.distributeToTab(itemById.get(actualItemIds[sourceSlot]),
					sourceSlot, target);
			}
			sectionStart += count;
		}
		return null;
	}

	/**
	 * Selects the remaining item that maximizes the permutation-cycle count after append.
	 *
	 * <p>If the item expected at the append slot is still in Main, appending it creates a fixed
	 * point. If it is already misplaced in the fixed tab prefix, that prefix contains one open
	 * path ending at the append slot. Following predecessor items back to the path start identifies
	 * the still-unplaced item that closes the whole path into one cycle. Closing every path
	 * independently maximizes cycles and therefore minimizes the later swap count {@code n-cycles}.
	 * No search tree is required.</p>
	 */
	static int cycleClosingAppendItemId(int[] actualItemIds, int sectionStart, int currentCount,
		List<BankPreviewItem> targetItems)
	{
		if (sectionStart < 0 || currentCount < 0
			|| sectionStart + currentCount > actualItemIds.length
			|| currentCount >= targetItems.size())
		{
			throw new IllegalArgumentException("invalid partial section bounds");
		}

		Map<Integer, Integer> prefixPositionByItemId = new HashMap<>();
		for (int position = 0; position < currentCount; position++)
		{
			prefixPositionByItemId.put(actualItemIds[sectionStart + position], position);
		}

		int pathStart = currentCount;
		boolean[] visitedPrefixPositions = new boolean[currentCount];
		while (true)
		{
			int itemId = targetItems.get(pathStart).getItemId();
			Integer predecessor = prefixPositionByItemId.get(itemId);
			if (predecessor == null)
			{
				return itemId;
			}
			if (visitedPrefixPositions[predecessor])
			{
				throw new IllegalArgumentException("partial section does not form an open append path");
			}
			visitedPrefixPositions[predecessor] = true;
			pathStart = predecessor;
		}
	}

	private static Validation validateItems(int[] actualItemIds, List<BankPreviewItem> plannedItems)
	{
		Set<Integer> plannedIds = new HashSet<>();
		for (BankPreviewItem item : plannedItems)
		{
			if (item == null || item.isBlank() || item.getItemId() <= 0)
			{
				return Validation.blocked(Status.UNSUPPORTED_PLAN, List.of());
			}
			if (!plannedIds.add(item.getItemId()))
			{
				return Validation.blocked(Status.DUPLICATE_ITEMS,
					duplicateItemIds(actualItemIds, plannedItems));
			}
		}

		Set<Integer> actualIds = new HashSet<>();
		for (int itemId : actualItemIds)
		{
			if (itemId <= 0)
			{
				return Validation.blocked(Status.UNSTABLE_BANK, List.of());
			}
			if (!actualIds.add(itemId))
			{
				return Validation.blocked(Status.DUPLICATE_ITEMS,
					duplicateItemIds(actualItemIds, plannedItems));
			}
		}
		return actualItemIds.length == plannedItems.size() && actualIds.equals(plannedIds)
			? Validation.valid()
			: Validation.blocked(Status.RESCAN_REQUIRED, List.of());
	}

	private static List<Integer> duplicateItemIds(int[] actualItemIds,
		List<BankPreviewItem> plannedItems)
	{
		Set<Integer> duplicates = new TreeSet<>();
		Set<Integer> plannedIds = new HashSet<>();
		for (BankPreviewItem item : plannedItems)
		{
			if (item != null && item.getItemId() > 0 && !plannedIds.add(item.getItemId()))
			{
				duplicates.add(item.getItemId());
			}
		}

		Set<Integer> actualIds = new HashSet<>();
		for (int itemId : actualItemIds)
		{
			if (itemId > 0 && !actualIds.add(itemId))
			{
				duplicates.add(itemId);
			}
		}
		return List.copyOf(duplicates);
	}

	private static Map<Integer, BankPreviewItem> itemsById(List<BankPreviewItem> items)
	{
		Map<Integer, BankPreviewItem> result = new HashMap<>();
		for (BankPreviewItem item : items)
		{
			result.put(item.getItemId(), item);
		}
		return result;
	}

	private static Map<Integer, Integer> targetTabsByItemId(List<TargetTab> targets)
	{
		Map<Integer, Integer> result = new HashMap<>();
		for (TargetTab target : targets)
		{
			for (BankPreviewItem item : target.getItems())
			{
				result.put(item.getItemId(), target.getBankTabNumber());
			}
		}
		return result;
	}

	private static int firstMainSourceForTab(int[] actualItemIds, int mainStart,
		Map<Integer, Integer> targetTabByItemId, int targetTab)
	{
		for (int slot = mainStart; slot < actualItemIds.length; slot++)
		{
			if (Integer.valueOf(targetTab).equals(targetTabByItemId.get(actualItemIds[slot])))
			{
				return slot;
			}
		}
		return -1;
	}

	private static int firstMainSourceForItem(int[] actualItemIds, int mainStart, int itemId)
	{
		for (int slot = mainStart; slot < actualItemIds.length; slot++)
		{
			if (actualItemIds[slot] == itemId)
			{
				return slot;
			}
		}
		return -1;
	}

	/** Exact unrestricted same-section swap lower bound: n minus permutation cycles. */
	static int minimumRemainingSwaps(int[] actualItemIds, List<BankPreviewItem> targetItems)
	{
		Map<Integer, Integer> targetSlotByItemId = new HashMap<>();
		for (int slot = 0; slot < targetItems.size(); slot++)
		{
			targetSlotByItemId.put(targetItems.get(slot).getItemId(), slot);
		}
		boolean[] visited = new boolean[actualItemIds.length];
		int cycles = 0;
		for (int start = 0; start < actualItemIds.length; start++)
		{
			if (visited[start])
			{
				continue;
			}
			cycles++;
			int slot = start;
			while (!visited[slot])
			{
				visited[slot] = true;
				Integer next = targetSlotByItemId.get(actualItemIds[slot]);
				if (next == null)
				{
					throw new IllegalArgumentException("actual and target item IDs must match");
				}
				slot = next;
			}
		}
		return actualItemIds.length - cycles;
	}

	private static int numberedItemCount(List<TargetTab> targets)
	{
		int count = 0;
		for (TargetTab target : targets)
		{
			count += target.getItems().size();
		}
		return count;
	}

	private static int correctPositionCount(int[] actualItemIds, List<BankPreviewItem> targetItems)
	{
		int correct = 0;
		for (int slot = 0; slot < actualItemIds.length; slot++)
		{
			if (actualItemIds[slot] == targetItems.get(slot).getItemId())
			{
				correct++;
			}
		}
		return correct;
	}

	private static Set<Integer> itemIds(List<BankPreviewItem> items)
	{
		Set<Integer> result = new HashSet<>();
		for (BankPreviewItem item : items)
		{
			result.add(item.getItemId());
		}
		return result;
	}

	private static Set<Integer> sectionSet(int[] itemIds, int start, int end)
	{
		Set<Integer> result = new HashSet<>();
		for (int slot = start; slot < end; slot++)
		{
			result.add(itemIds[slot]);
		}
		return result;
	}

	private static int leadingTabCount(int[] tabCounts)
	{
		int tabs = 0;
		boolean ended = false;
		for (int count : tabCounts)
		{
			if (count < 0)
			{
				return -1;
			}
			if (count == 0)
			{
				ended = true;
			}
			else if (ended)
			{
				return -1;
			}
			else
			{
				tabs++;
			}
		}
		return tabs;
	}

	private static int sumLeadingCounts(int[] tabCounts, int currentTabs)
	{
		int sum = 0;
		for (int tabIndex = 0; tabIndex < currentTabs; tabIndex++)
		{
			sum += tabCounts[tabIndex];
		}
		return sum;
	}

	/**
	 * Pins one recommendation while only widgets/scrolling change and verifies
	 * the bank transition before advancing.
	 */
	public static final class Session
	{
		private BankTabPlan pinnedPlan;
		private int[] pinnedActualItemIds;
		private int[] pinnedTabCounts;
		private int pinnedFocusTab;
		private Assessment pinnedAssessment;
		private int[] pendingActualItemIds;
		private int[] pendingTabCounts;
		private int pendingSinceTick;
		private int syntheticTick;
		private int focusTabNumber;

		public Assessment assess(int[] actualItemIds, BankTabPlan plan, int[] tabCounts)
		{
			return assess(actualItemIds, plan, tabCounts, ++syntheticTick);
		}

		public Assessment assess(int[] actualItemIds, BankTabPlan plan, int[] tabCounts,
			int tickCount)
		{
			return assess(actualItemIds, plan, tabCounts, tickCount, 0);
		}

		public Assessment assess(int[] actualItemIds, BankTabPlan plan, int[] tabCounts,
			int tickCount, int focusTabNumber)
		{
			Objects.requireNonNull(actualItemIds, "actualItemIds");
			Objects.requireNonNull(plan, "plan");
			Objects.requireNonNull(tabCounts, "tabCounts");

			this.focusTabNumber = focusTabNumber;
			if (pinnedPlan != plan)
			{
				reset();
				pinnedPlan = plan;
			}
			if (pinnedAssessment != null
				&& Arrays.equals(pinnedActualItemIds, actualItemIds)
				&& Arrays.equals(pinnedTabCounts, tabCounts))
			{
				if (pinnedFocusTab != focusTabNumber)
				{
					// Only the viewed tab changed; the bank itself did not, so
					// recompute directly without transition verification.
					pin(actualItemIds, tabCounts,
						TabRouteAdvisor.assess(actualItemIds, plan, tabCounts, focusTabNumber));
				}
				clearPending();
				return pinnedAssessment;
			}

			if (pinnedAssessment != null)
			{
				if (pinnedAssessment.getStatus() != Status.READY
					|| !transitionMatches(pinnedActualItemIds, pinnedTabCounts,
						actualItemIds, tabCounts, pinnedAssessment.getMove().get()))
				{
					return recoverAlternative(actualItemIds, plan, tabCounts, tickCount);
				}
			}

			pin(actualItemIds, tabCounts,
				TabRouteAdvisor.assess(actualItemIds, plan, tabCounts, focusTabNumber));
			return pinnedAssessment;
		}

		private Assessment recoverAlternative(int[] actualItemIds, BankTabPlan plan,
			int[] tabCounts, int tickCount)
		{
			if (!Arrays.equals(pendingActualItemIds, actualItemIds)
				|| !Arrays.equals(pendingTabCounts, tabCounts))
			{
				pendingActualItemIds = Arrays.copyOf(actualItemIds, actualItemIds.length);
				pendingTabCounts = Arrays.copyOf(tabCounts, tabCounts.length);
				pendingSinceTick = tickCount;
				return Assessment.blocked(Status.WAITING_FOR_BANK,
					pinnedAssessment.getProgress(), List.of());
			}
			if (tickCount <= pendingSinceTick)
			{
				return Assessment.blocked(Status.WAITING_FOR_BANK,
					pinnedAssessment.getProgress(), List.of());
			}
			Assessment alternative = TabRouteAdvisor.assess(actualItemIds, plan, tabCounts,
				focusTabNumber);
			if (isSafeAlternative(alternative)
				&& matchesSafeManualTransition(pinnedActualItemIds, pinnedTabCounts,
					actualItemIds, tabCounts))
			{
				pin(actualItemIds, tabCounts, alternative);
				return alternative;
			}
			return Assessment.blocked(Status.MANUAL_RECOVERY_REQUIRED,
				pinnedAssessment.getProgress(), List.of());
		}

		private static boolean isSafeAlternative(Assessment assessment)
		{
			return assessment.getStatus() == Status.COMPLETE
				|| assessment.getStatus() == Status.READY
					&& assessment.getProgress().getPhase() != Phase.REPAIRING;
		}

		private void pin(int[] actualItemIds, int[] tabCounts, Assessment assessment)
		{
			pinnedActualItemIds = Arrays.copyOf(actualItemIds, actualItemIds.length);
			pinnedTabCounts = Arrays.copyOf(tabCounts, tabCounts.length);
			pinnedFocusTab = focusTabNumber;
			pinnedAssessment = assessment;
			clearPending();
		}

		private void clearPending()
		{
			pendingActualItemIds = null;
			pendingTabCounts = null;
			pendingSinceTick = 0;
		}

		public void reset()
		{
			pinnedPlan = null;
			pinnedActualItemIds = null;
			pinnedTabCounts = null;
			pinnedAssessment = null;
			clearPending();
		}
	}

	private static boolean matchesSafeManualTransition(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts)
	{
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| beforeCounts.length != afterCounts.length
			|| !hasValidSectionBounds(beforeItems, beforeCounts)
			|| !hasValidSectionBounds(afterItems, afterCounts))
		{
			return false;
		}
		int beforeTabs = leadingTabCount(beforeCounts);
		int afterTabs = leadingTabCount(afterCounts);
		if (beforeTabs < 0 || afterTabs < beforeTabs || afterTabs > beforeTabs + 1)
		{
			return false;
		}
		if (afterTabs == beforeTabs + 1)
		{
			return matchesAnyCreate(beforeItems, beforeCounts, afterItems, afterCounts,
				beforeTabs);
		}

		int increased = -1;
		int decreased = -1;
		for (int index = 0; index < beforeCounts.length; index++)
		{
			int delta = afterCounts[index] - beforeCounts[index];
			if (delta == 1 && increased < 0)
			{
				increased = index;
			}
			else if (delta == -1 && decreased < 0)
			{
				decreased = index;
			}
			else if (delta != 0)
			{
				return false;
			}
		}

		if (increased >= 0 && decreased >= 0)
		{
			return matchesAnyTransfer(beforeItems, beforeCounts, afterItems, afterCounts,
				decreased, increased, beforeTabs);
		}
		if (increased >= 0)
		{
			return matchesAnyDistribution(beforeItems, beforeCounts, afterItems, afterCounts,
				increased, beforeTabs);
		}
		if (decreased >= 0)
		{
			return matchesAnyReturnToMain(beforeItems, beforeCounts, afterItems, afterCounts,
				decreased, beforeTabs);
		}
		return matchesAnySwap(beforeItems, afterItems);
	}

	private static boolean hasValidSectionBounds(int[] items, int[] counts)
	{
		int tabs = leadingTabCount(counts);
		return tabs >= 0 && sumLeadingCounts(counts, tabs) <= items.length;
	}

	private static boolean matchesAnyCreate(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int beforeTabs)
	{
		if (beforeTabs >= afterCounts.length || afterCounts[beforeTabs] != 1)
		{
			return false;
		}
		for (int index = 0; index < afterCounts.length; index++)
		{
			int expected = index < beforeTabs ? beforeCounts[index]
				: index == beforeTabs ? 1 : 0;
			if (afterCounts[index] != expected || index >= beforeTabs && beforeCounts[index] != 0)
			{
				return false;
			}
		}
		for (int index = 0; index < beforeTabs; index++)
		{
			if (!tabSectionSet(beforeItems, beforeCounts, index)
				.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		Set<Integer> created = tabSectionSet(afterItems, afterCounts, beforeTabs);
		return created.size() == 1 && mainLostOnly(beforeItems, beforeCounts,
			afterItems, afterCounts, created.iterator().next());
	}

	private static boolean matchesAnyDistribution(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int targetIndex, int tabs)
	{
		Set<Integer> added = addedItems(tabSectionSet(beforeItems, beforeCounts, targetIndex),
			tabSectionSet(afterItems, afterCounts, targetIndex));
		if (added.size() != 1 || !otherTabSectionsUnchanged(beforeItems, beforeCounts,
			afterItems, afterCounts, tabs, targetIndex, -1))
		{
			return false;
		}
		return mainLostOnly(beforeItems, beforeCounts, afterItems, afterCounts,
			added.iterator().next());
	}

	private static boolean matchesAnyTransfer(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int sourceIndex, int targetIndex, int tabs)
	{
		if (sourceIndex == targetIndex || beforeCounts[sourceIndex] <= 1)
		{
			return false;
		}
		Set<Integer> beforeSource = tabSectionSet(beforeItems, beforeCounts, sourceIndex);
		Set<Integer> afterSource = tabSectionSet(afterItems, afterCounts, sourceIndex);
		Set<Integer> removed = removedItems(beforeSource, afterSource);
		Set<Integer> added = addedItems(tabSectionSet(beforeItems, beforeCounts, targetIndex),
			tabSectionSet(afterItems, afterCounts, targetIndex));
		return removed.size() == 1 && removed.equals(added)
			&& otherTabSectionsUnchanged(beforeItems, beforeCounts, afterItems, afterCounts,
				tabs, sourceIndex, targetIndex)
			&& mainSectionSet(beforeItems, beforeCounts)
				.equals(mainSectionSet(afterItems, afterCounts));
	}

	private static boolean matchesAnyReturnToMain(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int sourceIndex, int tabs)
	{
		if (beforeCounts[sourceIndex] <= 1
			|| !otherTabSectionsUnchanged(beforeItems, beforeCounts, afterItems, afterCounts,
				tabs, sourceIndex, -1))
		{
			return false;
		}
		Set<Integer> removed = removedItems(
			tabSectionSet(beforeItems, beforeCounts, sourceIndex),
			tabSectionSet(afterItems, afterCounts, sourceIndex));
		if (removed.size() != 1)
		{
			return false;
		}
		Set<Integer> expectedMain = mainSectionSet(beforeItems, beforeCounts);
		expectedMain.add(removed.iterator().next());
		return expectedMain.equals(mainSectionSet(afterItems, afterCounts));
	}

	private static boolean matchesAnySwap(int[] beforeItems, int[] afterItems)
	{
		int first = -1;
		int second = -1;
		for (int slot = 0; slot < beforeItems.length; slot++)
		{
			if (beforeItems[slot] == afterItems[slot])
			{
				continue;
			}
			if (first < 0)
			{
				first = slot;
			}
			else if (second < 0)
			{
				second = slot;
			}
			else
			{
				return false;
			}
		}
		return first >= 0 && second >= 0
			&& beforeItems[first] == afterItems[second]
			&& beforeItems[second] == afterItems[first];
	}

	private static boolean otherTabSectionsUnchanged(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int tabs, int firstExcluded, int secondExcluded)
	{
		for (int index = 0; index < tabs; index++)
		{
			if (index != firstExcluded && index != secondExcluded
				&& !tabSectionSet(beforeItems, beforeCounts, index)
					.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		return true;
	}

	private static Set<Integer> addedItems(Set<Integer> before, Set<Integer> after)
	{
		Set<Integer> added = new HashSet<>(after);
		added.removeAll(before);
		return added;
	}

	private static Set<Integer> removedItems(Set<Integer> before, Set<Integer> after)
	{
		Set<Integer> removed = new HashSet<>(before);
		removed.removeAll(after);
		return removed;
	}

	private static boolean transitionMatches(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		switch (move.getType())
		{
			case COLLAPSE_TAB:
				return matchesCollapse(beforeItems, beforeCounts,
					afterItems, afterCounts, move.getTargetTab());
			case DRAG_TO_NEW_TAB:
				return matchesCreate(beforeItems, beforeCounts,
					afterItems, afterCounts, move);
			case DISTRIBUTE_TO_TAB:
				return matchesDistribution(beforeItems, beforeCounts,
					afterItems, afterCounts, move);
			case TRANSFER_TO_TAB:
				return matchesTransfer(beforeItems, beforeCounts,
					afterItems, afterCounts, move);
			case RETURN_TO_MAIN:
				return matchesReturnToMain(beforeItems, beforeCounts,
					afterItems, afterCounts, move);
			case SWAP_SECTION:
				return matchesSwap(beforeItems, beforeCounts,
					afterItems, afterCounts, move);
			default:
				return false;
		}
	}

	private static boolean matchesCollapse(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int targetTab)
	{
		int targetIndex = targetTab - 1;
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| targetIndex < 0 || targetIndex >= beforeCounts.length
			|| beforeCounts.length != afterCounts.length
			|| beforeCounts[targetIndex] <= 0)
		{
			return false;
		}
		int preservedPrefixSize = 0;
		for (int index = 0; index < targetIndex; index++)
		{
			if (afterCounts[index] != beforeCounts[index])
			{
				return false;
			}
			preservedPrefixSize += beforeCounts[index];
		}
		for (int index = targetIndex; index < afterCounts.length; index++)
		{
			if (afterCounts[index] != 0)
			{
				return false;
			}
		}
		for (int slot = 0; slot < preservedPrefixSize; slot++)
		{
			if (beforeItems[slot] != afterItems[slot])
			{
				return false;
			}
		}
		return true;
	}

	private static boolean matchesCreate(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		int targetIndex = move.getTargetTab() - 1;
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| beforeCounts.length != afterCounts.length
			|| targetIndex < 0 || targetIndex >= beforeCounts.length)
		{
			return false;
		}
		for (int index = 0; index < beforeCounts.length; index++)
		{
			int expected = index < targetIndex ? beforeCounts[index]
				: index == targetIndex ? 1 : 0;
			if (afterCounts[index] != expected || index >= targetIndex && beforeCounts[index] != 0)
			{
				return false;
			}
		}
		int afterStart = sumLeadingCounts(afterCounts, targetIndex);
		if (afterItems[afterStart] != move.getItemId())
		{
			return false;
		}
		return lowerSectionsUnchanged(beforeItems, beforeCounts, afterItems, afterCounts,
			targetIndex) && mainLostOnly(beforeItems, beforeCounts, afterItems, afterCounts,
			move.getItemId());
	}

	private static boolean matchesDistribution(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		int targetIndex = move.getTargetTab() - 1;
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| beforeCounts.length != afterCounts.length
			|| targetIndex < 0 || targetIndex >= beforeCounts.length)
		{
			return false;
		}
		int tabs = leadingTabCount(beforeCounts);
		if (tabs < 0 || leadingTabCount(afterCounts) != tabs)
		{
			return false;
		}
		for (int index = 0; index < beforeCounts.length; index++)
		{
			int expected = beforeCounts[index] + (index == targetIndex ? 1 : 0);
			if (afterCounts[index] != expected)
			{
				return false;
			}
		}

		for (int index = 0; index < tabs; index++)
		{
			Set<Integer> before = tabSectionSet(beforeItems, beforeCounts, index);
			Set<Integer> expected = new HashSet<>(before);
			if (index == targetIndex)
			{
				expected.add(move.getItemId());
			}
			if (!expected.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		return mainLostOnly(beforeItems, beforeCounts, afterItems, afterCounts,
			move.getItemId());
	}

	private static boolean matchesSwap(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		if (!Arrays.equals(beforeCounts, afterCounts)
			|| beforeItems.length != afterItems.length
			|| move.getFromSlot() < 0 || move.getFromSlot() >= beforeItems.length
			|| move.getToSlot() < 0 || move.getToSlot() >= beforeItems.length)
		{
			return false;
		}
		int[] expected = Arrays.copyOf(beforeItems, beforeItems.length);
		int temporary = expected[move.getFromSlot()];
		expected[move.getFromSlot()] = expected[move.getToSlot()];
		expected[move.getToSlot()] = temporary;
		return Arrays.equals(expected, afterItems);
	}

	private static boolean matchesTransfer(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		int sourceIndex = move.getSourceTab() - 1;
		int targetIndex = move.getTargetTab() - 1;
		int tabs = leadingTabCount(beforeCounts);
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| beforeCounts.length != afterCounts.length
			|| tabs < 0 || leadingTabCount(afterCounts) != tabs
			|| sourceIndex < 0 || sourceIndex >= tabs
			|| targetIndex < 0 || targetIndex >= tabs
			|| sourceIndex == targetIndex || beforeCounts[sourceIndex] <= 1)
		{
			return false;
		}
		for (int index = 0; index < beforeCounts.length; index++)
		{
			int expected = beforeCounts[index]
				+ (index == targetIndex ? 1 : 0) - (index == sourceIndex ? 1 : 0);
			if (afterCounts[index] != expected)
			{
				return false;
			}
		}
		for (int index = 0; index < tabs; index++)
		{
			Set<Integer> expected = tabSectionSet(beforeItems, beforeCounts, index);
			if (index == sourceIndex)
			{
				expected.remove(move.getItemId());
			}
			if (index == targetIndex)
			{
				expected.add(move.getItemId());
			}
			if (!expected.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		return mainSectionSet(beforeItems, beforeCounts)
			.equals(mainSectionSet(afterItems, afterCounts));
	}

	private static boolean matchesReturnToMain(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, Move move)
	{
		int sourceIndex = move.getSourceTab() - 1;
		int tabs = leadingTabCount(beforeCounts);
		if (!sameUniqueItemSet(beforeItems, afterItems)
			|| beforeCounts.length != afterCounts.length
			|| tabs < 0 || leadingTabCount(afterCounts) != tabs
			|| sourceIndex < 0 || sourceIndex >= tabs || beforeCounts[sourceIndex] <= 1)
		{
			return false;
		}
		for (int index = 0; index < beforeCounts.length; index++)
		{
			int expected = beforeCounts[index] - (index == sourceIndex ? 1 : 0);
			if (afterCounts[index] != expected)
			{
				return false;
			}
		}
		for (int index = 0; index < tabs; index++)
		{
			Set<Integer> expected = tabSectionSet(beforeItems, beforeCounts, index);
			if (index == sourceIndex)
			{
				expected.remove(move.getItemId());
			}
			if (!expected.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		Set<Integer> expectedMain = mainSectionSet(beforeItems, beforeCounts);
		expectedMain.add(move.getItemId());
		return expectedMain.equals(mainSectionSet(afterItems, afterCounts));
	}

	private static boolean lowerSectionsUnchanged(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int sectionLimit)
	{
		for (int index = 0; index < sectionLimit; index++)
		{
			if (!tabSectionSet(beforeItems, beforeCounts, index)
				.equals(tabSectionSet(afterItems, afterCounts, index)))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean mainLostOnly(int[] beforeItems, int[] beforeCounts,
		int[] afterItems, int[] afterCounts, int itemId)
	{
		Set<Integer> expectedMain = mainSectionSet(beforeItems, beforeCounts);
		if (!expectedMain.remove(itemId))
		{
			return false;
		}
		return expectedMain.equals(mainSectionSet(afterItems, afterCounts));
	}

	private static Set<Integer> tabSectionSet(int[] items, int[] counts, int tabIndex)
	{
		int start = sumLeadingCounts(counts, tabIndex);
		return sectionSet(items, start, start + counts[tabIndex]);
	}

	private static Set<Integer> mainSectionSet(int[] items, int[] counts)
	{
		int tabs = leadingTabCount(counts);
		int start = tabs < 0 ? items.length : sumLeadingCounts(counts, tabs);
		return sectionSet(items, start, items.length);
	}

	private static boolean sameUniqueItemSet(int[] first, int[] second)
	{
		if (first.length != second.length)
		{
			return false;
		}
		Set<Integer> firstSet = new HashSet<>();
		Set<Integer> secondSet = new HashSet<>();
		for (int itemId : first)
		{
			firstSet.add(itemId);
		}
		for (int itemId : second)
		{
			secondSet.add(itemId);
		}
		return firstSet.size() == first.length && firstSet.equals(secondSet);
	}

	public enum Status
	{
		READY,
		COMPLETE,
		RESCAN_REQUIRED,
		DUPLICATE_ITEMS,
		UNSTABLE_BANK,
		UNSUPPORTED_PLAN,
		WAITING_FOR_BANK,
		MANUAL_RECOVERY_REQUIRED,
		MECHANICS_MISMATCH
	}

	public enum Phase
	{
		RECOVERING,
		REPAIRING,
		CREATING,
		DISTRIBUTING,
		SORTING,
		COMPLETE
	}

	public enum MoveType
	{
		COLLAPSE_TAB,
		DRAG_TO_NEW_TAB,
		DISTRIBUTE_TO_TAB,
		TRANSFER_TO_TAB,
		RETURN_TO_MAIN,
		SWAP_SECTION
	}

	public static final class Move
	{
		private final MoveType type;
		private final int itemId;
		private final String displayName;
		private final int fromSlot;
		private final int toSlot;
		private final int sourceTab;
		private final int targetTab;
		private final int blueprintTabNumber;
		private final String categoryName;

		private Move(MoveType type, int itemId, String displayName, int fromSlot,
			int toSlot, int sourceTab, int targetTab, int blueprintTabNumber,
			String categoryName)
		{
			this.type = Objects.requireNonNull(type, "type");
			this.itemId = itemId;
			this.displayName = Objects.requireNonNull(displayName, "displayName");
			this.fromSlot = fromSlot;
			this.toSlot = toSlot;
			this.sourceTab = sourceTab;
			this.targetTab = targetTab;
			this.blueprintTabNumber = blueprintTabNumber;
			this.categoryName = Objects.requireNonNull(categoryName, "categoryName");
		}

		private static Move collapseTab(int tabNumber, TargetTab target)
		{
			int blueprintNumber = target == null ? tabNumber + 1
				: target.getBlueprintCategoryNumber();
			String categoryName = target == null ? "" : target.getCategoryName();
			return new Move(MoveType.COLLAPSE_TAB, -1, "Tab " + blueprintNumber,
				-1, -1, tabNumber, tabNumber, blueprintNumber, categoryName);
		}

		private static Move dragToNewTab(BankPreviewItem item, int sourceSlot, TargetTab target)
		{
			return itemMove(MoveType.DRAG_TO_NEW_TAB, item, sourceSlot, target);
		}

		private static Move distributeToTab(BankPreviewItem item, int sourceSlot, TargetTab target)
		{
			return itemMove(MoveType.DISTRIBUTE_TO_TAB, item, sourceSlot, target);
		}

		private static Move transferToTab(BankPreviewItem item, int sourceSlot,
			int sourceTab, TargetTab target)
		{
			return new Move(MoveType.TRANSFER_TO_TAB, item.getItemId(), item.getDisplayName(),
				sourceSlot, -1, sourceTab, target.getBankTabNumber(),
				target.getBlueprintCategoryNumber(), target.getCategoryName());
		}

		private static Move returnToMain(BankPreviewItem item, int sourceSlot,
			int sourceTab, String mainCategoryName)
		{
			return new Move(MoveType.RETURN_TO_MAIN, item.getItemId(), item.getDisplayName(),
				sourceSlot, -1, sourceTab, 0, 1, mainCategoryName);
		}

		private static Move itemMove(MoveType type, BankPreviewItem item,
			int sourceSlot, TargetTab target)
		{
			return new Move(type, item.getItemId(), item.getDisplayName(), sourceSlot,
				-1, 0, target.getBankTabNumber(), target.getBlueprintCategoryNumber(),
				target.getCategoryName());
		}

		private static Move swapSection(BankPreviewItem item, int sourceSlot,
			int targetSlot, int targetTab, int blueprintTabNumber, String categoryName)
		{
			return new Move(MoveType.SWAP_SECTION, item.getItemId(), item.getDisplayName(),
				sourceSlot, targetSlot, targetTab, targetTab, blueprintTabNumber, categoryName);
		}

		public MoveType getType()
		{
			return type;
		}

		public int getItemId()
		{
			return itemId;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public int getFromSlot()
		{
			return fromSlot;
		}

		public int getToSlot()
		{
			return toSlot;
		}

		public int getTargetTab()
		{
			return targetTab;
		}

		public int getSourceTab()
		{
			return sourceTab;
		}

		public int getBlueprintTabNumber()
		{
			return blueprintTabNumber;
		}

		public String getCategoryName()
		{
			return categoryName;
		}
	}

	public static final class Assessment
	{
		private final Status status;
		private final Move move;
		private final Progress progress;
		private final List<Integer> duplicateItemIds;

		private Assessment(Status status, Move move, Progress progress,
			List<Integer> duplicateItemIds)
		{
			this.status = Objects.requireNonNull(status, "status");
			this.move = move;
			this.progress = Objects.requireNonNull(progress, "progress");
			this.duplicateItemIds = List.copyOf(
				Objects.requireNonNull(duplicateItemIds, "duplicateItemIds"));
		}

		private static Assessment ready(Move move, Progress progress)
		{
			return new Assessment(Status.READY, Objects.requireNonNull(move, "move"), progress,
				List.of());
		}

		private static Assessment blocked(Status status, Progress progress,
			List<Integer> duplicateItemIds)
		{
			return new Assessment(status, null, progress, duplicateItemIds);
		}

		public Status getStatus()
		{
			return status;
		}

		public Optional<Move> getMove()
		{
			return Optional.ofNullable(move);
		}

		public Progress getProgress()
		{
			return progress;
		}

		public List<Integer> getDuplicateItemIds()
		{
			return duplicateItemIds;
		}
	}

	public static final class Progress
	{
		private final Phase phase;
		private final int completed;
		private final int total;
		private final int minimumRemainingSwaps;

		private Progress(Phase phase, int completed, int total, int minimumRemainingSwaps)
		{
			this.phase = Objects.requireNonNull(phase, "phase");
			this.completed = completed;
			this.total = total;
			this.minimumRemainingSwaps = minimumRemainingSwaps;
		}

		private static Progress none()
		{
			return new Progress(Phase.REPAIRING, 0, 0, -1);
		}

		private static Progress recovering(int foreignItems)
		{
			return new Progress(Phase.RECOVERING, 0, foreignItems, -1);
		}

		private static Progress repairing(int tabs)
		{
			return new Progress(Phase.REPAIRING, 0, tabs, -1);
		}

		private static Progress creating(int created, int total)
		{
			return new Progress(Phase.CREATING, created, total, -1);
		}

		private static Progress distributing(int placed, int total)
		{
			return new Progress(Phase.DISTRIBUTING, placed, total, -1);
		}

		private static Progress sorting(int correct, int total, int minimumRemainingSwaps)
		{
			return new Progress(Phase.SORTING, correct, total, minimumRemainingSwaps);
		}

		private static Progress complete(int total)
		{
			return new Progress(Phase.COMPLETE, total, total, 0);
		}

		public Phase getPhase()
		{
			return phase;
		}

		public int getCompleted()
		{
			return completed;
		}

		public int getTotal()
		{
			return total;
		}

		public int getMinimumRemainingSwaps()
		{
			return minimumRemainingSwaps;
		}

		public int getPercent()
		{
			return total == 0 ? phase == Phase.COMPLETE ? 100 : 0 : completed * 100 / total;
		}
	}

	private static final class Validation
	{
		private final Status status;
		private final List<Integer> duplicateItemIds;

		private Validation(Status status, List<Integer> duplicateItemIds)
		{
			this.status = status;
			this.duplicateItemIds = List.copyOf(
				Objects.requireNonNull(duplicateItemIds, "duplicateItemIds"));
		}

		private static Validation valid()
		{
			return new Validation(null, List.of());
		}

		private static Validation blocked(Status status, List<Integer> duplicateItemIds)
		{
			return new Validation(Objects.requireNonNull(status, "status"), duplicateItemIds);
		}
	}
}

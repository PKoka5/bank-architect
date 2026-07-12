package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Pure next-move planner for a dense bank order. Guidance is deliberately
 * fail-closed: it only proposes a swap when the live bank and analyzed plan
 * contain the same unique item IDs and every planned target is occupied.
 */
public final class NextMoveAdvisor
{
	private NextMoveAdvisor()
	{
	}

	public static Assessment assess(int[] actualItemIds, List<BankPreviewItem> plannedItems)
	{
		return assess(actualItemIds, plannedItems, java.util.Collections.emptySet());
	}

	public static Assessment assess(int[] actualItemIds, List<BankPreviewItem> plannedItems,
		Set<Integer> preferredVisibleSlots)
	{
		Objects.requireNonNull(actualItemIds, "actualItemIds");
		Objects.requireNonNull(plannedItems, "plannedItems");
		Objects.requireNonNull(preferredVisibleSlots, "preferredVisibleSlots");

		Map<Integer, Integer> plannedCounts = new HashMap<>();
		Set<Integer> plannedDuplicates = new HashSet<>();
		for (BankPreviewItem planned : plannedItems)
		{
			int itemId = planned.getItemId();
			if (itemId <= 0 || planned.isBlank())
			{
				return Assessment.blocked(Status.UNSUPPORTED_PLAN,
					progress(actualItemIds, plannedItems));
			}
			if (plannedCounts.merge(itemId, 1, Integer::sum) > 1)
			{
				plannedDuplicates.add(itemId);
			}
		}

		Map<Integer, Integer> actualCounts = new HashMap<>();
		Set<Integer> actualDuplicates = new HashSet<>();
		for (int itemId : actualItemIds)
		{
			if (itemId <= 0)
			{
				continue;
			}
			if (actualCounts.merge(itemId, 1, Integer::sum) > 1)
			{
				actualDuplicates.add(itemId);
			}
		}

		GuideProgress progress = progress(actualItemIds, plannedItems);
		if (!plannedDuplicates.isEmpty() || !actualDuplicates.isEmpty())
		{
			return Assessment.blocked(Status.DUPLICATE_ITEMS, progress);
		}
		if (!plannedCounts.equals(actualCounts))
		{
			return Assessment.blocked(Status.RESCAN_REQUIRED, progress);
		}

		int plannedSize = plannedItems.size();
		if (actualItemIds.length < plannedSize)
		{
			return Assessment.blocked(Status.RESCAN_REQUIRED, progress);
		}
		for (int slot = 0; slot < plannedSize; slot++)
		{
			if (actualItemIds[slot] <= 0)
			{
				return Assessment.blocked(Status.UNSTABLE_TARGET, progress);
			}
		}
		for (int slot = plannedSize; slot < actualItemIds.length; slot++)
		{
			if (actualItemIds[slot] > 0)
			{
				return Assessment.blocked(Status.UNSTABLE_TARGET, progress);
			}
		}

		if (progress.getCorrectSlots() == progress.getPlannedSlots())
		{
			return Assessment.blocked(Status.COMPLETE, progress);
		}

		Map<Integer, Integer> slotByItemId = new HashMap<>();
		for (int slot = 0; slot < plannedSize; slot++)
		{
			slotByItemId.put(actualItemIds[slot], slot);
		}

		NextMove bestMove = null;
		int bestVisibilityRank = Integer.MAX_VALUE;
		int bestDistance = Integer.MAX_VALUE;
		for (int targetSlot = 0; targetSlot < plannedSize; targetSlot++)
		{
			BankPreviewItem planned = plannedItems.get(targetSlot);
			int expectedItemId = planned.getItemId();
			if (actualItemIds[targetSlot] == expectedItemId)
			{
				continue;
			}

			Integer sourceSlot = slotByItemId.get(expectedItemId);
			if (sourceSlot == null)
			{
				return Assessment.blocked(Status.RESCAN_REQUIRED, progress);
			}

			int distance = Math.abs(sourceSlot - targetSlot);
			int visibilityRank = visibilityRank(sourceSlot, targetSlot, preferredVisibleSlots);
			if (bestMove == null || visibilityRank < bestVisibilityRank
				|| (visibilityRank == bestVisibilityRank && distance < bestDistance)
				|| (visibilityRank == bestVisibilityRank && distance == bestDistance
					&& targetSlot < bestMove.getToSlot()))
			{
				bestMove = new NextMove(expectedItemId, planned.getDisplayName(), sourceSlot, targetSlot);
				bestVisibilityRank = visibilityRank;
				bestDistance = distance;
			}
		}

		return bestMove == null
			? Assessment.blocked(Status.RESCAN_REQUIRED, progress)
			: Assessment.ready(bestMove, progress);
	}

	private static int visibilityRank(int sourceSlot, int targetSlot, Set<Integer> visibleSlots)
	{
		if (visibleSlots.isEmpty())
		{
			return 0;
		}
		boolean sourceVisible = visibleSlots.contains(sourceSlot);
		boolean targetVisible = visibleSlots.contains(targetSlot);
		if (sourceVisible && targetVisible) return 0;
		if (sourceVisible || targetVisible) return 1;
		return 2;
	}

	public static Optional<NextMove> advise(int[] actualItemIds, List<BankPreviewItem> plannedItems)
	{
		return assess(actualItemIds, plannedItems).getMove();
	}

	/**
	 * Keeps one safe recommendation stable while only the bank viewport changes.
	 * A new recommendation is selected as soon as either the analyzed plan contents
	 * or the live bank item order changes.
	 */
	public static final class Session
	{
		private int[] pinnedActualItemIds;
		private List<BankPreviewItem> pinnedPlannedItemsSnapshot;
		private Assessment pinnedAssessment;

		public Assessment assess(int[] actualItemIds, List<BankPreviewItem> plannedItems,
			Set<Integer> preferredVisibleSlots)
		{
			Objects.requireNonNull(actualItemIds, "actualItemIds");
			Objects.requireNonNull(plannedItems, "plannedItems");
			Objects.requireNonNull(preferredVisibleSlots, "preferredVisibleSlots");

			if (pinnedAssessment != null && pinnedPlannedItemsSnapshot.equals(plannedItems)
				&& Arrays.equals(pinnedActualItemIds, actualItemIds))
			{
				return pinnedAssessment;
			}

			pinnedAssessment = NextMoveAdvisor.assess(actualItemIds, plannedItems,
				preferredVisibleSlots);
			pinnedActualItemIds = Arrays.copyOf(actualItemIds, actualItemIds.length);
			pinnedPlannedItemsSnapshot = new ArrayList<>(plannedItems);
			return pinnedAssessment;
		}

		public void reset()
		{
			pinnedActualItemIds = null;
			pinnedPlannedItemsSnapshot = null;
			pinnedAssessment = null;
		}
	}

	public static GuideProgress progress(int[] actualItemIds, List<BankPreviewItem> plannedItems)
	{
		return progress(actualItemIds, plannedItems, 0);
	}

	public static GuideProgress progress(int[] actualItemIds, List<BankPreviewItem> plannedItems,
		int actualStartSlot)
	{
		Objects.requireNonNull(actualItemIds, "actualItemIds");
		Objects.requireNonNull(plannedItems, "plannedItems");
		if (actualStartSlot < 0)
		{
			throw new IllegalArgumentException("actualStartSlot must not be negative");
		}

		int planned = 0;
		int correct = 0;
		for (int localSlot = 0; localSlot < plannedItems.size(); localSlot++)
		{
			int expectedItemId = plannedItems.get(localSlot).getItemId();
			if (expectedItemId <= 0)
			{
				continue;
			}

			planned++;
			int actualSlot = actualStartSlot + localSlot;
			if (actualSlot < actualItemIds.length && actualItemIds[actualSlot] == expectedItemId)
			{
				correct++;
			}
		}

		return new GuideProgress(correct, planned);
	}

	public enum Status
	{
		READY,
		COMPLETE,
		RESCAN_REQUIRED,
		DUPLICATE_ITEMS,
		UNSTABLE_TARGET,
		UNSUPPORTED_PLAN
	}

	public static final class Assessment
	{
		private final Status status;
		private final NextMove move;
		private final GuideProgress progress;

		private Assessment(Status status, NextMove move, GuideProgress progress)
		{
			this.status = Objects.requireNonNull(status, "status");
			this.move = move;
			this.progress = Objects.requireNonNull(progress, "progress");
		}

		private static Assessment ready(NextMove move, GuideProgress progress)
		{
			return new Assessment(Status.READY, Objects.requireNonNull(move, "move"), progress);
		}

		private static Assessment blocked(Status status, GuideProgress progress)
		{
			return new Assessment(status, null, progress);
		}

		public Status getStatus()
		{
			return status;
		}

		public Optional<NextMove> getMove()
		{
			return Optional.ofNullable(move);
		}

		public GuideProgress getProgress()
		{
			return progress;
		}
	}

	public static final class NextMove
	{
		private final int itemId;
		private final String displayName;
		private final int fromSlot;
		private final int toSlot;

		private NextMove(int itemId, String displayName, int fromSlot, int toSlot)
		{
			this.itemId = itemId;
			this.displayName = Objects.requireNonNull(displayName, "displayName");
			this.fromSlot = fromSlot;
			this.toSlot = toSlot;
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
	}

	public static final class GuideProgress
	{
		private final int correctSlots;
		private final int plannedSlots;

		private GuideProgress(int correctSlots, int plannedSlots)
		{
			this.correctSlots = correctSlots;
			this.plannedSlots = plannedSlots;
		}

		public int getCorrectSlots()
		{
			return correctSlots;
		}

		public int getPlannedSlots()
		{
			return plannedSlots;
		}

		public int getPercent()
		{
			return plannedSlots == 0 ? 100 : correctSlots * 100 / plannedSlots;
		}
	}
}

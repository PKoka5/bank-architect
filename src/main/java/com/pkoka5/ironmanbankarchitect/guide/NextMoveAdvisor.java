package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Pure next-move planner for a dense bank order. Guidance is deliberately
 * fail-closed: it only proposes a swap when the live bank and analyzed plan
 * contain the same item-ID multiplicities and every planned target is occupied.
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
		for (BankPreviewItem planned : plannedItems)
		{
			int itemId = planned.getItemId();
			if (itemId <= 0 || planned.isBlank())
			{
				return Assessment.blocked(Status.UNSUPPORTED_PLAN,
					progress(actualItemIds, plannedItems), List.of());
			}
			plannedCounts.merge(itemId, 1, Integer::sum);
		}

		Map<Integer, Integer> actualCounts = new HashMap<>();
		for (int itemId : actualItemIds)
		{
			if (itemId <= 0)
			{
				continue;
			}
			actualCounts.merge(itemId, 1, Integer::sum);
		}

		GuideProgress progress = progress(actualItemIds, plannedItems);
		if (!plannedCounts.equals(actualCounts))
		{
			List<Integer> duplicateItemIds = ItemOccurrenceMatcher.excessRepeatedItemIds(
				actualCounts, plannedCounts);
			if (!duplicateItemIds.isEmpty())
			{
				return Assessment.blocked(Status.DUPLICATE_ITEMS, progress,
					List.copyOf(duplicateItemIds));
			}
			return Assessment.blocked(Status.RESCAN_REQUIRED, progress, List.of());
		}

		int plannedSize = plannedItems.size();
		if (actualItemIds.length < plannedSize)
		{
			return Assessment.blocked(Status.RESCAN_REQUIRED, progress, List.of());
		}
		for (int slot = 0; slot < plannedSize; slot++)
		{
			if (actualItemIds[slot] <= 0)
			{
				return Assessment.blocked(Status.UNSTABLE_TARGET, progress, List.of());
			}
		}
		for (int slot = plannedSize; slot < actualItemIds.length; slot++)
		{
			if (actualItemIds[slot] > 0)
			{
				return Assessment.blocked(Status.UNSTABLE_TARGET, progress, List.of());
			}
		}

		if (progress.getCorrectSlots() == progress.getPlannedSlots())
		{
			return Assessment.blocked(Status.COMPLETE, progress, List.of());
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

			for (int sourceSlot = 0; sourceSlot < plannedSize; sourceSlot++)
			{
				if (actualItemIds[sourceSlot] != expectedItemId
					|| actualItemIds[sourceSlot] == plannedItems.get(sourceSlot).getItemId())
				{
					continue;
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
		}

		return bestMove == null
			? Assessment.blocked(Status.RESCAN_REQUIRED, progress, List.of())
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
		private final List<Integer> duplicateItemIds;

		private Assessment(Status status, NextMove move, GuideProgress progress,
			List<Integer> duplicateItemIds)
		{
			this.status = Objects.requireNonNull(status, "status");
			this.move = move;
			this.progress = Objects.requireNonNull(progress, "progress");
			this.duplicateItemIds = List.copyOf(
				Objects.requireNonNull(duplicateItemIds, "duplicateItemIds"));
		}

		private static Assessment ready(NextMove move, GuideProgress progress)
		{
			return new Assessment(Status.READY, Objects.requireNonNull(move, "move"), progress,
				List.of());
		}

		private static Assessment blocked(Status status, GuideProgress progress,
			List<Integer> duplicateItemIds)
		{
			return new Assessment(status, null, progress, duplicateItemIds);
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

		public List<Integer> getDuplicateItemIds()
		{
			return duplicateItemIds;
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

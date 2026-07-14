package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Deterministic bounded beam packer. It owns candidate generation so callers cannot inject partial
 * groups or non-canonical candidates. Only meaningful semantic cells occupy masks; nominal slack
 * may contain real spillover, but every nominal rectangle must remain inside the dense category.
 */
final class BoundedLayoutPacker
{
	static final int PRODUCTION_BEAM_WIDTH = 128;
	static final int PRODUCTION_CANDIDATE_ORIGIN_CAP = 1_000_000;
	private static final int GRID_COLUMNS = SemanticRule.MAX_WIDTH;

	private BoundedLayoutPacker()
	{
	}

	static Outcome packValidated(LayoutRequest request, List<Integer> stableFallbackItemIds,
		Limits limits)
	{
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(stableFallbackItemIds, "stableFallbackItemIds");
		Objects.requireNonNull(limits, "limits");

		Context context = new Context(request, stableFallbackItemIds);
		List<LayoutCandidateGroup> groups = new ArrayList<>(LayoutCandidateGenerator.generate(request));
		groups.sort(groupComparator(context));

		State initial = State.initial(context);
		State pureFallback = closeWithFallback(initial, groups, 0);
		CompletePlan fallbackPlan = complete(pureFallback, context);
		if (!fallbackPlan.result.isSuccess())
		{
			return Outcome.conflict(fallbackPlan.result);
		}

		List<State> beam = new ArrayList<>(Collections.singletonList(initial));
		int candidateOriginEvaluations = 0;
		int maximumBeamSize = beam.size();
		int materializedPlacementStates = 0;
		boolean capReached = false;

		for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++)
		{
			LayoutCandidateGroup group = groups.get(groupIndex);
			beam.sort(BoundedLayoutPacker::compareStates);
			BestChildCollector children = new BestChildCollector(limits.beamWidth);

			// Fallback is a safety transition, not a spatial search expansion, and never consumes cap.
			for (State parent : beam)
			{
				children.add(PendingChild.fallback(parent, group, context));
			}

			List<CandidateOption> options = candidateOptions(group, context);
			semanticExpansion:
			for (State parent : beam)
			{
				for (CandidateOption option : options)
				{
					for (Origin origin : option.origins)
					{
						if (candidateOriginEvaluations >= limits.candidateOriginCap)
						{
							capReached = true;
							break semanticExpansion;
						}
						candidateOriginEvaluations++;

						if (canPlace(parent, option, origin, context))
						{
							children.add(PendingChild.placed(parent, group, option, origin, context));
						}
					}
				}
			}

			BestChildCollector.MaterializedChildren materialized = children.toSortedStates();
			beam = materialized.states;
			materializedPlacementStates = Math.addExact(materializedPlacementStates,
				materialized.placementCount);
			maximumBeamSize = Math.max(maximumBeamSize, beam.size());
			if (capReached)
			{
				List<State> closed = new ArrayList<>(beam.size());
				for (State state : beam)
				{
					closed.add(closeWithFallback(state, groups, groupIndex + 1));
				}
				return chooseBest(closed, fallbackPlan, context,
					new SearchStats(candidateOriginEvaluations, true, maximumBeamSize,
						materializedPlacementStates));
			}
		}

		return chooseBest(beam, fallbackPlan, context,
			new SearchStats(candidateOriginEvaluations, false, maximumBeamSize,
				materializedPlacementStates));
	}

	private static Comparator<LayoutCandidateGroup> groupComparator(Context context)
	{
		return (left, right) ->
		{
			int result = Integer.compare(lockedProjectedCount(right, context),
				lockedProjectedCount(left, context));
			if (result != 0)
			{
				return result;
			}
			result = Integer.compare(left.getConfidenceTier().ordinal(), right.getConfidenceTier().ordinal());
			if (result != 0)
			{
				return result;
			}
			result = Integer.compare(right.getMissedCompleteness(), left.getMissedCompleteness());
			if (result != 0)
			{
				return result;
			}
			result = left.getRuleKey().compareTo(right.getRuleKey());
			return result != 0 ? result : compareStringVectors(left.getAtomKeys(), right.getAtomKeys());
		};
	}

	private static int lockedProjectedCount(LayoutCandidateGroup group, Context context)
	{
		int count = 0;
		for (Integer itemId : group.getProjectedItemIds())
		{
			if (context.lockedTargetByItemId.containsKey(itemId))
			{
				count++;
			}
		}
		return count;
	}

	private static List<CandidateOption> candidateOptions(LayoutCandidateGroup group, Context context)
	{
		List<CandidateOption> options = new ArrayList<>();
		for (LayoutCandidate candidate : group.getCandidates())
		{
			options.add(new CandidateOption(group, candidate, context));
		}
		options.sort((left, right) ->
		{
			int result = left.localScore.compareTo(right.localScore);
			if (result != 0)
			{
				return result;
			}
			result = Integer.compare(left.preferenceRank, right.preferenceRank);
			return result != 0 ? result : Integer.compare(left.candidate.getWidth(), right.candidate.getWidth());
		});
		return options;
	}

	private static boolean canPlace(State state, CandidateOption option, Origin origin,
		Context context)
	{
		LayoutCandidate candidate = option.candidate;
		if (!nominalRectangleFits(candidate, origin, context))
		{
			return false;
		}

		for (LocalCell cell : option.cells)
		{
			int row = origin.baseRow + cell.localRow;
			int column = origin.startColumn + cell.localColumn;
			int physicalTarget = context.physicalTarget(row, column);
			int target = context.localTarget(physicalTarget);
			int bit = 1 << column;
			if ((state.unavailableOrOccupiedMasks[row] & bit) != 0)
			{
				return false;
			}

			int lockedItem = context.lockedItemAtTarget[target];
			if (lockedItem != 0 && lockedItem != cell.itemId)
			{
				return false;
			}
			Integer ownLockedTarget = context.lockedTargetByItemId.get(cell.itemId);
			if (ownLockedTarget != null && ownLockedTarget != target)
			{
				return false;
			}
		}
		return true;
	}

	private static boolean nominalRectangleFits(LayoutCandidate candidate, Origin origin,
		Context context)
	{
		if (origin.baseRow < 0 || origin.startColumn < 0
			|| origin.startColumn > GRID_COLUMNS - candidate.getWidth()
			|| origin.baseRow > context.rowCount - candidate.getRows().size())
		{
			return false;
		}
		for (int localRow = 0; localRow < candidate.getRows().size(); localRow++)
		{
			long first = ((long) origin.baseRow + localRow) * GRID_COLUMNS + origin.startColumn;
			long last = first + candidate.getWidth() - 1L;
			if (!context.containsPhysicalTarget(first) || !context.containsPhysicalTarget(last))
			{
				return false;
			}
		}
		return true;
	}

	private static State closeWithFallback(State state, List<LayoutCandidateGroup> groups,
		int firstUnresolvedGroup)
	{
		State closed = state;
		for (int index = firstUnresolvedGroup; index < groups.size(); index++)
		{
			closed = closed.withFallback(groups.get(index));
		}
		return closed;
	}

	private static Outcome chooseBest(List<State> states, CompletePlan fallbackPlan,
		Context context, SearchStats stats)
	{
		CompletePlan best = fallbackPlan;
		for (State state : states)
		{
			CompletePlan plan = complete(state, context);
			if (plan.result.isSuccess() && compareCompletePlans(plan, best) < 0)
			{
				best = plan;
			}
		}
		return Outcome.success(best.result, best.score, best.tieKey, stats);
	}

	private static int compareCompletePlans(CompletePlan left, CompletePlan right)
	{
		return LayoutScore.compareByScoreThenTieKey(left.score, left.tieKey, right.score, right.tieKey);
	}

	private static CompletePlan complete(State state, Context context)
	{
		int[] finalItemIds = Arrays.copyOf(state.itemIdAtTarget, state.itemIdAtTarget.length);
		Set<Integer> alreadyPlaced = new HashSet<>();
		for (int itemId : finalItemIds)
		{
			if (itemId != 0)
			{
				alreadyPlaced.add(itemId);
			}
		}

		for (int target = 0; target < context.lockedItemAtTarget.length; target++)
		{
			int lockedItemId = context.lockedItemAtTarget[target];
			if (lockedItemId == 0 || alreadyPlaced.contains(lockedItemId))
			{
				continue;
			}
			if (finalItemIds[target] != 0)
			{
				return CompletePlan.invalid(LayoutResult.conflict(Collections.singletonList(
					new LayoutConflict(LayoutConflict.Type.PLAN_LOCK_VIOLATION, lockedItemId,
						"semantic placement occupied locked fallback target " + target))));
			}
			finalItemIds[target] = lockedItemId;
			alreadyPlaced.add(lockedItemId);
		}

		int freeTarget = 0;
		for (Integer itemId : context.stableFallbackItemIds)
		{
			if (alreadyPlaced.contains(itemId))
			{
				continue;
			}
			while (freeTarget < finalItemIds.length && finalItemIds[freeTarget] != 0)
			{
				freeTarget++;
			}
			if (freeTarget >= finalItemIds.length)
			{
				throw new IllegalStateException("validated fallback order exceeded dense target capacity");
			}
			finalItemIds[freeTarget] = itemId;
			alreadyPlaced.add(itemId);
		}

		List<LayoutConflict> geometryConflicts = CompleteLayoutGeometryValidator.validate(
			context.request, state.canonicalBlocks, state.itemIdAtTarget, finalItemIds);
		if (!geometryConflicts.isEmpty())
		{
			return CompletePlan.invalid(LayoutResult.conflict(geometryConflicts));
		}

		List<LayoutPlacement> placements = new ArrayList<>(finalItemIds.length);
		List<Integer> finalOrder = new ArrayList<>(finalItemIds.length);
		for (int target = 0; target < finalItemIds.length; target++)
		{
			int itemId = finalItemIds[target];
			LayoutEntry entry = context.entryByItemId.get(itemId);
			if (entry == null)
			{
				throw new IllegalStateException("dense completion did not contain a canonical request item");
			}
			placements.add(new LayoutPlacement(entry.getItem(), target));
			finalOrder.add(itemId);
		}

		LayoutResult result = LayoutPlanValidator.validate(context.request, placements);
		if (!result.isSuccess())
		{
			return CompletePlan.invalid(result);
		}
		LayoutScore score = completeScore(state, finalItemIds, context);
		DeterministicTieKey tieKey = new DeterministicTieKey(state.canonicalBlocks, finalOrder);
		return CompletePlan.success(result, score, tieKey);
	}

	private static LayoutScore completeScore(State state, int[] finalItemIds, Context context)
	{
		ScoreAccumulator score = new ScoreAccumulator();
		for (Decision decision : state.decisions)
		{
			if (decision.isFallback())
			{
				score.add(LayoutCandidateScorer.scoreFallback(decision.group));
				continue;
			}

			score.add(LayoutCandidateScorer.score(decision.group, decision.candidate));
			addBlockMetrics(score, decision, finalItemIds, context);
		}

		int[] movement = movement(finalItemIds, context.request);
		return score.build(context.request, movement[0], movement[1]);
	}

	private static void addBlockMetrics(ScoreAccumulator score, Decision decision,
		int[] finalItemIds, Context context)
	{
		LayoutCandidate candidate = decision.candidate;
		int height = candidate.getRows().size();
		int meaningfulCount = candidate.getRowMajorItemIds().size();
		score.add(14, Math.subtractExact(Math.multiplyExact(candidate.getWidth(), height), meaningfulCount));

		Map<Integer, Integer> startCounts = new HashMap<>();
		for (LayoutCandidate.Row row : candidate.getRows())
		{
			int start = decision.startColumn + row.getStartOffset();
			startCounts.merge(start, 1, Integer::sum);
		}
		int modalStart = Integer.MAX_VALUE;
		int modalCount = -1;
		for (Map.Entry<Integer, Integer> entry : startCounts.entrySet())
		{
			if (entry.getValue() > modalCount
				|| (entry.getValue() == modalCount && entry.getKey() < modalStart))
			{
				modalStart = entry.getKey();
				modalCount = entry.getValue();
			}
		}
		for (LayoutCandidate.Row row : candidate.getRows())
		{
			score.add(10, Math.abs(decision.startColumn + row.getStartOffset() - modalStart));
		}

		int minimumTarget = Integer.MAX_VALUE;
		int maximumTarget = Integer.MIN_VALUE;
		for (int localRow = 0; localRow < candidate.getRows().size(); localRow++)
		{
			LayoutCandidate.Row row = candidate.getRows().get(localRow);
			for (int memberIndex = 0; memberIndex < row.length(); memberIndex++)
			{
				int target = (decision.baseRow + localRow) * GRID_COLUMNS
					+ decision.startColumn + row.getStartOffset() + memberIndex;
				minimumTarget = Math.min(minimumTarget, target);
				maximumTarget = Math.max(maximumTarget, target);
			}
		}
		score.add(15, Math.addExact(Math.subtractExact(maximumTarget, minimumTarget), 1));

		Set<String> typedBlockSubcategories = typedBlockSubcategories(decision.group, context);
		for (int localRow = 0; localRow < candidate.getRows().size(); localRow++)
		{
			LayoutCandidate.Row row = candidate.getRows().get(localRow);
			boolean previousMeaningful = false;
			boolean hasPrevious = false;
			for (int localColumn = 0; localColumn < candidate.getWidth(); localColumn++)
			{
				int physicalTarget = (decision.baseRow + localRow) * GRID_COLUMNS
					+ decision.startColumn + localColumn;
				int target = context.localTarget(physicalTarget);
				boolean meaningful = localColumn >= row.getStartOffset()
					&& localColumn < row.getStartOffset() + row.length();
				if (!meaningful)
				{
					int spilloverItemId = finalItemIds[target];
					score.add(12, spilloverCompatibilityCost(decision.group, spilloverItemId,
						typedBlockSubcategories, context));
					if (localColumn < row.getStartOffset()
						&& context.lockedItemAtTarget[target] == spilloverItemId)
					{
						score.add(11, 1);
					}
				}
				if (hasPrevious && meaningful != previousMeaningful)
				{
					score.add(13, 1);
				}
				previousMeaningful = meaningful;
				hasPrevious = true;
			}
		}
	}

	private static Set<String> typedBlockSubcategories(LayoutCandidateGroup group, Context context)
	{
		Set<String> subcategories = new HashSet<>();
		for (Integer itemId : group.getProjectedItemIds())
		{
			BankPreviewItem item = context.entryByItemId.get(itemId).getItem();
			if (!"unknown".equalsIgnoreCase(item.getSubcategory()))
			{
				subcategories.add(item.getSubcategory());
			}
		}
		return subcategories;
	}

	private static int spilloverCompatibilityCost(LayoutCandidateGroup blockGroup,
		int spilloverItemId, Set<String> typedBlockSubcategories, Context context)
	{
		String ownerRuleKey = context.ownerRuleKeyByItemId.get(spilloverItemId);
		if (blockGroup.getRuleKey().equals(ownerRuleKey)
			|| (ownerRuleKey != null
				&& blockGroup.getRule().getSpilloverCompatibleRuleKeys().contains(ownerRuleKey)))
		{
			return 0;
		}

		String spilloverSubcategory = context.entryByItemId.get(spilloverItemId)
			.getItem().getSubcategory();
		return !"unknown".equalsIgnoreCase(spilloverSubcategory)
			&& typedBlockSubcategories.contains(spilloverSubcategory) ? 1 : 4;
	}

	private static int[] movement(int[] finalItemIds, LayoutRequest request)
	{
		if (!request.hasCurrentDenseCategoryOrder())
		{
			return new int[]{0, 0};
		}

		Map<Integer, Integer> targetByItemId = new HashMap<>();
		for (int target = 0; target < finalItemIds.length; target++)
		{
			targetByItemId.put(finalItemIds[target], target);
		}
		int[] permutation = new int[finalItemIds.length];
		long displacement = 0;
		for (int current = 0; current < permutation.length; current++)
		{
			permutation[current] = targetByItemId.get(request.getCurrentDenseCategoryOrder().get(current));
			displacement += Math.abs((long) current - permutation[current]);
		}

		boolean[] visited = new boolean[permutation.length];
		int cycles = 0;
		for (int index = 0; index < permutation.length; index++)
		{
			if (visited[index])
			{
				continue;
			}
			cycles++;
			int cursor = index;
			while (!visited[cursor])
			{
				visited[cursor] = true;
				cursor = permutation[cursor];
			}
		}
		return new int[]{permutation.length - cycles, Math.toIntExact(displacement)};
	}

	private static int compareStates(State left, State right)
	{
		int result = left.partialScore.compareTo(right.partialScore);
		if (result != 0)
		{
			return result;
		}
		result = compareBlockVectors(left.canonicalBlocks, right.canonicalBlocks);
		if (result != 0)
		{
			return result;
		}
		return compareIntArrays(left.itemIdAtTarget, right.itemIdAtTarget);
	}

	private static int comparePendingChildren(PendingChild left, PendingChild right)
	{
		for (int component = 0; component < LayoutScore.COMPONENT_COUNT; component++)
		{
			int result = Integer.compare(left.partialComponent(component),
				right.partialComponent(component));
			if (result != 0)
			{
				return result;
			}
		}

		int result = compareProjectedBlockVectors(left, right);
		if (result != 0)
		{
			return result;
		}
		int shared = Math.min(left.itemVectorSize(), right.itemVectorSize());
		for (int target = 0; target < shared; target++)
		{
			result = Integer.compare(left.itemAtTarget(target), right.itemAtTarget(target));
			if (result != 0)
			{
				return result;
			}
		}
		return Integer.compare(left.itemVectorSize(), right.itemVectorSize());
	}

	private static int compareBlockVectors(List<PlacedBlock> left, List<PlacedBlock> right)
	{
		int result = compareAlignedBlocks(left, right, BoundedLayoutPacker::compareBlockIdentity);
		if (result != 0) return result;
		result = compareAlignedBlocks(left, right,
			(a, b) -> Integer.compare(a.getWidthPreferenceRank(), b.getWidthPreferenceRank()));
		if (result != 0) return result;
		result = compareAlignedBlocks(left, right, (a, b) -> Integer.compare(a.getWidth(), b.getWidth()));
		if (result != 0) return result;
		result = compareAlignedBlocks(left, right,
			(a, b) -> Integer.compare(a.getShapePrimitive().ordinal(), b.getShapePrimitive().ordinal()));
		if (result != 0) return result;
		result = compareAlignedBlocks(left, right,
			(a, b) -> Integer.compare(a.getStartRow(), b.getStartRow()));
		if (result != 0) return result;
		result = compareAlignedBlocks(left, right,
			(a, b) -> Integer.compare(a.getStartColumn(), b.getStartColumn()));
		if (result != 0) return result;
		return compareAlignedBlocks(left, right,
			(a, b) -> compareRows(a.getRows(), b.getRows()));
	}

	private static int compareProjectedBlockVectors(PendingChild left, PendingChild right)
	{
		int result = compareAlignedProjectedBlocks(left, right,
			BoundedLayoutPacker::compareBlockIdentity);
		if (result != 0) return result;
		result = compareAlignedProjectedBlocks(left, right,
			(a, b) -> Integer.compare(a.getWidthPreferenceRank(), b.getWidthPreferenceRank()));
		if (result != 0) return result;
		result = compareAlignedProjectedBlocks(left, right,
			(a, b) -> Integer.compare(a.getWidth(), b.getWidth()));
		if (result != 0) return result;
		result = compareAlignedProjectedBlocks(left, right,
			(a, b) -> Integer.compare(a.getShapePrimitive().ordinal(), b.getShapePrimitive().ordinal()));
		if (result != 0) return result;
		result = compareAlignedProjectedBlocks(left, right,
			(a, b) -> Integer.compare(a.getStartRow(), b.getStartRow()));
		if (result != 0) return result;
		result = compareAlignedProjectedBlocks(left, right,
			(a, b) -> Integer.compare(a.getStartColumn(), b.getStartColumn()));
		if (result != 0) return result;
		return compareAlignedProjectedBlocks(left, right,
			(a, b) -> compareRows(a.getRows(), b.getRows()));
	}

	private static int compareBlockIdentity(PlacedBlock left, PlacedBlock right)
	{
		int result = left.getRuleKey().compareTo(right.getRuleKey());
		return result != 0 ? result : compareStringVectors(left.getAtomKeys(), right.getAtomKeys());
	}

	private static int compareAlignedBlocks(List<PlacedBlock> left, List<PlacedBlock> right,
		Comparator<PlacedBlock> comparator)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = comparator.compare(left.get(index), right.get(index));
			if (result != 0)
			{
				return result;
			}
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareAlignedProjectedBlocks(PendingChild left, PendingChild right,
		Comparator<PlacedBlock> comparator)
	{
		int shared = Math.min(left.blockCount(), right.blockCount());
		for (int index = 0; index < shared; index++)
		{
			int result = comparator.compare(left.blockAt(index), right.blockAt(index));
			if (result != 0)
			{
				return result;
			}
		}
		return Integer.compare(left.blockCount(), right.blockCount());
	}

	private static int compareRows(List<LayoutCandidate.Row> left, List<LayoutCandidate.Row> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index).getStartOffset(), right.get(index).getStartOffset());
			if (result != 0) return result;
			result = compareIntegerVectors(left.get(index).getItemIds(), right.get(index).getItemIds());
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareStringVectors(List<String> left, List<String> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = left.get(index).compareTo(right.get(index));
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareIntegerVectors(List<Integer> left, List<Integer> right)
	{
		int shared = Math.min(left.size(), right.size());
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left.get(index), right.get(index));
			if (result != 0) return result;
		}
		return Integer.compare(left.size(), right.size());
	}

	private static int compareIntArrays(int[] left, int[] right)
	{
		int shared = Math.min(left.length, right.length);
		for (int index = 0; index < shared; index++)
		{
			int result = Integer.compare(left[index], right[index]);
			if (result != 0) return result;
		}
		return Integer.compare(left.length, right.length);
	}

	static final class Limits
	{
		private final int beamWidth;
		private final int candidateOriginCap;

		Limits(int beamWidth, int candidateOriginCap)
		{
			if (beamWidth <= 0)
			{
				throw new IllegalArgumentException("beamWidth must be positive");
			}
			if (candidateOriginCap < 0)
			{
				throw new IllegalArgumentException("candidateOriginCap must not be negative");
			}
			this.beamWidth = beamWidth;
			this.candidateOriginCap = candidateOriginCap;
		}

		static Limits production()
		{
			return new Limits(PRODUCTION_BEAM_WIDTH, PRODUCTION_CANDIDATE_ORIGIN_CAP);
		}
	}

	static final class SearchStats
	{
		private final int candidateOriginEvaluations;
		private final boolean capReached;
		private final int maximumBeamSize;
		private final int materializedPlacementStates;

		private SearchStats(int candidateOriginEvaluations, boolean capReached, int maximumBeamSize,
			int materializedPlacementStates)
		{
			this.candidateOriginEvaluations = candidateOriginEvaluations;
			this.capReached = capReached;
			this.maximumBeamSize = maximumBeamSize;
			this.materializedPlacementStates = materializedPlacementStates;
		}

		int getCandidateOriginEvaluations()
		{
			return candidateOriginEvaluations;
		}

		boolean isCapReached()
		{
			return capReached;
		}

		int getMaximumBeamSize()
		{
			return maximumBeamSize;
		}

		int getMaterializedPlacementStates()
		{
			return materializedPlacementStates;
		}
	}

	static final class Outcome
	{
		private final LayoutResult result;
		private final LayoutScore score;
		private final DeterministicTieKey tieKey;
		private final SearchStats stats;

		private Outcome(LayoutResult result, LayoutScore score, DeterministicTieKey tieKey,
			SearchStats stats)
		{
			this.result = Objects.requireNonNull(result, "result");
			this.score = score;
			this.tieKey = tieKey;
			this.stats = stats;
		}

		static Outcome conflict(LayoutResult result)
		{
			return new Outcome(result, null, null, new SearchStats(0, false, 0, 0));
		}

		static Outcome success(LayoutResult result, LayoutScore score, DeterministicTieKey tieKey,
			SearchStats stats)
		{
			return new Outcome(result, Objects.requireNonNull(score, "score"),
				Objects.requireNonNull(tieKey, "tieKey"), Objects.requireNonNull(stats, "stats"));
		}

		LayoutResult getResult()
		{
			return result;
		}

		LayoutScore getScore()
		{
			if (!result.isSuccess()) throw new IllegalStateException("conflicted outcome has no score");
			return score;
		}

		DeterministicTieKey getTieKey()
		{
			if (!result.isSuccess()) throw new IllegalStateException("conflicted outcome has no tie key");
			return tieKey;
		}

		SearchStats getStats()
		{
			return stats;
		}
	}

	private static final class Context
	{
		private final LayoutRequest request;
		private final int size;
		private final int gridStartColumn;
		private final int physicalEndExclusive;
		private final int rowCount;
		private final List<Integer> stableFallbackItemIds;
		private final Map<Integer, LayoutEntry> entryByItemId = new HashMap<>();
		private final int[] lockedItemAtTarget;
		private final Map<Integer, Integer> lockedTargetByItemId = new HashMap<>();
		private final Map<Integer, String> ownerRuleKeyByItemId = new HashMap<>();

		private Context(LayoutRequest request, List<Integer> stableFallbackItemIds)
		{
			this.request = request;
			this.size = request.size();
			this.gridStartColumn = request.getGridStartColumn();
			this.physicalEndExclusive = Math.addExact(gridStartColumn, size);
			this.rowCount = size == 0 ? 0
				: Math.toIntExact((physicalEndExclusive + 7L) / GRID_COLUMNS);
			this.stableFallbackItemIds = Collections.unmodifiableList(
				new ArrayList<>(stableFallbackItemIds));
			this.lockedItemAtTarget = new int[size];
			for (LayoutEntry entry : request.getEntries())
			{
				int itemId = entry.getItem().getItemId();
				entryByItemId.put(itemId, entry);
				if (entry.hasLockedTarget())
				{
					lockedItemAtTarget[entry.getLockedTarget()] = itemId;
					lockedTargetByItemId.put(itemId, entry.getLockedTarget());
				}
			}
			for (SemanticRule rule : request.getRules())
			{
				for (Integer itemId : rule.getMemberItemIds())
				{
					ownerRuleKeyByItemId.put(itemId, rule.getRuleKey());
				}
			}
		}

		private int physicalTarget(int localTarget)
		{
			if (localTarget < 0 || localTarget >= size)
			{
				throw new IllegalArgumentException("local target outside request: " + localTarget);
			}
			return gridStartColumn + localTarget;
		}

		private int physicalTarget(int row, int column)
		{
			return Math.addExact(Math.multiplyExact(row, GRID_COLUMNS), column);
		}

		private int localTarget(int physicalTarget)
		{
			if (!containsPhysicalTarget(physicalTarget))
			{
				throw new IllegalArgumentException("physical target outside request window: "
					+ physicalTarget);
			}
			return physicalTarget - gridStartColumn;
		}

		private boolean containsPhysicalTarget(long physicalTarget)
		{
			return physicalTarget >= gridStartColumn && physicalTarget < physicalEndExclusive;
		}
	}

	private static final class State
	{
		private final int[] unavailableOrOccupiedMasks;
		private final int[] itemIdAtTarget;
		private final List<Decision> decisions;
		private final List<PlacedBlock> canonicalBlocks;
		private final PartialScore partialScore;

		private State(int[] unavailableOrOccupiedMasks, int[] itemIdAtTarget,
			List<Decision> decisions, List<PlacedBlock> canonicalBlocks, PartialScore partialScore)
		{
			this.unavailableOrOccupiedMasks = unavailableOrOccupiedMasks;
			this.itemIdAtTarget = itemIdAtTarget;
			this.decisions = decisions;
			this.canonicalBlocks = canonicalBlocks;
			this.partialScore = partialScore;
		}

		private static State initial(Context context)
		{
			int[] masks = new int[context.rowCount];
			if (context.size != 0)
			{
				masks[0] |= (1 << context.gridStartColumn) - 1;
				int remainder = context.physicalEndExclusive % GRID_COLUMNS;
				if (remainder != 0)
				{
					masks[masks.length - 1] |= (~((1 << remainder) - 1)) & 0xff;
				}
			}
			return new State(masks, new int[context.size], Collections.emptyList(),
				Collections.emptyList(), PartialScore.zero());
		}

		private State withFallback(LayoutCandidateGroup group)
		{
			List<Decision> nextDecisions = new ArrayList<>(decisions);
			nextDecisions.add(Decision.fallback(group));
			return new State(unavailableOrOccupiedMasks, itemIdAtTarget,
				Collections.unmodifiableList(nextDecisions), canonicalBlocks,
				partialScore.plus(LayoutCandidateScorer.scoreFallback(group)));
		}

		private State withPlacement(LayoutCandidateGroup group, CandidateOption option,
			Origin origin, PlacedBlock placedBlock, int gridStartColumn)
		{
			int[] nextMasks = Arrays.copyOf(unavailableOrOccupiedMasks,
				unavailableOrOccupiedMasks.length);
			int[] nextItems = Arrays.copyOf(itemIdAtTarget, itemIdAtTarget.length);
			for (LocalCell cell : option.cells)
			{
				int physicalTarget = (origin.baseRow + cell.localRow) * GRID_COLUMNS
					+ origin.startColumn + cell.localColumn;
				int row = physicalTarget / GRID_COLUMNS;
				int column = physicalTarget % GRID_COLUMNS;
				nextMasks[row] |= 1 << column;
				nextItems[physicalTarget - gridStartColumn] = cell.itemId;
			}

			List<Decision> nextDecisions = new ArrayList<>(decisions);
			nextDecisions.add(Decision.placed(group, option.candidate, origin));
			List<PlacedBlock> nextBlocks = new ArrayList<>(canonicalBlocks);
			int insertionIndex = 0;
			while (insertionIndex < nextBlocks.size()
				&& compareBlockIdentity(nextBlocks.get(insertionIndex), placedBlock) < 0)
			{
				insertionIndex++;
			}
			nextBlocks.add(insertionIndex, placedBlock);
			return new State(nextMasks, nextItems, Collections.unmodifiableList(nextDecisions),
				Collections.unmodifiableList(nextBlocks), partialScore.plus(option.localScore));
		}
	}

	/**
	 * One lightweight expansion descriptor. Full occupancy and item arrays are copied only after
	 * this child survives the top-K collector.
	 */
	private static final class PendingChild
	{
		private final State parent;
		private final LayoutCandidateGroup group;
		private final CandidateOption option;
		private final Origin origin;
		private final LayoutScore scoreDelta;
		private final PlacedBlock addedBlock;
		private final int blockInsertionIndex;
		private final int gridStartColumn;

		private PendingChild(State parent, LayoutCandidateGroup group, CandidateOption option,
			Origin origin, LayoutScore scoreDelta, PlacedBlock addedBlock,
			int blockInsertionIndex, int gridStartColumn)
		{
			this.parent = parent;
			this.group = group;
			this.option = option;
			this.origin = origin;
			this.scoreDelta = scoreDelta;
			this.addedBlock = addedBlock;
			this.blockInsertionIndex = blockInsertionIndex;
			this.gridStartColumn = gridStartColumn;
		}

		private static PendingChild fallback(State parent, LayoutCandidateGroup group, Context context)
		{
			return new PendingChild(parent, group, null, null,
				LayoutCandidateScorer.scoreFallback(group), null, -1, context.gridStartColumn);
		}

		private static PendingChild placed(State parent, LayoutCandidateGroup group,
			CandidateOption option, Origin origin, Context context)
		{
			PlacedBlock block = PlacedBlock.place(group, option.candidate,
				origin.baseRow, origin.startColumn);
			int insertionIndex = 0;
			while (insertionIndex < parent.canonicalBlocks.size()
				&& compareBlockIdentity(parent.canonicalBlocks.get(insertionIndex), block) < 0)
			{
				insertionIndex++;
			}
			if (insertionIndex < parent.canonicalBlocks.size()
				&& compareBlockIdentity(parent.canonicalBlocks.get(insertionIndex), block) == 0)
			{
				throw new IllegalStateException("duplicate placed-block identity during expansion");
			}
			return new PendingChild(parent, group, option, origin, option.localScore,
				block, insertionIndex, context.gridStartColumn);
		}

		private boolean isFallback()
		{
			return option == null;
		}

		private int partialComponent(int component)
		{
			return Math.addExact(parent.partialScore.componentAt(component),
				scoreDelta.componentAt(component));
		}

		private int blockCount()
		{
			return parent.canonicalBlocks.size() + (isFallback() ? 0 : 1);
		}

		private PlacedBlock blockAt(int index)
		{
			if (isFallback())
			{
				return parent.canonicalBlocks.get(index);
			}
			if (index == blockInsertionIndex)
			{
				return addedBlock;
			}
			return parent.canonicalBlocks.get(index > blockInsertionIndex ? index - 1 : index);
		}

		private int itemVectorSize()
		{
			return parent.itemIdAtTarget.length;
		}

		private int itemAtTarget(int target)
		{
			if (!isFallback())
			{
				for (LocalCell cell : option.cells)
				{
					int candidateTarget = (origin.baseRow + cell.localRow) * GRID_COLUMNS
						+ origin.startColumn + cell.localColumn;
					if (candidateTarget - gridStartColumn == target)
					{
						return cell.itemId;
					}
				}
			}
			return parent.itemIdAtTarget[target];
		}

		private State materialize()
		{
			return isFallback()
				? parent.withFallback(group)
				: parent.withPlacement(group, option, origin, addedBlock, gridStartColumn);
		}
	}

	private static final class BestChildCollector
	{
		private final int maximumSize;
		private final PriorityQueue<PendingChild> worstFirst;

		private BestChildCollector(int maximumSize)
		{
			this.maximumSize = maximumSize;
			this.worstFirst = new PriorityQueue<>(maximumSize + 1,
				(left, right) -> comparePendingChildren(right, left));
		}

		private void add(PendingChild child)
		{
			worstFirst.add(child);
			if (worstFirst.size() > maximumSize)
			{
				worstFirst.poll();
			}
		}

		private MaterializedChildren toSortedStates()
		{
			List<PendingChild> retained = new ArrayList<>(worstFirst);
			retained.sort(BoundedLayoutPacker::comparePendingChildren);
			List<State> states = new ArrayList<>(retained.size());
			int placementCount = 0;
			for (PendingChild child : retained)
			{
				states.add(child.materialize());
				if (!child.isFallback())
				{
					placementCount++;
				}
			}
			states.sort(BoundedLayoutPacker::compareStates);
			return new MaterializedChildren(states, placementCount);
		}

		private static final class MaterializedChildren
		{
			private final List<State> states;
			private final int placementCount;

			private MaterializedChildren(List<State> states, int placementCount)
			{
				this.states = states;
				this.placementCount = placementCount;
			}
		}
	}

	private static final class CandidateOption
	{
		private final LayoutCandidate candidate;
		private final List<LocalCell> cells;
		private final List<Origin> origins;
		private final LayoutScore localScore;
		private final int preferenceRank;

		private CandidateOption(LayoutCandidateGroup group, LayoutCandidate candidate, Context context)
		{
			this.candidate = candidate;
			this.cells = localCells(candidate);
			this.origins = origins(candidate, cells, context);
			this.localScore = LayoutCandidateScorer.score(group, candidate);
			this.preferenceRank = LayoutCandidateScorer.widthPreferenceRank(group, candidate);
		}

		private static List<LocalCell> localCells(LayoutCandidate candidate)
		{
			List<LocalCell> cells = new ArrayList<>();
			for (int rowIndex = 0; rowIndex < candidate.getRows().size(); rowIndex++)
			{
				LayoutCandidate.Row row = candidate.getRows().get(rowIndex);
				for (int memberIndex = 0; memberIndex < row.length(); memberIndex++)
				{
					cells.add(new LocalCell(rowIndex, row.getStartOffset() + memberIndex,
						row.getItemIds().get(memberIndex)));
				}
			}
			return Collections.unmodifiableList(cells);
		}

		private static List<Origin> origins(LayoutCandidate candidate, List<LocalCell> cells,
			Context context)
		{
			Origin forced = null;
			boolean hasLockedMember = false;
			for (LocalCell cell : cells)
			{
				Integer lockedTarget = context.lockedTargetByItemId.get(cell.itemId);
				if (lockedTarget == null)
				{
					continue;
				}
				hasLockedMember = true;
				int physicalLockedTarget = context.physicalTarget(lockedTarget);
				Origin implied = new Origin(physicalLockedTarget / GRID_COLUMNS - cell.localRow,
					physicalLockedTarget % GRID_COLUMNS - cell.localColumn);
				if (forced != null && !forced.equals(implied))
				{
					return Collections.emptyList();
				}
				forced = implied;
			}

			if (hasLockedMember)
			{
				return originWithinGrid(candidate, forced, context)
					? Collections.singletonList(forced) : Collections.emptyList();
			}

			List<Origin> result = new ArrayList<>();
			int maximumBaseRow = context.rowCount - candidate.getRows().size();
			int maximumStartColumn = GRID_COLUMNS - candidate.getWidth();
			for (int baseRow = 0; baseRow <= maximumBaseRow; baseRow++)
			{
				for (int startColumn = 0; startColumn <= maximumStartColumn; startColumn++)
				{
					result.add(new Origin(baseRow, startColumn));
				}
			}
			return Collections.unmodifiableList(result);
		}

		private static boolean originWithinGrid(LayoutCandidate candidate, Origin origin,
			Context context)
		{
			return nominalRectangleFits(candidate, origin, context);
		}
	}

	private static final class LocalCell
	{
		private final int localRow;
		private final int localColumn;
		private final int itemId;

		private LocalCell(int localRow, int localColumn, int itemId)
		{
			this.localRow = localRow;
			this.localColumn = localColumn;
			this.itemId = itemId;
		}
	}

	private static final class Origin
	{
		private final int baseRow;
		private final int startColumn;

		private Origin(int baseRow, int startColumn)
		{
			this.baseRow = baseRow;
			this.startColumn = startColumn;
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other) return true;
			if (!(other instanceof Origin)) return false;
			Origin origin = (Origin) other;
			return baseRow == origin.baseRow && startColumn == origin.startColumn;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(baseRow, startColumn);
		}
	}

	private static final class Decision
	{
		private final LayoutCandidateGroup group;
		private final LayoutCandidate candidate;
		private final int baseRow;
		private final int startColumn;

		private Decision(LayoutCandidateGroup group, LayoutCandidate candidate,
			int baseRow, int startColumn)
		{
			this.group = group;
			this.candidate = candidate;
			this.baseRow = baseRow;
			this.startColumn = startColumn;
		}

		private static Decision fallback(LayoutCandidateGroup group)
		{
			return new Decision(group, null, -1, -1);
		}

		private static Decision placed(LayoutCandidateGroup group, LayoutCandidate candidate,
			Origin origin)
		{
			return new Decision(group, candidate, origin.baseRow, origin.startColumn);
		}

		private boolean isFallback()
		{
			return candidate == null;
		}
	}

	private static final class PartialScore implements Comparable<PartialScore>
	{
		private final int[] components;

		private PartialScore(int[] components)
		{
			this.components = components;
		}

		private static PartialScore zero()
		{
			return new PartialScore(new int[LayoutScore.COMPONENT_COUNT]);
		}

		private PartialScore plus(LayoutScore score)
		{
			int[] next = Arrays.copyOf(components, components.length);
			int[] added = score.toComponentArray();
			for (int index = 0; index < next.length; index++)
			{
				next[index] = Math.addExact(next[index], added[index]);
			}
			return new PartialScore(next);
		}

		private int componentAt(int index)
		{
			return components[index];
		}

		@Override
		public int compareTo(PartialScore other)
		{
			return compareIntArrays(components, other.components);
		}
	}

	private static final class ScoreAccumulator
	{
		private final int[] components = new int[LayoutScore.COMPONENT_COUNT];

		private void add(LayoutScore score)
		{
			int[] added = score.toComponentArray();
			for (int index = 0; index < components.length; index++)
			{
				components[index] = Math.addExact(components[index], added[index]);
			}
		}

		private void add(int component, int value)
		{
			components[component] = Math.addExact(components[component], value);
		}

		private LayoutScore build(LayoutRequest request, int swapLowerBound, int rankDisplacement)
		{
			return LayoutScore.builder()
				.highMissedRelations(components[0])
				.highMissedCompleteness(components[1])
				.mediumMissedRelations(components[2])
				.mediumMissedCompleteness(components[3])
				.lowMissedRelations(components[4])
				.lowMissedCompleteness(components[5])
				.orientationEvidenceRegret(components[6])
				.widthEvidenceRegret(components[7])
				.semanticFragmentation(components[8])
				.semanticRowBreaks(components[9])
				.startColumnDeviation(components[10])
				.lockedPrefixSpillover(components[11])
				.spilloverCompatibilityCost(components[12])
				.spilloverTransitions(components[13])
				.nominalFootprintSlack(components[14])
				.semanticSpan(components[15])
				.movement(request, swapLowerBound, rankDisplacement)
				.build();
		}
	}

	private static final class CompletePlan
	{
		private final LayoutResult result;
		private final LayoutScore score;
		private final DeterministicTieKey tieKey;

		private CompletePlan(LayoutResult result, LayoutScore score, DeterministicTieKey tieKey)
		{
			this.result = result;
			this.score = score;
			this.tieKey = tieKey;
		}

		private static CompletePlan success(LayoutResult result, LayoutScore score,
			DeterministicTieKey tieKey)
		{
			return new CompletePlan(result, score, tieKey);
		}

		private static CompletePlan invalid(LayoutResult result)
		{
			return new CompletePlan(result, null, null);
		}
	}
}

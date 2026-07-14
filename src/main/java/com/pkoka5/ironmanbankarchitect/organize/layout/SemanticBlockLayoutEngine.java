package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Pure public facade for planning one category on the fixed eight-column bank grid. The caller
 * supplies the existing deterministic micro-sort order explicitly; source bank slots, request-list
 * order, and a proven current dense order have different meanings and are never substituted for it.
 *
 * <p>The engine only returns manual target guidance. It performs no bank actions and exposes no
 * mouse, keyboard, packet, or game-state mutation path.</p>
 */
public final class SemanticBlockLayoutEngine
{
	public LayoutResult plan(LayoutRequest request, List<Integer> stableFallbackItemIds)
	{
		return planDetailed(request, stableFallbackItemIds,
			BoundedLayoutPacker.Limits.production()).getResult();
	}

	static BoundedLayoutPacker.Outcome planDetailed(LayoutRequest request,
		List<Integer> stableFallbackItemIds, BoundedLayoutPacker.Limits limits)
	{
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(limits, "limits");

		List<LayoutConflict> requestConflicts = LayoutRequestValidator.validate(request);
		if (!requestConflicts.isEmpty())
		{
			return BoundedLayoutPacker.Outcome.conflict(LayoutResult.conflict(requestConflicts));
		}

		List<Integer> fallback = validateFallbackOrder(request, stableFallbackItemIds);
		if (fallback == null)
		{
			LayoutConflict conflict = new LayoutConflict(
				LayoutConflict.Type.FALLBACK_ORDER_NOT_PERMUTATION,
				LayoutConflict.NO_ITEM,
				"stableFallbackItemIds must be an exact unique permutation of all request item IDs");
			return BoundedLayoutPacker.Outcome.conflict(
				LayoutResult.conflict(Collections.singletonList(conflict)));
		}

		return BoundedLayoutPacker.packValidated(request, fallback, limits);
	}

	private static List<Integer> validateFallbackOrder(LayoutRequest request,
		List<Integer> stableFallbackItemIds)
	{
		if (stableFallbackItemIds == null || stableFallbackItemIds.size() != request.size())
		{
			return null;
		}

		Set<Integer> requested = new HashSet<>();
		for (LayoutEntry entry : request.getEntries())
		{
			requested.add(entry.getItem().getItemId());
		}

		Set<Integer> seen = new HashSet<>();
		List<Integer> validated = new ArrayList<>(stableFallbackItemIds.size());
		for (Integer itemId : stableFallbackItemIds)
		{
			if (itemId == null || !requested.contains(itemId) || !seen.add(itemId))
			{
				return null;
			}
			validated.add(itemId);
		}
		return seen.size() == requested.size()
			? Collections.unmodifiableList(validated)
			: null;
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.runelite.api.gameval.ItemID;

/**
 * Validates one manually constructed complete plan against its {@link LayoutRequest}. A valid plan
 * contains exactly the request's item-ID multiset on target indices {@code 0..n-1}, preserves
 * every item's ID, quantity, and real OSRS placeholder state, respects every lock exactly, and
 * contains no null, Bank Filler, blank, or phantom item.
 *
 * <p>The result is immutable and never partial: any conflict yields a {@link LayoutResult} without
 * placements. Successful placements are rebuilt from the request's own immutable
 * {@link BankPreviewItem}s, so caller-supplied plan metadata can never leak into the result.</p>
 *
 * <p>Neither the request nor the plan list is mutated. Placements are aggregated per item ID
 * before validation and the final conflicts are sorted canonically, so the exact same conflict
 * list is produced for any plan list order.</p>
 */
public final class LayoutPlanValidator
{
	private LayoutPlanValidator()
	{
	}

	public static LayoutResult validate(LayoutRequest request, List<LayoutPlacement> plan)
	{
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(plan, "plan");

		List<LayoutConflict> requestConflicts = LayoutRequestValidator.validate(request);
		if (!requestConflicts.isEmpty())
		{
			return LayoutResult.conflict(requestConflicts);
		}

		List<LayoutConflict> conflicts = new ArrayList<>();
		int size = request.size();
		if (plan.size() != size)
		{
			conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_SIZE_MISMATCH, LayoutConflict.NO_ITEM,
				"plan has " + plan.size() + " placements for " + size + " request entries"));
		}

		Map<Integer, LayoutEntry> entriesByItemId = new TreeMap<>();
		for (LayoutEntry entry : request.getEntries())
		{
			entriesByItemId.put(entry.getItem().getItemId(), entry);
		}

		// Canonicalize first: group every valid-ID placement per item ID so that no check depends
		// on which occurrence the plan list happens to present first.
		Map<Integer, List<LayoutPlacement>> placementsByItemId = new TreeMap<>();
		for (LayoutPlacement placement : plan)
		{
			if (placement == null)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.NULL_ENTRY, LayoutConflict.NO_ITEM,
					"plan placements must not contain null"));
				continue;
			}

			BankPreviewItem item = placement.getItem();
			int itemId = item.getItemId();
			if (itemId == ItemID.BANK_FILLER)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.BANK_FILLER_ITEM, itemId,
					"plan must not contain Bank Filler"));
				continue;
			}
			if (item.isBlank())
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.BLANK_ITEM, itemId,
					"plan must not contain blank preview items"));
				continue;
			}
			if (itemId <= 0)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.NON_POSITIVE_ITEM_ID, itemId,
					"plan item ID must be positive, got " + itemId));
				continue;
			}

			placementsByItemId.computeIfAbsent(itemId, key -> new ArrayList<>()).add(placement);
		}

		Map<Integer, Integer> targetsByItemId = new TreeMap<>();
		Map<Integer, List<Integer>> itemIdsByTarget = new TreeMap<>();
		for (Map.Entry<Integer, List<LayoutPlacement>> group : placementsByItemId.entrySet())
		{
			int itemId = group.getKey();
			List<LayoutPlacement> placements = group.getValue();
			if (placements.size() > 1)
			{
				List<Integer> targets = new ArrayList<>();
				for (LayoutPlacement placement : placements)
				{
					targets.add(placement.getTargetIndex());
				}
				Collections.sort(targets);
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_DUPLICATE_ITEM, itemId,
					"plan places item " + itemId + " on targets " + targets));
				continue;
			}

			LayoutPlacement placement = placements.get(0);
			LayoutEntry entry = entriesByItemId.get(itemId);
			if (entry == null)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_PHANTOM_ITEM, itemId,
					"plan places item " + itemId + " that is not in the request"));
				continue;
			}

			BankPreviewItem item = placement.getItem();
			if (item.getQuantity() != entry.getItem().getQuantity())
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_QUANTITY_MISMATCH, itemId,
					"plan quantity " + item.getQuantity() + " differs from request quantity "
						+ entry.getItem().getQuantity()));
			}
			if (item.isPlaceholder() != entry.getItem().isPlaceholder())
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_PLACEHOLDER_MISMATCH, itemId,
					"plan placeholder state differs from request placeholder state"));
			}

			int target = placement.getTargetIndex();
			targetsByItemId.put(itemId, target);
			if (target < 0 || target >= size)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_TARGET_OUT_OF_RANGE, itemId,
					"target " + target + " is outside 0.." + (size - 1)));
			}
			else
			{
				itemIdsByTarget.computeIfAbsent(target, key -> new ArrayList<>()).add(itemId);
			}
		}

		for (Map.Entry<Integer, List<Integer>> target : itemIdsByTarget.entrySet())
		{
			if (target.getValue().size() > 1)
			{
				List<Integer> itemIds = new ArrayList<>(target.getValue());
				Collections.sort(itemIds);
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_DUPLICATE_TARGET, LayoutConflict.NO_ITEM,
					"target " + target.getKey() + " is used by items " + itemIds));
			}
		}

		for (Map.Entry<Integer, LayoutEntry> requested : entriesByItemId.entrySet())
		{
			int itemId = requested.getKey();
			if (!placementsByItemId.containsKey(itemId))
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_MISSING_ITEM, itemId,
					"plan does not place request item " + itemId));
				continue;
			}

			LayoutEntry entry = requested.getValue();
			Integer target = targetsByItemId.get(itemId);
			if (entry.hasLockedTarget() && target != null && target != entry.getLockedTarget())
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.PLAN_LOCK_VIOLATION, itemId,
					"item " + itemId + " is locked to target " + entry.getLockedTarget()
						+ " but placed on " + target));
			}
		}

		if (!conflicts.isEmpty())
		{
			conflicts.sort(LayoutConflict.CANONICAL_ORDER);
			return LayoutResult.conflict(conflicts);
		}

		// Rebuild the result from the request's own immutable items, never from plan metadata.
		List<LayoutPlacement> ordered = new ArrayList<>(size);
		for (Map.Entry<Integer, Integer> target : targetsByItemId.entrySet())
		{
			ordered.add(new LayoutPlacement(entriesByItemId.get(target.getKey()).getItem(), target.getValue()));
		}
		ordered.sort((left, right) -> Integer.compare(left.getTargetIndex(), right.getTargetIndex()));
		return LayoutResult.success(ordered);
	}
}

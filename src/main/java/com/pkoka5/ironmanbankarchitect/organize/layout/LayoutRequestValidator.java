package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.runelite.api.gameval.ItemID;

/**
 * Validates a {@link LayoutRequest} into a list of typed {@link LayoutConflict}s. An empty result
 * means the request is valid.
 *
 * <p>The validator never mutates the request. It aggregates unordered facts (duplicate IDs, lock
 * collisions, rank collisions, rule overlap) before emitting conflicts and sorts the final list by
 * the canonical (type, itemId, detail) order, so the exact same conflict list is produced for any
 * input iteration order that is not semantically meaningful.</p>
 */
public final class LayoutRequestValidator
{
	private LayoutRequestValidator()
	{
	}

	public static List<LayoutConflict> validate(LayoutRequest request)
	{
		Objects.requireNonNull(request, "request");

		List<LayoutConflict> conflicts = new ArrayList<>();
		validateEntries(request, conflicts);
		validateLocks(request, conflicts);
		validateRules(request, conflicts);
		validateDenseCategoryOrder(request, conflicts);
		conflicts.sort(LayoutConflict.CANONICAL_ORDER);
		return Collections.unmodifiableList(conflicts);
	}

	private static void validateEntries(LayoutRequest request, List<LayoutConflict> conflicts)
	{
		Map<Integer, Integer> occurrencesByItemId = new TreeMap<>();
		for (LayoutEntry entry : request.getEntries())
		{
			if (entry == null)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.NULL_ENTRY, LayoutConflict.NO_ITEM,
					"request entries must not contain null"));
				continue;
			}

			BankPreviewItem item = entry.getItem();
			int itemId = item.getItemId();
			if (itemId == ItemID.BANK_FILLER)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.BANK_FILLER_ITEM, itemId,
					"Bank Filler is not a real layout entry"));
			}
			else if (item.isBlank())
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.BLANK_ITEM, itemId,
					"blank preview items are not real layout entries"));
			}
			else if (itemId <= 0)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.NON_POSITIVE_ITEM_ID, itemId,
					"item ID must be positive, got " + itemId));
			}
			else if (item.isPlaceholder() && item.getQuantity() != 0)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.INVALID_PLACEHOLDER_STATE, itemId,
					"a real bank placeholder must have quantity 0, got " + item.getQuantity()));
			}
			else
			{
				occurrencesByItemId.merge(itemId, 1, Integer::sum);
			}
		}

		for (Map.Entry<Integer, Integer> occurrence : occurrencesByItemId.entrySet())
		{
			if (occurrence.getValue() > 1)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.DUPLICATE_ITEM_ID, occurrence.getKey(),
					"item ID " + occurrence.getKey() + " appears " + occurrence.getValue()
						+ " times and cannot be guided safely"));
			}
		}
	}

	private static void validateLocks(LayoutRequest request, List<LayoutConflict> conflicts)
	{
		int size = request.size();
		Map<Integer, List<Integer>> itemIdsByLockedTarget = new TreeMap<>();
		for (LayoutEntry entry : request.getEntries())
		{
			if (entry == null || !entry.hasLockedTarget())
			{
				continue;
			}

			int target = entry.getLockedTarget();
			int itemId = entry.getItem().getItemId();
			if (target < 0 || target >= size)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.LOCK_TARGET_OUT_OF_RANGE, itemId,
					"locked target " + target + " is outside 0.." + (size - 1)));
				continue;
			}

			itemIdsByLockedTarget.computeIfAbsent(target, key -> new ArrayList<>()).add(itemId);
		}

		for (Map.Entry<Integer, List<Integer>> lock : itemIdsByLockedTarget.entrySet())
		{
			if (lock.getValue().size() > 1)
			{
				List<Integer> itemIds = new ArrayList<>(lock.getValue());
				Collections.sort(itemIds);
				conflicts.add(new LayoutConflict(LayoutConflict.Type.DUPLICATE_LOCK_TARGET, LayoutConflict.NO_ITEM,
					"target " + lock.getKey() + " is locked by items " + itemIds));
			}
		}
	}

	private static void validateRules(LayoutRequest request, List<LayoutConflict> conflicts)
	{
		Map<String, Integer> occurrencesByRuleKey = new TreeMap<>();
		Map<Integer, Set<String>> ruleKeysByItemId = new TreeMap<>();
		for (SemanticRule rule : request.getRules())
		{
			occurrencesByRuleKey.merge(rule.getRuleKey(), 1, Integer::sum);
			for (Integer itemId : rule.getMemberItemIds())
			{
				ruleKeysByItemId.computeIfAbsent(itemId, key -> new TreeSet<>()).add(rule.getRuleKey());
			}
		}

		for (Map.Entry<String, Integer> occurrence : occurrencesByRuleKey.entrySet())
		{
			if (occurrence.getValue() > 1)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.DUPLICATE_RULE_KEY, LayoutConflict.NO_ITEM,
					"rule key " + occurrence.getKey() + " appears " + occurrence.getValue() + " times"));
			}
		}

		for (Map.Entry<Integer, Set<String>> selection : ruleKeysByItemId.entrySet())
		{
			if (selection.getValue().size() > 1)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.RULE_ITEM_OVERLAP, selection.getKey(),
					"item " + selection.getKey() + " is selected by rules " + selection.getValue()));
			}
		}
	}

	private static void validateDenseCategoryOrder(LayoutRequest request, List<LayoutConflict> conflicts)
	{
		int size = request.size();
		if (!request.hasCurrentDenseCategoryOrder())
		{
			for (LayoutEntry entry : request.getEntries())
			{
				if (entry != null && entry.hasDenseCategoryRank())
				{
					conflicts.add(new LayoutConflict(LayoutConflict.Type.DENSE_RANK_WITHOUT_ORDER,
						entry.getItem().getItemId(),
						"entry rank " + entry.getDenseCategoryRank()
							+ " requires a complete currentDenseCategoryOrder"));
				}
			}
			return;
		}

		List<Integer> order = request.getCurrentDenseCategoryOrder();
		Set<Integer> requestItemIds = new HashSet<>();
		for (LayoutEntry entry : request.getEntries())
		{
			if (entry != null)
			{
				requestItemIds.add(entry.getItem().getItemId());
			}
		}

		Set<Integer> orderItemIds = new HashSet<>();
		boolean permutation = order.size() == size;
		for (Integer itemId : order)
		{
			if (itemId == null || !orderItemIds.add(itemId) || !requestItemIds.contains(itemId))
			{
				permutation = false;
				break;
			}
		}
		permutation = permutation && orderItemIds.size() == requestItemIds.size();

		if (!permutation)
		{
			conflicts.add(new LayoutConflict(LayoutConflict.Type.DENSE_ORDER_NOT_PERMUTATION, LayoutConflict.NO_ITEM,
				"currentDenseCategoryOrder must be an exact unique permutation of all request item IDs"));
		}

		Map<Integer, List<Integer>> itemIdsByRank = new TreeMap<>();
		for (LayoutEntry entry : request.getEntries())
		{
			if (entry == null || !entry.hasDenseCategoryRank())
			{
				continue;
			}

			int itemId = entry.getItem().getItemId();
			int rank = entry.getDenseCategoryRank();
			if (rank < 0 || rank >= size)
			{
				conflicts.add(new LayoutConflict(LayoutConflict.Type.DENSE_RANK_OUT_OF_RANGE, itemId,
					"entry rank " + rank + " is outside 0.." + (size - 1)));
				continue;
			}

			itemIdsByRank.computeIfAbsent(rank, key -> new ArrayList<>()).add(itemId);
			if (permutation)
			{
				int expectedRank = order.indexOf(itemId);
				if (rank != expectedRank)
				{
					conflicts.add(new LayoutConflict(LayoutConflict.Type.DENSE_ORDER_RANK_MISMATCH, itemId,
						"entry rank " + rank + " does not match order rank " + expectedRank));
				}
			}
		}

		for (Map.Entry<Integer, List<Integer>> rank : itemIdsByRank.entrySet())
		{
			if (rank.getValue().size() > 1)
			{
				List<Integer> itemIds = new ArrayList<>(rank.getValue());
				Collections.sort(itemIds);
				conflicts.add(new LayoutConflict(LayoutConflict.Type.DENSE_RANK_DUPLICATE, LayoutConflict.NO_ITEM,
					"rank " + rank.getKey() + " is claimed by items " + itemIds));
			}
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Packs a category by following the sorter's order. The real bank always
 * compacts, so density is never a choice — the only decision is what happens
 * when a semantic family (a charge set, an outfit) meets the end of a row.
 * Single items are never plucked out of their flow to fix it: the family is
 * the movable thing, sliding behind the whole run of singles that follows it
 * until it finds a gap it fits cleanly, and wrapping in place when none is
 * near.
 *
 * <p>Everything else about the sorter's sequence is preserved. The item a
 * player expects first is first; nothing jumps the queue to make a rectangle
 * work out.</p>
 */
final class OrderPreservingPacker
{
	/** How far ahead a row may borrow singles from, in sequence positions. */
	private static final int NUDGE_WINDOW = 2 * SemanticRule.MAX_WIDTH;
	private static final int GRID_COLUMNS = SemanticRule.MAX_WIDTH;

	private OrderPreservingPacker()
	{
	}

	static LayoutResult pack(LayoutRequest request, List<Integer> fallbackItemIds)
	{
		Map<Integer, BankPreviewItem> itemsById = new HashMap<>();
		for (LayoutEntry entry : request.getEntries())
		{
			itemsById.put(entry.getItem().getItemId(), entry.getItem());
		}

		// An item belongs to the first atom that names it; a family is a run of
		// consecutive fallback items under one atom. A rule can hold many
		// families (every potion's doses under one rule), so the atom is the
		// unit that must stay whole. The sorter already placed family members
		// side by side, so runs are how families arrive here.
		Map<Integer, String> ruleKeyByItemId = new HashMap<>();
		for (SemanticRule rule : request.getRules())
		{
			for (SemanticAtom atom : rule.getAtoms())
			{
				for (Integer itemId : atom.getItemIds())
				{
					ruleKeyByItemId.putIfAbsent(itemId, rule.getRuleKey() + "/" + atom.getAtomKey());
				}
			}
		}
		// Charge and dose families are families even when no rule names them:
		// a run of glories must neither wrap mid-family nor be broken up to
		// serve as a nudge for someone else's row.
		for (Integer itemId : fallbackItemIds)
		{
			ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId)
				.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE
					|| metadata.getVariantKind() == ItemSortMetadata.VariantKind.DOSE)
				.ifPresent(metadata ->
					ruleKeyByItemId.putIfAbsent(itemId, "family:" + metadata.getFamilyKey()));
		}

		List<List<Integer>> blocks = toBlocks(fallbackItemIds, ruleKeyByItemId);

		// Place blocks in order. When a family block would straddle a row edge,
		// borrow the next few singles from the window ahead to finish the row.
		Map<Integer, Boolean> placed = new LinkedHashMap<>();
		List<Integer> laidOut = new ArrayList<>(fallbackItemIds.size());
		int column = Math.floorMod(request.getGridStartColumn(), GRID_COLUMNS);
		for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++)
		{
			List<Integer> block = blocks.get(blockIndex);
			List<Integer> pending = new ArrayList<>();
			for (Integer itemId : block)
			{
				if (!placed.containsKey(itemId))
				{
					pending.add(itemId);
				}
			}
			if (pending.isEmpty())
			{
				continue;
			}

			int gap = (GRID_COLUMNS - column) % GRID_COLUMNS;
			boolean isFamily = pending.size() > 1 && pending.size() <= GRID_COLUMNS;
			boolean wouldStraddle = gap != 0 && pending.size() > gap;
			if (isFamily && wouldStraddle)
			{
				// The family slides behind the run of singles that follows it,
				// as far as the run reaches, to the latest point it still fits
				// a row cleanly. The singles' own order is never disturbed.
				List<Integer> run = consecutiveSingles(blocks, blockIndex + 1, placed);
				for (int deferred = run.size(); deferred > 0; deferred--)
				{
					int columnAfter = (column + deferred) % GRID_COLUMNS;
					int gapAfter = (GRID_COLUMNS - columnAfter) % GRID_COLUMNS;
					if (gapAfter == 0 || gapAfter >= pending.size())
					{
						for (Integer itemId : run.subList(0, deferred))
						{
							placed.put(itemId, Boolean.TRUE);
							laidOut.add(itemId);
						}
						column = columnAfter;
						break;
					}
				}
			}

			for (Integer itemId : pending)
			{
				placed.put(itemId, Boolean.TRUE);
				laidOut.add(itemId);
				column = (column + 1) % GRID_COLUMNS;
			}
		}

		List<LayoutPlacement> placements = new ArrayList<>(laidOut.size());
		for (int index = 0; index < laidOut.size(); index++)
		{
			placements.add(new LayoutPlacement(itemsById.get(laidOut.get(index)), index));
		}
		return LayoutResult.success(placements);
	}

	private static List<List<Integer>> toBlocks(List<Integer> fallbackItemIds,
		Map<Integer, String> ruleKeyByItemId)
	{
		List<List<Integer>> blocks = new ArrayList<>();
		List<Integer> current = new ArrayList<>();
		String currentKey = null;
		for (Integer itemId : fallbackItemIds)
		{
			String key = ruleKeyByItemId.get(itemId);
			boolean continuesRun = !current.isEmpty() && currentKey != null && currentKey.equals(key);
			if (!continuesRun && !current.isEmpty())
			{
				blocks.add(current);
				current = new ArrayList<>();
			}
			if (key == null)
			{
				// A single is its own block, so it can be borrowed as a nudge.
				blocks.add(new ArrayList<>(List.of(itemId)));
				currentKey = null;
				continue;
			}
			current.add(itemId);
			currentKey = key;
		}
		if (!current.isEmpty())
		{
			blocks.add(current);
		}
		return blocks;
	}

	/** The unbroken run of single items directly after a block, within the window. */
	private static List<Integer> consecutiveSingles(List<List<Integer>> blocks, int fromBlock,
		Map<Integer, Boolean> placed)
	{
		List<Integer> run = new ArrayList<>();
		for (int index = fromBlock; index < blocks.size() && run.size() < NUDGE_WINDOW; index++)
		{
			List<Integer> block = blocks.get(index);
			if (block.size() != 1 || placed.containsKey(block.get(0)))
			{
				break;
			}
			run.add(block.get(0));
		}
		return run;
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;

public class RuneSemanticRuleSetTest
{
	@Test
	public void ownedRuneFamiliesFormCanonicalFourWidePhysicalRows()
	{
		List<Integer> fallback = new ArrayList<>(Arrays.asList(
			555, 554, 558, 559, 564, 562, 561, 560, 565, 566, 9075, 4699));
		for (int offset = 0; offset < 16; offset++)
		{
			fallback.add(980000 + offset);
		}
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < fallback.size(); index++)
		{
			entries.add(entry(fallback.get(index), index * 17));
		}

		LayoutRequest request = RuneSemanticRuleSet.forEntries(entries);
		LayoutResult result = new SemanticBlockLayoutEngine().plan(request, fallback);
		assertTrue(result.getConflicts().toString(), result.isSuccess());
		List<Integer> target = targetOrder(result);

		assertEquals(Arrays.asList(555, 554), target.subList(0, 2));
		assertEquals(Arrays.asList(558, 562, 560, 565), target.subList(8, 12));
		assertEquals(Arrays.asList(559, 564, 561), target.subList(16, 19));
		assertEquals(Arrays.asList(9075, 566), target.subList(24, 26));
		assertEquals(new HashSet<>(fallback), new HashSet<>(target));
	}

	@Test
	public void firstPresentCanonicalRuneAnchorsTheMatrixAtTargetZero()
	{
		LayoutRequest request = RuneSemanticRuleSet.forEntries(Arrays.asList(
			entry(555, 80), entry(554, 4), entry(558, 200)));

		LayoutEntry water = request.getEntries().stream()
			.filter(entry -> entry.getItem().getItemId() == 555)
			.findFirst().orElseThrow(AssertionError::new);
		assertTrue(water.hasLockedTarget());
		assertEquals(0, water.getLockedTarget());
	}

	private static LayoutEntry entry(int itemId, int sourceSlot)
	{
		return LayoutEntry.of(new BankPreviewItem(new CatalogItem(itemId, "Item " + itemId,
			ItemCategory.RUNE, "rune", Collections.emptySet(), null), 1), sourceSlot);
	}

	private static List<Integer> targetOrder(LayoutResult result)
	{
		Integer[] target = new Integer[result.getPlacements().size()];
		for (LayoutPlacement placement : result.getPlacements())
		{
			target[placement.getTargetIndex()] = placement.getItem().getItemId();
		}
		return Arrays.asList(target);
	}
}

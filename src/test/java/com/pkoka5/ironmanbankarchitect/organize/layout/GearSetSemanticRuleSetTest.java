package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GearSetSemanticRuleSetTest
{
	@Test
	public void evidenceBackedCombatFamiliesBecomeVerticalColumns()
	{
		List<Integer> proselyte = Arrays.asList(9672, 9674, 9676);
		List<Integer> mixedHide = Arrays.asList(29280, 29283, 29286);
		List<Integer> ids = new ArrayList<>();
		ids.addAll(mixedHide);
		ids.addAll(proselyte);
		for (int index = 0; index < 24; index++)
		{
			ids.add(900000 + index);
		}

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, proselyte);
		assertVertical(result, mixedHide);
	}

	@Test
	public void incompleteBodyAndLegsStillKeepTheirEquipmentOrder()
	{
		List<Integer> ids = new ArrayList<>(Arrays.asList(9674, 9676));
		for (int index = 0; index < 15; index++)
		{
			ids.add(900000 + index);
		}

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(targetFor(result, 9674) + 8, targetFor(result, 9676));
	}

	private static LayoutRequest request(List<Integer> ids)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < ids.size(); index++)
		{
			BankPreviewItem item = new BankPreviewItem(new CatalogItem(ids.get(index),
				"Gear " + ids.get(index), ItemCategory.GEAR, "gear",
				Collections.emptySet(), null), 1);
			entries.add(LayoutEntry.of(item, 100 + index));
		}
		return GearSetSemanticRuleSet.forEntries(entries);
	}

	private static void assertVertical(LayoutResult result, List<Integer> ids)
	{
		int first = targetFor(result, ids.get(0));
		for (int index = 0; index < ids.size(); index++)
		{
			assertEquals(first + index * 8, targetFor(result, ids.get(index)));
		}
	}

	private static int targetFor(LayoutResult result, int itemId)
	{
		for (LayoutPlacement placement : result.getPlacements())
		{
			if (placement.getItem().getItemId() == itemId)
			{
				return placement.getTargetIndex();
			}
		}
		throw new AssertionError("missing itemId " + itemId);
	}
}

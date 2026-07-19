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

public class CosmeticSetSemanticRuleSetTest
{
	@Test
	public void workbookCosmeticAndLeagueSetsBecomeVerticalColumns()
	{
		List<Integer> cow = Arrays.asList(11919, 12956, 12957, 12958, 12959);
		List<Integer> ragingEchoes = Arrays.asList(30404, 30406, 30408, 30410);
		List<Integer> ids = new ArrayList<>();
		ids.addAll(ragingEchoes);
		ids.addAll(cow);
		for (int index = 0; index < 32; index++) ids.add(950000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, cow);
		assertVertical(result, ragingEchoes);
	}

	@Test
	public void singletonCosmeticPieceDoesNotActivateASet()
	{
		List<Integer> input = Arrays.asList(11919, 950001, 950002);
		List<Integer> fallback = Arrays.asList(950002, 11919, 950001);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), fallback);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(fallback, targetOrder(result));
	}

	@Test
	public void navalUniformIncludesTricornShirtAndSlacks()
	{
		List<Integer> blueNaval = Arrays.asList(8959, 8952, 8991);
		List<Integer> input = new ArrayList<>(blueNaval);
		for (int index = 0; index < 18; index++) input.add(951000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, blueNaval);
	}

	@Test
	public void reviewedHamAndSillyJesterSupplementsBecomeVerticalColumns()
	{
		List<Integer> ham = Arrays.asList(4302, 4304, 4306, 4298, 4300, 4308, 4310);
		List<Integer> sillyJester = Arrays.asList(10836, 10837, 10838, 10839);
		List<Integer> input = new ArrayList<>();
		input.addAll(sillyJester);
		input.addAll(ham);
		for (int index = 0; index < 46; index++) input.add(952000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, ham);
		assertVertical(result, sillyJester);
	}

	@Test
	public void blueprintReviewFamiliesBecomeIndependentVerticalColumns()
	{
		List<List<Integer>> families = Arrays.asList(
			Arrays.asList(19687, 19697, 19689, 19693, 19691, 19695),
			Arrays.asList(12810, 12811, 12812),
			Arrays.asList(6070, 6065, 6066, 6067, 6068, 6069),
			Arrays.asList(9945, 9946, 9944),
			Arrays.asList(6750, 6752),
			Arrays.asList(284, 285));
		List<Integer> input = new ArrayList<>();
		for (List<Integer> family : families) input.addAll(family);
		for (int index = 0; index < 42; index++) input.add(953000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		for (List<Integer> family : families) assertVertical(result, family);
	}

	private static LayoutRequest request(List<Integer> ids)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < ids.size(); index++)
		{
			BankPreviewItem item = new BankPreviewItem(new CatalogItem(ids.get(index),
				"Cosmetic " + ids.get(index), ItemCategory.CLUE, "cosmetic",
				Collections.emptySet(), null), 1);
			entries.add(LayoutEntry.of(item, index));
		}
		return CosmeticSetSemanticRuleSet.forEntries(entries);
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
			if (placement.getItem().getItemId() == itemId) return placement.getTargetIndex();
		}
		throw new AssertionError("missing itemId " + itemId);
	}

	private static List<Integer> targetOrder(LayoutResult result)
	{
		Integer[] ids = new Integer[result.getPlacements().size()];
		for (LayoutPlacement placement : result.getPlacements())
		{
			ids[placement.getTargetIndex()] = placement.getItem().getItemId();
		}
		return Arrays.asList(ids);
	}
}

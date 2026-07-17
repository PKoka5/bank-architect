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

public class ToolOutfitSemanticRuleSetTest
{
	@Test
	public void completeOutfitsBecomeSeparateHeadToFeetColumns()
	{
		List<Integer> angler = Arrays.asList(13258, 13259, 13260, 13261);
		List<Integer> lumberjack = Arrays.asList(10941, 10939, 10940, 10933);
		List<Integer> input = new ArrayList<>();
		input.addAll(lumberjack);
		input.addAll(angler);
		input.addAll(Arrays.asList(900001, 900002, 900003, 900004, 900005, 900006,
			900007, 900008, 900009, 900010, 900011, 900012, 900013, 900014, 900015, 900016,
			900017, 900018, 900019, 900020, 900021, 900022, 900023, 900024));

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, angler);
		assertVertical(result, lumberjack);
	}

	@Test
	public void singletonOutfitPieceUsesFallbackWithoutInventingTheRestOfTheSet()
	{
		List<Integer> input = Arrays.asList(24878, 900001, 900002);
		List<Integer> fallback = Arrays.asList(900002, 24878, 900001);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), fallback);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(fallback, targetOrder(result));
	}

	@Test
	public void verticalOutfitsAndHorizontalSkillRunsCoexist()
	{
		List<Integer> angler = Arrays.asList(13258, 13259, 13260, 13261);
		List<Integer> graceful = Arrays.asList(11850, 11852, 11854, 11856, 11858, 11860);
		List<Integer> lumberjack = Arrays.asList(10941, 10939, 10940, 10933);
		List<Integer> prospector = Arrays.asList(12013, 12014, 12015, 12016);
		List<Integer> pyromancer = Arrays.asList(20708, 20704, 20706, 20710);
		List<Integer> rogue = Arrays.asList(5554, 5553, 5555, 5556, 5557);
		List<Integer> mining = Arrays.asList(25539, 11920, 5013, 776, 12019, 24481);
		List<Integer> woodcutting = Arrays.asList(10491, 6739, 6313, 10132, 28136, 28142);
		List<Integer> fishingFirst = Arrays.asList(10129, 11323, 305, 307, 309, 3159, 301, 1585);
		List<Integer> fishingTail = Arrays.asList(303, 25584);
		List<Integer> farming = Arrays.asList(5340, 5325, 7409, 5341, 5343, 22997, 24482);
		List<Integer> monkeyKit = Arrays.asList(4021, 4026, 4031, 4024, 4030);
		List<Integer> underwaterKit = Arrays.asList(7534, 7535);
		List<Integer> vyreNoble = Arrays.asList(24676, 24678, 24680);
		List<Integer> questUtilities = Arrays.asList(6544, 28577, 2958, 22435, 1506, 552, 4567, 10890);
		List<Integer> input = new ArrayList<>();
		input.addAll(farming);
		input.addAll(rogue);
		input.addAll(lumberjack);
		input.addAll(graceful);
		input.addAll(fishingTail);
		input.addAll(mining);
		input.addAll(angler);
		input.addAll(pyromancer);
		input.addAll(woodcutting);
		input.addAll(prospector);
		input.addAll(fishingFirst);
		input.addAll(monkeyKit);
		input.addAll(underwaterKit);
		input.addAll(vyreNoble);
		input.addAll(questUtilities);
		for (int id = 910000; id < 910040; id++) input.add(id);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);
		List<Integer> target = targetOrder(result);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, angler);
		assertVertical(result, graceful);
		assertVertical(result, lumberjack);
		assertVertical(result, prospector);
		assertVertical(result, pyromancer);
		assertVertical(result, rogue);
		assertHorizontal(target, mining);
		assertHorizontal(target, woodcutting);
		assertHorizontal(target, fishingFirst);
		assertHorizontal(target, fishingTail);
		assertHorizontal(target, farming);
		assertEquals(0, targetFor(result, angler.get(0)));
		assertEquals(1, targetFor(result, graceful.get(0)));
		assertEquals(2, targetFor(result, lumberjack.get(0)));
		assertEquals(48, targetFor(result, mining.get(0)));
	}

	@Test
	public void questUtilitiesNeverBecomeSkillRunMembers()
	{
		List<Integer> input = Arrays.asList(7917, 24676, 6786, 11920);
		List<Integer> fallback = Arrays.asList(6786, 7917, 11920, 24676);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), fallback);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(fallback, targetOrder(result));
	}

	@Test
	public void allOwnedRunecraftingFocusItemsStayInOneHorizontalRun()
	{
		List<Integer> runecrafting = Arrays.asList(1438, 1444, 1442, 5529, 1456, 1462, 1458);
		List<Integer> input = Arrays.asList(1458, 5529, 1438, 1462, 1442, 1456, 1444, 900001);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertHorizontal(targetOrder(result), runecrafting);
	}

	@Test
	public void essencePouchesAndBindingNecklaceContinueDirectlyAfterRunecraftingFocus()
	{
		List<Integer> firstRow = Arrays.asList(1438, 1444, 1442, 5529, 1456, 1462, 1458, 5509);
		List<Integer> secondRow = Arrays.asList(5510, 5511, 5512, 5513, 5514, 5515, 26784, 26786);
		List<Integer> input = new ArrayList<>();
		input.addAll(secondRow);
		input.add(5521);
		input.addAll(firstRow);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);
		List<Integer> target = targetOrder(result);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(firstRow, target.subList(0, 8));
		assertEquals(secondRow, target.subList(8, 16));
		assertEquals(Integer.valueOf(5521), target.get(16));
	}

	@Test
	public void soulBearerStaysAdjacentToUtilityContainers()
	{
		List<Integer> containers = Arrays.asList(11941, 13226, 13639, 19634);
		List<Integer> input = Arrays.asList(19634, 13639, 13226, 11941, 900001, 900002,
			900003, 900004);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertHorizontal(targetOrder(result), containers);
	}

	@Test
	public void utilityContainerGroupingDoesNotActivateWithoutIronmanSoulBearer()
	{
		LayoutRequest request = request(Arrays.asList(13639, 11941, 13226));

		assertEquals(0, request.getRules().size());
	}

	@Test
	public void workbookGracefulRecolourUsesItsOwnVerticalColumn()
	{
		List<Integer> arceuus = Arrays.asList(13579, 13581, 13583, 13585, 13587, 13589);
		List<Integer> input = new ArrayList<>(arceuus);
		for (int index = 0; index < 42; index++) input.add(960000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, arceuus);
	}

	@Test
	public void largeIronmanToolTabKeepsRunecraftingAndContainersInReservedRows()
	{
		List<Integer> outfits = Arrays.asList(
			24872, 24876, 24878,
			10941, 10939, 10940, 10933,
			12013, 12014, 12015,
			26850, 26852, 26854, 26856,
			5554, 5553, 5555, 5556, 5557);
		List<Integer> runecrafting = Arrays.asList(
			1438, 1444, 1442, 5529, 1456, 1462, 1458, 1440,
			1446, 1454, 1452, 22118, 26801, 5509, 5510, 5512, 5514, 5521);
		List<Integer> containers = Arrays.asList(
			11941, 24478, 24482, 19634, 12019, 24481, 25584, 28142, 24882);
		List<Integer> input = new ArrayList<>();
		input.addAll(containers);
		input.addAll(runecrafting);
		input.addAll(outfits);
		for (int itemId = 940000; input.size() < 114; itemId++) input.add(itemId);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(input), input);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertReservedRows(result, runecrafting, 5);
		assertReservedRows(result, containers, 8);
	}

	private static LayoutRequest request(List<Integer> ids)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < ids.size(); index++)
		{
			BankPreviewItem item = new BankPreviewItem(new CatalogItem(ids.get(index), "Item " + ids.get(index),
				ItemCategory.TOOL, "skilling-outfit", Collections.emptySet(), null), 1);
			entries.add(LayoutEntry.of(item, 100 + index));
		}
		return ToolOutfitSemanticRuleSet.forEntries(entries);
	}

	private static void assertVertical(LayoutResult result, List<Integer> ids)
	{
		int first = targetFor(result, ids.get(0));
		for (int index = 0; index < ids.size(); index++)
		{
			assertEquals(first + index * 8, targetFor(result, ids.get(index)));
		}
	}

	private static void assertHorizontal(List<Integer> target, List<Integer> ids)
	{
		int first = target.indexOf(ids.get(0));
		assertTrue(first >= 0);
		assertTrue(first % 8 + ids.size() <= 8);
		assertEquals(ids, target.subList(first, first + ids.size()));
	}

	private static void assertReservedRows(LayoutResult result, List<Integer> ids, int startRow)
	{
		for (int index = 0; index < ids.size(); index++)
		{
			assertEquals((startRow + index / 8) * 8 + index % 8,
				targetFor(result, ids.get(index)));
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

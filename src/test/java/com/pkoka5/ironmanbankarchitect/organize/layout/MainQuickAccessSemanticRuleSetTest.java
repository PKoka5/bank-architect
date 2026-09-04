package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainQuickAccessSemanticRuleSetTest
{
	@Test
	public void requestAnchorsCoinsAndCombinesReviewedMainRules()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(995, "Coins", ItemCategory.CURRENCY, "currency"));
		for (Integer id : Arrays.asList(11850, 11854, 11856, 11858, 11860, 11852))
		{
			entries.add(entry(id, "Graceful piece", ItemCategory.TOOL, "skilling-outfit"));
		}
		entries.add(entry(556, "Air rune", ItemCategory.RUNE, "rune"));
		entries.add(entry(557, "Earth rune", ItemCategory.RUNE, "rune"));
		entries.add(entry(555, "Water rune", ItemCategory.RUNE, "rune"));
		entries.add(entry(554, "Fire rune", ItemCategory.RUNE, "rune"));

		LayoutRequest request = MainQuickAccessSemanticRuleSet.forEntries(entries);

		assertTrue(request.getEntries().get(0).hasLockedTarget());
		assertEquals(0, request.getEntries().get(0).getLockedTarget());
		assertEquals(Arrays.asList("main.graceful-column", "runes.four-wide-rows"),
			request.getRules().stream().map(SemanticRule::getRuleKey).collect(Collectors.toList()));
		assertEquals(ShapePrimitive.VERTICAL_RUN,
			request.getRules().get(0).getShapePrimitive());
		assertEquals(ShapePrimitive.ROW_GROUP_MATRIX,
			request.getRules().get(1).getShapePrimitive());
	}

	/**
	 * The column follows the set that reached the tab. A player who files his
	 * old set with the tools and his new one here gets the column on the new
	 * one - the plugin has no opinion about which recolour is the real one.
	 */
	@Test
	public void theGracefulSetOnTheTabTakesTheColumn()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(30045, "Graceful hood", ItemCategory.TOOL, "skilling-outfit"));
		entries.add(entry(30048, "Graceful cape", ItemCategory.TOOL, "skilling-outfit"));
		entries.add(entry(30051, "Graceful top", ItemCategory.TOOL, "skilling-outfit"));
		entries.add(entry(30054, "Graceful legs", ItemCategory.TOOL, "skilling-outfit"));
		entries.add(entry(30057, "Graceful gloves", ItemCategory.TOOL, "skilling-outfit"));
		entries.add(entry(30060, "Graceful boots", ItemCategory.TOOL, "skilling-outfit"));

		SemanticRule rule = MainQuickAccessSemanticRuleSet.forEntries(entries).getRules().get(0);

		assertEquals("main.graceful-column", rule.getRuleKey());
		assertEquals(ShapePrimitive.VERTICAL_RUN, rule.getShapePrimitive());
		// Wear order, cape last, exactly as the base set has always read.
		assertEquals(Arrays.asList(30045, 30051, 30054, 30057, 30060, 30048),
			rule.getAtoms().get(0).getItemIds());
	}

	/** Two sets and no instruction: the more complete one, base breaking a tie. */
	@Test
	public void theBaseSetKeepsTheColumnWhenBothAreWhole()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (Integer id : Arrays.asList(30045, 30048, 30051, 30054, 30057, 30060))
		{
			entries.add(entry(id, gracefulPieceName(id), ItemCategory.TOOL, "skilling-outfit"));
		}
		for (Integer id : Arrays.asList(11850, 11852, 11854, 11856, 11858, 11860))
		{
			entries.add(entry(id, gracefulPieceName(id), ItemCategory.TOOL, "skilling-outfit"));
		}

		SemanticRule rule = MainQuickAccessSemanticRuleSet.forEntries(entries).getRules().get(0);

		assertEquals(Arrays.asList(11850, 11854, 11856, 11858, 11860, 11852),
			rule.getAtoms().get(0).getItemIds());
	}

	/** Every family names its pieces identically, so one lookup covers them all. */
	private static String gracefulPieceName(int itemId)
	{
		switch (itemId)
		{
			case 11850: case 30045: return "Graceful hood";
			case 11852: case 30048: return "Graceful cape";
			case 11854: case 30051: return "Graceful top";
			case 11856: case 30054: return "Graceful legs";
			case 11858: case 30057: return "Graceful gloves";
			case 11860: case 30060: return "Graceful boots";
			default: throw new IllegalArgumentException("not a graceful piece: " + itemId);
		}
	}

	@Test
	public void runeMatrixIsComposedIntoMainWithoutAClueRule()
	{
		List<LayoutEntry> entries = Arrays.asList(
			entry(556, "Air rune", ItemCategory.RUNE, "rune"),
			entry(557, "Earth rune", ItemCategory.RUNE, "rune"),
			entry(555, "Water rune", ItemCategory.RUNE, "rune"),
			entry(554, "Fire rune", ItemCategory.RUNE, "rune"));

		SemanticRule rule = MainQuickAccessSemanticRuleSet.forEntries(entries).getRules().get(0);

		assertEquals("runes.four-wide-rows", rule.getRuleKey());
		assertEquals(Arrays.asList(556, 555, 557, 554),
			rule.getAtoms().get(0).getItemIds());
	}

	@Test
	public void mainRuneMatrixStartsInColumnZeroAndKeepsEveryRuneInsideTheBlock()
	{
		List<Integer> runes = Arrays.asList(556, 555, 557, 554, 558, 562, 560, 565,
			559, 564, 561, 563, 9075, 566, 4699, 21880, 4695, 4696, 4698, 4697, 4694);
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(995, "Coins", ItemCategory.CURRENCY, "currency"));
		for (Integer rune : runes)
		{
			entries.add(entry(rune, "Rune " + rune, ItemCategory.RUNE, "rune"));
		}
		for (int index = 0; index < 32; index++)
		{
			entries.add(entry(900000 + index, "Filler " + index, ItemCategory.TELEPORT, "teleport"));
		}

		LayoutRequest request = MainQuickAccessSemanticRuleSet.forEntries(entries);
		LayoutResult result = new SemanticBlockLayoutEngine().plan(request,
			entries.stream().map(entry -> entry.getItem().getItemId()).collect(Collectors.toList()));

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		for (int row = 0; row < 6; row++)
		{
			int first = targetFor(result, runes.get(row * 4));
			assertEquals("row=" + row + ", target=" + first, 0, first % 8);
			assertEquals("row=" + row + ", target=" + first, 1 + row, first / 8);
			int rowSize = Math.min(4, runes.size() - row * 4);
			for (int column = 0; column < rowSize; column++)
			{
				int target = targetFor(result, runes.get(row * 4 + column));
				assertEquals(1 + row, target / 8);
				assertEquals(column, target % 8);
			}
		}
		assertEquals(runes, runes.stream()
			.sorted((left, right) -> Integer.compare(targetFor(result, left), targetFor(result, right)))
			.collect(Collectors.toList()));
	}

	@Test
	public void houseTabFillsTheRaggedRuneTailInsideTheFourWideBand()
	{
		List<Integer> tail = Arrays.asList(9075, 566, 4699, 8013);
		LayoutResult result = planMainWith(tail);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		for (int column = 0; column < tail.size(); column++)
		{
			int target = targetFor(result, tail.get(column));
			assertEquals(4, target / 8);
			assertEquals(column, target % 8);
		}
	}

	@Test
	public void houseTabMovesToTheNextBandCellAfterExtraOwnedRunes()
	{
		List<Integer> tail = Arrays.asList(9075, 566, 4699, 21880, 4695, 8013);
		LayoutResult result = planMainWith(tail);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		for (int index = 0; index < tail.size(); index++)
		{
			int target = targetFor(result, tail.get(index));
			assertEquals(4 + index / 4, target / 8);
			assertEquals(index % 4, target % 8);
		}
	}

	@Test
	public void quickToolsFormOneCanonicalPhysicalRun()
	{
		assertQuickToolRun(Arrays.asList(6739, 11920, 2347, 1755, 952));
	}

	@Test
	public void missingQuickToolCompressesThePhysicalRun()
	{
		assertQuickToolRun(Arrays.asList(6739, 11920, 1755, 952));
	}

	@Test
	public void runesUseSecondRowWithoutCoinsWhenMainHasEnoughEntries()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(557, "Earth rune", ItemCategory.RUNE, "rune"));
		entries.add(entry(11920, "Dragon pickaxe", ItemCategory.TOOL, "tool"));
		entries.add(entry(2347, "Hammer", ItemCategory.TOOL, "tool"));
		for (int index = 0; index < 12; index++)
		{
			entries.add(entry(940000 + index, "Filler " + index,
				ItemCategory.CURRENCY, "currency"));
		}

		LayoutResult result = new SemanticBlockLayoutEngine().plan(
			MainQuickAccessSemanticRuleSet.forEntries(entries),
			entries.stream().map(entry -> entry.getItem().getItemId()).collect(Collectors.toList()));

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(0, targetFor(result, 11920));
		assertEquals(1, targetFor(result, 2347));
		assertEquals(8, targetFor(result, 557));
	}

	@Test
	public void sparseMainStaysDenseWhenASecondRowCannotBeFormed()
	{
		List<LayoutEntry> entries = Arrays.asList(
			entry(557, "Earth rune", ItemCategory.RUNE, "rune"),
			entry(11920, "Dragon pickaxe", ItemCategory.TOOL, "tool"),
			entry(2347, "Hammer", ItemCategory.TOOL, "tool"));

		LayoutResult result = new SemanticBlockLayoutEngine().plan(
			MainQuickAccessSemanticRuleSet.forEntries(entries),
			entries.stream().map(entry -> entry.getItem().getItemId()).collect(Collectors.toList()));

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(0, targetFor(result, 11920));
		assertEquals(1, targetFor(result, 2347));
		assertEquals(2, targetFor(result, 557));
	}

	private static LayoutResult planMainWith(List<Integer> tail)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(995, "Coins", ItemCategory.CURRENCY, "currency"));
		for (Integer itemId : Arrays.asList(556, 555, 557, 554, 558, 562, 560, 565,
			559, 564, 561, 563))
		{
			entries.add(entry(itemId, "Rune " + itemId, ItemCategory.RUNE, "rune"));
		}
		for (Integer itemId : tail)
		{
			entries.add(entry(itemId, "Tail " + itemId,
				itemId == 8013 ? ItemCategory.TELEPORT : ItemCategory.RUNE,
				itemId == 8013 ? "teleport" : "rune"));
		}
		for (int index = 0; index < 32; index++)
		{
			entries.add(entry(920000 + index, "Filler " + index, ItemCategory.CURRENCY, "currency"));
		}
		return new SemanticBlockLayoutEngine().plan(
			MainQuickAccessSemanticRuleSet.forEntries(entries),
			entries.stream().map(entry -> entry.getItem().getItemId()).collect(Collectors.toList()));
	}

	private static void assertQuickToolRun(List<Integer> tools)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(entry(995, "Coins", ItemCategory.CURRENCY, "currency"));
		for (Integer itemId : tools)
		{
			entries.add(entry(itemId, "Tool " + itemId, ItemCategory.TOOL, "tool"));
		}
		entries.add(entry(930000, "Filler", ItemCategory.CURRENCY, "currency"));

		LayoutRequest request = MainQuickAccessSemanticRuleSet.forEntries(entries);
		LayoutResult result = new SemanticBlockLayoutEngine().plan(request,
			entries.stream().map(entry -> entry.getItem().getItemId()).collect(Collectors.toList()));

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		int first = targetFor(result, tools.get(0));
		assertEquals(1, first);
		assertTrue(first % 8 + tools.size() <= 8);
		for (int index = 0; index < tools.size(); index++)
		{
			assertEquals(first + index, targetFor(result, tools.get(index)));
		}
		assertTrue(request.getRules().stream()
			.anyMatch(rule -> "main.quick-tools".equals(rule.getRuleKey())));
	}

	private static int targetFor(LayoutResult result, int itemId)
	{
		for (LayoutPlacement placement : result.getPlacements())
		{
			if (placement.getItem().getItemId() == itemId) return placement.getTargetIndex();
		}
		throw new AssertionError("missing itemId " + itemId);
	}

	private static LayoutEntry entry(int id, String name, ItemCategory category, String subcategory)
	{
		BankPreviewItem item = new BankPreviewItem(new CatalogItem(id, name, category, subcategory,
			Collections.emptySet(), null), 1);
		return LayoutEntry.of(item, id);
	}
}

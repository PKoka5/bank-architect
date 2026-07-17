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

	@Test
	public void monkRobesAndRepairedDharokBecomeSeparateVerticalColumns()
	{
		List<Integer> monk = Arrays.asList(544, 542);
		List<Integer> dharok = Arrays.asList(4716, 4718, 4720, 4722);
		List<Integer> ids = new ArrayList<>();
		ids.addAll(monk);
		ids.addAll(dharok);
		for (int index = 0; index < 26; index++)
		{
			ids.add(901000 + index);
		}

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, monk);
		assertVertical(result, dharok);
	}

	@Test
	public void mixedBarrowsDegradationStatesStillFormOneEquipmentColumn()
	{
		List<Integer> mixedDharok = Arrays.asList(4881, 4889, 4894, 4901);
		List<Integer> ids = new ArrayList<>(mixedDharok);
		for (int index = 0; index < 28; index++) ids.add(902000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, mixedDharok);
	}

	@Test
	public void activeAndInactiveCrystalPiecesRemainOneSetFamily()
	{
		List<Integer> mixedCrystal = Arrays.asList(23971, 23977, 23979);
		List<Integer> ids = new ArrayList<>(mixedCrystal);
		for (int index = 0; index < 20; index++) ids.add(904000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, mixedCrystal);
	}

	@Test
	public void workbookOnlyJusticiarAndBlueMoonSetsBecomeVerticalColumns()
	{
		List<Integer> justiciar = Arrays.asList(22326, 22327, 22328);
		List<Integer> blueMoon = Arrays.asList(29019, 28988, 29013, 29016);
		List<Integer> ids = new ArrayList<>();
		ids.addAll(blueMoon);
		ids.addAll(justiciar);
		for (int index = 0; index < 28; index++) ids.add(903000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, justiciar);
		assertVertical(result, blueMoon);
	}

	@Test
	public void reviewedLunarEquipmentSupplementBecomesOneVerticalColumn()
	{
		List<Integer> lunar = Arrays.asList(9096, 9101, 9102, 9084, 9097, 9098, 9099, 9100, 9104);
		List<Integer> ids = new ArrayList<>(lunar);
		for (int index = 0; index < 64; index++) ids.add(905000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, lunar);
	}

	@Test
	public void lunarRemainderUsesAdjacentColumnsWhenBisGlovesLeaveASevenRowTail()
	{
		List<Integer> lunarWithoutBisGloves = Arrays.asList(
			9096, 9101, 9102, 9084, 9097, 9098, 9100, 9104);
		List<Integer> ids = new ArrayList<>(lunarWithoutBisGloves);
		for (int index = 0; index < 42; index++) ids.add(907000 + index);

		LayoutRequest request = request(ids);
		LayoutResult result = new SemanticBlockLayoutEngine().plan(
			GearSetSemanticRuleSet.forEntries(request.getEntries(), 7), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		int first = targetFor(result, 9096);
		assertEquals(first + 1, targetFor(result, 9097));
		assertEquals(first + 8, targetFor(result, 9101));
		assertEquals(first + 9, targetFor(result, 9098));
		assertEquals(first + 16, targetFor(result, 9102));
		assertEquals(first + 17, targetFor(result, 9100));
		assertEquals(first + 24, targetFor(result, 9084));
		assertEquals(first + 25, targetFor(result, 9104));
	}

	@Test
	public void ghostlyRobesRemainFunctionalGearInOneVerticalColumn()
	{
		List<Integer> ghostly = Arrays.asList(6109, 6111, 6107, 6108, 6110, 6106);
		List<Integer> ids = new ArrayList<>(ghostly);
		for (int index = 0; index < 42; index++) ids.add(906000 + index);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(request(ids), ids);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertVertical(result, ghostly);
	}

	@Test
	public void onlyOwnedFamiliesWithAtLeastTwoPiecesAreProtectedFromSetupFillers()
	{
		List<BankPreviewItem> items = Arrays.asList(
			item(544), item(542), item(4716), item(999999));

		assertEquals(Arrays.asList(544, 542),
			new ArrayList<>(GearSetSemanticRuleSet.presentFamilyItemIds(items)));
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

	private static BankPreviewItem item(int itemId)
	{
		return new BankPreviewItem(new CatalogItem(itemId, "Gear " + itemId,
			ItemCategory.GEAR, "gear", Collections.emptySet(), null), 1);
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

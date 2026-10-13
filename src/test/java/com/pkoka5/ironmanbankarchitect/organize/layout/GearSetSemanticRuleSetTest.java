package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
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

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, proselyte);
		assertVertical(result, mixedHide);
	}

	@Test
	public void voidPiecesFormTheirOwnColumn()
	{
		List<Integer> voidSet = Arrays.asList(11665, 8839, 8840, 8842);
		List<Integer> ids = new ArrayList<>(voidSet);
		for (int index = 0; index < 24; index++)
		{
			ids.add(900000 + index);
		}

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, voidSet);
	}

	/**
	 * Blood and eclipse moon were known sets but blue moon was not, so the
	 * magic pieces stayed loose while a lower-tier set took the magic column.
	 */
	@Test
	public void blueMoonPiecesFormTheirOwnColumnLikeTheOtherMoonSets()
	{
		List<Integer> blueMoon = Arrays.asList(29019, 29013, 29016);
		List<Integer> ids = new ArrayList<>(blueMoon);
		for (int index = 0; index < 24; index++)
		{
			ids.add(900000 + index);
		}

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, blueMoon);
	}

	@Test
	public void incompleteBodyAndLegsStillKeepTheirEquipmentOrder()
	{
		List<Integer> ids = new ArrayList<>(Arrays.asList(9674, 9676));
		for (int index = 0; index < 15; index++)
		{
			ids.add(900000 + index);
		}

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
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

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, monk);
		assertVertical(result, dharok);
	}

	@Test
	public void mixedBarrowsDegradationStatesStillFormOneEquipmentColumn()
	{
		List<Integer> mixedDharok = Arrays.asList(4881, 4889, 4894, 4901);
		List<Integer> ids = new ArrayList<>(mixedDharok);
		for (int index = 0; index < 28; index++) ids.add(902000 + index);

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, mixedDharok);
	}

	@Test
	public void activeAndInactiveCrystalPiecesRemainOneSetFamily()
	{
		List<Integer> mixedCrystal = Arrays.asList(23971, 23977, 23979);
		List<Integer> ids = new ArrayList<>(mixedCrystal);
		for (int index = 0; index < 20; index++) ids.add(904000 + index);

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
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

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, justiciar);
		assertVertical(result, blueMoon);
	}

	@Test
	public void reviewedLunarEquipmentSupplementBecomesOneVerticalColumn()
	{
		List<Integer> lunar = Arrays.asList(9096, 9101, 9102, 9084, 9097, 9098, 9099, 9100, 9104);
		List<Integer> ids = new ArrayList<>(lunar);
		for (int index = 0; index < 64; index++) ids.add(905000 + index);

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, lunar);
	}

	@Test
	public void lunarRemainderUsesAdjacentColumnsWhenBisGlovesLeaveASevenRowTail()
	{
		List<Integer> lunarWithoutBisGloves = Arrays.asList(
			9096, 9101, 9102, 9084, 9097, 9098, 9100, 9104);
		List<Integer> ids = new ArrayList<>(lunarWithoutBisGloves);
		for (int index = 0; index < 42; index++) ids.add(907000 + index);

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
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

		List<BankPreviewItem> result = pack(ids);

		assertPreserved(ids, result);
		assertVertical(result, ghostly);
	}

	@Test
	public void conditionVariantsUseAdjacentCellsWithinTheirEquipmentSlotRows()
	{
		List<Integer> crystal = Arrays.asList(23971,23973,23975,23977,23979,23981);
		List<Integer> ids = new ArrayList<>(crystal);
		for (int index = 0; index < 12; index++) ids.add(908000 + index);
		List<BankPreviewItem> result = pack(ids);
		assertPreserved(ids, result);
		int first = targetFor(result, 23971);
		assertEquals(first + 1, targetFor(result, 23973));
		assertEquals(first + 8, targetFor(result, 23975));
		assertEquals(first + 9, targetFor(result, 23977));
		assertEquals(first + 16, targetFor(result, 23979));
		assertEquals(first + 17, targetFor(result, 23981));
	}

	@Test
	public void sparseSetUsesCanonicalDenseOrderInsteadOfInventingVerticalGaps()
	{
		List<Integer> ids = Arrays.asList(9672,9674,9676);
		List<BankPreviewItem> result = pack(ids);
		assertPreserved(ids, result);
		assertEquals(ids, result.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
	}

	@Test
	public void alternativeWeaponsStayOneCompactCanonicalRoleBlock()
	{
		List<Integer> ids = Arrays.asList(22978,22324,26219);
		List<BankPreviewItem> result = pack(ids);
		assertPreserved(ids, result);
		assertEquals(Arrays.asList(26219,22324,22978),
			result.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
	}

	@Test
	public void onlyOwnedFamiliesWithAtLeastTwoPiecesAreProtectedFromSetupFillers()
	{
		List<BankPreviewItem> items = Arrays.asList(
			item(544), item(542), item(4716), item(999999));

		assertEquals(Arrays.asList(544, 542),
			new ArrayList<>(GearSetSemanticRuleSet.ownedFamilySizeByItemId(items).keySet()));
	}

	private static final Map<Integer, Integer> SLOTS = new LinkedHashMap<>();
	static
	{
		// Independently specified slots; source IDs are never queried from production families.
		for (int id : new int[] {9672,11665,29019,4716,4881,23971,23973,22326,9096,6109}) SLOTS.put(id, 0);
		for (int id : new int[] {9674,29280,8839,29013,544,4720,4894,23975,23977,22327,9097,6107}) SLOTS.put(id, 1);
		for (int id : new int[] {9676,29283,8840,29016,542,4722,4901,23979,23981,22328,9098,6108}) SLOTS.put(id, 2);
		for (int id : new int[] {9101,6111}) SLOTS.put(id, 3);
		SLOTS.put(9102, 4);
		for (int id : new int[] {8842,9099,6110}) SLOTS.put(id, 6);
		for (int id : new int[] {29286,9100,6106}) SLOTS.put(id, 7);
		for (int id : new int[] {4718,4889,26219,22324,22978}) SLOTS.put(id, 8);
		for (int id : new int[] {28988,9084}) SLOTS.put(id, 10);
		SLOTS.put(9104, 12);
	}

	private static List<BankPreviewItem> pack(List<Integer> ids)
	{
		List<BankPreviewItem> items = ids.stream().map(GearSetSemanticRuleSetTest::item).collect(Collectors.toList());
		return GearSetSemanticRuleSet.packSections(items, 0, id -> SLOTS.getOrDefault(id, 13));
	}

	private static void assertPreserved(List<Integer> expected, List<BankPreviewItem> actual)
	{
		assertEquals("only actual owned items occupy cells", expected.size(), actual.size());
		assertEquals("every source ID survives exactly once", new java.util.HashSet<>(expected),
			actual.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
		assertTrue("no invented bank gap", actual.stream().noneMatch(BankPreviewItem::isBlank));
	}

	private static BankPreviewItem item(int itemId)
	{
		return new BankPreviewItem(new CatalogItem(itemId, "Gear " + itemId,
			ItemCategory.GEAR, "gear", Collections.emptySet(), null), 1);
	}

	private static void assertVertical(List<BankPreviewItem> result, List<Integer> ids)
	{
		int first = targetFor(result, ids.get(0));
		for (int index = 0; index < ids.size(); index++)
		{
			assertEquals(first + index * 8, targetFor(result, ids.get(index)));
		}
	}

	private static int targetFor(List<BankPreviewItem> result, int itemId)
	{
		for (int index = 0; index < result.size(); index++)
			if (result.get(index).getItemId() == itemId) return index;
		throw new AssertionError("missing itemId " + itemId);
	}
}

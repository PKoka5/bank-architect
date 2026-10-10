package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class CombatGearGroupingRegressionTest
{
	private static final int[] OATHPLATE = {30750, 30753, 30756};
	private static final int[] RADIANT_OATHPLATE = {30777, 30779, 30781};
	private static final int[] ARMADYL = {11826, 11828, 11830};
	private static final int[] MYSTIC_DARK = {4099, 4101, 4103, 4105, 4107};
	private static final int[] MYSTIC_LIGHT = {4109, 4111, 4113, 4115, 4117};
	private static final GearSlot[] FAMILY_SLOTS = {
		GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.HANDS, GearSlot.FEET
	};

	@Test
	public void oathplateLegsStayInTheBestOwnedFrontAcrossDefaultsAndPlaceholderStates()
	{
		assertEquals("Rune skirt fixture remains a lower progression stage", 2,
			GearTierCatalog.INSTANCE.tierOf(1093).orElse(-1));
		for (int[] family : Arrays.asList(OATHPLATE, RADIANT_OATHPLATE))
		{
			Map<Integer, GearStats> stats = new LinkedHashMap<>();
			for (int slot = 0; slot < family.length; slot++)
			{
				stats.put(family[slot], styledStats(FAMILY_SLOTS[slot], GearStyle.MELEE, 3));
			}
			stats.put(1093, styledStats(GearSlot.LEGS, GearStyle.MELEE, 1));
			for (boolean useStats : Arrays.asList(false, true))
			{
				GearStatsSource source = useStats ? id -> Optional.ofNullable(stats.get(id)) : GearStatsSource.NONE;
				assertTrue("Oathplate legs outrank the Rune skirt",
					GearItemSorter.score(described(family[2]), source)
						> GearItemSorter.score(described(1093), source));
				List<BankPreviewItem> input = Arrays.asList(described(1093), described(family[2]),
					described(family[0]), described(family[1]));
				assertEquals("sparse front contains the three actual best melee pieces", integers(family),
					ids(GearItemSorter.plan(input, source).getSetupRows()));
				for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
				{
					for (boolean placeholder : Arrays.asList(false, true))
					{
						List<Integer> bankIds = new ArrayList<>(Arrays.asList(1093));
						bankIds.addAll(integers(family));
						BankSnapshot bank = bank(bankIds, placeholder ? integers(family) : Collections.emptyList());
						BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
						BankOrganizationPreview preview = build(bank, preset, source, plan);
						List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
						String context = family[2] + ", " + preset.getType() + ", stats=" + useStats
							+ ", placeholder=" + placeholder;

						assertEquals(context + ", semantic groups cannot repack the best-owned front",
							integers(family), ids(combat.subList(0, family.length)));
						assertEquals(context + ", legs placeholder state", placeholder, combat.get(2).isPlaceholder());
						assertEquals(context + ", legs quantity", placeholder ? 0 : 1, combat.get(2).getQuantity());
						assertPreserved(context, bankIds, preview.getPlannedItems());
					}
				}
			}
		}
	}

	@Test
	public void compactFrontContainsOnlyBestPiecesAndKeepsWholeSecondaryFamiliesInTheTail()
	{
		List<int[]> families = Arrays.asList(RADIANT_OATHPLATE, ARMADYL, MYSTIC_DARK, MYSTIC_LIGHT);
		for (int familyIndex = 0; familyIndex < families.size(); familyIndex++)
		{
			int[] family = families.get(familyIndex);
			GearStyle style = familyIndex == 0 ? GearStyle.MELEE : familyIndex == 1 ? GearStyle.RANGED : GearStyle.MAGIC;
			List<BankPreviewItem> items = new ArrayList<>();
			List<Integer> primaryIds = new ArrayList<>();
			Map<Integer, GearStats> stats = new LinkedHashMap<>();
			for (int slot = 0; slot < family.length; slot++)
			{
				int primaryId = 980000 + slot;
				primaryIds.add(primaryId);
				items.add(new BankPreviewItem(new CatalogItem(primaryId, "Primary " + FAMILY_SLOTS[slot],
					ItemCategory.GEAR, "gear", Collections.emptySet(), null), 1));
				items.add(described(family[slot]));
				stats.put(primaryId, styledStats(FAMILY_SLOTS[slot], style, 400));
				stats.put(family[slot], styledStats(FAMILY_SLOTS[slot], style, 1));
			}
			GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
			GearItemSorter.GearLayout plan = GearItemSorter.plan(items, source);
			String context = "secondary family " + family[0];

			assertTrue(context + ", reviewed members share the grouping dictionary",
				GearSetSemanticRuleSet.presentFamilyItemIds(items).containsAll(integers(family)));
			assertEquals(context + ", front contains only actual best pieces", primaryIds, ids(plan.getSetupRows()));
			assertEquals(context + ", no secondary member becomes filler or a second promoted primary",
				integers(family), ids(plan.getTail()));
			assertPreserved(context, ids(items), GearItemSorter.layout(items, source));
		}
	}

	@Test
	public void defaultGridsKeepPartialMixedBarrowsStatesInVerticalColumnsBesideBestGear()
	{
		List<Integer> primary = Arrays.asList(30753, 30756);
		List<Integer> dharok = Arrays.asList(4894, 4901);
		List<Integer> guthan = Arrays.asList(4918, 4924);
		List<Integer> input = new ArrayList<>(Arrays.asList(4901, 4924, 30756, 4894, 30753, 4918));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int itemId : input)
		{
			GearSlot slot = Arrays.asList(30753, 4894, 4918).contains(itemId) ? GearSlot.BODY : GearSlot.LEGS;
			stats.put(itemId, styledStats(slot, GearStyle.MELEE, primary.contains(itemId) ? 3 : 1));
		}
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		Map<Integer, CatalogItem> fillers = addUnrelatedGear(input, 12);
		ItemCatalog catalog = id -> fillers.containsKey(id)
			? Optional.of(fillers.get(id)) : CompositeItemCatalog.DEFAULT.findById(id);
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			BankSnapshot bank = bank(input, Collections.emptyList());
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
				catalog, preset, source, ItemValueSource.NONE, CategoryOverrideSource.NONE,
				plan, BankLayoutOptions.defaultFor(preset));
			List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
			String context = preset.getType() + ", vertical partial Barrows families";

			assertVerticalFamily(context + ", best body and legs", ids(combat), primary);
			assertEquals(context + ", best body starts the melee column", (int) primary.get(0), combat.get(0).getItemId());
			assertVerticalFamily(context + ", Dharok", ids(combat), dharok);
			assertVerticalFamily(context + ", Guthan", ids(combat), guthan);
			assertPreserved(context, input, preview.getPlannedItems());
		}
	}

	@Test
	public void twelveBestPiecesAndTwoFivePieceMysticFamiliesKeepTheirVerticalColumns()
	{
		List<Integer> primaryIds = new ArrayList<>();
		Map<Integer, CatalogItem> primaryCatalog = new LinkedHashMap<>();
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (GearStyle style : Arrays.asList(GearStyle.MELEE, GearStyle.RANGED, GearStyle.MAGIC, GearStyle.PRAYER))
		{
			int slotCount = style == GearStyle.MAGIC ? 5 : style == GearStyle.PRAYER ? 1 : 3;
			for (int slot = 0; slot < slotCount; slot++)
			{
				int itemId = 981000 + primaryIds.size();
				primaryIds.add(itemId);
				primaryCatalog.put(itemId, new CatalogItem(itemId, "Primary " + style + " " + FAMILY_SLOTS[slot],
					ItemCategory.GEAR, "gear", Collections.emptySet(), null));
				stats.put(itemId, styledStats(FAMILY_SLOTS[slot], style, 400));
			}
		}
		List<Integer> input = new ArrayList<>(primaryIds);
		for (int[] family : Arrays.asList(MYSTIC_DARK, MYSTIC_LIGHT))
		{
			input.addAll(integers(family));
			for (int slot = 0; slot < family.length; slot++)
			{
				stats.put(family[slot], styledStats(FAMILY_SLOTS[slot], GearStyle.MAGIC, 1));
			}
		}
		primaryCatalog.putAll(addUnrelatedGear(input, 16));
		ItemCatalog catalog = id -> primaryCatalog.containsKey(id)
			? Optional.of(primaryCatalog.get(id)) : CompositeItemCatalog.DEFAULT.findById(id);
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		List<BankPreviewItem> items = input.stream()
			.map(id -> new BankPreviewItem(catalog.describeOrUnknown(id), 1)).collect(Collectors.toList());
		GearItemSorter.GearLayout gear = GearItemSorter.plan(items, source);
		assertEquals("fixture has exactly twelve actual best pieces", primaryIds, ids(gear.getSetupRows()));
		assertEquals("both complete secondary families and real loose gear stay in the tail", 26, gear.getTail().size());

		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank(input, Collections.emptyList()),
				catalog, preset, source, ItemValueSource.NONE, CategoryOverrideSource.NONE,
				plan, BankLayoutOptions.defaultFor(preset));
			List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
			String context = preset.getType() + ", twelve best pieces and two Mystic sets";

			assertVerticalFamily(context + ", best melee gear", ids(combat), primaryIds.subList(0, 3));
			assertVerticalFamily(context + ", best ranged gear", ids(combat), primaryIds.subList(3, 6));
			assertVerticalFamily(context + ", best magic gear", ids(combat), primaryIds.subList(6, 11));
			for (int style = 0; style < 4; style++)
			{
				int itemId = primaryIds.get(style == 3 ? 11 : style * 3);
				assertEquals(context + ", best helmet style " + style, itemId, combat.get(style).getItemId());
			}
			assertVerticalFamily(context + ", dark Mystic", ids(combat), integers(MYSTIC_DARK));
			assertVerticalFamily(context + ", light Mystic", ids(combat), integers(MYSTIC_LIGHT));
			assertPreserved(context, input, combat);
			assertPreserved(context + ", full preview", input, preview.getPlannedItems());
		}
	}

	@Test
	public void groupingValidationAcceptsOrderedDenseAndGeometricFamiliesButRejectsSplitMystic()
	{
		List<BankPreviewItem> dense = new ArrayList<>();
		for (int[] family : Arrays.asList(MYSTIC_DARK, MYSTIC_LIGHT))
		{
			for (int itemId : family) dense.add(described(itemId));
		}
		assertTrue("an ordered dense run can cross a physical row boundary",
			GearSetSemanticRuleSet.keepsFamiliesTogether(dense, 12));

		List<BankPreviewItem> vertical = unrelatedGear(33);
		for (int slot = 0; slot < MYSTIC_DARK.length; slot++)
		{
			vertical.set(slot * 8, described(MYSTIC_DARK[slot]));
		}
		assertTrue("an intact vertical column remains a valid grouped family",
			GearSetSemanticRuleSet.keepsFamiliesTogether(vertical, 12));

		int[] lunarWithoutGloves = {9096, 9101, 9102, 9084, 9097, 9098, 9100, 9104};
		int[] rectanglePositions = {0, 8, 16, 24, 1, 9, 17, 25};
		List<BankPreviewItem> rectangle = unrelatedGear(26);
		for (int slot = 0; slot < lunarWithoutGloves.length; slot++)
		{
			rectangle.set(rectanglePositions[slot], described(lunarWithoutGloves[slot]));
		}
		assertTrue("equipment order may form adjacent vertical columns",
			GearSetSemanticRuleSet.keepsFamiliesTogether(rectangle, 0));

		List<BankPreviewItem> splitMystic = Arrays.asList(4109, 4111, 4113, 4115, 4099,
			4101, 4103, 4105, 4107, 4117).stream().map(CombatGearGroupingRegressionTest::described)
			.collect(Collectors.toList());
		assertFalse("four light pieces before dark and the last light piece after dark split the set",
			GearSetSemanticRuleSet.keepsFamiliesTogether(splitMystic, 12));
	}

	@Test
	public void listLayoutGroupsReviewedVariantsAndPartialDegradedFamiliesFromTheSameDictionary()
	{
		List<List<Integer>> families = Arrays.asList(integers(OATHPLATE), integers(RADIANT_OATHPLATE),
			integers(ARMADYL), integers(MYSTIC_DARK), integers(MYSTIC_LIGHT),
			Arrays.asList(4894, 4901), Arrays.asList(4918, 4924));
		List<BankPreviewItem> items = new ArrayList<>();
		for (int slot = 4; slot >= 0; slot--)
		{
			for (List<Integer> family : families)
			{
				if (slot < family.size()) items.add(described(family.get(slot)));
			}
		}
		List<BankPreviewItem> listed = GearItemSorter.bySet(items, GearStatsSource.NONE);
		for (List<Integer> family : families)
		{
			assertContiguousFamily("list family " + family.get(0), ids(listed), family);
		}
		assertPreserved("merged dictionary list", ids(items), listed);
	}

	// Complete synthetic stat vectors exercise sorting and layout, not live game bonuses.
	private static GearStats styledStats(GearSlot slot, GearStyle style, int offence)
	{
		int melee = style == GearStyle.MELEE ? offence : 0;
		int ranged = style == GearStyle.RANGED ? offence : 0;
		int magic = style == GearStyle.MAGIC ? offence : 0;
		int prayer = style == GearStyle.PRAYER ? offence : 0;
		return new GearStats(slot, melee, melee, melee, magic, ranged, melee, ranged, prayer,
			10, 10, 10, 10, 10, 0, 0);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, BankPreset preset,
		GearStatsSource stats, BankLayoutPlan plan)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			stats, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
	}

	private static BankSnapshot bank(List<Integer> ids, List<Integer> placeholders)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int itemId : ids)
		{
			boolean placeholder = placeholders.contains(itemId);
			items.add(new BankItemSnapshot(itemId, placeholder ? 0 : 1, items.size(), placeholder));
		}
		return new BankSnapshot(items);
	}

	private static void assertContiguousFamily(String context, List<Integer> laidOut, List<Integer> family)
	{
		int first = laidOut.indexOf(family.get(0));
		assertTrue(context + ", missing family", first >= 0);
		assertEquals(context + ", family remains one ordered run", family,
			laidOut.subList(first, first + family.size()));
	}

	private static void assertVerticalFamily(String context, List<Integer> laidOut, List<Integer> family)
	{
		int first = laidOut.indexOf(family.get(0));
		assertTrue(context + ", missing first family member", first >= 0);
		for (int member = 0; member < family.size(); member++)
			assertEquals(context + ", each piece stays one row below the previous piece",
				first + member * 8, laidOut.indexOf(family.get(member)));
	}

	private static Map<Integer, CatalogItem> addUnrelatedGear(List<Integer> input, int count)
	{
		Map<Integer, CatalogItem> catalog = new LinkedHashMap<>();
		for (int index = 0; index < count; index++)
		{
			int itemId = 983000 + index;
			input.add(itemId);
			catalog.put(itemId, new CatalogItem(itemId, "Unrelated gear " + index,
				ItemCategory.GEAR, "gear", Collections.emptySet(), null));
		}
		return catalog;
	}

	private static void assertPreserved(String context, List<Integer> expected, List<BankPreviewItem> actual)
	{
		assertEquals(context + ", every bank entry is preserved", expected.size(), actual.size());
		assertEquals(context + ", item IDs are preserved", new HashSet<>(expected), new HashSet<>(ids(actual)));
		assertEquals(context + ", each item appears once", actual.size(), new HashSet<>(ids(actual)).size());
		assertTrue(context + ", no invented empty cells", actual.stream().noneMatch(BankPreviewItem::isBlank));
	}

	private static List<Integer> integers(int[] ids)
	{
		return Arrays.stream(ids).boxed().collect(Collectors.toList());
	}

	private static List<BankPreviewItem> unrelatedGear(int count)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int slot = 0; slot < count; slot++)
		{
			int itemId = 982000 + slot;
			items.add(new BankPreviewItem(new CatalogItem(itemId, "Unrelated gear " + slot,
				ItemCategory.GEAR, "gear", Collections.emptySet(), null), 1));
		}
		return items;
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static BankPreviewItem described(int itemId)
	{
		return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId), 1);
	}
}

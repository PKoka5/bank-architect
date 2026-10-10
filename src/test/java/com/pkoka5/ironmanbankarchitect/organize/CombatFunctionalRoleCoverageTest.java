package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Ordinary TOP_LEVEL identities reviewed against item-id-research-index.tsv on 2026-10-09. */
@RunWith(Parameterized.class)
public class CombatFunctionalRoleCoverageTest
{
	private static final int[] HOODS = {
		9749, 9752, 9755, 9758, 9761, 9764, 9767, 9770, 9773, 9776, 9779, 9782, 9785, 9788,
		9791, 9794, 9797, 9800, 9803, 9806, 9809, 9812, 9814, 9950, 13070, 13223, 13281, 31292,
		13330, 13332, 13334, 13336, 13338, 20764, 21282, 21778, 21782, 21786, 21900, 24857, 27366, 28904
	};
	private static final int[] HOUSE_BANNERS = {20251, 20254, 20257, 20260, 20263};
	private static final int[] EVENT_COSTUMES = {
		20838, 20840, 20842, 20844, 20846,
		23091, 23093, 23095, 23097, 23099, 23101,
		27428, 27430, 27432, 27434, 27436, 27438
	};
	private static final int[] UTILITIES = {25557, 21180, 11136, 11138, 11140, 13103};
	private static final int[] FUNCTIONAL_CAPES = {9747, 9748, 9756, 9757, 9759, 9760, 9762, 9763, 9768, 9769};
	private static final int CAPE_POUCH = 28613;
	// Each colour: head active/inactive, body active/inactive, legs active/inactive.
	private static final int[][] COLOURED_CRYSTAL = {
		{27705, 27707, 27697, 27699, 27701, 27703},
		{27717, 27719, 27709, 27711, 27713, 27715},
		{27729, 27731, 27721, 27723, 27725, 27727},
		{27741, 27743, 27733, 27735, 27737, 27739},
		{27753, 27755, 27745, 27747, 27749, 27751},
		{27765, 27767, 27757, 27759, 27761, 27763},
		{27777, 27779, 27769, 27771, 27773, 27775}
	};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.MAIN}, {BankPresetType.IRONMAN}});
	}

	public CombatFunctionalRoleCoverageTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
		plan = BankLayoutPlan.defaultFor(preset).withTagAt("gear", 2).withTagAt("cosmetics", 8)
			.withTagAt("containers", 6).withTagAt("frequently-used", 5).withTagAt("teleports", 7);
	}

	@Test
	public void achievementHoodsAndHouseBannersAreCosmeticsWhileTheirFunctionalCapesRemainGear()
	{
		assertEquals(42, HOODS.length);
		for (int id : HOODS)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertTrue("ordinary hood identity " + id, item.getDisplayName().toLowerCase().contains("hood"));
			assertRole(id, ItemCategory.CLUE, "cosmetic");
		}
		for (int id : HOUSE_BANNERS)
		{
			assertTrue("ordinary house banner identity " + id,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getDisplayName().toLowerCase().contains("banner"));
			assertRole(id, ItemCategory.CLUE, "cosmetic");
		}
		assertRole(CAPE_POUCH, ItemCategory.TOOL, "resource-container");
		for (int id : EVENT_COSTUMES) assertRole(id, ItemCategory.CLUE, "cosmetic");
		for (int id : FUNCTIONAL_CAPES) assertFunctionalGear(id);
		for (int id : new int[]{25557, 21180}) assertRole(id, ItemCategory.TOOL, "skilling-utility");
		for (int id : new int[]{11136, 11138}) assertRole(id, ItemCategory.TOOL, "achievement-utility");
		for (int id : new int[]{11140, 13103}) assertRole(id, ItemCategory.TELEPORT, "teleport");
		for (int id : new int[]{13141, 13142}) assertRole(id, ItemCategory.TOOL, "achievement-utility");
		for (int id : new int[]{13143, 13144}) assertRole(id, ItemCategory.TELEPORT, "teleport");
	}

	@Test
	public void realEquipmentShapesKeepCosmeticsAndUtilitiesOutOfCombatInBothPresets()
	{
		List<Integer> ids = reviewedRoleIds();
		for (int id : FUNCTIONAL_CAPES) ids.add(id);
		for (int id : new int[]{13141, 13142, 13143, 13144}) ids.add(id);
		BankOrganizationPreview preview = build(bank(ids, 1), this::representativeRealStats, CategoryOverrideSource.NONE);
		assertEquals(ids.size(), preview.getPlannedItemCount());
		for (int id : HOODS) assertAt(preview, id, "cosmetics");
		for (int id : HOUSE_BANNERS) assertAt(preview, id, "cosmetics");
		for (int id : EVENT_COSTUMES) assertAt(preview, id, "cosmetics");
		for (int id : UTILITIES) assertAt(preview, id, automaticTag(id));
		assertAt(preview, CAPE_POUCH, "containers");
		assertEquals(ItemCategory.TOOL, item(preview, CAPE_POUCH).getItemCategory());
		for (int id : FUNCTIONAL_CAPES) assertAt(preview, id, "gear");
		String utility = preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "tools";
		for (int id : new int[]{13141, 13142}) assertAt(preview, id, utility);
		for (int id : new int[]{13143, 13144}) assertAt(preview, id, "teleports");
	}

	@Test
	public void evenPositiveEquipmentBonusesCannotOverrideReviewedCosmeticAndContainerFunctions()
	{
		List<Integer> ids = reviewedRoleIds();
		// Deliberately positive artificial stats and bulk value test the promotion and Alch guards.
		GearStatsSource misleadingStats = id -> Optional.of(new GearStats(GearSlot.HEAD,
			20, 20, 20, 20, 20, 20, 20, 4, 30));
		BankOrganizationPreview preview = build(bank(ids, 100), misleadingStats, CategoryOverrideSource.NONE);
		assertEquals(ids.size(), preview.getPlannedItemCount());
		for (int id : ids)
		{
			CatalogItem reviewed = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals("reviewed category " + id, reviewed.getCategory(), item(preview, id).getItemCategory());
			assertEquals("reviewed function " + id, reviewed.getSubcategory(), item(preview, id).getSubcategory());
			assertAt(preview, id, automaticTag(id));
			assertEquals(100, item(preview, id).getQuantity());
			assertFalse("reviewed non-combat role cannot become Alch " + id,
				"alch".equals(item(preview, id).getLayoutTagKey()));
		}
	}

	@Test
	public void personalGearPinsSurviveReloadAndClearingRestoresTheReviewedRoles()
	{
		List<Integer> ids = Arrays.asList(9803, 20251, CAPE_POUCH, 20838, 23091, 27428, 25557, 21180, 11136, 11140);
		UserCategoryOverrides choices = new UserCategoryOverrides();
		for (int id : ids) choices.put(id, "gear");
		UserCategoryOverrides restored = UserCategoryOverrides.parse(choices.serialize());
		BankOrganizationPreview manual = build(bank(ids, 1), this::representativeRealStats, restored);
		for (int id : ids) assertAt(manual, id, "gear");
		restored.clear();
		BankOrganizationPreview automatic = build(bank(ids, 1), this::representativeRealStats,
			UserCategoryOverrides.parse(restored.serialize()));
		for (int id : ids) assertAt(automatic, id, automaticTag(id));
		assertEquals(manual.getPlannedItemCount(), automatic.getPlannedItemCount());
	}

	@Test
	public void colouredCrystalActiveAndInactiveMembersShareOrdinaryFamiliesAndExcludePlaceholderRecords()
	{
		List<List<Integer>> actual = GearSetSemanticRuleSet.gearSetsInSlotOrder();
		Set<Integer> allFamilyIds = actual.stream().flatMap(Collection::stream).collect(Collectors.toSet());
		List<BankPreviewItem> input = new ArrayList<>();
		Map<Integer, GearSlot> slots = new LinkedHashMap<>();
		for (int[] family : COLOURED_CRYSTAL)
		{
			List<Integer> expected = Arrays.stream(family).boxed().collect(Collectors.toList());
			assertEquals("one reviewed family per colour " + family[0], 1,
				actual.stream().filter(members -> members.containsAll(expected)).count());
			assertTrue("same head/body/legs order for active and inactive states " + family[0], actual.contains(expected));
			for (int index = 0; index < family.length; index++)
			{
				int id = family[index];
				assertFunctionalGear(id);
				slots.put(id, new GearSlot[]{GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS}[index / 2]);
				assertFalse("raw placeholder record cannot enter an ordinary family " + (id + 1), allFamilyIds.contains(id + 1));
				input.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id), 1));
			}
		}
		Collections.reverse(input);
		List<Integer> sorted = GearItemSorter.bySet(input, GearStatsSource.NONE).stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
		assertEquals(42, sorted.size());
		assertEquals(42, new HashSet<>(sorted).size());
		for (int[] family : COLOURED_CRYSTAL)
		{
			List<Integer> expected = Arrays.stream(family).boxed().collect(Collectors.toList());
			int first = sorted.indexOf(family[0]);
			assertTrue("missing Crystal family " + family[0], first >= 0);
			assertEquals("one contiguous canonical family " + family[0], expected,
				sorted.subList(first, first + family.length));
		}
		// Runtime equipment slots refine generic catalogue "gear" metadata.
		GearStatsSource source = id -> Optional.of(new GearStats(slots.get(id), 0, 0, 0, 0, 20, 0, 0, 0, 30));
		BankOrganizationPreview preview = build(bank(sorted, 1), source, CategoryOverrideSource.NONE);
		assertEquals(42, preview.getPlannedItemCount());
		for (Map.Entry<Integer, GearSlot> entry : slots.entrySet())
		{
			assertAt(preview, entry.getKey(), "gear");
			assertEquals(entry.getKey() + ": supplied equipment slot refines catalog metadata",
				entry.getValue().name().toLowerCase(), item(preview, entry.getKey()).getSubcategory());
		}
	}

	private void assertFunctionalGear(int id)
	{
		CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
		assertEquals(id + " " + item.getDisplayName(), ItemCategory.GEAR, item.getCategory());
		assertEquals(id + " " + item.getDisplayName(), "combat-gear", PresetCategoryMapper.map(preset, item).getKey());
	}

	private static void assertRole(int id, ItemCategory category, String subcategory)
	{
		CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
		assertEquals(id + " " + item.getDisplayName(), category, item.getCategory());
		assertEquals(id + " " + item.getDisplayName(), subcategory, item.getSubcategory());
	}

	private List<Integer> reviewedRoleIds()
	{
		List<Integer> ids = new ArrayList<>();
		for (int id : HOODS) ids.add(id);
		for (int id : HOUSE_BANNERS) ids.add(id);
		ids.add(CAPE_POUCH);
		for (int id : EVENT_COSTUMES) ids.add(id);
		for (int id : UTILITIES) ids.add(id);
		assertEquals("reviewed role correction fixture", 71, ids.size());
		return ids;
	}

	private String automaticTag(int id)
	{
		if (id == CAPE_POUCH) return "containers";
		if (id == 11140 || id == 13103) return "teleports";
		if (id == 25557 || id == 21180) return "tools";
		if (id == 11136 || id == 11138)
			return preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "tools";
		return "cosmetics";
	}

	private BankOrganizationPreview build(BankSnapshot bank, GearStatsSource stats, CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset, stats,
			id -> 25000, overrides, plan, BankLayoutOptions.defaultFor(preset));
	}

	private static BankSnapshot bank(List<Integer> ids, int quantity)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, quantity, items.size()));
		return new BankSnapshot(items);
	}

	private Optional<GearStats> representativeRealStats(int id)
	{
		// Actual cached RuneLite equipment shapes: zero-stat hood; positive-bonus house banner;
		// untrimmed skillcape defensive vector. Cape pouch has no equipment stats.
		if (id == CAPE_POUCH) return Optional.empty();
		if (Arrays.stream(HOODS).anyMatch(hood -> hood == id))
			return Optional.of(new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		if (Arrays.stream(HOUSE_BANNERS).anyMatch(banner -> banner == id) || id >= 13141 && id <= 13144)
			return Optional.of(new GearStats(GearSlot.WEAPON, 12, 12, 12, 0, 0, 12, 0, 0,
				1, 1, 0, 0, 0, 0, 5));
		return Optional.of(new GearStats(GearSlot.CAPE, 0, 0, 0, 0, 0, 0, 0, 0,
			9, 9, 9, 9, 9, 0, 0));
	}

	private void assertAt(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals(preset.getType() + ": tag for " + id, tag, item(preview, id).getLayoutTagKey());
		assertTrue(preset.getType() + ": destination for " + id + " " + tag,
			preview.getCategories().get(plan.destinationOf(tag)).getItems().stream().anyMatch(entry -> entry.getItemId() == id));
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(entry -> entry.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("Missing item " + id));
	}
}

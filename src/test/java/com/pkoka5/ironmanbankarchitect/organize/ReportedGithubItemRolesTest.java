package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Exact ordinary items reported in GitHub issues 42/44 and the Crafting cape follow-up. */
@RunWith(Parameterized.class)
public class ReportedGithubItemRolesTest
{
	private static final List<Integer> DEADMAN_CRYSTAL = Arrays.asList(33031,33033,33023,33025,33027,33029);
	private static final int[] TELEPORT_CAPES = {9780,9781,9789,9790};
	private static final int[] ITEMS = {995,9780,9781,9789,9790,33021,33031,33033,33023,33025,33027,33029,
		25867,23971,23975,23979};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public ReportedGithubItemRolesTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void deadmanVariantsHaveTheSameReviewedCombatFunctionsAsOrdinaryCrystal()
	{
		assertEquals("weapon-ranged-bow", GearSetSemanticRuleSet.weaponRole(33021));
		assertTrue("Deadman Bowfa activates the same Crystal armour setup policy", GearSetSemanticRuleSet.isBowfa(33021));
		assertTrue("All states form one actual equipment family", GearSetSemanticRuleSet.gearSetsInSlotOrder()
			.stream().anyMatch(family -> family.containsAll(DEADMAN_CRYSTAL)));
		for (int id : DEADMAN_CRYSTAL)
		{
			assertTrue("Crystal synergy recognizes the exact state " + id, GearSetSemanticRuleSet.isCrystalArmour(id));
			assertGear(id, id == 33031 || id == 33033 ? "head" : id == 33023 || id == 33025 ? "body" : "legs");
		}
		assertGear(33021, "weapon");
		for (int id : new int[] {33021,33031,33033,33023,33025,33027,33029})
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals("Controlled ordinary-name tier fallback " + id, 4,
				GearTierCatalog.INSTANCE.tierOf(id, item.getDisplayName()).orElse(-1));
			assertEquals("Both presets retain functional combat equipment", "combat-gear",
				PresetCategoryMapper.map(preset, item).getKey());
		}
	}

	@Test
	public void mixedOrdinaryAndDeadmanEquipmentRemainsStableWithShufflesAndPlaceholders()
	{
		BankSnapshot source = bank(false);
		BankOrganizationPreview baseline = build(source, CategoryOverrideSource.NONE, BlueprintItemOrders.EMPTY);
		List<Integer> expected = ids(baseline);
		for (int trial = 0; trial < 4; trial++)
		{
			List<BankItemSnapshot> entries = new ArrayList<>(bank(trial >= 2).getPhysicalItems());
			Collections.shuffle(entries, new Random(202610130042L + trial));
			BankSnapshot changed = new BankSnapshot(entries);
			BankOrganizationPreview actual = build(changed, CategoryOverrideSource.NONE, BlueprintItemOrders.EMPTY);
			assertEquals("Shuffle/placeholder trial " + trial, expected, ids(actual));
			assertConserved(changed, actual);
			for (int id : new int[] {33021,33031,33023,33027}) assertTag(actual, id, "gear");
			for (int id : TELEPORT_CAPES) assertTag(actual, id, "teleports");
		}
	}

	@Test
	public void reviewedCapesRouteAsTeleportsDespitePositiveEquipmentStatsAndAlchValues()
	{
		BankSnapshot bank = bank(false);
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, BlueprintItemOrders.EMPTY);
		for (int id : TELEPORT_CAPES)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals(ItemCategory.TELEPORT, item.getCategory());
			assertEquals("teleport", item.getSubcategory());
			assertTag(preview, id, "teleports");
			int destination = preset.getType() == BankPresetType.IRONMAN ? 0 : 1;
			assertTrue("Cape joins the preset's other teleports", idsOn(preview, destination).contains(id));
			assertEquals("Functional teleport survives runtime cape stats", ItemCategory.TELEPORT,
				item(preview, id).getItemCategory());
		}
		assertNotEquals("The separate hood receives no cape teleport policy", ItemCategory.TELEPORT,
			CompositeItemCatalog.DEFAULT.describeOrUnknown(9791).getCategory());
		CatalogItem craftingHood = CompositeItemCatalog.DEFAULT.describeOrUnknown(9782);
		assertEquals("The Crafting hood retains its reviewed cosmetic role", ItemCategory.CLUE, craftingHood.getCategory());
		assertEquals("cosmetic", craftingHood.getSubcategory());
		assertConserved(bank, preview);
	}

	@Test
	public void explicitOwnerCorrectionsStillWinAndClearingThemRestoresTheAutomaticRole()
	{
		UserCategoryOverrides choices = new UserCategoryOverrides();
		choices.put(9780, "tools");
		choices.put(9781, "frequently-used");
		choices.put(9789, "tools");
		choices.put(33021, "alch");
		BankSnapshot bank = bank(false);
		BankOrganizationPreview chosen = build(bank, UserCategoryOverrides.parse(choices.serialize()), BlueprintItemOrders.EMPTY);
		assertTag(chosen, 9780, "tools");
		assertTag(chosen, 9781, "frequently-used");
		assertTag(chosen, 9789, "tools");
		assertTag(chosen, 33021, "alch");
		assertConserved(bank, chosen);
		choices.remove(9780);
		choices.remove(9781);
		choices.remove(9789);
		choices.remove(33021);
		BankOrganizationPreview automatic = build(bank, choices, BlueprintItemOrders.EMPTY);
		assertTag(automatic, 9780, "teleports");
		assertTag(automatic, 9781, "teleports");
		assertTag(automatic, 9789, "teleports");
		assertTag(automatic, 33021, "gear");
		assertConserved(bank, automatic);
	}

	@Test
	public void savedPhysicalCombatAndTeleportOrdersSurviveTheNewMetadata()
	{
		BankSnapshot bank = bank(false);
		BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, BlueprintItemOrders.EMPTY);
		for (String tag : new String[] {"gear", "teleports"})
		{
			int destination = plan.destinationOf(tag);
			List<Integer> wanted = new ArrayList<>(idsOn(automatic, destination));
			Collections.reverse(wanted);
			BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY.withTab(destination, wanted).serialize());
			BankOrganizationPreview edited = build(bank, CategoryOverrideSource.NONE, saved);
			assertEquals(wanted, idsOn(edited, destination));
			assertTrue(edited.getCategories().get(destination).hasManualOrder());
			assertConserved(bank, edited);
		}
	}

	@Test
	public void reviewedSlotsAndRolesWorkEvenWhenRuntimeEquipmentStatsAreUnavailable()
	{
		BankSnapshot bank = bank(false);
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan,
			BankLayoutOptions.defaultFor(preset));
		for (int id : DEADMAN_CRYSTAL)
		{
			assertTag(preview, id, "gear");
			int expected = id == 33031 || id == 33033 ? 0 : id == 33023 || id == 33025 ? 1 : 2;
			assertEquals("Equipment slot without runtime stats " + id, expected,
				GearItemSorter.slotRankOf(item(preview,id), GearStatsSource.NONE));
		}
		assertTag(preview, 33021, "gear");
		assertEquals("Reviewed Bowfa remains a Ranged weapon without stats", 9,
			GearItemSorter.slotRankOf(item(preview,33021), GearStatsSource.NONE));
		for (int id : TELEPORT_CAPES) assertTag(preview,id,"teleports");
		assertConserved(bank, preview);
	}

	@Test
	public void activeAndInactiveCrystalStatesFormAdjacentVerticalColumnsInAFilledRangedRegion()
	{
		int[] ids = {33031,33033,33023,33025,33027,33029,23971,23973,23975,23977,23979,23981,
			12496,12492,12494,23197,12490,19921,4732,4734,4736,4738,2503,2497,19547,22284,22109};
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : ids) entries.add(new BankItemSnapshot(id,1,entries.size()));
		BankSnapshot bank = new BankSnapshot(entries);
		BankLayoutOptions base = BankLayoutOptions.defaultFor(preset);
		BankLayoutOptions sets = new BankLayoutOptions(true,base.fillHerbloreRows(),false,Collections.emptyMap(),
			GearLayout.GRID_SETS,base.potionDoses(),base.runeOrder(),base.teleportOrder(),base.gatherFrequentlyUsed());
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan, sets);
		List<Integer> actual = idsOn(preview,plan.destinationOf("gear"));
		assertEquals("All 27 real Ranged pieces provide the physical region", 27,actual.size());
		for (int[] column : new int[][] {{33031,33023,33027},{33033,33025,33029}})
		{
			int head = actual.indexOf(column[0]);
			assertTrue("Column head is present",head >= 0);
			assertEquals("Body directly below its state head",head+8,actual.indexOf(column[1]));
			assertEquals("Legs directly below its state body",head+16,actual.indexOf(column[2]));
		}
		assertEquals("Active/inactive columns are adjacent",actual.indexOf(33031)+1,actual.indexOf(33033));
		assertConserved(bank,preview);
	}

	private void assertGear(int id, String slot)
	{
		CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
		assertEquals("Reviewed ordinary equipment " + id, ItemCategory.GEAR, item.getCategory());
		assertEquals("Reviewed equipment slot " + id, slot, item.getSubcategory());
	}

	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource choices, BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			id -> Optional.ofNullable(stats(id)), id -> 25000, choices, plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
	}

	private static BankSnapshot bank(boolean withdrawn)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ITEMS)
		{
			boolean placeholder = withdrawn && id != 995;
			items.add(new BankItemSnapshot(id, placeholder ? 0 : id == 995 ? 100000 : 1, items.size(), placeholder));
		}
		return new BankSnapshot(items);
	}

	/** Frozen RuneLite item-stats snapshot checked locally, not fetched by the plugin or test. */
	private static GearStats stats(int id)
	{
		switch (id)
		{
			case 25867: case 33021:
				return new GearStats(GearSlot.WEAPON,0,0,0,0,128,0,106,0,0,0,0,0,0,0,5);
			case 23971: case 33031:
				return new GearStats(GearSlot.HEAD,0,0,0,-10,9,0,0,2,12,8,14,10,18,0,0);
			case 23975: case 33023:
				return new GearStats(GearSlot.BODY,0,0,0,-18,31,0,0,3,46,38,48,44,68,0,0);
			case 23979: case 33027:
				return new GearStats(GearSlot.LEGS,0,0,0,-12,18,0,0,2,26,21,30,34,38,0,0);
			case 33033: return new GearStats(GearSlot.HEAD,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
			case 33025: return new GearStats(GearSlot.BODY,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
			case 33029: return new GearStats(GearSlot.LEGS,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
			case 9780: case 9781: case 9789: case 9790:
				return new GearStats(GearSlot.CAPE,0,0,0,0,0,0,0,id == 9781 || id == 9790 ? 4 : 0,9,9,9,9,9,0,0);
			default: return null;
		}
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("Missing item " + id));
	}

	private void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("Effective tag " + id, tag, item(preview,id).getLayoutTagKey());
		assertTrue("Effective destination " + id, idsOn(preview,plan.destinationOf(tag)).contains(id));
	}

	private static List<Integer> ids(BankOrganizationPreview preview)
	{
		return preview.getPlannedItems().stream().filter(item -> !item.isBlank())
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<Integer> idsOn(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().filter(item -> !item.isBlank())
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertConserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		for (BankItemSnapshot original : bank.getPhysicalItems())
		{
			assertEquals("One real item " + original.getItemId(), 1, Collections.frequency(ids(preview), original.getItemId()));
			BankPreviewItem actual = item(preview, original.getItemId());
			assertEquals(original.getQuantity(), actual.getQuantity());
			assertEquals(original.isPlaceholder(), actual.isPlaceholder());
		}
	}
}

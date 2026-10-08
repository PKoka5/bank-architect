package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Reviewed functions must survive mixed-bank stats, Alch and saved player choices. */
@RunWith(Parameterized.class)
public class ReviewedFunctionalPlacementTest
{
	private static final ItemValueSource VALUES = id -> 25000;
	// Deliberately positive stats even for non-equipment, to exercise promotion protection.
	private static final GearStatsSource STATS = id -> Optional.of(stats(
		id == 9768 || id == 9769 ? GearSlot.CAPE : GearSlot.WEAPON, id == 4151 ? 60 : 20));
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	private final BankLayoutOptions defaults;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.IRONMAN}, {BankPresetType.MAIN}});
	}

	public ReviewedFunctionalPlacementTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
		plan = BankLayoutPlan.defaultFor(preset);
		defaults = BankLayoutOptions.defaultFor(preset);
	}

	@Test
	public void reviewedNonCombatRolesSurvivePositiveStatsAndAnActiveMixedBankAlchPile()
	{
		BankSnapshot bank = bank(995, 20017, 22840, 22842, 24719, 28152, 31946, 4485,
			13137, 13138, 9768, 9769, 13108, 13109, 1373, 4151);
		BankOrganizationPreview preview = build(bank, CompositeItemCatalog.DEFAULT,
			CategoryOverrideSource.NONE, defaults);
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		assertTag(preview, 1373, "alch");
		assertTag(preview, 4151, "gear");
		assertTag(preview, 995, "currency");
		for (int id : new int[] {20017, 22840}) assertTag(preview, id, "cosmetics");
		for (int id : new int[] {22842, 24719}) assertTag(preview, id, "tools");
		for (int id : new int[] {28152, 31946}) assertTag(preview, id, "raw-resources");
		assertTag(preview, 4485, "quest-items");
		for (int id : new int[] {13137, 13138})
			assertTag(preview, id, preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "tools");
		for (int id : new int[] {995, 20017, 22840, 22842, 24719, 28152, 31946, 4485, 13137, 13138})
		{
			CatalogItem catalog = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals("Keep reviewed function " + id, catalog.getCategory(), item(preview, id).getItemCategory());
			assertEquals("Keep reviewed subcategory " + id, catalog.getSubcategory(), item(preview, id).getSubcategory());
			assertFalse("Non-combat role must not become Alch " + id, "alch".equals(item(preview, id).getLayoutTagKey()));
		}
		for (int id : new int[] {9768, 9769}) assertTag(preview, id, "gear");
		for (int id : new int[] {9768, 9769, 13108, 13109})
		{
			assertEquals("Functional equipment " + id, ItemCategory.GEAR, item(preview, id).getItemCategory());
			assertFalse("Equipment is not currency " + id, "currency".equals(item(preview, id).getLayoutTagKey()));
			assertFalse("Lower-tier item has no own teleport " + id, "teleports".equals(item(preview, id).getLayoutTagKey()));
		}
		for (int id : new int[] {13108, 13109})
			assertTag(preview, id, preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "gear");
	}

	@Test
	public void explicitTagCorrectionReloadsAndClearingItRestoresTheReviewedAutomaticRole()
	{
		BankSnapshot bank = bank(28152, 20017, 995);
		UserCategoryOverrides choice = new UserCategoryOverrides();
		choice.put(28152, "frequently-used");
		UserCategoryOverrides reloaded = UserCategoryOverrides.parse(choice.serialize());
		BankOrganizationPreview manual = build(bank, CompositeItemCatalog.DEFAULT, reloaded, defaults);
		assertTag(manual, 28152, "frequently-used");
		assertEquals(ItemCategory.SKILLING, item(manual, 28152).getItemCategory());
		assertEquals("woodcutting-supply", item(manual, 28152).getSubcategory());
		assertTag(manual, 20017, "cosmetics");
		assertTag(manual, 995, "currency");

		reloaded.remove(28152);
		BankOrganizationPreview automatic = build(bank, CompositeItemCatalog.DEFAULT,
			UserCategoryOverrides.parse(reloaded.serialize()), defaults);
		assertTag(automatic, 28152, "raw-resources");
		assertEquals(ItemCategory.SKILLING, item(automatic, 28152).getItemCategory());
		assertEquals(manual.getPlannedItemCount(), automatic.getPlannedItemCount());
	}

	@Test
	public void capturedPhysicalOrderSurvivesCorrectedFunctionsAndPlaceholderUpgradesProveNothing()
	{
		List<BankItemSnapshot> items = new ArrayList<>(bank(22840, 4485, 28152, 995, 1275).getPhysicalItems());
		items.add(new BankItemSnapshot(11920, 0, items.size(), true));
		BankSnapshot bank = new BankSnapshot(items);
		ItemCatalog legacy = id -> id == 28152
			? Optional.of(new CatalogItem(id, "Nature offerings", ItemCategory.GEAR, "weapon", Collections.emptySet(), null))
			: CompositeItemCatalog.DEFAULT.findById(id);
		BankOrganizationPreview old = build(bank, legacy, CategoryOverrideSource.NONE, defaults);
		assertEquals(ItemCategory.GEAR, item(old, 28152).getItemCategory());
		List<Integer> physicalOrder = Arrays.asList(22840, 4485, 28152, 995, 11920, 1275);
		Map<Integer, List<Integer>> physical = new LinkedHashMap<>();
		physical.put(7, physicalOrder);
		BlueprintItemOrders captured = BlueprintItemOrders.parse(
			BlueprintItemOrders.capture(physical, old, BlueprintItemOrders.EMPTY).serialize());
		assertTrue(captured.isCaptured());

		BankOrganizationPreview corrected = build(bank, CompositeItemCatalog.DEFAULT,
			CategoryOverrideSource.NONE, defaults.withItemOrders(captured));
		assertEquals(physicalOrder, ids(corrected, 7));
		assertEquals(bank.getPhysicalItems().size(), corrected.getPlannedItemCount());
		for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			if (tab != 7) assertTrue("Captured bank remains on its saved physical tab " + tab, ids(corrected, tab).isEmpty());
		assertEquals(ItemCategory.SKILLING, item(corrected, 28152).getItemCategory());
		assertEquals("woodcutting-supply", item(corrected, 28152).getSubcategory());
		assertEquals("raw-resources", item(corrected, 28152).getLayoutTagKey());
		assertEquals("quest-items", item(corrected, 4485).getLayoutTagKey());
		assertEquals("cosmetics", item(corrected, 22840).getLayoutTagKey());
		assertTrue(item(corrected, 11920).isPlaceholder());
		assertEquals(0, item(corrected, 11920).getQuantity());
		assertFalse(item(corrected, 1275).isPlaceholder());
		assertFalse("Placeholder Dragon pickaxe cannot replace the owned Rune pickaxe for Alch",
			"alch".equals(item(corrected, 1275).getLayoutTagKey()));
		assertEquals(0, corrected.getTagCounts().getOrDefault("alch", 0).intValue());
		assertEquals(corrected.getPlannedItemCount() - 1,
			corrected.getTagCounts().values().stream().mapToInt(Integer::intValue).sum());
	}

	@Test
	public void unknownFutureEquipmentNeverBecomesAutomaticAlchWithoutAReviewedRole()
	{
		ItemCatalog catalog = id -> id == 900002
			? Optional.of(new CatalogItem(id, "Unreviewed future armour", ItemCategory.UNCATEGORIZED,
				"unknown", Collections.emptySet(), null))
			: id == 900003 ? Optional.of(new CatalogItem(id, "Reviewed production armour", ItemCategory.GEAR,
				"body", Collections.emptySet(), null)) : CompositeItemCatalog.DEFAULT.findById(id);
		GearStatsSource equipment = id -> Optional.of(stats(GearSlot.BODY, id >= 900001 ? 5 : 100));
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(900001, 100, 0), new BankItemSnapshot(900002, 100, 1),
			new BankItemSnapshot(900003, 100, 2), new BankItemSnapshot(1127, 1, 3),
			new BankItemSnapshot(3140, 1, 4), new BankItemSnapshot(11832, 1, 5)));
		BankOrganizationPreview automatic = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
			equipment, VALUES, CategoryOverrideSource.NONE, plan, defaults);
		// Same weak stats, bulk quantity and value qualify the reviewed control for Alch.
		assertTag(automatic, 900003, "alch");
		for (int id : new int[] {900001, 900002})
		{
			assertTag(automatic, id, "gear");
			assertEquals("Runtime equipment evidence may promote an unreviewed item " + id,
				ItemCategory.GEAR, item(automatic, id).getItemCategory());
			assertEquals("body", item(automatic, id).getSubcategory());
			assertEquals(100, item(automatic, id).getQuantity());
		}
		UserCategoryOverrides ownerChoice = UserCategoryOverrides.parse("900001=alch,900002=alch");
		BankOrganizationPreview manual = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
			equipment, VALUES, ownerChoice, plan, defaults);
		assertTag(manual, 900001, "alch");
		assertTag(manual, 900002, "alch");
		assertEquals(automatic.getPlannedItemCount(), manual.getPlannedItemCount());

		BankOrganizationPreview withoutStats = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
			GearStatsSource.NONE, VALUES, CategoryOverrideSource.NONE, plan, defaults);
		assertTag(withoutStats, 900001, "cleanup");
		assertEquals(ItemCategory.UNKNOWN, item(withoutStats, 900001).getItemCategory());
		assertTag(withoutStats, 900002, "cleanup");
		assertEquals(ItemCategory.UNCATEGORIZED, item(withoutStats, 900002).getItemCategory());
	}

	private BankOrganizationPreview build(BankSnapshot bank, ItemCatalog catalog,
		CategoryOverrideSource choices, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, catalog, preset, STATS, VALUES, choices, plan, options);
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids)
		{
			int quantity = id == 995 ? 100000 : id == 28152 || id == 31946 || id == 1373 ? 25 : 1;
			items.add(new BankItemSnapshot(id, quantity, items.size()));
		}
		return new BankSnapshot(items);
	}

	private static GearStats stats(GearSlot slot, int score)
	{
		return new GearStats(slot, score, score, score, 0, 0, score, 0, 0,
			score, score, score, score, score, 0, 4);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> !item.isBlank() && item.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("Missing planned item " + id));
	}

	private void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("Item " + id + " final tag", tag, item(preview, id).getLayoutTagKey());
		assertTrue("Item " + id + " reaches the tag's physical destination", ids(preview, plan.destinationOf(tag)).contains(id));
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().filter(item -> !item.isBlank())
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

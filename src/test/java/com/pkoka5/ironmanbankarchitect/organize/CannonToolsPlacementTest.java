package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CannonToolsPlacementTest
{
	private static final List<Integer> NORMAL = Arrays.asList(6, 8, 10, 12);
	private static final List<Integer> ORNAMENTED = Arrays.asList(26520, 26522, 26524, 26526);
	private static final List<Integer> OTHER_TOOLS = Arrays.asList(
		305, 307, 309, 301, 303, 5340, 5325, 5341, 5343, 2347, 8794, 1785, 1733, 946,
		10006, 10008, 10010, 10150, 590, 233);
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public CannonToolsPlacementTest(BankPreset preset)
	{
		this.preset = preset;
		this.plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void bothCannonFinishesAndUnopenedBoxUseToolsWhileFinishedCannonballsRemainDrops()
	{
		BankSnapshot bank = bank(6, 8, 10, 12, 26520, 26522, 26524, 26526, 12863, 2, 21728);
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
		for (int id : allParts()) assertPlacement(preview, id, "tools", plan.destinationOf("tools"));
		assertPlacement(preview, 12863, "tools", plan.destinationOf("tools"));
		for (int id : new int[] {2, 21728})
			assertPlacement(preview, id, "boss-loot", plan.destinationOf("boss-loot"));
		List<Integer> tools = ids(preview.getCategories().get(plan.destinationOf("tools")));
		assertCompactRectangle(tools, NORMAL);
		assertCompactRectangle(tools, ORNAMENTED);
		assertPreserved(bank, preview);
	}

	@Test
	public void sparsePartialAndBusyToolsKeepEachOwnedFinishInACompactPhysicalBlock()
	{
		List<List<Integer>> partSets = Arrays.asList(NORMAL, ORNAMENTED, allParts(),
			Arrays.asList(6, 12), Arrays.asList(8, 10, 12), Arrays.asList(26520, 26524),
			Arrays.asList(26522, 26524, 26526), Arrays.asList(6, 8, 10, 26526),
			Arrays.asList(6, 26522, 26524, 26526), Arrays.asList(6, 26526));
		for (List<Integer> parts : partSets)
		{
			for (int count : new int[] {0, 1, 3, 7, 12, OTHER_TOOLS.size()})
			{
				List<Integer> source = new ArrayList<>(parts);
				source.addAll(OTHER_TOOLS.subList(0, count));
				Collections.shuffle(source, new Random(31L * count + parts.get(0)));
				BankSnapshot bank = bank(source);
				BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan,
					BlueprintItemOrders.EMPTY);
				for (int id : parts) assertPlacement(preview, id, "tools", plan.destinationOf("tools"));
				List<Integer> tools = ids(preview.getCategories().get(plan.destinationOf("tools")));
				assertCompactRectangle(tools, NORMAL);
				assertCompactRectangle(tools, ORNAMENTED);
				assertPreserved(bank, preview);
			}
		}
	}

	@Test
	public void cannonBlocksStayDenseBesideMultipleSkillingOutfits()
	{
		List<Integer> source = new ArrayList<>(allParts());
		source.addAll(OTHER_TOOLS);
		source.addAll(Arrays.asList(13258, 13259, 13260, 13261,
			12013, 12014, 12015, 12016, 5554, 5553, 5555, 5556, 5557));
		BankSnapshot bank = bank(source);
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
		List<Integer> tools = ids(preview.getCategories().get(plan.destinationOf("tools")));
		assertCompactRectangle(tools, NORMAL);
		assertCompactRectangle(tools, ORNAMENTED);
		assertPreserved(bank, preview);
	}

	@Test
	public void withdrawnCannonPlaceholdersRetainTheirToolBlockAndQuantities()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(6, 0, 0, true),
			new BankItemSnapshot(8, 1, 1), new BankItemSnapshot(10, 0, 2, true),
			new BankItemSnapshot(12, 1, 3), new BankItemSnapshot(26520, 0, 4, true),
			new BankItemSnapshot(26522, 1, 5), new BankItemSnapshot(26524, 1, 6),
			new BankItemSnapshot(26526, 0, 7, true), new BankItemSnapshot(590, 1, 8)));
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
		List<Integer> tools = ids(preview.getCategories().get(plan.destinationOf("tools")));
		assertCompactRectangle(tools, NORMAL);
		assertCompactRectangle(tools, ORNAMENTED);
		for (int id : allParts()) assertPlacement(preview, id, "tools", plan.destinationOf("tools"));
		assertPreserved(bank, preview);
	}

	@Test
	public void cannonPackingDoesNotDependOnTheCombatLayoutOrEquipmentRowFilling()
	{
		BankSnapshot bank = bank(6, 8, 10, 12, 26520, 26522, 26524, 26526, 590, 305, 307);
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		List<Integer> expected = null;
		for (GearLayout gearLayout : GearLayout.values())
		{
			for (boolean fill : Arrays.asList(false, true))
			{
				BankLayoutOptions options = new BankLayoutOptions(fill, defaults.fillHerbloreRows(),
					defaults.alchPile(), Collections.emptyMap(), gearLayout, defaults.potionDoses(),
					defaults.runeOrder(), defaults.teleportOrder(), false);
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
					CompositeItemCatalog.DEFAULT, preset, GearStatsSource.NONE, ItemValueSource.NONE,
					CategoryOverrideSource.NONE, plan, options);
				List<Integer> actual = ids(preview.getCategories().get(plan.destinationOf("tools")));
				if (expected == null) expected = actual;
				else assertEquals(expected, actual);
				assertCompactRectangle(actual, NORMAL);
				assertCompactRectangle(actual, ORNAMENTED);
				assertPreserved(bank, preview);
			}
		}
	}

	@Test
	public void explicitCombatCorrectionsWinAndResetRestoresTools()
	{
		BankSnapshot bank = bank(6, 8, 10, 12, 26520, 26522, 26524, 26526);
		for (String choice : Arrays.asList("gear", "combat-gear"))
		{
			BankOrganizationPreview preview = build(bank,
				id -> NORMAL.contains(id) ? Optional.of(choice) : Optional.empty(), plan,
				BlueprintItemOrders.EMPTY);
			for (int id : NORMAL) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
			for (int id : ORNAMENTED) assertPlacement(preview, id, "tools", plan.destinationOf("tools"));
			assertPreserved(bank, preview);
		}
		BankOrganizationPreview reset = build(bank, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
		for (int id : allParts()) assertPlacement(reset, id, "tools", plan.destinationOf("tools"));
	}

	@Test
	public void personalToolsOrderMayIntentionallySeparateCannonParts()
	{
		BankSnapshot bank = bank(6, 8, 10, 12, 305, 307, 590);
		List<Integer> manual = Arrays.asList(6, 305, 8, 307, 10, 590, 12);
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("tools"), manual).serialize());
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, saved);
		BankCategoryPreview tools = preview.getCategories().get(plan.destinationOf("tools"));
		assertTrue(tools.hasManualOrder());
		assertEquals(manual, ids(tools));
		assertPreserved(bank, preview);
	}

	@Test
	public void validOldEditorCombatDestinationSurvivesTheAutomaticMoveToTools()
	{
		BankSnapshot bank = bank(6, 8, 10, 12, 26520, 26522, 26524, 26526);
		Map<String, BlueprintItemOrders.Destination> destinations = new LinkedHashMap<>();
		for (int id : NORMAL) destinations.put(id + "#0",
			new BlueprintItemOrders.Destination(plan.destinationOf("gear"), "gear", "gear"));
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withDestinations(destinations).serialize());
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, saved);
		for (int id : NORMAL) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
		for (int id : ORNAMENTED) assertPlacement(preview, id, "tools", plan.destinationOf("tools"));
		assertPreserved(bank, preview);
		BankLayoutPlan movedGear = plan.withTagAt("gear", 9);
		BankOrganizationPreview released = build(bank, CategoryOverrideSource.NONE, movedGear, saved);
		for (int id : allParts()) assertPlacement(released, id, "tools", movedGear.destinationOf("tools"));
		assertPreserved(bank, released);
	}

	@Test
	public void capturedBankCombatOrderKeepsThePlayersExistingCannonLocation()
	{
		BankSnapshot bank = bank(6, 8, 10, 12);
		BankOrganizationPreview original = build(bank, id -> Optional.of("gear"), plan,
			BlueprintItemOrders.EMPTY);
		List<Integer> manual = Arrays.asList(12, 6, 10, 8);
		int combat = plan.destinationOf("gear");
		BlueprintItemOrders captured = BlueprintItemOrders.parse(BlueprintItemOrders.capture(
			Collections.singletonMap(combat, manual), original, BlueprintItemOrders.EMPTY).serialize());
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE, plan, captured);
		assertTrue(preview.getCategories().get(combat).hasManualOrder());
		assertEquals(manual, ids(preview.getCategories().get(combat)));
		for (int id : NORMAL) assertPlacement(preview, id, "gear", combat);
		assertPreserved(bank, preview);
	}

	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource overrides,
		BankLayoutPlan layout, BlueprintItemOrders orders)
	{
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		BankLayoutOptions options = new BankLayoutOptions(defaults.fillGearRows(), defaults.fillHerbloreRows(),
			defaults.alchPile(), Collections.emptyMap(), defaults.gearLayout(), defaults.potionDoses(),
			defaults.runeOrder(), defaults.teleportOrder(), false).withItemOrders(orders);
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			GearStatsSource.NONE, ItemValueSource.NONE, overrides, layout, options);
	}

	private static BankSnapshot bank(Integer... ids)
	{
		return bank(Arrays.asList(ids));
	}

	private static BankSnapshot bank(List<Integer> ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, 1, items.size()));
		return new BankSnapshot(items);
	}

	private static List<Integer> allParts()
	{
		List<Integer> result = new ArrayList<>(NORMAL);
		result.addAll(ORNAMENTED);
		return result;
	}

	private static List<Integer> ids(BankCategoryPreview category)
	{
		return category.getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertPlacement(BankOrganizationPreview preview, int id, String tag, int destination)
	{
		List<BankPreviewItem> matches = preview.getCategories().get(destination).getItems().stream()
			.filter(item -> item.getItemId() == id).collect(Collectors.toList());
		assertEquals("item " + id + " in destination " + destination, 1, matches.size());
		assertEquals("item " + id, tag, matches.get(0).getLayoutTagKey());
	}

	private static void assertCompactRectangle(List<Integer> target, List<Integer> family)
	{
		List<Integer> positions = family.stream().filter(target::contains).map(target::indexOf)
			.collect(Collectors.toList());
		if (positions.size() < 2) return;
		int minRow = positions.stream().mapToInt(index -> index / 8).min().getAsInt();
		int maxRow = positions.stream().mapToInt(index -> index / 8).max().getAsInt();
		int minColumn = positions.stream().mapToInt(index -> index % 8).min().getAsInt();
		int maxColumn = positions.stream().mapToInt(index -> index % 8).max().getAsInt();
		assertEquals("split cannon block at " + positions, positions.size(),
			(maxRow - minRow + 1) * (maxColumn - minColumn + 1));
	}

	private static void assertPreserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		Map<Integer, BankItemSnapshot> expected = new LinkedHashMap<>();
		for (BankItemSnapshot item : bank.getItems()) expected.put(item.getItemId(), item);
		List<BankPreviewItem> actual = preview.getCategories().stream().flatMap(category -> category.getItems().stream())
			.collect(Collectors.toList());
		assertEquals("item loss or duplication", expected.size(), actual.size());
		for (BankPreviewItem item : actual)
		{
			assertFalse("artificial bank gap", item.isBlank());
			BankItemSnapshot original = expected.remove(item.getItemId());
			assertTrue("unexpected or duplicate item " + item.getItemId(), original != null);
			assertEquals("quantity of " + item.getItemId(), original.getQuantity(), item.getQuantity());
			assertEquals("placeholder of " + item.getItemId(), original.isPlaceholder(), item.isPlaceholder());
		}
		assertTrue(expected.isEmpty());
	}
}

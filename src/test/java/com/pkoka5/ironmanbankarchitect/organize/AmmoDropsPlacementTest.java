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
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AmmoDropsPlacementTest
{
	// Finished ammo, including poison/enchantment variants and both dart equipment slots.
	// Resource components, quest props and pouches are exercised separately below.
	private static final int[] DROP_AMMO = {
		882, 883, 892, 893, 11212, 11227, 11228, 598, 942, 2532, 2533, 2540, 2541,
		11217, 11222, 21328, 21330, 4160, 9706, 4773, 4778, 4783, 4788, 4793, 4798, 4803,
		2, 21728, 31906, 31908, 31910, 31912, 31914, 31916, 31922, 31930, 31936, 31944,
		877, 878, 879, 880, 881, 6061, 6062, 8882, 9139, 9140, 9141, 9142, 9143, 9144, 9145,
		9236, 9237, 9238, 9239, 9240, 9241, 9242, 9243, 9244, 9245,
		9286, 9287, 9288, 9289, 9290, 9291, 9292, 9293, 9294, 9295, 9296, 9297, 9298, 9299,
		9300, 9301, 9302, 9303, 9304, 9305, 9306, 9335, 9336, 9337, 9338, 9339, 9340, 9341, 9342,
		10158, 10159, 11875, 21316, 21905, 21924, 21926, 21928,
		21932, 21934, 21936, 21938, 21940, 21942, 21944, 21946, 21948, 21950,
		21955, 21957, 21959, 21961, 21963, 21965, 21967, 21969, 21971, 21973, 4740,
		806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 3093, 3094,
		5628, 5629, 5630, 5631, 5632, 5633, 5634, 5635, 5636, 5637, 5638, 5639, 5640, 5641,
		11230, 11231, 11233, 11234, 25849, 25851, 25855, 25857, 28991
	};
	private static final List<Integer> CORRECTABLE = Arrays.asList(892, 2);
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public AmmoDropsPlacementTest(BankPreset preset)
	{
		this.preset = preset;
		this.plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void allFinishedAmmoFamiliesUseDropsWithAndWithoutEquipmentStats()
	{
		BankSnapshot bank = bank(DROP_AMMO);
		for (GearStatsSource stats : Arrays.asList(GearStatsSource.NONE, ammoStats(DROP_AMMO),
			finishedAmmoStats(DROP_AMMO)))
		{
			BankOrganizationPreview preview = build(bank, stats, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
			for (int id : DROP_AMMO) assertPlacement(preview, id, "boss-loot", plan.destinationOf("boss-loot"));
			assertEquals(DROP_AMMO.length, preview.getTagCounts().get("boss-loot").intValue());
			assertEquals(0, preview.getTagCounts().getOrDefault("ammunition", 0).intValue());
			assertPreserved(bank, preview);
		}
		BankLayoutPlan movedDrops = plan.withTagAt("boss-loot", 9);
		BankOrganizationPreview moved = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE,
			movedDrops, BlueprintItemOrders.EMPTY);
		for (int id : DROP_AMMO) assertPlacement(moved, id, "boss-loot", 9);
		assertPreserved(bank, moved);
		BankOrganizationPreview categories = build(bank, finishedAmmoStats(DROP_AMMO), CategoryOverrideSource.NONE,
			null, BlueprintItemOrders.EMPTY);
		assertEquals(DROP_AMMO.length, categories.getCategories().stream()
			.filter(category -> "slayer-boss-loot".equals(category.getCategory().getKey()))
			.mapToInt(BankCategoryPreview::getItemCount).sum());
		for (int id : DROP_AMMO) assertEquals("boss-loot", item(categories, id).getLayoutTagKey());
		assertPreserved(bank, categories);
	}

	@Test
	public void boltsAndWeaponSlotDartsAlsoRespectCorrectionsAndSavedDestinations()
	{
		List<Integer> corrected = Arrays.asList(9243, 806);
		BankSnapshot bank = bank(9243, 806, 28991);
		GearStatsSource stats = finishedAmmoStats(9243, 806, 28991);
		for (String choice : Arrays.asList("gear", "ammunition", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> corrected.contains(id) ? Optional.of(choice) : Optional.empty();
			BankOrganizationPreview preview = build(bank, stats, overrides, plan, BlueprintItemOrders.EMPTY);
			for (int id : corrected)
			{
				String expectedTag = "combat-gear".equals(choice) ? (id == 806 ? "gear" : "ammunition") : choice;
				assertPlacement(preview, id, expectedTag, plan.destinationOf("gear"));
			}
			assertPlacement(preview, 28991, "boss-loot", plan.destinationOf("boss-loot"));
			assertPreserved(bank, preview);
		}
		for (String originalTag : Arrays.asList("ammunition", "boss-loot"))
		{
			BlueprintItemOrders saved = routes(originalTag, "gear", plan.destinationOf("gear"), corrected);
			GearStatsSource savedStats = "ammunition".equals(originalTag) ? ammoStats(9243, 806, 28991) : stats;
			BankOrganizationPreview preview = build(bank, savedStats, CategoryOverrideSource.NONE, plan, saved);
			for (int id : corrected) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
			assertPlacement(preview, 28991, "boss-loot", plan.destinationOf("boss-loot"));
			assertPreserved(bank, preview);
		}
		BankOrganizationPreview reset = build(bank, stats, CategoryOverrideSource.NONE, plan, BlueprintItemOrders.EMPTY);
		for (int id : new int[] {9243, 806, 28991})
			assertPlacement(reset, id, "boss-loot", plan.destinationOf("boss-loot"));
	}

	@Test
	public void aLegacyGearRouteForAWeaponSlotDartSurvivesAndInvalidatingItRestoresDrops()
	{
		BankSnapshot bank = bank(806, 11230);
		GearStatsSource stats = finishedAmmoStats(806, 11230);
		int tools = plan.destinationOf("tools");
		BlueprintItemOrders saved = routes("gear", "tools", tools, Collections.singletonList(806));
		BankOrganizationPreview preview = build(bank, stats, CategoryOverrideSource.NONE, plan, saved);
		assertPlacement(preview, 806, "tools", tools);
		assertPlacement(preview, 11230, "boss-loot", plan.destinationOf("boss-loot"));
		assertPreserved(bank, preview);
		BankLayoutPlan changed = plan.withTagAt("tools", 9);
		BankOrganizationPreview invalidated = build(bank, stats, CategoryOverrideSource.NONE, changed, saved);
		for (int id : new int[] {806, 11230})
			assertPlacement(invalidated, id, "boss-loot", changed.destinationOf("boss-loot"));
		assertPreserved(bank, invalidated);
	}

	@Test
	public void placeholdersKeepTheirDropsDestinationWithoutCountingAsOwnedStock()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(892, 0, 0, true),
			new BankItemSnapshot(2, 0, 1, true), new BankItemSnapshot(11212, 357, 2),
			new BankItemSnapshot(21728, 2400, 3), new BankItemSnapshot(806, 0, 4, true),
			new BankItemSnapshot(9243, 0, 5, true)));
		BankOrganizationPreview preview = build(bank, finishedAmmoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan,
			BlueprintItemOrders.EMPTY);
		for (int id : new int[] {892, 2, 11212, 21728, 806, 9243})
			assertPlacement(preview, id, "boss-loot", plan.destinationOf("boss-loot"));
		assertTrue(item(preview, 892).isPlaceholder());
		assertTrue(item(preview, 2).isPlaceholder());
		assertTrue(item(preview, 806).isPlaceholder());
		assertTrue(item(preview, 9243).isPlaceholder());
		assertEquals(2, preview.getTagCounts().get("boss-loot").intValue());
		assertPreserved(bank, preview);
	}

	@Test
	public void explicitCombatCorrectionsWinAndClearingThemRestoresTheDefaultDrops()
	{
		BankSnapshot bank = bank(892, 2);
		for (String choice : Arrays.asList("gear", "ammunition", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> CORRECTABLE.contains(id) ? Optional.of(choice) : Optional.empty();
			BankOrganizationPreview preview = build(bank, ammoStats(DROP_AMMO), overrides, plan, BlueprintItemOrders.EMPTY);
			String expectedTag = "combat-gear".equals(choice) ? "ammunition" : choice;
			for (int id : CORRECTABLE) assertPlacement(preview, id, expectedTag, plan.destinationOf("gear"));
			assertPreserved(bank, preview);
		}
		BankOrganizationPreview cleared = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan,
			BlueprintItemOrders.EMPTY);
		for (int id : CORRECTABLE) assertPlacement(cleared, id, "boss-loot", plan.destinationOf("boss-loot"));
	}

	@Test
	public void validLegacyEditorDestinationsSurviveReloadAndInvalidatedRoutesReleaseToDrops()
	{
		BankSnapshot bank = bank(892, 2, 11212);
		BlueprintItemOrders saved = routes("ammunition", "gear", plan.destinationOf("gear"), CORRECTABLE);
		BankOrganizationPreview preview = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan, saved);
		for (int id : CORRECTABLE) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
		assertPlacement(preview, 11212, "boss-loot", plan.destinationOf("boss-loot"));
		assertPreserved(bank, preview);
		BankLayoutPlan changed = plan.withTagAt("gear", 9);
		BankOrganizationPreview released = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, changed, saved);
		for (int id : CORRECTABLE) assertPlacement(released, id, "boss-loot", changed.destinationOf("boss-loot"));
		assertPreserved(bank, released);
	}

	@Test
	public void aNewDropsToCombatEditorMoveSurvivesReloadAndResetRestoresDrops()
	{
		BankSnapshot bank = bank(892, 2, 11212);
		BlueprintItemOrders saved = routes("boss-loot", "gear", plan.destinationOf("gear"), CORRECTABLE);
		BankOrganizationPreview preview = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan, saved);
		for (int id : CORRECTABLE) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
		assertPlacement(preview, 11212, "boss-loot", plan.destinationOf("boss-loot"));
		assertPreserved(bank, preview);
		BankOrganizationPreview reset = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan,
			saved.resetTab(plan.destinationOf("gear")));
		for (int id : CORRECTABLE) assertPlacement(reset, id, "boss-loot", plan.destinationOf("boss-loot"));
	}

	@Test
	public void movingTheWholeAmmunitionTagKeepsThePlayersExistingGroup()
	{
		BankSnapshot bank = bank(892, 2, 8882, 806, 9433);
		BankLayoutPlan custom = BankLayoutPlan.parse(preset, plan.withTagAt("ammunition", 9).serialize());
		BankOrganizationPreview preview = build(bank, ammoStats(892, 2, 8882, 806), CategoryOverrideSource.NONE, custom,
			BlueprintItemOrders.EMPTY);
		for (int id : new int[] {892, 2, 8882, 806, 9433}) assertPlacement(preview, id, "ammunition", 9);
		assertEquals(5, preview.getTagCounts().get("ammunition").intValue());
		assertPreserved(bank, preview);
	}

	@Test
	public void capturedPhysicalChoicesSurviveBothOldAmmunitionAndNewDropsTags()
	{
		BankSnapshot bank = bank(892, 2);
		for (boolean legacy : Arrays.asList(false, true))
		{
			CategoryOverrideSource oldAmmo = legacy ? id -> Optional.of("ammunition") : CategoryOverrideSource.NONE;
			BankOrganizationPreview before = build(bank, ammoStats(DROP_AMMO), oldAmmo, plan, BlueprintItemOrders.EMPTY);
			int physicalTab = legacy ? 9 : plan.destinationOf("gear");
			BlueprintItemOrders captured = BlueprintItemOrders.parse(BlueprintItemOrders.capture(
				Collections.singletonMap(physicalTab, Arrays.asList(2, 892)), before,
				BlueprintItemOrders.EMPTY).serialize());
			assertTrue(captured.isCaptured());
			BankOrganizationPreview after = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, plan, captured);
			String retainedTag = legacy ? "ammunition" : "boss-loot";
			for (int id : CORRECTABLE) assertPlacement(after, id, retainedTag, physicalTab);
			assertEquals(Arrays.asList(2, 892), after.getCategories().get(physicalTab).getItems().stream()
				.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
			assertPreserved(bank, after);
		}
	}

	@Test
	public void aStaleEditorRouteDoesNotInheritAnUnrelatedCapturedDestination()
	{
		BankSnapshot bank = bank(892, 2);
		BankOrganizationPreview before = build(bank, ammoStats(DROP_AMMO), id -> Optional.of("ammunition"),
			plan, BlueprintItemOrders.EMPTY);
		int combat = plan.destinationOf("gear");
		BlueprintItemOrders captured = BlueprintItemOrders.capture(
			Collections.singletonMap(combat, Arrays.asList(2, 892)), before, BlueprintItemOrders.EMPTY)
			.withDestinations(Collections.singletonMap("892#0",
				new BlueprintItemOrders.Destination(combat, "ammunition", "gear")));
		captured = BlueprintItemOrders.parse(captured.serialize());
		BankLayoutPlan changed = plan.withTagAt("gear", 9);
		BankOrganizationPreview after = build(bank, ammoStats(DROP_AMMO), CategoryOverrideSource.NONE, changed, captured);
		assertPlacement(after, 892, "boss-loot", changed.destinationOf("boss-loot"));
		assertPlacement(after, 2, "ammunition", combat);
		assertPreserved(bank, after);
	}

	@Test
	public void blessingsPouchGrappleThrownWeaponsComponentsAndQuestPropsKeepTheirRoles()
	{
		BankSnapshot bank = bank(20220, 9433, 9419, 865, 825, 6, 8, 10, 12,
			52, 53, 44, 9187, 822, 823, 9375, 11876, 11887, 687, 1849, 1853, 8790, 31475, 31472);
		BankOrganizationPreview preview = build(bank, ammoStats(20220), CategoryOverrideSource.NONE, plan,
			BlueprintItemOrders.EMPTY);
		for (int id : new int[] {20220, 9433})
			assertPlacement(preview, id, "ammunition", plan.destinationOf("ammunition"));
		assertPlacement(preview, 9419, "tools", plan.destinationOf("tools"));
		assertPlacement(preview, 865, "gear", plan.destinationOf("gear"));
		assertPlacement(preview, 825, "gear", plan.destinationOf("gear"));
		for (int id : new int[] {6, 8, 10, 12}) assertPlacement(preview, id, "gear", plan.destinationOf("gear"));
		for (int id : new int[] {52, 53, 44, 9187, 822, 823, 9375, 11876, 11887})
			assertPlacement(preview, id, "ammo-components", plan.destinationOf("ammo-components"));
		assertPlacement(preview, 687, "cleanup", plan.destinationOf("cleanup"));
		for (int id : new int[] {1849, 1853})
			assertPlacement(preview, id, "quest-items", plan.destinationOf("quest-items"));
		for (int id : new int[] {8790, 31475, 31472})
			assertPlacement(preview, id, "raw-resources", plan.destinationOf("raw-resources"));
		assertFalse(preview.getTagCounts().containsKey("boss-loot"));
		assertPreserved(bank, preview);
	}

	@Test
	public void shuffledDropsKeepLootAmmoFamiliesAndAlchInSeparateRuns()
	{
		BankSnapshot bank = mixedDropsBank();
		for (GearStatsSource stats : Arrays.asList(GearStatsSource.NONE, finishedAmmoStats(DROP_AMMO)))
		{
			BankOrganizationPreview preview = build(bank, stats, sortingOverrides(), plan, BlueprintItemOrders.EMPTY);
			assertEquals(Arrays.asList(4207, 19677, 20718, 989,
				11212, 892, 890, 882,
				9243, 21905, 9144, 9143, 8882,
				11230, 25849, 811, 810, 806, 28991,
				31916, 31914, 31912, 31910, 2, 31908,
				20714, 1373, 1163, 1093), dropsIds(preview));
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void customAlchTagOrderIsRespectedWithoutScatteringAmmo()
	{
		BankSnapshot bank = mixedDropsBank();
		BankLayoutPlan custom = BankLayoutPlan.parse(preset, plan.withTagShifted("alch", -1).serialize());
		BankOrganizationPreview preview = build(bank, finishedAmmoStats(DROP_AMMO), sortingOverrides(), custom,
			BlueprintItemOrders.EMPTY);
		assertEquals(Arrays.asList(1373, 1163, 1093), dropsIds(preview).subList(0, 3));
		assertEquals(Arrays.asList(11212, 892, 890, 882, 9243, 21905, 9144, 9143, 8882,
			11230, 25849, 811, 810, 806, 28991, 31916, 31914, 31912, 31910, 2, 31908),
			dropsIds(preview).subList(7, 28));
		assertPreserved(bank, preview);
	}

	@Test
	public void savedDropsItemOrderWinsOverTheDefaultAmmoGroupsAfterReload()
	{
		BankSnapshot bank = mixedDropsBank();
		BankOrganizationPreview initial = build(bank, finishedAmmoStats(DROP_AMMO), sortingOverrides(), plan,
			BlueprintItemOrders.EMPTY);
		List<Integer> personal = new ArrayList<>(dropsIds(initial));
		Collections.reverse(personal);
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("boss-loot"), personal).serialize());
		BankOrganizationPreview preview = build(bank, finishedAmmoStats(DROP_AMMO), sortingOverrides(), plan, saved);
		assertEquals(personal, dropsIds(preview));
		assertTrue(preview.getCategories().get(plan.destinationOf("boss-loot")).hasManualOrder());
		assertPreserved(bank, preview);
	}

	@Test
	public void savedDropsBlockOrderWinsOverTheDefaultAmmoGroupsAfterReload()
	{
		BankSnapshot bank = mixedDropsBank();
		BlockArrangements blocks = BlockArrangements.parse(BlockArrangements.EMPTY
			.withTag("boss-loot", Arrays.asList("item:2", "item:882")).serialize());
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, finishedAmmoStats(DROP_AMMO), ItemValueSource.NONE, sortingOverrides(), plan,
			BankLayoutOptions.defaultFor(preset).withBlockArrangements(blocks));
		List<Integer> arranged = dropsIds(preview);
		assertEquals(Integer.valueOf(2), arranged.get(0));
		assertTrue(arranged.indexOf(2) < arranged.indexOf(882));
		List<BankBlockDescriptor> descriptors = preview.getBlockDescriptors().get("boss-loot");
		assertEquals(Arrays.asList("item:2", "item:882"), descriptors.stream()
			.map(BankBlockDescriptor::getBlockKey).filter(key -> "item:2".equals(key) || "item:882".equals(key))
			.collect(Collectors.toList()));
		assertEquals(26, descriptors.stream().mapToInt(BankBlockDescriptor::getMemberCount).sum());
		assertPreserved(bank, preview);
	}

	@Test
	public void expensiveAmmoRemainsInItsAmmoGroupDespiteStrongerStacksAndManualAlchStillWins()
	{
		// Official RuneLite item-stats and Wiki mapping cache, retrieved 2026-10-09:
		// Onyx bolts (e) have 120 ranged strength and a 9,000 high-alch value;
		// these three Dragon-bolt variants have 122 ranged strength.
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(9245, 500, 0),
			new BankItemSnapshot(21905, 1000, 1), new BankItemSnapshot(21946, 150, 2),
			new BankItemSnapshot(21950, 50, 3), new BankItemSnapshot(892, 1000, 4),
			new BankItemSnapshot(11212, 400, 5), new BankItemSnapshot(2, 2400, 6)));
		Map<Integer, GearStats> ammunition = new LinkedHashMap<>();
		ammunition.put(9245, fullAmmoStats(120));
		for (int id : new int[] {21905, 21946, 21950}) ammunition.put(id, fullAmmoStats(122));
		ammunition.put(892, fullAmmoStats(49));
		ammunition.put(11212, fullAmmoStats(60));
		GearStatsSource stats = id -> Optional.ofNullable(ammunition.get(id));
		Map<Integer, Integer> alchValues = new LinkedHashMap<>();
		alchValues.put(9245, 9000);
		alchValues.put(21905, 255);
		alchValues.put(21946, 528);
		alchValues.put(21950, 9480);
		alchValues.put(892, 240);
		alchValues.put(11212, 480);
		alchValues.put(2, 3);
		ItemValueSource values = id -> alchValues.getOrDefault(id, 0);
		BankPreviewItem onyx = new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(9245).get(), 500);
		for (int id : new int[] {21905, 21946, 21950})
		{
			assertTrue(ammunition.get(id).dominates(ammunition.get(9245)));
			assertTrue(GearItemSorter.score(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(id).get(), 50),
				stats) > GearItemSorter.score(onyx, stats));
		}
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, stats, values, CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
		assertEquals(Arrays.asList(11212, 892, 21946, 21905, 21950, 9245, 2), dropsIds(preview));
		for (BankItemSnapshot source : bank.getItems())
			assertPlacement(preview, source.getItemId(), "boss-loot", plan.destinationOf("boss-loot"));
		assertFalse(preview.getTagCounts().containsKey("alch"));
		assertPreserved(bank, preview);
		BankLayoutPlan relocated = plan.withTagAt("ammunition", 9);
		BankOrganizationPreview custom = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, stats, values, CategoryOverrideSource.NONE, relocated, BankLayoutOptions.defaultFor(preset));
		for (BankItemSnapshot source : bank.getItems())
			assertPlacement(custom, source.getItemId(), "ammunition", 9);
		assertFalse(custom.getTagCounts().containsKey("alch"));
		assertPreserved(bank, custom);
		BankOrganizationPreview corrected = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, stats, values, id -> id == 9245 ? Optional.of("alch") : Optional.empty(), plan,
			BankLayoutOptions.defaultFor(preset));
		assertPlacement(corrected, 9245, "alch", plan.destinationOf("alch"));
		assertEquals(Arrays.asList(11212, 892, 21946, 21905, 21950, 2, 9245), dropsIds(corrected));
		assertEquals(1, corrected.getTagCounts().get("alch").intValue());
		assertPreserved(bank, corrected);
	}

	private static BankSnapshot mixedDropsBank()
	{
		return bank(1163, 882, 28991, 31908, 8882, 4207, 806, 31912, 1373, 892, 20718, 811,
			31914, 9143, 20714, 11212, 25849, 2, 989, 21905, 810, 31916, 1093, 890, 9243,
			11230, 19677, 31910, 9144);
	}

	private static CategoryOverrideSource sortingOverrides()
	{
		return id -> Arrays.asList(1163, 1373, 1093).contains(id) ? Optional.of("alch")
			: id == 20714 ? Optional.of("boss-loot") : Optional.empty();
	}

	private List<Integer> dropsIds(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("boss-loot")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private BankOrganizationPreview build(BankSnapshot bank, GearStatsSource stats, CategoryOverrideSource overrides,
		BankLayoutPlan layout, BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset, stats,
			ItemValueSource.NONE, overrides, layout, BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
	}

	private static BlueprintItemOrders routes(String originalTag, String targetTag, int tab, List<Integer> ids)
	{
		Map<String, BlueprintItemOrders.Destination> routes = new LinkedHashMap<>();
		for (int id : ids) routes.put(id + "#0", new BlueprintItemOrders.Destination(tab, originalTag, targetTag));
		return BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY.withDestinations(routes).serialize());
	}

	private static GearStatsSource ammoStats(int... ids)
	{
		List<Integer> supported = Arrays.stream(ids).boxed().collect(Collectors.toList());
		GearStats ammo = new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0, 0);
		return id -> supported.contains(id) ? Optional.of(ammo) : Optional.empty();
	}

	private static GearStatsSource finishedAmmoStats(int... ids)
	{
		List<Integer> supported = Arrays.stream(ids).boxed().collect(Collectors.toList());
		GearStats ammo = new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0, 0);
		GearStats dart = new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 5, 0, 6, 0, 0);
		return id -> !supported.contains(id) ? Optional.empty()
			: Optional.of(id != 28991 && CompositeItemCatalog.DEFAULT.findById(id).get().getDisplayName()
				.toLowerCase(java.util.Locale.ROOT).contains("dart") ? dart : ammo);
	}

	private static GearStats fullAmmoStats(int rangedStrength)
	{
		return new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, rangedStrength, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> bank = new ArrayList<>();
		for (int id : ids) bank.add(new BankItemSnapshot(id, id % 17 + 1, bank.size()));
		return new BankSnapshot(bank);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("Missing item " + id));
	}

	private static void assertPlacement(BankOrganizationPreview preview, int id, String tag, int tab)
	{
		assertEquals("tag for " + id, tag, item(preview, id).getLayoutTagKey());
		assertTrue("destination for " + id, preview.getCategories().get(tab).getItems().stream()
			.anyMatch(item -> item.getItemId() == id));
	}

	private static void assertPreserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getItems().size(), preview.getPlannedItems().size());
		for (BankItemSnapshot source : bank.getItems())
		{
			BankPreviewItem actual = item(preview, source.getItemId());
			assertEquals("quantity for " + source.getItemId(), source.getQuantity(), actual.getQuantity());
			assertEquals("placeholder for " + source.getItemId(), source.isPlaceholder(), actual.isPlaceholder());
			assertEquals(source.getPhysicalSlotQuantities(), actual.physicalBankSlots().stream()
				.map(BankPreviewItem::getQuantity).collect(Collectors.toList()));
		}
	}
}

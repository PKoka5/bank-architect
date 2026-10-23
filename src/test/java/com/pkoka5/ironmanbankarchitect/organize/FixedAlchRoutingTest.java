package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Fixed is an unconditional exact list. Synthetic vectors test policy, not equipment balance or DPS. */
@RunWith(Parameterized.class)
public class FixedAlchRoutingTest
{
	private static final int STOCK = 990001;
	private static final int UPGRADE = 990002;
	private static final ItemValueSource VALUES = id -> 39000;
	private static final ItemCatalog CATALOG = id -> Optional.of(CompositeItemCatalog.DEFAULT.findById(id)
		.orElseGet(() -> new CatalogItem(id, "Synthetic gear " + id, ItemCategory.GEAR,
			"gear", Collections.emptySet(), null)));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public FixedAlchRoutingTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void everyExactFixedItemGoesToAlchWithoutStatsValueOrReplacement()
	{
		List<Integer> ids = fixedIds();
		assertEquals("Reviewed list size", 105, ids.size());
		assertEquals("Every list entry is a distinct exact ID", 105, new LinkedHashSet<>(ids).size());
		for (int id : ids)
			for (int quantity : new int[] {0, 1, 25})
			{
				BankOrganizationPreview preview = build(bank(entry(id, quantity, 0)), Collections.emptyMap(),
					ItemValueSource.NONE, CategoryOverrideSource.NONE, fixed());
				assertTag(preview, id, "alch");
				assertAt(preview, id, plan.destinationOf("alch"));
			}
	}

	@Test public void fixedListBypassesProtectedCatalogFunctionsAndNegativeValueMetadata()
	{
		for (String role : Arrays.asList("special-attack", "quest-use", "clue-required", "skilling-outfit"))
		{
			ItemCatalog protectedRole = id -> Optional.of(new CatalogItem(id, "Protected gear",
				ItemCategory.GEAR, "body", Collections.singleton(role), null));
			assertTag(build(bank(entry(1121, 1, 0)), protectedRole, Collections.emptyMap(), id -> -1,
				CategoryOverrideSource.NONE, fixed()), 1121, "alch");
		}
	}

	@Test public void ordinaryDragonScimitarAndHalberdUseFixedAlchWhilePlayerChoicesAndVariantsStayDistinct()
	{
		for (int id : new int[] {4587, 3204})
			for (int quantity : new int[] {1, 0})
			{
				BankSnapshot bank = bank(entry(id, quantity, 0));
				assertTag(build(bank, Collections.emptyMap(), ItemValueSource.NONE,
					CategoryOverrideSource.NONE, fixed()), id, "alch");
				assertTag(build(bank, Collections.emptyMap(), ItemValueSource.NONE,
					candidate -> candidate == id ? Optional.of("gear") : Optional.empty(), fixed()), id, "gear");
			}
		for (int variant : new int[] {20000, 20002, 20406, 28031, 28049})
		{
			assertFalse("Unlisted variant " + variant, fixedIds().contains(variant));
			assertNotAlch(build(bank(entry(variant, 1, 0)), Collections.emptyMap(), ItemValueSource.NONE,
				CategoryOverrideSource.NONE, fixed()), variant);
		}
	}

	@Test public void fixedRoutingDoesNotChangeWhenStatsAreIncompleteOrHaveTradeoffs()
	{
		Map<Integer, GearStats> stats = meleePair(1121, 11832, GearSlot.BODY);
		stats.put(11832, new GearStats(GearSlot.BODY, 20, 20, 20, 0, 0, 20, 0, 0,
			20, 20, 20, 9, 20, 0, 0));
		assertFalse(stats.get(11832).dominates(stats.get(1121)));
		assertTag(build(pair(1121, 25, 11832, 1), stats, fixed()), 1121, "alch");
		stats.put(1121, new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, 100));
		assertTag(build(pair(1121, 25, 11832, 1), stats, fixed()), 1121, "alch");
	}

	@Test public void fixedModeDoesNotUseFlexibleBulkFallbackOutsideTheExactList()
	{
		BankSnapshot bank = pair(STOCK, 25, UPGRADE, 1);
		Map<Integer, GearStats> stats = meleePair(STOCK, UPGRADE, GearSlot.BODY);
		assertFalse(fixedIds().contains(STOCK));
		assertTag(build(bank, stats, flexible()), STOCK, "alch");
		assertTag(build(bank, stats, fixed()), STOCK, "gear");
	}

	@Test public void notedVariantsOrnamentVariantsAndComponentsDoNotEnterByName()
	{
		for (int id : new int[] {1128, 3481, 1391, STOCK})
		{
			assertFalse("Exact variant " + id, fixedIds().contains(id));
			ItemCatalog collision = candidate -> Optional.of(new CatalogItem(candidate, "Rune platebody",
				ItemCategory.GEAR, "body", Collections.emptySet(), null));
			assertNotAlch(build(pair(id, 25, UPGRADE, 1), collision,
				meleePair(id, UPGRADE, GearSlot.BODY), VALUES, CategoryOverrideSource.NONE, fixed()), id);
		}
	}

	@Test public void flexibleModeRetainsTheBestRecordedGearAndTools()
	{
		for (int id : new int[] {1127, 2503, 1275, 1359, 1393, 4093, 9185})
			for (int quantity : new int[] {0, 1, 25})
				assertNotAlch(build(bank(entry(id, quantity, 0)), Collections.emptyMap(), flexible()), id);
	}

	@Test public void flexibleModeRetainsSpecialAttackWeaponsWithDominatingAlternatives()
	{
		for (int id : new int[] {1215, 1305, 1377, 3101, 859})
		{
			Map<Integer, GearStats> stats = meleePair(id, UPGRADE, GearSlot.WEAPON);
			if (id == 859)
			{
				stats.put(id, ranged(GearSlot.WEAPON, 10, 10));
				stats.put(UPGRADE, ranged(GearSlot.WEAPON, 50, 50));
			}
			assertTrue(stats.get(UPGRADE).dominates(stats.get(id)));
			assertNotAlch(build(pair(id, 25, UPGRADE, 1), stats, flexible()), id);
			assertTag(build(pair(id, 25, UPGRADE, 1), stats, fixed()), id, "alch");
		}
	}

	@Test public void flexibleDhideStillRequiresFullDominanceRatherThanOnlyAHigherTier()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(2503, ranged(GearSlot.BODY, 20, 20));
		stats.put(27229, ranged(GearSlot.BODY, 50, 19));
		assertFalse(stats.get(27229).dominates(stats.get(2503)));
		assertTag(build(pair(2503, 25, 27229, 1), stats, flexible()), 2503, "gear");
		assertTag(build(pair(2503, 25, 27229, 1), stats, fixed()), 2503, "alch");
		stats.put(27229, ranged(GearSlot.BODY, 50, 50));
		assertTag(build(pair(2503, 25, 27229, 1), stats, flexible()), 2503, "alch");
	}

	@Test public void flexibleCombinationStavesKeepTheirRuneSupply()
	{
		for (int candidate : new int[] {3053, 6562})
			for (int replacement : new int[] {1401, 1403, 4675, candidate == 3053 ? 6563 : 3054})
			{
				Map<Integer, GearStats> stats = new LinkedHashMap<>();
				stats.put(candidate, magic(10));
				stats.put(replacement, magic(20));
				assertTrue(stats.get(replacement).dominates(stats.get(candidate)));
				assertTag(build(pair(candidate, 25, replacement, 1), stats, flexible()), candidate, "gear");
				assertTag(build(pair(candidate, 25, replacement, 1), stats, fixed()), candidate, "alch");
			}
	}

	@Test public void onyxBoltsUseTheExplicitFixedListWhileFlexibleKeepsTheirAmmunitionRole()
	{
		BankSnapshot bank = bank(entry(9245, 100, 0));
		assertTag(build(bank, Collections.emptyMap(), flexible()), 9245, "boss-loot");
		assertTag(build(bank, Collections.emptyMap(), fixed()), 9245, "alch");
	}

	@Test public void scurriusCraftingIngredientProtectionIsSpecificToFlexibleMode()
	{
		Map<Integer, GearStats> stats = meleePair(1432, 1434, GearSlot.WEAPON);
		BankSnapshot unfinished = bank(entry(1432, 1, 0), entry(1434, 1, 1), entry(28798, 1, 2));
		assertTag(build(unfinished, stats, flexible()), 1432, "gear");
		assertTag(build(unfinished, stats, fixed()), 1432, "alch");
		BankSnapshot finished = bank(entry(1432, 1, 0), entry(1434, 1, 1), entry(28798, 1, 2), entry(28792, 0, 3));
		assertTag(build(finished, stats, flexible()), 1432, "alch");
	}

	@Test public void flexibleToolAndStaffReplacementGuardsRemainActive()
	{
		for (int[] pair : new int[][] {{1275, 11920}, {1359, 6739}, {1393, 1401}})
		{
			assertNotAlch(build(bank(entry(pair[0], 25, 0)), Collections.emptyMap(), flexible()), pair[0]);
			BankSnapshot upgraded = pair(pair[0], 25, pair[1], 0);
			assertTag(build(upgraded, Collections.emptyMap(), flexible()), pair[0], "alch");
			assertNotAlch(build(upgraded, Collections.emptyMap(), ItemValueSource.NONE,
				CategoryOverrideSource.NONE, flexible()), pair[0]);
		}
	}

	@Test public void disablingAlchGatheringOverridesBothModesForEveryFixedItem()
	{
		for (AlchMode mode : AlchMode.values())
		{
			BankLayoutOptions disabled = new BankLayoutOptions(true, true, false).withAlchMode(mode);
			for (int id : fixedIds())
				assertNotAlch(build(bank(entry(id, 1, 0)), Collections.emptyMap(), disabled), id);
		}
	}

	@Test public void manualTagsOverrideFixedMembershipAndExclusion()
	{
		BankSnapshot bank = bank(entry(1121, 25, 0), entry(9245, 100, 1), entry(STOCK, 1, 2));
		Map<Integer, String> tags = new LinkedHashMap<>();
		tags.put(1121, "gear");
		tags.put(9245, "boss-loot");
		tags.put(STOCK, "alch");
		BankOrganizationPreview preview = build(bank, Collections.emptyMap(), ItemValueSource.NONE,
			id -> Optional.ofNullable(tags.get(id)), fixed());
		assertTag(preview, 1121, "gear");
		assertAt(preview, 1121, plan.destinationOf("gear"));
		assertTag(preview, 9245, "boss-loot");
		assertTag(preview, STOCK, "alch");
	}

	@Test public void savedEditorGearDestinationWinsOverFixedAutomaticAlch()
	{
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"1121#0", new BlueprintItemOrders.Destination(plan.destinationOf("gear"), "alch", "gear")));
		BankOrganizationPreview preview = build(bank(entry(1121, 0, 0)), Collections.emptyMap(),
			fixed().withItemOrders(orders));
		assertTag(preview, 1121, "gear");
		assertAt(preview, 1121, plan.destinationOf("gear"));
	}

	@Test public void anEditorRouteCreatedFromGearRemainsActiveWhenFixedChangesTheAutomaticTag()
	{
		int target = plan.destinationOf("tools");
		BlueprintItemOrders saved = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"9185#0", new BlueprintItemOrders.Destination(target, "gear", "tools")));
		for (BankLayoutOptions options : Arrays.asList(flexible(), fixed()))
		{
			BankOrganizationPreview preview = build(bank(entry(9185, 1, 0)), Collections.emptyMap(),
				options.withItemOrders(saved));
			assertTag(preview, 9185, "tools");
			assertAt(preview, 9185, target);
		}
	}

	@Test public void anEditorAmmunitionRouteCanKeepOnyxBoltsInCombatAcrossTheModeChange()
	{
		int target = plan.destinationOf("gear");
		BlueprintItemOrders saved = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"9245#0", new BlueprintItemOrders.Destination(target, "ammunition", "gear")));
		for (BankLayoutOptions options : Arrays.asList(flexible(), fixed()))
		{
			BankOrganizationPreview preview = build(bank(entry(9245, 25, 0)), Collections.emptyMap(),
				options.withItemOrders(saved));
			assertTag(preview, 9245, "gear");
			assertAt(preview, 9245, target);
		}
	}

	@Test public void capturedTargetTagsRemainActiveWhenFixedChangesTheAutomaticTag()
	{
		int target = plan.destinationOf("tools");
		BlueprintItemOrders captured = BlueprintItemOrders.parse("v2|c:9185#0:" + target + ":gear:tools");
		assertTrue(captured.isCaptured());
		BankOrganizationPreview preview = build(bank(entry(9185, 0, 0)), Collections.emptyMap(),
			fixed().withItemOrders(captured));
		assertTag(preview, 9185, "tools");
		assertAt(preview, 9185, target);
	}

	@Test public void dormantEditorRoutesDoNotBecomeActiveMerelyBecauseFixedIsSelected()
	{
		int wrongTarget = (plan.destinationOf("tools") + 1) % BankLayoutPlan.DESTINATION_COUNT;
		BlueprintItemOrders dormant = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"9185#0", new BlueprintItemOrders.Destination(wrongTarget, "gear", "tools")));
		BankOrganizationPreview preview = build(bank(entry(9185, 1, 0)), Collections.emptyMap(),
			fixed().withItemOrders(dormant));
		assertTag(preview, 9185, "alch");
		assertAt(preview, 9185, plan.destinationOf("alch"));
	}

	@Test public void aNewExplicitCorrectionWinsOverAnOlderSavedRouteDuringTheModeChange()
	{
		int target = plan.destinationOf("tools");
		BlueprintItemOrders saved = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"9185#0", new BlueprintItemOrders.Destination(target, "gear", "tools")));
		BlueprintItemOrders captured = BlueprintItemOrders.parse("v2|c:9185#0:" + target + ":gear:tools");
		for (String tag : Arrays.asList("alch", "quest-items"))
			for (BlueprintItemOrders orders : Arrays.asList(saved, captured))
			{
				BankOrganizationPreview preview = build(bank(entry(9185, 1, 0)), Collections.emptyMap(),
					ItemValueSource.NONE, id -> Optional.of(tag), fixed().withItemOrders(orders));
				assertTag(preview, 9185, tag);
				assertAt(preview, 9185, orders.isCaptured() ? target : plan.destinationOf(tag));
			}
	}

	@Test public void fixedOnyxRoutingUsesTheAlchDestinationEvenWhenAmmunitionHasItsOwnMovedTag()
	{
		int alchTarget = (plan.destinationOf("alch") + 1) % BankLayoutPlan.DESTINATION_COUNT;
		int ammoTarget = (alchTarget + 1) % BankLayoutPlan.DESTINATION_COUNT;
		BankLayoutPlan changed = plan.withTagAt("alch", alchTarget).withTagAt("ammunition", ammoTarget);
		BankSnapshot bank = bank(entry(9245, 25, 0));
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CATALOG, preset,
			GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, changed, fixed());
		assertContents(bank, preview);
		assertTag(preview, 9245, "alch");
		assertAt(preview, 9245, alchTarget);
	}

	@Test public void capturedPhysicalPlacementSurvivesEnablingFixedModeAndWithdrawal()
	{
		BankSnapshot original = pair(1121, 1, 9185, 1);
		BankOrganizationPreview before = build(original, Collections.emptyMap(), flexible());
		int chosenTab = 8;
		BlueprintItemOrders orders = BlueprintItemOrders.capture(Collections.singletonMap(chosenTab,
			Arrays.asList(9185, 1121)), before, BlueprintItemOrders.EMPTY);
		assertTrue(orders.isCaptured());
		for (int quantity : new int[] {25, 1, 0})
		{
			BankOrganizationPreview after = build(pair(1121, quantity, 9185, 0), Collections.emptyMap(),
				fixed().withItemOrders(orders));
			assertEquals(Arrays.asList(9185, 1121), after.getCategories().get(chosenTab).getItems().stream()
				.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		}
	}

	@Test public void modeSwitchesInvalidateWarmRetainedDecisionsForMembersAndNonmembers()
	{
		BankSnapshot bank = bank(entry(1121, 1, 0), entry(STOCK, 25, 1), entry(UPGRADE, 1, 2), entry(995, 0, 3));
		Session session = new Session(meleePair(STOCK, UPGRADE, GearSlot.BODY));
		for (BankLayoutOptions options : Arrays.asList(fixed(), flexible(), fixed(), flexible()))
		{
			BankOrganizationPreview preview = session.analyze(bank, options);
			boolean fixed = options.alchMode() == AlchMode.FIXED;
			assertTag(preview, 1121, fixed ? "alch" : "gear");
			assertTag(preview, STOCK, fixed ? "gear" : "alch");
		}
	}

	@Test public void fixedWarmRoutingDoesNotDependOnRecordedOrReleasedCrossbowUpgrades()
	{
		Session session = new Session(Collections.emptyMap());
		BankSnapshot recorded = bank(entry(9185, 3, 0), entry(21902, 0, 1), entry(995, 0, 2));
		assertTag(session.analyze(recorded, fixed()), 9185, "alch");
		for (int quantity : new int[] {3, 1, 0, 25})
		{
			BankSnapshot released = bank(entry(9185, quantity, 0), entry(995, 0, 1));
			assertTag(session.analyze(released, fixed()), 9185, "alch");
		}
	}

	@Test public void fixedRoutingPreservesMultiplePhysicalCopiesAndASeparatePlaceholder()
	{
		BankSnapshot bank = bank(entry(1393, 1, 0), entry(1393, 1, 1), entry(1401, 0, 2), entry(995, 0, 3));
		BankOrganizationPreview preview = build(bank, Collections.emptyMap(), fixed());
		assertTag(preview, 1393, "alch");
		assertEquals(Arrays.asList(1, 1), preview.getPlannedItems().stream()
			.filter(planned -> planned.getItemId() == 1393).map(BankPreviewItem::getQuantity).collect(Collectors.toList()));
	}

	private BankLayoutOptions fixed() { return BankLayoutOptions.defaultFor(preset).withAlchMode(AlchMode.FIXED); }
	private BankLayoutOptions flexible() { return BankLayoutOptions.defaultFor(preset).withAlchMode(AlchMode.FLEXIBLE); }

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats, BankLayoutOptions options)
	{
		return build(bank, stats, VALUES, CategoryOverrideSource.NONE, options);
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats, ItemValueSource values,
		CategoryOverrideSource choices, BankLayoutOptions options)
	{
		return build(bank, CATALOG, stats, values, choices, options);
	}

	private BankOrganizationPreview build(BankSnapshot bank, ItemCatalog catalog, Map<Integer, GearStats> stats,
		ItemValueSource values, CategoryOverrideSource choices, BankLayoutOptions options)
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
			id -> Optional.ofNullable(stats.get(id)), values, choices, plan, options);
		assertContents(bank, preview);
		return preview;
	}

	private static void assertContents(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItems().size());
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		Map<String, Long> expected = bank.getPhysicalItems().stream().collect(Collectors.groupingBy(
			entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(),
			LinkedHashMap::new, Collectors.counting()));
		Map<String, Long> actual = preview.getPlannedItems().stream().collect(Collectors.groupingBy(
			entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(),
			LinkedHashMap::new, Collectors.counting()));
		assertEquals("Exact physical ID, quantity, placeholder and occurrence counts", expected, actual);
	}

	private static List<Integer> fixedIds()
	{
		InputStream input = FixedAlchRoutingTest.class.getResourceAsStream("alch-candidates.tsv");
		assertNotNull("The reviewed list resource is required", input);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)))
		{
			String row = reader.lines().filter(line -> line.startsWith("fixed-list\t"))
				.findFirst().orElseThrow(AssertionError::new);
			return Arrays.stream(row.split("\t", 2)[1].split(",")).map(Integer::parseInt).collect(Collectors.toList());
		}
		catch (IOException ex) { throw new AssertionError(ex); }
	}

	private static Map<Integer, GearStats> meleePair(int candidate, int upgrade, GearSlot slot)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(candidate, melee(slot, 10));
		stats.put(upgrade, melee(slot, 20));
		return stats;
	}

	private static GearStats melee(GearSlot slot, int value)
	{
		return new GearStats(slot, value, value, value, 0, 0, value, 0, 0,
			value, value, value, value, value, 0, slot == GearSlot.WEAPON ? 4 : 0);
	}

	private static GearStats ranged(GearSlot slot, int attack, int defence)
	{
		return new GearStats(slot, 0, 0, 0, -10, attack, 0, 0, 0,
			defence, defence, defence, defence, defence, 0, slot == GearSlot.WEAPON ? 4 : 0);
	}

	private static GearStats magic(int value)
	{
		return new GearStats(GearSlot.WEAPON, 0, 0, 0, value, 0, 0, 0, 0,
			value, value, value, value, value, 0, 4);
	}

	private static BankSnapshot pair(int candidate, int quantity, int upgrade, int replacementQuantity)
	{
		return bank(entry(candidate, quantity, 0), entry(upgrade, replacementQuantity, 1));
	}

	private static BankSnapshot bank(BankItemSnapshot... items) { return new BankSnapshot(Arrays.asList(items)); }
	private static BankItemSnapshot entry(int id, int quantity, int slot)
	{
		return new BankItemSnapshot(id, quantity, slot, quantity == 0);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("Item " + id, tag, item(preview, id).getLayoutTagKey());
	}

	private static void assertNotAlch(BankOrganizationPreview preview, int id)
	{
		assertFalse("Keep item " + id, "alch".equals(item(preview, id).getLayoutTagKey()));
	}

	private static void assertAt(BankOrganizationPreview preview, int id, int tab)
	{
		assertTrue("Item " + id + " in tab " + tab, preview.getCategories().get(tab).getItems().stream()
			.anyMatch(item -> item.getItemId() == id));
	}

	private final class Session
	{
		private final Map<Integer, GearStats> stats;
		private final AtomicReference<Optional<BankAnalysisRequest>> request = new AtomicReference<>(Optional.empty());
		private final AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		private final BankAnalysis analysis = new BankAnalysis(Runnable::run, Runnable::run, request::get, status::set,
			CATALOG, preset);

		private Session(Map<Integer, GearStats> stats) { this.stats = stats; }

		private BankOrganizationPreview analyze(BankSnapshot bank, BankLayoutOptions options)
		{
			Map<Integer, Integer> values = bank.getItems().stream().collect(Collectors.toMap(
				BankItemSnapshot::getItemId, entry -> 39000));
			request.set(Optional.of(new BankAnalysisRequest(bank, stats, values, Collections.emptyMap(), plan, options, preset)));
			analysis.analyzeBank();
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			BankOrganizationPreview preview = status.get().organizationPreview().orElseThrow(AssertionError::new);
			assertContents(bank, preview);
			return preview;
		}
	}
}

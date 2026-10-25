package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
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

/** Exact ordinary IDs from RuneLite 1.13.3 and the pinned 23 October Wiki snapshots. */
@RunWith(Parameterized.class)
public class FracturedArchiveItemsTest
{
	private static final int[] GEAR = {34681, 34715, 34717, 34719, 34725, 34727};
	private static final List<Integer> ASCENSION_AMMO = Arrays.asList(34702, 34708, 34690, 34696, 34684);
	private static final Object[][] FACTS = {
		{34659, "Living record (tier 1)", ItemCategory.CLUE, "cosmetic", "cosmetics"},
		{34661, "Living record (tier 2)", ItemCategory.CLUE, "cosmetic", "cosmetics"},
		{34663, "Living record (tier 3)", ItemCategory.CLUE, "cosmetic", "cosmetics"},
		{34665, "Living record (tier 4)", ItemCategory.CLUE, "cosmetic", "cosmetics"},
		{34667, "Living record (tier 5)", ItemCategory.CLUE, "cosmetic", "cosmetics"},
		{34669, "Ascension shards", ItemCategory.UNIQUE, "equipment-charge", "boss-loot"},
		{34671, "Fragment of living earth", ItemCategory.UNIQUE, "equipment-upgrade", "boss-loot"},
		{34673, "Fragment of deep water", ItemCategory.UNIQUE, "equipment-upgrade", "boss-loot"},
		{34675, "Fragment of whispering wind", ItemCategory.UNIQUE, "equipment-upgrade", "boss-loot"},
		{34677, "Fragment of burning fire", ItemCategory.UNIQUE, "equipment-upgrade", "boss-loot"},
		{34679, "Horn of Bran", ItemCategory.UNIQUE, "equipment-upgrade", "boss-loot"},
		{34681, "Ascension crossbows", ItemCategory.GEAR, "weapon", "gear"},
		{34684, "Ascension bolts", ItemCategory.GEAR, "ammo", "boss-loot"},
		{34690, "Diamond ascension bolts", ItemCategory.GEAR, "ammo", "boss-loot"},
		{34696, "Diamond ascension bolts (e)", ItemCategory.GEAR, "ammo", "boss-loot"},
		{34702, "Onyx ascension bolts", ItemCategory.GEAR, "ammo", "boss-loot"},
		{34708, "Onyx ascension bolts (e)", ItemCategory.GEAR, "ammo", "boss-loot"},
		{34715, "Obligator", ItemCategory.GEAR, "weapon", "gear"},
		{34717, "Obligator (uncharged)", ItemCategory.GEAR, "weapon", "gear"},
		{34719, "Rondache", ItemCategory.GEAR, "shield", "gear"},
		{34722, "Archivist's folio (inert)", ItemCategory.TELEPORT, "teleport", "teleports"},
		{34723, "Archivist's folio", ItemCategory.TELEPORT, "teleport", "teleports"},
		{34725, "Zorya's tome", ItemCategory.GEAR, "weapon", "gear"},
		{34727, "Zorya's tome (uncharged)", ItemCategory.GEAR, "weapon", "gear"}
	};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}") public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public FracturedArchiveItemsTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void allTwentyFourExactItemsHaveTheirReviewedNamesCategoriesAndSubcategories()
	{
		assertEquals(24, FACTS.length);
		for (Object[] fact : FACTS)
		{
			CatalogItem item = catalog((int) fact[0]);
			assertEquals("Name " + fact[0], fact[1], item.getDisplayName());
			assertEquals("Category " + fact[0], fact[2], item.getCategory());
			assertEquals("Subcategory " + fact[0], fact[3], item.getSubcategory());
		}
	}

	@Test public void ordinaryAndPlaceholderItemsReachTheirReviewedDestinationsWithoutEquipmentStats()
	{
		for (AlchMode mode : AlchMode.values())
			for (int quantity : new int[] {1, 0})
			{
				BankOrganizationPreview preview = build(allItems(quantity), Collections.emptyMap(),
					CategoryOverrideSource.NONE, options(mode));
				for (Object[] fact : FACTS)
				{
					assertTag(preview, (int) fact[0], (String) fact[4]);
					assertAt(preview, (int) fact[0], plan.destinationOf((String) fact[4]));
				}
			}
	}

	@Test public void fallbackSlotsTiersAndNavigationRolesRecognizeEveryWeaponAndShieldState()
	{
		for (int id : GEAR)
			assertEquals("Raid-tier navigation " + id, 5, GearTierCatalog.INSTANCE.tierOf(id).orElse(-1));
		assertRole(34681, "weapon-ranged-rapid", 0, 9);
		assertRole(34715, "weapon-crush", 100, 8);
		assertRole(34717, "weapon-crush", 100, 8);
		assertRole(34719, "support-defence", -1, 5);
		assertRole(34725, "weapon-magic-special", 0, 10);
		assertRole(34727, "weapon-magic-special", 0, 10);
		for (int id : ASCENSION_AMMO)
			assertEquals(11, GearItemSorter.slotRankOf(new BankPreviewItem(catalog(id), 1), GearStatsSource.NONE));
	}

	@Test public void adjacentOtherIdsDoNotInheritTheExactRaidMetadata()
	{
		for (int id : new int[] {34660, 34662, 34670, 34680, 34682, 34685, 34716, 34718, 34720, 34724, 34726, 34728, 999931})
		{
			assertEquals("No exact raid role for another ID " + id, "", GearSetSemanticRuleSet.roleOf(id));
			assertFalse("No exact raid tier for another ID " + id, GearTierCatalog.INSTANCE.tierOf(id).isPresent());
			assertFalse("No name-collision Alch eligibility " + id, IronmanAlchCandidateCatalog.containsFixed(id));
			assertEquals("Unreviewed sibling remains unknown " + id, ItemCategory.UNKNOWN,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getCategory());
		}
	}

	@Test public void raidItemsNeverBecomeAutomaticAlchEvenAsBulkStockWithDominatingStatFixtures()
	{
		Map<Integer, GearStats> actual = actualGearStats();
		for (int candidate : GEAR)
		{
			GearStats owned = actual.get(candidate);
			GearStats stronger = syntheticDominator(owned.getSlot(), owned.style());
			assertTrue("Synthetic policy guard fixture, not a real replacement claim", stronger.dominates(owned));
			Map<Integer, GearStats> stats = new LinkedHashMap<>(actual);
			stats.put(999901, stronger);
			stats.put(999902, stronger);
			for (AlchMode mode : AlchMode.values())
			{
				List<BankItemSnapshot> entries = new ArrayList<>(allItems(25).getPhysicalItems());
				entries.add(entry(999901, 1, entries.size()));
				entries.add(entry(999902, 1, entries.size()));
				BankOrganizationPreview preview = build(new BankSnapshot(entries), stats,
					CategoryOverrideSource.NONE, options(mode));
				for (Object[] fact : FACTS)
					assertNotEquals("Preserve functional raid item " + fact[0], "alch", item(preview, (int) fact[0]).getLayoutTagKey());
			}
		}
	}

	@Test public void ascensionBoltVariantsFormACompactBlockSeparateFromOrdinaryDiamondBolts()
	{
		List<Integer> oldOrder = Arrays.asList(9243, 9144, 9143, 11875, 9142);
		List<Integer> ids = new ArrayList<>(ASCENSION_AMMO);
		ids.addAll(oldOrder);
		ids.add(34669);
		for (int trial = 0; trial < 4; trial++)
		{
			Collections.shuffle(ids, new Random(2026102334684L + trial));
			List<BankItemSnapshot> entries = new ArrayList<>();
			for (int id : ids) entries.add(entry(id, trial % 2 == 0 ? 25 : 0, entries.size()));
			BankOrganizationPreview preview = build(new BankSnapshot(entries), Collections.emptyMap(),
				CategoryOverrideSource.NONE, options(AlchMode.FLEXIBLE));
			List<Integer> loot = preview.getCategories().get(plan.destinationOf("boss-loot")).getItems().stream()
				.map(BankPreviewItem::getItemId).collect(Collectors.toList());
			int first = loot.indexOf(ASCENSION_AMMO.get(0));
			assertTrue(first >= 0);
			assertEquals(ASCENSION_AMMO, loot.subList(first, first + ASCENSION_AMMO.size()));
			assertEquals("Existing relative ammunition order is preserved", oldOrder,
				loot.stream().filter(oldOrder::contains).collect(Collectors.toList()));
			for (int id : ASCENSION_AMMO) assertTag(preview, id, "boss-loot");
		}
	}

	@Test public void knownEndgamePrioritiesKeepTheShieldAndSpecialistsOutOfUnrelatedPrimaryCells()
	{
		List<Integer> ids = Arrays.asList(34719, 34725, 34727, 34681, 34715, 34717, 22322, 20997, 27275, 22325, 22324);
		Map<Integer, GearStats> stats = actualGearStats();
		stats.put(22324, CombatQualityReport.FACTS.get(22324).stats);
		assertFalse("Rondache defence does not replace Avernic attack accuracy", stats.get(34719).dominates(stats.get(22322)));
		assertFalse("Avernic accuracy does not erase Rondache's defensive function", stats.get(22322).dominates(stats.get(34719)));
		for (GearStatsSource source : Arrays.asList((GearStatsSource) id -> Optional.ofNullable(stats.get(id)), GearStatsSource.NONE))
		{
			List<BankPreviewItem> gear = ids.stream().map(id -> new BankPreviewItem(catalog(id), 1)).collect(Collectors.toList());
			GearItemSorter.GearLayout selection = GearItemSorter.plan(gear, source);
			Map<Integer, Integer> targets = GearItemSorter.setupTargets(selection, 128);
			assertTrue("Avernic remains the primary melee shield", targets.containsKey(22322));
			assertTrue("General one-handed weapon permits the defender", targets.containsKey(22324));
			assertFalse("Size-dependent Scythe remains available separately", targets.containsKey(22325));
			assertFalse("Rondache remains a separate support shield", targets.containsKey(34719));
			assertTrue("Twisted bow keeps its existing primary priority", targets.containsKey(20997));
			assertFalse(targets.containsKey(34681));
			assertTrue("Shadow keeps its existing primary priority", targets.containsKey(27275));
			assertFalse(targets.containsKey(34725));
			assertFalse(targets.containsKey(34727));
			assertEquals(gear.size(), selection.getSetupRows().size() + selection.getTail().size());
		}
	}

	@Test public void ascensionCrossbowsDoNotProveAnOrdinaryRuneCrossbowIsRedundantInFlexibleMode()
	{
		Map<Integer, GearStats> stats = actualGearStats();
		stats.put(9185, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6));
		BankSnapshot bank = new BankSnapshot(Arrays.asList(entry(34681, 1, 0), entry(9185, 25, 1), entry(9243, 100, 2)));
		BankOrganizationPreview preview = build(bank, stats, CategoryOverrideSource.NONE, options(AlchMode.FLEXIBLE));
		assertTag(preview, 9185, "gear");
		assertTag(preview, 34681, "gear");
	}

	@Test public void bowfaAndAscensionTogetherDoNotReplaceTheRuneCrossbowsOrdinaryBoltFunction()
	{
		Map<Integer, GearStats> stats = actualGearStats();
		GearStats rune = new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6);
		// Charged Bowfa vector from the same existing RuneLite snapshot as EndgameCombatPlacementTest.
		GearStats bowfa = new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 128, 0, 106, 0, 0, 0, 0, 0, 0, 0, 5);
		stats.put(9185, rune);
		stats.put(25865, bowfa);
		assertTrue("A raw vector can dominate without preserving enchanted-bolt compatibility", bowfa.dominates(rune));
		assertFalse(stats.get(34681).dominates(rune));
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		int oldScore = GearItemSorter.score(new BankPreviewItem(catalog(9185), 3), source);
		for (int id : new int[] {25865, 34681})
			assertTrue("Both raid weapons clear the generic higher-score threshold " + id,
				GearItemSorter.score(new BankPreviewItem(catalog(id), 1), source) > oldScore);
		for (int quantity : new int[] {3, 25})
		{
			BankSnapshot bank = new BankSnapshot(Arrays.asList(entry(9185, quantity, 0), entry(25865, 1, 1),
				entry(34681, 1, 2), entry(9243, 100, 3)));
			BankOrganizationPreview preview = build(bank, stats, CategoryOverrideSource.NONE, options(AlchMode.FLEXIBLE));
			assertTag(preview, 9185, "gear");
			assertTag(preview, 25865, "gear");
			assertTag(preview, 34681, "gear");
			BankOrganizationPreview fixed = build(bank, stats, CategoryOverrideSource.NONE, options(AlchMode.FIXED));
			assertTag(fixed, 9185, "alch");
			assertTag(fixed, 25865, "gear");
			assertTag(fixed, 34681, "gear");
		}
	}

	@Test public void manualTagsAndSavedPhysicalChoicesRemainAuthoritative()
	{
		BankSnapshot bank = allItems(1);
		CategoryOverrideSource correction = id -> id == 34715 ? Optional.of("alch")
			: id == 34722 ? Optional.of("tools") : Optional.empty();
		for (AlchMode mode : AlchMode.values())
		{
			BankOrganizationPreview corrected = build(bank, Collections.emptyMap(), correction, options(mode));
			assertTag(corrected, 34715, "alch");
			assertTag(corrected, 34722, "tools");
			BlueprintItemOrders saved = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
				"34725#0", new BlueprintItemOrders.Destination(plan.destinationOf("tools"), "gear", "tools")));
			assertTag(build(bank, Collections.emptyMap(), CategoryOverrideSource.NONE, options(mode).withItemOrders(saved)),
				34725, "tools");
			BankOrganizationPreview automatic = build(bank, Collections.emptyMap(), CategoryOverrideSource.NONE, options(mode));
			int destination = 8;
			List<Integer> moved = Arrays.asList(34725, 34715, 34681);
			Map<Integer, List<Integer>> physicalTabs = new LinkedHashMap<>();
			for (int tab = 0; tab < automatic.getCategories().size(); tab++)
				physicalTabs.put(tab, automatic.getCategories().get(tab).getItems().stream()
					.filter(entry -> !moved.contains(entry.getItemId())).map(BankPreviewItem::getItemId)
					.collect(Collectors.toCollection(ArrayList::new)));
			physicalTabs.get(destination).addAll(0, moved);
			BlueprintItemOrders captured = BlueprintItemOrders.capture(physicalTabs, automatic, BlueprintItemOrders.EMPTY);
			BankOrganizationPreview restored = build(bank, Collections.emptyMap(), CategoryOverrideSource.NONE,
				options(mode).withItemOrders(captured));
			for (int id : new int[] {34725, 34715, 34681}) assertAt(restored, id, destination);
		}
	}

	@Test public void chargedStatesKeepTheirSeparateIdsAndEveryPhysicalCopy()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(entry(34715, 1, 0), entry(34715, 1, 1),
			entry(34717, 0, 2), entry(34725, 1, 3), entry(34725, 1, 4), entry(34727, 0, 5), entry(34669, 25, 6)));
		for (AlchMode mode : AlchMode.values())
		{
			BankOrganizationPreview preview = build(bank, actualGearStats(), CategoryOverrideSource.NONE, options(mode));
			assertEquals(2, preview.getPlannedItems().stream().filter(item -> item.getItemId() == 34715).count());
			assertEquals(2, preview.getPlannedItems().stream().filter(item -> item.getItemId() == 34725).count());
			assertTrue(item(preview, 34717).isPlaceholder());
			assertTrue(item(preview, 34727).isPlaceholder());
		}
	}

	private void assertRole(int id, String role, int priority, int fallbackSlot)
	{
		assertEquals(role, GearSetSemanticRuleSet.roleOf(id));
		assertEquals(priority, GearSetSemanticRuleSet.frontlinePriority(id));
		assertEquals(fallbackSlot, GearItemSorter.slotRankOf(new BankPreviewItem(catalog(id), 1), GearStatsSource.NONE));
	}

	private BankLayoutOptions options(AlchMode mode) { return BankLayoutOptions.defaultFor(preset).withAlchMode(mode); }

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats,
		CategoryOverrideSource choices, BankLayoutOptions options)
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, id -> Optional.of(
			CompositeItemCatalog.DEFAULT.findById(id).orElseGet(() -> new CatalogItem(id, "Synthetic control " + id,
				ItemCategory.GEAR, "gear", Collections.emptySet(), null))), preset,
			id -> Optional.ofNullable(stats.get(id)), id -> id == 9185 ? 9720 : 39000, choices, plan, options);
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		Map<String, Long> expected = bank.getPhysicalItems().stream().collect(Collectors.groupingBy(
			entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(), Collectors.counting()));
		Map<String, Long> actual = preview.getPlannedItems().stream().collect(Collectors.groupingBy(
			entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(), Collectors.counting()));
		assertEquals("Every physical ID, stack quantity, placeholder and occurrence is preserved", expected, actual);
		return preview;
	}

	private static BankSnapshot allItems(int quantity)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (Object[] fact : FACTS) entries.add(entry((int) fact[0], quantity, entries.size()));
		return new BankSnapshot(entries);
	}

	private static BankItemSnapshot entry(int id, int quantity, int slot) { return new BankItemSnapshot(id, quantity, slot, quantity == 0); }
	private static CatalogItem catalog(int id) { return CompositeItemCatalog.DEFAULT.describeOrUnknown(id); }
	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).findFirst().orElseThrow(AssertionError::new);
	}
	private static void assertTag(BankOrganizationPreview preview, int id, String tag) { assertEquals("Item " + id, tag, item(preview, id).getLayoutTagKey()); }
	private static void assertAt(BankOrganizationPreview preview, int id, int destination)
	{
		assertTrue("Item " + id + " in destination " + destination,
			preview.getCategories().get(destination).getItems().stream().anyMatch(item -> item.getItemId() == id));
	}

	/** Full Wiki vectors: Rondache oldid15379785, Ascension15378507, Obligator15379822, Zorya15379800. */
	private static Map<Integer, GearStats> actualGearStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(34681, new GearStats(GearSlot.WEAPON, 0, 0, 0, 3, 34, 0, 0, 0, -7, -5, 0, 10, 0, 0, 3));
		GearStats obligator = new GearStats(GearSlot.WEAPON, 22, -8, 120, 0, 0, 40, 0, 3, 0, 16, 0, 3, 8, 0, 5);
		stats.put(34715, obligator);
		stats.put(34717, obligator);
		stats.put(34719, new GearStats(GearSlot.SHIELD, 0, 0, 0, -5, -8, 8, 0, 0, 35, 41, 38, -6, 55, 0, 0));
		GearStats zorya = new GearStats(GearSlot.WEAPON, 0, 0, 0, 45, 0, 0, 0, 2, 1, 5, 5, 10, 0, 150, 3);
		stats.put(34725, zorya);
		stats.put(34727, zorya);
		// Existing 2026-10-09 RuneLite snapshot, independently preserved in EndgameCombatPlacementTest.
		stats.put(22322, new GearStats(GearSlot.SHIELD, 30, 29, 28, -5, -4, 8, 0, 0, 30, 29, 28, -5, -4, 0, 0));
		stats.put(20997, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 70, 0, 20, 0, 0, 0, 0, 0, 0, 0, 6));
		stats.put(27275, new GearStats(GearSlot.WEAPON, 0, 0, 0, 35, 0, 0, 0, 1, 0, 0, 0, 20, 0, 0, 5));
		stats.put(22325, new GearStats(GearSlot.WEAPON, 70, 125, 30, -6, 0, 75, 0, 0, -2, 8, 10, 0, 0, 0, 5));
		return stats;
	}

	/** Impossible high-stat controls isolate the protection policy; they describe no live gear or DPS. */
	private static GearStats syntheticDominator(GearSlot slot, GearStyle style)
	{
		return new GearStats(slot, style == GearStyle.MELEE ? 1000 : 30, 30, 1000,
			style == GearStyle.MAGIC ? 5000 : 60, style == GearStyle.RANGED ? 5000 : 30,
			1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, slot == GearSlot.WEAPON ? 1 : 0);
	}
}

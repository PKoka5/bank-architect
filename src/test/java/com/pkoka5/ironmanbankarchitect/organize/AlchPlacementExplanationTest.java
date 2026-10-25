package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
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

/** Explanations must name the actual evidence without changing the shared Alch policy. */
@RunWith(Parameterized.class)
public class AlchPlacementExplanationTest
{
	private static final int STOCK = 900010;
	private static final int SCORE_ONLY = 900011;
	private static final int DOMINATOR = 900012;
	private static final ItemCatalog CATALOG = id -> Optional.of(CompositeItemCatalog.DEFAULT.findById(id)
		.orElseGet(() -> new CatalogItem(id, id == STOCK ? "Unreviewed production plate"
			: id == SCORE_ONLY ? "Unreviewed offensive plate" : "Unreviewed covering plate",
			ItemCategory.GEAR, "body", Collections.emptySet(), null)));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public AlchPlacementExplanationTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void reviewedToolsNameTheirExactFunctionalUpgradeWithoutEquipmentStats()
	{
		for (int[] pair : new int[][] {{1275, 11920}, {1359, 6739}})
		{
			BankOrganizationPreview preview = build(bank(entry(pair[0], 7, 0), entry(pair[1], 1, 1)));
			assertWitness(preview, pair[0], pair[1], false);
			assertFalse("The named upgrade remains usable", "alch".equals(item(preview, pair[1]).getLayoutTagKey()));
		}
	}

	@Test public void elementalStaffAndMysticColourNameTheMatchingRoleRatherThanUnrelatedGear()
	{
		for (int[] pair : new int[][] {{1387, 1393}, {4101, 4091}})
		{
			BankOrganizationPreview preview = build(bank(entry(4151, 1, 0), entry(pair[0], 2, 1), entry(pair[1], 1, 2)));
			assertWitness(preview, pair[0], pair[1], false);
			assertFalse(item(preview, pair[0]).getPlacementReason().contains(name(4151)));
		}
	}

	@Test public void anExplicitMysticCombatColourIsTheReplacementActuallyNamed()
	{
		CategoryOverrideSource choice = id -> id == 4101 ? Optional.of("gear") : Optional.empty();
		BankOrganizationPreview preview = build(bank(entry(4091, 1, 0), entry(4101, 1, 1)),
			Collections.emptyMap(), choice, defaults());
		assertWitness(preview, 4091, 4101, false);
		assertTrue(item(preview,4101).getPlacementReason().startsWith("Your category choice."));
	}

	@Test public void bankPlaceholdersAreNamedAsPlaceholdersForEachReviewedReplacementPath()
	{
		for (int[] pair : new int[][] {{1275, 11920}, {1359, 6739}, {1387, 1393}, {4101, 4091}, {1127, 11832}})
		{
			Map<Integer, GearStats> stats = pair[0] == 1127 ? armourPair(pair[0], pair[1], false) : Collections.emptyMap();
			BankOrganizationPreview preview = build(bank(entry(pair[0], 1, 0), entry(pair[1], 0, 1)), stats);
			assertWitness(preview, pair[0], pair[1], true);
		}
	}

	@Test public void removingTheOnlyRecordedReplacementRestoresTheAutomaticDestinationAndReason()
	{
		for (int[] pair : new int[][] {{1275, 11920}, {1359, 6739}, {1387, 1393}, {4101, 4091}, {1127, 11832}})
		{
			Map<Integer, GearStats> stats = pair[0] == 1127 ? armourPair(pair[0], pair[1], false) : Collections.emptyMap();
			assertWitness(build(bank(entry(pair[0], 1, 0), entry(pair[1], 1, 1)), stats), pair[0], pair[1], false);
			BankPreviewItem restored = item(build(bank(entry(pair[0], 1, 0)), stats), pair[0]);
			assertFalse("No replacement remains for " + pair[0], "alch".equals(restored.getLayoutTagKey()));
			assertFalse(restored.getPlacementReason(), restored.getPlacementReason().startsWith("Flexible Alch:"));
			assertFalse(restored.getPlacementReason().contains(name(pair[1])));
		}
	}

	@Test public void reviewedArmourTradeoffsUseProgressionEvidenceWithoutClaimingFullStatDominance()
	{
		Map<Integer, GearStats> stats = armourPair(1127, 11832, false);
		assertFalse(stats.get(11832).dominates(stats.get(1127)));
		BankOrganizationPreview preview = build(bank(entry(1127, 1, 0), entry(11832, 1, 1)), stats);
		assertWitness(preview, 1127, 11832, false);
		String reason = item(preview, 1127).getPlacementReason();
		assertTrue(reason, reason.contains("reviewed armour progression"));
		assertFalse(reason, reason.contains("all compared stats covered"));
	}

	@Test public void reviewedArmourFullStatWitnessDoesNotNeedAReviewedTier()
	{
		Map<Integer, GearStats> stats = armourPair(1127, DOMINATOR, true);
		assertTrue(stats.get(DOMINATOR).dominates(stats.get(1127)));
		BankOrganizationPreview preview = build(bank(entry(1127, 1, 0), entry(DOMINATOR, 1, 1)), stats);
		assertWitness(preview, 1127, DOMINATOR, false);
		assertTrue(item(preview, 1127).getPlacementReason().contains("all compared stats covered"));
	}

	@Test public void unreviewedBulkAndSpareReasonsSeparateTheDominatorFromTheHigherScoreCount()
	{
		Map<Integer, GearStats> stats = stockStats();
		assertTrue(stats.get(DOMINATOR).dominates(stats.get(STOCK)));
		assertFalse(stats.get(SCORE_ONLY).dominates(stats.get(STOCK)));
		assertTrue(stats.get(SCORE_ONLY).score() > stats.get(DOMINATOR).score());
		for (int quantity : new int[] {3, 8})
		{
			BankOrganizationPreview preview = build(bank(entry(STOCK, quantity, 0), entry(SCORE_ONLY, 1, 1),
				entry(DOMINATOR, 1, 2)), stats);
			assertWitness(preview, STOCK, DOMINATOR, false);
			String reason = item(preview, STOCK).getPlacementReason();
			assertTrue(reason, reason.contains("all compared stats covered"));
			assertTrue(reason, reason.contains("Higher-scoring bank entries: 2."));
			assertTrue(reason, reason.contains(quantity == 8 ? "bulk stock" : "spare gear"));
			assertFalse("Highest score alone is not the covering witness", reason.contains(name(SCORE_ONLY)));
		}
	}

	@Test public void unreviewedStockStillRequiresRealOwnershipQuantityAndBackupEvidence()
	{
		Map<Integer, GearStats> stats = stockStats();
		for (BankSnapshot bank : Arrays.asList(
			bank(entry(STOCK, 3, 0), entry(DOMINATOR, 1, 1)),
			bank(entry(STOCK, 1, 0), entry(SCORE_ONLY, 1, 1), entry(DOMINATOR, 1, 2)),
			bank(entry(STOCK, 8, 0), entry(SCORE_ONLY, 1, 1), entry(DOMINATOR, 0, 2))))
		{
			BankPreviewItem kept = item(build(bank, stats), STOCK);
			assertEquals("gear", kept.getLayoutTagKey());
			assertFalse(kept.getPlacementReason(), kept.getPlacementReason().startsWith("Flexible Alch:"));
		}
	}

	@Test public void fixedModeStatesTheListWithoutInventingAReplacement()
	{
		for (BankSnapshot bank : Arrays.asList(bank(entry(1359, 1, 0)),
			bank(entry(1359, 1, 0), entry(6739, 1, 1))))
		{
			BankPreviewItem fixed = item(build(bank, Collections.emptyMap(), CategoryOverrideSource.NONE,
				defaults().withAlchMode(AlchMode.FIXED)), 1359);
			assertEquals("alch", fixed.getLayoutTagKey());
			assertEquals("Fixed Alch list.", fixed.getPlacementReason());
		}
	}

	@Test public void theReviewedDragonHalberdExceptionExplainsTheRuleWithoutAnInventedWitness()
	{
		BankPreviewItem halberd = item(build(bank(entry(3204, 1, 0))), 3204);
		assertEquals("alch", halberd.getLayoutTagKey());
		assertTrue(halberd.getPlacementReason(), halberd.getPlacementReason().startsWith("Flexible Alch:"));
		assertTrue(halberd.getPlacementReason(), halberd.getPlacementReason().contains("reviewed Dragon halberd rule"));
		assertFalse(halberd.getPlacementReason().contains(" in bank."));
		assertFalse(halberd.getPlacementReason().contains("(bank placeholder)"));
	}

	@Test public void usefulSpecialAttackWeaponsDoNotGainAReplacementClaimFromRawStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1215, weapon(10));
		stats.put(4151, weapon(100));
		assertTrue(stats.get(4151).dominates(stats.get(1215)));
		BankPreviewItem dagger = item(build(bank(entry(1215, 25, 0), entry(4151, 1, 1)), stats), 1215);
		assertEquals("gear", dagger.getLayoutTagKey());
		assertFalse(dagger.getPlacementReason(), dagger.getPlacementReason().startsWith("Flexible Alch:"));
	}

	@Test public void personalAndSavedDestinationsStillOverrideReplacementProvenance()
	{
		BankSnapshot bank = bank(entry(1359, 3, 0), entry(6739, 1, 1));
		BankOrganizationPreview personal = build(bank, Collections.emptyMap(),
			id -> id == 1359 ? Optional.of("tools") : Optional.empty(), defaults());
		assertEquals("tools", item(personal, 1359).getLayoutTagKey());
		assertEquals("Your category choice.", item(personal, 1359).getPlacementReason());
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap("1359#0",
			new BlueprintItemOrders.Destination(plan.destinationOf("tools"), "alch", "tools")));
		BankOrganizationPreview saved = build(bank, Collections.emptyMap(), CategoryOverrideSource.NONE,
			defaults().withItemOrders(orders));
		assertEquals("tools", item(saved, 1359).getLayoutTagKey());
		assertEquals("Saved item destination.", item(saved, 1359).getPlacementReason());
	}

	@Test public void retainedDecisionsDoNotPretendTheAbsentReplacementIsStillInTheBank()
	{
		CategoryOverrideSource retained = new CategoryOverrideSource()
		{
			@Override public Optional<String> categoryKeyFor(int id) { return id == 1359 ? Optional.of("alch") : Optional.empty(); }
			@Override public boolean isPersonalChoice(int id) { return false; }
		};
		BankPreviewItem item = item(build(bank(entry(1359, 3, 0)), Collections.emptyMap(), retained, defaults()), 1359);
		assertEquals("alch", item.getLayoutTagKey());
		assertEquals("Preserved previous Alch decision.", item.getPlacementReason());
	}

	@Test public void replacementExplanationsRemainStableWhenBankEntriesArePermuted()
	{
		Map<Integer, GearStats> stats = stockStats();
		BankOrganizationPreview before = build(bank(entry(STOCK, 3, 0), entry(SCORE_ONLY, 1, 1),
			entry(DOMINATOR, 1, 2)), stats);
		BankOrganizationPreview after = build(bank(entry(DOMINATOR, 1, 0), entry(STOCK, 3, 1),
			entry(SCORE_ONLY, 1, 2)), stats);
		assertEquals(item(before, STOCK).getPlacementReason(), item(after, STOCK).getPlacementReason());
	}

	@Test public void aWitnessBecomingAPlaceholderChangesOnlyTheReasonNotTheRememberedDestination()
	{
		BankOrganizationPreview owned = build(bank(entry(1359, 3, 0), entry(6739, 1, 1)));
		BankOrganizationPreview withdrawn = build(bank(entry(1359, 3, 0), entry(6739, 0, 1)));
		assertWitness(owned, 1359, 6739, false);
		assertWitness(withdrawn, 1359, 6739, true);
		assertNotEquals(item(owned, 1359).getPlacementReason(), item(withdrawn, 1359).getPlacementReason());
		BlueprintChanges changes = BlueprintChanges.compare(BlueprintChanges.capture(owned), withdrawn);
		assertTrue(changes.isSupported());
		assertEquals(0, changes.changedCount());
		assertEquals(0, changes.addedCount());
		assertEquals(0, changes.missingCount());
	}

	@Test public void explanationMetadataPreservesDuplicatePhysicalCopiesAndTheirQuantities()
	{
		BankSnapshot bank = bank(entry(1359, 1, 0), entry(1359, 2, 1), entry(1359, 0, 2), entry(6739, 0, 3));
		BankOrganizationPreview preview = build(bank);
		List<BankPreviewItem> copies = preview.getPlannedItems().stream().filter(item -> item.getItemId() == 1359)
			.collect(Collectors.toList());
		assertEquals(3, copies.size());
		for (BankPreviewItem copy : copies)
		{
			assertEquals("alch", copy.getLayoutTagKey());
			assertTrue(copy.getPlacementReason(), copy.getPlacementReason().contains(name(6739) + " (bank placeholder)."));
		}
	}

	private BankLayoutOptions defaults() { return BankLayoutOptions.defaultFor(preset).withAlchMode(AlchMode.FLEXIBLE); }
	private BankOrganizationPreview build(BankSnapshot bank) { return build(bank, Collections.emptyMap()); }
	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats)
	{
		return build(bank, stats, CategoryOverrideSource.NONE, defaults());
	}
	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats,
		CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CATALOG, preset,
			id -> Optional.ofNullable(stats.get(id)), id -> 39000, overrides, plan, options);
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getPhysicalItems().stream().collect(Collectors.groupingBy(
			item -> item.getItemId() + ":" + item.getQuantity() + ":" + item.isPlaceholder(), Collectors.counting())),
			preview.getPlannedItems().stream().collect(Collectors.groupingBy(
				item -> item.getItemId() + ":" + item.getQuantity() + ":" + item.isPlaceholder(), Collectors.counting())));
		return preview;
	}
	private static void assertWitness(BankOrganizationPreview preview, int stock, int replacement, boolean placeholder)
	{
		BankPreviewItem item = item(preview, stock);
		assertEquals("Item " + stock, "alch", item.getLayoutTagKey());
		assertTrue(item.getPlacementReason(), item.getPlacementReason().startsWith("Flexible Alch:"));
		assertTrue(item.getPlacementReason(), item.getPlacementReason().contains(name(replacement)
			+ (placeholder ? " (bank placeholder)." : " in bank.")));
		assertEquals(placeholder, item(preview, replacement).isPlaceholder());
	}
	private static String name(int id) { return CATALOG.describeOrUnknown(id).getDisplayName(); }
	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).findFirst().orElseThrow(AssertionError::new);
	}
	private static BankItemSnapshot entry(int id, int quantity, int slot) { return new BankItemSnapshot(id, quantity, slot, quantity == 0); }
	private static BankSnapshot bank(BankItemSnapshot... items) { return new BankSnapshot(Arrays.asList(items)); }
	private static Map<Integer, GearStats> armourPair(int stock, int replacement, boolean dominance)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(stock, fullStats(10, 100));
		stats.put(replacement, fullStats(30, dominance ? 120 : 80));
		return stats;
	}
	private static Map<Integer, GearStats> stockStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(STOCK, fullStats(10, 10));
		stats.put(SCORE_ONLY, fullStats(100, 5));
		stats.put(DOMINATOR, fullStats(12, 12));
		return stats;
	}
	private static GearStats fullStats(int strength, int defence)
	{
		return new GearStats(GearSlot.BODY, strength, strength, strength, 0, 0, strength, 0, 0,
			defence, defence, defence, defence, defence, 0, 4);
	}
	private static GearStats weapon(int bonus)
	{
		return new GearStats(GearSlot.WEAPON, bonus, bonus, bonus, 0, 0, bonus, 0, 0,
			bonus, bonus, bonus, bonus, bonus, 0, 4);
	}
}

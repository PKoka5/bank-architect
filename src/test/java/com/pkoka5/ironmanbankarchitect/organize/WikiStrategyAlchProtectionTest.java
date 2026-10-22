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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** A strategy table's overall rank cannot erase a ranged armour piece's defensive role. */
@RunWith(Parameterized.class)
public class WikiStrategyAlchProtectionTest
{
	private static final int[] STOCK = {1135, 2499, 2501, 2503, 2493, 2495, 2497};
	private static final int BLACK_BODY = 2503;
	private static final int BLACK_CHAPS = 2497;
	private static final int BLESSED_BODY = 10386;
	private static final int BLESSED_CHAPS = 10388;
	private static final int UNKNOWN_UPGRADE = 900001;
	private static final int UNKNOWN_STOCK = 900002;
	private static final ItemValueSource VALUES = id -> 39000;
	private static final ItemCatalog CATALOG = id -> Optional.of(CompositeItemCatalog.DEFAULT.findById(id)
		.orElseGet(() -> new CatalogItem(id, id == UNKNOWN_UPGRADE ? "Saradomin d'hide body" : "Black d'hide body",
			ItemCategory.GEAR, "body", Collections.emptySet(), null)));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public WikiStrategyAlchProtectionTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void everyReviewedDragonhidePieceRetainsAStatTradeoffDespiteHigherTierAndScore()
	{
		for (int candidate : STOCK)
		{
			int upgrade = blessedFor(candidate);
			Map<Integer, GearStats> stats = synthetic(candidate, upgrade, false);
			assertTrue(IronmanAlchCandidateCatalog.tierOf(upgrade)
				> IronmanAlchCandidateCatalog.tierOf(candidate));
			assertTrue(stats.get(upgrade).score() > stats.get(candidate).score());
			assertFalse(stats.get(upgrade).dominates(stats.get(candidate)));
			for (int quantity : new int[] {25, 1, 0})
				for (int replacementQuantity : new int[] {1, 0})
					assertTag(build(pair(candidate, quantity, upgrade, replacementQuantity), stats), candidate, "gear");
		}
	}

	@Test
	public void everyReviewedDragonhidePieceAcceptsFullDominanceWithHigherTierPlaceholderEvidence()
	{
		for (int candidate : STOCK)
		{
			int upgrade = blessedFor(candidate);
			Map<Integer, GearStats> stats = synthetic(candidate, upgrade, true);
			assertTrue(stats.get(upgrade).dominates(stats.get(candidate)));
			for (int quantity : new int[] {25, 1, 0, 25})
				for (int replacementQuantity : new int[] {1, 0})
				{
					BankOrganizationPreview preview = build(pair(candidate, quantity, upgrade, replacementQuantity), stats);
					assertTag(preview, candidate, "alch");
					assertAt(preview, candidate, plan.destinationOf("alch"));
					assertEquals(quantity == 0 ? 0 : 1, preview.getTagCounts().getOrDefault("alch", 0).intValue());
				}
		}
	}

	@Test
	public void actualBlackDragonhideRetainsMagicDefenceAgainstCrystalAndUnfortifiedMasori()
	{
		for (int[] pair : new int[][] {{BLACK_BODY, 23975}, {BLACK_BODY, 27229}, {BLACK_CHAPS, 27232}})
		{
			Map<Integer, GearStats> stats = wikiStats();
			assertTrue(IronmanAlchCandidateCatalog.tierOf(pair[1])
				> IronmanAlchCandidateCatalog.tierOf(pair[0]));
			assertFalse(stats.get(pair[1]).dominates(stats.get(pair[0])));
			for (int quantity : new int[] {25, 1, 0})
				for (int replacementQuantity : new int[] {1, 0})
					assertTag(build(pair(pair[0], quantity, pair[1], replacementQuantity), stats), pair[0], "gear");
		}
	}

	@Test
	public void actualBlessedAndFortifiedMasoriPiecesFullyReplaceOrdinaryBlackDragonhide()
	{
		for (int[] pair : new int[][] {{BLACK_BODY, BLESSED_BODY}, {BLACK_CHAPS, BLESSED_CHAPS},
			{BLACK_BODY, 27238}, {BLACK_CHAPS, 27241}})
		{
			Map<Integer, GearStats> stats = wikiStats();
			assertTrue(stats.get(pair[1]).dominates(stats.get(pair[0])));
			for (int quantity : new int[] {25, 1, 0, 25})
				for (int replacementQuantity : new int[] {1, 0})
					assertTag(build(pair(pair[0], quantity, pair[1], replacementQuantity), stats), pair[0], "alch");
		}
	}

	@Test
	public void removingTheRecordedUpgradeRestoresTheDragonhidePieceWithoutSessionHistory()
	{
		for (int candidate : STOCK)
		{
			Map<Integer, GearStats> stats = synthetic(candidate, blessedFor(candidate), true);
			assertTag(build(pair(candidate, 1, blessedFor(candidate), 0), stats), candidate, "alch");
			for (int quantity : new int[] {25, 1, 0})
				assertTag(build(bank(entry(candidate, quantity, 0)), stats), candidate, "gear");
		}
	}

	@Test
	public void unknownAndEqualTierDominatorPlaceholdersAreNotReviewedUpgradeProof()
	{
		for (int upgrade : new int[] {UNKNOWN_UPGRADE, 2501})
		{
			Map<Integer, GearStats> stats = synthetic(BLACK_BODY, upgrade, true);
			assertTrue(stats.get(upgrade).dominates(stats.get(BLACK_BODY)));
			assertTag(build(pair(BLACK_BODY, 25, upgrade, 0), stats), BLACK_BODY, "gear");
			// Ordinary real ownership remains valid full-stat proof; no new tier restriction is imposed there.
			assertTag(build(pair(BLACK_BODY, 25, upgrade, 1), stats), BLACK_BODY, "alch");
		}
	}

	@Test
	public void anotherEquipmentSlotOrStyleCannotSupplyPlaceholderReplacementProof()
	{
		for (GearStats incompatible : Arrays.asList(syntheticStats(GearSlot.LEGS, true),
			new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 100, 0, 0, 200, 200, 200, 200, 200, 0, 0)))
		{
			Map<Integer, GearStats> stats = synthetic(BLACK_BODY, BLESSED_BODY, true);
			stats.put(BLESSED_BODY, incompatible);
			assertTag(build(pair(BLACK_BODY, 25, BLESSED_BODY, 0), stats), BLACK_BODY, "gear");
		}
	}

	@Test
	public void partialEquipmentMetadataDoesNotProveFullDominance()
	{
		for (int missing : new int[] {BLACK_BODY, BLESSED_BODY})
		{
			Map<Integer, GearStats> stats = wikiStats();
			stats.remove(missing);
			assertTag(build(pair(BLACK_BODY, 25, BLESSED_BODY, 0), stats), BLACK_BODY, "gear");
			stats.put(missing, new GearStats(GearSlot.BODY, 0, 0, 0, -15, 100, 0, 0, 0, 1000));
			assertTag(build(pair(BLACK_BODY, 25, BLESSED_BODY, 0), stats), BLACK_BODY, "gear");
		}
	}

	@Test
	public void manualGearAndAlchChoicesOverrideEitherAutomaticDecision()
	{
		for (String correction : Arrays.asList("gear", "combat-gear", "alch"))
			for (int quantity : new int[] {25, 1, 0})
			{
				boolean alch = "alch".equals(correction);
				Map<Integer, GearStats> stats = alch ? synthetic(BLACK_BODY, BLESSED_BODY, false) : wikiStats();
				CategoryOverrideSource override = id -> id == BLACK_BODY ? Optional.of(correction) : Optional.empty();
				BankOrganizationPreview preview = build(pair(BLACK_BODY, quantity, BLESSED_BODY, 0), stats,
					VALUES, override, defaults());
				assertTag(preview, BLACK_BODY, alch ? "alch" : "gear");
				assertAt(preview, BLACK_BODY, plan.destinationOf(alch ? "alch" : "gear"));
			}
	}

	@Test
	public void aSavedEditorDestinationSurvivesTheContextualUpgradeRule()
	{
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			BLACK_BODY + "#0", new BlueprintItemOrders.Destination(plan.destinationOf("gear"), "alch", "gear")));
		for (int quantity : new int[] {25, 1, 0})
		{
			BankOrganizationPreview preview = build(pair(BLACK_BODY, quantity, BLESSED_BODY, 0), wikiStats(),
				VALUES, CategoryOverrideSource.NONE, defaults().withItemOrders(orders));
			assertTag(preview, BLACK_BODY, "gear");
			assertAt(preview, BLACK_BODY, plan.destinationOf("gear"));
		}
	}

	@Test
	public void capturedPhysicalOrderRemainsAuthoritativeDuringWithdrawal()
	{
		BankSnapshot owned = pair(BLACK_BODY, 25, BLESSED_BODY, 1);
		BankOrganizationPreview before = build(owned, wikiStats(), VALUES, CategoryOverrideSource.NONE,
			new BankLayoutOptions(true, true, false));
		int chosenTab = 8;
		BlueprintItemOrders orders = BlueprintItemOrders.capture(Collections.singletonMap(chosenTab,
			Arrays.asList(BLESSED_BODY, BLACK_BODY)), before, BlueprintItemOrders.EMPTY);
		assertTrue(orders.isCaptured());
		for (int quantity : new int[] {25, 1, 0})
		{
			BankOrganizationPreview preview = build(pair(BLACK_BODY, quantity, BLESSED_BODY, 0), wikiStats(),
				VALUES, CategoryOverrideSource.NONE, defaults().withItemOrders(orders));
			assertEquals(Arrays.asList(BLESSED_BODY, BLACK_BODY), preview.getCategories().get(chosenTab).getItems()
				.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		}
	}

	@Test
	public void alchGatheringAndPositiveValueRemainRequired()
	{
		BankSnapshot bank = pair(BLACK_BODY, 25, BLESSED_BODY, 0);
		assertTag(build(bank, wikiStats(), ItemValueSource.NONE, CategoryOverrideSource.NONE, defaults()),
			BLACK_BODY, "gear");
		assertTag(build(bank, wikiStats(), VALUES, CategoryOverrideSource.NONE,
			new BankLayoutOptions(true, true, false)), BLACK_BODY, "gear");
	}

	@Test
	public void anUnknownNamesakeDoesNotInheritReviewedSingletonOrPlaceholderEligibility()
	{
		Map<Integer, GearStats> stats = synthetic(UNKNOWN_STOCK, BLESSED_BODY, true);
		assertFalse(IronmanAlchCandidateCatalog.contains(UNKNOWN_STOCK));
		for (int quantity : new int[] {1, 0})
			assertTag(build(pair(UNKNOWN_STOCK, quantity, BLESSED_BODY, 1), stats), UNKNOWN_STOCK, "gear");
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats)
	{
		return build(bank, stats, VALUES, CategoryOverrideSource.NONE, defaults());
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats,
		ItemValueSource values, CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CATALOG, preset,
			id -> Optional.ofNullable(stats.get(id)), values, overrides, plan, options);
		assertEquals(bank.getItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getItems().size(), preview.getPlannedItems().size());
		for (BankItemSnapshot original : bank.getItems())
		{
			BankPreviewItem planned = item(preview, original.getItemId());
			assertEquals(original.getQuantity(), planned.getQuantity());
			assertEquals(original.isPlaceholder(), planned.isPlaceholder());
			assertEquals(original.getPhysicalSlotQuantities(), planned.physicalBankSlots().stream()
				.map(BankPreviewItem::getQuantity).collect(Collectors.toList()));
		}
		return preview;
	}

	private BankLayoutOptions defaults() { return BankLayoutOptions.defaultFor(preset); }

	private static BankSnapshot pair(int candidate, int quantity, int upgrade, int replacementQuantity)
	{
		return bank(entry(candidate, quantity, 0), entry(upgrade, replacementQuantity, 1));
	}

	private static BankSnapshot bank(BankItemSnapshot... entries) { return new BankSnapshot(Arrays.asList(entries)); }

	private static BankItemSnapshot entry(int id, int quantity, int slot)
	{
		return new BankItemSnapshot(id, quantity, slot, quantity == 0);
	}

	private static GearSlot slotOf(int itemId)
	{
		return itemId == 2493 || itemId == 2495 || itemId == 2497 ? GearSlot.LEGS : GearSlot.BODY;
	}

	private static int blessedFor(int candidate) { return slotOf(candidate) == GearSlot.BODY ? BLESSED_BODY : BLESSED_CHAPS; }

	private static Map<Integer, GearStats> synthetic(int candidate, int upgrade, boolean dominates)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		GearSlot slot = slotOf(candidate);
		stats.put(candidate, new GearStats(slot, 0, 0, 0, -10, 20, 0, 0, 0,
			100, 100, 100, 100, 100, 0, 0));
		stats.put(upgrade, syntheticStats(slot, dominates));
		return stats;
	}

	/** Deliberate tradeoff isolates role proof from real equipment balance and names. */
	private static GearStats syntheticStats(GearSlot slot, boolean dominates)
	{
		return new GearStats(slot, 0, 0, 0, -10, 40, 0, 0, 0,
			150, 150, 150, dominates ? 150 : 90, 150, 0, 0);
	}

	/** Wiki revision vectors, checked independently of the production tier and replacement tables. */
	private static Map<Integer, GearStats> wikiStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		// Black_d'hide_body?oldid=15352980; Black_d'hide_chaps?oldid=15352979.
		stats.put(BLACK_BODY, ranged(GearSlot.BODY, -15, 30, 0, 0, 30, 38, 45, 45, 50));
		stats.put(BLACK_CHAPS, ranged(GearSlot.LEGS, -10, 17, 0, 0, 18, 20, 26, 23, 26));
		// Crystal_body?oldid=15369568; Masori_body?oldid=15190527; Masori_chaps?oldid=15190528.
		stats.put(23975, ranged(GearSlot.BODY, -18, 31, 0, 3, 46, 38, 48, 44, 68));
		stats.put(27229, ranged(GearSlot.BODY, -4, 43, 4, 0, 37, 35, 38, 25, 33));
		stats.put(27232, ranged(GearSlot.LEGS, -2, 27, 2, 0, 26, 24, 29, 19, 22));
		// Saradomin_d'hide_body?oldid=15351668; Saradomin_chaps?oldid=15351669.
		stats.put(BLESSED_BODY, ranged(GearSlot.BODY, -15, 30, 0, 1, 55, 47, 60, 50, 55));
		stats.put(BLESSED_CHAPS, ranged(GearSlot.LEGS, -10, 17, 0, 1, 31, 25, 33, 28, 31));
		// Masori_body_(f)?oldid=15190976; Masori_chaps_(f)?oldid=15190977.
		stats.put(27238, ranged(GearSlot.BODY, -4, 43, 4, 1, 59, 52, 64, 74, 60));
		stats.put(27241, ranged(GearSlot.LEGS, -2, 27, 2, 1, 35, 30, 39, 46, 37));
		return stats;
	}

	private static GearStats ranged(GearSlot slot, int magic, int ranged, int strength, int prayer,
		int stab, int slash, int crush, int magicDefence, int rangedDefence)
	{
		return new GearStats(slot, 0, 0, 0, magic, ranged, 0, strength, prayer,
			stab, slash, crush, magicDefence, rangedDefence, 0, 0);
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

	private static void assertAt(BankOrganizationPreview preview, int id, int tab)
	{
		assertTrue("Item " + id + " in tab " + tab, preview.getCategories().get(tab).getItems().stream()
			.anyMatch(item -> item.getItemId() == id));
	}
}

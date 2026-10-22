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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Fresh analyses must not reinterpret reviewed Alch stock merely because it was withdrawn. */
@RunWith(Parameterized.class)
public class ReviewedAlchWithdrawalStabilityTest
{
	private static final int[][] REVIEWED_PAIRS = {
		{1289, 4151}, {1275, 11920}, {1359, 6739}, {1387, 1393}, {4103, 4093}, {1127, 11832}
	};
	private static final int UNKNOWN_STOCK = 900001;
	private static final int UNKNOWN_UPGRADE = 900002;
	private static final int UNKNOWN_BACKUP = 900003;
	private static final ItemCatalog CATALOG = id -> Optional.of(CompositeItemCatalog.DEFAULT.findById(id)
		.orElseGet(() -> new CatalogItem(id, "Unreviewed stock " + id, ItemCategory.GEAR,
			"gear", Collections.emptySet(), null)));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public ReviewedAlchWithdrawalStabilityTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void reviewedStockKeepsItsAlchDestinationThroughPartialAndFullWithdrawal()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			for (int upgradeQuantity : new int[] {1, 0})
			{
				Map<Integer, GearStats> stats = reviewedStats(pair);
				for (int stockQuantity : new int[] {25, 1, 0, 25})
				{
					BankSnapshot bank = pairBank(pair, stockQuantity, upgradeQuantity);
					// Every invocation is stateless, including a client restart with both items withdrawn.
					BankOrganizationPreview preview = build(bank, stats);
					assertTag(preview, pair[0], "alch");
					assertAt(preview, pair[0], plan.destinationOf("alch"));
					assertFalse("Retain reviewed replacement " + pair[1],
						"alch".equals(item(preview, pair[1]).getLayoutTagKey()));
					assertContents(bank, preview);
				}
			}
		}
	}

	@Test
	public void releasingTheUpgradePlaceholderRestoresOrdinaryClassification()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			Map<Integer, GearStats> stats = reviewedStats(pair);
			assertTag(build(pairBank(pair, 1, 0), stats), pair[0], "alch");
			for (int quantity : new int[] {25, 1, 0})
			{
				BankSnapshot withoutUpgrade = bank(entry(pair[0], quantity, 0));
				BankOrganizationPreview preview = build(withoutUpgrade, stats);
				assertFalse("No remaining bank evidence for " + pair[0],
					"alch".equals(item(preview, pair[0]).getLayoutTagKey()));
				assertContents(withoutUpgrade, preview);
			}
		}
	}

	@Test
	public void releasingTheStockPlaceholderDoesNotInventAnAlchEntry()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			Map<Integer, GearStats> stats = reviewedStats(pair);
			assertTag(build(pairBank(pair, 0, 0), stats), pair[0], "alch");
			BankSnapshot released = bank(entry(pair[1], 0, 0));
			BankOrganizationPreview preview = build(released, stats);
			assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
			assertContents(released, preview);
		}
	}

	@Test
	public void reviewedPlaceholderRoutingStillRequiresAlchValueAndTheGatheringOption()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			BankSnapshot bank = pairBank(pair, 0, 0);
			Map<Integer, GearStats> stats = reviewedStats(pair);
			for (BankOrganizationPreview preview : Arrays.asList(
				build(bank, stats, ItemValueSource.NONE, CategoryOverrideSource.NONE, defaults()),
				build(bank, stats, id -> 39000, CategoryOverrideSource.NONE,
					new BankLayoutOptions(true, true, false))))
			{
				assertFalse("Alch guard for " + pair[0],
					"alch".equals(item(preview, pair[0]).getLayoutTagKey()));
				assertContents(bank, preview);
			}
		}
	}

	@Test
	public void explicitCorrectionsAndSavedEditorDestinationsWinDuringWithdrawal()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			Map<Integer, GearStats> stats = reviewedStats(pair);
			for (int quantity : new int[] {25, 1, 0})
			{
				BankSnapshot bank = pairBank(pair, quantity, 0);
				CategoryOverrideSource correction = id -> id == pair[0] ? Optional.of("gear") : Optional.empty();
				BankOrganizationPreview corrected = build(bank, stats, id -> 39000, correction, defaults());
				assertTag(corrected, pair[0], "gear");
				assertAt(corrected, pair[0], plan.destinationOf("gear"));
				BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
					pair[0] + "#0", new BlueprintItemOrders.Destination(plan.destinationOf("gear"), "alch", "gear")));
				BankOrganizationPreview saved = build(bank, stats, id -> 39000, CategoryOverrideSource.NONE,
					defaults().withItemOrders(orders));
				assertTag(saved, pair[0], "gear");
				assertAt(saved, pair[0], plan.destinationOf("gear"));
				assertContents(bank, corrected);
				assertContents(bank, saved);
			}
		}
	}

	@Test
	public void capturedPhysicalPlacementSurvivesReviewedStockAndUpgradeWithdrawal()
	{
		for (int[] pair : REVIEWED_PAIRS)
		{
			Map<Integer, GearStats> stats = reviewedStats(pair);
			BankSnapshot owned = pairBank(pair, 25, 1);
			BankOrganizationPreview before = build(owned, stats);
			int chosenTab = 8;
			BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(chosenTab,
				Arrays.asList(pair[1], pair[0])), before, BlueprintItemOrders.EMPTY);
			assertTrue(captured.isCaptured());
			for (int quantity : new int[] {1, 0, 25})
			{
				BankSnapshot bank = pairBank(pair, quantity, 0);
				BankOrganizationPreview preview = build(bank, stats, id -> 39000, CategoryOverrideSource.NONE,
					defaults().withItemOrders(captured));
				assertEquals(Arrays.asList(pair[1], pair[0]), preview.getCategories().get(chosenTab).getItems()
					.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
				assertContents(bank, preview);
			}
		}
	}

	@Test
	public void unreviewedStockStillNeedsQuantityActualOwnershipAndConservativeBackupProof()
	{
		Map<Integer, GearStats> stats = unknownStats();
		for (int quantity : new int[] {1, 0})
			assertTag(build(unknownBank(quantity, 1, 1), stats), UNKNOWN_STOCK, "gear");
		assertTag(build(unknownBank(3, 1, 1), stats), UNKNOWN_STOCK, "alch");
		assertTag(build(unknownBank(25, 0, 0), stats), UNKNOWN_STOCK, "gear");
		assertTag(build(bank(entry(UNKNOWN_STOCK, 3, 0), entry(UNKNOWN_UPGRADE, 1, 1)), stats),
			UNKNOWN_STOCK, "gear");
		BankSnapshot bulk = bank(entry(UNKNOWN_STOCK, 8, 0), entry(UNKNOWN_UPGRADE, 1, 1));
		assertTag(build(bulk, stats, id -> 1000, CategoryOverrideSource.NONE, defaults()), UNKNOWN_STOCK, "alch");
		assertTag(build(bulk, stats, id -> 999, CategoryOverrideSource.NONE, defaults()), UNKNOWN_STOCK, "gear");
		assertTag(build(unknownBank(3, 1, 1), stats, id -> 4999, CategoryOverrideSource.NONE, defaults()),
			UNKNOWN_STOCK, "gear");
	}

	@Test
	public void unreviewedNicheGearAndUnknownSourceMetadataDoNotBecomeAlchByScore()
	{
		Map<Integer, GearStats> stats = unknownStats();
		stats.put(UNKNOWN_STOCK, new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			10, 10, 10, 10, 900, 0, 0));
		assertFalse(stats.get(UNKNOWN_UPGRADE).dominates(stats.get(UNKNOWN_STOCK)));
		assertFalse(stats.get(UNKNOWN_BACKUP).dominates(stats.get(UNKNOWN_STOCK)));
		assertTag(build(unknownBank(25, 1, 1), stats), UNKNOWN_STOCK, "gear");
		BankOrganizationPreview unknown = BankOrganizationPreviewBuilder.build(unknownBank(25, 1, 1),
			CompositeItemCatalog.DEFAULT, preset, id -> Optional.ofNullable(unknownStats().get(id)),
			id -> 39000, CategoryOverrideSource.NONE, plan, defaults());
		assertFalse("Unknown catalogue entries cannot become reviewed Alch stock",
			"alch".equals(item(unknown, UNKNOWN_STOCK).getLayoutTagKey()));
	}

	@Test
	public void thePreferredMeleeDefenderStaysSelectedWhenItBecomesAPlaceholder()
	{
		int defender = 12954;
		int shield = 1540;
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(defender, new GearStats(GearSlot.SHIELD, 25, 24, 23, -3, -2, 6, 0, 0,
			25, 24, 23, -3, -2, 0, 0));
		stats.put(shield, new GearStats(GearSlot.SHIELD, 0, 0, 0, -3, 0, 0, 0, 0,
			7, 8, 9, 2, 8, 0, 0));
		for (int defenderQuantity : new int[] {1, 0, 1})
		{
			BankSnapshot bank = bank(entry(shield, 1, 0), entry(defender, defenderQuantity, 1));
			BankOrganizationPreview preview = build(bank, stats);
			List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
			List<Integer> primary = GearItemSorter.plan(combat, id -> Optional.ofNullable(stats.get(id)))
				.getSetupRows().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
			assertTrue("Dragon defender remains in the primary equipment selection", primary.contains(defender));
			assertFalse("Anti-dragon shield remains a secondary option", primary.contains(shield));
			assertContents(bank, preview);
		}
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats)
	{
		return build(bank, stats, id -> 39000, CategoryOverrideSource.NONE, defaults());
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats,
		ItemValueSource values, CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CATALOG, preset, id -> Optional.ofNullable(stats.get(id)),
			values, overrides, plan, options);
	}

	private BankLayoutOptions defaults() { return BankLayoutOptions.defaultFor(preset); }

	private static Map<Integer, GearStats> reviewedStats(int[] pair)
	{
		GearSlot slot = pair[0] == 1127 ? GearSlot.BODY : pair[0] == 4103 ? GearSlot.LEGS : GearSlot.WEAPON;
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		// The reviewed role/tier is the replacement proof despite a defensive tradeoff.
		stats.put(pair[0], fullStats(slot, 10, 100));
		stats.put(pair[1], fullStats(slot, 30, 80));
		return stats;
	}

	private static Map<Integer, GearStats> unknownStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(UNKNOWN_STOCK, fullStats(GearSlot.BODY, 10, 10));
		stats.put(UNKNOWN_UPGRADE, fullStats(GearSlot.BODY, 30, 30));
		stats.put(UNKNOWN_BACKUP, fullStats(GearSlot.BODY, 20, 20));
		return stats;
	}

	private static GearStats fullStats(GearSlot slot, int strength, int defence)
	{
		return new GearStats(slot, strength, strength, strength, 0, 0, strength, 0, 0,
			defence, defence, defence, defence, defence, 0, 4);
	}

	private static BankSnapshot pairBank(int[] pair, int stockQuantity, int upgradeQuantity)
	{
		return bank(entry(pair[0], stockQuantity, 0), entry(pair[1], upgradeQuantity, 1));
	}

	private static BankSnapshot unknownBank(int quantity, int upgradeQuantity, int backupQuantity)
	{
		return bank(entry(UNKNOWN_STOCK, quantity, 0), entry(UNKNOWN_UPGRADE, upgradeQuantity, 1),
			entry(UNKNOWN_BACKUP, backupQuantity, 2));
	}

	private static BankItemSnapshot entry(int id, int quantity, int slot)
	{
		return new BankItemSnapshot(id, quantity, slot, quantity == 0);
	}

	private static BankSnapshot bank(BankItemSnapshot... entries) { return new BankSnapshot(Arrays.asList(entries)); }

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
		assertTrue("Item " + id + " belongs in destination " + tab, preview.getCategories().get(tab).getItems()
			.stream().anyMatch(item -> item.getItemId() == id));
	}

	private static void assertContents(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		for (BankItemSnapshot original : bank.getItems())
		{
			BankPreviewItem planned = item(preview, original.getItemId());
			assertEquals(original.getQuantity(), planned.getQuantity());
			assertEquals(original.isPlaceholder(), planned.isPlaceholder());
			assertEquals(original.getPhysicalSlotQuantities(), planned.physicalBankSlots().stream()
				.map(BankPreviewItem::getQuantity).collect(Collectors.toList()));
		}
	}
}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PlaceholderArmourAlchTest
{
	private static final int RUNE_FULL = 1163;
	private static final int RUNE_MED = 1147;
	private static final int RUNE_SKIRT = 1093;
	private static final int NEITIZNOT = 10828;
	private static final int BANDOS_LEGS = 11834;
	private static final int OBSIDIAN_LEGS = 21304;
	private static final int ANCHOR = 10887;
	private static final int UNKNOWN = 900001;
	private static final ItemCatalog CATALOG = id -> Optional.of(CompositeItemCatalog.DEFAULT.findById(id)
		.orElseGet(() -> new CatalogItem(id, "Plain stock " + id, ItemCategory.GEAR,
			"gear", Collections.emptySet(), null)));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public PlaceholderArmourAlchTest(BankPreset preset)
	{
		this.preset = preset;
		this.plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void freshRuneStockUsesHigherTierBankArmourPlaceholdersWithoutAnchorOrSessionHistory()
	{
		Map<Integer, GearStats> stats = replacementStats();
		assertFalse(stats.get(NEITIZNOT).dominates(stats.get(RUNE_FULL)));
		assertFalse(stats.get(BANDOS_LEGS).dominates(stats.get(RUNE_SKIRT)));
		List<BankItemSnapshot> entries = new ArrayList<>(Arrays.asList(
			new BankItemSnapshot(RUNE_FULL, 2, 0), new BankItemSnapshot(RUNE_MED, 4, 1),
			new BankItemSnapshot(RUNE_SKIRT, 1, 2), placeholder(NEITIZNOT, 3), placeholder(BANDOS_LEGS, 4)));
		for (int addition = 0; addition < 2; addition++)
		{
			BankOrganizationPreview preview = build(new BankSnapshot(entries), stats);
			for (int id : new int[] {RUNE_FULL, RUNE_MED, RUNE_SKIRT}) assertTag(preview, id, "alch");
			assertTag(preview, NEITIZNOT, "gear");
			assertTag(preview, BANDOS_LEGS, "gear");
			assertTrue(item(preview, NEITIZNOT).isPlaceholder());
			assertEquals(0, item(preview, NEITIZNOT).getQuantity());
			assertEquals(entries.size(), preview.getPlannedItemCount());
			if (addition > 0) assertTag(preview, ANCHOR, "gear");
			entries.add(new BankItemSnapshot(ANCHOR, 1, entries.size()));
		}
	}

	@Test
	public void ownedObsidianLegsReplaceRuneSkirtDespiteItsDefensiveAdvantage()
	{
		Map<Integer, GearStats> stats = stats(RUNE_SKIRT, armour(GearSlot.LEGS, 0, 100));
		stats.put(OBSIDIAN_LEGS, armour(GearSlot.LEGS, 1, 80));
		assertFalse(stats.get(OBSIDIAN_LEGS).dominates(stats.get(RUNE_SKIRT)));
		BankOrganizationPreview preview = build(bank(new BankItemSnapshot(RUNE_SKIRT, 1, 0),
			new BankItemSnapshot(OBSIDIAN_LEGS, 1, 1)), stats);
		assertTag(preview, RUNE_SKIRT, "alch");
		assertTag(preview, OBSIDIAN_LEGS, "gear");
	}

	@Test
	public void wrongStyleWrongSlotAndEqualTierPlaceholdersDoNotReplaceRuneHelm()
	{
		for (int mismatch = 0; mismatch < 3; mismatch++)
		{
			int upgrade = mismatch == 2 ? RUNE_MED : NEITIZNOT;
			Map<Integer, GearStats> stats = stats(RUNE_FULL, armour(GearSlot.HEAD, 0, 100));
			stats.put(upgrade, mismatch == 0
				? new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 30, 0, 0, 0, 200, 200, 200, 200, 200, 0, 0)
				: armour(mismatch == 1 ? GearSlot.LEGS : GearSlot.HEAD, 3, 200));
			BankOrganizationPreview preview = build(bank(new BankItemSnapshot(RUNE_FULL, 2, 0),
				placeholder(upgrade, 1)), stats);
			assertTag(preview, RUNE_FULL, "gear");
			assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
		}
	}

	@Test
	public void anUnknownTierDominatorRequiresActualOwnership()
	{
		Map<Integer, GearStats> stats = stats(RUNE_FULL, armour(GearSlot.HEAD, 0, 100));
		stats.put(UNKNOWN, armour(GearSlot.HEAD, 3, 200));
		assertTrue(stats.get(UNKNOWN).dominates(stats.get(RUNE_FULL)));
		BankOrganizationPreview withdrawn = build(bank(new BankItemSnapshot(RUNE_FULL, 2, 0),
			placeholder(UNKNOWN, 1)), stats);
		assertTag(withdrawn, RUNE_FULL, "gear");
		BankOrganizationPreview owned = build(bank(new BankItemSnapshot(RUNE_FULL, 2, 0),
			new BankItemSnapshot(UNKNOWN, 1, 1)), stats);
		assertTag(owned, RUNE_FULL, "alch");
	}

	@Test
	public void unreviewedBulkStockCannotUsePlaceholderDominance()
	{
		Map<Integer, GearStats> stats = stats(UNKNOWN, armour(GearSlot.HEAD, 0, 100));
		stats.put(NEITIZNOT, armour(GearSlot.HEAD, 3, 200));
		assertTrue(stats.get(NEITIZNOT).dominates(stats.get(UNKNOWN)));
		BankOrganizationPreview preview = build(bank(new BankItemSnapshot(UNKNOWN, 20, 0),
			placeholder(NEITIZNOT, 1)), stats);
		assertTag(preview, UNKNOWN, "gear");
	}

	@Test
	public void weaponToolAndStaffReplacementRolesStillNeedActualOwnership()
	{
		for (int[] pair : new int[][] {{1333, 4587}, {1275, 11920}, {1387, 1401}})
		{
			Map<Integer, GearStats> stats = stats(pair[0], armour(GearSlot.WEAPON, 10, 100));
			stats.put(pair[1], armour(GearSlot.WEAPON, 20, 200));
			BankOrganizationPreview preview = build(bank(new BankItemSnapshot(pair[0], 4, 0),
				placeholder(pair[1], 1)), stats);
			assertFalse("Stock " + pair[0], "alch".equals(item(preview, pair[0]).getLayoutTagKey()));
		}
	}

	@Test
	public void placeholderStockNeverBecomesAnAutomaticAlchItem()
	{
		BankOrganizationPreview preview = build(bank(placeholder(RUNE_FULL, 0),
			new BankItemSnapshot(NEITIZNOT, 1, 1)), replacementStats());
		assertTag(preview, RUNE_FULL, "gear");
		assertTrue(item(preview, RUNE_FULL).isPlaceholder());
	}

	@Test
	public void bothStockAndPlaceholderNeedEquipmentStats()
	{
		for (int missing : new int[] {RUNE_FULL, NEITIZNOT})
		{
			Map<Integer, GearStats> stats = replacementStats();
			stats.remove(missing);
			assertTag(build(helmBank(), stats), RUNE_FULL, "gear");
		}
	}

	@Test
	public void gatheringOffAndManualGearPinsWinOverPlaceholderTierEvidence()
	{
		assertTag(build(helmBank(), replacementStats(), CategoryOverrideSource.NONE,
			new BankLayoutOptions(true, true, false)), RUNE_FULL, "gear");
		for (String pin : Arrays.asList("gear", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> id == RUNE_FULL ? Optional.of(pin) : Optional.empty();
			assertTag(build(helmBank(), replacementStats(), overrides, defaults()), RUNE_FULL, "gear");
		}
	}

	@Test
	public void aSavedGearDestinationKeepsTheReviewedStockInTheChosenTab()
	{
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			RUNE_FULL + "#0", new BlueprintItemOrders.Destination(plan.destinationOf("gear"), "alch", "gear")));
		BankOrganizationPreview preview = build(helmBank(), replacementStats(), CategoryOverrideSource.NONE,
			defaults().withItemOrders(orders));
		assertTag(preview, RUNE_FULL, "gear");
		assertAtGear(preview, RUNE_FULL);
	}

	@Test
	public void capturedPhysicalGearPlacementSurvivesTheNewAutomaticArmourRule()
	{
		BankOrganizationPreview before = build(helmBank(), replacementStats(), CategoryOverrideSource.NONE,
			new BankLayoutOptions(true, true, false));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(
			plan.destinationOf("gear"), Arrays.asList(RUNE_FULL, NEITIZNOT)), before, BlueprintItemOrders.EMPTY);
		assertTrue(captured.isCaptured());
		BankOrganizationPreview after = build(helmBank(), replacementStats(), CategoryOverrideSource.NONE,
			defaults().withItemOrders(captured));
		assertAtGear(after, RUNE_FULL);
		assertAtGear(after, NEITIZNOT);
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats)
	{
		return build(bank, stats, CategoryOverrideSource.NONE, defaults());
	}

	private BankOrganizationPreview build(BankSnapshot bank, Map<Integer, GearStats> stats,
		CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CATALOG, preset, id -> Optional.ofNullable(stats.get(id)),
			id -> 39000, overrides, plan, options);
	}

	private BankLayoutOptions defaults() { return BankLayoutOptions.defaultFor(preset); }

	private static BankSnapshot helmBank()
	{
		return bank(new BankItemSnapshot(RUNE_FULL, 2, 0), placeholder(NEITIZNOT, 1));
	}

	private static BankSnapshot bank(BankItemSnapshot... items) { return new BankSnapshot(Arrays.asList(items)); }

	private static BankItemSnapshot placeholder(int id, int slot) { return new BankItemSnapshot(id, 0, slot, true); }

	/** Synthetic vectors probe the tier rule independently of full stat dominance. */
	private static Map<Integer, GearStats> replacementStats()
	{
		Map<Integer, GearStats> stats = stats(RUNE_FULL, armour(GearSlot.HEAD, 0, 100));
		stats.put(RUNE_MED, armour(GearSlot.HEAD, 0, 100));
		stats.put(RUNE_SKIRT, armour(GearSlot.LEGS, 0, 100));
		stats.put(NEITIZNOT, armour(GearSlot.HEAD, 3, 80));
		stats.put(BANDOS_LEGS, armour(GearSlot.LEGS, 2, 80));
		stats.put(ANCHOR, armour(GearSlot.WEAPON, 10, 80));
		return stats;
	}

	private static Map<Integer, GearStats> stats(int id, GearStats value)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(id, value);
		return stats;
	}

	private static GearStats armour(GearSlot slot, int strength, int defence)
	{
		return new GearStats(slot, 0, 0, 0, 0, 0, strength, 0, 0,
			defence, defence, defence, defence, defence, 0, 0);
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

	private void assertAtGear(BankOrganizationPreview preview, int id)
	{
		assertTrue("Gear destination must contain " + id, preview.getCategories().get(plan.destinationOf("gear"))
			.getItems().stream().anyMatch(item -> item.getItemId() == id));
	}
}

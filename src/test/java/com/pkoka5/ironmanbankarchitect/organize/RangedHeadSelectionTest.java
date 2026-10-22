package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** General Ranged headgear uses its offensive role, rather than a Slayer helm's tank bonuses. */
@RunWith(Parameterized.class)
public class RangedHeadSelectionTest
{
	private static final int ARCHER = 3749;
	private static final int NEITIZNOT = 10828;
	private static final int BERSERKER = 3751;
	private static final int TORVA = 26382;
	private static final int SLAYER = 11864;
	private static final int[] IMBUED = {11865, 25177, 26674};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public RangedHeadSelectionTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void archerHelmBeatsEveryReportedImbuedSlayerVariantForGeneralRanged()
	{
		Map<Integer, GearStats> stats = actualStats();
		assertTrue("the old combined defence score explains the wrong selection",
			stats.get(11865).score(GearStyle.RANGED) > stats.get(ARCHER).score(GearStyle.RANGED));
		for (int slayer : IMBUED)
		{
			List<BankPreviewItem> input = items(Arrays.asList(NEITIZNOT, BERSERKER, TORVA, SLAYER, slayer, ARCHER),
				Collections.emptySet());
			Map<Integer, Integer> targets = targets(input, stats);
			assertEquals("Archer supplies the general Ranged head slot", Integer.valueOf(1), targets.get(ARCHER));
			assertEquals("Torva remains the Melee head", Integer.valueOf(0), targets.get(TORVA));
			assertFalse("Slayer remains available as a task-specific alternative", targets.containsKey(slayer));
			assertFalse(targets.containsKey(SLAYER));
		}
	}

	@Test
	public void densePreviewKeepsTheArcherTargetWhenHeadgearIsWithdrawnOrInputIsShuffled()
	{
		Map<Integer, GearStats> stats = actualStats();
		List<Integer> ids = denseBank();
		Set<Integer> heads = new HashSet<>(Arrays.asList(ARCHER, NEITIZNOT, BERSERKER, TORVA, SLAYER,
			11865, 25177, 26674));
		Random random = new Random(2026102203749L);
		for (int trial = 0; trial < 8; trial++)
		{
			Collections.shuffle(ids, random);
			Set<Integer> withdrawn = trial < 4 ? Collections.emptySet() : heads;
			List<BankPreviewItem> input = items(ids, withdrawn);
			assertEquals(Integer.valueOf(1), targets(input, stats).get(ARCHER));
			BankOrganizationPreview preview = build(ids, withdrawn, stats, BlueprintItemOrders.EMPTY);
			assertEquals("the selected helmet owns the real Ranged column's first cell", Integer.valueOf(ARCHER),
				combatIds(preview).get(1));
		}
	}

	@Test
	public void pureMeleeHelmetsNeverBecomeSelectedRangedHeadsWhenNoRangedHelmetExists()
	{
		List<BankPreviewItem> input = items(Arrays.asList(NEITIZNOT, BERSERKER, TORVA, SLAYER),
			Collections.emptySet());
		Map<Integer, Integer> targets = targets(input, actualStats());
		assertEquals(1, targets.size());
		assertEquals(Integer.valueOf(0), targets.get(TORVA));
		assertTrue("a compact bank may have loose fillers, but none is a selected Ranged helmet",
			targets.values().stream().noneMatch(target -> target % 8 == 1));
	}

	@Test
	public void rangedStrengthPrecedesAccuracyAndDefenceForOtherwiseEqualHeadRoles()
	{
		int power = 990701, accuracy = 990702;
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(power, head(0, 0, 0, 0, 1, 0, 2, 0, 1, 1, 1, 1, 1));
		stats.put(accuracy, head(0, 0, 0, 0, 100, 0, 0, 0, 100, 100, 100, 100, 100));
		Map<Integer, Integer> targets = targets(items(Arrays.asList(accuracy, power), Collections.emptySet()), stats);
		assertEquals(Integer.valueOf(1), targets.get(power));
		assertFalse(targets.containsKey(accuracy));
	}

	@Test
	public void betterGeneralRangedAccuracyStillPromotesArmadylOverArcher()
	{
		Map<Integer, Integer> targets = targets(items(Arrays.asList(ARCHER, 11826, 11865),
			Collections.emptySet()), actualStats());
		assertEquals(Integer.valueOf(1), targets.get(11826));
		assertFalse(targets.containsKey(ARCHER));
	}

	@Test
	public void bowfaStillSelectsCrystalHeadgearBeforeStandaloneMasoriBonuses()
	{
		Map<Integer, Integer> targets = targets(items(Arrays.asList(ARCHER, 23971, 27235, 25867),
			Collections.emptySet()), actualStats());
		assertEquals("the selected Bowfa's Crystal multiplier remains the earlier contextual rule",
			Integer.valueOf(1), targets.get(23971));
		assertFalse(targets.containsKey(27235));
	}

	@Test
	public void twistedBowLeavesTheGeneralMasoriHeadPreferenceIntact()
	{
		Map<Integer, Integer> targets = targets(items(Arrays.asList(ARCHER, 23971, 27235, 25867, 20997),
			Collections.emptySet()), actualStats());
		assertEquals(Integer.valueOf(1), targets.get(27235));
		assertFalse(targets.containsKey(23971));
	}

	@Test
	public void archerNameFallbackRemainsInRangedWhenSupportedEquipmentStatsAreAbsent()
	{
		List<BankPreviewItem> input = items(Arrays.asList(ARCHER, NEITIZNOT, BERSERKER), Collections.emptySet());
		assertEquals(Integer.valueOf(1), targets(input, Collections.emptyMap()).get(ARCHER));
	}

	@Test
	public void savedManualAndCapturedOrdersKeepThePlayersChosenSlayerHelmetPosition()
	{
		Map<Integer, GearStats> stats = actualStats();
		List<Integer> desired = denseBank();
		desired.remove(Integer.valueOf(26674));
		desired.add(1, 26674);
		int combat = plan.destinationOf("gear");
		BlueprintItemOrders manual = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY.withTab(combat, desired).serialize());
		BankOrganizationPreview automatic = build(desired, Collections.emptySet(), stats, BlueprintItemOrders.EMPTY);
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(combat, desired),
			automatic, BlueprintItemOrders.EMPTY);
		assertTrue(captured.isCaptured());
		for (BlueprintItemOrders orders : Arrays.asList(manual, captured))
			for (Set<Integer> withdrawn : Arrays.asList(Collections.<Integer>emptySet(),
				new HashSet<>(Arrays.asList(ARCHER, 26674))))
				assertEquals("saved physical order wins over the automatic headgear ranking", desired,
					combatIds(build(desired, withdrawn, stats, orders)));
	}

	private BankOrganizationPreview build(List<Integer> ids, Set<Integer> withdrawn,
		Map<Integer, GearStats> stats, BlueprintItemOrders orders)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : ids)
			entries.add(new BankItemSnapshot(id, withdrawn.contains(id) ? 0 : 1, entries.size(), withdrawn.contains(id)));
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(entries),
			id -> Optional.of(catalog(id)), preset, id -> Optional.ofNullable(stats.get(id)), ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
		assertEquals(ids.size(), preview.getPlannedItemCount());
		assertEquals(new HashSet<>(ids), new HashSet<>(preview.getPlannedItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList())));
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertEquals(withdrawn.contains(item.getItemId()), item.isPlaceholder());
			assertEquals(withdrawn.contains(item.getItemId()) ? 0 : 1, item.getQuantity());
		}
		return preview;
	}

	private List<Integer> combatIds(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("gear")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static Map<Integer, Integer> targets(List<BankPreviewItem> input, Map<Integer, GearStats> stats)
	{
		GearItemSorter.GearLayout selection = GearItemSorter.plan(input, id -> Optional.ofNullable(stats.get(id)));
		assertEquals(input.size(), selection.getSetupRows().size() + selection.getTail().size());
		return GearItemSorter.setupTargets(selection, 128);
	}

	private static List<BankPreviewItem> items(List<Integer> ids, Set<Integer> withdrawn)
	{
		return ids.stream().map(id -> new BankPreviewItem(catalog(id), withdrawn.contains(id) ? 0 : 1,
			withdrawn.contains(id))).collect(Collectors.toList());
	}

	private static CatalogItem catalog(int id)
	{
		return CompositeItemCatalog.DEFAULT.findById(id).orElseGet(() -> new CatalogItem(id,
			"Ranged head regression " + id, ItemCategory.GEAR, id < 990710 ? "head" : "gear",
			Collections.emptySet(), null));
	}

	private static List<Integer> denseBank()
	{
		List<Integer> ids = new ArrayList<>(Arrays.asList(ARCHER, NEITIZNOT, BERSERKER, TORVA, SLAYER,
			11865, 25177, 26674, 11832, 11834, 2503, 2497, 4091, 4093));
		for (int index = 0; index < 10; index++) ids.add(990710 + index);
		return ids;
	}

	private static Map<Integer, GearStats> actualStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		// Frozen RuneLite vectors in reported-midgame-combat-85.tsv, checked against Wiki equipment stats.
		stats.put(ARCHER, head(-5, -5, -5, -5, 6, 0, 0, 0, 6, 8, 10, 6, 6));
		stats.put(NEITIZNOT, head(0, 0, 0, 0, 0, 3, 0, 3, 31, 29, 34, 3, 30));
		for (int id : IMBUED) stats.put(id, head(0, 0, 0, 3, 3, 0, 0, 0, 30, 32, 27, 10, 30));
		// Berserker_helm?oldid=14546161; ordinary Slayer excludes the imbued offensive bonuses.
		stats.put(BERSERKER, head(0, 0, 0, -5, -5, 3, 0, 0, 31, 29, 33, 0, 30));
		stats.put(SLAYER, head(0, 0, 0, -6, -2, 0, 0, 0, 30, 32, 27, -1, 30));
		// Existing independent combat-quality-facts.tsv: Torva, Armadyl, Crystal and Masori.
		stats.put(TORVA, head(0, 0, 0, -5, -5, 8, 0, 1, 59, 60, 62, -2, 57));
		stats.put(11826, head(-5, -5, -5, -5, 10, 0, 0, 1, 6, 8, 10, 10, 8));
		stats.put(23971, head(0, 0, 0, -10, 9, 0, 0, 2, 12, 8, 14, 10, 18));
		stats.put(27235, head(0, 0, 0, -1, 12, 0, 2, 1, 8, 10, 12, 12, 9));
		stats.put(25867, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 128, 0, 106, 0,
			0, 0, 0, 0, 0, 0, 5));
		stats.put(20997, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 70, 0, 20, 0,
			0, 0, 0, 0, 0, 0, 6));
		// Only supporting body/legs geometry uses synthetic vectors; head selection uses the facts above.
		for (int id : new int[] {11832, 11834, 2503, 2497, 4091, 4093})
		{
			GearSlot slot = id == 11832 || id == 2503 || id == 4091 ? GearSlot.BODY : GearSlot.LEGS;
			stats.put(id, new GearStats(slot, 0, 0, 0, id == 4091 || id == 4093 ? 20 : 0,
				id == 2503 || id == 2497 ? 20 : 0, id == 11832 || id == 11834 ? 3 : 0, 0, 0,
				10, 10, 10, 10, 10, 0, 0));
		}
		return stats;
	}

	private static GearStats head(int stab, int slash, int crush, int magic, int ranged, int meleeStrength,
		int rangedStrength, int prayer, int stabDefence, int slashDefence, int crushDefence, int magicDefence,
		int rangedDefence)
	{
		return new GearStats(GearSlot.HEAD, stab, slash, crush, magic, ranged, meleeStrength, rangedStrength,
			prayer, stabDefence, slashDefence, crushDefence, magicDefence, rangedDefence, 0, 0);
	}
}

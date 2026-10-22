package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Physical cluster expectations authored independently of the layout planner. */
@RunWith(Parameterized.class)
public class RingAndWeaponClustersTest
{
	private static final int[][] PRIMARY_COLUMNS = {
		{10828,11832,11834,6570,31088,6585,26219,12954,7462,26770},
		{3749,2503,2497,10499,29286,25865},
		{3755,4091,4093,21795,6920,31115,6889}
	};
	private static final int[] MELEE_WEAPONS = {
		4151,4587,13265,28997,29889,1215,10887,11061,6746,10858,24699,4158,7668
	};
	private static final int[] WEAPON_CELLS = {3,4,5,6,7,11,12,13,14,15,19,20,21};
	private static final Set<Integer> EXTRA_RINGS = immutableSet(25975,2550,6735,21140,2570,12601);
	private static final Set<Integer> ALL_RINGS = immutableSet(26770,25975,2550,6735,21140,2570,12601);
	private static final Set<Integer> PRIMARY = primaryIds();
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	private final Map<Integer, CatalogItem> catalog = new LinkedHashMap<>();
	private final Map<Integer, GearStats> stats = new LinkedHashMap<>();
	private final Map<Integer, Integer> quantities = new LinkedHashMap<>();
	private final List<Integer> sourceIds = new ArrayList<>();

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public RingAndWeaponClustersTest(BankPreset preset) throws Exception
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			RingAndWeaponClustersTest.class.getResourceAsStream("reported-midgame-combat-85.tsv")), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] row = line.split("\t", -1);
				assertEquals("frozen fixture schema", 6, row.length);
				int id = Integer.parseInt(row[0]);
				assertFalse("fixture contains each physical ID once: " + id, catalog.containsKey(id));
				sourceIds.add(id);
				catalog.put(id, new CatalogItem(id, row[1], ItemCategory.valueOf(row[2]), row[3], Collections.emptySet(), null));
				quantities.put(id, Integer.parseInt(row[4]));
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				assertEquals("complete frozen equipment vector", 16, v.length);
				GearSlot slot = GearSlot.fromRuneLiteSlot(v[0]);
				assertNotNull("known equipment slot for " + id, slot);
				stats.put(id, new GearStats(slot, v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8],
					v[9], v[10], v[11], v[12], v[13], v[14], v[15]));
			}
		}
		assertEquals("exact reported Combat fixture", 85, sourceIds.size());
		assertEquals("the three full primary columns still select 23 actual items", 23, PRIMARY.size());
	}

	@Test
	public void secondaryMeleeWeaponsUseTheRightHandRegionBesideUnchangedFullColumns()
	{
		BankSnapshot bank = bank(sourceIds, Collections.emptySet(), false);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> actual = combatIds(preview);
		assertPrimaryColumns(actual);
		assertWeaponRegion(actual);
		assertPreserved(bank, preview);
	}

	@Test
	public void everyRingSharesOnePhysicalRowBesideTheSelectedMeleeRing()
	{
		BankSnapshot bank = bank(sourceIds, Collections.emptySet(), false);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> actual = combatIds(preview);
		assertPrimaryColumns(actual);
		assertRingRow(actual);
		assertEquals("fixture identifies exactly these seven rings", ALL_RINGS, stats.entrySet().stream()
			.filter(entry -> entry.getValue().getSlot() == GearSlot.RING).map(Map.Entry::getKey).collect(Collectors.toSet()));
		assertPreserved(bank, preview);
	}

	@Test
	public void sourceShufflesQuantitiesAndWithdrawnRingPlaceholdersKeepBothClustersStable()
	{
		List<Integer> expected = combatIds(build(bank(sourceIds, Collections.emptySet(), false), BlueprintItemOrders.EMPTY));
		Random random = new Random(202610130085L);
		for (int trial = 0; trial < 5; trial++)
		{
			List<Integer> shuffled = new ArrayList<>(sourceIds);
			Collections.shuffle(shuffled, random);
			Set<Integer> withdrawn = new HashSet<>();
			if (trial >= 2) withdrawn.addAll(ALL_RINGS);
			if (trial >= 3) withdrawn.addAll(PRIMARY);
			BankSnapshot bank = bank(shuffled, withdrawn, trial % 2 == 1);
			BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
			List<Integer> actual = combatIds(preview);
			assertEquals("physical order remains stable in trial " + trial, expected, actual);
			assertPrimaryColumns(actual);
			assertWeaponRegion(actual);
			assertRingRow(actual);
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void anEntireWithdrawnRingRowRetainsItsSevenRealPlaceholderCells()
	{
		BankSnapshot bank = bank(sourceIds, ALL_RINGS, false);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		assertRingRow(combatIds(preview));
		List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
		for (int cell = 72; cell < 79; cell++)
		{
			assertTrue("withdrawn ring stays a placeholder at " + cell, combat.get(cell).isPlaceholder());
			assertEquals("withdrawn ring quantity at " + cell, 0, combat.get(cell).getQuantity());
		}
		assertPreserved(bank, preview);
	}

	@Test
	public void savedPhysicalManualOrderOverridesBothAutomaticClusters()
	{
		List<Integer> desired = new ArrayList<>(sourceIds);
		if (preset.getType() == BankPresetType.MAIN) desired.remove(Integer.valueOf(9084));
		Collections.reverse(desired);
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("gear"), desired).serialize());
		BankSnapshot bank = bank(sourceIds, ALL_RINGS, true);
		BankOrganizationPreview preview = build(bank, saved);
		assertEquals("manual order wins for every real physical item", desired, combatIds(preview));
		assertTrue("manual layout stays identifiable", preview.getCategories().get(plan.destinationOf("gear")).hasManualOrder());
		assertPreserved(bank, preview);
	}

	private BankOrganizationPreview build(BankSnapshot bank, BlueprintItemOrders order)
	{
		return BankOrganizationPreviewBuilder.build(bank, id -> Optional.ofNullable(catalog.get(id)), preset,
			id -> Optional.ofNullable(stats.get(id)), ItemValueSource.NONE, CategoryOverrideSource.NONE, plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(order));
	}

	private BankSnapshot bank(List<Integer> ids, Set<Integer> withdrawn, boolean variedQuantities)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : ids)
		{
			int quantity = withdrawn.contains(id) ? 0 : variedQuantities ? id % 7 + 2 : quantities.get(id);
			entries.add(new BankItemSnapshot(id, quantity, entries.size(), quantity == 0));
		}
		return new BankSnapshot(entries);
	}

	private List<Integer> combatIds(BankOrganizationPreview preview)
	{
		List<Integer> result = preview.getCategories().get(plan.destinationOf("gear")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
		assertEquals("Main independently keeps Lunar staff in transport", preset.getType() == BankPresetType.MAIN ? 84 : 85,
			result.size());
		return result;
	}

	private static void assertPrimaryColumns(List<Integer> actual)
	{
		for (int column = 0; column < PRIMARY_COLUMNS.length; column++)
			for (int row = 0; row < PRIMARY_COLUMNS[column].length; row++)
				assertEquals("unchanged primary row " + row + ", style " + column,
					Integer.valueOf(PRIMARY_COLUMNS[column][row]), actual.get(row * 8 + column));
	}

	private static void assertWeaponRegion(List<Integer> actual)
	{
		for (int weapon = 0; weapon < MELEE_WEAPONS.length; weapon++)
		{
			int cell = WEAPON_CELLS[weapon];
			assertTrue("secondary Melee weapon is to the right of primary columns", cell % 8 >= 3);
			assertEquals("independent role-cluster target at cell " + cell,
				Integer.valueOf(MELEE_WEAPONS[weapon]), actual.get(cell));
		}
		assertEquals("the reviewed thirteen secondary weapons appear once", 13,
			actual.stream().filter(id -> Arrays.stream(MELEE_WEAPONS).anyMatch(weapon -> weapon == id)).count());
	}

	private static void assertRingRow(List<Integer> actual)
	{
		assertEquals("primary strength ring retains the bottom of Melee", Integer.valueOf(26770), actual.get(72));
		assertEquals("six extra rings occupy adjacent cells without prescribing their order", EXTRA_RINGS,
			new HashSet<>(actual.subList(73, 79)));
		for (int id : ALL_RINGS)
			assertEquals("every ring occupies the same physical row: " + id, 9, actual.indexOf(id) / 8);
		assertTrue("Lightbearer joins the ring cluster instead of occupying the first rows", actual.indexOf(25975) >= 73);
	}

	private static void assertPreserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		Map<Integer, BankItemSnapshot> expected = new LinkedHashMap<>();
		for (BankItemSnapshot item : bank.getItems()) expected.put(item.getItemId(), item);
		assertEquals("every real physical entry survives", bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		Set<Integer> seen = new HashSet<>();
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertFalse("no invented empty bank cell", item.isBlank());
			assertTrue("no duplicate physical entry for " + item.getItemId(), seen.add(item.getItemId()));
			BankItemSnapshot original = expected.get(item.getItemId());
			assertNotNull("no invented item " + item.getItemId(), original);
			assertEquals("unchanged quantity for " + item.getItemId(), original.getQuantity(), item.getQuantity());
			assertEquals("unchanged placeholder for " + item.getItemId(), original.isPlaceholder(), item.isPlaceholder());
		}
		assertEquals("no omitted bank items", expected.keySet(), seen);
	}

	private static Set<Integer> immutableSet(Integer... ids)
	{
		return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(ids)));
	}

	private static Set<Integer> primaryIds()
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int[] column : PRIMARY_COLUMNS) for (int id : column) result.add(id);
		return Collections.unmodifiableSet(result);
	}
}

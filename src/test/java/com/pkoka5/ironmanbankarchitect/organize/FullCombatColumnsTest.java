package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Independently specified full-column contract for the actual reported midgame bank. */
@RunWith(Parameterized.class)
public class FullCombatColumnsTest
{
	private static final int[][] COLUMNS = {
		{10828,11832,11834,6570,31088,6585,26219,12954,7462,26770},
		{3749,2503,2497,10499,29286,25865},
		{3755,4091,4093,21795,6920,31115,6889}
	};
	private static final int LIGHTBEARER = 25975;
	private static final int[][] REMAINDERS = {{29280,29283},{9674,9676},{544,542}};
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

	public FullCombatColumnsTest(BankPreset preset) throws Exception
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			FullCombatColumnsTest.class.getResourceAsStream("reported-midgame-combat-85.tsv")), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] row = line.split("\t", -1);
				assertEquals("frozen fixture schema", 6, row.length);
				int id = Integer.parseInt(row[0]);
				assertFalse("fixture ID appears once: " + id, catalog.containsKey(id));
				sourceIds.add(id);
				catalog.put(id, new CatalogItem(id, row[1], ItemCategory.valueOf(row[2]), row[3], Collections.emptySet(), null));
				quantities.put(id, Integer.parseInt(row[4]));
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				assertEquals("frozen complete equipment vector", 16, v.length);
				GearSlot slot = GearSlot.fromRuneLiteSlot(v[0]);
				assertNotNull("fixture equipment slot for " + id, slot);
				stats.put(id, new GearStats(slot, v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8],
					v[9], v[10], v[11], v[12], v[13], v[14], v[15]));
			}
		}
		assertEquals("exact bank fixture", 85, sourceIds.size());
	}

	@Test
	public void everyPrimaryCapeBootAndAccessoryJoinsItsFullVerticalStyleColumn()
	{
		BankSnapshot bank = bank(sourceIds, Collections.emptySet());
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		assertColumns(combatIds(preview));
		assertPreserved(bank, preview);
		assertEquals("Main keeps its independent Lunar transport destination", preset.getType() == BankPresetType.MAIN ? 84 : 85,
			combatIds(preview).size());
		assertEquals("shared owners are physical items, not references or fabricated copies", 23, PRIMARY.size());
		for (int id : new int[] {6585,7462,31088})
		{
			assertEquals("shared item belongs to the Melee column: " + id, 0, combatIds(preview).indexOf(id) % 8);
			assertEquals("shared item occupies one physical cell: " + id, 1,
				preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).count());
		}
		assertEquals("Lightbearer remains one additional real support item", 1,
			preview.getPlannedItems().stream().filter(item -> item.getItemId() == LIGHTBEARER).count());
	}

	@Test
	public void wholeOwnedSecondaryRemaindersSurviveTheFullColumns()
	{
		BankSnapshot bank = bank(sourceIds, Collections.emptySet());
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> actual = combatIds(preview);
		assertColumns(actual);
		for (int[] family : REMAINDERS) assertCanonicalFamily(actual, family);
		assertEquals("the third Mixed hide member is the real best remaining Ranged boot", 4 * 8 + 1,
			actual.indexOf(29286));
		assertPreserved(bank, preview);
	}

	@Test
	public void inputShufflesAndWithdrawnPrimaryPlaceholdersKeepEveryPhysicalTarget()
	{
		List<Integer> expected = combatIds(build(bank(sourceIds, Collections.emptySet()), BlueprintItemOrders.EMPTY));
		Random random = new Random(202610120085L);
		for (int trial = 0; trial < 4; trial++)
		{
			List<Integer> shuffled = new ArrayList<>(sourceIds);
			Collections.shuffle(shuffled, random);
			Set<Integer> withdrawn = trial < 2 ? Collections.emptySet() : new HashSet<>(PRIMARY);
			if (trial == 3) withdrawn.add(LIGHTBEARER);
			BankSnapshot bank = bank(shuffled, withdrawn);
			BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
			assertEquals("shuffle/placeholder trial " + trial, expected, combatIds(preview));
			assertColumns(combatIds(preview));
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void savedManualOrderOverridesTheAutomaticFullColumns()
	{
		List<Integer> desired = new ArrayList<>(sourceIds);
		if (preset.getType() == BankPresetType.MAIN) desired.remove(Integer.valueOf(9084));
		Collections.reverse(desired);
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("gear"), desired).serialize());
		BankSnapshot bank = bank(sourceIds, Collections.emptySet());
		BankOrganizationPreview preview = build(bank, saved);
		assertEquals("the player keeps the exact chosen physical order", desired, combatIds(preview));
		assertTrue("the UI can distinguish a saved manual layout", preview.getCategories()
			.get(plan.destinationOf("gear")).hasManualOrder());
		assertPreserved(bank, preview);
	}

	@Test
	public void renderTheActualEightColumnResultForHumanReview() throws Exception
	{
		BankSnapshot bank = bank(sourceIds, Collections.emptySet());
		long started = System.nanoTime();
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		long elapsed = System.nanoTime() - started;
		assertColumns(combatIds(preview));
		assertPreserved(bank, preview);
		Set<Integer> highlighted = new LinkedHashSet<>(PRIMARY);
		highlighted.add(LIGHTBEARER);
		CombatQualityReport report = new CombatQualityReport(preset.getType() + "-full-combat-columns-85",
			bank, preview, plan, highlighted, false, elapsed, false, renderFacts());
		assertTrue("independent conservation gates: " + report.violations, report.violations.isEmpty());
		report.write(Paths.get("build/reports/combat-quality"));
	}

	private BankOrganizationPreview build(BankSnapshot bank, BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(bank, id -> Optional.ofNullable(catalog.get(id)), preset,
			id -> Optional.ofNullable(stats.get(id)), ItemValueSource.NONE, CategoryOverrideSource.NONE, plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
	}

	private BankSnapshot bank(List<Integer> ids, Set<Integer> withdrawn)
	{
		List<BankItemSnapshot> rows = new ArrayList<>();
		for (int id : ids)
		{
			int quantity = withdrawn.contains(id) ? 0 : quantities.get(id);
			rows.add(new BankItemSnapshot(id, quantity, rows.size(), quantity == 0));
		}
		return new BankSnapshot(rows);
	}

	private List<Integer> combatIds(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("gear")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertColumns(List<Integer> actual)
	{
		for (int column = 0; column < COLUMNS.length; column++)
			for (int row = 0; row < COLUMNS[column].length; row++)
				assertEquals("independent full-column target row " + row + ", style " + column,
					Integer.valueOf(COLUMNS[column][row]), actual.get(row * 8 + column));
	}

	private static void assertCanonicalFamily(List<Integer> actual, int[] family)
	{
		int first = actual.indexOf(family[0]);
		assertTrue("missing first family member " + family[0], first >= 0);
		boolean dense = true, vertical = true;
		for (int member = 0; member < family.length; member++)
		{
			dense &= actual.indexOf(family[member]) == first + member;
			vertical &= actual.indexOf(family[member]) == first + member * 8;
		}
		assertTrue("whole canonical remaining family " + Arrays.toString(family), dense || vertical);
	}

	private static void assertPreserved(BankSnapshot source, BankOrganizationPreview preview)
	{
		Map<Integer, BankItemSnapshot> expected = new LinkedHashMap<>();
		for (BankItemSnapshot item : source.getItems()) expected.put(item.getItemId(), item);
		Set<Integer> seen = new HashSet<>();
		assertEquals("all physical bank entries survive", source.getPhysicalItems().size(), preview.getPlannedItemCount());
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertFalse("no invented physical gap", item.isBlank());
			assertTrue("no physical duplicate for " + item.getItemId(), seen.add(item.getItemId()));
			BankItemSnapshot original = expected.get(item.getItemId());
			assertNotNull("no invented item " + item.getItemId(), original);
			assertEquals("quantity for " + item.getItemId(), original.getQuantity(), item.getQuantity());
			assertEquals("placeholder for " + item.getItemId(), original.isPlaceholder(), item.isPlaceholder());
		}
		assertEquals("no omitted bank items", expected.keySet(), seen);
	}

	private static Set<Integer> primaryIds()
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int[] column : COLUMNS) for (int id : column) result.add(id);
		return Collections.unmodifiableSet(result);
	}

	private Map<Integer, CombatQualityReport.Fact> renderFacts()
	{
		Set<Integer> ranged = new HashSet<>(Arrays.asList(26674,2503,2497,25865,10499,3749,23985,29280,29283,
			29286,12788,805,8880,865,10034,859,855,10033,4827,847));
		Set<Integer> magic = new HashSet<>(Arrays.asList(3755,4091,4093,31115,21795,8923,6889,6920,20714,
			12899,12658,1381,1385,1383,9084,4675,1393,3053));
		Set<Integer> prayer = new HashSet<>(Arrays.asList(26763,9674,9676,544,542,12601));
		Map<Integer, CombatQualityReport.Fact> facts = new LinkedHashMap<>();
		for (int id : sourceIds)
		{
			String zone = id == LIGHTBEARER ? "S" : ranged.contains(id) ? "R" : magic.contains(id) ? "G" : prayer.contains(id) ? "P" : "M";
			facts.put(id, new CombatQualityReport.Fact(id, zone, "item-" + id, 0, 0, "loose", stats.get(id)));
		}
		return facts;
	}
}

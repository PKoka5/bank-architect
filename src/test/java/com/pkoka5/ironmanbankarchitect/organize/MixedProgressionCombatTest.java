package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Frozen midgame/endgame ownership union; production classification remains authoritative. */
@RunWith(Parameterized.class)
public class MixedProgressionCombatTest
{
	private static final Path REPORTS = Paths.get("build/reports/combat-quality");
	private static final int[][] PRIMARY_COLUMNS = {
		{26382,26384,26386,21295,31097,29801,22325,22322,22981,28307},
		{27235,27238,27241,28951,31088,33639,20997,21000,26235},
		{21018,21021,21024,21791,13235,12002,27275,27251,31106}
	};
	// The ordinary Treads are a separate real pair: after max Treads go to Melee,
	// their frozen +15 Ranged/+2 strength beat the owned Pegasians' +12/+1.
	private static final int[][] SECONDARY_ARMOUR = {
		{30750,30753,30756}, {24419,24420,24421}, {22326,22327,22328},
		{11832,11834,11836}, {4716,4718,4720,4722}, {4753,4755,4757,4759},
		{11826,11828,11830}, {23971,23975,23979}, {26241,26243,26245},
		{28933,28936,28939}, {29280,29283,29286}, {9674,9676}, {544,542}, {4091,4093}
	};
	private static final int[] SPECIAL_WEAPONS = {11802,11804,21003,13576,1215,10887,11061};
	private static final int[] SPECIAL_CELLS = {27,28,29,30,31,35,36};
	private static final int[] SECONDARY_BOWS = {25865,25867,27610,23985,12788,859,855,847};
	private static final int[] BOW_CELLS = {43,44,45,46,47,51,52,53};
	private static final Set<Integer> RINGS = Collections.unmodifiableSet(new LinkedHashSet<>(
		Arrays.asList(28307,26770,25975,2550,6735,21140,2570,12601)));
	private static final Set<Integer> MID_MAGIC_WEAPONS = new HashSet<>(
		Arrays.asList(12658,4675,9084,1381,1383,1385,1393,3053));
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	private final Map<Integer,CombatQualityReport.Fact> facts = new LinkedHashMap<>();
	private final Map<Integer,Integer> quantities = new LinkedHashMap<>();
	private final List<Integer> sourceIds = new ArrayList<>();

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public MixedProgressionCombatTest(BankPreset preset) throws Exception
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			MixedProgressionCombatTest.class.getResourceAsStream("reported-midgame-combat-85.tsv")), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] row = line.split("\t",-1);
				assertEquals("frozen midgame schema",6,row.length);
				int id = Integer.parseInt(row[0]);
				assertFalse("each reported item occurs once",facts.containsKey(id));
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				assertEquals("frozen complete equipment vector",16,v.length);
				GearSlot slot = GearSlot.fromRuneLiteSlot(v[0]);
				assertNotNull("reviewed slot " + id,slot);
				GearStats stats = new GearStats(slot,v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
					v[9],v[10],v[11],v[12],v[13],v[14],v[15]);
				String zone = MID_MAGIC_WEAPONS.contains(id) ? "G" : stats.style() == GearStyle.RANGED ? "R" : stats.style() == GearStyle.MAGIC ? "G"
					: stats.style() == GearStyle.PRAYER ? "P" : "M";
				facts.put(id,new CombatQualityReport.Fact(id,zone,"midgame-" + id,0,id,"loose",stats));
				quantities.put(id,Integer.parseInt(row[4]));
			}
		}
		assertEquals("reported input contains85 identities",85,facts.size());
		for (CombatQualityReport.Fact fact : CombatQualityReport.FACTS.values())
		{
			if (fact.role.equals("variant") || fact.role.equals("space")) continue;
			facts.put(fact.id,fact); // Frozen endgame vectors win where the two inputs overlap.
			quantities.putIfAbsent(fact.id,fact.id%3+1);
		}
		for (int style = 0; style < PRIMARY_COLUMNS.length; style++)
			for (int id : PRIMARY_COLUMNS[style])
			{
				CombatQualityReport.Fact fact = facts.get(id);
				facts.put(id,new CombatQualityReport.Fact(id,CombatQualityReport.ZONES.get(style),fact.family,
					fact.memberOrder,fact.canonicalSlot,fact.role,fact.stats));
			}
		sourceIds.addAll(facts.keySet());
		assertEquals("85 reported items plus102 endgame items share five identities",182,sourceIds.size());
		assertEquals("owned real ring identities",RINGS,facts.values().stream()
			.filter(fact -> fact.stats != null && fact.stats.getSlot() == GearSlot.RING)
			.map(fact -> fact.id).collect(Collectors.toSet()));
	}

	@Test(timeout = 30000)
	public void fullMixedOwnershipKeepsPrimaryColumnsWholeRetainedArmourAndAllEightRings() throws Exception
	{
		BankSnapshot bank = bank(sourceIds,Collections.emptySet());
		long started = System.nanoTime();
		BankOrganizationPreview preview = build(bank);
		long elapsed = System.nanoTime()-started;
		CombatQualityReport report = new CombatQualityReport(preset.getType() + "-mixed-midgame-endgame-182",
			bank,preview,plan,primaryIds(),false,elapsed,false,facts);
		report.write(REPORTS);
		writeDestinations(preview,elapsed);
		assertTrue("every physical stack/placeholder survives: " + report.violations,report.violations.isEmpty());
		assertEquals("no fabricated or missing physical entries",182,preview.getPlannedItemCount());
		List<Integer> combat = combatIds(preview);
		assertColumns(combat);
		assertRings(combat);
		assertRetainedArmour(combat);
		assertTrue("bounded mixed preview generation",elapsed < 10000000000L);
	}

	@Test(timeout = 30000)
	public void shufflesAndWithdrawnBestGearAndRingsRetainEveryMixedTarget() throws Exception
	{
		BankOrganizationPreview original = build(bank(sourceIds,Collections.emptySet()));
		List<Integer> expected = combatIds(original);
		Map<Integer,String> destinations = destinations(original);
		Random random = new Random(202610130182L);
		for (int trial = 0; trial < 4; trial++)
		{
			List<Integer> order = new ArrayList<>(sourceIds);
			Collections.shuffle(order,random);
			Set<Integer> withdrawn = new HashSet<>();
			if (trial >= 1) withdrawn.addAll(primaryIds());
			if (trial >= 2) withdrawn.addAll(RINGS);
			BankSnapshot bank = bank(order,withdrawn);
			BankOrganizationPreview preview = build(bank);
			assertEquals("mixed targets remain stable, trial " + trial,expected,combatIds(preview));
			assertEquals("classification remains stable, trial " + trial,destinations,destinations(preview));
			CombatQualityReport conservation = new CombatQualityReport("mixed-conservation",bank,preview,plan,
				primaryIds(),false,0,false,facts);
			assertTrue("physical stack/placeholder conservation: " + conservation.violations,conservation.violations.isEmpty());
			assertColumns(combatIds(preview));
			assertRings(combatIds(preview));
			assertRetainedArmour(combatIds(preview));
		}
	}

	@Test(timeout = 30000)
	public void elevenRingsKeepAllThreeBestRingsInTheirColumnsAndExtrasTogetherBelow() throws Exception
	{
		// Public RuneLite snapshot captured 2026-10-12; SHA256 recorded in the verification report.
		// Full constructor order: five attacks, two strengths, prayer, five defences, magic damage tenths, speed.
		addRing(28310,"R",new GearStats(GearSlot.RING,0,0,0,0,10,0,2,0,0,0,0,0,0,0,0));
		addRing(28313,"G",new GearStats(GearSlot.RING,0,0,0,15,0,0,0,0,0,0,0,0,0,20,0));
		addRing(28316,"M",new GearStats(GearSlot.RING,0,20,0,0,0,6,0,0,0,0,0,0,0,0,0));
		Set<Integer> allRings = new LinkedHashSet<>(RINGS);
		allRings.addAll(Arrays.asList(28310,28313,28316));
		Set<Integer> primary = primaryIds();
		primary.addAll(Arrays.asList(28310,28313));
		List<Integer> expected = null;
		Map<Integer,String> expectedDestinations = null;
		for (int trial = 0; trial < 3; trial++)
		{
			List<Integer> order = new ArrayList<>(sourceIds);
			if (trial > 0) Collections.shuffle(order,new Random(202610130185L+trial));
			Set<Integer> withdrawn = new HashSet<>();
			if (trial == 2) { withdrawn.addAll(primary); withdrawn.addAll(allRings); }
			BankSnapshot bank = bank(order,withdrawn);
			long started = System.nanoTime();
			BankOrganizationPreview preview = build(bank);
			long elapsed = System.nanoTime()-started;
			CombatQualityReport report = new CombatQualityReport(preset.getType()+"-mixed-complete-rings-185",
				bank,preview,plan,primary,false,elapsed,false,facts);
			if (trial == 0) { report.write(REPORTS); writeDestinations(preview,elapsed); }
			assertTrue("all185 physical entries survive: " + report.violations,report.violations.isEmpty());
			assertEquals(185,preview.getPlannedItemCount());
			List<Integer> combat = combatIds(preview);
			assertColumns(combat);
			assertEquals("the Ranged column ends with its actual Venator ring",Integer.valueOf(28310),combat.get(73));
			assertEquals("the Magic column ends with its actual Magus ring",Integer.valueOf(28313),combat.get(74));
			Set<Integer> extraRings = new LinkedHashSet<>(allRings);
			extraRings.removeAll(Arrays.asList(28307,28310,28313));
			assertEquals("eight extra rings share the full row directly below the three best rings",
				extraRings,new LinkedHashSet<>(combat.subList(80,88)));
			assertTrue("all11 actual rings remain available",combat.containsAll(allRings));
			assertRetainedArmour(combat);
			if (expected == null) { expected=combat; expectedDestinations=destinations(preview); }
			else { assertEquals("shuffles and withdrawals preserve185-item targets",expected,combat);
				assertEquals(expectedDestinations,destinations(preview)); }
			assertTrue("bounded complete-ring mixed preview generation",elapsed < 10000000000L);
		}
	}

	private void addRing(int id,String zone,GearStats stats)
	{
		assertFalse("ring supplements are distinct actual identities",facts.containsKey(id));
		facts.put(id,new CombatQualityReport.Fact(id,zone,"ring-"+id,9,id,"ring",stats));
		quantities.put(id,1); sourceIds.add(id);
	}

	private BankOrganizationPreview build(BankSnapshot bank)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,
			id -> Optional.ofNullable(facts.get(id)).map(fact -> fact.stats),ItemValueSource.NONE,
			CategoryOverrideSource.NONE,plan,BankLayoutOptions.defaultFor(preset));
	}

	private BankSnapshot bank(List<Integer> ids,Set<Integer> withdrawn)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : ids)
		{
			int quantity = withdrawn.contains(id) ? 0 : quantities.get(id);
			entries.add(new BankItemSnapshot(id,quantity,entries.size(),quantity == 0));
		}
		return new BankSnapshot(entries);
	}

	private List<Integer> combatIds(BankOrganizationPreview preview)
	{
		return CombatQualityReport.ids(preview.getCategories().get(plan.destinationOf("gear")).getItems());
	}

	private static Set<Integer> primaryIds()
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int[] column : PRIMARY_COLUMNS) for (int id : column) result.add(id);
		return result;
	}

	private static void assertColumns(List<Integer> combat)
	{
		for (int style = 0; style < PRIMARY_COLUMNS.length; style++)
			for (int row = 0; row < PRIMARY_COLUMNS[style].length; row++)
				assertEquals("independently reviewed full column, style " + style + ", row " + row,
					Integer.valueOf(PRIMARY_COLUMNS[style][row]),combat.get(row*8+style));
		for (int i = 0; i < SPECIAL_WEAPONS.length; i++)
			assertEquals("specials share two right-hand rows near the other Melee weapons",
				Integer.valueOf(SPECIAL_WEAPONS[i]),combat.get(SPECIAL_CELLS[i]));
		for (int i = 0; i < SECONDARY_BOWS.length; i++)
			assertEquals("the large bow group also uses the right-hand region",
				Integer.valueOf(SECONDARY_BOWS[i]),combat.get(BOW_CELLS[i]));
	}

	private static void assertRings(List<Integer> combat)
	{
		assertTrue("Lightbearer and the old Berserker remain alongside Ultor",combat.containsAll(RINGS));
		assertEquals("eight real rings form one row beside the primary Ultor",RINGS,
			new LinkedHashSet<>(combat.subList(72,80)));
	}

	private static void assertRetainedArmour(List<Integer> combat)
	{
		for (int[] family : SECONDARY_ARMOUR)
		{
			// Alch/other routing is not overridden to force an artificial all-Combat result.
			List<Integer> retained = Arrays.stream(family).filter(combat::contains).boxed().collect(Collectors.toList());
			if (retained.size() < 2) continue;
			int first = combat.indexOf(retained.get(0));
			for (int member = 0; member < retained.size(); member++)
				assertEquals("this sufficiently populated ownership keeps every retained armour family vertical: " + retained,
					first+8*member,combat.indexOf(retained.get(member)));
		}
	}

	private static Map<Integer,String> destinations(BankOrganizationPreview preview)
	{
		Map<Integer,String> result = new TreeMap<>();
		for (BankPreviewItem item : preview.getPlannedItems()) result.put(item.getItemId(),item.getLayoutTagKey());
		return result;
	}

	private void writeDestinations(BankOrganizationPreview preview,long elapsed) throws Exception
	{
		List<String> rows = new ArrayList<>();
		rows.add("# Frozen"+sourceIds.size()+"-item midgame/endgame union. Public production catalog; no category overrides.");
		rows.add("# Equipment vectors are frozen October2026 fixtures; this is a representative ownership mix, not every gear item.");
		rows.add("# ItemValueSource.NONE isolates layout from changing market prices; live monetary Alch thresholds are not covered.");
		rows.add("# Preview generation milliseconds: " + String.format(Locale.ROOT,"%.2f",elapsed/1e6));
		rows.add("# Actual destinations: " + preview.getPlannedItems().stream().collect(Collectors.groupingBy(
			BankPreviewItem::getLayoutTagKey,TreeMap::new,Collectors.counting())));
		rows.add("itemId\tname\tdestination\tcombatCell\tquantity\tplaceholder");
		List<BankPreviewItem> items = new ArrayList<>(preview.getPlannedItems());
		items.sort(Comparator.comparingInt(BankPreviewItem::getItemId));
		List<Integer> combat = combatIds(preview);
		for (BankPreviewItem item : items)
			rows.add(item.getItemId() + "\t" + item.getDisplayName() + "\t" + item.getLayoutTagKey()
				+ "\t" + combat.indexOf(item.getItemId()) + "\t" + item.getQuantity() + "\t" + item.isPlaceholder());
		Files.createDirectories(REPORTS);
		Files.write(REPORTS.resolve(preset.getType() + "-mixed-midgame-endgame-"+sourceIds.size()+"-destinations.tsv"),rows,StandardCharsets.UTF_8);
	}
}

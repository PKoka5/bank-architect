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

/** Actual reported bank with frozen public equipment stats and independently specified expectations. */
@RunWith(Parameterized.class)
public class ReportedMidgameCombatTest
{
	private static final List<Integer> PRIMARY = Arrays.asList(10828, 3749, 3755, 11832, 2503, 4091,
		11834, 2497, 4093, 26219, 26770, 25975, 25865, 12658, 6570, 6585, 12954, 7462,
		29286, 10499, 31088, 21795, 6889, 6920);
	private static final int[][] CORES = {{10828,11832,11834},{3749,2503,2497},{3755,4091,4093}};
	private static final int[][] ROLE_GROUPS = {
		{4151,4587}, {26219,13265}, {28997,29889}, {1215,10887,11061}, {24699,4158,7668},
		{23985,12788,859,855,847}, {805,865}, {10033,10034}, {1381,1383,1385,1393,3053}
	};
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	private final Map<Integer,CatalogItem> catalog = new LinkedHashMap<>();
	private final Map<Integer,GearStats> stats = new LinkedHashMap<>();
	private final List<Integer> source = new ArrayList<>();

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN},new Object[] {BankPresets.MAIN});
	}
	public ReportedMidgameCombatTest(BankPreset preset) throws Exception
	{
		this.preset = preset; plan = BankLayoutPlan.defaultFor(preset);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
			ReportedMidgameCombatTest.class.getResourceAsStream("reported-midgame-combat-85.tsv"),StandardCharsets.UTF_8)))
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] row = line.split("\t",-1); int id = Integer.parseInt(row[0]);
				assertFalse("duplicate fixture ID",catalog.containsKey(id)); source.add(id);
				catalog.put(id,new CatalogItem(id,row[1],ItemCategory.valueOf(row[2]),row[3],Collections.emptySet(),null));
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				assertEquals(16,v.length);
				stats.put(id,new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
					v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
			}
		}
	}

	@Test
	public void generalGearAndSupportHaveDistinctOwnershipAndArmourStaysVertical() throws Exception
	{
		BankOrganizationPreview preview = build(source,Collections.emptySet(),BlueprintItemOrders.EMPTY);
		List<Integer> actual = ids(preview);
		Map<Integer,CombatQualityReport.Fact> facts = new LinkedHashMap<>();
		Set<Integer> ranged = new HashSet<>(Arrays.asList(26674,2503,2497,25865,10499,3749,23985,
			29280,29283,29286,12788,805,8880,865,10034,859,855,10033,4827,847));
		Set<Integer> magic = new HashSet<>(Arrays.asList(3755,4091,4093,31115,21795,8923,6889,6920,
			20714,12899,12658,1381,1385,1383,9084,4675,1393,3053));
		Set<Integer> prayer = new HashSet<>(Arrays.asList(26763,9674,9676,544,542,12601));
		for (int id : source) facts.put(id,new CombatQualityReport.Fact(id,ranged.contains(id)?"R":magic.contains(id)?"G":prayer.contains(id)?"P":"M",
			"item-"+id,0,0,"loose",stats.get(id)));
		new CombatQualityReport(preset.getType()+"-reported-midgame",bank(source,Collections.emptySet()),preview,
			plan,new LinkedHashSet<>(PRIMARY),true,0,false,facts).write(Paths.get("build/reports/combat-quality"));
		java.nio.file.Files.writeString(Paths.get("build/reports/combat-quality",preset.getType()+"-reported-midgame.txt"),
			BankBlueprintTextExporter.export(preview),StandardCharsets.UTF_8);
		assertEquals(preset.getType()==BankPresetType.MAIN?84:85,actual.size());
		assertEquals("the Main preset retains its separate transport destination",preset.getType()!=BankPresetType.MAIN,actual.contains(9084));
		for (int column=0;column<CORES.length;column++)
			for (int row=0;row<3;row++) assertEquals(Integer.valueOf(CORES[column][row]),actual.get(row*8+column));
		assertEquals("Melee cape",Integer.valueOf(6570),actual.get(24));
		assertEquals("Ranged cape",Integer.valueOf(10499),actual.get(25));
		assertEquals("Magic cape",Integer.valueOf(21795),actual.get(26));
		assertEquals("Melee boots",Integer.valueOf(31088),actual.get(32));
		assertEquals("primary melee strength ring",Integer.valueOf(26770),actual.get(72));
		assertFalse("conditional Slayer item is not general primary gear",PRIMARY.contains(8923));
	}

	@Test
	public void reviewedWeaponRolesFormWholeCanonicalGroups()
	{
		List<Integer> actual = ids(build(source,Collections.emptySet(),BlueprintItemOrders.EMPTY));
		assertEquals("the reviewed general Fang preference joins the complete Melee column",Integer.valueOf(26219),actual.get(48));
		for (int[] group : ROLE_GROUPS)
			assertGroup(actual,Arrays.stream(group).filter(id -> id != 26219).toArray());
		int slash = actual.indexOf(4151), stab = actual.indexOf(13265), crush = actual.indexOf(28997);
		assertTrue("slash then stab then crush",slash<stab && stab<crush);
		assertTrue("quest sword follows combat/task weapons",actual.indexOf(10858)>actual.indexOf(6746));
		if (preset.getType()==BankPresetType.IRONMAN)
			assertTrue("transport staff follows casting and elemental staves",actual.indexOf(9084)>actual.indexOf(4675)
				&& actual.indexOf(9084)>actual.indexOf(3053));
	}

	@Test
	public void shuffleAndWithdrawnPlaceholdersCannotChangeTheLayout()
	{
		List<Integer> expected = ids(build(source,Collections.emptySet(),BlueprintItemOrders.EMPTY));
		Random random = new Random(20261012);
		for (int trial=0;trial<3;trial++)
		{
			List<Integer> shuffled = new ArrayList<>(source); Collections.shuffle(shuffled,random);
			Set<Integer> placeholders = trial==2 ? new HashSet<>(PRIMARY) : Collections.emptySet();
			BankOrganizationPreview preview = build(shuffled,placeholders,BlueprintItemOrders.EMPTY);
			assertEquals(expected,ids(preview));
			assertEquals(85,preview.getPlannedItems().size());
			for (BankPreviewItem item : preview.getPlannedItems())
			{
				assertFalse(item.isBlank()); assertTrue(source.contains(item.getItemId()));
				assertEquals(placeholders.contains(item.getItemId()),item.isPlaceholder());
				assertEquals(item.isPlaceholder()?0:1,item.getQuantity());
			}
		}
	}

	@Test
	public void savedBlueprintOrderRemainsAuthoritative()
	{
		List<Integer> order = new ArrayList<>(source);
		if (preset.getType()==BankPresetType.MAIN) order.remove(Integer.valueOf(9084));
		Collections.reverse(order);
		assertEquals(order,ids(build(source,Collections.emptySet(),BlueprintItemOrders.EMPTY.withTab(plan.destinationOf("gear"),order))));
	}

	private BankOrganizationPreview build(List<Integer> ids,Set<Integer> placeholders,BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(bank(ids,placeholders),id->Optional.ofNullable(catalog.get(id)),preset,
			id->Optional.ofNullable(stats.get(id)),ItemValueSource.NONE,CategoryOverrideSource.NONE,plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
	}
	private static BankSnapshot bank(List<Integer> ids,Set<Integer> placeholders)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id,placeholders.contains(id)?0:1,items.size(),placeholders.contains(id)));
		return new BankSnapshot(items);
	}
	private List<Integer> ids(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("gear")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static void assertGroup(List<Integer> actual,int[] group)
	{
		int first=actual.indexOf(group[0]); assertTrue(first>=0);
		boolean dense=true,vertical=true;
		for (int i=0;i<group.length;i++)
		{
			dense &= actual.indexOf(group[i])==first+i;
			vertical &= actual.indexOf(group[i])==first+i*8;
		}
		assertTrue("whole canonical weapon group "+Arrays.toString(group)+" at "+
			Arrays.stream(group).map(actual::indexOf).boxed().collect(Collectors.toList()),dense||vertical);
	}
}

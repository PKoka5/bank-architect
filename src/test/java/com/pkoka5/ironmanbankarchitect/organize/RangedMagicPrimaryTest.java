package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

/** Reviewed roles and independent expected choices; no production score is used as an oracle. */
public class RangedMagicPrimaryTest
{
	private static final Map<Integer,GearStats> FROZEN=frozen();
	@Test public void reviewedTablesCoverTenSlotsAndOnlyKnownExactIdentities()
	{
		for (String style:Arrays.asList("ranged","magic"))
		{
			Map<Integer,int[]> facts=PrimaryGearSelector.load(getClass().getResourceAsStream(style+"-primary-candidates.tsv"));
			assertEquals(style.equals("ranged")?427:442,facts.size());
			Set<Integer> slots=new HashSet<>();
			facts.forEach((id,fact) -> {
				assertTrue("registered "+style+" candidate "+id,CompositeItemCatalog.DEFAULT.findById(id).isPresent());
				slots.add(fact[0]);
			});
			assertEquals(new HashSet<>(Arrays.asList(0,1,2,3,4,5,6,7,8,12)),slots);
			for(int id:new int[]{27187,33190,33166,25495,24037,999999}) assertFalse("temporary/unreviewed identity",facts.containsKey(id));
		}
	}
	@Test public void rangedHeadUsesRangedOffenceInsteadOfMeleeDefenceOrSlayerTaskEffects()
	{
		assertEquals(Collections.singletonList(3749),column(select(FROZEN,10828,4724,3749,26674),1));
		assertEquals(Collections.singletonList(3749),column(select(Collections.emptyMap(),10828,4724,3749,26674),1));
		assertEquals(Collections.singletonList(3755),column(select(FROZEN,10828,4724,3755,26674),2));
	}
	@Test public void earlyAndMidgameCoreFallbacksDoNotRequireAnEndgameSample()
	{
		assertEquals(Arrays.asList(1169,1133,1097),column(select(Collections.emptyMap(),1167,1169,1129,1133,1095,1097),1));
		assertEquals(Arrays.asList(3755,4091,4093),column(select(Collections.emptyMap(),579,3755,577,4091,1033,4093),2));
		assertEquals(Arrays.asList(27235,27238,27241),column(select(FROZEN,11826,11828,11830,27235,27238,27241),1));
		assertEquals(Arrays.asList(21018,21021,21024),column(select(FROZEN,26241,26243,26245,21018,21021,21024),2));
	}
	@Test public void damageWinsOverDefenceTierAndMissingStatsUsesOneWholeSlotGroupOrder()
	{
		Map<Integer,GearStats> stats=new HashMap<>();
		stats.put(27235,new GearStats(GearSlot.HEAD,0,0,0,0,1,0,2,0,0));
		stats.put(11826,new GearStats(GearSlot.HEAD,0,0,0,0,99,0,0,99,999));
		assertEquals(Collections.singletonList(27235),column(select(stats,27235,11826),1));
		stats.remove(27235);
		assertEquals(Collections.singletonList(27235),column(select(stats,27235,11826),1));
		stats.clear();
		stats.put(21018,new GearStats(GearSlot.HEAD,0,0,0,1,0,0,0,0,0,0,0,0,0,30,0));
		stats.put(26241,new GearStats(GearSlot.HEAD,0,0,0,99,0,0,0,99,999,999,999,999,999,10,0));
		assertEquals(Collections.singletonList(21018),column(select(stats,21018,26241),2));
	}
	@Test public void oneHandedWeaponsTakeTheirOffhandAndTwoHandedWeaponsReleaseIt()
	{
		for(Map<Integer,GearStats> stats:Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
		{
			assertEquals(Arrays.asList(26374,21000),column(select(stats,26374,21000),1));
			assertEquals(Collections.singletonList(20997),column(select(stats,20997,21000),1));
			assertEquals(Collections.singletonList(34681),column(select(stats,34681,21000),1));
			assertEquals(Arrays.asList(31113,27251),column(select(stats,31113,27251),2));
			assertEquals(Collections.singletonList(27275),column(select(stats,27275,27251),2));
			assertEquals(Collections.singletonList(34725),column(select(stats,34725,27251),2));
		}
	}
	@Test public void unchargedPoweredWeaponsCannotBeatTheirUsableAlternatives()
	{
		for(Map<Integer,GearStats> stats:Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
		{
			assertEquals(Collections.singletonList(22323),column(select(stats,27277,31115,22323),2));
			assertEquals(Collections.singletonList(12658),column(select(stats,28583,11908,12900,12658),2));
			assertEquals(Collections.singletonList(9185),column(select(stats,25862,12924,9185),1));
			GearItemSorter.GearLayout inactive=select(stats,27277,31115,25862,12924,34727);
			assertTrue(inactive.getSetupRows().isEmpty()); assertEquals(5,inactive.getTail().size());
		}
	}
	@Test public void bowfaAndCrystalBowUseTheirOwnedActiveCrystalPieces()
	{
		for(int bow:new int[]{25865,25867,23983})
		{
			GearItemSorter.GearLayout selection=select(FROZEN,bow,23971,23975,23979,27235,27238,27241);
			assertEquals(Arrays.asList(23971,23975,23979,bow),column(selection,1));
			assertTrue(selection.getSetupRows().get(0).getPlacementReason().contains("per-piece bow synergy"));
			assertTrue(selection.getSetupRows().get(0).getPlacementReason().startsWith("Source placement."));
		}
		assertEquals(Arrays.asList(27235,27238,27241,20997),column(select(FROZEN,20997,25865,23971,23975,23979,27235,27238,27241),1));
		assertEquals(Arrays.asList(27235,27238,27241),column(select(FROZEN,25862,23973,23977,23981,27235,27238,27241),1));
	}
	@Test public void voidAndSlayerHaveNoInventedConditionalDamageBonus()
	{
		GearItemSorter.GearLayout selection=select(FROZEN,11664,8839,8840,8842,11865,3749,2503,2497);
		assertTrue(column(selection,1).containsAll(Arrays.asList(3749,2503,2497)));
		assertFalse(column(selection,1).contains(11664));
		assertTrue(selection.getTail().stream().anyMatch(item -> item.getItemId()==11664));
	}
	@Test public void newlyReviewedFangsAndSharedAccessoriesHaveOnePhysicalOwner()
	{
		Map<Integer,GearStats> stats=new HashMap<>(FROZEN);
		stats.put(34401,new GearStats(GearSlot.NECK,0,0,0,0,12,0,1,0,0));
		GearItemSorter.GearLayout selection=select(stats,6585,34401,19547,12002,7462,22981,19544);
		assertEquals(Arrays.asList(6585,22981),column(selection,0));
		assertTrue(column(selection,1).containsAll(Arrays.asList(19547,7462)));
		assertTrue(column(selection,2).containsAll(Arrays.asList(12002,19544)));
		assertEquals(Collections.singletonList(34401),column(select(Collections.emptyMap(),34401),1));
		assertTrue(CompositeItemCatalog.DEFAULT.findById(34401).isPresent());
		assertEquals("combat-ranged",CombatStyleTags.forItem(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(34401).get(),1),GearStatsSource.NONE));
		assertEquals("combat-magic",CombatStyleTags.forItem(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(3755).get(),1),GearStatsSource.NONE));
		assertEquals("gear",CombatStyleTags.forItem(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(6585).get(),1),GearStatsSource.NONE));
	}
	@Test public void shufflesAndWithdrawnPlaceholdersKeepEveryChosenRoleAndPhysicalItem() throws Exception
	{
		List<Integer> ids=new ArrayList<>(CombatQualityReport.FACTS.keySet());
		GearItemSorter.GearLayout baseline=select(FROZEN,ids.stream().mapToInt(Integer::intValue).toArray());
		for(int seed=0;seed<3;seed++)
		{
			Collections.shuffle(ids,new Random(20261025L+seed));
			List<BankPreviewItem> input=new ArrayList<>();
			for(int id:ids) input.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(id).get(),seed==2?0:3,seed==2));
			GearItemSorter.GearLayout result=GearItemSorter.plan(input,source(FROZEN));
			for(int style=0;style<3;style++) assertEquals(column(baseline,style),column(result,style));
			List<BankPreviewItem> actual=new ArrayList<>(result.getSetupRows()); actual.addAll(result.getTail()); assertEquals(input.size(),actual.size());
			for(BankPreviewItem item:actual) { assertEquals(seed==2?0:3,item.getQuantity()); assertEquals(seed==2,item.isPlaceholder()); }
		}
	}
	@Test public void reviewedSimulationChangesStillFinishWithTheMinimumSwapCounts() throws Exception
	{
		java.nio.file.Path output=java.nio.file.Path.of("build/reports/ranged-magic");
		java.nio.file.Files.createDirectories(output);
		List<Integer> universe=com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.loadItemUniverse();
		for(int[] sample:new int[][]{{20260718,34,24},{20260737,71,43}})
		{
			com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult result=
				com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.simulate(sample[0],
					com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Scenario.RANDOM_TABS,sample[1],universe);
			assertEquals(com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Outcome.COMPLETED,result.getOutcome());
			assertTrue(result.isFinalOrderVerified()); assertEquals(sample[2],result.getSwapMoves());
			assertEquals(result.getMinimumSwapsAtSortStart(),result.getSwapMoves());
			List<BankItemSnapshot> entries=new ArrayList<>();
			for(int id:result.getSampledItemIds())entries.add(new BankItemSnapshot(id,1,entries.size()));
			BankOrganizationPreview preview=BankOrganizationPreviewBuilder.build(new BankSnapshot(entries),CompositeItemCatalog.DEFAULT,BankPresets.IRONMAN);
			String gear=preview.getPlannedItems().stream().filter(item -> item.getItemCategory()==ItemCategory.GEAR)
				.map(item -> item.getItemId()+"\t"+item.getDisplayName()+"\t"+item.getPlacementReason()).collect(Collectors.joining("\n"));
			java.nio.file.Files.writeString(output.resolve(sample[0]+"-gear.txt"),gear);
			java.nio.file.Files.writeString(output.resolve(sample[0]+"-blueprint.txt"),BankBlueprintTextExporter.export(preview));
		}
	}
	private static GearItemSorter.GearLayout select(Map<Integer,GearStats> stats,int... ids)
	{
		List<BankPreviewItem> items=Arrays.stream(ids).mapToObj(id -> new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(id).get(),1)
			.withPlacementReason("Source placement.")).collect(Collectors.toList());
		return GearItemSorter.plan(items,source(stats));
	}
	private static GearStatsSource source(Map<Integer,GearStats> stats) { return id -> Optional.ofNullable(stats.get(id)); }
	private static List<Integer> column(GearItemSorter.GearLayout selection,int style)
	{
		Map<Integer,Integer> targets=GearItemSorter.setupTargets(selection,1000);
		return selection.getSetupRows().stream().map(BankPreviewItem::getItemId).filter(id -> targets.get(id)%8==style).collect(Collectors.toList());
	}
	private static Map<Integer,GearStats> frozen()
	{
		Map<Integer,GearStats> stats=new HashMap<>();
		CombatQualityReport.FACTS.forEach((id,fact) -> { if(fact.stats!=null)stats.put(id,fact.stats); });
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(RangedMagicPrimaryTest.class.getResourceAsStream("reported-midgame-combat-85.tsv"),StandardCharsets.UTF_8)))
		{
			for(String line=reader.readLine();line!=null;line=reader.readLine()) if(!line.isEmpty()&&!line.startsWith("#"))
			{
				String[] row=line.split("\t",-1); int[] v=Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				stats.putIfAbsent(Integer.parseInt(row[0]),new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
			}
		}
		catch(IOException failure){throw new AssertionError(failure);}
		return stats;
	}
}

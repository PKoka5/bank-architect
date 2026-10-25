package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class VoidSetCoherenceTest
{
	private static final int[] NORMAL={11665,11664,11663,8839,8840,8842};
	private static final int[][] KITS={NORMAL,{11664,13072,13073,8842},{26475,26469,26471,26467},
		{24184,24178,24180,24182},{27007,26473,27003,24180,8842},{8839,8842}};
	private static final Map<Integer,GearStats> STATS=stats();
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	@Parameterized.Parameters(name="{0}") public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[]{BankPresets.IRONMAN},new Object[]{BankPresets.MAIN});
	}
	public VoidSetCoherenceTest(BankPreset preset){this.preset=preset;plan=BankLayoutPlan.defaultFor(preset);}

	@Test public void reportedNinetyFourItemCombatBankKeepsAllWithdrawnVoidPiecesIncludingTheGloves() throws Exception
	{
		BankSnapshot bank=reported(); assertEquals(94,bank.getPhysicalItems().size());
		BankOrganizationPreview preview=build(bank,plan,options(GearLayout.GRID_STYLES),CategoryOverrideSource.NONE);
		assertKit(combat(preview,plan),NORMAL); assertConserved(bank,preview);
		List<Integer> positions=combat(preview,plan).stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
		assertEquals("the reported bank has room for the whole six-piece Void run on one row",
			positions.indexOf(11665)/8,positions.indexOf(8842)/8);
		for(int id:NORMAL) assertTrue(item(preview,id).isPlaceholder());
		GearItemSorter.GearLayout selection=GearItemSorter.plan(bank.getItems().stream().map(entry ->
			new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(entry.getItemId()).get(),entry.getQuantity(),entry.isPlaceholder()))
			.collect(Collectors.toList()),source());
		assertFalse(selection.getSetupRows().stream().anyMatch(item -> GearSetSemanticRuleSet.isVoid(item.getItemId())));
		Map<Integer,Integer> targets=GearItemSorter.setupTargets(selection,1000);
		assertEquals(Arrays.asList(10828,11832,11834,6570,31088,6585,26219,12954,7462,26770),
			selection.getSetupRows().stream().filter(item -> targets.get(item.getItemId())%8==0)
				.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		Path output=Path.of("build/reports/void-coherence");
		new CombatQualityReport(preset.getType()+"-reported-void-94",bank,preview,plan,Collections.emptySet(),false,0,false).write(output);
		Files.writeString(output.resolve(preset.getType()+"-reported-void-94.txt"),BankBlueprintTextExporter.export(preview));
	}

	@Test public void normalEliteOrnamentLockedMixedAndPartialKitsStayWholeAcrossLayoutsAndPlaceholders() throws Exception
	{
		for(int[] kit:KITS) for(GearLayout layout:GearLayout.values()) for(boolean placeholders:new boolean[]{false,true})
		{
			BankSnapshot bank=withKit(kit,placeholders);
			BankOrganizationPreview preview=build(bank,plan,options(layout),CategoryOverrideSource.NONE);
			assertKit(combat(preview,plan),kit); assertConserved(bank,preview);
		}
	}

	@Test public void optionalStyleDestinationsLeaveTheAutomaticVoidKitTogetherInCombatGear() throws Exception
	{
		BankLayoutPlan split=plan.withTagAt("combat-melee",7).withTagAt("combat-ranged",8).withTagAt("combat-magic",9);
		for(int[] kit:KITS)
		{
			BankSnapshot bank=withKit(kit,true);
			BankOrganizationPreview preview=build(bank,split,options(GearLayout.GRID_STYLES),CategoryOverrideSource.NONE);
			assertKit(combat(preview,split),kit);
			for(int id:kit) assertEquals("gear",item(preview,id).getLayoutTagKey());
			assertConserved(bank,preview);
		}
	}

	@Test public void aVoidOnlyBankAndInputShufflesNeverNeedToSplitTheSetAcrossPrimaryStyles() throws Exception
	{
		List<BankItemSnapshot> entries=new ArrayList<>();
		for(int id:NORMAL) entries.add(new BankItemSnapshot(id,0,entries.size(),true));
		for(int trial=0;trial<3;trial++)
		{
			Collections.shuffle(entries,new Random(20261025L+trial));
			BankSnapshot bank=new BankSnapshot(entries);
			BankOrganizationPreview preview=build(bank,plan,options(GearLayout.GRID_STYLES),CategoryOverrideSource.NONE);
			assertKit(combat(preview,plan),NORMAL); assertConserved(bank,preview);
		}
		assertFalse(GearSetSemanticRuleSet.keepsVoidTogether(java.util.stream.IntStream.of(8842,8842)));
		assertTrue(GearSetSemanticRuleSet.keepsVoidTogether(java.util.stream.IntStream.of(8839,8842)));
		List<Integer> reviewed=new OrderedItemFamilies(getClass().getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"),0).ids("gear.void");
		assertEquals(32,reviewed.size());
		for(int id:reviewed) assertTrue("registered usable Void identity "+id,CompositeItemCatalog.DEFAULT.findById(id).isPresent());
	}

	@Test public void manualItemChoicesSavedPhysicalOrderAndAcceptedTargetsKeepPriority() throws Exception
	{
		BankSnapshot bank=withKit(NORMAL,false);
		CategoryOverrideSource choice=id -> id==8842?Optional.of("frequently-used"):Optional.empty();
		BankOrganizationPreview personal=build(bank,plan,options(GearLayout.GRID_STYLES),choice);
		assertTrue(personal.getCategories().get(plan.destinationOf("frequently-used")).getItems().stream().anyMatch(item -> item.getItemId()==8842));
		BankOrganizationPreview automatic=build(bank,plan,options(GearLayout.GRID_STYLES),CategoryOverrideSource.NONE);
		List<Integer> order=combat(automatic,plan).stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
		order.remove(Integer.valueOf(8842)); order.add(0,8842);
		BankOrganizationPreview saved=build(bank,plan,options(GearLayout.GRID_STYLES).withItemOrders(
			BlueprintItemOrders.EMPTY.withTab(plan.destinationOf("gear"),order)),CategoryOverrideSource.NONE);
		assertEquals(order,combat(saved,plan).stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		String accepted=AcceptedBlueprint.capture(saved,"same personal choices");
		BankOrganizationPreview kept=AcceptedBlueprint.apply(accepted,"same personal choices",automatic,plan);
		assertEquals(order,combat(kept,plan).stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		assertConserved(bank,personal); assertConserved(bank,saved); assertConserved(bank,kept);
	}

	@Test public void repeatedPhysicalGlovesRetainTheirStacksAndPlaceholder() throws Exception
	{
		List<BankItemSnapshot> entries=new ArrayList<>(withKit(NORMAL,false).getPhysicalItems());
		entries.add(new BankItemSnapshot(8842,4,entries.size()));
		entries.add(new BankItemSnapshot(8842,0,entries.size(),true));
		BankSnapshot bank=new BankSnapshot(entries);
		assertConserved(bank,build(bank,plan,options(GearLayout.GRID_STYLES),CategoryOverrideSource.NONE));
	}

	private BankSnapshot withKit(int[] kit,boolean placeholders) throws Exception
	{
		List<BankItemSnapshot> entries=reported().getItems().stream().filter(item -> !GearSetSemanticRuleSet.isVoid(item.getItemId()))
			.collect(Collectors.toCollection(ArrayList::new));
		for(int id:kit) entries.add(new BankItemSnapshot(id,placeholders?0:1,entries.size(),placeholders));
		return new BankSnapshot(entries);
	}
	private BankLayoutOptions options(GearLayout layout)
	{
		BankLayoutOptions base=BankLayoutOptions.defaultFor(preset);
		return new BankLayoutOptions(true,base.fillHerbloreRows(),base.alchPile(),Collections.emptyMap(),layout,
			base.potionDoses(),base.runeOrder(),base.teleportOrder(),base.gatherFrequentlyUsed());
	}
	private BankOrganizationPreview build(BankSnapshot bank,BankLayoutPlan layout,BankLayoutOptions options,CategoryOverrideSource choice)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,source(),ItemValueSource.NONE,choice,layout,options);
	}
	private static List<BankPreviewItem> combat(BankOrganizationPreview preview,BankLayoutPlan plan){return preview.getCategories().get(plan.destinationOf("gear")).getItems();}
	private static BankPreviewItem item(BankOrganizationPreview preview,int id){return preview.getPlannedItems().stream().filter(item -> item.getItemId()==id).findFirst().get();}
	private static GearStatsSource source(){return id -> Optional.ofNullable(STATS.get(id));}
	private static BankSnapshot reported() throws Exception
	{
		List<BankItemSnapshot> entries=new ArrayList<>();
		try(BufferedReader reader=reader("reported-void-combat-94.tsv"))
		{
			for(String line=reader.readLine();line!=null;line=reader.readLine()) if(!line.isEmpty()&&!line.startsWith("#"))
			{
				String[] fields=line.split("\t");int amount=Integer.parseInt(fields[1]);
				entries.add(new BankItemSnapshot(Integer.parseInt(fields[0]),amount,entries.size(),amount==0));
			}
		}
		return new BankSnapshot(entries);
	}
	private static Map<Integer,GearStats> stats()
	{
		Map<Integer,GearStats> values=new HashMap<>();
		CombatQualityReport.FACTS.forEach((id,fact) -> {if(fact.stats!=null)values.put(id,fact.stats);});
		try(BufferedReader reader=reader("reported-midgame-combat-85.tsv"))
		{
			for(String line=reader.readLine();line!=null;line=reader.readLine()) if(!line.isEmpty()&&!line.startsWith("#"))
			{
				String[] row=line.split("\t",-1);int[] v=Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				values.putIfAbsent(Integer.parseInt(row[0]),new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
			}
		}
		catch(IOException failure){throw new AssertionError(failure);}
		return values;
	}
	private static BufferedReader reader(String name){return new BufferedReader(new InputStreamReader(Objects.requireNonNull(VoidSetCoherenceTest.class.getResourceAsStream(name)),StandardCharsets.UTF_8));}
	private static void assertKit(List<BankPreviewItem> items,int[] kit)
	{
		List<Integer> all=items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
		List<Integer> positions=Arrays.stream(kit).map(all::indexOf).boxed().collect(Collectors.toList());
		assertTrue("all owned Void pieces stay in Combat",positions.stream().allMatch(at -> at>=0));
		List<Integer> physical=new ArrayList<>(positions);Collections.sort(physical);
		int lowRow=physical.stream().mapToInt(at -> at/8).min().getAsInt(), highRow=physical.stream().mapToInt(at -> at/8).max().getAsInt();
		int lowCol=physical.stream().mapToInt(at -> at%8).min().getAsInt(), highCol=physical.stream().mapToInt(at -> at%8).max().getAsInt();
		boolean compact=physical.get(physical.size()-1)-physical.get(0)+1==kit.length;
		boolean rectangle=(highRow-lowRow+1)*(highCol-lowCol+1)==kit.length;
		assertTrue("Void remains one compact run or rectangle, gloves included: "+positions,compact||rectangle);
		List<Integer> columnMajor=new ArrayList<>(physical);columnMajor.sort(Comparator.comparingInt((Integer at) -> at%8).thenComparingInt(at -> at/8));
		assertTrue("canonical Void equipment order",positions.equals(physical)||rectangle&&positions.equals(columnMajor));
	}
	private static void assertConserved(BankSnapshot bank,BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder()).sorted().collect(Collectors.toList()),
			preview.getPlannedItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder()).sorted().collect(Collectors.toList()));
		assertFalse(preview.getPlannedItems().stream().anyMatch(BankPreviewItem::isBlank));
	}
}

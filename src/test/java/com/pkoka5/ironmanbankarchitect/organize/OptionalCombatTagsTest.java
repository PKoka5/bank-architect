package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OptionalCombatTagsTest
{
	private static final String[] STYLES = {"combat-melee","combat-ranged","combat-magic"};
	private final BankPreset preset;
	private final BankLayoutPlan original;
	private final GearStatsSource stats = id -> Optional.ofNullable(CombatQualityReport.FACTS.get(id)).map(fact -> fact.stats);
	@Parameterized.Parameters(name="{0}") public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[]{BankPresets.IRONMAN},new Object[]{BankPresets.MAIN});
	}
	public OptionalCombatTagsTest(BankPreset preset) { this.preset=preset; original=BankLayoutPlan.defaultFor(preset); }

	@Test public void oldPlansRemainUnsplitIncludingLegacyCategoryExpansion()
	{
		for (BankLayoutPlan plan : Arrays.asList(original,BankLayoutPlan.parse(preset,original.serialize()),
			BankLayoutPlan.parse(preset,"combat-gear|||||||||")))
		{
			for (String style : STYLES) assertEquals(-1,plan.destinationOf(style));
			assertEquals(30,plan.getDestinations().stream().mapToInt(List::size).sum());
		}
		assertEquals(Arrays.asList("gear","ammunition"),BankTags.forCategory("combat-gear").stream()
			.map(BankTag::getKey).collect(Collectors.toList()));
		assertEquals("gear",BankTags.tagFor("combat-gear","weapon").getKey());
	}

	@Test public void optionalTagsSurviveProfileRoundTripsWholeTabMovesAndRemoval()
	{
		BankLayoutPlan split=split().withCurrentOrder(7,true);
		String name="Combat styles";
		BankLayoutProfiles profiles=BankLayoutProfiles.parse("",null,BankLayoutProfiles.defaultName(preset))
			.withProfile(name,split.serialize());
		BankLayoutPlan restored=BankLayoutPlan.parse(preset,BankLayoutProfiles.parse(profiles.serialize(),name,
			BankLayoutProfiles.defaultName(preset)).activePlan());
		assertEquals(split.serialize(),restored.serialize());
		BankLayoutShareCode shared=BankLayoutShareCode.decode(BankLayoutShareCode.encode(name,split,preset)).get();
		assertEquals(preset.getType(),shared.getPresetType());
		assertEquals(split.serialize(),BankLayoutPlan.parse(preset,shared.getPlan()).serialize());
		BankLayoutPlan moved=restored.withDestinationMoved(7,9,preset);
		assertEquals(9,moved.destinationOf(STYLES[0])); assertTrue(moved.keepsCurrentOrder(9));
		for (String style:STYLES) restored=restored.withoutOptionalTag(style);
		assertEquals(original.getDestinations(),restored.getDestinations());
		assertTrue(restored.keepsCurrentOrder(7));
		assertSame(restored,restored.withoutOptionalTag(STYLES[0]));
		try { restored.withoutOptionalTag("gear"); fail("Cannot remove the parent tag"); }
		catch (IllegalArgumentException expected) { }
	}

	@Test public void sharedGearStaysSingleAndStyleHelmetsDoNotFollowMeleeDefence()
	{
		int[][] ids={{11665,24185,26477,27007},{11664,24184,26475,27006},{11663,24183,26473,27005},
			{8839,8840,8842,13072,13073,11864,11865,24177,26463,29816,33338}};
		for (int group=0;group<ids.length;group++) for (int id:ids[group])
			assertEquals("reviewed identity "+id,group<3?STYLES[group]:"gear",CombatStyleTags.forItem(item(id),GearStatsSource.NONE));
		GearStatsSource multi=id -> Optional.of(new GearStats(GearSlot.NECK,10,10,10,10,10,8,0,3,30));
		for (int id:new int[]{6585,7462,25975})
			assertEquals("shared accessories", "gear",CombatStyleTags.forItem(item(id),multi));
		assertEquals("gear",CombatStyleTags.forItem(new BankPreviewItem(999999,"Unknown equipment",1),GearStatsSource.NONE));
		GearStatsSource cape=id -> Optional.of(new GearStats(GearSlot.CAPE,4,4,4,1,1,8,0,2,60));
		for (int id:new int[]{6570,21295})
			assertEquals("Melee cape despite minor hybrid accuracy",STYLES[0],CombatStyleTags.forItem(item(id),cape));
	}

	@Test public void reviewedOverridesHaveUniqueRegisteredIdentitiesAndMatchTheirDeclaredRole()
	{
		Map<String,List<Integer>> table=new OrderedItemFamilies(getClass().getResourceAsStream("combat-style-tags.tsv"),0).entries();
		assertEquals(100,table.values().stream().mapToInt(List::size).sum());
		table.forEach((tag,ids) -> ids.forEach(id -> assertEquals("reviewed identity "+id,tag,CombatStyleTags.forItem(item(id),GearStatsSource.NONE))));
	}

	@Test public void reviewedCoreArmourAndWeaponsUseTheirActualStyle()
	{
		for (CombatQualityReport.Fact fact:CombatQualityReport.FACTS.values())
		{
			if (fact.stats==null || fact.zone.equals("P") || fact.zone.equals("S") || fact.zone.equals("T")) continue;
			if (fact.stats.slotRank()>2 && fact.stats.getSlot()!=GearSlot.WEAPON) continue;
			int style=Arrays.asList("M","R","G").indexOf(fact.zone);
			if (style>=0) assertEquals("core style "+fact.id,STYLES[style],CombatStyleTags.forItem(item(fact.id),stats));
		}
	}

	@Test public void splittingChangesDestinationsWithoutChangingAlchDecisionsOrPhysicalItems() throws Exception
	{
		List<Integer> ids=new ArrayList<>(CombatQualityReport.FACTS.keySet());
		ids.addAll(Arrays.asList(11865,8839,8840,8842,6585,7462,1275,882,2));
		ids=new ArrayList<>(new LinkedHashSet<>(ids));
		for (AlchMode mode:AlchMode.values())
		{
			BankSnapshot bank=bank(ids,true);
			BankLayoutOptions options=BankLayoutOptions.defaultFor(preset).withAlchMode(mode);
			BankOrganizationPreview before=build(bank,original,options,CategoryOverrideSource.NONE);
			BankLayoutPlan split=split();
			BankOrganizationPreview after=build(bank,split,options,CategoryOverrideSource.NONE);
			assertEquals(tagIds(before,"alch"),tagIds(after,"alch"));
			assertEquals(tagIds(before,"boss-loot"),tagIds(after,"boss-loot"));
			assertConserved(bank,after);
			for (BankPreviewItem entry:after.getPlannedItems())
				if (BankTags.isOptional(entry.getLayoutTagKey()))
					assertTrue(ids(after,split.destinationOf(entry.getLayoutTagKey())).contains(entry.getItemId()));
			for (int id:new int[]{11865,8839,8840,8842,6585,7462})
				assertTrue("shared gear stays in Combat: "+id,ids(after,split.destinationOf("gear")).contains(id));
			if (mode==AlchMode.FLEXIBLE)
			{
				java.nio.file.Path output=java.nio.file.Path.of("build/reports/combat-style-groups");
				for (String tag:STYLES)
					new CombatQualityReport(preset.getType()+"-"+tag,bank,after,split.withTagAt("gear",split.destinationOf(tag)),
						Collections.emptySet(),false,0,false).write(output);
				java.nio.file.Files.writeString(output.resolve(preset.getType()+"-blueprint.txt"),BankBlueprintTextExporter.export(after));
			}
		}
	}

	@Test public void oneEnabledStyleLeavesTheOthersInCombatAndMergeRestoresTheDefault()
	{
		BankSnapshot bank=bank(Arrays.asList(11665,11664,11663,11865,4151,12924),false);
		BankLayoutPlan one=original.withTagAt(STYLES[1],8);
		BankOrganizationPreview preview=build(bank,one,options(),CategoryOverrideSource.NONE);
		assertEquals(Collections.singletonList(12924),tagIds(preview,STYLES[1]));
		assertTrue("Void helmets stay with their owned family instead of splitting across automatic styles",
			tagIds(preview,"gear").containsAll(Arrays.asList(11665,11664,11663,11865)));
		BankOrganizationPreview baseline=build(bank,original,options(),CategoryOverrideSource.NONE);
		BankOrganizationPreview merged=build(bank,one.withoutOptionalTag(STYLES[1]),options(),CategoryOverrideSource.NONE);
		assertEquals(BankBlueprintTextExporter.export(baseline),BankBlueprintTextExporter.export(merged));
	}

	@Test public void personalTagChoicesWinAndDormantStyleChoicesFallBackToCombat()
	{
		BankSnapshot bank=bank(Arrays.asList(11665,11664,4151),false);
		CategoryOverrideSource choices=id -> Optional.of(id==11665?STYLES[1]:id==11664?"gear":"tools");
		BankOrganizationPreview split=build(bank,split(),options(),choices);
		assertEquals(Collections.singletonList(11665),tagIds(split,STYLES[1]));
		assertTrue(tagIds(split,"gear").contains(11664)); assertTrue(tagIds(split,"tools").contains(4151));
		BankOrganizationPreview merged=build(bank,original,options(),choices);
		assertTrue(tagIds(merged,"gear").containsAll(Arrays.asList(11665,11664)));
		assertConserved(bank,merged);
	}

	@Test public void savedTransfersFromOldParentAndNewStylesSurviveSplitAndMerge()
	{
		BankSnapshot bank=bank(Arrays.asList(11665,11664,11663,11865),false);
		BlueprintItemOrders old=BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap("11665#0",
			new BlueprintItemOrders.Destination(original.destinationOf("tools"),"gear","tools")));
		BankOrganizationPreview moved=build(bank,split(),options().withItemOrders(old),CategoryOverrideSource.NONE);
		assertTrue(tagIds(moved,"tools").contains(11665));
		BlueprintItemOrders newer=BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap("11664#0",
			new BlueprintItemOrders.Destination(original.destinationOf("tools"),STYLES[1],"tools")));
		BankOrganizationPreview merged=build(bank,original,options().withItemOrders(newer),CategoryOverrideSource.NONE);
		assertTrue(tagIds(merged,"tools").contains(11664));
		assertFalse(BankTags.sameOriginalTag(STYLES[0],STYLES[1]));
		assertFalse(BankTags.sameOriginalTag("gear","alch"));
		assertConserved(bank,moved); assertConserved(bank,merged);
	}

	@Test public void acceptedPositionsAndRepeatedPhysicalCopiesRemainAuthoritative()
	{
		BankSnapshot bank=bank(Arrays.asList(11665,11664,11663,11865,11665,6585),true);
		BankOrganizationPreview before=build(bank,original,options(),CategoryOverrideSource.NONE);
		String accepted=AcceptedBlueprint.capture(before,"same choices");
		BankOrganizationPreview proposal=build(bank,split(),options(),CategoryOverrideSource.NONE);
		BankOrganizationPreview kept=AcceptedBlueprint.apply(accepted,"same choices",proposal,split());
		for (int tab=0;tab<10;tab++) assertEquals(ids(before,tab),ids(kept,tab));
		assertConserved(bank,proposal); assertConserved(bank,kept);
	}

	@Test public void shuffledBanksKeepStableStyleOrdersWithPlaceholders()
	{
		List<Integer> input=new ArrayList<>(CombatQualityReport.FACTS.keySet());
		BankOrganizationPreview first=build(bank(input,false),split(),options(),CategoryOverrideSource.NONE);
		for (int trial=0;trial<3;trial++)
		{
			Collections.shuffle(input,new Random(20261025L+trial));
			BankSnapshot bank=bank(input,true);
			BankOrganizationPreview preview=build(bank,split(),options(),CategoryOverrideSource.NONE);
			for (String tag:STYLES) assertEquals(tagIds(first,tag),tagIds(preview,tag));
			assertConserved(bank,preview);
		}
	}

	private BankLayoutPlan split() { return original.withTagAt(STYLES[0],7).withTagAt(STYLES[1],8).withTagAt(STYLES[2],9); }
	private BankLayoutOptions options() { return BankLayoutOptions.defaultFor(preset); }
	private BankOrganizationPreview build(BankSnapshot bank,BankLayoutPlan plan,BankLayoutOptions options,CategoryOverrideSource choices)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,stats,ItemValueSource.NONE,choices,plan,options);
	}
	private static BankPreviewItem item(int id) { return new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(id).get(),1); }
	private static BankSnapshot bank(List<Integer> ids,boolean placeholders)
	{
		List<BankItemSnapshot> items=new ArrayList<>();
		for (int id:ids) { boolean empty=placeholders && items.size()%4==0; items.add(new BankItemSnapshot(id,empty?0:3,items.size(),empty)); }
		return new BankSnapshot(items);
	}
	private static List<Integer> ids(BankOrganizationPreview preview,int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static List<Integer> tagIds(BankOrganizationPreview preview,String tag)
	{
		return preview.getPlannedItems().stream().filter(item -> tag.equals(item.getLayoutTagKey()))
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static void assertConserved(BankSnapshot bank,BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder())
			.sorted().collect(Collectors.toList()),preview.getPlannedItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder())
			.sorted().collect(Collectors.toList()));
		assertFalse(preview.getPlannedItems().stream().anyMatch(BankPreviewItem::isBlank));
	}
}

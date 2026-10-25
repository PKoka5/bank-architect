package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Owned activity kits, including their containers; expectations are independent of the layout rules. */
@RunWith(Parameterized.class)
public class SkillActivityGroupsTest
{
	private static final int[][] OUTFITS = {
		{12013,12014,12015,12016}, {10941,10939,10940,10933},
		{13258,13259,13260,13261}, {5554,5553,5555,5556,5557}
	};
	private static final int[][] EQUIPMENT = {
		{25539,11920,12019,24481,21392,9792},
		{6739,28136,28142,28140,28143,28145,28613,9807},
		{10129,305,307,309,25584,25582,25585,25587,25580,9798},
		{1523,4600,4599,6408,6410,6412,6414,6416,6418,6420,9777}
	};
	private static final int[] OTHER_TOOLS = {590,233,5340,5325,7409,5341,5343,9625,8794,
		1785,1733,10006,10008,10010,10150,10031,10029,9065,596,1887,775,946,9419,2871,
		4446,4,1595,9434,11065,1599,1597,1592,5523,4161,4155,6696,7159,7053,4156,3337,
		4162,6720,31398,1925,1929,1935,1937,1931,5376,6,8,10,12};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name="{0}") public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[]{BankPresets.IRONMAN},new Object[]{BankPresets.MAIN});
	}
	public SkillActivityGroupsTest(BankPreset preset)
	{
		this.preset = preset; plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void completeActivityKitsKeepOutfitsVerticalAndToolsInTheirContinuation() throws Exception
	{
		BankSnapshot bank = bank(largeIds(),false);
		BankOrganizationPreview preview = build(bank,plan,options(false),CategoryOverrideSource.NONE);
		List<Integer> tools = tools(preview,plan);
		assertConserved(bank,preview);
		Map<Integer,CombatQualityReport.Fact> facts = new LinkedHashMap<>();
		String[] colours = {"M","R","G","P"};
		for (int group = 0; group < OUTFITS.length; group++)
			for (int[] members : new int[][]{OUTFITS[group],EQUIPMENT[group]})
				for (int id : members) facts.put(id,new CombatQualityReport.Fact(id,colours[group],"skill-"+group,0,0,"kit",null));
		Path output = Path.of("build/reports/skill-groups");
		new CombatQualityReport(preset.getType()+"-activity-kits",bank,preview,plan,
			Collections.emptySet(),false,0,false,facts).write(output);
		Files.writeString(output.resolve(preset.getType()+"-activity-kits.txt"),
			BankBlueprintTextExporter.export(preview),StandardCharsets.UTF_8);
		for (int group = 0; group < OUTFITS.length; group++)
		{
			int origin = tools.indexOf(OUTFITS[group][0]);
			assertTrue(origin >= 0);
			for (int row = 0; row < OUTFITS[group].length; row++)
				assertEquals(origin + row*8,tools.indexOf(OUTFITS[group][row]));
			for (int member : EQUIPMENT[group])
			{
				int at = tools.indexOf(member);
				assertTrue("known member in Tools: " + member,at >= 0);
				assertTrue("owned equipment stays near its outfit instead of at the bottom of a long column: " + member,
					Math.abs(at/8-(origin/8+OUTFITS[group].length-1)) <= 6);
			}
		}
		int[][] core = {{9792,21392,25539,11920},{9807,6739,28136,28142},
			{9798,25582,25584,25585},{9777,1523,4600,4599}};
		for (int group=0;group<core.length;group++) for (int row=0;row<core[group].length;row++)
			assertEquals("core skill equipment follows its outfit",tools.indexOf(OUTFITS[group][0])+(OUTFITS[group].length+row)*8,
				tools.indexOf(core[group][row]));
	}

	@Test public void partialAndSmallKitsStayDenseAndNeverInventMissingOutfitParts()
	{
		for (int[] ids : new int[][]{{13258,25594,305,25582,25587},
			{10941,28175,6739,28140,28143}, {12013,29478,11920,21392,25541},
			{5554,5557,1523,6418,9777}, {25582,25584,25585,25587}})
		{
			BankSnapshot bank = bank(Arrays.stream(ids).boxed().collect(Collectors.toList()),false);
			BankOrganizationPreview preview = build(bank,plan,options(false),CategoryOverrideSource.NONE);
			assertEquals(ids.length,tools(preview,plan).size());
			assertConserved(bank,preview);
		}
	}

	@Test public void shufflesQuantitiesAndWithdrawnPlaceholdersKeepTheSameActivityPositions()
	{
		List<Integer> input = largeIds();
		List<Integer> expected = tools(build(bank(input,false),plan,options(false),CategoryOverrideSource.NONE),plan);
		for (int trial = 0; trial < 4; trial++)
		{
			Collections.shuffle(input,new Random(20261025L+trial));
			BankSnapshot bank = bank(input,trial%2==1);
			BankOrganizationPreview preview = build(bank,plan,options(false),CategoryOverrideSource.NONE);
			assertEquals(expected,tools(preview,plan)); assertConserved(bank,preview);
		}
	}

	@Test public void frequentlyUsedCheckboxOffersBothQuickAccessAndSkillGrouping()
	{
		BankSnapshot bank = bank(Arrays.asList(12013,12014,11920,10941,10939,6739),true);
		BankOrganizationPreview preview = build(bank,plan,options(false),CategoryOverrideSource.NONE);
		assertTrue(tools(preview,plan).containsAll(Arrays.asList(11920,6739)));
		if (preset.getType()==BankPresetType.IRONMAN)
			assertTrue(ids(build(bank,plan,options(true),CategoryOverrideSource.NONE),
				plan.destinationOf("frequently-used")).containsAll(Arrays.asList(11920,6739)));
		CategoryOverrideSource choice = id -> id == 11920 ? Optional.of("frequently-used") : Optional.empty();
		BankOrganizationPreview personal = build(bank,plan,options(false),choice);
		assertTrue(ids(personal,plan.destinationOf("frequently-used")).contains(11920));
		assertFalse(tools(personal,plan).contains(11920));
		assertConserved(bank,personal);
	}

	@Test public void absentOutfitsAndTwoCopiesOfOnlyTheBootSlotDoNotDisableQuickAccess()
	{
		if (preset.getType()!=BankPresetType.IRONMAN) return;
		for (List<Integer> ids : Arrays.asList(Arrays.asList(11920,6739),Arrays.asList(12016,29478,11920)))
		{
			BankSnapshot bank = bank(ids,false);
			BankOrganizationPreview preview = build(bank,plan,options(true),CategoryOverrideSource.NONE);
			assertTrue(ids(preview,plan.destinationOf("frequently-used")).contains(11920));
			assertConserved(bank,preview);
		}
	}

	@Test public void movedTagsAndPersonalOutfitChoicesKeepTheirChosenDestinations()
	{
		BankSnapshot bank = bank(Arrays.asList(12013,12014,11920,13258,13259,25582,28143),false);
		BankLayoutPlan split = plan.withTagAt("containers",plan.destinationOf("raw-resources"));
		BankOrganizationPreview preview = build(bank,split,options(false),CategoryOverrideSource.NONE);
		assertTrue(ids(preview,split.destinationOf("containers")).containsAll(Arrays.asList(25582,28143)));
		assertFalse(tools(preview,split).contains(25582));
		assertConserved(bank,preview);
		if (preset.getType()==BankPresetType.IRONMAN)
		{
			CategoryOverrideSource choices = id -> id == 12013 || id == 12014 ? Optional.of("cosmetics") : Optional.empty();
			BankOrganizationPreview personal = build(bank,plan,options(true),choices);
			assertTrue(ids(personal,plan.destinationOf("frequently-used")).contains(11920));
			assertTrue(ids(personal,plan.destinationOf("cosmetics")).containsAll(Arrays.asList(12013,12014)));
		}
	}

	@Test public void savedAndPreviouslyAcceptedPhysicalOrdersOverrideAutomaticActivityGrouping()
	{
		BankSnapshot bank = bank(largeIds(),false);
		BankOrganizationPreview automatic = build(bank,plan,options(false),CategoryOverrideSource.NONE);
		List<Integer> reversed = new ArrayList<>(tools(automatic,plan)); Collections.reverse(reversed);
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withTab(plan.destinationOf("tools"),reversed);
		BankOrganizationPreview manual = build(bank,plan,options(false).withItemOrders(orders),CategoryOverrideSource.NONE);
		assertEquals(reversed,tools(manual,plan)); assertConserved(bank,manual);
		String kept = AcceptedBlueprint.capture(manual,"unchanged choices").replace("BAkeep1|"+AcceptedBlueprint.RULES+"|","BAkeep1|3|");
		assertEquals(reversed,tools(AcceptedBlueprint.apply(kept,"unchanged choices",automatic,plan),plan));
	}

	@Test public void fixedAlchAndTeleportFunctionsStillWinOverSkillMembership()
	{
		BankSnapshot bank = bank(Arrays.asList(12013,12014,11920,1275,10941,10939,6739,1359,9780,1785),false);
		BankOrganizationPreview preview = build(bank,plan,options(true).withAlchMode(AlchMode.FIXED),CategoryOverrideSource.NONE);
		assertTrue(ids(preview,plan.destinationOf("alch")).containsAll(Arrays.asList(1275,1359)));
		assertTrue(ids(preview,plan.destinationOf("teleports")).contains(9780));
		assertConserved(bank,preview);
	}

	@Test public void repeatedPhysicalItemsKeepEveryStackAndPlaceholder()
	{
		List<Integer> ids = largeIds(); ids.add(25582); ids.add(12013);
		BankSnapshot bank = bank(ids,true);
		BankOrganizationPreview preview = build(bank,plan,options(false),CategoryOverrideSource.NONE);
		assertConserved(bank,preview);
	}

	@Test public void reviewedSkillTablesHaveKnownIdentitiesAndOnePrimaryToolSkill()
	{
		OrderedItemFamilies table = new OrderedItemFamilies(getClass().getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/tool-layout-families.tsv"),0,false);
		Set<Integer> seen = new HashSet<>();
		table.group("families").forEach((key,ids) -> ids.forEach(id -> {
			assertTrue("one primary tool skill: " + id,seen.add(id));
			assertTrue("known exact identity: " + id,CompositeItemCatalog.DEFAULT.findById(id).isPresent());
		}));
		for (int dummy : new int[]{25581,25583,25586,28137,28141,28144,21344,21346}) assertFalse(seen.contains(dummy));
		for (int id : new int[]{28143,28145}) assertEquals(ItemCategory.TOOL,CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getCategory());
	}

	private BankLayoutOptions options(boolean frequent)
	{
		BankLayoutOptions base=BankLayoutOptions.defaultFor(preset);
		return new BankLayoutOptions(base.fillGearRows(),base.fillHerbloreRows(),base.alchPile(),Collections.emptyMap(),
			base.gearLayout(),base.potionDoses(),base.runeOrder(),base.teleportOrder(),frequent);
	}
	private BankOrganizationPreview build(BankSnapshot bank,BankLayoutPlan layout,BankLayoutOptions options,CategoryOverrideSource choices)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,GearStatsSource.NONE,
			ItemValueSource.NONE,choices,layout,options);
	}
	private static List<Integer> largeIds()
	{
		Set<Integer> ids = new LinkedHashSet<>();
		for (int[][] groups : new int[][][]{OUTFITS,EQUIPMENT}) for (int[] group : groups) for (int id : group) ids.add(id);
		for (int id : OTHER_TOOLS) ids.add(id);
		return new ArrayList<>(ids);
	}
	private static BankSnapshot bank(List<Integer> ids,boolean placeholders)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : ids)
		{
			boolean placeholder = placeholders && entries.size()%3==0;
			entries.add(new BankItemSnapshot(id,placeholder?0:entries.size()%5+1,entries.size(),placeholder));
		}
		return new BankSnapshot(entries);
	}
	private static List<Integer> tools(BankOrganizationPreview preview,BankLayoutPlan layout) { return ids(preview,layout.destinationOf("tools")); }
	private static List<Integer> ids(BankOrganizationPreview preview,int destination)
	{
		return preview.getCategories().get(destination).getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static void assertConserved(BankSnapshot bank,BankOrganizationPreview preview)
	{
		List<String> expected=bank.getPhysicalItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder()).sorted().collect(Collectors.toList());
		List<String> actual=preview.getPlannedItems().stream().map(item -> item.getItemId()+"/"+item.getQuantity()+"/"+item.isPlaceholder()).sorted().collect(Collectors.toList());
		assertEquals(expected,actual);
		assertTrue(preview.getPlannedItems().stream().noneMatch(BankPreviewItem::isBlank));
	}
}

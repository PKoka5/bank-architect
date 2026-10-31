package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

public class MainDiaryPlacementTest
{
	// All twelve regional reward families, in the existing diary grid's region order.
	private static final int[][] REWARDS = {
		{13121,13122,13123,13124},{13133,13134,13135,13136},
		{13125,13126,13127,13128},{13117,13118,13119,13120},
		{13129,13130,13131,13132},{13137,13138,13139,13140},
		{11136,11138,11140,13103},{13112,13113,13114,13115},
		{22941,22943,22945,22947},{13104,13105,13106,13107},
		{13141,13142,13143,13144},{13108,13109,13110,13111}
	};
	private static final BankLayoutPlan PLAN=BankLayoutPlan.defaultFor(BankPresets.MAIN);

	@Test public void allRegionalTiersAndBothCompletionCapesUseTeleportsIncludingPlaceholders()
	{
		List<Integer> all=new ArrayList<>();
		for(int[] family:REWARDS) for(int id:family) all.add(id);
		all.add(19476); all.add(13069);
		BankSnapshot bank=bank(all);
		for(boolean placeholders:new boolean[]{false,true})
		{
			if(placeholders) bank=new BankSnapshot(bank.getItems().stream()
				.map(i->new BankItemSnapshot(i.getItemId(),0,i.getSlotIndex(),true)).collect(Collectors.toList()));
			BankOrganizationPreview preview=build(bank,PLAN,BankLayoutOptions.defaultFor(BankPresets.MAIN),CategoryOverrideSource.NONE);
			assertEquals(new HashSet<>(all),new HashSet<>(ids(preview,PLAN.destinationOf("teleports"))));
			assertEquals(all.size(),preview.getPlannedItemCount());
			for(BankPreviewItem item:preview.getPlannedItems()) if(!item.isBlank())
			{
				assertEquals("teleports",item.getLayoutTagKey());
				assertEquals(placeholders,item.isPlaceholder());
				assertEquals(placeholders?0:1,item.getQuantity());
			}
		}
	}

	@Test public void fullRegionalGridStaysTogetherBesideRunesAndOtherTeleports()
	{
		List<Integer> source=new ArrayList<>(Arrays.asList(995,556,555,557,554,8013,2552,563,560));
		for(int[] family:REWARDS) source.add(family[3]);
		BankOrganizationPreview preview=build(bank(source),PLAN,BankLayoutOptions.defaultFor(BankPresets.MAIN),CategoryOverrideSource.NONE);
		List<Integer> utility=ids(preview,PLAN.destinationOf("teleports"));
		int start=utility.indexOf(REWARDS[0][3]);
		assertTrue(start>=0);
		for(int region=0;region<12;region++)
			assertEquals(Integer.valueOf(REWARDS[region][3]),utility.get(start+region/4*8+region%4));
		assertEquals(Collections.singletonList(995),ids(preview,0));
	}

	@Test public void rewardsFollowAMovedTeleportTagAndExplicitCorrectionsWin()
	{
		BankLayoutPlan moved=PLAN.withTagAt("teleports",7);
		BankOrganizationPreview preview=build(bank(Arrays.asList(13104,13108,22947)),moved,
			BankLayoutOptions.defaultFor(BankPresets.MAIN),id->id==13104?Optional.of("tools"):Optional.empty());
		assertTrue(ids(preview,moved.destinationOf("tools")).contains(13104));
		assertEquals(new HashSet<>(Arrays.asList(13108,22947)),new HashSet<>(ids(preview,7)));
	}

	@Test public void legacyEditorRoutesRemainActiveButDoNotOverrideANewCorrection()
	{
		BlueprintItemOrders saved=BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap("13121#0",
			new BlueprintItemOrders.Destination(2,"gear","gear")));
		BankLayoutOptions options=BankLayoutOptions.defaultFor(BankPresets.MAIN).withItemOrders(saved);
		assertEquals(Collections.singletonList(13121),ids(build(bank(Arrays.asList(13121)),PLAN,options,CategoryOverrideSource.NONE),2));
		BankOrganizationPreview corrected=build(bank(Arrays.asList(13121)),PLAN,options,id->Optional.of("tools"));
		assertEquals(Collections.singletonList(13121),ids(corrected,PLAN.destinationOf("tools")));
		assertEquals(saved.serialize(),options.itemOrders().serialize());
		BankLayoutPlan dormant=PLAN.withTagAt("gear",9);
		assertEquals(Collections.singletonList(13121),ids(build(bank(Arrays.asList(13121)),dormant,options,CategoryOverrideSource.NONE),1));
	}

	@Test public void keptOldMainBlueprintRetainsItsOriginalPosition()
	{
		BankOrganizationPreview old=build(bank(Arrays.asList(13104,995)),PLAN,
			BankLayoutOptions.defaultFor(BankPresets.MAIN),id->id==13104?Optional.of("tools"):Optional.empty());
		String kept=AcceptedBlueprint.capture(old,"same profile");
		BankOrganizationPreview proposed=build(bank(Arrays.asList(13104,995)),PLAN,
			BankLayoutOptions.defaultFor(BankPresets.MAIN),CategoryOverrideSource.NONE);
		assertTrue(ids(proposed,1).contains(13104));
		assertTrue(ids(AcceptedBlueprint.apply(kept,"same profile",proposed,PLAN),PLAN.destinationOf("tools")).contains(13104));
	}

	@Test public void ironmanClassificationsAndNonRewardPropsKeepTheirExistingRoles()
	{
		CatalogItem gloves=CompositeItemCatalog.DEFAULT.describeOrUnknown(11136);
		assertEquals("skilling-tools",PresetCategoryMapper.map(BankPresets.IRONMAN,gloves,false).getKey());
		CatalogItem sword=CompositeItemCatalog.DEFAULT.describeOrUnknown(13108);
		assertEquals("combat-gear",PresetCategoryMapper.map(BankPresets.IRONMAN,sword,false).getKey());
		assertEquals("currency-utilities",PresetCategoryMapper.map(BankPresets.IRONMAN,sword,true).getKey());
		BankOrganizationPreview preview=build(bank(Arrays.asList(762,770,13070)),PLAN,
			BankLayoutOptions.defaultFor(BankPresets.MAIN),CategoryOverrideSource.NONE);
		assertTrue(ids(preview,1).isEmpty());
		assertTrue(ids(preview,8).contains(13070));
		assertTrue(ids(preview,9).containsAll(Arrays.asList(762,770)));
	}

	private static BankSnapshot bank(List<Integer> ids)
	{
		List<BankItemSnapshot> items=new ArrayList<>();
		for(int id:ids) items.add(new BankItemSnapshot(id,1,items.size()));
		return new BankSnapshot(items);
	}
	private static BankOrganizationPreview build(BankSnapshot bank,BankLayoutPlan plan,BankLayoutOptions options,CategoryOverrideSource overrides)
	{ return PreviewFixtureBuilder.build(bank,CompositeItemCatalog.DEFAULT,BankPresets.MAIN,GearStatsSource.NONE,ItemValueSource.NONE,overrides,plan,options); }
	private static List<Integer> ids(BankOrganizationPreview preview,int tab)
	{ return preview.getCategories().get(tab).getItems().stream().filter(i->!i.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList()); }
}

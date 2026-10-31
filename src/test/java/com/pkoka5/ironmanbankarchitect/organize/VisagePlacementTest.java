package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Issue #50: exact shield components belong with Combat, never as wearable equipment. */
@RunWith(Parameterized.class)
public class VisagePlacementTest
{
	private static final int[] VISAGES={11286,21637,22006};
	private final BankPreset preset;
	private final BankLayoutPlan plan;
	@Parameterized.Parameters(name="{0}") public static Collection<Object[]> presets()
	{ return Arrays.asList(new Object[]{BankPresets.IRONMAN},new Object[]{BankPresets.MAIN}); }
	public VisagePlacementTest(BankPreset preset) { this.preset=preset; this.plan=BankLayoutPlan.defaultFor(preset); }

	@Test public void exactComponentFactsAndBothDefaultBlueprintsAgree()
	{
		BankOrganizationPreview preview=build(bank(1),plan,defaults(AlchMode.FLEXIBLE,GearLayout.GRID_STYLES),CategoryOverrideSource.NONE,GearStatsSource.NONE);
		assertEquals(VISAGES.length,preview.getPlannedItemCount());
		for(int id:VISAGES)
		{
			CatalogItem fact=CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals(ItemCategory.UNIQUE,fact.getCategory());
			assertEquals("equipment-upgrade",fact.getSubcategory());
			assertTrue(fact.getTags().contains("equipment-upgrade"));
			assertEquals("support-upgrade",GearSetSemanticRuleSet.roleOf(id));
			assertEquals("combat-gear",PresetCategoryMapper.map(preset,fact,false).getKey());
			assertPlaced(preview,id,"gear");
			for(GearStyle style:Arrays.asList(GearStyle.MELEE,GearStyle.RANGED,GearStyle.MAGIC))
				assertEquals(-1,PrimaryGearSelector.slot(style,id));
			assertFalse(IronmanAlchCandidateCatalog.containsFixed(id));
		}
	}

	@Test public void stacksAndPlaceholdersStayOutOfBothAlchModesAndSurviveEveryGearLayout()
	{
		for(AlchMode alch:AlchMode.values()) for(GearLayout layout:GearLayout.values()) for(int quantity:new int[]{1,25,0})
		{
			BankOrganizationPreview preview=build(bank(quantity),plan,defaults(alch,layout),CategoryOverrideSource.NONE,GearStatsSource.NONE);
			assertEquals(VISAGES.length,preview.getPlannedItemCount());
			for(int id:VISAGES)
			{
				assertPlaced(preview,id,"gear");
				assertEquals(quantity,item(preview,id).getQuantity());
				assertEquals(quantity==0,item(preview,id).isPlaceholder());
			}
		}
	}

	@Test public void evenErroneousPositiveEquipmentStatsCannotPromoteComponentsToPrimaryOrAlch()
	{
		// Synthetic policy stress, not claims about real equipment stats: visages are not equipable.
		for(GearSlot slot:new GearSlot[]{GearSlot.HEAD,GearSlot.SHIELD,GearSlot.WEAPON})
		{
			GearStatsSource positive=id->Optional.of(new GearStats(slot,100,100,100,100,100,100,100,10,
				100,100,100,100,100,100,4));
			List<BankPreviewItem> components=Arrays.stream(VISAGES).mapToObj(id->new BankPreviewItem(
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id),25)).collect(Collectors.toList());
			assertTrue(GearItemSorter.plan(components,positive).getSetupRows().isEmpty());
			for(AlchMode mode:AlchMode.values())
			{
				BankOrganizationPreview preview=build(bank(25),plan,defaults(mode,GearLayout.GRID_STYLES),CategoryOverrideSource.NONE,positive);
				for(int id:VISAGES) { assertPlaced(preview,id,"gear"); assertEquals(ItemCategory.UNIQUE,item(preview,id).getItemCategory()); }
			}
		}
	}

	@Test public void explicitAssignmentsWinAndMovingTheGearTagCarriesComponents()
	{
		BankOrganizationPreview moved=build(bank(1),plan.withTagAt("gear",8),defaults(AlchMode.FIXED,GearLayout.GRID_STYLES),CategoryOverrideSource.NONE,GearStatsSource.NONE);
		for(int id:VISAGES) assertTrue(ids(moved,8).contains(id));
		Map<Integer,String> choices=new HashMap<>(); choices.put(11286,"boss-loot"); choices.put(21637,"tools"); choices.put(22006,"alch");
		for(AlchMode mode:AlchMode.values())
		{
			BankOrganizationPreview personal=build(bank(1),plan,defaults(mode,GearLayout.GRID_STYLES),id->Optional.ofNullable(choices.get(id)),GearStatsSource.NONE);
			choices.forEach((id,tag)->assertPlaced(personal,id,tag));
		}
	}

	@Test public void oldKeptAndCapturedCleanupPositionsRemainUsableAfterTheClassificationFix()
	{
		BankLayoutOptions options=defaults(AlchMode.FLEXIBLE,GearLayout.GRID_STYLES);
		BankOrganizationPreview old=build(bank(1),plan,options,id->Optional.of("cleanup"),GearStatsSource.NONE);
		BankOrganizationPreview current=build(bank(0),plan,options,CategoryOverrideSource.NONE,GearStatsSource.NONE);
		String saved=AcceptedBlueprint.capture(old,"same choices").replace("BAkeep1|"+AcceptedBlueprint.RULES+"|","BAkeep1|11|");
		BankOrganizationPreview kept=AcceptedBlueprint.apply(saved,"same choices",current,plan);
		for(int id:VISAGES) { assertPlaced(kept,id,"cleanup"); assertTrue(item(kept,id).isPlaceholder()); }
		Map<Integer,List<Integer>> physical=new LinkedHashMap<>(); physical.put(9,Arrays.asList(11286,21637,22006));
		BlueprintItemOrders captured=BlueprintItemOrders.capture(physical,old,BlueprintItemOrders.EMPTY);
		BankOrganizationPreview restored=build(bank(0),plan,options.withItemOrders(BlueprintItemOrders.parse(captured.serialize())),CategoryOverrideSource.NONE,GearStatsSource.NONE);
		assertEquals(physical.get(9),ids(restored,9));
		for(int id:VISAGES) { assertEquals("gear",item(restored,id).getLayoutTagKey()); assertTrue(item(restored,id).isPlaceholder()); }
	}

	@Test public void rawNotesAndPlaceholderDefinitionsAreNotReclassifiedByAdjacentIdsOrNames()
	{
		for(int id:new int[]{11287,16834,21638,21639,22007,22008})
		{
			CatalogItem raw=CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
			assertEquals(ItemCategory.CLEANUP,raw.getCategory());
			assertFalse(raw.getTags().contains("equipment-upgrade"));
			assertEquals("",GearSetSemanticRuleSet.roleOf(id));
		}
	}

	private BankLayoutOptions defaults(AlchMode mode,GearLayout layout)
	{
		BankLayoutOptions base=BankLayoutOptions.defaultFor(preset);
		return new BankLayoutOptions(base.fillGearRows(),base.fillHerbloreRows(),base.alchPile(),Collections.emptyMap(),
			layout,base.potionDoses(),base.runeOrder(),base.teleportOrder(),base.gatherFrequentlyUsed()).withAlchMode(mode);
	}
	private BankOrganizationPreview build(BankSnapshot bank,BankLayoutPlan plan,BankLayoutOptions options,CategoryOverrideSource overrides,GearStatsSource stats)
	{ return PreviewFixtureBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,stats,id->900000,overrides,plan,options); }
	private static BankSnapshot bank(int quantity)
	{
		List<BankItemSnapshot> items=new ArrayList<>();
		for(int id:VISAGES) items.add(new BankItemSnapshot(id,quantity,items.size(),quantity==0));
		return new BankSnapshot(items);
	}
	private void assertPlaced(BankOrganizationPreview preview,int id,String tag)
	{ assertEquals(tag,item(preview,id).getLayoutTagKey()); assertTrue(ids(preview,plan.destinationOf(tag)).contains(id)); }
	private static BankPreviewItem item(BankOrganizationPreview preview,int id)
	{ return preview.getPlannedItems().stream().filter(i->i.getItemId()==id).findFirst().orElseThrow(AssertionError::new); }
	private static List<Integer> ids(BankOrganizationPreview preview,int tab)
	{ return preview.getCategories().get(tab).getItems().stream().filter(i->!i.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList()); }
}

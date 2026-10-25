package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

/** Independently expected primary-helm choices; ordinary placement and disposal scores stay separate. */
public class MeleePrimaryHelmTest
{
	private static final int NEITIZNOT = 10828;
	private static final int GUTHAN = 4724;
	// Wiki item/equipment pages reviewed 2026-10-24; see tools/research/melee-bis/helm-review.tsv.
	private static final GearStats NEITIZNOT_STATS = new GearStats(GearSlot.HEAD,
		0,0,0,0,0,3,0,3,31,29,34,3,30,0,0);
	private static final GearStats GUTHAN_STATS = new GearStats(GearSlot.HEAD,
		0,0,0,0,0,0,0,0,55,58,54,-1,62,0,0);

	@Test public void offensiveNeitiznotBeatsGuthanWithoutRewritingTheSharedScore()
	{
		Map<Integer,GearStats> stats = helmStats();
		GearStatsSource source = source(stats);
		assertEquals("existing tier/defence score remains separate from primary selection",1028,
			GearItemSorter.score(item(GUTHAN),source));
		assertEquals("existing shared Neitiznot score is unchanged",751,
			GearItemSorter.score(item(NEITIZNOT),source));
		assertEquals(NEITIZNOT,primary(Arrays.asList(item(GUTHAN),item(NEITIZNOT)),source));
		assertEquals(Arrays.asList(GUTHAN,NEITIZNOT),ids(GearItemSorter.dense(
			Arrays.asList(item(NEITIZNOT),item(GUTHAN)),source)));
	}

	@Test public void usableGuthanDurabilityStatesCannotDisplaceNeitiznot()
	{
		for (int id : new int[] {4724,4904,4905,4906,4907})
		{
			Map<Integer,GearStats> stats = helmStats(); stats.put(id,GUTHAN_STATS);
			List<BankPreviewItem> input = Arrays.asList(item(id),item(NEITIZNOT));
			GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source(stats));
			assertEquals("usable Guthan state " + id,NEITIZNOT,selected.getSetupRows().get(0).getItemId());
			assertEquals(Collections.singletonList(id),ids(selected.getTail()));
		}
	}

	@Test public void guthanAloneRemainsAValidOwnedFallback()
	{
		for (int id : new int[] {4724,4904,4905,4906,4907})
		{
			assertEquals(id,primary(Collections.singletonList(item(id)),value -> Optional.of(GUTHAN_STATS)));
			assertEquals(id,primary(Collections.singletonList(item(id)),GearStatsSource.NONE));
		}
	}

	@Test public void reviewedGeneralCandidateAlsoWinsWithoutStatsOrWithPartialStats()
	{
		List<BankPreviewItem> input = Arrays.asList(item(GUTHAN),item(NEITIZNOT));
		assertEquals(NEITIZNOT,primary(input,GearStatsSource.NONE));
		assertEquals(NEITIZNOT,primary(input,id -> id == GUTHAN ? Optional.of(GUTHAN_STATS) : Optional.empty()));
		assertEquals(NEITIZNOT,primary(input,id -> id == NEITIZNOT ? Optional.of(NEITIZNOT_STATS) : Optional.empty()));
	}

	@Test public void aWithdrawnGeneralHelmKeepsItsPlaceAndPhysicalState()
	{
		BankPreviewItem placeholder = new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(NEITIZNOT),0,true);
		GearItemSorter.GearLayout selected = GearItemSorter.plan(Arrays.asList(item(GUTHAN),placeholder),source(helmStats()));
		BankPreviewItem head = selected.getSetupRows().get(0);
		assertEquals(NEITIZNOT,head.getItemId());
		assertTrue(head.isPlaceholder());
		assertEquals(0,head.getQuantity());
		assertEquals(1,head.physicalBankSlotCount());
		assertEquals(0,head.physicalBankSlots().get(0).getQuantity());
	}

	@Test public void torvaAndItsCosmeticVariantUseAnIdentityTieBreak()
	{
		GearStats torva = CombatQualityReport.FACTS.get(26382).stats;
		for (GearStatsSource source : Arrays.asList((GearStatsSource)(id -> Optional.of(torva)),GearStatsSource.NONE))
			for (List<BankPreviewItem> input : Arrays.asList(Arrays.asList(item(28254),item(26382)),
				Arrays.asList(item(26382),item(28254))))
				assertEquals("equal cosmetic preferences resolve by stable real identity",26382,primary(input,source));
	}

	@Test public void realOffensiveStatsBeatAFormerRaidArmourNavigationPriority()
	{
		Map<Integer,GearStats> stats = helmStats();
		stats.put(26382,CombatQualityReport.FACTS.get(26382).stats);
		stats.put(24419,CombatQualityReport.FACTS.get(24419).stats);
		stats.put(30750,CombatQualityReport.FACTS.get(30750).stats);
		assertEquals("Torva's eight Strength leads reviewed six-Strength alternatives",26382,
			primary(items(NEITIZNOT,30750,24419,26382),source(stats)));
		assertEquals("Inquisitor's current six Strength, ten crush and Prayer beat the remaining raw stats",24419,
			primary(items(NEITIZNOT,30750,24419),source(stats)));
		assertEquals("slash-oriented Oathplate still contributes unconditional offensive stats",30750,
			primary(items(NEITIZNOT,30750),source(stats)));
	}

	@Test public void aMissingStatSwitchesTheComparableGroupToOneStableFallbackOrder()
	{
		Map<Integer,GearStats> stats = helmStats();
		// Controlled future-stat example: numeric preference deliberately disagrees with reviewed fallback rank.
		stats.put(26382,new GearStats(GearSlot.HEAD,0,0,0,-5,-5,1,0,1,59,60,62,-2,57,0,0));
		assertEquals("with complete relevant stats, live offensive bonuses decide",NEITIZNOT,
			primary(items(NEITIZNOT,26382),source(stats)));
		List<BankPreviewItem> input = items(NEITIZNOT,24271,26382);
		Random random = new Random(20261024);
		for (int trial = 0; trial < 20; trial++)
		{
			Collections.shuffle(input,random);
			assertEquals("partially known general group, shuffle " + trial,26382,primary(input,source(stats)));
		}
	}

	@Test public void knownInactiveHelmsRemainVisibleButCannotBeatAUsableHead()
	{
		for (int inactive : new int[] {26376,12929,13196,13198,29073,4908})
		{
			Map<Integer,GearStats> stats = helmStats();
			// Deliberately misleading numbers must not make a known nonfunctional state wearable.
			stats.put(inactive,new GearStats(GearSlot.HEAD,99,99,99,0,0,99,0,99,999));
			List<BankPreviewItem> input = items(inactive,NEITIZNOT);
			GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source(stats));
			assertEquals("inactive state " + inactive,NEITIZNOT,selected.getSetupRows().get(0).getItemId());
			assertEquals(Collections.singletonList(inactive),ids(selected.getTail()));
			assertEquals(NEITIZNOT,primary(input,GearStatsSource.NONE));
			assertEquals("usable tank fallback must also beat inactive state " + inactive,GUTHAN,
				primary(items(inactive,GUTHAN),source(stats)));
			assertEquals(GUTHAN,primary(items(inactive,GUTHAN),GearStatsSource.NONE));
		}
	}

	@Test public void namesCannotTurnAnUnknownItemIntoAReviewedPrimaryCandidate()
	{
		int unknown = 989910;
		BankPreviewItem impostor = new BankPreviewItem(new CatalogItem(unknown,"Torva full helm",
			ItemCategory.GEAR,"head",Collections.emptySet(),null),1);
		Map<Integer,GearStats> stats = helmStats();
		stats.put(unknown,new GearStats(GearSlot.HEAD,100,100,100,0,0,100,0,100,1000));
		GearItemSorter.GearLayout selected = GearItemSorter.plan(Arrays.asList(impostor,item(NEITIZNOT)),source(stats));
		assertEquals(NEITIZNOT,selected.getSetupRows().get(0).getItemId());
		assertEquals(Collections.singletonList(unknown),ids(selected.getTail()));
	}

	@Test public void anEntirelyUnreviewedPoolKeepsTheExistingFallbackComparison()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		stats.put(989911,new GearStats(GearSlot.HEAD,1,0,0,0,0,1,0,0,1));
		stats.put(989912,new GearStats(GearSlot.HEAD,2,0,0,0,0,2,0,0,2));
		List<BankPreviewItem> input = Arrays.asList(new BankPreviewItem(989911,"Unreviewed alpha helm",1),
			new BankPreviewItem(989912,"Unreviewed beta helm",1));
		assertEquals(989912,primary(input,source(stats)));
		assertEquals(new HashSet<>(Arrays.asList(989911,989912)),new HashSet<>(ids(GearItemSorter.layout(input,source(stats)))));
	}

	@Test public void meleeHeadRepairCannotChangeRangedMagicOrSharedAccessoryAssignments() throws Exception
	{
		List<BankPreviewItem> input = new ArrayList<>();
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			getClass().getResourceAsStream("reported-midgame-combat-85.tsv")),StandardCharsets.UTF_8)))
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] row = line.split("\t",-1); int id = Integer.parseInt(row[0]);
				input.add(new BankPreviewItem(new CatalogItem(id,row[1],ItemCategory.valueOf(row[2]),row[3],Collections.emptySet(),null),1));
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				stats.put(id,new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
					v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
			}
		}
		GearStatsSource source = source(stats);
		Map<Integer,Integer> before = GearItemSorter.setupTargets(GearItemSorter.plan(input,source),200);
		input.add(item(GUTHAN)); stats.put(GUTHAN,GUTHAN_STATS);
		Map<Integer,Integer> after = GearItemSorter.setupTargets(GearItemSorter.plan(input,source),200);
		assertEquals(Integer.valueOf(0),after.get(NEITIZNOT));
		assertFalse(after.containsKey(GUTHAN));
		for (int column = 1; column <= 2; column++)
			assertEquals("other style and shared-accessory reservation column " + column,column(before,column),column(after,column));
	}

	@Test public void manualCategorySavedOrderAndAcceptedTargetRemainAuthoritative()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(GUTHAN,1,0),new BankItemSnapshot(NEITIZNOT,1,1)));
		for (BankPreset preset : Arrays.asList(BankPresets.IRONMAN,BankPresets.MAIN))
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankLayoutOptions options = BankLayoutOptions.defaultFor(preset);
			BankOrganizationPreview proposal = build(bank,preset,plan,options,CategoryOverrideSource.NONE);
			assertEquals(Arrays.asList(NEITIZNOT,GUTHAN),combatIds(proposal,plan));
			BankOrganizationPreview manual = build(bank,preset,plan,options.withItemOrders(
				BlueprintItemOrders.EMPTY.withTab(plan.destinationOf("gear"),Arrays.asList(GUTHAN,NEITIZNOT))),CategoryOverrideSource.NONE);
			assertEquals(Arrays.asList(GUTHAN,NEITIZNOT),combatIds(manual,plan));
			String accepted = AcceptedBlueprint.capture(manual,"same personal settings");
			assertEquals(Arrays.asList(GUTHAN,NEITIZNOT),combatIds(AcceptedBlueprint.apply(
				accepted,"same personal settings",proposal,plan),plan));
			BankOrganizationPreview choice = build(bank,preset,plan,options,
				id -> id == NEITIZNOT ? Optional.of("alch") : Optional.empty());
			assertEquals(Collections.singletonList(GUTHAN),combatIds(choice,plan));
			assertEquals("alch",choice.getPlannedItems().stream().filter(item -> item.getItemId() == NEITIZNOT)
				.findFirst().get().getLayoutTagKey());
		}
	}

	@Test public void anInactiveOnlyPoolKeepsEveryItemWithoutInventingAPrimaryHead()
	{
		List<BankPreviewItem> input = items(26376,12929,29073,4908);
		for (GearStatsSource source : Arrays.asList(GearStatsSource.NONE,
			(GearStatsSource)(id -> Optional.of(new GearStats(GearSlot.HEAD,99,99,99,0,0,99,0,99,999)))))
		{
			GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source);
			assertTrue("nonfunctional states are not a wearable primary substitute",selected.getSetupRows().isEmpty());
			assertEquals(new HashSet<>(ids(input)),new HashSet<>(ids(selected.getTail())));
			assertEquals(input.size(),GearItemSorter.layout(input,source).size());
		}
	}

	@Test public void removingInactiveHeadsStillLeavesTheUnknownLegacyFallbackAvailable()
	{
		int unknown = 989913;
		List<BankPreviewItem> input = Arrays.asList(item(26376),new BankPreviewItem(unknown,"Unreviewed rune helm",1));
		GearStatsSource controlled = id -> Optional.of(new GearStats(GearSlot.HEAD,
			0,0,0,0,0,id == unknown ? 1 : 99,0,0,id == unknown ? 1 : 999));
		for (GearStatsSource source : Arrays.asList(GearStatsSource.NONE,controlled))
		{
			GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source);
			assertEquals(unknown,selected.getSetupRows().get(0).getItemId());
			assertEquals(Collections.singletonList(26376),ids(selected.getTail()));
			assertFalse("an unreviewed legacy fallback gains no new reviewed-BIS explanation",
				selected.getSetupRows().get(0).getPlacementReason().contains("General Melee head:"));
		}
	}

	@Test public void missingFallbackStatsCannotChangeTheCompleteGeneralBandComparison()
	{
		Map<Integer,GearStats> stats = helmStats(); stats.remove(GUTHAN);
		// Controlled values deliberately make the numeric result differ from the reviewed fallback order.
		stats.put(26382,new GearStats(GearSlot.HEAD,0,0,0,-5,-5,1,0,1,59,60,62,-2,57,0,0));
		assertEquals("only the fallback band is incomplete",NEITIZNOT,
			primary(items(26382,GUTHAN,NEITIZNOT),source(stats)));
	}

	@Test public void comparisonExplanationAndPhysicalCopiesKeepTheirActualState()
	{
		BankPreviewItem repeated = new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(NEITIZNOT),
			1,false,Arrays.asList(1,0)).withPlacementReason("Original category evidence.");
		List<BankPreviewItem> input = Arrays.asList(item(GUTHAN),repeated);
		BankPreviewItem numeric = GearItemSorter.plan(input,source(helmStats())).getSetupRows().get(0);
		String reason = numeric.getPlacementReason();
		assertTrue(reason,reason.contains("Strength 3, attack 0, Prayer 3, defence 127."));
		assertTrue("the original categorization proof remains first",reason.startsWith("Original category evidence."));
		List<BankPreviewItem> physical = numeric.physicalBankSlots();
		assertEquals(2,physical.size());
		assertEquals(1,physical.get(0).getQuantity()); assertFalse(physical.get(0).isPlaceholder());
		assertEquals(0,physical.get(1).getQuantity()); assertTrue(physical.get(1).isPlaceholder());
		for (BankPreviewItem item : physical) assertEquals(reason,item.getPlacementReason());
		BankPreviewItem fallback = GearItemSorter.plan(input,GearStatsSource.NONE).getSetupRows().get(0);
		assertTrue(fallback.getPlacementReason(),fallback.getPlacementReason().contains("Reviewed order; some candidate stats are unavailable."));
		assertFalse("fallback does not fabricate a live numeric comparison",fallback.getPlacementReason().contains("Strength 3"));
		assertEquals(2,fallback.physicalBankSlotCount());
	}

	@Test public void aSpecialistSetCanSupplySparseFallbacksOrRemainWholeBesideSeparateGeneralGear()
	{
		Set<Integer> guthan = new LinkedHashSet<>(Arrays.asList(4724,4726,4728,4730));
		Map<Integer,GearStats> stats = helmStats();
		List<BankPreviewItem> sparse = items(NEITIZNOT,4724,4726,4728,4730);
		GearItemSorter.GearLayout fallback = GearItemSorter.plan(sparse,source(stats));
		assertEquals("only available body, legs and weapon may fill their primary slots",
			Arrays.asList(NEITIZNOT,4728,4730,4726),ids(fallback.getSetupRows()));
		assertEquals(Collections.singletonList(GUTHAN),ids(fallback.getTail()));
		// Independent RuneLite vectors from reported-midgame-combat-85.tsv.
		stats.put(11832,new GearStats(GearSlot.BODY,0,0,0,-15,-10,4,0,1,98,93,105,-6,133,0,0));
		stats.put(11834,new GearStats(GearSlot.LEGS,0,0,0,-21,-7,2,0,1,71,63,66,-4,93,0,0));
		stats.put(4151,new GearStats(GearSlot.WEAPON,0,82,0,0,0,82,0,0,0,0,0,0,0,0,4));
		List<BankPreviewItem> full = items(NEITIZNOT,4724,4726,4728,4730,11832,11834,4151,1127);
		GearItemSorter.GearLayout selected = GearItemSorter.plan(full,source(stats));
		assertEquals(Arrays.asList(NEITIZNOT,11832,11834,4151),ids(selected.getSetupRows()));
		List<Integer> tail = ids(GearItemSorter.bySet(selected.getTail(),source(stats)));
		int first = tail.indexOf(GUTHAN);
		assertTrue(first >= 0 && first + guthan.size() <= tail.size());
		assertEquals("the unselected complete healing family is one canonical run",guthan,
			new LinkedHashSet<>(tail.subList(first,first + guthan.size())));
		assertEquals(new HashSet<>(ids(full)),new HashSet<>(ids(GearItemSorter.layout(full,source(stats)))));
	}

	private static BankOrganizationPreview build(BankSnapshot bank,BankPreset preset,BankLayoutPlan plan,
		BankLayoutOptions options,CategoryOverrideSource choices)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,
			source(helmStats()),ItemValueSource.NONE,choices,plan,options);
	}
	private static List<Integer> combatIds(BankOrganizationPreview preview,BankLayoutPlan plan)
	{
		return ids(preview.getCategories().get(plan.destinationOf("gear")).getItems());
	}
	private static Map<Integer,Integer> column(Map<Integer,Integer> targets,int column)
	{
		Map<Integer,Integer> result = new LinkedHashMap<>();
		targets.forEach((id,target) -> { if (target % 8 == column) result.put(id,target); });
		return result;
	}
	private static int primary(List<BankPreviewItem> items,GearStatsSource source)
	{
		return GearItemSorter.plan(items,source).getSetupRows().get(0).getItemId();
	}
	private static Map<Integer,GearStats> helmStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		stats.put(NEITIZNOT,NEITIZNOT_STATS); stats.put(GUTHAN,GUTHAN_STATS);
		return stats;
	}
	private static GearStatsSource source(Map<Integer,GearStats> stats)
	{
		return id -> Optional.ofNullable(stats.get(id));
	}
	private static BankPreviewItem item(int id)
	{
		return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),1);
	}
	private static List<BankPreviewItem> items(int... ids)
	{
		return Arrays.stream(ids).mapToObj(MeleePrimaryHelmTest::item).collect(Collectors.toList());
	}
	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

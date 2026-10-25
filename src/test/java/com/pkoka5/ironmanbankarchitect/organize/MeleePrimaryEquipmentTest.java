package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

/** Independently specified all-round Melee choices, shared ownership and compatible physical targets. */
public class MeleePrimaryEquipmentTest
{
	private static final Map<Integer,GearStats> FROZEN = frozenStats();
	private static final List<Integer> GENERAL = Arrays.asList(
		26382,26384,26386,21295,31097,29801,24417,22322,22981,28307);

	@Test public void allTenPrimaryPositionsUseTheReviewedOneHandedGeneralSetup()
	{
		List<Integer> input = new ArrayList<>(GENERAL);
		input.addAll(Arrays.asList(30750,30753,30756,24419,24420,24421,22325,13239,7462,26770));
		assertEquals(GENERAL,meleeTargets(plan(items(input),FROZEN)));
		assertConserved(items(input),plan(items(input),FROZEN));
	}

	@Test public void earlyBudgetBankUsesItsOwnedFallbacksAndExportsAReviewablePreview() throws Exception
	{
		List<Integer> expected = Arrays.asList(1153,1115,1067,6568,4129,1731,1323,8844,7454,2550);
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : expected) entries.add(new BankItemSnapshot(id,1,entries.size()));
		BankSnapshot bank = new BankSnapshot(entries);
		for (BankPreset preset : Arrays.asList(BankPresets.IRONMAN,BankPresets.MAIN))
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,
				GearStatsSource.NONE,ItemValueSource.NONE,CategoryOverrideSource.NONE,plan,BankLayoutOptions.defaultFor(preset));
			assertEquals("The sparse bank keeps every owned equipment position in its compact real-cell order",
				expected,combat(preview,plan));
			assertEquals(expected.size(),preview.getPlannedItemCount());
			Map<Integer,CombatQualityReport.Fact> facts = new LinkedHashMap<>();
			for (int id : expected) facts.put(id,new CombatQualityReport.Fact(id,"M","early-"+id,0,id,"primary",null));
			String name = preset.getType()+"-early-melee-fallback";
			java.nio.file.Path output = java.nio.file.Paths.get("build/reports/combat-quality");
			new CombatQualityReport(name,bank,preview,plan,new LinkedHashSet<>(expected),false,0,false,facts).write(output);
			java.nio.file.Files.writeString(output.resolve(name+".txt"),BankBlueprintTextExporter.export(preview),StandardCharsets.UTF_8);
		}
	}

	@Test public void bodySelectionPrefersRawStrengthRatherThanDefensiveTier()
	{
		assertEquals(26384,first(FROZEN,4720,11832,26384));
		assertEquals(11832,first(FROZEN,4720,11832));
		assertEquals(26384,first(Collections.emptyMap(),4720,11832,26384));
	}

	@Test public void legsRetainRealSlashAndCrushOffenceWithoutActivatingSetEffects()
	{
		assertEquals(26386,first(FROZEN,11834,30756,24421,26386));
		assertEquals("equal Strength/accuracy is resolved by actual Prayer",24421,first(FROZEN,11834,30756,24421));
		assertEquals(30756,first(FROZEN,11834,30756));
	}

	@Test public void offensiveCapesAndGlovesRemainAheadOfDefensiveAlternatives()
	{
		assertEquals(21295,first(FROZEN,6568,6570,21295));
		assertEquals(6570,first(FROZEN,6568,6570));
		assertEquals(22981,first(FROZEN,7462,22981));
		assertEquals(22981,first(Collections.emptyMap(),7462,22981));
	}

	@Test public void bootsDistinguishOrdinaryAndPrimordialUpgradedTreads()
	{
		assertEquals(13239,first(FROZEN,11840,31088,13239));
		assertEquals(31097,first(FROZEN,11840,31088,13239,31097));
		assertEquals(31088,first(FROZEN,11840,31088));
	}

	@Test public void exactReviewedBootsHaveAFallbackWhenNameHintsAndStatsAreAbsent()
	{
		assertEquals(13239,first(Collections.emptyMap(),31088,13239));
		assertEquals(31097,first(Collections.emptyMap(),31088,13239,31097));
	}

	@Test public void allRoundNeckBandExplicitlyPrefersFuryOverTheStrongerBudgetAmulet()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		// The strength amulet has +10 Strength and no other equipment bonuses.
		stats.put(1725,new GearStats(GearSlot.NECK,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0));
		GearItemSorter.GearLayout selected = plan(items(1725,6585),stats);
		assertEquals(Collections.singletonList(6585),meleeTargets(selected));
		assertTrue("the preference must be explained as a band, not greater Strength",
			selected.getSetupRows().get(0).getPlacementReason().contains("All-round amulet preference."));
		assertEquals(29801,first(stats,1725,6585,29801));
		assertEquals(6585,first(Collections.emptyMap(),1725,6585));
	}

	@Test public void missingStatsInTheBudgetNeckBandCannotDisableCompleteAllRoundStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		// Controlled future bonuses deliberately disagree with the reviewed Torture-over-Fury fallback rank.
		stats.put(19553,controlled(GearSlot.NECK,15,1,2,0));
		stats.remove(1725);
		assertEquals(6585,first(stats,1725,6585,19553));
		stats.remove(19553);
		assertEquals("a missing candidate in the same higher band selects that whole band's reviewed order",
			19553,first(stats,1725,6585,19553));
	}

	@Test public void missingBodyStatsCannotDisableNumericComparisonForBoots()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		stats.remove(26384);
		stats.put(13239,controlled(GearSlot.FEET,2,1,0,66));
		GearItemSorter.GearLayout selected = plan(items(26384,11840,13239),stats);
		assertEquals(Arrays.asList(26384,11840),meleeTargets(selected));
	}

	@Test public void offensiveRingsStayAheadOfSupportWithoutTurningSupportIntoAlch()
	{
		GearItemSorter.GearLayout selected = plan(items(25975,26770,28307),FROZEN);
		assertEquals(Collections.singletonList(28307),meleeTargets(selected));
		assertEquals(new HashSet<>(Arrays.asList(25975,26770)),new HashSet<>(ids(selected.getTail())));
		assertEquals(26770,first(FROZEN,25975,26770));
	}

	@Test public void missingStatsCannotAdmitAnExplicitSupportRoleToTheGeneralPrimary()
	{
		for (Map<Integer,GearStats> stats : Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
		{
			GearItemSorter.GearLayout selected = plan(items(25975),stats);
			assertTrue(selected.getSetupRows().isEmpty());
			assertEquals(Collections.singletonList(25975),ids(selected.getTail()));
		}
	}

	@Test public void ghommalDefendersKeepTheAvernicPreferenceAcrossNormalAndLockedAppearances()
	{
		for (int id : new int[]{27550,27551,27552,27553})
		{
			Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
			stats.put(id,FROZEN.get(22322)); // Exact same raw bonuses; Wiki Ghommal's avernic defender 5/6.
			assertEquals(id,first(stats,12954,id));
			assertEquals(id,first(Collections.emptyMap(),12954,id));
		}
	}

	@Test public void permanentWhipAndRuneScimitarAppearancesCannotLoseToWeakerReviewedBaseGear()
	{
		for (int id : new int[]{12773,12774,26482})
		{
			Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
			stats.put(id,FROZEN.get(4151)); // Frozen, Volcanic and ornamented whips retain ordinary Whip stats.
			assertEquals(id,first(stats,4587,id));
			assertEquals(id,first(Collections.emptyMap(),4587,id));
		}
		for (int id : new int[]{12389,23330,23332,23334})
			assertEquals(id,first(Collections.emptyMap(),1331,id));
	}

	@Test public void releasedSpecialAttackOrnamentsKeepTheirHandednessAndBrokenAnchorIsNotWearable()
	{
		assertEquals(Arrays.asList(28035,12954),meleeTargets(plan(items(28035,12954),Collections.emptyMap())));
		GearItemSorter.GearLayout twoHanded = plan(items(28039,12954),Collections.emptyMap());
		assertEquals(Collections.singletonList(28039),meleeTargets(twoHanded));
		assertConserved(items(28039,12954),twoHanded);
		GearItemSorter.GearLayout broken = plan(items(10888,4587),Collections.emptyMap());
		assertEquals(Collections.singletonList(4587),meleeTargets(broken));
		assertConserved(items(10888,4587),broken);
	}

	@Test public void primordialChoiceReleasesOrdinaryTreadsForTheRangedColumn()
	{
		GearItemSorter.GearLayout before = plan(items(31088,13237),FROZEN);
		assertEquals(Collections.singletonList(31088),meleeTargets(before));
		assertEquals(Collections.singletonList(13237),column(before,1));
		GearItemSorter.GearLayout after = plan(items(31088,13237,13239),FROZEN);
		assertEquals(Collections.singletonList(13239),meleeTargets(after));
		assertEquals("released shared boots are allocated once by unchanged Ranged preferences",
			Collections.singletonList(31088),column(after,1));
		assertEquals(1,ids(after.getSetupRows()).stream().filter(id -> id == 31088).count());
	}

	@Test public void ferociousChoiceReleasesBarrowsGlovesForRanged()
	{
		assertEquals(Collections.singletonList(7462),meleeTargets(plan(items(7462),FROZEN)));
		GearItemSorter.GearLayout selected = plan(items(7462,22981),FROZEN);
		assertEquals(Collections.singletonList(22981),meleeTargets(selected));
		assertEquals(Collections.singletonList(7462),column(selected,1));
		assertEquals(1,ids(selected.getSetupRows()).stream().filter(id -> id == 7462).count());
	}

	@Test public void fixedReservationsKeepTheOtherStyleSelectionsIdentical()
	{
		List<Integer> input = new ArrayList<>(GENERAL);
		input.addAll(Arrays.asList(27235,27238,27241,28951,13237,33639,20997,21000,26235,
			21018,21021,21024,21791,13235,12002,27275,27251,31106));
		GearItemSorter.GearLayout before = plan(items(input),FROZEN);
		input.addAll(Arrays.asList(30750,30753,30756,24419,24420,24421));
		GearItemSorter.GearLayout after = plan(items(input),FROZEN);
		for (int style = 1; style <= 2; style++) assertEquals(column(before,style),column(after,style));
	}

	@Test public void reviewedMixedStyleBodyAndLegRowsDoNotStealOtherStylesCoreArmour()
	{
		List<Integer> input = Arrays.asList(26384,26386,29280,29283,26243,26245);
		GearItemSorter.GearLayout selected = plan(items(input),FROZEN);
		assertEquals(Arrays.asList(26384,26386),meleeTargets(selected));
		assertTrue("the existing Ranged body/legs pool stays populated",column(selected,1).containsAll(Arrays.asList(29280,29283)));
		assertTrue("the existing Magic body/legs pool stays populated",column(selected,2).containsAll(Arrays.asList(26243,26245)));
	}

	@Test public void generalOneHandedWeaponsPrecedeTwoHandedAndTargetSpecificSwitches()
	{
		GearItemSorter.GearLayout selected = plan(items(22325,26219,24417,22324,4151,22322),FROZEN);
		assertEquals(Arrays.asList(24417,22322),meleeTargets(selected));
		assertTrue(ids(selected.getTail()).containsAll(Arrays.asList(22325,26219,22324,4151)));
		assertEquals(4151,first(FROZEN,22325,4151));
		assertEquals("a demon-target effect is not invented for the ordinary main set",4151,
			first(FROZEN,29589,4151));
	}

	@Test public void equalVerifiedWeaponSpeedUsesActualStatsWithinTheNavigationBand()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		assertEquals(24417,first(stats,22324,24417));
		stats.put(22324,weapon(200,200,4));
		assertEquals("controlled live bonuses beat a fallback rank when both weapons remain four-tick",22324,
			first(stats,22324,24417));
	}

	@Test public void missingOrConflictingWeaponSpeedUsesOneWholeBandFallbackOrder()
	{
		for (int ticks : new int[]{0,5})
		{
			Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
			stats.put(22324,weapon(200,200,ticks));
			assertEquals("unverified speed cannot masquerade as an offensive upgrade: " + ticks,24417,
				first(stats,22324,24417));
		}
		Map<Integer,GearStats> missing = new LinkedHashMap<>(FROZEN); missing.remove(22324);
		assertEquals(24417,first(missing,22324,24417));
	}

	@Test public void onlyReviewedTopDefendersReceiveTheAllRoundShieldBand()
	{
		assertEquals(22322,first(FROZEN,11283,12954,22322));
		assertEquals(12954,first(FROZEN,11283,12954));
		assertEquals(12954,first(Collections.emptyMap(),11283,12954));
		assertEquals("lower defenders have no blanket priority over stronger shields",11283,
			first(Collections.emptyMap(),8850,11283));
	}

	@Test public void twoHandedPrimaryOmitsItsOffhandAndAOneHandedReplacementRestoresIt()
	{
		GearItemSorter.GearLayout twoHanded = plan(items(22325,22322),FROZEN);
		assertEquals(Collections.singletonList(22325),meleeTargets(twoHanded));
		assertEquals(Collections.singletonList(22322),ids(twoHanded.getTail()));
		GearItemSorter.GearLayout oneHanded = plan(items(22325,22322,4151),FROZEN);
		assertEquals(Arrays.asList(4151,22322),meleeTargets(oneHanded));
		assertEquals(Collections.singletonList(22325),ids(oneHanded.getTail()));
		Map<Integer,Integer> targets = GearItemSorter.setupTargets(oneHanded,300);
		assertEquals(Integer.valueOf(0),targets.get(4151));
		assertEquals("the compatible shield occupies the next real Melee row",Integer.valueOf(8),targets.get(22322));
	}

	@Test public void unknownWeaponHandednessIsNotAssumedButNoWeaponAllowsAnOffhand()
	{
		int unknown = 989920;
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN); stats.put(unknown,weapon(100,100,4));
		List<BankPreviewItem> input = Arrays.asList(new BankPreviewItem(unknown,"Unreviewed spear",1),item(12954));
		GearItemSorter.GearLayout selected = plan(input,stats);
		assertEquals(Collections.singletonList(unknown),meleeTargets(selected));
		assertEquals(Collections.singletonList(12954),ids(selected.getTail()));
		assertEquals(Collections.singletonList(12954),meleeTargets(plan(items(12954),FROZEN)));
	}

	@Test public void wholeUnselectedArmourFamiliesAndSpecialistWeaponsRemainAvailable()
	{
		List<Integer> input = new ArrayList<>(GENERAL);
		input.addAll(Arrays.asList(30750,30753,30756,24419,24420,24421,22325,11804,13576));
		GearItemSorter.GearLayout selected = plan(items(input),FROZEN);
		List<Integer> grouped = ids(GearItemSorter.bySet(selected.getTail(),source(FROZEN)));
		assertWholeRun(grouped,30750,30753,30756);
		assertWholeRun(grouped,24419,24420,24421);
		assertTrue(grouped.containsAll(Arrays.asList(22325,11804,13576)));
		assertConserved(items(input),selected);
	}

	@Test public void shufflesAndWithdrawnGearKeepSelectionsWithoutChangingPhysicalStacks()
	{
		List<Integer> input = new ArrayList<>(GENERAL);
		input.addAll(Arrays.asList(30750,30753,30756,24419,24420,24421,22325,13239,7462,26770));
		Random random = new Random(20261024);
		for (boolean placeholder : new boolean[]{false,true})
			for (int trial = 0; trial < 5; trial++)
			{
				Collections.shuffle(input,random);
				List<BankPreviewItem> physical = new ArrayList<>();
				for (int id : input) physical.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),
					placeholder ? 0 : 3,placeholder));
				GearItemSorter.GearLayout selected = plan(physical,FROZEN);
				assertEquals(GENERAL,meleeTargets(selected));
				assertConserved(physical,selected);
				for (BankPreviewItem chosen : selected.getSetupRows())
				{
					assertEquals(placeholder,chosen.isPlaceholder());
					assertEquals(placeholder ? 0 : 3,chosen.getQuantity());
				}
			}
	}

	@Test public void explicitSavedAndAcceptedOrdersStillOverrideTheNewWholeEquipmentProposal()
	{
		List<BankItemSnapshot> bankItems = new ArrayList<>();
		for (int id : GENERAL) bankItems.add(new BankItemSnapshot(id,1,bankItems.size()));
		BankSnapshot bank = new BankSnapshot(bankItems);
		List<Integer> reversed = new ArrayList<>(GENERAL); Collections.reverse(reversed);
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN,BankPresets.IRONMAN))
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankLayoutOptions options = BankLayoutOptions.defaultFor(preset);
			BankOrganizationPreview proposal = build(bank,preset,plan,options);
			BankOrganizationPreview manual = build(bank,preset,plan,options.withItemOrders(
				BlueprintItemOrders.EMPTY.withTab(plan.destinationOf("gear"),reversed)));
			assertEquals(reversed,combat(manual,plan));
			String kept = AcceptedBlueprint.capture(manual,"unchanged personal choices");
			assertEquals(reversed,combat(AcceptedBlueprint.apply(kept,"unchanged personal choices",proposal,plan),plan));
		}
	}

	private static BankOrganizationPreview build(BankSnapshot bank,BankPreset preset,BankLayoutPlan plan,BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,
			source(FROZEN),ItemValueSource.NONE,CategoryOverrideSource.NONE,plan,options);
	}
	private static List<Integer> combat(BankOrganizationPreview preview,BankLayoutPlan plan)
	{
		return ids(preview.getCategories().get(plan.destinationOf("gear")).getItems());
	}
	private static void assertWholeRun(List<Integer> actual,int... expected)
	{
		int start = actual.indexOf(expected[0]); assertTrue(start >= 0);
		assertEquals(Arrays.stream(expected).boxed().collect(Collectors.toList()),actual.subList(start,start + expected.length));
	}
	private static void assertConserved(List<BankPreviewItem> input,GearItemSorter.GearLayout selected)
	{
		List<BankPreviewItem> actual = new ArrayList<>(selected.getSetupRows()); actual.addAll(selected.getTail());
		assertEquals(input.size(),actual.size());
		assertEquals(new HashSet<>(ids(input)),new HashSet<>(ids(actual)));
		assertTrue(actual.stream().noneMatch(BankPreviewItem::isBlank));
		for (BankPreviewItem item : actual)
		{
			BankPreviewItem before = input.stream().filter(value -> value.getItemId() == item.getItemId()).findFirst().get();
			assertEquals(before.getQuantity(),item.getQuantity()); assertEquals(before.isPlaceholder(),item.isPlaceholder());
		}
	}
	private static int first(Map<Integer,GearStats> stats,int... ids)
	{
		return meleeTargets(plan(items(ids),stats)).get(0);
	}
	private static List<Integer> meleeTargets(GearItemSorter.GearLayout selection) { return column(selection,0); }
	private static List<Integer> column(GearItemSorter.GearLayout selection,int column)
	{
		return GearItemSorter.setupTargets(selection,1000).entrySet().stream()
			.filter(entry -> entry.getValue() % 8 == column).map(Map.Entry::getKey).collect(Collectors.toList());
	}
	private static GearItemSorter.GearLayout plan(List<BankPreviewItem> input,Map<Integer,GearStats> stats)
	{
		return GearItemSorter.plan(input,source(stats));
	}
	private static GearStatsSource source(Map<Integer,GearStats> stats) { return id -> Optional.ofNullable(stats.get(id)); }
	private static GearStats controlled(GearSlot slot,int attack,int strength,int prayer,int defence)
	{
		return new GearStats(slot,attack,attack,attack,0,0,strength,0,prayer,defence);
	}
	private static GearStats weapon(int attack,int strength,int ticks)
	{
		return new GearStats(GearSlot.WEAPON,attack,attack,attack,0,0,strength,0,0,0,0,0,0,0,0,ticks);
	}
	private static List<BankPreviewItem> items(int... ids)
	{
		return Arrays.stream(ids).mapToObj(MeleePrimaryEquipmentTest::item).collect(Collectors.toList());
	}
	private static List<BankPreviewItem> items(List<Integer> ids)
	{
		return ids.stream().map(MeleePrimaryEquipmentTest::item).collect(Collectors.toList());
	}
	private static BankPreviewItem item(int id) { return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),1); }
	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static Map<Integer,GearStats> frozenStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		for (CombatQualityReport.Fact fact : CombatQualityReport.FACTS.values()) if (fact.stats != null) stats.put(fact.id,fact.stats);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			MeleePrimaryEquipmentTest.class.getResourceAsStream("reported-midgame-combat-85.tsv")),StandardCharsets.UTF_8)))
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] row = line.split("\t",-1); int id = Integer.parseInt(row[0]);
				int[] v = Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
				stats.putIfAbsent(id,new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],
					v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
			}
		}
		catch (Exception failure) { throw new AssertionError("Cannot read independent frozen equipment data",failure); }
		return Collections.unmodifiableMap(stats);
	}
}

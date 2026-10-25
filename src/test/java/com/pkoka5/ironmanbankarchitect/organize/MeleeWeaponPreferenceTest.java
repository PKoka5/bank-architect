package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;

/** Independent examples of navigation preferences, not a target-specific damage ranking. */
public class MeleeWeaponPreferenceTest
{
	private static final Map<Integer,GearStats> FROZEN = frozenStats();
	private static final int[] ADVANCED_TWO_HANDED = {22325,25736,25739,28338,33335,34715,29796,13263};

	@Test public void advancedTwoHandedWeaponsReplaceEarlyAndIntermediateOneHandersWithoutStats()
	{
		for (int advanced : ADVANCED_TWO_HANDED)
			for (int weaker : new int[]{1321,1333,1305,4587,28810,23528,11889,22978})
				assertEquals("owned advanced weapon versus " + weaker,advanced,first(Collections.emptyMap(),weaker,advanced));
	}

	@Test public void availableFrozenStatsCannotRestoreTheWeakOneHandedBlanketPreference()
	{
		for (int advanced : new int[]{22325,28338,29796})
		{
			assertNotNull(FROZEN.get(advanced));
			assertNotNull(FROZEN.get(4587));
			assertEquals(advanced,first(FROZEN,4587,advanced));
		}
	}

	@Test public void matureGeneralOneHandersKeepTheChosenNavigationRoleAheadOfAdvancedTwoHanders()
	{
		for (Map<Integer,GearStats> stats : Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
			for (int advanced : ADVANCED_TWO_HANDED)
				for (int general : new int[]{4151,12006,13265})
					assertEquals("mature general weapon versus " + advanced,general,first(stats,advanced,general));
	}

	@Test public void budgetOnlyBanksRetainTheCompleteExplicitMaterialAndDragonProgression()
	{
		List<Integer> expected = Arrays.asList(4587,1305,1333,1331,1329,1327,1325,1323,1321);
		List<Integer> reversed = new ArrayList<>(expected); Collections.reverse(reversed);
		assertEquals(expected,ordered(Collections.emptyMap(),reversed));
		assertEquals(expected,ordered(FROZEN,reversed));
	}

	@Test public void bothFangAppearancesPrecedeWhipTentacleAndDaggerAsAnExplicitGeneralPreference()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		stats.put(27246,FROZEN.get(26219)); // The ornament changes appearance, not raw Fang bonuses.
		for (Map<Integer,GearStats> source : Arrays.asList(stats,Collections.<Integer,GearStats>emptyMap()))
			for (int fang : new int[]{26219,27246})
				for (int other : new int[]{4151,12006,13265})
					assertEquals(fang,first(source,other,fang));
	}

	@Test public void reviewedTopFastWeaponsRemainAheadOfBothFangAppearances()
	{
		for (Map<Integer,GearStats> stats : Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
			for (int top : new int[]{24417,22324,23995})
				for (int fang : new int[]{26219,27246})
					assertEquals(top,first(stats,fang,top));
	}

	@Test public void unchargedWeaponsDoNotInheritTheChargedAdvancedPreference()
	{
		for (int uncharged : new int[]{22486,25738,25741,34717})
		{
			assertEquals(1321,first(Collections.emptyMap(),uncharged,1321));
			assertEquals(4587,first(FROZEN,uncharged,4587));
		}
	}

	@Test public void specialAttackAndTargetSpecificWeaponsAreNotPromotedWithTheAdvancedBackups()
	{
		for (int specialist : new int[]{11802,11804,21003,13576,27690,13652,23987,29589,19675})
		{
			assertEquals(1321,first(Collections.emptyMap(),specialist,1321));
			assertEquals(4587,first(FROZEN,specialist,4587));
		}
	}

	@Test public void allPermutationsKeepThePreferenceChainTransitiveAcrossWeaponRoles()
	{
		List<Integer> expected = Arrays.asList(4151,22325,4587);
		for (int a : expected) for (int b : expected) for (int c : expected)
			if (a != b && a != c && b != c)
			{
				assertEquals(expected,ordered(FROZEN,Arrays.asList(a,b,c)));
				assertEquals(expected,ordered(Collections.emptyMap(),Arrays.asList(a,b,c)));
			}
	}

	@Test public void mixedBankShufflesKeepTheExplicitWholeWeaponOrder()
	{
		List<Integer> expected = Arrays.asList(24417,22324,23995,26219,27246,12006,4151,13265,
			28338,34715,22325,29796,13263,28810,4587,1305,1333,1321,22486,34717,11802);
		List<Integer> input = new ArrayList<>(expected);
		Random random = new Random(20261024);
		for (int trial = 0; trial < 12; trial++)
		{
			Collections.shuffle(input,random);
			assertEquals(expected,ordered(Collections.emptyMap(),input));
		}
	}

	@Test public void advancedTwoHandedChoiceSuppressesTheDefenderWithoutLosingPhysicalStacks()
	{
		for (Map<Integer,GearStats> stats : Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
			for (boolean placeholder : new boolean[]{false,true})
			{
				List<BankPreviewItem> input = physical(placeholder,4587,22325,12954);
				GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source(stats));
				assertEquals(Collections.singletonList(22325),melee(selected));
				assertTrue(ids(selected.getTail()).containsAll(Arrays.asList(4587,12954)));
				assertConserved(input,selected);
			}
	}

	@Test public void matureOneHandedChoiceRestoresTheDefenderWithoutLosingTheAdvancedWeapon()
	{
		for (Map<Integer,GearStats> stats : Arrays.asList(FROZEN,Collections.<Integer,GearStats>emptyMap()))
			for (boolean placeholder : new boolean[]{false,true})
			{
				List<BankPreviewItem> input = physical(placeholder,4587,22325,12954,4151);
				GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source(stats));
				assertEquals(Arrays.asList(4151,12954),melee(selected));
				assertTrue(ids(selected.getTail()).containsAll(Arrays.asList(4587,22325)));
				assertConserved(input,selected);
			}
	}

	@Test public void unrelatedOrMissingArmourStatsCannotChangeTheWeaponChoice()
	{
		List<BankPreviewItem> weapons = physical(false,4587,22325,12954);
		assertEquals(Collections.singletonList(22325),melee(GearItemSorter.plan(weapons,source(FROZEN))));
		for (boolean knownArmour : new boolean[]{true,false})
		{
			Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
			if (!knownArmour) stats.remove(26384);
			List<BankPreviewItem> input = new ArrayList<>(weapons); input.add(item(26384));
			GearItemSorter.GearLayout selected = GearItemSorter.plan(input,source(stats));
			assertTrue(melee(selected).contains(22325));
			assertFalse(melee(selected).contains(4587));
			assertFalse(melee(selected).contains(12954));
			assertConserved(input,selected);
		}
	}

	@Test public void missingStatsInAnotherWeaponPreferenceGroupDoNotDisableComparableTopWeaponStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>(FROZEN);
		stats.put(22324,new GearStats(GearSlot.WEAPON,200,200,200,0,0,200,0,0,0,0,0,0,0,0,4));
		stats.remove(26219);
		assertEquals(22324,first(stats,24417,22324,26219));
		stats.remove(24417);
		assertEquals("missing stats within the same fast-weapon group require its reviewed fallback",
			24417,first(stats,24417,22324,26219));
	}

	private static int first(Map<Integer,GearStats> stats,int... ids)
	{
		return ordered(stats,Arrays.stream(ids).boxed().collect(Collectors.toList())).get(0);
	}
	private static List<Integer> ordered(Map<Integer,GearStats> stats,List<Integer> ids)
	{
		List<BankPreviewItem> items = ids.stream().map(MeleeWeaponPreferenceTest::item).collect(Collectors.toList());
		PrimaryGearSelector.sort(8,items,source(stats),Comparator.comparingInt(BankPreviewItem::getItemId));
		return ids(items);
	}
	private static GearStatsSource source(Map<Integer,GearStats> stats) { return id -> Optional.ofNullable(stats.get(id)); }
	private static BankPreviewItem item(int id) { return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),1); }
	private static List<BankPreviewItem> physical(boolean placeholder,int... ids)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int id : ids) items.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),
			placeholder ? 0 : 7,placeholder));
		return items;
	}
	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static List<Integer> melee(GearItemSorter.GearLayout selected)
	{
		return GearItemSorter.setupTargets(selected,1000).entrySet().stream()
			.filter(entry -> entry.getValue() % 8 == 0).map(Map.Entry::getKey).collect(Collectors.toList());
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
			assertEquals(before.getQuantity(),item.getQuantity());
			assertEquals(before.isPlaceholder(),item.isPlaceholder());
		}
	}
	private static Map<Integer,GearStats> frozenStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		for (CombatQualityReport.Fact fact : CombatQualityReport.FACTS.values())
			if (fact.stats != null) stats.put(fact.id,fact.stats);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
			MeleeWeaponPreferenceTest.class.getResourceAsStream("reported-midgame-combat-85.tsv")),StandardCharsets.UTF_8)))
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

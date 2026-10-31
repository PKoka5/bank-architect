package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class EquipmentVariantRegressionTest
{
	@Test public void cosmeticCounterpartsOfReviewedItemsMustBeCoveredOrExplicitlyExcluded() throws Exception
	{
		Map<Integer,String> names = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/compiled-item-registry.tsv"), StandardCharsets.UTF_8)))
		{
			for (String row = reader.readLine(); row != null; row = reader.readLine())
				if (!row.startsWith("#") && !row.isEmpty()) {
					String[] fields = row.split("\t", -1); names.put(Integer.parseInt(fields[0]), fields[1]);
				}
		}
		// Proposed PvP Arena ornaments never released; do not infer usability from their names.
		Set<Integer> excluded = new HashSet<>(Arrays.asList(26708,26710,26714,26715,26716,26718,26719,26720));
		for (String style : Arrays.asList("melee", "ranged", "magic"))
		{
			Map<Integer,int[]> facts = PrimaryGearSelector.load(getClass().getResourceAsStream(style + "-primary-candidates.tsv"));
			Map<String,Integer> bases = new HashMap<>();
			for (int id : facts.keySet()) if (names.containsKey(id)) bases.put(names.get(id).toLowerCase(Locale.ROOT), id);
			for (Map.Entry<Integer,String> row : names.entrySet())
			{
				String plain = row.getValue().replaceFirst("(?i) \\((g|t|or|cr|b)\\)$", "").toLowerCase(Locale.ROOT);
				Integer base = bases.get(plain);
				if (base == null || plain.equals(row.getValue().toLowerCase(Locale.ROOT))) continue;
				int id = row.getKey();
				if (excluded.contains(id)) { assertFalse(facts.containsKey(id)); continue; }
				assertTrue(style + " missing counterpart " + id + " " + row.getValue(), facts.containsKey(id));
				// The ornament belongs to the fortified ward, not the unfortified item suggested by its name.
				if (id == 27253) base = 27251;
				assertArrayEquals(style + " counterpart policy " + row.getValue(), facts.get(base), facts.get(id));
			}
		}
	}

	@Test public void trimmedRuneHelmBeatsAdamantWithAndWithoutLiveStatsAndAsPlaceholder()
	{
		for (boolean placeholder : new boolean[]{false,true}) for (boolean live : new boolean[]{false,true})
		{
			List<BankPreviewItem> items = items(1161,2627);
			if (placeholder) items.set(1,new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(2627).get(),0,true));
			Map<Integer,GearStats> stats = new HashMap<>();
			if (live) { stats.put(1161,armour(GearSlot.HEAD,90)); stats.put(2627,armour(GearSlot.HEAD,140)); }
			GearItemSorter.GearLayout layout = select(items,stats);
			assertEquals(Collections.singletonList(2627),column(layout,0));
			assertEquals(placeholder,layout.getSetupRows().get(0).isPlaceholder());
			assertEquals(Collections.singletonList(1161),ids(layout.getTail()));
		}
	}

	@Test public void colouredCrossbowBeatsBudgetBowAndKeepsItsWard()
	{
		for (int weapon : new int[]{25916,25918})
			assertEquals(Arrays.asList(weapon,22002),column(select(items(861,weapon,22002),Collections.emptyMap()),1));
		assertEquals(Collections.singletonList(27253),column(select(items(25985,27253),Collections.emptyMap()),2));
	}

	@Test public void unreviewedWeaponKeepsOffhandWhileKnownTwoHanderStillExcludesIt()
	{
		Map<Integer,GearStats> stats = new HashMap<>();
		stats.put(999001,new GearStats(GearSlot.WEAPON,0,0,0,0,95,0,0,0,0));
		List<BankPreviewItem> items = items(22002); items.add(new BankPreviewItem(999001,"Unreviewed crossbow",1));
		assertEquals(Arrays.asList(999001,22002),column(select(items,stats),1));
		assertEquals(Collections.singletonList(20997),column(select(items(20997,22002),stats),1));
		assertFalse(PrimaryGearSelector.twoHanded(GearStyle.RANGED,999001));
	}

	@Test public void equallyStrongMeleePiecesPreferTheOwnedMatchingSet()
	{
		List<BankPreviewItem> bank = items(1127,2615,2617);
		Map<Integer,GearStats> stats = new HashMap<>();
		stats.put(1127,armour(GearSlot.BODY,200)); stats.put(2615,armour(GearSlot.BODY,200));
		stats.put(2617,armour(GearSlot.LEGS,150));
		for (int seed = 0; seed < 10; seed++) {
			Collections.shuffle(bank,new Random(seed));
			assertEquals(Arrays.asList(2615,2617),column(select(bank,stats),0));
		}
		stats.put(1127,new GearStats(GearSlot.BODY,0,0,0,0,0,1,0,0,200));
		assertEquals(Arrays.asList(1127,2617),column(select(bank,stats),0));
	}

	private static GearStats armour(GearSlot slot,int defence) { return new GearStats(slot,0,0,0,0,0,0,0,0,defence); }
	private static List<BankPreviewItem> items(int... ids) {
		return Arrays.stream(ids).mapToObj(id -> new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(id).get(),1)).collect(Collectors.toList());
	}
	private static GearItemSorter.GearLayout select(List<BankPreviewItem> items,Map<Integer,GearStats> stats) {
		return GearItemSorter.plan(items,id -> Optional.ofNullable(stats.get(id)));
	}
	private static List<Integer> ids(List<BankPreviewItem> items) { return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()); }
	private static List<Integer> column(GearItemSorter.GearLayout layout,int style) {
		Map<Integer,Integer> targets=GearItemSorter.setupTargets(layout,1000);
		return layout.getSetupRows().stream().filter(item -> targets.get(item.getItemId())%8==style).map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Real item identities and frozen known vectors; missing equipment vectors explicitly use metadata fallback. */
@RunWith(Parameterized.class)
public class CombatLargeConditionQualityTest
{
	// Independent, frozen real IDs: head, weapon, body, legs; each has five 100/75/50/25/0 condition IDs.
	// The gaps in condition ID ranges are intentional. No production family/catalog helper generates expectations.
	private static final int[][] BASES = {
		{4708,4710,4712,4714}, {4716,4718,4720,4722}, {4724,4726,4728,4730},
		{4732,4734,4736,4738}, {4745,4747,4749,4751}, {4753,4755,4757,4759}
	};
	private static final int[] CONDITION_STARTS = {4856,4880,4904,4928,4952,4976};
	private static final String[] FAMILIES = {"ahrim","dharok","guthan","karil","torag","verac"};
	private static final String[] ZONES = {"G","M","M","R","M","M"};
	private static final Path REPORTS = Paths.get("build/reports/combat-quality");
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.MAIN}, {BankPresetType.IRONMAN}});
	}
	public CombatLargeConditionQualityTest(BankPresetType type)
	{
		preset = BankPresets.forType(type); plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test(timeout = 30000)
	public void allSixBarrowsFamiliesAndEveryConditionStayFiniteCompleteAndDeterministic() throws IOException
	{
		Map<Integer,CombatQualityReport.Fact> facts = expandedFacts();
		List<Integer> ids = CombatQualityReport.FACTS.values().stream()
			.filter(fact -> !fact.role.equals("variant") && !fact.role.equals("space"))
			.map(fact -> fact.id).collect(Collectors.toList());
		Set<Integer> barrows = new LinkedHashSet<>();
		for (int family = 0; family < BASES.length; family++)
			for (int slot = 0; slot < 4; slot++) barrows.addAll(slotIds(family,slot));
		assertEquals("six four-slot families with six real conditions each",144,barrows.size());
		ids.removeIf(barrows::contains); ids.addAll(barrows);
		assertEquals("base102, replacing its eight original Barrows entries with all144",238,ids.size());
		Set<Integer> primary = CombatQualityReport.FACTS.values().stream()
			.filter(fact -> fact.role.equals("armour") || fact.role.equals("primary"))
			.map(fact -> fact.id).collect(Collectors.toCollection(LinkedHashSet::new));
		primary.add(13237); // Owned Pegasians fill the Ranged column after shared Treads go to Melee.
		List<Long> timings = new ArrayList<>();
		List<Integer> expected = null;
		long overallStart = System.nanoTime();
		for (int trial = 0; trial < 3; trial++)
		{
			List<Integer> order = new ArrayList<>(ids);
			if (trial == 1) Collections.shuffle(order,new Random(20261012L));
			if (trial == 2) Collections.reverse(order);
			Set<Integer> placeholders = trial == 2 ? primary : Collections.emptySet();
			BankSnapshot source = bank(order,placeholders);
			long started = System.nanoTime();
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(source,CompositeItemCatalog.DEFAULT,
				preset,id -> Optional.ofNullable(facts.get(id)).map(fact -> fact.stats),id -> 25000,
				CategoryOverrideSource.NONE,plan,BankLayoutOptions.defaultFor(preset));
			long elapsed = System.nanoTime()-started; timings.add(elapsed);
			CombatQualityReport report = new CombatQualityReport(preset.getType() + "-all-barrows-conditions-" + trial,
				source,preview,plan,primary,true,elapsed,true,facts);
			assertAllConditionSlots(report);
			report.write(REPORTS);
			assertTrue(report.name + ":\n" + String.join("\n",report.violations),report.violations.isEmpty());
			List<Integer> actual = CombatQualityReport.ids(report.combat);
			if (expected == null) expected = actual;
			else assertEquals("all conditions retain targets after shuffles and primary withdrawals",expected,actual);
		}
		long overallMs = (System.nanoTime()-overallStart)/1000000;
		Files.createDirectories(REPORTS);
		String timing = "238 real entries, all144 Barrows condition IDs. Equipment vectors absent for unspecified IDs; metadata fallback is intentional.\n"
			+ "Preview-only milliseconds: " + timings.stream().map(nanos -> Long.toString(nanos/1000000)).collect(Collectors.joining(", "))
			+ "; total test/report milliseconds: " + overallMs + "\n";
		Files.write(REPORTS.resolve(preset.getType() + "-all-barrows-conditions-timing.txt"),timing.getBytes(StandardCharsets.UTF_8));
		assertTrue("three bounded plans and report rendering must finish within a loose20second limit: " + overallMs,overallMs < 20000);
	}

	@Test
	public void repeatedPrimaryAndSecondaryPhysicalEntriesPreserveStacksAndOtherStyleGeometry()
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (CombatQualityReport.Fact fact : CombatQualityReport.FACTS.values())
			if (!fact.role.equals("variant"))
				entries.add(new BankItemSnapshot(fact.id,fact.id%3+1,entries.size()));
		entries.add(new BankItemSnapshot(26382,2,entries.size()));
		entries.add(new BankItemSnapshot(26382,0,entries.size(),true));
		entries.add(new BankItemSnapshot(26243,4,entries.size()));
		assertEquals("110 distinct entries plus two primary copies and one secondary copy",113,entries.size());
		Set<Integer> primary = CombatQualityReport.FACTS.values().stream()
			.filter(fact -> fact.role.equals("armour") || fact.role.equals("primary"))
			.map(fact -> fact.id).collect(Collectors.toCollection(LinkedHashSet::new));
		primary.add(13237);
		int frontCount = (int) entries.stream().filter(item -> primary.contains(item.getItemId())).count();
		assertEquals("primary envelope counts real physical entries",30,frontCount);
		List<Integer> expectedIds = null;
		for (int trial = 0; trial < 2; trial++)
		{
			if (trial == 1) Collections.shuffle(entries,new Random(20261012L));
			BankSnapshot source = new BankSnapshot(entries);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(source,CompositeItemCatalog.DEFAULT,
				preset,id -> Optional.ofNullable(CombatQualityReport.FACTS.get(id)).map(fact -> fact.stats),id -> 25000,
				CategoryOverrideSource.NONE,plan,BankLayoutOptions.defaultFor(preset));
			List<BankPreviewItem> planned = preview.getPlannedItems();
			assertTrue("never manufacture real blank entries",planned.stream().noneMatch(BankPreviewItem::isBlank));
			Map<String,Long> expected = source.getPhysicalItems().stream().collect(Collectors.groupingBy(
				item -> physicalKey(item.getItemId(),item.getQuantity(),item.isPlaceholder()),Collectors.counting()));
			Map<String,Long> actual = planned.stream().collect(Collectors.groupingBy(
				item -> physicalKey(item.getItemId(),item.getQuantity(),item.isPlaceholder()),Collectors.counting()));
			assertEquals("every duplicate stack and the physical placeholder survives exactly",expected,actual);
			List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
			Set<Integer> frontIds = combat.subList(0,frontCount).stream().map(BankPreviewItem::getItemId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals("compact primary envelope includes every selected gear ID",primary,frontIds);
			int previous = -1;
			for (int i = frontCount; i < combat.size(); i++)
			{
				String zone = CombatQualityReport.FACTS.get(combat.get(i).getItemId()).zone;
				int current = CombatQualityReport.ZONES.indexOf(zone);
				assertTrue("physical style zones stay contiguous after expanded front; cell " + i,current >= previous);
				previous = current;
			}
			List<Integer> ids = CombatQualityReport.ids(combat);
			int head = ids.indexOf(11826);
			assertEquals("unaffected Armadyl body keeps its column at the real section start",head+8,ids.indexOf(11828));
			assertEquals("unaffected Armadyl legs keep their column at the real section start",head+16,ids.indexOf(11830));
			if (expectedIds == null) expectedIds = ids;
			else assertEquals("duplicate physical stack input order does not shuffle gear ID targets",expectedIds,ids);
		}
	}
	private static String physicalKey(int id,int quantity,boolean placeholder)
	{
		return id + ":" + quantity + ":" + placeholder;
	}

	private static void assertAllConditionSlots(CombatQualityReport report)
	{
		List<Integer> actual = CombatQualityReport.ids(report.combat);
		for (int family = 0; family < BASES.length; family++)
		{
			List<Integer> canonicalStarts = new ArrayList<>();
			for (int slot = 0; slot < 4; slot++)
			{
				List<Integer> positions = slotIds(family,slot).stream().map(actual::indexOf).collect(Collectors.toList());
				if (positions.stream().anyMatch(index -> index < 0))
				{
					report.fail("BARROWS_CONDITION_MISSING",FAMILIES[family] + " slot " + slot + " " + positions); continue;
				}
				int low = Collections.min(positions), high = Collections.max(positions);
				canonicalStarts.add(low);
				if (high / 8 - low / 8 > 1)
					report.fail("BARROWS_CONDITION_COLUMN",FAMILIES[family] + " canonical slot " + BASES[family][slot] + " " + positions);
			}
			for (int slot = 1; slot < canonicalStarts.size(); slot++)
				if (canonicalStarts.get(slot) <= canonicalStarts.get(slot-1))
					report.fail("BARROWS_CANONICAL_ORDER",FAMILIES[family] + " " + canonicalStarts);
		}
	}
	private static Map<Integer,CombatQualityReport.Fact> expandedFacts()
	{
		Map<Integer,CombatQualityReport.Fact> result = new LinkedHashMap<>(CombatQualityReport.FACTS);
		for (int family = 0; family < BASES.length; family++)
			for (int slot = 0; slot < 4; slot++)
				for (int id : slotIds(family,slot))
				{
					CombatQualityReport.Fact captured = CombatQualityReport.FACTS.get(id);
					result.put(id,new CombatQualityReport.Fact(id,ZONES[family],FAMILIES[family],slot,BASES[family][slot],
						id == BASES[family][slot] ? "secondary" : "variant",captured == null ? null : captured.stats));
				}
		return result;
	}
	private static List<Integer> slotIds(int family,int slot)
	{
		List<Integer> ids = new ArrayList<>(); ids.add(BASES[family][slot]);
		for (int state = 0; state < 5; state++) ids.add(CONDITION_STARTS[family] + slot*6 + state);
		return ids;
	}
	private static BankSnapshot bank(List<Integer> ids,Set<Integer> placeholders)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids)
		{
			boolean placeholder = placeholders.contains(id);
			items.add(new BankItemSnapshot(id,placeholder ? 0 : id%3+1,items.size(),placeholder));
		}
		return new BankSnapshot(items);
	}
}

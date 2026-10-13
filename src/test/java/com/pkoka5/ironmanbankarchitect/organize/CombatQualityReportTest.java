package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Checks actual public-builder results against frozen expectations, not production layout helpers. */
@RunWith(Parameterized.class)
public class CombatQualityReportTest
{
	private static final Path REPORTS = Paths.get("build/reports/combat-quality");
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.MAIN}, {BankPresetType.IRONMAN}});
	}
	public CombatQualityReportTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void completeEndgameKeepsThreePrimaryColumnsThenWholeStyleSections() throws IOException
	{
		requirePass(report("endgame", baseIds(), Collections.emptySet(), primaryIds(), true));
	}

	@Test
	public void multipleConditionsBelongToTheirWearableSlotRatherThanOneLongColumn() throws IOException
	{
		List<Integer> ids = CombatQualityReport.FACTS.values().stream().filter(fact -> !fact.role.equals("space"))
			.map(fact -> fact.id).collect(Collectors.toList());
		requirePass(report("endgame-condition-variants", ids, Collections.emptySet(), primaryIds(), true));
	}

	@Test
	public void populatedRangedAndMagicSectionsRetainVerticalArmourWhenTheirOwnCellsPermitIt() throws IOException
	{
		List<Integer> ids = baseIds();
		CombatQualityReport.FACTS.values().stream().filter(fact -> fact.role.equals("space"))
			.forEach(fact -> ids.add(fact.id));
		requirePass(report("endgame-all-style-vertical-space",ids,Collections.emptySet(),primaryIds(),true));
	}

	@Test
	public void inputShuffleAndWithdrawnPlaceholdersKeepTargetsAndPreserveEveryStack() throws IOException
	{
		CombatQualityReport original = report("endgame-owned", baseIds(), Collections.emptySet(), primaryIds(), true);
		requirePass(original);
		List<Integer> expected = CombatQualityReport.ids(original.combat);
		Random random = new Random(20261012L);
		for (int trial = 0; trial < 3; trial++)
		{
			List<Integer> shuffled = baseIds();
			Collections.shuffle(shuffled, random);
			CombatQualityReport variant = report("endgame-shuffled-" + trial, shuffled,
				trial == 2 ? primaryIds() : Collections.emptySet(), primaryIds(), true);
			assertEquals("bank input order and placeholders cannot change intended gear targets",expected,
				CombatQualityReport.ids(variant.combat));
			requirePass(variant);
		}
	}

	@Test
	public void unrelatedCoinsSuppliesAndCosmeticsCannotRearrangeCombat() throws IOException
	{
		List<Integer> source = baseIds();
		CombatQualityReport original = report("endgame-before-unrelated-deposit", source,
			Collections.emptySet(),primaryIds(),true);
		source.addAll(Arrays.asList(995,6685,6654));
		CombatQualityReport deposited = report("endgame-after-unrelated-deposit", source,
			Collections.emptySet(),primaryIds(),true);
		assertEquals("unrelated deposits leave every Combat target unchanged",CombatQualityReport.ids(original.combat),
			CombatQualityReport.ids(deposited.combat));
		requirePass(original); requirePass(deposited);
	}

	@Test
	public void unequalStylePopulationsAndIncompleteFamiliesRetainSectionBoundaries() throws IOException
	{
		List<Integer> meleeHeavy = baseIds();
		meleeHeavy.removeIf(id -> {
			CombatQualityReport.Fact fact = CombatQualityReport.FACTS.get(id);
			return !isPrimary(fact) && (fact.zone.equals("R") || fact.zone.equals("G"));
		});
		meleeHeavy.removeAll(Arrays.asList(30750,24420,4759));
		requirePass(report("melee-heavy-incomplete-sets",meleeHeavy,Collections.emptySet(),primaryIds(),true));
		List<Integer> magicHeavy = baseIds();
		magicHeavy.removeIf(id -> {
			CombatQualityReport.Fact fact = CombatQualityReport.FACTS.get(id);
			return !isPrimary(fact) && (fact.zone.equals("M") || fact.zone.equals("R") || fact.zone.equals("P"));
		});
		requirePass(report("magic-heavy-no-prayer",magicHeavy,Collections.emptySet(),primaryIds(),true));
	}

	@Test
	public void sparseBanksAroundRowEdgesUseActualCellsAndExplicitCompactFamilies() throws IOException
	{
		List<Integer> sparse = Arrays.asList(30750,30753,30756,24419,24420,24421,22326,
			22327,22328,4716,4718,4720,4722,4753,4755,4757,4759);
		// The independently reviewed best owned Oathplate core and only main weapon become the compact front.
		for (int size : new int[] {7,8,9,15,16,17})
		{
			List<Integer> ids = new ArrayList<>(sparse.subList(0,size));
			Set<Integer> front = new LinkedHashSet<>(Arrays.asList(30750,30753,30756));
			// Prayer/tank family pieces may remain in an independent Prayer/shared destination, not the primary overview.
			if (size > 10) front.add(4718);
			CombatQualityReport report = report("sparse-" + size,ids,Collections.emptySet(),front,false);
			requirePass(report);
		}
	}

	@Test
	public void savedPhysicalOrderAndCategoryCorrectionsWinOverAutomaticQualityPolicy() throws IOException
	{
		List<Integer> source = baseIds();
		CombatQualityReport original = report("manual-before",source,Collections.emptySet(),primaryIds(),true);
		List<Integer> order = CombatQualityReport.ids(original.combat);
		Collections.reverse(order);
		BankLayoutOptions options = BankLayoutOptions.defaultFor(preset).withItemOrders(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("gear"),order));
		BankSnapshot bank = bank(source,Collections.emptySet());
		BankOrganizationPreview manual = build(bank,CategoryOverrideSource.NONE,options);
		assertEquals("manual physical order is preserved, even where the user chooses mixed styles",order,
			CombatQualityReport.ids(manual.getCategories().get(plan.destinationOf("gear")).getItems()));
		CombatQualityReport exempt = new CombatQualityReport(preset.getType() + "-manual-policy-exempt",bank,
			manual,plan,new LinkedHashSet<>(order),false,0,false);
		requirePass(exempt);
		UserCategoryOverrides corrections = new UserCategoryOverrides();
		corrections.put(20997,"alch"); corrections.put(6,"gear");
		BankOrganizationPreview corrected = build(bank,corrections,BankLayoutOptions.defaultFor(preset));
		assertEquals("manual Twisted bow destination wins","alch",find(corrected,20997).getLayoutTagKey());
		assertEquals("manual cannon destination wins","gear",find(corrected,6).getLayoutTagKey());
	}

	private CombatQualityReport report(String name,List<Integer> ids,Set<Integer> placeholders,
		Set<Integer> primary,boolean armourMatrix)
	{
		primary = new LinkedHashSet<>(primary);
		if (ids.contains(13237)) primary.add(13237);
		BankSnapshot bank = bank(ids,placeholders);
		long started = System.nanoTime();
		BankOrganizationPreview preview = build(bank,CategoryOverrideSource.NONE,BankLayoutOptions.defaultFor(preset));
		return new CombatQualityReport(preset.getType() + "-" + name,bank,preview,plan,primary,armourMatrix,
			System.nanoTime()-started);
	}
	private BankOrganizationPreview build(BankSnapshot bank,CategoryOverrideSource corrections,BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank,CompositeItemCatalog.DEFAULT,preset,
			id -> Optional.ofNullable(CombatQualityReport.FACTS.get(id)).map(fact -> fact.stats),
			id -> 25000,corrections,plan,options);
	}
	private static BankSnapshot bank(List<Integer> ids,Set<Integer> placeholders)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids)
		{
			boolean placeholder = placeholders.contains(id);
			items.add(new BankItemSnapshot(id,placeholder ? 0 : id % 3 + 1,items.size(),placeholder));
		}
		return new BankSnapshot(items);
	}
	private static List<Integer> baseIds()
	{
		return CombatQualityReport.FACTS.values().stream().filter(fact -> !fact.role.equals("variant") && !fact.role.equals("space"))
			.map(fact -> fact.id).collect(Collectors.toList());
	}
	private static Set<Integer> primaryIds()
	{
		return CombatQualityReport.FACTS.values().stream().filter(CombatQualityReportTest::isPrimary)
			.map(fact -> fact.id).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	private static boolean isPrimary(CombatQualityReport.Fact fact)
	{
		return fact.role.equals("armour") || fact.role.equals("primary");
	}
	private static BankPreviewItem find(BankOrganizationPreview preview,int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).findFirst()
			.orElseThrow(() -> new AssertionError("missing " + id));
	}
	private static void requirePass(CombatQualityReport report) throws IOException
	{
		report.write(REPORTS);
		assertTrue(report.name + ":\n" + String.join("\n",report.violations),report.violations.isEmpty());
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Complete Ironman recipes form rows; remaining ingredients form runs by kind. */
public class HerbloreLayoutStyleTest
{
	private static final int[] RANARR = {207, 257, 5295, 99, 231, 139, 141, 143};
	private static final BankSnapshot BANK = bank(207, 257, 3049, 2998, 231, 5295,
		99, 139, 141, 143);

	@Test
	public void recipeRowsStayWhileThePartDosesSitWithTheHerbs()
	{
		assertTrue(BankLayoutStyles.herbloreUsesRecipeRows(BankLayoutPlan.defaultFor(BankPresets.IRONMAN)));
	}

	@Test
	public void movingThePartDosesOntoThePotionsTabSwitchesToRunsByKind()
	{
		int potionsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", potionsTab);

		assertFalse(BankLayoutStyles.herbloreUsesRecipeRows(plan));
	}

	@Test
	public void movingThePartDosesToAnUnrelatedTabKeepsTheRecipeRows()
	{
		int potionsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions");
		int elsewhere = potionsTab == 9 ? 8 : 9;
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", elsewhere);

		assertTrue(BankLayoutStyles.herbloreUsesRecipeRows(plan));
	}

	@Test
	public void runsByKindKeepEachKindTogetherInHerbProgressionOrder()
	{
		int potionsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", potionsTab);
		int herbloreTab = plan.destinationOf("clean-herbs");
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, BankLayoutOptions.DEFAULTS);

		assertEquals(Arrays.asList(207, 3049, 257, 2998, 5295, 99, 231), idsOn(preview, herbloreTab));
		assertRunsInStageOrder(kindsOn(preview, herbloreTab));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void aCompleteRecipePrecedesTheIncompleteMaterials()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int herbloreTab = plan.destinationOf("clean-herbs");
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, BankLayoutOptions.DEFAULTS);

		List<Integer> expected = Arrays.stream(RANARR).boxed().collect(Collectors.toCollection(ArrayList::new));
		expected.addAll(Arrays.asList(3049, 2998));
		assertEquals(expected, idsOn(preview, herbloreTab));
		assertTrue(runCount(kindsOn(preview, herbloreTab)) >
			new LinkedHashSet<>(kindsOn(preview, herbloreTab)).size());
	}

	@Test
	public void eachMissingRecipeCellMakesTheDefaultIronmanTabUseDenseStageRuns()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int destination = plan.destinationOf("clean-herbs");
		for (int missing : RANARR)
		{
			List<BankItemSnapshot> remaining = BANK.getItems().stream()
				.filter(item -> item.getItemId() != missing).collect(Collectors.toList());
			BankOrganizationPreview preview = build(new BankSnapshot(remaining), BankPresets.IRONMAN,
				plan, CategoryOverrideSource.NONE, BankLayoutOptions.DEFAULTS);
			List<Integer> expected = new ArrayList<>(Arrays.asList(207, 3049, 257, 2998,
				5295, 99, 231, 139, 141, 143));
			expected.remove(Integer.valueOf(missing));
			assertEquals("Missing recipe ingredient " + missing, expected, idsOn(preview, destination));
			assertRunsInStageOrder(kindsOn(preview, destination));
			assertEquals(9, countItems(preview));
		}
	}

	@Test
	public void switchingStyleNeverLosesAnItem()
	{
		BankLayoutPlan rows = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankLayoutPlan runs = rows.withTagAt("potion-doses", rows.destinationOf("potions"));

		assertEquals(countItems(build(BANK, BankPresets.IRONMAN, rows, CategoryOverrideSource.NONE,
			BankLayoutOptions.DEFAULTS)), countItems(build(BANK, BankPresets.IRONMAN, runs,
			CategoryOverrideSource.NONE, BankLayoutOptions.DEFAULTS)));
	}

	@Test
	public void mainPresetKeepsDenseMaterialRunsAndPotionsOnTheirOwnTab()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.MAIN);
		BankOrganizationPreview preview = build(BANK, BankPresets.MAIN, plan,
			CategoryOverrideSource.NONE, BankLayoutOptions.defaultFor(BankPresets.MAIN));

		assertEquals(Arrays.asList(207, 3049, 257, 2998, 5295, 99, 231),
			idsOn(preview, plan.destinationOf("clean-herbs")));
		assertEquals(Arrays.asList(139, 141, 143), idsOn(preview, plan.destinationOf("potions")));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void savedPhysicalBlueprintOrderWinsOverTheHybridRecipeLayout()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int destination = plan.destinationOf("clean-herbs");
		List<Integer> chosen = Arrays.asList(2998, 143, 257, 5295, 3049, 99, 141, 207, 231, 139);
		BankLayoutOptions options = BankLayoutOptions.DEFAULTS.withItemOrders(
			BlueprintItemOrders.EMPTY.withTab(destination, chosen));
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, options);

		assertEquals(chosen, idsOn(preview, destination));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void customTagOrderWinsOverTheCompleteRecipeLayout()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagShifted("clean-herbs", -1);
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, BankLayoutOptions.DEFAULTS);

		assertEquals(Arrays.asList(5295, 257, 2998, 207, 3049, 231, 99, 139, 141, 143),
			idsOn(preview, plan.destinationOf("clean-herbs")));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void relevantSavedBlockOrderWinsOverTheCompleteRecipeLayout()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankLayoutOptions options = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("clean-herbs", Arrays.asList("item:2998", "item:257")));
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, options);

		assertEquals(Arrays.asList(207, 3049, 2998, 257, 5295, 99, 231, 139, 141, 143),
			idsOn(preview, plan.destinationOf("clean-herbs")));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void unrelatedCombatBlockOrderDoesNotDisableTheCompleteHerbloreRow()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankLayoutOptions options = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("gear", Arrays.asList("item:11832")));
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			CategoryOverrideSource.NONE, options);

		assertEquals(Arrays.stream(RANARR).boxed().collect(Collectors.toList()),
			idsOn(preview, plan.destinationOf("clean-herbs")).subList(0, 8));
		assertEquals(10, countItems(preview));
	}

	@Test
	public void manualSecondaryDestinationPreventsAForcedRecipeRow()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankOrganizationPreview preview = build(BANK, BankPresets.IRONMAN, plan,
			id -> id == 231 ? Optional.of("raw-resources") : Optional.empty(), BankLayoutOptions.DEFAULTS);

		assertEquals(Arrays.asList(207, 3049, 257, 2998, 5295, 99, 139, 141, 143),
			idsOn(preview, plan.destinationOf("clean-herbs")));
		assertEquals(Arrays.asList(231), idsOn(preview, plan.destinationOf("raw-resources")));
		assertEquals(10, countItems(preview));
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int index = 0; index < ids.length; index++) items.add(new BankItemSnapshot(ids[index], 5, index));
		return new BankSnapshot(items);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, BankPreset preset,
		BankLayoutPlan plan, CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			GearStatsSource.NONE, ItemValueSource.NONE, overrides, plan, options);
	}

	private static List<Integer> idsOn(BankOrganizationPreview preview, int destination)
	{
		return preview.getCategories().get(destination).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<String> kindsOn(BankOrganizationPreview preview, int destination)
	{
		return preview.getCategories().get(destination).getItems().stream()
			.map(item -> BankTags.tagFor("herblore", item.getSubcategory()).getKey())
			.collect(Collectors.toList());
	}

	private static void assertRunsInStageOrder(List<String> kinds)
	{
		assertEquals(kinds.toString(), new LinkedHashSet<>(kinds).size(), runCount(kinds));
		List<String> expected = Arrays.asList("grimy-herbs", "clean-herbs", "herb-seeds",
			"unfinished-potions", "secondaries", "potion-doses", "herblore-other");
		int previous = -1;
		for (String kind : new LinkedHashSet<>(kinds))
		{
			int position = expected.indexOf(kind);
			assertTrue(kind + " out of stage order in " + kinds, position > previous);
			previous = position;
		}
	}

	private static int runCount(List<String> kinds)
	{
		int runs = 0;
		String previous = null;
		for (String kind : kinds)
		{
			if (!kind.equals(previous)) { runs++; previous = kind; }
		}
		return runs;
	}

	private static int countItems(BankOrganizationPreview preview)
	{
		return preview.getCategories().stream().mapToInt(BankCategoryPreview::getItemCount).sum();
	}
}

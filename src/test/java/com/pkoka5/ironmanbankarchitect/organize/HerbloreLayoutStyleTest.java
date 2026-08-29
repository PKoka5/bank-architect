package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The Herblore tab is arranged one of two ways, and the plan is what decides
 * which: recipe rows while the part doses sit with the herbs, runs by kind once
 * the player moves them onto the tab holding the full potions.
 */
public class HerbloreLayoutStyleTest
{
	// Grimy and clean ranarr and toadflax, a secondary, a seed, an unfinished
	// potion and three prayer potion doses.
	private static final BankSnapshot BANK = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(207, 5, 0),
		new BankItemSnapshot(257, 5, 1),
		new BankItemSnapshot(3049, 4, 2),
		new BankItemSnapshot(2998, 4, 3),
		new BankItemSnapshot(231, 20, 4),
		new BankItemSnapshot(5296, 3, 5),
		new BankItemSnapshot(99, 4, 6),
		new BankItemSnapshot(139, 2, 7),
		new BankItemSnapshot(141, 2, 8),
		new BankItemSnapshot(143, 2, 9)));

	@Test
	public void recipeRowsStayWhileThePartDosesSitWithTheHerbs()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);

		assertTrue(BankLayoutStyles.herbloreUsesRecipeRows(plan));
	}

	@Test
	public void movingThePartDosesOntoThePotionsTabSwitchesToRunsByKind()
	{
		int potionsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", potionsTab);

		assertFalse(BankLayoutStyles.herbloreUsesRecipeRows(plan));
	}

	/** Moving the doses somewhere else entirely is not the same statement. */
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
	public void runsByKindKeepEachKindTogetherInChainOrder()
	{
		int potionsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", potionsTab);
		int herbloreTab = plan.destinationOf("clean-herbs");

		List<String> kinds = kindsOn(build(plan), herbloreTab);

		// Each kind appears as one unbroken run: as many runs as distinct kinds.
		assertEquals(kinds.toString(),
			new java.util.LinkedHashSet<>(kinds).size(), runCount(kinds));

		// And the runs follow the chain a herb travels, whichever kinds the bank
		// happens to hold.
		List<String> expected = Arrays.asList("grimy-herbs", "clean-herbs",
			"unfinished-potions", "secondaries", "herb-seeds", "potion-doses", "herblore-other");
		int previous = -1;
		for (String kind : new java.util.LinkedHashSet<>(kinds))
		{
			int position = expected.indexOf(kind);
			assertTrue(kind + " out of chain order in " + kinds, position > previous);
			previous = position;
		}
	}

	@Test
	public void recipeRowsInterleaveTheKindsInsteadOfRunningThem()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int herbloreTab = plan.destinationOf("clean-herbs");

		List<String> kinds = kindsOn(build(plan), herbloreTab);

		// A row per recipe puts a grimy herb next to its clean one, so the kinds
		// alternate rather than forming one run each.
		assertTrue(kinds.toString(), runCount(kinds) > new java.util.LinkedHashSet<>(kinds).size());
	}

	@Test
	public void switchingStyleNeverLosesAnItem()
	{
		BankLayoutPlan rows = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int potionsTab = rows.destinationOf("potions");
		BankLayoutPlan runs = rows.withTagAt("potion-doses", potionsTab);

		assertEquals(countItems(build(rows)), countItems(build(runs)));
	}

	private static BankOrganizationPreview build(BankLayoutPlan plan)
	{
		return BankOrganizationPreviewBuilder.build(BANK, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan);
	}

	private static List<String> kindsOn(BankOrganizationPreview preview, int destination)
	{
		List<String> kinds = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(destination).getItems())
		{
			if (!item.isBlank())
			{
				kinds.add(BankTags.tagFor("herblore", item.getSubcategory()).getKey());
			}
		}

		return kinds;
	}

	/** How many times the kind changes as the tab is read left to right. */
	private static int runCount(List<String> kinds)
	{
		int runs = 0;
		String previous = null;
		for (String kind : kinds)
		{
			if (!kind.equals(previous))
			{
				runs++;
				previous = kind;
			}
		}

		return runs;
	}

	private static int countItems(BankOrganizationPreview preview)
	{
		int count = 0;
		for (BankCategoryPreview category : preview.getCategories())
		{
			count += category.getItemCount();
		}

		return count;
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A tab holding tags of two categories reads in the player's tag order even
 * when that order alternates between the categories, provided every category
 * on the tab is laid out as a plain run. Shaped layouts keep stacking whole.
 */
public class TagInterleaveTest
{
	private static final int GRIMY_RANARR = 207;
	private static final int CLEAN_RANARR = 257;
	private static final int STRAWBERRY = 5504;
	private static final int RANARR_SEED = 5295;
	private static final int POTATO_SEED = 5318;
	private static final int RANARR_UNF = 99;
	private static final int BIRD_NEST = 5075;
	private static final int ADAMANT_FULL_HELM = 1161;
	private static final int BRONZE_ARROW = 882;
	private static final int ROTTEN_FOOD = 2959;

	// Herbs, produce, herb seed, garden seed, unfinished potion, secondary.
	private static final BankSnapshot FARM_AND_HERBS = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(GRIMY_RANARR, 5, 0),
		new BankItemSnapshot(CLEAN_RANARR, 5, 1),
		new BankItemSnapshot(STRAWBERRY, 3, 2),
		new BankItemSnapshot(RANARR_SEED, 4, 3),
		new BankItemSnapshot(POTATO_SEED, 6, 4),
		new BankItemSnapshot(RANARR_UNF, 2, 5),
		new BankItemSnapshot(BIRD_NEST, 1, 6)));

	/** Herbs, then produce, then both seed tags, then the potion tags: H F H F H. */
	private static final String WOVEN_TAB =
		"grimy-herbs+clean-herbs+produce+herb-seeds+seeds+unfinished-potions+secondaries+herblore-other";

	private static final BankLayoutOptions FARMING_AS_LIST = new BankLayoutOptions(true, true, true,
		Collections.singletonMap(BankCategorySortMode.FARMING, TabOrder.SEQUENTIAL));

	private static final BankLayoutOptions GEAR_AS_LIST = new BankLayoutOptions(true, true, true,
		Collections.emptyMap(), GearLayout.LIST, PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL,
		TeleportOrder.ALPHABETICAL);

	@Test
	public void plainRunsOfTwoCategoriesWeaveInTheTagOrder()
	{
		BankLayoutPlan plan = planWithHerbTab(WOVEN_TAB);

		assertEquals(Arrays.asList(GRIMY_RANARR, CLEAN_RANARR, STRAWBERRY, RANARR_SEED,
				POTATO_SEED, RANARR_UNF, BIRD_NEST),
			idsOn(build(FARMING_AS_LIST, plan), plan.destinationOf("grimy-herbs")));
	}

	/**
	 * A category laid out by column keeps its block whole: with the farming
	 * grid in play the tab stacks herblore then farming as it always has.
	 */
	@Test
	public void aShapedCategoryOnTheTabKeepsTheBlocksStacked()
	{
		BankLayoutPlan plan = planWithHerbTab(WOVEN_TAB);

		assertEquals(Arrays.asList(GRIMY_RANARR, CLEAN_RANARR, RANARR_SEED, RANARR_UNF, BIRD_NEST,
				POTATO_SEED, STRAWBERRY),
			idsOn(build(BankLayoutOptions.DEFAULTS, plan), plan.destinationOf("grimy-herbs")));
	}

	/**
	 * A farming item corrected onto a herblore tag is laid out by column inside
	 * the herblore block, so that block is no longer a plain run and the tab
	 * stacks.
	 */
	@Test
	public void aFarmingItemPinnedIntoTheHerbloreBlockKeepsTheBlocksStacked()
	{
		BankLayoutPlan plan = planWithHerbTab(WOVEN_TAB);
		CategoryOverrideSource potatoSeedAsSecondary = itemId ->
			itemId == POTATO_SEED ? java.util.Optional.of("secondaries") : java.util.Optional.empty();

		assertEquals(Arrays.asList(GRIMY_RANARR, CLEAN_RANARR, RANARR_SEED, RANARR_UNF, BIRD_NEST,
				POTATO_SEED, STRAWBERRY),
			idsOn(build(FARMING_AS_LIST, plan, potatoSeedAsSecondary), plan.destinationOf("grimy-herbs")));
	}

	/** A default plan's order is bookkeeping, not a statement: the blocks stack. */
	@Test
	public void theDefaultTagOrderStillStacksTheBlocks()
	{
		BankLayoutPlan plan = planWithHerbTab(
			"herb-seeds+grimy-herbs+clean-herbs+secondaries+unfinished-potions+herblore-other+seeds+produce");

		assertEquals(Arrays.asList(GRIMY_RANARR, CLEAN_RANARR, RANARR_UNF, BIRD_NEST, RANARR_SEED,
				POTATO_SEED, STRAWBERRY),
			idsOn(build(FARMING_AS_LIST, plan), plan.destinationOf("grimy-herbs")));
	}

	@Test
	public void weavingNeverLosesOrDuplicatesAnItem()
	{
		BankOrganizationPreview preview = build(FARMING_AS_LIST, planWithHerbTab(WOVEN_TAB));

		int count = 0;
		for (BankCategoryPreview category : preview.getCategories())
		{
			count += category.getItemCount();
		}
		assertEquals(FARM_AND_HERBS.getItems().size(), count);
	}

	/**
	 * Weaving is not a herblore special case: gear on its List layout weaves
	 * with cleanup just the same, while the gear grid keeps the tab stacked.
	 */
	@Test
	public void gearOnListWeavesWithAnotherCategoryAndTheGridDoesNot()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(ADAMANT_FULL_HELM, 1, 0),
			new BankItemSnapshot(ROTTEN_FOOD, 4, 1),
			new BankItemSnapshot(BRONZE_ARROW, 32, 2)));
		BankLayoutPlan plan = BankLayoutPlan.parse(BankPresets.IRONMAN, BankLayoutShareCode.decode(
			"BAv1~Gear weave~currency+frequently-used|gear+cleanup+ammunition|food+potions+potion-doses"
				+ "|runes+teleports|tools+skilling-outfits+containers|raw-resources+gems+ammo-components|"
				+ WOVEN_TAB + "|clues+cosmetics+collection-log|quest-items|boss-loot").get().getPlan());
		int gearTab = plan.destinationOf("gear");

		assertEquals(Arrays.asList(ADAMANT_FULL_HELM, ROTTEN_FOOD, BRONZE_ARROW),
			idsOn(build(bank, GEAR_AS_LIST, plan, CategoryOverrideSource.NONE), gearTab));

		List<Integer> stacked = idsOn(build(bank, BankLayoutOptions.DEFAULTS, plan, CategoryOverrideSource.NONE), gearTab);
		assertEquals(Integer.valueOf(ROTTEN_FOOD), stacked.get(2));
	}

	/** Recipe rows are a shaped layout, so a herb tab that keeps its doses stacks. */
	@Test
	public void recipeRowsKeepTheBlocksStacked()
	{
		BankLayoutPlan plan = BankLayoutPlan.parse(BankPresets.IRONMAN, BankLayoutShareCode.decode(
			"BAv1~Rows~currency+frequently-used|gear+ammunition|food+potions"
				+ "|runes+teleports|tools+skilling-outfits+containers|raw-resources+gems+ammo-components|"
				+ WOVEN_TAB + "+potion-doses|clues+cosmetics+collection-log|quest-items|boss-loot+cleanup")
			.get().getPlan());

		List<Integer> ids = idsOn(build(FARMING_AS_LIST, plan), plan.destinationOf("grimy-herbs"));

		assertEquals(Arrays.asList(POTATO_SEED, STRAWBERRY), ids.subList(5, 7));
		assertEquals(new java.util.HashSet<>(Arrays.asList(GRIMY_RANARR, CLEAN_RANARR, RANARR_SEED,
			RANARR_UNF, BIRD_NEST)), new java.util.HashSet<>(ids.subList(0, 5)));
	}

	/** Part doses stay with the potions so the herb tab is a plain run, not recipe rows. */
	private static BankLayoutPlan planWithHerbTab(String herbTab)
	{
		return BankLayoutPlan.parse(BankPresets.IRONMAN, BankLayoutShareCode.decode(
			"BAv1~Woven~currency+frequently-used|gear+ammunition|food+potions+potion-doses"
				+ "|runes+teleports|tools+skilling-outfits+containers"
				+ "|raw-resources+gems+ammo-components|" + herbTab
				+ "|clues+cosmetics+collection-log|quest-items|boss-loot+cleanup").get().getPlan());
	}

	private static BankOrganizationPreview build(BankLayoutOptions options, BankLayoutPlan plan)
	{
		return build(options, plan, CategoryOverrideSource.NONE);
	}

	private static BankOrganizationPreview build(BankLayoutOptions options, BankLayoutPlan plan,
		CategoryOverrideSource overrides)
	{
		return build(FARM_AND_HERBS, options, plan, overrides);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, BankLayoutOptions options,
		BankLayoutPlan plan, CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			overrides, plan, options);
	}

	private static List<Integer> idsOn(BankOrganizationPreview preview, int destination)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(destination).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}
		return ids;
	}
}

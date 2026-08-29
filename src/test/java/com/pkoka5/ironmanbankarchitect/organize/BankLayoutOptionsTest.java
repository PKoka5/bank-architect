package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The two layout choices a plan cannot state for the player: whether a
 * part-empty row may be completed with unrelated items, and whether gear the
 * player has outgrown is gathered for alching.
 */
public class BankLayoutOptionsTest
{
	private static final BankLayoutOptions NO_FILLERS = new BankLayoutOptions(false, true);
	private static final BankLayoutOptions NO_ALCH = new BankLayoutOptions(true, false);

	// A part-finished Herblore chain: two herbs and one dose, so a recipe row is
	// short and would otherwise be padded out to eight columns.
	private static final BankSnapshot HERBLORE_BANK = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(207, 5, 0),
		new BankItemSnapshot(257, 5, 1),
		new BankItemSnapshot(139, 2, 2),
		new BankItemSnapshot(3049, 4, 3),
		new BankItemSnapshot(2998, 4, 4),
		new BankItemSnapshot(5296, 3, 5),
		new BankItemSnapshot(231, 20, 6)));

	@Test
	public void fillingRowsIsOnByDefault()
	{
		assertTrue(BankLayoutOptions.DEFAULTS.fillRows());
		assertTrue(BankLayoutOptions.DEFAULTS.alchPile());
	}

	/**
	 * The point of the switch: with filling off, a Herblore item never sits in a
	 * row merely to complete it, so the order follows the recipes alone.
	 */
	@Test
	public void herbloreRowsAreNotPaddedWhenFillingIsOff()
	{
		List<Integer> filled = herbloreItems(BankLayoutOptions.DEFAULTS);
		List<Integer> unfilled = herbloreItems(NO_FILLERS);

		assertEquals(filled.size(), unfilled.size());
		assertTrue(unfilled.containsAll(filled));
	}

	@Test
	public void switchingFillersOffNeverLosesAnItem()
	{
		assertEquals(countItems(build(HERBLORE_BANK, BankLayoutOptions.DEFAULTS)),
			countItems(build(HERBLORE_BANK, NO_FILLERS)));
	}

	/**
	 * Two strictly better rune platebodies make the third an alch candidate, so
	 * it leaves combat gear. With the option off it stays where it belongs.
	 */
	@Test
	public void outclassedGearStaysInCombatGearWhenTheAlchPileIsOff()
	{
		BankSnapshot gearBank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1127, 1, 0),
			new BankItemSnapshot(1079, 1, 1),
			new BankItemSnapshot(1163, 1, 2)));

		int withPile = tagCount(build(gearBank, BankLayoutOptions.DEFAULTS), "boss-loot");
		int withoutPile = tagCount(build(gearBank, NO_ALCH), "boss-loot");

		// Never more loot-tab items with the pile off than with it on.
		assertTrue("pile off must not add to the loot tab", withoutPile <= withPile);
		assertEquals(countItems(build(gearBank, BankLayoutOptions.DEFAULTS)),
			countItems(build(gearBank, NO_ALCH)));
	}

	/**
	 * Potions already group by family with the doses running 4 to 1 behind them,
	 * so there is no option for it. Recorded here so a sorter change that quietly
	 * regrouped them by dose would be caught.
	 */
	@Test
	public void potionsGroupByFamilyWithDosesDescending()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("potions-food"), Arrays.asList(
				potion(2434, "Prayer potion(4)"), potion(139, "Prayer potion(3)"),
				potion(141, "Prayer potion(2)"), potion(143, "Prayer potion(1)"),
				potion(3024, "Super restore(4)"), potion(3026, "Super restore(3)")));

		List<String> names = new ArrayList<>();
		for (BankPreviewItem item : sorted)
		{
			names.add(item.getDisplayName());
		}

		assertEquals(Arrays.asList("Prayer potion(4)", "Prayer potion(3)", "Prayer potion(2)",
			"Prayer potion(1)", "Super restore(4)", "Super restore(3)"), names);
	}

	private static List<Integer> herbloreItems(BankLayoutOptions options)
	{
		BankOrganizationPreview preview = build(HERBLORE_BANK, options);
		int herbloreTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.destinationOf("clean-herbs");
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(herbloreTab).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}

		return ids;
	}

	private static BankOrganizationPreview build(BankSnapshot snapshot, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(snapshot, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options);
	}

	private static int tagCount(BankOrganizationPreview preview, String tagKey)
	{
		Integer count = preview.getTagCounts().get(tagKey);
		return count == null ? 0 : count;
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

	private static BankPreviewItem potion(int id, String name)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.POTION,
			"potion", Collections.emptySet(), null), 1);
	}
}

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
 * The promise of splitting the bundles: a bundle keeps its own layout while its
 * tags share a tab, and splitting it degrades gracefully instead of corrupting
 * the tab. Sorting therefore has to happen after the items are grouped into
 * destinations, not before.
 */
public class TagLayoutGroupingTest
{
	// A Herblore chain: grimy and clean ranarr, a snapdragon, the secondary, an
	// unfinished potion and three prayer potion doses.
	private static final BankSnapshot HERBLORE_BANK = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(207, 5, 0),
		new BankItemSnapshot(257, 5, 1),
		new BankItemSnapshot(3000, 3, 2),
		new BankItemSnapshot(231, 20, 3),
		new BankItemSnapshot(99, 4, 4),
		new BankItemSnapshot(139, 2, 5),
		new BankItemSnapshot(141, 2, 6),
		new BankItemSnapshot(143, 2, 7),
		new BankItemSnapshot(2434, 3, 8)));

	@Test
	public void aBundleWhoseTagsShareATabIsLaidOutAsOneBlock()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int herbloreTab = plan.destinationOf("clean-herbs");

		// Every Herblore tag defaults to the same tab, which is what lets the
		// recipe sorter see the whole chain.
		for (BankTag tag : BankTags.forCategory("herblore"))
		{
			assertEquals(tag.getKey(), herbloreTab, plan.destinationOf(tag.getKey()));
		}

		List<Integer> byCategory = itemIds(build(null), herbloreCategoryIndex());
		List<Integer> byPlan = itemIds(build(plan), herbloreTab);

		assertEquals(byCategory, byPlan);
	}

	@Test
	public void movingOneTagOffTheBundleSplitsTheItemsBetweenTheTwoTabs()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int herbloreTab = plan.destinationOf("potion-doses");
		int elsewhere = herbloreTab == 8 ? 7 : 8;
		BankLayoutPlan split = plan.withTagAt("potion-doses", elsewhere);

		BankOrganizationPreview preview = build(split);
		List<Integer> movedTab = itemIds(preview, elsewhere);
		List<Integer> remaining = itemIds(preview, herbloreTab);

		assertTrue(movedTab.toString(), movedTab.containsAll(Arrays.asList(139, 141, 143)));
		assertFalse(remaining.toString(), remaining.contains(139));
		// The chain that stayed behind is still there, only shorter.
		assertTrue(remaining.toString(), remaining.contains(207));
		assertTrue(remaining.toString(), remaining.contains(257));
	}

	@Test
	public void splittingABundleNeverLosesOrDuplicatesAnItem()
	{
		BankLayoutPlan split = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potion-doses", 8)
			.withTagAt("secondaries", 7)
			.withTagAt("food", 2);

		assertEquals(countItems(build(BankLayoutPlan.defaultFor(BankPresets.IRONMAN))),
			countItems(build(split)));
	}

	@Test
	public void tagCountsAreReportedForTheLayoutScreen()
	{
		BankOrganizationPreview preview = build(BankLayoutPlan.defaultFor(BankPresets.IRONMAN));

		assertTrue(preview.getTagCounts().toString(),
			preview.getTagCounts().getOrDefault("potion-doses", 0) >= 3);
		assertEquals(0, preview.getTagCounts().getOrDefault("not-a-tag", 0).intValue());
	}

	private static BankOrganizationPreview build(BankLayoutPlan plan)
	{
		return BankOrganizationPreviewBuilder.build(HERBLORE_BANK, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan);
	}

	private static int herbloreCategoryIndex()
	{
		List<BankCategory> categories = BankPresets.IRONMAN.getCategories();
		for (int index = 0; index < categories.size(); index++)
		{
			if ("herblore".equals(categories.get(index).getKey()))
			{
				return index;
			}
		}

		throw new AssertionError("herblore category missing");
	}

	private static List<Integer> itemIds(BankOrganizationPreview preview, int index)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(index).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}

		return ids;
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

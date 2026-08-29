package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A correction names a tag, because the plan places tags. Naming a category
 * could only say which bundle an item joins, leaving the subcategory to decide
 * which part of it, which is exactly the decision the player is overruling.
 */
public class TagOverrideTest
{
	// A cowhide: classified as a resource, so its own subcategory would never
	// route it anywhere near runes.
	private static final int COWHIDE = 1739;

	private static final BankSnapshot BANK = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(COWHIDE, 5, 0),
		new BankItemSnapshot(995, 100000, 1)));

	@Test
	public void aCorrectionNamingATagPutsTheItemOnThatTag()
	{
		BankOrganizationPreview preview = build(overrides("runes"));

		assertEquals(1, count(preview, "runes"));
		assertEquals(0, count(preview, "raw-resources"));
	}

	@Test
	public void theCorrectedItemLandsOnTheTabItsTagIsOn()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("runes", 6);

		BankOrganizationPreview preview = build(overrides("runes"), plan);

		assertTrue(idsOn(preview, 6).contains(COWHIDE));
	}

	/**
	 * Corrections stored before the bundles were split named a category, and must
	 * keep working: the category still decides the bundle, and the item's own
	 * subcategory decides which part of it, exactly as it used to.
	 */
	@Test
	public void aCorrectionStoredAsACategoryStillResolves()
	{
		BankOrganizationPreview preview = build(overrides("currency-utilities"));

		// Cowhide has no rune or teleport subcategory, so it falls to the
		// bundle's catch-all rather than being dropped.
		assertEquals(1, count(preview, "frequently-used"));
		assertEquals(0, count(preview, "raw-resources"));
	}

	@Test
	public void anUnknownStoredKeyLeavesTheAutomaticClassificationAlone()
	{
		BankOrganizationPreview preview = build(overrides("not-a-key"));

		assertEquals(1, count(preview, "raw-resources"));
	}

	private static UserCategoryOverrides overrides(String key)
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		overrides.put(COWHIDE, key);
		return overrides;
	}

	private static BankOrganizationPreview build(UserCategoryOverrides overrides)
	{
		return build(overrides, BankLayoutPlan.defaultFor(BankPresets.IRONMAN));
	}

	private static BankOrganizationPreview build(UserCategoryOverrides overrides,
		BankLayoutPlan plan)
	{
		return BankOrganizationPreviewBuilder.build(BANK, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE, overrides, plan);
	}

	private static int count(BankOrganizationPreview preview, String tagKey)
	{
		Integer count = preview.getTagCounts().get(tagKey);
		return count == null ? 0 : count;
	}

	private static java.util.List<Integer> idsOn(BankOrganizationPreview preview, int destination)
	{
		java.util.List<Integer> ids = new java.util.ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(destination).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}

		return Collections.unmodifiableList(ids);
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class BankTabOrderTest
{
	private static final String MAIN_KEY = "currency-utilities";

	@Test
	public void emptyOrderKeepsThePresetOrder()
	{
		assertSame(BankPresets.IRONMAN, BankTabOrder.apply(BankPresets.IRONMAN, ""));
		assertSame(BankPresets.IRONMAN, BankTabOrder.apply(BankPresets.IRONMAN, null));
		assertTrue(BankTabOrder.isDefault(BankPresets.IRONMAN, ""));
		assertEquals(keysOf(BankPresets.IRONMAN), BankTabOrder.orderedKeys(BankPresets.IRONMAN, ""));
	}

	@Test
	public void storedOrderMovesTheTabsButKeepsEveryDestination()
	{
		BankPreset reordered = BankTabOrder.apply(BankPresets.IRONMAN,
			"storage-cleanup,combat-gear");

		List<String> keys = keysOf(reordered);
		assertEquals(MAIN_KEY, keys.get(0));
		assertEquals("storage-cleanup", keys.get(1));
		assertEquals("combat-gear", keys.get(2));
		assertEquals(keysOf(BankPresets.IRONMAN).size(), keys.size());
		assertTrue(keys.containsAll(keysOf(BankPresets.IRONMAN)));
		// Reordering must not change what a destination is or how it sorts.
		assertEquals(BankPresets.IRONMAN.getCategory("combat-gear"),
			reordered.getCategory("combat-gear"));
	}

	@Test
	public void mainSectionStaysFirstEvenWhenTheStoredOrderTriesToMoveIt()
	{
		List<String> keys = BankTabOrder.orderedKeys(BankPresets.IRONMAN,
			"combat-gear," + MAIN_KEY + ",resources");

		assertEquals(MAIN_KEY, keys.get(0));
		assertEquals("combat-gear", keys.get(1));
		assertEquals("resources", keys.get(2));
		assertEquals(keysOf(BankPresets.IRONMAN).size(), keys.size());
	}

	@Test
	public void unknownKeysAreDroppedAndMissingOnesAppendedInPresetOrder()
	{
		List<String> keys = BankTabOrder.orderedKeys(BankPresets.IRONMAN,
			"resources, no-such-tab ,resources,clues-cosmetics");

		assertEquals(Arrays.asList(MAIN_KEY, "resources", "clues-cosmetics",
			"combat-gear", "potions-food", "herblore", "seeds-farming", "skilling-tools",
			"slayer-boss-loot", "storage-cleanup"), keys);
	}

	@Test
	public void serializedOrderRoundTrips()
	{
		List<String> keys = BankTabOrder.orderedKeys(BankPresets.IRONMAN, "storage-cleanup");

		assertEquals(keys, BankTabOrder.orderedKeys(BankPresets.IRONMAN,
			BankTabOrder.serialize(keys)));
		assertFalse(BankTabOrder.isDefault(BankPresets.IRONMAN, BankTabOrder.serialize(keys)));
	}

	@Test
	public void movingShiftsOneDestinationAndLeavesTheRestAlone()
	{
		List<String> keys = keysOf(BankPresets.IRONMAN);

		List<String> up = BankTabOrder.moved(keys, 3, -1);

		assertEquals(keys.get(3), up.get(2));
		assertEquals(keys.get(2), up.get(3));
		assertEquals(keys.get(1), up.get(1));
		assertEquals(keys.size(), up.size());
	}

	@Test
	public void movingRefusesToLeaveTheListOrDisplaceTheMainSection()
	{
		List<String> keys = keysOf(BankPresets.IRONMAN);

		assertEquals(keys, BankTabOrder.moved(keys, 1, -1));
		assertEquals(keys, BankTabOrder.moved(keys, keys.size() - 1, 1));
		assertEquals(keys, BankTabOrder.moved(keys, 0, 1));
		assertEquals(keys, BankTabOrder.moved(keys, -1, 1));
		assertEquals(keys, BankTabOrder.moved(keys, keys.size(), -1));
	}

	@Test
	public void destinationKeepsItsColourAfterAReorder()
	{
		BankPreset reordered = BankTabOrder.apply(BankPresets.IRONMAN, "storage-cleanup");
		List<String> keys = keysOf(reordered);

		assertEquals("storage-cleanup", keys.get(1));
		assertNotEquals(1, CategoryPalette.paletteIndex("storage-cleanup", 1));
		assertEquals(CategoryPalette.colorFor(9),
			CategoryPalette.colorForCategory("storage-cleanup", 1));
		// Colours stay unique so the legend never shows one twice.
		List<java.awt.Color> colors = new ArrayList<>();
		for (int index = 0; index < keys.size(); index++)
		{
			colors.add(CategoryPalette.colorForCategory(keys.get(index), index));
		}
		assertEquals(keys.size(), new java.util.HashSet<>(colors).size());
	}

	@Test
	public void reorderedPresetPlansTheSameItemsInTheNewTabOrder()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(995, 100000, 0),
			new BankItemSnapshot(1153, 1, 1),
			new BankItemSnapshot(2434, 3, 2)));

		BankOrganizationPreview standard = BankOrganizationPreviewBuilder.build(snapshot,
			StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);
		BankOrganizationPreview reordered = BankOrganizationPreviewBuilder.build(snapshot,
			StaticItemCatalog.INSTANCE, BankTabOrder.apply(BankPresets.IRONMAN, "storage-cleanup"));

		assertEquals("storage-cleanup",
			reordered.getCategories().get(1).getCategory().getKey());
		assertEquals(standard.getPlannedItemCount(), reordered.getPlannedItemCount());
		for (BankCategoryPreview category : standard.getCategories())
		{
			assertEquals(category.getItemCount(),
				categoryPreview(reordered, category.getCategory().getKey()).getItemCount());
		}
	}

	private static BankCategoryPreview categoryPreview(BankOrganizationPreview preview, String key)
	{
		for (BankCategoryPreview category : preview.getCategories())
		{
			if (category.getCategory().getKey().equals(key))
			{
				return category;
			}
		}

		throw new AssertionError("Missing destination: " + key);
	}

	private static List<String> keysOf(BankPreset preset)
	{
		List<String> keys = new ArrayList<>();
		for (BankCategory category : preset.getCategories())
		{
			keys.add(category.getKey());
		}

		return keys;
	}
}

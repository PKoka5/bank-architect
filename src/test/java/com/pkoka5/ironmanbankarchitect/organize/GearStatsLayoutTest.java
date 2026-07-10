package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class GearStatsLayoutTest
{
	@Test
	public void statsDriveLaneColumnsWithoutNameHints()
	{
		// Names carry no recognizable gear words; only the stats say what these are.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.WEAPON, 0, 82, 0, 0, 0, 82, 0, 0));
		stats.put(2, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 70, 0, 0, 0));
		stats.put(3, new GearStats(GearSlot.WEAPON, 0, 0, 0, 25, 0, 0, 0, 0));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(1, "Trophy of valor"),
			item(2, "Trophy of winds"),
			item(3, "Trophy of stars")
		), sourceOf(stats));

		assertEquals(3, laidOut.size());
		assertEquals("Trophy of valor", laidOut.get(0).getDisplayName());
		assertEquals("Trophy of winds", laidOut.get(1).getDisplayName());
		assertEquals("Trophy of stars", laidOut.get(2).getDisplayName());
	}

	@Test
	public void higherStatsWinTheSetupCell()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 0, 0, 12));
		stats.put(2, new GearStats(GearSlot.HEAD, 0, 0, 0, 0, 0, 0, 0, 60));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(1, "Guard's kettle"),
			item(2, "Guard's bucket")
		), sourceOf(stats));

		assertEquals("Guard's bucket", laidOut.get(0).getDisplayName());
		assertEquals("Guard's kettle", laidOut.get(1).getDisplayName());
	}

	@Test
	public void statsOverrideMisleadingNames()
	{
		// The name suggests a magic hat, but the stats say melee tank helm.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.HEAD, 0, 0, 0, -6, -3, 0, 0, 55));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(
			Collections.singletonList(item(1, "Mystic hat")), sourceOf(stats));

		assertEquals(1, laidOut.size());
		assertEquals("Mystic hat", laidOut.get(0).getDisplayName());
	}

	@Test
	public void ringsAndUnknownSlotsStayOutOfSetupLanes()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.RING, 0, 0, 0, 0, 0, 4, 0, 0));
		stats.put(2, new GearStats(GearSlot.WEAPON, 0, 60, 0, 0, 0, 55, 0, 0));

		List<BankPreviewItem> laidOut = GearItemSorter.layout(Arrays.asList(
			item(1, "Circle of dawn"),
			item(2, "Trophy of valor")
		), sourceOf(stats));

		assertEquals("Trophy of valor", laidOut.get(0).getDisplayName());
		assertEquals("Circle of dawn", laidOut.get(1).getDisplayName());
	}

	@Test
	public void styleClassificationFollowsBonuses()
	{
		assertEquals(GearStyle.MELEE, new GearStats(GearSlot.BODY, 0, 0, 0, -30, -10, 0, 0, 82).style());
		assertEquals(GearStyle.RANGED, new GearStats(GearSlot.BODY, 0, 0, 0, -15, 30, 0, 0, 55).style());
		assertEquals(GearStyle.MAGIC, new GearStats(GearSlot.BODY, 0, 0, 0, 30, -14, 0, 0, 48).style());
		assertEquals(GearStyle.RANGED, new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0).style());
		assertEquals(GearStyle.MELEE, new GearStats(GearSlot.CAPE, 1, 1, 1, 2, 2, 4, 0, 11).style());
	}

	@Test
	public void ammoRanksIntoTheRangedAmmoRow()
	{
		GearStats arrows = new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0);
		assertEquals(11, arrows.slotRank());

		GearStats staff = new GearStats(GearSlot.WEAPON, 0, 0, 10, 17, 0, 5, 0, 3);
		assertEquals(10, staff.slotRank());
	}

	@Test
	public void runeLiteSlotIndicesMapToGearSlots()
	{
		assertEquals(GearSlot.HEAD, GearSlot.fromRuneLiteSlot(0));
		assertEquals(GearSlot.WEAPON, GearSlot.fromRuneLiteSlot(3));
		assertEquals(GearSlot.LEGS, GearSlot.fromRuneLiteSlot(7));
		assertEquals(GearSlot.AMMO, GearSlot.fromRuneLiteSlot(13));
		assertNull(GearSlot.fromRuneLiteSlot(6));
		assertNull(GearSlot.fromRuneLiteSlot(99));
	}

	private static GearStatsSource sourceOf(Map<Integer, GearStats> stats)
	{
		return itemId -> Optional.ofNullable(stats.get(itemId));
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			ItemCategory.GEAR.getDisplayLabel().toLowerCase(), Collections.emptySet(), null), 1);
	}
}

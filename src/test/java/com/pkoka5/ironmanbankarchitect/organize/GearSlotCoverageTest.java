package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

public class GearSlotCoverageTest
{
	private static final GearSlot[] WEARABLE_ROWS = {
		GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.CAPE,
		GearSlot.NECK, GearSlot.SHIELD, GearSlot.HANDS, GearSlot.FEET
	};
	private static final int FIXTURE_BASE = 970000;

	@Test
	public void everyRuneLiteEquipmentSlotHasAnExplicitMapping()
	{
		Map<EquipmentInventorySlot, GearSlot> expected = new EnumMap<>(EquipmentInventorySlot.class);
		expected.put(EquipmentInventorySlot.HEAD, GearSlot.HEAD);
		expected.put(EquipmentInventorySlot.CAPE, GearSlot.CAPE);
		expected.put(EquipmentInventorySlot.AMULET, GearSlot.NECK);
		expected.put(EquipmentInventorySlot.WEAPON, GearSlot.WEAPON);
		expected.put(EquipmentInventorySlot.BODY, GearSlot.BODY);
		expected.put(EquipmentInventorySlot.SHIELD, GearSlot.SHIELD);
		expected.put(EquipmentInventorySlot.LEGS, GearSlot.LEGS);
		expected.put(EquipmentInventorySlot.GLOVES, GearSlot.HANDS);
		expected.put(EquipmentInventorySlot.BOOTS, GearSlot.FEET);
		expected.put(EquipmentInventorySlot.RING, GearSlot.RING);
		expected.put(EquipmentInventorySlot.AMMO, GearSlot.AMMO);

		EnumSet<EquipmentInventorySlot> appearanceSlots = EnumSet.of(
			EquipmentInventorySlot.ARMS, EquipmentInventorySlot.HAIR, EquipmentInventorySlot.JAW);
		EnumSet<EquipmentInventorySlot> reviewed = EnumSet.copyOf(expected.keySet());
		reviewed.addAll(appearanceSlots);
		assertEquals("new RuneLite slots require an explicit mapping review",
			EnumSet.allOf(EquipmentInventorySlot.class), reviewed);
		assertEquals("every planner slot is represented", EnumSet.allOf(GearSlot.class),
			EnumSet.copyOf(expected.values()));
		for (EquipmentInventorySlot slot : expected.keySet())
		{
			assertEquals("RuneLite slot " + slot, expected.get(slot), GearSlot.fromRuneLiteSlot(slot.getSlotIdx()));
		}
		for (EquipmentInventorySlot slot : appearanceSlots)
		{
			assertNull("appearance slot is not a bank equipment slot: " + slot,
				GearSlot.fromRuneLiteSlot(slot.getSlotIdx()));
		}
		for (int index : new int[]{-1, 6, 8, 11, 14, Integer.MAX_VALUE})
		{
			assertNull("unsupported equipment index " + index, GearSlot.fromRuneLiteSlot(index));
		}
	}

	@Test
	public void allSlotsReachBothPresetPlannersAcrossLayoutsAndPlaceholders()
	{
		Fixture fixture = new Fixture();
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			for (GearLayout layout : GearLayout.values())
			{
				for (boolean placeholders : Arrays.asList(false, true))
				{
					BankSnapshot bank = fixture.bank(placeholders);
					BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
					BankLayoutOptions options = new BankLayoutOptions(true, true, false,
						Collections.emptyMap(), layout, PotionDoseOrder.BY_FAMILY,
						RuneOrder.ELEMENTAL, TeleportOrder.ALPHABETICAL, false);
					BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
						fixture.catalog(), preset, fixture.source(), ItemValueSource.NONE,
						CategoryOverrideSource.NONE, plan, options);
					List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
					String context = preset.getType() + ", " + layout + ", placeholders=" + placeholders;
					assertPreserved(context, bank, combat);
					assertEquals(context + ", every equipment slot survives the planner",
						EnumSet.allOf(GearSlot.class), combat.stream()
							.map(item -> fixture.stats.get(item.getItemId()).getSlot())
							.collect(Collectors.toCollection(() -> EnumSet.noneOf(GearSlot.class))));
					if (layout == GearLayout.GRID_STYLES)
					{
						assertGridSlots(context, combat, fixture);
					}
				}
			}
		}
	}

	@Test
	public void declaredCatalogSlotsOverrideMisleadingNamesWithoutRuntimeStats()
	{
		String[] subcategories = {
			"head", "body", "legs", "cape", "neck", "shield", "hands", "feet", "weapon", "ring", "ammo"
		};
		List<BankPreviewItem> items = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		for (int slot = 0; slot < subcategories.length; slot++)
		{
			int itemId = FIXTURE_BASE + 300 + slot;
			items.add(new BankPreviewItem(catalogItem(itemId, "Rune ring token " + (20 - slot), subcategories[slot]), 1));
			expected.add(itemId);
		}
		Collections.reverse(items);

		assertEquals("declared slots place pieces correctly even when the names suggest rings",
			expected, ids(GearItemSorter.layout(items, GearStatsSource.NONE)));
	}

	@Test
	public void uncommonEquipmentNamesUseEverySlotWithoutRuntimeStats()
	{
		String[] names = {"Proselyte sallet", "Sunfire fanatic cuirass", "Sunfire fanatic chausses",
			"Fire cape", "Amulet of rancour", "Falador shield 3", "Zamorak bracers", "Holy sandals",
			"Scythe of vitur", "Zaryte crossbow", "Tumeken's shadow", "Berserker ring", "Holy blessing"};
		List<BankPreviewItem> items = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		for (int index = 0; index < names.length; index++)
		{
			int itemId = FIXTURE_BASE + 500 + index;
			// Test name fallback for entries already assigned to Combat, independently of catalog routing.
			items.add(new BankPreviewItem(catalogItem(itemId, names[index], "gear"), 1));
			expected.add(itemId);
		}
		Collections.reverse(items);
		assertEquals(expected, ids(GearItemSorter.dense(items, GearStatsSource.NONE)));
	}

	@Test
	public void specificPrayerGearStaysInItsColumnWhenItsNameContainsRobe()
	{
		List<BankPreviewItem> items = new ArrayList<>();
		items.add(new BankPreviewItem(catalogItem(542, "Monk's robe", "legs"), 1));
		items.add(new BankPreviewItem(catalogItem(4093, "Mystic robe bottom", "legs"), 1));
		items.add(new BankPreviewItem(catalogItem(2497, "Black d'hide chaps", "legs"), 1));
		items.add(new BankPreviewItem(catalogItem(1079, "Rune platelegs", "legs"), 1));
		for (int index = 0; index < 4; index++)
		{
			items.add(new BankPreviewItem(catalogItem(FIXTURE_BASE + 600 + index, "Signet " + index, "ring"), 1));
		}
		List<BankPreviewItem> layout = GearItemSorter.layout(items, GearStatsSource.NONE);
		assertEquals("melee, ranged, magic and prayer retain their own cells",
			Arrays.asList(1079, 2497, 4093, 542), ids(layout.subList(0, 4)));
		assertEquals(items.size(), layout.size());
	}

	@Test
	public void runtimeEquipmentSlotsOverrideWrongCatalogSlotsAndNames()
	{
		BankPreviewItem head = new BankPreviewItem(catalogItem(FIXTURE_BASE + 400, "Rune boots token", "feet"), 1);
		BankPreviewItem body = new BankPreviewItem(catalogItem(FIXTURE_BASE + 401, "Rune helm token", "head"), 1);
		GearStatsSource stats = itemId -> Optional.of(fullStats(
			itemId == head.getItemId() ? GearSlot.HEAD : GearSlot.BODY, GearStyle.MELEE, 2));

		assertEquals(Arrays.asList(head.getItemId(), body.getItemId()),
			ids(GearItemSorter.dense(Arrays.asList(body, head), stats)));
	}

	private static void assertGridSlots(String context, List<BankPreviewItem> combat, Fixture fixture)
	{
		for (int row = 0; row < WEARABLE_ROWS.length; row++)
		{
			for (int style = 0; style < GearStyle.values().length; style++)
			{
				assertEquals(context + ", " + WEARABLE_ROWS[row] + " " + GearStyle.values()[style],
					primaryId(row, style), combat.get(row * GearItemSorter.GRID_COLUMNS + style).getItemId());
			}
			for (int column = 0; column < GearItemSorter.GRID_COLUMNS; column++)
			{
				assertEquals(context + ", wearable row contains only its own slot",
					WEARABLE_ROWS[row], fixture.stats.get(combat.get(row * 8 + column).getItemId()).getSlot());
			}
		}
		for (int style = 0; style < 3; style++)
		{
			assertEquals(context + ", weapon style " + GearStyle.values()[style],
				primaryId(8, style), combat.get(64 + style).getItemId());
		}
		for (int index = 0; index < combat.size(); index++)
		{
			GearSlot slot = fixture.stats.get(combat.get(index).getItemId()).getSlot();
			if (slot == GearSlot.RING)
			{
				assertTrue(context + ", rings may fill rows but do not occupy a setup primary",
					index >= 64 && index % 8 >= 3);
			}
			assertEquals(context + ", ammunition follows all equipment at cell " + index,
				index >= 72, slot == GearSlot.AMMO);
		}
	}

	private static void assertPreserved(String context, BankSnapshot bank, List<BankPreviewItem> items)
	{
		Map<Integer, BankItemSnapshot> expected = new LinkedHashMap<>();
		for (BankItemSnapshot item : bank.getItems()) expected.put(item.getItemId(), item);
		assertEquals(context + ", every bank entry appears once", expected.size(), items.size());
		assertEquals(context + ", no missing or duplicated IDs", expected.keySet(),
			items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
		assertTrue(context + ", no invented empty cells", items.stream().noneMatch(BankPreviewItem::isBlank));
		for (BankPreviewItem item : items)
		{
			BankItemSnapshot original = expected.get(item.getItemId());
			assertEquals(context + ", quantity for " + item.getItemId(), original.getQuantity(), item.getQuantity());
			assertEquals(context + ", placeholder for " + item.getItemId(), original.isPlaceholder(), item.isPlaceholder());
		}
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static int primaryId(int row, int style)
	{
		return FIXTURE_BASE + row * 8 + style * 2 + 1;
	}

	private static CatalogItem catalogItem(int itemId, String name, String subcategory)
	{
		return new CatalogItem(itemId, name, ItemCategory.GEAR, subcategory, Collections.emptySet(), null);
	}

	// Complete synthetic vectors exercise placement; they make no claims about game equipment bonuses.
	private static GearStats fullStats(GearSlot slot, GearStyle style, int power)
	{
		return new GearStats(slot, style == GearStyle.MELEE ? power : 0, 0, 0,
			style == GearStyle.MAGIC ? power : 0, style == GearStyle.RANGED ? power : 0,
			0, 0, style == GearStyle.PRAYER ? power : 0, 1, 1, 1, 1, 1, 0, slot == GearSlot.WEAPON ? 4 : 0);
	}

	private static final class Fixture
	{
		private final Map<Integer, CatalogItem> catalogItems = new LinkedHashMap<>();
		private final Map<Integer, GearStats> stats = new LinkedHashMap<>();

		private Fixture()
		{
			for (int row = 0; row <= WEARABLE_ROWS.length; row++)
			{
				GearSlot slot = row == WEARABLE_ROWS.length ? GearSlot.WEAPON : WEARABLE_ROWS[row];
				int styles = slot == GearSlot.WEAPON ? 3 : GearStyle.values().length;
				for (int style = 0; style < styles; style++)
				{
					for (int variant = 0; variant < 2; variant++)
					{
						int itemId = primaryId(row, style) + variant;
						String name = slot == GearSlot.WEAPON && style == 0
							? "2h sword token " + itemId : "Wizard hat token " + itemId;
						add(itemId, name, fullStats(slot, GearStyle.values()[style], variant == 0 ? 20 : 10));
					}
				}
			}
			for (int index = 0; index < 2; index++)
			{
				add(FIXTURE_BASE + 100 + index, "Wizard hat ring token " + index,
					fullStats(GearSlot.RING, GearStyle.MELEE, 1));
			}
			for (int index = 0; index < 3; index++)
			{
				add(FIXTURE_BASE + 110 + index, "Wizard hat ammo token " + index,
					fullStats(GearSlot.AMMO, GearStyle.RANGED, 1));
			}
		}

		private void add(int itemId, String name, GearStats gearStats)
		{
			catalogItems.put(itemId, catalogItem(itemId, name, "gear"));
			stats.put(itemId, gearStats);
		}

		private ItemCatalog catalog()
		{
			return itemId -> Optional.ofNullable(catalogItems.get(itemId));
		}

		private GearStatsSource source()
		{
			return itemId -> Optional.ofNullable(stats.get(itemId));
		}

		private BankSnapshot bank(boolean placeholders)
		{
			List<Integer> itemIds = new ArrayList<>(catalogItems.keySet());
			Collections.reverse(itemIds);
			List<BankItemSnapshot> items = new ArrayList<>();
			for (int itemId : itemIds)
			{
				boolean placeholder = placeholders && itemId % 2 == 1;
				items.add(new BankItemSnapshot(itemId, placeholder ? 0 : 1, items.size(), placeholder));
			}
			return new BankSnapshot(items);
		}
	}
}

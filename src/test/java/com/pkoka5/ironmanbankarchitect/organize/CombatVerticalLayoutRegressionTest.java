package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class CombatVerticalLayoutRegressionTest
{
	// Complete 108-item Combat tab from the user's 2026-10-09 export. Stats below
	// are controlled fixtures for ranking/geometry, not claims about these items' live bonuses.
	private static final int[] REAL_COMBAT_IDS = {
		6, 8, 10, 12, 10828, 11832, 11834, 6570, 6585, 12954, 7462, 11840,
		26219, 26674, 2503, 2497, 10499, 31088, 25865, 3755, 4091, 4093,
		21795, 8923, 6889, 6920, 12899, 9674, 9676, 26763, 29280, 29283,
		29286, 544, 542, 11200, 3749, 10551, 21304, 8850, 1540, 1185,
		6524, 33101, 2890, 20714, 3105, 4125, 23037, 4151, 13265, 10858,
		4587, 1215, 28997, 10887, 29889, 24699, 4158, 7668, 11061, 6746,
		23985, 12788, 805, 8880, 865, 10034, 859, 855, 10033, 4827,
		847, 12658, 1381, 1385, 1383, 31115, 9084, 4675, 1393, 3053,
		25975, 26770, 2550, 6735, 21140, 2570, 12601, 11212, 892, 890,
		4793, 888, 886, 884, 882, 9243, 9143, 11875, 9142, 8882,
		9144, 28991, 31914, 31912, 2, 31908
	};
	private static final int[][] BEST_ARMOUR = {
		{10828, 11832, 11834}, {26674, 2503, 2497}, {3755, 4091, 4093}
	};
	private static final List<Integer> MIXED_HIDE = Arrays.asList(29280, 29283, 29286);
	private static final List<Integer> MONK = Arrays.asList(544, 542);
	private static final List<Integer> CANNON = Arrays.asList(6, 8, 10, 12);
	private static final List<BankPreset> PRESETS = Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN);

	@Test
	public void denseRealCombatBanksKeepBestArmourInTheFirstThreeVerticalStyleColumns()
	{
		List<BankItemSnapshot> bank = realBank(false);
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(bank, preset, controlledStats());
			List<BankPreviewItem> combat = combat(preview, preset);
			List<Integer> ids = ids(combat);
			assertTrue("fixture must exercise a bank with room for vertical setups", combat.size() >= 80);
			for (int style = 0; style < BEST_ARMOUR.length; style++)
			{
				assertVerticalColumn(preset.getType() + " primary style " + style,
					ids, integers(BEST_ARMOUR[style]), style, 0);
			}
			assertVerticalColumn(preset.getType() + " Prayer body and legs", ids,
				Arrays.asList(9674, 9676), 3, 1);
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void secondaryMixedHideAndMonkFamiliesRemainVerticalBesideTheBestGear()
	{
		for (BankPreset preset : PRESETS)
		{
			List<BankItemSnapshot> bank = realBank(false);
			BankOrganizationPreview preview = build(bank, preset, controlledStats());
			List<Integer> ids = ids(combat(preview, preset));
			assertVerticalColumn(preset.getType() + " secondary Mixed hide", ids, MIXED_HIDE);
			assertVerticalColumn(preset.getType() + " secondary Monk robes", ids, MONK);
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void aBestOwnedMixedHideBootKeepsItsSetupTargetWhileItsRemainingFamilyStaysVertical()
	{
		GearStatsSource allStats = controlledStats();
		GearStatsSource withoutStrongerBoot = id -> id == 31088 ? Optional.empty() : allStats.statsFor(id);
		for (BankPreset preset : PRESETS)
		{
			List<BankItemSnapshot> bank = realBank(false);
			BankOrganizationPreview preview = build(bank, preset, withoutStrongerBoot);
			List<Integer> ids = ids(combat(preview, preset));
			assertEquals(preset.getType() + " best boot stays in the Ranged feet row", 7 * 8 + 1, ids.indexOf(29286));
			assertVerticalColumn(preset.getType() + " remaining Mixed hide pieces", ids, MIXED_HIDE.subList(0, 2));
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void aFullCannonUsesOneFreeRowWithoutShiftingTheBestGearColumns()
	{
		for (BankPreset preset : PRESETS)
		{
			List<BankItemSnapshot> bank = realBank(false);
			BankOrganizationPreview preview = build(bank, preset, controlledStats());
			List<Integer> ids = ids(combat(preview, preset));
			int first = ids.indexOf(CANNON.get(0));
			assertTrue(preset.getType() + " missing cannon", first >= 0);
			assertEquals(preset.getType() + " canonical cannon components form one run", CANNON,
				ids.subList(first, first + CANNON.size()));
			assertEquals(preset.getType() + " cannon cannot wrap across opposite bank edges",
				first / 8, (first + CANNON.size() - 1) / 8);
			for (int style = 0; style < BEST_ARMOUR.length; style++)
				assertVerticalColumn(preset.getType() + " cannon preserves setup " + style,
					ids, integers(BEST_ARMOUR[style]), style, 0);
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void sourceBankOrderCannotChangeTheAutomaticVerticalBlueprint()
	{
		Random random = new Random(20261009L);
		for (BankPreset preset : PRESETS)
		{
			List<BankItemSnapshot> original = realBank(false);
			List<Integer> expected = ids(combat(build(original, preset, controlledStats()), preset));
			for (int trial = 0; trial < 10; trial++)
			{
				List<BankItemSnapshot> shuffled = new ArrayList<>(original);
				Collections.shuffle(shuffled, random);
				List<BankItemSnapshot> snapshots = new ArrayList<>();
				for (BankItemSnapshot item : shuffled)
					snapshots.add(new BankItemSnapshot(item.getItemId(), item.getQuantity(), snapshots.size(), item.isPlaceholder()));
				BankOrganizationPreview preview = build(snapshots, preset, controlledStats());
				assertEquals(preset.getType() + " shuffle " + trial, expected, ids(combat(preview, preset)));
				assertPreserved(snapshots, preview);
			}
		}
	}

	@Test
	public void withdrawnBandosPlaceholdersKeepTheirExactVerticalTargetsAndReturnToTheSameSlots()
	{
		for (BankPreset preset : PRESETS)
		{
			List<BankItemSnapshot> owned = realBank(false);
			List<BankItemSnapshot> withdrawn = realBank(true);
			BankOrganizationPreview before = build(owned, preset, controlledStats());
			BankOrganizationPreview placeholders = build(withdrawn, preset, controlledStats());
			BankOrganizationPreview returned = build(owned, preset, controlledStats());
			List<Integer> expected = ids(combat(before, preset));
			assertEquals(preset.getType() + " placeholder positions", expected, ids(combat(placeholders, preset)));
			assertEquals(preset.getType() + " returned positions", expected, ids(combat(returned, preset)));
			for (int id : new int[]{11832, 11834})
			{
				BankPreviewItem item = placeholders.getPlannedItems().stream()
					.filter(entry -> entry.getItemId() == id).findFirst().orElseThrow(AssertionError::new);
				assertTrue(preset.getType() + " placeholder flag " + id, item.isPlaceholder());
				assertEquals(0, item.getQuantity());
			}
			assertPreserved(withdrawn, placeholders);
		}
	}

	@Test
	public void genuinelySparseBanksKeepOnlyRealEntriesWithoutInventingVerticalPadding()
	{
		for (BankPreset preset : PRESETS)
		{
			for (boolean placeholder : Arrays.asList(false, true))
			{
				for (List<Integer> sparse : Arrays.asList(Arrays.asList(30750, 30753, 30756),
					Arrays.asList(1093, 30750, 30753, 30756)))
				{
					List<BankItemSnapshot> bank = new ArrayList<>();
					for (int id : sparse)
					{
						boolean withdrawn = placeholder && id != 1093;
						bank.add(new BankItemSnapshot(id, withdrawn ? 0 : 1, bank.size(), withdrawn));
					}
					BankOrganizationPreview preview = build(bank, preset, controlledStats());
					assertEquals(sparse.size(), combat(preview, preset).size());
					assertPreserved(bank, preview);
				}
			}
		}
	}

	private static GearStatsSource controlledStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS};
		for (int style = 0; style < BEST_ARMOUR.length; style++)
			for (int slot = 0; slot < slots.length; slot++)
				stats.put(BEST_ARMOUR[style][slot], styled(slots[slot], GearStyle.values()[style], 500));
		stats.put(9674, styled(GearSlot.BODY, GearStyle.PRAYER, 500));
		stats.put(9676, styled(GearSlot.LEGS, GearStyle.PRAYER, 500));
		stats.put(29280, styled(GearSlot.BODY, GearStyle.RANGED, 10));
		stats.put(29283, styled(GearSlot.LEGS, GearStyle.RANGED, 10));
		stats.put(29286, styled(GearSlot.FEET, GearStyle.RANGED, 10));
		// A stronger owned Ranged boot makes every Mixed hide piece secondary.
		stats.put(31088, styled(GearSlot.FEET, GearStyle.RANGED, 500));
		stats.put(544, styled(GearSlot.BODY, GearStyle.PRAYER, 5));
		stats.put(542, styled(GearSlot.LEGS, GearStyle.PRAYER, 5));
		for (int slot = 0; slot < slots.length; slot++)
			stats.put(new int[]{30750, 30753, 30756}[slot], styled(slots[slot], GearStyle.MELEE, 500));
		return id -> Optional.ofNullable(stats.get(id));
	}

	private static GearStats styled(GearSlot slot, GearStyle style, int strength)
	{
		int melee = style == GearStyle.MELEE ? strength : 0;
		int ranged = style == GearStyle.RANGED ? strength : 0;
		int magic = style == GearStyle.MAGIC ? strength : 0;
		int prayer = style == GearStyle.PRAYER ? strength : 0;
		return new GearStats(slot, melee, melee, melee, magic, ranged, melee, ranged, prayer,
			10, 10, 10, 10, 10, 0, 0);
	}

	private static List<BankItemSnapshot> realBank(boolean withdrawnBandos)
	{
		List<BankItemSnapshot> bank = new ArrayList<>();
		for (int id : REAL_COMBAT_IDS)
		{
			boolean placeholder = withdrawnBandos && (id == 11832 || id == 11834);
			int quantity = id == 892 ? 3140 : id == 2 ? 8950 : id == 10499 ? 2 : 1;
			bank.add(new BankItemSnapshot(id, placeholder ? 0 : quantity, bank.size(), placeholder));
		}
		return bank;
	}

	private static BankOrganizationPreview build(List<BankItemSnapshot> bank, BankPreset preset, GearStatsSource stats)
	{
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(bank), CompositeItemCatalog.DEFAULT, preset,
			stats, ItemValueSource.NONE, CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset));
	}

	private static List<BankPreviewItem> combat(BankOrganizationPreview preview, BankPreset preset)
	{
		return preview.getCategories().get(BankLayoutPlan.defaultFor(preset).destinationOf("gear")).getItems();
	}

	private static void assertVerticalColumn(String context, List<Integer> ids, List<Integer> family, int column, int firstRow)
	{
		for (int index = 0; index < family.size(); index++)
			assertEquals(context + " position of " + family.get(index), (firstRow + index) * 8 + column,
				ids.indexOf(family.get(index)));
	}

	private static void assertVerticalColumn(String context, List<Integer> ids, List<Integer> family)
	{
		int first = ids.indexOf(family.get(0));
		assertTrue(context + " missing first member", first >= 0);
		for (int index = 0; index < family.size(); index++)
			assertEquals(context + " must read down one physical bank column", first + index * 8,
				ids.indexOf(family.get(index)));
	}

	private static void assertPreserved(List<BankItemSnapshot> bank, BankOrganizationPreview preview)
	{
		assertEquals("No extra padding or missing bank entries", bank.size(), preview.getPlannedItemCount());
		Map<Integer, BankItemSnapshot> expected = bank.stream().collect(Collectors.toMap(BankItemSnapshot::getItemId, item -> item));
		assertEquals(expected.keySet(), preview.getPlannedItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertFalse("No virtual blank can represent a physical bank slot", item.isBlank());
			BankItemSnapshot source = expected.get(item.getItemId());
			assertEquals("Quantity of " + item.getItemId(), source.getQuantity(), item.getQuantity());
			assertEquals("Placeholder of " + item.getItemId(), source.isPlaceholder(), item.isPlaceholder());
		}
	}

	private static List<Integer> integers(int[] ids)
	{
		return Arrays.stream(ids).boxed().collect(Collectors.toList());
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

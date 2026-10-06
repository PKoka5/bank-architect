package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AlchCandidateRoutingTest
{
	private static final ItemCatalog GEAR_CATALOG = itemId -> Optional.of(new CatalogItem(itemId,
		"Gear " + itemId, ItemCategory.GEAR, "gear", Collections.emptySet(), null));
	private final BankPreset preset;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.IRONMAN}, {BankPresetType.MAIN}});
	}

	public AlchCandidateRoutingTest(BankPresetType presetType)
	{
		this.preset = BankPresets.forType(presetType);
	}

	@Test public void knownUsesProtectOrdinaryReviewedAndBulkStockFromAutomaticAlchRouting()
	{
		for (String role : Arrays.asList("quest-use", "special-attack", "skilling-outfit"))
		{
			for (int quantity : new int[]{1, 2, 25})
			{
				int id = ItemID.RUNE_PLATEBODY;
				ItemCatalog catalog = candidate -> Optional.of(new CatalogItem(candidate, "Gear " + candidate,
					ItemCategory.GEAR, "body", candidate == id ? Collections.singleton(role) : Collections.emptySet(), null));
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
					new BankItemSnapshot(99001, 1, 0), new BankItemSnapshot(99002, 1, 1),
					new BankItemSnapshot(id, quantity, 2))), catalog, preset,
					candidate -> Optional.of(meleeBody(candidate == id ? 100 : 300)), candidate -> 39000);
				assertEquals(role + " x" + quantity, 3, categoryByKey(preview, "combat-gear").getItemCount());
				assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
			}
		}
	}

	@Test
	public void reviewedClueRequiredGearNeedsAnOwnedReplacementBeforeMovingToAlch()
	{
		int id = ItemID.RUNE_PLATEBODY;
		ItemCatalog catalog = candidate -> Optional.of(new CatalogItem(candidate, "Gear " + candidate,
			ItemCategory.GEAR, "body", candidate == id ? Collections.singleton("clue-required")
				: Collections.emptySet(), null));
		for (int quantity : new int[] {1, 2, 25})
		{
			BankOrganizationPreview withoutUpgrade = BankOrganizationPreviewBuilder.build(new BankSnapshot(
				Collections.singletonList(new BankItemSnapshot(id, quantity, 0))), catalog, preset,
				candidate -> Optional.of(meleeBody(100)), candidate -> 39000);
			assertEquals(1, categoryByKey(withoutUpgrade, "combat-gear").getItemCount());
			assertEquals(0, categoryByKey(withoutUpgrade, "slayer-boss-loot").getItemCount());

			BankOrganizationPreview withUpgrade = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
				new BankItemSnapshot(99001, 1, 0), new BankItemSnapshot(id, quantity, 1))), catalog, preset,
				candidate -> Optional.of(meleeBody(candidate == id ? 100 : 300)), candidate -> 39000);
			assertEquals(1, categoryByKey(withUpgrade, "combat-gear").getItemCount());
			assertEquals(1, categoryByKey(withUpgrade, "slayer-boss-loot").getItemCount());
		}
	}

	@Test
	public void outclassedTradeableGearMovesToTheAlchTab()
	{
		// Four melee bodies: two clearly better ones exist, so the third is an
		// alch candidate; the fourth is even worse but untradeable and stays.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(200));
		stats.put(3, meleeBody(100));
		stats.put(4, meleeBody(50));
		Map<Integer, Integer> alchValues = new LinkedHashMap<>();
		alchValues.put(1, 60000);
		alchValues.put(2, 50000);
		alchValues.put(3, 39000);
		// item 4 untradeable: no alch value.

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 17, 2),
			new BankItemSnapshot(4, 1, 3)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> alchValues.getOrDefault(itemId, 0));

		BankCategoryPreview combatGear = categoryByKey(preview, "combat-gear");
		BankCategoryPreview alchTab = categoryByKey(preview, "slayer-boss-loot");

		assertEquals(3, combatGear.getItemCount());
		assertEquals(1, alchTab.getItemCount());
		assertEquals("Gear 3", alchTab.getItems().get(0).getDisplayName());
	}

	@Test
	public void gearThatOnlyLosesOnTheTierScoreStaysInCombatGear()
	{
		// Item 4 scores lowest of all four, so the tier score alone calls it
		// outclassed. Nothing owned actually beats it though: its ranged defence
		// is far above every alternative. Only item 3, which is beaten on every
		// single stat, may move.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, body(120, 120, 120, 120, 120));
		stats.put(2, body(110, 110, 110, 110, 110));
		stats.put(3, body(100, 100, 100, 100, 100));
		stats.put(4, body(0, 0, 0, 0, 480));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 3, 2),
			new BankItemSnapshot(4, 3, 3)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 39000);

		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(1, categoryByKey(preview, "slayer-boss-loot").getItemCount());
		assertEquals("Gear 3",
			categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
	}

	@Test
	public void aReviewedWeaponStaysWhenTheOwnedUpgradeLosesOnAStat()
	{
		// The whip scores far higher but loses on crush defence, so it does not
		// replace the adamant 2h on every stat. Weapons require that full proof.
		int betterWeaponId = 99_004;
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			itemId == ItemID.ADAMANT_2H_SWORD ? "Adamant 2h sword" : "Abyssal whip",
			ItemCategory.GEAR, "weapon", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(betterWeaponId, new GearStats(GearSlot.WEAPON, 100, 0, 0, 0, 0, 100, 0, 0,
			0, 0, 0, 0, 0, 0, 4));
		stats.put(ItemID.ADAMANT_2H_SWORD, new GearStats(GearSlot.WEAPON, 50, 0, 0, 0, 0, 50, 0, 0,
			0, 0, 5, 0, 0, 0, 4));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(betterWeaponId, 1, 0),
			new BankItemSnapshot(ItemID.ADAMANT_2H_SWORD, 1, 1))),
			catalog, preset, itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> itemId == ItemID.ADAMANT_2H_SWORD ? 3840 : 0);

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void singleCopyOutclassedGearNeverBecomesAnAlchCandidate()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(200));
		stats.put(3, meleeBody(100));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 1, 2)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 39000);

		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void bestAndBackupGearNeverBecomeAlchCandidates()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(200));
		Map<Integer, Integer> alchValues = new LinkedHashMap<>();
		alchValues.put(1, 60000);
		alchValues.put(2, 50000);

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> alchValues.getOrDefault(itemId, 0));

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void cheapOutclassedGearStaysInCombatGear()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(200));
		stats.put(3, meleeBody(100));
		Map<Integer, Integer> alchValues = new LinkedHashMap<>();
		alchValues.put(1, 60000);
		alchValues.put(2, 50000);
		alchValues.put(3, 300);

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 2, 2)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> alchValues.getOrDefault(itemId, 0));

		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void bulkQuantityDoesNotMakeBarrowsArmourReplaceable()
	{
		// Local canonical Wiki bonus vectors: Torva scores higher but loses on
		// stab, slash, magic defence and ranged attack. A stack is not proof of replacement.
		GearStats torva = new GearStats(GearSlot.BODY, 0, 0, 0, -18, -14, 6, 0, 1,
			117, 111, 117, -11, 142, 0, 0);
		GearStats dharok = new GearStats(GearSlot.BODY, 0, 0, 0, -30, -10, 0, 0, 0,
			122, 120, 107, -6, 132, 0, 0);
		assertFalse(torva.dominates(dharok));
		for (int quantity : new int[]{3, 8, 25})
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
				new BankItemSnapshot(26384, 1, 0), new BankItemSnapshot(4720, quantity, 1))),
				CompositeItemCatalog.DEFAULT, preset,
				itemId -> itemId == 26384 ? Optional.of(torva) : Optional.of(dharok),
				itemId -> itemId == 4720 ? 168000 : 360000);
			assertEquals("Dharok x" + quantity, 2, categoryByKey(preview, "combat-gear").getItemCount());
			assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
		}
	}

	@Test
	public void bulkStockWearablesMoveToAlchEvenBelowValueThreshold()
	{
		// 820 mithril platebodies are smithing stock, not gear, even though
		// their alch value sits below the normal threshold.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(26384, meleeBody(300));
		stats.put(ItemID.MITHRIL_PLATEBODY, meleeBody(100));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(26384, 1, 0),
			new BankItemSnapshot(ItemID.MITHRIL_PLATEBODY, 820, 1)
		)), CompositeItemCatalog.DEFAULT, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 1560);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(1, categoryByKey(preview, "slayer-boss-loot").getItemCount());
		assertEquals("Mithril platebody", categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
	}

	@Test
	public void unreviewedWearablesUseTheSameConservativeBulkThresholds()
	{
		for (int quantity : new int[] {7, 8})
		{
			for (int value : new int[] {999, 1000})
			{
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
					new BankItemSnapshot(99001, 1, 0), new BankItemSnapshot(99003, quantity, 1))),
					GEAR_CATALOG, preset, itemId -> Optional.of(meleeBody(itemId == 99001 ? 300 : 100)),
					itemId -> value);
				boolean alch = quantity == 8 && value == 1000;
				assertEquals(alch ? 1 : 0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
				assertEquals(alch ? 1 : 2, categoryByKey(preview, "combat-gear").getItemCount());
			}
		}
	}

	@Test
	public void anUnreviewedClueRequiredStackStaysInGearDespiteDominatingAlternatives()
	{
		int id = 99003;
		ItemCatalog catalog = candidate -> Optional.of(new CatalogItem(candidate, "Gear " + candidate,
			ItemCategory.GEAR, "body", candidate == id ? Collections.singleton("clue-required")
				: Collections.emptySet(), null));
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(99001, 1, 0), new BankItemSnapshot(99002, 1, 1),
			new BankItemSnapshot(id, 25, 2))), catalog, preset,
			candidate -> Optional.of(meleeBody(candidate == id ? 100 : 300)), candidate -> 39000);
		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void bulkWeaponsAndAmmoAreConsumablesAndStayInGear()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 70, 0, 0, 0, 0));
		// Chinchompa-style consumable weapon and low-tier arrows, both in bulk.
		stats.put(2, new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 40, 0, 0, 0, 0));
		stats.put(3, new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 5, 0, 0));
		stats.put(4, new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0, 0));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 296, 1),
			new BankItemSnapshot(3, 450, 2),
			new BankItemSnapshot(4, 80, 3)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 100);

		assertEquals(4, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void cheapBulkUtilityWearablesStayInGear()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(100));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 19, 1)
		)), GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 30);

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void specialAttackWeaponsAreNeverAlchCandidates()
	{
		// A stack of dragon daggers is outclassed and valuable, but spec
		// weapons keep niche value and must stay in combat gear.
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			itemId == 3 ? "Dragon dagger" : "Gear " + itemId,
			ItemCategory.GEAR, "gear", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, new GearStats(GearSlot.WEAPON, 0, 60, 0, 0, 0, 55, 0, 0, 0));
		stats.put(2, new GearStats(GearSlot.WEAPON, 0, 50, 0, 0, 0, 45, 0, 0, 0));
		stats.put(3, new GearStats(GearSlot.WEAPON, 25, 0, 0, 0, 0, 20, 0, 0, 0));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 12, 2)
		)), catalog, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 40000);

		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void reviewedDuplicateSpecialAttackWeaponStaysTogetherInGear()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			"Dragon dagger(p++)", ItemCategory.GEAR, "weapon", Collections.emptySet(), null));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(
			Collections.singletonList(new BankItemSnapshot(ItemID.DRAGON_DAGGER_P__, 2, 0))),
			catalog, preset, GearStatsSource.NONE, itemId -> 18000);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void reviewedStockWithoutABetterAlternativeStaysInGear()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			"Rune platebody", ItemCategory.GEAR, "body", Collections.emptySet(), null));

		for (int quantity : new int[] {1, 2, 25})
		{
			for (GearStatsSource stats : Arrays.asList(GearStatsSource.NONE,
				(GearStatsSource) itemId -> Optional.of(meleeBody(100))))
			{
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(
					Collections.singletonList(new BankItemSnapshot(ItemID.RUNE_PLATEBODY, quantity, 0))),
					catalog, preset, stats, itemId -> 39000);
				assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
				assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
			}
		}
	}

	@Test
	public void anEqualOwnedAlternativeDoesNotMakeAReviewedStackReplaceable()
	{
		for (int quantity : new int[] {1, 2, 25})
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
				new BankItemSnapshot(99001, 1, 0), new BankItemSnapshot(ItemID.RUNE_PLATEBODY, quantity, 1))),
				GEAR_CATALOG, preset, itemId -> Optional.of(meleeBody(100)), itemId -> 39000);
			assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
			assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
		}
	}

	@Test
	public void reviewedSingleOutclassedAdamantTwoHanderMovesToAlch()
	{
		int betterWeaponId = 99_001;
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			itemId == ItemID.ADAMANT_2H_SWORD ? "Adamant 2h sword" : "Abyssal whip",
			ItemCategory.GEAR, "weapon", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(betterWeaponId,
			new GearStats(GearSlot.WEAPON, 100, 0, 0, 0, 0, 100, 0, 0,
				0, 0, 0, 0, 0, 0, 4));
		stats.put(ItemID.ADAMANT_2H_SWORD,
			new GearStats(GearSlot.WEAPON, 50, 0, 0, 0, 0, 50, 0, 0,
				0, 0, 0, 0, 0, 0, 4));
		assertTrue(stats.get(betterWeaponId).dominates(stats.get(ItemID.ADAMANT_2H_SWORD)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(betterWeaponId, 1, 0),
			new BankItemSnapshot(ItemID.ADAMANT_2H_SWORD, 1, 1))),
			catalog, preset, itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> itemId == ItemID.ADAMANT_2H_SWORD ? 3840 : 0);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals("Adamant 2h sword",
			categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
	}

	@Test
	public void reviewedSingleSpecialAttackWeaponStaysInGear()
	{
		int betterWeaponId = 99_002;
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			itemId == ItemID.DRAGON_DAGGER ? "Dragon dagger" : "Abyssal whip",
			ItemCategory.GEAR, "weapon", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(betterWeaponId,
			new GearStats(GearSlot.WEAPON, 100, 0, 0, 0, 0, 100, 0, 0,
				0, 0, 0, 0, 0, 0, 4));
		stats.put(ItemID.DRAGON_DAGGER,
			new GearStats(GearSlot.WEAPON, 25, 0, 0, 0, 0, 20, 0, 0,
				0, 0, 0, 0, 0, 0, 4));
		assertTrue(stats.get(betterWeaponId).dominates(stats.get(ItemID.DRAGON_DAGGER)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(betterWeaponId, 1, 0),
			new BankItemSnapshot(ItemID.DRAGON_DAGGER, 1, 1))),
			catalog, preset, itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> itemId == ItemID.DRAGON_DAGGER ? 18000 : 0);

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void reviewedDuplicateWithoutARealAlchValueStaysInGear()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			"Mystic robe top", ItemCategory.GEAR, "body", Collections.emptySet(), null));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(
			Collections.singletonList(new BankItemSnapshot(ItemID.MYSTIC_ROBE_TOP, 2, 0))),
			catalog, preset, GearStatsSource.NONE, ItemValueSource.NONE);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void bulkStockWithoutABetterAlternativeStaysInGear()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(
			Collections.singletonList(new BankItemSnapshot(1, 40, 0))),
			GEAR_CATALOG, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 1560);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void reviewedArmourMovesWithAnOwnedHigherCuratedTierDespiteWeakerDefence()
	{
		Map<Integer, String> names = new LinkedHashMap<>();
		names.put(11832, "Bandos chestplate");
		names.put(10551, "Fighter torso");
		names.put(ItemID.RUNE_PLATEBODY, "Rune platebody");
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, names.get(itemId),
			ItemCategory.GEAR, "body", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(11832, meleeBody(250));
		stats.put(10551, new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 4, 0, 0, 100));
		// The exact Bandos ID supplies a reviewed armour upgrade even when the
		// owned primary/backup alternatives do not dominate raw defence.
		stats.put(ItemID.RUNE_PLATEBODY, meleeBody(308));
		assertFalse(stats.get(11832).dominates(stats.get(ItemID.RUNE_PLATEBODY)));
		assertFalse(stats.get(10551).dominates(stats.get(ItemID.RUNE_PLATEBODY)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(11832, 1, 0),
			new BankItemSnapshot(10551, 1, 1),
			new BankItemSnapshot(ItemID.RUNE_PLATEBODY, 25, 2)
		)), catalog, preset,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> itemId == ItemID.RUNE_PLATEBODY ? 39000 : 0);

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals("Rune platebody",
			categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
	}

	/**
	 * Body armour whose defence is spread evenly over the five defence types,
	 * so the total matches {@code defence} exactly while the item still carries
	 * a full, comparable stat vector.
	 */
	/** Body armour with each defence type set individually. */
	private static GearStats body(int stab, int slash, int crush, int magic, int ranged)
	{
		return new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			stab, slash, crush, magic, ranged, 0, 0);
	}

	private static GearStats meleeBody(int defence)
	{
		int perType = defence / 5;
		int remainder = defence - perType * 4;
		return new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0,
			perType, perType, perType, perType, remainder, 0, 0);
	}

	private static BankCategoryPreview categoryByKey(BankOrganizationPreview preview, String key)
	{
		List<BankCategoryPreview> categories = preview.getCategories();
		for (BankCategoryPreview category : categories)
		{
			if (key.equals(category.getCategory().getKey()))
			{
				return category;
			}
		}

		throw new AssertionError("category " + key + " not found");
	}
}

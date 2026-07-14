package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class AlchCandidateRoutingTest
{
	private static final ItemCatalog GEAR_CATALOG = itemId -> Optional.of(new CatalogItem(itemId,
		"Gear " + itemId, ItemCategory.GEAR, "gear", Collections.emptySet(), null));

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
		)), GEAR_CATALOG, BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> alchValues.getOrDefault(itemId, 0));

		BankCategoryPreview combatGear = categoryByKey(preview, "combat-gear");
		BankCategoryPreview alchTab = categoryByKey(preview, "slayer-boss-loot");

		assertEquals(3, combatGear.getItemCount());
		assertEquals(1, alchTab.getItemCount());
		assertEquals("Gear 3", alchTab.getItems().get(0).getDisplayName());
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
		)), GEAR_CATALOG, BankPresets.IRONMAN,
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
		)), GEAR_CATALOG, BankPresets.IRONMAN,
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
			new BankItemSnapshot(3, 1, 2)
		)), GEAR_CATALOG, BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> alchValues.getOrDefault(itemId, 0));

		assertEquals(3, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void bulkStockWearablesMoveToAlchEvenBelowValueThreshold()
	{
		// 820 mithril platebodies are smithing stock, not gear, even though
		// their alch value sits below the normal threshold.
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(300));
		stats.put(2, meleeBody(100));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 820, 1)
		)), GEAR_CATALOG, BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 1560);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(1, categoryByKey(preview, "slayer-boss-loot").getItemCount());
		assertEquals("Gear 2", categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
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
		)), GEAR_CATALOG, BankPresets.IRONMAN,
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
		)), GEAR_CATALOG, BankPresets.IRONMAN,
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
		)), catalog, BankPresets.IRONMAN,
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
			catalog, BankPresets.IRONMAN, GearStatsSource.NONE, itemId -> 18000);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void reviewedSingleCopyWithoutABetterAlternativeStaysInGear()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			"Rune platebody", ItemCategory.GEAR, "body", Collections.emptySet(), null));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(
			Collections.singletonList(new BankItemSnapshot(ItemID.RUNE_PLATEBODY, 1, 0))),
			catalog, BankPresets.IRONMAN, GearStatsSource.NONE, itemId -> 39000);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
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
			new GearStats(GearSlot.WEAPON, 100, 0, 0, 0, 0, 100, 0, 0, 0));
		stats.put(ItemID.ADAMANT_2H_SWORD,
			new GearStats(GearSlot.WEAPON, 50, 0, 0, 0, 0, 50, 0, 0, 0));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(betterWeaponId, 1, 0),
			new BankItemSnapshot(ItemID.ADAMANT_2H_SWORD, 1, 1))),
			catalog, BankPresets.IRONMAN, itemId -> Optional.ofNullable(stats.get(itemId)),
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
			new GearStats(GearSlot.WEAPON, 100, 0, 0, 0, 0, 100, 0, 0, 0));
		stats.put(ItemID.DRAGON_DAGGER,
			new GearStats(GearSlot.WEAPON, 25, 0, 0, 0, 0, 20, 0, 0, 0));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(betterWeaponId, 1, 0),
			new BankItemSnapshot(ItemID.DRAGON_DAGGER, 1, 1))),
			catalog, BankPresets.IRONMAN, itemId -> Optional.ofNullable(stats.get(itemId)),
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
			catalog, BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE);

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
			GEAR_CATALOG, BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> 1560);

		assertEquals(1, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals(0, categoryByKey(preview, "slayer-boss-loot").getItemCount());
	}

	@Test
	public void alchDecisionUsesTheSameSemanticTiersAsGearLayout()
	{
		Map<Integer, String> names = new LinkedHashMap<>();
		names.put(1, "Bandos chestplate");
		names.put(2, "Fighter torso");
		names.put(3, "Rune platebody");
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, names.get(itemId),
			ItemCategory.GEAR, "body", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1, meleeBody(250));
		stats.put(2, new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 4, 0, 0, 100));
		// Rune has more raw defence than either alternative; semantic tiers must
		// still recognize Bandos and torso as the owned primary + backup.
		stats.put(3, meleeBody(308));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 0),
			new BankItemSnapshot(2, 1, 1),
			new BankItemSnapshot(3, 25, 2)
		)), catalog, BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId)),
			itemId -> itemId == 3 ? 39000 : 0);

		assertEquals(2, categoryByKey(preview, "combat-gear").getItemCount());
		assertEquals("Rune platebody",
			categoryByKey(preview, "slayer-boss-loot").getItems().get(0).getDisplayName());
	}

	private static GearStats meleeBody(int defence)
	{
		return new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, defence);
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

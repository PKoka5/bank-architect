package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class BankOrganizationPreviewBuilderTest
{
	@Test
	public void previewKeepsPresetOrderAndOwnedItemSamples()
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 3, 0),
			new BankItemSnapshot(209, 2, 1),
			new BankItemSnapshot(145, 1, 2),
			new BankItemSnapshot(999999, 1, 3)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		assertEquals(BankPresets.IRONMAN, preview.getPreset());
		assertEquals(10, preview.getCategories().size());
		assertEquals("currency-utilities", preview.getCategories().get(0).getCategory().getKey());
		assertEquals("teleports-runes", preview.getCategories().get(1).getCategory().getKey());
		assertEquals("farming-herblore", preview.getCategories().get(4).getCategory().getKey());
		assertEquals(0, preview.getCategories().get(0).getItemCount());
		assertEquals(0, preview.getCategories().get(3).getItemCount());
		assertEquals(3, preview.getCategories().get(4).getItemCount());
		assertEquals(1, preview.getCategories().get(9).getItemCount());
		assertEquals("Grimy irit x2", preview.getCategories().get(4).getSampleItems().get(0));
		assertEquals("Irit seed x3", preview.getCategories().get(4).getSampleItems().get(1));
		assertEquals("Super attack (3)", preview.getCategories().get(4).getSampleItems().get(2));
		assertEquals("Unknown item #999999", preview.getCategories().get(9).getSampleItems().get(0));
	}

	@Test
	public void previewTextRendersReadableTabPlan()
	{
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(145, 1, 1)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		String text = preview.toPreviewText();

		assertTrue(text.contains("Suggested Bank Blueprint"));
		assertTrue(text.contains("Ironman - All-Round Bank"));
		assertTrue(text.contains("MAIN. Currency & Account Utilities: 0"));
		assertTrue(text.contains("Irit seed"));
		assertTrue(text.contains("4. Potions, Food & PvM Supplies: 0"));
		assertTrue(text.contains("Super attack (3)"));
	}

	@Test
	public void runtimeEquipmentFactsRescueUnclassifiedItemsFromCleanup()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, "New equipable item",
			ItemCategory.CLEANUP, "cleanup", Collections.emptySet(), null));
		GearStats stats = new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, 10);

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(Collections.singletonList(new BankItemSnapshot(900001, 1, 0))),
			catalog, BankPresets.IRONMAN, itemId -> Optional.of(stats));

		assertEquals(1, preview.getCategories().get(2).getItemCount());
		assertEquals(0, preview.getCategories().get(9).getItemCount());
		BankPreviewItem item = preview.getCategories().get(2).getItems().get(0);
		assertEquals(ItemCategory.GEAR, item.getItemCategory());
		assertEquals("body", item.getSubcategory());
	}

	@Test
	public void bonuslessEquipableItemsAreNotAssumedToBeCombatGear()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, "Equipable quest utility",
			ItemCategory.CLEANUP, "cleanup", Collections.emptySet(), null));
		GearStats noCombatStats = new GearStats(GearSlot.WEAPON, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(Collections.singletonList(new BankItemSnapshot(900002, 1, 0))),
			catalog, BankPresets.IRONMAN, itemId -> Optional.of(noCombatStats));

		assertEquals(0, preview.getCategories().get(2).getItemCount());
		assertEquals(1, preview.getCategories().get(9).getItemCount());
	}

	@Test
	public void knownGearUsesRuntimeEquipmentSlotInBlueprintMetadata()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, "Existing boots",
			ItemCategory.GEAR, "gear", Collections.emptySet(), null));
		GearStats stats = new GearStats(GearSlot.FEET, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(Collections.singletonList(new BankItemSnapshot(900003, 1, 0))),
			catalog, BankPresets.IRONMAN, itemId -> Optional.of(stats));

		BankPreviewItem item = preview.getCategories().get(2).getItems().get(0);
		assertEquals(ItemCategory.GEAR, item.getItemCategory());
		assertEquals("feet", item.getSubcategory());
	}

	@Test
	public void potionPlaceholderOccupiesItsBlueprintCellWithoutBecomingOwned()
	{
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId, "Saradomin brew(3)",
			ItemCategory.POTION, "potion-dose-3", Collections.emptySet(), null));
		BankSnapshot snapshot = new BankSnapshot(Collections.singletonList(
			new BankItemSnapshot(6687, 0, 5, true)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog, BankPresets.IRONMAN);
		BankCategoryPreview herblore = preview.getCategories().get(4);

		assertEquals(1, herblore.getItemCount());
		assertEquals(0, snapshot.getTotalQuantity(6687));
		assertEquals(true, herblore.getItems().get(0).isPlaceholder());
		assertEquals(0, herblore.getItems().get(0).getQuantity());
	}
}

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
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor;
import com.pkoka5.ironmanbankarchitect.organize.layout.LayoutEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
		assertEquals("combat-gear", preview.getCategories().get(1).getCategory().getKey());
		assertEquals("herblore", preview.getCategories().get(3).getCategory().getKey());
		assertEquals(0, preview.getCategories().get(0).getItemCount());
		assertEquals(3, preview.getCategories().get(3).getItemCount());
		assertEquals(0, preview.getCategories().get(4).getItemCount());
		assertEquals(1, preview.getCategories().get(9).getItemCount());
		assertEquals("Grimy irit x2", preview.getCategories().get(3).getSampleItems().get(0));
		assertEquals("Irit seed x3", preview.getCategories().get(3).getSampleItems().get(1));
		assertTrue(preview.getCategories().get(3).getSampleItems().contains("Super attack (3)"));
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
		assertTrue(text.contains("MAIN. Frequently Used, Runes & Teleports: 0"));
		assertTrue(text.contains("Irit seed"));
		assertTrue(text.contains("4. Herblore & Potion Making: 2"));
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

		assertEquals(1, preview.getCategories().get(1).getItemCount());
		assertEquals(0, preview.getCategories().get(9).getItemCount());
		BankPreviewItem item = preview.getCategories().get(1).getItems().get(0);
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

		assertEquals(0, preview.getCategories().get(1).getItemCount());
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

		BankPreviewItem item = preview.getCategories().get(1).getItems().get(0);
		assertEquals(ItemCategory.GEAR, item.getItemCategory());
		assertEquals("feet", item.getSubcategory());
	}

	@Test
	public void potionPlaceholderOccupiesItsBlueprintCellWithoutBecomingOwned()
	{
		BankSnapshot snapshot = new BankSnapshot(Collections.singletonList(
			new BankItemSnapshot(6687, 0, 5, true)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN);
		BankCategoryPreview herblore = preview.getCategories().get(3);

		assertEquals(1, herblore.getItemCount());
		assertEquals(0, snapshot.getTotalQuantity(6687));
		assertEquals("Saradomin brew(3)", herblore.getItems().get(0).getDisplayName());
		assertEquals(true, herblore.getItems().get(0).isPlaceholder());
		assertEquals(0, herblore.getItems().get(0).getQuantity());
	}

	@Test
	public void gearPlaceholderSkipsAlchCalculationAndRemainsInBlueprint()
	{
		int adamant2hSword = 1317;
		int comparisonWeapon = 900004;
		ItemCatalog catalog = itemId -> Optional.of(new CatalogItem(itemId,
			itemId == adamant2hSword ? "Adamant 2h sword" : "Comparison weapon",
			ItemCategory.GEAR, "weapon", Collections.emptySet(), null));
		GearStats stats = new GearStats(GearSlot.WEAPON, 10, 0, 0, 0, 0, 10, 0, 0, 0);
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(comparisonWeapon, 1, 4),
			new BankItemSnapshot(adamant2hSword, 0, 17, true)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog, BankPresets.IRONMAN, itemId -> Optional.of(stats));

		List<BankPreviewItem> gear = category(preview, "combat-gear").getItems();
		assertEquals(2, gear.size());
		BankPreviewItem placeholder = gear.stream()
			.filter(item -> item.getItemId() == adamant2hSword)
			.findFirst()
			.orElseThrow(AssertionError::new);
		assertTrue(placeholder.isPlaceholder());
		assertEquals(0, placeholder.getQuantity());
	}

	@Test
	public void layoutEntryPreservesSourceSlotQuantityAndPlaceholderWithoutDenseRank()
	{
		BankItemSnapshot bankItem = new BankItemSnapshot(440, 0, 87, true);
		CatalogItem catalogItem = catalogItem(440, "Renamed iron ore", ItemCategory.SKILLING,
			"resource");

		LayoutEntry entry = BankOrganizationPreviewBuilder.toLayoutEntry(bankItem, catalogItem);

		assertEquals(87, entry.getSourceFlatBankSlot());
		assertFalse(entry.hasDenseCategoryRank());
		assertFalse(entry.hasLockedTarget());
		assertEquals(440, entry.getItem().getItemId());
		assertEquals(0, entry.getItem().getQuantity());
		assertTrue(entry.getItem().isPlaceholder());
	}

	@Test
	public void resourceCategoryUsesSemanticMetalMatrixAndExistingGuidancePath()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(2363, "Renamed runite bar", ItemCategory.SKILLING, "resource"),
			catalogItem(440, "Renamed iron ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2355, "Renamed silver bar", ItemCategory.SKILLING, "resource"),
			catalogItem(449, "Renamed adamantite ore", ItemCategory.SKILLING, "resource"),
			catalogItem(442, "Renamed silver ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2351, "Renamed iron bar", ItemCategory.SKILLING, "resource"),
			catalogItem(451, "Renamed runite ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2357, "Renamed gold bar", ItemCategory.SKILLING, "resource"),
			catalogItem(447, "Renamed mithril ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2359, "Renamed mithril bar", ItemCategory.SKILLING, "resource"),
			catalogItem(444, "Renamed gold ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2361, "Renamed adamantite bar", ItemCategory.SKILLING, "resource"),
			// Same-zone spillover: ore names keep both fillers inside Mining/Smithing.
			catalogItem(900001, "Renamed copper ore", ItemCategory.SKILLING, "resource"),
			catalogItem(900002, "Renamed blurite ore", ItemCategory.SKILLING, "resource"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				11 + index * 73));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		BankCategoryPreview resources = category(preview, "resources");
		List<Integer> expected = Arrays.asList(442, 444, 447, 449, 451, 900001, 440, 900002,
			2351, 2355, 2357, 2359, 2361, 2363);

		assertEquals(expected, itemIds(resources.getItems()));
		for (BankPreviewItem item : resources.getItems())
		{
			assertFalse(item.isBlank());
		}

		List<BankPreviewItem> flattened = BankTabPlan.fromPreview(preview).getFlattenedItems();
		assertEquals(expected, itemIds(flattened));
		int[] actual = itemIdArray(flattened);
		assertEquals(NextMoveAdvisor.Status.COMPLETE,
			NextMoveAdvisor.assess(actual, flattened).getStatus());
		int first = actual[0];
		actual[0] = actual[1];
		actual[1] = first;
		assertEquals(NextMoveAdvisor.Status.READY,
			NextMoveAdvisor.assess(actual, flattened).getStatus());
	}

	@Test
	public void resourcePlaceholderSurvivesBuilderAndSemanticPlacement()
	{
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(440, "Renamed iron ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(2351, "Renamed iron bar", ItemCategory.SKILLING, "resource"));
		for (int offset = 0; offset < 7; offset++)
		{
			catalogItems.add(catalogItem(901000 + offset, "Filler " + offset + " ore",
				ItemCategory.SKILLING, "resource"));
		}

		List<BankItemSnapshot> snapshots = new ArrayList<>();
		snapshots.add(new BankItemSnapshot(440, 0, 307, true));
		snapshots.add(new BankItemSnapshot(2351, 4, 2));
		for (int offset = 0; offset < 7; offset++)
		{
			snapshots.add(new BankItemSnapshot(901000 + offset, 1, 81 + offset * 19));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<BankPreviewItem> resources = category(preview, "resources").getItems();

		assertEquals(9, resources.size());
		assertEquals(440, resources.get(0).getItemId());
		assertTrue(resources.get(0).isPlaceholder());
		assertEquals(0, resources.get(0).getQuantity());
		assertEquals(2351, resources.get(8).getItemId());
		assertFalse(resources.get(8).isPlaceholder());
		assertEquals(4, resources.get(8).getQuantity());
	}

	@Test
	public void resourceCategoryUsesSeparateWoodAndPlankRows()
	{
		List<Integer> normalLogs = Arrays.asList(1511, 1521, 1517, 1515, 1513, 19669);
		List<Integer> constructionLogs = Arrays.asList(6333, 6332, 32904, 32907);
		List<Integer> otherLogs = Arrays.asList(10810, 24691);
		List<Integer> planks = Arrays.asList(960, 8778, 8780, 8782, 31435);
		List<Integer> sourceOrder = new ArrayList<>();
		sourceOrder.addAll(planks);
		sourceOrder.addAll(otherLogs);
		sourceOrder.addAll(constructionLogs);
		sourceOrder.addAll(normalLogs);

		List<CatalogItem> catalogItems = new ArrayList<>();
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < sourceOrder.size(); index++)
		{
			int itemId = sourceOrder.get(index);
			catalogItems.add(catalogItem(itemId, "Wood item " + itemId,
				ItemCategory.SKILLING, "resource"));
			snapshots.add(new BankItemSnapshot(itemId, 1, 500 + index * 11));
		}
		for (int offset = 0; offset < 13; offset++)
		{
			int itemId = 902000 + offset;
			catalogItems.add(catalogItem(itemId, "Filler " + offset + " logs",
				ItemCategory.SKILLING, "resource"));
			snapshots.add(new BankItemSnapshot(itemId, 1, 900 + offset * 13));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "resources").getItems());

		assertEquals(normalLogs, target.subList(0, 6));
		assertEquals(constructionLogs, target.subList(8, 12));
		assertEquals(otherLogs, target.subList(16, 18));
		assertEquals(planks, target.subList(24, 29));
		assertEquals(sourceOrder.size() + 13, target.size());
	}

	@Test
	public void mixedResourcesFallbackFollowsSkillZonesWithoutInventedCells()
	{
		// No item is a semantic-rule member, so every zone returns its local fallback.
		// The builder concatenates those zones densely in their fixed primary-skill order.
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(903011, "Iron bar", ItemCategory.SKILLING, "resource"),
			catalogItem(903010, "Iron ore", ItemCategory.SKILLING, "resource"),
			catalogItem(903012, "Oak logs", ItemCategory.SKILLING, "resource"),
			catalogItem(903013, "Oak plank", ItemCategory.SKILLING, "resource"),
			catalogItem(903014, "Uncut sapphire", ItemCategory.SKILLING, "uncut-gem"),
			catalogItem(903015, "Sapphire", ItemCategory.SKILLING, "gem"),
			catalogItem(903002, "Steel nails", ItemCategory.SKILLING, "skilling"),
			catalogItem(903003, "Arrow shaft", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(903004, "Bow string", ItemCategory.SKILLING, "textile"),
			catalogItem(903005, "Raw shark", ItemCategory.SKILLING, "raw-food"),
			catalogItem(903006, "Dragon bones", ItemCategory.SKILLING, "prayer-resource"),
			catalogItem(903007, "Smashed mirror", ItemCategory.SKILLING, "resource"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			int itemId = catalogItems.get(index).getItemId();
			if (itemId == 903011)
			{
				snapshots.add(new BankItemSnapshot(itemId, 0, 23 + index * 47, true));
			}
			else
			{
				snapshots.add(new BankItemSnapshot(itemId, index + 1, 23 + index * 47));
			}
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<BankPreviewItem> resources = category(preview, "resources").getItems();

		// Zone-first fallback order: Mining/Smithing before Fletching, Woodcutting and
		// Crafting each one block, with the internal workflow order preserved inside every zone.
		assertEquals(Arrays.asList(
			903010, 903011,
			903012, 903013,
			903014, 903015,
			903002,
			903003, 903004,
			903005,
			903006,
			903007), itemIds(resources));

		for (BankPreviewItem item : resources)
		{
			assertFalse(item.isBlank());
		}
		BankPreviewItem placeholder = resources.get(1);
		assertEquals(903011, placeholder.getItemId());
		assertTrue(placeholder.isPlaceholder());
		assertEquals(0, placeholder.getQuantity());
		assertEquals(903010, resources.get(0).getItemId());
		assertEquals(2, resources.get(0).getQuantity());
		assertFalse(resources.get(0).isPlaceholder());
	}

	@Test
	public void woodRowAfterEarlierZoneNeverCrossesAPhysicalRowBoundary()
	{
		List<Integer> normalLogs = Arrays.asList(1511, 1521, 1519, 1517, 1515, 1513, 19669);
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(904001, "Copper ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(904002, "Tin ore", ItemCategory.SKILLING, "resource"));
		for (Integer itemId : normalLogs)
		{
			catalogItems.add(catalogItem(itemId, "Wood item " + itemId, ItemCategory.SKILLING, "resource"));
		}
		for (int offset = 0; offset < 6; offset++)
		{
			catalogItems.add(catalogItem(904100 + offset, "Wood filler " + offset + " logs",
				ItemCategory.SKILLING, "resource"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				40 + index * 29));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "resources").getItems());

		// Dense permutation without blanks; quantities survive planning.
		assertEquals(snapshots.size(), target.size());
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
		for (BankPreviewItem item : category(preview, "resources").getItems())
		{
			assertFalse(item.isBlank());
			assertEquals(itemIdsFromSnapshots(snapshots).indexOf(item.getItemId()) + 1,
				item.getQuantity());
		}

		// The full 7-wide log row is one ordered block of consecutive cells...
		List<Integer> logIndices = new ArrayList<>();
		for (Integer itemId : normalLogs)
		{
			logIndices.add(target.indexOf(itemId));
		}
		for (int position = 1; position < logIndices.size(); position++)
		{
			assertEquals("log tiers must occupy consecutive ordered cells",
				logIndices.get(position - 1) + 1, (int) logIndices.get(position));
		}

		// ...and that block never crosses an 8-column physical row boundary.
		int physicalRow = logIndices.get(0) / 8;
		for (Integer index : logIndices)
		{
			assertEquals("log row crosses a physical row boundary at target " + index,
				physicalRow, index / 8);
		}
	}

	@Test
	public void woodRowsCanUseGlobalSpilloverAfterTwentyTwoMiningItems()
	{
		List<CatalogItem> catalogItems = new ArrayList<>();
		int[] miningIds = {436, 440, 453, 442, 444, 447, 449, 451,
			2351, 2353, 2355, 2357, 2359, 2361, 31719, 32892,
			22603, 21543, 13573, 21545, 13421, 21622};
		String[] miningNames = {"Copper ore", "Iron ore", "Coal", "Silver ore", "Gold ore",
			"Mithril ore", "Adamantite ore", "Runite ore", "Iron bar", "Steel bar", "Silver bar",
			"Gold bar", "Mithril bar", "Adamantite bar", "Nickel ore", "Cupronickel bar",
			"Basalt", "Calcite", "Dynamite", "Pyrophosphite", "Saltpetre", "Volcanic ash"};
		for (int index = 0; index < miningIds.length; index++)
		{
			catalogItems.add(catalogItem(miningIds[index], miningNames[index],
				ItemCategory.SKILLING, "resource"));
		}

		List<Integer> normalLogs = Arrays.asList(1511, 1521, 1517, 1515, 1513, 19669);
		String[] normalNames = {"Logs", "Oak logs", "Maple logs", "Yew logs", "Magic logs", "Redwood logs"};
		for (int index = 0; index < normalLogs.size(); index++)
		{
			catalogItems.add(catalogItem(normalLogs.get(index), normalNames[index],
				ItemCategory.SKILLING, "resource"));
		}
		catalogItems.add(catalogItem(6333, "Teak logs", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(6332, "Mahogany logs", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(32904, "Camphor logs", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(24691, "Blisterwood logs", ItemCategory.SKILLING, "resource"));
		List<Integer> planks = Arrays.asList(960, 8778, 8780, 8782, 31435);
		String[] plankNames = {"Plank", "Oak plank", "Teak plank", "Mahogany plank", "Ironwood plank"};
		for (int index = 0; index < planks.size(); index++)
		{
			catalogItems.add(catalogItem(planks.get(index), plankNames[index],
				ItemCategory.SKILLING, "resource"));
		}
		for (int offset = 0; offset < 11; offset++)
		{
			catalogItems.add(catalogItem(907000 + offset, "Crafting filler " + offset,
				ItemCategory.SKILLING, "crafting-material"));
		}

		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1, 80 + index * 31));
		}
		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"resources").getItems());

		List<Integer> ores = Arrays.asList(436, 440, 453, 442, 444, 447, 449, 451);
		List<Integer> processed = Arrays.asList(2351, 2353, 2355, 2357, 2359, 2361, 32892);
		List<Integer> supplemental = Arrays.asList(31719, 22603, 21543, 13573, 21545, 13421, 21622);
		assertPhysicalRun(target, ores);
		assertPhysicalRun(target, processed);
		assertPhysicalRun(target, supplemental);
		assertFalse("Nickel ore must not fill the processed-metal row",
			target.indexOf(31719) / 8 == target.indexOf(2351) / 8);
		assertPhysicalRun(target, normalLogs);
		assertPhysicalRun(target, planks);
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
	}

	@Test
	public void gemMatrixAfterEarlierZoneKeepsRawAndProcessedOnAlignedPhysicalRows()
	{
		List<Integer> rawGems = Arrays.asList(1623, 1621, 1619, 1617, 1631);
		List<Integer> processedGems = Arrays.asList(1607, 1605, 1603, 1601, 1615);
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(904001, "Copper ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(904002, "Tin ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(904003, "Blurite ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(904004, "Bronze bar", ItemCategory.SKILLING, "resource"));
		for (int position = 0; position < rawGems.size(); position++)
		{
			catalogItems.add(catalogItem(rawGems.get(position), "Gem item " + rawGems.get(position),
				ItemCategory.SKILLING, "resource"));
			catalogItems.add(catalogItem(processedGems.get(position),
				"Gem item " + processedGems.get(position), ItemCategory.SKILLING, "resource"));
		}
		for (int offset = 0; offset < 7; offset++)
		{
			catalogItems.add(catalogItem(904200 + offset, "Crafting filler " + offset,
				ItemCategory.SKILLING, "crafting-material"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				60 + index * 37));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "resources").getItems());

		// No phantom items: exactly the snapshot multiset, no blanks.
		assertEquals(snapshots.size(), target.size());
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
		for (BankPreviewItem item : category(preview, "resources").getItems())
		{
			assertFalse(item.isBlank());
		}

		// All raw gems share one physical row; all processed gems share the next physical row.
		int rawRow = target.indexOf(rawGems.get(0)) / 8;
		int processedRow = target.indexOf(processedGems.get(0)) / 8;
		assertEquals(rawRow + 1, processedRow);
		for (int position = 0; position < rawGems.size(); position++)
		{
			int rawIndex = target.indexOf(rawGems.get(position));
			int processedIndex = target.indexOf(processedGems.get(position));
			assertEquals("raw gem left its physical row", rawRow, rawIndex / 8);
			assertEquals("processed gem left its physical row", processedRow, processedIndex / 8);
			assertEquals("family column must align raw above processed",
				rawIndex % 8, processedIndex % 8);
		}

		// Both rows start at the same physical column.
		assertEquals(target.indexOf(rawGems.get(0)) % 8, target.indexOf(processedGems.get(0)) % 8);
	}

	@Test
	public void constructionBonesCanCompleteTheRowBeforePrayerStarts()
	{
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(10977, "Curved bone", ItemCategory.SKILLING, "skilling"));
		catalogItems.add(catalogItem(10976, "Long bone", ItemCategory.SKILLING, "skilling"));
		for (int index = 0; index < 6; index++)
		{
			catalogItems.add(catalogItem(906100 + index, "Raw fish " + index,
				ItemCategory.SKILLING, "raw-food"));
		}
		catalogItems.add(catalogItem(536, "Dragon bones", ItemCategory.SKILLING, "skilling"));
		catalogItems.add(catalogItem(13475, "Ensouled giant head",
			ItemCategory.SKILLING, "prayer-resource"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1, index * 17));
		}

		List<BankPreviewItem> resources = category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"resources").getItems();

		assertEquals("Dragon bones", resources.get(8).getDisplayName());
		assertEquals("Ensouled giant head", resources.get(9).getDisplayName());
		assertEquals(10, new HashSet<>(itemIds(resources)).size());
	}

	@Test
	public void arrowtipRunAfterEarlierZoneUsesItsPhysicalOffsetWithoutCrossingRows()
	{
		List<Integer> arrowtips = Arrays.asList(39, 40, 41, 42, 43, 44, 21350, 11237);
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(905001, "Copper ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(905002, "Tin ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(905003, "Coal", ItemCategory.SKILLING, "resource"));
		for (Integer itemId : arrowtips)
		{
			catalogItems.add(catalogItem(itemId, "Fletching item " + itemId,
				ItemCategory.SKILLING, "ammo-component"));
		}
		for (int offset = 0; offset < 5; offset++)
		{
			catalogItems.add(catalogItem(905100 + offset, "Fletching filler " + offset,
				ItemCategory.SKILLING, "ammo-component"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				70 + index * 41));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "resources").getItems());

		assertEquals(snapshots.size(), target.size());
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
		int first = target.indexOf(arrowtips.get(0));
		assertEquals(0, first % 8);
		assertEquals(arrowtips, target.subList(first, first + arrowtips.size()));
		for (int index = first; index < first + arrowtips.size(); index++)
		{
			assertEquals("arrowtip run crosses a physical bank row", first / 8, index / 8);
		}
	}

	@Test
	public void constructionNailsUseOnePhysicalTierRowAfterEarlierResources()
	{
		List<Integer> nails = Arrays.asList(4819, 4820, 1539, 4822, 4823, 4824);
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(905201, "Copper ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(905202, "Tin ore", ItemCategory.SKILLING, "resource"));
		catalogItems.add(catalogItem(905203, "Coal", ItemCategory.SKILLING, "resource"));
		for (Integer itemId : nails)
		{
			catalogItems.add(catalogItem(itemId, "Construction nail " + itemId,
				ItemCategory.SKILLING, "resource"));
		}
		for (int offset = 0; offset < 5; offset++)
		{
			catalogItems.add(catalogItem(905300 + offset, "Crafting filler " + offset,
				ItemCategory.SKILLING, "crafting-resource"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				90 + index * 37));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "resources").getItems());

		assertEquals(snapshots.size(), target.size());
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
		int first = target.indexOf(nails.get(0));
		assertTrue(first >= 0);
		assertTrue("nail run crosses a physical bank row", first % 8 + nails.size() <= 8);
		assertEquals(nails, target.subList(first, first + nails.size()));
	}

	@Test
	public void ownedFletchingWorkflowsFinishBeforeRawFoodStarts()
	{
		List<Integer> fletchingItems =
			Arrays.asList(9422, 29311, 52, 314, 53, 9416, 1777, 39, 43, 822, 823);
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(436, "Copper ore", ItemCategory.SKILLING, "resource"),
			catalogItem(440, "Iron ore", ItemCategory.SKILLING, "resource"),
			catalogItem(453, "Coal", ItemCategory.SKILLING, "resource"),
			catalogItem(442, "Silver ore", ItemCategory.SKILLING, "resource"),
			catalogItem(444, "Gold ore", ItemCategory.SKILLING, "resource"),
			catalogItem(447, "Mithril ore", ItemCategory.SKILLING, "resource"),
			catalogItem(449, "Adamantite ore", ItemCategory.SKILLING, "resource"),
			catalogItem(451, "Runite ore", ItemCategory.SKILLING, "resource"),
			catalogItem(2351, "Iron bar", ItemCategory.SKILLING, "resource"),
			catalogItem(2353, "Steel bar", ItemCategory.SKILLING, "resource"),
			catalogItem(2355, "Silver bar", ItemCategory.SKILLING, "resource"),
			catalogItem(2357, "Gold bar", ItemCategory.SKILLING, "resource"),
			catalogItem(2359, "Mithril bar", ItemCategory.SKILLING, "resource"),
			catalogItem(2361, "Adamantite bar", ItemCategory.SKILLING, "resource"),
			catalogItem(32892, "Cupronickel bar", ItemCategory.SKILLING, "resource"),
			catalogItem(31719, "Nickel ore", ItemCategory.SKILLING, "resource"),
			catalogItem(22603, "Basalt", ItemCategory.SKILLING, "resource"),
			catalogItem(21543, "Calcite", ItemCategory.SKILLING, "resource"),
			catalogItem(13573, "Dynamite", ItemCategory.SKILLING, "resource"),
			catalogItem(21545, "Pyrophosphite", ItemCategory.SKILLING, "resource"),
			catalogItem(13421, "Saltpetre", ItemCategory.SKILLING, "resource"),
			catalogItem(21622, "Volcanic ash", ItemCategory.SKILLING, "resource"),
			catalogItem(9422, "Blurite limbs", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(29311, "Hunter spear tips", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(52, "Arrow shaft", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(314, "Feather", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(53, "Headless arrow", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(9416, "Mith grapple tip", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(1777, "Bow string", ItemCategory.SKILLING, "textile"),
			catalogItem(39, "Bronze arrowtips", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(43, "Adamant arrowtips", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(822, "Mithril dart tip", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(823, "Adamant dart tip", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(327, "Raw sardine", ItemCategory.SKILLING, "raw-food"),
			catalogItem(353, "Raw mackerel", ItemCategory.SKILLING, "raw-food"),
			catalogItem(331, "Raw salmon", ItemCategory.SKILLING, "raw-food"),
			catalogItem(359, "Raw tuna", ItemCategory.SKILLING, "raw-food"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1, 90 + index * 23));
		}

		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"resources").getItems());

		int lastFletching = -1;
		for (Integer itemId : fletchingItems)
		{
			lastFletching = Math.max(lastFletching, target.indexOf(itemId));
		}
		int firstRawFood = Math.min(Math.min(target.indexOf(327), target.indexOf(353)),
			Math.min(target.indexOf(331), target.indexOf(359)));
		assertTrue("raw food started before the Fletching zone ended: " + target,
			lastFletching < firstRawFood);
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
	}

	@Test
	public void everyResourcesModeFallbackKeepsSkillZoneOrderAcrossPresets()
	{
		// Fallback-only fixture: no semantic-rule members, so zone order is deterministic.
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(903010, "Iron ore", ItemCategory.SKILLING, "resource"),
			catalogItem(903011, "Iron bar", ItemCategory.SKILLING, "resource"),
			catalogItem(903012, "Willow logs", ItemCategory.SKILLING, "resource"),
			catalogItem(903003, "Arrow shaft", ItemCategory.SKILLING, "ammo-component"),
			catalogItem(903006, "Dragon bones", ItemCategory.SKILLING, "prayer-resource"),
			catalogItem(385, "Shark", ItemCategory.POTION, "food"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				31 + index * 59));
		}
		Set<Integer> expectedIds = new HashSet<>(itemIdsFromSnapshots(snapshots));

		for (BankPreset preset : Arrays.asList(BankPresets.IRONMAN, BankPresets.SKILLER))
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				new BankSnapshot(snapshots), catalog(catalogItems), preset);

			List<Integer> plannedIds = new ArrayList<>();
			for (BankPreviewItem item : preview.getPlannedItems())
			{
				assertFalse(preset.getKey() + " invented a blank cell", item.isBlank());
				plannedIds.add(item.getItemId());
			}
			assertEquals(preset.getKey() + " changed the entry count",
				snapshots.size(), plannedIds.size());
			assertEquals(preset.getKey() + " lost or invented item IDs",
				expectedIds, new HashSet<>(plannedIds));

			for (BankCategoryPreview category : preview.getCategories())
			{
				if (category.getCategory().getSortMode() != BankCategorySortMode.RESOURCES)
				{
					continue;
				}
				int previousZone = -1;
				for (BankPreviewItem item : category.getItems())
				{
					int zone = ResourceSkillZoneClassifier.classify(item).ordinal();
					assertTrue(preset.getKey() + "/" + category.getCategory().getKey()
						+ " interleaves skill zones", zone >= previousZone);
					previousZone = zone;
				}
			}
		}
	}

	@Test
	public void resourceCategoryWithoutEligibleFamiliesUsesExactMicroSortFallback()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(900101, "Oak plank", ItemCategory.SKILLING, "resource"),
			catalogItem(900102, "Iron ore", ItemCategory.SKILLING, "resource"),
			catalogItem(900103, "Iron bar", ItemCategory.SKILLING, "resource"));
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(900101, 1, 90),
			new BankItemSnapshot(900103, 1, 4),
			new BankItemSnapshot(900102, 1, 211)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog(catalogItems), BankPresets.IRONMAN);

		assertEquals(Arrays.asList(900102, 900103, 900101),
			itemIds(category(preview, "resources").getItems()));
	}

	@Test
	public void completeSkillingOutfitsUseSeparateVerticalColumnsInTheToolsTab()
	{
		List<Integer> angler = Arrays.asList(13258, 13259, 13260, 13261);
		List<Integer> lumberjack = Arrays.asList(10941, 10939, 10940, 10933);
		List<CatalogItem> catalogItems = new ArrayList<>();
		for (Integer itemId : angler)
		{
			catalogItems.add(catalogItem(itemId, "Angler item " + itemId,
				ItemCategory.TOOL, "skilling-outfit"));
		}
		for (Integer itemId : lumberjack)
		{
			catalogItems.add(catalogItem(itemId, "Lumberjack item " + itemId,
				ItemCategory.TOOL, "skilling-outfit"));
		}
		for (int offset = 0; offset < 24; offset++)
		{
			catalogItems.add(catalogItem(906000 + offset, "Loose tool " + offset,
				ItemCategory.TOOL, "tool"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1, 20 + index * 17));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> target = itemIds(category(preview, "skilling-tools").getItems());

		assertVerticalFamily(target, angler);
		assertVerticalFamily(target, lumberjack);
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
	}

	@Test
	public void supplyCategoryMovesCompleteDoseRunOffAPhysicalRowBoundary()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(9739, "Renamed combat dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(141, "Renamed prayer dose two", ItemCategory.POTION, "potion-dose-2"),
			catalogItem(12905, "Renamed anti-venom dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(2434, "Renamed prayer dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(2452, "Renamed antifire dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(143, "Renamed prayer dose one", ItemCategory.POTION, "potion-dose-1"),
			catalogItem(2428, "Renamed attack dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(385, "Renamed shark", ItemCategory.POTION, "food"),
			catalogItem(12913, "Renamed anti-venom-plus dose four", ItemCategory.POTION,
				"potion-dose-4"),
			catalogItem(139, "Renamed prayer dose three", ItemCategory.POTION, "potion-dose-3"),
			catalogItem(2446, "Renamed antipoison dose four", ItemCategory.POTION, "potion-dose-4"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), index + 1,
				17 + index * 61));
		}
		List<BankPreviewItem> fallbackInput = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			fallbackInput.add(new BankPreviewItem(catalogItems.get(index), index + 1));
		}
		List<Integer> fallbackOrder = Arrays.asList(12905, 12913, 2452, 2446, 2428, 9739,
			2434, 139, 141, 143, 385);
		assertEquals(fallbackOrder, itemIds(SupplyItemSorter.sort(fallbackInput)));
		assertEquals(Arrays.asList(2434, 139, 141, 143), fallbackOrder.subList(6, 10));

		List<Integer> expected = Arrays.asList(2434, 139, 141, 143, 12905, 12913, 2452, 2446,
			2428, 9739, 385);
		BankSnapshot snapshot = new BankSnapshot(snapshots);
		for (BankPreset preset : Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN,
			BankPresets.PVM, BankPresets.PVP))
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				snapshot, catalog(catalogItems), preset);
			List<BankPreviewItem> supplies = category(preview, BankCategorySortMode.SUPPLIES).getItems();

			List<Integer> expectedSupplies = preset.getType() == BankPresetType.IRONMAN
				? Arrays.asList(12905, 12913, 2452, 2446, 2428, 9739, 2434, 385)
				: expected;
			assertEquals(preset.getType().name(), expectedSupplies, itemIds(supplies));
			for (BankPreviewItem item : supplies)
			{
				assertFalse(item.isBlank());
			}

			List<BankPreviewItem> flattened = BankTabPlan.fromPreview(preview).getFlattenedItems();
			assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(itemIds(flattened)));
			int[] actual = itemIdArray(flattened);
			assertEquals(NextMoveAdvisor.Status.COMPLETE,
				NextMoveAdvisor.assess(actual, flattened).getStatus());
			int first = actual[0];
			actual[0] = actual[1];
			actual[1] = first;
			assertEquals(NextMoveAdvisor.Status.READY,
				NextMoveAdvisor.assess(actual, flattened).getStatus());
		}
	}

	@Test
	public void supplyDosePlaceholderSurvivesBuilderAndHorizontalRun()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(2434, "Renamed prayer dose four", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(139, "Renamed prayer dose three", ItemCategory.POTION, "potion-dose-3"));
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(139, 6, 2),
			new BankItemSnapshot(2434, 0, 144, true)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog(catalogItems), BankPresets.IRONMAN);
		List<BankPreviewItem> supplies = category(preview, "potions-food").getItems();
		List<BankPreviewItem> herblore = category(preview, "herblore").getItems();

		assertEquals(Collections.singletonList(2434), itemIds(supplies));
		assertEquals(Collections.singletonList(139), itemIds(herblore));
		assertTrue(supplies.get(0).isPlaceholder());
		assertEquals(0, supplies.get(0).getQuantity());
		assertFalse(herblore.get(0).isPlaceholder());
		assertEquals(6, herblore.get(0).getQuantity());
	}

	@Test
	public void supplyCategoryWithoutEligibleFamilyUsesExistingSorting()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(385, "Shark", ItemCategory.POTION, "food"),
			catalogItem(2434, "Prayer potion(4)", ItemCategory.POTION, "potion-dose-4"));
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(385, 10, 71),
			new BankItemSnapshot(2434, 2, 8)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog(catalogItems), BankPresets.IRONMAN);

		assertEquals(Arrays.asList(2434, 385),
			itemIds(category(preview, "potions-food").getItems()));
	}

	@Test
	public void skillerGenericHerbloreCategoryDoesNotUseSupplyDoseSemantics()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(2434, "Zulu dose", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(139, "Alpha dose", ItemCategory.POTION, "potion-dose-3"),
			catalogItem(141, "Mike dose", ItemCategory.POTION, "potion-dose-2"),
			catalogItem(143, "Beta dose", ItemCategory.POTION, "potion-dose-1"));
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(2434, 1, 1), new BankItemSnapshot(139, 1, 2),
			new BankItemSnapshot(141, 1, 3), new BankItemSnapshot(143, 1, 4)));

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			snapshot, catalog(catalogItems), BankPresets.SKILLER);

		assertEquals(Arrays.asList(139, 143, 141, 2434),
			itemIds(category(preview, "herblore-materials").getItems()));
	}

	@Test
	public void mainCategoryBuildsDiaryRewardsAsFourColumnsByThreeRows()
	{
		List<CatalogItem> catalogItems = new ArrayList<>();
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < 16; index++)
		{
			int itemId = 900000 + index;
			catalogItems.add(catalogItem(itemId, "Currency " + index,
				ItemCategory.CURRENCY, "currency"));
			snapshots.add(new BankItemSnapshot(itemId, 1, index * 19));
		}
		String[] names = {"Ardougne cloak 4", "Desert amulet 4", "Explorer's ring 4",
			"Falador shield 4", "Fremennik sea boots 4", "Kandarin headgear 4",
			"Karamja gloves 4", "Morytania legs 4", "Rada's blessing 4",
			"Varrock armour 4", "Western banner 4", "Wilderness sword 4"};
		List<Integer> rewardIds = new ArrayList<>();
		for (int index = 0; index < names.length; index++)
		{
			int itemId = 910000 + index;
			catalogItems.add(catalogItem(itemId, names[index], ItemCategory.CURRENCY, "currency"));
			snapshots.add(new BankItemSnapshot(itemId, 1, 401 + index * 23));
			rewardIds.add(itemId);
		}

		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"currency-utilities").getItems());

		int first = target.indexOf(rewardIds.get(0));
		assertTrue(first >= 0);
		for (int index = 0; index < rewardIds.size(); index++)
		{
			assertEquals(first + (index / 4) * 8 + index % 4,
				target.indexOf(rewardIds.get(index)));
		}
		assertEquals(snapshots.size(), target.size());
	}

	@Test
	public void mainCombinesCoinsGracefulRunesAndDiaryWhileCluesStaySeparate()
	{
		List<Integer> graceful = Arrays.asList(11850, 11854, 11856, 11858, 11860, 11852);
		List<Integer> elementalRunes = Arrays.asList(556, 555, 557, 554);
		List<Integer> combatRunes = Arrays.asList(558, 562, 560, 565);
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(995, "Coins", ItemCategory.CURRENCY, "currency"));
		catalogItems.add(catalogItem(12791, "Rune pouch", ItemCategory.RUNE, "rune-container"));
		int[] runeIds = {556, 555, 557, 554, 558, 562, 560, 565, 559, 564, 561, 563,
			9075, 21880, 566, 4699};
		for (int runeId : runeIds)
		{
			catalogItems.add(catalogItem(runeId, "Rune " + runeId, ItemCategory.RUNE, "rune"));
		}
		String[] gracefulNames = {"Graceful hood", "Graceful top", "Graceful legs",
			"Graceful gloves", "Graceful boots", "Graceful cape"};
		for (int index = 0; index < graceful.size(); index++)
		{
			catalogItems.add(catalogItem(graceful.get(index), gracefulNames[index],
				ItemCategory.TOOL, "skilling-outfit"));
		}
		int[] clueIds = {23182, 24361, 2681, 24362, 19758, 24363, 7249, 24364, 12094, 24365};
		String[] clueNames = {"Clue scroll (beginner)", "Scroll box (beginner)",
			"Clue scroll (easy)", "Scroll box (easy)", "Clue scroll (medium)",
			"Scroll box (medium)", "Clue scroll (hard)", "Scroll box (hard)",
			"Clue scroll (elite)", "Scroll box (elite)"};
		for (int index = 0; index < clueIds.length; index++)
		{
			catalogItems.add(catalogItem(clueIds[index], clueNames[index],
				ItemCategory.CLUE, "treasure-trail"));
		}
		String[] diaryNames = {"Ardougne cloak 4", "Desert amulet 4", "Explorer's ring 4",
			"Falador shield 4", "Fremennik sea boots 4", "Kandarin headgear 4",
			"Karamja gloves 4", "Morytania legs 4", "Rada's blessing 4",
			"Varrock armour 4", "Western banner 4", "Wilderness sword 4"};
		for (int index = 0; index < diaryNames.length; index++)
		{
			catalogItems.add(catalogItem(940000 + index, diaryNames[index],
				ItemCategory.CURRENCY, "currency"));
		}
		catalogItems.add(catalogItem(300001, "Clue ornament kit",
			ItemCategory.CLUE, "cosmetic"));
		for (int index = 0; index < 20; index++)
		{
			catalogItems.add(catalogItem(930000 + index, "Main utility " + index,
				ItemCategory.CURRENCY, "currency"));
		}
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1,
				17 + index * 31));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> main = itemIds(category(preview, "currency-utilities").getItems());

		assertEquals(Integer.valueOf(995), main.get(0));
		assertVerticalFamily(main, graceful);
		assertPhysicalRun(main, elementalRunes);
		assertPhysicalRun(main, combatRunes);
		int diaryStart = main.indexOf(940000);
		assertTrue(diaryStart >= 0);
		for (int index = 0; index < diaryNames.length; index++)
		{
			assertTrue("diary item left Main: " + diaryNames[index],
				main.contains(940000 + index));
		}
		assertEquals(56, main.size());
		Set<Integer> expectedClues = new HashSet<>();
		for (int clueId : clueIds) expectedClues.add(clueId);
		expectedClues.add(300001);
		assertEquals(expectedClues,
			new HashSet<>(itemIds(category(preview, "clues-cosmetics").getItems())));
	}

	@Test
	public void mainTakesOnlyHammerAndHighestOwnedPickaxeAndAxe()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(2347, "Hammer", ItemCategory.TOOL, "tool"),
			catalogItem(1275, "Rune pickaxe", ItemCategory.TOOL, "tool"),
			catalogItem(11920, "Dragon pickaxe", ItemCategory.TOOL, "tool"),
			catalogItem(1359, "Rune axe", ItemCategory.TOOL, "tool"),
			catalogItem(10491, "Blessed axe", ItemCategory.TOOL, "tool"),
			catalogItem(6739, "Dragon axe", ItemCategory.TOOL, "tool"));
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			snapshots.add(new BankItemSnapshot(catalogItems.get(index).getItemId(), 1, index));
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);

		assertEquals(new HashSet<>(Arrays.asList(2347, 11920, 6739)),
			new HashSet<>(itemIds(category(preview, "currency-utilities").getItems())));
		assertEquals(new HashSet<>(Arrays.asList(1275, 1359, 10491)),
			new HashSet<>(itemIds(category(preview, "skilling-tools").getItems())));
	}

	@Test
	public void gearCategoryBuildsEvidenceBackedSetsAsVerticalColumns()
	{
		List<Integer> proselyte = Arrays.asList(9672, 9674, 9676);
		List<Integer> mixedHide = Arrays.asList(29280, 29283, 29286);
		List<Integer> ids = new ArrayList<>();
		ids.addAll(proselyte);
		ids.addAll(mixedHide);
		for (int index = 0; index < 24; index++)
		{
			ids.add(920000 + index);
		}
		List<CatalogItem> catalogItems = new ArrayList<>();
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < ids.size(); index++)
		{
			catalogItems.add(catalogItem(ids.get(index), "Gear " + ids.get(index),
				ItemCategory.GEAR, "gear"));
			snapshots.add(new BankItemSnapshot(ids.get(index), 1, 31 + index * 29));
		}

		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"combat-gear").getItems());

		assertVerticalFamily(target, proselyte);
		assertVerticalFamily(target, mixedHide);
		assertEquals(new HashSet<>(ids), new HashSet<>(target));
	}

	@Test
	public void gearSetRulesNeverRepackPrimaryCombatStyleColumns()
	{
		int[][] rows = {
			{930101, 930102, 930103, 9672, 930105, 930106, 930107, 930108},
			{930201, 29280, 930203, 9674, 930205, 930206, 930207, 930208},
			{930301, 29283, 930303, 9676, 930305, 930306, 930307, 930308},
			{930401, 29286, 930403, 930404, 930405, 930406, 930407, 930408}
		};
		String[][] names = {
			{"Helm of neitiznot", "Archer helm", "Farseer helm", "Proselyte sallet"},
			{"Bandos chestplate", "Mixed hide top", "Mystic robe top", "Proselyte hauberk"},
			{"Obsidian platelegs", "Mixed hide legs", "Mystic robe bottom", "Proselyte cuisse"},
			{"Dragon boots", "Mixed hide boots", "Mystic boots", "Prayer boots"}
		};
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.FEET};
		List<CatalogItem> catalogItems = new ArrayList<>();
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		Map<Integer, GearStats> stats = new HashMap<>();
		for (int row = 0; row < rows.length; row++)
		{
			for (int column = 0; column < rows[row].length; column++)
			{
				int itemId = rows[row][column];
				String name = column < 4 ? names[row][column] : "Spare " + row + " " + column;
				catalogItems.add(catalogItem(itemId, name, ItemCategory.GEAR, "gear"));
				snapshots.add(new BankItemSnapshot(itemId, 1, row * 100 + column));
				int melee = column == 0 || column >= 4 ? 5 : 0;
				int ranged = column == 1 ? 5 : 0;
				int magic = column == 2 ? 5 : 0;
				int prayer = column == 3 ? 5 : 0;
				stats.put(itemId, new GearStats(slots[row], melee, 0, 0, magic, ranged,
					melee, ranged, prayer, 100 - column));
			}
		}

		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN,
			itemId -> Optional.ofNullable(stats.get(itemId))), "combat-gear").getItems());

		for (int row = 0; row < rows.length; row++)
		{
			assertEquals("melee/strength column", Integer.valueOf(rows[row][0]), target.get(row * 8));
			assertEquals("ranged column", Integer.valueOf(rows[row][1]), target.get(row * 8 + 1));
			assertEquals("magic column", Integer.valueOf(rows[row][2]), target.get(row * 8 + 2));
			assertEquals("prayer column", Integer.valueOf(rows[row][3]), target.get(row * 8 + 3));
		}
		assertEquals(32, new HashSet<>(target).size());
	}

	@Test
	public void everyPresetBuildsDensePermutationWithoutInventedCells()
	{
		List<CatalogItem> catalogItems = Arrays.asList(
			catalogItem(1001, "Coins", ItemCategory.CURRENCY, "currency"),
			catalogItem(1002, "Law rune", ItemCategory.RUNE, "rune"),
			catalogItem(1003, "Dragon scimitar", ItemCategory.GEAR, "weapon"),
			catalogItem(1004, "Prayer potion(4)", ItemCategory.POTION, "potion-dose-4"),
			catalogItem(1005, "Irit seed", ItemCategory.FARMING, "herb-seed"),
			catalogItem(1006, "Iron ore", ItemCategory.SKILLING, "ore"),
			catalogItem(1007, "Clue scroll (hard)", ItemCategory.CLUE, "clue"));
		ItemCatalog catalog = itemId -> catalogItems.stream()
			.filter(item -> item.getItemId() == itemId)
			.findFirst();
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1001, 100, 0),
			new BankItemSnapshot(1002, 50, 1),
			new BankItemSnapshot(1003, 1, 2),
			new BankItemSnapshot(1004, 2, 3),
			new BankItemSnapshot(1005, 4, 4),
			new BankItemSnapshot(1006, 20, 5),
			new BankItemSnapshot(1007, 1, 6),
			new BankItemSnapshot(999999, 1, 7)));
		Set<Integer> expectedIds = new HashSet<>();
		for (BankItemSnapshot item : snapshot.getItems())
		{
			expectedIds.add(item.getItemId());
		}

		for (BankPresetType type : BankPresetType.values())
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				snapshot, catalog, BankPresets.forType(type));
			List<Integer> actualIds = new ArrayList<>();
			for (BankPreviewItem item : preview.getPlannedItems())
			{
				assertEquals("preset " + type + " invented a blank cell", false, item.isBlank());
				actualIds.add(item.getItemId());
			}

			assertEquals("preset " + type + " changed the entry count",
				snapshot.getItems().size(), actualIds.size());
			assertEquals("preset " + type + " lost or invented item IDs",
				expectedIds, new HashSet<>(actualIds));
		}
	}

	@Test
	public void ironmanMainUsesFourWideRuneRowsBeforeTeleportSpillover()
	{
		int[] runeIds = {555, 554, 558, 559, 564, 562, 561, 560, 565, 566, 9075, 4699};
		String[] runeNames = {"Water rune", "Fire rune", "Mind rune", "Body rune", "Cosmic rune",
			"Chaos rune", "Nature rune", "Death rune", "Blood rune", "Soul rune", "Astral rune",
			"Lava rune"};
		List<CatalogItem> catalogItems = new ArrayList<>();
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		for (int index = 0; index < runeIds.length; index++)
		{
			catalogItems.add(catalogItem(runeIds[index], runeNames[index], ItemCategory.RUNE, "rune"));
			snapshots.add(new BankItemSnapshot(runeIds[index], 1, 100 + index * 13));
		}
		for (int offset = 0; offset < 16; offset++)
		{
			int itemId = 990000 + offset;
			catalogItems.add(catalogItem(itemId, "Teleport filler " + offset,
				ItemCategory.TELEPORT, "teleport"));
			snapshots.add(new BankItemSnapshot(itemId, 1, 500 + offset * 19));
		}

		List<Integer> target = itemIds(category(BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN),
			"currency-utilities").getItems());

		assertEquals(Arrays.asList(555, 554), target.subList(0, 2));
		assertEquals(Arrays.asList(558, 562, 560, 565), target.subList(8, 12));
		assertEquals(Arrays.asList(559, 564, 561), target.subList(16, 19));
		assertEquals(Arrays.asList(9075, 566), target.subList(24, 26));
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)), new HashSet<>(target));
	}

	@Test
	public void mixedIronmanReroutesRemainAWholePreviewDensePermutation()
	{
		List<CatalogItem> catalogItems = new ArrayList<>();
		catalogItems.add(catalogItem(995, "Coins", ItemCategory.CURRENCY, "currency"));
		catalogItems.add(catalogItem(8013, "Teleport to house", ItemCategory.TELEPORT, "teleport"));
		catalogItems.add(catalogItem(6739, "Dragon axe", ItemCategory.TOOL, "tool"));
		catalogItems.add(catalogItem(11920, "Dragon pickaxe", ItemCategory.TOOL, "tool"));
		catalogItems.add(catalogItem(2347, "Hammer", ItemCategory.TOOL, "tool"));
		catalogItems.add(catalogItem(1755, "Chisel", ItemCategory.TOOL, "tool"));
		catalogItems.add(catalogItem(952, "Spade", ItemCategory.TOOL, "tool"));
		int[] runes = {556, 555, 557, 554, 558, 562, 560, 565, 559, 564, 561, 563,
			9075, 566, 4699, 21880, 4695, 4696, 4698, 4697, 4694};
		for (int rune : runes)
		{
			catalogItems.add(catalogItem(rune, "Rune " + rune, ItemCategory.RUNE, "rune"));
		}
		for (int index = 0; index < 32; index++)
		{
			catalogItems.add(catalogItem(970000 + index, "Teleport " + index,
				ItemCategory.TELEPORT, "teleport"));
		}
		for (int itemId : new int[] {7936, 24704, 32083, 32085})
		{
			catalogItems.add(catalogItem(itemId, "Resource " + itemId,
				itemId < 30000 ? ItemCategory.RUNE : ItemCategory.SKILLING, "resource"));
		}
		for (int itemId : new int[] {1438, 1444, 5509, 5510, 5511, 5512, 5513, 5514, 5515,
			26784, 26786, 5521, 19634})
		{
			catalogItems.add(catalogItem(itemId, "Tool " + itemId, ItemCategory.RUNE,
				itemId == 1438 || itemId == 1444 ? "runecrafting-focus" : "utility"));
		}
		for (int itemId : new int[] {11941, 13226, 13639})
		{
			catalogItems.add(catalogItem(itemId, "Container " + itemId, ItemCategory.TOOL,
				"utility-container"));
		}
		for (int index = 0; index < 8; index++)
		{
			catalogItems.add(catalogItem(980000 + index, "Tool filler " + index,
				ItemCategory.TOOL, "tool"));
		}
		catalogItems.add(catalogItem(23182, "Clue scroll (beginner)", ItemCategory.CLUE,
			"treasure-trail"));
		for (int itemId : new int[] {6183, 6529, 6306, 12012, 25527, 21555})
		{
			catalogItems.add(catalogItem(itemId, "Reward " + itemId, ItemCategory.CURRENCY,
				"currency"));
		}
		catalogItems.add(catalogItem(11832, "Bandos chestplate", ItemCategory.GEAR, "body"));
		catalogItems.add(catalogItem(385, "Shark", ItemCategory.POTION, "food"));

		List<BankItemSnapshot> snapshots = new ArrayList<>();
		Map<Integer, Integer> quantities = new HashMap<>();
		for (int index = 0; index < catalogItems.size(); index++)
		{
			int quantity = index + 1;
			int itemId = catalogItems.get(index).getItemId();
			snapshots.add(new BankItemSnapshot(itemId, quantity, 1000 + index * 37));
			quantities.put(itemId, quantity);
		}

		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
			new BankSnapshot(snapshots), catalog(catalogItems), BankPresets.IRONMAN);
		List<Integer> main = itemIds(category(preview, "currency-utilities").getItems());
		assertPhysicalRun(main, Arrays.asList(6739, 11920, 2347, 1755, 952));
		List<Integer> tools = itemIds(category(preview, "skilling-tools").getItems());
		assertPhysicalRun(tools, Arrays.asList(11941, 13226, 13639, 19634));
		List<BankPreviewItem> allItems = new ArrayList<>();
		for (BankCategoryPreview category : preview.getCategories())
		{
			allItems.addAll(category.getItems());
		}

		assertEquals(snapshots.size(), allItems.size());
		assertEquals(new HashSet<>(itemIdsFromSnapshots(snapshots)),
			new HashSet<>(itemIds(allItems)));
		for (BankPreviewItem item : allItems)
		{
			assertFalse(item.isBlank());
			assertEquals(quantities.get(item.getItemId()).intValue(), item.getQuantity());
		}
	}

	private static CatalogItem catalogItem(int itemId, String name, ItemCategory category, String subcategory)
	{
		return new CatalogItem(itemId, name, category, subcategory, Collections.emptySet(), null);
	}

	private static ItemCatalog catalog(List<CatalogItem> items)
	{
		return itemId -> items.stream()
			.filter(item -> item.getItemId() == itemId)
			.findFirst();
	}

	private static BankCategoryPreview category(BankOrganizationPreview preview, String categoryKey)
	{
		for (BankCategoryPreview category : preview.getCategories())
		{
			if (categoryKey.equals(category.getCategory().getKey()))
			{
				return category;
			}
		}
		throw new AssertionError("missing category " + categoryKey);
	}

	private static BankCategoryPreview category(BankOrganizationPreview preview,
		BankCategorySortMode sortMode)
	{
		for (BankCategoryPreview category : preview.getCategories())
		{
			if (sortMode == category.getCategory().getSortMode())
			{
				return category;
			}
		}
		throw new AssertionError("missing sort mode " + sortMode);
	}

	private static List<Integer> itemIds(List<BankPreviewItem> items)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			ids.add(item.getItemId());
		}
		return ids;
	}

	private static void assertVerticalFamily(List<Integer> target, List<Integer> family)
	{
		int first = target.indexOf(family.get(0));
		assertTrue("missing first outfit item", first >= 0);
		for (int index = 0; index < family.size(); index++)
		{
			assertEquals("outfit family left its vertical column",
				first + index * 8, target.indexOf(family.get(index)));
		}
	}

	private static void assertPhysicalRun(List<Integer> target, List<Integer> family)
	{
		int first = target.indexOf(family.get(0));
		assertTrue("missing first run item", first >= 0);
		assertTrue("run crosses a physical row: first=" + first + ", family=" + family
			+ ", target=" + target, first % 8 + family.size() <= 8);
		assertEquals(family, target.subList(first, first + family.size()));
	}

	private static int[] itemIdArray(List<BankPreviewItem> items)
	{
		int[] ids = new int[items.size()];
		for (int index = 0; index < items.size(); index++)
		{
			ids[index] = items.get(index).getItemId();
		}
		return ids;
	}

	private static List<Integer> itemIdsFromSnapshots(List<BankItemSnapshot> items)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankItemSnapshot item : items)
		{
			ids.add(item.getItemId());
		}
		return ids;
	}
}

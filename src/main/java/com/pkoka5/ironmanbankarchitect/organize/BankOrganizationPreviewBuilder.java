package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BankOrganizationPreviewBuilder
{
	// An item is an alch candidate when this many strictly better items of the
	// same style and slot are owned (best + one backup stay in combat gear).
	private static final int OUTCLASSED_BY_COUNT = 2;
	private static final int ALCH_VALUE_THRESHOLD = 5000;
	// Owning this many copies of one wearable marks it as production stock
	// (smithing/crafting output), not a gear option the player switches to.
	private static final int BULK_STOCK_QUANTITY = 8;
	private static final String ALCH_CATEGORY_KEY = "slayer-boss-loot";

	private BankOrganizationPreviewBuilder()
	{
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset)
	{
		return build(snapshot, catalog, preset, GearStatsSource.NONE, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats)
	{
		return build(snapshot, catalog, preset, gearStats, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues)
	{
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(catalog, "catalog");
		Objects.requireNonNull(preset, "preset");
		Objects.requireNonNull(gearStats, "gearStats");
		Objects.requireNonNull(itemValues, "itemValues");

		Map<String, MutableCategoryPreview> previewsByCategory = new LinkedHashMap<>();
		for (BankCategory category : preset.getCategories())
		{
			previewsByCategory.put(category.getKey(), new MutableCategoryPreview(category));
		}

		Map<String, List<Integer>> gearScoresByKey = new LinkedHashMap<>();
		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			if (bankItem.isPlaceholder())
			{
				continue;
			}
			Optional<GearStats> stats = gearStats.statsFor(bankItem.getItemId());
			if (stats.isPresent())
			{
				CatalogItem catalogItem = effectiveCatalogItem(catalog.describeOrUnknown(bankItem.getItemId()),
					bankItem.getItemId(), gearStats);
				if (catalogItem.getCategory() != ItemCategory.GEAR)
				{
					continue;
				}
				gearScoresByKey.computeIfAbsent(gearKey(stats.get()), key -> new ArrayList<>())
					.add(GearItemSorter.score(new BankPreviewItem(catalogItem, bankItem.getQuantity()), gearStats));
			}
		}

		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			CatalogItem catalogItem = effectiveCatalogItem(catalog.describeOrUnknown(bankItem.getItemId()),
				bankItem.getItemId(), gearStats);
			BankCategory category = PresetCategoryMapper.map(preset, catalogItem);
			if (isAlchCandidate(preset, category, catalogItem, bankItem.getQuantity(),
				gearStats, itemValues, gearScoresByKey))
			{
				category = preset.getCategory(ALCH_CATEGORY_KEY);
			}
			MutableCategoryPreview preview = previewsByCategory.get(category.getKey());
			if (preview == null)
			{
				throw new IllegalStateException("Preset mapper returned unknown category: " + category.getKey());
			}

			preview.add(catalogItem, bankItem.getQuantity(), bankItem.isPlaceholder());
		}

		List<BankCategoryPreview> categories = new ArrayList<>();
		for (MutableCategoryPreview preview : previewsByCategory.values())
		{
			categories.add(preview.toImmutable(gearStats));
		}

		return new BankOrganizationPreview(preset, categories);
	}

	private static CatalogItem effectiveCatalogItem(CatalogItem item, int itemId, GearStatsSource gearStats)
	{
		Optional<GearStats> stats = gearStats.statsFor(itemId);
		if (stats.isPresent() && (item.getCategory() == ItemCategory.GEAR
			|| ((item.getCategory() == ItemCategory.CLEANUP
				|| item.getCategory() == ItemCategory.UNKNOWN
				|| item.getCategory() == ItemCategory.UNCATEGORIZED)
				&& stats.get().score() > 0)))
		{
			return new CatalogItem(item.getItemId(), item.getDisplayName(), ItemCategory.GEAR,
				stats.get().getSlot().name().toLowerCase(), item.getTags(),
				item.getWorkflowKey().orElse(null));
		}

		return item;
	}

	/**
	 * Ironman alch rule: a combat gear item whose style+slot already has two
	 * strictly better owned items is a duplicate the player will realistically
	 * never wear again; when it is also worth alching it moves to the alch
	 * review tab instead of cluttering the gear columns.
	 */
	private static boolean isAlchCandidate(BankPreset preset, BankCategory category, CatalogItem catalogItem, int quantity,
		GearStatsSource gearStats, ItemValueSource itemValues, Map<String, List<Integer>> gearScoresByKey)
	{
		if (preset.getType() != BankPresetType.IRONMAN || !"combat-gear".equals(category.getKey()))
		{
			return false;
		}
		if (quantity <= 1)
		{
			return false;
		}

		Optional<GearStats> stats = gearStats.statsFor(catalogItem.getItemId());
		if (!stats.isPresent())
		{
			return false;
		}

		List<Integer> scores = gearScoresByKey.get(gearKey(stats.get()));
		if (scores == null)
		{
			return false;
		}

		int ownScore = GearItemSorter.score(new BankPreviewItem(catalogItem, quantity), gearStats);
		int strictlyBetter = 0;
		for (int score : scores)
		{
			if (score > ownScore)
			{
				strictlyBetter++;
			}
		}

		// Bulk production stock (rune platebodies, crafted jewellery) moves out
		// regardless of alch value, but weapons and ammo stay: high quantities
		// there are consumables (chinchompas, thrown weapons), not stock.
		GearSlot slot = stats.get().getSlot();
		if (quantity >= BULK_STOCK_QUANTITY && slot != GearSlot.WEAPON && slot != GearSlot.AMMO
			&& strictlyBetter >= 1)
		{
			return true;
		}

		return itemValues.highAlchValue(catalogItem.getItemId()) >= ALCH_VALUE_THRESHOLD
			&& strictlyBetter >= OUTCLASSED_BY_COUNT;
	}

	private static String gearKey(GearStats stats)
	{
		return stats.style().ordinal() + ":" + stats.slotRank();
	}

	private static final class MutableCategoryPreview
	{
		private final BankCategory category;
		private final List<BankPreviewItem> items = new ArrayList<>();

		private MutableCategoryPreview(BankCategory category)
		{
			this.category = category;
		}

		private void add(CatalogItem catalogItem, int quantity, boolean placeholder)
		{
			items.add(new BankPreviewItem(catalogItem, quantity, placeholder));
		}

		private BankCategoryPreview toImmutable(GearStatsSource gearStats)
		{
			return new BankCategoryPreview(category, PresetItemSorter.sort(category, items, gearStats));
		}
	}
}

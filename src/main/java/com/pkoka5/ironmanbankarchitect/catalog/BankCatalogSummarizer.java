package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.List;



public final class BankCatalogSummarizer
{
	private static final int MAX_REVIEW_ENTRIES = 10;

	private BankCatalogSummarizer()
	{
	}

	public static BankCatalogSummary summarize(BankSnapshot snapshot, ItemCatalog catalog)
	{
		return summarize(snapshot, catalog, null);
	}

	public static BankCatalogSummary summarize(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset)
	{
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(catalog, "catalog");

		int knownIdCount = 0;
		int unknownIdCount = 0;
		Map<ItemCategory, Integer> countsByCategory = new EnumMap<>(ItemCategory.class);
		Map<String, Integer> countsByPresetCategory = new LinkedHashMap<>();
		List<BankItemReviewEntry> reviewEntries = new ArrayList<>();

		for (BankItemSnapshot item : snapshot.getItems())
		{
			CatalogItem described = catalog.describeOrUnknown(item.getItemId());
			countsByCategory.merge(described.getCategory(), 1, Integer::sum);

			if (described.getCategory() == ItemCategory.UNKNOWN)
			{
				unknownIdCount++;
			}
			else
			{
				knownIdCount++;
			}

			if (described.getCategory() == ItemCategory.UNKNOWN && reviewEntries.size() < MAX_REVIEW_ENTRIES)
			{
				reviewEntries.add(new BankItemReviewEntry(item.getItemId(), described.getDisplayName(), item.getSlotIndex()));
			}

			if (preset != null)
			{
				BankCategory presetCategory = PresetCategoryMapper.map(preset, described);
				countsByPresetCategory.merge(presetCategory.getKey(), 1, Integer::sum);
			}
		}

		return new BankCatalogSummary(knownIdCount, unknownIdCount, countsByCategory, preset,
			countsByPresetCategory, reviewEntries);
	}
}

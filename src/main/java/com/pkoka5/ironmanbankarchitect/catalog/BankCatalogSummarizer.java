package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class BankCatalogSummarizer
{
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

			if (preset != null)
			{
				if (described.getCategory() != ItemCategory.UNCATEGORIZED)
				{
					BankCategory presetCategory = PresetCategoryMapper.map(preset, described);
					countsByPresetCategory.merge(presetCategory.getKey(), 1, Integer::sum);
				}
			}
		}

		return new BankCatalogSummary(knownIdCount, unknownIdCount, countsByCategory, preset, countsByPresetCategory);
	}
}

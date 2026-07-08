package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class BankCatalogSummarizer
{
	private BankCatalogSummarizer()
	{
	}

	public static BankCatalogSummary summarize(BankSnapshot snapshot, ItemCatalog catalog)
	{
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(catalog, "catalog");

		int knownIdCount = 0;
		int unknownIdCount = 0;
		Map<ItemCategory, Integer> countsByCategory = new EnumMap<>(ItemCategory.class);

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
		}

		return new BankCatalogSummary(knownIdCount, unknownIdCount, countsByCategory);
	}
}

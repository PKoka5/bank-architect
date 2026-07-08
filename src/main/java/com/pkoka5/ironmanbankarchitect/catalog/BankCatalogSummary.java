package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable summary of a bank scan classified through {@link ItemCatalog}. Counts are by unique
 * item ID, not by bank slot or stack size, since {@code BankSnapshot} already rolls up duplicate
 * item IDs.
 */
public final class BankCatalogSummary
{
	private final int knownIdCount;
	private final int unknownIdCount;
	private final Map<ItemCategory, Integer> countsByCategory;

	public BankCatalogSummary(int knownIdCount, int unknownIdCount, Map<ItemCategory, Integer> countsByCategory)
	{
		if (knownIdCount < 0 || unknownIdCount < 0)
		{
			throw new IllegalArgumentException("counts must not be negative");
		}

		this.knownIdCount = knownIdCount;
		this.unknownIdCount = unknownIdCount;
		this.countsByCategory = Collections.unmodifiableMap(
			new EnumMap<>(Objects.requireNonNull(countsByCategory, "countsByCategory")));
	}

	public int getKnownIdCount()
	{
		return knownIdCount;
	}

	public int getUnknownIdCount()
	{
		return unknownIdCount;
	}

	public int getTotalScannedIdCount()
	{
		return knownIdCount + unknownIdCount;
	}

	public int countFor(ItemCategory category)
	{
		Integer count = countsByCategory.get(category);
		return count == null ? 0 : count;
	}

	public Map<ItemCategory, Integer> getCountsByCategory()
	{
		return countsByCategory;
	}

	public String toOverviewText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Bank Scan Overview").append('\n')
			.append("Known catalog IDs: ").append(knownIdCount).append('\n')
			.append("Unknown IDs: ").append(unknownIdCount);

		StringBuilder categoryLines = new StringBuilder();
		for (Map.Entry<ItemCategory, Integer> entry : countsByCategory.entrySet())
		{
			if (entry.getValue() == null || entry.getValue() <= 0)
			{
				continue;
			}

			categoryLines.append('\n')
				.append(entry.getKey().getDisplayLabel())
				.append(": ")
				.append(entry.getValue());
		}

		if (categoryLines.length() > 0)
		{
			builder.append('\n').append('\n').append("Categories:").append(categoryLines);
		}

		return builder.toString();
	}
}

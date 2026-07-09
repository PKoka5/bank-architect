package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
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
	private final BankPreset preset;
	private final Map<String, Integer> countsByPresetCategory;
	private final List<BankItemReviewEntry> reviewEntries;

	public BankCatalogSummary(int knownIdCount, int unknownIdCount, Map<ItemCategory, Integer> countsByCategory)
	{
		this(knownIdCount, unknownIdCount, countsByCategory, null, Collections.emptyMap(), Collections.emptyList());
	}

	public BankCatalogSummary(int knownIdCount, int unknownIdCount, Map<ItemCategory, Integer> countsByCategory,
		BankPreset preset, Map<String, Integer> countsByPresetCategory)
	{
		this(knownIdCount, unknownIdCount, countsByCategory, preset, countsByPresetCategory, Collections.emptyList());
	}

	public BankCatalogSummary(int knownIdCount, int unknownIdCount, Map<ItemCategory, Integer> countsByCategory,
		BankPreset preset, Map<String, Integer> countsByPresetCategory, List<BankItemReviewEntry> reviewEntries)
	{
		if (knownIdCount < 0 || unknownIdCount < 0)
		{
			throw new IllegalArgumentException("counts must not be negative");
		}

		this.knownIdCount = knownIdCount;
		this.unknownIdCount = unknownIdCount;
		this.countsByCategory = Collections.unmodifiableMap(
			new EnumMap<>(Objects.requireNonNull(countsByCategory, "countsByCategory")));
		this.preset = preset;
		this.countsByPresetCategory = Collections.unmodifiableMap(
			new LinkedHashMap<>(Objects.requireNonNull(countsByPresetCategory, "countsByPresetCategory")));
		this.reviewEntries = Collections.unmodifiableList(
			new ArrayList<>(Objects.requireNonNull(reviewEntries, "reviewEntries")));
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

	public int countForPresetCategory(String categoryKey)
	{
		Integer count = countsByPresetCategory.get(categoryKey);
		return count == null ? 0 : count;
	}

	public Map<String, Integer> getCountsByPresetCategory()
	{
		return countsByPresetCategory;
	}

	public List<BankItemReviewEntry> getReviewEntries()
	{
		return reviewEntries;
	}

	public String toOverviewText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Bank Scan Overview").append('\n')
			.append("Recognized item IDs: ").append(knownIdCount).append('\n')
			.append("Unrecognized IDs: ").append(unknownIdCount);
		int needsRulesCount = countFor(ItemCategory.UNCATEGORIZED);
		if (needsRulesCount > 0)
		{
			builder.append('\n').append("Needs category rules: ").append(needsRulesCount);
		}

		if (preset != null)
		{
			builder.append('\n').append('\n').append(preset.getName()).append(':');
			for (BankCategory category : preset.getCategories())
			{
				builder.append('\n')
					.append(category.getName())
					.append(": ")
					.append(countForPresetCategory(category.getKey()));
			}
		}

		if (!reviewEntries.isEmpty())
		{
			builder.append('\n').append('\n').append("Rule review:");
			for (BankItemReviewEntry entry : reviewEntries)
			{
				builder.append('\n').append(entry.toCompactText());
			}
		}

		return builder.toString();
	}
}

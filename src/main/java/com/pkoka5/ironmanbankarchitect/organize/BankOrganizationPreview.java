package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BankOrganizationPreview
{
	private final BankPreset preset;
	private final List<BankCategoryPreview> categories;
	private final Map<String, Integer> tagCounts;

	public BankOrganizationPreview(BankPreset preset, List<BankCategoryPreview> categories)
	{
		this(preset, categories, Collections.<String, Integer>emptyMap());
	}

	public BankOrganizationPreview(BankPreset preset, List<BankCategoryPreview> categories,
		Map<String, Integer> tagCounts)
	{
		this.preset = Objects.requireNonNull(preset, "preset");
		this.categories = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(categories, "categories")));
		this.tagCounts = Collections.unmodifiableMap(
			new LinkedHashMap<>(Objects.requireNonNull(tagCounts, "tagCounts")));
	}

	public BankPreset getPreset()
	{
		return preset;
	}

	/**
	 * How many items each tag holds, for the layout screen to show what a tab
	 * would weigh under an arrangement the player has not saved yet. Empty when
	 * the blueprint was built without a plan, because the tags are only resolved
	 * on that path.
	 */
	public Map<String, Integer> getTagCounts()
	{
		return tagCounts;
	}

	public List<BankCategoryPreview> getCategories()
	{
		return categories;
	}

	public List<BankPreviewItem> getPlannedItems()
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (BankCategoryPreview category : categories)
		{
			items.addAll(category.getItems());
		}

		return Collections.unmodifiableList(items);
	}

	public int getExpectedItemId(int slotIndex)
	{
		List<BankPreviewItem> items = getPlannedItems();
		if (slotIndex < 0 || slotIndex >= items.size())
		{
			return -1;
		}

		return items.get(slotIndex).getItemId();
	}

	public int getPlannedItemCount()
	{
		int count = 0;
		for (BankCategoryPreview category : categories)
		{
			count += category.getItemCount();
		}

		return count;
	}

	public int getPlannedSlotIndex(int itemId)
	{
		List<BankPreviewItem> items = getPlannedItems();
		for (int i = 0; i < items.size(); i++)
		{
			BankPreviewItem item = items.get(i);
			if (!item.isBlank() && item.getItemId() == itemId)
			{
				return i;
			}
		}

		return -1;
	}

	public String toPreviewText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Suggested Bank Blueprint").append('\n')
			.append(preset.getName());

		int tabNumber = 1;
		for (BankCategoryPreview category : categories)
		{
			builder.append('\n')
				.append(tabNumber == 1 ? "MAIN" : "TAB " + tabNumber)
				.append(". ")
				.append(category.getCategory().getName())
				.append(": ")
				.append(category.getItemCount());

			if (!category.getSampleItems().isEmpty())
			{
				builder.append('\n')
					.append("   e.g. ")
					.append(String.join(", ", category.getSampleItems()));
				int hiddenItemCount = category.getItemCount() - category.getSampleItems().size();
				if (hiddenItemCount > 0)
				{
					builder.append(" +")
						.append(hiddenItemCount)
						.append(" more");
				}
			}

			tabNumber++;
		}

		return builder.toString();
	}
}

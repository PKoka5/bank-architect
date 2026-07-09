package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BankOrganizationPreview
{
	private final BankPreset preset;
	private final List<BankCategoryPreview> categories;

	public BankOrganizationPreview(BankPreset preset, List<BankCategoryPreview> categories)
	{
		this.preset = Objects.requireNonNull(preset, "preset");
		this.categories = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(categories, "categories")));
	}

	public BankPreset getPreset()
	{
		return preset;
	}

	public List<BankCategoryPreview> getCategories()
	{
		return categories;
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
				.append(tabNumber)
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

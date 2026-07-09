package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BankCategoryPreview
{
	private final BankCategory category;
	private final List<BankPreviewItem> items;

	public BankCategoryPreview(BankCategory category, List<BankPreviewItem> items)
	{
		this.category = Objects.requireNonNull(category, "category");
		this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "items")));
	}

	public BankCategory getCategory()
	{
		return category;
	}

	public int getItemCount()
	{
		return items.size();
	}

	public List<BankPreviewItem> getItems()
	{
		return items;
	}

	public List<String> getSampleItems()
	{
		List<String> samples = new ArrayList<>();
		int sampleCount = Math.min(3, items.size());
		for (int i = 0; i < sampleCount; i++)
		{
			samples.add(items.get(i).toCompactLabel());
		}

		return Collections.unmodifiableList(samples);
	}
}

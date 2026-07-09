package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BankCategoryPreview
{
	private final BankCategory category;
	private final int itemCount;
	private final List<String> sampleItems;

	public BankCategoryPreview(BankCategory category, int itemCount, List<String> sampleItems)
	{
		if (itemCount < 0)
		{
			throw new IllegalArgumentException("itemCount must not be negative");
		}

		this.category = Objects.requireNonNull(category, "category");
		this.itemCount = itemCount;
		this.sampleItems = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(sampleItems, "sampleItems")));
	}

	public BankCategory getCategory()
	{
		return category;
	}

	public int getItemCount()
	{
		return itemCount;
	}

	public List<String> getSampleItems()
	{
		return sampleItems;
	}
}

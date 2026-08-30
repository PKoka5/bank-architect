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
		List<BankPreviewItem> physicalItems = new ArrayList<>();
		for (BankPreviewItem item : Objects.requireNonNull(items, "items"))
		{
			physicalItems.addAll(Objects.requireNonNull(item, "item").physicalBankSlots());
		}
		this.items = Collections.unmodifiableList(physicalItems);
	}

	public BankCategory getCategory()
	{
		return category;
	}

	public int getItemCount()
	{
		int count = 0;
		for (BankPreviewItem item : items)
		{
			if (!item.isBlank())
			{
				count++;
			}
		}

		return count;
	}

	public List<BankPreviewItem> getItems()
	{
		return items;
	}

	public List<String> getSampleItems()
	{
		List<String> samples = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			if (samples.size() >= 3)
			{
				break;
			}
			if (!item.isBlank())
			{
				samples.add(item.toCompactLabel());
			}
		}

		return Collections.unmodifiableList(samples);
	}
}

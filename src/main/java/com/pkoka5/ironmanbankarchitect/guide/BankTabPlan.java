package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Dense physical-bank-tab plan derived from the ten blueprint categories.
 * Blueprint category 1 is the existing untabbed/main range. Non-empty
 * categories 2..10 are mapped densely to the nine physical bank tabs because
 * OSRS cannot retain an empty numbered tab.
 */
public final class BankTabPlan
{
	private static final int MAIN_CATEGORY_INDEX = 0;
	private static final int FIRST_NUMBERED_CATEGORY_INDEX = 1;
	private static final int TOTAL_CATEGORY_COUNT = 10;

	private final List<TargetTab> numberedTabs;
	private final String mainCategoryKey;
	private final String mainCategoryName;
	private final List<BankPreviewItem> mainItems;
	private final List<BankPreviewItem> flattenedItems;

	private BankTabPlan(List<TargetTab> numberedTabs, String mainCategoryKey,
		String mainCategoryName, List<BankPreviewItem> mainItems)
	{
		this.numberedTabs = Collections.unmodifiableList(new ArrayList<>(numberedTabs));
		this.mainCategoryKey = Objects.requireNonNull(mainCategoryKey, "mainCategoryKey");
		this.mainCategoryName = Objects.requireNonNull(mainCategoryName, "mainCategoryName");
		this.mainItems = immutableCopy(mainItems);

		List<BankPreviewItem> flattened = new ArrayList<>();
		for (TargetTab tab : numberedTabs)
		{
			flattened.addAll(tab.getItems());
		}
		flattened.addAll(mainItems);
		this.flattenedItems = Collections.unmodifiableList(flattened);
	}

	public static BankTabPlan fromPreview(BankOrganizationPreview preview)
	{
		Objects.requireNonNull(preview, "preview");
		List<BankCategoryPreview> categories = preview.getCategories();
		if (categories.size() != TOTAL_CATEGORY_COUNT)
		{
			throw new IllegalArgumentException("tab guidance requires exactly 10 blueprint categories");
		}

		BankCategoryPreview main = categories.get(MAIN_CATEGORY_INDEX);
		List<TargetTab> numbered = new ArrayList<>();
		for (int categoryIndex = FIRST_NUMBERED_CATEGORY_INDEX;
			categoryIndex < TOTAL_CATEGORY_COUNT; categoryIndex++)
		{
			BankCategoryPreview category = categories.get(categoryIndex);
			if (category.getItemCount() == 0)
			{
				continue;
			}
			numbered.add(new TargetTab(numbered.size() + 1, categoryIndex + 1,
				category.getCategory().getKey(), category.getCategory().getName(), category.getItems()));
		}

		return new BankTabPlan(numbered, main.getCategory().getKey(),
			main.getCategory().getName(), main.getItems());
	}

	public List<TargetTab> getNumberedTabs()
	{
		return numberedTabs;
	}

	public List<BankPreviewItem> getMainItems()
	{
		return mainItems;
	}

	public String getMainCategoryKey()
	{
		return mainCategoryKey;
	}

	public String getMainCategoryName()
	{
		return mainCategoryName;
	}

	public List<BankPreviewItem> getFlattenedItems()
	{
		return flattenedItems;
	}

	private static List<BankPreviewItem> immutableCopy(List<BankPreviewItem> items)
	{
		return Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "items")));
	}

	public static final class TargetTab
	{
		private final int bankTabNumber;
		private final int blueprintCategoryNumber;
		private final String categoryKey;
		private final String categoryName;
		private final List<BankPreviewItem> items;

		private TargetTab(int bankTabNumber, int blueprintCategoryNumber, String categoryKey,
			String categoryName, List<BankPreviewItem> items)
		{
			this.bankTabNumber = bankTabNumber;
			this.blueprintCategoryNumber = blueprintCategoryNumber;
			this.categoryKey = Objects.requireNonNull(categoryKey, "categoryKey");
			this.categoryName = Objects.requireNonNull(categoryName, "categoryName");
			this.items = immutableCopy(items);
		}

		public int getBankTabNumber()
		{
			return bankTabNumber;
		}

		public int getBlueprintCategoryNumber()
		{
			return blueprintCategoryNumber;
		}

		public String getCategoryKey()
		{
			return categoryKey;
		}

		public String getCategoryName()
		{
			return categoryName;
		}

		public List<BankPreviewItem> getItems()
		{
			return items;
		}
	}
}

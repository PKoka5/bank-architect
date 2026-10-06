package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.List;



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
	private final BankLayoutPlan layout;
	private int[] lastActual;
	private int[] lastCounts;
	private List<BankPreviewItem> lastEffective;

	private BankTabPlan(List<TargetTab> numberedTabs, String mainCategoryKey,
		String mainCategoryName, List<BankPreviewItem> mainItems, BankLayoutPlan layout)
	{
		this.layout = Objects.requireNonNull(layout, "layout");
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
		return fromPreview(preview, BankLayoutPlan.defaultFor(preview.getPreset()));
	}

	public static BankTabPlan fromPreview(BankOrganizationPreview preview, BankLayoutPlan layout)
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
			main.getCategory().getName(), main.getItems(), layout);
	}

	/** Project live order without changing the plan identity or its item destinations. */
	public synchronized List<BankPreviewItem> effectiveItems(int[] actualItemIds, int[] tabCounts)
	{
		if (!layout.hasCurrentOrder()) return flattenedItems;
		if (Arrays.equals(lastActual, actualItemIds) && Arrays.equals(lastCounts, tabCounts)) return lastEffective;
		if (tabCounts.length != TabRouteAdvisor.MAX_TABS) throw new IllegalArgumentException("Invalid tab counts");
		long total = 0;
		for (int count : tabCounts)
		{
			if (count < 0) throw new IllegalArgumentException("Negative tab count");
			total += count;
		}
		if (total > actualItemIds.length) throw new IllegalArgumentException("Tab counts exceed bank size");
		List<BankPreviewItem> result = new ArrayList<>();
		int start = 0;
		for (TargetTab tab : numberedTabs)
		{
			int end = start + tabCounts[tab.getBankTabNumber() - 1];
			result.addAll(currentOrder(tab.getItems(), actualItemIds, start, end,
				layout.keepsCurrentOrder(tab.getBlueprintCategoryNumber() - 1)));
			start = end;
		}
		result.addAll(currentOrder(mainItems, actualItemIds, start, actualItemIds.length,
			layout.keepsCurrentOrder(0)));
		lastActual = actualItemIds.clone();
		lastCounts = tabCounts.clone();
		lastEffective = Collections.unmodifiableList(result);
		return lastEffective;
	}

	private static List<BankPreviewItem> currentOrder(List<BankPreviewItem> target, int[] actual,
		int start, int end, boolean keep)
	{
		if (!keep) return target;
		Map<Integer, Deque<BankPreviewItem>> remaining = new HashMap<>();
		for (BankPreviewItem item : target)
			remaining.computeIfAbsent(item.getItemId(), key -> new ArrayDeque<>()).addLast(item);
		List<BankPreviewItem> result = new ArrayList<>();
		for (int slot = start; slot < end; slot++)
		{
			Deque<BankPreviewItem> copies = remaining.get(actual[slot]);
			if (copies != null && !copies.isEmpty()) result.add(copies.removeFirst());
		}
		for (BankPreviewItem item : target)
		{
			Deque<BankPreviewItem> copies = remaining.get(item.getItemId());
			if (!copies.isEmpty() && copies.peekFirst() == item) result.add(copies.removeFirst());
		}
		return result;
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

	public boolean keepsCurrentOrder(int destinationIndex) { return layout.keepsCurrentOrder(destinationIndex); }

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

package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintItemOrders;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/** A local concept: operations never mutate the preview or the real bank. */
final class BlueprintDraft
{
	private List<List<BankPreviewItem>> tabs = new ArrayList<>();
	private Map<String, BlueprintItemOrders.Destination> transfers = new LinkedHashMap<>();
	private Set<Integer> modified = new LinkedHashSet<>();
	private final Map<String, BlueprintItemOrders.Destination> previous;
	private final Deque<State> undo = new ArrayDeque<>();
	private int tab;

	BlueprintDraft(List<BankPreviewItem> items)
	{
		tabs.add(new ArrayList<>(items));
		previous = Collections.emptyMap();
	}
	BlueprintDraft(BankOrganizationPreview preview, BlueprintItemOrders orders, int selectedTab)
	{
		previous = orders.destinations();
		Map<Integer, Integer> occurrences = new LinkedHashMap<>();
		preview.getCategories().forEach(category -> {
			List<BankPreviewItem> items = new ArrayList<>();
			for (BankPreviewItem item : category.getItems())
			{
				int ordinal = occurrences.merge(item.getItemId(), 1, Integer::sum) - 1;
				items.add(item.getBlueprintOccurrence() >= 0 ? item : item.withBlueprintOccurrence(ordinal));
			}
			tabs.add(items);
		});
		tab = selectedTab;
	}
	void selectTab(int tab) { this.tab = tab; }
	int itemCount(int tab) { return tabs.get(tab).size(); }
	List<BankPreviewItem> items() { return Collections.unmodifiableList(tabs.get(tab)); }
	List<Integer> ids() { return items().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()); }
	Map<Integer, List<Integer>> orders()
	{
		Map<Integer, List<Integer>> result = new LinkedHashMap<>();
		for (int changed : modified) result.put(changed, tabs.get(changed).stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		return result;
	}
	Map<String, BlueprintItemOrders.Destination> transfers() { return Collections.unmodifiableMap(transfers); }
	boolean canUndo() { return !undo.isEmpty(); }

	/** A clicked slot is the final index, unlike a drag insertion boundary. */
	void place(int from, int target, boolean swap)
	{
		List<BankPreviewItem> items = tabs.get(tab);
		if (from < 0 || from >= items.size() || target < 0 || target >= items.size() || from == target) return;
		remember();
		if (swap) Collections.swap(items, from, target);
		else items.add(target, items.remove(from));
		modified.add(tab);
	}

	/** Target is an insertion boundary in the original displayed list. */
	void move(int from, int boundary)
	{
		List<BankPreviewItem> items = tabs.get(tab);
		if (from < 0 || from >= items.size() || boundary < 0 || boundary > items.size()) return;
		int target = boundary > from ? boundary - 1 : boundary;
		if (target == from) return;
		remember();
		BankPreviewItem item = items.remove(from);
		items.add(target, item);
		modified.add(tab);
	}

	void moveAcross(int sourceTab, int from, int destinationTab, int boundary, String tag)
	{
		if (sourceTab < 0 || sourceTab >= tabs.size() || destinationTab < 0 || destinationTab >= tabs.size()) return;
		if (sourceTab == destinationTab) { selectTab(sourceTab); move(from, boundary); return; }
		if (from < 0 || from >= tabs.get(sourceTab).size() || boundary < 0
			|| boundary > tabs.get(destinationTab).size()) return;
		BankPreviewItem item = tabs.get(sourceTab).get(from);
		String key = BlueprintItemOrders.occurrenceKey(item, 0);
		BlueprintItemOrders.Destination prior = transfers.containsKey(key) ? transfers.get(key) : previous.get(key);
		String original = prior != null && prior.tag.equals(item.getLayoutTagKey())
			? prior.originalTag : item.getLayoutTagKey();
		BlueprintItemOrders.Destination destination = new BlueprintItemOrders.Destination(destinationTab, original, tag);
		remember();
		tabs.get(sourceTab).remove(from);
		transfers.put(key, destination);
		tabs.get(destinationTab).add(boundary, item.withLayoutTag(tag));
		modified.add(sourceTab);
		modified.add(destinationTab);
	}

	private void remember() { undo.push(new State(tabs, transfers, modified)); }
	void undo()
	{
		if (!canUndo()) return;
		State state = undo.pop();
		tabs = state.tabs;
		transfers = state.transfers;
		modified = state.modified;
	}
	private static final class State
	{
		final List<List<BankPreviewItem>> tabs = new ArrayList<>();
		final Map<String, BlueprintItemOrders.Destination> transfers;
		final Set<Integer> modified;
		State(List<List<BankPreviewItem>> source, Map<String, BlueprintItemOrders.Destination> transfers,
			Set<Integer> modified)
		{
			for (List<BankPreviewItem> items : source) tabs.add(new ArrayList<>(items));
			this.transfers = new LinkedHashMap<>(transfers);
			this.modified = new LinkedHashSet<>(modified);
		}
	}
}

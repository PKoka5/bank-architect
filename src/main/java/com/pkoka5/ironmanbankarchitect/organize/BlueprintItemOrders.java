package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;
import java.util.List;
import net.runelite.api.gameval.ItemID;

/** Saved physical item order by blueprint tab. Missing entries stay dormant. */
public final class BlueprintItemOrders
{
	public static final BlueprintItemOrders EMPTY = new BlueprintItemOrders(Collections.emptyMap(), null);
	public static final int BANK_CAPACITY = 1410;
	private final Map<Integer, List<Integer>> orders;
	private final String futureValue;
	private final Map<String, Destination> destinations;

	private BlueprintItemOrders(Map<Integer, List<Integer>> orders, String futureValue)
	{
		this(orders, futureValue, Collections.emptyMap());
	}

	private BlueprintItemOrders(Map<Integer, List<Integer>> orders, String futureValue,
		Map<String, Destination> destinations)
	{
		Map<Integer, List<Integer>> copy = new LinkedHashMap<>();
		orders.forEach((tab, ids) -> copy.put(tab, Collections.unmodifiableList(new ArrayList<>(ids))));
		this.orders = Collections.unmodifiableMap(copy);
		this.futureValue = futureValue;
		this.destinations = Collections.unmodifiableMap(new LinkedHashMap<>(destinations));
	}

	public static BlueprintItemOrders parse(String value)
	{
		if (value == null || value.isEmpty()) return EMPTY;
		if (!value.startsWith("v1|") && !value.startsWith("v2|")) return new BlueprintItemOrders(Collections.emptyMap(), value);
		Map<Integer, List<Integer>> parsed = new LinkedHashMap<>();
		Map<String, Destination> routes = new LinkedHashMap<>();
		for (String entry : value.substring(3).split("\\|"))
		{
			try
			{
				if (entry.startsWith("r:") || value.startsWith("v2|") && entry.startsWith("c:"))
				{
					String[] fields = entry.split(":", -1);
					if (fields.length == 5)
					{
						validateKey(fields[1]);
						routes.put(fields[1], new Destination(Integer.parseInt(fields[2]), fields[3], fields[4], entry.startsWith("c:")));
					}
					continue;
				}
				String[] parts = entry.split(":", 2);
				if (parts.length != 2) continue;
				int tab = Integer.parseInt(parts[0]);
				if (tab < 0 || tab >= BankLayoutPlan.DESTINATION_COUNT) continue;
				List<Integer> ids = new ArrayList<>();
				for (String id : parts[1].split(","))
				{
					int itemId = Integer.parseInt(id);
					if (itemId <= 0 || itemId == ItemID.BANK_FILLER) throw new IllegalArgumentException();
					ids.add(itemId);
				}
				parsed.put(tab, ids);
			}
			catch (IllegalArgumentException damagedEntry)
			{
				// Preserve other valid tabs when one entry is damaged.
			}
		}
		return new BlueprintItemOrders(parsed, null, routes);
	}

	public boolean isSupported() { return futureValue == null; }
	public boolean hasTab(int tab) { return orders.containsKey(tab); }
	public boolean isCaptured() { return destinations.values().stream().anyMatch(target -> target.captured); }

	/** The current bank's physical tabs become the target, without changing item categories. */
	public static BlueprintItemOrders capture(Map<Integer, List<Integer>> physicalTabs,
		BankOrganizationPreview preview, BlueprintItemOrders previous)
	{
		Map<Integer, Deque<BankPreviewItem>> available = new LinkedHashMap<>();
		Map<Integer, Integer> occurrences = new LinkedHashMap<>();
		preview.getCategories().forEach(category -> category.getItems().forEach(item -> {
			if (item.isBlank()) return;
			int ordinal = occurrences.merge(item.getItemId(), 1, Integer::sum) - 1;
			available.computeIfAbsent(item.getItemId(), key -> new ArrayDeque<>())
				.add(item.getBlueprintOccurrence() < 0 ? item.withBlueprintOccurrence(ordinal) : item);
		}));
		BlueprintItemOrders result = EMPTY;
		Map<String, Destination> routes = new LinkedHashMap<>();
		occurrences.clear();
		List<Integer> physicalOrder = new ArrayList<>(physicalTabs.keySet());
		physicalOrder.sort(java.util.Comparator.comparingInt(tab -> tab == 0 ? BankLayoutPlan.DESTINATION_COUNT : tab));
		for (int tab : physicalOrder)
		{
			result = result.withTab(tab, physicalTabs.get(tab));
			for (int id : physicalTabs.get(tab))
			{
				Deque<BankPreviewItem> copies = available.get(id);
				if (copies == null || copies.isEmpty()) throw new IllegalArgumentException("Bank contents changed");
				BankPreviewItem item = copies.removeFirst();
				String key = occurrenceKey(item, 0);
				Destination prior = previous.destinations.get(key);
				String original = prior != null && prior.tag.equals(item.getLayoutTagKey())
					? prior.originalTag : item.getLayoutTagKey();
				key = id + "#" + (occurrences.merge(id, 1, Integer::sum) - 1);
				routes.put(key, new Destination(tab, original, item.getLayoutTagKey(), true));
			}
		}
		if (available.values().stream().anyMatch(copies -> !copies.isEmpty()))
			throw new IllegalArgumentException("Bank contents changed");
		return result.withDestinations(routes);
	}

	/** Empty order restores automatic placement; existing absent IDs are retained on edits. */
	public BlueprintItemOrders withTab(int tab, List<Integer> visibleOrder)
	{
		if (!isSupported()) throw new IllegalStateException("Blueprint order was saved by a newer version");
		if (tab < 0 || tab >= BankLayoutPlan.DESTINATION_COUNT || visibleOrder.size() > BANK_CAPACITY)
			throw new IllegalArgumentException("Invalid blueprint tab or size");
		Map<Integer, Integer> present = new LinkedHashMap<>();
		for (int id : visibleOrder)
		{
			if (id <= 0 || id == ItemID.BANK_FILLER) throw new IllegalArgumentException("Not a bank item");
			present.merge(id, 1, Integer::sum);
		}
		Map<Integer, List<Integer>> changed = new LinkedHashMap<>(orders);
		if (visibleOrder.isEmpty()) changed.remove(tab);
		else
		{
			List<Integer> merged = new ArrayList<>();
			int next = 0;
			for (int old : orders.getOrDefault(tab, Collections.emptyList()))
			{
				int remaining = present.getOrDefault(old, 0);
				if (remaining > 0)
				{
					present.put(old, remaining - 1);
					merged.add(visibleOrder.get(next++));
				}
				else merged.add(old);
			}
			merged.addAll(visibleOrder.subList(next, visibleOrder.size()));
			changed.put(tab, merged);
		}
		return new BlueprintItemOrders(changed, null, destinations);
	}

	public Map<String, Destination> destinations() { return destinations; }

	public BlueprintItemOrders withDestinations(Map<String, Destination> changes)
	{
		if (!isSupported()) throw new IllegalStateException("Newer blueprint format");
		Map<String, Destination> routes = new LinkedHashMap<>(destinations);
		changes.forEach((key, target) -> { validateKey(key); routes.put(key, target); });
		return new BlueprintItemOrders(orders, null, routes);
	}

	/** Reset also releases incoming per-item destinations for this tab. */
	public BlueprintItemOrders resetTab(int tab)
	{
		BlueprintItemOrders cleared = withTab(tab, Collections.emptyList());
		Map<String, Destination> routes = new LinkedHashMap<>(destinations);
		routes.values().removeIf(destination -> destination.tab == tab);
		return new BlueprintItemOrders(cleared.orders, null, routes);
	}

	public static String occurrenceKey(BankPreviewItem item, int fallbackOccurrence)
	{
		return item.getItemId() + "#" + (item.getBlueprintOccurrence() >= 0
			? item.getBlueprintOccurrence() : fallbackOccurrence);
	}

	private static void validateKey(String key)
	{
		String[] parts = key.split("#", -1);
		if (parts.length != 2 || Integer.parseInt(parts[0]) <= 0
			|| Integer.parseInt(parts[1]) < 0 || Integer.parseInt(parts[1]) >= BANK_CAPACITY)
			throw new IllegalArgumentException("Invalid physical item identity");
	}

	public static final class Destination
	{
		public final int tab;
		public final String originalTag;
		public final String tag;
		private final boolean captured;
		public Destination(int tab, String originalTag, String tag)
		{
			this(tab, originalTag, tag, false);
		}
		private Destination(int tab, String originalTag, String tag, boolean captured)
		{
			if (tab < 0 || tab >= BankLayoutPlan.DESTINATION_COUNT) throw new IllegalArgumentException("Invalid tab");
			BankTags.byKey(originalTag);
			BankTags.byKey(tag);
			this.tab = tab;
			this.originalTag = originalTag;
			this.tag = tag;
			this.captured = captured;
		}
	}

	public BankOrganizationPreview apply(BankOrganizationPreview preview)
	{
		return apply(preview, BankLayoutPlan.defaultFor(preview.getPreset()));
	}

	public BankOrganizationPreview apply(BankOrganizationPreview preview, BankLayoutPlan plan)
	{
		if (orders.isEmpty() && destinations.isEmpty()) return preview;
		if (!destinations.isEmpty()) preview = route(preview, plan);
		List<BankCategoryPreview> tabs = new ArrayList<>();
		for (int tab = 0; tab < preview.getCategories().size(); tab++)
		{
			BankCategoryPreview original = preview.getCategories().get(tab);
			if (!orders.containsKey(tab)) { tabs.add(original); continue; }
			Map<Integer, Deque<BankPreviewItem>> available = new LinkedHashMap<>();
			for (BankPreviewItem item : original.getItems())
				available.computeIfAbsent(item.getItemId(), key -> new ArrayDeque<>()).add(item);
			List<BankPreviewItem> result = new ArrayList<>();
			for (int id : orders.get(tab))
			{
				Deque<BankPreviewItem> copies = available.get(id);
				if (copies != null && !copies.isEmpty()) result.add(copies.removeFirst());
			}
			// New occurrences append in their original curated order, not by ID.
			for (BankPreviewItem item : original.getItems())
			{
				Deque<BankPreviewItem> copies = available.get(item.getItemId());
				if (!copies.isEmpty() && copies.peekFirst() == item) result.add(copies.removeFirst());
			}
			tabs.add(new BankCategoryPreview(original.getCategory(), result, true));
		}
		Map<String, List<BankBlockDescriptor>> descriptors = new LinkedHashMap<>(preview.getBlockDescriptors());
		for (int tab = 0; tab < tabs.size(); tab++)
			if (tabs.get(tab).hasManualOrder())
				for (String tag : plan.getTagKeys(tab)) descriptors.remove(tag);
		return new BankOrganizationPreview(preview.getPreset(), tabs, preview.getTagCounts(), descriptors);
	}

	private BankOrganizationPreview route(BankOrganizationPreview preview, BankLayoutPlan plan)
	{
		boolean captured = isCaptured();
		List<List<BankPreviewItem>> routed = new ArrayList<>();
		for (int i = 0; i < preview.getCategories().size(); i++) routed.add(new ArrayList<>());
		Map<Integer, Integer> occurrences = new LinkedHashMap<>();
		java.util.Set<Integer> modified = new java.util.HashSet<>();
		for (int tab = 0; tab < preview.getCategories().size(); tab++)
		{
			for (BankPreviewItem item : preview.getCategories().get(tab).getItems())
			{
				if (captured && item.isBlank()) continue;
				int occurrence = occurrences.merge(item.getItemId(), 1, Integer::sum) - 1;
				BankPreviewItem identified = item.withBlueprintOccurrence(occurrence);
				Destination target = destinations.get(occurrenceKey(identified, occurrence));
				if (target != null && target.tab < routed.size()
					&& (target.captured || target.originalTag.equals(item.getLayoutTagKey())
						&& plan != null && plan.destinationOf(target.tag) == target.tab))
				{
					routed.get(target.tab).add(target.originalTag.equals(item.getLayoutTagKey())
						? identified.withLayoutTag(target.tag) : identified);
					modified.add(tab);
					modified.add(target.tab);
				}
				else routed.get(tab).add(identified);
			}
		}
		Map<String, java.util.Set<Integer>> counted = new LinkedHashMap<>();
		List<BankCategoryPreview> tabs = new ArrayList<>();
		for (int tab = 0; tab < routed.size(); tab++)
		{
			for (BankPreviewItem item : routed.get(tab))
				if (!item.isPlaceholder() && item.getLayoutTagKey() != null)
					counted.computeIfAbsent(item.getLayoutTagKey(), key -> new java.util.HashSet<>()).add(item.getItemId());
			tabs.add(new BankCategoryPreview(preview.getCategories().get(tab).getCategory(), routed.get(tab), modified.contains(tab)));
		}
		Map<String, Integer> counts = new LinkedHashMap<>();
		counted.forEach((tag, ids) -> counts.put(tag, ids.size()));
		return new BankOrganizationPreview(preview.getPreset(), tabs, counts, preview.getBlockDescriptors());
	}

	public String serialize()
	{
		if (futureValue != null) return futureValue;
		if (orders.isEmpty() && destinations.isEmpty()) return "";
		StringBuilder out = new StringBuilder(isCaptured() ? "v2" : "v1");
		orders.forEach((tab, ids) -> {
			out.append('|').append(tab).append(':');
			for (int i = 0; i < ids.size(); i++)
			{
				if (i > 0) out.append(',');
				out.append(ids.get(i));
			}
		});
		destinations.forEach((key, target) -> out.append(target.captured ? "|c:" : "|r:").append(key).append(':')
			.append(target.tab).append(':').append(target.originalTag).append(':').append(target.tag));
		return out.toString();
	}
}

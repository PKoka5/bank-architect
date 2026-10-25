package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import java.util.*;

/** Reviewed primary equipment preferences; independent of disposal and general gear scores. */
final class PrimaryGearSelector
{
	private static final List<RequiredResource<Map<Integer,int[]>>> TABLES = Arrays.asList(
		table("melee"),table("ranged"),table("magic"));
	private PrimaryGearSelector() { }
	private static RequiredResource<Map<Integer,int[]>> table(String style)
	{
		return new RequiredResource<>(style + " equipment candidates", () -> load(
			PrimaryGearSelector.class.getResourceAsStream(style + "-primary-candidates.tsv")));
	}
	private static Map<Integer,int[]> facts(GearStyle style) { return TABLES.get(style.ordinal()).get(); }

	static Map<Integer, int[]> load(java.io.InputStream input)
	{
		Map<Integer, int[]> result = new HashMap<>();
		new OrderedItemFamilies(input, 0).entries().forEach((key, ids) -> {
			if (!key.matches("([0-8]|12)/[012]/[0-9]{1,2}/[0-9]{1,4}/[0-9]/[012]/[a-z0-9][a-z0-9-]*"))
				throw new IllegalArgumentException("Invalid equipment group: " + key);
			String[] fields = key.split("/");
			int[] fact = Arrays.stream(fields).limit(6).mapToInt(Integer::parseInt).toArray();
			if (fact[0] != 8 && (fact[4] != 0 || fact[5] != 0)
				|| fact[0] == 8 && fact[1] > 0 && fact[5] == 0
				|| fact[1] == 0 && (fact[2] != 0 || fact[3] != 0 || fact[4] != 0 || fact[5] != 0))
				throw new IllegalArgumentException("Invalid weapon facts: " + key);
			for (int id : ids) result.put(id, fact);
		});
		for (int slot : new int[]{0,1,2,3,4,5,6,7,8,12})
			if (result.values().stream().noneMatch(fact -> fact[0] == slot))
				throw new IllegalArgumentException("Missing equipment slot: " + slot);
		return Collections.unmodifiableMap(result);
	}

	static boolean oneHanded(int itemId)
	{
		return oneHanded(GearStyle.MELEE,itemId);
	}
	static boolean oneHanded(GearStyle style, int itemId)
	{
		int[] fact = facts(style).get(itemId);
		return fact != null && fact[0] == 8 && fact[1] > 0 && fact[5] == 1;
	}

	static boolean candidate(int itemId, int slot)
	{
		return candidate(GearStyle.MELEE,itemId,slot);
	}
	static boolean candidate(GearStyle style, int itemId, int slot)
	{
		int[] fact = facts(style).get(itemId);
		return fact != null && fact[0] == slot && fact[1] > 0;
	}
	static int slot(GearStyle style, int itemId)
	{
		int[] fact = facts(style).get(itemId);
		return fact != null && fact[1] > 0 ? fact[0] : -1;
	}

	static void sort(int slot, List<BankPreviewItem> items, GearStatsSource source, Comparator<BankPreviewItem> legacy)
	{
		sort(GearStyle.MELEE,slot,items,source,legacy);
	}
	static void sort(GearStyle style, int slot, List<BankPreviewItem> items, GearStatsSource source, Comparator<BankPreviewItem> legacy)
	{
		Map<Integer, int[]> facts = facts(style);
		items.removeIf(item -> facts.containsKey(item.getItemId()) && facts.get(item.getItemId())[1] == 0);
		Map<Integer, GearStats> stats = new HashMap<>();
		Map<Integer, Boolean> complete = new HashMap<>();
		Map<Integer, Integer> ticks = new HashMap<>();
		for (BankPreviewItem item : items)
		{
			int[] fact = facts.get(item.getItemId());
			if (fact == null || fact[0] != slot) continue;
			int band = fact[1] * 100 + fact[2];
			Optional<GearStats> value = source.statsFor(item.getItemId());
			if (value.isPresent()) stats.put(item.getItemId(), value.get());
			boolean usable = value.isPresent() && (slot != 8 || fact[1] == 2
				&& fact[4] > 0 && value.get().attackSpeed() == fact[4]);
			complete.merge(band, usable, (left, right) -> left && right);
			Integer previous = ticks.putIfAbsent(band, fact[4]);
			if (slot == 8 && previous != null && previous != fact[4]) complete.put(band, false);
		}
		Map<Integer, int[]> keys = new HashMap<>();
		for (BankPreviewItem item : items)
		{
			int id = item.getItemId();
			int[] fact = facts.get(id);
			int[] key = new int[7];
			if (fact != null && fact[0] == slot)
			{
				// Weapon navigation allows advanced two-handers ahead of budget one-handers.
				key[0] = slot == 8 ? fact[2] : fact[1];
				key[1] = slot == 8 ? fact[1] : fact[2];
				key[2] = fact[3];
				key[6] = style == GearStyle.MELEE ? -id : 0;
				if (complete.get(fact[1] * 100 + fact[2]))
				{
					GearStats value = stats.get(id);
					key[2] = value.power(style);
					key[3] = value.accuracy(style);
					key[4] = value.prayerBonus();
					key[5] = value.defenceSum();
				}
			}
			keys.put(id, key);
		}
		items.sort((left, right) -> {
			int order = Arrays.compare(keys.get(right.getItemId()), keys.get(left.getItemId()));
			return order == 0 ? legacy.compare(left, right) : order;
		});
		if (items.isEmpty()) return;
		BankPreviewItem chosen = items.get(0);
		int[] fact = facts.get(chosen.getItemId());
		if (fact == null || fact[0] != slot) return;
		int[] key = keys.get(chosen.getItemId());
		String reason = complete.get(fact[1] * 100 + fact[2]) ? (style == GearStyle.MAGIC ? "Magic damage (tenths %) " : "Strength ") + key[2] + ", attack " + key[3]
			+ ", Prayer " + key[4] + ", defence " + key[5] + "."
			: "Reviewed order; some candidate stats are unavailable.";
		if (style == GearStyle.MELEE && slot == 8 && fact[1] == 1) reason = fact[2] == 24
			? "Reviewed advanced two-handed fallback ahead of earlier one-handers. Target effects are not assumed."
			: "Reviewed specialist fallback; target effects are not assumed.";
		else if (slot == 8 && !complete.get(fact[1] * 100 + fact[2]))
			reason = "Reviewed role and speed order; complete comparable weapon stats are unavailable.";
		String label = new String[]{"head","body","legs","cape","neck","shield","gloves","boots","weapon","","","","ring"}[slot];
		String preference = style != GearStyle.MELEE ? slot == 8 ? "Reviewed weapon layout preference; damage varies by target and ammunition or spell. " : ""
			: slot == 4 && fact[2] == 20 ? "All-round amulet preference. "
			: slot == 5 && fact[2] == 10 ? "Offensive defender preference. "
			: slot == 8 && fact[1] == 2 ? "Reviewed one-handed layout preference; damage varies by target. " : "";
		String name = style == GearStyle.MELEE ? "Melee" : style == GearStyle.RANGED ? "Ranged" : "Magic";
		items.set(0, chosen.withPlacementReason(chosen.getPlacementReason() + " General " + name + " " + label + ": "
			+ (fact[1] == 1 ? "fallback candidate. " : "reviewed candidate. ")
			+ preference + reason));
	}
}

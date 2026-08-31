package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable item lookup and ranking facts for one combat-layout planning pass. */
final class CombatGearIndex
{
	private final List<BankPreviewItem> items;
	private final Map<Integer, BankPreviewItem> itemById;
	private final Map<Integer, ItemFacts> factsByItemId;

	CombatGearIndex(List<BankPreviewItem> items, GearStatsSource gearStats)
	{
		List<BankPreviewItem> ownedItems = new ArrayList<>(items);
		Map<Integer, BankPreviewItem> byId = new LinkedHashMap<>();
		Map<Integer, ItemFacts> facts = new LinkedHashMap<>();
		for (BankPreviewItem item : ownedItems)
		{
			int score = item.isPlaceholder() ? 0 : CombatGearRanking.score(item, gearStats);
			int activeScore = item.isPlaceholder()
				? 0 : CombatGearRanking.score(item, gearStats, true);
			int utilityScore = item.isPlaceholder()
				? 0 : CombatGearUtilityCatalog.INSTANCE.itemScore(item.getItemId());
			byId.put(item.getItemId(), item);
			facts.put(item.getItemId(), new ItemFacts(
				CombatGearRanking.style(item, gearStats),
				CombatGearRanking.slot(item, gearStats),
				score, activeScore, utilityScore));
		}
		this.items = Collections.unmodifiableList(ownedItems);
		this.itemById = Collections.unmodifiableMap(byId);
		this.factsByItemId = Collections.unmodifiableMap(facts);
	}

	List<BankPreviewItem> items()
	{
		return items;
	}

	Map<Integer, BankPreviewItem> itemById()
	{
		return itemById;
	}

	GearStyle style(BankPreviewItem item)
	{
		return facts(item).style;
	}

	int slot(BankPreviewItem item)
	{
		return facts(item).slot;
	}

	int score(BankPreviewItem item)
	{
		return facts(item).score;
	}

	int activeScore(BankPreviewItem item)
	{
		return facts(item).activeScore;
	}

	int utilityScore(BankPreviewItem item)
	{
		return facts(item).utilityScore;
	}

	private ItemFacts facts(BankPreviewItem item)
	{
		ItemFacts facts = factsByItemId.get(item.getItemId());
		if (facts == null)
		{
			throw new IllegalArgumentException("Item is not part of this combat planning pass: "
				+ item.getItemId());
		}
		return facts;
	}

	private static final class ItemFacts
	{
		private final GearStyle style;
		private final int slot;
		private final int score;
		private final int activeScore;
		private final int utilityScore;

		private ItemFacts(GearStyle style, int slot, int score, int activeScore,
			int utilityScore)
		{
			this.style = style;
			this.slot = slot;
			this.score = score;
			this.activeScore = activeScore;
			this.utilityScore = utilityScore;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lays out the farming/herblore tab as one production row per potion recipe:
 * grimy herb, clean herb, seed, unfinished potion, secondaries — the order a
 * player works through the chain. Rows are padded to the full grid width with
 * leftover items so the recipe rows stay aligned after bank compaction; once
 * filler runs out the remaining items continue densely.
 */
final class HerbloreItemSorter
{
	private static final int GRID_COLUMNS = 8;
	private static final int MIN_CHAIN_CELLS = 2;

	// Herb level order, low to high. Secondaries listed per chain.
	private static final Chain[] CHAINS = {
		chain("guam", "eye of newt"),
		chain("marrentill", "unicorn horn"),
		chain("tarromin", "limpwurt"),
		chain("harralander", "red spiders"),
		chain("ranarr", "snape grass"),
		chain("toadflax", "crushed nest"),
		chain("irit", "eye of newt"),
		chain("avantoe", "mort myre fungus"),
		chain("kwuarm", "limpwurt"),
		chain("snapdragon", "red spiders"),
		chain("cadantine", "white berries"),
		chain("lantadyme", "dragon scale"),
		chain("dwarf weed", "wine of zamorak"),
		chain("torstol", "vial of water")
	};

	private HerbloreItemSorter()
	{
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		Set<BankPreviewItem> unused = new LinkedHashSet<>(items);
		List<List<BankPreviewItem>> recipeRows = new ArrayList<>();
		for (Chain chain : CHAINS)
		{
			List<BankPreviewItem> cells = matchChain(chain, unused);
			if (cells.size() >= MIN_CHAIN_CELLS)
			{
				recipeRows.add(cells);
				unused.removeAll(cells);
			}
		}

		List<BankPreviewItem> filler = new ArrayList<>(unused);
		filler.sort(Comparator
			.comparingInt((BankPreviewItem item) -> PresetItemSorter.subgroupRank("farming-herblore", item))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));

		List<BankPreviewItem> laidOut = new ArrayList<>();
		int fillerIndex = 0;
		for (List<BankPreviewItem> cells : recipeRows)
		{
			laidOut.addAll(cells);
			int padding = GRID_COLUMNS - cells.size();
			if (padding > 0 && fillerIndex + padding <= filler.size())
			{
				laidOut.addAll(filler.subList(fillerIndex, fillerIndex + padding));
				fillerIndex += padding;
			}
		}

		laidOut.addAll(filler.subList(fillerIndex, filler.size()));
		return laidOut;
	}

	private static List<BankPreviewItem> matchChain(Chain chain, Set<BankPreviewItem> unused)
	{
		BankPreviewItem grimy = null;
		BankPreviewItem clean = null;
		BankPreviewItem seed = null;
		BankPreviewItem unfinished = null;
		List<BankPreviewItem> secondaries = new ArrayList<>();

		for (BankPreviewItem item : unused)
		{
			String name = normalizedName(item.getDisplayName());
			if (name.contains(chain.herb))
			{
				if (name.contains("grimy"))
				{
					grimy = first(grimy, item);
				}
				else if (name.contains("seed"))
				{
					seed = first(seed, item);
				}
				else if (name.contains("unf"))
				{
					unfinished = first(unfinished, item);
				}
				else if (!name.contains("potion"))
				{
					clean = first(clean, item);
				}
				continue;
			}
			for (String secondary : chain.secondaries)
			{
				if (name.contains(secondary))
				{
					secondaries.add(item);
					break;
				}
			}
		}

		List<BankPreviewItem> cells = new ArrayList<>();
		addIfPresent(cells, grimy);
		addIfPresent(cells, clean);
		addIfPresent(cells, seed);
		addIfPresent(cells, unfinished);
		for (BankPreviewItem secondary : secondaries)
		{
			if (cells.size() >= GRID_COLUMNS)
			{
				break;
			}
			cells.add(secondary);
		}

		return cells;
	}

	private static BankPreviewItem first(BankPreviewItem current, BankPreviewItem candidate)
	{
		return current == null ? candidate : current;
	}

	private static void addIfPresent(List<BankPreviewItem> cells, BankPreviewItem item)
	{
		if (item != null)
		{
			cells.add(item);
		}
	}

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}

	private static Chain chain(String herb, String... secondaries)
	{
		return new Chain(herb, secondaries);
	}

	private static final class Chain
	{
		private final String herb;
		private final String[] secondaries;

		private Chain(String herb, String[] secondaries)
		{
			this.herb = herb;
			this.secondaries = secondaries;
		}
	}
}

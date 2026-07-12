package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lays out owned Herblore chains in stable eight-cell rows:
 * grimy, clean, seed, unfinished, secondary, dose 3, dose 2, dose 1.
 * Missing cells are filled with non-chain farming items because the real bank
 * compacts empty slots. If a full row cannot be filled, the remainder stays dense.
 */
final class HerbloreItemSorter
{
	private static final int GRID_COLUMNS = 8;
	private static final int MIN_CHAIN_CELLS = 2;

	private static final Chain[] CHAINS = {
		chain("guam", "attack potion", "eye of newt"),
		chain("marrentill", "antipoison", "unicorn horn"),
		chain("tarromin", "strength potion", "limpwurt"),
		chain("harralander", "energy potion", "chocolate dust"),
		chain("ranarr", "prayer potion", "snape grass"),
		chain("toadflax", "saradomin brew", "crushed nest"),
		chain("irit", "super attack", "eye of newt"),
		chain("avantoe", "super energy", "mort myre fungus"),
		chain("kwuarm", "super strength", "limpwurt"),
		chain("snapdragon", "super restore", "red spiders"),
		chain("cadantine", "super defence", "white berries"),
		chain("lantadyme", "magic potion", "potato cactus"),
		chain("dwarf weed", "ranging potion", "wine of zamorak"),
		chain("torstol", "super combat", "vial of water")
	};

	private HerbloreItemSorter()
	{
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		Set<BankPreviewItem> unused = new LinkedHashSet<>(items);
		List<RecipeRow> recipeRows = new ArrayList<>();
		// Allocate shared secondaries from the highest-tier owned chain down.
		// Insert accepted rows at the front so the final visual order remains
		// low-to-high even though ownership is resolved high-to-low.
		for (int chainIndex = CHAINS.length - 1; chainIndex >= 0; chainIndex--)
		{
			Chain chain = CHAINS[chainIndex];
			RecipeRow row = matchChain(chain, unused);
			if (row.itemCount() >= MIN_CHAIN_CELLS && row.hasHerbInput())
			{
				recipeRows.add(0, row);
				unused.removeAll(row.items());
			}
		}

		List<BankPreviewItem> filler = new ArrayList<>(unused);
		filler.sort(Comparator
			.comparingInt((BankPreviewItem item) -> PresetItemSorter.subgroupRank("farming-herblore", item))
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));

		List<BankPreviewItem> laidOut = new ArrayList<>();
		int fillerIndex = 0;
		for (int rowIndex = 0; rowIndex < recipeRows.size(); rowIndex++)
		{
			RecipeRow row = recipeRows.get(rowIndex);
			int missing = GRID_COLUMNS - row.itemCount();
			if (fillerIndex + missing > filler.size())
			{
				for (int remaining = rowIndex; remaining < recipeRows.size(); remaining++)
				{
					laidOut.addAll(recipeRows.get(remaining).items());
				}
				laidOut.addAll(filler.subList(fillerIndex, filler.size()));
				return laidOut;
			}

			for (BankPreviewItem cell : row.cells)
			{
				laidOut.add(cell == null ? filler.get(fillerIndex++) : cell);
			}
		}

		laidOut.addAll(filler.subList(fillerIndex, filler.size()));
		return laidOut;
	}

	private static RecipeRow matchChain(Chain chain, Set<BankPreviewItem> unused)
	{
		BankPreviewItem[] cells = new BankPreviewItem[GRID_COLUMNS];
		for (BankPreviewItem item : unused)
		{
			String name = normalizedName(item.getDisplayName());
			if (name.contains(chain.herb))
			{
				if (name.contains("grimy"))
				{
					cells[0] = first(cells[0], item);
				}
				else if (name.contains("seed"))
				{
					cells[2] = first(cells[2], item);
				}
				else if (name.contains("unf"))
				{
					cells[3] = first(cells[3], item);
				}
				else if (!name.contains("potion"))
				{
					cells[1] = first(cells[1], item);
				}
				continue;
			}

			if (name.contains(chain.product))
			{
				int dose = doseOf(name);
				if (dose >= 1 && dose <= 3)
				{
					cells[8 - dose] = first(cells[8 - dose], item);
				}
				continue;
			}

			for (String secondary : chain.secondaries)
			{
				if (!name.endsWith(" seed") && name.contains(secondary))
				{
					cells[4] = first(cells[4], item);
					break;
				}
			}
		}
		return new RecipeRow(cells);
	}

	private static int doseOf(String name)
	{
		int length = name.length();
		if (length >= 3 && name.charAt(length - 3) == '(' && name.charAt(length - 1) == ')')
		{
			char value = name.charAt(length - 2);
			return value >= '1' && value <= '4' ? value - '0' : -1;
		}
		return -1;
	}

	private static BankPreviewItem first(BankPreviewItem current, BankPreviewItem candidate)
	{
		return current == null ? candidate : current;
	}

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}

	private static Chain chain(String herb, String product, String... secondaries)
	{
		return new Chain(herb, product, secondaries);
	}

	private static final class Chain
	{
		private final String herb;
		private final String product;
		private final String[] secondaries;

		private Chain(String herb, String product, String[] secondaries)
		{
			this.herb = herb;
			this.product = product;
			this.secondaries = secondaries;
		}
	}

	private static final class RecipeRow
	{
		private final BankPreviewItem[] cells;

		private RecipeRow(BankPreviewItem[] cells)
		{
			this.cells = cells;
		}

		private int itemCount()
		{
			return items().size();
		}

		private boolean hasHerbInput()
		{
			return cells[0] != null || cells[1] != null || cells[2] != null || cells[3] != null;
		}

		private List<BankPreviewItem> items()
		{
			List<BankPreviewItem> items = new ArrayList<>();
			for (BankPreviewItem cell : cells)
			{
				if (cell != null)
				{
					items.add(cell);
				}
			}
			return items;
		}
	}
}

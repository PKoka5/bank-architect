package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lays out owned Herblore chains in stable eight-cell rows:
 * grimy, clean, seed, unfinished, secondary, dose 3, dose 2, dose 1.
 * Complete recipes retain physical rows. Incomplete recipes stay dense and are
 * followed by other Herblore items, then Farming spillover; unrelated seeds or
 * produce therefore never masquerade as missing recipe cells.
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
		chain("torstol", "super combat potion")
	};

	/**
	 * The order the runs appear in when the tab is grouped by kind rather than by
	 * recipe: the chain a herb actually travels, with the seeds behind it.
	 */
	private static final List<String> RUN_ORDER = java.util.Arrays.asList(
		"grimy-herbs", "clean-herbs", "unfinished-potions", "secondaries",
		"herb-seeds", "potion-doses", "herblore-other");

	private HerbloreItemSorter()
	{
	}

	/**
	 * Every kind of Herblore item in one run, rather than a row per recipe.
	 *
	 * <p>Recipe rows only pay for themselves while the doses sit on the same tab
	 * to finish them. Once the player keeps their potions elsewhere, a row per
	 * recipe is mostly gaps, and a plain run of all the grimy herbs followed by
	 * all the clean ones is both denser and easier to work through.</p>
	 */
	static List<BankPreviewItem> layoutByKind(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> herblore = new ArrayList<>();
		List<BankPreviewItem> farming = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			if (takesFarmingLayout(item))
			{
				farming.add(item);
			}
			else
			{
				herblore.add(item);
			}
		}

		herblore.sort(Comparator
			.comparingInt(HerbloreItemSorter::runRank)
			.thenComparing(item -> potionFamily(normalizedName(item.getDisplayName())))
			.thenComparing(Comparator.comparingInt(
				(BankPreviewItem item) -> doseOf(normalizedName(item.getDisplayName()))).reversed())
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));

		List<BankPreviewItem> laidOut = new ArrayList<>(herblore);
		laidOut.addAll(FarmingItemSorter.layout(farming, laidOut.size() % GRID_COLUMNS));
		return laidOut;
	}

	/**
	 * Whether {@link #layoutByKind} would place anything by column. It hands
	 * every farming item other than a herb seed to the farming layout, which
	 * keeps seed families off row edges by position.
	 */
	static boolean layoutByKindPlacesByColumn(List<BankPreviewItem> items)
	{
		for (BankPreviewItem item : items)
		{
			if (takesFarmingLayout(item))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean takesFarmingLayout(BankPreviewItem item)
	{
		return item.getItemCategory() == ItemCategory.FARMING
			&& !"herb-seeds".equals(runKey(item));
	}

	private static int runRank(BankPreviewItem item)
	{
		int index = RUN_ORDER.indexOf(runKey(item));
		return index < 0 ? RUN_ORDER.size() : index;
	}

	/** Which part of the chain an item belongs to, by the same rule the tags use. */
	private static String runKey(BankPreviewItem item)
	{
		return BankTags.tagFor("herblore", item.getSubcategory()).getKey();
	}

	static List<BankPreviewItem> layout(List<BankPreviewItem> items)
	{
		return layout(items, true);
	}

	/**
	 * The recipe rows. With row filling off, a short row is left short
	 * rather than padded out with whatever else the tab happens to hold.
	 */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items, boolean fillRows)
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

		List<BankPreviewItem> herbloreSpillover = new ArrayList<>();
		List<BankPreviewItem> farmingSpillover = new ArrayList<>();
		for (BankPreviewItem item : unused)
		{
			if (item.getItemCategory() == ItemCategory.FARMING)
			{
				farmingSpillover.add(item);
			}
			else
			{
				herbloreSpillover.add(item);
			}
		}
		Comparator<BankPreviewItem> spilloverOrder = Comparator
			.comparingInt(PresetItemSorter::herbloreSpilloverRank)
			.thenComparing(item -> potionFamily(normalizedName(item.getDisplayName())))
			.thenComparing(Comparator.comparingInt(
				(BankPreviewItem item) -> doseOf(normalizedName(item.getDisplayName()))).reversed())
			.thenComparing(item -> normalizedName(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId);
		herbloreSpillover.sort(spilloverOrder);
		farmingSpillover.sort(spilloverOrder);

		List<BankPreviewItem> laidOut = new ArrayList<>();
		// Exact recipes are the only rows whose semantic cells can occupy all
		// eight physical bank columns without unrelated filler.
		for (RecipeRow row : recipeRows)
		{
			if (row.isComplete())
			{
				laidOut.addAll(row.items());
			}
		}
		for (RecipeRow row : recipeRows)
		{
			if (!row.isComplete())
			{
				List<BankPreviewItem> rowItems = row.items();
				int usedColumns = laidOut.size() % GRID_COLUMNS;
				if (fillRows && usedColumns != 0 && usedColumns + rowItems.size() > GRID_COLUMNS)
				{
					while (laidOut.size() % GRID_COLUMNS != 0 && !herbloreSpillover.isEmpty())
					{
						laidOut.add(herbloreSpillover.remove(0));
					}
					while (laidOut.size() % GRID_COLUMNS != 0 && !farmingSpillover.isEmpty())
					{
						laidOut.add(farmingSpillover.remove(0));
					}
				}
				laidOut.addAll(rowItems);
			}
		}
		laidOut.addAll(herbloreSpillover);
		laidOut.addAll(FarmingItemSorter.layout(farmingSpillover,
			laidOut.size() % GRID_COLUMNS));
		return laidOut;
	}

	private static RecipeRow matchChain(Chain chain, Set<BankPreviewItem> unused)
	{
		BankPreviewItem[] cells = new BankPreviewItem[GRID_COLUMNS];
		for (BankPreviewItem item : unused)
		{
			String name = normalizedName(item.getDisplayName());
			int herbCell = metadataHerbCell(chain, item);
			if (herbCell < 0)
			{
				herbCell = herbCell(chain.herb, name);
			}
			if (herbCell >= 0)
			{
				cells[herbCell] = first(cells[herbCell], item);
				continue;
			}

			int dose = metadataPotionDose(chain, item);
			if (dose < 0 && potionFamily(name).equals(chain.product))
			{
				dose = doseOf(name);
			}
			if (dose >= 1 && dose <= 3)
			{
				cells[8 - dose] = first(cells[8 - dose], item);
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

	private static int metadataPotionDose(Chain chain, BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.DOSE)
			.filter(metadata -> chain.potionFamilyKey.equals(metadata.getFamilyKey()))
			.map(ItemSortMetadata::getVariantValue)
			.orElse(-1);
	}

	private static int metadataHerbCell(Chain chain, BankPreviewItem item)
	{
		return ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.filter(metadata -> metadata.getVariantKind() == ItemSortMetadata.VariantKind.WORKFLOW_STAGE)
			.filter(metadata -> chain.familyKey.equals(metadata.getFamilyKey()))
			.map(ItemSortMetadata::getVariantValue)
			.filter(stage -> stage >= 0 && stage <= 3)
			.orElse(-1);
	}

	private static int herbCell(String herb, String name)
	{
		if (name.equals(seedName(herb))) return 2;
		if (name.equals(herb + " potion (unf)")) return 3;
		if (isGrimyHerb(herb, name)) return 0;
		if (isCleanHerb(herb, name)) return 1;
		return -1;
	}

	private static String seedName(String herb)
	{
		return herb + " seed";
	}

	private static boolean isGrimyHerb(String herb, String name)
	{
		if ("guam".equals(herb)) return name.equals("grimy guam leaf") || name.equals("grimy guam");
		if ("ranarr".equals(herb)) return name.equals("grimy ranarr weed") || name.equals("grimy ranarr");
		if ("irit".equals(herb)) return name.equals("grimy irit leaf") || name.equals("grimy irit");
		return name.equals("grimy " + herb);
	}

	private static boolean isCleanHerb(String herb, String name)
	{
		if ("guam".equals(herb)) return name.equals("guam leaf") || name.equals("clean guam");
		if ("ranarr".equals(herb)) return name.equals("ranarr weed") || name.equals("clean ranarr");
		if ("irit".equals(herb)) return name.equals("irit leaf") || name.equals("clean irit")
			|| name.equals("irit");
		return name.equals(herb) || name.equals("clean " + herb);
	}

	private static String potionFamily(String name)
	{
		return doseOf(name) > 0 ? name.substring(0, name.length() - 3).trim() : name;
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
		return new Chain(herb, "herb." + herb.replace(' ', '_'),
			"potion." + product.replace(" potion", "").replace(' ', '_'), product, secondaries);
	}

	private static final class Chain
	{
		private final String herb;
		private final String familyKey;
		private final String potionFamilyKey;
		private final String product;
		private final String[] secondaries;

		private Chain(String herb, String familyKey, String potionFamilyKey, String product,
			String[] secondaries)
		{
			this.herb = herb;
			this.familyKey = familyKey;
			this.potionFamilyKey = potionFamilyKey;
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

		private boolean isComplete()
		{
			return itemCount() == GRID_COLUMNS;
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

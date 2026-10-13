package com.pkoka5.ironmanbankarchitect.organize;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.normalized;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.List;


/**
 * Lays out owned Herblore chains in stable eight-cell rows:
 * grimy, clean, seed, unfinished, secondary, dose 3, dose 2, dose 1.
 * Complete recipes retain physical rows. Everything left over is grouped by
 * kind, without incomplete recipe fragments or unrelated filler.
 */
final class HerbloreItemSorter
{
	private static final int GRID_COLUMNS = 8;

	private static final Chain[] CHAINS = {
		chain("guam", "attack potion", 221),
		chain("marrentill", "antipoison", 235),
		chain("tarromin", "strength potion", 225),
		chain("harralander", "energy potion", 1975),
		chain("ranarr", "prayer potion", 231),
		chain("toadflax", "saradomin brew", 6693),
		chain("irit", "super attack", 221),
		chain("avantoe", "super energy", 2970),
		chain("kwuarm", "super strength", 225),
		chain("snapdragon", "super restore", 223),
		chain("cadantine", "super defence", 239),
		chain("lantadyme", "magic potion", 3138),
		chain("dwarf weed", "ranging potion", 245),
		chain("torstol", "super combat potion", 0)
	};

	/**
	 * The order the runs appear in when the tab is grouped by kind rather than by
	 * recipe: the chain a herb actually travels, with the seeds behind it.
	 */
	private static final List<String> RUN_ORDER = java.util.Arrays.asList(
		"grimy-herbs", "clean-herbs", "herb-seeds", "unfinished-potions",
		"secondaries", "potion-doses", "herblore-other");

	private HerbloreItemSorter()
	{
	}

	static List<BankPreviewItem> layoutMain(List<BankPreviewItem> items)
	{
		List<BankPreviewItem> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparingInt(HerbloreItemSorter::mainStage)
			.thenComparingInt(item -> {
				String family = ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
					.map(ItemSortMetadata::getFamilyKey).orElse("");
				for (int index = 0; index < CHAINS.length; index++)
					if (family.equals(CHAINS[index].familyKey)) return index;
				return CHAINS.length;
			})
			.thenComparing(item -> normalized(item.getDisplayName()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return sorted;
	}

	private static int mainStage(BankPreviewItem item)
	{
		String tag = "herb-seed".equals(item.getSubcategory()) ? "herb-seeds"
			: BankTags.tagFor(item.getItemCategory() == ItemCategory.FARMING ? "seeds-farming" : "herblore",
				item.getSubcategory()).getKey();
		int rank = java.util.Arrays.asList("grimy-herbs", "clean-herbs", "herb-seeds", "seeds",
			"unfinished-potions", "secondaries", "herblore-other", "produce").indexOf(tag);
		return rank < 0 ? 8 : rank;
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
			.thenComparingInt(HerbloreItemSorter::chainRank)
			.thenComparing(item -> potionFamily(normalized(item.getDisplayName())))
			.thenComparing(Comparator.comparingInt(
				(BankPreviewItem item) -> doseOf(normalized(item.getDisplayName()))).reversed())
			.thenComparing(item -> normalized(item.getDisplayName()))
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

	private static int chainRank(BankPreviewItem item)
	{
		String family = ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId())
			.map(ItemSortMetadata::getFamilyKey).orElse("");
		String name = normalized(item.getDisplayName());
		for (int index = 0; index < CHAINS.length; index++)
			if (family.equals(CHAINS[index].familyKey) || herbCell(CHAINS[index].herb, name) >= 0) return index;
		return CHAINS.length;
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
	 * Only complete recipes claim a physical row; a disabled row option keeps
	 * the entire tab grouped by kind. Placeholders still count as owned cells.
	 */
	static List<BankPreviewItem> layout(List<BankPreviewItem> items, boolean fillRows)
	{
		if (!fillRows) return layoutByKind(items);
		Set<BankPreviewItem> unused = new LinkedHashSet<>(items);
		List<BankPreviewItem> laidOut = new ArrayList<>();
		// Allocate shared secondaries from the highest-tier owned chain down.
		// Insert accepted rows at the front so the final visual order remains
		// low-to-high even though ownership is resolved high-to-low.
		for (int chainIndex = CHAINS.length - 1; chainIndex >= 0; chainIndex--)
		{
			Chain chain = CHAINS[chainIndex];
			RecipeRow row = matchChain(chain, unused);
			if (row.isComplete())
			{
				laidOut.addAll(0, row.items());
				unused.removeAll(row.items());
			}
		}

		laidOut.addAll(layoutByKind(new ArrayList<>(unused)));
		return laidOut;
	}

	private static RecipeRow matchChain(Chain chain, Set<BankPreviewItem> unused)
	{
		BankPreviewItem[] cells = new BankPreviewItem[GRID_COLUMNS];
		for (BankPreviewItem item : unused)
		{
			String name = normalized(item.getDisplayName());
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

			if (item.getItemId() == chain.secondary) cells[4] = first(cells[4], item);
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

	private static Chain chain(String herb, String product, int secondary)
	{
		return new Chain(herb, "herb." + herb.replace(' ', '_'),
			"potion." + product.replace(" potion", "").replace(' ', '_'), product, secondary);
	}

	private static final class Chain
	{
		private final String herb;
		private final String familyKey;
		private final String potionFamilyKey;
		private final String product;
		private final int secondary;

		private Chain(String herb, String familyKey, String potionFamilyKey, String product,
			int secondary)
		{
			this.herb = herb;
			this.familyKey = familyKey;
			this.potionFamilyKey = potionFamilyKey;
			this.product = product;
			this.secondary = secondary;
		}
	}

	private static final class RecipeRow
	{
		private final BankPreviewItem[] cells;

		private RecipeRow(BankPreviewItem[] cells)
		{
			this.cells = cells;
		}

		private boolean isComplete()
		{
			for (BankPreviewItem cell : cells)
				if (cell == null || cell.physicalBankSlotCount() != 1) return false;
			return true;
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

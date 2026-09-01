package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.WikiItemLists;
import com.pkoka5.ironmanbankarchitect.organize.layout.LayoutEntry;
import com.pkoka5.ironmanbankarchitect.organize.layout.AchievementDiarySemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.CosmeticSetSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.LayoutPlacement;
import com.pkoka5.ironmanbankarchitect.organize.layout.LayoutRequest;
import com.pkoka5.ironmanbankarchitect.organize.layout.LayoutResult;
import com.pkoka5.ironmanbankarchitect.organize.layout.MainQuickAccessSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.PotionDoseSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.ResourceSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.RuneSemanticRuleSet;
import com.pkoka5.ironmanbankarchitect.organize.layout.SemanticBlockLayoutEngine;
import com.pkoka5.ironmanbankarchitect.organize.layout.ToolOutfitSemanticRuleSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class BankOrganizationPreviewBuilder
{
	// An item is an alch candidate when this many strictly better items of the
	// same style and slot are owned (best + one backup stay in combat gear).
	private static final int OUTCLASSED_BY_COUNT = 2;
	private static final int ALCH_VALUE_THRESHOLD = 5000;
	// Bulk smithing output can still be worth processing below the normal
	// threshold, but very cheap utility clothing (for example Monk's robes)
	// must never move merely because the player owns a stack.
	private static final int BULK_STOCK_MIN_ALCH_VALUE = 1000;
	// Owning this many copies of one wearable marks it as production stock
	// (smithing/crafting output), not a gear option the player switches to.
	private static final int BULK_STOCK_QUANTITY = 8;
	private static final String ALCH_CATEGORY_KEY = "slayer-boss-loot";

	private BankOrganizationPreviewBuilder()
	{
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset)
	{
		return build(snapshot, catalog, preset, GearStatsSource.NONE, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats)
	{
		return build(snapshot, catalog, preset, gearStats, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, CategoryOverrideSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, overrides, null);
	}

	/**
	 * The blueprint arranged into the destinations of a plan, rather than into
	 * the preset's own ten categories. Pass {@code null} for the plan to get the
	 * per-category blueprint the rest of the analysis is built from.
	 */
	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides,
		BankLayoutPlan layoutPlan)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, overrides, layoutPlan,
			BankLayoutOptions.DEFAULTS);
	}

	/** The blueprint under a plan and the player's layout options. */
	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides,
		BankLayoutPlan layoutPlan, BankLayoutOptions options)
	{
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(catalog, "catalog");
		Objects.requireNonNull(preset, "preset");
		Objects.requireNonNull(gearStats, "gearStats");
		Objects.requireNonNull(itemValues, "itemValues");
		Objects.requireNonNull(overrides, "overrides");

		// A plan buckets by destination and category together, and those buckets
		// are only known once the items are read, so they are filled in as they
		// are met rather than seeded from the preset.
		BankLayoutPlan plan = layoutPlan == null ? null : layoutPlan.completedFor(preset);
		// Without a plan there is nothing to read a layout choice from, so the
		// per-category blueprint keeps the recipe rows it has always had.
		boolean herbloreRecipeRows = plan == null || BankLayoutStyles.herbloreUsesRecipeRows(plan);
		Map<String, Integer> tagCounts = new LinkedHashMap<>();
		Map<String, MutableCategoryPreview> previewsByCategory = new LinkedHashMap<>();
		if (plan == null)
		{
			for (BankCategory category : preset.getCategories())
			{
				previewsByCategory.put(category.getKey(), new MutableCategoryPreview(category, herbloreRecipeRows, options));
			}
		}

		Map<String, List<OwnedGear>> ownedGearByKey = new LinkedHashMap<>();
		java.util.Set<Integer> quickToolIds = IronmanQuickToolSelector.select(snapshot);
		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			if (bankItem.isPlaceholder())
			{
				continue;
			}
			Optional<GearStats> stats = gearStats.statsFor(bankItem.getItemId());
			if (stats.isPresent())
			{
				CatalogItem catalogItem = effectiveCatalogItem(catalog.describeOrUnknown(bankItem.getItemId()),
					bankItem.getItemId(), gearStats);
				if (catalogItem.getCategory() != ItemCategory.GEAR)
				{
					continue;
				}
				ownedGearByKey.computeIfAbsent(gearKey(stats.get()), key -> new ArrayList<>())
					.add(new OwnedGear(GearItemSorter.score(
						new BankPreviewItem(catalogItem, bankItem.getQuantity()), gearStats),
						stats.get()));
			}
		}

		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			CatalogItem catalogItem = effectiveCatalogItem(catalog.describeOrUnknown(bankItem.getItemId()),
				bankItem.getItemId(), gearStats);
			BankCategory category = PresetCategoryMapper.map(preset, catalogItem);
			if (preset.getType() == BankPresetType.IRONMAN
				&& quickToolIds.contains(catalogItem.getItemId()))
			{
				category = preset.getCategory("currency-utilities");
			}
			if (options.alchPile() && !bankItem.isPlaceholder()
				&& isAlchCandidate(preset, category, catalogItem, bankItem.getQuantity(),
				gearStats, itemValues, ownedGearByKey))
			{
				category = preset.getCategory(ALCH_CATEGORY_KEY);
			}
			// The player's own choice is applied last so it wins over every
			// automatic rule, including the quick-tool and alch overrides above.
			// A correction names a tag, which settles the category too; one that
			// still names a category is read the way it always was.
			BankTag pinnedTag = overrideTag(overrides, catalogItem.getItemId());
			if (pinnedTag != null)
			{
				category = preset.getCategory(pinnedTag.getCategoryKey());
			}
			else
			{
				BankCategory overridden = overriddenCategory(preset, overrides,
					catalogItem.getItemId());
				if (overridden != null)
				{
					category = overridden;
				}
			}
			MutableCategoryPreview preview;
			if (plan == null)
			{
				preview = previewsByCategory.get(category.getKey());
				if (preview == null)
				{
					throw new IllegalStateException("Preset mapper returned unknown category: " + category.getKey());
				}
			}
			else
			{
				// Bucketed by destination as well as by category, so the sorter
				// below sees exactly the items that share a tab. Tags of one
				// category on one tab share a bucket, which is what keeps a
				// bundle's layout intact while its parts stay together.
				BankTag tag = pinnedTag != null ? pinnedTag
					: BankTags.tagFor(category.getKey(), catalogItem.getSubcategory());
				if (!bankItem.isPlaceholder())
				{
					Integer counted = tagCounts.get(tag.getKey());
					tagCounts.put(tag.getKey(), counted == null ? 1 : counted + 1);
				}
				int destination = plan.destinationOf(tag.getKey());
				if (destination < 0)
				{
					destination = BankLayoutPlan.DESTINATION_COUNT - 1;
				}
				String bucketKey = destination + "|" + category.getKey();
				preview = previewsByCategory.get(bucketKey);
				if (preview == null)
				{
					preview = new MutableCategoryPreview(category, herbloreRecipeRows, options);
					previewsByCategory.put(bucketKey, preview);
				}
			}

			preview.add(toLayoutEntry(bankItem, catalogItem));
		}

		if (plan != null)
		{
			return new BankOrganizationPreview(preset,
				destinationPreviews(plan, previewsByCategory, gearStats), tagCounts);
		}

		List<BankCategoryPreview> categories = new ArrayList<>();
		for (MutableCategoryPreview preview : previewsByCategory.values())
		{
			categories.add(preview.toImmutable(gearStats));
		}

		return new BankOrganizationPreview(preset, categories);
	}

	/**
	 * The ten destinations of a plan, each laid out from the buckets that landed
	 * on it.
	 *
	 * <p>Sorting happens after grouping, not before, and once per category per
	 * destination. That is what lets a bundle keep its layout while its tags stay
	 * together: all seven Herblore tags on one tab is a single call to the recipe
	 * sorter, so the rows still form. Move the doses to another tab and each side
	 * is sorted on its own, which loses the rows rather than corrupting them.</p>
	 */
	private static List<BankCategoryPreview> destinationPreviews(BankLayoutPlan plan,
		Map<String, MutableCategoryPreview> buckets, GearStatsSource gearStats)
	{
		List<BankCategoryPreview> destinations =
			new ArrayList<>(BankLayoutPlan.DESTINATION_COUNT);
		for (int index = 0; index < BankLayoutPlan.DESTINATION_COUNT; index++)
		{
			destinations.add(destinationPreview(plan, buckets, index, gearStats));
		}

		return destinations;
	}

	/**
	 * One destination, built from its tags in the order the player arranged them.
	 * Tags of the same category share a bucket, so the first of them decides
	 * where that category's block sits on the tab.
	 */
	private static BankCategoryPreview destinationPreview(BankLayoutPlan plan,
		Map<String, MutableCategoryPreview> buckets, int destination, GearStatsSource gearStats)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		List<String> names = new ArrayList<>();
		Set<String> usedCategories = new LinkedHashSet<>();
		String firstCategoryKey = null;

		for (String tagKey : plan.getTagKeys(destination))
		{
			BankTag tag = BankTags.isKnown(tagKey) ? BankTags.byKey(tagKey) : null;
			if (tag == null)
			{
				continue;
			}
			names.add(tag.getName());
			if (firstCategoryKey == null)
			{
				firstCategoryKey = tag.getCategoryKey();
			}
			if (!usedCategories.add(tag.getCategoryKey()))
			{
				continue;
			}
			MutableCategoryPreview bucket = buckets.get(destination + "|" + tag.getCategoryKey());
			if (bucket != null)
			{
				items.addAll(bucket.toImmutable(gearStats).getItems());
			}
		}

		String key = firstCategoryKey == null ? "empty-" + destination : firstCategoryKey;
		String name = names.isEmpty() ? "Empty" : String.join(" + ", names);
		BankCategorySortMode sortMode = firstCategoryKey == null
			? BankCategorySortMode.GENERIC : new BankCategory(firstCategoryKey, name).getSortMode();
		return new BankCategoryPreview(new BankCategory(key, name, sortMode), items);
	}

	/**
	 * Resolves a player override to a category of this preset, or {@code null}
	 * when there is no override or the recorded key is not part of the preset.
	 */
	/**
	 * The tag a correction names, or {@code null} when it names something else.
	 *
	 * <p>Corrections made before the bundles were split named a category. Those
	 * still resolve, through {@link #overriddenCategory}, to that category with
	 * the tag worked out from the item's own subcategory as before.</p>
	 */
	private static BankTag overrideTag(CategoryOverrideSource overrides, int itemId)
	{
		Optional<String> key = overrides.categoryKeyFor(itemId);
		return key.isPresent() && BankTags.isKnown(key.get())
			? BankTags.byKey(key.get()) : null;
	}

	private static BankCategory overriddenCategory(BankPreset preset,
		CategoryOverrideSource overrides, int itemId)
	{
		Optional<String> key = overrides.categoryKeyFor(itemId);
		if (!key.isPresent())
		{
			return null;
		}
		for (BankCategory category : preset.getCategories())
		{
			if (category.getKey().equals(key.get()))
			{
				return category;
			}
		}
		return null;
	}

	private static CatalogItem effectiveCatalogItem(CatalogItem item, int itemId, GearStatsSource gearStats)
	{
		Optional<GearStats> stats = gearStats.statsFor(itemId);
		if (stats.isPresent() && (item.getCategory() == ItemCategory.GEAR
			|| ((item.getCategory() == ItemCategory.CLEANUP
				|| item.getCategory() == ItemCategory.UNKNOWN
				|| item.getCategory() == ItemCategory.UNCATEGORIZED)
				&& stats.get().score() > 0)))
		{
			return new CatalogItem(item.getItemId(), item.getDisplayName(), ItemCategory.GEAR,
				stats.get().getSlot().name().toLowerCase(), item.getTags(),
				item.getWorkflowKey().orElse(null));
		}

		return item;
	}

	static LayoutEntry toLayoutEntry(BankItemSnapshot bankItem, CatalogItem catalogItem)
	{
		Objects.requireNonNull(bankItem, "bankItem");
		Objects.requireNonNull(catalogItem, "catalogItem");
		return LayoutEntry.of(new BankPreviewItem(catalogItem, bankItem.getQuantity(),
			bankItem.isPlaceholder(), bankItem.getPhysicalSlotQuantities()), bankItem.getSlotIndex());
	}

	/**
	 * Ironman alch rule: a combat gear item whose style+slot already has two
	 * strictly better owned items is a duplicate the player will realistically
	 * never wear again; when it is also worth alching it moves to the alch
	 * review tab instead of cluttering the gear columns.
	 *
	 * <p>The tier score that produces "strictly better" collapses fifteen stats
	 * into one number, so on its own it can rank an item lower even when that
	 * item is the better choice on some axis. Automatically chosen items must
	 * therefore also be beaten outright by an owned item - see
	 * {@link GearStats#dominates(GearStats)}. Hand-reviewed stock in
	 * {@link IronmanAlchCandidateCatalog} is exempt: an explicit maintainer
	 * decision outranks the automatic rule.</p>
	 */
	private static boolean isAlchCandidate(BankPreset preset, BankCategory category, CatalogItem catalogItem, int quantity,
		GearStatsSource gearStats, ItemValueSource itemValues, Map<String, List<OwnedGear>> ownedGearByKey)
	{
		if (preset.getType() != BankPresetType.IRONMAN || !"combat-gear".equals(category.getKey()))
		{
			return false;
		}
		if (WikiItemLists.INSTANCE.isSpecialAttackWeapon(catalogItem.getDisplayName()))
		{
			// A bank layout cannot split duplicate copies across tabs. Keep the
			// complete stack of important niche/spec weapons in combat gear.
			return false;
		}
		boolean reviewedAlchable = IronmanAlchCandidateCatalog.contains(catalogItem.getItemId());
		if (reviewedAlchable && quantity > 1)
		{
			// Reviewed non-special stock belongs in the manual alch workflow.
			return itemValues.highAlchValue(catalogItem.getItemId()) > 0;
		}
		if (quantity <= 1 && !reviewedAlchable)
		{
			return false;
		}
		Optional<GearStats> stats = gearStats.statsFor(catalogItem.getItemId());
		if (!stats.isPresent())
		{
			return false;
		}

		List<OwnedGear> owned = ownedGearByKey.get(gearKey(stats.get()));
		if (owned == null)
		{
			return false;
		}

		int ownScore = GearItemSorter.score(new BankPreviewItem(catalogItem, quantity), gearStats);
		int strictlyBetter = 0;
		boolean beatenOutright = false;
		for (OwnedGear candidate : owned)
		{
			if (candidate.score > ownScore)
			{
				strictlyBetter++;
			}
			if (candidate.stats.dominates(stats.get()))
			{
				beatenOutright = true;
			}
		}
		// An explicit maintainer decision outranks the automatic proof.
		boolean provenReplaceable = reviewedAlchable || beatenOutright;

		// Bulk production stock (rune platebodies, crafted jewellery) moves out
		// regardless of alch value, but weapons and ammo stay: high quantities
		// there are consumables (chinchompas, thrown weapons), not stock.
		GearSlot slot = stats.get().getSlot();
		int highAlchValue = itemValues.highAlchValue(catalogItem.getItemId());
		// Deliberately not gated on dominance: this rule identifies production
		// stock by quantity, not by an item being beaten. Twenty-five rune
		// platebodies are smithing output even though nothing owned beats their
		// raw defence.
		if (quantity >= BULK_STOCK_QUANTITY && slot != GearSlot.WEAPON && slot != GearSlot.AMMO
			&& highAlchValue >= BULK_STOCK_MIN_ALCH_VALUE && strictlyBetter >= 1)
		{
			return true;
		}

		int requiredBetterItems = reviewedAlchable ? 1 : OUTCLASSED_BY_COUNT;
		int requiredAlchValue = reviewedAlchable ? 1 : ALCH_VALUE_THRESHOLD;
		return highAlchValue >= requiredAlchValue
			&& strictlyBetter >= requiredBetterItems
			&& provenReplaceable;
	}

	private static String gearKey(GearStats stats)
	{
		return stats.style().ordinal() + ":" + stats.slotRank();
	}

	/** One owned combat-gear item, kept per style/slot bucket for comparison. */
	private static final class OwnedGear
	{
		private final int score;
		private final GearStats stats;

		private OwnedGear(int score, GearStats stats)
		{
			this.score = score;
			this.stats = stats;
		}
	}

	private static final class MutableCategoryPreview
	{
		private final BankCategory category;
		private final boolean herbloreRecipeRows;
		private final BankLayoutOptions options;
		private final List<LayoutEntry> entries = new ArrayList<>();

		private MutableCategoryPreview(BankCategory category, boolean herbloreRecipeRows,
			BankLayoutOptions options)
		{
			this.category = category;
			this.herbloreRecipeRows = herbloreRecipeRows;
			this.options = options;
		}

		private void add(LayoutEntry entry)
		{
			entries.add(Objects.requireNonNull(entry, "entry"));
		}

		private BankCategoryPreview toImmutable(GearStatsSource gearStats)
		{
			List<BankPreviewItem> items = items(entries);
				switch (category.getSortMode())
				{
				case MAIN:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						IronmanMainItemSorter.sort(items), MainQuickAccessSemanticRuleSet.forEntries(entries)));
				case RESOURCES:
					return BankCategoryPreview.fromLogicalItems(category, resourceLayout(items));
				case TELEPORTS:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						TeleportItemSorter.sort(items), RuneSemanticRuleSet.forEntries(entries)));
				case SUPPLIES:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						SupplyItemSorter.sort(items), PotionDoseSemanticRuleSet.forEntries(entries)));
				case TOOLS:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						ToolItemSorter.sort(items), ToolOutfitSemanticRuleSet.forEntries(entries)));
				case CURRENCY:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						CurrencyItemSorter.sort(items), AchievementDiarySemanticRuleSet.forEntries(entries)));
				case FARMING:
					return BankCategoryPreview.fromLogicalItems(category, FarmingItemSorter.layout(items, 0));
				case GEAR:
					return BankCategoryPreview.fromLogicalItems(category, gearLayout(items, gearStats));
				case CLUES:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						PresetItemSorter.sort(category, items, gearStats),
						CosmeticSetSemanticRuleSet.forEntries(entries)));
				case HERBLORE:
					// The only layout the plan can talk out of its default shape:
					// see BankLayoutStyles for why moving the doses changes it.
					return BankCategoryPreview.fromLogicalItems(category, herbloreRecipeRows
						? HerbloreItemSorter.layout(items, options.fillHerbloreRows())
						: HerbloreItemSorter.layoutByKind(items));
				default:
					return BankCategoryPreview.fromLogicalItems(category,
						PresetItemSorter.sort(category, items, gearStats));
			}
		}

		/** Keeps the primary strength/ranged/magic/prayer rows physically fixed. */
		private List<BankPreviewItem> gearLayout(List<BankPreviewItem> items, GearStatsSource gearStats)
		{
			if (!options.fillGearRows())
			{
				// The aligned setup rows are the only thing here that needs padding,
				// so without filling there is nothing to align and the whole tab is
				// laid out as the dense tail. Sets still hold together as columns;
				// what goes is the four-style grid, not the families.
				List<BankPreviewItem> dense = GearItemSorter.dense(items, gearStats);
				List<LayoutEntry> denseEntries = entriesForItems(entries, dense);
				int rows = (denseEntries.size() + GearItemSorter.GRID_COLUMNS - 1)
					/ GearItemSorter.GRID_COLUMNS;
				return semanticLayout(dense,
					GearSetSemanticRuleSet.forEntries(denseEntries, Math.max(1, rows)));
			}

			GearItemSorter.GearLayout gear = GearItemSorter.plan(items, gearStats);
			List<BankPreviewItem> planned = new ArrayList<>(items.size());
			planned.addAll(gear.getSetupRows());

			List<LayoutEntry> tailEntries = entriesForItems(entries, gear.getTail());
			int gridStartColumn = planned.size() % GearItemSorter.GRID_COLUMNS;
			int physicalTailRows = (gridStartColumn + tailEntries.size()
				+ GearItemSorter.GRID_COLUMNS - 1) / GearItemSorter.GRID_COLUMNS;
			LayoutRequest tailRequest = GearSetSemanticRuleSet
				.forEntries(tailEntries, Math.max(1, physicalTailRows))
				.withGridStartColumn(gridStartColumn);
			planned.addAll(semanticLayout(gear.getTail(), tailRequest));
			return planned;
		}

		/**
		 * Plans each {@link ResourceSkillZone} independently at its real physical start column so a
		 * zone stays hard-contiguous without blank separators or borrowed items from another zone.
		 * {@link ResourceItemSorter#sort} already orders items by zone first, so same-zone items are
		 * already contiguous runs in its output.
		 */
		private List<BankPreviewItem> resourceLayout(List<BankPreviewItem> items)
		{
			List<BankPreviewItem> sorted = ResourceItemSorter.sort(items);
			List<BankPreviewItem> planned = new ArrayList<>(sorted.size());
			int start = 0;
			while (start < sorted.size())
			{
				ResourceSkillZone zone = ResourceSkillZoneClassifier.classify(sorted.get(start));
				int end = start + 1;
				while (end < sorted.size() && ResourceSkillZoneClassifier.classify(sorted.get(end)) == zone)
				{
					end++;
				}

				List<BankPreviewItem> zoneItems = new ArrayList<>(sorted.subList(start, end));
				List<LayoutEntry> zoneEntries = entriesForItems(entries, zoneItems);
				LayoutRequest zoneRequest = ResourceSemanticRuleSet.forZoneEntries(zoneEntries)
					.withGridStartColumn(planned.size() % GearItemSorter.GRID_COLUMNS);
				planned.addAll(semanticLayout(zoneItems, zoneRequest));
				start = end;
			}
			return planned;
		}

		private static List<BankPreviewItem> items(List<LayoutEntry> source)
		{
			List<BankPreviewItem> items = new ArrayList<>(source.size());
			for (LayoutEntry entry : source)
			{
				items.add(entry.getItem());
			}
			return items;
		}

		private static List<LayoutEntry> entriesForItems(List<LayoutEntry> source,
			List<BankPreviewItem> selected)
		{
			Map<Integer, LayoutEntry> byItemId = new LinkedHashMap<>();
			for (LayoutEntry entry : source)
			{
				byItemId.put(entry.getItem().getItemId(), entry);
			}

			List<LayoutEntry> result = new ArrayList<>(selected.size());
			for (BankPreviewItem item : selected)
			{
				LayoutEntry entry = byItemId.get(item.getItemId());
				if (entry == null)
				{
					throw new IllegalStateException("Missing layout entry for item " + item.getItemId());
				}
				result.add(entry);
			}
			return result;
		}

		private List<BankPreviewItem> semanticLayout(List<BankPreviewItem> fallback,
			LayoutRequest request)
		{
			List<Integer> fallbackItemIds = new ArrayList<>(fallback.size());
			for (BankPreviewItem item : fallback)
			{
				fallbackItemIds.add(item.getItemId());
			}

			LayoutResult result = new SemanticBlockLayoutEngine().plan(request, fallbackItemIds);
			if (!result.isSuccess())
			{
				throw new IllegalStateException("Semantic layout failed for category "
					+ category.getKey() + ": " + result.getConflicts());
			}

			BankPreviewItem[] byTarget = new BankPreviewItem[fallback.size()];
			for (LayoutPlacement placement : result.getPlacements())
			{
				int target = placement.getTargetIndex();
				if (target < 0 || target >= byTarget.length || byTarget[target] != null)
				{
					throw new IllegalStateException("Semantic layout for category " + category.getKey()
						+ " returned invalid target " + target);
				}
				byTarget[target] = placement.getItem();
			}

			List<BankPreviewItem> planned = new ArrayList<>(byTarget.length);
			for (int target = 0; target < byTarget.length; target++)
			{
				if (byTarget[target] == null)
				{
					throw new IllegalStateException("Semantic layout for category "
						+ category.getKey() + " omitted target " + target);
				}
				planned.add(byTarget[target]);
			}
			return planned;
		}
	}
}

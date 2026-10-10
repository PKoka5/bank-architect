package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.layout.*;
import java.util.*;
import java.util.List;





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

	/**
	 * The tag order of this destination when the player has made it their own.
	 * A default plan's order is bookkeeping, not a statement - the sorters'
	 * curated group orders stand until the player rearranges the tags, and
	 * from then on their sequence wins.
	 */
	private static List<String> customizedTagOrder(BankPreset preset, BankLayoutPlan plan,
		int destination)
	{
		List<String> tags = plan.getTagKeys(destination);
		if (preset.getType() == BankPresetType.MAIN) return tags;
		if (tags.size() < 2)
		{
			return Collections.emptyList();
		}
		List<String> defaultOrder = new ArrayList<>();
		for (List<String> defaultTags : BankLayoutPlan.defaultFor(preset).getDestinations())
		{
			for (String tag : defaultTags)
			{
				if (tags.contains(tag))
				{
					defaultOrder.add(tag);
				}
			}
		}
		for (String tag : tags)
		{
			if (!defaultOrder.contains(tag))
			{
				defaultOrder.add(tag);
			}
		}
		return tags.equals(defaultOrder) ? Collections.emptyList() : tags;
	}

	private static String normalizedSubcategory(CatalogItem item)
	{
		String subcategory = item.getSubcategory();
		return subcategory == null ? "" : subcategory.trim().toLowerCase(java.util.Locale.ROOT);
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
		boolean main = preset.getType() == BankPresetType.MAIN;
		boolean ammunitionMoved = plan != null && plan.destinationOf("ammunition")
			!= BankLayoutPlan.defaultFor(preset).destinationOf("ammunition");
		boolean herbloreRecipeRows = !main && (plan == null || BankLayoutStyles.herbloreUsesRecipeRows(plan));
		Map<String, Integer> tagCounts = new LinkedHashMap<>();
		Map<String, MutableCategoryPreview> previewsByCategory = new LinkedHashMap<>();
		if (plan == null)
		{
			for (BankCategory category : preset.getCategories())
			{
				previewsByCategory.put(category.getKey(), new MutableCategoryPreview(category, main,
					herbloreRecipeRows, options, Collections.emptyList()));
			}
		}

		Map<String, List<BankGear>> gearByKey = new LinkedHashMap<>();
		Set<Integer> realOwned = new HashSet<>();
		Map<Integer, String> choices = new HashMap<>();
		boolean captured = options.itemOrders().isCaptured();
		java.util.Set<Integer> quickToolIds = options.gatherFrequentlyUsed()
			? IronmanQuickToolSelector.select(snapshot) : Collections.emptySet();
		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			if (!options.alchPile() || (!main && preset.getType() != BankPresetType.IRONMAN)) break;
			int itemId = bankItem.getItemId();
			if (!bankItem.isPlaceholder()) realOwned.add(itemId);
			BankTag choice = overrideTag(overrides, itemId);
			BankCategory legacyChoice = overriddenCategory(preset, overrides, itemId);
			if (choice != null) choices.put(itemId, choice.getKey());
			else if (legacyChoice != null) choices.put(itemId, legacyChoice.getKey());
			BlueprintItemOrders.Destination saved = options.itemOrders().destinations().get(itemId + "#0");
			if (saved != null && (captured
				|| plan != null && plan.destinationOf(saved.tag) == saved.tab)) choices.put(itemId, saved.tag);
			Optional<GearStats> stats = gearStats.statsFor(bankItem.getItemId());
			if (stats.isPresent())
			{
				CatalogItem catalogItem = effectiveCatalogItem(catalog.describeOrUnknown(bankItem.getItemId()),
					bankItem.getItemId(), gearStats);
				if (catalogItem.getCategory() != ItemCategory.GEAR)
				{
					continue;
				}
				gearByKey.computeIfAbsent(gearKey(catalogItem, stats.get()), key -> new ArrayList<>())
					.add(new BankGear(GearItemSorter.score(
						new BankPreviewItem(catalogItem, bankItem.getQuantity(), bankItem.isPlaceholder()), gearStats),
						stats.get(), catalogItem.getItemId()));
			}
		}

		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			CatalogItem sourceItem = catalog.describeOrUnknown(bankItem.getItemId());
			CatalogItem catalogItem = effectiveCatalogItem(sourceItem,
				bankItem.getItemId(), gearStats);
			BankCategory category = PresetCategoryMapper.map(preset, catalogItem,
				options.gatherFrequentlyUsed() && !IronmanQuickToolSelector.isTieredTool(bankItem.getItemId()));
			if (preset.getType() == BankPresetType.IRONMAN
				&& quickToolIds.contains(catalogItem.getItemId()))
			{
				category = preset.getCategory("currency-utilities");
			}
			// By family, a part dose counts as its potion: it classifies with
			// the full potions rather than as Herblore's to-decant pile, so
			// each potion runs 4 to 1 in one place.
			boolean partDoseAsPotion = (main || options.potionDoses() == PotionDoseOrder.BY_FAMILY)
				&& normalizedSubcategory(catalogItem).matches("(?:potion-)?dose-[123]");
			if (partDoseAsPotion)
			{
				category = preset.getCategory("potions-food");
			}
			boolean alchCandidate = options.alchPile() && !bankItem.isPlaceholder()
				&& sourceItem.getCategory() != ItemCategory.UNKNOWN
				&& sourceItem.getCategory() != ItemCategory.UNCATEGORIZED
				&& isAlchCandidate(preset, category, catalogItem, bankItem.getQuantity(),
				gearStats, itemValues, gearByKey, realOwned, choices);
			if (alchCandidate)
			{
				category = preset.getCategory(ALCH_CATEGORY_KEY);
			}
			boolean ammoDrops = !ammunitionMoved && "combat-gear".equals(category.getKey())
				&& PresetItemSorter.isDropAmmunition(catalogItem.getCategory(),
					normalizedSubcategory(catalogItem), catalogItem.getDisplayName());
			BlueprintItemOrders.Destination saved = options.itemOrders().destinations()
				.get(catalogItem.getItemId() + "#0");
			// Old editor moves retain their original Combat tag until the
			// existing per-item router applies the player's saved destination.
			if (saved != null && saved.originalTag.equals(
				BankTags.tagFor("combat-gear", catalogItem.getSubcategory()).getKey())
				&& (saved.isCaptured() || plan != null && plan.destinationOf(saved.tag) == saved.tab))
				ammoDrops = false;
			if (ammoDrops) category = preset.getCategory(ALCH_CATEGORY_KEY);
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
					alchCandidate = false;
				}
			}
			MutableCategoryPreview preview;
			BankTag routedTag = pinnedTag != null ? pinnedTag
				: alchCandidate && ALCH_CATEGORY_KEY.equals(category.getKey()) ? BankTags.byKey("alch")
				: ammoDrops && ALCH_CATEGORY_KEY.equals(category.getKey()) ? BankTags.byKey("boss-loot") : null;
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
				BankTag tag = routedTag != null ? routedTag
					: main && catalogItem.getItemId() == 9084 && "currency-utilities".equals(category.getKey())
						? BankTags.byKey("teleports")
					: partDoseAsPotion && "potions-food".equals(category.getKey()) ? BankTags.byKey("potions")
					: BankTags.tagFor(category.getKey(), catalogItem.getSubcategory());
				routedTag = tag;
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
					preview = new MutableCategoryPreview(category, main, herbloreRecipeRows, options,
						customizedTagOrder(preset, plan, destination));
					previewsByCategory.put(bucketKey, preview);
				}
			}

			preview.add(toLayoutEntry(bankItem, catalogItem,
				routedTag == null ? null : routedTag.getKey()));
			if (routedTag != null)
			{
				preview.routedTags.put(catalogItem.getItemId(), routedTag.getKey());
			}
		}

		if (plan != null)
		{
			List<BankCategoryPreview> destinations =
				destinationPreviews(plan, previewsByCategory, gearStats);
			Map<String, List<BankBlockDescriptor>> blockDescriptors = new LinkedHashMap<>();
			for (MutableCategoryPreview mutable : previewsByCategory.values())
			{
				blockDescriptors.putAll(mutable.getBlockDescriptors());
			}
			return options.itemOrders().apply(new BankOrganizationPreview(preset, destinations, tagCounts, blockDescriptors), plan);
		}

		List<BankCategoryPreview> categories = new ArrayList<>();
		for (MutableCategoryPreview preview : previewsByCategory.values())
		{
			categories.add(preview.toImmutable(gearStats));
		}

		return options.itemOrders().apply(new BankOrganizationPreview(preset, categories));
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
	 * Tags of the same category share a bucket, and by default the first of
	 * them decides where that category's whole block sits on the tab. Once the
	 * player has rearranged the tab's tags, the blocks are woven instead: each
	 * tag's items are drawn from its category's bucket in turn, so the tab
	 * reads in the tag order even where that order alternates between
	 * categories.
	 */
	private static BankCategoryPreview destinationPreview(BankLayoutPlan plan,
		Map<String, MutableCategoryPreview> buckets, int destination, GearStatsSource gearStats)
	{
		List<String> names = new ArrayList<>();
		List<BankTag> tags = new ArrayList<>();
		Map<String, MutableCategoryPreview> bucketsByCategory = new LinkedHashMap<>();
		Map<String, List<BankPreviewItem>> blocks = new LinkedHashMap<>();

		for (String tagKey : plan.getTagKeys(destination))
		{
			BankTag tag = BankTags.isKnown(tagKey) ? BankTags.byKey(tagKey) : null;
			if (tag == null)
			{
				continue;
			}
			names.add(tag.getName());
			tags.add(tag);
			String categoryKey = tag.getCategoryKey();
			MutableCategoryPreview bucket = buckets.get(destination + "|" + categoryKey);
			if (bucket != null && !blocks.containsKey(categoryKey))
			{
				bucketsByCategory.put(categoryKey, bucket);
				blocks.put(categoryKey, bucket.toImmutable(gearStats).getItems());
			}
		}

		List<BankPreviewItem> items = weavesByTag(bucketsByCategory.values())
			? weave(tags, bucketsByCategory, blocks)
			: stack(blocks.values());

		String firstCategoryKey = tags.isEmpty() ? null : tags.get(0).getCategoryKey();
		String key = firstCategoryKey == null ? "empty-" + destination : firstCategoryKey;
		String name = names.isEmpty() ? "Empty" : String.join(" + ", names);
		BankCategorySortMode sortMode = firstCategoryKey == null
			? BankCategorySortMode.GENERIC : new BankCategory(firstCategoryKey, name).getSortMode();
		return new BankCategoryPreview(new BankCategory(key, name, sortMode), items);
	}

	/**
	 * Whether a tab's category blocks are woven by tag rather than stacked.
	 * Only a tab the player has rearranged qualifies, only when it holds more
	 * than one category - a single block reads the same either way - and only
	 * while every block on it is a plain run. A block placed by column, like
	 * the gear grid or the farming families, keeps its shape by staying whole.
	 */
	private static boolean weavesByTag(Collection<MutableCategoryPreview> buckets)
	{
		if (buckets.size() < 2)
		{
			return false;
		}
		for (MutableCategoryPreview bucket : buckets)
		{
			if (!bucket.arrangedByPlayer() || !bucket.isPlainRun())
			{
				return false;
			}
		}
		return true;
	}

	/** The blocks one after another, each whole, in the order they were met. */
	private static List<BankPreviewItem> stack(Collection<List<BankPreviewItem>> blocks)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (List<BankPreviewItem> block : blocks)
		{
			items.addAll(block);
		}
		return items;
	}

	/**
	 * The blocks woven in tag order: each tag draws its own items out of its
	 * category's laid-out block, keeping their order within the tag. Anything
	 * a block holds under no listed tag follows at the end, block by block, so
	 * no item is dropped.
	 */
	private static List<BankPreviewItem> weave(List<BankTag> tags,
		Map<String, MutableCategoryPreview> bucketsByCategory, Map<String, List<BankPreviewItem>> blocks)
	{
		List<BankPreviewItem> woven = new ArrayList<>();
		Set<BankPreviewItem> placed = Collections.newSetFromMap(new IdentityHashMap<>());
		for (BankTag tag : tags)
		{
			MutableCategoryPreview bucket = bucketsByCategory.get(tag.getCategoryKey());
			if (bucket == null)
			{
				continue;
			}
			for (BankPreviewItem item : blocks.get(tag.getCategoryKey()))
			{
				if (!placed.contains(item) && tag.getKey().equals(bucket.tagKeyOf(item)))
				{
					placed.add(item);
					woven.add(item);
				}
			}
		}
		for (List<BankPreviewItem> block : blocks.values())
		{
			for (BankPreviewItem item : block)
			{
				if (placed.add(item))
				{
					woven.add(item);
				}
			}
		}
		return woven;
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
		if (item.getCategory() == ItemCategory.CLEANUP && "quest-item".equals(item.getSubcategory())) return item;
		Optional<GearStats> stats = gearStats.statsFor(itemId);
		if (stats.isPresent() && (item.getCategory() == ItemCategory.GEAR
			|| ((item.getCategory() == ItemCategory.CLEANUP
				|| item.getCategory() == ItemCategory.UNKNOWN
				|| item.getCategory() == ItemCategory.UNCATEGORIZED)
				&& stats.get().score() > 0)))
		{
			return new CatalogItem(item.getItemId(), item.getDisplayName(), ItemCategory.GEAR,
				stats.get().getSlot().name().toLowerCase(java.util.Locale.ROOT), item.getTags(),
				item.getWorkflowKey().orElse(null));
		}

		return item;
	}

	static LayoutEntry toLayoutEntry(BankItemSnapshot bankItem, CatalogItem catalogItem)
	{
		return toLayoutEntry(bankItem, catalogItem, null);
	}

	private static LayoutEntry toLayoutEntry(BankItemSnapshot bankItem, CatalogItem catalogItem,
		String tagKey)
	{
		Objects.requireNonNull(bankItem, "bankItem");
		Objects.requireNonNull(catalogItem, "catalogItem");
		return LayoutEntry.of(new BankPreviewItem(catalogItem, bankItem.getQuantity(),
			bankItem.isPlaceholder(), bankItem.getPhysicalSlotQuantities()).withLayoutTag(tagKey), bankItem.getSlotIndex());
	}

	/** Shared Main/Ironman policy: reviewed armour accepts exact higher-tier bank placeholders;
	 * other comparisons need an owned replacement. A placeholder records the planned kit,
	 * not present ownership. Removing it restores normal classification.
	 * Unknown stock needs full dominance and the conservative value/backup rules.
	 * A weighted score alone cannot prove replacement. The exact Dragon halberd
	 * exception is an explicit owner choice, still subject to the option and overrides. */
	private static boolean isAlchCandidate(BankPreset preset, BankCategory category, CatalogItem catalogItem, int quantity,
		GearStatsSource gearStats, ItemValueSource itemValues, Map<String, List<BankGear>> gearByKey,
		Set<Integer> realOwned, Map<Integer, String> choices)
	{
		if (PresetItemSorter.isDropAmmunition(catalogItem.getCategory(),
			normalizedSubcategory(catalogItem), catalogItem.getDisplayName())) return false;
		if (preset.getType() != BankPresetType.MAIN && preset.getType() != BankPresetType.IRONMAN)
		{
			return false;
		}
		// Owner-reviewed policy: the ordinary Dragon halberd is always alch stock.
		if (catalogItem.getItemId() == net.runelite.api.gameval.ItemID.DRAGON_HALBERD)
			return itemValues.highAlchValue(catalogItem.getItemId()) > 0;
		boolean reviewedAlchable = IronmanAlchCandidateCatalog.contains(catalogItem.getItemId());
		if (catalogItem.getTags().stream().anyMatch(tag -> !reviewedAlchable || !"clue-required".equals(tag))
			|| WikiItemLists.INSTANCE.isSpecialAttackWeapon(catalogItem.getDisplayName()))
		{
			// Known roles can matter despite weaker stats. A bank layout cannot
			// split duplicate copies, so retain the complete stack for manual review.
			return false;
		}
		int highAlchValue = itemValues.highAlchValue(catalogItem.getItemId());
		if (highAlchValue <= 0 || (quantity <= 1 && !reviewedAlchable))
		{
			return false;
		}
		int itemId = catalogItem.getItemId();
		if (reviewedAlchable && IronmanQuickToolSelector.isTieredTool(itemId))
			return IronmanQuickToolSelector.hasOwnedUpgrade(itemId, realOwned);
		boolean roleReplacement = reviewedAlchable
			&& IronmanAlchCandidateCatalog.hasRoleReplacement(itemId, realOwned, choices);
		if (IronmanAlchCandidateCatalog.isElementalStaff(itemId) || roleReplacement) return roleReplacement;
		if (!"combat-gear".equals(category.getKey())) return false;
		Optional<GearStats> stats = gearStats.statsFor(catalogItem.getItemId());
		if (!stats.isPresent())
		{
			return false;
		}

		List<BankGear> gear = gearByKey.get(gearKey(catalogItem, stats.get()));
		if (gear == null)
		{
			return false;
		}
		if (reviewedAlchable)
		{
			int tier = alchTier(catalogItem.getItemId());
			boolean armour = stats.get().getSlot() != GearSlot.WEAPON && stats.get().getSlot() != GearSlot.AMMO;
			for (BankGear candidate : gear)
				if (realOwned.contains(candidate.itemId) && candidate.stats.dominates(stats.get())
					|| (armour && tier > 0 && alchTier(candidate.itemId) > tier)) return true;
			return false;
		}

		int ownScore = GearItemSorter.score(new BankPreviewItem(catalogItem, quantity), gearStats);
		int strictlyBetter = 0;
		boolean beatenOutright = false;
		for (BankGear candidate : gear)
		{
			if (!realOwned.contains(candidate.itemId)) continue;
			if (candidate.score > ownScore)
			{
				strictlyBetter++;
			}
			if (candidate.stats.dominates(stats.get()))
			{
				beatenOutright = true;
			}
		}
		// Keep weapons and ammo: large quantities can be consumable supplies.
		GearSlot slot = stats.get().getSlot();
		// Quantity alone cannot prove unknown niche gear is replaceable.
		if (quantity >= BULK_STOCK_QUANTITY && slot != GearSlot.WEAPON && slot != GearSlot.AMMO
			&& highAlchValue >= BULK_STOCK_MIN_ALCH_VALUE && strictlyBetter >= 1 && beatenOutright)
		{
			return true;
		}

		return highAlchValue >= ALCH_VALUE_THRESHOLD
			&& strictlyBetter >= OUTCLASSED_BY_COUNT && beatenOutright;
	}

	private static String gearKey(CatalogItem item, GearStats stats)
	{
		return GearItemSorter.styleOf(item.getDisplayName(), stats).ordinal()
			+ ":" + GearItemSorter.slotOf(item.getDisplayName(), stats);
	}

	/** Exact progression facts; names alone cannot prove a replacement for alching. */
	private static int alchTier(int itemId)
	{
		switch (itemId)
		{
			case 1101: case 1103: case 1105: case 1107: case 1109: case 1111: case 1113:
			case 1137: case 1139: case 1141: case 1143: case 1145: case 1147: case 1151:
				return 2; // Ordinary metal chainbodies and med helms, matching plate/full helm stages.
			case 1149: case 3140: return 3; // Ordinary Dragon med helm and chainbody.
			case 11826: case 11828: case 11830: return 4; // Armadyl armour.
			case 27226: case 27229: case 27232: return 5; // Unfortified Masori armour.
			default: return GearTierCatalog.INSTANCE.tierOf(itemId).orElse(0);
		}
	}

	/** Bank gear including placeholders, kept per style/slot bucket for bounded comparisons. */
	private static final class BankGear
	{
		private final int score;
		private final GearStats stats;
		private final int itemId;

		private BankGear(int score, GearStats stats, int itemId)
		{
			this.score = score;
			this.stats = stats;
			this.itemId = itemId;
		}
	}

	private static final class MutableCategoryPreview
	{
		private final BankCategory category;
		private final boolean main;
		private final boolean herbloreRecipeRows;
		private final BankLayoutOptions options;
		private final List<LayoutEntry> entries = new ArrayList<>();
		private final Map<Integer, String> routedTags = new LinkedHashMap<>();
		private final Map<String, List<BankBlockDescriptor>> blockDescriptors = new LinkedHashMap<>();

		private final List<String> destinationTags;
		private boolean plainRun;

		private MutableCategoryPreview(BankCategory category, boolean main, boolean herbloreRecipeRows,
			BankLayoutOptions options, List<String> destinationTags)
		{
			this.category = category;
			this.main = main;
			this.herbloreRecipeRows = herbloreRecipeRows;
			this.options = options;
			this.destinationTags = destinationTags;
		}

		/**
		 * The player's layout lists this tab's tags in an order, and that order
		 * is theirs: the sorted items regroup by tag, tags in the layout's
		 * sequence, order within a tag untouched. Bundle layouts that span
		 * tags by design - Herblore's recipe rows, the gear grid - are exempt.
		 */
		/** Whether the player has rearranged the tags of this bucket's tab. */
		private boolean arrangedByPlayer()
		{
			return !destinationTags.isEmpty();
		}

		/**
		 * Whether the layout this bucket last produced is a plain run: the
		 * sorter's order, wrapping row by row, with nothing placed by column.
		 * Only such a run can be woven with another category's block without
		 * losing its shape. Each layout path declares itself as it runs, and a
		 * path that says nothing counts as shaped, so an oversight stacks the
		 * tab rather than scrambling a grid.
		 */
		private boolean isPlainRun()
		{
			return plainRun;
		}

		private List<BankPreviewItem> honorTagOrder(List<BankPreviewItem> sorted)
		{
			if (destinationTags.size() < 2)
			{
				return sorted;
			}
			// A correction joins the tag's existing run. Keep native members first;
			// an explicit block arrangement, applied afterward, still wins.
			sorted = new ArrayList<>(sorted);
			sorted.sort(java.util.Comparator.comparing(item ->
				!tagKeyOf(item).equals(inferredTagKeyOf(item))));
			List<BankPreviewItem> regrouped = new ArrayList<>(sorted.size());
			for (String tagKey : destinationTags)
			{
				for (BankPreviewItem item : sorted)
				{
					if (tagKey.equals(tagKeyOf(item)))
					{
						regrouped.add(item);
					}
				}
			}
			for (BankPreviewItem item : sorted)
			{
				if (!destinationTags.contains(tagKeyOf(item)))
				{
					regrouped.add(item);
				}
			}
			return regrouped;
		}

		/**
		 * The player's saved block order for a tag, applied at the sorter
		 * stage: their blocks lead in their sequence, and an unarranged block
		 * slots in after its nearest curated-order predecessor among them,
		 * joining the tail only when none precedes it. A pure permutation of
		 * the input; with nothing saved it IS the input, byte for byte.
		 */
		private List<BankPreviewItem> honorBlockOrder(List<BankPreviewItem> sorted)
		{
			Map<String, List<String>> arrangements = options.blockArrangements().orders();
			if (arrangements.isEmpty())
			{
				return sorted;
			}

			Map<String, Map<String, List<BankPreviewItem>>> blocksByTag = new LinkedHashMap<>();
			for (BankPreviewItem item : sorted)
			{
				blocksByTag.computeIfAbsent(tagKeyOf(item), ignored -> new LinkedHashMap<>())
					.computeIfAbsent(BlockKeys.blockKeyOf(item), ignored -> new ArrayList<>())
					.add(item);
			}

			Map<String, List<String>> sequenceByTag = new LinkedHashMap<>();
			for (Map.Entry<String, Map<String, List<BankPreviewItem>>> tag : blocksByTag.entrySet())
			{
				List<String> saved = arrangements.get(tag.getKey());
				if (saved != null)
				{
					sequenceByTag.put(tag.getKey(),
						blockSequence(saved, new ArrayList<>(tag.getValue().keySet())));
				}
			}
			if (sequenceByTag.isEmpty())
			{
				return sorted;
			}

			// Each arranged tag's items are emitted as one run where the tag
			// first appears, so a regrouped tag stays contiguous and a
			// scattered one collects where it began.
			List<BankPreviewItem> result = new ArrayList<>(sorted.size());
			Set<String> emitted = new LinkedHashSet<>();
			for (BankPreviewItem item : sorted)
			{
				String tagKey = tagKeyOf(item);
				List<String> sequence = sequenceByTag.get(tagKey);
				if (sequence == null)
				{
					result.add(item);
				}
				else if (emitted.add(tagKey))
				{
					for (String blockKey : sequence)
					{
						result.addAll(blocksByTag.get(tagKey).get(blockKey));
					}
				}
			}
			return result;
		}

		/**
		 * Merges the saved order with the blocks actually present. A saved key
		 * matching nothing resolves through its representative item to the
		 * block now containing it - the arrangement survives an item joining
		 * a catalogued family - and is otherwise skipped but kept stored.
		 */
		private List<String> blockSequence(List<String> saved, List<String> curated)
		{
			List<String> arranged = new ArrayList<>();
			for (String key : saved)
			{
				String resolved = curated.contains(key) ? key : successorOf(key, curated);
				if (resolved != null && !arranged.contains(resolved))
				{
					arranged.add(resolved);
				}
			}
			if (arranged.isEmpty())
			{
				return curated;
			}

			List<String> sequence = new ArrayList<>(arranged);
			for (String key : curated)
			{
				if (sequence.contains(key))
				{
					continue;
				}
				int anchor = -1;
				for (int i = curated.indexOf(key) - 1; i >= 0 && anchor < 0; i--)
				{
					anchor = sequence.indexOf(curated.get(i));
				}
				if (anchor < 0)
				{
					sequence.add(key);
				}
				else
				{
					sequence.add(anchor + 1, key);
				}
			}
			return sequence;
		}

		private String successorOf(String savedKey, List<String> curated)
		{
			if (!savedKey.startsWith("item:"))
			{
				return null;
			}
			int itemId;
			try
			{
				itemId = Integer.parseInt(savedKey.substring("item:".length()));
			}
			catch (NumberFormatException malformed)
			{
				return null;
			}
			for (LayoutEntry entry : entries)
			{
				if (entry.getItem().getItemId() == itemId)
				{
					String current = BlockKeys.blockKeyOf(entry.getItem());
					return curated.contains(current) ? current : null;
				}
			}
			return null;
		}

		/** Publishes the tag's blocks, in effective order, for the arrange editor. */
		private List<BankPreviewItem> recordBlocks(List<BankPreviewItem> ordered)
		{
			blockDescriptors.clear();
			Map<String, Map<String, BankBlockDescriptor>> collected = new LinkedHashMap<>();
			for (BankPreviewItem item : ordered)
			{
				String tagKey = tagKeyOf(item);
				String blockKey = BlockKeys.blockKeyOf(item);
				Map<String, BankBlockDescriptor> tagBlocks =
					collected.computeIfAbsent(tagKey, ignored -> new LinkedHashMap<>());
				BankBlockDescriptor existing = tagBlocks.get(blockKey);
				tagBlocks.put(blockKey, new BankBlockDescriptor(tagKey, blockKey,
					existing == null ? BlockKeys.blockNameOf(item) : existing.getDisplayName(),
					existing == null ? 1 : existing.getMemberCount() + 1));
			}
			for (Map.Entry<String, Map<String, BankBlockDescriptor>> tag : collected.entrySet())
			{
				blockDescriptors.put(tag.getKey(), new ArrayList<>(tag.getValue().values()));
			}
			return ordered;
		}

		Map<String, List<BankBlockDescriptor>> getBlockDescriptors()
		{
			return blockDescriptors;
		}

		private String tagKeyOf(BankPreviewItem item)
		{
			String routed = routedTags.get(item.getItemId());
			if (routed != null)
			{
				return routed;
			}
			return inferredTagKeyOf(item);
		}

		private String inferredTagKeyOf(BankPreviewItem item)
		{
			String subcategory = item.getSubcategory() == null ? ""
				: item.getSubcategory().trim().toLowerCase(java.util.Locale.ROOT);
			if ((main || options.potionDoses() == PotionDoseOrder.BY_FAMILY)
				&& subcategory.startsWith("potion-dose-") && !subcategory.equals("potion-dose-4"))
			{
				return "potions";
			}
			try
			{
				return BankTags.tagFor(category.getKey(), item.getSubcategory()).getKey();
			}
			catch (RuntimeException unknownTag)
			{
				return "";
			}
		}

		private void add(LayoutEntry entry)
		{
			entries.add(Objects.requireNonNull(entry, "entry"));
		}

		private BankCategoryPreview toImmutable(GearStatsSource gearStats)
		{
			List<BankPreviewItem> items = items(entries);
			plainRun = false;
			if (main && (category.getSortMode() == BankCategorySortMode.HERBLORE
				|| category.getSortMode() == BankCategorySortMode.FARMING))
			{
				plainRun = true;
				return BankCategoryPreview.fromLogicalItems(category,
					recordBlocks(honorBlockOrder(honorTagOrder(HerbloreItemSorter.layoutMain(items)))));
			}
				switch (category.getSortMode())
				{
				case MAIN:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(IronmanMainItemSorter.sort(items, options.runeOrder(), options.teleportOrder())))),
						MainQuickAccessSemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.MAIN)));
				case RESOURCES:
					return BankCategoryPreview.fromLogicalItems(category, resourceLayout(items));
				case TELEPORTS:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(TeleportItemSorter.sort(items)))), RuneSemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.TELEPORTS)));
				case SUPPLIES:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(SupplyItemSorter.sort(items,
							com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog.INSTANCE,
							main ? PotionDoseOrder.BY_FAMILY : options.potionDoses())))),
						PotionDoseSemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.SUPPLIES)));
				case TOOLS:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(ToolItemSorter.sort(items)))), ToolOutfitSemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.TOOLS)));
				case CURRENCY:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(CurrencyItemSorter.sort(items)))), AchievementDiarySemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.CURRENCY)));
				case FARMING:
					plainRun = sequential(BankCategorySortMode.FARMING);
					return BankCategoryPreview.fromLogicalItems(category, plainRun
						? FarmingItemSorter.sequential(items)
						: FarmingItemSorter.layout(items, 0));
				case GEAR:
					return BankCategoryPreview.fromLogicalItems(category, gearLayout(items, gearStats));
				case CLUES:
					return BankCategoryPreview.fromLogicalItems(category, semanticLayout(
						recordBlocks(honorBlockOrder(honorTagOrder(PresetItemSorter.sort(category, items, gearStats)))),
						CosmeticSetSemanticRuleSet.forEntries(entries),
						sequential(BankCategorySortMode.CLUES)));
				case HERBLORE:
					// The only layout the plan can talk out of its default shape:
					// see BankLayoutStyles for why moving the doses changes it.
					if (herbloreRecipeRows)
					{
						return BankCategoryPreview.fromLogicalItems(category,
							HerbloreItemSorter.layout(items, options.fillHerbloreRows()));
					}
					plainRun = !HerbloreItemSorter.layoutByKindPlacesByColumn(items);
					return BankCategoryPreview.fromLogicalItems(category,
						recordBlocks(honorBlockOrder(honorTagOrder(HerbloreItemSorter.layoutByKind(items)))));
				case BOSS_LOOT:
					plainRun = true;
					return BankCategoryPreview.fromLogicalItems(category,
						recordBlocks(honorBlockOrder(honorTagOrder(PresetItemSorter.sort(category, items, gearStats)))));
				default:
					plainRun = true;
					return BankCategoryPreview.fromLogicalItems(category,
						PresetItemSorter.sort(category, items, gearStats));
			}
		}

		private boolean sequential(BankCategorySortMode mode)
		{
			return options.orderFor(mode) == TabOrder.SEQUENTIAL;
		}

		/**
		 * Gear is the one category with two curated grid shapes, so its layout
		 * carries three values: the four-style best-in-slot matrix (the
		 * default), each set as a vertical column, or each set as one run.
		 */
		private List<BankPreviewItem> gearLayout(List<BankPreviewItem> items, GearStatsSource gearStats)
		{
			if (options.gearLayout() == GearLayout.LIST)
			{
				// Each set reads as one run, strongest first, loose gear
				// flowing after like text.
				plainRun = true;
				return new ArrayList<>(recordBlocks(honorBlockOrder(honorTagOrder(GearItemSorter.bySet(items, gearStats)))));
			}
			if (options.gearLayout() == GearLayout.GRID_SETS || !options.fillGearRows())
			{
				// The semantic engine stacks each curated set as a vertical
				// column and arranges the rest of the kit around it, so the
				// columns stay straight without borrowing filler.
				List<BankPreviewItem> dense = GearItemSorter.dense(items, gearStats);
				List<LayoutEntry> denseEntries = entriesForItems(entries, dense);
				int rows = (denseEntries.size() + GearItemSorter.GRID_COLUMNS - 1)
					/ GearItemSorter.GRID_COLUMNS;
				return semanticLayout(dense,
					GearSetSemanticRuleSet.forEntries(denseEntries, Math.max(1, rows)), false);
			}

			List<BankPreviewItem> equipment = new ArrayList<>(), ammo = new ArrayList<>();
			for (BankPreviewItem item : GearItemSorter.dense(items, gearStats))
				(GearItemSorter.isAmmo(item, gearStats) ? ammo : equipment).add(item);
			GearItemSorter.GearLayout gear = GearItemSorter.plan(equipment, gearStats);
			List<BankPreviewItem> fallback = new ArrayList<>(gear.getSetupRows());
			fallback.addAll(GearItemSorter.bySet(gear.getTail(), gearStats));
			Map<Integer, Integer> targets = GearItemSorter.setupTargets(gear, equipment.size(), gearStats);
			Map<String, List<BankPreviewItem>> cannons = new LinkedHashMap<>();
			for (BankPreviewItem item : fallback)
				if (GearSetSemanticRuleSet.isCannonPart(item.getItemId()))
					cannons.computeIfAbsent(ItemSetCatalog.setKeyOf(item.getItemId()).get(), key -> new ArrayList<>()).add(item);
			// Each finish gets its own physical row run; a combined run may be too wide.
			for (List<BankPreviewItem> cannon : cannons.values())
			{
				for (int start = 0; start + cannon.size() <= equipment.size(); start++)
				{
					if (start % 8 + cannon.size() > 8) continue;
					boolean free = true;
					for (int i = 0; i < cannon.size(); i++) free &= !targets.containsValue(start + i);
					if (!free) continue;
					for (int i = 0; i < cannon.size(); i++) targets.put(cannon.get(i).getItemId(), start + i);
					break;
				}
			}
			List<LayoutEntry> anchored = new ArrayList<>();
			for (LayoutEntry entry : entriesForItems(entries, fallback))
			{
				Integer target = targets.get(entry.getItem().getItemId());
				anchored.add(target == null ? entry : entry.withLockedTarget(target));
			}
			int rows = (equipment.size() + 7) / 8;
			LayoutRequest secondary = GearSetSemanticRuleSet.forEntries(
				entriesForItems(entries, gear.getTail()), Math.max(1, rows));
			List<BankPreviewItem> planned = semanticLayout(fallback,
				new LayoutRequest(anchored, secondary.getRules()), false);
			planned.addAll(ammo);
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
			List<BankPreviewItem> sorted = honorTagOrder(ResourceItemSorter.sort(items));
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
				planned.addAll(semanticLayout(zoneItems, zoneRequest,
					sequential(BankCategorySortMode.RESOURCES)));
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
			LayoutRequest request, boolean sequential)
		{
			plainRun = sequential;
			if (sequential)
			{
				// The sorter's order is the layout: no family rectangles, no
				// row-completing rearrangement, items simply wrap row by row.
				return new ArrayList<>(fallback);
			}

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

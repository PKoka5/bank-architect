package com.pkoka5.ironmanbankarchitect.catalog;

import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsAny;
import static com.pkoka5.ironmanbankarchitect.util.NameMatching.containsWord;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;




public final class ResourceItemRegistry implements ItemCatalog
{
	static final String RESOURCE_PATH = "/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";

	private static final String[] TELEPORT_NEEDLES = {
		"ring of dueling", "games necklace", "amulet of glory", "skills necklace",
		"combat bracelet", "burning amulet", "necklace of passage", "digsite pendant", "ring of wealth",
		"bracelet of ethereum", "teleport", "tablet", "teletab", "jewellery", "ectophial",
		"xeric's talisman", "xeric talisman", "drakan's medallion", "drakans medallion",
		"book of the dead", "magic whistle"
	};

	private static final RequiredResource<java.util.List<String[]>> NAME_RULES =
		new RequiredResource<>("registry name rules", () -> loadNameRules(ResourceItemRegistry.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/registry-name-rules.tsv")));

	public static final ResourceItemRegistry INSTANCE = new ResourceItemRegistry();

	private final RequiredResource<Map<Integer, CatalogItem>> itemsById;
	private final CanonicalItemClassificationOverrides overrides;

	private ResourceItemRegistry()
	{
		this(CanonicalItemClassificationOverrides.INSTANCE);
	}

	ResourceItemRegistry(CanonicalItemClassificationOverrides overrides)
	{
		this.overrides = overrides;
		this.itemsById = new RequiredResource<>("item registry",
			() -> Collections.unmodifiableMap(loadItems(ResourceItemRegistry.class.getResourceAsStream(RESOURCE_PATH), overrides)));
	}

	@Override
	public void requireAvailable()
	{
		overrides.requireAvailable();
		NAME_RULES.get();
		itemsById.get();
		GearTierCatalog.INSTANCE.size();
		WikiItemLists.INSTANCE.requireAvailable();
		com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog.requireAvailable();
	}

	@Override
	public Optional<CatalogItem> findById(int itemId)
	{
		requireAvailable();
		return Optional.ofNullable(itemsById.get().get(itemId));
	}

	public boolean containsId(int itemId)
	{
		requireAvailable();
		return itemsById.get().containsKey(itemId);
	}

	public int size()
	{
		requireAvailable();
		return itemsById.get().size();
	}

	static Map<Integer, CatalogItem> loadItems(InputStream stream, CanonicalItemClassificationOverrides overrides)
	{
		if (stream == null)
		{
			throw new IllegalStateException("Missing item registry resource: " + RESOURCE_PATH);
		}

		Map<Integer, CatalogItem> items = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().isEmpty())
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length != 4)
				{
					throw new IllegalStateException("Invalid item registry line: " + line);
				}

				String itemIdText = fields[0];
				if (!itemIdText.isEmpty() && itemIdText.charAt(0) == '\uFEFF')
				{
					itemIdText = itemIdText.substring(1);
				}

				int itemId = Integer.parseInt(itemIdText);
				String displayName = fields[1];
				if (itemId <= 0 || displayName.trim().isEmpty() || items.containsKey(itemId))
				{
					throw new IllegalStateException("Invalid or duplicate item registry row: " + line);
				}
				ItemCategory explicitCategory = parseRegistryCategory(fields.length >= 3 ? fields[2] : "");
				String constantName = fields.length >= 4 ? fields[3] : "";
				ItemCategory legacyCategory = resolveCategory(displayName, constantName, explicitCategory);
				ItemClassificationRefiner.Classification classification =
					ItemClassificationRefiner.refine(displayName, constantName, legacyCategory);
				Optional<ItemSortMetadata> sortMetadata =
					ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId);
				if (sortMetadata.isPresent() && sortMetadata.get().isFood())
				{
					// ID facts catch potatoes and hunter meats without broad display-name
					// rules that could also capture raw or non-edible items.
					classification = new ItemClassificationRefiner.Classification(ItemCategory.POTION, "food");
				}
				else if (sortMetadata.isPresent()
					&& sortMetadata.get().getVariantKind() == ItemSortMetadata.VariantKind.DOSE
					&& classification.getCategory() == ItemCategory.POTION)
				{
					// A curated canonical ID disambiguates true potion doses from jewellery,
					// waterskins and other unrelated items with numeric name suffixes.
					classification = new ItemClassificationRefiner.Classification(ItemCategory.POTION,
						"potion-dose-" + sortMetadata.get().getVariantValue());
				}
				String subcategory = classification.getSubcategory();
				if (classification.getCategory() == ItemCategory.CLEANUP
					&& "cleanup".equals(subcategory)
					&& WikiItemLists.INSTANCE.isQuestItem(displayName))
				{
					// Wiki-confirmed quest items get an honest review label
					// instead of the generic cleanup bucket.
					subcategory = "quest-item";
				}
				ItemCategory category = classification.getCategory();
				Optional<ItemClassificationRefiner.Classification> wikiClassification =
					WikiItemCategories.INSTANCE.overrideFor(itemId, category);
				if (wikiClassification.isPresent())
				{
					// Wiki category membership is a recorded fact about the item, so it
					// settles the tab where the name-shaped rules above only guessed.
					// Where both already agree, the refiner keeps its finer subcategory:
					// it is what separates herb seeds from the rest of the seeds and
					// partial potion doses from whole potions.
					category = wikiClassification.get().getCategory();
					subcategory = wikiClassification.get().getSubcategory();
				}
				Optional<ItemClassificationRefiner.Classification> canonicalOverride =
					overrides.lookup(itemId);
				if (canonicalOverride.isPresent())
				{
					// Exact item-ID overrides for canonical equipment run last, so cert,
					// placeholder, and Battle Royale records sharing a display name or
					// constant family never inherit them.
					category = canonicalOverride.get().getCategory();
					subcategory = canonicalOverride.get().getSubcategory();
				}
				if ("cosmetics".equals(com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog.domainOf(itemId).orElse(""))
					|| com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog.cosmeticFamilyOf(itemId).isPresent())
				{
					category = ItemCategory.CLUE;
					subcategory = "cosmetic";
				}
				items.putIfAbsent(itemId, new CatalogItem(itemId, displayName, category,
					subcategory, ItemUsageTags.forItem(itemId), null));
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to load item registry resource", ex);
		}

		if (items.isEmpty()) throw new IllegalStateException("Empty item registry");
		return items;
	}

	private static ItemCategory parseRegistryCategory(String value)
	{
		if (value == null || value.trim().isEmpty() || "UNKNOWN".equals(value))
		{
			return null;
		}

		try
		{
			return ItemCategory.valueOf(value);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}

	/**
	 * Explicit registry categories win over the name-based refinement, with two exceptions
	 * where the generated column is known to be unreliable:
	 * teleport jewellery is labelled GEAR or POTION (charge suffixes like "(4)" were read as
	 * potion doses), and every item containing the word "rune" was labelled RUNE (Rune
	 * scimitar, Rune dart, ...). Items without an explicit category are refined by name and
	 * default to CLEANUP for manual review.
	 */
	private static ItemCategory resolveCategory(String displayName, String constantName, ItemCategory explicitCategory)
	{
		String name = (displayName + " " + constantName.replace('_', ' ')).toLowerCase(java.util.Locale.ROOT);
		if (containsAny(name, TELEPORT_NEEDLES))
		{
			return ItemCategory.TELEPORT;
		}
		if (isGearOverride(displayName))
		{
			return ItemCategory.GEAR;
		}
		if (isToolOverride(displayName))
		{
			return ItemCategory.TOOL;
		}
		if (explicitCategory == ItemCategory.RUNE && !isActualRune(displayName))
		{
			return refineCategory(displayName, constantName, ItemCategory.CLEANUP);
		}
		if (explicitCategory == ItemCategory.SKILLING
			&& refineCategory(displayName, constantName, ItemCategory.SKILLING) == ItemCategory.TOOL)
		{
			// Plain tools (hammer, saw, chisel) carry an explicit SKILLING label
			// in the generated registry but belong in the tools tab.
			return ItemCategory.TOOL;
		}
		if (explicitCategory != null)
		{
			return explicitCategory;
		}

		return refineCategory(displayName, constantName, ItemCategory.CLEANUP);
	}

	private static boolean isGearOverride(String displayName)
	{
		String name = displayName.toLowerCase(java.util.Locale.ROOT);
		if (name.startsWith("torag's hammers")) return true;
		if (name.contains("thrownaxe") && !containsAny(name, "head", "crate", "ornament kit")) return true;
		return containsWord(name, "warhammer") && !containsAny(name, "crate", "ornament kit");
	}

	private static boolean isToolOverride(String displayName)
	{
		String name = displayName.toLowerCase(java.util.Locale.ROOT);
		String base = name.replaceFirst("\\s*\\([^)]*\\)$", "").trim();
		if (containsWord(name, "pickaxe") && !containsAny(name, "head", "handle", "kit", "crate")) return true;
		if ((base.equals("harpoon") || base.endsWith(" harpoon")) && !name.contains("crate")) return true;
		if ((base.equals("machete") || base.endsWith(" machete")) && !name.contains("crate")) return true;
		if (base.endsWith("butterfly net") || base.endsWith("fishing rod")
			|| base.equals("barbarian rod") || base.equals("lobster pot") || base.endsWith("fishing net"))
		{
			return true;
		}

		String[] axeMaterials = {"bronze", "iron", "steel", "black", "mithril", "adamant",
			"rune", "dragon", "crystal", "infernal", "3rd age", "gilded", "blessed", "imcando"};
		for (String material : axeMaterials)
		{
			if (base.equals(material + " axe") || base.equals(material + " felling axe")) return true;
		}
		return false;
	}

	private static boolean isActualRune(String displayName)
	{
		String name = displayName.toLowerCase(java.util.Locale.ROOT);
		return name.endsWith(" rune") || name.contains("essence");
	}

	private static ItemCategory refineCategory(String displayName, String constantName, ItemCategory category)
	{
		String name = (displayName + " " + constantName.replace('_', ' ')).toLowerCase(java.util.Locale.ROOT);
		for (String[] rule : NAME_RULES.get())
			for (int i = 2; i < rule.length; i++)
				if ("word".equals(rule[1]) ? containsWord(name, rule[i]) : name.contains(rule[i]))
					return ItemCategory.valueOf(rule[0]);
		return category;
	}

	static java.util.List<String[]> loadNameRules(InputStream stream)
	{
		if (stream == null) throw new IllegalStateException("Missing registry name rules");
		java.util.List<String[]> rules = new java.util.ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			if (!"# schema=1".equals(reader.readLine())) throw new IllegalStateException("Invalid name-rule schema");
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.trim().isEmpty()) continue;
				String[] rule = line.split("\\t", -1);
				if (rule.length < 3 || !("contains".equals(rule[1]) || "word".equals(rule[1])))
					throw new IllegalStateException("Invalid registry name rule");
				ItemCategory.valueOf(rule[0]);
				for (String field : rule) if (field.isEmpty()) throw new IllegalStateException("Empty name-rule field");
				rules.add(rule);
			}
		}
		catch (IOException ex) { throw new IllegalStateException("Cannot read registry name rules", ex); }
		if (rules.isEmpty()) throw new IllegalStateException("Empty registry name rules");
		return Collections.unmodifiableList(rules);
	}

}

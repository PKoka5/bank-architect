package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Item category membership from the Old School RuneScape Wiki
 * (oldschool.runescape.wiki, CC BY-NC-SA 3.0), baked in at build time so the
 * plugin never makes network calls at runtime. Refresh the snapshot with
 * {@code tools/fetch-wiki-item-categories.ps1}.
 *
 * The wiki records which categories an item page belongs to. What a category
 * means for a bank tab is decided here rather than in the data, because most
 * items sit in several at once: a pestle and mortar is Tools and Herblore, and
 * every potion is Potions and Herblore. The first mapping that matches wins, so
 * the order below is the ruling, from the most specific category to the
 * broadest.
 *
 * A mapping is either functional or topical. A functional category describes
 * what an item is for and may replace what the name rules concluded. A topical
 * one only groups pages by subject: Tools holds feathers and rope, and
 * Teleportation items holds every skillcape, so those may name an item the name
 * rules left in review but may never re-file one they already placed.
 *
 * Food and Fish are absent from the snapshot entirely. The wiki files raw meat
 * and raw fish as food and cooked fish as fish, while the bank splits them by
 * whether they are cooking material or something to eat, which the curated
 * {@link ItemSortMetadata} food flag already answers per item ID. Arrows and
 * Bolts are absent for the same reason, holding fletching parts such as
 * arrowtips alongside finished ammunition.
 */
final class WikiItemCategories
{
	static final String RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/wiki-item-categories.tsv";

	private static final List<Mapping> MAPPINGS = Collections.unmodifiableList(Arrays.asList(
		Mapping.functional("Runes", ItemCategory.RUNE, "rune"),
		// Skilling outfits belong beside the tools they are worn with rather
		// than in the resources tab with the raw materials, and they rank above
		// teleports because a skillcape is filed under both: it is worn for the
		// skill and teleports as a side effect.
		Mapping.functional("Skilling equipment", ItemCategory.TOOL, "skilling-equipment"),
		Mapping.topical("Teleportation items", ItemCategory.TELEPORT, "teleport"),
		Mapping.functional("Clue scrolls", ItemCategory.CLUE, "treasure-trail"),
		// Tools outrank the workflow categories below so the implement stays
		// with the other tools: a seed dibber is not a seed, and a pestle and
		// mortar is not a Herblore ingredient.
		Mapping.topical("Tools", ItemCategory.TOOL, "tool"),
		Mapping.topical("Potions", ItemCategory.POTION, "potion"),
		Mapping.functional("Herbs", ItemCategory.HERBLORE, "herb"),
		Mapping.functional("Seeds", ItemCategory.FARMING, "seed"),
		Mapping.functional("Saplings", ItemCategory.FARMING, "sapling"),
		// Everything left in the broad Herblore category once potions, herbs
		// and seeds have been claimed: vials, secondaries, pastes and mixtures.
		Mapping.functional("Herblore", ItemCategory.HERBLORE, "herblore-supply"),
		Mapping.functional("Currency", ItemCategory.CURRENCY, "currency"),
		Mapping.functional("Ores", ItemCategory.SKILLING, "ore"),
		Mapping.functional("Metal bars", ItemCategory.SKILLING, "bar"),
		Mapping.functional("Logs", ItemCategory.SKILLING, "log"),
		// Gems carries the slayer master's enchanted gem alongside the real ones.
		Mapping.topical("Gems", ItemCategory.SKILLING, "gem"),
		Mapping.functional("Bones", ItemCategory.SKILLING, "bones"),
		Mapping.functional("Leather", ItemCategory.SKILLING, "leather")
	));

	static final WikiItemCategories INSTANCE = new WikiItemCategories();

	private final Map<Integer, Mapping> mappingsById;

	private WikiItemCategories()
	{
		this.mappingsById = Collections.unmodifiableMap(load());
	}

	/**
	 * The classification this snapshot has for the item, or empty when it has
	 * none or when it is not allowed to replace {@code current}.
	 */
	Optional<ItemClassificationRefiner.Classification> overrideFor(int itemId, ItemCategory current)
	{
		Mapping mapping = mappingsById.get(itemId);
		if (mapping == null || !overrules(current, mapping.category, mapping.functional))
		{
			return Optional.empty();
		}

		return Optional.of(new ItemClassificationRefiner.Classification(
			mapping.category, mapping.subcategory));
	}

	int size()
	{
		return mappingsById.size();
	}

	/**
	 * The first mapping whose wiki category the item belongs to, or empty when
	 * the item carries none of the categories this snapshot tracks.
	 */
	static Optional<Mapping> classify(List<String> wikiCategories)
	{
		for (Mapping mapping : MAPPINGS)
		{
			if (wikiCategories.contains(mapping.wikiCategory))
			{
				return Optional.of(mapping);
			}
		}

		return Optional.empty();
	}

	/**
	 * Whether a wiki category may replace the classification the name rules
	 * arrived at.
	 *
	 * A topical category only ever promotes an item the name rules left for
	 * review. Beyond that, two moves are refused outright. Gear never leaves the
	 * combat tab for carrying a teleport, because a great many teleports are
	 * worn. And nothing crosses between the Herblore and potion tabs, which
	 * split by workflow stage rather than by what an item is: one holds what a
	 * potion is made from and made into, the other holds what gets drunk. The
	 * wiki records no such stage and files every unfinished potion under both.
	 */
	static boolean overrules(ItemCategory current, ItemCategory wikiCategory, boolean functional)
	{
		if (current == wikiCategory)
		{
			return false;
		}
		if (!functional)
		{
			return isUnplaced(current);
		}
		if (current == ItemCategory.GEAR && wikiCategory == ItemCategory.TELEPORT)
		{
			return false;
		}

		return !(current == ItemCategory.HERBLORE && wikiCategory == ItemCategory.POTION
			|| current == ItemCategory.POTION && wikiCategory == ItemCategory.HERBLORE);
	}

	private static boolean isUnplaced(ItemCategory category)
	{
		return category == ItemCategory.CLEANUP
			|| category == ItemCategory.UNCATEGORIZED
			|| category == ItemCategory.UNKNOWN;
	}

	private static Map<Integer, Mapping> load()
	{
		InputStream stream = WikiItemCategories.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing wiki category resource: " + RESOURCE_PATH);
		}

		Map<Integer, Mapping> mappings = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (!line.isEmpty() && line.charAt(0) == '\uFEFF')
				{
					line = line.substring(1);
				}
				if (line.trim().isEmpty() || line.charAt(0) == '#')
				{
					continue;
				}

				String[] fields = line.split("\t", -1);
				if (fields.length < 3)
				{
					throw new IllegalStateException("Invalid wiki category line: " + line);
				}

				int itemId;
				try
				{
					itemId = Integer.parseInt(fields[0].trim());
				}
				catch (NumberFormatException ex)
				{
					throw new IllegalStateException("Invalid wiki category item id: " + line, ex);
				}

				List<String> wikiCategories = Arrays.asList(fields[2].trim().split(","));
				int id = itemId;
				classify(wikiCategories).ifPresent(mapping -> mappings.put(id, mapping));
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to load wiki category resource", ex);
		}

		return mappings;
	}

	static final class Mapping
	{
		private final String wikiCategory;
		private final ItemCategory category;
		private final String subcategory;
		private final boolean functional;

		private Mapping(String wikiCategory, ItemCategory category, String subcategory, boolean functional)
		{
			this.wikiCategory = wikiCategory;
			this.category = category;
			this.subcategory = subcategory;
			this.functional = functional;
		}

		static Mapping functional(String wikiCategory, ItemCategory category, String subcategory)
		{
			return new Mapping(wikiCategory, category, subcategory, true);
		}

		static Mapping topical(String wikiCategory, ItemCategory category, String subcategory)
		{
			return new Mapping(wikiCategory, category, subcategory, false);
		}

		ItemCategory getCategory()
		{
			return category;
		}

		boolean isFunctional()
		{
			return functional;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankTags;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 * A set is one thing to the player: an outfit worn together must never have
 * its pieces classified onto different tabs. Builder's boots on the clue tab
 * while the shirt sat with the quest items is exactly the kind of split this
 * walks the whole catalog for, so the next one surfaces here instead of in
 * someone's bank.
 */
public class ItemSetCategoryConsistencyTest
{
	private static final String SET_CATALOG_PATH =
		"/com/pkoka5/ironmanbankarchitect/organize/item-set-catalog.tsv";

	/**
	 * Outfits that are not in the set catalog but are one set to the player.
	 */
	private static final Map<String, int[]> SUPPLEMENTAL_SETS = supplementalSets();

	/**
	 * Sets whose split is deliberate. Every entry needs a reason a player
	 * would accept.
	 */
	private static final Set<String> INTENTIONAL_SPLITS = new LinkedHashSet<>(Arrays.asList(
		// The rune kiteshield is on the reviewed Ironman loot list
		// (PresetCategoryMapper.IRONMAN_REVIEWED_LOOT_IDS) while the rest of
		// the rune set counts as gear - an explicit upstream curation choice.
		"gear.rune-armour",
		// The mourner gloves and boots are pinned as functional gear by the
		// combat-gear quest audit while the soft pieces are costumes; the
		// clues and cosmetics tags share one tab in every bundled plan, so
		// the split never separates the outfit in a real bank.
		"cosmetics.mourner-outfit"));

	@Test
	public void everySetKeepsAllItsPiecesOnOneTab()
	{
		Map<String, List<Integer>> sets = new LinkedHashMap<>();
		for (Map.Entry<String, int[]> supplement : SUPPLEMENTAL_SETS.entrySet())
		{
			List<Integer> ids = new ArrayList<>();
			Arrays.stream(supplement.getValue()).forEach(ids::add);
			sets.put(supplement.getKey(), ids);
		}
		sets.putAll(catalogSets());

		List<String> splits = new ArrayList<>();
		for (Map.Entry<String, List<Integer>> set : sets.entrySet())
		{
			if (INTENTIONAL_SPLITS.contains(set.getKey()))
			{
				continue;
			}
			Set<String> destinations = new LinkedHashSet<>();
			List<String> detail = new ArrayList<>();
			for (Integer itemId : set.getValue())
			{
				CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId);
				BankCategory category = PresetCategoryMapper.map(BankPresets.IRONMAN, item);
				String destination = category.getKey() + "/"
					+ BankTags.tagFor(category.getKey(), item.getSubcategory()).getKey();
				destinations.add(destination);
				detail.add(itemId + ":" + destination);
			}
			if (destinations.size() > 1)
			{
				splits.add(set.getKey() + " -> " + detail);
			}
		}

		assertTrue("sets split across tabs:\n" + String.join("\n", splits), splits.isEmpty());
	}

	private static Map<String, int[]> supplementalSets()
	{
		Map<String, int[]> sets = new LinkedHashMap<>();
		// Builder's outfit: hard hat, shirt, trousers, boots (Tower of Life).
		sets.put("supplemental.builders-outfit", new int[] {10862, 10863, 10864, 10865});
		// Plague outfit: jacket and trousers (Plague City).
		sets.put("supplemental.plague-outfit", new int[] {284, 285});
		// Xerician robes: hat, top and robe.
		sets.put("supplemental.xerician-robes", new int[] {13385, 13387, 13389});
		return sets;
	}

	private static Map<String, List<Integer>> catalogSets()
	{
		Map<String, List<Integer>> sets = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
			ItemSetCategoryConsistencyTest.class.getResourceAsStream(SET_CATALOG_PATH),
			StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] columns = line.split("\t");
				if (columns.length < 5)
				{
					continue;
				}
				sets.computeIfAbsent(columns[1], key -> new ArrayList<>())
					.add(Integer.parseInt(columns[4].trim()));
			}
		}
		catch (Exception e)
		{
			throw new AssertionError("could not read the set catalog", e);
		}
		assertTrue("set catalog looks empty", sets.size() > 10);
		return sets;
	}
}

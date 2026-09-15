package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact combat families whose reviewed equipment order is vertical outside the primary setups. */
public final class GearSetSemanticRuleSet
{

	/**
	 * Every curated gear set's item IDs in slot order, for layouts that read a
	 * set as one run. Sets keep the catalog's order.
	 */
	public static java.util.List<java.util.List<Integer>> gearSetsInSlotOrder()
	{
		java.util.List<java.util.List<Integer>> sets = new java.util.ArrayList<>();
		for (ItemSetCatalog.SetDefinition set : ItemSetCatalog.sets("gear"))
		{
			sets.add(set.getItemIds());
		}
		return sets;
	}

	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		GearSetSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"), 0);
	private static final RequiredResource<List<SetFact>> SETS =
		new RequiredResource<>("gear layout", GearSetSemanticRuleSet::buildSets);

	private GearSetSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		return forEntries(entries, Integer.MAX_VALUE);
	}

	/**
	 * Builds the vertical-set request for the physically available gear-tail height. A set that
	 * lost a primary BIS member may otherwise be one item taller than the dense tail. When its
	 * owned remainder forms an exact rectangle, keep the equipment order in adjacent vertical
	 * columns instead of abandoning the family to dense fallback.
	 */
	public static LayoutRequest forEntries(List<LayoutEntry> entries, int maxVerticalHeight)
	{
		Objects.requireNonNull(entries, "entries");
		if (maxVerticalHeight < 1)
		{
			throw new IllegalArgumentException("maxVerticalHeight must be positive");
		}

		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}

		List<ItemSetCatalog.SetDefinition> verticalDefinitions = new ArrayList<>();
		List<SemanticRule> rules = new ArrayList<>();
		for (ItemSetCatalog.SetDefinition definition : definitions())
		{
			List<Integer> owned = new ArrayList<>();
			for (Integer itemId : definition.getItemIds())
			{
				if (present.contains(itemId))
				{
					owned.add(itemId);
				}
			}

			SemanticRule compact = owned.size() > maxVerticalHeight
				? compactRectangleRule(definition.getKey(), owned, maxVerticalHeight) : null;
			if (compact == null)
			{
				verticalDefinitions.add(definition);
			}
			else
			{
				rules.add(compact);
			}
		}

		SemanticRule rule = VerticalItemSetRuleFactory.build(
			"gear.vertical-sets", entries, verticalDefinitions);
		if (rule != null)
		{
			rules.add(rule);
		}
		return new LayoutRequest(entries, rules);
	}

	private static SemanticRule compactRectangleRule(String setKey, List<Integer> owned,
		int maxVerticalHeight)
	{
		int columns = 2;
		while (columns <= SemanticRule.MAX_WIDTH
			&& (owned.size() % columns != 0 || owned.size() / columns > maxVerticalHeight))
		{
			columns++;
		}
		if (columns > SemanticRule.MAX_WIDTH)
		{
			return null;
		}

		int rows = owned.size() / columns;
		List<SemanticAtom> atoms = new ArrayList<>(rows);
		for (int row = 0; row < rows; row++)
		{
			List<SemanticAtom.Member> members = new ArrayList<>(columns);
			for (int column = 0; column < columns; column++)
			{
				members.add(new SemanticAtom.Member("column-" + column,
					owned.get(column * rows + row)));
			}
			atoms.add(new SemanticAtom("row-" + row, members));
		}
		return SemanticRule.builder()
			.ruleKey("gear.compact." + setKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(Collections.singleton(columns))
			.build();
	}

	/**
	 * Returns real owned members of eligible vertical families. The gear setup planner uses this
	 * exact-ID set to keep non-primary family members out of arbitrary row-filler cells.
	 */
	public static Set<Integer> presentFamilyItemIds(List<BankPreviewItem> items)
	{
		Objects.requireNonNull(items, "items");
		Set<Integer> present = new LinkedHashSet<>();
		for (BankPreviewItem item : items)
		{
			present.add(item.getItemId());
		}

		Set<Integer> familyItems = new LinkedHashSet<>();
		for (SetFact set : SETS.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int itemId : set.itemIds)
			{
				if (present.contains(itemId))
				{
					owned.add(itemId);
				}
			}
			if (owned.size() >= 2)
			{
				familyItems.addAll(owned);
			}
		}
		return Collections.unmodifiableSet(familyItems);
	}

	/**
	 * How many members of its own family the player owns, per owned family member. An item in no
	 * eligible family is absent. The gear setup planner uses this to break a tie between equally
	 * tiered candidates for a primary cell: a piece of a family the player actually holds wins
	 * over a loose item of the same tier, so owning a set cannot cost you its body row.
	 */
	public static Map<Integer, Integer> ownedFamilySizeByItemId(List<BankPreviewItem> items)
	{
		Objects.requireNonNull(items, "items");
		Set<Integer> present = new LinkedHashSet<>();
		for (BankPreviewItem item : items)
		{
			present.add(item.getItemId());
		}

		Map<Integer, Integer> sizeByItemId = new LinkedHashMap<>();
		for (SetFact set : SETS.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int itemId : set.itemIds)
			{
				if (present.contains(itemId))
				{
					owned.add(itemId);
				}
			}
			if (owned.size() < 2)
			{
				continue;
			}
			// An item can sit in more than one family; the largest one it is
			// actually part of is the one worth keeping whole.
			for (int itemId : owned)
			{
				sizeByItemId.merge(itemId, owned.size(), Math::max);
			}
		}
		return Collections.unmodifiableMap(sizeByItemId);
	}

	private static List<SetFact> buildSets()
	{
		List<SetFact> sets = new ArrayList<>();
		TABLE.entries().forEach((key, ids) -> sets.add(set(key, ids.stream().mapToInt(Integer::intValue).toArray())));

		Set<Integer> reserved = new LinkedHashSet<>();
		for (SetFact set : sets)
		{
			for (int itemId : set.itemIds) reserved.add(itemId);
		}
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("gear"))
		{
			boolean overlaps = false;
			for (Integer itemId : definition.getItemIds())
			{
				if (reserved.contains(itemId))
				{
					overlaps = true;
					break;
				}
			}
			if (!overlaps)
			{
				int[] itemIds = new int[definition.getItemIds().size()];
				for (int index = 0; index < itemIds.length; index++)
				{
					itemIds[index] = definition.getItemIds().get(index);
					reserved.add(itemIds[index]);
				}
				sets.add(set(definition.getKey(), itemIds));
			}
		}
		return Collections.unmodifiableList(sets);
	}

	private static List<ItemSetCatalog.SetDefinition> definitions()
	{
		List<ItemSetCatalog.SetDefinition> definitions = new ArrayList<>();
		for (SetFact set : SETS.get())
		{
			List<Integer> itemIds = new ArrayList<>(set.itemIds.length);
			for (int itemId : set.itemIds) itemIds.add(itemId);
			definitions.add(ItemSetCatalog.definition("gear", set.key, set.key, itemIds));
		}
		return definitions;
	}

	private static SetFact set(String key, int... itemIds)
	{
		return new SetFact(key, itemIds);
	}

	private static final class SetFact
	{
		private final String key;
		private final int[] itemIds;

		private SetFact(String key, int[] itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}
}

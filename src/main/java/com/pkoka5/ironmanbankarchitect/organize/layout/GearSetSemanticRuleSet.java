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
	private static final RequiredResource<List<ItemSetCatalog.SetDefinition>> SETS =
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
		for (ItemSetCatalog.SetDefinition definition : SETS.get())
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
		return ownedFamilySizeByItemId(items).keySet();
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
		for (ItemSetCatalog.SetDefinition set : SETS.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int itemId : set.getItemIds())
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

	private static List<ItemSetCatalog.SetDefinition> buildSets()
	{
		List<ItemSetCatalog.SetDefinition> sets = new ArrayList<>();
		TABLE.entries().forEach((key, ids) -> sets.add(ItemSetCatalog.definition("gear", key, key, ids)));

		Set<Integer> reserved = new LinkedHashSet<>();
		for (ItemSetCatalog.SetDefinition set : sets) reserved.addAll(set.getItemIds());
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("gear"))
		{
			if (Collections.disjoint(reserved, definition.getItemIds()))
			{
				reserved.addAll(definition.getItemIds());
				sets.add(ItemSetCatalog.definition("gear", definition.getKey(), definition.getKey(), definition.getItemIds()));
			}
		}
		return Collections.unmodifiableList(sets);
	}
}

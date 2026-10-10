package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.*;
import java.util.List;




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
		for (ItemSetCatalog.SetDefinition set : SETS.get())
		{
			sets.add(set.getItemIds());
		}
		return sets;
	}

	public static boolean isCannonPart(int itemId)
	{
		return ItemSetCatalog.setKeyOf(itemId).orElse("").startsWith("gear.dwarf-cannon");
	}

	/** Accept an ordered dense run, a vertical column, or an exact column-major rectangle. */
	public static boolean keepsFamiliesTogether(List<BankPreviewItem> items, int physicalStart)
	{
		Map<Integer, Integer> positions = new HashMap<>();
		for (int i = 0; i < items.size(); i++) positions.put(items.get(i).getItemId(), physicalStart + i);
		for (ItemSetCatalog.SetDefinition set : SETS.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int id : set.getItemIds()) if (positions.containsKey(id)) owned.add(positions.get(id));
			if (owned.size() < 2) continue;
			int first = owned.get(0), minRow = first / 8, maxRow = minRow;
			int minCol = first % 8, maxCol = minCol;
			boolean run = true;
			for (int i = 0; i < owned.size(); i++)
			{
				int target = owned.get(i);
				run &= target == first + i;
				minRow = Math.min(minRow, target / 8); maxRow = Math.max(maxRow, target / 8);
				minCol = Math.min(minCol, target % 8); maxCol = Math.max(maxCol, target % 8);
			}
			if (run) continue;
			int height = maxRow - minRow + 1;
			if (height * (maxCol - minCol + 1) != owned.size()) return false;
			for (int i = 0; i < owned.size(); i++)
				if (owned.get(i) != minRow * 8 + minCol + i % height * 8 + i / height) return false;
		}
		return true;
	}

	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		GearSetSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"), 0);
	private static final OrderedItemFamilies ROLES = new OrderedItemFamilies(
		GearSetSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/combat-gear-roles.tsv"), 0);
	private static final RequiredResource<List<ItemSetCatalog.SetDefinition>> SETS =
		new RequiredResource<>("gear layout", GearSetSemanticRuleSet::buildSets);

	private GearSetSemanticRuleSet()
	{
	}

	/** Navigation preferences only; never evidence that another item can be alched. */
	public static int frontlinePriority(int itemId)
	{
		for (Map.Entry<String, List<Integer>> row : ROLES.entries().entrySet())
			if (row.getValue().contains(itemId)) return Integer.parseInt(row.getKey().split("/", 3)[0]);
		return 0;
	}

	public static String weaponRole(int itemId)
	{
		for (Map.Entry<String, List<Integer>> row : ROLES.entries().entrySet())
			if (row.getValue().contains(itemId))
			{
				String role = row.getKey().split("/", 3)[1];
				return role.startsWith("weapon-") ? role : "";
			}
		return "";
	}

	public static boolean isBowfa(int itemId)
	{
		return ROLES.ids("0/weapon-ranged-bow/bowfa").contains(itemId);
	}

	public static boolean isCrystalArmour(int itemId)
	{
		return TABLE.entries().entrySet().stream()
			.anyMatch(row -> row.getKey().startsWith("gear.crystal") && row.getValue().contains(itemId));
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
		Map<Integer, Integer> cannonTargets = new LinkedHashMap<>();
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
			if (definition.getKey().startsWith("gear.dwarf-cannon"))
			{
				for (int itemId : owned) cannonTargets.put(itemId, cannonTargets.size());
				continue;
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
		List<LayoutEntry> anchored = new ArrayList<>(entries.size());
		for (LayoutEntry entry : entries)
		{
			Integer target = cannonTargets.get(entry.getItem().getItemId());
			anchored.add(target == null ? entry : entry.withLockedTarget(target));
		}
		return new LayoutRequest(anchored, rules);
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
		// Weapons already belonging to an armour set retain that set. Shared gear appears once.
		Map<String, List<Integer>> weaponRoles = new LinkedHashMap<>();
		ROLES.entries().forEach((key, ids) ->
		{
			String role = key.split("/", 3)[1];
			if (role.startsWith("weapon-"))
				for (int id : ids)
					if (reserved.add(id)) weaponRoles.computeIfAbsent(role, ignored -> new ArrayList<>()).add(id);
		});
		weaponRoles.forEach((role, ids) -> sets.add(
			ItemSetCatalog.definition("gear", "gear.role." + role, role, ids)));
		return Collections.unmodifiableList(sets);
	}
}

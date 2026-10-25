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
	public static List<List<Integer>> gearSetsInSlotOrder()
	{
		List<List<Integer>> sets = new ArrayList<>();
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

	public static boolean isVoid(int itemId) { return TABLE.ids("gear.void").contains(itemId); }

	/** Preserve partial kits too; duplicate physical copies of one item are not another set piece. */
	public static boolean keepsVoidTogether(java.util.stream.IntStream itemIds)
	{
		return itemIds.filter(GearSetSemanticRuleSet::isVoid).distinct().limit(2).count() == 2;
	}

	public static boolean isMeleeTankArmour(int itemId)
	{
		return ItemSetCatalog.setKeyOf(itemId).orElse("").startsWith("gear.justiciar")
			|| TABLE.ids("gear.barrows-verac").contains(itemId);
	}

	/** Packs actual families atomically; all-dense canonical order is the bounded-search fallback. */
	public static List<BankPreviewItem> packSections(List<BankPreviewItem> dense, int physicalStart,
		java.util.function.IntUnaryOperator slotOf)
	{
		return packSections(dense,physicalStart,slotOf,Collections.emptyMap());
	}

	public static List<BankPreviewItem> packSections(List<BankPreviewItem> dense, int physicalStart,
		java.util.function.IntUnaryOperator slotOf, Map<Integer,Integer> targets)
	{
		Map<Integer, BankPreviewItem> present = new LinkedHashMap<>();
		Map<Integer, Integer> ranks = new HashMap<>();
		for (BankPreviewItem item : dense) { present.put(item.getItemId(), item); ranks.put(item.getItemId(), ranks.size()); }
		List<List<Integer>> blocks = new ArrayList<>();
		Set<Integer> used = new HashSet<>(targets.keySet());
		for (ItemSetCatalog.SetDefinition family : SETS.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int id : family.getItemIds()) if (present.containsKey(id) && !used.contains(id)) owned.add(id);
			if (owned.size() >= 2 || owned.size() == 1 && family.getKey().startsWith("gear.role."))
			{ blocks.add(owned); used.addAll(owned); }
		}
		List<Integer> rings = new ArrayList<>();
		if (!targets.isEmpty()) for (BankPreviewItem item : dense)
			if (!used.contains(item.getItemId()) && slotOf.applyAsInt(item.getItemId()) == 12) rings.add(item.getItemId());
		if (!rings.isEmpty()) { blocks.add(rings); used.addAll(rings); }
		if (!targets.isEmpty()) for (BankPreviewItem item : dense)
			if (!used.contains(item.getItemId()) && weapon(item.getItemId(),slotOf))
			{ blocks.add(Collections.singletonList(item.getItemId())); used.add(item.getItemId()); }
		blocks.sort(Comparator.comparingInt((List<Integer> block) -> block == rings ? -2
			: !targets.isEmpty() && block.stream().allMatch(id -> weapon(id,slotOf)) ? -1 : 0)
			.thenComparingInt(block -> block.stream().allMatch(id -> weapon(id,slotOf)) ? slotOf.applyAsInt(block.get(0)) : 0)
			.thenComparingInt(block -> block.stream().mapToInt(ranks::get).min().getAsInt()));
		Map<Integer,Integer> origins = new HashMap<>();
		if (!rings.isEmpty()) origins.put(rings.get(0),targets.entrySet().stream()
			.filter(entry -> slotOf.applyAsInt(entry.getKey()) == 12).mapToInt(Map.Entry::getValue).max()
			.orElse(ranks.get(rings.get(0)) - 1) + 1);
		Map<List<Integer>,List<int[]>> shapes = new HashMap<>();
		int weaponWidth = 8 - (targets.values().stream().mapToInt(at -> (physicalStart+at)%8).max().orElse(7)+1);
		for (List<Integer> block : blocks)
		{
			boolean weapons = block.stream().allMatch(id -> !weaponRole(id).isEmpty());
			Map<Integer, List<Integer>> slots = new LinkedHashMap<>();
			for (int id : block) slots.computeIfAbsent(slotOf.applyAsInt(id), key -> new ArrayList<>()).add(id);
			int width = slots.values().iterator().next().size();
			List<int[]> alternatives = new ArrayList<>();
			if (weapons && weaponWidth > 1 && block.size() > weaponWidth)
			{
				int[] flow = new int[block.size()];
				for (int i = 0; i < flow.length; i++) flow[i] = i / weaponWidth * 8 + i % weaponWidth;
				alternatives.add(flow);
			}
			if (!weapons && width <= 8 && slots.values().stream().allMatch(row -> row.size() == width))
			{
				int[] matrix = new int[block.size()];
				for (int i = 0; i < matrix.length; i++) matrix[i] = i / width * 8 + i % width;
				alternatives.add(matrix);
			}
			if (!weapons && (slots.size() == block.size() || block == rings)) for (int columns = 2; columns <= 8; columns++)
			{
				if (block.size() % columns != 0) continue;
				int rows = block.size() / columns;
				int[] rectangle = new int[block.size()];
				for (int i = 0; i < rectangle.length; i++) rectangle[i] = i % rows * 8 + i / rows;
				alternatives.add(rectangle);
			}
			int[] compact = new int[block.size()];
			for (int i = 0; i < compact.length; i++) compact[i] = i;
			alternatives.add(compact); shapes.put(block,alternatives);
		}
		BankPreviewItem[] cells = new BankPreviewItem[dense.size()];
		seed(cells,present,targets);
		if (!placeBlocks(0, blocks, shapes, present, cells, physicalStart, origins, new int[] {10000}))
		{
			// Large condition matrices must not force every smaller armour set into a flat run.
			for (List<Integer> block : blocks)
			{
				List<int[]> alternatives = shapes.get(block);
				int[] preferred = alternatives.get(0);
				if (block.size() > 8 || preferred[preferred.length - 1] < 8)
					shapes.put(block,Collections.singletonList(alternatives.get(alternatives.size()-1)));
			}
			// If reserving the weapon area exhausts the search, place armour families first.
			blocks.sort(Comparator.comparingInt(block -> block == rings ? -1
				: block.stream().mapToInt(ranks::get).min().getAsInt()));
			Arrays.fill(cells, null); seed(cells,present,targets);
			if (!placeBlocks(0, blocks, shapes, present, cells, physicalStart, origins, new int[] {10000}))
			{
				Arrays.fill(cells,null); seed(cells,present,targets); used = new HashSet<>(targets.keySet());
			}
		}
		int next = 0;
		for (BankPreviewItem item : dense) if (!used.contains(item.getItemId()))
		{
			while (cells[next] != null) next++;
			cells[next] = item;
		}
		return Arrays.asList(cells);
	}

	private static boolean weapon(int id, java.util.function.IntUnaryOperator slotOf)
	{
		int slot = slotOf.applyAsInt(id); return slot >= 8 && slot <= 10;
	}

	private static void seed(BankPreviewItem[] cells, Map<Integer,BankPreviewItem> items, Map<Integer,Integer> targets)
	{
		for (Map.Entry<Integer,Integer> entry : targets.entrySet())
		{
			int at = entry.getValue();
			if (at < 0 || at >= cells.length || cells[at] != null || !items.containsKey(entry.getKey()))
				throw new IllegalArgumentException("Invalid Combat target " + entry);
			cells[at] = items.get(entry.getKey());
		}
	}

	private static boolean placeBlocks(int group, List<List<Integer>> blocks,
		Map<List<Integer>,List<int[]>> shapes, Map<Integer, BankPreviewItem> items, BankPreviewItem[] cells,
		int physicalStart, Map<Integer,Integer> origins, int[] budget)
	{
		if (group == blocks.size()) return true;
		List<Integer> block = blocks.get(group);
		int first = 0;
		if (block.size() == 1 && !weaponRole(block.get(0)).isEmpty() && !weaponRole(block.get(0)).equals("weapon-quest"))
			for (int previous = group - 1; previous >= 0; previous--)
				if (blocks.get(previous).stream().allMatch(id -> !weaponRole(id).isEmpty()))
				{ first = Arrays.asList(cells).indexOf(items.get(blocks.get(previous).get(0))) + 1; break; }
		for (int[] shape : shapes.get(block))
		{
			boolean matrix = shape != shapes.get(block).get(shapes.get(block).size() - 1)
				|| block.size() <= 8 && block.stream().allMatch(GearSetSemanticRuleSet::isVoid);
			int width = 1;
			if (matrix) for (int offset : shape) width = Math.max(width, offset % 8 + 1);
			int last = cells.length - 1 - shape[shape.length - 1];
			int origin = Math.max(first,Math.min(last,origins.getOrDefault(block.get(0),first)));
			for (int step = 0; step < cells.length * 2; step++)
			{
				int start = origin + (step + 1) / 2 * (step % 2 == 0 ? 1 : -1);
				if (start < first || start > last) continue;
				if (--budget[0] < 0) return false;
				if (matrix && (physicalStart + start) % 8 + width > 8) continue;
				boolean free = true;
				for (int offset : shape) if (cells[start + offset] != null) { free = false; break; }
				if (!free) continue;
				for (int i = 0; i < shape.length; i++) cells[start + shape[i]] = items.get(block.get(i));
				if (placeBlocks(group + 1, blocks, shapes, items, cells, physicalStart, origins, budget)) return true;
				for (int offset : shape) cells[start + offset] = null;
			}
		}
		return false;
	}


	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		GearSetSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"), 0);
	private static final OrderedItemFamilies ROLES = new OrderedItemFamilies(
		GearSetSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/combat-gear-roles.tsv"), 0);
	private static final RequiredResource<Map<Integer, String[]>> ROLE_FACTS =
		new RequiredResource<>("combat roles", GearSetSemanticRuleSet::roleFacts);

	private static Map<Integer, String[]> roleFacts()
	{
		Map<Integer, String[]> facts = new HashMap<>();
		ROLES.entries().forEach((key, ids) -> ids.forEach(id -> facts.put(id, key.split("/", 3))));
		return Collections.unmodifiableMap(facts);
	}
	private static final RequiredResource<List<ItemSetCatalog.SetDefinition>> SETS =
		new RequiredResource<>("gear layout", GearSetSemanticRuleSet::buildSets);

	private GearSetSemanticRuleSet()
	{
	}

	/** Navigation preferences only; never evidence that another item can be alched. */
	public static int frontlinePriority(int itemId)
	{
		String[] fact = ROLE_FACTS.get().get(itemId);
		return fact == null ? 0 : Integer.parseInt(fact[0]);
	}

	public static String roleOf(int itemId)
	{
		String[] fact = ROLE_FACTS.get().get(itemId);
		return fact == null ? "" : fact[1];
	}

	public static String weaponRole(int itemId)
	{
		String role = roleOf(itemId);
		return role.startsWith("weapon-") ? role : "";
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
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("tools"))
			if (definition.getKey().startsWith("gear.dwarf-cannon")) sets.add(definition);
		// Weapons already belonging to an armour set retain that set. Shared gear appears once.
		Map<String, List<Integer>> weaponRoles = new LinkedHashMap<>();
		ROLES.entries().forEach((key, ids) ->
		{
			String role = key.split("/", 3)[1];
			if (role.startsWith("weapon-") || role.equals("support-defence"))
				for (int id : ids)
					weaponRoles.computeIfAbsent(role, ignored -> new ArrayList<>()).add(id);
		});
		weaponRoles.forEach((role, ids) -> sets.add(
			ItemSetCatalog.definition("gear", "gear.role." + role, role, ids)));
		return Collections.unmodifiableList(sets);
	}
}

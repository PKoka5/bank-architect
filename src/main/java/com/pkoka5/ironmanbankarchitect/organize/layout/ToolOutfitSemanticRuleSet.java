package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.*;
import java.util.List;




/** Exact canonical outfit columns plus compact horizontal tool runs grouped by primary skill. */
public final class ToolOutfitSemanticRuleSet
{
	private static final String OUTFIT_RULE_KEY = "tool.outfit-columns";
	private static final String SKILL_RUN_RULE_KEY = "tool.primary-skill-runs";
	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		ToolOutfitSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/tool-layout-families.tsv"), 0, false);
	private static final RequiredResource<List<OutfitFact>> OUTFITS =
		new RequiredResource<>("tool outfits", ToolOutfitSemanticRuleSet::buildOutfits);
	private static final RequiredResource<List<OutfitFact>> TOOL_FAMILIES = new RequiredResource<>("tool families", () ->
	{
		List<OutfitFact> families = new ArrayList<>();
		TABLE.group("families").forEach((key, ids) -> families.add(outfit(key, ids)));
		return Collections.unmodifiableList(families);
	});

	private static List<OutfitFact> buildOutfits()
	{
		List<OutfitFact> outfits = new ArrayList<>();
		TABLE.group("outfits").forEach((key, ids) ->
		{
			if (key.endsWith("/outfit.farmer"))
			{
				for (int variant = 0; variant < 2; variant++)
				{
					List<Integer> pieces = new ArrayList<>();
					for (int slot = variant; slot < ids.size(); slot += 2) pieces.add(ids.get(slot));
					outfits.add(outfit(key + "-" + variant, pieces));
				}
			}
			else outfits.add(outfit(key, ids));
		});

		Set<Integer> reserved = new LinkedHashSet<>();
		for (OutfitFact outfit : outfits)
		{
			for (int itemId : outfit.itemIds) reserved.add(itemId);
		}
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("tools"))
		{
			if (definition.getKey().startsWith("gear.dwarf-cannon")) continue;
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
				reserved.addAll(definition.getItemIds());
				outfits.add(outfit(definition.getKey(), definition.getItemIds()));
			}
		}
		return Collections.unmodifiableList(outfits);
	}

	private ToolOutfitSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		List<LayoutEntry> anchored = anchoredEntries(entries);
		List<SemanticRule> rules = new ArrayList<>();
		SemanticRule outfitRule = buildOutfitRule(anchored);
		if (outfitRule != null)
		{
			rules.add(outfitRule);
		}
		SemanticRule skillRuns = buildPresentRows(anchored, SKILL_RUN_RULE_KEY, TOOL_FAMILIES.get());
		if (skillRuns != null)
		{
			rules.add(skillRuns);
		}
		return new LayoutRequest(anchored, rules);
	}

	public static Set<Integer> outfitItemIds(List<LayoutEntry> entries)
	{
		Set<Integer> present = LayoutEntry.presentIds(entries), protectedIds = new HashSet<>();
		for (OutfitFact outfit : outfitsFor(entries))
		{
			List<Integer> owned = presentIds(outfit.itemIds, present);
			if (owned.size() >= 2) protectedIds.addAll(owned);
		}
		return protectedIds;
	}

	/** Dense fallback keeps each owned column together when the physical grid cannot fit it. */
	public static List<Integer> outfitOrder(List<LayoutEntry> entries)
	{
		List<Integer> order = new ArrayList<>();
		for (OutfitFact outfit : outfitsFor(entries)) order.addAll(outfit.itemIds);
		return order;
	}

	/** Keep real outfit cells and attach owned skill tools around their best present outfit. */
	public static List<BankPreviewItem> groupSkills(List<BankPreviewItem> items,
		java.util.function.IntPredicate reserved)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		Map<Integer, Integer> positions = new HashMap<>();
		Map<Integer, BankPreviewItem> present = new HashMap<>();
		for (int i = 0; i < items.size(); i++)
		{
			BankPreviewItem item = items.get(i);
			entries.add(LayoutEntry.of(item, i)); positions.put(item.getItemId(), i); present.put(item.getItemId(), item);
		}
		BankPreviewItem[] cells = new BankPreviewItem[items.size()];
		Map<String, List<Integer>> outfits = new LinkedHashMap<>();
		Set<Integer> used = new HashSet<>();
		for (OutfitFact outfit : outfitsFor(entries))
		{
			List<Integer> owned = presentIds(outfit.itemIds, present.keySet());
			for (int id : owned) { cells[positions.get(id)] = present.get(id); used.add(id); }
			int separator = outfit.key.indexOf('/');
			if (separator > 0 && !owned.isEmpty())
			{
				String skill = outfit.key.substring(0, separator);
				if (owned.size() > outfits.getOrDefault(skill, Collections.emptyList()).size()) outfits.put(skill, owned);
			}
		}
		for (BankPreviewItem item : items) if (reserved.test(item.getItemId()))
		{ cells[positions.get(item.getItemId())] = item; used.add(item.getItemId()); }
		Map<String, List<Integer>> tools = new LinkedHashMap<>();
		for (OutfitFact family : TOOL_FAMILIES.get())
		{
			List<Integer> owned = new ArrayList<>();
			for (int id : family.itemIds) if (present.containsKey(id) && used.add(id)) owned.add(id);
			tools.put(family.key.substring("tool.".length()), owned);
		}
		// Reserve each skill's own column before overflow can occupy another skill's continuation.
		Map<String, Integer> ends = new HashMap<>();
		for (Map.Entry<String, List<Integer>> group : tools.entrySet())
		{
			List<Integer> outfit = outfits.get(group.getKey());
			if (outfit == null) continue;
			int last = positions.get(outfit.get(outfit.size() - 1));
			ends.put(group.getKey(), last);
			for (int at = last + 8, placed = 0; at < cells.length && placed < 4 && !group.getValue().isEmpty(); at += 8)
				if (cells[at] == null)
				{
					cells[at] = present.get(group.getValue().remove(0));
					ends.put(group.getKey(), at); placed++;
				}
		}
		List<Map.Entry<String, List<Integer>>> groups = new ArrayList<>(tools.entrySet());
		groups.sort(Comparator.comparing(group -> !outfits.containsKey(group.getKey())));
		for (Map.Entry<String, List<Integer>> group : groups)
		{
			List<Integer> owned = group.getValue();
			if (owned.isEmpty()) continue;
			List<Integer> outfit = outfits.get(group.getKey());
			int first = outfit == null ? positions.get(owned.get(0)) : positions.get(outfit.get(0));
			int anchor = outfit == null ? first : (first / 8 + ends.get(group.getKey()) / 8) / 2 * 8 + first % 8;
			int run = -1;
			if (outfit == null) for (int at = 0; at + owned.size() <= cells.length; at++)
			{
				if (owned.size() <= 8 && at % 8 + owned.size() > 8) continue;
				boolean free = true;
				for (int i = 0; i < owned.size(); i++) if (cells[at + i] != null) { free = false; break; }
				if (free && (run < 0 || Math.abs(at - anchor) < Math.abs(run - anchor))) run = at;
			}
			for (int id : owned)
			{
				int target = run < 0 ? -1 : run++;
				if (target < 0) for (int at = 0; at < cells.length; at++)
					if (cells[at] == null && (target < 0 || distance(at, anchor) < distance(target, anchor))) target = at;
				cells[target] = present.get(id);
			}
		}
		int next = 0;
		for (BankPreviewItem item : items) if (!used.contains(item.getItemId()))
		{
			while (cells[next] != null) next++;
			cells[next] = item;
		}
		return Arrays.asList(cells);
	}

	private static int distance(int at, int anchor)
	{
		return Math.abs(at / 8 - anchor / 8) * 8 + Math.abs(at % 8 - anchor % 8);
	}

	private static List<LayoutEntry> anchoredEntries(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			if (entry.hasLockedTarget())
			{
				return entries;
			}
			present.add(entry.getItem().getItemId());
		}

		Map<Integer, Integer> lockedTargets = new LinkedHashMap<>();
		int maxOutfitHeight = 0;
		for (OutfitFact outfit : outfitsFor(entries))
		{
			List<Integer> owned = presentIds(outfit.itemIds, present);
			if (owned.size() < 2)
			{
				continue;
			}
			for (int stride : new int[]{8, 1})
			{
				if (lockedTargets.containsKey(owned.get(0))) break;
				for (int origin = 0; origin + (owned.size() - 1) * stride < entries.size(); origin++)
				{
					boolean free = true;
					for (int row = 0; row < owned.size(); row++)
						if (lockedTargets.containsValue(origin + row * stride)) free = false;
					if (!free) continue;
					for (int row = 0; row < owned.size(); row++) lockedTargets.put(owned.get(row), origin + row * stride);
					maxOutfitHeight = Math.max(maxOutfitHeight, (origin + (owned.size() - 1) * stride) / 8 + 1);
					break;
				}
			}
		}

		int priorityRow = maxOutfitHeight;
		int runecraftingRows = lockPriorityRows(TABLE.ids("RUNECRAFTING_PRIORITY"), present,
			priorityRow, entries.size(), lockedTargets);
		priorityRow += runecraftingRows;
		int containerRows = present.contains(19634)
			? lockPriorityRows(TABLE.ids("IRONMAN_CONTAINER_PRIORITY"), present, priorityRow,
				entries.size(), lockedTargets)
			: 0;

		// A sparse tab may end before the row below its tallest outfit exists.
		if (runecraftingRows == 0 && containerRows == 0 && maxOutfitHeight * 8 < entries.size())
		{
			for (OutfitFact family : TOOL_FAMILIES.get())
			{
				List<Integer> owned = presentIds(family.itemIds, present);
				if (owned.size() >= 2)
				{
					lockedTargets.put(owned.get(0), maxOutfitHeight * 8);
					break;
				}
			}
		}
		if (lockedTargets.isEmpty())
		{
			return entries;
		}

		return LayoutEntry.withTargets(entries, lockedTargets);
	}

	private static int lockPriorityRows(List<Integer> itemIds, Set<Integer> present,
		int startRow, int entryCount, Map<Integer, Integer> lockedTargets)
	{
		List<Integer> owned = presentIds(itemIds, present);
		if (owned.size() < 2)
		{
			return 0;
		}
		int rows = (owned.size() + 7) / 8;
		int lastTarget = (startRow + rows - 1) * 8 + (owned.size() - 1) % 8;
		if (lastTarget >= entryCount)
		{
			return 0;
		}
		for (int index = 0; index < owned.size(); index++)
		{
			lockedTargets.put(owned.get(index),
				(startRow + index / 8) * 8 + index % 8);
		}
		return rows;
	}

	private static List<Integer> presentIds(List<Integer> itemIds, Set<Integer> present)
	{
		return LayoutEntry.ownedIds(itemIds, present);
	}

	private static SemanticRule buildOutfitRule(List<LayoutEntry> entries)
	{
		List<ItemSetCatalog.SetDefinition> definitions = new ArrayList<>(OUTFITS.get().size());
		for (OutfitFact outfit : outfitsFor(entries))
		{
			definitions.add(ItemSetCatalog.definition("tools", outfit.key.replace('/', '.'), outfit.key, outfit.itemIds));
		}
		return VerticalItemSetRuleFactory.build(OUTFIT_RULE_KEY, entries, definitions);
	}

	private static List<OutfitFact> outfitsFor(List<LayoutEntry> entries)
	{
		Set<Integer> present = LayoutEntry.presentIds(entries);
		Map<String, List<OutfitFact>> families = new LinkedHashMap<>();
		for (OutfitFact outfit : OUTFITS.get())
			families.computeIfAbsent(outfitFamily(outfit.key), key -> new ArrayList<>()).add(outfit);
		Set<Integer> unused = new LinkedHashSet<>(present);
		List<OutfitFact> outfits = new ArrayList<>();
		for (Map.Entry<String, List<OutfitFact>> family : families.entrySet())
		{
			for (OutfitFact variant : family.getValue())
			{
				if (unused.containsAll(variant.itemIds))
				{
					outfits.add(variant);
					unused.removeAll(variant.itemIds);
				}
			}
			int slots = family.getValue().stream().mapToInt(variant -> variant.itemIds.size()).max().orElse(0);
			for (int column = 0; ; column++)
			{
				List<Integer> pieces = new ArrayList<>();
				for (int slot = 0; slot < slots; slot++)
				{
					for (OutfitFact variant : family.getValue())
						if (slot < variant.itemIds.size() && unused.remove(variant.itemIds.get(slot)))
						{ pieces.add(variant.itemIds.get(slot)); break; }
				}
				if (pieces.isEmpty()) break;
				outfits.add(outfit(family.getKey() + "-mixed-" + column, pieces));
			}
		}
		if (outfits.stream().noneMatch(outfit -> outfit.key.contains("outfit.raiments-eye"))
			&& !Collections.disjoint(present, TABLE.ids("EYE_SHARED")))
			outfits.add(outfit("runecrafting/outfit.raiments-eye", Collections.emptyList()));
		int best = -1;
		int count = -1;
		for (int i = 0; i < outfits.size(); i++)
		{
			OutfitFact outfit = outfits.get(i);
			int owned = presentIds(outfit.itemIds, present).size();
			if (outfit.key.contains("outfit.raiments-eye") && owned > count)
			{
				best = i;
				count = owned;
			}
		}
		if (best >= 0)
		{
			OutfitFact outfit = outfits.get(best);
			List<Integer> ids = presentIds(outfit.itemIds, present);
			ids.addAll(presentIds(TABLE.ids("EYE_SHARED"), present));
			outfits.set(best, outfit(outfit.key, ids));
		}
		return outfits;
	}

	private static String outfitFamily(String key)
	{
		int separator = key.indexOf('/');
		String prefix = separator < 0 ? "" : key.substring(0, separator + 1);
		key = separator < 0 ? key : key.substring(separator + 1);
		if (key.startsWith("tools.graceful-")) return "agility/outfit.graceful";
		if (key.equals("outfit.golden-prospector")) key = "outfit.prospector";
		if (key.equals("outfit.spirit-angler")) key = "outfit.angler";
		for (String family : Arrays.asList("outfit.graceful", "outfit.prospector", "outfit.lumberjack",
			"outfit.raiments-eye", "outfit.farmer", "outfit.smiths"))
			if (key.startsWith(family + "-")) return prefix + family;
		return prefix + key;
	}

	private static SemanticRule buildPresentRows(List<LayoutEntry> entries, String ruleKey,
		List<OutfitFact> families)
	{
		Set<Integer> present = LayoutEntry.presentIds(entries);

		List<SemanticAtom> atoms = new ArrayList<>();
		for (OutfitFact family : families)
		{
			if (family.key.equals("tool.utility-containers") && !present.contains(19634))
			{
				continue;
			}
			if (presentIds(family.itemIds, present).size() < 2) continue;
			SemanticAtom.appendPresentChunks(atoms, family.key, family.itemIds, present, "tool-");
		}
		if (atoms.isEmpty())
		{
			return null;
		}
		return SemanticRule.highConfidence(ruleKey, atoms, ShapePrimitive.ROW_GROUP_MATRIX, SemanticRule.ALL_WIDTHS).build();
	}

	private static OutfitFact outfit(String key, List<Integer> itemIds)
	{
		return new OutfitFact(key, itemIds);
	}


	private static final class OutfitFact
	{
		private final String key;
		private final List<Integer> itemIds;

		private OutfitFact(String key, List<Integer> itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}

}

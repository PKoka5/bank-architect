package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact canonical outfit columns plus compact horizontal tool runs grouped by primary skill. */
public final class ToolOutfitSemanticRuleSet
{
	private static final String OUTFIT_RULE_KEY = "tool.outfit-columns";
	private static final String SKILL_RUN_RULE_KEY = "tool.primary-skill-runs";
	private static final Set<Integer> ALL_WIDTHS = Collections.unmodifiableSet(
		new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		ToolOutfitSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/tool-layout-families.tsv"), 0, false);
	private static final RequiredResource<List<OutfitFact>> OUTFITS =
		new RequiredResource<>("tool outfits", ToolOutfitSemanticRuleSet::buildOutfits);
	private static final RequiredResource<List<ToolFamilyFact>> TOOL_FAMILIES = new RequiredResource<>("tool families", () ->
	{
		List<ToolFamilyFact> families = new ArrayList<>();
		TABLE.group("families").forEach((key, ids) -> families.add(tools(key, ids)));
		return Collections.unmodifiableList(families);
	});

	private static List<OutfitFact> buildOutfits()
	{
		List<OutfitFact> outfits = new ArrayList<>();
		TABLE.group("outfits").forEach((key, ids) -> outfits.add(outfit(key, ids.stream().mapToInt(Integer::intValue).toArray())));

		Set<Integer> reserved = new LinkedHashSet<>();
		for (OutfitFact outfit : outfits)
		{
			for (int itemId : outfit.itemIds) reserved.add(itemId);
		}
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("tools"))
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
				outfits.add(outfit(definition.getKey(), itemIds));
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
		int outfitColumn = 0;
		int maxOutfitHeight = 0;
		for (OutfitFact outfit : OUTFITS.get())
		{
			List<Integer> owned = presentIds(outfit.itemIds, present);
			if (owned.size() < 2 || outfitColumn >= 8)
			{
				continue;
			}
			lockedTargets.put(owned.get(0), outfitColumn++);
			maxOutfitHeight = Math.max(maxOutfitHeight, owned.size());
		}

		int priorityRow = maxOutfitHeight;
		int runecraftingRows = lockPriorityRows(TABLE.ids("RUNECRAFTING_PRIORITY"), present,
			priorityRow, entries.size(), lockedTargets);
		priorityRow += runecraftingRows;
		int containerRows = present.contains(19634)
			? lockPriorityRows(TABLE.ids("IRONMAN_CONTAINER_PRIORITY"), present, priorityRow,
				entries.size(), lockedTargets)
			: 0;

		if (runecraftingRows == 0 && containerRows == 0)
		{
			for (ToolFamilyFact family : TOOL_FAMILIES.get())
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

		List<LayoutEntry> anchored = new ArrayList<>(entries.size());
		for (LayoutEntry entry : entries)
		{
			Integer target = lockedTargets.get(entry.getItem().getItemId());
			anchored.add(target == null ? entry : entry.withLockedTarget(target));
		}
		return Collections.unmodifiableList(anchored);
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

	private static List<Integer> presentIds(int[] itemIds, Set<Integer> present)
	{
		List<Integer> owned = new ArrayList<>();
		for (int itemId : itemIds)
		{
			if (present.contains(itemId)) owned.add(itemId);
		}
		return owned;
	}

	private static List<Integer> presentIds(List<Integer> itemIds, Set<Integer> present)
	{
		List<Integer> owned = new ArrayList<>();
		for (Integer itemId : itemIds)
		{
			if (present.contains(itemId)) owned.add(itemId);
		}
		return owned;
	}

	private static SemanticRule buildOutfitRule(List<LayoutEntry> entries)
	{
		List<ItemSetCatalog.SetDefinition> definitions = new ArrayList<>(OUTFITS.get().size());
		for (OutfitFact outfit : OUTFITS.get())
		{
			List<Integer> itemIds = new ArrayList<>(outfit.itemIds.length);
			for (int itemId : outfit.itemIds) itemIds.add(itemId);
			definitions.add(ItemSetCatalog.definition("tools", outfit.key, outfit.key, itemIds));
		}
		return VerticalItemSetRuleFactory.build(OUTFIT_RULE_KEY, entries, definitions);
	}

	private static SemanticRule buildPresentRows(List<LayoutEntry> entries, String ruleKey,
		List<ToolFamilyFact> families)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}

		List<SemanticAtom> atoms = new ArrayList<>();
		for (ToolFamilyFact family : families)
		{
			if (family.key.equals("tool.utility-containers") && !present.contains(19634))
			{
				continue;
			}
			List<SemanticAtom.Member> members = new ArrayList<>();
			int chunk = 0;
			for (Integer itemId : family.itemIds)
			{
				if (!present.contains(itemId))
				{
					continue;
				}
				if (members.size() == 8)
				{
					atoms.add(new SemanticAtom(family.key + "-" + chunk++, members));
					members = new ArrayList<>();
				}
				members.add(new SemanticAtom.Member("tool-" + members.size(), itemId));
			}
			if (!members.isEmpty())
			{
				atoms.add(new SemanticAtom(family.key + "-" + chunk, members));
			}
		}
		if (atoms.isEmpty())
		{
			return null;
		}
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static OutfitFact outfit(String key, int... itemIds)
	{
		return new OutfitFact(key, itemIds);
	}


	private static ToolFamilyFact tools(String key, List<Integer> itemIds)
	{
		return new ToolFamilyFact(key, itemIds);
	}

	private static final class OutfitFact
	{
		private final String key;
		private final int[] itemIds;

		private OutfitFact(String key, int[] itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}

	private static final class ToolFamilyFact
	{
		private final String key;
		private final List<Integer> itemIds;

		private ToolFamilyFact(String key, List<Integer> itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}
}

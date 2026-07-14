package com.pkoka5.ironmanbankarchitect.organize.layout;

import net.runelite.api.gameval.ItemID;
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
	private static final List<Integer> RUNECRAFTING_PRIORITY = Collections.unmodifiableList(Arrays.asList(
		1438, 1444, 1442, 5529, 1456, 1462, 1458,
		1448, 1440, 1446, 1454, 1452, 1450, 1460, 22118,
		5527, 5531, 5533, 5535, 5537, 5539, 5541, 5543, 5545, 5547, 26801,
		ItemID.RCU_POUCH_SMALL, ItemID.RCU_POUCH_MEDIUM, ItemID.RCU_POUCH_MEDIUM_DEGRADE,
		ItemID.RCU_POUCH_LARGE, ItemID.RCU_POUCH_LARGE_DEGRADE,
		ItemID.RCU_POUCH_GIANT, ItemID.RCU_POUCH_GIANT_DEGRADE,
		ItemID.RCU_POUCH_COLOSSAL, ItemID.RCU_POUCH_COLOSSAL_DEGRADE,
		ItemID.MAGIC_EMERALD_NECKLACE));
	private static final List<Integer> IRONMAN_CONTAINER_PRIORITY = Collections.unmodifiableList(Arrays.asList(
		11941, 22586, 13226, 24478, 13639, 24482, 19634,
		12019, 12020, 24481, 25582, 25584, 28140, 28142, 24882));
	private static final List<OutfitFact> OUTFITS = Collections.unmodifiableList(Arrays.asList(
		outfit("outfit.angler", 13258, 13259, 13260, 13261),
		outfit("outfit.carpenter", 24872, 24874, 24876, 24878),
		outfit("outfit.farmer-male", 13646, 13642, 13640, 13644),
		outfit("outfit.farmer-female", 13647, 13643, 13641, 13645),
		outfit("outfit.graceful", 11850, 11854, 11856, 11858, 11860, 11852),
		outfit("outfit.lumberjack", 10941, 10939, 10940, 10933),
		outfit("outfit.prospector", 12013, 12014, 12015, 29478),
		outfit("outfit.pyromancer", 20708, 20704, 20706, 20710),
		outfit("outfit.raiments-eye", 26850, 26852, 26854, 26856),
		outfit("outfit.raiments-eye-red", 26858, 26860, 26862),
		outfit("outfit.raiments-eye-green", 26864, 26866, 26868),
		outfit("outfit.raiments-eye-blue", 26870, 26872, 26874),
		outfit("outfit.rogue", 5554, 5553, 5555, 5556, 5557),
		outfit("outfit.smiths", 27023, 27025, 27027, 27029),
		outfit("outfit.spirit-angler", 25592, 25594, 25596, 25598),
		outfit("outfit.zealot", 25438, 25434, 25436, 25440)));
	private static final List<ToolFamilyFact> TOOL_FAMILIES = Collections.unmodifiableList(Arrays.asList(
		tools("tool.mining", 25539, 11920, 5013, 776, 12019, 24481),
		tools("tool.woodcutting", 10491, 6739, 6313, 10132, 28136, 28142),
		tools("tool.fishing", 10129, 11323, 305, 307, 309, 3159, 301, 1585, 303, 25584),
		tools("tool.farming", 5340, 5325, 7409, 5341, 5343, 22997, 24482),
		tools("tool.construction", 9625, 2347, 8794, 24882),
		tools("tool.crafting", 1785, 1733, 946),
		tools("tool.hunter", 10006, 10008, 10010, 10150, 10031, 10029, 29303, 29297),
		tools("tool.thieving", 29325, 1523, 24740, 4600),
		tools("tool.light-firemaking", 9065, 590, 596, 20712),
		tools("tool.cooking", 1887, 775),
		tools("tool.herblore", 233, 24478),
		tools("tool.runecrafting", RUNECRAFTING_PRIORITY),
		tools("tool.utility-containers", 11941, 22586, 13226, 13639, 12020,
			25582, 28140, 19634),
		tools("tool.sailing", 31733, 31989, 31986, 31745, 31757)));
	private static final SemanticRule OUTFIT_RULE = buildOutfitRule();

	private ToolOutfitSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		List<LayoutEntry> anchored = anchoredEntries(entries);
		List<SemanticRule> rules = new ArrayList<>();
		rules.add(OUTFIT_RULE);
		SemanticRule skillRuns = buildPresentRows(anchored, SKILL_RUN_RULE_KEY, TOOL_FAMILIES);
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
		for (OutfitFact outfit : OUTFITS)
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
		int runecraftingRows = lockPriorityRows(RUNECRAFTING_PRIORITY, present,
			priorityRow, entries.size(), lockedTargets);
		priorityRow += runecraftingRows;
		int containerRows = present.contains(19634)
			? lockPriorityRows(IRONMAN_CONTAINER_PRIORITY, present, priorityRow,
				entries.size(), lockedTargets)
			: 0;

		if (runecraftingRows == 0 && containerRows == 0)
		{
			for (ToolFamilyFact family : TOOL_FAMILIES)
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

	private static SemanticRule buildOutfitRule()
	{
		List<SemanticAtom> atoms = new ArrayList<>(OUTFITS.size());
		for (OutfitFact outfit : OUTFITS)
		{
			List<SemanticAtom.Member> members = new ArrayList<>(outfit.itemIds.length);
			for (int index = 0; index < outfit.itemIds.length; index++)
			{
				members.add(new SemanticAtom.Member("slot-" + index, outfit.itemIds[index]));
			}
			atoms.add(new SemanticAtom(outfit.key, members));
		}
		return SemanticRule.builder()
			.ruleKey(OUTFIT_RULE_KEY)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(Collections.singleton(1))
			.build();
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

	private static ToolFamilyFact tools(String key, Integer... itemIds)
	{
		return new ToolFamilyFact(key, Collections.unmodifiableList(Arrays.asList(itemIds)));
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

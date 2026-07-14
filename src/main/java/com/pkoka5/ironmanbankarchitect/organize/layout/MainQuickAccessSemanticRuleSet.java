package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.IronmanQuickToolSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Reviewed quick-access geometry for the active Ironman main tab. */
public final class MainQuickAccessSemanticRuleSet
{
	private static final List<Integer> GRACEFUL = Collections.unmodifiableList(
		Arrays.asList(11850, 11854, 11856, 11858, 11860, 11852));
	private static final int GRACEFUL_COLUMN = 7;
	private static final int RUNE_BLOCK_SECOND_ROW = 8;

	private MainQuickAccessSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		List<LayoutEntry> anchored = anchorReviewedTargets(entries);
		List<SemanticRule> rules = new ArrayList<>(
			AchievementDiarySemanticRuleSet.forEntries(anchored).getRules());
		SemanticRule graceful = gracefulRule(anchored);
		if (graceful != null)
		{
			rules.add(graceful);
		}
		SemanticRule quickTools = quickToolRule(anchored);
		if (quickTools != null)
		{
			rules.add(quickTools);
		}
		rules.addAll(RuneSemanticRuleSet.forMainEntries(anchored).getRules());
		return new LayoutRequest(anchored, rules);
	}

	private static List<LayoutEntry> anchorReviewedTargets(List<LayoutEntry> entries)
	{
		for (LayoutEntry entry : entries)
		{
			if (entry.hasLockedTarget())
			{
				return entries;
			}
		}
		int ownedGraceful = 0;
		for (Integer itemId : GRACEFUL)
		{
			if (contains(entries, itemId)) ownedGraceful++;
		}
		boolean anchorGraceful = ownedGraceful >= 2
			&& GRACEFUL_COLUMN + (ownedGraceful - 1) * SemanticRule.MAX_WIDTH < entries.size();
		int runeTarget = contains(entries, 995) ? RUNE_BLOCK_SECOND_ROW : 0;
		Map<Integer, Integer> runeTargets = runeTargets(entries, runeTarget);
		Map<Integer, Integer> gracefulTargets = gracefulTargets(entries, anchorGraceful);
		Map<Integer, Integer> quickToolTargets = quickToolTargets(entries, contains(entries, 995) ? 1 : 0);

		List<LayoutEntry> anchored = new ArrayList<>(entries.size());
		for (LayoutEntry entry : entries)
		{
			if (entry.getItem().getItemId() == 995)
			{
				anchored.add(entry.withLockedTarget(0));
			}
			else if (gracefulTargets.containsKey(entry.getItem().getItemId()))
			{
				anchored.add(entry.withLockedTarget(gracefulTargets.get(entry.getItem().getItemId())));
			}
			else if (runeTargets.containsKey(entry.getItem().getItemId()))
			{
				anchored.add(entry.withLockedTarget(runeTargets.get(entry.getItem().getItemId())));
			}
			else if (quickToolTargets.containsKey(entry.getItem().getItemId()))
			{
				anchored.add(entry.withLockedTarget(quickToolTargets.get(entry.getItem().getItemId())));
			}
			else
			{
				anchored.add(entry);
			}
		}
		return Collections.unmodifiableList(anchored);
	}

	private static Map<Integer, Integer> gracefulTargets(List<LayoutEntry> entries,
		boolean anchorGraceful)
	{
		Map<Integer, Integer> targets = new LinkedHashMap<>();
		if (!anchorGraceful) return targets;
		int row = 0;
		for (Integer itemId : GRACEFUL)
		{
			if (contains(entries, itemId)) targets.put(itemId, GRACEFUL_COLUMN + row++ * 8);
		}
		return targets;
	}

	private static Map<Integer, Integer> runeTargets(List<LayoutEntry> entries, int startTarget)
	{
		Map<Integer, Integer> targets = new LinkedHashMap<>();
		List<List<Integer>> rows = RuneSemanticRuleSet.mainRows(entries);
		for (int row = 0; row < rows.size(); row++)
		{
			int column = 0;
			for (Integer itemId : rows.get(row))
			{
				if (contains(entries, itemId)) targets.put(itemId, startTarget + row * 8 + column++);
			}
		}
		for (Integer target : targets.values())
		{
			if (target >= entries.size()) return Collections.emptyMap();
		}
		return targets;
	}

	private static Map<Integer, Integer> quickToolTargets(List<LayoutEntry> entries, int startTarget)
	{
		Map<Integer, Integer> targets = new LinkedHashMap<>();
		List<Integer> tools = presentQuickTools(entries);
		if (startTarget % 8 + tools.size() > 8 || startTarget + tools.size() > entries.size())
		{
			return targets;
		}
		for (int index = 0; index < tools.size(); index++)
		{
			targets.put(tools.get(index), startTarget + index);
		}
		return targets;
	}

	/** Canonical compact Main run: best axe, best pickaxe, hammer, chisel, spade. */
	private static SemanticRule quickToolRule(List<LayoutEntry> entries)
	{
		List<Integer> tools = presentQuickTools(entries);
		if (tools.size() < 2) return null;
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < tools.size(); index++)
		{
			members.add(new SemanticAtom.Member("quick-tool-" + index, tools.get(index)));
		}
		return SemanticRule.builder()
			.ruleKey("main.quick-tools")
			.atoms(Collections.singletonList(new SemanticAtom("main.quick-tools", members)))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(Collections.singleton(tools.size()))
			.build();
	}

	private static List<Integer> presentQuickTools(List<LayoutEntry> entries)
	{
		List<Integer> tools = new ArrayList<>();
		for (int rank = 0; rank < 5; rank++)
		{
			for (LayoutEntry entry : entries)
			{
				if (IronmanQuickToolSelector.quickAccessRank(entry.getItem().getItemId()) == rank)
				{
					tools.add(entry.getItem().getItemId());
					break;
				}
			}
		}
		return Collections.unmodifiableList(tools);
	}

	private static SemanticRule gracefulRule(List<LayoutEntry> entries)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (Integer itemId : GRACEFUL)
		{
			if (contains(entries, itemId))
			{
				members.add(new SemanticAtom.Member("slot-" + members.size(), itemId));
			}
		}
		if (members.size() < 2)
		{
			return null;
		}
		return SemanticRule.builder()
			.ruleKey("main.graceful-column")
			.atoms(Collections.singletonList(new SemanticAtom("outfit.graceful", members)))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(Collections.singleton(1))
			.build();
	}

	private static boolean contains(List<LayoutEntry> entries, int itemId)
	{
		for (LayoutEntry entry : entries)
		{
			if (entry.getItem().getItemId() == itemId) return true;
		}
		return false;
	}

}

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
	private static final String GRACEFUL_FAMILY_PREFIX = "tools.graceful-";
	private static final String GRACEFUL_BASE_FAMILY = "tools.graceful-base";
	/** Wear order, which is not catalogue order: the cape reads last, as it did. */
	private static final List<String> GRACEFUL_PIECE_ORDER = Collections.unmodifiableList(
		Arrays.asList("graceful hood", "graceful top", "graceful legs",
			"graceful gloves", "graceful boots", "graceful cape"));
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
		int ownedGraceful = gracefulColumn(entries).size();
		boolean anchorGraceful = ownedGraceful >= 2
			&& GRACEFUL_COLUMN + (ownedGraceful - 1) * SemanticRule.MAX_WIDTH < entries.size();
		boolean hasCoins = contains(entries, 995);
		boolean hasQuickTools = !presentQuickTools(entries).isEmpty();
		int runeTarget = hasCoins || hasQuickTools ? RUNE_BLOCK_SECOND_ROW : 0;
		Map<Integer, Integer> runeTargets = runeTargets(entries, runeTarget);
		Map<Integer, Integer> gracefulTargets = gracefulTargets(entries, anchorGraceful);
		Map<Integer, Integer> quickToolTargets = quickToolTargets(entries, hasCoins ? 1 : 0);

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
		for (Integer itemId : gracefulColumn(entries))
		{
			targets.put(itemId, GRACEFUL_COLUMN + row++ * 8);
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

	/**
	 * The graceful pieces this tab stands in a column, in wear order.
	 *
	 * <p>Read from the tab rather than from a list of IDs, so the column
	 * follows whichever set actually reached the tab. A player who files their
	 * old set with the tools and their new one here gets the column on the new
	 * one, without the plugin having an opinion about which recolour is the
	 * real one. Owning two and saying nothing gives the column to the more
	 * complete set, and the base set breaks a tie.</p>
	 */
	private static List<Integer> gracefulColumn(List<LayoutEntry> entries)
	{
		Map<String, List<LayoutEntry>> byFamily = new LinkedHashMap<>();
		for (LayoutEntry entry : entries)
		{
			String key = ItemSetCatalog.setKeyOf(entry.getItem().getItemId()).orElse("");
			if (key.startsWith(GRACEFUL_FAMILY_PREFIX))
			{
				byFamily.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
			}
		}

		List<LayoutEntry> chosen = Collections.emptyList();
		for (Map.Entry<String, List<LayoutEntry>> family : byFamily.entrySet())
		{
			boolean better = family.getValue().size() > chosen.size()
				|| (family.getValue().size() == chosen.size()
					&& GRACEFUL_BASE_FAMILY.equals(family.getKey()));
			if (better)
			{
				chosen = family.getValue();
			}
		}

		List<Integer> column = new ArrayList<>(chosen.size());
		for (String piece : GRACEFUL_PIECE_ORDER)
		{
			for (LayoutEntry entry : chosen)
			{
				if (piece.equalsIgnoreCase(entry.getItem().getDisplayName().trim()))
				{
					column.add(entry.getItem().getItemId());
				}
			}
		}
		// A piece whose name does not read like the others still belongs to the
		// set, so it follows rather than being dropped from its own column.
		for (LayoutEntry entry : chosen)
		{
			if (!column.contains(entry.getItem().getItemId()))
			{
				column.add(entry.getItem().getItemId());
			}
		}
		return column;
	}

	private static SemanticRule gracefulRule(List<LayoutEntry> entries)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (Integer itemId : gracefulColumn(entries))
		{
			members.add(new SemanticAtom.Member("slot-" + members.size(), itemId));
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

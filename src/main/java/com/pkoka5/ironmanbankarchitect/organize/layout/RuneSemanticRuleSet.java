package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Exact canonical four-wide rune rows; non-rune entries remain ordinary dense spillover. */
public final class RuneSemanticRuleSet
{
	private static final String RULE_KEY = "runes.four-wide-rows";
	private static final List<List<Integer>> ROWS = Collections.unmodifiableList(Arrays.asList(
		row(556, 555, 557, 554),
		row(558, 562, 560, 565),
		row(559, 564, 561, 563),
		row(9075, 566, 4699, 21880),
		row(4695, 4696, 4698, 4697),
		row(4694)));

	private RuneSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		return new LayoutRequest(anchorFirstRune(entries),
			Collections.singletonList(ruleForRows(ROWS)));
	}

	static LayoutRequest forMainEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		return new LayoutRequest(entries,
			Collections.singletonList(ruleForRows(mainRows(entries))));
	}

	static Integer firstPresentRuneId(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}
		for (List<Integer> row : ROWS)
		{
			for (Integer itemId : row)
			{
				if (present.contains(itemId)) return itemId;
			}
		}
		return null;
	}

	static List<List<Integer>> mainRows(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}
		List<List<Integer>> rows = new ArrayList<>();
		rows.add(ROWS.get(0));
		rows.add(ROWS.get(1));
		rows.add(ROWS.get(2));
		List<Integer> tail = new ArrayList<>();
		for (int row = 3; row < ROWS.size(); row++)
		{
			for (Integer itemId : ROWS.get(row))
			{
				if (present.contains(itemId)) tail.add(itemId);
			}
		}
		if (present.contains(8013)) tail.add(8013);
		for (int offset = 0; offset < tail.size(); offset += 4)
		{
			rows.add(Collections.unmodifiableList(new ArrayList<>(
				tail.subList(offset, Math.min(offset + 4, tail.size())))));
		}
		return Collections.unmodifiableList(rows);
	}

	private static List<LayoutEntry> anchorFirstRune(List<LayoutEntry> entries)
	{
		for (LayoutEntry entry : entries)
		{
			if (entry.hasLockedTarget())
			{
				return entries;
			}
		}
		Integer firstRune = firstPresentRuneId(entries);
		if (firstRune != null)
		{
			List<LayoutEntry> anchored = new ArrayList<>(entries.size());
			for (LayoutEntry entry : entries)
			{
				anchored.add(entry.getItem().getItemId() == firstRune
					? entry.withLockedTarget(0) : entry);
			}
			return Collections.unmodifiableList(anchored);
		}
		return entries;
	}

	private static SemanticRule ruleForRows(List<List<Integer>> rows)
	{
		return SemanticRule.builder()
			.ruleKey(RULE_KEY)
			.atoms(atoms(rows))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(Collections.singleton(4))
			.build();
	}

	private static List<SemanticAtom> atoms(List<List<Integer>> rows)
	{
		List<SemanticAtom> atoms = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++)
		{
			List<SemanticAtom.Member> members = new ArrayList<>();
			for (int column = 0; column < rows.get(rowIndex).size(); column++)
			{
				members.add(new SemanticAtom.Member("rune-" + column, rows.get(rowIndex).get(column)));
			}
			atoms.add(new SemanticAtom("rune-row-" + rowIndex, members));
		}
		return atoms;
	}

	private static List<Integer> row(Integer... itemIds)
	{
		return Collections.unmodifiableList(Arrays.asList(itemIds));
	}
}

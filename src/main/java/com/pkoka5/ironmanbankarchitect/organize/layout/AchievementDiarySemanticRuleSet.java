package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Compact region-ordered achievement-diary rewards for the main utility tab.
 * The four-column shape is an aggregate cohort decision; actual member IDs are
 * taken from the player's bank so every reward tier remains supported.
 */
public final class AchievementDiarySemanticRuleSet
{
	private static final String[][] REGION_ROWS = {
		{"ardougne cloak", "desert amulet", "explorer's ring", "falador shield"},
		{"fremennik sea boots", "kandarin headgear", "karamja gloves", "morytania legs"},
		{"rada's blessing", "varrock armour", "western banner", "wilderness sword"}
	};

	private AchievementDiarySemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		List<SemanticAtom> rows = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < REGION_ROWS.length; rowIndex++)
		{
			List<SemanticAtom.Member> members = new ArrayList<>();
			for (int regionIndex = 0; regionIndex < REGION_ROWS[rowIndex].length; regionIndex++)
			{
				LayoutEntry reward = bestReward(entries, REGION_ROWS[rowIndex][regionIndex]);
				if (reward != null)
				{
					members.add(new SemanticAtom.Member("region-" + regionIndex,
						reward.getItem().getItemId()));
				}
			}
			if (members.size() >= 2)
			{
				rows.add(new SemanticAtom("diary-row-" + rowIndex, members));
			}
		}

		if (rows.isEmpty())
		{
			return new LayoutRequest(entries, Collections.emptyList());
		}

		SemanticRule rule = SemanticRule.builder()
			.ruleKey("main.achievement-diary-grid")
			.atoms(rows)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(Collections.singleton(4))
			.build();
		return new LayoutRequest(entries, Collections.singletonList(rule));
	}

	public static boolean isDiaryReward(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		for (String[] row : REGION_ROWS)
		{
			for (String prefix : row)
			{
				if (matches(name, prefix))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static LayoutEntry bestReward(List<LayoutEntry> entries, String prefix)
	{
		return entries.stream()
			.filter(entry -> matches(normalized(entry.getItem().getDisplayName()), prefix))
			.max(Comparator.comparingInt((LayoutEntry entry) -> tier(entry.getItem().getDisplayName()))
				.thenComparingInt(entry -> entry.getItem().getItemId()))
			.orElse(null);
	}

	private static boolean matches(String name, String prefix)
	{
		return name.equals(prefix) || name.startsWith(prefix + " ");
	}

	private static int tier(String value)
	{
		String name = normalized(value);
		for (int index = name.length() - 1; index >= 0; index--)
		{
			char valueAtIndex = name.charAt(index);
			if (Character.isDigit(valueAtIndex))
			{
				return valueAtIndex - '0';
			}
		}
		return 0;
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ENGLISH);
	}
}

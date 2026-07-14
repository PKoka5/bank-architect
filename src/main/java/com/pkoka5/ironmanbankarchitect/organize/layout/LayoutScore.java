package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.Arrays;
import java.util.Objects;

/**
 * The lexicographic layout score: eighteen named non-negative integer components, minimized from
 * left to right. There is no weighted floating-point total, and the {@link DeterministicTieKey}
 * deliberately stays outside this numeric tuple — it is consulted only when two scores are exactly
 * equal, via {@link #compareByScoreThenTieKey}.
 *
 * <p>The two movement components exist only for a proven complete dense category order. Use
 * {@link Builder#movement(LayoutRequest, int, int)} to keep them zero whenever that proof is absent.
 * {@code unrestrictedSwapLowerBound} allows unrestricted swaps and does not model tab transfers or
 * guidance restrictions.</p>
 */
public final class LayoutScore implements Comparable<LayoutScore>
{
	public static final int COMPONENT_COUNT = 18;

	private static final int HIGH_MISSED_RELATIONS = 0;
	private static final int HIGH_MISSED_COMPLETENESS = 1;
	private static final int MEDIUM_MISSED_RELATIONS = 2;
	private static final int MEDIUM_MISSED_COMPLETENESS = 3;
	private static final int LOW_MISSED_RELATIONS = 4;
	private static final int LOW_MISSED_COMPLETENESS = 5;
	private static final int ORIENTATION_EVIDENCE_REGRET = 6;
	private static final int WIDTH_EVIDENCE_REGRET = 7;
	private static final int SEMANTIC_FRAGMENTATION = 8;
	private static final int SEMANTIC_ROW_BREAKS = 9;
	private static final int START_COLUMN_DEVIATION = 10;
	private static final int LOCKED_PREFIX_SPILLOVER = 11;
	private static final int SPILLOVER_COMPATIBILITY_COST = 12;
	private static final int SPILLOVER_TRANSITIONS = 13;
	private static final int NOMINAL_FOOTPRINT_SLACK = 14;
	private static final int SEMANTIC_SPAN = 15;
	private static final int UNRESTRICTED_SWAP_LOWER_BOUND = 16;
	private static final int TOTAL_RANK_DISPLACEMENT = 17;

	private final int[] components;

	private LayoutScore(int[] components)
	{
		this.components = components;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static LayoutScore zero()
	{
		return builder().build();
	}

	/**
	 * Compares two complete plans: first by the numeric score tuple, and only when the scores are
	 * exactly equal by the separate deterministic tie key.
	 */
	public static int compareByScoreThenTieKey(LayoutScore leftScore, DeterministicTieKey leftKey,
		LayoutScore rightScore, DeterministicTieKey rightKey)
	{
		int result = leftScore.compareTo(rightScore);
		if (result != 0)
		{
			return result;
		}

		return leftKey.compareTo(rightKey);
	}

	public int getHighMissedRelations()
	{
		return components[HIGH_MISSED_RELATIONS];
	}

	public int getHighMissedCompleteness()
	{
		return components[HIGH_MISSED_COMPLETENESS];
	}

	public int getMediumMissedRelations()
	{
		return components[MEDIUM_MISSED_RELATIONS];
	}

	public int getMediumMissedCompleteness()
	{
		return components[MEDIUM_MISSED_COMPLETENESS];
	}

	public int getLowMissedRelations()
	{
		return components[LOW_MISSED_RELATIONS];
	}

	public int getLowMissedCompleteness()
	{
		return components[LOW_MISSED_COMPLETENESS];
	}

	public int getOrientationEvidenceRegret()
	{
		return components[ORIENTATION_EVIDENCE_REGRET];
	}

	public int getWidthEvidenceRegret()
	{
		return components[WIDTH_EVIDENCE_REGRET];
	}

	public int getSemanticFragmentation()
	{
		return components[SEMANTIC_FRAGMENTATION];
	}

	public int getSemanticRowBreaks()
	{
		return components[SEMANTIC_ROW_BREAKS];
	}

	public int getStartColumnDeviation()
	{
		return components[START_COLUMN_DEVIATION];
	}

	public int getLockedPrefixSpillover()
	{
		return components[LOCKED_PREFIX_SPILLOVER];
	}

	public int getSpilloverCompatibilityCost()
	{
		return components[SPILLOVER_COMPATIBILITY_COST];
	}

	public int getSpilloverTransitions()
	{
		return components[SPILLOVER_TRANSITIONS];
	}

	public int getNominalFootprintSlack()
	{
		return components[NOMINAL_FOOTPRINT_SLACK];
	}

	public int getSemanticSpan()
	{
		return components[SEMANTIC_SPAN];
	}

	public int getUnrestrictedSwapLowerBound()
	{
		return components[UNRESTRICTED_SWAP_LOWER_BOUND];
	}

	public int getTotalRankDisplacement()
	{
		return components[TOTAL_RANK_DISPLACEMENT];
	}

	/**
	 * The full component tuple in comparison order, as a defensive copy.
	 */
	public int[] toComponentArray()
	{
		return Arrays.copyOf(components, components.length);
	}

	int componentAt(int index)
	{
		return components[index];
	}

	@Override
	public int compareTo(LayoutScore other)
	{
		for (int index = 0; index < COMPONENT_COUNT; index++)
		{
			int result = Integer.compare(components[index], other.components[index]);
			if (result != 0)
			{
				return result;
			}
		}

		return 0;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof LayoutScore))
		{
			return false;
		}

		return Arrays.equals(components, ((LayoutScore) other).components);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(components);
	}

	@Override
	public String toString()
	{
		return "LayoutScore" + Arrays.toString(components);
	}

	public static final class Builder
	{
		private final int[] components = new int[COMPONENT_COUNT];

		private Builder()
		{
		}

		public Builder highMissedRelations(int value)
		{
			return set(HIGH_MISSED_RELATIONS, value, "highMissedRelations");
		}

		public Builder highMissedCompleteness(int value)
		{
			return set(HIGH_MISSED_COMPLETENESS, value, "highMissedCompleteness");
		}

		public Builder mediumMissedRelations(int value)
		{
			return set(MEDIUM_MISSED_RELATIONS, value, "mediumMissedRelations");
		}

		public Builder mediumMissedCompleteness(int value)
		{
			return set(MEDIUM_MISSED_COMPLETENESS, value, "mediumMissedCompleteness");
		}

		public Builder lowMissedRelations(int value)
		{
			return set(LOW_MISSED_RELATIONS, value, "lowMissedRelations");
		}

		public Builder lowMissedCompleteness(int value)
		{
			return set(LOW_MISSED_COMPLETENESS, value, "lowMissedCompleteness");
		}

		public Builder orientationEvidenceRegret(int value)
		{
			return set(ORIENTATION_EVIDENCE_REGRET, value, "orientationEvidenceRegret");
		}

		public Builder widthEvidenceRegret(int value)
		{
			return set(WIDTH_EVIDENCE_REGRET, value, "widthEvidenceRegret");
		}

		public Builder semanticFragmentation(int value)
		{
			return set(SEMANTIC_FRAGMENTATION, value, "semanticFragmentation");
		}

		public Builder semanticRowBreaks(int value)
		{
			return set(SEMANTIC_ROW_BREAKS, value, "semanticRowBreaks");
		}

		public Builder startColumnDeviation(int value)
		{
			return set(START_COLUMN_DEVIATION, value, "startColumnDeviation");
		}

		public Builder lockedPrefixSpillover(int value)
		{
			return set(LOCKED_PREFIX_SPILLOVER, value, "lockedPrefixSpillover");
		}

		public Builder spilloverCompatibilityCost(int value)
		{
			return set(SPILLOVER_COMPATIBILITY_COST, value, "spilloverCompatibilityCost");
		}

		public Builder spilloverTransitions(int value)
		{
			return set(SPILLOVER_TRANSITIONS, value, "spilloverTransitions");
		}

		public Builder nominalFootprintSlack(int value)
		{
			return set(NOMINAL_FOOTPRINT_SLACK, value, "nominalFootprintSlack");
		}

		public Builder semanticSpan(int value)
		{
			return set(SEMANTIC_SPAN, value, "semanticSpan");
		}

		/**
		 * The only way to set the two movement components ({@code unrestrictedSwapLowerBound} and
		 * {@code totalRankDisplacement}): there are deliberately no direct setters. Without a
		 * validated request carrying a proven complete dense category order both components are forced
		 * to zero for every plan, regardless of the supplied values.
		 */
		public Builder movement(LayoutRequest request, int unrestrictedSwapLowerBound,
			int totalRankDisplacement)
		{
			Objects.requireNonNull(request, "request");
			boolean provenCompleteDenseOrder = request.hasCurrentDenseCategoryOrder()
				&& LayoutRequestValidator.validate(request).isEmpty();
			if (!provenCompleteDenseOrder)
			{
				return set(UNRESTRICTED_SWAP_LOWER_BOUND, 0, "unrestrictedSwapLowerBound")
					.set(TOTAL_RANK_DISPLACEMENT, 0, "totalRankDisplacement");
			}

			return set(UNRESTRICTED_SWAP_LOWER_BOUND, unrestrictedSwapLowerBound, "unrestrictedSwapLowerBound")
				.set(TOTAL_RANK_DISPLACEMENT, totalRankDisplacement, "totalRankDisplacement");
		}

		public LayoutScore build()
		{
			return new LayoutScore(Arrays.copyOf(components, components.length));
		}

		private Builder set(int index, int value, String name)
		{
			if (value < 0)
			{
				throw new IllegalArgumentException(name + " must not be negative");
			}

			components[index] = value;
			return this;
		}
	}
}

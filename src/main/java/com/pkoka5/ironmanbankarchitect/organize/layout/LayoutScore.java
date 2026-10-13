package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.*;


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
	/** Declaration order is the lexicographic score priority. */
	public enum Component
	{
		HIGH_MISSED_RELATIONS,
		HIGH_MISSED_COMPLETENESS,
		MEDIUM_MISSED_RELATIONS,
		MEDIUM_MISSED_COMPLETENESS,
		LOW_MISSED_RELATIONS,
		LOW_MISSED_COMPLETENESS,
		ORIENTATION_EVIDENCE_REGRET,
		WIDTH_EVIDENCE_REGRET,
		SEMANTIC_FRAGMENTATION,
		SEMANTIC_ROW_BREAKS,
		START_COLUMN_DEVIATION,
		LOCKED_PREFIX_SPILLOVER,
		SPILLOVER_COMPATIBILITY_COST,
		SPILLOVER_TRANSITIONS,
		NOMINAL_FOOTPRINT_SLACK,
		SEMANTIC_SPAN,
		UNRESTRICTED_SWAP_LOWER_BOUND,
		TOTAL_RANK_DISPLACEMENT
	}

	public static final int COMPONENT_COUNT = Component.values().length;

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

	public int get(Component component)
	{
		return components[component.ordinal()];
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
		return Arrays.compare(components, other.components);
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

		public Builder set(Component component, int value)
		{
			if (component.ordinal() >= Component.UNRESTRICTED_SWAP_LOWER_BOUND.ordinal())
				throw new IllegalArgumentException("Movement components require a validated request");
			return put(component, value);
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
			return put(Component.UNRESTRICTED_SWAP_LOWER_BOUND, provenCompleteDenseOrder ? unrestrictedSwapLowerBound : 0)
				.put(Component.TOTAL_RANK_DISPLACEMENT, provenCompleteDenseOrder ? totalRankDisplacement : 0);
		}

		public LayoutScore build()
		{
			return new LayoutScore(Arrays.copyOf(components, components.length));
		}

		private Builder put(Component component, int value)
		{
			if (value < 0)
			{
				throw new IllegalArgumentException(component + " must not be negative");
			}

			components[component.ordinal()] = value;
			return this;
		}
	}
}

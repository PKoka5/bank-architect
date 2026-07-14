package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable aggregate template evidence for nominal block widths 1 through 8. Each vector entry is
 * an exact distinct-template or distinct-family support count for that width; missing widths may
 * not be silently treated as zero.
 *
 * <p>A preference becomes active only when the best width has support from at least five
 * templates and three families, reaches a 60% integer support rate, and leads the runner-up by at
 * least two templates. When those thresholds are not met every width has zero regret and rank.</p>
 */
public final class WidthEvidence
{
	private static final int MIN_TOP_TEMPLATE_SUPPORT = 5;
	private static final int MIN_TOP_FAMILY_SUPPORT = 3;
	private static final int MIN_SUPPORT_RATE = 600;
	private static final int MIN_RUNNER_UP_LEAD = 2;

	private final int eligibleTemplateCount;
	private final List<Integer> distinctTemplateSupportByWidth;
	private final List<Integer> distinctFamilySupportByWidth;
	private final boolean hasPreferredWidth;
	private final int preferredWidth;
	private final int bestSupportRate;

	public WidthEvidence(int eligibleTemplateCount, List<Integer> distinctTemplateSupportByWidth,
		List<Integer> distinctFamilySupportByWidth)
	{
		if (eligibleTemplateCount <= 0)
		{
			throw new IllegalArgumentException("eligibleTemplateCount must be positive");
		}
		this.eligibleTemplateCount = eligibleTemplateCount;
		this.distinctTemplateSupportByWidth = requireSupportVector(
			distinctTemplateSupportByWidth, eligibleTemplateCount, true);
		this.distinctFamilySupportByWidth = requireSupportVector(
			distinctFamilySupportByWidth, Integer.MAX_VALUE, false);
		validateAlignedSupport(this.distinctTemplateSupportByWidth, this.distinctFamilySupportByWidth);

		int bestWidth = SemanticRule.MIN_WIDTH;
		for (int width = SemanticRule.MIN_WIDTH + 1; width <= SemanticRule.MAX_WIDTH; width++)
		{
			if (getDistinctTemplateSupport(width) > getDistinctTemplateSupport(bestWidth))
			{
				bestWidth = width;
			}
		}

		int bestTemplateSupport = getDistinctTemplateSupport(bestWidth);
		int runnerUpSupport = 0;
		for (int width = SemanticRule.MIN_WIDTH; width <= SemanticRule.MAX_WIDTH; width++)
		{
			if (width != bestWidth)
			{
				runnerUpSupport = Math.max(runnerUpSupport, getDistinctTemplateSupport(width));
			}
		}

		this.bestSupportRate = supportRate(bestTemplateSupport);
		this.hasPreferredWidth = bestTemplateSupport >= MIN_TOP_TEMPLATE_SUPPORT
			&& getDistinctFamilySupport(bestWidth) >= MIN_TOP_FAMILY_SUPPORT
			&& bestSupportRate >= MIN_SUPPORT_RATE
			&& bestTemplateSupport - runnerUpSupport >= MIN_RUNNER_UP_LEAD;
		this.preferredWidth = bestWidth;
	}

	public int getEligibleTemplateCount()
	{
		return eligibleTemplateCount;
	}

	public List<Integer> getDistinctTemplateSupportByWidth()
	{
		return distinctTemplateSupportByWidth;
	}

	public List<Integer> getDistinctFamilySupportByWidth()
	{
		return distinctFamilySupportByWidth;
	}

	public int getDistinctTemplateSupport(int width)
	{
		return distinctTemplateSupportByWidth.get(index(width));
	}

	public int getDistinctFamilySupport(int width)
	{
		return distinctFamilySupportByWidth.get(index(width));
	}

	public int getSupportRate(int width)
	{
		return supportRate(getDistinctTemplateSupport(width));
	}

	public boolean hasPreferredWidth()
	{
		return hasPreferredWidth;
	}

	public int getPreferredWidth()
	{
		if (!hasPreferredWidth)
		{
			throw new IllegalStateException("width evidence does not clear the preference threshold");
		}
		return preferredWidth;
	}

	/**
	 * Exact integer support-rate regret for one width. Inactive evidence is deliberately neutral.
	 */
	public int regretForWidth(int width)
	{
		int supportRate = getSupportRate(width);
		return hasPreferredWidth ? bestSupportRate - supportRate : 0;
	}

	/**
	 * Dense evidence rank used only after numeric-score equality: best support is rank 0, and equal
	 * support counts share a rank. Inactive evidence gives every width rank 0.
	 */
	public int preferenceRankForWidth(int width)
	{
		int support = getDistinctTemplateSupport(width);
		if (!hasPreferredWidth)
		{
			return 0;
		}

		Set<Integer> higherSupportCounts = new HashSet<>();
		for (Integer candidateSupport : distinctTemplateSupportByWidth)
		{
			if (candidateSupport > support)
			{
				higherSupportCounts.add(candidateSupport);
			}
		}
		return higherSupportCounts.size();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof WidthEvidence))
		{
			return false;
		}

		WidthEvidence evidence = (WidthEvidence) other;
		return eligibleTemplateCount == evidence.eligibleTemplateCount
			&& distinctTemplateSupportByWidth.equals(evidence.distinctTemplateSupportByWidth)
			&& distinctFamilySupportByWidth.equals(evidence.distinctFamilySupportByWidth);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(eligibleTemplateCount, distinctTemplateSupportByWidth,
			distinctFamilySupportByWidth);
	}

	@Override
	public String toString()
	{
		return "WidthEvidence{eligible=" + eligibleTemplateCount + ", templates="
			+ distinctTemplateSupportByWidth + ", families=" + distinctFamilySupportByWidth + "}";
	}

	private int supportRate(int support)
	{
		return (int) ((1000L * support) / eligibleTemplateCount);
	}

	private static int index(int width)
	{
		if (width < SemanticRule.MIN_WIDTH || width > SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("width must be within "
				+ SemanticRule.MIN_WIDTH + ".." + SemanticRule.MAX_WIDTH);
		}
		return width - SemanticRule.MIN_WIDTH;
	}

	private static List<Integer> requireSupportVector(List<Integer> values, int maximum,
		boolean templateVector)
	{
		if (values == null || values.size() != SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("support vectors must contain exactly one value for widths 1..8");
		}

		List<Integer> validated = new ArrayList<>(values.size());
		for (Integer value : values)
		{
			if (value == null || value < 0 || value > maximum)
			{
				throw new IllegalArgumentException(templateVector
					? "template support must be within 0..eligibleTemplateCount"
					: "family support must not be negative");
			}
			validated.add(value);
		}
		return Collections.unmodifiableList(validated);
	}

	private static void validateAlignedSupport(List<Integer> templateSupport, List<Integer> familySupport)
	{
		for (int index = 0; index < templateSupport.size(); index++)
		{
			if ((templateSupport.get(index) == 0) != (familySupport.get(index) == 0))
			{
				throw new IllegalArgumentException(
					"template and family support must both be zero or both be positive for each width");
			}
		}
	}
}

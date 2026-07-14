package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * One immutable eligible semantic block group plus all of its feasible canonical local shapes.
 * A group may intentionally have no candidates: the later packer must still retain its fallback
 * transition and charge the group's missed semantic relations and completeness.
 */
final class LayoutCandidateGroup
{
	private final SemanticRule rule;
	private final List<AtomProjection> atomProjections;
	private final List<String> atomKeys;
	private final List<Integer> projectedItemIds;
	private final List<LayoutCandidate> candidates;
	private final int missedRelations;
	private final int missedCompleteness;

	LayoutCandidateGroup(SemanticRule rule, List<AtomProjection> atomProjections,
		List<LayoutCandidate> candidates)
	{
		this.rule = Objects.requireNonNull(rule, "rule");
		this.atomProjections = requireProjections(rule, atomProjections);
		this.atomKeys = atomKeys(this.atomProjections);
		this.projectedItemIds = projectedItemIds(this.atomProjections);
		this.candidates = requireCandidates(rule, this.atomProjections, this.atomKeys, candidates);
		this.missedRelations = sumRelations(this.atomProjections);
		this.missedCompleteness = sumCompleteness(this.atomProjections);
	}

	SemanticRule getRule()
	{
		return rule;
	}

	String getRuleKey()
	{
		return rule.getRuleKey();
	}

	ShapePrimitive getShapePrimitive()
	{
		return rule.getShapePrimitive();
	}

	ConfidenceTier getConfidenceTier()
	{
		return rule.getConfidenceTier();
	}

	List<AtomProjection> getAtomProjections()
	{
		return atomProjections;
	}

	List<String> getAtomKeys()
	{
		return atomKeys;
	}

	List<Integer> getProjectedItemIds()
	{
		return projectedItemIds;
	}

	List<LayoutCandidate> getCandidates()
	{
		return candidates;
	}

	int getMissedRelations()
	{
		return missedRelations;
	}

	int getMissedCompleteness()
	{
		return missedCompleteness;
	}

	void requireCandidate(LayoutCandidate candidate)
	{
		if (candidate == null || !candidates.contains(candidate))
		{
			throw new IllegalArgumentException("candidate does not belong to group "
				+ getRuleKey() + "/" + atomKeys);
		}
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof LayoutCandidateGroup))
		{
			return false;
		}

		LayoutCandidateGroup group = (LayoutCandidateGroup) other;
		return sameRuleFacts(rule, group.rule)
			&& atomProjections.equals(group.atomProjections)
			&& candidates.equals(group.candidates);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(rule.getRuleKey(), rule.getConfidenceTier(), rule.getShapePrimitive(),
			rule.getAllowedWidths(), widthEvidenceOrNull(rule), rule.getSpilloverCompatibleRuleKeys(),
			atomProjections, candidates);
	}

	@Override
	public String toString()
	{
		return "LayoutCandidateGroup{" + getRuleKey() + ", " + getShapePrimitive() + ", atoms="
			+ atomKeys + ", projected=" + projectedItemIds + ", candidates=" + candidates + "}";
	}

	private static List<AtomProjection> requireProjections(SemanticRule rule,
		List<AtomProjection> projections)
	{
		if (projections == null || projections.isEmpty())
		{
			throw new IllegalArgumentException("atomProjections must not be empty");
		}

		List<AtomProjection> validated = new ArrayList<>(projections.size());
		Set<String> seenAtomKeys = new HashSet<>();
		Set<Integer> seenItemIds = new HashSet<>();
		int previousAtomIndex = -1;
		for (AtomProjection projection : projections)
		{
			if (projection == null)
			{
				throw new IllegalArgumentException("atomProjections must not contain null");
			}
			if (!seenAtomKeys.add(projection.getAtomKey()))
			{
				throw new IllegalArgumentException("duplicate projected atom " + projection.getAtomKey());
			}

			int atomIndex = validateProjectionAgainstRule(rule, projection);
			if (atomIndex <= previousAtomIndex)
			{
				throw new IllegalArgumentException("atomProjections must retain reviewed atom order");
			}
			previousAtomIndex = atomIndex;

			for (Integer itemId : projection.getItemIds())
			{
				if (!seenItemIds.add(itemId))
				{
					throw new IllegalArgumentException("duplicate projected item ID " + itemId);
				}
			}
			validated.add(projection);
		}

		ShapePrimitive primitive = rule.getShapePrimitive();
		if ((primitive == ShapePrimitive.HORIZONTAL_RUN || primitive == ShapePrimitive.VERTICAL_RUN)
			&& validated.size() != 1)
		{
			throw new IllegalArgumentException("run groups must contain exactly one atom projection");
		}
		if (primitive == ShapePrimitive.STAGE_MATRIX)
		{
			List<String> signature = validated.get(0).getMemberKeys();
			for (AtomProjection projection : validated)
			{
				if (!signature.equals(projection.getMemberKeys()))
				{
					throw new IllegalArgumentException(
						"stage-matrix projections must have the same ordered member signature");
				}
			}
		}

		return Collections.unmodifiableList(validated);
	}

	private static int validateProjectionAgainstRule(SemanticRule rule, AtomProjection projection)
	{
		for (int atomIndex = 0; atomIndex < rule.getAtoms().size(); atomIndex++)
		{
			SemanticAtom atom = rule.getAtoms().get(atomIndex);
			if (!atom.getAtomKey().equals(projection.getAtomKey()))
			{
				continue;
			}
			if (atom.getMembers().size() != projection.getReviewedMemberCount())
			{
				throw new IllegalArgumentException("projection reviewed-member count does not match atom "
					+ projection.getAtomKey());
			}

			int projectedIndex = 0;
			for (SemanticAtom.Member member : atom.getMembers())
			{
				if (projectedIndex < projection.size()
					&& member.getMemberKey().equals(projection.getMemberKeys().get(projectedIndex))
					&& member.getItemId() == projection.getItemIds().get(projectedIndex))
				{
					projectedIndex++;
				}
			}
			if (projectedIndex != projection.size())
			{
				throw new IllegalArgumentException("projection is not an ordered member subset of atom "
					+ projection.getAtomKey());
			}
			return atomIndex;
		}

		throw new IllegalArgumentException("projection atom is absent from rule: " + projection.getAtomKey());
	}

	private static List<LayoutCandidate> requireCandidates(SemanticRule rule,
		List<AtomProjection> projections, List<String> atomKeys, List<LayoutCandidate> candidates)
	{
		Objects.requireNonNull(candidates, "candidates");
		List<LayoutCandidate> canonical = new ArrayList<>(candidates.size());
		for (LayoutCandidate candidate : candidates)
		{
			if (candidate == null)
			{
				throw new IllegalArgumentException("candidates must not contain null");
			}
			if (!rule.getRuleKey().equals(candidate.getRuleKey())
				|| rule.getShapePrimitive() != candidate.getShapePrimitive()
				|| !atomKeys.equals(candidate.getAtomKeys()))
			{
				throw new IllegalArgumentException("candidate identity does not match its group");
			}
			if (!rule.getAllowedWidths().contains(candidate.getWidth()))
			{
				throw new IllegalArgumentException("candidate width is not allowed by its rule");
			}
			canonical.add(candidate);
		}

		canonical.sort(Comparator.comparingInt(LayoutCandidate::getWidth));

		List<LayoutCandidate> expected = new ArrayList<>();
		for (int width = SemanticRule.MIN_WIDTH; width <= SemanticRule.MAX_WIDTH; width++)
		{
			if (!rule.getAllowedWidths().contains(width))
			{
				continue;
			}
			List<LayoutCandidate.Row> rows = LayoutCandidateGeometry.rowsFor(
				rule.getShapePrimitive(), projections, width);
			if (rows != null)
			{
				expected.add(new LayoutCandidate(rule.getRuleKey(), rule.getShapePrimitive(), width,
					atomKeys, rows));
			}
		}
		if (!expected.equals(canonical))
		{
			throw new IllegalArgumentException(
				"candidates must exactly match every feasible canonical width for their group");
		}
		return Collections.unmodifiableList(canonical);
	}

	private static List<String> atomKeys(List<AtomProjection> projections)
	{
		List<String> keys = new ArrayList<>(projections.size());
		for (AtomProjection projection : projections)
		{
			keys.add(projection.getAtomKey());
		}
		return Collections.unmodifiableList(keys);
	}

	private static List<Integer> projectedItemIds(List<AtomProjection> projections)
	{
		List<Integer> itemIds = new ArrayList<>();
		for (AtomProjection projection : projections)
		{
			itemIds.addAll(projection.getItemIds());
		}
		return Collections.unmodifiableList(itemIds);
	}

	private static int sumRelations(List<AtomProjection> projections)
	{
		int result = 0;
		for (AtomProjection projection : projections)
		{
			result = Math.addExact(result, projection.getMissedRelations());
		}
		return result;
	}

	private static int sumCompleteness(List<AtomProjection> projections)
	{
		int result = 0;
		for (AtomProjection projection : projections)
		{
			result = Math.addExact(result, projection.getMissedCompleteness());
		}
		return result;
	}

	private static boolean sameRuleFacts(SemanticRule left, SemanticRule right)
	{
		return left.getRuleKey().equals(right.getRuleKey())
			&& left.getConfidenceTier() == right.getConfidenceTier()
			&& left.getShapePrimitive() == right.getShapePrimitive()
			&& left.getAllowedWidths().equals(right.getAllowedWidths())
			&& Objects.equals(widthEvidenceOrNull(left), widthEvidenceOrNull(right))
			&& left.getSpilloverCompatibleRuleKeys().equals(right.getSpilloverCompatibleRuleKeys());
	}

	private static WidthEvidence widthEvidenceOrNull(SemanticRule rule)
	{
		return rule.hasWidthEvidence() ? rule.getWidthEvidence() : null;
	}

	/**
	 * One eligible atom projected onto its present members, retaining reviewed member-key order and
	 * the reviewed family size needed for exact completeness scoring.
	 */
	static final class AtomProjection
	{
		private final String atomKey;
		private final int reviewedMemberCount;
		private final List<String> memberKeys;
		private final List<Integer> itemIds;

		AtomProjection(String atomKey, int reviewedMemberCount, List<String> memberKeys,
			List<Integer> itemIds)
		{
			this.atomKey = SemanticRule.requireRuleKey(atomKey, "atomKey");
			if (reviewedMemberCount < 2)
			{
				throw new IllegalArgumentException("reviewedMemberCount must be at least two");
			}
			if (memberKeys == null || itemIds == null || memberKeys.size() != itemIds.size()
				|| memberKeys.size() < 2 || memberKeys.size() > reviewedMemberCount)
			{
				throw new IllegalArgumentException(
					"projected member keys and item IDs must have the same eligible size");
			}

			Set<String> seenKeys = new HashSet<>();
			Set<Integer> seenItemIds = new HashSet<>();
			List<String> validatedKeys = new ArrayList<>(memberKeys.size());
			List<Integer> validatedItemIds = new ArrayList<>(itemIds.size());
			for (int index = 0; index < memberKeys.size(); index++)
			{
				String memberKey = SemanticRule.requireRuleKey(memberKeys.get(index), "memberKey");
				Integer itemId = itemIds.get(index);
				if (!seenKeys.add(memberKey))
				{
					throw new IllegalArgumentException("duplicate projected member key " + memberKey);
				}
				if (itemId == null || itemId <= 0 || itemId == ItemID.BANK_FILLER
					|| !seenItemIds.add(itemId))
				{
					throw new IllegalArgumentException("projected item IDs must be positive, unique, and real");
				}
				validatedKeys.add(memberKey);
				validatedItemIds.add(itemId);
			}

			this.reviewedMemberCount = reviewedMemberCount;
			this.memberKeys = Collections.unmodifiableList(validatedKeys);
			this.itemIds = Collections.unmodifiableList(validatedItemIds);
		}

		String getAtomKey()
		{
			return atomKey;
		}

		int getReviewedMemberCount()
		{
			return reviewedMemberCount;
		}

		List<String> getMemberKeys()
		{
			return memberKeys;
		}

		List<Integer> getItemIds()
		{
			return itemIds;
		}

		int size()
		{
			return itemIds.size();
		}

		int getMissedRelations()
		{
			return size() - 1;
		}

		int getMissedCompleteness()
		{
			return (int) ((1000L * size()) / reviewedMemberCount);
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof AtomProjection))
			{
				return false;
			}

			AtomProjection projection = (AtomProjection) other;
			return reviewedMemberCount == projection.reviewedMemberCount
				&& atomKey.equals(projection.atomKey)
				&& memberKeys.equals(projection.memberKeys)
				&& itemIds.equals(projection.itemIds);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(atomKey, reviewedMemberCount, memberKeys, itemIds);
		}

		@Override
		public String toString()
		{
			return atomKey + "{" + memberKeys + "=" + itemIds + ", reviewed="
				+ reviewedMemberCount + "}";
		}
	}
}

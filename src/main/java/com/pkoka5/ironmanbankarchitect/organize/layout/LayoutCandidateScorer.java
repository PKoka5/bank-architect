package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.Objects;
import static com.pkoka5.ironmanbankarchitect.organize.layout.LayoutScore.Component.*;

/**
 * Candidate-local 3A3 scoring. Only width evidence and canonical stage-matrix fragmentation are
 * known before placement; every packing, spillover, footprint, span, and movement component stays
 * zero until the bounded packer has a concrete state.
 */
final class LayoutCandidateScorer
{
	private LayoutCandidateScorer()
	{
	}

	static LayoutScore score(LayoutCandidateGroup group, LayoutCandidate candidate)
	{
		Objects.requireNonNull(group, "group");
		group.requireCandidate(candidate);

		return LayoutScore.builder()
			.set(WIDTH_EVIDENCE_REGRET, widthRegret(group.getRule(), candidate.getWidth()))
			.set(SEMANTIC_FRAGMENTATION, fragmentation(group, candidate))
			.build();
	}

	static int widthPreferenceRank(LayoutCandidateGroup group, LayoutCandidate candidate)
	{
		Objects.requireNonNull(group, "group");
		group.requireCandidate(candidate);
		SemanticRule rule = group.getRule();
		return rule.hasWidthEvidence()
			? rule.getWidthEvidence().preferenceRankForWidth(candidate.getWidth())
			: 0;
	}

	static LayoutScore scoreFallback(LayoutCandidateGroup group)
	{
		Objects.requireNonNull(group, "group");
		LayoutScore.Builder score = LayoutScore.builder();
		switch (group.getConfidenceTier())
		{
			case HIGH:
				return score.set(HIGH_MISSED_RELATIONS, group.getMissedRelations())
					.set(HIGH_MISSED_COMPLETENESS, group.getMissedCompleteness())
					.build();
			case MEDIUM:
				return score.set(MEDIUM_MISSED_RELATIONS, group.getMissedRelations())
					.set(MEDIUM_MISSED_COMPLETENESS, group.getMissedCompleteness())
					.build();
			case LOW:
				return score.set(LOW_MISSED_RELATIONS, group.getMissedRelations())
					.set(LOW_MISSED_COMPLETENESS, group.getMissedCompleteness())
					.build();
			default:
				throw new IllegalStateException("Unhandled confidence tier: " + group.getConfidenceTier());
		}
	}

	private static int widthRegret(SemanticRule rule, int width)
	{
		return rule.hasWidthEvidence() ? rule.getWidthEvidence().regretForWidth(width) : 0;
	}

	private static int fragmentation(LayoutCandidateGroup group, LayoutCandidate candidate)
	{
		if (group.getShapePrimitive() != ShapePrimitive.STAGE_MATRIX)
		{
			return 0;
		}

		return (group.getAtomKeys().size() - 1) / candidate.getWidth();
	}
}

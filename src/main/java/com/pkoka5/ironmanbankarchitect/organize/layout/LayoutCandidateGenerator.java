package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Pure deterministic generation of eligible semantic block groups and their canonical local
 * shapes. Geometry depends only on a valid request's owned item IDs and reviewed rule topology;
 * entry order, quantities, locks, dense ranks, and width preferences never filter alternatives.
 */
final class LayoutCandidateGenerator
{
	private LayoutCandidateGenerator()
	{
	}

	/**
	 * Flat run-only view retained for the focused 3A2 geometry contract. Packing code must consume
	 * {@link #generate(LayoutRequest)} so eligible groups with no feasible candidate are not lost.
	 */
	static List<LayoutCandidate> generateRunCandidates(LayoutRequest request)
	{
		List<LayoutCandidate> candidates = new ArrayList<>();
		for (LayoutCandidateGroup group : generate(request))
		{
			if (group.getShapePrimitive() == ShapePrimitive.HORIZONTAL_RUN
				|| group.getShapePrimitive() == ShapePrimitive.VERTICAL_RUN)
			{
				candidates.addAll(group.getCandidates());
			}
		}
		return Collections.unmodifiableList(candidates);
	}

	static List<LayoutCandidateGroup> generate(LayoutRequest request)
	{
		Objects.requireNonNull(request, "request");
		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
		if (!conflicts.isEmpty())
		{
			throw new IllegalArgumentException(
				"request must be valid before candidate generation: " + conflicts);
		}

		Set<Integer> ownedItemIds = new HashSet<>();
		for (LayoutEntry entry : request.getEntries())
		{
			ownedItemIds.add(entry.getItem().getItemId());
		}

		List<SemanticRule> rules = new ArrayList<>(request.getRules());
		rules.sort(Comparator.comparing(SemanticRule::getRuleKey));

		List<LayoutCandidateGroup> groups = new ArrayList<>();
		for (SemanticRule rule : rules)
		{
			switch (rule.getShapePrimitive())
			{
				case HORIZONTAL_RUN:
				case VERTICAL_RUN:
					groups.addAll(generateRunGroups(rule, ownedItemIds));
					break;
				case STAGE_MATRIX:
					groups.addAll(generateStageMatrixGroups(rule, ownedItemIds));
					break;
				case ROW_GROUP_MATRIX:
					LayoutCandidateGroup rowGroup = generateRowGroupMatrix(rule, ownedItemIds);
					if (rowGroup != null)
					{
						groups.add(rowGroup);
					}
					break;
				default:
					throw new IllegalStateException("Unhandled shape primitive: " + rule.getShapePrimitive());
			}
		}

		return Collections.unmodifiableList(groups);
	}

	private static List<LayoutCandidateGroup> generateRunGroups(SemanticRule rule,
		Set<Integer> ownedItemIds)
	{
		List<LayoutCandidateGroup> groups = new ArrayList<>();
		for (SemanticAtom atom : rule.getAtoms())
		{
			LayoutCandidateGroup.AtomProjection projection = project(atom, ownedItemIds);
			if (projection == null)
			{
				continue;
			}

			List<LayoutCandidateGroup.AtomProjection> projections = Collections.singletonList(projection);
			List<LayoutCandidate> candidates = new ArrayList<>();
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
					candidates.add(candidate(rule, width, projections, rows));
				}
			}
			groups.add(new LayoutCandidateGroup(rule, projections, candidates));
		}
		return groups;
	}

	private static List<LayoutCandidateGroup> generateStageMatrixGroups(SemanticRule rule,
		Set<Integer> ownedItemIds)
	{
		Map<List<String>, List<LayoutCandidateGroup.AtomProjection>> projectionsBySignature =
			new LinkedHashMap<>();
		for (SemanticAtom atom : rule.getAtoms())
		{
			LayoutCandidateGroup.AtomProjection projection = project(atom, ownedItemIds);
			if (projection != null)
			{
				projectionsBySignature.computeIfAbsent(projection.getMemberKeys(), key -> new ArrayList<>())
					.add(projection);
			}
		}

		List<LayoutCandidateGroup> groups = new ArrayList<>();
		for (List<LayoutCandidateGroup.AtomProjection> projections : projectionsBySignature.values())
		{
			List<LayoutCandidate> candidates = new ArrayList<>();
			for (int width = SemanticRule.MIN_WIDTH; width <= SemanticRule.MAX_WIDTH; width++)
			{
				if (rule.getAllowedWidths().contains(width))
				{
					candidates.add(candidate(rule, width, projections,
						LayoutCandidateGeometry.rowsFor(rule.getShapePrimitive(), projections, width)));
				}
			}
			groups.add(new LayoutCandidateGroup(rule, projections, candidates));
		}
		return groups;
	}

	private static LayoutCandidateGroup generateRowGroupMatrix(SemanticRule rule,
		Set<Integer> ownedItemIds)
	{
		List<LayoutCandidateGroup.AtomProjection> projections = new ArrayList<>();
		for (SemanticAtom atom : rule.getAtoms())
		{
			LayoutCandidateGroup.AtomProjection projection = project(atom, ownedItemIds);
			if (projection != null)
			{
				projections.add(projection);
			}
		}
		if (projections.isEmpty())
		{
			return null;
		}

		List<LayoutCandidate> candidates = new ArrayList<>();
		for (int width = SemanticRule.MIN_WIDTH; width <= SemanticRule.MAX_WIDTH; width++)
		{
			if (rule.getAllowedWidths().contains(width))
			{
				List<LayoutCandidate.Row> rows = LayoutCandidateGeometry.rowsFor(
					rule.getShapePrimitive(), projections, width);
				if (rows != null)
				{
					candidates.add(candidate(rule, width, projections, rows));
				}
			}
		}
		return new LayoutCandidateGroup(rule, projections, candidates);
	}

	private static LayoutCandidateGroup.AtomProjection project(SemanticAtom atom,
		Set<Integer> ownedItemIds)
	{
		List<String> memberKeys = new ArrayList<>();
		List<Integer> itemIds = new ArrayList<>();
		for (SemanticAtom.Member member : atom.getMembers())
		{
			if (ownedItemIds.contains(member.getItemId()))
			{
				memberKeys.add(member.getMemberKey());
				itemIds.add(member.getItemId());
			}
		}
		return itemIds.size() < 2 ? null : new LayoutCandidateGroup.AtomProjection(
			atom.getAtomKey(), atom.getMembers().size(), memberKeys, itemIds);
	}

	private static LayoutCandidate candidate(SemanticRule rule, int width,
		List<LayoutCandidateGroup.AtomProjection> projections, List<LayoutCandidate.Row> rows)
	{
		List<String> atomKeys = new ArrayList<>(projections.size());
		for (LayoutCandidateGroup.AtomProjection projection : projections)
		{
			atomKeys.add(projection.getAtomKey());
		}
		return new LayoutCandidate(rule.getRuleKey(), rule.getShapePrimitive(), width, atomKeys, rows);
	}

}

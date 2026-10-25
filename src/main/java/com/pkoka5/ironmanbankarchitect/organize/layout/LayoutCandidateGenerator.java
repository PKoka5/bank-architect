package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.*;
import java.util.List;




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

	static List<LayoutCandidateGroup> generate(LayoutRequest request)
	{
		Objects.requireNonNull(request, "request");
		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
		if (!conflicts.isEmpty())
		{
			throw new IllegalArgumentException(
				"request must be valid before candidate generation: " + conflicts);
		}

		Set<Integer> ownedItemIds = LayoutEntry.presentIds(request.getEntries());

		List<SemanticRule> rules = new ArrayList<>(request.getRules());
		rules.sort(Comparator.comparing(SemanticRule::getRuleKey));

		List<LayoutCandidateGroup> groups = new ArrayList<>();
		for (SemanticRule rule : rules)
		{
			for (List<LayoutCandidateGroup.AtomProjection> projections : projectionGroups(rule, ownedItemIds))
				groups.add(new LayoutCandidateGroup(rule, projections, LayoutCandidateGeometry.candidatesFor(
					rule, projections, LayoutCandidateGroup.atomKeys(projections))));
		}

		return Collections.unmodifiableList(groups);
	}

	private static Collection<List<LayoutCandidateGroup.AtomProjection>> projectionGroups(SemanticRule rule,
		Set<Integer> ownedItemIds)
	{
		Map<List<String>, List<LayoutCandidateGroup.AtomProjection>> groups = new LinkedHashMap<>();
		for (SemanticAtom atom : rule.getAtoms())
		{
			LayoutCandidateGroup.AtomProjection projection = project(atom, ownedItemIds);
			if (projection == null) continue;
			List<String> key;
			switch (rule.getShapePrimitive())
			{
				case HORIZONTAL_RUN:
				case VERTICAL_RUN:
					key = Collections.singletonList(projection.getAtomKey());
					break;
				case STAGE_MATRIX:
					key = projection.getMemberKeys();
					break;
				case ROW_GROUP_MATRIX:
					key = Collections.emptyList();
					break;
				default:
					throw new IllegalStateException("Unhandled shape primitive: " + rule.getShapePrimitive());
			}
			groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(projection);
		}
		return groups.values();
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

}

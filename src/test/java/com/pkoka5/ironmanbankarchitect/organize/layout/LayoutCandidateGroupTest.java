package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class LayoutCandidateGroupTest
{
	@Test
	public void structurallyInfeasibleRunAtomsRemainEligibleFallbackGroups()
	{
		SemanticRule horizontal = rule("long.horizontal", ShapePrimitive.HORIZONTAL_RUN, allWidths(),
			atom("long.family", 1, 2, 3, 4, 5, 6, 7, 8, 9));
		LayoutCandidateGroup horizontalGroup = onlyGroup(
			request(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), horizontal));
		assertTrue(horizontalGroup.getCandidates().isEmpty());
		assertEquals(8, horizontalGroup.getMissedRelations());
		assertEquals(1000, horizontalGroup.getMissedCompleteness());

		SemanticRule vertical = rule("vertical.no-width", ShapePrimitive.VERTICAL_RUN, widths(2, 4, 8),
			atom("vertical.family", 20, 21));
		LayoutCandidateGroup verticalGroup = onlyGroup(request(Arrays.asList(20, 21), vertical));
		assertTrue(verticalGroup.getCandidates().isEmpty());
		assertEquals(1, verticalGroup.getMissedRelations());

		SemanticRule singleton = rule("single.run", ShapePrimitive.HORIZONTAL_RUN, allWidths(),
			atom("single.family", 30, 31));
		assertEquals(Collections.emptyList(),
			LayoutCandidateGenerator.generate(request(Collections.singletonList(30), singleton)));
	}

	@Test
	public void projectedFallbackFactsUseOwnedCountOverReviewedCount()
	{
		SemanticRule rule = rule("incomplete.run", ShapePrimitive.HORIZONTAL_RUN, widths(3, 4),
			atom("incomplete.family", 10, 20, 30, 40));

		LayoutCandidateGroup group = onlyGroup(request(Arrays.asList(40, 30, 10), rule));

		assertEquals(Arrays.asList(10, 30, 40), group.getProjectedItemIds());
		assertEquals(2, group.getMissedRelations());
		assertEquals(750, group.getMissedCompleteness());
		assertEquals(4, group.getAtomProjections().get(0).getReviewedMemberCount());
		assertEquals(3, group.getAtomProjections().get(0).size());
	}

	@Test
	public void everyGeneratedCollectionIsImmutable()
	{
		SemanticRule rule = rule("immutable.stage", ShapePrimitive.STAGE_MATRIX, widths(1, 2),
			atom("family.a", 10, 11), atom("family.b", 20, 21));
		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(
			request(Arrays.asList(10, 11, 20, 21), rule));
		LayoutCandidateGroup group = groups.get(0);
		LayoutCandidateGroup.AtomProjection projection = group.getAtomProjections().get(0);

		assertImmutable(groups);
		assertImmutable(group.getAtomProjections());
		assertImmutable(group.getAtomKeys());
		assertImmutable(group.getProjectedItemIds());
		assertImmutable(group.getCandidates());
		assertImmutable(projection.getMemberKeys());
		assertImmutable(projection.getItemIds());
	}

	@Test
	public void equivalentGenerationHasValueEqualityIndependentOfRuleInstances()
	{
		SemanticRule firstRule = rule("equal.stage", ShapePrimitive.STAGE_MATRIX, widths(1, 2, 3),
			atom("family.a", 10, 11), atom("family.b", 20, 21));
		SemanticRule secondRule = rule("equal.stage", ShapePrimitive.STAGE_MATRIX, widths(3, 2, 1),
			atom("family.a", 10, 11), atom("family.b", 20, 21));

		LayoutCandidateGroup first = onlyGroup(request(Arrays.asList(10, 11, 20, 21), firstRule));
		LayoutCandidateGroup second = onlyGroup(request(Arrays.asList(21, 20, 11, 10), secondRule));
		LayoutCandidateGroup incomplete = onlyGroup(request(Arrays.asList(10, 11, 20), firstRule));

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertNotEquals(first, incomplete);
		assertTrue(first.toString().contains("equal.stage"));
	}

	@Test
	public void groupRejectsCandidateIdentityItemsAndDuplicateWidthsThatDoNotMatch()
	{
		SemanticRule rule = rule("strict.run", ShapePrimitive.HORIZONTAL_RUN, widths(2, 3),
			atom("strict.family", 10, 20));
		LayoutCandidateGroup valid = onlyGroup(request(Arrays.asList(10, 20), rule));
		List<LayoutCandidateGroup.AtomProjection> projections = valid.getAtomProjections();

		assertGroupFails(() -> new LayoutCandidateGroup(rule, projections, Collections.singletonList(
			new LayoutCandidate("other.rule", ShapePrimitive.HORIZONTAL_RUN, 2,
				Collections.singletonList("strict.family"), Collections.singletonList(row(0, 10, 20))))));
		assertGroupFails(() -> new LayoutCandidateGroup(rule, projections, Collections.singletonList(
			new LayoutCandidate("strict.run", ShapePrimitive.HORIZONTAL_RUN, 2,
				Collections.singletonList("strict.family"), Collections.singletonList(row(0, 10, 30))))));
		LayoutCandidate candidate = valid.getCandidates().get(0);
		assertGroupFails(() -> new LayoutCandidateGroup(rule, projections,
			Arrays.asList(candidate, candidate)));
	}

	@Test
	public void groupRejectsNonCanonicalGeometryForEveryPrimitiveAndMissingFeasibleWidths()
	{
		SemanticRule horizontal = rule("strict.horizontal", ShapePrimitive.HORIZONTAL_RUN, widths(2),
			atom("horizontal.family", 10, 20));
		LayoutCandidateGroup horizontalGroup = onlyGroup(request(Arrays.asList(10, 20), horizontal));
		assertGroupFails(() -> new LayoutCandidateGroup(horizontal,
			horizontalGroup.getAtomProjections(), Collections.singletonList(
				new LayoutCandidate("strict.horizontal", ShapePrimitive.HORIZONTAL_RUN, 2,
					Collections.singletonList("horizontal.family"),
					Collections.singletonList(row(0, 20, 10))))));

		SemanticRule vertical = rule("strict.vertical", ShapePrimitive.VERTICAL_RUN, widths(1),
			atom("vertical.family", 30, 31));
		LayoutCandidateGroup verticalGroup = onlyGroup(request(Arrays.asList(30, 31), vertical));
		assertGroupFails(() -> new LayoutCandidateGroup(vertical,
			verticalGroup.getAtomProjections(), Collections.singletonList(
				new LayoutCandidate("strict.vertical", ShapePrimitive.VERTICAL_RUN, 1,
					Collections.singletonList("vertical.family"),
					Arrays.asList(row(0, 31), row(0, 30))))));

		SemanticRule stage = rule("strict.stage", ShapePrimitive.STAGE_MATRIX, widths(2),
			atom("stage.a", 40, 41), atom("stage.b", 50, 51));
		LayoutCandidateGroup stageGroup = onlyGroup(request(Arrays.asList(40, 41, 50, 51), stage));
		assertGroupFails(() -> new LayoutCandidateGroup(stage, stageGroup.getAtomProjections(),
			Collections.singletonList(new LayoutCandidate("strict.stage", ShapePrimitive.STAGE_MATRIX, 2,
				Arrays.asList("stage.a", "stage.b"),
				Arrays.asList(row(0, 40, 41), row(0, 50, 51))))));

		SemanticRule rowGroup = rule("strict.rows", ShapePrimitive.ROW_GROUP_MATRIX, widths(2),
			atom("row.a", 60, 61), atom("row.b", 70, 71));
		LayoutCandidateGroup rowGroupGroup = onlyGroup(
			request(Arrays.asList(60, 61, 70, 71), rowGroup));
		assertGroupFails(() -> new LayoutCandidateGroup(rowGroup,
			rowGroupGroup.getAtomProjections(), Collections.singletonList(
				new LayoutCandidate("strict.rows", ShapePrimitive.ROW_GROUP_MATRIX, 2,
					Arrays.asList("row.a", "row.b"),
					Arrays.asList(row(0, 70, 71), row(0, 60, 61))))));

		SemanticRule multipleWidths = rule("strict.widths", ShapePrimitive.HORIZONTAL_RUN,
			widths(2, 3), atom("width.family", 80, 81));
		LayoutCandidateGroup complete = onlyGroup(request(Arrays.asList(80, 81), multipleWidths));
		assertGroupFails(() -> new LayoutCandidateGroup(multipleWidths,
			complete.getAtomProjections(), Collections.singletonList(complete.getCandidates().get(0))));
	}

	private static LayoutCandidateGroup onlyGroup(LayoutRequest request)
	{
		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(request);
		assertEquals(1, groups.size());
		return groups.get(0);
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive, Set<Integer> allowedWidths,
		SemanticAtom... atoms)
	{
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(Arrays.asList(atoms))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(primitive)
			.allowedWidths(allowedWidths)
			.build();
	}

	private static SemanticAtom atom(String atomKey, int... itemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			members.add(new SemanticAtom.Member("member." + index, itemIds[index]));
		}
		return new SemanticAtom(atomKey, members);
	}

	private static LayoutRequest request(List<Integer> itemIds, SemanticRule rule)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			int itemId = itemIds.get(index);
			entries.add(LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), index));
		}
		return new LayoutRequest(entries, Collections.singletonList(rule));
	}

	private static LayoutCandidate.Row row(int offset, Integer... itemIds)
	{
		return new LayoutCandidate.Row(offset, Arrays.asList(itemIds));
	}

	private static Set<Integer> allWidths()
	{
		return widths(1, 2, 3, 4, 5, 6, 7, 8);
	}

	private static Set<Integer> widths(int... values)
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int value : values)
		{
			result.add(value);
		}
		return result;
	}

	private static void assertGroupFails(Runnable construction)
	{
		try
		{
			construction.run();
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void assertImmutable(List<?> values)
	{
		try
		{
			((List) values).add(null);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}
}

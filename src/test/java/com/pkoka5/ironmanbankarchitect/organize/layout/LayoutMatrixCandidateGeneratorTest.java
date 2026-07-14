package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class LayoutMatrixCandidateGeneratorTest
{
	@Test
	public void completeStageMatrixUsesCanonicalConsecutiveNextFitChunks()
	{
		SemanticRule rule = rule("gem.workflow", ShapePrimitive.STAGE_MATRIX, allWidths(),
			SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED,
			stageAtom("gem.a", 10, 11), stageAtom("gem.b", 20, 21), stageAtom("gem.c", 30, 31),
			stageAtom("gem.d", 40, 41), stageAtom("gem.e", 50, 51));

		LayoutCandidateGroup group = onlyGroup(request(
			Arrays.asList(51, 50, 41, 40, 31, 30, 21, 20, 11, 10), rule));

		assertEquals(Arrays.asList("gem.a", "gem.b", "gem.c", "gem.d", "gem.e"), group.getAtomKeys());
		assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8), candidateWidths(group));
		assertEquals(Arrays.asList(
			row(0, 10, 20, 30), row(0, 11, 21, 31), row(0, 40, 50), row(0, 41, 51)),
			candidate(group, 3).getRows());
		assertEquals(Arrays.asList(
			row(0, 10, 20, 30, 40), row(0, 11, 21, 31, 41), row(0, 50), row(0, 51)),
			candidate(group, 4).getRows());
		assertEquals(Arrays.asList(
			row(0, 10, 20), row(0, 11, 21), row(0, 30, 40), row(0, 31, 41),
			row(0, 50), row(0, 51)), candidate(group, 2).getRows());
		assertEquals(Arrays.asList(10, 11, 20, 21, 30, 31, 40, 41, 50, 51),
			candidate(group, 1).getRowMajorItemIds());
		assertEquals(Arrays.asList(row(0, 10, 20, 30, 40, 50), row(0, 11, 21, 31, 41, 51)),
			candidate(group, 5).getRows());
		assertEquals(candidate(group, 5).getRows(), candidate(group, 8).getRows());
		assertEquals(8, candidate(group, 8).getWidth());
	}

	@Test
	public void stageMatrixGroupsOnlyExactOrderedProjectedSignatures()
	{
		SemanticAtom a = atom("family.a", member("raw", 10), member("middle", 11), member("done", 12));
		SemanticAtom c = atom("family.c", member("raw", 30), member("middle", 31), member("done", 32));
		SemanticAtom b = atom("family.b", member("raw", 20), member("done", 22));
		SemanticAtom d = atom("family.d", member("done", 42), member("raw", 40));
		SemanticRule rule = rule("stage.signatures", ShapePrimitive.STAGE_MATRIX, widths(2), null,
			a, c, b, d);

		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(request(
			Arrays.asList(42, 40, 31, 30, 22, 20, 12, 10), rule));

		assertEquals(3, groups.size());
		assertEquals(Arrays.asList("family.a", "family.b"), groups.get(0).getAtomKeys());
		assertEquals(Arrays.asList("raw", "done"),
			groups.get(0).getAtomProjections().get(0).getMemberKeys());
		assertEquals(Arrays.asList(row(0, 10, 20), row(0, 12, 22)),
			candidate(groups.get(0), 2).getRows());
		assertEquals(2, groups.get(0).getMissedRelations());
		assertEquals(1666, groups.get(0).getMissedCompleteness());

		assertEquals(Collections.singletonList("family.c"), groups.get(1).getAtomKeys());
		assertEquals(Arrays.asList(row(0, 30), row(0, 31)), candidate(groups.get(1), 2).getRows());
		assertEquals(Collections.singletonList("family.d"), groups.get(2).getAtomKeys());
		assertEquals(Arrays.asList("done", "raw"),
			groups.get(2).getAtomProjections().get(0).getMemberKeys());
		assertEquals(Arrays.asList(row(0, 42), row(0, 40)), candidate(groups.get(2), 2).getRows());
	}

	@Test
	public void rowGroupMatrixKeepsEveryEligibleExplicitRowWithoutWrapping()
	{
		SemanticRule rule = rule("rune.rows", ShapePrimitive.ROW_GROUP_MATRIX, allWidths(), null,
			atom("row.one", member("a", 1), member("b", 2), member("c", 3)),
			atom("row.two", member("a", 4), member("b", 5)),
			atom("row.three", member("a", 6), member("b", 7), member("c", 8), member("d", 9),
				member("e", 10)),
			atom("row.singleton", member("a", 11), member("b", 12)));

		LayoutCandidateGroup group = onlyGroup(request(
			Arrays.asList(12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1), rule));

		assertEquals(Arrays.asList("row.one", "row.two", "row.three", "row.singleton"),
			group.getAtomKeys());
		assertEquals(Arrays.asList(5, 6, 7, 8), candidateWidths(group));
		assertEquals(Arrays.asList(
			row(0, 1, 2, 3), row(0, 4, 5), row(0, 6, 7, 8, 9, 10), row(0, 11, 12)),
			candidate(group, 5).getRows());
		assertEquals(8, group.getMissedRelations());
		assertEquals(4000, group.getMissedCompleteness());
	}

	@Test
	public void incompleteAndSingletonRowsAreCompactedButBoundariesRemainExplicit()
	{
		SemanticRule rule = rule("workflow.rows", ShapePrimitive.ROW_GROUP_MATRIX, widths(2, 3), null,
			atom("row.first", member("a", 1), member("b", 2), member("c", 3), member("d", 4)),
			atom("row.singleton", member("a", 5), member("b", 6), member("c", 7)),
			atom("row.last", member("a", 8), member("b", 9)));

		LayoutCandidateGroup group = onlyGroup(request(Arrays.asList(9, 8, 5, 4, 3, 1), rule));

		assertEquals(Arrays.asList("row.first", "row.last"), group.getAtomKeys());
		assertEquals(Collections.singletonList(3), candidateWidths(group));
		assertEquals(Arrays.asList(row(0, 1, 3, 4), row(0, 8, 9)), candidate(group, 3).getRows());
		assertEquals(3, group.getMissedRelations());
		assertEquals(1750, group.getMissedCompleteness());
	}

	@Test
	public void eligibleNineItemRowSurvivesAsCandidateLessFallbackGroup()
	{
		SemanticRule rule = rule("long.rows", ShapePrimitive.ROW_GROUP_MATRIX, allWidths(), null,
			atom("row.long", member("m1", 1), member("m2", 2), member("m3", 3), member("m4", 4),
				member("m5", 5), member("m6", 6), member("m7", 7), member("m8", 8), member("m9", 9)));

		LayoutCandidateGroup group = onlyGroup(request(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), rule));

		assertEquals(Collections.singletonList("row.long"), group.getAtomKeys());
		assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), group.getProjectedItemIds());
		assertEquals(8, group.getMissedRelations());
		assertEquals(1000, group.getMissedCompleteness());
		assertEquals(Collections.emptyList(), group.getCandidates());
	}

	@Test
	public void noEligibleMatrixAtomProducesNoGroup()
	{
		SemanticRule stage = rule("a.stage", ShapePrimitive.STAGE_MATRIX, allWidths(), null,
			stageAtom("stage.none", 10, 11), stageAtom("stage.single", 20, 21));
		SemanticRule rows = rule("b.rows", ShapePrimitive.ROW_GROUP_MATRIX, allWidths(), null,
			stageAtom("row.none", 30, 31), stageAtom("row.single", 40, 41));

		assertEquals(Collections.emptyList(), LayoutCandidateGenerator.generate(
			request(Arrays.asList(20, 40, 999), rows, stage)));
	}

	@Test
	public void mixedRuleAndEntryOrderCannotChangeExactGroups()
	{
		SemanticRule stage = rule("z.stage", ShapePrimitive.STAGE_MATRIX, widths(2, 3), null,
			stageAtom("stage.a", 10, 11), stageAtom("stage.b", 20, 21));
		SemanticRule run = rule("a.run", ShapePrimitive.HORIZONTAL_RUN, widths(2, 3), null,
			stageAtom("run.a", 30, 31));
		SemanticRule rows = rule("m.rows", ShapePrimitive.ROW_GROUP_MATRIX, widths(2, 3), null,
			stageAtom("row.a", 40, 41));
		List<Integer> itemIds = Arrays.asList(41, 40, 31, 30, 21, 20, 11, 10);

		List<LayoutCandidateGroup> forward = LayoutCandidateGenerator.generate(
			request(itemIds, stage, run, rows));
		List<Integer> reversedItemIds = new ArrayList<>(itemIds);
		Collections.reverse(reversedItemIds);
		List<LayoutCandidateGroup> reversedEntries = LayoutCandidateGenerator.generate(
			request(reversedItemIds, stage, run, rows));
		List<LayoutCandidateGroup> reversedRules = LayoutCandidateGenerator.generate(
			request(itemIds, rows, run, stage));

		assertEquals(forward, reversedEntries);
		assertEquals(forward, reversedRules);
		assertEquals(Arrays.asList("a.run", "m.rows", "z.stage"), ruleKeys(forward));
	}

	private static LayoutCandidateGroup onlyGroup(LayoutRequest request)
	{
		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(request);
		assertEquals(1, groups.size());
		return groups.get(0);
	}

	private static LayoutCandidate candidate(LayoutCandidateGroup group, int width)
	{
		for (LayoutCandidate candidate : group.getCandidates())
		{
			if (candidate.getWidth() == width)
			{
				return candidate;
			}
		}
		throw new AssertionError("missing width " + width);
	}

	private static List<Integer> candidateWidths(LayoutCandidateGroup group)
	{
		List<Integer> result = new ArrayList<>();
		for (LayoutCandidate candidate : group.getCandidates())
		{
			result.add(candidate.getWidth());
		}
		return result;
	}

	private static List<String> ruleKeys(List<LayoutCandidateGroup> groups)
	{
		List<String> result = new ArrayList<>();
		for (LayoutCandidateGroup group : groups)
		{
			result.add(group.getRuleKey());
		}
		return result;
	}

	private static SemanticRule rule(String ruleKey, ShapePrimitive primitive, Set<Integer> allowedWidths,
		WidthEvidence evidence, SemanticAtom... atoms)
	{
		SemanticRule.Builder builder = SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(Arrays.asList(atoms))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(primitive)
			.allowedWidths(allowedWidths);
		if (evidence != null)
		{
			builder.widthEvidence(evidence);
		}
		return builder.build();
	}

	private static SemanticAtom stageAtom(String atomKey, int rawItemId, int processedItemId)
	{
		return atom(atomKey, member("raw", rawItemId), member("processed", processedItemId));
	}

	private static SemanticAtom atom(String atomKey, SemanticAtom.Member... members)
	{
		return new SemanticAtom(atomKey, Arrays.asList(members));
	}

	private static SemanticAtom.Member member(String memberKey, int itemId)
	{
		return new SemanticAtom.Member(memberKey, itemId);
	}

	private static LayoutRequest request(List<Integer> itemIds, SemanticRule... rules)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			int itemId = itemIds.get(index);
			entries.add(LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), index));
		}
		return new LayoutRequest(entries, Arrays.asList(rules));
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
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class LayoutCandidateGeneratorTest
{
	@Test
	public void completeHorizontalRunEnumeratesEveryStructurallyValidWidth()
	{
		SemanticRule rule = rule("potion.doses", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3, 4, 5, 6, 7, 8),
			atom("potion.prayer", 40, 30, 20, 10));

		List<LayoutCandidate> actual = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(10, 20, 30, 40), rule));
		List<LayoutCandidate> expected = new ArrayList<>();
		for (int width = 4; width <= 8; width++)
		{
			expected.add(candidate("potion.doses", ShapePrimitive.HORIZONTAL_RUN, width,
				"potion.prayer", row(0, 40, 30, 20, 10)));
		}

		assertEquals(expected, actual);
	}

	@Test
	public void incompleteHorizontalProjectionIsCompactAndKeepsReviewedOrder()
	{
		SemanticRule rule = rule("rune.family", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3, 4),
			atom("rune.elemental", 10, 20, 30, 40));

		List<LayoutCandidate> missingMiddle = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(999, 40, 10, 30), rule));
		assertEquals(Arrays.asList(
			candidate("rune.family", ShapePrimitive.HORIZONTAL_RUN, 3, "rune.elemental", row(0, 10, 30, 40)),
			candidate("rune.family", ShapePrimitive.HORIZONTAL_RUN, 4, "rune.elemental", row(0, 10, 30, 40))),
			missingMiddle);

		List<LayoutCandidate> missingFirstAndMiddle = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(40, 20), rule));
		assertEquals(Arrays.asList(
			candidate("rune.family", ShapePrimitive.HORIZONTAL_RUN, 2, "rune.elemental", row(0, 20, 40)),
			candidate("rune.family", ShapePrimitive.HORIZONTAL_RUN, 3, "rune.elemental", row(0, 20, 40)),
			candidate("rune.family", ShapePrimitive.HORIZONTAL_RUN, 4, "rune.elemental", row(0, 20, 40))),
			missingFirstAndMiddle);
	}

	@Test
	public void allowedAndPreferredWidthsDoNotChangeCanonicalWidthOrder()
	{
		SemanticRule rule = SemanticRule.builder()
			.ruleKey("herb.workflow")
			.atoms(Collections.singletonList(atom("herb.guam", 10, 20, 30)))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(widths(5, 1, 3, 4))
			.widthEvidence(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED)
			.build();

		List<LayoutCandidate> candidates = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(30, 20, 10), rule));

		assertEquals(Arrays.asList(3, 4, 5), candidateWidths(candidates));
		for (LayoutCandidate candidate : candidates)
		{
			assertEquals(Arrays.asList(10, 20, 30), candidate.getRowMajorItemIds());
		}
	}

	@Test
	public void verticalRunUsesOnlyWidthOneAndOneOwnedItemPerRow()
	{
		SemanticRule rule = rule("charge.family", ShapePrimitive.VERTICAL_RUN, widths(8, 4, 1, 2),
			atom("charge.glory", 10, 20, 30, 40));

		List<LayoutCandidate> actual = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(40, 10, 30), rule));

		assertEquals(Collections.singletonList(candidate("charge.family", ShapePrimitive.VERTICAL_RUN, 1,
			"charge.glory", row(0, 10), row(0, 30), row(0, 40))), actual);

		SemanticRule withoutWidthOne = rule("charge.other", ShapePrimitive.VERTICAL_RUN, widths(2, 4, 8),
			atom("charge.ring", 50, 60));
		assertTrue(LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(50, 60), withoutWidthOne)).isEmpty());
	}

	@Test
	public void absentAndSingletonAtomsDoNotCreateCandidates()
	{
		SemanticRule rule = rule("tool.families", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3),
			atom("tool.absent", 10, 20), atom("tool.singleton", 30, 40), atom("tool.present", 50, 60));

		List<LayoutCandidate> candidates = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(999, 30, 60, 50), rule));

		assertEquals(Arrays.asList(
			candidate("tool.families", ShapePrimitive.HORIZONTAL_RUN, 2, "tool.present", row(0, 50, 60)),
			candidate("tool.families", ShapePrimitive.HORIZONTAL_RUN, 3, "tool.present", row(0, 50, 60))),
			candidates);
	}

	@Test
	public void ruleKeysAreCanonicalWhileAtomOrderRemainsReviewedOrder()
	{
		SemanticRule laterRule = rule("z.rule", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3, 4),
			atom("family.z", 10, 11, 12), atom("family.a", 20, 21));
		SemanticRule earlierRule = rule("a.rule", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3, 4),
			atom("family.only", 30, 31));
		List<Integer> itemIds = Arrays.asList(31, 21, 12, 30, 11, 20, 10);

		List<LayoutCandidate> expected = Arrays.asList(
			candidate("a.rule", ShapePrimitive.HORIZONTAL_RUN, 2, "family.only", row(0, 30, 31)),
			candidate("a.rule", ShapePrimitive.HORIZONTAL_RUN, 3, "family.only", row(0, 30, 31)),
			candidate("a.rule", ShapePrimitive.HORIZONTAL_RUN, 4, "family.only", row(0, 30, 31)),
			candidate("z.rule", ShapePrimitive.HORIZONTAL_RUN, 3, "family.z", row(0, 10, 11, 12)),
			candidate("z.rule", ShapePrimitive.HORIZONTAL_RUN, 4, "family.z", row(0, 10, 11, 12)),
			candidate("z.rule", ShapePrimitive.HORIZONTAL_RUN, 2, "family.a", row(0, 20, 21)),
			candidate("z.rule", ShapePrimitive.HORIZONTAL_RUN, 3, "family.a", row(0, 20, 21)),
			candidate("z.rule", ShapePrimitive.HORIZONTAL_RUN, 4, "family.a", row(0, 20, 21)));
		List<LayoutCandidate> forward = LayoutCandidateGenerator.generateRunCandidates(
			request(itemIds, laterRule, earlierRule));
		List<Integer> reversedItemIds = new ArrayList<>(itemIds);
		Collections.reverse(reversedItemIds);
		List<LayoutCandidate> reversedEntries = LayoutCandidateGenerator.generateRunCandidates(
			request(reversedItemIds, laterRule, earlierRule));
		List<LayoutCandidate> reversedRules = LayoutCandidateGenerator.generateRunCandidates(
			request(itemIds, earlierRule, laterRule));

		assertEquals(expected, forward);
		assertEquals(expected, reversedEntries);
		assertEquals(expected, reversedRules);
	}

	@Test
	public void matrixRulesAreDeferredWhileRunRulesStillGenerate()
	{
		SemanticRule stageMatrix = rule("a.stage", ShapePrimitive.STAGE_MATRIX, widths(1, 2, 3),
			atom("stage.family", 30, 40));
		SemanticRule rowMatrix = rule("b.rows", ShapePrimitive.ROW_GROUP_MATRIX, widths(1, 2, 3),
			atom("row.family", 50, 60));
		SemanticRule run = rule("c.run", ShapePrimitive.HORIZONTAL_RUN, widths(1, 2, 3),
			atom("run.family", 10, 20));

		List<LayoutCandidate> candidates = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(60, 50, 40, 30, 20, 10), rowMatrix, run, stageMatrix));

		assertEquals(Arrays.asList(
			candidate("c.run", ShapePrimitive.HORIZONTAL_RUN, 2, "run.family", row(0, 10, 20)),
			candidate("c.run", ShapePrimitive.HORIZONTAL_RUN, 3, "run.family", row(0, 10, 20))),
			candidates);
		assertTrue(LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(30, 40), stageMatrix)).isEmpty());
	}

	@Test
	public void invalidRequestIsRejectedBeforeAnyPartialGeneration()
	{
		SemanticRule rule = rule("duplicate.rule", ShapePrimitive.HORIZONTAL_RUN, widths(2, 3),
			atom("duplicate.family", 10, 20));
		List<LayoutEntry> entries = Arrays.asList(entry(10, 0), entry(20, 1), entry(10, 2));
		List<LayoutEntry> reversed = new ArrayList<>(entries);
		Collections.reverse(reversed);

		LayoutRequest forward = new LayoutRequest(entries, Collections.singletonList(rule));
		LayoutRequest backward = new LayoutRequest(reversed, Collections.singletonList(rule));
		List<LayoutConflict> forwardConflicts = LayoutRequestValidator.validate(forward);
		List<LayoutConflict> backwardConflicts = LayoutRequestValidator.validate(backward);

		assertEquals(forwardConflicts, backwardConflicts);
		assertEquals(1, forwardConflicts.size());
		assertEquals(LayoutConflict.Type.DUPLICATE_ITEM_ID, forwardConflicts.get(0).getType());
		assertGenerationFails(forward);
		assertGenerationFails(backward);

		LayoutRequest filler = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(20, 1), entry(ItemID.BANK_FILLER, 2)),
			Collections.singletonList(rule));
		assertEquals(LayoutConflict.Type.BANK_FILLER_ITEM,
			LayoutRequestValidator.validate(filler).get(0).getType());
		assertGenerationFails(filler);
	}

	@Test
	public void validPlaceholderAndLocksDoNotAlterLocalGeometry()
	{
		CatalogItem catalogItem = new CatalogItem(10, "Placeholder item", ItemCategory.UNKNOWN, "unknown",
			Collections.emptySet(), null);
		LayoutEntry placeholder = LayoutEntry.of(new BankPreviewItem(catalogItem, 0, true), 7).withLockedTarget(1);
		LayoutEntry regular = entry(20, 3).withLockedTarget(0);
		SemanticRule rule = rule("placeholder.rule", ShapePrimitive.HORIZONTAL_RUN, widths(2),
			atom("placeholder.family", 10, 20));

		List<LayoutCandidate> candidates = LayoutCandidateGenerator.generateRunCandidates(
			new LayoutRequest(Arrays.asList(regular, placeholder), Collections.singletonList(rule)));

		assertEquals(Collections.singletonList(candidate("placeholder.rule", ShapePrimitive.HORIZONTAL_RUN, 2,
			"placeholder.family", row(0, 10, 20))), candidates);
	}

	@Test
	public void horizontalRunsNeverWrapAndVerticalHeightMayExceedEight()
	{
		int[] nineIds = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		SemanticRule horizontal = rule("long.horizontal", ShapePrimitive.HORIZONTAL_RUN,
			widths(1, 2, 3, 4, 5, 6, 7, 8), atom("long.family", nineIds));
		assertTrue(LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), horizontal)).isEmpty());

		List<LayoutCandidate> projectedEight = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8), horizontal));
		assertEquals(Collections.singletonList(candidate("long.horizontal", ShapePrimitive.HORIZONTAL_RUN, 8,
			"long.family", row(0, 1, 2, 3, 4, 5, 6, 7, 8))), projectedEight);

		SemanticRule vertical = rule("long.vertical", ShapePrimitive.VERTICAL_RUN, widths(1, 8),
			atom("long.family", nineIds));
		List<LayoutCandidate> verticalCandidates = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), vertical));
		assertEquals(1, verticalCandidates.size());
		assertEquals(9, verticalCandidates.get(0).getRows().size());
		assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9),
			verticalCandidates.get(0).getRowMajorItemIds());
	}

	@Test
	public void resultListIsImmutable()
	{
		SemanticRule rule = rule("immutable.rule", ShapePrimitive.HORIZONTAL_RUN, widths(2),
			atom("immutable.family", 10, 20));
		List<LayoutCandidate> candidates = LayoutCandidateGenerator.generateRunCandidates(
			request(Arrays.asList(10, 20), rule));

		try
		{
			candidates.add(null);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	@Test
	public void nullRequestIsRejected()
	{
		try
		{
			LayoutCandidateGenerator.generateRunCandidates(null);
			fail("expected NullPointerException");
		}
		catch (NullPointerException expected)
		{
			// expected
		}
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

	private static Set<Integer> widths(int... values)
	{
		Set<Integer> widths = new LinkedHashSet<>();
		for (int value : values)
		{
			widths.add(value);
		}
		return widths;
	}

	private static LayoutRequest request(List<Integer> itemIds, SemanticRule... rules)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			entries.add(entry(itemIds.get(index), index));
		}
		return new LayoutRequest(entries, Arrays.asList(rules));
	}

	private static LayoutEntry entry(int itemId, int sourceFlatBankSlot)
	{
		return LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), sourceFlatBankSlot);
	}

	private static LayoutCandidate candidate(String ruleKey, ShapePrimitive primitive, int width,
		String atomKey, LayoutCandidate.Row... rows)
	{
		return new LayoutCandidate(ruleKey, primitive, width, Collections.singletonList(atomKey),
			Arrays.asList(rows));
	}

	private static LayoutCandidate.Row row(int offset, Integer... itemIds)
	{
		return new LayoutCandidate.Row(offset, Arrays.asList(itemIds));
	}

	private static List<Integer> candidateWidths(List<LayoutCandidate> candidates)
	{
		List<Integer> widths = new ArrayList<>();
		for (LayoutCandidate candidate : candidates)
		{
			widths.add(candidate.getWidth());
		}
		return widths;
	}

	private static void assertGenerationFails(LayoutRequest request)
	{
		try
		{
			LayoutCandidateGenerator.generateRunCandidates(request);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class CompleteLayoutGeometryValidatorTest
{
	@Test
	public void shiftedWindowReconstructsPhysicalBlockIntoLocalDenseTargets()
	{
		LayoutRequest request = request(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
			.withGridStartColumn(2);
		PlacedBlock block = block("shifted", "shifted.atom", 7, 1, 0,
			1, 2, 3, 4, 5, 6, 7);
		int[] semantic = {0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7};
		int[] complete = {8, 9, 10, 11, 12, 13, 1, 2, 3, 4, 5, 6, 7};

		assertTrue(CompleteLayoutGeometryValidator.validate(request,
			Collections.singletonList(block), semantic, complete).isEmpty());
	}

	@Test
	public void shiftedWindowRejectsNominalRectangleBeforeItsPhysicalStart()
	{
		LayoutRequest request = request(1, 2, 3, 4, 5).withGridStartColumn(3);
		PlacedBlock block = block("before", "before.atom", 2, 0, 2, 1, 2);

		assertGeometryConflict(request, Collections.singletonList(block),
			new int[]{0, 0, 0, 0, 0}, new int[]{1, 2, 3, 4, 5});
	}

	@Test
	public void shiftedWindowRejectsNominalRectangleBeyondItsPhysicalTail()
	{
		LayoutRequest request = request(1, 2, 3, 4, 5).withGridStartColumn(3);
		PlacedBlock block = block("after", "after.atom", 2, 1, 0, 1, 2);

		assertGeometryConflict(request, Collections.singletonList(block),
			new int[]{0, 0, 0, 0, 0}, new int[]{1, 2, 3, 4, 5});
	}

	@Test
	public void reconstructsMeaningfulTargetsAndAllowsAnotherBlockInsideNominalSlack()
	{
		LayoutRequest request = request(1, 2, 3, 4, 5);
		PlacedBlock wide = block("a.wide", "wide.atom", 5, 0, 0, 1, 2, 3);
		PlacedBlock spill = block("b.spill", "spill.atom", 2, 0, 3, 4, 5);
		int[] semantic = {1, 2, 3, 4, 5};
		int[] complete = {1, 2, 3, 4, 5};

		assertTrue(CompleteLayoutGeometryValidator.validate(request,
			Arrays.asList(spill, wide), semantic, complete).isEmpty());
	}

	@Test
	public void rejectsFinalTargetThatDisagreesWithBlockGeometry()
	{
		LayoutRequest request = request(1, 2);
		PlacedBlock block = block("pair", "pair.atom", 2, 0, 0, 1, 2);

		assertGeometryConflict(request, Collections.singletonList(block),
			new int[]{1, 2}, new int[]{2, 1});
	}

	@Test
	public void rejectsSemanticStateCellWithoutAPlacedBlockFact()
	{
		LayoutRequest request = request(1, 2);

		assertGeometryConflict(request, Collections.emptyList(),
			new int[]{1, 0}, new int[]{1, 2});
	}

	@Test
	public void rejectsPlacedBlockCellMissingFromSemanticState()
	{
		LayoutRequest request = request(1, 2);
		PlacedBlock block = block("pair", "pair.atom", 2, 0, 0, 1, 2);

		assertGeometryConflict(request, Collections.singletonList(block),
			new int[]{0, 0}, new int[]{1, 2});
	}

	@Test
	public void rejectsGeometryVectorsWithTheWrongDenseLength()
	{
		LayoutRequest request = request(1, 2);

		assertGeometryConflict(request, Collections.emptyList(),
			new int[]{0}, new int[]{1, 2});
	}

	@Test
	public void rejectsDuplicateSemanticItemAndKeepsConflictOrderInputIndependent()
	{
		LayoutRequest request = request(1, 2, 3);
		PlacedBlock first = block("a.first", "first.atom", 2, 0, 0, 1, 2);
		PlacedBlock duplicate = block("b.duplicate", "duplicate.atom", 2, 0, 1, 1, 3);
		int[] semantic = {1, 1, 3};
		int[] complete = {1, 2, 3};

		List<LayoutConflict> forward = CompleteLayoutGeometryValidator.validate(request,
			Arrays.asList(first, duplicate), semantic, complete);
		List<LayoutConflict> reverse = CompleteLayoutGeometryValidator.validate(request,
			Arrays.asList(duplicate, first), semantic, complete);

		assertEquals(forward, reverse);
		assertTrue(forward.toString(), forward.stream()
			.anyMatch(conflict -> conflict.getDetail().contains("more than one placed block")));
	}

	@Test
	public void rejectsTwoBlocksClaimingTheSameMeaningfulTarget()
	{
		LayoutRequest request = request(1, 2, 3, 4);
		PlacedBlock first = block("a.first", "first.atom", 2, 0, 0, 1, 2);
		PlacedBlock second = block("b.second", "second.atom", 2, 0, 0, 3, 4);
		int[] semantic = {1, 2, 0, 0};
		int[] complete = {1, 2, 3, 4};

		List<LayoutConflict> forward = CompleteLayoutGeometryValidator.validate(request,
			Arrays.asList(first, second), semantic, complete);
		List<LayoutConflict> reverse = CompleteLayoutGeometryValidator.validate(request,
			Arrays.asList(second, first), semantic, complete);
		assertEquals(forward, reverse);
		assertTrue(forward.toString(), forward.stream()
			.anyMatch(conflict -> conflict.getDetail().contains("is claimed by items")));
	}

	@Test
	public void rejectsNominalRectangleThatExtendsIntoTheTail()
	{
		LayoutRequest request = request(1, 2, 3);
		PlacedBlock block = block("tail", "tail.atom", 4, 0, 0, 1, 2);

		assertGeometryConflict(request, Collections.singletonList(block),
			new int[]{1, 2, 0}, new int[]{1, 2, 3});
	}

	private static void assertGeometryConflict(LayoutRequest request, List<PlacedBlock> blocks,
		int[] semantic, int[] complete)
	{
		List<LayoutConflict> conflicts = CompleteLayoutGeometryValidator.validate(
			request, blocks, semantic, complete);
		assertTrue(conflicts.toString(), !conflicts.isEmpty());
		for (LayoutConflict conflict : conflicts)
		{
			assertEquals(LayoutConflict.Type.PLAN_SEMANTIC_GEOMETRY_MISMATCH,
				conflict.getType());
		}
	}

	private static PlacedBlock block(String ruleKey, String atomKey, int width,
		int startRow, int startColumn, Integer... itemIds)
	{
		return LayoutTestFixtures.placedBlock(ruleKey, atomKey, 0, width,
			ShapePrimitive.HORIZONTAL_RUN, startRow, startColumn,
			new LayoutCandidate.Row(0, Arrays.asList(itemIds)));
	}

	private static LayoutRequest request(Integer... itemIds)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			entries.add(LayoutEntry.of(
				new BankPreviewItem(itemIds[index], "Item " + itemIds[index], 1), index));
		}
		return new LayoutRequest(entries, Collections.emptyList());
	}
}

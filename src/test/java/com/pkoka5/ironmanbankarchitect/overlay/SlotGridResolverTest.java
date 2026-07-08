package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SlotGridResolverTest
{
	@Test
	public void emptyInputReturnsEmptyList()
	{
		assertTrue(SlotGridResolver.resolveFirstRow(Collections.emptyList(), 8).isEmpty());
	}

	@Test
	public void nullInputReturnsEmptyList()
	{
		assertTrue(SlotGridResolver.resolveFirstRow(null, 8).isEmpty());
	}

	@Test
	public void zeroMaxCellsReturnsEmptyList()
	{
		List<Rectangle> input = twoRowGrid();
		assertTrue(SlotGridResolver.resolveFirstRow(input, 0).isEmpty());
	}

	@Test
	public void selectsTopRowSortedLeftToRight()
	{
		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(twoRowGrid(), 8);

		assertEquals(8, firstRow.size());
		for (int i = 0; i < 8; i++)
		{
			Rectangle cell = firstRow.get(i);
			assertEquals(i * 36, cell.x);
			assertEquals(10, cell.y);
		}
	}

	@Test
	public void handlesUnsortedInput()
	{
		List<Rectangle> shuffled = new ArrayList<>(twoRowGrid());
		Collections.reverse(shuffled);

		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(shuffled, 8);

		assertEquals(8, firstRow.size());
		assertEquals(0, firstRow.get(0).x);
		assertEquals(7 * 36, firstRow.get(7).x);
	}

	@Test
	public void groupsRowsWithinSmallYTolerance()
	{
		List<Rectangle> nearlyAlignedRow = new ArrayList<>();
		for (int i = 0; i < 8; i++)
		{
			int jitter = i % 2 == 0 ? 0 : 1;
			nearlyAlignedRow.add(new Rectangle(i * 36, 10 + jitter, 32, 32));
		}
		nearlyAlignedRow.add(new Rectangle(0, 50, 32, 32));

		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(nearlyAlignedRow, 8);

		assertEquals(8, firstRow.size());
	}

	@Test
	public void excludesSecondRowFromResult()
	{
		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(twoRowGrid(), 8);

		for (Rectangle cell : firstRow)
		{
			assertEquals(10, cell.y);
		}
	}

	@Test
	public void limitsResultToMaxCells()
	{
		List<Rectangle> tenInRow = new ArrayList<>();
		for (int i = 0; i < 10; i++)
		{
			tenInRow.add(new Rectangle(i * 36, 10, 32, 32));
		}

		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(tenInRow, 8);

		assertEquals(8, firstRow.size());
		assertEquals(7 * 36, firstRow.get(7).x);
	}

	@Test
	public void fewerCandidatesThanMaxReturnsAvailableOnly()
	{
		List<Rectangle> partialRow = Arrays.asList(
			new Rectangle(0, 10, 32, 32),
			new Rectangle(36, 10, 32, 32),
			new Rectangle(72, 10, 32, 32));

		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(partialRow, 8);

		assertEquals(3, firstRow.size());
	}

	private static List<Rectangle> twoRowGrid()
	{
		List<Rectangle> rects = new ArrayList<>();
		for (int row = 0; row < 2; row++)
		{
			for (int col = 0; col < 8; col++)
			{
				rects.add(new Rectangle(col * 36, 10 + (row * 40), 32, 32));
			}
		}

		return rects;
	}
}

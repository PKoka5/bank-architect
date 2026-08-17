package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import org.junit.Test;

public class BankCategoryOverlayTest
{
	@Test
	public void legendPrefersTheFreeCanvasRightOfTheBank()
	{
		Rectangle legend = BankCategoryOverlay.legendBounds(
			new Rectangle(200, 100, 300, 400), 120, 90, 900, 700);

		assertEquals(508, legend.x);
		assertEquals(100, legend.y);
		assertEquals(120, legend.width);
		assertEquals(90, legend.height);
	}

	@Test
	public void legendFallsBackToTheLeftWhenTheRightDoesNotFit()
	{
		Rectangle legend = BankCategoryOverlay.legendBounds(
			new Rectangle(200, 100, 300, 400), 120, 90, 520, 700);

		assertEquals(72, legend.x);
	}

	@Test
	public void legendStaysOnCanvasWhenNeitherSideFits()
	{
		Rectangle grid = new Rectangle(10, 100, 300, 400);
		Rectangle legend = BankCategoryOverlay.legendBounds(grid, 120, 90, 320, 700);

		assertTrue(legend.x >= 0);
		assertTrue(legend.x + legend.width <= 320);
	}

	@Test
	public void legendIsPulledUpWhenItWouldRunOffTheBottom()
	{
		Rectangle legend = BankCategoryOverlay.legendBounds(
			new Rectangle(200, 650, 300, 40), 120, 90, 900, 700);

		assertEquals(610, legend.y);
	}

	@Test
	public void aCanvasSmallerThanTheLegendStillProducesOnScreenCoordinates()
	{
		Rectangle legend = BankCategoryOverlay.legendBounds(
			new Rectangle(0, 0, 50, 50), 120, 90, 60, 60);

		assertEquals(0, legend.x);
		assertEquals(0, legend.y);
	}
}

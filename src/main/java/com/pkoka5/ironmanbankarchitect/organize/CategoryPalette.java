package com.pkoka5.ironmanbankarchitect.organize;

import java.awt.Color;

/**
 * Fixed colours for the ten blueprint destinations, indexed by preset order.
 *
 * <p>The hues are spread around the wheel and kept at a similar lightness so no
 * destination reads as more important than another, and so neighbouring tabs
 * stay distinguishable against the bank's dark background. The palette is fixed
 * rather than generated so a tab keeps the same colour between sessions.</p>
 */
public final class CategoryPalette
{
	private static final Color[] COLORS = {
		new Color(255, 205, 60),   // 1 Frequently Used, Runes & Teleports
		new Color(226, 74, 74),    // 2 Combat Gear
		new Color(94, 200, 120),   // 3 Potions, Food & PvM Supplies
		new Color(120, 200, 205),  // 4 Herblore & Potion Making
		new Color(150, 205, 75),   // 5 Seeds & Farming
		new Color(240, 150, 70),   // 6 Skilling Tools
		new Color(175, 135, 100),  // 7 Raw & Processed Resources
		new Color(190, 120, 225),  // 8 Slayer, Boss Loot & Unique Drops
		new Color(95, 165, 240),   // 9 Clues, Cosmetics & Collection Log
		new Color(150, 150, 150)   // 10 Storage & Cleanup Review
	};

	private CategoryPalette()
	{
	}

	public static int size()
	{
		return COLORS.length;
	}

	/** Opaque colour for a destination, cycling if a preset ever grows past ten. */
	public static Color colorFor(int categoryIndex)
	{
		if (categoryIndex < 0)
		{
			throw new IllegalArgumentException("categoryIndex must not be negative");
		}
		return COLORS[categoryIndex % COLORS.length];
	}

	/** Same colour at the configured overlay opacity, clamped to a sane range. */
	public static Color colorFor(int categoryIndex, int opacityPercent)
	{
		Color base = colorFor(categoryIndex);
		int alpha = Math.max(0, Math.min(100, opacityPercent)) * 255 / 100;
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
	}
}

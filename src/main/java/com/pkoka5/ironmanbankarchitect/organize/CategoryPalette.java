package com.pkoka5.ironmanbankarchitect.organize;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed colours for the ten blueprint destinations, indexed by preset order.
 *
 * <p>A destination keeps its colour when the player reorders the tabs, because
 * the colour is looked up from the destination itself rather than from where it
 * currently sits. Moving one tab therefore recolours nothing else.</p>
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

	/** Where each all-round destination sits before the player reorders anything. */
	private static final Map<String, Integer> DEFAULT_INDEX_BY_KEY = defaultIndexByKey();

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

	/**
	 * Palette slot for a destination currently drawn at {@code positionIndex}.
	 *
	 * <p>A destination of the all-round preset keeps the slot it was defined
	 * with, so its colour survives a reorder. Anything else falls back to its
	 * position, which is what the fixed presets have always used.</p>
	 */
	public static int paletteIndex(String categoryKey, int positionIndex)
	{
		Integer defaultIndex = DEFAULT_INDEX_BY_KEY.get(categoryKey);
		return defaultIndex == null ? positionIndex : defaultIndex;
	}

	/** Opaque colour of a destination drawn at {@code positionIndex}. */
	public static Color colorForCategory(String categoryKey, int positionIndex)
	{
		return colorFor(paletteIndex(categoryKey, positionIndex));
	}

	/** Same colour at the configured overlay opacity. */
	public static Color colorForCategory(String categoryKey, int positionIndex, int opacityPercent)
	{
		return colorFor(paletteIndex(categoryKey, positionIndex), opacityPercent);
	}

	private static Map<String, Integer> defaultIndexByKey()
	{
		Map<String, Integer> byKey = new HashMap<>();
		List<BankCategory> categories = BankPresets.IRONMAN.getCategories();
		for (int index = 0; index < categories.size(); index++)
		{
			byKey.put(categories.get(index).getKey(), index);
		}
		return byKey;
	}
}

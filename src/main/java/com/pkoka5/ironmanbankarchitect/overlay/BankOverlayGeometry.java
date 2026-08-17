package com.pkoka5.ironmanbankarchitect.overlay;

import java.awt.Rectangle;

/**
 * Geometry checks shared by the bank overlays.
 *
 * <p>Widget bounds can be null or collapsed while the bank interface is being
 * built or resized. Every overlay has to reject those before drawing, so the
 * rule lives in one place rather than once per overlay.</p>
 */
final class BankOverlayGeometry
{
	private BankOverlayGeometry()
	{
	}

	static boolean isSafeGeometry(Rectangle bounds)
	{
		return bounds != null && bounds.width > 0 && bounds.height > 0;
	}

	/**
	 * Visible item grid: the item area clipped to its scroll container, or
	 * {@code null} when the two do not overlap.
	 */
	static Rectangle itemViewportBounds(Rectangle itemBounds, Rectangle outerBounds)
	{
		if (!isSafeGeometry(itemBounds))
		{
			return itemBounds;
		}
		if (!isSafeGeometry(outerBounds))
		{
			return new Rectangle(itemBounds);
		}
		Rectangle intersection = itemBounds.intersection(outerBounds);
		return isSafeGeometry(intersection) ? intersection : null;
	}

	static boolean isFullyVisible(Rectangle viewportBounds, Rectangle slotBounds)
	{
		return isSafeGeometry(viewportBounds) && isSafeGeometry(slotBounds)
			&& viewportBounds.contains(slotBounds);
	}
}

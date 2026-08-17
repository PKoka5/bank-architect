package com.pkoka5.ironmanbankarchitect.overlay;

import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Screen space one bank overlay has claimed, so the other can route around it.
 *
 * <p>Both overlays want the free canvas beside the bank, and on a small client
 * window that is the same few hundred pixels. Without a shared claim they draw
 * on top of each other. The destination legend has one sensible position, so it
 * claims its rectangle and the movable guidance panel yields.</p>
 *
 * <p>A claim may be read one frame late, since overlay render order is not
 * fixed. That is harmless: the legend only moves when the bank window does.</p>
 */
public final class BankOverlayReservations
{
	private final AtomicReference<Rectangle> legendBounds = new AtomicReference<>();

	/** Records where the legend is drawn this frame. */
	void reserveLegend(Rectangle bounds)
	{
		legendBounds.set(bounds == null ? null : new Rectangle(bounds));
	}

	/** Releases the claim when the legend is not drawn at all. */
	void clearLegend()
	{
		legendBounds.set(null);
	}

	/** Claimed legend rectangle, or {@code null} when nothing is claimed. */
	Rectangle getLegendBounds()
	{
		return legendBounds.get();
	}
}

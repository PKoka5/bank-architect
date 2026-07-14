package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.Objects;

/**
 * One item of a manually constructed complete plan placed on one dense category-local target
 * index in the eight-column row-major grid.
 *
 * <p>The target index is deliberately not range-checked here: {@link LayoutPlanValidator} reports
 * out-of-range and duplicate targets as typed {@link LayoutConflict}s against the request size.</p>
 */
public final class LayoutPlacement
{
	private final BankPreviewItem item;
	private final int targetIndex;

	public LayoutPlacement(BankPreviewItem item, int targetIndex)
	{
		this.item = Objects.requireNonNull(item, "item");
		this.targetIndex = targetIndex;
	}

	public BankPreviewItem getItem()
	{
		return item;
	}

	public int getTargetIndex()
	{
		return targetIndex;
	}
}

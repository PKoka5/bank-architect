package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.Objects;

/**
 * One layout engine input entry: a real bank item plus the placement context that
 * {@link BankPreviewItem} does not carry.
 *
 * <p>The source flat bank slot never implies a dense category rank: before tab distribution the
 * category items may be spread across Main and several numbered tabs. A rank exists only when it
 * was explicitly proven and supplied. A locked target is a required final category-local target
 * index, not a promise that the item never moves during manual execution.</p>
 *
 * <p>Item content (blank, Bank Filler, non-positive or duplicate IDs) is deliberately not rejected
 * here; {@link LayoutRequestValidator} reports those as typed {@link LayoutConflict}s.</p>
 */
public final class LayoutEntry
{
	private final BankPreviewItem item;
	private final int sourceFlatBankSlot;
	private final boolean hasDenseCategoryRank;
	private final int denseCategoryRank;
	private final boolean hasLockedTarget;
	private final int lockedTarget;

	private LayoutEntry(BankPreviewItem item, int sourceFlatBankSlot, boolean hasDenseCategoryRank,
		int denseCategoryRank, boolean hasLockedTarget, int lockedTarget)
	{
		this.item = Objects.requireNonNull(item, "item");
		this.sourceFlatBankSlot = sourceFlatBankSlot;
		this.hasDenseCategoryRank = hasDenseCategoryRank;
		this.denseCategoryRank = denseCategoryRank;
		this.hasLockedTarget = hasLockedTarget;
		this.lockedTarget = lockedTarget;
	}

	public static LayoutEntry of(BankPreviewItem item, int sourceFlatBankSlot)
	{
		if (sourceFlatBankSlot < 0)
		{
			throw new IllegalArgumentException("sourceFlatBankSlot must not be negative");
		}

		return new LayoutEntry(item, sourceFlatBankSlot, false, 0, false, 0);
	}

	/**
	 * Returns a copy carrying a proven dense category-local rank. Range validity against the
	 * request size is checked by {@link LayoutRequestValidator}, not here.
	 */
	public LayoutEntry withDenseCategoryRank(int rank)
	{
		return new LayoutEntry(item, sourceFlatBankSlot, true, rank, hasLockedTarget, lockedTarget);
	}

	/**
	 * Returns a copy carrying a required final target index. Range validity against the request
	 * size is checked by {@link LayoutRequestValidator}, not here.
	 */
	public LayoutEntry withLockedTarget(int target)
	{
		return new LayoutEntry(item, sourceFlatBankSlot, hasDenseCategoryRank, denseCategoryRank, true, target);
	}

	public BankPreviewItem getItem()
	{
		return item;
	}

	public int getSourceFlatBankSlot()
	{
		return sourceFlatBankSlot;
	}

	public boolean hasDenseCategoryRank()
	{
		return hasDenseCategoryRank;
	}

	public int getDenseCategoryRank()
	{
		if (!hasDenseCategoryRank)
		{
			throw new IllegalStateException("entry has no proven dense category rank");
		}

		return denseCategoryRank;
	}

	public boolean hasLockedTarget()
	{
		return hasLockedTarget;
	}

	public int getLockedTarget()
	{
		if (!hasLockedTarget)
		{
			throw new IllegalStateException("entry has no locked target");
		}

		return lockedTarget;
	}
}

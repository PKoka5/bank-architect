package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.*;

/**
 * Bank item plus placement context. Flat source slots do not imply a dense category rank; ranks
 * require explicit proof. Locks specify final category-local indices, not immobility during manual
 * moves. LayoutRequestValidator reports invalid item content as typed conflicts.
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

	static Set<Integer> presentIds(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries) present.add(entry.item.getItemId());
		return present;
	}

	static List<Integer> ownedIds(List<Integer> canonicalIds, Set<Integer> present)
	{
		List<Integer> owned = new ArrayList<>();
		for (Integer id : canonicalIds) if (present.contains(id)) owned.add(id);
		return owned;
	}

	static List<LayoutEntry> withTargets(List<LayoutEntry> entries, Map<Integer, Integer> targets)
	{
		List<LayoutEntry> anchored = new ArrayList<>(entries.size());
		for (LayoutEntry entry : entries)
		{
			Integer target = targets.get(entry.item.getItemId());
			anchored.add(target == null ? entry : entry.withLockedTarget(target));
		}
		return Collections.unmodifiableList(anchored);
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

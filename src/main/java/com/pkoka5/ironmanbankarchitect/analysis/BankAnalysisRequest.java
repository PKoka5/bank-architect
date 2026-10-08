package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;





/** All player and bank facts used by one coherent bank analysis. */
public final class BankAnalysisRequest
{
	private final BankSnapshot bankSnapshot;
	private final Map<Integer, GearStats> gearStatsByItemId;
	private final Map<Integer, Integer> alchValuesByItemId;
	private final Map<Integer, String> categoryKeysByItemId;
	private final BankLayoutPlan layoutPlan;
	private final BankLayoutOptions layoutOptions;
	private final BankPreset preset;

	public BankAnalysisRequest(BankSnapshot bankSnapshot,
		Map<Integer, GearStats> gearStatsByItemId,
		Map<Integer, Integer> alchValuesByItemId,
		Map<Integer, String> categoryKeysByItemId,
		BankLayoutPlan layoutPlan,
		BankLayoutOptions layoutOptions)
	{
		this(bankSnapshot, gearStatsByItemId, alchValuesByItemId, categoryKeysByItemId,
			layoutPlan, layoutOptions, null);
	}

	public BankAnalysisRequest(BankSnapshot bankSnapshot, Map<Integer, GearStats> gearStatsByItemId,
		Map<Integer, Integer> alchValuesByItemId, Map<Integer, String> categoryKeysByItemId,
		BankLayoutPlan layoutPlan, BankLayoutOptions layoutOptions, BankPreset preset)
	{
		this.preset = preset;
		this.bankSnapshot = Objects.requireNonNull(bankSnapshot, "bankSnapshot");
		this.gearStatsByItemId = immutableCopy(gearStatsByItemId, "gearStatsByItemId");
		this.alchValuesByItemId = immutableCopy(alchValuesByItemId, "alchValuesByItemId");
		this.categoryKeysByItemId = immutableCopy(categoryKeysByItemId, "categoryKeysByItemId");
		this.layoutPlan = Objects.requireNonNull(layoutPlan, "layoutPlan");
		this.layoutOptions = Objects.requireNonNull(layoutOptions, "layoutOptions");
	}

	public BankSnapshot bankSnapshot()
	{
		return bankSnapshot;
	}

	public BankPreset presetOr(BankPreset fallback) { return preset == null ? fallback : preset; }

	public Optional<GearStats> gearStats(int itemId)
	{
		return Optional.ofNullable(gearStatsByItemId.get(itemId));
	}

	public int alchValue(int itemId)
	{
		Integer value = alchValuesByItemId.get(itemId);
		return value == null ? 0 : value;
	}

	public Optional<String> categoryKey(int itemId)
	{
		return Optional.ofNullable(categoryKeysByItemId.get(itemId));
	}

	public BankLayoutPlan layoutPlan()
	{
		return layoutPlan;
	}

	public BankLayoutOptions layoutOptions()
	{
		return layoutOptions;
	}

	/** Additions and withdrawals preserve routing; changed previous facts or removed slots do not. */
	boolean sameLayoutContext(BankAnalysisRequest other, BankPreset fallback)
	{
		if (other == null || presetOr(fallback) != other.presetOr(fallback)
			|| !layoutPlan.serialize().equals(other.layoutPlan.serialize())
			|| !layoutOptions.equals(other.layoutOptions)
			|| !categoryKeysByItemId.equals(other.categoryKeysByItemId)) return false;
		Map<Integer, List<Integer>> mine = bankSnapshot.contents();
		Map<Integer, List<Integer>> theirs = other.bankSnapshot.contents();
		if (!mine.keySet().containsAll(theirs.keySet())) return false;
		for (int id : theirs.keySet())
			if (mine.get(id).size() != theirs.get(id).size()
				|| !Objects.equals(gearStatsByItemId.get(id), other.gearStatsByItemId.get(id))
				|| !Objects.equals(alchValuesByItemId.get(id), other.alchValuesByItemId.get(id))) return false;
		return true;
	}

	private static <K, V> Map<K, V> immutableCopy(Map<K, V> source, String name)
	{
		return Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(source, name)));
	}
}

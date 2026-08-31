package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** All player and bank facts used by one coherent bank analysis. */
public final class BankAnalysisRequest
{
	private final BankSnapshot bankSnapshot;
	private final Map<Integer, GearStats> gearStatsByItemId;
	private final Map<Integer, Integer> alchValuesByItemId;
	private final Map<Integer, String> categoryKeysByItemId;
	private final BankLayoutPlan layoutPlan;
	private final BankLayoutOptions layoutOptions;

	public BankAnalysisRequest(BankSnapshot bankSnapshot,
		Map<Integer, GearStats> gearStatsByItemId,
		Map<Integer, Integer> alchValuesByItemId,
		Map<Integer, String> categoryKeysByItemId,
		BankLayoutPlan layoutPlan,
		BankLayoutOptions layoutOptions)
	{
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

	private static <K, V> Map<K, V> immutableCopy(Map<K, V> source, String name)
	{
		return Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(source, name)));
	}
}

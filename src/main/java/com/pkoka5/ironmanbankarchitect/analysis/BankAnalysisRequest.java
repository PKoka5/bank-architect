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
	private final Map<Integer, Integer> gePricesByItemId;
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
		this(bankSnapshot, gearStatsByItemId, alchValuesByItemId, Collections.emptyMap(), categoryKeysByItemId,
			layoutPlan, layoutOptions, preset);
	}

	public BankAnalysisRequest(BankSnapshot bankSnapshot, Map<Integer, GearStats> gearStatsByItemId,
		Map<Integer, Integer> alchValuesByItemId, Map<Integer, Integer> gePricesByItemId,
		Map<Integer, String> categoryKeysByItemId, BankLayoutPlan layoutPlan, BankLayoutOptions layoutOptions,
		BankPreset preset)
	{
		this.preset = preset;
		this.bankSnapshot = Objects.requireNonNull(bankSnapshot, "bankSnapshot");
		this.gearStatsByItemId = immutableCopy(gearStatsByItemId, "gearStatsByItemId");
		this.alchValuesByItemId = immutableCopy(alchValuesByItemId, "alchValuesByItemId");
		this.gePricesByItemId = immutableCopy(gePricesByItemId, "gePricesByItemId");
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

	public ItemValueSource itemValues()
	{
		return new ItemValueSource()
		{
			@Override
			public int highAlchValue(int itemId)
			{
				return alchValuesByItemId.getOrDefault(itemId, 0);
			}

			@Override
			public int gePrice(int itemId)
			{
				return gePricesByItemId.getOrDefault(itemId, 0);
			}
		};
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

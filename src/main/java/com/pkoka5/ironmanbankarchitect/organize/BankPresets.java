package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;

public final class BankPresets
{
	private static final Map<BankPresetType, BankPreset> BY_TYPE = load();
	public static final BankPreset IRONMAN = forType(BankPresetType.IRONMAN);
	public static final BankPreset MAIN = forType(BankPresetType.MAIN);
	public static final BankPreset PVM = forType(BankPresetType.PVM);
	public static final BankPreset PVP = forType(BankPresetType.PVP);
	public static final BankPreset SKILLER = forType(BankPresetType.SKILLER);

	private BankPresets() {}

	public static BankPreset forType(BankPresetType type)
	{
		BankPreset preset = BY_TYPE.get(type);
		if (preset == null) throw new IllegalArgumentException("Unknown preset type: " + type);
		return preset;
	}

	private static Map<BankPresetType, BankPreset> load()
	{
		Map<BankPresetType, List<String[]>> groups = new EnumMap<>(BankPresetType.class);
		for (String[] row : BankDefinitionRows.read("bank-presets.tsv", 6))
			groups.computeIfAbsent(BankPresetType.valueOf(row[0]), key -> new ArrayList<>()).add(row);
		Map<BankPresetType, BankPreset> presets = new EnumMap<>(BankPresetType.class);
		for (Map.Entry<BankPresetType, List<String[]>> entry : groups.entrySet())
		{
			String[] first = entry.getValue().get(0);
			List<BankCategory> categories = new ArrayList<>();
			for (String[] row : entry.getValue())
			{
				if (!first[1].equals(row[1]) || !first[2].equals(row[2]))
					throw new IllegalStateException("Inconsistent preset metadata");
				categories.add(new BankCategory(row[3], row[4], BankCategorySortMode.valueOf(row[5])));
			}
			presets.put(entry.getKey(), new BankPreset(entry.getKey(), first[1], first[2], categories));
		}
		if (presets.size() != BankPresetType.values().length) throw new IllegalStateException("Incomplete bank presets");
		return Collections.unmodifiableMap(presets);
	}
}

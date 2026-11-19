package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;


/** Readable, versioned share codes containing a profile name and tag plan. */
public final class BankLayoutShareCode
{
	/** Marks the text as ours and says which format it is in. */
	public static final String PREFIX = "BAv1";

	/** Precedes the share code on the last line of a Blueprint Export. */
	public static final String EXPORT_LABEL = "Share code: ";

	private static final String SEPARATOR = "~";
	private static final int PARTS = 3;
	static final int MAX_NAME_LENGTH = 40;

	private final String name;
	private final String plan;
	private final BankPresetType presetType;
	private final String itemOrders;

	private BankLayoutShareCode(String name, String plan, BankPresetType presetType, String itemOrders)
	{
		this.name = name;
		this.plan = plan;
		this.presetType = presetType;
		this.itemOrders = itemOrders;
	}

	public BankPresetType getPresetType() { return presetType; }

	/** Saved item positions, present only when the sharer chose to include them. */
	public Optional<BlueprintItemOrders> getItemOrders()
	{
		return itemOrders.isEmpty() ? Optional.empty() : Optional.of(BlueprintItemOrders.parse(itemOrders));
	}

	public String getName()
	{
		return name;
	}

	/** The serialized plan, still to be parsed against the preset. */
	public String getPlan()
	{
		return plan;
	}

	public static String encode(String name, BankLayoutPlan plan)
	{
		Objects.requireNonNull(plan, "plan");

		return PREFIX + SEPARATOR + sanitize(name) + SEPARATOR + plan.serialize();
	}

	public static String encode(String name, BankLayoutPlan plan, BankPreset preset)
	{
		return preset.getType() == BankPresetType.IRONMAN ? encode(name, plan)
			: "BAv2~" + preset.getType().name() + "~" + sanitize(name) + "~" + plan.serialize();
	}

	/** Adds saved item positions as a BAv3 code; without any, the code stays BAv1/BAv2. */
	public static String encode(String name, BankLayoutPlan plan, BankPreset preset, BlueprintItemOrders items)
	{
		String orders = items.isSupported() ? items.serialize() : "";
		return orders.isEmpty() ? encode(name, plan, preset)
			: "BAv3~" + preset.getType().name() + "~" + sanitize(name) + "~" + plan.serialize() + "~" + orders;
	}

	/** Ignores surrounding whitespace; rejects missing or unsupported prefixes. */
	public static Optional<BankLayoutShareCode> decode(String text)
	{
		if (text == null)
		{
			return Optional.empty();
		}

		String trimmed = text.trim();
		// A whole Blueprint Export ends with its code; single-line fields turn its newlines into spaces.
		int label = trimmed.lastIndexOf(EXPORT_LABEL);
		if (label >= 0 && !trimmed.regionMatches(true, 0, "BAv", 0, 3))
			trimmed = trimmed.substring(label + EXPORT_LABEL.length()).trim();
		String orders = "";
		if (trimmed.regionMatches(true, 0, "BAv3~", 0, 5))
		{
			// The plan may contain the separator (~keep), but serialized orders never do.
			int split = trimmed.lastIndexOf(SEPARATOR);
			BlueprintItemOrders items = BlueprintItemOrders.parse(trimmed.substring(split + 1).trim());
			orders = items.isSupported() ? items.serialize() : "";
			if (orders.isEmpty()) return Optional.empty();
			trimmed = "BAv2" + trimmed.substring(4, split);
		}
		BankPresetType type = BankPresetType.IRONMAN;
		if (trimmed.regionMatches(true, 0, "BAv2~", 0, 5))
		{
			String[] versioned = trimmed.split(SEPARATOR, 3);
			if (versioned.length != 3) return Optional.empty();
			try { type = BankPresetType.valueOf(versioned[1].trim()); }
			catch (IllegalArgumentException invalid) { return Optional.empty(); }
			trimmed = PREFIX + SEPARATOR + versioned[2];
		}
		String[] parts = trimmed.split(SEPARATOR, PARTS);
		if (parts.length != PARTS || !PREFIX.equalsIgnoreCase(parts[0].trim()))
		{
			return Optional.empty();
		}

		String name = sanitize(parts[1]);
		String plan = parts[2].trim();
		if (plan.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new BankLayoutShareCode(name, plan, type, orders));
	}

	/**
	 * A name fit to store and display: the separator would split the code in the
	 * wrong place, and an unbounded name would push the profile list off the
	 * sidebar. A blank name becomes a usable placeholder rather than nothing.
	 */
	public static String sanitize(String name)
	{
		String cleaned = name == null ? "" : name.replace(SEPARATOR, " ")
			.replace("|", " ").replace("+", " ").replace(";", " ").trim();
		if (cleaned.isEmpty())
		{
			return "Shared layout";
		}

		return cleaned.length() > MAX_NAME_LENGTH
			? cleaned.substring(0, MAX_NAME_LENGTH).trim() : cleaned;
	}
}

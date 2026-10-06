package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;
import java.util.List;



/** Named plans with a permanent bundled default. Saving over the default creates a named copy. Serialized config skips malformed entries without discarding other profiles. */
public final class BankLayoutProfiles
{
	/** The bundled layout, always present and always the preset's own. */
	public static final String DEFAULT_NAME = "Ironman - All-Round";

	private static final String PROFILE_SEPARATOR = ";";
	private static final String FIELD_SEPARATOR = "~";
	private static final int MAX_PROFILES = 20;

	private final Map<String, String> plansByName;
	private final String activeName;
	private final String defaultName;

	private BankLayoutProfiles(Map<String, String> plansByName, String activeName, String defaultName)
	{
		this.defaultName = defaultName;
		Map<String, String> ordered = new LinkedHashMap<>();
		ordered.put(defaultName, "");
		ordered.putAll(plansByName);
		ordered.put(defaultName, "");

		this.plansByName = Collections.unmodifiableMap(ordered);
		this.activeName = ordered.containsKey(activeName) ? activeName : defaultName;
	}

	public static BankLayoutProfiles parse(String serialized, String activeName)
	{
		return parse(serialized, activeName, DEFAULT_NAME);
	}

	public static String defaultName(BankPreset preset)
	{
		return preset.getType() == BankPresetType.MAIN ? "Main - All-Round" : DEFAULT_NAME;
	}

	public String getDefaultName() { return defaultName; }

	public static BankLayoutProfiles parse(String serialized, String activeName, String defaultName)
	{
		Map<String, String> plans = new LinkedHashMap<>();
		if (serialized != null)
		{
			serialized = repairActiveName(serialized, activeName);
			for (String entry : serialized.split(PROFILE_SEPARATOR))
			{
				String[] parts = entry.split(FIELD_SEPARATOR, 2);
				if (parts.length != 2)
				{
					continue;
				}
				String name = BankLayoutShareCode.sanitize(parts[0]);
				if (defaultName.equals(name) || parts[1].trim().isEmpty())
				{
					continue;
				}
				plans.put(name, parts[1].trim());
			}
		}

		return new BankLayoutProfiles(plans, BankLayoutShareCode.sanitize(activeName), defaultName);
	}

	/** Recover a known active name saved before semicolons were excluded. */
	public static String repairActiveName(String serialized, String activeName)
	{
		return serialized != null && activeName != null && activeName.contains(PROFILE_SEPARATOR)
			? (PROFILE_SEPARATOR + serialized).replace(PROFILE_SEPARATOR + activeName + FIELD_SEPARATOR,
				PROFILE_SEPARATOR + BankLayoutShareCode.sanitize(activeName) + FIELD_SEPARATOR).substring(1)
			: serialized;
	}

	/** Every profile name, the bundled one first. */
	public List<String> names()
	{
		return Collections.unmodifiableList(new ArrayList<>(plansByName.keySet()));
	}

	public String getActiveName()
	{
		return activeName;
	}

	/** The stored plan of a profile; blank means the preset's own arrangement. */
	public String planFor(String name)
	{
		String plan = plansByName.get(name);
		return plan == null ? "" : plan;
	}

	public String activePlan()
	{
		return planFor(activeName);
	}

	public boolean isDefaultActive()
	{
		return defaultName.equals(activeName);
	}

	/** The same set with a different profile in use. */
	public BankLayoutProfiles withActive(String name)
	{
		return new BankLayoutProfiles(withoutDefault(), BankLayoutShareCode.sanitize(name), defaultName);
	}

	/**
	 * Stores a plan under a name and makes it the one in use.
	 *
	 * <p>Saving onto the bundled layout is redirected to a copy, because that one
	 * has to keep meaning "the preset's own". A set that is already full drops
	 * nothing: the save is refused rather than silently evicting a layout the
	 * player may have spent time on.</p>
	 */
	public BankLayoutProfiles withProfile(String name, String plan)
	{
		Objects.requireNonNull(plan, "plan");

		String cleaned = BankLayoutShareCode.sanitize(name);
		if (defaultName.equals(cleaned))
		{
			cleaned = cleaned + " (copy)";
		}

		Map<String, String> updated = withoutDefault();
		if (!updated.containsKey(cleaned) && updated.size() >= MAX_PROFILES)
		{
			return this;
		}

		updated.put(cleaned, plan);
		return new BankLayoutProfiles(updated, cleaned, defaultName);
	}

	/** Removes a saved profile; the bundled one cannot be removed. */
	public BankLayoutProfiles without(String name)
	{
		if (defaultName.equals(name))
		{
			return this;
		}

		Map<String, String> updated = withoutDefault();
		if (updated.remove(name) == null)
		{
			return this;
		}

		return new BankLayoutProfiles(updated,
			name.equals(activeName) ? defaultName : activeName, defaultName);
	}

	/** A name not yet taken, so an import never overwrites an existing layout. */
	public String freeName(String wanted)
	{
		return freeName(wanted, names());
	}

	public static String freeName(String wanted, List<String> taken)
	{
		String cleaned = BankLayoutShareCode.sanitize(wanted);
		if (!taken.contains(cleaned))
		{
			return cleaned;
		}

		for (int suffix = 2; ; suffix++)
		{
			String ending = " " + suffix;
			String candidate = cleaned.substring(0, Math.min(cleaned.length(),
				BankLayoutShareCode.MAX_NAME_LENGTH - ending.length())).trim() + ending;
			if (!taken.contains(candidate))
			{
				return candidate;
			}
		}
	}

	public String serialize()
	{
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : withoutDefault().entrySet())
		{
			if (builder.length() > 0)
			{
				builder.append(PROFILE_SEPARATOR);
			}
			builder.append(entry.getKey()).append(FIELD_SEPARATOR).append(entry.getValue());
		}

		return builder.toString();
	}

	private Map<String, String> withoutDefault()
	{
		Map<String, String> copy = new LinkedHashMap<>(plansByName);
		copy.remove(defaultName);
		return copy;
	}
}

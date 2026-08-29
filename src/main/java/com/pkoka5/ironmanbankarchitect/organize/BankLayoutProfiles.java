package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The player's saved tab layouts, one of which is in use.
 *
 * <p>A profile is a named plan. The bundled Ironman layout is always the first
 * one and always the preset's own arrangement, so there is a way back to a
 * working bank no matter what an imported layout turns out to be. It cannot be
 * edited away or renamed; saving over it stores a copy under its own name
 * instead, which keeps "reset" meaning something.</p>
 *
 * <p>Stored as one string because the config holds strings. Parsing is forgiving
 * in the same way the plan's is: a malformed entry is skipped rather than
 * failing the whole set, so one bad import cannot cost the player every layout
 * they had.</p>
 */
public final class BankLayoutProfiles
{
	/** The bundled layout, always present and always the preset's own. */
	public static final String DEFAULT_NAME = "Ironman - All-Round";

	private static final String PROFILE_SEPARATOR = ";";
	private static final String FIELD_SEPARATOR = "~";
	private static final int MAX_PROFILES = 20;

	private final Map<String, String> plansByName;
	private final String activeName;

	private BankLayoutProfiles(Map<String, String> plansByName, String activeName)
	{
		Map<String, String> ordered = new LinkedHashMap<>();
		ordered.put(DEFAULT_NAME, "");
		ordered.putAll(plansByName);
		ordered.put(DEFAULT_NAME, "");

		this.plansByName = Collections.unmodifiableMap(ordered);
		this.activeName = ordered.containsKey(activeName) ? activeName : DEFAULT_NAME;
	}

	public static BankLayoutProfiles parse(String serialized, String activeName)
	{
		Map<String, String> plans = new LinkedHashMap<>();
		if (serialized != null)
		{
			for (String entry : serialized.split(PROFILE_SEPARATOR))
			{
				String[] parts = entry.split(FIELD_SEPARATOR, 2);
				if (parts.length != 2)
				{
					continue;
				}
				String name = BankLayoutShareCode.sanitize(parts[0]);
				if (DEFAULT_NAME.equals(name) || parts[1].trim().isEmpty())
				{
					continue;
				}
				plans.put(name, parts[1].trim());
			}
		}

		return new BankLayoutProfiles(plans, BankLayoutShareCode.sanitize(activeName));
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
		return DEFAULT_NAME.equals(activeName);
	}

	/** The same set with a different profile in use. */
	public BankLayoutProfiles withActive(String name)
	{
		return new BankLayoutProfiles(withoutDefault(), BankLayoutShareCode.sanitize(name));
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
		if (DEFAULT_NAME.equals(cleaned))
		{
			cleaned = cleaned + " (copy)";
		}

		Map<String, String> updated = withoutDefault();
		if (!updated.containsKey(cleaned) && updated.size() >= MAX_PROFILES)
		{
			return this;
		}

		updated.put(cleaned, plan);
		return new BankLayoutProfiles(updated, cleaned);
	}

	/** Removes a saved profile; the bundled one cannot be removed. */
	public BankLayoutProfiles without(String name)
	{
		if (DEFAULT_NAME.equals(name))
		{
			return this;
		}

		Map<String, String> updated = withoutDefault();
		if (updated.remove(name) == null)
		{
			return this;
		}

		return new BankLayoutProfiles(updated,
			name.equals(activeName) ? DEFAULT_NAME : activeName);
	}

	/** A name not yet taken, so an import never overwrites an existing layout. */
	public String freeName(String wanted)
	{
		String cleaned = BankLayoutShareCode.sanitize(wanted);
		if (!plansByName.containsKey(cleaned))
		{
			return cleaned;
		}

		for (int suffix = 2; suffix < MAX_PROFILES + 2; suffix++)
		{
			String candidate = cleaned + " " + suffix;
			if (!plansByName.containsKey(candidate))
			{
				return candidate;
			}
		}

		return cleaned + " " + System.currentTimeMillis();
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
		copy.remove(DEFAULT_NAME);
		return copy;
	}
}

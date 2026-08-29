package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;
import java.util.Optional;

/**
 * The text a player copies to hand their tab layout to someone else.
 *
 * <p>Deliberately readable rather than compact. A share code is pasted into
 * chat and read by a stranger who has no way to debug it, so the tag keys stay
 * in plain sight: a code that has visibly lost a tag can be diagnosed by eye,
 * where an opaque blob can only be thrown away.</p>
 *
 * <p>The version marker lets a later format be recognised instead of guessed
 * at, and the name travels with the layout so the receiver keeps whatever the
 * author called it.</p>
 */
public final class BankLayoutShareCode
{
	/** Marks the text as ours and says which format it is in. */
	public static final String PREFIX = "BAv1";

	private static final String SEPARATOR = "~";
	private static final int PARTS = 3;
	private static final int MAX_NAME_LENGTH = 40;

	private final String name;
	private final String plan;

	private BankLayoutShareCode(String name, String plan)
	{
		this.name = name;
		this.plan = plan;
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

	/**
	 * Reads a pasted code, or empty when the text is not one of ours.
	 *
	 * <p>Chat clients wrap and pad, so surrounding whitespace is ignored. What is
	 * not ignored is the prefix: without it the text is rejected rather than
	 * hopefully parsed, so pasting the wrong thing says so instead of quietly
	 * rearranging the player's bank.</p>
	 */
	public static Optional<BankLayoutShareCode> decode(String text)
	{
		if (text == null)
		{
			return Optional.empty();
		}

		String trimmed = text.trim();
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

		return Optional.of(new BankLayoutShareCode(name, plan));
	}

	/**
	 * A name fit to store and display: the separator would split the code in the
	 * wrong place, and an unbounded name would push the profile list off the
	 * sidebar. A blank name becomes a usable placeholder rather than nothing.
	 */
	public static String sanitize(String name)
	{
		String cleaned = name == null ? "" : name.replace(SEPARATOR, " ")
			.replace("|", " ").replace("+", " ").trim();
		if (cleaned.isEmpty())
		{
			return "Shared layout";
		}

		return cleaned.length() > MAX_NAME_LENGTH
			? cleaned.substring(0, MAX_NAME_LENGTH).trim() : cleaned;
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogUnavailableException;

/** A deliberately accepted local target, separate from personal editing instructions. */
public final class AcceptedBlueprint
{
	// Advance when classification or layout rules change. Older accepted targets remain usable.
	public static final String RULES = "7";
	private AcceptedBlueprint() { }

	public static String capture(BankOrganizationPreview preview, String context)
	{
		return "BAkeep1|" + RULES + "|" + fingerprint(context) + "|"
			+ BlueprintItemOrders.fromPreview(preview).serialize();
	}

	public static BankOrganizationPreview apply(String value, String context,
		BankOrganizationPreview proposal, BankLayoutPlan plan)
	{
		if (value.isEmpty()) return proposal;
		try
		{
			String[] fields = read(value);
			return fields[2].equals(fingerprint(context))
				? BlueprintItemOrders.parse(fields[3]).apply(proposal, plan, id -> true) : proposal;
		}
		catch (IllegalArgumentException invalid)
		{
			throw new CatalogUnavailableException("Saved blueprint is unsupported or damaged. Select another profile to continue.", invalid);
		}
	}

	public static boolean isSupported(String value)
	{
		try { if (!value.isEmpty()) read(value); return true; }
		catch (IllegalArgumentException invalid) { return false; }
	}

	public static String reference(String value)
	{
		if (value.isEmpty()) return "";
		try { return BlueprintItemOrders.parse(read(value)[3]).reference(); }
		catch (IllegalArgumentException invalid) { return "unsupported"; }
	}

	public static String description(String value, String context)
	{
		if (value.isEmpty()) return "Automatic blueprint; no accepted snapshot.";
		if (!isSupported(value)) return "Saved blueprint is unsupported or damaged; stored data is preserved.";
		String[] fields = read(value);
		return "Saved rules " + fields[1] + "; current rules " + RULES + ". "
			+ (fields[2].equals(fingerprint(context)) ? "Saved order and destinations are active."
				: "Personal settings changed. Saved snapshot is paused; current settings take priority.");
	}

	private static String[] read(String value)
	{
		if (value.length() > 160000) throw new IllegalArgumentException("Snapshot too large");
		String[] fields = value.split("\\|", 4);
		if (fields.length != 4 || !"BAkeep1".equals(fields[0]) || !fields[1].matches("[1-9][0-9]{0,5}")
			|| !UUID.fromString(fields[2]).toString().equals(fields[2])) throw new IllegalArgumentException("Unknown snapshot format");
		BlueprintItemOrders orders = BlueprintItemOrders.parse(fields[3]);
		orders.requireSnapshot();
		if (!orders.serialize().equals(fields[3])) throw new IllegalArgumentException("Noncanonical snapshot");
		return fields;
	}

	private static String fingerprint(String context)
	{
		return UUID.nameUUIDFromBytes(context.getBytes(StandardCharsets.UTF_8)).toString();
	}
}

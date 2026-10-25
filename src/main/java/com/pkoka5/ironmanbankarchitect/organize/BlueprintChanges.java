package com.pkoka5.ironmanbankarchitect.organize;

import java.util.*;

/** Destination comparison of physical copies; quantities and slot offsets are irrelevant. */
public final class BlueprintChanges
{
	private final boolean reference;
	private boolean supported = true;
	private int changed, added, missing, reordered;
	private final StringBuilder details = new StringBuilder();

	private BlueprintChanges(boolean reference) { this.reference = reference; }
	public boolean hasReference() { return reference; }
	public boolean isSupported() { return supported; }
	public int changedCount() { return changed; }
	public int addedCount() { return added; }
	public int missingCount() { return missing; }
	public int reorderedTabCount() { return reordered; }

	public static String capture(BankOrganizationPreview preview)
	{
		StringBuilder out = new StringBuilder("v1");
		for (Position p : positions(preview)) out.append('|').append(p.id).append(':').append(p.tab).append(':').append(p.tag);
		return out.toString();
	}

	public static BlueprintChanges compare(String value, BankOrganizationPreview current)
	{
		BlueprintChanges result = new BlueprintChanges(value != null && !value.isEmpty());
		if (!result.reference) return result;
		List<Position> old = new ArrayList<>();
		try
		{
			if (value.length() > 120000) throw new IllegalArgumentException();
			String[] records = value.split("\\|", -1);
			if (!"v1".equals(records[0]) || records.length > BlueprintItemOrders.BANK_CAPACITY + 1)
				throw new IllegalArgumentException();
			for (int i = 1; i < records.length; i++)
			{
				String[] fields = records[i].split(":", -1);
				if (fields.length != 3) throw new IllegalArgumentException();
				old.add(new Position(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), fields[2], null));
			}
		}
		catch (IllegalArgumentException invalid) { result.supported = false; return result; }
		List<Position> now = positions(current);
		result.compareOrder(old, now);
		// Exact destinations match first so interchangeable duplicate copies cannot create false moves.
		for (Iterator<Position> it = now.iterator(); it.hasNext();)
		{
			Position p = it.next();
			for (int i = 0; i < old.size(); i++)
			{
				Position prior = old.get(i);
				if (p.id == prior.id && p.tab == prior.tab && p.tag.equals(prior.tag))
				{
					old.remove(i); it.remove(); break;
				}
			}
		}
		for (Position p : now)
		{
			Position prior = null;
			for (Position candidate : old) if (candidate.id == p.id) { prior = candidate; break; }
			if (prior == null) result.added++;
			else
			{
				old.remove(prior);
				result.changed++;
				result.details.append(p.item.getDisplayName()).append(": ").append(prior.label())
					.append(" -> ").append(p.label()).append('\n');
				if (!p.item.getPlacementReason().isEmpty()) result.details.append("  ").append(p.item.getPlacementReason()).append('\n');
			}
		}
		result.missing = old.size();
		return result;
	}

	/** Compare the maximum common multiset in order, allowing indistinguishable duplicate copies. */
	private void compareOrder(List<Position> old, List<Position> now)
	{
		for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
		{
			List<Integer> before = new ArrayList<>(), after = new ArrayList<>();
			for (Position p : old) if (p.tab == tab) before.add(p.id);
			for (Position p : now) if (p.tab == tab) after.add(p.id);
			Map<Integer, Integer> counts = new HashMap<>();
			for (int id : before) counts.merge(id, 1, Integer::sum);
			int common = 0;
			for (int id : after)
			{
				int remaining = counts.getOrDefault(id, 0);
				if (remaining > 0) { common++; counts.put(id, remaining - 1); }
			}
			if (common < 2 || before.equals(after)) continue;
			int[] lengths = new int[after.size() + 1];
			for (int id : before)
			{
				int diagonal = 0;
				for (int i = 0; i < after.size(); i++)
				{
					int previous = lengths[i + 1];
					lengths[i + 1] = id == after.get(i) ? diagonal + 1 : Math.max(previous, lengths[i]);
					diagonal = previous;
				}
			}
			if (lengths[after.size()] < common)
			{
				reordered++;
				details.append(tab == 0 ? "Main" : "Tab " + (tab + 1)).append(": relative item order changed\n");
			}
		}
	}

	public String report()
	{
		if (!supported) return "The remembered reference is invalid or from an unsupported version.";
		if (!reference) return "No remembered blueprint for this profile. Remember this blueprint to compare future destinations.";
		return changed + " destination/category changes\n" + reordered + " tabs with relative order changes\n" + added + " new bank entries\n" + missing
			+ " remembered entries currently absent\n\n" + details;
	}

	private static List<Position> positions(BankOrganizationPreview preview)
	{
		List<Position> result = new ArrayList<>();
		for (int tab = 0; tab < preview.getCategories().size(); tab++)
			for (BankPreviewItem item : preview.getCategories().get(tab).getItems())
				if (!item.isBlank()) result.add(new Position(item.getItemId(), tab,
					item.getLayoutTagKey() == null ? "_" : item.getLayoutTagKey(), item));
		if (result.size() > BlueprintItemOrders.BANK_CAPACITY) throw new IllegalArgumentException("Bank capacity exceeded");
		return result;
	}

	private static final class Position
	{
		final int id, tab;
		final String tag;
		final BankPreviewItem item;
		Position(int id, int tab, String tag, BankPreviewItem item)
		{
			if (id <= 0 || id == net.runelite.api.gameval.ItemID.BANK_FILLER || tab < 0 || tab >= BankLayoutPlan.DESTINATION_COUNT)
				throw new IllegalArgumentException("Invalid reference entry");
			if (!"_".equals(tag)) BankTags.byKey(tag);
			this.id = id; this.tab = tab; this.tag = tag; this.item = item;
		}
		String label() { return (tab == 0 ? "Main" : "Tab " + (tab + 1)) + ("_".equals(tag) ? "" : " / " + BankTags.byKey(tag).getName()); }
	}
}

package com.pkoka5.ironmanbankarchitect.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlockMatchResult
{
	private final String blockKey;
	private final List<SlotMatchResult> slotResults;

	public BlockMatchResult(String blockKey, List<SlotMatchResult> slotResults)
	{
		this.blockKey = requireText(blockKey, "blockKey");
		if (slotResults == null)
		{
			throw new IllegalArgumentException("slotResults must not be null");
		}

		this.slotResults = Collections.unmodifiableList(new ArrayList<>(slotResults));
	}

	public String getBlockKey()
	{
		return blockKey;
	}

	public List<SlotMatchResult> getSlotResults()
	{
		return slotResults;
	}

	public int count(SlotMatchState state)
	{
		int count = 0;
		for (SlotMatchResult result : slotResults)
		{
			if (result.getState() == state)
			{
				count++;
			}
		}

		return count;
	}

	public String toCompactSummary()
	{
		return "Owned: " + count(SlotMatchState.OWNED)
			+ " | Missing: " + count(SlotMatchState.MISSING)
			+ " | Role-only: " + count(SlotMatchState.ROLE_ONLY)
			+ " | Reserved: " + count(SlotMatchState.RESERVED_EMPTY);
	}

	public String toSlotDetailText()
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < slotResults.size(); i++)
		{
			if (i > 0)
			{
				builder.append('\n');
			}

			SlotMatchResult result = slotResults.get(i);
			builder.append(i + 1)
				.append(". ")
				.append(result.getSlotLabel())
				.append(" \u2014 ")
				.append(result.getState().getDisplayLabel());
		}

		return builder.toString();
	}

	private static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}

		return value;
	}
}

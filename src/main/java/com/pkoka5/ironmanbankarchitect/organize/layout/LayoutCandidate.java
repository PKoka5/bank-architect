package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * One manually constructed canonical local shape for a rule: the rule key, the prescribed shape
 * primitive, one concrete width, the ordered atom keys it realizes, and its explicit immutable
 * rows. Each row carries a local start offset plus a non-empty contiguous item-ID vector, and
 * {@code startOffset + rowLength <= width} always holds.
 *
 * <p>Item IDs are positive and unique across the whole candidate; there are no blanks, Bank
 * Fillers, or phantom cells — a row cell exists only for a real owned item. Row boundaries and
 * offsets are part of the stable identity: the same flat item vector split into different rows is
 * a different candidate.</p>
 *
 * <p>Candidate generation itself is a later slice — 3A1 only models and validates hand-built
 * candidates.</p>
 */
public final class LayoutCandidate
{
	private final String ruleKey;
	private final ShapePrimitive shapePrimitive;
	private final int width;
	private final List<String> atomKeys;
	private final List<Row> rows;

	public LayoutCandidate(String ruleKey, ShapePrimitive shapePrimitive, int width, List<String> atomKeys,
		List<Row> rows)
	{
		this.ruleKey = SemanticRule.requireRuleKey(ruleKey, "ruleKey");
		this.shapePrimitive = Objects.requireNonNull(shapePrimitive, "shapePrimitive");
		if (width < SemanticRule.MIN_WIDTH || width > SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("width must be within "
				+ SemanticRule.MIN_WIDTH + ".." + SemanticRule.MAX_WIDTH);
		}
		this.width = width;
		this.atomKeys = requireAtomKeys(atomKeys);
		this.rows = requireRows(rows, width);
	}

	public String getRuleKey()
	{
		return ruleKey;
	}

	public ShapePrimitive getShapePrimitive()
	{
		return shapePrimitive;
	}

	public int getWidth()
	{
		return width;
	}

	public List<String> getAtomKeys()
	{
		return atomKeys;
	}

	public List<Row> getRows()
	{
		return rows;
	}

	/**
	 * Derived flat view over all rows in row-major order.
	 */
	public List<Integer> getRowMajorItemIds()
	{
		List<Integer> itemIds = new ArrayList<>();
		for (Row row : rows)
		{
			itemIds.addAll(row.getItemIds());
		}
		return Collections.unmodifiableList(itemIds);
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof LayoutCandidate))
		{
			return false;
		}

		LayoutCandidate candidate = (LayoutCandidate) other;
		return width == candidate.width
			&& ruleKey.equals(candidate.ruleKey)
			&& shapePrimitive == candidate.shapePrimitive
			&& atomKeys.equals(candidate.atomKeys)
			&& rows.equals(candidate.rows);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleKey, shapePrimitive, width, atomKeys, rows);
	}

	@Override
	public String toString()
	{
		return "LayoutCandidate{" + ruleKey + ", " + shapePrimitive + ", width=" + width
			+ ", atoms=" + atomKeys + ", rows=" + rows + "}";
	}

	static List<String> requireAtomKeys(List<String> atomKeys)
	{
		if (atomKeys == null || atomKeys.isEmpty())
		{
			throw new IllegalArgumentException("atomKeys must not be empty");
		}

		Set<String> seen = new HashSet<>();
		List<String> validated = new ArrayList<>(atomKeys.size());
		for (String atomKey : atomKeys)
		{
			String valid = SemanticRule.requireRuleKey(atomKey, "atomKeys");
			if (!seen.add(valid))
			{
				throw new IllegalArgumentException("duplicate atom key " + valid);
			}
			validated.add(valid);
		}

		return Collections.unmodifiableList(validated);
	}

	static List<Row> requireRows(List<Row> rows, int width)
	{
		if (rows == null || rows.isEmpty())
		{
			throw new IllegalArgumentException("rows must not be empty");
		}

		Set<Integer> seenItemIds = new HashSet<>();
		for (Row row : rows)
		{
			if (row == null)
			{
				throw new IllegalArgumentException("rows must not contain null");
			}
			if (row.getStartOffset() > width - row.length())
			{
				throw new IllegalArgumentException("row with start offset " + row.getStartOffset()
					+ " and length " + row.length() + " does not fit width " + width);
			}
			for (Integer itemId : row.getItemIds())
			{
				if (!seenItemIds.add(itemId))
				{
					throw new IllegalArgumentException("duplicate item ID " + itemId + " across rows");
				}
			}
		}

		return Collections.unmodifiableList(new ArrayList<>(rows));
	}

	/**
	 * One explicit candidate row: a local start offset inside the block plus one non-empty
	 * contiguous item-ID vector. Uniqueness of IDs is enforced candidate-wide, not per row.
	 */
	public static final class Row
	{
		private final int startOffset;
		private final List<Integer> itemIds;

		public Row(int startOffset, List<Integer> itemIds)
		{
			if (startOffset < 0)
			{
				throw new IllegalArgumentException("startOffset must not be negative");
			}
			this.startOffset = startOffset;
			this.itemIds = requireItemIds(itemIds);
		}

		public int getStartOffset()
		{
			return startOffset;
		}

		public List<Integer> getItemIds()
		{
			return itemIds;
		}

		public int length()
		{
			return itemIds.size();
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof Row))
			{
				return false;
			}

			Row row = (Row) other;
			return startOffset == row.startOffset && itemIds.equals(row.itemIds);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(startOffset, itemIds);
		}

		@Override
		public String toString()
		{
			return "Row{offset=" + startOffset + ", items=" + itemIds + "}";
		}

		private static List<Integer> requireItemIds(List<Integer> itemIds)
		{
			if (itemIds == null || itemIds.isEmpty())
			{
				throw new IllegalArgumentException("row itemIds must not be empty");
			}

			for (Integer itemId : itemIds)
			{
				if (itemId == null || itemId <= 0)
				{
					throw new IllegalArgumentException("row itemIds must contain positive item IDs");
				}
				if (itemId == ItemID.BANK_FILLER)
				{
					throw new IllegalArgumentException("row itemIds must not contain Bank Filler");
				}
			}

			return Collections.unmodifiableList(new ArrayList<>(itemIds));
		}
	}
}

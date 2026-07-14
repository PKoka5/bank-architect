package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.Comparator;
import java.util.Objects;

/**
 * One typed validation conflict. A conflict never produces a partial plan: any conflict makes the
 * whole request or plan invalid via {@link LayoutResult}.
 */
public final class LayoutConflict
{
	/**
	 * Canonical stable conflict order used by the validators: type, then item ID, then detail.
	 */
	static final Comparator<LayoutConflict> CANONICAL_ORDER =
		Comparator.comparingInt((LayoutConflict conflict) -> conflict.getType().ordinal())
			.thenComparingInt(LayoutConflict::getItemId)
			.thenComparing(LayoutConflict::getDetail);

	/**
	 * Item ID value for conflicts that are not about one specific item.
	 */
	public static final int NO_ITEM = -1;

	public enum Type
	{
		NULL_ENTRY,
		NON_POSITIVE_ITEM_ID,
		BANK_FILLER_ITEM,
		BLANK_ITEM,
		INVALID_PLACEHOLDER_STATE,
		DUPLICATE_ITEM_ID,
		DUPLICATE_RULE_KEY,
		RULE_ITEM_OVERLAP,
		LOCK_TARGET_OUT_OF_RANGE,
		DUPLICATE_LOCK_TARGET,
		DENSE_ORDER_NOT_PERMUTATION,
		DENSE_ORDER_RANK_MISMATCH,
		DENSE_RANK_WITHOUT_ORDER,
		DENSE_RANK_OUT_OF_RANGE,
		DENSE_RANK_DUPLICATE,
		FALLBACK_ORDER_NOT_PERMUTATION,
		PLAN_SIZE_MISMATCH,
		PLAN_DUPLICATE_ITEM,
		PLAN_PHANTOM_ITEM,
		PLAN_MISSING_ITEM,
		PLAN_TARGET_OUT_OF_RANGE,
		PLAN_DUPLICATE_TARGET,
		PLAN_QUANTITY_MISMATCH,
		PLAN_PLACEHOLDER_MISMATCH,
		PLAN_LOCK_VIOLATION,
		PLAN_SEMANTIC_GEOMETRY_MISMATCH
	}

	private final Type type;
	private final int itemId;
	private final String detail;

	public LayoutConflict(Type type, int itemId, String detail)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.itemId = itemId;
		this.detail = requireText(detail, "detail");
	}

	public Type getType()
	{
		return type;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getDetail()
	{
		return detail;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof LayoutConflict))
		{
			return false;
		}

		LayoutConflict conflict = (LayoutConflict) other;
		return type == conflict.type && itemId == conflict.itemId && detail.equals(conflict.detail);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type, itemId, detail);
	}

	@Override
	public String toString()
	{
		return type + "(itemId=" + itemId + "): " + detail;
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

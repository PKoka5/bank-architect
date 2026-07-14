package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One immutable layout request for a single planner category: the real entries, the applicable
 * semantic rules, and an optional proven complete dense category order.
 *
 * <p>The entry list and the dense-order list may contain invalid content (including {@code null}
 * elements) because {@link LayoutRequestValidator} must be able to report those as typed
 * {@link LayoutConflict}s. Rules are curated in code and may never be {@code null}.</p>
 *
 * <p>The dense order, when present, claims that the request items currently occupy that exact
 * category-local order. It is valid only as an exact unique permutation of all request item IDs;
 * flat source slots never imply it.</p>
 */
public final class LayoutRequest
{
	private final List<LayoutEntry> entries;
	private final List<SemanticRule> rules;
	private final int gridStartColumn;
	private final boolean hasCurrentDenseCategoryOrder;
	private final List<Integer> currentDenseCategoryOrder;

	public LayoutRequest(List<LayoutEntry> entries, List<SemanticRule> rules)
	{
		this(entries, rules, null, 0);
	}

	public LayoutRequest(List<LayoutEntry> entries, List<SemanticRule> rules,
		List<Integer> currentDenseCategoryOrder)
	{
		this(entries, rules, currentDenseCategoryOrder, 0);
	}

	private LayoutRequest(List<LayoutEntry> entries, List<SemanticRule> rules,
		List<Integer> currentDenseCategoryOrder, int gridStartColumn)
	{
		Objects.requireNonNull(entries, "entries");
		Objects.requireNonNull(rules, "rules");
		if (gridStartColumn < 0 || gridStartColumn >= SemanticRule.MAX_WIDTH)
		{
			throw new IllegalArgumentException("gridStartColumn must be between 0 and 7");
		}
		for (SemanticRule rule : rules)
		{
			Objects.requireNonNull(rule, "rules must not contain null");
		}

		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
		this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
		this.gridStartColumn = gridStartColumn;
		this.hasCurrentDenseCategoryOrder = currentDenseCategoryOrder != null;
		this.currentDenseCategoryOrder = currentDenseCategoryOrder == null
			? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(currentDenseCategoryOrder));
	}

	/**
	 * Returns an immutable copy whose local target zero begins at the supplied physical bank
	 * column. Dense targets, locks, and movement facts remain request-local.
	 */
	public LayoutRequest withGridStartColumn(int column)
	{
		return new LayoutRequest(entries, rules,
			hasCurrentDenseCategoryOrder ? currentDenseCategoryOrder : null, column);
	}

	public int size()
	{
		return entries.size();
	}

	public List<LayoutEntry> getEntries()
	{
		return entries;
	}

	public List<SemanticRule> getRules()
	{
		return rules;
	}

	public int getGridStartColumn()
	{
		return gridStartColumn;
	}

	public boolean hasCurrentDenseCategoryOrder()
	{
		return hasCurrentDenseCategoryOrder;
	}

	public List<Integer> getCurrentDenseCategoryOrder()
	{
		if (!hasCurrentDenseCategoryOrder)
		{
			throw new IllegalStateException("request has no current dense category order");
		}

		return currentDenseCategoryOrder;
	}
}

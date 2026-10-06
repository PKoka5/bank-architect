package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.*;
import java.util.List;


/** Exact cosmetic, holiday and League outfit columns compiled from the reviewed set catalog. */
public final class CosmeticSetSemanticRuleSet
{
	private CosmeticSetSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		SemanticRule rule = VerticalItemSetRuleFactory.build(
			"cosmetic.vertical-sets", entries, ItemSetCatalog.sets("cosmetics"));
		return new LayoutRequest(entries, rule == null
			? Collections.emptyList() : Collections.singletonList(rule));
	}
}

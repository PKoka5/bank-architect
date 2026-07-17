package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Builds one exact-ID vertical rule containing only owned sets with at least two pieces. */
final class VerticalItemSetRuleFactory
{
	private VerticalItemSetRuleFactory()
	{
	}

	static SemanticRule build(String ruleKey, List<LayoutEntry> entries,
		List<ItemSetCatalog.SetDefinition> definitions)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}

		List<SemanticAtom> atoms = new ArrayList<>();
		for (ItemSetCatalog.SetDefinition definition : definitions)
		{
			List<SemanticAtom.Member> members = new ArrayList<>();
			for (Integer itemId : definition.getItemIds())
			{
				if (present.contains(itemId))
				{
					members.add(new SemanticAtom.Member("piece-" + members.size(), itemId));
				}
			}
			if (members.size() >= 2)
			{
				atoms.add(new SemanticAtom(definition.getKey(), members));
			}
		}
		if (atoms.isEmpty())
		{
			return null;
		}
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(Collections.singleton(1))
			.build();
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Exact combat families whose head/body/legs or body/legs/feet order is vertical. */
public final class GearSetSemanticRuleSet
{
	private static final List<SetFact> SETS = Collections.unmodifiableList(Arrays.asList(
		set("gear.proselyte", 9672, 9674, 9676),
		set("gear.mixed-hide", 29280, 29283, 29286),
		set("gear.eclipse-moon", 29010, 29004, 29007),
		set("gear.blood-moon", 29028, 29022, 29025)));
	private static final List<SemanticRule> RULES = Collections.singletonList(buildRule());

	private GearSetSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		return new LayoutRequest(Objects.requireNonNull(entries, "entries"), RULES);
	}

	private static SemanticRule buildRule()
	{
		List<SemanticAtom> atoms = new ArrayList<>();
		for (SetFact set : SETS)
		{
			List<SemanticAtom.Member> members = new ArrayList<>();
			for (int index = 0; index < set.itemIds.length; index++)
			{
				members.add(new SemanticAtom.Member("slot-" + index, set.itemIds[index]));
			}
			atoms.add(new SemanticAtom(set.key, members));
		}
		return SemanticRule.builder()
			.ruleKey("gear.vertical-sets")
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(Collections.singleton(1))
			.build();
	}

	private static SetFact set(String key, int... itemIds)
	{
		return new SetFact(key, itemIds);
	}

	private static final class SetFact
	{
		private final String key;
		private final int[] itemIds;

		private SetFact(String key, int[] itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}
}

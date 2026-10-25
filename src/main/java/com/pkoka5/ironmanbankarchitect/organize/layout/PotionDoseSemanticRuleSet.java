package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.*;
import java.util.*;
import java.util.List;



/**
 * Exact canonical potion-dose families used by the supplies-category semantic layout. Each owned
 * family projects to a horizontal run in reviewed descending-dose order without name inference.
 */
public final class PotionDoseSemanticRuleSet
{
	private static final String RULE_KEY = "potion.dose-runs";

	private static final OrderedItemFamilies FAMILIES = new OrderedItemFamilies(
		PotionDoseSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/potion-layout-families.tsv"), 4);

	private PotionDoseSemanticRuleSet()
	{
	}

	/**
	 * Creates a request without claiming a dense current order or adding entry-level dense ranks.
	 */
	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		validateMetadata();
		return new LayoutRequest(entries, Collections.singletonList(buildRule()));
	}

	private static SemanticRule buildRule()
	{
		List<SemanticAtom> atoms = new ArrayList<>(FAMILIES.entries().size());
		for (Map.Entry<String, List<Integer>> family : FAMILIES.entries().entrySet())
		{
			atoms.add(SemanticAtom.indexed(family.getKey(), family.getValue(), i -> "dose-" + (4 - i)));
		}

		return SemanticRule.highConfidence(RULE_KEY, atoms, ShapePrimitive.HORIZONTAL_RUN, SemanticRule.ALL_WIDTHS).build();
	}

	private static void validateMetadata()
	{
		for (Map.Entry<String, List<Integer>> family : FAMILIES.entries().entrySet())
		{
			for (int index = 0; index < family.getValue().size(); index++)
			{
				ResourceItemSortMetadataCatalog.INSTANCE.requireVariant(family.getValue().get(index),
					family.getKey(), ItemSortMetadata.VariantKind.DOSE, 4 - index);
			}
		}
	}

}

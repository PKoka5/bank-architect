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
	private static final Set<Integer> ALL_WIDTHS = Collections.unmodifiableSet(
		new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));

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
			List<SemanticAtom.Member> members = new ArrayList<>();
			for (int index = 0; index < 4; index++)
			{
				members.add(new SemanticAtom.Member("dose-" + (4 - index), family.getValue().get(index)));
			}
			atoms.add(new SemanticAtom(family.getKey(), members));
		}

		return SemanticRule.builder()
			.ruleKey(RULE_KEY)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static void validateMetadata()
	{
		for (Map.Entry<String, List<Integer>> family : FAMILIES.entries().entrySet())
		{
			for (int index = 0; index < family.getValue().size(); index++)
			{
				validateMember(family.getKey(), family.getValue().get(index), 4 - index);
			}
		}
	}

	private static void validateMember(String expectedFamilyKey, int itemId, int expectedDose)
	{
		ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId)
			.orElseThrow(() -> new IllegalStateException(
				"Missing potion semantic metadata for itemId " + itemId));
		if (!expectedFamilyKey.equals(metadata.getFamilyKey())
			|| metadata.getVariantKind() != ItemSortMetadata.VariantKind.DOSE
			|| metadata.getVariantValue() != expectedDose)
		{
			throw new IllegalStateException("Potion semantic metadata mismatch for itemId " + itemId
				+ ": expected family=" + expectedFamilyKey + ", kind=DOSE, dose=" + expectedDose
				+ " but was family=" + metadata.getFamilyKey() + ", kind="
				+ metadata.getVariantKind() + ", dose=" + metadata.getVariantValue());
		}
	}

}

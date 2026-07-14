package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Exact canonical potion-dose families used by the supplies-category semantic layout. Each owned
 * family projects to a horizontal run in reviewed descending-dose order without name inference.
 */
public final class PotionDoseSemanticRuleSet
{
	private static final String RULE_KEY = "potion.dose-runs";
	private static final Set<Integer> ALL_WIDTHS = Collections.unmodifiableSet(
		new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));

	private static final List<FamilyFact> FAMILIES = Collections.unmodifiableList(Arrays.asList(
		family("potion.anti_venom", 12905, 12907, 12909, 12911),
		family("potion.anti_venom_plus", 12913, 12915, 12917, 12919),
		family("potion.antifire", 2452, 2454, 2456, 2458),
		family("potion.antipoison", 2446, 175, 177, 179),
		family("potion.attack", 2428, 121, 123, 125),
		family("potion.combat", 9739, 9741, 9743, 9745),
		family("potion.defence", 2432, 133, 135, 137),
		family("potion.energy", 3008, 3010, 3012, 3014),
		family("potion.magic", 3040, 3042, 3044, 3046),
		family("potion.prayer", 2434, 139, 141, 143),
		family("potion.ranging", 2444, 169, 171, 173),
		family("potion.restore", 2430, 127, 129, 131),
		family("potion.saradomin_brew", 6685, 6687, 6689, 6691),
		family("potion.stamina", 12625, 12627, 12629, 12631),
		family("potion.strength", 113, 115, 117, 119),
		family("potion.super_attack", 2436, 145, 147, 149),
		family("potion.super_combat", 12695, 12697, 12699, 12701),
		family("potion.super_defence", 2442, 163, 165, 167),
		family("potion.super_energy", 3016, 3018, 3020, 3022),
		family("potion.super_restore", 3024, 3026, 3028, 3030),
		family("potion.super_strength", 2440, 157, 159, 161),
		family("potion.superantipoison", 2448, 181, 183, 185)));

	private static final List<SemanticRule> RULES = Collections.singletonList(buildRule());

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
		return new LayoutRequest(entries, RULES);
	}

	private static SemanticRule buildRule()
	{
		List<SemanticAtom> atoms = new ArrayList<>(FAMILIES.size());
		for (FamilyFact family : FAMILIES)
		{
			atoms.add(new SemanticAtom(family.familyKey, Arrays.asList(
				new SemanticAtom.Member("dose-4", family.itemIds[0]),
				new SemanticAtom.Member("dose-3", family.itemIds[1]),
				new SemanticAtom.Member("dose-2", family.itemIds[2]),
				new SemanticAtom.Member("dose-1", family.itemIds[3]))));
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
		for (FamilyFact family : FAMILIES)
		{
			for (int index = 0; index < family.itemIds.length; index++)
			{
				validateMember(family.familyKey, family.itemIds[index], 4 - index);
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

	private static FamilyFact family(String familyKey, int dose4, int dose3, int dose2, int dose1)
	{
		return new FamilyFact(familyKey, new int[]{dose4, dose3, dose2, dose1});
	}

	private static final class FamilyFact
	{
		private final String familyKey;
		private final int[] itemIds;

		private FamilyFact(String familyKey, int[] itemIds)
		{
			this.familyKey = familyKey;
			this.itemIds = itemIds;
		}
	}
}

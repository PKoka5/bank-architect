package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PotionDoseSemanticRuleSetTest
{
	private static final List<String> FAMILY_KEYS = Arrays.asList(
		"potion.anti_venom", "potion.anti_venom_plus", "potion.antifire", "potion.antipoison",
		"potion.attack", "potion.combat", "potion.defence", "potion.energy", "potion.magic",
		"potion.prayer", "potion.ranging", "potion.restore", "potion.saradomin_brew",
		"potion.stamina", "potion.strength", "potion.super_attack", "potion.super_combat",
		"potion.super_defence", "potion.super_energy", "potion.super_restore",
		"potion.super_strength", "potion.superantipoison");

	private static final List<List<Integer>> FAMILY_IDS = Arrays.asList(
		ids(12905, 12907, 12909, 12911), ids(12913, 12915, 12917, 12919),
		ids(2452, 2454, 2456, 2458), ids(2446, 175, 177, 179),
		ids(2428, 121, 123, 125), ids(9739, 9741, 9743, 9745),
		ids(2432, 133, 135, 137), ids(3008, 3010, 3012, 3014),
		ids(3040, 3042, 3044, 3046), ids(2434, 139, 141, 143),
		ids(2444, 169, 171, 173), ids(2430, 127, 129, 131),
		ids(6685, 6687, 6689, 6691), ids(12625, 12627, 12629, 12631),
		ids(113, 115, 117, 119), ids(2436, 145, 147, 149),
		ids(12695, 12697, 12699, 12701), ids(2442, 163, 165, 167),
		ids(3016, 3018, 3020, 3022), ids(3024, 3026, 3028, 3030),
		ids(2440, 157, 159, 161), ids(2448, 181, 183, 185));

	@Test
	public void canonicalRuleContainsAllTwentyTwoExactDescendingDoseFamilies()
	{
		LayoutRequest request = PotionDoseSemanticRuleSet.forEntries(Collections.emptyList());

		assertFalse(request.hasCurrentDenseCategoryOrder());
		assertEquals(1, request.getRules().size());
		SemanticRule rule = request.getRules().get(0);
		assertEquals("potion.dose-runs", rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.HORIZONTAL_RUN, rule.getShapePrimitive());
		assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)),
			rule.getAllowedWidths());
		assertFalse(rule.hasWidthEvidence());
		assertTrue(rule.getSpilloverCompatibleRuleKeys().isEmpty());
		assertEquals(22, rule.getAtoms().size());

		for (int familyIndex = 0; familyIndex < FAMILY_KEYS.size(); familyIndex++)
		{
			SemanticAtom atom = rule.getAtoms().get(familyIndex);
			assertEquals(FAMILY_KEYS.get(familyIndex), atom.getAtomKey());
			assertEquals(FAMILY_IDS.get(familyIndex), atom.getItemIds());
			for (int memberIndex = 0; memberIndex < 4; memberIndex++)
			{
				int expectedDose = 4 - memberIndex;
				assertEquals("dose-" + expectedDose,
					atom.getMembers().get(memberIndex).getMemberKey());
				assertMetadata(atom.getAtomKey(), atom.getItemIds().get(memberIndex), expectedDose);
			}
		}
	}

	@Test
	public void twoCompleteFamiliesShareOneRowAsFourWideRuns()
	{
		List<Integer> fallback = Arrays.asList(2428, 121, 123, 125, 2434, 139, 141, 143);

		BoundedLayoutPacker.Outcome outcome = planDetailed(fallback, fallback);

		assertEquals(fallback, targetOrder(outcome));
		assertEquals(2, outcome.getTieKey().getBlocks().size());
		PlacedBlock attack = outcome.getTieKey().getBlocks().get(0);
		PlacedBlock prayer = outcome.getTieKey().getBlocks().get(1);
		assertEquals("potion.attack", attack.getAtomKeys().get(0));
		assertEquals(4, attack.getWidth());
		assertEquals(0, attack.getStartRow());
		assertEquals(0, attack.getStartColumn());
		assertEquals("potion.prayer", prayer.getAtomKeys().get(0));
		assertEquals(4, prayer.getWidth());
		assertEquals(0, prayer.getStartRow());
		assertEquals(4, prayer.getStartColumn());
	}

	@Test
	public void incompleteFamilyStaysCompactInReviewedDoseOrderWithoutPhantoms()
	{
		List<Integer> input = Arrays.asList(123, 2428);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);

		assertEquals(Arrays.asList(2428, 123), targetOrder(outcome));
		assertEquals(1, outcome.getTieKey().getBlocks().size());
		assertEquals(2, outcome.getTieKey().getBlocks().get(0).getWidth());
		assertFalse(targetOrder(outcome).contains(121));
		assertFalse(targetOrder(outcome).contains(125));
	}

	@Test
	public void singletonFamiliesReturnExactCallerSuppliedFallback()
	{
		List<Integer> input = Arrays.asList(2428, 2434, 385);
		List<Integer> fallback = Arrays.asList(2434, 385, 2428);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, fallback);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, targetOrder(outcome));
	}

	@Test
	public void fourLookalikeIdsNeverBecomeACanonicalDoseFamily()
	{
		List<Integer> input = Arrays.asList(990004, 990003, 990002, 990001);
		List<Integer> fallback = Arrays.asList(990001, 990002, 990003, 990004);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, fallback);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, targetOrder(outcome));
	}

	@Test
	public void requestRetainsSourceSlotsWithoutDenseRanksOrLocks()
	{
		List<LayoutEntry> entries = Arrays.asList(entry(2434, 91), entry(139, 4));

		LayoutRequest request = PotionDoseSemanticRuleSet.forEntries(entries);

		assertEquals(91, request.getEntries().get(0).getSourceFlatBankSlot());
		assertEquals(4, request.getEntries().get(1).getSourceFlatBankSlot());
		assertFalse(request.hasCurrentDenseCategoryOrder());
		for (LayoutEntry entry : request.getEntries())
		{
			assertFalse(entry.hasDenseCategoryRank());
			assertFalse(entry.hasLockedTarget());
		}
	}

	private static void assertMetadata(String familyKey, int itemId, int dose)
	{
		ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId)
			.orElseThrow(AssertionError::new);
		assertEquals(familyKey, metadata.getFamilyKey());
		assertEquals(ItemSortMetadata.VariantKind.DOSE, metadata.getVariantKind());
		assertEquals(dose, metadata.getVariantValue());
	}

	private static BoundedLayoutPacker.Outcome planDetailed(List<Integer> input,
		List<Integer> fallback)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < input.size(); index++)
		{
			entries.add(entry(input.get(index), 200 + index * 13));
		}
		BoundedLayoutPacker.Outcome outcome = SemanticBlockLayoutEngine.planDetailed(
			PotionDoseSemanticRuleSet.forEntries(entries), fallback,
			BoundedLayoutPacker.Limits.production());
		assertTrue(outcome.getResult().getConflicts().toString(), outcome.getResult().isSuccess());
		return outcome;
	}

	private static List<Integer> targetOrder(BoundedLayoutPacker.Outcome outcome)
	{
		Integer[] byTarget = new Integer[outcome.getResult().getPlacements().size()];
		for (LayoutPlacement placement : outcome.getResult().getPlacements())
		{
			byTarget[placement.getTargetIndex()] = placement.getItem().getItemId();
		}
		return Arrays.asList(byTarget);
	}

	private static LayoutEntry entry(int itemId, int sourceSlot)
	{
		CatalogItem catalogItem = new CatalogItem(itemId, "Item " + itemId, ItemCategory.POTION,
			"potion", Collections.emptySet(), null);
		return LayoutEntry.of(new BankPreviewItem(catalogItem, 1), sourceSlot);
	}

	private static List<Integer> ids(int... values)
	{
		List<Integer> result = new ArrayList<>();
		for (int value : values)
		{
			result.add(value);
		}
		return result;
	}
}

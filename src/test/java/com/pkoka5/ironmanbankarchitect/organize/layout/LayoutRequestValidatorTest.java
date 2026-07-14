package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class LayoutRequestValidatorTest
{
	@Test
	public void gridStartColumnCopiesPlacementContextWithoutChangingLocalFacts()
	{
		List<LayoutEntry> entries = Collections.singletonList(entry(100, 3));
		LayoutRequest original = new LayoutRequest(entries, Collections.emptyList(),
			Collections.singletonList(100));

		LayoutRequest shifted = original.withGridStartColumn(7);

		assertEquals(0, original.getGridStartColumn());
		assertEquals(7, shifted.getGridStartColumn());
		assertEquals(original.getEntries(), shifted.getEntries());
		assertEquals(original.getRules(), shifted.getRules());
		assertTrue(shifted.hasCurrentDenseCategoryOrder());
		assertEquals(original.getCurrentDenseCategoryOrder(), shifted.getCurrentDenseCategoryOrder());
	}

	@Test
	public void gridStartColumnRejectsValuesOutsidePhysicalBankWidth()
	{
		LayoutRequest request = new LayoutRequest(Collections.emptyList(), Collections.emptyList());
		assertIllegalGridStart(request, -1);
		assertIllegalGridStart(request, 8);
	}
	@Test
	public void emptyRequestIsValid()
	{
		LayoutRequest request = new LayoutRequest(Collections.emptyList(), Collections.emptyList());

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
	}

	@Test
	public void normalRequestIsValid()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(
				entry(10, 0).withLockedTarget(0),
				entry(20, 9),
				entry(30, 4)),
			Arrays.asList(rule("herb.workflow", 10, 20)));

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
	}

	@Test
	public void duplicateItemIdsAreATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(10, 1)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.DUPLICATE_ITEM_ID, 10);
	}

	@Test
	public void nonPositiveItemIdIsATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(LayoutEntry.of(new BankPreviewItem(0, "Zero", 1), 0)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.NON_POSITIVE_ITEM_ID, 0);
	}

	@Test
	public void bankFillerIsATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(LayoutEntry.of(new BankPreviewItem(ItemID.BANK_FILLER, "Bank filler", 1), 0)),
			Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.BANK_FILLER_ITEM, ItemID.BANK_FILLER);
	}

	@Test
	public void blankPreviewItemIsATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(LayoutEntry.of(BankPreviewItem.blank(), 0)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.BLANK_ITEM, -1);
	}

	@Test
	public void impossiblePositiveQuantityPlaceholderIsATypedConflict()
	{
		CatalogItem catalogItem = new CatalogItem(10, "Item 10", ItemCategory.UNKNOWN, "unknown",
			Collections.emptySet(), null);
		LayoutRequest request = new LayoutRequest(Arrays.asList(
			LayoutEntry.of(new BankPreviewItem(catalogItem, 1, true), 0)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.INVALID_PLACEHOLDER_STATE, 10);
	}

	@Test
	public void nullEntryIsATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), null), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.NULL_ENTRY, LayoutConflict.NO_ITEM);
	}

	@Test
	public void overlappingRulesAreATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(20, 1), entry(30, 2)),
			Arrays.asList(rule("herb.workflow", 10, 20), rule("gem.workflow", 20, 30)));

		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
		assertEquals(1, conflicts.size());
		assertEquals(LayoutConflict.Type.RULE_ITEM_OVERLAP, conflicts.get(0).getType());
		assertEquals(20, conflicts.get(0).getItemId());
	}

	@Test
	public void duplicateRuleKeysAreATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(20, 1)),
			Arrays.asList(rule("herb.workflow", 10), rule("herb.workflow", 20)));

		assertSingleConflict(request, LayoutConflict.Type.DUPLICATE_RULE_KEY, LayoutConflict.NO_ITEM);
	}

	@Test
	public void negativeLockedTargetIsOutOfRange()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withLockedTarget(-1), entry(20, 1)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.LOCK_TARGET_OUT_OF_RANGE, 10);
	}

	@Test
	public void lockedTargetAtOrBeyondSizeIsOutOfRange()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withLockedTarget(2), entry(20, 1)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.LOCK_TARGET_OUT_OF_RANGE, 10);
	}

	@Test
	public void twoItemsOnTheSameLockedTargetAreATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withLockedTarget(1), entry(20, 1).withLockedTarget(1)),
			Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.DUPLICATE_LOCK_TARGET, LayoutConflict.NO_ITEM);
	}

	@Test
	public void missingDenseOrderIsValidAndUnproven()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(20, 6)), Collections.emptyList());

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
		assertFalse(request.hasCurrentDenseCategoryOrder());
	}

	@Test
	public void partialDenseOrderIsNotAPermutation()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(20, 1), entry(30, 2)),
			Collections.emptyList(),
			Arrays.asList(20, 10));

		assertSingleConflict(request, LayoutConflict.Type.DENSE_ORDER_NOT_PERMUTATION, LayoutConflict.NO_ITEM);
	}

	@Test
	public void denseOrderWithDuplicateOrForeignOrNullIdsIsNotAPermutation()
	{
		List<LayoutEntry> entries = Arrays.asList(entry(10, 0), entry(20, 1));

		for (List<Integer> order : Arrays.asList(
			Arrays.asList(10, 10),
			Arrays.asList(10, 99),
			Arrays.asList(10, (Integer) null),
			Arrays.asList(10, 20, 30)))
		{
			LayoutRequest request = new LayoutRequest(entries, Collections.emptyList(), order);
			assertSingleConflict(request, LayoutConflict.Type.DENSE_ORDER_NOT_PERMUTATION, LayoutConflict.NO_ITEM);
		}
	}

	@Test
	public void completeDenseOrderIsValid()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 5), entry(20, 2), entry(30, 8)),
			Collections.emptyList(),
			Arrays.asList(30, 10, 20));

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
		assertTrue(request.hasCurrentDenseCategoryOrder());
	}

	@Test
	public void entryRankMustMatchTheDenseOrder()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withDenseCategoryRank(0), entry(20, 1)),
			Collections.emptyList(),
			Arrays.asList(20, 10));

		assertSingleConflict(request, LayoutConflict.Type.DENSE_ORDER_RANK_MISMATCH, 10);
	}

	@Test
	public void matchingEntryRanksAreValid()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 7).withDenseCategoryRank(1), entry(20, 3).withDenseCategoryRank(0)),
			Collections.emptyList(),
			Arrays.asList(20, 10));

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
	}

	@Test
	public void entryRankWithoutCompleteOrderIsATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withDenseCategoryRank(0)), Collections.emptyList());

		assertSingleConflict(request, LayoutConflict.Type.DENSE_RANK_WITHOUT_ORDER, 10);
	}

	@Test
	public void entryRanksMustStayInsideRequestRange()
	{
		for (int rank : Arrays.asList(-1, 2))
		{
			LayoutRequest request = new LayoutRequest(
				Arrays.asList(entry(10, 0).withDenseCategoryRank(rank), entry(20, 1)),
				Collections.emptyList(), Arrays.asList(10, 20));

			List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
			assertTrue(conflicts.toString(), containsType(conflicts, LayoutConflict.Type.DENSE_RANK_OUT_OF_RANGE));
		}
	}

	@Test
	public void duplicateEntryRanksAreATypedConflict()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withDenseCategoryRank(0), entry(20, 1).withDenseCategoryRank(0)),
			Collections.emptyList(), Arrays.asList(10, 20));

		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
		assertTrue(containsType(conflicts, LayoutConflict.Type.DENSE_RANK_DUPLICATE));
		assertTrue(containsType(conflicts, LayoutConflict.Type.DENSE_ORDER_RANK_MISMATCH));
	}

	@Test
	public void sourceFlatBankSlotsNeverProveADenseOrder()
	{
		// Entries carry flat slots 3 and 0 but no explicit order: valid, yet unproven.
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 3), entry(20, 0)), Collections.emptyList());

		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
		assertFalse(request.hasCurrentDenseCategoryOrder());
		assertFalse(request.getEntries().get(0).hasDenseCategoryRank());
	}

	@Test
	public void validatorDoesNotMutateInputAndRequestCopiesDefensively()
	{
		List<LayoutEntry> entries = new ArrayList<>(Arrays.asList(entry(10, 0), entry(20, 1)));
		List<SemanticRule> rules = new ArrayList<>(Collections.singletonList(rule("herb.workflow", 10)));
		List<Integer> order = new ArrayList<>(Arrays.asList(20, 10));
		LayoutRequest request = new LayoutRequest(entries, rules, order);

		LayoutRequestValidator.validate(request);
		assertEquals(2, entries.size());
		assertEquals(1, rules.size());
		assertEquals(Arrays.asList(20, 10), order);

		entries.clear();
		rules.clear();
		order.clear();
		assertEquals(2, request.size());
		assertEquals(1, request.getRules().size());
		assertEquals(Arrays.asList(20, 10), request.getCurrentDenseCategoryOrder());
		assertTrue(LayoutRequestValidator.validate(request).isEmpty());
	}

	@Test
	public void requestCollectionsAndConflictListAreImmutable()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0), entry(10, 1)), Collections.emptyList());
		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);

		try
		{
			request.getEntries().add(null);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}

		try
		{
			conflicts.add(new LayoutConflict(LayoutConflict.Type.NULL_ENTRY, LayoutConflict.NO_ITEM, "extra"));
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	@Test
	public void repeatedValidationIsDeterministic()
	{
		LayoutRequest request = new LayoutRequest(
			Arrays.asList(entry(10, 0).withLockedTarget(0), entry(20, 1).withLockedTarget(0), entry(20, 2)),
			Arrays.asList(rule("herb.workflow", 10, 20), rule("gem.workflow", 20)));

		assertEquals(LayoutRequestValidator.validate(request), LayoutRequestValidator.validate(request));
	}

	@Test
	public void reversedEntryAndRuleOrderYieldsTheSameConflicts()
	{
		List<LayoutEntry> entries = Arrays.asList(
			entry(10, 0).withLockedTarget(1), entry(20, 1).withLockedTarget(1), entry(30, 2));
		List<SemanticRule> rules = Arrays.asList(rule("herb.workflow", 10, 30), rule("gem.workflow", 30));

		List<LayoutEntry> reversedEntries = new ArrayList<>(entries);
		Collections.reverse(reversedEntries);
		List<SemanticRule> reversedRules = new ArrayList<>(rules);
		Collections.reverse(reversedRules);

		List<LayoutConflict> forward = LayoutRequestValidator.validate(new LayoutRequest(entries, rules));
		List<LayoutConflict> backward = LayoutRequestValidator.validate(
			new LayoutRequest(reversedEntries, reversedRules));

		assertEquals(forward, backward);
	}

	@Test
	public void reversedInvalidLocksYieldTheExactSameConflictList()
	{
		List<LayoutEntry> entries = Arrays.asList(
			entry(10, 0).withLockedTarget(-1), entry(20, 1).withLockedTarget(2));
		List<LayoutEntry> reversed = new ArrayList<>(entries);
		Collections.reverse(reversed);

		List<LayoutConflict> forward = LayoutRequestValidator.validate(
			new LayoutRequest(entries, Collections.emptyList()));
		List<LayoutConflict> backward = LayoutRequestValidator.validate(
			new LayoutRequest(reversed, Collections.emptyList()));

		assertEquals(forward, backward);
		assertEquals(2, forward.size());
		assertEquals(LayoutConflict.Type.LOCK_TARGET_OUT_OF_RANGE, forward.get(0).getType());
		assertEquals(LayoutConflict.Type.LOCK_TARGET_OUT_OF_RANGE, forward.get(1).getType());
	}

	private static void assertSingleConflict(LayoutRequest request, LayoutConflict.Type type, int itemId)
	{
		List<LayoutConflict> conflicts = LayoutRequestValidator.validate(request);
		assertEquals("expected exactly one conflict, got " + conflicts, 1, conflicts.size());
		assertEquals(type, conflicts.get(0).getType());
		assertEquals(itemId, conflicts.get(0).getItemId());
	}

	private static void assertIllegalGridStart(LayoutRequest request, int column)
	{
		try
		{
			request.withGridStartColumn(column);
			fail("expected illegal gridStartColumn " + column);
		}
		catch (IllegalArgumentException expected)
		{
			assertTrue(expected.getMessage().contains("gridStartColumn"));
		}
	}

	private static LayoutEntry entry(int itemId, int sourceFlatBankSlot)
	{
		return LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), sourceFlatBankSlot);
	}

	private static SemanticRule rule(String ruleKey, Integer... memberItemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < memberItemIds.length; index++)
		{
			members.add(new SemanticAtom.Member("member." + index, memberItemIds[index]));
		}
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(Collections.singletonList(new SemanticAtom(ruleKey + ".members", members)))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(new LinkedHashSet<>(Arrays.asList(2, 3)))
			.build();
	}

	private static boolean containsType(List<LayoutConflict> conflicts, LayoutConflict.Type type)
	{
		for (LayoutConflict conflict : conflicts)
		{
			if (conflict.getType() == type)
			{
				return true;
			}
		}
		return false;
	}
}

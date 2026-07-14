package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class LayoutPlanValidatorTest
{
	@Test
	public void emptyRequestAcceptsEmptyPlan()
	{
		LayoutResult result = LayoutPlanValidator.validate(request(), Collections.emptyList());

		assertTrue(result.isSuccess());
		assertTrue(result.getPlacements().isEmpty());
		assertTrue(result.getConflicts().isEmpty());
	}

	@Test
	public void validPlanSucceedsInAscendingTargetOrder()
	{
		LayoutRequest request = request(entry(10, 5), entry(20, 3), entry(30, 0));
		List<LayoutPlacement> plan = Arrays.asList(
			placement(30, 1, 2),
			placement(10, 1, 0),
			placement(20, 1, 1));

		LayoutResult result = LayoutPlanValidator.validate(request, plan);

		assertTrue(result.isSuccess());
		assertEquals(3, result.getPlacements().size());
		assertEquals(0, result.getPlacements().get(0).getTargetIndex());
		assertEquals(10, result.getPlacements().get(0).getItem().getItemId());
		assertEquals(1, result.getPlacements().get(1).getTargetIndex());
		assertEquals(20, result.getPlacements().get(1).getItem().getItemId());
		assertEquals(2, result.getPlacements().get(2).getTargetIndex());
		assertEquals(30, result.getPlacements().get(2).getItem().getItemId());
	}

	@Test
	public void planSizeMismatchIsATypedConflict()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		LayoutResult result = LayoutPlanValidator.validate(request, Arrays.asList(placement(10, 1, 0)));

		assertConflictType(result, LayoutConflict.Type.PLAN_SIZE_MISMATCH);
		assertConflictType(result, LayoutConflict.Type.PLAN_MISSING_ITEM);
	}

	@Test
	public void phantomItemIsATypedConflict()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		LayoutResult result = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(99, 1, 1)));

		assertConflictType(result, LayoutConflict.Type.PLAN_PHANTOM_ITEM);
		assertConflictType(result, LayoutConflict.Type.PLAN_MISSING_ITEM);
	}

	@Test
	public void duplicatePlanItemIsATypedConflict()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		LayoutResult result = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(10, 1, 1)));

		assertConflictType(result, LayoutConflict.Type.PLAN_DUPLICATE_ITEM);
	}

	@Test
	public void targetIndicesMustBeExactlyZeroToNMinusOne()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));

		LayoutResult negative = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, -1), placement(20, 1, 1)));
		assertConflictType(negative, LayoutConflict.Type.PLAN_TARGET_OUT_OF_RANGE);

		LayoutResult beyond = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(20, 1, 2)));
		assertConflictType(beyond, LayoutConflict.Type.PLAN_TARGET_OUT_OF_RANGE);

		LayoutResult duplicate = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(20, 1, 0)));
		assertConflictType(duplicate, LayoutConflict.Type.PLAN_DUPLICATE_TARGET);
	}

	@Test
	public void quantityMustBePreserved()
	{
		LayoutRequest request = request(entry(10, 0, 7));
		LayoutResult result = LayoutPlanValidator.validate(request, Arrays.asList(placement(10, 6, 0)));

		assertConflictType(result, LayoutConflict.Type.PLAN_QUANTITY_MISMATCH);
	}

	@Test
	public void realPlaceholderStateMustBePreserved()
	{
		LayoutRequest request = request(LayoutEntry.of(placeholderItem(10), 0));
		LayoutResult dropped = LayoutPlanValidator.validate(request, Arrays.asList(placement(10, 1, 0)));
		assertConflictType(dropped, LayoutConflict.Type.PLAN_PLACEHOLDER_MISMATCH);

		LayoutResult preserved = LayoutPlanValidator.validate(request,
			Arrays.asList(new LayoutPlacement(placeholderItem(10), 0)));
		assertTrue(preserved.isSuccess());
		assertTrue(preserved.getPlacements().get(0).getItem().isPlaceholder());
		assertEquals(0, preserved.getPlacements().get(0).getItem().getQuantity());
	}

	@Test
	public void successfulPlanReusesCanonicalRequestMetadata()
	{
		BankPreviewItem requestItem = new BankPreviewItem(10, "Canonical item", 1);
		CatalogItem forgedCatalog = new CatalogItem(10, "Forged item", ItemCategory.GEAR, "weapon",
			Collections.singleton("forged-tag"), null);
		BankPreviewItem forgedItem = new BankPreviewItem(forgedCatalog, 1);
		LayoutRequest request = request(LayoutEntry.of(requestItem, 0));

		LayoutResult result = LayoutPlanValidator.validate(request,
			Collections.singletonList(new LayoutPlacement(forgedItem, 0)));

		assertTrue(result.isSuccess());
		assertSame(requestItem, result.getPlacements().get(0).getItem());
		assertEquals("Canonical item", result.getPlacements().get(0).getItem().getDisplayName());
		assertEquals(ItemCategory.UNKNOWN, result.getPlacements().get(0).getItem().getItemCategory());
		assertFalse(result.getPlacements().get(0).getItem().hasTag("forged-tag"));
	}

	@Test
	public void locksMustBeRespectedExactly()
	{
		LayoutRequest request = request(entry(10, 0).withLockedTarget(1), entry(20, 1));

		LayoutResult violated = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(20, 1, 1)));
		assertConflictType(violated, LayoutConflict.Type.PLAN_LOCK_VIOLATION);

		LayoutResult respected = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 1), placement(20, 1, 0)));
		assertTrue(respected.isSuccess());
	}

	@Test
	public void nullFillerAndBlankPlanItemsAreTypedConflicts()
	{
		LayoutRequest request = request(entry(10, 0));

		LayoutResult withNull = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), null));
		assertConflictType(withNull, LayoutConflict.Type.NULL_ENTRY);

		LayoutResult withFiller = LayoutPlanValidator.validate(request,
			Arrays.asList(new LayoutPlacement(new BankPreviewItem(ItemID.BANK_FILLER, "Bank filler", 1), 0)));
		assertConflictType(withFiller, LayoutConflict.Type.BANK_FILLER_ITEM);

		LayoutResult withBlank = LayoutPlanValidator.validate(request,
			Arrays.asList(new LayoutPlacement(BankPreviewItem.blank(), 0)));
		assertConflictType(withBlank, LayoutConflict.Type.BLANK_ITEM);
	}

	@Test
	public void invalidRequestShortCircuitsWithoutPartialResult()
	{
		LayoutRequest request = request(entry(10, 0), entry(10, 1));
		LayoutResult result = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(10, 1, 1)));

		assertFalse(result.isSuccess());
		assertEquals(LayoutConflict.Type.DUPLICATE_ITEM_ID, result.getConflicts().get(0).getType());
		assertTrue(result.getPlacements().isEmpty());
	}

	@Test
	public void conflictedResultNeverContainsPlacements()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		LayoutResult result = LayoutPlanValidator.validate(request,
			Arrays.asList(placement(10, 1, 0), placement(20, 1, 2)));

		assertFalse(result.isSuccess());
		assertTrue(result.getPlacements().isEmpty());
	}

	@Test
	public void inputPlanIsNotMutatedAndResultCollectionsAreImmutable()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1));
		List<LayoutPlacement> plan = new ArrayList<>(Arrays.asList(placement(20, 1, 1), placement(10, 1, 0)));
		LayoutResult result = LayoutPlanValidator.validate(request, plan);

		assertTrue(result.isSuccess());
		assertEquals(20, plan.get(0).getItem().getItemId());
		assertEquals(10, plan.get(1).getItem().getItemId());

		try
		{
			result.getPlacements().add(placement(30, 1, 2));
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}

		try
		{
			result.getConflicts().add(new LayoutConflict(LayoutConflict.Type.NULL_ENTRY, LayoutConflict.NO_ITEM,
				"extra"));
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	@Test
	public void repeatedAndReversedPlanOrderYieldTheSameOutcome()
	{
		LayoutRequest request = request(entry(10, 0), entry(20, 1), entry(30, 2));
		List<LayoutPlacement> plan = Arrays.asList(placement(10, 1, 2), placement(20, 1, 0), placement(30, 1, 1));
		List<LayoutPlacement> reversed = new ArrayList<>(plan);
		Collections.reverse(reversed);

		LayoutResult first = LayoutPlanValidator.validate(request, plan);
		LayoutResult second = LayoutPlanValidator.validate(request, plan);
		LayoutResult reversedResult = LayoutPlanValidator.validate(request, reversed);

		assertTrue(first.isSuccess());
		assertEquals(targetOrder(first), targetOrder(second));
		assertEquals(targetOrder(first), targetOrder(reversedResult));

		// The same holds for a conflicted plan: reversed input reports the same typed conflicts.
		List<LayoutPlacement> badPlan = Arrays.asList(placement(10, 1, 0), placement(20, 1, 0), placement(99, 1, 1));
		List<LayoutPlacement> badReversed = new ArrayList<>(badPlan);
		Collections.reverse(badReversed);

		LayoutResult badResult = LayoutPlanValidator.validate(request, badPlan);
		LayoutResult badReversedResult = LayoutPlanValidator.validate(request, badReversed);
		assertEquals(badResult.getConflicts(), badReversedResult.getConflicts());
	}

	@Test
	public void duplicateLockedItemIsExactlyOrderIndependent()
	{
		LayoutRequest request = request(entry(10, 0).withLockedTarget(0), entry(20, 1));
		List<LayoutPlacement> forwardPlan = Arrays.asList(placement(10, 1, 0), placement(10, 1, 1));
		List<LayoutPlacement> reversedPlan = new ArrayList<>(forwardPlan);
		Collections.reverse(reversedPlan);

		LayoutResult forward = LayoutPlanValidator.validate(request, forwardPlan);
		LayoutResult reversed = LayoutPlanValidator.validate(request, reversedPlan);

		assertEquals(forward.getConflicts(), reversed.getConflicts());
		assertEquals(2, forward.getConflicts().size());
		assertEquals(LayoutConflict.Type.PLAN_DUPLICATE_ITEM, forward.getConflicts().get(0).getType());
		assertEquals(LayoutConflict.Type.PLAN_MISSING_ITEM, forward.getConflicts().get(1).getType());
		assertEquals(20, forward.getConflicts().get(1).getItemId());
	}

	@Test
	public void multipleQuantityMismatchesAreExactlyOrderIndependent()
	{
		LayoutRequest request = request(entry(10, 0, 7), entry(20, 1, 9));
		List<LayoutPlacement> forwardPlan = Arrays.asList(placement(10, 6, 0), placement(20, 8, 1));
		List<LayoutPlacement> reversedPlan = new ArrayList<>(forwardPlan);
		Collections.reverse(reversedPlan);

		LayoutResult forward = LayoutPlanValidator.validate(request, forwardPlan);
		LayoutResult reversed = LayoutPlanValidator.validate(request, reversedPlan);

		assertEquals(forward.getConflicts(), reversed.getConflicts());
		assertEquals(2, forward.getConflicts().size());
		assertEquals(LayoutConflict.Type.PLAN_QUANTITY_MISMATCH, forward.getConflicts().get(0).getType());
		assertEquals(LayoutConflict.Type.PLAN_QUANTITY_MISMATCH, forward.getConflicts().get(1).getType());
	}

	@Test
	public void resultFactoriesRemainNonPublicValidationBoundaries() throws Exception
	{
		String source = new String(Files.readAllBytes(Paths.get(
			"src/main/java/com/pkoka5/ironmanbankarchitect/organize/layout/LayoutResult.java")),
			StandardCharsets.UTF_8);

		assertFalse(Pattern.compile("\\bpublic\\s+static\\s+LayoutResult\\s+success\\s*\\(")
			.matcher(source).find());
		assertFalse(Pattern.compile("\\bpublic\\s+static\\s+LayoutResult\\s+conflict\\s*\\(")
			.matcher(source).find());
	}

	private static List<Integer> targetOrder(LayoutResult result)
	{
		List<Integer> itemIds = new ArrayList<>();
		for (LayoutPlacement placement : result.getPlacements())
		{
			itemIds.add(placement.getItem().getItemId());
		}
		return itemIds;
	}

	private static void assertConflictType(LayoutResult result, LayoutConflict.Type type)
	{
		assertFalse(result.isSuccess());
		assertTrue(result.getPlacements().isEmpty());
		for (LayoutConflict conflict : result.getConflicts())
		{
			if (conflict.getType() == type)
			{
				return;
			}
		}
		fail("expected conflict " + type + " in " + result.getConflicts());
	}

	private static LayoutRequest request(LayoutEntry... entries)
	{
		return new LayoutRequest(Arrays.asList(entries), Collections.emptyList());
	}

	private static LayoutEntry entry(int itemId, int sourceFlatBankSlot)
	{
		return entry(itemId, sourceFlatBankSlot, 1);
	}

	private static LayoutEntry entry(int itemId, int sourceFlatBankSlot, int quantity)
	{
		return LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, quantity), sourceFlatBankSlot);
	}

	private static LayoutPlacement placement(int itemId, int quantity, int targetIndex)
	{
		return new LayoutPlacement(new BankPreviewItem(itemId, "Item " + itemId, quantity), targetIndex);
	}

	private static BankPreviewItem placeholderItem(int itemId)
	{
		CatalogItem catalogItem = new CatalogItem(itemId, "Item " + itemId, ItemCategory.UNKNOWN, "unknown",
			Collections.emptySet(), null);
		return new BankPreviewItem(catalogItem, 0, true);
	}
}

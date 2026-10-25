package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Change accounting compares physical ID/destination/tag multisets, independently of stack contents. */
public class BlueprintChangesTest
{
	@Test public void unchangedCapturedPreviewHasNoChangesAfterRoundTrip()
	{
		BankOrganizationPreview bank = preview(tab(item(1, "Coins", "currency"), item(2, "Hammer", "tools")),
			tab(item(3, "Rune platebody", "gear")));
		String reference = BlueprintChanges.capture(bank);
		assertTrue(reference.startsWith("v1|"));
		BlueprintChanges comparison = BlueprintChanges.compare(reference, bank);
		assertTrue(comparison.hasReference());
		assertTrue(comparison.isSupported());
		assertCounts(comparison, 0, 0, 0);
		assertNotNull(comparison.report());
	}

	@Test public void addingAnItemBeforeExistingItemsDoesNotTurnTheirAbsoluteOffsetsIntoChanges()
	{
		BankOrganizationPreview before = preview(tab(item(1, "A", "gear"), item(2, "B", "gear")));
		BankOrganizationPreview after = preview(tab(item(3, "New", "gear"), item(2, "B", "gear"), item(1, "A", "gear")));
		assertCounts(compare(before, after), 0, 1, 0);
	}

	@Test public void missingAndNewIdsAreCountedSeparatelyFromDestinationChanges()
	{
		BankOrganizationPreview before = preview(tab(item(1, "Moved", "gear"), item(2, "Gone", "gear")));
		BankOrganizationPreview after = preview(tab(item(3, "Added", "tools")), tab(item(1, "Moved", "gear")));
		assertCounts(compare(before, after), 1, 1, 1);
	}

	@Test public void changingATagWithinTheSamePhysicalTabStillCountsAsOneChange()
	{
		BankOrganizationPreview before = preview(tab(item(1, "Rune helm", "gear")));
		BankOrganizationPreview after = preview(tab(item(1, "Rune helm", "alch")));
		assertCounts(compare(before, after), 1, 0, 0);
	}

	@Test public void changingBothDestinationAndTagCountsOnePhysicalItemOnce()
	{
		BankOrganizationPreview before = preview(tab(item(1, "Staff", "gear")), tab());
		BankOrganizationPreview after = preview(tab(), tab(item(1, "Staff", "tools")));
		assertCounts(compare(before, after), 1, 0, 0);
	}

	@Test public void quantitiesAndPlaceholderStateDoNotChangeAnExistingPlacement()
	{
		BankOrganizationPreview owned = preview(tab(item(1, "Stock", "gear", 25)));
		BankOrganizationPreview single = preview(tab(item(1, "Stock", "gear", 1)));
		BankOrganizationPreview withdrawn = preview(tab(item(1, "Stock", "gear", 0)));
		assertCounts(compare(owned, single), 0, 0, 0);
		assertCounts(compare(owned, withdrawn), 0, 0, 0);
		assertCounts(compare(withdrawn, owned), 0, 0, 0);
	}

	@Test public void physicalDuplicateCopiesAreNotCollapsedIntoOneStackOrOccurrenceOrdinal()
	{
		BankOrganizationPreview twoCopies = preview(tab(item(7, "Charged staff", "gear").withBlueprintOccurrence(0),
			item(7, "Charged staff", "gear").withBlueprintOccurrence(1)));
		BankOrganizationPreview oneStack = preview(tab(item(7, "Charged staff", "gear", 2).withBlueprintOccurrence(9)));
		assertCounts(compare(twoCopies, oneStack), 0, 0, 1);
		assertCounts(compare(oneStack, twoCopies), 0, 1, 0);
	}

	@Test public void exactDuplicatePlacementsMatchBeforeSameIdCopiesArePairedAsChanges()
	{
		BankOrganizationPreview before = preview(tab(item(7, "Staff", "gear"), item(7, "Staff", "gear")),
			tab(item(7, "Staff", "tools")), tab());
		BankOrganizationPreview after = preview(tab(item(7, "Staff", "gear")), tab(item(7, "Staff", "tools")),
			tab(item(7, "Staff", "teleports").withPlacementReason("Saved destination fixture")));
		BlueprintChanges comparison = compare(before, after);
		assertCounts(comparison, 1, 0, 0);
		assertTrue(comparison.report().contains("Staff"));
		assertTrue(comparison.report().contains("Saved destination fixture"));
	}

	@Test public void duplicateSurplusCountsOnlyUnmatchedPhysicalCopiesAsAddedOrMissing()
	{
		BankOrganizationPreview before = preview(tab(item(7, "Staff", "gear"), item(7, "Staff", "gear")),
			tab(item(7, "Staff", "tools")), tab());
		BankOrganizationPreview after = preview(tab(item(7, "Staff", "gear")), tab(),
			tab(item(7, "Staff", "teleports"), item(9, "New item", "cosmetics")));
		assertCounts(compare(before, after), 1, 1, 1);
		assertCounts(compare(after, before), 1, 1, 1);
	}

	@Test public void reorderedDuplicateOccurrencesDoNotCreateArtificialMoves()
	{
		BankOrganizationPreview before = preview(tab(item(7, "Staff", "gear").withBlueprintOccurrence(0)),
			tab(item(7, "Staff", "tools").withBlueprintOccurrence(1)));
		BankOrganizationPreview after = preview(tab(item(7, "Staff", "gear").withBlueprintOccurrence(9)),
			tab(item(7, "Staff", "tools").withBlueprintOccurrence(0)));
		assertCounts(compare(before, after), 0, 0, 0);
	}

	@Test public void blanksDoNotBecomeSnapshotEntriesOrArtificialChanges()
	{
		BankOrganizationPreview before = preview(tab(BankPreviewItem.blank(), item(1, "Item", "gear")));
		BankOrganizationPreview after = preview(tab(item(1, "Item", "gear"), BankPreviewItem.blank(), BankPreviewItem.blank()));
		assertEquals("v1|1:0:gear", BlueprintChanges.capture(before));
		assertCounts(compare(before, after), 0, 0, 0);
		assertEquals("v1", BlueprintChanges.capture(preview(tab(BankPreviewItem.blank()))));
	}

	@Test public void captureStoresOnlyTheVersionAndPhysicalIdDestinationTagFacts()
	{
		BankPreviewItem item = item(7, "Secret fixture name", "gear", 25).withBlueprintOccurrence(9)
			.withPlacementReason("Current provenance fixture");
		assertEquals("v1|7:0:gear", BlueprintChanges.capture(preview(tab(item))));
	}

	@Test public void changedRowsUseTheCurrentItemNameAndPlacementReason()
	{
		BankOrganizationPreview before = preview(tab(item(7, "Old name", "gear").withPlacementReason("Old reason")), tab());
		BankOrganizationPreview after = preview(tab(), tab(item(7, "Current fixture name", "tools")
			.withPlacementReason("Manual classification fixture")));
		BlueprintChanges changes = compare(before, after);
		assertCounts(changes, 1, 0, 0);
		assertTrue(changes.report().contains("Current fixture name"));
		assertTrue(changes.report().contains("Manual classification fixture"));
		assertFalse(changes.report().contains("Old reason"));
	}

	@Test public void nameAndReasonMetadataChangesAloneAreNotPlacementChanges()
	{
		BankOrganizationPreview before = preview(tab(item(7, "Old name", "gear").withPlacementReason("Old reason")));
		BankOrganizationPreview after = preview(tab(item(7, "New name", "gear").withPlacementReason("New reason")));
		assertCounts(compare(before, after), 0, 0, 0);
	}

	@Test public void absentReferencesAreSupportedButDoNotInventAnEmptyCapturedBank()
	{
		BankOrganizationPreview current = preview(tab(item(7, "Item", "gear")));
		for (String reference : new String[] {null, ""})
		{
			BlueprintChanges comparison = BlueprintChanges.compare(reference, current);
			assertFalse(comparison.hasReference());
			assertTrue(comparison.isSupported());
			assertCounts(comparison, 0, 0, 0);
			assertNotNull(comparison.report());
		}
	}

	@Test public void aCapturedEmptyBankIsAnActualReferenceAndCanGainOrLoseItems()
	{
		String empty = BlueprintChanges.capture(preview());
		assertEquals("v1", empty);
		BlueprintChanges added = BlueprintChanges.compare(empty, preview(tab(item(7, "Item", "gear"))));
		assertTrue(added.hasReference());
		assertTrue(added.isSupported());
		assertCounts(added, 0, 1, 0);
		assertCounts(compare(preview(tab(item(7, "Item", "gear"))), preview()), 0, 0, 1);
	}

	@Test public void nullTagsRoundTripUsingTheExplicitUnderscoreEncoding()
	{
		BankOrganizationPreview bank = preview(tab(item(7, "Unassigned", null)));
		assertEquals("v1|7:0:_", BlueprintChanges.capture(bank));
		assertCounts(compare(bank, bank), 0, 0, 0);
		assertCounts(compare(bank, preview(tab(item(7, "Unassigned", "gear")))), 1, 0, 0);
	}

	@Test public void malformedOrFutureReferencesFailAsAWholeWithoutPartialResults()
	{
		BankOrganizationPreview current = preview(tab(item(7, "Item", "gear")));
		for (String reference : new String[] {"v2|7:0:gear", "v99", " ", "v1|", "v1|bad",
			"v1|7:0:gear|bad", "v1|0:0:gear", "v1|-7:0:gear", "v1|7:-1:gear", "v1|7:10:gear",
			"v1|7:0:unknown-tag", "v1|7:0:", "v1|7:x:gear", "v1|7:0:gear:extra"})
		{
			BlueprintChanges comparison = BlueprintChanges.compare(reference, current);
			assertFalse("Reject unsupported input " + reference, comparison.isSupported());
			assertCounts(comparison, 0, 0, 0);
			assertNotNull(comparison.report());
		}
	}

	@Test public void physicalSnapshotCapacityIsBoundedWithoutSilentlyDroppingCopies()
	{
		String full = "v1|" + String.join("|", Collections.nCopies(BlueprintItemOrders.BANK_CAPACITY, "7:0:gear"));
		BlueprintChanges accepted = BlueprintChanges.compare(full, preview());
		assertTrue(accepted.hasReference());
		assertTrue(accepted.isSupported());
		assertCounts(accepted, 0, 0, BlueprintItemOrders.BANK_CAPACITY);
		BlueprintChanges rejected = BlueprintChanges.compare(full + "|7:0:gear", preview());
		assertFalse(rejected.isSupported());
		assertCounts(rejected, 0, 0, 0);
		BankPreviewItem[] tooMany = Collections.nCopies(BlueprintItemOrders.BANK_CAPACITY + 1,
			item(7, "Staff", "gear")).toArray(new BankPreviewItem[0]);
		assertThrows(IllegalArgumentException.class, () -> BlueprintChanges.capture(preview(tooMany)));
	}

	@Test public void captureCompareAndReportNeverMutateTheInputPreviewOrItsMetadata()
	{
		BankPreviewItem first = item(7, "Staff", "gear").withBlueprintOccurrence(1).withPlacementReason("Keep this reason");
		BankOrganizationPreview before = preview(tab(first, item(7, "Staff", "gear")));
		BankOrganizationPreview after = preview(tab(item(9, "New", "tools")), tab(first.withLayoutTag("tools")));
		String oldCapture = BlueprintChanges.capture(before), newCapture = BlueprintChanges.capture(after);
		String oldText = before.toPreviewText(), newText = after.toPreviewText();
		BlueprintChanges changes = BlueprintChanges.compare(oldCapture, after);
		String report = changes.report();
		assertEquals(report, changes.report());
		assertEquals(oldCapture, BlueprintChanges.capture(before));
		assertEquals(newCapture, BlueprintChanges.capture(after));
		assertEquals(oldText, before.toPreviewText());
		assertEquals(newText, after.toPreviewText());
		assertEquals("gear", first.getLayoutTagKey());
		assertEquals(1, first.getBlueprintOccurrence());
		assertEquals("Keep this reason", first.getPlacementReason());
	}

	private static BlueprintChanges compare(BankOrganizationPreview before, BankOrganizationPreview after)
	{
		return BlueprintChanges.compare(BlueprintChanges.capture(before), after);
	}

	private static void assertCounts(BlueprintChanges changes, int changed, int added, int missing)
	{
		assertEquals("Changed physical placements", changed, changes.changedCount());
		assertEquals("New physical entries", added, changes.addedCount());
		assertEquals("Missing physical entries", missing, changes.missingCount());
	}

	private static BankPreviewItem item(int id, String name, String tag) { return item(id, name, tag, 1); }
	private static BankPreviewItem item(int id, String name, String tag, int quantity)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.GEAR, "gear", Collections.emptySet(), null),
			quantity, quantity == 0).withLayoutTag(tag);
	}
	private static BankPreviewItem[] tab(BankPreviewItem... items) { return items; }
	private static BankOrganizationPreview preview(BankPreviewItem[]... tabs)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < tabs.length; index++)
			categories.add(new BankCategoryPreview(new BankCategory("test-" + index, "Tab " + index,
				BankCategorySortMode.GENERIC), Arrays.asList(tabs[index])));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}
}

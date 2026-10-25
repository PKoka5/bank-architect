package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Relative-order warnings must separate deliberate rearrangement from changing bank contents. */
public class BlueprintRelativeOrderTest
{
	@Test public void swappingExistingItemsReportsAnOrderChangeWithoutInventingDestinationChanges()
	{
		BlueprintChanges changes = compare(bank(tab(1, 2, 3)), bank(tab(2, 1, 3)));
		assertCounts(changes, 1, 0, 0, 0);
		assertTrue("The affected Main section is named", changes.report().contains("Main"));
	}

	@Test public void eachReorderedTabCountsOnceRegardlessOfHowManyItemsMove()
	{
		BankOrganizationPreview before = bank(tab(1, 2), tab(3, 4, 5, 6), tab(7, 8, 9));
		BankOrganizationPreview after = bank(tab(1, 2), tab(6, 5, 4, 3), tab(8, 9, 7));
		assertCounts(compare(before, after), 2, 0, 0, 0);
		assertCounts(compare(after, before), 2, 0, 0, 0);
	}

	@Test public void newItemsInsertedBeforeAndBetweenExistingItemsDoNotReorderTheSurvivors()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 3));
		BankOrganizationPreview after = bank(tab(4, 1, 5, 2, 6, 3));
		assertCounts(compare(before, after), 0, 0, 3, 0);
		assertCounts(compare(after, before), 0, 0, 0, 3);
	}

	@Test public void withdrawingInterleavedItemsDoesNotTurnCompactionIntoAReorder()
	{
		BankOrganizationPreview before = bank(tab(1, 4, 2, 5, 3));
		BankOrganizationPreview after = bank(tab(1, 2, 3));
		assertCounts(compare(before, after), 0, 0, 0, 2);
	}

	@Test public void insertingAnInterchangeableDuplicateDoesNotInventAReorderInEitherDirection()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 1));
		BankOrganizationPreview after = bank(tab(1, 1, 2, 1));
		assertCounts(compare(before, after), 0, 0, 1, 0);
		assertCounts(compare(after, before), 0, 0, 0, 1);
	}

	@Test public void simultaneousDuplicateAdditionAndWithdrawalCanPreserveAllCommonOrder()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 1, 3));
		BankOrganizationPreview after = bank(tab(2, 1, 3, 3));
		assertCounts(compare(before, after), 0, 0, 1, 1);
		assertCounts(compare(after, before), 0, 0, 1, 1);
	}

	@Test public void movingADuplicateAcrossAnotherItemStillReportsATrueReorder()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 1));
		BankOrganizationPreview after = bank(tab(1, 1, 2));
		assertCounts(compare(before, after), 1, 0, 0, 0);
		assertCounts(compare(after, before), 1, 0, 0, 0);
	}

	@Test public void additionsAndWithdrawalsDoNotHideAReorderOfTheCommonItems()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 1, 3));
		BankOrganizationPreview after = bank(tab(2, 2, 3, 1));
		assertCounts(compare(before, after), 1, 0, 1, 1);
		assertCounts(compare(after, before), 1, 0, 1, 1);
	}

	@Test public void changingATagAtTheSamePositionDoesNotAlsoReportAReorder()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 3));
		BankOrganizationPreview after = bank(new BankPreviewItem[] {
			item(1), item(2).withLayoutTag("alch"), item(3)});
		assertCounts(compare(before, after), 0, 1, 0, 0);
	}

	@Test public void quantitiesPlaceholderStateAndOccurrenceOrdinalsAreNotOrder()
	{
		BankOrganizationPreview before = bank(new BankPreviewItem[] {
			item(1).withBlueprintOccurrence(0), item(2).withBlueprintOccurrence(0)});
		BankOrganizationPreview after = bank(new BankPreviewItem[] {
			new BankPreviewItem(itemCatalog(1), 0, true).withLayoutTag("gear").withBlueprintOccurrence(9),
			new BankPreviewItem(itemCatalog(2), 200).withLayoutTag("gear").withBlueprintOccurrence(4)});
		assertCounts(compare(before, after), 0, 0, 0, 0);
	}

	@Test public void changingOnlyVirtualBlankGeometryDoesNotReorderBankItems()
	{
		BankOrganizationPreview before = bank(new BankPreviewItem[] {
			BankPreviewItem.blank(), item(1), item(2), BankPreviewItem.blank(), item(3)});
		BankOrganizationPreview after = bank(new BankPreviewItem[] {
			item(1), BankPreviewItem.blank(), item(2), item(3), BankPreviewItem.blank()});
		assertCounts(compare(before, after), 0, 0, 0, 0);
	}

	@Test public void movingAnItemToAnotherTabDoesNotReorderTheItemsThatStay()
	{
		BankOrganizationPreview before = bank(tab(1, 2, 3), tab(4, 5));
		BankOrganizationPreview after = bank(tab(2, 3), tab(4, 1, 5));
		assertCounts(compare(before, after), 0, 1, 0, 0);
		assertCounts(compare(after, before), 0, 1, 0, 0);
	}

	@Test public void replacingAllContentsOfATabHasNoCommonOrderToRearrange()
	{
		assertCounts(compare(bank(tab(1, 2)), bank(tab(3, 4))), 0, 0, 2, 2);
		assertCounts(compare(bank(tab()), bank(tab(3, 4))), 0, 0, 2, 0);
		assertCounts(compare(bank(tab(1, 2)), bank(tab())), 0, 0, 0, 2);
	}

	@Test public void invalidOrFutureReferencesCannotLeakPartialOrderResults()
	{
		BankOrganizationPreview current = bank(tab(2, 1));
		for (String reference : new String[] {"v2|1:0:gear|2:0:gear", "v1|1:0:gear|2:0:gear|bad",
			"v1|1:0:gear|2:10:gear", "v1|1:0:gear|2:0:missing-tag"})
		{
			BlueprintChanges changes = BlueprintChanges.compare(reference, current);
			assertFalse("Reject the entire reference " + reference, changes.isSupported());
			assertCounts(changes, 0, 0, 0, 0);
		}
	}

	@Test public void noReferenceDoesNotTreatTheCurrentOrderAsANewRearrangement()
	{
		for (String reference : new String[] {null, ""})
		{
			BlueprintChanges changes = BlueprintChanges.compare(reference, bank(tab(2, 1)));
			assertFalse(changes.hasReference());
			assertCounts(changes, 0, 0, 0, 0);
		}
	}

	private static BlueprintChanges compare(BankOrganizationPreview before, BankOrganizationPreview after)
	{
		return BlueprintChanges.compare(BlueprintChanges.capture(before), after);
	}

	private static void assertCounts(BlueprintChanges changes, int reordered, int destinations, int added, int missing)
	{
		assertEquals("Tabs with changed relative order", reordered, changes.reorderedTabCount());
		assertEquals("Changed item destinations or categories", destinations, changes.changedCount());
		assertEquals("Added physical entries", added, changes.addedCount());
		assertEquals("Missing physical entries", missing, changes.missingCount());
	}

	private static BankPreviewItem item(int id) { return new BankPreviewItem(id, "Item " + id, 1).withLayoutTag("gear"); }
	private static CatalogItem itemCatalog(int id)
	{
		return new CatalogItem(id, "Item " + id, ItemCategory.GEAR, "gear", Collections.emptySet(), null);
	}
	private static BankPreviewItem[] tab(int... ids)
	{
		return Arrays.stream(ids).mapToObj(BlueprintRelativeOrderTest::item).toArray(BankPreviewItem[]::new);
	}
	private static BankOrganizationPreview bank(BankPreviewItem[]... tabs)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < tabs.length; index++)
			categories.add(new BankCategoryPreview(new BankCategory("relative-order-" + index, "Tab " + index,
				BankCategorySortMode.GENERIC), Arrays.asList(tabs[index])));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}
}

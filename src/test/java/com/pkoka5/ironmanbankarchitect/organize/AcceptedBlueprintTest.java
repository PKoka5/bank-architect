package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogUnavailableException;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

/** Accepted targets are local snapshots; applying one must conserve the current bank's real entries. */
public class AcceptedBlueprintTest
{
	private static final String CONTEXT = "Ironman|Personal|gear+tools|fixed-alch";
	private static final BankLayoutPlan PLAN = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);

	@Test public void matchingContextKeepsAcceptedOrderTagsAndTabsAcrossNewRecommendations()
	{
		BankOrganizationPreview accepted = bank(tab(item(1, "gear"), item(2, "tools")), tab(item(3, "alch")));
		String saved = AcceptedBlueprint.capture(accepted, CONTEXT);
		BankOrganizationPreview proposal = bank(tab(item(3, "gear"), item(2, "alch")), tab(item(1, "tools")));
		BankOrganizationPreview result = apply(saved, proposal);
		assertTrue(AcceptedBlueprint.isSupported(saved));
		assertTab(result, 0, 1, 2);
		assertTab(result, 1, 3);
		assertEquals("gear", result.getCategories().get(0).getItems().get(0).getLayoutTagKey());
		assertEquals("tools", result.getCategories().get(0).getItems().get(1).getLayoutTagKey());
		assertEquals("alch", result.getCategories().get(1).getItems().get(0).getLayoutTagKey());
		assertEquals(3, result.getPlannedItemCount());
		assertTrue(AcceptedBlueprint.description(saved, CONTEXT).contains("active"));
	}

	@Test public void newItemsAppendInTheirCurrentRecommendedOrderToTheirRecommendedTabs()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(1, "gear")), tab(item(2, "tools"))), CONTEXT);
		BankOrganizationPreview proposal = bank(tab(item(9, "currency"), item(4, "gear"), item(1, "alch")),
			tab(item(8, "tools"), item(2, "gear"), item(3, "tools")), tab(item(7, "food")));
		BankOrganizationPreview result = apply(saved, proposal);
		assertTab(result, 0, 1, 9, 4);
		assertTab(result, 1, 2, 8, 3);
		assertTab(result, 2, 7);
		assertEquals(7, result.getPlannedItemCount());
	}

	@Test public void absentItemsRemainDormantAndReturnWithoutRewritingTheStoredSnapshot()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(1, "gear"), item(2, "gear"), item(3, "gear")),
			tab(item(4, "tools"))), CONTEXT);
		BankOrganizationPreview absent = apply(saved, bank(tab(item(3, "alch"), item(1, "tools")), tab()));
		assertTab(absent, 0, 1, 3);
		assertTab(absent, 1);
		BankOrganizationPreview returned = apply(saved, bank(tab(item(4, "gear"), item(3, "alch")),
			tab(item(2, "tools"), item(1, "gear"))));
		assertTab(returned, 0, 1, 2, 3);
		assertTab(returned, 1, 4);
		assertTrue(AcceptedBlueprint.isSupported(saved));
	}

	@Test public void currentQuantitiesPlaceholderStateNamesAndCatalogMetadataAreNotFrozen()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(1, "gear"), item(2, "tools"))), CONTEXT);
		CatalogItem changedCatalog = new CatalogItem(1, "Current fixture name", ItemCategory.SKILLING,
			"updated-resource", Collections.singleton("current-usage"), null);
		BankPreviewItem placeholder = new BankPreviewItem(changedCatalog, 0, true).withLayoutTag("alch");
		BankPreviewItem stock = new BankPreviewItem(catalog(2), 125).withLayoutTag("gear");
		BankOrganizationPreview result = apply(saved, bank(tab(stock, placeholder)));
		BankPreviewItem first = result.getCategories().get(0).getItems().get(0);
		BankPreviewItem second = result.getCategories().get(0).getItems().get(1);
		assertEquals(0, first.getQuantity());
		assertTrue(first.isPlaceholder());
		assertEquals("Current fixture name", first.getDisplayName());
		assertEquals(ItemCategory.SKILLING, first.getItemCategory());
		assertEquals("updated-resource", first.getSubcategory());
		assertTrue(first.getUsageTags().contains("current-usage"));
		assertEquals("gear", first.getLayoutTagKey());
		assertEquals(125, second.getQuantity());
		assertFalse(second.isPlaceholder());
		assertEquals("tools", second.getLayoutTagKey());
	}

	@Test public void duplicatePhysicalCopiesUseNaturalMainFirstOrderAndKeepTheirOwnCurrentMetadata()
	{
		BankOrganizationPreview accepted = bank(tab(item(7, "gear").withBlueprintOccurrence(8)),
			tab(item(7, "tools").withBlueprintOccurrence(2)));
		String saved = AcceptedBlueprint.capture(accepted, CONTEXT);
		BankPreviewItem placeholder = new BankPreviewItem(catalog(7), 0, true)
			.withLayoutTag("alch").withBlueprintOccurrence(15);
		BankPreviewItem owned = new BankPreviewItem(catalog(7), 12)
			.withLayoutTag("gear").withBlueprintOccurrence(0);
		BankOrganizationPreview result = apply(saved, bank(tab(placeholder), tab(owned)));
		assertTab(result, 0, 7);
		assertTab(result, 1, 7);
		BankPreviewItem main = result.getCategories().get(0).getItems().get(0);
		BankPreviewItem numbered = result.getCategories().get(1).getItems().get(0);
		assertTrue(main.isPlaceholder());
		assertEquals(0, main.getQuantity());
		assertEquals("gear", main.getLayoutTagKey());
		assertFalse(numbered.isPlaceholder());
		assertEquals(12, numbered.getQuantity());
		assertEquals("tools", numbered.getLayoutTagKey());
		assertEquals(2, result.getPlannedItemCount());
	}

	@Test public void duplicateSurplusIsConservedAsANewEntryAtItsRecommendedDestination()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(7, "gear")), tab(item(7, "tools"))), CONTEXT);
		BankOrganizationPreview result = apply(saved, bank(tab(item(7, "alch"), item(7, "alch"), item(7, "alch")), tab()));
		assertTab(result, 0, 7, 7);
		assertTab(result, 1, 7);
		assertEquals("gear", result.getCategories().get(0).getItems().get(0).getLayoutTagKey());
		assertEquals("alch", result.getCategories().get(0).getItems().get(1).getLayoutTagKey());
		assertEquals(3, result.getPlannedItemCount());
	}

	@Test public void changingPersonalContextPausesTheSnapshotAndReturningToItRestoresTheSnapshot()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(1, "gear"), item(2, "gear"))), CONTEXT);
		BankOrganizationPreview proposal = bank(tab(item(2, "alch"), item(1, "tools")));
		String changed = CONTEXT + "|manual-tools-choice";
		assertSame(proposal, AcceptedBlueprint.apply(saved, changed, proposal, PLAN));
		assertTrue(AcceptedBlueprint.description(saved, changed).contains("paused"));
		assertTrue(AcceptedBlueprint.isSupported(saved));
		assertTab(apply(saved, proposal), 0, 1, 2);
		assertTrue(AcceptedBlueprint.description(saved, CONTEXT).contains("active"));
	}

	@Test public void emptyValueLeavesAutomaticBlueprintUntouched()
	{
		BankOrganizationPreview proposal = bank(tab(item(2, "gear"), item(1, "tools")));
		assertTrue(AcceptedBlueprint.isSupported(""));
		assertSame(proposal, apply("", proposal));
		assertTrue(AcceptedBlueprint.description("", CONTEXT).contains("no accepted snapshot"));
	}

	@Test public void acceptingAnEmptyBankIsValidAndFutureItemsStillUseTheirRecommendation()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(), tab()), CONTEXT);
		assertFalse(saved.isEmpty());
		assertTrue(AcceptedBlueprint.isSupported(saved));
		assertTrue(AcceptedBlueprint.description(saved, CONTEXT).contains("active"));
		BankOrganizationPreview proposal = bank(tab(item(2, "gear")), tab(item(1, "tools")));
		assertSame(proposal, apply(saved, proposal));
	}

	@Test public void blankGeometryIsNotSavedAsPhysicalItemsOrReintroducedOnApply()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(BankPreviewItem.blank(), item(1, "gear"),
			BankPreviewItem.blank(), item(2, "tools"))), CONTEXT);
		BankOrganizationPreview result = apply(saved, bank(tab(item(2, "alch"), BankPreviewItem.blank(), item(1, "gear"))));
		assertTab(result, 0, 1, 2);
		assertFalse(result.getPlannedItems().stream().anyMatch(BankPreviewItem::isBlank));
	}

	@Test public void aDifferentKnownFormatRulesRevisionRemainsUsableAndIsDescribed()
	{
		String saved = AcceptedBlueprint.capture(bank(tab(item(1, "gear"), item(2, "gear"))), CONTEXT);
		String[] fields = saved.split("\\|", 4);
		String otherRevision = fields[0] + "|2|" + fields[2] + "|" + fields[3];
		assertTrue(AcceptedBlueprint.isSupported(otherRevision));
		assertTab(apply(otherRevision, bank(tab(item(2, "tools"), item(1, "alch")))), 0, 1, 2);
		String description = AcceptedBlueprint.description(otherRevision, CONTEXT);
		assertTrue(description.contains("Saved rules 2"));
		assertTrue(description.contains("current rules " + AcceptedBlueprint.RULES));
	}

	@Test public void deliberatelyAdoptingTheCurrentProposalReplacesTheOldTargetsIncludingAbsentItems()
	{
		String old = AcceptedBlueprint.capture(bank(tab(item(1, "gear"), item(2, "gear")), tab(item(3, "tools"))), CONTEXT);
		BankOrganizationPreview proposal = bank(tab(item(3, "alch"), item(1, "tools")), tab());
		String adopted = AcceptedBlueprint.capture(proposal, CONTEXT);
		BankOrganizationPreview later = bank(tab(item(1, "gear")), tab(item(2, "food"), item(3, "tools")));
		BankOrganizationPreview oldTarget = apply(old, later);
		BankOrganizationPreview newTarget = apply(adopted, later);
		assertTab(oldTarget, 0, 1, 2);
		assertTab(oldTarget, 1, 3);
		assertTab(newTarget, 0, 3, 1);
		assertTab(newTarget, 1, 2);
		assertNotEquals(old, adopted);
		assertTrue(AcceptedBlueprint.isSupported(old));
	}

	@Test public void captureAndApplyDoNotMutateTheAcceptedPreviewTheProposalOrTheirReasons()
	{
		BankPreviewItem source = item(1, "gear").withBlueprintOccurrence(7).withPlacementReason("Accepted source reason");
		BankPreviewItem current = item(1, "alch").withBlueprintOccurrence(2).withPlacementReason("Current proposal reason");
		BankOrganizationPreview accepted = bank(tab(source, item(2, "tools")));
		BankOrganizationPreview proposal = bank(tab(item(2, "gear"), current));
		String saved = AcceptedBlueprint.capture(accepted, CONTEXT);
		String beforeProposal = BlueprintChanges.capture(proposal);
		apply(saved, proposal);
		assertEquals(saved, AcceptedBlueprint.capture(accepted, CONTEXT));
		assertEquals(beforeProposal, BlueprintChanges.capture(proposal));
		assertEquals("gear", source.getLayoutTagKey());
		assertEquals(7, source.getBlueprintOccurrence());
		assertEquals("Accepted source reason", source.getPlacementReason());
		assertEquals("alch", current.getLayoutTagKey());
		assertEquals(2, current.getBlueprintOccurrence());
		assertEquals("Current proposal reason", current.getPlacementReason());
	}

	@Test public void corruptFutureOrTruncatedEnvelopesFailClosedEvenForChangedPersonalContext()
	{
		String valid = AcceptedBlueprint.capture(bank(tab(item(1, "gear"))), CONTEXT);
		String[] fields = valid.split("\\|", 4);
		for (String invalid : new String[] {
			"BAkeep2|" + fields[1] + "|" + fields[2] + "|" + fields[3],
			"BAkeep1|0|" + fields[2] + "|" + fields[3],
			"BAkeep1|-1|" + fields[2] + "|" + fields[3],
			"BAkeep1|1000000|" + fields[2] + "|" + fields[3],
			"BAkeep1|1|not-a-uuid|" + fields[3],
			"BAkeep1|1|AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA|" + fields[3],
			"BAkeep1|1|" + fields[2], "BAkeep1", " "})
			assertInvalid(invalid);
	}

	@Test public void malformedRouteTabItemAndDuplicateRecordsCannotPartiallyApply()
	{
		for (String payload : new String[] {
			"v99|0:1|c:1#0:0:gear:gear", "v2|", "v2|0:1", "v2|0:1|c:1#0:0:gear:gear|bad",
			"v2|0:1|c:1#0:0:gear:gear|", "v2|0:1|0:1|c:1#0:0:gear:gear",
			"v2|0:1|c:1#0:0:gear:gear|c:1#0:0:gear:gear",
			"v2|10:1|c:1#0:10:gear:gear", "v2|-1:1|c:1#0:-1:gear:gear",
			"v2|0:1|c:1#0:1:gear:gear", "v1|0:1|r:1#0:0:gear:gear",
			"v2|0:1|c:1#0:0:gear:alch", "v2|0:1|c:1#0:0:gear:missing-tag",
			"v2|0:1|c:1#0:0:gear:gear|c:2#0:0:gear:gear",
			"v2|0:1,1|c:1#0:0:gear:gear", "v2|0:1|c:1#-1:0:gear:gear",
			"v2|0:0|c:0#0:0:gear:gear", "v2|0:-1|c:-1#0:0:gear:gear",
			"v2|0:" + ItemID.BANK_FILLER + "|c:" + ItemID.BANK_FILLER + "#0:0:gear:gear"})
			assertInvalid(envelope(payload));
	}

	@Test public void oversizedSerializedValuesAreRefusedBeforePartialApplication()
	{
		assertInvalid(envelope(String.join("", Collections.nCopies(160001, "x"))));
	}

	@Test public void bankCapacityIsEnforcedAcrossAllTabsDuringCapture()
	{
		BankPreviewItem[] full = Collections.nCopies(BlueprintItemOrders.BANK_CAPACITY,
			item(7, "gear")).toArray(new BankPreviewItem[0]);
		String saved = AcceptedBlueprint.capture(bank(full), CONTEXT);
		assertTrue(AcceptedBlueprint.isSupported(saved));
		assertEquals(BlueprintItemOrders.BANK_CAPACITY, apply(saved, bank(full)).getPlannedItemCount());
		assertThrows(IllegalArgumentException.class,
			() -> AcceptedBlueprint.capture(bank(full, tab(item(8, "tools"))), CONTEXT));
	}

	@Test public void aSerializedSnapshotWithTooManyPhysicalEntriesIsRefused()
	{
		StringBuilder ids = new StringBuilder("v2|0:");
		StringBuilder routes = new StringBuilder();
		for (int index = 0; index <= BlueprintItemOrders.BANK_CAPACITY; index++)
		{
			int id = 10000 + index;
			if (index > 0) ids.append(',');
			ids.append(id);
			routes.append("|c:").append(id).append("#0:0:gear:gear");
		}
		assertInvalid(envelope(ids.append(routes).toString()));
	}

	private static void assertInvalid(String value)
	{
		BankOrganizationPreview proposal = bank(tab(item(2, "tools"), item(1, "gear")));
		String before = BlueprintChanges.capture(proposal);
		assertFalse("Reject the complete stored value", AcceptedBlueprint.isSupported(value));
		assertTrue(AcceptedBlueprint.description(value, CONTEXT).contains("preserved"));
		assertThrows(CatalogUnavailableException.class, () -> apply(value, proposal));
		assertThrows(CatalogUnavailableException.class,
			() -> AcceptedBlueprint.apply(value, CONTEXT + "|changed", proposal, PLAN));
		assertEquals(before, BlueprintChanges.capture(proposal));
	}

	private static String envelope(String payload)
	{
		return AcceptedBlueprint.capture(bank(tab()), CONTEXT) + payload;
	}
	private static BankOrganizationPreview apply(String value, BankOrganizationPreview proposal)
	{
		return AcceptedBlueprint.apply(value, CONTEXT, proposal, PLAN);
	}
	private static CatalogItem catalog(int id)
	{
		return new CatalogItem(id, "Item " + id, ItemCategory.GEAR, "gear", Collections.emptySet(), null);
	}
	private static BankPreviewItem item(int id, String tag)
	{
		return new BankPreviewItem(catalog(id), 1).withLayoutTag(tag);
	}
	private static BankPreviewItem[] tab(BankPreviewItem... items) { return items; }
	private static BankOrganizationPreview bank(BankPreviewItem[]... tabs)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < tabs.length; index++)
			categories.add(new BankCategoryPreview(new BankCategory("accepted-" + index, "Tab " + index,
				BankCategorySortMode.GENERIC), Arrays.asList(tabs[index])));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}
	private static void assertTab(BankOrganizationPreview preview, int tab, Integer... expected)
	{
		assertEquals("Physical item order in destination " + tab, Arrays.asList(expected),
			preview.getCategories().get(tab).getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
	}
}

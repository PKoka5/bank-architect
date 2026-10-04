package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class BankBlueprintCaptureTest
{
	private static final BankLayoutPlan PLAN = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);

	@Test public void mixedTagsAndAllNineTabsReloadAsTheExactPhysicalBank()
	{
		BankOrganizationPreview automatic = automatic(item(101, "gear"), item(102, "currency"),
			item(103, "food"), item(104, "tools"), item(105, "cleanup"), item(106, "gear"),
			item(107, "food"), item(108, "tools"), item(109, "currency"), item(110, "cleanup"),
			item(111, "food"), item(112, "gear"));
		List<BankCategoryPreview> categories = new ArrayList<>(automatic.getCategories());
		int gearTab = PLAN.destinationOf("gear");
		List<BankPreviewItem> withGeometry = new ArrayList<>(categories.get(gearTab).getItems());
		withGeometry.add(1, BankPreviewItem.blank());
		categories.set(gearTab, new BankCategoryPreview(categories.get(gearTab).getCategory(), withGeometry));
		automatic = new BankOrganizationPreview(BankPresets.IRONMAN, categories);
		Map<Integer, List<Integer>> physical = physical(new int[]{112, 109}, new int[]{104, 101},
			new int[]{102}, new int[]{103}, new int[]{105}, new int[]{106}, new int[]{107},
			new int[]{108}, new int[]{110}, new int[]{111});

		BlueprintItemOrders captured = BlueprintItemOrders.capture(physical, automatic, BlueprintItemOrders.EMPTY);
		assertTrue(captured.isCaptured());
		assertTrue(captured.serialize().startsWith("v2|"));
		BlueprintItemOrders reloaded = BlueprintItemOrders.parse(captured.serialize());
		assertTrue(reloaded.isSupported());
		assertTrue(reloaded.isCaptured());
		BankOrganizationPreview target = reloaded.apply(automatic, PLAN);
		assertPhysicalBank(physical, target);
		assertEquals(12, target.getPlannedItemCount());
		for (BankPreviewItem source : automatic.getPlannedItems())
			if (!source.isBlank())
				assertEquals(source.getLayoutTagKey(), find(target, source.getItemId(), 0).getLayoutTagKey());
	}

	@Test public void duplicateOwnedAndPlaceholderOccurrencesKeepTheirMetadataAndQuantities()
	{
		CatalogItem catalog = new CatalogItem(4151, "Abyssal whip", ItemCategory.UNKNOWN,
			"weapon", Collections.singleton("melee"), null);
		BankPreviewItem owned = new BankPreviewItem(catalog, 7).withLayoutTag("gear").withBlueprintOccurrence(0);
		BankPreviewItem placeholder = new BankPreviewItem(catalog, 0, true)
			.withLayoutTag("gear").withBlueprintOccurrence(1);
		BankOrganizationPreview automatic = automatic(owned, placeholder, item(995, "currency"));
		Map<Integer, List<Integer>> physical = physical(new int[]{4151}, new int[]{995, 4151});
		BlueprintItemOrders captured = BlueprintItemOrders.capture(physical, automatic, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview target = BlueprintItemOrders.parse(captured.serialize()).apply(automatic, PLAN);

		assertPhysicalBank(physical, target);
		assertEquals(3, target.getPlannedItemCount());
		assertEquals(3, captured.destinations().size());
		assertEquals(1, captured.destinations().get("4151#0").tab);
		assertEquals(0, captured.destinations().get("4151#1").tab);
		BankPreviewItem capturedOwned = find(target, 4151, 0);
		BankPreviewItem capturedPlaceholder = find(target, 4151, 1);
		assertEquals(7, capturedOwned.getQuantity());
		assertFalse(capturedOwned.isPlaceholder());
		assertEquals(0, capturedPlaceholder.getQuantity());
		assertTrue(capturedPlaceholder.isPlaceholder());
		assertEquals(owned.getItemCategory(), capturedOwned.getItemCategory());
		assertEquals(owned.getSubcategory(), capturedOwned.getSubcategory());
		assertEquals(owned.getUsageTags(), capturedOwned.getUsageTags());
		assertEquals(placeholder.getUsageTags(), capturedPlaceholder.getUsageTags());
		assertEquals(Integer.valueOf(1), target.getTagCounts().get("gear"));
	}

	@Test public void recaptureAfterManualTagRouteUsesItsOriginalClassificationOnReload()
	{
		BankOrganizationPreview automatic = automatic(item(4151, "gear"), item(2347, "tools"));
		BlueprintItemOrders manual = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"4151#0", new BlueprintItemOrders.Destination(PLAN.destinationOf("tools"), "gear", "tools")));
		BankOrganizationPreview routed = manual.apply(automatic, PLAN);
		assertEquals("tools", find(routed, 4151, 0).getLayoutTagKey());
		Map<Integer, List<Integer>> physical = physical(new int[]{2347}, new int[]{4151});
		BlueprintItemOrders recaptured = BlueprintItemOrders.capture(physical, routed, manual);
		BlueprintItemOrders.Destination destination = recaptured.destinations().get("4151#0");
		assertEquals("gear", destination.originalTag);
		assertEquals("tools", destination.tag);
		BlueprintItemOrders reloaded = BlueprintItemOrders.parse(recaptured.serialize());
		BankOrganizationPreview target = reloaded.apply(automatic, PLAN);
		assertPhysicalBank(physical, target);
		assertEquals("tools", find(target, 4151, 0).getLayoutTagKey());
		assertPhysicalBank(physical, reloaded.apply(automatic, PLAN.withTagAt("tools", 9)));
	}

	@Test public void recaptureRebasesRoutedCopiesToFreshPhysicalOccurrencesAcrossNumberedTabAndMain()
	{
		BankSnapshot oldBank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(4151, 7, 0),
			new BankItemSnapshot(4151, 0, 1, true)));
		BankOrganizationPreview automatic = fromBank(oldBank, BlueprintItemOrders.EMPTY);
		BlueprintItemOrders manual = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"4151#0", new BlueprintItemOrders.Destination(PLAN.destinationOf("tools"), "gear", "tools")));
		BankOrganizationPreview routed = fromBank(oldBank, manual);
		Map<Integer, List<Integer>> physical = physical(new int[]{4151}, new int[]{4151});
		BlueprintItemOrders captured = BlueprintItemOrders.capture(physical, routed, manual);

		assertEquals("gear", captured.destinations().get("4151#0").tag);
		assertEquals("tools", captured.destinations().get("4151#1").tag);
		assertEquals("gear", captured.destinations().get("4151#1").originalTag);
		// The numbered tab's placeholder is now physically read before the owned Main copy.
		BankSnapshot movedBank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(4151, 0, 0, true),
			new BankItemSnapshot(4151, 7, 1)));
		BlueprintItemOrders reloaded = BlueprintItemOrders.parse(captured.serialize());
		BankOrganizationPreview target = fromBank(movedBank, reloaded);
		assertPhysicalBank(physical, target);
		assertEquals(1, target.getCategories().get(0).getItems().get(0).getBlueprintOccurrence());
		assertEquals(0, target.getCategories().get(1).getItems().get(0).getBlueprintOccurrence());
		assertEquals(7, target.getCategories().get(0).getItems().get(0).getQuantity());
		assertFalse(target.getCategories().get(0).getItems().get(0).isPlaceholder());
		assertEquals("tools", target.getCategories().get(0).getItems().get(0).getLayoutTagKey());
		assertEquals(0, target.getCategories().get(1).getItems().get(0).getQuantity());
		assertTrue(target.getCategories().get(1).getItems().get(0).isPlaceholder());
		assertEquals(automatic.getPlannedItemCount(), target.getPlannedItemCount());
	}

	@Test public void missingCapturedItemsReturnAndNewItemsAppendToAutomaticDestinations()
	{
		BankOrganizationPreview original = automatic(item(995, "currency"), item(4151, "gear"), item(2347, "tools"));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(
			physical(new int[]{4151}, new int[]{2347, 995}), original, BlueprintItemOrders.EMPTY);
		BlueprintItemOrders reloaded = BlueprintItemOrders.parse(captured.serialize());
		BankOrganizationPreview missing = automatic(item(4151, "gear"), item(2347, "tools"), item(11840, "gear"));
		assertPhysicalBank(physical(new int[]{4151}, new int[]{2347, 11840}), reloaded.apply(missing, PLAN));
		BankOrganizationPreview returned = automatic(item(995, "currency"), item(4151, "gear"),
			item(2347, "tools"), item(11840, "gear"));
		assertPhysicalBank(physical(new int[]{4151}, new int[]{2347, 995, 11840}), reloaded.apply(returned, PLAN));
	}

	@Test public void resetReleasesOnlyTheChosenTabsCapturedTargets()
	{
		BankOrganizationPreview automatic = automatic(item(995, "currency"), item(4151, "gear"), item(2347, "tools"));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(
			physical(new int[]{2347}, new int[]{4151, 995}), automatic, BlueprintItemOrders.EMPTY);
		BlueprintItemOrders reset = BlueprintItemOrders.parse(captured.resetTab(1).serialize());
		assertFalse(reset.hasTab(1));
		assertTrue(reset.isCaptured());
		assertEquals(1, reset.destinations().size());
		assertPhysicalBank(physical(new int[]{2347, 995}, new int[]{4151}), reset.apply(automatic, PLAN));
		BlueprintItemOrders allReset = reset.resetTab(0);
		assertFalse(allReset.isCaptured());
		assertEquals("", allReset.serialize());
		assertSame(automatic, allReset.apply(automatic, PLAN));
	}

	@Test public void automaticClassificationChangePreservesCapturedPositionAndUsesTheNewMetadata()
	{
		BankOrganizationPreview automatic = automatic(item(995, "currency"), item(4151, "gear"));
		Map<Integer, List<Integer>> physical = physical(new int[]{4151}, new int[]{995});
		BlueprintItemOrders captured = BlueprintItemOrders.capture(
			physical, automatic, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview corrected = captured.apply(automatic(item(995, "currency"), item(4151, "food")), PLAN);
		assertPhysicalBank(physical, corrected);
		assertEquals("food", find(corrected, 4151, 0).getLayoutTagKey());
		assertFalse(corrected.getTagCounts().containsKey("gear"));
		assertEquals(Integer.valueOf(1), corrected.getTagCounts().get("food"));
		assertEquals(2, corrected.getPlannedItemCount());
	}

	@Test public void explicitItemAssignmentReplacesTheCaptureAndMovesTheItemToItsSelectedTag()
	{
		BankOrganizationPreview original = automatic(item(995, "currency"), item(4151, "gear"));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(
			physical(new int[]{4151}, new int[]{995}), original, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview classified = automatic(item(995, "currency"), item(4151, "food"));
		BlueprintItemOrders assigned = captured.withDestinations(Collections.singletonMap("4151#0",
			new BlueprintItemOrders.Destination(PLAN.destinationOf("tools"), "food", "tools")));
		BankOrganizationPreview target = BlueprintItemOrders.parse(assigned.serialize()).apply(classified, PLAN);
		assertTrue(target.getCategories().get(0).getItems().isEmpty());
		assertEquals(Collections.singletonList(995), ids(target.getCategories().get(1).getItems()));
		assertEquals(Collections.singletonList(4151), ids(target.getCategories().get(PLAN.destinationOf("tools")).getItems()));
		assertEquals("tools", find(target, 4151, 0).getLayoutTagKey());
		assertFalse(target.getTagCounts().containsKey("food"));
		assertEquals(Integer.valueOf(1), target.getTagCounts().get("currency"));
		assertEquals(Integer.valueOf(1), target.getTagCounts().get("tools"));
		assertEquals(2, target.getPlannedItemCount());
		assertEquals(Arrays.asList(995, 4151), ids(BankTabPlan.fromPreview(target).getFlattenedItems()));
	}

	@Test public void capturedProfilesReloadIndependently()
	{
		BankOrganizationPreview automatic = automatic(item(995, "currency"), item(4151, "gear"), item(2347, "tools"));
		Map<Integer, List<Integer>> personal = physical(new int[]{4151}, new int[]{2347, 995});
		Map<Integer, List<Integer>> bossing = physical(new int[]{995, 2347}, new int[]{4151});
		BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse("");
		profiles.put("Personal", BlueprintItemOrders.capture(personal, automatic, BlueprintItemOrders.EMPTY));
		profiles.put("Bossing", BlueprintItemOrders.capture(bossing, automatic, BlueprintItemOrders.EMPTY));
		profiles = BlueprintOrderProfiles.parse(profiles.serialize());
		assertPhysicalBank(personal, profiles.forProfile("Personal").apply(automatic, PLAN));
		assertPhysicalBank(bossing, profiles.forProfile("Bossing").apply(automatic, PLAN));
		profiles.put("Personal", profiles.forProfile("Personal").resetTab(0));
		assertPhysicalBank(bossing, profiles.forProfile("Bossing").apply(automatic, PLAN));
		assertFalse(profiles.forProfile("Unrelated").isCaptured());
		assertSame(automatic, profiles.forProfile("Unrelated").apply(automatic, PLAN));
	}

	@Test public void equalTotalsWithDifferentDuplicateCountsAreRejected()
	{
		BankOrganizationPreview automatic = automatic(item(4151, "gear").withBlueprintOccurrence(0),
			item(4151, "gear").withBlueprintOccurrence(1), item(995, "currency"));
		assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.capture(
			physical(new int[]{4151}, new int[]{995, 995}), automatic, BlueprintItemOrders.EMPTY));
		assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.capture(
			physical(new int[]{4151}, new int[]{995}), automatic, BlueprintItemOrders.EMPTY));
		assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.capture(
			physical(new int[]{4151}, new int[]{4151, 995, 2347}), automatic, BlueprintItemOrders.EMPTY));
	}

	private static BankPreviewItem item(int id, String tag)
	{
		return new BankPreviewItem(id, "Item " + id, 1).withLayoutTag(tag);
	}

	private static BankOrganizationPreview automatic(BankPreviewItem... items)
	{
		List<List<BankPreviewItem>> tabs = new ArrayList<>();
		for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++) tabs.add(new ArrayList<>());
		Map<Integer, Integer> occurrences = new LinkedHashMap<>();
		for (BankPreviewItem item : items)
		{
			int occurrence = occurrences.merge(item.getItemId(), 1, Integer::sum) - 1;
			tabs.get(PLAN.destinationOf(item.getLayoutTagKey())).add(item.getBlueprintOccurrence() < 0
				? item.withBlueprintOccurrence(occurrence) : item);
		}
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int tab = 0; tab < tabs.size(); tab++)
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(tab), tabs.get(tab)));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}

	private static Map<Integer, List<Integer>> physical(int[]... tabs)
	{
		Map<Integer, List<Integer>> result = new LinkedHashMap<>();
		for (int tab = 1; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			result.put(tab, tab < tabs.length ? Arrays.stream(tabs[tab]).boxed().collect(Collectors.toList())
				: Collections.emptyList());
		result.put(0, tabs.length > 0 ? Arrays.stream(tabs[0]).boxed().collect(Collectors.toList())
			: Collections.emptyList());
		return result;
	}

	private static BankOrganizationPreview fromBank(BankSnapshot snapshot, BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(snapshot, CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN,
			GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, PLAN,
			BankLayoutOptions.DEFAULTS.withItemOrders(orders));
	}

	private static void assertPhysicalBank(Map<Integer, List<Integer>> expected, BankOrganizationPreview preview)
	{
		List<Integer> flattened = new ArrayList<>();
		for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			assertEquals("Physical tab " + tab, expected.get(tab), ids(preview.getCategories().get(tab).getItems()));
		for (int tab = 1; tab < BankLayoutPlan.DESTINATION_COUNT; tab++) flattened.addAll(expected.get(tab));
		flattened.addAll(expected.get(0));
		assertEquals(flattened, ids(BankTabPlan.fromPreview(preview).getFlattenedItems()));
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static BankPreviewItem find(BankOrganizationPreview preview, int id, int occurrence)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id
			&& item.getBlueprintOccurrence() == occurrence).findFirst().orElseThrow(AssertionError::new);
	}
}

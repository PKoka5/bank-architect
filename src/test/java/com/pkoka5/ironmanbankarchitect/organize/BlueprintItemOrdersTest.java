package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlueprintItemOrdersTest
{
	@Test public void fullCapacityOrderRoundTripsWithoutLosingAnyOccurrence()
	{
		List<Integer> ids = java.util.stream.IntStream.rangeClosed(1, BlueprintItemOrders.BANK_CAPACITY)
			.boxed().collect(Collectors.toList());
		BankOrganizationPreview original = preview(ids.toArray(new Integer[0]));
		Collections.reverse(ids);
		BlueprintItemOrders orders = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY.withTab(0, ids).serialize());
		assertEquals(ids, ids(orders.apply(original), 0));
		assertEquals(BlueprintItemOrders.BANK_CAPACITY, orders.apply(original).getPlannedItemCount());
	}

	@Test public void changedItemAssignmentDisablesRouteAndRestoringItReactivatesRoute()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int target = plan.destinationOf("tools");
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"4151#0", new BlueprintItemOrders.Destination(target, "gear", "tools")));
		BankOrganizationPreview corrected = weaponPreview(plan, orders, id -> java.util.Optional.of("food"));
		assertTrue(ids(corrected, target).isEmpty());
		assertEquals(Arrays.asList(4151, 4151), ids(corrected, plan.destinationOf("food")));
		assertEquals(Collections.singletonList(4151), ids(weaponPreview(plan,
			BlueprintItemOrders.parse(orders.serialize()), CategoryOverrideSource.NONE), target));
	}

	@Test public void crossTabMoveSurvivesReloadAndMovesOnlyOnePhysicalCopy()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int target = plan.destinationOf("tools");
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			"4151#1", new BlueprintItemOrders.Destination(target, "gear", "tools")));
		orders = BlueprintItemOrders.parse(orders.serialize());
		BankOrganizationPreview original = weaponPreview(plan, BlueprintItemOrders.EMPTY, CategoryOverrideSource.NONE);
		BankOrganizationPreview moved = weaponPreview(plan, orders, CategoryOverrideSource.NONE);
		assertEquals(2, moved.getPlannedItemCount());
		assertEquals(Collections.singletonList(4151), ids(moved, target));
		assertEquals(Collections.singletonList(4151), ids(moved, plan.destinationOf("gear")));
		assertEquals(1, moved.getCategories().get(target).getItems().get(0).getBlueprintOccurrence());
		assertEquals("tools", moved.getCategories().get(target).getItems().get(0).getLayoutTagKey());
		assertEquals(original.toPreviewText(), weaponPreview(plan, orders.resetTab(target),
			CategoryOverrideSource.NONE).toPreviewText());
		BankLayoutPlan changedPlan = plan.withTagAt("tools", 9);
		assertTrue(ids(weaponPreview(changedPlan, orders, CategoryOverrideSource.NONE), target).isEmpty());
	}

	private static BankOrganizationPreview weaponPreview(BankLayoutPlan plan, BlueprintItemOrders orders,
		CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(4151, 1, 0), new BankItemSnapshot(4151, 1, 1))),
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			overrides, plan, BankLayoutOptions.DEFAULTS.withItemOrders(orders));
	}

	@Test public void savedCoinsAndTokensLeadAfterAnalysisAndReload()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(2347, 1, 0),
			new BankItemSnapshot(995, 500, 1), new BankItemSnapshot(13204, 12, 2)));
		BlueprintItemOrders orders = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(0, Arrays.asList(995, 13204, 2347)).serialize());
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(BankPresets.IRONMAN),
			BankLayoutOptions.DEFAULTS.withItemOrders(orders));
		assertEquals(Arrays.asList(995, 13204, 2347), ids(preview, 0));
		assertTrue(preview.getCategories().get(0).hasManualOrder());
		BankTabPlan plan = BankTabPlan.fromPreview(preview);
		assertEquals(TabRouteAdvisor.Status.COMPLETE, TabRouteAdvisor.assess(
			new int[]{995, 13204, 2347}, plan, new int[9]).getStatus());
		assertTrue(BankBlueprintTextExporter.export(preview).contains("col=2 slot=2 | id=13204"));
	}

	@Test public void absentItemsReturnAndNewItemsAppendWithoutInventedSlots()
	{
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(1, 2, 3));
		assertEquals(Arrays.asList(1, 3, 4), ids(orders.apply(preview(3, 4, 1)), 0));
		orders = orders.withTab(0, Arrays.asList(3, 1, 4));
		assertEquals(Arrays.asList(3, 2, 1, 4), ids(orders.apply(preview(4, 2, 1, 3)), 0));
	}

	@Test public void duplicatesArePhysicalAndNothingIsLost()
	{
		BlueprintItemOrders orders = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(7, 2, 7));
		assertEquals(Arrays.asList(7, 2, 7, 9, 7), ids(orders.apply(preview(9, 7, 7, 2, 7)), 0));
	}

	@Test public void resetAndEmptyDefaultsLeaveAutomaticPreviewUntouched()
	{
		BankOrganizationPreview preview = preview(3, 2, 1);
		assertSame(preview, BlueprintItemOrders.EMPTY.apply(preview));
		assertSame(preview, BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(1, 2, 3))
			.withTab(0, Collections.emptyList()).apply(preview));
		BlueprintItemOrders otherTab = BlueprintItemOrders.EMPTY.withTab(1, Arrays.asList(1, 2));
		assertSame(preview.getCategories().get(0), otherTab.apply(preview).getCategories().get(0));
	}

	@Test public void profileNamesRoundTripAndOrdersDoNotLeak()
	{
		BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse("");
		profiles.put("Personal ~ ; é", BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(2, 1)));
		profiles.put("Bossing", BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(1, 2)));
		profiles = BlueprintOrderProfiles.parse(profiles.serialize());
		assertEquals(Arrays.asList(2, 1), ids(profiles.forProfile("Personal ~ ; é").apply(preview(1, 2)), 0));
		assertEquals(Arrays.asList(1, 2), ids(profiles.forProfile("Bossing").apply(preview(2, 1)), 0));
		profiles.remove("Bossing");
		assertFalse(profiles.forProfile("Bossing").hasTab(0));
		assertTrue(profiles.forProfile("Personal ~ ; é").hasTab(0));
	}

	@Test public void malformedTabsAreIsolatedAndFutureVersionsAreNotOverwritten()
	{
		BlueprintItemOrders parsed = BlueprintItemOrders.parse("v1|0:2,1|1:bad|22:1|2:-1");
		assertEquals(Arrays.asList(2, 1), ids(parsed.apply(preview(1, 2)), 0));
		assertFalse(parsed.hasTab(1));
		BlueprintItemOrders future = BlueprintItemOrders.parse("v2|keep-this");
		assertFalse(future.isSupported());
		assertEquals("v2|keep-this", future.serialize());
		assertThrows(IllegalStateException.class, () -> future.withTab(0, Arrays.asList(1)));
		assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.EMPTY.withTab(10, Arrays.asList(1)));
		assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.EMPTY.withTab(0,
			Collections.nCopies(BlueprintItemOrders.BANK_CAPACITY + 1, 1)));
	}

	private static BankOrganizationPreview preview(Integer... ids)
	{
		return new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(new BankCategoryPreview(
			BankPresets.IRONMAN.getCategories().get(0), Arrays.stream(ids)
				.map(id -> new BankPreviewItem(id, "Item " + id, 1)).collect(Collectors.toList()))));
	}
	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
	}
}

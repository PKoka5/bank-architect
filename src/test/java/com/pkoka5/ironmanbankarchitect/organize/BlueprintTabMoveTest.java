package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BlueprintTabMoveTest
{
	private static final int[] AMMO = {882, 877, 806, 2};
	private final BankPreset preset;
	private final BankLayoutPlan defaults;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[]{BankPresets.IRONMAN}, new Object[]{BankPresets.MAIN});
	}

	public BlueprintTabMoveTest(BankPreset preset)
	{
		this.preset = preset;
		defaults = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void everyMoveUsesInsertionAndCarriesTagsAndCurrentOrderFlags()
	{
		BankLayoutPlan original = defaults.withCurrentOrder(0, true).withCurrentOrder(2, true)
			.withCurrentOrder(7, true).withCurrentOrder(9, true);
		String unchanged = original.serialize();
		for (int from = 0; from < 10; from++) for (int to = 0; to < 10; to++)
		{
			List<Integer> newOrder = orderAfterMove(from, to);
			BankLayoutPlan moved = original.withDestinationMoved(from, to, preset);
			for (int old = 0; old < 10; old++)
			{
				int target = newOrder.indexOf(old);
				assertEquals(target, BankLayoutPlan.destinationAfterMove(old, from, to));
				assertEquals(original.getTagKeys(old), moved.getTagKeys(target));
				assertEquals(original.keepsCurrentOrder(old), moved.keepsCurrentOrder(target));
			}
			assertPlanSemantics(moved, BankLayoutPlan.parse(preset, moved.serialize()));
			assertPlanSemantics(original, moved.withDestinationMoved(to, from, preset));
			if (from == to) assertSame(original, moved);
		}
		assertEquals(unchanged, original.serialize());
	}

	@Test public void emptyMainAndLastDestinationParticipateWithoutDroppingTags()
	{
		BankLayoutPlan emptyMain = defaults;
		for (String tag : defaults.getTagKeys(0)) emptyMain = emptyMain.withTagAt(tag, 9);
		emptyMain = emptyMain.withCurrentOrder(0, true);
		assertTrue(emptyMain.getTagKeys(0).isEmpty());
		BankLayoutPlan moved = emptyMain.withDestinationMoved(0, 9, preset);
		assertTrue(moved.getTagKeys(9).isEmpty());
		assertTrue(moved.keepsCurrentOrder(9));
		assertEquals(emptyMain.getTagKeys(9), moved.getTagKeys(8));
		assertPlanSemantics(emptyMain, BankLayoutPlan.parse(preset, moved.serialize())
			.withDestinationMoved(9, 0, preset));
	}

	@Test public void oldPlansRoundTripWithoutAddingAnAmmoPolicy()
	{
		for (BankLayoutPlan old : Arrays.asList(defaults, defaults.withTagAt("ammunition", 6),
			defaults.withTagAt("gear", 6).withTagAt("ammunition", 6).withCurrentOrder(6, true)))
		{
			String serialized = old.serialize();
			assertFalse(serialized.contains("~ammo-"));
			BankLayoutPlan parsed = BankLayoutPlan.parse(preset, serialized);
			assertEquals(serialized, parsed.serialize());
			assertEquals(old.destinationOf("ammunition") != defaults.destinationOf("ammunition"),
				parsed.ammoUsesAssignedTag(preset));
		}
	}

	@Test public void allFourAmmoFamiliesKeepTheirDropsAcrossEveryWholeTabMove()
	{
		BankSnapshot bank = ammoBank();
		for (GearStatsSource stats : Arrays.asList(GearStatsSource.NONE, ammoStats()))
		{
			BankOrganizationPreview before = ammoPreview(bank, defaults, stats, CategoryOverrideSource.NONE);
			for (int id : AMMO) assertAmmo(before, id, "boss-loot", defaults.destinationOf("boss-loot"));
			for (int from = 0; from < 10; from++) for (int to = 0; to < 10; to++)
			{
				BankLayoutPlan moved = BankLayoutPlan.parse(preset,
					defaults.withDestinationMoved(from, to, preset).serialize());
				assertFalse(moved.ammoUsesAssignedTag(preset));
				BankOrganizationPreview after = ammoPreview(bank, moved, stats, CategoryOverrideSource.NONE);
				for (int id : AMMO) assertAmmo(after, id, "boss-loot",
					orderAfterMove(from, to).indexOf(defaults.destinationOf("boss-loot")));
				assertConserved(bank, after);
			}
		}
	}

	@Test public void aLegacyRelocatedGearAndAmmoBundleKeepsItsExplicitPlacementEvenAtDefaultIndex()
	{
		BankLayoutPlan legacy = defaults.withTagAt("gear", 6).withTagAt("ammunition", 6);
		assertTrue(legacy.ammoUsesAssignedTag(preset));
		BankSnapshot bank = ammoBank();
		for (int from = 0; from < 10; from++) for (int to = 0; to < 10; to++)
		{
			BankLayoutPlan moved = BankLayoutPlan.parse(preset, legacy.withDestinationMoved(from, to, preset).serialize());
			assertTrue(moved.ammoUsesAssignedTag(preset));
			BankOrganizationPreview after = ammoPreview(bank, moved, ammoStats(), CategoryOverrideSource.NONE);
			for (int id : AMMO) assertAmmo(after, id, "ammunition", orderAfterMove(from, to).indexOf(6));
			assertConserved(bank, after);
			assertPlanSemantics(legacy, moved.withDestinationMoved(to, from, preset));
		}
		BankLayoutPlan onDefaultIndex = legacy.withDestinationMoved(6, defaults.destinationOf("ammunition"), preset);
		assertEquals(defaults.destinationOf("ammunition"), onDefaultIndex.destinationOf("ammunition"));
		assertTrue(onDefaultIndex.ammoUsesAssignedTag(preset));
	}

	@Test public void aGenuineManualAmmoAssignmentResetsFrozenPolicyButRedundantClicksDoNot()
	{
		BankLayoutPlan moved = defaults.withDestinationMoved(defaults.destinationOf("ammunition"), 6, preset);
		assertFalse(moved.ammoUsesAssignedTag(preset));
		assertSame(moved, moved.withTagAt("ammunition", 6));
		BankLayoutPlan manuallyPlaced = moved.withTagAt("ammunition", 9);
		assertTrue(manuallyPlaced.ammoUsesAssignedTag(preset));
		assertFalse(manuallyPlaced.serialize().contains("~ammo-"));
		BankLayoutPlan reset = manuallyPlaced.withTagAt("ammunition", defaults.destinationOf("ammunition"));
		assertFalse(reset.ammoUsesAssignedTag(preset));
		BankLayoutPlan explicit = defaults.withTagAt("ammunition", 6).withDestinationMoved(6,
			defaults.destinationOf("ammunition"), preset);
		assertSame(explicit, explicit.withTagAt("ammunition", explicit.destinationOf("ammunition")));
		assertTrue(explicit.ammoUsesAssignedTag(preset));
	}

	@Test public void frozenAmmoPoliciesSurviveOtherPlanEditsAndShareCodes()
	{
		for (boolean assigned : Arrays.asList(false, true))
		{
			BankLayoutPlan marked = BankLayoutPlan.parse(preset,
				(assigned ? "~ammo-tag+" : "~ammo-drops+") + defaults.serialize());
			assertEquals(assigned, marked.ammoUsesAssignedTag(preset));
			assertEquals(!assigned, marked.isDefault(preset));
			for (BankLayoutPlan changed : Arrays.asList(marked.withCurrentOrder(9, true),
				marked.withTagShifted("gear", 1), marked.withTagAt("food", 9), marked.completedFor(preset)))
			{
				assertEquals(assigned, changed.ammoUsesAssignedTag(preset));
				BankLayoutShareCode decoded = BankLayoutShareCode.decode(
					BankLayoutShareCode.encode("Moved layout", changed, preset)).get();
				assertPlanSemantics(changed, BankLayoutPlan.parse(preset, decoded.getPlan()));
			}
		}
		assertFalse(BankLayoutPlan.parse(preset, "~ammo-drops+~ammo-tag+" + defaults.serialize())
			.ammoUsesAssignedTag(preset));
		assertTrue(BankLayoutPlan.parse(preset, "~ammo-tag+~ammo-drops+" + defaults.serialize())
			.ammoUsesAssignedTag(preset));
		assertFalse(BankLayoutPlan.parse(preset, "~ammo-future+" + defaults.serialize())
			.ammoUsesAssignedTag(preset));
	}

	@Test public void capturedManualAndDormantPhysicalOrdersFollowEveryMoveWithoutLosingOccurrences()
	{
		BlueprintItemOrders original = savedOrders();
		BankOrganizationPreview automatic = automatic(defaults, false);
		BankOrganizationPreview returned = automatic(defaults, true);
		BankOrganizationPreview before = original.apply(automatic, defaults);
		BankOrganizationPreview beforeReturned = original.apply(returned, defaults);
		for (int from = 0; from < 10; from++) for (int to = 0; to < 10; to++)
		{
			BankLayoutPlan plan = defaults.withDestinationMoved(from, to, preset);
			BlueprintItemOrders moved = BlueprintItemOrders.parse(original.withDestinationMoved(from, to).serialize());
			assertTrue(moved.isCaptured());
			for (int old = 0; old < 10; old++) assertEquals(original.hasTab(old), moved.hasTab(orderAfterMove(from, to).indexOf(old)));
			for (String key : original.destinations().keySet())
			{
				BlueprintItemOrders.Destination old = original.destinations().get(key), now = moved.destinations().get(key);
				assertEquals(orderAfterMove(from, to).indexOf(old.tab), now.tab);
				assertEquals(old.originalTag, now.originalTag);
				assertEquals(old.tag, now.tag);
				assertEquals(old.isCaptured(), now.isCaptured());
			}
			assertPermuted(before, moved.apply(automatic, plan), from, to);
			assertPermuted(beforeReturned, moved.apply(returned, plan), from, to);
			BlueprintItemOrders inverse = moved.withDestinationMoved(to, from);
			assertPermuted(beforeReturned, inverse.apply(returned, defaults), 0, 0);
			if (from == to) assertSame(original, original.withDestinationMoved(from, to));
		}
		assertEquals(Arrays.asList(1001, 9001, 1001, 1002), ids(beforeReturned, 0));
		assertEquals(Arrays.asList(9002, 1003), ids(beforeReturned, 8));
		assertEquals(Arrays.asList(1004), ids(beforeReturned, defaults.destinationOf("tools")));
	}

	@Test public void profileSerializationKeepsUnrelatedOrdersAndPreservesMovedCapturedLayouts()
	{
		BankLayoutPlan moved = defaults.withDestinationMoved(0, 9, preset);
		BlueprintItemOrders items = savedOrders().withDestinationMoved(0, 9);
		BlueprintOrderProfiles orders = BlueprintOrderProfiles.parse("");
		orders.put("Other", BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(5, 4)));
		orders.put("Moved", items);
		String other = orders.forProfile("Other").serialize();
		orders = BlueprintOrderProfiles.parse(orders.serialize());
		assertEquals(other, orders.forProfile("Other").serialize());
		assertEquals(items.serialize(), orders.forProfile("Moved").serialize());
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "", BankLayoutProfiles.defaultName(preset))
			.withProfile("Moved", moved.serialize());
		profiles = BankLayoutProfiles.parse(profiles.serialize(), profiles.getActiveName(), profiles.getDefaultName());
		assertPlanSemantics(moved, BankLayoutPlan.parse(preset, profiles.activePlan()));
	}

	@Test public void explicitItemCorrectionsStillWinAfterTheCombatBundleMoves()
	{
		BankLayoutPlan moved = defaults.withDestinationMoved(defaults.destinationOf("gear"), 9, preset);
		BankOrganizationPreview preview = ammoPreview(ammoBank(), moved, ammoStats(),
			id -> id == 882 ? Optional.of("tools") : Optional.empty());
		assertAmmo(preview, 882, "tools", moved.destinationOf("tools"));
		for (int id : new int[]{877, 806, 2}) assertAmmo(preview, id, "boss-loot", moved.destinationOf("boss-loot"));
	}

	@Test public void invalidIndexesAreRejectedAndFutureOrderFormatsRemainUntouched()
	{
		for (int invalid : new int[]{-1, 10})
		{
			assertThrows(IllegalArgumentException.class, () -> defaults.withDestinationMoved(invalid, 0, preset));
			assertThrows(IllegalArgumentException.class, () -> defaults.withDestinationMoved(0, invalid, preset));
			assertThrows(IllegalArgumentException.class, () -> defaults.withDestinationMoved(invalid, invalid, preset));
			assertThrows(IllegalArgumentException.class, () -> BankLayoutPlan.destinationAfterMove(invalid, 0, 9));
			assertThrows(IllegalArgumentException.class, () -> BankLayoutPlan.destinationAfterMove(0, invalid, 9));
			assertThrows(IllegalArgumentException.class, () -> BankLayoutPlan.destinationAfterMove(0, 9, invalid));
			assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.EMPTY.withDestinationMoved(invalid, 0));
			assertThrows(IllegalArgumentException.class, () -> BlueprintItemOrders.EMPTY.withDestinationMoved(0, invalid));
		}
		BlueprintItemOrders future = BlueprintItemOrders.parse("v99|opaque-data");
		assertFalse(future.isSupported());
		assertThrows(IllegalStateException.class, () -> future.withDestinationMoved(0, 9));
		assertEquals("v99|opaque-data", future.serialize());
	}

	private static List<Integer> orderAfterMove(int from, int to)
	{
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < 10; i++) order.add(i);
		order.add(to, order.remove(from));
		return order;
	}

	private void assertPlanSemantics(BankLayoutPlan expected, BankLayoutPlan actual)
	{
		assertEquals(expected.getDestinations(), actual.getDestinations());
		for (int i = 0; i < 10; i++) assertEquals(expected.keepsCurrentOrder(i), actual.keepsCurrentOrder(i));
		assertEquals(expected.ammoUsesAssignedTag(preset), actual.ammoUsesAssignedTag(preset));
	}

	private BlueprintItemOrders savedOrders()
	{
		return BlueprintItemOrders.parse("v2|0:1001,9001,1001,1002|8:9002,1003|" + defaults.destinationOf("tools")
			+ ":1004|c:1001#0:0:gear:gear|c:1001#1:0:gear:gear|c:1002#0:0:currency:currency"
			+ "|c:9001#0:0:gear:gear|c:1003#0:8:food:food|c:9002#0:8:food:food"
			+ "|r:1004#0:" + defaults.destinationOf("tools") + ":gear:tools");
	}

	private BankOrganizationPreview automatic(BankLayoutPlan plan, boolean returned)
	{
		List<List<BankPreviewItem>> tabs = new ArrayList<>();
		for (int i = 0; i < 10; i++) tabs.add(new ArrayList<>());
		List<BankPreviewItem> items = new ArrayList<>(Arrays.asList(item(1001, "gear", 7, false),
			item(1001, "gear", 0, true), item(1002, "currency", 10, false),
			item(1003, "food", 3, false), item(1004, "gear", 1, false)));
		if (returned) items.addAll(Arrays.asList(item(9001, "gear", 2, false), item(9002, "food", 4, false)));
		for (BankPreviewItem item : items) tabs.get(plan.destinationOf(item.getLayoutTagKey())).add(item);
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int i = 0; i < 10; i++) categories.add(new BankCategoryPreview(preset.getCategories().get(i), tabs.get(i)));
		return new BankOrganizationPreview(preset, categories);
	}

	private static BankPreviewItem item(int id, String tag, int quantity, boolean placeholder)
	{
		CatalogItem catalog = new CatalogItem(id, "Item " + id, ItemCategory.UNKNOWN, "unknown", Collections.emptySet(), null);
		return new BankPreviewItem(catalog, quantity, placeholder).withLayoutTag(tag);
	}

	private static void assertPermuted(BankOrganizationPreview before, BankOrganizationPreview after, int from, int to)
	{
		assertEquals(before.getPlannedItemCount(), after.getPlannedItemCount());
		for (int old = 0; old < 10; old++)
			assertEquals(itemSignatures(before, old), itemSignatures(after, orderAfterMove(from, to).indexOf(old)));
	}

	private static List<String> itemSignatures(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().map(item -> item.getItemId() + ":"
			+ item.getQuantity() + ":" + item.isPlaceholder() + ":" + item.getLayoutTagKey() + ":" + item.getBlueprintOccurrence())
			.collect(Collectors.toList());
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static BankSnapshot ammoBank()
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : AMMO) items.add(new BankItemSnapshot(id, id % 17 + 1, items.size()));
		return new BankSnapshot(items);
	}

	private static GearStatsSource ammoStats()
	{
		return id -> Arrays.stream(AMMO).anyMatch(ammo -> ammo == id)
			? Optional.of(new GearStats(GearSlot.AMMO, 0, 0, 0, 0, 0, 0, 31, 0, 0)) : Optional.empty();
	}

	private BankOrganizationPreview ammoPreview(BankSnapshot bank, BankLayoutPlan plan, GearStatsSource stats,
		CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset, stats,
			ItemValueSource.NONE, overrides, plan, BankLayoutOptions.defaultFor(preset));
	}

	private static void assertAmmo(BankOrganizationPreview preview, int id, String tag, int tab)
	{
		BankPreviewItem item = preview.getCategories().get(tab).getItems().stream().filter(candidate -> candidate.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("Missing item " + id + " in tab " + tab));
		assertEquals(tag, item.getLayoutTagKey());
	}

	private static void assertConserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getItems().size(), preview.getPlannedItems().size());
		for (BankItemSnapshot source : bank.getItems())
		{
			List<BankPreviewItem> found = preview.getPlannedItems().stream().filter(item -> item.getItemId() == source.getItemId())
				.collect(Collectors.toList());
			assertEquals(1, found.size());
			assertEquals(source.getQuantity(), found.get(0).getQuantity());
			assertEquals(source.isPlaceholder(), found.get(0).isPlaceholder());
		}
	}
}

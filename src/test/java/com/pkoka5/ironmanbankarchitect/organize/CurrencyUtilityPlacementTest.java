package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CurrencyUtilityPlacementTest
{
	private static final int[] CURRENCIES = {995, 12012, 21555, 25527, 6529, 6306, 31054};
	private static final int[] TELEPORT_REWARDS = {
		22941, 22943, 22945, 22947, 25926, 25928, 25930, 25932, 25934, 25936, 13143, 13144
	};
	private final BankPreset preset;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.IRONMAN}, {BankPresetType.MAIN}});
	}

	public CurrencyUtilityPlacementTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
	}

	@Test
	public void actualCurrenciesFollowCurrencyWhileFrogTokenRemainsACosmeticReward()
	{
		for (BankLayoutPlan plan : plans())
		{
			BankOrganizationPreview preview = build(bank(false, 6183, 995, 12012, 21555, 25527, 6529, 6306, 31054),
				plan, CategoryOverrideSource.NONE);
			for (int id : CURRENCIES)
			{
				assertAt(preview, plan, id, "currency");
				assertEquals(ItemCategory.CURRENCY, item(preview, id).getItemCategory());
			}
			assertAt(preview, plan, 6183, "clues");
			assertEquals(Integer.valueOf(CURRENCIES.length), preview.getTagCounts().get("currency"));
			assertEquals(CURRENCIES.length + 1, preview.getPlannedItemCount());
		}
	}

	@Test
	public void teleportRewardsKeepTheirRoleEvenWhenTheyHaveEquipmentStats()
	{
		for (BankLayoutPlan plan : plans())
		{
			BankOrganizationPreview preview = build(bank(false, TELEPORT_REWARDS), plan, CategoryOverrideSource.NONE);
			for (int id : TELEPORT_REWARDS)
			{
				assertAt(preview, plan, id, "teleports");
				assertEquals(ItemCategory.TELEPORT, item(preview, id).getItemCategory());
			}
			assertEquals(TELEPORT_REWARDS.length, preview.getPlannedItemCount());
		}
	}

	@Test
	public void lowerWesternBannersAreUtilitiesInsteadOfCurrencyOrInventedTeleports()
	{
		BankLayoutPlan plan = separatedPlan();
		BankOrganizationPreview preview = build(bank(false, 13141, 13142), plan, CategoryOverrideSource.NONE);
		String tag = preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "tools";
		for (int id : new int[] {13141, 13142})
		{
			assertAt(preview, plan, id, tag);
			assertEquals(ItemCategory.TOOL, item(preview, id).getItemCategory());
			assertEquals("achievement-utility", item(preview, id).getSubcategory());
		}
	}

	@Test
	public void personalTagsSurviveReloadAndClearingRestoresEachAutomaticRole()
	{
		BankLayoutPlan plan = separatedPlan();
		BankSnapshot bank = bank(false, 12012, 22947, 13141);
		UserCategoryOverrides saved = new UserCategoryOverrides();
		saved.put(12012, "clues");
		saved.put(22947, "gear");
		saved.put(13141, "currency");
		UserCategoryOverrides restored = UserCategoryOverrides.parse(saved.serialize());
		BankOrganizationPreview manual = build(bank, plan, restored);
		assertAt(manual, plan, 12012, "clues");
		assertAt(manual, plan, 22947, "gear");
		assertAt(manual, plan, 13141, "currency");
		restored.clear();
		BankOrganizationPreview automatic = build(bank, plan, UserCategoryOverrides.parse(restored.serialize()));
		assertAt(automatic, plan, 12012, "currency");
		assertAt(automatic, plan, 22947, "teleports");
		assertAt(automatic, plan, 13141, preset.getType() == BankPresetType.IRONMAN ? "frequently-used" : "tools");
	}

	@Test
	public void placeholdersRetainCurrencyAndTeleportDestinationsWithoutOwnedTagCounts()
	{
		BankLayoutPlan plan = separatedPlan();
		BankOrganizationPreview preview = build(bank(true, 12012, 22947), plan, CategoryOverrideSource.NONE);
		assertAt(preview, plan, 12012, "currency");
		assertAt(preview, plan, 22947, "teleports");
		assertTrue(item(preview, 12012).isPlaceholder());
		assertTrue(item(preview, 22947).isPlaceholder());
		assertEquals(0, item(preview, 12012).getQuantity());
		assertTrue(preview.getTagCounts().isEmpty());
	}

	@Test
	public void capturedBankKeepsPhysicalTabsWhenTheOldCurrencyAndClueRolesAreCorrected()
	{
		BankLayoutPlan plan = separatedPlan();
		BankSnapshot bank = bank(false, 22947, 12012);
		UserCategoryOverrides oldRoles = new UserCategoryOverrides();
		oldRoles.put(22947, "currency");
		oldRoles.put(12012, "clues");
		BankOrganizationPreview oldPreview = build(bank, plan, oldRoles);
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(3,
			Arrays.asList(22947, 12012)), oldPreview, BlueprintItemOrders.EMPTY);
		BankLayoutOptions options = BankLayoutOptions.defaultFor(preset)
			.withItemOrders(BlueprintItemOrders.parse(captured.serialize()));
		BankOrganizationPreview automatic = build(bank, plan, CategoryOverrideSource.NONE, options);
		assertEquals("teleports", item(automatic, 22947).getLayoutTagKey());
		assertEquals("currency", item(automatic, 12012).getLayoutTagKey());
		assertEquals(Arrays.asList(22947, 12012), Arrays.asList(
			automatic.getCategories().get(3).getItems().get(0).getItemId(),
			automatic.getCategories().get(3).getItems().get(1).getItemId()));
		UserCategoryOverrides personal = new UserCategoryOverrides();
		personal.put(22947, "gear");
		BankOrganizationPreview manual = build(bank, plan, personal, options);
		assertEquals("gear", item(manual, 22947).getLayoutTagKey());
		assertTrue(manual.getCategories().get(3).getItems().stream().anyMatch(item -> item.getItemId() == 22947));
	}

	private List<BankLayoutPlan> plans()
	{
		return Arrays.asList(BankLayoutPlan.defaultFor(preset), separatedPlan());
	}

	private BankLayoutPlan separatedPlan()
	{
		return BankLayoutPlan.defaultFor(preset).withTagAt("currency", 4).withTagAt("teleports", 7)
			.withTagAt("tools", 6).withTagAt("frequently-used", 5).withTagAt("gear", 8).withTagAt("clues", 9);
	}

	private static BankSnapshot bank(boolean placeholder, int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, placeholder ? 0 : 1, items.size(), placeholder));
		return new BankSnapshot(items);
	}

	private BankOrganizationPreview build(BankSnapshot bank, BankLayoutPlan plan, CategoryOverrideSource overrides)
	{
		return build(bank, plan, overrides, BankLayoutOptions.defaultFor(preset));
	}

	private BankOrganizationPreview build(BankSnapshot bank, BankLayoutPlan plan, CategoryOverrideSource overrides,
		BankLayoutOptions options)
	{
		GearStatsSource stats = id -> Optional.of(new GearStats(GearSlot.SHIELD, 0, 0, 0, 0, 0, 0, 0, 2, 0));
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset, stats,
			ItemValueSource.NONE, overrides, plan, options);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertAt(BankOrganizationPreview preview, BankLayoutPlan plan, int id, String tag)
	{
		assertEquals("Item " + id, tag, item(preview, id).getLayoutTagKey());
		assertTrue("Item " + id + " at destination for " + tag,
			preview.getCategories().get(plan.destinationOf(tag)).getItems().stream()
				.anyMatch(item -> item.getItemId() == id));
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ValeOfferingsPlacementTest
{
	private static final int OFFERINGS = 31054;
	private static final List<BankPreset> PRESETS = Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN);

	@Test
	public void offeringsFollowCurrencyWithoutMovingToolsCombatGearOrMaterials()
	{
		BankSnapshot bank = bank(false);
		for (BankPreset preset : PRESETS)
		{
			for (BankLayoutPlan plan : Arrays.asList(BankLayoutPlan.defaultFor(preset), separatedPlan(preset)))
			{
				BankOrganizationPreview preview = build(bank, preset, plan, CategoryOverrideSource.NONE);
				assertAt(preview, plan, OFFERINGS, "currency");
				assertAt(preview, plan, 995, "currency");
				assertAt(preview, plan, 31043, "tools");
				assertAt(preview, plan, 4151, "gear");
				assertAt(preview, plan, 1515, "raw-resources");
				assertEquals(200, item(preview, OFFERINGS).getQuantity());
				assertEquals(Integer.valueOf(2), preview.getTagCounts().get("currency"));
				assertEquals(5, preview.getPlannedItemCount());
			}
		}
	}

	@Test
	public void personalMaterialsAssignmentSurvivesReloadAndClearingRestoresCurrency()
	{
		for (BankPreset preset : PRESETS)
		{
			BankLayoutPlan plan = separatedPlan(preset);
			for (String choice : Arrays.asList("raw-resources", "resources"))
			{
				UserCategoryOverrides saved = new UserCategoryOverrides();
				saved.put(OFFERINGS, choice);
				UserCategoryOverrides restored = UserCategoryOverrides.parse(saved.serialize());
				BankOrganizationPreview manual = build(bank(false), preset, plan, restored);
				assertAt(manual, plan, OFFERINGS, "raw-resources");
				assertAt(manual, plan, 995, "currency");
				restored.remove(OFFERINGS);
				BankOrganizationPreview automatic = build(bank(false), preset, plan,
					UserCategoryOverrides.parse(restored.serialize()));
				assertAt(automatic, plan, OFFERINGS, "currency");
				assertAt(automatic, plan, 1515, "raw-resources");
			}
		}
	}

	@Test
	public void offeringsPlaceholderKeepsItsCurrencyDestinationWithoutAnOwnedItemCount()
	{
		for (BankPreset preset : PRESETS)
		{
			BankLayoutPlan plan = separatedPlan(preset);
			BankOrganizationPreview preview = build(bank(true), preset, plan, CategoryOverrideSource.NONE);
			assertAt(preview, plan, OFFERINGS, "currency");
			assertTrue(item(preview, OFFERINGS).isPlaceholder());
			assertEquals(0, item(preview, OFFERINGS).getQuantity());
			assertEquals(Integer.valueOf(1), preview.getTagCounts().get("currency"));
		}
	}

	private static BankLayoutPlan separatedPlan(BankPreset preset)
	{
		return BankLayoutPlan.defaultFor(preset).withTagAt("currency", 4)
			.withTagAt("raw-resources", 7).withTagAt("gear", 8).withTagAt("tools", 9);
	}

	private static BankSnapshot bank(boolean placeholder)
	{
		return new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(OFFERINGS, placeholder ? 0 : 200, 0, placeholder),
			new BankItemSnapshot(995, 10000, 1),
			new BankItemSnapshot(31043, 1, 2),
			new BankItemSnapshot(4151, 1, 3),
			new BankItemSnapshot(1515, 50, 4)));
	}

	private static BankOrganizationPreview build(BankSnapshot bank, BankPreset preset,
		BankLayoutPlan plan, CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			GearStatsSource.NONE, ItemValueSource.NONE, overrides, plan, BankLayoutOptions.defaultFor(preset));
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int itemId)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == itemId)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertAt(BankOrganizationPreview preview, BankLayoutPlan plan, int itemId, String tag)
	{
		assertEquals("Item " + itemId, tag, item(preview, itemId).getLayoutTagKey());
		assertTrue("Item " + itemId + " at destination for " + tag,
			preview.getCategories().get(plan.destinationOf(tag)).getItems().stream()
				.anyMatch(item -> item.getItemId() == itemId));
	}
}

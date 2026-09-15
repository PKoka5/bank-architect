package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Small synthetic banks reproducing the reported choices, without private bank exports. */
public class CommunityFeedbackPlacementTest
{
	@Test
	public void platinumAssignedToFrequentlyUsedPrecedesTeleportsInGridAndList()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("13204=frequently-used");
		for (TabOrder order : new TabOrder[]{TabOrder.PACKED, TabOrder.SEQUENTIAL})
		{
			BankLayoutOptions options = new BankLayoutOptions(true, true, true,
				Collections.singletonMap(BankCategorySortMode.MAIN, order));
			BankOrganizationPreview preview = build(bank(995, 2347, 13204, 563, 8013, 6529),
				overrides, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options);
			List<Integer> ids = ids(preview, 0);
			assertTrue("Chosen quick-access tag must precede teleports: " + ids,
				ids.indexOf(13204) < ids.indexOf(8013));
			assertEquals(Integer.valueOf(2), preview.getTagCounts().get("frequently-used"));
			BankPreviewItem platinum = preview.getCategories().get(0).getItems().stream()
				.filter(item -> item.getItemId() == 13204).findFirst().get();
			assertEquals("frequently-used", platinum.getLayoutTagKey());
			assertEquals("currency", platinum.getSubcategory());
			assertTrue(BankBlueprintTextExporter.export(preview).contains("layoutTag=frequently-used"));
		}
	}

	@Test
	public void platinumDestinationSurvivesStorageAndClearingRestoresCurrency()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("frequently-used", 4).withTagAt("currency", 6);
		UserCategoryOverrides saved = UserCategoryOverrides.parse("13204=frequently-used");
		UserCategoryOverrides restored = UserCategoryOverrides.parse(saved.serialize());
		BankSnapshot bank = bank(995, 13204);
		assertTrue(ids(build(bank, restored, plan, BankLayoutOptions.DEFAULTS), 4).contains(13204));
		restored.remove(13204);
		BankOrganizationPreview automatic = build(bank,
			UserCategoryOverrides.parse(restored.serialize()), plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(automatic, 6).contains(13204));
		assertFalse(ids(automatic, 4).contains(13204));
	}

	@Test
	public void mysticHatOutranksTheBlackWizardHatAfterManualGearAssignment()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("4089=gear,1017=gear");
		GearStatsSource magicHats = id -> Optional.of(new GearStats(GearSlot.HEAD,
			0, 0, 0, id == 4089 ? 4 : 2, 0, 0, 0, 0, id == 4089 ? 4 : 2));
		for (GearStatsSource stats : new GearStatsSource[]{GearStatsSource.NONE, magicHats})
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank(4089, 1017),
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, stats, ItemValueSource.NONE,
				overrides, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
			assertEquals(Arrays.asList(4089, 1017), ids(preview, 1));
		}
	}

	@Test
	public void effectiveTagSurvivesPhysicalCopiesAndPlaceholders()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(13204, 1, 0), new BankItemSnapshot(13204, 0, 1, true)));
		BankOrganizationPreview preview = build(bank, UserCategoryOverrides.parse("13204=frequently-used"),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
		assertEquals(Arrays.asList(13204, 13204), ids(preview, 0));
		for (BankPreviewItem item : preview.getCategories().get(0).getItems())
		{
			assertEquals("frequently-used", item.getLayoutTagKey());
		}
	}

	@Test
	public void explicitTagOrderStillWinsOverDefaultMainRoles()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagShifted("teleports", -1).withTagShifted("teleports", -1);
		BankLayoutOptions list = new BankLayoutOptions(true, true, true,
			Collections.singletonMap(BankCategorySortMode.MAIN, TabOrder.SEQUENTIAL));
		List<Integer> ids = ids(build(bank(995, 13204, 8013),
			UserCategoryOverrides.parse("13204=frequently-used"), plan, list), 0);
		assertTrue(ids.toString(), ids.indexOf(8013) < ids.indexOf(13204));
	}

	@Test
	public void moonSetsStayWholeInTheSetList()
	{
		int[] bankIds = {29004, 29028, 29016, 29007, 29022, 29019, 29010, 29025, 29013};
		BankLayoutOptions options = new BankLayoutOptions(true, true, true,
			Collections.emptyMap(), GearLayout.LIST, PotionDoseOrder.GRAB_AREA,
			RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
		List<Integer> laidOut = ids(build(bank(bankIds), new UserCategoryOverrides(),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options), 1);
		for (List<Integer> set : Arrays.asList(Arrays.asList(29028, 29022, 29025),
			Arrays.asList(29010, 29004, 29007), Arrays.asList(29019, 29013, 29016)))
		{
			int start = laidOut.indexOf(set.get(0));
			assertTrue("Missing set: " + laidOut, start >= 0 && start + 3 <= laidOut.size());
			assertEquals(set, laidOut.subList(start, start + 3));
		}
	}

	@Test
	public void clueEquipmentCanBeAssignedWithoutChangingOtherGear()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("12480=clues,23209=clues");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankOrganizationPreview preview = build(bank(12480, 23209, 11832), overrides,
			plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(preview, plan.destinationOf("clues")).containsAll(Arrays.asList(12480, 23209)));
		assertTrue(ids(preview, plan.destinationOf("gear")).contains(11832));
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int i = 0; i < ids.length; i++) items.add(new BankItemSnapshot(ids[i], 1, i));
		return new BankSnapshot(items);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, UserCategoryOverrides overrides,
		BankLayoutPlan plan, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE, overrides, plan, options);
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().filter(item -> !item.isBlank())
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

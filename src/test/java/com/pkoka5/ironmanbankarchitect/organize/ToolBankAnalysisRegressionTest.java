package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Real catalogue fixtures for an analysis failure reported with fishing tools. */
public class ToolBankAnalysisRegressionTest
{
	private static final List<Integer> ANGLER = Arrays.asList(13258, 13259, 13260, 13261);
	private static final List<Integer> REPORTED_TOOLS = Arrays.asList(305, 307, 303, 301, 309, 590);

	@Test
	public void thirtyItemToolsTabWithFullOutfitAndFishingToolsCanBeAnalyzed()
	{
		List<Integer> ids = new ArrayList<>(ANGLER);
		ids.addAll(REPORTED_TOOLS);
		List<Integer> fillers = Arrays.asList(995, 13204, 6529, 556, 558, 562, 554, 555,
			559, 564, 557, 561, 563, 565, 560, 566, 9075, 21880, 28929, 30843);
		ids.addAll(fillers);
		assertEquals(30, ids.size());
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		// Keep twenty ordinary bank items here so the public preview builder reaches
		// the exact 30-slot Tools category from the report. Its natural outfit and
		// fishing classifications still come from the production catalogue.
		for (int filler : fillers) overrides.put(filler, "tools");
		for (boolean gather : new boolean[]{false, true})
		{
			for (TabOrder order : TabOrder.values())
			{
				BankOrganizationPreview preview = analyze(ids, overrides, gather, order);
				assertConserved(ids, preview);
				int toolsTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("tools");
				assertEquals("The failure fixture must reach all 30 Tools entries", 30,
					preview.getCategories().get(toolsTab).getItemCount());
			}
		}
	}

	@Test
	public void smallOutfitBanksKeepEveryReportedToolWithoutAnAnalysisFailure()
	{
		for (int reported : REPORTED_TOOLS)
		{
			Set<Integer> bankIds = new LinkedHashSet<>(ANGLER);
			bankIds.add(303);
			bankIds.add(307);
			bankIds.add(reported);
			List<Integer> ids = new ArrayList<>(bankIds);
			for (boolean gather : new boolean[]{false, true})
			{
				for (TabOrder order : TabOrder.values())
				{
					assertConserved(ids, analyze(ids, new UserCategoryOverrides(), gather, order));
				}
			}
		}
	}

	private static BankOrganizationPreview analyze(List<Integer> ids, UserCategoryOverrides overrides,
		boolean gather, TabOrder order)
	{
		List<BankItemSnapshot> bankItems = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++)
		{
			int itemId = ids.get(i);
			assertTrue("Fixture item must have canonical metadata: " + itemId,
				CompositeItemCatalog.DEFAULT.findById(itemId).isPresent());
			bankItems.add(new BankItemSnapshot(itemId, 1, i));
		}
		BankLayoutOptions options = new BankLayoutOptions(true, true, true,
			Collections.singletonMap(BankCategorySortMode.TOOLS, order), GearLayout.GRID_STYLES,
			PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL, gather);
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(bankItems), CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE, overrides,
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options);
	}

	private static void assertConserved(List<Integer> expected, BankOrganizationPreview preview)
	{
		List<BankPreviewItem> actual = preview.getCategories().stream()
			.flatMap(tab -> tab.getItems().stream()).filter(item -> !item.isBlank())
			.collect(Collectors.toList());
		assertEquals("Each bank item must appear exactly once", expected.size(), actual.size());
		assertEquals(new LinkedHashSet<>(expected), actual.stream().map(BankPreviewItem::getItemId)
			.collect(Collectors.toSet()));
		assertEquals("Quantities must survive layout", expected.size(),
			actual.stream().mapToInt(BankPreviewItem::getQuantity).sum());
	}
}

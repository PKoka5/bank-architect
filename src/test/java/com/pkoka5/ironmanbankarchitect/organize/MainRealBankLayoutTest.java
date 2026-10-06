package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.RearrangeMode;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class MainRealBankLayoutTest
{
	@Test
	public void defaultMainLayoutHandlesTheFullReviewedRealBank() throws IOException
	{
		List<Integer> itemIds = realBankIds();
		assertEquals(770, itemIds.size());

		assertStableDenseLayout(itemIds, BankLayoutOptions.DEFAULTS, "reviewed 770-item bank");
	}

	@Test
	public void an817ItemBankHandlesEveryGearLayoutWithRowFillingOnAndOff() throws IOException
	{
		Set<Integer> extended = new LinkedHashSet<>(realBankIds());
		for (int itemId : RandomBankSimulator.loadItemUniverse())
		{
			if (extended.size() == 817) break;
			extended.add(itemId);
		}
		assertEquals(817, extended.size());
		List<Integer> itemIds = new ArrayList<>(extended);

		for (GearLayout gear : GearLayout.values())
		{
			for (boolean fill : new boolean[]{true, false})
			{
				BankLayoutOptions options = new BankLayoutOptions(fill, true, true,
					Collections.emptyMap(), gear, PotionDoseOrder.GRAB_AREA,
					RuneOrder.ELEMENTAL, TeleportOrder.ALPHABETICAL);
				assertStableDenseLayout(itemIds, options, gear + ", fill=" + fill);
			}
		}
	}

	private static void assertStableDenseLayout(List<Integer> itemIds, BankLayoutOptions options,
		String label)
	{
		BankLayoutPlan layout = BankLayoutPlan.defaultFor(BankPresets.MAIN);
		BankOrganizationPreview original = preview(itemIds, layout, options);
		List<Integer> expected = new ArrayList<>(itemIds);
		List<Integer> actual = ids(original.getPlannedItems());
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(label + ": every physical item is present exactly once", expected, actual);
		assertFalse(label + ": no empty targets", original.getPlannedItems().stream()
			.anyMatch(BankPreviewItem::isBlank));
		assertEquals(10, original.getCategories().size());

		List<Integer> rescanned = new ArrayList<>(itemIds);
		Collections.shuffle(rescanned, new Random(20261005L));
		BankOrganizationPreview reordered = preview(rescanned, layout, options);
		for (int destination = 0; destination < 10; destination++)
		{
			assertEquals(label + ": rescan changed destination " + destination,
				ids(original.getCategories().get(destination).getItems()),
				ids(reordered.getCategories().get(destination).getItems()));
		}

		BankTabPlan tabs = BankTabPlan.fromPreview(original, layout);
		int[] actualItems = tabs.getFlattenedItems().stream()
			.mapToInt(BankPreviewItem::getItemId).toArray();
		int[] tabCounts = new int[TabRouteAdvisor.MAX_TABS];
		for (BankTabPlan.TargetTab tab : tabs.getNumberedTabs())
		{
			tabCounts[tab.getBankTabNumber() - 1] = tab.getItems().size();
		}
		for (RearrangeMode mode : RearrangeMode.values())
		{
			assertEquals(label + ": finished guidance with " + mode,
				TabRouteAdvisor.Status.COMPLETE,
				TabRouteAdvisor.assess(actualItems, tabs, tabCounts, 0, mode).getStatus());
		}
	}

	private static BankOrganizationPreview preview(List<Integer> itemIds, BankLayoutPlan layout,
		BankLayoutOptions options)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int slot = 0; slot < itemIds.size(); slot++)
		{
			items.add(new BankItemSnapshot(itemIds.get(slot), 1, slot));
		}
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(items),
			CompositeItemCatalog.DEFAULT, BankPresets.MAIN, GearStatsSource.NONE,
			ItemValueSource.NONE, CategoryOverrideSource.NONE, layout, options);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<Integer> realBankIds() throws IOException
	{
		String path = "/com/pkoka5/ironmanbankarchitect/organize/real-bank-fixture-2026-07-19.tsv";
		InputStream stream = MainRealBankLayoutTest.class.getResourceAsStream(path);
		assertTrue("missing real-bank fixture", stream != null);
		List<Integer> itemIds = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream,
			StandardCharsets.UTF_8)))
		{
			assertEquals("itemId\texpectedTabKey", reader.readLine());
			String line;
			while ((line = reader.readLine()) != null)
			{
				itemIds.add(Integer.parseInt(line.substring(0, line.indexOf('\t'))));
			}
		}
		return itemIds;
	}
}

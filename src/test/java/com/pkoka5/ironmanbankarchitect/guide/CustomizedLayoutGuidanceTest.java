package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Cross-feature checks use physical occurrences, including quantities and placeholders. */
public class CustomizedLayoutGuidanceTest
{
	@Test
	public void customizedLayoutsKeepEveryOccurrenceAndBothGuideModesFinish()
	{
		List<BankItemSnapshot> raw = new ArrayList<>();
		for (int id : new int[]{995, 1161, 1161, 882, 2959, 2434, 139, 141, 143,
			5318, 5504, 11850, 11852, 1755, 952, 1042, 1038})
		{
			raw.add(new BankItemSnapshot(id, raw.size() + 1, raw.size()));
		}
		raw.add(new BankItemSnapshot(1161, 0, raw.size(), true));
		BankLayoutPlan layout = BankLayoutPlan.parse(BankPresets.IRONMAN,
			"currency+frequently-used|ammunition+cleanup+gear|food+potions|runes+teleports"
				+ "|tools+skilling-outfits+containers|raw-resources+gems+ammo-components"
				+ "|produce+grimy-herbs+clean-herbs+herb-seeds+seeds+unfinished-potions+secondaries+potion-doses+herblore-other"
				+ "|cosmetics+clues+collection-log|quest-items|boss-loot");
		CategoryOverrideSource corrections = id -> id == 1161 ? Optional.of("ammunition")
			: id == 5318 ? Optional.of("produce") : Optional.empty();
		Map<String, Integer> expected = new HashMap<>();
		for (BankItemSnapshot item : raw)
		{
			expected.merge(item.getItemId() + ":" + item.getQuantity() + ":" + item.isPlaceholder(), 1, Integer::sum);
		}
		int combinations = 0;
		for (GearLayout gear : GearLayout.values())
		for (PotionDoseOrder doses : PotionDoseOrder.values())
		for (TabOrder order : TabOrder.values())
		for (boolean gather : new boolean[]{false, true})
		for (boolean arranged : new boolean[]{false, true})
		{
			Map<BankCategorySortMode, TabOrder> orders = new EnumMap<>(BankCategorySortMode.class);
			for (BankCategorySortMode mode : BankCategorySortMode.values()) orders.put(mode, order);
			BankLayoutOptions options = new BankLayoutOptions(true, true, false, orders, gear,
				doses, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL, gather);
			if (arranged)
			{
				options = options.withBlockArrangements(BlockArrangements.parse(
					BlockArrangements.EMPTY.withTag("ammunition", Arrays.asList("set:gear.adamant-armour", "item:882"))
						.withTag("cosmetics", Arrays.asList("set:cosmetic-family.partyhats")).serialize()));
			}
			String context = gear + "/" + doses + "/" + order + "/" + gather + "/" + arranged;
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(raw),
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
				corrections, layout, options);
			Map<String, Integer> actual = new HashMap<>();
			for (int destination = 0; destination < preview.getCategories().size(); destination++)
			for (BankPreviewItem item : preview.getCategories().get(destination).getItems())
			{
				assertFalse(context, item.isBlank());
				actual.merge(item.getItemId() + ":" + item.getQuantity() + ":" + item.isPlaceholder(), 1, Integer::sum);
				if (item.getItemId() == 1161) assertEquals(context, layout.destinationOf("ammunition"), destination);
				if (item.getItemId() == 5318) assertEquals(context, layout.destinationOf("produce"), destination);
			}
			assertEquals(context, expected, actual);
			BankTabPlan plan = BankTabPlan.fromPreview(preview);
			for (RearrangeMode mode : RearrangeMode.values()) verifySorting(plan, mode, context);
			int[] missing = plan.getFlattenedItems().stream().mapToInt(BankPreviewItem::getItemId).toArray();
			missing = Arrays.copyOfRange(missing, 1, missing.length);
			assertFalse(context, TabRouteAdvisor.assess(missing, plan, new int[9]).getMove().isPresent());
			combinations++;
		}
		assertEquals(48, combinations);
	}

	private static void verifySorting(BankTabPlan plan, RearrangeMode mode, String context)
	{
		List<Integer> live = new ArrayList<>();
		int[] counts = new int[9];
		Random random = new Random(20260907);
		for (BankTabPlan.TargetTab tab : plan.getNumberedTabs())
		{
			counts[tab.getBankTabNumber() - 1] = tab.getItems().size();
			appendShuffled(live, tab.getItems(), random);
		}
		appendShuffled(live, plan.getMainItems(), random);
		TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
		int previousEstimate = Integer.MAX_VALUE;
		for (int step = 0; step <= live.size() * 2; step++)
		{
			int[] ids = live.stream().mapToInt(Integer::intValue).toArray();
			TabRouteAdvisor.Assessment advice = session.assess(ids, plan, counts, step + 1, 1, mode);
			if (advice.getStatus() == TabRouteAdvisor.Status.COMPLETE)
			{
				assertArrayEquals(context, plan.getFlattenedItems().stream().mapToInt(BankPreviewItem::getItemId).toArray(), ids);
				return;
			}
			assertEquals(context, TabRouteAdvisor.Status.READY, advice.getStatus());
			if (mode == RearrangeMode.INSERT)
			{
				int estimate = advice.getProgress().getRemainingDragsEstimate();
				if (previousEstimate != Integer.MAX_VALUE) assertEquals(context, previousEstimate - 1, estimate);
				previousEstimate = estimate;
			}
			TabRouteAdvisor.Move move = advice.getMove().get();
			assertEquals(context, move.getItemId(), (int) live.get(move.getFromSlot()));
			assertEquals(context, mode == RearrangeMode.SWAP ? TabRouteAdvisor.MoveType.SWAP_SECTION
				: TabRouteAdvisor.MoveType.INSERT_SECTION, move.getType());
			if (mode == RearrangeMode.SWAP) Collections.swap(live, move.getFromSlot(), move.getToSlot());
			else live.add(move.getToSlot(), live.remove(move.getFromSlot()));
		}
		fail("Guidance did not finish: " + context + "/" + mode);
	}

	private static void appendShuffled(List<Integer> live, List<BankPreviewItem> items, Random random)
	{
		List<Integer> section = new ArrayList<>();
		for (BankPreviewItem item : items) section.add(item.getItemId());
		Collections.shuffle(section, random);
		live.addAll(section);
	}
}

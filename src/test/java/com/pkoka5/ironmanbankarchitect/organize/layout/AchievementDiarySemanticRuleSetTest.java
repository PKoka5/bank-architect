package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AchievementDiarySemanticRuleSetTest
{
	@Test
	public void twelveRegionRewardsBecomeACompactFourByThreeBlock()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		List<Integer> fallback = new ArrayList<>();
		for (int index = 0; index < 16; index++)
		{
			add(entries, fallback, 900000 + index, "Currency " + index);
		}
		String[] rewards = {"Ardougne cloak 4", "Desert amulet 4", "Explorer's ring 4",
			"Falador shield 4", "Fremennik sea boots 4", "Kandarin headgear 4",
			"Karamja gloves 4", "Morytania legs 4", "Rada's blessing 4",
			"Varrock armour 4", "Western banner 4", "Wilderness sword 4"};
		List<Integer> rewardIds = new ArrayList<>();
		for (int index = 0; index < rewards.length; index++)
		{
			int itemId = 910000 + index;
			add(entries, fallback, itemId, rewards[index]);
			rewardIds.add(itemId);
		}

		LayoutResult result = new SemanticBlockLayoutEngine().plan(
			AchievementDiarySemanticRuleSet.forEntries(entries), fallback);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		int first = targetFor(result, rewardIds.get(0));
		for (int index = 0; index < rewardIds.size(); index++)
		{
			assertEquals(first + (index / 4) * 8 + index % 4,
				targetFor(result, rewardIds.get(index)));
		}
	}

	@Test
	public void oneDiaryRewardDoesNotInventAMatrix()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		List<Integer> fallback = new ArrayList<>();
		add(entries, fallback, 900001, "Coins");
		add(entries, fallback, 910001, "Ardougne cloak 2");

		LayoutRequest request = AchievementDiarySemanticRuleSet.forEntries(entries);
		LayoutResult result = new SemanticBlockLayoutEngine().plan(request,
			Arrays.asList(910001, 900001));

		assertTrue(request.getRules().isEmpty());
		assertTrue(result.getConflicts().toString(), result.isSuccess());
		assertEquals(0, targetFor(result, 910001));
		assertEquals(1, targetFor(result, 900001));
	}

	private static void add(List<LayoutEntry> entries, List<Integer> fallback, int itemId,
		String name)
	{
		BankPreviewItem item = new BankPreviewItem(new CatalogItem(itemId, name,
			ItemCategory.CURRENCY, "currency", Collections.emptySet(), null), 1);
		entries.add(LayoutEntry.of(item, entries.size() * 13));
		fallback.add(itemId);
	}

	private static int targetFor(LayoutResult result, int itemId)
	{
		for (LayoutPlacement placement : result.getPlacements())
		{
			if (placement.getItem().getItemId() == itemId)
			{
				return placement.getTargetIndex();
			}
		}
		throw new AssertionError("missing itemId " + itemId);
	}
}

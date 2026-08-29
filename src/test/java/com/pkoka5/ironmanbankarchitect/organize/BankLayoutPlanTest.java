package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BankLayoutPlanTest
{
	private static final BankPreset PRESET = BankPresets.IRONMAN;

	@Test
	public void defaultPlanKeepsEachCategorysTagsTogetherOnItsOwnDestination()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET);

		List<BankCategory> categories = PRESET.getCategories();
		assertEquals(BankLayoutPlan.DESTINATION_COUNT, plan.getDestinations().size());
		for (int index = 0; index < categories.size(); index++)
		{
			List<String> expected = new ArrayList<>();
			for (BankTag tag : BankTags.forCategory(categories.get(index).getKey()))
			{
				expected.add(tag.getKey());
			}
			assertEquals(expected, plan.getTagKeys(index));
		}
		assertTrue(plan.isDefault(PRESET));
	}

	/** The reported case: food leaves the potions tab without taking potions along. */
	@Test
	public void oneTagOfABundleCanMoveWithoutTheRest()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET).withTagAt("food", 8);

		assertEquals(8, plan.destinationOf("food"));
		assertFalse(plan.getTagKeys(8).contains("potions"));
		assertFalse(plan.destinationOf("potions") == 8);
		assertFalse(plan.isDefault(PRESET));
	}

	@Test
	public void everyTagOfThePlanHasExactlyOneDestination()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET)
			.withTagAt("runes", 2)
			.withTagAt("teleports", 5);

		for (BankTag tag : BankTags.all())
		{
			assertTrue(tag.getKey(), plan.destinationOf(tag.getKey()) >= 0);
			assertEquals(tag.getKey(), 1, countPlacements(plan, tag.getKey()));
		}
	}

	@Test
	public void mainSectionCanBeLeftEmpty()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET);
		for (String key : new ArrayList<>(plan.getTagKeys(BankLayoutPlan.MAIN_DESTINATION_INDEX)))
		{
			plan = plan.withTagAt(key, 5);
		}

		assertTrue(plan.getTagKeys(BankLayoutPlan.MAIN_DESTINATION_INDEX).isEmpty());
	}

	@Test
	public void shiftingWithinADestinationDecidesWhichTagLeadsTheTab()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET).withTagAt("food", 0);
		List<String> before = plan.getTagKeys(0);

		BankLayoutPlan shifted = plan.withTagShifted("food", -1);

		assertEquals(before.size(), shifted.getTagKeys(0).size());
		assertEquals(before.get(before.size() - 2),
			shifted.getTagKeys(0).get(before.size() - 1));
	}

	@Test
	public void shiftingPastTheEndOfADestinationIsIgnored()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET);
		String first = plan.getTagKeys(1).get(0);

		assertSame(plan, plan.withTagShifted(first, -1));
	}

	@Test
	public void movingATagWhereItAlreadySitsChangesNothing()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET);

		assertSame(plan, plan.withTagAt("potions", plan.destinationOf("potions")));
	}

	@Test
	public void planSurvivesASaveAndLoadRound()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET)
			.withTagAt("food", 8)
			.withTagAt("runes", 3)
			.withTagAt(PRESET.getCategories().get(0).getKey().equals("currency-utilities")
				? "currency" : "currency", 4);

		BankLayoutPlan reloaded = BankLayoutPlan.parse(PRESET, plan.serialize());

		assertEquals(plan.getDestinations(), reloaded.getDestinations());
	}

	@Test
	public void unplacedTagsJoinTheFallbackDestinationRatherThanDisappearing()
	{
		BankLayoutPlan plan = BankLayoutPlan.parse(PRESET,
			"potions|" + BankLayoutPlan.FALLBACK_TAG_KEY + "||||||||");

		assertEquals(1, plan.destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY));
		assertEquals(BankTags.all().size(), placedCount(plan));
		assertTrue(plan.getTagKeys(1).contains("clean-herbs"));
	}

	@Test
	public void remainderFallsToTheLastDestinationWhenTheFallbackTagIsAlsoUnplaced()
	{
		BankLayoutPlan plan = BankLayoutPlan.parse(PRESET, "potions|||||||||");

		assertEquals(BankTags.all().size(), placedCount(plan));
		assertTrue(plan.getTagKeys(BankLayoutPlan.DESTINATION_COUNT - 1)
			.contains(BankLayoutPlan.FALLBACK_TAG_KEY));
	}

	@Test
	public void unknownAndRepeatedKeysAreDroppedInsteadOfBreakingThePlan()
	{
		BankLayoutPlan plan = BankLayoutPlan.parse(PRESET,
			"potions+not-a-tag|potions|||||||food");

		assertEquals(Collections.singletonList("potions"), plan.getTagKeys(0));
		assertTrue(plan.getTagKeys(1).isEmpty());
		assertEquals(BankTags.all().size(), placedCount(plan));
	}

	/** A plan stored before tags existed named categories, and must still load. */
	@Test
	public void aStoredCategoryKeyIsReadAsAllOfItsTags()
	{
		BankLayoutPlan plan = BankLayoutPlan.parse(PRESET, "potions-food|combat-gear||||||||");

		for (BankTag tag : BankTags.forCategory("potions-food"))
		{
			assertEquals(tag.getKey(), 0, plan.destinationOf(tag.getKey()));
		}
		for (BankTag tag : BankTags.forCategory("combat-gear"))
		{
			assertEquals(tag.getKey(), 1, plan.destinationOf(tag.getKey()));
		}
	}

	@Test
	public void theOlderCommaSeparatedCategoryOrderIsStillRead()
	{
		List<String> reversed = new ArrayList<>();
		for (BankCategory category : PRESET.getCategories())
		{
			reversed.add(0, category.getKey());
		}

		BankLayoutPlan plan = BankLayoutPlan.parse(PRESET, String.join(",", reversed));

		for (int index = 0; index < reversed.size(); index++)
		{
			List<String> expected = new ArrayList<>();
			for (BankTag tag : BankTags.forCategory(reversed.get(index)))
			{
				expected.add(tag.getKey());
			}
			assertEquals(expected, plan.getTagKeys(index));
		}
	}

	@Test
	public void blankStoredPlanFallsBackToThePresetArrangement()
	{
		assertTrue(BankLayoutPlan.parse(PRESET, "").isDefault(PRESET));
		assertTrue(BankLayoutPlan.parse(PRESET, null).isDefault(PRESET));
	}

	@Test
	public void severalBundlesCanShareOneTab()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET)
			.withTagAt("cosmetics", 1)
			.withTagAt("collection-log", 1);

		assertTrue(plan.getTagKeys(1).containsAll(Arrays.asList("cosmetics", "collection-log")));
	}

	private static int countPlacements(BankLayoutPlan plan, String tagKey)
	{
		int count = 0;
		for (List<String> destination : plan.getDestinations())
		{
			count += Collections.frequency(destination, tagKey);
		}

		return count;
	}

	private static int placedCount(BankLayoutPlan plan)
	{
		int count = 0;
		for (List<String> destination : plan.getDestinations())
		{
			count += destination.size();
		}

		return count;
	}
}

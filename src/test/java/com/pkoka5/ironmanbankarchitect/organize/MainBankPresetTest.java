package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class MainBankPresetTest
{
	@Test
	public void emptyMainBankStillHasTenEditableDestinations()
	{
		BankOrganizationPreview preview = main(bank());

		assertEquals(10, preview.getCategories().size());
		assertEquals(0, preview.getPlannedItemCount());
		assertTrue(preview.getPlannedItems().isEmpty());
	}

	@Test
	public void defaultMainPlanRoutesTheApprovedItemGroupsWithoutLosingSlots()
	{
		BankSnapshot bank = bank(995, 556, 561, 563, 28929, 8013, 2552, 4251, 9084,
			4587, 861, 4091, 27641, 22324, 25734, 385, 3144, 2434, 139, 141, 143,
			207, 3049, 257, 2998, 5295, 5296, 5318, 5319, 99, 3002, 231,
			440, 4207, 2677, 999999);
		BankOrganizationPreview preview = main(bank);

		assertEquals(BankPresets.MAIN, preview.getPreset());
		assertEquals(10, preview.getCategories().size());
		assertDestinationContains(preview, 0, 995);
		assertDestinationContains(preview, 1, 556, 561, 563, 28929, 8013, 2552, 4251, 9084);
		assertDestinationContains(preview, 2, 4587, 861, 4091, 27641, 22324, 25734);
		assertDestinationContains(preview, 3, 385, 3144, 2434, 139, 141, 143);
		assertDestinationContains(preview, 4, 207, 3049, 257, 2998, 5295, 5296,
			5318, 5319, 99, 3002, 231);
		assertDestinationContains(preview, 6, 440);
		assertDestinationContains(preview, 7, 4207);
		assertDestinationContains(preview, 8, 2677);
		assertDestinationContains(preview, 9, 999999);
		assertPermutationOfBank(bank, preview);
	}

	@Test
	public void mainKeepsEachCompletePotionFamilyTogetherFromFourToOne()
	{
		BankOrganizationPreview preview = main(bank(2434, 123, 385, 143, 121,
			3144, 139, 125, 141, 2428));
		List<Integer> supplies = ids(preview, 3);

		assertContiguousRun(supplies, 2428, 121, 123, 125);
		assertContiguousRun(supplies, 2434, 139, 141, 143);
		assertTrue(supplies.containsAll(Arrays.asList(385, 3144)));
		assertEquals(10, supplies.size());
	}

	@Test
	public void mainDoseOrderDoesNotDependOnTheIronmanDosePreferenceOrPackingMode()
	{
		BankSnapshot bank = bank(2434, 141, 385, 143, 139, 2428, 125, 121, 123);
		for (PotionDoseOrder dosePreference : PotionDoseOrder.values())
		{
			for (TabOrder order : TabOrder.values())
			{
				Map<BankCategorySortMode, TabOrder> orders = new EnumMap<>(BankCategorySortMode.class);
				orders.put(BankCategorySortMode.SUPPLIES, order);
				BankLayoutOptions options = new BankLayoutOptions(true, true, true,
					orders, GearLayout.GRID_STYLES, dosePreference,
					RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
				BankOrganizationPreview preview = main(bank, options, CategoryOverrideSource.NONE);

				assertContiguousRun(ids(preview, 3), 2428, 121, 123, 125);
				assertContiguousRun(ids(preview, 3), 2434, 139, 141, 143);
				assertPermutationOfBank(bank, preview);
			}
		}
	}

	@Test
	public void aSparseDoseFamilyKeepsItsRealPlaceholderWithoutInventingMissingDoses()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(2434, 1, 20),
			new BankItemSnapshot(385, 1, 3),
			new BankItemSnapshot(143, 0, 8, true)));
		BankOrganizationPreview preview = main(bank);

		assertContiguousRun(ids(preview, 3), 2434, 143);
		assertFalse(ids(preview, 3).contains(139));
		assertFalse(ids(preview, 3).contains(141));
		BankPreviewItem placeholder = preview.getCategories().get(3).getItems().stream()
			.filter(item -> item.getItemId() == 143).findFirst().orElseThrow(AssertionError::new);
		assertTrue(placeholder.isPlaceholder());
		assertEquals(0, placeholder.getQuantity());
		assertPermutationOfBank(bank, preview);
	}

	@Test
	public void mainHerbsAndFarmingUseDenseStageRunsInsteadOfRecipeRows()
	{
		BankSnapshot bank = bank(99, 5319, 2998, 231, 3049, 5296, 257, 3002,
			5295, 207, 5318);
		BankOrganizationPreview preview = main(bank);
		List<Integer> ingredients = ids(preview, 4);

		assertEquals(Arrays.asList(207, 3049), ingredients.subList(0, 2));
		assertEquals(Arrays.asList(257, 2998), ingredients.subList(2, 4));
		assertEquals(new HashSet<>(Arrays.asList(5295, 5296, 5318, 5319)),
			new HashSet<>(ingredients.subList(4, 8)));
		assertEquals(Arrays.asList(99, 3002), ingredients.subList(8, 10));
		assertEquals(Integer.valueOf(231), ingredients.get(10));
		assertEquals(11, ingredients.size());
		assertPermutationOfBank(bank, preview);
	}

	@Test
	public void weaponSeedsDoNotJoinTheFarmingSeedStage()
	{
		BankOrganizationPreview preview = main(bank(4207, 5295, 5318));

		assertDestinationContains(preview, 4, 5295, 5318);
		assertFalse(ids(preview, 4).contains(4207));
		assertDestinationContains(preview, 7, 4207);
	}

	@Test
	public void playerCorrectionsWinOverMainHeartAndTravelDefaults()
	{
		BankSnapshot bank = bank(27641, 9084, 995);
		CategoryOverrideSource overrides = itemId -> itemId == 27641
			? Optional.of("currency") : itemId == 9084 ? Optional.of("gear") : Optional.empty();
		BankOrganizationPreview preview = main(bank, BankLayoutOptions.DEFAULTS, overrides);

		assertDestinationContains(preview, 0, 27641, 995);
		assertDestinationContains(preview, 2, 9084);
		assertFalse(ids(preview, 2).contains(27641));
		assertFalse(ids(preview, 1).contains(9084));
		assertPermutationOfBank(bank, preview);
	}

	@Test
	public void mainPoliciesLeaveTheExistingIronmanHomesUnchanged()
	{
		BankSnapshot bank = bank(995, 556, 8013, 9084, 27641, 2434, 139, 141, 143);
		BankOrganizationPreview ironman = BankOrganizationPreviewBuilder.build(bank,
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, GearStatsSource.NONE,
			ItemValueSource.NONE, CategoryOverrideSource.NONE,
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);

		assertDestinationContains(ironman, 0, 995, 556, 8013);
		assertDestinationContains(ironman, 1, 9084);
		assertDestinationContains(ironman, 2, 2434);
		assertDestinationContains(ironman, 3, 139, 141, 143);
		assertDestinationContains(ironman, 9, 27641);
		assertPermutationOfBank(bank, ironman);
	}

	@Test
	public void aCategoryCorrectionForLunarStaffCannotLoseTheItem()
	{
		BankSnapshot bank = bank(9084, 995);
		CategoryOverrideSource overrides = itemId -> itemId == 9084
			? Optional.of("combat-gear") : Optional.empty();
		BankOrganizationPreview preview = main(bank, BankLayoutOptions.DEFAULTS, overrides);

		assertDestinationContains(preview, 2, 9084);
		assertFalse(ids(preview, 1).contains(9084));
		assertPermutationOfBank(bank, preview);
	}

	private static BankOrganizationPreview main(BankSnapshot bank)
	{
		return main(bank, BankLayoutOptions.DEFAULTS, CategoryOverrideSource.NONE);
	}

	private static BankOrganizationPreview main(BankSnapshot bank, BankLayoutOptions options,
		CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			BankPresets.MAIN, GearStatsSource.NONE, ItemValueSource.NONE, overrides,
			BankLayoutPlan.defaultFor(BankPresets.MAIN), options);
	}

	private static BankSnapshot bank(int... itemIds)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			items.add(new BankItemSnapshot(itemIds[index], 1, index));
		}
		return new BankSnapshot(items);
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int destination)
	{
		return preview.getCategories().get(destination).getItems().stream()
			.filter(item -> !item.isBlank()).map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
	}

	private static void assertDestinationContains(BankOrganizationPreview preview,
		int destination, Integer... expected)
	{
		List<Integer> actual = ids(preview, destination);
		assertTrue("destination " + destination + ": " + actual,
			actual.containsAll(Arrays.asList(expected)));
	}

	private static void assertContiguousRun(List<Integer> actual, Integer... run)
	{
		int start = actual.indexOf(run[0]);
		assertTrue("Missing family in " + actual, start >= 0);
		assertTrue("Truncated family in " + actual, start + run.length <= actual.size());
		assertEquals(Arrays.asList(run), actual.subList(start, start + run.length));
	}

	private static void assertPermutationOfBank(BankSnapshot bank, BankOrganizationPreview preview)
	{
		List<Integer> expected = bank.getItems().stream().map(BankItemSnapshot::getItemId)
			.collect(Collectors.toList());
		List<Integer> actual = preview.getPlannedItems().stream().map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
		assertEquals("Blank or duplicate physical slots in " + actual, expected.size(), actual.size());
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}
}

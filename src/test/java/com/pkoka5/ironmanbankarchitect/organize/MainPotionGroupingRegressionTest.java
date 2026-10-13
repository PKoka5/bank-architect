package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainPotionGroupingRegressionTest
{
	private static final List<Integer> COMPLETE_ROW = Arrays.asList(
		2428, 121, 123, 125, 2434, 139, 141, 143);
	private static final BankLayoutPlan MAIN_PLAN = BankLayoutPlan.defaultFor(BankPresets.MAIN);
	private static final int SUPPLIES = MAIN_PLAN.destinationOf("potions");

	@Test
	public void earlyLoneDoseCannotOffsetTwoCompleteFamilies()
	{
		BankSnapshot bank = bank(179, 2428, 121, 123, 125, 2434, 139, 141, 143);
		BankOrganizationPreview preview = main(bank);
		List<Integer> expected = new ArrayList<>(COMPLETE_ROW);
		expected.add(179);

		assertEquals(expected, supplies(preview));
		assertEquals(COMPLETE_ROW, supplies(preview).subList(0, 8));
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void completeFamilyPriorityDoesNotDependOnSourceSlotOrCaptureOrder()
	{
		List<Integer> ids = new ArrayList<>(COMPLETE_ROW);
		ids.addAll(Arrays.asList(179, 2452, 2456, 385));
		List<Integer> expected = new ArrayList<>(COMPLETE_ROW);
		expected.addAll(Arrays.asList(2452, 2456, 179, 385));
		for (int seed = 0; seed < 12; seed++)
		{
			Collections.shuffle(ids, new Random(seed));
			BankSnapshot bank = bank(ids.toArray(new Integer[0]));
			BankOrganizationPreview preview = main(bank);
			assertEquals("capture order seed " + seed, expected, supplies(preview));
			assertPhysicalItemsPreserved(bank, preview);
		}
	}

	@Test
	public void realPlaceholderCompletesItsDoseFamilyAndKeepsItsPhysicalCell()
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		entries.add(new BankItemSnapshot(179, 1, 7));
		for (int index = 0; index < COMPLETE_ROW.size(); index++)
		{
			int id = COMPLETE_ROW.get(index);
			entries.add(new BankItemSnapshot(id, id == 121 ? 0 : 20, 20 + index * 11, id == 121));
		}
		BankSnapshot bank = new BankSnapshot(entries);
		BankOrganizationPreview preview = main(bank);
		List<BankPreviewItem> supplies = preview.getCategories().get(SUPPLIES).getItems();

		assertEquals(COMPLETE_ROW, supplies(preview).subList(0, 8));
		assertEquals(121, supplies.get(1).getItemId());
		assertTrue(supplies.get(1).isPlaceholder());
		assertEquals(0, supplies.get(1).getQuantity());
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void unknownAndMinigameLookalikesCannotCompleteACanonicalFamily()
	{
		BankSnapshot bank = bank(175, 177, 179, 20393, 999004, 2434, 139, 141, 143);
		ItemCatalog catalog = id -> id == 20393 || id == 999004
			? java.util.Optional.of(new CatalogItem(id, "Antipoison(4)", ItemCategory.POTION,
				"potion-dose-4", Collections.emptySet(), null))
			: CompositeItemCatalog.DEFAULT.findById(id);
		BankOrganizationPreview preview = main(bank, catalog, BankLayoutOptions.defaultFor(BankPresets.MAIN));
		List<Integer> supplies = supplies(preview);

		assertEquals(Arrays.asList(2434, 139, 141, 143), supplies.subList(0, 4));
		assertEquals(Arrays.asList(175, 177, 179), supplies.subList(4, 7));
		assertFalse(supplies.contains(2446));
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void oddNumberOfCompleteFamiliesStillPrecedesPartialsWithoutInventedCells()
	{
		BankSnapshot bank = bank(179, 2428, 139, 143, 2434, 141);
		BankOrganizationPreview preview = main(bank);

		assertEquals(Arrays.asList(2434, 139, 141, 143, 179, 2428), supplies(preview));
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void oddCompleteRegionUsesRealFoodToStartIncompleteFamiliesOnTheNextRow()
	{
		List<Integer> input = new ArrayList<>(COMPLETE_ROW);
		input.addAll(Arrays.asList(113,115,117,119,179,333,379,373,385));
		BankSnapshot bank = bank(input.toArray(new Integer[0]));
		List<Integer> expected = new ArrayList<>(COMPLETE_ROW);
		expected.addAll(Arrays.asList(113,115,117,119,385,373,379,333,179));
		BankOrganizationPreview preview = main(bank);
		assertEquals(expected, supplies(preview));
		assertEquals("the incomplete section starts below all complete-family rows", 16,
			supplies(preview).indexOf(179));
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void explicitSequentialSupplyOrderDoesNotBorrowFoodForRows()
	{
		BankSnapshot bank = bank(2434,139,141,143,179,333,379,373,385);
		BankLayoutOptions options = new BankLayoutOptions(true,false,true,
			Collections.singletonMap(BankCategorySortMode.SUPPLIES,TabOrder.SEQUENTIAL),
			GearLayout.GRID_STYLES,PotionDoseOrder.BY_FAMILY,RuneOrder.ELEMENTAL,
			TeleportOrder.ALPHABETICAL,false);
		BankOrganizationPreview preview = main(bank,CompositeItemCatalog.DEFAULT,options);
		assertEquals(Arrays.asList(2434,139,141,143,179,385,373,379,333), supplies(preview));
		assertPhysicalItemsPreserved(bank,preview);
	}

	@Test
	public void defaultIronmanStillKeepsPartialDosesInItsHerbloreRecipeTab()
	{
		BankSnapshot bank = bank(179, 2428, 121, 123, 125, 2434, 139, 141, 143);
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
			CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(BankPresets.IRONMAN));

		assertEquals(Arrays.asList(2428, 2434), ids(preview.getCategories()
			.get(plan.destinationOf("potions")).getItems()));
		List<Integer> partials = ids(preview.getCategories().get(plan.destinationOf("potion-doses")).getItems());
		assertTrue(partials.containsAll(Arrays.asList(179, 121, 123, 125, 139, 141, 143)));
		assertFalse(partials.contains(2428));
		assertFalse(partials.contains(2434));
		assertPhysicalItemsPreserved(bank, preview);
	}

	@Test
	public void savedPhysicalBlueprintOrderWinsOverAutomaticDosePriority()
	{
		BankSnapshot bank = bank(179, 2428, 121, 123, 125, 2434, 139, 141, 143);
		List<Integer> manual = Arrays.asList(179, 143, 141, 139, 2434, 125, 123, 121, 2428);
		BlueprintItemOrders orders = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(SUPPLIES, manual).serialize());
		BankOrganizationPreview preview = main(bank, CompositeItemCatalog.DEFAULT,
			BankLayoutOptions.defaultFor(BankPresets.MAIN).withItemOrders(orders));

		assertEquals(manual, supplies(preview));
		assertTrue(preview.getCategories().get(SUPPLIES).hasManualOrder());
		assertPhysicalItemsPreserved(bank, preview);
	}

	private static BankOrganizationPreview main(BankSnapshot bank)
	{
		return main(bank, CompositeItemCatalog.DEFAULT, BankLayoutOptions.defaultFor(BankPresets.MAIN));
	}

	private static BankOrganizationPreview main(BankSnapshot bank, ItemCatalog catalog, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, catalog, BankPresets.MAIN,
			GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, MAIN_PLAN, options);
	}

	private static BankSnapshot bank(Integer... ids)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int index = 0; index < ids.length; index++)
			entries.add(new BankItemSnapshot(ids[index], 1, 9 + index * 13));
		return new BankSnapshot(entries);
	}

	private static List<Integer> supplies(BankOrganizationPreview preview)
	{
		return ids(preview.getCategories().get(SUPPLIES).getItems());
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertPhysicalItemsPreserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		List<Integer> expected = bank.getPhysicalItems().stream().map(BankItemSnapshot::getItemId)
			.sorted().collect(Collectors.toList());
		List<Integer> actual = preview.getCategories().stream().flatMap(category -> category.getItems().stream())
			.peek(item -> assertFalse("no invented blank cells", item.isBlank()))
			.map(BankPreviewItem::getItemId).sorted().collect(Collectors.toList());
		assertEquals(expected, actual);
	}
}

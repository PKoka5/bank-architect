package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadataCatalog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SupplyItemSorterTest
{
	private static final ItemSortMetadataCatalog NO_METADATA = itemId -> Optional.empty();

	@Test
	public void keepsAllFullDosePotionsAheadOfUtilitiesFoodAndDrinks()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(1, "Shark", "food"), item(2, "Super energy(4)", "potion-dose-4"),
			item(3, "Prayer potion(4)", "potion-dose-4"), item(4, "Holy wrench", "pvm-utility"),
			item(5, "Sanfew serum(4)", "potion-dose-4"), item(6, "Bandit's brew", "drink"),
			item(7, "Superattack mix(2)", "potion")), NO_METADATA);

		assertEquals(Arrays.asList("Prayer potion(4)", "Sanfew serum(4)", "Super energy(4)",
			"Holy wrench", "Superattack mix(2)", "Shark", "Bandit's brew"), names(sorted));
	}

	@Test
	public void keepsFullAndHalfPiesInOneFamily()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(1, "Half a botanical pie", "food"), item(2, "Cake", "food"),
			item(3, "Botanical pie", "food")), NO_METADATA);

		assertEquals(Arrays.asList("Botanical pie", "Half a botanical pie", "Cake"), names(sorted));
	}

	@Test
	public void sortsStandardFoodByPinnedDirectHealingInsteadOfAlphabetically()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(379, "Lobster", "food"), item(385, "Shark", "food"),
			item(373, "Swordfish", "food"), item(32352, "Marlin", "food")));

		assertEquals(Arrays.asList("Marlin", "Shark", "Swordfish", "Lobster"), names(sorted));
	}

	@Test
	public void usesStableMaximumForVariableFoodWithoutReadingLiveStats()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(385, "Shark", "food"), item(13441, "Anglerfish", "food"),
			item(391, "Manta ray", "food")));

		assertEquals(Arrays.asList("Manta ray", "Anglerfish", "Shark"), names(sorted));
	}

	@Test
	public void separatesCombatRolesAndUsesPerActionRatherThanTotalHealing()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(2303, "1/2 pineapple pizza", "food"),
			item(29143, "Cooked moonlight antelope", "food"),
			item(3144, "Cooked karambwan", "food"),
			item(2301, "Pineapple pizza", "food"),
			item(385, "Shark", "food"),
			item(333, "Trout", "food")));

		assertEquals(Arrays.asList("Shark", "Cooked karambwan", "Cooked moonlight antelope",
			"Pineapple pizza", "1/2 pineapple pizza", "Trout"), names(sorted));
	}

	@Test
	public void unknownNamesCannotSpoofCuratedHealingFacts()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(999_999, "Marlin", "food"), item(379, "Lobster", "food")));

		assertEquals(Arrays.asList("Lobster", "Marlin"), names(sorted));
	}

	@Test
	public void keepsNormalAndBlightedFamilyTogetherWithUnrestrictedFirst()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(24589, "Blighted manta ray", "food"), item(391, "Manta ray", "food")));

		assertEquals(Arrays.asList("Manta ray", "Blighted manta ray"), names(sorted));
	}

	@Test
	public void groupsCuratedPotionFamiliesAndOrdersEachOneFromFourToOne()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(143, "Prayer potion(1)", "potion-dose-1"),
			item(121, "Attack potion(3)", "potion-dose-3"),
			item(2434, "Prayer potion(4)", "potion-dose-4"),
			item(125, "Attack potion(1)", "potion-dose-1"),
			item(141, "Prayer potion(2)", "potion-dose-2"),
			item(2428, "Attack potion(4)", "potion-dose-4"),
			item(139, "Prayer potion(3)", "potion-dose-3"),
			item(123, "Attack potion(2)", "potion-dose-2")));

		assertEquals(Arrays.asList("Attack potion(4)", "Attack potion(3)",
			"Attack potion(2)", "Attack potion(1)", "Prayer potion(4)",
			"Prayer potion(3)", "Prayer potion(2)", "Prayer potion(1)"), names(sorted));
	}

	@Test
	public void usesExplicitDoseFactsInsteadOfItemIdArithmetic()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(117, "Strength potion(2)", "potion-dose-2"),
			item(113, "Strength potion(4)", "potion-dose-4"),
			item(119, "Strength potion(1)", "potion-dose-1"),
			item(115, "Strength potion(3)", "potion-dose-3")));

		assertEquals(Arrays.asList("Strength potion(4)", "Strength potion(3)",
			"Strength potion(2)", "Strength potion(1)"), names(sorted));
	}

	@Test
	public void lookalikeAndMinigameIdsCannotJoinACuratedPotionFamily()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(999_999, "Prayer potion(4)", "potion-dose-4"),
			item(143, "Altered canonical label", "potion-dose-1"),
			item(20_393, "Prayer potion(4)", "potion-dose-4"),
			item(2434, "Another altered label", "potion-dose-4")));

		assertEquals(Arrays.asList("Another altered label", "Altered canonical label",
			"Prayer potion(4)", "Prayer potion(4)"), names(sorted));
		assertEquals(20_393, sorted.get(2).getItemId());
		assertEquals(999_999, sorted.get(3).getItemId());
	}

	@Test
	public void barbarianMixesStayOutsideTheStandardDoseGrid()
	{
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(2434, "Prayer potion(4)", "potion-dose-4"),
			item(143, "Prayer potion(1)", "potion-dose-1"),
			item(11429, "Superattack mix(2)", "potion-dose-2")));

		assertEquals(Arrays.asList("Prayer potion(4)", "Prayer potion(1)",
			"Superattack mix(2)"), names(sorted));
	}

	@Test
	public void realPotionPlaceholderKeepsItsCanonicalDosePosition()
	{
		BankPreviewItem placeholder = new BankPreviewItem(new CatalogItem(6687, "Saradomin brew(3)",
			ItemCategory.POTION, "potion-dose-3", Collections.emptySet(), null), 0, true);
		List<BankPreviewItem> sorted = SupplyItemSorter.sort(Arrays.asList(
			item(6689, "Saradomin brew(2)", "potion-dose-2"), placeholder,
			item(6685, "Saradomin brew(4)", "potion-dose-4")));

		assertEquals(Arrays.asList("Saradomin brew(4)", "Saradomin brew(3)",
			"Saradomin brew(2)"), names(sorted));
		assertTrue(sorted.get(1).isPlaceholder());
		assertEquals(0, sorted.get(1).getQuantity());
	}

	private static BankPreviewItem item(int id, String name, String subcategory)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.POTION, subcategory,
			Collections.emptySet(), null), 1);
	}

	private static List<String> names(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getDisplayName).collect(Collectors.toList());
	}
}

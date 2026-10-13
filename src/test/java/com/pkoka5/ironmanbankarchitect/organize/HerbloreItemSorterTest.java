package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class HerbloreItemSorterTest
{
	private static final int[] GUAM = {199, 249, 5291, 91, 221, 121, 123, 125};
	private static final int[] RANARR = {207, 257, 5295, 99, 231, 139, 141, 143};
	private static final int[] IRIT = {209, 259, 5297, 101, 221, 145, 147, 149};
	private static final int[] HARRALANDER = {205, 255, 5294, 97, 1975, 3010, 3012, 3014};

	@Test
	public void canonicalIdsRecognizeHerbStagesWithoutDependingOnDisplayNames()
	{
		List<BankPreviewItem> input = Arrays.asList(renamed(249, "Renamed clean input"),
			renamed(91, "Renamed unfinished input"), renamed(199, "Renamed grimy input"));

		assertEquals(Arrays.asList(199, 249, 91), ids(HerbloreItemSorter.layout(input)));
	}

	@Test
	public void completeRecipeUsesTheEightActualIngredientsInCellOrder()
	{
		List<BankPreviewItem> input = items(RANARR);
		Collections.reverse(input);

		assertEquals(asList(RANARR), ids(HerbloreItemSorter.layout(input)));
		assertConserved(input, HerbloreItemSorter.layout(input));
	}

	@Test
	public void everyMissingRecipeCellFallsBackToUnbrokenRunsByKind()
	{
		for (int missing : RANARR)
		{
			List<BankPreviewItem> input = items(249, 199);
			for (int id : RANARR)
			{
				if (id != missing) input.add(item(id));
			}
			List<Integer> expected = new ArrayList<>(Arrays.asList(
				199, 207, 249, 257, 5295, 99, 231, 139, 141, 143));
			expected.remove(Integer.valueOf(missing));

			List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
			assertEquals("Missing recipe ingredient " + missing, expected, ids(actual));
			assertConserved(input, actual);
		}
	}

	@Test
	public void twoCompleteRowsStayAlignedBeforeTypeSortedLeftovers()
	{
		List<BankPreviewItem> input = items(IRIT);
		input.addAll(items(RANARR));
		input.addAll(items(2483, 249, 5302, 199, 2481, 5291, 2485, 91));

		List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
		List<Integer> expected = asList(RANARR);
		expected.addAll(asList(IRIT));
		expected.addAll(Arrays.asList(199, 2485, 249, 2481, 5291, 5302, 91, 2483));
		assertEquals(expected, ids(actual));
		assertEquals(0, ids(actual).indexOf(207) % 8);
		assertEquals(0, ids(actual).indexOf(209) % 8);
		assertConserved(input, actual);
	}

	@Test
	public void rowOptionOffGroupsEvenCompleteRecipesByKind()
	{
		List<BankPreviewItem> input = items(IRIT);
		input.addAll(items(RANARR));

		List<BankPreviewItem> actual = HerbloreItemSorter.layout(input, false);
		assertEquals(Arrays.asList(207, 209, 257, 259, 5295, 5297, 99, 101,
			221, 231, 139, 141, 143, 145, 147, 149), ids(actual));
		assertConserved(input, actual);
	}

	@Test
	public void incompleteHigherRecipeCannotStealACompleteLowerRecipesSecondary()
	{
		List<BankPreviewItem> input = items(GUAM);
		input.addAll(items(209, 259, 5297, 101, 145, 147));

		List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
		List<Integer> expected = asList(GUAM);
		expected.addAll(Arrays.asList(209, 259, 5297, 101, 145, 147));
		assertEquals(expected, ids(actual));
		assertConserved(input, actual);
	}

	@Test
	public void sharedSecondaryBelongsOnceToTheHighestCompleteRecipe()
	{
		List<BankPreviewItem> input = items(IRIT);
		input.addAll(items(199, 249, 5291, 91, 121, 123, 125));

		List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
		List<Integer> expected = asList(IRIT);
		expected.addAll(Arrays.asList(199, 249, 5291, 91, 121, 123, 125));
		assertEquals(expected, ids(actual));
		assertEquals(1, Collections.frequency(ids(actual), 221));
		assertConserved(input, actual);
	}

	@Test
	public void withdrawnPlaceholdersKeepEveryCompleteRecipeCell()
	{
		for (int withdrawn : RANARR)
		{
			List<BankPreviewItem> input = new ArrayList<>();
			for (int id : RANARR)
			{
				input.add(id == withdrawn
					? new BankPreviewItem(catalog(id), 0, true) : item(id));
			}
			input.addAll(items(199, 249));
			List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
			assertEquals(asList(RANARR), ids(actual.subList(0, 8)));
			BankPreviewItem placeholder = actual.get(ids(actual).indexOf(withdrawn));
			assertTrue(placeholder.isPlaceholder());
			assertEquals(0, placeholder.getQuantity());
			assertConserved(input, actual);
		}
	}

	@Test
	public void shuffledPhysicalBankOrderCannotChangeTheAutomaticLayout()
	{
		List<BankPreviewItem> input = items(RANARR);
		input.addAll(items(IRIT));
		input.addAll(items(2483, 249, 5302, 199, 2481, 5291, 2485, 91, 227));
		List<Integer> expected = ids(HerbloreItemSorter.layout(input));
		for (int seed = 0; seed < 20; seed++)
		{
			Collections.shuffle(input, new Random(seed));
			List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
			assertEquals("Shuffle " + seed, expected, ids(actual));
			assertConserved(input, actual);
		}
	}

	@Test
	public void canonicalHerbAndDoseIdsMakeACompleteRowWithRenamedDisplayNames()
	{
		List<BankPreviewItem> input = Arrays.asList(
			renamed(149, "Renamed dose one"), renamed(101, "Renamed unfinished input"),
			renamed(209, "Renamed grimy input"), renamed(147, "Renamed dose two"),
			item(5297), renamed(259, "Renamed clean input"),
			renamed(145, "Renamed dose three"), renamed(221, "Renamed secondary"));

		assertEquals(asList(IRIT), ids(HerbloreItemSorter.layout(input)));
	}

	@Test
	public void incompleteRecipesUseHerbProgressionWithinEachStage()
	{
		List<BankPreviewItem> input = items(101, 2481, 207, 5302, 249, 91, 5295, 209,
			2483, 257, 199, 5291, 99, 259, 2485, 5297);

		assertEquals(Arrays.asList(199, 207, 209, 2485, 249, 257, 259, 2481,
			5291, 5295, 5297, 5302, 91, 99, 101, 2483),
			ids(HerbloreItemSorter.layout(input)));
	}

	@Test
	public void orphanPotionFamiliesStayTogetherWithDescendingDoses()
	{
		List<BankPreviewItem> input = items(2456, 5956, 2458, 5954, 2454);

		assertEquals(Arrays.asList(5954, 5956, 2454, 2456, 2458),
			ids(HerbloreItemSorter.layout(input)));
	}

	@Test
	public void limpwurtSeedsNeverFillTheLimpwurtRootCell()
	{
		List<BankPreviewItem> input = items(203, 253, 5293, 95, 5100, 115, 117, 119, 199);
		List<BankPreviewItem> incomplete = HerbloreItemSorter.layout(input);
		assertEquals(Integer.valueOf(199), ids(incomplete).get(0));
		assertEquals(1, Collections.frequency(ids(incomplete), 5100));
		assertConserved(input, incomplete);

		input.add(item(225));
		List<BankPreviewItem> complete = HerbloreItemSorter.layout(input);
		assertEquals(Arrays.asList(203, 253, 5293, 95, 225, 115, 117, 119),
			ids(complete.subList(0, 8)));
		assertTrue(ids(complete).indexOf(5100) >= 8);
		assertConserved(input, complete);
	}

	@Test
	public void rawUnicornHornCannotCompleteAnAntipoisonRecipe()
	{
		List<BankPreviewItem> input = items(201, 251, 5292, 93, 237, 175, 177, 179, 199);
		List<BankPreviewItem> incomplete = HerbloreItemSorter.layout(input);
		assertEquals(Integer.valueOf(199), ids(incomplete).get(0));
		assertConserved(input, incomplete);

		input.add(item(235));
		List<BankPreviewItem> complete = HerbloreItemSorter.layout(input);
		assertEquals(Arrays.asList(201, 251, 5292, 93, 235, 175, 177, 179),
			ids(complete.subList(0, 8)));
		assertTrue(ids(complete).indexOf(237) >= 8);
		assertConserved(input, complete);
	}

	@Test
	public void harralanderAndLantadymeUseTheirReviewedStandardRecipes()
	{
		List<BankPreviewItem> input = items(2485, 2481, 5302, 2483, 3138, 3042, 3044, 3046);
		input.addAll(items(HARRALANDER));

		List<Integer> expected = asList(HARRALANDER);
		expected.addAll(Arrays.asList(2485, 2481, 5302, 2483, 3138, 3042, 3044, 3046));
		assertEquals(expected, ids(HerbloreItemSorter.layout(input)));
	}

	@Test
	public void superEnergyCannotMasqueradeAsTheMissingEnergyDose()
	{
		List<BankPreviewItem> input = items(205, 255, 5294, 97, 1975, 3018, 3012, 3014, 199);
		List<BankPreviewItem> incomplete = HerbloreItemSorter.layout(input);
		assertEquals(Integer.valueOf(199), ids(incomplete).get(0));
		assertConserved(input, incomplete);

		input.add(item(3010));
		List<BankPreviewItem> complete = HerbloreItemSorter.layout(input);
		assertEquals(asList(HARRALANDER), ids(complete.subList(0, 8)));
		assertTrue(ids(complete).indexOf(3018) >= 8);
		assertConserved(input, complete);
	}

	@Test
	public void everyReviewedPotionSecondaryHasTheSecondaryTagInTheRealCatalog()
	{
		for (int id : new int[]{221, 235, 225, 1975, 231, 6693, 2970, 223, 239, 3138, 245})
		{
			CatalogItem ingredient = catalog(id);
			assertEquals("Ingredient " + id, ItemCategory.HERBLORE, ingredient.getCategory());
			assertEquals("Ingredient " + id, "secondaries",
				BankTags.tagFor("herblore", ingredient.getSubcategory()).getKey());
		}
	}

	@Test
	public void consolidatedPhysicalCopiesCannotCreateANineCellRecipeRow()
	{
		List<BankPreviewItem> input = items(RANARR);
		input.set(0, new BankPreviewItem(catalog(207), 2, false, Arrays.asList(1, 1)));
		input.addAll(items(199, 249));

		List<BankPreviewItem> actual = HerbloreItemSorter.layout(input);
		assertEquals(Arrays.asList(199, 207, 249, 257), ids(actual.subList(0, 4)));
		assertEquals(2, actual.get(1).physicalBankSlotCount());
		assertEquals(2, actual.get(1).getQuantity());
		assertConserved(input, actual);
	}

	private static CatalogItem catalog(int id)
	{
		return CompositeItemCatalog.DEFAULT.findById(id).orElseThrow(
			() -> new AssertionError("Missing real item " + id));
	}

	private static BankPreviewItem item(int id)
	{
		return new BankPreviewItem(catalog(id), 1);
	}

	private static BankPreviewItem renamed(int id, String name)
	{
		CatalogItem original = catalog(id);
		return new BankPreviewItem(new CatalogItem(id, name, original.getCategory(),
			original.getSubcategory(), original.getTags(), null), 1);
	}

	private static List<BankPreviewItem> items(int... ids)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int id : ids) items.add(item(id));
		return items;
	}

	private static List<Integer> asList(int... ids)
	{
		return Arrays.stream(ids).boxed().collect(Collectors.toCollection(ArrayList::new));
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertConserved(List<BankPreviewItem> input, List<BankPreviewItem> actual)
	{
		assertEquals(input.size(), actual.size());
		for (BankPreviewItem item : input)
		{
			assertFalse("Invented empty bank cell", item.isBlank());
			assertEquals("Lost or duplicated physical stack " + item.getItemId(),
				1, Collections.frequency(actual, item));
			assertSame(item, actual.get(actual.indexOf(item)));
		}
	}
}

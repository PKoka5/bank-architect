package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import com.pkoka5.ironmanbankarchitect.organize.PresetItemSorter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class ChargedJewellerySlice1Test
{
	private static final Map<String, int[]> ORDERED_FAMILIES = orderedFamilies();
	private static final Map<String, int[]> ORDER_VALUES = orderValues();

	@Test
	public void everyFamilyHasCompleteExactMembershipAndChargeFacts()
	{
		for (Map.Entry<String, int[]> family : ORDERED_FAMILIES.entrySet())
		{
			Set<Integer> expectedIds = Arrays.stream(family.getValue()).boxed()
				.collect(Collectors.toCollection(LinkedHashSet::new));
			Set<Integer> actualIds = ResourceItemSortMetadataCatalog.INSTANCE.entries().stream()
				.filter(metadata -> family.getKey().equals(metadata.getFamilyKey()))
				.map(ItemSortMetadata::getItemId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals(family.getKey(), expectedIds, actualIds);

			int[] values = ORDER_VALUES.get(family.getKey());
			assertEquals(family.getKey(), family.getValue().length, values.length);
			for (int index = 0; index < family.getValue().length; index++)
			{
				ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE
					.findById(family.getValue()[index]).get();
				assertEquals(ItemSortMetadata.VariantKind.CHARGE, metadata.getVariantKind());
				assertEquals(values[index], metadata.getVariantValue());
				assertEquals("runelite-gameval-1.12.32", metadata.getSourceKey());
			}
		}
	}

	@Test
	public void everyStateHasExplicitTeleportRoutingAndIronmanDestination()
	{
		for (int itemId : allShippedIds())
		{
			ItemClassificationRefiner.Classification override =
				CanonicalItemClassificationOverrides.find(itemId).get();
			assertEquals(ItemCategory.TELEPORT, override.getCategory());
			assertEquals("teleport", override.getSubcategory());

			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId).get();
			assertEquals(item.getDisplayName(), ItemCategory.TELEPORT, item.getCategory());
			assertEquals(item.getDisplayName(), "currency-utilities",
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	@Test
	public void chargeRunsAreDeterministicAndFollowTheReviewedOrder()
	{
		for (Map.Entry<String, int[]> family : ORDERED_FAMILIES.entrySet())
		{
			List<BankPreviewItem> reversed = new ArrayList<>();
			for (int itemId : family.getValue())
			{
				reversed.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(itemId).get(), 1));
			}
			Collections.reverse(reversed);

			List<Integer> actual = PresetItemSorter.sort(
				BankPresets.IRONMAN.getCategory("currency-utilities"), reversed).stream()
				.map(BankPreviewItem::getItemId)
				.collect(Collectors.toList());
			List<Integer> expected = Arrays.stream(family.getValue()).boxed()
				.collect(Collectors.toList());
			assertEquals(family.getKey(), expected, actual);
		}
	}

	@Test
	public void excludedCopiesAndUnknownControlsCannotJoinAChargeFamily()
	{
		int[] excludedIds = {
			2553, 2555, 2557, 2559, 2561, 2563, 2565, 2567, 16358, 21455,
			3854, 3856, 3858, 3860, 3862, 3864, 3866, 3868, 16362, 21460,
			21167, 21168, 21170, 21172, 21174, 21176, 21463,
			21147, 21148, 21150, 21152, 21154, 21156, 21462,
			11106, 11108, 11110, 11112, 11114, 11969, 11971, 16264, 21449, 21450,
			1705, 1707, 1709, 1711, 1713, 10355, 10357, 10359, 10361, 10363,
			10719, 11965, 11967, 11977, 11979, 16091, 16346, 19708, 19709,
			20586, 21443, 21444, 21453, 21454, 8283
		};
		for (int itemId : excludedIds)
		{
			assertFalse("excluded ID received charge metadata: " + itemId,
				ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId).isPresent());
		}

		for (int itemId : new int[] {0, -1, 2_000_000_000})
		{
			assertFalse(ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId).isPresent());
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			assertEquals("storage-cleanup",
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	private static List<Integer> allShippedIds()
	{
		List<Integer> result = new ArrayList<>();
		for (int[] itemIds : ORDERED_FAMILIES.values())
		{
			Arrays.stream(itemIds).forEach(result::add);
		}
		assertEquals(48, result.size());
		assertEquals(48, new LinkedHashSet<>(result).size());
		return result;
	}

	private static Map<String, int[]> orderedFamilies()
	{
		Map<String, int[]> families = new LinkedHashMap<>();
		families.put("jewellery.ring_of_dueling",
			new int[] {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566});
		families.put("jewellery.games_necklace",
			new int[] {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867});
		families.put("jewellery.burning_amulet",
			new int[] {21166, 21169, 21171, 21173, 21175});
		families.put("jewellery.necklace_of_passage",
			new int[] {21146, 21149, 21151, 21153, 21155});
		families.put("jewellery.skills_necklace",
			new int[] {11968, 11970, 11105, 11107, 11109, 11111, 11113});
		families.put("jewellery.amulet_of_glory",
			new int[] {19707, 11964, 11978, 11966, 11976, 10354, 1712, 10356,
				1710, 10358, 1708, 10360, 1706, 1704, 10362});
		return Collections.unmodifiableMap(families);
	}

	private static Map<String, int[]> orderValues()
	{
		Map<String, int[]> values = new LinkedHashMap<>();
		values.put("jewellery.ring_of_dueling", new int[] {8, 7, 6, 5, 4, 3, 2, 1});
		values.put("jewellery.games_necklace", new int[] {8, 7, 6, 5, 4, 3, 2, 1});
		values.put("jewellery.burning_amulet", new int[] {5, 4, 3, 2, 1});
		values.put("jewellery.necklace_of_passage", new int[] {5, 4, 3, 2, 1});
		values.put("jewellery.skills_necklace", new int[] {6, 5, 4, 3, 2, 1, 0});
		values.put("jewellery.amulet_of_glory",
			new int[] {Integer.MAX_VALUE, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0});
		return Collections.unmodifiableMap(values);
	}
}

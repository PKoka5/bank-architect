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

public class ChargedJewellerySlice2Test
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
	public void chargeRunsAndCombinedRunAreDeterministic()
	{
		for (Map.Entry<String, int[]> family : ORDERED_FAMILIES.entrySet())
		{
			assertSortOrder(family.getKey(), family.getValue());
		}

		int[] expected = allShippedIds().stream().mapToInt(Integer::intValue).toArray();
		assertSortOrder("combined slice-2 run", expected);
	}

	@Test
	public void excludedCopiesAndUnknownControlsCannotJoinAChargeFamily()
	{
		int[] excludedIds = {
			11119, 11121, 11123, 11125, 11127, 11973, 11975, 16266, 21451, 21452,
			11195, 18818, 18819, 21468, 22709,
			2573, 11981, 11983, 11985, 11987, 11989, 12783, 15089, 16361,
			21456, 21457, 21458, 21459,
			14008, 21269, 21441
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

	private static void assertSortOrder(String message, int[] expectedIds)
	{
		List<BankPreviewItem> reversed = Arrays.stream(expectedIds)
			.mapToObj(itemId -> new BankPreviewItem(CompositeItemCatalog.DEFAULT.findById(itemId).get(), 1))
			.collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(reversed);

		List<Integer> actual = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("currency-utilities"), reversed).stream()
			.map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
		List<Integer> expected = Arrays.stream(expectedIds).boxed().collect(Collectors.toList());
		assertEquals(message, expected, actual);
	}

	private static List<Integer> allShippedIds()
	{
		List<Integer> result = new ArrayList<>();
		for (int[] itemIds : ORDERED_FAMILIES.values())
		{
			Arrays.stream(itemIds).forEach(result::add);
		}
		assertEquals(33, result.size());
		assertEquals(33, new LinkedHashSet<>(result).size());
		return result;
	}

	private static Map<String, int[]> orderedFamilies()
	{
		Map<String, int[]> families = new LinkedHashMap<>();
		families.put("jewellery.combat_bracelet",
			new int[] {11972, 11974, 11118, 11120, 11122, 11124, 11126});
		families.put("jewellery.digsite_pendant",
			new int[] {11194, 11193, 11192, 11191, 11190});
		families.put("jewellery.ring_of_wealth.imbued",
			new int[] {20786, 20787, 20788, 20789, 20790, 12785});
		families.put("jewellery.ring_of_wealth.standard",
			new int[] {11980, 11982, 11984, 11986, 11988, 2572});
		families.put("jewellery.slayer_ring",
			new int[] {21268, 11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873});
		return Collections.unmodifiableMap(families);
	}

	private static Map<String, int[]> orderValues()
	{
		Map<String, int[]> values = new LinkedHashMap<>();
		values.put("jewellery.combat_bracelet", new int[] {6, 5, 4, 3, 2, 1, 0});
		values.put("jewellery.digsite_pendant", new int[] {5, 4, 3, 2, 1});
		values.put("jewellery.ring_of_wealth.imbued", new int[] {5, 4, 3, 2, 1, 0});
		values.put("jewellery.ring_of_wealth.standard", new int[] {5, 4, 3, 2, 1, 0});
		values.put("jewellery.slayer_ring",
			new int[] {Integer.MAX_VALUE, 8, 7, 6, 5, 4, 3, 2, 1});
		return Collections.unmodifiableMap(values);
	}
}

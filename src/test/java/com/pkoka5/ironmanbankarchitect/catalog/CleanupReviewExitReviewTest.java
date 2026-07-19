package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import org.junit.Test;

public class CleanupReviewExitReviewTest
{
	@Test
	public void routesCompleteExitReviewFamiliesToFunctionalIronmanTabs()
	{
		assertFamily(ItemCategory.POTION, "potions-food", 2015, 2017, 2019, 2021);

		assertFamily(ItemCategory.POTION, "potions-food",
			2028, 2030, 2032, 2034, 2036, 2038, 2040,
			2048, 2054, 2064, 2074, 2080, 2084, 2092); // Gnome cocktails
		assertFamily(ItemCategory.SKILLING, "resources",
			2042, 2044, 2046, 2050, 2052, 2056, 2058, 2060, 2062,
			2066, 2068, 2070, 2072, 2076, 2078, 2082, 2086, 2088,
			2090); // Unfinished cocktails

		assertFamily(ItemCategory.POTION, "potions-food",
			2185, 2187, 2191, 2195, 2229, 2231, 2233, 2235); // Gnome bowls
		assertFamily(ItemCategory.SKILLING, "resources",
			2177, 2178, 2179, 2181, 2183, 2189, 2193); // Unfinished bowls

		assertFamily(ItemCategory.POTION, "potions-food",
			2205, 2209, 2213, 2217, 2237, 2239, 2241, 2243); // Crunchies
		assertFamily(ItemCategory.SKILLING, "resources",
			2201, 2202, 2203, 2207, 2211, 2215); // Unfinished crunchies

		assertFamily(ItemCategory.POTION, "potions-food",
			2219, 2221, 2223, 2225, 2227, 2253, 2255, 2259, 2277,
			2281); // Battas
		assertFamily(ItemCategory.SKILLING, "resources",
			2249, 2250, 2251, 2257, 2261, 2263, 2265, 2267, 2269,
			2271, 2273, 2275, 2279); // Unfinished battas

		assertFamily(ItemCategory.POTION, "potions-food",
			7740, 7744, 7746, 7750, 7752, 7754); // POH barrel drinks
		assertFamily(ItemCategory.TOOL, "skilling-tools", 7742); // Beer glass
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			5030, 5032, 5034, 5036, 5038, 5040); // Keldagrim clothing
		assertFamily(ItemCategory.CLUE, "clues-cosmetics",
			10877, 10878, 10879, 10880, 10881, 10882); // Satchels
	}

	@Test
	public void spoiltExcludedAndUnknownControlsRemainInCleanup()
	{
		for (int itemId : new int[] {
			0, -1, 2_000_000_000,
			2094, 2096, 2098, 2100, // Spoilt cocktails
			2173, 2175, // Spoilt and burnt bowls
			2197, 2199, // Spoilt and burnt crunchies
			2245, 2247, // Spoilt and burnt battas
			9509, // CERT ALUFT cocktail
			18989, // PLACEHOLDER ALUFT crunchies
			25618 // Internal satchel dummy
		})
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			assertEquals(item.getDisplayName(), "storage-cleanup",
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}

	private static void assertFamily(ItemCategory expectedCategory, String expectedTab,
		int... itemIds)
	{
		for (int itemId : itemIds)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId).get();
			assertEquals(item.getDisplayName(), expectedCategory, item.getCategory());
			assertEquals(item.getDisplayName(), expectedTab,
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}
	}
}

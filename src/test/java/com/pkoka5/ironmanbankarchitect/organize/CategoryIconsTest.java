package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemRegistry;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class CategoryIconsTest
{
	@Test
	public void everyShippedDestinationHasAnIcon()
	{
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			assertTrue("no icon for " + category.getKey(),
				CategoryIcons.iconItemId(category.getKey()) > 0);
		}
	}

	@Test
	public void everyIconIsARealItemInTheBundledRegistry()
	{
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			int itemId = CategoryIcons.iconItemId(category.getKey());
			Optional<CatalogItem> item = ResourceItemRegistry.INSTANCE.findById(itemId);
			assertTrue("icon " + itemId + " for " + category.getKey()
				+ " is not a known item", item.isPresent());
		}
	}

	@Test
	public void noTwoDestinationsShareAnIcon()
	{
		Set<Integer> seen = new HashSet<>();
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			assertTrue("duplicate icon for " + category.getKey(),
				seen.add(CategoryIcons.iconItemId(category.getKey())));
		}
		assertEquals(BankPresets.IRONMAN.getCategories().size(), seen.size());
	}

	@Test
	public void anUnknownCategoryHasNoIconSoTheCallerCanFallBack()
	{
		assertEquals(-1, CategoryIcons.iconItemId("not-a-category"));
		assertEquals(-1, CategoryIcons.iconItemId(null));
		assertEquals(-1, CategoryIcons.iconItemId(""));
	}
}

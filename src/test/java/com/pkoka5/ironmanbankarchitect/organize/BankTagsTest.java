package com.pkoka5.ironmanbankarchitect.organize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankTagsTest
{
	@Test
	public void everyCategoryOfThePresetSplitsIntoAtLeastOneTag()
	{
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			List<BankTag> tags = BankTags.forCategory(category.getKey());
			assertTrue(category.getKey() + " has no tags", !tags.isEmpty());
			for (BankTag tag : tags)
			{
				assertEquals(category.getKey(), tag.getCategoryKey());
			}
		}
	}

	@Test
	public void tagKeysAreUnique()
	{
		Set<String> keys = new HashSet<>();
		for (BankTag tag : BankTags.all())
		{
			assertTrue("duplicate tag key: " + tag.getKey(), keys.add(tag.getKey()));
		}

		assertEquals(keys.size(), BankTags.all().size());
	}

	/** The reported case: potions and food were one bundle and must now separate. */
	@Test
	public void potionsAndFoodAreSeparateTags()
	{
		BankTag potions = BankTags.tagFor("potions-food", "potion");
		BankTag food = BankTags.tagFor("potions-food", "food");

		assertEquals("potions", potions.getKey());
		assertEquals("food", food.getKey());
	}

	@Test
	public void theMainSectionBundleSplitsIntoRunesTeleportsAndCurrency()
	{
		assertEquals("runes", BankTags.tagFor("currency-utilities", "rune").getKey());
		assertEquals("teleports", BankTags.tagFor("currency-utilities", "teleport").getKey());
		assertEquals("currency", BankTags.tagFor("currency-utilities", "currency").getKey());
	}

	/**
	 * The refined teleport subcategories must keep reaching the Teleports tag.
	 * When tablet and scroll were introduced without being listed here, every
	 * city tablet fell through to the Frequently Used catch-all and a plan
	 * sent them wherever that tag lived - usually the front of the main tab.
	 */
	@Test
	public void refinedTeleportSubcategoriesStayOnTheTeleportsTag()
	{
		assertEquals("teleports", BankTags.tagFor("currency-utilities", "teleport-tablet").getKey());
		assertEquals("teleports", BankTags.tagFor("currency-utilities", "teleport-scroll").getKey());
		assertEquals("teleports", BankTags.tagFor("currency-utilities", "teleport-charge").getKey());
		assertEquals("teleports", BankTags.tagFor("currency-utilities", "transport-access").getKey());
	}

	@Test
	public void theHerbloreChainSplitsIntoItsWorkflowSteps()
	{
		assertEquals("grimy-herbs", BankTags.tagFor("herblore", "grimy-herb").getKey());
		assertEquals("clean-herbs", BankTags.tagFor("herblore", "clean-herb").getKey());
		assertEquals("secondaries", BankTags.tagFor("herblore", "secondary").getKey());
		assertEquals("unfinished-potions", BankTags.tagFor("herblore", "unfinished-potion").getKey());
		assertEquals("potion-doses", BankTags.tagFor("herblore", "potion-dose-2").getKey());
	}

	@Test
	public void anUnmatchedSubcategoryFallsToItsCategoryCatchAllRatherThanVanishing()
	{
		assertEquals("raw-resources", BankTags.tagFor("resources", "not-a-subcategory").getKey());
		assertEquals("gear", BankTags.tagFor("combat-gear", "head").getKey());
		assertEquals("cleanup", BankTags.tagFor("storage-cleanup", "junk").getKey());
		assertEquals("frequently-used", BankTags.tagFor("currency-utilities", "").getKey());
		assertEquals("clues", BankTags.tagFor("clues-cosmetics", null).getKey());
	}

	/**
	 * A stored plan names both, and a key that could be either would be read as
	 * one tag where the player meant a whole bundle.
	 */
	@Test
	public void noTagKeyCollidesWithACategoryKey()
	{
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			assertFalse("tag key collides with category " + category.getKey(),
				BankTags.isKnown(category.getKey()));
		}
	}

	@Test
	public void subcategoryMatchingIgnoresCaseAndSurroundingSpace()
	{
		assertEquals("food", BankTags.tagFor("potions-food", "  FOOD ").getKey());
	}

	@Test
	public void everyTagIsReachableByItsOwnKey()
	{
		for (BankTag tag : BankTags.all())
		{
			assertTrue(BankTags.isKnown(tag.getKey()));
			assertEquals(tag, BankTags.byKey(tag.getKey()));
		}
	}
}

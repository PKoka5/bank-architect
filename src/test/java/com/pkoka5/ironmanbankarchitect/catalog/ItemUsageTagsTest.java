package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemUsageTagsTest
{
	@Test public void functionalFamiliesHaveExactDestinationsWithoutPromotingQuestNamesakes()
	{
		for (int id : new int[]{3095, 3101, 6587, 1017, 4089, 4099, 4109, 23047, 1724, 1167,
			2961, 24697, 33709, 33711, 33713})
			assertEquals("Functional equipment " + id, ItemCategory.GEAR,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getCategory());
		for (int id : new int[]{4773, 4778, 4783, 4788, 4793, 4798, 4803})
			assertEquals("ammo", CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getSubcategory());
		for (int id : new int[]{4599, 4600, 6408, 6410, 6412, 6414, 6416, 6418, 6420})
		{
			assertEquals(ItemCategory.TOOL, CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getCategory());
			assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(id).hasTag("thieving-utility"));
		}
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(4683).hasTag("prayer-gear"));
		assertEquals(ItemCategory.CLEANUP, CompositeItemCatalog.DEFAULT.describeOrUnknown(20405).getCategory());
	}
	@Test public void clueFactsDoNotSpreadToNotesNamesakesOrPoisonedWeapons()
	{
		for (int id : new int[]{579, 1061, 1167, 1205, 4310, 5525})
			assertTrue("Missing canonical clue role " + id,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).hasTag("clue-required"));
		for (int id : new int[]{580, 6893, 1206, 1221, 7394, 4304})
			assertFalse("Unreviewed clue variant " + id,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).hasTag("clue-required"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(24695).hasTag("bloom-utility"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(33713).hasTag("bloom-utility"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(24587).hasTag("exchange-for-rune-pouch"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(24587).hasTag("rune-container"));
	}

	@Test public void utilityFactsApplyOnlyToReviewedCanonicalStates()
	{
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(4252).hasTag("refill-required"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(4251).hasTag("refill-required"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(2963).hasTag("bloom-utility"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(2961).hasTag("bloom-utility"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(5315).hasTag("storage-option-seed-vault"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(23650).hasTag("rune-container"));
	}

	@Test public void skillingFactsDoNotInventExperienceBonusesForWarmGloves()
	{
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(20712).hasTag("warm-clothing"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(20712).hasTag("skilling-outfit"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(5553).hasTag("thieving-utility"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(25434).hasTag("prayer-training"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(30045).hasTag("weight-reducing"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(20705).hasTag("skilling-outfit"));
		for (int id : new int[]{29263, 29265, 29267, 29269})
			assertEquals("skilling-outfit", CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getSubcategory());
	}

	@Test public void exactFactsRespectFunctionalExceptions()
	{
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(4300).hasTag("clue-required"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(4304).hasTag("clue-required"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(19689).hasTag("warm-clothing"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(19687).hasTag("warm-clothing"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(11061).hasTag("special-attack"));
		assertFalse(CompositeItemCatalog.DEFAULT.describeOrUnknown(9091).hasTag("transport-access"));
		assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(259).hasTag("herb"));
	}

	@Test public void malformedFactsCannotSilentlyChangeTheCatalog()
	{
		String[] invalid = {"# schema=2\n1\tgear", "# schema=1\n", "# schema=1\n0\tgear",
			"# schema=1\n1\tgear\n1\tquest-use", "# schema=1\n1\tgear,gear",
			"# schema=1\n1\tgear,", "# schema=1\n1\tbad tag", "# schema=1\n1\tgear\textra"};
		for (String text : invalid)
		{
			try { ItemUsageTags.load(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))); fail(text); }
			catch (IllegalStateException expected) { }
		}
		try { ItemUsageTags.load(null); fail("missing resource"); }
		catch (IllegalStateException expected) { }
	}

	@Test(expected = UnsupportedOperationException.class)
	public void factsAreImmutable()
	{
		ItemUsageTags.forItem(772).add("cosmetic");
	}
}

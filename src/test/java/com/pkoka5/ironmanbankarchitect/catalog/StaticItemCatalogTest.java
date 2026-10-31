package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.match.HardcodedSlotItemMappings;
import com.pkoka5.ironmanbankarchitect.match.SlotItemMapping;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class StaticItemCatalogTest
{
	@Test public void seedResourcePreservesEveryPublishedFieldAndWorkflow() throws Exception
	{
		List<Integer> ids = new java.util.ArrayList<>(VERIFIED_PHASE_B_ITEM_IDS);
		ids.add(34024); ids.add(34401);
		StringBuilder rows = new StringBuilder();
		for (int id : ids)
		{
			CatalogItem item = StaticItemCatalog.INSTANCE.findById(id).get();
			rows.append(id).append('\t').append(item.getDisplayName()).append('\t').append(item.getCategory())
				.append('\t').append(item.getSubcategory()).append('\t').append(String.join(",",item.getTags()))
				.append('\t').append(item.getWorkflowKey().orElse("")).append('\n');
		}
		StringBuilder hash = new StringBuilder();
		for (byte b : java.security.MessageDigest.getInstance("SHA-256").digest(rows.toString()
			.getBytes(java.nio.charset.StandardCharsets.UTF_8))) hash.append(String.format("%02x",b & 255));
		// Independently extracted from StaticItemCatalog at published b3583ca.
		assertEquals("cc7af5d89766f79ee94f89a3b2cd17332510decb164c5d3457d4ac20b03e1b8f",hash.toString());
	}

	@Test public void seedAndCompiledFormatsKeepTheirOwnFieldCountsAndRejectDamagedRecords()
	{
		String ordinary = "1\tTest\tGEAR\tbody\t";
		String workflow = ordinary + "\therblore.test";
		assertFalse(ResourceItemRegistry.loadItems(table(ordinary)).get(1).getWorkflowKey().isPresent());
		assertEquals("herblore.test",ResourceItemRegistry.loadItems(table(workflow),true).get(1).getWorkflowKey().get());
		org.junit.Assert.assertThrows(IllegalStateException.class, () -> ResourceItemRegistry.loadItems(table(workflow)));
		org.junit.Assert.assertThrows(IllegalStateException.class, () -> ResourceItemRegistry.loadItems(table(ordinary),true));
		for (String row : new String[]{"0\tTest\tGEAR\tbody\t\t", "1\tTest\tBAD\tbody\t\t",
			"1\tTest\tGEAR\tbody\tbad tag\t", "1\tTest\tGEAR\tbody\tclue,clue\t", workflow + "\n" + workflow})
			org.junit.Assert.assertThrows(RuntimeException.class, () -> ResourceItemRegistry.loadItems(table(row),true));
		org.junit.Assert.assertThrows(IllegalStateException.class, () -> ResourceItemRegistry.loadItems(null,true));
		org.junit.Assert.assertThrows(IllegalStateException.class, () -> ResourceItemRegistry.loadItems(table(""),true));
	}

	private static java.io.InputStream table(String rows)
	{
		return new java.io.ByteArrayInputStream(("# schema=1\n" + rows).getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private static final List<Integer> VERIFIED_PHASE_B_ITEM_IDS = Arrays.asList(
		5297, 209, 259, 101, 221, 145, 147, 149
	);

	private static final List<String> VERIFIED_PHASE_B_SLOT_KEYS = Arrays.asList(
		"herblore.irit.seed",
		"herblore.irit.grimy",
		"herblore.irit.clean",
		"herblore.irit.unf",
		"herblore.irit.secondary",
		"herblore.super-attack.3",
		"herblore.super-attack.2",
		"herblore.super-attack.1"
	);

	@Test
	public void catalogFindsAllVerifiedPhaseBItemIds()
	{
		for (int itemId : VERIFIED_PHASE_B_ITEM_IDS)
		{
			assertTrue("expected catalog entry for item " + itemId, StaticItemCatalog.INSTANCE.findById(itemId).isPresent());
		}
	}

	@Test
	public void unknownIdReturnsUnknownSafely()
	{
		int unmappedItemId = 999999;

		assertFalse(StaticItemCatalog.INSTANCE.findById(unmappedItemId).isPresent());

		CatalogItem described = StaticItemCatalog.INSTANCE.describeOrUnknown(unmappedItemId);
		assertEquals(unmappedItemId, described.getItemId());
		assertEquals(ItemCategory.UNKNOWN, described.getCategory());
		assertEquals("unknown", described.getSubcategory());
		assertTrue(described.getTags().isEmpty());
		assertFalse(described.getWorkflowKey().isPresent());
	}

	@Test
	public void describeOrUnknownHandlesZeroWithoutThrowing()
	{
		CatalogItem described = StaticItemCatalog.INSTANCE.describeOrUnknown(0);

		assertEquals(0, described.getItemId());
		assertEquals(ItemCategory.UNKNOWN, described.getCategory());
	}

	@Test
	public void describeOrUnknownHandlesNegativeIdWithoutThrowing()
	{
		CatalogItem described = StaticItemCatalog.INSTANCE.describeOrUnknown(-1);

		assertEquals(-1, described.getItemId());
		assertEquals(ItemCategory.UNKNOWN, described.getCategory());
	}

	@Test
	public void describeOrUnknownHandlesIntegerMinValueWithoutThrowing()
	{
		CatalogItem described = StaticItemCatalog.INSTANCE.describeOrUnknown(Integer.MIN_VALUE);

		assertEquals(Integer.MIN_VALUE, described.getItemId());
		assertEquals(ItemCategory.UNKNOWN, described.getCategory());
	}

	@Test
	public void knownItemConstructionStillRejectsNonPositiveItemId()
	{
		assertThrowsIllegalArgument(() -> new CatalogItem(0, "Test item", ItemCategory.HERBLORE, "test", Collections.emptySet(), null));
		assertThrowsIllegalArgument(() -> new CatalogItem(-1, "Test item", ItemCategory.HERBLORE, "test", Collections.emptySet(), null));
	}

	@Test
	public void iritWorkflowCategoriesAndTagsAreCorrect()
	{
		assertCategoryAndTags(5297, ItemCategory.FARMING, "irit", "herb-seed");
		assertCategoryAndTags(209, ItemCategory.HERBLORE, "irit", "grimy");
		assertCategoryAndTags(259, ItemCategory.HERBLORE, "irit", "clean");
		assertCategoryAndTags(101, ItemCategory.HERBLORE, "irit", "unfinished-potion");
		assertCategoryAndTags(221, ItemCategory.HERBLORE, "secondary", "super-attack");
		assertCategoryAndTags(145, ItemCategory.POTION, "super-attack", "dose-3");
		assertCategoryAndTags(147, ItemCategory.POTION, "super-attack", "dose-2");
		assertCategoryAndTags(149, ItemCategory.POTION, "super-attack", "dose-1");
	}

	@Test
	public void superAttackFourIsNotPartOfPhaseBIritCatalogWorkflow()
	{
		// Super attack (4), item ID 2436, is reserved for a future Potion / Consumables / PvM
		// Supplies catalog area and must not appear in this Phase B Irit prep catalog.
		assertFalse(StaticItemCatalog.INSTANCE.findById(2436).isPresent());
	}

	@Test
	public void hardcodedSlotItemMappingIdsArePresentInCatalog()
	{
		for (String slotKey : VERIFIED_PHASE_B_SLOT_KEYS)
		{
			Optional<SlotItemMapping> mapping = HardcodedSlotItemMappings.forSlotKey(slotKey);
			assertTrue("expected a hardcoded mapping for " + slotKey, mapping.isPresent());

			int itemId = mapping.get().getItemId();
			assertTrue("expected catalog entry for mapped item " + itemId + " (" + slotKey + ")",
				StaticItemCatalog.INSTANCE.containsId(itemId));
		}
	}

	@Test
	public void noDuplicateItemIdsInStaticCatalog()
	{
		Set<Integer> uniqueIds = new HashSet<>(VERIFIED_PHASE_B_ITEM_IDS);
		uniqueIds.add(34024);
		uniqueIds.add(34401);
		assertEquals(uniqueIds.size(), StaticItemCatalog.INSTANCE.size());
	}

	private static void assertThrowsIllegalArgument(Runnable action)
	{
		try
		{
			action.run();
			throw new AssertionError("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	private static void assertCategoryAndTags(int itemId, ItemCategory expectedCategory, String... expectedTags)
	{
		CatalogItem item = StaticItemCatalog.INSTANCE.findById(itemId)
			.orElseThrow(() -> new AssertionError("expected catalog entry for item " + itemId));

		assertEquals(expectedCategory, item.getCategory());
		for (String expectedTag : expectedTags)
		{
			assertTrue("expected item " + itemId + " to have tag " + expectedTag, item.hasTag(expectedTag));
		}
	}
}

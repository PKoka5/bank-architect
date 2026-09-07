package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderedItemFamiliesTest
{
	@Test
	public void bundledTablesRetainEveryOriginalFamilyAndMemberInOrder() throws Exception
	{
		// Fingerprints independently calculated from the former Java literals at 0046cd2.
		assertOriginal("potion", 4, 22, "30902cec1cf66b2e8807a265f6a367abd796203cabaf7e6da6e9154e4e1beb5f");
		assertOriginal("farming", 0, 11, "b0d9cebddc4c4c19c2bf1ee30179f20424f5c1d77662280a9d50227ee50a65a1");
		assertOriginal("gear", 0, 43, "45ca82269e019bda9b9f3d194485e5558ceb964de06354d73fcea5bd7fb52b82");
		assertOriginal("tool", 0, 32, "eb44f6ecbe6c95096902e9a373b9494f984dcd08f16e5c76a02615fe5ac72f19");
		assertOriginal("resource", 0, 40, "8a8d2e0b5c704af9e462c61d3ee6d746d6de004db25ef573e82467d6e7b93a57");
	}

	@Test
	public void extractedRunecraftingConstantsMatchRuneLiteAndBarrowsKeepsPartOrder()
	{
		OrderedItemFamilies tools = new OrderedItemFamilies(getClass().getResourceAsStream("tool-layout-families.tsv"), 0, false);
		List<Integer> priority = tools.ids("RUNECRAFTING_PRIORITY");
		assertEquals(List.of(
			net.runelite.api.gameval.ItemID.RCU_POUCH_SMALL,
			net.runelite.api.gameval.ItemID.RCU_POUCH_MEDIUM,
			net.runelite.api.gameval.ItemID.RCU_POUCH_MEDIUM_DEGRADE,
			net.runelite.api.gameval.ItemID.RCU_POUCH_LARGE,
			net.runelite.api.gameval.ItemID.RCU_POUCH_LARGE_DEGRADE,
			net.runelite.api.gameval.ItemID.RCU_POUCH_GIANT,
			net.runelite.api.gameval.ItemID.RCU_POUCH_GIANT_DEGRADE,
			net.runelite.api.gameval.ItemID.RCU_POUCH_COLOSSAL,
			net.runelite.api.gameval.ItemID.RCU_POUCH_COLOSSAL_DEGRADE,
			net.runelite.api.gameval.ItemID.MAGIC_EMERALD_NECKLACE), priority.subList(26, priority.size()));
		assertEquals(priority, tools.group("families").get("tool.runecrafting"));
		List<Integer> ahrim = new OrderedItemFamilies(getClass().getResourceAsStream("gear-layout-families.tsv"), 0)
			.ids("gear.barrows-ahrim");
		assertEquals(List.of(4708, 4856, 4857, 4858, 4859, 4860), ahrim.subList(0, 6));
		assertEquals(List.of(4710, 4862, 4863, 4864, 4865, 4866), ahrim.subList(6, 12));
		assertEquals(List.of(4712, 4868, 4869, 4870, 4871, 4872), ahrim.subList(12, 18));
		assertEquals(List.of(4714, 4874, 4875, 4876, 4877, 4878), ahrim.subList(18, 24));
	}

	@Test
	public void badResourcesFailOnUseWithoutPublishingPartialFamilies()
	{
		assertThrows(CatalogUnavailableException.class, () -> new OrderedItemFamilies(null, 0).entries());
		for (String data : List.of("# schema=10\na\t1", "# schema=1\n", "# schema=1\na\t0",
			"# schema=1\na\t1,1", "# schema=1\na\t1\nb\t1", "# schema=1\na\t1\na\t2",
			"# schema=1\na\tx", "# schema=1\na\t1\nbad row", "# schema=1\n\t1"))
		{
			OrderedItemFamilies table = table(data, 0);
			assertThrows(data, CatalogUnavailableException.class, table::entries);
			assertThrows(data, CatalogUnavailableException.class, table::entries);
		}
		assertThrows(CatalogUnavailableException.class, () -> table("# schema=1\na\t1,2,3", 4).entries());
	}

	@Test
	public void loadedFamiliesCannotBeMutated()
	{
		Map<String, List<Integer>> entries = table("# schema=1\na\t2,1", 0).entries();
		assertEquals(List.of(2, 1), entries.get("a"));
		assertThrows(UnsupportedOperationException.class, entries::clear);
		assertThrows(UnsupportedOperationException.class, () -> entries.get("a").clear());
	}

	@Test
	public void overlappingGroupsRetainOrderButRejectDuplicatesWithinOneRow()
	{
		OrderedItemFamilies table = new OrderedItemFamilies(new ByteArrayInputStream(
			"# schema=1\ngroup/second\t2,1\ngroup/first\t1,3\nother\t3".getBytes(StandardCharsets.UTF_8)), 0, false);
		assertEquals(List.of("second", "first"), List.copyOf(table.group("group").keySet()));
		assertEquals(List.of(2, 1), table.group("group").get("second"));
		assertThrows(UnsupportedOperationException.class, () -> table.group("group").clear());
		assertThrows(CatalogUnavailableException.class, () -> table.group("missing"));
		assertThrows(CatalogUnavailableException.class, () -> table.ids("missing"));
		OrderedItemFamilies duplicate = new OrderedItemFamilies(new ByteArrayInputStream(
			"# schema=1\na\t1,1".getBytes(StandardCharsets.UTF_8)), 0, false);
		assertThrows(CatalogUnavailableException.class, duplicate::entries);
	}

	private static OrderedItemFamilies table(String data, int width)
	{
		return new OrderedItemFamilies(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), width);
	}

	private static void assertOriginal(String name, int width, int rows, String expected) throws Exception
	{
		Map<String, List<Integer>> families = new OrderedItemFamilies(OrderedItemFamiliesTest.class
			.getResourceAsStream(name + "-layout-families.tsv"), width, !name.equals("tool") && !name.equals("resource")).entries();
		assertEquals(rows, families.size());
		StringBuilder canonical = new StringBuilder();
		families.forEach((key, ids) -> canonical.append(key).append('\t')
			.append(ids.stream().map(String::valueOf).collect(Collectors.joining(","))).append('\n'));
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
		StringBuilder hex = new StringBuilder();
		for (byte value : digest) hex.append(String.format("%02x", value & 255));
		assertEquals(expected, hex.toString());
	}
}

package com.pkoka5.ironmanbankarchitect.bank;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class BankSnapshotReaderTest
{
	@Test
	public void actualVisagePlaceholderIdsResolveToTheirOrdinaryComponentIds()
	{
		int[] raw = {ItemID.Placeholder.DRAGONFIRE_VISAGE, ItemID.Placeholder.WYVERN_VISAGE, ItemID.Placeholder.SKELETAL_VISAGE};
		int[] canonical = {ItemID.DRAGONFIRE_VISAGE, ItemID.WYVERN_VISAGE, ItemID.SKELETAL_VISAGE};
		for (int index = 0; index < raw.length; index++)
		{
			BankItemSnapshot item = BankSnapshotReader.snapshotItem(raw[index], 0, 14401, canonical[index], index)
				.orElseThrow(AssertionError::new);
			assertEquals(canonical[index], item.getItemId());
			assertEquals(0, item.getQuantity());
			assertTrue(item.isPlaceholder());
		}
	}

	@Test
	public void bankFillerIsSkipped()
	{
		assertFalse(BankSnapshotReader.isSnapshotItem(ItemID.BANK_FILLER, 1));
	}

	@Test
	public void invalidItemsAreSkipped()
	{
		assertFalse(BankSnapshotReader.isSnapshotItem(0, 1));
		assertFalse(BankSnapshotReader.isSnapshotItem(-1, 1));
		assertFalse(BankSnapshotReader.isSnapshotItem(209, 0));
		assertFalse(BankSnapshotReader.isSnapshotItem(209, -1));
	}

	@Test
	public void validBankItemsAreAccepted()
	{
		assertTrue(BankSnapshotReader.isSnapshotItem(209, 1));
	}

	@Test
	public void placeholderVariantIsCanonicalizedAndPreservedAtZeroQuantity()
	{
		BankItemSnapshot placeholder = BankSnapshotReader.snapshotItem(50000, 0, 14401, 6687, 27)
			.orElseThrow(() -> new AssertionError("expected placeholder"));

		assertEquals(6687, placeholder.getItemId());
		assertEquals(0, placeholder.getQuantity());
		assertEquals(27, placeholder.getSlotIndex());
		assertTrue(placeholder.isPlaceholder());
	}

	@Test
	public void ordinaryZeroQuantitySlotIsStillSkipped()
	{
		assertFalse(BankSnapshotReader.snapshotItem(6687, 0, -1, 50000, 1).isPresent());
	}
}

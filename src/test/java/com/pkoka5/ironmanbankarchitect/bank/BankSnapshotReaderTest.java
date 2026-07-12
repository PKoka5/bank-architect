package com.pkoka5.ironmanbankarchitect.bank;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class BankSnapshotReaderTest
{
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

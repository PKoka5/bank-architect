package com.pkoka5.ironmanbankarchitect.bank;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}

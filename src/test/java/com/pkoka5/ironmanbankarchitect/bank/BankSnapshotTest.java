package com.pkoka5.ironmanbankarchitect.bank;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class BankSnapshotTest
{
	@Test
	public void preservesItemIdQuantityAndSlotIndex()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(new BankItemSnapshot(209, 3, 12)));

		assertEquals(1, snapshot.getItems().size());
		assertEquals(209, snapshot.getItems().get(0).getItemId());
		assertEquals(3, snapshot.getItems().get(0).getQuantity());
		assertEquals(12, snapshot.getItems().get(0).getSlotIndex());
	}

	@Test
	public void skipsInvalidIdsAndQuantities()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(0, 1, 0),
			new BankItemSnapshot(-1, 1, 1),
			new BankItemSnapshot(209, 0, 2),
			new BankItemSnapshot(259, -4, 3),
			new BankItemSnapshot(221, 2, 4)
		));

		assertEquals(1, snapshot.getItems().size());
		assertEquals(221, snapshot.getItems().get(0).getItemId());
		assertEquals(2, snapshot.getTotalQuantity(221));
	}

	@Test
	public void duplicateItemIdsAggregateAndPreserveFirstSlot()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(209, 3, 7),
			new BankItemSnapshot(259, 1, 8),
			new BankItemSnapshot(209, 5, 20)
		));

		assertEquals(2, snapshot.getItems().size());
		assertEquals(8, snapshot.getTotalQuantity(209));
		assertEquals(7, snapshot.getItems().get(0).getSlotIndex());
	}

	@Test
	public void preservesPlaceholderWithoutTreatingItAsOwned()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(6687, 0, 14, true)
		));

		assertEquals(1, snapshot.getItems().size());
		assertEquals(0, snapshot.getTotalQuantity(6687));
		assertEquals(14, snapshot.getItems().get(0).getSlotIndex());
		assertEquals(true, snapshot.getItems().get(0).isPlaceholder());
	}

	@Test
	public void ownedStackWinsIfPlaceholderAndOwnedEntryShareAnItemId()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(6687, 0, 14, true),
			new BankItemSnapshot(6687, 3, 20)
		));

		assertEquals(3, snapshot.getTotalQuantity(6687));
		assertEquals(false, snapshot.getItems().get(0).isPlaceholder());
	}
}

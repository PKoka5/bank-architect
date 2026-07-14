package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import org.junit.Test;

public class LayoutEntryTest
{
	@Test
	public void ofBuildsEntryWithoutOptionalContext()
	{
		BankPreviewItem item = new BankPreviewItem(10, "Rope", 3);
		LayoutEntry entry = LayoutEntry.of(item, 42);

		assertSame(item, entry.getItem());
		assertEquals(42, entry.getSourceFlatBankSlot());
		assertFalse(entry.hasDenseCategoryRank());
		assertFalse(entry.hasLockedTarget());
	}

	@Test(expected = NullPointerException.class)
	public void ofRejectsNullItem()
	{
		LayoutEntry.of(null, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ofRejectsNegativeSourceFlatBankSlot()
	{
		LayoutEntry.of(new BankPreviewItem(10, "Rope", 1), -1);
	}

	@Test
	public void withDenseCategoryRankReturnsCopyAndPreservesLock()
	{
		LayoutEntry locked = LayoutEntry.of(new BankPreviewItem(10, "Rope", 1), 5).withLockedTarget(2);
		LayoutEntry ranked = locked.withDenseCategoryRank(7);

		assertFalse(locked.hasDenseCategoryRank());
		assertTrue(ranked.hasDenseCategoryRank());
		assertEquals(7, ranked.getDenseCategoryRank());
		assertTrue(ranked.hasLockedTarget());
		assertEquals(2, ranked.getLockedTarget());
	}

	@Test
	public void withLockedTargetReturnsCopyAndPreservesRank()
	{
		LayoutEntry ranked = LayoutEntry.of(new BankPreviewItem(10, "Rope", 1), 5).withDenseCategoryRank(1);
		LayoutEntry locked = ranked.withLockedTarget(0);

		assertFalse(ranked.hasLockedTarget());
		assertTrue(locked.hasLockedTarget());
		assertEquals(0, locked.getLockedTarget());
		assertTrue(locked.hasDenseCategoryRank());
		assertEquals(1, locked.getDenseCategoryRank());
	}

	@Test
	public void absentOptionalGettersThrow()
	{
		LayoutEntry entry = LayoutEntry.of(new BankPreviewItem(10, "Rope", 1), 5);

		try
		{
			entry.getDenseCategoryRank();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException expected)
		{
			// expected
		}

		try
		{
			entry.getLockedTarget();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException expected)
		{
			// expected
		}
	}

	@Test
	public void sourceFlatBankSlotIsNotADenseCategoryRank()
	{
		LayoutEntry entry = LayoutEntry.of(new BankPreviewItem(10, "Rope", 1), 3);

		assertEquals(3, entry.getSourceFlatBankSlot());
		assertFalse(entry.hasDenseCategoryRank());
	}
}

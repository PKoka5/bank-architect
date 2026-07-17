package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IronmanQuickToolSelectorTest
{
	@Test
	public void selectsHammerAndExactlyOneHighestOwnedToolPerFamily()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(2347, 1, 1),
			new BankItemSnapshot(1275, 1, 2),
			new BankItemSnapshot(11920, 1, 3),
			new BankItemSnapshot(1359, 1, 4),
			new BankItemSnapshot(10491, 1, 5),
			new BankItemSnapshot(6739, 3, 6),
			new BankItemSnapshot(23680, 0, 7, true)));

		assertEquals(new HashSet<>(Arrays.asList(2347, 11920, 6739)),
			IronmanQuickToolSelector.select(snapshot));
	}

	@Test
	public void chargedCrystalToolsWinWhenActuallyOwned()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(23680, 1, 1), new BankItemSnapshot(13243, 1, 2),
			new BankItemSnapshot(11920, 1, 3), new BankItemSnapshot(23673, 1, 4),
			new BankItemSnapshot(13241, 1, 5), new BankItemSnapshot(6739, 1, 6)));

		assertEquals(new HashSet<>(Arrays.asList(23680, 23673)),
			IronmanQuickToolSelector.select(snapshot));
	}

	@Test
	public void blessedAxeDoesNotDisplaceTheBestGeneralPurposeAxe()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(10491, 1, 1),
			new BankItemSnapshot(1359, 1, 2)));

		assertEquals(new HashSet<>(Arrays.asList(1359)),
			IronmanQuickToolSelector.select(snapshot));
	}
}

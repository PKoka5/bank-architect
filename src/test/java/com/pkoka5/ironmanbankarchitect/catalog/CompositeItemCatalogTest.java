package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.Arrays;
import org.junit.Test;

public class CompositeItemCatalogTest
{
	@Test
	public void curatedCatalogTakesPriorityOverGeneratedRegistry()
	{
		CatalogItem item = CompositeItemCatalog.DEFAULT.findById(5297)
			.orElseThrow(() -> new AssertionError("expected irit seed"));

		assertEquals("Irit seed", item.getDisplayName());
		assertEquals(ItemCategory.FARMING, item.getCategory());
	}

	@Test
	public void generatedRegistryRecognizesNonCuratedItems()
	{
		CatalogItem coins = CompositeItemCatalog.DEFAULT.findById(995)
			.orElseThrow(() -> new AssertionError("expected coins"));

		assertEquals("Coins", coins.getDisplayName());
		assertEquals(ItemCategory.CURRENCY, coins.getCategory());
	}

	@Test
	public void bankSummarySeparatesRecognizedFromUnrecognized()
	{
		BankCatalogSummary summary = BankCatalogSummarizer.summarize(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(995, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		)), CompositeItemCatalog.DEFAULT);

		assertEquals(2, summary.getKnownIdCount());
		assertEquals(1, summary.getUnknownIdCount());
		assertEquals(1, summary.countFor(ItemCategory.FARMING));
		assertEquals(1, summary.countFor(ItemCategory.CURRENCY));
		assertTrue(summary.toOverviewText().contains("Currency: 1"));
	}

	@Test
	public void generatedRegistryKeepsKnownButUnclassifiedIdsAsUncategorized()
	{
		CatalogItem toolkit = CompositeItemCatalog.DEFAULT.findById(1)
			.orElseThrow(() -> new AssertionError("expected toolkit"));

		assertEquals("Toolkit", toolkit.getDisplayName());
		assertEquals(ItemCategory.UNCATEGORIZED, toolkit.getCategory());
	}
}

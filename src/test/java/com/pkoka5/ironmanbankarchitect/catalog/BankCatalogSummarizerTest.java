package com.pkoka5.ironmanbankarchitect.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class BankCatalogSummarizerTest
{
	@Test
	public void allEightKnownPhaseBIdsProduceKnownCountEight()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(259, 1, 2),
			new BankItemSnapshot(101, 1, 3),
			new BankItemSnapshot(221, 1, 4),
			new BankItemSnapshot(145, 1, 5),
			new BankItemSnapshot(147, 1, 6),
			new BankItemSnapshot(149, 1, 7)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE);

		assertEquals(8, summary.getKnownIdCount());
		assertEquals(0, summary.getUnknownIdCount());
		assertEquals(8, summary.getTotalScannedIdCount());
	}

	@Test
	public void unknownPositiveIdsCountAsUnknown()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(999999, 1, 1),
			new BankItemSnapshot(888888, 1, 2)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE);

		assertEquals(1, summary.getKnownIdCount());
		assertEquals(2, summary.getUnknownIdCount());
		assertEquals(2, summary.countFor(ItemCategory.UNKNOWN));
	}

	@Test
	public void categoryCountsAreCorrectForIritWorkflow()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(259, 1, 2),
			new BankItemSnapshot(101, 1, 3),
			new BankItemSnapshot(221, 1, 4),
			new BankItemSnapshot(145, 1, 5),
			new BankItemSnapshot(147, 1, 6),
			new BankItemSnapshot(149, 1, 7)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE);

		assertEquals(1, summary.countFor(ItemCategory.FARMING));
		assertEquals(4, summary.countFor(ItemCategory.HERBLORE));
		assertEquals(3, summary.countFor(ItemCategory.POTION));
		assertEquals(0, summary.countFor(ItemCategory.UNKNOWN));
	}

	@Test
	public void emptySnapshotProducesZeroCounts()
	{
		BankCatalogSummary summary = BankCatalogSummarizer.summarize(new BankSnapshot(Collections.emptyList()), StaticItemCatalog.INSTANCE);

		assertEquals(0, summary.getKnownIdCount());
		assertEquals(0, summary.getUnknownIdCount());
		assertEquals(0, summary.getTotalScannedIdCount());
		assertEquals("Bank Scan Overview\nRecognized item IDs: 0\nUnrecognized IDs: 0", summary.toOverviewText());
	}

	@Test
	public void summarizerDoesNotThrowForUnusualIds()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(Integer.MAX_VALUE, 1, 0)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE);

		assertEquals(0, summary.getKnownIdCount());
		assertEquals(1, summary.getUnknownIdCount());
	}

	@Test
	public void summarizerKeepsReviewSamplesForUncategorizedItems()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1, 1, 4),
			new BankItemSnapshot(3, 1, 9)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, CompositeItemCatalog.DEFAULT);

		assertEquals(2, summary.countFor(ItemCategory.UNCATEGORIZED));
		assertEquals(2, summary.getReviewEntries().size());
		assertEquals("Toolkit (#1) slot 4", summary.getReviewEntries().get(0).toCompactText());
		assertTrue(summary.toOverviewText().contains("Rule review:"));
	}

	@Test
	public void toOverviewTextListsOnlyNonZeroCategories()
	{
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));

		BankCatalogSummary summary = BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE);
		String overview = summary.toOverviewText();

		assertEquals(
			"Bank Scan Overview\n"
				+ "Recognized item IDs: 2\n"
				+ "Unrecognized IDs: 1",
			overview
		);
	}
}

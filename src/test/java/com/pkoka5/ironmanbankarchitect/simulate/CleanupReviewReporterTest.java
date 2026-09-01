package com.pkoka5.ironmanbankarchitect.simulate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.simulate.CleanupReviewReporter.RegistryRecord;
import com.pkoka5.ironmanbankarchitect.simulate.CleanupReviewReporter.ReviewRow;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CleanupReviewReporterTest
{
	@Test
	public void mergesOccurrenceCountsAcrossSeeds()
	{
		Map<Integer, Integer> firstSeed = counts(10, 2, 20, 1);
		Map<Integer, Integer> secondSeed = counts(10, 3, 30, 4);
		Map<Integer, Integer> thirdSeed = counts(20, 5, 30, 1);

		Map<Integer, Integer> aggregate = CleanupReviewReporter.mergeSeedOccurrences(
			Arrays.asList(firstSeed, secondSeed, thirdSeed));

		assertEquals(3, aggregate.size());
		assertEquals(Integer.valueOf(5), aggregate.get(10));
		assertEquals(Integer.valueOf(6), aggregate.get(20));
		assertEquals(Integer.valueOf(5), aggregate.get(30));
	}

	@Test
	public void equalCountsSortByAscendingItemId()
	{
		Map<Integer, Integer> occurrences = counts(30, 4, 10, 4, 20, 5);
		Map<Integer, RegistryRecord> registry = new HashMap<>();
		registry.put(10, new RegistryRecord("Ten", "TEN"));
		registry.put(20, new RegistryRecord("Twenty", "TWENTY"));
		registry.put(30, new RegistryRecord("Thirty", "THIRTY"));

		List<ReviewRow> rows = CleanupReviewReporter.sortedRows(occurrences, registry);

		assertEquals(Arrays.asList(20, 10, 30), Arrays.asList(
			rows.get(0).getItemId(), rows.get(1).getItemId(), rows.get(2).getItemId()));
	}

	@Test
	public void emptyCleanupResultWritesOnlyTheHeader() throws IOException
	{
		Path output = Files.createTempFile("cleanup-review-empty", ".tsv");
		try
		{
			CleanupReviewReporter.write(output, Collections.emptyMap());
			List<String> lines = Files.readAllLines(output, StandardCharsets.UTF_8);
			assertEquals(Collections.singletonList(CleanupReviewReporter.HEADER), lines);
		}
		finally
		{
			Files.deleteIfExists(output);
		}
	}

	@Test
	public void countsOnlyItemsEffectivelyRoutedToIronmanCleanup()
	{
		Map<Integer, Integer> occurrences = new HashMap<>();
		CleanupReviewReporter.addCleanupOccurrences(occurrences,
			Arrays.asList(1, 2, 1)); // Toolkit is cleanup; steel cannonball is combat gear.

		assertEquals(Integer.valueOf(2), occurrences.get(1));
		assertFalse(occurrences.containsKey(2));
	}

	@Test
	public void registryFingerprintMatchesTheProposedProtocol() throws IOException
	{
		assertEquals(CleanupReviewBenchmarkProtocol.REGISTRY_SHA256,
			CleanupReviewReporter.registrySha256());
	}

	@Test
	public void registryFingerprintIgnoresTheCheckoutsLineEndings()
	{
		String unix = "1\tToolkit\n2\tSteel cannonball\n";
		String windows = "1\tToolkit\r\n2\tSteel cannonball\r\n";
		String classicMac = "1\tToolkit\r2\tSteel cannonball\r";

		String expected = CleanupReviewReporter.sha256OfNormalisedLines(
			unix.getBytes(StandardCharsets.UTF_8));

		assertEquals(expected, CleanupReviewReporter.sha256OfNormalisedLines(
			windows.getBytes(StandardCharsets.UTF_8)));
		assertEquals(expected, CleanupReviewReporter.sha256OfNormalisedLines(
			classicMac.getBytes(StandardCharsets.UTF_8)));
	}

	private static Map<Integer, Integer> counts(int... pairs)
	{
		assertTrue("pairs required", pairs.length % 2 == 0);
		Map<Integer, Integer> counts = new HashMap<>();
		for (int index = 0; index < pairs.length; index += 2)
		{
			counts.put(pairs[index], pairs[index + 1]);
		}
		return counts;
	}
}

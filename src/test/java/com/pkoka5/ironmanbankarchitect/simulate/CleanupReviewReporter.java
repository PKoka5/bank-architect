package com.pkoka5.ironmanbankarchitect.simulate;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Test-scope cleanup observation aggregation and deterministic TSV output. */
final class CleanupReviewReporter
{
	static final String REGISTRY_RESOURCE =
		"/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";
	static final String HEADER =
		"itemId\tcanonicalName\tsourceConstant\toccurrenceCount";

	private CleanupReviewReporter()
	{
	}

	static Map<Integer, Integer> countCleanupOccurrences(List<SimulationResult> results)
	{
		Map<Integer, Integer> occurrences = new HashMap<>();
		for (SimulationResult result : results)
		{
			addCleanupOccurrences(occurrences, result.getSampledItemIds());
		}
		return occurrences;
	}

	static void addCleanupOccurrences(Map<Integer, Integer> occurrences,
		Iterable<Integer> sampledItemIds)
	{
		for (int itemId : sampledItemIds)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			if ("storage-cleanup".equals(
				PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey()))
			{
				occurrences.merge(itemId, 1, Integer::sum);
			}
		}
	}

	static Map<Integer, Integer> mergeSeedOccurrences(
		Iterable<Map<Integer, Integer>> seedOccurrences)
	{
		Map<Integer, Integer> aggregate = new HashMap<>();
		for (Map<Integer, Integer> seed : seedOccurrences)
		{
			for (Map.Entry<Integer, Integer> entry : seed.entrySet())
			{
				aggregate.merge(entry.getKey(), entry.getValue(), Integer::sum);
			}
		}
		return aggregate;
	}

	static List<ReviewRow> sortedRows(Map<Integer, Integer> occurrences,
		Map<Integer, RegistryRecord> registry)
	{
		List<ReviewRow> rows = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : occurrences.entrySet())
		{
			RegistryRecord record = registry.get(entry.getKey());
			rows.add(new ReviewRow(entry.getKey(), record == null ? "" : record.name,
				record == null ? "" : record.constant, entry.getValue()));
		}
		rows.sort(Comparator.comparingInt(ReviewRow::getOccurrenceCount).reversed()
			.thenComparingInt(ReviewRow::getItemId));
		return rows;
	}

	static void write(Path output, Map<Integer, Integer> occurrences) throws IOException
	{
		Path parent = output.toAbsolutePath().getParent();
		if (parent != null)
		{
			Files.createDirectories(parent);
		}
		List<ReviewRow> rows = sortedRows(occurrences, loadRegistry());
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			writer.write(HEADER);
			writer.newLine();
			for (ReviewRow row : rows)
			{
				writer.write(row.itemId + "\t" + row.name + "\t" + row.constant
					+ "\t" + row.occurrenceCount);
				writer.newLine();
			}
		}
	}

	static Map<Integer, RegistryRecord> loadRegistry() throws IOException
	{
		InputStream stream = CleanupReviewReporter.class.getResourceAsStream(REGISTRY_RESOURCE);
		if (stream == null)
		{
			throw new IOException("item registry resource missing: " + REGISTRY_RESOURCE);
		}
		Map<Integer, RegistryRecord> records = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] columns = line.split("\\t", -1);
				if (columns.length != 4)
				{
					continue;
				}
				try
				{
					int itemId = Integer.parseInt(columns[0].replace("\uFEFF", ""));
					records.put(itemId, new RegistryRecord(columns[1], columns[3]));
				}
				catch (NumberFormatException ignored)
				{
					// Header or malformed record.
				}
			}
		}
		return records;
	}

	static String registrySha256() throws IOException
	{
		InputStream stream = CleanupReviewReporter.class.getResourceAsStream(REGISTRY_RESOURCE);
		if (stream == null)
		{
			throw new IOException("item registry resource missing: " + REGISTRY_RESOURCE);
		}
		byte[] bytes;
		try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream())
		{
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) != -1)
			{
				output.write(buffer, 0, read);
			}
			bytes = output.toByteArray();
		}
		try
		{
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte value : digest)
			{
				hex.append(String.format("%02x", value & 0xff));
			}
			return hex.toString();
		}
		catch (NoSuchAlgorithmException ex)
		{
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}

	static final class RegistryRecord
	{
		private final String name;
		private final String constant;

		RegistryRecord(String name, String constant)
		{
			this.name = name;
			this.constant = constant;
		}
	}

	static final class ReviewRow
	{
		private final int itemId;
		private final String name;
		private final String constant;
		private final int occurrenceCount;

		ReviewRow(int itemId, String name, String constant, int occurrenceCount)
		{
			this.itemId = itemId;
			this.name = name;
			this.constant = constant;
			this.occurrenceCount = occurrenceCount;
		}

		int getItemId()
		{
			return itemId;
		}

		int getOccurrenceCount()
		{
			return occurrenceCount;
		}

		String getName()
		{
			return name;
		}

		String getConstant()
		{
			return constant;
		}
	}
}

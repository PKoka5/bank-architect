package com.pkoka5.ironmanbankarchitect.simulate;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Outcome;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Scenario;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Developer-only CLI: simulates many random banks against the tab guidance
 * route and writes a TSV report. Run via {@code gradlew simulateRandomBanks}
 * (optionally {@code -PsimBanks=… -PsimSeed=… -PsimMinItems=… -PsimMaxItems=…}).
 * This class is intentionally in the test source set and never ships.
 */
public final class RandomBankSimulationRunner
{
	private static final String REGISTRY_RESOURCE =
		"/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";

	private RandomBankSimulationRunner()
	{
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 5)
		{
			throw new IllegalArgumentException(
				"Expected banks, seed, minItems, maxItems and output arguments");
		}
		int banks = Integer.parseInt(args[0]);
		long baseSeed = Long.parseLong(args[1]);
		int minItems = Integer.parseInt(args[2]);
		int maxItems = Integer.parseInt(args[3]);
		Path output = Paths.get(args[4]);
		if (banks < 1 || minItems < 1 || maxItems < minItems)
		{
			throw new IllegalArgumentException("Invalid banks or item range");
		}

		List<Integer> universe = RandomBankSimulator.loadItemUniverse();
		System.out.println("Item universe: " + universe.size() + " registry items");
		System.out.println("Simulating " + banks + " banks per scenario, "
			+ minItems + "-" + maxItems + " items, base seed " + baseSeed);

		Random sizes = new Random(baseSeed);
		List<SimulationResult> results = new ArrayList<>();
		for (int run = 0; run < banks; run++)
		{
			int itemCount = minItems + sizes.nextInt(maxItems - minItems + 1);
			for (Scenario scenario : Scenario.values())
			{
				results.add(RandomBankSimulator.simulate(
					baseSeed + run, scenario, itemCount, universe));
			}
		}

		writeReport(output, results);
		Path cleanupReview = output.toAbsolutePath().resolveSibling("cleanup-review.tsv");
		writeCleanupReview(cleanupReview, results);
		printSummary(results);
		System.out.println("Report: " + output.toAbsolutePath());
		System.out.println("Cleanup review: " + cleanupReview);

		boolean hardFailure = results.stream().anyMatch(result ->
			result.getOutcome() == Outcome.STALLED
				|| result.getOutcome() == Outcome.NON_TERMINATING
				|| result.getOutcome() == Outcome.ADVISOR_BLOCKED
				|| result.getOutcome() == Outcome.COMPLETED && !result.isFinalOrderVerified());
		if (hardFailure)
		{
			System.out.println("HARD FAILURES FOUND - inspect the report rows above.");
			System.exit(1);
		}
	}

	private static void writeCleanupReview(Path output, List<SimulationResult> results)
		throws IOException
	{
		Map<Integer, RegistryRecord> registry = loadRegistry();
		Map<Integer, Integer> occurrences = new HashMap<>();
		for (SimulationResult result : results)
		{
			for (int itemId : result.getSampledItemIds())
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

		List<Map.Entry<Integer, Integer>> rows = new ArrayList<>(occurrences.entrySet());
		rows.sort(Map.Entry.<Integer, Integer>comparingByValue(Comparator.reverseOrder())
			.thenComparing(Map.Entry.comparingByKey()));
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			writer.write("itemId\tcanonicalName\tsourceConstant\toccurrenceCount");
			writer.newLine();
			for (Map.Entry<Integer, Integer> row : rows)
			{
				RegistryRecord record = registry.get(row.getKey());
				writer.write(row.getKey() + "\t"
					+ (record == null ? "" : record.name) + "\t"
					+ (record == null ? "" : record.constant) + "\t"
					+ row.getValue());
				writer.newLine();
			}
		}
	}

	private static Map<Integer, RegistryRecord> loadRegistry() throws IOException
	{
		InputStream stream = RandomBankSimulationRunner.class.getResourceAsStream(REGISTRY_RESOURCE);
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

	private static final class RegistryRecord
	{
		private final String name;
		private final String constant;

		private RegistryRecord(String name, String constant)
		{
			this.name = name;
			this.constant = constant;
		}
	}

	private static void printSummary(List<SimulationResult> results)
	{
		Map<Outcome, Integer> byOutcome = new EnumMap<>(Outcome.class);
		int totalMoves = 0;
		int totalSwaps = 0;
		int completed = 0;
		for (SimulationResult result : results)
		{
			byOutcome.merge(result.getOutcome(), 1, Integer::sum);
			if (result.getOutcome() == Outcome.COMPLETED)
			{
				completed++;
				totalMoves += result.getTotalMoves();
				totalSwaps += result.getSwapMoves();
			}
		}

		System.out.println();
		System.out.println("Outcomes:");
		for (Map.Entry<Outcome, Integer> entry : byOutcome.entrySet())
		{
			System.out.println("  " + entry.getKey() + ": " + entry.getValue());
		}
		if (completed > 0)
		{
			System.out.println("Average moves per completed bank: "
				+ totalMoves / completed + " (of which swaps: " + totalSwaps / completed + ")");
		}

		for (SimulationResult result : results)
		{
			if (result.getOutcome() == Outcome.COMPLETED
				|| result.getOutcome() == Outcome.UNSUPPORTED_PLAN)
			{
				continue;
			}
			System.out.println("  FAILURE seed=" + result.getSeed()
				+ " scenario=" + result.getScenario()
				+ " items=" + result.getItemCount()
				+ " outcome=" + result.getOutcome()
				+ (result.getErrorMessage().isEmpty() ? "" : " error=" + result.getErrorMessage())
				+ " itemIds=" + result.getFailedItemIds());
		}
	}

	private static void writeReport(Path output, List<SimulationResult> results)
		throws IOException
	{
		Path parent = output.toAbsolutePath().getParent();
		if (parent != null)
		{
			Files.createDirectories(parent);
		}
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			writer.write("seed\tscenario\titemCount\tplanTabs\toutcome\tfinalStatus"
				+ "\ttotalMoves\tswaps\tminSwapsAtSortStart\tcollapses\tcreates"
				+ "\tdistributes\ttransfers\treturns\terror");
			writer.newLine();
			for (SimulationResult result : results)
			{
				writer.write(result.getSeed()
					+ "\t" + result.getScenario()
					+ "\t" + result.getItemCount()
					+ "\t" + result.getPlanTabs()
					+ "\t" + result.getOutcome()
					+ "\t" + (result.getFinalStatus() == null ? "" : result.getFinalStatus())
					+ "\t" + result.getTotalMoves()
					+ "\t" + result.getSwapMoves()
					+ "\t" + result.getMinimumSwapsAtSortStart()
					+ "\t" + result.getMoveCounts().getOrDefault(MoveType.COLLAPSE_TAB, 0)
					+ "\t" + result.getMoveCounts().getOrDefault(MoveType.DRAG_TO_NEW_TAB, 0)
					+ "\t" + result.getMoveCounts().getOrDefault(MoveType.DISTRIBUTE_TO_TAB, 0)
					+ "\t" + result.getMoveCounts().getOrDefault(MoveType.TRANSFER_TO_TAB, 0)
					+ "\t" + result.getMoveCounts().getOrDefault(MoveType.RETURN_TO_MAIN, 0)
					+ "\t" + result.getErrorMessage().replace('\t', ' '));
				writer.newLine();
			}
		}
	}
}

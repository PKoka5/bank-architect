package com.pkoka5.ironmanbankarchitect.simulate;

import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Outcome;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.Scenario;
import com.pkoka5.ironmanbankarchitect.simulate.RandomBankSimulator.SimulationResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
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
		CleanupReviewReporter.write(cleanupReview,
			CleanupReviewReporter.countCleanupOccurrences(results));
		printSummary(results);
		System.out.println("Report: " + output.toAbsolutePath());
		System.out.println("Cleanup review: " + cleanupReview);

		boolean hardFailure = results.stream().anyMatch(SimulationOutcomePolicy::isHardFailure);
		if (hardFailure)
		{
			System.out.println("HARD FAILURES FOUND - inspect the report rows above.");
			System.exit(1);
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

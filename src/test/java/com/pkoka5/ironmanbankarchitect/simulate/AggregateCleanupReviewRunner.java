package com.pkoka5.ironmanbankarchitect.simulate;

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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Developer-only CLI for the fixed multi-seed cleanup-review benchmark. */
public final class AggregateCleanupReviewRunner
{
	private AggregateCleanupReviewRunner()
	{
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Expected aggregate output directory");
		}
		Path outputDirectory = Paths.get(args[0]).toAbsolutePath();
		Files.createDirectories(outputDirectory);

		String registrySha256 = CleanupReviewReporter.registrySha256();
		if (!CleanupReviewBenchmarkProtocol.REGISTRY_SHA256.equals(registrySha256))
		{
			throw new IllegalStateException("Registry fingerprint changed: expected "
				+ CleanupReviewBenchmarkProtocol.REGISTRY_SHA256 + " but found "
				+ registrySha256 + ". Review and update the benchmark protocol.");
		}

		List<Integer> universe = RandomBankSimulator.loadItemUniverse();
		List<Map<Integer, Integer>> seedOccurrences = new ArrayList<>();
		Map<Outcome, Integer> outcomes = new EnumMap<>(Outcome.class);
		boolean hardFailure = false;

		for (long protocolSeed : CleanupReviewBenchmarkProtocol.SEEDS)
		{
			List<SimulationResult> seedResults = simulateSeed(protocolSeed, universe);
			seedOccurrences.add(CleanupReviewReporter.countCleanupOccurrences(seedResults));
			for (SimulationResult result : seedResults)
			{
				outcomes.merge(result.getOutcome(), 1, Integer::sum);
				hardFailure |= isHardFailure(result);
			}
		}

		Map<Integer, Integer> aggregate =
			CleanupReviewReporter.mergeSeedOccurrences(seedOccurrences);
		Path cleanupReview = outputDirectory.resolve("cleanup-review.tsv");
		Path metadata = outputDirectory.resolve("metadata.tsv");
		CleanupReviewReporter.write(cleanupReview, aggregate);
		writeMetadata(metadata, registrySha256, aggregate, outcomes, universe.size());
		printSummary(aggregate, outcomes, cleanupReview, metadata);

		if (hardFailure)
		{
			System.out.println("HARD FAILURES FOUND - inspect outcome counts and rerun tests.");
			System.exit(1);
		}
	}

	private static List<SimulationResult> simulateSeed(long protocolSeed,
		List<Integer> universe)
	{
		Random sizes = new Random(protocolSeed);
		List<SimulationResult> results = new ArrayList<>();
		for (int run = 0; run < CleanupReviewBenchmarkProtocol.BANKS_PER_SEED; run++)
		{
			int itemCount = CleanupReviewBenchmarkProtocol.MIN_ITEMS + sizes.nextInt(
				CleanupReviewBenchmarkProtocol.MAX_ITEMS
					- CleanupReviewBenchmarkProtocol.MIN_ITEMS + 1);
			for (Scenario scenario : Scenario.values())
			{
				results.add(RandomBankSimulator.simulate(
					protocolSeed + run, scenario, itemCount, universe));
			}
		}
		return results;
	}

	private static boolean isHardFailure(SimulationResult result)
	{
		return result.getOutcome() == Outcome.STALLED
			|| result.getOutcome() == Outcome.NON_TERMINATING
			|| result.getOutcome() == Outcome.ADVISOR_BLOCKED
			|| result.getOutcome() == Outcome.COMPLETED && !result.isFinalOrderVerified();
	}

	private static void writeMetadata(Path output, String registrySha256,
		Map<Integer, Integer> aggregate, Map<Outcome, Integer> outcomes, int universeSize)
		throws IOException
	{
		int seedCount = CleanupReviewBenchmarkProtocol.SEEDS.length;
		int scenarioCount = Scenario.values().length;
		int generatedBanks = seedCount * CleanupReviewBenchmarkProtocol.BANKS_PER_SEED;
		int scenarioBanks = generatedBanks * scenarioCount;
		int aggregateOccurrences = aggregate.values().stream().mapToInt(Integer::intValue).sum();

		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			writeMetadataRow(writer, "protocolVersion", CleanupReviewBenchmarkProtocol.VERSION);
			writeMetadataRow(writer, "seedList", joinSeeds());
			writeMetadataRow(writer, "seedCount", seedCount);
			writeMetadataRow(writer, "seedDerivation", "protocolSeed+runIndex");
			writeMetadataRow(writer, "banksPerSeed",
				CleanupReviewBenchmarkProtocol.BANKS_PER_SEED);
			writeMetadataRow(writer, "minItems", CleanupReviewBenchmarkProtocol.MIN_ITEMS);
			writeMetadataRow(writer, "maxItems", CleanupReviewBenchmarkProtocol.MAX_ITEMS);
			writeMetadataRow(writer, "scenarioCount", scenarioCount);
			writeMetadataRow(writer, "generatedBanks", generatedBanks);
			writeMetadataRow(writer, "scenarioBanks", scenarioBanks);
			writeMetadataRow(writer, "occurrenceMeaning",
				"one sampled cleanup item per simulated scenario bank");
			writeMetadataRow(writer, "registryResource", CleanupReviewReporter.REGISTRY_RESOURCE);
			writeMetadataRow(writer, "registrySha256", registrySha256);
			writeMetadataRow(writer, "itemUniverseSize", universeSize);
			writeMetadataRow(writer, "aggregateDistinctItemIds", aggregate.size());
			writeMetadataRow(writer, "aggregateOccurrences", aggregateOccurrences);
			for (Outcome outcome : Outcome.values())
			{
				writeMetadataRow(writer, "outcome." + outcome,
					outcomes.getOrDefault(outcome, 0));
			}
		}
	}

	private static String joinSeeds()
	{
		StringBuilder value = new StringBuilder();
		for (long seed : CleanupReviewBenchmarkProtocol.SEEDS)
		{
			if (value.length() > 0)
			{
				value.append(',');
			}
			value.append(seed);
		}
		return value.toString();
	}

	private static void writeMetadataRow(BufferedWriter writer, String key, Object value)
		throws IOException
	{
		writer.write(key + "\t" + value);
		writer.newLine();
	}

	private static void printSummary(Map<Integer, Integer> aggregate,
		Map<Outcome, Integer> outcomes, Path cleanupReview, Path metadata)
	{
		int generatedBanks = CleanupReviewBenchmarkProtocol.SEEDS.length
			* CleanupReviewBenchmarkProtocol.BANKS_PER_SEED;
		int scenarioBanks = generatedBanks * Scenario.values().length;
		int occurrences = aggregate.values().stream().mapToInt(Integer::intValue).sum();
		System.out.println("Cleanup benchmark protocol " + CleanupReviewBenchmarkProtocol.VERSION);
		System.out.println("Seeds: " + Arrays.toString(CleanupReviewBenchmarkProtocol.SEEDS));
		System.out.println("Generated banks: " + generatedBanks
			+ "; scenario banks: " + scenarioBanks);
		System.out.println("Outcomes:");
		for (Outcome outcome : Outcome.values())
		{
			System.out.println("  " + outcome + ": " + outcomes.getOrDefault(outcome, 0));
		}
		System.out.println("Cleanup aggregate: " + aggregate.size()
			+ " distinct item IDs; " + occurrences + " occurrences");
		System.out.println("Cleanup review: " + cleanupReview);
		System.out.println("Metadata: " + metadata);
	}
}

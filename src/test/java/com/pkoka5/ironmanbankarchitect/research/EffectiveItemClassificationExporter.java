package com.pkoka5.ironmanbankarchitect.research;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Developer-only exporter for the effective catalog and Ironman preset route.
 * This class is intentionally in the test source set and never ships in the plugin jar.
 */
public final class EffectiveItemClassificationExporter
{
	private static final Pattern CACHE_ONLY_CONSTANT = Pattern.compile(
		"(^|_)(?:INTERFACE|PLACEHOLDER|DUMMY|NULL)(?:_|$)");
	private static final Pattern TRAILING_VARIANTS = Pattern.compile(
		"(?:\\s*\\((?:[0-9]+|[a-z]?\\+{0,2}|empty|uncharged|inactive|broken|bh|lms|cr|members)\\))+$",
		Pattern.CASE_INSENSITIVE);

	private EffectiveItemClassificationExporter()
	{
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			throw new IllegalArgumentException("Expected registry, output and excluded-output paths");
		}
		ExportStats stats = export(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2]));
		System.out.println("Effective records: " + stats.included);
		System.out.println("Excluded cache records: " + stats.excluded);
		System.out.println("Duplicate display-name groups: " + stats.duplicateNameGroups);
	}

	static ExportStats export(Path registryPath, Path outputPath, Path excludedOutputPath) throws IOException
	{
		List<RawRecord> included = new ArrayList<>();
		List<RawRecord> excluded = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(registryPath, StandardCharsets.UTF_8))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				RawRecord record = parse(line);
				if (record == null)
				{
					continue;
				}
				if (isCacheOnly(record))
				{
					excluded.add(record);
				}
				else
				{
					included.add(record);
				}
			}
		}

		included.sort(Comparator.comparingInt(record -> record.itemId));
		excluded.sort(Comparator.comparingInt(record -> record.itemId));
		Map<String, Integer> duplicateCounts = new LinkedHashMap<>();
		Map<String, Integer> familyCounts = new LinkedHashMap<>();
		for (RawRecord record : included)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(record.itemId);
			increment(duplicateCounts, normalize(item.getDisplayName()));
			increment(familyCounts, familyKey(item.getDisplayName()));
		}

		createParent(outputPath);
		try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))
		{
			writer.write("itemId\tname\titemCategory\tsubcategory\tironmanTabKey\tworkflowKey\ttags\tconstantName\tduplicateNameKey\tduplicateNameCount\tvariantFamilyKey\tvariantFamilyCount\tvariantFlags");
			writer.newLine();
			for (RawRecord record : included)
			{
				CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(record.itemId);
				String duplicateKey = normalize(item.getDisplayName());
				String familyKey = familyKey(item.getDisplayName());
				writer.write(Integer.toString(record.itemId));
				writeCell(writer, item.getDisplayName());
				writeCell(writer, item.getCategory().name());
				writeCell(writer, item.getSubcategory());
				writeCell(writer, PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
				writeCell(writer, item.getWorkflowKey().orElse(""));
				writeCell(writer, String.join(",", item.getTags()));
				writeCell(writer, record.constantName);
				writeCell(writer, duplicateKey);
				writeCell(writer, Integer.toString(duplicateCounts.get(duplicateKey)));
				writeCell(writer, familyKey);
				writeCell(writer, Integer.toString(familyCounts.get(familyKey)));
				writeCell(writer, String.join(",", variantFlags(item.getDisplayName(), record.constantName)));
				writer.newLine();
			}
		}

		createParent(excludedOutputPath);
		try (BufferedWriter writer = Files.newBufferedWriter(excludedOutputPath, StandardCharsets.UTF_8))
		{
			writer.write("itemId\tregistryName\tconstantName\treason");
			writer.newLine();
			for (RawRecord record : excluded)
			{
				writer.write(Integer.toString(record.itemId));
				writeCell(writer, record.registryName);
				writeCell(writer, record.constantName);
				writeCell(writer, "CACHE_ONLY_CONSTANT");
				writer.newLine();
			}
		}

		long duplicateGroups = duplicateCounts.values().stream().filter(count -> count > 1).count();
		return new ExportStats(included.size(), excluded.size(), duplicateGroups);
	}

	private static RawRecord parse(String line)
	{
		if (line.trim().isEmpty() || line.startsWith("#"))
		{
			return null;
		}
		String[] columns = line.split("\\t", -1);
		if (columns.length != 4)
		{
			throw new IllegalArgumentException("Expected four registry columns: " + line);
		}
		int itemId;
		try
		{
			itemId = Integer.parseInt(columns[0].replace("\uFEFF", ""));
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("Invalid item ID: " + columns[0], ex);
		}
		if (itemId <= 0 || isNullLike(columns[1]))
		{
			return null;
		}
		return new RawRecord(itemId, columns[1].trim(), columns[3].trim());
	}

	private static boolean isNullLike(String name)
	{
		String value = name == null ? "" : name.trim();
		return value.isEmpty() || "null".equalsIgnoreCase(value) || "null item".equalsIgnoreCase(value);
	}

	private static boolean isCacheOnly(RawRecord record)
	{
		return CACHE_ONLY_CONSTANT.matcher(record.constantName.toUpperCase(Locale.ROOT)).find();
	}

	private static String normalize(String name)
	{
		return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	private static String familyKey(String name)
	{
		String normalized = normalize(name);
		String withoutVariants = TRAILING_VARIANTS.matcher(normalized).replaceAll("").trim();
		return withoutVariants.replaceAll("\\s+[0-9]+%$", "").trim();
	}

	private static Set<String> variantFlags(String name, String constantName)
	{
		String lowerName = name.toLowerCase(Locale.ROOT);
		String upperConstant = constantName.toUpperCase(Locale.ROOT);
		String text = lowerName + " " + upperConstant.toLowerCase(Locale.ROOT);
		Set<String> flags = new LinkedHashSet<>();
		tokenFlag(flags, lowerName, upperConstant, "battle-royale", "BR", "battle_royale", "(br)");
		tokenFlag(flags, lowerName, upperConstant, "last-man-standing", "LMS", "lastman", "(lms)");
		tokenFlag(flags, lowerName, upperConstant, "bounty-hunter", "BH", "bounty", "(bh)");
		tokenFlag(flags, lowerName, upperConstant, "corrupted", "CR", "corrupted", "(cr)");
		flag(flags, text, "tutorial", "tutorial");
		flag(flags, text, "league", "league", "trailblazer");
		tokenFlag(flags, lowerName, upperConstant, "deadman", "DMM", "deadman", "(dmm)");
		flag(flags, text, "inactive", "inactive");
		flag(flags, text, "broken", "broken");
		flag(flags, text, "empty", "empty", "uncharged");
		if (!text.contains("uncharged"))
		{
			tokenFlag(flags, lowerName, upperConstant, "charged", "CHARGED", "(charged)", " charged");
		}
		if (lowerName.matches(".*\\((?:0|25|50|75|100)\\)$") || upperConstant.contains("DEGRADED"))
		{
			flags.add("degraded");
		}
		tokenFlag(flags, lowerName, upperConstant, "noted", "NOTED", "(noted)", " noted");
		return flags;
	}

	private static void tokenFlag(Set<String> flags, String lowerName, String upperConstant,
		String label, String constantToken, String... nameNeedles)
	{
		if (Pattern.compile("(^|_)" + Pattern.quote(constantToken) + "(_|$)").matcher(upperConstant).find())
		{
			flags.add(label);
			return;
		}
		for (String needle : nameNeedles)
		{
			if (lowerName.contains(needle))
			{
				flags.add(label);
				return;
			}
		}
	}

	private static void flag(Set<String> flags, String text, String label, String... needles)
	{
		for (String needle : needles)
		{
			if (text.contains(needle))
			{
				flags.add(label);
				return;
			}
		}
	}

	private static void increment(Map<String, Integer> counts, String key)
	{
		counts.put(key, counts.getOrDefault(key, 0) + 1);
	}

	private static void createParent(Path path) throws IOException
	{
		Path parent = path.toAbsolutePath().getParent();
		if (parent != null)
		{
			Files.createDirectories(parent);
		}
	}

	private static void writeCell(BufferedWriter writer, String value) throws IOException
	{
		writer.write('\t');
		writer.write(value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' '));
	}

	static final class ExportStats
	{
		final int included;
		final int excluded;
		final long duplicateNameGroups;

		ExportStats(int included, int excluded, long duplicateNameGroups)
		{
			this.included = included;
			this.excluded = excluded;
			this.duplicateNameGroups = duplicateNameGroups;
		}
	}

	private static final class RawRecord
	{
		private final int itemId;
		private final String registryName;
		private final String constantName;

		private RawRecord(int itemId, String registryName, String constantName)
		{
			this.itemId = itemId;
			this.registryName = registryName;
			this.constantName = constantName;
		}
	}
}

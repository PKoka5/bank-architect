package com.pkoka5.ironmanbankarchitect.research;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.CategoryOverrideSource;
import com.pkoka5.ironmanbankarchitect.organize.GearStatsSource;
import com.pkoka5.ironmanbankarchitect.organize.ItemValueSource;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

/**
 * Developer-only audit of the actual preview builder, with one owned item at a time.
 * An OK row means the scenario completed; it does not certify item function or bankability.
 * No live item stats, prices, upgrades, player choices or game state are available here.
 */
public final class FinalItemPlacementExporter
{
	private static final Pattern CACHE_ONLY_CONSTANT = Pattern.compile(
		"(^|_)(?:INTERFACE|PLACEHOLDER|DUMMY|NULL)(?:_|$)");
	private static final List<BankPreset> PRESETS = Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN);
	private static final String HEADER = "itemId\tname\tpreset\tscenario\tcatalogCategory\tcatalogSubcategory"
		+ "\tmappedCategoryKey\teffectiveCategory\teffectiveSubcategory\tlayoutTag\tfinalTab"
		+ "\tconstantName\tusageTags\tregistryName\tfinalTabName\tmapperContext\tgearStatsContext"
		+ "\titemValueContext\tupgradeContext\tplayerChoicesContext\tbankabilityContext\treviewContext\tstatus\terror";

	private FinalItemPlacementExporter()
	{
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 3)
		{
			throw new IllegalArgumentException("Expected registry, placements-output and excluded-output paths");
		}
		ExportStats stats = export(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2]));
		System.out.println("Included registry records (bankability unverified): " + stats.included);
		System.out.println("Excluded registry records: " + stats.excluded);
		System.out.println("Placement scenarios: " + stats.rows + "; failed scenarios: " + stats.failed);
	}

	static ExportStats export(Path registry, Path output, Path excludedOutput) throws IOException
	{
		return export(registry, output, excludedOutput, CompositeItemCatalog.DEFAULT, GearStatsSource.NONE);
	}

	/** Injection is for focused regression tests; the CLI always uses NONE for runtime stats. */
	static ExportStats export(Path registry, Path output, Path excludedOutput,
		ItemCatalog catalog, GearStatsSource gearStats) throws IOException
	{
		List<RawRecord> included = new ArrayList<>();
		List<RawRecord> excluded = new ArrayList<>();
		Set<Integer> seenIds = new HashSet<>();
		try (BufferedReader reader = Files.newBufferedReader(registry, StandardCharsets.UTF_8))
		{
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null)
			{
				RawRecord record = parse(line, ++lineNumber);
				if (record != null)
				{
					if (!seenIds.add(record.itemId))
					{
						throw new IllegalArgumentException("Duplicate registry item ID " + record.itemId
							+ " at line " + lineNumber);
					}
					(record.exclusionReason.isEmpty() ? included : excluded).add(record);
				}
			}
		}
		included.sort(Comparator.comparingInt(record -> record.itemId));
		excluded.sort(Comparator.comparingInt(record -> record.itemId));
		createParent(output);
		createParent(excludedOutput);
		int rows = 0;
		int failed = 0;
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			writer.write(HEADER);
			writer.newLine();
			for (int index = 0; index < included.size(); index++)
			{
				RawRecord record = included.get(index);
				for (BankPreset preset : PRESETS)
				{
					failed += writePlacement(writer, record, preset, catalog, gearStats) ? 0 : 1;
					rows++;
				}
				if ((index + 1) % 5000 == 0)
				{
					System.out.println("Placement audit: " + (index + 1) + "/" + included.size()
						+ " registry records; " + failed + " failed scenarios");
				}
			}
		}
		try (BufferedWriter writer = Files.newBufferedWriter(excludedOutput, StandardCharsets.UTF_8))
		{
			writer.write("itemId\tregistryName\tconstantName\treason");
			writer.newLine();
			for (RawRecord record : excluded)
			{
				writeRow(writer, Integer.toString(record.itemId), record.registryName,
					record.constantName, record.exclusionReason);
			}
		}
		return new ExportStats(included.size(), excluded.size(), rows, failed);
	}

	private static boolean writePlacement(BufferedWriter writer, RawRecord record, BankPreset preset,
		ItemCatalog catalog, GearStatsSource gearStats) throws IOException
	{
		CatalogItem catalogItem = null;
		String mappedCategoryKey = "";
		Placement placement = null;
		String status = "OK";
		String error = "";
		try
		{
			catalogItem = catalog.describeOrUnknown(record.itemId);
			mappedCategoryKey = PresetCategoryMapper.map(preset, catalogItem, false).getKey();
			BankSnapshot bank = new BankSnapshot(Collections.singletonList(
				new BankItemSnapshot(record.itemId, 1, 0)));
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
				gearStats, ItemValueSource.NONE, CategoryOverrideSource.NONE,
				BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset));
			placement = placementOf(preview, record.itemId);
		}
		catch (RuntimeException ex)
		{
			status = "FAILED";
			error = ex.getClass().getSimpleName() + ": " + ex.getMessage();
		}
		BankPreviewItem item = placement == null ? null : placement.item;
		List<String> tags = new ArrayList<>(item == null ? Collections.emptySet() : item.getUsageTags());
		Collections.sort(tags);
		writeRow(writer, Integer.toString(record.itemId),
			catalogItem == null ? record.registryName : catalogItem.getDisplayName(),
			preset.getType().name(), "isolated-owned",
			catalogItem == null ? "" : catalogItem.getCategory().name(),
			catalogItem == null ? "" : catalogItem.getSubcategory(), mappedCategoryKey,
			item == null ? "" : item.getItemCategory().name(), item == null ? "" : item.getSubcategory(),
			item == null ? "" : item.getLayoutTagKey(),
			placement == null ? "" : Integer.toString(placement.destination),
			record.constantName, String.join(",", tags), record.registryName,
			placement == null ? "" : placement.destinationName, "base-catalog-without-gathering",
			gearStats == GearStatsSource.NONE ? "unavailable" : "injected-test-source",
			"unavailable", "no-other-owned-items", "preset-defaults-no-overrides-or-capture",
			"unverified-cache-record", "unreviewed-observation", status, error);
		return "OK".equals(status);
	}

	private static Placement placementOf(BankOrganizationPreview preview, int itemId)
	{
		Placement result = null;
		List<BankCategoryPreview> destinations = preview.getCategories();
		for (int destination = 0; destination < destinations.size(); destination++)
		{
			BankCategoryPreview category = destinations.get(destination);
			for (BankPreviewItem item : category.getItems())
			{
				if (!item.isBlank() && item.getItemId() == itemId)
				{
					if (result != null)
					{
						throw new IllegalStateException("Isolated item appears in multiple physical slots: " + itemId);
					}
					result = new Placement(item, destination, category.getCategory().getName());
				}
			}
		}
		if (result == null)
		{
			throw new IllegalStateException("Isolated item missing from final blueprint: " + itemId);
		}
		return result;
	}

	private static RawRecord parse(String input, int lineNumber)
	{
		String line = input.replace("\uFEFF", "");
		if (line.trim().isEmpty() || line.trim().startsWith("#")) return null;
		String[] columns = line.split("\t", -1);
		if (columns.length != 4)
		{
			throw new IllegalArgumentException("Expected four registry columns at line " + lineNumber);
		}
		int itemId;
		try
		{
			itemId = Integer.parseInt(columns[0].trim());
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("Invalid item ID at registry line " + lineNumber, ex);
		}
		String name = columns[1].trim();
		String constantName = columns[3].trim();
		String reason = itemId <= 0 ? "NON_POSITIVE_ID"
			: itemId == ItemID.BANK_FILLER ? "BANK_FILLER"
			: name.isEmpty() || "null".equalsIgnoreCase(name) || "null item".equalsIgnoreCase(name)
				? "NULL_NAME"
			: CACHE_ONLY_CONSTANT.matcher(constantName.toUpperCase(Locale.ROOT)).find()
				? "CACHE_ONLY_CONSTANT" : "";
		return new RawRecord(itemId, name, constantName, reason);
	}

	private static void createParent(Path path) throws IOException
	{
		Path parent = path.toAbsolutePath().getParent();
		if (parent != null) Files.createDirectories(parent);
	}

	private static void writeRow(BufferedWriter writer, String... cells) throws IOException
	{
		for (int index = 0; index < cells.length; index++)
		{
			if (index > 0) writer.write('\t');
			String value = cells[index] == null ? "" : cells[index];
			writer.write(value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' '));
		}
		writer.newLine();
	}

	static final class ExportStats
	{
		final int included;
		final int excluded;
		final int rows;
		final int failed;

		private ExportStats(int included, int excluded, int rows, int failed)
		{
			this.included = included;
			this.excluded = excluded;
			this.rows = rows;
			this.failed = failed;
		}
	}

	private static final class RawRecord
	{
		private final int itemId;
		private final String registryName;
		private final String constantName;
		private final String exclusionReason;

		private RawRecord(int itemId, String registryName, String constantName, String exclusionReason)
		{
			this.itemId = itemId;
			this.registryName = registryName;
			this.constantName = constantName;
			this.exclusionReason = exclusionReason;
		}
	}

	private static final class Placement
	{
		private final BankPreviewItem item;
		private final int destination;
		private final String destinationName;

		private Placement(BankPreviewItem item, int destination, String destinationName)
		{
			this.item = item;
			this.destination = destination;
			this.destinationName = destinationName;
		}
	}
}

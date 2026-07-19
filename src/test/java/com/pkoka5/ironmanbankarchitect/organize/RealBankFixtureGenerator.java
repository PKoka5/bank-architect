package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Test-scope generator for the privacy-minimized real-bank routing fixture. */
public final class RealBankFixtureGenerator
{
	private static final int EXPECTED_ROWS = 770;
	private static final Pattern PLACEMENT_LINE = Pattern.compile(
		"^row=\\d+ col=\\d+ slot=\\d+ \\| id=(\\d+) \\|.*$");

	private RealBankFixtureGenerator()
	{
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			throw new IllegalArgumentException("expected input export and output fixture paths");
		}

		Set<Integer> uniqueIds = new LinkedHashSet<>();
		for (String line : Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8))
		{
			Matcher matcher = PLACEMENT_LINE.matcher(line);
			if (matcher.matches())
			{
				int itemId = Integer.parseInt(matcher.group(1));
				if (!uniqueIds.add(itemId))
				{
					throw new IllegalStateException("duplicate item ID in export: " + itemId);
				}
			}
		}
		if (uniqueIds.size() != EXPECTED_ROWS)
		{
			throw new IllegalStateException("expected " + EXPECTED_ROWS
				+ " unique placement rows but found " + uniqueIds.size());
		}

		List<Integer> sortedIds = new ArrayList<>(uniqueIds);
		Collections.sort(sortedIds);
		List<String> output = new ArrayList<>();
		output.add("itemId\texpectedTabKey");
		for (int itemId : sortedIds)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(itemId)
				.orElse(CatalogItem.unknown(itemId));
			output.add(itemId + "\t"
				+ PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey());
		}

		Path outputPath = Paths.get(args[1]);
		Files.createDirectories(outputPath.toAbsolutePath().getParent());
		Files.write(outputPath, output, StandardCharsets.UTF_8);
		System.out.println("Wrote " + EXPECTED_ROWS + " ID-only fixture rows to " + outputPath);
	}
}

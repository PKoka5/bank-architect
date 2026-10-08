package com.pkoka5.ironmanbankarchitect.research;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import com.pkoka5.ironmanbankarchitect.organize.GearStatsSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.gameval.ItemID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FinalItemPlacementExporterTest
{
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void exportsActualDefaultPresetDoseAndQuickToolPlacement() throws Exception
	{
		ExportResult result = export("2347\tHammer\tSKILLING\tHAMMER\n"
			+ "\uFEFF145\tSuper attack (3)\tPOTION\tSUPER_ATTACK3\n"
			+ "1275\tRune pickaxe\tRUNE\tRUNE_PICKAXE\n");

		assertEquals(3, result.stats.included);
		assertEquals(6, result.stats.rows);
		assertEquals(0, result.stats.failed);
		assertPlacement(result, 145, "IRONMAN", "POTION", "potion-doses", "3");
		assertPlacement(result, 145, "MAIN", "POTION", "potions", "3");
		assertPlacement(result, 2347, "IRONMAN", "TOOL", "frequently-used", "0");
		assertPlacement(result, 2347, "MAIN", "TOOL", "tools", "5");
		assertEquals("skilling-tools", result.row(1275, "IRONMAN").get("mappedCategoryKey"));
		assertPlacement(result, 1275, "IRONMAN", "TOOL", "frequently-used", "0");
		assertEquals("145", result.rows.get(0).get("itemId"));
		assertEquals("IRONMAN", result.rows.get(0).get("preset"));
		assertEquals("MAIN", result.rows.get(1).get("preset"));
		for (Map<String, String> row : result.rows)
		{
			assertEquals("isolated-owned", row.get("scenario"));
			assertEquals("unavailable", row.get("gearStatsContext"));
			assertEquals("unavailable", row.get("itemValueContext"));
			assertEquals("no-other-owned-items", row.get("upgradeContext"));
			assertEquals("unverified-cache-record", row.get("bankabilityContext"));
			assertEquals("unreviewed-observation", row.get("reviewContext"));
			assertEquals("preset-defaults-no-overrides-or-capture", row.get("playerChoicesContext"));
		}
	}

	@Test
	public void reportsCurrencyAndAchievementUtilityCorrectionsInBothPresets() throws Exception
	{
		ExportResult result = export("12012\tGolden nugget\tCURRENCY\tMOTHERLODE_NUGGET\n"
			+ "21555\tNumulite\tCURRENCY\tFOSSIL_NUMULITE\n"
			+ "25527\tStardust\tCURRENCY\tSTAR_DUST\n"
			+ "6529\tTokkul\tCURRENCY\tTZHAAR_TOKEN\n"
			+ "6306\tTrading sticks\tCURRENCY\tVILLAGE_TRADE_STICKS\n"
			+ "22947\tRada's blessing 4\tUNKNOWN\tZEAH_BLESSING_ELITE\n"
			+ "25926\tGhommal's hilt 1\tUNKNOWN\tCA_OFFHAND_EASY\n"
			+ "13142\tWestern banner 2\tUNKNOWN\tWESTERN_BANNER_MEDIUM\n"
			+ "13144\tWestern banner 4\tUNKNOWN\tWESTERN_BANNER_ELITE\n");

		assertEquals(0, result.stats.failed);
		for (String preset : new String[] {"IRONMAN", "MAIN"})
		{
			for (int id : new int[] {12012, 21555, 25527, 6529, 6306})
			{
				assertPlacement(result, id, preset, "CURRENCY", "currency", "0");
			}
			for (int id : new int[] {22947, 25926, 13144})
			{
				assertPlacement(result, id, preset, "TELEPORT", "teleports", "MAIN".equals(preset) ? "1" : "0");
			}
		}
		assertPlacement(result, 13142, "IRONMAN", "TOOL", "frequently-used", "0");
		assertPlacement(result, 13142, "MAIN", "TOOL", "tools", "5");
	}

	@Test
	public void exposesGearPromotionBeyondTheCatalogAndMapperStages() throws Exception
	{
		int id = 900020;
		ItemCatalog catalog = itemId -> Optional.of(CatalogItem.unknown(itemId));
		GearStatsSource stats = itemId -> Optional.of(new GearStats(GearSlot.WEAPON,
			10, 10, 10, 0, 0, 10, 0, 0, 0));
		ExportResult result = export(id + "\tFuture sword\tUNKNOWN\tFUTURE_SWORD\n", catalog, stats);

		assertEquals(0, result.stats.failed);
		for (String preset : new String[] {"IRONMAN", "MAIN"})
		{
			Map<String, String> row = result.row(id, preset);
			assertEquals("UNKNOWN", row.get("catalogCategory"));
			assertEquals("unknown", row.get("catalogSubcategory"));
			assertEquals("storage-cleanup", row.get("mappedCategoryKey"));
			assertEquals("weapon", row.get("effectiveSubcategory"));
			assertEquals("injected-test-source", row.get("gearStatsContext"));
			assertPlacement(result, id, preset, "GEAR", "gear", "MAIN".equals(preset) ? "2" : "1");
		}
	}

	@Test
	public void unknownItemsAndNewSubcategoriesRemainVisibleWithoutClaimingBankability() throws Exception
	{
		ItemCatalog catalog = id -> id == 900030 ? Optional.of(new CatalogItem(id, "Future tool",
			ItemCategory.TOOL, "new-tool-subcategory", Collections.singleton("future-usage-tag"), null))
			: Optional.empty();
		ExportResult result = export("900030\tFuture tool\tUNKNOWN\tFUTURE_TOOL\n"
			+ "900031\tFuture item\tUNKNOWN\tFUTURE_ITEM\n", catalog, GearStatsSource.NONE);

		assertEquals(4, result.stats.rows);
		assertEquals(0, result.stats.failed);
		for (String preset : new String[] {"IRONMAN", "MAIN"})
		{
			assertPlacement(result, 900030, preset, "TOOL", "tools", "5");
			assertEquals("future-usage-tag", result.row(900030, preset).get("usageTags"));
			assertPlacement(result, 900031, preset, "UNKNOWN", "cleanup", "9");
			assertEquals("Future item", result.row(900031, preset).get("registryName"));
		}
	}

	@Test
	public void excludesOnlyExplicitCacheTokensOrNullNamesWithoutGuessingFromItemNames() throws Exception
	{
		ExportResult result = export("# registry subset\n\n"
			+ "900000\tInterface thing\tUNKNOWN\tBANK_INTERFACE_DUMMY\n"
			+ "900001\tPlaceholder thing\tUNKNOWN\tITEM_PLACEHOLDER\n"
			+ "900002\tNull thing\tUNKNOWN\tNULL_ITEM\n"
			+ "900003\tDummy thing\tUNKNOWN\tdummy_item\n"
			+ "900004\tnull\tUNKNOWN\tUNUSED_ITEM\n"
			+ "900005\t\tUNKNOWN\tUNNAMED_ITEM\n"
			+ "0\tInvalid ID\tUNKNOWN\tINVALID_ID\n"
			+ "900006\tInterface-looking name\tUNKNOWN\tINTERFACESHIELD\n"
			+ "900007\tPlaceholder-looking name\tUNKNOWN\tPLACEHOLDERSWORD\n"
			+ "900008\tVariant whose bankability is unknown\tUNKNOWN\tWEAPON_BR_VARIANT\n"
			+ "900009\tCertificate needs exact runtime definitions\tUNKNOWN\tCERT_FUTURE_ITEM\n");

		assertEquals(4, result.stats.included);
		assertEquals(7, result.stats.excluded);
		assertEquals(8, result.stats.rows);
		assertEquals(0, result.stats.failed);
		assertEquals(8, result.excluded.size());
		assertTrue(result.excluded.stream().anyMatch(row -> row.equals("900004\tnull\tUNUSED_ITEM\tNULL_NAME")));
		assertTrue(result.excluded.stream().anyMatch(row -> row.startsWith("900003\tDummy thing\tdummy_item\tCACHE_ONLY_CONSTANT")));
		assertEquals("unverified-cache-record", result.row(900008, "MAIN").get("bankabilityContext"));
		assertEquals("unverified-cache-record", result.row(900009, "MAIN").get("bankabilityContext"));
	}

	@Test
	public void excludesExactBankFillerFilteredBySnapshotWithoutGuessingFromName() throws Exception
	{
		ItemCatalog catalog = id ->
		{
			if (id == ItemID.BANK_FILLER) throw new AssertionError("Bank filler must never reach the planner");
			return Optional.empty();
		};
		ExportResult result = export(ItemID.BANK_FILLER + "\tBank filler\tCURRENCY\tBANK_FILLER\n"
			+ "900040\tBank filler\tUNKNOWN\tFUTURE_FILLER\n", catalog, GearStatsSource.NONE);

		assertEquals(1, result.stats.included);
		assertEquals(1, result.stats.excluded);
		assertEquals(2, result.stats.rows);
		assertEquals(0, result.stats.failed);
		assertTrue(result.excluded.contains(ItemID.BANK_FILLER + "\tBank filler\tBANK_FILLER\tBANK_FILLER"));
		assertPlacement(result, 900040, "MAIN", "UNKNOWN", "cleanup", "9");
	}

	@Test
	public void rejectsMalformedAndDuplicateRegistryRowsBeforeWritingOutputs() throws Exception
	{
		assertInvalidRegistry("145\tPotion\tPOTION\tSUPER_ATTACK3\n145\tPotion\tPOTION\tSUPER_ATTACK3\n",
			"Duplicate registry item ID 145 at line 2");
		assertInvalidRegistry("145\tPotion\tPOTION\tSUPER_ATTACK3\n145\tCache alias\tUNKNOWN\tITEM_DUMMY\n",
			"Duplicate registry item ID 145 at line 2");
		assertInvalidRegistry("145\tPotion\tPOTION\n", "Expected four registry columns at line 1");
		assertInvalidRegistry("invalid\tPotion\tPOTION\tSUPER_ATTACK3\n", "Invalid item ID at registry line 1");
	}

	@Test
	public void retainsPerItemFailuresAndContinuesWithOtherItems() throws Exception
	{
		ItemCatalog catalog = id ->
		{
			if (id == 145) throw new IllegalStateException("bad\titem\r\nmetadata");
			return CompositeItemCatalog.DEFAULT.findById(id);
		};
		ExportResult result = export("145\tSuper attack (3)\tPOTION\tSUPER_ATTACK3\n"
			+ "2347\tHammer\tSKILLING\tHAMMER\n", catalog, GearStatsSource.NONE);

		assertEquals(4, result.stats.rows);
		assertEquals(2, result.stats.failed);
		for (String preset : new String[] {"IRONMAN", "MAIN"})
		{
			Map<String, String> failure = result.row(145, preset);
			assertEquals("FAILED", failure.get("status"));
			assertEquals("Super attack (3)", failure.get("name"));
			assertEquals("", failure.get("finalTab"));
			assertEquals("IllegalStateException: bad item  metadata", failure.get("error"));
			assertEquals("OK", result.row(2347, preset).get("status"));
		}
	}

	private void assertInvalidRegistry(String content, String expectedMessage) throws Exception
	{
		Path directory = temporary.newFolder().toPath();
		Path registry = directory.resolve("registry.tsv");
		Path output = directory.resolve("placements.tsv");
		Path excluded = directory.resolve("excluded.tsv");
		Files.write(registry, content.getBytes(StandardCharsets.UTF_8));
		try
		{
			FinalItemPlacementExporter.export(registry, output, excluded);
			fail("Invalid registry input must fail before producing observations");
		}
		catch (IllegalArgumentException ex)
		{
			assertEquals(expectedMessage, ex.getMessage());
		}
		assertFalse(Files.exists(output));
		assertFalse(Files.exists(excluded));
	}

	private ExportResult export(String registry) throws Exception
	{
		return export(registry, CompositeItemCatalog.DEFAULT, GearStatsSource.NONE);
	}

	private ExportResult export(String content, ItemCatalog catalog, GearStatsSource gearStats) throws Exception
	{
		Path directory = temporary.newFolder().toPath();
		Path registry = directory.resolve("registry.tsv");
		Path output = directory.resolve("nested/placements.tsv");
		Path excluded = directory.resolve("nested/excluded.tsv");
		Files.write(registry, content.getBytes(StandardCharsets.UTF_8));
		FinalItemPlacementExporter.ExportStats stats = FinalItemPlacementExporter.export(
			registry, output, excluded, catalog, gearStats);
		List<String> lines = Files.readAllLines(output, StandardCharsets.UTF_8);
		String[] headers = lines.get(0).split("\t", -1);
		List<Map<String, String>> rows = new ArrayList<>();
		for (String line : lines.subList(1, lines.size()))
		{
			String[] cells = line.split("\t", -1);
			assertEquals("Every output row must preserve the TSV schema", headers.length, cells.length);
			Map<String, String> row = new LinkedHashMap<>();
			for (int index = 0; index < headers.length; index++) row.put(headers[index], cells[index]);
			rows.add(row);
		}
		return new ExportResult(stats, rows, Files.readAllLines(excluded, StandardCharsets.UTF_8));
	}

	private static void assertPlacement(ExportResult result, int id, String preset,
		String category, String tag, String tab)
	{
		Map<String, String> row = result.row(id, preset);
		assertEquals("OK", row.get("status"));
		assertEquals(category, row.get("effectiveCategory"));
		assertEquals(tag, row.get("layoutTag"));
		assertEquals(tab, row.get("finalTab"));
	}

	private static final class ExportResult
	{
		private final FinalItemPlacementExporter.ExportStats stats;
		private final List<Map<String, String>> rows;
		private final List<String> excluded;

		private ExportResult(FinalItemPlacementExporter.ExportStats stats,
			List<Map<String, String>> rows, List<String> excluded)
		{
			this.stats = stats;
			this.rows = rows;
			this.excluded = excluded;
		}

		private Map<String, String> row(int id, String preset)
		{
			return rows.stream().filter(row -> Integer.toString(id).equals(row.get("itemId"))
				&& preset.equals(row.get("preset"))).findFirst().orElseThrow(() ->
				new AssertionError("Missing exported row for " + id + "/" + preset));
		}
	}
}

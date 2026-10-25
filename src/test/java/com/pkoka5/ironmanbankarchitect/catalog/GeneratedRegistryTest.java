package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class GeneratedRegistryTest
{
	private static final String COMPILED =
		"/com/pkoka5/ironmanbankarchitect/catalog/compiled-item-registry.tsv";
	private static final String VALID = "# schema=1\n1\tExample\tTOOL\ttool\tteleport,utility\n";

	@Rule public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void allOriginalRowsMatchTheCompiledRuntimeCatalogExactly()
	{
		Map<Integer, CatalogItem> expected = RegistryCompiler.loadItems(
			getClass().getResourceAsStream(RegistryCompiler.RESOURCE_PATH),
			CanonicalItemClassificationOverrides.INSTANCE);
		Map<Integer, CatalogItem> actual = ResourceItemRegistry.loadItems(getClass().getResourceAsStream(COMPILED));
		assertEquals(33766, expected.size());
		assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()));
		assertEquals(expected.size(), ResourceItemRegistry.INSTANCE.size());
		for (Map.Entry<Integer, CatalogItem> row : expected.entrySet())
		{
			assertFacts(row.getValue(), actual.get(row.getKey()));
			assertFacts(row.getValue(), ResourceItemRegistry.INSTANCE.findById(row.getKey()).get());
		}
		assertTrue(actual.containsKey(20594)); // The bank reader filters fillers, not the fact compiler.
		assertFalse(ResourceItemRegistry.INSTANCE.findById(900001).isPresent());
	}

	@Test
	public void checkedInArtifactMatchesFreshCompilationByteForByte() throws Exception
	{
		try (InputStream input = getClass().getResourceAsStream(COMPILED))
		{
			assertNotNull(input);
			assertEquals(RegistryCompiler.compiledText(), new String(input.readAllBytes(), StandardCharsets.UTF_8));
		}
	}

	@Test
	public void staticCatalogKeepsItsOwnWorkflowPrecedence()
	{
		CatalogItem compiled = ResourceItemRegistry.INSTANCE.findById(145).get();
		assertFalse(compiled.getWorkflowKey().isPresent());
		CatalogItem effective = CompositeItemCatalog.DEFAULT.findById(145).get();
		assertEquals("herblore.super-attack.3", effective.getWorkflowKey().get());
		assertEquals("dose-3", effective.getSubcategory());
		assertEquals("potion-dose-3", compiled.getSubcategory());
	}

	@Test
	public void explicitGenerationAndCheckAreDeterministicAndChecksNeverRewrite() throws Exception
	{
		Path output = temporary.getRoot().toPath().resolve("nested/compiled.tsv");
		RegistryCompiler.main(new String[]{output.toString()});
		String generated = Files.readString(output, StandardCharsets.UTF_8);
		assertEquals(RegistryCompiler.compiledText(), generated);
		RegistryCompiler.main(new String[]{output.toString(), "--check"});
		RegistryCompiler.main(new String[]{output.toString()});
		assertEquals(generated, Files.readString(output, StandardCharsets.UTF_8));
		String stale = generated + "# stale\n";
		Files.writeString(output, stale, StandardCharsets.UTF_8);
		assertThrows(IllegalStateException.class,
			() -> RegistryCompiler.main(new String[]{output.toString(), "--check"}));
		assertEquals(stale, Files.readString(output, StandardCharsets.UTF_8));
		Path missing = temporary.getRoot().toPath().resolve("missing.tsv");
		assertThrows(IllegalStateException.class,
			() -> RegistryCompiler.main(new String[]{missing.toString(), "--check"}));
		assertFalse(Files.exists(missing));
	}

	@Test
	public void generatorRejectsUnknownArgumentsWithoutWriting() throws Exception
	{
		Path output = temporary.getRoot().toPath().resolve("not-written.tsv");
		for (String[] args : new String[][]{new String[0], {output.toString(), "--unknown"},
			{output.toString(), "--check", "extra"}})
			assertThrows(IllegalArgumentException.class, () -> RegistryCompiler.main(args));
		assertFalse(Files.exists(output));
	}

	@Test
	public void runtimeParserPreservesFactsAndClosesItsStream()
	{
		boolean[] closed = {false};
		InputStream input = new ByteArrayInputStream(VALID.getBytes(StandardCharsets.UTF_8))
		{
			@Override public void close() { closed[0] = true; }
		};
		CatalogItem item = ResourceItemRegistry.loadItems(input).get(1);
		assertEquals("Example", item.getDisplayName());
		assertEquals(ItemCategory.TOOL, item.getCategory());
		assertEquals("tool", item.getSubcategory());
		assertEquals(List.of("teleport", "utility"), new ArrayList<>(item.getTags()));
		assertFalse(item.getWorkflowKey().isPresent());
		assertTrue(closed[0]);
		assertThrows(UnsupportedOperationException.class, () -> item.getTags().add("other"));
		assertTrue(ResourceItemRegistry.loadItems(stream("# schema=1\n1\tExample\tTOOL\ttool\t\n"))
			.get(1).getTags().isEmpty());
	}

	@Test
	public void runtimeParserRejectsMalformedRowsAndUnsupportedSchemas()
	{
		List<String> malformed = new ArrayList<>(List.of("", "# schema=1\n", "# schema=2\n",
			"# schema=10\n", "# schema=1 annotation\n", "# schema=1\n1\tExample\tTOOL\ttool\n",
			"# schema=1\n1\tExample\tTOOL\ttool\t\textra\n"));
		for (String row : List.of("bad\tExample\tTOOL\ttool\t", "0\tExample\tTOOL\ttool\t",
			"-1\tExample\tTOOL\ttool\t", "2\t\tTOOL\ttool\t", "2\t  \tTOOL\ttool\t",
			"2\tExample\tINVALID\ttool\t", "2\tExample\tTOOL\t\t",
			"2\tExample\tTOOL\ttool\tTag", "2\tExample\tTOOL\ttool\tutility,utility",
			"2\tExample\tTOOL\ttool\t,utility", "2\tExample\tTOOL\ttool\tutility,",
			"1\tDuplicate\tGEAR\tgear\t"))
			malformed.add(VALID + row + "\n");
		for (String text : malformed)
		{
			try
			{
				ResourceItemRegistry.loadItems(stream(text));
				fail("Malformed compiled data accepted: " + text);
			}
			catch (IllegalStateException | IllegalArgumentException expected) { }
		}
		assertThrows(IllegalStateException.class, () -> ResourceItemRegistry.loadItems(null));
	}

	@Test
	public void compiledParsingDoesNotDependOnTheDefaultLocale()
	{
		synchronized (Locale.class)
		{
			Locale previous = Locale.getDefault();
			try
			{
				Locale.setDefault(new Locale("tr", "TR"));
				CatalogItem item = ResourceItemRegistry.loadItems(stream(VALID)).get(1);
				assertEquals(ItemCategory.TOOL, item.getCategory());
				assertEquals("Example", item.getDisplayName());
			}
			finally { Locale.setDefault(previous); }
		}
	}

	@Test
	public void brokenCompiledDataBlocksAnalysisEvenWhenStaticCatalogKnowsTheItem()
	{
		for (String text : new String[]{null, "", "# schema=2\n", VALID + "1\tDuplicate\tGEAR\tgear\t\n"})
		{
			ItemCatalog catalog = new CompositeItemCatalog(StaticItemCatalog.INSTANCE,
				new ResourceItemRegistry(text == null ? null : stream(text)));
			assertThrows(CatalogUnavailableException.class, () -> catalog.findById(5297));
			for (List<BankItemSnapshot> items : List.of(Collections.<BankItemSnapshot>emptyList(),
				Collections.singletonList(new BankItemSnapshot(5297, 1, 0))))
			{
				BankAnalysisRequest request = new BankAnalysisRequest(new BankSnapshot(items),
					Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
					BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
				List<BankAnalysisStatus> statuses = new ArrayList<>();
				BankAnalysis analysis = new BankAnalysis(Runnable::run, Runnable::run,
					() -> Optional.of(request), statuses::add, catalog, BankPresets.IRONMAN);
				try
				{
					for (int attempt = 0; attempt < 2; attempt++)
					{
						analysis.analyzeBank();
						BankAnalysisStatus status = statuses.get(statuses.size() - 1);
						assertEquals(BankAnalysisStatus.Kind.FAILED, status.kind());
						String message = "Bank analysis is unavailable because its compiled item registry data is missing or invalid.";
						assertEquals(message, status.catalogSummaryText());
						assertEquals(message, status.organizationPreviewText());
						assertFalse(status.catalogSummary().isPresent());
						assertFalse(status.organizationPreview().isPresent());
					}
				}
				finally { analysis.close(); }
			}
		}
	}

	private static void assertFacts(CatalogItem expected, CatalogItem actual)
	{
		assertNotNull("Missing compiled item " + expected.getItemId(), actual);
		assertEquals(expected.getItemId(), actual.getItemId());
		assertEquals("name for " + expected.getItemId(), expected.getDisplayName(), actual.getDisplayName());
		assertEquals("category for " + expected.getItemId(), expected.getCategory(), actual.getCategory());
		assertEquals("subcategory for " + expected.getItemId(), expected.getSubcategory(), actual.getSubcategory());
		assertEquals("tags for " + expected.getItemId(), new ArrayList<>(expected.getTags()), new ArrayList<>(actual.getTags()));
		assertEquals("workflow for " + expected.getItemId(), expected.getWorkflowKey(), actual.getWorkflowKey());
	}

	private static InputStream stream(String text)
	{
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}

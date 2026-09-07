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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

import static org.junit.Assert.*;

public class CanonicalItemClassificationOverridesTest
{
	@Test
	public void missingResourceBlocksAnalysisWithoutPoisoningCatalogClasses()
	{
		assertUnavailable(getClass().getResourceAsStream("/missing-override-test.tsv"));
		assertTrue(CanonicalItemClassificationOverrides.find(29577).isPresent());
	}

	@Test
	public void duplicateIdBlocksAnalysisInsteadOfUsingAPartialTable()
	{
		assertUnavailable(stream("# schema=1\n1\tGEAR\tgear\n1\tTOOL\ttool\n"));
	}

	@Test
	public void malformedRowsBlockAnalysis()
	{
		assertUnavailable(stream("# schema=1\n# No rows\n"));
		for (String row : List.of("1\tGEAR", "no-id\tGEAR\tgear", "1\tNO_CATEGORY\tgear",
			"0\tGEAR\tgear", "1\tGEAR\t"))
		{
			assertUnavailable(stream("# schema=1\n" + row + "\n"));
		}
	}

	@Test
	public void schemaMustBeExactlyVersionOneWithoutAnnotations()
	{
		for (String header : List.of("# schema=10", "# schema=1garbage", "# schema=2",
			"# schema=1 annotation", ""))
		{
			assertUnavailable(stream(header + "\n1\tGEAR\tgear\n"));
		}
	}

	@Test
	public void validTableClassifiesAndClosesItsStream()
	{
		boolean[] closed = {false};
		InputStream input = new ByteArrayInputStream(
			"# schema=1\n1\tGEAR\tgear\tReadable note\n".getBytes(StandardCharsets.UTF_8))
		{
			@Override
			public void close()
			{
				closed[0] = true;
			}
		};
		CanonicalItemClassificationOverrides table = new CanonicalItemClassificationOverrides(input);
		table.requireAvailable();
		assertEquals(ItemCategory.GEAR, table.lookup(1).get().getCategory());
		assertFalse(table.lookup(2).isPresent());
		assertTrue(closed[0]);
	}

	private static InputStream stream(String text)
	{
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}

	private static void assertUnavailable(InputStream stream)
	{
		CanonicalItemClassificationOverrides table = new CanonicalItemClassificationOverrides(stream);
		ResourceItemRegistry registry = new ResourceItemRegistry(table);
		ItemCatalog catalog = new CompositeItemCatalog(StaticItemCatalog.INSTANCE, registry);
		// Even an item known to the first catalogue must not bypass the failed required table.
		assertThrows(CatalogUnavailableException.class, () -> catalog.findById(5297));
		assertThrows(CatalogUnavailableException.class, () -> table.lookup(1));
		for (List<BankItemSnapshot> items : List.of(Collections.<BankItemSnapshot>emptyList(),
			Collections.singletonList(new BankItemSnapshot(5297, 1, 0))))
		{
			BankAnalysisRequest request = new BankAnalysisRequest(new BankSnapshot(items),
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
				BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
			List<BankAnalysisStatus> statuses = new ArrayList<>();
			BankAnalysis analysis = new BankAnalysis(Runnable::run, Runnable::run,
				() -> Optional.of(request), statuses::add, catalog, BankPresets.IRONMAN);
			// Retrying remains a controlled failure, never NoClassDefFoundError or partial success.
			for (int attempt = 0; attempt < 2; attempt++)
			{
				analysis.analyzeBank();
				BankAnalysisStatus status = statuses.get(statuses.size() - 1);
				assertEquals(BankAnalysisStatus.Kind.FAILED, status.kind());
				assertEquals(CatalogUnavailableException.PLAYER_MESSAGE, status.catalogSummaryText());
				assertEquals(CatalogUnavailableException.PLAYER_MESSAGE, status.organizationPreviewText());
				assertFalse(status.catalogSummary().isPresent());
				assertFalse(status.organizationPreview().isPresent());
			}
			analysis.close();
		}
	}
}

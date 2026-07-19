package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Regression coverage for the maintainer's privacy-minimized 2026-07-19 real bank.
 *
 * <p>Deliberate regeneration: place the reviewed raw export at
 * {@code tmp/bank-export-2026-07-19.txt}, run {@code ./gradlew generateRealBankFixture}, inspect
 * the complete TSV diff, and update it only alongside the intended routing-policy change.</p>
 */
public class RealBankFixtureTest
{
	private static final int EXPECTED_ROWS = 770;
	private static final String RESOURCE =
		"/com/pkoka5/ironmanbankarchitect/organize/real-bank-fixture-2026-07-19.tsv";

	@Test
	public void everyRealBankItemRetainsItsReviewedIronmanTab() throws IOException
	{
		Map<Integer, String> expected = loadFixture();
		assertEquals("fixture row count", EXPECTED_ROWS, expected.size());

		StringBuilder differences = new StringBuilder();
		for (Map.Entry<Integer, String> row : expected.entrySet())
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.findById(row.getKey())
				.orElse(CatalogItem.unknown(row.getKey()));
			String actual = PresetCategoryMapper.map(BankPresets.IRONMAN, item).getKey();
			if (!row.getValue().equals(actual))
			{
				differences.append("itemId=").append(row.getKey())
					.append(" expected=").append(row.getValue())
					.append(" actual=").append(actual).append('\n');
			}
		}
		assertTrue("real-bank routing differences:\n" + differences, differences.length() == 0);
	}

	private static Map<Integer, String> loadFixture() throws IOException
	{
		InputStream stream = RealBankFixtureTest.class.getResourceAsStream(RESOURCE);
		if (stream == null)
		{
			throw new IllegalStateException("missing real-bank fixture: " + RESOURCE);
		}

		Map<Integer, String> rows = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			assertEquals("itemId\texpectedTabKey", reader.readLine());
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null)
			{
				lineNumber++;
				String[] fields = line.split("\\t", -1);
				assertEquals("fixture columns at line " + lineNumber, 2, fields.length);
				int itemId = Integer.parseInt(fields[0]);
				assertTrue("positive item ID at line " + lineNumber, itemId > 0);
				assertTrue("tab key at line " + lineNumber, !fields[1].isEmpty());
				assertTrue("duplicate item ID at line " + lineNumber,
					rows.put(itemId, fields[1]) == null);
			}
		}
		return rows;
	}
}

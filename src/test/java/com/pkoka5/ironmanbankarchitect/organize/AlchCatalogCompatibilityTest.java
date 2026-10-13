package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** Public results captured from compiled 0.9.5 classes before moving alch declarations to data. */
public class AlchCatalogCompatibilityTest
{
	private static final List<String[]> BASELINE = baseline();

	@Test
	public void reviewedCanonicalCandidatesKeepExactlyThePreviousMembership()
	{
		Set<Integer> expected = ids("CANDIDATE");
		assertEquals(100, expected.size());
		int lastId = limit();
		for (int id = 0; id <= lastId; id++)
			assertEquals("Candidate membership for " + id, expected.contains(id),
				IronmanAlchCandidateCatalog.contains(id));
		for (int id : new int[] {-1, Integer.MIN_VALUE, Integer.MAX_VALUE})
			assertFalse("Invalid candidate " + id, IronmanAlchCandidateCatalog.contains(id));
	}

	@Test
	public void elementalStaffRolesKeepTheirPreviousExactMembers()
	{
		Set<Integer> expected = ids("ELEMENTAL");
		assertEquals(12, expected.size());
		int lastId = limit();
		for (int id = 0; id <= lastId; id++)
			assertEquals("Elemental staff membership for " + id, expected.contains(id),
				IronmanAlchCandidateCatalog.isElementalStaff(id));
	}

	@Test
	public void replacementRolesKeepOwnedTierColourAndPinnedChoicePrecedence()
	{
		int queries = 0;
		for (String[] fields : BASELINE)
		{
			if (!"ROLE".equals(fields[0])) continue;
			Set<Integer> owned = parseIds(fields[2]);
			Map<Integer, String> choices = new HashMap<>();
			if (!fields[3].isEmpty())
				for (String choice : fields[3].split(","))
				{
					String[] pair = choice.split("=", 2);
					choices.put(Integer.parseInt(pair[0]), pair[1]);
				}
			assertEquals(String.join("\t", fields), Boolean.parseBoolean(fields[4]),
				IronmanAlchCandidateCatalog.hasRoleReplacement(Integer.parseInt(fields[1]), owned, choices));
			queries++;
		}
		assertEquals(1811, queries);
	}

	private static int limit()
	{
		return BASELINE.stream().filter(fields -> "LIMIT".equals(fields[0]))
			.mapToInt(fields -> Integer.parseInt(fields[1])).findFirst().getAsInt();
	}

	private static Set<Integer> ids(String kind)
	{
		Set<Integer> ids = new HashSet<>();
		for (String[] fields : BASELINE)
			if (kind.equals(fields[0])) ids.add(Integer.parseInt(fields[1]));
		return ids;
	}

	private static Set<Integer> parseIds(String serialized)
	{
		Set<Integer> ids = new HashSet<>();
		if (!serialized.isEmpty())
			for (String id : serialized.split(",")) ids.add(Integer.parseInt(id));
		return ids;
	}

	private static List<String[]> baseline()
	{
		InputStream stream = AlchCatalogCompatibilityTest.class.getResourceAsStream("alch-catalog-compatibility.tsv");
		assertNotNull("Frozen pre-migration alch behavior", stream);
		List<String[]> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
				if (!line.startsWith("#")) rows.add(line.split("\t", -1));
		}
		catch (IOException failure) { throw new AssertionError("Cannot read frozen alch behavior", failure); }
		return rows;
	}
}

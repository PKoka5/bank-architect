package com.pkoka5.ironmanbankarchitect.organize;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Ordered bundled preset/tag declarations; runtime routing stays in the existing classes. */
final class BankDefinitionRows
{
	static List<String[]> read(String resource, int columns)
	{
		InputStream stream = BankDefinitionRows.class.getResourceAsStream(resource);
		if (stream == null) throw new IllegalStateException("Missing bank definitions: " + resource);
		List<String[]> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			if (!"# schema=1".equals(reader.readLine())) throw new IllegalStateException("Invalid bank definition schema");
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] fields = line.split("\t", -1);
				if (fields.length != columns) throw new IllegalStateException("Invalid bank definition row: " + resource);
				rows.add(fields);
			}
		}
		catch (IOException failure) { throw new IllegalStateException("Cannot load bank definitions", failure); }
		return rows;
	}
}

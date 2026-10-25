package com.pkoka5.ironmanbankarchitect.organize;

import java.io.*;
import com.pkoka5.ironmanbankarchitect.util.BundledTable;
import java.util.*;

/** Ordered bundled preset/tag declarations; runtime routing stays in the existing classes. */
final class BankDefinitionRows
{
	static List<String[]> read(String resource, int columns)
	{
		InputStream stream = BankDefinitionRows.class.getResourceAsStream(resource);
		List<String[]> rows = new ArrayList<>();
		try
		{
			BundledTable.read(stream, resource, false, false, (line, fields) ->
			{
				if (fields.length != columns) throw new IllegalStateException("Invalid bank definition row: " + resource);
				rows.add(fields);
			});
		}
		catch (IOException failure) { throw new IllegalStateException("Cannot load bank definitions", failure); }
		return rows;
	}
}

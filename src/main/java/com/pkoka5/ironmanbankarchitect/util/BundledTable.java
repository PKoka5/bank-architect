package com.pkoka5.ironmanbankarchitect.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Shared framing for local schema-1 TSV resources; callers validate their own rows. */
public final class BundledTable
{
	private BundledTable() { }

	/** Header whitespace and ignored rows follow the declaring table's existing format. */
	public static void read(InputStream stream, String label, boolean trimHeader,
		boolean skipIgnored, RowReader row) throws IOException
	{
		if (stream == null) throw new IOException("Missing table: " + label);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (header == null || !"# schema=1".equals(trimHeader ? header.trim() : header))
				throw new IOException("Invalid table schema: " + label);
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (skipIgnored && (line.isEmpty() || line.startsWith("#"))) continue;
				row.read(line, line.split("\t", -1));
			}
		}
	}

	@FunctionalInterface
	public interface RowReader
	{
		void read(String line, String[] fields) throws IOException;
	}
}

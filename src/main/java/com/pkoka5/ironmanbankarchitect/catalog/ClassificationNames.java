package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Ordered name groups; matching and rule precedence remain in the refiner. */
public final class ClassificationNames
{
	private static final RequiredResource<List<String[]>> GROUPS = new RequiredResource<>(
		"classification names", () -> load(ClassificationNames.class.getResourceAsStream("classification-names.tsv")));

	public static String[] group(int index) { return GROUPS.get().get(index).clone(); }

	static List<String[]> load(InputStream stream)
	{
		if (stream == null) throw new IllegalStateException("Missing classification names");
		List<String[]> groups = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			if (!"# schema=1".equals(reader.readLine())) throw new IllegalStateException("Invalid name-group schema");
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] fields = line.split("\t", -1);
				if (fields.length < 2 || Integer.parseInt(fields[0]) != groups.size())
					throw new IllegalStateException("Invalid name-group index");
				for (String field : fields) if (field.isEmpty()) throw new IllegalStateException("Empty name-group field");
				groups.add(java.util.Arrays.copyOfRange(fields, 1, fields.length));
			}
		}
		catch (java.io.IOException ex) { throw new IllegalStateException("Cannot read classification names", ex); }
		if (groups.size() != 76) throw new IllegalStateException("Incomplete classification names");
		return java.util.Collections.unmodifiableList(groups);
	}
}

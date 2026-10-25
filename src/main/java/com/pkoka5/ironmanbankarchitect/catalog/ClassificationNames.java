package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.*;
import com.pkoka5.ironmanbankarchitect.util.BundledTable;
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
		List<String[]> groups = new ArrayList<>();
		try
		{
			BundledTable.read(stream, "classification names", false, false, (line, fields) ->
			{
				if (fields.length < 2 || Integer.parseInt(fields[0]) != groups.size())
					throw new IllegalStateException("Invalid name-group index");
				for (String field : fields) if (field.isEmpty()) throw new IllegalStateException("Empty name-group field");
				groups.add(java.util.Arrays.copyOfRange(fields, 1, fields.length));
			});
		}
		catch (java.io.IOException ex) { throw new IllegalStateException("Cannot read classification names", ex); }
		if (groups.size() != 88) throw new IllegalStateException("Incomplete classification names");
		return java.util.Collections.unmodifiableList(groups);
	}
}

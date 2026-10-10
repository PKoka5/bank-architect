package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClassificationNamesTest
{
	@Test public void bundledGroupsAreComplete()
	{
		assertEquals(81, ClassificationNames.load(ClassificationNames.class.getResourceAsStream("classification-names.tsv")).size());
	}

	@Test public void invalidResourcesFailClosed()
	{
		for (String invalid : new String[]{"", "# schema=2\n", "# schema=1\n0\tfoo\n",
			"# schema=1\n1\tfoo\n", "# schema=1\n0\t\n", "# schema=1\nno\tfoo\n"})
		{
			assertThrows(RuntimeException.class, () -> ClassificationNames.load(
				new ByteArrayInputStream(invalid.getBytes(StandardCharsets.UTF_8))));
		}
		assertThrows(IllegalStateException.class, () -> ClassificationNames.load(null));
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogUnavailableException;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemSetCatalogResourceTest
{
	@Test
	public void missingEmptyMalformedAndDuplicateRowsFailOnUse()
	{
		for (String text : List.of("", "# schema=1\n", "# schema=2\n",
			"# schema=1\nbad row", "# schema=1\ngear\tset\tName\t0\t0",
			"# schema=1\ngear\tset\tName\t0\t1\ngear\tset\tName\t1\t1"))
		{
			RequiredResource<?> resource = new RequiredResource<>("item set", () -> ItemSetCatalog.load(
				new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
			CatalogUnavailableException failure = assertThrows(CatalogUnavailableException.class, resource::get);
			assertSame(failure, assertThrows(CatalogUnavailableException.class, resource::get));
		}
		RequiredResource<?> missing = new RequiredResource<>("item set", () -> ItemSetCatalog.load(null));
		assertThrows(CatalogUnavailableException.class, missing::get);
	}
}

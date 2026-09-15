package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class RequiredResourceTest
{
	@Test
	public void loadingIsDeferredAndSuccessIsReused()
	{
		AtomicInteger calls = new AtomicInteger();
		Object value = new Object();
		RequiredResource<Object> resource = new RequiredResource<>("test", () ->
		{
			calls.incrementAndGet();
			return value;
		});
		assertEquals(0, calls.get());
		assertSame(value, resource.get());
		assertSame(value, resource.get());
		assertEquals(1, calls.get());
	}

	@Test
	public void failureIsDeferredAndRepeatedUseKeepsOriginalCause()
	{
		AtomicInteger calls = new AtomicInteger();
		RuntimeException cause = new IllegalStateException("broken data");
		RequiredResource<Object> resource = new RequiredResource<>("test", () ->
		{
			calls.incrementAndGet();
			throw cause;
		});
		assertEquals(0, calls.get());
		CatalogUnavailableException first = assertThrows(CatalogUnavailableException.class, resource::get);
		assertSame(cause, first.getCause());
		assertTrue(first.getMessage().contains("test data"));
		assertSame(first, assertThrows(CatalogUnavailableException.class, resource::get));
		assertEquals(1, calls.get());
		RequiredResource<Object> dependent = new RequiredResource<>("dependent", resource::get);
		assertSame(first, assertThrows(CatalogUnavailableException.class, dependent::get));
	}

	@Test
	public void actualCatalogParsersRejectMissingEmptyAndMalformedResources()
	{
		for (String input : List.of("", "# schema=1\n", "bad row"))
		{
			assertBroken(() -> GearTierCatalog.load(stream(input)));
			assertBroken(() -> WikiItemCategories.load(stream(input)));
			assertBroken(() -> ResourceItemRegistry.loadItems(stream(input), CanonicalItemClassificationOverrides.INSTANCE));
			assertBroken(() -> ResourceItemSortMetadataCatalog.loadMetadata(stream(input), Set.of()));
			assertBroken(() -> ResourceItemSortMetadataCatalog.loadSourceKeys(stream(input)));
		}
		assertBroken(() -> GearTierCatalog.load(null));
		assertBroken(() -> WikiItemCategories.load(null));
		assertBroken(() -> WikiItemLists.loadNames(null));
		assertBroken(() -> ResourceItemRegistry.loadItems(null, CanonicalItemClassificationOverrides.INSTANCE));
		assertBroken(() -> ResourceItemSortMetadataCatalog.loadMetadata(null, Set.of()));
		assertBroken(() -> ResourceItemSortMetadataCatalog.loadSourceKeys(null));
		assertBroken(() -> WikiItemLists.loadNames(stream("# comments only\n")));
		assertBroken(() -> WikiItemLists.loadNames(stream("name\textra column")));
	}

	@Test
	public void actualParsersRejectInvalidAndDuplicateRowsAfterValidData()
	{
		for (String row : List.of("1\t1\tName", "0\t1\tName", "2\t6\tName", "2\t1\t", "2\t1\tName\textra"))
		{
			assertBroken(() -> GearTierCatalog.load(stream("# schema=1\n1\t1\tName\n" + row)));
		}
		for (String row : List.of("1\tName\tRunes", "0\tName\tRunes", "2\t\tRunes", "2\tName\t"))
		{
			assertBroken(() -> WikiItemCategories.load(stream("1\tName\tRunes\n" + row)));
		}
		for (String row : List.of("1\tName\tUNKNOWN\tNAME", "0\tName\tUNKNOWN\tNAME", "2\t\tUNKNOWN\tNAME"))
		{
			assertBroken(() -> ResourceItemRegistry.loadItems(stream("1\tName\tUNKNOWN\tNAME\n" + row),
				CanonicalItemClassificationOverrides.INSTANCE));
		}
		assertEquals(Set.of("dragon dagger"), WikiItemLists.loadNames(stream("\uFEFF# comment\nDragon dagger\n")));
	}

	private static void assertBroken(Runnable parse)
	{
		RequiredResource<Boolean> resource = new RequiredResource<>("injected catalog", () ->
		{
			parse.run();
			return true;
		});
		CatalogUnavailableException failure = assertThrows(CatalogUnavailableException.class, resource::get);
		assertSame(failure, assertThrows(CatalogUnavailableException.class, resource::get));
	}

	private static InputStream stream(String input)
	{
		return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
	}
}

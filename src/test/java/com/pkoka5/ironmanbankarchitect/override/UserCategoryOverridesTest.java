package com.pkoka5.ironmanbankarchitect.override;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class UserCategoryOverridesTest
{
	@Test
	public void recordedCorrectionsRoundTripThroughTheStoredString()
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		overrides.put(995, "currency-utilities");
		overrides.put(4151, "combat-gear");

		String serialized = overrides.serialize();
		assertEquals("995=currency-utilities,4151=combat-gear", serialized);

		UserCategoryOverrides parsed = UserCategoryOverrides.parse(serialized);
		assertEquals(overrides, parsed);
		assertEquals(Optional.of("combat-gear"), parsed.categoryKeyFor(4151));
		assertEquals(Optional.empty(), parsed.categoryKeyFor(1));
	}

	@Test
	public void emptyAndMissingInputParseToNoCorrections()
	{
		assertTrue(UserCategoryOverrides.parse(null).isEmpty());
		assertTrue(UserCategoryOverrides.parse("").isEmpty());
		assertEquals("", new UserCategoryOverrides().serialize());
	}

	@Test
	public void damagedEntriesAreSkippedWithoutLosingTheHealthyOnes()
	{
		UserCategoryOverrides parsed = UserCategoryOverrides.parse(
			"995=currency-utilities,notanumber=combat-gear,=orphan,4151=,,0=zero,-3=negative,"
				+ "561=resources");

		assertEquals(2, parsed.size());
		assertEquals(Optional.of("currency-utilities"), parsed.categoryKeyFor(995));
		assertEquals(Optional.of("resources"), parsed.categoryKeyFor(561));
	}

	@Test
	public void whitespaceAroundStoredPairsIsTolerated()
	{
		UserCategoryOverrides parsed = UserCategoryOverrides.parse(" 995 = currency-utilities ");

		assertEquals(Optional.of("currency-utilities"), parsed.categoryKeyFor(995));
	}

	@Test
	public void aBlankKeyClearsTheCorrectionSoAutomaticClassificationApplies()
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		overrides.put(995, "currency-utilities");

		overrides.put(995, null);
		assertEquals(Optional.empty(), overrides.categoryKeyFor(995));

		overrides.put(995, "resources");
		overrides.put(995, "   ");
		assertTrue(overrides.isEmpty());
	}

	@Test
	public void separatorsInAKeyAreRejectedSoTheStoredStringStaysParseable()
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();

		assertThrows(() -> overrides.put(995, "a,b"));
		assertThrows(() -> overrides.put(995, "a=b"));
		assertThrows(() -> overrides.put(0, "resources"));
		assertTrue(overrides.isEmpty());
	}

	@Test
	public void clearAndRemoveDropRecordedCorrections()
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		overrides.put(995, "currency-utilities");
		overrides.put(4151, "combat-gear");

		overrides.remove(995);
		assertEquals(1, overrides.size());
		assertFalse(overrides.isEmpty());

		overrides.clear();
		assertTrue(overrides.isEmpty());
	}

	@Test
	public void exportedMapIsAnImmutableSnapshot()
	{
		UserCategoryOverrides overrides = new UserCategoryOverrides();
		overrides.put(995, "currency-utilities");
		Map<Integer, String> snapshot = overrides.asMap();

		overrides.put(4151, "combat-gear");

		assertEquals(1, snapshot.size());
		assertEquals("currency-utilities", snapshot.get(995));
		assertUnsupported(() -> snapshot.put(561, "resources"));
	}

	private static void assertThrows(Runnable action)
	{
		try
		{
			action.run();
		}
		catch (IllegalArgumentException expected)
		{
			return;
		}
		throw new AssertionError("expected IllegalArgumentException");
	}

	private static void assertUnsupported(Runnable action)
	{
		try
		{
			action.run();
		}
		catch (UnsupportedOperationException expected)
		{
			return;
		}
		throw new AssertionError("expected UnsupportedOperationException");
	}
}

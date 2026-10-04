package com.pkoka5.ironmanbankarchitect.organize;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BlueprintOrderProfilesTest
{
	@Test public void sanitizedLegacyNameRetainsItemOrdersAndRoutesThroughEditsAndRemoval()
	{
		String legacyName = "Raid; prep";
		String canonicalName = BankLayoutShareCode.sanitize(legacyName);
		assertEquals("Raid  prep", canonicalName);
		BlueprintItemOrders original = BlueprintItemOrders.parse(
			"v1|0:995,2347|r:995#0:0:currency:frequently-used");
		BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse(entry(legacyName, original));
		assertEquals(original.serialize(), profiles.forProfile(canonicalName).serialize());

		BlueprintItemOrders edited = profiles.forProfile(canonicalName).withTab(0, Arrays.asList(2347, 995));
		profiles.put(canonicalName, edited);
		assertEquals("Update the original record rather than making a duplicate",
			entry(legacyName, edited), profiles.serialize());
		profiles = BlueprintOrderProfiles.parse(profiles.serialize());
		assertEquals(edited.serialize(), profiles.forProfile(canonicalName).serialize());
		assertEquals(edited.serialize(), profiles.forProfile(legacyName).serialize());
		BlueprintItemOrders.Destination route = profiles.forProfile(canonicalName).destinations().get("995#0");
		assertEquals(0, route.tab);
		assertEquals("currency", route.originalTag);
		assertEquals("frequently-used", route.tag);

		BlueprintOrderProfiles cleared = BlueprintOrderProfiles.parse(profiles.serialize());
		cleared.put(canonicalName, BlueprintItemOrders.EMPTY);
		assertEquals("Clearing the canonical profile also clears its legacy record", "", cleared.serialize());
		profiles.remove(canonicalName);
		assertEquals("", profiles.serialize());
	}

	@Test public void exactCanonicalNameWinsWithoutOverwritingItsSeparateLegacyRecord()
	{
		String legacyName = "Raid; prep";
		String canonicalName = BankLayoutShareCode.sanitize(legacyName);
		BlueprintItemOrders legacy = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(995, 2347));
		BlueprintItemOrders canonical = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(1755, 952));
		BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse(
			entry(legacyName, legacy) + ";" + entry(canonicalName, canonical));
		assertEquals(canonical.serialize(), profiles.forProfile(canonicalName).serialize());
		assertEquals(legacy.serialize(), profiles.forProfile(legacyName).serialize());

		BlueprintItemOrders edited = canonical.withTab(0, Arrays.asList(952, 1755));
		profiles.put(canonicalName, edited);
		assertEquals(entry(legacyName, legacy) + ";" + entry(canonicalName, edited), profiles.serialize());
		profiles = BlueprintOrderProfiles.parse(profiles.serialize());
		assertEquals(edited.serialize(), profiles.forProfile(canonicalName).serialize());
		assertEquals(legacy.serialize(), profiles.forProfile(legacyName).serialize());
		profiles.remove(canonicalName);
		assertEquals("Removing the exact record preserves the separate legacy record",
			entry(legacyName, legacy), profiles.serialize());
	}

	private static String entry(String name, BlueprintItemOrders orders)
	{
		return Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8))
			+ "~" + orders.serialize();
	}
}

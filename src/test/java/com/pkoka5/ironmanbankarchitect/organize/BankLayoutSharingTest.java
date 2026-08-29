package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BankLayoutSharingTest
{
	private static final BankPreset PRESET = BankPresets.IRONMAN;

	@Test
	public void aSharedLayoutSurvivesTheRoundTripToAnotherPlayer()
	{
		BankLayoutPlan mine = BankLayoutPlan.defaultFor(PRESET)
			.withTagAt("food", 8)
			.withTagAt("runes", 3);

		String code = BankLayoutShareCode.encode("Maugor setup", mine);
		BankLayoutShareCode received = BankLayoutShareCode.decode(code).get();
		BankLayoutPlan theirs = BankLayoutPlan.parse(PRESET, received.getPlan());

		assertEquals("Maugor setup", received.getName());
		assertEquals(mine.getDestinations(), theirs.getDestinations());
	}

	@Test
	public void pastedTextWithSurroundingWhitespaceStillDecodes()
	{
		String code = BankLayoutShareCode.encode("Setup", BankLayoutPlan.defaultFor(PRESET));

		assertTrue(BankLayoutShareCode.decode("  " + code + "\n").isPresent());
	}

	@Test
	public void textThatIsNotAShareCodeIsRefusedRatherThanGuessedAt()
	{
		assertFalse(BankLayoutShareCode.decode(null).isPresent());
		assertFalse(BankLayoutShareCode.decode("").isPresent());
		assertFalse(BankLayoutShareCode.decode("hello there").isPresent());
		assertFalse(BankLayoutShareCode.decode("BAv1~name").isPresent());
		assertFalse(BankLayoutShareCode.decode("BAv1~name~").isPresent());
	}

	@Test
	public void aNameCannotCarryTheSeparatorsThatWouldSplitTheCode()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(PRESET);

		Optional<BankLayoutShareCode> decoded =
			BankLayoutShareCode.decode(BankLayoutShareCode.encode("od~d|na+me", plan));

		assertTrue(decoded.isPresent());
		assertEquals("od d na me", decoded.get().getName());
	}

	@Test
	public void aBlankNameBecomesSomethingTheReceiverCanRead()
	{
		assertEquals("Shared layout", BankLayoutShareCode.sanitize("   "));
		assertEquals("Shared layout", BankLayoutShareCode.sanitize(null));
	}

	@Test
	public void thereIsAlwaysABundledProfileToFallBackOn()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "");

		assertEquals(BankLayoutProfiles.DEFAULT_NAME, profiles.names().get(0));
		assertTrue(profiles.isDefaultActive());
		assertEquals("", profiles.activePlan());
	}

	@Test
	public void savingAProfileMakesItTheOneInUseAndSurvivesReload()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile("Skiller", "potions|gear");

		BankLayoutProfiles reloaded =
			BankLayoutProfiles.parse(profiles.serialize(), profiles.getActiveName());

		assertEquals("Skiller", reloaded.getActiveName());
		assertEquals("potions|gear", reloaded.activePlan());
		assertEquals(2, reloaded.names().size());
	}

	@Test
	public void thebundledProfileCannotBeOverwrittenOrRemoved()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile(BankLayoutProfiles.DEFAULT_NAME, "potions|gear");

		assertNotEquals(BankLayoutProfiles.DEFAULT_NAME, profiles.getActiveName());
		assertEquals("", profiles.planFor(BankLayoutProfiles.DEFAULT_NAME));

		BankLayoutProfiles afterRemoval = profiles.without(BankLayoutProfiles.DEFAULT_NAME);
		assertTrue(afterRemoval.names().contains(BankLayoutProfiles.DEFAULT_NAME));
	}

	@Test
	public void removingTheProfileInUseFallsBackToTheBundledOne()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile("Skiller", "potions|gear");

		BankLayoutProfiles removed = profiles.without("Skiller");

		assertTrue(removed.isDefaultActive());
		assertFalse(removed.names().contains("Skiller"));
	}

	@Test
	public void animportNeverOverwritesALayoutTheplayerAlreadyHas()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile("Skiller", "potions|gear");

		assertEquals("Skiller 2", profiles.freeName("Skiller"));
		assertEquals("Pvm", profiles.freeName("Pvm"));
	}

	@Test
	public void aMalformedStoredEntryIsSkippedWithoutLosingTheRest()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse(
			"broken;Skiller~potions|gear;alsobroken~", "Skiller");

		assertEquals(2, profiles.names().size());
		assertEquals("Skiller", profiles.getActiveName());
	}

	@Test
	public void anUnknownActiveNameFallsBackToTheBundledProfile()
	{
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "Gone");

		assertTrue(profiles.isDefaultActive());
	}
}

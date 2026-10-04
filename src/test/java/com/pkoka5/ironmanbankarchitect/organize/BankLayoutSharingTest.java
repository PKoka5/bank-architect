package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.List;
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
			BankLayoutShareCode.decode(BankLayoutShareCode.encode("od~d|na+me;setup", plan));

		assertTrue(decoded.isPresent());
		assertEquals("od d na me setup", decoded.get().getName());
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
	public void importingAMaximumLengthNamePreservesBothPlansAfterReload()
	{
		String originalName = "1234567890123456789012345678901234567890";
		String originalPlan = BankLayoutPlan.defaultFor(PRESET).withTagAt("food", 8).serialize();
		String importedPlan = BankLayoutPlan.defaultFor(PRESET).withTagAt("food", 3).serialize();
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile(originalName, originalPlan);
		String importedName = profiles.freeName(originalName);

		assertTrue(importedName.length() <= 40);
		assertNotEquals(originalName, importedName);
		assertEquals(importedName, BankLayoutShareCode.sanitize(importedName));
		assertEquals(importedName, BankLayoutProfiles.freeName(originalName, profiles.names()));
		profiles = profiles.withProfile(importedName, importedPlan);
		BankLayoutProfiles reloaded = BankLayoutProfiles.parse(profiles.serialize(), profiles.getActiveName());

		assertEquals(3, reloaded.names().size());
		assertEquals(originalPlan, reloaded.planFor(originalName));
		assertEquals(importedPlan, reloaded.activePlan());
		assertEquals(importedName, reloaded.getActiveName());
	}

	@Test
	public void twoDigitSuffixesAlsoFitAndCheckTheirFinalStoredName()
	{
		String wanted = "1234567890123456789012345678901234567890";
		List<String> existing = new ArrayList<>();
		existing.add(wanted);
		for (int suffix = 2; suffix <= 9; suffix++)
		{
			existing.add(wanted.substring(0, 38) + " " + suffix);
		}
		String freeName = BankLayoutProfiles.freeName(wanted, existing);

		assertEquals(wanted.substring(0, 37) + " 10", freeName);
		assertEquals(freeName, BankLayoutShareCode.sanitize(freeName));
		assertFalse(existing.contains(freeName));
	}

	@Test
	public void aSemicolonInAProfileNameCannotBreakItsActivePlanOnReload()
	{
		String plan = BankLayoutPlan.defaultFor(PRESET).withTagAt("gear", 8).serialize();
		String name = BankLayoutShareCode.sanitize("Raid; prep");
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "")
			.withProfile("Raid; prep", plan);
		BankLayoutProfiles reloaded = BankLayoutProfiles.parse(profiles.serialize(), profiles.getActiveName());

		assertFalse(name.contains(";"));
		assertEquals(name + " 2", BankLayoutProfiles.freeName("Raid; prep", profiles.names()));
		assertEquals(name, profiles.getActiveName());
		assertEquals(name, reloaded.getActiveName());
		assertEquals(plan, reloaded.activePlan());
		assertEquals(2, reloaded.names().size());
	}

	@Test
	public void aLegacyActiveSemicolonNameIsRecoveredWithoutLosingOtherProfiles()
	{
		String oldName = "Raid; prep";
		String plan = BankLayoutPlan.defaultFor(PRESET).withTagAt("gear", 8).serialize();
		BankLayoutProfiles recovered = BankLayoutProfiles.parse(
			"Skiller~original;" + oldName + "~" + plan + ";Bossing~other", oldName);
		String recoveredName = BankLayoutShareCode.sanitize(oldName);

		assertEquals("Raid  prep", recoveredName);
		assertEquals(recoveredName, recovered.getActiveName());
		assertEquals(plan, recovered.activePlan());
		assertEquals("original", recovered.planFor("Skiller"));
		assertEquals("other", recovered.planFor("Bossing"));
		assertEquals(4, recovered.names().size());
		BankLayoutProfiles reloaded = BankLayoutProfiles.parse(recovered.serialize(), recovered.getActiveName());
		assertEquals(recovered.names(), reloaded.names());
		assertEquals(recoveredName, reloaded.getActiveName());
		assertEquals(plan, reloaded.activePlan());
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

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MainLayoutProfilesTest
{
	private static final String MAIN_DEFAULT = "Main - All-Round";

	@Test
	public void mainHasItsOwnPermanentBundledProfile()
	{
		BankLayoutProfiles profiles = main("", "");

		assertEquals(MAIN_DEFAULT, BankLayoutProfiles.defaultName(BankPresets.MAIN));
		assertEquals(BankLayoutProfiles.DEFAULT_NAME, BankLayoutProfiles.defaultName(BankPresets.IRONMAN));
		assertEquals(MAIN_DEFAULT, profiles.getDefaultName());
		assertEquals(Arrays.asList(MAIN_DEFAULT), profiles.names());
		assertTrue(profiles.isDefaultActive());
		assertEquals("", profiles.activePlan());
	}

	@Test
	public void mainDefaultCannotBeOverwrittenOrDeleted()
	{
		BankLayoutProfiles profiles = main(MAIN_DEFAULT + "~corrupted-bundled-plan", MAIN_DEFAULT);
		assertEquals("", profiles.activePlan());
		profiles = profiles.withProfile(MAIN_DEFAULT, "my-main-plan");

		assertNotEquals(MAIN_DEFAULT, profiles.getActiveName());
		assertEquals("my-main-plan", profiles.activePlan());
		assertEquals("", profiles.planFor(MAIN_DEFAULT));
		BankLayoutProfiles afterRemoval = profiles.without(MAIN_DEFAULT);
		assertTrue(afterRemoval.names().contains(MAIN_DEFAULT));
		assertEquals(profiles.getActiveName(), afterRemoval.getActiveName());
		BankLayoutProfiles reloaded = main(afterRemoval.serialize(), afterRemoval.getActiveName());
		assertEquals(MAIN_DEFAULT, reloaded.getDefaultName());
		assertEquals("my-main-plan", reloaded.activePlan());
	}

	@Test
	public void deletingMainActiveLayoutFallsBackToMainDefault()
	{
		BankLayoutProfiles profiles = main("", "").withProfile("My bank", "main-plan");
		BankLayoutProfiles removed = profiles.without("My bank");

		assertEquals(MAIN_DEFAULT, removed.getActiveName());
		assertTrue(removed.isDefaultActive());
		assertFalse(removed.names().contains("My bank"));
		assertEquals(MAIN_DEFAULT, main(removed.serialize(), "Unknown layout").getActiveName());
	}

	@Test
	public void presetsCanSaveTheSameLayoutNameWithIndependentPlans()
	{
		BankLayoutProfiles ironman = BankLayoutProfiles.parse("", "").withProfile("My bank", "ironman-plan");
		BankLayoutProfiles main = main("", "").withProfile("My bank", "main-plan");
		ironman = BankLayoutProfiles.parse(ironman.serialize(), ironman.getActiveName());
		main = main(main.serialize(), main.getActiveName());

		assertEquals("My bank", ironman.getActiveName());
		assertEquals("My bank", main.getActiveName());
		assertEquals("ironman-plan", ironman.activePlan());
		assertEquals("main-plan", main.activePlan());
		assertEquals(Arrays.asList(BankLayoutProfiles.DEFAULT_NAME, "My bank"), ironman.names());
		assertEquals(Arrays.asList(MAIN_DEFAULT, "My bank"), main.names());
	}

	@Test
	public void legacyIronmanDefaultNameDoesNotBecomeTheMainBundledProfile()
	{
		BankLayoutProfiles main = main("", "")
			.withProfile(BankLayoutProfiles.DEFAULT_NAME, "user-created-main-plan");

		assertEquals(BankLayoutProfiles.DEFAULT_NAME, main.getActiveName());
		assertEquals("user-created-main-plan", main.activePlan());
		assertFalse(main.isDefaultActive());
		assertEquals(MAIN_DEFAULT, main.without(BankLayoutProfiles.DEFAULT_NAME).getActiveName());
	}

	private static BankLayoutProfiles main(String value, String active)
	{
		return BankLayoutProfiles.parse(value, active, MAIN_DEFAULT);
	}
}

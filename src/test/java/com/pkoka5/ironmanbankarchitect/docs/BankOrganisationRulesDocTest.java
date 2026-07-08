package com.pkoka5.ironmanbankarchitect.docs;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class BankOrganisationRulesDocTest
{
	private static final String DOC_PATH = "docs/bank-organisation-rules.md";

	@Test
	public void docFileExistsAndContainsMainRuleIds() throws IOException
	{
		String content = new String(Files.readAllBytes(Paths.get(DOC_PATH)), StandardCharsets.UTF_8);

		assertTrue(content.contains("HERB_001"));
		assertTrue(content.contains("POT_001"));
		assertTrue(content.contains("GEAR_001"));
		assertTrue(content.contains("TELE_001"));
		assertTrue(content.contains("SKILL_001"));
		assertTrue(content.contains("CLEAN_001"));
		assertTrue(content.contains("TAG_001"));
	}

	@Test
	public void docStatesRulesAreProductAndDesignRulesNotVerifiedClaims() throws IOException
	{
		String content = new String(Files.readAllBytes(Paths.get(DOC_PATH)), StandardCharsets.UTF_8);

		assertTrue(content.contains("product") && content.contains("design rules"));
		assertTrue(content.contains("YouTube") && content.contains("independently verified"));
	}

	@Test
	public void docStatesSuperAttackFourIsFuturePotionConsumablesPvmSuppliesWork() throws IOException
	{
		String content = new String(Files.readAllBytes(Paths.get(DOC_PATH)), StandardCharsets.UTF_8);

		assertTrue(content.contains("Super attack (4)"));
		assertTrue(content.contains("2436"));
		assertTrue(content.contains("Potion / Consumables / PvM Supplies"));
	}
}

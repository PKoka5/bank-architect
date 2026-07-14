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
	public void docStatesIronmanPotionSplitIsIdBackedAndExplicit() throws IOException
	{
		String content = new String(Files.readAllBytes(Paths.get(DOC_PATH)), StandardCharsets.UTF_8);

		assertTrue(content.contains("canonical item ID"));
		assertTrue(content.contains("(4) → (3) → (2) → (1)"));
		assertTrue(content.contains("dose `(4)` belongs in ready-to-use"));
		assertTrue(content.contains("`(3)`, `(2)`, and `(1)` doses join"));
		assertTrue(content.contains("Barbarian mixes"));
	}
}

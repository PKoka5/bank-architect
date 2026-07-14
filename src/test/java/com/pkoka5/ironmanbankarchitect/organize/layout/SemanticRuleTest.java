package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SemanticRuleTest
{
	@Test
	public void buildsValidRuleWithExplicitOrderedTopology()
	{
		SemanticAtom sapphire = atom("gem.sapphire", 30, 10);
		SemanticAtom emerald = atom("gem.emerald", 40, 20);
		SemanticRule rule = SemanticRule.builder()
			.ruleKey("gem.workflow")
			.atoms(Arrays.asList(sapphire, emerald))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.STAGE_MATRIX)
			.allowedWidths(widths(5, 3, 4))
			.widthEvidence(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED)
			.spilloverCompatibleRuleKeys(Collections.singleton("metal.workflow"))
			.build();

		assertEquals("gem.workflow", rule.getRuleKey());
		assertEquals(Arrays.asList(sapphire, emerald), rule.getAtoms());
		assertEquals(Arrays.asList(30, 10, 40, 20), rule.getMemberItemIds());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.STAGE_MATRIX, rule.getShapePrimitive());
		assertEquals(widths(3, 4, 5), rule.getAllowedWidths());
		assertTrue(rule.hasWidthEvidence());
		assertEquals(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED, rule.getWidthEvidence());
		assertTrue(rule.hasPreferredWidth());
		assertEquals(5, rule.getPreferredWidth());
		assertEquals(Collections.singleton("metal.workflow"), rule.getSpilloverCompatibleRuleKeys());
	}

	@Test
	public void preferredWidthIsOptional()
	{
		SemanticRule rule = validBuilder().build();

		assertFalse(rule.hasWidthEvidence());
		assertFalse(rule.hasPreferredWidth());
		try
		{
			rule.getWidthEvidence();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException expected)
		{
			// expected
		}
		try
		{
			rule.getPreferredWidth();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException expected)
		{
			// expected
		}
	}

	@Test
	public void rejectsInvalidRuleKeys()
	{
		for (String key : Arrays.asList(null, "", "Upper.Case", "spa ce", ".leading", "trailing-"))
		{
			assertBuildFails(validBuilder().ruleKey(key));
		}
	}

	@Test
	public void rejectsInvalidAtomTopology()
	{
		assertBuildFails(validBuilder().atoms(null));
		assertBuildFails(validBuilder().atoms(Collections.emptyList()));
		assertBuildFails(validBuilder().atoms(Arrays.asList(atom("family.one", 10), null)));
		assertBuildFails(validBuilder().atoms(Arrays.asList(atom("family.one", 10), atom("family.one", 20))));
		assertBuildFails(validBuilder().atoms(Arrays.asList(atom("family.one", 10), atom("family.two", 10))));
	}

	@Test
	public void rejectsWidthsOutsideOneToEight()
	{
		assertBuildFails(validBuilder().allowedWidths(null));
		assertBuildFails(validBuilder().allowedWidths(Collections.emptySet()));
		assertBuildFails(validBuilder().allowedWidths(widths(0, 3)));
		assertBuildFails(validBuilder().allowedWidths(widths(3, 9)));
		assertBuildFails(validBuilder().allowedWidths(widths(-1)));
	}

	@Test
	public void acceptsEveryWidthFromOneToEight()
	{
		SemanticRule rule = validBuilder().allowedWidths(widths(1, 2, 3, 4, 5, 6, 7, 8)).build();

		assertEquals(8, rule.getAllowedWidths().size());
	}

	@Test
	public void rejectsPreferredWidthOutsideAllowedWidths()
	{
		assertBuildFails(validBuilder().allowedWidths(widths(4, 5))
			.widthEvidence(SemanticWidthEvidenceFacts.HERB_WORKFLOW));
	}

	@Test
	public void verticalRunRejectsAnActivePreferenceForAnImpossibleWidth()
	{
		assertBuildFails(validBuilder()
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(widths(1, 5))
			.widthEvidence(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED));

		WidthEvidence widthOne = new WidthEvidence(7,
			Arrays.asList(5, 2, 0, 0, 0, 0, 0, 0),
			Arrays.asList(5, 2, 0, 0, 0, 0, 0, 0));
		SemanticRule vertical = validBuilder()
			.shapePrimitive(ShapePrimitive.VERTICAL_RUN)
			.allowedWidths(widths(1))
			.widthEvidence(widthOne)
			.build();
		assertEquals(1, vertical.getPreferredWidth());
	}

	@Test
	public void rejectsInvalidSpilloverCompatibleKeys()
	{
		assertBuildFails(validBuilder().spilloverCompatibleRuleKeys(Collections.singleton("Bad Key")));
	}

	@Test
	public void collectionsAreImmutableAndDetachedFromInput()
	{
		List<SemanticAtom> atoms = new ArrayList<>(Arrays.asList(atom("family.one", 10, 20)));
		Set<Integer> allowed = widths(2, 3);
		SemanticRule rule = validBuilder().atoms(atoms).allowedWidths(allowed).build();

		atoms.add(atom("family.two", 30));
		allowed.add(4);
		assertEquals(1, rule.getAtoms().size());
		assertEquals(widths(2, 3), rule.getAllowedWidths());

		assertImmutable(rule.getAtoms());
		assertImmutable(rule.getMemberItemIds());
		try
		{
			rule.getAllowedWidths().add(6);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	private static SemanticRule.Builder validBuilder()
	{
		return SemanticRule.builder()
			.ruleKey("herb.workflow")
			.atoms(Arrays.asList(atom("herb.guam", 10, 20)))
			.confidenceTier(ConfidenceTier.MEDIUM)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(widths(2, 3));
	}

	private static SemanticAtom atom(String key, int... itemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			members.add(new SemanticAtom.Member("member." + index, itemIds[index]));
		}
		return new SemanticAtom(key, members);
	}

	private static Set<Integer> widths(int... values)
	{
		Set<Integer> result = new LinkedHashSet<>();
		for (int value : values)
		{
			result.add(value);
		}
		return result;
	}

	private static void assertBuildFails(SemanticRule.Builder builder)
	{
		try
		{
			builder.build();
			fail("expected construction failure");
		}
		catch (IllegalArgumentException | NullPointerException expected)
		{
			// expected
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void assertImmutable(List<?> values)
	{
		try
		{
			((List) values).add(null);
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}
}

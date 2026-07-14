package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * One immutable V1 semantic rule: a stable key, an explicit ordered semantic topology of
 * {@link SemanticAtom}s, a confidence tier, exactly one shape primitive, the allowed widths, an
 * optional evidence-backed width preference, and the rule keys whose spillover is explicitly
 * compatible.
 *
 * <p>The atoms carry the real structure — families, stage columns, or explicit row groups
 * depending on the primitive. {@link #getMemberItemIds()} is only a derived flat ID view used for
 * cross-rule overlap validation.</p>
 *
 * <p>Rules are curated in code, so malformed rules fail fast with
 * {@link IllegalArgumentException}: keys must be stable lowercase keys, atom keys unique within
 * the rule, item IDs positive and unique across all atoms, and Bank Filler is excluded by
 * {@link SemanticAtom.Member} itself. Cross-rule properties (duplicate keys, overlapping item
 * sets) are request-level typed conflicts reported by {@link LayoutRequestValidator}.</p>
 */
public final class SemanticRule
{
	static final int MIN_WIDTH = 1;
	static final int MAX_WIDTH = 8;

	private static final Pattern KEY_PATTERN = Pattern.compile("[a-z0-9]+(?:[._-][a-z0-9]+)*");

	private final String ruleKey;
	private final List<SemanticAtom> atoms;
	private final List<Integer> memberItemIds;
	private final ConfidenceTier confidenceTier;
	private final ShapePrimitive shapePrimitive;
	private final Set<Integer> allowedWidths;
	private final WidthEvidence widthEvidence;
	private final Set<String> spilloverCompatibleRuleKeys;

	private SemanticRule(Builder builder)
	{
		this.ruleKey = requireRuleKey(builder.ruleKey, "ruleKey");
		this.atoms = requireAtoms(builder.atoms);
		this.memberItemIds = flattenUniqueItemIds(this.atoms);
		this.confidenceTier = Objects.requireNonNull(builder.confidenceTier, "confidenceTier");
		this.shapePrimitive = Objects.requireNonNull(builder.shapePrimitive, "shapePrimitive");
		this.allowedWidths = requireAllowedWidths(builder.allowedWidths);
		this.widthEvidence = builder.widthEvidence;
		this.spilloverCompatibleRuleKeys = requireCompatibleKeys(builder.spilloverCompatibleRuleKeys);

		if (hasPreferredWidth() && !allowedWidths.contains(getPreferredWidth()))
		{
			throw new IllegalArgumentException("preferredWidth must be one of the allowed widths");
		}
		if (shapePrimitive == ShapePrimitive.VERTICAL_RUN && hasPreferredWidth()
			&& getPreferredWidth() != 1)
		{
			throw new IllegalArgumentException(
				"vertical-run width evidence may only prefer its structurally feasible width 1");
		}
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public String getRuleKey()
	{
		return ruleKey;
	}

	/**
	 * The explicit ordered semantic topology: families for runs, family columns for a stage
	 * matrix, or explicit rows for a row-group matrix.
	 */
	public List<SemanticAtom> getAtoms()
	{
		return atoms;
	}

	/**
	 * Derived flat ID view over all atoms in atom/member order. Used for cross-rule overlap
	 * validation only; the semantic structure lives in {@link #getAtoms()}.
	 */
	public List<Integer> getMemberItemIds()
	{
		return memberItemIds;
	}

	public ConfidenceTier getConfidenceTier()
	{
		return confidenceTier;
	}

	public ShapePrimitive getShapePrimitive()
	{
		return shapePrimitive;
	}

	public Set<Integer> getAllowedWidths()
	{
		return allowedWidths;
	}

	public boolean hasWidthEvidence()
	{
		return widthEvidence != null;
	}

	public WidthEvidence getWidthEvidence()
	{
		if (widthEvidence == null)
		{
			throw new IllegalStateException("rule has no width evidence");
		}
		return widthEvidence;
	}

	public boolean hasPreferredWidth()
	{
		return widthEvidence != null && widthEvidence.hasPreferredWidth();
	}

	public int getPreferredWidth()
	{
		if (!hasPreferredWidth())
		{
			throw new IllegalStateException("rule has no evidence-backed width preference");
		}

		return widthEvidence.getPreferredWidth();
	}

	public Set<String> getSpilloverCompatibleRuleKeys()
	{
		return spilloverCompatibleRuleKeys;
	}

	static String requireRuleKey(String value, String field)
	{
		if (value == null || !KEY_PATTERN.matcher(value).matches())
		{
			throw new IllegalArgumentException(field + " must be a lowercase stable key");
		}

		return value;
	}

	private static List<SemanticAtom> requireAtoms(List<SemanticAtom> atoms)
	{
		if (atoms == null || atoms.isEmpty())
		{
			throw new IllegalArgumentException("atoms must not be empty");
		}

		Set<String> seenAtomKeys = new HashSet<>();
		for (SemanticAtom atom : atoms)
		{
			if (atom == null)
			{
				throw new IllegalArgumentException("atoms must not contain null");
			}
			if (!seenAtomKeys.add(atom.getAtomKey()))
			{
				throw new IllegalArgumentException("duplicate atom key " + atom.getAtomKey());
			}
		}

		return Collections.unmodifiableList(new ArrayList<>(atoms));
	}

	private static List<Integer> flattenUniqueItemIds(List<SemanticAtom> atoms)
	{
		List<Integer> itemIds = new ArrayList<>();
		Set<Integer> seen = new HashSet<>();
		for (SemanticAtom atom : atoms)
		{
			for (SemanticAtom.Member member : atom.getMembers())
			{
				if (!seen.add(member.getItemId()))
				{
					throw new IllegalArgumentException(
						"item ID " + member.getItemId() + " appears in more than one atom");
				}
				itemIds.add(member.getItemId());
			}
		}

		return Collections.unmodifiableList(itemIds);
	}

	private static Set<Integer> requireAllowedWidths(Set<Integer> widths)
	{
		if (widths == null || widths.isEmpty())
		{
			throw new IllegalArgumentException("allowedWidths must not be empty");
		}

		for (Integer width : widths)
		{
			if (width == null || width < MIN_WIDTH || width > MAX_WIDTH)
			{
				throw new IllegalArgumentException("allowedWidths must be within " + MIN_WIDTH + ".." + MAX_WIDTH);
			}
		}

		return Collections.unmodifiableSet(new TreeSet<>(widths));
	}

	private static Set<String> requireCompatibleKeys(Set<String> keys)
	{
		if (keys == null || keys.isEmpty())
		{
			return Collections.emptySet();
		}

		Set<String> validated = new TreeSet<>();
		for (String key : keys)
		{
			validated.add(requireRuleKey(key, "spilloverCompatibleRuleKeys"));
		}

		return Collections.unmodifiableSet(validated);
	}

	public static final class Builder
	{
		private String ruleKey;
		private List<SemanticAtom> atoms;
		private ConfidenceTier confidenceTier;
		private ShapePrimitive shapePrimitive;
		private Set<Integer> allowedWidths;
		private WidthEvidence widthEvidence;
		private Set<String> spilloverCompatibleRuleKeys;

		private Builder()
		{
		}

		public Builder ruleKey(String value)
		{
			this.ruleKey = value;
			return this;
		}

		public Builder atoms(List<SemanticAtom> value)
		{
			this.atoms = value;
			return this;
		}

		public Builder confidenceTier(ConfidenceTier value)
		{
			this.confidenceTier = value;
			return this;
		}

		public Builder shapePrimitive(ShapePrimitive value)
		{
			this.shapePrimitive = value;
			return this;
		}

		public Builder allowedWidths(Set<Integer> value)
		{
			this.allowedWidths = value;
			return this;
		}

		public Builder widthEvidence(WidthEvidence value)
		{
			this.widthEvidence = Objects.requireNonNull(value, "widthEvidence");
			return this;
		}

		public Builder spilloverCompatibleRuleKeys(Set<String> value)
		{
			this.spilloverCompatibleRuleKeys = value;
			return this;
		}

		public SemanticRule build()
		{
			return new SemanticRule(this);
		}
	}
}

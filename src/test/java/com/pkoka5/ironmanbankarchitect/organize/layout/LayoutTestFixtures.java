package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Test-only construction of real generated groups and candidates. Even comparator tests therefore
 * cannot bypass canonical geometry or forge a placed block's evidence rank.
 */
final class LayoutTestFixtures
{
	private LayoutTestFixtures()
	{
	}

	static PlacedBlock placedBlock(String ruleKey, String atomKey, int expectedPreferenceRank,
		int width, ShapePrimitive primitive, int startRow, int startColumn,
		LayoutCandidate.Row... rows)
	{
		LayoutCandidateGroup group = candidateGroup(ruleKey, atomKey, expectedPreferenceRank,
			width, primitive, rows);
		LayoutCandidate candidate = candidateAtWidth(group, width);
		PlacedBlock block = PlacedBlock.place(group, candidate, startRow, startColumn);
		if (block.getWidthPreferenceRank() != expectedPreferenceRank)
		{
			throw new IllegalArgumentException("fixture evidence did not produce the expected rank");
		}
		return block;
	}

	static LayoutCandidateGroup candidateGroup(String ruleKey, String atomKey,
		int expectedPreferenceRank, int width, ShapePrimitive primitive,
		LayoutCandidate.Row... rows)
	{
		if (rows == null || rows.length == 0)
		{
			throw new IllegalArgumentException("fixture rows must not be empty");
		}

		List<SemanticAtom> atoms = atoms(atomKey, primitive, rows);
		Set<Integer> allowedWidths = new LinkedHashSet<>();
		allowedWidths.add(width);
		SemanticRule.Builder builder = SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(primitive);
		WidthEvidence evidence = evidenceForRank(expectedPreferenceRank, width, allowedWidths);
		builder.allowedWidths(allowedWidths);
		if (evidence != null)
		{
			builder.widthEvidence(evidence);
		}

		List<LayoutEntry> entries = new ArrayList<>();
		int sourceIndex = 0;
		for (LayoutCandidate.Row row : rows)
		{
			for (Integer itemId : row.getItemIds())
			{
				entries.add(LayoutEntry.of(
					new BankPreviewItem(itemId, "Item " + itemId, 1), sourceIndex++));
			}
		}

		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(
			new LayoutRequest(entries, Collections.singletonList(builder.build())));
		if (groups.size() != 1)
		{
			throw new IllegalArgumentException("fixture must produce exactly one candidate group");
		}
		return groups.get(0);
	}

	static LayoutCandidate candidateAtWidth(LayoutCandidateGroup group, int width)
	{
		for (LayoutCandidate candidate : group.getCandidates())
		{
			if (candidate.getWidth() == width)
			{
				return candidate;
			}
		}
		throw new IllegalArgumentException("fixture width did not produce a feasible candidate");
	}

	private static List<SemanticAtom> atoms(String atomKey, ShapePrimitive primitive,
		LayoutCandidate.Row[] rows)
	{
		if (primitive == ShapePrimitive.HORIZONTAL_RUN)
		{
			if (rows.length != 1 || rows[0].getStartOffset() != 0)
			{
				throw new IllegalArgumentException("horizontal fixture requires one zero-offset row");
			}
			return Collections.singletonList(atom(atomKey, rows[0].getItemIds()));
		}
		if (primitive == ShapePrimitive.VERTICAL_RUN)
		{
			List<Integer> itemIds = new ArrayList<>();
			for (LayoutCandidate.Row row : rows)
			{
				if (row.getStartOffset() != 0 || row.length() != 1)
				{
					throw new IllegalArgumentException(
						"vertical fixture requires zero-offset singleton rows");
				}
				itemIds.add(row.getItemIds().get(0));
			}
			return Collections.singletonList(atom(atomKey, itemIds));
		}
		if (primitive == ShapePrimitive.ROW_GROUP_MATRIX)
		{
			List<SemanticAtom> result = new ArrayList<>();
			for (int index = 0; index < rows.length; index++)
			{
				if (rows[index].getStartOffset() != 0)
				{
					throw new IllegalArgumentException("row-group fixture rows must have zero offset");
				}
				String key = rows.length == 1 ? atomKey : atomKey + ".row" + index;
				result.add(atom(key, rows[index].getItemIds()));
			}
			return result;
		}

		throw new IllegalArgumentException("test fixture does not synthesize stage matrices");
	}

	private static SemanticAtom atom(String atomKey, List<Integer> itemIds)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		for (int index = 0; index < itemIds.size(); index++)
		{
			members.add(new SemanticAtom.Member("member." + index, itemIds.get(index)));
		}
		return new SemanticAtom(atomKey, members);
	}

	private static WidthEvidence evidenceForRank(int rank, int width, Set<Integer> allowedWidths)
	{
		if (rank == 0)
		{
			return null;
		}
		if (rank < 0 || rank > 2)
		{
			throw new IllegalArgumentException("fixture supports preference ranks zero through two");
		}

		int preferredWidth = width == SemanticRule.MAX_WIDTH ? width - 1 : width + 1;
		allowedWidths.add(preferredWidth);
		Integer[] templateSupport = new Integer[SemanticRule.MAX_WIDTH];
		Integer[] familySupport = new Integer[SemanticRule.MAX_WIDTH];
		Arrays.fill(templateSupport, 0);
		Arrays.fill(familySupport, 0);
		templateSupport[preferredWidth - 1] = 5;
		familySupport[preferredWidth - 1] = 5;
		if (rank == 1)
		{
			templateSupport[width - 1] = 3;
			familySupport[width - 1] = 3;
		}
		else
		{
			int middleWidth = firstOtherWidth(width, preferredWidth);
			templateSupport[middleWidth - 1] = 2;
			familySupport[middleWidth - 1] = 2;
		}
		return new WidthEvidence(7, Arrays.asList(templateSupport), Arrays.asList(familySupport));
	}

	private static int firstOtherWidth(int first, int second)
	{
		for (int width = SemanticRule.MIN_WIDTH; width <= SemanticRule.MAX_WIDTH; width++)
		{
			if (width != first && width != second)
			{
				return width;
			}
		}
		throw new IllegalStateException("no third width available");
	}
}

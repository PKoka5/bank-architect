package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Test;

public class LayoutProjectionGroupingTest
{
	@Test
	public void horizontalFamiliesWithMatchingMemberKeysStaySeparateInReviewedOrder()
	{
		assertSeparateRunFamilies(ShapePrimitive.HORIZONTAL_RUN);
	}

	@Test
	public void verticalFamiliesWithMatchingMemberKeysStaySeparateInReviewedOrder()
	{
		assertSeparateRunFamilies(ShapePrimitive.VERTICAL_RUN);
	}

	private static void assertSeparateRunFamilies(ShapePrimitive primitive)
	{
		SemanticRule rule = SemanticRule.builder()
			.ruleKey("run.separate")
			.atoms(Arrays.asList(atom("family.z", 10, 11), atom("family.a", 20, 21)))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(primitive)
			.allowedWidths(new LinkedHashSet<>(Arrays.asList(2, 1)))
			.build();
		List<LayoutEntry> entries = new ArrayList<>();
		for (Integer itemId : Arrays.asList(21, 11, 20, 10))
		{
			entries.add(LayoutEntry.of(new BankPreviewItem(itemId, "Item " + itemId, 1), entries.size()));
		}

		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(
			new LayoutRequest(entries, Collections.singletonList(rule)));

		assertEquals(2, groups.size());
		assertFamily(groups.get(0), "family.z", Arrays.asList(10, 11));
		assertFamily(groups.get(1), "family.a", Arrays.asList(20, 21));
	}

	private static SemanticAtom atom(String key, int firstItemId, int secondItemId)
	{
		return new SemanticAtom(key, Arrays.asList(
			new SemanticAtom.Member("first", firstItemId),
			new SemanticAtom.Member("second", secondItemId)));
	}

	private static void assertFamily(LayoutCandidateGroup group, String atomKey, List<Integer> itemIds)
	{
		assertEquals(Collections.singletonList(atomKey), group.getAtomKeys());
		assertEquals(1, group.getAtomProjections().size());
		assertEquals(itemIds, group.getProjectedItemIds());
		assertEquals(1, group.getCandidates().size());
		assertEquals(itemIds, group.getCandidates().get(0).getRowMajorItemIds());
	}
}

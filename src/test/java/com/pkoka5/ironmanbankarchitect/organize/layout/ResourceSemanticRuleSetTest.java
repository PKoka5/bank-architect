package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ResourceSemanticRuleSetTest
{
	private static final List<Integer> METAL_MATRIX_ORES =
		Arrays.asList(440, 442, 444, 447, 449, 451);
	private static final List<Integer> METAL_MATRIX_BARS =
		Arrays.asList(2351, 2355, 2357, 2359, 2361, 2363);
	private static final List<Integer> GEM_RAW = Arrays.asList(1623, 1621, 1619, 1617, 1631);
	private static final List<Integer> GEM_PROCESSED = Arrays.asList(1607, 1605, 1603, 1601, 1615);
	private static final List<Integer> NORMAL_LOGS =
		Arrays.asList(1511, 1521, 1519, 1517, 1515, 1513, 19669);
	private static final List<Integer> CONSTRUCTION_LOGS =
		Arrays.asList(6333, 6332, 32904, 32907, 32910);
	private static final List<Integer> SPECIAL_WOOD = Arrays.asList(10810, 24691, 22935, 28134);
	private static final List<Integer> PLANKS =
		Arrays.asList(960, 8778, 8780, 8782, 31432, 31435, 31438);
	private static final List<Integer> JEWELLERY_RINGS =
		Arrays.asList(1635, 1637, 1639, 1641, 1643, 1645, 6575, 19538);
	private static final List<Integer> GLASS_WORKFLOW =
		Arrays.asList(21504, 401, 1781, 1783, 1775, 4542, 567, 229);
	private static final List<Integer> TEXTILE_INPUTS = Arrays.asList(1779, 1759, 1734, 5931);
	private static final List<Integer> TEXTILE_FABRICS =
		Arrays.asList(31475, 8790, 31472, 31746, 31758, 31734);
	private static final List<Integer> NAILS = Arrays.asList(4819, 4820, 1539, 4822, 4823, 4824);
	private static final List<Integer> ARROW_PRODUCTION = Arrays.asList(52, 314, 53);
	private static final List<Integer> ARROWTIPS =
		Arrays.asList(39, 40, 41, 42, 43, 44, 21350, 11237);
	private static final List<Integer> DART_TIPS = Arrays.asList(819, 820, 821, 822, 823, 824, 25853);
	private static final List<Integer> CROSSBOW_LIMBS =
		Arrays.asList(9420, 9422, 9423, 9425, 9427, 9429, 9431);
	private static final List<Integer> UNSTRUNG_SHORTBOWS = Arrays.asList(50, 54, 60, 64, 68, 72);
	private static final List<Integer> UNSTRUNG_LONGBOWS = Arrays.asList(48, 56, 58, 62, 66, 70);

	@Test
	public void canonicalRulesUseExactReviewedFamiliesStagesAndEvidence()
	{
		LayoutRequest request = ResourceSemanticRuleSet.forEntries(Collections.emptyList());

		assertFalse(request.hasCurrentDenseCategoryOrder());
		assertEquals(5, request.getRules().size());
		assertMetalRows(request.getRules().get(0));
		assertFalse(request.getRules().get(0).hasWidthEvidence());

		assertRule(request.getRules().get(1), "resource.gem.raw-processed",
			Arrays.asList("gem.sapphire", "gem.emerald", "gem.ruby", "gem.diamond",
				"gem.dragonstone"), GEM_RAW, GEM_PROCESSED);
		assertSame(SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED,
			request.getRules().get(1).getWidthEvidence());
		assertEquals(5, request.getRules().get(1).getPreferredWidth());

		assertWoodRows(request.getRules().get(2));
		assertCraftingRows(request.getRules().get(3));
		assertFletchingRuns(request.getRules().get(4));
	}

	@Test
	public void completeArrowtipTiersFormOneEightWideHorizontalRun()
	{
		List<Integer> input = new ArrayList<>(ARROWTIPS);
		Collections.reverse(input);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);

		assertEquals(ShapePrimitive.HORIZONTAL_RUN, onlyBlock(outcome).getShapePrimitive());
		assertEquals(8, onlyBlock(outcome).getWidth());
		assertEquals(ARROWTIPS, targetOrder(outcome));
	}

	@Test
	public void shortAndLongBowTiersRemainSeparateOrderedHorizontalRuns()
	{
		List<Integer> input = new ArrayList<>();
		input.addAll(UNSTRUNG_LONGBOWS);
		input.addAll(UNSTRUNG_SHORTBOWS);
		input.addAll(fillers(980000, 4));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertEquals(2, outcome.getTieKey().getBlocks().size());
		assertHorizontalFamily(target, UNSTRUNG_LONGBOWS);
		assertHorizontalFamily(target, UNSTRUNG_SHORTBOWS);
		assertDensePermutation(input, target);
	}

	@Test
	public void singletonFletchingFamiliesFallBackWithoutInventingMissingTiers()
	{
		List<Integer> input = Arrays.asList(39, 50, 48, 990001);
		List<Integer> fallback = Arrays.asList(990001, 48, 39, 50);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, fallback);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, targetOrder(outcome));
	}

	@Test
	public void woodUsesSeparateTierRowsInsteadOfAlternatingLogsAndPlanks()
	{
		List<Integer> input = new ArrayList<>();
		input.addAll(NORMAL_LOGS);
		input.addAll(CONSTRUCTION_LOGS);
		input.addAll(SPECIAL_WOOD);
		input.addAll(PLANKS);
		input.addAll(fillers(960000, 10));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertEquals(7, onlyBlock(outcome).getWidth());
		assertEquals(ShapePrimitive.ROW_GROUP_MATRIX, onlyBlock(outcome).getShapePrimitive());
		assertEquals(NORMAL_LOGS, target.subList(0, 7));
		assertEquals(CONSTRUCTION_LOGS, target.subList(8, 13));
		assertEquals(SPECIAL_WOOD, target.subList(16, 20));
		assertEquals(PLANKS, target.subList(24, 31));
		assertDensePermutation(input, target);
	}

	@Test
	public void userLikeWoodSubsetKeepsOwnedRowsAndNeverInventsMissingTiers()
	{
		List<Integer> normal = Arrays.asList(1511, 1521, 1517, 1515, 1513, 19669);
		List<Integer> construction = Arrays.asList(6333, 6332, 32904, 32907);
		List<Integer> other = Arrays.asList(24691, 22935, 28134);
		List<Integer> planks = Arrays.asList(960, 8778, 8780, 8782, 31435);
		List<Integer> input = new ArrayList<>();
		input.addAll(planks);
		input.addAll(other);
		input.addAll(construction);
		input.addAll(normal);
		input.addAll(fillers(970000, 13));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		assertEquals(outcome.getTieKey().getBlocks().toString(), 1,
			outcome.getTieKey().getBlocks().size());
		List<Integer> target = targetOrder(outcome);

		assertEquals(normal, target.subList(0, 6));
		assertEquals(construction, target.subList(8, 12));
		assertEquals(other, target.subList(16, 19));
		assertEquals(planks, target.subList(24, 29));
		assertFalse(target.contains(1519));
		assertFalse(target.contains(32910));
		assertFalse(target.contains(31432));
		assertDensePermutation(input, target);
	}

	@Test
	public void craftingRowsKeepJewelleryGlassAndTextilesSeparate()
	{
		List<Integer> input = new ArrayList<>();
		input.addAll(TEXTILE_FABRICS);
		input.addAll(GLASS_WORKFLOW);
		input.addAll(JEWELLERY_RINGS);
		input.addAll(TEXTILE_INPUTS);
		input.addAll(fillers(971000, 6));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertHorizontalFamily(target, JEWELLERY_RINGS);
		assertHorizontalFamily(target, GLASS_WORKFLOW);
		assertHorizontalFamily(target, TEXTILE_INPUTS);
		assertHorizontalFamily(target, TEXTILE_FABRICS);
		assertDensePermutation(input, target);
	}

	@Test
	public void standardConstructionNailsFormOneOrderedHorizontalTierRun()
	{
		List<Integer> input = new ArrayList<>(NAILS);
		Collections.reverse(input);
		input.addAll(fillers(971100, 5));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertHorizontalFamily(target, NAILS);
		assertDensePermutation(input, target);
	}

	@Test
	public void standardConstructionNailsStayContiguousAmongOtherConstructionMaterials()
	{
		List<Integer> input = Arrays.asList(
			434, 4824, 1761, 4822, 3420, 1539, 4823, 3424, 4820, 5016, 4819);

		List<Integer> target = targetOrder(planDetailed(input, input));

		assertHorizontalFamily(target, NAILS);
		assertDensePermutation(input, target);
	}

	@Test
	public void extendedFletchingFamiliesFormIndependentTierRuns()
	{
		List<Integer> input = new ArrayList<>();
		input.addAll(CROSSBOW_LIMBS);
		input.addAll(DART_TIPS);
		input.addAll(ARROW_PRODUCTION);
		input.addAll(fillers(972000, 7));

		List<Integer> target = targetOrder(planDetailed(input, input));

		assertHorizontalFamily(target, ARROW_PRODUCTION);
		assertHorizontalFamily(target, DART_TIPS);
		assertHorizontalFamily(target, CROSSBOW_LIMBS);
		assertDensePermutation(input, target);
	}

	@Test
	public void requestPreservesArbitrarySourceSlotsWithoutInventingDenseRanks()
	{
		List<LayoutEntry> entries = Arrays.asList(entry(440, 87), entry(2351, 3), entry(900001, 412));

		LayoutRequest request = ResourceSemanticRuleSet.forEntries(entries);

		assertFalse(request.hasCurrentDenseCategoryOrder());
		assertEquals(Arrays.asList(87, 3, 412), sourceSlots(request.getEntries()));
		for (LayoutEntry entry : request.getEntries())
		{
			assertFalse(entry.hasDenseCategoryRank());
			assertFalse(entry.hasLockedTarget());
		}
	}

	@Test
	public void completeMetalFamiliesAlignRawAndProcessedStagesByPhysicalColumn()
	{
		List<Integer> fallback = Arrays.asList(
			436, 440, 453, 442, 444, 447, 449, 451,
			2351, 2353, 2355, 2357, 2359, 2361, 2363);

		BoundedLayoutPacker.Outcome outcome = planDetailed(fallback, fallback);
		List<Integer> target = targetOrder(outcome);
		assertEquals(6, onlyBlock(outcome).getWidth());
		assertEquals(ShapePrimitive.STAGE_MATRIX, onlyBlock(outcome).getShapePrimitive());
		assertMetalColumns(target);
		assertDensePermutation(fallback, target);
	}

	@Test
	public void supplementalMaterialPathKeepsTheSameMetalStageColumns()
	{
		List<Integer> input = Arrays.asList(
			436, 440, 453, 442, 444, 447, 449, 451,
			2351, 2353, 2355, 2357, 2359, 2361, 2363,
			22603); // Basalt forces the presentMaterialRule path.

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertTrue(outcome.getTieKey().getBlocks().stream()
			.anyMatch(block -> block.getShapePrimitive() == ShapePrimitive.STAGE_MATRIX
				&& block.getWidth() == 6));
		assertMetalColumns(target);
		assertDensePermutation(input, target);
	}

	@Test
	public void globalMiningPrefixStillAllowsTheFollowingWoodMatrix()
	{
		List<Integer> fallback = Arrays.asList(
			436, 440, 453, 442, 444, 447, 449, 451,
			2351, 2353, 2355, 2357, 2359, 2361, 31719, 32892,
			22603, 21543, 13573, 21545, 13421, 21622,
			1511, 1521, 6333, 1517, 6332, 1515, 1513, 19669,
			960, 8778, 8780, 8782, 31435, 24691, 32904,
			907000, 907001, 907002, 907003, 907004, 907005, 907006, 907007, 907008, 907009,
			907010);
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < fallback.size(); index++)
		{
			entries.add(entry(fallback.get(index), 1000 + index * 7));
		}
		LayoutRequest request = ResourceSemanticRuleSet.forEntries(entries);
		List<LayoutCandidateGroup> groups = LayoutCandidateGenerator.generate(request);
		LayoutEntry anchor = request.getEntries().stream()
			.filter(value -> value.getItem().getItemId() == 440)
			.findFirst().orElseThrow(AssertionError::new);
		assertTrue(anchor.hasLockedTarget());
		assertEquals(0, anchor.getLockedTarget());
		BoundedLayoutPacker.Outcome outcome = SemanticBlockLayoutEngine.planDetailed(
			request, fallback, BoundedLayoutPacker.Limits.production());
		List<Integer> target = targetOrder(outcome);

		assertTrue("groups=" + groups + ", score=" + outcome.getScore(),
			outcome.getTieKey().getBlocks().stream()
				.anyMatch(block -> block.getAtomKeys().stream()
					.anyMatch(key -> key.startsWith("wood.logs.normal"))));
		assertHorizontalFamily(target, Arrays.asList(1511, 1521, 1517, 1515, 1513, 19669));
		assertHorizontalFamily(target, Arrays.asList(960, 8778, 8780, 8782, 31435));
	}

	@Test
	public void globalResourceMatrixProtectsAllReviewedWorkflowRows()
	{
		List<Integer> input = new ArrayList<>(Arrays.asList(
			436, 440, 453, 442, 444, 447, 449, 451,
			2351, 2353, 2355, 2357, 2359, 2361, 31719, 32892,
			22603, 21543, 13573, 21545, 13421, 21622,
			1511, 1521, 1517, 1515, 1513, 19669,
			6333, 6332, 32904, 32907, 24691, 22935, 28134,
			960, 8778, 8780, 8782, 31435));
		List<Integer> currentRawGems = new ArrayList<>();
		currentRawGems.add(1625);
		currentRawGems.addAll(GEM_RAW);
		List<Integer> currentProcessedGems = new ArrayList<>();
		currentProcessedGems.add(1609);
		currentProcessedGems.addAll(GEM_PROCESSED);
		input.addAll(currentRawGems);
		input.addAll(currentProcessedGems);
		input.addAll(Arrays.asList(1635, 1637, 1639));
		input.addAll(GLASS_WORKFLOW);
		input.addAll(TEXTILE_INPUTS);
		input.addAll(TEXTILE_FABRICS);
		input.addAll(ARROW_PRODUCTION);
		input.add(1777);
		input.addAll(Arrays.asList(39, 43));
		input.addAll(Arrays.asList(822, 823, 9422));
		input.addAll(Arrays.asList(9416, 29311));
		input.addAll(fillers(908000, 50));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertTrue(outcome.getTieKey().getBlocks().size() >= 2);
		assertMetalColumns(target);
		assertHorizontalFamily(target, Arrays.asList(24691, 22935, 28134));
		assertHorizontalFamily(target, Arrays.asList(960, 8778, 8780, 8782, 31435));
		assertHorizontalFamily(target, currentRawGems);
		assertHorizontalFamily(target, currentProcessedGems);
		assertHorizontalFamily(target, Arrays.asList(1635, 1637, 1639));
		assertHorizontalFamily(target, GLASS_WORKFLOW);
		assertHorizontalFamily(target, TEXTILE_INPUTS);
		assertHorizontalFamily(target, TEXTILE_FABRICS);
		assertHorizontalFamily(target, Arrays.asList(52, 314, 53, 1777, 39, 43, 822, 823));
		assertHorizontalFamily(target, Arrays.asList(9422, 9416, 29311));
		assertDensePermutation(input, target);
	}

	@Test
	public void gemEvidenceChoosesWidthFiveEvenWhenWiderMatricesFit()
	{
		List<Integer> input = new ArrayList<>();
		for (int index = 0; index < GEM_RAW.size(); index++)
		{
			input.add(GEM_RAW.get(index));
			input.add(GEM_PROCESSED.get(index));
		}
		input.addAll(fillers(910000, 6));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertEquals(5, onlyBlock(outcome).getWidth());
		assertEquals(GEM_RAW, target.subList(0, 5));
		assertEquals(GEM_PROCESSED, target.subList(8, 13));
		assertDensePermutation(input, target);
	}

	@Test
	public void gemWidthFiveRetainsExactDenseTailWithThreeRealFillers()
	{
		List<Integer> input = new ArrayList<>();
		for (int index = 0; index < GEM_RAW.size(); index++)
		{
			input.add(GEM_RAW.get(index));
			input.add(GEM_PROCESSED.get(index));
		}
		input.addAll(fillers(920000, 3));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertEquals(5, onlyBlock(outcome).getWidth());
		assertEquals(GEM_RAW, target.subList(0, 5));
		assertEquals(GEM_PROCESSED, target.subList(8, 13));
		assertDensePermutation(input, target);
	}

	@Test
	public void oneCompleteMetalFamilyAlignsWhileIncompleteStagesFallBack()
	{
		List<Integer> input = new ArrayList<>(Arrays.asList(2351, 2355, 436, 438, 442, 444));
		input.addAll(fillers(930000, 12));

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, input);
		List<Integer> target = targetOrder(outcome);

		assertEquals(1, onlyBlock(outcome).getWidth());
		assertEquals(ShapePrimitive.STAGE_MATRIX, onlyBlock(outcome).getShapePrimitive());
		assertEquals(Integer.valueOf(442), target.get(0));
		assertEquals(Integer.valueOf(2355), target.get(8));
		assertEquals(target.indexOf(442) % 8, target.indexOf(2355) % 8);
		assertTrue(target.contains(2351));
		assertTrue(target.contains(444));
		assertDensePermutation(input, target);
	}

	@Test
	public void incompleteSingletonFamiliesUseExactFallbackWithoutPhantomMembers()
	{
		List<Integer> input = Arrays.asList(440, 2355, 1623, 1605, 940001, 940002);
		List<Integer> fallback = Arrays.asList(940002, 2355, 440, 1623, 940001, 1605);

		BoundedLayoutPacker.Outcome outcome = planDetailed(input, fallback);
		List<Integer> target = targetOrder(outcome);

		assertTrue(outcome.getTieKey().getBlocks().isEmpty());
		assertEquals(fallback, target);
		assertDensePermutation(input, target);
		assertFalse(target.contains(2351));
		assertFalse(target.contains(1607));
	}

	@Test
	public void realPlaceholderStateAndQuantitySurviveSemanticPlacement()
	{
		List<LayoutEntry> entries = new ArrayList<>();
		entries.add(placeholderEntry(436, 73));
		entries.add(entry(438, 2));
		for (int filler : fillers(950000, 7))
		{
			entries.add(entry(filler, filler));
		}
		List<Integer> fallback = itemIds(entries);

		LayoutResult result = new SemanticBlockLayoutEngine().plan(
			ResourceSemanticRuleSet.forEntries(entries), fallback);

		assertTrue(result.getConflicts().toString(), result.isSuccess());
		LayoutPlacement raw = placementFor(result, 436);
		assertTrue(raw.getItem().isPlaceholder());
		assertEquals(0, raw.getItem().getQuantity());
		assertEquals(0, raw.getTargetIndex());
		assertEquals(1, placementFor(result, 438).getTargetIndex());
	}

	private static void assertMetalRows(SemanticRule rule)
	{
		assertEquals("resource.metal.material-rows", rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.STAGE_MATRIX, rule.getShapePrimitive());
		assertEquals(Arrays.asList("metal.iron", "metal.silver", "metal.gold", "metal.mithril",
			"metal.adamantite", "metal.runite"),
			atomKeys(rule));
		for (int index = 0; index < rule.getAtoms().size(); index++)
		{
			SemanticAtom atom = rule.getAtoms().get(index);
			assertEquals(Arrays.asList(METAL_MATRIX_ORES.get(index), METAL_MATRIX_BARS.get(index)),
				atom.getItemIds());
			assertEquals("raw", atom.getMembers().get(0).getMemberKey());
			assertEquals("processed", atom.getMembers().get(1).getMemberKey());
		}
	}

	private static void assertMetalColumns(List<Integer> target)
	{
		for (int index = 0; index < METAL_MATRIX_ORES.size(); index++)
		{
			int rawTarget = target.indexOf(METAL_MATRIX_ORES.get(index));
			int processedTarget = target.indexOf(METAL_MATRIX_BARS.get(index));
			if (rawTarget < 0 || processedTarget < 0)
			{
				continue;
			}
			assertEquals("processed stage must occupy the following physical row",
				rawTarget / 8 + 1, processedTarget / 8);
			assertEquals("raw and processed stages must share a physical column",
				rawTarget % 8, processedTarget % 8);
		}
	}

	private static void assertRule(SemanticRule rule, String ruleKey, List<String> familyKeys,
		List<Integer> rawIds, List<Integer> processedIds)
	{
		assertEquals(ruleKey, rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.STAGE_MATRIX, rule.getShapePrimitive());
		assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)),
			rule.getAllowedWidths());
		assertTrue(rule.getSpilloverCompatibleRuleKeys().isEmpty());
		assertEquals(familyKeys.size(), rule.getAtoms().size());

		for (int index = 0; index < familyKeys.size(); index++)
		{
			SemanticAtom atom = rule.getAtoms().get(index);
			assertEquals(familyKeys.get(index), atom.getAtomKey());
			assertEquals(Arrays.asList(rawIds.get(index), processedIds.get(index)), atom.getItemIds());
			assertEquals("raw", atom.getMembers().get(0).getMemberKey());
			assertEquals("processed", atom.getMembers().get(1).getMemberKey());
			assertMetadata(atom.getAtomKey(), rawIds.get(index), 0);
			assertMetadata(atom.getAtomKey(), processedIds.get(index), 1);
		}
	}

	private static void assertWoodRows(SemanticRule rule)
	{
		assertEquals("resource.wood.material-rows", rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.ROW_GROUP_MATRIX, rule.getShapePrimitive());
		assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)),
			rule.getAllowedWidths());
		assertFalse(rule.hasWidthEvidence());
		assertEquals(Arrays.asList("wood.logs.normal", "wood.logs.construction",
			"wood.special-materials", "wood.planks"), atomKeys(rule));
		assertEquals(NORMAL_LOGS, rule.getAtoms().get(0).getItemIds());
		assertEquals(CONSTRUCTION_LOGS, rule.getAtoms().get(1).getItemIds());
		assertEquals(SPECIAL_WOOD, rule.getAtoms().get(2).getItemIds());
		assertEquals(PLANKS, rule.getAtoms().get(3).getItemIds());

		for (SemanticAtom atom : rule.getAtoms())
		{
			for (int index = 0; index < atom.getMembers().size(); index++)
			{
				assertEquals("tier-" + index, atom.getMembers().get(index).getMemberKey());
				assertMetadata(atom.getAtomKey(), atom.getItemIds().get(index), index);
			}
		}
	}

	private static void assertCraftingRows(SemanticRule rule)
	{
		assertEquals("resource.crafting.workflow-rows", rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.ROW_GROUP_MATRIX, rule.getShapePrimitive());
		assertEquals(Arrays.asList("crafting.jewellery.rings",
			"crafting.jewellery.necklaces", "crafting.jewellery.bracelets",
			"crafting.jewellery.amulets-unstrung", "crafting.glass-workflow",
			"crafting.textile-inputs", "crafting.textile-fabrics",
			"construction.nails"), atomKeys(rule));
		assertEquals(JEWELLERY_RINGS, rule.getAtoms().get(0).getItemIds());
		assertEquals(GLASS_WORKFLOW, rule.getAtoms().get(4).getItemIds());
		assertEquals(TEXTILE_INPUTS, rule.getAtoms().get(5).getItemIds());
		assertEquals(TEXTILE_FABRICS, rule.getAtoms().get(6).getItemIds());
		assertEquals(NAILS, rule.getAtoms().get(7).getItemIds());
		assertTierMetadata(rule);
	}

	private static void assertFletchingRuns(SemanticRule rule)
	{
		assertEquals("resource.fletching.tier-runs", rule.getRuleKey());
		assertEquals(ConfidenceTier.HIGH, rule.getConfidenceTier());
		assertEquals(ShapePrimitive.HORIZONTAL_RUN, rule.getShapePrimitive());
		assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)),
			rule.getAllowedWidths());
		assertFalse(rule.hasWidthEvidence());
		assertEquals(Arrays.asList("fletching.arrow-production", "fletching.arrowtips",
			"fletching.dart-tips", "fletching.crossbow-limbs",
			"fletching.unstrung-shortbows", "fletching.unstrung-longbows"), atomKeys(rule));
		assertEquals(ARROW_PRODUCTION, rule.getAtoms().get(0).getItemIds());
		assertEquals(ARROWTIPS, rule.getAtoms().get(1).getItemIds());
		assertEquals(DART_TIPS, rule.getAtoms().get(2).getItemIds());
		assertEquals(CROSSBOW_LIMBS, rule.getAtoms().get(3).getItemIds());
		assertEquals(UNSTRUNG_SHORTBOWS, rule.getAtoms().get(4).getItemIds());
		assertEquals(UNSTRUNG_LONGBOWS, rule.getAtoms().get(5).getItemIds());
		assertTierMetadata(rule);
	}

	private static void assertTierMetadata(SemanticRule rule)
	{
		for (SemanticAtom atom : rule.getAtoms())
		{
			for (int index = 0; index < atom.getMembers().size(); index++)
			{
				assertEquals("tier-" + index, atom.getMembers().get(index).getMemberKey());
				assertMetadata(atom.getAtomKey(), atom.getItemIds().get(index), index);
			}
		}
	}

	private static void assertHorizontalFamily(List<Integer> target, List<Integer> family)
	{
		int first = target.indexOf(family.get(0));
		assertTrue(first >= 0);
		assertTrue("family must not wrap across a bank row", first % 8 + family.size() <= 8);
		assertEquals(family, target.subList(first, first + family.size()));
	}

	private static List<String> atomKeys(SemanticRule rule)
	{
		List<String> keys = new ArrayList<>();
		for (SemanticAtom atom : rule.getAtoms())
		{
			keys.add(atom.getAtomKey());
		}
		return keys;
	}

	private static void assertMetadata(String familyKey, int itemId, int stage)
	{
		ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId)
			.orElseThrow(AssertionError::new);
		assertEquals(familyKey, metadata.getFamilyKey());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, metadata.getVariantKind());
		assertEquals(stage, metadata.getVariantValue());
	}

	private static BoundedLayoutPacker.Outcome planDetailed(List<Integer> input,
		List<Integer> fallback)
	{
		List<LayoutEntry> entries = new ArrayList<>();
		for (int index = 0; index < input.size(); index++)
		{
			entries.add(entry(input.get(index), 1000 + index * 7));
		}
		BoundedLayoutPacker.Outcome outcome = SemanticBlockLayoutEngine.planDetailed(
			ResourceSemanticRuleSet.forEntries(entries), fallback,
			BoundedLayoutPacker.Limits.production());
		assertTrue(outcome.getResult().getConflicts().toString(), outcome.getResult().isSuccess());
		return outcome;
	}

	private static List<Integer> targetOrder(BoundedLayoutPacker.Outcome outcome)
	{
		Integer[] byTarget = new Integer[outcome.getResult().getPlacements().size()];
		for (LayoutPlacement placement : outcome.getResult().getPlacements())
		{
			byTarget[placement.getTargetIndex()] = placement.getItem().getItemId();
		}
		return Arrays.asList(byTarget);
	}

	private static PlacedBlock onlyBlock(BoundedLayoutPacker.Outcome outcome)
	{
		assertEquals(1, outcome.getTieKey().getBlocks().size());
		return outcome.getTieKey().getBlocks().get(0);
	}

	private static LayoutPlacement placementFor(LayoutResult result, int itemId)
	{
		for (LayoutPlacement placement : result.getPlacements())
		{
			if (placement.getItem().getItemId() == itemId)
			{
				return placement;
			}
		}
		throw new AssertionError("missing itemId " + itemId);
	}

	private static LayoutEntry entry(int itemId, int sourceSlot)
	{
		return LayoutEntry.of(item(itemId, 1, false), sourceSlot);
	}

	private static LayoutEntry placeholderEntry(int itemId, int sourceSlot)
	{
		return LayoutEntry.of(item(itemId, 0, true), sourceSlot);
	}

	private static BankPreviewItem item(int itemId, int quantity, boolean placeholder)
	{
		return new BankPreviewItem(new CatalogItem(itemId, "Item " + itemId, ItemCategory.SKILLING,
			"resource", Collections.emptySet(), null), quantity, placeholder);
	}

	private static List<Integer> fillers(int firstItemId, int count)
	{
		List<Integer> ids = new ArrayList<>();
		for (int offset = 0; offset < count; offset++)
		{
			ids.add(firstItemId + offset);
		}
		return ids;
	}

	private static List<Integer> itemIds(List<LayoutEntry> entries)
	{
		List<Integer> ids = new ArrayList<>();
		for (LayoutEntry entry : entries)
		{
			ids.add(entry.getItem().getItemId());
		}
		return ids;
	}

	private static List<Integer> sourceSlots(List<LayoutEntry> entries)
	{
		List<Integer> slots = new ArrayList<>();
		for (LayoutEntry entry : entries)
		{
			slots.add(entry.getSourceFlatBankSlot());
		}
		return slots;
	}

	private static void assertDensePermutation(List<Integer> input, List<Integer> target)
	{
		assertEquals(input.size(), target.size());
		assertFalse(target.contains(null));
		Set<Integer> expected = new HashSet<>(input);
		Set<Integer> actual = new HashSet<>(target);
		assertEquals(input.size(), expected.size());
		assertEquals(expected, actual);
	}
}

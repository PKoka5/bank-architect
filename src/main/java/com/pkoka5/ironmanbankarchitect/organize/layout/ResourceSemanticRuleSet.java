package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Exact canonical resource relationships used by the category-level semantic layout engine.
 * Display names are deliberately absent: every family and stage is validated against the local
 * curated metadata before a request is created.
 */
public final class ResourceSemanticRuleSet
{
	private static final String METAL_RULE_KEY = "resource.metal.material-rows";
	private static final String PRESENT_MATERIAL_ROWS_RULE_KEY = "resource.material.present-rows";
	private static final String GEM_RULE_KEY = "resource.gem.raw-processed";
	private static final String WOOD_RULE_KEY = "resource.wood.material-rows";
	private static final String CRAFTING_RULE_KEY = "resource.crafting.workflow-rows";
	private static final String FLETCHING_RULE_KEY = "resource.fletching.tier-runs";

	private static final Set<Integer> ALL_WIDTHS = Collections.unmodifiableSet(
		new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));

	private static final OrderedItemFamilies TABLE = new OrderedItemFamilies(
		ResourceSemanticRuleSet.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/resource-layout-families.tsv"), 0, false);
	private static final RequiredResource<Data> DATA = new RequiredResource<>("resource layout", Data::new);

	private static final class Data
	{
		private final List<RowFact> metalRows = rows("METAL_ROWS");
		private final List<FamilyFact> metalFamilies = families("METAL_FAMILIES");
		private final List<Integer> metalSpilloverOres = TABLE.ids("METAL_SPILLOVER_ORES");
		private final List<Integer> metalSpilloverBars = TABLE.ids("METAL_SPILLOVER_BARS");
		private final List<Integer> sailingOres = TABLE.ids("SAILING_ORES");
		private final List<Integer> sailingProcessedMetals = TABLE.ids("SAILING_PROCESSED_METALS");
		private final List<Integer> supplementalMiningMaterials = TABLE.ids("SUPPLEMENTAL_MINING_MATERIALS");

		private final FamilyFact opalFamily = families("OPAL_FAMILY").get(0);
		private final List<FamilyFact> gemFamilies = families("GEM_FAMILIES");

		private final List<RowFact> woodRows = rows("WOOD_ROWS");

		private final List<RowFact> craftingRows = rows("CRAFTING_ROWS");

		private final List<RowFact> fletchingRows = rows("FLETCHING_ROWS");
		private final List<Integer> fletchingStringComponents = TABLE.ids("FLETCHING_STRING_COMPONENTS");
		private final List<Integer> fletchingSpecialComponents = TABLE.ids("FLETCHING_SPECIAL_COMPONENTS");

		private final List<SemanticRule> rules = Collections.unmodifiableList(Arrays.asList(
			stageMatrix(METAL_RULE_KEY, metalFamilies, null),
			stageMatrix(GEM_RULE_KEY, gemFamilies, SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED),
			rowGroupMatrix(WOOD_RULE_KEY, woodRows),
			rowGroupMatrix(CRAFTING_RULE_KEY, craftingRows),
			horizontalRuns(FLETCHING_RULE_KEY, fletchingRows)));
	}

	private static List<RowFact> rows(String group)
	{
		List<RowFact> rows = new ArrayList<>();
		TABLE.group(group).forEach((key, ids) -> rows.add(new RowFact(key, ids)));
		return Collections.unmodifiableList(rows);
	}

	private static List<FamilyFact> families(String group)
	{
		List<FamilyFact> families = new ArrayList<>();
		TABLE.group(group).forEach((key, ids) ->
		{
			if (ids.size() != 2) throw new IllegalStateException("Invalid resource family: " + key);
			families.add(new FamilyFact(key, ids.get(0), ids.get(1)));
		});
		return Collections.unmodifiableList(families);
	}

	private ResourceSemanticRuleSet()
	{
	}

	/**
	 * Creates a request without claiming a dense current order or adding entry-level dense ranks.
	 * Flat source bank slots retain only their placement-context meaning.
	 */
	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		validateRows(DATA.get().metalRows);
		validateMetadata(Collections.singletonList(DATA.get().opalFamily));
		validateMetadata(DATA.get().gemFamilies);
		validateRows(DATA.get().woodRows);
		validateRows(DATA.get().craftingRows);
		validateRows(DATA.get().fletchingRows);
		return new LayoutRequest(anchoredEntries(entries), rulesForEntries(entries));
	}

	/**
	 * Creates a request for entries already restricted to one {@code ResourceSkillZone}. A zone's
	 * physical window can be too small to hold a {@code ROW_GROUP_MATRIX} rule's full coupled block
	 * (all its rows share one origin and are placed as an all-or-nothing unit), so every such rule
	 * is decoupled here into independently placeable {@code HORIZONTAL_RUN} rows. Declared row order
	 * is preserved via an ordinal rule-key prefix so tie-breaking never falls back to alphabetical
	 * atom-key order. Metal/gem stage matrices and existing horizontal-run rules are unaffected.
	 */
	public static LayoutRequest forZoneEntries(List<LayoutEntry> entries)
	{
		Objects.requireNonNull(entries, "entries");
		validateRows(DATA.get().metalRows);
		validateMetadata(Collections.singletonList(DATA.get().opalFamily));
		validateMetadata(DATA.get().gemFamilies);
		validateRows(DATA.get().woodRows);
		validateRows(DATA.get().craftingRows);
		validateRows(DATA.get().fletchingRows);
		return new LayoutRequest(anchoredEntries(entries),
			decoupleRowGroupMatrices(rulesForEntries(entries)));
	}

	private static List<SemanticRule> decoupleRowGroupMatrices(List<SemanticRule> rules)
	{
		List<SemanticRule> decoupled = new ArrayList<>();
		for (SemanticRule rule : rules)
		{
			if (rule.getShapePrimitive() != ShapePrimitive.ROW_GROUP_MATRIX)
			{
				decoupled.add(rule);
				continue;
			}

			List<SemanticAtom> atoms = rule.getAtoms();
			for (int index = 0; index < atoms.size(); index++)
			{
				SemanticAtom atom = atoms.get(index);
				SemanticRule.Builder builder = SemanticRule.builder()
					.ruleKey(String.format("zone-row-%02d.%s", index, atom.getAtomKey()))
					.atoms(Collections.singletonList(atom))
					.confidenceTier(rule.getConfidenceTier())
					.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
					.allowedWidths(rule.getAllowedWidths());
				if (rule.hasWidthEvidence())
				{
					builder.widthEvidence(rule.getWidthEvidence());
				}
				if (!rule.getSpilloverCompatibleRuleKeys().isEmpty())
				{
					builder.spilloverCompatibleRuleKeys(rule.getSpilloverCompatibleRuleKeys());
				}
				decoupled.add(builder.build());
			}
		}
		return Collections.unmodifiableList(decoupled);
	}

	private static List<LayoutEntry> anchoredEntries(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
			if (entry.hasLockedTarget())
			{
				return entries;
			}
		}
		if (Collections.disjoint(present, DATA.get().supplementalMiningMaterials))
		{
			return entries;
		}

		List<Integer> anchorOrder = new ArrayList<>();
		for (FamilyFact family : DATA.get().metalFamilies)
		{
			anchorOrder.add(family.rawItemId);
		}
		for (FamilyFact family : DATA.get().metalFamilies)
		{
			anchorOrder.add(family.processedItemId);
		}
		anchorOrder.addAll(DATA.get().metalSpilloverOres);
		anchorOrder.addAll(DATA.get().metalSpilloverBars);
		anchorOrder.addAll(DATA.get().sailingOres);
		anchorOrder.addAll(DATA.get().sailingProcessedMetals);
		anchorOrder.addAll(DATA.get().supplementalMiningMaterials);
		for (Integer anchorItemId : anchorOrder)
		{
			if (!present.contains(anchorItemId))
			{
				continue;
			}
			List<LayoutEntry> anchored = new ArrayList<>(entries.size());
			for (LayoutEntry entry : entries)
			{
				anchored.add(entry.getItem().getItemId() == anchorItemId
					? entry.withLockedTarget(0) : entry);
			}
			return Collections.unmodifiableList(anchored);
		}
		return entries;
	}

	private static List<SemanticRule> rulesForEntries(List<LayoutEntry> entries)
	{
		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}
		if (Collections.disjoint(present, DATA.get().supplementalMiningMaterials))
		{
			return DATA.get().rules;
		}

		List<SemanticRule> rules = new ArrayList<>();
		rules.add(stageMatrix(METAL_RULE_KEY, DATA.get().metalFamilies, null));
		rules.add(presentMaterialRule(present));
		return Collections.unmodifiableList(rules);
	}

	private static SemanticRule presentMaterialRule(Set<Integer> present)
	{
		List<SemanticAtom> atoms = new ArrayList<>();
		addPresentChunks(atoms, "metal.ores-spillover", DATA.get().metalSpilloverOres, present);
		addPresentChunks(atoms, "metal.bars-spillover", DATA.get().metalSpilloverBars, present);
		addPresentChunks(atoms, "sailing.ores", DATA.get().sailingOres, present);
		addPresentChunks(atoms, "sailing.processed-metals", DATA.get().sailingProcessedMetals, present);
		addPresentChunks(atoms, "mining.supplemental", DATA.get().supplementalMiningMaterials, present);
		for (RowFact row : DATA.get().woodRows)
		{
			addPresentChunks(atoms, row.familyKey, row.itemIds, present);
		}
		addPresentGemStages(atoms, present);
		for (RowFact row : DATA.get().craftingRows)
		{
			addPresentChunks(atoms, row.familyKey, row.itemIds, present);
		}
		addCompactPresentFletchingRows(atoms, present);

		return SemanticRule.builder()
			.ruleKey(PRESENT_MATERIAL_ROWS_RULE_KEY)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static void addCompactPresentFletchingRows(List<SemanticAtom> atoms, Set<Integer> present)
	{
		List<List<Integer>> families = Arrays.asList(
			DATA.get().fletchingRows.get(0).itemIds,
			DATA.get().fletchingStringComponents,
			DATA.get().fletchingRows.get(1).itemIds,
			DATA.get().fletchingRows.get(2).itemIds,
			DATA.get().fletchingRows.get(3).itemIds,
			DATA.get().fletchingSpecialComponents,
			DATA.get().fletchingRows.get(4).itemIds,
			DATA.get().fletchingRows.get(5).itemIds);
		List<Integer> packedRow = new ArrayList<>();
		int rowIndex = 0;
		for (List<Integer> family : families)
		{
			List<Integer> ownedFamily = new ArrayList<>();
			for (Integer itemId : family)
			{
				if (present.contains(itemId))
				{
					ownedFamily.add(itemId);
				}
			}
			if (ownedFamily.isEmpty())
			{
				continue;
			}
			if (!packedRow.isEmpty() && packedRow.size() + ownedFamily.size() > 8)
			{
				addPresentChunks(atoms, "fletching.owned-workflows-" + rowIndex++, packedRow, present);
				packedRow = new ArrayList<>();
			}
			packedRow.addAll(ownedFamily);
			if (packedRow.size() == 8)
			{
				addPresentChunks(atoms, "fletching.owned-workflows-" + rowIndex++, packedRow, present);
				packedRow = new ArrayList<>();
			}
		}
		if (!packedRow.isEmpty())
		{
			addPresentChunks(atoms, "fletching.owned-workflows-" + rowIndex, packedRow, present);
		}
	}

	private static void addPresentGemStages(List<SemanticAtom> atoms, Set<Integer> present)
	{
		List<Integer> raw = new ArrayList<>(DATA.get().gemFamilies.size() + 1);
		List<Integer> processed = new ArrayList<>(DATA.get().gemFamilies.size() + 1);
		raw.add(DATA.get().opalFamily.rawItemId);
		processed.add(DATA.get().opalFamily.processedItemId);
		for (FamilyFact family : DATA.get().gemFamilies)
		{
			raw.add(family.rawItemId);
			processed.add(family.processedItemId);
		}
		addPresentChunks(atoms, "gem.raw", raw, present);
		addPresentChunks(atoms, "gem.processed", processed, present);
	}

	private static void addPresentChunks(List<SemanticAtom> atoms, String atomKey,
		List<Integer> canonicalIds, Set<Integer> present)
	{
		List<SemanticAtom.Member> members = new ArrayList<>();
		int chunk = 0;
		for (Integer itemId : canonicalIds)
		{
			if (!present.contains(itemId))
			{
				continue;
			}
			if (members.size() == 8)
			{
				atoms.add(new SemanticAtom(atomKey + "-" + chunk++, members));
				members = new ArrayList<>();
			}
			members.add(new SemanticAtom.Member("member-" + members.size(), itemId));
		}
		if (!members.isEmpty())
		{
			atoms.add(new SemanticAtom(atomKey + "-" + chunk, members));
		}
	}

	private static SemanticRule horizontalRuns(String ruleKey, List<RowFact> rows)
	{
		List<SemanticAtom> atoms = atomsForRows(rows);
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.HORIZONTAL_RUN)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static SemanticRule rowGroupMatrix(String ruleKey, List<RowFact> rows)
	{
		return SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atomsForRows(rows))
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static List<SemanticAtom> atomsForRows(List<RowFact> rows)
	{
		List<SemanticAtom> atoms = new ArrayList<>(rows.size());
		for (RowFact row : rows)
		{
			List<SemanticAtom.Member> members = new ArrayList<>(row.itemIds.size());
			for (int index = 0; index < row.itemIds.size(); index++)
			{
				members.add(new SemanticAtom.Member("tier-" + index, row.itemIds.get(index)));
			}
			atoms.add(new SemanticAtom(row.familyKey, members));
		}

		return atoms;
	}

	private static SemanticRule stageMatrix(String ruleKey, List<FamilyFact> families,
		WidthEvidence widthEvidence)
	{
		List<SemanticAtom> atoms = new ArrayList<>(families.size());
		for (FamilyFact family : families)
		{
			atoms.add(new SemanticAtom(family.familyKey, Arrays.asList(
				new SemanticAtom.Member("raw", family.rawItemId),
				new SemanticAtom.Member("processed", family.processedItemId))));
		}

		SemanticRule.Builder builder = SemanticRule.builder()
			.ruleKey(ruleKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.STAGE_MATRIX)
			.allowedWidths(ALL_WIDTHS);
		if (widthEvidence != null)
		{
			builder.widthEvidence(widthEvidence);
		}
		return builder.build();
	}

	private static void validateMetadata(List<FamilyFact> families)
	{
		for (FamilyFact family : families)
		{
			validateMember(family.familyKey, family.rawItemId, 0);
			validateMember(family.familyKey, family.processedItemId, 1);
		}
	}

	private static void validateRows(List<RowFact> rows)
	{
		for (RowFact row : rows)
		{
			for (int index = 0; index < row.itemIds.size(); index++)
			{
				validateMember(row.familyKey, row.itemIds.get(index), index);
			}
		}
	}

	private static void validateMember(String expectedFamilyKey, int itemId, int expectedStage)
	{
		ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId)
			.orElseThrow(() -> new IllegalStateException(
				"Missing resource semantic metadata for itemId " + itemId));
		if (!expectedFamilyKey.equals(metadata.getFamilyKey())
			|| metadata.getVariantKind() != ItemSortMetadata.VariantKind.WORKFLOW_STAGE
			|| metadata.getVariantValue() != expectedStage)
		{
			throw new IllegalStateException("Resource semantic metadata mismatch for itemId " + itemId
				+ ": expected family=" + expectedFamilyKey + ", kind=WORKFLOW_STAGE, stage="
				+ expectedStage + " but was family=" + metadata.getFamilyKey() + ", kind="
				+ metadata.getVariantKind() + ", stage=" + metadata.getVariantValue());
		}
	}


	private static final class RowFact
	{
		private final String familyKey;
		private final List<Integer> itemIds;

		private RowFact(String familyKey, List<Integer> itemIds)
		{
			this.familyKey = familyKey;
			this.itemIds = itemIds;
		}
	}

	private static final class FamilyFact
	{
		private final String familyKey;
		private final int rawItemId;
		private final int processedItemId;

		private FamilyFact(String familyKey, int rawItemId, int processedItemId)
		{
			this.familyKey = familyKey;
			this.rawItemId = rawItemId;
			this.processedItemId = processedItemId;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
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
	private static final String GEM_RULE_KEY = "resource.gem.raw-processed";
	private static final String WOOD_RULE_KEY = "resource.wood.material-rows";
	private static final String CRAFTING_RULE_KEY = "resource.crafting.workflow-rows";
	private static final String FLETCHING_RULE_KEY = "resource.fletching.tier-runs";

	private static final Set<Integer> ALL_WIDTHS = Collections.unmodifiableSet(
		new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));

	private static final List<RowFact> METAL_ROWS = Collections.unmodifiableList(Arrays.asList(
		row("metal.ores-base", 436, 438, 440, 453),
		row("metal.ores-tier", 442, 444, 447, 449, 451),
		row("metal.bars", 2349, 2351, 2353, 2355, 2357, 2359, 2361, 2363)));
	private static final List<Integer> SAILING_ORES =
		Collections.singletonList(31719);
	private static final List<Integer> SAILING_PROCESSED_METALS =
		Collections.singletonList(32892);
	private static final List<Integer> SUPPLEMENTAL_MINING_MATERIALS = Collections.unmodifiableList(
		Arrays.asList(22603, 21543, 13573, 21545, 13421, 21622));

	private static final FamilyFact OPAL_FAMILY = family("gem.opal", 1625, 1609);
	private static final List<FamilyFact> GEM_FAMILIES = Collections.unmodifiableList(Arrays.asList(
		family("gem.sapphire", 1623, 1607),
		family("gem.emerald", 1621, 1605),
		family("gem.ruby", 1619, 1603),
		family("gem.diamond", 1617, 1601),
		family("gem.dragonstone", 1631, 1615)));

	private static final List<RowFact> WOOD_ROWS = Collections.unmodifiableList(Arrays.asList(
		row("wood.logs.normal", 1511, 1521, 1519, 1517, 1515, 1513, 19669),
		row("wood.logs.construction", 6333, 6332, 32904, 32907, 32910),
		row("wood.special-materials", 10810, 24691, 22935, 28134),
		row("wood.planks", 960, 8778, 8780, 8782, 31432, 31435, 31438)));

	private static final List<RowFact> CRAFTING_ROWS = Collections.unmodifiableList(Arrays.asList(
		row("crafting.jewellery.rings", 1635, 1637, 1639, 1641, 1643, 1645, 6575, 19538),
		row("crafting.jewellery.necklaces", 1654, 1656, 1658, 1660, 1662, 1664, 6577, 19535),
		row("crafting.jewellery.bracelets", 11069, 11072, 11076, 11085, 11092, 11115, 11130, 19532),
		row("crafting.jewellery.amulets-unstrung", 1673, 1675, 1677, 1679, 1681, 1683, 6579, 19501),
		row("crafting.glass-workflow", 21504, 401, 1781, 1783, 1775, 4542, 567, 229),
		row("crafting.textile-inputs", 1779, 1759, 1734, 5931),
		row("crafting.textile-fabrics", 31475, 8790, 31472, 31746, 31758, 31734),
		row("construction.nails", 4819, 4820, 1539, 4822, 4823, 4824)));

	private static final List<RowFact> FLETCHING_ROWS = Collections.unmodifiableList(Arrays.asList(
		row("fletching.arrow-production", 52, 314, 53),
		row("fletching.arrowtips", 39, 40, 41, 42, 43, 44, 21350, 11237),
		row("fletching.dart-tips", 819, 820, 821, 822, 823, 824, 25853),
		row("fletching.crossbow-limbs", 9420, 9422, 9423, 9425, 9427, 9429, 9431),
		row("fletching.unstrung-shortbows", 50, 54, 60, 64, 68, 72),
		row("fletching.unstrung-longbows", 48, 56, 58, 62, 66, 70)));
	private static final List<Integer> FLETCHING_STRING_COMPONENTS =
		Collections.singletonList(1777);
	private static final List<Integer> FLETCHING_SPECIAL_COMPONENTS =
		Collections.unmodifiableList(Arrays.asList(9416, 29311));

	private static final List<SemanticRule> RULES = Collections.unmodifiableList(Arrays.asList(
		rowGroupMatrix(METAL_RULE_KEY, METAL_ROWS),
		stageMatrix(GEM_RULE_KEY, GEM_FAMILIES, SemanticWidthEvidenceFacts.GEM_RAW_PROCESSED),
		rowGroupMatrix(WOOD_RULE_KEY, WOOD_ROWS),
		rowGroupMatrix(CRAFTING_RULE_KEY, CRAFTING_ROWS),
		horizontalRuns(FLETCHING_RULE_KEY, FLETCHING_ROWS)));

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
		validateRows(METAL_ROWS);
		validateMetadata(Collections.singletonList(OPAL_FAMILY));
		validateMetadata(GEM_FAMILIES);
		validateRows(WOOD_ROWS);
		validateRows(CRAFTING_ROWS);
		validateRows(FLETCHING_ROWS);
		return new LayoutRequest(anchoredEntries(entries), rulesForEntries(entries));
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
		if (Collections.disjoint(present, SUPPLEMENTAL_MINING_MATERIALS))
		{
			return entries;
		}

		List<Integer> anchorOrder = new ArrayList<>();
		anchorOrder.addAll(METAL_ROWS.get(0).itemIds);
		anchorOrder.addAll(METAL_ROWS.get(1).itemIds);
		anchorOrder.addAll(METAL_ROWS.get(2).itemIds);
		anchorOrder.addAll(SAILING_ORES);
		anchorOrder.addAll(SAILING_PROCESSED_METALS);
		anchorOrder.addAll(SUPPLEMENTAL_MINING_MATERIALS);
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
		if (Collections.disjoint(present, SUPPLEMENTAL_MINING_MATERIALS))
		{
			return RULES;
		}

		List<SemanticRule> rules = new ArrayList<>(RULES);
		rules.set(0, presentMaterialRule(present));
		rules.removeIf(rule -> GEM_RULE_KEY.equals(rule.getRuleKey())
			|| WOOD_RULE_KEY.equals(rule.getRuleKey())
			|| CRAFTING_RULE_KEY.equals(rule.getRuleKey())
			|| FLETCHING_RULE_KEY.equals(rule.getRuleKey()));
		return Collections.unmodifiableList(rules);
	}

	private static SemanticRule presentMaterialRule(Set<Integer> present)
	{
		List<SemanticAtom> atoms = new ArrayList<>();
		List<Integer> ores = new ArrayList<>();
		ores.addAll(METAL_ROWS.get(0).itemIds);
		ores.addAll(METAL_ROWS.get(1).itemIds);
		addPresentChunks(atoms, "metal.ores", ores, present);

		List<Integer> processed = new ArrayList<>(METAL_ROWS.get(2).itemIds);
		processed.addAll(SAILING_PROCESSED_METALS);
		addPresentChunks(atoms, "metal.processed", processed, present);
		List<Integer> supplementalMining = new ArrayList<>(SAILING_ORES);
		supplementalMining.addAll(SUPPLEMENTAL_MINING_MATERIALS);
		addPresentChunks(atoms, "mining.supplemental", supplementalMining, present);
		for (RowFact row : WOOD_ROWS)
		{
			addPresentChunks(atoms, row.familyKey, row.itemIds, present);
		}
		addPresentGemStages(atoms, present);
		for (RowFact row : CRAFTING_ROWS)
		{
			addPresentChunks(atoms, row.familyKey, row.itemIds, present);
		}
		addCompactPresentFletchingRows(atoms, present);

		return SemanticRule.builder()
			.ruleKey(METAL_RULE_KEY)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(ALL_WIDTHS)
			.build();
	}

	private static void addCompactPresentFletchingRows(List<SemanticAtom> atoms, Set<Integer> present)
	{
		List<List<Integer>> families = Arrays.asList(
			FLETCHING_ROWS.get(0).itemIds,
			FLETCHING_STRING_COMPONENTS,
			FLETCHING_ROWS.get(1).itemIds,
			FLETCHING_ROWS.get(2).itemIds,
			FLETCHING_ROWS.get(3).itemIds,
			FLETCHING_SPECIAL_COMPONENTS,
			FLETCHING_ROWS.get(4).itemIds,
			FLETCHING_ROWS.get(5).itemIds);
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
		List<Integer> raw = new ArrayList<>(GEM_FAMILIES.size() + 1);
		List<Integer> processed = new ArrayList<>(GEM_FAMILIES.size() + 1);
		raw.add(OPAL_FAMILY.rawItemId);
		processed.add(OPAL_FAMILY.processedItemId);
		for (FamilyFact family : GEM_FAMILIES)
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

	private static FamilyFact family(String familyKey, int rawItemId, int processedItemId)
	{
		return new FamilyFact(familyKey, rawItemId, processedItemId);
	}

	private static RowFact row(String familyKey, Integer... itemIds)
	{
		return new RowFact(familyKey, Collections.unmodifiableList(Arrays.asList(itemIds)));
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

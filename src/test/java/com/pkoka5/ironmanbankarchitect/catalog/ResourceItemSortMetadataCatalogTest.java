package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ResourceItemSortMetadataCatalogTest
{
	private static final String SCHEMA = "# schema=1\n";

	@Test
	public void loadsPinnedFoodFactsWithoutRuntimeLookups()
	{
		ResourceItemSortMetadataCatalog catalog = ResourceItemSortMetadataCatalog.INSTANCE;
		ItemSortMetadata shark = catalog.findById(385).get();
		ItemSortMetadata anglerfish = catalog.findById(13441).get();
		ItemSortMetadata moonlightAntelope = catalog.findById(29143).get();
		ItemSortMetadata pineapplePizza = catalog.findById(2301).get();
		ItemSortMetadata blightedManta = catalog.findById(24589).get();
		ItemSortMetadata halibut = catalog.findById(32336).get();
		ItemSortMetadata prayerPotion = catalog.findById(2434).get();

		assertEquals(408, catalog.size());
		assertEquals(20, shark.getImmediateHealMax());
		assertEquals(ItemSortMetadata.HealModel.VARIABLE, anglerfish.getHealModel());
		assertEquals(3, anglerfish.getImmediateHealMin());
		assertEquals(22, anglerfish.getImmediateHealMax());
		assertEquals(ItemSortMetadata.FoodRole.DELAYED, moonlightAntelope.getFoodRole());
		assertEquals(14, moonlightAntelope.getImmediateHealMax());
		assertEquals(12, moonlightAntelope.getSecondaryHeal());
		assertEquals(ItemSortMetadata.FoodRole.MULTI_BITE, pineapplePizza.getFoodRole());
		assertEquals(2, pineapplePizza.getVariantValue());
		assertEquals(ItemSortMetadata.AreaRestriction.BLIGHTED_AREAS,
			blightedManta.getAreaRestriction());
		assertEquals(ItemSortMetadata.FoodRole.COMBO, halibut.getFoodRole());
		assertEquals("potion.prayer", prayerPotion.getFamilyKey());
		assertEquals(ItemSortMetadata.VariantKind.DOSE, prayerPotion.getVariantKind());
		assertEquals(4, prayerPotion.getVariantValue());
		assertEquals("osrs-wiki-potions-15243625", prayerPotion.getSourceKey());
		assertFalse(catalog.findById(999_999).isPresent());
		assertEquals(16, catalog.sourceKeys().size());
		assertEquals("Pineapple pizza", ResourceItemRegistry.INSTANCE.findById(2301).get().getDisplayName());
		assertEquals("Blighted manta ray", ResourceItemRegistry.INSTANCE.findById(24589).get().getDisplayName());
		assertEquals("Halibut", ResourceItemRegistry.INSTANCE.findById(32336).get().getDisplayName());
	}

	@Test
	public void containsExpectedFoodAndCompletePotionDoseFamilies()
	{
		int foodCount = 0;
		int doseCount = 0;
		int chargeCount = 0;
		int workflowStageCount = 0;
		for (ItemSortMetadata metadata : ResourceItemSortMetadataCatalog.INSTANCE.entries())
		{
			if (metadata.isFood()) foodCount++;
			if (metadata.getVariantKind() == ItemSortMetadata.VariantKind.DOSE) doseCount++;
			if (metadata.getVariantKind() == ItemSortMetadata.VariantKind.CHARGE) chargeCount++;
			if (metadata.getVariantKind() == ItemSortMetadata.VariantKind.WORKFLOW_STAGE) workflowStageCount++;
		}

		assertEquals(43, foodCount);
		assertEquals(88, doseCount);
		assertEquals(81, chargeCount);
		assertEquals(196, workflowStageCount);
		assertWorkflowFamily("herb.guam", 199, 249, 91);
		assertWorkflowFamily("herb.ranarr", 207, 257, 99);
		assertWorkflowFamily("herb.dwarf_weed", 217, 267, 109);
		assertOrderedWorkflowFamily("metal.ores-base", new int[] {436, 438, 440, 453});
		assertOrderedWorkflowFamily("metal.ores-tier", new int[] {442, 444, 447, 449, 451});
		assertOrderedWorkflowFamily("metal.bars",
			new int[] {2349, 2351, 2353, 2355, 2357, 2359, 2361, 2363});
		assertResourceWorkflowFamily("gem.sapphire", 1623, 1607);
		assertResourceWorkflowFamily("gem.opal", 1625, 1609);
		assertResourceWorkflowFamily("gem.dragonstone", 1631, 1615);
		assertOrderedWorkflowFamily("wood.logs.normal",
			new int[] {1511, 1521, 1519, 1517, 1515, 1513, 19669});
		assertOrderedWorkflowFamily("wood.logs.construction",
			new int[] {6333, 6332, 32904, 32907, 32910});
		assertOrderedWorkflowFamily("wood.special-materials", new int[] {10810, 24691, 22935, 28134});
		assertOrderedWorkflowFamily("wood.planks",
			new int[] {960, 8778, 8780, 8782, 31432, 31435, 31438});
		assertOrderedWorkflowFamily("fletching.arrowtips",
			new int[] {39, 40, 41, 42, 43, 44, 21350, 11237});
		assertOrderedWorkflowFamily("fletching.unstrung-shortbows",
			new int[] {50, 54, 60, 64, 68, 72});
		assertOrderedWorkflowFamily("fletching.unstrung-longbows",
			new int[] {48, 56, 58, 62, 66, 70});
		assertOrderedWorkflowFamily("crafting.glass-workflow",
			new int[] {21504, 401, 1781, 1783, 1775, 4542, 567, 229});
		assertOrderedWorkflowFamily("crafting.textile-inputs", new int[] {1779, 1759, 1734, 5931});
		assertOrderedWorkflowFamily("construction.nails",
			new int[] {4819, 4820, 1539, 4822, 4823, 4824});
		assertOrderedWorkflowFamily("fletching.arrow-production", new int[] {52, 314, 53});
		assertOrderedWorkflowFamily("fletching.ballista-assembly",
			new int[] {19586, 19589, 19592, 19601, 19610, 19595, 19604, 19598, 19607});
		assertDoseFamily("potion.attack", new int[] {2428, 121, 123, 125});
		assertDoseFamily("potion.strength", new int[] {113, 115, 117, 119});
		assertDoseFamily("potion.defence", new int[] {2432, 133, 135, 137});
		assertDoseFamily("potion.combat", new int[] {9739, 9741, 9743, 9745});
		assertDoseFamily("potion.super_attack", new int[] {2436, 145, 147, 149});
		assertDoseFamily("potion.super_strength", new int[] {2440, 157, 159, 161});
		assertDoseFamily("potion.super_defence", new int[] {2442, 163, 165, 167});
		assertDoseFamily("potion.super_combat", new int[] {12695, 12697, 12699, 12701});
		assertDoseFamily("potion.ranging", new int[] {2444, 169, 171, 173});
		assertDoseFamily("potion.magic", new int[] {3040, 3042, 3044, 3046});
		assertDoseFamily("potion.restore", new int[] {2430, 127, 129, 131});
		assertDoseFamily("potion.prayer", new int[] {2434, 139, 141, 143});
		assertDoseFamily("potion.super_restore", new int[] {3024, 3026, 3028, 3030});
		assertDoseFamily("potion.energy", new int[] {3008, 3010, 3012, 3014});
		assertDoseFamily("potion.super_energy", new int[] {3016, 3018, 3020, 3022});
		assertDoseFamily("potion.stamina", new int[] {12625, 12627, 12629, 12631});
		assertDoseFamily("potion.antipoison", new int[] {2446, 175, 177, 179});
		assertDoseFamily("potion.superantipoison", new int[] {2448, 181, 183, 185});
		assertDoseFamily("potion.antifire", new int[] {2452, 2454, 2456, 2458});
		assertDoseFamily("potion.anti_venom", new int[] {12905, 12907, 12909, 12911});
		assertDoseFamily("potion.anti_venom_plus", new int[] {12913, 12915, 12917, 12919});
		assertDoseFamily("potion.saradomin_brew", new int[] {6685, 6687, 6689, 6691});
	}

	@Test
	public void excludesMinigameAndDeadmanLookalikeIdsFromCanonicalFamilies()
	{
		int[] excludedIds = {
			20393, 20394, 20395, 20396,
			20548, 20549, 20550, 20551,
			23543, 23545, 23547, 23549,
			23551, 23553, 23555, 23557,
			23559, 23561, 23563, 23565,
			23567, 23569, 23571, 23573,
			23575, 23577, 23579, 23581,
			23583, 23585, 23587, 23589,
			26150, 26151, 26152, 26153
		};

		for (int itemId : excludedIds)
		{
			assertFalse("context-only ID must not be curated: " + itemId,
				ResourceItemSortMetadataCatalog.INSTANCE.findById(itemId).isPresent());
		}
	}

	@Test
	public void everyCuratedIdExistsAndUsesItsIdBackedClassification()
	{
		Set<String> usedSourceKeys = new LinkedHashSet<>();
		for (ItemSortMetadata metadata : ResourceItemSortMetadataCatalog.INSTANCE.entries())
		{
			usedSourceKeys.add(metadata.getSourceKey());
			assertTrue("missing registry item " + metadata.getItemId(),
				ResourceItemRegistry.INSTANCE.findById(metadata.getItemId()).isPresent());
			CatalogItem item = ResourceItemRegistry.INSTANCE.findById(metadata.getItemId()).get();
			if (metadata.isFood())
			{
				assertEquals("category of " + item.getDisplayName(), ItemCategory.POTION, item.getCategory());
				assertEquals("subcategory of " + item.getDisplayName(), "food", item.getSubcategory());
			}
			else if (metadata.getVariantKind() == ItemSortMetadata.VariantKind.DOSE)
			{
				assertEquals("category of " + item.getDisplayName(), ItemCategory.POTION, item.getCategory());
				assertEquals("subcategory of " + item.getDisplayName(),
					"potion-dose-" + metadata.getVariantValue(), item.getSubcategory());
			}
		}
		assertEquals(ResourceItemSortMetadataCatalog.INSTANCE.sourceKeys(), usedSourceKeys);
	}

	@Test
	public void acceptsNonFoodPotionDoseFactsIncludingAreaRestrictions()
	{
		String rows =
			"2434\tpotion.prayer\tDOSE\t4\tNONE\tNONE\t0\t0\t0\tNONE\ttest-source\n" +
			"24598\tpotion.blighted_super_restore\tDOSE\t4\tNONE\tNONE\t0\t0\t0\tBLIGHTED_AREAS\ttest-source\n";
		Map<Integer, ItemSortMetadata> loaded = ResourceItemSortMetadataCatalog.loadMetadata(
			stream(SCHEMA + rows), Collections.singleton("test-source"));

		assertEquals(2, loaded.size());
		assertEquals(ItemSortMetadata.AreaRestriction.BLIGHTED_AREAS,
			loaded.get(24598).getAreaRestriction());
	}

	@Test
	public void rejectsDuplicateIdsAndReportsTheLine()
	{
		String row = validRow(385);
		assertInvalid(row + row, "line 3", "duplicate item_id");
	}

	@Test
	public void rejectsUnknownSourcesAndInvalidHealingModels()
	{
		assertInvalid("385\tfood.shark\tNONE\t0\tSTANDARD\tFIXED\t20\t20\t0\tNONE\tmissing\n",
			"unknown sourceKey", "missing");
		assertInvalid("385\tfood.shark\tNONE\t0\tSTANDARD\tFIXED\t19\t20\t0\tNONE\ttest-source\n",
			"FIXED healing", "equal");
		assertInvalid("385\tfood.shark\tSERVINGS\t2\tSTANDARD\tFIXED\t20\t20\t0\tNONE\ttest-source\n",
			"SERVINGS metadata", "MULTI_BITE");
		assertInvalid("385\titem.example\tSERVINGS\t2\tNONE\tNONE\t0\t0\t0\tNONE\ttest-source\n",
			"SERVINGS metadata", "MULTI_BITE");
		assertInvalid("2434\tpotion.prayer\tDOSE\t0\tNONE\tNONE\t0\t0\t0\tNONE\ttest-source\n",
			"DOSE variant", "between 1 and 4");
		assertInvalid("2434\tpotion.prayer\tDOSE\t5\tNONE\tNONE\t0\t0\t0\tNONE\ttest-source\n",
			"DOSE variant", "between 1 and 4");
		assertInvalid("199\therb.guam\tWORKFLOW_STAGE\t-1\tNONE\tNONE\t0\t0\t0\tNONE\ttest-source\n",
			"WORKFLOW_STAGE variant", "negative");
		assertInvalid("2434\tpotion.prayer\tDOSE\t4\tNONE\tFIXED\t1\t1\t0\tNONE\ttest-source\n",
			"non-food metadata", "healing facts");
	}

	@Test
	public void rejectsBankFillerMetadataExplicitly()
	{
		assertInvalid(validRow(ItemID.BANK_FILLER), "Bank Filler", "forbidden");
	}

	@Test
	public void validatesSourceManifestShapeDatesAndDuplicates()
	{
		String valid = "test-source\thttps://oldschool.runescape.wiki/w/Food?oldid=1\t2026-07-13\t1\tCC BY-NC-SA 3.0\n";
		Set<String> loaded = ResourceItemSortMetadataCatalog.loadSourceKeys(stream(SCHEMA + valid));
		assertEquals(Collections.singleton("test-source"), loaded);

		assertInvalidSource(valid + valid, "line 3", "duplicate source_key");
		assertInvalidSource("test-source\thttp://example.com\t2026-07-13\t1\tlicense\n",
			"HTTPS", "source_url");
		assertInvalidSource("test-source\thttps://example.com\tnot-a-date\t1\tlicense\n",
			"line 2", "not-a-date");
		assertInvalidSource("Bad Source\thttps://example.com\t2026-07-13\t1\tlicense\n",
			"source_key", "lowercase stable key");
		assertInvalidSource("test-source\thttps://oldschool.runescape.wiki/w/Food?oldid=2\t2026-07-13\t1\tlicense\n",
			"oldid", "revision");
		assertInvalidSource("test-source\thttps://oldschool.runescape.wiki/w/Food?oldid=abc\t2026-07-13\tabc\tCC BY-NC-SA 3.0\n",
			"revision", "numeric");
		assertInvalidSource("test-source\thttps://oldschool.runescape.wiki/w/Food?oldid=1\t2026-07-13\t1\tunknown\n",
			"license", "CC BY-NC-SA 3.0");
	}

	@Test
	public void acceptsBomBeforeSchemaAndRejectsMissingOrUnsupportedSchemas()
	{
		Map<Integer, ItemSortMetadata> loaded = ResourceItemSortMetadataCatalog.loadMetadata(
			stream("\uFEFF" + SCHEMA + validRow(385)), Collections.singleton("test-source"));
		assertTrue(loaded.containsKey(385));

		assertSchemaInvalid(validRow(385), "first non-empty line", "schema=1");
		assertSchemaInvalid("# schema=2\n" + validRow(385), "first non-empty line", "schema=1");
	}

	private static String validRow(int itemId)
	{
		return itemId + "\tfood.shark\tNONE\t0\tSTANDARD\tFIXED\t20\t20\t0\tNONE\ttest-source\n";
	}

	private static void assertDoseFamily(String familyKey, int[] itemIds)
	{
		for (int index = 0; index < itemIds.length; index++)
		{
			ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE.findById(itemIds[index]).get();
			assertEquals(familyKey, metadata.getFamilyKey());
			assertEquals(ItemSortMetadata.VariantKind.DOSE, metadata.getVariantKind());
			assertEquals(4 - index, metadata.getVariantValue());
		}
	}

	private static void assertWorkflowFamily(String familyKey, int grimyItemId, int cleanItemId,
		int unfinishedItemId)
	{
		ItemSortMetadata grimy = ResourceItemSortMetadataCatalog.INSTANCE.findById(grimyItemId).get();
		ItemSortMetadata clean = ResourceItemSortMetadataCatalog.INSTANCE.findById(cleanItemId).get();
		ItemSortMetadata unfinished = ResourceItemSortMetadataCatalog.INSTANCE.findById(unfinishedItemId).get();
		assertEquals(familyKey, grimy.getFamilyKey());
		assertEquals(familyKey, clean.getFamilyKey());
		assertEquals(familyKey, unfinished.getFamilyKey());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, grimy.getVariantKind());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, clean.getVariantKind());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, unfinished.getVariantKind());
		assertEquals(0, grimy.getVariantValue());
		assertEquals(1, clean.getVariantValue());
		assertEquals(3, unfinished.getVariantValue());
	}

	private static void assertResourceWorkflowFamily(String familyKey, int rawItemId, int processedItemId)
	{
		ItemSortMetadata raw = ResourceItemSortMetadataCatalog.INSTANCE.findById(rawItemId).get();
		ItemSortMetadata processed = ResourceItemSortMetadataCatalog.INSTANCE.findById(processedItemId).get();
		assertEquals(familyKey, raw.getFamilyKey());
		assertEquals(familyKey, processed.getFamilyKey());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, raw.getVariantKind());
		assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, processed.getVariantKind());
		assertEquals(0, raw.getVariantValue());
		assertEquals(1, processed.getVariantValue());
	}

	private static void assertOrderedWorkflowFamily(String familyKey, int[] itemIds)
	{
		for (int index = 0; index < itemIds.length; index++)
		{
			ItemSortMetadata metadata = ResourceItemSortMetadataCatalog.INSTANCE
				.findById(itemIds[index]).get();
			assertEquals(familyKey, metadata.getFamilyKey());
			assertEquals(ItemSortMetadata.VariantKind.WORKFLOW_STAGE, metadata.getVariantKind());
			assertEquals(index, metadata.getVariantValue());
		}
	}

	private static void assertInvalid(String content, String... messageParts)
	{
		try
		{
			Map<Integer, ItemSortMetadata> ignored = ResourceItemSortMetadataCatalog.loadMetadata(
				stream(SCHEMA + content), Collections.singleton("test-source"));
			fail("expected invalid metadata, loaded " + ignored.size() + " rows");
		}
		catch (IllegalStateException ex)
		{
			for (String part : messageParts)
			{
				assertTrue("message should contain " + part + ": " + ex.getMessage(),
					ex.getMessage().contains(part));
			}
		}
	}

	private static void assertInvalidSource(String content, String... messageParts)
	{
		try
		{
			ResourceItemSortMetadataCatalog.loadSourceKeys(stream(SCHEMA + content));
			fail("expected invalid source metadata");
		}
		catch (IllegalStateException ex)
		{
			for (String part : messageParts)
			{
				assertTrue("message should contain " + part + ": " + ex.getMessage(),
					ex.getMessage().contains(part));
			}
		}
	}

	private static void assertSchemaInvalid(String content, String... messageParts)
	{
		try
		{
			ResourceItemSortMetadataCatalog.loadMetadata(stream(content), Collections.singleton("test-source"));
			fail("expected invalid schema");
		}
		catch (IllegalStateException ex)
		{
			for (String part : messageParts)
			{
				assertTrue("message should contain " + part + ": " + ex.getMessage(),
					ex.getMessage().contains(part));
			}
		}
	}

	private static ByteArrayInputStream stream(String value)
	{
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}
}

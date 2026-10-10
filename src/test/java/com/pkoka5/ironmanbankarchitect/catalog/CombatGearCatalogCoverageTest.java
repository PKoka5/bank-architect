package com.pkoka5.ironmanbankarchitect.catalog;

import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.PresetCategoryMapper;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Public Wiki armour families reviewed on 2026-10-09. Exact identities were checked against
 * the TOP_LEVEL rows in docs/research/item-id-research-index.tsv. This fixture is a scoped
 * ordinary-equipment reference, not a claim that every game/cache record has been reviewed.
 */
public class CombatGearCatalogCoverageTest
{
	private static final Map<String, List<Integer>> REVIEWED_ARMOUR = reviewedArmour();

	@Test
	public void reviewedOrdinaryArmourPiecesAreFunctionalGearInBothPresets()
	{
		List<String> failures = new ArrayList<>();
		for (Map.Entry<String, List<Integer>> family : REVIEWED_ARMOUR.entrySet())
		{
			for (int itemId : family.getValue())
			{
				CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId);
				String context = family.getKey() + ": " + itemId + " " + item.getDisplayName();
				if (item.getCategory() != ItemCategory.GEAR)
				{
					failures.add(context + " category=" + item.getCategory());
				}
				for (BankPreset preset : new BankPreset[]{BankPresets.IRONMAN, BankPresets.MAIN})
				{
					String destination = PresetCategoryMapper.map(preset, item).getKey();
					if (!"combat-gear".equals(destination))
					{
						failures.add(context + " preset=" + preset.getKey() + " destination=" + destination);
					}
				}
			}
		}
		assertTrue(String.join("\n", failures), failures.isEmpty());
	}

	@Test
	public void reviewedFamiliesReachTheSameDictionaryUsedByTheGearLayout()
	{
		List<List<Integer>> actualFamilies = GearSetSemanticRuleSet.gearSetsInSlotOrder();
		List<String> missing = new ArrayList<>();
		for (Map.Entry<String, List<Integer>> family : REVIEWED_ARMOUR.entrySet())
		{
			if (actualFamilies.stream().noneMatch(ids -> ids.containsAll(family.getValue())))
			{
				missing.add(family.getKey() + " " + family.getValue());
			}
		}
		assertTrue("Missing complete families:\n" + String.join("\n", missing), missing.isEmpty());
	}

	@Test
	public void previouslyMisroutedWearablesAndWeaponsHaveTheirActualSlots()
	{
		assertGearSlots("hands", 3391, 25392, 30082);
		assertGearSlots("head", 6326, 7400, 26531, 29566, 10342,
			10547, 10548, 10549, 10550, 10374, 10382, 10390, 12496, 12504, 12512);
		assertGearSlots("body", 10330);
		assertGearSlots("legs", 7398, 10340);
		assertGearSlots("weapon", 13652, 22324, 25734, 22978, 28338, 28919, 28922);
	}

	@Test
	public void reviewedUnusualNamesReferToTheOrdinaryItemsInsteadOfCertificateRecords()
	{
		assertName(6326, "Snakeskin bandana");
		assertName(7400, "Enchanted hat");
		assertName(7399, "Enchanted top");
		assertName(7398, "Enchanted robe");
		assertName(26531, "Mystic hat (or)");
		assertName(30082, "Hueycoatl hide vambraces");
		assertName(10330, "3rd age range top");
		assertName(10342, "3rd age mage hat");
		assertName(13652, "Dragon claws");
		assertName(22324, "Ghrazi rapier");
		assertName(25734, "Holy ghrazi rapier");
		assertName(22978, "Dragon hunter lance");
		assertName(28338, "Soulreaper axe");
		assertName(28919, "Tonalztics of ralos (uncharged)");
		assertName(28922, "Tonalztics of ralos");

		Set<Integer> layoutMembers = new LinkedHashSet<>();
		for (List<Integer> family : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			layoutMembers.addAll(family);
		}
		// Certificates, placeholders, and the ornament kit cannot stand in for worn pieces.
		for (int itemId : new int[]{7401, 7402, 7403, 26532, 26534, 26536, 26538, 26540, 26541,
			30751, 30752, 30754, 30755, 30757, 30758})
		{
			assertTrue("Non-equipment record " + itemId + " in a gear family", !layoutMembers.contains(itemId));
		}
	}

	private static void assertGearSlots(String expectedSlot, int... itemIds)
	{
		List<String> failures = new ArrayList<>();
		for (int itemId : itemIds)
		{
			CatalogItem item = CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId);
			if (item.getCategory() != ItemCategory.GEAR || !expectedSlot.equals(item.getSubcategory()))
			{
				failures.add(itemId + " " + item.getDisplayName() + ": "
					+ item.getCategory() + "/" + item.getSubcategory() + " expected GEAR/" + expectedSlot);
			}
		}
		assertTrue(String.join("\n", failures), failures.isEmpty());
	}

	private static void assertName(int itemId, String expectedName)
	{
		CatalogItem item = ResourceItemRegistry.INSTANCE.describeOrUnknown(itemId);
		assertEquals("Ordinary item " + itemId, expectedName.toLowerCase(Locale.ROOT),
			item.getDisplayName().toLowerCase(Locale.ROOT));
	}

	private static Map<String, List<Integer>> reviewedArmour()
	{
		Map<String, List<Integer>> families = new LinkedHashMap<>();
		family(families, "Oathplate", 30750, 30753, 30756);
		family(families, "Radiant Oathplate", 30777, 30779, 30781);
		family(families, "Armadyl", 11826, 11828, 11830);
		family(families, "White", 6623, 6617, 6625, 6627, 6633);
		family(families, "Granite", 10589, 10564, 6809, 3122);
		family(families, "Yak-hide", 10822, 10824);
		family(families, "Rock-shell", 6128, 6129, 6130, 6151, 6145);
		family(families, "Spined", 6131, 6133, 6135, 6149, 6143);
		family(families, "Skeletal", 6137, 6139, 6141, 6153, 6147);
		family(families, "Snakeskin", 6326, 6322, 6324, 22272, 6330, 6328);
		family(families, "Studded", 1133, 1097);
		family(families, "Studded gold", 7362, 7366);
		family(families, "Studded trimmed", 7364, 7368);
		family(families, "Frog-leather", 10954, 10956, 10958);
		family(families, "Splitbark", 3385, 3387, 3389, 3391, 3393);
		family(families, "Swampbark", 25398, 25389, 25401, 25392, 25395);
		family(families, "Mystic dark", 4099, 4101, 4103, 4105, 4107);
		family(families, "Mystic light", 4109, 4111, 4113, 4115, 4117);
		family(families, "Mystic dusk", 23047, 23050, 23053, 23056, 23059);
		family(families, "Mystic shattered", 26531, 26533, 26535, 26537, 26539);
		family(families, "Enchanted", 7400, 7399, 7398);
		family(families, "Darkness", 20128, 20131, 20137, 20134, 20140);
		family(families, "Dark squall", 29566, 29568, 29570);
		family(families, "Hueycoatl", 30073, 30076, 30079, 30082);
		family(families, "Third age melee", 10350, 10348, 10352, 23242, 10346);
		family(families, "Third age ranged", 10334, 10330, 10332, 10336);
		family(families, "Third age magic", 10342, 10338, 10340, 10344);
		family(families, "Ancient blessed dragonhide", 12496, 12492, 12494, 23197, 12490, 19921);
		family(families, "Armadyl blessed dragonhide", 12512, 12508, 12510, 23200, 12506, 19930);
		family(families, "Bandos blessed dragonhide", 12504, 12500, 12502, 23203, 12498, 19924);
		family(families, "Guthix blessed dragonhide", 10382, 10378, 10380, 23188, 10376, 19927);
		family(families, "Saradomin blessed dragonhide", 10390, 10386, 10388, 23191, 10384, 19933);
		family(families, "Zamorak blessed dragonhide", 10374, 10370, 10372, 23194, 10368, 19936);
		return families;
	}

	private static void family(Map<String, List<Integer>> families, String name, Integer... itemIds)
	{
		families.put(name, Arrays.asList(itemIds));
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class CombatGearFactsTest
{
	@Test
	public void catalogCoversReviewedMechanicDrivenLoadouts()
	{
		Set<String> keys = new HashSet<>();
		for (CombatGearFacts.LoadoutFact loadout : CombatGearFacts.loadouts())
		{
			assertTrue(keys.add(loadout.getKey()));
			assertFalse(loadout.getRoles().isEmpty());
			assertTrue(loadout.getRoles().stream().anyMatch(CombatGearFacts.Role::isRequired));
		}

		assertEquals(new HashSet<>(Arrays.asList(
			"crystal-ranged", "blood-moon", "eclipse-moon", "blue-moon",
			"ahrim", "dharok", "guthan", "karil", "torag", "verac",
			"void-melee", "void-ranged", "void-magic", "justiciar", "inquisitor",
			"obsidian", "swampbark", "bloodbark", "serpentine-ranged",
			"serpentine-magic")), keys);
	}

	@Test
	public void activeVariantsNeverAlsoAppearInTheMaintenanceCatalog()
	{
		Set<Integer> unusable = CombatGearFacts.unusableItemIds();
		for (CombatGearFacts.LoadoutFact loadout : CombatGearFacts.loadouts())
		{
			for (CombatGearFacts.Role role : loadout.getRoles())
			{
				for (int itemId : role.getItemIds())
				{
					assertFalse(loadout.getKey() + " contains unusable item " + itemId,
						unusable.contains(itemId));
				}
			}
		}
	}

	@Test
	public void reviewedDamagedTorvaStatesRequireMaintenance()
	{
		Set<Integer> unusable = CombatGearFacts.unusableItemIds();
		assertTrue(unusable.containsAll(Arrays.asList(26376, 26378, 26380)));
		assertTrue(unusable.containsAll(Arrays.asList(12929, 13196, 13198)));
	}

	@Test
	public void incompleteFunctionalSetsNeedThreeRolesButExplicitFamiliesNeedTwo()
	{
		assertEquals(3, family("karil").getMinimumRoles());
		assertEquals(3, family("verac").getMinimumRoles());
		assertEquals(2, family("ghostly").getMinimumRoles());
		assertEquals(2, family("elite-black").getMinimumRoles());
		assertEquals(2, family("shayzien-5").getMinimumRoles());
		assertEquals(2, family("dwarf-cannon").getMinimumRoles());
	}

	@Test
	public void utilityFactsDistinguishStandaloneItemsFromActivatedLoadouts()
	{
		CombatGearUtilityCatalog utility = CombatGearUtilityCatalog.INSTANCE;
		assertEquals(0, utility.itemScore(11664));
		assertEquals(250, utility.loadoutScore("void-ranged"));
		assertEquals(-300, utility.itemScore(4734));
		assertEquals(0, utility.activeItemScore(4734));
	}

	@Test
	public void rejectsUnknownRolesAndItemIdsSharedAcrossRoles()
	{
		assertCatalogRejected(
			"# schema=1\nrequired\ttest\tmelee\ttiara\t1\n",
			"Invalid combat gear role");
		assertCatalogRejected(
			"# schema=1\nrequired\ttest\tmelee\thead\t1\nrequired\ttest\tmelee\tbody\t1\n",
			"belongs to both");
	}

	@Test
	public void utilityCatalogRejectsUnknownLoadoutKeys()
	{
		try
		{
			CombatGearUtilityCatalog.load(stream(
				"# schema=2\nloadout\t100\tmissing-loadout\treason\n"),
				Collections.singleton("known-loadout"));
			throw new AssertionError("expected unknown loadout key to be rejected");
		}
		catch (IllegalStateException expected)
		{
			assertTrue(expected.getMessage(), expected.getMessage().contains("invalid utility loadout key"));
		}
	}

	@Test
	public void familyThresholdCountsDistinctRolesRatherThanVariants()
	{
		Map<Integer, BankPreviewItem> onlyKarilWeapons = items(
			4734, "Karil's crossbow", 4934, "Karil's crossbow 100",
			4935, "Karil's crossbow 75");
		Map<Integer, BankPreviewItem> onlyVoidHelms = items(
			11663, "Void mage helm", 11664, "Void ranger helm",
			11665, "Void melee helm");

		assertFalse(hasFamily(CombatLoadoutResolver.resolve(onlyKarilWeapons,
			GearStatsSource.NONE), "karil"));
		assertFalse(hasFamily(CombatLoadoutResolver.resolve(onlyVoidHelms,
			GearStatsSource.NONE), "void-family"));
	}

	private static boolean hasFamily(CombatLoadoutResolver.Relationships relationships,
		String key)
	{
		return relationships.families().stream().anyMatch(family -> key.equals(family.key()));
	}

	private static Map<Integer, BankPreviewItem> items(Object... values)
	{
		Map<Integer, BankPreviewItem> items = new LinkedHashMap<>();
		for (int index = 0; index < values.length; index += 2)
		{
			int itemId = (Integer) values[index];
			String name = (String) values[index + 1];
			CatalogItem catalogItem = new CatalogItem(itemId, name, ItemCategory.GEAR,
				"gear", Collections.emptySet(), null);
			items.put(itemId, new BankPreviewItem(catalogItem, 1));
		}
		return items;
	}

	private static void assertCatalogRejected(String catalog, String message)
	{
		try
		{
			CombatGearFacts.load(stream(catalog));
			throw new AssertionError("expected catalog to be rejected");
		}
		catch (IllegalStateException expected)
		{
			assertTrue(expected.getMessage(), expected.getMessage().contains(message));
		}
	}

	private static ByteArrayInputStream stream(String value)
	{
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}

	private static CombatGearFacts.FamilyFact family(String key)
	{
		for (CombatGearFacts.FamilyFact family : CombatGearFacts.families())
		{
			if (key.equals(family.getKey())) return family;
		}
		throw new AssertionError("missing family " + key);
	}
}

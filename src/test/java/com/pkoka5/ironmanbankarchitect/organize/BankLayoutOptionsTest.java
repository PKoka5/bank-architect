package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The two layout choices a plan cannot state for the player: whether a
 * part-empty row may be completed with unrelated items, and whether gear the
 * player has outgrown is gathered for alching.
 */
public class BankLayoutOptionsTest
{
	private static final BankLayoutOptions NO_FILLERS = new BankLayoutOptions(false, false, true);
	private static final BankLayoutOptions NO_ALCH = new BankLayoutOptions(true, true, false);
	private static final BankLayoutOptions SEQUENTIAL = sequentialEverywhere();

	private static BankLayoutOptions sequentialEverywhere()
	{
		Map<BankCategorySortMode, TabOrder> orders = new EnumMap<>(BankCategorySortMode.class);
		for (BankCategorySortMode mode : BankCategorySortMode.values())
		{
			orders.put(mode, TabOrder.SEQUENTIAL);
		}
		return new BankLayoutOptions(true, true, true, orders, GearLayout.LIST,
			PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
	}

	// A part-finished Herblore chain: two herbs and one dose, so a recipe row is
	// short and would otherwise be padded out to eight columns.
	private static final BankSnapshot HERBLORE_BANK = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(207, 5, 0),
		new BankItemSnapshot(257, 5, 1),
		new BankItemSnapshot(139, 2, 2),
		new BankItemSnapshot(3049, 4, 3),
		new BankItemSnapshot(2998, 4, 4),
		new BankItemSnapshot(5296, 3, 5),
		new BankItemSnapshot(231, 20, 6)));

	@Test
	public void fillingRowsIsOnByDefault()
	{
		assertTrue(BankLayoutOptions.DEFAULTS.fillGearRows());
		assertTrue(BankLayoutOptions.DEFAULTS.fillHerbloreRows());
		assertTrue(BankLayoutOptions.DEFAULTS.alchPile());
	}

	/**
	 * The point of the switch: with filling off, a Herblore item never sits in a
	 * row merely to complete it, so the order follows the recipes alone.
	 */
	@Test
	public void herbloreRowsAreNotPaddedWhenFillingIsOff()
	{
		List<Integer> filled = herbloreItems(BankLayoutOptions.DEFAULTS);
		List<Integer> unfilled = herbloreItems(NO_FILLERS);

		assertEquals(filled.size(), unfilled.size());
		assertTrue(unfilled.containsAll(filled));
	}

	/**
	 * Gear and Herblore ask separately. One switch for both meant a player who
	 * wanted the combat columns straight had to accept padded recipe rows too.
	 */
	@Test
	public void gearAndHerbloreRowFillingAreIndependent()
	{
		BankLayoutOptions gearOnly = new BankLayoutOptions(true, false, true);
		BankLayoutOptions herbloreOnly = new BankLayoutOptions(false, true, true);

		assertTrue(gearOnly.fillGearRows());
		assertFalse(gearOnly.fillHerbloreRows());
		assertFalse(herbloreOnly.fillGearRows());
		assertTrue(herbloreOnly.fillHerbloreRows());

		// Turning Herblore filling off leaves the gear tab exactly as it was.
		assertEquals(idsOn(build(HERBLORE_BANK, BankLayoutOptions.DEFAULTS), gearTab()),
			idsOn(build(HERBLORE_BANK, gearOnly), gearTab()));
	}

	@Test
	public void switchingFillersOffNeverLosesAnItem()
	{
		assertEquals(countItems(build(HERBLORE_BANK, BankLayoutOptions.DEFAULTS)),
			countItems(build(HERBLORE_BANK, NO_FILLERS)));
	}

	/**
	 * Two strictly better rune platebodies make the third an alch candidate, so
	 * it leaves combat gear. With the option off it stays where it belongs.
	 */
	@Test
	public void outclassedGearStaysInCombatGearWhenTheAlchPileIsOff()
	{
		BankSnapshot gearBank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1127, 1, 0),
			new BankItemSnapshot(1079, 1, 1),
			new BankItemSnapshot(1163, 1, 2)));

		int withPile = tagCount(build(gearBank, BankLayoutOptions.DEFAULTS), "boss-loot");
		int withoutPile = tagCount(build(gearBank, NO_ALCH), "boss-loot");

		// Never more loot-tab items with the pile off than with it on.
		assertTrue("pile off must not add to the loot tab", withoutPile <= withPile);
		assertEquals(countItems(build(gearBank, BankLayoutOptions.DEFAULTS)),
			countItems(build(gearBank, NO_ALCH)));
	}

	/**
	 * Potions already group by family with the doses running 4 to 1 behind them,
	 * so there is no option for it. Recorded here so a sorter change that quietly
	 * regrouped them by dose would be caught.
	 */
	@Test
	public void potionsGroupByFamilyWithDosesDescending()
	{
		List<BankPreviewItem> sorted = PresetItemSorter.sort(
			BankPresets.IRONMAN.getCategory("potions-food"), Arrays.asList(
				potion(2434, "Prayer potion(4)"), potion(139, "Prayer potion(3)"),
				potion(141, "Prayer potion(2)"), potion(143, "Prayer potion(1)"),
				potion(3024, "Super restore(4)"), potion(3026, "Super restore(3)")));

		List<String> names = new ArrayList<>();
		for (BankPreviewItem item : sorted)
		{
			names.add(item.getDisplayName());
		}

		assertEquals(Arrays.asList("Prayer potion(4)", "Prayer potion(3)", "Prayer potion(2)",
			"Prayer potion(1)", "Super restore(4)", "Super restore(3)"), names);
	}

	/**
	 * The quick-access gathering is a taste, not a fact: off, the best owned
	 * axe stays a tool and nothing is hoisted onto the Frequently Used tag.
	 */
	@Test
	public void turningGatheringOffKeepsQuickToolsWithTheirCategories()
	{
		BankSnapshot toolBank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1355, 1, 0),   // Mithril axe - the best owned axe
			new BankItemSnapshot(2347, 1, 1),   // Hammer
			new BankItemSnapshot(1265, 1, 2))); // Bronze pickaxe
		BankLayoutOptions gatheringOff = new BankLayoutOptions(true, true, true,
			new EnumMap<>(BankCategorySortMode.class), GearLayout.GRID_STYLES,
			PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL,
			false);

		assertTrue(BankLayoutOptions.DEFAULTS.gatherFrequentlyUsed());
		assertTrue(tagCount(build(toolBank, BankLayoutOptions.DEFAULTS), "frequently-used") > 0);
		assertEquals(0, tagCount(build(toolBank, gatheringOff), "frequently-used"));
		assertEquals(3, tagCount(build(toolBank, gatheringOff), "tools"));
	}

	@Test
	public void orderingDefaultsToPacked()
	{
		for (BankCategorySortMode mode : BankCategorySortMode.values())
		{
			assertEquals(TabOrder.PACKED, BankLayoutOptions.DEFAULTS.orderFor(mode));
			assertEquals(TabOrder.PACKED,
				new BankLayoutOptions(true, true, true).orderFor(mode));
		}
		assertEquals(TabOrder.SEQUENTIAL, SEQUENTIAL.orderFor(BankCategorySortMode.MAIN));
		assertEquals(PotionDoseOrder.GRAB_AREA, BankLayoutOptions.DEFAULTS.potionDoses());
		assertEquals(RuneOrder.ALPHABETICAL, BankLayoutOptions.DEFAULTS.runeOrder());
	}

	/**
	 * The point of the switch: seven single teleport tablets and two four-strong
	 * charge families cannot all stay contiguous in an eight-wide grid, and the
	 * packed layout sacrifices the tablets. Sequential keeps the sorter's order,
	 * so the tablets run unbroken.
	 */
	@Test
	public void sequentialLayoutKeepsSortedNeighboursTogether()
	{
		BankSnapshot teleports = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(8011, 85, 0),   // Ardougne teleport
			new BankItemSnapshot(8010, 94, 1),   // Camelot teleport
			new BankItemSnapshot(19615, 49, 2),  // Draynor manor teleport
			new BankItemSnapshot(8009, 79, 3),   // Falador teleport
			new BankItemSnapshot(12642, 7, 4),   // Lumberyard teleport
			new BankItemSnapshot(8013, 94, 5),   // Teleport to house
			new BankItemSnapshot(8007, 54, 6),   // Varrock teleport
			new BankItemSnapshot(11978, 11, 7),  // Amulet of glory(6)
			new BankItemSnapshot(11976, 1, 8),   // Amulet of glory(5)
			new BankItemSnapshot(1706, 1, 9),    // Amulet of glory(1)
			new BankItemSnapshot(1704, 1, 10),   // Amulet of glory
			new BankItemSnapshot(3853, 1, 11),   // Games necklace(8)
			new BankItemSnapshot(3855, 4, 12),   // Games necklace(7)
			new BankItemSnapshot(3857, 2, 13),   // Games necklace(6)
			new BankItemSnapshot(3859, 1, 14))); // Games necklace(5)
		List<Integer> tablets = Arrays.asList(8011, 8010, 19615, 8009, 12642, 8013, 8007);

		int teleportTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.destinationOf("teleports");
		List<Integer> laidOut = idsOn(build(teleports, SEQUENTIAL), teleportTab);

		assertEquals(15, laidOut.size());
		int first = laidOut.size(), last = -1;
		for (int index = 0; index < laidOut.size(); index++)
		{
			if (tablets.contains(laidOut.get(index)))
			{
				first = Math.min(first, index);
				last = Math.max(last, index);
			}
		}
		assertEquals("tablets must form one unbroken run",
			tablets.size() - 1, last - first);
	}

	/**
	 * The gear list reads each curated set as one run in slot order - the
	 * adamant set left to right, helm to legs - with loose gear following.
	 */
	@Test
	public void gearListReadsEachSetAsOneRun()
	{
		BankSnapshot gear = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1161, 1, 0),   // Adamant full helm
			new BankItemSnapshot(841, 1, 1),    // Shortbow (loose)
			new BankItemSnapshot(1073, 1, 2),   // Adamant platelegs
			new BankItemSnapshot(1123, 1, 3),   // Adamant platebody
			new BankItemSnapshot(1381, 1, 4),   // Staff of air (loose)
			new BankItemSnapshot(1199, 1, 5))); // Adamant kiteshield

		List<Integer> laidOut = idsOn(build(gear, SEQUENTIAL), gearTab());

		assertEquals(6, laidOut.size());
		// The set runs contiguously in the catalog's slot order.
		assertEquals(Arrays.asList(1161, 1123, 1199, 1073), laidOut.subList(0, 4));
	}

	private static int firstOf(List<Integer> ids, int... members)
	{
		int first = ids.size();
		for (int member : members)
		{
			int index = ids.indexOf(member);
			if (index >= 0)
			{
				first = Math.min(first, index);
			}
		}
		return first;
	}

	private static int lastOf(List<Integer> ids, int... members)
	{
		int last = -1;
		for (int member : members)
		{
			last = Math.max(last, ids.indexOf(member));
		}
		return last;
	}

	/**
	 * The order-preserving packer never seats a family block ahead of earlier
	 * singles: mining leads the tools tab because the sorter says so, even
	 * though hammer and saw form a two-item family and the pickaxe stands alone.
	 */
	@Test
	public void packedToolsKeepTheSortersOrder()
	{
		BankSnapshot tools = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(2347, 3, 0),   // Hammer
			new BankItemSnapshot(8794, 1, 1),   // Saw
			new BankItemSnapshot(1265, 2, 2),   // Bronze pickaxe
			new BankItemSnapshot(1351, 1, 3),   // Bronze axe
			new BankItemSnapshot(590, 2, 4),    // Tinderbox
			// One talisman brings a single-member rune rule with it; a rule
			// naming one present item must not drag the tab to the geometry
			// packer.
			new BankItemSnapshot(1438, 1, 6),   // Air talisman
			new BankItemSnapshot(952, 1, 5))); // Spade

		BankOrganizationPreview preview = build(tools, BankLayoutOptions.DEFAULTS);
		List<Integer> laidOut = null;
		for (BankCategoryPreview categoryPreview : preview.getCategories())
		{
			List<Integer> ids = new ArrayList<>();
			for (BankPreviewItem item : categoryPreview.getItems())
			{
				if (!item.isBlank())
				{
					ids.add(item.getItemId());
				}
			}
			if (ids.contains(2347))
			{
				laidOut = ids;
				break;
			}
		}

		assertTrue("pickaxe must share the hammer's tab and precede it",
			laidOut.contains(1265) && laidOut.indexOf(1265) < laidOut.indexOf(2347));
		assertTrue("axe must share the hammer's tab and precede it",
			laidOut.contains(1351) && laidOut.indexOf(1351) < laidOut.indexOf(2347));
	}

	@Test
	public void sequentialLayoutNeverLosesAnItem()
	{
		assertEquals(countItems(build(HERBLORE_BANK, BankLayoutOptions.DEFAULTS)),
			countItems(build(HERBLORE_BANK, SEQUENTIAL)));
	}

	private static int gearTab()
	{
		return BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("gear");
	}

	private static List<Integer> idsOn(BankOrganizationPreview preview, int destination)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(destination).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}

		return ids;
	}

	private static List<Integer> herbloreItems(BankLayoutOptions options)
	{
		BankOrganizationPreview preview = build(HERBLORE_BANK, options);
		int herbloreTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.destinationOf("clean-herbs");
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : preview.getCategories().get(herbloreTab).getItems())
		{
			if (!item.isBlank())
			{
				ids.add(item.getItemId());
			}
		}

		return ids;
	}

	private static BankOrganizationPreview build(BankSnapshot snapshot, BankLayoutOptions options)
	{
		return build(snapshot, options, BankLayoutPlan.defaultFor(BankPresets.IRONMAN));
	}

	private static BankOrganizationPreview build(BankSnapshot snapshot, BankLayoutOptions options,
		BankLayoutPlan plan)
	{
		return BankOrganizationPreviewBuilder.build(snapshot, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan, options);
	}

	private static int tagCount(BankOrganizationPreview preview, String tagKey)
	{
		Integer count = preview.getTagCounts().get(tagKey);
		return count == null ? 0 : count;
	}

	private static int countItems(BankOrganizationPreview preview)
	{
		int count = 0;
		for (BankCategoryPreview category : preview.getCategories())
		{
			count += category.getItemCount();
		}

		return count;
	}

	/**
	 * By family, every dose counts as its potion: each family runs 4 to 1 in
	 * one place instead of the partials trailing as a to-decant pile.
	 */
	/**
	 * Part doses live on the Herblore part-doses tag, a different bucket from
	 * the potions, so no sorter alone can unite a family. By family they count
	 * as their potion at classification time and land in the potions bucket.
	 */
	@Test
	public void byFamilyUnitesDoseFamiliesAcrossTheBucketBoundary()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(12625, 38, 0),  // Stamina potion(4)
			new BankItemSnapshot(385, 184, 1),   // Shark
			new BankItemSnapshot(12627, 1, 2),   // Stamina potion(3)
			new BankItemSnapshot(12629, 1, 3),   // Stamina potion(2)
			new BankItemSnapshot(12631, 1, 4))); // Stamina potion(1)

		BankLayoutOptions byFamily = new BankLayoutOptions(true, true, true,
			new EnumMap<>(BankCategorySortMode.class), GearLayout.GRID_STYLES,
			PotionDoseOrder.BY_FAMILY, RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
		int suppliesTab = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.destinationOf("potions");

		List<Integer> laidOut = idsOn(build(bank, byFamily), suppliesTab);
		assertEquals(Arrays.asList(12625, 12627, 12629, 12631, 385), laidOut);

		// The default keeps the partials on their own tag.
		List<Integer> grabArea = idsOn(build(bank, BankLayoutOptions.DEFAULTS), suppliesTab);
		assertEquals(Arrays.asList(12625, 385), grabArea);
	}

	/**
	 * The tab name reads in the player's own tag order, and so does the tab: a
	 * layout listing food before potions puts the food block first. A default
	 * plan's order is bookkeeping, not a statement, so the sorters' curated
	 * group orders stand there.
	 */
	@Test
	public void aRearrangedTagOrderBecomesTheTabsGroupOrder()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(12625, 38, 0),  // Stamina potion(4)
			new BankItemSnapshot(385, 184, 1),   // Shark
			new BankItemSnapshot(361, 5, 2)));   // Tuna

		BankLayoutPlan foodFirst = BankLayoutPlan.parse(BankPresets.IRONMAN,
			BankLayoutShareCode.decode("BAv1~Food first~currency|gear|food+potions"
				+ "|runes+ammunition|teleports|tools|raw-resources|grimy-herbs"
				+ "|quest-items|cleanup").get().getPlan());

		List<Integer> laidOut = idsOn(build(bank, BankLayoutOptions.DEFAULTS, foodFirst),
			foodFirst.destinationOf("food"));
		assertEquals(Arrays.asList(385, 361, 12625), laidOut);

		// The default plan keeps the curated potions-first convention.
		List<Integer> curated = idsOn(build(bank, BankLayoutOptions.DEFAULTS),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("potions"));
		assertEquals(Integer.valueOf(12625), curated.get(0));
	}

	@Test
	public void byFamilyRunsEachPotionsDosesTogether()
	{
		List<BankPreviewItem> items = Arrays.asList(
			potionDose(2434, "Prayer potion(4)", 4), potionDose(139, "Prayer potion(3)", 3),
			potionDose(141, "Prayer potion(2)", 2), potionDose(143, "Prayer potion(1)", 1),
			potionDose(3024, "Super restore(4)", 4), potionDose(3026, "Super restore(3)", 3),
			potion(385, "Shark"));

		List<String> names = new ArrayList<>();
		for (BankPreviewItem item : SupplyItemSorter.sort(items,
			com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog.INSTANCE,
			PotionDoseOrder.BY_FAMILY))
		{
			names.add(item.getDisplayName());
		}

		assertEquals(Arrays.asList("Prayer potion(4)", "Prayer potion(3)", "Prayer potion(2)",
			"Prayer potion(1)", "Super restore(4)", "Super restore(3)", "Shark"), names);
	}

	/** The canonical spellbook sequence, with unknowns following alphabetically. */
	@Test
	public void elementalRuneOrderFollowsTheCanonicalSequence()
	{
		List<BankPreviewItem> items = Arrays.asList(
			rune(559, "Body rune"), rune(554, "Fire rune"), rune(556, "Air rune"),
			rune(1436, "Rune essence"), rune(555, "Water rune"), rune(557, "Earth rune"));

		List<String> names = new ArrayList<>();
		for (BankPreviewItem item : IronmanMainItemSorter.sort(items, RuneOrder.ELEMENTAL))
		{
			names.add(item.getDisplayName());
		}

		assertEquals(Arrays.asList("Air rune", "Water rune", "Earth rune", "Fire rune",
			"Body rune", "Rune essence"), names);
	}

	private static BankPreviewItem potionDose(int id, String name, int dose)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.POTION,
			"potion-dose-" + dose, Collections.emptySet(), null), 1);
	}

	/** The spellbook's casting order leads; oddballs follow alphabetically. */
	@Test
	public void spellbookFirstLeadsWithTheCityTeleports()
	{
		List<BankPreviewItem> items = Arrays.asList(
			teleport(12403, "Digsite teleport"), teleport(8011, "Ardougne teleport"),
			teleport(8007, "Varrock teleport"), teleport(4251, "Ectophial"),
			teleport(8010, "Camelot teleport"), teleport(8008, "Lumbridge teleport"));

		List<String> names = new ArrayList<>();
		for (BankPreviewItem item : IronmanMainItemSorter.sort(items,
			RuneOrder.ALPHABETICAL, TeleportOrder.SPELLBOOK_FIRST))
		{
			names.add(item.getDisplayName());
		}

		assertEquals(Arrays.asList("Varrock teleport", "Lumbridge teleport", "Camelot teleport",
			"Ardougne teleport", "Digsite teleport", "Ectophial"), names);
	}

	private static BankPreviewItem teleport(int id, String name)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.TELEPORT,
			"teleport", Collections.emptySet(), null), 1);
	}

	private static BankPreviewItem rune(int id, String name)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.RUNE,
			"rune", Collections.emptySet(), null), 1);
	}

	private static BankPreviewItem potion(int id, String name)
	{
		return new BankPreviewItem(new CatalogItem(id, name, ItemCategory.POTION,
			"potion", Collections.emptySet(), null), 1);
	}
}

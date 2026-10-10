package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class GearProgressionConsistencyTest
{
	private static final int[][] UPGRADE_ROWS = {
		{30750, 10828, 11826, 21018},
		{30753, 10551, 11828, 21021},
		{30756, 1079, 11830, 21024},
		{29801, 6585, 19547, 12002}
	};
	private static final GearSlot[] ROW_SLOTS = {
		GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.NECK
	};
	private static final int[] RING_FILLERS = {
		2550, 2568, 6731, 6733, 6735, 6737, 11770, 11771, 11772, 11773,
		12601, 12603, 12605, 12691, 12692, 13202, 19550, 19710, 20655, 22975
	};

	@Test
	public void equivalentArmorVariantsShareAProgressionBaseWithEqualRuntimeStats()
	{
		assertEquivalent(item(1153, "Iron full helm"), item(1137, "Iron med helm"), GearSlot.HEAD);
		assertEquivalent(item(1157, "Steel full helm"), item(1141, "Steel med helm"), GearSlot.HEAD);
		assertEquivalent(item(1159, "Mithril full helm"), item(1143, "Mithril med helm"), GearSlot.HEAD);
		assertEquivalent(item(1161, "Adamant full helm"), item(1145, "Adamant med helm"), GearSlot.HEAD);
		assertEquivalent(item(1067, "Iron platelegs"), item(1081, "Iron plateskirt"), GearSlot.LEGS);
		assertEquivalent(item(1069, "Steel platelegs"), item(1083, "Steel plateskirt"), GearSlot.LEGS);
		assertEquivalent(item(1071, "Mithril platelegs"), item(1085, "Mithril plateskirt"), GearSlot.LEGS);
		assertEquivalent(item(1073, "Adamant platelegs"), item(1091, "Adamant plateskirt"), GearSlot.LEGS);
		assertEquivalent(item(1079, "Rune platelegs"), item(1093, "Rune plateskirt"), GearSlot.LEGS);
		assertEquivalent(item(1163, "Rune full helm"), item(1147, "Rune med helm"), GearSlot.HEAD);
		assertEquivalent(item(1127, "Rune platebody"), item(1113, "Rune chainbody"), GearSlot.BODY);
		assertEquivalent(item(1123, "Adamant platebody"), item(1111, "Adamant chainbody"), GearSlot.BODY);
	}

	@Test
	public void trimmedBlackWizardGearSharesTheBlueWizardProgressionBase()
	{
		assertEquivalent(item(577, "Blue wizard robe"), item(12449, "Black wizard robe (g)"), GearSlot.BODY);
		assertEquivalent(item(577, "Blue wizard robe"), item(12451, "Black wizard robe (t)"), GearSlot.BODY);
		assertEquivalent(item(579, "Blue wizard hat"), item(12453, "Black wizard hat (g)"), GearSlot.HEAD);
		assertEquivalent(item(579, "Blue wizard hat"), item(12455, "Black wizard hat (t)"), GearSlot.HEAD);
	}

	@Test
	public void anUnlistedRuneVariantUsesTheSameProgressionScaleAsCuratedRuneGear()
	{
		BankPreviewItem unlisted = item(963001, "Rune regression armour");
		assertFalse("fixture must exercise the name heuristic rather than curated metadata",
			GearTierCatalog.INSTANCE.tierOf(unlisted.getItemId(), unlisted.getDisplayName()).isPresent());

		assertEquivalent(item(1127, "Rune platebody"), unlisted, GearSlot.BODY);
	}

	@Test
	public void runtimeStatDifferencesStillDecideTheOrderWithinOneProgressionStage()
	{
		BankPreviewItem stronger = item(1127, "Rune platebody");
		BankPreviewItem weaker = item(1113, "Rune chainbody");
		GearStatsSource stats = itemId -> Optional.of(melee(GearSlot.BODY, itemId == 1127 ? 3 : 1));

		assertTrue("missing curated metadata must not reward the weaker variant",
			GearItemSorter.score(stronger, stats) > GearItemSorter.score(weaker, stats));
		assertEquals(1127, GearItemSorter.layout(Arrays.asList(weaker, stronger), stats).get(0).getItemId());
	}

	@Test
	public void recentEndgameUpgradesOutrankOlderEquipmentWithModestStatAdvantages()
	{
		Map<Integer, GearStats> stats = fixtureStats();
		GearStatsSource source = itemId -> Optional.ofNullable(stats.get(itemId));
		for (int[] row : UPGRADE_ROWS)
		{
			BankPreviewItem upgrade = described(row[0]);
			BankPreviewItem older = described(row[1]);
			assertEquals("curated endgame stage for " + upgrade.getDisplayName(), 5,
				GearTierCatalog.INSTANCE.tierOf(upgrade.getItemId()).orElse(-1));
			assertTrue(upgrade.getDisplayName() + " must outrank " + older.getDisplayName(),
				GearItemSorter.score(upgrade, source) > GearItemSorter.score(older, source));
		}
	}

	@Test
	public void appearanceVariantsRetainTheirCuratedProgressionStages()
	{
		for (int itemId : new int[]{30750, 30753, 30756, 30777, 30779, 30781, 29801, 29804})
		{
			assertEquals("endgame stage for item " + itemId, 5,
				GearTierCatalog.INSTANCE.tierOf(itemId).orElse(-1));
		}
		assertEquals("Echo boots remain a late defensive option", 4,
			GearTierCatalog.INSTANCE.tierOf(28945).orElse(-1));
	}

	@Test
	public void namesAlonePlaceRecentMeleeGearIntoItsSetupRows()
	{
		List<BankPreviewItem> items = new ArrayList<>(Arrays.asList(
			item(21018, "Ancestral hat"), item(12931, "Serpentine helm"),
			item(11826, "Armadyl helmet"), item(30750, "Oathplate helm"),
			item(21021, "Ancestral robe top"), item(10551, "Fighter torso"),
			item(11828, "Armadyl chestplate"), item(30753, "Oathplate chest"),
			item(21024, "Ancestral robe bottom"), item(1079, "Rune platelegs"),
			item(11830, "Armadyl chainskirt"), item(30756, "Oathplate legs"),
			item(12002, "Occult necklace"), item(6585, "Amulet of fury"),
			item(19547, "Necklace of anguish"), item(29801, "Amulet of rancour"),
			item(4097, "Mystic boots"), item(3105, "Climbing boots"),
			item(19930, "Armadyl d'hide boots"), item(28945, "Echo boots")
		));
		for (int ringId : RING_FILLERS)
		{
			items.add(described(ringId));
		}

		List<BankPreviewItem> laidOut = GearItemSorter.layout(items, GearStatsSource.NONE);
		assertEquals("rings cannot pad armour rows; actual best pieces form style runs",
			Arrays.asList(30750, 30753, 30756, 29801, 28945, 11826, 11828, 11830,
				19547, 19930, 21018, 21021, 21024, 12002, 4097),
			laidOut.subList(0, 15).stream().map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		assertEquals("preserve every supplied bank entry", items.size(), laidOut.size());
		assertEquals(items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()),
			laidOut.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
		assertTrue("name-only layout cannot invent empty cells", laidOut.stream().noneMatch(BankPreviewItem::isBlank));
	}

	@Test
	public void wikiOathplateStatsKeepTheMeleeSetupAndLegsAheadOfRune()
	{
		// Public equipment bonuses: https://oldschool.runescape.wiki/w/Oathplate
		// and https://oldschool.runescape.wiki/w/Rune_platelegs
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(30750, new GearStats(GearSlot.HEAD, 0, 10, 0, -2, -7, 6, 0, 0,
			50, 72, 45, 0, 50, 0, 0));
		stats.put(30753, new GearStats(GearSlot.BODY, 0, 16, 0, -16, -18, 4, 0, 0,
			105, 128, 100, -5, 112, 0, 0));
		stats.put(30756, new GearStats(GearSlot.LEGS, 0, 12, 0, -12, -14, 2, 0, 0,
			75, 100, 73, -3, 81, 0, 0));
		stats.put(1079, new GearStats(GearSlot.LEGS, 0, 0, 0, -21, -11, 0, 0, 0,
			51, 49, 47, -4, 49, 0, 0));
		GearStatsSource source = itemId -> Optional.ofNullable(stats.get(itemId));
		for (int itemId : new int[]{30750, 30753, 30756})
		{
			assertEquals("Oathplate remains melee with its actual bonuses", GearStyle.MELEE, stats.get(itemId).style());
		}
		assertTrue("Oathplate legs must lead Rune legs with their actual bonuses",
			GearItemSorter.score(described(30756), source) > GearItemSorter.score(described(1079), source));
		assertEquals(Arrays.asList(30750, 30753, 30756, 1079),
			GearItemSorter.layout(Arrays.asList(described(1079), described(30756),
				described(30753), described(30750)), source).stream()
				.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
	}

	@Test
	public void defaultGridsKeepUpgradePrimariesInBothPresetsIncludingPlaceholders()
	{
		Map<Integer, GearStats> stats = fixtureStats();
		GearStatsSource source = itemId -> Optional.ofNullable(stats.get(itemId));
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			for (boolean placeholders : Arrays.asList(false, true))
			{
				BankSnapshot bank = fixtureBank(placeholders);
				BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
					CompositeItemCatalog.DEFAULT, preset, source, ItemValueSource.NONE,
					CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
				List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
				String context = preset.getType() + ", placeholders=" + placeholders;

				for (int row = 0; row < UPGRADE_ROWS.length; row++)
				{
					int position = row * 8;
					assertEquals(context + ", melee slot " + row, UPGRADE_ROWS[row][0], combat.get(position).getItemId());
					assertEquals(context + ", ranged slot " + row, UPGRADE_ROWS[row][2], combat.get(position + 1).getItemId());
					assertEquals(context + ", magic slot " + row, UPGRADE_ROWS[row][3], combat.get(position + 2).getItemId());
					assertEquals(context + ", placeholder state slot " + row, placeholders, combat.get(position).isPlaceholder());
					assertEquals(context + ", quantity slot " + row, placeholders ? 0 : 1, combat.get(position).getQuantity());
				}
				assertEquals(context + ", each real bank entry appears once", bank.getItems().size(), combat.size());
				assertEquals(context + ", item IDs are preserved",
					bank.getItems().stream().map(BankItemSnapshot::getItemId).collect(Collectors.toSet()),
					combat.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
				assertEquals(context + ", no duplicate items", combat.size(),
					new HashSet<>(combat.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList())).size());
				assertTrue(context + ", no invented empty cells", combat.stream().noneMatch(BankPreviewItem::isBlank));
			}
		}
	}

	private static void assertEquivalent(BankPreviewItem curated, BankPreviewItem variant, GearSlot slot)
	{
		String context = curated.getDisplayName() + " and " + variant.getDisplayName();
		assertEquals(context + " without stats", GearItemSorter.score(curated, GearStatsSource.NONE),
			GearItemSorter.score(variant, GearStatsSource.NONE));
		GearStatsSource equalStats = itemId -> Optional.of(melee(slot, 2));
		assertEquals(context + " with equal stats", GearItemSorter.score(curated, equalStats),
			GearItemSorter.score(variant, equalStats));
	}

	private static Map<Integer, GearStats> fixtureStats()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int row = 0; row < UPGRADE_ROWS.length; row++)
		{
			int[] ids = UPGRADE_ROWS[row];
			stats.put(ids[0], melee(ROW_SLOTS[row], 3));
			stats.put(ids[1], melee(ROW_SLOTS[row], 2));
			stats.put(ids[2], styled(ROW_SLOTS[row], GearStyle.RANGED));
			stats.put(ids[3], styled(ROW_SLOTS[row], GearStyle.MAGIC));
		}
		for (int ringId : RING_FILLERS)
		{
			stats.put(ringId, new GearStats(GearSlot.RING, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		}
		return stats;
	}

	// These complete stat vectors are synthetic test inputs, not game-stat assertions.
	private static GearStats melee(GearSlot slot, int strength)
	{
		return new GearStats(slot, 1, 1, 1, 0, 0, strength, 0, 0, 10, 10, 10, 0, 10, 0, 0);
	}

	private static GearStats styled(GearSlot slot, GearStyle style)
	{
		return new GearStats(slot, 0, 0, 0, style == GearStyle.MAGIC ? 2 : 0,
			style == GearStyle.RANGED ? 2 : 0, 0, 0, 0, 10, 10, 10, 0, 10, 0, 0);
	}

	private static BankSnapshot fixtureBank(boolean upgradePlaceholders)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int[] row : UPGRADE_ROWS)
		{
			for (int column = row.length - 1; column >= 0; column--)
			{
				boolean placeholder = upgradePlaceholders && column == 0;
				entries.add(new BankItemSnapshot(row[column], placeholder ? 0 : 1, entries.size(), placeholder));
			}
		}
		for (int ringId : RING_FILLERS)
		{
			entries.add(new BankItemSnapshot(ringId, 1, entries.size()));
		}
		return new BankSnapshot(entries);
	}

	private static BankPreviewItem described(int itemId)
	{
		return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId), 1);
	}

	private static BankPreviewItem item(int itemId, String name)
	{
		return new BankPreviewItem(new CatalogItem(itemId, name, ItemCategory.GEAR,
			"gear", Collections.emptySet(), null), 1);
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AlchReplacementFamiliesTest
{
	private static final int[][] STAFF_CHAINS = {
		{1381, 1397, 1405}, {1383, 1395, 1403}, {1385, 1399, 1407}, {1387, 1393, 1401}
	};
	private static final int[][] MYSTIC_COLORS = {
		{4089, 4099, 4109}, {4091, 4101, 4111}, {4093, 4103, 4113},
		{4095, 4105, 4115}, {4097, 4107, 4117}
	};
	private static final GearSlot[] MYSTIC_SLOTS = {
		GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS, GearSlot.HANDS, GearSlot.FEET
	};
	private static final int[] BASIC_SLASH_WEAPONS = {1373, 1319, 1303, 1333, 1289, 1213, 1371, 1317, 1331};
	private static final int[][] REPLACED_PAIRS = {
		{1275, 11920}, {1359, 6739}, {1387, 1393}, {1373, 4151}, {4103, 4093}
	};
	private static final ItemValueSource VALUES = id -> 25000;
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.IRONMAN}, {BankPresetType.MAIN}});
	}

	public AlchReplacementFamiliesTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void runeToolsMoveToAlchWithRealHigherToolsWithoutEquipmentStats()
	{
		for (int quantity : new int[] {1, 2, 25})
		{
			for (int upgrade : new int[] {11920, 12797, 23677, 25376, 30351, 13243, 23680, 13244, 25369, 30346})
			{
				BankOrganizationPreview preview = build(bank(quantity, 1275, upgrade), GearStatsSource.NONE);
				assertTag(preview, 1275, "alch");
				assertFalse("Keep pickaxe upgrade " + upgrade, "alch".equals(item(preview, upgrade).getLayoutTagKey()));
				assertEquals(1, alchCount(preview));
			}
			for (int upgrade : new int[] {6739, 13241, 23673, 28217, 28220, 13242, 25371, 30348})
			{
				BankOrganizationPreview preview = build(bank(quantity, 1359, upgrade), GearStatsSource.NONE);
				assertTag(preview, 1359, "alch");
				assertFalse("Keep axe upgrade " + upgrade, "alch".equals(item(preview, upgrade).getLayoutTagKey()));
				assertEquals(1, alchCount(preview));
			}
		}
	}

	@Test
	public void owningADragonAxeReplacesBothAdamantAndRuneAxes()
	{
		for (int quantity : new int[] {1, 25})
		{
			BankOrganizationPreview preview = build(bank(quantity, 1357, 1359, 6739), GearStatsSource.NONE);
			assertTag(preview, 1357, "alch");
			assertTag(preview, 1359, "alch");
			assertFalse("Keep Dragon axe", "alch".equals(item(preview, 6739).getLayoutTagKey()));
			assertEquals(2, alchCount(preview));
			assertEquals(3, preview.getPlannedItemCount());
		}
	}

	@Test
	public void adamantAxeRequiresAnActualUpgradeAndRespectsPlayerChoices()
	{
		for (int quantity : new int[] {1, 25})
		{
			BankSnapshot lone = bank(quantity, 1357);
			BankSnapshot placeholderUpgrade = new BankSnapshot(Arrays.asList(new BankItemSnapshot(1357, quantity, 0),
				new BankItemSnapshot(6739, 0, 1, true)));
			for (BankSnapshot bank : Arrays.asList(lone, placeholderUpgrade, bank(quantity, 1357, 23675)))
			{
				BankOrganizationPreview preview = build(bank, GearStatsSource.NONE);
				assertFalse("Keep best usable Adamant axe", "alch".equals(item(preview, 1357).getLayoutTagKey()));
				assertEquals(0, alchCount(preview));
			}
			BankSnapshot upgraded = bank(quantity, 1357, 1359, 6739);
			BankOrganizationPreview disabled = build(upgraded, GearStatsSource.NONE, VALUES,
				CategoryOverrideSource.NONE, new BankLayoutOptions(true, true, false));
			assertEquals(0, alchCount(disabled));
			CategoryOverrideSource correction = id -> id == 1357 ? Optional.of("tools") : Optional.empty();
			BankOrganizationPreview corrected = build(upgraded, GearStatsSource.NONE, VALUES, correction, defaults());
			assertTag(corrected, 1357, "tools");
			assertAt(corrected, plan.destinationOf("tools"), 1357);
			assertTag(corrected, 1359, "alch");
			assertEquals(1, alchCount(corrected));
		}
	}

	@Test
	public void equalInactiveBrokenAndUnrelatedToolsDoNotReplaceRuneTools()
	{
		for (int[] pair : new int[][] {{1275, 23276}, {1359, 23279}, {1275, 6739}, {1359, 11920},
			{1275, 27695}, {1275, 23682}, {1359, 23675}})
		{
			BankOrganizationPreview preview = build(bank(25, pair), GearStatsSource.NONE);
			assertFalse("Rune tool " + pair[0], "alch".equals(item(preview, pair[0]).getLayoutTagKey()));
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void placeholdersNeverSupplyAutomaticReplacementProof()
	{
		for (int[] pair : REPLACED_PAIRS)
		{
			BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(pair[0], 25, 0),
				new BankItemSnapshot(pair[1], 0, 1, true)));
			BankOrganizationPreview preview = build(bank, sameStats(GearSlot.WEAPON));
			assertFalse("Actual item " + pair[0], "alch".equals(item(preview, pair[0]).getLayoutTagKey()));
			assertTrue(item(preview, pair[1]).isPlaceholder());
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void aLoneBestToolStaffWeaponOrMysticColorStaysEvenInLargeStacks()
	{
		for (int id : new int[] {1275, 1359, 1387, 1393, 1401, 1373, 1333, 4093, 4103, 4113})
		{
			BankOrganizationPreview preview = build(bank(25, id), sameStats(GearSlot.WEAPON));
			assertFalse("Best owned " + id, "alch".equals(item(preview, id).getLayoutTagKey()));
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void eachElementalStaffChainUsesItsExactRoleDespiteMisleadingRawStats()
	{
		for (int[] chain : STAFF_CHAINS)
		{
			for (int lower = 0; lower < chain.length - 1; lower++)
			{
				for (int higher = lower + 1; higher < chain.length; higher++)
				{
					int candidate = chain[lower], upgrade = chain[higher];
					GearStats oldStats = fullStats(GearSlot.WEAPON, 60, 100);
					GearStats newStats = fullStats(GearSlot.WEAPON, 20, 0);
					assertEquals(GearStyle.MELEE, oldStats.style());
					assertFalse(newStats.dominates(oldStats));
					for (int quantity : new int[] {1, 2, 25})
					{
						BankOrganizationPreview preview = build(bank(quantity, candidate, upgrade),
							id -> Optional.of(id == candidate ? oldStats : newStats));
						assertTag(preview, candidate, "alch");
						assertTag(preview, upgrade, "gear");
						assertEquals(1, alchCount(preview));
					}
				}
			}
		}
	}

	@Test
	public void otherElementsAutocastFamiliesAndMeleeWeaponsCannotReplaceFireStavesByStats()
	{
		GearStats oldStats = fullStats(GearSlot.WEAPON, 10, 10);
		GearStats betterStats = fullStats(GearSlot.WEAPON, 100, 100);
		assertTrue(betterStats.dominates(oldStats));
		for (int candidate : STAFF_CHAINS[3])
		{
			for (int incompatible : new int[] {1395, 4675, 11905, 4587})
			{
				BankOrganizationPreview preview = build(bank(25, candidate, incompatible),
					id -> Optional.of(id == candidate ? oldStats : betterStats));
				assertTag(preview, candidate, "gear");
			}
		}
	}

	@Test
	public void mysticColorsKeepOneCanonicalSurvivorIndependentOfOrderAndQuantity()
	{
		int[][] permutations = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};
		for (int family = 0; family < MYSTIC_COLORS.length; family++)
		{
			int[] colors = MYSTIC_COLORS[family];
			for (int quantity : new int[] {1, 2, 25})
			{
				for (int[] order : permutations)
				{
					BankOrganizationPreview preview = build(bank(quantity, colors[order[0]], colors[order[1]], colors[order[2]]),
						sameStats(MYSTIC_SLOTS[family]));
					assertTag(preview, colors[0], "gear");
					assertTag(preview, colors[1], "alch");
					assertTag(preview, colors[2], "alch");
					assertEquals(2, alchCount(preview));
					assertEquals(3, preview.getPlannedItemCount());
					assertEquals(quantity, item(preview, colors[1]).getQuantity());
				}
			}
		}
	}

	@Test
	public void aPlayersExplicitCombatColorBecomesTheMysticSurvivor()
	{
		for (String correction : Arrays.asList("gear", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> id == 4103 ? Optional.of(correction) : Optional.empty();
			BankOrganizationPreview preview = build(bank(1, MYSTIC_COLORS[2]), sameStats(GearSlot.LEGS), VALUES,
				overrides, defaults());
			assertTag(preview, 4093, "alch");
			assertTag(preview, 4103, "gear");
			assertTag(preview, 4113, "alch");
			assertEquals(2, alchCount(preview));
		}
	}

	@Test
	public void colorsExplicitlyAssignedAnotherKnownRoleCannotBeAutomaticSurvivors()
	{
		for (String correction : Arrays.asList("alch", "tools", "cosmetics", "quest-items"))
		{
			CategoryOverrideSource overrides = id -> id == 4093 ? Optional.of(correction) : Optional.empty();
			BankOrganizationPreview preview = build(bank(1, MYSTIC_COLORS[2]), sameStats(GearSlot.LEGS), VALUES,
				overrides, defaults());
			assertTag(preview, 4093, correction);
			assertTag(preview, 4103, "gear");
			assertTag(preview, 4113, "alch");
		}
	}

	@Test
	public void capturedMysticTagsPreserveTheChosenCombatColorOnRebuilding()
	{
		BankSnapshot bank = bank(1, MYSTIC_COLORS[2]);
		CategoryOverrideSource choices = id -> Optional.of(id == 4103 ? "gear" : "alch");
		BankOrganizationPreview before = build(bank, sameStats(GearSlot.LEGS), VALUES, choices, defaults());
		Map<Integer, List<Integer>> physicalTabs = new LinkedHashMap<>();
		physicalTabs.put(plan.destinationOf("gear"), Collections.singletonList(4103));
		physicalTabs.put(plan.destinationOf("alch"), Arrays.asList(4093, 4113));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(physicalTabs, before, BlueprintItemOrders.EMPTY);
		assertTrue(captured.isCaptured());
		BankOrganizationPreview after = build(bank, sameStats(GearSlot.LEGS), VALUES, CategoryOverrideSource.NONE,
			defaults().withItemOrders(captured));
		assertTag(after, 4093, "alch");
		assertTag(after, 4103, "gear");
		assertTag(after, 4113, "alch");
		assertAt(after, plan.destinationOf("gear"), 4103);
		assertAt(after, plan.destinationOf("alch"), 4093);
		assertAt(after, plan.destinationOf("alch"), 4113);
	}

	@Test
	public void capturedPhysicalPlacementStillWinsWhenAStaffBecomesReplaceable()
	{
		BankSnapshot bank = bank(1, 1387, 1393);
		BankOrganizationPreview before = build(bank, sameStats(GearSlot.WEAPON), VALUES, CategoryOverrideSource.NONE,
			new BankLayoutOptions(true, true, false));
		int combat = plan.destinationOf("gear");
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(combat,
			Arrays.asList(1387, 1393)), before, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview after = build(bank, sameStats(GearSlot.WEAPON), VALUES, CategoryOverrideSource.NONE,
			defaults().withItemOrders(captured));
		assertAt(after, combat, 1387);
		assertAt(after, combat, 1393);
		assertTag(after, 1387, "alch");
	}

	@Test
	public void approvedSlashUpgradesReplaceReviewedStockWithoutFullStatDominance()
	{
		GearStats oldStats = fullStats(GearSlot.WEAPON, 60, 100);
		GearStats newStats = fullStats(GearSlot.WEAPON, 80, 0);
		assertFalse(newStats.dominates(oldStats));
		for (int candidate : BASIC_SLASH_WEAPONS)
		{
			for (int upgrade : new int[] {4587, 4151, 12006, 26482})
			{
				BankOrganizationPreview preview = build(bank(1, candidate, upgrade),
					id -> Optional.of(id == candidate ? oldStats : newStats));
				assertTag(preview, candidate, "alch");
				assertTag(preview, upgrade, "gear");
				assertEquals(1, alchCount(preview));
			}
		}
	}

	@Test
	public void slashFamilyRulesDoNotBroadenToMacesSpearsOrSpecialWeapons()
	{
		for (int candidate : new int[] {1432, 1434, 1247, 1215, 1377})
		{
			BankOrganizationPreview preview = build(bank(25, candidate, 4587),
				id -> Optional.of(fullStats(GearSlot.WEAPON, id == candidate ? 60 : 80, id == candidate ? 100 : 0)));
			assertTag(preview, candidate, "gear");
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void disablingAlchGatheringKeepsEveryReplacementFamily()
	{
		for (int[] pair : REPLACED_PAIRS)
		{
			BankOrganizationPreview preview = build(bank(25, pair), sameStats(GearSlot.WEAPON), VALUES,
				CategoryOverrideSource.NONE, new BankLayoutOptions(true, true, false));
			assertFalse("Alch disabled " + pair[0], "alch".equals(item(preview, pair[0]).getLayoutTagKey()));
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void replacementFamiliesStillRequirePositiveAlchValues()
	{
		for (int[] pair : REPLACED_PAIRS)
		{
			BankOrganizationPreview preview = build(bank(25, pair), sameStats(GearSlot.WEAPON), ItemValueSource.NONE,
				CategoryOverrideSource.NONE, defaults());
			assertFalse("No value " + pair[0], "alch".equals(item(preview, pair[0]).getLayoutTagKey()));
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void explicitCombatCorrectionsStillWinAcrossAllReplacementFamilies()
	{
		for (int[] pair : REPLACED_PAIRS)
		{
			for (String correction : Arrays.asList("gear", "combat-gear"))
			{
				CategoryOverrideSource overrides = id -> id == pair[0] ? Optional.of(correction) : Optional.empty();
				BankOrganizationPreview preview = build(bank(25, pair), sameStats(GearSlot.WEAPON), VALUES,
					overrides, defaults());
				assertTag(preview, pair[0], "gear");
				assertEquals(pair[0] == 4103 ? 1 : 0, alchCount(preview));
			}
		}
	}

	@Test
	public void spikedBootsRemainQuestItemsWithPositiveEquipmentStats()
	{
		assertEquals(ItemCategory.CLEANUP, CompositeItemCatalog.DEFAULT.describeOrUnknown(3107).getCategory());
		assertEquals("quest-item", CompositeItemCatalog.DEFAULT.describeOrUnknown(3107).getSubcategory());
		BankOrganizationPreview preview = build(bank(1, 3107, 3105), sameStats(GearSlot.FEET));
		assertTag(preview, 3107, "quest-items");
		assertAt(preview, plan.destinationOf("quest-items"), 3107);
		assertTag(preview, 3105, "gear");
		assertEquals(0, alchCount(preview));
	}

	private BankLayoutOptions defaults()
	{
		return BankLayoutOptions.defaultFor(preset);
	}

	private BankOrganizationPreview build(BankSnapshot bank, GearStatsSource stats)
	{
		return build(bank, stats, VALUES, CategoryOverrideSource.NONE, defaults());
	}

	private BankOrganizationPreview build(BankSnapshot bank, GearStatsSource stats, ItemValueSource values,
		CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			stats, values, overrides, plan, options);
	}

	private static BankSnapshot bank(int quantity, int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, quantity, items.size()));
		return new BankSnapshot(items);
	}

	private static GearStatsSource sameStats(GearSlot slot)
	{
		return id -> Optional.of(fullStats(slot, 30, 30));
	}

	// Deliberate synthetic stat tradeoffs: exact reviewed capabilities supply the proof.
	private static GearStats fullStats(GearSlot slot, int strength, int defence)
	{
		return new GearStats(slot, strength, strength, strength, 10, 0, strength, 0, 0,
			defence, defence, defence, defence, defence, 0, 4);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("Item " + id, tag, item(preview, id).getLayoutTagKey());
	}

	private static void assertAt(BankOrganizationPreview preview, int tab, int id)
	{
		assertTrue("Item " + id + " in tab " + tab, preview.getCategories().get(tab).getItems().stream()
			.anyMatch(item -> item.getItemId() == id));
	}

	private static int alchCount(BankOrganizationPreview preview)
	{
		return preview.getTagCounts().getOrDefault("alch", 0);
	}
}

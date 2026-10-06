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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class AlchFeedbackRoutingTest
{
	private static final int DRAGON_HALBERD = 3204;
	private static final int NEITIZNOT_HELM = 10828;
	private static final int TORVA_HELM = 26382;
	private static final int BANDOS_BODY = 11832;
	private static final int[] MED_HELMS = {1139, 1137, 1141, 1151, 1143, 1145, 1147, 1149};
	private static final int[] CHAINBODIES = {1103, 1101, 1105, 1107, 1109, 1111, 1113, 3140};
	private static final int[] HERALDIC_HELMS = {10286, 10288, 10290, 10292, 10294};
	private static final List<BankPreset> PRESETS = Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN);
	private static final ItemValueSource VALUES = id -> 39000;

	@Test
	public void ordinaryMedHelmsAreReviewedAndRouteToAlchWhenAnOwnedHelmetReplacesThem()
	{
		assertUpgradedArmour(MED_HELMS, GearSlot.HEAD, NEITIZNOT_HELM);
	}

	@Test
	public void ordinaryChainbodiesAreReviewedAndRouteToAlchWhenAnOwnedBodyReplacesThem()
	{
		assertUpgradedArmour(CHAINBODIES, GearSlot.BODY, BANDOS_BODY);
	}

	@Test
	public void medHelmsAndChainbodiesStayCombatWhileTheyAreBestOwnedEvenInStacks()
	{
		for (BankPreset preset : PRESETS)
		{
			for (int[] family : new int[][] {MED_HELMS, CHAINBODIES})
			{
				GearSlot slot = family == MED_HELMS ? GearSlot.HEAD : GearSlot.BODY;
				for (int id : family)
				{
					for (int quantity : new int[] {1, 2, 25})
					{
						BankOrganizationPreview preview = build(preset, bank(quantity, id),
							stats(id, armour(slot, 0, 200)));
						assertTag(preview, id, "gear");
						assertEquals(0, alchCount(preview));
					}
				}
			}
		}
	}

	@Test
	public void armourUpgradeProofStillRequiresTheSameSlot()
	{
		for (BankPreset preset : PRESETS)
		{
			for (int candidate : new int[] {1147, 1113})
			{
				boolean helmet = candidate == 1147;
				int incompatible = helmet ? BANDOS_BODY : TORVA_HELM;
				Map<Integer, GearStats> stats = stats(candidate, armour(helmet ? GearSlot.HEAD : GearSlot.BODY, 0, 200));
				stats.put(incompatible, armour(helmet ? GearSlot.BODY : GearSlot.HEAD, 6, 100));
				BankOrganizationPreview preview = build(preset, bank(25, candidate, incompatible), stats);
				assertTag(preview, candidate, "gear");
				assertEquals(0, alchCount(preview));
			}
		}
	}

	@Test
	public void bothPresetsNeedActualCandidateAndReplacementStatsEvenForLargeStacks()
	{
		for (BankPreset preset : PRESETS)
		{
			for (int candidate : new int[] {1147, 1113})
			{
				boolean helmet = candidate == 1147;
				GearSlot slot = helmet ? GearSlot.HEAD : GearSlot.BODY;
				int upgrade = helmet ? NEITIZNOT_HELM : BANDOS_BODY;
				for (int missing : new int[] {candidate, upgrade})
				{
					Map<Integer, GearStats> stats = stats(candidate, armour(slot, 0, 200));
					stats.put(upgrade, armour(slot, 6, 100));
					stats.remove(missing);
					BankOrganizationPreview preview = build(preset, bank(25, candidate, upgrade), stats);
					assertTag(preview, candidate, "gear");
					assertEquals(0, alchCount(preview));
				}
			}
		}
	}

	@Test
	public void realRuneKiteshieldStaysCombatUntilAnOwnedShieldReplacesItInEitherPreset()
	{
		int runeShield = 1201;
		int dragonDefender = 12954;
		Map<Integer, GearStats> stats = stats(runeShield, armour(GearSlot.SHIELD, 0, 200));
		stats.put(dragonDefender, armour(GearSlot.SHIELD, 6, 100));
		assertFalse(stats.get(dragonDefender).dominates(stats.get(runeShield)));
		for (BankPreset preset : PRESETS)
		{
			for (int quantity : new int[] {1, 2, 25})
			{
				BankOrganizationPreview bestOwned = build(preset, bank(quantity, runeShield), stats);
				assertTag(bestOwned, runeShield, "gear");
				assertAt(bestOwned, BankLayoutPlan.defaultFor(preset).destinationOf("gear"), runeShield);
				assertEquals(0, alchCount(bestOwned));
				BankOrganizationPreview replaced = build(preset, bank(quantity, runeShield, dragonDefender), stats);
				assertTag(replaced, runeShield, "alch");
				assertTag(replaced, dragonDefender, "gear");
				assertAt(replaced, BankLayoutPlan.defaultFor(preset).destinationOf("alch"), runeShield);
				assertEquals(1, alchCount(replaced));
			}
		}
	}

	@Test
	public void namesakesNotesAndOrnamentsDoNotBecomeReviewedOrdinaryArmour()
	{
		for (int id : new int[] {1102, 1104, 1112, 1114, 3141, 2513, 6895, 6967, 12414, 20428})
			assertFalse("Unreviewed namesake " + id, IronmanAlchCandidateCatalog.contains(id));
	}

	@Test
	public void alchArmourFallbackDoesNotChangeTheSharedGearTierCatalog()
	{
		for (int[] family : new int[][] {MED_HELMS, CHAINBODIES})
			for (int id : family)
				assertFalse("Global tier changed for " + id, GearTierCatalog.INSTANCE.tierOf(id).isPresent());
	}

	@Test
	public void clueRequiredOrdinaryArmourUsesTheSameBestOwnedGateInBothPresets()
	{
		for (BankPreset preset : PRESETS)
		{
			for (int candidate : new int[] {1103, 1101, 1137, 1141})
			{
				assertTrue(CompositeItemCatalog.DEFAULT.describeOrUnknown(candidate).hasTag("clue-required"));
				boolean helmet = candidate == 1137 || candidate == 1141;
				GearSlot slot = helmet ? GearSlot.HEAD : GearSlot.BODY;
				int upgrade = helmet ? NEITIZNOT_HELM : BANDOS_BODY;
				Map<Integer, GearStats> stats = stats(candidate, armour(slot, 0, 200));
				stats.put(upgrade, armour(slot, 6, 100));
				BankOrganizationPreview withoutUpgrade = build(preset, bank(25, candidate), stats);
				assertTag(withoutUpgrade, candidate, "gear");
				assertEquals(0, alchCount(withoutUpgrade));
				BankOrganizationPreview withUpgrade = build(preset, bank(25, candidate, upgrade), stats);
				assertTag(withUpgrade, candidate, "alch");
				assertTag(withUpgrade, upgrade, "gear");
				assertEquals(1, alchCount(withUpgrade));
			}
		}
	}

	@Test
	public void everyHeraldicRuneHelmUsesTheCluesTagEvenWithEquipmentStats()
	{
		for (BankPreset preset : PRESETS)
		{
			for (int id : HERALDIC_HELMS)
			{
				CatalogItem catalogItem = CompositeItemCatalog.DEFAULT.describeOrUnknown(id);
				assertEquals(ItemCategory.CLUE, catalogItem.getCategory());
				assertEquals("treasure-trail", catalogItem.getSubcategory());
				BankOrganizationPreview preview = build(preset, bank(25, id), stats(id, armour(GearSlot.HEAD, 0, 200)));
				assertTag(preview, id, "clues");
				assertAt(preview, BankLayoutPlan.defaultFor(preset).destinationOf("clues"), id);
				assertEquals(0, alchCount(preview));
			}
		}
	}

	@Test
	public void heraldicClassificationDoesNotSpreadToTheOrdinaryRuneFullHelm()
	{
		int ordinary = 1163;
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(preset, bank(1, ordinary),
				stats(ordinary, armour(GearSlot.HEAD, 0, 200)));
			assertTag(preview, ordinary, "gear");
		}
	}

	@Test
	public void playersCanStillOverrideAHeraldicHelmToCombat()
	{
		int heraldic = HERALDIC_HELMS[0];
		CategoryOverrideSource overrides = id -> id == heraldic ? Optional.of("gear") : Optional.empty();
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(preset, bank(1, heraldic),
				stats(heraldic, armour(GearSlot.HEAD, 0, 200)), VALUES, overrides,
				BankLayoutOptions.defaultFor(preset));
			assertTag(preview, heraldic, "gear");
			assertAt(preview, BankLayoutPlan.defaultFor(preset).destinationOf("gear"), heraldic);
		}
	}

	@Test
	public void dragonHalberdAlwaysRoutesToAlchWithoutGearStatsOrAnOwnedUpgrade()
	{
		assertEquals(ItemCategory.GEAR, CompositeItemCatalog.DEFAULT.describeOrUnknown(DRAGON_HALBERD).getCategory());
		for (BankPreset preset : PRESETS)
		{
			for (int quantity : new int[] {1, 25})
			{
				BankOrganizationPreview preview = build(preset, bank(quantity, DRAGON_HALBERD), Collections.emptyMap());
				assertTag(preview, DRAGON_HALBERD, "alch");
				assertAt(preview, BankLayoutPlan.defaultFor(preset).destinationOf("alch"), DRAGON_HALBERD);
				assertEquals(1, alchCount(preview));
			}
		}
	}

	@Test
	public void dragonHalberdStillRequiresARealAlchValue()
	{
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(preset, bank(25, DRAGON_HALBERD), Collections.emptyMap(),
				ItemValueSource.NONE, CategoryOverrideSource.NONE, BankLayoutOptions.defaultFor(preset));
			assertTag(preview, DRAGON_HALBERD, "gear");
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void disablingGatheringRetainsDragonHalberdInCombat()
	{
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(preset, bank(25, DRAGON_HALBERD), Collections.emptyMap(),
				VALUES, CategoryOverrideSource.NONE, new BankLayoutOptions(true, true, false));
			assertTag(preview, DRAGON_HALBERD, "gear");
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void placeholderDragonHalberdIsNeverAlchStock()
	{
		BankSnapshot bank = new BankSnapshot(Collections.singletonList(new BankItemSnapshot(DRAGON_HALBERD, 0, 0, true)));
		for (BankPreset preset : PRESETS)
		{
			BankOrganizationPreview preview = build(preset, bank, Collections.emptyMap());
			assertTag(preview, DRAGON_HALBERD, "gear");
			assertTrue(item(preview, DRAGON_HALBERD).isPlaceholder());
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void explicitCombatOverridesStillWinOverTheAlwaysAlchHalberdRule()
	{
		for (BankPreset preset : PRESETS)
		{
			for (String correction : Arrays.asList("gear", "combat-gear"))
			{
				CategoryOverrideSource overrides = id -> id == DRAGON_HALBERD ? Optional.of(correction) : Optional.empty();
				BankOrganizationPreview preview = build(preset, bank(25, DRAGON_HALBERD), Collections.emptyMap(),
					VALUES, overrides, BankLayoutOptions.defaultFor(preset));
				assertTag(preview, DRAGON_HALBERD, "gear");
				assertEquals(0, alchCount(preview));
			}
		}
	}

	@Test
	public void legacyAndModernBossLootCorrectionsIgnoreASeparatelyPlacedAlchTag()
	{
		for (BankPreset preset : PRESETS)
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset).withTagAt("alch", 6);
			for (int candidate : new int[] {DRAGON_HALBERD, 1127})
			{
				Map<Integer, GearStats> stats = new LinkedHashMap<>();
				BankSnapshot bank = bank(1, candidate);
				if (candidate == 1127)
				{
					stats.put(candidate, armour(GearSlot.BODY, 0, 200));
					stats.put(BANDOS_BODY, armour(GearSlot.BODY, 6, 100));
					bank = bank(1, candidate, BANDOS_BODY);
				}
				for (String correction : Arrays.asList("slayer-boss-loot", "boss-loot"))
				{
					CategoryOverrideSource overrides = id -> id == candidate ? Optional.of(correction) : Optional.empty();
					BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
						CompositeItemCatalog.DEFAULT, preset, id -> Optional.ofNullable(stats.get(id)),
						VALUES, overrides, plan, BankLayoutOptions.defaultFor(preset));
					assertTag(preview, candidate, "boss-loot");
					assertAt(preview, plan.destinationOf("boss-loot"), candidate);
					assertFalse(preview.getCategories().get(plan.destinationOf("alch")).getItems().stream()
						.anyMatch(item -> item.getItemId() == candidate));
					assertEquals(0, alchCount(preview));
				}
			}
		}
	}

	@Test
	public void otherDragonSpecialWeaponsAndCorruptedHalberdRemainProtected()
	{
		for (int weapon : new int[] {1215, 1377, 28049})
		{
			Map<Integer, GearStats> stats = stats(weapon, armour(GearSlot.WEAPON, 10, 20));
			stats.put(99001, armour(GearSlot.WEAPON, 40, 40));
			stats.put(99002, armour(GearSlot.WEAPON, 50, 50));
			BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(weapon, 25, 0),
				new BankItemSnapshot(99001, 1, 1), new BankItemSnapshot(99002, 1, 2)));
			for (BankPreset preset : PRESETS)
			{
				BankOrganizationPreview preview = build(preset, bank, stats);
				assertTag(preview, weapon, "gear");
				assertEquals(0, alchCount(preview));
			}
		}
		assertFalse(IronmanAlchCandidateCatalog.contains(28049));
	}

	@Test
	public void capturedBankDestinationsWinOverTheAlwaysAlchHalberdRule()
	{
		for (BankPreset preset : PRESETS)
		{
			BankSnapshot bank = bank(1, DRAGON_HALBERD);
			BankOrganizationPreview before = build(preset, bank, Collections.emptyMap(), VALUES,
				CategoryOverrideSource.NONE, new BankLayoutOptions(true, true, false));
			int combat = BankLayoutPlan.defaultFor(preset).destinationOf("gear");
			BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(combat,
				Collections.singletonList(DRAGON_HALBERD)), before, BlueprintItemOrders.EMPTY);
			BankOrganizationPreview after = build(preset, bank, Collections.emptyMap(), VALUES,
				CategoryOverrideSource.NONE, BankLayoutOptions.defaultFor(preset).withItemOrders(captured));
			assertAt(after, combat, DRAGON_HALBERD);
			assertTag(after, DRAGON_HALBERD, "alch");
		}
	}

	private static void assertUpgradedArmour(int[] candidates, GearSlot slot, int upgrade)
	{
		for (BankPreset preset : PRESETS)
		{
			for (int candidate : candidates)
			{
				assertTrue("Missing reviewed armour " + candidate, IronmanAlchCandidateCatalog.contains(candidate));
				int replacement = candidate == 1149 ? TORVA_HELM : upgrade;
				Map<Integer, GearStats> stats = stats(candidate, armour(slot, 0, 200));
				stats.put(replacement, armour(slot, 6, 100));
				// Exact progression supplies the proof despite a defensive stat tradeoff.
				assertFalse(stats.get(replacement).dominates(stats.get(candidate)));
				BankOrganizationPreview preview = build(preset, bank(1, candidate, replacement), stats);
				assertTag(preview, candidate, "alch");
				assertTag(preview, replacement, "gear");
				assertEquals(1, alchCount(preview));
			}
		}
	}

	private static BankOrganizationPreview build(BankPreset preset, BankSnapshot bank, Map<Integer, GearStats> stats)
	{
		return build(preset, bank, stats, VALUES, CategoryOverrideSource.NONE, BankLayoutOptions.defaultFor(preset));
	}

	private static BankOrganizationPreview build(BankPreset preset, BankSnapshot bank, Map<Integer, GearStats> stats,
		ItemValueSource values, CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			id -> Optional.ofNullable(stats.get(id)), values, overrides, BankLayoutPlan.defaultFor(preset), options);
	}

	private static BankSnapshot bank(int quantity, int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, quantity, items.size()));
		return new BankSnapshot(items);
	}

	private static Map<Integer, GearStats> stats(int id, GearStats stats)
	{
		Map<Integer, GearStats> result = new LinkedHashMap<>();
		result.put(id, stats);
		return result;
	}

	// Synthetic comparable vectors isolate placement proofs from changing game-stat tables.
	private static GearStats armour(GearSlot slot, int strength, int defence)
	{
		return new GearStats(slot, 0, 0, 0, 0, 0, strength, 0, 0,
			defence, defence, defence, 0, defence, 0, 0);
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

	private static int alchCount(BankOrganizationPreview preview)
	{
		return preview.getTagCounts().getOrDefault("alch", 0);
	}

	private static void assertAt(BankOrganizationPreview preview, int destination, Integer... expected)
	{
		List<Integer> actual = preview.getCategories().get(destination).getItems().stream()
			.filter(item -> !item.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList());
		assertTrue("Destination " + destination + ": " + actual, actual.containsAll(Arrays.asList(expected)));
	}
}

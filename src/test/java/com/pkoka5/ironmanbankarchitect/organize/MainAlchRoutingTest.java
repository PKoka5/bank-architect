package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.GearTierCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class MainAlchRoutingTest
{
	private static final int RUNE_BODY = 1127;
	private static final int RUNE_LEGS = 1079;
	private static final int RUNE_HELM = 1163;
	private static final int RUNE_SHIELD = 1201;
	private static final int BANDOS_BODY = 11832;
	private static final int BANDOS_LEGS = 11834;
	private static final int BLACK_BODY = 2503;
	private static final int BLACK_LEGS = 2497;
	private static final int BLESSED_BODY = 10370;
	private static final int BLESSED_LEGS = 10372;
	private static final ItemCatalog PLAIN_GEAR = catalog(-1, Collections.emptySet());
	private static final BankLayoutPlan MAIN_PLAN = BankLayoutPlan.defaultFor(BankPresets.MAIN);
	private static final ItemValueSource VALUES = itemId -> 39000;

	@Test
	public void mainDefaultEnablesReviewedAlchGathering()
	{
		assertTrue(BankLayoutOptions.defaultFor(BankPresets.MAIN).alchPile());
		assertEquals(MAIN_PLAN.destinationOf("boss-loot"), MAIN_PLAN.destinationOf("alch"));
	}

	@Test
	public void bestOwnedRuneSetStaysCombatEvenWhenItIsAStack()
	{
		Map<Integer, GearStats> stats = runeSet();
		for (int quantity : new int[] {1, 2, 25})
		{
			BankOrganizationPreview preview = build(bank(quantity, RUNE_BODY, RUNE_LEGS, RUNE_HELM,
				RUNE_SHIELD), PLAIN_GEAR, stats);
			assertAt(preview, "gear", RUNE_BODY, RUNE_LEGS, RUNE_HELM, RUNE_SHIELD);
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void bandosReplacesOnlyTheRuneSlotsThatActuallyHaveAnUpgrade()
	{
		Map<Integer, GearStats> stats = runeSet();
		stats.put(BANDOS_BODY, melee(GearSlot.BODY, 4, 80));
		stats.put(BANDOS_LEGS, melee(GearSlot.LEGS, 2, 80));
		// The simplified vectors intentionally retain Rune's defence advantage.
		assertFalse(stats.get(BANDOS_BODY).dominates(stats.get(RUNE_BODY)));
		assertTrue(GearTierCatalog.INSTANCE.tierOf(BANDOS_BODY).getAsInt()
			> GearTierCatalog.INSTANCE.tierOf(RUNE_BODY).getAsInt());

		BankOrganizationPreview preview = build(bank(1, RUNE_BODY, RUNE_LEGS, RUNE_HELM,
			RUNE_SHIELD, BANDOS_BODY, BANDOS_LEGS), PLAIN_GEAR, stats);

		assertAt(preview, "alch", RUNE_BODY, RUNE_LEGS);
		assertAt(preview, "gear", BANDOS_BODY, BANDOS_LEGS, RUNE_HELM, RUNE_SHIELD);
		assertEquals(2, alchCount(preview));
		assertEquals("alch", item(preview, RUNE_BODY).getLayoutTagKey());
	}

	@Test
	public void realCatalogRuneClassificationParticipatesInTheMainAlchRule()
	{
		Map<Integer, GearStats> stats = runeSet();
		stats.put(BANDOS_BODY, melee(GearSlot.BODY, 4, 80));
		stats.put(BANDOS_LEGS, melee(GearSlot.LEGS, 2, 80));
		BankOrganizationPreview preview = build(bank(1, RUNE_BODY, RUNE_LEGS, BANDOS_BODY,
			BANDOS_LEGS), CompositeItemCatalog.DEFAULT, stats);

		assertAt(preview, "alch", RUNE_BODY, RUNE_LEGS);
		assertAt(preview, "gear", BANDOS_BODY, BANDOS_LEGS);
		assertEquals(4, preview.getPlannedItemCount());
	}

	@Test
	public void bestOwnedBlackDragonhideStaysCombatRegardlessOfQuantity()
	{
		Map<Integer, GearStats> stats = stats(BLACK_BODY, ranged(GearSlot.BODY, 30, 60));
		stats.put(BLACK_LEGS, ranged(GearSlot.LEGS, 17, 40));
		for (int quantity : new int[] {1, 2, 100})
		{
			BankOrganizationPreview preview = build(bank(quantity, BLACK_BODY, BLACK_LEGS), PLAIN_GEAR, stats);
			assertAt(preview, "gear", BLACK_BODY, BLACK_LEGS);
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void blessedDragonhideReplacesItsCorrespondingBlackPieces()
	{
		Map<Integer, GearStats> stats = stats(BLACK_BODY, ranged(GearSlot.BODY, 30, 60));
		stats.put(BLACK_LEGS, ranged(GearSlot.LEGS, 17, 40));
		stats.put(BLESSED_BODY, ranged(GearSlot.BODY, 30, 60));
		stats.put(BLESSED_LEGS, ranged(GearSlot.LEGS, 17, 40));
		BankOrganizationPreview preview = build(bank(1, BLACK_BODY, BLACK_LEGS, BLESSED_BODY,
			BLESSED_LEGS), PLAIN_GEAR, stats);

		assertAt(preview, "alch", BLACK_BODY, BLACK_LEGS);
		assertAt(preview, "gear", BLESSED_BODY, BLESSED_LEGS);
		assertEquals(2, alchCount(preview));
	}

	@Test
	public void fullDominanceCanProveAnUpgradeWithinTheSameCuratedTier()
	{
		int redBody = 2501;
		assertEquals(GearTierCatalog.INSTANCE.tierOf(redBody).getAsInt(),
			GearTierCatalog.INSTANCE.tierOf(BLACK_BODY).getAsInt());
		Map<Integer, GearStats> stats = stats(redBody, ranged(GearSlot.BODY, 30, 40));
		stats.put(BLACK_BODY, ranged(GearSlot.BODY, 30, 60));
		assertTrue(stats.get(BLACK_BODY).dominates(stats.get(redBody)));
		BankOrganizationPreview preview = build(bank(1, redBody, BLACK_BODY), PLAIN_GEAR, stats);

		assertAt(preview, "alch", redBody);
		assertAt(preview, "gear", BLACK_BODY);
	}

	@Test
	public void unknownUpgradeTierNeedsFullStatDominance()
	{
		int upgrade = 99001;
		assertFalse(GearTierCatalog.INSTANCE.tierOf(upgrade).isPresent());
		Map<Integer, GearStats> stats = stats(RUNE_BODY, melee(GearSlot.BODY, 0, 100));
		stats.put(upgrade, melee(GearSlot.BODY, 1, 101));
		BankOrganizationPreview preview = build(bank(1, RUNE_BODY, upgrade), PLAIN_GEAR, stats);

		assertAt(preview, "alch", RUNE_BODY);
		assertAt(preview, "gear", upgrade);
	}

	@Test
	public void higherScoreWithAnUnknownTierCannotHideAStatTradeoff()
	{
		int sidegrade = 99001;
		Map<Integer, GearStats> stats = stats(RUNE_BODY, melee(GearSlot.BODY, 0, 100));
		stats.put(sidegrade, melee(GearSlot.BODY, 40, 80));
		assertTrue(stats.get(sidegrade).score() > stats.get(RUNE_BODY).score());
		assertFalse(stats.get(sidegrade).dominates(stats.get(RUNE_BODY)));
		BankOrganizationPreview preview = build(bank(1, RUNE_BODY, sidegrade), PLAIN_GEAR, stats);

		assertAt(preview, "gear", RUNE_BODY, sidegrade);
		assertEquals(0, alchCount(preview));
	}

	@Test
	public void anUpgradeInAnotherSlotOrCombatStyleCannotReplaceRuneBody()
	{
		for (GearStats incompatible : Arrays.asList(melee(GearSlot.LEGS, 4, 200),
			ranged(GearSlot.BODY, 40, 200)))
		{
			Map<Integer, GearStats> stats = stats(RUNE_BODY, melee(GearSlot.BODY, 0, 100));
			stats.put(BANDOS_BODY, incompatible);
			BankOrganizationPreview preview = build(bank(1, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR, stats);
			assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void curatedArmourPlaceholderGuidesReviewedStockWithoutOwningTheReplacement()
	{
		Map<Integer, GearStats> stats = replacementStats();
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, 25, 0),
			new BankItemSnapshot(BANDOS_BODY, 0, 1, true)));
		BankOrganizationPreview preview = build(bank, PLAIN_GEAR, stats);

		assertAt(preview, "alch", RUNE_BODY);
		assertAt(preview, "gear", BANDOS_BODY);
		assertEquals(1, alchCount(preview));
		assertTrue(item(preview, BANDOS_BODY).isPlaceholder());
	}

	@Test
	public void placeholderRuneBodyIsNotAlchStock()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, 0, 0, true),
			new BankItemSnapshot(BANDOS_BODY, 1, 1)));
		BankOrganizationPreview preview = build(bank, PLAIN_GEAR, replacementStats());

		assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
		assertEquals(0, alchCount(preview));
		assertTrue(item(preview, RUNE_BODY).isPlaceholder());
	}

	@Test
	public void bothCandidateAndUpgradeNeedKnownEquipmentStats()
	{
		for (int missing : new int[] {RUNE_BODY, BANDOS_BODY})
		{
			Map<Integer, GearStats> stats = replacementStats();
			stats.remove(missing);
			BankOrganizationPreview preview = build(bank(2, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR, stats);
			assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void aCandidateWithoutARealAlchValueStaysCombat()
	{
		BankOrganizationPreview preview = build(bank(2, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR,
			replacementStats(), ItemValueSource.NONE, CategoryOverrideSource.NONE, MAIN_PLAN, defaults());
		assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
		assertEquals(0, alchCount(preview));
	}

	@Test
	public void unreviewedBulkStockNeedsFullDominance()
	{
		int unreviewed = 99002;
		Map<Integer, GearStats> stats = stats(unreviewed, melee(GearSlot.BODY, 0, 20));
		stats.put(BANDOS_BODY, melee(GearSlot.BODY, 4, 100));
		BankOrganizationPreview preview = build(bank(100, unreviewed, BANDOS_BODY), PLAIN_GEAR, stats);
		assertAt(preview, "alch", unreviewed);
		assertAt(preview, "gear", BANDOS_BODY);
		assertEquals(1, alchCount(preview));
		stats.put(unreviewed, melee(GearSlot.BODY, 0, 200));
		assertFalse(stats.get(BANDOS_BODY).dominates(stats.get(unreviewed)));
		BankOrganizationPreview niche = build(bank(100, unreviewed, BANDOS_BODY), PLAIN_GEAR, stats);
		assertAt(niche, "gear", unreviewed, BANDOS_BODY);
		assertEquals(0, alchCount(niche));
	}

	@Test
	public void clueRequirementAloneDoesNotRetainUpgradedRuneOrDragonhideOnMain()
	{
		for (int candidate : new int[] {RUNE_BODY, BLACK_BODY})
		{
			int upgrade = candidate == RUNE_BODY ? BANDOS_BODY : BLESSED_BODY;
			Map<Integer, GearStats> stats = stats(candidate, candidate == RUNE_BODY
				? melee(GearSlot.BODY, 0, 100) : ranged(GearSlot.BODY, 30, 60));
			stats.put(upgrade, candidate == RUNE_BODY
				? melee(GearSlot.BODY, 4, 80) : ranged(GearSlot.BODY, 30, 60));
			BankOrganizationPreview preview = build(bank(1, candidate, upgrade),
				catalog(candidate, Collections.singleton("clue-required")), stats);
			assertAt(preview, "alch", candidate);
			assertAt(preview, "gear", upgrade);
		}
	}

	@Test
	public void clueRequirementDoesNotOverrideOtherProtectedRoles()
	{
		for (String role : Arrays.asList("quest-use", "special-attack", "skilling-outfit", "god-protection",
			"prayer-gear", "rune-saving"))
		{
			Set<String> roles = new java.util.LinkedHashSet<>(Arrays.asList("clue-required", role));
			BankOrganizationPreview preview = build(bank(25, RUNE_BODY, BANDOS_BODY),
				catalog(RUNE_BODY, roles), replacementStats());
			assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
			assertEquals(role, 0, alchCount(preview));
		}
	}

	@Test
	public void specialAttackWeaponsStayEvenWithAFullStatUpgrade()
	{
		int dagger = 1215;
		int upgrade = 99001;
		ItemCatalog catalog = id -> Optional.of(new CatalogItem(id,
			id == dagger ? "Dragon dagger" : "Weapon " + id,
			ItemCategory.GEAR, "weapon", Collections.emptySet(), null));
		Map<Integer, GearStats> stats = stats(dagger, melee(GearSlot.WEAPON, 10, 20));
		stats.put(upgrade, melee(GearSlot.WEAPON, 40, 40));
		assertTrue(stats.get(upgrade).dominates(stats.get(dagger)));
		BankOrganizationPreview preview = build(bank(10, dagger, upgrade), catalog, stats);
		assertAt(preview, "gear", dagger, upgrade);
		assertEquals(0, alchCount(preview));
	}

	@Test
	public void turningGatheringOffRetainsAllReviewedGear()
	{
		BankOrganizationPreview preview = build(bank(25, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR,
			replacementStats(), VALUES, CategoryOverrideSource.NONE, MAIN_PLAN,
			new BankLayoutOptions(true, false, false));
		assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
		assertEquals(0, alchCount(preview));
	}

	@Test
	public void explicitCombatCorrectionsWinOverAutomaticAlchRouting()
	{
		for (String correction : Arrays.asList("gear", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> id == RUNE_BODY
				? Optional.of(correction) : Optional.empty();
			BankOrganizationPreview preview = build(bank(25, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR,
				replacementStats(), VALUES, overrides, MAIN_PLAN, defaults());
			assertAt(preview, "gear", RUNE_BODY, BANDOS_BODY);
			assertEquals(0, alchCount(preview));
		}
	}

	@Test
	public void explicitAlchTagCanPlaceEvenBestOwnedGearInTheChosenTab()
	{
		CategoryOverrideSource overrides = id -> id == RUNE_BODY ? Optional.of("alch") : Optional.empty();
		BankLayoutPlan plan = MAIN_PLAN.withTagAt("alch", 6);
		BankOrganizationPreview preview = build(bank(1, RUNE_BODY), PLAIN_GEAR, runeSet(), VALUES,
			overrides, plan, defaults());
		assertAt(preview, 6, RUNE_BODY);
		assertEquals("alch", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals(1, alchCount(preview));
	}

	@Test
	public void capturedBankDestinationsWinWhenAutomaticClassificationChanges()
	{
		BankSnapshot bank = bank(1, RUNE_BODY, BANDOS_BODY);
		BankOrganizationPreview before = build(bank, PLAIN_GEAR, replacementStats(), VALUES,
			CategoryOverrideSource.NONE, MAIN_PLAN, new BankLayoutOptions(true, false, false));
		Map<Integer, List<Integer>> physicalTabs = Collections.singletonMap(MAIN_PLAN.destinationOf("gear"),
			Arrays.asList(RUNE_BODY, BANDOS_BODY));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(physicalTabs, before, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview after = build(bank, PLAIN_GEAR, replacementStats(), VALUES,
			CategoryOverrideSource.NONE, MAIN_PLAN, defaults().withItemOrders(captured));

		assertTrue(captured.isCaptured());
		assertAt(after, "gear", RUNE_BODY, BANDOS_BODY);
		assertEquals(Arrays.asList(RUNE_BODY, BANDOS_BODY), ids(after, MAIN_PLAN.destinationOf("gear")));
		assertEquals("alch", item(after, RUNE_BODY).getLayoutTagKey());
		assertEquals(0, ids(after, MAIN_PLAN.destinationOf("alch")).size());
	}

	@Test
	public void alchTagMovesIndependentlyOfBossLootAndKeepsItsCount()
	{
		int loot = 99003;
		ItemCatalog catalog = id -> Optional.of(new CatalogItem(id, "Item " + id,
			id == loot ? ItemCategory.UNIQUE : ItemCategory.GEAR, "gear", Collections.emptySet(), null));
		BankLayoutPlan plan = MAIN_PLAN.withTagAt("alch", 6);
		BankOrganizationPreview preview = build(bank(1, RUNE_BODY, BANDOS_BODY, loot), catalog,
			replacementStats(), VALUES, CategoryOverrideSource.NONE, plan, defaults());

		assertAt(preview, 6, RUNE_BODY);
		assertAt(preview, MAIN_PLAN.destinationOf("boss-loot"), loot);
		assertAt(preview, "gear", BANDOS_BODY);
		assertEquals(1, alchCount(preview));
		assertEquals(3, preview.getPlannedItemCount());
	}

	@Test
	public void removingTheUpgradeReturnsRuneToCombat()
	{
		Map<Integer, GearStats> stats = replacementStats();
		BankOrganizationPreview withUpgrade = build(bank(25, RUNE_BODY, BANDOS_BODY), PLAIN_GEAR, stats);
		BankOrganizationPreview withoutUpgrade = build(bank(25, RUNE_BODY), PLAIN_GEAR, stats);
		assertAt(withUpgrade, "alch", RUNE_BODY);
		assertAt(withoutUpgrade, "gear", RUNE_BODY);
		assertEquals(0, alchCount(withoutUpgrade));
	}

	@Test
	public void armadylAndUnfortifiedMasoriCanReplaceReviewedDragonhide()
	{
		for (int upgrade : new int[] {11828, 27229})
		{
			Map<Integer, GearStats> stats = stats(BLACK_BODY, ranged(GearSlot.BODY, 30, 80));
			stats.put(upgrade, ranged(GearSlot.BODY, 40, 60));
			assertFalse(stats.get(upgrade).dominates(stats.get(BLACK_BODY)));
			BankOrganizationPreview preview = build(bank(1, BLACK_BODY, upgrade), PLAIN_GEAR, stats);
			assertAt(preview, "alch", BLACK_BODY);
			assertAt(preview, "gear", upgrade);
		}
	}

	@Test
	public void aHigherTierStaffDoesNotReplaceRuneScimitar()
	{
		int runeScimitar = 1333;
		int ibanStaff = 1409;
		assertTrue(GearTierCatalog.INSTANCE.tierOf(ibanStaff).getAsInt()
			> GearTierCatalog.INSTANCE.tierOf(runeScimitar).getAsInt());
		Map<Integer, GearStats> stats = stats(runeScimitar, melee(GearSlot.WEAPON, 30, 100));
		stats.put(ibanStaff, melee(GearSlot.WEAPON, 60, 80));
		assertFalse(stats.get(ibanStaff).dominates(stats.get(runeScimitar)));
		BankOrganizationPreview preview = build(bank(25, runeScimitar, ibanStaff), PLAIN_GEAR, stats);
		assertAt(preview, "gear", runeScimitar, ibanStaff);
		assertEquals(0, alchCount(preview));
	}

	private static BankLayoutOptions defaults()
	{
		return BankLayoutOptions.defaultFor(BankPresets.MAIN);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, ItemCatalog catalog,
		Map<Integer, GearStats> stats)
	{
		return build(bank, catalog, stats, VALUES, CategoryOverrideSource.NONE, MAIN_PLAN, defaults());
	}

	private static BankOrganizationPreview build(BankSnapshot bank, ItemCatalog catalog,
		Map<Integer, GearStats> stats, ItemValueSource values, CategoryOverrideSource overrides,
		BankLayoutPlan plan, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, catalog, BankPresets.MAIN,
			id -> Optional.ofNullable(stats.get(id)), values, overrides, plan, options);
	}

	private static ItemCatalog catalog(int taggedId, Set<String> tags)
	{
		return id -> Optional.of(new CatalogItem(id, "Test gear " + id, ItemCategory.GEAR,
			"gear", id == taggedId ? tags : Collections.emptySet(), null));
	}

	private static BankSnapshot bank(int quantity, int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, quantity, items.size()));
		return new BankSnapshot(items);
	}

	private static Map<Integer, GearStats> runeSet()
	{
		Map<Integer, GearStats> stats = stats(RUNE_BODY, melee(GearSlot.BODY, 0, 100));
		stats.put(RUNE_LEGS, melee(GearSlot.LEGS, 0, 100));
		stats.put(RUNE_HELM, melee(GearSlot.HEAD, 0, 100));
		stats.put(RUNE_SHIELD, melee(GearSlot.SHIELD, 0, 100));
		return stats;
	}

	private static Map<Integer, GearStats> replacementStats()
	{
		Map<Integer, GearStats> stats = runeSet();
		stats.put(BANDOS_BODY, melee(GearSlot.BODY, 4, 80));
		return stats;
	}

	private static Map<Integer, GearStats> stats(int id, GearStats stats)
	{
		Map<Integer, GearStats> result = new LinkedHashMap<>();
		result.put(id, stats);
		return result;
	}

	// Synthetic full vectors isolate proof conditions; they are not copied game-stat tables.
	private static GearStats melee(GearSlot slot, int strength, int defence)
	{
		return new GearStats(slot, 0, 0, 0, 0, 0, strength, 0, 0,
			defence, defence, defence, 0, defence, 0, 0);
	}

	private static GearStats ranged(GearSlot slot, int attack, int defence)
	{
		return new GearStats(slot, 0, 0, 0, 0, attack, 0, 0, 0,
			defence, defence, defence, defence, defence, 0, 0);
	}

	private static int alchCount(BankOrganizationPreview preview)
	{
		return preview.getTagCounts().getOrDefault("alch", 0);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int destination)
	{
		List<Integer> result = new ArrayList<>();
		preview.getCategories().get(destination).getItems().stream().filter(item -> !item.isBlank())
			.forEach(item -> result.add(item.getItemId()));
		return result;
	}

	private static void assertAt(BankOrganizationPreview preview, String tag, Integer... expected)
	{
		assertAt(preview, MAIN_PLAN.destinationOf(tag), expected);
	}

	private static void assertAt(BankOrganizationPreview preview, int destination, Integer... expected)
	{
		List<Integer> actual = ids(preview, destination);
		assertTrue("destination " + destination + ": " + actual, actual.containsAll(Arrays.asList(expected)));
	}
}

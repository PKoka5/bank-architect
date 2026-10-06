package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class IronmanAlchTagRoutingTest
{
	private static final int RUNE_BODY = 1127;
	private static final int BANDOS_BODY = 11832;
	private static final int LOOT = 99003;
	private static final BankLayoutPlan PLAN = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
	private static final ItemValueSource VALUES = id -> id == RUNE_BODY ? 39000 : 0;
	private static final ItemCatalog CATALOG = catalog(Collections.emptySet());

	@Test
	public void reviewedGearWithAnUpgradeUsesTheAlchTagInItsDefaultDestination()
	{
		BankOrganizationPreview preview = build(bank(2), CATALOG, PLAN, BankLayoutOptions.DEFAULTS,
			CategoryOverrideSource.NONE);

		assertEquals(PLAN.destinationOf("boss-loot"), PLAN.destinationOf("alch"));
		assertAt(preview, PLAN.destinationOf("boss-loot"), RUNE_BODY, LOOT);
		assertEquals("alch", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals("boss-loot", item(preview, LOOT).getLayoutTagKey());
		assertEquals(Integer.valueOf(1), preview.getTagCounts().get("alch"));
		assertEquals(Integer.valueOf(1), preview.getTagCounts().get("boss-loot"));
	}

	@Test
	public void aSingleReviewedItemWithoutAnUpgradeStillStaysCombat()
	{
		BankOrganizationPreview preview = build(bankWithoutUpgrade(1), CATALOG, PLAN, BankLayoutOptions.DEFAULTS,
			CategoryOverrideSource.NONE);

		assertAt(preview, PLAN.destinationOf("gear"), RUNE_BODY);
		assertEquals("gear", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
	}

	@Test
	public void alchCanMoveIndependentlyOfTrueBossLoot()
	{
		BankLayoutPlan plan = PLAN.withTagAt("alch", 6);
		BankOrganizationPreview preview = build(bank(2), CATALOG, plan, BankLayoutOptions.DEFAULTS,
			CategoryOverrideSource.NONE);

		assertAt(preview, 6, RUNE_BODY);
		assertAt(preview, PLAN.destinationOf("boss-loot"), LOOT);
		assertEquals("alch", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals(3, preview.getPlannedItemCount());
	}

	@Test
	public void anOlderCustomPlanKeepsNewAlchCandidatesBesideItsExistingLoot()
	{
		BankLayoutPlan oldPlan = PLAN.withTagAt("boss-loot", 5);
		String withoutAlch = oldPlan.getDestinations().stream()
			.map(tags -> tags.stream().filter(tag -> !"alch".equals(tag)).collect(Collectors.joining("+")))
			.collect(Collectors.joining("|"));
		BankLayoutPlan restored = BankLayoutPlan.parse(BankPresets.IRONMAN, withoutAlch);
		assertEquals(5, restored.destinationOf("boss-loot"));
		assertEquals(5, restored.destinationOf("alch"));

		BankOrganizationPreview preview = build(bank(2), CATALOG, restored, BankLayoutOptions.DEFAULTS,
			CategoryOverrideSource.NONE);
		assertAt(preview, 5, RUNE_BODY, LOOT);
	}

	@Test
	public void disablingAutomaticGatheringRetainsReviewedDuplicatesInCombat()
	{
		BankOrganizationPreview preview = build(bank(25), CATALOG, PLAN,
			new BankLayoutOptions(true, true, false), CategoryOverrideSource.NONE);
		assertAt(preview, PLAN.destinationOf("gear"), RUNE_BODY);
		assertAt(preview, PLAN.destinationOf("boss-loot"), LOOT);
		assertEquals("gear", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
	}

	@Test
	public void knownQuestAndSpecialAttackRolesStayProtectedForEveryStackSize()
	{
		for (String role : Arrays.asList("quest-use", "special-attack"))
		{
			for (int quantity : new int[] {1, 2, 25})
			{
				BankOrganizationPreview preview = build(bank(quantity), catalog(Collections.singleton(role)),
					PLAN, BankLayoutOptions.DEFAULTS, CategoryOverrideSource.NONE);
				assertAt(preview, PLAN.destinationOf("gear"), RUNE_BODY);
				assertEquals(role + " x" + quantity, "gear", item(preview, RUNE_BODY).getLayoutTagKey());
				assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
			}
		}
	}

	@Test
	public void explicitCombatCorrectionsStillWinOverTheAutomaticAlchTag()
	{
		for (String correction : Arrays.asList("gear", "combat-gear"))
		{
			CategoryOverrideSource overrides = id -> id == RUNE_BODY
				? Optional.of(correction) : Optional.empty();
			BankOrganizationPreview preview = build(bank(25), CATALOG, PLAN, BankLayoutOptions.DEFAULTS, overrides);
			assertAt(preview, PLAN.destinationOf("gear"), RUNE_BODY);
			assertEquals("gear", item(preview, RUNE_BODY).getLayoutTagKey());
			assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
		}
	}

	@Test
	public void anExplicitBossLootTagWinsEvenWhenTheAlchTagHasMoved()
	{
		BankLayoutPlan plan = PLAN.withTagAt("alch", 6);
		CategoryOverrideSource overrides = id -> id == RUNE_BODY
			? Optional.of("boss-loot") : Optional.empty();
		BankOrganizationPreview preview = build(bank(25), CATALOG, plan, BankLayoutOptions.DEFAULTS, overrides);
		assertAt(preview, PLAN.destinationOf("boss-loot"), RUNE_BODY, LOOT);
		assertEquals("boss-loot", item(preview, RUNE_BODY).getLayoutTagKey());
		assertEquals(0, preview.getTagCounts().getOrDefault("alch", 0).intValue());
	}

	@Test
	public void capturedDestinationsRemainAuthoritativeAfterAlchTagging()
	{
		BankSnapshot bank = bank(2);
		BankOrganizationPreview before = build(bank, CATALOG, PLAN, new BankLayoutOptions(true, true, false),
			CategoryOverrideSource.NONE);
		Map<Integer, List<Integer>> physicalTabs = new LinkedHashMap<>();
		physicalTabs.put(PLAN.destinationOf("gear"), Arrays.asList(RUNE_BODY, BANDOS_BODY));
		physicalTabs.put(PLAN.destinationOf("boss-loot"), Collections.singletonList(LOOT));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(physicalTabs, before, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview after = build(bank, CATALOG, PLAN,
			BankLayoutOptions.DEFAULTS.withItemOrders(captured), CategoryOverrideSource.NONE);

		assertTrue(captured.isCaptured());
		assertAt(after, PLAN.destinationOf("gear"), RUNE_BODY);
		assertAt(after, PLAN.destinationOf("boss-loot"), LOOT);
		assertEquals("alch", item(after, RUNE_BODY).getLayoutTagKey());
		assertEquals(Integer.valueOf(1), after.getTagCounts().get("alch"));
	}

	private static ItemCatalog catalog(Set<String> roles)
	{
		return id -> Optional.of(new CatalogItem(id,
			id == RUNE_BODY ? "Rune platebody" : id == BANDOS_BODY ? "Bandos chestplate" : "Boss loot",
			id == LOOT ? ItemCategory.UNIQUE : ItemCategory.GEAR, "gear",
			id == RUNE_BODY ? roles : Collections.emptySet(), null));
	}

	private static BankSnapshot bank(int runeQuantity)
	{
		return new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, runeQuantity, 0),
			new BankItemSnapshot(LOOT, 1, 1), new BankItemSnapshot(BANDOS_BODY, 1, 2)));
	}

	private static BankSnapshot bankWithoutUpgrade(int runeQuantity)
	{
		return new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, runeQuantity, 0),
			new BankItemSnapshot(LOOT, 1, 1)));
	}

	// Synthetic full vectors intentionally leave Rune ahead on defence;
	// the reviewed armour-stage proof, rather than full dominance, replaces it.
	private static Optional<GearStats> stats(int id)
	{
		int defence = id == RUNE_BODY ? 100 : 80;
		return id != RUNE_BODY && id != BANDOS_BODY ? Optional.empty()
			: Optional.of(new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0,
				id == BANDOS_BODY ? 4 : 0, 0, 0, defence, defence, defence, 0, defence, 0, 0));
	}

	private static BankOrganizationPreview build(BankSnapshot bank, ItemCatalog catalog,
		BankLayoutPlan plan, BankLayoutOptions options, CategoryOverrideSource overrides)
	{
		return BankOrganizationPreviewBuilder.build(bank, catalog, BankPresets.IRONMAN,
			IronmanAlchTagRoutingTest::stats, VALUES, overrides, plan, options);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertAt(BankOrganizationPreview preview, int destination, Integer... expected)
	{
		List<Integer> actual = preview.getCategories().get(destination).getItems().stream()
			.filter(item -> !item.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList());
		assertTrue("destination " + destination + ": " + actual, actual.containsAll(Arrays.asList(expected)));
	}
}

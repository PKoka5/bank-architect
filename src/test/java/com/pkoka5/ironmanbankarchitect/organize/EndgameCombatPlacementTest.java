package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Independent expected layout policy, using a self-contained RuneLite stat-data snapshot. */
@RunWith(Parameterized.class)
public class EndgameCombatPlacementTest
{
	private static final List<Integer> SCYTHES = Arrays.asList(22325,22486,25736,25738,25739,25741);
	private static final List<Integer> SHADOWS = Arrays.asList(27275,27277);
	private static final int[][] PRIMARY_ARMOUR = {
		{26382,26384,26386}, {27235,27238,27241}, {21018,21021,21024}, {28933,28936,28939}
	};
	private static final int[][] SPECIALIST_ARMOUR = {
		{30750,30753,30756}, {24419,24420,24421}, {22326,22327,22328}
	};
	private static final List<Integer> SPECIALIST_WEAPONS = Arrays.asList(22324,26219,24417,21003,11804,13576);
	private static final List<Integer> CANNON = Arrays.asList(6,8,10,12);
	private static final Set<Integer> WITHDRAWN = new HashSet<>(Arrays.asList(
		26382,26384,26386,27235,27238,27241,21018,21021,21024,22325,20997,27275,
		30750,30753,30756,24419,24420,24421,22326,22327,22328));
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[][] {{BankPresetType.MAIN}, {BankPresetType.IRONMAN}});
	}

	public EndgameCombatPlacementTest(BankPresetType type)
	{
		preset = BankPresets.forType(type);
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void fullEndgameFrontUsesGeneralStrengthArmourAndRaidWeaponPriorities()
	{
		BankSnapshot bank = endgameBank(Collections.emptySet(), 1, false);
		BankOrganizationPreview preview = build(bank);
		List<Integer> combat = ids(combat(preview));
		assertTrue("exercise a fully filled setup grid", combat.size() >= 72);
		for (int column = 0; column < PRIMARY_ARMOUR.length; column++)
			for (int row = 0; row < PRIMARY_ARMOUR[column].length; row++)
				assertEquals("independent armour target " + column + ":" + row,
					PRIMARY_ARMOUR[column][row], (int) combat.get(row * 8 + column));
		assertEquals("general Melee frontline weapon", 22325, (int) combat.get(8 * 8));
		assertEquals("general Ranged frontline weapon", 20997, (int) combat.get(8 * 8 + 1));
		assertEquals("general Magic frontline weapon", 27275, (int) combat.get(8 * 8 + 2));
		assertPreserved(bank, preview);
	}

	@Test
	public void allScytheAppearancesAndShadowChargeStatesKeepTheReviewedFrontlinePriority()
	{
		for (int scythe : SCYTHES)
			for (int shadow : SHADOWS)
			{
				BankSnapshot bank = bank(Arrays.asList(11802,25867,21006,scythe,20997,shadow),
					Collections.emptySet(), 1, false);
				BankOrganizationPreview preview = build(bank);
				assertEquals("frontline IDs for " + scythe + "/" + shadow,
					Arrays.asList(scythe,20997,shadow), ids(combat(preview)).subList(0,3));
				assertPreserved(bank, preview);
				BankOrganizationPreview fallback = buildWithoutStats(bank);
				assertEquals("frontline IDs without equipment data for " + scythe + "/" + shadow,
					Arrays.asList(scythe,20997,shadow), ids(combat(fallback)).subList(0,3));
				assertPreserved(bank,fallback);
			}
	}

	@Test
	public void nonRaidFrontUsesMainWeaponsBeforeSpecialAttackAndSpellcastingSwitches()
	{
		BankSnapshot bank = bank(Arrays.asList(26219,12926,12899,11802,25867,21006),
			Collections.emptySet(), 1, false);
		BankOrganizationPreview preview = build(bank);
		assertEquals("general weapon roles before switches", Arrays.asList(26219,25867,12899),
			ids(combat(preview)).subList(0,3));
		assertPreserved(bank,preview);
	}

	@Test
	public void sparseBanksPreferMainWeaponsButCanStillUseTheirOnlySpecialAttackWeapon()
	{
		int[][] scenarios = {{11802,26219},{21006,22323},{11802}};
		int[] expected = {26219,22323,11802};
		for (int scenario = 0; scenario < scenarios.length; scenario++)
		{
			List<Integer> ids = Arrays.stream(scenarios[scenario]).boxed().collect(Collectors.toList());
			BankSnapshot bank = bank(ids,Collections.emptySet(),1,false);
			BankOrganizationPreview preview = build(bank);
			assertEquals("independent sparse primary " + scenario, expected[scenario],
				combat(preview).get(0).getItemId());
			assertPreserved(bank,preview);
		}
	}

	@Test
	public void missingEquipmentMetadataDoesNotHideReviewedRaidWeaponPriorities()
	{
		BankSnapshot bank = bank(Arrays.asList(11802,25867,21006,22325,20997,27275),
			Collections.emptySet(), 1, false);
		BankOrganizationPreview preview = buildWithoutStats(bank);
		String metadata = preview.getPlannedItems().stream().map(item -> item.getItemId() + ":"
			+ item.getSubcategory() + ":" + item.getItemCategory() + ":" + item.getLayoutTagKey()
			+ ":" + item.getDisplayName()).collect(Collectors.joining("; "));
		assertEquals("exact identity priority survives absent stat data: " + metadata, Arrays.asList(22325,20997,27275),
			ids(combat(preview)).subList(0,3));
		assertPreserved(bank,preview);
	}

	@Test
	public void aBowfaFrontUsesItsCrystalArmourAndKeepsUnusedMasoriVertical()
	{
		List<Integer> ids = endgameBank(Collections.emptySet(),1,false).getItems().stream()
			.map(BankItemSnapshot::getItemId).filter(id -> id != 20997).collect(Collectors.toList());
		List<Integer> expected = null;
		for (Set<Integer> placeholders : Arrays.asList(Collections.<Integer>emptySet(),
			new HashSet<>(Arrays.asList(25867,23971,23975,23979))))
		{
			BankSnapshot bank = bank(ids,placeholders,1,false);
			BankOrganizationPreview preview = build(bank);
			List<Integer> combat = ids(combat(preview));
			assertEquals("Bowfa is the available Ranged frontline", 25867, (int) combat.get(8 * 8 + 1));
			int[] crystal = {23971,23975,23979};
			for (int row = 0; row < crystal.length; row++)
				assertEquals("Crystal matches the Bowfa setup " + row, crystal[row], (int) combat.get(row * 8 + 1));
			assertVertical(combat,PRIMARY_ARMOUR[1]);
			if (expected == null) expected = combat;
			else assertEquals("withdrawing Bowfa kit retains intended armour synergy", expected,combat);
			assertPreserved(bank,preview);
		}
	}

	@Test
	public void inactiveBowfaAndCrystalKeepTheirRangedSetupWithoutOffensiveStatBonuses()
	{
		List<Integer> ids = endgameBank(Collections.emptySet(),1,false).getItems().stream()
			.map(BankItemSnapshot::getItemId).collect(Collectors.toList());
		// Only the inactive Bowfa supplies this fixture's Ranged weapon slot.
		ids.removeAll(Arrays.asList(20997,27610,26374,12926,28922));
		ids.set(ids.indexOf(25867),25862);
		int[] active = {23971,23975,23979};
		int[] inactive = {23973,23977,23981};
		for (int row = 0; row < active.length; row++) ids.set(ids.indexOf(active[row]),inactive[row]);
		List<Integer> expected = null;
		for (Set<Integer> placeholders : Arrays.asList(Collections.<Integer>emptySet(),
			new HashSet<>(Arrays.asList(25862,23973,23977,23981))))
		{
			BankSnapshot bank = bank(ids,placeholders,1,false);
			BankOrganizationPreview preview = build(bank);
			List<Integer> combat = ids(combat(preview));
			assertEquals("inactive Bowfa retains the Ranged weapon role",25862,(int) combat.get(8 * 8 + 1));
			for (int row = 0; row < inactive.length; row++)
				assertEquals("inactive Crystal retains the Ranged armour role " + row,inactive[row],
					(int) combat.get(row * 8 + 1));
			assertVertical(combat,PRIMARY_ARMOUR[1]);
			if (expected == null) expected = combat;
			else assertEquals("inactive kit placeholders keep the same Ranged setup",expected,combat);
			assertPreserved(bank,preview);
		}
	}

	@Test
	public void currentPoweredMagicWeaponBeatsOlderCastingOptionsWithAndWithoutEquipmentData()
	{
		for (int eye : new int[] {31113,31115})
		{
			BankSnapshot bank = bank(Arrays.asList(26219,25867,22323,21006,12899,eye),
				Collections.emptySet(),1,false);
			for (BankOrganizationPreview preview : Arrays.asList(build(bank),buildWithoutStats(bank)))
			{
				assertEquals("Eye of ayak is the available primary Magic weapon " + eye,
					eye, combat(preview).get(2).getItemId());
				assertEquals(ItemCategory.GEAR,item(preview,eye).getItemCategory());
				assertEquals("weapon",item(preview,eye).getSubcategory());
				assertPreserved(bank,preview);
			}
		}
	}

	@Test
	public void currentGloveAndNeckUpgradesHaveIndependentStyleTargetsAndReviewedFallbackSlots()
	{
		BankSnapshot bank = endgameBank(Collections.emptySet(),1,false);
		BankOrganizationPreview preview = build(bank);
		assertEquals("Confliction improves the Tormented magic hand slot",31106,
			combat(preview).get(6 * 8 + 2).getItemId());
		assertEquals("Rupture improves the Anguish ranged neck slot",33639,
			combat(preview).get(4 * 8 + 1).getItemId());
		assertEquals("Shadow remains primary when Eye is also available",27275,
			combat(preview).get(8 * 8 + 2).getItemId());
		assertPreserved(bank,preview);
		BankSnapshot sparse = bank(Arrays.asList(31106,33639),Collections.emptySet(),1,false);
		BankOrganizationPreview fallback = buildWithoutStats(sparse);
		assertEquals(ItemCategory.GEAR,item(fallback,31106).getItemCategory());
		assertEquals("hands",item(fallback,31106).getSubcategory());
		assertEquals(ItemCategory.GEAR,item(fallback,33639).getItemCategory());
		assertEquals("neck",item(fallback,33639).getSubcategory());
		assertPreserved(sparse,fallback);
	}

	@Test
	public void sanguineTorvaUsesTheSameStrengthFrontAndKeepsSpecialistArmourVertical()
	{
		List<Integer> ids = endgameBank(Collections.emptySet(),1,false).getItems().stream()
			.map(BankItemSnapshot::getItemId).collect(Collectors.toList());
		for (int row = 0; row < 3; row++)
			ids.set(ids.indexOf(PRIMARY_ARMOUR[0][row]),28254 + row * 2);
		BankSnapshot bank = bank(ids,Collections.emptySet(),1,false);
		BankOrganizationPreview preview = build(bank);
		List<Integer> combat = ids(combat(preview));
		for (int row = 0; row < 3; row++)
			assertEquals("Sanguine strength target " + row, 28254 + row * 2, (int) combat.get(row * 8));
		for (int[] family : SPECIALIST_ARMOUR) assertVertical(combat,family);
		assertPreserved(bank,preview);
	}

	@Test
	public void withdrawnGearAndReverseInputKeepExactlyTheSameCombatLayout()
	{
		BankSnapshot owned = endgameBank(Collections.emptySet(), 1, false);
		List<Integer> expected = ids(combat(build(owned)));
		for (BankSnapshot variant : Arrays.asList(endgameBank(WITHDRAWN, 1, false),
			endgameBank(Collections.emptySet(), 1, true), endgameBank(WITHDRAWN, 1, true)))
		{
			BankOrganizationPreview preview = build(variant);
			assertEquals("input order and withdrawal cannot change intended kit", expected, ids(combat(preview)));
			assertPreserved(variant, preview);
		}
	}

	@Test
	public void specialistArmourRemainsVerticalAndCannonRemainsOnePhysicalBlock()
	{
		BankSnapshot bank = endgameBank(Collections.emptySet(), 1, false);
		BankOrganizationPreview preview = build(bank);
		List<Integer> combat = ids(combat(preview));
		for (int[] family : SPECIALIST_ARMOUR) assertVertical(combat, family);
		for (int id : SPECIALIST_WEAPONS)
			assertEquals("specialist weapon is useful Combat gear " + id, "gear", item(preview,id).getLayoutTagKey());
		assertSameVerticalBlock(combat,Arrays.asList(28338,29796)); // Slash alternatives.
		assertSameVerticalBlock(combat,Arrays.asList(26219,22324,22978)); // Stab alternatives.
		assertSameVerticalBlock(combat,Arrays.asList(11802,11804,21003,13576)); // Special-attack switches.
		assertVertical(combat,new int[] {4716,4718,4720,4722}); // Dharok weapon belongs to its armour set.
		assertVertical(combat,new int[] {4753,4755,4757,4759}); // Verac weapon belongs to its armour set.
		assertRectangle(combat, CANNON);
		assertPreserved(bank, preview);
	}

	@Test
	public void bulkSpecialistArmourAndWeaponsAreNotAlchEvidence()
	{
		for (int quantity : new int[] {8,25})
		{
			BankSnapshot bank = endgameBank(Collections.emptySet(), quantity, false);
			BankOrganizationPreview preview = build(bank);
			for (int[] family : SPECIALIST_ARMOUR)
				for (int id : family)
					assertEquals("retain specialist armour x" + quantity + ": " + id,
						"gear", item(preview,id).getLayoutTagKey());
			for (int id : SPECIALIST_WEAPONS)
				assertEquals("retain specialist weapon x" + quantity + ": " + id,
					"gear", item(preview,id).getLayoutTagKey());
			assertPreserved(bank, preview);
		}
	}

	@Test
	public void personalCategoryChoicesAndSavedPhysicalOrderOverrideAutomaticPriorities()
	{
		BankSnapshot bank = endgameBank(Collections.emptySet(), 1, false);
		UserCategoryOverrides choices = new UserCategoryOverrides();
		choices.put(20997,"alch");
		choices.put(30753,"gear");
		UserCategoryOverrides restored = UserCategoryOverrides.parse(choices.serialize());
		BankOrganizationPreview routed = build(bank, restored, BlueprintItemOrders.EMPTY);
		assertEquals("explicit player Alch pin", "alch", item(routed,20997).getLayoutTagKey());
		assertEquals("explicit specialist Gear pin", "gear", item(routed,30753).getLayoutTagKey());
		List<Integer> desired = ids(combat(routed));
		Collections.reverse(desired);
		BlueprintItemOrders saved = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("gear"),desired).serialize());
		BankOrganizationPreview manual = build(bank, restored, saved);
		assertEquals("saved physical order wins over endgame primary/set priorities", desired, ids(combat(manual)));
		assertEquals("pin remains after saved order", "alch", item(manual,20997).getLayoutTagKey());
		assertPreserved(bank,manual);
	}

	private BankOrganizationPreview build(BankSnapshot bank)
	{
		return build(bank, CategoryOverrideSource.NONE, BlueprintItemOrders.EMPTY);
	}

	private BankOrganizationPreview buildWithoutStats(BankSnapshot bank)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan,
			BankLayoutOptions.defaultFor(preset));
	}

	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource choices, BlueprintItemOrders orders)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			id -> Optional.ofNullable(STATS.get(id)), id -> 25000, choices, plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(orders));
	}

	private List<BankPreviewItem> combat(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("gear")).getItems();
	}

	private static BankSnapshot endgameBank(Set<Integer> placeholders, int specialistQuantity, boolean reverse)
	{
		List<Integer> ids = new ArrayList<>(STATS.keySet());
		ids.removeAll(SCYTHES.subList(1,SCYTHES.size()));
		ids.remove(Integer.valueOf(27277));
		ids.removeAll(Arrays.asList(28254,28256,28258));
		ids.removeAll(Arrays.asList(25862,23973,23977,23981));
		ids.addAll(CANNON);
		return bank(ids, placeholders, specialistQuantity, reverse);
	}

	private static BankSnapshot bank(List<Integer> ids, Set<Integer> placeholders, int specialistQuantity, boolean reverse)
	{
		List<Integer> order = new ArrayList<>(ids);
		if (reverse) Collections.reverse(order);
		Set<Integer> specialist = new HashSet<>(SPECIALIST_WEAPONS);
		for (int[] family : SPECIALIST_ARMOUR)
			for (int id : family) specialist.add(id);
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : order)
		{
			boolean placeholder = placeholders.contains(id);
			int quantity = placeholder ? 0 : specialist.contains(id) ? specialistQuantity : 1;
			items.add(new BankItemSnapshot(id,quantity,items.size(),placeholder));
		}
		return new BankSnapshot(items);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(entry -> entry.getItemId() == id)
			.findFirst().orElseThrow(() -> new AssertionError("missing " + id));
	}

	private static void assertPreserved(BankSnapshot bank, BankOrganizationPreview preview)
	{
		Map<Integer,BankItemSnapshot> expected = new LinkedHashMap<>();
		for (BankItemSnapshot entry : bank.getItems())
			assertEquals("unique fixture ID " + entry.getItemId(), null, expected.put(entry.getItemId(),entry));
		List<BankPreviewItem> planned = preview.getPlannedItems();
		assertEquals("all stacks preserved", expected.size(), planned.size());
		assertEquals("no duplicated stacks", expected.size(), new HashSet<>(ids(planned)).size());
		for (BankPreviewItem entry : planned)
		{
			assertFalse("no invented blank", entry.isBlank());
			BankItemSnapshot source = expected.get(entry.getItemId());
			assertTrue("no invented ID " + entry.getItemId(), source != null);
			assertEquals("quantity " + entry.getItemId(), source.getQuantity(), entry.getQuantity());
			assertEquals("placeholder " + entry.getItemId(), source.isPlaceholder(), entry.isPlaceholder());
		}
	}

	private static void assertVertical(List<Integer> combat, int[] family)
	{
		int first = combat.indexOf(family[0]);
		assertTrue("missing family " + family[0], first >= 0);
		for (int row = 1; row < family.length; row++)
			assertEquals("physical vertical family " + family[0] + ":" + row,
				first + row * 8, combat.indexOf(family[row]));
	}

	private static void assertRectangle(List<Integer> combat, List<Integer> family)
	{
		List<Integer> positions = family.stream().map(combat::indexOf).collect(Collectors.toList());
		assertTrue("all block members exist", positions.stream().allMatch(index -> index >= 0));
		int minRow = positions.stream().mapToInt(index -> index / 8).min().getAsInt();
		int maxRow = positions.stream().mapToInt(index -> index / 8).max().getAsInt();
		int minCol = positions.stream().mapToInt(index -> index % 8).min().getAsInt();
		int maxCol = positions.stream().mapToInt(index -> index % 8).max().getAsInt();
		assertEquals("one filled rectangle without bank-edge wrap", family.size(),
			(maxRow - minRow + 1) * (maxCol - minCol + 1));
	}

	private static void assertSameVerticalBlock(List<Integer> combat, List<Integer> family)
	{
		List<Integer> positions = family.stream().map(combat::indexOf).sorted().collect(Collectors.toList());
		assertTrue("all weapon-role members exist " + family, positions.get(0) >= 0);
		for (int row = 1; row < positions.size(); row++)
			assertEquals("one vertical weapon-role block " + family, positions.get(0) + row * 8,
				positions.get(row).intValue());
	}

	// RuneLite item-stats snapshot, cached 2026-10-09. Each row is ID, equipment slot,
	// stab/slash/crush/magic/ranged attack, melee/ranged strength, prayer,
	// stab/slash/crush/magic/ranged defence, magic damage tenths, attack speed.
	// These preserve the misleading raw stat scores which previously beat raid weapons.
	private static final int[][] RAW_STATS = {
		{26382,0,0,0,0,-5,-5,8,0,1,59,60,62,-2,57,0,0},
		{26384,4,0,0,0,-18,-14,6,0,1,117,111,117,-11,142,0,0},
		{26386,7,0,0,0,-24,-11,4,0,1,87,78,79,-9,102,0,0},
		{28254,0,0,0,0,-5,-5,8,0,1,59,60,62,-2,57,0,0},
		{28256,4,0,0,0,-18,-14,6,0,1,117,111,117,-11,142,0,0},
		{28258,7,0,0,0,-24,-11,4,0,1,87,78,79,-9,102,0,0},
		{30750,0,0,10,0,-2,-7,6,0,0,50,72,45,0,50,0,0},
		{30753,4,0,16,0,-16,-18,4,0,0,105,128,100,-5,112,0,0},
		{30756,7,0,12,0,-12,-14,2,0,0,75,100,73,-3,81,0,0},
		{24419,0,0,0,10,-5,-5,6,0,1,19,10,21,0,12,0,0},
		{24420,4,0,0,16,-11,-10,4,0,2,67,55,71,0,35,0,0},
		{24421,7,0,0,12,-9,-5,2,0,2,42,30,49,0,22,0,0},
		{22326,0,0,0,0,-6,-2,0,0,2,60,63,59,-6,67,0,0},
		{22327,4,0,0,0,-40,-20,0,0,4,132,130,117,-16,142,0,0},
		{22328,7,0,0,0,-31,-17,0,0,4,95,92,93,-14,102,0,0},
		{27235,0,0,0,0,-1,12,0,2,1,8,10,12,12,9,0,0},
		{27238,4,0,0,0,-4,43,0,4,1,59,52,64,74,60,0,0},
		{27241,7,0,0,0,-2,27,0,2,1,35,30,39,46,37,0,0},
		{21018,0,0,0,0,8,-2,0,0,0,12,11,13,5,0,30,0},
		{21021,4,0,0,0,35,-8,0,0,0,42,31,51,28,0,30,0},
		{21024,7,0,0,0,26,-7,0,0,0,27,24,30,20,0,30,0},
		{28933,0,0,0,0,-6,-3,0,0,6,30,32,27,-1,30,0,0},
		{28936,4,0,0,0,-30,-15,0,0,10,82,80,72,-6,80,0,0},
		{28939,7,0,0,0,-4,-11,0,0,8,51,49,47,-4,49,0,0},
		{22325,3,70,125,30,-6,0,75,0,0,-2,8,10,0,0,0,5},
		{22486,3,50,75,10,-6,0,50,0,0,-2,6,0,0,0,0,5},
		{25736,3,70,125,30,-6,0,75,0,0,-2,8,10,0,0,0,5},
		{25738,3,50,75,10,-6,0,50,0,0,-2,6,0,0,0,0,5},
		{25739,3,70,125,30,-6,0,75,0,0,-2,8,10,0,0,0,5},
		{25741,3,50,75,10,-6,0,50,0,0,-2,6,0,0,0,0,5},
		{20997,3,0,0,0,0,70,0,20,0,0,0,0,0,0,0,6},
		{27275,3,0,0,0,35,0,0,0,1,0,0,0,20,0,0,5},
		{27277,3,0,0,0,35,0,0,0,1,0,0,0,20,0,0,5},
		{11802,3,0,132,80,0,0,132,0,8,0,0,0,0,0,0,6},
		{25867,3,0,0,0,0,128,0,106,0,0,0,0,0,0,0,5},
		{25862,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5},
		{21006,3,0,0,0,28,0,0,0,0,0,3,3,20,0,150,4},
		{22323,3,0,0,0,25,-4,0,0,0,2,3,1,15,0,0,4},
		{31113,3,0,0,0,30,0,0,0,2,1,5,5,10,0,0,3},
		{31115,3,0,0,0,30,0,0,0,2,1,5,5,10,0,0,3},
		{22324,3,100,55,0,0,0,93,0,0,0,0,0,0,0,0,4},
		{26219,3,105,75,0,0,0,103,0,0,0,0,0,0,0,0,5},
		{24417,3,52,-4,102,0,0,96,0,2,0,0,0,0,0,0,4},
		{21003,3,0,0,135,0,0,147,0,0,0,0,0,0,0,0,6},
		{11804,3,0,132,80,0,0,132,0,8,0,0,0,0,0,0,6},
		{13576,3,-4,-4,95,-4,0,85,0,0,0,0,0,0,0,0,6},
		{27610,3,0,0,0,0,90,0,25,0,0,0,0,0,0,0,5},
		{26374,3,0,0,0,0,110,0,0,1,14,14,12,15,16,0,6},
		{21295,1,4,4,4,1,1,8,0,2,12,12,12,12,12,0,0},
		{28951,1,0,0,0,0,18,0,3,0,0,0,0,0,0,0,0},
		{21791,1,0,0,0,15,0,0,0,0,3,3,3,15,0,20,0},
		{29801,2,25,25,25,-6,-8,12,0,2,0,0,0,0,0,0,0},
		{12002,2,0,0,0,12,0,0,0,2,0,0,0,0,0,50,0},
		{19547,2,0,0,0,0,15,0,5,2,0,0,0,0,0,0,0},
		{33639,2,0,0,0,0,20,0,8,3,0,0,0,0,0,0,0},
		{28307,12,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0},
		{22981,9,16,16,16,-16,-16,14,0,0,0,0,0,0,0,0,0},
		{26235,9,-8,-8,-8,0,18,0,2,1,8,8,8,5,8,0,0},
		{31097,10,5,5,5,11,15,6,3,0,21,25,25,10,10,20,0},
		{13239,10,2,2,2,-4,-1,5,0,0,22,22,22,0,0,0,0},
		{13237,10,0,0,0,-12,12,0,1,0,5,5,5,5,5,0,0},
		{13235,10,0,0,0,8,0,0,0,0,5,5,5,8,5,10,0},
		{19544,9,0,0,0,10,0,0,0,2,0,0,0,0,0,50,0},
		{31106,9,0,0,0,20,-4,0,0,2,15,18,7,5,5,70,0},
		{7462,9,12,12,12,6,12,12,0,0,12,12,12,6,12,0,0},
		{12817,5,0,0,0,0,0,0,0,3,63,65,75,2,57,0,0},
		{12825,5,0,0,0,20,0,0,0,3,53,55,73,2,52,30,0},
		{12821,5,0,0,0,0,0,0,0,3,53,55,73,30,52,0,0},
		{11283,5,0,0,0,-10,-5,7,0,0,70,75,72,10,72,0,0},
		{22002,5,-10,-10,-10,-10,15,-2,8,0,25,30,28,28,68,0,0},
		{21633,5,-10,-10,-10,15,-10,-2,0,0,72,80,75,15,-5,20,0},
		{27251,5,0,0,0,25,0,0,0,4,53,55,73,2,52,50,0},
		{21000,5,-7,-8,-7,-10,18,0,10,0,22,24,22,26,58,0,0},
		{22322,5,30,29,28,-5,-4,8,0,0,30,29,28,-5,-4,0,0},
		{11832,4,0,0,0,-15,-10,4,0,1,98,93,105,-6,133,0,0},
		{11834,7,0,0,0,-21,-7,2,0,1,71,63,66,-4,93,0,0},
		{11836,10,0,0,0,-5,-3,0,0,1,17,18,19,0,15,0,0},
		{11826,0,-5,-5,-5,-5,10,0,0,1,6,8,10,10,8,0,0},
		{11828,4,-7,-7,-7,-15,33,0,0,1,56,48,61,70,57,0,0},
		{11830,7,-6,-6,-6,-10,20,0,0,1,32,26,34,40,33,0,0},
		{23971,0,0,0,0,-10,9,0,0,2,12,8,14,10,18,0,0},
		{23975,4,0,0,0,-18,31,0,0,3,46,38,48,44,68,0,0},
		{23979,7,0,0,0,-12,18,0,0,2,26,21,30,34,38,0,0},
		{23973,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{23977,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{23981,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{26241,0,0,0,0,8,-3,0,0,1,15,14,16,6,0,20,0},
		{26243,4,0,0,0,35,-11,0,0,2,47,36,56,31,0,20,0},
		{26245,7,0,0,0,26,-9,0,0,1,31,28,34,22,0,20,0},
		{4716,0,0,0,0,-3,-1,0,0,0,45,48,44,-1,51,0,0},
		{4718,3,-4,103,95,-4,0,105,0,0,0,0,0,0,-1,0,7},
		{4720,4,0,0,0,-30,-10,0,0,0,122,120,107,-6,132,0,0},
		{4722,7,0,0,0,-21,-11,0,0,0,85,82,83,-4,92,0,0},
		{4753,0,0,0,0,-6,-2,0,0,3,55,58,54,0,56,0,0},
		{4755,3,68,-2,82,0,0,72,0,6,0,0,0,0,0,0,5},
		{4757,4,0,0,0,-6,-2,0,0,5,81,95,85,0,81,0,0},
		{4759,7,0,0,0,-21,-11,0,0,4,85,82,83,0,84,0,0},
		{12926,3,0,0,0,0,30,0,20,0,0,0,0,0,0,0,3},
		{12899,3,0,0,0,25,0,0,0,0,2,3,1,15,0,0,4},
		{22978,3,85,65,65,0,0,70,0,0,0,0,0,0,0,0,4},
		{28338,3,28,134,66,0,0,125,0,0,0,0,0,0,0,0,5},
		{24422,3,0,0,0,16,0,0,0,0,0,0,0,14,0,150,5},
		{24423,3,0,0,0,16,0,0,0,0,0,0,0,14,0,150,5},
		{24424,3,0,0,0,16,0,0,0,0,0,0,0,14,0,150,5},
		{24425,3,0,0,0,16,0,0,0,0,0,0,0,14,0,150,5},
		{19675,3,10,38,0,0,0,8,0,0,0,3,2,2,0,0,4},
		{29796,3,80,132,0,0,0,142,0,0,0,0,0,0,0,0,5},
		{28922,3,0,0,0,0,115,0,55,2,0,0,0,0,0,0,7}
	};
	private static final Map<Integer,GearStats> STATS = stats();

	private static Map<Integer,GearStats> stats()
	{
		Map<Integer,GearStats> result = new LinkedHashMap<>();
		for (int[] row : RAW_STATS)
		{
			if (row.length != 17) throw new AssertionError("invalid stat vector " + row[0]);
			GearSlot slot;
			switch (row[1])
			{
				case 0: slot = GearSlot.HEAD; break;
				case 1: slot = GearSlot.CAPE; break;
				case 2: slot = GearSlot.NECK; break;
				case 3: slot = GearSlot.WEAPON; break;
				case 4: slot = GearSlot.BODY; break;
				case 5: slot = GearSlot.SHIELD; break;
				case 7: slot = GearSlot.LEGS; break;
				case 9: slot = GearSlot.HANDS; break;
				case 10: slot = GearSlot.FEET; break;
				case 12: slot = GearSlot.RING; break;
				default: throw new AssertionError("unexpected slot " + row[1]);
			}
			GearStats stats = new GearStats(slot,row[2],row[3],row[4],row[5],row[6],row[7],row[8],row[9],
				row[10],row[11],row[12],row[13],row[14],row[15],row[16]);
			if (result.put(row[0],stats) != null) throw new AssertionError("duplicate stat vector " + row[0]);
		}
		return result;
	}
}

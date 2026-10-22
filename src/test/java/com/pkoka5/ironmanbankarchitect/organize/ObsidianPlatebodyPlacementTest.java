package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Actual planner coverage for the missing Obsidian body progression stage in issue 46. */
@RunWith(Parameterized.class)
public class ObsidianPlatebodyPlacementTest
{
	private static final int OBSIDIAN = 21301;
	private static final int RUNE_PLATE = 1127;
	private static final int RUNE_CHAIN = 1113;
	private static final int INITIATE = 5575;
	private static final int TORSO = 10551;
	private static final int NEITIZNOT = 10828;
	private static final int[] HELM_FILLERS = {1153,1155,1157,1159,1161,1163,1137,1139,1141,1143,1145,1147};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public ObsidianPlatebodyPlacementTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void obsidianOccupiesThePrimaryMeleeBodyCellAheadOfRuneAndInitiate()
	{
		BankSnapshot bank = bodyBank(false);
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE,
			withoutAlch(BlueprintItemOrders.EMPTY));
		List<Integer> combat = ids(preview, plan.destinationOf("gear"));
		assertEquals("The real best head anchors the Melee column", 0, combat.indexOf(NEITIZNOT));
		assertEquals("Obsidian belongs directly below the Melee head", 8, combat.indexOf(OBSIDIAN));
		for (int id : new int[] {RUNE_PLATE,RUNE_CHAIN,INITIATE})
		{
			assertTrue("Weaker or prayer-role body is not the primary Melee body: " + id,
				combat.indexOf(id) != 8);
		}
		assertConserved(bank, preview);
	}

	@Test
	public void fighterTorsoKeepsThePrimaryBodyCellWhenItIsAlsoAvailable()
	{
		List<BankItemSnapshot> entries = new ArrayList<>(bodyBank(false).getPhysicalItems());
		entries.add(new BankItemSnapshot(TORSO, 1, entries.size()));
		BankSnapshot bank = new BankSnapshot(entries);
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE,
			withoutAlch(BlueprintItemOrders.EMPTY));
		List<Integer> combat = ids(preview, plan.destinationOf("gear"));
		assertEquals("Fighter torso still wins the shared Mid stage on its equipment score", 8,
			combat.indexOf(TORSO));
		assertTag(preview, OBSIDIAN, "gear");
		assertConserved(bank, preview);
	}

	@Test
	public void reviewedObsidianReplacementRoutesRuneBodiesToAlchWhenOwnedOrWithdrawn()
	{
		for (boolean withdrawn : new boolean[] {false,true})
		{
			BankSnapshot bank = bank(withdrawn, OBSIDIAN,RUNE_CHAIN,RUNE_PLATE);
			BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE,
				BankLayoutOptions.defaultFor(preset));
			assertTag(preview, OBSIDIAN, "gear");
			assertTag(preview, RUNE_CHAIN, "alch");
			assertTag(preview, RUNE_PLATE, "alch");
			assertEquals("The upgrade's placeholder remains a real physical entry", withdrawn,
				item(preview, OBSIDIAN).isPlaceholder());
			assertConserved(bank, preview);
		}
	}

	@Test
	public void explicitCategoryCorrectionsOverrideBothAutomaticCombatAndAlchPlacement()
	{
		BankSnapshot bank = bank(false, OBSIDIAN,RUNE_CHAIN,RUNE_PLATE);
		CategoryOverrideSource choices = id -> id == OBSIDIAN ? Optional.of("tools")
			: id == RUNE_CHAIN ? Optional.of("gear") : Optional.empty();
		BankOrganizationPreview preview = build(bank, choices, BankLayoutOptions.defaultFor(preset));
		assertTag(preview, OBSIDIAN, "tools");
		assertTag(preview, RUNE_CHAIN, "gear");
		assertConserved(bank, preview);
	}

	@Test
	public void capturedPhysicalOrderKeepsRuneGearWhereThePlayerChoseToKeepIt()
	{
		BankSnapshot bank = bank(false, OBSIDIAN,RUNE_CHAIN,RUNE_PLATE,INITIATE,TORSO);
		BankOrganizationPreview before = build(bank, CategoryOverrideSource.NONE,
			withoutAlch(BlueprintItemOrders.EMPTY));
		List<Integer> chosen = Arrays.asList(RUNE_CHAIN,TORSO,RUNE_PLATE,INITIATE,OBSIDIAN);
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(
			plan.destinationOf("gear"), chosen), before, BlueprintItemOrders.EMPTY);
		BlueprintItemOrders restored = BlueprintItemOrders.parse(captured.serialize());
		BankOrganizationPreview preview = build(bank, CategoryOverrideSource.NONE,
			BankLayoutOptions.defaultFor(preset).withItemOrders(restored));
		assertTrue(restored.isCaptured());
		assertEquals("The player's exact physical order wins", chosen, ids(preview, plan.destinationOf("gear")));
		assertTrue(preview.getCategories().get(plan.destinationOf("gear")).hasManualOrder());
		assertConserved(bank, preview);
	}

	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource choices, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			id -> Optional.ofNullable(stats(id)), id -> 39000, choices, plan, options);
	}

	private BankLayoutOptions withoutAlch(BlueprintItemOrders orders)
	{
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		return new BankLayoutOptions(defaults.fillGearRows(), defaults.fillHerbloreRows(), false,
			Collections.emptyMap(), defaults.gearLayout(), defaults.potionDoses(), defaults.runeOrder(),
			defaults.teleportOrder(), defaults.gatherFrequentlyUsed()).withItemOrders(orders);
	}

	private static BankSnapshot bodyBank(boolean withdrawn)
	{
		List<Integer> ids = new ArrayList<>(Arrays.asList(RUNE_CHAIN,INITIATE,RUNE_PLATE,OBSIDIAN,NEITIZNOT));
		for (int id : HELM_FILLERS) ids.add(id);
		return bank(withdrawn, ids.stream().mapToInt(Integer::intValue).toArray());
	}

	private static BankSnapshot bank(boolean withdrawn, int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids)
		{
			boolean placeholder = withdrawn && id == OBSIDIAN;
			items.add(new BankItemSnapshot(id, placeholder ? 0 : 1, items.size(), placeholder));
		}
		return new BankSnapshot(items);
	}

	/** Frozen defence/strength vectors supplied with this repository's PR 47; attack penalties are neutral. */
	private static GearStats stats(int id)
	{
		switch (id)
		{
			case OBSIDIAN: return body(0,3,55,78,56,-15,60);
			case RUNE_PLATE: return body(0,0,82,80,72,-6,80);
			case RUNE_CHAIN: return body(0,0,63,72,78,-3,65);
			case INITIATE: return body(6,0,46,44,38,-6,44);
			case TORSO: return body(0,4,62,85,62,-10,67);
			case NEITIZNOT: return new GearStats(GearSlot.HEAD,0,0,0,0,0,3,0,3,31,29,34,3,30,0,0);
			default: return null;
		}
	}

	private static GearStats body(int prayer, int strength,
		int stabDefence, int slashDefence, int crushDefence, int magicDefence, int rangedDefence)
	{
		return new GearStats(GearSlot.BODY,0,0,0,0,0,strength,0,prayer,
			stabDefence,slashDefence,crushDefence,magicDefence,rangedDefence,0,0);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).findFirst()
			.orElseThrow(() -> new AssertionError("Missing item " + id));
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
	}

	private void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("Effective tag for " + id, tag, item(preview,id).getLayoutTagKey());
		assertTrue("Chosen destination contains " + id, ids(preview,plan.destinationOf(tag)).contains(id));
	}

	private static void assertConserved(BankSnapshot source, BankOrganizationPreview preview)
	{
		assertEquals(source.getPhysicalItems().size(), preview.getPlannedItemCount());
		List<Integer> result = preview.getPlannedItems().stream().map(BankPreviewItem::getItemId)
			.collect(Collectors.toList());
		for (BankItemSnapshot expected : source.getPhysicalItems())
		{
			assertEquals("Exactly one physical item " + expected.getItemId(), 1,
				Collections.frequency(result, expected.getItemId()));
			BankPreviewItem actual = item(preview, expected.getItemId());
			assertEquals(expected.getQuantity(), actual.getQuantity());
			assertEquals(expected.isPlaceholder(), actual.isPlaceholder());
		}
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class CombatLayoutQualityRegressionTest
{
	private static final int[][] SECONDARY_FAMILIES = {
		{30750, 30753, 30756}, {1163, 1127, 1079},
		{27235, 27238, 27241}, {11826, 11828, 11830},
		{21018, 21021, 21024}, {9672, 9674, 9676}
	};
	@Test
	public void knownCastingWeaponsUseMagicEvenWhenTheirMeleeBonusesAreHigher()
	{
		// RuneLite's 2026-10-09 item stats: Iban's staff (u), then Staff of fire.
		GearStats iban = new GearStats(GearSlot.WEAPON, 10, -1, 40, 10, 0, 50, 0, 0,
			2, 3, 1, 10, 0, 0, 5);
		GearStats fire = new GearStats(GearSlot.WEAPON, 3, -1, 9, 10, 0, 6, 0, 0,
			2, 3, 1, 10, 0, 0, 5);
		assertEquals("the raw attack vector demonstrates the original mistake", GearStyle.MELEE, iban.style());
		assertEquals(GearStyle.MELEE, fire.style());
		for (CatalogItem caster : Arrays.asList(catalog(12658, "Iban's staff (u)", "weapon"),
			catalog(1387, "Staff of fire", "weapon"), catalog(985001, "Master wand", "weapon"),
			catalog(985002, "Trident of the seas", "weapon"), catalog(28988, "Blue moon spear", "weapon")))
		{
			GearStats stats = caster.getItemId() == 1387 ? fire : iban;
			assertEquals(caster.getDisplayName(), GearStyle.MAGIC, GearItemSorter.styleOf(caster.getDisplayName(), stats));
			assertEquals(caster.getDisplayName() + " uses the magic weapon lane", 10,
				GearItemSorter.slotOf(caster.getDisplayName(), stats));
		}
	}

	@Test
	public void castingNameHintsDoNotChangeArmourOrAnUnrelatedMeleeWeapon()
	{
		GearStats tankHelm = styled(GearSlot.HEAD, GearStyle.MELEE, 10);
		assertEquals(GearStyle.MELEE, GearItemSorter.styleOf("Staff trophy helm", tankHelm));
		assertEquals(0, GearItemSorter.slotOf("Staff trophy helm", tankHelm));
		GearStats meleeWeapon = styled(GearSlot.WEAPON, GearStyle.MELEE, 60);
		assertEquals(GearStyle.MELEE, GearItemSorter.styleOf("Rune sword", meleeWeapon));
		assertEquals(8, GearItemSorter.slotOf("Rune sword", meleeWeapon));
	}

	@Test
	public void aCastingStaffCannotReplaceTheBestOwnedMeleeWeaponForAlchRouting()
	{
		int swordId = 1303;
		int casterId = 985004;
		assertTrue("fixture uses a reviewed ordinary alch weapon", IronmanAlchCandidateCatalog.contains(swordId));
		GearStats sword = styled(GearSlot.WEAPON, GearStyle.MELEE, 20);
		// Deliberately dominates every axis; the caster still has a different role.
		GearStats caster = new GearStats(GearSlot.WEAPON, 80, 80, 80, 20, 20, 80, 20, 2,
			20, 20, 20, 20, 20, 10, 0);
		assertTrue("full dominance alone cannot cross semantic combat roles", caster.dominates(sword));
		ItemCatalog catalog = id -> Optional.of(catalog(id,
			id == swordId ? "Rune sword" : "Iban's staff", "weapon"));
		GearStatsSource source = id -> Optional.of(id == swordId ? sword : caster);
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(swordId, 1, 0), new BankItemSnapshot(casterId, 1, 1)));
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, catalog, preset,
				source, id -> 20000);
			assertEquals(preset.getType() + ": both distinct weapon roles remain in Combat",
				Arrays.asList(swordId, casterId), ids(category(preview, "combat-gear").getItems()));
			assertEquals(preset.getType() + ": no melee weapon is displaced by a caster", 0,
				category(preview, "slayer-boss-loot").getItemCount());
		}
	}

	@Test
	public void magicDamageBreaksEqualTierAndAccuracyTiesWithoutChangingDominanceSafety()
	{
		int accuracyOnly = 985005;
		int damageBoost = 985006;
		GearStats plain = new GearStats(GearSlot.HEAD, 0, 0, 0, 20, 0, 0, 0, 0,
			10, 10, 10, 10, 10, 0, 0);
		GearStats damage = new GearStats(GearSlot.HEAD, 0, 0, 0, 20, 0, 0, 0, 0,
			10, 10, 10, 10, 10, 25, 0);
		GearStatsSource source = id -> Optional.of(id == damageBoost ? damage : plain);
		List<BankPreviewItem> input = Arrays.asList(
			item(accuracyOnly, "Caster hood alpha", "head"), item(damageBoost, "Caster hood zeta", "head"));
		assertEquals("magic damage matters before the alphabetical tie-break", damageBoost,
			GearItemSorter.plan(input, source).getSetupRows().get(0).getItemId());
		assertTrue(damage.dominates(plain));
		assertFalse(plain.dominates(damage));
	}

	@Test
	public void irrelevantMeleeBonusesCannotPromoteACastingStaff()
	{
		int bluntCaster = 985007;
		int accurateCaster = 985008;
		GearStats blunt = new GearStats(GearSlot.WEAPON, 80, 80, 80, 10, 0, 80, 0, 0,
			10, 10, 10, 10, 10, 0, 5);
		GearStats accurate = new GearStats(GearSlot.WEAPON, 1, 1, 1, 20, 0, 1, 0, 0,
			10, 10, 10, 10, 10, 0, 5);
		GearStatsSource source = id -> Optional.of(id == accurateCaster ? accurate : blunt);
		List<BankPreviewItem> input = Arrays.asList(item(bluntCaster, "Fabled staff alpha", "weapon"),
			item(accurateCaster, "Fabled staff zeta", "weapon"));
		assertEquals("caster ranking uses its casting bonuses", Arrays.asList(accurateCaster),
			ids(GearItemSorter.plan(input, source).getSetupRows()));
		assertFalse("the ranking is a preference, not proof for automatic disposal", accurate.dominates(blunt));
		assertFalse(blunt.dominates(accurate));
	}

	@Test
	public void unrelatedAccessoriesCannotPadAHelmRowOrConsumeASecondaryFamily()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		List<BankPreviewItem> input = new ArrayList<>();
		for (int style = 0; style < 4; style++)
		{
			int id = 985010 + style;
			input.add(item(id, "Primary " + style, "head"));
			stats.put(id, styled(GearSlot.HEAD, GearStyle.values()[style], 500));
		}
		for (int id = 985020; id < 985024; id++)
		{
			input.add(item(id, "Signet " + id, "ring"));
			stats.put(id, styled(GearSlot.RING, GearStyle.MELEE, 0));
		}
		input.add(item(985030, "Primary magic body", "body"));
		input.add(item(985031, "Primary magic legs", "legs"));
		stats.put(985030, styled(GearSlot.BODY, GearStyle.MAGIC, 500));
		stats.put(985031, styled(GearSlot.LEGS, GearStyle.MAGIC, 500));
		input.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(4101), 1));
		input.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(4103), 0, true));
		stats.put(4101, styled(GearSlot.BODY, GearStyle.MAGIC, 1));
		stats.put(4103, styled(GearSlot.LEGS, GearStyle.MAGIC, 1));
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		GearItemSorter.GearLayout plan = GearItemSorter.plan(input, source);
		assertEquals("a sparse row cannot borrow jewellery or another armour slot", 0, plan.getAlignedSize());
		assertEquals("only best-owned pieces form the compact front", 6, plan.getSetupRows().size());
		assertTrue(plan.getSetupRows().stream().noneMatch(entry -> entry.getSubcategory().equals("ring")));
		List<BankPreviewItem> tail = GearItemSorter.bySet(plan.getTail(), source);
		assertEquals("the partial Mystic family stays together in canonical slot order", Arrays.asList(4101, 4103),
			ids(tail.subList(0, 2)));
		assertTrue("the placeholder retains its identity and quantity", tail.get(1).isPlaceholder());
		assertEquals(0, tail.get(1).getQuantity());
		List<BankPreviewItem> all = GearItemSorter.layout(input, source);
		assertEquals(input.size(), all.size());
		assertEquals(new HashSet<>(ids(input)), new HashSet<>(ids(all)));
		assertTrue(all.stream().noneMatch(BankPreviewItem::isBlank));
	}

	@Test
	public void remainingFamiliesStayInDeterministicStyleBlocksThenProgressionOrder()
	{
		int[][] families = SECONDARY_FAMILIES;
		List<BankPreviewItem> input = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (int family = 0; family < families.length; family++)
		{
			GearStyle style = family < 2 ? GearStyle.MELEE : family < 4 ? GearStyle.RANGED
				: family == 4 ? GearStyle.MAGIC : GearStyle.PRAYER;
			for (int slot = 0; slot < families[family].length; slot++)
			{
				int id = families[family][slot];
				input.add(new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id), 1));
				expected.add(id);
				stats.put(id, styled(new GearSlot[]{GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS}[slot], style, 10));
			}
		}
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		Random random = new Random(20261009L);
		for (int shuffle = 0; shuffle < 20; shuffle++)
		{
			Collections.shuffle(input, random);
			assertEquals("shuffle " + shuffle + ": Melee, Ranged, Magic, Prayer; strongest family first within a style",
				expected, ids(GearItemSorter.bySet(input, source)));
		}
	}

	@Test
	public void aPartialVeracFamilyUsesItsArmourRoleInsteadOfItsFlailForStyleGrouping()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(4755, styled(GearSlot.WEAPON, GearStyle.MELEE, 80));
		stats.put(4757, styled(GearSlot.BODY, GearStyle.PRAYER, 5));
		stats.put(11828, styled(GearSlot.BODY, GearStyle.RANGED, 20));
		stats.put(11830, styled(GearSlot.LEGS, GearStyle.RANGED, 20));
		List<BankPreviewItem> input = Arrays.asList(4757, 11830, 4755, 11828).stream()
			.map(id -> new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id), 1))
			.collect(Collectors.toList());
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		assertEquals("Ranged armour precedes Prayer armour even when the Prayer family owns a melee flail",
			Arrays.asList(11828, 11830, 4755, 4757), ids(GearItemSorter.bySet(input, source)));
	}

	@Test
	public void fullDefaultPreviewsKeepBestStyleColumnsAndSecondarySetsVerticalInTheSharedGrid()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		Map<Integer, CatalogItem> primaryCatalog = new LinkedHashMap<>();
		List<Integer> primaryIds = new ArrayList<>();
		List<BankItemSnapshot> entries = new ArrayList<>();
		GearSlot[] slots = {GearSlot.HEAD, GearSlot.BODY, GearSlot.LEGS};
		for (GearStyle style : Arrays.asList(GearStyle.MELEE, GearStyle.RANGED, GearStyle.MAGIC, GearStyle.PRAYER))
		{
			for (GearSlot slot : slots)
			{
				int id = 985100 + primaryIds.size();
				primaryIds.add(id);
				primaryCatalog.put(id, catalog(id, "Primary " + style + " " + slot, slot.name().toLowerCase()));
				stats.put(id, styled(slot, style, 500));
				entries.add(new BankItemSnapshot(id, 1, entries.size()));
			}
		}
		Map<Integer, Integer> secondaryStyles = new LinkedHashMap<>();
		for (int family = 0; family < SECONDARY_FAMILIES.length; family++)
		{
			int style = family < 2 ? 0 : family < 4 ? 1 : family == 4 ? 2 : 3;
			for (int slot = 0; slot < slots.length; slot++)
			{
				int id = SECONDARY_FAMILIES[family][slot];
				secondaryStyles.put(id, style);
				stats.put(id, styled(slots[slot], GearStyle.values()[style], 10));
				entries.add(new BankItemSnapshot(id, 1, entries.size()));
			}
		}
		ItemCatalog catalog = id -> primaryCatalog.containsKey(id)
			? Optional.of(primaryCatalog.get(id)) : CompositeItemCatalog.DEFAULT.findById(id);
		// Six secondary columns need physical height beyond the three anchored
		// primary rows. Real loose entries fill the intervening bank cells.
		for (int index = 0; index < 16; index++)
		{
			int id = 985300 + index;
			primaryCatalog.put(id, catalog(id, "Loose support item " + index, "gear"));
			entries.add(new BankItemSnapshot(id, 1, entries.size()));
		}
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
		{
			BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(entries), catalog,
				preset, source, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
			List<Integer> combat = ids(preview.getCategories().get(plan.destinationOf("gear")).getItems());
			for (int style = 0; style < 4; style++)
				for (int row = 0; row < 3; row++)
					assertEquals(preset.getType() + ": best gear keeps its vertical style column",
						row * 8 + style, combat.indexOf(primaryIds.get(style * 3 + row)));
			for (int[] family : SECONDARY_FAMILIES)
			{
				int first = combat.indexOf(family[0]);
				for (int piece = 0; piece < family.length; piece++)
					assertEquals(preset.getType() + ": secondary family reads vertically",
						first + piece * 8, combat.indexOf(family[piece]));
			}
			assertEquals(entries.size(), preview.getPlannedItemCount());
		}
	}

	// Synthetic stat vectors isolate roles and ranking; they are not claims about live item bonuses.
	private static GearStats styled(GearSlot slot, GearStyle style, int strength)
	{
		int melee = style == GearStyle.MELEE ? strength : 0;
		int ranged = style == GearStyle.RANGED ? strength : 0;
		int magic = style == GearStyle.MAGIC ? strength : 0;
		int prayer = style == GearStyle.PRAYER ? strength : 0;
		return new GearStats(slot, melee, melee, melee, magic, ranged, melee, ranged, prayer,
			10, 10, 10, 10, 10, 0, 0);
	}

	private static BankCategoryPreview category(BankOrganizationPreview preview, String key)
	{
		return preview.getCategories().stream().filter(category -> category.getCategory().getKey().equals(key))
			.findFirst().orElseThrow(() -> new AssertionError("Missing category " + key));
	}

	private static CatalogItem catalog(int id, String name, String slot)
	{
		return new CatalogItem(id, name, ItemCategory.GEAR, slot, Collections.emptySet(), null);
	}

	private static BankPreviewItem item(int id, String name, String slot)
	{
		return new BankPreviewItem(catalog(id, name, slot), 1);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

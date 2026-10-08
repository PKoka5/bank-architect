package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Small synthetic banks reproducing the reported choices, without private bank exports. */
public class CommunityFeedbackPlacementTest
{
	@Test public void upgradedQuickToolsReplaceOrdinaryVersionsAndRespectManualChoices()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		for (int hammer : new int[]{25644, 29775})
		{
			BankSnapshot bank = bank(1755, 2347, 34024, hammer, 952, 995);
			BankOrganizationPreview preview = build(bank, new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
			assertEquals(Arrays.asList(995, hammer, 34024, 952), ids(preview, 0));
			assertTrue(ids(preview, plan.destinationOf("tools")).containsAll(Arrays.asList(2347, 1755)));
			BankOrganizationPreview manual = build(bank,
				UserCategoryOverrides.parse("2347=frequently-used,34024=tools"), plan, BankLayoutOptions.DEFAULTS);
			assertTrue(ids(manual, 0).contains(2347));
			assertFalse(ids(manual, 0).contains(34024));
			assertTrue(ids(manual, plan.destinationOf("tools")).contains(34024));
			BankLayoutOptions noGather = new BankLayoutOptions(true, true, true, Collections.emptyMap(),
				GearLayout.GRID_STYLES, PotionDoseOrder.GRAB_AREA, RuneOrder.ALPHABETICAL,
				TeleportOrder.ALPHABETICAL, false);
			BankOrganizationPreview disabled = build(bank, new UserCategoryOverrides(), plan, noGather);
			assertTrue(ids(disabled, plan.destinationOf("tools")).containsAll(Arrays.asList(2347, 1755, hammer, 34024)));
		}
		assertEquals("Jeweller's chisel", CompositeItemCatalog.DEFAULT.describeOrUnknown(34024).getDisplayName());
	}

	@Test public void repeatableQuestAndPrayerUtilitiesAvoidCleanupAndRawResources()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int[] all = {22398, 22986, 25781, 24416, 9433, 2963, 5295, 5315, 5373, 5341};
		BankOrganizationPreview preview = build(bank(all), new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(preview, plan.destinationOf("gear")).containsAll(Arrays.asList(22398, 22986)));
		assertTrue(ids(preview, plan.destinationOf("tools")).containsAll(Arrays.asList(25781, 2963)));
		assertTrue(ids(preview, plan.destinationOf("ammunition")).contains(9433));
		assertTrue(ids(preview, plan.destinationOf("runes")).contains(24416));
		BankOrganizationPreview pinned = build(bank(all), UserCategoryOverrides.parse("5341=frequently-used"),
			plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(pinned, plan.destinationOf("frequently-used")).contains(5341));
		assertFalse(ids(pinned, plan.destinationOf("tools")).contains(5341));
	}

	@Test public void functionalQuestGearAndUsageFactsPreserveDestinations()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int[] items = {2402, 6745, 6746, 6106, 6107, 6108, 6109, 6110, 6111, 4300, 19689, 772};
		BankOrganizationPreview preview = build(bank(items), new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(preview, plan.destinationOf("gear")).containsAll(
			Arrays.asList(2402, 6745, 6746, 6106, 6107, 6108, 6109, 6110, 6111)));
		assertTrue(ids(preview, plan.destinationOf("cosmetics")).containsAll(Arrays.asList(4300, 19689)));
		BankOrganizationPreview pinned = build(bank(items), UserCategoryOverrides.parse("772=frequently-used"),
			plan, BankLayoutOptions.DEFAULTS);
		BankPreviewItem staff = pinned.getCategories().stream().flatMap(tab -> tab.getItems().stream())
			.filter(item -> item.getItemId() == 772).findFirst().get();
		assertEquals("frequently-used", staff.getLayoutTagKey());
		assertTrue(staff.hasTag("transport-access"));
	}

	@Test public void reviewedCosmeticOutfitsStayTogetherAndRespectManualAssignments()
	{
		int[][] outfits = {{6182, 6180, 6181}, {22689, 22692, 22695, 22698, 22701},
			{7594, 19912, 7592, 7593, 7595, 7596}};
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int[] all = Arrays.stream(outfits).flatMapToInt(Arrays::stream).toArray();
		BankOrganizationPreview preview = build(bank(all), new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
		List<Integer> cosmetics = ids(preview, plan.destinationOf("cosmetics"));
		for (int[] outfit : outfits)
		{
			List<Integer> positions = new ArrayList<>();
			for (int id : outfit)
			{
				assertEquals("cosmetic " + id, "cosmetic", CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getSubcategory());
				assertTrue("cosmetic " + id, cosmetics.contains(id));
				positions.add(cosmetics.indexOf(id));
			}
			assertEquals("outfit must not be interleaved", outfit.length - 1,
				Collections.max(positions) - Collections.min(positions));
		}
		BankOrganizationPreview manual = build(bank(all), UserCategoryOverrides.parse("6182=frequently-used"),
			plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(manual, plan.destinationOf("frequently-used")).contains(6182));
		assertFalse(ids(manual, plan.destinationOf("cosmetics")).contains(6182));
		for (int id : new int[]{775, 776, 1580})
			assertEquals("functional gauntlets " + id, "skilling-utility",
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getSubcategory());
	}

	@Test public void hunterConsumablesAndToolsUseTheirFunctionalTabs()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int[] consumables = {10014, 10016, 10018, 10020, 28890, 28893};
		int[] tools = {10006, 10008, 10029, 10031, 10150, 10010, 11259, 10012, 29295, 29297, 29299, 29301, 29303,
			29309, 29462, 29464, 29466, 29468, 29470};
		for (int id : consumables)
			assertEquals("consumable " + id, "potions-food", PresetCategoryMapper.map(BankPresets.IRONMAN,
				CompositeItemCatalog.DEFAULT.describeOrUnknown(id)).getKey());
		BankOrganizationPreview preview = build(bank(tools), new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
		for (int id : tools) assertTrue("hunter tool " + id, ids(preview, plan.destinationOf("tools")).contains(id));
	}

	@Test
	public void equalCompleteGracefulRecoloursStayStableAfterSortingAndReanalysis()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		// Two complete non-base recolours in a 53-item Main. Preserve the other
		// tabs too: reanalysis receives the whole bank, not only its Main section.
		// Frog token keeps an off-Main control now that Tokkul follows Currency.
		int[] original = {13579, 13583, 13585, 13587, 13589, 13581,
			13627, 13629, 13631, 13633, 13635, 13637, 995, 556, 558, 562,
			554, 555, 559, 564, 557, 561, 563, 565, 560, 566, 9075, 21880,
			2347, 1755, 952, 8013, 4251, 21389, 12791, 13393, 19564,
			25818, 24711, 22400, 30638, 32399, 13660, 3853, 6183, 13204,
			4699, 4698, 4697, 4696, 4695, 4694, 28929, 30843, 11832, 25781, 385};
		List<Integer> expected = null;
		for (int seed = 0; seed < 12; seed++)
		{
			List<Integer> shuffled = Arrays.stream(original).boxed().collect(Collectors.toList());
			Collections.shuffle(shuffled, new java.util.Random(seed));
			BankOrganizationPreview preview = build(bank(shuffled.stream().mapToInt(Integer::intValue).toArray()),
				new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
			assertEquals("Main fixture", 53, ids(preview, 0).size());
			List<Integer> ordered = allIds(preview);
			assertEquals("No bank items lost", original.length, ordered.size());
			if (expected == null) expected = ordered;
			assertEquals("Shuffled bank " + seed, expected, ordered);
			for (int reopen = 0; reopen < 2; reopen++)
			{
				preview = build(bank(ordered.stream().mapToInt(Integer::intValue).toArray()),
					new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
				ordered = allIds(preview);
				assertEquals("Reanalysis " + reopen + " of shuffled bank " + seed, expected, ordered);
			}
		}
	}

	@Test
	public void familyPotionOrderPreservesItemsWithLegacyCategoryCorrections()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("potions", 4).withTagAt("potion-doses", 8).withTagAt("tools", 6);
		BankLayoutOptions options = new BankLayoutOptions(true, true, true, Collections.emptyMap(),
			GearLayout.GRID_STYLES, PotionDoseOrder.BY_FAMILY, RuneOrder.ALPHABETICAL,
			TeleportOrder.ALPHABETICAL);
		for (BankCategory category : BankPresets.IRONMAN.getCategories())
		{
			UserCategoryOverrides overrides = UserCategoryOverrides.parse(
				"121=" + category.getKey() + ",139=" + category.getKey());
			BankOrganizationPreview preview = build(bank(121, 139, 995), overrides, plan, options);
			String tag = "potions-food".equals(category.getKey()) ? "potions"
				: BankTags.tagFor(category.getKey(), "potion-dose-3").getKey();
			assertEquals("Item count for " + category.getKey(), 3, allIds(preview).size());
			assertTrue("Correct destination for " + category.getKey(),
				ids(preview, plan.destinationOf(tag)).containsAll(Arrays.asList(121, 139)));
			assertEquals("Tag count for " + category.getKey(), Integer.valueOf(2), preview.getTagCounts().get(tag));
		}
		BankOrganizationPreview pinned = build(bank(121, 139, 995),
			UserCategoryOverrides.parse("121=tools,139=tools"), plan, options);
		assertEquals(3, allIds(pinned).size());
		assertTrue(ids(pinned, plan.destinationOf("tools")).containsAll(Arrays.asList(121, 139)));
	}

	@Test
	public void completedMainWithLunarStaffStaysCompleteAfterReanalysis()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		int[] original = {995, 6739, 11920, 2347, 1755, 952, 12791,
			556, 558, 562, 554, 555, 559, 564, 557, 561, 563, 565, 560,
			566, 9075, 21880, 9084, 13660, 13393, 3853, 4251, 8013, 21389,
			11850, 11852, 11854, 11856, 11858, 11860};
		for (String tag : Arrays.asList("frequently-used", "teleports"))
		{
			UserCategoryOverrides overrides = UserCategoryOverrides.parse("9084=" + tag);
			BankLayoutOptions options = new BankLayoutOptions(true, true, true, Collections.emptyMap(),
				GearLayout.GRID_STYLES, PotionDoseOrder.GRAB_AREA, RuneOrder.ELEMENTAL, TeleportOrder.SPELLBOOK_FIRST);
			List<Integer> expected = null;
			for (int seed = 0; seed < 12; seed++)
			{
				List<Integer> shuffled = Arrays.stream(original).boxed().collect(Collectors.toList());
				Collections.shuffle(shuffled, new java.util.Random(seed));
				BankOrganizationPreview preview = build(bank(shuffled.stream().mapToInt(Integer::intValue).toArray()),
					overrides, plan, options);
				List<Integer> ordered = preview.getCategories().stream().flatMap(tab -> tab.getItems().stream())
					.filter(item -> !item.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList());
				if (expected == null) expected = ordered;
				assertEquals("shuffled input " + seed, expected, ordered);
				BankOrganizationPreview reopened = build(bank(ordered.stream().mapToInt(Integer::intValue).toArray()),
					overrides, plan, options);
				assertEquals("reopened bank " + seed, ids(preview, 0), ids(reopened, 0));
			}
		}
	}

	@Test public void sunfireArmourRoutesToGearAndLitLanternsToTools()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankOrganizationPreview preview = build(bank(28933, 28936, 28939, 26822, 26824, 26848),
			new UserCategoryOverrides(), plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(preview, plan.destinationOf("gear")).containsAll(Arrays.asList(28933, 28936, 28939)));
		assertTrue(ids(preview, plan.destinationOf("tools")).containsAll(Arrays.asList(26822, 26824, 26848)));
	}
	@Test
	public void platinumAssignedToFrequentlyUsedPrecedesTeleportsInGridAndList()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("13204=frequently-used");
		for (TabOrder order : new TabOrder[]{TabOrder.PACKED, TabOrder.SEQUENTIAL})
		{
			BankLayoutOptions options = new BankLayoutOptions(true, true, true,
				Collections.singletonMap(BankCategorySortMode.MAIN, order));
			BankOrganizationPreview preview = build(bank(995, 2347, 13204, 563, 8013, 6529),
				overrides, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options);
			List<Integer> ids = ids(preview, 0);
			assertTrue("Chosen quick-access tag must precede teleports: " + ids,
				ids.indexOf(13204) < ids.indexOf(8013));
			assertEquals(Integer.valueOf(2), preview.getTagCounts().get("frequently-used"));
			BankPreviewItem platinum = preview.getCategories().get(0).getItems().stream()
				.filter(item -> item.getItemId() == 13204).findFirst().get();
			assertEquals("frequently-used", platinum.getLayoutTagKey());
			assertEquals("currency", platinum.getSubcategory());
			assertTrue(BankBlueprintTextExporter.export(preview).contains("layoutTag=frequently-used"));
		}
	}

	@Test
	public void platinumDestinationSurvivesStorageAndClearingRestoresCurrency()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("frequently-used", 4).withTagAt("currency", 6);
		UserCategoryOverrides saved = UserCategoryOverrides.parse("13204=frequently-used");
		UserCategoryOverrides restored = UserCategoryOverrides.parse(saved.serialize());
		BankSnapshot bank = bank(995, 13204);
		assertTrue(ids(build(bank, restored, plan, BankLayoutOptions.DEFAULTS), 4).contains(13204));
		restored.remove(13204);
		BankOrganizationPreview automatic = build(bank,
			UserCategoryOverrides.parse(restored.serialize()), plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(automatic, 6).contains(13204));
		assertFalse(ids(automatic, 4).contains(13204));
	}

	@Test
	public void mysticHatOutranksTheBlackWizardHatAfterManualGearAssignment()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("4089=gear,1017=gear");
		GearStatsSource magicHats = id -> Optional.of(new GearStats(GearSlot.HEAD,
			0, 0, 0, id == 4089 ? 4 : 2, 0, 0, 0, 0, id == 4089 ? 4 : 2));
		for (GearStatsSource stats : new GearStatsSource[]{GearStatsSource.NONE, magicHats})
		{
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank(4089, 1017),
				CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN, stats, ItemValueSource.NONE,
				overrides, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
			assertEquals(Arrays.asList(4089, 1017), ids(preview, 1));
		}
	}

	@Test
	public void effectiveTagSurvivesPhysicalCopiesAndPlaceholders()
	{
		BankSnapshot bank = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(13204, 1, 0), new BankItemSnapshot(13204, 0, 1, true)));
		BankOrganizationPreview preview = build(bank, UserCategoryOverrides.parse("13204=frequently-used"),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
		assertEquals(Arrays.asList(13204, 13204), ids(preview, 0));
		for (BankPreviewItem item : preview.getCategories().get(0).getItems())
		{
			assertEquals("frequently-used", item.getLayoutTagKey());
		}
	}

	@Test
	public void explicitTagOrderStillWinsOverDefaultMainRoles()
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagShifted("teleports", -1).withTagShifted("teleports", -1);
		BankLayoutOptions list = new BankLayoutOptions(true, true, true,
			Collections.singletonMap(BankCategorySortMode.MAIN, TabOrder.SEQUENTIAL));
		List<Integer> ids = ids(build(bank(995, 13204, 8013),
			UserCategoryOverrides.parse("13204=frequently-used"), plan, list), 0);
		assertTrue(ids.toString(), ids.indexOf(8013) < ids.indexOf(13204));
	}

	@Test
	public void moonSetsStayWholeInTheSetList()
	{
		int[] bankIds = {29004, 29028, 29016, 29007, 29022, 29019, 29010, 29025, 29013};
		BankLayoutOptions options = new BankLayoutOptions(true, true, true,
			Collections.emptyMap(), GearLayout.LIST, PotionDoseOrder.GRAB_AREA,
			RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL);
		List<Integer> laidOut = ids(build(bank(bankIds), new UserCategoryOverrides(),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options), 1);
		for (List<Integer> set : Arrays.asList(Arrays.asList(29028, 29022, 29025),
			Arrays.asList(29010, 29004, 29007), Arrays.asList(29019, 29013, 29016)))
		{
			int start = laidOut.indexOf(set.get(0));
			assertTrue("Missing set: " + laidOut, start >= 0 && start + 3 <= laidOut.size());
			assertEquals(set, laidOut.subList(start, start + 3));
		}
	}

	@Test
	public void clueEquipmentCanBeAssignedWithoutChangingOtherGear()
	{
		UserCategoryOverrides overrides = UserCategoryOverrides.parse("12480=clues,23209=clues");
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		BankOrganizationPreview preview = build(bank(12480, 23209, 11832), overrides,
			plan, BankLayoutOptions.DEFAULTS);
		assertTrue(ids(preview, plan.destinationOf("clues")).containsAll(Arrays.asList(12480, 23209)));
		assertTrue(ids(preview, plan.destinationOf("gear")).contains(11832));
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int i = 0; i < ids.length; i++) items.add(new BankItemSnapshot(ids[i], 1, i));
		return new BankSnapshot(items);
	}

	private static BankOrganizationPreview build(BankSnapshot bank, UserCategoryOverrides overrides,
		BankLayoutPlan plan, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE, overrides, plan, options);
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		return preview.getCategories().get(tab).getItems().stream().filter(item -> !item.isBlank())
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<Integer> allIds(BankOrganizationPreview preview)
	{
		return preview.getCategories().stream().flatMap(tab -> tab.getItems().stream())
			.filter(item -> !item.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
}

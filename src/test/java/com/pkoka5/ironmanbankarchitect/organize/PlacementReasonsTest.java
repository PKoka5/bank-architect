package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Provenance describes the decision that actually won without changing any physical placement. */
@RunWith(Parameterized.class)
public class PlacementReasonsTest
{
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}") public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public PlacementReasonsTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test public void itemCopiesAndPhysicalExpansionPreserveTheReasonWithoutMutatingTheirSource()
	{
		BankPreviewItem original = new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(1393),
			3, false, Arrays.asList(1, 2, 0)).withLayoutTag("gear");
		assertEquals("", original.getPlacementReason());
		BankPreviewItem reasoned = original.withPlacementReason("Fixture provenance");
		BankPreviewItem copied = reasoned.withLayoutTag("tools").withBlueprintOccurrence(9);
		assertEquals("Fixture provenance", copied.getPlacementReason());
		assertEquals("gear", original.getLayoutTagKey());
		assertEquals("", original.getPlacementReason());
		assertEquals(3, copied.getQuantity());
		assertEquals(1393, copied.getItemId());
		assertEquals(9, copied.getBlueprintOccurrence());
		BankCategoryPreview expanded = BankCategoryPreview.fromLogicalItems(preset.getCategory("combat-gear"),
			Collections.singletonList(copied));
		assertEquals(Arrays.asList(1, 2, 0), expanded.getItems().stream().map(BankPreviewItem::getQuantity).collect(Collectors.toList()));
		for (BankPreviewItem physical : expanded.getItems())
		{
			assertEquals(1393, physical.getItemId());
			assertEquals("tools", physical.getLayoutTagKey());
			assertEquals("Fixture provenance", physical.getPlacementReason());
			assertEquals(physical.getQuantity() == 0, physical.isPlaceholder());
		}
	}

	@Test public void allOneHundredFiveFixedItemsExplainTheListWithoutNeedingRuntimeMetadata()
	{
		List<Integer> ids = new OrderedItemFamilies(PlacementReasonsTest.class.getResourceAsStream("alch-candidates.tsv"),
			0, false).ids("fixed-list");
		assertEquals(105, ids.size());
		for (int quantity : new int[] {1, 0})
		{
			List<BankItemSnapshot> entries = new ArrayList<>();
			for (int id : ids) entries.add(entry(id, quantity, entries.size()));
			BankOrganizationPreview preview = build(new BankSnapshot(entries), CategoryOverrideSource.NONE,
				options(AlchMode.FIXED), GearStatsSource.NONE, ItemValueSource.NONE);
			for (BankPreviewItem item : preview.getPlannedItems())
			{
				assertEquals("alch", item.getLayoutTagKey());
				assertEquals("Fixed Alch list.", item.getPlacementReason());
				assertFalse(item.getPlacementReason().contains("Your"));
			}
		}
	}

	@Test public void validPersonalTagsAndLegacyCategoriesIdentifyThePlayersOwnChoice()
	{
		for (AlchMode mode : AlchMode.values())
			for (String choice : Arrays.asList("tools", "combat-gear"))
			{
				BankOrganizationPreview preview = build(bank(entry(1127, 1, 0)), id -> Optional.of(choice), options(mode));
				BankPreviewItem item = item(preview, 1127);
				assertEquals(choice.equals("tools") ? "tools" : "gear", item.getLayoutTagKey());
				assertTrue(item.getPlacementReason().startsWith("Your category choice."));
			}
	}

	@Test public void invalidOverridesKeepTheAutomaticReasonAndDestination()
	{
		for (AlchMode mode : AlchMode.values())
		{
			BankSnapshot bank = bank(entry(1127, 25, 0));
			BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, options(mode));
			BankOrganizationPreview invalid = build(bank, id -> Optional.of("not-a-real-category"), options(mode));
			assertEquals(BlueprintChanges.capture(automatic), BlueprintChanges.capture(invalid));
			assertEquals(item(automatic, 1127).getPlacementReason(), item(invalid, 1127).getPlacementReason());
			assertFalse(item(invalid, 1127).getPlacementReason().contains("Your"));
		}
	}

	@Test public void retainedAutomaticOverridesAreNeverDescribedAsPersonalChoices()
	{
		CategoryOverrideSource retained = new CategoryOverrideSource()
		{
			@Override public Optional<String> categoryKeyFor(int id) { return id == 1127 ? Optional.of("alch") : Optional.empty(); }
			@Override public boolean isPersonalChoice(int id) { return false; }
		};
		for (AlchMode mode : AlchMode.values())
		{
			BankPreviewItem result = item(build(bank(entry(1127, 1, 0)), retained, options(mode)), 1127);
			assertEquals("alch", result.getLayoutTagKey());
			assertEquals("Preserved previous Alch decision.", result.getPlacementReason());
			assertFalse(result.getPlacementReason().contains("Your"));
		}
	}

	@Test public void activeSavedDestinationsExplainTheMoveWhileDormantRoutesKeepTheAutomaticReason()
	{
		BankSnapshot bank = bank(entry(1127, 1, 0));
		for (AlchMode mode : AlchMode.values())
		{
			BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, options(mode));
			String originalTag = item(automatic, 1127).getLayoutTagKey();
			int destination = plan.destinationOf("tools");
			BlueprintItemOrders active = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
				"1127#0", new BlueprintItemOrders.Destination(destination, originalTag, "tools")));
			BankOrganizationPreview saved = build(bank, CategoryOverrideSource.NONE, options(mode).withItemOrders(active));
			assertEquals("tools", item(saved, 1127).getLayoutTagKey());
			assertEquals("Saved item destination.", item(saved, 1127).getPlacementReason());
			assertAt(saved, 1127, destination);
			BlueprintItemOrders dormant = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
				"1127#0", new BlueprintItemOrders.Destination((destination + 1) % 10, originalTag, "tools")));
			BankOrganizationPreview ignored = build(bank, CategoryOverrideSource.NONE, options(mode).withItemOrders(dormant));
			assertEquals(BlueprintChanges.capture(automatic), BlueprintChanges.capture(ignored));
			assertEquals(item(automatic, 1127).getPlacementReason(), item(ignored, 1127).getPlacementReason());
		}
	}

	@Test public void capturedPerCopyRoutesExplainSavedPositionsForBothPhysicalCopies()
	{
		BankSnapshot bank = bank(entry(1393, 1, 0), entry(1393, 1, 1));
		BankLayoutOptions options = options(AlchMode.FIXED);
		BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, options);
		Map<Integer, List<Integer>> tabs = new LinkedHashMap<>();
		tabs.put(0, Collections.singletonList(1393));
		tabs.put(plan.destinationOf("tools"), Collections.singletonList(1393));
		BlueprintItemOrders captured = BlueprintItemOrders.capture(tabs, automatic, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview saved = build(bank, CategoryOverrideSource.NONE, options.withItemOrders(captured));
		assertAt(saved, 1393, 0);
		assertAt(saved, 1393, plan.destinationOf("tools"));
		assertEquals(2, saved.getPlannedItemCount());
		for (BankPreviewItem item : saved.getPlannedItems())
		{
			assertEquals("alch", item.getLayoutTagKey());
			assertTrue(item.getPlacementReason(), item.getPlacementReason().contains("Saved bank position."));
			assertTrue(item.getPlacementReason(), item.getPlacementReason().contains("Saved category."));
			assertFalse(item.getPlacementReason().contains("Your"));
		}
	}

	@Test public void aPersonalCorrectionKeepsItsOwnTagAndReasonAtTheCapturedPhysicalPosition()
	{
		BankSnapshot bank = bank(entry(1393, 1, 0));
		BankLayoutOptions options = options(AlchMode.FIXED);
		BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, options);
		int destination = plan.destinationOf("tools");
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(destination,
			Collections.singletonList(1393)), automatic, BlueprintItemOrders.EMPTY);
		BankOrganizationPreview corrected = build(bank, id -> Optional.of("gear"), options.withItemOrders(captured));
		BankPreviewItem item = item(corrected, 1393);
		assertAt(corrected, 1393, destination);
		assertEquals("gear", item.getLayoutTagKey());
		assertTrue(item.getPlacementReason().contains("Saved bank position."));
		assertTrue(item.getPlacementReason().contains("Your category choice."));
		assertFalse(item.getPlacementReason().contains("Saved category."));
	}

	@Test public void aWarmBankAnalysisDistinguishesRetainedAlchFromAnExplicitPlayerOverride()
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(1432, weapon(10));
		stats.put(1434, weapon(20));
		Map<Integer, Integer> values = new LinkedHashMap<>();
		values.put(1432, 10000);
		values.put(1434, 10000);
		AtomicReference<Optional<BankAnalysisRequest>> request = new AtomicReference<>(Optional.empty());
		AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		BankAnalysis analysis = new BankAnalysis(Runnable::run, Runnable::run, request::get, status::set,
			CompositeItemCatalog.DEFAULT, preset);
		BankSnapshot before = bank(entry(1432, 3, 0), entry(1434, 1, 1), entry(995, 0, 2));
		BankSnapshot withdrawn = bank(entry(1432, 3, 0), entry(1434, 0, 1), entry(995, 0, 2));
		assertEquals("gear", item(build(withdrawn, CategoryOverrideSource.NONE, options(AlchMode.FLEXIBLE),
			id -> Optional.ofNullable(stats.get(id)), id -> values.getOrDefault(id, 0)), 1432).getLayoutTagKey());
		BankSnapshot[] banks = {before, withdrawn, withdrawn};
		String[] expected = {"Flexible Alch: all compared stats covered: Dragon mace in bank.",
			"Preserved previous Alch decision.", "Your category choice."};
		for (int index = 0; index < banks.length; index++)
		{
			request.set(Optional.of(new BankAnalysisRequest(banks[index], stats, values, index == 2
				? Collections.singletonMap(1432, "alch") : Collections.emptyMap(), plan, options(AlchMode.FLEXIBLE), preset)));
			analysis.analyzeBank();
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			BankPreviewItem result = item(status.get().organizationPreview().orElseThrow(AssertionError::new), 1432);
			assertEquals("alch", result.getLayoutTagKey());
			assertEquals(expected[index], result.getPlacementReason());
		}
	}

	@Test public void changingOnlyReasonMetadataDoesNotChangeSnapshotPlacementOrBankContents()
	{
		BankSnapshot bank = bank(entry(1127, 25, 0), entry(1393, 0, 1), entry(995, 100, 2));
		String originalPlan = plan.serialize();
		BankOrganizationPreview automatic = build(bank, CategoryOverrideSource.NONE, options(AlchMode.FIXED));
		List<BankCategoryPreview> categories = automatic.getCategories().stream().map(category -> new BankCategoryPreview(
			category.getCategory(), category.getItems().stream().map(item -> item.withPlacementReason("Different explanation"))
				.collect(Collectors.toList()), category.hasManualOrder())).collect(Collectors.toList());
		BankOrganizationPreview relabeled = new BankOrganizationPreview(preset, categories, automatic.getTagCounts(), automatic.getBlockDescriptors());
		assertEquals(BlueprintChanges.capture(automatic), BlueprintChanges.capture(relabeled));
		assertEquals(automatic.toPreviewText(), relabeled.toPreviewText());
		assertEquals(originalPlan, plan.serialize());
		assertContents(bank, relabeled);
	}

	private BankLayoutOptions options(AlchMode mode) { return BankLayoutOptions.defaultFor(preset).withAlchMode(mode); }
	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource overrides, BankLayoutOptions options)
	{
		return build(bank, overrides, options, GearStatsSource.NONE, id -> 39000);
	}
	private BankOrganizationPreview build(BankSnapshot bank, CategoryOverrideSource overrides, BankLayoutOptions options,
		GearStatsSource stats, ItemValueSource values)
	{
		BankOrganizationPreview result = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, stats, values, overrides, plan, options);
		assertContents(bank, result);
		return result;
	}
	private static void assertContents(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getPhysicalItems().stream().collect(Collectors.groupingBy(
			entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(), Collectors.counting())),
			preview.getPlannedItems().stream().collect(Collectors.groupingBy(
				entry -> entry.getItemId() + ":" + entry.getQuantity() + ":" + entry.isPlaceholder(), Collectors.counting())));
	}
	private static BankItemSnapshot entry(int id, int quantity, int slot) { return new BankItemSnapshot(id, quantity, slot, quantity == 0); }
	private static BankSnapshot bank(BankItemSnapshot... items) { return new BankSnapshot(Arrays.asList(items)); }
	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id).findFirst().orElseThrow(AssertionError::new);
	}
	private static void assertAt(BankOrganizationPreview preview, int id, int destination)
	{
		assertTrue(preview.getCategories().get(destination).getItems().stream().anyMatch(item -> item.getItemId() == id));
	}
	/** Synthetic complete vectors isolate the pre-existing replacement and retention policy. */
	private static GearStats weapon(int bonus)
	{
		return new GearStats(GearSlot.WEAPON, bonus, bonus, bonus, 0, 0, bonus, 0, 0, bonus, bonus, bonus, bonus, bonus, 0, 4);
	}
}

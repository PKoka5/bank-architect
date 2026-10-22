package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintItemOrders;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** A real unfinished crafting ingredient changes an automatic disposal decision, including session retention. */
@RunWith(Parameterized.class)
public class ScurriusMaterialProtectionTest
{
	private static final int RUNE_MACE = 1432;
	private static final int DRAGON_MACE = 1434;
	private static final int RUNE_SWORD = 1303;
	private static final int SPINE = 28798;
	private static final int BONE_MACE = 28792;
	private static final int COINS = 995;
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public ScurriusMaterialProtectionTest(BankPreset preset)
	{
		this.preset = preset;
		plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void anOrdinaryDominatedRuneMaceStillGoesToAlchWithoutTheCraftingIngredient()
	{
		for (int quantity : new int[] {1, 3, 25})
		{
			BankSnapshot bank = bank(quantity, -1, -1);
			assertTrue("synthetic full dominance supplies the existing Alch proof", weapon(20).dominates(weapon(10)));
			BankOrganizationPreview preview = fresh(bank, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
			assertTag(preview, RUNE_MACE, "alch");
			assertTag(preview, RUNE_SWORD, "alch");
		}
	}

	@Test
	public void aRealSpineKeepsTheRuneMaceAvailableBeforeTheBoneMaceExists()
	{
		for (int quantity : new int[] {25, 1, 0})
		{
			BankSnapshot bank = bank(quantity, 1, -1);
			BankOrganizationPreview preview = fresh(bank, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
			assertTag(preview, RUNE_MACE, "gear");
			assertAt(preview, RUNE_MACE, plan.destinationOf("gear"));
			assertTag(preview, RUNE_SWORD, "alch");
		}
	}

	@Test
	public void aSpinePlaceholderOrAnExistingBoneMaceDoesNotProtectReplacedRuneStock()
	{
		assertTag(fresh(bank(3, 0, -1), Collections.emptyMap(), BlueprintItemOrders.EMPTY), RUNE_MACE, "alch");
		for (int boneQuantity : new int[] {1, 0})
		{
			BankSnapshot bank = bank(3, 1, boneQuantity);
			BankOrganizationPreview preview = fresh(bank, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
			assertTag(preview, RUNE_MACE, "alch");
			assertEquals(boneQuantity == 0, item(preview, BONE_MACE).isPlaceholder());
		}
	}

	@Test
	public void depositingASpineOverridesPreviousAutomaticAlchWhileUnrelatedRetentionRemains()
	{
		Session session = new Session();
		BankSnapshot before = bank(3, -1, -1);
		BankOrganizationPreview previous = session.analyze(before, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
		assertTag(previous, RUNE_MACE, "alch");
		assertTag(previous, RUNE_SWORD, "alch");
		BankAnalysisRequest priorRequest = session.request.get().orElseThrow(AssertionError::new);
		List<BankItemSnapshot> depositedEntries = new ArrayList<>(bank(3, 1, -1).getItems());
		for (int index = 0; index < depositedEntries.size(); index++)
			if (depositedEntries.get(index).getItemId() == DRAGON_MACE)
				depositedEntries.set(index, new BankItemSnapshot(DRAGON_MACE, 0, index, true));
		BankSnapshot deposited = new BankSnapshot(depositedEntries);
		BankAnalysisRequest current = request(deposited, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
		assertTrue("adding the ingredient preserves the old context and exercises retained categories",
			current.sameLayoutContext(priorRequest, preset));
		assertTrue("an unrelated bank placeholder keeps the automatic retention path active",
			deposited.getItems().stream().anyMatch(entry -> entry.getItemId() == COINS && entry.isPlaceholder()));
		assertEquals("the mace quantity did not invalidate its old decision", before.getTotalQuantity(RUNE_MACE),
			deposited.getTotalQuantity(RUNE_MACE));
		assertTag(fresh(deposited, Collections.emptyMap(), BlueprintItemOrders.EMPTY), RUNE_SWORD, "gear");
		for (int reopen = 0; reopen < 3; reopen++)
		{
			BankOrganizationPreview preview = session.analyze(deposited, Collections.emptyMap(), BlueprintItemOrders.EMPTY);
			assertTag(preview, RUNE_MACE, "gear");
			assertTag(preview, RUNE_SWORD, "alch");
		}
	}

	@Test
	public void anExplicitAlchChoiceStillWinsWhenTheSpineIsDeposited()
	{
		Map<Integer, String> choice = Collections.singletonMap(RUNE_MACE, "alch");
		Session session = new Session();
		assertTag(session.analyze(bank(3, -1, -1), choice, BlueprintItemOrders.EMPTY), RUNE_MACE, "alch");
		BankSnapshot protectedBank = bank(3, 1, -1);
		assertTag(fresh(protectedBank, choice, BlueprintItemOrders.EMPTY), RUNE_MACE, "alch");
		assertTag(session.analyze(protectedBank, choice, BlueprintItemOrders.EMPTY), RUNE_MACE, "alch");
	}

	@Test
	public void savedAndCapturedPhysicalDestinationsStillWinOverAutomaticProtection()
	{
		BankSnapshot bank = bank(3, 1, -1);
		BlueprintItemOrders saved = BlueprintItemOrders.EMPTY.withDestinations(Collections.singletonMap(
			RUNE_MACE + "#0", new BlueprintItemOrders.Destination(plan.destinationOf("alch"), "gear", "alch")));
		BankOrganizationPreview manuallyTagged = fresh(bank, Collections.emptyMap(), saved);
		assertTag(manuallyTagged, RUNE_MACE, "alch");
		assertAt(manuallyTagged, RUNE_MACE, plan.destinationOf("alch"));

		List<Integer> physicalOrder = Arrays.asList(DRAGON_MACE, RUNE_MACE, SPINE, RUNE_SWORD, COINS);
		int chosenTab = 8;
		BlueprintItemOrders captured = BlueprintItemOrders.capture(Collections.singletonMap(chosenTab, physicalOrder),
			fresh(bank, Collections.emptyMap(), BlueprintItemOrders.EMPTY), BlueprintItemOrders.EMPTY);
		assertTrue(captured.isCaptured());
		Session session = new Session();
		for (BlueprintItemOrders orders : Arrays.asList(saved, captured))
		{
			BankOrganizationPreview preview = session.analyze(bank, Collections.emptyMap(), orders);
			assertAt(preview, RUNE_MACE, orders == captured ? chosenTab : plan.destinationOf("alch"));
			if (orders == captured)
				assertEquals(physicalOrder, preview.getCategories().get(chosenTab).getItems().stream()
					.map(BankPreviewItem::getItemId).collect(Collectors.toList()));
		}
	}

	@Test
	public void creatingTheBoneMaceReleasesTheIngredientProtectionIncludingAWithdrawnProduct()
	{
		Session session = new Session();
		assertTag(session.analyze(bank(3, 1, -1), Collections.emptyMap(), BlueprintItemOrders.EMPTY), RUNE_MACE, "gear");
		for (int productQuantity : new int[] {1, 0, 1})
			assertTag(session.analyze(bank(3, 1, productQuantity), Collections.emptyMap(), BlueprintItemOrders.EMPTY),
				RUNE_MACE, "alch");
	}

	private BankOrganizationPreview fresh(BankSnapshot bank, Map<Integer, String> choices, BlueprintItemOrders orders)
	{
		BankAnalysisRequest request = request(bank, choices, orders);
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT, preset,
			request::gearStats, request::alchValue, request::categoryKey, plan, request.layoutOptions());
		assertContents(bank, preview);
		return preview;
	}

	private BankAnalysisRequest request(BankSnapshot bank, Map<Integer, String> choices, BlueprintItemOrders orders)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(RUNE_MACE, weapon(10));
		stats.put(RUNE_SWORD, weapon(10));
		stats.put(DRAGON_MACE, weapon(20));
		Map<Integer, Integer> values = new LinkedHashMap<>();
		values.put(RUNE_MACE, 10000);
		values.put(RUNE_SWORD, 10000);
		values.put(DRAGON_MACE, 10000);
		return new BankAnalysisRequest(bank, stats, values, choices, plan,
			BankLayoutOptions.defaultFor(preset).withItemOrders(orders), preset);
	}

	private static BankSnapshot bank(int maceQuantity, int spineQuantity, int boneQuantity)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		add(entries, RUNE_MACE, maceQuantity);
		add(entries, DRAGON_MACE, 1);
		add(entries, RUNE_SWORD, 3);
		add(entries, COINS, 0);
		if (spineQuantity >= 0) add(entries, SPINE, spineQuantity);
		if (boneQuantity >= 0) add(entries, BONE_MACE, boneQuantity);
		return new BankSnapshot(entries);
	}

	private static void add(List<BankItemSnapshot> entries, int id, int quantity)
	{
		entries.add(new BankItemSnapshot(id, quantity, entries.size(), quantity == 0));
	}

	/** Deliberately synthetic complete vectors prove dominance without depending on live equipment balance. */
	private static GearStats weapon(int bonus)
	{
		return new GearStats(GearSlot.WEAPON, bonus, bonus, bonus, 0, 0, bonus, 0, 0,
			bonus, bonus, bonus, bonus, bonus, 0, 4);
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

	private static void assertAt(BankOrganizationPreview preview, int id, int tab)
	{
		assertTrue("Item " + id + " in tab " + tab, preview.getCategories().get(tab).getItems().stream()
			.anyMatch(item -> item.getItemId() == id));
	}

	private static void assertContents(BankSnapshot bank, BankOrganizationPreview preview)
	{
		assertEquals(bank.getPhysicalItems().size(), preview.getPlannedItemCount());
		assertEquals(bank.getItems().stream().map(BankItemSnapshot::getItemId).collect(Collectors.toSet()),
			new HashSet<>(preview.getPlannedItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList())));
		for (BankItemSnapshot entry : bank.getItems())
		{
			BankPreviewItem planned = item(preview, entry.getItemId());
			assertEquals(entry.getQuantity(), planned.getQuantity());
			assertEquals(entry.isPlaceholder(), planned.isPlaceholder());
		}
	}

	private final class Session
	{
		private final AtomicReference<Optional<BankAnalysisRequest>> request = new AtomicReference<>(Optional.empty());
		private final AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		private final BankAnalysis analysis = new BankAnalysis(Runnable::run, Runnable::run, request::get, status::set,
			CompositeItemCatalog.DEFAULT, preset);

		private BankOrganizationPreview analyze(BankSnapshot bank, Map<Integer, String> choices, BlueprintItemOrders orders)
		{
			request.set(Optional.of(ScurriusMaterialProtectionTest.this.request(bank, choices, orders)));
			analysis.analyzeBank();
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			BankOrganizationPreview preview = status.get().organizationPreview().orElseThrow(AssertionError::new);
			assertContents(bank, preview);
			return preview;
		}
	}
}

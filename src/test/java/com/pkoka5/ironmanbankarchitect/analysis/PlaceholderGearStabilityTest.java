package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.RearrangeMode;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Curated armour placeholders guide fresh layouts; other replacements need established session evidence. */
@RunWith(Parameterized.class)
public class PlaceholderGearStabilityTest
{
	private static final int RUNE_BODY = 1127;
	private static final int RUNE_LEGS = 1079;
	private static final int BANDOS_BODY = 11832;
	private static final int BANDOS_LEGS = 11834;
	private static final int RUNE_CHAIN = 1113;
	private static final int ANCHOR = 10887;
	private static final int UNTIERED_BODY = 900002;
	private static final int UNTIERED_LEGS = 900003;
	private final BankPreset preset;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public PlaceholderGearStabilityTest(BankPreset preset)
	{
		this.preset = preset;
	}

	@Test
	public void withdrawingAndReturningPreferredSetKeepsEstablishedDestinationsAndOrder()
	{
		Session session = new Session(preset);
		BankOrganizationPreview owned = session.analyze(bank(false));
		assertTag(owned, RUNE_BODY, "alch");
		assertTag(owned, RUNE_LEGS, "alch");
		Map<Integer, List<Integer>> established = tabIds(owned);

		for (int reopen = 0; reopen < 3; reopen++)
		{
			BankOrganizationPreview withdrawn = session.analyze(bank(true));
			assertEquals(established, tabIds(withdrawn));
			assertTag(withdrawn, RUNE_BODY, "alch");
			assertTag(withdrawn, RUNE_LEGS, "alch");
			for (int id : new int[] {BANDOS_BODY, BANDOS_LEGS})
			{
				assertTag(withdrawn, id, "gear");
				assertTrue(item(withdrawn, id).isPlaceholder());
				assertEquals(0, item(withdrawn, id).getQuantity());
			}
		}
		BankOrganizationPreview returned = session.analyze(bank(false));
		assertEquals(established, tabIds(returned));
		assertFalse(item(returned, BANDOS_BODY).isPlaceholder());
		assertEquals(1, item(returned, BANDOS_BODY).getQuantity());
	}

	@Test
	public void withdrawingMeleeSetLeavesCompletedFullBisRowsCompleteInBothRearrangeModes()
	{
		Session session = new Session(preset);
		int[][] rows = {{BANDOS_BODY, 11828, 21021, 9674}, {BANDOS_LEGS, 11830, 21024, 9676}};
		int[] rings = {6737, 6735, 6733, 6731, 22975, 19550, 12601, 12605};
		List<BankItemSnapshot> entries = new ArrayList<>(bank(false).getPhysicalItems());
		for (int row = 0; row < rows.length; row++)
		{
			for (int style = 0; style < rows[row].length; style++)
			{
				int id = rows[row][style];
				session.customStats.put(id, new GearStats(row == 0 ? GearSlot.BODY : GearSlot.LEGS,
					0, 0, 0, style == 2 ? 30 : 0, style == 1 ? 30 : 0, style == 0 ? 4 : 0,
					0, style == 3 ? 8 : 0, 200, 200, 200, 200, 200, 0, 0));
				if (style > 0) entries.add(new BankItemSnapshot(id, 1, entries.size()));
			}
		}
		for (int id : rings)
		{
			entries.add(new BankItemSnapshot(id, 1, entries.size()));
			session.customStats.put(id, new GearStats(GearSlot.RING, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		}
		BankOrganizationPreview owned = session.analyze(new BankSnapshot(entries));
		List<BankPreviewItem> combat = owned.getCategories()
			.get(BankLayoutPlan.defaultFor(preset).destinationOf("gear")).getItems();
		assertEquals(16, combat.size());
		for (int row = 0; row < rows.length; row++)
			for (int column = 0; column < rows[row].length; column++)
				assertEquals("BIS row " + row + ", column " + column,
					rows[row][column], combat.get(row * 8 + column).getItemId());
		assertEquals(Integer.valueOf(16), owned.getTagCounts().get("gear"));
		BankTabPlan ownedPlan = BankTabPlan.fromPreview(owned);
		int[] physicalOrder = ownedPlan.getFlattenedItems().stream().mapToInt(BankPreviewItem::getItemId).toArray();
		int[] tabCounts = new int[TabRouteAdvisor.MAX_TABS];
		for (BankTabPlan.TargetTab tab : ownedPlan.getNumberedTabs())
			tabCounts[tab.getBankTabNumber() - 1] = tab.getItems().size();
		assertComplete(owned, physicalOrder, tabCounts);
		List<BankItemSnapshot> withdrawnEntries = new ArrayList<>();
		for (BankItemSnapshot entry : entries)
		{
			boolean withdrawn = entry.getItemId() == BANDOS_BODY || entry.getItemId() == BANDOS_LEGS;
			withdrawnEntries.add(new BankItemSnapshot(entry.getItemId(), withdrawn ? 0 : entry.getQuantity(),
				entry.getSlotIndex(), withdrawn));
		}
		BankOrganizationPreview withdrawn = session.analyze(new BankSnapshot(withdrawnEntries));
		assertEquals(tabIds(owned), tabIds(withdrawn));
		assertEquals(Integer.valueOf(14), withdrawn.getTagCounts().get("gear"));
		assertComplete(withdrawn, physicalOrder, tabCounts);
		BankOrganizationPreview returned = session.analyze(new BankSnapshot(entries));
		assertEquals(Integer.valueOf(16), returned.getTagCounts().get("gear"));
		assertComplete(returned, physicalOrder, tabCounts);
	}

	@Test
	public void freshCuratedArmourPlaceholdersKeepReviewedStockInAlch()
	{
		BankOrganizationPreview preview = new Session(preset).analyze(bank(true));
		assertTag(preview, RUNE_BODY, "alch");
		assertTag(preview, RUNE_LEGS, "alch");
		assertTrue(item(preview, BANDOS_BODY).isPlaceholder());
	}

	@Test
	public void freshUntieredPlaceholdersCannotEstablishReplacementDecisions()
	{
		BankOrganizationPreview preview = new Session(preset).analyze(memoryBank(true));
		assertTag(preview, RUNE_BODY, "gear");
		assertTag(preview, RUNE_LEGS, "gear");
	}

	@Test
	public void releasingPreferredPlaceholdersRestoresNormalGearClassification()
	{
		Session session = new Session(preset);
		session.analyze(bank(false));
		session.analyze(bank(true));
		BankOrganizationPreview released = session.analyze(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(RUNE_BODY, 1, 0), new BankItemSnapshot(RUNE_LEGS, 1, 1))));
		assertTag(released, RUNE_BODY, "gear");
		assertTag(released, RUNE_LEGS, "gear");
		assertEquals(2, released.getPlannedItemCount());
	}

	@Test
	public void addingAnItemPreservesEstablishedDecisionsWithoutInferringNewOwnershipFromPlaceholders()
	{
		Session session = new Session(preset);
		establishMemory(session);
		session.analyze(memoryBank(true));
		List<BankItemSnapshot> entries = new ArrayList<>(memoryBank(true).getPhysicalItems());
		entries.add(new BankItemSnapshot(RUNE_CHAIN, 1, entries.size()));
		BankOrganizationPreview changed = session.analyze(new BankSnapshot(entries));
		assertTag(changed, RUNE_CHAIN, "gear");
		// The new chain gets no remembered decision from an untiered placeholder.
		assertTag(changed, RUNE_BODY, "alch");
		assertTag(changed, RUNE_LEGS, "alch");
	}

	@Test
	public void depositingAnchorKeepsReportedRuneHelmsAndSkirtInAlchAcrossFurtherAdditions()
	{
		int[] stock = {1163, 1147, 1093};
		int helmUpgrade = 26382;
		Session session = new Session(preset);
		for (int id : stock) session.customStats.put(id, defensiveStats(id == 1093 ? GearSlot.LEGS : GearSlot.HEAD, 100));
		session.customStats.put(helmUpgrade, defensiveStats(GearSlot.HEAD, 200));
		session.customStats.put(ANCHOR, defensiveStats(GearSlot.WEAPON, 0));
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int id : stock) entries.add(new BankItemSnapshot(id, 1, entries.size()));
		entries.add(new BankItemSnapshot(helmUpgrade, 1, entries.size()));
		entries.add(new BankItemSnapshot(BANDOS_LEGS, 1, entries.size()));
		BankOrganizationPreview owned = session.analyze(new BankSnapshot(entries));
		for (int id : stock) assertTag(owned, id, "alch");
		entries.set(3, new BankItemSnapshot(helmUpgrade, 0, 3, true));
		entries.set(4, new BankItemSnapshot(BANDOS_LEGS, 0, 4, true));
		BankOrganizationPreview withdrawn = session.analyze(new BankSnapshot(entries));
		for (int id : stock) assertTag(withdrawn, id, "alch");
		entries.add(new BankItemSnapshot(ANCHOR, 1, entries.size()));
		BankOrganizationPreview deposited = session.analyze(new BankSnapshot(entries));
		for (int id : stock) assertTag(deposited, id, "alch");
		assertTag(deposited, ANCHOR, "gear");
		assertEquals(entries.size(), deposited.getPlannedItemCount());
		entries.add(new BankItemSnapshot(995, 1000, entries.size()));
		for (int reopen = 0; reopen < 3; reopen++)
		{
			BankOrganizationPreview reopened = session.analyze(new BankSnapshot(entries));
			for (int id : stock) assertTag(reopened, id, "alch");
			assertEquals(entries.size(), reopened.getPlannedItemCount());
		}
		// Exact curated armour placeholders also work without previous session history.
		Session freshSession = new Session(preset);
		freshSession.customStats.putAll(session.customStats);
		BankOrganizationPreview fresh = freshSession.analyze(new BankSnapshot(entries));
		for (int id : stock) assertTag(fresh, id, "alch");
	}

	private static GearStats defensiveStats(GearSlot slot, int defence)
	{
		return new GearStats(slot, 0, 0, 0, 0, 0, 0, 0, 0, defence, defence, defence, defence, defence, 0, 0);
	}

	@Test
	public void presetSwitchAndPersonalChoicesResetPriorAutomaticDecisions()
	{
		Session session = new Session(preset);
		establishMemory(session);
		BankPreset other = preset == BankPresets.MAIN ? BankPresets.IRONMAN : BankPresets.MAIN;
		BankOrganizationPreview switched = session.analyze(memoryBank(true), other,
			BankLayoutPlan.defaultFor(other), BankLayoutOptions.defaultFor(other), Collections.emptyMap(), 0);
		assertTag(switched, RUNE_BODY, "gear");
		assertTag(switched, RUNE_LEGS, "gear");

		session = new Session(preset);
		establishMemory(session);
		BankOrganizationPreview personal = session.analyze(memoryBank(true), preset,
			BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset),
			Collections.singletonMap(RUNE_BODY, "gear"), 0);
		assertTag(personal, RUNE_BODY, "gear");
		assertTag(personal, RUNE_LEGS, "gear");
	}

	@Test
	public void changedGearStatsOrAlchLayoutOptionReevaluatesRatherThanKeepingStaleProof()
	{
		Session session = new Session(preset);
		establishMemory(session);
		BankOrganizationPreview newStats = session.analyze(memoryBank(true), preset,
			BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 1);
		assertTag(newStats, RUNE_BODY, "gear");
		assertTag(newStats, RUNE_LEGS, "gear");

		session = new Session(preset);
		session.analyze(bank(false));
		BankOrganizationPreview disabled = session.analyze(bank(true), preset,
			BankLayoutPlan.defaultFor(preset), new BankLayoutOptions(true, true, false), Collections.emptyMap(), 0);
		assertTag(disabled, RUNE_BODY, "gear");
		assertTag(disabled, RUNE_LEGS, "gear");
	}

	@Test
	public void changedOldAlchValuesReevaluateEvenWhileDepositingAnchor()
	{
		Session session = new Session(preset);
		establishMemory(session);
		List<BankItemSnapshot> entries = new ArrayList<>(memoryBank(true).getPhysicalItems());
		entries.add(new BankItemSnapshot(ANCHOR, 1, entries.size()));
		session.highAlchValue = 1;
		BankOrganizationPreview changed = session.analyze(new BankSnapshot(entries));
		assertTag(changed, RUNE_BODY, "gear");
		assertTag(changed, RUNE_LEGS, "gear");
	}

	@Test
	public void invalidatingAnalysisClearsPriorAutomaticDecisions()
	{
		Session session = new Session(preset);
		establishMemory(session);
		session.analysis.invalidate();
		BankOrganizationPreview preview = session.analyze(memoryBank(true));
		assertTag(preview, RUNE_BODY, "gear");
		assertTag(preview, RUNE_LEGS, "gear");
	}

	@Test
	public void successThatAlreadyStartedBeforeInvalidationCannotPublishOrRestoreItsMemory()
	{
		AtomicReference<Runnable> duringAnalysis = new AtomicReference<>();
		ItemCatalog interruptibleCatalog = new ItemCatalog()
		{
			@Override
			public Optional<CatalogItem> findById(int id)
			{
				return CompositeItemCatalog.DEFAULT.findById(id);
			}

			@Override
			public void requireAvailable()
			{
				Runnable action = duringAnalysis.getAndSet(null);
				if (action != null) action.run();
				CompositeItemCatalog.DEFAULT.requireAvailable();
			}
		};
		Session session = new Session(preset, Runnable::run, interruptibleCatalog);
		establishMemory(session);
		duringAnalysis.set(session.analysis::invalidate);
		session.submit(memoryBank(false), preset, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 0);
		assertEquals(BankAnalysisStatus.Kind.NOT_STARTED, session.status.get().kind());
		assertTrue(duringAnalysis.get() == null);
		BankOrganizationPreview fresh = session.analyze(memoryBank(true));
		assertTag(fresh, RUNE_BODY, "gear");
		assertTag(fresh, RUNE_LEGS, "gear");
	}

	@Test
	public void reducingBulkStockToOneCopyDoesNotRetainItsOldAlchDecision()
	{
		int stock = 900001;
		ItemCatalog plainGear = id -> Optional.of(new CatalogItem(id, "Stock " + id,
			ItemCategory.GEAR, "body", Collections.emptySet(), null));
		Session session = new Session(preset, Runnable::run, plainGear);
		session.highAlchValue = 2000;
		BankSnapshot bulk = new BankSnapshot(Arrays.asList(new BankItemSnapshot(stock, 8, 0),
			new BankItemSnapshot(BANDOS_BODY, 1, 1), new BankItemSnapshot(BANDOS_LEGS, 0, 2, true)));
		assertTag(session.analyze(bulk), stock, "alch");
		BankSnapshot one = new BankSnapshot(Arrays.asList(new BankItemSnapshot(stock, 1, 0),
			new BankItemSnapshot(BANDOS_BODY, 1, 1), new BankItemSnapshot(BANDOS_LEGS, 0, 2, true),
			new BankItemSnapshot(ANCHOR, 1, 3)));
		BankOrganizationPreview reduced = session.analyze(one);
		assertTag(reduced, stock, "gear");
		assertEquals(1, item(reduced, stock).getQuantity());
	}

	@Test
	public void changedPhysicalOccurrencesResetDecisionsEvenWhenTotalQuantityIsUnchanged()
	{
		Session session = new Session(preset);
		List<BankItemSnapshot> before = new ArrayList<>(memoryBank(false).getPhysicalItems());
		before.set(0, new BankItemSnapshot(RUNE_BODY, 2, 0));
		assertTag(session.analyze(new BankSnapshot(before)), RUNE_BODY, "alch");
		// Synthetic duplicate slots exercise the snapshot boundary without changing the aggregate quantity.
		List<BankItemSnapshot> split = new ArrayList<>(memoryBank(true).getPhysicalItems());
		split.add(new BankItemSnapshot(RUNE_BODY, 1, split.size()));
		BankOrganizationPreview changed = session.analyze(new BankSnapshot(split));
		assertTag(changed, RUNE_BODY, "gear");
		assertTag(changed, RUNE_LEGS, "gear");
		assertEquals(2, changed.getPlannedItems().stream().filter(item -> item.getItemId() == RUNE_BODY).count());
	}

	@Test
	public void staleWithdrawalRequestCannotRestoreDecisionsClearedByNewerPersonalChoices()
	{
		Deque<Runnable> pending = new ArrayDeque<>();
		Session session = new Session(preset, pending::addLast);
		session.submit(memoryBank(false), preset, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 0);
		pending.removeFirst().run();
		assertTag(session.preview(), RUNE_BODY, "alch");
		session.submit(memoryBank(true), preset, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 0);
		session.submit(memoryBank(true), preset, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), Collections.singletonMap(RUNE_BODY, "gear"), 0);
		pending.removeLast().run();
		BankAnalysisStatus newer = session.status.get();
		assertTag(session.preview(), RUNE_BODY, "gear");
		pending.removeFirst().run();
		assertTrue(newer == session.status.get());
		session.submit(memoryBank(true), preset, BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 0);
		pending.removeFirst().run();
		assertTag(session.preview(), RUNE_BODY, "gear");
		assertTag(session.preview(), RUNE_LEGS, "gear");
	}

	private static BankSnapshot bank(boolean withdrawn)
	{
		return new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, 1, 0),
			new BankItemSnapshot(RUNE_LEGS, 1, 1),
			new BankItemSnapshot(BANDOS_BODY, withdrawn ? 0 : 1, 2, withdrawn),
			new BankItemSnapshot(BANDOS_LEGS, withdrawn ? 0 : 1, 3, withdrawn)));
	}

	/** No exact tiers: only real stat dominance or established session history can route these replacements. */
	private static BankSnapshot memoryBank(boolean withdrawn)
	{
		return new BankSnapshot(Arrays.asList(new BankItemSnapshot(RUNE_BODY, 1, 0),
			new BankItemSnapshot(RUNE_LEGS, 1, 1),
			new BankItemSnapshot(UNTIERED_BODY, withdrawn ? 0 : 1, 2, withdrawn),
			new BankItemSnapshot(UNTIERED_LEGS, withdrawn ? 0 : 1, 3, withdrawn)));
	}

	private static void establishMemory(Session session)
	{
		BankOrganizationPreview owned = session.analyze(memoryBank(false));
		assertTag(owned, RUNE_BODY, "alch");
		assertTag(owned, RUNE_LEGS, "alch");
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		assertEquals("item " + id, tag, item(preview, id).getLayoutTagKey());
	}

	private static void assertComplete(BankOrganizationPreview preview, int[] physicalOrder, int[] tabCounts)
	{
		BankTabPlan plan = BankTabPlan.fromPreview(preview);
		for (RearrangeMode mode : RearrangeMode.values())
			for (int focusTab = 0; focusTab <= plan.getNumberedTabs().size(); focusTab++)
				assertEquals(mode + ", focus tab " + focusTab, TabRouteAdvisor.Status.COMPLETE,
					TabRouteAdvisor.assess(physicalOrder, plan, tabCounts, focusTab, mode).getStatus());
	}

	private static Map<Integer, List<Integer>> tabIds(BankOrganizationPreview preview)
	{
		Map<Integer, List<Integer>> result = new LinkedHashMap<>();
		for (int tab = 0; tab < preview.getCategories().size(); tab++)
		{
			List<Integer> ids = new ArrayList<>();
			for (BankPreviewItem item : preview.getCategories().get(tab).getItems()) ids.add(item.getItemId());
			result.put(tab, ids);
		}
		return result;
	}

	private static final class Session
	{
		private final BankPreset preset;
		private final AtomicReference<Optional<BankAnalysisRequest>> request = new AtomicReference<>();
		private final AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		private final BankAnalysis analysis;
		private final Map<Integer, GearStats> customStats = new HashMap<>();
		private int highAlchValue = 10000;

		private Session(BankPreset preset)
		{
			this(preset, Runnable::run);
		}

		private Session(BankPreset preset, Executor background)
		{
			this(preset, background, CompositeItemCatalog.DEFAULT);
		}

		private Session(BankPreset preset, Executor background, ItemCatalog catalog)
		{
			this.preset = preset;
			analysis = new BankAnalysis(Runnable::run, background, request::get, status::set,
				catalog, preset);
		}

		private BankOrganizationPreview analyze(BankSnapshot bank)
		{
			return analyze(bank, preset, BankLayoutPlan.defaultFor(preset),
				BankLayoutOptions.defaultFor(preset), Collections.emptyMap(), 0);
		}

		private BankOrganizationPreview analyze(BankSnapshot bank, BankPreset selectedPreset,
			BankLayoutPlan plan, BankLayoutOptions options, Map<Integer, String> choices, int statDelta)
		{
			submit(bank, selectedPreset, plan, options, choices, statDelta);
			return preview();
		}

		private BankOrganizationPreview preview()
		{
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			return status.get().organizationPreview().orElseThrow(AssertionError::new);
		}

		private void submit(BankSnapshot bank, BankPreset selectedPreset,
			BankLayoutPlan plan, BankLayoutOptions options, Map<Integer, String> choices, int statDelta)
		{
			Map<Integer, GearStats> stats = new LinkedHashMap<>();
			Map<Integer, Integer> values = new LinkedHashMap<>();
			for (BankItemSnapshot entry : bank.getItems())
			{
				int id = entry.getItemId();
				GearSlot slot = id == RUNE_LEGS || id == BANDOS_LEGS || id == UNTIERED_LEGS ? GearSlot.LEGS : GearSlot.BODY;
				int defence = id == BANDOS_BODY || id == BANDOS_LEGS || id == UNTIERED_BODY || id == UNTIERED_LEGS
					? 200 : id == RUNE_CHAIN ? 120 : 100;
				stats.put(id, customStats.getOrDefault(id, new GearStats(slot, 0, 0, 0, 0, 0, 0, 0, 0,
					defence + statDelta, defence, defence, defence, defence, 0, 0)));
				values.put(id, highAlchValue);
			}
			request.set(Optional.of(new BankAnalysisRequest(bank, stats, values, choices, plan, options, selectedPreset)));
			analysis.analyzeBank();
		}
	}
}

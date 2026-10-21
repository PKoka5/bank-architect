package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.assertEquals;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Issue #48: Alch routing asks whether the item is alch stock and whether the bank replaces it,
 * never how many copies remain or whether they are placeholders. */
@RunWith(Parameterized.class)
public class PlaceholderAlchRoutingTest
{
	private static final int RUNE_LONG = 1303;
	private static final int RUNE_2H = 1319;
	private static final int RUNE_SCIM = 1333;
	private static final int WHIP = 4151;
	private static final int STAFF_OF_WATER = 1381;
	private static final int WATER_BATTLESTAFF = 1397;
	private static final int RUNE_PICKAXE = 1275;
	private static final int DRAGON_PICKAXE = 11920;
	private static final int RUNE_BODY = 1127;
	private static final int UNTIERED_BODY = 900002;
	private static final int MITHRIL_BODY = 1121;
	private static final int BLACK_DHIDE_BODY = 2503;
	private static final int STOCK = 900001;
	private static final int BETTER_STOCK = 900004;
	private static final int BEST_STOCK = 900005;
	private static final ItemCatalog PLAIN_GEAR = id -> Optional.of(new CatalogItem(id, "Stock " + id,
		ItemCategory.GEAR, "body", Collections.emptySet(), null));
	private final BankPreset preset;
	private final Map<Integer, GearStats> stats = new HashMap<>();
	private final Map<Integer, Integer> alchValues = new HashMap<>();
	private final Map<Integer, Integer> gePrices = new HashMap<>();

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public PlaceholderAlchRoutingTest(BankPreset preset)
	{
		this.preset = preset;
	}

	@Test
	public void withdrawnAlchStockStaysInAlchWithoutSessionHistory()
	{
		BankOrganizationPreview preview = analyze(CompositeItemCatalog.DEFAULT,
			placeholder(RUNE_LONG), placeholder(RUNE_2H), placeholder(RUNE_SCIM), owned(WHIP, 1));
		assertTag(preview, RUNE_LONG, "alch");
		assertTag(preview, RUNE_2H, "alch");
		assertTag(preview, RUNE_SCIM, "alch");
	}

	@Test
	public void withdrawnReplacementStillReplacesAlchStock()
	{
		assertTag(analyze(CompositeItemCatalog.DEFAULT, owned(RUNE_LONG, 45), placeholder(WHIP)), RUNE_LONG, "alch");
		assertTag(analyze(CompositeItemCatalog.DEFAULT, placeholder(RUNE_LONG), placeholder(WHIP)), RUNE_LONG, "alch");
	}

	@Test
	public void stockWithoutReplacementStaysGearAsBestInSlotAtAnyQuantity()
	{
		for (BankItemSnapshot scimitar : Arrays.asList(owned(RUNE_SCIM, 1), owned(RUNE_SCIM, 3), placeholder(RUNE_SCIM)))
			assertTag(analyze(CompositeItemCatalog.DEFAULT, scimitar), RUNE_SCIM, "gear");
	}

	@Test
	public void releasingTheReplacementPlaceholderReturnsStockToGear()
	{
		assertTag(analyze(CompositeItemCatalog.DEFAULT, placeholder(RUNE_LONG)), RUNE_LONG, "gear");
	}

	@Test
	public void roleAndToolUpgradePlaceholdersReplaceStock()
	{
		BankOrganizationPreview preview = analyze(CompositeItemCatalog.DEFAULT,
			owned(STAFF_OF_WATER, 1), placeholder(WATER_BATTLESTAFF),
			placeholder(RUNE_PICKAXE), placeholder(DRAGON_PICKAXE));
		assertTag(preview, STAFF_OF_WATER, "alch");
		assertTag(preview, RUNE_PICKAXE, "alch");
	}

	@Test
	public void dominatingPlaceholderReplacesReviewedArmour()
	{
		stats.put(RUNE_BODY, defensive(100));
		stats.put(UNTIERED_BODY, defensive(200));
		assertTag(analyze(CompositeItemCatalog.DEFAULT, owned(RUNE_BODY, 1), placeholder(UNTIERED_BODY)),
			RUNE_BODY, "alch");
		assertTag(analyze(CompositeItemCatalog.DEFAULT, placeholder(RUNE_BODY), placeholder(UNTIERED_BODY)),
			RUNE_BODY, "alch");
	}

	@Test
	public void marketPricedStockIsAlchedBehindABestAndBackupAtAnyQuantity()
	{
		stats.put(MITHRIL_BODY, defensive(50));
		stats.put(RUNE_BODY, defensive(80));
		stats.put(UNTIERED_BODY, defensive(200));
		price(MITHRIL_BODY, 3120, 2500);
		for (BankItemSnapshot mithril : Arrays.asList(owned(MITHRIL_BODY, 1), owned(MITHRIL_BODY, 450),
			placeholder(MITHRIL_BODY)))
		{
			assertTag(analyze(CompositeItemCatalog.DEFAULT, mithril, owned(RUNE_BODY, 1), placeholder(UNTIERED_BODY)),
				MITHRIL_BODY, "alch");
			assertTag(analyze(CompositeItemCatalog.DEFAULT, mithril, placeholder(RUNE_BODY), owned(UNTIERED_BODY, 1)),
				MITHRIL_BODY, "alch");
			// Mithril is the backup to a lone Rune body, and a newer player's best without one.
			assertTag(analyze(CompositeItemCatalog.DEFAULT, mithril, owned(RUNE_BODY, 1)), MITHRIL_BODY, "gear");
			assertTag(analyze(CompositeItemCatalog.DEFAULT, mithril), MITHRIL_BODY, "gear");
		}
	}

	@Test
	public void gearTheMarketValuesAboveItsAlchValueStaysEvenWhenReplaced()
	{
		stats.put(STOCK, defensive(100));
		stats.put(BETTER_STOCK, defensive(150));
		stats.put(BEST_STOCK, defensive(200));
		for (int gePrice : new int[] {12501, 5_000_000, 0})
		{
			price(STOCK, 10000, gePrice);
			assertTag(analyze(PLAIN_GEAR, owned(STOCK, 1), owned(BETTER_STOCK, 1), owned(BEST_STOCK, 1)), STOCK, "gear");
		}
		price(STOCK, 10000, 12500);
		assertTag(analyze(PLAIN_GEAR, owned(STOCK, 1), owned(BETTER_STOCK, 1), owned(BEST_STOCK, 1)), STOCK, "alch");
	}

	@Test
	public void unreviewedGearKeepsABestAndBackupCountingWithdrawnItems()
	{
		stats.put(STOCK, defensive(100));
		stats.put(BETTER_STOCK, defensive(150));
		stats.put(BEST_STOCK, defensive(200));
		price(STOCK, 10000, 10000);
		assertTag(analyze(PLAIN_GEAR, owned(STOCK, 1), owned(BEST_STOCK, 1)), STOCK, "gear");
		assertTag(analyze(PLAIN_GEAR, owned(STOCK, 1), placeholder(BEST_STOCK)), STOCK, "gear");
		assertTag(analyze(PLAIN_GEAR, owned(STOCK, 1), placeholder(BETTER_STOCK), placeholder(BEST_STOCK)),
			STOCK, "alch");
	}

	@Test
	public void reviewedStockIgnoresTheMarketAndTheAlchFloor()
	{
		price(BLACK_DHIDE_BODY, 4680, 8000);
		stats.put(BLACK_DHIDE_BODY, ranged(50));
		stats.put(STOCK, ranged(100));
		assertTag(analyze(CompositeItemCatalog.DEFAULT, owned(BLACK_DHIDE_BODY, 1), placeholder(STOCK)),
			BLACK_DHIDE_BODY, "alch");
		price(BLACK_DHIDE_BODY, 120, 400);
		assertTag(analyze(CompositeItemCatalog.DEFAULT, owned(BLACK_DHIDE_BODY, 1), placeholder(STOCK)),
			BLACK_DHIDE_BODY, "alch");
		// Untradeable: no alch value at all.
		price(BLACK_DHIDE_BODY, 0, 0);
		assertTag(analyze(CompositeItemCatalog.DEFAULT, owned(BLACK_DHIDE_BODY, 1), placeholder(STOCK)),
			BLACK_DHIDE_BODY, "gear");
	}

	private void price(int id, int alchValue, int gePrice)
	{
		alchValues.put(id, alchValue);
		gePrices.put(id, gePrice);
	}

	private BankOrganizationPreview analyze(ItemCatalog catalog, BankItemSnapshot... items)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		Map<Integer, Integer> values = new HashMap<>();
		for (BankItemSnapshot item : items)
		{
			entries.add(new BankItemSnapshot(item.getItemId(), item.getQuantity(), entries.size(), item.isPlaceholder()));
			values.put(item.getItemId(), alchValues.getOrDefault(item.getItemId(), 20000));
		}
		AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		BankAnalysisRequest request = new BankAnalysisRequest(new BankSnapshot(entries), stats, values, gePrices,
			Collections.emptyMap(), BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset), preset);
		// A new analysis per bank: routing must not depend on what an earlier analysis saw.
		new BankAnalysis(Runnable::run, Runnable::run, () -> Optional.of(request), status::set, catalog, preset)
			.analyzeBank();
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
		return status.get().organizationPreview().orElseThrow(AssertionError::new);
	}

	private static BankItemSnapshot owned(int id, int quantity)
	{
		return new BankItemSnapshot(id, quantity, 0);
	}

	private static BankItemSnapshot placeholder(int id)
	{
		return new BankItemSnapshot(id, 0, 0, true);
	}

	private static GearStats defensive(int defence)
	{
		return new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, 0, 0, 0, defence, defence, defence, defence, defence, 0, 0);
	}

	private static GearStats ranged(int defence)
	{
		return new GearStats(GearSlot.BODY, 0, 0, 0, -15, 15, 0, 0, 0, defence, defence, defence, defence, defence, 0, 0);
	}

	private static void assertTag(BankOrganizationPreview preview, int id, String tag)
	{
		BankPreviewItem item = preview.getPlannedItems().stream().filter(planned -> planned.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
		assertEquals("item " + id, tag, item.getLayoutTagKey());
	}
}

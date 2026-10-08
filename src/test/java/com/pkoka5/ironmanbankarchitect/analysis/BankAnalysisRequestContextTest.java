package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class BankAnalysisRequestContextTest
{
	private static final int BODY = 1127;
	private static final int UPGRADE = 11832;
	private static final int ANCHOR = 10887;
	private static final BankPreset PRESET = BankPresets.IRONMAN;
	private static final BankLayoutPlan PLAN = BankLayoutPlan.defaultFor(PRESET);
	private static final BankLayoutOptions OPTIONS = BankLayoutOptions.defaultFor(PRESET);

	@Test
	public void unchangedFactsKeepTheContextIncludingFallbackPreset()
	{
		BankAnalysisRequest previous = request(bank(BODY));
		assertTrue(request(bank(BODY)).sameLayoutContext(previous, PRESET));
		BankAnalysisRequest fallback = new BankAnalysisRequest(bank(BODY), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(), PLAN, OPTIONS);
		assertTrue(fallback.sameLayoutContext(previous, PRESET));
	}

	@Test
	public void quantityAndPlaceholderChangesKeepTheContext()
	{
		BankAnalysisRequest previous = request(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(BODY, 5, 0), new BankItemSnapshot(UPGRADE, 1, 1))));
		BankAnalysisRequest current = request(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(UPGRADE, 0, 0, true), new BankItemSnapshot(BODY, 1, 1))));
		assertTrue(current.sameLayoutContext(previous, PRESET));
	}

	@Test
	public void newIdsAndTheirGearAndAlchMetadataKeepThePreviousContext()
	{
		Map<Integer, GearStats> oldStats = Collections.singletonMap(BODY, stats(10));
		Map<Integer, Integer> oldValues = Collections.singletonMap(BODY, 100);
		BankAnalysisRequest previous = request(bank(BODY), oldStats, oldValues, Collections.emptyMap());
		Map<Integer, GearStats> newStats = new LinkedHashMap<>(oldStats);
		newStats.put(ANCHOR, stats(20));
		Map<Integer, Integer> newValues = new LinkedHashMap<>(oldValues);
		newValues.put(ANCHOR, 200);
		BankAnalysisRequest current = request(bank(ANCHOR, BODY), newStats, newValues, Collections.emptyMap());

		assertTrue(current.sameLayoutContext(previous, PRESET));
		assertFalse("preservation is directional: losing the newly added ID resets it",
			previous.sameLayoutContext(current, PRESET));
	}

	@Test
	public void newIdsWithoutAdditionalMetadataKeepTheContext()
	{
		assertTrue(request(bank(BODY, ANCHOR)).sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void changedGearStatsForAPreviousIdResetTheContext()
	{
		BankAnalysisRequest previous = request(bank(BODY), Collections.singletonMap(BODY, stats(10)),
			Collections.emptyMap(), Collections.emptyMap());
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.singletonMap(BODY, stats(11)),
			Collections.emptyMap(), Collections.emptyMap());
		assertFalse(current.sameLayoutContext(previous, PRESET));
	}

	@Test
	public void newlyAvailableGearStatsForAPreviousIdResetTheContext()
	{
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.singletonMap(BODY, stats(10)),
			Collections.emptyMap(), Collections.emptyMap());
		assertFalse(current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void missingGearStatsForAPreviousIdResetTheContext()
	{
		BankAnalysisRequest previous = request(bank(BODY), Collections.singletonMap(BODY, stats(10)),
			Collections.emptyMap(), Collections.emptyMap());
		assertFalse(request(bank(BODY, ANCHOR)).sameLayoutContext(previous, PRESET));
	}

	@Test
	public void changedAlchValueForAPreviousIdResetsTheContext()
	{
		BankAnalysisRequest previous = request(bank(BODY), Collections.emptyMap(),
			Collections.singletonMap(BODY, 100), Collections.emptyMap());
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.emptyMap(),
			Collections.singletonMap(BODY, 101), Collections.emptyMap());
		assertFalse(current.sameLayoutContext(previous, PRESET));
	}

	@Test
	public void newlyAvailableZeroAlchValueForAPreviousIdResetsTheContext()
	{
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.emptyMap(),
			Collections.singletonMap(BODY, 0), Collections.emptyMap());
		assertFalse("an absent value and a reported zero are different facts",
			current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void missingZeroAlchValueForAPreviousIdResetsTheContext()
	{
		BankAnalysisRequest previous = request(bank(BODY), Collections.emptyMap(),
			Collections.singletonMap(BODY, 0), Collections.emptyMap());
		assertFalse(request(bank(BODY, ANCHOR)).sameLayoutContext(previous, PRESET));
	}

	@Test
	public void removingAPreviousIdResetsTheContextEvenWhenAnotherIdIsAdded()
	{
		assertFalse(request(bank(BODY, ANCHOR)).sameLayoutContext(request(bank(BODY, UPGRADE)), PRESET));
	}

	@Test
	public void increasingAnExistingIdsPhysicalOccurrencesResetsTheContext()
	{
		assertFalse(request(bank(BODY, BODY, ANCHOR)).sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void decreasingAnExistingIdsPhysicalOccurrencesResetsTheContext()
	{
		assertFalse(request(bank(BODY, ANCHOR)).sameLayoutContext(request(bank(BODY, BODY)), PRESET));
	}

	@Test
	public void changingThePresetResetsTheContextEvenWithTheSamePlanAndOptions()
	{
		BankAnalysisRequest current = new BankAnalysisRequest(bank(BODY), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(), PLAN, OPTIONS, BankPresets.MAIN);
		assertFalse(current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void changingThePlanResetsTheContext()
	{
		BankAnalysisRequest current = new BankAnalysisRequest(bank(BODY), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(), PLAN.withCurrentOrder(0, true), OPTIONS, PRESET);
		assertFalse(current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void changingTheLayoutOptionsResetsTheContext()
	{
		BankAnalysisRequest current = new BankAnalysisRequest(bank(BODY), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(), PLAN, new BankLayoutOptions(true, true, false), PRESET);
		assertFalse(current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void categoryMetadataForANewIdStillResetsTheContext()
	{
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.emptyMap(), Collections.emptyMap(),
			Collections.singletonMap(ANCHOR, "gear"));
		assertFalse("the full player category map must remain unchanged",
			current.sameLayoutContext(request(bank(BODY)), PRESET));
	}

	@Test
	public void changingAPreviousIdsCategoryChoiceResetsTheContext()
	{
		BankAnalysisRequest previous = request(bank(BODY), Collections.emptyMap(), Collections.emptyMap(),
			Collections.singletonMap(BODY, "gear"));
		BankAnalysisRequest current = request(bank(BODY, ANCHOR), Collections.emptyMap(), Collections.emptyMap(),
			Collections.singletonMap(BODY, "alch"));
		assertFalse(current.sameLayoutContext(previous, PRESET));
	}

	private static BankAnalysisRequest request(BankSnapshot bank)
	{
		return request(bank, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
	}

	private static BankAnalysisRequest request(BankSnapshot bank, Map<Integer, GearStats> stats,
		Map<Integer, Integer> values, Map<Integer, String> choices)
	{
		return new BankAnalysisRequest(bank, stats, values, choices, PLAN, OPTIONS, PRESET);
	}

	private static BankSnapshot bank(int... itemIds)
	{
		java.util.List<BankItemSnapshot> items = new java.util.ArrayList<>();
		for (int itemId : itemIds) items.add(new BankItemSnapshot(itemId, 1, items.size()));
		return new BankSnapshot(items);
	}

	private static GearStats stats(int strength)
	{
		return new GearStats(GearSlot.BODY, 0, 0, 0, 0, 0, strength, 0, 0,
			1, 1, 1, 1, 1, 0, 0);
	}
}

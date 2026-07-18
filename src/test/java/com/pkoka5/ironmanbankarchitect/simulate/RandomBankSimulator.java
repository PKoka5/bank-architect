package com.pkoka5.ironmanbankarchitect.simulate;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Phase;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

/**
 * Developer-only simulator that generates reproducible random banks from the
 * full item registry, derives the real Ironman organization plan for them, and
 * replays the {@link TabRouteAdvisor.Session} guidance loop until the bank is
 * complete. Where the transition matcher allows freedom (a distributed item can
 * land anywhere inside its tab, collapse landing order is ignored) the applied
 * position is randomized, so the walk also exercises states a real bank server
 * can produce. This class lives in the test source set and never ships.
 */
public final class RandomBankSimulator
{
	private static final String REGISTRY_RESOURCE =
		"/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv";
	private static final Pattern CACHE_ONLY_CONSTANT = Pattern.compile(
		"(^|_)(?:INTERFACE|PLACEHOLDER|DUMMY|NULL)(?:_|$)");
	private static final int MAX_TABS = TabRouteAdvisor.MAX_TABS;

	private RandomBankSimulator()
	{
	}

	public enum Scenario
	{
		/** Every item in main in random order; no numbered tabs yet. */
		SHUFFLED_NO_TABS,
		/** Random order plus a random dirty leading-tab partition. */
		RANDOM_TABS,
		/** The plan's finished layout perturbed by a few swaps and re-deposits. */
		NEARLY_SORTED
	}

	public enum Outcome
	{
		/** Guidance reached COMPLETE and the final order matches the plan. */
		COMPLETED,
		/** The generated plan contains blank targets; guidance fails closed. */
		UNSUPPORTED_PLAN,
		/** Building the preview/plan threw before guidance could start. */
		PLAN_BUILD_ERROR,
		/** The advisor returned a blocked status other than COMPLETE. */
		ADVISOR_BLOCKED,
		/** The session stopped advancing while moves were still expected. */
		STALLED,
		/** The move budget ran out before COMPLETE. */
		NON_TERMINATING
	}

	/** Loads all usable item IDs from the bundled item registry. */
	public static List<Integer> loadItemUniverse()
	{
		List<Integer> universe = new ArrayList<>();
		InputStream stream = RandomBankSimulator.class.getResourceAsStream(REGISTRY_RESOURCE);
		if (stream == null)
		{
			throw new IllegalStateException("item registry resource missing: " + REGISTRY_RESOURCE);
		}
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] columns = line.split("\\t", -1);
				if (columns.length != 4)
				{
					continue;
				}
				int itemId;
				try
				{
					itemId = Integer.parseInt(columns[0].replace("\uFEFF", ""));
				}
				catch (NumberFormatException ex)
				{
					continue;
				}
				String name = columns[1].trim();
				if (itemId <= 0 || itemId == ItemID.BANK_FILLER
					|| name.isEmpty() || "null".equalsIgnoreCase(name)
					|| "null item".equalsIgnoreCase(name)
					|| CACHE_ONLY_CONSTANT.matcher(
						columns[3].trim().toUpperCase(Locale.ROOT)).find())
				{
					continue;
				}
				universe.add(itemId);
			}
		}
		catch (IOException ex)
		{
			throw new UncheckedIOException(ex);
		}
		return universe;
	}

	public static SimulationResult simulate(long seed, Scenario scenario, int itemCount,
		List<Integer> universe)
	{
		Objects.requireNonNull(scenario, "scenario");
		Objects.requireNonNull(universe, "universe");
		if (itemCount < 1 || itemCount > universe.size())
		{
			throw new IllegalArgumentException("itemCount out of range: " + itemCount);
		}

		Random random = new Random(seed);
		List<Integer> sampled = sample(universe, itemCount, random);

		BankTabPlan plan;
		try
		{
			List<BankItemSnapshot> rawEntries = new ArrayList<>(sampled.size());
			for (int slot = 0; slot < sampled.size(); slot++)
			{
				rawEntries.add(new BankItemSnapshot(sampled.get(slot),
					1 + random.nextInt(100), slot));
			}
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				new BankSnapshot(rawEntries), CompositeItemCatalog.DEFAULT, BankPresets.IRONMAN);
			plan = BankTabPlan.fromPreview(preview);
		}
		catch (RuntimeException ex)
		{
			return SimulationResult.planBuildError(seed, scenario, itemCount, sampled, ex);
		}

		for (BankPreviewItem item : plan.getFlattenedItems())
		{
			if (item == null || item.isBlank() || item.getItemId() <= 0)
			{
				return SimulationResult.blocked(seed, scenario, itemCount, sampled,
					Outcome.UNSUPPORTED_PLAN, Status.UNSUPPORTED_PLAN, planTabs(plan));
			}
		}

		SimulatedBank bank = initialBank(scenario, sampled, plan, random);
		return walk(seed, scenario, itemCount, sampled, plan, bank, random);
	}

	private static SimulationResult walk(long seed, Scenario scenario, int itemCount,
		List<Integer> sampled, BankTabPlan plan, SimulatedBank bank, Random random)
	{
		TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
		Map<MoveType, Integer> moveCounts = new EnumMap<>(MoveType.class);
		int minimumSwapsAtSortStart = -1;
		int waits = 0;
		int tick = 0;
		int maxMoves = 6 * itemCount + 60;

		for (int moves = 0; moves <= maxMoves; moves++)
		{
			Assessment assessment = session.assess(bank.itemIds(), plan, bank.tabCounts(), ++tick);
			Status status = assessment.getStatus();
			if (status == Status.COMPLETE)
			{
				boolean verified = bank.matchesPlan(plan);
				return SimulationResult.completed(seed, scenario, itemCount, sampled, planTabs(plan),
					moveCounts, minimumSwapsAtSortStart, verified);
			}
			if (status == Status.WAITING_FOR_BANK)
			{
				if (++waits > itemCount + 8)
				{
					return SimulationResult.blocked(seed, scenario, itemCount, sampled,
						Outcome.STALLED, status, planTabs(plan));
				}
				continue;
			}
			if (status != Status.READY)
			{
				return SimulationResult.blocked(seed, scenario, itemCount, sampled,
					Outcome.ADVISOR_BLOCKED, status, planTabs(plan));
			}
			waits = 0;
			if (assessment.getProgress().getPhase() == Phase.SORTING && minimumSwapsAtSortStart < 0)
			{
				minimumSwapsAtSortStart = assessment.getProgress().getMinimumRemainingSwaps();
			}
			Move move = assessment.getMove().get();
			moveCounts.merge(move.getType(), 1, Integer::sum);
			bank.apply(move, random);
		}
		return SimulationResult.blocked(seed, scenario, itemCount, sampled,
			Outcome.NON_TERMINATING, Status.READY, planTabs(plan));
	}

	private static List<Integer> sample(List<Integer> universe, int itemCount, Random random)
	{
		List<Integer> copy = new ArrayList<>(universe);
		Collections.shuffle(copy, random);
		return new ArrayList<>(copy.subList(0, itemCount));
	}

	private static SimulatedBank initialBank(Scenario scenario, List<Integer> sampled,
		BankTabPlan plan, Random random)
	{
		switch (scenario)
		{
			case SHUFFLED_NO_TABS:
				return new SimulatedBank(new ArrayList<>(sampled), new int[MAX_TABS]);
			case RANDOM_TABS:
				return randomTabsBank(sampled, random);
			case NEARLY_SORTED:
			default:
				return nearlySortedBank(plan, random);
		}
	}

	private static SimulatedBank randomTabsBank(List<Integer> sampled, Random random)
	{
		List<Integer> items = new ArrayList<>(sampled);
		int[] counts = new int[MAX_TABS];
		int tabs = random.nextInt(Math.min(MAX_TABS, sampled.size()) + 1);
		int remaining = sampled.size();
		for (int tabIndex = 0; tabIndex < tabs && remaining > 1; tabIndex++)
		{
			counts[tabIndex] = 1 + random.nextInt(Math.max(1, remaining / 3));
			remaining -= counts[tabIndex];
		}
		return new SimulatedBank(items, counts);
	}

	private static SimulatedBank nearlySortedBank(BankTabPlan plan, Random random)
	{
		List<Integer> items = new ArrayList<>();
		for (BankPreviewItem item : plan.getFlattenedItems())
		{
			items.add(item.getItemId());
		}
		int[] counts = new int[MAX_TABS];
		List<BankTabPlan.TargetTab> targets = plan.getNumberedTabs();
		for (int tabIndex = 0; tabIndex < targets.size(); tabIndex++)
		{
			counts[tabIndex] = targets.get(tabIndex).getItems().size();
		}
		SimulatedBank bank = new SimulatedBank(items, counts);

		int perturbations = 2 + random.nextInt(6);
		for (int step = 0; step < perturbations; step++)
		{
			if (random.nextBoolean() || items.size() < 2)
			{
				bank.redepositRandomItem(random);
			}
			else
			{
				Collections.swap(items, random.nextInt(items.size()),
					random.nextInt(items.size()));
			}
		}
		return bank;
	}

	private static int planTabs(BankTabPlan plan)
	{
		return plan.getNumberedTabs().size();
	}

	/**
	 * Minimal bank model: a flat unique-item list plus leading tab counts, with
	 * the same free choices a real bank server has when applying each move.
	 */
	static final class SimulatedBank
	{
		private final List<Integer> items;
		private final int[] counts;

		SimulatedBank(List<Integer> items, int[] counts)
		{
			this.items = items;
			this.counts = counts;
		}

		int[] itemIds()
		{
			int[] ids = new int[items.size()];
			for (int slot = 0; slot < ids.length; slot++)
			{
				ids[slot] = items.get(slot);
			}
			return ids;
		}

		int[] tabCounts()
		{
			return counts.clone();
		}

		boolean matchesPlan(BankTabPlan plan)
		{
			List<BankPreviewItem> flattened = plan.getFlattenedItems();
			if (items.size() != flattened.size())
			{
				return false;
			}
			for (int slot = 0; slot < items.size(); slot++)
			{
				if (items.get(slot) != flattened.get(slot).getItemId())
				{
					return false;
				}
			}
			return true;
		}

		void apply(Move move, Random random)
		{
			switch (move.getType())
			{
				case COLLAPSE_TAB:
					collapse(move.getTargetTab(), random);
					break;
				case DRAG_TO_NEW_TAB:
					create(move.getFromSlot(), move.getTargetTab());
					break;
				case DISTRIBUTE_TO_TAB:
					distribute(move.getFromSlot(), move.getTargetTab(), random);
					break;
				case TRANSFER_TO_TAB:
					transfer(move.getFromSlot(), move.getSourceTab(), move.getTargetTab(), random);
					break;
				case RETURN_TO_MAIN:
					returnToMain(move.getFromSlot(), move.getSourceTab(), random);
					break;
				case SWAP_SECTION:
				default:
					Collections.swap(items, move.getFromSlot(), move.getToSlot());
					break;
			}
		}

		/** Simulates a withdraw + redeposit: the item leaves its tab and lands in main. */
		void redepositRandomItem(Random random)
		{
			int slot = random.nextInt(items.size());
			int tabIndex = tabIndexForSlot(slot);
			if (tabIndex >= 0 && counts[tabIndex] <= 1)
			{
				return;
			}
			int itemId = items.remove(slot);
			if (tabIndex >= 0)
			{
				counts[tabIndex]--;
			}
			items.add(insertPosition(mainStart(), items.size(), random), itemId);
		}

		private void collapse(int targetTab, Random random)
		{
			int targetIndex = targetTab - 1;
			int start = sectionStart(targetIndex);
			List<Integer> removed = new ArrayList<>(
				items.subList(start, start + counts[targetIndex]));
			items.subList(start, start + counts[targetIndex]).clear();
			counts[targetIndex] = 0;
			Collections.shuffle(removed, random);
			for (int itemId : removed)
			{
				items.add(insertPosition(mainStart(), items.size(), random), itemId);
			}
		}

		private void create(int fromSlot, int targetTab)
		{
			int targetIndex = targetTab - 1;
			int itemId = items.remove(fromSlot);
			items.add(sectionStart(targetIndex), itemId);
			counts[targetIndex] = 1;
		}

		private void distribute(int fromSlot, int targetTab, Random random)
		{
			int targetIndex = targetTab - 1;
			int itemId = items.remove(fromSlot);
			int start = sectionStart(targetIndex);
			items.add(insertPosition(start, start + counts[targetIndex], random), itemId);
			counts[targetIndex]++;
		}

		private void transfer(int fromSlot, int sourceTab, int targetTab, Random random)
		{
			int itemId = items.remove(fromSlot);
			counts[sourceTab - 1]--;
			int start = sectionStart(targetTab - 1);
			items.add(insertPosition(start, start + counts[targetTab - 1], random), itemId);
			counts[targetTab - 1]++;
		}

		private void returnToMain(int fromSlot, int sourceTab, Random random)
		{
			int itemId = items.remove(fromSlot);
			counts[sourceTab - 1]--;
			items.add(insertPosition(mainStart(), items.size(), random), itemId);
		}

		private int tabIndexForSlot(int slot)
		{
			int start = 0;
			for (int tabIndex = 0; tabIndex < counts.length && counts[tabIndex] > 0; tabIndex++)
			{
				if (slot < start + counts[tabIndex])
				{
					return tabIndex;
				}
				start += counts[tabIndex];
			}
			return -1;
		}

		private int sectionStart(int tabIndex)
		{
			int start = 0;
			for (int index = 0; index < tabIndex; index++)
			{
				start += counts[index];
			}
			return start;
		}

		private int mainStart()
		{
			int start = 0;
			for (int count : counts)
			{
				start += count;
			}
			return start;
		}

		private static int insertPosition(int startInclusive, int endInclusive, Random random)
		{
			return startInclusive + random.nextInt(endInclusive - startInclusive + 1);
		}
	}

	public static final class SimulationResult
	{
		private final long seed;
		private final Scenario scenario;
		private final int itemCount;
		private final int planTabs;
		private final Outcome outcome;
		private final Status finalStatus;
		private final Map<MoveType, Integer> moveCounts;
		private final int minimumSwapsAtSortStart;
		private final boolean finalOrderVerified;
		private final String errorMessage;
		private final List<Integer> failedItemIds;
		private final List<Integer> sampledItemIds;

		private SimulationResult(long seed, Scenario scenario, int itemCount,
			List<Integer> sampledItemIds, int planTabs,
			Outcome outcome, Status finalStatus, Map<MoveType, Integer> moveCounts,
			int minimumSwapsAtSortStart, boolean finalOrderVerified, String errorMessage,
			List<Integer> failedItemIds)
		{
			this.seed = seed;
			this.scenario = scenario;
			this.itemCount = itemCount;
			this.planTabs = planTabs;
			this.outcome = outcome;
			this.finalStatus = finalStatus;
			this.moveCounts = moveCounts;
			this.minimumSwapsAtSortStart = minimumSwapsAtSortStart;
			this.finalOrderVerified = finalOrderVerified;
			this.errorMessage = errorMessage;
			this.failedItemIds = failedItemIds;
			this.sampledItemIds = new ArrayList<>(sampledItemIds);
		}

		static SimulationResult completed(long seed, Scenario scenario, int itemCount,
			List<Integer> sampled, int planTabs, Map<MoveType, Integer> moveCounts,
			int minimumSwapsAtSortStart, boolean finalOrderVerified)
		{
			return new SimulationResult(seed, scenario, itemCount, sampled, planTabs,
				Outcome.COMPLETED, Status.COMPLETE, moveCounts, minimumSwapsAtSortStart,
				finalOrderVerified, "", Collections.emptyList());
		}

		static SimulationResult blocked(long seed, Scenario scenario, int itemCount,
			List<Integer> sampled, Outcome outcome, Status status, int planTabs)
		{
			return new SimulationResult(seed, scenario, itemCount, sampled, planTabs, outcome,
				status, new EnumMap<>(MoveType.class), -1, false, "", new ArrayList<>(sampled));
		}

		static SimulationResult planBuildError(long seed, Scenario scenario, int itemCount,
			List<Integer> sampled, RuntimeException ex)
		{
			return new SimulationResult(seed, scenario, itemCount, sampled, 0,
				Outcome.PLAN_BUILD_ERROR, null, new EnumMap<>(MoveType.class), -1, false,
				ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()),
				new ArrayList<>(sampled));
		}

		public long getSeed()
		{
			return seed;
		}

		public Scenario getScenario()
		{
			return scenario;
		}

		public int getItemCount()
		{
			return itemCount;
		}

		public int getPlanTabs()
		{
			return planTabs;
		}

		public Outcome getOutcome()
		{
			return outcome;
		}

		public Status getFinalStatus()
		{
			return finalStatus;
		}

		public Map<MoveType, Integer> getMoveCounts()
		{
			return Collections.unmodifiableMap(moveCounts);
		}

		public int getTotalMoves()
		{
			int total = 0;
			for (int count : moveCounts.values())
			{
				total += count;
			}
			return total;
		}

		public int getSwapMoves()
		{
			return moveCounts.getOrDefault(MoveType.SWAP_SECTION, 0);
		}

		public int getMinimumSwapsAtSortStart()
		{
			return minimumSwapsAtSortStart;
		}

		public boolean isFinalOrderVerified()
		{
			return finalOrderVerified;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}

		public List<Integer> getFailedItemIds()
		{
			return Collections.unmodifiableList(failedItemIds);
		}

		public List<Integer> getSampledItemIds()
		{
			return Collections.unmodifiableList(sampledItemIds);
		}
	}
}

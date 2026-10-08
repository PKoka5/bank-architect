package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import org.junit.Test;

public class WholeTabGuidanceTest
{
	@Test
	public void swappedCompleteTabsNeedOneHeaderDragInEitherItemMode()
	{
		BankTabPlan plan = plan(new int[] {9, 10}, new int[] {1, 2}, new int[] {3, 4, 5});
		for (RearrangeMode mode : RearrangeMode.values())
		{
			State bank = new State(new int[] {9, 10}, new int[] {3, 4, 5}, new int[] {1, 2});
			TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
			TabRouteAdvisor.Move move = session.assess(bank.ids(), plan, bank.counts(), 1, 0, mode).getMove().get();
			assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB, move.getType());
			assertEquals(1, move.getSourceTab());
			assertEquals(2, move.getTargetTab());
			assertEquals(-1, move.getItemId());
			assertEquals(-1, move.getFromSlot());
			assertEquals(-1, move.getToSlot());
			bank.swap(move);
			assertEquals(TabRouteAdvisor.Status.COMPLETE,
				session.assess(bank.ids(), plan, bank.counts(), 1, 0, mode).getStatus());
		}
	}

	@Test
	public void everyFourTabPermutationUsesExactlyItsAdjacentInversionCount()
	{
		int[][] tabs = {{1}, {2, 3}, {4, 5, 6}, {7, 8, 9, 10}};
		BankTabPlan plan = plan(new int[] {99}, tabs);
		List<List<Integer>> orders = new ArrayList<>();
		permutations(new ArrayList<>(Arrays.asList(0, 1, 2, 3)), 0, orders);
		for (RearrangeMode mode : RearrangeMode.values())
			for (List<Integer> order : orders)
			{
				int[][] reordered = order.stream().map(index -> tabs[index]).toArray(int[][]::new);
				State bank = new State(new int[] {99}, reordered);
				int inversions = 0;
				for (int i = 0; i < order.size(); i++)
					for (int j = i + 1; j < order.size(); j++) if (order.get(i) > order.get(j)) inversions++;
				TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
				for (int step = 0; step < inversions; step++)
				{
					TabRouteAdvisor.Assessment advice = session.assess(bank.ids(), plan, bank.counts(), step, 0, mode);
					assertEquals(order.toString(), TabRouteAdvisor.Status.READY, advice.getStatus());
					assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB, advice.getMove().get().getType());
					bank.swap(advice.getMove().get());
				}
				assertEquals(order.toString(), TabRouteAdvisor.Status.COMPLETE,
					session.assess(bank.ids(), plan, bank.counts(), inversions, 0, mode).getStatus());
			}
	}

	@Test
	public void headerDragPreservesUnsortedInternalOrderAndMainExactly()
	{
		BankTabPlan plan = plan(new int[] {9, 10}, new int[] {1, 2}, new int[] {3, 4, 5});
		State bank = new State(new int[] {10, 9}, new int[] {5, 3, 4}, new int[] {2, 1});
		TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
		TabRouteAdvisor.Move move = session.assess(bank.ids(), plan, bank.counts(), 1).getMove().get();
		bank.swap(move);
		assertArrayEquals(new int[] {2, 1, 5, 3, 4, 10, 9}, bank.ids());
		TabRouteAdvisor.Assessment next = session.assess(bank.ids(), plan, bank.counts(), 2);
		assertEquals(TabRouteAdvisor.Phase.SORTING, next.getProgress().getPhase());
		assertEquals(TabRouteAdvisor.MoveType.SWAP_SECTION, next.getMove().get().getType());
	}

	@Test
	public void partialCleanTabsCanMoveBeforeCreatingMissingTabs()
	{
		BankTabPlan plan = plan(new int[] {9}, new int[] {1, 2}, new int[] {3, 4}, new int[] {5});
		State bank = new State(new int[] {9, 2, 4, 5}, new int[] {3}, new int[] {1});
		assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB,
			TabRouteAdvisor.assess(bank.ids(), plan, bank.counts()).getMove().get().getType());
	}

	@Test
	public void mixedTabsOnlyMoveWhenTheirMisplacedMembershipFalls()
	{
		BankTabPlan plan = plan(new int[] {9}, new int[] {1, 2}, new int[] {3, 4});
		State improves = new State(new int[0], new int[] {3, 4, 9}, new int[] {1, 2});
		assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB,
			TabRouteAdvisor.assess(improves.ids(), plan, improves.counts()).getMove().get().getType());
		State doesNotImprove = new State(new int[0], new int[] {1, 3}, new int[] {2, 4, 9});
		assertNotEquals(TabRouteAdvisor.MoveType.REORDER_TAB,
			TabRouteAdvisor.assess(doesNotImprove.ids(), plan, doesNotImprove.counts()).getMove().get().getType());
	}

	@Test
	public void mixedTabHeaderDragDoesNotSpendOneMoveToSaveOnlyOneMisplacedItem()
	{
		BankTabPlan plan = plan(new int[] {9}, new int[] {1}, new int[] {2, 3});
		State bank = new State(new int[0], new int[] {2}, new int[] {1, 9, 3});
		assertNotEquals(TabRouteAdvisor.MoveType.REORDER_TAB,
			TabRouteAdvisor.assess(bank.ids(), plan, bank.counts()).getMove().get().getType());
	}

	@Test
	public void separateCountAndItemSamplesWaitUntilTheCompleteHeaderDragArrives()
	{
		BankTabPlan plan = plan(new int[] {9}, new int[] {1, 2}, new int[] {3, 4, 5});
		for (boolean countsFirst : new boolean[] {true, false})
			for (RearrangeMode mode : RearrangeMode.values())
			{
				State bank = new State(new int[] {9}, new int[] {3, 4, 5}, new int[] {1, 2});
				TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
				int[] oldIds = bank.ids(), oldCounts = bank.counts();
				TabRouteAdvisor.Move move = session.assess(oldIds, plan, oldCounts, 1, 0, mode).getMove().get();
				bank.swap(move);
				assertEquals(TabRouteAdvisor.Status.WAITING_FOR_BANK, session.assess(
					countsFirst ? oldIds : bank.ids(), plan, countsFirst ? bank.counts() : oldCounts, 2, 0, mode).getStatus());
				assertEquals(TabRouteAdvisor.Status.COMPLETE,
					session.assess(bank.ids(), plan, bank.counts(), 2, 0, mode).getStatus());
			}
	}

	@Test
	public void anUnadvisedHeaderDragIsRecognizedAfterAStableTick()
	{
		BankTabPlan plan = plan(new int[] {9}, new int[] {1, 2}, new int[] {3, 4});
		State bank = new State(new int[] {9}, new int[] {1, 2}, new int[] {3, 4});
		TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
		assertEquals(TabRouteAdvisor.Status.COMPLETE, session.assess(bank.ids(), plan, bank.counts(), 1).getStatus());
		Collections.swap(bank.tabs, 0, 1);
		assertEquals(TabRouteAdvisor.Status.WAITING_FOR_BANK, session.assess(bank.ids(), plan, bank.counts(), 2).getStatus());
		assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB,
			session.assess(bank.ids(), plan, bank.counts(), 3).getMove().get().getType());
	}

	@Test
	public void headerDragDoesNotAcknowledgeChangedInternalOrderOrMain()
	{
		BankTabPlan plan = plan(new int[] {9, 10}, new int[] {1, 2}, new int[] {3, 4});
		for (boolean changeMain : new boolean[] {true, false})
		{
			State bank = new State(new int[] {9, 10}, new int[] {3, 4}, new int[] {1, 2});
			TabRouteAdvisor.Session session = new TabRouteAdvisor.Session();
			bank.swap(session.assess(bank.ids(), plan, bank.counts(), 1).getMove().get());
			Collections.swap(changeMain ? bank.main : bank.tabs.get(0), 0, 1);
			assertEquals(TabRouteAdvisor.Status.WAITING_FOR_BANK, session.assess(bank.ids(), plan, bank.counts(), 2).getStatus());
			assertEquals(TabRouteAdvisor.Status.MANUAL_RECOVERY_REQUIRED, session.assess(bank.ids(), plan, bank.counts(), 3).getStatus());
		}
	}

	private static BankTabPlan plan(int[] main, int[]... tabs)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int category = 0; category < 10; category++)
		{
			int[] ids = category == 0 ? main : category <= tabs.length ? tabs[category - 1] : new int[0];
			List<BankPreviewItem> items = new ArrayList<>();
			for (int id : ids) items.add(new BankPreviewItem(id, "Item " + id, 1));
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(category), items));
		}
		return BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories));
	}

	private static void permutations(List<Integer> order, int index, List<List<Integer>> result)
	{
		if (index == order.size()) { result.add(new ArrayList<>(order)); return; }
		for (int swap = index; swap < order.size(); swap++)
		{
			Collections.swap(order, index, swap);
			permutations(order, index + 1, result);
			Collections.swap(order, index, swap);
		}
	}

	private static final class State
	{
		final List<List<Integer>> tabs = new ArrayList<>();
		final List<Integer> main;
		State(int[] main, int[]... tabs)
		{
			this.main = list(main);
			for (int[] tab : tabs) this.tabs.add(list(tab));
		}
		int[] ids()
		{
			List<Integer> result = new ArrayList<>();
			for (List<Integer> tab : tabs) result.addAll(tab);
			result.addAll(main);
			return result.stream().mapToInt(Integer::intValue).toArray();
		}
		int[] counts()
		{
			int[] result = new int[9];
			for (int tab = 0; tab < tabs.size(); tab++) result[tab] = tabs.get(tab).size();
			return result;
		}
		void swap(TabRouteAdvisor.Move move)
		{
			assertEquals(TabRouteAdvisor.MoveType.REORDER_TAB, move.getType());
			assertEquals(1, Math.abs(move.getSourceTab() - move.getTargetTab()));
			Collections.swap(tabs, move.getSourceTab() - 1, move.getTargetTab() - 1);
		}
		static List<Integer> list(int[] ids)
		{
			List<Integer> result = new ArrayList<>();
			for (int id : ids) result.add(id);
			return result;
		}
	}
}

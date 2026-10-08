package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.RearrangeMode;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.Rectangle;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

public class WholeTabOverlayTest
{
	@Test
	public void wholeTabHudNamesBankPositionsIncludingMainInsteadOfBlueprintCategoryNumbers()
	{
		for (RearrangeMode mode : RearrangeMode.values())
		{
			Assessment assessment = reorderedTabs(mode);
			Move move = assessment.getMove().get();
			assertEquals(MoveType.REORDER_TAB, move.getType());
			assertEquals(-1, move.getBlueprintTabNumber());
			assertEquals(-1, move.getFromSlot());
			assertEquals(-1, move.getToSlot());
			String hud = BankGuideOverlay.tabHudText(assessment, true, mode);
			assertTrue(hud.contains("REORDER TABS"));
			assertTrue(hud.contains("FROM POS " + (move.getSourceTab() + 1)
				+ " -> POS " + (move.getTargetTab() + 1)));
			assertTrue(hud.contains("Drag the whole tab"));
			assertFalse(hud.contains("Collapse"));
			assertFalse(hud.contains("MOVE -> DROP"));
			assertFalse(hud.contains("SWAPS LEFT"));
		}
	}

	@Test
	public void disabledNextMoveHighlightsDoNotLeakWholeTabInstructions()
	{
		String hud = BankGuideOverlay.tabHudText(reorderedTabs(RearrangeMode.SWAP),
			false, RearrangeMode.SWAP);
		assertTrue(hud.contains("Highlights disabled"));
		assertFalse(hud.contains("FROM POS"));
		assertFalse(hud.contains("Drag the whole tab"));
	}

	@Test
	public void wholeTabMoveTargetsHeadersAndHasNoLocalItemGridMove()
	{
		Move move = reorderedTabs(RearrangeMode.SWAP).getMove().get();
		assertTrue(BankGuideOverlay.isTabTargetMove(MoveType.REORDER_TAB));
		assertFalse(BankGuideOverlay.isGridMove(MoveType.REORDER_TAB));
		assertFalse(BankGuideOverlay.isSectionLocalMove(move, 0, 2));
	}

	@Test
	public void sourceAndTargetUseVisibleExistingTabHeadersAtTheirPhysicalIndices()
	{
		Move move = reorderedTabs(RearrangeMode.SWAP).getMove().get();
		Widget source = header(move.getSourceTab(), false, new Rectangle(100, 80, 40, 38),
			new String[]{"View tab", "Collapse tab"});
		Widget target = header(move.getTargetTab(), false, new Rectangle(144, 80, 40, 38),
			new String[]{"View tab", "Collapse tab"});
		Map<Integer, Widget> children = new HashMap<>();
		children.put(10 + move.getSourceTab(), source);
		children.put(10 + move.getTargetTab(), target);
		Widget tabs = tabBar(false, children);
		assertSame(source, BankGuideOverlay.resolveTabHeader(tabs, move.getSourceTab(), "View tab"));
		assertSame(target, BankGuideOverlay.resolveTabHeader(tabs, move.getTargetTab(), "View tab"));
	}

	@Test
	public void missingOrHiddenTabBarAndAbsentEndpointFailClosed()
	{
		assertNull(BankGuideOverlay.resolveTabHeader(null, 1, "View tab"));
		Widget child = header(1, false, new Rectangle(100, 80, 40, 38), new String[]{"View tab"});
		Map<Integer, Widget> children = Collections.singletonMap(11, child);
		assertNull(BankGuideOverlay.resolveTabHeader(tabBar(true, children), 1, "View tab"));
		assertNull(BankGuideOverlay.resolveTabHeader(tabBar(false, children), 2, "View tab"));
	}

	@Test
	public void hiddenOrMismatchedHeaderCannotStandInForAnEndpoint()
	{
		Widget hidden = header(1, true, new Rectangle(100, 80, 40, 38), new String[]{"View tab"});
		assertNull(BankGuideOverlay.resolveTabHeader(
			tabBar(false, Collections.singletonMap(11, hidden)), 1, "View tab"));
		Widget wrongIndex = header(2, false, new Rectangle(100, 80, 40, 38), new String[]{"View tab"});
		assertNull(BankGuideOverlay.resolveTabHeader(
			tabBar(false, Collections.singletonMap(11, wrongIndex)), 1, "View tab"));
	}

	@Test
	public void emptyNewOrUnrecognisedTabActionsDoNotResolveAsExistingTabs()
	{
		for (String[] actions : new String[][]{null, {}, {"New tab"}, {"Collapse tab"}, {"View all items"}})
		{
			Widget child = header(1, false, new Rectangle(100, 80, 40, 38), actions);
			assertNull(BankGuideOverlay.resolveTabHeader(
				tabBar(false, Collections.singletonMap(11, child)), 1, "View tab"));
		}
	}

	@Test
	public void unavailableHeaderGeometryFailsClosed()
	{
		for (Rectangle bounds : new Rectangle[]{null, new Rectangle(100, 80, 0, 38),
			new Rectangle(100, 80, 40, 0), new Rectangle(100, 80, -1, 38)})
		{
			Widget child = header(1, false, bounds, new String[]{"View tab"});
			assertNull(BankGuideOverlay.resolveTabHeader(
				tabBar(false, Collections.singletonMap(11, child)), 1, "View tab"));
		}
	}

	@Test
	public void allItemsTargetStaysReservedForMainRecoveryAndNumberedTabsStayWithinBounds()
	{
		Widget allItems = header(0, false, new Rectangle(56, 80, 40, 38),
			new String[]{"View all items"});
		Widget ninth = header(9, false, new Rectangle(496, 80, 40, 38), new String[]{"View tab"});
		Map<Integer, Widget> children = new HashMap<>();
		children.put(10, allItems);
		children.put(19, ninth);
		Widget tabs = tabBar(false, children);
		assertSame(allItems, BankGuideOverlay.resolveTabHeader(tabs, 0, "View all items"));
		assertNull(BankGuideOverlay.resolveTabHeader(tabs, 0, "View tab"));
		assertNull(BankGuideOverlay.resolveTabHeader(tabs, 0, "New tab"));
		assertSame(ninth, BankGuideOverlay.resolveTabHeader(tabs, 9, "View tab"));
		assertNull(BankGuideOverlay.resolveTabHeader(tabs, -1, "View tab"));
		assertNull(BankGuideOverlay.resolveTabHeader(tabs, 10, "View tab"));
	}

	private static Assessment reorderedTabs(RearrangeMode mode)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++)
		{
			List<BankPreviewItem> items = index == 0 ? items(99)
				: index == 3 ? items(11, 12) : index == 6 ? items(21, 22) : Collections.emptyList();
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index), items));
		}
		BankTabPlan plan = BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories));
		return TabRouteAdvisor.assess(new int[]{21, 22, 11, 12, 99}, plan,
			new int[]{2, 2, 0, 0, 0, 0, 0, 0, 0}, 0, mode);
	}

	private static List<BankPreviewItem> items(int... ids)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		for (int id : ids)
		{
			items.add(new BankPreviewItem(id, "Item " + id, 1));
		}
		return items;
	}

	private static Widget tabBar(boolean hidden, Map<Integer, Widget> children)
	{
		return (Widget) Proxy.newProxyInstance(WholeTabOverlayTest.class.getClassLoader(),
			new Class<?>[]{Widget.class}, (instance, method, arguments) -> {
				if ("isHidden".equals(method.getName())) return hidden;
				if ("getChild".equals(method.getName())) return children.get((Integer) arguments[0]);
				throw new AssertionError("Unexpected tab-bar access " + method.getName());
			});
	}

	private static Widget header(int tab, boolean hidden, Rectangle bounds, String[] actions)
	{
		return (Widget) Proxy.newProxyInstance(WholeTabOverlayTest.class.getClassLoader(),
			new Class<?>[]{Widget.class}, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return hidden;
					case "getIndex": return 10 + tab;
					case "getBounds": return bounds;
					case "getActions": return actions;
					default: throw new AssertionError("Unexpected tab-header access " + method.getName());
				}
			});
	}
}

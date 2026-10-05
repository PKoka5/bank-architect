package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class KeepCurrentOrderOverlayTest
{
	@Test public void currentMainAndKeptTabAreGreenWhileNormalMisorderStaysAmber() throws Exception
	{
		BankTabPlan plan = plan();
		int[] live = {41, 40, 71, 70, 11, 10};
		List<BankPreviewItem> effective = plan.effectiveItems(live, new int[]{2, 2, 0, 0, 0, 0, 0, 0, 0});
		Map<Integer, Integer> cachedSlots = slots(plan.getFlattenedItems());
		for (int slot = 0; slot < live.length; slot++)
		{
			BankGuideOverlay.SlotValidationState state = state(effective, cachedSlots, slot, live[slot]);
			boolean kept = slot < 2 || slot >= 4;
			assertEquals(kept ? BankGuideOverlay.SlotValidationState.CORRECT
				: BankGuideOverlay.SlotValidationState.MISPLACED, state);
			java.awt.Color fill = BankGuideOverlay.fillFor(state);
			assertTrue("Green for kept order; amber for normal misorder", kept
				? fill.getGreen() > fill.getRed() && fill.getGreen() > fill.getBlue()
				: fill.getRed() > fill.getGreen() && fill.getGreen() > fill.getBlue());
			assertTrue(BankGuideOverlay.drawsValidation(state, false));
			assertEquals(!kept, BankGuideOverlay.drawsValidation(state, true));
		}
	}

	@Test public void keepPolicyDoesNotColorForeignMembersGreen() throws Exception
	{
		BankTabPlan plan = plan();
		int[] live = {41, 10, 70, 71, 11, 40};
		List<BankPreviewItem> effective = plan.effectiveItems(live, new int[]{2, 2, 0, 0, 0, 0, 0, 0, 0});
		Map<Integer, Integer> cachedSlots = slots(plan.getFlattenedItems());
		assertEquals(BankGuideOverlay.SlotValidationState.CORRECT, state(effective, cachedSlots, 0, 41));
		assertEquals(BankGuideOverlay.SlotValidationState.MISPLACED, state(effective, cachedSlots, 1, 10));
		assertEquals(BankGuideOverlay.SlotValidationState.MISPLACED, state(effective, cachedSlots, 5, 40));
		assertTrue(BankGuideOverlay.drawsValidation(state(effective, cachedSlots, 1, 10), true));
	}

	private static BankGuideOverlay.SlotValidationState state(List<BankPreviewItem> target,
		Map<Integer, Integer> cachedSlots, int slot, int itemId) throws Exception
	{
		// Development-only access verifies the exact helper used by render; no production API is added.
		Method method = BankGuideOverlay.class.getDeclaredMethod("stateFor", List.class, Map.class, int.class, int.class);
		method.setAccessible(true);
		return (BankGuideOverlay.SlotValidationState) method.invoke(null, target, cachedSlots, slot, itemId);
	}

	private static Map<Integer, Integer> slots(List<BankPreviewItem> items)
	{
		Map<Integer, Integer> result = new HashMap<>();
		for (int index = 0; index < items.size(); index++) result.put(items.get(index).getItemId(), index);
		return result;
	}

	private static BankTabPlan plan()
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++)
		{
			List<BankPreviewItem> items = new ArrayList<>();
			int[] ids = index == 0 ? new int[]{10, 11} : index == 3 ? new int[]{40, 41}
				: index == 6 ? new int[]{70, 71} : new int[0];
			for (int id : ids) items.add(new BankPreviewItem(id, "Item " + id, 1));
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index), items));
		}
		return BankTabPlan.fromPreview(new BankOrganizationPreview(BankPresets.IRONMAN, categories),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withCurrentOrder(0, true).withCurrentOrder(3, true));
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class KeepCurrentOrderPlanTest
{
	private static final BankLayoutPlan DEFAULT = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);

	@Test public void existingAndLegacyPlansContinueSortingEveryDestination()
	{
		for (String value : new String[]{null, "", DEFAULT.serialize(), "main,gear,supplies,herblore,skilling-tools"})
		{
			BankLayoutPlan restored = BankLayoutPlan.parse(BankPresets.IRONMAN, value);
			for (int index = 0; index < 10; index++) assertFalse(restored.keepsCurrentOrder(index));
		}
		assertEquals(DEFAULT.serialize(), DEFAULT.withCurrentOrder(3, false).serialize());
	}

	@Test public void profileRoundTripAndOrdinaryTagEditsRetainOnlyTheirOwnKeepChoices()
	{
		BankLayoutPlan kept = DEFAULT.withCurrentOrder(0, true).withCurrentOrder(3, true);
		assertEquals(DEFAULT.getDestinations(), kept.getDestinations());
		assertFalse(kept.isDefault(BankPresets.IRONMAN));
		kept = kept.withTagAt("gear", 7).withTagShifted("potions", -1).completedFor(BankPresets.IRONMAN);
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", BankLayoutProfiles.DEFAULT_NAME)
			.withProfile("Keep my gear", kept.serialize()).withProfile("Sort everything", DEFAULT.serialize());
		profiles = BankLayoutProfiles.parse(profiles.serialize(), "Keep my gear");
		BankLayoutPlan restored = BankLayoutPlan.parse(BankPresets.IRONMAN, profiles.activePlan());
		assertTrue(restored.keepsCurrentOrder(0));
		assertTrue(restored.keepsCurrentOrder(3));
		assertFalse(restored.keepsCurrentOrder(7));
		assertEquals(7, restored.destinationOf("gear"));
		assertEquals(kept.getDestinations(), restored.getDestinations());
		BankLayoutPlan other = BankLayoutPlan.parse(BankPresets.IRONMAN, profiles.withActive("Sort everything").activePlan());
		for (int index = 0; index < 10; index++) assertFalse(other.keepsCurrentOrder(index));
		assertEquals("", profiles.planFor(BankLayoutProfiles.DEFAULT_NAME));
	}

	@Test public void sharedLayoutsKeepEachDestinationChoiceThroughSeparatorEncoding()
	{
		BankLayoutPlan kept = DEFAULT;
		for (int index = 0; index < 10; index += 2) kept = kept.withCurrentOrder(index, true);
		BankLayoutShareCode shared = BankLayoutShareCode.decode(BankLayoutShareCode.encode("Keep ~ my gear", kept)).get();
		assertEquals("Keep   my gear", shared.getName());
		BankLayoutPlan imported = BankLayoutPlan.parse(BankPresets.IRONMAN, shared.getPlan());
		for (int index = 0; index < 10; index++) assertEquals(index % 2 == 0, imported.keepsCurrentOrder(index));
		assertEquals(DEFAULT.getDestinations(), imported.getDestinations());
		for (int index = 0; index < 10; index++) imported = imported.withCurrentOrder(index, false);
		assertTrue(imported.isDefault(BankPresets.IRONMAN));
		assertEquals(DEFAULT.serialize(), imported.serialize());
	}

	@Test public void disablingKeepRestoresSavedManualOrderWithoutRewritingIt()
	{
		BankOrganizationPreview automatic = preview(40, 41, 42);
		BlueprintItemOrders manual = BlueprintItemOrders.EMPTY.withTab(3, Arrays.asList(42, 40, 41));
		String storedManual = manual.serialize();
		BankLayoutPlan kept = DEFAULT.withCurrentOrder(3, true);
		BankOrganizationPreview customized = manual.apply(automatic, kept);
		int[] live = {41, 42, 40};
		assertEquals(Arrays.asList(41, 42, 40), ids(BankTabPlan.fromPreview(customized, kept)
			.effectiveItems(live, new int[]{3, 0, 0, 0, 0, 0, 0, 0, 0})));
		BankLayoutPlan restored = BankLayoutPlan.parse(BankPresets.IRONMAN, kept.serialize()).withCurrentOrder(3, false);
		assertEquals(Arrays.asList(42, 40, 41), ids(BankTabPlan.fromPreview(manual.apply(automatic, restored), restored)
			.effectiveItems(live, new int[]{3, 0, 0, 0, 0, 0, 0, 0, 0})));
		assertEquals(storedManual, manual.serialize());
	}

	private static BankOrganizationPreview preview(int... ids)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++)
		{
			List<BankPreviewItem> items = new ArrayList<>();
			if (index == 3) for (int id : ids) items.add(new BankPreviewItem(id, "Item " + id, 1));
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index), items));
		}
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		List<Integer> result = new ArrayList<>();
		for (BankPreviewItem item : items) result.add(item.getItemId());
		return result;
	}
}

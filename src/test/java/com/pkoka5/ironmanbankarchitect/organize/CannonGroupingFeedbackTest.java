package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class CannonGroupingFeedbackTest
{
	private static final List<Integer> NORMAL = Arrays.asList(6, 8, 10, 12);
	private static final List<Integer> ORNAMENTED = Arrays.asList(26520, 26522, 26524, 26526);
	private static final List<BankPreset> PRESETS = Arrays.asList(BankPresets.IRONMAN, BankPresets.MAIN);

	@Test
	public void completeCannonsStayInCompactBlocksInEveryGearLayoutAndPreset()
	{
		for (List<Integer> cannonIds : Arrays.asList(NORMAL, ORNAMENTED, bothCannons()))
		{
			Fixture fixture = new Fixture().add(cannonIds);
			assertAutomaticLayouts(fixture, true);
		}
	}

	@Test
	public void spareCannonPartsDoNotSplitToCompleteASparseEquipmentRow()
	{
		Fixture fixture = new Fixture().addPrimaries(GearSlot.BODY, 0).addUtility(2).add(NORMAL);
		assertAutomaticLayouts(fixture, true);
	}

	@Test
	public void busyCombatTabsKeepCannonsTogetherAndPreservePrimaryStyleColumns()
	{
		Fixture fixture = new Fixture().addPrimaries(GearSlot.HEAD, 0)
			.addPrimaries(GearSlot.BODY, 1).addUtility(24)
			.add(Arrays.asList(544, 542, 4716, 4718, 4720, 4722, 9672, 9674, 9676))
			.add(bothCannons());
		for (BankPreset preset : PRESETS)
		{
			for (GearLayout layout : GearLayout.values())
			{
				BankCategoryPreview combat = combat(build(fixture, preset, options(preset, layout, true)), preset);
				assertBlocksAndContents(fixture, combat);
				if (layout == GearLayout.GRID_STYLES)
				{
					List<Integer> target = ids(combat);
					for (int row = 0; row < 2; row++)
					{
						for (int style = 0; style < 4; style++)
						{
							assertEquals("Primary combat style column changed",
								Integer.valueOf(primaryId(row, style)), target.get(row * 8 + style));
						}
					}
				}
			}
		}
	}

	@Test
	public void incompleteCannonsKeepTheirOwnedComponentsTogetherWithoutAddingMissingParts()
	{
		for (List<Integer> partial : Arrays.asList(
			Arrays.asList(6, 12), Arrays.asList(8, 10, 12),
			Arrays.asList(26520, 26524), Arrays.asList(26522, 26524, 26526),
			Arrays.asList(6, 8, 10, 12, 26522, 26526),
			Arrays.asList(6, 10, 26520, 26522, 26524, 26526)))
		{
			Fixture fixture = new Fixture().addPrimaries(GearSlot.BODY, 0).addUtility(3).add(partial);
			assertAutomaticLayouts(fixture, true);
		}
	}

	@Test
	public void mixedCannonFinishesStayTogetherEvenWhenOneVariantHasOnlyOneOwnedComponent()
	{
		for (List<Integer> parts : Arrays.asList(
			Arrays.asList(6, 8, 10, 26526), Arrays.asList(6, 26526),
			Arrays.asList(6, 26522, 26524, 26526),
			Collections.singletonList(6), Collections.singletonList(26526)))
		{
			Fixture fixture = new Fixture().add(Arrays.asList(4716, 4718, 4720, 4722))
				.addUtility(8).add(parts);
			for (BankPreset preset : PRESETS)
			{
				for (GearLayout layout : GearLayout.values())
				{
					BankCategoryPreview combat = combat(build(fixture, preset, options(preset, layout, true)), preset);
					assertBlocksAndContents(fixture, combat);
					assertCompactRectangle(ids(combat), bothCannons());
					if (parts.size() == 1)
					{
						assertEquals("A lone cannon component must stay before the gear-set run",
							parts.get(0), ids(combat).get(0));
					}
				}
			}
		}
	}

	@Test
	public void disablingEquipmentRowFillStillKeepsBothCannonsCompact()
	{
		Fixture fixture = new Fixture().addPrimaries(GearSlot.HEAD, 0).addUtility(5).add(bothCannons());
		assertAutomaticLayouts(fixture, false);
	}

	@Test
	public void capturedPhysicalOrderWinsWhenThePlayerChoosesToSeparateCannonParts()
	{
		Fixture fixture = new Fixture().addPrimaries(GearSlot.BODY, 0).addUtility(2).add(NORMAL);
		List<Integer> manual = Arrays.asList(6, primaryId(0, 0), 8, primaryId(0, 1),
			10, primaryId(0, 2), 12, primaryId(0, 3), 951000, 951001);
		for (BankPreset preset : PRESETS)
		{
			for (GearLayout layout : GearLayout.values())
			{
				BankLayoutOptions options = options(preset, layout, true);
				BankOrganizationPreview before = build(fixture, preset, options);
				int destination = BankLayoutPlan.defaultFor(preset).destinationOf("gear");
				BlueprintItemOrders captured = BlueprintItemOrders.capture(
					Collections.singletonMap(destination, manual), before, BlueprintItemOrders.EMPTY);
				BankCategoryPreview after = combat(build(fixture, preset, options.withItemOrders(captured)), preset);
				assertTrue("Captured order must retain manual placement", after.hasManualOrder());
				assertEquals(manual, ids(after));
				assertContents(fixture, after);
			}
		}
	}

	private static void assertAutomaticLayouts(Fixture fixture, boolean fillRows)
	{
		for (BankPreset preset : PRESETS)
		{
			for (GearLayout layout : GearLayout.values())
			{
				assertBlocksAndContents(fixture,
					combat(build(fixture, preset, options(preset, layout, fillRows)), preset));
			}
		}
	}

	private static void assertBlocksAndContents(Fixture fixture, BankCategoryPreview combat)
	{
		assertContents(fixture, combat);
		assertCompactRectangle(ids(combat), NORMAL);
		assertCompactRectangle(ids(combat), ORNAMENTED);
	}

	private static void assertContents(Fixture fixture, BankCategoryPreview combat)
	{
		List<Integer> expected = new ArrayList<>(fixture.items.keySet());
		List<Integer> actual = ids(combat);
		assertEquals("Unexpected item loss or duplication", expected.size(), actual.size());
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals("Combat contents changed", expected, actual);
		for (BankPreviewItem item : combat.getItems())
		{
			assertFalse("Cannon grouping inserted an empty bank slot", item.isBlank());
		}
	}

	private static void assertCompactRectangle(List<Integer> target, List<Integer> family)
	{
		List<Integer> positions = family.stream().filter(target::contains)
			.map(target::indexOf).collect(Collectors.toList());
		if (positions.size() < 2) return;
		int minRow = positions.stream().mapToInt(index -> index / 8).min().getAsInt();
		int maxRow = positions.stream().mapToInt(index -> index / 8).max().getAsInt();
		int minColumn = positions.stream().mapToInt(index -> index % 8).min().getAsInt();
		int maxColumn = positions.stream().mapToInt(index -> index % 8).max().getAsInt();
		assertEquals("Cannon components occupy a split or interrupted block: " + positions,
			positions.size(), (maxRow - minRow + 1) * (maxColumn - minColumn + 1));
	}

	private static BankLayoutOptions options(BankPreset preset, GearLayout layout, boolean fillRows)
	{
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		return new BankLayoutOptions(fillRows, defaults.fillHerbloreRows(), defaults.alchPile(),
			Collections.emptyMap(), layout, defaults.potionDoses(), defaults.runeOrder(),
			defaults.teleportOrder(), false);
	}

	private static BankOrganizationPreview build(Fixture fixture, BankPreset preset, BankLayoutOptions options)
	{
		List<BankItemSnapshot> snapshots = new ArrayList<>();
		List<Integer> reverseSource = new ArrayList<>(fixture.items.keySet());
		Collections.reverse(reverseSource);
		for (int itemId : reverseSource)
		{
			snapshots.add(new BankItemSnapshot(itemId, 1, snapshots.size() * 2));
		}
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(snapshots),
			id -> Optional.ofNullable(fixture.items.get(id)), preset,
			id -> Optional.ofNullable(fixture.stats.get(id)), ItemValueSource.NONE,
			CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(preset), options);
	}

	private static BankCategoryPreview combat(BankOrganizationPreview preview, BankPreset preset)
	{
		return preview.getCategories().get(BankLayoutPlan.defaultFor(preset).destinationOf("gear"));
	}

	private static List<Integer> ids(BankCategoryPreview category)
	{
		return category.getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<Integer> bothCannons()
	{
		List<Integer> result = new ArrayList<>(NORMAL);
		result.addAll(ORNAMENTED);
		return result;
	}

	private static int primaryId(int row, int style)
	{
		return 950000 + row * 10 + style;
	}

	private static final class Fixture
	{
		private final Map<Integer, CatalogItem> items = new LinkedHashMap<>();
		private final Map<Integer, GearStats> stats = new LinkedHashMap<>();

		private Fixture add(List<Integer> ids)
		{
			for (int id : ids) items.put(id, CompositeItemCatalog.DEFAULT.describeOrUnknown(id));
			return this;
		}

		private Fixture addPrimaries(GearSlot slot, int row)
		{
			for (int style = 0; style < 4; style++)
			{
				int id = primaryId(row, style);
				items.put(id, gear(id, "Primary " + row + " " + style));
				stats.put(id, new GearStats(slot, style == 0 ? 10 : 0, 0, 0,
					style == 2 ? 10 : 0, style == 1 ? 10 : 0, 0, 0,
					style == 3 ? 10 : 0, 2000));
			}
			return this;
		}

		private Fixture addUtility(int count)
		{
			for (int index = 0; index < count; index++)
			{
				int id = 951000 + index;
				items.put(id, gear(id, "Utility " + index));
			}
			return this;
		}

		private static CatalogItem gear(int id, String name)
		{
			return new CatalogItem(id, name, ItemCategory.GEAR, "gear", Collections.emptySet(), null);
		}
	}
}

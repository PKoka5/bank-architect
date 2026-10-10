package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;

public class AvernicTreadsProgressionTest
{
	private static final int[] TREADS_IDS = {
		31088, 31091, 31092, 31093, 31094, 31095, 31096, 31097
	};
	// These are synthetic runtime assignments, not assertions about live game bonuses.
	// Each hybrid has one assigned style, including the max state.
	private static final GearStyle[] TREADS_STYLES = {
		GearStyle.MELEE, GearStyle.MELEE, GearStyle.RANGED, GearStyle.MAGIC,
		GearStyle.MELEE, GearStyle.MELEE, GearStyle.RANGED, GearStyle.MELEE
	};
	private static final int[] OLDER_BOOT_IDS = {13239, 13237, 13235};
	private static final int[] RING_FILLERS = {2550, 2568, 6731, 6733};

	@Test
	public void everyCanonicalStateSharesTheOlderBootsEndgameBase()
	{
		for (int state = 0; state < TREADS_IDS.length; state++)
		{
			int itemId = TREADS_IDS[state];
			GearStats runtimeStats = footStats(TREADS_STYLES[state], 3);
			assertEquals("without stats, treads state " + itemId, 1000,
				GearItemSorter.score(described(itemId), GearStatsSource.NONE));
			assertEquals("runtime stats add to the shared base for " + itemId,
				1000 + runtimeStats.score(),
				GearItemSorter.score(described(itemId), id -> Optional.of(runtimeStats)));
		}
		for (int itemId : OLDER_BOOT_IDS)
		{
			assertEquals("older endgame boots " + itemId, 1000,
				GearItemSorter.score(described(itemId), GearStatsSource.NONE));
		}
	}

	@Test
	public void strongerFullRuntimeStatsPutEachStateAheadOfOlderBootsInItsStyle()
	{
		for (int state = 0; state < TREADS_IDS.length; state++)
		{
			int itemId = TREADS_IDS[state];
			GearStyle style = TREADS_STYLES[state];
			int olderId = OLDER_BOOT_IDS[style.ordinal()];
			Map<Integer, GearStats> stats = fixtureStats(itemId, style);
			GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
			String context = "treads state " + itemId + ", " + style;

			assertEquals(context + ", upgrade style", style, stats.get(itemId).style());
			assertEquals(context + ", older boots style", style, stats.get(olderId).style());
			assertTrue(context + ", complete upgrade stats", stats.get(itemId).isComparable());
			assertTrue(context + ", complete older boots stats", stats.get(olderId).isComparable());
			assertTrue(context + ", stronger stats retain priority within the same stage",
				GearItemSorter.score(described(itemId), source) > GearItemSorter.score(described(olderId), source));
			assertEquals(context + ", dense order", itemId,
				GearItemSorter.layout(Arrays.asList(described(olderId), described(itemId)), source)
					.get(0).getItemId());
		}
	}

	@Test
	public void bothDefaultPresetsKeepEachTreadsStatePrimaryIncludingZeroQuantityPlaceholders()
	{
		for (int state = 0; state < TREADS_IDS.length; state++)
		{
			int itemId = TREADS_IDS[state];
			GearStyle style = TREADS_STYLES[state];
			Map<Integer, GearStats> stats = fixtureStats(itemId, style);
			GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
			for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
			{
				for (boolean placeholder : Arrays.asList(false, true))
				{
					BankSnapshot bank = fixtureBank(itemId, placeholder);
					BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
					BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank,
						CompositeItemCatalog.DEFAULT, preset, source, ItemValueSource.NONE,
						CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
					List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
					String context = itemId + ", " + preset.getType() + ", placeholder=" + placeholder;

					assertEquals(context + ", complete feet setup row", GearItemSorter.GRID_COLUMNS, combat.size());
					for (int column = 0; column < OLDER_BOOT_IDS.length; column++)
					{
						int expectedId = column == style.ordinal() ? itemId : OLDER_BOOT_IDS[column];
						assertEquals(context + ", style column " + column, expectedId, combat.get(column).getItemId());
					}
					BankPreviewItem primary = combat.get(style.ordinal());
					assertEquals(context + ", placeholder state", placeholder, primary.isPlaceholder());
					assertEquals(context + ", quantity", placeholder ? 0 : 1, primary.getQuantity());
					assertEquals(context + ", every bank entry is preserved", bank.getItems().size(), combat.size());
					assertEquals(context + ", item IDs are preserved",
						bank.getItems().stream().map(BankItemSnapshot::getItemId).collect(Collectors.toSet()),
						combat.stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet()));
					assertEquals(context + ", each item appears once", combat.size(),
						new HashSet<>(combat.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList())).size());
					assertTrue(context + ", no invented empty cells", combat.stream().noneMatch(BankPreviewItem::isBlank));
				}
			}
		}
	}

	private static Map<Integer, GearStats> fixtureStats(int treadsId, GearStyle style)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		stats.put(treadsId, footStats(style, 3));
		for (int column = 0; column < OLDER_BOOT_IDS.length; column++)
		{
			stats.put(OLDER_BOOT_IDS[column], footStats(GearStyle.values()[column], 2));
		}
		for (int ringId : RING_FILLERS)
		{
			stats.put(ringId, new GearStats(GearSlot.RING, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		}
		return stats;
	}

	// Complete synthetic vectors: only the assigned style's offence changes.
	private static GearStats footStats(GearStyle style, int offence)
	{
		int melee = style == GearStyle.MELEE ? offence : 0;
		int ranged = style == GearStyle.RANGED ? offence : 0;
		int magic = style == GearStyle.MAGIC ? offence : 0;
		return new GearStats(GearSlot.FEET, melee, melee, melee, magic, ranged, melee, ranged, 0,
			10, 10, 10, 10, 10, 0, 0);
	}

	private static BankSnapshot fixtureBank(int treadsId, boolean placeholder)
	{
		List<BankItemSnapshot> entries = new ArrayList<>();
		for (int itemId : OLDER_BOOT_IDS)
		{
			entries.add(new BankItemSnapshot(itemId, 1, entries.size()));
		}
		for (int itemId : RING_FILLERS)
		{
			entries.add(new BankItemSnapshot(itemId, 1, entries.size()));
		}
		entries.add(new BankItemSnapshot(treadsId, placeholder ? 0 : 1, entries.size(), placeholder));
		return new BankSnapshot(entries);
	}

	private static BankPreviewItem described(int itemId)
	{
		return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(itemId), 1);
	}
}

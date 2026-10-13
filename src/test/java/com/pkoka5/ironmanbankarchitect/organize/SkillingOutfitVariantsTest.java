package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Real item identities cover recoloured pieces without inventing missing outfit parts. */
@RunWith(Parameterized.class)
public class SkillingOutfitVariantsTest
{
	private static final int[] FILLERS = {305, 307, 309, 301, 303, 590, 233, 5340, 5325,
		7409, 5341, 5343, 9625, 8794, 1785, 1733, 10006, 10008, 10010, 10150, 10031,
		10029, 9065, 596, 1887, 775, 946, 9419, 2871, 4446, 4600, 4, 1595, 9434,
		11065, 1599, 1597, 1592, 5523, 4161, 4155, 6696, 7159, 7053, 4156, 3337,
		4162, 6720, 31398, 1925, 1929, 1935, 1937, 1931, 5376};
	private static final int[][] GRACEFUL = {
		{11850, 11852, 11854, 11856, 11858, 11860},
		{13579, 13581, 13583, 13585, 13587, 13589},
		{13591, 13593, 13595, 13597, 13599, 13601},
		{13603, 13605, 13607, 13609, 13611, 13613},
		{13615, 13617, 13619, 13621, 13623, 13625},
		{13627, 13629, 13631, 13633, 13635, 13637},
		{13667, 13669, 13671, 13673, 13675, 13677},
		{24743, 24746, 24749, 24752, 24755, 24758},
		{25069, 25072, 25075, 25078, 25081, 25084},
		{27444, 27447, 27450, 27453, 27456, 27459},
		{30045, 30048, 30051, 30054, 30057, 30060},
		{21061, 21064, 21067, 21070, 21073, 21076}
	};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public SkillingOutfitVariantsTest(BankPreset preset)
	{
		this.preset = preset;
		this.plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void prospectorRecolouredBootCompletesTheOwnedOriginalOutfit()
	{
		assertOwnedColumn(new int[]{12013, 12014, 12015, 29478});
		assertOwnedColumn(new int[]{25549, 12014, 29476, 25555});
		assertOwnedColumn(new int[]{29472, 25551, 12015, 12016});
	}

	@Test
	public void eachCompleteProspectorFinishRetainsItsOwnColumn()
	{
		int[][] outfits = {{12013, 12014, 12015, 12016}, {25549, 25551, 25553, 25555},
			{29472, 29474, 29476, 29478}};
		List<BankItemSnapshot> bank = bankWithFillers(outfits);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		for (int[] outfit : outfits) assertVertical(tools, outfit);
		assertConserved(bank, preview);
	}

	@Test
	public void duplicateBootVariantsNeverExtendOneProspectorColumnToFivePieces()
	{
		int[] original = {12013, 12014, 12015, 12016};
		List<BankItemSnapshot> bank = bankWithFillers(original, new int[]{29478});
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, original);
		assertTrue("an extra pair of boots is not a fifth slot in the same outfit",
			tools.indexOf(29478) != tools.indexOf(12013) + 32);
		assertConserved(bank, preview);
	}

	@Test
	public void everyReviewedGracefulColourUsesItsOwnCompleteColumn()
	{
		for (int[] outfit : GRACEFUL) assertOwnedColumn(outfit);
	}

	@Test
	public void completeGracefulColoursLeadASeparateMixedColourRemainderColumn()
	{
		int[] mixed = {21061, 21064, 21067, 25078, 25081, 25084};
		List<BankItemSnapshot> bank = bankWithFillers(GRACEFUL[0], GRACEFUL[1], mixed);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, GRACEFUL[0]);
		assertVertical(tools, GRACEFUL[1]);
		assertVertical(tools, mixed);
		assertTrue(tools.indexOf(GRACEFUL[0][0]) < tools.indexOf(mixed[0]));
		assertTrue(tools.indexOf(GRACEFUL[1][0]) < tools.indexOf(mixed[0]));
		assertConserved(bank, preview);
	}

	@Test
	public void forestryAndSpiritAnglerPartsCanCompleteCompatibleOriginalOutfits()
	{
		assertOwnedColumn(new int[]{10941, 28169, 10940, 28175});
		assertOwnedColumn(new int[]{28173, 10939, 28171, 10933});
		assertOwnedColumn(new int[]{28173, 28169, 28171, 28175});
		assertOwnedColumn(new int[]{13258, 25594, 13260, 25598});
		assertOwnedColumn(new int[]{25592, 13259, 25596, 13261});
	}

	@Test
	public void farmerFormsAreTwoFourPieceColumnsInsteadOfOneEightPieceColumn()
	{
		int[] first = {13646, 13642, 13640, 13644};
		int[] second = {13647, 13643, 13641, 13645};
		List<BankItemSnapshot> bank = bankWithFillers(first, second);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, first);
		assertVertical(tools, second);
		assertConserved(bank, preview);
	}

	@Test
	public void smithsIceGlovesSubstituteTheGlovePieceWithoutDuplicatingItsSlot()
	{
		assertOwnedColumn(new int[]{27023, 27025, 27027, 27031});
		int[] original = {27023, 27025, 27027, 27029};
		List<BankItemSnapshot> bank = bankWithFillers(original, new int[]{27031});
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, original);
		assertTrue(tools.indexOf(27031) != tools.indexOf(27023) + 32);
		assertConserved(bank, preview);
	}

	@Test
	public void eyeColourMixIncludesSharedBootsAndOneLanternOnlyOnce()
	{
		assertOwnedColumn(new int[]{26858, 26866, 26874, 26856, 26822});
		int[] primary = {26850, 26852, 26854, 26856, 26822};
		int[][] colours = {{26858, 26860, 26862}, {26864, 26866, 26868}, {26870, 26872, 26874}};
		List<BankItemSnapshot> bank = bankWithFillers(primary, colours[0], colours[1], colours[2]);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, primary);
		for (int[] colour : colours) assertVertical(tools, colour);
		assertConserved(bank, preview);
	}

	@Test
	public void moreThanEightCompleteOutfitsStayIntactWithoutDuplicatingAnyParts()
	{
		List<BankItemSnapshot> bank = bankWithFillers(GRACEFUL);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		for (int[] outfit : GRACEFUL) assertVertical(tools, outfit);
		assertConserved(bank, preview);
	}

	@Test
	public void mixedOutfitColumnsSurviveShufflesAndWithdrawnPlaceholders()
	{
		int[][] outfits = {{12013, 12014, 12015, 29478}, {10941, 28169, 10940, 28175},
			{26858, 26866, 26874, 26856, 26822}, {27023, 27025, 27027, 27031}};
		List<BankItemSnapshot> original = bankWithFillers(outfits);
		List<Integer> expected = tools(build(original, BlueprintItemOrders.EMPTY));
		for (int trial = 0; trial < 4; trial++)
		{
			List<BankItemSnapshot> shuffled = new ArrayList<>(original);
			Collections.shuffle(shuffled, new Random(20261013L + trial));
			List<BankItemSnapshot> changed = new ArrayList<>();
			for (BankItemSnapshot item : shuffled)
			{
				boolean placeholder = trial % 2 == 1 && item.getItemId() % 2 == 0;
				changed.add(new BankItemSnapshot(item.getItemId(), placeholder ? 0 : 1,
					changed.size(), placeholder));
			}
			BankOrganizationPreview preview = build(changed, BlueprintItemOrders.EMPTY);
			assertEquals("source position and withdrawal do not change outfit ownership", expected, tools(preview));
			for (int[] outfit : outfits) assertVertical(tools(preview), outfit);
			assertConserved(changed, preview);
		}
	}

	@Test
	public void tinyMixedColourBanksRemainCompactWhenNoFullVerticalColumnCanFit()
	{
		for (int[] pieces : new int[][]{{12013, 29478}, {11850, 13583, 21076},
			{10941, 28175}, {26858, 26866, 26856}, {27023, 27031}})
		{
			List<BankItemSnapshot> bank = snapshots(pieces);
			bank.add(new BankItemSnapshot(305, 1, bank.size()));
			bank.add(new BankItemSnapshot(590, 1, bank.size()));
			BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
			List<Integer> tools = tools(preview);
			int first = tools.indexOf(pieces[0]);
			for (int i = 0; i < pieces.length; i++)
				assertEquals("compact compatible pieces " + Arrays.toString(pieces) + " in " + tools,
					first + i, tools.indexOf(pieces[i]));
			assertConserved(bank, preview);
		}
	}

	@Test
	public void compactOutfitsStayConsecutiveBesideTheColumnsThatFit()
	{
		int[][] outfits = {{13258,13259,13260,13261}, {10941,10939,10940,10933},
			{12013,12014,12015,29478}, {20708,20704,20706,20710}};
		List<BankItemSnapshot> bank = new ArrayList<>();
		for (int[] outfit : outfits) for (BankItemSnapshot item : snapshots(outfit))
			bank.add(new BankItemSnapshot(item.getItemId(),1,bank.size()));
		for (int index = 0; index < 10; index++) bank.add(new BankItemSnapshot(FILLERS[index],1,bank.size()));
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		List<Integer> tools = tools(preview);
		assertVertical(tools, outfits[0]);
		assertVertical(tools, outfits[1]);
		for (int group = 2; group < outfits.length; group++)
			for (int piece = 0; piece < outfits[group].length; piece++)
				assertEquals("compact fallback cannot be split by another outfit column",
					tools.indexOf(outfits[group][0]) + piece, tools.indexOf(outfits[group][piece]));
		assertConserved(bank, preview);
	}

	@Test
	public void savedPhysicalItemOrderTakesPrecedenceOverAutomaticVariantColumns()
	{
		List<BankItemSnapshot> bank = bankWithFillers(new int[]{12013, 12014, 12015, 29478},
			new int[]{10941, 28169, 10940, 28175});
		List<Integer> automatic = tools(build(bank, BlueprintItemOrders.EMPTY));
		List<Integer> manual = new ArrayList<>(automatic);
		Collections.reverse(manual);
		BlueprintItemOrders orders = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("tools"), manual).serialize());
		BankOrganizationPreview preview = build(bank, orders);
		assertEquals(manual, tools(preview));
		assertTrue(preview.getCategories().get(plan.destinationOf("tools")).hasManualOrder());
		assertConserved(bank, preview);
	}

	private void assertOwnedColumn(int[] outfit)
	{
		List<BankItemSnapshot> bank = bankWithFillers(outfit);
		BankOrganizationPreview preview = build(bank, BlueprintItemOrders.EMPTY);
		assertVertical(tools(preview), outfit);
		assertConserved(bank, preview);
	}

	private BankOrganizationPreview build(List<BankItemSnapshot> bank, BlueprintItemOrders orders)
	{
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		BankLayoutOptions options = new BankLayoutOptions(defaults.fillGearRows(), defaults.fillHerbloreRows(),
			defaults.alchPile(), Collections.emptyMap(), defaults.gearLayout(), defaults.potionDoses(),
			defaults.runeOrder(), defaults.teleportOrder(), false).withItemOrders(orders);
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(bank), CompositeItemCatalog.DEFAULT,
			preset, GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE, plan, options);
	}

	private List<Integer> tools(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("tools")).getItems().stream()
			.map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static List<BankItemSnapshot> bankWithFillers(int[]... outfits)
	{
		LinkedHashSet<Integer> ids = new LinkedHashSet<>();
		for (int[] outfit : outfits) for (int id : outfit) ids.add(id);
		for (int id : FILLERS) ids.add(id);
		return snapshots(ids.stream().mapToInt(Integer::intValue).toArray());
	}

	private static List<BankItemSnapshot> snapshots(int[] ids)
	{
		List<BankItemSnapshot> bank = new ArrayList<>();
		for (int id : ids)
		{
			assertTrue("real catalogue entry " + id, CompositeItemCatalog.DEFAULT.findById(id).isPresent());
			bank.add(new BankItemSnapshot(id, 1, bank.size()));
		}
		return bank;
	}

	private static void assertVertical(List<Integer> tools, int[] outfit)
	{
		int first = tools.indexOf(outfit[0]);
		assertTrue("outfit head is in Tools: " + outfit[0], first >= 0);
		for (int row = 0; row < outfit.length; row++)
			assertEquals("owned piece " + outfit[row] + " belongs below " + outfit[0],
				first + row * 8, tools.indexOf(outfit[row]));
	}

	private static void assertConserved(List<BankItemSnapshot> bank, BankOrganizationPreview preview)
	{
		Map<Integer, BankItemSnapshot> originals = new LinkedHashMap<>();
		for (BankItemSnapshot item : bank) originals.put(item.getItemId(), item);
		assertEquals(bank.size(), preview.getPlannedItems().size());
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertFalse("no synthetic outfit parts or spacers", item.isBlank());
			BankItemSnapshot original = originals.remove(item.getItemId());
			assertTrue("each physical item appears once", original != null);
			assertEquals(original.getQuantity(), item.getQuantity());
			assertEquals(original.isPlaceholder(), item.isPlaceholder());
		}
		assertTrue("all original bank entries remain", originals.isEmpty());
	}
}

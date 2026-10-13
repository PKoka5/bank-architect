package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/** The second of two reported Main exports has a complete Combat section; the first is truncated. */
public class TwoMainCombatLayoutRegressionTest
{
	private static final int[] SHAYZIEN = {13379,13381,13380,13377,13378};
	private static final int[] ELITE_BLACK = {29560,29562,29564};
	private static final int[][] SECONDARY_FAMILIES = {
		{29047,28997}, SHAYZIEN, ELITE_BLACK, {1161,1091},
		{27238,27241}, {29566,29568,29570}, {25413,25404,25416},
		{9096,9101,9102,9097,9098,9099,9100,9104}, {5574,5575,5576}
	};
	private static final int[][] PRIMARY_ARMOUR = {
		{30750,30753,30756}, {27705,27697,27701}, {26241,26243,26245}
	};

	@Test
	public void completeReportedCombatKeepsItsRealItemsAndNewlyReviewedSetsVertical()
	{
		Fixture fixture = loadFixture();
		assertEquals("the second export contains all 194 Combat entries",194,fixture.bank.size());
		BankOrganizationPreview preview = build(fixture,fixture.bank);
		List<Integer> combat = ids(combat(preview));
		assertPreserved(fixture.bank,preview);
		for (int style = 0; style < PRIMARY_ARMOUR.length; style++)
			for (int slot = 0; slot < PRIMARY_ARMOUR[style].length; slot++)
				assertEquals("best owned armour retains its style column",slot * 8 + style,
					combat.indexOf(PRIMARY_ARMOUR[style][slot]));
		assertVertical("the complete Shayzien outfit",combat,SHAYZIEN);
		assertVertical("the complete Elite black outfit",combat,ELITE_BLACK);
		List<Integer> toolIds = ids(preview.getCategories().get(BankLayoutPlan.defaultFor(BankPresets.MAIN).destinationOf("tools")).getItems());
		int cannon = toolIds.indexOf(6);
		assertTrue("all four cannon parts fit on one bank row",cannon >= 0 && cannon % 8 <= 4);
		assertEquals(Arrays.asList(6,8,10,12),toolIds.subList(cannon,cannon + 4));
	}

	@Test
	public void physicalSecondaryFamilyOrderMatchesTheReviewedStyleAndProgressionOrder()
	{
		Fixture fixture = loadFixture();
		GearStatsSource stats = controlledStats();
		List<BankPreviewItem> logical = fixture.bank.stream().map(bankItem ->
			new BankPreviewItem(fixture.catalog.get(bankItem.getItemId()),bankItem.getQuantity(),
				bankItem.isPlaceholder())).collect(Collectors.toList());
		List<Integer> orderedTail = ids(GearItemSorter.bySet(GearItemSorter.plan(logical,stats).getTail(),stats));
		List<int[]> expectedFamilies = new ArrayList<>();
		for (int[] family : SECONDARY_FAMILIES)
		{
			// Best pieces keep their front cells; only the owned remainder is one secondary column.
			int[] remainder = Arrays.stream(family).filter(orderedTail::contains).toArray();
			assertTrue("the fixture must retain a meaningful secondary family: " + family[0],remainder.length >= 2);
			expectedFamilies.add(remainder);
		}
		expectedFamilies.sort((left,right) -> Integer.compare(orderedTail.indexOf(left[0]),orderedTail.indexOf(right[0])));
		List<Integer> physical = ids(combat(build(fixture,fixture.bank)));
		int previous = -1;
		for (int[] family : expectedFamilies)
		{
			assertTrue("the fixture keeps this family outside the BIS cells: " + family[0],
				orderedTail.contains(family[0]));
			assertVertical("secondary family " + family[0],physical,family);
			int start = physical.indexOf(family[0]);
			assertTrue("physical family " + family[0] + " must follow the previous style/progression family",
				start > previous);
			previous = start;
		}
	}

	@Test
	public void shuffledInputAndWithdrawnPrimaryPlaceholdersKeepTheSamePhysicalTargets()
	{
		Fixture fixture = loadFixture();
		List<Integer> expected = ids(combat(build(fixture,fixture.bank)));
		Random random = new Random(20261012L);
		for (int trial = 0; trial < 3; trial++)
		{
			List<BankItemSnapshot> shuffled = new ArrayList<>(fixture.bank);
			Collections.shuffle(shuffled,random);
			shuffled = withSourceSlots(shuffled,Collections.emptySet());
			BankOrganizationPreview preview = build(fixture,shuffled);
			assertEquals("source bank order cannot change the intended Combat layout",expected,ids(combat(preview)));
			assertPreserved(shuffled,preview);
		}
		Set<Integer> withdrawn = new HashSet<>();
		for (int[] family : PRIMARY_ARMOUR) for (int id : family) withdrawn.add(id);
		List<BankItemSnapshot> placeholders = withSourceSlots(fixture.bank,withdrawn);
		BankOrganizationPreview preview = build(fixture,placeholders);
		assertEquals("withdrawing gear keeps its planned physical locations",expected,ids(combat(preview)));
		assertPreserved(placeholders,preview);
	}

	@Test
	public void looseAlternativesGroupByCombatStyleBeforeEquipmentSlot()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		List<BankPreviewItem> input = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		GearSlot[] slots = {GearSlot.HEAD,GearSlot.WEAPON,GearSlot.RING};
		for (int style = 0; style < 3; style++)
			for (GearSlot slot : slots)
			{
				int id = 991000 + expected.size();
				expected.add(id);
				stats.put(id,styled(slot,GearStyle.values()[style],10));
				input.add(new BankPreviewItem(new CatalogItem(id,"Loose " + id,ItemCategory.GEAR,
					slot.name().toLowerCase(java.util.Locale.ROOT),Collections.emptySet(),null),1));
			}
		Collections.reverse(input);
		assertEquals("Melee, Ranged and Magic keep their own loose accessories and weapons together",expected,
			ids(GearItemSorter.dense(input,id -> Optional.ofNullable(stats.get(id)))));
	}

	private static BankOrganizationPreview build(Fixture fixture,List<BankItemSnapshot> bank)
	{
		ItemCatalog catalog = id -> Optional.ofNullable(fixture.catalog.get(id));
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(bank),catalog,BankPresets.MAIN,
			controlledStats(),ItemValueSource.NONE,CategoryOverrideSource.NONE,
			BankLayoutPlan.defaultFor(BankPresets.MAIN),BankLayoutOptions.defaultFor(BankPresets.MAIN));
	}

	private static List<BankPreviewItem> combat(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(BankLayoutPlan.defaultFor(BankPresets.MAIN).destinationOf("gear")).getItems();
	}

	private static List<BankItemSnapshot> withSourceSlots(List<BankItemSnapshot> source,Set<Integer> withdrawn)
	{
		List<BankItemSnapshot> result = new ArrayList<>();
		for (BankItemSnapshot item : source)
		{
			boolean placeholder = item.isPlaceholder() || withdrawn.contains(item.getItemId());
			result.add(new BankItemSnapshot(item.getItemId(),placeholder ? 0 : 1,result.size(),placeholder));
		}
		return result;
	}

	private static void assertVertical(String context,List<Integer> physical,int[] family)
	{
		int start = physical.indexOf(family[0]);
		assertTrue(context + " must be present",start >= 0);
		int minRow = start / 8, maxRow = minRow, minCol = start % 8, maxCol = minCol;
		for (int id : family) { int position = physical.indexOf(id); minRow=Math.min(minRow,position/8);maxRow=Math.max(maxRow,position/8);minCol=Math.min(minCol,position%8);maxCol=Math.max(maxCol,position%8); }
		int height=maxRow-minRow+1,width=maxCol-minCol+1;
		boolean dense=true,columns=true,rows=true;
		for (int i=0;i<family.length;i++) { int position=physical.indexOf(family[i]);dense &= position==start+i;columns &= position==start+i%height*8+i/height;rows &= position==start+i/width*8+i%width; }
		assertTrue(context + " stays in canonical run or filled rectangle",dense || height*width==family.length && (columns || rows));
	}

	private static void assertPreserved(List<BankItemSnapshot> source,BankOrganizationPreview preview)
	{
		List<BankPreviewItem> output = preview.getPlannedItems();
		assertEquals("every real input entry occurs exactly once",source.size(),output.size());
		Map<Integer,BankPreviewItem> byId = new LinkedHashMap<>();
		for (BankPreviewItem item : output)
		{
			assertFalse("manual target layouts cannot invent blank fillers",item.isBlank());
			assertTrue("each item occurs once: " + item.getItemId(),byId.put(item.getItemId(),item) == null);
		}
		for (BankItemSnapshot item : source)
		{
			BankPreviewItem actual = byId.get(item.getItemId());
			assertTrue("missing item " + item.getItemId(),actual != null);
			assertEquals("quantity " + item.getItemId(),item.getQuantity(),actual.getQuantity());
			assertEquals("placeholder " + item.getItemId(),item.isPlaceholder(),actual.isPlaceholder());
		}
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	// Tier-only synthetic role vectors isolate layout policy; these are not live game bonuses or Alch evidence.
	private static GearStatsSource controlledStats()
	{
		Map<Integer,GearStats> stats = new LinkedHashMap<>();
		GearSlot[] armourSlots = {GearSlot.HEAD,GearSlot.BODY,GearSlot.LEGS};
		for (int style = 0; style < PRIMARY_ARMOUR.length; style++)
			for (int slot = 0; slot < armourSlots.length; slot++)
				stats.put(PRIMARY_ARMOUR[style][slot],styled(armourSlots[slot],GearStyle.values()[style],500));
		GearSlot[] outfitSlots = {GearSlot.HEAD,GearSlot.BODY,GearSlot.LEGS,GearSlot.HANDS,GearSlot.FEET};
		for (int slot = 0; slot < SHAYZIEN.length; slot++)
			stats.put(SHAYZIEN[slot],styled(outfitSlots[slot],GearStyle.MELEE,5));
		for (int slot = 0; slot < ELITE_BLACK.length; slot++)
			stats.put(ELITE_BLACK[slot],styled(armourSlots[slot],GearStyle.MELEE,10));
		stats.put(29047,styled(GearSlot.HEAD,GearStyle.MELEE,20));
		stats.put(27238,styled(GearSlot.BODY,GearStyle.RANGED,40));
		stats.put(27241,styled(GearSlot.LEGS,GearStyle.RANGED,40));
		for (int slot = 0; slot < armourSlots.length; slot++)
		{
			stats.put(new int[]{29566,29568,29570}[slot],styled(armourSlots[slot],GearStyle.MAGIC,20));
			stats.put(new int[]{25413,25404,25416}[slot],styled(armourSlots[slot],GearStyle.MAGIC,10));
		}
		stats.put(28810,styled(GearSlot.WEAPON,GearStyle.MELEE,500));
		stats.put(25886,styled(GearSlot.WEAPON,GearStyle.RANGED,500));
		stats.put(22323,styled(GearSlot.WEAPON,GearStyle.MAGIC,500));
		stats.put(7462,styled(GearSlot.HANDS,GearStyle.MELEE,500));
		stats.put(26235,styled(GearSlot.HANDS,GearStyle.RANGED,500));
		stats.put(31106,styled(GearSlot.HANDS,GearStyle.MAGIC,500));
		stats.put(28945,styled(GearSlot.FEET,GearStyle.MELEE,500));
		stats.put(29806,styled(GearSlot.FEET,GearStyle.RANGED,500));
		stats.put(25410,styled(GearSlot.FEET,GearStyle.MAGIC,500));
		return id -> Optional.ofNullable(stats.get(id));
	}

	private static GearStats styled(GearSlot slot,GearStyle style,int strength)
	{
		int melee = style == GearStyle.MELEE ? strength : 0;
		int ranged = style == GearStyle.RANGED ? strength : 0;
		int magic = style == GearStyle.MAGIC ? strength : 0;
		int prayer = style == GearStyle.PRAYER ? strength : 0;
		return new GearStats(slot,melee,melee,melee,magic,ranged,melee,ranged,prayer,50);
	}

	private static Fixture loadFixture()
	{
		Fixture fixture = new Fixture();
		InputStream stream = TwoMainCombatLayoutRegressionTest.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/organize/main-bank-combat-2026-10-12.tsv");
		assertTrue("complete Main Combat fixture must be available",stream != null);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] columns = line.split("\t",-1);
				assertEquals("fixture fields",5,columns.length);
				int id = Integer.parseInt(columns[0]);
				boolean placeholder = Boolean.parseBoolean(columns[1]);
				CatalogItem catalog = new CatalogItem(id,columns[4],ItemCategory.valueOf(columns[2]),
					columns[3],Collections.emptySet(),null);
				assertTrue("fixture IDs must be unique",fixture.catalog.put(id,catalog) == null);
				fixture.bank.add(new BankItemSnapshot(id,placeholder ? 0 : 1,fixture.bank.size(),placeholder));
			}
		}
		catch (Exception exception)
		{
			throw new AssertionError("could not read complete Combat fixture",exception);
		}
		return fixture;
	}

	private static final class Fixture
	{
		private final List<BankItemSnapshot> bank = new ArrayList<>();
		private final Map<Integer,CatalogItem> catalog = new LinkedHashMap<>();
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CannonSlayerToolsRegressionTest
{
	private static final List<Integer> NORMAL = Arrays.asList(6, 8, 10, 12);
	private static final List<Integer> ORNAMENTED = Arrays.asList(26520, 26522, 26524, 26526);
	private static final List<Integer> SLAYER = Arrays.asList(4161, 4155, 6696, 7159, 7053,
		4156, 3337, 4162, 6720, 31398);
	private static final List<Integer> LOOSE_TOOLS = Arrays.asList(305, 307, 309, 301, 303, 590, 233);
	private static final int[][] OUTFITS = {
		{13258, 13259, 13260, 13261}, {10941, 10939, 10940, 10933}, {12013, 12014, 12015, 29478},
		{20708, 20704, 20706, 20710}, {5554, 5553, 5555, 5556, 5557}
	};
	private final BankPreset preset;
	private final BankLayoutPlan plan;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.MAIN}, new Object[] {BankPresets.IRONMAN});
	}

	public CannonSlayerToolsRegressionTest(BankPreset preset)
	{
		this.preset = preset;
		this.plan = BankLayoutPlan.defaultFor(preset);
	}

	@Test
	public void reportedToolsBankKeepsNativeOutfitColumnsAndPlacesCannonBesideSlayerTools() throws Exception
	{
		Fixture fixture = reportedFixture();
		assertEquals("normalized reported Tools entries", 161, fixture.bank.size());
		BankOrganizationPreview preview = build(fixture, BlueprintItemOrders.EMPTY, BlockArrangements.EMPTY);
		List<Integer> tools = ids(tools(preview));
		for (int column = 0; column < OUTFITS.length; column++)
			for (int row = 0; row < OUTFITS[column].length; row++)
				assertEquals("outfit " + column + " row " + row + " keeps its native column",
					Integer.valueOf(OUTFITS[column][row]), tools.get(row * 8 + column));
		assertTrue("cannon does not lead the Tools tab", tools.indexOf(6) > 36);
		assertCompact(tools, NORMAL);
		int firstSlayer = SLAYER.stream().mapToInt(tools::indexOf).min().getAsInt();
		int lastCannon = NORMAL.stream().mapToInt(tools::indexOf).max().getAsInt();
		if (preset.getType() == BankPresetType.IRONMAN)
			assertEquals("reported Ironman boundary fits cannon immediately before Slayer", lastCannon + 1, firstSlayer);
		else assertTrue("Main's row-edge boundary keeps cannon in the Slayer cluster", Math.abs(tools.indexOf(6) - firstSlayer) <= 4);
		assertPreserved(fixture, preview);
	}

	@Test
	public void reportedOutfitColumnsAndCannonLocationSurviveSourceShufflesAndWithdrawals() throws Exception
	{
		Fixture fixture = reportedFixture();
		List<Integer> expected = ids(tools(build(fixture, BlueprintItemOrders.EMPTY, BlockArrangements.EMPTY)));
		for (int trial = 0; trial < 5; trial++)
		{
			List<BankItemSnapshot> changed = new ArrayList<>(fixture.bank);
			Collections.shuffle(changed, new Random(20261012L + trial));
			List<BankItemSnapshot> entries = new ArrayList<>();
			for (BankItemSnapshot item : changed)
			{
				boolean placeholder = item.isPlaceholder() || trial % 2 == 1 && NORMAL.contains(item.getItemId());
				entries.add(new BankItemSnapshot(item.getItemId(), placeholder ? 0 : 1, entries.size(), placeholder));
			}
			Fixture variation = new Fixture(entries, fixture.catalog);
			BankOrganizationPreview preview = build(variation, BlueprintItemOrders.EMPTY, BlockArrangements.EMPTY);
			assertEquals("source shuffle and withdrawn cannon do not rearrange the Tools blueprint",
				expected, ids(tools(preview)));
			assertPreserved(variation, preview);
		}
	}

	@Test
	public void sparseSlayerBoundariesKeepBothFinishesAndPartialComponentsCompact()
	{
		List<List<Integer>> finishes = Arrays.asList(NORMAL, ORNAMENTED, bothFinishes(),
			Arrays.asList(6, 12), Arrays.asList(8, 10, 12), Arrays.asList(26520, 26524),
			Arrays.asList(26522, 26524, 26526), Arrays.asList(6, 8, 10, 26526),
			Arrays.asList(6, 26522, 26524, 26526));
		for (int boundary = 0; boundary <= LOOSE_TOOLS.size(); boundary++)
			for (List<Integer> parts : finishes)
			{
				List<Integer> source = new ArrayList<>(LOOSE_TOOLS.subList(0, boundary));
				source.addAll(SLAYER);
				source.addAll(parts);
				Fixture fixture = fixture(source);
				BankOrganizationPreview preview = build(fixture, BlueprintItemOrders.EMPTY, BlockArrangements.EMPTY);
				List<Integer> tools = ids(tools(preview));
				assertCompact(tools, NORMAL);
				assertCompact(tools, ORNAMENTED);
				int firstSlayer = SLAYER.stream().mapToInt(tools::indexOf).min().getAsInt();
				int firstCannon = parts.stream().mapToInt(tools::indexOf).min().getAsInt();
				int lastCannon = parts.stream().mapToInt(tools::indexOf).max().getAsInt();
				assertTrue("cannon stays near the Slayer tools at boundary " + boundary,
					Math.min(Math.abs(firstCannon - firstSlayer), Math.abs(lastCannon - firstSlayer)) <= 8);
				assertPreserved(fixture, preview);
			}
	}

	@Test
	public void explicitCannonBlockOrderIsRetainedAndItsDescriptorIncludesAllParts()
	{
		Fixture fixture = fixture(Arrays.asList(305, 307, 590, 4161, 4162, 6, 8, 10, 12));
		BlockArrangements blocks = BlockArrangements.parse(BlockArrangements.EMPTY.withTag("tools",
			Arrays.asList("item:4162", "set:gear.dwarf-cannon", "item:305")).serialize());
		BankOrganizationPreview preview = build(fixture, BlueprintItemOrders.EMPTY, blocks);
		List<Integer> tools = ids(tools(preview));
		assertTrue("the player's preceding block stays before cannon", tools.indexOf(4162) < tools.indexOf(6));
		assertTrue("the player's following block stays after cannon", tools.indexOf(12) < tools.indexOf(305));
		List<BankBlockDescriptor> descriptors = preview.getBlockDescriptors().get("tools");
		BankBlockDescriptor cannon = descriptors.stream().filter(block -> "set:gear.dwarf-cannon".equals(block.getBlockKey()))
			.findFirst().orElseThrow(AssertionError::new);
		assertEquals("all four parts remain available to the block editor", 4, cannon.getMemberCount());
		assertPreserved(fixture, preview);
	}

	@Test
	public void personalItemOrderMayPlaceCannonBeforeOutfitsAndSlayerTools() throws Exception
	{
		Fixture fixture = reportedFixture();
		List<Integer> manual = fixture.bank.stream().map(BankItemSnapshot::getItemId).collect(Collectors.toList());
		BlueprintItemOrders orders = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
			.withTab(plan.destinationOf("tools"), manual).serialize());
		BankOrganizationPreview preview = build(fixture, orders, BlockArrangements.EMPTY);
		assertEquals("manual order wins over automatic Slayer placement", manual, ids(tools(preview)));
		assertTrue(tools(preview).hasManualOrder());
		assertPreserved(fixture, preview);
	}

	private BankCategoryPreview tools(BankOrganizationPreview preview)
	{
		return preview.getCategories().get(plan.destinationOf("tools"));
	}

	private BankOrganizationPreview build(Fixture fixture, BlueprintItemOrders orders, BlockArrangements blocks)
	{
		BankLayoutOptions defaults = BankLayoutOptions.defaultFor(preset);
		BankLayoutOptions options = new BankLayoutOptions(defaults.fillGearRows(), defaults.fillHerbloreRows(),
			defaults.alchPile(), Collections.emptyMap(), defaults.gearLayout(), defaults.potionDoses(),
			defaults.runeOrder(), defaults.teleportOrder(), false).withItemOrders(orders).withBlockArrangements(blocks);
		return BankOrganizationPreviewBuilder.build(new BankSnapshot(fixture.bank),
			id -> Optional.ofNullable(fixture.catalog.get(id)), preset, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, plan, options);
	}

	private static Fixture fixture(List<Integer> ids)
	{
		List<BankItemSnapshot> bank = new ArrayList<>();
		Map<Integer, CatalogItem> catalog = new LinkedHashMap<>();
		for (int id : ids)
		{
			bank.add(new BankItemSnapshot(id, 1, bank.size()));
			catalog.put(id, CompositeItemCatalog.DEFAULT.describeOrUnknown(id));
		}
		return new Fixture(bank, catalog);
	}

	private static Fixture reportedFixture() throws Exception
	{
		List<BankItemSnapshot> bank = new ArrayList<>();
		Map<Integer, CatalogItem> catalog = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
			CannonSlayerToolsRegressionTest.class.getResourceAsStream("reported-tools-161.tsv"), StandardCharsets.UTF_8)))
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				String[] fields = line.split("\t", -1);
				int id = Integer.parseInt(fields[0]), quantity = Integer.parseInt(fields[4]);
				catalog.put(id, new CatalogItem(id, fields[1], ItemCategory.valueOf(fields[2]), fields[3],
					Collections.emptySet(), null));
				bank.add(new BankItemSnapshot(id, quantity, bank.size(), quantity == 0));
			}
		}
		return new Fixture(bank, catalog);
	}

	private static List<Integer> bothFinishes()
	{
		List<Integer> parts = new ArrayList<>(NORMAL);
		parts.addAll(ORNAMENTED);
		return parts;
	}

	private static List<Integer> ids(BankCategoryPreview category)
	{
		return category.getItems().stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}

	private static void assertCompact(List<Integer> target, List<Integer> family)
	{
		List<Integer> positions = family.stream().filter(target::contains).map(target::indexOf).collect(Collectors.toList());
		if (positions.size() < 2) return;
		int minRow = positions.stream().mapToInt(index -> index / 8).min().getAsInt();
		int maxRow = positions.stream().mapToInt(index -> index / 8).max().getAsInt();
		int minColumn = positions.stream().mapToInt(index -> index % 8).min().getAsInt();
		int maxColumn = positions.stream().mapToInt(index -> index % 8).max().getAsInt();
		assertEquals("split cannon at " + positions, positions.size(),
			(maxRow - minRow + 1) * (maxColumn - minColumn + 1));
	}

	private static void assertPreserved(Fixture fixture, BankOrganizationPreview preview)
	{
		Map<Integer, BankItemSnapshot> originals = new LinkedHashMap<>();
		for (BankItemSnapshot item : fixture.bank) originals.put(item.getItemId(), item);
		assertEquals("each actual bank entry remains", fixture.bank.size(), preview.getPlannedItems().size());
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			assertFalse("no artificial row fillers", item.isBlank());
			BankItemSnapshot original = originals.remove(item.getItemId());
			assertTrue("no item loss, duplicates or invented parts", original != null);
			assertEquals(original.getQuantity(), item.getQuantity());
			assertEquals(original.isPlaceholder(), item.isPlaceholder());
		}
		assertTrue(originals.isEmpty());
	}

	private static final class Fixture
	{
		private final List<BankItemSnapshot> bank;
		private final Map<Integer, CatalogItem> catalog;

		private Fixture(List<BankItemSnapshot> bank, Map<Integer, CatalogItem> catalog)
		{
			this.bank = bank;
			this.catalog = catalog;
		}
	}
}

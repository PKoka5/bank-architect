package com.pkoka5.ironmanbankarchitect.organize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Developer-only exporter. Uses the real preview implementation, never starts a client. */
public final class CombatLayoutSimulation
{
	private static final Gson JSON = new GsonBuilder().disableHtmlEscaping().create();
	private static final Pattern ROW = Pattern.compile(
		"^row=\\d+ col=\\d+ slot=\\d+ \\| id=(\\d+) \\| name=(.*?) \\| quantity=(\\d+) \\| placeholder=(true|false)(?: \\|.*)?$");
	private static final Pattern TAB = Pattern.compile("^TAB \\d+ .*? \\| key=([^|]+) \\| name=([^|]+) \\| items=(\\d+)$");
	private static final Map<Integer, String> FAMILIES = families();

	public static void main(String[] args) throws Exception
	{
		if (args.length != 5 && args.length != 6)
			throw new IllegalArgumentException("output, cached stats, Main export, Ironman export, cached alch mapping, optional latest export required");
		byte[] statBytes = Files.readAllBytes(Paths.get(args[1]));
		JsonObject official = new JsonParser().parse(new String(statBytes, StandardCharsets.UTF_8)).getAsJsonObject();
		Map<Integer, GearStats> stats = readStats(official);
		GearStatsSource source = id -> Optional.ofNullable(stats.get(id));
		byte[] mappingBytes = Files.readAllBytes(Paths.get(args[4]));
		Map<Integer,Integer> values = new LinkedHashMap<>();
		for (JsonElement row : new JsonParser().parse(new String(mappingBytes, StandardCharsets.UTF_8)).getAsJsonArray())
		{
			JsonObject item = row.getAsJsonObject();
			if (item.has("highalch") && !item.get("highalch").isJsonNull())
				values.put(item.get("id").getAsInt(),item.get("highalch").getAsInt());
		}
		ItemValueSource alchValues = id -> values.getOrDefault(id,0);
		List<Scenario> scenarios = new ArrayList<>(Arrays.asList(
			readExport("friend-main", "Vriend · gedeeltelijke Main-export", Paths.get(args[2])),
			readExport("ironman-bank", "Jouw volledige Ironman-export", Paths.get(args[3])),
			fixture("sparse-owned", "Weinig gear · Oathplate en Rune", Collections.emptySet(), 1093, 30756, 30750, 30753),
			fixture("sparse-placeholders", "Oathplate als placeholders", new HashSet<>(Arrays.asList(30750, 30753, 30756)),
				1093, 30756, 30750, 30753),
			fixture("partial-barrows", "Incomplete Barrows-sets", Collections.emptySet(), 4901, 4924, 30756, 4894, 30753, 4918),
			fixture("busy-gear", "Veel sets en cannon", Collections.emptySet(),
				30750,30753,30756,30777,30779,30781,11826,11828,11830,27235,27238,27241,
				21018,21021,21024,4099,4101,4103,4105,4107,4109,4111,4113,4115,4117,
				4716,4718,4720,4722,4724,4726,4728,4730,6,8,10,12,
				11804,11806,11808,22324,22978,28338,13652,31097,13239,13237,13235,
				10498,10499,6585,19553,19547,12002,11840,6889,12825,12821,12605,6737,11773,6733,11771,6731,11770)));
		if (args.length == 6)
			scenarios.add(readExport("latest-ironman-bank", "Jouw nieuwste Ironman-export", Paths.get(args[5])));
		Scenario endgame = endgameFixture(stats);
		scenarios.add(endgame);
		scenarios.add(endgameVariant(endgame, "endgame-placeholders", "Endgame gear als placeholders",
			new HashSet<>(Arrays.asList(26382,26384,26386,31106,33639,30750,30753,30756,27235,27238,27241,21018,21021,21024,
				28933,28936,28939,22325,20997,27275,25867,21006,31097,21295,28951,21791))));
		Scenario reversed = endgameVariant(endgame, "endgame-reversed", "Endgame met omgekeerde invoer", Collections.emptySet());
		Collections.reverse(reversed.items); scenarios.add(reversed);
		Map<String, List<Integer>> endgameCombatOrders = new LinkedHashMap<>();
		List<Object> exported = new ArrayList<>();
		for (Scenario scenario : scenarios)
		{
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("key", scenario.key); data.put("label", scenario.label);
			data.put("partial", scenario.skipped > 0 || scenario.items.size() != scenario.declared);
			data.put("inputCount", scenario.items.size()); data.put("declaredCount", scenario.declared);
			data.put("skippedIncompleteRows", scenario.skipped);
			data.put("placeholderCount", scenario.items.stream().filter(i -> i.placeholder).count());
			data.put("originalCombat", encode(scenario.originalCombat, Collections.emptySet(), stats));
			data.put("originalDrops", encode(scenario.originalDrops, Collections.emptySet(), stats));
			data.put("originalDropsAmmoGroups", dropsGroups(scenario.originalDrops));
			List<Object> results = new ArrayList<>();
			for (BankPreset preset : Arrays.asList(BankPresets.MAIN, BankPresets.IRONMAN))
			{
				List<BankItemSnapshot> bankItems = new ArrayList<>();
				for (Input item : scenario.items)
					bankItems.add(new BankItemSnapshot(item.id, item.quantity, bankItems.size(), item.placeholder));
				BankLayoutPlan plan = BankLayoutPlan.defaultFor(preset);
				long buildStarted = System.nanoTime();
				BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(new BankSnapshot(bankItems),
					CompositeItemCatalog.DEFAULT, preset, source, alchValues,
					CategoryOverrideSource.NONE, plan, BankLayoutOptions.defaultFor(preset));
				long buildMillis = (System.nanoTime() - buildStarted) / 1_000_000;
				assertPreserved(scenario, preview);
				List<BankPreviewItem> combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
				List<BankPreviewItem> alch = preview.getPlannedItems().stream()
					.filter(i -> "alch".equals(i.getLayoutTagKey())).collect(Collectors.toList());
				List<BankPreviewItem> drops = preview.getCategories().get(plan.destinationOf("boss-loot")).getItems();
				List<BankPreviewItem> equipment = combat.stream().filter(i -> !GearItemSorter.isAmmo(i, source))
					.collect(Collectors.toList());
				GearItemSorter.GearLayout gear = GearItemSorter.plan(equipment, source);
				Set<Integer> frontIds = gear.getSetupRows().stream().map(BankPreviewItem::getItemId).collect(Collectors.toSet());
				Map<Integer, Integer> primaryTargets = GearItemSorter.setupTargets(gear, equipment.size(), source);
				assertPrimaryTargets(combat, primaryTargets, scenario.key + " / " + preset.getType());
				assertVerticalFamilies(combat, frontIds, equipment.size(), scenario.key + " / " + preset.getType());
				if (scenario.key.startsWith("sparse-"))
				{
					if (!frontIds.containsAll(Arrays.asList(30750,30753,30756)))
						throw new IllegalStateException("Oathplate did not supply the selected setup: " + scenario.key);
					if (alch.stream().noneMatch(i -> i.getItemId() == 1093))
						throw new IllegalStateException("Rune skirt did not route to Alch: " + scenario.key);
				}
				assertCannon(combat,scenario.key);
				if (scenario.key.startsWith("endgame-"))
				{
					assertEndgame(combat, drops, primaryTargets, scenario.key + " / " + preset.getType());
					List<Integer> order = combat.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
					List<Integer> baseline = endgameCombatOrders.putIfAbsent(preset.getType().name(), order);
					if (baseline != null && !baseline.equals(order))
						throw new IllegalStateException("Endgame Combat changed when withdrawing gear or reversing input: " + scenario.key);
				}
				Map<String, Object> result = new LinkedHashMap<>();
				result.put("preset", preset.getType().name());
				result.put("previewBuildMs", buildMillis);
				result.put("combat", encode(combat, frontIds, stats)); result.put("alch", encode(alch, Collections.emptySet(), stats));
				result.put("drops", encode(drops, Collections.emptySet(), stats));
				result.put("dropsGroups", dropsGroups(drops));
				result.put("frontCount", frontIds.size()); result.put("alignedRows", gear.getAlignedSize() / 8);
				result.put("primaryTargets",primaryTargets);
				result.put("primaryRows",primaryTargets.values().stream().map(i -> i / 8).distinct().count());
				result.put("combatStatCount", combat.stream().filter(i -> stats.containsKey(i.getItemId())).count());
				result.put("metadataFallbackCount", combat.stream().filter(i -> !stats.containsKey(i.getItemId())).count());
				if (scenario.key.startsWith("endgame-"))
				{
					result.put("independentArmourTargets", endgameArmourTargets());
					List<BankPreviewItem> weapons = combat.stream().filter(i -> frontIds.contains(i.getItemId())
						&& stats.containsKey(i.getItemId()) && stats.get(i.getItemId()).getSlot() == GearSlot.WEAPON)
						.collect(Collectors.toList());
					result.put("selectedWeapons", encode(weapons, frontIds, stats));
					System.out.println("  Selected frontline weapons: " + weapons.stream()
						.map(BankPreviewItem::getDisplayName).collect(Collectors.joining(", ")));
				}
				results.add(result);
				System.out.println(scenario.key + " / " + preset.getType() + ": input=" + bankItems.size()
					+ ", combat=" + combat.size() + ", alch=" + alch.size() + ", drops=" + drops.size()
					+ ", primaryTargets=" + primaryTargets.size());
				System.out.println("  Preview build duration: " + buildMillis + " ms");
			}
			data.put("results", results); exported.add(data);
		}
		Map<String, Object> output = new LinkedHashMap<>();
		output.put("statsSource", "https://static.runelite.net/item/stats.ids.min.json");
		output.put("statsSha256", hex(MessageDigest.getInstance("SHA-256").digest(statBytes)));
		output.put("alchSource", "https://prices.runescape.wiki/api/v1/osrs/mapping");
		output.put("alchMappingSha256",hex(MessageDigest.getInstance("SHA-256").digest(mappingBytes)));
		output.put("statsDate", "2026-10-09"); output.put("scenarios", exported);
		Path target = Paths.get(args[0]); Files.createDirectories(target.toAbsolutePath().getParent());
		Files.writeString(target, JSON.toJson(output), StandardCharsets.UTF_8);
		System.out.println("All " + scenarios.size() * 2 + " preview simulations preserved IDs, quantities and placeholders; physical setup and vertical secondary grouping passed.");
	}

	private static void assertPrimaryTargets(List<BankPreviewItem> items, Map<Integer, Integer> targets, String scenario)
	{
		for (Map.Entry<Integer, Integer> target : targets.entrySet())
			if (items.get(target.getValue()).getItemId() != target.getKey())
				throw new IllegalStateException("Setup item " + target.getKey() + " did not occupy physical slot "
					+ target.getValue() + ": " + scenario);
	}

	/** Reports actual physical positions without prescribing a sorter implementation. */
	private static Map<String, Object> dropsGroups(List<BankPreviewItem> items)
	{
		Map<String, List<Integer>> positions = new LinkedHashMap<>();
		for (String group : Arrays.asList("ammo", "arrows", "bolts", "darts", "cannonballs", "alch"))
			positions.put(group, new ArrayList<>());
		for (int i = 0; i < items.size(); i++)
		{
			BankPreviewItem item = items.get(i);
			String family = ammoFamily(item);
			if (family != null) { positions.get("ammo").add(i); positions.get(family).add(i); }
			if ("alch".equals(item.getLayoutTagKey())) positions.get("alch").add(i);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<String, List<Integer>> group : positions.entrySet())
		{
			int runs = 0, previous = -2;
			for (int index : group.getValue()) { if (index != previous + 1) runs++; previous = index; }
			Map<String, Object> metric = new LinkedHashMap<>();
			metric.put("count", group.getValue().size()); metric.put("runs", runs);
			metric.put("physicalIndices", group.getValue()); result.put(group.getKey(), metric);
		}
		return result;
	}

	private static String ammoFamily(BankPreviewItem item)
	{
		if (item.getItemCategory() != ItemCategory.GEAR) return null;
		String name = item.getDisplayName().toLowerCase(Locale.ROOT);
		if (name.contains("pouch") || name.contains("grapple")) return null;
		if (name.matches(".*\\b(?:arrows?)\\b.*|(?:bronze|iron|steel|black|mithril|adamant|rune) brutal")) return "arrows";
		if (name.matches(".*\\bbolts?\\b.*|bolt rack")) return "bolts";
		if (name.matches(".*\\bdarts?\\b.*")) return "darts";
		if (name.matches(".*\\bcannonballs?\\b.*")) return "cannonballs";
		return null;
	}

	private static void assertVerticalFamilies(List<BankPreviewItem> items, Set<Integer> primaryIds,
		int equipmentCount, String scenario)
	{
		Map<Integer, Integer> positions = new LinkedHashMap<>();
		for (int i = 0; i < equipmentCount; i++)
			if (!primaryIds.contains(items.get(i).getItemId())) positions.put(items.get(i).getItemId(), i);
		for (List<Integer> family : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			if (GearSetSemanticRuleSet.isCannonPart(family.get(0))) continue;
			List<Integer> owned = family.stream().filter(positions::containsKey).map(positions::get)
				.collect(Collectors.toList());
			if (owned.size() < 2) continue;
			int minRow=owned.stream().mapToInt(i -> i / 8).min().getAsInt();
			int maxRow=owned.stream().mapToInt(i -> i / 8).max().getAsInt();
			int minCol=owned.stream().mapToInt(i -> i % 8).min().getAsInt();
			int maxCol=owned.stream().mapToInt(i -> i % 8).max().getAsInt();
			int height=maxRow-minRow+1;
			if (height * (maxCol-minCol+1) != owned.size() || height == 1 && equipmentCount > 8)
				throw new IllegalStateException("Secondary family does not form a vertical rectangle: "
					+ family.get(0) + " / " + owned + " / " + scenario);
			for (int i = 0; i < owned.size(); i++)
				if (owned.get(i) != minRow * 8 + minCol + i % height * 8 + i / height)
					throw new IllegalStateException("Secondary family lost column-major equipment order: "
						+ family.get(0) + " / " + owned + " / " + scenario);
		}
	}

	private static void assertCannon(List<BankPreviewItem> items,String scenario)
	{
		List<Integer> positions = new ArrayList<>();
		for (int i=0;i<items.size();i++) if (GearSetSemanticRuleSet.isCannonPart(items.get(i).getItemId())) positions.add(i);
		if (positions.size()<2) return;
		int minRow=positions.stream().mapToInt(i->i/8).min().getAsInt();
		int maxRow=positions.stream().mapToInt(i->i/8).max().getAsInt();
		int minCol=positions.stream().mapToInt(i->i%8).min().getAsInt();
		int maxCol=positions.stream().mapToInt(i->i%8).max().getAsInt();
		if ((maxRow-minRow+1)*(maxCol-minCol+1)!=positions.size())
			throw new IllegalStateException("Cannon wraps across bank edges: " + scenario);
	}

	private static Map<Integer, GearStats> readStats(JsonObject official)
	{
		Map<Integer, GearStats> stats = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> row : official.entrySet())
		{
			JsonObject item = row.getValue().getAsJsonObject();
			if (!item.has("equipable") || !item.get("equipable").getAsBoolean() || !item.has("equipment")) continue;
			JsonObject equipment = item.getAsJsonObject("equipment");
			GearSlot slot = GearSlot.fromRuneLiteSlot(value(equipment, "slot"));
			if (slot == null) continue;
			stats.put(Integer.parseInt(row.getKey()), new GearStats(slot,
				value(equipment,"astab"),value(equipment,"aslash"),value(equipment,"acrush"),
				value(equipment,"amagic"),value(equipment,"arange"),value(equipment,"str"),value(equipment,"rstr"),
				value(equipment,"prayer"),value(equipment,"dstab"),value(equipment,"dslash"),value(equipment,"dcrush"),
				value(equipment,"dmagic"),value(equipment,"drange"),
				equipment.has("mdmg") ? Math.round(equipment.get("mdmg").getAsFloat() * 10f) : 0,value(equipment,"aspeed")));
		}
		return stats;
	}

	private static int value(JsonObject object, String key) { return object.has(key) ? object.get(key).getAsInt() : 0; }

	private static Scenario readExport(String key, String label, Path path) throws Exception
	{
		Scenario scenario = new Scenario(key, label); String tabKey = "";
		for (String line : normalizedLines(path))
		{
			Matcher tab = TAB.matcher(line);
			if (tab.matches()) { tabKey = tab.group(1).trim(); scenario.declared += Integer.parseInt(tab.group(3)); }
			Matcher row = ROW.matcher(line);
			if (row.matches())
			{
				Input item = new Input(Integer.parseInt(row.group(1)), row.group(2), Integer.parseInt(row.group(3)),
					Boolean.parseBoolean(row.group(4)));
				scenario.items.add(item);
				if ("combat-gear".equals(tabKey)) scenario.originalCombat.add(item.preview());
				if ("slayer-boss-loot".equals(tabKey)) scenario.originalDrops.add(item.preview());
			}
			else if (line.startsWith("row=")) scenario.skipped++;
		}
		if (scenario.items.isEmpty()) throw new IllegalArgumentException("No complete bank rows in " + path);
		return scenario;
	}

	private static List<String> normalizedLines(Path path) throws Exception
	{
		List<String> result = new ArrayList<>();
		for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8))
		{
			String line = raw.replace("\ufeff", "").trim();
			if (line.isEmpty()) continue;
			boolean continuation = line.startsWith("|") || line.matches("^(quantity|placeholder|catalogCategory|subcategory|layoutTag|key|name|items)=.*");
			if (continuation && !result.isEmpty())
			{
				int last = result.size() - 1;
				String previous = result.get(last);
				if (previous.startsWith("row=") || previous.startsWith("TAB "))
				{
					result.set(last, previous + (line.startsWith("|") || previous.endsWith("|") ? " " : " | ") + line);
					continue;
				}
			}
			result.add(line);
		}
		return result;
	}

	private static Scenario fixture(String key, String label, Set<Integer> placeholders, int... ids)
	{
		Scenario scenario = new Scenario(key, label); scenario.declared = ids.length;
		for (int id : ids)
		{
			Input item = new Input(id, CompositeItemCatalog.DEFAULT.describeOrUnknown(id).getDisplayName(),
				placeholders.contains(id) ? 0 : 1, placeholders.contains(id));
			scenario.items.add(item);
		}
		return scenario;
	}

	/** Representative collector bank: one active/base state per equipment slot, not every degraded or colour variant. */
	private static Scenario endgameFixture(Map<Integer, GearStats> stats)
	{
		Set<Integer> ids = new LinkedHashSet<>();
		Set<String> wanted = new HashSet<>(Arrays.asList("gear.rune", "gear.proselyte", "gear.sunfire-fanatic",
			"gear.black-dhide", "gear.blessed-saradomin", "gear.crystal", "gear.masori-f", "gear.mystic-blue",
			"gear.mystic-dark", "gear.infinity", "gear.bloodbark", "gear.virtus", "gear.ancestral",
			"gear.eclipse-moon", "gear.blood-moon", "gear.blue-moon", "gear.inquisitor", "gear.torva",
			"gear.barrows-ahrim", "gear.barrows-dharok", "gear.barrows-guthan", "gear.barrows-karil",
			"gear.barrows-torag", "gear.barrows-verac", "gear.oathplate", "gear.armadyl", "gear.rock-shell",
			"gear.spined", "gear.skeletal", "gear.hueycoatl"));
		OrderedItemFamilies table = new OrderedItemFamilies(CombatLayoutSimulation.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"), 0);
		table.entries().forEach((key, family) -> {
			if (!wanted.remove(key)) return;
			Set<GearSlot> usedSlots = new HashSet<>();
			for (int id : family)
				if (stats.containsKey(id) && usedSlots.add(stats.get(id).getSlot())) ids.add(id);
		});
		if (!wanted.isEmpty()) throw new IllegalStateException("Missing endgame fixture families: " + wanted);
		for (int id : new int[] {
			// Boss sets absent from the ordered-family table; all Void helmet roles and elite pieces.
			11832,11834,11836,22326,22327,22328,21298,21301,21304,11665,11664,11663,13072,13073,8842,
			// Raids, GWD, Nightmare, Corp, DT2, Slayer and specialist weapons/shields.
			22325,20997,27275,25867,26374,22323,22324,26219,21003,21006,21012,22978,28338,24417,
			24422,24423,24424,24425,12926,12899,11802,11804,11806,11808,13576,13652,29577,
			31106,31113,33639,27690,29591,29594,30634,
			27610,28922,29589,19675,29796,21015,12817,12825,12821,11283,22002,21633,27251,21000,22322,
			// Capes, boots, jewellery, gloves and non-wearable combat utility.
			21295,28951,21791,22109,19547,29801,24780,12002,28307,28310,28313,28316,25975,
			22981,26235,19544,7462,31097,13239,13237,13235,11840,12931,24271,22947,27641,
			11773,11771,11770,6,8,10,12,
			// Obsolete progression gear/tools exercise universal Alch routing.
			1147,1373,1359,1357,6739,1275,1271,11920,1387,1393,3204,
			// All four finished ammunition families; high and low tiers and enchantments.
			11212,21326,892,882,4803,21905,21944,21946,21950,9243,9144,9143,11875,4740,
			11230,25849,811,805,28991,2,31912,31914,31916
		}) ids.add(id);
		Scenario scenario = fixture("endgame-owned", "Endgame Ironman met boss- en raidsgear", Collections.emptySet(),
			ids.stream().mapToInt(Integer::intValue).toArray());
		for (int i = 0; i < scenario.items.size(); i++)
		{
			Input input = scenario.items.get(i);
			if (ammoFamily(input.preview()) != null)
				scenario.items.set(i, new Input(input.id, input.name, 1000 + i, false));
		}
		return scenario;
	}

	private static Scenario endgameVariant(Scenario original, String key, String label, Set<Integer> placeholders)
	{
		Scenario scenario = new Scenario(key, label); scenario.declared = original.declared;
		for (Input input : original.items)
			scenario.items.add(new Input(input.id, input.name, placeholders.contains(input.id) ? 0 : input.quantity,
				placeholders.contains(input.id)));
		return scenario;
	}

	private static Map<Integer, Integer> endgameArmourTargets()
	{
		Map<Integer, Integer> result = new LinkedHashMap<>();
		int[][] columns = {{26382,26384,26386},{27235,27238,27241},{21018,21021,21024},{28933,28936,28939}};
		for (int column = 0; column < columns.length; column++)
			for (int row = 0; row < 3; row++) result.put(columns[column][row], row * 8 + column);
		return result;
	}

	private static void assertEndgame(List<BankPreviewItem> combat, List<BankPreviewItem> drops,
		Map<Integer, Integer> primaryTargets, String scenario)
	{
		for (Map.Entry<Integer, Integer> target : endgameArmourTargets().entrySet())
			if (!target.getValue().equals(primaryTargets.get(target.getKey()))
				|| combat.get(target.getValue()).getItemId() != target.getKey())
				throw new IllegalStateException("Independent endgame armour target failed: " + target + " / " + scenario);
		if (!primaryTargets.keySet().containsAll(Arrays.asList(22325,20997,27275)))
			throw new IllegalStateException("Independent raid-weapon priority failed: " + scenario);
		if (combat.stream().anyMatch(item -> ammoFamily(item) != null))
			throw new IllegalStateException("Finished ammunition remained in endgame Combat: " + scenario);
		int previousFamily = -1;
		for (BankPreviewItem item : drops)
		{
			String family = ammoFamily(item);
			if (family == null) continue;
			int index = Arrays.asList("arrows", "bolts", "darts", "cannonballs").indexOf(family);
			if (index < previousFamily) throw new IllegalStateException("Endgame ammunition families are interleaved: " + scenario);
			previousFamily = index;
		}
		Map<String, Object> groups = dropsGroups(drops);
		for (String key : Arrays.asList("ammo", "arrows", "bolts", "darts", "cannonballs", "alch"))
			if (((Number) ((Map<?, ?>) groups.get(key)).get("runs")).intValue() != 1)
				throw new IllegalStateException("Endgame Drops group is missing or scattered: " + key + " / " + scenario);
	}

	private static List<Object> encode(List<BankPreviewItem> items, Set<Integer> front, Map<Integer, GearStats> stats)
	{
		List<Object> encoded = new ArrayList<>();
		for (BankPreviewItem item : items)
		{
			Map<String,Object> row = new LinkedHashMap<>(); int id = item.getItemId();
			row.put("id",id); row.put("name",item.getDisplayName()); row.put("quantity",item.getQuantity());
			row.put("placeholder",item.isPlaceholder()); row.put("front",front.contains(id));
			row.put("family",FAMILIES.getOrDefault(id,"")); row.put("tag",item.getLayoutTagKey());
			row.put("style",stats.containsKey(id) ? GearItemSorter.styleOf(item.getDisplayName(), stats.get(id)).name() : "");
			row.put("slot",stats.containsKey(id) ? stats.get(id).getSlot().name() : item.getSubcategory());
			encoded.add(row);
		}
		return encoded;
	}

	private static void assertPreserved(Scenario scenario, BankOrganizationPreview preview)
	{
		Map<Integer,Input> expected = new LinkedHashMap<>();
		for (Input input : scenario.items)
			if (expected.put(input.id,input) != null) throw new IllegalStateException("Duplicate input ID: " + input.id);
		List<BankPreviewItem> planned = preview.getPlannedItems();
		if (planned.size() != expected.size()) throw new IllegalStateException("Item count changed in " + scenario.key);
		Set<Integer> seen = new HashSet<>();
		for (BankPreviewItem item : planned)
		{
			Input original = expected.get(item.getItemId());
			if (original == null || !seen.add(item.getItemId()) || item.getQuantity() != original.quantity
				|| item.isPlaceholder() != original.placeholder || item.isBlank())
				throw new IllegalStateException("Bank entry changed: " + scenario.key + " / " + item.getItemId());
		}
	}

	private static Map<Integer,String> families()
	{
		Map<Integer,String> result = new LinkedHashMap<>();
		OrderedItemFamilies table = new OrderedItemFamilies(CombatLayoutSimulation.class.getResourceAsStream(
			"/com/pkoka5/ironmanbankarchitect/catalog/gear-layout-families.tsv"),0);
		table.entries().forEach((key, ids) -> ids.forEach(id -> result.put(id,key)));
		for (List<Integer> family : GearSetSemanticRuleSet.gearSetsInSlotOrder())
		{
			String key = GearSetSemanticRuleSet.isCannonPart(family.get(0))
				? "gear.dwarf-cannon" : "gear.set-" + family.get(0);
			for (int id : family) result.putIfAbsent(id,key);
		}
		return result;
	}

	private static String hex(byte[] bytes)
	{
		StringBuilder hex = new StringBuilder(); for (byte value : bytes) hex.append(String.format("%02x",value & 255));
		return hex.toString();
	}

	private static final class Scenario
	{
		final String key, label; final List<Input> items = new ArrayList<>();
		final List<BankPreviewItem> originalCombat = new ArrayList<>(); int declared, skipped;
		final List<BankPreviewItem> originalDrops = new ArrayList<>();
		Scenario(String key, String label) { this.key=key; this.label=label; }
	}

	private static final class Input
	{
		final int id, quantity; final String name; final boolean placeholder;
		Input(int id,String name,int quantity,boolean placeholder)
		{ this.id=id; this.name=name; this.quantity=quantity; this.placeholder=placeholder; }
		BankPreviewItem preview()
		{ return new BankPreviewItem(CompositeItemCatalog.DEFAULT.describeOrUnknown(id),quantity,placeholder); }
	}
}

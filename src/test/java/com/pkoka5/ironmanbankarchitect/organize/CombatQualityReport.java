package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/** Development output and independent assertions; never part of the plugin JAR. */
final class CombatQualityReport
{
	static final Map<Integer, Fact> FACTS = facts();
	static final List<String> ZONES = Arrays.asList("M", "R", "G", "P", "S");
	final String name;
	final BankOrganizationPreview preview;
	final List<BankPreviewItem> combat;
	final List<BankPreviewItem> tools;
	final List<String> violations = new ArrayList<>();
	final List<String> geometry = new ArrayList<>();
	final int frontCount;
	final long buildNanos;
	private final Map<Integer, Fact> expectedFacts;
	private final Set<Integer> primary;

	CombatQualityReport(String name, BankSnapshot source, BankOrganizationPreview preview,
		BankLayoutPlan plan, Set<Integer> primary, boolean armourMatrix, long buildNanos)
	{
		this(name,source,preview,plan,primary,armourMatrix,buildNanos,true);
	}
	CombatQualityReport(String name, BankSnapshot source, BankOrganizationPreview preview,
		BankLayoutPlan plan, Set<Integer> primary, boolean armourMatrix, long buildNanos, boolean automatic)
	{
		this(name,source,preview,plan,primary,armourMatrix,buildNanos,automatic,FACTS);
	}
	CombatQualityReport(String name, BankSnapshot source, BankOrganizationPreview preview,
		BankLayoutPlan plan, Set<Integer> primary, boolean armourMatrix, long buildNanos, boolean automatic,
		Map<Integer,Fact> expectedFacts)
	{
		this.expectedFacts = expectedFacts; this.primary = primary;
		this.name = name;
		this.preview = preview;
		this.buildNanos = buildNanos;
		combat = preview.getCategories().get(plan.destinationOf("gear")).getItems();
		tools = preview.getCategories().get(plan.destinationOf("tools")).getItems();
		frontCount = primary.size();
		conservation(source);
		if (!automatic) return;
		List<Integer> actual = ids(combat);
		int maxPrimaryTarget = -1;
		for (int style = 0; style < 3; style++)
		{
			String zone = ZONES.get(style);
			long count = primary.stream().map(expectedFacts::get).filter(Objects::nonNull).filter(fact -> fact.zone.equals(zone)).count();
			if (count > 0) maxPrimaryTarget = Math.max(maxPrimaryTarget,(int)(count-1)*8+style);
		}
		boolean columnsFit = maxPrimaryTarget < actual.size();
		if (armourMatrix && columnsFit)
		{
			List<Integer> slotOrder = Arrays.asList(0,1,2,3,7,4,8,5,6,9);
			for (int style = 0; style < 3; style++)
			{
				String zone = ZONES.get(style);
				List<Fact> column = primary.stream().map(expectedFacts::get).filter(Objects::nonNull)
					.filter(fact -> fact.zone.equals(zone)).sorted(Comparator.comparingInt(fact -> slotOrder.indexOf(fact.memberOrder)))
					.collect(Collectors.toList());
				for (int row = 0; row < column.size(); row++)
					if (actual.indexOf(column.get(row).id) != row * 8 + style)
						fail("PRIMARY_COLUMN",column.get(row).id + " expected " + (row*8+style) + "; actual " + actual.indexOf(column.get(row).id));
			}
		}
		List<Integer> toolIds = ids(tools);
		for (BankItemSnapshot item : source.getItems())
		{
			Fact fact = expectedFacts.get(item.getItemId());
			if (fact != null && fact.zone.equals("T") && !toolIds.contains(fact.id))
				fail("CANNON_DESTINATION", fact.id + " missing from Tools");
			else if (fact != null && !fact.zone.equals("T") && !actual.contains(fact.id))
				fail("COMBAT_DESTINATION", "reviewed retained gear " + fact.id + " missing from Combat");
		}
		if (!armourMatrix || !columnsFit) zones(actual, primary);
		families(actual, primary, false);
		families(ids(tools), Collections.emptySet(), true);
		variants(actual, primary);
		verticalOpportunity(actual, primary);
	}

	private void conservation(BankSnapshot source)
	{
		Map<Integer, BankItemSnapshot> wanted = new LinkedHashMap<>();
		for (BankItemSnapshot item : source.getItems())
			if (wanted.put(item.getItemId(), item) != null) fail("FIXTURE_DUPLICATE", "source " + item.getItemId());
		Set<Integer> seen = new LinkedHashSet<>();
		for (BankPreviewItem item : preview.getPlannedItems())
		{
			if (item.isBlank()) fail("INVENTED_CELL", "blank in real bank target");
			if (!seen.add(item.getItemId())) fail("DUPLICATE", "planned " + item.getItemId());
			BankItemSnapshot before = wanted.get(item.getItemId());
			if (before == null) fail("INVENTED_ITEM", Integer.toString(item.getItemId()));
			else if (before.getQuantity() != item.getQuantity() || before.isPlaceholder() != item.isPlaceholder())
				fail("STACK_CHANGED", Integer.toString(item.getItemId()));
		}
		if (!seen.equals(wanted.keySet())) fail("MISSING_ITEM", "expected " + wanted.keySet() + "; actual " + seen);
	}

	private void zones(List<Integer> actual, Set<Integer> primary)
	{
		int previous = -1;
		String previousZone = "";
		for (int index = 0; index < actual.size(); index++)
		{
			int id = actual.get(index);
			if (primary.contains(id)) continue;
			Fact fact = expectedFacts.get(id);
			if (fact == null) { fail("UNREVIEWED_FIXTURE_ID", Integer.toString(id)); continue; }
			int zone = ZONES.indexOf(fact.zone);
			if (zone < 0) fail("DESTINATION", "Tools item " + id + " remains in Combat");
			else if (zone < previous) fail("ZONE_ORDER", "cell " + index + ": " + fact.zone + " after " + previousZone);
			previous = Math.max(previous, zone);
			previousZone = fact.zone;
		}
		for (Fact fact : expectedFacts.values())
			if (fact.zone.equals("T") && actual.contains(fact.id)) fail("CANNON_IN_COMBAT", Integer.toString(fact.id));
	}

	private void families(List<Integer> actual, Set<Integer> primary, boolean cannonOnly)
	{
		Map<String, List<Fact>> families = new LinkedHashMap<>();
		for (int id : actual)
		{
			Fact fact = expectedFacts.get(id);
			if (fact != null && !primary.contains(id) && (fact.zone.equals("T") == cannonOnly))
				families.computeIfAbsent(fact.family, ignored -> new ArrayList<>()).add(fact);
		}
		for (Map.Entry<String, List<Fact>> entry : families.entrySet())
		{
			List<Fact> family = entry.getValue();
			if (family.size() < 2) continue;
			List<Integer> positions = family.stream().map(fact -> actual.indexOf(fact.id)).sorted().collect(Collectors.toList());
			int lowRow = positions.stream().mapToInt(index -> index / 8).min().getAsInt();
			int highRow = positions.stream().mapToInt(index -> index / 8).max().getAsInt();
			int lowCol = positions.stream().mapToInt(index -> index % 8).min().getAsInt();
			int highCol = positions.stream().mapToInt(index -> index % 8).max().getAsInt();
			boolean rectangle = family.size() == (highRow - lowRow + 1) * (highCol - lowCol + 1);
			boolean compact = positions.get(positions.size() - 1) - positions.get(0) + 1 == family.size();
			String shape = rectangle ? lowCol == highCol ? "vertical" : "rectangle" : compact ? "compact" : "scattered";
			geometry.add(entry.getKey() + ": " + shape + " " + positions);
			if (cannonOnly && !rectangle) fail("CANNON_SHAPE", entry.getKey() + " " + positions);
			if (!cannonOnly && !rectangle && !compact) fail("FAMILY_SHAPE", entry.getKey() + " " + positions);
			// Armour's independently specified member order; functional weapon groups have no fictional wearable order.
			if (!cannonOnly && family.stream().anyMatch(fact -> fact.memberOrder < 8)
				&& family.stream().noneMatch(fact -> fact.role.equals("variant")))
			{
				List<Fact> expected = new ArrayList<>(family);
				expected.sort(Comparator.comparingInt(fact -> fact.memberOrder));
				List<Integer> canonicalPositions = expected.stream().map(fact -> actual.indexOf(fact.id)).collect(Collectors.toList());
				List<Integer> columnMajor = new ArrayList<>(positions);
				columnMajor.sort(Comparator.comparingInt((Integer index) -> index % 8).thenComparingInt(index -> index / 8));
				if (!canonicalPositions.equals(positions) && !(rectangle && canonicalPositions.equals(columnMajor)))
					fail("FAMILY_SLOT_ORDER", entry.getKey() + " " + idsOf(expected));
			}
		}
	}

	private void variants(List<Integer> actual, Set<Integer> primary)
	{
		Map<Integer, List<Integer>> bySlot = new LinkedHashMap<>();
		for (int id : actual)
		{
			if (primary.contains(id)) continue;
			Fact fact = expectedFacts.get(id);
			if (fact != null && fact.family.equals("dharok"))
				bySlot.computeIfAbsent(fact.canonicalSlot, ignored -> new ArrayList<>()).add(actual.indexOf(id));
		}
		for (Map.Entry<Integer, List<Integer>> entry : bySlot.entrySet())
			if (entry.getValue().size() > 1)
			{
				int lowRow = entry.getValue().stream().mapToInt(index -> index / 8).min().getAsInt();
				int highRow = entry.getValue().stream().mapToInt(index -> index / 8).max().getAsInt();
				if (highRow - lowRow > 1) fail("CONDITION_SLOT_SCATTERED", entry.getKey() + " " + entry.getValue());
			}
		if (bySlot.values().stream().noneMatch(positions -> positions.size() > 1)) return;
		List<Integer> canonicalSlots = new ArrayList<>(bySlot.keySet());
		canonicalSlots.sort(Comparator.comparingInt(id -> expectedFacts.get(id).memberOrder));
		int previous = -1;
		for (int slot : canonicalSlots)
		{
			int start = Collections.min(bySlot.get(slot));
			if (start <= previous) fail("CONDITION_CANONICAL_ORDER", "Dharok slot " + slot + " begins at " + start + " after " + previous);
			previous = start;
		}
	}

	private void verticalOpportunity(List<Integer> actual, Set<Integer> primary)
	{
		int melee = secondarySize(actual,primary,"M");
		int[][] families = {{30750,30753,30756},{24419,24420,24421}};
		boolean bothPresent = Arrays.stream(families).allMatch(family -> Arrays.stream(family)
			.allMatch(id -> actual.contains(id) && !primary.contains(id)));
		// The frozen 33-cell Melee section admits vertical columns for these complete three-piece families.
		// This is a real independent policy gate: the earlier all-horizontal planner fails it.
		if (melee >= 33 && bothPresent && Arrays.stream(families).noneMatch(family ->
			actual.indexOf(family[1]) == actual.indexOf(family[0]) + 8
				&& actual.indexOf(family[2]) == actual.indexOf(family[0]) + 16))
			fail("FEASIBLE_VERTICAL", "33+ Melee cells retain neither complete Oathplate nor Inquisitor as a column");
		for (int[] family : new int[][] {{11826,11828,11830},{26241,26243,26245}})
		{
			String zone = expectedFacts.get(family[0]).zone;
			if (secondarySize(actual,primary,zone) >= 17 && Arrays.stream(family).allMatch(id -> actual.contains(id) && !primary.contains(id))
				&& (actual.indexOf(family[1]) != actual.indexOf(family[0]) + 8
					|| actual.indexOf(family[2]) != actual.indexOf(family[0]) + 16))
				fail("FEASIBLE_VERTICAL", "17+ " + zone + " cells do not keep independently reviewed family " + Arrays.toString(family) + " vertical");
		}
	}
	private int secondarySize(List<Integer> actual,Set<Integer> primary,String zone)
	{
		return (int) actual.stream().filter(id -> !primary.contains(id) && expectedFacts.get(id) != null && expectedFacts.get(id).zone.equals(zone)).count();
	}

	void fail(String rule, String detail) { violations.add(rule + ": " + detail); }

	void write(Path directory) throws IOException
	{
		Files.createDirectories(directory);
		String slug = name.replaceAll("[^a-zA-Z0-9-]", "-");
		StringBuilder html = new StringBuilder("<!doctype html><meta charset=\"utf-8\"><title>Combat quality: ")
			.append(escape(name)).append("</title><style>body{background:#181b20;color:#eee;font:15px system-ui;margin:24px}table{border-collapse:collapse;table-layout:fixed;width:1200px}td{border:1px solid #647080;padding:8px;height:70px;vertical-align:top;width:12.5%}small{display:block;color:#bcc5d1}.M{background:#44312c}.R{background:#2c4435}.G{background:#303754}.P{background:#51472e}.T{background:#44484f}.primary{outline:2px solid #b9cae0;outline-offset:-3px}.bad{color:#ffc0af}pre{white-space:pre-wrap}</style><h1>")
			.append(escape(name)).append("</h1><p>Actual physical eight-column output. Front ownership: ")
			.append(frontCount).append(" real items; preview build: ").append(String.format(java.util.Locale.ROOT,"%.2f",buildNanos/1e6))
			.append(" ms. Outlined cells are primary ownership. No fake blank cells are added.</p><h2>Independent gates</h2><pre class=\"bad\">")
			.append(escape(violations.isEmpty() ? "PASS" : String.join("\n", violations))).append("</pre>");
		appendGrid(html, "Combat", combat);
		appendGrid(html, "Tools / cannon", tools);
		html.append("<h2>Geometry diagnostics</h2><pre>").append(escape(String.join("\n",geometry))).append("</pre>");
		Files.write(directory.resolve(slug + ".html"), html.toString().getBytes(StandardCharsets.UTF_8));
		png(directory.resolve(slug + ".png"));
	}

	private void appendGrid(StringBuilder html, String title, List<BankPreviewItem> items)
	{
		html.append("<h2>").append(title).append("</h2><table>");
		for (int i = 0; i < items.size(); i++)
		{
			if (i % 8 == 0) html.append("<tr>");
			BankPreviewItem item = items.get(i);
			Fact fact = expectedFacts.get(item.getItemId());
			html.append("<td class=\"").append(fact == null ? "" : fact.zone)
				.append(title.equals("Combat") && primary.contains(item.getItemId()) ? " primary" : "").append("\"><small>")
				.append(i).append(" / ").append(item.getItemId()).append(" / ")
				.append(fact == null ? "unreviewed" : escape(fact.zone + "/" + fact.family))
				.append("</small>").append(escape(item.getDisplayName())).append("<small>quantity ")
				.append(item.getQuantity()).append(item.isPlaceholder() ? " / placeholder" : "").append("</small></td>");
			if (i % 8 == 7 || i + 1 == items.size()) html.append("</tr>");
		}
		html.append("</table>");
	}

	private void png(Path path) throws IOException
	{
		int rows = (combat.size() + 7) / 8;
		BufferedImage image = new BufferedImage(1200, Math.max(160, 70 + rows * 80), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(24,27,32)); graphics.fillRect(0,0,image.getWidth(),image.getHeight());
		graphics.setFont(new Font(Font.SANS_SERIF,Font.BOLD,18)); graphics.setColor(Color.WHITE);
		graphics.drawString(name + " / " + (violations.isEmpty() ? "PASS" : violations.size() + " violations"),12,28);
		for (int i = 0; i < combat.size(); i++)
		{
			BankPreviewItem item = combat.get(i); Fact fact = expectedFacts.get(item.getItemId());
			int x = i % 8 * 150, y = 50 + i / 8 * 80;
			graphics.setColor(color(fact == null ? "" : fact.zone)); graphics.fillRect(x,y,149,79);
			graphics.setColor(primary.contains(item.getItemId()) ? new Color(190,210,235) : new Color(90,100,120));
			graphics.drawRect(x+1,y+1,146,76);
			graphics.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11)); graphics.setColor(new Color(210,220,230));
			graphics.drawString(i + " / " + item.getItemId() + " / " + (fact == null ? "?" : fact.zone),x+6,y+16);
			String name = item.getDisplayName(); graphics.setColor(Color.WHITE);
			for (int line = 0; line < 3 && !name.isEmpty(); line++)
			{
				int cut = Math.min(21,name.length());
				if (cut < name.length() && name.substring(0,cut).lastIndexOf(' ') > 0) cut = name.substring(0,cut).lastIndexOf(' ');
				graphics.drawString(name.substring(0,cut),x+6,y+34+line*14); name = name.substring(cut).trim();
			}
		}
		graphics.dispose(); ImageIO.write(image,"png",path.toFile());
	}

	private static Color color(String zone)
	{
		switch (zone) {
			case "M": return new Color(68,49,44); case "R": return new Color(44,68,53);
			case "G": return new Color(48,55,84); case "P": return new Color(81,71,46);
			default: return new Color(68,72,79);
		}
	}

	static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(Collectors.toList());
	}
	private static List<Integer> idsOf(List<Fact> items)
	{
		return items.stream().map(item -> item.id).collect(Collectors.toList());
	}
	private static String escape(String text)
	{
		return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
	}

	private static Map<Integer, Fact> facts()
	{
		Map<Integer,Fact> result = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(CombatQualityReport.class
			.getResourceAsStream("combat-quality-facts.tsv"),StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.isEmpty()) continue;
				Fact fact = new Fact(line.split("\t",-1));
				if (result.put(fact.id,fact) != null) throw new AssertionError("duplicate fact " + fact.id);
			}
		}
		catch (IOException e) { throw new AssertionError(e); }
		return Collections.unmodifiableMap(result);
	}

	static final class Fact
	{
		final int id, memberOrder, canonicalSlot, armourRow, armourColumn;
		final String zone, family, role;
		final GearStats stats;
		Fact(int id,String zone,String family,int memberOrder,int canonicalSlot,String role,GearStats stats)
		{
			this.id = id; this.zone = zone; this.family = family; this.memberOrder = memberOrder;
			this.canonicalSlot = canonicalSlot; this.role = role; this.stats = stats;
			armourRow = -1; armourColumn = -1;
		}
		Fact(String[] columns)
		{
			if (columns.length != 9) throw new AssertionError("invalid fact " + Arrays.toString(columns));
			id = Integer.parseInt(columns[0]); zone = columns[1]; family = columns[2];
			memberOrder = Integer.parseInt(columns[3]); canonicalSlot = Integer.parseInt(columns[4]);
			armourRow = Integer.parseInt(columns[5]); armourColumn = Integer.parseInt(columns[6]); role = columns[7];
			if (columns[8].equals("-")) { stats = null; return; }
			int[] values = Arrays.stream(columns[8].split(",")).mapToInt(Integer::parseInt).toArray();
			if (values.length != 16) throw new AssertionError("invalid vector " + id);
			GearSlot slot;
			switch (values[0]) {
				case 0: slot = GearSlot.HEAD; break; case 1: slot = GearSlot.CAPE; break;
				case 2: slot = GearSlot.NECK; break; case 3: slot = GearSlot.WEAPON; break;
				case 4: slot = GearSlot.BODY; break; case 5: slot = GearSlot.SHIELD; break;
				case 7: slot = GearSlot.LEGS; break; case 9: slot = GearSlot.HANDS; break;
				case 10: slot = GearSlot.FEET; break; case 12: slot = GearSlot.RING; break;
				default: throw new AssertionError("invalid slot " + id);
			}
			stats = new GearStats(slot,values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8],
				values[9],values[10],values[11],values[12],values[13],values[14],values[15]);
		}
	}
}

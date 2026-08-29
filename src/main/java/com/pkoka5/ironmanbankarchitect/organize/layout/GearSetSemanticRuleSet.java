package com.pkoka5.ironmanbankarchitect.organize.layout;

import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Exact combat families whose reviewed equipment order is vertical outside the primary setups. */
public final class GearSetSemanticRuleSet
{
	private static final List<SetFact> SETS = buildSets();

	private GearSetSemanticRuleSet()
	{
	}

	public static LayoutRequest forEntries(List<LayoutEntry> entries)
	{
		return forEntries(entries, Integer.MAX_VALUE);
	}

	/**
	 * Builds the vertical-set request for the physically available gear-tail height. A set that
	 * lost a primary BIS member may otherwise be one item taller than the dense tail. When its
	 * owned remainder forms an exact rectangle, keep the equipment order in adjacent vertical
	 * columns instead of abandoning the family to dense fallback.
	 */
	public static LayoutRequest forEntries(List<LayoutEntry> entries, int maxVerticalHeight)
	{
		Objects.requireNonNull(entries, "entries");
		if (maxVerticalHeight < 1)
		{
			throw new IllegalArgumentException("maxVerticalHeight must be positive");
		}

		Set<Integer> present = new LinkedHashSet<>();
		for (LayoutEntry entry : entries)
		{
			present.add(entry.getItem().getItemId());
		}

		List<ItemSetCatalog.SetDefinition> verticalDefinitions = new ArrayList<>();
		List<SemanticRule> rules = new ArrayList<>();
		for (ItemSetCatalog.SetDefinition definition : definitions())
		{
			List<Integer> owned = new ArrayList<>();
			for (Integer itemId : definition.getItemIds())
			{
				if (present.contains(itemId))
				{
					owned.add(itemId);
				}
			}

			SemanticRule compact = owned.size() > maxVerticalHeight
				? compactRectangleRule(definition.getKey(), owned, maxVerticalHeight) : null;
			if (compact == null)
			{
				verticalDefinitions.add(definition);
			}
			else
			{
				rules.add(compact);
			}
		}

		SemanticRule rule = VerticalItemSetRuleFactory.build(
			"gear.vertical-sets", entries, verticalDefinitions);
		if (rule != null)
		{
			rules.add(rule);
		}
		return new LayoutRequest(entries, rules);
	}

	private static SemanticRule compactRectangleRule(String setKey, List<Integer> owned,
		int maxVerticalHeight)
	{
		int columns = 2;
		while (columns <= SemanticRule.MAX_WIDTH
			&& (owned.size() % columns != 0 || owned.size() / columns > maxVerticalHeight))
		{
			columns++;
		}
		if (columns > SemanticRule.MAX_WIDTH)
		{
			return null;
		}

		int rows = owned.size() / columns;
		List<SemanticAtom> atoms = new ArrayList<>(rows);
		for (int row = 0; row < rows; row++)
		{
			List<SemanticAtom.Member> members = new ArrayList<>(columns);
			for (int column = 0; column < columns; column++)
			{
				members.add(new SemanticAtom.Member("column-" + column,
					owned.get(column * rows + row)));
			}
			atoms.add(new SemanticAtom("row-" + row, members));
		}
		return SemanticRule.builder()
			.ruleKey("gear.compact." + setKey)
			.atoms(atoms)
			.confidenceTier(ConfidenceTier.HIGH)
			.shapePrimitive(ShapePrimitive.ROW_GROUP_MATRIX)
			.allowedWidths(Collections.singleton(columns))
			.build();
	}

	/**
	 * Returns real owned members of eligible vertical families. The gear setup planner uses this
	 * exact-ID set to keep non-primary family members out of arbitrary row-filler cells.
	 */
	public static Set<Integer> presentFamilyItemIds(List<BankPreviewItem> items)
	{
		Objects.requireNonNull(items, "items");
		Set<Integer> present = new LinkedHashSet<>();
		for (BankPreviewItem item : items)
		{
			present.add(item.getItemId());
		}

		Set<Integer> familyItems = new LinkedHashSet<>();
		for (SetFact set : SETS)
		{
			List<Integer> owned = new ArrayList<>();
			for (int itemId : set.itemIds)
			{
				if (present.contains(itemId))
				{
					owned.add(itemId);
				}
			}
			if (owned.size() >= 2)
			{
				familyItems.addAll(owned);
			}
		}
		return Collections.unmodifiableSet(familyItems);
	}

	private static List<SetFact> buildSets()
	{
		List<SetFact> sets = new ArrayList<>(Arrays.asList(
			set("gear.iron", 1153, 1323, 1115, 1191, 1067, 1081),
			set("gear.steel", 1157, 1325, 1119, 1193, 1069, 1083),
			set("gear.mithril", 1159, 1329, 1121, 1197, 1071, 1085),
			set("gear.adamant", 1161, 1331, 1123, 1199, 1073, 1091),
			set("gear.rune", 1163, 1333, 1127, 1201, 1079, 1093),
			set("gear.monk", 544, 542),
			set("gear.monk-gold", 20199, 20202),
			set("gear.monk-trimmed", 23303, 23306),
			set("gear.initiate", 5574, 5575, 5576),
			set("gear.proselyte", 9672, 9674, 9676, 9678),
			set("gear.sunfire-fanatic", 28933, 28936, 28939),
			set("gear.leather", 1167, 1129, 1095, 1063, 1061),
			set("gear.green-dhide", 1135, 1099, 1065, 22275),
			set("gear.blue-dhide", 2499, 2493, 2487),
			set("gear.red-dhide", 2501, 2495, 2489),
			set("gear.black-dhide", 2503, 2497, 2491, 22284),
			set("gear.blessed-ancient", 12496, 12492, 12494, 12490, 19921),
			set("gear.blessed-armadyl", 12512, 12508, 12510, 12506, 19930),
			set("gear.blessed-bandos", 12504, 12500, 12502, 12498, 19924),
			set("gear.blessed-guthix", 10382, 10378, 10380, 10376, 19927),
			set("gear.blessed-saradomin", 10390, 10386, 10388, 10384, 19933),
			set("gear.blessed-zamorak", 10374, 10370, 10372, 10368, 19936),
			set("gear.mixed-hide", 29280, 29283, 29289, 29286),
			set("gear.crystal", 23971, 23973, 23975, 23977, 23979, 23981),
			set("gear.masori-f", 27235, 27238, 27241),
			set("gear.xerician", 13385, 13387, 13389),
			set("gear.mystic-blue", 4089, 4091, 4093, 4095, 4097),
			set("gear.infinity", 6918, 6916, 6924, 6922, 6920),
			set("gear.bloodbark", 25413, 25404, 25416, 25407, 25410),
			set("gear.virtus", 26241, 26243, 26245),
			set("gear.ancestral", 21018, 21021, 21024),
			set("gear.eclipse-moon", 29010, 29035, 29000, 29004, 29031, 29007, 29033),
			set("gear.blood-moon", 29028, 29047, 28997, 29022, 29043, 29025, 29045),
			// The magic moon set was missing, so its pieces could never claim the
			// magic column and a lower-tier set kept winning it instead.
			set("gear.blue-moon", 29019, 29041, 28988, 29013, 29037, 29016, 29039),
			// Void was not a known family at all, so its pieces were left to the
			// dense tail and the gloves drifted away from the rest. The three
			// style helms share one body, legs and hands, so they stay in one
			// family rather than three: head, body, legs, hands.
			set("gear.void", 11665, 11664, 11663, 8839, 13072, 8840, 13073, 8842),
			set("gear.inquisitor", 24419, 24420, 24421),
			set("gear.torva", 26382, 26384, 26386)));

		addBarrowsFamily(sets, "ahrim",
			ids(4708, 4856, 4857, 4858, 4859, 4860),
			ids(4712, 4868, 4869, 4870, 4871, 4872),
			ids(4714, 4874, 4875, 4876, 4877, 4878),
			ids(4710, 4862, 4863, 4864, 4865, 4866));
		addBarrowsFamily(sets, "dharok",
			ids(4716, 4880, 4881, 4882, 4883, 4884),
			ids(4720, 4892, 4893, 4894, 4895, 4896),
			ids(4722, 4898, 4899, 4900, 4901, 4902),
			ids(4718, 4886, 4887, 4888, 4889, 4890));
		addBarrowsFamily(sets, "guthan",
			ids(4724, 4904, 4905, 4906, 4907, 4908),
			ids(4728, 4916, 4917, 4918, 4919, 4920),
			ids(4730, 4922, 4923, 4924, 4925, 4926),
			ids(4726, 4910, 4911, 4912, 4913, 4914));
		addBarrowsFamily(sets, "karil",
			ids(4732, 4928, 4929, 4930, 4931, 4932),
			ids(4736, 4940, 4941, 4942, 4943, 4944),
			ids(4738, 4946, 4947, 4948, 4949, 4950),
			ids(4734, 4934, 4935, 4936, 4937, 4938));
		addBarrowsFamily(sets, "torag",
			ids(4745, 4952, 4953, 4954, 4955, 4956),
			ids(4749, 4964, 4965, 4966, 4967, 4968),
			ids(4751, 4970, 4971, 4972, 4973, 4974),
			ids(4747, 4958, 4959, 4960, 4961, 4962));
		addBarrowsFamily(sets, "verac",
			ids(4753, 4976, 4977, 4978, 4979, 4980),
			ids(4757, 4988, 4989, 4990, 4991, 4992),
			ids(4759, 4994, 4995, 4996, 4997, 4998),
			ids(4755, 4982, 4983, 4984, 4985, 4986));

		Set<Integer> reserved = new LinkedHashSet<>();
		for (SetFact set : sets)
		{
			for (int itemId : set.itemIds) reserved.add(itemId);
		}
		for (ItemSetCatalog.SetDefinition definition : ItemSetCatalog.sets("gear"))
		{
			boolean overlaps = false;
			for (Integer itemId : definition.getItemIds())
			{
				if (reserved.contains(itemId))
				{
					overlaps = true;
					break;
				}
			}
			if (!overlaps)
			{
				int[] itemIds = new int[definition.getItemIds().size()];
				for (int index = 0; index < itemIds.length; index++)
				{
					itemIds[index] = definition.getItemIds().get(index);
					reserved.add(itemIds[index]);
				}
				sets.add(set(definition.getKey(), itemIds));
			}
		}
		return Collections.unmodifiableList(sets);
	}

	private static void addBarrowsFamily(List<SetFact> sets, String family, int[] heads,
		int[] bodies, int[] legs, int[] weapons)
	{
		int[] itemIds = new int[heads.length + bodies.length + legs.length + weapons.length];
		int offset = 0;
		for (int[] slot : Arrays.asList(heads, weapons, bodies, legs))
		{
			System.arraycopy(slot, 0, itemIds, offset, slot.length);
			offset += slot.length;
		}
		sets.add(set("gear.barrows-" + family, itemIds));
	}

	private static int[] ids(int... itemIds)
	{
		return itemIds;
	}

	private static List<ItemSetCatalog.SetDefinition> definitions()
	{
		List<ItemSetCatalog.SetDefinition> definitions = new ArrayList<>();
		for (SetFact set : SETS)
		{
			List<Integer> itemIds = new ArrayList<>(set.itemIds.length);
			for (int itemId : set.itemIds) itemIds.add(itemId);
			definitions.add(ItemSetCatalog.definition("gear", set.key, set.key, itemIds));
		}
		return definitions;
	}

	private static SetFact set(String key, int... itemIds)
	{
		return new SetFact(key, itemIds);
	}

	private static final class SetFact
	{
		private final String key;
		private final int[] itemIds;

		private SetFact(String key, int[] itemIds)
		{
			this.key = key;
			this.itemIds = itemIds;
		}
	}
}

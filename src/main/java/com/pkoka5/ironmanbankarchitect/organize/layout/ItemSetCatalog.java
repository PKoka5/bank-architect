package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Exact-ID set dictionary compiled from the reviewed user-supplied master workbook. */
public final class ItemSetCatalog
{
	private static final String RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/organize/item-set-catalog.tsv";
	private static final String SCHEMA_HEADER = "# schema=1";
	private static final String COSMETIC_FAMILY_DOMAIN = "cosmetic-family";
	private static final Map<String, List<SetDefinition>> SETS_BY_DOMAIN = load();
	private static final Map<Integer, String> DOMAIN_BY_ITEM_ID = indexDomains();
	private static final Map<Integer, String> COSMETIC_FAMILY_BY_ITEM_ID = indexCosmeticFamilies();
	private static final Map<Integer, Integer> COSMETIC_FAMILY_RANK_BY_ITEM_ID = indexCosmeticFamilyRanks();

	private ItemSetCatalog()
	{
	}

	/**
	 * The name of the colour family the item belongs to, if any. Recoloured
	 * cosmetics lead with a colour word, so a sorter that files them by name
	 * scatters each family; this lookup lets it file them by what they are.
	 */
	public static Optional<String> cosmeticFamilyOf(int itemId)
	{
		return Optional.ofNullable(COSMETIC_FAMILY_BY_ITEM_ID.get(itemId));
	}

	/**
	 * Where the item's colour family stands in the catalogue's declaration
	 * order, which is the curated display order: showpieces first, materials
	 * last. Items outside every family rank after all of them.
	 */
	public static int cosmeticFamilyRankOf(int itemId)
	{
		Integer rank = COSMETIC_FAMILY_RANK_BY_ITEM_ID.get(itemId);
		return rank == null ? Integer.MAX_VALUE : rank;
	}

	private static Map<Integer, String> indexCosmeticFamilies()
	{
		Map<Integer, String> families = new LinkedHashMap<>();
		for (SetDefinition definition : sets(COSMETIC_FAMILY_DOMAIN))
		{
			for (Integer itemId : definition.itemIds)
			{
				families.put(itemId, definition.name);
			}
		}
		return Collections.unmodifiableMap(families);
	}

	private static Map<Integer, Integer> indexCosmeticFamilyRanks()
	{
		Map<Integer, Integer> ranks = new LinkedHashMap<>();
		List<SetDefinition> definitions = sets(COSMETIC_FAMILY_DOMAIN);
		for (int rank = 0; rank < definitions.size(); rank++)
		{
			for (Integer itemId : definitions.get(rank).itemIds)
			{
				ranks.put(itemId, rank);
			}
		}
		return Collections.unmodifiableMap(ranks);
	}

	static List<SetDefinition> sets(String domain)
	{
		List<SetDefinition> sets = SETS_BY_DOMAIN.get(domain);
		return sets == null ? Collections.emptyList() : sets;
	}

	public static Optional<String> domainOf(int itemId)
	{
		return Optional.ofNullable(DOMAIN_BY_ITEM_ID.get(itemId));
	}

	private static Map<Integer, String> indexDomains()
	{
		Map<Integer, String> domains = new LinkedHashMap<>();
		for (Map.Entry<String, List<SetDefinition>> domain : SETS_BY_DOMAIN.entrySet())
		{
			for (SetDefinition definition : domain.getValue())
			{
				for (Integer itemId : definition.itemIds)
				{
					String earlier = domains.put(itemId, domain.getKey());
					if (earlier != null && !earlier.equals(domain.getKey()))
					{
						throw new IllegalStateException(
							"Item ID " + itemId + " appears in multiple set domains");
					}
				}
			}
		}
		return Collections.unmodifiableMap(domains);
	}

	static SetDefinition definition(String domain, String key, String name, List<Integer> itemIds)
	{
		return new SetDefinition(domain, key, name, new ArrayList<>(itemIds));
	}

	private static Map<String, List<SetDefinition>> load()
	{
		InputStream stream = ItemSetCatalog.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing item set catalog resource: " + RESOURCE_PATH);
		}

		Map<String, MutableSet> byKey = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (!SCHEMA_HEADER.equals(header))
			{
				throw new IllegalStateException("Unexpected item set catalog schema: " + header);
			}
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] columns = line.split("\t", -1);
				if (columns.length != 5)
				{
					throw new IllegalStateException("Malformed item set catalog row: " + line);
				}
				String domain = columns[0];
				String key = columns[1];
				String name = columns[2];
				int slotRank = Integer.parseInt(columns[3]);
				int itemId = Integer.parseInt(columns[4]);
				if (domain.isEmpty() || key.isEmpty() || name.isEmpty() || slotRank < 0 || itemId <= 0)
				{
					throw new IllegalStateException("Invalid item set catalog row: " + line);
				}
				String compoundKey = domain + "\u0000" + key;
				MutableSet set = byKey.computeIfAbsent(compoundKey,
					ignored -> new MutableSet(domain, key, name));
				if (!set.name.equals(name))
				{
					throw new IllegalStateException("Conflicting set name for " + key);
				}
				set.add(slotRank, itemId);
			}
		}
		catch (IOException | NumberFormatException e)
		{
			throw new IllegalStateException("Failed to read item set catalog", e);
		}

		Map<String, List<SetDefinition>> domains = new LinkedHashMap<>();
		Map<String, Set<Integer>> idsByDomain = new LinkedHashMap<>();
		for (MutableSet mutable : byKey.values())
		{
			SetDefinition definition = mutable.freeze();
			Set<Integer> domainIds = idsByDomain.computeIfAbsent(
				definition.domain, ignored -> new LinkedHashSet<>());
			for (int itemId : definition.itemIds)
			{
				if (!domainIds.add(itemId))
				{
					throw new IllegalStateException(
						"Item ID " + itemId + " appears in multiple " + definition.domain + " sets");
				}
			}
			domains.computeIfAbsent(definition.domain, ignored -> new ArrayList<>()).add(definition);
		}
		for (Map.Entry<String, List<SetDefinition>> entry : domains.entrySet())
		{
			entry.setValue(Collections.unmodifiableList(entry.getValue()));
		}
		return Collections.unmodifiableMap(domains);
	}

	static final class SetDefinition
	{
		private final String domain;
		private final String key;
		private final String name;
		private final List<Integer> itemIds;

		private SetDefinition(String domain, String key, String name, List<Integer> itemIds)
		{
			this.domain = domain;
			this.key = key;
			this.name = name;
			this.itemIds = Collections.unmodifiableList(itemIds);
		}

		String getKey()
		{
			return key;
		}

		String getName()
		{
			return name;
		}

		List<Integer> getItemIds()
		{
			return itemIds;
		}
	}

	private static final class MutableSet
	{
		private final String domain;
		private final String key;
		private final String name;
		private final List<Member> members = new ArrayList<>();
		private final Set<Integer> itemIds = new LinkedHashSet<>();

		private MutableSet(String domain, String key, String name)
		{
			this.domain = domain;
			this.key = key;
			this.name = name;
		}

		private void add(int slotRank, int itemId)
		{
			if (!itemIds.add(itemId))
			{
				throw new IllegalStateException("Duplicate item ID " + itemId + " in " + key);
			}
			members.add(new Member(slotRank, itemId, members.size()));
		}

		private SetDefinition freeze()
		{
			members.sort((left, right) ->
			{
				int bySlot = Integer.compare(left.slotRank, right.slotRank);
				return bySlot != 0 ? bySlot : Integer.compare(left.sourceOrder, right.sourceOrder);
			});
			List<Integer> ordered = new ArrayList<>(members.size());
			for (Member member : members)
			{
				ordered.add(member.itemId);
			}
			return new SetDefinition(domain, key, name, ordered);
		}
	}

	private static final class Member
	{
		private final int slotRank;
		private final int itemId;
		private final int sourceOrder;

		private Member(int slotRank, int itemId, int sourceOrder)
		{
			this.slotRank = slotRank;
			this.itemId = itemId;
			this.sourceOrder = sourceOrder;
		}
	}
}

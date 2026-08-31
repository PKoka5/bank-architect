package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Resolves owned items against the combat relationships catalog. */
final class CombatLoadoutResolver
{
	private CombatLoadoutResolver()
	{
	}

	static Relationships resolve(Map<Integer, BankPreviewItem> available,
		GearStatsSource gearStats)
	{
		return resolve(new CombatGearIndex(new ArrayList<>(available.values()), gearStats));
	}

	static Relationships resolve(CombatGearIndex gear)
	{
		List<Loadout> loadouts = new ArrayList<>();
		for (CombatGearFacts.LoadoutFact fact : CombatGearFacts.loadouts())
		{
			Loadout loadout = loadout(fact, gear);
			if (loadout != null)
			{
				loadouts.add(loadout);
			}
		}

		return new Relationships(loadouts, families(gear));
	}

	static List<Family> families(CombatGearIndex gear)
	{
		List<Family> families = new ArrayList<>();
		for (CombatGearFacts.FamilyFact fact : CombatGearFacts.families())
		{
			Family family = family(fact, gear.itemById());
			if (family != null)
			{
				families.add(family);
			}
		}
		return families;
	}

	private static Loadout loadout(CombatGearFacts.LoadoutFact fact,
		CombatGearIndex gear)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		Map<Integer, Integer> orderByItemId = new LinkedHashMap<>();
		Set<Integer> requiredItemIds = new LinkedHashSet<>();
		for (CombatGearFacts.Role role : fact.getRoles())
		{
			BankPreviewItem selected = null;
			for (int itemId : role.getItemIds())
			{
				BankPreviewItem candidate = gear.itemById().get(itemId);
				if (candidate != null && (selected == null
					|| gear.score(candidate) > gear.score(selected)))
				{
					selected = candidate;
				}
			}
			if (selected == null)
			{
				if (role.isRequired())
				{
					return null;
				}
				continue;
			}
			items.add(selected);
			orderByItemId.put(selected.getItemId(), role.getRole().sortOrder());
			if (role.isRequired())
			{
				requiredItemIds.add(selected.getItemId());
			}
		}
		items.sort(Comparator
			.comparingInt((BankPreviewItem item) -> orderByItemId.get(item.getItemId()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return new Loadout(fact.getKey(), fact.getStyle(), items, requiredItemIds);
	}

	private static Family family(CombatGearFacts.FamilyFact fact,
		Map<Integer, BankPreviewItem> available)
	{
		List<BankPreviewItem> items = new ArrayList<>();
		Map<Integer, Integer> orderByItemId = new LinkedHashMap<>();
		Map<Integer, GearRole> roleByItemId = new LinkedHashMap<>();
		Set<Integer> itemIds = new LinkedHashSet<>();
		int matchedRoles = 0;
		for (CombatGearFacts.Role role : fact.getRoles())
		{
			int roleRank = role.getRole().sortOrder();
			boolean matchedRole = false;
			for (int itemId : role.getItemIds())
			{
				BankPreviewItem item = available.get(itemId);
				if (item != null && itemIds.add(itemId))
				{
					items.add(item);
					orderByItemId.put(itemId, roleRank);
					roleByItemId.put(itemId, role.getRole());
					matchedRole = true;
				}
			}
			if (matchedRole)
			{
				matchedRoles++;
			}
		}
		if (matchedRoles < fact.getMinimumRoles())
		{
			return null;
		}
		items.sort(Comparator
			.comparingInt((BankPreviewItem item) -> orderByItemId.get(item.getItemId()))
			.thenComparingInt(BankPreviewItem::getItemId));
		return new Family(fact.getKey(), fact.getStyle(), items, roleByItemId, matchedRoles,
			fact.getMinimumRoles());
	}

	static final class Relationships
	{
		private final List<Loadout> loadouts;
		private final List<Family> families;

		private Relationships(List<Loadout> loadouts, List<Family> families)
		{
			this.loadouts = Collections.unmodifiableList(new ArrayList<>(loadouts));
			this.families = Collections.unmodifiableList(new ArrayList<>(families));
		}

		List<Loadout> loadouts()
		{
			return loadouts;
		}

		List<Family> families()
		{
			return families;
		}
	}

	static final class Loadout
	{
		private final String key;
		private final GearStyle style;
		private final List<BankPreviewItem> items;
		private final Set<Integer> requiredItemIds;

		private Loadout(String key, GearStyle style, List<BankPreviewItem> items,
			Set<Integer> requiredItemIds)
		{
			this.key = key;
			this.style = style;
			this.items = Collections.unmodifiableList(new ArrayList<>(items));
			this.requiredItemIds = Collections.unmodifiableSet(
				new LinkedHashSet<>(requiredItemIds));
		}

		String key()
		{
			return key;
		}

		GearStyle style()
		{
			return style;
		}

		List<BankPreviewItem> items()
		{
			return items;
		}

		Set<Integer> requiredItemIds()
		{
			return requiredItemIds;
		}

		Set<Integer> itemIds()
		{
			Set<Integer> itemIds = new LinkedHashSet<>();
			for (BankPreviewItem item : items)
			{
				itemIds.add(item.getItemId());
			}
			return itemIds;
		}
	}

	static final class Family
	{
		private final String key;
		private final GearStyle style;
		private final List<BankPreviewItem> items;
		private final Map<Integer, GearRole> roleByItemId;
		private final int matchedRoles;
		private final int minimumRoles;

		private Family(String key, GearStyle style, List<BankPreviewItem> items,
			Map<Integer, GearRole> roleByItemId,
			int matchedRoles, int minimumRoles)
		{
			this.key = key;
			this.style = style;
			this.items = Collections.unmodifiableList(new ArrayList<>(items));
			this.roleByItemId = Collections.unmodifiableMap(new LinkedHashMap<>(roleByItemId));
			this.matchedRoles = matchedRoles;
			this.minimumRoles = minimumRoles;
		}

		String key()
		{
			return key;
		}

		GearStyle style()
		{
			return style;
		}

		List<BankPreviewItem> items()
		{
			return items;
		}

		int matchedRoles()
		{
			return matchedRoles;
		}

		int minimumRoles()
		{
			return minimumRoles;
		}

		int roleCount(List<BankPreviewItem> selectedItems)
		{
			Set<GearRole> roles = new LinkedHashSet<>();
			for (BankPreviewItem item : selectedItems)
			{
				GearRole role = roleByItemId.get(item.getItemId());
				if (role != null)
				{
					roles.add(role);
				}
			}
			return roles.size();
		}

		Set<Integer> itemIds()
		{
			Set<Integer> result = new LinkedHashSet<>();
			for (BankPreviewItem item : items)
			{
				result.add(item.getItemId());
			}
			return result;
		}
	}
}

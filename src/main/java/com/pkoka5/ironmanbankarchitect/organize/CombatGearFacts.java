package com.pkoka5.ironmanbankarchitect.organize;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * OSRS relationships that equipment stats cannot express, such as required
 * set pieces, compatible weapon variants, and unusable lifecycle states.
 */
final class CombatGearFacts
{
	private static final String RESOURCE_PATH =
		"/com/pkoka5/ironmanbankarchitect/organize/combat-gear-facts.tsv";
	private static final String SCHEMA_HEADER = "# schema=1";
	private static volatile Catalog catalog;

	private CombatGearFacts()
	{
	}

	static List<LoadoutFact> loadouts()
	{
		return catalog().loadouts;
	}

	static Set<Integer> unusableItemIds()
	{
		return catalog().unusableItemIds;
	}

	static List<FamilyFact> families()
	{
		return catalog().families;
	}

	static Set<String> loadoutKeys()
	{
		return catalog().loadoutKeys;
	}

	private static Catalog catalog()
	{
		Catalog current = catalog;
		if (current != null)
		{
			return current;
		}
		synchronized (CombatGearFacts.class)
		{
			if (catalog == null)
			{
				catalog = loadResource();
			}
			return catalog;
		}
	}

	private static Catalog loadResource()
	{
		InputStream stream = CombatGearFacts.class.getResourceAsStream(RESOURCE_PATH);
		if (stream == null)
		{
			throw new IllegalStateException("Missing combat gear facts resource: " + RESOURCE_PATH);
		}
		return load(stream);
	}

	static Catalog load(InputStream stream)
	{
		if (stream == null)
		{
			throw new IllegalArgumentException("stream must not be null");
		}
		Map<String, MutableLoadout> byKey = new LinkedHashMap<>();
		Map<String, MutableFamily> familiesByKey = new LinkedHashMap<>();
		Set<Integer> unusableItemIds = new LinkedHashSet<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			if (!SCHEMA_HEADER.equals(header))
			{
				throw new IllegalStateException("Unexpected combat gear facts schema: " + header);
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
					throw new IllegalStateException("Malformed combat gear fact row: " + line);
				}

				String kind = columns[0];
				List<Integer> itemIds = itemIds(columns[4], line);
				if ("unusable".equals(kind))
				{
					for (int itemId : itemIds)
					{
						if (!unusableItemIds.add(itemId))
						{
							throw new IllegalStateException("Duplicate unusable item ID " + itemId);
						}
					}
					continue;
				}
				if ("family".equals(kind))
				{
					String key = columns[1];
					GearStyle style = style(columns[2], line);
					String roleValue = columns[3];
					if (key.isEmpty() || roleValue.isEmpty() || "-".equals(key) || "-".equals(roleValue))
					{
						throw new IllegalStateException("Missing family key or role: " + line);
					}
					requireKey(key, line);
					GearRole role = role(roleValue, line);
					MutableFamily family = familiesByKey.computeIfAbsent(key,
						ignored -> new MutableFamily(key, style, 2));
					if (family.style != style)
					{
						throw new IllegalStateException("Conflicting styles for family " + key);
					}
					family.add(role, itemIds);
					continue;
				}

				boolean required;
				if ("required".equals(kind))
				{
					required = true;
				}
				else if ("optional".equals(kind))
				{
					required = false;
				}
				else
				{
					throw new IllegalStateException("Unknown combat gear fact kind: " + kind);
				}

				String key = columns[1];
				GearStyle style = style(columns[2], line);
				String roleValue = columns[3];
				if (key.isEmpty() || roleValue.isEmpty() || "-".equals(key) || "-".equals(roleValue))
				{
					throw new IllegalStateException("Missing loadout key or role: " + line);
				}
				requireKey(key, line);
				GearRole role = role(roleValue, line);

				MutableLoadout loadout = byKey.computeIfAbsent(key,
					ignored -> new MutableLoadout(key, style));
				if (loadout.style != style)
				{
					throw new IllegalStateException("Conflicting styles for loadout " + key);
				}
				loadout.add(role, required, itemIds);
			}
		}
		catch (IOException | NumberFormatException e)
		{
			throw new IllegalStateException("Failed to read combat gear facts", e);
		}

		List<LoadoutFact> loadouts = new ArrayList<>();
		for (MutableLoadout loadout : byKey.values())
		{
			LoadoutFact frozen = loadout.freeze();
			loadouts.add(frozen);
			int requiredRoles = 0;
			for (Role role : frozen.roles)
			{
				if (role.required)
				{
					requiredRoles++;
				}
			}
			int minimumRoles = Math.min(3, requiredRoles);
			MutableFamily derived = familiesByKey.computeIfAbsent(loadout.key,
				ignored -> new MutableFamily(loadout.key, loadout.style, minimumRoles));
			for (Role role : frozen.roles)
			{
				if (role.required && !derived.rolesByType.containsKey(role.role))
				{
					derived.add(role.role, role.itemIds);
				}
			}
		}
		List<FamilyFact> families = new ArrayList<>();
		for (MutableFamily family : familiesByKey.values())
		{
			families.add(family.freeze());
		}
		return new Catalog(Collections.unmodifiableList(loadouts),
			Collections.unmodifiableList(families),
			Collections.unmodifiableSet(unusableItemIds),
			Collections.unmodifiableSet(new LinkedHashSet<>(byKey.keySet())));
	}

	private static List<Integer> itemIds(String column, String line)
	{
		List<Integer> itemIds = new ArrayList<>();
		Set<Integer> unique = new LinkedHashSet<>();
		for (String value : column.split(","))
		{
			int itemId = Integer.parseInt(value);
			if (itemId <= 0 || !unique.add(itemId))
			{
				throw new IllegalStateException("Invalid item IDs in combat gear fact row: " + line);
			}
			itemIds.add(itemId);
		}
		if (itemIds.isEmpty())
		{
			throw new IllegalStateException("Missing item IDs in combat gear fact row: " + line);
		}
		return itemIds;
	}

	private static GearStyle style(String value, String line)
	{
		try
		{
			return GearStyle.valueOf(value.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalStateException("Invalid combat style in row: " + line, e);
		}
	}

	private static GearRole role(String value, String line)
	{
		try
		{
			return GearRole.fromCatalogValue(value);
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalStateException("Invalid combat gear role in row: " + line, e);
		}
	}

	private static void requireKey(String key, String line)
	{
		if (!key.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
		{
			throw new IllegalStateException("Invalid combat gear key in row: " + line);
		}
	}

	static final class LoadoutFact
	{
		private final String key;
		private final GearStyle style;
		private final List<Role> roles;

		private LoadoutFact(String key, GearStyle style, List<Role> roles)
		{
			this.key = key;
			this.style = style;
			this.roles = roles;
		}

		String getKey()
		{
			return key;
		}

		GearStyle getStyle()
		{
			return style;
		}

		List<Role> getRoles()
		{
			return roles;
		}
	}

	static final class Role
	{
		private final GearRole role;
		private final boolean required;
		private final List<Integer> itemIds;

		private Role(GearRole role, boolean required, List<Integer> itemIds)
		{
			this.role = role;
			this.required = required;
			this.itemIds = Collections.unmodifiableList(new ArrayList<>(itemIds));
		}

		GearRole getRole()
		{
			return role;
		}

		boolean isRequired()
		{
			return required;
		}

		List<Integer> getItemIds()
		{
			return itemIds;
		}
	}

	static final class FamilyFact
	{
		private final String key;
		private final GearStyle style;
		private final List<Role> roles;
		private final int minimumRoles;

		private FamilyFact(String key, GearStyle style, List<Role> roles, int minimumRoles)
		{
			this.key = key;
			this.style = style;
			this.roles = roles;
			this.minimumRoles = minimumRoles;
		}

		String getKey()
		{
			return key;
		}

		GearStyle getStyle()
		{
			return style;
		}

		List<Role> getRoles()
		{
			return roles;
		}

		int getMinimumRoles()
		{
			return minimumRoles;
		}
	}

	private static final class MutableLoadout
	{
		private final String key;
		private final GearStyle style;
		private final List<Role> roles = new ArrayList<>();
		private final Map<GearRole, Role> rolesByType = new LinkedHashMap<>();
		private final Map<Integer, GearRole> roleByItemId = new LinkedHashMap<>();

		private MutableLoadout(String key, GearStyle style)
		{
			this.key = key;
			this.style = style;
		}

		private void add(GearRole role, boolean required, List<Integer> itemIds)
		{
			if (rolesByType.containsKey(role))
			{
				throw new IllegalStateException("Duplicate role " + role + " in " + key);
			}
			for (int itemId : itemIds)
			{
				GearRole previous = roleByItemId.putIfAbsent(itemId, role);
				if (previous != null)
				{
					throw new IllegalStateException("Item " + itemId + " belongs to both "
						+ previous + " and " + role + " in " + key);
				}
			}
			Role fact = new Role(role, required, itemIds);
			roles.add(fact);
			rolesByType.put(role, fact);
		}

		private LoadoutFact freeze()
		{
			boolean hasRequiredRole = false;
			for (Role role : roles)
			{
				hasRequiredRole |= role.required;
			}
			if (!hasRequiredRole)
			{
				throw new IllegalStateException("Loadout has no required role: " + key);
			}
			return new LoadoutFact(key, style,
				Collections.unmodifiableList(new ArrayList<>(roles)));
		}
	}

	private static final class MutableFamily
	{
		private final String key;
		private final GearStyle style;
		private final List<Role> roles = new ArrayList<>();
		private final Map<GearRole, Role> rolesByType = new LinkedHashMap<>();
		private final Map<Integer, GearRole> roleByItemId = new LinkedHashMap<>();
		private final int minimumRoles;

		private MutableFamily(String key, GearStyle style, int minimumRoles)
		{
			this.key = key;
			this.style = style;
			this.minimumRoles = minimumRoles;
		}

		private void add(GearRole role, List<Integer> itemIds)
		{
			if (rolesByType.containsKey(role))
			{
				throw new IllegalStateException("Duplicate role " + role + " in family " + key);
			}
			for (int itemId : itemIds)
			{
				GearRole previous = roleByItemId.putIfAbsent(itemId, role);
				if (previous != null)
				{
					throw new IllegalStateException("Item " + itemId + " belongs to both "
						+ previous + " and " + role + " in family " + key);
				}
			}
			Role fact = new Role(role, false, itemIds);
			roles.add(fact);
			rolesByType.put(role, fact);
		}

		private FamilyFact freeze()
		{
			if (roles.size() < minimumRoles)
			{
				throw new IllegalStateException("Family " + key + " requires " + minimumRoles
					+ " distinct roles but defines only " + roles.size());
			}
			return new FamilyFact(key, style,
				Collections.unmodifiableList(new ArrayList<>(roles)), minimumRoles);
		}
	}

	static final class Catalog
	{
		private final List<LoadoutFact> loadouts;
		private final List<FamilyFact> families;
		private final Set<Integer> unusableItemIds;
		private final Set<String> loadoutKeys;

		private Catalog(List<LoadoutFact> loadouts, List<FamilyFact> families,
			Set<Integer> unusableItemIds, Set<String> loadoutKeys)
		{
			this.loadouts = loadouts;
			this.families = families;
			this.unusableItemIds = unusableItemIds;
			this.loadoutKeys = loadoutKeys;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Static item name lists sourced from the Old School RuneScape Wiki
 * (oldschool.runescape.wiki, CC BY-NC-SA 3.0), baked in at build time so the
 * plugin never makes network calls at runtime.
 */
public final class WikiItemLists
{
	static final String SPECIAL_ATTACK_WEAPONS_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/special-attack-weapons.txt";
	static final String QUEST_ITEMS_PATH =
		"/com/pkoka5/ironmanbankarchitect/catalog/quest-items.txt";

	public static final WikiItemLists INSTANCE = new WikiItemLists();

	private final Set<String> specialAttackWeapons;
	private final Set<String> questItems;

	private WikiItemLists()
	{
		Set<String> weapons = new LinkedHashSet<>();
		for (String name : loadNames(SPECIAL_ATTACK_WEAPONS_PATH))
		{
			weapons.add(name);
			weapons.add(baseName(name));
		}
		this.specialAttackWeapons = Collections.unmodifiableSet(weapons);
		this.questItems = Collections.unmodifiableSet(new LinkedHashSet<>(loadNames(QUEST_ITEMS_PATH)));
	}

	/**
	 * Matches on the base name, so charged and poisoned variants such as
	 * "Dragon dagger(p++)" count as their special attack weapon.
	 */
	public boolean isSpecialAttackWeapon(String displayName)
	{
		return displayName != null && specialAttackWeapons.contains(baseName(displayName.toLowerCase()));
	}

	/**
	 * Exact name matches only: the wiki category also lists quest-specific
	 * variants of regular items ("Abyssal whip (My Arm's Big Adventure)"),
	 * which are filtered out of the resource to avoid false positives.
	 */
	public boolean isQuestItem(String displayName)
	{
		return displayName != null && questItems.contains(displayName.toLowerCase().trim());
	}

	static String baseName(String name)
	{
		String trimmed = name.trim();
		int parenthesis = trimmed.indexOf('(');
		if (parenthesis > 0)
		{
			trimmed = trimmed.substring(0, parenthesis);
		}

		return trimmed.trim();
	}

	private static Set<String> loadNames(String resourcePath)
	{
		InputStream stream = WikiItemLists.class.getResourceAsStream(resourcePath);
		if (stream == null)
		{
			throw new IllegalStateException("Missing wiki item list resource: " + resourcePath);
		}

		Set<String> names = new LinkedHashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#"))
				{
					continue;
				}
				if (!trimmed.isEmpty() && trimmed.charAt(0) == '\uFEFF')
				{
					trimmed = trimmed.substring(1);
				}

				names.add(trimmed.toLowerCase());
			}
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Failed to load wiki item list: " + resourcePath, ex);
		}

		return Collections.unmodifiableSet(names);
	}
}

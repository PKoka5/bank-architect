package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Reads and stores the player's assignment of categories to bank destinations.
 *
 * <p>Kept behind an interface so the layout screen can be built and tested
 * without a live config, in the same way the panel takes its analyze and reset
 * actions as callbacks. This replaces the earlier order-only model: arranging
 * the tabs is now one case of assigning categories to them, so a single screen
 * covers both rather than two screens disagreeing about the same thing.</p>
 */
interface BankLayoutModel
{
	default String editingContext() { return preset().getKey() + "|" + matchingProfile() + "|" + plan().serialize(); }
	default String defaultProfileName() { return BankLayoutProfiles.defaultName(preset()); }
	default void selectPreset(BankPresetType type) { }

	default List<String> presetChoices()
	{
		List<String> choices = new ArrayList<>(Arrays.asList("Ironman", "Main"));
		for (String name : profileNames())
			if (!defaultProfileName().equals(name)) choices.add(choiceLabel(name));
		return choices;
	}

	default String choiceLabel(String name)
	{
		String base = preset().getType() == BankPresetType.MAIN ? "Main" : "Ironman";
		return defaultProfileName().equals(name) ? base : base + ": " + name;
	}

	default String selectedPresetChoice() { return choiceLabel(matchingProfile()); }

	default void selectPresetChoice(String choice)
	{
		boolean main = choice.equals("Main") || choice.startsWith("Main: ");
		BankPresetType type = main ? BankPresetType.MAIN : BankPresetType.IRONMAN;
		if (!choice.contains(": ")) selectPreset(type);
		else
		{
			if (preset().getType() != type) selectPreset(type);
			selectProfile(choice.substring(choice.indexOf(": ") + 2));
		}
	}

	default void captureCurrentBank(String name,
		BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		completed.accept(false);
	}

	default void saveItemOrder(int tab, List<Integer> itemIds,
		BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		completed.accept(false);
	}

	default void saveBlueprintEdit(Map<Integer, List<Integer>> orders,
		Map<String, BlueprintItemOrders.Destination> transfers,
		BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		if (transfers.isEmpty() && orders.size() == 1)
		{
			Map.Entry<Integer, List<Integer>> entry = orders.entrySet().iterator().next();
			saveItemOrder(entry.getKey(), entry.getValue(), expected, expectedContext, completed);
		}
		else completed.accept(false);
	}
	/** The preset's own arrangement, with saving ignored. Used when no config is wired up. */
	BankLayoutModel DEFAULT = new BankLayoutModel()
	{
		@Override
		public BankPreset preset()
		{
			return BankPresets.IRONMAN;
		}

		@Override
		public BankLayoutPlan plan()
		{
			return BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		}

		@Override
		public void save(BankLayoutPlan plan)
		{
		}
	};

	/** Every category the blueprint knows, in the preset's own order. */
	BankPreset preset();

	/** Where the player currently has each category placed. */
	BankLayoutPlan plan();

	/** Stores a new plan and rebuilds the blueprint from it. */
	void save(BankLayoutPlan plan);

	/** Every saved layout, the bundled one first. */
	default List<String> profileNames()
	{
		return Collections.singletonList(defaultProfileName());
	}

	/** The active preset's saved name. Editing a bundled preset creates a custom. */
	default String matchingProfile()
	{
		return defaultProfileName();
	}

	/** Loads a saved layout as the working plan. */
	default void selectProfile(String name)
	{
	}

	/** Stores the working plan under a name, replacing any layout of that name. */
	default void saveProfile(String name, BankLayoutPlan plan)
	{
	}

	/** Forgets a saved layout. The bundled one cannot be forgotten. */
	default void deleteProfile(String name)
	{
	}

	/**
	 * The layout choices that are the player's taste rather than their plan.
	 *
	 * <p>They live beside the tab list rather than in the client's plugin
	 * settings because that is where their effect is: a player deciding how a tab
	 * should look should not have to leave the screen showing the tabs.</p>
	 */
	default BankLayoutOptions options()
	{
		return BankLayoutOptions.defaultFor(preset());
	}

	/** Stores the layout options and rebuilds the blueprint from them. */
	default void saveOptions(BankLayoutOptions options)
	{
	}

	/**
	 * Stores a tag's block order and rebuilds the blueprint from it. An empty
	 * list forgets the tag's arrangement, so its curated order stands again.
	 */
	default void saveBlockOrder(String tagKey, List<String> blockKeys)
	{
	}
}

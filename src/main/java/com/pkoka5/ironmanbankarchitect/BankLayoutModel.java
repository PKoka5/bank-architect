package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import java.util.Collections;
import java.util.List;

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
		return Collections.singletonList(BankLayoutProfiles.DEFAULT_NAME);
	}

	/**
	 * The saved layout the working plan currently matches, or empty when it
	 * matches none. An edit leaves the player on no profile rather than quietly
	 * rewriting the one they loaded, so switching back to it still returns the
	 * layout they saved.
	 */
	default String matchingProfile()
	{
		return BankLayoutProfiles.DEFAULT_NAME;
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
		return BankLayoutOptions.DEFAULTS;
	}

	/** Stores the layout options and rebuilds the blueprint from them. */
	default void saveOptions(BankLayoutOptions options)
	{
	}
}

package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;

/**
 * Reads the layout a plan implies, where a category can be arranged more than
 * one way.
 *
 * <p>These are decided from where the player put their tags rather than from a
 * separate switch. A layout has a reason to exist, and the reason is visible in
 * the plan: recipe rows only help while the doses are on hand to finish the
 * recipe. Asking the player to state the same thing twice, once by moving a tag
 * and once by ticking a box, is how the two end up disagreeing.</p>
 */
public final class BankLayoutStyles
{
	private static final String PART_DOSES = "potion-doses";
	private static final String FULL_POTIONS = "potions";

	private BankLayoutStyles()
	{
	}

	/**
	 * Whether the Herblore tab is laid out as a row per recipe.
	 *
	 * <p>It is, until the player moves their part doses onto the tab that holds
	 * the full potions. Doing that says they keep potions together rather than
	 * beside the herbs they came from, and the recipe rows left behind would be
	 * mostly empty cells. The tab then runs by kind instead: all the grimy herbs,
	 * then all the clean ones, and so on.</p>
	 */
	public static boolean herbloreUsesRecipeRows(BankLayoutPlan plan)
	{
		Objects.requireNonNull(plan, "plan");

		int doses = plan.destinationOf(PART_DOSES);
		int potions = plan.destinationOf(FULL_POTIONS);
		return doses < 0 || potions < 0 || doses != potions;
	}
}

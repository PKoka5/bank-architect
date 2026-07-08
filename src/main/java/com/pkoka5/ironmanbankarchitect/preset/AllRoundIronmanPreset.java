package com.pkoka5.ironmanbankarchitect.preset;

import com.pkoka5.ironmanbankarchitect.blueprint.BankProfile;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSection;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSlot;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintTab;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import java.util.Arrays;

public final class AllRoundIronmanPreset
{
	public static final String PROFILE_NAME = "Ironman — All-Round Bank";

	private AllRoundIronmanPreset()
	{
	}

	public static BankProfile create()
	{
		return new BankProfile("ironman.all-round", PROFILE_NAME, Arrays.asList(
			combatTab(),
			herbloreTab()
		));
	}

	private static BlueprintTab combatTab()
	{
		VisualBlock meleeSetup = new VisualBlock("melee-setup", "Melee Setup", "Gear set", Arrays.asList(
			BlueprintSlot.gearRole("melee.helmet", "Helmet"),
			BlueprintSlot.gearRole("melee.cape", "Cape"),
			BlueprintSlot.gearRole("melee.amulet", "Amulet"),
			BlueprintSlot.gearRole("melee.body", "Body"),
			BlueprintSlot.gearRole("melee.legs", "Legs"),
			BlueprintSlot.gearRole("melee.gloves", "Gloves"),
			BlueprintSlot.gearRole("melee.boots", "Boots"),
			BlueprintSlot.gearRole("melee.weapon", "Weapon")
		));

		return new BlueprintTab("combat-loadouts", "Combat & Loadouts", Arrays.asList(
			new BlueprintSection("core-setup-blocks", "Core setup blocks", Arrays.asList(meleeSetup))
		));
	}

	private static BlueprintTab herbloreTab()
	{
		VisualBlock iritRecipe = new VisualBlock("irit-super-attack", "Irit → Super attack", "Recipe", Arrays.asList(
			BlueprintSlot.workflowItem("herblore.irit.grimy", "Grimy irit"),
			BlueprintSlot.workflowItem("herblore.irit.clean", "Clean irit"),
			BlueprintSlot.workflowItem("herblore.irit.unfinished", "Irit potion (unf)"),
			BlueprintSlot.workflowItem("herblore.eye-of-newt", "Eye of newt"),
			BlueprintSlot.workflowItem("herblore.super-attack.4", "Super attack (4)"),
			BlueprintSlot.workflowItem("herblore.super-attack.3", "Super attack (3)"),
			BlueprintSlot.workflowItem("herblore.super-attack.2", "Super attack (2)"),
			BlueprintSlot.workflowItem("herblore.super-attack.1", "Super attack (1)")
		));

		return new BlueprintTab("herblore-farming", "Herblore & Farming", Arrays.asList(
			new BlueprintSection("potion-workflows", "Potion workflows", Arrays.asList(iritRecipe))
		));
	}
}

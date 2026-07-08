package com.pkoka5.ironmanbankarchitect.blueprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class AllRoundIronmanPresetTest
{
	@Test
	public void presetHasExactProfileName()
	{
		assertEquals("Ironman — All-Round Bank", AllRoundIronmanPreset.create().getName());
	}

	@Test
	public void presetHasExactTabAndSectionOrder()
	{
		BankProfile profile = AllRoundIronmanPreset.create();

		assertEquals(Arrays.asList("Combat & Loadouts", "Herblore & Farming"), names(profile.getTabs()));
		assertEquals("Core setup blocks", profile.getTabs().get(0).getSections().get(0).getName());
		assertEquals("Potion workflows", profile.getTabs().get(1).getSections().get(0).getName());
	}

	@Test
	public void presetHasStableKeysAtEveryHierarchyLevel()
	{
		BankProfile profile = AllRoundIronmanPreset.create();

		assertEquals("ironman.all-round", profile.getKey());

		BlueprintTab combatTab = profile.getTabs().get(0);
		BlueprintTab herbloreTab = profile.getTabs().get(1);
		assertEquals("combat-loadouts", combatTab.getKey());
		assertEquals("herblore-farming", herbloreTab.getKey());

		BlueprintSection coreSetupBlocks = combatTab.getSections().get(0);
		BlueprintSection potionWorkflows = herbloreTab.getSections().get(0);
		assertEquals("core-setup-blocks", coreSetupBlocks.getKey());
		assertEquals("potion-workflows", potionWorkflows.getKey());

		VisualBlock meleeSetup = coreSetupBlocks.getBlocks().get(0);
		VisualBlock iritSuperAttack = potionWorkflows.getBlocks().get(0);
		assertEquals("melee-setup", meleeSetup.getKey());
		assertEquals("irit-super-attack", iritSuperAttack.getKey());
	}

	@Test
	public void presetKeysAreUniqueAtEveryHierarchyLevel()
	{
		BankProfile profile = AllRoundIronmanPreset.create();

		assertUniqueKeys(profile.getTabs().stream().map(BlueprintTab::getKey).collect(Collectors.toList()));

		for (BlueprintTab tab : profile.getTabs())
		{
			assertUniqueKeys(tab.getSections().stream().map(BlueprintSection::getKey).collect(Collectors.toList()));

			for (BlueprintSection section : tab.getSections())
			{
				assertUniqueKeys(section.getBlocks().stream().map(VisualBlock::getKey).collect(Collectors.toList()));
			}
		}
	}

	@Test
	public void combatBlockHasExactSlotOrderAndUniqueKeys()
	{
		VisualBlock block = AllRoundIronmanPreset.create()
			.getTabs().get(0)
			.getSections().get(0)
			.getBlocks().get(0);

		assertEquals("Melee Setup", block.getName());
		assertEquals("Gear set", block.getTypeLabel());
		assertEquals(Arrays.asList("Helmet", "Cape", "Amulet", "Body", "Legs", "Gloves", "Boots", "Weapon"), slotLabels(block));
		assertEquals(Arrays.asList("melee.helmet", "melee.cape", "melee.amulet", "melee.body", "melee.legs", "melee.gloves", "melee.boots", "melee.weapon"), slotKeys(block));
		assertEquals(Arrays.asList(SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE, SlotKind.GEAR_ROLE), slotKinds(block));
		assertUniqueSlotKeys(block);
	}

	@Test
	public void herbloreBlockHasExactSlotOrderAndUniqueKeys()
	{
		VisualBlock block = AllRoundIronmanPreset.create()
			.getTabs().get(1)
			.getSections().get(0)
			.getBlocks().get(0);

		assertEquals("Irit → Super attack", block.getName());
		assertEquals("Recipe", block.getTypeLabel());
		assertEquals(Arrays.asList(
			"Irit seed",
			"Grimy irit",
			"Clean irit",
			"Irit potion (unf)",
			"Eye of newt",
			"Super attack (3)",
			"Super attack (2)",
			"Super attack (1)"
		), slotLabels(block));
		assertEquals(Arrays.asList(
			"herblore.irit.seed",
			"herblore.irit.grimy",
			"herblore.irit.clean",
			"herblore.irit.unf",
			"herblore.irit.secondary",
			"herblore.super-attack.3",
			"herblore.super-attack.2",
			"herblore.super-attack.1"
		), slotKeys(block));
		assertEquals(Arrays.asList(
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM,
			SlotKind.WORKFLOW_ITEM
		), slotKinds(block));
		assertUniqueSlotKeys(block);
	}

	@Test
	public void emptySlotPreservesKeyLabelOrderAndKind()
	{
		VisualBlock block = new VisualBlock("future-block", "Future Block", "Reserved", Arrays.asList(
			BlueprintSlot.gearRole("future.before", "Before"),
			BlueprintSlot.empty("Future upgrade"),
			BlueprintSlot.workflowItem("future.after", "After")
		));

		BlueprintSlot empty = block.getSlots().get(1);
		assertEquals("empty.future-upgrade", empty.getKey());
		assertEquals("Future upgrade", empty.getLabel());
		assertSame(SlotKind.EMPTY, empty.getKind());
		assertEquals(Arrays.asList("Before", "Future upgrade", "After"), slotLabels(block));
	}

	private static void assertUniqueSlotKeys(VisualBlock block)
	{
		Set<String> keys = new HashSet<>(slotKeys(block));
		assertEquals(block.getSlots().size(), keys.size());
	}

	private static void assertUniqueKeys(List<String> keys)
	{
		assertEquals(keys.size(), new HashSet<>(keys).size());
	}

	private static List<String> names(List<BlueprintTab> tabs)
	{
		return tabs.stream()
			.map(BlueprintTab::getName)
			.collect(Collectors.toList());
	}

	private static List<String> slotLabels(VisualBlock block)
	{
		return block.getSlots().stream()
			.map(BlueprintSlot::getLabel)
			.collect(Collectors.toList());
	}

	private static List<String> slotKeys(VisualBlock block)
	{
		return block.getSlots().stream()
			.map(BlueprintSlot::getKey)
			.collect(Collectors.toList());
	}

	private static List<SlotKind> slotKinds(VisualBlock block)
	{
		return block.getSlots().stream()
			.map(BlueprintSlot::getKind)
			.collect(Collectors.toList());
	}
}

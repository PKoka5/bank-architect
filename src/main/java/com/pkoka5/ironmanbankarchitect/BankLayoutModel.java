package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.function.Consumer;

/** Reads and stores the player's blueprint and category assignments. */
interface BankLayoutModel
{
	BankPreset preset();
	BankLayoutPlan plan();
	void save(BankLayoutPlan plan);
	void moveTab(int from, int to, String expectedContext, Consumer<Boolean> completed);
	String editingContext();
	void selectPreset(BankPresetType type);
	List<String> presetChoices();
	List<String> profileNames();
	String matchingProfile();
	void selectProfile(String name);
	void saveProfile(String name, BankLayoutPlan plan);
	void deleteProfile(String name);
	BankLayoutOptions options();
	default String rememberedBlueprint() { return ""; }
	default String acceptedBlueprint() { return ""; }
	default BankOrganizationPreview proposedBlueprint() { return null; }
	default String blueprintState() { return AcceptedBlueprint.description(acceptedBlueprint(), ""); }
	default String reviewReference()
	{
		return acceptedBlueprint().isEmpty() ? rememberedBlueprint() : AcceptedBlueprint.reference(acceptedBlueprint());
	}
	default void acceptBlueprint(BankOrganizationPreview expected, BankOrganizationPreview proposal,
		String expectedContext, Consumer<Boolean> completed) { completed.accept(false); }
	default void rememberBlueprint(BankOrganizationPreview expected, String expectedContext, Consumer<Boolean> completed)
	{
		completed.accept(false);
	}
	void saveOptions(BankLayoutOptions options);
	void saveBlockOrder(String tagKey, List<String> blockKeys);
	void captureCurrentBank(String name, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed);
	void saveItemOrder(int tab, List<Integer> itemIds, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed);
	void saveBlueprintEdit(Map<Integer, List<Integer>> orders,
		Map<String, BlueprintItemOrders.Destination> transfers, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed);

	default String defaultProfileName() { return BankLayoutProfiles.defaultName(preset()); }

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
}

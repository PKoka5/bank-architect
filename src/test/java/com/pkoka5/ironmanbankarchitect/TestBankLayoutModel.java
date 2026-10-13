package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JLabel;

/** Lightweight model and panel fixtures; the plugin always supplies its live model. */
abstract class TestBankLayoutModel implements BankLayoutModel
{
	static final TestBankLayoutModel DEFAULT = new TestBankLayoutModel()
	{
		@Override public BankPreset preset() { return BankPresets.IRONMAN; }
		@Override public BankLayoutPlan plan() { return BankLayoutPlan.defaultFor(preset()); }
		@Override public void save(BankLayoutPlan plan) { }
	};

	@Override public String editingContext()
	{
		return preset().getKey() + "|" + matchingProfile() + "|" + plan().serialize();
	}

	@Override public void selectPreset(BankPresetType type) { }

	@Override public List<String> presetChoices()
	{
		List<String> choices = new ArrayList<>(Arrays.asList("Ironman", "Main"));
		for (String name : profileNames())
			if (!defaultProfileName().equals(name)) choices.add(choiceLabel(name));
		return choices;
	}

	@Override public void captureCurrentBank(String name, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		completed.accept(false);
	}

	@Override public void saveItemOrder(int tab, List<Integer> itemIds, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		completed.accept(false);
	}

	@Override public void saveBlueprintEdit(Map<Integer, List<Integer>> orders,
		Map<String, BlueprintItemOrders.Destination> transfers, BankOrganizationPreview expected,
		String expectedContext, Consumer<Boolean> completed)
	{
		if (transfers.isEmpty() && orders.size() == 1)
		{
			Map.Entry<Integer, List<Integer>> entry = orders.entrySet().iterator().next();
			saveItemOrder(entry.getKey(), entry.getValue(), expected, expectedContext, completed);
		}
		else completed.accept(false);
	}

	@Override public List<String> profileNames() { return Collections.singletonList(defaultProfileName()); }
	@Override public String matchingProfile() { return defaultProfileName(); }
	@Override public void selectProfile(String name) { }
	@Override public void saveProfile(String name, BankLayoutPlan plan) { }
	@Override public void deleteProfile(String name) { }
	@Override public BankLayoutOptions options() { return BankLayoutOptions.defaultFor(preset()); }
	@Override public void saveOptions(BankLayoutOptions options) { }
	@Override public void saveBlockOrder(String tagKey, List<String> blockKeys) { }

	static IronmanBankArchitectPanel panel(BankGuideController controller)
	{
		return panel(controller, () -> {});
	}

	static IronmanBankArchitectPanel panel(BankGuideController controller, Runnable analyze)
	{
		return panel(controller, analyze, (item, label) -> {});
	}

	static IronmanBankArchitectPanel panel(BankGuideController controller, Runnable analyze,
		BiConsumer<BankPreviewItem, JLabel> renderer)
	{
		return panel(controller, analyze, renderer, () -> {});
	}

	static IronmanBankArchitectPanel panel(BankGuideController controller, Runnable analyze,
		BiConsumer<BankPreviewItem, JLabel> renderer, Runnable reset)
	{
		return panel(controller, analyze, renderer, reset, DEFAULT);
	}

	static IronmanBankArchitectPanel panel(BankGuideController controller, Runnable analyze,
		BiConsumer<BankPreviewItem, JLabel> renderer, Runnable reset, BankLayoutModel model)
	{
		return new IronmanBankArchitectPanel(controller, analyze, renderer, reset, model);
	}
}

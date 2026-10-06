package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.GearLayout;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PresetSettingsTest
{
	@Test
	public void switchingToMainPreservesEveryLegacyIronmanStateValue()
	{
		Fixture fixture = new Fixture();
		for (String key : Arrays.asList("categoryOverrides", "tabOrder", "blockOrders",
			"blockOrdersByProfile", "blueprintOrdersByProfile", "layoutProfiles", "activeLayoutProfile"))
			fixture.values.put(key, "ironman-" + key);
		Map<String, String> original = new LinkedHashMap<>(fixture.values);

		fixture.preset.set(BankPresets.MAIN);
		for (String key : original.keySet())
		{
			assertEquals("", fixture.settings.get(key, ""));
			fixture.settings.set(key, "main-" + key);
		}

		for (Map.Entry<String, String> entry : original.entrySet())
			assertEquals(entry.getValue(), fixture.values.get(entry.getKey()));
		fixture.preset.set(BankPresets.IRONMAN);
		for (Map.Entry<String, String> entry : original.entrySet())
			assertEquals(entry.getValue(), fixture.settings.get(entry.getKey(), ""));
	}

	@Test
	public void presetsRetainIndependentLayoutOptionsAfterRestart()
	{
		Fixture fixture = new Fixture();
		fixture.settings.set("gearLayout", "GRID_STYLES");
		fixture.settings.set("fillHerbloreRows", true);
		fixture.settings.set("keepDoseRows", false);
		fixture.settings.set("potionDoses", "GRAB_AREA");
		fixture.preset.set(BankPresets.MAIN);
		fixture.settings.set("gearLayout", "SETS");
		fixture.settings.set("fillHerbloreRows", false);
		fixture.settings.set("keepDoseRows", true);
		fixture.settings.set("potionDoses", "BY_FAMILY");
		PresetSettings restarted = fixture.newSettings();

		assertEquals("SETS", restarted.get("gearLayout", ""));
		assertEquals("false", restarted.get("fillHerbloreRows", ""));
		assertEquals("true", restarted.get("keepDoseRows", ""));
		assertEquals("BY_FAMILY", restarted.get("potionDoses", ""));
		fixture.preset.set(BankPresets.IRONMAN);
		assertEquals("GRID_STYLES", restarted.get("gearLayout", ""));
		assertEquals("true", restarted.get("fillHerbloreRows", ""));
		assertEquals("false", restarted.get("keepDoseRows", ""));
		assertEquals("GRAB_AREA", restarted.get("potionDoses", ""));
	}

	@Test
	public void mainResetDoesNotClearIronmanCorrectionsOrCapturedOrder()
	{
		Fixture fixture = new Fixture();
		fixture.settings.set("categoryOverrides", "20724=gear");
		fixture.settings.set("blueprintOrdersByProfile", "captured-ironman-layout");
		fixture.preset.set(BankPresets.MAIN);
		fixture.settings.set("categoryOverrides", "20724=food");
		fixture.settings.set("blueprintOrdersByProfile", "captured-main-layout");
		fixture.settings.set("categoryOverrides", "");
		fixture.settings.set("blueprintOrdersByProfile", "");

		assertEquals("", fixture.settings.get("categoryOverrides", "default"));
		assertEquals("", fixture.settings.get("blueprintOrdersByProfile", "default"));
		fixture.preset.set(BankPresets.IRONMAN);
		assertEquals("20724=gear", fixture.settings.get("categoryOverrides", ""));
		assertEquals("captured-ironman-layout", fixture.settings.get("blueprintOrdersByProfile", ""));
	}

	@Test
	public void missingMainValuesUseMainDefaultsWithoutFallingBackToIronman()
	{
		Fixture fixture = new Fixture();
		fixture.settings.set("potionDoses", "GRAB_AREA");
		fixture.preset.set(BankPresets.MAIN);

		assertEquals("BY_FAMILY", fixture.settings.get("potionDoses", "BY_FAMILY"));
		assertFalse(fixture.values.containsKey("main.potionDoses"));
		fixture.settings.set("tabOrder", "");
		assertEquals("", fixture.settings.get("tabOrder", "bundled-main-plan"));
	}

	@Test
	public void sameNamedLayoutsAndKeepCurrentOrderPlansRemainIndependent()
	{
		Fixture fixture = new Fixture();
		fixture.settings.set("activeLayoutProfile", "My bank");
		fixture.settings.set("layoutProfiles", "My bank~ironman-plan");
		fixture.settings.set("tabOrder", "ironman-plan|k:2");
		fixture.settings.set("blockOrdersByProfile", "My bank~ironman-blocks");
		fixture.preset.set(BankPresets.MAIN);
		fixture.settings.set("activeLayoutProfile", "My bank");
		fixture.settings.set("layoutProfiles", "My bank~main-plan");
		fixture.settings.set("tabOrder", "main-plan|k:4");
		fixture.settings.set("blockOrdersByProfile", "My bank~main-blocks");

		assertEquals("My bank", fixture.settings.get("activeLayoutProfile", ""));
		assertEquals("My bank~main-plan", fixture.settings.get("layoutProfiles", ""));
		assertEquals("main-plan|k:4", fixture.settings.get("tabOrder", ""));
		assertEquals("My bank~main-blocks", fixture.settings.get("blockOrdersByProfile", ""));
		fixture.preset.set(BankPresets.IRONMAN);
		assertEquals("My bank", fixture.settings.get("activeLayoutProfile", ""));
		assertEquals("My bank~ironman-plan", fixture.settings.get("layoutProfiles", ""));
		assertEquals("ironman-plan|k:2", fixture.settings.get("tabOrder", ""));
		assertEquals("My bank~ironman-blocks", fixture.settings.get("blockOrdersByProfile", ""));
	}

	@Test
	public void typedPreferencesRoundTripIndependentlyAndUnknownEnumsUseThePresetDefault()
	{
		Fixture fixture = new Fixture();
		fixture.settings.set("gearLayout", GearLayout.LIST);
		fixture.settings.set("keepDoseRows", false);
		fixture.preset.set(BankPresets.MAIN);
		assertEquals(GearLayout.GRID_SETS, fixture.settings.get("gearLayout", GearLayout.GRID_SETS));
		fixture.settings.set("gearLayout", GearLayout.GRID_SETS);
		fixture.settings.set("keepDoseRows", true);
		assertEquals(GearLayout.GRID_SETS, fixture.settings.get("gearLayout", GearLayout.GRID_STYLES));
		assertEquals(true, fixture.settings.get("keepDoseRows", false));
		fixture.settings.set("gearLayout", "FUTURE_LAYOUT");
		assertEquals(GearLayout.GRID_SETS, fixture.settings.get("gearLayout", GearLayout.GRID_SETS));
		fixture.preset.set(BankPresets.IRONMAN);
		assertEquals(GearLayout.LIST, fixture.settings.get("gearLayout", GearLayout.GRID_STYLES));
		assertEquals(false, fixture.settings.get("keepDoseRows", true));
	}

	private static final class Fixture
	{
		private final Map<String, String> values = new LinkedHashMap<>();
		private final AtomicReference<BankPreset> preset = new AtomicReference<>(BankPresets.IRONMAN);
		private final PresetSettings settings = newSettings();

		private PresetSettings newSettings()
		{
			return new PresetSettings((key, fallback) -> values.getOrDefault(key, fallback),
				(key, value) -> values.put(key, value instanceof Enum ? ((Enum<?>) value).name() : value.toString()), preset::get);
		}
	}
}

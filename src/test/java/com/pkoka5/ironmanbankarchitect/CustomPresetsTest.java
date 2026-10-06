package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class CustomPresetsTest
{
	@Test public void changingLayoutForksOnceAndBuiltinAlwaysRestoresItsOriginalPlan() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		String original = fixture.model.plan().serialize();
		fixture.model.save(fixture.model.plan().withTagAt("runes", 7));
		String choice = fixture.model.selectedPresetChoice();
		assertEquals("Ironman: Custom layout", choice);
		fixture.model.save(fixture.model.plan().withTagAt("food", 8));
		String edited = fixture.model.plan().serialize();
		assertEquals(choice, fixture.model.selectedPresetChoice());
		assertEquals(Arrays.asList("Ironman", "Main", choice), fixture.model.presetChoices());
		fixture.model.selectPresetChoice("Ironman");
		assertEquals(original, fixture.model.plan().serialize());
		fixture.model.selectPresetChoice(choice);
		assertEquals(edited, fixture.model.plan().serialize());
		Fixture restarted = new Fixture(fixture.values);
		assertEquals(choice, restarted.model.selectedPresetChoice());
		assertEquals(edited, restarted.model.plan().serialize());
	}

	@Test public void bothBundledPresetsUseBISAndKeepTheirOriginalIndependentDefaults() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		assertEquals(GearLayout.GRID_STYLES, fixture.model.options().gearLayout());
		assertTrue(fixture.model.options().fillHerbloreRows());
		assertTrue(fixture.model.options().gatherFrequentlyUsed());
		assertTrue(fixture.model.options().alchPile());
		fixture.model.selectPresetChoice("Main");
		assertEquals(GearLayout.GRID_STYLES, fixture.model.options().gearLayout());
		assertFalse(fixture.model.options().fillHerbloreRows());
		assertFalse(fixture.model.options().gatherFrequentlyUsed());
		assertTrue(fixture.model.options().alchPile());
		assertEquals(PotionDoseOrder.BY_FAMILY, fixture.model.options().potionDoses());
		assertEquals(1, fixture.model.plan().destinationOf("runes"));
		assertEquals(2, fixture.model.plan().destinationOf("gear"));
	}

	@Test public void optionsCorrectionsAndBlockEditsStayWithTheirCustomAndSurviveRestart() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.saveOptions(new BankLayoutOptions(true, false, false, Collections.emptyMap(),
			GearLayout.LIST, PotionDoseOrder.BY_FAMILY, RuneOrder.ELEMENTAL, TeleportOrder.ALPHABETICAL, false));
		String choice = fixture.model.selectedPresetChoice();
		fixture.assign(9084, "frequently-used");
		fixture.model.saveBlockOrder("cosmetics", Collections.singletonList("item:1042"));
		String blocks = fixture.model.options().blockArrangements().serialize();
		fixture.model.selectPresetChoice("Ironman");
		assertEquals(GearLayout.GRID_STYLES, fixture.model.options().gearLayout());
		assertEquals("", fixture.corrections());
		assertEquals("", fixture.model.options().blockArrangements().serialize());
		fixture.model.selectPresetChoice(choice);
		assertEquals(GearLayout.LIST, fixture.model.options().gearLayout());
		assertFalse(fixture.model.options().gatherFrequentlyUsed());
		assertEquals("9084=frequently-used", fixture.corrections());
		assertEquals(blocks, fixture.model.options().blockArrangements().serialize());
		Fixture restarted = new Fixture(fixture.values);
		assertEquals(GearLayout.LIST, restarted.model.options().gearLayout());
		assertEquals("9084=frequently-used", restarted.corrections());
		assertEquals(blocks, restarted.model.options().blockArrangements().serialize());
	}

	@Test public void correctionAndBlockEditEachForkBundledPresetWithoutChangingItsDefaults() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.assign(9084, "frequently-used");
		String corrected = fixture.model.selectedPresetChoice();
		assertEquals("Ironman: Custom layout", corrected);
		fixture.model.selectPresetChoice("Ironman");
		fixture.model.saveBlockOrder("cosmetics", Collections.singletonList("item:1042"));
		assertEquals("Ironman: Custom layout 2", fixture.model.selectedPresetChoice());
		assertEquals("", fixture.corrections());
		fixture.model.selectPresetChoice(corrected);
		assertEquals("9084=frequently-used", fixture.corrections());
		assertEquals("", fixture.model.options().blockArrangements().serialize());
	}

	@Test public void sameNamedCustomsAreListedAndSelectedAcrossBothParentsWithoutLeaking() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.saveProfile("My bank", fixture.model.plan().withTagAt("runes", 7));
		fixture.assign(9084, "frequently-used");
		fixture.model.selectPresetChoice("Main");
		fixture.model.saveProfile("My bank", fixture.model.plan().withTagAt("food", 8));
		fixture.assign(9084, "teleports");
		assertEquals(Arrays.asList("Ironman", "Main", "Ironman: My bank", "Main: My bank"), fixture.model.presetChoices());
		fixture.model.selectPresetChoice("Ironman: My bank");
		assertEquals(7, fixture.model.plan().destinationOf("runes"));
		assertEquals("9084=frequently-used", fixture.corrections());
		fixture.model.selectPresetChoice("Main: My bank");
		assertEquals(1, fixture.model.plan().destinationOf("runes"));
		assertEquals(8, fixture.model.plan().destinationOf("food"));
		assertEquals("9084=teleports", fixture.corrections());
		fixture.model.selectPresetChoice("Main");
		assertEquals("", fixture.corrections());
		assertEquals(3, fixture.model.plan().destinationOf("food"));
	}

	@Test public void savedCustomPlanWithMissingLegacyWorkingPlanIsPreservedOnMigration() throws Exception
	{
		Map<String, String> values = new LinkedHashMap<>();
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("runes", 7);
		values.put("layoutProfiles", BankLayoutProfiles.parse("", "").withProfile("Old bank", plan.serialize()).serialize());
		values.put("activeLayoutProfile", "Old bank");
		values.put("categoryOverrides", "9084=frequently-used");
		Fixture fixture = new Fixture(values);
		assertEquals("Ironman: Old bank", fixture.model.selectedPresetChoice());
		assertEquals(plan.serialize(), fixture.model.plan().serialize());
		assertEquals("9084=frequently-used", fixture.corrections());
		fixture.model.selectPresetChoice("Ironman");
		assertEquals(BankLayoutPlan.defaultFor(BankPresets.IRONMAN).serialize(), fixture.model.plan().serialize());
		assertEquals("", fixture.corrections());
	}

	@Test public void deletingActiveCustomReturnsToBuiltinAndKeepsOtherCustoms() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.saveProfile("A", fixture.model.plan().withTagAt("runes", 7));
		fixture.model.saveProfile("B", fixture.model.plan().withTagAt("food", 8));
		fixture.model.deleteProfile("B");
		assertEquals("Ironman", fixture.model.selectedPresetChoice());
		assertEquals(Arrays.asList("Ironman", "Main", "Ironman: A"), fixture.model.presetChoices());
		fixture.model.selectPresetChoice("Ironman: A");
		assertEquals(7, fixture.model.plan().destinationOf("runes"));
	}

	@Test public void fullProfileListRefusesSaveAsWithoutChangingAnyStoredSetting() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		for (int index = 0; index < 20; index++) fixture.model.saveProfile("Saved " + index, fixture.model.plan());
		Map<String, String> before = new LinkedHashMap<>(fixture.values);
		fixture.model.saveProfile("Another", fixture.model.plan().withTagAt("food", 8));
		assertEquals(before, fixture.values);
	}

	@Test public void runeliteOptionChangeOnMainForksAndUsesTheEventValue() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.selectPresetChoice("Main");
		field(fixture.plugin, "guideController", new BankGuideController(AllRoundIronmanPreset.create()));
		ConfigChanged event = new ConfigChanged();
		event.setGroup(IronmanBankArchitectConfig.GROUP);
		event.setKey("gearLayout");
		event.setOldValue("GRID_STYLES");
		event.setNewValue("LIST");
		fixture.plugin.onConfigChanged(event);
		assertEquals("Main: Custom layout", fixture.model.selectedPresetChoice());
		assertEquals(GearLayout.LIST, fixture.model.options().gearLayout());
		fixture.model.selectPresetChoice("Main");
		assertEquals(GearLayout.GRID_STYLES, fixture.model.options().gearLayout());
		event.setOldValue("LIST");
		fixture.plugin.onConfigChanged(event);
		assertEquals("Main", fixture.model.selectedPresetChoice());
	}

	private static void field(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static final class Fixture
	{
		private final Map<String, String> values;
		private final IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
		private final BankLayoutModel model;
		@SuppressWarnings({"unchecked", "rawtypes"})
		private Fixture(Map<String, String> values) throws Exception
		{
			this.values = values;
			IronmanBankArchitectConfig config = (IronmanBankArchitectConfig) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[]{IronmanBankArchitectConfig.class}, (proxy, method, args) -> {
					String name = method.getName();
					if (name.startsWith("set"))
					{
						values.put(Character.toLowerCase(name.charAt(3)) + name.substring(4), args[0].toString());
						return null;
					}
					if (values.containsKey(name))
					{
						String value = values.get(name);
						if (method.getReturnType() == boolean.class) return Boolean.parseBoolean(value);
						if (method.getReturnType().isEnum()) return Enum.valueOf((Class) method.getReturnType(), value);
						return value;
					}
					return method.isDefault() ? MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
						.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy).invokeWithArguments() : null;
				});
			field(plugin, "config", config);
			PresetTestSettings.attach(plugin, config, values);
			Method factory = IronmanBankArchitectPlugin.class.getDeclaredMethod("bankLayoutModel");
			factory.setAccessible(true);
			model = (BankLayoutModel) factory.invoke(plugin);
		}
		private void assign(int id, String category) throws Exception
		{
			Method method = IronmanBankArchitectPlugin.class.getDeclaredMethod("applyCategoryOverride", int.class, String.class, String.class);
			method.setAccessible(true);
			method.invoke(plugin, id, "Item", category);
		}
		private String corrections() throws Exception
		{
			Method method = IronmanBankArchitectPlugin.class.getDeclaredMethod("categoryOverrides");
			method.setAccessible(true);
			return ((UserCategoryOverrides) method.invoke(plugin)).serialize();
		}
	}
}

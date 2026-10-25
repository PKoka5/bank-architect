package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;

/** The real plugin stores a local comparison reference atomically without changing the chosen layout. */
public class BlueprintReferenceLifecycleTest
{
	@Test public void rememberingBuiltinBlueprintsDoesNotForkOrReanalyzeAndSurvivesRestart() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
		{
			Map<String, String> values = new LinkedHashMap<>();
			values.put("bankPreset", base.equals("Main") ? "MAIN" : "IRONMAN");
			Fixture fixture = new Fixture(values);
			String plan = fixture.model.plan().serialize();
			BankLayoutOptions options = fixture.model.options();
			List<String> choices = fixture.model.presetChoices();
			Map<String, String> expected = new LinkedHashMap<>(fixture.values);
			expected.put(fixture.referenceKey(), BlueprintChanges.capture(fixture.preview));
			int analyses = fixture.analyses;
			fixture.queue();
			fixture.complete(true);
			assertEquals(expected, fixture.values);
			assertEquals(base, fixture.model.selectedPresetChoice());
			assertEquals(plan, fixture.model.plan().serialize());
			assertEquals(options, fixture.model.options());
			assertEquals(choices, fixture.model.presetChoices());
			assertEquals(analyses, fixture.analyses);
			Fixture restarted = new Fixture(values);
			assertEquals(base, restarted.model.selectedPresetChoice());
			assertEquals(expected.get(fixture.referenceKey()), restarted.model.rememberedBlueprint());
			assertEquals(choices, restarted.model.presetChoices());
		}
	}

	@Test public void builtinMainAndIronmanReferencesRemainIndependentAcrossRestart() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.remember();
		String ironman = fixture.model.rememberedBlueprint();
		fixture.select("Main", 1);
		assertEquals("", fixture.model.rememberedBlueprint());
		fixture.remember();
		String main = fixture.model.rememberedBlueprint();
		assertNotEquals(ironman, main);
		fixture.select("Ironman", 0);
		assertEquals(ironman, fixture.model.rememberedBlueprint());
		Fixture restarted = new Fixture(fixture.values);
		assertEquals(ironman, restarted.model.rememberedBlueprint());
		restarted.select("Main", 1);
		assertEquals(main, restarted.model.rememberedBlueprint());
		assertEquals(Arrays.asList("Ironman", "Main"), restarted.model.presetChoices());
	}

	@Test public void equallyNamedCustomProfilesKeepTheirReferencesSeparateFromEachOtherAndTheirParents() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.saveProfile("Shared", fixture.model.plan());
		fixture.publish(1);
		fixture.remember();
		String ironman = fixture.model.rememberedBlueprint();
		fixture.select("Main", 0);
		fixture.model.saveProfile("Shared", fixture.model.plan());
		fixture.publish(2);
		fixture.remember();
		String main = fixture.model.rememberedBlueprint();
		assertNotEquals(ironman, main);
		fixture.select("Ironman", 0);
		assertEquals("", fixture.model.rememberedBlueprint());
		fixture.select("Main", 0);
		assertEquals("", fixture.model.rememberedBlueprint());
		fixture.select("Ironman: Shared", 1);
		assertEquals(ironman, fixture.model.rememberedBlueprint());
		fixture.select("Main: Shared", 2);
		assertEquals(main, fixture.model.rememberedBlueprint());
		Fixture restarted = new Fixture(fixture.values);
		assertEquals("Main: Shared", restarted.model.selectedPresetChoice());
		assertEquals(main, restarted.model.rememberedBlueprint());
		restarted.select("Ironman: Shared", 1);
		assertEquals(ironman, restarted.model.rememberedBlueprint());
	}

	@Test public void deletingACustomClearsOnlyThatProfilesReference() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.model.saveProfile("First", fixture.model.plan());
		fixture.publish(1);
		fixture.remember();
		String firstKey = fixture.referenceKey();
		fixture.model.saveProfile("Second", fixture.model.plan());
		fixture.publish(2);
		fixture.remember();
		String second = fixture.model.rememberedBlueprint();
		fixture.model.deleteProfile("First");
		assertEquals("", fixture.values.getOrDefault(firstKey, ""));
		assertEquals(second, fixture.model.rememberedBlueprint());
		assertEquals("Ironman: Second", fixture.model.selectedPresetChoice());
	}

	@Test public void aClosedBankRejectsTheQueuedReferenceWithoutWritingSettings() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.queue();
		fixture.bankOpen = false;
		fixture.rejectWithoutWrites();
	}

	@Test public void aDifferentCurrentAnalysisRejectsTheQueuedReference() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.queue();
		fixture.publish(0);
		fixture.rejectWithoutWrites();
	}

	@Test public void changingTheActiveProfileOrPresetRejectsQueuedReference() throws Exception
	{
		for (String key : Arrays.asList("activeLayoutProfile", "bankPreset"))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.queue();
			fixture.values.put(key, key.equals("bankPreset") ? "MAIN" : "Changed after request");
			fixture.rejectWithoutWrites();
		}
	}

	@Test public void changedLiveItemsOrQuantitiesRejectTheQueuedReference() throws Exception
	{
		for (Item[] changed : Arrays.asList(
			new Item[] {new Item(995, 101), new Item(2347, 1), new Item(1755, 1)},
			new Item[] {new Item(995, 100), new Item(2347, 1), new Item(952, 1)}))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.queue();
			fixture.items = changed;
			fixture.rejectWithoutWrites();
		}
	}

	@Test public void queuedSettingsChangesRejectReferenceEvenWhenThePlanAndPreviewAreStillTheSame() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		String context = fixture.model.editingContext();
		String plan = fixture.model.plan().serialize();
		BankOrganizationPreview preview = fixture.preview;
		fixture.queue();
		fixture.values.put("alchMode", "FIXED");
		fixture.event("alchMode", "FLEXIBLE", "FIXED");
		assertNotEquals(context, fixture.model.editingContext());
		assertEquals(plan, fixture.model.plan().serialize());
		assertSame(preview, fixture.controller.organizationPreview());
		fixture.rejectWithoutWrites();
	}

	@Test public void futureAndMalformedReferenceFormatsArePreservedWhenRememberingIsRefused() throws Exception
	{
		for (String opaque : Arrays.asList("v99|future-reference-data", "malformed-reference"))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.values.put(fixture.referenceKey(), opaque);
			assertEquals(opaque, fixture.model.rememberedBlueprint());
			fixture.queue();
			fixture.rejectWithoutWrites();
			assertEquals(opaque, fixture.model.rememberedBlueprint());
		}
	}

	@Test public void referenceConfigEventsDoNotReanalyzeInvalidateContextOrQueueSettingsRefresh() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
		{
			Map<String, String> values = new LinkedHashMap<>();
			values.put("bankPreset", base.equals("Main") ? "MAIN" : "IRONMAN");
			Fixture fixture = new Fixture(values);
			String context = fixture.model.editingContext();
			int analyses = fixture.analyses;
			BankOrganizationPreview preview = fixture.preview;
			fixture.values.put(fixture.referenceKey(), BlueprintChanges.capture(preview));
			fixture.event(fixture.referenceKey(), "", fixture.model.rememberedBlueprint());
			assertEquals(context, fixture.model.editingContext());
			assertEquals(analyses, fixture.analyses);
			assertTrue(fixture.clientThread.pending.isEmpty());
			assertSame(preview, fixture.controller.organizationPreview());
			assertEquals(base, fixture.model.selectedPresetChoice());
		}
	}

	@Test public void rememberingDoesNotRequireAnotherFreeCustomProfileSlot() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		for (int index = 0; index < 20; index++) fixture.model.saveProfile("Saved " + index, fixture.model.plan());
		fixture.select("Ironman", 0);
		List<String> choices = fixture.model.presetChoices();
		fixture.remember();
		assertEquals("Ironman", fixture.model.selectedPresetChoice());
		assertEquals(choices, fixture.model.presetChoices());
		assertEquals(BlueprintChanges.capture(fixture.preview), fixture.model.rememberedBlueprint());
	}

	private static void set(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static Object unusedValue(Class<?> type)
	{
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		return null;
	}

	private static final class Fixture
	{
		private final Map<String, String> values;
		private final IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
		private final QueuedClientThread clientThread = new QueuedClientThread();
		private final BankGuideController controller = new BankGuideController();
		private final AtomicReference<Boolean> result = new AtomicReference<>();
		private final BankLayoutModel model;
		private final Client client;
		private BankOrganizationPreview preview;
		private Item[] items = {new Item(995, 100), new Item(2347, 1), new Item(1755, 1)};
		private boolean bankOpen = true;
		private boolean callbackOnEdt;
		private int analyses;

		@SuppressWarnings({"unchecked", "rawtypes"})
		private Fixture(Map<String, String> values) throws Exception
		{
			this.values = values;
			IronmanBankArchitectConfig config = (IronmanBankArchitectConfig) Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class<?>[] {IronmanBankArchitectConfig.class}, (proxy, method, args) -> {
					String key = method.getName();
					if (key.startsWith("set"))
					{
						values.put(Character.toLowerCase(key.charAt(3)) + key.substring(4), args[0].toString());
						return null;
					}
					if (values.containsKey(key))
					{
						String value = values.get(key);
						if (method.getReturnType() == boolean.class) return Boolean.parseBoolean(value);
						if (method.getReturnType().isEnum()) return Enum.valueOf((Class) method.getReturnType(), value);
						return value;
					}
					return method.isDefault() ? MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
						.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy).invokeWithArguments() : unusedValue(method.getReturnType());
				});
			Widget bankItems = (Widget) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Widget.class},
				(proxy, method, args) -> unusedValue(method.getReturnType()));
			ItemContainer container = (ItemContainer) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] {ItemContainer.class}, (proxy, method, args) ->
					"getItems".equals(method.getName()) ? items : unusedValue(method.getReturnType()));
			ItemComposition composition = (ItemComposition) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] {ItemComposition.class}, (proxy, method, args) ->
					method.getName().startsWith("getPlaceholder") ? -1 : unusedValue(method.getReturnType()));
			client = (Client) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Client.class},
				(proxy, method, args) -> {
					switch (method.getName())
					{
						case "getWidget":
							assertEquals(InterfaceID.Bankmain.ITEMS, ((Integer) args[0]).intValue());
							return bankOpen ? bankItems : null;
						case "getItemContainer":
							assertEquals(InventoryID.BANK, ((Integer) args[0]).intValue());
							return container;
						case "getItemDefinition": return composition;
						default: return unusedValue(method.getReturnType());
					}
				});
			set(plugin, "config", config);
			PresetTestSettings.attach(plugin, config, values);
			set(plugin, "client", client);
			set(plugin, "clientThread", clientThread);
			set(plugin, "guideController", controller);
			Method factory = IronmanBankArchitectPlugin.class.getDeclaredMethod("bankLayoutModel");
			factory.setAccessible(true);
			model = (BankLayoutModel) factory.invoke(plugin);
			set(plugin, "bankAnalysis", new BankAnalysis(command -> analyses++, command -> {}, Optional::empty,
				status -> {}, id -> Optional.empty(), model.preset()));
			publish(0);
		}

		private String referenceKey()
		{
			String profile = Base64.getUrlEncoder().withoutPadding().encodeToString(model.matchingProfile().getBytes(StandardCharsets.UTF_8));
			return (model.preset().getType() == BankPresetType.MAIN ? "main." : "") + "profile." + profile + ".rememberedBlueprint";
		}

		private void publish(int destination) throws Exception
		{
			List<BankCategoryPreview> categories = new ArrayList<>();
			for (int tab = 0; tab < model.preset().getCategories().size(); tab++)
				categories.add(new BankCategoryPreview(model.preset().getCategories().get(tab), tab == destination
					? Arrays.asList(new BankPreviewItem(995, "Coins", 100).withLayoutTag("currency"),
						new BankPreviewItem(2347, "Hammer", 1).withLayoutTag("tools"),
						new BankPreviewItem(1755, "Chisel", 1).withLayoutTag("tools")) : Collections.emptyList()));
			preview = new BankOrganizationPreview(model.preset(), categories);
			controller.publishBankAnalysis(BankAnalysisStatus.success(
				new BankCatalogSummary(3, 0, new EnumMap<>(ItemCategory.class)), preview));
			set(plugin, "analyzedBankContents", BankSnapshotReader.readOpenBank(client).get().contents());
		}

		private void select(String choice, int destination) throws Exception
		{
			model.selectPresetChoice(choice);
			while (!clientThread.pending.isEmpty()) clientThread.pending.removeFirst().run();
			SwingUtilities.invokeAndWait(() -> {});
			publish(destination);
		}

		private void queue()
		{
			result.set(null);
			callbackOnEdt = false;
			model.rememberBlueprint(preview, model.editingContext(), accepted -> {
				callbackOnEdt = SwingUtilities.isEventDispatchThread();
				result.set(accepted);
			});
			assertNull(result.get());
			assertEquals(1, clientThread.pending.size());
		}

		private void complete(boolean accepted) throws Exception
		{
			clientThread.pending.removeFirst().run();
			SwingUtilities.invokeAndWait(() -> {});
			assertEquals(accepted, result.get());
			assertTrue("Completion returns on Swing's event thread", callbackOnEdt);
		}

		private void remember() throws Exception { queue(); complete(true); }
		private void rejectWithoutWrites() throws Exception
		{
			Map<String, String> before = new LinkedHashMap<>(values);
			int analysesBefore = analyses;
			complete(false);
			assertEquals(before, values);
			assertEquals(analysesBefore, analyses);
		}

		private void event(String key, String before, String after)
		{
			ConfigChanged event = new ConfigChanged();
			event.setGroup(IronmanBankArchitectConfig.GROUP);
			event.setKey(key);
			event.setOldValue(before);
			event.setNewValue(after);
			plugin.onConfigChanged(event);
		}
	}

	private static final class QueuedClientThread extends ClientThread
	{
		private final Deque<Runnable> pending = new ArrayDeque<>();
		@Override public void invoke(Runnable command) { pending.addLast(command); }
		@Override public void invokeLater(Runnable command) { pending.addLast(command); }
	}
}

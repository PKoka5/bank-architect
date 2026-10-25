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

/** Real plugin persistence and stale-input checks for explicitly accepted local targets. */
public class AcceptedBlueprintLifecycleTest
{
	@Test public void acceptingABuiltinForksOneCustomAndRestoresItsActiveSnapshotAfterRestart() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
		{
			Map<String, String> values = new LinkedHashMap<>();
			values.put("bankPreset", base.equals("Main") ? "MAIN" : "IRONMAN");
			Fixture fixture = new Fixture(values);
			String originalPlan = fixture.model.plan().serialize();
			String builtinReference = BlueprintChanges.capture(fixture.effective);
			fixture.values.put(fixture.profileKey("rememberedBlueprint"), builtinReference);
			int analyses = fixture.analyses;
			fixture.accept();
			String custom = fixture.model.selectedPresetChoice();
			String snapshot = fixture.model.acceptedBlueprint();
			assertTrue(custom.startsWith(base + ": "));
			assertTrue(AcceptedBlueprint.isSupported(snapshot));
			assertFalse(snapshot.isEmpty());
			assertEquals(originalPlan, fixture.model.plan().serialize());
			assertEquals("The builtin's comparison reference is not inherited or overwritten", "", fixture.model.rememberedBlueprint());
			assertEquals(analyses + 1, fixture.analyses);
			assertTrue(fixture.model.blueprintState().contains("active"));
			Fixture restarted = new Fixture(values);
			assertEquals(custom, restarted.model.selectedPresetChoice());
			assertEquals(snapshot, restarted.model.acceptedBlueprint());
			assertTrue(restarted.model.blueprintState().contains("active"));
			restarted.select(base, 0);
			assertEquals("", restarted.model.acceptedBlueprint());
			assertEquals(builtinReference, restarted.model.rememberedBlueprint());
			assertEquals(3, restarted.model.presetChoices().size());
		}
	}

	@Test public void acceptingACustomUsesTheCurrentProposalAndLeavesTheOldReferenceUntouched() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.custom("Working", 0);
		String remembered = BlueprintChanges.capture(fixture.effective);
		fixture.values.put(fixture.profileKey("rememberedBlueprint"), remembered);
		fixture.publish(fixture.effective, fixture.preview(2));
		BankOrganizationPreview proposal = fixture.proposal;
		assertSame(proposal, fixture.model.proposedBlueprint());
		List<String> choices = fixture.model.presetChoices();
		fixture.accept();
		assertEquals("Ironman: Working", fixture.model.selectedPresetChoice());
		assertEquals(choices, fixture.model.presetChoices());
		assertEquals(remembered, fixture.model.rememberedBlueprint());
		assertEquals(BlueprintChanges.capture(proposal), fixture.model.reviewReference());
		BankOrganizationPreview applied = AcceptedBlueprint.apply(fixture.model.acceptedBlueprint(), fixture.context(),
			fixture.preview(0), fixture.model.plan());
		assertEquals(0, applied.getCategories().get(0).getItemCount());
		assertEquals(3, applied.getCategories().get(2).getItemCount());
	}

	@Test public void equallyNamedCustomsAndBothBuiltinPresetsKeepSnapshotsIndependent() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.custom("Shared", 1);
		fixture.accept();
		String ironman = fixture.model.acceptedBlueprint();
		fixture.select("Main", 0);
		assertEquals("", fixture.model.acceptedBlueprint());
		fixture.custom("Shared", 2);
		assertEquals("", fixture.model.acceptedBlueprint());
		fixture.accept();
		String main = fixture.model.acceptedBlueprint();
		assertNotEquals(ironman, main);
		fixture.select("Ironman", 0);
		assertEquals("", fixture.model.acceptedBlueprint());
		fixture.select("Ironman: Shared", 1);
		assertEquals(ironman, fixture.model.acceptedBlueprint());
		assertTrue(fixture.model.blueprintState().contains("active"));
		fixture.select("Main: Shared", 2);
		assertEquals(main, fixture.model.acceptedBlueprint());
		Fixture restarted = new Fixture(fixture.values);
		assertEquals(main, restarted.model.acceptedBlueprint());
		restarted.select("Ironman: Shared", 1);
		assertEquals(ironman, restarted.model.acceptedBlueprint());
	}

	@Test public void deletingACustomClearsOnlyItsAcceptedSnapshot() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.custom("First", 1);
		fixture.accept();
		String firstKey = fixture.profileKey("acceptedBlueprint");
		fixture.custom("Second", 2);
		fixture.accept();
		String second = fixture.model.acceptedBlueprint();
		fixture.model.deleteProfile("First");
		assertEquals("", fixture.values.getOrDefault(firstKey, ""));
		assertEquals(second, fixture.model.acceptedBlueprint());
		assertEquals("Ironman: Second", fixture.model.selectedPresetChoice());
	}

	@Test public void replacingTheEffectivePreviewRejectsQueuedAcceptanceWithoutCreatingACustom() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.queue();
		fixture.publish(0);
		fixture.rejectWithoutWrites();
		assertEquals("Ironman", fixture.model.selectedPresetChoice());
	}

	@Test public void replacingOnlyTheProposalRejectsQueuedAcceptanceEvenWhenTheEffectivePreviewIsIdentical() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.queue();
		BankOrganizationPreview effective = fixture.effective;
		fixture.publish(effective, fixture.preview(1));
		assertSame(effective, fixture.controller.organizationPreview());
		fixture.rejectWithoutWrites();
	}

	@Test public void closedBankOrChangedLiveContentsRejectAcceptanceBeforeAnyWrites() throws Exception
	{
		for (int changed = 0; changed < 3; changed++)
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.queue();
			if (changed == 0) fixture.bankOpen = false;
			else if (changed == 1) fixture.items = new Item[] {new Item(995, 101), new Item(2347, 1), new Item(1755, 1)};
			else fixture.items = new Item[] {new Item(995, 100), new Item(2347, 1), new Item(952, 1)};
			fixture.rejectWithoutWrites();
		}
	}

	@Test public void profilePresetAndQueuedSettingsChangesInvalidateAcceptance() throws Exception
	{
		for (String changed : Arrays.asList("activeLayoutProfile", "bankPreset", "alchMode"))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.queue();
			if (changed.equals("alchMode")) fixture.event("alchMode", "FLEXIBLE", "FIXED");
			else fixture.values.put(changed, changed.equals("bankPreset") ? "MAIN" : "Changed after request");
			fixture.rejectWithoutWrites();
		}
	}

	@Test public void unsupportedStoredSnapshotsArePreservedAndCannotBeOverwrittenByAcceptance() throws Exception
	{
		for (String opaque : Arrays.asList("BAkeep99|future-data", "damaged-snapshot"))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.values.put(fixture.profileKey("acceptedBlueprint"), opaque);
			fixture.queue();
			fixture.rejectWithoutWrites();
			assertEquals(opaque, fixture.model.acceptedBlueprint());
			assertEquals("Ironman", fixture.model.selectedPresetChoice());
			assertTrue(fixture.model.blueprintState().contains("preserved"));
		}
	}

	@Test public void changingAPersonalOptionPausesTheStoredSnapshotWithoutDeletingIt() throws Exception
	{
		Fixture fixture = acceptedCustom();
		String saved = fixture.model.acceptedBlueprint();
		String context = fixture.context();
		fixture.model.saveOptions(fixture.model.options().withAlchMode(AlchMode.FIXED));
		fixture.assertPaused(saved, context);
	}

	@Test public void changingTheTabPlanOrAnExplicitItemCorrectionPausesTheStoredSnapshot() throws Exception
	{
		for (boolean correction : new boolean[] {false, true})
		{
			Fixture fixture = acceptedCustom();
			String saved = fixture.model.acceptedBlueprint();
			String context = fixture.context();
			if (correction)
			{
				Method apply = IronmanBankArchitectPlugin.class.getDeclaredMethod("applyCategoryOverride",
					int.class, String.class, String.class);
				apply.setAccessible(true);
				apply.invoke(fixture.plugin, 995, "Coins", "tools");
			}
			else fixture.model.save(fixture.model.plan().withTagAt("gear", 9));
			fixture.assertPaused(saved, context);
		}
	}

	@Test public void savingANewPersonalItemOrderPausesTheSnapshot() throws Exception
	{
		Fixture fixture = acceptedCustom();
		String saved = fixture.model.acceptedBlueprint();
		String context = fixture.context();
		fixture.result.set(null);
		fixture.model.saveItemOrder(0, Arrays.asList(1755, 2347, 995), fixture.effective,
			fixture.model.editingContext(), fixture.result::set);
		fixture.complete(true, false);
		fixture.assertPaused(saved, context);
	}

	@Test public void savingANewPersonalBlockOrderPausesTheSnapshot() throws Exception
	{
		Fixture fixture = acceptedCustom();
		String saved = fixture.model.acceptedBlueprint();
		String context = fixture.context();
		fixture.model.saveBlockOrder("tools", Arrays.asList("fixture-second", "fixture-first"));
		fixture.assertPaused(saved, context);
	}

	@Test public void unrelatedProfilesItemOrdersDoNotPauseTheActiveSnapshot() throws Exception
	{
		Fixture fixture = acceptedCustom();
		String saved = fixture.model.acceptedBlueprint();
		String context = fixture.context();
		String editingContext = fixture.model.editingContext();
		BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse(fixture.values.get("blueprintOrdersByProfile"));
		profiles.put("Unrelated saved profile", BlueprintItemOrders.EMPTY.withTab(1, Arrays.asList(952, 2347)));
		fixture.values.put("blueprintOrdersByProfile", profiles.serialize());
		assertNotEquals("In-flight editing still guards the whole settings change", editingContext, fixture.model.editingContext());
		assertEquals(context, fixture.context());
		assertEquals(saved, fixture.model.acceptedBlueprint());
		assertTrue(fixture.model.blueprintState().contains("active"));
	}

	@Test public void acceptingANewProposalAfterPersonalChangesReactivatesTheSnapshotInTheSameCustom() throws Exception
	{
		Fixture fixture = acceptedCustom();
		String old = fixture.model.acceptedBlueprint();
		fixture.model.saveOptions(fixture.model.options().withAlchMode(AlchMode.FIXED));
		assertTrue(fixture.model.blueprintState().contains("paused"));
		fixture.publish(2);
		List<String> choices = fixture.model.presetChoices();
		fixture.accept();
		assertNotEquals(old, fixture.model.acceptedBlueprint());
		assertTrue(fixture.model.blueprintState().contains("active"));
		assertEquals("Ironman: Working", fixture.model.selectedPresetChoice());
		assertEquals(choices, fixture.model.presetChoices());
		assertEquals(BlueprintChanges.capture(fixture.proposal), fixture.model.reviewReference());
	}

	private static Fixture acceptedCustom() throws Exception
	{
		Fixture fixture = new Fixture(new LinkedHashMap<>());
		fixture.custom("Working", 0);
		fixture.accept();
		return fixture;
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
		private BankOrganizationPreview effective, proposal;
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

		private String profileKey(String setting)
		{
			String profile = Base64.getUrlEncoder().withoutPadding().encodeToString(model.matchingProfile().getBytes(StandardCharsets.UTF_8));
			return (model.preset().getType() == BankPresetType.MAIN ? "main." : "") + "profile." + profile + "." + setting;
		}
		private String context() throws Exception
		{
			Method context = IronmanBankArchitectPlugin.class.getDeclaredMethod("acceptanceContext");
			context.setAccessible(true);
			return (String) context.invoke(plugin);
		}
		private BankOrganizationPreview preview(int destination)
		{
			List<BankCategoryPreview> categories = new ArrayList<>();
			for (int tab = 0; tab < model.preset().getCategories().size(); tab++)
				categories.add(new BankCategoryPreview(model.preset().getCategories().get(tab), tab == destination
					? Arrays.asList(new BankPreviewItem(995, "Coins", 100).withLayoutTag("currency"),
						new BankPreviewItem(2347, "Hammer", 1).withLayoutTag("tools"),
						new BankPreviewItem(1755, "Chisel", 1).withLayoutTag("tools")) : Collections.emptyList()));
			return new BankOrganizationPreview(model.preset(), categories);
		}
		private void publish(int destination) throws Exception
		{
			BankOrganizationPreview current = preview(destination);
			publish(current, current);
		}
		private void publish(BankOrganizationPreview effective, BankOrganizationPreview proposal) throws Exception
		{
			this.effective = effective;
			this.proposal = proposal;
			controller.publishBankAnalysis(BankAnalysisStatus.success(
				new BankCatalogSummary(3, 0, new EnumMap<>(ItemCategory.class)), effective, proposal));
			set(plugin, "analyzedBankContents", BankSnapshotReader.readOpenBank(client).get().contents());
		}
		private void custom(String name, int destination) throws Exception
		{
			model.saveProfile(name, model.plan());
			publish(destination);
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
			model.acceptBlueprint(effective, proposal, model.editingContext(), accepted -> {
				callbackOnEdt = SwingUtilities.isEventDispatchThread();
				result.set(accepted);
			});
			assertNull(result.get());
			assertEquals(1, clientThread.pending.size());
		}
		private void complete(boolean accepted, boolean checkEdt) throws Exception
		{
			clientThread.pending.removeFirst().run();
			SwingUtilities.invokeAndWait(() -> {});
			assertEquals(accepted, result.get());
			if (checkEdt) assertTrue("Completion returns on Swing's event thread", callbackOnEdt);
		}
		private void accept() throws Exception { queue(); complete(true, true); }
		private void rejectWithoutWrites() throws Exception
		{
			Map<String, String> before = new LinkedHashMap<>(values);
			int analysesBefore = analyses;
			complete(false, true);
			assertEquals(before, values);
			assertEquals(analysesBefore, analyses);
		}
		private void assertPaused(String saved, String previousContext) throws Exception
		{
			assertEquals(saved, model.acceptedBlueprint());
			assertNotEquals(previousContext, context());
			assertTrue(model.blueprintState().contains("paused"));
			assertSame(proposal, AcceptedBlueprint.apply(saved, context(), proposal, model.plan()));
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

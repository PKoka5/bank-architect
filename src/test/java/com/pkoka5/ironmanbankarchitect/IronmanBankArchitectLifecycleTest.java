package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BlockArrangements;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintItemOrders;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintOrderProfiles;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class IronmanBankArchitectLifecycleTest
{
	@Test
	public void correctingAnItemAfterProfileSwitchPreservesOnlyTheNewProfilesCorrections() throws Exception
	{
		AtomicReference<String> stored = new AtomicReference<>("2347=frequently-used");
		IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
		IronmanBankArchitectConfig config = config(stored);
		set(plugin, "config", config);
		Map<String, String> values = new LinkedHashMap<>();
		PresetTestSettings.attach(plugin, config, values);
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		set(plugin, "guideController", controller);
		EventBus events = new EventBus();
		events.register(plugin);
		assign(plugin, 2347, "Hammer", "frequently-used");
		String profileA = corrections(plugin);

		stored.set("1755=tools");
		values.clear();
		events.post(new ProfileChanged());
		assign(plugin, 952, "Spade", "tools");

		UserCategoryOverrides profileB = UserCategoryOverrides.parse(corrections(plugin));
		assertEquals("2347=frequently-used", profileA);
		assertEquals(Optional.of("tools"), profileB.categoryKeyFor(1755));
		assertEquals(Optional.of("tools"), profileB.categoryKeyFor(952));
		assertFalse(profileB.categoryKeyFor(2347).isPresent());
		assertEquals(2, profileB.size());
		assertEquals(2, controller.getCategoryOverrideCount());
	}

	@Test
	public void profileSwitchImmediatelyHidesThePreviousSuccessfulBlueprint() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.completeAnalysis();
		assertNotNull(fixture.controller.organizationPreview());

		fixture.stored.set("5297=tools,952=tools");
		fixture.events.post(new ProfileChanged());

		assertEquals(BankAnalysisStatus.Kind.NOT_STARTED, fixture.controller.bankAnalysisStatus().kind());
		assertNull(fixture.controller.organizationPreview());
		assertEquals(1, fixture.clientThread.pending.size());
		fixture.clientThread.runNext();
		assertEquals(2, fixture.controller.getCategoryOverrideCount());
		assertEquals(BankAnalysisStatus.Kind.RUNNING, fixture.controller.bankAnalysisStatus().kind());
		fixture.clientThread.runNext();
		fixture.background.removeFirst().run();
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, fixture.controller.bankAnalysisStatus().kind());
		assertEquals(1, fixture.controller.organizationPreview().getTagCounts().get("tools").intValue());
	}

	@Test
	public void settingsEventBurstCapturesOneFreshRequestWithTheLatestCorrections() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.completeAnalysis();
		fixture.stored.set("5297=tools");
		fixture.events.post(new ProfileChanged());
		fixture.events.post(configChanged(IronmanBankArchitectConfig.GROUP, "tabOrder"));
		fixture.stored.set("5297=tools,952=tools,1755=tools");
		fixture.events.post(configChanged(IronmanBankArchitectConfig.GROUP, "categoryOverrides"));

		assertEquals(1, fixture.clientThread.pending.size());
		assertEquals(1, fixture.captures.get());
		fixture.clientThread.runNext();
		assertEquals(3, fixture.controller.getCategoryOverrideCount());
		assertEquals(1, fixture.clientThread.pending.size());
		fixture.clientThread.runNext();
		assertEquals(2, fixture.captures.get());
		assertEquals(1, fixture.background.size());
		fixture.background.removeFirst().run();
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, fixture.controller.bankAnalysisStatus().kind());
	}

	@Test
	public void unrelatedSettingsAndReleaseAcknowledgmentKeepTheCurrentBlueprint() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.completeAnalysis();
		BankAnalysisStatus before = fixture.controller.bankAnalysisStatus();

		fixture.events.post(configChanged("anotherPlugin", "categoryOverrides"));
		fixture.events.post(configChanged(IronmanBankArchitectConfig.GROUP, "lastSeenRelease"));

		assertSame(before, fixture.controller.bankAnalysisStatus());
		assertEquals(0, fixture.clientThread.pending.size());
		assertEquals(1, fixture.captures.get());
	}

	@Test
	public void oldQueuedAnalysisCannotPublishAfterProfileSwitch() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.analysis.analyzeBank();
		fixture.clientThread.runNext();
		assertEquals(1, fixture.background.size());
		fixture.stored.set("5297=tools");
		fixture.events.post(new ProfileChanged());

		fixture.background.removeFirst().run();

		assertEquals(BankAnalysisStatus.Kind.NOT_STARTED, fixture.controller.bankAnalysisStatus().kind());
		assertNull(fixture.controller.organizationPreview());
		fixture.clientThread.runNext();
		fixture.clientThread.runNext();
		fixture.background.removeFirst().run();
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, fixture.controller.bankAnalysisStatus().kind());
		assertEquals(1, fixture.controller.organizationPreview().getTagCounts().get("tools").intValue());
	}

	@Test
	public void selectingAwayFromARecoveredLegacyNamePersistsItsPlanAndAssociatedOrders() throws Exception
	{
		String oldName = "Raid; prep";
		String recoveredName = "Raid  prep";
		BankLayoutPlan legacyPlan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("runes", 7);
		BankLayoutPlan otherPlan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("food", 8);
		String legacyBlocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042")).serialize();
		String otherBlocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1038")).serialize();
		BlueprintItemOrders legacyItems = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(995, 2347));
		BlueprintOrderProfiles itemProfiles = BlueprintOrderProfiles.parse("");
		itemProfiles.put(oldName, legacyItems);
		Map<String, String> values = new LinkedHashMap<>();
		values.put("layoutProfiles", oldName + "~" + legacyPlan.serialize() + ";Bossing~" + otherPlan.serialize());
		values.put("activeLayoutProfile", oldName);
		values.put("tabOrder", legacyPlan.serialize());
		values.put("blockOrders", legacyBlocks);
		values.put("blockOrdersByProfile", oldName + "~" + legacyBlocks + ";Bossing~" + otherBlocks);
		values.put("blueprintOrdersByProfile", itemProfiles.serialize());
		BankLayoutModel model = layoutModel(values);

		model.selectProfile("Bossing");

		BankLayoutProfiles reloaded = BankLayoutProfiles.parse(values.get("layoutProfiles"), values.get("activeLayoutProfile"));
		assertTrue(reloaded.names().contains(recoveredName));
		assertEquals(legacyPlan.serialize(), reloaded.planFor(recoveredName));
		assertEquals(otherPlan.serialize(), values.get("tabOrder"));
		assertEquals(otherBlocks, model.options().blockArrangements().serialize());
		model.selectProfile(recoveredName);
		assertEquals(recoveredName, values.get("activeLayoutProfile"));
		assertEquals(legacyPlan.serialize(), values.get("tabOrder"));
		assertEquals(legacyBlocks, model.options().blockArrangements().serialize());
		assertEquals(legacyItems.serialize(), BlueprintOrderProfiles.parse(values.get("blueprintOrdersByProfile"))
			.forProfile(values.get("activeLayoutProfile")).serialize());
	}

	@Test
	public void savingACopyOfALegacyProfilePreservesTheOriginalBlockOrderLinkage() throws Exception
	{
		String oldName = "Raid; prep";
		String recoveredName = "Raid  prep";
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("runes", 7);
		String blocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042")).serialize();
		Map<String, String> values = new LinkedHashMap<>();
		values.put("layoutProfiles", oldName + "~" + plan.serialize());
		values.put("activeLayoutProfile", oldName);
		values.put("tabOrder", plan.serialize());
		values.put("blockOrders", blocks);
		values.put("blockOrdersByProfile", oldName + "~" + blocks);
		values.put("blueprintOrdersByProfile", "");
		BankLayoutModel model = layoutModel(values);

		model.saveProfile("Copied setup", plan);

		assertEquals("Copied setup", values.get("activeLayoutProfile"));
		BankLayoutProfiles reloaded = BankLayoutProfiles.parse(values.get("layoutProfiles"), values.get("activeLayoutProfile"));
		assertEquals(plan.serialize(), reloaded.planFor(recoveredName));
		assertEquals(plan.serialize(), reloaded.planFor("Copied setup"));
		model.selectProfile(recoveredName);
		assertEquals(blocks, model.options().blockArrangements().serialize());
		assertEquals(plan.serialize(), values.get("tabOrder"));
		model.selectProfile("Copied setup");
		assertEquals(blocks, model.options().blockArrangements().serialize());
	}

	@Test
	public void mainAndIronmanLayoutsKeepIndependentPlansOrdersAndMatchingNames() throws Exception
	{
		BankLayoutPlan ironmanPlan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN)
			.withTagAt("runes", 7).withCurrentOrder(1, true);
		String blocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042")).serialize();
		BlueprintItemOrders ironmanItems = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(995, 2347));
		BlueprintOrderProfiles orders = BlueprintOrderProfiles.parse("");
		orders.put("My bank", ironmanItems);
		Map<String, String> values = new LinkedHashMap<>();
		values.put("layoutProfiles", BankLayoutProfiles.parse("", "")
			.withProfile("My bank", ironmanPlan.serialize()).serialize());
		values.put("activeLayoutProfile", "My bank");
		values.put("tabOrder", ironmanPlan.serialize());
		values.put("blockOrders", blocks);
		values.put("blockOrdersByProfile", "My bank~" + blocks);
		values.put("blueprintOrdersByProfile", orders.serialize());
		Map<String, String> original = new LinkedHashMap<>(values);
		BankLayoutModel model = layoutModel(values);
		String ironmanContext = model.editingContext();

		values.put("bankPreset", "MAIN");
		assertEquals(BankPresets.MAIN, model.preset());
		assertFalse(ironmanContext.equals(model.editingContext()));
		BankLayoutPlan mainPlan = BankLayoutPlan.defaultFor(BankPresets.MAIN).withCurrentOrder(2, true);
		model.saveProfile("My bank", mainPlan);
		assertEquals("My bank", model.matchingProfile());
		assertEquals(mainPlan.serialize(), model.plan().serialize());
		assertTrue(model.plan().keepsCurrentOrder(2));
		assertFalse(model.plan().keepsCurrentOrder(1));
		assertEquals("", model.options().itemOrders().serialize());
		model.saveBlockOrder("cosmetics", Arrays.asList("item:1038"));
		String mainBlocks = model.options().blockArrangements().serialize();
		assertNotNull(mainBlocks);
		for (Map.Entry<String, String> entry : original.entrySet())
			assertEquals(entry.getValue(), values.get(entry.getKey()));

		values.put("bankPreset", "IRONMAN");
		assertEquals(BankPresets.IRONMAN, model.preset());
		assertEquals("My bank", model.matchingProfile());
		assertEquals(ironmanPlan.serialize(), model.plan().serialize());
		assertEquals(ironmanItems.serialize(), model.options().itemOrders().serialize());
		assertEquals(blocks, model.options().blockArrangements().serialize());
		values.put("bankPreset", "MAIN");
		assertEquals(mainPlan.serialize(), model.plan().serialize());
		assertEquals(mainBlocks, model.options().blockArrangements().serialize());
	}

	@Test
	public void legacyUnnamedIronmanWorkingOrdersRemainActiveAndSurviveSaveAs() throws Exception
	{
		BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("runes", 7);
		BlueprintItemOrders items = BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(995, 2347));
		BlueprintOrderProfiles orders = BlueprintOrderProfiles.parse("");
		orders.put("Shared layout", items);
		String blocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042")).serialize();
		Map<String, String> values = new LinkedHashMap<>();
		values.put("activeLayoutProfile", "");
		values.put("layoutProfiles", "");
		values.put("tabOrder", plan.serialize());
		values.put("blueprintOrdersByProfile", orders.serialize());
		values.put("blockOrders", blocks);
		values.put("blockOrdersByProfile", "Shared layout~" + blocks);
		BankLayoutModel model = layoutModel(values);

		assertEquals(items.serialize(), model.options().itemOrders().serialize());
		model.saveProfile("Copied setup", plan);
		assertEquals(items.serialize(), model.options().itemOrders().serialize());
		BlueprintOrderProfiles after = BlueprintOrderProfiles.parse(values.get("blueprintOrdersByProfile"));
		assertEquals(items.serialize(), after.forProfile("Shared layout").serialize());
		assertEquals(items.serialize(), after.forProfile("Copied setup").serialize());
		assertTrue(values.get("blockOrdersByProfile").contains("Shared layout~" + blocks));
	}

	private static BankLayoutModel layoutModel(Map<String, String> values) throws Exception
	{
		IronmanBankArchitectConfig config = (IronmanBankArchitectConfig) Proxy.newProxyInstance(
			IronmanBankArchitectLifecycleTest.class.getClassLoader(), new Class<?>[]{IronmanBankArchitectConfig.class},
			(proxy, method, arguments) -> {
				String key = method.getName();
				if (key.startsWith("set"))
				{
					key = Character.toLowerCase(key.charAt(3)) + key.substring(4);
					values.put(key, (String) arguments[0]);
					return null;
				}
				if (values.containsKey(key)) return values.get(key);
				return method.isDefault() ? MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
					.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy).invokeWithArguments() : null;
			});
		IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
		set(plugin, "config", config);
		PresetTestSettings.attach(plugin, config, values);
		Method factory = IronmanBankArchitectPlugin.class.getDeclaredMethod("bankLayoutModel");
		factory.setAccessible(true);
		return (BankLayoutModel) factory.invoke(plugin);
	}

	private static ConfigChanged configChanged(String group, String key)
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup(group);
		event.setKey(key);
		return event;
	}

	private static IronmanBankArchitectConfig config(AtomicReference<String> stored)
	{
		Map<String, Object> fields = new LinkedHashMap<>();
		return (IronmanBankArchitectConfig) Proxy.newProxyInstance(
			IronmanBankArchitectLifecycleTest.class.getClassLoader(),
			new Class<?>[]{IronmanBankArchitectConfig.class}, (proxy, method, arguments) -> {
				if ("categoryOverrides".equals(method.getName())) return stored.get();
				if ("setCategoryOverrides".equals(method.getName())) stored.set((String) arguments[0]);
				if (method.getName().startsWith("set"))
				{
					fields.put(Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4), arguments[0]);
					return null;
				}
				if (fields.containsKey(method.getName())) return fields.get(method.getName());
				return method.isDefault() ? MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
					.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy).invokeWithArguments() : null;
			});
	}

	private static String corrections(IronmanBankArchitectPlugin plugin) throws Exception
	{
		Method method = IronmanBankArchitectPlugin.class.getDeclaredMethod("categoryOverrides");
		method.setAccessible(true);
		return ((UserCategoryOverrides) method.invoke(plugin)).serialize();
	}

	private static void assign(IronmanBankArchitectPlugin plugin, int id, String name, String tag) throws Exception
	{
		Method method = IronmanBankArchitectPlugin.class.getDeclaredMethod(
			"applyCategoryOverride", int.class, String.class, String.class);
		method.setAccessible(true);
		method.invoke(plugin, id, name, tag);
	}

	private static void set(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static final class Fixture
	{
		private final AtomicReference<String> stored = new AtomicReference<>("5297=frequently-used");
		private final AtomicInteger captures = new AtomicInteger();
		private final QueuedClientThread clientThread = new QueuedClientThread();
		private final Deque<Runnable> background = new ArrayDeque<>();
		private final EventBus events = new EventBus();
		private final BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		private final BankAnalysis analysis;

		private Fixture() throws Exception
		{
			IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
			IronmanBankArchitectConfig config = config(stored);
			set(plugin, "config", config);
			PresetTestSettings.attach(plugin, config);
			set(plugin, "clientThread", clientThread);
			set(plugin, "guideController", controller);
			controller.publishCategoryOverrideCount(1);
			analysis = new BankAnalysis(clientThread::invoke, background::addLast, () -> {
				captures.incrementAndGet();
				return Optional.of(new BankAnalysisRequest(new BankSnapshot(Collections.singletonList(
					new BankItemSnapshot(5297, 1, 0))), Collections.emptyMap(), Collections.emptyMap(),
					UserCategoryOverrides.parse(stored.get()).asMap(),
					BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS));
			}, controller::publishBankAnalysis, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);
			set(plugin, "bankAnalysis", analysis);
			events.register(plugin);
		}

		private void completeAnalysis()
		{
			analysis.analyzeBank();
			clientThread.runNext();
			background.removeFirst().run();
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, controller.bankAnalysisStatus().kind());
		}
	}

	private static final class QueuedClientThread extends ClientThread
	{
		private final Deque<Runnable> pending = new ArrayDeque<>();

		@Override
		public void invoke(Runnable command) { pending.addLast(command); }

		@Override
		public void invokeLater(Runnable command) { pending.addLast(command); }

		private void runNext() { pending.removeFirst().run(); }
	}
}

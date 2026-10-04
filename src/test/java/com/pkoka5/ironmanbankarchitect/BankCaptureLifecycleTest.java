package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshotReader;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BlockArrangements;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintItemOrders;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintOrderProfiles;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BankCaptureLifecycleTest
{
	@Test
	public void captureReadsTheLatestPhysicalOrderAndCreatesANewProfileWithoutOverwritingTheOriginal() throws Exception
	{
		Fixture fixture = new Fixture();
		String originalPlan = fixture.values.get("tabOrder");
		String originalOrders = fixture.orders().forProfile("Saved setup").serialize();
		String originalBlocks = fixture.values.get("blockOrders");
		fixture.queue("Saved setup");
		assertNull(fixture.result.get());
		assertEquals("Saved setup", fixture.values.get("activeLayoutProfile"));

		fixture.items = new Item[]{new Item(1755, 1), new Item(2347, 1), new Item(995, 100)};
		fixture.tabOneCount = 2;
		fixture.complete();

		assertEquals(Boolean.TRUE, fixture.result.get());
		assertTrue(fixture.callbackOnEdt);
		assertEquals("Saved setup 2", fixture.values.get("activeLayoutProfile"));
		BankLayoutProfiles layouts = BankLayoutProfiles.parse(fixture.values.get("layoutProfiles"), "Saved setup 2");
		assertEquals(originalPlan, layouts.planFor("Saved setup"));
		assertEquals(originalPlan, layouts.planFor("Saved setup 2"));
		assertEquals(originalPlan, fixture.values.get("tabOrder"));
		assertEquals(originalOrders, fixture.orders().forProfile("Saved setup").serialize());
		assertEquals(originalBlocks, fixture.values.get("blockOrders"));
		assertTrue(fixture.values.get("blockOrdersByProfile").contains("Saved setup~" + originalBlocks));
		assertTrue(fixture.values.get("blockOrdersByProfile").contains("Saved setup 2~" + originalBlocks));
		BlueprintItemOrders captured = fixture.orders().forProfile("Saved setup 2");
		assertTrue(captured.isCaptured());
		BankOrganizationPreview applied = captured.apply(fixture.preview, fixture.plan);
		assertEquals(Arrays.asList(995), ids(applied, 0));
		assertEquals(Arrays.asList(1755, 2347), ids(applied, 1));
	}

	@Test
	public void aNewerAnalysisRejectsAQueuedCaptureWithoutChangingConfiguration() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.queue("Captured");
		fixture.publish(preview());
		fixture.assertRejectedWithoutMutation();
	}

	@Test
	public void aProfileOrSavedOrderChangeRejectsAQueuedCapture() throws Exception
	{
		for (String key : Arrays.asList("activeLayoutProfile", "blueprintOrdersByProfile"))
		{
			Fixture fixture = new Fixture();
			fixture.queue("Captured");
			fixture.values.put(key, "changed after request");
			fixture.assertRejectedWithoutMutation();
		}
	}

	@Test
	public void closingTheBankRejectsAQueuedCapture() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.queue("Captured");
		fixture.bankOpen = false;
		fixture.assertRejectedWithoutMutation();
	}

	@Test
	public void changingStackQuantityOrReplacingAnItemRejectsCapture() throws Exception
	{
		for (Item[] changed : Arrays.asList(
			new Item[]{new Item(995, 101), new Item(2347, 1), new Item(1755, 1)},
			new Item[]{new Item(995, 100), new Item(2347, 1), new Item(952, 1)}))
		{
			Fixture fixture = new Fixture();
			fixture.queue("Captured");
			fixture.items = changed;
			fixture.assertRejectedWithoutMutation();
		}
	}

	@Test
	public void aFullProfileListDoesNotEvictOrOverwriteAnExistingLayout() throws Exception
	{
		Fixture fixture = new Fixture();
		BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "");
		profiles = profiles.withProfile("Saved setup", fixture.plan.serialize());
		for (int index = 1; index < 20; index++) profiles = profiles.withProfile("Layout " + index, fixture.plan.serialize());
		fixture.values.put("layoutProfiles", profiles.serialize());
		fixture.queue("Saved setup");
		fixture.assertRejectedWithoutMutation();
	}

	@Test
	public void aNewerOrderFormatIsPreservedWhenCaptureIsRefused() throws Exception
	{
		Fixture fixture = new Fixture();
		BlueprintOrderProfiles profiles = fixture.orders();
		profiles.put("Saved setup", BlueprintItemOrders.parse("v9|future-data"));
		fixture.values.put("blueprintOrdersByProfile", profiles.serialize());
		fixture.queue("Captured");
		fixture.assertRejectedWithoutMutation();
		assertEquals("v9|future-data", fixture.orders().forProfile("Saved setup").serialize());
	}

	@Test
	public void invalidLiveTabCountsDoNotPartiallyCreateAProfile() throws Exception
	{
		Fixture fixture = new Fixture();
		fixture.queue("Captured");
		fixture.tabOneCount = 4;
		fixture.assertRejectedWithoutMutation();
	}

	private static List<Integer> ids(BankOrganizationPreview preview, int tab)
	{
		List<Integer> ids = new ArrayList<>();
		preview.getCategories().get(tab).getItems().forEach(item -> ids.add(item.getItemId()));
		return ids;
	}

	private static BankOrganizationPreview preview()
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int tab = 0; tab < BankPresets.IRONMAN.getCategories().size(); tab++)
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(tab), tab == 0
				? Arrays.asList(new BankPreviewItem(995, "Coins", 100).withLayoutTag("currency"),
					new BankPreviewItem(2347, "Hammer", 1).withLayoutTag("frequently-used"),
					new BankPreviewItem(1755, "Chisel", 1).withLayoutTag("tools")) : Collections.emptyList()));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
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
		if (type.isEnum()) return type.getEnumConstants()[0];
		return null;
	}

	private static final class Fixture
	{
		private final Map<String, String> values = new LinkedHashMap<>();
		private final QueuedClientThread clientThread = new QueuedClientThread();
		private final BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		private final BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN).withTagAt("runes", 7);
		private final BankOrganizationPreview preview = preview();
		private final AtomicReference<Boolean> result = new AtomicReference<>();
		private final BankLayoutModel model;
		private Item[] items = {new Item(995, 100), new Item(2347, 1), new Item(1755, 1)};
		private boolean bankOpen = true;
		private int tabOneCount;
		private boolean callbackOnEdt;

		private Fixture() throws Exception
		{
			String blocks = BlockArrangements.EMPTY.withTag("cosmetics", Arrays.asList("item:1042")).serialize();
			values.put("layoutProfiles", BankLayoutProfiles.parse("", "").withProfile("Saved setup", plan.serialize()).serialize());
			values.put("activeLayoutProfile", "Saved setup");
			values.put("tabOrder", plan.serialize());
			values.put("blockOrders", blocks);
			values.put("blockOrdersByProfile", "Saved setup~" + blocks);
			BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse("");
			profiles.put("Saved setup", BlueprintItemOrders.EMPTY.withTab(0, Arrays.asList(995, 1755, 2347)));
			values.put("blueprintOrdersByProfile", profiles.serialize());
			IronmanBankArchitectConfig config = (IronmanBankArchitectConfig) Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class<?>[]{IronmanBankArchitectConfig.class}, (proxy, method, arguments) -> {
					String key = method.getName();
					if (key.startsWith("set"))
					{
						key = Character.toLowerCase(key.charAt(3)) + key.substring(4);
						values.put(key, (String) arguments[0]);
						return null;
					}
					return method.getReturnType() == String.class ? values.getOrDefault(key, "") : unusedValue(method.getReturnType());
				});
			Widget bankItems = (Widget) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Widget.class},
				(proxy, method, arguments) -> unusedValue(method.getReturnType()));
			ItemContainer container = (ItemContainer) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[]{ItemContainer.class}, (proxy, method, arguments) ->
					"getItems".equals(method.getName()) ? items : unusedValue(method.getReturnType()));
			ItemComposition composition = (ItemComposition) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[]{ItemComposition.class}, (proxy, method, arguments) ->
					method.getName().startsWith("getPlaceholder") ? -1 : unusedValue(method.getReturnType()));
			Client client = (Client) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Client.class},
				(proxy, method, arguments) -> {
					switch (method.getName())
					{
						case "getWidget":
							assertEquals(InterfaceID.Bankmain.ITEMS, ((Integer) arguments[0]).intValue());
							return bankOpen ? bankItems : null;
						case "getItemContainer":
							assertEquals(InventoryID.BANK, ((Integer) arguments[0]).intValue());
							return container;
						case "getItemDefinition": return composition;
						case "getVarbitValue": return ((Integer) arguments[0]) == VarbitID.BANK_TAB_1 ? tabOneCount : 0;
						default: return unusedValue(method.getReturnType());
					}
				});
			IronmanBankArchitectPlugin plugin = new IronmanBankArchitectPlugin();
			set(plugin, "config", config);
			set(plugin, "client", client);
			set(plugin, "clientThread", clientThread);
			set(plugin, "guideController", controller);
			set(plugin, "analyzedBankContents", BankSnapshotReader.readOpenBank(client).get().contents());
			publish(preview);
			Method factory = IronmanBankArchitectPlugin.class.getDeclaredMethod("bankLayoutModel");
			factory.setAccessible(true);
			model = (BankLayoutModel) factory.invoke(plugin);
		}

		private BlueprintOrderProfiles orders() { return BlueprintOrderProfiles.parse(values.get("blueprintOrdersByProfile")); }

		private void publish(BankOrganizationPreview preview)
		{
			controller.publishBankAnalysis(BankAnalysisStatus.success(
				new BankCatalogSummary(3, 0, new EnumMap<>(ItemCategory.class)), preview));
		}

		private void queue(String name)
		{
			model.captureCurrentBank(name, preview, model.editingContext(), accepted -> {
				callbackOnEdt = SwingUtilities.isEventDispatchThread();
				result.set(accepted);
			});
			assertEquals(1, clientThread.pending.size());
		}

		private void complete() throws Exception
		{
			clientThread.pending.removeFirst().run();
			SwingUtilities.invokeAndWait(() -> {});
		}

		private void assertRejectedWithoutMutation() throws Exception
		{
			Map<String, String> before = new LinkedHashMap<>(values);
			complete();
			assertEquals(Boolean.FALSE, result.get());
			assertEquals(before, values);
			assertTrue(callbackOnEdt);
			assertFalse(clientThread.pending.size() > 0);
		}
	}

	private static final class QueuedClientThread extends ClientThread
	{
		private final Deque<Runnable> pending = new ArrayDeque<>();
		@Override public void invoke(Runnable command) { pending.addLast(command); }
	}
}

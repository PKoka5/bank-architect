package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis;
import com.pkoka5.ironmanbankarchitect.override.UserCategoryOverrides;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import net.runelite.client.callback.ClientThread;
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
		field(fixture.plugin, "guideController", new BankGuideController());
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

	@Test public void movingCompleteTabsForksBuiltinOnceAndSurvivesRestartForBothPresets() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
			for (int[] move : Arrays.asList(new int[]{0, 7}, new int[]{8, 0}))
			{
				Fixture fixture = new Fixture(new LinkedHashMap<>());
				fixture.model.selectPresetChoice(base);
				BankLayoutPlan original = fixture.model.plan();
				Map<String, String> before = new LinkedHashMap<>(fixture.values);
				fixture.queueMove(move[0], move[1]);
				assertEquals("Queueing cannot change either preset", before, fixture.values);
				assertEquals(base, fixture.model.selectedPresetChoice());
				fixture.completeMove(true);
				String choice = fixture.model.selectedPresetChoice();
				assertEquals(base + ": Custom layout", choice);
				assertMovedPlan(original, fixture.model.plan(), move[0], move[1]);
				String moved = fixture.model.plan().serialize();
				Fixture restarted = new Fixture(fixture.values);
				assertEquals(choice, restarted.model.selectedPresetChoice());
				assertEquals(moved, restarted.model.plan().serialize());
				restarted.model.selectPresetChoice(base);
				assertEquals(original.serialize(), restarted.model.plan().serialize());
				assertEquals("", restarted.model.options().itemOrders().serialize());
				restarted.model.selectPresetChoice(choice);
				assertEquals(moved, restarted.model.plan().serialize());
				assertEquals(3, restarted.model.presetChoices().size());
			}
	}

	@Test public void tabMovePreservesCapturedRoutesDormantOrdersAndIndependentProfiles() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
		{
			Fixture fixture = new Fixture(new LinkedHashMap<>());
			fixture.model.selectPresetChoice(base);
			BankLayoutPlan original = fixture.model.plan().withTagAt("food", 8)
				.withCurrentOrder(0, true).withCurrentOrder(2, true).withCurrentOrder(8, true);
			fixture.model.saveProfile("Personal", original);
			fixture.model.saveOptions(new BankLayoutOptions(true, false, false, Collections.emptyMap(),
				GearLayout.LIST, PotionDoseOrder.BY_FAMILY, RuneOrder.ELEMENTAL, TeleportOrder.ALPHABETICAL, false));
			fixture.assign(9084, "frequently-used");
			fixture.model.saveBlockOrder("cosmetics", Collections.singletonList("item:1042"));
			String orders = "v2|0:995,13204,999999|2:11832,11834|8:952"
				+ "|c:995#0:0:currency:currency|c:11832#0:2:gear:gear|c:999998#0:8:tools:tools"
				+ "|r:4151#0:" + original.destinationOf("tools") + ":gear:tools";
			fixture.storeOrders(orders);
			fixture.model.saveProfile("Other", original);
			String otherOrders = fixture.storedOrders("Other").serialize();
			fixture.model.selectPresetChoice(base + ": Personal");
			BankLayoutOptions preferences = fixture.model.options().withItemOrders(BlueprintItemOrders.EMPTY);
			String corrections = fixture.corrections();
			String blocks = fixture.model.options().blockArrangements().serialize();
			fixture.queueMove(0, 8);
			fixture.completeMove(true);
			assertEquals(base + ": Personal", fixture.model.selectedPresetChoice());
			assertMovedPlan(original, fixture.model.plan(), 0, 8);
			BlueprintItemOrders moved = fixture.model.options().itemOrders();
			assertTrue(moved.isCaptured());
			assertEquals(Arrays.asList(995, 13204, 999999), savedIds(moved, 8));
			assertEquals(Arrays.asList(11832, 11834), savedIds(moved, 1));
			assertEquals(Collections.singletonList(952), savedIds(moved, 7));
			assertFalse(moved.hasTab(0));
			assertTrue(moved.serialize().contains("|c:995#0:8:currency:currency"));
			assertTrue(moved.serialize().contains("|c:11832#0:1:gear:gear"));
			assertTrue("Absent captured items retain their route", moved.serialize().contains("|c:999998#0:7:tools:tools"));
			BlueprintItemOrders.Destination manual = moved.destinations().get("4151#0");
			assertEquals(movedIndex(original.destinationOf("tools"), 0, 8), manual.tab);
			assertEquals("gear", manual.originalTag);
			assertEquals("tools", manual.tag);
			assertTrue(moved.serialize().contains("|r:4151#0:"));
			assertEquals(preferences, fixture.model.options().withItemOrders(BlueprintItemOrders.EMPTY));
			assertEquals(corrections, fixture.corrections());
			assertEquals(blocks, fixture.model.options().blockArrangements().serialize());
			assertEquals(otherOrders, fixture.storedOrders("Other").serialize());
			String movedPlan = fixture.model.plan().serialize();
			String movedOrders = moved.serialize();
			Fixture restarted = new Fixture(fixture.values);
			assertEquals(movedPlan, restarted.model.plan().serialize());
			assertEquals(movedOrders, restarted.model.options().itemOrders().serialize());
			assertEquals(corrections, restarted.corrections());
			restarted.model.selectPresetChoice(base + ": Other");
			assertEquals(original.serialize(), restarted.model.plan().serialize());
			assertEquals(otherOrders, restarted.model.options().itemOrders().serialize());
			restarted.model.selectPresetChoice(base.equals("Main") ? "Ironman" : "Main");
			assertEquals(BankLayoutPlan.defaultFor(restarted.model.preset()).serialize(), restarted.model.plan().serialize());
			assertEquals("", restarted.model.options().itemOrders().serialize());
			assertEquals("", restarted.corrections());
			restarted.model.selectPresetChoice(base + ": Personal");
			restarted.queueMove(8, 0);
			restarted.completeMove(true);
			assertMovedPlan(original, restarted.model.plan(), 0, 0);
			assertEquals(original.ammoUsesAssignedTag(restarted.model.preset()),
				restarted.model.plan().ammoUsesAssignedTag(restarted.model.preset()));
			assertEquals(orders, restarted.model.options().itemOrders().serialize());
		}
	}

	@Test public void queuedTabMoveRefusesStalePresetPlanOrOrdersWithoutFurtherWrites() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
			for (String change : Arrays.asList("preset", "plan", "orders"))
			{
				Fixture fixture = new Fixture(new LinkedHashMap<>());
				fixture.model.selectPresetChoice(base);
				fixture.model.saveProfile("Personal", fixture.model.plan());
				fixture.queueMove(0, 7);
				if (change.equals("preset")) fixture.model.selectPresetChoice(base.equals("Main") ? "Ironman" : "Main");
				else if (change.equals("plan")) fixture.model.save(fixture.model.plan().withTagAt("food", 8));
				else fixture.storeOrders("v1|0:995,13204");
				Map<String, String> changed = new LinkedHashMap<>(fixture.values);
				fixture.analysisBeforeMove = fixture.analysisRequests;
				fixture.completeMove(false);
				assertEquals(change + " must reject atomically", changed, fixture.values);
			}
	}

	@Test public void futureOrdersAndInvalidOrNoOpTabMovesNeverWriteSettings() throws Exception
	{
		for (String base : Arrays.asList("Ironman", "Main"))
			for (int[] move : Arrays.asList(new int[]{-1, 1}, new int[]{0, 10}, new int[]{3, 3}, new int[]{0, 7}))
			{
				Fixture fixture = new Fixture(new LinkedHashMap<>());
				fixture.model.selectPresetChoice(base);
				if (move[0] == 0 && move[1] == 7)
				{
					fixture.model.saveProfile("Future", fixture.model.plan());
					fixture.storeOrders("v99|must-preserve-this-verbatim");
				}
				Map<String, String> before = new LinkedHashMap<>(fixture.values);
				fixture.queueMove(move[0], move[1]);
				fixture.completeMove(false);
				assertEquals(before, fixture.values);
			}
	}

	private static void assertMovedPlan(BankLayoutPlan before, BankLayoutPlan after, int from, int to)
	{
		for (int original = 0; original < BankLayoutPlan.DESTINATION_COUNT; original++)
		{
			int moved = movedIndex(original, from, to);
			assertEquals(before.getTagKeys(original), after.getTagKeys(moved));
			assertEquals(before.keepsCurrentOrder(original), after.keepsCurrentOrder(moved));
		}
	}

	private static int movedIndex(int original, int from, int to)
	{
		return original == from ? to : from < to && original > from && original <= to ? original - 1
			: from > to && original >= to && original < from ? original + 1 : original;
	}

	private static List<Integer> savedIds(BlueprintItemOrders orders, int tab)
	{
		for (String entry : orders.serialize().split("\\|"))
			if (entry.startsWith(tab + ":"))
			{
				List<Integer> result = new ArrayList<>();
				for (String id : entry.substring(entry.indexOf(':') + 1).split(",")) result.add(Integer.parseInt(id));
				return result;
			}
		return Collections.emptyList();
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
		private final QueuedClientThread clientThread = new QueuedClientThread();
		private final AtomicReference<Boolean> moveResult = new AtomicReference<>();
		private int analysisRequests;
		private int analysisBeforeMove;
		private boolean callbackOnEdt;
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
			field(plugin, "clientThread", clientThread);
			field(plugin, "bankAnalysis", new BankAnalysis(command -> analysisRequests++, command -> {},
				Optional::empty, status -> {}, id -> Optional.empty(), model.preset()));
		}
		private void storeOrders(String serialized)
		{
			BlueprintOrderProfiles profiles = BlueprintOrderProfiles.parse(values.get(ordersKey()));
			profiles.put(model.matchingProfile(), BlueprintItemOrders.parse(serialized));
			values.put(ordersKey(), profiles.serialize());
		}
		private BlueprintItemOrders storedOrders(String profile)
		{
			return BlueprintOrderProfiles.parse(values.get(ordersKey())).forProfile(profile);
		}
		private String ordersKey()
		{
			return (model.preset().getType() == BankPresetType.MAIN ? "main." : "") + "blueprintOrdersByProfile";
		}
		private void queueMove(int from, int to)
		{
			moveResult.set(null);
			callbackOnEdt = false;
			analysisBeforeMove = analysisRequests;
			model.moveTab(from, to, model.editingContext(), accepted -> {
				callbackOnEdt = SwingUtilities.isEventDispatchThread();
				moveResult.set(accepted);
			});
			assertNull(moveResult.get());
			assertEquals(1, clientThread.pending.size());
		}
		private void completeMove(boolean expected) throws Exception
		{
			clientThread.pending.removeFirst().run();
			SwingUtilities.invokeAndWait(() -> {});
			assertEquals(expected, moveResult.get());
			assertTrue("Completion callback belongs on Swing's event thread", callbackOnEdt);
			assertEquals("Only a successful atomic move requests one analysis",
				analysisBeforeMove + (expected ? 1 : 0), analysisRequests);
			assertTrue(clientThread.pending.isEmpty());
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

	private static final class QueuedClientThread extends ClientThread
	{
		private final Deque<Runnable> pending = new ArrayDeque<>();
		@Override public void invoke(Runnable command) { pending.addLast(command); }
	}
}

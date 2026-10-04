package com.pkoka5.ironmanbankarchitect.bank;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import static org.junit.Assert.*;

public class BankCaptureReaderTest
{
	private static final int PLACEHOLDER_ID = 50000;
	private static final int[] TAB_VARBITS = {VarbitID.BANK_TAB_1, VarbitID.BANK_TAB_2,
		VarbitID.BANK_TAB_3, VarbitID.BANK_TAB_4, VarbitID.BANK_TAB_5, VarbitID.BANK_TAB_6,
		VarbitID.BANK_TAB_7, VarbitID.BANK_TAB_8, VarbitID.BANK_TAB_9};

	@Test public void freshReadCanonicalizesPlaceholderAndKeepsEachPhysicalOccurrenceInItsTab()
	{
		Fixture fixture = new Fixture(new Item(PLACEHOLDER_ID, 0), new Item(4151, 7),
			new Item(2347, 1), new Item(995, 100));
		fixture.counts[0] = 2;
		fixture.counts[1] = 1;
		BankSnapshot snapshot = fixture.snapshot();
		assertEquals(4, snapshot.getPhysicalItems().size());
		assertEquals(3, snapshot.getItems().size());
		assertEquals(4151, snapshot.getPhysicalItems().get(0).getItemId());
		assertTrue(snapshot.getPhysicalItems().get(0).isPlaceholder());
		assertEquals(0, snapshot.getPhysicalItems().get(0).getQuantity());
		assertFalse(snapshot.getPhysicalItems().get(1).isPlaceholder());
		assertEquals(7, snapshot.getTotalQuantity(4151));

		Map<Integer, List<Integer>> tabs = BankSnapshotReader.readTabOrders(fixture.client, snapshot).get();
		assertEquals(Arrays.asList(4151, 4151), tabs.get(1));
		assertEquals(Arrays.asList(2347), tabs.get(2));
		assertEquals(Arrays.asList(995), tabs.get(0));
		assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), new ArrayList<>(tabs.keySet()));
		for (int tab = 3; tab <= 9; tab++) assertTrue(tabs.get(tab).isEmpty());
	}

	@Test public void allNineDenseVarbitRangesPrecedeMainWithoutLosingTheLastTab()
	{
		Item[] raw = new Item[11];
		Arrays.setAll(raw, slot -> new Item(101 + slot, 1));
		Fixture fixture = new Fixture(raw);
		Arrays.fill(fixture.counts, 1);
		Map<Integer, List<Integer>> tabs = BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).get();
		for (int tab = 1; tab <= 9; tab++) assertEquals(Arrays.asList(100 + tab), tabs.get(tab));
		assertEquals(Arrays.asList(110, 111), tabs.get(0));
		List<Integer> flattened = new ArrayList<>();
		tabs.values().forEach(flattened::addAll);
		assertEquals(Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111), flattened);
	}

	@Test public void allMainAndNoMainBanksAreBothValid()
	{
		Fixture fixture = new Fixture(new Item(995, 100), new Item(2347, 1));
		Map<Integer, List<Integer>> allMain = BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).get();
		assertEquals(Arrays.asList(995, 2347), allMain.get(0));
		for (int tab = 1; tab <= 9; tab++) assertTrue(allMain.get(tab).isEmpty());
		fixture.counts[0] = 2;
		Map<Integer, List<Integer>> noMain = BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).get();
		assertTrue(noMain.get(0).isEmpty());
		assertEquals(Arrays.asList(995, 2347), noMain.get(1));
	}

	@Test public void closedHiddenAndMissingContainerBanksHaveNoFreshSnapshot()
	{
		Fixture fixture = new Fixture(new Item(995, 1));
		fixture.open = false;
		assertFalse(BankSnapshotReader.readOpenBank(fixture.client).isPresent());
		fixture.open = true;
		fixture.hidden = true;
		assertFalse(BankSnapshotReader.readOpenBank(fixture.client).isPresent());
		fixture.hidden = false;
		fixture.containerPresent = false;
		assertFalse(BankSnapshotReader.readOpenBank(fixture.client).isPresent());
	}

	@Test public void emptyOpenBankCanBeReadButCannotBeCaptured()
	{
		Fixture fixture = new Fixture();
		BankSnapshot empty = fixture.snapshot();
		assertTrue(empty.isEmpty());
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, empty).isPresent());
		fixture.items = new Item[]{null, new Item(-1, 0)};
		assertTrue(fixture.snapshot().isEmpty());
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).isPresent());
	}

	@Test public void internalHolesNullGapsAndFillersAreRefused()
	{
		assertRejected(new Item(995, 1), new Item(-1, 0), new Item(2347, 1));
		assertRejected(new Item(995, 1), null, new Item(2347, 1));
		assertRejected(new Item(995, 1), new Item(ItemID.BANK_FILLER, 1), new Item(2347, 1));
		assertRejected(new Item(ItemID.BANK_FILLER, 1), new Item(995, 1));
		assertRejected(new Item(995, 1), new Item(2347, 0), new Item(4151, 1));
	}

	@Test public void trailingFillersAreRefusedWhileTrailingEmptyContainerSlotsAreAllowed()
	{
		assertRejected(new Item(995, 1), new Item(ItemID.BANK_FILLER, 1));
		Fixture fixture = new Fixture(new Item(995, 100), new Item(2347, 1), null, new Item(-1, 0), null);
		fixture.counts[0] = 1;
		Map<Integer, List<Integer>> tabs = BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).get();
		assertEquals(Arrays.asList(995), tabs.get(1));
		assertEquals(Arrays.asList(2347), tabs.get(0));
	}

	@Test public void negativeExcessAndGappedTabCountsAreRefused()
	{
		int[][] invalidCounts = {{-1}, {3}, {1, 2}, {0, 1}, {1, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 0, 1}};
		for (int[] counts : invalidCounts)
		{
			Fixture fixture = new Fixture(new Item(995, 1), new Item(2347, 1));
			System.arraycopy(counts, 0, fixture.counts, 0, counts.length);
			assertFalse("Invalid tab counts " + Arrays.toString(counts),
				BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).isPresent());
		}
	}

	@Test public void rawContainerLengthChangesAndDisappearanceRejectTheOldSnapshot()
	{
		Fixture fixture = new Fixture(new Item(995, 100), new Item(2347, 1));
		BankSnapshot before = fixture.snapshot();
		fixture.items = new Item[]{new Item(995, 100)};
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.items = new Item[]{new Item(995, 100), new Item(2347, 1), new Item(4151, 1)};
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.containerPresent = false;
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
	}

	@Test public void rawIdentityQuantityAndPlaceholderChangesRejectTheOldSnapshot()
	{
		Fixture fixture = new Fixture(new Item(995, 100), new Item(4151, 1));
		BankSnapshot before = fixture.snapshot();
		fixture.items[1] = new Item(2347, 1);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.items[1] = new Item(4151, 2);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.items[1] = new Item(PLACEHOLDER_ID, 0);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.items[1] = null;
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
		fixture.items[1] = new Item(ItemID.BANK_FILLER, 1);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, before).isPresent());
	}

	@Test public void bankAboveSupportedCapacityIsRefused()
	{
		Item[] raw = new Item[1411];
		Arrays.setAll(raw, slot -> new Item(4151, 1));
		Fixture fixture = new Fixture(raw);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).isPresent());
	}

	private static void assertRejected(Item... raw)
	{
		Fixture fixture = new Fixture(raw);
		assertFalse(BankSnapshotReader.readTabOrders(fixture.client, fixture.snapshot()).isPresent());
	}

	private static final class Fixture
	{
		private Item[] items;
		private final int[] counts = new int[9];
		private boolean open = true;
		private boolean hidden;
		private boolean containerPresent = true;
		private final Client client;

		private Fixture(Item... items)
		{
			this.items = items;
			Widget widget = proxy(Widget.class, (instance, method, arguments) -> {
				if ("isHidden".equals(method.getName())) return hidden;
				throw new AssertionError("Unexpected widget access " + method.getName());
			});
			ItemContainer container = proxy(ItemContainer.class, (instance, method, arguments) -> {
				if ("getItems".equals(method.getName())) return this.items;
				throw new AssertionError("Unexpected container access " + method.getName());
			});
			ItemComposition ordinary = composition(-1, -1);
			ItemComposition placeholder = composition(14401, 4151);
			client = proxy(Client.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "getWidget":
						assertEquals(InterfaceID.Bankmain.ITEMS, ((Integer) arguments[0]).intValue());
						return open ? widget : null;
					case "getItemContainer":
						assertEquals(InventoryID.BANK, ((Integer) arguments[0]).intValue());
						return containerPresent ? container : null;
					case "getItemDefinition": return ((Integer) arguments[0]) == PLACEHOLDER_ID ? placeholder : ordinary;
					case "getVarbitValue":
						for (int index = 0; index < TAB_VARBITS.length; index++)
							if (((Integer) arguments[0]) == TAB_VARBITS[index]) return counts[index];
						throw new AssertionError("Unexpected varbit " + arguments[0]);
					default: throw new AssertionError("Unexpected client access " + method.getName());
				}
			});
		}

		private BankSnapshot snapshot()
		{
			return BankSnapshotReader.readOpenBank(client).orElseThrow(AssertionError::new);
		}
	}

	private static ItemComposition composition(int template, int canonicalId)
	{
		return proxy(ItemComposition.class, (instance, method, arguments) -> {
			if ("getPlaceholderTemplateId".equals(method.getName())) return template;
			if ("getPlaceholderId".equals(method.getName())) return canonicalId;
			throw new AssertionError("Unexpected definition access " + method.getName());
		});
	}

	private static <T> T proxy(Class<T> type, InvocationHandler handler)
	{
		return type.cast(Proxy.newProxyInstance(BankCaptureReaderTest.class.getClassLoader(), new Class<?>[]{type}, handler));
	}
}

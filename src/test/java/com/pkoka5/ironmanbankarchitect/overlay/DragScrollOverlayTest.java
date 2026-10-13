package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectConfig;
import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectPlugin;
import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.RearrangeMode;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Exercises the actual renderer while only scroll and client drag observations change. */
@RunWith(Parameterized.class)
public class DragScrollOverlayTest
{
	@Parameterized.Parameters(name = "{0}")
	public static List<Object[]> modes()
	{
		return Arrays.asList(new Object[]{RearrangeMode.SWAP}, new Object[]{RearrangeMode.INSERT});
	}

	@Parameterized.Parameter
	public RearrangeMode mode;

	@Test public void draggingKeepsTargetCellAfterSourceScrollsAway() throws IOException
	{
		Fixture fixture = new Fixture(mode);
		fixture.scrollTo(fixture.move.getFromSlot());
		fixture.render();
		fixture.startDrag();
		fixture.scrollTo(fixture.move.getToSlot());
		BufferedImage image = fixture.render();
		writeRender(mode + "-visible-target.png", image);
		assertTrue("The actual visible target must keep its cyan highlight",
			cyanPixels(image, fixture.targetBounds()) > 20);
		assertFalse(fixture.text().contains("SYNCING"));
		assertFalse(fixture.text().contains("to FROM."));
	}

	@Test public void draggingKeepsTargetBadgeWhenBothEndpointsAreOffscreen() throws IOException
	{
		Fixture fixture = new Fixture(mode);
		fixture.startDrag();
		fixture.bothOffscreen();
		BufferedImage image = fixture.render();
		writeRender(mode + "-both-offscreen.png", image);
		assertTrue("The target direction must remain visible during the intervening scroll",
			cyanPixels(image, fixture.targetEdge()) > 20);
		assertEquals(0, cyanPixels(image, fixture.sourceEdge()));
		assertTrue(fixture.text(), fixture.text().endsWith("to " + fixture.targetLabel() + "."));
		assertFalse(fixture.text().contains("to FROM."));
	}

	@Test public void targetAboveViewportUsesTopBadgeWhileDragging()
	{
		Fixture fixture = new Fixture(mode);
		fixture.startDrag();
		fixture.bothOffscreen();
		fixture.slotBounds[fixture.move.getToSlot()] = new Rectangle(244, 50, 32, 32);
		BufferedImage image = fixture.render();
		assertTrue(cyanPixels(image, topEdge()) > 20);
		assertEquals(0, cyanPixels(image, bottomEdge()));
		assertTrue(fixture.text().contains("above this view"));
	}

	@Test public void partlyClippedTargetUsesEdgeBadgeUntilWholeSlotIsVisible()
	{
		Fixture fixture = new Fixture(mode);
		fixture.startDrag();
		fixture.bothOffscreen();
		fixture.slotBounds[fixture.move.getToSlot()] = new Rectangle(244, 196, 32, 32);
		BufferedImage image = fixture.render();
		assertTrue(cyanPixels(image, bottomEdge()) > 20);
		assertTrue(fixture.text().contains("below this view"));
		fixture.slotBounds[fixture.move.getToSlot()] = new Rectangle(244, 160, 32, 32);
		image = fixture.render();
		assertTrue(cyanPixels(image, fixture.targetBounds()) > 20);
		assertFalse(fixture.text().contains("Scroll "));
	}

	@Test public void cancelingDragRestoresSourceDirection()
	{
		Fixture fixture = new Fixture(mode);
		fixture.startDrag();
		fixture.bothOffscreen();
		assertTrue(cyanPixels(fixture.render(), Fixture.VIEWPORT) > 20);
		fixture.dragging = false;
		BufferedImage image = fixture.render();
		assertEquals(0, cyanPixels(image, Fixture.VIEWPORT));
		assertTrue(yellowPixels(image, fixture.sourceEdge()) > 20);
		assertTrue(fixture.text().endsWith("to FROM."));
	}

	@Test public void clickBelowDragThresholdKeepsSourceGuidance()
	{
		Fixture fixture = new Fixture(mode);
		fixture.draggedWidget = fixture.children[fixture.move.getFromSlot()];
		fixture.bothOffscreen();
		assertSourceGuidance(fixture);
	}

	@Test public void wrongItemSlotOrInterfaceCannotReplaceSourceGuidance()
	{
		for (int mismatch = 0; mismatch < 5; mismatch++)
		{
			Fixture fixture = new Fixture(mode);
			fixture.startDrag();
			fixture.bothOffscreen();
			int slot = fixture.move.getFromSlot();
			fixture.draggedWidget = mismatch == 3 ? null : fixture.itemWidget(
				mismatch == 0 ? slot + 1 : slot,
				mismatch == 4 ? -1 : mismatch == 1 ? fixture.move.getItemId() + 200 : fixture.move.getItemId(),
				mismatch == 2 ? InterfaceID.Bankmain.TABS : InterfaceID.Bankmain.ITEMS);
			assertSourceGuidance(fixture);
		}
	}

	@Test public void placeholderSourceUsesItsCanonicalItemIdentity()
	{
		Fixture fixture = new Fixture(mode);
		int source = fixture.move.getFromSlot();
		fixture.placeholderRawId = 50000;
		fixture.items[source] = new Item(fixture.placeholderRawId, 0);
		fixture.widgetItemIds[source] = fixture.placeholderRawId;
		fixture.startDrag();
		fixture.bothOffscreen();
		assertTrue(cyanPixels(fixture.render(), fixture.targetEdge()) > 20);
		assertFalse(fixture.text().contains("SYNCING"));
	}

	@Test public void missingSourceGeometryStillBlocksUnverifiedView()
	{
		for (boolean hidden : new boolean[]{false, true})
		{
			Fixture fixture = new Fixture(mode);
			fixture.startDrag();
			fixture.bothOffscreen();
			int source = fixture.move.getFromSlot();
			if (hidden) fixture.hidden[source] = true;
			else fixture.slotBounds[source] = null;
			assertEquals(0, cyanPixels(fixture.render(), Fixture.VIEWPORT));
			assertTrue(fixture.text().contains("SYNCING BANK"));
		}
	}

	@Test public void actualBankChangeCannotKeepStaleDropTarget()
	{
		Fixture fixture = new Fixture(mode);
		fixture.startDrag();
		fixture.bothOffscreen();
		assertTrue(cyanPixels(fixture.render(), fixture.targetEdge()) > 20);
		fixture.items[5] = new Item(9000, 1);
		assertEquals(0, cyanPixels(fixture.render(), Fixture.VIEWPORT));
		assertTrue(fixture.text().contains("SYNCING BANK"));
		fixture.widgetItemIds[5] = 9000;
		assertEquals(0, cyanPixels(fixture.render(), Fixture.VIEWPORT));
		fixture.tick++;
		assertEquals(0, cyanPixels(fixture.render(), Fixture.VIEWPORT));
		assertTrue(fixture.text().contains("Analyze"));
	}

	private static void assertSourceGuidance(Fixture fixture)
	{
		BufferedImage image = fixture.render();
		assertEquals(0, cyanPixels(image, Fixture.VIEWPORT));
		assertTrue(yellowPixels(image, fixture.sourceEdge()) > 20);
		assertTrue(fixture.text().endsWith("to FROM."));
	}

	private static Rectangle topEdge() { return new Rectangle(100, 100, 368, 30); }
	private static Rectangle bottomEdge() { return new Rectangle(100, 178, 368, 30); }
	private static void writeRender(String name, BufferedImage image) throws IOException
	{
		Path directory = Paths.get("build/reports/drag-scroll");
		Files.createDirectories(directory);
		ImageIO.write(image, "png", directory.resolve(name).toFile());
	}
	private static int cyanPixels(BufferedImage image, Rectangle area) { return colorPixels(image, area, true); }
	private static int yellowPixels(BufferedImage image, Rectangle area) { return colorPixels(image, area, false); }
	private static int colorPixels(BufferedImage image, Rectangle area, boolean cyan)
	{
		int count = 0;
		Rectangle clipped = area.intersection(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
		for (int y = clipped.y; y < clipped.y + clipped.height; y++)
			for (int x = clipped.x; x < clipped.x + clipped.width; x++)
			{
				Color color = new Color(image.getRGB(x, y), true);
				if (color.getAlpha() > 100 && (cyan
					? color.getRed() < 140 && color.getGreen() > 150 && color.getBlue() > 180
					: color.getRed() > 230 && color.getGreen() > 180 && color.getBlue() < 100)) count++;
			}
		return count;
	}

	private static final class Fixture
	{
		private static final Rectangle VIEWPORT = new Rectangle(100, 100, 368, 108);
		private final Item[] items = new Item[48];
		private final int[] widgetItemIds = new int[48];
		private final Rectangle[] slotBounds = new Rectangle[48];
		private final boolean[] hidden = new boolean[48];
		private final Widget[] children = new Widget[48];
		private final BankGuideController controller = new BankGuideController();
		private final Move move;
		private final BankGuideOverlay overlay;
		private final RearrangeMode mode;
		private boolean dragging;
		private Widget draggedWidget;
		private int tick = 100;
		private int placeholderRawId = -1;

		private Fixture(RearrangeMode mode)
		{
			this.mode = mode;
			List<BankPreviewItem> planned = new ArrayList<>();
			int[] actual = new int[48];
			for (int slot = 0; slot < actual.length; slot++)
			{
				actual[slot] = 1000 + slot;
				planned.add(new BankPreviewItem(actual[slot], "Item " + actual[slot], 1));
			}
			actual[0] = 1040;
			actual[40] = 1000;
			List<BankCategoryPreview> categories = new ArrayList<>();
			for (int index = 0; index < 10; index++)
				categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(index),
					index == 0 ? planned : Collections.emptyList()));
			BankOrganizationPreview preview = new BankOrganizationPreview(BankPresets.IRONMAN, categories);
			move = TabRouteAdvisor.assess(actual, BankTabPlan.fromPreview(preview), new int[9], 0, mode)
				.getMove().orElseThrow();
			assertEquals(mode == RearrangeMode.SWAP ? 0 : 40, move.getFromSlot());
			assertEquals(mode == RearrangeMode.SWAP ? 40 : 1, move.getToSlot());
			for (int slot = 0; slot < actual.length; slot++)
			{
				items[slot] = new Item(actual[slot], 1);
				widgetItemIds[slot] = actual[slot];
				final int index = slot;
				children[slot] = proxy(Widget.class, (instance, method, arguments) -> {
					switch (method.getName())
					{
						case "getId": return InterfaceID.Bankmain.ITEMS;
						case "getIndex": return index;
						case "getItemId": return widgetItemIds[index];
						case "getBounds": return slotBounds[index];
						case "isHidden": return hidden[index];
						default: throw new AssertionError("Unexpected item-widget read " + method.getName());
					}
				});
			}
			scrollTo(0);
			Widget grid = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return false;
					case "getBounds": return VIEWPORT;
					case "getChildren": case "getDynamicChildren": return children;
					default: throw new AssertionError("Unexpected grid read " + method.getName());
				}
			});
			ItemContainer bank = proxy(ItemContainer.class, (instance, method, arguments) -> {
				if (method.getName().equals("getItems")) return items;
				throw new AssertionError("Unexpected bank read " + method.getName());
			});
			Client client = proxy(Client.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "getWidget": return (Integer) arguments[0] == InterfaceID.Bankmain.ITEMS
						|| (Integer) arguments[0] == InterfaceID.Bankmain.ITEMS_CONTAINER ? grid : null;
					case "getItemContainer": assertEquals(InventoryID.BANK, ((Integer) arguments[0]).intValue()); return bank;
					case "getItemDefinition": return composition((Integer) arguments[0]);
					case "getVarbitValue": return (Integer) arguments[0] == VarbitID.BANK_INSERTMODE
						&& mode == RearrangeMode.INSERT ? 1 : 0;
					case "getVarcIntValue": return (Integer) arguments[0] == VarClientID.BANKTAGS_ACTIVE_TAG ? -1 : 0;
					case "getVarcStrValue": return "";
					case "getTickCount": return tick;
					case "getCanvasWidth": return 900;
					case "getCanvasHeight": return 600;
					case "isDraggingWidget": return dragging;
					case "getDraggedWidget": return draggedWidget;
					default: throw new AssertionError("Unexpected client read " + method.getName());
				}
			});
			IronmanBankArchitectConfig config = proxy(IronmanBankArchitectConfig.class,
				(instance, method, arguments) -> {
					switch (method.getName())
					{
						case "suggestNextMove": return true;
						case "hideSortedHighlights": return false;
						case "tabOrder": return "";
						default: throw new AssertionError("Unexpected config read " + method.getName());
					}
				});
			controller.publishBankAnalysis(BankAnalysisStatus.success(new BankCatalogSummary(48, 0,
				new EnumMap<>(ItemCategory.class)), preview));
			controller.setGuideEnabled(true);
			overlay = new BankGuideOverlay(new IronmanBankArchitectPlugin(), client, controller, config,
				new BankOverlayReservations(), null);
		}

		private void startDrag() { dragging = true; draggedWidget = children[move.getFromSlot()]; }
		private String targetLabel() { return mode == RearrangeMode.INSERT ? "DROP" : "TO"; }
		private String text() { return controller.getGuideProgressText(); }
		private Rectangle targetBounds() { return slotBounds[move.getToSlot()]; }
		private Rectangle targetEdge() { return mode == RearrangeMode.SWAP ? bottomEdge() : topEdge(); }
		private Rectangle sourceEdge() { return mode == RearrangeMode.SWAP ? topEdge() : bottomEdge(); }
		private void scrollTo(int slot) { scroll(slot / 8 * 36); }
		private void bothOffscreen() { scroll(72); }
		private void scroll(int pixels)
		{
			for (int slot = 0; slot < slotBounds.length; slot++)
				slotBounds[slot] = new Rectangle(100 + slot % 8 * 44, 100 + slot / 8 * 36 - pixels, 32, 32);
		}
		private BufferedImage render()
		{
			BufferedImage image = new BufferedImage(900, 600, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try { overlay.render(graphics); }
			finally { graphics.dispose(); }
			return image;
		}
		private Widget itemWidget(int index, int itemId, int widgetId)
		{
			return proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "getId": return widgetId;
					case "getIndex": return index;
					case "getItemId": return itemId;
					default: throw new AssertionError("Unexpected dragged-widget read " + method.getName());
				}
			});
		}
		private ItemComposition composition(int itemId)
		{
			return proxy(ItemComposition.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "getPlaceholderTemplateId": return itemId == placeholderRawId ? 1 : -1;
					case "getPlaceholderId": return itemId == placeholderRawId ? move.getItemId() : -1;
					case "getName": return "Item " + itemId;
					default: throw new AssertionError("Unexpected item-definition read " + method.getName());
				}
			});
		}
	}

	private static <T> T proxy(Class<T> type, InvocationHandler handler)
	{
		return type.cast(Proxy.newProxyInstance(DragScrollOverlayTest.class.getClassLoader(), new Class<?>[]{type}, handler));
	}
}

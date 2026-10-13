package com.pkoka5.ironmanbankarchitect.overlay;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectPlugin;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import org.junit.Test;

/** Real rendering and AWT input against API doubles which reject every game-state mutation. */
public class BankBlueprintOverlayTest
{
	@Test public void usesFullScrollViewportEvenWhenUnderlyingItemContentIsShort() throws IOException
	{
		Fixture fixture = new Fixture();
		fixture.nativeCount = 1;
		fixture.show(items(120));
		BufferedImage image = fixture.render();
		writeRender("normal.png", image);
		assertEquals("The area below the actual bank's short item content is still covered",
			fixture.backgroundAt(111, 530), image.getRGB(111, 530));
		assertTrue("Preview samples native item-child placement",
			fixture.widgetReads.contains(InterfaceID.Bankmain.ITEMS));
		assertEquals("Twelve complete native rows plus the clipped partial row", 104, fixture.drawn.size());
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 546));
		assertPaintConfinedTo(image, fixture.paintBounds());
		assertTrue(fixture.reservations.previewActive);
		assertEquals(137, fixture.originalScrollY);
	}

	@Test public void preservesBlankDuplicateAndPlaceholderSlotsWithoutRequestingBlankIcons()
	{
		Fixture fixture = new Fixture();
		BankPreviewItem real = item(1000);
		BankPreviewItem placeholder = new BankPreviewItem(CatalogItem.unknown(1002), 0, true);
		fixture.show(Arrays.asList(real, BankPreviewItem.blank(), placeholder, real));
		BufferedImage image = fixture.render();
		assertEquals(Arrays.asList(real, placeholder, real), fixture.drawn);
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 110));
		assertEquals(fixture.backgroundAt(162, 110), image.getRGB(162, 110));
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(258, 110));
		assertEquals("The provider's native placeholder sprite receives no extra alpha",
			Fixture.PLACEHOLDER_COLOR.getRGB(), image.getRGB(210, 110));
		assertEquals("Transparent native sprite pixels retain the original background",
			fixture.backgroundAt(110, 106), image.getRGB(110, 106));
	}

	@Test public void previewScrollClampsAtBothEndsAndDoesNotTouchNativeBankScroll()
	{
		Fixture fixture = shortFixture();
		fixture.show(items(80));
		fixture.render();
		assertTrue(fixture.wheel(120, 180, 1).isConsumed());
		fixture.render();
		assertEquals("Pixel scrolling includes the clipped tail of the previous row", 1016, fixture.drawn.get(0).getItemId());
		assertTrue(fixture.wheel(120, 180, 100).isConsumed());
		fixture.render();
		assertEquals(1024, fixture.drawn.get(0).getItemId());
		assertEquals(1079, fixture.drawn.get(fixture.drawn.size() - 1).getItemId());
		fixture.wheel(120, 180, -100);
		fixture.render();
		assertEquals(1000, fixture.drawn.get(0).getItemId());
		assertEquals(137, fixture.originalScrollY);
	}

	@Test public void equalSnapshotAndNativeTabSwitchPreserveEachViewsScroll()
	{
		Fixture fixture = shortFixture();
		List<BankPreviewItem> main = items(80), combat = items(2000, 80);
		fixture.overlay.show(snapshot(Map.of(0, main, 3, combat)));
		fixture.render();
		fixture.wheel(120, 180, 1);
		fixture.overlay.show(snapshot(Map.of(0, new ArrayList<>(main), 3, new ArrayList<>(combat))));
		fixture.render();
		assertEquals(1016, fixture.drawn.get(0).getItemId());
		fixture.currentTab = 1;
		fixture.render();
		assertEquals(2000, fixture.drawn.get(0).getItemId());
		fixture.wheel(120, 180, 1);
		fixture.render();
		assertEquals(2016, fixture.drawn.get(0).getItemId());
		fixture.currentTab = 0;
		fixture.render();
		assertEquals("Returning to All restores its own scroll", 1016, fixture.drawn.get(0).getItemId());
		fixture.currentTab = 1;
		fixture.render();
		assertEquals("Returning to Combat restores its separate scroll", 2016, fixture.drawn.get(0).getItemId());
		assertTrue(fixture.overlay.isActive());
		assertEquals(0, fixture.closed);
	}

	@Test public void copiesSuppliedSnapshotAndClampsScrollAfterResize() throws IOException
	{
		Fixture fixture = shortFixture();
		List<BankPreviewItem> snapshot = items(80);
		fixture.show(snapshot);
		snapshot.clear();
		fixture.render();
		assertEquals(56, fixture.drawn.size());
		fixture.wheel(120, 180, 100);
		fixture.render();
		assertEquals(1024, fixture.drawn.get(0).getItemId());
		fixture.viewport = new Rectangle(100, 100, 648, 510);
		fixture.bankBounds = new Rectangle(60, 30, 730, 620);
		fixture.nativePitchX = 64;
		fixture.nativePitchY = 32;
		fixture.nativeOriginX = 14;
		fixture.nativeOriginY = 12;
		BufferedImage resized = fixture.render();
		writeRender("resized.png", resized);
		assertEquals(1000, fixture.drawn.get(0).getItemId());
		assertEquals(80, fixture.drawn.size());
		assertEquals(Color.MAGENTA.getRGB(), resized.getRGB(118, 116));
		assertEquals("Horizontal spacing follows resized native item bounds",
			Color.MAGENTA.getRGB(), resized.getRGB(182, 116));
		assertEquals("Vertical spacing follows resized native item bounds",
			Color.MAGENTA.getRGB(), resized.getRGB(118, 148));
		assertPaintConfinedTo(resized, fixture.paintBounds());
		assertEquals(137, fixture.originalScrollY);
	}

	@Test public void bankPressClosesPreviewAndConsumesEntireGestureAfterClosing()
	{
		Fixture fixture = new Fixture();
		fixture.show(items(8));
		fixture.render();
		MouseEvent pressed = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
		assertSame(pressed, fixture.overlay.mousePressed(pressed));
		assertTrue(pressed.isConsumed());
		assertFalse(fixture.overlay.isActive());
		assertFalse(fixture.reservations.previewActive);
		assertEquals(1, fixture.closed);
		MouseEvent dragged = fixture.mouse(MouseEvent.MOUSE_DRAGGED, 850, 650);
		fixture.overlay.mouseDragged(dragged);
		assertTrue("The interrupted gesture cannot become a native bank drag", dragged.isConsumed());
		MouseEvent released = fixture.mouse(MouseEvent.MOUSE_RELEASED, 850, 650);
		fixture.overlay.mouseReleased(released);
		assertTrue(released.isConsumed());
		MouseEvent clicked = fixture.mouse(MouseEvent.MOUSE_CLICKED, 130, 150);
		fixture.overlay.mouseClicked(clicked);
		assertTrue("The closing click cannot withdraw the underlying real item", clicked.isConsumed());
		MouseEvent nextClick = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
		fixture.overlay.mousePressed(nextClick);
		assertFalse("Subsequent real-bank interaction is restored", nextClick.isConsumed());
		fixture.overlay.close();
		assertEquals(1, fixture.closed);
	}

	@Test public void bankChromeClosesPreviewAndWheelCannotScrollUnderlyingBank()
	{
		Fixture fixture = shortFixture();
		fixture.show(items(80));
		fixture.render();
		assertTrue(fixture.wheel(80, 50, 2).isConsumed());
		fixture.render();
		assertEquals(1000, fixture.drawn.get(0).getItemId());
		MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 80, 50);
		fixture.overlay.mousePressed(press);
		assertTrue("Bank header controls require preview dismissal before action", press.isConsumed());
		assertFalse(fixture.overlay.isActive());
	}

	@Test public void outsideInputPassesThroughWithoutClosingOrScrollingPreview()
	{
		Fixture fixture = new Fixture();
		fixture.show(items(80));
		fixture.render();
		MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 850, 650);
		fixture.overlay.mousePressed(press);
		assertFalse(press.isConsumed());
		MouseEvent release = fixture.mouse(MouseEvent.MOUSE_RELEASED, 850, 650);
		fixture.overlay.mouseReleased(release);
		assertFalse(release.isConsumed());
		MouseEvent click = fixture.mouse(MouseEvent.MOUSE_CLICKED, 850, 650);
		fixture.overlay.mouseClicked(click);
		assertFalse(click.isConsumed());
		assertFalse(fixture.wheel(850, 650, 3).isConsumed());
		assertTrue(fixture.overlay.isActive());
		assertEquals(0, fixture.closed);
		fixture.render();
		assertEquals(1000, fixture.drawn.get(0).getItemId());
	}

	@Test public void hideAndUnsafeOrClosedBankReleaseReservationsAndInputGuard()
	{
		for (int reason = 0; reason < 17; reason++)
		{
			Fixture fixture = new Fixture();
			fixture.show(items(80));
			fixture.render();
			switch (reason)
			{
				case 0: fixture.overlay.hide(); break;
				case 1: fixture.bankPresent = false; break;
				case 2: fixture.containerPresent = false; break;
				case 3: fixture.bankHidden = true; break;
				case 4: fixture.containerHidden = true; break;
				case 5: fixture.viewport = null; break;
				case 6: fixture.viewport = new Rectangle(100, 100, 200, 200); break;
				case 7: fixture.viewport = new Rectangle(650, 100, 460, 450); break;
				case 8: fixture.menuOpen = true; break;
				case 9: fixture.dragging = true; break;
				case 10: fixture.itemsPresent = false; break;
				case 11: fixture.itemsHidden = true; break;
				case 12: fixture.nativeCount = 0; break;
				case 13: fixture.nativeBadGeometry = true; break;
				case 14: fixture.framePresent = false; break;
				case 15: fixture.backingPresent = false; break;
				case 16: fixture.backgroundAvailable = false; break;
				default: throw new AssertionError();
			}
			fixture.render();
			assertFalse("Still active for unsafe/closed case " + reason, fixture.overlay.isActive());
			assertFalse(fixture.reservations.previewActive);
			MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
			fixture.overlay.mousePressed(press);
			assertFalse("Stale input guard for case " + reason, press.isConsumed());
			assertFalse(fixture.wheel(130, 150, 1).isConsumed());
			assertEquals(reason == 0 ? 0 : 1, fixture.closed);
		}
	}

	@Test public void initialShowAndTabChangeGuardBankPressBeforeNextRenderedFrame()
	{
		for (boolean changingTab : new boolean[]{false, true})
		{
			Fixture fixture = new Fixture();
			fixture.show(items(80));
			if (changingTab)
			{
				fixture.render();
				fixture.show(items(16));
			}
			MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
			fixture.overlay.mousePressed(press);
			assertTrue("Bank press escaped before render; changed tab=" + changingTab, press.isConsumed());
			assertFalse(fixture.overlay.isActive());
			assertFalse(fixture.reservations.previewActive);
			assertEquals(1, fixture.closed);
			MouseEvent drag = fixture.mouse(MouseEvent.MOUSE_DRAGGED, 150, 180);
			fixture.overlay.mouseDragged(drag);
			assertTrue("A pre-render dismissal cannot initiate a real-bank drag", drag.isConsumed());
			MouseEvent release = fixture.mouse(MouseEvent.MOUSE_RELEASED, 150, 180);
			fixture.overlay.mouseReleased(release);
			assertTrue(release.isConsumed());
			MouseEvent click = fixture.mouse(MouseEvent.MOUSE_CLICKED, 130, 150);
			fixture.overlay.mouseClicked(click);
			assertTrue(click.isConsumed());
		}
	}

	@Test public void wheelBetweenTabChangeAndNextRenderCannotReachUnderlyingBank()
	{
		Fixture fixture = shortFixture();
		fixture.show(items(80));
		fixture.render();
		fixture.show(items(16));
		assertTrue("Pending tab rendering still protects the bank from native scrolling",
			fixture.wheel(120, 180, 1).isConsumed());
		fixture.render();
		assertEquals(1000, fixture.drawn.get(0).getItemId());
		assertEquals(137, fixture.originalScrollY);
	}

	@Test public void heldMouseButtonClosesPreviewAndReleasesInputGuard()
	{
		Fixture fixture = new Fixture();
		fixture.show(items(8));
		fixture.mouseButton = 1;
		fixture.render();
		assertFalse(fixture.overlay.isActive());
		assertFalse(fixture.reservations.previewActive);
		assertEquals(1, fixture.closed);
		MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
		fixture.overlay.mousePressed(press);
		assertFalse(press.isConsumed());
	}

	@Test public void minimumAcceptedViewportCannotLeaveAnActivePreviewWithoutAnItemRow()
	{
		Fixture fixture = new Fixture();
		fixture.viewport = new Rectangle(100, 100, 382, 42);
		fixture.show(items(8));
		fixture.render();
		assertTrue(fixture.overlay.isActive());
		assertEquals(8, fixture.drawn.size());
		fixture.viewport.height = 41;
		fixture.render();
		assertFalse("An undersized viewport returns to the real bank", fixture.overlay.isActive());
	}

	@Test public void tilesNativeBackgroundAtItsOwnAnchorWithoutAddingChrome() throws IOException
	{
		Fixture fixture = new Fixture();
		fixture.backingBounds = new Rectangle(61, 31, 600, 590);
		fixture.show(items(8));
		BufferedImage image = fixture.render();
		writeRender("native-tile.png", image);
		for (int[] point : new int[][]{{111, 101}, {112, 101}, {111, 530}, {559, 549}})
			assertEquals("Native tile anchor at " + Arrays.toString(point),
				fixture.backgroundAt(point[0], point[1]), image.getRGB(point[0], point[1]));
		assertEquals(1, fixture.backgroundRequests);
		assertPaintConfinedTo(image, fixture.paintBounds());
	}

	@Test public void scalesUntiledNativeBackgroundAcrossItsBounds() throws IOException
	{
		Fixture fixture = new Fixture();
		fixture.backgroundTiling = false;
		fixture.show(items(8));
		BufferedImage image = fixture.render();
		writeRender("native-scaled.png", image);
		assertEquals(fixture.texture.getRGB(0, 0), image.getRGB(113, 128));
		assertEquals(fixture.texture.getRGB(1, 0), image.getRGB(535, 128));
		assertEquals(fixture.texture.getRGB(0, 1), image.getRGB(113, 530));
		assertEquals(fixture.texture.getRGB(1, 1), image.getRGB(535, 530));
		assertPaintConfinedTo(image, fixture.paintBounds());
	}

	@Test public void filledNativeRectangleNeedsNoBackgroundSprite()
	{
		Fixture fixture = new Fixture();
		fixture.backingType = WidgetType.RECTANGLE;
		fixture.backgroundAvailable = false;
		fixture.show(items(8));
		BufferedImage image = fixture.render();
		assertTrue(fixture.overlay.isActive());
		assertEquals(Fixture.SOLID_COLOR.getRGB(), image.getRGB(111, 530));
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 110));
		assertEquals("A filled rectangle never requests an unrelated sprite", 0, fixture.backgroundRequests);
		assertPaintConfinedTo(image, fixture.paintBounds());
	}

	@Test public void unknownUnfilledBackdropReturnsToTheRealBank()
	{
		Fixture fixture = new Fixture();
		fixture.backingType = WidgetType.RECTANGLE;
		fixture.backingFilled = false;
		fixture.show(items(8));
		BufferedImage image = fixture.render();
		assertFalse(fixture.overlay.isActive());
		assertEquals(1, fixture.closed);
		assertEquals(0, image.getRGB(114, 110));
		assertTrue(fixture.drawn.isEmpty());
	}

	@Test public void scrolledNativeOriginUsesContentPositionAndDynamicChildren()
	{
		Fixture fixture = new Fixture();
		fixture.useDynamicItems = true;
		fixture.show(items(16));
		for (int scroll : new int[]{0, 137, 173})
		{
			fixture.originalScrollY = scroll;
			BufferedImage image = fixture.render();
			assertEquals("Scroll " + scroll + " must not offset the preview's first content row",
				Color.MAGENTA.getRGB(), image.getRGB(114, 110));
			assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 146));
			assertEquals(fixture.backgroundAt(114, 132), image.getRGB(114, 132));
			assertEquals(16, fixture.drawn.size());
			assertEquals(scroll, fixture.originalScrollY);
		}
	}

	@Test public void nativeTabHeaderGapsCannotBecomePreviewRowSpacing()
	{
		for (int[] offsets : new int[][]{{0, 54, 90}, {0, 54, 108}})
		{
			Fixture fixture = new Fixture();
			fixture.nativeRowOffsets = offsets;
			fixture.nativeCount = 24;
			fixture.show(items(24));
			BufferedImage image = fixture.render();
			assertEquals("Header gaps " + Arrays.toString(offsets) + " are not native item pitch",
				Color.MAGENTA.getRGB(), image.getRGB(114, 146));
			assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 182));
			assertEquals(24, fixture.drawn.size());
		}
	}

	@Test public void unavailableAsyncItemIconDoesNotReplaceNativeBackground()
	{
		Fixture fixture = new Fixture();
		fixture.missingIconId = 1000;
		fixture.show(items(8));
		BufferedImage image = fixture.render();
		assertTrue(fixture.overlay.isActive());
		assertEquals(fixture.backgroundAt(114, 110), image.getRGB(114, 110));
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(162, 110));
	}

	@Test public void fullBankContainerCannotPaintOverTabsButtonsScrollbarOrBankTags() throws IOException
	{
		Fixture fixture = new Fixture();
		fixture.nativeCount = 1;
		fixture.fullContainer = fixture.containerBounds();
		for (int index = 0; index < fixture.controlOverrides.length; index++)
			fixture.controlOverrides[index] = fixture.controlBounds(index);
		Rectangle originalContainer = new Rectangle(fixture.fullContainer);
		Rectangle originalBank = new Rectangle(fixture.bankBounds);
		Rectangle[] originalControls = Arrays.stream(fixture.controlOverrides)
			.map(Rectangle::new).toArray(Rectangle[]::new);
		BufferedImage before = fixture.bankScreenshot();
		fixture.show(items(120));
		BufferedImage after = fixture.render(fixture.bankScreenshot());
		writeRender("preserves-chrome.png", after);
		assertTrue("The bank container includes controls outside the item band",
			fixture.fullContainer.contains(fixture.viewport));
		assertTrue(fixture.fullContainer.contains(fixture.controlBounds(0)));
		assertTrue(fixture.fullContainer.contains(fixture.controlBounds(1)));
		assertTrue(fixture.fullContainer.contains(fixture.controlBounds(2)));
		assertUnchangedOutside(before, after, new Rectangle(110, 100, 450, 450));
		assertEquals(Color.MAGENTA.getRGB(), after.getRGB(114, 110));
		assertNotEquals("The temporary preview replaces the real item content",
			before.getRGB(111, 530), after.getRGB(111, 530));
		assertEquals(originalContainer, fixture.fullContainer);
		assertEquals(originalBank, fixture.bankBounds);
		assertArrayEquals("Reading control bounds cannot mutate the native bank controls",
			originalControls, fixture.controlOverrides);
		for (int id : new int[]{InterfaceID.Bankmain.TABS, InterfaceID.Bankmain.BOTTOM, InterfaceID.Bankmain.SCROLLBAR})
			assertTrue("The native control geometry must be consulted", fixture.widgetReads.contains(id));
		assertTrue(fixture.wheel(80, 140, 3).isConsumed());
		fixture.render();
		assertEquals("Scrolling over Bank Tags cannot scroll the covered real bank or the preview",
			1000, fixture.drawn.get(0).getItemId());
		assertEquals(137, fixture.originalScrollY);
	}

	@Test public void scrollTrackAndBottomControlsBoundTheItemPaintArea()
	{
		Fixture fixture = new Fixture();
		fixture.controlOverrides[0] = new Rectangle(75, 68, 503, 25);
		fixture.controlOverrides[2] = new Rectangle(540, 115, 18, 410);
		BufferedImage before = fixture.bankScreenshot();
		fixture.show(items(120));
		BufferedImage after = fixture.render(fixture.bankScreenshot());
		assertTrue(fixture.overlay.isActive());
		assertUnchangedOutside(before, after, new Rectangle(110, 115, 430, 410));
		assertEquals("The scroll track's top clips the area below tabs too",
			Color.MAGENTA.getRGB(), after.getRGB(114, 119));
		assertEquals(96, fixture.drawn.size());
	}

	@Test public void missingHiddenOrInvalidNativeControlsCloseWithoutPainting()
	{
		for (int control = 0; control < 3; control++)
			for (int failure = 0; failure < 3; failure++)
			{
				Fixture fixture = new Fixture();
				fixture.show(items(16));
				fixture.render();
				if (failure == 0) fixture.controlsPresent[control] = false;
				else if (failure == 1) fixture.controlsHidden[control] = true;
				else fixture.controlOverrides[control] = new Rectangle(100, 100, 0, 0);
				BufferedImage before = fixture.bankScreenshot();
				BufferedImage after = fixture.render(fixture.bankScreenshot());
				assertUnchangedOutside(before, after, new Rectangle());
				assertFalse("Control " + control + ", failure " + failure, fixture.overlay.isActive());
				assertFalse(fixture.reservations.previewActive);
				assertTrue(fixture.drawn.isEmpty());
				assertEquals(1, fixture.closed);
				assertFalse(fixture.wheel(120, 180, 1).isConsumed());
				MouseEvent press = fixture.mouse(MouseEvent.MOUSE_PRESSED, 130, 150);
				fixture.overlay.mousePressed(press);
				assertFalse(press.isConsumed());
			}
	}

	@Test public void allShowsMainFirstAndStartsEveryDenseSectionOnANewRow() throws IOException
	{
		Fixture fixture = new Fixture();
		fixture.colorById = true;
		fixture.nativeSeparator = true;
		fixture.nativeRowOffsets = new int[]{0, 60, 96};
		fixture.nativeCount = 24;
		List<BankPreviewItem> main = items(1000, 9), combat = items(2000, 2), tools = items(3000, 9);
		fixture.overlay.show(snapshot(Map.of(0, main, 3, combat, 6, tools)));
		BufferedImage image = fixture.render();
		writeRender("native-all-sections.png", image);
		List<BankPreviewItem> expected = new ArrayList<>(main);
		expected.addAll(combat);
		expected.addAll(tools);
		assertEquals("All preserves Main followed by the nonempty planned physical tabs", expected, fixture.drawn);
		assertEquals(Fixture.color(1000).getRGB(), image.getRGB(114, 110));
		assertEquals(Fixture.color(1008).getRGB(), image.getRGB(114, 146));
		assertEquals("Combat starts in column zero after Main's incomplete second row",
			Fixture.color(2000).getRGB(), image.getRGB(114, 206));
		assertEquals(Fixture.color(2001).getRGB(), image.getRGB(162, 206));
		assertEquals("Tools get their own new row, not the remaining Combat columns",
			Fixture.color(3000).getRGB(), image.getRGB(114, 266));
		assertEquals(Fixture.color(3008).getRGB(), image.getRGB(114, 302));
		assertEquals("All borrows the native separator color and observed 24-pixel gap",
			Fixture.SEPARATOR_COLOR.getRGB(), image.getRGB(200, 190));
		assertEquals(Fixture.SEPARATOR_COLOR.getRGB(), image.getRGB(200, 250));
		assertPaintConfinedTo(image, fixture.paintBounds());
	}

	@Test public void numberedNativeTabsMapDenselyAcrossEmptyBlueprintCategories()
	{
		Fixture fixture = new Fixture();
		List<BankPreviewItem> main = items(1000, 3), combat = items(2000, 2), tools = items(3000, 2);
		fixture.overlay.show(snapshot(Map.of(0, main, 3, combat, 8, tools)));
		fixture.currentTab = 1;
		fixture.render();
		assertEquals("Physical tab one maps to the first nonempty category, not category one", combat, fixture.drawn);
		fixture.currentTab = 2;
		fixture.render();
		assertEquals(tools, fixture.drawn);
		fixture.currentTab = 9;
		BufferedImage image = fixture.render();
		assertTrue("An unmapped physical tab stays a view-only empty preview", fixture.overlay.isActive());
		assertTrue(fixture.drawn.isEmpty());
		assertEquals(fixture.backgroundAt(114, 110), image.getRGB(114, 110));
		fixture.currentTab = 0;
		fixture.render();
		assertEquals(Arrays.asList(main.get(0), main.get(1), main.get(2), combat.get(0), combat.get(1), tools.get(0), tools.get(1)), fixture.drawn);
		assertEquals(0, fixture.closed);
	}

	@Test public void emptyMainDoesNotInheritUnderlyingAllViewsFirstTabHeaderOffset()
	{
		Fixture fixture = new Fixture();
		fixture.nativeRowOffsets = new int[]{54, 90};
		List<BankPreviewItem> combat = items(2000, 10);
		fixture.overlay.show(snapshot(Map.of(3, combat)));
		BufferedImage image = fixture.render();
		assertEquals("Empty Main retains the leading numbered-section gap", fixture.backgroundAt(114, 110), image.getRGB(114, 110));
		assertEquals("All adds one separator gap, without inheriting the source's 54-pixel header offset",
			Color.MAGENTA.getRGB(), image.getRGB(114, 128));
		assertEquals(Color.MAGENTA.getRGB(), image.getRGB(114, 164));
		assertEquals(combat, fixture.drawn);
		fixture.currentTab = 1;
		image = fixture.render();
		assertEquals("A single numbered tab starts at the unshifted native item-layer origin",
			Color.MAGENTA.getRGB(), image.getRGB(114, 110));
		assertEquals(combat, fixture.drawn);
	}

	@Test public void nativeLeftTabClickSurvivesHeldButtonAndReleaseProcessing()
	{
		Fixture fixture = new Fixture();
		List<BankPreviewItem> main = items(1000, 3), combat = items(2000, 8);
		fixture.overlay.show(snapshot(Map.of(0, main, 3, combat)));
		MouseEvent press = fixture.tabMouse(MouseEvent.MOUSE_PRESSED, 1, MouseEvent.BUTTON1);
		assertSame(press, fixture.overlay.mousePressed(press));
		assertFalse("The native tab owns its left press even before first rendering", press.isConsumed());
		fixture.mouseButton = 1;
		fixture.render();
		assertTrue("A held tab press does not dismiss preview", fixture.overlay.isActive());
		MouseEvent jitter = fixture.mouse(MouseEvent.MOUSE_DRAGGED, press.getX() + 1, press.getY() + 1);
		fixture.overlay.mouseDragged(jitter);
		assertFalse(jitter.isConsumed());
		assertTrue("Small click jitter must not be treated as a deliberate tab drag", fixture.overlay.isActive());
		assertTrue(fixture.overlay.isTabView(menu(1, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		fixture.currentTab = 1;
		MouseEvent release = fixture.tabMouse(MouseEvent.MOUSE_RELEASED, 1, MouseEvent.BUTTON1);
		fixture.overlay.mouseReleased(release);
		assertFalse("Vanilla must receive the release corresponding to its press", release.isConsumed());
		fixture.render();
		assertTrue("Release can reach AWT before vanilla clears its held-button state", fixture.overlay.isActive());
		assertEquals(combat, fixture.drawn);
		fixture.mouseButton = 0;
		fixture.render();
		MouseEvent click = fixture.tabMouse(MouseEvent.MOUSE_CLICKED, 1, MouseEvent.BUTTON1);
		fixture.overlay.mouseClicked(click);
		assertFalse(click.isConsumed());
		assertTrue(fixture.overlay.isActive());
		fixture.mouseButton = 1;
		fixture.render();
		assertFalse("A later untracked held button is not exempted by the old tab click", fixture.overlay.isActive());
		assertEquals(1, fixture.closed);
	}

	@Test public void draggingANativeTabClosesPreviewWithoutTrappingVanillasRelease()
	{
		Fixture fixture = new Fixture();
		fixture.show(items(8));
		fixture.render();
		MouseEvent press = fixture.tabMouse(MouseEvent.MOUSE_PRESSED, 1, MouseEvent.BUTTON1);
		fixture.overlay.mousePressed(press);
		assertFalse(press.isConsumed());
		MouseEvent drag = fixture.mouse(MouseEvent.MOUSE_DRAGGED, 220, 82);
		fixture.overlay.mouseDragged(drag);
		assertFalse("A real tab gesture remains owned by vanilla", drag.isConsumed());
		assertFalse(fixture.overlay.isActive());
		assertFalse(fixture.reservations.previewActive);
		assertEquals(1, fixture.closed);
		MouseEvent release = fixture.mouse(MouseEvent.MOUSE_RELEASED, 220, 82);
		fixture.overlay.mouseReleased(release);
		assertFalse("Consuming a release after a passed press can leave native input held", release.isConsumed());
		MouseEvent click = fixture.mouse(MouseEvent.MOUSE_CLICKED, 220, 82);
		fixture.overlay.mouseClicked(click);
		assertFalse(click.isConsumed());
	}

	@Test public void rightTabClickAndNewTabButtonStillDismissAndConsumeTheirGestures()
	{
		for (boolean newTab : new boolean[]{false, true})
		{
			Fixture fixture = new Fixture();
			if (newTab) fixture.tabActions[1] = new String[]{"New tab"};
			fixture.show(items(8));
			fixture.render();
			MouseEvent press = fixture.tabMouse(MouseEvent.MOUSE_PRESSED, 1, newTab ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
			fixture.overlay.mousePressed(press);
			assertTrue("Only passive left View-tab clicks may remain in preview", press.isConsumed());
			assertFalse(fixture.overlay.isActive());
			MouseEvent release = fixture.tabMouse(MouseEvent.MOUSE_RELEASED, 1, newTab ? MouseEvent.BUTTON1 : MouseEvent.BUTTON3);
			fixture.overlay.mouseReleased(release);
			assertTrue(release.isConsumed());
			assertEquals(0, fixture.currentTab);
			assertEquals(1, fixture.closed);
		}
	}

	@Test public void tabViewAllowlistValidatesWidgetActionTypeIdentifierAndLiveAction()
	{
		Fixture fixture = new Fixture();
		fixture.show(items(8));
		for (MenuAction type : new MenuAction[]{MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY})
		{
			assertTrue(fixture.overlay.isTabView(menu(0, "View all items", 1, type, InterfaceID.Bankmain.TABS)));
			assertTrue(fixture.overlay.isTabView(menu(1, "View tab", 1, type, InterfaceID.Bankmain.TABS)));
		}
		assertFalse(fixture.overlay.isTabView(menu(1, "Collapse tab", 2, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 2, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 0, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 9, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(0, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 1, MenuAction.RUNELITE, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.ITEMS)));
		assertFalse(fixture.overlay.isTabView(menu(-1, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertFalse(fixture.overlay.isTabView(menu(10, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		fixture.tabActions[1] = new String[]{"Inspect", "View tab", "Collapse tab"};
		assertFalse(fixture.overlay.isTabView(menu(1, "View tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertTrue("Use the live action index instead of assuming View always has op one",
			fixture.overlay.isTabView(menu(1, "View tab", 2, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		fixture.tabActions[1] = new String[]{"New tab"};
		assertFalse(fixture.overlay.isTabView(menu(1, "New tab", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		fixture.tabHidden[0] = true;
		assertFalse(fixture.overlay.isTabView(menu(0, "View all items", 1, MenuAction.CC_OP, InterfaceID.Bankmain.TABS)));
		assertEquals("Validating menus does not select or rearrange native tabs", 0, fixture.currentTab);
	}

	@Test public void invalidNativeTabValueClosesPreviewWithoutPainting()
	{
		for (int tab : new int[]{-1, 10})
		{
			Fixture fixture = new Fixture();
			fixture.show(items(8));
			fixture.currentTab = tab;
			BufferedImage image = fixture.render();
			assertFalse(fixture.overlay.isActive());
			assertTrue(fixture.drawn.isEmpty());
			assertEquals(0, image.getRGB(114, 110));
			assertEquals(1, fixture.closed);
		}
	}

	private static Fixture shortFixture()
	{
		Fixture fixture = new Fixture();
		fixture.viewport = new Rectangle(100, 100, 460, 240);
		fixture.bankBounds = new Rectangle(60, 30, 600, 380);
		return fixture;
	}
	private static BankPreviewItem item(int id) { return new BankPreviewItem(id, "Item " + id, 1); }
	private static List<BankPreviewItem> items(int count) { return items(1000, count); }
	private static List<BankPreviewItem> items(int firstId, int count)
	{
		List<BankPreviewItem> result = new ArrayList<>();
		for (int index = 0; index < count; index++) result.add(item(firstId + index));
		return result;
	}
	private static BankOrganizationPreview snapshot(Map<Integer, List<BankPreviewItem>> sections)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int category = 0; category < 10; category++)
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(category),
				sections.getOrDefault(category, Collections.emptyList())));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}
	private static MenuOptionClicked menu(int tab, String option, int identifier, MenuAction type, int widgetId)
	{
		MenuEntry entry = proxy(MenuEntry.class, (instance, method, arguments) -> {
			switch (method.getName())
			{
				case "getParam0": return 10 + tab;
				case "getParam1": return widgetId;
				case "getType": return type;
				case "getIdentifier": return identifier;
				case "getOption": return option;
				default: throw new AssertionError("Unexpected read-only menu access " + method.getName());
			}
		});
		return new MenuOptionClicked(entry);
	}
	private static void writeRender(String name, BufferedImage image) throws IOException
	{
		Path directory = Paths.get("build/reports/bank-preview");
		Files.createDirectories(directory);
		ImageIO.write(image, "png", directory.resolve(name).toFile());
	}
	private static void assertPaintConfinedTo(BufferedImage image, Rectangle viewport)
	{
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
				if (!viewport.contains(x, y) && image.getRGB(x, y) != 0)
					fail("Paint outside item viewport at " + x + "," + y);
	}
	private static void assertUnchangedOutside(BufferedImage before, BufferedImage after, Rectangle paintBounds)
	{
		for (int y = 0; y < before.getHeight(); y++)
			for (int x = 0; x < before.getWidth(); x++)
				if (!paintBounds.contains(x, y) && before.getRGB(x, y) != after.getRGB(x, y))
					fail("Native bank chrome changed at " + x + "," + y);
	}

	private static final class Fixture
	{
		private static final Color PLACEHOLDER_COLOR = new Color(125, 125, 125);
		private static final Color SOLID_COLOR = new Color(64, 48, 34);
		private static final Color SEPARATOR_COLOR = new Color(20, 175, 185);
		private final Canvas canvas = new Canvas();
		private final BankOverlayReservations reservations = new BankOverlayReservations();
		private final List<BankPreviewItem> drawn = new ArrayList<>();
		private final List<Integer> widgetReads = new ArrayList<>();
		private final BankBlueprintOverlay overlay;
		private final BufferedImage texture = nativeTexture();
		private Rectangle viewport = new Rectangle(100, 100, 460, 450);
		private Rectangle bankBounds = new Rectangle(60, 30, 600, 590);
		private Rectangle backingBounds;
		private Rectangle fullContainer;
		private final Rectangle[] controlOverrides = new Rectangle[3];
		private final boolean[] controlsPresent = {true, true, true};
		private final boolean[] controlsHidden = new boolean[3];
		private final String[][] tabActions = new String[10][];
		private final boolean[] tabHidden = new boolean[10];
		private int originalScrollY = 137;
		private boolean bankPresent = true, containerPresent = true, bankHidden, containerHidden, menuOpen, dragging;
		private boolean itemsPresent = true, framePresent = true, backingPresent = true, backingFilled = true;
		private boolean itemsHidden, nativeBadGeometry, useDynamicItems;
		private boolean backgroundAvailable = true, backgroundTiling = true;
		private boolean nativeSeparator, colorById;
		private int backingType = WidgetType.GRAPHIC;
		private int nativeCount = 16, nativePitchX = 48, nativePitchY = 36, nativeOriginX = 10, nativeOriginY = 6;
		private int[] nativeRowOffsets;
		private int mouseButton, currentTab, closed, backgroundRequests, missingIconId = -1;

		private Fixture()
		{
			for (int tab = 0; tab < tabActions.length; tab++)
				tabActions[tab] = tab == 0 ? new String[]{"View all items"} : new String[]{"View tab", "Collapse tab"};
			Widget bank = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return bankHidden;
					case "getBounds": return bankBounds;
					default: throw new AssertionError("Unexpected bank-widget call " + method.getName());
				}
			});
			Widget container = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return containerHidden;
					case "getBounds": return containerBounds();
					default: throw new AssertionError("Unexpected viewport call " + method.getName());
				}
			});
			Widget items = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return itemsHidden;
					case "getBounds": return viewport == null ? null : new Rectangle(viewport.x + nativeOriginX,
						viewport.y + nativeOriginY, 7 * nativePitchX + 36, Math.max(1, (nativeCount + 7) / 8) * nativePitchY);
					case "getScrollY": return originalScrollY;
					case "getChildren": return useDynamicItems ? null : nativeItems();
					case "getDynamicChildren": return nativeItems();
					default: throw new AssertionError("Unexpected item-content call " + method.getName());
				}
			});
			Widget backdrop = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return false;
					case "getBounds": return backdropBounds();
					case "getType": return backingType;
					case "getSpriteId": return 123;
					case "getSpriteTiling": return backgroundTiling;
					case "getTextColor": return SOLID_COLOR.getRGB() & 0xFFFFFF;
					case "isFilled": return backingFilled;
					case "getDynamicChildren":
					case "getStaticChildren":
					case "getNestedChildren": return null;
					default: throw new AssertionError("Unexpected background-widget call " + method.getName());
				}
			});
			Widget backgroundLayer = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return false;
					case "getBounds": return bankBounds;
					case "getType": return WidgetType.LAYER;
					case "getDynamicChildren":
					case "getStaticChildren": return null;
					case "getNestedChildren": return backingPresent ? new Widget[]{backdrop} : null;
					default: throw new AssertionError("Unexpected background-layer call " + method.getName());
				}
			});
			Widget frame = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return false;
					case "getBounds": return bankBounds;
					case "getType": return WidgetType.LAYER;
					case "getDynamicChildren": return null;
					case "getStaticChildren": return new Widget[]{backgroundLayer};
					case "getNestedChildren": return null;
					default: throw new AssertionError("Unexpected frame-widget call " + method.getName());
				}
			});
			Widget[] controls = new Widget[3];
			for (int index = 0; index < controls.length; index++)
			{
				final int control = index;
				controls[index] = proxy(Widget.class, (instance, method, arguments) -> {
					switch (method.getName())
					{
						case "isHidden": return controlsHidden[control];
						case "getBounds": return controlBounds(control);
						case "getChild":
							if (control != 0) throw new AssertionError("Only native Tabs have action children");
							int tab = (Integer) arguments[0] - 10;
							return tab < 0 || tab >= 10 ? null : tabHeader(tab);
						default: throw new AssertionError("Unexpected bank-control call " + method.getName());
					}
				});
			}
			Client client = proxy(Client.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "getWidget":
						int id = (Integer) arguments[0];
						widgetReads.add(id);
						if (id == InterfaceID.Bankmain.UNIVERSE) return bankPresent ? bank : null;
						if (id == InterfaceID.Bankmain.ITEMS_CONTAINER) return containerPresent ? container : null;
						if (id == InterfaceID.Bankmain.ITEMS) return itemsPresent ? items : null;
						if (id == InterfaceID.Bankmain.FRAME) return framePresent ? frame : null;
						if (id == InterfaceID.Bankmain.TABS) return controlsPresent[0] ? controls[0] : null;
						if (id == InterfaceID.Bankmain.BOTTOM) return controlsPresent[1] ? controls[1] : null;
						if (id == InterfaceID.Bankmain.SCROLLBAR) return controlsPresent[2] ? controls[2] : null;
						throw new AssertionError("Unexpected widget id " + id);
					case "isMenuOpen": return menuOpen;
					case "isDraggingWidget": return dragging;
					case "getMouseCurrentButton": return mouseButton;
					case "getVarbitValue":
						assertEquals("Preview only reads native tab selection", VarbitID.BANK_CURRENTTAB, arguments[0]);
						return currentTab;
					case "getMouseCanvasPosition": return new net.runelite.api.Point(850, 650);
					default: throw new AssertionError("Unexpected client call (read-only preview): " + method.getName());
				}
			});
			overlay = new BankBlueprintOverlay(new IronmanBankArchitectPlugin(), client, item -> {
				drawn.add(item);
				if (item.getItemId() == missingIconId) return null;
				BufferedImage icon = new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = icon.createGraphics();
				try
				{
					graphics.setColor(item.isPlaceholder() ? PLACEHOLDER_COLOR : colorById ? color(item.getItemId()) : Color.MAGENTA);
					graphics.fillRect(4, 4, 12, 12);
				}
				finally { graphics.dispose(); }
				return icon;
			}, widget -> {
				assertSame("Use the discovered native background", backdrop, widget);
				backgroundRequests++;
				return backgroundAvailable ? texture : null;
			}, reservations, () -> closed++);
		}
		private void show(List<BankPreviewItem> items)
		{
			overlay.show(snapshot(Collections.singletonMap(0, items)));
		}
		private static Color color(int id)
		{
			return new Color((id * 13) & 255, (id * 29) & 255, (id * 47) & 255);
		}
		private Widget tabHeader(int tab)
		{
			return proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return tabHidden[tab];
					case "getIndex": return 10 + tab;
					case "getBounds": return tabRectangle(tab);
					case "getActions": return tabActions[tab];
					default: throw new AssertionError("Unexpected native-tab call " + method.getName());
				}
			});
		}
		private Rectangle tabRectangle(int tab)
		{
			Rectangle tabs = controlBounds(0);
			return tabs == null ? null : new Rectangle(tabs.x + tab * 48, tabs.y, 48, tabs.height);
		}
		private MouseEvent tabMouse(int type, int tab, int button)
		{
			Rectangle bounds = tabRectangle(tab);
			return new MouseEvent(canvas, type, 1L, 0, bounds.x + bounds.width / 2,
				bounds.y + bounds.height / 2, 1, button == MouseEvent.BUTTON3, button);
		}
		private Rectangle containerBounds()
		{
			if (fullContainer != null) return fullContainer;
			return viewport == null ? null : new Rectangle(viewport.x - 25, viewport.y - 52,
				viewport.width + 43, viewport.height + 84);
		}
		private Rectangle controlBounds(int index)
		{
			if (controlOverrides[index] != null) return controlOverrides[index];
			if (viewport == null) return null;
			Rectangle container = containerBounds();
			if (index == 0) return new Rectangle(container.x, viewport.y - 32, container.width, 32);
			if (index == 1) return new Rectangle(container.x, viewport.y + viewport.height, container.width, 32);
			return new Rectangle(viewport.x + viewport.width, viewport.y, 18, viewport.height);
		}
		private Rectangle paintBounds()
		{
			return new Rectangle(viewport.x + nativeOriginX, viewport.y,
				viewport.width - nativeOriginX, viewport.height);
		}
		private Rectangle backdropBounds() { return backingBounds == null ? bankBounds : backingBounds; }
		private int backgroundAt(int x, int y)
		{
			Rectangle bounds = backdropBounds();
			return texture.getRGB(Math.floorMod(x - bounds.x, texture.getWidth()),
				Math.floorMod(y - bounds.y, texture.getHeight()));
		}
		private Widget[] nativeItems()
		{
			Widget[] result = new Widget[nativeCount + (nativeSeparator ? 1 : 0)];
			for (int index = 0; index < nativeCount; index++)
			{
				final int slot = index;
				result[index] = proxy(Widget.class, (instance, method, arguments) -> {
					switch (method.getName())
					{
						case "isHidden": return false;
						case "getItemId": return 2000 + slot;
						case "getBounds":
							int rowOffset = nativeRowOffsets == null ? slot / 8 * nativePitchY : nativeRowOffsets[slot / 8];
							return nativeBadGeometry ? null : new Rectangle(viewport.x + nativeOriginX + slot % 8 * nativePitchX,
								viewport.y + nativeOriginY + rowOffset - originalScrollY, 36, 32);
						default: throw new AssertionError("Unexpected native item call " + method.getName());
					}
				});
			}
			if (nativeSeparator) result[nativeCount] = proxy(Widget.class, (instance, method, arguments) -> {
				switch (method.getName())
				{
					case "isHidden": return false;
					case "getItemId": return -1;
					case "getType": return WidgetType.LINE;
					case "getBounds": return new Rectangle(viewport.x + nativeOriginX, viewport.y + 54, viewport.width - nativeOriginX, 0);
					case "getTextColor": return SEPARATOR_COLOR.getRGB() & 0xFFFFFF;
					default: throw new AssertionError("Unexpected native-separator call " + method.getName());
				}
			});
			return result;
		}
		private static BufferedImage nativeTexture()
		{
			BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, new Color(50, 35, 22).getRGB());
			image.setRGB(1, 0, new Color(64, 46, 28).getRGB());
			image.setRGB(0, 1, new Color(73, 52, 31).getRGB());
			image.setRGB(1, 1, new Color(83, 59, 36).getRGB());
			return image;
		}
		private BufferedImage render()
		{
			return render(new BufferedImage(1000, 700, BufferedImage.TYPE_INT_ARGB));
		}
		private BufferedImage render(BufferedImage image)
		{
			drawn.clear();
			Graphics2D graphics = image.createGraphics();
			try { overlay.render(graphics); }
			finally { graphics.dispose(); }
			return image;
		}
		private BufferedImage bankScreenshot()
		{
			BufferedImage image = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try
			{
				graphics.setColor(new Color(12, 14, 20));
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
				graphics.setColor(new Color(58, 46, 32));
				graphics.fill(bankBounds);
				graphics.setColor(new Color(88, 79, 62));
				graphics.fill(containerBounds());
				graphics.setColor(new Color(60, 115, 130));
				graphics.fill(controlBounds(0));
				graphics.setColor(new Color(160, 105, 45));
				graphics.fill(controlBounds(1));
				graphics.setColor(new Color(135, 140, 150));
				graphics.fill(controlBounds(2));
				graphics.setColor(new Color(105, 65, 125));
				Rectangle container = containerBounds();
				graphics.fillRect(container.x, viewport.y, viewport.x + nativeOriginX - container.x, viewport.height);
				graphics.setColor(Color.WHITE);
				graphics.drawString("Tabs", container.x + 8, viewport.y - 10);
				graphics.drawString("Bank controls", container.x + 8, viewport.y + viewport.height + 21);
				graphics.drawString("Top row", container.x + 8, container.y + 15);
			}
			finally { graphics.dispose(); }
			return image;
		}
		private MouseEvent mouse(int type, int x, int y)
		{
			return new MouseEvent(canvas, type, 1L, 0, x, y, 1, false, MouseEvent.BUTTON1);
		}
		private MouseWheelEvent wheel(int x, int y, int rotation)
		{
			MouseWheelEvent event = new MouseWheelEvent(canvas, MouseEvent.MOUSE_WHEEL, 1L, 0, x, y,
				0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, rotation);
			assertSame(event, overlay.mouseWheelMoved(event));
			return event;
		}
	}
	private static <T> T proxy(Class<T> type, InvocationHandler handler)
	{
		return type.cast(Proxy.newProxyInstance(BankBlueprintOverlayTest.class.getClassLoader(), new Class<?>[]{type}, handler));
	}
}

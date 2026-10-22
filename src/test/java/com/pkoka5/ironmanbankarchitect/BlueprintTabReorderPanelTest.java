package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class BlueprintTabReorderPanelTest
{
	@Parameterized.Parameters(name = "{0}")
	public static List<Object[]> presets()
	{
		return Arrays.asList(new Object[]{BankPresets.IRONMAN}, new Object[]{BankPresets.MAIN});
	}

	private final BankPreset preset;
	public BlueprintTabReorderPanelTest(BankPreset preset) { this.preset = preset; }

	@Test public void everyHeaderHasAccessibleControlsAndOnlyTheEdgesAreDisabled() throws Exception
	{
		withFixture(fixture -> {
			for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			{
				JButton up = arrow(fixture.panel, tab, "up");
				JButton down = arrow(fixture.panel, tab, "down");
				assertEquals(tab > 0, up.isEnabled());
				assertEquals(tab < BankLayoutPlan.DESTINATION_COUNT - 1, down.isEnabled());
				for (JButton button : new JButton[]{up, down})
				{
					assertTrue(button.getToolTipText().startsWith(button.getAccessibleContext().getAccessibleName()));
					assertTrue(button.getToolTipText().contains("saved item positions"));
				}
			}
		});
	}

	@Test public void pendingMoveStopsPreviewAndPreventsASecondRequest() throws Exception
	{
		withFixture(fixture -> {
			String plan = fixture.model.plan.serialize();
			String orders = fixture.model.orders.serialize();
			AtomicInteger stopped = new AtomicInteger();
			fixture.panel.configureBankPreview(preview -> { if (preview == null) stopped.incrementAndGet(); });
			arrow(fixture.panel, 2, "down").doClick();
			assertEquals(1, fixture.model.calls);
			assertEquals(1, stopped.get());
			assertEquals(plan, fixture.model.plan.serialize());
			assertEquals(orders, fixture.model.orders.serialize());
			for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			{
				assertFalse(arrow(fixture.panel, tab, "up").isEnabled());
				assertFalse(arrow(fixture.panel, tab, "down").isEnabled());
			}
			fixture.panel.moveBlueprintTab(3, 4, fixture.model.editingContext());
			assertEquals("A pending move must be the only request", 1, fixture.model.calls);
			fixture.model.complete(false);
			assertEquals(plan, fixture.model.plan.serialize());
			assertEquals(orders, fixture.model.orders.serialize());
			assertTrue(arrow(fixture.panel, 2, "down").isEnabled());
		});
	}

	@Test public void mainMoveInsertsTheWholeDestinationAndFollowsItsContents() throws Exception
	{
		withFixture(fixture -> {
			List<List<String>> before = fixture.model.plan.getDestinations();
			fixture.panel.moveBlueprintTab(0, 3, fixture.model.editingContext());
			fixture.model.complete(true);
			assertEquals(before.get(1), fixture.panel.layoutPlan.getTagKeys(0));
			assertEquals(before.get(2), fixture.panel.layoutPlan.getTagKeys(1));
			assertEquals(before.get(3), fixture.panel.layoutPlan.getTagKeys(2));
			assertEquals(before.get(0), fixture.panel.layoutPlan.getTagKeys(3));
			for (int tab = 4; tab < 10; tab++) assertEquals(before.get(tab), fixture.panel.layoutPlan.getTagKeys(tab));
			assertTrue(fixture.panel.layoutPlan.keepsCurrentOrder(3));
			assertTrue(fixture.panel.layoutPlan.keepsCurrentOrder(1));
			assertFalse(fixture.panel.layoutPlan.keepsCurrentOrder(0));
			assertTrue(fixture.model.orders.serialize().contains("|3:995,4067"));
			assertTrue(fixture.model.orders.serialize().contains("|1:4151"));
			assertEquals(3, fixture.model.orders.destinations().get("995#0").tab);
			assertEquals(1, fixture.model.orders.destinations().get("4151#0").tab);
			assertTrue(fixture.model.orders.isCaptured());
			assertEquals(ColorScheme.BRAND_ORANGE, header(fixture.panel, 3).getForeground());
			assertEquals(Color.WHITE, header(fixture.panel, 0).getForeground());
		});
	}

	@Test public void headerButtonMovesTheWholeTabAndTheNeighbourUpOnePosition() throws Exception
	{
		withFixture(fixture -> {
			List<String> moved = fixture.model.plan.getTagKeys(2);
			List<String> shifted = fixture.model.plan.getTagKeys(3);
			arrow(fixture.panel, 2, "down").doClick();
			fixture.model.complete(true);
			assertEquals(moved, fixture.panel.layoutPlan.getTagKeys(3));
			assertEquals(shifted, fixture.panel.layoutPlan.getTagKeys(2));
			assertEquals(ColorScheme.BRAND_ORANGE, header(fixture.panel, 0).getForeground());
		});
	}

	@Test public void staleRenderedHeaderCannotChangeANewContext() throws Exception
	{
		withFixture(fixture -> {
			JButton stale = arrow(fixture.panel, 2, "down");
			fixture.model.epoch++;
			String plan = fixture.model.plan.serialize(), orders = fixture.model.orders.serialize();
			stale.doClick();
			assertNull("Stale actions must not create a pending write", fixture.model.pending);
			assertEquals(plan, fixture.model.plan.serialize());
			assertEquals(orders, fixture.model.orders.serialize());
		});
	}

	@Test public void changedContextDuringTheRequestRejectsItsCompletion() throws Exception
	{
		withFixture(fixture -> {
			String plan = fixture.model.plan.serialize(), orders = fixture.model.orders.serialize();
			arrow(fixture.panel, 2, "down").doClick();
			fixture.model.epoch++;
			fixture.model.complete(true);
			assertEquals(plan, fixture.model.plan.serialize());
			assertEquals(orders, fixture.model.orders.serialize());
			assertTrue(arrow(fixture.panel, 2, "down").isEnabled());
		});
	}

	@Test public void futureSavedOrderFormatDisablesReorderingWithoutOverwritingIt() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model(preset);
			model.orders = BlueprintItemOrders.parse("v99|keep-this-future-order");
			Fixture fixture = new Fixture(model);
			try
			{
				for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
				{
					assertFalse(arrow(fixture.panel, tab, "up").isEnabled());
					assertFalse(arrow(fixture.panel, tab, "down").isEnabled());
				}
				fixture.panel.moveBlueprintTab(2, 3, model.editingContext());
				assertNull(model.pending);
				assertEquals("v99|keep-this-future-order", model.orders.serialize());
			}
			finally { fixture.panel.shutdown(); }
		});
	}

	@Test public void compactHeaderButtonsFitInsideTheNarrowSidebar() throws Exception
	{
		withFixture(fixture -> {
			fixture.panel.setSize(225, 800);
			layout(fixture.panel);
			for (int tab = 0; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
			{
				JLabel heading = header(fixture.panel, tab);
				Container row = heading.getParent();
				for (String direction : new String[]{"up", "down"})
				{
					JButton button = arrow(fixture.panel, tab, direction);
					Rectangle bounds = SwingUtilities.convertRectangle(button.getParent(), button.getBounds(), row);
					assertTrue("Header has no layout", row.getWidth() > 0);
					assertTrue("Header control must have area", bounds.width > 0 && bounds.height > 0);
					assertTrue("Header control outside its row: " + bounds,
						new Rectangle(0, 0, row.getWidth(), row.getHeight()).contains(bounds));
					assertFalse("Header title overlaps its controls", heading.getBounds().intersects(bounds));
				}
			}
			BufferedImage image = new BufferedImage(225, 800, BufferedImage.TYPE_INT_ARGB);
			Graphics2D draw = image.createGraphics();
			try { fixture.panel.paint(draw); }
			finally { draw.dispose(); }
			try
			{
				Path directory = Paths.get("build", "reports", "blueprint-tab-reorder");
				Files.createDirectories(directory);
				ImageIO.write(image, "png", directory.resolve(preset.getType() + "-sidebar.png").toFile());
			}
			catch (java.io.IOException error) { throw new AssertionError(error); }
		});
	}

	private void withFixture(Consumer<Fixture> test) throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Fixture fixture = new Fixture(new Model(preset));
			try { test.accept(fixture); }
			finally { fixture.panel.shutdown(); }
		});
	}

	private static JLabel header(IronmanBankArchitectPanel panel, int tab)
	{
		String expected = tab == 0 ? "Main section" : "Tab " + tab;
		JLabel found = findHeader(panel.layoutRows, expected);
		if (found != null) return found;
		throw new AssertionError("Missing header " + expected);
	}

	private static JLabel findHeader(Container parent, String expected)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JLabel)
			{
				String text = ((JLabel) child).getText();
				if (text.equals(expected) || text.startsWith(expected + " -")) return (JLabel) child;
			}
			if (child instanceof Container)
			{
				JLabel nested = findHeader((Container) child, expected);
				if (nested != null) return nested;
			}
		}
		return null;
	}

	private static JButton arrow(IronmanBankArchitectPanel panel, int tab, String direction)
	{
		String tooltip = "Move entire blueprint " + (tab == 0 ? "Main section" : "Tab " + tab) + " " + direction;
		JButton found = findButton(header(panel, tab).getParent(), tooltip);
		if (found == null) throw new AssertionError("Missing control " + tooltip);
		return found;
	}

	private static JButton findButton(Container parent, String tooltip)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JButton && ((JButton) child).getToolTipText() != null
				&& ((JButton) child).getToolTipText().startsWith(tooltip)) return (JButton) child;
			if (child instanceof Container)
			{
				JButton nested = findButton((Container) child, tooltip);
				if (nested != null) return nested;
			}
		}
		return null;
	}

	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
	}

	private static final class Fixture
	{
		private final Model model;
		private final IronmanBankArchitectPanel panel;
		private Fixture(Model model)
		{
			this.model = model;
			panel = TestBankLayoutModel.panel(new BankGuideController(), () -> {}, (item, label) -> {}, () -> {}, model);
			panel.tabOrderButton.doClick();
		}
	}

	private static final class Model extends TestBankLayoutModel
	{
		private final BankPreset preset;
		private BankLayoutPlan plan;
		private BlueprintItemOrders orders = BlueprintItemOrders.parse(
			"v2|0:995,4067|2:4151|c:995#0:0:currency:currency|c:4151#0:2:gear:gear");
		private int calls, epoch, from, to;
		private String requestedContext;
		private Consumer<Boolean> pending;
		private Model(BankPreset preset)
		{
			this.preset = preset;
			plan = BankLayoutPlan.defaultFor(preset).withCurrentOrder(0, true).withCurrentOrder(2, true);
		}
		@Override public BankPreset preset() { return preset; }
		@Override public BankLayoutPlan plan() { return plan; }
		@Override public void save(BankLayoutPlan updated) { plan = updated; }
		@Override public BankLayoutOptions options() { return BankLayoutOptions.defaultFor(preset).withItemOrders(orders); }
		@Override public String editingContext() { return super.editingContext() + "|" + orders.serialize() + "|" + epoch; }
		@Override public void moveTab(int from, int to, String expectedContext, Consumer<Boolean> completed)
		{
			calls++;
			if (!expectedContext.equals(editingContext()) || !orders.isSupported()) { completed.accept(false); return; }
			assertNull(pending);
			this.from = from;
			this.to = to;
			requestedContext = expectedContext;
			pending = completed;
		}
		private void complete(boolean requestedSuccess)
		{
			assertNotNull("No asynchronous request to complete", pending);
			Consumer<Boolean> callback = pending;
			pending = null;
			boolean success = requestedSuccess && requestedContext.equals(editingContext());
			if (success)
			{
				plan = plan.withDestinationMoved(from, to, preset);
				orders = orders.withDestinationMoved(from, to);
			}
			callback.accept(success);
		}
	}
}

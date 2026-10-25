package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.runelite.client.ui.laf.RuneLiteLAF;
import org.junit.Test;
import static org.junit.Assert.*;

public class HeaderActionsPanelTest
{
	private static final String[] VERSIONS = {
		"0.9.9", "0.9.8", "0.9.7", "0.9.6", "0.9.5", "0.9.4", "0.9.3", "0.9.2", "0.9.1", "0.9.0",
		"0.8.1", "0.8.0", "0.7.1", "0.7.0", "0.6.0"
	};

	@Test public void shortcutsWorkInPlannerNoticeAndHistoryWithoutAcknowledgingUpdates() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Fixture fixture = new Fixture(ReleaseNoticePanel.RELEASE_ID);
			try
			{
				fixture.panel.onActivate();
				assertTrue(visible(fixture.panel.analyzeButton, fixture.panel));
				for (String action : new String[] {"Updates", "Settings", "GitHub issues"})
				{
					JButton shortcut = button(fixture.panel, action);
					assertEquals(action, shortcut.getActionCommand());
					assertEquals(action, shortcut.getAccessibleContext().getAccessibleName());
					assertNotNull(shortcut.getToolTipText());
					assertFalse(shortcut.getToolTipText().trim().isEmpty());
					assertNotNull("Icon required for " + action, shortcut.getIcon());
				}
				callbacks(fixture, 1);
				button(fixture.panel, "Updates").doClick();
				assertTrue(visible(versions(fixture.panel), fixture.panel));
				assertFalse(visible(fixture.panel.analyzeButton, fixture.panel));
				callbacks(fixture, 2);
				button(fixture.panel, "Back to planner").doClick();
				assertTrue(visible(fixture.panel.analyzeButton, fixture.panel));
				assertEquals(0, fixture.acknowledgements.get());

				fixture.seen.set("0.9.7");
				fixture.panel.onActivate();
				assertFalse(visible(fixture.panel.analyzeButton, fixture.panel));
				assertTrue(visible(button(fixture.panel, "Got it - continue"), fixture.panel));
				callbacks(fixture, 3);
				button(fixture.panel, "Updates").doClick();
				callbacks(fixture, 4);
				button(fixture.panel, "Back to planner").doClick();
				assertTrue("Browsing history must not dismiss the unseen notice",
					visible(button(fixture.panel, "Got it - continue"), fixture.panel));
				assertEquals("0.9.7", fixture.seen.get());
				assertEquals(0, fixture.acknowledgements.get());
				button(fixture.panel, "Got it - continue").doClick();
				assertEquals(ReleaseNoticePanel.RELEASE_ID, fixture.seen.get());
				assertEquals(1, fixture.acknowledgements.get());
				fixture.panel.onActivate();
				assertTrue(visible(fixture.panel.analyzeButton, fixture.panel));
				button(fixture.panel, "Updates").doClick();
				button(fixture.panel, "Back to planner").doClick();
				assertEquals(1, fixture.acknowledgements.get());
				assertEquals(0, fixture.analyses.get());
			}
			finally { fixture.panel.shutdown(); }
		});
	}

	@Test public void everyBundledReleaseCanBeReadWithoutChangingThePlannerOrGuide() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Fixture fixture = new Fixture(ReleaseNoticePanel.RELEASE_ID);
			try
			{
				fixture.controller.setBankOpen(true);
				fixture.controller.setGuideEnabled(true);
				fixture.panel.tabOrderButton.doClick();
				String plan = fixture.panel.layoutPlan.serialize();
				Object profile = fixture.panel.presetChooser.getSelectedItem();
				BankAnalysisStatus analysis = fixture.controller.bankAnalysisStatus();
				fixture.panel.onActivate();
				button(fixture.panel, "Updates").doClick();
				JComboBox<?> chooser = versions(fixture.panel);
				assertEquals(VERSIONS.length, chooser.getItemCount());
				assertTrue(chooser.getSelectedItem().toString().contains(ReleaseNoticePanel.RELEASE_ID));
				String latestNotes = visibleLabelText(fixture.panel);
				for (String feature : new String[] {"Melee", "Ranged", "Magic", "Void", "Skill", "Keep this proposal", "Flexible"})
					assertTrue("Missing latest-release feature: " + feature, latestNotes.contains(feature));
				for (int index = 0; index < VERSIONS.length; index++)
				{
					assertTrue(chooser.getItemAt(index).toString().contains(VERSIONS[index]));
					chooser.setSelectedIndex(index);
					String releaseNotes = visibleLabelText(fixture.panel);
					assertTrue("Selected release has no visible notes: " + VERSIONS[index],
						releaseNotes.contains(VERSIONS[index]));
					if (index > 0) assertNotEquals(latestNotes, releaseNotes);
					assertEquals(0, fixture.acknowledgements.get());
					assertEquals(0, fixture.analyses.get());
				}
				button(fixture.panel, "Back to planner").doClick();
				assertTrue(visible(fixture.panel.analyzeButton, fixture.panel));
				assertTrue("An already open layout editor should remain open",
					visible(fixture.panel.layoutRows, fixture.panel));
				assertEquals(plan, fixture.panel.layoutPlan.serialize());
				assertEquals(profile, fixture.panel.presetChooser.getSelectedItem());
				assertSame("Browsing releases must not replace the current bank analysis",
					analysis, fixture.controller.bankAnalysisStatus());
				assertTrue(fixture.controller.isGuideEnabled());
				assertTrue(fixture.controller.isBankOpen());
				assertFalse(fixture.controller.isCategoryAssignMode());
				assertEquals(ReleaseNoticePanel.RELEASE_ID, fixture.seen.get());
			}
			finally { fixture.panel.shutdown(); }
		});
	}

	@Test public void shortcutsAndHistoryRemainReadableAtSidebarWidths() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			LookAndFeel previous = UIManager.getLookAndFeel();
			Fixture fixture = null;
			try
			{
				UIManager.setLookAndFeel(new RuneLiteLAF());
				fixture = new Fixture(ReleaseNoticePanel.RELEASE_ID);
				for (int width : new int[] {225, 204})
				{
					fixture.seen.set(ReleaseNoticePanel.RELEASE_ID);
					fixture.panel.onActivate();
					render(fixture.panel, width, "planner");
					for (String action : new String[] {"Updates", "Settings", "GitHub issues"})
						assertFits(fixture.panel, button(fixture.panel, action));
					assertFits(fixture.panel, fixture.panel.presetChooser);
					fixture.seen.set("0.9.7");
					fixture.panel.onActivate();
					render(fixture.panel, width, "notice");
					assertFits(fixture.panel, button(fixture.panel, "Got it - continue"));
					button(fixture.panel, "Updates").doClick();
					versions(fixture.panel).setSelectedIndex(0);
					render(fixture.panel, width, "history");
					assertFits(fixture.panel, versions(fixture.panel));
					assertFits(fixture.panel, button(fixture.panel, "Back to planner"));
					for (String action : new String[] {"Updates", "Settings", "GitHub issues"})
						assertFits(fixture.panel, button(fixture.panel, action));
					for (int index = 0; index < VERSIONS.length; index++)
					{
						versions(fixture.panel).setSelectedIndex(index);
						layout(fixture.panel);
						assertVisibleNotesFitViewport(fixture.panel);
					}
					versions(fixture.panel).setSelectedIndex(VERSIONS.length - 1);
					render(fixture.panel, width, "history-oldest");
				}
			}
			catch (UnsupportedLookAndFeelException failure) { throw new RuntimeException(failure); }
			finally
			{
				if (fixture != null) fixture.panel.shutdown();
				try { UIManager.setLookAndFeel(previous); }
				catch (UnsupportedLookAndFeelException failure) { throw new RuntimeException(failure); }
			}
		});
	}

	private static void callbacks(Fixture fixture, int calls)
	{
		for (String action : new String[] {"Settings", "GitHub issues"})
		{
			JButton shortcut = button(fixture.panel, action);
			assertTrue(visible(shortcut, fixture.panel));
			assertTrue(shortcut.isEnabled());
			shortcut.doClick();
		}
		assertEquals(calls, fixture.settings.get());
		assertEquals(calls, fixture.issues.get());
	}

	private static void assertFits(IronmanBankArchitectPanel panel, Component child)
	{
		assertTrue(visible(child, panel));
		Rectangle bounds = SwingUtilities.convertRectangle(child.getParent(), child.getBounds(), panel);
		assertTrue("Control has no usable size: " + bounds, bounds.width > 0 && bounds.height >= 18);
		assertTrue("Control exceeds sidebar: " + bounds, new Rectangle(0, 0, panel.getWidth(), panel.getHeight()).contains(bounds));
	}

	private static void assertVisibleNotesFitViewport(Container parent)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JScrollPane && child.isVisible())
			{
				JScrollPane scroll = (JScrollPane) child;
				Component notes = scroll.getViewport().getView();
				if (notes instanceof JLabel && ((JLabel) notes).getText().contains("<html>"))
				{
					assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, scroll.getHorizontalScrollBarPolicy());
					assertTrue(scroll.getViewport().getExtentSize().width > 0);
					assertTrue("Release text is wider than its viewport", notes.getPreferredSize().width
						<= scroll.getViewport().getExtentSize().width);
				}
			}
			if (child instanceof Container && child.isVisible()) assertVisibleNotesFitViewport((Container) child);
		}
	}

	private static void render(IronmanBankArchitectPanel panel, int width, String name)
	{
		panel.setSize(width, 600);
		layout(panel);
		BufferedImage image = new BufferedImage(width, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		panel.paint(graphics);
		graphics.dispose();
		try
		{
			Path file = Paths.get("build/reports/header-actions/" + name + "-" + width + ".png");
			Files.createDirectories(file.getParent());
			ImageIO.write(image, "png", file.toFile());
		}
		catch (IOException failure) { throw new RuntimeException(failure); }
	}

	private static JButton button(Container parent, String action)
	{
		JButton found = findButton(parent, action);
		assertNotNull("Missing button: " + action, found);
		return found;
	}

	private static JButton findButton(Container parent, String action)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JButton)
			{
				JButton button = (JButton) child;
				if (action.equals(button.getActionCommand()) || action.equals(button.getText())) return button;
			}
			if (child instanceof Container)
			{
				JButton nested = findButton((Container) child, action);
				if (nested != null) return nested;
			}
		}
		return null;
	}

	private static JComboBox<?> versions(Container parent)
	{
		JComboBox<?> found = findVersions(parent);
		assertNotNull("Release version chooser is missing", found);
		return found;
	}

	private static JComboBox<?> findVersions(Container parent)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JComboBox && "Release version".equals(
				((JComboBox<?>) child).getAccessibleContext().getAccessibleName())) return (JComboBox<?>) child;
			if (child instanceof Container)
			{
				JComboBox<?> nested = findVersions((Container) child);
				if (nested != null) return nested;
			}
		}
		return null;
	}

	private static String visibleLabelText(Container parent)
	{
		StringBuilder text = new StringBuilder();
		for (Component child : parent.getComponents())
		{
			if (!child.isVisible()) continue;
			if (child instanceof JLabel) text.append(((JLabel) child).getText()).append('\n');
			if (child instanceof Container) text.append(visibleLabelText((Container) child));
		}
		return text.toString();
	}

	private static boolean visible(Component child, Container root)
	{
		for (Component current = child; current != null; current = current.getParent())
		{
			if (!current.isVisible()) return false;
			if (current == root) return true;
		}
		return false;
	}

	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
	}

	private static final class Fixture
	{
		private final AtomicReference<String> seen;
		private final AtomicInteger acknowledgements = new AtomicInteger();
		private final AtomicInteger settings = new AtomicInteger();
		private final AtomicInteger issues = new AtomicInteger();
		private final AtomicInteger analyses = new AtomicInteger();
		private final BankGuideController controller = new BankGuideController();
		private final IronmanBankArchitectPanel panel;

		private Fixture(String seen)
		{
			this.seen = new AtomicReference<>(seen);
			panel = TestBankLayoutModel.panel(controller, analyses::incrementAndGet);
			panel.configureReleaseNotice(this.seen::get, release -> {
				acknowledgements.incrementAndGet();
				this.seen.set(release);
			});
			panel.configureHeaderActions(settings::incrementAndGet, issues::incrementAndGet);
		}
	}
}

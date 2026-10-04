package com.pkoka5.ironmanbankarchitect;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReleaseNoticePanelTest
{
	@Test public void newReleaseAppearsAfterPreviousReleaseWasDismissedAndStaysDismissedAfterRestart() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			assertEquals("0.8.0", ReleaseNoticePanel.RELEASE_ID);
			AtomicReference<String> seen = new AtomicReference<>("0.7.1");
			JPanel normal = new JPanel();
			ReleaseNoticePanel panel = new ReleaseNoticePanel(normal, seen::get, seen::set);
			panel.opened();
			assertFalse(normal.isVisible());
			assertEquals("0.7.1", seen.get());
			panel.opened();
			assertFalse(normal.isVisible());
			assertEquals("0.7.1", seen.get());
			JButton dismiss = button(panel);
			assertNotNull(dismiss);
			dismiss.doClick();
			assertTrue(normal.isVisible());
			assertEquals("0.8.0", seen.get());
			panel.opened();
			assertTrue(normal.isVisible());
			JPanel restarted = new JPanel();
			new ReleaseNoticePanel(restarted, seen::get, seen::set).opened();
			assertTrue(restarted.isVisible());
			JPanel updated = new JPanel();
			new ReleaseNoticePanel(updated, seen::get, seen::set, "next-release").opened();
			assertFalse(updated.isVisible());
		});
	}

	@Test public void rendersNewFeatureNotesAtSidebarWidth() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			ReleaseNoticePanel panel = new ReleaseNoticePanel(new JPanel(), () -> "", ignored -> {});
			panel.setOpaque(true);
			panel.setBackground(new java.awt.Color(40, 40, 40));
			panel.setSize(204, 600);
			panel.opened();
			layout(panel);
			JLabel notes = notes(panel);
			assertNotNull(notes);
			assertTrue(notes.getText().contains("Bank Architect 0.8.0"));
			assertTrue(notes.getText().contains("Save current bank"));
			assertTrue(notes.getText().contains("Blueprint"));
			JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, notes);
			assertNotNull(scroll);
			assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, scroll.getHorizontalScrollBarPolicy());
			assertTrue(scroll.getViewport().getExtentSize().width > 0);
			assertTrue(notes.getPreferredSize().width <= scroll.getViewport().getExtentSize().width);
			JButton dismiss = button(panel);
			assertNotNull(dismiss);
			java.awt.Rectangle dismissBounds = SwingUtilities.convertRectangle(
				dismiss.getParent(), dismiss.getBounds(), panel);
			assertTrue(dismissBounds.width > 0);
			assertTrue(dismissBounds.height > 0);
			assertTrue(panel.getBounds().contains(dismissBounds));
			java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(204, 600, 2);
			java.awt.Graphics2D graphics = image.createGraphics();
			panel.paint(graphics);
			graphics.dispose();
			try {
				java.nio.file.Path file = java.nio.file.Paths.get("build/reports/release-notice/notice.png");
				java.nio.file.Files.createDirectories(file.getParent());
				javax.imageio.ImageIO.write(image, "png", file.toFile());
			} catch (java.io.IOException ex) { throw new RuntimeException(ex); }
		});
	}

	private static JButton button(Container parent)
	{
		for (Component child : parent.getComponents()) {
			if (child instanceof JButton && ((JButton) child).getText().startsWith("Got it")) return (JButton) child;
			if (child instanceof Container) { JButton found = button((Container) child); if (found != null) return found; }
		}
		return null;
	}
	private static JLabel notes(Container parent)
	{
		for (Component child : parent.getComponents()) {
			if (child instanceof JLabel && ((JLabel) child).getText().contains("What's new")) return (JLabel) child;
			if (child instanceof Container) { JLabel found = notes((Container) child); if (found != null) return found; }
		}
		return null;
	}
	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
	}
}

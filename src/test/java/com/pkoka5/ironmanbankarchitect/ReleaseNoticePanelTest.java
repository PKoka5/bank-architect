package com.pkoka5.ironmanbankarchitect;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReleaseNoticePanelTest
{
	@Test public void dismissedReleaseStaysDismissedAfterReopeningAndRestart() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			AtomicReference<String> seen = new AtomicReference<>("");
			JPanel normal = new JPanel();
			ReleaseNoticePanel panel = new ReleaseNoticePanel(normal, seen::get, seen::set);
			panel.opened();
			assertFalse(normal.isVisible());
			assertEquals("", seen.get());
			panel.opened();
			assertFalse(normal.isVisible());
			button(panel).doClick();
			assertTrue(normal.isVisible());
			assertEquals(ReleaseNoticePanel.RELEASE_ID, seen.get());
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

	@Test public void rendersAtSidebarWidth() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			ReleaseNoticePanel panel = new ReleaseNoticePanel(new JPanel(), () -> "", ignored -> {});
			panel.setOpaque(true);
			panel.setBackground(new java.awt.Color(40, 40, 40));
			panel.setSize(204, 600);
			panel.opened();
			layout(panel);
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
	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
	}
}

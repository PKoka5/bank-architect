package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresetType;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class MainPresetPanelTest
{
	@Test public void chooserOffersOnlyIronmanAndMainAndStartsOnIronman() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			IronmanBankArchitectPanel panel = panel(model);
			try
			{
				JComboBox<String> chooser = panel.presetChooser;
				assertEquals(2, chooser.getItemCount());
				assertEquals("Ironman", chooser.getItemAt(0));
				assertEquals("Main", chooser.getItemAt(1));
				assertEquals("Ironman", chooser.getSelectedItem());
				assertEquals(0, model.presetSelections);
				assertEquals(model.plan().serialize(), panel.layoutPlan.serialize());
			}
			finally { panel.shutdown(); }
		});
	}

	@Test public void choosingPresetsReloadsTheirPlansAndProtectsBothBundledProfiles() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			String originalIronman = model.plan().serialize();
			IronmanBankArchitectPanel panel = panel(model);
			try
			{
				panel.tabOrderButton.doClick();
				String ironmanContext = model.editingContext();
				assertTrue(panel.alchPileBox.isVisible());
				assertEquals("Ironman", panel.presetChooser.getSelectedItem());
				assertFalse(button(panel, "Delete").isEnabled());
				panel.presetChooser.setSelectedItem("Main");
				assertEquals(1, model.presetSelections);
				assertSame(BankPresets.MAIN, model.preset());
				assertTrue(panel.alchPileBox.isVisible());
				assertEquals(model.plan().serialize(), panel.layoutPlan.serialize());
				assertEquals(1, panel.layoutPlan.destinationOf("runes"));
				assertEquals(4, panel.layoutPlan.destinationOf("grimy-herbs"));
				assertNotEquals(ironmanContext, model.editingContext());
				assertEquals("Main", panel.presetChooser.getSelectedItem());
				assertFalse(button(panel, "Delete").isEnabled());
				button(panel, "Delete").doClick();
				assertEquals(0, model.profileDeletions);
				panel.presetChooser.setSelectedItem("Ironman");
				assertEquals(2, model.presetSelections);
				assertEquals(originalIronman, panel.layoutPlan.serialize());
				assertTrue(panel.alchPileBox.isVisible());
				assertEquals(ironmanContext, model.editingContext());
				assertFalse(button(panel, "Delete").isEnabled());
			}
			finally { panel.shutdown(); }
		});
	}

	@Test public void externalPresetRefreshUpdatesChooserAndPlanWithoutWritingSelection() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			IronmanBankArchitectPanel panel = panel(model);
			try
			{
				panel.tabOrderButton.doClick();
				model.active = BankPresetType.MAIN;
				panel.refreshSettings();
				panel.refreshSettings();
				assertEquals("Main", panel.presetChooser.getSelectedItem());
				assertTrue(panel.alchPileBox.isVisible());
				assertEquals(model.plan().serialize(), panel.layoutPlan.serialize());
				assertEquals("Main", panel.presetChooser.getSelectedItem());
				assertEquals(0, model.presetSelections);
				model.active = BankPresetType.IRONMAN;
				panel.refreshSettings();
				assertEquals("Ironman", panel.presetChooser.getSelectedItem());
				assertTrue(panel.alchPileBox.isVisible());
				assertEquals(model.plan().serialize(), panel.layoutPlan.serialize());
				assertEquals(0, model.presetSelections);
			}
			finally { panel.shutdown(); }
		});
	}

	@Test public void rendersMainPresetWithinNarrowSidebar() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.active = BankPresetType.MAIN;
			IronmanBankArchitectPanel panel = panel(model);
			try
			{
				Files.createDirectories(Paths.get("build/reports/main-preset"));
				for (int width : new int[] {230, 225})
				{
					panel.setSize(width, 800);
					layout(panel);
					Rectangle chooser = SwingUtilities.convertRectangle(panel.presetChooser.getParent(),
						panel.presetChooser.getBounds(), panel);
					assertTrue("Preset chooser exceeds sidebar", chooser.x >= 0 && chooser.x + chooser.width <= width);
					assertTrue("Preset chooser is not readable", chooser.height >= 18);
					JScrollPane scroll = find(panel, JScrollPane.class);
					assertTrue("Main content exceeds viewport", scroll.getViewport().getView().getPreferredSize().width
						<= scroll.getViewport().getWidth());
					BufferedImage image = new BufferedImage(width, 800, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = image.createGraphics();
					panel.paint(graphics);
					graphics.dispose();
					ImageIO.write(image, "png", Paths.get("build/reports/main-preset/sidebar-main-" + width + ".png").toFile());
				}
			}
			catch (java.io.IOException failure) { throw new RuntimeException(failure); }
			finally { panel.shutdown(); }
		});
	}

	private static IronmanBankArchitectPanel panel(Model model)
	{
		return TestBankLayoutModel.panel(new BankGuideController(),
			() -> {}, (item, label) -> {}, () -> {}, model);
	}

	private static JButton button(Container container, String text)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton && text.equals(((JButton) component).getText())) return (JButton) component;
			if (component instanceof Container)
			{
				JButton found = button((Container) component, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static <T extends Component> T find(Container container, Class<T> type)
	{
		for (Component component : container.getComponents())
		{
			if (type.isInstance(component)) return type.cast(component);
			if (component instanceof Container)
			{
				T found = find((Container) component, type);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static void layout(Container container)
	{
		container.doLayout();
		for (Component component : container.getComponents()) if (component instanceof Container) layout((Container) component);
	}

	private static final class Model extends TestBankLayoutModel
	{
		private BankPresetType active = BankPresetType.IRONMAN;
		private int presetSelections;
		private int profileDeletions;
		private final Map<BankPresetType, BankLayoutPlan> plans = new EnumMap<>(BankPresetType.class);

		private Model()
		{
			plans.put(BankPresetType.IRONMAN, BankLayoutPlan.defaultFor(BankPresets.IRONMAN));
			plans.put(BankPresetType.MAIN, BankLayoutPlan.defaultFor(BankPresets.MAIN));
		}

		@Override public BankPreset preset() { return BankPresets.forType(active); }
		@Override public BankLayoutPlan plan() { return plans.get(active); }
		@Override public void save(BankLayoutPlan plan) { plans.put(active, plan); }
		@Override public void selectPreset(BankPresetType type) { presetSelections++; active = type; }
		@Override public void deleteProfile(String name) { profileDeletions++; }
	}
}

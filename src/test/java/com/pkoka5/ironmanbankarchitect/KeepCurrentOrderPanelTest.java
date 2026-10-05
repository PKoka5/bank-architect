package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BlueprintItemOrders;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class KeepCurrentOrderPanelTest
{
	@Test public void choiceFollowsSelectedTabAndPersistsThroughModelAndAnalysisRefresh() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			JCheckBox keep = checkbox(panel);
			assertFalse(keep.isEnabled());
			panel.setPreview(preview());
			assertTrue(keep.isEnabled());
			assertFalse(keep.isSelected());
			panel.selectTab(3);
			keep.doClick();
			assertEquals(1, model.planSaves);
			assertTrue(model.plan.keepsCurrentOrder(3));
			assertFalse(model.plan.keepsCurrentOrder(0));
			assertTrue(status(panel).getText().contains("Preview shows saved/automatic order"));
			assertTrue(status(panel).getText().contains("items still move between tabs"));
			panel.selectTab(0);
			assertFalse(keep.isSelected());
			keep.doClick();
			assertEquals(2, model.planSaves);
			assertTrue(model.plan.keepsCurrentOrder(0));
			panel.selectTab(3);
			assertTrue(keep.isSelected());
			panel.setPreview(preview());
			assertEquals(3, tabs(panel).getSelectedIndex());
			assertTrue(keep.isSelected());
			keep.doClick();
			assertEquals(3, model.planSaves);
			assertFalse(model.plan.keepsCurrentOrder(3));
			assertTrue(model.plan.keepsCurrentOrder(0));
			panel.setPreview(preview());
			assertEquals(3, tabs(panel).getSelectedIndex());
			assertFalse(keep.isSelected());
		});
	}

	@Test public void draftingOrPendingSaveCannotChangeTheGuidanceOrderPolicy() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.plan = model.plan.withCurrentOrder(3, true);
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview());
			panel.selectTab(3);
			panel.beginEdit();
			assertFalse(checkbox(panel).isEnabled());
			checkbox(panel).doClick();
			assertEquals(0, model.planSaves);
			assertTrue(model.plan.keepsCurrentOrder(3));
			panel.saveDraft();
			assertNotNull(model.pending);
			assertFalse(checkbox(panel).isEnabled());
			panel.setPreview(preview());
			assertFalse(checkbox(panel).isEnabled());
			model.pending.accept(true);
			assertTrue(checkbox(panel).isEnabled());
			assertTrue(checkbox(panel).isSelected());
			assertEquals(3, tabs(panel).getSelectedIndex());
			panel.beginEdit();
			panel.cancelEdit();
			assertTrue(checkbox(panel).isEnabled());
			assertTrue(checkbox(panel).isSelected());
			assertEquals(0, model.planSaves);
		});
	}

	@Test public void rendersTheChoiceAndExplanationWithinStandardAndNarrowDialogs() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.plan = model.plan.withCurrentOrder(3, true);
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview());
			panel.selectTab(3);
			try
			{
				Files.createDirectories(Paths.get("build/reports/keep-current-order"));
				for (int width : new int[]{760, 560})
				{
					panel.setSize(width, 500);
					layout(panel);
					for (Component control : new Component[]{checkbox(panel), status(panel), button(panel, "Save current bank...")})
					{
						Rectangle bounds = SwingUtilities.convertRectangle(control.getParent(), control.getBounds(), panel);
						assertTrue("Control exceeds width " + width, bounds.x >= 0 && bounds.x + bounds.width <= width);
						assertTrue("Control exceeds height", bounds.y >= 0 && bounds.y + bounds.height <= panel.getHeight());
					}
					assertTrue(checkbox(panel).isSelected());
					BufferedImage image = new BufferedImage(width, 500, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = image.createGraphics();
					panel.paint(graphics);
					graphics.dispose();
					ImageIO.write(image, "png", Paths.get("build/reports/keep-current-order/"
						+ (width == 760 ? "editor.png" : "editor-narrow.png")).toFile());
				}
			}
			catch (java.io.IOException failure) { throw new RuntimeException(failure); }
		});
	}

	private static BlueprintEditorPanel panel(Model model)
	{
		return new BlueprintEditorPanel(model, item -> {
			JLabel label = new JLabel(Integer.toString(item.getItemId()), JLabel.CENTER);
			label.setForeground(java.awt.Color.WHITE);
			return label;
		}, ignored -> {});
	}

	private static BankOrganizationPreview preview()
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < 10; index++) categories.add(new BankCategoryPreview(
			BankPresets.IRONMAN.getCategories().get(index), Arrays.asList(
				new BankPreviewItem(100 + index * 3, "Item A", 1),
				new BankPreviewItem(101 + index * 3, "Item B", 1),
				new BankPreviewItem(102 + index * 3, "Item C", 1))));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}

	private static JCheckBox checkbox(Container panel) { return find(panel, JCheckBox.class); }
	private static JTextArea status(Container panel) { return find(panel, JTextArea.class); }
	private static JTabbedPane tabs(Container panel) { return find(panel, JTabbedPane.class); }
	private static JButton button(Container panel, String text)
	{
		for (Component component : panel.getComponents())
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

	private static <T extends Component> T find(Container panel, Class<T> type)
	{
		for (Component component : panel.getComponents())
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

	private static void layout(Container panel)
	{
		panel.doLayout();
		for (Component component : panel.getComponents()) if (component instanceof Container) layout((Container) component);
	}

	private static final class Model implements BankLayoutModel
	{
		private BankLayoutPlan plan = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		private int planSaves;
		private Consumer<Boolean> pending;
		public BankPreset preset() { return BankPresets.IRONMAN; }
		public BankLayoutPlan plan() { return plan; }
		public BankLayoutOptions options() { return BankLayoutOptions.DEFAULTS; }
		public void save(BankLayoutPlan plan) { this.plan = plan; planSaves++; }
		public void saveBlueprintEdit(Map<Integer, List<Integer>> orders,
			Map<String, BlueprintItemOrders.Destination> transfers, BankOrganizationPreview expected,
			String context, Consumer<Boolean> completed) { pending = completed; }
	}
}

package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlueprintEditorPanelTest
{
	@Test public void assigningCategoryWithinSameTabCanBeUndoneCancelledAndSaved() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = assignmentPanel(model);
			panel.beginEdit();
			assertFalse(button(panel, "Assign category...").isEnabled());
			click(panel, "13204");
			assertTrue(button(panel, "Assign category...").isEnabled());
			panel.assignSelectedCategory("frequently-used");
			assertTrue(button(panel, "Undo").isEnabled());
			assertEquals(0, model.saves);
			button(panel, "Undo").doClick();
			click(panel, "13204");
			panel.assignSelectedCategory("potions");
			panel.cancelEdit();
			assertEquals(0, model.saves);
			panel.selectTab(0);
			panel.beginEdit();
			click(panel, "13204");
			panel.assignSelectedCategory("frequently-used");
			panel.saveDraft();
			assertEquals("frequently-used", model.routes.get("13204#0").tag);
			assertEquals("currency", model.routes.get("13204#0").originalTag);
			assertEquals(0, model.routes.get("13204#0").tab);
			assertEquals(1, model.saves);
			BlueprintItemOrders stored = BlueprintItemOrders.parse(BlueprintItemOrders.EMPTY
				.withDestinations(model.routes).serialize());
			BankOrganizationPreview restored = stored.apply(previewForAssignment(), model.plan());
			assertEquals("frequently-used", restored.getCategories().get(0).getItems().stream()
				.filter(item -> item.getItemId() == 13204).findFirst().get().getLayoutTagKey());
		});
	}

	@Test public void assigningCategoryToAnotherTabPersistsDestinationAndRejectsStalePreview() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = assignmentPanel(model);
			panel.beginEdit();
			click(panel, "13204");
			model.context = "changed";
			panel.assignSelectedCategory("potions");
			assertFalse(button(panel, "Undo").isEnabled());
			panel.cancelEdit();
			panel.beginEdit();
			click(panel, "13204");
			panel.assignSelectedCategory("potions");
			panel.saveDraft();
			assertEquals("potions", model.routes.get("13204#0").tag);
			assertEquals(model.plan().destinationOf("potions"), model.routes.get("13204#0").tab);
			assertFalse(model.orders.get(0).contains(13204));
		});
	}

	@Test public void clickingSelectsGreenThenSwapsAndUndoRestoresOrder() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.beginEdit();
			click(panel, "995");
			assertEquals(java.awt.Color.GREEN, ((javax.swing.border.LineBorder)
				((JLabel) find(panel, "995")).getBorder()).getLineColor());
			assertFalse(button(panel, "Undo").isEnabled());
			click(panel, "13204");
			assertEquals(0, model.saves);
			assertTrue(button(panel, "Undo").isEnabled());
			button(panel, "Undo").doClick();
			panel.saveDraft();
			assertEquals(Arrays.asList(995, 2347, 13204), model.ids);
			panel.beginEdit();
			click(panel, "995");
			click(panel, "13204");
			panel.saveDraft();
			assertEquals(Arrays.asList(13204, 2347, 995), model.ids);
		});
	}

	@Test public void insertEndsOnClickedSlotInBothDirectionsAndSelectionCanBeCancelled() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.beginEdit();
			mode(panel).setSelectedItem("Insert");
			click(panel, "995");
			click(panel, "995");
			assertFalse(button(panel, "Undo").isEnabled());
			click(panel, "995");
			click(panel, "13204");
			panel.saveDraft();
			assertEquals(Arrays.asList(2347, 13204, 995), model.ids);
			panel.beginEdit();
			click(panel, "13204");
			click(panel, "995");
			panel.saveDraft();
			assertEquals(Arrays.asList(13204, 995, 2347), model.ids);
		});
	}

	private static javax.swing.JComboBox<?> mode(Container container)
	{
		for (Component component : container.getComponents()) {
			if (component instanceof javax.swing.JComboBox) return (javax.swing.JComboBox<?>) component;
			if (component instanceof Container) {
				javax.swing.JComboBox<?> found = mode((Container) component);
				if (found != null) return found;
			}
		}
		return null;
	}
	private static void click(BlueprintEditorPanel panel, String id)
	{
		JLabel cell = (JLabel) find(panel, id);
		cell.dispatchEvent(new MouseEvent(cell, MouseEvent.MOUSE_PRESSED, 0, 0, 5, 10, 1, false, MouseEvent.BUTTON1));
		cell.dispatchEvent(new MouseEvent(cell, MouseEvent.MOUSE_RELEASED, 1, 0, 5, 10, 1, false, MouseEvent.BUTTON1));
		cell.dispatchEvent(new MouseEvent(cell, MouseEvent.MOUSE_CLICKED, 2, 0, 5, 10, 1, false, MouseEvent.BUTTON1));
	}

	@Test public void pendingSaveCannotBeRepeatedAndRejectionKeepsDraftUntilCancel() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.deferSave = true;
			BlueprintEditorPanel panel = panel(model);
			panel.beginEdit();
			layout(panel);
			drag(panel, "13204", "2347");
			panel.saveDraft();
			panel.saveDraft();
			assertEquals(1, model.saves);
			assertFalse(button(panel, "Cancel").isEnabled());
			assertFalse(button(panel, "Save").isEnabled());
			panel.setPreview(null); // analysis has started while the client-thread save is queued
			model.pending.accept(false);
			assertFalse(button(panel, "Save").isEnabled());
			assertTrue(button(panel, "Cancel").isEnabled());
			panel.saveDraft();
			assertEquals(1, model.saves);
			panel.cancelEdit();
			panel.setPreview(preview());
			assertTrue(button(panel, "Edit this tab").isEnabled());
		});
	}

	@Test public void resetUsesTheLatestPreviewAfterAnAsynchronousRejection() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.deferSave = true;
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(item(995)), true))));
			button(panel, "Reset tab order").doClick();
			assertTrue(model.ids.isEmpty());
			panel.setPreview(preview());
			model.pending.accept(false);
			assertNotNull(find(panel, "13204"));
			assertFalse(button(panel, "Reset tab order").isEnabled());
		});
	}

	@Test public void draggingToTabHeaderSavesBothTabOrdersAndDestination() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(item(995).withLayoutTag("currency"))),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(item(4151).withLayoutTag("gear"))))));
			panel.beginEdit();
			layout(panel);
			javax.swing.JTabbedPane tabs = (javax.swing.JTabbedPane) panel.getComponent(0);
			JLabel source = (JLabel) find(panel, "995");
			java.awt.Rectangle bounds = tabs.getBoundsAt(1);
			Point point = SwingUtilities.convertPoint(tabs, new Point(bounds.x + 5, bounds.y + 5), source);
			dragTo(source, point);
			assertEquals(1, tabs.getSelectedIndex());
			assertEquals(0, model.saves);
			panel.saveDraft();
			assertEquals(Arrays.asList(4151, 995), model.orders.get(1));
			assertTrue(model.orders.get(0).isEmpty());
			assertEquals("gear", model.routes.get("995#0").tag);
		});
	}

	@Test public void crossTabDraftUndoRestoresBothTabsAndRoutes()
	{
		BankOrganizationPreview preview = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0),
				Arrays.asList(item(7).withLayoutTag("tools"), item(7).withLayoutTag("tools"))),
			new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(item(2).withLayoutTag("gear")))));
		BlueprintDraft draft = new BlueprintDraft(preview, BlueprintItemOrders.EMPTY, 0);
		draft.moveAcross(0, 1, 1, 0, "gear");
		assertEquals(Arrays.asList(7), draft.ids());
		draft.selectTab(1);
		assertEquals(Arrays.asList(7, 2), draft.ids());
		assertTrue(draft.transfers().containsKey("7#1"));
		draft.undo();
		assertEquals(Arrays.asList(2), draft.ids());
		assertEquals(2, draft.itemCount(0));
		assertTrue(draft.transfers().isEmpty());
		assertTrue(draft.orders().isEmpty());
	}

	@Test public void dragUndoCancelAndSaveUseOnlyTheDraft() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.beginEdit();
			layout(panel);
			drag(panel, "13204", "2347");
			assertEquals(0, model.saves);
			button(panel, "Undo").doClick();
			panel.saveDraft();
			assertEquals(Arrays.asList(995, 2347, 13204), model.ids);
			panel.beginEdit();
			layout(panel);
			drag(panel, "13204", "2347");
			panel.cancelEdit();
			assertEquals(1, model.saves);
			panel.beginEdit();
			layout(panel);
			drag(panel, "13204", "2347");
			panel.saveDraft();
			assertEquals(Arrays.asList(995, 13204, 2347), model.ids);
		});
	}

	@Test public void newerPreviewOrProfileBlocksSavingAStaleDraft() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.beginEdit();
			panel.setPreview(preview());
			panel.saveDraft();
			assertEquals(0, model.saves);
			assertFalse(button(panel, "Save").isEnabled());
			panel.cancelEdit();
			panel.beginEdit();
			model.context = "different profile";
			panel.saveDraft();
			assertEquals(0, model.saves);
		});
	}

	@Test public void draftMovePreservesPhysicalDuplicatesAndInsertionSemantics()
	{
		BlueprintDraft draft = new BlueprintDraft(Arrays.asList(item(7), item(2), item(7)));
		draft.move(0, 3);
		assertEquals(Arrays.asList(2, 7, 7), draft.ids());
		draft.undo();
		assertEquals(Arrays.asList(7, 2, 7), draft.ids());
		draft.move(0, -1);
		assertFalse(draft.canUndo());
	}

	@Test public void rendersReviewableEditor() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			BlueprintEditorPanel panel = panel(new Model());
			panel.beginEdit();
			click(panel, "995");
			layout(panel);
			BufferedImage image = new BufferedImage(760, 500, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D graphics = image.createGraphics();
			panel.paint(graphics);
			graphics.dispose();
			try
			{
				Files.createDirectories(Paths.get("build/reports/blueprint-editor"));
				ImageIO.write(image, "png", Paths.get("build/reports/blueprint-editor/editor.png").toFile());
				panel.setSize(560, 500);
				layout(panel);
				for (String name : new String[]{"Edit this tab", "Undo", "Cancel", "Save", "Reset tab order", "Assign category..."})
				{
					Component button = find(panel, name);
					java.awt.Rectangle bounds = SwingUtilities.convertRectangle(button.getParent(), button.getBounds(), panel);
					assertTrue(name + " exceeds editor width", bounds.x >= 0 && bounds.x + bounds.width <= panel.getWidth());
				}
				image = new BufferedImage(560, 500, BufferedImage.TYPE_INT_ARGB);
				graphics = image.createGraphics();
				panel.paint(graphics);
				graphics.dispose();
				ImageIO.write(image, "png", Paths.get("build/reports/blueprint-editor/editor-narrow.png").toFile());
			}
			catch (java.io.IOException ex) { throw new RuntimeException(ex); }
		});
	}

	private static BlueprintEditorPanel panel(Model model)
	{
		BlueprintEditorPanel panel = new BlueprintEditorPanel(model,
			item -> { JLabel label = new JLabel(Integer.toString(item.getItemId()), JLabel.CENTER);
				label.setForeground(java.awt.Color.WHITE); return label; }, ignored -> {});
		panel.setSize(760, 500);
		panel.setPreview(preview());
		return panel;
	}
	private static BlueprintEditorPanel assignmentPanel(Model model)
	{
		BlueprintEditorPanel panel = panel(model);
		panel.setPreview(previewForAssignment());
		return panel;
	}
	private static BankOrganizationPreview previewForAssignment()
	{
		java.util.List<BankCategoryPreview> categories = new java.util.ArrayList<>();
		for (int i = 0; i < BankPresets.IRONMAN.getCategories().size(); i++)
			categories.add(new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(i), i == 0
				? Arrays.asList(item(995).withLayoutTag("currency"), item(2347).withLayoutTag("frequently-used"),
					item(13204).withLayoutTag("currency")) : java.util.Collections.emptyList()));
		return new BankOrganizationPreview(BankPresets.IRONMAN, categories);
	}
	private static BankPreviewItem item(int id) { return new BankPreviewItem(id, "Item " + id, 1); }
	private static BankOrganizationPreview preview()
	{
		return new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(new BankCategoryPreview(
			BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(item(995), item(2347), item(13204)))));
	}
	private static void layout(Container container)
	{
		container.doLayout();
		for (Component component : container.getComponents()) if (component instanceof Container) layout((Container) component);
	}
	private static Component find(Container container, String text)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JButton && text.equals(((JButton) component).getText())) return component;
			if (component instanceof JLabel && text.equals(((JLabel) component).getText())) return component;
			if (component instanceof Container)
			{
				Component result = find((Container) component, text);
				if (result != null) return result;
			}
		}
		return null;
	}
	private static JButton button(Container panel, String text) { return (JButton) find(panel, text); }
	private static void drag(BlueprintEditorPanel panel, String from, String before)
	{
		JLabel source = (JLabel) find(panel, from);
		JLabel target = (JLabel) find(panel, before);
		Point point = SwingUtilities.convertPoint(target, new Point(2, 10), source);
		dragTo(source, point);
	}
	private static void dragTo(JLabel source, Point point)
	{
		source.dispatchEvent(new MouseEvent(source, MouseEvent.MOUSE_PRESSED, 0, 0, 5, 10, 1, false, MouseEvent.BUTTON1));
		source.dispatchEvent(new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, 1, MouseEvent.BUTTON1_DOWN_MASK,
			point.x, point.y, 0, false, MouseEvent.NOBUTTON));
		source.dispatchEvent(new MouseEvent(source, MouseEvent.MOUSE_RELEASED, 2, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
	}
	private static final class Model implements BankLayoutModel
	{
		int saves;
		List<Integer> ids;
		String context = "profile";
		boolean deferSave;
		Consumer<Boolean> pending;
		java.util.Map<Integer, List<Integer>> orders;
		java.util.Map<String, BlueprintItemOrders.Destination> routes;
		public void saveBlueprintEdit(java.util.Map<Integer, List<Integer>> orders,
			java.util.Map<String, BlueprintItemOrders.Destination> routes, BankOrganizationPreview expected,
			String context, Consumer<Boolean> completed)
		{
			this.orders = orders;
			this.routes = routes;
			this.ids = orders.values().iterator().next();
			saves++;
			if (deferSave) pending = completed; else completed.accept(true);
		}
		public BankPreset preset() { return BankPresets.IRONMAN; }
		public BankLayoutPlan plan() { return BankLayoutPlan.defaultFor(preset()).withTagAt("ammunition", 9); }
		public void save(BankLayoutPlan plan) {}
		public String editingContext() { return context; }
		public void saveItemOrder(int tab, List<Integer> ids, BankOrganizationPreview expected,
			String context, Consumer<Boolean> completed)
		{
			this.ids = ids;
			saves++;
			if (deferSave) pending = completed; else completed.accept(true);
		}
	}
}

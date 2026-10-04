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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BlueprintCapturePanelTest
{
	@Test
	public void captureNeedsAnAnalyzedPreviewAndSupportedOrders() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			assertFalse(captureButton(panel).isEnabled());
			panel.saveCurrentBank("Current bank");
			assertEquals(0, model.captures);

			panel.setPreview(preview(995));
			assertTrue(captureButton(panel).isEnabled());
			model.options = BankLayoutOptions.DEFAULTS.withItemOrders(BlueprintItemOrders.parse("v9|future"));
			panel.setPreview(preview(2347));
			assertFalse(captureButton(panel).isEnabled());
			panel.saveCurrentBank("Current bank");
			assertEquals(0, model.captures);
		});
	}

	@Test
	public void cancelledAndBlankNamesLeaveThePanelReady() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview(995));
			panel.saveCurrentBank(null);
			panel.saveCurrentBank("");
			panel.saveCurrentBank(" \t ");
			assertEquals(0, model.captures);
			assertTrue(captureButton(panel).isEnabled());
			assertTrue(button(panel, "Edit this tab").isEnabled());
		});
	}

	@Test
	public void aDraftMustBeFinishedBeforeCapturingTheBank() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview(995));
			panel.beginEdit();
			assertFalse(captureButton(panel).isEnabled());
			panel.saveCurrentBank("Current bank");
			assertEquals(0, model.captures);
			panel.cancelEdit();
			assertTrue(captureButton(panel).isEnabled());
		});
	}

	@Test
	public void aQueuedDraftSaveBlocksCaptureUntilItsCallback() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview(995));
			panel.beginEdit();
			panel.saveDraft();
			assertEquals(1, model.edits);
			assertFalse(captureButton(panel).isEnabled());
			panel.saveCurrentBank("Current bank");
			assertEquals(0, model.captures);
			model.pending.accept(true);
			assertTrue(captureButton(panel).isEnabled());
		});
	}

	@Test
	public void captureUsesTheCurrentPreviewAndContextAndWaitsForCompletion() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			BankOrganizationPreview expected = preview(995);
			panel.setPreview(expected);
			model.context = "new active profile";
			panel.saveCurrentBank("Current bank");
			assertEquals("Current bank", model.name);
			assertSame(expected, model.expected);
			assertEquals(model.context, model.expectedContext);
			assertEquals(1, model.captures);
			assertFalse(captureButton(panel).isEnabled());
			assertFalse(button(panel, "Edit this tab").isEnabled());
			panel.saveCurrentBank("Second bank");
			panel.beginEdit();
			assertEquals(1, model.captures);
			assertFalse(button(panel, "Cancel").isEnabled());

			panel.setPreview(preview(2347));
			model.pending.accept(true);
			assertNotNull(find(panel, "2347"));
			assertTrue(captureButton(panel).isEnabled());
			assertTrue(button(panel, "Edit this tab").isEnabled());
		});
	}

	@Test
	public void rejectionShowsTheLatestPreviewAndAllowsAnotherCapture() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(preview(995));
			panel.saveCurrentBank("Current bank");
			BankOrganizationPreview latest = preview(2347);
			panel.setPreview(latest);
			model.pending.accept(false);
			assertNotNull(find(panel, "2347"));
			assertTrue(captureButton(panel).isEnabled());
			assertTrue(button(panel, "Edit this tab").isEnabled());
			panel.saveCurrentBank("Retry");
			assertEquals(2, model.captures);
			assertSame(latest, model.expected);
		});
	}

	private static BlueprintEditorPanel panel(Model model)
	{
		return new BlueprintEditorPanel(model,
			item -> new JLabel(Integer.toString(item.getItemId())), ignored -> {});
	}

	private static BankOrganizationPreview preview(int id)
	{
		return new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(new BankCategoryPreview(
			BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(new BankPreviewItem(id, "Item " + id, 1)))));
	}

	private static JButton captureButton(Container panel) { return button(panel, "Save current bank..."); }
	private static JButton button(Container panel, String text) { return (JButton) find(panel, text); }

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

	private static final class Model implements BankLayoutModel
	{
		private int captures;
		private int edits;
		private String name;
		private String context = "active profile";
		private String expectedContext;
		private BankOrganizationPreview expected;
		private BankLayoutOptions options = BankLayoutOptions.DEFAULTS;
		private Consumer<Boolean> pending;

		public void captureCurrentBank(String name, BankOrganizationPreview expected,
			String expectedContext, Consumer<Boolean> completed)
		{
			this.name = name;
			this.expected = expected;
			this.expectedContext = expectedContext;
			captures++;
			pending = completed;
		}

		public void saveBlueprintEdit(Map<Integer, List<Integer>> orders,
			Map<String, BlueprintItemOrders.Destination> transfers, BankOrganizationPreview expected,
			String expectedContext, Consumer<Boolean> completed)
		{
			edits++;
			pending = completed;
		}

		public BankPreset preset() { return BankPresets.IRONMAN; }
		public BankLayoutPlan plan() { return BankLayoutPlan.defaultFor(preset()); }
		public void save(BankLayoutPlan plan) {}
		public String editingContext() { return context; }
		public BankLayoutOptions options() { return options; }
	}
}

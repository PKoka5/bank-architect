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
	@Test public void proposalViewerHasNoActionsAndCannotPublishOrSaveTheActiveBankPreview() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.proposal = previewForAssignment();
			int[] editingEvents = {0}, bankPreviews = {0};
			BlueprintEditorPanel view = new BlueprintEditorPanel(model,
				item -> new JLabel(Integer.toString(item.getItemId())), ignored -> editingEvents[0]++, true);
			view.setPreview(model.proposal);
			view.configureBankPreview(ignored -> bankPreviews[0]++);
			assertNull(((java.awt.BorderLayout) view.getLayout()).getLayoutComponent(java.awt.BorderLayout.SOUTH));
			for (String action : new String[]{"Edit this tab", "Save", "Save current bank...", "Review changes"})
				assertNull(action + " must be absent from the proposal viewer", button(view, action));
			click(view, "995");
			view.beginEdit();
			view.assignSelectedCategory("potions");
			view.saveDraft();
			view.saveCurrentBank("Must not save");
			view.rememberBlueprint(model.proposal, model.context);
			view.acceptBlueprint(model.proposal, model.proposal, model.context);
			view.selectTab(1);
			view.publishBankPreview();
			view.setPreview(preview());
			assertEquals(0, editingEvents[0]);
			assertEquals("Opening or switching proposal tabs cannot replace the active bank overlay", 0, bankPreviews[0]);
			assertEquals(0, model.saves);
			assertEquals(0, model.captures);
			assertEquals(0, model.remembered);
			assertEquals(0, model.accepted);
			assertEquals("", model.acceptedValue);
		});
	}

	@Test public void acceptanceRefusesStaleEffectivePreviewProposalContextDraftOrClosedAnalysis() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (int reason = 0; reason < 6; reason++)
			{
				Model model = new Model();
				BlueprintEditorPanel panel = panel(model);
				BankOrganizationPreview expected = preview();
				BankOrganizationPreview proposed = previewForAssignment();
				model.proposal = proposed;
				panel.setPreview(expected);
				String expectedContext = model.context;
				if (reason == 0) panel.setPreview(preview());
				else if (reason == 1) model.proposal = previewForAssignment();
				else if (reason == 2) model.context = "new profile or settings";
				else if (reason == 3) panel.beginEdit();
				else if (reason == 4) panel.setPreview(null);
				else proposed = null;
				panel.acceptBlueprint(expected, proposed, expectedContext);
				assertEquals("Refuse stale acceptance case " + reason, 0, model.accepted);
				assertEquals(0, model.saves);
				assertEquals("", model.acceptedValue);
			}
		});
	}

	@Test public void queuedAcceptanceBlocksOtherMutationsAndUnlocksAfterSuccessOrFailure() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (boolean success : new boolean[]{false, true})
			{
				Model model = new Model();
				model.deferSave = true;
				model.proposal = previewForAssignment();
				BlueprintEditorPanel panel = panel(model);
				BankOrganizationPreview expected = preview();
				panel.setPreview(expected);
				PreviewCapture bankPreview = new PreviewCapture();
				panel.configureBankPreview(bankPreview);
				panel.publishBankPreview();
				assertSame(expected, bankPreview.snapshot);
				panel.acceptBlueprint(expected, model.proposal, model.context);
				assertEquals(1, model.accepted);
				assertSame(expected, model.acceptedExpected);
				assertSame(model.proposal, model.acceptedProposal);
				assertEquals(model.context, model.acceptedContext);
				assertNull("Pending acceptance withdraws the bank overlay", bankPreview.snapshot);
				for (String action : new String[]{"Edit this tab", "Save", "Cancel", "Save current bank...", "Review changes"})
					assertFalse(action, button(panel, action).isEnabled());
				panel.beginEdit();
				panel.saveDraft();
				panel.saveCurrentBank("Must wait");
				panel.rememberBlueprint(expected, model.context);
				panel.acceptBlueprint(expected, model.proposal, model.context);
				assertEquals(1, model.accepted);
				assertEquals(0, model.saves);
				assertEquals(0, model.captures);
				assertEquals(0, model.remembered);
				assertEquals("", model.acceptedValue);
				model.pending.accept(success);
				assertTrue(button(panel, "Edit this tab").isEnabled());
				assertTrue(button(panel, "Review changes").isEnabled());
				assertTrue(button(panel, "Save current bank...").isEnabled());
				assertSame(expected, bankPreview.snapshot);
				assertEquals(success, !model.acceptedValue.isEmpty());
				assertEquals("profile", model.context);
			}
		});
	}

	@Test public void acceptanceCallbackRendersAnyNewAnalysisReceivedWhileSaving() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (boolean success : new boolean[]{false, true})
			{
				Model model = new Model();
				model.deferSave = true;
				model.proposal = previewForAssignment();
				BlueprintEditorPanel panel = panel(model);
				BankOrganizationPreview expected = preview();
				panel.setPreview(expected);
				panel.acceptBlueprint(expected, model.proposal, model.context);
				BankOrganizationPreview latest = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
					new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(item(4151)))));
				panel.setPreview(latest);
				model.pending.accept(success);
				assertNotNull("The completed save must render the latest analysis", find(panel, "4151"));
				assertNull("The prior preview cannot remain clickable after the callback", find(panel, "995"));
				assertTrue(button(panel, "Edit this tab").isEnabled());
			}
		});
	}

	@Test public void reviewComparesTheAcceptedSnapshotWithTheFreshProposalInsteadOfTheKeptTarget() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BankOrganizationPreview kept = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(
					item(995).withLayoutTag("currency"), item(2347).withLayoutTag("frequently-used"))),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(item(4151).withLayoutTag("gear")))));
			model.proposal = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(item(2347).withLayoutTag("frequently-used"))),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(
					item(4151).withLayoutTag("gear"), item(995).withLayoutTag("gear").withPlacementReason("New rule fixture")))));
			model.acceptedValue = AcceptedBlueprint.capture(kept, model.context);
			model.reference = BlueprintChanges.capture(model.proposal);
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(kept);
			String report = panel.reviewText();
			assertTrue(report, report.contains("Saved rules"));
			assertTrue(report, report.contains("1 destination/category changes"));
			assertTrue(report, report.contains("0 new bank entries"));
			assertTrue(report, report.contains("0 remembered entries currently absent"));
			assertTrue(report, report.contains("Item 995"));
			assertTrue(report, report.contains("New rule fixture"));
			assertEquals(0, model.accepted);
			assertEquals(0, model.saves);
		});
	}

	@Test public void referenceReviewIsExplicitAndCannotAcceptAnEditingOrStalePreview() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			BankOrganizationPreview expected = preview();
			panel.setPreview(expected);
			assertEquals("", model.reference);
			assertTrue(panel.reviewText().contains("No remembered blueprint"));
			assertTrue(button(panel, "Review changes").isEnabled());
			panel.beginEdit();
			assertFalse(button(panel, "Review changes").isEnabled());
			panel.rememberBlueprint(expected, model.context);
			assertEquals(0, model.remembered);
			panel.cancelEdit();
			panel.rememberBlueprint(expected, "stale");
			panel.rememberBlueprint(preview(), model.context);
			assertEquals(0, model.remembered);
			panel.rememberBlueprint(expected, model.context);
			assertEquals(1, model.remembered);
			assertEquals(0, model.saves);
			assertTrue(panel.reviewText().contains("0 destination/category changes"));
			panel.setPreview(null);
			assertFalse(button(panel, "Review changes").isEnabled());
			assertTrue(panel.reviewText().contains("Analyze"));
		});
	}

	@Test public void referenceSaveDisablesEditingUntilItsCallbackCompletes() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			model.deferSave = true;
			BlueprintEditorPanel panel = panel(model);
			BankOrganizationPreview expected = preview();
			panel.setPreview(expected);
			panel.rememberBlueprint(expected, model.context);
			assertFalse(button(panel, "Edit this tab").isEnabled());
			assertFalse(button(panel, "Review changes").isEnabled());
			panel.rememberBlueprint(expected, model.context);
			assertEquals(1, model.remembered);
			model.pending.accept(false);
			assertTrue(button(panel, "Review changes").isEnabled());
		});
	}

	@Test public void bankPreviewPublishesAllPhysicalTabsWithoutSavingOrChangingProfile() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			PreviewCapture capture = new PreviewCapture();
			panel.configureBankPreview(capture);
			BankPreviewItem duplicate = new BankPreviewItem(
				com.pkoka5.ironmanbankarchitect.catalog.CatalogItem.unknown(7), 6, false, Arrays.asList(2, 0, 4));
			BankCategoryPreview main = BankCategoryPreview.fromLogicalItems(
				BankPresets.IRONMAN.getCategories().get(0), Arrays.asList(duplicate, BankPreviewItem.blank(), item(995)));
			BankCategoryPreview combat = new BankCategoryPreview(
				BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(item(4151), BankPreviewItem.blank()));
			String originalPlan = model.plan().serialize();
			BankOrganizationPreview original = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(main, combat));
			panel.setPreview(original);
			assertSame(original, capture.snapshot);
			assertEquals(2, capture.snapshot.getCategories().size());
			assertEquals(Arrays.asList(7, 7, 7, -1, 995), ids(capture.items(0)));
			assertEquals(2, capture.items(0).get(0).getQuantity());
			assertTrue(capture.items(0).get(1).isPlaceholder());
			assertEquals(4, capture.items(0).get(2).getQuantity());
			assertTrue(capture.items(0).get(3).isBlank());
			List<BankPreviewItem> mainSnapshot = capture.items(0);
			javax.swing.JTabbedPane tabs = (javax.swing.JTabbedPane) panel.getComponent(0);
			tabs.setSelectedIndex(1);
			assertSame("Editor tab selection cannot narrow the bank snapshot", original, capture.snapshot);
			assertEquals(Arrays.asList(7, 7, 7, -1, 995), ids(capture.items(0)));
			assertEquals(Arrays.asList(4151, -1), ids(capture.items(1)));
			assertEquals(Arrays.asList(7, 7, 7, -1, 995), ids(mainSnapshot));
			assertThrows(UnsupportedOperationException.class, () -> capture.snapshot.getCategories().clear());
			assertThrows(UnsupportedOperationException.class, () -> capture.items(0).clear());
			assertEquals(1, tabs.getSelectedIndex());
			assertEquals(0, model.saves);
			assertNull(model.orders);
			assertSame(BankPresets.IRONMAN, model.preset());
			assertEquals("profile", model.context);
			assertEquals(originalPlan, model.plan().serialize());
		});
	}

	@Test public void bankPreviewShowsSwapAndInsertDraftsWhileUndoAndCancelPreserveQueuedSnapshots() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (String movement : new String[]{"Swap", "Insert"})
			{
				Model model = new Model();
				BlueprintEditorPanel panel = panel(model);
				PreviewCapture capture = new PreviewCapture();
				panel.configureBankPreview(capture);
				button(panel, "Edit this tab").doClick();
				mode(panel).setSelectedItem(movement);
				click(panel, "995");
				click(panel, "13204");
				List<Integer> expected = "Swap".equals(movement)
					? Arrays.asList(13204, 2347, 995) : Arrays.asList(2347, 13204, 995);
				assertEquals(movement, expected, ids(capture.items(0)));
				List<BankPreviewItem> queuedSnapshot = capture.items(0);
				assertThrows(UnsupportedOperationException.class, queuedSnapshot::clear);
				assertEquals(0, model.saves);
				assertNull(model.orders);
				button(panel, "Undo").doClick();
				assertEquals(Arrays.asList(995, 2347, 13204), ids(capture.items(0)));
				assertEquals("Undo must not alter an already published snapshot", expected, ids(queuedSnapshot));
				click(panel, "995");
				click(panel, "13204");
				button(panel, "Cancel").doClick();
				assertEquals(Arrays.asList(995, 2347, 13204), ids(capture.items(0)));
				assertEquals(expected, ids(queuedSnapshot));
				assertEquals(0, model.saves);
				button(panel, "Edit this tab").doClick();
				click(panel, "995");
				click(panel, "13204");
				button(panel, "Save").doClick();
				assertEquals(1, model.saves);
				assertEquals(expected, model.ids);
				assertEquals(0, ((javax.swing.JTabbedPane) panel.getComponent(0)).getSelectedIndex());
				assertSame(BankPresets.IRONMAN, model.preset());
				assertEquals("profile", model.context);
			}
		});
	}

	@Test public void bankPreviewStopsForNewAnalysisChangedProfileAndPendingSaveUntilDraftCancelled() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			for (int reason = 0; reason < 3; reason++)
			{
				Model model = new Model();
				BlueprintEditorPanel panel = panel(model);
				BankOrganizationPreview original = new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
					new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0),
						Arrays.asList(item(995), item(2347), item(13204))),
					new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), Arrays.asList(item(4151)))));
				panel.setPreview(original);
				PreviewCapture capture = new PreviewCapture();
				panel.configureBankPreview(capture);
				button(panel, "Edit this tab").doClick();
				click(panel, "995");
				click(panel, "13204");
				assertNotNull(capture.snapshot);
				BankOrganizationPreview queuedDraft = capture.snapshot;
				if (reason == 0) panel.setPreview(new BankOrganizationPreview(BankPresets.IRONMAN, original.getCategories()));
				else if (reason == 1)
				{
					model.context = "different profile";
					((javax.swing.JTabbedPane) panel.getComponent(0)).setSelectedIndex(1);
				}
				else
				{
					model.deferSave = true;
					button(panel, "Save").doClick();
					assertFalse(button(panel, "Cancel").isEnabled());
				}
				assertNull("Stale/busy preview must be withdrawn for case " + reason, capture.snapshot);
				assertEquals(Arrays.asList(13204, 2347, 995), ids(queuedDraft.getCategories().get(0).getItems()));
				assertEquals(Arrays.asList(4151), ids(queuedDraft.getCategories().get(1).getItems()));
				if (reason == 2)
				{
					model.pending.accept(false);
					assertNull("A rejected save cannot revive its stale draft", capture.snapshot);
				}
				button(panel, "Cancel").doClick();
				assertNotNull(capture.snapshot);
				assertEquals(2, capture.snapshot.getCategories().size());
				assertEquals(Arrays.asList(995, 2347, 13204), ids(capture.items(0)));
				assertEquals(Arrays.asList(4151), ids(capture.items(1)));
				assertEquals(reason == 2 ? 1 : 0, model.saves);
				assertSame(BankPresets.IRONMAN, model.preset());
			}
		});
	}

	@Test public void allTabDraftSnapshotsPreserveTransfersAndDoNotChangeTheSelectedEditTab() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			Model model = new Model();
			BlueprintEditorPanel panel = panel(model);
			panel.setPreview(new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0),
					Arrays.asList(item(995).withLayoutTag("currency"), BankPreviewItem.blank(), item(13204).withLayoutTag("currency"))),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1),
					Arrays.asList(item(4151).withLayoutTag("gear"), BankPreviewItem.blank())),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(2), java.util.Collections.emptyList()))));
			PreviewCapture capture = new PreviewCapture();
			panel.configureBankPreview(capture);
			javax.swing.JTabbedPane tabs = (javax.swing.JTabbedPane) panel.getComponent(0);
			button(panel, "Edit this tab").doClick();
			panel.publishBankPreview();
			assertEquals(0, tabs.getSelectedIndex());
			assertEquals(3, capture.snapshot.getCategories().size());
			assertTrue("The empty category keeps its logical index", capture.items(2).isEmpty());
			click(panel, "995");
			click(panel, "13204");
			assertEquals("Reading all categories cannot move draft edits onto another tab",
				Arrays.asList(13204, -1, 995), ids(capture.items(0)));
			assertEquals(Arrays.asList(4151, -1), ids(capture.items(1)));
			BankOrganizationPreview beforeTransfer = capture.snapshot;
			layout(panel);
			JLabel source = (JLabel) find(panel, "995");
			java.awt.Rectangle targetHeader = tabs.getBoundsAt(1);
			dragTo(source, SwingUtilities.convertPoint(tabs, new Point(targetHeader.x + 5, targetHeader.y + 5), source));
			assertEquals(1, tabs.getSelectedIndex());
			assertEquals(Arrays.asList(13204, -1), ids(capture.items(0)));
			assertEquals(Arrays.asList(4151, -1, 995), ids(capture.items(1)));
			BankOrganizationPreview transferred = capture.snapshot;
			assertThrows(UnsupportedOperationException.class, () -> transferred.getCategories().clear());
			assertThrows(UnsupportedOperationException.class, () -> transferred.getCategories().get(1).getItems().clear());
			panel.publishBankPreview();
			assertEquals(1, tabs.getSelectedIndex());
			clickCell((JLabel) find((Container) tabs.getSelectedComponent(), "4151"));
			clickCell((JLabel) find((Container) tabs.getSelectedComponent(), "995"));
			assertEquals(Arrays.asList(995, -1, 4151), ids(capture.items(1)));
			assertEquals(Arrays.asList(13204, -1), ids(capture.items(0)));
			button(panel, "Undo").doClick();
			assertEquals(Arrays.asList(4151, -1, 995), ids(capture.items(1)));
			button(panel, "Undo").doClick();
			assertEquals(Arrays.asList(13204, -1, 995), ids(capture.items(0)));
			assertEquals(Arrays.asList(4151, -1), ids(capture.items(1)));
			button(panel, "Cancel").doClick();
			assertEquals(Arrays.asList(995, -1, 13204), ids(capture.items(0)));
			assertEquals(Arrays.asList(4151, -1), ids(capture.items(1)));
			assertEquals(Arrays.asList(13204, -1, 995), ids(beforeTransfer.getCategories().get(0).getItems()));
			assertEquals(Arrays.asList(4151, -1), ids(beforeTransfer.getCategories().get(1).getItems()));
			assertEquals(Arrays.asList(13204, -1), ids(transferred.getCategories().get(0).getItems()));
			assertEquals(Arrays.asList(4151, -1, 995), ids(transferred.getCategories().get(1).getItems()));
			assertEquals(0, model.saves);
			assertNull(model.orders);
			assertEquals("profile", model.context);
			assertSame(BankPresets.IRONMAN, model.preset());
		});
	}

	private static List<Integer> ids(List<BankPreviewItem> items)
	{
		return items.stream().map(BankPreviewItem::getItemId).collect(java.util.stream.Collectors.toList());
	}
	private static final class PreviewCapture implements Consumer<BankOrganizationPreview>
	{
		BankOrganizationPreview snapshot;
		List<BankPreviewItem> items(int tab) { return snapshot.getCategories().get(tab).getItems(); }
		@Override public void accept(BankOrganizationPreview snapshot) { this.snapshot = snapshot; }
	}

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

	@Test public void selectingSwappingInsertingAndUndoKeepEachTabsScrollPosition() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			BlueprintEditorPanel panel = panel(new Model());
			java.util.List<BankPreviewItem> main = new java.util.ArrayList<>();
			java.util.List<BankPreviewItem> gear = new java.util.ArrayList<>();
			for (int id = 1; id <= 400; id++)
			{
				main.add(item(id));
				gear.add(item(id + 400));
			}
			panel.setPreview(new BankOrganizationPreview(BankPresets.IRONMAN, Arrays.asList(
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(0), main),
				new BankCategoryPreview(BankPresets.IRONMAN.getCategories().get(1), gear))));
			panel.beginEdit();
			layout(panel);
			javax.swing.JTabbedPane tabs = (javax.swing.JTabbedPane) panel.getComponent(0);
			Point mainPosition = new Point(0, 1400);
			((javax.swing.JScrollPane) tabs.getComponentAt(0)).getViewport().setViewPosition(mainPosition);
			for (String movement : new String[]{"Swap", "Insert"})
			{
				mode(panel).setSelectedItem(movement);
				click(panel, "300");
				layout(panel);
				assertEquals("Selection in " + movement, mainPosition,
					((javax.swing.JScrollPane) tabs.getComponentAt(0)).getViewport().getViewPosition());
				JLabel selected = (JLabel) find(panel, "300");
				assertEquals(java.awt.Color.GREEN, ((javax.swing.border.LineBorder) selected.getBorder()).getLineColor());
				click(panel, "301");
				layout(panel);
				assertEquals(movement, mainPosition,
					((javax.swing.JScrollPane) tabs.getComponentAt(0)).getViewport().getViewPosition());
				Component moved = find(panel, "300");
				assertEquals("Clicked destination in " + movement, 300, moved.getParent().getComponentZOrder(moved));
				button(panel, "Undo").doClick();
				layout(panel);
				assertEquals("Undo in " + movement, mainPosition,
					((javax.swing.JScrollPane) tabs.getComponentAt(0)).getViewport().getViewPosition());
				Component restored = find(panel, "300");
				assertEquals("Restored original slot", 299, restored.getParent().getComponentZOrder(restored));
			}
			tabs.setSelectedIndex(1);
			layout(panel);
			Point gearPosition = new Point(0, 1000);
			((javax.swing.JScrollPane) tabs.getComponentAt(1)).getViewport().setViewPosition(gearPosition);
			click(panel, "650");
			layout(panel);
			assertEquals(gearPosition,
				((javax.swing.JScrollPane) tabs.getComponentAt(1)).getViewport().getViewPosition());
			tabs.setSelectedIndex(0);
			layout(panel);
			assertEquals("Main position after switching tabs", mainPosition,
				((javax.swing.JScrollPane) tabs.getComponentAt(0)).getViewport().getViewPosition());
			tabs.setSelectedIndex(1);
			layout(panel);
			assertEquals("Gear position after switching tabs", gearPosition,
				((javax.swing.JScrollPane) tabs.getComponentAt(1)).getViewport().getViewPosition());
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
		clickCell((JLabel) find(panel, id));
	}
	private static void clickCell(JLabel cell)
	{
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
				for (String name : new String[]{"Edit this tab", "Undo", "Cancel", "Save", "Reset tab order", "Assign category...", "Save current bank...", "Review changes"})
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
	private static final class Model extends TestBankLayoutModel
	{
		int saves;
		int remembered;
		int accepted;
		int captures;
		String reference = "";
		String acceptedValue = "";
		BankOrganizationPreview proposal;
		BankOrganizationPreview acceptedExpected, acceptedProposal;
		String acceptedContext;
		List<Integer> ids;
		String context = "profile";
		boolean deferSave;
		Consumer<Boolean> pending;
		public String rememberedBlueprint() { return reference; }
		public String acceptedBlueprint() { return acceptedValue; }
		public BankOrganizationPreview proposedBlueprint() { return proposal; }
		public String blueprintState() { return AcceptedBlueprint.description(acceptedValue, context); }
		public void acceptBlueprint(BankOrganizationPreview expected, BankOrganizationPreview proposed,
			String expectedContext, Consumer<Boolean> completed)
		{
			accepted++;
			acceptedExpected = expected;
			acceptedProposal = proposed;
			acceptedContext = expectedContext;
			Consumer<Boolean> result = success -> {
				if (success) acceptedValue = AcceptedBlueprint.capture(proposed, expectedContext);
				completed.accept(success);
			};
			if (deferSave) pending = result; else result.accept(true);
		}
		public void captureCurrentBank(String name, BankOrganizationPreview expected,
			String expectedContext, Consumer<Boolean> completed)
		{
			captures++;
			completed.accept(false);
		}
		public void rememberBlueprint(BankOrganizationPreview expected, String context, Consumer<Boolean> completed)
		{
			remembered++;
			reference = BlueprintChanges.capture(expected);
			if (deferSave) pending = completed; else completed.accept(true);
		}
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

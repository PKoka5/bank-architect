package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankTabOrder;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class IronmanBankArchitectPanelTest
{
	@Test
	public void analyzeButtonRunsCallback()
	{
		AtomicInteger calls = new AtomicInteger();
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()),
			calls::incrementAndGet
		);

		panel.getAnalyzeButton().doClick();

		assertEquals(1, calls.get());
		panel.shutdown();
	}

	@Test
	public void panelRendersCompactBankScanOverviewFromCatalogSummary()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		BankCatalogSummary summary = BankCatalogSummarizer.summarize(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		)), StaticItemCatalog.INSTANCE);
		controller.publishCatalogSummary(summary);
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		)), StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		String labelText = panel.getCatalogSummaryLabel().getText();
		assertTrue(labelText.contains("Bank Scan Overview"));
		assertTrue(labelText.contains("Recognized item IDs: 2"));
		assertTrue(labelText.contains("Unrecognized IDs: 1"));
		assertFalse(labelText.contains("Categories:"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("Blueprint ready"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("3 item IDs sorted"));

		panel.shutdown();
	}

	@Test
	public void panelFramesWholeBankOrganizationAsPrimaryFlow()
	{
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()),
			() -> {});

		String panelText = String.join("\n", collectLabelText(panel));

		assertTrue(panelText.contains(AllRoundIronmanPreset.PROFILE_NAME));
		assertTrue(panelText.contains("Analyze Bank"));
		assertTrue(panelText.contains("Analyze your bank, then open the blueprint."));
		assertTrue(panelText.contains("Guided mode highlights one manual bank action"));
		assertTrue(panelText.contains("in Swap or Insert mode"));
		assertTrue(panelText.contains("Guides tab creation and item order"));
		assertTrue(panelText.contains("Every move stays manual"));
		assertTrue(panel.getGuideProgressLabel().getText().contains("Analyze your bank"));
		assertFalse(panelText.contains("Advanced preview block"));
		assertFalse(panelText.contains("Owned:"));
		assertFalse(panelText.contains("Missing:"));

		panel.shutdown();
	}

	@Test
	public void restingPanelShowsTheDestinationGridAndKeepsProseFolded()
	{
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()),
			() -> {});

		// One cell per blueprint destination, and the prose starts out of sight.
		assertEquals(10, panel.getDestinationCells().size());
		assertFalse("details must start folded", panel.getDetailsPanel().isVisible());
		assertTrue("the summary lives inside the folded details",
			isInside(panel.getDetailsPanel(), panel.getCatalogSummaryLabel()));
		assertTrue(isInside(panel.getDetailsPanel(), panel.getOrganizationPreviewLabel()));
		assertTrue(isInside(panel.getDetailsPanel(), panel.getResetOverridesButton()));

		// Without a scan every cell is empty, so no count is painted.
		for (IronmanBankArchitectPanel.DestinationCell cell : panel.getDestinationCells())
		{
			assertEquals("", cell.getCount());
		}

		panel.shutdown();
	}

	@Test
	public void aScanFillsEveryDestinationCellThatHasItems()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(snapshot,
			StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		// Herblore holds the two seeds, cleanup review holds the unknown item.
		assertEquals("2", panel.getDestinationCells().get(3).getCount());
		assertEquals("1", panel.getDestinationCells().get(9).getCount());
		assertEquals("", panel.getDestinationCells().get(1).getCount());

		panel.shutdown();
	}

	@Test
	public void theNextMoveLineKeepsOneSentenceAndParksTheRestInTheTooltip()
	{
		assertEquals("", IronmanBankArchitectPanel.firstSentence(""));
		assertEquals("Drag Mind helmet to tab 2.", IronmanBankArchitectPanel.firstSentence(
			"Distributing: 99%\nDrag Mind helmet to tab 2. Main is processed top to bottom."));
		assertEquals("Blueprint complete.",
			IronmanBankArchitectPanel.firstSentence("Sorting: 100%\nBlueprint complete."));
	}

	@Test
	public void categoryCorrectionCardTracksAssignModeAndRecordedCount()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		boolean[] reset = {false};
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {},
			(item, label) -> {}, () -> reset[0] = true);

		// The switch carries its state in the tooltip, not in a flipping label.
		assertEquals("Assign categories", panel.getAssignCategoriesButton().getToolTipText());
		assertTrue(panel.getCategoryOverrideLabel().getText().contains("No category corrections"));
		assertFalse(panel.getResetOverridesButton().isEnabled());

		panel.getAssignCategoriesButton().doClick();
		assertTrue(controller.isCategoryAssignMode());
		assertTrue(panel.getAssignCategoriesButton().getToolTipText().contains("right-click"));
		assertTrue(panel.getCategoryOverrideLabel().getText().contains("Right-click a bank item"));

		controller.publishCategoryOverrideCount(2);
		panel.getAssignCategoriesButton().doClick();
		assertFalse(controller.isCategoryAssignMode());
		assertTrue(panel.getCategoryOverrideLabel().getText().contains("2 items are corrected"));
		assertTrue(panel.getResetOverridesButton().isEnabled());

		panel.getResetOverridesButton().doClick();
		assertTrue(reset[0]);
		panel.shutdown();
	}

	@Test
	public void selectedBlockMatchDetailsDoNotRenderInMainOrganizerSidebar()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		controller.selectBlock("irit-super-attack");
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));
		controller.publishCatalogSummary(BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE));
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(snapshot, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getAnalyzeButton().doClick();

		String panelText = String.join("\n", collectLabelText(panel));
		assertTrue(panel.getCatalogSummaryLabel().getText().contains("Bank Scan Overview"));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains("Blueprint ready"));
		assertFalse(panel.getOrganizationPreviewLabel().getText().contains("Irit seed"));
		assertFalse(panelText.contains("Owned:"));
		assertFalse(panelText.contains("Missing:"));

		panel.shutdown();
	}

	@Test
	public void guideProgressBarOnlyShowsWithAMeaningfulPercent()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(new BankItemSnapshot(5297, 1, 0)));
		controller.publishOrganizationPreview(BankOrganizationPreviewBuilder.build(snapshot,
			StaticItemCatalog.INSTANCE, BankPresets.IRONMAN));
		controller.setBankOpen(true);
		controller.toggleGuide();
		controller.publishGuideProgress("Sorting", 42);

		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});

		assertTrue(panel.getGuideProgressBar().isVisible());
		assertEquals(42, panel.getGuideProgressBar().getPercentage());
		panel.shutdown();
	}

	@Test
	public void guideProgressBarStaysHiddenWithoutGuidance()
	{
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()), () -> {});

		assertFalse(panel.getGuideProgressBar().isVisible());
		panel.shutdown();
	}

	@Test
	public void sidebarCardsAndButtonsStayInsideTheVisibleViewport()
	{
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()), () -> {});
		panel.setSize(225, 400);
		layoutTree(panel);

		JScrollPane scrollPane = findScrollPane(panel);
		int viewportWidth = scrollPane.getViewport().getWidth();
		assertTrue("content preferred width "
			+ scrollPane.getViewport().getView().getPreferredSize().width
			+ " exceeds viewport " + viewportWidth,
			scrollPane.getViewport().getView().getPreferredSize().width <= viewportWidth);

		Rectangle analyzeBounds = SwingUtilities.convertRectangle(
			panel.getAnalyzeButton().getParent(), panel.getAnalyzeButton().getBounds(),
			scrollPane.getViewport());
		Rectangle showBankBounds = SwingUtilities.convertRectangle(
			panel.getShowBankButton().getParent(), panel.getShowBankButton().getBounds(),
			scrollPane.getViewport());
		assertTrue(analyzeBounds.x >= 0
			&& analyzeBounds.x + analyzeBounds.width <= viewportWidth);
		assertTrue(showBankBounds.x >= 0
			&& showBankBounds.x + showBankBounds.width <= viewportWidth);
		panel.shutdown();
	}

	@Test
	public void panelSourceHasNoRuneLiteClientApiImports() throws Exception
	{
		String source = new String(Files.readAllBytes(Paths.get("src/main/java/com/pkoka5/ironmanbankarchitect/IronmanBankArchitectPanel.java")), StandardCharsets.UTF_8);

		assertFalse(source.contains("net.runelite.api.Client"));
		assertFalse(source.contains("net.runelite.api.widgets.Widget"));
		assertFalse(source.contains("InventoryID"));
		assertFalse(source.contains("ItemContainer"));
		assertFalse(source.contains("ItemManager"));
		assertFalse(source.contains("net.runelite.api."));
	}

	private static boolean isInside(Container container, Component target)
	{
		for (Component component : container.getComponents())
		{
			if (component == target
				|| component instanceof Container && isInside((Container) component, target))
			{
				return true;
			}
		}
		return false;
	}

	private static List<String> collectLabelText(Container container)
	{
		List<String> texts = new ArrayList<>();
		for (Component component : container.getComponents())
		{
			if (component instanceof JLabel)
			{
				texts.add(((JLabel) component).getText());
			}
			if (component instanceof AbstractButton)
			{
				texts.add(((AbstractButton) component).getText());
			}
			if (component instanceof Container)
			{
				texts.addAll(collectLabelText((Container) component));
			}
		}

		return texts;
	}

	private static void layoutTree(Component component)
	{
		component.doLayout();
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				layoutTree(child);
			}
		}
	}

	@Test
	public void tabOrderButtonSavesAnArrangedOrder()
	{
		List<String> saved = new ArrayList<>();
		TabOrderModel model = new TabOrderModel()
		{
			@Override
			public List<com.pkoka5.ironmanbankarchitect.organize.BankCategory> categories()
			{
				return BankTabOrder.apply(BankPresets.IRONMAN,
					BankTabOrder.serialize(saved)).getCategories();
			}

			@Override
			public void save(List<String> keys)
			{
				saved.clear();
				saved.addAll(keys);
			}
		};
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()), () -> {}, (item, label) -> {},
			() -> {}, model);

		assertTrue(panel.getTabOrderButton().isEnabled());
		// The dialog itself needs a display, so exercise what its buttons call.
		model.save(BankTabOrder.moved(BankTabOrder.orderedKeys(BankPresets.IRONMAN, ""), 9, -1));

		List<String> stored = BankTabOrder.orderedKeys(BankPresets.IRONMAN,
			BankTabOrder.serialize(saved));
		assertEquals("storage-cleanup", stored.get(8));
		assertEquals("currency-utilities", stored.get(0));
		panel.shutdown();
	}

	private static JScrollPane findScrollPane(Container container)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JScrollPane)
			{
				return (JScrollPane) component;
			}
			if (component instanceof Container)
			{
				JScrollPane nested = findScrollPane((Container) component);
				if (nested != null)
				{
					return nested;
				}
			}
		}
		return null;
	}
}

package com.pkoka5.ironmanbankarchitect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankTags;
import com.pkoka5.ironmanbankarchitect.organize.CategoryOverrideSource;
import com.pkoka5.ironmanbankarchitect.organize.GearStatsSource;
import com.pkoka5.ironmanbankarchitect.organize.ItemValueSource;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));
		controller.publishBankAnalysis(success(snapshot));

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
		controller.publishBankAnalysis(success(snapshot));

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
		controller.publishBankAnalysis(success(snapshot));

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
		controller.publishBankAnalysis(success(snapshot));
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
	public void failedAnalysisReplacesBothOutputsWithTheGenericFailure()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		controller.publishBankAnalysis(BankAnalysisStatus.failed());
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});

		assertTrue(panel.getCatalogSummaryLabel().getText().contains(BankAnalysisStatus.FAILED_TEXT));
		assertTrue(panel.getOrganizationPreviewLabel().getText().contains(BankAnalysisStatus.FAILED_TEXT));
		assertFalse(panel.getShowBankButton().isEnabled());

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

	private static BankAnalysisStatus success(BankSnapshot snapshot)
	{
		return BankAnalysisStatus.success(
			BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE),
			BankOrganizationPreviewBuilder.build(snapshot, StaticItemCatalog.INSTANCE,
				BankPresets.IRONMAN));
	}

	private static BankAnalysisStatus successWithCounts(BankSnapshot snapshot)
	{
		return BankAnalysisStatus.success(
			BankCatalogSummarizer.summarize(snapshot, StaticItemCatalog.INSTANCE),
			BankOrganizationPreviewBuilder.build(snapshot, StaticItemCatalog.INSTANCE,
				BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
				CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(BankPresets.IRONMAN),
				BankLayoutOptions.DEFAULTS));
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

	private static String layoutTagText(IronmanBankArchitectPanel panel, String tagName)
	{
		for (String text : collectLabelText(panel.getLayoutRows()))
		{
			if (text.contains(tagName))
			{
				return text;
			}
		}
		throw new AssertionError("missing layout row for " + tagName);
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

	/** A model backed by a plan in memory, standing in for the config. */
	private static final class RecordingLayoutModel implements BankLayoutModel
	{
		private BankLayoutPlan stored = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		private int saves;

		@Override
		public com.pkoka5.ironmanbankarchitect.organize.BankPreset preset()
		{
			return BankPresets.IRONMAN;
		}

		@Override
		public BankLayoutPlan plan()
		{
			return stored;
		}

		@Override
		public void save(BankLayoutPlan plan)
		{
			stored = plan;
			saves++;
		}
	}

	private static IronmanBankArchitectPanel panelWith(BankLayoutModel model)
	{
		return new IronmanBankArchitectPanel(
			new BankGuideController(AllRoundIronmanPreset.create()), () -> {}, (item, label) -> {},
			() -> {}, model);
	}

	@Test
	public void layoutEditorStaysClosedUntilTheTabLayoutButtonIsPressed()
	{
		IronmanBankArchitectPanel panel = panelWith(new RecordingLayoutModel());

		assertTrue(panel.getTabOrderButton().isEnabled());
		assertFalse(panel.isLayoutEditorVisible());

		panel.getTabOrderButton().doClick();

		assertTrue(panel.isLayoutEditorVisible());
		panel.shutdown();
	}

	@Test
	public void openLayoutEditorRepopulatesCountsWhenAnalysisSucceeds()
	{
		BankGuideController controller = new BankGuideController(AllRoundIronmanPreset.create());
		BankSnapshot snapshot = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 1, 1),
			new BankItemSnapshot(999999, 1, 2)
		));
		BankAnalysisStatus successfulAnalysis = successWithCounts(snapshot);
		BankOrganizationPreview preview = successfulAnalysis.organizationPreview().get();
		Map.Entry<String, Integer> populatedTag = preview.getTagCounts().entrySet().stream()
			.filter(entry -> entry.getValue() > 0)
			.findFirst()
			.orElseThrow(AssertionError::new);
		String tagName = BankTags.byKey(populatedTag.getKey()).getName();
		controller.publishBankAnalysis(successfulAnalysis);
		IronmanBankArchitectPanel panel = new IronmanBankArchitectPanel(controller, () -> {});
		panel.getTabOrderButton().doClick();

		assertTrue(layoutTagText(panel, tagName).contains(">" + populatedTag.getValue() + " items<"));

		controller.publishBankAnalysis(BankAnalysisStatus.running());
		panel.getAnalyzeButton().doClick();
		assertTrue(layoutTagText(panel, tagName).contains(">0 items<"));

		controller.publishBankAnalysis(successfulAnalysis);
		panel.getAnalyzeButton().doClick();
		assertTrue(layoutTagText(panel, tagName).contains(">" + populatedTag.getValue() + " items<"));

		panel.shutdown();
	}

	/**
	 * The reported case: runes start in the main section and the player wants
	 * them on the combat gear tab, without teleports coming along.
	 */
	@Test
	public void oneTagLeavesItsBundleForAnotherTab()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();
		int gearTab = model.plan().destinationOf("gear");

		panel.moveTagTo("runes", gearTab);

		assertEquals(gearTab, model.plan().destinationOf("runes"));
		assertFalse(model.plan().getTagKeys(gearTab).contains("teleports"));
		assertEquals(1, model.saves);
		panel.shutdown();
	}

	@Test
	public void movingTheLastTagOffATabLeavesThatTabEmpty()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		for (String key : new ArrayList<>(
			model.plan().getTagKeys(BankLayoutPlan.MAIN_DESTINATION_INDEX)))
		{
			panel.moveTagTo(key, 4);
		}

		assertTrue(model.plan().getTagKeys(BankLayoutPlan.MAIN_DESTINATION_INDEX).isEmpty());
		panel.shutdown();
	}

	/** Taking a tag off a tab sends it to the fallback rather than nowhere. */
	@Test
	public void removingATagSendsItToTheFallbackTab()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		panel.removeFromTab("potions");

		assertEquals(model.plan().destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY),
			model.plan().destinationOf("potions"));
		panel.shutdown();
	}

	@Test
	public void theFallbackTagAlwaysKeepsATab()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();
		int before = model.plan().destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY);

		panel.removeFromTab(BankLayoutPlan.FALLBACK_TAG_KEY);

		assertEquals(before, model.plan().destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY));
		assertEquals(0, model.saves);
		panel.shutdown();
	}

	/** Every tag keeps a home, so no items drop out of the blueprint. */
	@Test
	public void everyTagStillHasADestinationAfterAMove()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		panel.moveTagTo("gems", 2);
		panel.moveTagTo("food", 2);

		for (com.pkoka5.ironmanbankarchitect.organize.BankTag tag
			: com.pkoka5.ironmanbankarchitect.organize.BankTags.all())
		{
			assertTrue(tag.getKey(), model.plan().destinationOf(tag.getKey()) >= 0);
		}
		panel.shutdown();
	}

	@Test
	public void everyTagGetsARowSoNoneIsUnreachable()
	{
		IronmanBankArchitectPanel panel = panelWith(new RecordingLayoutModel());
		panel.getTabOrderButton().doClick();

		assertEquals(com.pkoka5.ironmanbankarchitect.organize.BankTags.all().size(),
			countRows(panel.getLayoutRows()));
		panel.shutdown();
	}
	/**
	 * The move instructions call the first real tab "blueprint tab 1", so the
	 * layout screen has to as well. Numbering the main section as tab one would
	 * have the two halves of the plugin name the same tab differently.
	 */
	@Test
	public void tabsAreNumberedFromTheFirstRealTabNotFromTheMainSection()
	{
		IronmanBankArchitectPanel panel = panelWith(new RecordingLayoutModel());
		panel.getTabOrderButton().doClick();

		panel.selectDestination(BankLayoutPlan.MAIN_DESTINATION_INDEX);
		assertTrue(headingOf(panel).contains("Main section"));

		panel.selectDestination(3);
		String heading = headingOf(panel);
		assertTrue(heading, heading.contains("Tab 3"));
		assertFalse(heading, heading.contains("Tab 4"));
		panel.shutdown();
	}

	/** The heading names what the destination holds, not just its number. */
	@Test
	public void theHeadingSpellsOutWhichCategoriesAreOnTheShownTab()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();
		int storage = model.plan().destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY);

		panel.moveTagTo("cosmetics", storage);
		panel.selectDestination(storage);

		String heading = headingOf(panel);
		assertTrue(heading, heading.contains(
			com.pkoka5.ironmanbankarchitect.organize.BankTags.byKey("cosmetics").getName()));
		assertTrue(heading, heading.contains(
			com.pkoka5.ironmanbankarchitect.organize.BankTags.byKey(
				BankLayoutPlan.FALLBACK_TAG_KEY).getName()));
		panel.shutdown();
	}

	/** The list is organised tab first: a heading per destination, then its categories. */
	@Test
	public void theListIsGroupedByTabWithAHeadingForEveryDestination()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		List<String> headings = new ArrayList<>();
		for (Component child : panel.getLayoutRows().getComponents())
		{
			if (child instanceof JLabel)
			{
				headings.add(((JLabel) child).getText());
			}
		}

		assertTrue(headings.toString(), headings.get(0).startsWith("Main section"));
		for (int tab = 1; tab < BankLayoutPlan.DESTINATION_COUNT; tab++)
		{
			final String expected = "Tab " + tab;
			boolean found = false;
			for (String heading : headings)
			{
				found = found || heading.startsWith(expected);
			}
			assertTrue(expected + " missing from " + headings, found);
		}
		panel.shutdown();
	}

	/** An emptied tab keeps its heading, so the player can see it is empty on purpose. */
	@Test
	public void anEmptiedTabIsStillListedAsEmpty()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		for (String key : new ArrayList<>(
			model.plan().getTagKeys(BankLayoutPlan.MAIN_DESTINATION_INDEX)))
		{
			panel.moveTagTo(key, 4);
		}

		boolean sawEmptyNote = false;
		for (Component child : panel.getLayoutRows().getComponents())
		{
			sawEmptyNote = sawEmptyNote || (child instanceof JLabel
				&& ((JLabel) child).getText().startsWith("Empty"));
		}
		assertTrue(sawEmptyNote);
		panel.shutdown();
	}

	/**
	 * A vertical BoxLayout centres anything narrower than the widest child, so a
	 * single row that asks to be wide staggers the whole column to the right and
	 * pushes the buttons out of view. Every child must share one left edge.
	 */
	@Test
	public void everyRowInTheEditorSharesOneLeftEdgeAndOneWidth()
	{
		IronmanBankArchitectPanel panel = panelWith(new RecordingLayoutModel());
		panel.getTabOrderButton().doClick();

		int width = -1;
		for (Component child : panel.getLayoutRows().getComponents())
		{
			if (child instanceof Box.Filler)
			{
				continue;
			}
			assertEquals(child.getClass().getName(),
				Component.LEFT_ALIGNMENT, ((JComponent) child).getAlignmentX(), 0.001);
			int preferred = child.getPreferredSize().width;
			if (width < 0)
			{
				width = preferred;
			}
			assertEquals(child.getClass().getName(), width, preferred);
		}
		panel.shutdown();
	}

	/** Every tag needs a way off its tab, and the fallback needs it refused. */
	@Test
	public void everyTagRowCarriesARemoveButton()
	{
		RecordingLayoutModel model = new RecordingLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		int rows = 0;
		int enabledRemovals = 0;
		for (Component child : panel.getLayoutRows().getComponents())
		{
			if (!(child instanceof JPanel))
			{
				continue;
			}
			rows++;
			List<AbstractButton> buttons = new ArrayList<>();
			collectButtons((Container) child, buttons);
			assertEquals("row " + rows + " is missing controls", 3, buttons.size());
			if (buttons.get(2).isEnabled())
			{
				enabledRemovals++;
			}
		}

		assertEquals(com.pkoka5.ironmanbankarchitect.organize.BankTags.all().size(), rows);
		// Every tag but the fallback can be taken off its tab.
		assertEquals(rows - 1, enabledRemovals);
		panel.shutdown();
	}

	/**
	 * An edited layout is no longer the layout it was loaded from, so the list
	 * must say so rather than leave a saved profile looking selected.
	 */
	@Test
	public void editingALayoutLeavesTheProfileListShowingUnsaved()
	{
		ProfileLayoutModel model = new ProfileLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		assertEquals(BankLayoutProfiles.DEFAULT_NAME,
			panel.getProfileChooser().getSelectedItem());

		panel.moveTagTo("runes", 6);

		assertEquals("Custom (unsaved)", panel.getProfileChooser().getSelectedItem());
		panel.shutdown();
	}

	@Test
	public void savedLayoutsAreOfferedAndTheBundledOneComesFirst()
	{
		ProfileLayoutModel model = new ProfileLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		panel.moveTagTo("runes", 6);
		model.saveProfile("Maugor setup", panel.getLayoutPlan());
		panel.getTabOrderButton().doClick();
		panel.getTabOrderButton().doClick();

		assertEquals(BankLayoutProfiles.DEFAULT_NAME, panel.getProfileChooser().getItemAt(0));
		assertEquals("Maugor setup", panel.getProfileChooser().getSelectedItem());
		panel.shutdown();
	}

	/**
	 * The two layout options live beside the tabs they affect. They were only in
	 * the client's plugin settings at first, where nobody arranging tabs thinks
	 * to look.
	 */
	@Test
	public void layoutOptionsSitInTheEditorAndSaveWhenTicked()
	{
		ProfileLayoutModel model = new ProfileLayoutModel();
		IronmanBankArchitectPanel panel = panelWith(model);
		panel.getTabOrderButton().doClick();

		assertTrue(panel.getAlchPileBox().isSelected());

		panel.getAlchPileBox().doClick();

		assertFalse(model.options().alchPile());
		panel.shutdown();
	}

	@Test
	public void reopeningTheEditorShowsTheStoredOptions()
	{
		ProfileLayoutModel model = new ProfileLayoutModel();
		model.saveOptions(new BankLayoutOptions(false, false, false));
		IronmanBankArchitectPanel panel = panelWith(model);

		panel.getTabOrderButton().doClick();

		assertFalse(panel.getAlchPileBox().isSelected());
		panel.shutdown();
	}

	/** A model that keeps its profiles in memory, standing in for the config. */
	private static final class ProfileLayoutModel implements BankLayoutModel
	{
		private BankLayoutPlan working = BankLayoutPlan.defaultFor(BankPresets.IRONMAN);
		private BankLayoutProfiles profiles = BankLayoutProfiles.parse("", "");
		private BankLayoutOptions layoutOptions = BankLayoutOptions.DEFAULTS;

		@Override
		public BankLayoutOptions options()
		{
			return layoutOptions;
		}

		@Override
		public void saveOptions(BankLayoutOptions options)
		{
			layoutOptions = options;
		}

		@Override
		public com.pkoka5.ironmanbankarchitect.organize.BankPreset preset()
		{
			return BankPresets.IRONMAN;
		}

		@Override
		public BankLayoutPlan plan()
		{
			return working;
		}

		@Override
		public void save(BankLayoutPlan plan)
		{
			working = plan;
		}

		@Override
		public List<String> profileNames()
		{
			return profiles.names();
		}

		@Override
		public String matchingProfile()
		{
			for (String name : profiles.names())
			{
				if (BankLayoutPlan.parse(BankPresets.IRONMAN, profiles.planFor(name))
					.getDestinations().equals(working.getDestinations()))
				{
					return name;
				}
			}

			return "";
		}

		@Override
		public void selectProfile(String name)
		{
			profiles = profiles.withActive(name);
			working = BankLayoutPlan.parse(BankPresets.IRONMAN, profiles.activePlan());
		}

		@Override
		public void saveProfile(String name, BankLayoutPlan plan)
		{
			profiles = profiles.withProfile(name, plan.serialize());
			working = plan;
		}

		@Override
		public void deleteProfile(String name)
		{
			profiles = profiles.without(name);
		}
	}

	private static void collectButtons(Container container, List<AbstractButton> found)
	{
		for (Component child : container.getComponents())
		{
			if (child instanceof AbstractButton)
			{
				found.add((AbstractButton) child);
			}
			else if (child instanceof Container)
			{
				collectButtons((Container) child, found);
			}
		}
	}

	private static String headingOf(IronmanBankArchitectPanel panel)
	{
		return panel.getLayoutHeading().getText();
	}

	private static int countRows(JPanel rows)
	{
		int count = 0;
		for (Component child : rows.getComponents())
		{
			if (child instanceof JPanel)
			{
				count++;
			}
		}

		return count;
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

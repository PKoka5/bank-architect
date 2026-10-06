package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.Timer;
import net.runelite.client.ui.*;
import net.runelite.client.ui.components.ProgressBar;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Bank Architect";
	private static final String SUMMARY = "Read-only bank blueprint planner";
	private static final String SAFETY_NOTE = "No bank actions are automated.";
	private static final String DETAILS_LABEL = "Details";
	private static final String CATEGORY_CORRECTION_HELP =
		"Corrections are stored locally and win over the bundled classification. "
			+ "Analyze again to rebuild the blueprint.";
	private static final String PREVIEW_BLOCK_HELP =
		"Guided mode highlights one manual bank action at a time.";
	private static final String LAYOUT_EDITOR_HELP =
		"Each tab lists what is on it. Use its dropdown to add a category or tag, and "
			+ "the no-entry button to take one off. A tab left with nothing is simply not "
			+ "created, so the main section can stay empty for dumping loot. Keeping a "
			+ "bundle's tags on one tab keeps its layout; splitting them is allowed and "
			+ "each side is then arranged on its own. ";
	private static final String PREVIEW_OVERLAY_NOTE =
		"Guides tab creation and item order in the vanilla All items view, in Swap or Insert mode; "
			+ "Insert usually needs fewer drags. Every move stays manual.";
	private static final int STATUS_REFRESH_MILLIS = 100;
	private static final int DESTINATION_COLUMNS = 2;
	private static final int DESTINATION_GAP = 2;
	private static final int CELL_WIDTH = 36;
	private static final int CELL_HEIGHT = 32;
	private static final int DIALOG_WIDTH = 560;
	private static final int DIALOG_HEIGHT = 640;
	private static final Color BANK_PANEL = new Color(31, 31, 31);
	private static final Color SLOT_BORDER = new Color(78, 78, 78);
	private static final Color BLANK_SLOT_BORDER = new Color(52, 52, 52);
	private static final Color DESTINATION_CELL_BG = new Color(35, 35, 35);
	private static final Color DESTINATION_EMPTY_BG = new Color(26, 26, 26);
	// PluginPanel is ~225px wide; leave room for panel padding, card padding
	// and the scrollbar so wrapped text never clips at the right edge.
	private static final int SIDEBAR_TEXT_WIDTH = 180;
	// The layout editor sits inside the same 225px panel, so its rows are pinned
	// to one width; anything wider pushes the whole column off the right edge.
	private static final int SIDEBAR_CONTENT_WIDTH = 184;
	private static final int LAYOUT_NAME_WIDTH = 110;
	/** Shown in the profile list while the working layout matches no saved one. */

	private final BankGuideController guideController;
	private final BiConsumer<BankPreviewItem, JLabel> itemIconRenderer;
	private final JButton toggleButton;
	private final JButton analyzeButton;
	private final JButton showBankButton;
	private final JButton assignCategoriesButton;
	private final JButton resetOverridesButton;
	private final JButton tabOrderButton;
	private final JButton resetLayoutButton;
	private final JButton saveProfileButton;
	private final JButton importButton;
	private final JButton exportButton;
	private final JButton deleteProfileButton;
	private final JComboBox<String> presetChooser = new JComboBox<>(new String[] {"Ironman", "Main"});
	private boolean refreshingPreset;
	private final JCheckBox alchPileBox;
	private boolean refreshingOptions;
	private final JPanel layoutEditor;
	private final JPanel layoutRows;
	private final JLabel layoutHeading;
	private final JLabel layoutHelp;
	private final BankLayoutModel bankLayoutModel;
	private BankLayoutPlan layoutPlan;
	private int selectedDestination = BankLayoutPlan.MAIN_DESTINATION_INDEX;
	private final JLabel categoryOverrideLabel;
	private final JLabel statusLabel;
	private final JLabel catalogSummaryLabel;
	private final JLabel organizationPreviewLabel;
	private final JLabel guideProgressLabel;
	private final JLabel nextMoveLabel;
	private JLabel workflowSteps;
	private final JLabel detailsChevron;
	private final ProgressBar guideProgressBar;
	private final JPanel detailsPanel;
	private final List<DestinationCell> destinationCells = new ArrayList<>();
	private final Timer statusTimer;
	private BankOrganizationPreview renderedDestinations;
	private BankOrganizationPreview renderedLayoutPreview;
	private String arrangeOpenTagKey;
	private JDialog bankDialog;
	private BlueprintEditorPanel blueprintEditor;
	private JButton exportBlueprintButton;
	private ReleaseNoticePanel releaseNotice;

	void configureReleaseNotice(java.util.function.Supplier<String> lastSeen,
		java.util.function.Consumer<String> acknowledge)
	{
		JComponent normal = (JComponent) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
		remove(normal);
		releaseNotice = new ReleaseNoticePanel(normal, lastSeen, acknowledge);
		add(releaseNotice, BorderLayout.CENTER);
	}

	@Override
	public void onActivate()
	{
		if (releaseNotice != null) releaseNotice.opened();
	}

	IronmanBankArchitectPanel(BankGuideController guideController)
	{
		this(guideController, () -> {}, (item, label) -> {});
	}

	IronmanBankArchitectPanel(BankGuideController guideController, Runnable analyzeCallback)
	{
		this(guideController, analyzeCallback, (item, label) -> {});
	}

	IronmanBankArchitectPanel(BankGuideController guideController, Runnable analyzeCallback,
		BiConsumer<BankPreviewItem, JLabel> itemIconRenderer)
	{
		this(guideController, analyzeCallback, itemIconRenderer, () -> {});
	}

	IronmanBankArchitectPanel(BankGuideController guideController, Runnable analyzeCallback,
		BiConsumer<BankPreviewItem, JLabel> itemIconRenderer, Runnable resetOverridesCallback)
	{
		this(guideController, analyzeCallback, itemIconRenderer, resetOverridesCallback,
			BankLayoutModel.DEFAULT);
	}

	IronmanBankArchitectPanel(BankGuideController guideController, Runnable analyzeCallback,
		BiConsumer<BankPreviewItem, JLabel> itemIconRenderer, Runnable resetOverridesCallback,
		BankLayoutModel bankLayoutModel)
	{
		this.guideController = guideController;
		this.itemIconRenderer = itemIconRenderer;
		this.bankLayoutModel = bankLayoutModel;
		this.layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());

		setLayout(new BorderLayout(0, 8));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		analyzeButton = new JButton("Analyze Bank");
		analyzeButton.addActionListener(event -> {
			analyzeCallback.run();
			refreshAnalysis();
		});

		showBankButton = new JButton("Open Blueprint");
		showBankButton.addActionListener(event -> showBankDialog());

		toggleButton = new JButton();
		toggleButton.addActionListener(event -> onToggleGuide());

		assignCategoriesButton = new JButton();
		assignCategoriesButton.addActionListener(event -> {
			guideController.toggleCategoryAssignMode();
			refreshControls();
		});

		resetOverridesButton = new JButton("Reset Corrections");
		resetOverridesButton.addActionListener(event -> {
			resetOverridesCallback.run();
			refreshControls();
		});

		tabOrderButton = new JButton("Tab Layout");
		tabOrderButton.setFont(FontManager.getRunescapeSmallFont());
		tabOrderButton.setToolTipText("Choose which categories go on which tab");
		tabOrderButton.setFocusPainted(false);
		tabOrderButton.addActionListener(event -> toggleLayoutEditor());

		layoutHeading = label("");
		layoutRows = verticalPanel();
		resetLayoutButton = new JButton("Reset to default");
		resetLayoutButton.setFont(FontManager.getRunescapeSmallFont());
		resetLayoutButton.setFocusPainted(false);
		resetLayoutButton.addActionListener(event ->
			applyLayoutPlan(BankLayoutPlan.defaultFor(bankLayoutModel.preset())));
		resetLayoutButton.setAlignmentX(LEFT_ALIGNMENT);
		// Asked separately, because they are the same mechanism but not the same
		alchPileBox = layoutOptionBox("Gather outclassed gear",
			"<html>Gather replaced gear and tools, plus spare standard Mystic colours. "
				+ "Keep the best owned tool and each staff's rune supply.<br>Reviewed obsolete clue gear may move; "
				+ "Dragon halberds always count as alch stock.<br>Off: keep normal categories.</html>");

		saveProfileButton = layoutActionButton("Save as", "Save this layout under a name",
			event -> saveLayoutAs());
		importButton = layoutActionButton("Import", "Paste a layout shared with you",
			event -> importLayout());
		exportButton = layoutActionButton("Export", "Copy this layout to the clipboard to share",
			event -> exportLayout());
		deleteProfileButton = layoutActionButton("Delete", "Forget the selected saved layout",
			event -> deleteSelectedProfile());

		JPanel profileActions = new JPanel(new GridLayout(1, 0, 2, 0));
		profileActions.setOpaque(false);
		profileActions.add(saveProfileButton);
		profileActions.add(importButton);
		profileActions.add(exportButton);
		profileActions.add(deleteProfileButton);

		layoutEditor = verticalPanel();
		layoutEditor.setVisible(false);
		layoutHelp = mutedLabel(layoutHelpText());
		layoutHelp.setAlignmentX(LEFT_ALIGNMENT);
		layoutRows.setAlignmentX(LEFT_ALIGNMENT);
		sizeToSidebar(profileActions, 22);
		layoutEditor.add(profileActions);
		layoutEditor.add(Box.createVerticalStrut(6));
		layoutEditor.add(layoutHelp);
		layoutEditor.add(Box.createVerticalStrut(6));
		layoutEditor.add(alchPileBox);
		layoutEditor.add(Box.createVerticalStrut(6));
		// Kept for its text, but not shown: every tab already carries its own
		// heading in the list, so a second copy on top only repeated it.
		layoutHeading.setVisible(false);
		layoutEditor.add(layoutRows);
		layoutEditor.add(Box.createVerticalStrut(6));
		layoutEditor.add(resetLayoutButton);

		categoryOverrideLabel = label("");
		statusLabel = label("");
		catalogSummaryLabel = label("");
		organizationPreviewLabel = label("");
		guideProgressLabel = label("");
		nextMoveLabel = label("");

		guideProgressBar = new ProgressBar();
		guideProgressBar.setMaximumValue(100);
		guideProgressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		guideProgressBar.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		guideProgressBar.setVisible(false);

		detailsChevron = new JLabel(new ImageIcon(chevronIcon(false)));
		detailsPanel = verticalPanel();
		detailsPanel.setVisible(false);
		detailsPanel.add(card(catalogSummaryLabel));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(organizationPreviewLabel));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(statusLabel, guideProgressLabel,
			mutedLabel(PREVIEW_BLOCK_HELP), mutedLabel(PREVIEW_OVERLAY_NOTE)));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(categoryOverrideLabel, resetOverridesButton,
			mutedLabel(CATEGORY_CORRECTION_HELP)));

		JPanel content = new JPanel(new GridBagLayout());
		content.setOpaque(false);
		GridBagConstraints cardConstraints = new GridBagConstraints();
		cardConstraints.gridx = 0;
		cardConstraints.gridy = 0;
		cardConstraints.weightx = 1;
		cardConstraints.fill = GridBagConstraints.HORIZONTAL;
		cardConstraints.insets = new Insets(0, 0, 8, 0);
		content.add(actionRow(), cardConstraints);
		cardConstraints.gridy++;
		content.add(destinationGrid(), cardConstraints);
		cardConstraints.gridy++;
		content.add(tabOrderButton, cardConstraints);
		cardConstraints.gridy++;
		content.add(layoutEditor, cardConstraints);
		cardConstraints.gridy++;
		content.add(guideProgressBar, cardConstraints);
		cardConstraints.gridy++;
		JPanel instruction = new JPanel(new BorderLayout(0, 8));
		instruction.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		instruction.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		JLabel heading = new JLabel("NEXT STEP");
		heading.setFont(FontManager.getRunescapeSmallFont());
		heading.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		instruction.add(heading, BorderLayout.NORTH);
		instruction.add(nextMoveLabel, BorderLayout.CENTER);
		cardConstraints.weighty = 1;
		cardConstraints.fill = GridBagConstraints.HORIZONTAL;
		cardConstraints.anchor = GridBagConstraints.NORTH;
		content.add(instruction, cardConstraints);
		cardConstraints.weighty = 0;
		cardConstraints.fill = GridBagConstraints.HORIZONTAL;
		cardConstraints.gridy++;
		content.add(detailsHeader(), cardConstraints);
		cardConstraints.gridy++;
		content.add(detailsPanel, cardConstraints);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(identityStrip(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		refreshControls();

		// Refreshes the switches too, not just the status text: turning on one
		// mode turns the other off, and assign mode is also reachable from the
		// bank's right-click menu, so the lit edges have to follow state the
		// player did not change from this panel.
		statusTimer = new Timer(STATUS_REFRESH_MILLIS, event -> refreshControls());
		statusTimer.start();
	}

	/** Name and profile on two lines, next to the same mark as the toolbar button. */
	private JPanel identityStrip()
	{
		JPanel strip = new JPanel(new BorderLayout(8, 0));
		strip.setOpaque(false);
		strip.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BORDER_COLOR),
			BorderFactory.createEmptyBorder(2, 0, 8, 0)));

		JLabel mark = new JLabel(new ImageIcon(IronmanBankArchitectPlugin.createIcon()));
		mark.setVerticalAlignment(SwingConstants.CENTER);

		JPanel who = verticalPanel();
		JLabel title = new JLabel(TITLE);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.TEXT_COLOR);
		title.setAlignmentX(LEFT_ALIGNMENT);
		presetChooser.setFont(FontManager.getRunescapeSmallFont());
		presetChooser.setAlignmentX(LEFT_ALIGNMENT);
		presetChooser.setMaximumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, 24));
		presetChooser.setPrototypeDisplayValue("Ironman: Custom layout");
		nameTheClosedChooser(presetChooser, "Preset");
		refreshPresetChoices();
		presetChooser.addActionListener(event -> {
			if (refreshingPreset || presetChooser.getSelectedItem() == null) return;
			bankLayoutModel.selectPresetChoice(presetChooser.getSelectedItem().toString());
			refreshSettings();
		});
		who.add(title);
		who.add(presetChooser);

		strip.add(mark, BorderLayout.WEST);
		strip.add(who, BorderLayout.CENTER);
		strip.setToolTipText(SUMMARY + ". " + SAFETY_NOTE);
		return strip;
	}

	/**
	 * The ten blueprint destinations as a grid of your own item sprites. It says
	 * the same thing the old ten-line list said, without asking anyone to read.
	 */
	private JPanel destinationGrid()
	{
		JPanel grid = new JPanel(new GridLayout(0, DESTINATION_COLUMNS,
			DESTINATION_GAP, DESTINATION_GAP));
		grid.setOpaque(false);
		for (int index = 0; index < CategoryPalette.size(); index++)
		{
			DestinationCell cell = new DestinationCell(index);
			destinationCells.add(cell);
			grid.add(cell);
		}
		return grid;
	}

	private JPanel actionRow()
	{
		JPanel row = new JPanel(new GridLayout(0, 1, 0, 4));
		row.setOpaque(false);

		showBankButton.setText("Open Blueprint");
		for (JButton button : new JButton[] {analyzeButton, showBankButton,
			toggleButton, assignCategoriesButton, tabOrderButton})
		{
			button.setFont(FontManager.getRunescapeSmallFont());
			button.setPreferredSize(new Dimension(SIDEBAR_CONTENT_WIDTH,
				button == analyzeButton || button == showBankButton ? 36 : 30));
		}
		row.add(analyzeButton);
		row.add(showBankButton);
		row.add(toggleButton);
		row.add(assignCategoriesButton);
		JPanel actions = new JPanel(new BorderLayout(0, 4));
		actions.setOpaque(false);
		actions.add(row, BorderLayout.CENTER);
		workflowSteps = label("<html>1. Analyze your bank<br>2. Edit the blueprint<br>3. Follow the sorting guide</html>");
		workflowSteps.setFont(FontManager.getRunescapeSmallFont());
		workflowSteps.setForeground(ColorScheme.TEXT_COLOR);
		actions.add(workflowSteps, BorderLayout.SOUTH);
		return actions;
	}

	private JPanel detailsHeader()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.MEDIUM_GRAY_COLOR));

		JLabel caption = new JLabel(DETAILS_LABEL);
		caption.setFont(FontManager.getRunescapeSmallFont());
		caption.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		header.add(caption, BorderLayout.WEST);
		header.add(detailsChevron, BorderLayout.EAST);
		header.setCursor(new Cursor(Cursor.HAND_CURSOR));
		header.setToolTipText("Show everything the panel knows");
		header.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent event)
			{
				setDetailsExpanded(!detailsPanel.isVisible());
			}
		});
		return header;
	}

	private void setDetailsExpanded(boolean expanded)
	{
		detailsPanel.setVisible(expanded);
		detailsChevron.setIcon(new ImageIcon(chevronIcon(expanded)));
		revalidate();
		repaint();
	}

	void shutdown()
	{
		statusTimer.stop();
		if (bankDialog != null)
		{
			bankDialog.dispose();
			bankDialog = null;
		}
	}

	JButton getAnalyzeButton()
	{
		return analyzeButton;
	}

	JButton getShowBankButton()
	{
		return showBankButton;
	}

	JLabel getCatalogSummaryLabel()
	{
		return catalogSummaryLabel;
	}

	JLabel getOrganizationPreviewLabel()
	{
		return organizationPreviewLabel;
	}

	JLabel getGuideProgressLabel()
	{
		return guideProgressLabel;
	}

	JLabel getNextMoveLabel()
	{
		return nextMoveLabel;
	}

	JButton getAssignCategoriesButton()
	{
		return assignCategoriesButton;
	}

	JButton getResetOverridesButton()
	{
		return resetOverridesButton;
	}

	JButton getTabOrderButton()
	{
		return tabOrderButton;
	}

	JLabel getCategoryOverrideLabel()
	{
		return categoryOverrideLabel;
	}

	JPanel getDetailsPanel()
	{
		return detailsPanel;
	}

	List<DestinationCell> getDestinationCells()
	{
		return destinationCells;
	}

	ProgressBar getGuideProgressBar()
	{
		return guideProgressBar;
	}

	/**
	 * Shows or hides the layout editor beneath the destination grid. It lives in
	 * the sidebar rather than a window of its own so the destinations stay on
	 * screen while they are being filled: the grid above is the picker, and the
	 * list below is whatever that destination currently holds.
	 */
	private void toggleLayoutEditor()
	{
		setLayoutEditorVisible(!layoutEditor.isVisible());
	}

	private void setLayoutEditorVisible(boolean visible)
	{
		layoutEditor.setVisible(visible);
		if (visible)
		{
			layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
			renderLayoutEditor();
		}
		tabOrderButton.setToolTipText(visible
			? "Close the tab layout editor" : "Choose which categories go on which tab");
		revalidate();
		repaint();
	}

	boolean isLayoutEditorVisible()
	{
		return layoutEditor.isVisible();
	}

	/** Highlights a destination in the grid so its row can be found in the list. */
	void selectDestination(int destinationIndex)
	{
		selectedDestination = destinationIndex;
		renderLayoutEditor();
	}

	int getSelectedDestination()
	{
		return selectedDestination;
	}

	BankLayoutPlan getLayoutPlan()
	{
		return layoutPlan;
	}

	/**
	 * Moves one category to one destination. Every category carries its own
	 * destination chooser, so nothing has to be selected first and no category is
	 * ever tied to the tab it started on: picking a tab is the whole gesture.
	 */
	void moveTagTo(String tagKey, int destinationIndex)
	{
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		applyLayoutPlan(layoutPlan.withTagAt(tagKey, destinationIndex));
	}

	JComboBox<String> getPresetChooser() { return presetChooser; }

	JPanel getLayoutRows()
	{
		return layoutRows;
	}

	JLabel getLayoutHeading()
	{
		return layoutHeading;
	}

	/** Reorders a category within its destination, which decides what leads the tab. */
	void shiftWithinSelected(String tagKey, int offset)
	{
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		applyLayoutPlan(layoutPlan.withTagShifted(tagKey, offset));
	}

	private void applyLayoutPlan(BankLayoutPlan updated)
	{
		BankLayoutPlan completed = updated.completedFor(bankLayoutModel.preset());
		if (completed.serialize().equals(layoutPlan.serialize()))
		{
			return;
		}

		layoutPlan = completed;
		bankLayoutModel.save(layoutPlan);
		renderLayoutEditor();
	}

	private void renderLayoutEditor()
	{
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		layoutHelp.setText(sidebarHtml(layoutHelpText()));
		BankOrganizationPreview preview = guideController.bankAnalysisStatus()
			.organizationPreview()
			.orElse(null);
		renderedLayoutPreview = preview;
		Map<String, Integer> tagCounts = preview == null
			? Collections.emptyMap() : preview.getTagCounts();

		// Reads back the whole assignment, "Tab 3 - Teleports & Runes", so the
		// player can confirm what they built without counting cells.
		// Each tab carries its own heading in the list below, so a summary of the
		// selected one on top would only repeat it, and repeat it in the width
		// that was already too narrow to read.
		layoutHeading.setText(destinationName(selectedDestination) + " - "
			+ describeDestination(selectedDestination));

		// Grouped by destination rather than listing the categories flat: the
		// question the player is answering is what each tab holds, so the tab is
		// the heading and its categories sit underneath it. An empty tab still
		// gets a heading, because "nothing here" is a state worth seeing.
		layoutRows.removeAll();
		for (int destination = 0; destination < BankLayoutPlan.DESTINATION_COUNT; destination++)
		{
			layoutRows.add(destinationHeader(destination, tagCounts));
			layoutRows.add(tagChooser(destination));
			addLayoutChoices(destination);
			List<String> keys = layoutPlan.getTagKeys(destination);
			if (keys.isEmpty())
			{
				layoutRows.add(emptyDestinationNote());
			}
			for (String key : keys)
			{
				layoutRows.add(layoutRow(BankTags.byKey(key), destination, tagCounts));
				if (key.equals(arrangeOpenTagKey))
				{
					addArrangeRows(key);
				}
			}
			layoutRows.add(Box.createVerticalStrut(6));
		}

		resetLayoutButton.setEnabled(!layoutPlan.isDefault(bankLayoutModel.preset()));
		refreshProfiles();
		refreshLayoutOptions();
		for (DestinationCell cell : destinationCells)
		{
			cell.repaint();
		}
		layoutRows.revalidate();
		layoutRows.repaint();
		revalidate();
		repaint();
	}

	/**
	 * One tag sitting on a tab, with the controls that move it. The name wraps
	 * above the buttons rather than beside them: at 225px a name and controls on
	 * one line squeezed the buttons off the right edge, which left the player
	 * looking at a list they could read but not change.
	 */
	private JPanel layoutRow(BankTag tag, int destination, Map<String, Integer> tagCounts)
	{
		String key = tag.getKey();
		List<String> onDestination = layoutPlan.getTagKeys(destination);
		int position = onDestination.indexOf(key);
		boolean isFallback = BankLayoutPlan.FALLBACK_TAG_KEY.equals(key);

		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 3, 0, 0,
				CategoryPalette.colorForCategory(tag.getCategoryKey(), destination)),
			BorderFactory.createEmptyBorder(2, 4, 2, 0)));
		sizeToSidebar(row, 40);

		int itemCount = tagCounts.getOrDefault(key, 0);
		JLabel name = new JLabel("<html><body width='" + LAYOUT_NAME_WIDTH + "'>" + tag.getName()
			+ "<br><font color='#9a9a9a'>" + itemCount
			+ " items</font></body></html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(Color.WHITE);
		name.setToolTipText(tag.getName());

		JPanel buttons = new JPanel(new GridLayout(1, 0, 1, 0));
		buttons.setOpaque(false);
		List<BankBlockDescriptor> blocks = blocksFor(key);
		buttons.add(layoutButton("≡", blocks.size() >= 2
				? (key.equals(arrangeOpenTagKey)
					? "Close the arrange rows"
					: "Arrange the blocks inside " + tag.getName())
				: "Nothing to arrange here - the order is curated geometry, or the "
					+ "tag holds a single block. A Grid layout arranges once "
					+ "switched to List.",
			blocks.size() >= 2, event -> toggleArrange(key)));
		buttons.add(layoutButton("▲", "Move earlier on this tab", position > 0,
			event -> shiftWithinSelected(key, -1)));
		buttons.add(layoutButton("▼", "Move later on this tab",
			position >= 0 && position < onDestination.size() - 1,
			event -> shiftWithinSelected(key, 1)));
		buttons.add(layoutButton("✕", isFallback
				? "This tag catches everything left over, so it always has a tab"
				: "Take " + tag.getName() + " off this tab and send it to the "
					+ fallbackName() + " tab",
			!isFallback, event -> removeFromTab(key)));

		row.add(name, BorderLayout.CENTER);
		row.add(buttons, BorderLayout.EAST);
		return row;
	}

	private List<BankBlockDescriptor> blocksFor(String tagKey)
	{
		if (renderedLayoutPreview == null)
		{
			return Collections.emptyList();
		}
		List<BankBlockDescriptor> blocks = renderedLayoutPreview.getBlockDescriptors().get(tagKey);
		return blocks == null ? Collections.emptyList() : blocks;
	}

	private void toggleArrange(String tagKey)
	{
		arrangeOpenTagKey = tagKey.equals(arrangeOpenTagKey) ? null : tagKey;
		renderLayoutEditor();
	}

	/**
	 * The blocks of one tag as rows the player can shift, in the order the
	 * blueprint will lay them out. Every press stores the whole sequence, so a
	 * single nudge is the same statement a full rearrangement would be.
	 */
	private void addArrangeRows(String tagKey)
	{
		List<BankBlockDescriptor> blocks = blocksFor(tagKey);
		for (int index = 0; index < blocks.size(); index++)
		{
			layoutRows.add(blockRow(tagKey, blocks, index));
		}

		JPanel reset = new JPanel(new BorderLayout());
		reset.setOpaque(false);
		reset.setBorder(BorderFactory.createEmptyBorder(2, 16, 2, 2));
		JButton resetButton = new JButton("Reset to curated order");
		resetButton.setFont(FontManager.getRunescapeSmallFont());
		resetButton.setToolTipText(
			"Forget this tag's saved order; the curated order stands again");
		resetButton.addActionListener(event ->
			bankLayoutModel.saveBlockOrder(tagKey, Collections.emptyList()));
		reset.add(resetButton, BorderLayout.WEST);
		sizeToSidebar(reset, 26);
		layoutRows.add(reset);
	}

	private JPanel blockRow(String tagKey, List<BankBlockDescriptor> blocks, int index)
	{
		BankBlockDescriptor block = blocks.get(index);
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(1, 16, 1, 0));
		sizeToSidebar(row, 24);

		String countNote = block.getMemberCount() > 1
			? " <font color='#9a9a9a'>(" + block.getMemberCount() + ")</font>" : "";
		JLabel name = new JLabel("<html><body width='" + LAYOUT_NAME_WIDTH + "'>"
			+ block.getDisplayName() + countNote + "</body></html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(Color.WHITE);
		name.setToolTipText(block.getDisplayName());

		JPanel buttons = new JPanel(new GridLayout(1, 0, 1, 0));
		buttons.setOpaque(false);
		buttons.add(layoutButton("⤒", "Send this block to the top", index > 0,
			event -> moveBlock(tagKey, index, 0)));
		buttons.add(layoutButton("▲", "Move this block earlier", index > 0,
			event -> moveBlock(tagKey, index, index - 1)));
		buttons.add(layoutButton("▼", "Move this block later", index < blocks.size() - 1,
			event -> moveBlock(tagKey, index, index + 1)));

		row.add(name, BorderLayout.CENTER);
		row.add(buttons, BorderLayout.EAST);
		return row;
	}

	private void moveBlock(String tagKey, int from, int to)
	{
		List<BankBlockDescriptor> blocks = blocksFor(tagKey);
		List<String> keys = new ArrayList<>();
		for (BankBlockDescriptor block : blocks)
		{
			keys.add(block.getBlockKey());
		}
		if (from < 0 || from >= keys.size() || to < 0 || to >= keys.size())
		{
			return;
		}
		keys.add(to, keys.remove(from));
		bankLayoutModel.saveBlockOrder(tagKey, keys);
	}

	/**
	 * Pins a row to the sidebar's width. A vertical BoxLayout hands a component
	 * its preferred width and centres whatever is narrower, so one item that
	 * wants to be wide pushes the whole column out of view; fixing the width
	 * keeps every row starting at the same left edge.
	 */
	private void sizeToSidebar(JComponent component, int height)
	{
		// Preferred width is a baseline; the maximum is left open so the
		// vertical BoxLayout stretches every row to the sidebar's true width
		// on any platform, instead of leaving a dead strip beside a guess.
		component.setPreferredSize(new Dimension(SIDEBAR_CONTENT_WIDTH, height));
		component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		component.setMinimumSize(new Dimension(SIDEBAR_CONTENT_WIDTH, height));
		component.setAlignmentX(LEFT_ALIGNMENT);
	}

	/**
	 * The chooser that fills a tab. It sits on the tab and lists the tags, not
	 * the other way round: the player is furnishing a tab, so the tab is what
	 * they have already picked and the tag is what they are choosing.
	 */
	private JComboBox<String> tagChooser(int destination)
	{
		JComboBox<String> chooser = new JComboBox<>();
		String prompt = "Add a category or tag...";
		chooser.addItem(prompt);
		final List<String> keys = new ArrayList<>();
		for (BankTag tag : BankTags.all())
		{
			if (layoutPlan.destinationOf(tag.getKey()) == destination)
			{
				continue;
			}
			keys.add(tag.getKey());
			chooser.addItem(tag.getName() + "  (" + destinationName(
				layoutPlan.destinationOf(tag.getKey())) + ")");
		}

		chooser.setFont(FontManager.getRunescapeSmallFont());
		chooser.setFocusable(false);
		chooser.setEnabled(!keys.isEmpty());
		chooser.setToolTipText("Put a category or tag on " + destinationName(destination));
		// Without a prototype the box asks to be as wide as its longest entry,
		// and one long tag name would drag the whole sidebar column off screen.
		chooser.setPrototypeDisplayValue(prompt);
		sizeToSidebar(chooser, 22);
		// The prompt is the resting selection, so choosing the same tag twice in
		// a row still fires and the list never looks stuck on a past choice.
		chooser.addActionListener(event -> {
			int index = chooser.getSelectedIndex() - 1;
			if (index >= 0 && index < keys.size())
			{
				moveTagTo(keys.get(index), destination);
			}
		});
		return chooser;
	}

	/**
	 * One layout option, beside the tabs it affects rather than in the client's
	 * plugin settings. The two of them are the only layout choices a plan cannot
	 * state, so this is where a player already deciding how a tab should look
	 * will look for them.
	 */
	private JCheckBox layoutOptionBox(String text, String tooltip)
	{
		JCheckBox box = new JCheckBox(text);
		box.setFont(FontManager.getRunescapeSmallFont());
		box.setForeground(Color.WHITE);
		box.setOpaque(false);
		box.setFocusPainted(false);
		box.setToolTipText(tooltip);
		box.setAlignmentX(LEFT_ALIGNMENT);
		box.addActionListener(event -> {
			if (!refreshingOptions)
			{
				BankLayoutOptions base = bankLayoutModel.options();
				bankLayoutModel.saveOptions(new BankLayoutOptions(base.fillGearRows(), base.fillHerbloreRows(),
					alchPileBox.isSelected(), ordersOf(base), base.gearLayout(), base.potionDoses(),
					base.runeOrder(), base.teleportOrder(), base.gatherFrequentlyUsed()));
				refreshProfiles();
			}
		});
		return box;
	}

	/**
	 * The layout choices belonging to one tab, under its heading.
	 *
	 * <p>A choice is asked where the player already is. Reaching the client's
	 * plugin settings to decide how the gear tab should look means leaving the
	 * list of tabs to go and find a menu that says nothing about tabs, so the
	 * question is repeated here, beside the tab it shapes. Both places write the
	 * same setting.</p>
	 *
	 * <p>Only the categories actually sitting on this tab are asked about, and
	 * the three utility categories share one setting, so a tab holding runes and
	 * teleports still shows a single choice.</p>
	 *
	 * <p>The controls go straight into the rows rather than into a panel of
	 * their own: a panel in this list reads as a tag row, both to the eye and to
	 * the code that walks it.</p>
	 */
	private String layoutHelpText()
	{
		return LAYOUT_EDITOR_HELP + (bankLayoutModel.preset().getType() == BankPresetType.MAIN
			? "Main keeps potion families in dose order 4 to 1. Herbs run by stage: grimy, clean, seeds, unfinished."
			: "Put Part Doses on the tab with Potions and the Herblore tab runs by kind instead of a row per recipe.");
	}

	private void addLayoutChoices(int destination)
	{
		Set<BankCategorySortMode> modes = new LinkedHashSet<>();
		for (String key : layoutPlan.getTagKeys(destination))
		{
			BankCategory category = bankLayoutModel.preset()
				.getCategory(BankTags.byKey(key).getCategoryKey());
			if (category != null)
			{
				modes.add(category.getSortMode());
			}
		}

		BankLayoutOptions options = bankLayoutModel.options();
		boolean utilitiesAsked = false;
		for (BankCategorySortMode mode : modes)
		{
			switch (mode)
			{
				case GEAR:
					layoutRows.add(gearLayoutChooser(options));
					break;
				case MAIN:
				case TELEPORTS:
				case CURRENCY:
					if (!utilitiesAsked)
					{
						utilitiesAsked = true;
						layoutRows.add(orderChooser(options, BankCategorySortMode.MAIN,
							"How the runes, teleports and currency read: Grid keeps the curated "
								+ "shapes, List runs them in reading order."));
					}
					break;
				case TOOLS:
				case RESOURCES:
				case CLUES:
					layoutRows.add(orderChooser(options, mode,
						"Grid keeps this tab's curated shape; List runs it in reading order, "
							+ "wrapping like text."));
					break;
				default:
					// Supplies and Herblore ask their question as a checkbox in
					// the client settings; the tab order is not theirs to choose.
					break;
			}
		}
	}

	private JComboBox<GearLayout> gearLayoutChooser(BankLayoutOptions options)
	{
		JComboBox<GearLayout> chooser = new JComboBox<>(GearLayout.values());
		chooser.setSelectedItem(options.gearLayout());
		chooser.setToolTipText("<html>Best in slot: the four-style matrix, your strongest melee, "
			+ "ranged, magic and prayer options per slot.<br>Sets together: each set as a "
			+ "column.<br>List: sets as runs, strongest first.</html>");
		styleChooser(chooser);
		chooser.addActionListener(event -> {
			if (!refreshingOptions)
			{
				GearLayout chosen = (GearLayout) chooser.getSelectedItem();
				bankLayoutModel.saveOptions(withGearLayout(bankLayoutModel.options(), chosen));
				refreshProfiles();
			}
		});
		return chooser;
	}

	private JComboBox<TabOrder> orderChooser(BankLayoutOptions options, BankCategorySortMode mode,
		String tooltip)
	{
		JComboBox<TabOrder> chooser = new JComboBox<>(TabOrder.values());
		chooser.setSelectedItem(options.orderFor(mode));
		chooser.setToolTipText(tooltip);
		styleChooser(chooser);
		chooser.addActionListener(event -> {
			if (!refreshingOptions)
			{
				TabOrder chosen = (TabOrder) chooser.getSelectedItem();
				bankLayoutModel.saveOptions(withOrder(bankLayoutModel.options(), mode, chosen));
				refreshProfiles();
			}
		});
		return chooser;
	}

	private void styleChooser(JComboBox<?> chooser)
	{
		chooser.setFont(FontManager.getRunescapeSmallFont());
		chooser.setFocusable(false);
		sizeToSidebar(chooser, 20);
		nameTheClosedChooser(chooser);
	}

	/**
	 * Says "Layout: Best in slot" while closed, and just "Best in slot" in the
	 * open list.
	 *
	 * <p>Unlabelled, this box sits under the tag chooser and reads as a second
	 * copy of it: the value alone does not say what question it answers, and a
	 * player scanning the tab list slides straight past it. A heading of its own
	 * would cost a row in a sidebar already short of them, so the name rides on
	 * the closed value instead - the one place it is always visible.</p>
	 */
	private void nameTheClosedChooser(JComboBox<?> chooser)
	{
		nameTheClosedChooser(chooser, "Layout");
	}

	private void nameTheClosedChooser(JComboBox<?> chooser, String label)
	{
		ListCellRenderer<Object> base = (ListCellRenderer<Object>) chooser.getRenderer();
		chooser.setRenderer((list, value, index, selected, focused) -> {
			Component cell = base.getListCellRendererComponent(
				list, value, index, selected, focused);
			if (index < 0 && cell instanceof JLabel)
			{
				((JLabel) cell).setText(label + ": " + value);
			}
			return cell;
		});
	}

	private BankLayoutOptions withGearLayout(BankLayoutOptions base, GearLayout gearLayout)
	{
		return new BankLayoutOptions(base.fillGearRows(), base.fillHerbloreRows(), base.alchPile(),
			ordersOf(base), gearLayout, base.potionDoses(), base.runeOrder(), base.teleportOrder(), base.gatherFrequentlyUsed());
	}

	private BankLayoutOptions withOrder(BankLayoutOptions base, BankCategorySortMode mode,
		TabOrder order)
	{
		Map<BankCategorySortMode, TabOrder> orders = ordersOf(base);
		orders.put(mode, order);
		if (mode == BankCategorySortMode.MAIN)
		{
			// One setting behind three categories: choosing for the main tab
			// chooses for the teleports and currency that share it.
			orders.put(BankCategorySortMode.TELEPORTS, order);
			orders.put(BankCategorySortMode.CURRENCY, order);
		}
		return new BankLayoutOptions(base.fillGearRows(), base.fillHerbloreRows(), base.alchPile(),
			orders, base.gearLayout(), base.potionDoses(), base.runeOrder(), base.teleportOrder(), base.gatherFrequentlyUsed());
	}

	private Map<BankCategorySortMode, TabOrder> ordersOf(BankLayoutOptions base)
	{
		Map<BankCategorySortMode, TabOrder> orders =
			new EnumMap<>(BankCategorySortMode.class);
		for (BankCategorySortMode mode : BankCategorySortMode.values())
		{
			orders.put(mode, base.orderFor(mode));
		}
		return orders;
	}

	private void refreshLayoutOptions()
	{
		refreshingOptions = true;
		try
		{
			BankLayoutOptions options = bankLayoutModel.options();
			alchPileBox.setSelected(options.alchPile());
			alchPileBox.setVisible(true);
		}
		finally
		{
			refreshingOptions = false;
		}
	}

	JCheckBox getAlchPileBox()
	{
		return alchPileBox;
	}

	private JButton layoutActionButton(String text, String tooltip,
		java.awt.event.ActionListener action)
	{
		JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusPainted(false);
		button.setToolTipText(tooltip);
		button.addActionListener(action);
		return button;
	}

	/** One header chooser represents both bundled presets and saved customs. */
	private void refreshProfiles()
	{
		refreshPresetChoices();
		String matching = bankLayoutModel.matchingProfile();
		deleteProfileButton.setEnabled(!matching.isEmpty()
			&& !bankLayoutModel.defaultProfileName().equals(matching));
	}

	private void refreshPresetChoices()
	{
		refreshingPreset = true;
		try
		{
			List<String> choices = bankLayoutModel.presetChoices();
			presetChooser.setModel(new DefaultComboBoxModel<>(choices.toArray(new String[0])));
			presetChooser.setSelectedItem(bankLayoutModel.selectedPresetChoice());
			presetChooser.setToolTipText("Using " + bankLayoutModel.selectedPresetChoice()
				+ ". Editing a bundled preset creates a custom copy.");
		}
		finally
		{
			refreshingPreset = false;
		}
	}

	private void saveLayoutAs()
	{
		String suggested = bankLayoutModel.matchingProfile();
		String name = JOptionPane.showInputDialog(this, "Name for this layout:",
			suggested.isEmpty() || bankLayoutModel.defaultProfileName().equals(suggested)
				? "My layout" : suggested);
		if (name == null || name.trim().isEmpty())
		{
			return;
		}

		bankLayoutModel.saveProfile(name.trim(), layoutPlan);
		renderLayoutEditor();
	}

	/**
	 * Copies the layout as a share code. Nothing leaves the client: the code goes
	 * to the clipboard and it is the player who decides where to paste it.
	 */
	private void exportLayout()
	{
		String matching = bankLayoutModel.matchingProfile();
		String code = BankLayoutShareCode.encode(
			matching.isEmpty() ? "Shared layout" : matching, layoutPlan, bankLayoutModel.preset());
		Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(code), null);
		JOptionPane.showMessageDialog(this,
			"Layout copied to your clipboard. Paste it to share it.",
			"Tab layout exported", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Reads a pasted share code into a new saved layout. It is always saved under
	 * a free name, so importing can never overwrite a layout the player built.
	 */
	private void importLayout()
	{
		String pasted = JOptionPane.showInputDialog(this,
			"Paste the layout code you were given:", "");
		if (pasted == null)
		{
			return;
		}

		java.util.Optional<BankLayoutShareCode> decoded = BankLayoutShareCode.decode(pasted);
		if (!decoded.isPresent())
		{
			JOptionPane.showMessageDialog(this,
				"That does not look like a Bank Architect layout code.",
				"Import failed", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (decoded.get().getPresetType() != bankLayoutModel.preset().getType())
		{
			JOptionPane.showMessageDialog(this, "Choose the " + decoded.get().getPresetType()
				+ " preset before importing this layout.", "Different preset", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		BankLayoutPlan imported = BankLayoutPlan
			.parse(bankLayoutModel.preset(), decoded.get().getPlan())
			.completedFor(bankLayoutModel.preset());
		bankLayoutModel.saveProfile(freeProfileName(decoded.get().getName()), imported);
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		renderLayoutEditor();
	}

	private void deleteSelectedProfile()
	{
		String matching = bankLayoutModel.matchingProfile();
		if (matching.isEmpty() || bankLayoutModel.defaultProfileName().equals(matching))
		{
			return;
		}

		bankLayoutModel.deleteProfile(matching);
		renderLayoutEditor();
	}

	/** A name no saved layout is using yet. */
	private String freeProfileName(String wanted)
	{
		return BankLayoutProfiles.freeName(wanted, bankLayoutModel.profileNames());
	}

	private String fallbackName()
	{
		return BankTags.byKey(BankLayoutPlan.FALLBACK_TAG_KEY).getName();
	}

	/**
	 * Takes a tag off a tab. It goes to the tab holding the fallback tag rather
	 * than nowhere, because its items still have to live somewhere and a silent
	 * disappearance is the one outcome the player cannot diagnose.
	 */
	void removeFromTab(String tagKey)
	{
		if (BankLayoutPlan.FALLBACK_TAG_KEY.equals(tagKey))
		{
			return;
		}

		int fallback = layoutPlan.destinationOf(BankLayoutPlan.FALLBACK_TAG_KEY);
		moveTagTo(tagKey, fallback < 0 ? BankLayoutPlan.DESTINATION_COUNT - 1 : fallback);
	}

	private JButton layoutButton(String glyph, String tooltip, boolean enabled,
		java.awt.event.ActionListener action)
	{
		JButton button = new JButton(glyph);
		button.setPreferredSize(new Dimension(20, 20));
		button.setMargin(new Insets(0, 0, 0, 0));
		// The RuneScape font draws none of these glyphs and RuneLite makes it
		// the Swing-wide default, so inheriting any "default" font still shows
		// tofu; the controls ask for a real system font by name.
		button.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
		button.setFocusPainted(false);
		button.setToolTipText(tooltip);
		button.setEnabled(enabled);
		button.addActionListener(action);
		return button;
	}

	/** The tab this group of rows belongs to, with what it will hold. */
	private JLabel destinationHeader(int destination, Map<String, Integer> tagCounts)
	{
		int items = itemsOnDestination(destination, tagCounts);
		JLabel header = new JLabel(destinationName(destination)
			+ (items > 0 ? " - " + items + " items" : ""));
		header.setFont(FontManager.getRunescapeBoldFont());
		header.setForeground(destination == selectedDestination
			? ColorScheme.BRAND_ORANGE : Color.WHITE);
		header.setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
		sizeToSidebar(header, 16);
		return header;
	}

	private JLabel emptyDestinationNote()
	{
		JLabel note = new JLabel("Empty - this tab is not created");
		note.setFont(FontManager.getRunescapeSmallFont());
		note.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		note.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));
		sizeToSidebar(note, 14);
		return note;
	}

	/** What a destination holds, spelled out: "Teleports & Runes + Clues". */
	private String describeDestination(int destinationIndex)
	{
		List<String> keys = layoutPlan.getTagKeys(destinationIndex);
		if (keys.isEmpty())
		{
			return "empty";
		}

		StringBuilder names = new StringBuilder();
		for (String key : keys)
		{
			if (names.length() > 0)
			{
				names.append(" + ");
			}
			names.append(BankTags.byKey(key).getName());
		}

		return names.toString();
	}

	private int itemsOnDestination(int destinationIndex, Map<String, Integer> tagCounts)
	{
		int total = 0;
		for (String key : layoutPlan.getTagKeys(destinationIndex))
		{
			total += tagCounts.getOrDefault(key, 0);
		}

		return total;
	}

	/**
	 * Numbered the way the move instructions already number them: the main
	 * section is not a tab, so the first real tab is Tab 1. Counting the main
	 * section as position one here would have the layout screen and the guide
	 * calling the same tab by two different numbers.
	 */
	private static String destinationName(int destinationIndex)
	{
		if (destinationIndex < 0)
		{
			return "no tab";
		}

		return destinationIndex == BankLayoutPlan.MAIN_DESTINATION_INDEX
			? "Main section" : "Tab " + destinationIndex;
	}

	/** The same identity as {@link #destinationName}, short enough for a 36px cell. */
	private static String destinationLabel(int destinationIndex)
	{
		return destinationIndex == BankLayoutPlan.MAIN_DESTINATION_INDEX
			? "M" : Integer.toString(destinationIndex);
	}

	private void onToggleGuide()
	{
		guideController.toggleGuide();
		refreshControls();
	}

	private void refreshControls()
	{
		toggleButton.setText("Sorting Guide: " + (guideController.isGuideEnabled() ? "On" : "Off"));
		assignCategoriesButton.setText("Assign Categories: " + (guideController.isCategoryAssignMode() ? "On" : "Off"));
		toggleButton.setToolTipText(guideController.isGuideEnabled()
			? "Bank guide is on" : "Bank guide is off");
		toggleButton.setBorder(activeBorder(guideController.isGuideEnabled()));
		assignCategoriesButton.setToolTipText(guideController.isCategoryAssignMode()
			? "Assigning categories - right-click a bank item" : "Assign categories");
		assignCategoriesButton.setBorder(activeBorder(guideController.isCategoryAssignMode()));
		refreshStatus();
	}

	/** A lit edge reads faster than a button label that flips between two verbs. */
	private static javax.swing.border.Border activeBorder(boolean active)
	{
		return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(
			active ? new Color(111, 169, 130) : ColorScheme.BORDER_COLOR),
			BorderFactory.createEmptyBorder(7, 4, 7, 4));
	}

	void refreshSettings()
	{
		refreshPresetChoices();
		arrangeOpenTagKey = null;
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		if (layoutEditor.isVisible()) renderLayoutEditor();
		refreshStatus();
	}

	private void refreshStatus()
	{
		categoryOverrideLabel.setText(sidebarHtml(guideController.getCategoryOverrideText()));
		resetOverridesButton.setEnabled(guideController.getCategoryOverrideCount() > 0);
		statusLabel.setText(sidebarHtml(guideController.getStatusText()));
		String guideText = guideController.getGuideProgressText();
		guideProgressLabel.setText(sidebarHtml(guideText));
		nextMoveLabel.setText(sidebarHtml(guideText).replace(
			"width='" + SIDEBAR_TEXT_WIDTH, "width='" + (SIDEBAR_TEXT_WIDTH - 18)));
		nextMoveLabel.setForeground(ColorScheme.TEXT_COLOR);
		nextMoveLabel.setVerticalAlignment(SwingConstants.TOP);
		int percent = guideController.getGuideProgressPercent();
		guideProgressBar.setVisible(percent >= 0);
		if (percent >= 0)
		{
			guideProgressBar.setValue(percent);
			guideProgressBar.setLeftLabel("Sorted");
			guideProgressBar.setCenterLabel(percent + "%");
			guideProgressBar.setRightLabel(plannedItemText());
		}
		refreshAnalysis();
	}

	private String plannedItemText()
	{
		BankOrganizationPreview preview = guideController.organizationPreview();
		return preview == null ? "" : Integer.toString(preview.getPlannedItemCount());
	}

	/**
	 * The resting panel shows one line; the rest of the instruction stays in the
	 * tooltip and under Details.
	 */
	static String firstSentence(String text)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}
		String lastLine = text.substring(text.lastIndexOf('\n') + 1).trim();
		int stop = lastLine.indexOf(". ");
		return stop < 0 ? lastLine : lastLine.substring(0, stop + 1);
	}

	private void refreshAnalysis()
	{
		BankLayoutPlan latestPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		boolean planChanged = !latestPlan.serialize().equals(layoutPlan.serialize());
		layoutPlan = latestPlan;
		BankAnalysisStatus analysis = guideController.bankAnalysisStatus();
		catalogSummaryLabel.setText(sidebarHtml(analysis.catalogSummaryText()));
		BankOrganizationPreview preview = analysis.organizationPreview().orElse(null);
		workflowSteps.setVisible(preview == null);
		organizationPreviewLabel.setText(sidebarHtml(blueprintStatusText(analysis)));
		showBankButton.setEnabled(preview != null);
		refreshDestinations(preview);
		if (layoutEditor.isVisible() && (planChanged || preview != renderedLayoutPreview))
		{
			renderLayoutEditor();
		}
		if (bankDialog != null && bankDialog.isVisible())
		{
			refreshBankDialog(preview);
		}
	}

	private void refreshDestinations(BankOrganizationPreview preview)
	{
		if (preview == renderedDestinations)
		{
			return;
		}
		renderedDestinations = preview;
		for (int index = 0; index < destinationCells.size(); index++)
		{
			destinationCells.get(index).update(preview == null
				|| index >= preview.getCategories().size()
				? null : preview.getCategories().get(index));
		}
	}

	/**
	 * The destination's fixed icon, so the same picture always means the same
	 * tab. A preset without a fixed icon falls back to whatever the player owns
	 * there, which is better than an empty cell.
	 */
	private static BankPreviewItem iconItem(BankCategoryPreview category)
	{
		if (category == null)
		{
			return null;
		}
		int fixed = CategoryIcons.iconItemId(category.getCategory().getKey());
		return fixed > 0
			? new BankPreviewItem(fixed, category.getCategory().getName(), 1)
			: firstRealItem(category);
	}

	private static BankPreviewItem firstRealItem(BankCategoryPreview category)
	{
		for (BankPreviewItem item : category.getItems())
		{
			if (item.getItemId() > 0 && !item.isBlank())
			{
				return item;
			}
		}
		return null;
	}

	/**
	 * One blueprint destination: the first item you own there, that tab's colour,
	 * and how many items land in it. The tooltip carries the full name.
	 */
	final class DestinationCell extends JPanel
	{
		private final int categoryIndex;
		private final JLabel sprite = new JLabel("", SwingConstants.CENTER);
		private String count = "";
		private String caption = "Not analyzed";

		private DestinationCell(int categoryIndex)
		{
			this.categoryIndex = categoryIndex;
			setLayout(new BorderLayout());
			setBackground(DESTINATION_EMPTY_BG);
			setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
				CategoryPalette.colorFor(categoryIndex)));
			setPreferredSize(new Dimension(90, 48));
			add(sprite, BorderLayout.CENTER);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent event)
				{
					// While the layout editor is open the grid is the destination
					// picker, so a click chooses what the list below shows rather
					// than opening the blueprint over the top of it.
					if (isLayoutEditorVisible())
					{
						selectDestination(DestinationCell.this.categoryIndex);
					}
					else
					{
						showBankDialog(DestinationCell.this.categoryIndex);
					}
				}
			});
		}

		String getCount()
		{
			return count;
		}

		private void update(BankCategoryPreview category)
		{
			boolean filled = category != null && category.getItemCount() > 0;
			count = filled ? Integer.toString(category.getItemCount()) : "";
			caption = category == null ? "Not analyzed" : category.getCategory().getName();
			sprite.setIcon(null);
			sprite.setText("");
			setBackground(filled ? DESTINATION_CELL_BG : DESTINATION_EMPTY_BG);
			// The colour belongs to the destination, not to the cell, so a
			// reordered tab keeps the colour the player already knows.
			setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
				CategoryPalette.colorForCategory(category == null ? ""
					: category.getCategory().getKey(), categoryIndex)));
			BankPreviewItem icon = iconItem(category);
			if (icon != null)
			{
				itemIconRenderer.accept(icon, sprite);
			}
			setToolTipText(category == null ? destinationName(categoryIndex)
				: destinationName(categoryIndex) + ": " + category.getCategory().getName()
					+ (filled ? " - " + category.getItemCount() + " items" : " - empty"));
			repaint();
		}

		/** Marks the destination the layout editor is showing. */
		private boolean isSelected()
		{
			return isLayoutEditorVisible() && selectedDestination == categoryIndex;
		}

		@Override
		public void paint(Graphics graphics)
		{
			super.paint(graphics);
			if (isSelected())
			{
				// Drawn rather than bordered: the bottom edge already carries the
				// destination's colour, which the selection must not overwrite.
				graphics.setColor(ColorScheme.BRAND_ORANGE);
				graphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
				graphics.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
			}
			// Painted after the sprite so both stay readable over any icon. The
			// tab's own number goes top left and the item count bottom right, so
			// the cell says which tab it is even before the bank is scanned.
			graphics.setFont(FontManager.getRunescapeSmallFont());
			FontMetrics metrics = graphics.getFontMetrics();
			String label = destinationLabel(categoryIndex);
			drawTag(graphics, label, 2, metrics.getAscent() + 1,
				isSelected() ? ColorScheme.BRAND_ORANGE : Color.WHITE);
			String shortName = caption;
			while (metrics.stringWidth(shortName) > getWidth() - 8 && shortName.length() > 1)
				shortName = shortName.substring(0, shortName.length() - 2) + "…";
			drawTag(graphics, shortName, 4, getHeight() - 5, ColorScheme.TEXT_COLOR);
			if (count.isEmpty())
			{
				return;
			}
			drawTag(graphics, count, getWidth() - metrics.stringWidth(count) - 2,
				metrics.getAscent() + 1, Color.WHITE);
		}

		private void drawTag(Graphics graphics, String text, int x, int y, Color color)
		{
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);
		}
	}

	private static BufferedImage chevronIcon(boolean pointingUp)
	{
		return drawIcon(graphics -> {
			graphics.setColor(ColorScheme.LIGHT_GRAY_COLOR);
			if (pointingUp)
			{
				graphics.drawLine(3, 8, 7, 4);
				graphics.drawLine(7, 4, 11, 8);
			}
			else
			{
				graphics.drawLine(3, 5, 7, 9);
				graphics.drawLine(7, 9, 11, 5);
			}
		});
	}

	private static BufferedImage drawIcon(java.util.function.Consumer<Graphics2D> painter)
	{
		BufferedImage icon = new BufferedImage(14, 14, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setStroke(new BasicStroke(1.4f));
		painter.accept(graphics);
		graphics.dispose();
		return icon;
	}

	private void showBankDialog(int categoryIndex)
	{
		showBankDialog();
		if (blueprintEditor != null) blueprintEditor.selectTab(categoryIndex);
	}

	private void showBankDialog()
	{
		BankOrganizationPreview preview = guideController.organizationPreview();
		if (preview == null)
		{
			return;
		}

		if (bankDialog == null)
		{
			Window owner = SwingUtilities.getWindowAncestor(this);
			bankDialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Bank Blueprint", false);
			bankDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
			bankDialog.setLocationRelativeTo(this);
			exportBlueprintButton = new JButton("Copy Blueprint Export");
			exportBlueprintButton.addActionListener(event -> copyBlueprintExport());
			blueprintEditor = new BlueprintEditorPanel(bankLayoutModel, this::itemCell,
				editing -> exportBlueprintButton.setEnabled(!editing));
			bankDialog.add(blueprintEditor, BorderLayout.CENTER);
			bankDialog.add(exportBlueprintButton, BorderLayout.SOUTH);
			bankDialog.addWindowListener(new java.awt.event.WindowAdapter()
			{
				@Override public void windowClosing(java.awt.event.WindowEvent event)
				{
					blueprintEditor.cancelEdit();
				}
			});
		}

		refreshBankDialog(preview);
		bankDialog.setVisible(true);
	}

	private void copyBlueprintExport()
	{
		BankOrganizationPreview preview = guideController.organizationPreview();
		if (preview == null)
		{
			return;
		}

		String export = BankBlueprintTextExporter.export(preview);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(export), null);
		exportBlueprintButton.setText("Copied Blueprint Export");
	}

	private void refreshBankDialog(BankOrganizationPreview preview)
	{
		if (blueprintEditor != null) blueprintEditor.setPreview(preview);
	}

	private JLabel itemCell(BankPreviewItem item)
	{
		JLabel label = new JLabel("", SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);
		label.setBackground(BANK_PANEL);
		Dimension cellSize = new Dimension(CELL_WIDTH, CELL_HEIGHT);
		label.setPreferredSize(cellSize);
		label.setMinimumSize(cellSize);
		label.setMaximumSize(cellSize);

		if (item.isBlank())
		{
			label.setToolTipText("Planned empty slot");
			label.setBorder(BorderFactory.createLineBorder(BLANK_SLOT_BORDER));
			return label;
		}

		label.setText(Integer.toString(item.getItemId()));
		label.setToolTipText(itemTooltip(item));
		label.setBorder(BorderFactory.createLineBorder(SLOT_BORDER));
		itemIconRenderer.accept(item, label);
		return label;
	}

	static String itemTooltip(BankPreviewItem item)
	{
		String text = item.toCompactLabel();
		if (item.getLayoutTagKey() != null)
			text += "\nPlacement: " + BankTags.byKey(item.getLayoutTagKey()).getName();
		if (!item.getUsageTags().isEmpty())
			text += "\nUses: " + String.join(", ", item.getUsageTags()).replace('-', ' ');
		if (item.getUsageTags().stream().anyMatch(tag -> tag.startsWith("storage-option-")))
			text += "\nStorage is optional; check access and capacity.";
		if (item.hasTag("storage-option-poh-fancy-dress-box"))
			text += "\nRequires a costume room and fancy dress box. UIM: store the full outfit before retrieving pieces.";
		if (item.hasTag("clue-required"))
			text += "\nKeep for clues, or check the matching STASH unit and full set.";
		if (item.hasTag("quest-use") || item.hasTag("quest-weapon"))
			text += "\nCheck current and later quest uses before removing.";
		if (item.getItemCategory() == ItemCategory.CLEANUP
			|| item.getItemCategory() == ItemCategory.UNKNOWN
			|| "cleanup".equals(item.getLayoutTagKey()) || "quest-items".equals(item.getLayoutTagKey()))
			text += "\nReview placement is not a recommendation to discard.";
		return sidebarHtml(text);
	}

	private static String blueprintStatusText(BankAnalysisStatus analysis)
	{
		BankOrganizationPreview preview = analysis.organizationPreview().orElse(null);
		if (preview == null)
		{
			return analysis.kind() == BankAnalysisStatus.Kind.RUNNING
				|| analysis.kind() == BankAnalysisStatus.Kind.FAILED
				? analysis.organizationPreviewText()
				: "Analyze your bank, then open the blueprint.";
		}

		return "Blueprint ready: " + preview.getPlannedItemCount() + " item IDs sorted.";
	}

	private static JPanel card(Component... rows)
	{
		JPanel card = new JPanel(new GridBagLayout());
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 6, 0);
		for (Component row : rows)
		{
			card.add(row, constraints);
			constraints.gridy++;
		}
		return card;
	}

	private static JLabel mutedLabel(String text)
	{
		JLabel label = new JLabel(sidebarHtml(text));
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	private static JPanel verticalPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		return panel;
	}

	private static JLabel label(String text)
	{
		JLabel label = new JLabel(text, SwingConstants.LEFT);
		label.setForeground(Color.WHITE);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	/**
	 * Sidebar labels need an explicit width so long lines wrap inside the
	 * plugin panel instead of pushing the cards past its right edge.
	 */
	private static String sidebarHtml(String text)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}

		return "<html><body width='" + SIDEBAR_TEXT_WIDTH + "'>" + text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\n", "<br>") + "</body></html>";
	}
}

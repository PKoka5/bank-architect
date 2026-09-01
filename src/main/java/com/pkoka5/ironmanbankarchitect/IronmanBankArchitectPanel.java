package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankBlueprintTextExporter;
import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankCategorySortMode;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutShareCode;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankTag;
import com.pkoka5.ironmanbankarchitect.organize.BankTags;
import com.pkoka5.ironmanbankarchitect.organize.CategoryIcons;
import com.pkoka5.ironmanbankarchitect.organize.CategoryPalette;
import com.pkoka5.ironmanbankarchitect.organize.GearLayout;
import com.pkoka5.ironmanbankarchitect.organize.PresetItemSorter;
import com.pkoka5.ironmanbankarchitect.organize.TabOrder;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ProgressBar;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Bank Architect";
	private static final String SUMMARY = "Read-only Ironman bank blueprint planner";
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
			+ "each side is then arranged on its own. Put Part Doses on the tab with "
			+ "Potions and the Herblore tab runs by kind instead of a row per recipe.";
	private static final String PREVIEW_OVERLAY_NOTE =
		"Guides tab creation and item order in the vanilla All items view, in Swap or Insert mode; "
			+ "Insert usually needs fewer drags. Every move stays manual.";
	private static final int STATUS_REFRESH_MILLIS = 100;
	private static final int DESTINATION_COLUMNS = 5;
	private static final int DESTINATION_GAP = 2;
	private static final int DESTINATION_CELL = 36;
	private static final int BANK_GRID_COLUMNS = 8;
	private static final int CELL_WIDTH = 36;
	private static final int CELL_HEIGHT = 32;
	private static final int CELL_GAP = 2;
	private static final int DIALOG_WIDTH = 560;
	private static final int DIALOG_HEIGHT = 640;
	private static final Color BANK_BG = new Color(38, 38, 38);
	private static final Color BANK_PANEL = new Color(31, 31, 31);
	private static final Color SLOT_BORDER = new Color(78, 78, 78);
	private static final Color BLANK_SLOT_BORDER = new Color(52, 52, 52);
	private static final Color DESTINATION_CELL_BG = new Color(35, 35, 35);
	private static final Color DESTINATION_EMPTY_BG = new Color(26, 26, 26);
	// PluginPanel is ~225px wide; leave room for panel padding, card padding
	// and the scrollbar so wrapped text never clips at the right edge.
	private static final int SIDEBAR_TEXT_WIDTH = 160;
	// The layout editor sits inside the same 225px panel, so its rows are pinned
	// to one width; anything wider pushes the whole column off the right edge.
	private static final int SIDEBAR_CONTENT_WIDTH = 186;
	private static final int LAYOUT_NAME_WIDTH = 110;
	/** Shown in the profile list while the working layout matches no saved one. */
	private static final String UNSAVED_PROFILE = "Custom (unsaved)";

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
	private final JComboBox<String> profileChooser;
	private final JCheckBox alchPileBox;
	private boolean refreshingProfiles;
	private boolean refreshingOptions;
	private final JPanel layoutEditor;
	private final JPanel layoutRows;
	private final JLabel layoutHeading;
	private final BankLayoutModel bankLayoutModel;
	private BankLayoutPlan layoutPlan;
	private int selectedDestination = BankLayoutPlan.MAIN_DESTINATION_INDEX;
	private final JLabel categoryOverrideLabel;
	private final JLabel statusLabel;
	private final JLabel catalogSummaryLabel;
	private final JLabel organizationPreviewLabel;
	private final JLabel guideProgressLabel;
	private final JLabel nextMoveLabel;
	private final JLabel detailsChevron;
	private final ProgressBar guideProgressBar;
	private final JPanel detailsPanel;
	private final List<DestinationCell> destinationCells = new ArrayList<>();
	private final Timer statusTimer;
	private BankOrganizationPreview renderedOrganizationPreview;
	private BankOrganizationPreview renderedDestinations;
	private BankOrganizationPreview renderedLayoutPreview;
	private JDialog bankDialog;
	private JTabbedPane bankTabs;
	private JButton exportBlueprintButton;

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
		// A fixed preferred width keeps the action row inside 225px; BorderLayout
		// stretches the button to whatever space the icons leave over.
		analyzeButton.setPreferredSize(new Dimension(100, 26));
		analyzeButton.addActionListener(event -> {
			analyzeCallback.run();
			refreshAnalysis();
		});

		showBankButton = iconButton(blueprintIcon(), "Open the blueprint");
		showBankButton.addActionListener(event -> showBankDialog());

		toggleButton = iconButton(guideIcon(), "");
		toggleButton.addActionListener(event -> onToggleGuide());

		assignCategoriesButton = iconButton(assignIcon(), "");
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
			"<html>Move gear you own two strictly better versions of, and that is worth "
				+ "alching, to the Slayer &amp; Boss Loot tab.<br>Off: every piece of gear "
				+ "stays in the combat gear tab.</html>");

		profileChooser = new JComboBox<>();
		profileChooser.setFont(FontManager.getRunescapeSmallFont());
		profileChooser.setFocusable(false);
		profileChooser.setToolTipText("The saved layout you are using");
		profileChooser.addActionListener(event -> onProfileChosen());
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
		JLabel layoutHelp = mutedLabel(LAYOUT_EDITOR_HELP);
		layoutHelp.setAlignmentX(LEFT_ALIGNMENT);
		layoutRows.setAlignmentX(LEFT_ALIGNMENT);
		sizeToSidebar(profileChooser, 22);
		sizeToSidebar(profileActions, 22);
		layoutEditor.add(profileChooser);
		layoutEditor.add(Box.createVerticalStrut(3));
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
		detailsPanel.add(card(null, catalogSummaryLabel));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(null, organizationPreviewLabel));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(null, statusLabel, guideProgressLabel,
			mutedLabel(PREVIEW_BLOCK_HELP), mutedLabel(PREVIEW_OVERLAY_NOTE)));
		detailsPanel.add(Box.createVerticalStrut(8));
		detailsPanel.add(card(null, categoryOverrideLabel, resetOverridesButton,
			mutedLabel(CATEGORY_CORRECTION_HELP)));

		JPanel content = new JPanel(new GridBagLayout());
		content.setOpaque(false);
		GridBagConstraints cardConstraints = new GridBagConstraints();
		cardConstraints.gridx = 0;
		cardConstraints.gridy = 0;
		cardConstraints.weightx = 1;
		cardConstraints.fill = GridBagConstraints.HORIZONTAL;
		cardConstraints.insets = new Insets(0, 0, 8, 0);
		content.add(destinationGrid(), cardConstraints);
		cardConstraints.gridy++;
		content.add(tabOrderButton, cardConstraints);
		cardConstraints.gridy++;
		content.add(layoutEditor, cardConstraints);
		cardConstraints.gridy++;
		content.add(guideProgressBar, cardConstraints);
		cardConstraints.gridy++;
		content.add(actionRow(), cardConstraints);
		cardConstraints.gridy++;
		content.add(nextMoveLabel, cardConstraints);
		cardConstraints.gridy++;
		content.add(detailsHeader(), cardConstraints);
		cardConstraints.gridy++;
		content.add(detailsPanel, cardConstraints);
		cardConstraints.gridy++;
		cardConstraints.weighty = 1;
		cardConstraints.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		content.add(filler, cardConstraints);

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

		JLabel mark = new JLabel(new ImageIcon(IronmanBankArchitectPlugin.createIcon()));
		mark.setVerticalAlignment(SwingConstants.CENTER);

		JPanel who = verticalPanel();
		JLabel title = new JLabel(TITLE);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		title.setAlignmentX(LEFT_ALIGNMENT);
		JLabel profile = new JLabel(AllRoundIronmanPreset.PROFILE_NAME);
		profile.setFont(FontManager.getRunescapeSmallFont());
		profile.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		profile.setAlignmentX(LEFT_ALIGNMENT);
		who.add(title);
		who.add(profile);

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
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setOpaque(false);

		JPanel switches = new JPanel(new GridLayout(1, 0, 4, 0));
		switches.setOpaque(false);
		switches.add(showBankButton);
		switches.add(toggleButton);
		switches.add(assignCategoriesButton);

		row.add(analyzeButton, BorderLayout.CENTER);
		row.add(switches, BorderLayout.EAST);
		return row;
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

	private JButton iconButton(BufferedImage icon, String tooltip)
	{
		JButton button = new JButton(new ImageIcon(icon));
		button.setPreferredSize(new Dimension(26, 26));
		button.setToolTipText(tooltip);
		button.setFocusPainted(false);
		return button;
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
		applyLayoutPlan(layoutPlan.withTagAt(tagKey, destinationIndex));
	}

	JComboBox<String> getProfileChooser()
	{
		return profileChooser;
	}

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
		applyLayoutPlan(layoutPlan.withTagShifted(tagKey, offset));
	}

	private void applyLayoutPlan(BankLayoutPlan updated)
	{
		BankLayoutPlan completed = updated.completedFor(bankLayoutModel.preset());
		if (completed.getDestinations().equals(layoutPlan.getDestinations()))
		{
			return;
		}

		layoutPlan = completed;
		bankLayoutModel.save(layoutPlan);
		renderLayoutEditor();
	}

	private void renderLayoutEditor()
	{
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
			BorderFactory.createEmptyBorder(2, 4, 2, 2)));
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
		buttons.add(layoutButton("▲", "Move earlier on this tab", position > 0,
			event -> shiftWithinSelected(key, -1)));
		buttons.add(layoutButton("▼", "Move later on this tab",
			position >= 0 && position < onDestination.size() - 1,
			event -> shiftWithinSelected(key, 1)));
		buttons.add(layoutButton("⃠", isFallback
				? "This tag catches everything left over, so it always has a tab"
				: "Take " + tag.getName() + " off this tab and send it to the "
					+ fallbackName() + " tab",
			!isFallback, event -> removeFromTab(key)));

		row.add(name, BorderLayout.CENTER);
		row.add(buttons, BorderLayout.EAST);
		return row;
	}

	/**
	 * Pins a row to the sidebar's width. A vertical BoxLayout hands a component
	 * its preferred width and centres whatever is narrower, so one item that
	 * wants to be wide pushes the whole column out of view; fixing the width
	 * keeps every row starting at the same left edge.
	 */
	private void sizeToSidebar(JComponent component, int height)
	{
		Dimension size = new Dimension(SIDEBAR_CONTENT_WIDTH, height);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		component.setMinimumSize(size);
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
				bankLayoutModel.saveOptions(
					new BankLayoutOptions(true, true, alchPileBox.isSelected()));
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
			}
		});
		return chooser;
	}

	private void styleChooser(JComboBox<?> chooser)
	{
		chooser.setFont(FontManager.getRunescapeSmallFont());
		chooser.setFocusable(false);
		sizeToSidebar(chooser, 20);
	}

	private BankLayoutOptions withGearLayout(BankLayoutOptions base, GearLayout gearLayout)
	{
		return new BankLayoutOptions(base.fillGearRows(), base.fillHerbloreRows(), base.alchPile(),
			ordersOf(base), gearLayout, base.potionDoses(), base.runeOrder(), base.teleportOrder());
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
			orders, base.gearLayout(), base.potionDoses(), base.runeOrder(), base.teleportOrder());
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

	/**
	 * The saved layouts, with the working one marked when it matches none of
	 * them. Showing "unsaved" rather than leaving a profile selected keeps the
	 * dropdown honest: an edited layout is no longer the layout it was loaded
	 * from, and pretending otherwise would lose the player's edits on a switch.
	 */
	private void refreshProfiles()
	{
		refreshingProfiles = true;
		try
		{
			String matching = bankLayoutModel.matchingProfile();
			profileChooser.removeAllItems();
			if (matching.isEmpty())
			{
				profileChooser.addItem(UNSAVED_PROFILE);
			}
			for (String name : bankLayoutModel.profileNames())
			{
				profileChooser.addItem(name);
			}
			profileChooser.setSelectedItem(matching.isEmpty() ? UNSAVED_PROFILE : matching);
			profileChooser.setToolTipText(matching.isEmpty()
				? "This layout has not been saved under a name"
				: "Using the saved layout \"" + matching + "\"");
			deleteProfileButton.setEnabled(!matching.isEmpty()
				&& !BankLayoutProfiles.DEFAULT_NAME.equals(matching));
		}
		finally
		{
			refreshingProfiles = false;
		}
	}

	private void onProfileChosen()
	{
		Object selected = profileChooser.getSelectedItem();
		if (refreshingProfiles || selected == null || UNSAVED_PROFILE.equals(selected))
		{
			return;
		}

		bankLayoutModel.selectProfile(selected.toString());
		layoutPlan = bankLayoutModel.plan().completedFor(bankLayoutModel.preset());
		renderLayoutEditor();
	}

	private void saveLayoutAs()
	{
		String suggested = bankLayoutModel.matchingProfile();
		String name = JOptionPane.showInputDialog(this, "Name for this layout:",
			suggested.isEmpty() || BankLayoutProfiles.DEFAULT_NAME.equals(suggested)
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
			matching.isEmpty() ? "Shared layout" : matching, layoutPlan);
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
		if (matching.isEmpty() || BankLayoutProfiles.DEFAULT_NAME.equals(matching))
		{
			return;
		}

		bankLayoutModel.deleteProfile(matching);
		renderLayoutEditor();
	}

	/** A name no saved layout is using yet. */
	private String freeProfileName(String wanted)
	{
		List<String> taken = bankLayoutModel.profileNames();
		if (!taken.contains(wanted))
		{
			return wanted;
		}

		for (int suffix = 2; suffix < taken.size() + 3; suffix++)
		{
			String candidate = wanted + " " + suffix;
			if (!taken.contains(candidate))
			{
				return candidate;
			}
		}

		return wanted + " copy";
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
		button.setFont(FontManager.getRunescapeSmallFont());
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
		return active
			? BorderFactory.createLineBorder(ColorScheme.BRAND_ORANGE)
			: BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR);
	}

	private void refreshStatus()
	{
		categoryOverrideLabel.setText(sidebarHtml(guideController.getCategoryOverrideText()));
		resetOverridesButton.setEnabled(guideController.getCategoryOverrideCount() > 0);
		statusLabel.setText(sidebarHtml(guideController.getStatusText()));
		String guideText = guideController.getGuideProgressText();
		guideProgressLabel.setText(sidebarHtml(guideText));
		nextMoveLabel.setText(sidebarHtml(firstSentence(guideText)));
		nextMoveLabel.setToolTipText(sidebarHtml(guideText));
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
		BankAnalysisStatus analysis = guideController.bankAnalysisStatus();
		catalogSummaryLabel.setText(sidebarHtml(analysis.catalogSummaryText()));
		BankOrganizationPreview preview = analysis.organizationPreview().orElse(null);
		organizationPreviewLabel.setText(sidebarHtml(blueprintStatusText(analysis)));
		showBankButton.setEnabled(preview != null);
		refreshDestinations(preview);
		if (layoutEditor.isVisible() && preview != renderedLayoutPreview)
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

		private DestinationCell(int categoryIndex)
		{
			this.categoryIndex = categoryIndex;
			setLayout(new BorderLayout());
			setBackground(DESTINATION_EMPTY_BG);
			setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
				CategoryPalette.colorFor(categoryIndex)));
			setPreferredSize(new Dimension(DESTINATION_CELL, DESTINATION_CELL));
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
			if (count.isEmpty())
			{
				return;
			}
			drawTag(graphics, count, getWidth() - metrics.stringWidth(count) - 2,
				getHeight() - 5, Color.WHITE);
		}

		private void drawTag(Graphics graphics, String text, int x, int y, Color color)
		{
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);
		}
	}

	private static BufferedImage blueprintIcon()
	{
		return drawIcon(graphics -> {
			graphics.setColor(ColorScheme.TEXT_COLOR);
			graphics.drawRect(1, 2, 11, 9);
			graphics.drawLine(1, 5, 12, 5);
			graphics.drawLine(5, 5, 5, 11);
		});
	}

	private static BufferedImage guideIcon()
	{
		return drawIcon(graphics -> {
			graphics.setColor(ColorScheme.TEXT_COLOR);
			graphics.drawOval(1, 1, 11, 11);
			graphics.fillOval(5, 5, 4, 4);
		});
	}

	private static BufferedImage assignIcon()
	{
		return drawIcon(graphics -> {
			graphics.setColor(ColorScheme.TEXT_COLOR);
			graphics.drawLine(1, 3, 12, 3);
			graphics.drawLine(1, 7, 12, 7);
			graphics.drawLine(1, 11, 6, 11);
			graphics.drawOval(8, 8, 5, 5);
		});
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
		if (bankTabs != null && categoryIndex >= 0 && categoryIndex < bankTabs.getTabCount())
		{
			bankTabs.setSelectedIndex(categoryIndex);
		}
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
			bankTabs = new JTabbedPane();
			bankTabs.setTabPlacement(JTabbedPane.LEFT);
			bankTabs.setBackground(BANK_BG);
			bankTabs.setForeground(Color.WHITE);
			exportBlueprintButton = new JButton("Copy Blueprint Export");
			exportBlueprintButton.addActionListener(event -> copyBlueprintExport());
			bankDialog.add(bankTabs, BorderLayout.CENTER);
			bankDialog.add(exportBlueprintButton, BorderLayout.SOUTH);
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
		if (bankTabs == null || preview == renderedOrganizationPreview)
		{
			return;
		}

		renderedOrganizationPreview = preview;
		bankTabs.removeAll();
		if (preview == null)
		{
			return;
		}

		int tabNumber = 1;
		for (BankCategoryPreview category : preview.getCategories())
		{
			String placement = tabNumber == 1 ? "MAIN" : "TAB " + tabNumber;
			String title = placement + "  " + shortCategoryName(category)
				+ "  " + category.getItemCount();
			bankTabs.addTab(title, categoryScrollPane(category));
			tabNumber++;
		}
	}

	private JScrollPane categoryScrollPane(BankCategoryPreview category)
	{
		JPanel wrapper = verticalPanel();
		wrapper.setBackground(BANK_BG);
		wrapper.setOpaque(true);
		wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		wrapper.add(label(category.getCategory().getName() + " - " + category.getItemCount() + " planned item IDs"));
		wrapper.add(Box.createVerticalStrut(8));
		wrapper.add(categoryContent(category));

		JScrollPane scrollPane = new JScrollPane(wrapper);
		scrollPane.getViewport().setBackground(BANK_BG);
		scrollPane.setBackground(BANK_BG);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	private JPanel categoryContent(BankCategoryPreview category)
	{
		if (!"storage-cleanup".equals(category.getCategory().getKey()))
		{
			return categoryGrid(category.getItems());
		}

		JPanel content = verticalPanel();
		content.setBackground(BANK_BG);
		content.setOpaque(true);
		Map<String, List<BankPreviewItem>> itemsByLane = new LinkedHashMap<>();
		for (BankPreviewItem item : category.getItems())
		{
			String label = PresetItemSorter.subgroupLabel(category.getCategory(), item);
			itemsByLane.computeIfAbsent(label, ignored -> new ArrayList<>()).add(item);
		}

		if (itemsByLane.isEmpty())
		{
			content.add(categoryGrid(category.getItems()));
			return content;
		}

		for (Map.Entry<String, List<BankPreviewItem>> entry : itemsByLane.entrySet())
		{
			content.add(label(entry.getKey() + " - " + entry.getValue().size()));
			content.add(Box.createVerticalStrut(4));
			content.add(categoryGrid(entry.getValue()));
			content.add(Box.createVerticalStrut(10));
		}

		return content;
	}

	private JPanel categoryGrid(List<BankPreviewItem> items)
	{
		if (items.isEmpty())
		{
			return emptyCategoryPanel();
		}

		JPanel grid = new JPanel(new GridBagLayout());
		grid.setBackground(BANK_BG);
		grid.setOpaque(true);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.insets = new Insets(0, 0, CELL_GAP, CELL_GAP);
		for (int i = 0; i < items.size(); i++)
		{
			constraints.gridx = i % BANK_GRID_COLUMNS;
			constraints.gridy = i / BANK_GRID_COLUMNS;
			grid.add(itemCell(items.get(i)), constraints);
		}

		return grid;
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
		label.setToolTipText(item.toCompactLabel());
		label.setBorder(BorderFactory.createLineBorder(SLOT_BORDER));
		itemIconRenderer.accept(item, label);
		return label;
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

	private static String shortCategoryName(BankCategoryPreview category)
	{
		String key = category.getCategory().getKey();
		if ("currency-utilities".equals(key))
		{
			return "Main";
		}
		if ("combat-gear".equals(key))
		{
			return "Gear";
		}
		if ("potions-food".equals(key))
		{
			return "Supplies";
		}
		if ("herblore".equals(key))
		{
			return "Herblore";
		}
		if ("seeds-farming".equals(key))
		{
			return "Farming";
		}
		if ("skilling-tools".equals(key))
		{
			return "Tools";
		}
		if ("resources".equals(key))
		{
			return "Resources";
		}
		if ("slayer-boss-loot".equals(key))
		{
			return "Boss Loot";
		}
		if ("clues-cosmetics".equals(key))
		{
			return "Clues";
		}
		if ("storage-cleanup".equals(key))
		{
			return "Review";
		}

		return category.getCategory().getName();
	}

	private static JPanel card(String title, Component... rows)
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
		if (title != null)
		{
			card.add(headerLabel(title), constraints);
			constraints.gridy++;
		}
		for (Component row : rows)
		{
			card.add(row, constraints);
			constraints.gridy++;
		}
		return card;
	}

	private static JLabel headerLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeBoldFont());
		label.setForeground(ColorScheme.BRAND_ORANGE);
		return label;
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

	private static JPanel emptyCategoryPanel()
	{
		JPanel panel = verticalPanel();
		panel.setBackground(BANK_BG);
		panel.setOpaque(true);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label = label("No owned items");
		label.setForeground(new Color(140, 140, 140));
		panel.add(label);
		return panel;
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

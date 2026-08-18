package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankBlueprintTextExporter;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.CategoryIcons;
import com.pkoka5.ironmanbankarchitect.organize.CategoryPalette;
import com.pkoka5.ironmanbankarchitect.organize.PresetItemSorter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

	private final BankGuideController guideController;
	private final BiConsumer<BankPreviewItem, JLabel> itemIconRenderer;
	private final JButton toggleButton;
	private final JButton analyzeButton;
	private final JButton showBankButton;
	private final JButton assignCategoriesButton;
	private final JButton resetOverridesButton;
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
		this.guideController = guideController;
		this.itemIconRenderer = itemIconRenderer;

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

		statusTimer = new Timer(STATUS_REFRESH_MILLIS, event -> refreshStatus());
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
		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
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
		catalogSummaryLabel.setText(sidebarHtml(guideController.getCatalogSummaryText()));
		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
		organizationPreviewLabel.setText(sidebarHtml(blueprintStatusText(preview)));
		showBankButton.setEnabled(preview != null);
		refreshDestinations(preview);
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
					showBankDialog(DestinationCell.this.categoryIndex);
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
			BankPreviewItem icon = iconItem(category);
			if (icon != null)
			{
				itemIconRenderer.accept(icon, sprite);
			}
			setToolTipText(category == null ? null
				: category.getCategory().getName()
					+ (filled ? " - " + category.getItemCount() + " items" : " - empty"));
			repaint();
		}

		@Override
		public void paint(Graphics graphics)
		{
			super.paint(graphics);
			if (count.isEmpty())
			{
				return;
			}
			// Painted after the sprite so the count always stays readable.
			graphics.setFont(FontManager.getRunescapeSmallFont());
			FontMetrics metrics = graphics.getFontMetrics();
			int x = getWidth() - metrics.stringWidth(count) - 2;
			int y = getHeight() - 5;
			graphics.setColor(Color.BLACK);
			graphics.drawString(count, x + 1, y + 1);
			graphics.setColor(Color.WHITE);
			graphics.drawString(count, x, y);
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
		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
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
		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
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

	private static String blueprintStatusText(BankOrganizationPreview preview)
	{
		if (preview == null)
		{
			return "Analyze your bank, then open the blueprint.";
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

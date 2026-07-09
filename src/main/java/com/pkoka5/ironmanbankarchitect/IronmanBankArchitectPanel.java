package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import net.runelite.client.ui.PluginPanel;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Ironman Bank Architect";
	private static final String SUMMARY = "Read-only whole-bank organization planner";
	private static final String SAFETY_NOTE = "No bank actions are automated.";
	private static final String MAIN_ACTION_LABEL = "Main action";
	private static final String WHOLE_BANK_SCAN_LABEL = "Whole Bank Scan";
	private static final String BLUEPRINT_ACTION_LABEL = "Blueprint";
	private static final String PREVIEW_BLOCK_HELP =
		"Bank guide preview is temporary while whole-bank planning is being built.";
	private static final String PREVIEW_OVERLAY_NOTE =
		"Overlay preview compares physical bank slots with the sorted blueprint.";
	private static final int STATUS_REFRESH_MILLIS = 500;
	private static final int BANK_GRID_COLUMNS = 8;
	private static final int CELL_WIDTH = 40;
	private static final int CELL_HEIGHT = 34;
	private static final int DIALOG_WIDTH = 560;
	private static final int DIALOG_HEIGHT = 640;
	private static final Color BANK_BG = new Color(38, 38, 38);
	private static final Color BANK_PANEL = new Color(31, 31, 31);
	private static final Color SLOT_BORDER = new Color(78, 78, 78);

	private final BankGuideController guideController;
	private final BiConsumer<BankPreviewItem, JLabel> itemIconRenderer;
	private final JButton toggleButton;
	private final JButton analyzeButton;
	private final JButton showBankButton;
	private final JLabel statusLabel;
	private final JLabel catalogSummaryLabel;
	private final JLabel organizationPreviewLabel;
	private final Timer statusTimer;
	private BankOrganizationPreview renderedOrganizationPreview;
	private JDialog bankDialog;
	private JTabbedPane bankTabs;

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
		this.guideController = guideController;
		this.itemIconRenderer = itemIconRenderer;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

		JPanel identity = new JPanel(new GridLayout(0, 1, 0, 8));
		identity.setOpaque(false);
		identity.add(label(TITLE));
		identity.add(label(SUMMARY));
		identity.add(label(SAFETY_NOTE));

		JPanel controls = verticalPanel();
		controls.add(Box.createVerticalStrut(12));
		controls.add(label(profileLine()));

		toggleButton = new JButton();
		toggleButton.addActionListener(event -> onToggleGuide());

		analyzeButton = new JButton("Analyze My Bank");
		analyzeButton.addActionListener(event -> {
			analyzeCallback.run();
			refreshAnalysis();
		});

		showBankButton = new JButton("Show My Bank");
		showBankButton.addActionListener(event -> showBankDialog());

		statusLabel = label("");
		catalogSummaryLabel = label("");
		organizationPreviewLabel = label("");

		controls.add(Box.createVerticalStrut(4));
		controls.add(label(MAIN_ACTION_LABEL));
		controls.add(analyzeButton);
		controls.add(Box.createVerticalStrut(4));
		controls.add(label(BLUEPRINT_ACTION_LABEL));
		controls.add(showBankButton);
		controls.add(Box.createVerticalStrut(8));
		controls.add(label(WHOLE_BANK_SCAN_LABEL));
		controls.add(catalogSummaryLabel);
		controls.add(Box.createVerticalStrut(8));
		controls.add(organizationPreviewLabel);
		controls.add(Box.createVerticalStrut(12));
		controls.add(label(PREVIEW_BLOCK_HELP));
		controls.add(Box.createVerticalStrut(4));
		controls.add(label(PREVIEW_OVERLAY_NOTE));
		controls.add(Box.createVerticalStrut(8));
		controls.add(toggleButton);
		controls.add(Box.createVerticalStrut(8));
		controls.add(statusLabel);

		JScrollPane scrollPane = new JScrollPane(controls);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(identity, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		refreshControls();

		statusTimer = new Timer(STATUS_REFRESH_MILLIS, event -> refreshStatus());
		statusTimer.start();
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

	private void onToggleGuide()
	{
		guideController.toggleGuide();
		refreshControls();
	}

	private void refreshControls()
	{
		toggleButton.setText(guideController.isGuideEnabled() ? "Hide Bank Guide" : "Show Bank Guide");
		refreshStatus();
	}

	private void refreshStatus()
	{
		statusLabel.setText(guideController.getStatusText());
		refreshAnalysis();
	}

	private void refreshAnalysis()
	{
		catalogSummaryLabel.setText(toHtmlLines(guideController.getCatalogSummaryText()));
		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
		organizationPreviewLabel.setText(toHtmlLines(blueprintStatusText(preview)));
		showBankButton.setEnabled(preview != null);
		if (bankDialog != null && bankDialog.isVisible())
		{
			refreshBankDialog(preview);
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
			bankDialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Ironman Bank Blueprint", false);
			bankDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
			bankDialog.setLocationRelativeTo(this);
			bankTabs = new JTabbedPane();
			bankTabs.setTabPlacement(JTabbedPane.LEFT);
			bankTabs.setBackground(BANK_BG);
			bankTabs.setForeground(Color.WHITE);
			bankDialog.add(bankTabs, BorderLayout.CENTER);
		}

		refreshBankDialog(preview);
		bankDialog.setVisible(true);
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
			String title = tabNumber + "  " + shortCategoryName(category) + "  " + category.getItemCount();
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
		wrapper.add(label(category.getCategory().getName() + " - " + category.getItemCount() + " owned item IDs"));
		wrapper.add(Box.createVerticalStrut(8));
		wrapper.add(categoryGrid(category));

		JScrollPane scrollPane = new JScrollPane(wrapper);
		scrollPane.getViewport().setBackground(BANK_BG);
		scrollPane.setBackground(BANK_BG);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	private JPanel categoryGrid(BankCategoryPreview category)
	{
		JPanel grid = new JPanel(new GridLayout(0, BANK_GRID_COLUMNS, 3, 3));
		grid.setBackground(BANK_BG);
		grid.setOpaque(true);
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (BankPreviewItem item : category.getItems())
		{
			grid.add(itemCell(item));
		}

		if (category.getItems().isEmpty())
		{
			grid.add(emptyCell());
		}

		return grid;
	}

	private JLabel itemCell(BankPreviewItem item)
	{
		JLabel label = new JLabel("", SwingConstants.CENTER);
		label.setText(Integer.toString(item.getItemId()));
		label.setToolTipText(item.toCompactLabel());
		label.setForeground(Color.WHITE);
		label.setOpaque(true);
		label.setBackground(BANK_PANEL);
		label.setBorder(BorderFactory.createLineBorder(SLOT_BORDER));
		label.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
		itemIconRenderer.accept(item, label);
		return label;
	}

	private static String blueprintStatusText(BankOrganizationPreview preview)
	{
		if (preview == null)
		{
			return "Analyze your bank, then open the blueprint.";
		}

		return "Blueprint ready: " + preview.getPlannedItems().size() + " item IDs sorted.";
	}

	private static String shortCategoryName(BankCategoryPreview category)
	{
		String key = category.getCategory().getKey();
		if ("currency-utilities".equals(key))
		{
			return "Currency";
		}
		if ("teleports-runes".equals(key))
		{
			return "Teleports";
		}
		if ("combat-gear".equals(key))
		{
			return "Gear";
		}
		if ("potions-food".equals(key))
		{
			return "Supplies";
		}
		if ("farming-herblore".equals(key))
		{
			return "Herblore";
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

	private String profileLine()
	{
		return "Profile: " + AllRoundIronmanPreset.PROFILE_NAME;
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

	private static JLabel emptyCell()
	{
		JLabel label = new JLabel("-", SwingConstants.CENTER);
		label.setForeground(new Color(140, 140, 140));
		label.setOpaque(true);
		label.setBackground(BANK_PANEL);
		label.setBorder(BorderFactory.createLineBorder(SLOT_BORDER));
		label.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
		return label;
	}

	private static String toHtmlLines(String text)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}

		return "<html>" + text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\n", "<br>") + "</html>";
	}
}

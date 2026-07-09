package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import net.runelite.client.ui.PluginPanel;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Ironman Bank Architect";
	private static final String SUMMARY = "Read-only whole-bank organization planner";
	private static final String SAFETY_NOTE = "No bank actions are automated.";
	private static final String MAIN_ACTION_LABEL = "Main action";
	private static final String WHOLE_BANK_SCAN_LABEL = "Whole Bank Scan";
	private static final String SUGGESTED_BLUEPRINT_LABEL = "Suggested Bank Blueprint";
	private static final String PREVIEW_BLOCK_HELP =
		"Bank guide preview is temporary while whole-bank planning is being built.";
	private static final String PREVIEW_OVERLAY_NOTE =
		"Overlay preview is neutral: it shows suggested row positions only, not correct or missing slots.";
	private static final int STATUS_REFRESH_MILLIS = 500;

	private final BankGuideController guideController;
	private final JButton toggleButton;
	private final JButton analyzeButton;
	private final JLabel statusLabel;
	private final JLabel catalogSummaryLabel;
	private final JLabel organizationPreviewLabel;
	private final Timer statusTimer;

	IronmanBankArchitectPanel(BankGuideController guideController)
	{
		this(guideController, () -> {});
	}

	IronmanBankArchitectPanel(BankGuideController guideController, Runnable analyzeCallback)
	{
		this.guideController = guideController;

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

		statusLabel = label("");
		catalogSummaryLabel = label("");
		organizationPreviewLabel = label("");

		controls.add(Box.createVerticalStrut(4));
		controls.add(label(MAIN_ACTION_LABEL));
		controls.add(analyzeButton);
		controls.add(Box.createVerticalStrut(8));
		controls.add(label(WHOLE_BANK_SCAN_LABEL));
		controls.add(catalogSummaryLabel);
		controls.add(Box.createVerticalStrut(8));
		controls.add(label(SUGGESTED_BLUEPRINT_LABEL));
		controls.add(organizationPreviewLabel);
		controls.add(Box.createVerticalStrut(12));
		controls.add(label(PREVIEW_BLOCK_HELP));
		controls.add(Box.createVerticalStrut(4));
		controls.add(label(PREVIEW_OVERLAY_NOTE));
		controls.add(Box.createVerticalStrut(8));
		controls.add(toggleButton);
		controls.add(Box.createVerticalStrut(8));
		controls.add(statusLabel);

		add(identity, BorderLayout.NORTH);
		add(controls, BorderLayout.CENTER);

		refreshControls();

		statusTimer = new Timer(STATUS_REFRESH_MILLIS, event -> refreshStatus());
		statusTimer.start();
	}

	void shutdown()
	{
		statusTimer.stop();
	}

	JButton getAnalyzeButton()
	{
		return analyzeButton;
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
		organizationPreviewLabel.setText(toHtmlLines(guideController.getOrganizationPreviewText()));
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
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
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

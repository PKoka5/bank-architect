package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import net.runelite.client.ui.PluginPanel;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Ironman Bank Architect";
	private static final String SUMMARY = "Standalone bank blueprint planner";
	private static final String SAFETY_NOTE = "No bank actions are automated.";
	private static final int STATUS_REFRESH_MILLIS = 500;

	private final BankGuideController guideController;
	private final JComboBox<String> blockDropdown;
	private final JButton toggleButton;
	private final JLabel statusLabel;
	private final Timer statusTimer;

	IronmanBankArchitectPanel(BankGuideController guideController)
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

		List<VisualBlock> availableBlocks = guideController.getAvailableBlocks();
		String[] blockNames = new String[availableBlocks.size()];
		int selectedIndex = 0;
		for (int i = 0; i < availableBlocks.size(); i++)
		{
			blockNames[i] = availableBlocks.get(i).getName();
			if (availableBlocks.get(i).getKey().equals(guideController.getSelectedBlock().getKey()))
			{
				selectedIndex = i;
			}
		}

		blockDropdown = new JComboBox<>(blockNames);
		blockDropdown.setSelectedIndex(selectedIndex);
		blockDropdown.addActionListener(event -> onBlockSelected());

		toggleButton = new JButton();
		toggleButton.addActionListener(event -> onToggleGuide());

		statusLabel = label("");

		controls.add(Box.createVerticalStrut(4));
		controls.add(blockDropdown);
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

	private void onBlockSelected()
	{
		List<VisualBlock> availableBlocks = guideController.getAvailableBlocks();
		int index = blockDropdown.getSelectedIndex();
		if (index >= 0 && index < availableBlocks.size())
		{
			guideController.selectBlock(availableBlocks.get(index).getKey());
		}

		refreshStatus();
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
}

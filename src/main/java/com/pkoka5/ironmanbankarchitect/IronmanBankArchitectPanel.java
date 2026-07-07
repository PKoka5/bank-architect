package com.pkoka5.ironmanbankarchitect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.PluginPanel;

final class IronmanBankArchitectPanel extends PluginPanel
{
	private static final String TITLE = "Ironman Bank Architect";
	private static final String SUMMARY = "Standalone bank blueprint planner";
	private static final String SAFETY_NOTE = "No bank actions are automated.";

	IronmanBankArchitectPanel()
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

		JPanel content = new JPanel(new GridLayout(0, 1, 0, 8));
		content.setOpaque(false);
		content.add(label(TITLE));
		content.add(label(SUMMARY));
		content.add(label(SAFETY_NOTE));

		add(content, BorderLayout.NORTH);
	}

	private static JLabel label(String text)
	{
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setForeground(Color.WHITE);
		return label;
	}
}

package com.pkoka5.ironmanbankarchitect;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.function.*;
import javax.swing.*;






/** Bundled release notes, acknowledged locally only when the player dismisses them. */
final class ReleaseNoticePanel extends JPanel
{
	// Update both this ID and the notes when preparing a player-facing release.
	static final String RELEASE_ID = "0.9.0";
	private final CardLayout cards = new CardLayout();
	private final Supplier<String> lastSeen;
	private final Consumer<String> acknowledge;
	private final String releaseId;

	ReleaseNoticePanel(JComponent normal, Supplier<String> lastSeen, Consumer<String> acknowledge)
	{
		this(normal, lastSeen, acknowledge, RELEASE_ID);
	}

	ReleaseNoticePanel(JComponent normal, Supplier<String> lastSeen, Consumer<String> acknowledge, String releaseId)
	{
		this.lastSeen = lastSeen;
		this.acknowledge = acknowledge;
		this.releaseId = releaseId;
		setLayout(cards);
		setOpaque(false);
		add(normal, "normal");
		JPanel notice = new JPanel(new BorderLayout(0, 12));
		notice.setOpaque(false);
		notice.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
		JLabel notes = new JLabel("<html><div style='width:135px'>"
			+ "<h2>What's new</h2><p>Bank Architect " + releaseId + "</p>"
			+ "<h3>Main and custom presets</h3>"
			+ "<p>Choose Ironman or Main at the top of the sidebar. Main groups runes and teleports, "
			+ "potions from 4 to 1 doses, and herbs by stage. Editing a layout creates a custom preset; "
			+ "the bundled presets keep their defaults.</p>"
			+ "<h3>Smarter Alch sorting</h3>"
			+ "<p>Both presets gather reviewed gear and tools when you own a replacement, plus spare standard "
			+ "Mystic colours. Staffs keep their rune supply. Dragon halberds count as Alch stock. "
			+ "Your own item choices still win.</p>"
			+ "<p>Cannon parts stay together, Spiked boots go to Quest Items, and heraldic Rune helms go to Clues.</p>"
			+ "<p>In Blueprint, Keep current order accepts a tab's live order while guiding transfers.</p>"
			+ "<p>You still move every real bank item yourself.</p></div></html>");
		notes.setForeground(Color.WHITE);
		notes.setVerticalAlignment(JLabel.TOP);
		JScrollPane scroll = new JScrollPane(notes);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		notice.add(scroll, BorderLayout.CENTER);
		JButton dismiss = new JButton("Got it - continue");
		dismiss.addActionListener(event -> {
			this.acknowledge.accept(this.releaseId);
			cards.show(this, "normal");
		});
		notice.add(dismiss, BorderLayout.SOUTH);
		add(notice, "notice");
		cards.show(this, "normal");
	}

	void opened()
	{
		cards.show(this, releaseId.equals(lastSeen.get()) ? "normal" : "notice");
	}
}

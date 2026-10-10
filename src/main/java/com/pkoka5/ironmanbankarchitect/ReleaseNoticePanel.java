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
	static final String RELEASE_ID = "0.9.3";
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
			+ "<h3>Clearer Combat layouts</h3>"
			+ "<p>Best gear leads each style column. Other sets stay together vertically, with separate weapon roles "
			+ "and Crystal armour beside Bowfa.</p>"
			+ "<h3>Tidier loot</h3>"
			+ "<p>Arrows, bolts, darts and cannonballs group together in Slayer &amp; Boss Loot, followed by Alch items.</p>"
			+ "<p>Newer gear coverage and faster dense Combat previews.</p>"
			+ "<p>Your corrections and saved blueprints take priority. All bank moves remain manual.</p></div></html>");
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

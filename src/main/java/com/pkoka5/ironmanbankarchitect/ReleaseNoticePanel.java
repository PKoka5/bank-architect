package com.pkoka5.ironmanbankarchitect;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Bundled release notes, acknowledged locally only when the player dismisses them. */
final class ReleaseNoticePanel extends JPanel
{
	// Update both this ID and the notes when preparing a player-facing release.
	static final String RELEASE_ID = "0.7.0";
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
			+ "<h3>Clearer bank planning</h3>"
			+ "<p>A simpler sidebar and blueprint editor. Choose an item's category directly in the editor. "
			+ "Your saved placements still come first.</p>"
			+ "<h3>Better item placement</h3>"
			+ "<p>Improved hunter supplies, cosmetics, skilling outfits, quest tools and combat gear.</p>"
			+ "<h3>Know why you keep it</h3>"
			+ "<p>Item tooltips explain known uses and optional storage. Check clue and quest uses before removing items. "
			+ "Items with known uses stay out of the automatic alch selection.</p>"
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

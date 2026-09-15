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
	static final String RELEASE_ID = "0.6.0";
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
			+ "<h3>Your blueprint, your order</h3>"
			+ "<p>Click an item to select it in green, choose Swap or Insert, then click its new slot. "
			+ "Undo, Cancel and Save keep you in control.</p>"
			+ "<h3>Move items between tabs</h3>"
			+ "<p>Drag an item onto a blueprint tab. Your saved layout stays with your profile.</p>"
			+ "<h3>Smoother bank guidance</h3>"
			+ "<p>Better recovery after deposits and withdrawals, placeholder-aware tool selection, "
			+ "and improved Frequently Used placement.</p>"
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

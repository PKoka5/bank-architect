package com.pkoka5.ironmanbankarchitect;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import javax.swing.*;






/** Bundled release notes, acknowledged locally only when the player dismisses them. */
final class ReleaseNoticePanel extends JPanel
{
	// Update both this ID and the notes when preparing a player-facing release.
	static final String RELEASE_ID = "0.9.7";
	private static final Properties NOTES = loadNotes();
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
		notice.add(scroll(notes(NOTES.getProperty("notice").replace("{version}", releaseId))), BorderLayout.CENTER);
		JButton dismiss = new JButton("Got it - continue");
		dismiss.addActionListener(event -> {
			this.acknowledge.accept(this.releaseId);
			cards.show(this, "normal");
		});
		notice.add(dismiss, BorderLayout.SOUTH);
		add(notice, "notice");
		JPanel history = new JPanel(new BorderLayout(0, 8));
		history.setOpaque(false);
		JComboBox<String> versions = new JComboBox<>(NOTES.getProperty("versions").split(","));
		versions.getAccessibleContext().setAccessibleName("Release version");
		versions.setToolTipText("Choose a Bank Architect update");
		JLabel details = notes(NOTES.getProperty(versions.getItemAt(0)));
		JScrollPane historyScroll = scroll(details);
		versions.addActionListener(event -> {
			details.setText(html(NOTES.getProperty((String) versions.getSelectedItem())));
			historyScroll.getVerticalScrollBar().setValue(0);
		});
		history.add(versions, BorderLayout.NORTH);
		history.add(historyScroll, BorderLayout.CENTER);
		JButton back = new JButton("Back to planner");
		back.addActionListener(event -> opened());
		history.add(back, BorderLayout.SOUTH);
		add(history, "history");
		cards.show(this, "normal");
	}

	void showHistory() { cards.show(this, "history"); }

	void opened()
	{
		cards.show(this, releaseId.equals(lastSeen.get()) ? "normal" : "notice");
	}

	private static String html(String body) { return "<html><div style='width:135px'>" + body + "</div></html>"; }

	private static JLabel notes(String body)
	{
		JLabel label = new JLabel(html(body));
		label.setForeground(Color.WHITE);
		label.setVerticalAlignment(JLabel.TOP);
		return label;
	}

	private static JScrollPane scroll(JLabel notes)
	{
		JScrollPane scroll = new JScrollPane(notes);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scroll;
	}

	private static Properties loadNotes()
	{
		Properties notes = new Properties();
		try (Reader reader = new InputStreamReader(Objects.requireNonNull(
			ReleaseNoticePanel.class.getResourceAsStream("release-notes.properties")), StandardCharsets.UTF_8))
		{
			notes.load(reader);
			return notes;
		}
		catch (IOException e) { throw new IllegalStateException("Cannot load bundled update history", e); }
	}
}

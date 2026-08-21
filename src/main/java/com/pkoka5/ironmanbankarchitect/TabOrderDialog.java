package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankTabOrder;
import com.pkoka5.ironmanbankarchitect.organize.CategoryIcons;
import com.pkoka5.ironmanbankarchitect.organize.CategoryPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Lets the player arrange the blueprint tabs.
 *
 * <p>The first destination fills the bank's main section rather than a tab, so
 * it is shown pinned in place; the nine tabs after it move freely. Changing the
 * order re-plans the bank, but nothing is moved for the player: the guide simply
 * starts advising drags towards the new arrangement.</p>
 */
final class TabOrderDialog extends JDialog
{
	private static final String TITLE = "Tab order";
	private static final String HELP =
		"The order of your blueprint tabs. The main section stays first; the tabs "
			+ "after it can go in any order. Changing this re-plans the bank, and "
			+ "every move still happens by hand.";
	private static final int DIALOG_WIDTH = 320;
	private static final int DIALOG_HEIGHT = 460;
	private static final int ROW_HEIGHT = 30;
	private static final int ICON_WIDTH = 34;

	private final TabOrderModel model;
	private final BiConsumer<BankPreviewItem, JLabel> itemIconRenderer;
	private final JPanel rows = new JPanel();
	private final JButton resetButton = new JButton("Reset to default");
	private List<String> keys;
	private List<BankCategory> categories;

	private TabOrderDialog(Window owner, TabOrderModel model,
		BiConsumer<BankPreviewItem, JLabel> itemIconRenderer)
	{
		super(owner, TITLE, ModalityType.MODELESS);
		this.model = model;
		this.itemIconRenderer = itemIconRenderer;
		this.categories = new ArrayList<>(model.categories());
		this.keys = keysOf(categories);

		rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
		rows.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel help = new JLabel("<html><body width='260'>" + HELP + "</body></html>");
		help.setFont(FontManager.getRunescapeSmallFont());
		help.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		help.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

		resetButton.addActionListener(event ->
			apply(new ArrayList<>(BankTabOrder.orderedKeys(BankPresets.IRONMAN, ""))));

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(event -> dispose());

		JPanel footer = new JPanel(new GridLayout(1, 0, 6, 0));
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footer.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
		footer.add(resetButton);
		footer.add(closeButton);

		JScrollPane scroll = new JScrollPane(rows);
		scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.add(help, BorderLayout.NORTH);
		content.add(scroll, BorderLayout.CENTER);
		content.add(footer, BorderLayout.SOUTH);

		setContentPane(content);
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setLocationRelativeTo(owner);
		render();
	}

	static TabOrderDialog show(Component parent, TabOrderModel model,
		BiConsumer<BankPreviewItem, JLabel> itemIconRenderer)
	{
		TabOrderDialog dialog = new TabOrderDialog(SwingUtilities.getWindowAncestor(parent),
			model, itemIconRenderer);
		dialog.setVisible(true);
		return dialog;
	}

	/** Moves the destination at {@code index} by {@code delta}, then saves and redraws. */
	void moveTab(int index, int delta)
	{
		List<String> moved = BankTabOrder.moved(keys, index, delta);
		if (!moved.equals(keys))
		{
			apply(new ArrayList<>(moved));
		}
	}

	private void apply(List<String> newKeys)
	{
		List<BankCategory> reordered = new ArrayList<>(newKeys.size());
		for (String key : newKeys)
		{
			reordered.add(categoryFor(key));
		}

		keys = newKeys;
		categories = reordered;
		model.save(keys);
		render();
	}

	private BankCategory categoryFor(String key)
	{
		for (BankCategory category : categories)
		{
			if (category.getKey().equals(key))
			{
				return category;
			}
		}

		throw new IllegalStateException("Unknown destination key: " + key);
	}

	private void render()
	{
		rows.removeAll();
		for (int index = 0; index < categories.size(); index++)
		{
			rows.add(row(index));
		}
		resetButton.setEnabled(
			!BankTabOrder.isDefault(BankPresets.IRONMAN, BankTabOrder.serialize(keys)));
		rows.revalidate();
		rows.repaint();
	}

	private JPanel row(int index)
	{
		BankCategory category = categories.get(index);
		boolean pinned = index < BankTabOrder.FIRST_MOVABLE_INDEX;

		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 3, 0, 0,
				CategoryPalette.colorForCategory(category.getKey(), index)),
			BorderFactory.createEmptyBorder(3, 6, 3, 3)));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT + 8));

		JLabel icon = new JLabel("", SwingConstants.CENTER);
		icon.setPreferredSize(new Dimension(ICON_WIDTH, ROW_HEIGHT));
		int iconItemId = CategoryIcons.iconItemId(category.getKey());
		if (iconItemId > 0)
		{
			itemIconRenderer.accept(new BankPreviewItem(iconItemId, category.getName(), 1), icon);
		}

		JLabel name = new JLabel("<html><body width='150'>"
			+ (pinned ? "Main section" : index + ". " + category.getName()) + "</body></html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(pinned ? ColorScheme.LIGHT_GRAY_COLOR : Color.WHITE);
		name.setToolTipText(category.getName());

		row.add(icon, BorderLayout.WEST);
		row.add(name, BorderLayout.CENTER);
		row.add(pinned ? pinnedNote() : moveButtons(index), BorderLayout.EAST);
		return row;
	}

	private JLabel pinnedNote()
	{
		JLabel note = new JLabel("fixed");
		note.setFont(FontManager.getRunescapeSmallFont());
		note.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		note.setToolTipText("The first destination fills the bank's main section, not a tab.");
		return note;
	}

	private JPanel moveButtons(int index)
	{
		JPanel buttons = new JPanel(new GridLayout(1, 2, 2, 0));
		buttons.setOpaque(false);
		buttons.add(moveButton("▲", "Move up", index, -1,
			index > BankTabOrder.FIRST_MOVABLE_INDEX));
		buttons.add(moveButton("▼", "Move down", index, 1,
			index < categories.size() - 1));
		return buttons;
	}

	private JButton moveButton(String glyph, String tooltip, int index, int delta, boolean enabled)
	{
		JButton button = new JButton(glyph);
		button.setPreferredSize(new Dimension(24, 22));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusPainted(false);
		button.setToolTipText(tooltip);
		button.setEnabled(enabled);
		button.addActionListener(event -> moveTab(index, delta));
		return button;
	}

	List<String> getKeys()
	{
		return keys;
	}

	JPanel getRows()
	{
		return rows;
	}

	private static List<String> keysOf(List<BankCategory> categories)
	{
		List<String> keys = new ArrayList<>(categories.size());
		for (BankCategory category : categories)
		{
			keys.add(category.getKey());
		}

		return keys;
	}
}

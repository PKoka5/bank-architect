package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;

/** Swing-only blueprint editor. Dragging updates a draft; Save updates local configuration. */
final class BlueprintEditorPanel extends JPanel
{
	private static final Color BACKGROUND = ColorScheme.DARKER_GRAY_COLOR;
	private final BankLayoutModel model;
	private final Function<BankPreviewItem, JLabel> cellRenderer;
	private final Consumer<Boolean> editingChanged;
	private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
	private final JButton edit = new JButton("Edit this tab");
	private final JButton undo = new JButton("Undo");
	private final JButton cancel = new JButton("Cancel");
	private final JButton save = new JButton("Save");
	private final JButton reset = new JButton("Reset tab order");
	private final JButton assign = new JButton("Assign category...");
	private final JButton capture = new JButton("Save current bank...");
	private final javax.swing.JCheckBox keepCurrentOrder = new javax.swing.JCheckBox("Keep current order");
	private final javax.swing.JComboBox<String> moveMode = new javax.swing.JComboBox<>(new String[]{"Swap", "Insert"});
	private int selectedItem = -1;
	private final javax.swing.JTextArea status = new javax.swing.JTextArea("Analyze your bank to begin.", 2, 0);
	private BankOrganizationPreview latest;
	private BankOrganizationPreview source;
	private BlueprintDraft draft;
	private String context;
	private int editedTab;
	private boolean saving;
	private JPanel draftGrid;
	private List<JLabel> draftCells = new ArrayList<>();
	private int dragSource = -1;
	private int boundary = -1;
	private int dropTab = -1;

	BlueprintEditorPanel(BankLayoutModel model, Function<BankPreviewItem, JLabel> renderer,
		Consumer<Boolean> editingChanged)
	{
		super(new BorderLayout());
		this.model = model;
		this.cellRenderer = renderer;
		this.editingChanged = editingChanged;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		tabs.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tabs.setForeground(ColorScheme.TEXT_COLOR);
		tabs.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controls.setOpaque(false);
		for (JButton button : new JButton[]{edit, undo, cancel, save, reset, assign, capture})
		{
			button.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
			if (button != assign && button != capture) controls.add(button);
		}
		JPanel movement = new JPanel(new FlowLayout(FlowLayout.LEFT));
		movement.setOpaque(false);
		JLabel modeLabel = new JLabel("Move mode:");
		modeLabel.setForeground(ColorScheme.TEXT_COLOR);
		movement.add(modeLabel);
		movement.add(moveMode);
		movement.add(assign);
		movement.add(capture);
		capture.setToolTipText("Save your open bank's tabs and item order as a new active layout. Remove bank fillers first.");
		moveMode.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		moveMode.setForeground(ColorScheme.TEXT_COLOR);
		moveMode.setToolTipText("Swap exchanges items; Insert moves the selected item to the clicked slot.");
		JPanel footer = new JPanel(new BorderLayout());
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footer.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		keepCurrentOrder.setOpaque(false);
		keepCurrentOrder.setForeground(ColorScheme.TEXT_COLOR);
		keepCurrentOrder.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
		keepCurrentOrder.setToolTipText("Keep this bank tab's order during guidance; items still move in and out. Save as keeps the choice with a named layout.");
		JPanel actions = new JPanel(new BorderLayout());
		actions.setOpaque(false);
		actions.add(keepCurrentOrder, BorderLayout.NORTH);
		actions.add(controls, BorderLayout.CENTER);
		footer.add(actions, BorderLayout.NORTH);
		footer.add(movement, BorderLayout.CENTER);
		status.setForeground(ColorScheme.TEXT_COLOR);
		status.setOpaque(false);
		status.setEditable(false);
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		status.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
		footer.add(status, BorderLayout.SOUTH);
		add(tabs, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
		edit.addActionListener(event -> beginEdit());
		undo.addActionListener(event -> { selectedItem = -1; draft.undo(); renderDraft(); });
		cancel.addActionListener(event -> cancelEdit());
		save.addActionListener(event -> saveDraft());
		reset.addActionListener(event -> resetTab());
		keepCurrentOrder.addActionListener(event -> {
			if (latest == null || draft != null || saving || tabs.getSelectedIndex() < 0) return;
			model.save(model.plan().withCurrentOrder(tabs.getSelectedIndex(), keepCurrentOrder.isSelected()));
			refreshControls();
		});
		capture.addActionListener(event -> saveCurrentBank(javax.swing.JOptionPane.showInputDialog(this,
			"Keep your bank open. Save all current tabs and item positions as a new active layout.",
			com.pkoka5.ironmanbankarchitect.organize.BankLayoutProfiles.freeName("My bank", model.profileNames()))));
		assign.addActionListener(event -> {
			String[] names = com.pkoka5.ironmanbankarchitect.organize.BankTags.all().stream()
				.filter(tag -> model.plan().destinationOf(tag.getKey()) >= 0)
				.map(tag -> tag.getName()).toArray(String[]::new);
			String chosen = (String) javax.swing.JOptionPane.showInputDialog(this,
				"Choose a category for the selected item. Save to keep this change.", "Assign blueprint category",
				javax.swing.JOptionPane.QUESTION_MESSAGE, null, names,
				com.pkoka5.ironmanbankarchitect.organize.BankTags.byKey(draft.items().get(selectedItem).getLayoutTagKey()).getName());
			com.pkoka5.ironmanbankarchitect.organize.BankTags.all().stream()
				.filter(tag -> tag.getName().equals(chosen)).findFirst().ifPresent(tag -> assignSelectedCategory(tag.getKey()));
		});
		tabs.addChangeListener(event -> {
			if (draft != null && tabs.getSelectedIndex() >= 0) {
				selectedItem = -1;
				editedTab = tabs.getSelectedIndex();
				draft.selectTab(editedTab);
				renderDraft();
			}
			refreshControls();
		});
		refreshControls();
	}

	void setPreview(BankOrganizationPreview preview)
	{
		if (preview == latest) return;
		latest = preview;
		if (draft == null && !saving) renderTabs();
		refreshControls();
	}

	void selectTab(int tab)
	{
		if (draft == null && tab >= 0 && tab < tabs.getTabCount()) tabs.setSelectedIndex(tab);
	}

	void beginEdit()
	{
		if (latest == null || saving || draft != null || tabs.getSelectedIndex() < 0) return;
		editedTab = tabs.getSelectedIndex();
		source = latest;
		context = model.editingContext();
		draft = new BlueprintDraft(source, model.options().itemOrders(), editedTab);
		selectedItem = -1;
		editingChanged.accept(true);
		renderDraft();
	}

	void assignSelectedCategory(String tag)
	{
		if (draft == null || selectedItem < 0 || saving || source == null || latest != source
			|| !model.editingContext().equals(context)) return;
		int target = model.plan().destinationOf(tag);
		if (target < 0 || draft.items().get(selectedItem).isBlank()
			|| tag.equals(draft.items().get(selectedItem).getLayoutTagKey())) return;
		draft.moveAcross(editedTab, selectedItem, target, draft.itemCount(target), tag);
		selectedItem = -1;
		tabs.setSelectedIndex(target);
		renderDraft();
	}

	void cancelEdit()
	{
		if (saving) return;
		draft = null;
		selectedItem = -1;
		source = null;
		editingChanged.accept(false);
		renderTabs();
		refreshControls();
	}

	void saveDraft()
	{
		if (draft == null || source == null || saving || latest != source || !model.editingContext().equals(context)) return;
		saving = true;
		refreshControls();
		java.util.Map<Integer, List<Integer>> orders = draft.orders();
		if (orders.isEmpty()) orders = Collections.singletonMap(editedTab, draft.ids());
		model.saveBlueprintEdit(orders, draft.transfers(), source, context, this::saved);
	}

	void saveCurrentBank(String name)
	{
		if (name == null || name.trim().isEmpty() || latest == null || draft != null || saving
			|| !model.options().itemOrders().isSupported()) return;
		saving = true;
		refreshControls();
		model.captureCurrentBank(name, latest, model.editingContext(), success -> {
			saved(success);
			status.setText(success ? "Current bank saved as your active layout. Future analysis follows these positions."
				: "Unable to save. Open your bank, remove fillers and analyze again; check the saved-layout limit.");
		});
	}

	private void resetTab()
	{
		if (latest == null || draft != null || saving || tabs.getSelectedIndex() < 0) return;
		persist(tabs.getSelectedIndex(), Collections.emptyList(), latest, model.editingContext());
	}

	private void persist(int tab, List<Integer> ids, BankOrganizationPreview expected, String expectedContext)
	{
		saving = true;
		refreshControls();
		model.saveItemOrder(tab, ids, expected, expectedContext, this::saved);
	}

	private void saved(boolean success)
	{
			saving = false;
			if (success) cancelEdit();
			else
			{
				// Keep the concept visible, but never allow it to overwrite newer state.
				source = null;
				if (draft == null) renderTabs();
				refreshControls();
				status.setText("Bank or layout changed. Cancel and reopen Edit to use the latest preview.");
			}
	}

	private void renderTabs()
	{
		int selected = Math.max(0, tabs.getSelectedIndex());
		tabs.removeAll();
		if (latest == null) return;
		for (int i = 0; i < latest.getCategories().size(); i++)
		{
			BankCategoryPreview category = latest.getCategories().get(i);
			String title = tabTitle(i, category.getItemCount());
			tabs.addTab(title, gridPane(category.getItems(), false));
			tabs.setToolTipTextAt(i, model.options().itemOrders().isCaptured() ? "Saved bank tab" : category.getCategory().getName());
		}
		selectTab(Math.min(selected, tabs.getTabCount() - 1));
	}

	private void renderDraft()
	{
		JScrollPane previous = (JScrollPane) tabs.getComponentAt(editedTab);
		Point position = previous.getViewport().getViewPosition();
		for (int i = 0; i < tabs.getTabCount(); i++)
			tabs.setTitleAt(i, tabTitle(i, draft.itemCount(i)));
		JScrollPane replacement = gridPane(draft.items(), true);
		tabs.setComponentAt(editedTab, replacement);
		replacement.getViewport().setViewPosition(position);
		refreshControls();
	}

	private JScrollPane gridPane(List<BankPreviewItem> items, boolean editable)
	{
		JPanel grid = new JPanel(new GridLayout(0, 8, 4, 4));
		grid.setBackground(BACKGROUND);
		grid.setPreferredSize(new Dimension(8 * 44, Math.max(1, (items.size() + 7) / 8) * 40));
		if (editable) { draftGrid = grid; draftCells = new ArrayList<>(); }
		for (int index = 0; index < items.size(); index++)
		{
			JLabel cell = cellRenderer.apply(items.get(index));
			cell.setOpaque(true);
			cell.setBackground(ColorScheme.DARK_GRAY_COLOR);
			cell.setForeground(ColorScheme.TEXT_COLOR);
			cell.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR));
			grid.add(cell);
			if (editable)
			{
				final int from = index;
				if (index == selectedItem) cell.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
				draftCells.add(cell);
				MouseAdapter mouse = new MouseAdapter()
				{
					private boolean dragged;
					@Override public void mouseClicked(MouseEvent event)
					{
						if (dragged || !javax.swing.SwingUtilities.isLeftMouseButton(event)
							|| saving || source == null || latest != source || !model.editingContext().equals(context)) return;
						if (selectedItem == from) selectedItem = -1;
						else if (selectedItem < 0) selectedItem = from;
						else {
							draft.place(selectedItem, from, "Swap".equals(moveMode.getSelectedItem()));
							selectedItem = -1;
						}
						renderDraft();
					}
					@Override public void mousePressed(MouseEvent event)
					{
						dragged = false;
						if (javax.swing.SwingUtilities.isLeftMouseButton(event)) {
							dragSource = from;
							boundary = -1;
							dropTab = -1;
						}
					}
					@Override public void mouseDragged(MouseEvent event)
					{
						if (dragSource < 0 || saving) return;
						dragged = true;
						selectedItem = -1;
						Point point = javax.swing.SwingUtilities.convertPoint(cell, event.getPoint(), draftGrid);
						Point tabPoint = javax.swing.SwingUtilities.convertPoint(cell, event.getPoint(), tabs);
						dropTab = tabs.indexAtLocation(tabPoint.x, tabPoint.y);
						boundary = insertionBoundary(point);
						for (JLabel label : draftCells) label.setBorder(BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR));
						if (boundary >= 0 && !draftCells.isEmpty())
						{
							boolean end = boundary == draftCells.size();
							draftCells.get(end ? boundary - 1 : boundary).setBorder(
								BorderFactory.createMatteBorder(0, end ? 0 : 3, 0, end ? 3 : 0, Color.ORANGE));
						}
						draftGrid.scrollRectToVisible(new Rectangle(point.x, point.y, 1, 24));
					}
					@Override public void mouseReleased(MouseEvent event)
					{
						if (!dragged) { dragSource = -1; return; }
						boolean emptyDestination = false;
						if (dragSource >= 0 && !saving) {
							if (dropTab >= 0 && dropTab != editedTab) {
								List<String> tags = model.plan().getTagKeys(dropTab);
								emptyDestination = tags.isEmpty();
								String tag = tags.size() == 1 ? tags.get(0) : tags.isEmpty() ? null :
									(String) javax.swing.JOptionPane.showInputDialog(BlueprintEditorPanel.this,
										"Choose the item's destination tag", "Move blueprint item",
										javax.swing.JOptionPane.QUESTION_MESSAGE, null, tags.toArray(), tags.get(0));
								if (tag != null) {
									draft.moveAcross(editedTab, dragSource, dropTab, draft.itemCount(dropTab), tag);
									tabs.setSelectedIndex(dropTab);
								}
							} else draft.move(dragSource, boundary);
						}
						dragSource = -1;
						boundary = -1;
						dropTab = -1;
						renderDraft();
						if (emptyDestination) status.setText("Assign a tag to that tab in Layout before moving items there.");
					}
				};
				cell.addMouseListener(mouse);
				cell.addMouseMotionListener(mouse);
			}
		}
		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper.setBackground(BACKGROUND);
		wrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		wrapper.add(grid);
		JScrollPane scroll = new JScrollPane(wrapper);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(BACKGROUND);
		return scroll;
	}

	private int insertionBoundary(Point point)
	{
		for (int i = 0; i < draftCells.size(); i++)
		{
			Rectangle bounds = draftCells.get(i).getBounds();
			if (bounds.contains(point)) return i + (point.x >= bounds.getCenterX() ? 1 : 0);
		}
		return -1;
	}

	private String tabTitle(int tab, int count)
	{
		return (tab == 0 ? "MAIN" : "TAB " + (tab + 1)) + "  " + count;
	}

	private void refreshControls()
	{
		boolean editing = draft != null;
		boolean stale = editing && (source == null || latest != source || !model.editingContext().equals(context));
		boolean ready = latest != null && tabs.getSelectedIndex() >= 0 && !saving
			&& model.options().itemOrders().isSupported();
		keepCurrentOrder.setSelected(tabs.getSelectedIndex() >= 0 && model.plan().keepsCurrentOrder(tabs.getSelectedIndex()));
		keepCurrentOrder.setEnabled(ready && !editing);
		edit.setEnabled(ready && !editing && !latest.getCategories().get(tabs.getSelectedIndex()).getItems().isEmpty());
		undo.setEnabled(editing && draft.canUndo() && !saving);
		cancel.setEnabled(editing && !saving);
		save.setEnabled(editing && !stale && !saving);
		reset.setEnabled(ready && !editing && latest.getCategories().get(tabs.getSelectedIndex()).hasManualOrder());
		capture.setEnabled(ready && !editing);
		moveMode.setEnabled(editing && !stale && !saving);
		assign.setEnabled(editing && selectedItem >= 0 && !draft.items().get(selectedItem).isBlank() && !stale && !saving);
		if (saving) status.setText("Saving local blueprint...");
		else if (stale) status.setText("Bank or layout changed. Cancel to load the latest preview.");
		else if (editing) status.setText(selectedItem >= 0
			? "Selected (green): click a target slot or Assign category. Save keeps changes; Cancel discards them."
			: "Select an item, then a target slot (Swap/Insert). Save updates the plan; move bank items manually.");
		else if (ready)
		{
			BankCategoryPreview selected = latest.getCategories().get(tabs.getSelectedIndex());
			status.setText(keepCurrentOrder.isSelected()
				? "Current bank order is kept during guidance. Preview shows saved/automatic order; items still move between tabs."
				: (model.options().itemOrders().isCaptured() ? "Saved current bank" : selected.getCategory().getName()) + (selected.hasManualOrder()
				? " — Manual item order" : " — Automatic item order"));
		}
		else status.setText("Waiting for bank analysis...");
	}
}

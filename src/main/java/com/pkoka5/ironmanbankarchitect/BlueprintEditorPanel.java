package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/** Swing-only blueprint editor. Dragging updates a draft; Save updates local configuration. */
final class BlueprintEditorPanel extends JPanel
{
	private static final Color BACKGROUND = new Color(35, 31, 25);
	private final BankLayoutModel model;
	private final Function<BankPreviewItem, JLabel> cellRenderer;
	private final Consumer<Boolean> editingChanged;
	private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
	private final JButton edit = new JButton("Edit this tab");
	private final JButton undo = new JButton("Undo");
	private final JButton cancel = new JButton("Cancel");
	private final JButton save = new JButton("Save");
	private final JButton reset = new JButton("Reset tab order");
	private final javax.swing.JComboBox<String> moveMode = new javax.swing.JComboBox<>(new String[]{"Swap", "Insert"});
	private int selectedItem = -1;
	private final JLabel status = new JLabel("Analyze your bank to begin.");
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
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for (JButton button : new JButton[]{edit, undo, cancel, save, reset}) controls.add(button);
		controls.add(new JLabel("Move mode:"));
		controls.add(moveMode);
		moveMode.setToolTipText("Swap exchanges items; Insert moves the selected item to the clicked slot.");
		JPanel footer = new JPanel(new BorderLayout());
		footer.add(controls, BorderLayout.NORTH);
		footer.add(status, BorderLayout.SOUTH);
		add(tabs, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
		edit.addActionListener(event -> beginEdit());
		undo.addActionListener(event -> { selectedItem = -1; draft.undo(); renderDraft(); });
		cancel.addActionListener(event -> cancelEdit());
		save.addActionListener(event -> saveDraft());
		reset.addActionListener(event -> resetTab());
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
			String title = (i == 0 ? "MAIN" : "TAB " + (i + 1)) + "  " + category.getItemCount();
			tabs.addTab(title, gridPane(category.getItems(), false));
			tabs.setToolTipTextAt(i, category.getCategory().getName());
		}
		selectTab(Math.min(selected, tabs.getTabCount() - 1));
	}

	private void renderDraft()
	{
		for (int i = 0; i < tabs.getTabCount(); i++)
			tabs.setTitleAt(i, (i == 0 ? "MAIN" : "TAB " + (i + 1)) + "  " + draft.itemCount(i));
		tabs.setComponentAt(editedTab, gridPane(draft.items(), true));
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
						for (JLabel label : draftCells) label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
		wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		wrapper.add(grid);
		return new JScrollPane(wrapper);
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

	private void refreshControls()
	{
		boolean editing = draft != null;
		boolean stale = editing && (source == null || latest != source || !model.editingContext().equals(context));
		boolean ready = latest != null && tabs.getSelectedIndex() >= 0 && !saving
			&& model.options().itemOrders().isSupported();
		edit.setEnabled(ready && !editing && !latest.getCategories().get(tabs.getSelectedIndex()).getItems().isEmpty());
		undo.setEnabled(editing && draft.canUndo() && !saving);
		cancel.setEnabled(editing && !saving);
		save.setEnabled(editing && !stale && !saving);
		reset.setEnabled(ready && !editing && latest.getCategories().get(tabs.getSelectedIndex()).hasManualOrder());
		moveMode.setEnabled(editing && !stale && !saving);
		if (saving) status.setText("Saving local blueprint...");
		else if (stale) status.setText("Bank or layout changed. Cancel to load the latest preview.");
		else if (editing) status.setText(selectedItem >= 0
			? "Item selected (green). Click a target slot, or click the item again to deselect."
			: "Click an item, then a target slot. Choose Swap or Insert. Drag to a tab to move between tabs.");
		else if (ready)
		{
			BankCategoryPreview selected = latest.getCategories().get(tabs.getSelectedIndex());
			status.setText(selected.getCategory().getName() + (selected.hasManualOrder()
				? " — Manual item order" : " — Automatic item order"));
		}
		else status.setText("Waiting for bank analysis...");
	}
}

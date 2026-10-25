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
	private final boolean readOnly;
	private Consumer<BankOrganizationPreview> bankPreview = preview -> {};
	private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
	private final JButton edit = new JButton("Edit this tab");
	private final JButton undo = new JButton("Undo");
	private final JButton cancel = new JButton("Cancel");
	private final JButton save = new JButton("Save");
	private final JButton reset = new JButton("Reset tab order");
	private final JButton assign = new JButton("Assign category...");
	private final JButton capture = new JButton("Save current bank...");
	private final JButton review = new JButton("Review changes");
	private final JCheckBox keepCurrentOrder = new JCheckBox("Keep current order");
	private final JComboBox<String> moveMode = new JComboBox<>(new String[]{"Swap", "Insert"});
	private int selectedItem = -1;
	private final JTextArea status = new JTextArea("Analyze your bank to begin.", 2, 0);
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
		this(model, renderer, editingChanged, false);
	}

	BlueprintEditorPanel(BankLayoutModel model, Function<BankPreviewItem, JLabel> renderer,
		Consumer<Boolean> editingChanged, boolean readOnly)
	{
		super(new BorderLayout());
		this.model = model;
		this.cellRenderer = renderer;
		this.editingChanged = editingChanged;
		this.readOnly = readOnly;
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
		movement.add(review);
		review.setFont(net.runelite.client.ui.FontManager.getRunescapeSmallFont());
		review.setToolTipText("Compare destinations and relative item order, preview the latest proposal, or keep it as your target.");
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
		if (!readOnly) add(footer, BorderLayout.SOUTH);
		edit.addActionListener(event -> beginEdit());
		review.addActionListener(event -> showChanges());
		undo.addActionListener(event -> { selectedItem = -1; draft.undo(); renderDraft(); });
		cancel.addActionListener(event -> cancelEdit());
		save.addActionListener(event -> saveDraft());
		reset.addActionListener(event -> resetTab());
		keepCurrentOrder.addActionListener(event -> {
			if (latest == null || draft != null || saving || tabs.getSelectedIndex() < 0) return;
			model.save(model.plan().withCurrentOrder(tabs.getSelectedIndex(), keepCurrentOrder.isSelected()));
			refreshControls();
		});
		capture.addActionListener(event -> saveCurrentBank(JOptionPane.showInputDialog(this,
			"Keep your bank open. Save all current tabs and item positions as a new active layout.",
			BankLayoutProfiles.freeName("My bank", model.profileNames()))));
		assign.addActionListener(event -> {
			String[] names = BankTags.all().stream()
				.filter(tag -> model.plan().destinationOf(tag.getKey()) >= 0)
				.map(tag -> tag.getName()).toArray(String[]::new);
			String chosen = (String) JOptionPane.showInputDialog(this,
				"Choose a category for the selected item. Save to keep this change.", "Assign blueprint category",
				JOptionPane.QUESTION_MESSAGE, null, names,
				BankTags.byKey(draft.items().get(selectedItem).getLayoutTagKey()).getName());
			BankTags.all().stream()
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

	private BankOrganizationPreview proposal()
	{
		BankOrganizationPreview proposed = model.proposedBlueprint();
		return proposed == null ? latest : proposed;
	}

	String reviewText()
	{
		return latest == null ? "Analyze your bank to review changes." : model.blueprintState() + "\n\n"
			+ BlueprintChanges.compare(model.reviewReference(), proposal()).report()
			+ "\n\nKeeping a proposal saves the order and destinations of its current items. New items append to their matching tab."
			+ "\nAdopting another proposal replaces that snapshot, including any saved entries currently absent."
			+ "\nEmpty planner cells are excluded; the saved layout can appear more compact."
			+ "\nPersonal setting changes pause the saved target. Bank items always move manually.";
	}

	private void showChanges()
	{
		if (readOnly || latest == null || draft != null || saving) return;
		BankOrganizationPreview expected = latest;
		BankOrganizationPreview proposed = proposal();
		String expectedContext = model.editingContext();
		JTextArea text = new JTextArea(reviewText(), 16, 56);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		List<String> actions = new ArrayList<>();
		actions.add("View proposal");
		if (AcceptedBlueprint.isSupported(model.acceptedBlueprint())) actions.add("Keep this proposal");
		if (model.acceptedBlueprint().isEmpty() && BlueprintChanges.compare(model.rememberedBlueprint(), expected).isSupported())
			actions.add("Remember reference only");
		actions.add("Close");
		int choice = JOptionPane.showOptionDialog(this, new JScrollPane(text), "Review blueprint",
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, actions.toArray(), "Close");
		if (choice < 0) return;
		if ("Keep this proposal".equals(actions.get(choice))) acceptBlueprint(expected, proposed, expectedContext);
		else if ("Remember reference only".equals(actions.get(choice))) rememberBlueprint(expected, expectedContext);
		else if ("View proposal".equals(actions.get(choice)))
		{
			BlueprintEditorPanel view = new BlueprintEditorPanel(model, cellRenderer, ignored -> {}, true);
			view.setPreview(proposed);
			view.setPreferredSize(new Dimension(700, 560));
			JOptionPane.showMessageDialog(this, view, "Latest proposal — preview only", JOptionPane.PLAIN_MESSAGE);
		}
	}

	void acceptBlueprint(BankOrganizationPreview expected, BankOrganizationPreview proposed, String expectedContext)
	{
		if (readOnly || latest == null || latest != expected || proposed == null || proposal() != proposed
			|| draft != null || saving || !model.editingContext().equals(expectedContext)) return;
		saving = true;
		refreshControls();
		model.acceptBlueprint(expected, proposed, expectedContext, success -> {
			saving = false;
			renderTabs();
			refreshControls();
			status.setText(success ? "Blueprint kept. Existing items keep their saved order; new items append to their matching tab."
				: "Unable to keep the proposal. Bank or layout changed, or saved data is unsupported. Analyze again.");
		});
	}

	void rememberBlueprint(BankOrganizationPreview expected, String expectedContext)
	{
		if (readOnly || latest == null || latest != expected || draft != null || saving || !model.editingContext().equals(expectedContext)) return;
		saving = true;
		refreshControls();
		model.rememberBlueprint(expected, expectedContext, success -> {
			saving = false;
			refreshControls();
			status.setText(success ? "Blueprint reference remembered. Future comparisons include destinations and relative order."
				: "Unable to remember. Bank or layout changed, or the reference is unsupported. Analyze again.");
		});
	}

	void configureBankPreview(Consumer<BankOrganizationPreview> action) { bankPreview = action; }

	void publishBankPreview()
	{
		if (readOnly) return;
		boolean ready = latest != null && !saving
			&& (draft == null || source == latest && model.editingContext().equals(context));
		BankOrganizationPreview snapshot = ready ? latest : null;
		if (ready && draft != null)
		{
			List<BankCategoryPreview> categories = new ArrayList<>();
			for (int i = 0; i < latest.getCategories().size(); i++)
				categories.add(new BankCategoryPreview(latest.getCategories().get(i).getCategory(), draft.items(i)));
			snapshot = new BankOrganizationPreview(latest.getPreset(), categories);
		}
		bankPreview.accept(snapshot);
	}

	void beginEdit()
	{
		if (readOnly || latest == null || saving || draft != null || tabs.getSelectedIndex() < 0) return;
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
		Map<Integer, List<Integer>> orders = draft.orders();
		if (orders.isEmpty()) orders = Collections.singletonMap(editedTab, draft.ids());
		model.saveBlueprintEdit(orders, draft.transfers(), source, context, this::saved);
	}

	void saveCurrentBank(String name)
	{
		if (readOnly || name == null || name.trim().isEmpty() || latest == null || draft != null || saving
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
						if (dragged || !SwingUtilities.isLeftMouseButton(event)
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
						if (SwingUtilities.isLeftMouseButton(event)) {
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
						Point point = SwingUtilities.convertPoint(cell, event.getPoint(), draftGrid);
						Point tabPoint = SwingUtilities.convertPoint(cell, event.getPoint(), tabs);
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
									(String) JOptionPane.showInputDialog(BlueprintEditorPanel.this,
										"Choose the item's destination tag", "Move blueprint item",
										JOptionPane.QUESTION_MESSAGE, null, tags.toArray(), tags.get(0));
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
		boolean ready = !readOnly && latest != null && tabs.getSelectedIndex() >= 0 && !saving
			&& model.options().itemOrders().isSupported();
		keepCurrentOrder.setSelected(tabs.getSelectedIndex() >= 0 && model.plan().keepsCurrentOrder(tabs.getSelectedIndex()));
		keepCurrentOrder.setEnabled(ready && !editing);
		edit.setEnabled(ready && !editing && !latest.getCategories().get(tabs.getSelectedIndex()).getItems().isEmpty());
		undo.setEnabled(editing && draft.canUndo() && !saving);
		cancel.setEnabled(editing && !saving);
		save.setEnabled(editing && !stale && !saving);
		reset.setEnabled(ready && !editing && latest.getCategories().get(tabs.getSelectedIndex()).hasManualOrder());
		capture.setEnabled(ready && !editing);
		review.setEnabled(ready && !editing);
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
		publishBankPreview();
	}
}

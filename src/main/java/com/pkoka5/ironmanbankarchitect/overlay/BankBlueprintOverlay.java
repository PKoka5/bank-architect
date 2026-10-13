package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.organize.*;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Function;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseWheelListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.*;

/** Temporary drawing only. Published hitboxes keep AWT input away from covered bank actions. */
public final class BankBlueprintOverlay extends Overlay implements MouseListener, MouseWheelListener
{
	private final Client client;
	private final Function<BankPreviewItem, BufferedImage> icons;
	private final Function<Widget, BufferedImage> backgrounds;
	private final BankOverlayReservations reservations;
	private final Runnable closed;
	private volatile View view;
	private volatile Frame frame;
	private boolean consumedPress;
	private volatile boolean tabPress, tabReleased;
	private Point tabStart;

	public BankBlueprintOverlay(Plugin plugin, Client client, Function<BankPreviewItem, BufferedImage> icons,
		Function<Widget, BufferedImage> backgrounds,
		BankOverlayReservations reservations, Runnable closed)
	{
		super(plugin);
		this.client = client;
		this.icons = icons;
		this.backgrounds = backgrounds;
		this.reservations = reservations;
		this.closed = closed;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_HIGHEST);
		setMovable(false);
		setResizable(false);
	}

	public void show(BankOrganizationPreview preview)
	{
		if (preview == null) { hide(); return; }
		View next = new View(preview);
		View previous = view;
		if (previous != null && previous.tabs.equals(next.tabs)) return;
		view = next;
		if (frame == null)
		{
			Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
			if (bank != null && !bank.isHidden() && BankOverlayGeometry.isSafeGeometry(bank.getBounds()))
				frame = new Frame(view, bank.getBounds(), new Rectangle(), 0, 0, 36, tabBounds());
		}
		reservations.previewActive = true;
	}

	public boolean isActive() { return view != null; }
	public void hide() { view = null; frame = null; tabPress = tabReleased = false; reservations.previewActive = false; }
	public void close() { if (isActive()) { hide(); closed.run(); } }

	@Override public Dimension render(Graphics2D graphics)
	{
		View current = view;
		if (current == null) return null;
		if (tabReleased && client.getMouseCurrentButton() == 0) tabPress = tabReleased = false;
		Widget container = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		if (container == null || bank == null || container.isHidden() || bank.isHidden()
			|| client.isMenuOpen() || client.isDraggingWidget() || client.getMouseCurrentButton() != 0 && !tabPress)
		{ close(); return null; }
		Rectangle bounds = viewport(container.getBounds()), bankBounds = bank.getBounds();
		if (!BankOverlayGeometry.isSafeGeometry(bounds) || !BankOverlayGeometry.isSafeGeometry(bankBounds)
			|| !bankBounds.contains(bounds))
		{ close(); return null; }
		Grid grid = grid(client.getWidget(InterfaceID.Bankmain.ITEMS), bounds);
		if (grid == null) { close(); return null; }
		bounds.width -= grid.x - bounds.x;
		bounds.x = grid.x;
		Widget backing = backing(client.getWidget(InterfaceID.Bankmain.FRAME), bounds);
		BufferedImage texture = backing != null && backing.getType() == WidgetType.GRAPHIC ? backgrounds.apply(backing) : null;
		if (backing == null || backing.getType() == WidgetType.GRAPHIC && texture == null)
		{ close(); return null; }
		int tab = client.getVarbitValue(VarbitID.BANK_CURRENTTAB);
		if (tab < 0 || tab > 9 || bounds.y + bounds.height - grid.y < grid.height) { close(); return null; }
		List<List<BankPreviewItem>> sections = tab == 0 ? current.tabs : Collections.singletonList(
			tab < current.tabs.size() ? current.tabs.get(tab) : Collections.emptyList());
		int height = 0;
		for (int i = 0; i < sections.size(); i++)
			if (!sections.get(i).isEmpty()) height += (tab == 0 && i > 0 ? grid.gap : 0) + (sections.get(i).size() + 7) / 8 * grid.height;
		int maxScroll = Math.max(0, height - (bounds.y + bounds.height - grid.y));
		int scroll = current.scroll.updateAndGet(tab, value -> Math.max(0, Math.min(value, maxScroll)));
		Graphics2D draw = (Graphics2D) graphics.create();
		try
		{
			draw.clip(bounds);
			draw.setColor(new Color(backing.getTextColor()));
			draw.fill(bounds);
			Rectangle anchor = backing.getBounds();
			if (texture != null)
			{
				if (backing.getSpriteTiling()) draw.setPaint(new TexturePaint(texture, new Rectangle(anchor.x, anchor.y, texture.getWidth(), texture.getHeight())));
				else draw.setPaint(new TexturePaint(texture, anchor));
				draw.fill(bounds);
			}
			int offset = 0;
			for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++)
			{
				List<BankPreviewItem> section = sections.get(sectionIndex);
				if (section.isEmpty()) continue;
				if (tab == 0 && sectionIndex > 0)
				{
					draw.setColor(grid.separator);
					draw.drawLine(bounds.x, grid.y + offset + grid.gap / 2 - scroll, bounds.x + bounds.width, grid.y + offset + grid.gap / 2 - scroll);
					offset += grid.gap;
				}
				for (int slot = 0; slot < section.size(); slot++)
				{
					int y = grid.y + offset + slot / 8 * grid.height - scroll;
					BankPreviewItem item = section.get(slot);
					if (item.isBlank() || y + Constants.ITEM_SPRITE_HEIGHT <= bounds.y || y >= bounds.y + bounds.height) continue;
					BufferedImage icon = icons.apply(item);
					if (icon != null) draw.drawImage(icon, grid.x + slot % 8 * grid.width, y, null);
				}
				offset += (section.size() + 7) / 8 * grid.height;
			}
		}
		finally { draw.dispose(); }
		frame = new Frame(current, bankBounds, bounds, tab, maxScroll, grid.height, tabBounds());
		return null;
	}

	private Rectangle viewport(Rectangle container)
	{
		Rectangle tabs = widgetBounds(InterfaceID.Bankmain.TABS), bottom = widgetBounds(InterfaceID.Bankmain.BOTTOM),
			bar = widgetBounds(InterfaceID.Bankmain.SCROLLBAR);
		if (!BankOverlayGeometry.isSafeGeometry(container) || tabs == null || bottom == null || bar == null) return null;
		int top = Math.max(container.y, Math.max(tabs.y + tabs.height, bar.y));
		int end = Math.min(container.y + container.height, Math.min(bottom.y, bar.y + bar.height));
		return new Rectangle(container.x, top, Math.min(container.x + container.width, bar.x) - container.x, end - top);
	}

	private Rectangle widgetBounds(int id)
	{
		Widget widget = client.getWidget(id);
		if (widget == null || widget.isHidden()) return null;
		Rectangle bounds = widget.getBounds();
		return BankOverlayGeometry.isSafeGeometry(bounds) ? bounds : null;
	}

	private static Widget backing(Widget widget, Rectangle viewport)
	{
		if (widget == null || widget.isHidden()) return null;
		Rectangle bounds = widget.getBounds();
		if (BankOverlayGeometry.isSafeGeometry(bounds) && bounds.contains(viewport)
			&& (widget.getType() == WidgetType.GRAPHIC && widget.getSpriteId() >= 0
				|| widget.getType() == WidgetType.RECTANGLE && widget.isFilled())) return widget;
		for (Widget[] children : new Widget[][]{widget.getDynamicChildren(), widget.getStaticChildren(), widget.getNestedChildren()})
			if (children != null) for (Widget child : children)
			{
				Widget found = backing(child, viewport);
				if (found != null) return found;
			}
		return null;
	}

	private static Grid grid(Widget items, Rectangle viewport)
	{
		if (items == null || items.isHidden()) return null;
		Rectangle area = items.getBounds();
		if (!BankOverlayGeometry.isSafeGeometry(area)) return null;
		Widget[] children = items.getChildren();
		if (children == null) children = items.getDynamicChildren();
		TreeSet<Integer> xs = new TreeSet<>(), ys = new TreeSet<>();
		Color separator = new Color(0x4b453d);
		if (children != null) for (Widget child : children)
		{
			if (child == null || child.isHidden()) continue;
			Rectangle bounds = child.getBounds();
			if (child.getItemId() <= 0)
			{
				if (child.getType() == WidgetType.LINE && bounds != null && bounds.width > 100) separator = new Color(child.getTextColor());
				continue;
			}
			if (!BankOverlayGeometry.isSafeGeometry(bounds)) continue;
			xs.add(bounds.x); ys.add(bounds.y);
		}
		if (xs.isEmpty()) return null;
		int dx = pitch(xs, Constants.ITEM_SPRITE_WIDTH, 48), dy = Math.min(36, pitch(ys, Constants.ITEM_SPRITE_HEIGHT, 36));
		int gap = Integer.MAX_VALUE, previous = ys.first();
		for (int y : ys) { if (y - previous > dy && y - previous < dy * 2) gap = Math.min(gap, y - previous - dy); previous = y; }
		if (xs.first() < viewport.x || xs.first() + 7 * dx + Constants.ITEM_SPRITE_WIDTH > viewport.x + viewport.width) return null;
		return new Grid(xs.first(), Math.max(viewport.y, area.y), dx, dy, gap == Integer.MAX_VALUE ? dy / 2 : gap, separator);
	}

	private Rectangle[] tabBounds()
	{
		Rectangle[] result = new Rectangle[10];
		Widget tabs = client.getWidget(InterfaceID.Bankmain.TABS);
		for (int tab = 0; tab < result.length; tab++)
		{
			Widget header = BankGuideOverlay.resolveTabHeader(tabs, tab, tab == 0 ? "View all items" : "View tab");
			if (header != null) result[tab] = new Rectangle(header.getBounds());
		}
		return result;
	}

	public boolean isTabView(MenuOptionClicked event)
	{
		int tab = event.getParam0() - 10;
		if (event.getParam1() != InterfaceID.Bankmain.TABS || tab < 0 || tab > 9
			|| event.getMenuAction() != MenuAction.CC_OP && event.getMenuAction() != MenuAction.CC_OP_LOW_PRIORITY) return false;
		String action = tab == 0 ? "View all items" : "View tab";
		Widget header = BankGuideOverlay.resolveTabHeader(client.getWidget(InterfaceID.Bankmain.TABS), tab, action);
		String[] actions = header == null ? null : header.getActions();
		return actions != null && event.getId() > 0 && event.getId() <= actions.length
			&& action.equals(actions[event.getId() - 1]) && action.equals(event.getMenuOption());
	}

	private static int pitch(SortedSet<Integer> positions, int size, int fallback)
	{
		int previous = positions.first(), smallest = Integer.MAX_VALUE;
		for (int value : positions)
		{
			int delta = value - previous;
			if (delta >= size && delta < smallest) smallest = delta;
			previous = value;
		}
		return smallest <= size * 2 ? smallest : fallback;
	}

	@Override public MouseEvent mousePressed(MouseEvent event)
	{
		Frame shown = frame;
		boolean inside = isActive() && shown != null && shown.bank.contains(event.getPoint());
		tabPress = inside && event.getButton() == MouseEvent.BUTTON1 && Arrays.stream(shown.tabs)
			.anyMatch(bounds -> bounds != null && bounds.contains(event.getPoint()));
		tabReleased = false;
		tabStart = tabPress ? event.getPoint() : null;
		consumedPress = inside && !tabPress;
		if (consumedPress) { event.consume(); close(); }
		return event;
	}
	@Override public MouseEvent mouseReleased(MouseEvent event) { if (consumedPress) event.consume(); if (tabPress) tabReleased = true; return event; }
	@Override public MouseEvent mouseClicked(MouseEvent event) { if (consumedPress) event.consume(); consumedPress = false; return event; }
	@Override public MouseEvent mouseDragged(MouseEvent event)
	{ if (consumedPress) event.consume(); if (tabPress && tabStart.distance(event.getPoint()) > 5) close(); return event; }
	@Override public MouseEvent mouseMoved(MouseEvent event) { return event; }
	@Override public MouseEvent mouseEntered(MouseEvent event) { return event; }
	@Override public MouseEvent mouseExited(MouseEvent event) { return event; }
	@Override public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
	{
		Frame shown = frame;
		if (isActive() && shown != null && shown.bank.contains(event.getPoint()))
		{
			event.consume();
			if (view == shown.view && shown.viewport.contains(event.getPoint())) shown.view.scroll.updateAndGet(shown.tab, value ->
				Math.max(0, Math.min(shown.maxScroll, value + event.getWheelRotation() * shown.pitch * 3)));
		}
		return event;
	}

	private static final class View
	{
		final List<List<BankPreviewItem>> tabs = new ArrayList<>();
		final AtomicIntegerArray scroll = new AtomicIntegerArray(10);
		View(BankOrganizationPreview preview)
		{
			BankTabPlan plan = BankTabPlan.fromPreview(preview);
			tabs.add(plan.getMainItems());
			for (BankTabPlan.TargetTab tab : plan.getNumberedTabs()) tabs.add(tab.getItems());
		}
	}
	private static final class Grid extends Rectangle
	{
		final int gap;
		final Color separator;
		Grid(int x, int y, int dx, int dy, int gap, Color separator)
		{ super(x, y, dx, dy); this.gap = gap; this.separator = separator; }
	}
	private static final class Frame
	{
		final View view;
		final Rectangle bank, viewport;
		final int tab, maxScroll, pitch;
		final Rectangle[] tabs;
		Frame(View view, Rectangle bank, Rectangle viewport, int tab, int maxScroll, int pitch, Rectangle[] tabs)
		{ this.view = view; this.bank = new Rectangle(bank); this.viewport = new Rectangle(viewport); this.tab = tab;
			this.maxScroll = maxScroll; this.pitch = pitch; this.tabs = tabs; }
	}
}

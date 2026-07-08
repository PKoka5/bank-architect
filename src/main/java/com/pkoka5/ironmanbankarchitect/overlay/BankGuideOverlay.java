package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSlot;
import com.pkoka5.ironmanbankarchitect.blueprint.SlotKind;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public final class BankGuideOverlay extends Overlay
{
	private static final int SLOT_COUNT = 8;

	private static final Color GUIDE_FILL = new Color(64, 200, 255, 55);
	private static final Color GUIDE_BORDER = new Color(64, 200, 255, 170);
	private static final Color RESERVED_FILL = new Color(150, 150, 150, 45);
	private static final Color RESERVED_BORDER = new Color(150, 150, 150, 150);
	private static final Color BADGE_COLOR = new Color(255, 255, 255, 220);

	private final Client client;
	private final BankGuideController guideController;

	public BankGuideOverlay(Plugin plugin, Client client, BankGuideController guideController)
	{
		super(plugin);
		this.client = client;
		this.guideController = guideController;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(false);
		setResizable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget bankItems = client.getWidget(InterfaceID.Bankmain.ITEMS);
		boolean bankIsOpen = bankItems != null && !bankItems.isHidden();
		guideController.setBankOpen(bankIsOpen);

		if (!bankIsOpen || !guideController.isGuideEnabled())
		{
			return null;
		}

		Rectangle gridBounds = bankItems.getBounds();
		if (!isSafeGeometry(gridBounds))
		{
			return null;
		}

		List<Rectangle> firstRow = SlotGridResolver.resolveFirstRow(collectCandidateBounds(bankItems, gridBounds), SLOT_COUNT);
		if (firstRow.size() < SLOT_COUNT)
		{
			return null;
		}

		VisualBlock block = guideController.getSelectedBlock();
		List<BlueprintSlot> slots = block.getSlots();

		Shape originalClip = graphics.getClip();
		Font originalFont = graphics.getFont();
		Stroke originalStroke = graphics.getStroke();

		graphics.clip(gridBounds);
		graphics.setFont(originalFont.deriveFont(Font.BOLD, 11f));
		graphics.setStroke(new BasicStroke(1f));

		for (int i = 0; i < SLOT_COUNT && i < slots.size(); i++)
		{
			BlueprintSlot slot = slots.get(i);
			Rectangle cell = firstRow.get(i);

			boolean reserved = slot.getKind() == SlotKind.EMPTY;
			graphics.setColor(reserved ? RESERVED_FILL : GUIDE_FILL);
			graphics.fill(cell);
			graphics.setColor(reserved ? RESERVED_BORDER : GUIDE_BORDER);
			graphics.draw(cell);

			graphics.setColor(BADGE_COLOR);
			graphics.drawString(Integer.toString(i + 1), cell.x + 3, cell.y + 12);
		}

		graphics.setStroke(originalStroke);
		graphics.setFont(originalFont);
		graphics.setClip(originalClip);

		return null;
	}

	private static List<Rectangle> collectCandidateBounds(Widget container, Rectangle gridBounds)
	{
		List<Rectangle> candidates = new ArrayList<>();
		addVisibleBounds(container.getDynamicChildren(), gridBounds, candidates);
		if (candidates.isEmpty())
		{
			addVisibleBounds(container.getChildren(), gridBounds, candidates);
		}

		return candidates;
	}

	private static void addVisibleBounds(Widget[] children, Rectangle gridBounds, List<Rectangle> candidates)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			if (child == null || child.isHidden())
			{
				continue;
			}

			Rectangle bounds = child.getBounds();
			if (!isSafeGeometry(bounds) || !gridBounds.contains(bounds))
			{
				continue;
			}

			candidates.add(bounds);
		}
	}

	private static boolean isSafeGeometry(Rectangle bounds)
	{
		return bounds != null && bounds.width > 0 && bounds.height > 0;
	}
}

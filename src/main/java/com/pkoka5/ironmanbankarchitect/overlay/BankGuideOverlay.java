package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Comparator;
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
	private static final Color CORRECT_FILL = new Color(75, 190, 95, 50);
	private static final Color CORRECT_BORDER = new Color(75, 220, 95, 190);
	private static final Color MISPLACED_FILL = new Color(240, 165, 45, 50);
	private static final Color MISPLACED_BORDER = new Color(245, 175, 45, 190);
	private static final Color WRONG_FILL = new Color(220, 70, 70, 50);
	private static final Color WRONG_BORDER = new Color(230, 80, 80, 190);
	private static final Color UNKNOWN_FILL = new Color(150, 150, 150, 35);
	private static final Color UNKNOWN_BORDER = new Color(150, 150, 150, 130);
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

		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
		if (preview == null)
		{
			return null;
		}

		Rectangle gridBounds = bankItems.getBounds();
		if (!isSafeGeometry(gridBounds))
		{
			return null;
		}

		List<BankSlotWidget> bankSlots = collectBankSlots(bankItems, gridBounds);
		if (bankSlots.isEmpty())
		{
			return null;
		}

		Shape originalClip = graphics.getClip();
		Font originalFont = graphics.getFont();
		Stroke originalStroke = graphics.getStroke();

		graphics.clip(gridBounds);
		graphics.setFont(originalFont.deriveFont(Font.BOLD, 11f));
		graphics.setStroke(new BasicStroke(1f));

		for (int i = 0; i < bankSlots.size(); i++)
		{
			BankSlotWidget slot = bankSlots.get(i);
			SlotValidationState state = stateFor(preview, i, slot.itemId);
			Rectangle cell = slot.bounds;

			graphics.setColor(fillFor(state));
			graphics.fill(cell);
			graphics.setColor(borderFor(state));
			graphics.draw(cell);

			graphics.setColor(BADGE_COLOR);
			graphics.drawString(Integer.toString(i + 1), cell.x + 3, cell.y + 12);
		}

		graphics.setStroke(originalStroke);
		graphics.setFont(originalFont);
		graphics.setClip(originalClip);

		return null;
	}

	static SlotValidationState stateFor(BankOrganizationPreview preview, int physicalSlotIndex, int actualItemId)
	{
		int expectedItemId = preview.getExpectedItemId(physicalSlotIndex);
		if (expectedItemId <= 0)
		{
			return SlotValidationState.UNKNOWN;
		}

		if (actualItemId == expectedItemId)
		{
			return SlotValidationState.CORRECT;
		}

		if (actualItemId <= 0)
		{
			return SlotValidationState.WRONG;
		}

		return preview.getPlannedSlotIndex(actualItemId) >= 0
			? SlotValidationState.MISPLACED
			: SlotValidationState.WRONG;
	}

	static Color fillFor(SlotValidationState state)
	{
		switch (state)
		{
			case CORRECT:
				return CORRECT_FILL;
			case MISPLACED:
				return MISPLACED_FILL;
			case WRONG:
				return WRONG_FILL;
			case UNKNOWN:
			default:
				return UNKNOWN_FILL;
		}
	}

	static Color borderFor(SlotValidationState state)
	{
		switch (state)
		{
			case CORRECT:
				return CORRECT_BORDER;
			case MISPLACED:
				return MISPLACED_BORDER;
			case WRONG:
				return WRONG_BORDER;
			case UNKNOWN:
			default:
				return UNKNOWN_BORDER;
		}
	}

	private static List<BankSlotWidget> collectBankSlots(Widget container, Rectangle gridBounds)
	{
		List<BankSlotWidget> candidates = new ArrayList<>();
		addVisibleSlots(container.getDynamicChildren(), gridBounds, candidates);
		if (candidates.isEmpty())
		{
			addVisibleSlots(container.getChildren(), gridBounds, candidates);
		}

		candidates.sort(Comparator.comparingInt((BankSlotWidget slot) -> slot.bounds.y)
			.thenComparingInt(slot -> slot.bounds.x));
		return candidates;
	}

	private static void addVisibleSlots(Widget[] children, Rectangle gridBounds, List<BankSlotWidget> candidates)
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

			candidates.add(new BankSlotWidget(bounds, child.getItemId()));
		}
	}

	private static boolean isSafeGeometry(Rectangle bounds)
	{
		return bounds != null && bounds.width > 0 && bounds.height > 0;
	}

	enum SlotValidationState
	{
		CORRECT,
		MISPLACED,
		WRONG,
		UNKNOWN
	}

	private static final class BankSlotWidget
	{
		private final Rectangle bounds;
		private final int itemId;

		private BankSlotWidget(Rectangle bounds, int itemId)
		{
			this.bounds = bounds;
			this.itemId = itemId;
		}
	}
}

package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectConfig;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Assessment;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.GuideProgress;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.NextMove;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Session;
import com.pkoka5.ironmanbankarchitect.guide.NextMoveAdvisor.Status;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public final class BankGuideOverlay extends Overlay
{
	private static final int ALL_ITEMS_TAB = 0;
	private static final int SWAP_MODE = 0;

	private static final Color CORRECT_FILL = new Color(75, 190, 95, 50);
	private static final Color CORRECT_BORDER = new Color(75, 220, 95, 190);
	private static final Color MISPLACED_FILL = new Color(240, 165, 45, 50);
	private static final Color MISPLACED_BORDER = new Color(245, 175, 45, 190);
	private static final Color WRONG_FILL = new Color(220, 70, 70, 50);
	private static final Color WRONG_BORDER = new Color(230, 80, 80, 190);
	private static final Color UNKNOWN_FILL = new Color(150, 150, 150, 35);
	private static final Color UNKNOWN_BORDER = new Color(150, 150, 150, 130);
	private static final Color BADGE_COLOR = new Color(255, 255, 255, 220);
	private static final Color MOVE_SOURCE = new Color(255, 205, 60, 240);
	private static final Color MOVE_TARGET = new Color(90, 200, 255, 240);
	private static final Color STATUS_BACKGROUND = new Color(0, 0, 0, 205);
	private static final Color STATUS_FOREGROUND = new Color(255, 255, 255, 235);

	private final Client client;
	private final BankGuideController guideController;
	private final IronmanBankArchitectConfig config;
	private final Session moveSession = new Session();
	private final Map<Integer, Integer> canonicalItemIdCache = new HashMap<>();
	private BankOrganizationPreview cachedPreview;
	private List<BankPreviewItem> cachedPlannedItems;
	private Map<Integer, Integer> cachedPlannedSlotByItemId;

	public BankGuideOverlay(Plugin plugin, Client client, BankGuideController guideController,
		IronmanBankArchitectConfig config)
	{
		super(plugin);
		this.client = client;
		this.guideController = guideController;
		this.config = config;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(false);
		setResizable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget bankItems = client.getWidget(InterfaceID.Bankmain.ITEMS);
		Widget bankItemsContainer = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
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

		Rectangle gridBounds = itemViewportBounds(bankItems.getBounds(),
			bankItemsContainer == null || bankItemsContainer.isHidden()
				? null : bankItemsContainer.getBounds());
		if (!isSafeGeometry(gridBounds))
		{
			return null;
		}

		if (client.getVarbitValue(VarbitID.BANK_CURRENTTAB) != ALL_ITEMS_TAB)
		{
			return blocked(graphics, gridBounds,
				"Open the vanilla All items tab for item-order guidance.");
		}
		if (client.getVarbitValue(VarbitID.BANK_INSERTMODE) != SWAP_MODE)
		{
			return blocked(graphics, gridBounds,
				"Switch the bank rearrange mode to Swap for safe guided moves.");
		}

		refreshPlanCache(preview);
		List<BankPreviewItem> plannedItems = cachedPlannedItems;
		int[] actualItemIds = readCanonicalBankItemIds(canonicalItemIdCache);
		List<BankSlotWidget> bankSlots = collectBankSlots(bankItems, actualItemIds.length,
			canonicalItemIdCache);
		if (!viewMatchesLogicalBank(bankSlots, actualItemIds))
		{
			return blocked(graphics, gridBounds,
				"Bank view cannot be mapped safely. Clear search/tag views and remove fillers or gaps.");
		}

		Map<Integer, BankSlotWidget> allByLogicalSlot = new HashMap<>();
		Map<Integer, BankSlotWidget> visibleByLogicalSlot = new HashMap<>();
		for (BankSlotWidget slot : bankSlots)
		{
			allByLogicalSlot.put(slot.logicalSlot, slot);
			if (isFullyVisible(gridBounds, slot.bounds))
			{
				visibleByLogicalSlot.put(slot.logicalSlot, slot);
			}
		}
		Assessment assessment = moveSession.assess(actualItemIds, plannedItems,
			visibleByLogicalSlot.keySet());
		if (assessment.getStatus() != Status.READY && assessment.getStatus() != Status.COMPLETE)
		{
			return blocked(graphics, gridBounds, blockedMessage(assessment.getStatus()));
		}

		String guideText = guideText(preview, actualItemIds, assessment, visibleByLogicalSlot.keySet());
		guideController.publishGuideProgressText(guideText);
		Map<Integer, Integer> plannedSlotByItemId = cachedPlannedSlotByItemId;

		Graphics2D bankGraphics = (Graphics2D) graphics.create();
		try
		{
			bankGraphics.clip(gridBounds);
			bankGraphics.setFont(bankGraphics.getFont().deriveFont(Font.BOLD, 11f));
			bankGraphics.setStroke(new BasicStroke(1f));
			for (BankSlotWidget slot : bankSlots)
			{
				if (!gridBounds.intersects(slot.bounds))
				{
					continue;
				}
				SlotValidationState state = stateFor(plannedItems, plannedSlotByItemId,
					slot.logicalSlot, slot.itemId);
				bankGraphics.setColor(fillFor(state));
				bankGraphics.fill(slot.bounds);
				bankGraphics.setColor(borderFor(state));
				bankGraphics.draw(slot.bounds);
				bankGraphics.setColor(BADGE_COLOR);
				bankGraphics.drawString(Integer.toString(slot.logicalSlot + 1),
					slot.bounds.x + 3, slot.bounds.y + 12);
			}

			if (config.suggestNextMove() && assessment.getMove().isPresent())
			{
				drawMove(bankGraphics, assessment.getMove().get(), allByLogicalSlot,
					visibleByLogicalSlot, gridBounds);
			}
		}
		finally
		{
			bankGraphics.dispose();
		}

		drawStatus(graphics, gridBounds, guideText);
		return null;
	}

	private Dimension blocked(Graphics2D graphics, Rectangle gridBounds, String message)
	{
		guideController.publishGuideProgressText(message);
		drawStatus(graphics, gridBounds, message);
		return null;
	}

	private void refreshPlanCache(BankOrganizationPreview preview)
	{
		if (cachedPreview == preview)
		{
			return;
		}
		cachedPreview = preview;
		cachedPlannedItems = preview.getPlannedItems();
		cachedPlannedSlotByItemId = plannedSlotByItemId(cachedPlannedItems);
		moveSession.reset();
	}

	private int[] readCanonicalBankItemIds(Map<Integer, Integer> canonicalItemIds)
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			return new int[0];
		}

		Item[] items = bank.getItems();
		int[] itemIds = new int[items.length];
		Arrays.fill(itemIds, -1);
		int lastOccupiedSlot = -1;
		for (int slot = 0; slot < items.length; slot++)
		{
			Item item = items[slot];
			if (item == null || item.getId() <= 0 || item.getId() == ItemID.BANK_FILLER)
			{
				continue;
			}
			itemIds[slot] = canonicalItemId(item.getId(), canonicalItemIds);
			if (itemIds[slot] > 0)
			{
				lastOccupiedSlot = slot;
			}
		}

		return lastOccupiedSlot < 0 ? new int[0] : Arrays.copyOf(itemIds, lastOccupiedSlot + 1);
	}

	private int canonicalItemId(int itemId, Map<Integer, Integer> canonicalItemIds)
	{
		Integer cached = canonicalItemIds.get(itemId);
		if (cached != null)
		{
			return cached;
		}
		ItemComposition composition = client.getItemDefinition(itemId);
		int canonical = canonicalItemId(itemId,
			composition == null ? -1 : composition.getPlaceholderTemplateId(),
			composition == null ? -1 : composition.getPlaceholderId());
		canonicalItemIds.put(itemId, canonical);
		return canonical;
	}

	static int canonicalItemId(int itemId, int placeholderTemplateId, int placeholderItemId)
	{
		if (itemId <= 0 || itemId == ItemID.BANK_FILLER)
		{
			return -1;
		}
		return placeholderTemplateId != -1 && placeholderItemId > 0 ? placeholderItemId : itemId;
	}

	private List<BankSlotWidget> collectBankSlots(Widget container, int logicalSlotLimit,
		Map<Integer, Integer> canonicalItemIds)
	{
		List<BankSlotWidget> candidates = new ArrayList<>();
		Widget[] children = container.getChildren();
		if (children == null)
		{
			children = container.getDynamicChildren();
		}
		addBankSlots(children, logicalSlotLimit, canonicalItemIds, candidates);
		candidates.sort(Comparator.comparingInt(slot -> slot.logicalSlot));
		return candidates;
	}

	private void addBankSlots(Widget[] children, int logicalSlotLimit,
		Map<Integer, Integer> canonicalItemIds, List<BankSlotWidget> candidates)
	{
		if (children == null)
		{
			return;
		}
		for (Widget child : children)
		{
			if (child == null || child.isHidden() || child.getIndex() < 0
				|| child.getIndex() >= logicalSlotLimit || child.getItemId() <= 0)
			{
				continue;
			}
			Rectangle bounds = child.getBounds();
			if (!isSafeGeometry(bounds))
			{
				continue;
			}
			int itemId = canonicalItemId(child.getItemId(), canonicalItemIds);
			if (itemId > 0)
			{
				candidates.add(new BankSlotWidget(child.getIndex(), bounds, itemId));
			}
		}
	}

	private static boolean viewMatchesLogicalBank(List<BankSlotWidget> bankSlots, int[] actualItemIds)
	{
		if (bankSlots.isEmpty() || bankSlots.size() != actualItemIds.length)
		{
			return false;
		}
		Set<Integer> seenSlots = new HashSet<>();
		for (BankSlotWidget slot : bankSlots)
		{
			if (slot.logicalSlot < 0 || slot.logicalSlot >= actualItemIds.length
				|| actualItemIds[slot.logicalSlot] != slot.itemId
				|| !seenSlots.add(slot.logicalSlot))
			{
				return false;
			}
		}
		return true;
	}

	private static Map<Integer, Integer> plannedSlotByItemId(List<BankPreviewItem> plannedItems)
	{
		Map<Integer, Integer> slots = new HashMap<>();
		for (int slot = 0; slot < plannedItems.size(); slot++)
		{
			int itemId = plannedItems.get(slot).getItemId();
			if (itemId > 0)
			{
				slots.put(itemId, slot);
			}
		}
		return slots;
	}

	static SlotValidationState stateFor(BankOrganizationPreview preview, int physicalSlotIndex, int actualItemId)
	{
		List<BankPreviewItem> planned = preview.getPlannedItems();
		return stateFor(planned, plannedSlotByItemId(planned), physicalSlotIndex, actualItemId);
	}

	private static SlotValidationState stateFor(List<BankPreviewItem> plannedItems,
		Map<Integer, Integer> plannedSlotByItemId, int physicalSlotIndex, int actualItemId)
	{
		if (physicalSlotIndex < 0 || physicalSlotIndex >= plannedItems.size()
			|| plannedItems.get(physicalSlotIndex).getItemId() <= 0)
		{
			return SlotValidationState.UNKNOWN;
		}
		int expectedItemId = plannedItems.get(physicalSlotIndex).getItemId();
		if (actualItemId == expectedItemId)
		{
			return SlotValidationState.CORRECT;
		}
		if (actualItemId <= 0)
		{
			return SlotValidationState.WRONG;
		}
		return plannedSlotByItemId.containsKey(actualItemId)
			? SlotValidationState.MISPLACED : SlotValidationState.WRONG;
	}

	private static String blockedMessage(Status status)
	{
		switch (status)
		{
			case RESCAN_REQUIRED:
				return "Bank contents changed. Run Analyze My Bank again.";
			case DUPLICATE_ITEMS:
				return "Duplicate live/planned item IDs are unsupported; analyze after the bank compacts.";
			case UNSTABLE_TARGET:
				return "Bank fillers or true gaps detected. Remove them before guided swaps.";
			case UNSUPPORTED_PLAN:
				return "This blueprint contains unsupported blank targets.";
			default:
				return "No safe manual move is available. Analyze the bank again.";
		}
	}

	private String guideText(BankOrganizationPreview preview, int[] actualItemIds, Assessment assessment,
		Set<Integer> visibleSlots)
	{
		GuideProgress total = assessment.getProgress();
		if (assessment.getStatus() == Status.COMPLETE)
		{
			return "Blueprint item order matches: " + total.getCorrectSlots() + "/" + total.getPlannedSlots()
				+ " slots. Physical tab boundaries are not yet validated.";
		}

		NextMove move = assessment.getMove().get();
		int categoryOffset = 0;
		String categoryName = "Whole bank";
		GuideProgress categoryProgress = total;
		for (BankCategoryPreview category : preview.getCategories())
		{
			int categorySize = category.getItems().size();
			if (move.getToSlot() < categoryOffset + categorySize)
			{
				categoryName = category.getCategory().getName();
				categoryProgress = NextMoveAdvisor.progress(actualItemIds, category.getItems(), categoryOffset);
				break;
			}
			categoryOffset += categorySize;
		}

		String progressLine = categoryName + ": " + categoryProgress.getPercent()
			+ "% item-order progress (" + categoryProgress.getCorrectSlots() + "/"
			+ categoryProgress.getPlannedSlots() + ").";
		if (!config.suggestNextMove())
		{
			return progressLine + "\nNext-move highlight is disabled in plugin settings.";
		}
		String moveLine = "Next manual swap: All-items slot " + (move.getFromSlot() + 1)
			+ " -> " + (move.getToSlot() + 1) + ".";
		String itemLine = "Move " + move.getDisplayName() + ".";
		boolean sourceVisible = visibleSlots.contains(move.getFromSlot());
		boolean targetVisible = visibleSlots.contains(move.getToSlot());
		if (sourceVisible && targetVisible)
		{
			return progressLine + "\n" + moveLine + "\n" + itemLine
				+ "\nFollow the FROM -> TO arrow.";
		}
		if (!sourceVisible && targetVisible)
		{
			return progressLine + "\n" + moveLine + "\n" + itemLine + "\nFROM is "
				+ directionForSlot(move.getFromSlot(), visibleSlots) + ".";
		}
		if (sourceVisible)
		{
			return progressLine + "\n" + moveLine + "\n" + itemLine + "\nTO is "
				+ directionForSlot(move.getToSlot(), visibleSlots) + ".";
		}
		return progressLine + "\n" + moveLine + "\n" + itemLine + "\nScroll "
			+ directionForSlot(move.getFromSlot(), visibleSlots) + " to FROM.";
	}

	static String directionForSlot(int logicalSlot, Set<Integer> visibleSlots)
	{
		if (visibleSlots.isEmpty()) return "off-screen";
		int firstVisible = Integer.MAX_VALUE;
		int lastVisible = Integer.MIN_VALUE;
		for (int visibleSlot : visibleSlots)
		{
			firstVisible = Math.min(firstVisible, visibleSlot);
			lastVisible = Math.max(lastVisible, visibleSlot);
		}
		if (logicalSlot < firstVisible) return "above this view";
		if (logicalSlot > lastVisible) return "below this view";
		return "outside the visible bank area";
	}

	private static void drawMove(Graphics2D graphics, NextMove move,
		Map<Integer, BankSlotWidget> allByLogicalSlot,
		Map<Integer, BankSlotWidget> visibleByLogicalSlot, Rectangle viewportBounds)
	{
		BankSlotWidget source = visibleByLogicalSlot.get(move.getFromSlot());
		BankSlotWidget target = visibleByLogicalSlot.get(move.getToSlot());
		BankSlotWidget logicalSource = allByLogicalSlot.get(move.getFromSlot());
		BankSlotWidget logicalTarget = allByLogicalSlot.get(move.getToSlot());

		if (source != null && target != null)
		{
			drawArrowConnector(graphics, centerX(source.bounds), centerY(source.bounds),
				centerX(target.bounds), centerY(target.bounds));
			drawMoveCell(graphics, source.bounds, MOVE_SOURCE, "FROM");
			drawMoveCell(graphics, target.bounds, MOVE_TARGET, "TO");
			return;
		}

		if (source != null)
		{
			int targetY = edgeY(move.getToSlot(), visibleByLogicalSlot.keySet(), viewportBounds);
			int targetX = edgeX(logicalTarget, centerX(source.bounds), viewportBounds);
			drawArrowConnector(graphics, centerX(source.bounds), centerY(source.bounds), targetX, targetY);
			drawMoveCell(graphics, source.bounds, MOVE_SOURCE, "FROM");
			drawEdgeBadge(graphics, targetX, targetY, MOVE_TARGET,
				"TO " + (move.getToSlot() + 1), targetY <= viewportBounds.y + 14, viewportBounds);
			return;
		}

		if (target != null)
		{
			int sourceY = edgeY(move.getFromSlot(), visibleByLogicalSlot.keySet(), viewportBounds);
			int sourceX = edgeX(logicalSource, centerX(target.bounds), viewportBounds);
			drawArrowConnector(graphics, sourceX, sourceY, centerX(target.bounds), centerY(target.bounds));
			drawEdgeBadge(graphics, sourceX, sourceY, MOVE_SOURCE,
				"FROM " + (move.getFromSlot() + 1), sourceY <= viewportBounds.y + 14, viewportBounds);
			drawMoveCell(graphics, target.bounds, MOVE_TARGET, "TO");
			return;
		}

		// No endpoint is currently visible. Use their logical order (and their
		// off-screen widget bounds when available) to point the player toward FROM.
		int sourceY = edgeY(move.getFromSlot(), visibleByLogicalSlot.keySet(), viewportBounds);
		int sourceX = viewportBounds.x + viewportBounds.width / 2;
		if (logicalSource != null)
		{
			sourceX = clamp(centerX(logicalSource.bounds), viewportBounds.x + 14,
				viewportBounds.x + viewportBounds.width - 14);
		}
		drawEdgeBadge(graphics, sourceX, sourceY, MOVE_SOURCE,
			"FROM " + (move.getFromSlot() + 1), sourceY <= viewportBounds.y + 14, viewportBounds);
	}

	private static int edgeX(BankSlotWidget offscreenSlot, int fallbackX, Rectangle viewportBounds)
	{
		int x = offscreenSlot == null ? fallbackX : centerX(offscreenSlot.bounds);
		return clamp(x, viewportBounds.x + 14, viewportBounds.x + viewportBounds.width - 14);
	}

	private static void drawArrowConnector(Graphics2D graphics, int fromX, int fromY, int toX, int toY)
	{
		graphics.setColor(new Color(0, 0, 0, 210));
		graphics.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.drawLine(fromX, fromY, toX, toY);
		graphics.setColor(new Color(255, 255, 255, 235));
		graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.drawLine(fromX, fromY, toX, toY);
		drawArrowHead(graphics, fromX, fromY, toX, toY);
	}

	private static void drawArrowHead(Graphics2D graphics, int fromX, int fromY, int toX, int toY)
	{
		double angle = Math.atan2(toY - fromY, toX - fromX);
		int length = 12;
		double spread = Math.toRadians(28);
		int leftX = toX - (int) Math.round(length * Math.cos(angle - spread));
		int leftY = toY - (int) Math.round(length * Math.sin(angle - spread));
		int rightX = toX - (int) Math.round(length * Math.cos(angle + spread));
		int rightY = toY - (int) Math.round(length * Math.sin(angle + spread));
		Polygon arrow = new Polygon(new int[]{toX, leftX, rightX}, new int[]{toY, leftY, rightY}, 3);
		graphics.setColor(MOVE_TARGET);
		graphics.fillPolygon(arrow);
		graphics.setColor(new Color(0, 0, 0, 230));
		graphics.setStroke(new BasicStroke(2f));
		graphics.drawPolygon(arrow);
	}

	private static void drawMoveCell(Graphics2D graphics, Rectangle bounds, Color color, String label)
	{
		graphics.setStroke(new BasicStroke(4f));
		graphics.setColor(new Color(0, 0, 0, 220));
		graphics.drawRect(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2);
		graphics.setColor(color);
		graphics.draw(bounds);
		graphics.fillRect(bounds.x + 1, bounds.y + bounds.height - 13,
			Math.min(bounds.width - 2, graphics.getFontMetrics().stringWidth(label) + 5), 12);
		graphics.setColor(Color.BLACK);
		graphics.drawString(label, bounds.x + 3, bounds.y + bounds.height - 3);
	}

	private static void drawEdgeBadge(Graphics2D graphics, int centerX, int edgeY, Color color,
		String label, boolean topEdge, Rectangle viewportBounds)
	{
		FontMetrics metrics = graphics.getFontMetrics();
		int width = metrics.stringWidth(label) + 10;
		int height = 16;
		int x = clamp(centerX - width / 2, viewportBounds.x + 2,
			viewportBounds.x + viewportBounds.width - width - 2);
		int y = topEdge ? edgeY + 2 : edgeY - height - 2;
		graphics.setColor(new Color(0, 0, 0, 220));
		graphics.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 5, 5);
		graphics.setColor(color);
		graphics.fillRoundRect(x, y, width, height, 4, 4);
		graphics.setColor(Color.BLACK);
		graphics.drawString(label, x + 5, y + 12);
	}

	private static int edgeY(int logicalSlot, Set<Integer> visibleSlots, Rectangle viewportBounds)
	{
		if (visibleSlots.isEmpty()) return viewportBounds.y + viewportBounds.height - 7;
		int firstVisible = Integer.MAX_VALUE;
		for (int visibleSlot : visibleSlots)
		{
			firstVisible = Math.min(firstVisible, visibleSlot);
		}
		return logicalSlot < firstVisible
			? viewportBounds.y + 7
			: viewportBounds.y + viewportBounds.height - 7;
	}

	private static int clamp(int value, int minimum, int maximum)
	{
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static void drawStatus(Graphics2D graphics, Rectangle gridBounds, String text)
	{
		Graphics2D statusGraphics = (Graphics2D) graphics.create();
		try
		{
			Font font = statusGraphics.getFont().deriveFont(Font.BOLD, 11f);
			statusGraphics.setFont(font);
			FontMetrics metrics = statusGraphics.getFontMetrics(font);
			String[] rawLines = text.split("\\n", -1);
			String[] lines = new String[rawLines.length];
			int maxTextWidth = Math.max(80, gridBounds.width - 16);
			int width = 0;
			for (int i = 0; i < rawLines.length; i++)
			{
				lines[i] = fitText(rawLines[i], metrics, maxTextWidth);
				width = Math.max(width, metrics.stringWidth(lines[i]));
			}
			int lineHeight = metrics.getHeight();
			int height = lineHeight * lines.length + 6;
			int x = gridBounds.x + 5;
			int y = Math.max(2, gridBounds.y - height - 3);
			if (y + height > gridBounds.y)
			{
				y = gridBounds.y + 4;
			}

			statusGraphics.setColor(STATUS_BACKGROUND);
			statusGraphics.fillRoundRect(x, y, width + 10, height, 6, 6);
			statusGraphics.setColor(STATUS_FOREGROUND);
			for (int i = 0; i < lines.length; i++)
			{
				statusGraphics.drawString(lines[i], x + 5,
					y + 3 + metrics.getAscent() + i * lineHeight);
			}
		}
		finally
		{
			statusGraphics.dispose();
		}
	}

	private static String fitText(String text, FontMetrics metrics, int maxWidth)
	{
		if (metrics.stringWidth(text) <= maxWidth)
		{
			return text;
		}
		String suffix = "...";
		int end = text.length();
		while (end > 0 && metrics.stringWidth(text.substring(0, end) + suffix) > maxWidth)
		{
			end--;
		}
		return text.substring(0, end) + suffix;
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

	private static boolean isSafeGeometry(Rectangle bounds)
	{
		return bounds != null && bounds.width > 0 && bounds.height > 0;
	}

	static Rectangle itemViewportBounds(Rectangle itemBounds, Rectangle outerBounds)
	{
		if (!isSafeGeometry(itemBounds))
		{
			return itemBounds;
		}
		if (!isSafeGeometry(outerBounds))
		{
			return new Rectangle(itemBounds);
		}
		Rectangle intersection = itemBounds.intersection(outerBounds);
		return isSafeGeometry(intersection) ? intersection : new Rectangle(itemBounds);
	}

	static boolean isFullyVisible(Rectangle viewportBounds, Rectangle slotBounds)
	{
		return isSafeGeometry(viewportBounds) && isSafeGeometry(slotBounds)
			&& viewportBounds.contains(slotBounds);
	}

	private static int centerX(Rectangle bounds)
	{
		return bounds.x + bounds.width / 2;
	}

	private static int centerY(Rectangle bounds)
	{
		return bounds.y + bounds.height / 2;
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
		private final int logicalSlot;
		private final Rectangle bounds;
		private final int itemId;

		private BankSlotWidget(int logicalSlot, Rectangle bounds, int itemId)
		{
			this.logicalSlot = logicalSlot;
			this.bounds = bounds;
			this.itemId = itemId;
		}
	}
}

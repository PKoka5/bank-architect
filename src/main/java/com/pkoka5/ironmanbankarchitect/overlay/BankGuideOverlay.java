package com.pkoka5.ironmanbankarchitect.overlay;

import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectConfig;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.guide.BankTabPlan;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Move;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.MoveType;
import com.pkoka5.ironmanbankarchitect.guide.TabRouteAdvisor.Phase;
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
import net.runelite.api.gameval.VarClientID;
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
	private static final int[] BANK_TAB_VARBITS = {
		VarbitID.BANK_TAB_1,
		VarbitID.BANK_TAB_2,
		VarbitID.BANK_TAB_3,
		VarbitID.BANK_TAB_4,
		VarbitID.BANK_TAB_5,
		VarbitID.BANK_TAB_6,
		VarbitID.BANK_TAB_7,
		VarbitID.BANK_TAB_8,
		VarbitID.BANK_TAB_9
	};

	private static final Color CORRECT_FILL = new Color(75, 190, 95, 50);
	private static final Color CORRECT_BORDER = new Color(75, 220, 95, 190);
	private static final Color MISPLACED_FILL = new Color(240, 165, 45, 50);
	private static final Color MISPLACED_BORDER = new Color(245, 175, 45, 190);
	private static final Color WRONG_FILL = new Color(220, 70, 70, 50);
	private static final Color WRONG_BORDER = new Color(230, 80, 80, 190);
	private static final Color UNKNOWN_FILL = new Color(150, 150, 150, 35);
	private static final Color UNKNOWN_BORDER = new Color(150, 150, 150, 130);
	private static final Color MOVE_SOURCE = new Color(255, 205, 60, 240);
	private static final Color MOVE_TARGET = new Color(90, 200, 255, 240);
	private static final Color STATUS_BACKGROUND = new Color(0, 0, 0, 205);
	private static final Color STATUS_FOREGROUND = new Color(255, 255, 255, 235);

	private final Client client;
	private final BankGuideController guideController;
	private final IronmanBankArchitectConfig config;
	private final TabRouteAdvisor.Session tabRouteSession = new TabRouteAdvisor.Session();
	private final Map<Integer, Integer> canonicalItemIdCache = new HashMap<>();
	private BankOrganizationPreview cachedPreview;
	private BankTabPlan cachedTabPlan;
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

		int currentBankTab = client.getVarbitValue(VarbitID.BANK_CURRENTTAB);
		if (currentBankTab != ALL_ITEMS_TAB
			&& (currentBankTab < 1 || currentBankTab > TabRouteAdvisor.MAX_TABS))
		{
			return blocked(graphics, gridBounds,
				"Open the vanilla All items tab for item-order guidance.");
		}
		if (client.getVarbitValue(VarbitID.BANK_INSERTMODE) != SWAP_MODE)
		{
			return blocked(graphics, gridBounds,
				"Switch the bank rearrange mode to Swap for safe guided moves.");
		}
		if (isFilteredBankView())
		{
			return blocked(graphics, gridBounds,
				"Clear bank search and bank-tag filters before using item-order guidance.");
		}
		int[] tabCounts = readBankTabCounts();

		refreshPlanCache(preview);
		List<BankPreviewItem> plannedItems = cachedPlannedItems;
		int[] actualItemIds = readCanonicalBankItemIds(canonicalItemIdCache);
		List<BankSlotWidget> bankSlots = collectBankSlots(bankItems, actualItemIds.length,
			canonicalItemIdCache);
		int[] sectionRange = null;
		if (currentBankTab == ALL_ITEMS_TAB)
		{
			if (!viewMatchesLogicalBank(bankSlots, actualItemIds))
			{
				return blocked(graphics, gridBounds, viewSyncMessage(false),
					guideController.getGuideProgressPercent());
			}
		}
		else
		{
			sectionRange = sectionRangeForTab(tabCounts, currentBankTab);
			if (sectionRange == null
				|| !viewMatchesSection(bankSlots, actualItemIds, sectionRange[0], sectionRange[1]))
			{
				return blocked(graphics, gridBounds, viewSyncMessage(true),
					guideController.getGuideProgressPercent());
			}
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
		boolean suggestNextMove = config.suggestNextMove();
		TabRouteAdvisor.Assessment assessment;
		if (suggestNextMove)
		{
			assessment = tabRouteSession.assess(actualItemIds, cachedTabPlan, tabCounts,
				client.getTickCount(), currentBankTab);
		}
		else
		{
			assessment = TabRouteAdvisor.assess(actualItemIds, cachedTabPlan, tabCounts,
				currentBankTab);
		}
		if (assessment.getStatus() != TabRouteAdvisor.Status.READY
			&& assessment.getStatus() != TabRouteAdvisor.Status.COMPLETE)
		{
			String blockedMessage = tabBlockedMessage(assessment.getStatus());
			if (assessment.getStatus() == TabRouteAdvisor.Status.DUPLICATE_ITEMS)
			{
				List<String> names = new ArrayList<>();
				for (int itemId : assessment.getDuplicateItemIds())
				{
					ItemComposition composition = client.getItemDefinition(itemId);
					String name = composition == null ? null : composition.getName();
					names.add(name == null || name.isEmpty() ? "#" + itemId : name);
				}
				blockedMessage = duplicateItemsMessage(names);
			}
			return blocked(graphics, gridBounds, blockedMessage,
				blockedProgressPercent(assessment.getStatus(),
					assessment.getProgress().getPercent()));
		}

		Move move = assessment.getMove().orElse(null);
		if (sectionRange != null && move != null
			&& !isSectionLocalSwap(move, sectionRange[0], sectionRange[1]))
		{
			return blocked(graphics, gridBounds,
				"Nothing left to sort in this tab right now.\nOpen the All items tab to continue guidance.",
				assessment.getProgress().getPercent());
		}
		boolean tabTargetMove = suggestNextMove
			&& move != null && isTabTargetMove(move.getType());
		Widget tabTarget = tabTargetMove
			? resolveTabTarget(client.getWidget(InterfaceID.Bankmain.TABS), move) : null;
		if (tabTargetMove && tabTarget == null)
		{
			return blocked(graphics, gridBounds, missingTabTargetMessage(move.getType()));
		}

		String guideText = tabGuideText(assessment, visibleByLogicalSlot.keySet(),
			allByLogicalSlot, gridBounds);
		String hudText = tabHudText(assessment, suggestNextMove);
		guideController.publishGuideProgress(guideText, assessment.getProgress().getPercent());
		Map<Integer, Integer> plannedSlotByItemId = cachedPlannedSlotByItemId;
		boolean showFinalValidation = assessment.getProgress().getPhase() == Phase.SORTING
			|| assessment.getStatus() == TabRouteAdvisor.Status.COMPLETE;

		Graphics2D bankGraphics = (Graphics2D) graphics.create();
		try
		{
			bankGraphics.clip(gridBounds);
			bankGraphics.setFont(bankGraphics.getFont().deriveFont(Font.BOLD, 11f));
			bankGraphics.setStroke(new BasicStroke(1f));
			if (showFinalValidation)
			{
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
				}
			}

			if (suggestNextMove && move != null && isGridSwap(move.getType()))
			{
				drawSwapMove(bankGraphics, move.getFromSlot(), move.getToSlot(),
					allByLogicalSlot, visibleByLogicalSlot, gridBounds);
			}
		}
		finally
		{
			bankGraphics.dispose();
		}

		if (tabTargetMove)
		{
			Graphics2D connectorGraphics = (Graphics2D) graphics.create();
			try
			{
				connectorGraphics.setFont(connectorGraphics.getFont().deriveFont(Font.BOLD, 11f));
				drawTabMove(connectorGraphics, move, tabTarget.getBounds(), allByLogicalSlot,
					visibleByLogicalSlot, gridBounds);
			}
			finally
			{
				connectorGraphics.dispose();
			}
		}

		Rectangle hudSource = suggestNextMove && move != null && move.getFromSlot() >= 0
			&& allByLogicalSlot.containsKey(move.getFromSlot())
			? allByLogicalSlot.get(move.getFromSlot()).bounds : null;
		Rectangle hudTarget = tabTargetMove ? tabTarget.getBounds()
			: suggestNextMove && move != null && move.getToSlot() >= 0
				&& allByLogicalSlot.containsKey(move.getToSlot())
				? allByLogicalSlot.get(move.getToSlot()).bounds : null;
		drawStatus(graphics, gridBounds, hudText, true, hudSource, hudTarget);
		return null;
	}

	private Dimension blocked(Graphics2D graphics, Rectangle gridBounds, String message)
	{
		return blocked(graphics, gridBounds, message, -1);
	}

	private Dimension blocked(Graphics2D graphics, Rectangle gridBounds, String message, int percent)
	{
		guideController.publishGuideProgress(message, percent);
		drawStatus(graphics, gridBounds, message);
		return null;
	}

	/**
	 * Container slot range [start, end) of a 1-based numbered bank tab, or
	 * null when that tab does not currently exist.
	 */
	static int[] sectionRangeForTab(int[] tabCounts, int tabNumber)
	{
		if (tabNumber < 1 || tabNumber > tabCounts.length || tabCounts[tabNumber - 1] <= 0)
		{
			return null;
		}
		int start = 0;
		for (int tabIndex = 0; tabIndex < tabNumber - 1; tabIndex++)
		{
			if (tabCounts[tabIndex] <= 0)
			{
				return null;
			}
			start += tabCounts[tabIndex];
		}
		return new int[]{start, start + tabCounts[tabNumber - 1]};
	}

	/**
	 * A numbered-tab view only shows that tab's container range; every visible
	 * slot must map inside it and match the live container contents.
	 */
	static boolean viewMatchesSection(List<BankSlotWidget> bankSlots, int[] actualItemIds,
		int sectionStart, int sectionEnd)
	{
		if (bankSlots.isEmpty() || bankSlots.size() != sectionEnd - sectionStart)
		{
			return false;
		}
		Set<Integer> seenSlots = new HashSet<>();
		for (BankSlotWidget slot : bankSlots)
		{
			if (slot.logicalSlot < sectionStart || slot.logicalSlot >= sectionEnd
				|| slot.logicalSlot >= actualItemIds.length
				|| actualItemIds[slot.logicalSlot] != slot.itemId
				|| !seenSlots.add(slot.logicalSlot))
			{
				return false;
			}
		}
		return true;
	}

	static boolean isSectionLocalSwap(Move move, int sectionStart, int sectionEnd)
	{
		return isGridSwap(move.getType())
			&& move.getFromSlot() >= sectionStart && move.getFromSlot() < sectionEnd
			&& move.getToSlot() >= sectionStart && move.getToSlot() < sectionEnd;
	}

	private int[] readBankTabCounts()
	{
		int[] counts = new int[BANK_TAB_VARBITS.length];
		for (int index = 0; index < BANK_TAB_VARBITS.length; index++)
		{
			counts[index] = client.getVarbitValue(BANK_TAB_VARBITS[index]);
		}
		return counts;
	}

	private boolean isFilteredBankView()
	{
		Widget bankTitle = client.getWidget(InterfaceID.Bankmain.TITLE);
		return client.getVarcIntValue(VarClientID.BANKTAGS_ACTIVE_TAG) != -1
			|| isBankTagTabTitle(bankTitle == null ? null : bankTitle.getText())
			|| isBankSearching(client.getVarcIntValue(VarClientID.MESLAYERMODE),
				client.getVarcStrValue(VarClientID.MESLAYERINPUT));
	}

	static boolean isBankTagTabTitle(String title)
	{
		if (title == null)
		{
			return false;
		}
		String trimmed = title.trim();
		return trimmed.regionMatches(true, 0, "Tag tab ", 0, "Tag tab ".length());
	}

	static boolean isBankSearching(int inputType, String inputText)
	{
		return inputType == 11
			|| inputType <= 0 && inputText != null && !inputText.isEmpty();
	}

	private void refreshPlanCache(BankOrganizationPreview preview)
	{
		if (cachedPreview == preview)
		{
			return;
		}
		cachedPreview = preview;
		cachedTabPlan = BankTabPlan.fromPreview(preview);
		cachedPlannedItems = cachedTabPlan.getFlattenedItems();
		cachedPlannedSlotByItemId = plannedSlotByItemId(cachedPlannedItems);
		tabRouteSession.reset();
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
		if (bankSlots.size() != actualItemIds.length)
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
		List<BankPreviewItem> planned = BankTabPlan.fromPreview(preview).getFlattenedItems();
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

	static String duplicateItemsMessage(List<String> names)
	{
		if (names.isEmpty())
		{
			return tabBlockedMessage(TabRouteAdvisor.Status.DUPLICATE_ITEMS);
		}

		int shown = Math.min(3, names.size());
		String more = names.size() > shown
			? " (+" + (names.size() - shown) + " more)" : "";
		return "Duplicate items detected:\n"
			+ String.join(", ", names.subList(0, shown)) + more
			+ "\nRelease duplicate placeholders,\nthen run Analyze My Bank again.";
	}

	static String tabBlockedMessage(TabRouteAdvisor.Status status)
	{
		switch (status)
		{
			case RESCAN_REQUIRED:
				return "Bank contents changed.\nRun Analyze My Bank again.";
			case DUPLICATE_ITEMS:
				return "Duplicate item IDs detected.\nAnalyze again after the bank compacts.";
			case UNSTABLE_BANK:
				return "Bank layout cannot be mapped safely.\nStop and Analyze My Bank again.";
			case UNSUPPORTED_PLAN:
				return "This blueprint contains unsupported blank targets.";
			case WAITING_FOR_BANK:
				return "MOVE RECEIVED\nSyncing the next guide...";
			case MANUAL_RECOVERY_REQUIRED:
				return "Unexpected bank move.\nUndo that move manually,\nor run Analyze My Bank again.";
			case MECHANICS_MISMATCH:
				return "Bank changed differently than expected.\nGuidance stopped.\nRun Analyze My Bank again.";
			default:
				return "No safe manual move is available.\nRun Analyze My Bank again.";
		}
	}

	static String viewSyncMessage(boolean numberedTab)
	{
		return numberedTab
			? "SYNCING BANK\nPreparing this tab's next guide...\nIf this remains, open All items."
			: "SYNCING BANK\nPreparing the next guide...\nIf this remains, check fillers or gaps.";
	}

	static int blockedProgressPercent(TabRouteAdvisor.Status status, int currentPercent)
	{
		return status == TabRouteAdvisor.Status.WAITING_FOR_BANK ? currentPercent : -1;
	}

	static String missingTabTargetMessage(MoveType type)
	{
		return type == MoveType.RETURN_TO_MAIN
			? "Show the vanilla bank tab bar.\nThe infinity (All items) target is unavailable."
			: "Show the vanilla bank tab bar.\nThe required tab or New tab target is unavailable.";
	}

	private String tabGuideText(TabRouteAdvisor.Assessment assessment, Set<Integer> visibleSlots,
		Map<Integer, BankSlotWidget> allByLogicalSlot, Rectangle viewportBounds)
	{
		TabRouteAdvisor.Progress progress = assessment.getProgress();
		String progressLine = progressText(progress);
		if (assessment.getStatus() == TabRouteAdvisor.Status.COMPLETE)
		{
			return "Blueprint complete: Main and blueprint tabs 2-10 match their final order.";
		}
		if (!config.suggestNextMove())
		{
			return progressLine + "\nNext-move highlight is disabled in plugin settings.";
		}

		Move move = assessment.getMove().get();
		String instruction;
			switch (move.getType())
		{
			case COLLAPSE_TAB:
				instruction = "Right-click bank tab position " + (move.getTargetTab() + 1)
					+ " (Main is position 1) and choose Collapse tab.";
				break;
			case DRAG_TO_NEW_TAB:
				instruction = "Drag " + move.getDisplayName() + " from Main to + to create "
					+ "blueprint tab " + move.getBlueprintTabNumber() + " ("
					+ move.getCategoryName() + ") at bank position "
					+ (move.getTargetTab() + 1) + ".";
				break;
			case DISTRIBUTE_TO_TAB:
				instruction = "Drag " + move.getDisplayName() + " from Main to blueprint tab "
					+ move.getBlueprintTabNumber() + " (" + move.getCategoryName()
					+ ", bank position " + (move.getTargetTab() + 1)
					+ "). Main is processed from top to bottom.";
				break;
			case TRANSFER_TO_TAB:
				instruction = "Recovery: drag misplaced " + move.getDisplayName()
					+ " from bank position " + (move.getSourceTab() + 1)
					+ " to blueprint tab " + move.getBlueprintTabNumber() + " ("
					+ move.getCategoryName() + ", bank position "
					+ (move.getTargetTab() + 1) + ").";
				break;
			case RETURN_TO_MAIN:
				instruction = "Recovery: drag misplaced " + move.getDisplayName()
					+ " from bank position " + (move.getSourceTab() + 1)
					+ " onto the infinity (All items) icon to return it to Main.";
				break;
			case SWAP_SECTION:
			default:
				String section = move.getTargetTab() == 0
					? "Main"
					: "blueprint tab " + move.getBlueprintTabNumber();
				instruction = "Sort " + section + " (" + move.getCategoryName()
					+ "): swap the highlighted FROM item to TO: "
					+ move.getDisplayName() + ".";
				break;
		}
		if (move.getFromSlot() >= 0 && !visibleSlots.contains(move.getFromSlot()))
		{
			BankSlotWidget source = allByLogicalSlot.get(move.getFromSlot());
			instruction += " Scroll " + offscreenDirection(
				source == null ? null : source.bounds, viewportBounds)
				+ " to FROM.";
		}
		return progressLine + "\n" + instruction;
	}

	private static String progressText(TabRouteAdvisor.Progress progress)
	{
		String phase;
		switch (progress.getPhase())
		{
			case RECOVERING:
				phase = "Recovering misplaced items";
				break;
			case REPAIRING:
				phase = "Repairing tab buckets";
				break;
			case CREATING:
				phase = "Creating tabs";
				break;
			case DISTRIBUTING:
				phase = "Distributing Main top-to-bottom";
				break;
			case SORTING:
				phase = "Sorting inside each tab";
				break;
			case COMPLETE:
			default:
				phase = "Blueprint complete";
				break;
		}
		String text = phase + ": " + progress.getPercent() + "% ("
			+ progress.getCompleted() + "/" + progress.getTotal() + ").";
		return progress.getPhase() == Phase.SORTING
			? text + " Minimum swaps remaining: " + progress.getMinimumRemainingSwaps() + "."
			: text;
	}

	static String tabHudText(TabRouteAdvisor.Assessment assessment, boolean suggestNextMove)
	{
		if (assessment.getStatus() == TabRouteAdvisor.Status.COMPLETE)
		{
			return "BLUEPRINT COMPLETE\nMain + tabs 2-10 are sorted";
		}
		Move move = assessment.getMove().orElse(null);
		if (!suggestNextMove)
		{
			return assessment.getProgress().getPhase().name() + "  "
				+ assessment.getProgress().getPercent() + "%\nHighlights disabled in settings";
		}
		if (move == null)
		{
			return progressText(assessment.getProgress());
		}
		String progress = assessment.getProgress().getPhase().name() + "  "
			+ assessment.getProgress().getPercent() + "%";
		if (assessment.getProgress().getPhase() == Phase.SORTING)
		{
			progress += "  MIN SWAPS " + assessment.getProgress().getMinimumRemainingSwaps();
		}
			switch (move.getType())
		{
			case COLLAPSE_TAB:
				return progress + "\nTAB POSITION " + (move.getTargetTab() + 1)
					+ ": RIGHT-CLICK\nChoose Collapse tab";
			case DRAG_TO_NEW_TAB:
				return progress + "\nFROM: " + move.getDisplayName()
					+ "\nTO + : NEW BP TAB " + move.getBlueprintTabNumber();
			case DISTRIBUTE_TO_TAB:
				return progress + "\nFROM: " + move.getDisplayName() + "\nTO BP TAB "
					+ move.getBlueprintTabNumber() + " / POS " + (move.getTargetTab() + 1);
			case TRANSFER_TO_TAB:
				return "RECOVERY\nFROM: " + move.getDisplayName() + " / POS "
					+ (move.getSourceTab() + 1) + "\nTO BP TAB "
					+ move.getBlueprintTabNumber() + " / POS " + (move.getTargetTab() + 1);
			case RETURN_TO_MAIN:
				return "RECOVERY\nFROM: " + move.getDisplayName() + " / POS "
					+ (move.getSourceTab() + 1) + "\nTO INFINITY -> MAIN";
			case SWAP_SECTION:
			default:
				String section = move.getTargetTab() == 0 ? "MAIN"
					: "TAB " + move.getBlueprintTabNumber();
				return progress + "  SORT " + section
					+ "\nFROM -> TO\n" + move.getDisplayName();
		}
	}

	static boolean isTabTargetMove(MoveType type)
	{
		return type == MoveType.COLLAPSE_TAB || type == MoveType.DRAG_TO_NEW_TAB
			|| type == MoveType.DISTRIBUTE_TO_TAB || type == MoveType.TRANSFER_TO_TAB
			|| type == MoveType.RETURN_TO_MAIN;
	}

	static boolean isGridSwap(MoveType type)
	{
		return type == MoveType.SWAP_SECTION;
	}

	private static Widget resolveTabTarget(Widget tabs, Move move)
	{
		boolean mainTarget = move.getType() == MoveType.RETURN_TO_MAIN;
		if (tabs == null || tabs.isHidden() || !mainTarget && (move.getTargetTab() < 1
			|| move.getTargetTab() > TabRouteAdvisor.MAX_TABS))
		{
			return null;
		}
		int childIndex = mainTarget ? 10 : tabActionChildIndex(move.getTargetTab());
		Widget child = tabs.getChild(childIndex);
		if (child == null || child.isHidden() || child.getIndex() != childIndex
			|| !isSafeGeometry(child.getBounds()))
		{
			return null;
		}
		String requiredAction = mainTarget ? "View all items"
			: move.getType() == MoveType.DRAG_TO_NEW_TAB ? "New tab"
				: move.getType() == MoveType.COLLAPSE_TAB ? "Collapse tab" : "View tab";
		return hasAction(child.getActions(), requiredAction) ? child : null;
	}

	static int tabActionChildIndex(int tabNumber)
	{
		return 10 + tabNumber;
	}

	static boolean hasAction(String[] actions, String expectedAction)
	{
		if (actions == null)
		{
			return false;
		}
		for (String action : actions)
		{
			if (expectedAction.equals(action))
			{
				return true;
			}
		}
		return false;
	}

	static String offscreenDirection(Rectangle slotBounds, Rectangle viewportBounds)
	{
		if (slotBounds == null)
		{
			return "off-screen";
		}
		if (slotBounds.y < viewportBounds.y) return "above this view";
		if (slotBounds.y + slotBounds.height
			> viewportBounds.y + viewportBounds.height) return "below this view";
		return "outside the visible bank area";
	}

	private static void drawSwapMove(Graphics2D graphics, int fromSlot, int toSlot,
		Map<Integer, BankSlotWidget> allByLogicalSlot,
		Map<Integer, BankSlotWidget> visibleByLogicalSlot, Rectangle viewportBounds)
	{
		BankSlotWidget source = visibleByLogicalSlot.get(fromSlot);
		BankSlotWidget target = visibleByLogicalSlot.get(toSlot);
		BankSlotWidget logicalSource = allByLogicalSlot.get(fromSlot);
		BankSlotWidget logicalTarget = allByLogicalSlot.get(toSlot);

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
			int targetY = edgeY(logicalTarget, viewportBounds);
			int targetX = edgeX(logicalTarget, centerX(source.bounds), viewportBounds);
			drawArrowConnector(graphics, centerX(source.bounds), centerY(source.bounds), targetX, targetY);
			drawMoveCell(graphics, source.bounds, MOVE_SOURCE, "FROM");
			drawEdgeBadge(graphics, targetX, targetY, MOVE_TARGET,
				"TO", targetY <= viewportBounds.y + 14, viewportBounds);
			return;
		}

		if (target != null)
		{
			int sourceY = edgeY(logicalSource, viewportBounds);
			int sourceX = edgeX(logicalSource, centerX(target.bounds), viewportBounds);
			drawArrowConnector(graphics, sourceX, sourceY, centerX(target.bounds), centerY(target.bounds));
			drawEdgeBadge(graphics, sourceX, sourceY, MOVE_SOURCE,
				"FROM", sourceY <= viewportBounds.y + 14, viewportBounds);
			drawMoveCell(graphics, target.bounds, MOVE_TARGET, "TO");
			return;
		}

		// No endpoint is visible. Use the source widget's actual off-screen
		// geometry to point the player toward FROM.
		int sourceY = edgeY(logicalSource, viewportBounds);
		int sourceX = viewportBounds.x + viewportBounds.width / 2;
		if (logicalSource != null)
		{
			sourceX = clamp(centerX(logicalSource.bounds), viewportBounds.x + 14,
				viewportBounds.x + viewportBounds.width - 14);
		}
		drawEdgeBadge(graphics, sourceX, sourceY, MOVE_SOURCE,
			"FROM", sourceY <= viewportBounds.y + 14, viewportBounds);
	}

	private static void drawTabMove(Graphics2D graphics, Move move, Rectangle targetBounds,
		Map<Integer, BankSlotWidget> allByLogicalSlot,
		Map<Integer, BankSlotWidget> visibleByLogicalSlot, Rectangle viewportBounds)
	{
		String targetLabel = move.getType() == MoveType.RETURN_TO_MAIN
			? "ALL -> MAIN"
			: move.getType() == MoveType.COLLAPSE_TAB
			? "POS " + (move.getTargetTab() + 1)
			: move.getType() == MoveType.DRAG_TO_NEW_TAB
				? "NEW BP " + move.getBlueprintTabNumber()
				: "BP " + move.getBlueprintTabNumber();
		if (move.getType() == MoveType.COLLAPSE_TAB)
		{
			drawMoveCell(graphics, targetBounds, MOVE_TARGET, targetLabel);
			return;
		}

		BankSlotWidget source = visibleByLogicalSlot.get(move.getFromSlot());
		BankSlotWidget logicalSource = allByLogicalSlot.get(move.getFromSlot());
		if (source != null)
		{
			drawArrowConnector(graphics, centerX(source.bounds), centerY(source.bounds),
				centerX(targetBounds), centerY(targetBounds));
			drawMoveCell(graphics, source.bounds, MOVE_SOURCE, "FROM");
			drawMoveCell(graphics, targetBounds, MOVE_TARGET, targetLabel);
			return;
		}

		int sourceY = edgeY(logicalSource, viewportBounds);
		int sourceX = logicalSource == null ? viewportBounds.x + viewportBounds.width / 2
			: clamp(centerX(logicalSource.bounds), viewportBounds.x + 14,
				viewportBounds.x + viewportBounds.width - 14);
		drawArrowConnector(graphics, sourceX, sourceY, centerX(targetBounds), centerY(targetBounds));
		drawEdgeBadge(graphics, sourceX, sourceY, MOVE_SOURCE,
			"FROM", sourceY <= viewportBounds.y + 14, viewportBounds);
		drawMoveCell(graphics, targetBounds, MOVE_TARGET, targetLabel);
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

	private static int edgeY(BankSlotWidget offscreenSlot, Rectangle viewportBounds)
	{
		return offscreenSlot != null && offscreenSlot.bounds.y < viewportBounds.y
			? viewportBounds.y + 7
			: viewportBounds.y + viewportBounds.height - 7;
	}

	private static int clamp(int value, int minimum, int maximum)
	{
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static void drawStatus(Graphics2D graphics, Rectangle gridBounds, String text)
	{
		drawStatus(graphics, gridBounds, text, false, null, null);
	}

	private static void drawStatus(Graphics2D graphics, Rectangle gridBounds, String text,
		boolean preferOutside, Rectangle firstAvoid, Rectangle secondAvoid)
	{
		Graphics2D statusGraphics = (Graphics2D) graphics.create();
		try
		{
			Font font = statusGraphics.getFont().deriveFont(Font.BOLD, 14f);
			statusGraphics.setFont(font);
			FontMetrics metrics = statusGraphics.getFontMetrics(font);
			String[] rawLines = text.split("\\n", -1);
			String[] lines = new String[rawLines.length];
			int maxTextWidth = Math.max(20, Math.min(280, gridBounds.width - 24));
			int width = 0;
			for (int i = 0; i < rawLines.length; i++)
			{
				lines[i] = fitText(rawLines[i], metrics, maxTextWidth);
				width = Math.max(width, metrics.stringWidth(lines[i]));
			}
			int lineHeight = metrics.getHeight();
			int height = lineHeight * lines.length + 10;
			Rectangle statusBounds = statusBounds(statusGraphics.getClipBounds(), gridBounds,
				width + 14, height, firstAvoid, secondAvoid, preferOutside);
			int x = statusBounds.x;
			int y = statusBounds.y;

			statusGraphics.setColor(STATUS_BACKGROUND);
			statusGraphics.fillRoundRect(x, y, width + 14, height, 7, 7);
			statusGraphics.setColor(STATUS_FOREGROUND);
			for (int i = 0; i < lines.length; i++)
			{
				statusGraphics.drawString(lines[i], x + 7,
					y + 5 + metrics.getAscent() + i * lineHeight);
			}
		}
		finally
		{
			statusGraphics.dispose();
		}
	}

	static Rectangle statusBounds(Rectangle canvasBounds, Rectangle gridBounds,
		int width, int height, Rectangle firstAvoid, Rectangle secondAvoid,
		boolean preferOutside)
	{
		Rectangle canvas = isSafeGeometry(canvasBounds) ? canvasBounds : gridBounds;
		List<Rectangle> candidates = new ArrayList<>();
		int gap = 6;
		if (preferOutside)
		{
			candidates.add(new Rectangle(gridBounds.x - width - gap, gridBounds.y + 4,
				width, height));
			candidates.add(new Rectangle(gridBounds.x + gridBounds.width + gap,
				gridBounds.y + 4, width, height));
		}
		candidates.add(new Rectangle(gridBounds.x + gridBounds.width - width - 4,
			gridBounds.y + 4, width, height));
		candidates.add(new Rectangle(gridBounds.x + 4, gridBounds.y + 4, width, height));
		candidates.add(new Rectangle(gridBounds.x + gridBounds.width - width - 4,
			gridBounds.y + gridBounds.height - height - 4, width, height));
		candidates.add(new Rectangle(gridBounds.x + 4,
			gridBounds.y + gridBounds.height - height - 4, width, height));

		for (Rectangle candidate : candidates)
		{
			if (canvas.contains(candidate) && !intersects(candidate, firstAvoid)
				&& !intersects(candidate, secondAvoid))
			{
				return candidate;
			}
		}
		for (Rectangle candidate : candidates)
		{
			if (canvas.contains(candidate))
			{
				return candidate;
			}
		}
		return new Rectangle(gridBounds.x + 4, gridBounds.y + 4,
			Math.min(width, Math.max(1, gridBounds.width - 8)),
			Math.min(height, Math.max(1, gridBounds.height - 8)));
	}

	private static boolean intersects(Rectangle candidate, Rectangle avoid)
	{
		if (!isSafeGeometry(avoid))
		{
			return false;
		}
		Rectangle expanded = new Rectangle(avoid);
		expanded.grow(4, 4);
		return candidate.intersects(expanded);
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
		return isSafeGeometry(intersection) ? intersection : null;
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
